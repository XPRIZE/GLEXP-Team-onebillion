package com.maq.xprize.onecourse.hindi.utils;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.maq.xprize.onecourse.hindi.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static android.content.ContentValues.TAG;

public class Zip {

    private ZipFile _zipFile;
    private TextView percentText;
    private Activity zipActivity;
    private static int count = 0;

    public Zip(ZipFile zipFile, Activity _activity) {
        this._zipFile = zipFile;
        zipActivity = _activity;
    }

    public Zip(String pathToZipFile) throws IOException {
        this._zipFile = new ZipFile(pathToZipFile);
    }

    public void close() throws IOException {
        _zipFile.close();
    }

    public void unzip(String extractPath, int totalZipSize) throws IOException {
        File targetDir = new File(extractPath);
        int percent;
        ProgressBar progressBar = (ProgressBar) zipActivity.findViewById(R.id.p);
        percentText = (TextView) zipActivity.findViewById(R.id.mPercentText);
        String path;
        ZipEntry zipEntry;
        File outputFile;
        File outputDir;
        File flagFile;
        BufferedInputStream inputStream;
        BufferedOutputStream outputStream;
        boolean isExtractionSuccessful = false;

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Unable to create directory");
        }

        if (!targetDir.isDirectory()) {
            throw new IOException("Unable to extract to a non-directory");
        }

        Enumeration<? extends ZipEntry> zipEntries = _zipFile.entries();

        progressBar = (ProgressBar) progressBar.findViewById(R.id.p);
        while (zipEntries.hasMoreElements()) {
            ++count;
            // Calculate the percentage of extracted content
            percent = (count * 100) / totalZipSize;
            Log.d(TAG, "unzip percent: " + percent);
            // Sync the progress bar with percentage value
            progressBar.setProgress(percent);
            final int finalPercent = percent;
            zipActivity.runOnUiThread(new Runnable() {
                public void run() {
                    // Show the percentage value on progress bar
                    percentText.setText(finalPercent + " %");
                }
            });

            zipEntry = zipEntries.nextElement();
            path = extractPath + zipEntry.getName();
            if (zipEntry.isDirectory()) {
                /*File newDir = new File(path);
				if(!newDir.mkdirs()){
					throw new IOException("Unable to extract the zip entry " + path);
				}*/
            } else {
                inputStream = new BufferedInputStream(_zipFile.getInputStream(zipEntry));

                outputFile = new File(path);
                outputDir = new File(outputFile.getParent());

                if (!outputDir.exists() && !outputDir.mkdirs()) {
                    throw new IOException("Unable to make directory for entry " + path);
                }

                if (!outputFile.exists() && !outputFile.createNewFile()) {
                    throw new IOException("Unable to create directory for " + path);
                }

                outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
                try {
                    int currByte;
                    while ((currByte = inputStream.read()) != -1) {
                        outputStream.write(currByte);
                    }
                    isExtractionSuccessful = true;
                } catch (Exception e) {
                    isExtractionSuccessful = false;
                    e.printStackTrace();
                } finally {
                    outputStream.close();
                    inputStream.close();
                }
            }
        }
        if (isExtractionSuccessful) {
            flagFile = new File(extractPath + ".success.txt");
            flagFile.createNewFile();
        }
    }
}