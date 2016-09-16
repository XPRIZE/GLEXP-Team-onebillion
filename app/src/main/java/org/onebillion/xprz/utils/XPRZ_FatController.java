package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;
import android.os.Handler;

import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.OBSectionController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michal on 08/08/16.
 */
public class XPRZ_FatController extends OBFatController
{

    public long firstUnstartedIndex;
    public int scoreCorrect,scoreWrong;
    public float finalScore;
    public XPRZ_FatReceiver menu;
    public long sessionSegmentStartTime,sessionSegmentLastActive,sessionTotalTime;

    public static final int OFC_SUCCEEDED = 1,
            OFC_FINISHED_LOW_SCORE = 2,
            OFC_TIMED_OUT = 3,
            OFC_SESSION_TIMED_OUT = 4,
            OFC_UNIT_LIST = 5,
            OFC_FIRST_TIME_IN = 6,
            OFC_NEW_SESSION =7;

    private static final long SESSION_TIMEOUT_MIN = 60;
    private static final int MAX_UNIT_ATTEMPTS_COUNT = 3;

    private MlUnitInstance currentUnitInstance;
    private OBUser currentUser;
    private int currentSessionId;
    private long currentSessionStartTime, currentSessionEndTime;

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    public long getCurrentTime()
    {
        return  System.currentTimeMillis()/1000;
    }

