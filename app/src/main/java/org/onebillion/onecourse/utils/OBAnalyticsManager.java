package org.onebillion.onecourse.utils;

import android.app.Activity;
import android.graphics.PointF;


import org.onebillion.onecourse.mainui.MainActivity;

import java.lang.reflect.Constructor;

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
    public void studyZoneStartedNewDay ()
    {

    }

    @Override
    public void communityModeEntered ()
    {

    }

    @Override
    public void communityModeUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {

    }

    @Override
    public void playZoneEntered ()
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
            Class aClass = Class.forName("org.onebillion.onecourse.utils." + analyticsClassName);
            Constructor<?> cons = aClass.getConstructor(Activity.class);
            sharedManager = (OBAnalyticsManager) cons.newInstance(activity);
            //
            MainActivity.log("AnalyticsManager has been initialised with class name %s", analyticsClassName);
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
    public void deviceMobileSignalStrength (float value)
    {

    }

    @Override
    public void deviceStorageUse (long used, long total)
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
    public void playZoneCreationsVideoAdded ()
    {

    }

    @Override
    public void playZoneCreationsDoodleAdded ()
    {

    }

    @Override
    public void playZoneCreationsTextAdded ()
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
    public void nightModeEntered ()
    {

    }
}
