package org.onebillion.xprz.utils;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.mainui.MainActivity;

import java.io.IOException;
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
            suffixes = (List<String>) obj;
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

    public Bitmap bitmapForName (String imageName)
    {
        String path = getImgPath(imageName);
        if (path == null)
            return null;
        Bitmap b;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inScaled = false;
        b = BitmapFactory.decodeStream(OBUtils.getInputStreamForPath(path), null, opt);
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

    public Drawable drawableForName (String imageName)
    {
        String path = getImgPath(imageName);
        if (path == null)
            return null;
        Drawable d;
        d = Drawable.createFromStream(OBUtils.getInputStreamForPath(path), null);
//        try
//        {
//            d = Drawable.createFromStream(MainActivity.mainActivity.getAssets().open(path), null);
//        }
//        catch (IOException e)
//        {
//            return null;
//        }
        float densityFactor = MainActivity.mainActivity.getResources().getDisplayMetrics().density;
        float imageScale = MainActivity.mainActivity.configFloatForKey(MainActivity.CONFIG_GRAPHIC_SCALE);
        int w = d.getIntrinsicWidth();
        int h = d.getIntrinsicHeight();
        w *= densityFactor;
        h *= densityFactor;
        d.setBounds(0, 0, w, h);
        return d;
    }

    public OBImage imageForName (String imageName)
    {
        Bitmap b = bitmapForName(imageName);
        if (b != null)
        {
            OBImage im = new OBImage(b);
            float densityFactor = MainActivity.mainActivity.getResources().getDisplayMetrics().density;
            int w = b.getWidth();
            int h = b.getHeight();
            //w *= densityFactor;
            //h *= densityFactor;
            im.setBounds(0, 0, w, h);
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

}
