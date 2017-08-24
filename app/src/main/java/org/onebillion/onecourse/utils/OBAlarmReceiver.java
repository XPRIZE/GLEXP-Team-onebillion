package org.onebillion.onecourse.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.onebillion.onecourse.mainui.MainActivity;

import java.util.Calendar;
import java.util.Currency;

/**
 * OBAlarmReceiver
 *
 * Created by michal on 20/09/16.
 */

public class OBAlarmReceiver extends BroadcastReceiver
{
    public static final String EXTRA_ALARMTIME = "org.onebillion.OBAlarmReceiver.alarmtime";
    public static final String EXTRA_INTERVAL = "org.onebillion.OBAlarmReceiver.interval";
    public static final String EXTRA_REQUESTCODE = "org.onebillion.OBAlarmReceiver.requestcode";

    @Override
    public void onReceive (Context context, Intent intent)
    {
        MainActivity.log("Alarm triggered!");
        if (MainActivity.mainActivity != null)
        {
            MainActivity.mainActivity.onAlarmReceived(intent);
        }
        long starttime = intent.getLongExtra(EXTRA_ALARMTIME, 0);
        long interval = intent.getLongExtra(EXTRA_INTERVAL, 0);
        int requestCode = intent.getIntExtra(EXTRA_REQUESTCODE, 0);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(starttime);

        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.HOUR, startCalendar.get(Calendar.HOUR));
        currentCalendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
        currentCalendar.set(Calendar.SECOND, startCalendar.get(Calendar.SECOND));
        //
        if (interval > 0)
        {
            OBAlarmManager.scheduleRepeatingAlarm(currentCalendar.getTimeInMillis() + interval, interval, requestCode);
        }
    }
}