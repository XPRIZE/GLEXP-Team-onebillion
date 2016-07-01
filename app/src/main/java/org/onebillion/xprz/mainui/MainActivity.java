package org.onebillion.xprz.mainui;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.glstuff.OBGLView;
import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBFatController;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBUser;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

public class MainActivity extends ActionBarActivity
{
    public static String CONFIG_IMAGE_SUFFIX = "image_suffix",
            CONFIG_AUDIO_SUFFIX = "audio_suffix",
            CONFIG_AUDIO_SEARCH_PATH = "audioSearchPath",
            CONFIG_IMAGE_SEARCH_PATH = "imageSearchPath",
            CONFIG_CONFIG_SEARCH_PATH = "configSearchPath",
            CONFIG_LEFT_BUTTON_POS = "lbuttonpos",
            CONFIG_RIGHT_BUTTON_POS = "rbuttonpos",
            CONFIG_GRAPHIC_SCALE = "graphicscale",
            CONFIG_POINTERS = "pointers",
            CONFIG_POINTERCOORDS = "pointercoords",
            CONFIG_POINTERSTARTPOINTS = "pointerstartpoints",
            CONFIG_COLOURS = "colours",
            CONFIG_SKINCOLOURS = "skincolours",
            CONFIG_SKINCOLOUR = "skincolour",
            CONFIG_MENUTABCOLOURS = "menutabcolours",
            CONFIG_LOCKED = "locked",
            CONFIG_LANGUAGE = "language",
            CONFIG_DEFAULT_LANGUAGE = "defaultlanguage",
            CONFIG_CLOTHCOLOUR = "clothcolour",
            CONFIG_VECTOR_SEARCH_PATH = "vectorsearchpath",
            CONFIG_AWARDAUDIO = "staraudio",
            CONFIG_APP_CODE = "app_code",
            CONFIG_USER = "user",
            CONFIG_FAT_CONTROLLER = "fatcontrollerclass";

    public static MainActivity mainActivity;
    public static OBMainViewController mainViewController;
    public static Typeface standardTypeFace;
    public Map<String, Object> config;
    public List<OBUser>users;
    public OBFatController fatController;
    public OBGLView glSurfaceView;
    public OBRenderer renderer;
    private int b;

    public static OBGroup armPointer()
    {
        OBGroup arm = OBImageManager.sharedImageManager().vectorForName("arm_sleeve");
        OBControl anchor = arm.objectDict.get("anchor");
        if (anchor != null)
        {
            PointF pt = arm.convertPointFromControl(anchor.position(), anchor.parent);
            PointF rpt = OB_Maths.relativePointInRectForLocation(pt, arm.bounds());
            arm.anchorPoint = rpt;
        }
        else
            arm.anchorPoint = new PointF(0.64f, 0);

        int skincol = OBUtils.SkinColour(0);
        arm.substituteFillForAllMembers("skin.*",skincol);
        arm.setRasterScale((Float)Config().get(CONFIG_GRAPHIC_SCALE));
        //arm.borderColour = 0xff000000;
        //arm.borderWidth = 1;
        return arm;
    }

