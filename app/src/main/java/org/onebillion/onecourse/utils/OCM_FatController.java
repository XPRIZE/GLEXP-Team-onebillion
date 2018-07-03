package org.onebillion.onecourse.utils;

import android.app.AlarmManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PointF;
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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Array;
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
 * Fat Controller is the main source of logic of the software. It contains current user, session,
 * setups up units, menus etc. It's a single point decision that tells system what screen to display.
 */

public class OCM_FatController extends OBFatController implements OBSystemsManager.TimeSynchronizationReceiver
{
    public final static int STUDY_LISTID=1,
            PLAYZONE_LISTID=2,
            LIBRARY_LISTID=3;

    public int scoreCorrect,scoreWrong;
    public float finalScore;
    public Map<String,Integer> colourDict;
    public long sessionSegmentLastActive;
    public OCM_FatReceiver menu;

    private float lockBatteryLevel;
    private int unitAttemptsCount, disallowStartHour, disallowEndHour, playzoneActiveHour, playzoneLockTimeout;
    private OCM_MlUnitInstance currentUnitInstance;
    private OCM_User currentUser;
    private int currentSessionId, currentSessionDay;
    private int currentStudyListMaxWeek = 1, currentPlayzoneListMaxDay = 1;
    private long currentSessionStartTime, currentSessionEndTime, currentSessionWorkTime;
    private boolean allowsTimeOuts, showUserName;
    private Date startDate;
    private List<OCM_MlUnitInstance> unitInstancesList;

    private int replayAudioCount;
    private String menuAppCode;

    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    private boolean testMenuMode;

    public static final int SESSION_UNIT_COUNT = 15,
            SESSION_VALID_COUNT = 10,
            COLOUR_COUNT = 20,
            MASTERLIST_RESTART_WEEK = 15,
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

