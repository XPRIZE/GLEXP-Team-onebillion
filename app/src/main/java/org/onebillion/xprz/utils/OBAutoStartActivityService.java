package org.onebillion.xprz.utils;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.onebillion.xprz.mainui.MainActivity;


/**
 * Created by pedroloureiro on 02/09/16.
 */
public class OBAutoStartActivityService extends Service
{
    private final String TAG = "livecode";

    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate ()
    {
        super.onCreate();
        //
        log("OBAutoStartActivityService.created", this);
    }

    public void onDestroy ()
    {
        super.onDestroy();
        //
        log("OBAutoStartActivityService.killed", this);
    }


    public int onStartCommand (Intent intent, int flags, int startId)
    {
        log("OBAutoStartActivityService.onStartCommand " + intent, this);
        //
        if (intent == null)
        {
//            Toast.makeText(this, "Recovering from crash", Toast.LENGTH_LONG).show();
        }
        else
        {
//            Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        }
        //
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        try
        {
            log("OBAutoStartActivityService.launching:" + launchIntent.toString(), this);
            contentIntent.send();
        }
        catch (PendingIntent.CanceledException e)
        {
            e.printStackTrace();
        }
        //
        return START_STICKY;
    }


    private void log(String message, Context context)
    {
        Log.v(TAG, message);
        //
//        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
