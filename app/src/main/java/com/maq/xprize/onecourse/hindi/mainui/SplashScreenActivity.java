package com.maq.xprize.onecourse.hindi.mainui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
    Zip zipHandler;
    String unzipFilePath;
    File packageNameDir;
    SharedPreferences sharedPref;
    int defaultFileVersion = 0;
    int storedMainFileVersion;
    int storedPatchFileVersion;
    boolean isExtractionRequired = false;

    public String getUnzippedExpansionFilePath() {
        return "/storage/emulated/0/Android/data/" + getPackageName() + "/files/";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
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
            storedMainFileVersion = sharedPref.getInt(getString(R.string.mainFileVersion), defaultFileVersion);
            storedPatchFileVersion = sharedPref.getInt(getString(R.string.patchFileVersion), defaultFileVersion);
            isExtractionRequired = isExpansionExtractionRequired(storedMainFileVersion, storedPatchFileVersion);
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
                storedMainFileVersion = sharedPref.getInt(getString(R.string.mainFileVersion), defaultFileVersion);
                storedPatchFileVersion = sharedPref.getInt(getString(R.string.patchFileVersion), defaultFileVersion);
                isExtractionRequired = isExpansionExtractionRequired(storedMainFileVersion, storedPatchFileVersion);
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

    private boolean isExpansionExtractionRequired(int storedMainFileVersion, int storedPatchFileVersion) {
        for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
            // If main or patch file is updated set isExtractionRequired to true
            if (xf.mIsMain && xf.mFileVersion != storedMainFileVersion || !xf.mIsMain && xf.mFileVersion != storedPatchFileVersion) {
                return true;
            }
        }
        return false;
    }

    /* function to call the main application after extraction */
    public void toCallApplication() {
        mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    public void unzipFile() {
        int totalZipSize = getTotalExpansionFileSize();
        try {
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                if (xf.mIsMain && xf.mFileVersion != storedMainFileVersion || !xf.mIsMain && xf.mFileVersion != storedPatchFileVersion) {
                    expansionFilePath = getExpansionFilePath(xf.mIsMain, xf.mFileVersion);
                    expansionFile = new File(expansionFilePath);
                    expansionZipFile = new ZipFile(expansionFile);
                    zipHandler = new Zip(expansionZipFile, this);
                    unzipFilePath = getUnzippedExpansionFilePath();
                    packageNameDir = new File(unzipFilePath);
                    if (xf.mIsMain && !packageNameDir.exists()) {
                        packageNameDir.mkdir();
                    }
                    zipHandler.unzip(unzipFilePath, totalZipSize, xf.mIsMain, xf.mFileVersion, sharedPref);
                    zipHandler.close();
                }
            }
            toCallApplication();
        } catch (IOException e) {
            System.out.println("Could not extract assets");
            System.out.println("Stack trace:" + e);
        }
    }

    public boolean isStorageSpaceAvailable() {
        long totalExpansionFileSize = 0;
        File internalStorageDir = new File("/storage/emulated/0/Android/data/");
        for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
            if (xf.mIsMain && xf.mFileVersion != storedMainFileVersion || !xf.mIsMain && xf.mFileVersion != storedPatchFileVersion) {
                totalExpansionFileSize = xf.mFileSize;
            }
        }
        return totalExpansionFileSize < internalStorageDir.getFreeSpace();
    }

    public int getTotalExpansionFileSize() {
        int totalExpansionFileSize = 0;
        ZipFile zipFile;
        try {
            for (DownloadExpansionFile.XAPKFile xf : xAPKS) {
                if (xf.mIsMain && xf.mFileVersion != storedMainFileVersion || !xf.mIsMain && xf.mFileVersion != storedPatchFileVersion) {
                    expansionFilePath = getExpansionFilePath(xf.mIsMain, xf.mFileVersion);
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 99);
                    expansionFile = new File(expansionFilePath);
                    zipFile = new ZipFile(expansionFile);
                    totalExpansionFileSize += zipFile.size();
                }
            }
        } catch (IOException ie) {
            System.out.println("Couldn't get total expansion file size");
            System.out.println("Stacktrace: " + ie);
        }
        return totalExpansionFileSize;
    }

    public String getExpansionFilePath(boolean isMain, int fileVersion) {
        return getObbDir() + File.separator + Helpers.getExpansionAPKFileName(this, isMain, fileVersion);
    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... sUrl) {
            if (isStorageSpaceAvailable()) {
                unzipFile();
            } else {
                Toast.makeText(SplashScreenActivity.this, "Insufficient storage space! Please free up your storage to use this application.", Toast.LENGTH_LONG).show();
                finish();
            }
            return null;
        }
    }
}