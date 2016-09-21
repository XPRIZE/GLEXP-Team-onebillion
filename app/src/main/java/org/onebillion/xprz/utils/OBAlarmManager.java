package org.onebillion.xprz.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.onebillion.xprz.mainui.MainActivity;

/**
 * Created by michal on 20/09/16.
 */
public class OBAlarmManager
{
    public static PendingIntent scheduleRepeatingAlarm(long triggerAtMillis, long intervalMillis)
    {
        Intent alarmIntent = new Intent(MainActivity.mainActivity, OBAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.mainActivity, 0, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager)MainActivity.mainActivity.getSystemService(Context.ALARM_SERVICE);
        cancelAlarm(pendingIntent);

        alarmManager.setRepeating(AlarmManager.RTC,triggerAtMillis,intervalMillis,pendingIntent);
        return pendingIntent;
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




