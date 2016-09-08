package org.onebillion.xprz.mainui;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.service.carrier.CarrierMessagingService;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;
import org.onebillion.xprz.utils.OB_Maths;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XPRZ_JudgeMenu extends OBSectionController
{
    String saveConfig;
    private WebView webView;
    private Map<String, OBXMLNode> masterList;
    private Boolean firstRun;

    public XPRZ_JudgeMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void viewWillAppear (Boolean animated)
    {
        super.viewWillAppear(animated);
        for (OBControl c : filterControls("button.*"))
        {
            c.lowlight();
        }
        if (saveConfig != null)
        {
            MainActivity.mainActivity.updateConfigPaths(saveConfig, false);
        }
    }

    public void loadMasterList () throws Exception
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                try
                {
                    OBXMLManager xmlManager = new OBXMLManager();
                    //
//                URL url = new URL(getURL("units.xml", false));
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.connect();
//                List<OBXMLNode> xml = xmlManager.parseFile(urlConnection.getInputStream());
                    //
                    String url = getURL("units.xml", true);
                    if (url != null)
                    {
                        InputStream is = OBUtils.getInputStreamForPath("xprize-menu/units.xml");
                        List<OBXMLNode> xml = xmlManager.parseFile(is);
                        //
                        OBXMLNode rootNode = xml.get(0);
                        masterList = new HashMap<String, OBXMLNode>();
                        for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                        {
                            for (OBXMLNode unitNode : levelNode.childrenOfType("unit"))
                            {
                                String id = unitNode.attributeStringValue("id");
                                String breakdown[] = id.split("\\.");
                                String unitNumber = breakdown[0];
                                masterList.put(unitNumber, unitNode);
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    public void loadUnit (String id)
    {
        OBXMLNode xmlNode = masterList.get(id);
        if (xmlNode == null)
        {
            MainActivity.log("Unknown Unit: " + id);
        }
        else
        {
            String target = xmlNode.attributeStringValue("target");
            final String parameters = xmlNode.attributeStringValue("params");
            String config = xmlNode.attributeStringValue("config");
            String lang = xmlNode.attributeStringValue("lang");
            MainActivity.mainActivity.updateConfigPaths(config, false);
            String breakdown[] = config.split("\\/");
            final String className = breakdown[0].replace("-", "_") + "." + target;
            MainActivity.mainActivity.updateConfigPaths(config, true, lang);
            this.onPause();
            //
            swapViews(className, parameters);
        }
    }


    private void swapViews (String nm, Object _params)
    {
        try
        {
            Bitmap image = takeScreenshot();
            XPRZ_SwapperMenu controller = new XPRZ_SwapperMenu(image, nm, _params);
            //
            OBRenderer renderer = MainActivity.mainActivity.renderer;
            if (renderer != null)
            {
                controller.setViewPort(0, 0, renderer.w, renderer.h);
            }
            controller.viewWillAppear(false);
            controller.prepare();
            //
            //
            MainViewController().viewControllers.add(controller);
            //
            final OBSectionController vc = controller;
            new Handler().post(new Runnable()
            {
                @Override
                public void run ()
                {
                    vc.start();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Bitmap takeScreenshot ()
    {
        try
        {
            View v1 = MainActivity.mainActivity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            return bitmap;
        }
        catch (Throwable e)
        {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void prepare ()
    {
        super.prepare();
        firstRun = true;
        //
        try
        {
            saveConfig = (String) Config().get(MainActivity.CONFIG_APP_CODE);
            loadMasterList();

            if (webView == null)
            {
                webView = new WebView(MainActivity.mainActivity);
                webView.setWebChromeClient(new WebChromeClient());
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.setWebViewClient(new WebViewClient()
                {
                    public void onPageFinished (WebView view, String url)
                    {
                        if (url.contains("#"))
                        {
                            MainActivity.log(url);
                            String breakdown[] = url.split("#");
                            String unit = breakdown[breakdown.length - 1];
                            loadUnit(unit);
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading (WebView view, String url)
                    {
                        view.loadUrl(url);
                        return false;
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void start ()
    {
        if (firstRun)
        {
            String mainPage = getURL("beta.html", true);
            webView.loadUrl(mainPage);
            firstRun = false;
            //
//             Intent intent = new Intent(android.provider.Settings.ACTION_DATE_SETTINGS);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//            MainActivity.mainActivity.startActivity(intent);
        }
        MainActivity.mainActivity.setContentView(webView);
        //
        if (!firstRun) webView.onResume();
    }


    public String getURL (String file, Boolean forWeb)
    {
        String path = OBUtils.getAbsolutePathForFile(String.format("xprize-menu/%s", file));
        if (path != null)
        {
            return (forWeb ? "file:///" : "") + path;
        }
        return null;
    }

    @Override
    public void onResume ()
    {
        super.onResume();
        start();
    }

    @Override
    public void onPause ()
    {
        super.onPause();
        webView.onPause();
    }

    @Override
    public int buttonFlags ()
    {
        return 0;
    }
}
