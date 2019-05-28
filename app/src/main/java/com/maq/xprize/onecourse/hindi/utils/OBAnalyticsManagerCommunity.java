package com.maq.xprize.onecourse.hindi.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.PointF;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;

import org.json.JSONObject;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pedroloureiro on 10/01/2018.
 */

public class OBAnalyticsManagerCommunity extends OBAnalyticsManager
{
    public Map<String, Object> deviceStatusValues;
    public int lastRecordedVolume;
    private Handler reportHandler;
    private Runnable statusRunnable, volumeRunnable;
    
    public OBAnalyticsManagerCommunity (Activity activity)
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
        lastRecordedVolume = -1;
        reportHandler = new Handler();
        statusRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                refreshDeviceStatus();
            }
        };
        volumeRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                reportCurrentVolume();
            }
        };
        deviceStatusValues = new HashMap<>();
        //
        refreshDeviceStatus();
    }

    protected void refreshDeviceStatus()
    {
        if(deviceStatusValues.size() > 0)
            logEvent(OBAnalytics.Event.STATUS, deviceStatusValues);
        //
        long delay = (long)(OBConfigManager.sharedManager.getAnalyticsDeviceStatusRefreshIntervalMinutes() * 60f * 1000);
        //
        reportHandler.postDelayed(statusRunnable, delay);
    }

    @Override
    public void onStart ()
    {

    }

    @Override
    public void onStop ()
    {
    }


    private void logEvent (final String eventName, Map<String, Object> properties)
    {
        if (!OBConfigManager.sharedManager.isAnalyticsEnabled()) return;
        //
        long currentTime = System.currentTimeMillis();
        JSONObject parameters = new JSONObject(properties);
        //
        DBSQL db = null;
        try
        {
            db = new DBSQL(true);
            //
            ContentValues contentValues = new ContentValues();
            contentValues.put("timestamp", currentTime);
            contentValues.put("event", eventName);
            contentValues.put("parameters", parameters.toString());
            db.doInsertOnTable(DBSQL.TABLE_ANALYTICS, contentValues);
            //
            /// IMPORTANT: This is spamming the log --> needs to be improved
            //MainActivity.log("OBAnalyticsManagerCommunity.logEvent: " + currentTime + " " + eventName + " " + parameters.toString());
        }
        catch (Exception e)
        {
            MainActivity.log("OBAnalyticsManagerCommunity.exception caught: " + e.getMessage());
            //e.printStackTrace();
        }
        finally
        {
            if(db != null)
            {
                db.close();
            }
        }
        //
    }


    @Override
    public void deviceTurnedOn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_ON, Boolean.valueOf(true));
        //
        logEvent(OBAnalytics.Event.SCREEN, parameters);
    }


    @Override
    public void deviceTurnedOff ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_ON, Boolean.valueOf(false));
        //
        logEvent(OBAnalytics.Event.SCREEN, parameters);
    }

    @Override
    public void deviceHeadphonesPluggedIn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_HEADPHONES_PLUGGED_IN, Boolean.valueOf(true));
        //
        logEvent(OBAnalytics.Event.HEADPHONES, parameters);
    }

    @Override
    public void deviceHeadphonesUnplugged ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_HEADPHONES_PLUGGED_IN, Boolean.valueOf(false));
        //
        logEvent(OBAnalytics.Event.HEADPHONES, parameters);
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
            deviceStatusValues.put(OBAnalytics.Params.DEVICE_GPS_LATITUDE, Double.valueOf(loc.getLatitude()));
            deviceStatusValues.put(OBAnalytics.Params.DEVICE_GPS_LONGITUDE, Double.valueOf(loc.getLongitude()));
            deviceStatusValues.put(OBAnalytics.Params.DEVICE_GPS_ALTITUDE, Double.valueOf(loc.getAltitude()));
            deviceStatusValues.put(OBAnalytics.Params.DEVICE_GPS_BEARING, Float.valueOf(loc.getBearing()));
            /*
             * Value is now stored in a buffer for regular updates to the database
             *
            parameters.put(OBAnalytics.Params.DEVICE_GPS_LATITUDE, Double.valueOf(loc.getLatitude()));
            parameters.put(OBAnalytics.Params.DEVICE_GPS_LONGITUDE, Double.valueOf(loc.getLongitude()));
            parameters.put(OBAnalytics.Params.DEVICE_GPS_ALTITUDE, Double.valueOf(loc.getAltitude()));
            parameters.put(OBAnalytics.Params.DEVICE_GPS_BEARING, Float.valueOf(loc.getBearing()));
            //
            logEvent(OBAnalytics.Event.DEVICE, parameters);
            */
        }
        else
        {
            MainActivity.log("Last Known Location is NULL. Skipping analytics event");
        }
    }


    @Override
    public void deviceVolumeChanged (float value)
    {
        int newVolume = Integer.valueOf(Math.round(value*100));
        if(newVolume != lastRecordedVolume)
        {
            lastRecordedVolume = newVolume;
            reportHandler.removeCallbacks(volumeRunnable);
            reportHandler.postDelayed(volumeRunnable, 5 * 1000);
        }

    }

    public void reportCurrentVolume()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_VOLUME,lastRecordedVolume);
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }



    @Override
    public void deviceScreenTurnedOn ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_SCREEN_ON, Boolean.valueOf(true));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }


    @Override
    public void deviceScreenTurnedOff ()
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_SCREEN_ON, Boolean.valueOf(false));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
    }


    @Override
    public void deviceMobileSignalStrength (int value)
    {
        deviceStatusValues.put(OBAnalytics.Params.DEVICE_SIGNAL_STRENGTH, Integer.valueOf(value));
        /*
         * Value is now stored in a buffer for regular updates to the database
         *
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_SIGNAL_STRENGTH, Integer.valueOf(value));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
        */
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
        deviceStatusValues.put(OBAnalytics.Params.DEVICE_USED_STORAGE, Long.valueOf(megaBytesUsed));
        deviceStatusValues.put(OBAnalytics.Params.DEVICE_TOTAL_STORAGE, Long.valueOf(megaBytesTotal));
        /*
         * Value is now stored in a buffer for regular updates to the database
         *
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.DEVICE_USED_STORAGE, Long.valueOf(megaBytesUsed));
        parameters.put(OBAnalytics.Params.DEVICE_TOTAL_STORAGE, Long.valueOf(megaBytesTotal));
        //
        logEvent(OBAnalytics.Event.DEVICE, parameters);
        */
    }


    @Override
    public void batteryState (float batteryValue, Boolean pluggedIn, String chargerType)
    {
        deviceStatusValues.put(OBAnalytics.Params.BATTERY_LEVEL, Float.valueOf(batteryValue));
        if (!pluggedIn)
        {
            deviceStatusValues.put(OBAnalytics.Params.BATTERY_CHARGER_STATE, OBAnalytics.Params.BATTERY_CHARGER_STATE_UNPLUGGED);
        }
        else
        {
            deviceStatusValues.put(OBAnalytics.Params.BATTERY_CHARGER_STATE, chargerType);
        }
        /*
         * Value is now stored in a buffer for regular updates to the database
         *
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
        */
    }



    @Override
    public void studyZoneUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        /*
         * DISABLED ON PURPOSE
         *
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.UNIT_START_TIME, Long.valueOf(started));
        parameters.put(OBAnalytics.Params.UNIT_END_TIME, Long.valueOf(finished));
        parameters.put(OBAnalytics.Params.UNIT_REPLAY_AUDIO_COUNT, Integer.valueOf(replayAudioPresses));
        parameters.put(OBAnalytics.Params.UNIT_MODE, OBAnalytics.Params.UNIT_MODE_STUDY_ZONE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
        */
    }


    @Override
    public void enteredScreen(String screen)
    {
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.APP_SCREEN_CHANGE, screen);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
    }


    @Override
    public void communityModeUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        /*
         * DISABLED ON PURPOSE
         *
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.UNIT_START_TIME, Long.valueOf(started));
        parameters.put(OBAnalytics.Params.UNIT_END_TIME, Long.valueOf(finished));
        parameters.put(OBAnalytics.Params.UNIT_REPLAY_AUDIO_COUNT, Integer.valueOf(replayAudioPresses));
        parameters.put(OBAnalytics.Params.UNIT_MODE, OBAnalytics.Params.UNIT_MODE_COMMUNITY_MODE);
        //
        logEvent(OBAnalytics.Event.APP, parameters);
        */
    }



    @Override
    public void playZoneUnitCompleted (String unitID, long started, long finished, float score, int replayAudioPresses)
    {
        /*
         * DISABLED ON PURPOSE
         *
        Map<String, Object> parameters = new HashMap();
        parameters.put(OBAnalytics.Params.UNIT_ID, unitID);
        parameters.put(OBAnalytics.Params.UNIT_SCORE, Float.valueOf(score));
        parameters.put(OBAnalytics.Params.UNIT_START_TIME, Long.valueOf(started));
        parameters.put(OBAnalytics.Params.UNIT_END_TIME, Long.valueOf(finished));
        parameters.put(OBAnalytics.Params.UNIT_REPLAY_AUDIO_COUNT, Integer.valueOf(replayAudioPresses));
        parameters.put(OBAnalytics.Params.UNIT_MODE, OBAnalytics.Params.UNIT_MODE_PLAY_ZONE);
        //
        logEvent(OBAnalytics.Event.UNITS, parameters);
        */
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
        /*
         * DISABLED ON PURPOSE
         *
        Map<String, Object> parameters = new HashMap();
        //
        String creationType = "";
        if (assetType == OC_PlayZoneAsset.ASSET_DOODLE)
            creationType = OBAnalytics.Params.CREATION_TYPE_DOODLE;
        else if (assetType == OC_PlayZoneAsset.ASSET_TEXT)
            creationType = OBAnalytics.Params.CREATION_TYPE_TEXT;
        else if (assetType == OC_PlayZoneAsset.ASSET_VIDEO)
            creationType = OBAnalytics.Params.CREATION_TYPE_VIDEO;
        //
        parameters.put(OBAnalytics.Params.CREATION_TYPE, creationType);
        //
        for (String key : data.keySet())
        {
            parameters.put(key, data.get(key));
        }
        //
        logEvent(OBAnalytics.Event.PLAY_ZONE, parameters);
        */
    }
}
