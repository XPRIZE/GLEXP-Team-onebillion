package org.onebillion.onecourse.utils;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ObbInfo;
import android.content.res.ObbScanner;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.onebillion.onecourse.mainui.MainActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    private int unpackFileCounter, unpackFileTotal;
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
                //
                checkIfSetupIsComplete();
            }
            else if (state == OnObbStateChangeListener.ERROR_ALREADY_MOUNTED)
            {
                MainActivity.log("Already mounted OBB file " + path);
                //
                checkIfSetupIsComplete();
            }
            else if (state == OnObbStateChangeListener.MOUNTED)
            {
                MainActivity.log("Mounted OBB file " + path);
                //
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        File source = new File(storageManager.getMountedObbPath(obbFilePath.getAbsolutePath()));
                        String folderName = FilenameUtils.removeExtension(obbFilePath.getName());
                        File destination = new File(MainActivity.mainActivity.getFilesDir() + File.separator + folderName);
                        destination.mkdirs();
                        try
                        {
                            IOFileFilter acceptAllFileFilter = new IOFileFilter()
                            {
                                @Override
                                public boolean accept (File file)
                                {
                                    return true;
                                }

                                @Override
                                public boolean accept (File file, String s)
                                {
                                    return true;
                                }
                            };
                            //
                            Collection<File> result = FileUtils.listFilesAndDirs(source, acceptAllFileFilter, acceptAllFileFilter);
                            unpackFileTotal = result.size();
                            unpackFileCounter = 0;
                            //
                            OBUtils.runOnMainThread(new OBUtils.RunLambda()
                            {
                                @Override
                                public void run () throws Exception
                                {
                                    if (waitDialog != null)
                                    {
                                        waitDialog.dismiss();
                                        waitDialog.cancel();
                                    }
                                    waitDialog = new ProgressDialog(MainActivity.mainActivity);
                                    waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    waitDialog.setCanceledOnTouchOutside(false);
                                    waitDialog.setMessage("Copying assets to internal storage");
                                    waitDialog.setProgress(unpackFileCounter);
                                    waitDialog.setMax(unpackFileTotal);
                                    waitDialog.show();
                                }
                            });
                            //
                            MainActivity.log("OBExpansionManager.eventListener: copying " + unpackFileTotal + " files/folders");
                            //
                            FileFilter counterFileFilter = new FileFilter()
                            {
                                @Override
                                public boolean accept (File pathname)
                                {
                                    updateProgressDialog_unpack();
                                    return true;
                                }
                            };
                            //
                            FileUtils.copyDirectory(source, destination, counterFileFilter, false);
                            //
//                            FileUtils.copyDirectory(source, destination, false);
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
                });
            }
            else if (state == OnObbStateChangeListener.UNMOUNTED)
            {
                MainActivity.log("Unmounted OBB file " + path);
                if (path.contains(internalOBBFile().replace("//", "/")))
                {
                    MainActivity.log("OBExpansionManager.preventing deletion of OBB file in protected folder");
                }
                else
                {
                    try
                    {
                        boolean deleted = obbFilePath.delete();
                        if (!deleted)
                        {
                            MainActivity.log("OBExpansionManager.unable to delete downloaded file: " + path);
                        }
                        else
                        {
                            MainActivity.log("OBExpansionManager.file deleted" + path);
                        }
                    }
                    catch (Exception e)
                    {
                        MainActivity.log("OBExpansionManager.exception caught while trying to delete a file.");
                        e.printStackTrace();
                    }
                }
            }
            else if (state == OnObbStateChangeListener.ERROR_PERMISSION_DENIED)
            {
                updateProgressDialog("Error: could not mount OBB file. Permission Denied.", true);
                MainActivity.log("Permission Denied " + path);
                //
                checkIfSetupIsComplete();
            }
            else if (state == OnObbStateChangeListener.ERROR_COULD_NOT_UNMOUNT)
            {
                MainActivity.log("Could not unmount OBB file " + path);
            }
            else if (state == OnObbStateChangeListener.ERROR_INTERNAL)
            {
                updateProgressDialog("Error: could not mount OBB file. Internal Error.", true);
                MainActivity.log("Internal Error " + path);
                //
                checkIfSetupIsComplete();
            }
            else
            {
                updateProgressDialog("Error: could not mount OBB file. Unkown Error.", true);
                MainActivity.log("Unknown Error " + path);
                //
                checkIfSetupIsComplete();
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
            //
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            StatusBarNotification notifications[] = notificationManager.getActiveNotifications();
            MainActivity.log("OBExpansionManager.current notifications (BEFORE)");
            for (StatusBarNotification notification : notifications)
            {
                MainActivity.log(notification.toString());
            }
            //
            MainActivity.log("OBExpansionManager.cancelling all notifications");
            notificationManager.cancelAll();
            //
            notifications = notificationManager.getActiveNotifications();
            MainActivity.log("OBExpansionManager.current notifications (AFTER)");
            for (StatusBarNotification notification : notifications)
            {
                MainActivity.log(notification.toString());
            }
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
        MainActivity.log("OBExpansionManager.checkIfSetupIsComplete");
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
                MainActivity.log("OBExpansionManager.calling onContinue");
                OBSystemsManager.sharedManager.onContinue();
                //
                MainActivity.log("OBExpansionManager.running completionBlock");
                OBUtils.runOnMainThread(completionBlock);
                MainActivity.log("OBExpansionManager.running completionBlock complete");
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
//            waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            updateProgressDialog("Downloading assets. Please wait...", false);
//            waitDialog.setIndeterminate(true);
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


    public boolean checkForBundledOBB()
    {
        final PackageManager pm = MainActivity.mainActivity.getPackageManager();
        List<ApplicationInfo> packages =  pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo packageInfo : packages)
        {
            if (packageInfo.packageName.equals(MainActivity.mainActivity.getPackageName()))
            {
                MainActivity.log("OBExpansionManager.checkForBundledOBB.package found: " + packageInfo.sourceDir);
                final File possibleFile = new File(new File(packageInfo.sourceDir).getParentFile(), internalOBBFile());
                if (possibleFile.exists())
                {
                    MainActivity.log("OBExpansionManager. internal file exists.");
                    final File externalFile = new File(Environment.getExternalStorageDirectory(), "onebillion/" + internalOBBFile());
                    if (!externalFile.exists())
                    {
                        MainActivity.log("OBExpansionManager. external file does NOT exist. moving internal to external");
                        OBUtils.runOnMainThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run () throws Exception
                            {
                                if (waitDialog != null)
                                {
                                    waitDialog.dismiss();
                                    waitDialog.cancel();
                                }
                                waitDialog = new ProgressDialog(MainActivity.mainActivity);
                                waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                waitDialog.setCanceledOnTouchOutside(false);
                                waitDialog.setMessage("First run detected. Setting up OBB file. Please wait");
                                waitDialog.show();
                            }
                        });
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run () throws Exception
                            {
                                try
                                {
                                    FileUtils.copyFile(possibleFile, externalFile);
                                    unpackOBB(externalFile.getAbsolutePath());
                                }
                                catch (Exception e)
                                {
                                    MainActivity.log("OBExpansionManager.exception caught while copying OBB file from internal to external storage");
                                    e.printStackTrace();
                                }
                            }
                        });
                        return true;
                    }
                    else
                    {
                        MainActivity.log("OBExpansionManager.copy not needed. File already exists");
                    }
                    //
                    MainActivity.log("OBExpansionManager.checkForBundledOBB: file found");
                    unpackOBB(externalFile.getAbsolutePath());
                    return true;
                }
            }
        }
        MainActivity.log("OBExpansionManager.checkForBundledOBB: nothing found");
        return false;
    }


    public String internalOBBFile()
    {
        return MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_BUNDLED_OBB_FILENAME);
    }

    public void checkForUpdates (OBUtils.RunLambda whenComplete)
    {
        MainActivity.log("OBExpansionManager.checkForUpdates");
        OBSystemsManager.sharedManager.onSuspend();
        completionBlock = whenComplete;
        //
        final String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
        if (expansionURL == null)
        {

            MainActivity.log("OBExpansionManager.checkForUpdates: Expansion URL is null. nothing to do here");
            checkIfSetupIsComplete();
            return;
        }
        //
        if (!MainActivity.mainActivity.isStoragePermissionGranted())
        {
            MainActivity.log("OBExpansionManager.checkForUpdates: User hasn't given permissions. Suspending operations until resolve");
            // Setup is not complete here
            return;
        }
        //
        checkForInternalExpansionFiles();
        //
        waitForDownload();
        //
        if (internalExpansionFiles.isEmpty())
        {
            MainActivity.log("OBExpansionManager.checkForUpdates.the internal expansion files are empty. checking for bundled OBB");
            if (checkForBundledOBB())
            {
                MainActivity.log("OBExpansionManager.checkForUpdates.there is a bundled OBB");
                return;
            }
            else
            {
                MainActivity.log("OBExpansionManager.checkForUpdates.there is NO bundled OBB");
                checkForRemoteOBB();
            }
        }
        else
        {
            checkForRemoteOBB();
        }
    }

    private void compareExpansionFilesAndInstallMissingOrOutdated ()
    {
//        MainActivity.log("compareExpansionFilesAndInstallMissingOrOutdated");
        //
        checkForInternalExpansionFiles();
        downloadQueue.clear();
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
            //
            if (!MainActivity.mainActivity.getPackageName().equals(remoteFile.bundle))
            {
                continue;
            }
            //
            MainActivity.log("OBExpansionManager.compareExpansionFilesAndInstallMissingOrOutdated: checking: " + remoteFile.id + " " + remoteFile.version + " " + remoteFile.bundle);
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
                MainActivity.log("OBExpansionManager.compareExpansionFilesAndInstallMissingOrOutdated: Download required --> " + remoteFile.id + " local:" + (internalFile != null ? internalFile.version : "missing") + "   remote:" + remoteFile.version);
                downloadOBB(remoteFile.id);
            }
            else
            {
                MainActivity.log("OBExpansionManager.compareExpansionFilesAndInstallMissingOrOutdated: Download NOT required --> " + remoteFile.id + " local:" + (internalFile != null ? internalFile.version : "missing") + "   remote:" + remoteFile.version);
            }
        }
        checkIfSetupIsComplete();
    }

    private void unpackOBB (final String filePath)
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                if (waitDialog != null)
                {
                    waitDialog.dismiss();
                    waitDialog.cancel();
                }
                waitDialog = new ProgressDialog(MainActivity.mainActivity);
                waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                waitDialog.setCanceledOnTouchOutside(false);
                waitDialog.setMessage("Mounting OBB file");
                waitDialog.show();
            }
        });
        //
        MainActivity.log("OBExpansionManager.unpackOBB: " + filePath);
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                if (MainActivity.mainActivity.isStoragePermissionGranted())
                {
                    String password = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_OBB_PASSWORD); // due to a bug in the Android SDK we cannot use encrypted OBB files --> http://stackoverflow.com/questions/21161475/can-mount-unencrypted-obb-but-with-encrypted-error-21/30301701#30301701
                    storageManager = (StorageManager) MainActivity.mainActivity.getSystemService(Context.STORAGE_SERVICE);
                    //
                    try
                    {
                        File downloadedFile = new File(filePath);
                        obbFilePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + downloadedFile.getName());
                        if (!obbFilePath.exists())
                        {
                            obbFilePath = new File(filePath);
                        }
                        if (obbFilePath.exists())
                        {
                            ObbInfo info = ObbScanner.getObbInfo(obbFilePath.getAbsolutePath());
                            String packageName = info.packageName;
                            String file = info.filename;
                            MainActivity.log("OBExpansionManager.unpackOBB: Info from unpacked OBB: " + packageName + " " + obbFilePath.getAbsolutePath());
                            storageManager.mountObb(obbFilePath.getPath(), null, eventListener);
                        }
                        else
                        {
                            MainActivity.log("OBExpansionManager.unpackOBB: unable to find specified OBB file: " + filePath);
                            //
                            waitDialog.dismiss();
                            waitDialog.cancel();
                            //
                            Toast.makeText(MainActivity.mainActivity, "Unable to find specified OBB file", Toast.LENGTH_LONG).show();
                            //
                            MainActivity.mainActivity.finish();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    MainActivity.log("OBExpansionManager.unpackOBB: Permission required. re-attempting after user gives permission");
                }
            }
        });
    }


    private void downloadOBB (String fileName)
    {
        String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
        //
        if (expansionURL == null)
        {
            MainActivity.log("OBExpansionManager.downloadOBB: Expansion URL is null. nothing to do here");
        }
        //
        MainActivity.log("OBExpansionManager.downloadOBB: Attempting to download OBB " + fileName + ".obb");
        if (MainActivity.mainActivity.isStoragePermissionGranted())
        {
            MainActivity.log("OBExpansionManager.downloadOBB: downloading OBB");
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
            {
                downloadManager = (DownloadManager) MainActivity.mainActivity.getSystemService(Context.DOWNLOAD_SERVICE);
            }
            //
            // grab pending downloads and cancel all of them
            Cursor c = downloadManager.query(new DownloadManager.Query().setFilterByStatus(DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING));
            while (c.moveToNext())
            {
                long id = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID));
                MainActivity.log("OBExpansionManager.downloadOBB: cancelling download " + id);
                downloadManager.remove(id);
            }
            c.close();
            //
            final long downloadID = downloadManager.enqueue(request);
            downloadQueue.put(downloadID, fileName);
            //
            new Thread(new Runnable()
            {
                @Override
                public void run ()
                {
                    boolean downloading = true;
                    while (downloading)
                    {
                        try
                        {
                            DownloadManager.Query q = new DownloadManager.Query();
                            q.setFilterById(downloadID);
                            //
                            Cursor cursor = downloadManager.query(q);
                            if (cursor.moveToFirst())
                            {
                                final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                                final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                                //
                                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL)
                                {
                                    downloading = false;
                                }
                                //
                                final int dl_progress = (int) ((double) bytes_downloaded / (double) bytes_total * 100f);
                                //
                                OBUtils.runOnMainThread(new OBUtils.RunLambda()
                                {
                                    @Override
                                    public void run () throws Exception
                                    {
                                        waitDialog.setMax(Math.round(bytes_total / (1024f * 1024f)));
                                        waitDialog.setProgress(Math.round(bytes_downloaded / (1024f * 1024f)));
                                        //waitDialog.setProgress(dl_progress);
                                    }
                                });
                                //
                                // MainActivity.log("OBExpansionManager.downloadOBB: " + bytes_downloaded + " " + bytes_total + " " + dl_progress);
                            }
                            cursor.close();
                            //
                            Thread.sleep(250);
                        }
                        catch (Exception e)
                        {
                            MainActivity.log("OBExpansionManager.downloadOBB: exception caught");
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
        else
        {
            MainActivity.log("OBExpansionManager.downloadOBB: User didn't give permission to access storage");
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
            MainActivity.log("OBExpansionManager.addExpansionAssetsFolder: Expansion Folder Installed: " + id + " --> " + folder.getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void updateProgressDialog_unpack()
    {
        // can be used with RunOnMainThread but the trade-off is a slower unpack
        if (waitDialog != null)
        {
            waitDialog.setProgress(unpackFileCounter++);
        }
    }


    public void updateProgressDialog(final String message, final Boolean killDialog)
    {
        if (waitDialog != null)
        {
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    waitDialog.setMessage(message);
                    //
                    if (killDialog)
                    {
                        waitDialog.setCanceledOnTouchOutside(true);
                    }
                }
            });
        }
    }

    private void checkForRemoteOBB()
    {
        MainActivity.log("OBExpansionManager.checkForRemoteOBB");
        final String expansionURL = (String) MainActivity.mainActivity.Config().get(MainActivity.CONFIG_EXPANSION_URL);
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
                    }//
                    compareExpansionFilesAndInstallMissingOrOutdated();
                }
                catch (UnknownHostException e)
                {
                    updateProgressDialog(e.getMessage(), true);
                    compareExpansionFilesAndInstallMissingOrOutdated();
                    return;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    updateProgressDialog(e.getMessage(), true);
                    compareExpansionFilesAndInstallMissingOrOutdated();
                    return;
                }
            }
        });
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