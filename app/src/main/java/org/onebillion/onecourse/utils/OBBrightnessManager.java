package org.onebillion.onecourse.utils;

import android.provider.Settings;
import android.view.WindowManager;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBBrightnessManager
{
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
//                    MainActivity.log("setBrightness (has write settings permission --> " + OBSystemsManager.sharedManager.hasWriteSettingsPermission() + ")");
                    int valueForSettings = Math.round(value * 255);
                    if (OBSystemsManager.sharedManager.hasWriteSettingsPermission())
                    {
                        Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, valueForSettings);
                        Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                        WindowManager.LayoutParams layoutpars = MainActivity.mainActivity.getWindow().getAttributes();
                        layoutpars.screenBrightness = value;
                        MainActivity.mainActivity.getWindow().setAttributes(layoutpars);
                        OBSystemsManager.sharedManager.refreshStatus();
                        MainActivity.log("Brightness has been set to: " + value + " --> " + valueForSettings);
                    }
                }
                catch (Exception e)
                {
//                    e.printStackTrace();
                }
            }
        });
    }

    public float maxBrightness ()
    {
        float value = OBConfigManager.sharedManager.getBatteryMaxBrightnessForLevel(OBSystemsManager.sharedManager.getBatterySettingKeyForCurrentLevel());
        return value;
    }


    public long getBrightnessCheckInterval ()
    {
        return OBConfigManager.sharedManager.getBrightnessCheckIntervalInSeconds() * 1000;
    }


    public void runBrightnessCheck ()
    {
        if (suspended) return;
        //
        if (!OBConfigManager.sharedManager.isBrightnessManagerEnabled())
        {
            disableBrightnessAdjustment();
        }
        else if (OBSystemsManager.sharedManager.getMainHandler() != null)
        {
            //
            if (brightnessCheckRunnable == null)
            {
                final long interval = getBrightnessCheckInterval();
                //
                MainActivity.log("OBBrightnessManager.created brightnessCheckRunnable with " + interval + "ms interval");
                //
                brightnessCheckRunnable = new Runnable()
                {
                    public void run ()
                    {
                        try
                        {
                            if (updateBrightness(true))
                            {
                                MainActivity.log("OBBrightnessManager:runBrightnessCheck:posting brightnessCheckRunnable delayed [" + interval + "]");
                                OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(brightnessCheckRunnable);
                                OBSystemsManager.sharedManager.getMainHandler().postDelayed(this, interval);
                            }
                            else
                            {
//                        MainActivity.log("Brightness checker was paused");
                            }
                        }
                        catch (Exception e)
                        {
                            MainActivity.log("OBBrightnessManager.brightnessCheckRunnable exception caught");
                            e.printStackTrace();
                        }
                    }
                };
            }
            //
            MainActivity.log("OBBrightnessManager:runBrightnessCheck:posting brightnessCheckRunnable");
            OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(brightnessCheckRunnable);
            OBSystemsManager.sharedManager.getMainHandler().post(brightnessCheckRunnable);
        }
    }


    public String printStatus ()
    {
        WindowManager.LayoutParams layoutpars = MainActivity.mainActivity.getWindow().getAttributes();
        float brightness = Math.abs(layoutpars.screenBrightness);
        String result = String.format("%.1f%%", brightness * 100);
        return result;
    }


    public void registeredTouchOnScreen (boolean forceRefresh)
    {
        if (OBConfigManager.sharedManager.isBrightnessManagerEnabled())
        {
            long interval = getBrightnessCheckInterval();
            //
            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - lastTouchTimeStamp;
            //
            lastTouchTimeStamp = System.currentTimeMillis();
            //
            if (elapsed > interval || forceRefresh)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        runBrightnessCheck();
                    }
                });
            }
        }
    }


    public boolean updateBrightness (boolean loop)
    {
        if (!OBConfigManager.sharedManager.isBrightnessManagerEnabled())
        {
            disableBrightnessAdjustment();
            return false;
        }
        if (suspended) return false;
        //
        if (MainActivity.mainViewController == null) return false;
        //
        if (MainActivity.mainViewController.topController() == null) return false;
        //
        int status = MainActivity.mainViewController.topController().status();
        if (status == OBSectionController.STATUS_DOING_DEMO || status == OBSectionController.STATUS_CHECKING || status == OBSectionController.STATUS_DRAGGING)
        {
            return loop && !paused;
        }
        //
        if (OBAudioManager.audioManager.isPlaying())
        {
            long duration = (long) (OBAudioManager.audioManager.duration() * 1000);
            MainActivity.log("OBBrightnessManager.brightnessCheckRunnable.audio is playing file with " + duration + "ms. ignoring brightness update");
            //
            MainActivity.log("OBBrightnessManager:runBrightnessCheck:posting brightnessCheckRunnable delayed [" + duration + "]");
            OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(brightnessCheckRunnable);
            OBSystemsManager.sharedManager.getMainHandler().postDelayed(brightnessCheckRunnable, duration);
            //
            return loop && !paused;
        }
        //
        long checkIntervalMS = OBConfigManager.sharedManager.getBrightnessCheckIntervalInSeconds() * 1000;
        long currentTimeStamp = System.currentTimeMillis();
        long elapsed = currentTimeStamp - lastTouchTimeStamp;
        String batteryLevelKey = OBSystemsManager.sharedManager.getBatterySettingKeyForCurrentLevel();
        float maxBrightness = OBConfigManager.sharedManager.getBatteryMaxBrightnessForLevel(batteryLevelKey);
        float minBrightness = OBConfigManager.sharedManager.getBrightnessTurnOffThresholdValue();
        //
        float percentage = maxBrightness * Math.min(checkIntervalMS / (float) (elapsed * 2), 1.0f);
        //float percentage = (elapsed < checkIntervalMS) ? maxBrightness : (elapsed < checkIntervalMS * 2) ? maxBrightness / 2.0f : (elapsed < checkIntervalMS * 3) ? maxBrightness / 4.0f : 0.0f;
        //
        MainActivity.log("OBBrightnessManager.updateBrightness : " + Math.round(elapsed / 1000) + "s -->" + Math.round(percentage * 100) + "%");
        //
        if (lastBrightness != percentage)
        {
            lastBrightness = percentage;
            setBrightness(percentage);
            //
            if (percentage <= minBrightness)
            {
                MainActivity.log("OBBrightnessManager.updateBrightness: Threshold reached. Turning off screen");
                setScreenSleepTimeToMin();
            }
            else
            {
                setScreenSleepTimeToMax();
            }
        }
        return loop && !paused && percentage > minBrightness;
    }


    public void onSuspend ()
    {
        MainActivity.log("OBBrightnessManager.onSuspend detected");
        suspended = true;
        setBrightness(maxBrightness());
        setScreenSleepTimeToMax();
    }


    public void onContinue ()
    {
        MainActivity.log("OBBrightnessManager.onContinue detected");
        suspended = false;
        //
        registeredTouchOnScreen(true);
    }


    public void onResume ()
    {
        updateBrightness(false);
        //
        MainActivity.log("OBBrightnessManager.onResume detected");
        paused = false;
        registeredTouchOnScreen(true);
    }


    public void onPause ()
    {
//        MainActivity.log("OBBrightnessManager.onPause detected");
        paused = true;
        if (OBSystemsManager.sharedManager.getMainHandler() != null && brightnessCheckRunnable != null)
        {
            MainActivity.log("OBBrightnessManager.onPause --> removing brightnessCheckRunnable from Handler");
            OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(brightnessCheckRunnable);
        }
    }


    public void onStop ()
    {
//        MainActivity.log("OBBrightnessManager.onStop detected");
    }


    public void disableBrightnessAdjustment ()
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                MainActivity.log("OBBrightnessManager.disabling brightness adjustment");
                MainActivity.mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                MainActivity.mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                if (OBSystemsManager.sharedManager.hasWriteSettingsPermission())
                {
                    int valueForSettings = Math.round(maxBrightness() * 255);
                    Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, valueForSettings);
                    Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
                    Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, getScreenMaxTimeout());
                    WindowManager.LayoutParams layoutpars = MainActivity.mainActivity.getWindow().getAttributes();
                    layoutpars.screenBrightness = maxBrightness();
                    MainActivity.mainActivity.getWindow().setAttributes(layoutpars);
                    OBSystemsManager.sharedManager.refreshStatus();
                    suspended = true;
                }
                else
                {
                    MainActivity.log("OBBrightnessManager.does not have write settings permission. unable to revert changes for brightness management");
                }
            }
        });
    }


    public void setScreenTimeout (int millisecs)
    {
        MainActivity.log("OBBrightnessManager.setScreenTimeout: " + millisecs);
        try
        {
            if (OBSystemsManager.sharedManager.hasWriteSettingsPermission())
            {
                Settings.System.putInt(MainActivity.mainActivity.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, millisecs);
            }
            else
            {
                MainActivity.log("OBBrightnessManager.setScreenTimeout: Application does not have write settings permission");
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OBBrightnessManager.setScreenTimeout: exception caught");
            e.printStackTrace();
        }
    }

    public void setScreenSleepTimeToMax ()
    {
        MainActivity.log("OBBrightnessManager.setScreenSleepTimeToMax");
        //
        MainActivity.mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainActivity.mainActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //
        setScreenTimeout(getScreenMaxTimeout());
    }


    public void setScreenSleepTimeToMin ()
    {
        MainActivity.log("OBBrightnessManager.setScreenSleepTimeToMin");
        //
        MainActivity.mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainActivity.mainActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        //
        setScreenTimeout(1);
    }


    /**
     *
     * @return milliseconds of inactivity to turn the screen off
     */
    public int getScreenMaxTimeout ()
    {
        int secondsToScreenLock = OBConfigManager.sharedManager.getBatteryMaxScreenTimeoutInSecondsForLevel(OBSystemsManager.sharedManager.getBatterySettingKeyForCurrentLevel());
        return secondsToScreenLock * 1000;
    }

}
