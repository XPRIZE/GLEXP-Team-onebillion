package org.onebillion.xprz.mainui;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
                MainActivity.mainActivity.log("Could not mount OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_ALREADY_MOUNTED)
            {
                MainActivity.mainActivity.log("Already mounted OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.MOUNTED)
            {
                MainActivity.mainActivity.log("Mounted OBB file " + path);
                //
                File source = new File(storageManager.getMountedObbPath(obbFilePath.getAbsolutePath()));
                String folderName = FilenameUtils.removeExtension(obbFilePath.getName());
                File destination = new File(MainActivity.mainActivity.getFilesDir() + File.separator + folderName);
                destination.mkdirs();
                try
                {
                    FileUtils.copyDirectory(source, destination);
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
                            for (String folder : externalAssets)
                            {
                                externalAssets.add(folder + ",");
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
                MainActivity.mainActivity.log("Unmounted OBB file " + path);
                //
                try
                {
                    boolean deleted = obbFilePath.delete();
                    if (!deleted)
                    {
                        MainActivity.mainActivity.log("unable to delete downloaded file");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else if (state == OnObbStateChangeListener.ERROR_PERMISSION_DENIED)
            {
                MainActivity.mainActivity.log("Permission Denied " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_COULD_NOT_UNMOUNT)
            {
                MainActivity.mainActivity.log("Could not unmount OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_INTERNAL)
            {
                MainActivity.mainActivity.log("Internal Error " + path);
            }
            else
            {
                MainActivity.mainActivity.log("Unknown Error " + path);
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
                MainActivity.mainActivity.log("Ignoring unrelated download " + id);
                return;
            }
            downloadQueue.remove(id);
            //
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = downloadManager.query(query);
            if (!cursor.moveToFirst())
            {
                MainActivity.mainActivity.log("Empty row");
                return;
            }
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (DownloadManager.STATUS_SUCCESSFUL != cursor.getInt(statusIndex))
            {
                MainActivity.mainActivity.log("Download Failed");
                return;
            }

            int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
            String downloadedPackageUriString = cursor.getString(uriIndex);
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

    public void checkIfSetupIsComplete()
    {
        if (downloadQueue.size() == 0)
        {
            setupComplete = true;
            MainActivity.mainActivity.log("ExpansionManager has downloaded all the files it requires to continue.");
            //
            if (waitDialog != null)
            {
                waitDialog.dismiss();
                waitDialog.cancel();
            }
            if (completionBlock != null)
            {
                OBUtils.runOnMainThread(completionBlock);
            }
        }
    }

    public void waitForDownload()
    {
        if (waitDialog == null)
        {
            waitDialog = new ProgressDialog(MainActivity.mainActivity);
        }
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        waitDialog.setMessage("Downloading assets. Please wait...");
        waitDialog.setIndeterminate(true);
        waitDialog.setCanceledOnTouchOutside(false);
        waitDialog.show();
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

    public void checkForUpdates(OBUtils.RunLambda whenComplete)
    {
        completionBlock = whenComplete;
        //
        final String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
        if (expansionURL == null)
        {
            MainActivity.mainActivity.log("Expansion URL is null. nothing to do here");
            checkIfSetupIsComplete();
            return;
        }
        //
        if (!MainActivity.mainActivity.isStoragePermissionGranted())
        {
            MainActivity.mainActivity.log("User hasn't given permissions. Suspending operations until resolve");
            // Setup is not complete here
            return;
        }
        //
        if (internalExpansionFiles == null)
        {
            internalExpansionFiles = new HashMap<String, OBExpansionFile>();
            mountAvailableExpansionFolders();
        }
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
                        String type = xmlNode.attributeStringValue("type");
                        int version = xmlNode.attributeIntValue("version");
                        remoteExpansionFiles.put(id, new OBExpansionFile(id, type, version, null));
                    }
                    //
                    compareExpansionFilesAndInstallMissingOrOutdated();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        //
        waitForDownload();
    }

    private void compareExpansionFilesAndInstallMissingOrOutdated ()
    {
        MainActivity.mainActivity.log("CompareExpansionFilesAndInstallMissingOrOutdate");
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
            MainActivity.mainActivity.log("checking: " + remoteFile.id + " " + remoteFile.version);
            //
            Boolean needsUpdate = true;
            OBExpansionFile internalFile = internalExpansionFiles.get(remoteFile.id);
            if (internalFile != null && internalFile.id.equals(remoteFile.id) && internalFile.version == remoteFile.version)
            {
                needsUpdate = false;
            }
            //
            if (needsUpdate)
            {
                MainActivity.mainActivity.log("Download required --> " + remoteFile.id + " local:" + (internalFile != null ? internalFile.version : "missing") + "   remote:" + remoteFile.version);
                downloadOBB(remoteFile.id);
            }
            else
            {
                MainActivity.mainActivity.log("Download NOT required --> " + remoteFile.id + " local:" + (internalFile != null ? internalFile.version : "missing") + "   remote:" + remoteFile.version);
            }
        }
        checkIfSetupIsComplete();
    }

    private void unpackOBB (String filePath)
    {
        if (MainActivity.mainActivity.isStoragePermissionGranted())
        {
            String password = null; // "4asterix";
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
                MainActivity.mainActivity.log("Info from downloaded OBB: " + packageName + " " + file);
                storageManager.mountObb(obbFilePath.getPath(), password, eventListener);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            MainActivity.mainActivity.log("Permission required. re-attempting after user gives permission");
        }
    }


    private void downloadOBB (String fileName)
    {
        String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
        //
        if (expansionURL == null)
        {
            MainActivity.mainActivity.log("Expansion URL is null. nothing to do here");
        }
        //
        MainActivity.mainActivity.log("Attempting to download OBB " + fileName + ".obb");
        String externalAssets = MainActivity.mainActivity.getPreferences("externalAssets");
        if (externalAssets == null)
        {
            if (MainActivity.mainActivity.isStoragePermissionGranted())
            {
                MainActivity.mainActivity.log("downloading OBB");
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
                MainActivity.mainActivity.log("User didn't give permission to access storage");
            }
        }
        else
        {
            MainActivity.mainActivity.log("already downloaded external assets. nothing to do here");
            //
            addExpansionAssetsFolder(externalAssets);
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
            String type = rootNode.attributeStringValue("type");
            int version = rootNode.attributeIntValue("version");
            //
            OBExpansionFile expansionFile = new OBExpansionFile(id, type, version, folder);
            internalExpansionFiles.put(expansionFile.id, expansionFile);
            //
            MainActivity.mainActivity.log("Expansion Folder Installed: " + id + " --> " + folder.getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
