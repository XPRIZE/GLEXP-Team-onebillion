package org.onebillion.onecourse.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OCM_FatController;

/**
 * Created by pedroloureiro on 25/08/16.
 */
public class OBBatteryReceiver extends BroadcastReceiver
{
    public boolean usbCharge, acCharge, isCharging;
    int batteryScale, batteryLevel;

    public OBBatteryReceiver()
    {
        usbCharge = false;
        acCharge = false;
        isCharging = false;
        batteryScale = -1;
        batteryLevel= -1;
    }


    @Override
    public void onReceive (Context context, Intent intent)
    {
        if (MainActivity.mainActivity == null) return;
        //
        Boolean isNowCharging = false;
        //
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int chargePlug = -1;
        if(status != -1)
        {
            chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            //
            // BatteryManager.BATTERY_STATUS_CHARGING cannot be trusted. It happens on occasion, but it's thrown while the tablet is not charging
            isNowCharging = status == BatteryManager.BATTERY_STATUS_CHARGING && chargePlug > 0;
            //
            usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
            //
            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            //
            MainActivity.log("OBBatteryReceiver.onReceive: battery level : " + batteryLevel + ", battery scale: " + batteryScale + " " + (isNowCharging ? "CHARGING " : ""));
        }
        //
        MainActivity.log("OBBatteryReceiver.onReceive: " + printStatus());
        //
        if(MainActivity.mainActivity != null)
        {
            MainActivity.mainActivity.onBatteryStatusReceived(getBatteryLevel(),cablePluggedIn());
        }
        //
        if (OBSystemsManager.sharedManager != null)
        {
            OBSystemsManager.sharedManager.refreshStatus();
            //
            MainActivity.log("OBBatteryReceiver.onReceive: chargePlug flag value: " + chargePlug + ", battery status: " + status);
            //
            Boolean actionIsRequired = !isCharging && isNowCharging;
            isCharging = isNowCharging;
            //
            if (actionIsRequired)
            {
                MainActivity.log("OBBatteryReceiver.onReceive: it is now charging and/or plugged in. Action is required");
                //
                if (OBConfigManager.sharedManager.isBackupWhenChargingEnabled())
                {
                    if (OBSystemsManager.sharedManager.isBackupRequired())
                    {
                        MainActivity.log("OBBatteryReceiver.onReceive: Backup is required. Synchronising time and data.");
                        OBSystemsManager.sharedManager.connectToWifiAndSynchronizeTimeAndData();
                    }
                    else
                    {
                        MainActivity.log("OBBatteryReceiver.onReceive: Backup is NOT required. Synchronising time");
                        OBSystemsManager.sharedManager.connectToWifiAndSynchronizeTime();
                    }
                }
                else
                {
                    MainActivity.log("OBBatteryReceiver.onReceive: Shouldn't send backup when connecting, just synchronising time");
                    OBSystemsManager.sharedManager.connectToWifiAndSynchronizeTime();
                }
            }
            else
            {
                MainActivity.log("OBBatteryReceiver.onReceive: State hasn't changed, No action is required for now");
            }
        }
        else
        {
            MainActivity.log("OBBatteryReceiver:onReceive: OBSystemsManager hasn't been created yet. Aborting");
        }
    }

    public String printStatus()
    {
        String batteryLevel = String.format("%.1f%%", getBatteryLevel());
        return batteryLevel + " " + ((isCharging) ? "charging" : "") + " " + ((usbCharge) ? "USB" : "") + ((acCharge) ? "AC" : "");
    }

    public float getBatteryLevel()
    {
        if (batteryLevel == -1 || batteryScale == -1)
        {
            return 50.0f;
        }
        return ((float) batteryLevel / (float) batteryScale) * 100.0f;
    }

    public boolean cablePluggedIn()
    {
        return usbCharge || acCharge;
    }

}
