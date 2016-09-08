package org.onebillion.xprz.receivers;

import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;

import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.utils.OBSystemsManager;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBSettingsContentObserver extends ContentObserver
{
    private int currentVolume;

    public OBSettingsContentObserver(Context context, Handler handler)
    {
        super(handler);
    }

    @Override
    public boolean deliverSelfNotifications()
    {
        return false;
    }

    @Override
    public void onChange(boolean selfChange)
    {
        String minVolume = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_MIN_AUDIO_VOLUME);
        if (minVolume != null)
        {

            float minVolumePercentage = Float.parseFloat(minVolume) / (float) 100;
            AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
            currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int minVolumeLimit = Math.round(maxVolume * minVolumePercentage);
            if (currentVolume < minVolumeLimit)
            {
                MainActivity.log("Current Volume (" + currentVolume + ") lower than permitted minimum (" + minVolumeLimit + "). Resetting value");
                am.setStreamVolume(AudioManager.STREAM_MUSIC, minVolumeLimit, 0);
            }
            OBSystemsManager.sharedManager.refreshStatus();
        }
    }


    public String printVolumeStatus()
    {
        AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        //
        return Math.round((currentVolume / (float) maxVolume) * 100.0f) + "%";
    }


    public static void setDefaultAudioVolume()
    {
        String volume = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_DEFAULT_AUDIO_VOLUME);
        if (volume != null)
        {
            float volumePercentage = Float.parseFloat(volume) / (float) 100;
            AudioManager am = (AudioManager) MainActivity.mainActivity.getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, Math.round(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volumePercentage), 0);
        }
    }



    public void onResume()
    {
        MainActivity.mainActivity.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, this);
    }



    public void onPause()
    {
        MainActivity.mainActivity.getContentResolver().unregisterContentObserver(this);
    }
}
