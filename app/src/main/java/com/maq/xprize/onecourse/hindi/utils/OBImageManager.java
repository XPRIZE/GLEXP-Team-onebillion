package com.maq.xprize.onecourse.hindi.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;

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
        // check if it's icon for masterlist
        // TODO: it's missing the playzone and library masterlists here
        String mlname = OBConfigManager.sharedManager.getMasterlist();
        if (mlname != null)
        {
            for (String imageSuffix : OBConfigManager.sharedManager.getImageExtensions())
            {
                String fullPath = String.format("masterlists/%s/icons/%s.%s", mlname, imageName, imageSuffix);
                if (OBUtils.fileExistsAtPath(fullPath))
                {
                    MainActivity.log("Found masterlist icon " + fullPath);
                    return fullPath;
                }
            }
        }
        //
        for (String imageSuffix : OBConfigManager.sharedManager.getImageExtensions())
        {
            for (String path : OBConfigManager.sharedManager.getImageSearchPaths())
            {
                String fullPath = path + "/" + imageName + "." + imageSuffix;
                if (OBUtils.fileExistsAtPath(fullPath)) return fullPath;
            }
        }
        //
        return null;
    }

    public String getVectorPath (String imageName)
    {
        for (String suffix : OBConfigManager.sharedManager.getVectorExtensions())
        {
            for (String path : OBConfigManager.sharedManager.getVectorSearchPaths())
            {
                String fullPath = path + "/" + imageName + "." + suffix;
                if (OBUtils.fileExistsAtPath(fullPath)) return fullPath;
            }
        }
        //
        return null;
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
