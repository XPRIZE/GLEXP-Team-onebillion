package org.onebillion.onecourse.mainui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.vending.expansion.downloader.Helpers;

import org.onebillion.onecourse.utils.Zip;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import onecourse.DownloadExpansionFile;

import static onecourse.DownloadExpansionFile.xAPKS;
import static org.onebillion.onecourse.R.layout.activity_splash_screen;

public class SplashScreenActivity extends Activity {

    Intent intent = null;
    String filePath;
    File file;
    ZipFile zipFile;
    Zip _zip;
    String unzipFilePath;
    File packageNameDir;

    public static String getUnzippedExpansionFilePath() {
        return "/storage/emulated/0/Android/data/org.onebillion.onecourse.child.en_GB/";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = this.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
        setContentView(activity_splash_screen);
        new DownloadFile().execute(null, null, null);
    }

    /* function to call the main application after extraction */
    public void toCallApplication() {
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void unzipFile() {
        int totalZipSize = getTotalExpansionFileSize();
        try {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                filePath = getExpansionFilePath(xf.mIsMain, xf.mFileVersion);
                file = new File(filePath);
                zipFile = new ZipFile(file);
                _zip = new Zip(zipFile, this);
                unzipFilePath = getUnzippedExpansionFilePath();
                packageNameDir = new File(unzipFilePath);
                if (xf.mIsMain) {
                    if (packageNameDir.exists()) {
                        DownloadExpansionFile.deleteDir(packageNameDir);
                    }
                    packageNameDir.mkdir();
                }
                _zip.unzip(unzipFilePath, totalZipSize);
                _zip.close();
            }
            toCallApplication();
        } catch (IOException ie) {
            unzipFile();
        }
    }

    public int getTotalExpansionFileSize() {
        int totalExpansionFileSize = 0;
        ZipFile zipFile;
        try {
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                filePath = getExpansionFilePath(xf.mIsMain, xf.mFileVersion);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
                file = new File(filePath);
                zipFile = new ZipFile(file);
                totalExpansionFileSize += zipFile.size();
            }
        } catch (IOException ie) {
            System.out.println("Couldn't get total expansion file size");
            System.out.println("Stacktrace: " + ie);
        }
        return totalExpansionFileSize;
    }

    public String getExpansionFilePath(boolean isMain, int fileVersion) {
        return Environment.getExternalStorageDirectory().toString() + "/Android/obb/" + Helpers.getPackageName(this) + File.separator +
                Helpers.getExpansionAPKFileName(this, isMain, fileVersion);
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            unzipFile();
            return null;
        }
    }
}