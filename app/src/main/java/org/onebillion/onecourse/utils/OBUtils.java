package org.onebillion.onecourse.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextPaint;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;

import static org.onebillion.onecourse.mainui.MainActivity.CONFIG_GRAPHIC_SCALE;
import static org.onebillion.onecourse.mainui.MainActivity.Config;

public class OBUtils
{
    public static String lastPathComponent (String path)
    {
        int idx = path.lastIndexOf("/");
        if (idx == -1)
            return path;
        if (idx == path.length() - 1)
            return lastPathComponent(path.substring(0, path.length() - 1));
        return path.substring(idx + 1, path.length());
    }

    public static String stringByDeletingLastPathComponent (String path)
    {
        int idx = path.lastIndexOf("/");
        if (idx == -1)
            return path;
        if (idx == path.length() - 1)
            return stringByDeletingLastPathComponent(path.substring(0, path.length() - 1));
        return path.substring(0, idx);
    }

    public static String stringByAppendingPathComponent (String path, String component)
    {
        int idx = path.lastIndexOf("/");
        if (idx == path.length() - 1)
            return path + component;
        return path + "/" + component;
    }

    public static String pathExtension (String path)
    {
        String lastcomp = lastPathComponent(path);
        int idx = lastcomp.lastIndexOf(".");
        if (idx == -1)
            return "";
        if (idx == lastcomp.length() - 1)
            return "";
        return lastcomp.substring(idx + 1, lastcomp.length());
    }

