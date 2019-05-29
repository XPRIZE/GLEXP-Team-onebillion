package com.maq.xprize.onecourse.hindi.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.receivers.OBBatteryReceiver;
import com.maq.xprize.onecourse.hindi.receivers.OBDeviceAdminReceiver;
import com.maq.xprize.onecourse.hindi.receivers.OBHeadphoneReceiver;
import com.maq.xprize.onecourse.hindi.receivers.OBSettingsContentObserver;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by pedroloureiro on 31/08/16.
 */
public class OBSystemsManager implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener
{
    public static OBSystemsManager sharedManager;

    private OBBatteryReceiver batteryReceiver;
    private OBHeadphoneReceiver headphoneReceiver;

    private OBBrightnessManager brightnessManager;
    private Map<String, List<String>> memoryUsageMap;

    private Handler mainHandler;
    public OBLabel statusLabel;

    public OBSettingsContentObserver settingsContentObserver;
    public OBConnectionManager connectionManager;

    private ReentrantLock backupLock;
    private boolean AppIsInForeground;
    private boolean suspended;
    private boolean needsToRunChecksAfterResume;
    private boolean kioskModeActive = false;

    private OBUtils.RunLambda dateSetCompletionBlock, timeSetCompletionBlock;

    private String currentUnit = null;
    private String currentWeek = null;
    private String currentDay = null;

    public OBSystemsManager(Activity activity)
    {
        batteryReceiver = new OBBatteryReceiver();
        headphoneReceiver = new OBHeadphoneReceiver();
        //
        settingsContentObserver = new OBSettingsContentObserver(activity, new Handler());
        //
        brightnessManager = new OBBrightnessManager();
        connectionManager = new OBConnectionManager();
        //
        suspended = true;
        needsToRunChecksAfterResume = false;
        //
        memoryUsageMap = new HashMap<String, List<String>>();
        //
        sharedManager = this;
        //
        MainActivity.log("OBSystemsManager is up and running for device with UUID: " + device_getUUID());
    }

    public boolean hasWriteSettingsPermission()
    {
        if (!MainActivity.isSDKCompatible())
        {
            MainActivity.log("OBSystemsManager:hasWriteSettingsPermission: incompatible SDK version. exiting function");
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            return Settings.System.canWrite(MainActivity.mainActivity);
        } else
        {
            MainActivity.log("OBSystemsManager:hasWriteSettingsPermission: failsafe incompatible SDK version. exiting function");
            return true;
        }
    }

    public boolean isAppIsInForeground()
    {
        return AppIsInForeground;
    }


