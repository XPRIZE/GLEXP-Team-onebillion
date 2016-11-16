package org.onebillion.onecourse.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBSystemsManager;

/**
 * Created by pedroloureiro on 25/08/16.
 */
public class OBBatteryReceiver extends BroadcastReceiver
{
    public boolean usbCharge, acCharge, isCharging;

    @Override
    public void onReceive (Context context, Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;

        acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        //
//        float battery = OBBatteryReceiver.getBatteryLevel();
//        MainActivity.log("Battery Info: " + ((isCharging) ? "is charging" : "not charging") + " " + ((usbCharge) ? "USB" : "" + " ") + ((acCharge) ? "AC" : "") + " " + battery + "%");
        //
        OBSystemsManager.sharedManager.refreshStatus();
        //
        MainActivity.log(" chargePlug flag value: " + chargePlug);
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

    public String printStatus()
    {
        String batteryLevel = String.format("%.1f%%", OBBatteryReceiver.getBatteryLevel());
        return batteryLevel + " " + ((isCharging) ? "charging" : "") + " " + ((usbCharge) ? "USB" : "") + ((acCharge) ? "AC" : "");
    }

    public static float getBatteryLevel()
    {
        Intent batteryIntent = MainActivity.mainActivity.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1)
        {
            return 50.0f;
        }
        return ((float) level / (float) scale) * 100.0f;
    }
}
