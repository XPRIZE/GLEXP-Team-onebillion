package com.maq.xprize.onecourse.hindi.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.maq.xprize.onecourse.hindi.mainui.MainActivity;

/**
 * Created by pedroloureiro on 06/09/16.
 */
public class OBDeviceAdminReceiver extends DeviceAdminReceiver
{
    public static ComponentName getComponentName (Context context)
    {
        return new ComponentName(context.getApplicationContext(), OBDeviceAdminReceiver.class);
    }

    public void onLockTaskModeEntering (Context context, Intent intent, String pkg)
    {
//        Toast.makeText(context, "Lock task mode entered", Toast.LENGTH_LONG).show();
        // ....
    }

    public void onLockTaskModeExiting (Context context, Intent intent)
    {
//        Toast.makeText(context, "Lock task mode exited", Toast.LENGTH_LONG).show();
        // ...
    }

    @Override
    public void onEnabled (Context context, Intent intent)
    {
        MainActivity.log("OBDeviceAdminReceiver.onEnabled");
        // admin rights
//        App.getPreferences().edit().putBoolean(App.ADMIN_ENABLED, true).commit(); //App.getPreferences() returns the sharedPreferences

    }

    @Override
    public void onDisabled (Context context, Intent intent)
    {
        MainActivity.log("OBDeviceAdminReceiver.onDisabled");
        // admin rights removed
//        App.getPreferences().edit().putBoolean(App.ADMIN_ENABLED, false).commit(); //App.getPreferences() returns the sharedPreferences
    }


}
