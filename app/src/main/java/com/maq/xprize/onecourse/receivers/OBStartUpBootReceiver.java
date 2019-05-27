package com.maq.xprize.onecourse.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.maq.xprize.onecourse.utils.OBAnalytics;
import com.maq.xprize.onecourse.utils.OBAnalyticsManager;

/**
 * Created by pedroloureiro on 08/09/16.
 */
public class OBStartUpBootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        try
        {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
            //
            contentIntent.send();
            //
            if (OBAnalyticsManager.sharedManager != null)
            {
                OBAnalyticsManager.sharedManager.deviceTurnedOn();
            }
        }
        catch (PendingIntent.CanceledException e)
        {
            e.printStackTrace();
        }
    }
}
