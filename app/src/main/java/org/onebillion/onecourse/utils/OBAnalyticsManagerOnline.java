package org.onebillion.onecourse.utils;

import android.app.Activity;
import android.graphics.PointF;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.oc_playzone.OC_PlayZoneAsset;

import java.util.HashMap;
import java.util.Map;

import com.parse.Parse;
import com.parse.ParseObject;

import android.location.Location;
import android.os.Environment;
import android.os.StatFs;


/**
 * Created by pedroloureiro on 29/11/2017.
 */

public class OBAnalyticsManagerOnline extends OBAnalyticsManager
{

    /*
     * To open the dashboard
     * parse-dashboard --appId "org.onebillion.onecourse.kenya" --masterKey "4asterix" --serverURL "http://onecourse-kenya.herokuapp.com/parse" --appName "onecourse-kenya"
     *
     */

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
        Parse.enableLocalDatastore(activity.getBaseContext());
        Parse.initialize(new Parse.Configuration.Builder(activity)
                .applicationId("org.onebillion.onecourse.kenya")
                .server("http://onecourse-kenya.herokuapp.com/parse/")
                .build()
        );
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
    public void uploadData()
    {
    }


    private void logEvent(String eventName, Map<String, Object> properties)
    {
        if (!OBConfigManager.sharedManager.isAnalyticsEnabled()) return;
        //
        MainActivity.log("OBAnalyticsManagerOnline.logEvent: " + eventName + " " + properties.toString());
        //
        ParseObject parseObject = new ParseObject(eventName);
        for (String key : properties.keySet())
        {
            parseObject.put(key, properties.get(key));
        }
        //
        parseObject.put(OBAnalytics.Params.DEVICE_UUID, OBSystemsManager.sharedManager.device_getUUID());
        parseObject.saveInBackground();
    }


