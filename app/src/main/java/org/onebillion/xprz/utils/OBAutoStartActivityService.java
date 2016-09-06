package org.onebillion.xprz.utils;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.widget.Toast;

import org.onebillion.xprz.mainui.MainActivity;


/**
 * Created by pedroloureiro on 02/09/16.
 */
public class OBAutoStartActivityService extends Service
{

    @Override
    public IBinder onBind (Intent intent)
    {
        return null;
//        return mBinder;
    }


    @Override
    public void onCreate ()
    {
        super.onCreate();
        //
        MainActivity.log("OBAutoStartActivityService.created");
    }



    public void onDestroy ()
    {
        super.onDestroy();
        //
        MainActivity.log("OBAutoStartActivityService.killed");
//        Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show();
    }


    public int onStartCommand (Intent intent, int flags, int startId)
    {
        MainActivity.log("OBAutoStartActivityService.started");
        //
        if (intent == null)
        {
            Toast.makeText(this, "Recovering from crash", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
        }
        //
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getBaseContext().getPackageName());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        try
        {
            contentIntent.send();
        }
        catch (PendingIntent.CanceledException e)
        {
            e.printStackTrace();
        }
        //
//        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        i.setAction(Intent.ACTION_MAIN);
//        i.addCategory(Intent.CATEGORY_LAUNCHER);
//        startActivity(i);
        //
        return START_STICKY;
    }
}
