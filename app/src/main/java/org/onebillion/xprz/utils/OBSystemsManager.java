package org.onebillion.xprz.utils;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.receivers.OBBatteryReceiver;
import org.onebillion.xprz.receivers.OBSettingsContentObserver;

/**
 * Created by pedroloureiro on 31/08/16.
 */
public class OBSystemsManager
{
    public static OBSystemsManager sharedManager;

    private OBBatteryReceiver batteryReceiver;
    private OBSettingsContentObserver settingsContentObserver;
    private OBBrightnessManager brightnessManager;
    private OBExpansionManager expansionManager;
    private OBConnectionManager connectionManager;


    public OBSystemsManager()
    {
        batteryReceiver = new OBBatteryReceiver();
        settingsContentObserver = new OBSettingsContentObserver(MainActivity.mainActivity, new Handler());
        brightnessManager = new OBBrightnessManager();
        expansionManager = new OBExpansionManager();
        connectionManager = new OBConnectionManager();
        //
        sharedManager = this;
    }

    public void runChecks()
    {
        connectionManager.sharedManager.checkForConnection();
        //
        OBSQLiteHelper.getSqlHelper().runMaintenance();
    }


    public void onResume()
    {
        if (batteryReceiver != null)
        {
            MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        if (settingsContentObserver != null)
        {
            settingsContentObserver.onResume();
        }
        if (expansionManager != null)
        {
            MainActivity.mainActivity.registerReceiver(OBExpansionManager.sharedManager.downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        OBBrightnessManager.sharedManager.onResume();
    }


    public void onPause()
    {
        if (batteryReceiver != null)
        {
            MainActivity.mainActivity.unregisterReceiver(batteryReceiver);
        }
        if (settingsContentObserver != null)
        {
            settingsContentObserver.onPause();
        }
        if (OBExpansionManager.sharedManager.downloadCompleteReceiver != null)
        {
            MainActivity.mainActivity.unregisterReceiver(OBExpansionManager.sharedManager.downloadCompleteReceiver);
        }
        OBBrightnessManager.sharedManager.onResume();
    }


    public void onStop()
    {
        OBBrightnessManager.sharedManager.onStop();
    }



    public String printBatteryStatus()
    {
        return batteryReceiver.printStatus();
    }


    public void setBatteryStatusLabel(OBLabel label)
    {
        batteryReceiver.statusLabel = label;
    }
}
