package com.maq.xprize.onecourse.receivers;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBAnalyticsManager;
import com.maq.xprize.onecourse.utils.OBConfigManager;
import com.maq.xprize.onecourse.utils.OBSystemsManager;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBSettingsContentObserver extends ContentObserver
{
    private int currentVolume;

    public OBSettingsContentObserver (Context context, Handler handler)
    {
        super(handler);
    }

    @Override
    public boolean deliverSelfNotifications ()
    {
        return false;
    }


    public boolean allowsLowerVolume ()
    {
        if (MainActivity.mainActivity == null) return false;
        //
        float minVolume = OBConfigManager.sharedManager.getMinimumAudioVolumePercentage() / (float) 100;
        // if value is not present in the config, the min volume will be -.01
        AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int minVolumeLimit = Math.round(maxVolume * minVolume);
        return currentVolume > minVolumeLimit;
    }

    @Override
    public void onChange (boolean selfChange)
    {
        if (MainActivity.mainActivity == null) return;
        //
        float minVolume = OBConfigManager.sharedManager.getMinimumAudioVolumePercentage() / (float) 100;
        // if value is not present in the config, the min volume will be -.01
        AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int minVolumeLimit = Math.round(maxVolume * minVolume);
        if (currentVolume < minVolumeLimit)
        {
            MainActivity.log("Current Volume (" + currentVolume + ") lower than permitted minimum (" + minVolumeLimit + "). Resetting value");
            am.setStreamVolume(AudioManager.STREAM_MUSIC, minVolumeLimit, 0);
            currentVolume = minVolumeLimit;
        }
        OBAnalyticsManager.sharedManager.deviceVolumeChanged(currentVolume / (float) maxVolume);
        OBSystemsManager.sharedManager.refreshStatus();
    }


    public String printVolumeStatus ()
    {
        if (MainActivity.mainActivity == null) return "";
        //
        AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        //
        return Math.round((currentVolume / (float) maxVolume) * 100.0f) + "%";
    }


    public static void setDefaultAudioVolume ()
    {
        if (MainActivity.mainActivity == null) return;
        //
        float volumePercentage = OBConfigManager.sharedManager.getDefaultAudioVolumePercentage() / (float) 100;
        if (volumePercentage < 0) return;
        //
        AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercentage), 0);
    }


    public void onResume ()
    {
        if (MainActivity.mainActivity == null) return;
        //
        MainActivity.mainActivity.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this);
    }


    public void onPause ()
    {
        if (MainActivity.mainActivity == null) return;
        //
        MainActivity.mainActivity.getContentResolver().unregisterContentObserver(this);
    }
}
