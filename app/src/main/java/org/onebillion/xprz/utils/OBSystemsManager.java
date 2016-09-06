package org.onebillion.xprz.utils;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.Display;
import android.widget.Toast;

import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.receivers.OBBatteryReceiver;
import org.onebillion.xprz.receivers.OBSettingsContentObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 31/08/16.
 */
public class OBSystemsManager
{
    public static OBSystemsManager sharedManager;

    private OBBatteryReceiver batteryReceiver;
    private OBSettingsContentObserver settingsContentObserver;
    private OBBrightnessManager brightnessManager;
    private OBExpansionManager expansionManager;
    private OBConnectionManager connectionManager;
    private Map<String, List<String>> memoryUsageMap;

    public Handler mainHandler;
    public OBLabel statusLabel;

    private boolean AppIsInForeground;
    private final int keepInForegroundInterval = 1000;
    private Runnable keepInForegroundRunnable;
    private boolean suspended;


    public OBSystemsManager ()
    {
        batteryReceiver = new OBBatteryReceiver();
        settingsContentObserver = new OBSettingsContentObserver(MainActivity.mainActivity, new Handler());
        brightnessManager = new OBBrightnessManager();
        expansionManager = new OBExpansionManager();
        connectionManager = new OBConnectionManager();
        suspended = false;
        //
        memoryUsageMap = new HashMap<String, List<String>>();
        //
        keepInForegroundRunnable = new Runnable()
        {
            @Override
            public void run ()
            {
                if (OBSystemsManager.sharedManager.isScreenOn(MainActivity.mainActivity))
                {
                    if (!OBSystemsManager.sharedManager.isAppIsInForeground())
                    {
                        MainActivity.log("App is not in the foreground. Move it!");
                        //
                        Intent i = MainActivity.mainActivity.getPackageManager().getLaunchIntentForPackage(MainActivity.mainActivity.getPackageName());
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.mainActivity, 0, i, 0);
                        try
                        {
                            pendingIntent.send();
                        }
                        catch (PendingIntent.CanceledException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    mainHandler.postDelayed(this, keepInForegroundInterval);
                }
            }
        };
        //
        sharedManager = this;
    }


    public void startForegroundCheck ()
    {
        if (mainHandler != null && keepInForegroundRunnable != null && !suspended)
        {
            MainActivity.log("keep in foreground runnable: starting check");
            mainHandler.removeCallbacks(keepInForegroundRunnable);
            mainHandler.postDelayed(keepInForegroundRunnable, keepInForegroundInterval);
        }
    }


    public boolean isAppIsInForeground ()
    {
        return AppIsInForeground;
    }


    public boolean isScreenOn (Context context)
    {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH)
        {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            boolean screenOn = false;
            for (Display display : dm.getDisplays())
            {
                if (display.getState() != Display.STATE_OFF)
                {
                    screenOn = true;
                }
            }
            return screenOn;
        }
        else
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }


