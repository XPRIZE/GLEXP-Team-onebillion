package org.onebillion.onecourse.utils;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static android.app.ProgressDialog.STYLE_HORIZONTAL;

/**
 * Created by pedroloureiro on 07/03/2018.
 */

public class OBUnZip extends AsyncTask<Void, Integer, Integer>
{
    private String _location;
    //
    private int per;
    private int totalFiles;
    //
    private ProgressDialog _progressDialog;
    private OBUtils.RunLambda _completionBlock;
    private Boolean _useProgressDialog;
    private List<String> _zipFiles;
    //
    public OBPath externalProgressBar;

    public OBUnZip(List<String> zipFiles, String location, OBUtils.RunLambda completionBlock, Boolean useProgressDialog)
    {
        _zipFiles = zipFiles;
        //
        if (location.endsWith("/"))
        {
            _location = location;
        }
        else
        {
            _location = location + "/";
        }
        //
        _completionBlock = completionBlock;
        //
        _useProgressDialog = useProgressDialog;
        //
        _dirChecker("");
    }


    public OBUnZip(String zipFile, String location, OBUtils.RunLambda completionBlock, Boolean useProgressDialog)
    {
        this(Arrays.asList(zipFile), location, completionBlock, useProgressDialog);
    }


    public static void streamCopy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[128 * 1024]; // play with sizes..
        int readCount;
        while ((readCount = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, readCount);
        }
    }


    protected Integer doInBackground(Void... voids)
    {
        per = 0;
        try
        {
            totalFiles = 0;
            for (String zipFile : _zipFiles)
            {
                ZipFile zip = new ZipFile(zipFile);
                totalFiles += zip.size();
            }
            //
            if (_useProgressDialog)
            {
                _progressDialog.setMax(totalFiles);
            }
            //
            for (String zipFile : _zipFiles)
            {
                FileInputStream fin = new FileInputStream(zipFile);
                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null)
                {
                    //MainActivity.log("Unzipping " + ze.getName());
                    if (ze.isDirectory())
                    {
                        _dirChecker(ze.getName());
                    }
                    else
                    {
                        per++;
                        publishProgress(per);
                        //
                        FileOutputStream fout = new FileOutputStream(_location + ze.getName());
                        //
                        streamCopy(zin, fout);
                        //
                        zin.closeEntry();
                        fout.close();
                    }
                }
                zin.close();
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OBUnZip.Exception caught: " + e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    protected void onPreExecute()
    {
        if (_useProgressDialog)
        {
            _progressDialog = new ProgressDialog(MainActivity.mainActivity);
            _progressDialog.setMessage("Decompressing assets. Please wait...");
            _progressDialog.setProgressStyle(STYLE_HORIZONTAL);
            _progressDialog.setProgress(0);
            _progressDialog.show();
        }
        else if (externalProgressBar != null)
        {
            float totalWidth = externalProgressBar.width();
            externalProgressBar.setProperty("totalWidth", totalWidth);
            int fillColour = externalProgressBar.fillColor();
            externalProgressBar.setProperty("finalColour", fillColour);
            //
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    externalProgressBar.setFillColor(Color.RED);
                    externalProgressBar.disable();
                }
            });
        }
        //
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(Integer integer)
    {
        if (_useProgressDialog)
        {
            if (_progressDialog.isShowing())
            {
                _progressDialog.dismiss();
            }
        }
        else if (externalProgressBar != null)
        {
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    int finalColour = (int) externalProgressBar.propertyValue("finalColour");
                    externalProgressBar.setFillColor(finalColour);
                    externalProgressBar.enable();
                }
            });
        }
        //
        if (_completionBlock != null)
        {
            OBUtils.runOnMainThread(_completionBlock);
        }
        //
        super.onPostExecute(integer);
    }


    @Override
    protected void onProgressUpdate(Integer... values)
    {
        if (_useProgressDialog)
        {
            _progressDialog.setProgress(per);
        }
        else if (externalProgressBar != null)
        {
            final float ratio = per / (float) totalFiles;
            final float totalWidth = (float) externalProgressBar.propertyValue("totalWidth");
            final float left = externalProgressBar.left();
            //
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    externalProgressBar.setWidth(totalWidth * ratio);
                    externalProgressBar.setLeft(left);
                }
            });
            //
            //MainActivity.log("Decompressing: " + ratio * 100.0);
        }
        super.onProgressUpdate(values);
    }

    private void _dirChecker(String dir)
    {
        File f = new File(_location + dir);
        if (!f.isDirectory())
        {
            f.mkdirs();
        }
    }

}

