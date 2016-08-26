package org.onebillion.xprz.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import org.onebillion.xprz.mainui.MainActivity;

/**
 * Created by pedroloureiro on 25/08/16.
 */
public class PowerConnectionReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;

        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        //
        float battery = MainActivity.mainActivity.getBatteryLevel();
        //
        MainActivity.mainActivity.log("Battery Info: " + ((isCharging) ? "is charging" : "not charging") + " " + ((usbCharge) ? "USB" : "" + " ") + ((acCharge) ? "AC" : "") + " " + battery * 100 + "%");
    }
}
