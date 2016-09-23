package org.onebillion.xprz.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.onebillion.xprz.mainui.MainActivity;

/**
 * Created by michal on 20/09/16.
 */

public class OBAlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity.log("Alarm triggered!");
        MainActivity.mainActivity.onAlarmReceived(intent);
    }
}