package org.onebillion.onecourse.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.mainui.MainActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alan on 11/11/15.
 */
public class OBImageManager
{
    public static int MAX_SVG_CACHE_COUNT = 32;
    protected static OBImageManager _sharedImageManager;
    public Map<String, OBXMLNode> svgCacheDict;
    public List<OBXMLNode> svgCacheList;
    public OBImageManager ()
    {
        svgCacheDict = new HashMap<String, OBXMLNode>();
        svgCacheList = new ArrayList<>();
    }

    public static OBImageManager sharedImageManager ()
    {
        if (_sharedImageManager == null)
            _sharedImageManager = new OBImageManager();
        return _sharedImageManager;
    }

    public String getImgPath (String imageName)
    {
        Map<String, Object> config = MainActivity.mainActivity.config;
        Object obj = config.get(MainActivity.CONFIG_IMAGE_SUFFIX);
        List<String> suffixes;
        if (obj instanceof String)
        {
            suffixes = new ArrayList<String>();
            suffixes.add((String) obj);
        }
        else
        {
            suffixes = (List<String>) obj;
        }
        // check if it's icon for masterlist
        String mlname = (String) MainActivity.mainActivity.config.get(MainActivity.CONFIG_MASTER_LIST);
        if (mlname != null)
        {
            for (String imageSuffix : suffixes)
            {
                String fullPath = String.format("masterlists/%s/icons/%s.%s", mlname, imageName, imageSuffix);
                if (OBUtils.fileExistsAtPath(fullPath)) return fullPath;
            }
        }
        //
        List<String> searchPaths = (List<String>) config.get(MainActivity.CONFIG_IMAGE_SEARCH_PATH);
        //
        for (String imageSuffix : suffixes)
        {
            for (String path : searchPaths)
            {
                String fullPath = path + "/" + imageName + "." + imageSuffix;
                if (OBUtils.fileExistsAtPath(fullPath)) return fullPath;
            }
        }
        //
        return null;
//        AssetManager am = MainActivity.mainActivity.getAssets();
//        for (String imageSuffix : suffixes)
//        {
//            for (String path : searchPaths)
//            {
//                String fullpath = path + "/" + imageName + "." + imageSuffix;
//                try
//                {
//                    InputStream is = am.open(fullpath);
//                    is.close();
//                    return fullpath;
//                }
//                catch (IOException e)
//                {
//                }
//            }
//        }
//        return null;
    }

    public String getVectorPath (String imageName)
    {
        Map<String, Object> config = MainActivity.mainActivity.config;
        List<String> searchPaths = (List<String>) config.get(MainActivity.CONFIG_VECTOR_SEARCH_PATH);
        for (String path : searchPaths)
        {
            String fullPath = path + "/" + imageName + ".svg";
            if (OBUtils.fileExistsAtPath(fullPath)) return fullPath;
        }
        return null;
//        AssetManager am = MainActivity.mainActivity.getAssets();
//        for (String path : searchPaths)
//        {
//            String fullpath = path + "/" + imageName + ".svg";
//            try
//            {
//                InputStream is = am.open(fullpath);
//                is.close();
//                return fullpath;
//            }
//            catch (IOException e)
//            {
//            }
//        }
//        return null;
    }

    static boolean HighResPath(String path)
    {
        path = OBUtils.stringByDeletingLastPathComponent(path);
        String last = OBUtils.lastPathComponent(path);
        if(last.equals("shared_4"))
            return true;
        return false;
    }

    public Bitmap bitmapForName (String imageName,OB_MutFloat scale)
    {
        String path = getImgPath(imageName);
        if (path == null)
            return null;
        Bitmap b;
        scale.value = 1.0f;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inScaled = false;
        b = BitmapFactory.decodeStream(OBUtils.getInputStreamForPath(path), null, opt);
        if (HighResPath(path))
            scale.value = 2.0f;
//        try
//        {
//            b = BitmapFactory.decodeStream(MainActivity.mainActivity.getAssets().open(path), null, opt);
//        }
//        catch (IOException e)
//        {
//            return null;
//        }
        return b;
    }

