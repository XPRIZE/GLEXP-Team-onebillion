package org.onebillion.xprz.utils;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.ObbInfo;
import android.content.res.ObbScanner;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.onebillion.xprz.mainui.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 06/07/16.
 */
public class OBExpansionManager
{
    public static OBExpansionManager sharedManager;
    public Map<String, OBExpansionFile> internalExpansionFiles;
    public Map<String, OBExpansionFile> remoteExpansionFiles;
    private Map<Long, String> downloadQueue;

    private DownloadManager downloadManager;
    private File obbFilePath;
    private StorageManager storageManager;
    private Boolean setupComplete;
    private ProgressDialog waitDialog;
    private OBUtils.RunLambda completionBlock;
    private OnObbStateChangeListener eventListener = new OnObbStateChangeListener()
    {
        @Override
        public void onObbStateChange (String path, int state)
        {
            if (state == OnObbStateChangeListener.ERROR_COULD_NOT_MOUNT)
            {
                updateProgressDialog("Error: could not mount OBB file", true);
                MainActivity.log("Could not mount OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_ALREADY_MOUNTED)
            {
                MainActivity.log("Already mounted OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.MOUNTED)
            {
                MainActivity.log("Mounted OBB file " + path);
                //
                File source = new File(storageManager.getMountedObbPath(obbFilePath.getAbsolutePath()));
                String folderName = FilenameUtils.removeExtension(obbFilePath.getName());
                File destination = new File(MainActivity.mainActivity.getFilesDir() + File.separator + folderName);
                destination.mkdirs();
                try
                {
                    FileUtils.copyDirectory(source, destination, false);
                    //
                    String currentExternalAssets = MainActivity.mainActivity.getPreferences("externalAssets");
                    if (currentExternalAssets == null)
                    {
                        MainActivity.mainActivity.addToPreferences("externalAssets", folderName);
                        addExpansionAssetsFolder(folderName);
                    }
                    else
                    {
                        List<String> externalAssets = new ArrayList(Arrays.asList(currentExternalAssets.split(",")));
                        if (!externalAssets.contains(folderName))
                        {
                            externalAssets.add(folderName);
                            StringBuilder newExternalAssets = new StringBuilder();
                            //
                            for (String folder : externalAssets)
                            {
                                newExternalAssets.append(folder + ",");
                            }
                            MainActivity.mainActivity.addToPreferences("externalAssets", newExternalAssets.toString());
                            addExpansionAssetsFolder(folderName);
                        }
                    }
                    checkIfSetupIsComplete();
                    //
                    storageManager.unmountObb(obbFilePath.getPath(), true, eventListener);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (state == OnObbStateChangeListener.UNMOUNTED)
            {
                MainActivity.log("Unmounted OBB file " + path);
                //
                try
                {
                    boolean deleted = obbFilePath.delete();
                    if (!deleted)
                    {
                        MainActivity.log("unable to delete downloaded file");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (state == OnObbStateChangeListener.ERROR_PERMISSION_DENIED)
            {
                updateProgressDialog("Error: could not mount OBB file. Permission Denied.", true);
                MainActivity.log("Permission Denied " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_COULD_NOT_UNMOUNT)
            {
                MainActivity.log("Could not unmount OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_INTERNAL)
            {
                updateProgressDialog("Error: could not mount OBB file. Internal Error.", true);
                MainActivity.log("Internal Error " + path);
            }
            else
            {
                updateProgressDialog("Error: could not mount OBB file. Unkown Error.", true);
                MainActivity.log("Unknown Error " + path);
            }
        }
    };
    public BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (!downloadQueue.containsKey(id))
            {
                MainActivity.log("Ignoring unrelated download " + id);
                return;
            }
            downloadQueue.remove(id);
            //
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst())
            {
                MainActivity.log("Empty row");
                cursor.close();
                checkIfSetupIsComplete(); // needs to be checked if this is the correct solution
                return;
            }
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex))
            {
                MainActivity.log("Download Failed");
                cursor.close();
                checkIfSetupIsComplete(); // needs to be checked if this is the correct solution
                return;
            }
            NotificationManager notifManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancelAll();
            //
            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String downloadedPackageUriString = cursor.getString(uriIndex);
            cursor.close();
            //
            unpackOBB(downloadedPackageUriString);
        }
    };


    public OBExpansionManager ()
    {
        downloadQueue = new HashMap();
        remoteExpansionFiles = new HashMap();
        sharedManager = this;
        setupComplete = false;
        /*
        <key>expansionURL</key>
	    <string>http://ting.onebillion.org:5007/obb/</string>
         */
    }

    private void mountAvailableExpansionFolders ()
    {
        String currentExternalAssets = MainActivity.mainActivity.getPreferences("externalAssets");
        if (currentExternalAssets != null)
        {
            List<String> externalAssets = new ArrayList(Arrays.asList(currentExternalAssets.split(",")));
            for (String folder : externalAssets)
            {
                addExpansionAssetsFolder(folder);
            }
        }
    }

    public void checkIfSetupIsComplete ()
    {
        if (downloadQueue.size() == 0)
        {
            setupComplete = true;
            MainActivity.log("ExpansionManager has downloaded all the files it requires to continue.");
            //
            if (waitDialog != null)
            {
                waitDialog.dismiss();
                waitDialog.cancel();
            }
            if (completionBlock != null)
            {
                OBSystemsManager.sharedManager.onContinue();
                //
                OBUtils.runOnMainThread(completionBlock);
            }
        }
    }

    public void waitForDownload ()
    {
        try
        {
            if (waitDialog == null)
            {
                waitDialog = new ProgressDialog(MainActivity.mainActivity);
            }
            waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            updateProgressDialog("Downloading assets. Please wait...", false);
            waitDialog.setIndeterminate(true);
            waitDialog.setCanceledOnTouchOutside(false);
            waitDialog.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<File> getExternalExpansionFolders ()
    {
        List<File> result = new ArrayList();
        if (internalExpansionFiles != null)
        {
            for (OBExpansionFile file : internalExpansionFiles.values())
            {
                if (file.folder != null) result.add(file.folder);
            }
        }
        return result;
    }

    public List<File> getChecksumFiles()
    {
        List<File> result = new ArrayList();
        //
        checkForInternalExpansionFiles();
        //
        if (internalExpansionFiles != null)
        {
            for (OBExpansionFile file : internalExpansionFiles.values())
            {
                if (file.folder != null)
                {
                    File checksum = new File(file.folder, "checksum.xml");
                    if (checksum.exists())
                    {
                        result.add(checksum);
                    }
                }
            }
        }
        return result;
    }


    private void checkForInternalExpansionFiles()
    {
        if (internalExpansionFiles == null)
        {
            internalExpansionFiles = new HashMap<String, OBExpansionFile>();
            mountAvailableExpansionFolders();
        }
    }

    public void checkForUpdates (OBUtils.RunLambda whenComplete)
    {
        OBSystemsManager.sharedManager.onSuspend();
        completionBlock = whenComplete;
        //
        final String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
        if (expansionURL == null)
        {
            MainActivity.log("Expansion URL is null. nothing to do here");
            checkIfSetupIsComplete();
            return;
        }
        //
        if (!MainActivity.mainActivity.isStoragePermissionGranted())
        {
            MainActivity.log("User hasn't given permissions. Suspending operations until resolve");
            // Setup is not complete here
            return;
        }
        //
        checkForInternalExpansionFiles();
        //
        waitForDownload();
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                try
                {
                    URL url = new URL(expansionURL + "list.xml");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();
                    OBXMLManager xmlManager = new OBXMLManager();
                    List<OBXMLNode> xml = xmlManager.parseFile(urlConnection.getInputStream());
                    OBXMLNode rootNode = xml.get(0);
                    for (OBXMLNode xmlNode : rootNode.children)
                    {
                        String id = xmlNode.attributeStringValue("id");
                        String bundle = xmlNode.attributeStringValue("bundle");
                        String destination = xmlNode.attributeStringValue("destination");
                        long version = xmlNode.attributeLongValue("version");
                        if (id.length() > 0)
                        {
                            remoteExpansionFiles.put(id, new OBExpansionFile(id, bundle, destination, version, null));
                        }
                    }
                    //
                    compareExpansionFilesAndInstallMissingOrOutdated();
                }
                catch (UnknownHostException e)
                {
                    updateProgressDialog(e.getMessage(), true);
                    checkIfSetupIsComplete();
                    return;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    updateProgressDialog(e.getMessage(), true);
                    checkIfSetupIsComplete();
                    return;
                }
            }
        });
    }

    private void compareExpansionFilesAndInstallMissingOrOutdated ()
    {
//        MainActivity.log("compareExpansionFilesAndInstallMissingOrOutdated");
        //
        checkForInternalExpansionFiles();
        //
        if (internalExpansionFiles == null) return;
        //
        if (remoteExpansionFiles == null)
        {
            remoteExpansionFiles = new HashMap();
        }
        //
        for (OBExpansionFile remoteFile : remoteExpansionFiles.values())
        {
            if (remoteFile == null) continue;
            MainActivity.log("checking: " + remoteFile.id + " " + remoteFile.version + " " + remoteFile.bundle);
            //
            if (!MainActivity.mainActivity.getPackageName().equals(remoteFile.bundle))
            {
                MainActivity.log("Mismatched bundleID, continuing");
                continue;
            }
            //
            Boolean needsUpdate = true;
            OBExpansionFile internalFile = internalExpansionFiles.get(remoteFile.id);
            if (internalFile != null && internalFile.id.equals(remoteFile.id) && internalFile.version >= remoteFile.version)
            {
                needsUpdate = false;
            }
            //
            if (needsUpdate)
            {
                MainActivity.log("Download required --> " + remoteFile.id + " local:" + (internalFile != null ? internalFile.version : "missing") + "   remote:" + remoteFile.version);
                downloadOBB(remoteFile.id);
            }
            else
            {
                MainActivity.log("Download NOT required --> " + remoteFile.id + " local:" + (internalFile != null ? internalFile.version : "missing") + "   remote:" + remoteFile.version);
            }
        }
        checkIfSetupIsComplete();
    }

    private void unpackOBB (String filePath)
    {
        updateProgressDialog("Unpacking assets", false);
        //
        if (MainActivity.mainActivity.isStoragePermissionGranted())
        {
            String password = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_OBB_PASSWORD); // due to a bug in the Android SDK we cannot use encryted OBB files --> http://stackoverflow.com/questions/21161475/can-mount-unencrypted-obb-but-with-encrypted-error-21/30301701#30301701
            storageManager = (StorageManager) MainActivity.mainActivity.getSystemService(Context.STORAGE_SERVICE);
            //
            try
            {
                File downloadedFile = new File(filePath);
                obbFilePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + downloadedFile.getName());
                //
                ObbInfo info = ObbScanner.getObbInfo(obbFilePath.getAbsolutePath());
                String packageName = info.packageName;
                String file = info.filename;
                MainActivity.log("Info from downloaded OBB: " + packageName + " " + file);
                storageManager.mountObb(obbFilePath.getPath(), null, eventListener);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            MainActivity.log("Permission required. re-attempting after user gives permission");
        }
    }


    private void downloadOBB (String fileName)
    {
        String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
        //
        if (expansionURL == null)
        {
            MainActivity.log("Expansion URL is null. nothing to do here");
        }
        //
        MainActivity.log("Attempting to download OBB " + fileName + ".obb");
        if (MainActivity.mainActivity.isStoragePermissionGranted())
        {
            MainActivity.log("downloading OBB");
            //
            String url = expansionURL + fileName + ".obb";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("XPRZ0 assets");
            request.setTitle("Downloading assets");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            }
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName + ".obb");
            if (downloadManager == null)
                downloadManager = (DownloadManager) MainActivity.mainActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            long downloadID = downloadManager.enqueue(request);
            downloadQueue.put(downloadID, fileName);
        }
        else
        {
            MainActivity.log("User didn't give permission to access storage");
        }
    }


    private void addExpansionAssetsFolder (String folderName)
    {
        try
        {
            File folder = new File(MainActivity.mainActivity.getFilesDir() + File.separator + folderName);
            File versionXML = new File(folder.getAbsolutePath() + File.separator + "version.xml");
            //
            OBXMLManager manager = new OBXMLManager();
            FileInputStream xmlFile = new FileInputStream(versionXML);
            List<OBXMLNode> xml = manager.parseFile(xmlFile);
            OBXMLNode rootNode = xml.get(0);
            //
            String id = rootNode.attributeStringValue("id");
            String bundle = rootNode.attributeStringValue("bundle");
            String destination = rootNode.attributeStringValue("destination");
            long version = rootNode.attributeLongValue("version");
            //
            OBExpansionFile expansionFile = new OBExpansionFile(id, bundle, destination, version, folder);
            internalExpansionFiles.put(expansionFile.id, expansionFile);
            //
            MainActivity.log("Expansion Folder Installed: " + id + " --> " + folder.getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void updateProgressDialog(String message, Boolean killDialog)
    {
        if (waitDialog != null)
        {
            waitDialog.setMessage(message);
        }
        if (killDialog)
        {
            waitDialog.setCanceledOnTouchOutside(true);
        }
    }


//    protected void moveDownloadedFileToInternalStorage (String filePath) throws IOException
//    {
//        File downloadedFile = new File(filePath);
//        File source = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + downloadedFile.getName());
//        File destinationFolder = new File(getFilesDir().getAbsoluteFile() + File.separator + "obb" + File.separator);
//        destinationFolder.mkdirs();
//        File destination = new File(destinationFolder.getAbsolutePath() + File.separator + source.getName());
//        //
//        FileChannel inChannel = new FileInputStream(source).getChannel();
//        FileChannel outChannel = new FileOutputStream(destination).getChannel();
//        //
//        try
//        {
//            inChannel.transferTo(0, inChannel.size(), outChannel);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        finally
//        {
//            if (inChannel != null) inChannel.close();
//            if (outChannel != null) outChannel.close();
//            //
//            boolean deleted = source.delete();
//            if (!deleted)
//            {
//                Log.v(TAG, "unable to delete downloaded file");
//            }
//        }
//        //
//        unpackOBB(destination.getName());
//    }


}
