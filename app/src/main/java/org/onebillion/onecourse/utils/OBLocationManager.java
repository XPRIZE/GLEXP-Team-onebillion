package org.onebillion.onecourse.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;


import org.onebillion.onecourse.mainui.MainActivity;

/**
 * Created by pedroloureiro on 29/11/2017.
 *
 * https://stackoverflow.com/questions/1513485/how-do-i-get-the-current-gps-location-programmatically-in-android
 *
 */

public class OBLocationManager
{
    private LocationManager locationManager;

    public static OBLocationManager sharedManager;

    public OBLocationManager (Activity activity)
    {
        sharedManager = this;
        //
        locationManager = (LocationManager) activity.getSystemService(activity.getApplicationContext().LOCATION_SERVICE);
    }

    public Location getLastKnownLocation ()
    {
        if (ActivityCompat.checkSelfPermission(MainActivity.mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            MainActivity.log("OBLocationManager.getLastKnownLocation.Permissions were NOT granted to access device location");
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }


}
