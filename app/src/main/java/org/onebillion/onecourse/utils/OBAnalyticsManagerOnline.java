package org.onebillion.onecourse.utils;

import android.app.Activity;
import android.graphics.PointF;

import org.onebillion.onecourse.mainui.MainActivity;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by pedroloureiro on 29/11/2017.
 */

public class OBAnalyticsManagerOnline extends OBAnalyticsManager
{

    public OBAnalyticsManagerOnline (Activity activity)
    {
        super();
        //
        if (OBConfigManager.sharedManager.isAnalyticsEnabled())
        {
            startupAnalytics(activity);
        }
    }



    @Override
    protected void startupAnalytics (Activity activity)
    {
        super.startupAnalytics(activity);
        //

    }


    @Override
    public void onStart()
    {

    }

    @Override
    public void onStop()
    {

    }


    private void logEvent(String eventName, Map<String, Object> properties)
    {
        if (!OBConfigManager.sharedManager.isAnalyticsEnabled()) return;
        //
        MainActivity.log("OBAnalyticsManagerOnline.logEvent: " + eventName + " " + properties.toString());
        //

    }



    @Override
    public void deviceGpsLocation ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.GPS_COORDINATES, OBLocationManager.sharedManager.getLastKnownLocation().toString());
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceVolumeChanged (float value)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.VOLUME, Float.valueOf(value));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceScreenTurnedOn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.SCREEN_STATE, "on");
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceScreenTurnedOff ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.SCREEN_STATE, "off");
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceMobileSignalStrength (float value)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.SIGNAL_STRENGTH, Float.valueOf(value));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceStorageUse (long used, long total)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.USED_STORAGE, Long.valueOf(used));
        parameters.put(OBAnalytics.Params.TOTAL_STORAGE, Long.valueOf(total));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void touchMade (PointF coordinates)
    {
        // do nothing, Appsee takes care of it
    }

    @Override
    public void batteryState(float batteryValue, Boolean chargerPluggedIn)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.BATTERY_LEVEL, Float.valueOf(batteryValue));
        parameters.put(OBAnalytics.Params.CHARGER_STATE, (chargerPluggedIn) ? "plugged" : "unplugged");
        //
        logEvent(OBAnalytics.Event.BATTERY, parameters);
    }

    @Override
    public void studyZoneUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        float elapsedSeconds = (finished - started) / (float) 1000;
        //
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.STUDY_ZONE_UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.STUDY_ZONE_UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.STUDY_ZONE_UNIT_ELAPSED,Float.valueOf(elapsedSeconds));
        parameters.put(OBAnalytics.Params.STUDY_ZONE_REPLAY_AUDIO, Integer.valueOf(replayAudioPresses));
        //
        logEvent(OBAnalytics.Event.STUDY_ZONE, parameters);
    }


    @Override
    public void playZoneVideoWatched (String videoID)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.VIDEO_ID, videoID);
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }

    @Override
    public void playZoneActivityCompleted(String activityID, long started, long finished)
    {
        float elapsedSeconds = (finished - started) / (float) 1000;
        //
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.PLAY_ZONE_ACTIVITY_ID, activityID);
        parameters.put(OBAnalytics.Params.PLAY_ZONE_ELAPSED, Float.valueOf(elapsedSeconds));
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }


    @Override
    public void playZoneCreationsVideoAdded ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.PLAY_ZONE_CREATION_TYPE, "video");
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }

    @Override
    public void playZoneCreationsDoodleAdded ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.PLAY_ZONE_CREATION_TYPE, "doodle");
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }

    @Override
    public void playZoneCreationsTextAdded ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.PLAY_ZONE_CREATION_TYPE, "text");
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }

    @Override
    public void nightModeTriggered ()
    {
        Map<String, Object> parameters = new HashMap();
        //
        logEvent(OBAnalytics.Event.NIGHT_MODE, parameters);
    }
}
