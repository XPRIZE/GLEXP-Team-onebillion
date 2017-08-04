package org.onebillion.onecourse.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.onebillion.onecourse.mainui.MainActivity;

/**
 * OBAlarmManager
 * Sets Alarms in the device to trigger actions at exact times
 *
 * Created by michal on 20/09/16.
 */
public class OBAlarmManager
{

    public static PendingIntent scheduleRepeatingAlarm(long triggerAtMillis, long intervalMillis, int requestCode)
    {
        if (MainActivity.mainActivity != null)
        {
            Intent alarmIntent = new Intent(MainActivity.mainActivity, OBAlarmReceiver.class);

            alarmIntent.putExtra(OBAlarmReceiver.EXTRA_ALARMTIME, triggerAtMillis);
            alarmIntent.putExtra(OBAlarmReceiver.EXTRA_INTERVAL, intervalMillis);
            alarmIntent.putExtra(OBAlarmReceiver.EXTRA_REQUESTCODE, requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.mainActivity, requestCode, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarmManager = (AlarmManager) MainActivity.mainActivity.getSystemService(Context.ALARM_SERVICE);
            cancelAlarm(pendingIntent);


            alarmManager.setExact(AlarmManager.RTC, triggerAtMillis, pendingIntent);

            return pendingIntent;
        }
        return null;
    }

    public static void cancelAlarm(PendingIntent pendingIntent)
    {
        try
        {
         AlarmManager alarmManager = (AlarmManager)MainActivity.mainActivity.getSystemService(Context.ALARM_SERVICE);
         alarmManager.cancel(pendingIntent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}