    public static Map<String, Object> Config()
    {
        return mainActivity.config;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        doGLStuff();
        users = new ArrayList<OBUser>();
        try
        {
            setUpConfig();
            mainViewController = new OBMainViewController(this);
            glSurfaceView.controller = mainViewController;
            new OBAudioManager();
            ((ThreadPoolExecutor)AsyncTask.THREAD_POOL_EXECUTOR).setCorePoolSize(12);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void doGLStuff()
    {
        glSurfaceView = new OBGLView(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        final ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        /*

        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
         */
        // Even though the latest emulator supports OpenGL ES 2.0,
        // it has a bug where it doesn't set the reqGlEsVersion so
        // the above check doesn't work. The below will detect if the
        // app is running on an emulator, and assume that it supports
        // OpenGL ES 2.0.
        final boolean supportsEs2 =
                configurationInfo.reqGlEsVersion >= 0x20000
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
                        && (Build.FINGERPRINT.startsWith("generic")
                        || Build.FINGERPRINT.startsWith("unknown")
                        || Build.MODEL.contains("google_sdk")
                        || Build.MODEL.contains("Emulator")
                        || Build.MODEL.contains("Android SDK built for x86")));

        if (supportsEs2)
        {
            // Request an OpenGL ES 2.0 compatible context.
            glSurfaceView.setEGLContextClientVersion(2);

            // Assign our renderer.
            glSurfaceView.setRenderer(renderer = new OBRenderer());
        } else
        {
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        setContentView(glSurfaceView);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public String languageCode()
    {
        return "en_gb";
    }

    OBSectionController topController()
    {
        List l = mainViewController.viewControllers;
        return (OBSectionController) l.get(l.size()-1);
    }

    public Object configValueForKey(String k)
    {
        return config.get(k);
    }

    public String configStringForKey(String k)
    {
        return (String)config.get(k);
    }

    public int configIntForKey(String k)
    {
        Integer i = (Integer) config.get(k);
        return i.intValue();
    }
    public float configFloatForKey(String k)
    {
        Float f = (Float) config.get(k);
        return f.floatValue();
    }

    public List<String> audioSearchPath(String appDir,String genDir)
    {
        String wDir = null;
        if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"))
            wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
        String language = (String)config.get(CONFIG_LANGUAGE);
        String defaultLanguage = (String)config.get(CONFIG_DEFAULT_LANGUAGE);
        List audioSearchPath = new ArrayList(4);
        if (!language.equals(defaultLanguage))
        {
            if (appDir != null)
                audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(appDir,"local"),language));
            if (wDir != null)
                audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(wDir,"local"),language));
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(genDir,"local"),language));
        }
        if (appDir != null)
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(appDir,"local"),defaultLanguage));
        if (wDir != null)
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(wDir,"local"),defaultLanguage));
        audioSearchPath.add(OBUtils.stringByAppendingPathComponent(OBUtils.stringByAppendingPathComponent(genDir,"local"),defaultLanguage));

        if (appDir != null)
            audioSearchPath.add(OBUtils.stringByAppendingPathComponent(appDir,"sfx"));
        audioSearchPath.add(OBUtils.stringByAppendingPathComponent(genDir,"sfx"));

        for (int i = audioSearchPath.size() - 1;i >= 0;i--)
            if (!OBUtils.assetsDirectoryExists((String)audioSearchPath.get(i)))
                audioSearchPath.remove(i);

        return audioSearchPath;
    }

    public List<String> imageSearchPath(String appDir,String genDir)
    {
        Boolean inBooks = (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"));
        List lowres = new ArrayList(4);
        if (appDir != null)
        {
            lowres.add(OBUtils.stringByAppendingPathComponent(appDir,"img/shared_3"));
            if (inBooks)
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                lowres.add(OBUtils.stringByAppendingPathComponent(wDir,"img/shared_3"));
            }
        }
        lowres.add(OBUtils.stringByAppendingPathComponent(genDir,"img/shared_3"));
        List highres = new ArrayList(4);
        if (appDir != null)
        {
            highres.add(OBUtils.stringByAppendingPathComponent(appDir,"img/shared_4"));
            if (inBooks)
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                highres.add(OBUtils.stringByAppendingPathComponent(wDir,"img/shared_4"));
            }
        }
        highres.add(OBUtils.stringByAppendingPathComponent(genDir,"img/shared_4"));

        List imageSearchPath = new ArrayList(4);
        imageSearchPath.addAll(highres);
        imageSearchPath.addAll(lowres);
        return imageSearchPath;
    }

    public List<String> configSearchPath(String appDir,String genDir)
    {
        List configSearchPath = new ArrayList(4);
        if (appDir != null)
        {
            configSearchPath.add(OBUtils.stringByAppendingPathComponent(appDir,"config"));
            if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"))
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                configSearchPath.add(OBUtils.stringByAppendingPathComponent(wDir,"config"));
            }
        }
        configSearchPath.add(OBUtils.stringByAppendingPathComponent(genDir,"config"));
        return configSearchPath;
    }

    public List<String> vectorSearchPath(String appDir,String genDir)
    {
        List configSearchPath = new ArrayList(4);
        if (appDir != null)
        {
            configSearchPath.add(OBUtils.stringByAppendingPathComponent(appDir,"img/vector"));
            if (OBUtils.lastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir)).equals("books"))
            {
                String wDir = OBUtils.stringByDeletingLastPathComponent(OBUtils.stringByDeletingLastPathComponent(appDir));
                configSearchPath.add(OBUtils.stringByAppendingPathComponent(wDir,"img/vector"));
            }
        }
        configSearchPath.add(OBUtils.stringByAppendingPathComponent(genDir,"img/vector"));
        return configSearchPath;
    }

    public void updateConfigPaths(String newAppCode,Boolean force)
    {
        String lastAppCode = (String)config.get(CONFIG_APP_CODE);
        if (lastAppCode.equals(newAppCode) && !force)
            return;
        config.put(CONFIG_APP_CODE,newAppCode);
        String appDir = newAppCode;
        String genDir = (String)config.get("gen_code");
        String languageCode = languageCode();
        if (languageCode != null)
            config.put(CONFIG_LANGUAGE,languageCode);
        else
            config.put(CONFIG_LANGUAGE,config.get(CONFIG_DEFAULT_LANGUAGE));
        config.put(CONFIG_AUDIO_SEARCH_PATH,audioSearchPath(appDir,genDir));
        config.put(CONFIG_IMAGE_SEARCH_PATH,imageSearchPath(appDir, genDir));
        config.put(CONFIG_VECTOR_SEARCH_PATH,vectorSearchPath(appDir, genDir));
        config.put(CONFIG_CONFIG_SEARCH_PATH,configSearchPath(appDir, genDir));
    }

    public void setUpConfig() throws Exception
    {
        InputStream pis;
        pis = getAssets().open("config/settings.plist");
        OBXMLManager xmlManager = new OBXMLManager();
        config = (Map<String,Object>)xmlManager.parsePlist(pis);

        float h = getResources().getDisplayMetrics().heightPixels;
        float w = getResources().getDisplayMetrics().widthPixels;
        if (h > w)
        {
            float temp = w;
            w = h;
            h = temp;
        }
        float graphicScale = h / 768;
        config.put(CONFIG_GRAPHIC_SCALE,graphicScale);

        config.put(CONFIG_DEFAULT_LANGUAGE, configStringForKey(CONFIG_LANGUAGE));
        config.put(CONFIG_LEFT_BUTTON_POS, new PointF(0.0677f,0.075f));
        config.put(CONFIG_RIGHT_BUTTON_POS, new PointF(0.9323f,0.075f));
        List<String> cols = (List<String>) config.get(CONFIG_COLOURS);
        ArrayList<Integer> newcols =  new ArrayList<Integer>();
        for (String s : cols)
        {
            int col = OBUtils.colorFromRGBString(s);
            newcols.add(Integer.valueOf(col));
        }
        config.put(CONFIG_COLOURS,newcols);
        cols = (List<String>) config.get(CONFIG_SKINCOLOURS);
        newcols =  new ArrayList<Integer>();
        for (String s : cols)
        {
            int col = OBUtils.colorFromRGBString(s);
            newcols.add(Integer.valueOf(col));
        }
        config.put(CONFIG_SKINCOLOURS,newcols);
        Object skincolour = config.get(CONFIG_SKINCOLOUR);
        if (skincolour != null && skincolour instanceof String)
        {
            int col = OBUtils.colorFromRGBString((String)skincolour);
            config.put(CONFIG_SKINCOLOUR, Integer.valueOf(col));
        }
        updateConfigPaths((String)config.get(CONFIG_APP_CODE),true);
        String fcname = (String)config.get(CONFIG_FAT_CONTROLLER);
        if (fcname == null)
            fcname = "OBFatController";
        Class aClass = Class.forName("org.onebillion.xprz.utils."+fcname);
        Constructor<?> cons = aClass.getConstructor();
        fatController = (OBFatController)cons.newInstance();
    }

    void retrieveUsers()
    {
    }

    public float applyGraphicScale(float val)
    {
        return val * configFloatForKey(CONFIG_GRAPHIC_SCALE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (renderer != null)
        {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (renderer != null)
        {
            glSurfaceView.onResume();
        }
    }

}

