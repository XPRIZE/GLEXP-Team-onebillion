package org.onebillion.xprz.utils;

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
    private boolean paused;

    public OBBrightnessManager ()
    {
        sharedManager = this;
        lastTimeStamp = lastTouchTimeStamp = System.currentTimeMillis();
        lastBrightness = 0f;
        paused = false;
        //
        sharedManager.run();
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
                    MainActivity.mainActivity.log("Brightness has been set to: " + valueForSettings);
                }
                catch (Exception e)
                {
                    // ignore exceptions, permissions may have not been set yet
                    e.printStackTrace();
                }
            }
        });
    }


    public void run()
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
        long currentTimeStamp = System.currentTimeMillis();
        long elapsed = currentTimeStamp - lastTouchTimeStamp;
        float percentage = (elapsed < 5000) ? 1.0f : (elapsed < 10000) ? 0.5f : (elapsed < 15000) ? 0.25f : 0.0f;
        //
        MainActivity.mainActivity.log("updateBrightness : " + elapsed + " " + percentage);
        //
        if (lastBrightness != percentage)
        {
            lastBrightness = percentage;
            this.setBrightness(percentage);
            //
            if (percentage == 1.0f)
            {
                MainActivity.mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else if (percentage == 0.0f)
            {
                MainActivity.mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                //
                Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1);
            }
        }
        if (loop && !paused) run();
    }

    public void onResume()
    {
        paused = false;
        run();
    }

    public void onPause()
    {
        paused = true;
    }

}