    public static Boolean assetsDirectoryExists (String path)
    {
        AssetManager am = MainActivity.mainActivity.getAssets();
        try
        {
            String files[] = am.list(path);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    public static List<String> filesAtPath (String path)
    {
        AssetManager am = MainActivity.mainActivity.getAssets();
        try
        {
            String lst[] = am.list(path);
            if (lst != null)
            {
                return Arrays.asList(lst);
            }
        }
        catch (IOException e)
        {
        }
        return Collections.emptyList();
    }

//    public static Boolean fileExistsAtPath (String path)
//    {
//        AssetManager am = MainActivity.mainActivity.getAssets();
//        try
//        {
//            InputStream pis = am.open(path);
//            return (pis != null);
//        }
//        catch (IOException e)
//        {
//            //e.printStackTrace();
//        }
//        return false;
//    }


    public static Boolean fileExistsAtPath (String path)
    {
        Boolean result = getInputStreamForPath(path) != null;
        return result;
        /*
        try
        {
            AssetManager am = MainActivity.mainActivity.getAssets();
            InputStream is = am.open(path);
            return (is != null);
        }
        catch (IOException e)
        {
//            Log.v("fileExistsAtPath", "unable to find asset in bundled assets " + path);
        }
        //
        for (File mounted : OBExpansionManager.sharedManager.getExternalExpansionFolders())
        {
            try
            {
                File extendedFile = new File(mounted.getAbsolutePath() + "/" + path);
                Boolean fileExists = extendedFile.exists();
                return fileExists;
            }
            catch (Exception e)
            {
//                Log.v("getFilePathInAssets", "exception caught " + e.toString());
//                e.printStackTrace();
            }
        }
        //
        return false;
        */
    }




    public static String getAbsolutePathForFile (String path)
    {
        try
        {
            AssetManager am = MainActivity.mainActivity.getAssets();
            InputStream is = am.open(path);
            if (is != null) return "android_asset/" + path;
        }
        catch (IOException e)
        {
//             e.printStackTrace();
        }
        for (File mounted : OBExpansionManager.sharedManager.getExternalExpansionFolders())
        {
            try
            {
                File extendedFile = new File(mounted.getAbsolutePath() + "/" + path);
                if (extendedFile.exists()) return extendedFile.getAbsolutePath();
            }
            catch (Exception e)
            {
//                e.printStackTrace();
            }
        }
        return null;
    }



    public static InputStream getInputStreamForPath (String path)
    {
        if (path == null) return null;
        //


        try
        {
            File file = new File(path);
            Boolean fileExists = file.exists();
            if (fileExists)
            {
                InputStream is = MainActivity.mainActivity.getAssets().open(path);
                return is;
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OBUtils:getInputStreamPath:unable to find bundled asset: " + path);
            e.printStackTrace();
        }

        //
        for (File mounted : OBExpansionManager.sharedManager.getExternalExpansionFolders())
        {
            String extendedPath = mounted.getAbsolutePath() + "/" + path;
            //
            MainActivity.log("OBUtils:getInputStreamForPath:looking for [" + extendedPath + "]");
            //
            try
            {
                File file = new File(extendedPath);
                Boolean fileExists = file.exists();
                if (fileExists)
                {
                    MainActivity.log("OBUtils:getInputStreamPath:found external/expansion asset: " + extendedPath);
                    InputStream is = new FileInputStream(file);
                    return is;
                }
                else
                {
                    MainActivity.log("OBUtils:getInputStreamPath:unable to find external/expansion asset: " + extendedPath);
                }
            }
            catch (Exception e)
            {
                MainActivity.log("OBUtils:getInputStreamPath:unable to find external/expansion asset: " + extendedPath);
                e.printStackTrace();
            }
        }
        //
        try
        {
            File file = new File(path);
            Boolean fileExists = file.exists();
            if (fileExists)
            {
                InputStream is = new FileInputStream(file);
                return is;
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OBUtils:getInputStreamPath:unable to find literal path asset: " + path);
            e.printStackTrace();
        }
        //
        MainActivity.log("OBUtils:getInputStreamForPath:unable to find file [" + path + "]");
        return null;
    }


    public static AssetFileDescriptor getAssetFileDescriptorForPath (String path)
    {
        AssetManager am = MainActivity.mainActivity.getAssets();
        // attempts to get file descriptor from assets
        try
        {
            AssetFileDescriptor fd = am.openFd(path);
            return fd;
        }
        catch (IOException e)
        {
//            MainActivity.log("OBUtils.getAssetFileDescriptor. unable to find asset in bundled assets " + path);
        }
        // attempt to get from external assets
        for (File mounted : OBExpansionManager.sharedManager.getExternalExpansionFolders())
        {
            File extendedFile = new File(mounted.getAbsolutePath() + "/" + path);
            Uri uri = Uri.fromFile(extendedFile);
            try
            {
                AssetFileDescriptor fd = MainActivity.mainActivity.getContentResolver().openAssetFileDescriptor(uri, "r");
                return fd;
            }
            catch (IOException e)
            {
//                MainActivity.log("OBUtils.getAssetFileDescriptor. unable to find asset in downloaded assets " + path);
            }
        }
        try
        {
            File extendedFile = new File(path);
            Uri uri = Uri.fromFile(extendedFile);
            //
            AssetFileDescriptor fd = MainActivity.mainActivity.getContentResolver().openAssetFileDescriptor(uri, "r");
            return fd;
        }
        catch (Exception e)
        {
//            MainActivity.log("OBUtils.getAssetFileDescriptor. unable to find asset with path " + path);
        }
        return null;
    }


    public static void getFloatColour (int col, float outcol[])
    {
        outcol[0] = Color.red(col) / 255f;
        outcol[1] = Color.green(col) / 255f;
        outcol[2] = Color.blue(col) / 255f;
        outcol[3] = Color.alpha(col) / 255f;
    }

    public static void setFloatColour (float r, float g, float b, float a, float outcol[])
    {
        outcol[0] = r;
        outcol[1] = g;
        outcol[2] = b;
        outcol[3] = a;
    }

    public static int parseColourComponent (String comp)
    {
        int i = comp.indexOf("%");
        if (i == 0)
            return 0;
        if (i > -1)
        {
            float f = Float.parseFloat(comp.substring(0, i - 1));
            return (int) Math.round(f / 100f * 255f);
        }
        i = comp.indexOf(".");
        if (i > -1)
        {
            float f = Float.parseFloat(comp);
            return (int) Math.round(f * 255f);
        }
        return Integer.parseInt(comp);
    }

    public static int colorFromRGBString (String colstr)
    {
        String strings[] = colstr.split(",");
        return Color.argb(255, parseColourComponent(strings[0]), parseColourComponent(strings[1]), parseColourComponent(strings[2]));
    }

    public static int svgColorFromRGBString (String str)
    {
        if (str.equals("none"))
            return 0;
        if (str.length() < 4)
            return 0;
        if (str.startsWith("#"))
        {
            str = str.substring(1).toLowerCase();
            if (str.length() == 3)
            {
                int rgb[] = {0, 0, 0};
                for (int i = 0; i < 3; i++)
                {
                    int ch = str.codePointAt(i);
                    int val = 0;
                    if (Character.isDigit(ch))
                        val = ch - '0';
                    else if (ch >= 'a' && ch <= 'f')
                        val = ch - 'a' + 10;
                    rgb[i] = val;
                }
                return Color.argb(255, Math.round(rgb[0] / 15f * 255f), Math.round(rgb[1] / 15f * 255f), Math.round(rgb[2] / 15f * 255f));
            }
            else if (str.length() == 6)
            {
                int rrggbb[] = {0, 0, 0, 0, 0, 0};
                for (int i = 0; i < 6; i++)
                {
                    int ch = str.codePointAt(i);
                    int val = 0;
                    if (Character.isDigit(ch))
                        val = ch - '0';
                    else if (ch >= 'a' && ch <= 'f')
                        val = ch - 'a' + 10;
                    rrggbb[i] = val;
                }
                return Color.argb(255, (rrggbb[0] * 16 + rrggbb[1]), (rrggbb[2] * 16 + rrggbb[3]), (rrggbb[4] * 16 + rrggbb[5]));
            }
        }
        else if (str.startsWith("rgb(") && str.substring(str.length() - 1).equals(")"))
        {
            str = str.substring(4, str.length() - 1);
            String components[] = str.split(",");
            if (components.length == 3)
            {
                int rgb[] = {0, 0, 0};
                for (int i = 0; i < 3; i++)
                {
                    String comp = components[i];
                    float f;
                    if (comp.endsWith("%"))
                        f = Float.parseFloat(comp.substring(0, comp.length() - 1)) / 100f * 255f;
                    else
                        f = Float.parseFloat(comp);
                    rgb[i] = Math.round(f);
                }
                return Color.argb(255, rgb[0], rgb[1], rgb[2]);
            }
        }
        return 0;
    }

    public static PointF pointFromString (String str)
    {
        try
        {
            String strings[] = str.split(",");
            return new PointF(Float.parseFloat(strings[0]), Float.parseFloat(strings[1]));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public static Point roundPoint (PointF ptf)
    {
        return new Point((int) ptf.x, (int) ptf.y);
    }


    public static InputStream getConfigStream (String cfgName)
    {
        Map<String, Object> config = MainActivity.mainActivity.config;
        @SuppressWarnings("unchecked")
        List<String> searchPaths = (List<String>) config.get(MainActivity.CONFIG_CONFIG_SEARCH_PATH);
        AssetManager am = MainActivity.mainActivity.getAssets();
        for (String path : searchPaths)
        {
            String fullpath = path + "/" + cfgName;
            try
            {
                InputStream is = am.open(fullpath);
                return is;
            }
            catch (IOException e)
            {
            }
        }
        return null;
    }


    public static OBImage buttonFromImageName (String imageName)
    {
        OBImage im = OBImageManager.sharedImageManager().imageForName(imageName);
        float imageScale = MainActivity.mainActivity.configFloatForKey(CONFIG_GRAPHIC_SCALE);
        im.setScale(imageScale);
        return im;
    }

    public static OBControl buttonFromSVGName (String imageName)
    {
        OBGroup im = OBImageManager.sharedImageManager().vectorForName(imageName);
        float imageScale = MainActivity.mainActivity.configFloatForKey(CONFIG_GRAPHIC_SCALE);
        im.setScale(imageScale);
        im.setRasterScale(imageScale);
        im.textureKey = imageName;
        return im;
    }

    public static int PresenterColourIndex ()
    {
        return (Integer) Config().get(MainActivity.CONFIG_SKINCOLOUR);
    }

    public static int SkinColour (int offset)
    {
        @SuppressWarnings("unchecked")
        List<Integer> colList = (List<Integer>) MainActivity.mainActivity.config.get(MainActivity.CONFIG_SKINCOLOURS);
        return colList.get(Math.abs(9 - (((PresenterColourIndex() + offset) + 8) % 18)));
    }

    public static int SkinColourIndex ()
    {
        return ((Integer) MainActivity.mainActivity.config.get(MainActivity.CONFIG_SKINCOLOUR)).intValue();
    }

    public static List<String> stringSplitByCharType (String str)
    {
        List<String> arr = new ArrayList<String>();
        if (str.length() > 0)
        {
            int idx = 1, startindex = 0;
            while (idx < str.length())
            {
                while (idx < str.length() && Character.isDigit(str.charAt(idx)) == Character.isDigit(str.charAt(idx - 1)))
                    idx++;
                if (idx > startindex)
                    arr.add(str.substring(startindex, idx));
                startindex = idx;
                idx++;
            }
            if (startindex < str.length())
                arr.add(str.substring(startindex));
        }
        return arr;
    }

    static boolean isInteger (String s)
    {
        try
        {
            int v1 = Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static int orderStringArray (List<String> a1, List<String> a2)
    {
        for (int idx = 0; true; idx++)
        {
            if (idx >= a1.size())
            {
                if (idx >= a2.size())
                    return 0;
                else
                    return -1;
            }
            if (idx >= a2.size())
            {
                return 1;
            }
            String s1 = a1.get(idx), s2 = a2.get(idx);
            int res;
            if (isInteger(s1) && isInteger(s2))
            {
                int v1 = Integer.parseInt(s1);
                int v2 = Integer.parseInt(s2);
                if (v1 < v2)
                    res = -1;
                else if (v1 > v2)
                    res = 1;
                else
                    res = 0;
            }
            else
                res = s1.compareToIgnoreCase(s2);
            if (res != 0)
                return res;
        }
    }

    public static int caseInsensitiveCompareWithNumbers (String s1, String s2)
    {
        return orderStringArray(stringSplitByCharType(s1), stringSplitByCharType(s2));
    }

    public static String StrAndNo (String s, int n)
    {
        if (n == 1)
            return s;
        return s + n;
    }

    public static float floatOrPercentage (String str)
    {
        str = str.trim();
        if (str.length() == 0)
            return 0;
        boolean ispc = false;
        if (str.substring(str.length() - 1).equals("%"))
        {
            ispc = true;
            str = str.substring(0, str.length() - 1);
        }
        float f = Float.parseFloat(str);
        if (ispc)
            f = f / 100;
        return f;
    }

    public static Typeface standardTypeFace ()
    {
        if (MainActivity.standardTypeFace == null)
            MainActivity.standardTypeFace = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "fonts/onebillionreader-Regular.otf");

        //        MainActivity.standardTypeFace = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "fonts/Heinemann Collection - HeinemannSpecial-Roman.otf");
        return MainActivity.standardTypeFace;
    }

    public static OBFont StandardReadingFontOfSize(float size)
    {
        float graphicScale = (Float) (Config().get(MainActivity.mainActivity.CONFIG_GRAPHIC_SCALE));
        OBFont font = new OBFont(standardTypeFace(),size * graphicScale);
        return font;
    }

    public static OBFont UnscaledReadingFontOfSize(float size)
    {
        OBFont font = new OBFont(standardTypeFace(),size);
        return font;
    }

    public static int setColourOpacity (int colour, float opacity)
    {
        int intop = Math.round(opacity * 255f);
        colour = colour | (intop << 24);
        return colour;
    }

    public static int applyColourOpacity (int colour, float opacity)
    {
        if (opacity == 1)
            return colour;
        int opac = Color.alpha(colour);
        float fopac = opac / 255f;
        fopac = fopac * opacity;
        int intop = Math.round(fopac * 255f);
        //colour = colour | (intop << 24);
        colour = Color.argb(intop, Color.red(colour), Color.green(colour), Color.blue(colour));
        return colour;
    }

    public static float durationForPointDist (PointF p0, PointF p1, float speed)
    {
        return OB_Maths.PointDistance(p0, p1) / speed;
    }

    public static <T> List<T> randomlySortedArray (List<T> sofar, List<T> inarray)
    {
        if (inarray.size() == 0)
            return sofar;
        if (inarray.size() == 1)
        {
            sofar.add(inarray.get(0));
            return sofar;
        }
        int idx = OB_Maths.randomInt(0, (int) inarray.size() - 1);
        T obj = inarray.get(idx);
        inarray.remove(idx);
        sofar.add(obj);
        return randomlySortedArray(sofar, inarray);
    }

    public static <T> List<T> randomlySortedArray (List<T> inarray)
    {
        return randomlySortedArray(new ArrayList<T>(), new ArrayList<T>(inarray));
    }

    static <T> boolean VeryRandomlySortedArray(List<T> result,List<T> inArray,int idx,List<T> unused)
    {
        if(unused.size()  == 0)
            return true;
        for(int i = 0;i < unused.size();i++)
        {
            if(!unused.get(i).equals(inArray.get(idx)))
            {
                result.add(unused.get(i));
                List<T> arr = new ArrayList<>(unused);
                arr.remove(i);
                if(VeryRandomlySortedArray(result, inArray, idx + 1, arr))
                    return true;
                else
                {
                    result.remove(result.size()-1);
                }
            }
        }
        return false;
    }

    public static <T> List  VeryRandomlySortedArray(List<T> inArray)
    {
        List<T> result = new ArrayList<>();
        List<T> rarray =  OBUtils.randomlySortedArray(inArray);
        if(VeryRandomlySortedArray(result, inArray, 0, rarray))
            return result;
        return rarray;
    }


    public static String readTextFileFromResource (int resourceId)
    {
        Context context = MainActivity.mainActivity;
        StringBuilder body = new StringBuilder();

        try
        {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;

            while ((nextLine = bufferedReader.readLine()) != null)
            {
                body.append(nextLine);
                body.append('\n');
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(
                    "Could not open resource: " + resourceId, e);
        }
        catch (Resources.NotFoundException nfe)
        {
            throw new RuntimeException("Resource not found: " + resourceId, nfe);
        }

        return body.toString();
    }

    public static float scaleFromTransform (Matrix t)
    {
        float values[] = new float[9];
        t.getValues(values);
        float a = values[0];
        float b = values[3];
        float c = values[1];
        float d = values[4];
        float sx = (float) Math.sqrt(a * a + c * c);
        float sy = (float) Math.sqrt(b * b + d * d);
        return Math.max(sx, sy);
    }

    public static List<Object> insertAudioInterval (Object audios, int interval)
    {
        List<Object> arr = new ArrayList<>();
        //
        if (audios == null)
            return null;

        if (audios instanceof String)
        {
            String audioFile = (String) audios;
            arr.add(audioFile);
        }
        else
        {
            List<String> ls = (List<String>) audios;
            for (String audio : ls)
            {
                arr.add(audio);
                if (ls.get(ls.size() - 1) != audio)
                {
                    arr.add(interval);
                }
            }
        }
        return arr;
    }

    public static void runOnMainThread (final RunLambda lamb)
    {
        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    lamb.run();
                }
                catch (OBUserPressedBackException e)
                {
                    OBAudioManager.audioManager.stopAllAudio();
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);
                }
            }
        }.run();
    }

    public static void runOnOtherThread (final RunLambda lamb)
    {
        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground (Void... params)
            {
                try
                {
                    lamb.run();
                }
                catch (OBUserPressedBackException e)
                {
                    OBAudioManager.audioManager.stopAllAudio();
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnOtherThread", exception);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public static void runOnOtherThreadDelayed (final float delay, final RunLambda lamb)
    {
        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground (Void... params)
            {
                try
                {
                    Thread.sleep(Math.round(delay * 1000));
                    lamb.run();
                }
                catch (OBUserPressedBackException e)
                {
                    OBAudioManager.audioManager.stopAllAudio();
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnOtherThreadDelayed", exception);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public static void runOnRendererThread(final RunLambda runLambda)
    {

        try
        {
            FutureTask<Integer> futureTask = new FutureTask<Integer>(new Callable<Integer>()
            {
                @Override
                public Integer call() throws Exception
                {
                    runLambda.run();
                    return 0;
                }
            });
            MainActivity.mainActivity.glSurfaceView.queueEvent(futureTask);
         //   Integer result = futureTask.get();
        }
        catch (Exception exception)
        {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE, "Error in runOnRendererThread", exception);
        }

    }

    public static Path SimplePath (PointF from, PointF to, float offset)
    {
        Path path = new Path();
        path.moveTo(from.x, from.y);
        PointF c1 = OB_Maths.tPointAlongLine(0.33f, from, to);
        PointF c2 = OB_Maths.tPointAlongLine(0.66f, from, to);
        PointF lp = OB_Maths.ScalarTimesPoint(offset, OB_Maths.NormalisedVector(OB_Maths.lperp(OB_Maths.DiffPoints(to, from))));
        PointF cp1 = OB_Maths.AddPoints(c1, lp);
        PointF cp2 = OB_Maths.AddPoints(c2, lp);
        path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, to.x, to.y);
        return path;
    }

    public static UCurve SimpleUCurve (PointF from, PointF to, float offset)
    {
        PointF c1 = OB_Maths.tPointAlongLine(0.33f, from, to);
        PointF c2 = OB_Maths.tPointAlongLine(0.66f, from, to);
        PointF lp = OB_Maths.ScalarTimesPoint(offset, OB_Maths.NormalisedVector(OB_Maths.lperp(OB_Maths.DiffPoints(to, from))));
        PointF cp1 = OB_Maths.AddPoints(c1, lp);
        PointF cp2 = OB_Maths.AddPoints(c2, lp);
        return new UCurve(from.x, from.y, to.x, to.y, cp1.x, cp1.y, cp2.x, cp2.y);
    }

    public static int DesaturatedColour (int colour, float sat)
    {
        float components[] = {0, 0, 0, 1};
        components[0] = Color.red(colour) / 255f;
        components[1] = Color.green(colour) / 255f;
        components[2] = Color.blue(colour) / 255f;
        components[3] = Color.alpha(colour) / 255f;
        float weights[] = {0.299f, 0.587f, 0.114f};
        float greyVal = 0;
        for (int i = 0; i < 3; i++)
            greyVal += weights[i] * components[i];
        float dscomponents[] = new float[4];
        for (int i = 0; i < 3; i++)
            dscomponents[i] = components[i] * sat + greyVal * (1 - sat);
        dscomponents[3] = components[3];
        int outcol = Color.argb((int) (dscomponents[3] * 255), (int) (dscomponents[0] * 255), (int) (dscomponents[1] * 255), (int) (dscomponents[2] * 255));
        return outcol;
    }

    public static int highlightedColour (int colour)
    {
        return Color.argb(255,
                Math.round(Color.red(colour) * 0.8f),
                Math.round(Color.green(colour) * 0.8f),
                Math.round(Color.blue(colour) * 0.8f));
    }

    static String getConfigFile (String fileName)
    {
        Map<String, Object> config = Config();
        for (String path : (List<String>) config.get(MainActivity.CONFIG_CONFIG_SEARCH_PATH))
        {
            String fullPath = stringByAppendingPathComponent(path, fileName);
            if (fileExistsAtPath(fullPath))
            {
                return fullPath;
            }
        }
        return null;
    }

    static String getLocalFile (String fileName)
    {
        Map<String, Object> config = Config();
        for (String path : (List<String>) config.get(MainActivity.CONFIG_AUDIO_SEARCH_PATH))
        {
            String fullPath = stringByAppendingPathComponent(path, fileName);
            if (fileExistsAtPath(fullPath))
            {
                return fullPath;
            }
        }
        return null;
    }

    static String getFilePath (String fileName)
    {
        if (fileExistsAtPath(fileName))
        {
            return fileName;
        }
        else
        {
            String filePath = getConfigFile(fileName);
            if (filePath != null)
            {
                return filePath;
            }
            else
            {
                filePath = getLocalFile(fileName);
                if (filePath != null)
                {
                    return filePath;
                }
            }
        }
        return null;
    }

    public static List<List<Double>> ComponentTimingsForWord (String xmlPath)
    {
        List<List<Double>> timings = new ArrayList<List<Double>>();
        if (xmlPath == null)
            return timings;
        //
        xmlPath = getFilePath(xmlPath);
        //
        if (xmlPath != null)
        {
            OBXMLNode xmlNode = null;
            try
            {
                OBXMLManager xmlManager = new OBXMLManager();
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
//                List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(xmlPath));
                xmlNode = xl.get(0);
                OBXMLNode timingsNode = xmlNode.childrenOfType("timings").get(0);
                for (OBXMLNode xtiming : timingsNode.childrenOfType("timing"))
                {
                    double start = xtiming.attributeFloatValue("start");
                    double end = xtiming.attributeFloatValue("end");
                    timings.add(Arrays.asList(start, end));
                }
            }
            catch (Exception e)
            {
                System.out.println("OBUtils.ComponentTimingsForWord.exception caught " + e.toString());
                e.printStackTrace();
            }
        }
        //
        return timings;
    }

    public static String stringJoin (String[] aArr, String sSep)
    {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.length; i < il; i++)
        {
            if (i > 0)
            {
                sbStr.append(sSep);
            }
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }

    public static Map<String, OBPhoneme> LoadWordComponentsXML (Boolean includeWords)
    {
        Map<String, Object> dictionary = new HashMap<String, Object>();
        String xmlPath = getLocalFile("wordcomponents.xml");
        if (xmlPath != null)
        {
            OBXMLNode xmlNode = null;
            try
            {
                OBXMLManager xmlManager = new OBXMLManager();
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
//                List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(xmlPath));
                xmlNode = xl.get(0);
                //
                OBXMLNode phonemesNode = xmlNode.childrenOfType("phonemes").get(0);
                for (OBXMLNode phonemeNode : phonemesNode.childrenOfType("phoneme"))
                {
                    String audioID = phonemeNode.attributeStringValue("id");
                    String content = phonemeNode.contents;
                    OBPhoneme pho = new OBPhoneme(content, audioID, null);
                    dictionary.put(audioID, pho);
                }
                //
                OBXMLNode syllablesNode = xmlNode.childrenOfType("syllables").get(0);
                for (OBXMLNode syllableNode : syllablesNode.childrenOfType("syllable"))
                {
                    String audioID = syllableNode.attributeStringValue("id");
                    String content = syllableNode.contents;
                    String lets[] = content.split("/");
                    List<String> phonemeIDs = new ArrayList<String>();
                    String phonemeAttr = syllableNode.attributeStringValue("phonemes");
                    if (phonemeAttr != null)
                    {
                        for (String phoneme : phonemeAttr.split("/"))
                        {
                            phonemeIDs.add(phoneme);
                        }
                    }
                    else
                    {
                        for (String let : lets)
                        {
                            phonemeIDs.add(String.format("is_%s", let));
                        }
                    }
                    //
                    List phonemes = new ArrayList();
                    for (String phonemeID : phonemeIDs)
                    {
                        phonemes.add(dictionary.get(phonemeID));
                    }
                    //
                    OBSyllable syl = new OBSyllable(stringJoin(lets, ""), audioID, null, phonemes);
                    dictionary.put(audioID, syl);
                }
                //
                if (includeWords)
                {
                    OBXMLNode wordsNode = xmlNode.childrenOfType("words").get(0);
                    for (OBXMLNode wordNode : wordsNode.childrenOfType("word"))
                    {
                        String audioID = wordNode.attributeStringValue("id");
                        String content = wordNode.contents;
                        String sylls[] = content.split("/");
                        String fullText = stringJoin(sylls, "");
                        List<OBSyllable> syllables = new ArrayList<OBSyllable>();
                        //
                        String syllableAttr = wordNode.attributeStringValue("syllables");
                        if (syllableAttr != null)
                        {
                            for (String syllableID : syllableAttr.split("/"))
                            {
                                OBSyllable syl = (OBSyllable) dictionary.get(syllableID);
                                if (syl != null)
                                {
                                    syllables.add(syl);
                                }
                            }
                        }
                        else
                        {
                            for (String syllString : sylls)
                            {
                                String syllID = String.format("isyl_%s", syllString);
                                OBSyllable syl = (OBSyllable) dictionary.get(syllID);
                                if (syl != null)
                                {
                                    syllables.add(syl);
                                }
                                else
                                {
                                    OBSyllable obSyllable = new OBSyllable(syllString);
                                    List<OBPhoneme> sylPhos = new ArrayList<>();
                                    for(int i=0; i<obSyllable.text.length(); i++)
                                    {
                                        String letter = obSyllable.text.substring(i,i+1);
                                        String letterId = String.format("is_%s",letter);

                                        if(dictionary.get(letterId) != null)
                                        {
                                            sylPhos.add((OBPhoneme)dictionary.get(letterId));
                                        }
                                        else
                                        {
                                            sylPhos.add(new OBPhoneme(letter));
                                        }
                                    }
                                    obSyllable.phonemes = sylPhos;
                                    syllables.add(obSyllable);
                                }
                            }
                        }
                        String image = wordNode.attributeStringValue("image");
                        OBWord wor = new OBWord(fullText, audioID, null, syllables, image);
                        //
                        dictionary.put(audioID, wor);
                    }
                }

            }
            catch (Exception e)
            {
                System.out.println("OBUtils.LoadWordComponentsXML.exception caught " + e.toString());
                e.printStackTrace();
            }
        }
        //
        return (Map<String, OBPhoneme>) (Object) dictionary;
    }

    public static List<String> getFramesList (String prefix, int from, int to)
    {
        List<String> list = new ArrayList<>();
        if (from < to)
        {
            for (int i = from; i <= to; i++)
                list.add(String.format("%s%d", prefix, i));
        }
        else
        {
            for (int i = from; i >= to; i--)
                list.add(String.format("%s%d", prefix, i));
        }

        return list;

    }

    public static boolean getBooleanValue (String val)
    {
        if (val == null)
            return false;
        if (val.equalsIgnoreCase("true"))
            return true;
        else
            return false;
    }

    public static int getIntValue (String val)
    {
        if (val == null)
            return 0;

        try
        {
            return Integer.valueOf(val);
        }
        catch (Exception e)
        {
            return 0;
        }

    }

    public static RectF getBoundsForSelectionInLabel (int start, int end, OBLabel label)
    {
        OBTextLayer textLayer = (OBTextLayer) label.layer;
        Path path = new Path();
        textLayer.getSelectionPath(start, end, path);
        RectF pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
        return label.convertRectToControl(pathBounds, null);
    }

    public static float getFontXHeight (Typeface font, float fontSize)
    {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(font);
        textPaint.setTextSize(fontSize);
        Rect tempRect = new Rect();
        textPaint.getTextBounds("x", 0, 1, tempRect);
        return tempRect.height();
    }

    public static float getFontCapHeight (Typeface font, float fontSize)
    {
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTypeface(font);
        textPaint.setTextSize(fontSize);
        Rect tempRect = new Rect();
        textPaint.getTextBounds("H", 0, 1, tempRect);
        return tempRect.height();
    }

    public static String getFilePathForTempFile (OBSectionController controller)
    {
        String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        try
        {
            File outputDir = controller.activity.getCacheDir();
            File outputFile = File.createTempFile(fileName, ".tmp", outputDir);
            return outputFile.getPath();
        }
        catch (Exception exception)
        {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE, "Error in filePathForTempFile", exception);
        }
        return null;
    }

    public static String getFilePathForFolder (String fileName, String folderName, OBSectionController controller)
    {
        try
        {
            File outputDir = controller.activity.getDir(folderName, Context.MODE_PRIVATE);
            File outputFile = new File(outputDir, fileName);
            return outputFile.getPath();
        }
        catch (Exception exception)
        {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE, "Error in getFilePathForFolder", exception);
        }
        return null;
    }

    public static String getExternalFilePathForFolder (String fileName, String folderName, OBSectionController controller)
    {
        try
        {
            File outputDir = new File(Environment.getExternalStorageDirectory(), folderName);
            boolean folderCreated = outputDir.mkdir();
            File outputFile = new File(outputDir, fileName);
            return outputFile.getAbsolutePath();
        }
        catch (Exception exception)
        {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE, "Error in getExternalFilePathForFolder", exception);
        }
        return null;
    }


    public static void deleteEmptyFilesInFolder(String folderName, OBSectionController controller)
    {
        try
        {
            File outputDir = controller.activity.getDir(folderName, Context.MODE_PRIVATE);
            for(File file : outputDir.listFiles())
            {
                if(file.length() == 0)
                    file.delete();
            }
        }
        catch (Exception exception)
        {
            Logger logger = Logger.getAnonymousLogger();
            logger.log(Level.SEVERE, "Error in deleteEmptyFilesInFolder", exception);
        }
    }

    public static void cleanUpTempFiles (OBSectionController controller)
    {

        File outputDir = controller.activity.getCacheDir();
        File[] files = outputDir.listFiles();
        for (File file : files)
        {
            file.delete();
        }
    }

    public static RectF unionBounds (OBGroup group)
    {
        if (group.members.size() == 0) return new RectF(group.bounds().left, group.bounds().top, group.bounds().right, group.bounds().bottom);
        else
        {
            RectF result = new RectF();
            for (OBControl c : group.members)
            {
                if (OBGroup.class.isInstance(c))
                {
                    OBGroup subGroup = (OBGroup) c;
                    result.union(OBUtils.unionBounds(subGroup));
                }
                else
                {
                    result.union(c.bounds());
                }
            }
            return result;
        }
    }

    public static RectF unionFrame (OBGroup group)
    {
        if (group.members.size() == 0) return new RectF(group.frame().left, group.frame().top, group.frame().right, group.frame().bottom);
        else
        {
            RectF result = new RectF();
            for (OBControl c : group.members)
            {
                if (OBGroup.class.isInstance(c))
                {
                    OBGroup subGroup = (OBGroup) c;
                    result.union(OBUtils.unionBounds(subGroup));
                }
                else
                {
                    result.union(c.frame());
                }
            }
            return result;
        }
    }

    public static boolean AreAntiClockWise(PointF p0,PointF p1,PointF p2)
    {
        PointF pts[] = {p0,p1,p2};
        return OB_Maths.PolygonArea(pts, 3) > 0;
    }

    public static boolean LastThreeAntiClockWise(List<PointF> arr)
    {
        int ct = arr.size();
        if(ct < 3)
            return false;
        return AreAntiClockWise(arr.get(ct-3),arr.get(ct-2),arr.get(ct-1));
    }

    public static List<PointF>convexHullFromPoints(List<PointF>pts)
    {
        List<PointF>points = new ArrayList<>(pts);
        Collections.sort(points, new Comparator<PointF>()
        {
            @Override
            public int compare (PointF p1, PointF p2)
            {
                if(p1.x < p2.x)
                    return -1;
                if(p1.x > p2.x)
                    return 1;
                if(p1.y < p2.y)
                    return -1;
                if(p1.y > p2.y)
                    return 1;
                return 0;
            }
        });

        for(int i = points.size()  - 1;i > 0;i--)
            if(points.get(i).equals(points.get(i-1)))
                points.remove(i);
        if(points.size() < 3)
            return points;
        List<PointF> upperHull = new ArrayList<>();
        upperHull.add(points.get(0));
        upperHull.add(points.get(1));
        for(int i = 2;i < points.size();i++)
        {
            upperHull.add(points.get(i));
            while(upperHull.size()  >= 3 && LastThreeAntiClockWise(upperHull))
                upperHull.remove(upperHull.size() -2);
        }
        int n = points.size();
        List<PointF> lowerHull = new ArrayList<>();
        lowerHull.add(points.get(n - 1));
        lowerHull.add(points.get(n - 2));
        for(int i = n - 3;i >= 0;i--)
        {
            lowerHull.add(points.get(i));
            while(lowerHull.size()  >= 3 && LastThreeAntiClockWise(lowerHull))
                lowerHull.remove(lowerHull.size() - 2);
        }
        List<PointF> hull = new ArrayList<>();
        hull.addAll(upperHull);
        hull.addAll(lowerHull.subList(1, lowerHull.size()  - 1));
        return hull;
    }

    public static RectF PathsUnionRect(List<OBPath> paths)
    {
        RectF r = new RectF(paths.get(0).frame());
        for(OBPath p : paths)
            r.union(p.frame());
        return r;
    }

    private static RectF RectIntersection(RectF r1, RectF r2)
    {
        RectF r1copy = new RectF(r1);
        if (r1copy.intersect(r2))
        {
            return r1copy;
        }
        else
        {
            return null;
        }
    }

    public static float RectOverlapRatio(RectF rect1, RectF rect2)
    {
        RectF intersect = RectIntersection(rect1, rect2);
        if (intersect != null)
        {
            return (intersect.width() * intersect.height()) / (rect1.height() * rect1.width());
        }
        return 0;
    }


    public interface RunLambda
    {
        public void run () throws Exception;
    }

    public static List<Integer> RandomIndexesTo(int num)
    {
        List<Integer> indexes = new ArrayList<>();
        for(int i=0; i<num; i++)
            indexes.add(i);

        return randomlySortedArray(indexes);
    }

    public static Typeface TypefaceForFile(String file)
    {
       return Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), String.format("fonts/%s",file));
    }

    public static <T> T coalesce(T t1,T t2)
    {
        if (t1 == null)
            return t2;
        return t1;
    }

    public static <T> T RandomObjectFromArray(List<T> arr)
    {
        int i = OB_Maths.randomInt(0,(int)arr.size()  - 1);
        return arr.get(i);
    }

    public static void copyInputStreamToFile(InputStream in, File file)
    {
        OutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0)
            {
                out.write(buf,0,len);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(out != null)
                {
                    out.close();
                }
                in.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static long timestampForDateOnly(long timestamp)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}