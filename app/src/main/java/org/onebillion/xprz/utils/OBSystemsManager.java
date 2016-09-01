package org.onebillion.xprz.utils;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.receivers.OBBatteryReceiver;
import org.onebillion.xprz.receivers.OBSettingsContentObserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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


    public OBSystemsManager ()
    {
        batteryReceiver = new OBBatteryReceiver();
        settingsContentObserver = new OBSettingsContentObserver(MainActivity.mainActivity, new Handler());
        brightnessManager = new OBBrightnessManager();
        expansionManager = new OBExpansionManager();
        connectionManager = new OBConnectionManager();
        //
        sharedManager = this;
    }


    public void runChecks ()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                connectionManager.sharedManager.checkForConnection();
                //
                OBSQLiteHelper.getSqlHelper().runMaintenance();
                //
                runChecksumComparisonTest();
            }
        });
    }


    public void onResume ()
    {
        if (batteryReceiver != null)
        {
            MainActivity.mainActivity.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
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
        if (batteryReceiver != null)
        {
            MainActivity.mainActivity.unregisterReceiver(batteryReceiver);
        }
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


    public void onStop ()
    {
        OBBrightnessManager.sharedManager.onStop();
    }


    public String printBatteryStatus ()
    {
        return batteryReceiver.printStatus();
    }


    public void setBatteryStatusLabel (OBLabel label)
    {
        batteryReceiver.statusLabel = label;
    }


    private void runChecksumComparisonTest ()
    {
        MainActivity.mainActivity.log("Checksum comparison BEGIN");
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
                        MainActivity.mainActivity.log("Checksum comparison: Problem detected with asset " + path);
                    }
//                    MainActivity.mainActivity.log("CHECKSUM COMPARISON --> " + md5 + "\t" + ((is == null) ? "NOT FOUND" : "FOUND" + "\t" + (result ? "OK" : "ERROR") + "\t" + path));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        MainActivity.mainActivity.log("Checksum comparison END");
    }


    public static boolean checkMD5 (String md5, InputStream is)
    {
        if (TextUtils.isEmpty(md5) || is == null)
        {
            MainActivity.mainActivity.log("MD5 string empty or inputStream null");
            return false;
        }
        //
        String calculatedDigest = calculateMD5(is);
        if (calculatedDigest == null)
        {
            MainActivity.mainActivity.log("calculatedDigest null");
            return false;
        }
//        MainActivity.mainActivity.log("Calculated digest: " + calculatedDigest);
//        MainActivity.mainActivity.log("Provided digest: " + md5);
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
            MainActivity.mainActivity.log("Exception while getting digest");
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
                MainActivity.mainActivity.log("Exception on closing MD5 input stream");
                e.printStackTrace();
            }
        }
    }

}