    public void loadMasterListIntoDB()
    {
        DBSQL db = new DBSQL(true);
        String token = OBPreferenceManager.getStringPreference(OBPreferenceManager.PREFERENCE_ML_TOKEN, db);
        try
        {
            String mlname = (String) MainActivity.mainActivity.config.get(MainActivity.CONFIG_MASTER_LIST);
            OBXMLManager xmlManager = new OBXMLManager();
            InputStream is = OBUtils.getInputStreamForPath(String.format("config/%s", mlname));
            List<OBXMLNode> xml = xmlManager.parseFile(is);
            OBXMLNode rootNode = xml.get(0);
            List<OBXMLNode> masterList = new ArrayList<>();
            String masterListToken = rootNode.attributeStringValue("token");
            if(token == null || !token.equals(masterListToken))
            {
                db.beginTransaction();
                try
                {
                    db.doDeleteOnTable(DBSQL.TABLE_UNITS, null);
                    int unitid = 0;
                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        masterList.addAll(levelNode.childrenOfType("unit"));
                        for (OBXMLNode node : levelNode.childrenOfType("unit"))
                        {
                            MlUnit.insertUnitFromXMLNodeintoDB(db, node, unitid, levelNode.attributeIntValue("id"));
                            unitid++;
                        }
                    }
                    OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCE_ML_TOKEN,masterListToken, db);
                    db.setTransactionSuccessful();
                } finally
                {
                    db.commitTransaction();
                }
            }

        } catch (Exception e)
        {

        }
        db.close();

    }


    public void loadUser()
    {
        DBSQL db = new DBSQL(true);
        OBUser u = OBUser.lastUserFromDB(db);
        if (u == null)
        {
            u = OBUser.initAndSaveUserInDB(db,"Student");
            currentUser = u;
            currentSessionId = -1;
            startNewDayInDB(db);
        }
        else
        {
            currentUser = u;
            loadLastSessionFromDB(db,u.userid);
        }
        int lastUnitID = currentUser.lastUnitIDFromDB(db);
        if(unitAttemtpsCountInDB(db,lastUnitID) >= MAX_UNIT_ATTEMPTS_COUNT)
            lastUnitID++;

        firstUnstartedIndex = lastUnitID;
        db.close();
        checkAndStartNewSession();
    }

    public void deleteDBFile()
    {
        DBSQL.deleteDB();
    }

    @Override
    public void startUp()
    {
        initDB();
        timeoutHandler = new Handler();
        String menuClassName = (String)MainActivity.mainActivity.config.get("menuclass");
        if (showTestMenu())
            menuClassName = "XPRZ_TestMenu";
        if (menuClassName != null)
            OBMainViewController.MainViewController().pushViewControllerWithName(menuClassName,false,false,"menu");
    }

    public void initDB()
    {
        loadMasterListIntoDB();
        loadUser();

    }

    public void continueFromLastUnit()
    {
        DBSQL db = new DBSQL(false);
        firstUnstartedIndex = lastPlayedUnitIndex(db) + 1;
        db.close();
    }

    @Override
    public void initScores()
    {
        if(currentUnitInstance == null)
            return;
        scoreCorrect = scoreWrong = 0;
    }

    @Override
    public void gotAnswerRight(int amount)
    {
        scoreCorrect += amount;
    }

    @Override
    public void gotAnswerWrong(int amount)
    {
        scoreWrong += amount;
    }

    @Override
    public void completeEvent(OBSectionController cont)
    {
        cancelTimeout();
        updateScores();
        if (finalScore >= currentUnit().passThreshold)
            signalSectionSucceeded();
        else
            signalSectionFailed();

        currentUnitInstance = null;

        try
        {
            cont.displayAward();
        }catch (Exception e)
        {

        }
        cont.exitEvent();

    }

    public void timeOutEvent(OBSectionController cont)
    {
        DBSQL db = new DBSQL(true);
        int count = unitAttemtpsCountInDB(db,currentUnitInstance.mlUnit.unitid);
        if(count >= MAX_UNIT_ATTEMPTS_COUNT)
            signalSectionFailed();
        else
            signalSectionTimedOut();

        db.close();
        currentUnitInstance = null;

        cont.exitEvent();

    }

    public boolean showTestMenu()
    {
        return true;
    }


    @Override
    public void updateScores()
    {
        DBSQL db = new DBSQL(true);
        int tot = scoreCorrect + scoreWrong;
        finalScore = 1;
        if (tot > 0)
            finalScore = scoreCorrect * 1.0f / tot;

        currentUnitInstance.endtime = getCurrentTime();
        currentUnitInstance.score = finalScore;
        currentUnitInstance.elapsedtime = (int)(currentUnitInstance.endtime - currentUnitInstance.starttime);
        currentUnitInstance.updateDataInDB(db);
        checkCurrentSessionTimeout(db);
        db.close();
    }

    public Map<String,Object> commandWith(int code, MlUnit unit)
    {
        Map<String,Object> command = new ArrayMap<>();
        command.put("code", code);
        command.put("unit", unit);
        return command;
    }

    public void signalSectionFailed()
    {
        menu.receiveCommand(commandWith(OFC_FINISHED_LOW_SCORE,currentUnit()));
    }

    public void signalSectionSucceeded()
    {
        menu.receiveCommand(commandWith(OFC_SUCCEEDED,currentUnit()));
    }

    public void signalSectionTimedOut()
    {
        menu.receiveCommand(commandWith(OFC_TIMED_OUT,currentUnit()));
    }

    public void signalSessionTimedOut()
    {
        menu.receiveCommand(commandWith(OFC_SESSION_TIMED_OUT,currentUnit()));
    }

    public List<MlUnit> requestNextUnits(int count)
    {
        List<MlUnit> arr = new ArrayList<>();
        for (int i = 0;i < count;i++)
        {
            arr.add(MlUnit.mlUnitFromDBforUnitID(currentUnit().unitid + 1 + i));
        }
        return arr;
    }

    public MlUnit requestNextUnit()
    {
        //do all the black magic of finding out what's the next topic here
        return MlUnit.mlUnitFromDBforUnitID(firstUnstartedIndex);
    }


    public MlUnit currentUnit()
    {
        if(currentUnitInstance == null)
            return null;
        else
            return currentUnitInstance.mlUnit;
    }
    public void sectionStartedWithUnit(MlUnit unit)
    {
        if(unit.unitid >= firstUnstartedIndex)
        {
            firstUnstartedIndex = unit.unitid+1;
        }
        currentUnitInstance = MlUnitInstance.initMlUnitDBWith(unit,currentUser.userid,currentSessionId,getCurrentTime());
        initScores();
    }

    public void startSectionByUnit(final MlUnit unit)
    {

        sectionStartedWithUnit(unit);
        final String lastAppCode = (String)MainActivity.mainActivity.config.get(MainActivity.CONFIG_APP_CODE);

        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    MainActivity.mainActivity.updateConfigPaths(unit.config, false, unit.lang);
                    if(OBMainViewController.MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {
                        currentUnitInstance.sectionController = OBMainViewController.MainViewController().topController();
                        startCurrentUnitInstanceTimeout();
                    }
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);

                    MainActivity.mainActivity.updateConfigPaths(lastAppCode, false, null);
                }
            }
        }.run();

    }

    public void userActivityOccurred(long systime)
    {
        sessionSegmentLastActive = systime;
    }

    public long lastPlayedUnitIndex(DBSQL db)
    {
        return MlUnitInstance.lastPlayedUnitIndexForUserIDInDB(db, currentUser.userid);
    }

    public void resetDatabase()
    {
        DBSQL.deleteDB();
        initDB();

    }

    public void refreshUnitsList()
    {
        DBSQL db = new DBSQL(true);
        db.doDeleteOnTable(DBSQL.TABLE_UNITS,null);
        db.close();
        loadMasterListIntoDB();
    }


    public int getLastCommand()
    {
        if(currentSessionFinished())
            return OFC_SESSION_TIMED_OUT;
        else if(!currentSessionHasProgress())
            return OFC_NEW_SESSION;
        else
            return OFC_SUCCEEDED;

    }

    public void checkCurrentSessionTimeout(DBSQL db)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("sessionid",String.valueOf(currentSessionId));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES, Collections.singletonList("SUM(elapsedtime) as elapsedtime"), whereMap);
        long elapsedTime = 0;
        if(cursor.moveToFirst())
            elapsedTime = cursor.getLong(cursor.getColumnIndex("elapsedtime"));

        cursor.close();

        if(elapsedTime/60.0 > 0.2)
            finishCurrentDayInDB(db);

        db.close();
    }

    public int unitAttemtpsCountInDB(DBSQL db, int unitid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("unitid",String.valueOf(unitid));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES, Collections.singletonList("COUNT(*) as count"), whereMap);
        int count = 0;
        if(cursor.moveToFirst())
            count = cursor.getInt(cursor.getColumnIndex("count"));

        cursor.close();

        return count;
    }

    public boolean currentSessionFinished()
    {
        if(currentSessionId < 0)
            return true;

        return currentSessionEndTime > 0;
    }

    public boolean checkAndStartNewSession()
    {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar calendarLastSession = Calendar.getInstance();
        calendarLastSession.setTimeInMillis(currentSessionStartTime*1000);

        if(currentCalendar.get(Calendar.DAY_OF_YEAR) != calendarLastSession.get(Calendar.DAY_OF_YEAR) ||
                currentCalendar.get(Calendar.YEAR) != calendarLastSession.get(Calendar.YEAR))
        {
            startNewDay();
            return true;
        }
        return false;
    }

    private void loadLastSessionFromDB(DBSQL db, int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        currentSessionId = -1;
        currentSessionEndTime = currentSessionStartTime = 0;
        try
        {
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_SESSIONS, Arrays.asList("MAX(sessionid) as sessionid", "starttime", "endtime"), whereMap);
            if (cursor.moveToFirst())
            {
                currentSessionStartTime = cursor.getLong(cursor.getColumnIndex("starttime"));
                currentSessionEndTime = cursor.getLong(cursor.getColumnIndex("endtime"));
                currentSessionId = cursor.getInt(cursor.getColumnIndex("sessionid"));
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean currentSessionHasProgress()
    {
        if(currentSessionId < 0)
            return false;

        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("sessionid",String.valueOf(currentSessionId));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES,Collections.singletonList("COUNT(*) as count"),whereMap);
        boolean result = false;
        if(cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex("count")) > 0;

        cursor.close();
        db.close();
        return result;
    }

    public void startNewDay()
    {
        DBSQL db = new DBSQL(true);
        startNewDayInDB(db);
        db.close();
    }


    public void startNewDayInDB(DBSQL db)
    {
        if(!currentSessionFinished())
            finishCurrentDayInDB(db);

        currentSessionStartTime = getCurrentTime();
        currentSessionEndTime = 0;

        int sessionid = currentSessionId;
        if(sessionid<0)
            sessionid = 1;
        else
            sessionid++;

        ContentValues contentValues = new ContentValues();
        contentValues.put("userid", currentUser.userid);
        contentValues.put("sessionid", sessionid);
        contentValues.put("starttime",currentSessionStartTime);
        db.doInsertOnTable(DBSQL.TABLE_SESSIONS,contentValues);
        currentSessionId = sessionid;

    }

    public void finishCurrentDayInDB(DBSQL db)
    {
        if(currentSessionId < 0)
            return;


        currentSessionEndTime =  getCurrentTime();
        Map<String,String> whereMap  = new ArrayMap<>();

        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("sessionid",String.valueOf(currentSessionId));
        ContentValues contentValues = new ContentValues();
        contentValues.put("endtime",currentSessionEndTime);
        db.doUpdateOnTable(DBSQL.TABLE_SESSIONS,whereMap, contentValues);
    }

    public void saveStarForUnit(MlUnit unit,String colour)
    {
        if(unit == null)
            return;

        DBSQL db = new DBSQL(true);
        ContentValues contentValues = new ContentValues();
        contentValues.put("userid",currentUser.userid);
        contentValues.put("level",unit.level);
        contentValues.put("starnum",unit.awardStar);
        contentValues.put("colour",colour);

        boolean result = db.doReplaceOnTable(DBSQL.TABLE_STARS,contentValues) > 0;

        db.close();
    }

    public Map<Integer,String> starsForLevel(int level)
    {
        DBSQL db = new DBSQL(false);
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("level",String.valueOf(level));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS,Arrays.asList("starnum","colour"),whereMap);

        Map<Integer,String> result = new ArrayMap<>();
        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                result.put(cursor.getInt(cursor.getColumnIndex("starnum")), cursor.getString(cursor.getColumnIndex("colour")));
                cursor.moveToNext();
            }

        }
        cursor.close();
        db.close();
        return result;
    }

    public String lastStarColourForLevel(int level)
    {
        DBSQL db = new DBSQL(false);
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("level",String.valueOf(level));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS, Arrays.asList("MAX(starnum) as starnum","colour"),whereMap);

        String result = null;
        if(cursor.moveToFirst())
            result = cursor.getString(cursor.getColumnIndex("colour"));

        cursor.close();
        db.close();
        return result;
    }

    public String starForLevel(int level,int starnum)
    {
        DBSQL db = new DBSQL(false);
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("level",String.valueOf(level));
        whereMap.put("starnum",String.valueOf(starnum));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS, Collections.singletonList("colour"),whereMap);
        String result = null;
        if(cursor.moveToFirst())
            result = cursor.getString(cursor.getColumnIndex("colour"));

        cursor.close();
        db.close();
        return result;
    }

    public boolean shouldAwardStar(MlUnit unit)
    {
        if(unit == null)
            return false;

        if(unit.awardStar < 0)
            return false;

        return starForLevel(unit.level,unit.awardStar) == null;
    }

    public boolean saveCertificateInDBwithFile(String fileName, int level)
    {
        ContentValues data = new ContentValues();
        data.put("userid",currentUser.userid);
        data.put("file",fileName);
        data.put("level",level);
        DBSQL db = new DBSQL(true);
        boolean result =  db.doInsertOnTable(DBSQL.TABLE_CERTIFICATES,data) > 0;
        db.close();
        return result;
    }

    public String certificateFromDBforLevel(int level)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("level",String.valueOf(level));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_CERTIFICATES, Collections.singletonList("file"),whereMap);
        String file = null;
        if(cursor.moveToFirst())
        {
            file =  cursor.getString(cursor.getColumnIndex("file"));
        }
        cursor.close();
        db.close();
        return file;
    }

    public int lastCertificateLevelFromDB()
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        DBSQL db = new DBSQL(false);
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_CERTIFICATES, Collections.singletonList("MAX(level) as level"),whereMap);
        int level = -1;
        if(cursor.moveToFirst())
        {
            level = cursor.getInt(cursor.getColumnIndex("level"));
        }
        cursor.close();
        db.close();
        return level;
    }

    @Override
    public void onPause(OBSectionController cont)
    {

    }

    @Override
    public void onResume(OBSectionController cont)
    {
        checkTimeout(currentUnitInstance);
    }

    @Override
    public void onExitSection(OBSectionController cont)
    {
        cancelTimeout();
    }


    public void startCurrentUnitInstanceTimeout()
    {
        cancelTimeout();

        final MlUnitInstance currentInstance = currentUnitInstance;
        timeoutRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(checkTimeout(currentInstance))
                    timeoutHandler.postDelayed(this,1*1000);
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable,10000); //currentInstance.mlUnit.targetDurationstance.)

    }

    public void cancelTimeout()
    {
        if(timeoutRunnable != null)
            timeoutHandler.removeCallbacks(timeoutRunnable);

        timeoutRunnable = null;
    }

    public boolean checkTimeout(MlUnitInstance unitInstance)
    {
        if (!allowsTimeOut())
            return false;

        if(unitInstance != currentUnitInstance)
            return false;

        if(unitInstance.sectionController == null || unitInstance.sectionController._aborting)
            return false;

        if((unitInstance.starttime + 20) < getCurrentTime())
        {
            MainActivity.log("Time out!!");
            timeOutEvent(unitInstance.sectionController);
            return false;
        }
        return true;
    }


    public boolean allowsTimeOut()
    {
        String value = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_ALLOWS_TIMEOUT);
        return (value != null && value.equals("true"));
    }

}
