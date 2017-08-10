package org.onebillion.onecourse.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
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
        MainActivity.log(" BATTERY RECEIVER CREATED ");
    }


    @Override
    public void onReceive (Context context, Intent intent)
    {
        if (MainActivity.mainActivity == null) return;
        //
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        int chargePlug = -1;
        if(status != -1)
        {
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

            chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;

            acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            MainActivity.log(" battery level : " + batteryLevel + ", battery scale: " + batteryScale);
        }


        //
        MainActivity.log(printStatus());
        if(MainActivity.mainActivity != null)
        {
            MainActivity.mainActivity.onBatteryStatusReceived(getBatteryLevel(),cablePluggedIn());
        }
        //
        if (OBSystemsManager.sharedManager != null)
        {
            OBSystemsManager.sharedManager.refreshStatus();
            //
            MainActivity.log(" chargePlug flag value: " + chargePlug + ", battery status: " + status);
            //
            if (chargePlug > 0 && OBSystemsManager.sharedManager.shouldSendBackupWhenConnected())
            {
                MainActivity.log("OBBatteryReceiver.Device is now connected to a power supply.");
                if (OBSystemsManager.sharedManager.backup_isRequired())
                {
                    MainActivity.log("OBBatteryReceiver.Backup is required. Connecting to backup WIFI.");
                    OBSystemsManager.sharedManager.backup_connectToWifiAndUploadDatabase();
                }
            }
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
