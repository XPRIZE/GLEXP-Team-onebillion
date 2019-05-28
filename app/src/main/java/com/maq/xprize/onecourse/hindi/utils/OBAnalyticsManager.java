package com.maq.xprize.onecourse.hindi.utils;

import android.app.Activity;
import android.graphics.PointF;


import com.maq.xprize.onecourse.hindi.mainui.MainActivity;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * Created by pedroloureiro on 29/11/2017.
 */

public class OBAnalyticsManager implements OBAnalyticsProtocol
{
    public static OBAnalyticsManager sharedManager;

    protected OBAnalyticsManager ()
    {

    }

    @Override
    public void enteredScreen(String screen)
    {

    }


    @Override
    public void deviceTurnedOn ()
    {

    }

    @Override
    public void deviceTurnedOff ()
    {

    }

    @Override
    public void touchMadeInUnit (String unitID, PointF startLoc, long started, PointF endLoc, long finished)
    {

    }

    @Override
    public void communityModeUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {

    }


    @Override
    public void playZoneUnitCompleted (String activityID, long started, long finished, float score, int replayAudioPresses)
    {

    }

    public OBAnalyticsManager (Activity activity)
    {
        if (OBConfigManager.sharedManager.isAnalyticsEnabled())
        {
            startupAnalytics(activity);
        }
        //
        String analyticsClassName = "";
        try
        {
            analyticsClassName = OBConfigManager.sharedManager.getAnalyticsClassName();
            if (analyticsClassName == null)
            {
                MainActivity.log("No AnalyticsClassName set in config. AnalyticsManager reverted to root version (no actions)");
                sharedManager = this;
            }
            else
            {
                Class aClass = Class.forName("com.maq.xprize.onecourse.hindi.utils." + analyticsClassName);
                Constructor<?> cons = aClass.getConstructor(Activity.class);
                sharedManager = (OBAnalyticsManager) cons.newInstance(activity);
                //
                MainActivity.log("AnalyticsManager has been initialised with class name %s", analyticsClassName);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("Exception caught while trying to setup AnalyticsManager with class name %s: %s", analyticsClassName, e.toString());
            e.printStackTrace();
            MainActivity.log("AnalyticsManager reverted to root version (no actions)");
            sharedManager = this;
        }
    }

    protected void startupAnalytics(Activity activity)
    {
    }

    @Override
    public void onStart()
    {

    }

    @Override
    public void onStop()
    {

    }

    @Override
    public void deviceStatus()
    {

    }


    @Override
    public void deviceGpsLocation ()
    {

    }

    @Override
    public void deviceVolumeChanged (float value)
    {

    }

    @Override
    public void deviceScreenTurnedOn ()
    {

    }

    @Override
    public void deviceScreenTurnedOff ()
    {

    }

    @Override
    public void deviceMobileSignalStrength (int value)
    {

    }

    @Override
    public void deviceStorageUse ()
    {

    }


    @Override
    public void batteryState (float batteryValue, Boolean pluggedIn, String chargerType)
    {

    }

    @Override
    public void studyZoneUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {

    }

    @Override
    public void playZoneVideoWatched (String videoID)
    {

    }

    @Override
    public void playZoneAssetCreated (int assetType, Map<String, String> data)
    {

    }

    @Override
    public void deviceHeadphonesPluggedIn ()
    {

    }

    @Override
    public void deviceHeadphonesUnplugged ()
    {

    }

    @Override
    public void uploadData ()
    {

    }
}
