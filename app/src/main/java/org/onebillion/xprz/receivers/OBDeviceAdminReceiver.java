package org.onebillion.xprz.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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


}
