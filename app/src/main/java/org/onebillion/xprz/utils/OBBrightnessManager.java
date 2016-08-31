package org.onebillion.xprz.utils;

import android.content.Context;
import android.os.AsyncTask;
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

    public static OBBrightnessManager sharedManager;
    private long lastTouchTimeStamp, lastTimeStamp;
    private float lastBrightness;
    private boolean paused, suspended;

    public OBBrightnessManager ()
    {
        sharedManager = this;
        lastTimeStamp = lastTouchTimeStamp = System.currentTimeMillis();
        lastBrightness = 0f;
        paused = suspended = false;
        runBrightnessCheck();
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
//                    MainActivity.mainActivity.log("Brightness has been set to: " + valueForSettings);
                }
                catch (Exception e)
                {
                    // ignore exceptions, permissions may have not been set yet
                    e.printStackTrace();
                }
            }
        });
    }


    public void runBrightnessCheck()
    {
        OBUtils.runOnOtherThreadDelayed(5.0f, new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                updateBrightness(true);
            }
        });
    }



    public void registeredTouchOnScreen ()
    {
        lastTouchTimeStamp = System.currentTimeMillis();
        updateBrightness(false);
    }

    public void updateBrightness (boolean loop)
    {
        if (suspended) return;
        //
        long currentTimeStamp = System.currentTimeMillis();
        long elapsed = currentTimeStamp - lastTouchTimeStamp;
        float percentage = (elapsed < 5000) ? 1.0f : (elapsed < 10000) ? 0.5f : (elapsed < 15000) ? 0.25f : 0.0f;
        //
//        MainActivity.mainActivity.log("updateBrightness : " + elapsed + " " + percentage);
        //
        if (lastBrightness != percentage)
        {
            lastBrightness = percentage;
            setBrightness(percentage);
            //
            if (percentage == 0.0f)
            {
//                MainActivity.mainActivity.log("Brightness manager killing screen");
                MainActivity.mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //
                Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1);
            }
        }
        if (loop && !paused) runBrightnessCheck();
    }

    public void onSuspend()
    {
//        MainActivity.mainActivity.log("OBBrightnessManager.onSuspend called");
        suspended = true;
        setBrightness(1.0f);
        MainActivity.mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onContinue()
    {
//        MainActivity.mainActivity.log("OBBrightnessManager.onContinue called");
        lastTimeStamp = lastTouchTimeStamp = System.currentTimeMillis();
        suspended = false;
        updateBrightness(true);
    }

    public void onResume()
    {
//        MainActivity.mainActivity.log("OBBrightnessManager.onResume called");
        lastTimeStamp = lastTouchTimeStamp = System.currentTimeMillis();
        paused = false;
        updateBrightness(true);
    }

    public void onPause()
    {
        paused = true;
    }

    public void onStop()
    {
        int maxTime = 60000; // 1 minute
        if (MainActivity.mainActivity.isDebugMode()) maxTime = Integer.MAX_VALUE;
        Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, maxTime);
    }

}
