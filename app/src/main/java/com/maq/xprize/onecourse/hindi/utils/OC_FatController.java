package com.maq.xprize.onecourse.hindi.utils;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.ArrayMap;
import android.os.Handler;
import android.widget.Toast;

import com.maq.xprize.onecourse.hindi.R;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OBMainViewController;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.mainui.oc_playzone.OC_PlayZoneAsset;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.maq.xprize.onecourse.hindi.mainui.OBViewController.MainViewController;

/**
 * Created by michal on 08/08/16.
 */


public class OC_FatController extends OBFatController
{

    private int firstUnstartedIndex;
    public int scoreCorrect,scoreWrong;
    public float finalScore;
    public OC_FatReceiver menu;
    public long sessionSegmentLastActive;

    public static final int OFC_SUCCEEDED = 1,
            OFC_FINISHED_LOW_SCORE = 2,
            OFC_TIMED_OUT = 3,
            OFC_SESSION_TIMED_OUT = 4,
            OFC_NEW_SESSION = 5,
            OFC_SESSION_LOCKED = 6;

    public static final int REQUEST_SESSION_CHECK =1,
            REQUEST_SESSION_CHECK2=2;

    private long sessionTimeout;
    private int unitAttemptsCount, disallowStartHour,disallowEndHour;

    private MlUnitInstance currentUnitInstance;
    private static OC_User currentUser;
    private int currentSessionId;
    private long currentSessionStartTime, currentSessionEndTime;
    private boolean  allowsTimeOuts, showUserName;

    public static String firebase_user;


    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    public int databaseResource()
    {
        return R.raw.tables;
    }

    @Override
    public int buttonFlags()
    {
        int result = OBMainViewController.SHOW_TOP_RIGHT_BUTTON | OBMainViewController.SHOW_BOTTOM_LEFT_BUTTON | OBMainViewController.SHOW_BOTTOM_RIGHT_BUTTON;
        if (OBConfigManager.sharedManager.shouldShowBackArrow())
            result = result | OBMainViewController.SHOW_TOP_LEFT_BUTTON;
        return result;
    }

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
            String mlname = OBConfigManager.sharedManager.getMasterlist();
            //
            MainActivity.log("OC_FatController:loadMasterListIntoDB:masterlist name [" + mlname + "]");
            //
            if (mlname == null || mlname.length() == 0)
            {
                MainActivity.log("OC_FatController:loadMasterListIntoDB:no masterlist in the settings file. skipping");
                return;
            }
            OBXMLManager xmlManager = new OBXMLManager();
//            InputStream is = OBUtils.getInputStreamForPath(String.format("config/%s", mlname));
            //
            String masterlistXMLPath = String.format("masterlists/%s/units.xml", mlname);
            //
            MainActivity.log("OC_FatController:loadMasterListIntoDB:masterlist XML path [" + masterlistXMLPath + "]");
            //
            InputStream is = OBUtils.getInputStreamForPath(masterlistXMLPath);
            //
            if (is == null)
            {
                MainActivity.log("OC_FatController:loadMasterListIntoDB:unable to find XML for masterlist");
            }
            List<OBXMLNode> xml = xmlManager.parseFile(is);
            //
            OBXMLNode rootNode = xml.get(0);
            List<OBXMLNode> masterList = new ArrayList<>();
            String masterListToken = rootNode.attributeStringValue("token");
            if(token == null || !token.equals(masterListToken))
            {
                db.beginTransaction();
                try
                {
                    int startAudio = 0;
                    int nextAudio = OB_Maths.randomInt(3,5);
                    db.doDeleteOnTable(DBSQL.TABLE_UNITS, null);
                    int unitIndex = 0;
                    int nextStar = 0;
                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        int level = levelNode.attributeIntValue("id");

                        List<OBXMLNode> nodes = levelNode.childrenOfType("unit");
                        masterList.addAll(nodes);

                        for (int i=0; i<nodes.size(); i++)
                        {
                            OBXMLNode node = nodes.get(i);

                            if(i+2< nodes.size())
                            {
                                OBXMLNode nextNode = nodes.get(i+2);
                                if(nextNode.attributeIntValue("awardStar") > 0)
                                    nextStar = unitIndex+2;

                            }

                            int currentStartAudio = -1;
                            if((unitIndex >= nextStar-2) && (unitIndex <= nextStar+2))
                                currentStartAudio = -2;

                            if(unitIndex == nextAudio)
                            {
                                if(currentStartAudio == -2)
                                {
                                    nextAudio++;
                                }
                                else
                                {
                                    currentStartAudio = startAudio;
                                    nextAudio += OB_Maths.randomInt(3,5);
                                    startAudio = (startAudio+1)%8;
                                }
                            }

                            MlUnit.insertUnitFromXMLNodeintoDB(db, node, 1, unitIndex,level,currentStartAudio);
                            unitIndex++;
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

    public void loadUserDB(DBSQL db)
    {
        OC_User u = lastUserActiveFromDB(db);
        if (u == null)
        {
            u = OC_User.initAndSaveUserInDB(db, "Student");
        }
        setCurrentUserDB(db,u);
        checkAndPrepareNewSession();

    }

    public void loadUserDBSQL(DBSQL db)
    {
        OC_User u = lastUserActiveFromDB(db);
        if (u == null)
        {
            u = OC_User.initAndSaveUserInDB(db, "Student");
        }
        setCurrentUserDB(db,u);
        checkAndPrepareNewSession();

    }

    public static OC_User lastUserActiveFromDB(DBSQL db)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT U.userid AS userid FROM %s AS U LEFT JOIN %s AS S ON S.userid = U.userid ORDER BY S.startTime DESC LIMIT 1",
                DBSQL.TABLE_USERS,DBSQL.TABLE_SESSIONS), null);
        int userId = -1;
        int columnIndex = cursor.getColumnIndex("userid");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
        {
            userId = cursor.getInt(columnIndex);
        }
        cursor.close();
        OC_User user = null;

        if(userId > -1)
            user = OC_User.UserFromDBForID(db, userId);

        return user;

    }

