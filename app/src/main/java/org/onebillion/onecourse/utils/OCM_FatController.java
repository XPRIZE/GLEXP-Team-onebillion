package org.onebillion.onecourse.utils;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.ArrayMap;
import android.widget.Toast;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset;
import org.onebillion.onecourse.receivers.OBBatteryReceiver;

import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.onebillion.onecourse.mainui.OBViewController.MainViewController;

/**
 * Created by michal on 02/08/2017.
 */

public class OCM_FatController extends OBFatController
{
    public int scoreCorrect,scoreWrong;
    public float finalScore;
    public Map<String,Integer> colourDict;
    public long sessionSegmentLastActive;
    public OCM_FatReceiver menu;

    private int unitAttemptsCount, disallowStartHour, disallowEndHour, playzoneActiveHour;
    private OCM_MlUnitInstance currentUnitInstance;
    private OCM_User currentUser;
    private int currentSessionId;
    private int currentMode;
    private long currentSessionStartTime, currentSessionEndTime;
    private boolean allowsTimeOuts, showUserName;
    private Date startDate;

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    public static final int SESSION_UNIT_COUNT = 15,
            COLOUR_COUNT = 20,
            MAX_WEEK_COUNT = 68,
            MAX_PZ_ASSETS = 30;

    public static final int OFC_UNIT_SUCCEEDED = 1,
            OFC_UNIT_FAILED = 2,
            OFC_UNIT_TIMED_OUT = 3,
            OFC_SESSION_NEW = 4,
            OFC_SESSION_LOCKED = 5,
            OFC_BATTERY_LOW = 6;

    private static final int ALARM_DAY_LOCK = 1,
            ALARM_DAY_UNLOCK = 2,
            ALARM_PLAYZONE_UNLOCK = 3;


    public void userActivityOccurred(long systime)
    {
        sessionSegmentLastActive = systime;
    }

    @Override
    public int databaseResource()
    {
        return R.raw.ocm_tables;
    }

    @Override
    public int buttonFlags()
    {
        int result = OBMainViewController.SHOW_TOP_RIGHT_BUTTON | OBMainViewController.SHOW_BOTTOM_LEFT_BUTTON | OBMainViewController.SHOW_BOTTOM_RIGHT_BUTTON;
        boolean runningExampleUnit = MainActivity.mainActivity.getPreferences(MainActivity.PREFERENCES_RUNNING_EXAMPLE_UNIT).equals("true");
        if (showBackButton() || runningExampleUnit) result = result | OBMainViewController.SHOW_TOP_LEFT_BUTTON;
        return result;
    }

    private boolean showBackButton()
    {
       // String value = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_SHOW_BACK_BUTTON);
       // return (value != null && value.equalsIgnoreCase("true"));
        return currentUnitInstance == null;
    }

    public void loadMasterListIntoDB()
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            String token = OBPreferenceManager.getStringPreference(OBPreferenceManager.PREFERENCE_ML_TOKEN, db);
            String mlname = (String) MainActivity.mainActivity.config.get(MainActivity.CONFIG_MASTER_LIST);
            if (mlname.length() == 0)
            {
                MainActivity.log("OCM_FatController:loadMasterListIntoDB:no masterlist in the settings file. skipping");
                return;
            }
            OBXMLManager xmlManager = new OBXMLManager();
            InputStream is = OBUtils.getInputStreamForPath(String.format("masterlists/%s/units.xml", mlname));
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
                    int nextAudio = OB_Maths.randomInt(3,5);
                    db.doDeleteOnTable(DBSQL.TABLE_UNITS, null);
                    int unitIndex = 0;
                    int nextStar = 0;
                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        int week = levelNode.attributeIntValue("id");

                        List<OBXMLNode> nodes = levelNode.childrenOfType("unit");
                        masterList.addAll(nodes);

