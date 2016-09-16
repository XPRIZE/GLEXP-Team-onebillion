package org.onebillion.xprz.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.WindowManager;

import org.onebillion.xprz.mainui.MainActivity;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBBrightnessManager
{
    private float maxBrightness;
    private long checkInterval;
    private boolean usesBrightnessAdjustment;
    //
    public static OBBrightnessManager sharedManager;
    private long lastTouchTimeStamp;
    private float lastBrightness;
    private boolean paused, suspended;
    private Runnable brightnessCheckRunnable;

    public OBBrightnessManager ()
    {
        sharedManager = this;
        lastTouchTimeStamp = System.currentTimeMillis();
        lastBrightness = 0f;
        paused = suspended = false;
    }


    public static void setBrightness (final float value)
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                try
                {
                    int valueForSettings = Math.round(value * 255);
                    Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, valueForSettings);
                    Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    WindowManager.LayoutParams layoutpars = MainActivity.mainActivity.getWindow().getAttributes();
                    layoutpars.screenBrightness = value;
                    MainActivity.mainActivity.getWindow().setAttributes(layoutpars);
                    OBSystemsManager.sharedManager.refreshStatus();
                    MainActivity.log("Brightness has been set to: " + value + " --> " + valueForSettings);
                }
                catch (Exception e)
                {
                    // ignore exceptions, permissions may have not been set yet
//                    e.printStackTrace();
                }
            }
        });
    }

    public void runBrightnessCheck ()
    {
        if (suspended) return;
        //
        String maxBrightnessString = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_MAX_BRIGHTNESS);
        maxBrightness = 1.0f;
        if (maxBrightnessString != null) maxBrightness = Float.parseFloat(maxBrightnessString);
        //
        String brightnessInterval = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_BRIGHTNESS_CHECK_INTERVAL);
        checkInterval = 5000;
        if (brightnessInterval != null) checkInterval = Long.parseLong(brightnessInterval);
        //
        String usesBrightnessAdjustmentString = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_USES_BRIGHTNESS_ADJUSTMENT);
        usesBrightnessAdjustment = (usesBrightnessAdjustmentString != null && usesBrightnessAdjustmentString.equals("true"));
        //
        //
        if (brightnessCheckRunnable == null)
        {
            final long interval = checkInterval;
            brightnessCheckRunnable = new Runnable()
            {
                public void run ()
                {
                    if (updateBrightness(true))
                    {
                        OBSystemsManager.sharedManager.mainHandler.removeCallbacks(brightnessCheckRunnable);
                        OBSystemsManager.sharedManager.mainHandler.postDelayed(this, interval);
                    }
                    else
                    {
//                        MainActivity.log("Brightness checker was paused");
                    }
                }
            };
        }
        //
        if (OBSystemsManager.sharedManager.mainHandler != null && brightnessCheckRunnable != null)
        {
            OBSystemsManager.sharedManager.mainHandler.removeCallbacks(brightnessCheckRunnable);
            OBSystemsManager.sharedManager.mainHandler.post(brightnessCheckRunnable);
        }
    }


    public String printStatus ()
    {
        WindowManager.LayoutParams layoutpars = MainActivity.mainActivity.getWindow().getAttributes();
        float brightness = layoutpars.screenBrightness;
        String result = String.format("%.1f%%", brightness * 100);
        return result;
    }


    public void registeredTouchOnScreen ()
    {
        lastTouchTimeStamp = System.currentTimeMillis();
        runBrightnessCheck();
    }


    public boolean updateBrightness (boolean loop)
    {
        if (!usesBrightnessAdjustment)
        {
            setScreenSleepTimeToMax();
            setBrightness(maxBrightness);
            return false;
        }
        if (suspended) return false;
        //
        long currentTimeStamp = System.currentTimeMillis();
        long elapsed = currentTimeStamp - lastTouchTimeStamp;
        float percentage = (elapsed < checkInterval) ? maxBrightness : (elapsed < checkInterval * 2) ? maxBrightness / 2.0f : (elapsed < checkInterval * 3) ? maxBrightness / 4.0f : 0.0f;
        //
//        MainActivity.log("updateBrightness : " + elapsed + " " + percentage);
        //
        if (lastBrightness != percentage)
        {
            lastBrightness = percentage;
            setBrightness(percentage);
            //
            if (percentage == maxBrightness)
            {
                setScreenSleepTimeToMax();
            }
            else if (percentage == 0.0f)
            {
                setScreenSleepTimeToMin();
            }
        }
        return loop && !paused && percentage > 0.0f;
    }


    public void onSuspend ()
    {
//        MainActivity.log("OBBrightnessManager.onSuspend detected");
        suspended = true;
        setBrightness(maxBrightness);
    }


    public void onContinue ()
    {
//        MainActivity.log("OBBrightnessManager.onContinue detected");
        lastTouchTimeStamp = System.currentTimeMillis();
        suspended = false;
        runBrightnessCheck();
    }


    public void onResume ()
    {
//        MainActivity.log("OBBrightnessManager.onResume detected");
        lastTouchTimeStamp = System.currentTimeMillis();
        paused = false;
        MainActivity.log("OBBrightnessManager.onResume --> restoring brightnessCheckRunnable to Handler");
        runBrightnessCheck();
    }


    public void onPause ()
    {
//        MainActivity.log("OBBrightnessManager.onPause detected");
        paused = true;
        if (OBSystemsManager.sharedManager.mainHandler != null && brightnessCheckRunnable != null)
        {
            MainActivity.log("OBBrightnessManager.onPause --> removing brightnessCheckRunnable from Handler");
            OBSystemsManager.sharedManager.mainHandler.removeCallbacks(brightnessCheckRunnable);
        }
    }


    public void onStop ()
    {
//        MainActivity.log("OBBrightnessManager.onStop detected");
    }


    public void setScreenTimeout (int millisecs)
    {
        MainActivity.mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainActivity.mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //
        try
        {
            Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, millisecs);
        }
        catch (Exception e)
        {
            // do nothing, permissions may have not been set yet
        }
    }

    public void setScreenSleepTimeToMax()
    {
        int maxTime = 60000; // 1 minute
        if (MainActivity.mainActivity.isDebugMode()) maxTime = Integer.MAX_VALUE;
        setScreenTimeout(maxTime);
    }


    public void setScreenSleepTimeToMin ()
    {
        setScreenTimeout(1);
    }

}