    public void setCurrentUserDB(DBSQL db, OC_User user)
    {
        loadLastSessionFromDB(db, user.userid);
        currentUser = user;
        firebase_user = currentUser.toString();
        if(currentSessionId == -1)
            prepareNewSessionInDB(db, user.userid);

    }

    public static String getFirebase_user(){
        if(firebase_user == null || firebase_user.isEmpty()){
            return "Default_user";
        }

        return firebase_user;
    }

    public void loadUser()
    {
        DBSQL db = null;
        try
        {

            db = new DBSQL(true);
            loadUserDB(db);
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

        checkAndPrepareNewSession();
    }

    public void loadLastUnitIndexFromDB(DBSQL db)
    {
        int lastUnitIndex = currentUser.lastUnitIndexFromDB(db, currentUser.masterlistid);
        if (!unitCompletedByUser(db,currentUser.userid, currentUser.masterlistid, lastUnitIndex))
            lastUnitIndex--;

        firstUnstartedIndex = lastUnitIndex + 1;
    }


    public void deleteDBFile()
    {
        DBSQL.deleteDB();
    }

    @Override
    public void startUp()
    {
//        MainActivity.mainActivity.addToPreferences(MainActivity.PREFERENCES_RUNNING_EXAMPLE_UNIT, "false");
        //
        // initial setup
        try
        {
            sessionTimeout = OBConfigManager.sharedManager.getFatControllerSessionTimeoutInSeconds();
            unitAttemptsCount = OBConfigManager.sharedManager.getFatControllerMaxUnitAttempts();
            String disallowHours = OBConfigManager.sharedManager.getFatControllerNightModeHours();
            String[] disallowArray = disallowHours.split(",");
            disallowStartHour = Integer.valueOf(disallowArray[0]);
            disallowEndHour = Integer.valueOf(disallowArray[1]);
            allowsTimeOuts = OBConfigManager.sharedManager.isFatControllerSessionTimeoutEnabled();
            showUserName = OBConfigManager.sharedManager.shouldFatControllerShowUserName();
        }
        catch (Exception e)
        {
            sessionTimeout = 0;
            unitAttemptsCount = 3;
            disallowStartHour = 0;
            disallowEndHour = 0;
            allowsTimeOuts = true;
            showUserName = false;
        }
        initDB();
        //
        timeoutHandler = new Handler();
        //
        prepareAlarm();
        //
        // Setup screen
        Boolean usesSetupMenu = OBConfigManager.sharedManager.isSetupMenuEnabled();
        Boolean isSetupComplete = OBPreferenceManager.getBooleanPreference(OBPreferenceManager.PREFERENCES_SETUP_COMPLETE);
        //
        if (usesSetupMenu && !isSetupComplete)
        {
            // before overriding the app_code save it in the preferences to restore after setup is complete
            OBPreferenceManager.setPreference("originalAppCode", OBConfigManager.sharedManager.getMainFolder());
            OBConfigManager.sharedManager.updateConfigPaths(OBConfigManager.sharedManager.getSetupMenuFolder(), true);
            //
            String setupClassName = OBConfigManager.sharedManager.getSetupMenuClassName();
            if (setupClassName != null)
            {
                MainActivity.mainViewController.pushViewControllerWithName(setupClassName, false, true, "menu");
            }
        }
        else
        {
            // setup is now complete: continue as usual
            continueFromLastUnit();
            //
            if (OBConfigManager.sharedManager.shouldShowTestMenu())
            {
                MainViewController().pushViewControllerWithName("OC_TestMenu", false, false, "menu");
            }
            else
            {
                // restore app_code is it's coming from setup
                String originalAppCode = OBPreferenceManager.getStringPreference("originalAppCode");
//                String originalAppCode = MainActivity.mainActivity.getPreferences("originalAppCode");
                if (originalAppCode != null)
                {
                    OBConfigManager.sharedManager.updateConfigPaths(originalAppCode, true);
                }
                //
                if (OBConfigManager.sharedManager.hasSplashScreen())
                {
                    String menuClassName = "OC_SplashScreen";
                    String appCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
                    //
                    if (menuClassName != null && appCode != null)
                    {
                        OBBrightnessManager.sharedManager.onContinue();
                        if (!MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, false, null))
                        {
                            MainActivity.log("OC_FatController:startUp:unable to load view controller [%s] [%s]", menuClassName, appCode);
                        }
                    }
                }
                else
                {
                    String menuClassName = OBConfigManager.sharedManager.getMenuClassName();
                    String appCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
                    //
                    MainActivity.log("OC_FatController:startUp: pushing view controller [%s] [%s]", menuClassName, appCode);
                    //
                    if (menuClassName != null && appCode != null)
                    {
                        OBBrightnessManager.sharedManager.onContinue();
                        if (!MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, false, null))
                        {
                            MainActivity.log("OC_FatController:startUp:unable to load view controller [%s] [%s]", menuClassName, appCode);
                        }
                    }
                }
            }
        }
    }

    public boolean showUserName()
    {
        return showUserName;
    }

    public static String currentUserName()
    {
        if(currentUser != null)
        {
            return currentUser.name;
        }
        else
        {
            return null;
        }
    }


    public void prepareAlarm()
    {
        if(disallowEndHour == disallowStartHour)
            return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getCurrentTime() * 1000);
        //calendar.add(Calendar.DATE,1);
        calendar.set(Calendar.HOUR_OF_DAY, disallowStartHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        OBAlarmManager.scheduleRepeatingAlarm(calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, REQUEST_SESSION_CHECK);

        calendar.set(Calendar.HOUR_OF_DAY, disallowEndHour);
        OBAlarmManager.scheduleRepeatingAlarm(calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, REQUEST_SESSION_CHECK2);
    }

    public boolean checkAndPrepareNewSession()
    {
        if(currentSessionLocked())
            return false;

        if(currentSessionStartTime == 0)
            return true;

        if(disallowEndHour == disallowStartHour)
            return false;

        Calendar currentCalendar = Calendar.getInstance();
        Calendar calendarLastSession = Calendar.getInstance();

        currentCalendar.setTimeInMillis(getCurrentTime()*1000);
        calendarLastSession.setTimeInMillis(currentSessionStartTime*1000);

        if(currentCalendar.get(Calendar.DAY_OF_YEAR) != calendarLastSession.get(Calendar.DAY_OF_YEAR)
                || currentCalendar.get(Calendar.YEAR) != calendarLastSession.get(Calendar.YEAR))
        {
            prepareNewSession();
            return true;
        }
        return false;
    }

    public boolean currentSessionLocked()
    {
        if(disallowEndHour == disallowStartHour)
            return false;

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(getCurrentTime()*1000);
        int hourNow = currentCalendar.get(Calendar.HOUR_OF_DAY);

        boolean hourIsBetween = (disallowEndHour > disallowStartHour && hourNow >= disallowStartHour && hourNow < disallowEndHour)
                || (disallowEndHour < disallowStartHour && (hourNow >= disallowStartHour || hourNow < disallowEndHour));
        return hourIsBetween;
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
        if(currentUnitInstance != null)
        {
            updateScores();
            if (currentSessionLocked())
                signalSessionLocked();
            else if (finalScore >= currentUnit().passThreshold)
                signalSectionSucceeded();
            else
                signalSectionFailed();

            currentUnitInstance = null;
        }
        try
        {
            cont.displayAward();
        }catch (Exception e)
        {

        }
        cont.exitEvent();
    }

    public void completeEvent2(OBSectionController cont)
    {
        cancelTimeout();
        updateScores2();

        if(currentSessionLocked())
            signalSessionLocked();
        else if (finalScore >= currentUnit().passThreshold)
            signalSectionSucceeded();
        else
            signalSectionFailed();

        currentUnitInstance = null;

        cont.exitEvent();
    }

    public void completeEvent3()
    {
        OBSectionController cont = currentUnitInstance.sectionController;
        cancelTimeout();
        updateScores2();

        if(currentSessionLocked())
            signalSessionLocked();
        else if (finalScore >= currentUnit().passThreshold)
            signalSectionSucceeded();
        else
            signalSectionFailed();

        currentUnitInstance = null;

        if (cont != null)
        {
            cont.exitEvent();
        }
    }

    public void triggerTimeout()
    {
        cancelTimeout();
        timeOutUnit(currentUnitInstance);
    }

    public void timeOutUnit(MlUnitInstance unitInstance)
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            if(currentSessionLocked())
            {
                signalSessionLocked();
            }
            else if(unitCompletedByCurrentUser(db, unitInstance.mlUnit))
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

        if(unitInstance.sectionController != null && !unitInstance.sectionController._aborting)
            unitInstance.sectionController.exitEvent();
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

            currentUnitInstance.endTime = getCurrentTime();
            currentUnitInstance.score = finalScore;
            currentUnitInstance.elapsedTime = (int)(currentUnitInstance.endTime - currentUnitInstance.startTime);
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

    public void updateScores2()
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            int tot = scoreCorrect + scoreWrong;
            finalScore = 1;
            if (tot > 0)
                finalScore = scoreCorrect * 1.0f / tot;

            currentUnitInstance.endTime = getCurrentTime();
            currentUnitInstance.score = finalScore;
            currentUnitInstance.elapsedTime = 30;
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

    public void singalNewSession()
    {
        menu.receiveCommand(commandWith(OFC_NEW_SESSION,currentUnit()));
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

    public void signalSessionLocked()
    {
        menu.receiveCommand(commandWith(OFC_SESSION_LOCKED,currentUnit()));
    }


    public List<MlUnit> requestNextUnits(int count)
    {
        List<MlUnit> arr = new ArrayList<>();
        for (int i = 0;i < count;i++)
        {
            arr.add(MlUnit.mlUnitforMasterlistID(currentUser.masterlistid,currentUnit().unitIndex + 1 + i));
        }
        return arr;
    }

    public MlUnit requestNextUnit()
    {
        return MlUnit.mlUnitforMasterlistID(currentUser.masterlistid,firstUnstartedIndex);
    }

    private MlUnit currentUnit()
    {
        if(currentUnitInstance == null)
            return null;
        else
            return currentUnitInstance.mlUnit;
    }

    public int getLastUnitIndex()
    {
        return firstUnstartedIndex -1;
    }

    public void setCurrentUnitIndex(int unitIndex)
    {
        firstUnstartedIndex = unitIndex;
    }

    public Map<String,Object> getLastCommand()
    {
        MlUnit unit = null;
        int code = -1;
        DBSQL db = new DBSQL(false);
        try
        {
            unit = MlUnit.mlUnitforMasterlistIDFromDB(db,currentUser.masterlistid,getLastUnitIndex());

            if(currentSessionLocked())
                code = OFC_SESSION_LOCKED;
            else if(currentSessionFinished())
                code = OFC_SESSION_TIMED_OUT;
            else if(currentSessionReadyToStart() || unit == null)
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
            firstUnstartedIndex = unit.unitIndex+1;
        }
        currentUnitInstance = MlUnitInstance.initWithMlUnit(unit,currentUser.userid,currentSessionId,getCurrentTime());
        initScores();
    }



    public void startSectionByUnitNoUser(final MlUnit unit)
    {
        final String lastAppCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
        currentUnitInstance = null;
        //
        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    OBConfigManager.sharedManager.updateConfigPaths(unit.config, false, unit.lang);
                    if(MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {
                        if (currentUnitInstance != null)
                        {
                            currentUnitInstance.sectionController = MainViewController().topController();
                            startUnitInstanceTimeout(currentUnitInstance);
                        }
                    }
                    else
                    {
                        if (OBConfigManager.sharedManager.isDebugEnabled())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false);
                        }
                    }
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);

                    OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false);
                }
            }
        }.run();
    }



    public void startSectionByUnit(final MlUnit unit)
    {
        sectionStartedWithUnit(unit);
        final String lastAppCode = OBConfigManager.sharedManager.getCurrentActivityFolder();

        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    OBConfigManager.sharedManager.updateConfigPaths(unit.config, false, unit.lang);
                    if(MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {
                        currentUnitInstance.sectionController = MainViewController().topController();
                        startUnitInstanceTimeout(currentUnitInstance);
                    }
                    else
                    {
                        if (OBConfigManager.sharedManager.isDebugEnabled())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false);
                        }
                    }
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);

                    OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false);
                }
            }
        }.run();

    }

    public void userActivityOccurred(long systime)
    {
        sessionSegmentLastActive = systime;
    }

    public int lastPlayedUnitIndexFromDB(DBSQL db)
    {
        return MlUnitInstance.lastPlayedUnitIndexForUserIDInDB(db, currentUser.userid, currentUser.masterlistid);
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
        if(sessionTimeout <= 0)
            return;

        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("sessionid",String.valueOf(currentSessionId));
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNIT_INSTANCES, Collections.singletonList("SUM(elapsedTime) as elapsedTime"), whereMap);
        long elapsedTime = 0;

        int columnIndex = cursor.getColumnIndex("elapsedTime");
        if(cursor.moveToFirst()  && !cursor.isNull(columnIndex))
            elapsedTime = cursor.getLong(columnIndex);

        cursor.close();

        if(elapsedTime >= sessionTimeout)
            finishCurrentSessionInDB(db);
    }

    public int unitAttemptsCountInDB(DBSQL db, int userid, int masterlistid, int unitIndex)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT MAX(seqNo) as count FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid WHERE UI.userid = ? AND U.masterlistid = ? AND U.unitIndex = ?",
                DBSQL.TABLE_UNITS,  DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(userid),String.valueOf(masterlistid),String.valueOf(unitIndex)));
        int count = 0;

        int columnIndex = cursor.getColumnIndex("count");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
            count = cursor.getInt(columnIndex);

        cursor.close();

        return count;
    }

    public  boolean unitCompletedByCurrentUser(DBSQL db, MlUnit unit)
    {
        return unitCompletedByUser(db,currentUser.userid, unit.masterlistid, unit.unitIndex);
    }

    public boolean unitCompletedByUser(DBSQL db, int userid, int masterlistid, int unitIndex)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT U.unitid FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid WHERE UI.userid = ? AND U.masterlistid = ? AND U.unitIndex = ? AND UI.endTime > 0",
                DBSQL.TABLE_UNITS,  DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(userid),String.valueOf(masterlistid),String.valueOf(unitIndex)));

        boolean rowExists = cursor.moveToFirst();
        cursor.close();
        if(rowExists)
            return true;


        if(unitAttemptsCount>0 && (unitAttemptsCount-1) <= unitAttemptsCountInDB(db, userid, masterlistid, unitIndex))
            return true;

        return false;
    }

    public boolean currentSessionFinished()
    {
        if(currentSessionId < 0)
            return true;

        return currentSessionEndTime > 0;
    }


    private void loadLastSessionFromDB(DBSQL db, int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        currentSessionId = -1;
        currentSessionEndTime = currentSessionStartTime = 0;
        try
        {
            Cursor cursor = db.prepareRawQuery(String.format("SELECT sessionid, startTime, endTime FROM %s WHERE userid = ? ORDER BY sessionid DESC LIMIT 1",DBSQL.TABLE_SESSIONS)
                    ,Collections.singletonList(String.valueOf(userid)));
            if (cursor.moveToFirst())
            {
                currentSessionStartTime = cursor.getLong(cursor.getColumnIndex("startTime"));
                currentSessionEndTime = cursor.getLong(cursor.getColumnIndex("endTime"));
                currentSessionId = cursor.getInt(cursor.getColumnIndex("sessionid"));
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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

    public void prepareNewSession()
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            prepareNewSessionInDB(db, currentUser.userid);
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


    public void prepareNewSessionInDB(DBSQL db, int userid)
    {
        if(!currentSessionFinished())
            finishCurrentSessionInDB(db);

        fixMissingStarsInDB(db);

        currentSessionStartTime = 0;
        currentSessionEndTime = 0;

        int sessionid = currentSessionId;
        if(sessionid<0)
            sessionid = 1;
        else
            sessionid++;

        ContentValues contentValues = new ContentValues();
        contentValues.put("userid", userid);
        contentValues.put("sessionid", sessionid);
        contentValues.put("startTime",currentSessionStartTime);
        db.doInsertOnTable(DBSQL.TABLE_SESSIONS,contentValues);
        currentSessionId = sessionid;

        loadLastUnitIndexFromDB(db);
    }

    public void fixMissingStarsInDB(DBSQL db)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT TAB.unitid as unitid, TAB.awardStar as awardStar, TAB.masterlistid as masterlistid FROM (SELECT UI.unitid as unitid, userid, awardStar, masterlistid  " +
                "FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid " +
                "WHERE (endTime>0 OR (SELECT COUNT(*) FROM %s WHERE unitid = UI.unitid AND userid = UI.userid) >= ?) AND userid = ? AND awardStar >0) TAB " +
                "LEFT JOIN %s AS S ON  TAB.userid = S.userid AND TAB.unitid = S.unitid WHERE S.userid IS NULL",DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES,DBSQL.TABLE_UNIT_INSTANCES, DBSQL.TABLE_STARS)
                ,Arrays.asList(String.valueOf(unitAttemptsCount), String.valueOf(currentUser.userid)));

        Map<Integer,List<Long>> result = new ArrayMap<>();
        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                int masterlistid = cursor.getInt(cursor.getColumnIndex("masterlistid"));
                long unitid = cursor.getLong(cursor.getColumnIndex("unitid"));
                if(result.get(masterlistid) == null)
                    result.put(masterlistid, new ArrayList<Long>());

                result.get(masterlistid).add(unitid);
                cursor.moveToNext();
            }

        }
        cursor.close();

        for(int masterlistid : result.keySet())
        {
            String colour = lastStarColourForLevelFromDB(db, currentUser.userid, masterlistid);
            if (colour == null)
                colour = String.valueOf(OB_Maths.randomInt(1, 5));
            for(long unitid : result.get(masterlistid))
                saveStarInDB(db,currentUser.userid,unitid,colour);
        }
    }

    public boolean currentSessionReadyToStart()
    {
        if(currentSessionLocked())
            return false;

        return currentSessionStartTime == 0;
    }

    public void startCurrentSession()
    {
        if(!currentSessionReadyToStart())
            return;

        currentSessionStartTime = getCurrentTime();
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            updateCurrentSessionTimeInDB(db,"startTime", currentSessionStartTime);
        }
        catch (Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }

    }

    public void finishCurrentSessionInDB(DBSQL db)
    {
        if(sessionTimeout <= 0)
            return;

        if(currentSessionId < 0)
            return;

        currentSessionEndTime =  getCurrentTime();

        updateCurrentSessionTimeInDB(db,"endTime", currentSessionEndTime);
    }

    private boolean updateCurrentSessionTimeInDB(DBSQL db, String fieldName, long value)
    {
        Map<String,String> whereMap  = new ArrayMap<>();

        whereMap.put("userid",String.valueOf(currentUser.userid));
        whereMap.put("sessionid",String.valueOf(currentSessionId));
        ContentValues contentValues = new ContentValues();
        contentValues.put(fieldName,value);
        return  db.doUpdateOnTable(DBSQL.TABLE_SESSIONS,whereMap, contentValues) > 0;
    }

    public void saveStarForUnit(MlUnit unit,String colour)
    {
        if(unit == null)
            return;

        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            saveStarInDB(db,currentUser.userid,unit.unitid,colour);
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

    public void saveStarInDB(DBSQL db, int userid, long unitid, String colour)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put("userid",userid);
        contentValues.put("unitid",unitid);
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
            result = starsForLevelFromDB(db,currentUser.userid, currentUser.masterlistid, level);
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

    private Map<Integer,String> starsForLevelFromDB(DBSQL db, int userid, int masterlistid,  int level)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT awardStar,colour FROM %s AS S JOIN %s AS U ON U.unitid = S.unitid WHERE S.userid = ? AND U.masterlistid = ? AND U.level = ?",
                DBSQL.TABLE_STARS, DBSQL.TABLE_UNITS),
                Arrays.asList(String.valueOf(userid),String.valueOf(masterlistid),String.valueOf(level)));

        Map<Integer,String> result = new ArrayMap<>();
        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                result.put(cursor.getInt(cursor.getColumnIndex("awardStar")), cursor.getString(cursor.getColumnIndex("colour")));
                cursor.moveToNext();
            }

        }
        cursor.close();

        return result;
    }

    public String lastStarColourForLevelFromDB(DBSQL db, int userid, int masterlistid)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT colour FROM %s AS S JOIN %s AS U ON U.unitid = S.unitid WHERE S.userid = ? AND U.masterlistid = ? ORDER BY unitIndex DESC LIMIT 1",
                DBSQL.TABLE_STARS, DBSQL.TABLE_UNITS),
                Arrays.asList(String.valueOf(userid),String.valueOf(masterlistid)));
        String result = null;

        int columnIndex = cursor.getColumnIndex("colour");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
            result = cursor.getString(columnIndex);

        cursor.close();
        return result;
    }

    public String starForUser(int userid,int unitid)
    {
        String result = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Map<String,String> whereMap = new ArrayMap<>();
            whereMap.put("userid",String.valueOf(userid));
            whereMap.put("unitid",String.valueOf(unitid));
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

        return starForUser(currentUser.userid,unit.unitid) == null;
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


    public void startUnitInstanceTimeout(final MlUnitInstance unitInstance)
    {
        cancelTimeout();

        timeoutRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(checkTimeout(unitInstance))
                    timeoutHandler.postDelayed(this,60*1000);
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable,(int)(unitInstance.mlUnit.targetDuration)*1000); //currentInstance.mlUnit.targetDurationstance.)

    }

    public void cancelTimeout()
    {
        if(timeoutRunnable != null)
            timeoutHandler.removeCallbacks(timeoutRunnable);

        timeoutRunnable = null;
    }

    public boolean checkTimeout(MlUnitInstance unitInstance)
    {
        if (!allowsTimeOuts)
            return false;

        if(unitInstance == null)
            return false;

        if(unitInstance != currentUnitInstance)
            return false;

        if(unitInstance.sectionController == null || unitInstance.sectionController._aborting)
            return false;

        if((unitInstance.startTime + unitInstance.mlUnit.targetDuration) <= getCurrentTime())
        {
            MainActivity.log("Time out!!");
            timeOutUnit(unitInstance);
            return false;
        }
        return true;
    }

    public boolean currentPathComplete()
    {
        int maxUnitIndex = 0;
        boolean unitCompleted = false;
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Map<String,String> whereMap = new ArrayMap<>();
            whereMap.put("masterlistid", String.valueOf(currentUser.masterlistid));
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS, Collections.singletonList("MAX(unitIndex) as unitIndex"),whereMap);
            if(cursor.moveToFirst())
                maxUnitIndex = cursor.getInt(cursor.getColumnIndex("unitIndex"));
            cursor.close();

            unitCompleted = unitCompletedByUser(db,currentUser.userid, currentUser.masterlistid, maxUnitIndex);

        }
        catch(Exception e)
        {

        }
        finally
        {
            if(db != null)
                db.close();
        }
        return unitCompleted;
    }

    public void resetProgress()
    {
        int maxUnitID = 0;
        boolean unitCompleted = false;
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            db.doDeleteOnTable(DBSQL.TABLE_STARS,null);
            db.doDeleteOnTable(DBSQL.TABLE_UNIT_INSTANCES,null);
            db.doDeleteOnTable(DBSQL.TABLE_SESSIONS,null);

            firstUnstartedIndex = 0;
            loadUserDB(db);
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

    public int getCurrentMoonPhase()
    {
        Calendar calendar = Calendar.getInstance();
        int lp = 2551443; // full moon phase(from full to full) in seconds
        calendar.setTimeInMillis(getCurrentTime()*1000);
        calendar.set(Calendar.HOUR,20);
        calendar.set(Calendar.MINUTE,35);
        calendar.set(Calendar.SECOND,0);
        long now = calendar.getTimeInMillis();

        calendar.set(Calendar.YEAR,1970);
        calendar.set(Calendar.MONTH,Calendar.JANUARY);
        calendar.set(Calendar.DATE,7);
        long new_moon = calendar.getTimeInMillis();
        long phase = ((now - new_moon)/1000) % lp;
        return (int)(Math.floor(phase /(24*3600)) + 1);
    }

    public List<OC_PlayZoneAsset> getPlayZoneAssetForCurrentUser()
    {
        return OC_PlayZoneAsset.assetsFromDBForUserId(currentUser.userid);
    }

    public boolean savePlayZoneAssetForCurrentUserType(int typeid,String thumbnail,Map<String,String> params)
    {
        boolean result = false;
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            result = OC_PlayZoneAsset.saveAssetInDBForUserId(db,currentUser.userid,typeid,thumbnail,params) > 0;
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

}
