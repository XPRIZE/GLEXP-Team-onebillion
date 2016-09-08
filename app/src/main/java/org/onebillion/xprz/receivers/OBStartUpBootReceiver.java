package org.onebillion.xprz.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.utils.OBAutoStartActivityService;

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
        }
        catch (PendingIntent.CanceledException e)
        {
            e.printStackTrace();
        }
    }
}
