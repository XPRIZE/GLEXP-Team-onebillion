package org.onebillion.xprz.mainui;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 07/07/16.
 */
public class XPRZ_JudgeMenu extends OBSectionController
{
    private WebView webView;
    private List<OBXMLNode> masterList;

    public XPRZ_JudgeMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void loadMasterList () throws Exception
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                OBXMLManager xmlManager = new OBXMLManager();
                //
//                URL url = new URL(getURL("units.xml", false));
//                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.connect();
//                List<OBXMLNode> xml = xmlManager.parseFile(urlConnection.getInputStream());
                //
                String url = getURL("units.xml", false);
                InputStream is = OBUtils.getInputStreamForPath(url);
                List<OBXMLNode> xml = xmlManager.parseFile(is);
                //
                OBXMLNode rootNode = xml.get(0);
                masterList = new ArrayList<OBXMLNode>();
                for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
                {
                    masterList.addAll(levelNode.childrenOfType("unit"));
                }
            }
        });
    }


    public void loadUnit (int unitIndex)
    {
        OBXMLNode xmlNode = masterList.get(unitIndex);
        String target = xmlNode.attributeStringValue("target");
        String parameters = xmlNode.attributeStringValue("params");
        String config = xmlNode.attributeStringValue("config");
        MainActivity.mainActivity.updateConfigPaths(config, false);
        String className = config.replace("-", "_") + "." + target;
        MainViewController().pushViewControllerWithName(className, true, false, parameters);
    }


    @Override
    public void prepare ()
    {
        try
        {
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
                            String breakdown[] = url.split("#");
                            int unitIndex = Integer.parseInt(breakdown[breakdown.length - 1]);
                            loadUnit(unitIndex);
                        }
                        return;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading (WebView view, String url)
                    {
                        view.loadUrl(url);
                        return true;
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
        webView.loadUrl(getURL("index.html", true));
        MainActivity.mainActivity.setContentView(webView);
    }


    public String getURL (String file, Boolean forWeb)
    {
//        return "http://ting.onebillion.org:5007/xprize-menu/" + file;
        for (File mounted : OBExpansionManager.sharedManager.getExternalExpansionFolders())
        {
            File extendedFile = new File(MainActivity.mainActivity.getFilesDir() + File.separator + mounted.getName() + "/xprize-menu/");
            if (extendedFile.exists()) return (forWeb ? "file:///" : "")  + extendedFile.getAbsolutePath() + File.separator + file;
        }
        return null;
    }


    @Override
    public int buttonFlags ()
    {
        return 0;
    }
}