        if (showBackButton())
            result = result | OBMainViewController.SHOW_TOP_LEFT_BUTTON;
        return result;
    }

    private boolean showBackButton()
    {
        if(currentUnitInstance != null && currentUnitInstance.mlUnit.showBack == 1)
            return true;

        return currentUnitInstance == null || OBConfigManager.sharedManager.shouldShowBackArrow();
    }

    public interface SectionOpeningCallback
    {
        public void run (OCM_MlUnitInstance unitInstance, boolean success);
    }

    /**
     * Loads masterlist from xml into database
     * @param masterlistid int number the masterlist should have in db
     * @param mlname name of the masterlist(and folder in masterlist folder that contains the xml)
     */
    public void loadMasterListIntoDB(int masterlistid, String mlname)
    {

        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            long token = -1;
            Map<String,String> whereMap = new ArrayMap<>();
            whereMap.put("masterlistid",String.valueOf(masterlistid));
            whereMap.put("name",mlname);
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_MASTERLISTS, Arrays.asList("token"),whereMap);
            if(cursor.moveToFirst())
            {
                int columnIndex = cursor.getColumnIndex("token");
                if(!cursor.isNull(columnIndex))
                    token = cursor.getLong(columnIndex);
            }
            cursor.close();
            OBXMLManager xmlManager = new OBXMLManager();
            InputStream is = OBUtils.getInputStreamForPath(String.format("masterlists/%s/units.xml", mlname));
            List<OBXMLNode> xml = xmlManager.parseFile(is);
            OBXMLNode rootNode = xml.get(0);
            List<OBXMLNode> masterList = new ArrayList<>();
            long masterListToken = rootNode.attributeLongValue("token");
            if(token < 0 || token != masterListToken)
            {
                db.beginTransaction();
                try
                {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("masterlistid", masterlistid);
                    contentValues.put("name", mlname);
                    contentValues.put("folder", mlname);
                    contentValues.put("token", masterListToken);
                    db.doReplaceOnTable(DBSQL.TABLE_MASTERLISTS,contentValues);
                    whereMap = new ArrayMap<>();
                    whereMap.put("masterlistid",String.valueOf(masterlistid));
                    db.doDeleteOnTable(DBSQL.TABLE_UNITS, whereMap);
                    int unitIndex = 0;

                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        int level = levelNode.attributeIntValue("id");

                        List<OBXMLNode> nodes = levelNode.childrenOfType("unit");
                        masterList.addAll(nodes);

                        for (int i = 0; i < nodes.size(); i++)
                        {
                            OBXMLNode node = nodes.get(i);
                            OCM_MlUnit.insertUnitFromXMLNodeintoDB(db, node, masterlistid, unitIndex, level);

                            unitIndex++;
                        }
                    }

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

    private boolean showTestMenu()
    {
        // Needs to return false, because OC_TestMenu cant work with OCM_FatController
        return false;
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
                u = OCM_User.initAndSaveUserInDB(db,"Student",STUDY_LISTID,PLAYZONE_LISTID,LIBRARY_LISTID);
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

    private int maxLevelForListDB(DBSQL db, int masterlistid)
    {
        Map<String,String> map = new ArrayMap<>();
        map.put("masterlistid", String.valueOf(masterlistid));
        int maxLevel = 1;
        Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_UNITS, Arrays.asList("MAX(level) as level"),map);
        if(cursor.moveToFirst())
        {
            int columnIndex = cursor.getColumnIndex("level");
            if(!cursor.isNull(columnIndex))
                maxLevel = cursor.getInt(columnIndex);
        }
        cursor.close();
        return maxLevel;
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
        currentStudyListMaxWeek = maxLevelForListDB(db, user.studylistid);
        currentPlayzoneListMaxDay = maxLevelForListDB(db, user.playzonelistid);
        if(currentSessionId == -1)
            prepareNewSessionInDB(db, user.userid);

    }

    /* Date/Time functions
    */

    @Override
    public void timeReceived(long timestamp)
    {
        MainActivity.log("OCM_FatController.timeReceived "+timestamp);
        if(startDate != null)
        {
            if(getDaysBetweenTimestamps(startDate.getTime(), timestamp) >= 0)
            {
                OBSystemsManager.sharedManager.setSystemTime(timestamp);
            }
        }
        else
        {
            OBSystemsManager.sharedManager.setSystemTime(timestamp);
        }

    }

    /**
     * Checks if current system clock provides correct time.
     * Time is considered incorrect if it's year is lower than 2017
     * or when current day is before the trial start date.
     *
     * @return true if time is incorrect.
     */
    public boolean currentTimeIsDirty()
    {
        return timestampIsDirty(System.currentTimeMillis());
    }

    public boolean timestampIsDirty(long timestampMillis)
    {
        if(startDate == null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestampMillis);
            return calendar.get(Calendar.YEAR) < 2017;
        }
        else
        {
            return getDaysBetweenTimestamps(startDate.getTime(), timestampMillis) < 0;
        }
    }


    /**
     * @return current timestamp in seconds.
     */
    public long getCurrentTime()
    {
        return  System.currentTimeMillis()/1000;
    }

    public int getCurrentDay()
    {
        if(startDate == null)
            return 1;

        if(!currentTimeIsDirty())
        {
            return getDaysSinceStartDate();
        }
        else
        {
            return currentSessionDay;
        }
    }

    /**
     * Get amount of days since the first day of usage(Start Day). This is the base for calculations
     * of current weeks and masterlist weeks below.
     * @return days since Start Day starting from 1(1 is Start Day)
     */
    public int getDaysSinceStartDate()
    {
        if(startDate == null)
            return 1;

        int currentDay = getDaysSinceTimestamp(startDate.getTime());
        if (currentDay > 0)
            return currentDay + 1;
        else
            return 1;
    }

    private int getDaysSinceTimestamp(long timestamp)
    {
        return getDaysBetweenTimestamps(timestamp,getCurrentTime()*1000);
    }

    private int getDaysBetweenTimestamps(long millisFrom, long millisTo)
    {
        long dayLengthMillisec = 1000 * 60 * 60 * 24;
        int dayDif = (int)Math.floor((millisTo - millisFrom) / dayLengthMillisec);
        return dayDif;
    }

    /**
     * Current week since Start Day.
     * @return int starting from 1(first week of usage)
     */
    public int getCurrentWeek()
    {
        return (int)Math.ceil(getCurrentDay() / 7.0f);
    }

    /**
     * Gets current day of the week for the child menu.
     * @return int 1-7 where 1 - Monday
     */
    public int getCurrentDayOfWeek()
    {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        day--;
        if(day<1)
            day = 7;
        return day;
    }

    /**
     * Get day for study masterlist.
     * This day loops up back to MASTERLIST_RESTART_WEEK once the date is beyond the lenght of masterlist
     * @return int day number since start date
     */
    public int getMasterlistDay()
    {
        int lastDay = currentStudyListMaxWeek * 7;
        int currentDay = getCurrentDay();
        if(currentDay <= lastDay)
        {
            return currentDay;
        }
        else
        {
            int currentRestartWeek = MASTERLIST_RESTART_WEEK;
            if(currentRestartWeek > currentStudyListMaxWeek)
                currentRestartWeek = 1;
            int restartDay = (currentRestartWeek-1)*7 + 1;
            return restartDay + ((currentDay - lastDay - 1) % (lastDay - restartDay + 1));
        }
    }

    /**
     *   Get day for playzone masterlist.
     *   Playzone days loop up just like study masterlist days do.
     * @return int day number since start date
     */
    public int getPlayzoneDay()
    {
        int currentDay = getMasterlistDay();
        return ((currentDay-1)%currentPlayzoneListMaxDay)+1;
    }

    public int getMasterlistWeek()
    {
        return (int)Math.ceil(getMasterlistDay() / 7.0f);
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
        if(testMenuMode)
            return;

        boolean batteryStatusLow = batteryStatusLow();
        if(batteryStatusLow ||
                currentSessionLocked() ||
                checkAndPrepareNewSession())
        {
            closeCurrentUnitInstance(batteryStatusLow ? OCM_MlUnitInstance.STATUS_BATTERY_LOW
            : OCM_MlUnitInstance.STATUS_SESSION_LOCKED,MainViewController().topController());

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

    /**
     * Starts timer (as handler) for unit instances timeout
     * @param unitInstance
     */
    public void startUnitInstanceTimeout(final OCM_MlUnitInstance unitInstance)
    {
        cancelTimeout();

        if(unitInstance.mlUnit.targetDuration <= 0)
            return;

        timeoutRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if(checkTimeout(unitInstance))
                    timeoutHandler.postDelayed(this,60*1000);
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable,(int)(unitInstance.mlUnit.targetDuration)*1000);
    }

    public void cancelTimeout()
    {
        if(timeoutRunnable != null)
            timeoutHandler.removeCallbacks(timeoutRunnable);

        timeoutRunnable = null;
    }

    /**
     * Checks if timeout for unit instance should be allowed
     * @param unitInstance
     * @return
     */
    public boolean checkTimeout(OCM_MlUnitInstance unitInstance)
    {
        if (!allowsTimeOuts)
            return false;

        if(unitInstance == null)
            return false;

        if(unitInstance.mlUnit.targetDuration < 0)
            return false;

        if(unitInstance != currentUnitInstance)
            return false;

        if(unitInstance.sectionController == null || unitInstance.sectionController._aborting)
            return false;

        if((unitInstance.startTime + unitInstance.mlUnit.targetDuration) <= getCurrentTime())
        {
            timeOutUnit(unitInstance);
            return false;
        }
        return true;
    }

    /**
     * triggers timeouts for the unit instance if possible
     * @param unitInstance
     */
    public void timeOutUnit(OCM_MlUnitInstance unitInstance)
    {
        if(currentSessionLocked())
        {
            signalSessionLocked();
        }
        else
        {
            boolean skipUnit = ((unitInstance.typeid == OCM_MlUnitInstance.INSTANCE_TYPE_STUDY ||
                                unitInstance.typeid == OCM_MlUnitInstance.INSTANCE_TYPE_EXTRA) &&
                    unitInstance.seqNo >= unitAttemptsCount - 1);
            DBSQL db = null;
            try
            {
                db = new DBSQL(true);
                if(skipUnit && unitInstance.mlUnit.starOrder > 0)
                    unitInstance.starColour = nextStarColourFromDB(db,unitInstance.typeid);

                setUnitInstanceStatus(unitInstance, OCM_MlUnitInstance.STATUS_UNIT_TIMEOUT);
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

            if(skipUnit)
                signalUnitFailed();
            else
                signalUnitTimedOut();
        }
        unitInstancesList.remove(unitInstance);
        currentUnitInstance = null;
        if(unitInstance.sectionController != null && !unitInstance.sectionController._aborting)
            unitInstance.sectionController.exitEvent();
        unitInstance.sectionController = null;
    }

    /**
     * Sets all the fields for unit instance such us score, end time, elapsed time
     * @param unitInstance
     * @param statusid of unit completion. Aside from standard completion, the unit instance
     *                 might be closed because of timeout, low battery status, night time etc
     *                 (Full list of statuses on top of OCM_MlUnitInstance file)
     */
    private void setUnitInstanceStatus(OCM_MlUnitInstance unitInstance, int statusid)
    {
        unitInstance.statusid = statusid;
        unitInstance.endTime = getCurrentTime();
        unitInstance.scoreCorrect = scoreCorrect;
        unitInstance.scoreWrong = scoreWrong;
        unitInstance.elapsedTime = (int)(unitInstance.endTime - unitInstance.startTime);
        unitInstance.addExtraData("replay_audio",replayAudioCount);
        if(unitInstance.mlUnit.targetDuration > 0 &&
                unitInstance.elapsedTime > unitInstance.mlUnit.targetDuration)
            unitInstance.elapsedTime = unitInstance.mlUnit.targetDuration;
    }

    /**
     * Completes current unit instance if valid
     * @param statusid explained above
     * @param sectionController current event screen that it is triggered from
     */
    public void closeCurrentUnitInstance(int statusid, OBSectionController sectionController)
    {
        OCM_MlUnitInstance unitInstance = currentUnitInstance;
        if(currentUnitInstance == null && sectionController != null)
        {
            for(OCM_MlUnitInstance instance : unitInstancesList)
            {
                if (instance.sectionController == sectionController)
                {
                    unitInstance = instance;
                    break;
                }
            }
        }
        if(unitInstance != null)
        {
            DBSQL db = null;
            try
            {
                db = new DBSQL(true);
                setUnitInstanceStatus(unitInstance,statusid);
                unitInstance.updateDataInDB(db);
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

            unitInstancesList.remove(unitInstance);
            unitInstance.sectionController = null;
        }

        currentUnitInstance = null;
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
        cancelTimeout();
        closeCurrentUnitInstance(OCM_MlUnitInstance.STATUS_USER_CLOSED, cont);
    }

    @Override
    public void onReplayAudioButtonPressed()
    {
        if (currentUnitInstance != null)
        {
            replayAudioCount++;
        }
    }

    @Override
    public void onSectionStarted(OBSectionController controller)
    {
        if (currentUnitInstance != null)
        {
            currentUnitInstance.sectionController = controller;
        }
    }

    /**
     * Gets next unit that user should do. This calculation is based on amount of day it has passed since the start
     * and units done during current week. Each week is a "fresh" start of next level in masterlist,
     * however following days within the week will have units based on progress/diagnostic results.
     *
     * @param db object
     * @return unit
     */
    public OCM_MlUnit getNextUnitFromDB(DBSQL db)
    {
        OCM_MlUnit mlUnit = null;
        int currentWeek = getCurrentWeek();
        int materlistWeek = getMasterlistWeek();
        int firstDay = ((currentWeek-1)*7)+1;
        int lastDay = currentWeek*7;
        Cursor cursor = null;

        //test if there are any extra units in the table
        int totalExtraUnitCount = 0;
        Map<String,String> whereMap = new ArrayMap<>();
        whereMap.put("userid", String.valueOf(currentUser.userid));
        whereMap.put("level", String.valueOf(currentWeek));
        cursor = db.doSelectOnTable(DBSQL.TABLE_EXTRA_UNITS, Arrays.asList("COUNT(*) as count"),whereMap);
        if(cursor.moveToFirst())
        {
            int index = cursor.getColumnIndex("count");
            if(!cursor.isNull(index))
                totalExtraUnitCount = cursor.getInt(index);
        }
        cursor.close();

        if(totalExtraUnitCount > 0)
        {
            //if extra units are found check how many of them have been completed this week
            cursor = db.prepareRawQuery(String.format("SELECT MAX(orderIndex) as orderIndex,  COUNT(*) as count, " +
                    "SUM(CASE WHEN sessionid = %d THEN 1 ELSE 0 END) as sessionCount " +
                    "FROM (SELECT EU.unitid AS unitid,  EU.orderIndex AS orderIndex, MAX(S.sessionid) as sessionid " +
                    "FROM %s AS EU " +
                    "JOIN %s AS UI ON UI.unitid = EU.unitid AND EU.userid = UI.userid " +
                    "AND EU.userid = ? AND EU.level = ? AND UI.typeid = ? " +
                    "JOIN %s AS S ON S.sessionid = UI.sessionid AND S.userid = UI.userid " +
                    "AND S.day >= ? AND S.day <= ? " +
                    "WHERE (UI.seqNo >= ? OR UI.statusid IN (?,?)) " +
                    "GROUP BY EU.userid, EU.level, EU.unitid ) TAB",currentSessionId,
                    DBSQL.TABLE_EXTRA_UNITS,DBSQL.TABLE_UNIT_INSTANCES,DBSQL.TABLE_SESSIONS),
                    Arrays.asList(String.valueOf(currentUser.userid),
                            String.valueOf(currentWeek),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_EXTRA),
                            String.valueOf(firstDay),String.valueOf(lastDay),String.valueOf(unitAttemptsCount-1),
                            String.valueOf(OCM_MlUnitInstance.STATUS_COMPLETED),
                            String.valueOf(OCM_MlUnitInstance.STATUS_FAILURE)));


            int lastOrderIndex = -1;
            int completedExtraCount = 0;
            int currentSessionExtraCount = 0;

            if(cursor.moveToFirst())
            {
                int orderIndexIndex = cursor.getColumnIndex("orderIndex");
                int countIndex = cursor.getColumnIndex("count");
                int sessionCountIndex = cursor.getColumnIndex("sessionCount");

                if(!cursor.isNull(orderIndexIndex))
                    lastOrderIndex = cursor.getInt(orderIndexIndex);

                if(!cursor.isNull(countIndex))
                    completedExtraCount = cursor.getInt(countIndex);

                if(!cursor.isNull(sessionCountIndex))
                    currentSessionExtraCount = cursor.getInt(sessionCountIndex);

            }
            cursor.close();

            int totalExtraDayCount = (int)Math.ceil(totalExtraUnitCount/2.0f);

            //if half of count of them have already been completed we can move on to normal schedule
            //otherwise pick the next of extra list
            if(currentSessionExtraCount < totalExtraDayCount && completedExtraCount < totalExtraUnitCount)
            {
                cursor = db.prepareRawQuery(String.format("SELECT * FROM %s " +
                        "WHERE unitid IN (" +
                        "SELECT unitid FROM %s " +
                        "WHERE userid = ? AND level = ? AND orderIndex > ? " +
                        "ORDER BY orderIndex LIMIT 1)",
                        DBSQL.TABLE_UNITS, DBSQL.TABLE_EXTRA_UNITS),
                        Arrays.asList(String.valueOf(currentUser.userid), String.valueOf(currentWeek),
                                String.valueOf(lastOrderIndex)));

                if(cursor.moveToFirst())
                {
                    mlUnit = OCM_MlUnit.mlUnitFromCursor(cursor);
                    mlUnit.typeid = OCM_MlUnit.TYPE_EXTRA;
                }
                cursor.close();
            }

            //normally the unit has to be the same week as current week, but in this case we skip this check as it's unit from the past
            if(mlUnit != null)
                return mlUnit;
        }



        //if no unit found search the masterlist for the next study one
        if(mlUnit == null)
        {
            //search for a next study unit for current days of the week
            cursor = db.prepareRawQuery(String.format("SELECT MAX(unitIndex) AS unitIndex FROM %s AS U " +
                            "JOIN %s AS UI ON UI.unitid = U.unitid AND U.masterlistid = ? AND UI.typeid = ? " +
                            "JOIN %s AS S ON S.sessionid = UI.sessionid AND S.userid = UI.userid " +
                            "WHERE UI.userid = ? AND U.level = ?  AND S.day >= ? AND S.day <= ? " +
                            "AND (UI.seqNo >= ? OR UI.statusid IN (?,?))", DBSQL.TABLE_UNITS, DBSQL.TABLE_UNIT_INSTANCES, DBSQL.TABLE_SESSIONS),
                    Arrays.asList(String.valueOf(currentUser.studylistid), String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_STUDY)
                            , String.valueOf(currentUser.userid),
                            String.valueOf(materlistWeek), String.valueOf(firstDay),
                            String.valueOf(lastDay), String.valueOf(unitAttemptsCount - 1), String.valueOf(OCM_MlUnitInstance.STATUS_COMPLETED),
                            String.valueOf(OCM_MlUnitInstance.STATUS_FAILURE)));

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("unitIndex");
                if (!cursor.isNull(columnIndex))
                    mlUnit = OCM_MlUnit.nextMlUnitFromDB(db, currentUser.studylistid, cursor.getInt(columnIndex));
            }
            cursor.close();
        }

        //if no unit found, this means it's a new week
        if(mlUnit == null)
        {
            //pick first units for that period
            cursor = db.prepareRawQuery(String.format("SELECT MIN(unitIndex) AS unitIndex FROM %s AS U WHERE U.level = ? AND U.masterlistid = ?", DBSQL.TABLE_UNITS),
                    Arrays.asList(String.valueOf(materlistWeek),String.valueOf(currentUser.studylistid)));

            if(cursor.moveToFirst())
            {
                int columnIndex = cursor.getColumnIndex("unitIndex");
                if(!cursor.isNull(columnIndex))
                    mlUnit = OCM_MlUnit.mlUnitforMasterlistIDFromDB(db,currentUser.studylistid, cursor.getInt(columnIndex));
            }
            cursor.close();
        }
       if(mlUnit.level != materlistWeek)
            return null;
        return mlUnit;
    }

    /* Database functions
    */

    public void initDB()
    {
        String mlname = OBConfigManager.sharedManager.getMasterlist();
        if (mlname.length() > 0)
        {
            loadMasterListIntoDB(STUDY_LISTID,mlname);
        }
        else
        {
            MainActivity.log("OCM_FatController:loadMasterListIntoDB:no study masterlist in the settings file. skipping");
        }

        mlname = OBConfigManager.sharedManager.getMasterlistForPlayzone();
        if (mlname.length() > 0)
        {
            loadMasterListIntoDB(PLAYZONE_LISTID,mlname);
        }
        else
        {
            MainActivity.log("OCM_FatController:loadMasterListIntoDB:no playzone masterlist in the settings file. skipping");
        }

        mlname = OBConfigManager.sharedManager.getMasterlistForLibrary();
        if (mlname.length() > 0)
        {
            loadMasterListIntoDB(LIBRARY_LISTID,mlname);
        }
        else
        {
            MainActivity.log("OCM_FatController:loadMasterListIntoDB:no library masterlist in the settings file. skipping");
        }

        String mac = OBPreferenceManager.getStringPreference(OBPreferenceManager.PREFERENCES_MAC_NUM);
        if(mac == null)
        {
            mac = OBSystemsManager.sharedManager.device_getMac();
            if(mac != null)
                OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCES_MAC_NUM, mac);
        }

        String serial = OBPreferenceManager.getStringPreference(OBPreferenceManager.PREFERENCES_SERIAL_NUM);
        if(serial == null)
        {
            serial = OBSystemsManager.sharedManager.device_getSerial();
            if(serial != null)
                OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCES_SERIAL_NUM, serial);
        }

        loadStartDate();
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
        // initial setup
        //DBSQL.deleteDB();
        unitInstancesList = new ArrayList<>();
        try
        {
            unitAttemptsCount = OBConfigManager.sharedManager.getFatControllerMaxUnitAttempts();
            String disallowHours = OBConfigManager.sharedManager.getFatControllerNightModeHours();
            String[] disallowArray = disallowHours.split(",");
            disallowStartHour = Integer.valueOf(disallowArray[0]);
            disallowEndHour = Integer.valueOf(disallowArray[1]);
            showUserName = OBConfigManager.sharedManager.shouldFatControllerShowUserName();
            allowsTimeOuts = OBConfigManager.sharedManager.isFatControllerSessionTimeoutEnabled();
            playzoneActiveHour = OBConfigManager.sharedManager.getFatControllerPlayzoneActiveHour();
            lockBatteryLevel = OBConfigManager.sharedManager.getBatteryMaxValueForLevel(OBConfigManager.BATTERY_LEVEL_CRITICAL);
            playzoneLockTimeout = OBConfigManager.sharedManager.getFatControllerPlayzoneLockTimeout();
            if (lockBatteryLevel < 0) lockBatteryLevel = 10;
        }
        catch (Exception e)
        {
            unitAttemptsCount = 3;
            disallowStartHour = 22;
            disallowEndHour = 5;
            showUserName = false;
            allowsTimeOuts = true;
            playzoneActiveHour = 12;
            lockBatteryLevel = 10;
            playzoneLockTimeout = 10;
        }
        initDB();
        //
        timeoutHandler = new Handler();
        // Setup screen
        Boolean usesSetupMenu = OBConfigManager.sharedManager.isSetupMenuEnabled();
        Boolean isSetupComplete = OBPreferenceManager.getBooleanPreference(OBPreferenceManager.PREFERENCES_SETUP_COMPLETE);
        long trialTimestamp = OBPreferenceManager.getLongPreference(OBPreferenceManager.PREFERENCES_TRIAL_START_TIMESTAMP);
        if(trialTimestamp < 0 && currentTimeIsDirty())
            isSetupComplete = false;
        //
        if (usesSetupMenu && !isSetupComplete)
        {
            // before overriding the app_code save it in the preferences to restore after setup is complete
            testMenuMode = true;
            menuAppCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
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
            //
            if (showTestMenu())
            {
                MainViewController().pushViewControllerWithName("OC_TestMenu", false, false, "menu");
            }
            else
            {
                testMenuMode = false;
                // setup is now complete: continue as usual
                prepareAlarm();
                //
                resetTempData();
                // restore app_code is it's coming from setup
                if (menuAppCode != null)
                {
                    OBConfigManager.sharedManager.updateConfigPaths(menuAppCode, true);
                }
                //
                String menuClassName = OBConfigManager.sharedManager.getMenuClassName();
                String appCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
                //
                MainActivity.log("OC_FatController:startUp: pushing view controller [%s] [%s]", menuClassName, appCode);
                //
                if (menuClassName != null && appCode != null)
                {
                    OBBrightnessManager.sharedManager.onContinue();
                    if (!MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, true, null, true))
                    {
                        MainActivity.log("OC_FatController:startUp:unable to load view controller [%s] [%s]", menuClassName, appCode);
                    }
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
    Section functions
    */

    public void initScores()
    {
        if(currentUnitInstance == null)
            return;
        scoreCorrect = scoreWrong = replayAudioCount = 0;
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
        OBSystemsManager.sharedManager.setCurrentUnit(null);
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
            unitInstancesList.remove(currentUnitInstance);
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
            e.printStackTrace();
        }
        if(!cont._aborting)
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

            setUnitInstanceStatus(currentUnitInstance,OCM_MlUnitInstance.STATUS_COMPLETED);
            if(currentUnitInstance.mlUnit.masterlistid == currentUser.studylistid
                    && currentUnitInstance.mlUnit.typeid != OCM_MlUnit.TYPE_DIAGNOSTIC)
                currentUnitInstance.starColour = nextStarColourFromDB(db, currentUnitInstance.typeid);

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

    /**
     * Information provided to the child menu to generate the current screen
     * @return Map containing information about next unit, community mode state
     */
    public Map<String,Object> getNextUnitData()
    {
        Map<String,Object> dict = new ArrayMap<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Map<Integer, Integer> countMap = sessionUnitCountDB(db,currentSessionId,currentUser.userid,
                    currentUser.studylistid);
            if(countMap.get(OCM_MlUnit.TYPE_STANDARD) >= SESSION_UNIT_COUNT)
            {
                dict.put("community",true);
            }
            else
            {
                int fullCount = 1;
                for(int val : countMap.values())
                    fullCount += val;

                dict.put("unitOrder",fullCount);
                OCM_MlUnit currentUnit = getNextUnitFromDB(db);
                if(currentUnit != null)
                {
                    dict.put("community",false);
                    dict.put("unit", currentUnit);
                    if(currentUnit.typeid == OCM_MlUnit.TYPE_DIAGNOSTIC)
                        currentUnit.starOrder = -1;
                    else
                        currentUnit.starOrder = countMap.get(OCM_MlUnit.TYPE_STANDARD)+1;
                }
                else
                {
                    dict.put("community",true);
                }

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

    public void jumpToCommunity()
    {
        Map<String,Object> dict = getNextUnitData();
        while(!(boolean)dict.get("community"))
        {
            OCM_MlUnit unit = (OCM_MlUnit)dict.get("unit");
            int typeId;
            if(unit.typeid == OCM_MlUnit.TYPE_EXTRA)
                typeId = OCM_MlUnitInstance.INSTANCE_TYPE_EXTRA;
            else
                typeId = OCM_MlUnitInstance.INSTANCE_TYPE_STUDY;
            sectionStartedWithUnit(unit,typeId);
            updateScores();
            currentUnitInstance = null;
            dict = getNextUnitData();
        }
    }

    /**
     *
     * @return list of units for community mode(after 15 study units have been completed)
     */
    public List<OCM_MlUnit> getUnitsForGrid()
    {
        List<OCM_MlUnit> unitsList = new ArrayList<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Cursor cursor = db.prepareRawQuery(String.format("SELECT * FROM %s "+
                            "WHERE unitid IN (SELECT unitid FROM %s "+
                            "WHERE userid = ? AND sessionid = ?) AND masterlistid = ? AND typeid = ?"+
                            "GROUP BY unitid ORDER BY rowid ASC",DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES),
                    Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(currentSessionId),
                            String.valueOf(currentUser.studylistid),String.valueOf(OCM_MlUnit.TYPE_STANDARD)));

            if(cursor.getCount() != SESSION_UNIT_COUNT)
            {
                cursor.close();
                cursor = db.prepareRawQuery(String.format("SELECT * FROM %s "+
                                "WHERE unitid IN (SELECT unitid FROM %s "+
                                "WHERE level = ? AND masterlistid = ? AND typeid = ? ORDER BY unitIndex DESC LIMIT %d) AND masterlistid = ? "+
                                "GROUP BY unitid ORDER BY unitIndex ASC",DBSQL.TABLE_UNITS,DBSQL.TABLE_UNITS,SESSION_UNIT_COUNT),
                        Arrays.asList(String.valueOf(getMasterlistWeek()),String.valueOf(currentUser.studylistid),
                                String.valueOf(OCM_MlUnit.TYPE_STANDARD),String.valueOf(currentUser.studylistid)));
            }

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
        {
            MainActivity.log("OCM_FatController.getUnitsForGrid. exception caught: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.close();
        }

        return unitsList;

    }

    /**
     *
     * @return int 0-30
     * 0 - new moon(no moon visible)
     * 15 - full moon
     * 1-14, 16-30 part moon visible
     */
    public int getCurrentMoonPhase()
    {
        Calendar calendar = Calendar.getInstance();
        int lp = 2551443; // full moon phase(from full to full) in seconds
        calendar.setTimeInMillis(getCurrentTime()*1000);
        calendar.set(Calendar.HOUR_OF_DAY,20);
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
        if(currentTimeIsDirty())
            return true;

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(getCurrentTime()*1000);
        int hourNow = currentCalendar.get(Calendar.HOUR_OF_DAY);

        return hourNow >= playzoneActiveHour;
    }

    public boolean communityModeActive()
    {
        if(currentSessionStandardUnitCount() >= SESSION_UNIT_COUNT)
        {
            return true;
        }
        else
        {

            //Check if last unit for the week was already reached, if so show community mode
            boolean listWeekEndReached = false;
            DBSQL db = new DBSQL(false);
            try
            {
                int unitIndex = -1;
                Cursor cursor = db.prepareRawQuery(String.format("SELECT MAX(unitIndex) as unitIndex FROM %s " +
                                "WHERE level = ? AND masterlistid = ?",DBSQL.TABLE_UNITS)
                        ,Arrays.asList(String.valueOf(getMasterlistWeek()),String.valueOf(currentUser.studylistid)));

                if(cursor.moveToFirst())
                {
                    int index = cursor.getColumnIndex("unitIndex");
                    if(!cursor.isNull(index))
                        unitIndex = cursor.getInt(index);
                }
                cursor.close();

                if(unitIndex >= 0)
                    listWeekEndReached = unitCompletedByUser(db, currentUser.userid, currentUser.studylistid, unitIndex);

            }
            catch (Exception e)
            {}
            finally
            {
                if(db!=null)
                    db.close();
            }

            return listWeekEndReached;
        }
    }

    /*
    Stars functions
     */

    /**
     * List of stars to display on the top of child menu screen in study mode
     * @param db
     * @param sessionid
     * @return list of stars colours(as integers)
     */
    public List<Integer> getStudyStarsForSessionFromDB(DBSQL db,int sessionid)
    {
        List<Integer> coloursList = new ArrayList<>();
        Cursor cursor = db.prepareRawQuery(String.format("SELECT UI.starColour as starColour FROM %s AS UI " +
                        "JOIN %s AS U ON U.unitid = UI.unitid " +
                        "WHERE UI.userid = ? AND UI.sessionid = ? AND U.masterlistid = ? " +
                        "AND UI.starColour > 0 AND UI.typeid IN (?,?) " +
                        "GROUP BY UI.unitid ORDER BY UI.rowid ASC", DBSQL.TABLE_UNIT_INSTANCES, DBSQL.TABLE_UNITS),
                Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(sessionid),
                        String.valueOf(currentUser.studylistid),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_STUDY),
                        String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_EXTRA)));

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

    /**
     * Stars to display on units in community(grid) mode
     * @param db
     * @param sessionid
     * @return list of stars colours(as integers)
     */
    public List<Integer> getGridStarsForSessionFromDB(DBSQL db, int sessionid)
    {
        List<Integer> coloursList = new ArrayList<>();
        Cursor cursor = db.prepareRawQuery(String.format("SELECT UI.starColour as starColour FROM %s AS UI "+
                        "JOIN (SELECT UI.unitid as unitid, MAX(UI.seqNo) as seqNo FROM %s AS UI " +
                        "JOIN %s AS U on U.unitid = UI.unitid AND U.masterlistid = ? "+
                        "WHERE UI.userid = ? AND UI.sessionid = ? AND UI.starColour > 0 AND UI.typeid = ? "+
                        "GROUP BY UI.unitid) AS TAB ON UI.unitid = TAB.unitid AND UI.seqNo = TAB.seqNo "+
                        "WHERE UI.userid = ? AND UI.sessionid = ? AND UI.typeid =?",DBSQL.TABLE_UNIT_INSTANCES,DBSQL.TABLE_UNIT_INSTANCES,DBSQL.TABLE_UNITS),
                Arrays.asList(String.valueOf(currentUser.studylistid),String.valueOf(currentUser.userid),String.valueOf(sessionid),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_COMMUNITY),
                        String.valueOf(currentUser.userid),String.valueOf(sessionid),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_COMMUNITY)));
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
            coloursList = getStudyStarsForSessionFromDB(db,currentSessionId);
        }catch (Exception e)
        {}
        finally
        {
            if(db != null)
                db.close();
        }
        return coloursList;
    }

    /**
     * Get next possible star colour for current mode(either study or community). There are 20(COLOUR_COUNT) possible
     * colours and no 2 stars should have the same colour on one screen
     * @param db
     * @param typeid of the completed unit
     * @return int star colour
     */
    public int nextStarColourFromDB(DBSQL db, int typeid)
    {
        List<Integer> starColours = null;
        if(typeid == OCM_MlUnitInstance.INSTANCE_TYPE_COMMUNITY)
            starColours = getGridStarsForSessionFromDB(db,currentSessionId);
         else
            starColours = getStudyStarsForSessionFromDB(db,currentSessionId);

        List<Integer> nums = OBUtils.randomlySortedArray(OBMisc.integerList(1,COLOUR_COUNT));
        nums.removeAll(starColours);
        int starColour = -1;
        if(nums.size() > 0)
            starColour = nums.get(0);
        return starColour;
    }


    /**
     * Check and fix any missing starts in case of failure when it was not awarded(etc when battery low was triggered)
     * @param sessionId
     */
    public void fixMissingStars(int sessionId)
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            Cursor cursor = db.prepareRawQuery(String.format("SELECT UI.unitid as unitid, UI.typeid as typeid, MAX(UI.seqNo) as seqNo " +
                            "FROM %s AS UI " +
                            "JOIN %s AS U ON U.unitid = UI.unitid "+
                            "WHERE UI.userid = ? AND UI.sessionid = ? AND UI.typeid IN (?,?)"+
                            "AND (UI.seqNo >= ? OR UI.statusid IN(?,?)) AND UI.starColour < 0 AND U.typeid <> ? "+
                            "GROUP BY UI.unitid, UI.typeid", DBSQL.TABLE_UNIT_INSTANCES, DBSQL.TABLE_UNITS),
                    Arrays.asList(String.valueOf(currentUser.userid),String.valueOf(sessionId),
                            String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_STUDY),String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_EXTRA)
                            ,String.valueOf(unitAttemptsCount-1),
                            String.valueOf(OCM_MlUnitInstance.STATUS_COMPLETED),String.valueOf(OCM_MlUnitInstance.STATUS_FAILURE),
                            String.valueOf(OCM_MlUnit.TYPE_DIAGNOSTIC)));

            List<Map<String,Integer>> fixUnits = new ArrayList<>();
            if(cursor.moveToFirst())
            {
                while (cursor.isAfterLast() == false)
                {
                    int columnIndex = cursor.getColumnIndex("unitid");
                    int columnIndex2 = cursor.getColumnIndex("seqNo");
                    int columnIndex3 = cursor.getColumnIndex("typeid");
                    if(!cursor.isNull(columnIndex) && !cursor.isNull(columnIndex2) )
                    {
                        Map<String,Integer> unitData = new ArrayMap<>();
                        unitData.put("unitid", cursor.getInt(columnIndex));
                        unitData.put("seqNo", cursor.getInt(columnIndex2));
                        unitData.put("typeid", cursor.getInt(columnIndex3));
                        fixUnits.add(unitData);
                    }
                    cursor.moveToNext();
                }
            }
            cursor.close();

            if(fixUnits.size() > 0)
            {
                List<Integer> starColours = getStudyStarsForSessionFromDB(db, sessionId);
                List<Integer> availableColours = OBUtils.randomlySortedArray(OBMisc.integerList(1,COLOUR_COUNT));
                availableColours.removeAll(starColours);

                int index = 0;
                for(Map<String,Integer> unitData: fixUnits)
                {
                    Map<String,String> whereMap = new ArrayMap<>();
                    whereMap.put("userid",String.valueOf(currentUser.userid));
                    whereMap.put("sessionid",String.valueOf(sessionId));
                    whereMap.put("typeid",String.valueOf(unitData.get("typeid")));
                    whereMap.put("unitid",String.valueOf(unitData.get("unitid")));
                    whereMap.put("seqNo",String.valueOf(unitData.get("seqNo")));
                    ContentValues contentValues = new ContentValues();
                    int starColour = availableColours.size() > index ? availableColours.get(index) : OB_Maths.randomInt(1, COLOUR_COUNT);
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
        currentUnitInstance =  OCM_MlUnitInstance.initWithMlUnit(unit,currentUser.userid ,currentSessionId,getCurrentTime(),getCurrentWeek(),type);
        initScores();
    }

    /**
     * Start section without any tracking for testing purposes(via xprize menu)
     * @param unit
     */
    public void startSectionByUnitNoUser(final OCM_MlUnit unit)
    {
        final String lastAppCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
        currentUnitInstance = null;
        //
        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                String lang = OBConfigManager.sharedManager.getCurrentLanguage();
                //
                try
                {
                    OBConfigManager.sharedManager.updateConfigPaths(unit.config, false, unit.lang);
                    if(MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {

                    }
                    else
                    {
                        if (OBConfigManager.sharedManager.isDebugEnabled())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, lang);
                        }
                    }
                }
                catch (Exception exception)
                {
                    if (OBConfigManager.sharedManager.isDebugEnabled())
                    {
                        Toast.makeText(MainActivity.mainActivity, unit.target + " failed to open the unit.", Toast.LENGTH_LONG).show();
                        OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, lang);
                    }
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);

                    OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, lang);
                }
            }
        }.run();
    }

    /**
     * Setup and start unit for current user.
     * @param unit
     * @param instanceType
     * @param openingCallback callback triggered after opening or failing to open the unit
     */
    public void startUnit(final OCM_MlUnit unit, final int instanceType, final SectionOpeningCallback openingCallback)
    {
        sectionStartedWithUnit(unit, instanceType);
        final String lastAppCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {

                String lang = OBConfigManager.sharedManager.getCurrentLanguage();
                //
                try
                {

                    //TEST PARAMS
                    /*unit.config = "oc-community";
                    unit.target = "OCM_TestEvent";
                    unit.params = "test";*/

                    OBConfigManager.sharedManager.updateConfigPaths(unit.config, false, unit.lang);
                    if(MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {
                        unitInstancesList.add(currentUnitInstance);
                        if (OBConfigManager.sharedManager.isDebugEnabled())
                        {
                            OBSystemsManager.sharedManager.setCurrentUnit(unit.key);
                        }
                        startUnitInstanceTimeout(currentUnitInstance);
                        openingCallback.run(currentUnitInstance, true);
                    }
                    else
                    {
                        if (OBConfigManager.sharedManager.isDebugEnabled())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                        }
                        OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, lang);

                        openingCallback.run(currentUnitInstance, false);
                        signalUnitSucceeded();
                        closeCurrentUnitInstance(OCM_MlUnitInstance.STATUS_FAILURE, null);
                    }
                }
                catch (Exception ex)
                {
                    if (OBConfigManager.sharedManager.isDebugEnabled())
                    {
                        Toast.makeText(MainActivity.mainActivity, unit.target + " failed to open.", Toast.LENGTH_LONG).show();
                    }
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", ex);

                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    currentUnitInstance.addExtraData("error", sw.toString());

                    OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, lang);
                    openingCallback.run(currentUnitInstance, false);
                    signalUnitSucceeded();
                    closeCurrentUnitInstance(OCM_MlUnitInstance.STATUS_FAILURE, null);

                }
            }
        }.run();
    }

    public void startSectionByUnit(final OCM_MlUnit unit, boolean studyMode, final SectionOpeningCallback openingCallback)
    {
        int typeId = OCM_MlUnitInstance.INSTANCE_TYPE_COMMUNITY;
        if(studyMode)
        {
            if(unit.typeid == OCM_MlUnit.TYPE_EXTRA)
                typeId = OCM_MlUnitInstance.INSTANCE_TYPE_EXTRA;
            else
                typeId = OCM_MlUnitInstance.INSTANCE_TYPE_STUDY;
        }
        startUnit(unit,typeId,openingCallback);
    }

    /**
     * Open Play Zone screen
     * @param transfer true if Play Zone will start with transfer animation (box opening)
     * @param first true if first time entering Play Zone during current session
     */
    public void startPlayZone(final boolean transfer,final boolean first)
    {
        OBAnalyticsManager.sharedManager.enteredScreen(OBAnalytics.Screen.PLAY_ZONE);
        //
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

                    String lang = OBConfigManager.sharedManager.getCurrentLanguage();
                    OBConfigManager.sharedManager.updateConfigPaths("oc-playzone", false, lang);
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

    /**
     * Pull list of units for current day of the week for playzone
     * @return list of units
     */
    public List<OCM_MlUnit> getUnitsForPlayzone()
    {
        List<OCM_MlUnit> unitsList = new ArrayList<>();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);

            Cursor cursor = db.prepareRawQuery(String.format("SELECT * FROM %s WHERE masterlistid = ? AND level = ?",DBSQL.TABLE_UNITS),
                        Arrays.asList(String.valueOf(currentUser.playzonelistid),String.valueOf(getPlayzoneDay())));


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

    /**
     * Lock units are units that have to be done from time to time in Play Zone.
     * Picks unit that haven't been done in a while. Play Zone menu will lock to it.
     * @return unit id of the unit to lock to.
     */
    public int getPlayzoneLockUnitId()
    {
        int playZoneFunTime = playzoneLockTimeout * 60;
        int returnUnitId = -1;

        DBSQL db = null;
        try
        {
            db = new DBSQL(false);

            Cursor cursor = db.prepareRawQuery(String.format(
                    "SELECT SUM(elapsedTime) as elapsed FROM %s AS UI " +
                            "JOIN %s AS U ON U.unitid = UI.unitid AND U.masterlistid = ?" +
                            "WHERE  UI.sessionid = ? AND U.typeid = ? " +
                            "AND UI.startTime > (SELECT IFNULL(MAX(startTime),0) " +
                            "FROM %s WHERE sessionid = UI.sessionid AND typeid = ?)",
                    DBSQL.TABLE_UNIT_INSTANCES, DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES),
                    Arrays.asList(String.valueOf(currentUser.playzonelistid),String.valueOf(currentSessionId)
                            ,String.valueOf(OCM_MlUnit.TYPE_PLAYZONE_FUN),
                            String.valueOf(OCM_MlUnitInstance.INSTANCE_TYPE_PZ_LOCKED))

            );

            long elapsed = -1;
            if(cursor.moveToFirst())
            {
                int index = cursor.getColumnIndex("elapsed");
                if(!cursor.isNull(index))
                    elapsed = cursor.getLong(index);
            }
            cursor.close();


            if(elapsed >= playZoneFunTime)
            {
                cursor = db.prepareRawQuery(String.format("SELECT U.unitid as unitid, COUNT(UI.unitid) as count " +
                                "FROM %s AS U LEFT JOIN %s AS UI ON U.unitid = UI.unitid " +
                                "AND UI.sessionid = ? WHERE U.typeid = ? AND U.masterlistid = ? " +
                                "AND U.level = ? GROUP BY U.unitid ORDER BY count, U.unitIndex LIMIT 1",
                        DBSQL.TABLE_UNITS, DBSQL.TABLE_UNIT_INSTANCES),
                        Arrays.asList(String.valueOf(currentSessionId),String.valueOf(OCM_MlUnit.TYPE_PLAYZONE_STUDY),
                                String.valueOf(currentUser.playzonelistid),String.valueOf(getPlayzoneDay())));
                if(cursor.moveToFirst())
                {
                    int index = cursor.getColumnIndex("unitid");
                    if(!cursor.isNull(index))
                        returnUnitId = cursor.getInt(index);
                }
                cursor.close();
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(db != null)
                db.close();
        }


        return returnUnitId;
    }

    /**
     * Get list of units for library
     * @return list of levels and units for that level, there are 3 levels
     */
    public Map<Integer,List<OCM_MlUnit>> getUnitsForLibrary()
    {
        Map<Integer,List<OCM_MlUnit>> unitsMap = new ArrayMap();
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);

            Cursor cursor = db.prepareRawQuery(String.format("SELECT * FROM %s WHERE masterlistid = ? ORDER BY level, unitIndex",DBSQL.TABLE_UNITS),
                    Arrays.asList(String.valueOf(currentUser.librarylistid)));


            if(cursor.moveToFirst())
            {
                while (cursor.isAfterLast() == false)
                {
                    OCM_MlUnit mlUnit = OCM_MlUnit.mlUnitFromCursor(cursor);
                    if(!unitsMap.containsKey(mlUnit.level))
                        unitsMap.put(mlUnit.level, new ArrayList<OCM_MlUnit>());

                    unitsMap.get(mlUnit.level).add(mlUnit);
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

        return unitsMap;
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

    /**
     * Check is certain units was completed by used before.
     * @param db
     * @param userid
     * @param masterlistid
     * @param unitIndex
     * @return true if completed
     */
    public boolean unitCompletedByUser(DBSQL db, int userid, int masterlistid, int unitIndex)
    {
        Cursor cursor = db.prepareRawQuery(String.format("SELECT U.unitid FROM %s AS U JOIN %s AS UI ON UI.unitid = U.unitid " +
                        "WHERE UI.userid = ? AND U.masterlistid = ? AND U.unitIndex = ? AND (UI.seqNo >= ? OR UI.statusid IN(?,?))",
                DBSQL.TABLE_UNITS,  DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(userid),String.valueOf(masterlistid),
                        String.valueOf(unitIndex),String.valueOf(unitAttemptsCount-1),
                        String.valueOf(OCM_MlUnitInstance.STATUS_COMPLETED),String.valueOf(OCM_MlUnitInstance.STATUS_FAILURE)));

        boolean rowExists = cursor.moveToFirst();
        cursor.close();
        if(rowExists)
            return true;

        return false;
    }

    /*
     Sessions functions
     */

    /**
     * Checks if new session can be started, if yes, it sets up all the values for current session
     * @return
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
        calendarLastSession.setTimeInMillis(currentSessionWorkTime*1000);

        if(currentCalendar.get(Calendar.DAY_OF_YEAR) != calendarLastSession.get(Calendar.DAY_OF_YEAR)
                || currentCalendar.get(Calendar.YEAR) != calendarLastSession.get(Calendar.YEAR))
        {
            if(currentTimeIsDirty())
            {

                int count = currentSessionStandardUnitCount();
                if(count < SESSION_VALID_COUNT)
                {
                    currentSessionWorkTime = getCurrentTime();
                    DBSQL db = null;
                    try
                    {
                        db = new DBSQL(true);
                        updateCurrentSessionTimeInDB(db, "workTime", currentSessionWorkTime);
                    }
                    catch (Exception e)
                    {

                    }
                    finally
                    {
                        if(db != null)
                            db.close();
                    }
                    return false;
                }
            }
            prepareNewSession();
            return true;
        }
        return false;
    }


    /**
     * Check if current time is outside of allowed time then
     * the screen will display moon and stars(night mode)
     * @return true if locked
     */
    public boolean currentSessionLocked()
    {
        if(disallowEndHour == disallowStartHour)
            return false;

        if(currentTimeIsDirty())
            return false;

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(getCurrentTime()*1000);
        int hourNow = currentCalendar.get(Calendar.HOUR_OF_DAY);

        boolean hourIsBetween = (disallowEndHour > disallowStartHour && hourNow >= disallowStartHour && hourNow < disallowEndHour)
                || (disallowEndHour < disallowStartHour && (hourNow >= disallowStartHour || hourNow < disallowEndHour));
        return hourIsBetween;
    }

    /**
     * Check if current session is vailable or not
     * @return false if no session setup or current session locked
     */
    public boolean currentSessionFinished()
    {
        if(currentSessionId < 0)
            return true;

        return currentSessionEndTime > 0;
    }

    /**
     * Load latest session from the database and setup values for it.
     * @param db
     * @param userid
     */
    private void loadLastSessionFromDB(DBSQL db, int userid)
    {
        Map<String,String> whereMap  = new ArrayMap<>();
        whereMap.put("userid",String.valueOf(userid));
        currentSessionId = -1;
        currentSessionEndTime = currentSessionStartTime = currentSessionWorkTime = 0;
        currentSessionDay = 1;
        try
        {
            Cursor cursor = db.prepareRawQuery(String.format("SELECT sessionid, startTime, endTime, workTime, day FROM %s WHERE userid = ? ORDER BY sessionid DESC LIMIT 1",DBSQL.TABLE_SESSIONS)
                    , Collections.singletonList(String.valueOf(userid)));
            if (cursor.moveToFirst())
            {
                currentSessionDay = cursor.getInt(cursor.getColumnIndex("day"));
                currentSessionStartTime = cursor.getLong(cursor.getColumnIndex("startTime"));
                currentSessionEndTime = cursor.getLong(cursor.getColumnIndex("endTime"));
                currentSessionWorkTime = cursor.getLong(cursor.getColumnIndex("workTime"));
                currentSessionId = cursor.getInt(cursor.getColumnIndex("sessionid"));
            }
            cursor.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return amount of study units completed for current session
     */
    public int currentSessionStandardUnitCount()
    {
        if(currentSessionId < 0)
            return 0;

        int result = 0;

        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Map<Integer,Integer> countMap =  sessionUnitCountDB(db, currentSessionId, currentUser.userid, currentUser.studylistid);
            result = countMap.get(OCM_MlUnit.TYPE_STANDARD);
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

    private Map<Integer,Integer> sessionUnitCountDB(DBSQL db, int sessionid, int userid, int masterlistid)
    {
        Map<Integer,Integer> countMap = new ArrayMap<>();
        countMap.put(OCM_MlUnit.TYPE_STANDARD, 0);
        if(sessionid < 0)
            return countMap;


        Cursor cursor = db.prepareRawQuery(String.format("SELECT U.typeid as typeid, COUNT(DISTINCT(UI.unitid)) AS count " +
                        "FROM %s AS U "+
                        "JOIN %s AS UI ON UI.unitid = U.unitid AND U.masterlistid = ? "+
                        "WHERE UI.userid = ? AND UI.sessionid = ?  "+
                        "AND (UI.seqNo >= ? OR UI.statusid IN (?,?)) " +
                        "GROUP BY U.typeid",
                DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES),
                Arrays.asList(String.valueOf(masterlistid), String.valueOf(userid),String.valueOf(sessionid),
                        String.valueOf(unitAttemptsCount-1),String.valueOf(OCM_MlUnitInstance.STATUS_COMPLETED),
                String.valueOf(OCM_MlUnitInstance.STATUS_FAILURE)));

        int columnIndex1 = cursor.getColumnIndex("typeid");
        int columnIndex2 = cursor.getColumnIndex("count");
        if(cursor.moveToFirst())
        {
            while(cursor.isAfterLast() == false)
            {
                if(!cursor.isNull(columnIndex1))
                {
                    countMap.put(cursor.getInt(columnIndex1), cursor.getInt(columnIndex2));
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return countMap;
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
        currentSessionWorkTime = 0;

        int sessionid = currentSessionId;
        if(sessionid<0)
            sessionid = 1;
        else
            sessionid++;

        if(currentTimeIsDirty())
        {
            currentSessionDay++;
        }
        else
        {
            currentSessionDay = getDaysSinceStartDate();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("userid", userid);
        contentValues.put("sessionid", sessionid);
        contentValues.put("startTime",currentSessionStartTime);
        contentValues.put("workTime",currentSessionStartTime);
        contentValues.put("day",currentSessionDay);
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

    public boolean startCurrentSession()
    {
        if(!currentSessionReadyToStart())
            return false;

        boolean needRefresh = checkAndSetupStartDay();
        currentSessionStartTime = currentSessionWorkTime = getCurrentTime();
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            Map <String,String> whereMap = new ArrayMap<>();
            whereMap.put("userid",String.valueOf(currentUser.userid));
            whereMap.put("sessionid",String.valueOf(currentSessionId));
            ContentValues contentValues = new ContentValues();
            contentValues.put("startTime",currentSessionStartTime);
            contentValues.put("workTime",currentSessionWorkTime);
            if(!currentTimeIsDirty() && currentSessionDay != getCurrentDay())
            {
                currentSessionDay = getCurrentDay();
                contentValues.put("day", currentSessionDay);
                needRefresh = true;
            }
            boolean updated = db.doUpdateOnTable(DBSQL.TABLE_SESSIONS,whereMap, contentValues) > 0;
        }
        catch (Exception e)
        {
            MainActivity.log(e.getMessage());
        }
        finally
        {
            if(db != null)
                db.close();
        }
        return needRefresh;
    }

    public void finishCurrentSessionInDB(DBSQL db)
    {

        if(currentSessionId < 0)
            return;

        currentSessionEndTime = getCurrentTime();

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

    /**
     * Checks if no start date of the tablet usage, based on this date the units from masterlist will be picked up. If no date, set it up.
     * Start Day is different to Setup Day. Trial Start is aproximate date the trial started at. If today
     * is 14 day away from Setup Day, Start Day is setup to Setup Day. Otherwise it's set up to today.
     * This system assures that reusing different tablet will bring the child experience as close to previous progress as possible,
     * as long as it's setup to the same Start Day in xprize menu.
     * @return false if date already setup, true if date just setup
     */
    public boolean checkAndSetupStartDay()
    {
        if(startDate == null)
        {
            long trialTimestamp = OBPreferenceManager.getLongPreference(OBPreferenceManager.PREFERENCES_TRIAL_START_TIMESTAMP);

            if (trialTimestamp < 0)
            {
                long setupTimestamp = getSetupStartTimestamp();
                int days = getDaysSinceTimestamp(setupTimestamp);
                if (Math.abs(days) < 14)
                {
                    trialTimestamp = OBUtils.timestampForDateOnly(System.currentTimeMillis());
                }
                else
                {
                    trialTimestamp = setupTimestamp;
                }
                OBPreferenceManager.setPreference(OBPreferenceManager.PREFERENCES_TRIAL_START_TIMESTAMP, trialTimestamp);
                startDate = new Date(trialTimestamp);
                return true;
            }
        }
        return false;
    }

    public void loadStartDate()
    {
        long trialTimestamp = OBPreferenceManager.getLongPreference(OBPreferenceManager.PREFERENCES_TRIAL_START_TIMESTAMP);
        if (trialTimestamp > 0)
        {
            startDate = new Date(trialTimestamp);
        }
    }


    public long getSetupStartTimestamp()
    {
        long setupTimestamp = OBPreferenceManager.getLongPreference(OBPreferenceManager.PREFERENCES_SETUP_START_TIMESTAMP);
        if (setupTimestamp < 0)
        {
            Date date = Date.valueOf("2017-10-01");
            setupTimestamp = date.getTime();
        }
        return setupTimestamp;
    }


    /*
    Play zone assets functions
     */

    /**
     * Lists of various creations of the child that display at the bottom of playzone menu.
     * @return List of assets
     */
    public List<OC_PlayZoneAsset> getPlayZoneAssetForCurrentUser()
    {
        if (currentUser == null)
        {
            return new ArrayList<>();
        }
        //
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
        OBAnalyticsManager.sharedManager.playZoneAssetCreated(type, params);
        //
        boolean result = false;
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            long assetId = OC_PlayZoneAsset.saveAssetInDBForUserId(db,currentUser.userid,type,thumbnail,params);
            if(currentUnitInstance != null)
                currentUnitInstance.assetid = assetId;
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
        OBGroup batteryIcon = null;
        if(!controller.objectDict.containsKey("battery_icon"))
        {
            batteryIcon = controller.loadVectorWithName("battery_icon", new PointF(0.9f, 0.9f), controller.boundsf());
            batteryIcon.setScale(controller.applyGraphicScale(25)/batteryIcon.height());
            batteryIcon.setRight(controller.boundsf().right - controller.applyGraphicScale(10));
            batteryIcon.setBottom(controller.boundsf().bottom - controller.applyGraphicScale(10));
            batteryIcon.setZPosition(100);
            OBControl bar = batteryIcon.objectDict.get("level_bar");
            bar.setAnchorPoint(1,0.5f);
            controller.objectDict.put("battery_icon", batteryIcon);
            refreshBatteryStatus(controller);
        }

    }

    public void refreshBatteryStatus(OBSectionController controller)
    {
        float batteryLevel = OBSystemsManager.sharedManager.getBatteryLevel();
        boolean charging = OBSystemsManager.sharedManager.isBatteryCharging();
        setBatteryChargingLevel(batteryLevel,charging,controller);
    }

    public void setBatteryChargingLevel(float level, boolean charging, OBSectionController controller)
    {
        if(controller.objectDict.containsKey("battery_icon"))
        {
            OBGroup batteryIcon = (OBGroup)controller.objectDict.get("battery_icon");
            batteryIcon.hideMembers("level_.*");
            if(charging)
                batteryIcon.objectDict.get("level_charging").show();

            if(level > 20.0)
            {
                OBControl bar = batteryIcon.objectDict.get("level_bar");
                bar.setScaleX(OB_Maths.clamp01(level/100.0f));
                bar.show();
            }
            else
            {
                batteryIcon.objectDict.get("level_low").show();
            }
        }
    }

    public boolean batteryStatusLow()
    {
        float batteryLevel = OBSystemsManager.sharedManager.getBatteryLevel();
        return batteryLevel <= lockBatteryLevel;
    }

    public void deleteDBProgressAndReboot()
    {
        DBSQL.deleteDB();
        MainActivity.mainActivity.restartApplication();
    }

    /*
    extra data collection for units
     */

    public boolean shouldCollectMiscData()
    {
        return true;
    }

    public void collectMiscData(String tag, Object data)
    {
        if(currentUnitInstance != null)
            currentUnitInstance.addExtraData(tag, data);
    }

    /*
    extra units handling
     */

    private void saveExtraUnitsIntoDb(DBSQL db, int userid, int level, List<Integer> unitList)
    {
        db.beginTransaction();
        try {
            Map<String,String> whereMap = new ArrayMap<>();
            whereMap.put("userid", String.valueOf(userid));
            whereMap.put("level", String.valueOf(level));
            Cursor cursor = db.doSelectOnTable(DBSQL.TABLE_EXTRA_UNITS,Arrays.asList("COUNT(*) as count"),whereMap);
            int count = 0;
            if(cursor.moveToFirst())
            {
                int index = cursor.getColumnIndex("count");
                if(!cursor.isNull(index))
                    count = cursor.getInt(index);

            }

            if(count == 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put("userid", userid);
                contentValues.put("level", level);
                for (int i = 0; i < unitList.size(); i++) {
                    contentValues.put("orderIndex", i);
                    contentValues.put("unitid", unitList.get(i));
                    db.doInsertOnTable(DBSQL.TABLE_EXTRA_UNITS, contentValues);
                }
                db.setTransactionSuccessful();
            }
        }
        catch (Exception e) {

        }
        finally {
            db.commitTransaction();
        }
    }

    public void saveExtraUnitsForCurrentUser( List<Integer> unitList)
    {
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            saveExtraUnitsIntoDb(db, currentUser.userid, getCurrentWeek(), unitList);
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

    public void testExtraUnits()
    {
        if(currentUnitInstance.mlUnit.typeid == OCM_MlUnit.TYPE_DIAGNOSTIC)
        {
            List<Integer> units = new ArrayList<>();
            for(int i=0; i<10; i++)
                units.add(2+i);

            saveExtraUnitsForCurrentUser(units);
        }
    }


    public List<OCM_MlUnit> getUnitsForWeek(int start, int end)
    {
        List<OCM_MlUnit> result = new ArrayList<>();
        //
        DBSQL db = null;
        try
        {
            db = new DBSQL(false);
            Cursor cursor = null;
            if (start == -1 || end ==  -1)
            {
                cursor = db.prepareRawQuery(String.format("SELECT unitIndex AS unitIndex FROM %s AS U WHERE U.masterlistid = ?", DBSQL.TABLE_UNITS),
                        Arrays.asList(String.valueOf(currentUser.studylistid)));
            }
            else
            {
                cursor = db.prepareRawQuery(String.format("SELECT unitIndex AS unitIndex FROM %s AS U WHERE U.level >= ? AND U.level <= ? AND U.masterlistid = ?", DBSQL.TABLE_UNITS),
                        Arrays.asList(String.valueOf(start), String.valueOf(end), String.valueOf(currentUser.studylistid)));
            }
            //
            if (cursor.moveToFirst())
            {
                do
                {
                    int columnIndex = cursor.getColumnIndex("unitIndex");
                    if (!cursor.isNull(columnIndex))
                    {
                        result.add(OCM_MlUnit.mlUnitforMasterlistIDFromDB(db, currentUser.studylistid, cursor.getInt(columnIndex)));
                    }
                }
                while(cursor.moveToNext());
            }
            cursor.close();
            //
            return result;
        }
        catch (Exception e)
        {
            MainActivity.log("OC_DiagnosticsManager: database access error: " + e.getMessage());
        }
        finally
        {
            if (db != null)
                db.close();
        }
        return null;
    }

}