    public boolean isScreenOn(Context context)
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
        } else
        {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            return pm.isScreenOn();
        }
    }


    private boolean isMyServiceRunning(Class<?> serviceClass)
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


    public void killAllServices()
    {
        if (suspended) return;
        if (!OBSystemsManager.isFirstSetupComplete()) return;
        //
        MainActivity.log("OBSystemsManager.killing all services");
        //
        MainActivity.mainActivity.stopService(new Intent(MainActivity.mainActivity, OBAutoStartActivityService.class));
    }


    public void startServices()
    {
        if (suspended)
        {
            MainActivity.log("OBSystemsManager.startServices. Currently suspended. Aborting");
            return;
        }
        if (!OBSystemsManager.isFirstSetupComplete())
        {
            MainActivity.log("OBSystemsManager.startServices. Setup NOT complete. Aborting");
            return;
        }
        //
        MainActivity.log("OBSystemsManager.startServices");
        //
        if (!isMyServiceRunning(OBAutoStartActivityService.class) && OBConfigManager.sharedManager.shouldAppRestartAfterCrash())
        {
            MainActivity.log("OBAutoStartActivityService was not running. Starting now");
            //
            Intent serviceIntent = new Intent(MainActivity.mainActivity, OBAutoStartActivityService.class);
            MainActivity.mainActivity.startService(serviceIntent);
        }
        //
        PhoneStateListener phoneStateListener = new PhoneStateListener()
        {
            public void onCallForwardingIndicatorChanged(boolean cfi)
            {
            }

            public void onCallStateChanged(int state, String incomingNumber)
            {
            }

            public void onCellLocationChanged(CellLocation location)
            {
            }

            public void onDataActivity(int direction)
            {
            }

            public void onDataConnectionStateChanged(int state)
            {
            }

            public void onMessageWaitingIndicatorChanged(boolean mwi)
            {
            }

            public void onServiceStateChanged(ServiceState serviceState)
            {
            }

            public void onSignalStrengthsChanged(SignalStrength signalStrength)
            {
                OBAnalyticsManager.sharedManager.deviceMobileSignalStrength(signalStrength.getGsmSignalStrength());
            }
        };
        //
        TelephonyManager telephonyManager = (TelephonyManager) MainActivity.mainActivity.getSystemService(Context.TELEPHONY_SERVICE);

        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR |
                        PhoneStateListener.LISTEN_CALL_STATE |
                        PhoneStateListener.LISTEN_CELL_LOCATION |
                        PhoneStateListener.LISTEN_DATA_ACTIVITY |
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE |
                        PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR |
                        PhoneStateListener.LISTEN_SERVICE_STATE |
                        PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        MainActivity.log("OBSystemsManager.startServices complete");
    }


    public static boolean isFirstSetupComplete()
    {
        return OBPreferenceManager.getBooleanPreference("firstSetupComplete");
//        String result = MainActivity.mainActivity.getPreferences("firstSetupComplete");
//        return result != null && result.equals("true");
    }


    public static void printBuildVersion()
    {
        String buildNumber = OBConfigManager.sharedManager.getBuildNumber();
        MainActivity.log("Build Version: " + buildNumber);
    }


    public void runChecks()
    {
        if (suspended)
        {
            MainActivity.log("OBSystemsManager.runChecks.suspended. Aborting");
            needsToRunChecksAfterResume = true;
            return;
        }
        //
        MainActivity.log("OBSystemsManager.runChecks check for first setup complete");
        if (!OBSystemsManager.isFirstSetupComplete())
        {
            MainActivity.log("OBSystemsManager.runCheck.FirstSetup is NOT complete. Aborting");
            return;
        }
        //
        runChecksAfterResume();
    }

    public void runChecksAfterResume()
    {
        MainActivity.log("OBSystemsManager.runChecks --> pinApplication");
        pinApplication();
        //
        MainActivity.log("OBSystemsManager.runChecks --> startServices");
        startServices();
        //
        if (mainHandler == null)
        {
            mainHandler = new Handler(MainActivity.mainActivity.getMainLooper());
        }
        //
        MainActivity.log("OBSystemsManager.runChecks --> SQL maintenance");
        OBSQLiteHelper.getSqlHelper().runMaintenance();
        //
        needsToRunChecksAfterResume = false;
    }


    public Handler getMainHandler()
    {
        if (mainHandler == null)
        {
            mainHandler = new Handler(MainActivity.mainActivity.getMainLooper());
        }
        return mainHandler;
    }


    public void printMemoryStatus(String message)
    {
        if (!MainActivity.isSDKCompatible())
        {
            MainActivity.log("OBSystemsManager:printMemoryStatus: incompatible SDK version. exiting function");
            return;
        }
        //
        ActivityManager activityManager = (ActivityManager) MainActivity.mainActivity.getSystemService(MainActivity.ACTIVITY_SERVICE);
        int id = android.os.Process.myPid();
        int[] list = {id};
        Debug.MemoryInfo result[] = activityManager.getProcessMemoryInfo(list);

        if (message != null) MainActivity.log("Memory status: " + message);

        for (Debug.MemoryInfo info : result)
        {
            Map<String, String> map = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
            {
                map = info.getMemoryStats();
            }
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
            } else
            {
                long diff = lastValue - firstValue;
                MainActivity.log("Memory status: " + key + " --> " + lastValue + " (" + ((diff > 0) ? "+" : "") + diff + ")");
            }
        }
    }


    public void runBatterySavingMode()
    {
        MainActivity.log("OBSystemsManager.runBatterySavingMode");
        OBConnectionManager.sharedManager.setBluetooth(false);
        //
        //killBackgroundProcesses(); it's killing the keep alive services
        MainActivity.log("OBSystemsManager.runBatterySavingMode complete");
    }


    public void killBackgroundProcesses()
    {
        MainActivity.log("OBSystemsManager.killBackgroundProcesses");
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
                MainActivity.log("OBSystemsManager.killBackgroundProcesses: " + name);
                //activityManager.killBackgroundProcesses(name);
            }
        }
    }


    public void unzipAssetsIfFound(final OBUtils.RunLambda completionBlock)
    {
        if (OBConfigManager.sharedManager.shouldLookForZippedAsssets())
        {
            MainActivity.log("OBSystemsManager.unzipAssetsIfFound. looking for zipped assets");
            OBBrightnessManager.sharedManager.onSuspend();
            //
            Boolean unzipAll = OBConfigManager.sharedManager.shouldUnzipAllAssetsOnStartup();
            List<String> priorityList = OBConfigManager.sharedManager.getZippedAssetsPriorityFolders();
            List<File> assetsFolders = OBSystemsManager.sharedManager.getExternalAssetsFolders();
            //
            final File externalAssetsFolder = assetsFolders.get(0);
            String tempFolderPath = externalAssetsFolder.getAbsolutePath();
            //
            MainActivity.log("looking at " + tempFolderPath);
            //
            if (!tempFolderPath.endsWith("/")) tempFolderPath += "/";
            //
            final String externalAssetsFolderPath = tempFolderPath;
            //
            final List<String> priorityFiles = new ArrayList();
            //
            if (unzipAll)
            {
                List<String> otherFiles = Arrays.asList(externalAssetsFolder.list(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String filename)
                    {
                        return filename.toLowerCase().endsWith(".zip");
                    }
                }));
                //
                // Start unzipping the rest
                List<String> decompressList = new ArrayList<>();
                for (String file : otherFiles)
                {
                    String zippedFilePath = externalAssetsFolderPath + file; // already has the .zip extension
                    String folderPath = zippedFilePath.replaceAll(".zip", "");
                    //
                    if (OBUtils.fileExistsAtPath(zippedFilePath))
                    {
                        File decompressedFolder = new File(folderPath);
                        if (decompressedFolder.exists() && decompressedFolder.isDirectory())
                        {
                            // already extracted
                            continue;
                        }
                        decompressList.add(zippedFilePath);
                    }
                }
                OBUnZip unzipPriority = new OBUnZip(decompressList, externalAssetsFolderPath, new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        // enable "Test onecourse" button in setup menu
                        MainActivity.log("OBSystemsManager.unZipAssetsIfFound.assets are now ready to be used");
                        OBConfigManager.sharedManager.setAssetsReadyToBeUsed(true);
                        //
                        // Files have been unzip, can continue with operations
                        OBUtils.runOnMainThread(completionBlock);
                        OBBrightnessManager.sharedManager.onContinue();
                    }
                }, true);
                //
                MainActivity.log("unzipping: " + decompressList);
                //
                unzipPriority.execute();
            }
            else
            {
                for (String assetsFolderName : priorityList)
                {
                    final String zippedFilePath = externalAssetsFolderPath + assetsFolderName + ".zip";
                    MainActivity.log("OBSystemsManager.unzipAssetsIfFound.Looking for " + zippedFilePath);
                    if (OBUtils.fileExistsAtPath(zippedFilePath))
                    {
                        File decompressedFolder = new File(externalAssetsFolderPath + assetsFolderName);
                        if (decompressedFolder.exists() && decompressedFolder.isDirectory())
                            continue;
                        priorityFiles.add(zippedFilePath);
                        MainActivity.log("OBSystemsManager.unzipAssetsIfFound. Found " + zippedFilePath);
                    }
                }
                //
                OBUnZip unzipPriority = new OBUnZip(priorityFiles, externalAssetsFolderPath, new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        // Files have been unzip, can continue with operations
                        OBUtils.runOnMainThread(completionBlock);
                        OBBrightnessManager.sharedManager.onContinue();
                    }
                }, true);
                unzipPriority.execute();
            }
        }
        else
        {
            OBUtils.runOnMainThread(completionBlock);
        }
    }

    public OBUnZip unzipRestOfAssets()
    {
        if (OBConfigManager.sharedManager.shouldLookForZippedAsssets())
        {
            List<String> priorityList = OBConfigManager.sharedManager.getZippedAssetsPriorityFolders();
            List<File> assetsFolders = OBSystemsManager.sharedManager.getExternalAssetsFolders();
            //
            final File externalAssetsFolder = assetsFolders.get(0);
            String tempFolderPath = externalAssetsFolder.getAbsolutePath();
            if (!tempFolderPath.endsWith("/")) tempFolderPath += "/";
            //
            final String externalAssetsFolderPath = tempFolderPath;
            //
            final List<String> priorityFiles = new ArrayList();
            //
            for (String assetsFolderName : priorityList)
            {
                final String zippedFilePath = externalAssetsFolderPath + assetsFolderName + ".zip";
                if (OBUtils.fileExistsAtPath(zippedFilePath))
                {
                    File decompressedFolder = new File(externalAssetsFolderPath + assetsFolderName);
                    if (decompressedFolder.exists() && decompressedFolder.isDirectory()) continue;
                    priorityFiles.add(zippedFilePath);
                }
            }

            List<String> otherFiles = Arrays.asList(externalAssetsFolder.list(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String filename)
                {
                    return filename.toLowerCase().endsWith(".zip");
                }
            }));
            // Start unzipping the rest
            List<String> decompressList = new ArrayList<>();
            for (String file : otherFiles)
            {
                String zippedFilePath = externalAssetsFolderPath + file; // already has the .zip extension
                String folderPath = zippedFilePath.replaceAll(".zip", "");
                //
                if (priorityFiles.contains(zippedFilePath)) continue; // already extracted
                if (OBUtils.fileExistsAtPath(zippedFilePath))
                {
                    File decompressedFolder = new File(folderPath);
                    if (decompressedFolder.exists() && decompressedFolder.isDirectory())
                        continue; // already extracted
                    decompressList.add(zippedFilePath);
                }
            }
            //
            OBConfigManager.sharedManager.setAssetsReadyToBeUsed(decompressList.size() == 0);
            //
            OBUnZip unzipRest = new OBUnZip(decompressList, externalAssetsFolderPath, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    // enable "Test onecourse" button in setup menu
                    MainActivity.log("OBSystemsManager.unZipAssetsIfFound.assets are now ready to be used");
                    OBConfigManager.sharedManager.setAssetsReadyToBeUsed(true);
                }
            }, false);
            //
            MainActivity.log("unzipping: " + decompressList);
            //
            return unzipRest;
        }
        else
        {
            return null;
        }
    }

    public void onStart()
    {
        AppIsInForeground = true;
    }


    public void onResume()
    {
        MainActivity.log("OBSystemsManager.onResume detected");
        //
        if (needsToRunChecksAfterResume)
        {
            MainActivity.log("OBSystemsManager.onResume.running checks after resume");
            runChecksAfterResume();
        }
        //
        if (batteryReceiver != null)
        {
            registerBatteryReceiver();
        }
        //
        if (headphoneReceiver != null)
        {
            registerHeadphoneReceiver();
        }
        //
        if (settingsContentObserver != null)
        {
            settingsContentObserver.onResume();
        }
        //
        OBBrightnessManager.sharedManager.onResume();
    }


    public void onPause()
    {
        MainActivity.log("OBSystemsManager.onPause detected" + (MainActivity.mainActivity.isFinishing() ? " and is finishing!" : ""));
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
        if (headphoneReceiver != null)
        {
            MainActivity.mainActivity.unregisterReceiver(headphoneReceiver);
        }
        //
        if (settingsContentObserver != null)
        {
            settingsContentObserver.onPause();
        }
        //
        OBBrightnessManager.sharedManager.onPause();
    }


    public void onDestroy()
    {
        MainActivity.log("OBSystemsManager.onDestroy detected");
        DBSQL.finalise();
    }


    public void onStop()
    {
        if (MainActivity.mainActivity.isFinishing())
        {
            MainActivity.log("OBSystemsManager.onStop detected");
            //
            shutdownProcedures();
            //
            OBBrightnessManager.sharedManager.onStop();
        }
    }


    public void onSuspend()
    {
        MainActivity.log("OBSystemsManager.onSuspend detected");
        //
        suspended = true;
        //
        killAllServices();
        //
        OBBrightnessManager.sharedManager.onSuspend();
    }


    public void onContinue()
    {
        MainActivity.log("OBSystemsManager.onContinue");
        //
        suspended = false;
        //
        OBBrightnessManager.sharedManager.onContinue();
        //
        startServices();
        //
        runBatterySavingMode();
        //
        MainActivity.log("OBSystemsManager.onContinue complete");
    }


    public void refreshStatus()
    {
        if (statusLabel != null)
        {
            String info = String.format("Build:%s    Bat:%s    Br:%s    Vol:%s    %s%s%s",
                    OBConfigManager.sharedManager.getBuildNumber(),
                    batteryReceiver.printStatus(),
                    brightnessManager.printStatus(),
                    settingsContentObserver.printVolumeStatus(),
                    currentUnit != null ? String.format("Unit:%s   ", currentUnit) : "",
                    currentWeek != null ? String.format("Week:%s   ", currentWeek) : "",
                    currentDay != null ? String.format("Day:%s   ", currentDay) : "");
            //
            statusLabel.setString(info);
        }
    }

    public void setCurrentUnit(String unit)
    {
        if (OBConfigManager.sharedManager.isDebugEnabled())
        {
            currentUnit = unit;
            refreshStatus();
        }
    }


    public void setCurrentWeek(String week)
    {
        if (OBConfigManager.sharedManager.isDebugEnabled())
        {
            currentWeek = week;
            refreshStatus();
        }
    }


    public void setCurrentDay(String day)
    {
        if (OBConfigManager.sharedManager.isDebugEnabled())
        {
            currentDay = day;
            refreshStatus();
        }
    }


    public void setStatusLabel(OBLabel label)
    {
        statusLabel = label;
        refreshStatus();
    }

    public void registerBatteryReceiver()
    {
        Intent intent = MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_CONNECTED));
        MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_POWER_DISCONNECTED));
    }

    public void registerHeadphoneReceiver()
    {
        Intent intent = MainActivity.mainActivity.registerReceiver(headphoneReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }


    public static boolean checkMD5(String md5, InputStream is)
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


    public static String calculateMD5(InputStream is)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e)
        {
            MainActivity.log("Exception while getting digest");
//            e.printStackTrace();
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
        } catch (IOException e)
        {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally
        {
            try
            {
                is.close();
            } catch (IOException e)
            {
                MainActivity.log("Exception on closing MD5 input stream");
//                e.printStackTrace();
            }
        }
    }


    public void requestRootAccess()
    {
        MainActivity.log("OBSystemsManager.requestRootAccess");
        Process p;
        try
        {
            p = Runtime.getRuntime().exec("su");
            //
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo \"Do I have root?\" >/system/sd/temporary.txt\n");
            os.writeBytes("exit\n");
            os.flush();
            //
            try
            {
                p.waitFor();
                if (p.exitValue() != 255)
                {
                    MainActivity.log("OBSystemsManager.requestRootAccess --> App has been granted Root Access");
                } else
                {
                    MainActivity.log("OBSystemsManager.requestRootAccess --> App has NOT been granted Root Access");
                }
            } catch (InterruptedException e)
            {
                MainActivity.log("OBSystemsManager.requestRootAccess --> App has NOT been granted Root Access");
            }
        } catch (IOException e)
        {
            MainActivity.log("OBSystemsManager.requestRootAccess --> App has NOT been granted Root Access");
        }
    }


    public boolean hasAdministratorPrivileges()
    {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        return devicePolicyManager.isAdminActive(AdministratorReceiver());
    }


    public ComponentName AdministratorReceiver()
    {
        return OBDeviceAdminReceiver.getComponentName(MainActivity.mainActivity);
    }


    public boolean isDeviceOwner()
    {
        MainActivity.log("OBSystemsManager.isDeviceOwner");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        boolean result = devicePolicyManager.isDeviceOwnerApp(MainActivity.mainActivity.getPackageName());
        MainActivity.log(result ? "It is device owner" : "It's NOT device owner");
        return result;
    }


    public void requestToRemoveAccounts()
    {
        Toast.makeText(MainActivity.mainActivity, "Please remove all accounts before going back", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
        MainActivity.mainActivity.startActivityForResult(intent, MainActivity.REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES);
    }


    public void requestDeviceOwner()
    {
        MainActivity.log("OBSystemsManager.requestDeviceOwner");
        //
        String packageName = MainActivity.mainActivity.getPackageName();
        try
        {
//            String[] command = new String[]{"su", "-c", "dpm set-device-owner " + packageName + "/" + AdministratorReceiver().getClassName()}; // for normal Root
            String[] command = new String[]{"/system/xbin/su", "1000", "dpm", "set-device-owner", packageName + "/" + AdministratorReceiver().getClassName()}; // for AOSP root
            //
            MainActivity.log("OBSystemsManager.requestDeviceOwner.running [" + Arrays.toString(command) + "]");
            //
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            //
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line + "\n");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line + "\n");
            }
            String output = log.toString();
            MainActivity.log("OBSystemsManager.requestDeviceOwner.output from process: " + output);
            //
            if (!output.contains("Active admin set to component"))
            {
                Toast.makeText(MainActivity.mainActivity, "Device Owner was not set. Another App is the device owner.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e)
        {
            MainActivity.log("OBSystemsManager.requestDeviceOwner: device is not rooted. No point in continuing.");
//            e.printStackTrace();
        }
    }


    public boolean enableAdminstratorPrivileges()
    {
        MainActivity.log("OBSystemsManager.enableAdminstratorPrivileges");
        //
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = OBDeviceAdminReceiver.getComponentName(MainActivity.mainActivity);
        //
        if (!devicePolicyManager.isAdminActive(adminReceiver))
        {
            MainActivity.log("OBSystemsManager.enableAdminstratorPrivileges.requesting administrator privileges");
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "");
            MainActivity.mainActivity.startActivityForResult(intent, MainActivity.REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES);
            return false;
        }
        if (!devicePolicyManager.isDeviceOwnerApp(MainActivity.mainActivity.getPackageName()))
        {
            MainActivity.log("OBSystemsManager.enableAdminstratorPrivileges.requesting device owner");
            String packageName = MainActivity.mainActivity.getPackageName();
            try
            {
                // String resetCommand = "su -c rm /data/system/device_owner.xml; su -c rm /data/system/device_policies.xml;";
                //
                String[] command = new String[]{"su", "-c", "dpm set-device-owner " + packageName + "/" + adminReceiver.getClassName()};
                //
                MainActivity.log("OBSystemsManager.enableAdministratorPrivileges.running [" + command.toString() + "]");
                //
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
                //
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder log = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    log.append(line + "\n");
                }
                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = bufferedReader.readLine()) != null)
                {
                    log.append(line + "\n");
                }
                String output = log.toString();
                MainActivity.log("OBSystemsManager.output from process: " + output);
                //
                if (output.contains("Active admin set to component"))
                {
                    MainActivity.log("OBSystemsManager. App is now set to device owner");
                    return true;
                } else
                {
                    MainActivity.log("OBSystemsManager. Another account was detected. Requesting user to remove them"); // no idea how to do this programmatically
                    //
                    Toast.makeText(MainActivity.mainActivity, "Please remove all accounts before going back", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                    MainActivity.mainActivity.startActivityForResult(intent, MainActivity.REQUEST_FIRST_SETUP_ADMINISTRATOR_PRIVILEGES);
                    //
                    return false;
                }
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.device is not rooted. No point in continuing enableAdminstratorPrivileges");
//                e.printStackTrace();
                //
                return true;
            }
        }
        MainActivity.log("OBSystemsManager.enableAdminstratorPrivileges. all good");
        return true;
    }


    public void disableAdministratorPrivileges()
    {
        if (!MainActivity.isSDKCompatible())
        {
            MainActivity.log("OBSystemsManager:disableAdministratorPrivileges: incompatible SDK version. exiting function");
            return;
        }
        MainActivity.log("OBSystemsManager.disableAdministratorPrivileges");
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = OBDeviceAdminReceiver.getComponentName(MainActivity.mainActivity);
        //
        if (devicePolicyManager.isDeviceOwnerApp(MainActivity.mainActivity.getPackageName()))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                devicePolicyManager.setKeyguardDisabled(adminReceiver, false);
            }
            MainActivity.log("OBSystemsManager.keyguard restored");
            //
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                devicePolicyManager.setStatusBarDisabled(adminReceiver, false);
            }
            MainActivity.log("OBSystemsManager.status bar restored");
            //
            try
            {
                MainActivity.log("OBSystemsManager.disableAdministratorPrivileges: removing active admin");
                devicePolicyManager.removeActiveAdmin(adminReceiver);
                MainActivity.log("OBSystemsManager.disableAdministratorPrivileges: clearing device owner");
                devicePolicyManager.clearDeviceOwnerApp(MainActivity.mainActivity.getPackageName());
                MainActivity.log("OBSystemsManager.disableAdministratorPrivileges: done");
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.disableAdministratorPrivileges: exception caught");
//                e.printStackTrace();
                // App might not be the device owner at this point
            }
        }
        if (kioskModeActive)
        {
            MainActivity.mainActivity.stopLockTask();
            kioskModeActive = false;
        }
    }


    public void pinApplication()
    {
        MainActivity.log("OBSystemsManager.pinApplication");
        //
        if (OBConfigManager.sharedManager.shouldPinApplication())
        {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminReceiver = OBDeviceAdminReceiver.getComponentName(MainActivity.mainActivity);
            //
            if (devicePolicyManager.isDeviceOwnerApp(MainActivity.mainActivity.getPackageName()))
            {
                MainActivity.log("OBSystemsManager.pinApplication: attempting to pin app");
                String[] packages = {MainActivity.mainActivity.getPackageName()};
                devicePolicyManager.setLockTaskPackages(adminReceiver, packages);
                //
                if (devicePolicyManager.isLockTaskPermitted(MainActivity.mainActivity.getPackageName()))
                {
                    if (isAppIsInForeground())
                    {
                        try
                        {
                            MainActivity.log("OBSystemsManager.pinApplication: starting locked task");
                            MainActivity.mainActivity.startLockTask();
                            kioskModeActive = true;
                        } catch (Exception e)
                        {
                            MainActivity.log("OBSystemsManager.pinApplication: exception caught");
//                            e.printStackTrace();
                            kioskModeActive = false;
                        }
                    } else
                    {
                        MainActivity.log("OBSystemsManager.pinApplication:application is not in foreground, cancelling");
                    }
                }
            } else
            {
                MainActivity.log("OBSystemsManager.pinApplication: unable to pin application, not a device owner");
            }
            toggleKeyguardAndStatusBar(false);
        } else
        {
            MainActivity.log("OBSystemsManager.pinApplication: disabled in settings");
        }
    }


    public void unpinApplication()
    {
        MainActivity.log("OBSystemsManager.unpinApplication");
        //
        if (OBConfigManager.sharedManager.shouldPinApplication())
        {
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminReceiver = OBDeviceAdminReceiver.getComponentName(MainActivity.mainActivity);
            //
            if (devicePolicyManager.isDeviceOwnerApp(MainActivity.mainActivity.getPackageName()))
            {
                MainActivity.log("OBSystemsManager.unpinApplication: attempting to unpin app");
                String[] packages = {MainActivity.mainActivity.getPackageName()};
                devicePolicyManager.setLockTaskPackages(adminReceiver, packages);
                //
                if (devicePolicyManager.isLockTaskPermitted(MainActivity.mainActivity.getPackageName()))
                {
                    if (isAppIsInForeground())
                    {
                        try
                        {
                            MainActivity.log("OBSystemsManager.unpinApplication: starting locked task");
                            MainActivity.mainActivity.stopLockTask();
                            kioskModeActive = false;
                        } catch (Exception e)
                        {
                            MainActivity.log("OBSystemsManager.unpinApplication: exception caught");
                        }
                    } else
                    {
                        MainActivity.log("OBSystemsManager.unpinApplication:application is not in foreground, cancelling");
                    }
                }
            } else
            {
                MainActivity.log("OBSystemsManager.unpinApplication: unable to unpin application, not a device owner");
            }
            toggleKeyguardAndStatusBar(false);
        } else
        {
            MainActivity.log("OBSystemsManager.unpinApplication: disabled in settings");
        }
    }


    public void toggleKeyguardAndStatusBar(boolean status)
    {
        if (!MainActivity.isSDKCompatible())
        {
            MainActivity.log("OBSystemsManager:toggleKeyguardAndStatusBar: incompatible SDK version. exiting function");
            return;
        }
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = OBDeviceAdminReceiver.getComponentName(MainActivity.mainActivity);
        //
        if (devicePolicyManager.isDeviceOwnerApp(MainActivity.mainActivity.getPackageName()))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                devicePolicyManager.setKeyguardDisabled(adminReceiver, !status);
            }
            MainActivity.log("OBSystemsManager.keyguard has been " + (status ? "enabled" : "disabled"));
            //
