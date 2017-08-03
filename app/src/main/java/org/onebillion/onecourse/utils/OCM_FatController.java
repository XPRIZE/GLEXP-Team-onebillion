package org.onebillion.onecourse.utils;

import android.database.Cursor;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;

import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by michal on 02/08/2017.
 */

public class OCM_FatController extends OBFatController
{
    int unitAttemptsCount, disallowStartHour, disallowEndHour, playzoneActiveHour;
    OCM_MlUnitInstance currentUnitInstance;
    OCM_User currentUser;
    int currentSessionId;
    int currentMode;
    long currentSessionStartTime, currentSessionEndTime;
    boolean showTestMenu, showUserListMenu, allowsTimeouts, showUserName;
    Date startDate;

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
        String value = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_SHOW_BACK_BUTTON);
        return (value != null && value.equalsIgnoreCase("true"));
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
                MainActivity.log("OC_FatController:loadMasterListIntoDB:no masterlist in the settings file. skipping");
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
        DBSQL db = new DBSQL(true);
        try
        {
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

     //   checkAndPrepareNewSession();
    //    fixMissingStars(currentSessionId);
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
      //  loadLastSessionFromDB(db, user.userid);
        currentUser = user;
     //   if(currentSessionId == -1)
         //   prepareNewSessionInDB(db, user.userid);

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


    public OCM_MlUnit getNextUnitFromDB(DBSQL db)
    {
        OCM_MlUnit mlUnit = null;
        int currentWeek = getCurrentWeek();
        Cursor cursor = db.prepareRawQuery(String.format("SELECT MAX(unitIndex) AS unitIndex FROM %s AS U " +
                "JOIN %s AS UI ON UI.unitid = U.unitid " +
                "WHERE UI.userid = ? AND U.week = ? AND U.masterlistid = ? " +
                "AND (UI.setSeqNo >( ? OR UI.endTime > 0)", DBSQL.TABLE_UNITS,DBSQL.TABLE_UNIT_INSTANCES), Arrays.asList(String .valueOf(currentUser.userid),
                String.valueOf(currentWeek),String.valueOf(currentUser.masterlistid),String.valueOf(unitAttemptsCount-1)));

        if(cursor.moveToFirst())
        {
            int returnIndex = cursor.getInt(cursor.getColumnIndex("unitIndex"));
            mlUnit = OCM_MlUnit.nextMlUnitFromDB(db,currentUser.masterlistid,returnIndex);
        }
        cursor.close();

        if(mlUnit != null)
        {
            cursor = db.prepareRawQuery(String.format("SELECT MIN(unitIndex) AS unitIndex FROM %@ AS U WHERE U.week = ? AND U.masterlistid = ?", DBSQL.TABLE_UNITS),
                    Arrays.asList(String.valueOf(currentWeek),String.valueOf(currentUser.masterlistid)));

            if(cursor.moveToFirst())
            {
                int returnIndex = cursor.getInt(cursor.getColumnIndex("unitIndex"));
                mlUnit = OCM_MlUnit.mlUnitforMasterlistIDFromDB(db,currentUser.masterlistid,returnIndex);
            }
        }
        return mlUnit;
    }

    

}