    private boolean isMyServiceRunning (Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) MainActivity.mainActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName()))
            {
                return true;
            }
        }
        return false;
    }


    public void killAllServices ()
    {
        MainActivity.log("OBSystemsManager.killing all services");
        //
        MainActivity.mainActivity.stopService(new Intent(MainActivity.mainActivity, OBAutoStartActivityService.class));
    }


    public void startServices ()
    {
        MainActivity.log("OBSystemsManager.starting all services");
        //
        if (!isMyServiceRunning(OBAutoStartActivityService.class))
        {
            MainActivity.log("OBAutoStartActivityService was not running. Starting now");
            Intent serviceIntent = new Intent(MainActivity.mainActivity, OBAutoStartActivityService.class);
            MainActivity.mainActivity.startService(serviceIntent);
        }
    }


    public void runChecks ()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                startServices();
                //
                if (mainHandler == null)
                {
                    mainHandler = new Handler(MainActivity.mainActivity.getMainLooper());
                }
                //
                connectionManager.sharedManager.checkForConnection();
                //
                OBSQLiteHelper.getSqlHelper().runMaintenance();
                //
                runChecksumComparisonTest();
            }
        });
    }


    public void printMemoryStatus (String message)
    {
        ActivityManager activityManager = (ActivityManager) MainActivity.mainActivity.getSystemService(MainActivity.ACTIVITY_SERVICE);
        int id = android.os.Process.myPid();
        int[] list = {id};
        Debug.MemoryInfo result[] = activityManager.getProcessMemoryInfo(list);

        if (message != null) MainActivity.log("Memory status: " + message);

        for (Debug.MemoryInfo info : result)
        {
            Map<String, String> map = info.getMemoryStats();
            for (String key : map.keySet())
            {
                List<String> memoryEntry = memoryUsageMap.get(key);
                if (memoryEntry == null) memoryEntry = new ArrayList<String>();
                memoryEntry.add(map.get(key));
                memoryUsageMap.put(key, memoryEntry);
            }
        }
        //
        for (String key : memoryUsageMap.keySet())
        {
            List<String> values = memoryUsageMap.get(key);
            if (values == null) continue;
            if (values.isEmpty()) continue;
            //
            long firstValue = Long.parseLong(values.get(0));
            long lastValue = Long.parseLong(values.get(values.size() - 1));
            if (values.size() > 2)
            {
                long secondLastValue = Long.parseLong(values.get(values.size() - 2));
                long diff = lastValue - secondLastValue;
                long fullDiff = lastValue - firstValue;
                MainActivity.log("Memory status: " + key + " --> " + lastValue + " (" + ((diff > 0) ? "+" : "") + diff + ")  --> since beginning (" + ((fullDiff > 0) ? "+" : "") + fullDiff + ")");
            }
            else
            {
                long diff = lastValue - firstValue;
                MainActivity.log("Memory status: " + key + " --> " + lastValue + " (" + ((diff > 0) ? "+" : "") + diff + ")");
            }
        }
    }


    public void runBatterySavingMode ()
    {
        OBConnectionManager.sharedManager.disconnectWifiIfAllowed();
        OBConnectionManager.sharedManager.setBluetooth(false);
        //
        killBackgroundProcesses();
    }


    public void killBackgroundProcesses ()
    {
        ActivityManager activityManager = (ActivityManager) MainActivity.mainActivity.getSystemService(MainActivity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfo = activityManager.getRunningAppProcesses();
        if (procInfo != null)
        {
            for (ActivityManager.RunningAppProcessInfo process : procInfo)
            {
                int importance = process.importance;
                int pid = process.pid;
                String name = process.processName;
                //
                if (name.equals("manager.main")) continue;
                if (importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE)
                {
                    continue;
                }
                activityManager.killBackgroundProcesses(name);
            }
        }
    }


    public void onResume ()
    {
        MainActivity.log("OBSystemsManager.onResume detected");
        //
        startServices();
        //
        AppIsInForeground = true;
        //
        if (mainHandler != null && keepInForegroundRunnable != null)
        {
            mainHandler.removeCallbacks(keepInForegroundRunnable);
        }
        //
        if (batteryReceiver != null)
        {
            MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        //
        if (settingsContentObserver != null)
        {
            settingsContentObserver.onResume();
        }
        if (expansionManager != null)
        {
            MainActivity.mainActivity.registerReceiver(OBExpansionManager.sharedManager.downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        OBBrightnessManager.sharedManager.onResume();
    }


    public void onPause ()
    {
        MainActivity.log("OBSystemsManager.onPause detected");
        //
        AppIsInForeground = false;
        //
//        startForegroundCheck();
        //
        if (batteryReceiver != null)
        {
            MainActivity.mainActivity.unregisterReceiver(batteryReceiver);
        }
        //
        if (settingsContentObserver != null)
        {
            settingsContentObserver.onPause();
        }
        if (OBExpansionManager.sharedManager.downloadCompleteReceiver != null)
        {
            MainActivity.mainActivity.unregisterReceiver(OBExpansionManager.sharedManager.downloadCompleteReceiver);
        }
        OBBrightnessManager.sharedManager.onPause();
        //
//        OBSQLiteHelper.getSqlHelper().emergencyRestore();
    }


    public void onDestroy ()
    {
        MainActivity.log("OBSystemsManager.onDestroy detected");
        //
        mainHandler.removeCallbacks(keepInForegroundRunnable);
    }


    public void onStop ()
    {
        MainActivity.log("OBSystemsManager.onStop detected");
        //
        OBBrightnessManager.sharedManager.onStop();
    }


    public void onSuspend ()
    {
        MainActivity.log("OBSystemsManager.onSuspend detected");
        //
        suspended = true;
        //
        killAllServices();
        //
        OBBrightnessManager.sharedManager.onSuspend();
        //
        if (mainHandler != null && keepInForegroundRunnable != null)
        {
            mainHandler.removeCallbacks(keepInForegroundRunnable);
        }
    }


    public void onContinue ()
    {
        MainActivity.log("OBSystemsManager.onContinue detected");
        //
        suspended = false;
        //
        OBBrightnessManager.sharedManager.onContinue();
        //
        runBatterySavingMode();
    }


    public void refreshStatus ()
    {
        String info = String.format("Battery [%s]    Brightness[%s]    Volume[%s]", batteryReceiver.printStatus(), brightnessManager.printStatus(), settingsContentObserver.printVolumeStatus());
        //
        if (statusLabel != null)
        {
            statusLabel.setString(info);
        }
    }


    public void setStatusLabel (OBLabel label)
    {
        statusLabel = label;
        refreshStatus();
    }


    private void runChecksumComparisonTest ()
    {
        MainActivity.log("Checksum comparison BEGIN");
        OBXMLManager xmlManager = new OBXMLManager();
        List<File> checkSumFiles = OBExpansionManager.sharedManager.getChecksumFiles();
        for (File file : checkSumFiles)
        {
            try
            {
                List<OBXMLNode> xml = xmlManager.parseFile(new FileInputStream(file));
                OBXMLNode rootNode = xml.get(0);
                //
                for (OBXMLNode node : rootNode.children)
                {
                    String path = node.attributeStringValue("id");
                    String md5 = node.attributeStringValue("md5");
                    InputStream is = OBUtils.getInputStreamForPath(path);
                    Boolean result = checkMD5(md5, is);
                    if (!result)
                    {
                        MainActivity.log("Checksum comparison: Problem detected with asset " + path);
                    }
//                    MainActivity.log("CHECKSUM COMPARISON --> " + md5 + "\t" + ((is == null) ? "NOT FOUND" : "FOUND" + "\t" + (result ? "OK" : "ERROR") + "\t" + path));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        MainActivity.log("Checksum comparison END");
    }


    public static boolean checkMD5 (String md5, InputStream is)
    {
        if (TextUtils.isEmpty(md5) || is == null)
        {
            MainActivity.log("MD5 string empty or inputStream null");
            return false;
        }
        //
        String calculatedDigest = calculateMD5(is);
        if (calculatedDigest == null)
        {
            MainActivity.log("calculatedDigest null");
            return false;
        }
//        MainActivity.log("Calculated digest: " + calculatedDigest);
//        MainActivity.log("Provided digest: " + md5);
        //
        return calculatedDigest.equalsIgnoreCase(md5);
    }


    public static String calculateMD5 (InputStream is)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            MainActivity.log("Exception while getting digest");
            e.printStackTrace();
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try
        {
            while ((read = is.read(buffer)) > 0)
            {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                MainActivity.log("Exception on closing MD5 input stream");
                e.printStackTrace();
            }
        }
    }


}
