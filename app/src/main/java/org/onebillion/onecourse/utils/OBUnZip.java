package org.onebillion.onecourse.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.onebillion.onecourse.mainui.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static android.app.ProgressDialog.STYLE_HORIZONTAL;

/**
 * Created by pedroloureiro on 07/03/2018.
 */

public class OBUnZip extends AsyncTask<Void, Integer, Integer>
{
    private String _zipFile;
    private String _location;
    private int per = 0;
    private ProgressDialog _progressDialog;
    private OBUtils.RunLambda _completionBlock;

    public OBUnZip (String zipFile, String location, OBUtils.RunLambda completionBlock)
    {
        _zipFile = zipFile;
        //
        if (location.endsWith("/")) _location = location;
        else _location = location + "/";
        //
        _completionBlock = completionBlock;
        //
        _dirChecker("");
    }

    public static void streamCopy (InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[128 * 1024]; // play with sizes..
        int readCount;
        while ((readCount = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, readCount);
        }
    }

    protected Integer doInBackground (Void... voids)
    {
        try
        {
            final ZipFile zip = new ZipFile(_zipFile);
            //
            _progressDialog.setMax(zip.size());
            //
            FileInputStream fin = new FileInputStream(_zipFile);
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
            //
            return zip.size();
        }
        catch (Exception e)
        {
            MainActivity.log("OBUnZip.Exception caught: " + e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    protected void onPreExecute ()
    {
        _progressDialog = new ProgressDialog(MainActivity.mainActivity);
        _progressDialog.setMessage("Decompressing assets. Plese wait...");
        _progressDialog.setProgressStyle(STYLE_HORIZONTAL);
        _progressDialog.setProgress(0);
        _progressDialog.show();
        //
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute (Integer integer)
    {
        if (_progressDialog.isShowing())
        {
            _progressDialog.dismiss();
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
    protected void onProgressUpdate (Integer... values)
    {
        _progressDialog.setProgress(per);
        //
        super.onProgressUpdate(values);
    }

    private void _dirChecker (String dir)
    {
        File f = new File(_location + dir);
        if (!f.isDirectory())
        {
            f.mkdirs();
        }
    }

}