//            devicePolicyManager.setStatusBarDisabled(adminReceiver, !status);
//            MainActivity.log("OBSystemsManager.status bar has been " + (status ? "enabled" : "disabled"));
        }
    }


    public void screenLock()
    {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) MainActivity.mainActivity.getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.lockNow();
    }

    public void shutdownProcedures()
    {
        // the shutdown procedure should only be triggered for debug mode, as we want the app to restart automatically and not lose any setting
        //
        MainActivity.log("OBSystemManager.shutdownProcedures");
        //
        OBBrightnessManager.sharedManager.disableBrightnessAdjustment();
        //
        saveLogToFile();
    }


    public void toggleNavigationBar(final int value)
    {
        MainActivity.log("OBSystemsManager." + (value == 0 ? "enabling" : "disabling") + " navigation bar");
        try
        {
            String command = "getprop qemu.hw.mainkeys";
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            //
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line + "\n");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = bufferedReader.readLine()) != null)
            {
                log.append(line + "\n");
            }
            String output = log.toString();
            MainActivity.log("OBSystemsManager.output from toggleNavigationBar: " + output);
            //
            if (output.contains(String.format("%d", value)))
            {
                MainActivity.log("OBSystemsManager.toggleNavigationBar. the system value is the same. nothing to do here");
                return;
            }
            command = "su -c 'setprop qemu.hw.mainkeys " + value + "; stop; start'";
            MainActivity.log("OBSystemsManager.running:" + command);
            //
            try
            {
                process = Runtime.getRuntime().exec(command);
                process.waitFor();
                //
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                log = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null)
                {
                    log.append(line + "\n");
                }
                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = bufferedReader.readLine()) != null)
                {
                    log.append(line + "\n");
                }
                output = log.toString();
                MainActivity.log("OBSystemsManager.toggleNavigationBar.output from process: " + output);
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.toggleNavigationBar. unable to execute command. Device is probably not rooted");
            }
        } catch (Exception e)
        {
            MainActivity.log("OBSystemsManager.toggleNagivationBar. exception caught");
//            e.printStackTrace();
        }

    }


    public void saveLogToFile()
    {
        File sd = new File(Environment.getExternalStorageDirectory(), "//onebillion//logs//");
        sd.mkdirs();
        //
        if (sd.canWrite())
        {
            DateFormat df = new android.text.format.DateFormat();
            String date = df.format("yyyy.MM.dd.hh.mm.ss", new java.util.Date()).toString();
            File file = new File(sd, String.format("%s.txt", date));
            try
            {
                MainActivity.log("Log exported to " + file.getAbsolutePath());
                @SuppressWarnings("unused")
                Process process = Runtime.getRuntime().exec("logcat -df " + file.getAbsolutePath());
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.saveLogToFile: exception caught");
//                e.printStackTrace();
            }
        }
    }

    public boolean isBackupRequired()
    {
        String value_string = OBPreferenceManager.getStringPreference("lastBackupTimeStamp");
        if (value_string == null) return true;
        long value = Long.parseLong(value_string);
        long currentTime = System.currentTimeMillis() / 1000;
        long elapsed = currentTime - value;
        boolean result = elapsed > OBConfigManager.sharedManager.getBackupIntervalInMinutes() * 60;
        MainActivity.log("OBSystemsManager.isBackupRequired --> " + result);
        return result;
    }


    public String device_getUUID()
    {
        String uuid = device_getSerial();
        //
        if (uuid == null)
        {
            uuid = "unknown_uuid";

            String mac = device_getMac();
            if (mac != null)
                uuid = mac.replace(":", "");

        }
        //
        return uuid;
    }

    public String device_getSerial()
    {
        return Build.SERIAL;
    }

    public String device_getMac()
    {
        String mac = null;
        try
        {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all)
            {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes != null)
                {
                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes)
                    {
                        res1.append(Integer.toHexString(b & 0xFF) + ":");
                    }
                    if (res1.length() > 0)
                    {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    mac = res1.toString();
                    break;
                }
            }
        } catch (Exception ex)
        {

        }
        return mac;
    }


    public void connectToWifiAndSynchronizeTime()
    {
        MainActivity.log("OBSystemsManager.connectToWifiAndSynchronizeTime");
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                String wifiSSID = OBConfigManager.sharedManager.getBackupWifiSSID();
                String wifiPassword = OBConfigManager.sharedManager.getBackupWifiPassword();
                //
                connectionManager.connectToNetwork_connectToWifi(wifiSSID, wifiPassword, new OBUtils.RunLambdaWithSuccess()
                {
                    @Override
                    public void run(boolean success) throws Exception
                    {
                        if (success)
                        {
                            time_synchronizeAndUpdate();
                        }
                    }
                });
            }
        });
    }


    public void connectToWifiAndSynchronizeTimeAndData()
    {
        MainActivity.log("OBSystemsManager.connectToWifiAndSynchronizeTimeAndData");
        //asd
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                String wifiSSID = OBConfigManager.sharedManager.getBackupWifiSSID();
                String wifiPassword = OBConfigManager.sharedManager.getBackupWifiPassword();
                //
                connectionManager.connectToNetwork_connectToWifi(wifiSSID, wifiPassword, new OBUtils.RunLambdaWithSuccess()
                {
                    @Override
                    public void run(boolean success) throws Exception
                    {
                        if (success)
                        {
                            MainActivity.log("OBSystemsManager.connectToWifiAndSynchronizeTimeAndData. now connected to the wifi, running completion block");
                            time_synchronizeAndUpdate();
                            backup_uploadDatabase_ftp(true);
                        }
                    }
                });
            }
        });
    }

    public Lock backup_getLock()
    {
        if (backupLock == null)
        {
            backupLock = new ReentrantLock();
        }
        return backupLock;
    }


    public void backup_uploadDatabase_ftp(Boolean disconnectAfter)
    {
        MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp attempting lock");
        if (backup_getLock().tryLock())
        {
            try
            {
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp.generating database backup");
                final boolean freshBackup = true;
                String fileURL = (freshBackup) ? OBSQLiteHelper.getSqlHelper().backupDatabase() : OBSQLiteHelper.getSqlHelper().getLatestDatabaseBackup();
                if (fileURL == null) fileURL = OBSQLiteHelper.getSqlHelper().backupDatabase();
                //
                if (fileURL == null)
                {
                    MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp.could not generate a database backup");
                } else
                {
                    File file = new File(fileURL);
                    FTPClient ftpClient = new FTPClient();
                    //
                    String backupURL = OBConfigManager.sharedManager.getBackupURL();
                    String backupWorkingDirectory = OBConfigManager.sharedManager.getBackupWorkingDirectory();
                    //
                    ftpClient.connect(InetAddress.getByName(backupURL));
                    ftpClient.login("anonymous", "");
                    ftpClient.changeWorkingDirectory(backupWorkingDirectory);
                    //
                    String reply = ftpClient.getReplyString();
                    //
                    if (reply.contains("250") || reply.contains("230"))
                    {
                        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
                        BufferedInputStream buffIn = null;
                        buffIn = new BufferedInputStream(new FileInputStream(file));
                        ftpClient.enterLocalPassiveMode();
                        boolean result = ftpClient.storeFile(file.getName(), new FileInputStream(file));
                        buffIn.close();
                        ftpClient.logout();
                        ftpClient.disconnect();
                        //
                        if (result)
                        {
                            MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp updating lastBackupTimeStamp");
                            long currentTime = System.currentTimeMillis() / 1000;
                            OBPreferenceManager.setPreference("lastBackupTimeStamp", String.format("%d", currentTime));
                            //
                            OBUtils.runOnMainThread(new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    Toast.makeText(MainActivity.mainActivity, "Database has been uploaded to the server", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else
                        {
                            MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp error while uploading FTP file");
                        }
                        //
                        if (disconnectAfter)
                        {
                            MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp. disconnecting Wifi");
                            connectionManager.disconnectWifi();
                        }
                    }
                }
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp exception caught");
                e.printStackTrace();
            }
            //
            MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp releasing lock");
            backup_getLock().unlock();
        } else
        {
            MainActivity.log("OBSystemsManager.backup_uploadDatabase_ftp backup already in progress");
        }
    }


    public void backup_uploadDatabase_php()
    {
        MainActivity.log("OBSystemsManager.backup_uploadDatabase_php attempting lock");
        if (backup_getLock().tryLock())
        {
            try
            {
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_php.generating database backup");
                final boolean freshBackup = true;
                String fileURL = (freshBackup) ? OBSQLiteHelper.getSqlHelper().backupDatabase() : OBSQLiteHelper.getSqlHelper().getLatestDatabaseBackup();
                if (fileURL == null) fileURL = OBSQLiteHelper.getSqlHelper().backupDatabase();
                //
                if (fileURL == null)
                {
                    MainActivity.log("OBSystemsManager.backup_uploadDatabase_php.could not generate a database backup");
                    return;
                }
                //
                String backupURL = OBConfigManager.sharedManager.getBackupURL();
                File file = new File(fileURL);
                URL url = new URL("http://" + backupURL);
                //
                String attachmentName = "file";
                String attachmentFileName = "database.sql";
                String crlf = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                //
                HttpURLConnection httpUrlConnection = null;

                MainActivity.log("OBSystemsManager.backup_uploadDatabase_php.URL: " + url);
                httpUrlConnection = (HttpURLConnection) url.openConnection();
                httpUrlConnection.setUseCaches(false);
                httpUrlConnection.setDoOutput(true);
                //
                httpUrlConnection.setRequestMethod("POST");
                httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                httpUrlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //
                DataOutputStream request = new DataOutputStream(httpUrlConnection.getOutputStream());
                request.writeBytes(twoHyphens + boundary + crlf);
                request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
                request.writeBytes(crlf);
                //
                int size = (int) file.length();
                byte[] bytes = new byte[size];
                BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(file));
                buffer.read(bytes, 0, bytes.length);
                request.write(bytes);
                //
                request.writeBytes(crlf);
                request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                request.flush();
                request.close();
                //
                InputStream responseStream = new BufferedInputStream(httpUrlConnection.getInputStream());
                BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = responseStreamReader.readLine()) != null)
                {
                    stringBuilder.append(line).append("\n");
                }
                responseStreamReader.close();
                //
                String response = stringBuilder.toString();
                //
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_php response from server: " + response);
                //
                responseStream.close();
                httpUrlConnection.disconnect();
                //
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_php updating lastBackupTimeStamp");
                long currentTime = System.currentTimeMillis() / 1000;
                OBPreferenceManager.setPreference("lastBackupTimeStamp", String.format("%d", currentTime));
                //
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_php disconnecting from wifi");

                //
                OBUtils.runOnMainThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        Toast.makeText(MainActivity.mainActivity, "Database has been uploaded to the server", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.backup_uploadDatabase_php exception caught");
                e.printStackTrace();
            }
            MainActivity.log("OBSystemsManager.backup_uploadDatabase_php releasing lock");
            backup_getLock().unlock();
        } else
        {
            MainActivity.log("OBSystemsManager.backup_uploadDatabase_php backup already in progress");
        }
    }


    public static void unregisterReceiver(BroadcastReceiver receiver)
    {
        if (receiver != null)
        {
            try
            {
                MainActivity.mainActivity.unregisterReceiver(receiver);
                MainActivity.log("OBSystemsManager.unregisterReceiver successfull");
            } catch (Exception e)
            {
                MainActivity.log("OBSystemsManager.unregisterReceiver NOT successfull --> not registered");
            }
        } else
        {
            MainActivity.log("OBSystemsManager.unregisterReceiver NOT successfull --> not initialised");
        }
    }

    public Dialog createDateSetDialog(String message, Boolean cancelable, OBUtils.RunLambda completionBlock)
    {
        dateSetCompletionBlock = completionBlock;
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(MainActivity.mainActivity, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        //
        d.setCancelable(cancelable);
        d.setCanceledOnTouchOutside(cancelable);
        //
        if (message == null)
        {
            LinearLayout linearLayout = new LinearLayout(MainActivity.mainActivity.getApplicationContext());
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setCustomTitle(linearLayout);
        } else
        {
            d.setMessage(message + "\n");
        }
        //
        d.setButton(DatePickerDialog.BUTTON_NEGATIVE, null, (DialogInterface.OnClickListener) null);
        //
        DatePicker datePicker = d.getDatePicker();
        calendar.clear();
        calendar.set(2016, Calendar.JANUARY, 1);
        datePicker.setMinDate(calendar.getTimeInMillis());
        calendar.clear();
        calendar.set(2020, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(calendar.getTimeInMillis());
        return d;
    }

    public Dialog createTimeSetDialog(OBUtils.RunLambda completionBlock, final OBUtils.RunLambda cancelCompletionBlock)
    {
        timeSetCompletionBlock = completionBlock;
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog d = new TimePickerDialog(MainActivity.mainActivity, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(MainActivity.mainActivity));
        d.setCancelable(false);
        d.setCanceledOnTouchOutside(false);
        d.setButton(DatePickerDialog.BUTTON_NEGATIVE, "Back", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                OBUtils.runOnMainThread(cancelCompletionBlock);
            }
        });
        d.setMessage("Please set the current time.\n");
        return d;
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
    {
        final Activity activity = MainActivity.mainActivity;
        if (activity != null)
        {
            setDate(activity, year, monthOfYear, dayOfMonth);
        }
        if (dateSetCompletionBlock != null)
        {
            OBUtils.runOnMainThread(dateSetCompletionBlock);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        final Activity activity = MainActivity.mainActivity;
        ;
        if (activity != null)
        {
            setTime(activity, hourOfDay, minute);
        }
        if (timeSetCompletionBlock != null)
        {
            OBUtils.runOnMainThread(timeSetCompletionBlock);
        }
    }

    public static void setDate(Context context, int year, int month, int day)
    {
        Calendar c = Calendar.getInstance();
        //
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();
        //
        if (when / 1000 < Integer.MAX_VALUE)
        {
            try
            {
                ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
            } catch (Exception e)
            {
                MainActivity.log("Exception caught while trying to set the Date");
//                e.printStackTrace();
            }
        }
    }

    public static void setTime(Context context, int hourOfDay, int minute)
    {
        Calendar c = Calendar.getInstance();
        //
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long when = c.getTimeInMillis();
        //
        if (when / 1000 < Integer.MAX_VALUE)
        {
            try
            {
                ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
            } catch (Exception e)
            {
                MainActivity.log("Exception caught while trying to set the Time");
                //e.printStackTrace();
            }
        }
    }


    public void setDateAndTimeDialog(final OBUtils.RunLambda completionBlock)
    {
        createDateSetDialog("Please set the current date.", false, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                createTimeSetDialog(completionBlock, new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        setDateAndTimeDialog(completionBlock);
                    }
                }).show();
            }
        }).show();
    }


    public String getBatterySettingKeyForCurrentLevel()
    {
        float batteryLevel = batteryReceiver.getBatteryLevel();
        //
        for (String levelKey : OBConfigManager.sharedManager.getBatteryLevelKeys())
        {
            float maxLevel = OBConfigManager.sharedManager.getBatteryMaxValueForLevel(levelKey);
            float minLevel = OBConfigManager.sharedManager.getBatteryMinValueForLevel(levelKey);
            //
            if (batteryLevel > minLevel && batteryLevel <= maxLevel)
            {
                MainActivity.log("OBSystemsManager:getBatterySettingKeyForCurrentLevel [" + batteryLevel + "%]: " + levelKey);
                return levelKey;
            }
        }
        //
        MainActivity.log("OBSystemsManager:getBatterySettingKeyForCurrentLevel [" + batteryLevel + "%" + "]: unable to find level key for battery");
        return null;
    }

    /**
     * @return battery level on 0.0-100.0 scale, -1 if no battery data
     */
    public float getBatteryLevel()
    {
        if (batteryReceiver != null)
        {
            return batteryReceiver.getBatteryLevel();
        }
        return -1;
    }

    /**
     * @return true if battery is charging(cable is plugged in)
     */
    public boolean isBatteryCharging()
    {
        if (batteryReceiver != null)
        {
            return batteryReceiver.cablePluggedIn();
        }
        return false;
    }


    public long getNTPTimestamp(String timeServerURL)
    {
        long timestamp = -1;
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(1000);
        //
        try
        {
            client.open();
            InetAddress hostAddr = InetAddress.getByName(timeServerURL);
            //
            TimeInfo info = client.getTime(hostAddr);
            info.computeDetails();
            client.close();
            //
            timestamp = info.getMessage().getReceiveTimeStamp().getTime();
        } catch (SocketException e)
        {
            e.printStackTrace();

        } catch (SocketTimeoutException e)
        {


        } catch (Exception e)
        {

        }

        return timestamp;
    }

    public void setSystemTime(long timestamp)
    {
        try
        {
            ((AlarmManager) MainActivity.mainActivity.getSystemService(Context.ALARM_SERVICE)).setTime(timestamp);
        } catch (Exception e)
        {
            MainActivity.log("OBSystemManager:setSystemTime: Exception caught while trying to set the Date");
            e.printStackTrace();
        }
    }

    public void time_synchronizeAndUpdate()
    {
        if (!OBConfigManager.sharedManager.isTimeServerEnabled()) return;
        //
        final String timeServerURL = OBConfigManager.sharedManager.getTimeServerURL();
        if (timeServerURL != null)
        {
            long timestampMillisec = getNTPTimestamp(timeServerURL);
            MainActivity.log("OBSystemManager:synchronizeAndUpdate: Timestamp received from server: " + timestampMillisec);
            if (timestampMillisec > 0)
            {
                OBFatController fatController = MainActivity.mainActivity.fatController;
                if (fatController != null &&
                        TimeSynchronizationReceiver.class.isAssignableFrom(fatController.getClass()))
                {
                    ((TimeSynchronizationReceiver) fatController).timeReceived(timestampMillisec);
                }
            }
        }
    }

    public interface TimeSynchronizationReceiver
    {
        void timeReceived(long timestampMillisec);
    }


    public List<File> getExternalAssetsFolders()
    {
        List<File> result = new ArrayList();
        //
        String externalAssetsFolderPath = OBConfigManager.sharedManager.getAssetsExternalPath();
        if (externalAssetsFolderPath != null)
        {
            File externalAssets = new File(externalAssetsFolderPath);
            //
            if (externalAssets.exists())
            {
                MainActivity.log("OBSystemsManager.getExternalAssetsFolder.found: " + externalAssets);
                result.add(externalAssets);
            }
            //
            // trying to find path in external storage directories
            File[] externalMediaFolders = MainActivity.mainActivity.getBaseContext().getExternalMediaDirs();
            for (File externalMediaFolder : externalMediaFolders)
            {
                if (externalMediaFolder == null) continue;
                //
                File parentFolder = externalMediaFolder.getParentFile().getParentFile().getParentFile();
                externalAssets = new File(parentFolder + "/" + externalAssetsFolderPath);
                //
                if (externalAssets.exists())
                {
                    MainActivity.log("OBSystemsManager.getExternalAssetsFolder.found: " + externalAssets);
                    result.add(externalAssets);
                }
            }
            //
            // trying to find path in sdcard folder
            externalAssets = new File("/sdcard/" + externalAssetsFolderPath);
            //
            if (externalAssets.exists())
            {
                MainActivity.log("OBSystemsManager.getExternalAssetsFolder.found: " + externalAssets);
                result.add(externalAssets);
            }
            //
            // trying to find path in data folder
            externalAssets = new File("/data/" + externalAssetsFolderPath);
            //
            if (externalAssets.exists())
            {
                MainActivity.log("OBSystemsManager.getExternalAssetsFolder.found: " + externalAssets);
                result.add(externalAssets);
            }
        }
        //
        return result;
    }


    /*public static void disableStatusBar()
    {
        MainActivity.log("OBSystemsManager.disableStatusBar");
        runShellCommand("pm disable com.android.systemui && service call activity 42 s16 com.android.systemui");
    }*/


    /*public static void runShellCommand(String cmd)
    {
        DataOutputStream os;
        try
        {
            Process process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
        } catch (Exception e)
        {
            MainActivity.log("OBSystemsManager.runShellCommand.exception caught: " + e.getMessage());
            e.printStackTrace();
        }
    }*/


}
