package org.onebillion.onecourse.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.onebillion.onecourse.mainui.MainActivity;

import java.util.List;

/**
 * Created by pedroloureiro on 30/08/16.
 */
public class OBConnectionManager
{
    public static OBConnectionManager sharedManager;
    //
    private BroadcastReceiver scanResultsReceiver;
    private int targetNetworkID;

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

    public void startupConnection (final OBUtils.RunLambda block)
    {
        MainActivity.log("OBConnectionManager.checkForConnection");
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
                    MainActivity.log("Wifi not connected. Attempting to activate and connect");
                    // attempt to connect to wifi
                    connectToNetwork(wifiSSID(), wifiPassword(), block);
                }
            });
        }
        else
        {
            if (block != null)
            {
                try
                {
                    MainActivity.log("OBConnectionManager.startupConnection. running completion block");
                    OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            block.run();
                        }
                    });
                }
                catch (Exception e)
                {
                    MainActivity.log("OBConnectionManager.startupConnection.exception caught while running completion block");
                    e.printStackTrace();
                }
            }
        }
    }

    public void disconnectWifi()
    {
        MainActivity.log("OBConnectionManager.disconnectWifi");
        WifiManager wifiManager = (WifiManager) MainActivity.mainActivity.getSystemService(MainActivity.WIFI_SERVICE);
        MainActivity.log("OBConnectionManager.disconnectWifi. Disconnected network");
        wifiManager.disconnect();
        MainActivity.log("OBConnectionManager.disconnectWifi. Disabling wifi");
        wifiManager.setWifiEnabled(false);
    }


    public Boolean keepWifiOn ()
    {
        String keepWifiOn = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_KEEP_WIFI_ON);
        if (keepWifiOn != null && keepWifiOn.equals("false"))
        {
            return false;
        }
        return true;
    }


    public boolean isScanningDisabled()
    {
        WifiManager wifiManager = (WifiManager) MainActivity.mainActivity.getSystemService(MainActivity.WIFI_SERVICE);
        if (wifiManager.isScanAlwaysAvailable()) return false;
        //
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isDiscovering()) return false;
        //
        return true;
    }


    public void disconnectWifiIfAllowed ()
    {
        if (!keepWifiOn())
        {
            WifiManager wifiManager = (WifiManager) MainActivity.mainActivity.getSystemService(MainActivity.WIFI_SERVICE);
            wifiManager.setWifiEnabled(false);
        }
    }

    public boolean setBluetooth (boolean enable)
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled)
        {
            return bluetoothAdapter.enable();
        }
        else if (!enable && isEnabled)
        {
            return bluetoothAdapter.disable();
        }
        return true;
    }



    public void connectToNetwork(final String ssid, final String password, final OBUtils.RunLambda block)
    {
        MainActivity.log("OBConnectionManager.connectToNetwork [" + ssid + "] [" + password + "]");
        if (ssid == null)
        {
            MainActivity.log("OBConnectionManager.connectToNetwork SSID is null. Aborting and not running completion block");
            return;
        }
        final WifiManager wfMgr = (WifiManager) MainActivity.mainActivity.getSystemService(Context.WIFI_SERVICE);
        //
        if (!wfMgr.isWifiEnabled())
        {
            OBSystemsManager.unregisterReceiver(scanResultsReceiver);
            scanResultsReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive (Context context, Intent intent)
                {
                    MainActivity.log("OBConnectionManager.receiver.WIFI_STATE_CHANGED_ACTION");
                    //
                    if (wfMgr.isWifiEnabled())
                    {
                        MainActivity.log("OBConnectionManager.connectToNetwork. Wifi is now enabled. Attempting to connect again");
                        //
                        OBSystemsManager.unregisterReceiver(scanResultsReceiver);
                        connectToNetwork(ssid, password, block);
                    }
                }
            };
            MainActivity.mainActivity.registerReceiver(scanResultsReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
            wfMgr.setWifiEnabled(true);
        }
        else
        {
            WifiInfo newInfo = wfMgr.getConnectionInfo();
            if (newInfo.getSSID().equals(ssid) && newInfo.getSupplicantState() == SupplicantState.COMPLETED)
            {
                MainActivity.log("OBConnectionManager.connectToNetwork. already connected to the network");
                //
                if (block != null)
                {
                    try
                    {
                        MainActivity.log("OBConnectionManager.connectToNetwork. running completion block");
                        OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
                        {
                            @Override
                            public void run () throws Exception
                            {
                                block.run();
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        MainActivity.log("OBConnectionManager.connectToNetwork.exception caught while running completion block");
                        e.printStackTrace();
                    }
                }
                else
                {
                    MainActivity.log("OBConnectionManager.connectToNetwork.block is empty. nothing to do");
                }
            }
            else
            {
                OBSystemsManager.unregisterReceiver(scanResultsReceiver);
                scanResultsReceiver = new BroadcastReceiver()
                {
                    @Override
                    public void onReceive (Context context, Intent intent)
                    {
                        MainActivity.log("OBConnectionManager.connectToNetwork.scanResultsReceiver.onReceive");
                        //
                        WifiConfiguration wfc = new WifiConfiguration();
                        wfc.SSID = "\"".concat(ssid).concat("\"");
                        wfc.status = WifiConfiguration.Status.DISABLED;
                        wfc.priority = 40;
                        //
                        List<ScanResult> networkList = wfMgr.getScanResults();
                        //
                        if (networkList != null)
                        {
                            for (ScanResult network : networkList)
                            {
//                            MainActivity.log("OBConnectionManager.connectToNetwork.available network: " + network.toString());
                                //
                                if (ssid.equals(network.SSID))
                                {
                                    String capabilities = network.capabilities;
                                    MainActivity.log(network.SSID + " capabilities : " + capabilities);
                                    //
                                    if (capabilities.contains("WPA"))
                                    {
//                                    MainActivity.log("OBConnectionManager.connectToNetwork.WPA");
                                        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                                        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                                        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                                        wfc.preSharedKey = "\"".concat(password).concat("\"");
                                    }
                                    else if (capabilities.contains("WEP"))
                                    {
//                                    MainActivity.log("OBConnectionManager.connectToNetwork.WEP");
                                        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                                        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                                        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                                        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                                        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                                        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                                        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                                        wfc.wepKeys[0] = "\"".concat(password).concat("\"");
                                        wfc.wepTxKeyIndex = 0;
                                    }
                                    else
                                    {
//                                    MainActivity.log("OBConnectionManager.connectToNetwork.Other");
                                        wfc.BSSID = network.BSSID;
                                        wfc.allowedAuthAlgorithms.clear();
                                        wfc.allowedGroupCiphers.clear();
                                        wfc.allowedKeyManagement.clear();
                                        wfc.allowedPairwiseCiphers.clear();
                                        wfc.allowedProtocols.clear();
                                    }
                                }
                            }
                        }
                        //
                        int networkId = wfMgr.addNetwork(wfc);
//                    MainActivity.log("OBConnectionManager.connectToNetwork.networkID " + networkId);
                        //
                        if (networkId != -1)
                        {
                            OBSystemsManager.unregisterReceiver(scanResultsReceiver);
                            scanResultsReceiver = new BroadcastReceiver()
                            {
                                @Override
                                public void onReceive (Context context, Intent intent)
                                {
                                    WifiInfo newInfo = wfMgr.getConnectionInfo();
                                    MainActivity.log("OBConnectionManager.receiver.NETWORK_STATE_CHANGED_ACTION.info --> " + newInfo.toString());
                                    //
                                    if (newInfo.getSupplicantState() == SupplicantState.COMPLETED)
                                    {
                                        MainActivity.log("Wifi is now connected!!");
                                        OBSystemsManager.unregisterReceiver(scanResultsReceiver);
                                        //
                                        if (block != null)
                                        {
                                            try
                                            {
                                                MainActivity.log("OBConnectionManager.connectToNetwork. running completion block");
                                                OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
                                                {
                                                    @Override
                                                    public void run () throws Exception
                                                    {
                                                        block.run();
                                                    }
                                                });
                                            }
                                            catch (Exception e)
                                            {
                                                MainActivity.log("OBConnectionManager.connectToNetwork.exception caught while running completion block");
                                                e.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            MainActivity.log("OBConnectionManager.connectToNetwork.block is empty. nothing to do");
                                        }
                                    }
                                }
                            };
                            //
                            MainActivity.mainActivity.registerReceiver(scanResultsReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
                            MainActivity.log("OBConnectionManager.connectToNetwork.enabling network");
                            wfMgr.enableNetwork(networkId, true);
                        }
                    }
                };
                MainActivity.log("OBConnectionManager.connectToNetwork.registering receiver");
                MainActivity.mainActivity.registerReceiver(scanResultsReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                MainActivity.log("OBConnectionManager.connectToNetwork.starting scan");
                wfMgr.disconnect();
                wfMgr.startScan();
            }
        }
    }

}
