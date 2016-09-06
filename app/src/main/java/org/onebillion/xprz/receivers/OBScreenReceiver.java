package org.onebillion.xprz.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.onebillion.xprz.mainui.MainActivity;

/**
 * Created by pedroloureiro on 02/09/16.
 */
public class OBScreenReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        MainActivity.log("OBScreenReceiver triggered");
        //
        if (intent == null) return;
        //
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        {
            System.out.println("SCREEN TURNED OFF on BroadcastReceiver");
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
        {
            System.out.println("SCREEN TURNED ON on BroadcastReceiver");
        }
        else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            System.out.println("USER PRESENT on BroadcastReceiver");
        }
    }
}
