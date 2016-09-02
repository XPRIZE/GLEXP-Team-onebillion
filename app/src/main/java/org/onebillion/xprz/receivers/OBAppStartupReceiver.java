package org.onebillion.xprz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.onebillion.xprz.mainui.MainActivity;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBAppStartupReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        MainActivity.mainActivity.log("Received OnAppStartupReceiver");
        //
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_USER_PRESENT.equals(intent.getAction()))
        {
            Intent serviceIntent = new Intent(context, MainActivity.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(serviceIntent);
        }
    }
}
