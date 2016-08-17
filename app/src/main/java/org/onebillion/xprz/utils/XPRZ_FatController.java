package org.onebillion.xprz.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.ArrayMap;

import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.OBSectionController;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/08/16.
 */
public class XPRZ_FatController extends OBFatController
{

    public long currentIndex, firstUnstartedIndex;
    public int scoreCorrect,scoreWrong;
    public float finalScore;
    public XPRZ_FatReceiver menu;
    public long sessionSegmentStartTime,sessionSegmentLastActive,sessionTotalTime;
    public OBUser currentUser;


    private static final int OFC_SUCCEEDED = 1,
            OFC_FINISHED_LOW_SCORE = 2,
            OFC_TIMED_OUT = 3,
            OFC_SESSION_TIMED_OUT = 4,
            OFC_UNIT_LIST = 5,
            OFC_FIRST_TIME_IN = 6,
            OFC_NEW_SESSION =7;


    MlUnitInstance currentUnitInstance;

    public void loadMasterListIntoDB()
    {
        DBSQL db = new DBSQL(true);
        Cursor cursor = db.prepareSelectOnTable("units", Collections.singletonList("COUNT(*) as count"),null);
        if(cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndex("count")) == 0)
        {
                cursor.close();
                try
                {
                   // db.beginTransaction();
                    String mlname = (String) MainActivity.mainActivity.config.get("masterlist");
                    OBXMLManager xmlManager = new OBXMLManager();
                    InputStream is = OBUtils.getInputStreamForPath(String.format("config/%s", mlname));
                    List<OBXMLNode> xml = xmlManager.parseFile(is);
                    MainActivity.mainActivity.getApplicationContext().deleteDatabase("db");
                    OBXMLNode rootNode = xml.get(0);
                    List<OBXMLNode> masterList = new ArrayList<>();
                    int unitid = 0;
                    for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                    {
                        masterList.addAll(levelNode.childrenOfType("unit"));
                        for (OBXMLNode node : levelNode.childrenOfType("unit"))
                        {
                            MlUnit.insertUnitFromXMLNodeintoDB(db,node,unitid, levelNode.attributeIntValue("id"));
                            unitid++;
                        }
                    }
                    db.commitTransaction();

                } catch (Exception e)
                {

                }
        }
        else
        {
            cursor.close();
        }
        db.close();
    }


    public void loadUser()
    {
        OBUser u = OBUser.lastUserFromDB();
        if (u == null)
        {
            u = OBUser.initAndSaveUserInDB("test");
            u.startNewSession();
        }
        currentUser = u;
    }

    public void deleteDBFile()
    {
        DBSQL.deleteDB();
    }

    @Override
    public void startUp()
    {

        initDB();

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
        currentIndex = firstUnstartedIndex =  currentUser.highestIncompleteUnitIDfromDB();
    }

    @Override
    public void initScores()
    {
        scoreCorrect = scoreWrong = 0;
        currentUnitInstance = MlUnitInstance.initMlUnitDBWith(currentUser.userid,currentIndex,currentUser.currentsessionid);
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
        updateScores();
        if (finalScore >= currentUnit().passThreshold)
            signalSectionSucceeded();
        else
            signalSectionFailed();

        try
        {
            cont.displayAward();
        }catch (Exception e)
        {

        }
        cont.exitEvent();
    }

    public boolean showTestMenu()
    {
        return true;
    }

    public MlUnit currentUnit()
    {
        return MlUnit.mlUnitFromDBforUnitID(currentIndex);
    }

    @Override
    public void updateScores()
    {
        int tot = scoreCorrect + scoreWrong;
        finalScore = 1;
        if (tot > 0)
            finalScore = scoreCorrect * 1.0f / tot;

        currentUnitInstance.endtime = System.currentTimeMillis()/1000;
        currentUnitInstance.score = finalScore;
        currentUnitInstance.elapsedtime = (int)(currentUnitInstance.endtime - currentUnitInstance.starttime);
        currentUnitInstance.updateDataInDB();
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

    public void signalUnitTimedOut()
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
            arr.add(MlUnit.mlUnitFromDBforUnitID(currentIndex + 1 + i));
        }
        return arr;
    }

    public MlUnit requestNextUnit()
    {
        //do all the black magic of finding out what's the next topic here
        return MlUnit.mlUnitFromDBforUnitID(firstUnstartedIndex);
    }


    public void sectionStartedWithIndex(long index)
    {
        currentIndex = index;
        if(currentIndex >= firstUnstartedIndex)
        {
            firstUnstartedIndex = currentIndex+1;
        }
        initScores();
    }

    public void startSectionByIndex(long unitId)
    {
        sectionStartedWithIndex(unitId);
        final MlUnit currunit = MlUnit.mlUnitFromDBforUnitID(unitId);
        MainActivity.mainActivity.updateConfigPaths(currunit.config, false, currunit.lang);

        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
               OBMainViewController.MainViewController().pushViewControllerWithNameConfig(currunit.target,currunit.config,true,false,currunit.params);
            }
        });

    }

    public void userActivityOccurred(long systime)
    {
        sessionSegmentLastActive = systime;
    }

    public long lastPlayedUnitIndex(DBSQL db)
    {
        return MlUnitInstance.lastPlayedUnitIndex(db);
    }


    public void resetDatabase()
    {
        DBSQL.deleteDB();
        initDB();

    }

    public void refreshUnitsList()
    {
        DBSQL db = new DBSQL(true);
        db.doDeleteOnTable("units",null);
        db.close();
        loadMasterListIntoDB();
    }

}
