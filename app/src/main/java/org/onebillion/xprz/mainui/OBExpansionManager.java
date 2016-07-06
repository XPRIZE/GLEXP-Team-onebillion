package org.onebillion.xprz.mainui;

import android.app.DownloadManager;
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
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 06/07/16.
 */
public class OBExpansionManager
{
    public static OBExpansionManager sharedManager;
    public List<OBExpansionFile> internalExpansionFiles;
    public List<OBExpansionFile> remoteExpansionFiles;
    public List<File> mountedExpansionFiles;

    DownloadManager downloadManager;
    long downloadID;
    File obbFilePath;
    StorageManager storageManager;

    public OBExpansionManager()
    {
        internalExpansionFiles = new ArrayList();
        remoteExpansionFiles = new ArrayList();
        mountedExpansionFiles = new ArrayList();
        sharedManager = this;
        getAvailableOBBList();
    }


    public void stopListening()
    {
        MainActivity.mainActivity.unregisterReceiver(downloadCompleteReceiver);
    }




    public void getAvailableOBBList()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                try
                {
                    URL url = new URL(MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL) + "list.xml");
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
                        remoteExpansionFiles.add(new OBExpansionFile(id, type, version));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    private BroadcastReceiver downloadCompleteReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive (Context context, Intent intent)
        {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
            if (id != downloadID)
            {
                MainActivity.mainActivity.log("Ingnoring unrelated download " + id);
                return;
            }
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



    OnObbStateChangeListener eventListener = new OnObbStateChangeListener()
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
                // copy content to internal storage
                File source = new File(storageManager.getMountedObbPath(obbFilePath.getAbsolutePath()));
                String folderName = FilenameUtils.removeExtension(obbFilePath.getName());
                File destination = new File(MainActivity.mainActivity.getFilesDir() + File.separator + folderName);
                destination.mkdirs();
                try
                {
                    FileUtils.copyDirectory(source, destination);
                    //
                    MainActivity.mainActivity.addToPreferences("externalAssets", folderName);
                    addExpansionAssetsFolder(folderName);
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



    protected void unpackOBB (String filePath)
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
                //
                MainActivity.mainActivity.addToPreferences("externalAssets", null);
                downloadOBB();
            }
        }
        else
        {
            MainActivity.mainActivity.log("Permission required. re-attempting after user gives permission");
        }
    }



    protected void downloadOBB ()
    {
        String externalAssets = MainActivity.mainActivity.getPreferences("externalAssets");
        if (externalAssets == null)
        {
            if (MainActivity.mainActivity.isStoragePermissionGranted())
            {
                MainActivity.mainActivity.log("downloading OBB");
                MainActivity.mainActivity.registerReceiver(downloadCompleteReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                //
                String url = MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL) + "my-app-assets.obb"; // needs to be dynamic, grab the list and pick the file
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription("XPRZ0 assets");
                request.setTitle("Downloading assets");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "my-app-assets.obb"); // needs to be dynamic, grab the name from the list
                if (downloadManager == null)
                    downloadManager = (DownloadManager) MainActivity.mainActivity.getSystemService(Context.DOWNLOAD_SERVICE);
                downloadID = downloadManager.enqueue(request);
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


    public void addExpansionAssetsFolder (String folderName)
    {
        File expansionFile = new File(MainActivity.mainActivity.getFilesDir() + File.separator + folderName);
        mountedExpansionFiles.add(expansionFile);
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
