package org.onebillion.onecourse.utils;

import android.content.Intent;
import android.util.ArrayMap;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OBSectionController;

import java.lang.ref.SoftReference;
import java.util.Map;;

/**
 * Created by alan on 27/02/16.
 */
public class OBFatController
{
    Map<String,Map<String,String>> tempData;

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON | OBMainViewController.SHOW_TOP_RIGHT_BUTTON | OBMainViewController.SHOW_BOTTOM_LEFT_BUTTON | OBMainViewController.SHOW_BOTTOM_RIGHT_BUTTON;
    }

    public void gotAnswerRight(int amount)
    {

    }

    public void gotAnswerWrong(int amount)
    {

    }

    public void initScores()
    {

    }

    public void updateScores()
    {

    }

    public void completeEvent(OBSectionController cont)
    {
        try
        {
            updateScores();
            cont.displayAward();
            cont.exitEvent();
        }
        catch (Exception e)
        {

        }
        cont.exitEvent();
    }


    public void startUp()
    {
        OBConfigManager.sharedManager.updateConfigPaths(OBConfigManager.sharedManager.getMainFolder(), true);
        //
        String menuClassName = OBConfigManager.sharedManager.getMenuClassName();
        if (menuClassName != null)
        {
            MainActivity.mainViewController.pushViewControllerWithName(menuClassName, false, true, "menu");
        }
    }

    public void onPause(OBSectionController cont)
    {

    }

    public void onResume(OBSectionController cont)
    {

    }

    public void onExitSection(OBSectionController cont)
    {

    }

    public void onAlamReceived(Intent intent, OBSectionController cont)
    {

    }

    public void onBatteryStatusReceived(float level, boolean charging)
    {

    }

    public int databaseResource()
    {
        return -1;
    }

    public void saveTempData(Map<String,String> data, String key)
    {
        if(tempData == null)
            resetTempData();
        tempData.put(key,data);

    }

    public Map<String,String> getTempDataForKey(String key)
    {
        if(tempData == null)
            resetTempData();
        return tempData.get(key);

    }

    public void resetTempData()
    {
        tempData = new ArrayMap<>();
    }

}