                        for (int i=0; i<nodes.size(); i++)
                        {
                            OBXMLNode node = nodes.get(i);
                            OCM_MlUnit.insertUnitFromXMLNodeintoDB(db, node, 1, unitIndex,week);
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

    /* Functions that manage user
     */
    public void loadUser()
    {
        OCM_User u = null;
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            u = lastUserActiveFromDB(db);
            if (u == null)
            {
                u = OCM_User.initAndSaveUserInDB(db,"Student");
            }
            setCurrentUserDB(db,u);
        }
        catch (Exception e)
        {}
        finally
        {
            if(db != null)
                db.close();
        }

        checkAndPrepareNewSession();
        fixMissingStars(currentSessionId);
    }

    public static OCM_User lastUserActiveFromDB(DBSQL db)
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
        OCM_User user = null;

        if(userId > -1)
            user = OCM_User.UserFromDBForID(db, userId);

        return user;
    }

    public void setCurrentUserDB(DBSQL db, OCM_User user)
    {
        loadLastSessionFromDB(db, user.userid);
        currentUser = user;
        if(currentSessionId == -1)
            prepareNewSessionInDB(db, user.userid);

    }

    /* Date/Time functions
    */
    public long getCurrentTime()
    {
        return  System.currentTimeMillis()/1000;
    }

    public int getCurrentDay()
    {
        Date date = new Date(System.currentTimeMillis());
        long dayLengthMillisec = 1000 * 60 * 60 * 24;
        int dayDif = (int)Math.floor((date.getTime() - startDate.getTime()) / dayLengthMillisec);

        return dayDif + 1;
    }

    public int getCurrentWeek()
    {
        return (int)Math.ceil(getCurrentDay() / 7.0f);
    }

    public int getCurrentDayOfWeek()
    {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        day--;
        if(day<1)
            day = 7;
        return day;
    }

    private void prepareAlarm()
    {
        if(disallowEndHour == disallowStartHour)
            return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getCurrentTime() * 1000);
        //calendar.add(Calendar.DATE,1);

        //day lock
        calendar.set(Calendar.HOUR_OF_DAY, disallowStartHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        OBAlarmManager.scheduleRepeatingAlarm(calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, ALARM_DAY_LOCK);

        //day unlock
        calendar.set(Calendar.HOUR_OF_DAY, disallowEndHour);
        OBAlarmManager.scheduleRepeatingAlarm(calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, ALARM_DAY_UNLOCK);


        //play zone active
        calendar.set(Calendar.HOUR_OF_DAY, playzoneActiveHour);
        OBAlarmManager.scheduleRepeatingAlarm(calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, ALARM_PLAYZONE_UNLOCK);
    }

    public void checkCurrentStatus()
    {
        if(batteryStatusLow() ||
                currentSessionLocked() ||
                checkAndPrepareNewSession())
        {
            if(currentSessionLocked())
                signalSessionLocked();
            else if(batteryStatusLow())
                signalBatteryLow();
            else if(currentSessionReadyToStart())
                signalNewSession();

            quitToTopMenu();
        }
    }

    /*
    UnitInstance timeout
     */

    public void startUnitInstanceTimeout(final OCM_MlUnitInstance unitInstance)
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

    public boolean checkTimeout(OCM_MlUnitInstance unitInstance)
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

    public void timeOutUnit(OCM_MlUnitInstance unitInstance)
    {
        if(currentSessionLocked())
        {
            signalSessionLocked();
        }
        else if(unitInstance.type == OCM_MlUnitInstance.INSTANCE_TYPE_STUDY &&
                unitInstance.seqNo >= unitAttemptsCount - 1)
        {
            DBSQL db = null;
            try
            {
                db = new DBSQL(true);
                unitInstance.starColour = nextStarColourFromDB(db,OCM_MlUnitInstance.INSTANCE_TYPE_STUDY);
                unitInstance.updateDataInDB(db);
            }catch(Exception e)
            {
                MainActivity.log("OCM_FatController: database access error: " + e.getMessage());
            }
            finally
            {
                if(db != null)
                    db.close();
            }
            signalUnitFailed();
        }
        else
        {
            signalUnitTimedOut();
        }

        currentUnitInstance = null;
        if(unitInstance.sectionController != null && !unitInstance.sectionController._aborting)
            unitInstance.sectionController.exitEvent();
    }


    /*
    callbacks
     */
    @Override
    public void onAlamReceived(Intent intent, OBSectionController cont)
    {
        checkCurrentStatus();
    }

    @Override
    public void onPause(OBSectionController cont)
    {

    }
    @Override
    public void onResume(OBSectionController cont)
    {
        checkCurrentStatus();
        checkTimeout(currentUnitInstance);
    }

    @Override
    public void onExitSection(OBSectionController cont)
    {
        currentUnitInstance = null;
        cancelTimeout();

    }


    public OCM_MlUnit getNextUnitFromDB(DBSQL db)
    {
        OCM_MlUnit mlUnit = null;
        int currentWeek = getCurrentWeek();
        Cursor cursor = db.prepareRawQuery(String.format("SELECT MAX(unitIndex) AS unitIndex FROM %s AS U "+
                "JOIN %s AS UI ON UI.unitid = U.unitid "+
                "WHERE UI.userid = ? AND U.week = ? AND U.masterlistid = ? "+
                "AND (UI.seqNo >= ? OR UI.endTime > 0)", DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES), Arrays.asList(String .valueOf(currentUser.userid),
                String.valueOf(currentWeek),String.valueOf(currentUser.masterlistid),String.valueOf(unitAttemptsCount-1)));

        if(cursor.moveToFirst())
        {
            int columnIndex = cursor.getColumnIndex("unitIndex");
            if(!cursor.isNull(columnIndex))
                mlUnit = OCM_MlUnit.nextMlUnitFromDB(db,currentUser.masterlistid, cursor.getInt(columnIndex));
        }
        cursor.close();

        if(mlUnit == null)
        {
            cursor = db.prepareRawQuery(String.format("SELECT MIN(unitIndex) AS unitIndex FROM %s AS U WHERE U.week = ? AND U.masterlistid = ?", DBSQL.TABLE_UNITS),
                    Arrays.asList(String.valueOf(currentWeek),String.valueOf(currentUser.masterlistid)));

            if(cursor.moveToFirst())
            {
                int columnIndex = cursor.getColumnIndex("unitIndex");
                if(!cursor.isNull(columnIndex))
                    mlUnit = OCM_MlUnit.mlUnitforMasterlistIDFromDB(db,currentUser.masterlistid, cursor.getInt(columnIndex));
            }
            cursor.close();
        }
        return mlUnit;
    }

    /* Database functions
    */

    public void initDB()
    {
        loadMasterListIntoDB();
        loadUser();
    }

    public void resetDatabase()
    {
        DBSQL.deleteDB();
        initDB();
    }

    @Override
    public void startUp()
    {
        // disabling the flag for running example unit from the setup menu
        MainActivity.mainActivity.addToPreferences(MainActivity.PREFERENCES_RUNNING_EXAMPLE_UNIT, "false");
        //
        // initial setup
        try
        {
            unitAttemptsCount = MainActivity.mainActivity.configIntForKey(MainActivity.CONFIG_UNIT_TIMEOUT_COUNT);
            String disallowHours = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_DISALLOW_HOURS);
            String[] disallowArray = disallowHours.split(",");
            disallowStartHour = Integer.valueOf(disallowArray[0]);
            disallowEndHour = Integer.valueOf(disallowArray[1]);
            showUserName = MainActivity.mainActivity.configBooleanForKey(MainActivity.CONFIG_SHOW_USER_NAME);
            allowsTimeOuts = MainActivity.mainActivity.configBooleanForKey(MainActivity.CONFIG_ALLOWS_TIMEOUT);
            playzoneActiveHour = MainActivity.mainActivity.configIntForKey(MainActivity.CONFIG_PLAYZONE_ACTIVE_HOUR);
        }
        catch (Exception e)
        {
            unitAttemptsCount = 3;
            disallowStartHour = 22;
            disallowEndHour = 5;
            showUserName = false;
            allowsTimeOuts = true;
            playzoneActiveHour = 12;
        }
        initDB();
        //
        startDate = Date.valueOf("2017-08-01");
        //
        timeoutHandler = new Handler();
        // Setup screen
        Boolean usesSetupMenu = MainActivity.mainActivity.configBooleanForKey(MainActivity.CONFIG_USES_SETUP_MENU);
        String isSetupComplete = MainActivity.mainActivity.getPreferences(MainActivity.PREFERENCES_SETUP_COMPLETE);
        //
        if (usesSetupMenu && (isSetupComplete == null || !isSetupComplete.equals("true")))
        {
            // before overriding the app_code save it in the preferences to restore after setup is complete
            MainActivity.mainActivity.addToPreferences("originalAppCode", MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_APP_CODE));
            MainActivity.mainActivity.updateConfigPaths(MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_SETUP_FOLDER), true, null);
            //
            String setupClassName = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_SETUP_CLASS);
            if (setupClassName != null)
            {
                MainActivity.mainViewController.pushViewControllerWithName(setupClassName, false, true, "menu");
            }
        }
        else
        {
            // setup is now complete: continue as usual
            prepareAlarm();
            //
            resetTempData();

            // restore app_code is it's coming from setup
            String originalAppCode = MainActivity.mainActivity.getPreferences("originalAppCode");
            if (originalAppCode != null)
            {
                MainActivity.mainActivity.updateConfigPaths(originalAppCode, true, null);
            }
            //
            String menuClassName = MainActivity.mainActivity.configStringForKey (MainActivity.CONFIG_MENU_CLASS);
            String appCode = MainActivity.mainActivity.configStringForKey (MainActivity.CONFIG_APP_CODE);
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

    private void quitToTopMenu()
    {
        List<OBSectionController> controllers = MainViewController().viewControllers;
        for(int i = (int)controllers.size() -1; i>=1; i--)
        {
            quitEvent(controllers.get(i));
        }
        if(currentUnitInstance != null)
        {
            currentUnitInstance.sectionController = null;
        }
        MainViewController().popViewControllerToBottom(true);
    }

    private void quitEvent(OBSectionController sectionController)
    {
        if (!sectionController._aborting)
        {
            sectionController.cleanUp();
        }
    }


    /*
    Session functions
     */

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


    /*
    Section functions
    */

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
        int starColour = -1;
        if(currentUnitInstance != null)
        {
            updateScores();
            if (currentSessionLocked())
                signalSessionLocked();
            else
                signalUnitSucceeded();

            starColour = currentUnitInstance.starColour;
            currentUnitInstance.sectionController = null;
            currentUnitInstance = null;
        }
        try
        {
            if(cont instanceof OC_SectionController && starColour > 0)
                ((OC_SectionController)cont).displayAward2(colourDict.get(String.format("star_%d",starColour)));
            else
                cont.displayAward();
        }catch (Exception e)
        {

        }
        cont.exitEvent();
    }

    public void triggerTimeoutUnit()
    {
        timeOutUnit(currentUnitInstance);
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
            currentUnitInstance.starColour = nextStarColourFromDB(db, currentUnitInstance.type);
            currentUnitInstance.updateDataInDB(db);
        }
        catch(Exception e)
        {
            MainActivity.log("OCM_FatController: database access error: " + e.getMessage());
        }
        finally
        {
            if(db != null)
                db.close();
        }
    }

    /*
    Menu signals
     */

    public Map<String,Object> commandWith(int code, OCM_MlUnitInstance unitInstance)
    {
        Map<String,Object> command = new ArrayMap<>();
        command.put("code", code);
        if(unitInstance != null)
            command.put("instance", unitInstance);
        return command;
    }

    public void signalUnitFailed()
    {
        if(menu != null)
            menu.receiveCommand(commandWith(OFC_UNIT_FAILED,currentUnitInstance));
    }

    public void signalUnitSucceeded()
    {
        if(menu != null)
            menu.receiveCommand(commandWith(OFC_UNIT_SUCCEEDED,currentUnitInstance));
    }

    public void signalUnitTimedOut()
    {
        if(menu != null)
            menu.receiveCommand(commandWith(OFC_UNIT_TIMED_OUT,null));
    }

    public void signalNewSession()
    {
        if(menu != null)
            menu.receiveCommand(commandWith(OFC_SESSION_NEW,null));
    }

    public void signalSessionLocked()
    {
        if(menu != null)
            menu.receiveCommand(commandWith(OFC_SESSION_LOCKED,null));
    }

    public void signalBatteryLow()
    {
        if(menu != null)
            menu.receiveCommand(commandWith(OFC_BATTERY_LOW,null));
    }

    /*
    Data for menu
     */

    public Map<String,Object> getNextUnitData()
    {
        Map<String,Object> dict = new ArrayMap<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            int count = sessionUnitCountFromDB(db,currentSessionId,currentUser.userid);
            if(count >= SESSION_UNIT_COUNT)
            {
                dict.put("community",true);
            }
            else
            {
                dict.put("community",false);
                count++;
                dict.put("unitOrder",count);
                OCM_MlUnit currentUnit = getNextUnitFromDB(db);
                if(currentUnit != null)
                    dict.put("unit",currentUnit);

            }

        }catch (Exception e)
        {
            MainActivity.log("OCM_FatController: database access error: " + e.getMessage());
        }
        finally
        {
            if(db != null)
                db.close();
        }

        return dict;
    }

    public List<OCM_MlUnit> getUnitsForGrid()
    {

        List<OCM_MlUnit> unitsList = new ArrayList<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Cursor cursor = db.prepareRawQuery(String.format("SELECT * FROM %s "+
                            "WHERE unitid IN (SELECT unitid FROM %s "+
                            "WHERE userid = ? AND sessionid = ?) "+
                            "GROUP BY unitid ORDER BY unitIndex ASC",DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES),
                    Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(currentSessionId)));

            if(cursor.moveToFirst())
            {
                while (cursor.isAfterLast() == false)
                {
                    unitsList.add(OCM_MlUnit.mlUnitFromCursor(cursor));
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }catch (Exception e)
        {}
        finally
        {
            if(db != null)
                db.close();
        }

        return unitsList;

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

    public Map<String,Object> getCurrentCommand()
    {
        int code = -1;
        if(batteryStatusLow())
            code = OFC_BATTERY_LOW;
        else if(currentSessionLocked())
            code = OFC_SESSION_LOCKED;
        else if(currentSessionReadyToStart())
            code =  OFC_SESSION_NEW;
        else
            code = OFC_UNIT_SUCCEEDED;

        Map<String,Object> command = new ArrayMap<>();
        command.put("code", code);
        return command;
    }

    public boolean playZoneActive()
    {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(getCurrentTime()*1000);
        int hourNow = currentCalendar.get(Calendar.HOUR_OF_DAY);

        return hourNow >= playzoneActiveHour;
    }

    public boolean communityModeActive()
    {
        return currentSessionUnitCount() >= SESSION_UNIT_COUNT;
    }

    /*
    Stars functions
     */

    public List<Integer> getStarsForSessionFromDB(DBSQL db,int sessionid)
    {
        List<Integer> coloursList = new ArrayList<>();
        Cursor cursor = db.prepareRawQuery(String.format("SELECT UI.starColour as starColour FROM %s AS UI" +
                        " JOIN %s AS U ON U.unitid = UI.unitid" +
                        " WHERE UI.userid = ? AND UI.sessionid = ?" +
                        " AND UI.starColour > 0 AND UI.type = ?" +
                        " GROUP BY UI.unitid ORDER BY U.unitIndex ASC", DBSQL.TABLE_UNIT_INSTANCES, DBSQL.TABLE_UNITS),
                Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(sessionid),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_STUDY)));

        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                int columnIndex = cursor.getColumnIndex("starColour");
                if(!cursor.isNull(columnIndex))
                    coloursList.add(cursor.getInt(columnIndex));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return coloursList;

    }

    public List<Integer> getGridStarsForSessionFromDB(DBSQL db, int sessionid)
    {
        List<Integer> coloursList = new ArrayList<>();
        Cursor cursor = db.prepareRawQuery(String.format("SELECT UI.starColour as starColour FROM %s AS UI "+
                        "JOIN (SELECT UI.unitid as unitid, MAX(UI.seqNo) as seqNo FROM %s AS UI "+
                        "WHERE UI.userid = ? AND UI.sessionid = ? AND UI.starColour > 0 AND UI.type = ? "+
                        "GROUP BY UI.unitid) AS TAB ON UI.unitid = TAB.unitid AND UI.seqNo = TAB.seqNo "+
                        "WHERE UI.userid = ? AND UI.sessionid = ? AND UI.type =?",DBSQL.TABLE_UNIT_INSTANCES,DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(sessionid),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_REVIEW),
                        String.valueOf(currentUser.userid),String.valueOf(sessionid),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_REVIEW)));
        if(cursor.moveToFirst())
        {
            while (cursor.isAfterLast() == false)
            {
                int columnIndex = cursor.getColumnIndex("starColour");
                if(!cursor.isNull(columnIndex))
                    coloursList.add(cursor.getInt(columnIndex));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return coloursList;
    }

    public List<Integer> getStarsForSession()
    {
        List<Integer> coloursList = new ArrayList<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            coloursList = getStarsForSessionFromDB(db,currentSessionId);
        }catch (Exception e)
        {}
        finally
        {
            if(db != null)
                db.close();
        }
        return coloursList;
    }

    public int nextStarColourFromDB(DBSQL db,int type)
    {
        List<Integer> starColours = null;
        if(type == OCM_MlUnitInstance.INSTANCE_TYPE_STUDY)
            starColours = getStarsForSessionFromDB(db,currentSessionId);
         else
             starColours = getGridStarsForSessionFromDB(db,currentSessionId);
        List<Integer> nums = OBUtils.randomlySortedArray(OBMisc.integerList(1,COLOUR_COUNT));
        nums.removeAll(starColours);
        int starColour = -1;
        if(nums.size() > 0)
            starColour = nums.get(0);
        return starColour;
    }


    public void fixMissingStars(int sessionId)
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            Cursor cursor = db.prepareRawQuery(String.format("SELECT UI.unitid as unitid, MAX(UI.seqNo) as seqNo FROM %s AS UI "+
                            "WHERE UI.userid = ? AND UI.sessionid= ? AND UI.type = ?"+
                            "AND (UI.seqNo >= ? OR UI.endTime > 0) AND UI.starColour < 0 "+
                            "GROUP BY UI.unitid", DBSQL.TABLE_UNIT_INSTANCES),
                    Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(sessionId),
                            String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_STUDY),String.valueOf(unitAttemptsCount-1)));

            List<Map<String,Integer>> fixUnits = new ArrayList<>();
            if(cursor.moveToFirst())
            {
                while (cursor.isAfterLast() == false)
                {
                    int columnIndex = cursor.getColumnIndex("unitid");
                    int columnIndex2 = cursor.getColumnIndex("seqNo");
                    if(!cursor.isNull(columnIndex) && !cursor.isNull(columnIndex2) )
                    {
                        Map<String,Integer> unitData = new ArrayMap<>();
                        unitData.put("unitid", cursor.getInt(columnIndex));
                        unitData.put("seqNo", cursor.getInt(columnIndex2));
                        fixUnits.add(unitData);
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();

            if(fixUnits.size() > 0)
            {
                List<Integer> starColours = getStarsForSessionFromDB(db, sessionId);
                List<Integer> availableColours = OBUtils.randomlySortedArray(OBMisc.integerList(1,COLOUR_COUNT));
                availableColours.removeAll(starColours);

                int index = 0;
                for(Map<String,Integer> unitData: fixUnits)
                {
                    Map<String,String> whereMap = new ArrayMap<>();
                    whereMap.put("userid",String.valueOf(currentUser.userid));
                    whereMap.put("sessionid",String.valueOf(sessionId));
                    whereMap.put("type",String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_STUDY));
                    whereMap.put("unitid",String.valueOf(unitData.get("unitid")));
                    whereMap.put("seqNo",String.valueOf(unitData.get("seqNo")));
                    ContentValues contentValues = new ContentValues();
                    int starColour = starColour = availableColours.size() > index ? availableColours.get(index) : OB_Maths.randomInt(1, COLOUR_COUNT);
                    contentValues.put("starColour",starColour);
                    db.doUpdateOnTable(DBSQL.TABLE_UNIT_INSTANCES,whereMap,contentValues);
                    index++;
                }
            }


        }catch (Exception e)
        {}
        finally
        {
            if(db != null)
                db.close();
        }
    }


    /*
    Section start commands
     */

    public void sectionStartedWithUnit(OCM_MlUnit unit,int type)
    {
        currentUnitInstance =  OCM_MlUnitInstance.initWithMlUnit(unit,currentUser.userid ,currentSessionId,getCurrentTime(),type);
        initScores();
    }

    public void startSectionByUnitNoUser(final OCM_MlUnit unit)
    {
        final String lastAppCode = (String)MainActivity.mainActivity.config.get(MainActivity.CONFIG_APP_CODE);
        currentUnitInstance = null;
        //
        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    MainActivity.mainActivity.updateConfigPaths(unit.config, false, unit.lang);
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
                        if (MainActivity.mainActivity.isDebugMode())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            MainActivity.mainActivity.updateConfigPaths(lastAppCode, false, null);
                        }
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

    public void startSectionByUnit(final OCM_MlUnit unit, boolean study)
    {
        sectionStartedWithUnit(unit, study ? OCM_MlUnitInstance.INSTANCE_TYPE_STUDY : OCM_MlUnitInstance.INSTANCE_TYPE_REVIEW);
        final String lastAppCode = (String)MainActivity.mainActivity.config.get(MainActivity.CONFIG_APP_CODE);

        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    //MainActivity.mainActivity.updateConfigPaths(unit.config, false, unit.lang);
                    if(OBMainViewController.MainViewController().pushViewControllerWithNameConfig("OC_TestEvent","oc-childmenu",true,true,"test"))
                    // if(MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {
                        currentUnitInstance.sectionController = MainViewController().topController();
                        startUnitInstanceTimeout(currentUnitInstance);
                    }
                    else
                    {
                        if (MainActivity.mainActivity.isDebugMode())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            MainActivity.mainActivity.updateConfigPaths(lastAppCode, false, null);
                        }
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

    public void startPlayZone(final boolean transfer,final boolean first)
    {
        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {

                    StringBuffer stringBuffer = new StringBuffer("menu");
                    if(transfer)
                        stringBuffer.append("/intro=true");
                    if(first)
                        stringBuffer.append("/first=true");

                    MainActivity.mainActivity.updateConfigPaths("oc-playzone", false,"en_GB");
                    if(MainViewController().pushViewControllerWithNameConfig("OC_PlayZoneMenu","oc-playzone",false,true,stringBuffer.toString()))
                    {

                    }
                    else
                    {

                    }
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);
                }
            }
        }.run();
    }


    /*
        unit check stuff
     */
    public int unitAttemptsCountInDB(DBSQL db, int userid, int masterlistid, int unitIndex)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT MAX(seqNo) as count FROM %s AS U " +
                        "JOIN %s AS UI ON UI.unitid = U.unitid " +
                        "WHERE UI.userid = ? AND U.masterlistid = ? AND U.unitIndex = ?",
                DBSQL.TABLE_UNITS,  DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(userid),String.valueOf(masterlistid),String.valueOf(unitIndex)));
        int count = 0;

        int columnIndex = cursor.getColumnIndex("count");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
            count = cursor.getInt(columnIndex);

        cursor.close();

        return count;
    }

    public  boolean unitCompletedByCurrentUser(DBSQL db, OCM_MlUnit unit)
    {
        return unitCompletedByUser(db,currentUser.userid, unit.masterlistid, unit.unitIndex);
    }

    public boolean unitCompletedByUser(DBSQL db, int userid, int masterlistid, int unitIndex)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT U.unitid FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid " +
                        "WHERE UI.userid = ? AND U.masterlistid = ? AND U.unitIndex = ? AND UI.endTime > 0",
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

    /*
     Sessions functions
     */
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
                    , Collections.singletonList(String.valueOf(userid)));
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
            MainActivity.log("OCM_FatController: database access error: " + e.getMessage());
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

        Cursor cursor = db.prepareRawQuery(String.format("SELECT COUNT(DISTINCT(UI.unitid)) AS count FROM %s AS U "+
                "JOIN %s AS UI ON UI.unitid = U.unitid "+
                "WHERE UI.userid = ? AND UI.sessionid = ? "+
                "AND (UI.seqNo >= ? OR UI.endTime > 0)",DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES),Arrays.asList(String.valueOf(userid),
                String.valueOf(sessionid),String.valueOf(unitAttemptsCount-1)));

        int columnIndex = cursor.getColumnIndex("count");
        if(cursor.moveToFirst() && !cursor.isNull(columnIndex))
            result = cursor.getInt(columnIndex);

        cursor.close();
        return result;
    }

    public void prepareNewSessionUser(int userid)
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            prepareNewSessionInDB(db, userid);
        }
        catch(Exception e)
        {
            MainActivity.log("OCM_FatController: database access error: " + e.getMessage());
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

        resetTempData();
    }

    public void prepareNewSession()
    {
        prepareNewSessionUser(currentUser.userid);
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
        {}
        finally
        {
            if(db != null)
                db.close();
        }

    }

    public void finishCurrentSessionInDB(DBSQL db)
    {

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


    /*
    Play zone assets functions
     */
    public List<OC_PlayZoneAsset> getPlayZoneAssetForCurrentUser()
    {
        List<OC_PlayZoneAsset> assets = OC_PlayZoneAsset.assetsFromDBForUserId(currentUser.userid);
        if(assets.size() > MAX_PZ_ASSETS)
        {
            for(int i=MAX_PZ_ASSETS; i<assets.size(); i++)
            {
                OC_PlayZoneAsset asset = assets.get(i);
                asset.deleteAssetData();
            }
            assets = assets.subList(0,MAX_PZ_ASSETS);
        }

        return assets;
    }

    public boolean savePlayZoneAssetForCurrentUserType(int type,String thumbnail,Map<String,String> params)
    {
        boolean result = false;
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            result = OC_PlayZoneAsset.saveAssetInDBForUserId(db,currentUser.userid,type,thumbnail,params);
        }
        catch(Exception e)
        {
            MainActivity.log("OCM_FatController: database access error: " + e.getMessage());
        }
        finally
        {
            if(db != null)
                db.close();
        }
        return result;
    }


    /*
    Battery actions
     */

    @Override
    public void onBatteryStatusReceived(float level, boolean charging)
    {
        checkCurrentStatus();
    }

    public void loadBatteryIcon(OBSectionController controller)
    {
        OBControl batteryIcon = null;
        if(!controller.objectDict.containsKey("battery_icon"))
        {
            batteryIcon = controller.loadVectorWithName("battery_icon", new PointF(0.9f, 0.9f), controller.boundsf());
            batteryIcon.setScale(controller.applyGraphicScale(25)/batteryIcon.height());
            batteryIcon.setRight(controller.boundsf().right - controller.applyGraphicScale(10));
            batteryIcon.setBottom(controller.boundsf().bottom - controller.applyGraphicScale(10));
            batteryIcon.setZPosition(100);
            controller.objectDict.put("battery_icon", batteryIcon);
            refreshBatteryStatus(controller);
        }

    }

    public void refreshBatteryStatus(OBSectionController controller)
    {
        float batteryLevel = OBSystemsManager.sharedManager.getBatteryLevel();
        boolean charging = OBSystemsManager.sharedManager.isBatteryCharging();
        refreshBatteryStatus(batteryLevel,charging,controller);
    }

    public void refreshBatteryStatus(float batteryLevel, boolean charging, OBSectionController controller)
    {
        if(charging)
            setBatteryLevel(controller,0);
        else if(batteryLevel > 60.0)
            setBatteryLevel(controller,3);
        else if(batteryLevel > 20.0)
            setBatteryLevel(controller,2);
        else
            setBatteryLevel(controller,1);
    }

    private void setBatteryLevel(OBSectionController controller, int level)
    {
        if(controller.objectDict.containsKey("battery_icon"))
        {
            OBGroup batteryIcon = (OBGroup)controller.objectDict.get("battery_icon");
            batteryIcon.hideMembers("level_.*");
            batteryIcon.objectDict.get(String.format("level_%d",level)).show();
        }
    }

    public boolean batteryStatusLow()
    {
        float batteryLevel = OBSystemsManager.sharedManager.getBatteryLevel();
        return batteryLevel <= 10;
    }



}