    @Override
    public void deviceTurnedOn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_STATE, OBAnalytics.Params.DEVICE_STATE_ON);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }


    @Override
    public void deviceTurnedOff ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_STATE, OBAnalytics.Params.DEVICE_STATE_OFF);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceHeadphonesPluggedIn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_HEADPHONES_STATE, OBAnalytics.Params.DEVICE_HEADPHONES_STATE_PLUGGED);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }

    @Override
    public void deviceHeadphonesUnplugged ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_HEADPHONES_STATE, OBAnalytics.Params.DEVICE_HEADPHONES_STATE_UNPLUGGED);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }


    @Override
    public void touchMadeInUnit (String unitID, PointF startLoc, long started, PointF endLoc, long finished)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.TOUCH_UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.TOUCH_START_LOCATION, startLoc);
        parameters.put(OBAnalytics.Params.TOUCH_START_TIME, Long.valueOf(started));
        parameters.put(OBAnalytics.Params.TOUCH_END_LOCATION, endLoc);
        parameters.put(OBAnalytics.Params.TOUCH_END_TIME, Long.valueOf(finished));
        //
        logEvent(OBAnalytics.Event.TOUCH, parameters);
    }



    @Override
    public void deviceGpsLocation ()
    {
        Map<String, Object> parameters = new HashMap();
        //
        Location loc = OBLocationManager.sharedManager.getLastKnownLocation();
        if (loc != null)
        {
            parameters.put(OBAnalytics.Params.DEVICE_GPS_LATITUDE, Double.valueOf(loc.getLatitude()));
            parameters.put(OBAnalytics.Params.DEVICE_GPS_LONGITUDE, Double.valueOf(loc.getLongitude()));
            parameters.put(OBAnalytics.Params.DEVICE_GPS_ALTITUDE, Double.valueOf(loc.getAltitude()));
            parameters.put(OBAnalytics.Params.DEVICE_GPS_BEARING, Float.valueOf(loc.getBearing()));
            //
            logEvent(OBAnalytics.Event.DEVICE, parameters);
        }
        else
        {
            MainActivity.log("Last Known Location is NULL. Skipping analytics event");
        }
    }



    @Override
    public void deviceVolumeChanged (float value)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_VOLUME, Float.valueOf(value));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }



    @Override
    public void deviceScreenTurnedOn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_SCREEN_STATE, OBAnalytics.Params.DEVICE_SCREEN_STATE_ON);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }



    @Override
    public void deviceScreenTurnedOff ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_SCREEN_STATE, OBAnalytics.Params.DEVICE_SCREEN_STATE_OFF);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }



    @Override
    public void deviceMobileSignalStrength (int value)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_SIGNAL_STRENGTH, Integer.valueOf(value));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }



    @Override
    public void deviceStorageUse ()
    {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = stat.getAvailableBytes();
        long bytesTotal = stat.getTotalBytes();
        //
        long megaBytesAvailable = bytesAvailable / (1024 * 1024);
        long megaBytesTotal = bytesTotal / (1024 * 1024);
        long megaBytesUsed = megaBytesTotal - megaBytesAvailable;
        //
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_USED_STORAGE, Long.valueOf(megaBytesUsed));
        parameters.put(OBAnalytics.Params.DEVICE_TOTAL_STORAGE, Long.valueOf(megaBytesTotal));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }



    @Override
    public void batteryState(float batteryValue, Boolean pluggedIn, String chargerType)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.BATTERY_LEVEL, Float.valueOf(batteryValue));
        if (!pluggedIn)
        {
            parameters.put(OBAnalytics.Params.BATTERY_CHARGER_STATE, OBAnalytics.Params.BATTERY_CHARGER_STATE_UNPLUGGED);
        }
        else
        {
            parameters.put(OBAnalytics.Params.BATTERY_CHARGER_STATE, chargerType);
        }
        //
        logEvent(OBAnalytics.Event.BATTERY, parameters);
    }




    @Override
    public void studyZoneStartedNewDay ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.APP_MODE_CHANGE, OBAnalytics.Params.APP_STUDY_ZONE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
    }



    @Override
    public void studyZoneUnitCompleted(String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.UNIT_START_TIME,Long.valueOf(started));
        parameters.put(OBAnalytics.Params.UNIT_END_TIME,Long.valueOf(finished));
        parameters.put(OBAnalytics.Params.UNIT_REPLAY_AUDIO_COUNT, Integer.valueOf(replayAudioPresses));
        parameters.put(OBAnalytics.Params.UNIT_MODE, OBAnalytics.Params.UNIT_MODE_STUDY_ZONE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
    }



    @Override
    public void communityModeEntered ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.APP_MODE_CHANGE, OBAnalytics.Params.APP_COMMUNITY_MODE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
    }



    @Override
    public void communityModeUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.UNIT_START_TIME,Long.valueOf(started));
        parameters.put(OBAnalytics.Params.UNIT_END_TIME,Long.valueOf(finished));
        parameters.put(OBAnalytics.Params.UNIT_REPLAY_AUDIO_COUNT, Integer.valueOf(replayAudioPresses));
        parameters.put(OBAnalytics.Params.UNIT_MODE, OBAnalytics.Params.UNIT_MODE_COMMUNITY_MODE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
    }


    @Override
    public void playZoneEntered ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.APP_MODE_CHANGE, OBAnalytics.Params.APP_PLAY_ZONE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
    }



    @Override
    public void playZoneUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.UNIT_START_TIME,Long.valueOf(started));
        parameters.put(OBAnalytics.Params.UNIT_END_TIME,Long.valueOf(finished));
        parameters.put(OBAnalytics.Params.UNIT_REPLAY_AUDIO_COUNT, Integer.valueOf(replayAudioPresses));
        parameters.put(OBAnalytics.Params.UNIT_MODE, OBAnalytics.Params.UNIT_MODE_PLAY_ZONE);
        //
        logEvent(OBAnalytics.Event.UNITS, parameters);
    }


    @Override
    public void playZoneVideoWatched (String videoID)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.PLAY_ZONE_VIDEO_ID, videoID);
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }

    @Override
    public void playZoneAssetCreated (int assetType, Map<String, String> data)
    {
        Map<String, Object> parameters = new HashMap();
        //
        String creationType = "";
        if (assetType == OC_PlayZoneAsset.ASSET_DOODLE) creationType = OBAnalytics.Params.CREATION_TYPE_DOODLE;
        else if (assetType == OC_PlayZoneAsset.ASSET_TEXT) creationType = OBAnalytics.Params.CREATION_TYPE_TEXT;
        else if (assetType == OC_PlayZoneAsset.ASSET_VIDEO) creationType = OBAnalytics.Params.CREATION_TYPE_VIDEO;
        //
        parameters.put(OBAnalytics.Params.CREATION_TYPE, creationType);
        //
        for (String key : data.keySet())
        {
            parameters.put(key, data.get(key));
        }
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
    }



    @Override
    public void nightModeEntered ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.APP_MODE_CHANGE, OBAnalytics.Params.APP_NIGHT_MODE);
    }
}
