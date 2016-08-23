package org.onebillion.xprz.mainui;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 07/07/16.
 */
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
            MainActivity.mainActivity.log("Unknown Unit: " + id);
        }
        else
        {
            String target = xmlNode.attributeStringValue("target");
            String parameters = xmlNode.attributeStringValue("params");
            String config = xmlNode.attributeStringValue("config");
            String lang = xmlNode.attributeStringValue("lang");
            MainActivity.mainActivity.updateConfigPaths(config, false);
            String breakdown[] = config.split("\\/");
            String className = breakdown[0].replace("-", "_") + "." + target;
            MainActivity.mainActivity.updateConfigPaths(config, false, lang);
            MainViewController().pushViewControllerWithName(className, true, false, parameters);
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
                //
                webView.setWebViewClient(new WebViewClient()
                {
                    public void onPageFinished (WebView view, String url)
                    {
                        if (url.contains("#"))
                        {
                            MainActivity.mainActivity.log(url);
                            String breakdown[] = url.split("#");
                            String unit = breakdown[breakdown.length - 1];
                            loadUnit(unit);
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading (WebView view, String url)
                    {
                        view.loadUrl(url);
//                        MainActivity.mainActivity.log("Updating current URL: " + url);
                        return false;
                    }
                });
                //
//                webView.setOnTouchListener(new View.OnTouchListener()
//                {
//                    @Override
//                    public boolean onTouch (View v, MotionEvent event)
//                    {
//                        if (event.getAction() == MotionEvent.ACTION_UP)
//                        {
//                            WebView.HitTestResult hr = ((WebView) v).getHitTestResult();
//                            if (hr == null) return false;
//                            //
//                            MainActivity.mainActivity.log("" + hr.getType() + " " + event.toString());
//                            //
//                            String iconFile = hr.getExtra();
//                            if (iconFile != null)
//                            {
//                                lastResult = hr.getExtra();
//                                v.cancelPendingInputEvents();
//                                //
//                                String breakdown[] = iconFile.split("/");
//                                String icon = breakdown[breakdown.length - 1];
//                                icon = icon.replace(".png", "").replace("_big", "");
//                                MainActivity.mainActivity.log("Loading UNIT: " + icon);
//                                loadUnit(icon);
//                                return false;
//                            }
//                        }
//                        return false;
//                    }
//                });
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
        }
        else
        {

        }
        MainActivity.mainActivity.setContentView(webView);
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
    public int buttonFlags ()
    {
        return 0;
    }
}
