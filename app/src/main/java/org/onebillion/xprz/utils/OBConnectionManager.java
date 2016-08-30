package org.onebillion.xprz.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import org.onebillion.xprz.mainui.MainActivity;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBConnectionManager
{
    public static OBConnectionManager sharedManager;

    public OBConnectionManager ()
    {
        sharedManager = this;
    }

    public String wifiSSID ()
    {
        return MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_WIFI_SSID);
    }

    public String wifiPassword ()
    {
        return MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_WIFI_PASSWORD);
    }

    public void checkForConnection ()
    {
        ConnectivityManager connManager = (ConnectivityManager) MainActivity.mainActivity.getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        Network networks[] = connManager.getAllNetworks();
        boolean wifiFoundAndConnected = false;
        for (Network network : networks)
        {
            NetworkInfo info = connManager.getNetworkInfo(network);
            if (info.getType() == ConnectivityManager.TYPE_WIFI && info.isConnected())
            {
                wifiFoundAndConnected = true;
            }
        }
        if (!wifiFoundAndConnected)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    MainActivity.mainActivity.log("Wifi not connected. Attempting to activate and connect");
                    // attempt to connect to wifi
                    connectToWifi();
                }
            });
        }
    }

    public void connectToWifi ()
    {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", wifiSSID());
        wifiConfig.preSharedKey = String.format("\"%s\"", wifiPassword());

        WifiManager wifiManager = (WifiManager) MainActivity.mainActivity.getSystemService(MainActivity.WIFI_SERVICE);
        boolean wifiEnabled = wifiManager.isWifiEnabled();
        if (!wifiEnabled)
        {
            MainActivity.mainActivity.log("Wifi was disabled. Enabling now.");
            wifiManager.setWifiEnabled(true);
        }
        //remember id
        MainActivity.mainActivity.log("Wifi attempting to connect to " + wifiConfig.SSID);
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }
}
