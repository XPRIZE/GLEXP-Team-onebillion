package com.maq.xprize.onecourse.hindi.mainui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.vending.expansion.downloader.Helpers;
import com.maq.xprize.onecourse.hindi.R;
import com.maq.xprize.onecourse.hindi.utils.Zip;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import static com.maq.xprize.onecourse.hindi.R.layout.activity_splash_screen;
import static com.maq.xprize.onecourse.hindi.mainui.DownloadExpansionFile.xAPKS;

public class SplashScreenActivity extends Activity {

    Intent mainActivityIntent = null;
    String expansionFilePath;
    File expansionFile;
    ZipFile expansionZipFile;
    Zip _zip;
    String unzipFilePath;
    File packageNameDir;
    SharedPreferences sharedPref;
    int defaultFileVersion = 0;
    int mainFileVersion;
    int patchFileVersion;
    boolean isExtractionRequired = false;

    public static String getUnzippedExpansionFilePath() {
        return "/storage/emulated/0/Android/data/com.maq.xprize.onecourse.hindi/files/";
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            sharedPref = getSharedPreferences("ExpansionFile", MODE_PRIVATE);
            // Retrieve the stored values of main and patch file version
            mainFileVersion = sharedPref.getInt(getString(R.string.mainFileVersion), defaultFileVersion);
            patchFileVersion = sharedPref.getInt(getString(R.string.patchFileVersion), defaultFileVersion);
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                // If main or patch file is updated set isExtractionRequired to true
                if (xf.mIsMain && xf.mFileVersion != mainFileVersion || !xf.mIsMain && xf.mFileVersion != patchFileVersion) {
                    isExtractionRequired = true;
                    break;
                }
            }
            // If main or patch file is updated, the extraction process needs to be performed again
            if (isExtractionRequired) {
                System.out.println("Splash onCreate: isExtractionRequired = " + isExtractionRequired);
                new DownloadFile().execute(null, null, null);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                sharedPref = getSharedPreferences("ExpansionFile", MODE_PRIVATE);
                // Retrieve the stored values of main and patch file version
                mainFileVersion = sharedPref.getInt(getString(R.string.mainFileVersion), defaultFileVersion);
                patchFileVersion = sharedPref.getInt(getString(R.string.patchFileVersion), defaultFileVersion);
                for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                    // If main or patch file is updated set isExtractionRequired to true
                    if (xf.mIsMain && xf.mFileVersion != mainFileVersion || !xf.mIsMain && xf.mFileVersion != patchFileVersion) {
                        isExtractionRequired = true;
                        break;
                    }
                }
                // If main or patch file is updated, the extraction process needs to be performed again
                if (isExtractionRequired) {
                    System.out.println("Splash onRequestPermissionsResult: isExtractionRequired = " + isExtractionRequired);
                    new DownloadFile().execute(null, null, null);
                }
            } else {
                Toast.makeText(this, "Permission required!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    /* function to call the main application after extraction */
    public void toCallApplication() {
        mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    public void unzipFile() {
        int totalZipSize = getTotalExpansionFileSize();
        SharedPreferences.Editor editor = sharedPref.edit();
        try {
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                expansionFilePath = getExpansionFilePath(xf.mIsMain, xf.mFileVersion);
                expansionFile = new File(expansionFilePath);
                expansionZipFile = new ZipFile(expansionFile);
                _zip = new Zip(expansionZipFile, this);
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
                if (xf.mIsMain) {
                    editor.putInt(getString(R.string.mainFileVersion), xf.mFileVersion);
                    editor.commit();
                } else {
                    editor.putInt(getString(R.string.patchFileVersion), xf.mFileVersion);
                    editor.commit();
                }
            }
            toCallApplication();
        } catch (IOException e) {
            System.out.println("Could not extract assets");
            System.out.println("Stack trace:" + e);
        }
    }

    public int getTotalExpansionFileSize() {
        int totalExpansionFileSize = 0;
        ZipFile zipFile;
        try {
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                expansionFilePath = getExpansionFilePath(xf.mIsMain, xf.mFileVersion);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
                expansionFile = new File(expansionFilePath);
                zipFile = new ZipFile(expansionFile);
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