    public Bitmap bitmapForPath (String imagePath,OB_MutFloat scale)
    {
        if (imagePath == null)
            return null;
        Bitmap b;
        scale.value = 1.0f;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inScaled = false;
        b = BitmapFactory.decodeStream(OBUtils.getInputStreamForPath(imagePath), null, opt);
        if (HighResPath(imagePath))
            scale.value = 2.0f;
        return b;
    }

    public OBImage imageForName (String imageName)
    {
        OB_MutFloat fileScale = new OB_MutFloat(1.0f);
        Bitmap b = bitmapForName(imageName,fileScale);
        if (b != null)
        {
            OBImage im = new OBImage(b);
            int w = b.getWidth();
            int h = b.getHeight();
            im.setBounds(0, 0, w, h);
            im.setIntrinsicScale(fileScale.value);
            return im;
        }
        return null;
    }

    public OBImage imageForPath (String imagePath)
    {
        OB_MutFloat fileScale = new OB_MutFloat(1.0f);
        Bitmap b = bitmapForPath(imagePath,fileScale);
        if (b != null)
        {
            OBImage im = new OBImage(b);
            int w = b.getWidth();
            int h = b.getHeight();
            im.setBounds(0, 0, w, h);
            im.setIntrinsicScale(fileScale.value);
            return im;
        }
        return null;
    }

    public Drawable imageNamed (String imageName)
    {
        float densityFactor = MainActivity.mainActivity.getResources().getDisplayMetrics().density;
//        AssetManager assetManager = MainActivity.mainActivity.getAssets();
        Drawable d;
        try
        {
            d = Drawable.createFromStream(OBUtils.getInputStreamForPath("sysimages/" + imageName + "@2x.png"), null);
//            d = Drawable.createFromStream(assetManager.open("sysimages/" + imageName + "@2x.png"), null);
            int w = d.getIntrinsicWidth();
            int h = d.getIntrinsicHeight();
            w *= densityFactor;
            h *= densityFactor;
            d.setBounds(0, 0, w, h);
        }
        catch (Exception e)
        {
            return null;
        }
        return d;
    }

    public OBXMLNode svgXMLNodeForName (String name)
    {
        OBXMLNode svgnode = svgCacheDict.get(name);
        if (svgnode == null)
        {
            String path = getVectorPath(name);
            OBXMLManager xmlManager = new OBXMLManager();
            try
            {
                InputStream is = OBUtils.getInputStreamForPath(path);
//                InputStream is = MainActivity.mainActivity.getAssets().open(path);
                svgnode = xmlManager.parseFile(is).get(0);
                if (svgnode == null)
                    return null;
                svgCacheDict.put(name, svgnode);
            }
            catch (Exception e)
            {
                MainActivity.log("unable to retrieve SVG:" + name);
                e.printStackTrace();
                return null;
            }
        }
        svgCacheList.remove(svgnode);
        svgCacheList.add(svgnode);
        if (svgCacheList.size() >= MAX_SVG_CACHE_COUNT)
        {
            OBXMLNode firstobj = svgCacheList.get(0);
            svgCacheList.remove(0);
            Set<String> s = new HashSet<>(svgCacheDict.keySet());
            for (String k : s)
                if (svgCacheDict.get(k) == firstobj)
                    svgCacheDict.remove(k);
        }
        return svgnode;
    }

    public OBGroup groupForSVGName (String name)
    {
        OBXMLNode svgnode = svgXMLNodeForName(name);
        if (svgnode == null)
            return null;

        OBGroup g = OBGroup.groupFromSVGXML(svgnode);

        return g;
    }

    public OBGroup vectorForName (String name)
    {
        return groupForSVGName(name);
    }

    public void clearCaches()
    {
        svgCacheDict.clear();
        svgCacheList.clear();
    }

}
