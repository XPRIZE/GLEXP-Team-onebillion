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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michal on 08/08/16.
 */
public class XPRZ_FatController extends OBFatController
{

    private long firstUnstartedIndex;
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

    private static final long SESSION_TIMEOUT = 60*60;
    private static final int MAX_UNIT_ATTEMPTS_COUNT = 3;
    private static final int UNIT_TIMEOUT = 15*60;

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
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            String token = OBPreferenceManager.getStringPreference(OBPreferenceManager.PREFERENCE_ML_TOKEN, db);
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
                    int startAudio = 0;
                    int nextAudio = OB_Maths.randomInt(6,8);
                    db.doDeleteOnTable(DBSQL.TABLE_UNITS, null);
                    int unitid = 0;
                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        int level = levelNode.attributeIntValue("id");
                        masterList.addAll(levelNode.childrenOfType("unit"));
                        List<OBXMLNode> nodes = levelNode.childrenOfType("unit");

                        for (int i=0; i<nodes.size(); i++)
                        {
                            OBXMLNode node = nodes.get(i);
                            int currentStartAudio = -1;
                            if(unitid == nextAudio)
                            {
                                if((i <= 1) && (i >= nodes.size()-2))
                                {
                                    nextAudio++;
                                }
                                else
                                {
                                    currentStartAudio = startAudio;
                                    nextAudio += OB_Maths.randomInt(6,8);
                                    startAudio++;
                                    if(startAudio > 7)
                                        startAudio = 0;
                                }
                            }

                            MlUnit.insertUnitFromXMLNodeintoDB(db, node, unitid,level,currentStartAudio);
                            unitid++;
                        }
                    }
                    OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCE_ML_TOKEN,masterListToken, db);
                    db.setTransactionSuccessful();
                }
                finally
                {
                    db.commitTransaction();
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.close();
        }


    }


    public void loadUser()
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            OBUser u = OBUser.lastUserFromDB(db);
            if (u == null)
            {
                u = OBUser.initAndSaveUserInDB(db, "Student");
                currentUser = u;
                currentSessionId = -1;
                startNewDayInDB(db, u.userid);
            } else
            {
                currentUser = u;
                loadLastSessionFromDB(db, u.userid);
                loadLastUnitIndexFromDB(db);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.close();
        }

        checkAndStartNewSession();
    }

    public void loadLastUnitIndexFromDB(DBSQL db)
    {
        int lastUnitID = currentUser.lastUnitIDFromDB(db);
        if (unitAttemtpsCountInDB(db, lastUnitID) < MAX_UNIT_ATTEMPTS_COUNT)
            lastUnitID--;

        firstUnstartedIndex = lastUnitID + 1;
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
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            loadLastUnitIndexFromDB(db);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
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
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            int count = unitAttemtpsCountInDB(db,currentUnitInstance.mlUnit.unitid);
            if(count >= MAX_UNIT_ATTEMPTS_COUNT)
            {
                signalSectionFailed();
            }
            else
            {
                signalSectionTimedOut();
            }
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
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
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            int tot = scoreCorrect + scoreWrong;
            finalScore = 1;
            if (tot > 0)
                finalScore = scoreCorrect * 1.0f / tot;

            currentUnitInstance.endtime = getCurrentTime();
            currentUnitInstance.score = finalScore;
            currentUnitInstance.elapsedtime = (int)(currentUnitInstance.endtime - currentUnitInstance.starttime);
            currentUnitInstance.updateDataInDB(db);
            checkCurrentSessionTimeout(db);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
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
            arr.add(MlUnit.mlUnitforUnitID(currentUnit().unitid + 1 + i));
        }
        return arr;
    }

    public MlUnit requestNextUnit()
    {
        return MlUnit.mlUnitforUnitID(firstUnstartedIndex);
    }

    private MlUnit currentUnit()
    {
        if(currentUnitInstance == null)
            return null;
        else
            return currentUnitInstance.mlUnit;
    }

    public long getLastUnitId()
    {
        return firstUnstartedIndex -1;
    }

    public void setCurrentUnitId(long unitid)
    {
        firstUnstartedIndex = unitid;
    }

    public Map<String,Object> getLastCommand()
    {
        MlUnit unit = null;
        int code = -1;
        DBSQL db = new DBSQL(false);
        try
        {
            unit = MlUnit.mlUnitforUnitIDFromDB(db,getLastUnitId());

            if(currentSessionFinished())
                code = OFC_SESSION_TIMED_OUT;
            else if(sessionUnitCountFromDB(db, currentSessionId, currentUser.userid) == 0)
                code =  OFC_NEW_SESSION;
            else
                code = OFC_SUCCEEDED;
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }

        return commandWith(code,unit);
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
                   // if(OBMainViewController.MainViewController().pushViewControllerWithNameConfig("X_TestEvent","x-miniapp6",true,true,"test"))
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

    public long lastPlayedUnitIndexFromDB(DBSQL db)
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
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            db.doDeleteOnTable(DBSQL.TABLE_UNITS,null);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
        loadMasterListIntoDB();
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

        if(elapsedTime > SESSION_TIMEOUT)
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
        currentCalendar.setTimeInMillis(getCurrentTime()*1000);
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
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_SESSIONS, Arrays.asList("MAX(sessionid) as sessionid", "starttime", "endtime"), whereMap);
        if (cursor.moveToFirst())
        {
            currentSessionStartTime = cursor.getLong(cursor.getColumnIndex("starttime"));
            currentSessionEndTime = cursor.getLong(cursor.getColumnIndex("endtime"));
            currentSessionId = cursor.getInt(cursor.getColumnIndex("sessionid"));
        }
        cursor.close();
    }

    public int currentSessionUnitCount()
    {
        if(currentSessionId < 0)
            return 0;

        int result = 0;

        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            result =  sessionUnitCountFromDB(db, currentSessionId, currentUser.userid);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
        return result;
    }

    private int sessionUnitCountFromDB(DBSQL db, int sessionid, int userid)
    {
        if(sessionid < 0)
            return 0;

        int result = 0;

        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("sessionid",String.valueOf(sessionid));

        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES,Collections.singletonList("COUNT(DISTINCT(unitid)) as count"),whereMap);

        if(cursor.moveToFirst())
            result = cursor.getInt(cursor.getColumnIndex("count"));

        cursor.close();
        return result;
    }

    public void startNewDay()
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            startNewDayInDB(db, currentUser.userid);
            loadLastUnitIndexFromDB(db);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
    }


    public void startNewDayInDB(DBSQL db, int userid)
    {
        if(!currentSessionFinished())
            finishCurrentDayInDB(db);

        fixMissingStarsInDB(db);

        currentSessionStartTime = getCurrentTime();
        currentSessionEndTime = 0;

        int sessionid = currentSessionId;
        if(sessionid<0)
            sessionid = 1;
        else
            sessionid++;

        ContentValues contentValues = new ContentValues();
        contentValues.put("userid", userid);
        contentValues.put("sessionid", sessionid);
        contentValues.put("starttime",currentSessionStartTime);
        db.doInsertOnTable(DBSQL.TABLE_SESSIONS,contentValues);
        currentSessionId = sessionid;

        loadLastUnitIndexFromDB(db);
    }

    public void fixMissingStarsInDB(DBSQL db)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT TAB.level as level, TAB.awardStar as awardStar " +
                "FROM (SELECT DISTINCT(UI.unitid), userid, level, awardStar " +
                "FROM "+DBSQL.TABLE_UNITS+" AS U JOIN "+DBSQL.TABLE_UNIT_INSTANCES+" AS UI ON UI.unitid = U.unitid  " +
                "WHERE (endtime>0 OR (SELECT COUNT(*) FROM "+DBSQL.TABLE_UNIT_INSTANCES+" WHERE unitid = UI.unitid AND userid = UI.userid) >= %d) AND userid = %d AND awardStar >0) TAB " +
                "LEFT JOIN "+DBSQL.TABLE_STARS+" AS S ON  TAB.userid = S.userid AND TAB.level = S.level AND TAB.awardStar = S.starnum WHERE S.userid IS NULL",
                MAX_UNIT_ATTEMPTS_COUNT , currentUser.userid)
                ,null);
        Map<Integer,List<Integer>> result = new ArrayMap<>();
        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                int level = cursor.getInt(cursor.getColumnIndex("level"));
                int awardStar = cursor.getInt(cursor.getColumnIndex("awardStar"));
                if(result.get(level) == null)
                    result.put(level, new ArrayList<Integer>());
                result.get(level).add(awardStar);
                cursor.moveToNext();
            }

        }
        cursor.close();

        for(int level : result.keySet())
        {
            String colour = lastStarColourForLevelFromDB(db, currentUser.userid, level);
            if (colour == null)
                colour = String.valueOf(OB_Maths.randomInt(1, 5));
            for(int awardNum : result.get(level))
                saveStarForUnitInDB(db,currentUser.userid,level,awardNum,colour);
        }
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

        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            saveStarForUnitInDB(db,currentUser.userid,unit.level,unit.awardStar,colour);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
    }

    public void saveStarForUnitInDB(DBSQL db, int userid, int level, int awardStar,String colour)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("userid",userid);
        contentValues.put("level",level);
        contentValues.put("starnum",awardStar);
        contentValues.put("colour",colour);

        boolean result = db.doReplaceOnTable(DBSQL.TABLE_STARS,contentValues) > 0;

    }

    public Map<Integer,String> starsForLevel(int level)
    {
        Map<Integer,String> result = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            result = starsForLevelFromDB(db,level);
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
        return result;
    }

    private Map<Integer,String> starsForLevelFromDB(DBSQL db, int level)
    {
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

        return result;
    }

    public String lastStarColourForLevelFromDB(DBSQL db, int userid, int level)
    {
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        whereMap.put("level",String.valueOf(level));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS, Arrays.asList("MAX(starnum) as starnum","colour"),whereMap);

        String result = null;
        if(cursor.moveToFirst())
            result = cursor.getString(cursor.getColumnIndex("colour"));

        cursor.close();
        return result;
    }

    public String starForLevel(int level,int starnum)
    {
        String result = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Map<String,String> whereMap = new ArrayMap<>();
            whereMap.put("userid",String.valueOf(currentUser.userid));
            whereMap.put("level",String.valueOf(level));
            whereMap.put("starnum",String.valueOf(starnum));
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_STARS, Collections.singletonList("colour"),whereMap);

            if(cursor.moveToFirst())
                result = cursor.getString(cursor.getColumnIndex("colour"));

            cursor.close();
        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
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
                    timeoutHandler.postDelayed(this,60*1000);
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable,UNIT_TIMEOUT*1000); //currentInstance.mlUnit.targetDurationstance.)

    }

    public void cancelTimeout()
    {
        if(timeoutRunnable != null)
            timeoutHandler.removeCallbacks(timeoutRunnable);

        timeoutRunnable = null;
    }

    public boolean checkTimeout(MlUnitInstance unitInstance)
    {
        if(unitInstance != currentUnitInstance)
            return false;

        if(unitInstance.sectionController == null || unitInstance.sectionController._aborting)
            return false;

        if((unitInstance.starttime + UNIT_TIMEOUT) < getCurrentTime())
        {
            timeOutEvent(unitInstance.sectionController);
            return false;
        }
        return true;
    }

}
