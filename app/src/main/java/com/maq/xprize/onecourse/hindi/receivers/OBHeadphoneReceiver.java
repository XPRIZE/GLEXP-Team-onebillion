package com.maq.xprize.onecourse.hindi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.utils.OBAnalytics;
import com.maq.xprize.onecourse.hindi.utils.OBAnalyticsManager;

/**
 * Created by pedroloureiro on 07/12/2017.
 */

public class OBHeadphoneReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive (Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG))
        {
            int state = intent.getIntExtra("state", -1);
            switch (state)
            {
                case 0:
                    MainActivity.log("OBHeadPhoneReceiver:unplugged");
                    OBAnalyticsManager.sharedManager.deviceHeadphonesUnplugged();
                    break;
                //
                case 1:
                    MainActivity.log("OBHeadPhoneReceiver:plugged in");
                    OBAnalyticsManager.sharedManager.deviceHeadphonesPluggedIn();
                    break;
                //
                default:
                    //
            }
        }
    }
}
