package org.onebillion.xprz.mainui;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.onebillion.xprz.R;
import org.onebillion.xprz.glstuff.OBGLView;
import org.onebillion.xprz.utils.OBUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by pedroloureiro on 07/07/16.
 */
public class XPRZ_JudgeMenu extends OBSectionController
{
    public XPRZ_JudgeMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    @Override
    public void prepare ()
    {
        try
        {
            WebView webView = new WebView(MainActivity.mainActivity);
            webView.setWebViewClient(new WebViewClient()
            {
                @Override
                public boolean shouldOverrideUrlLoading (WebView view, String url)
                {
                    view.loadUrl(url);
                    return true;
                }
            });
//            String url = getURL();
//            String url = "http://www.google.com";
            String url = "http://ting.onebillion.org:5007/xprize-menu/index.html";
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(url);
            MainActivity.mainActivity.setContentView(webView);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }




    public String getURL()
    {
        for (File mounted : OBExpansionManager.sharedManager.getExternalExpansionFolders())
        {
            File extendedFile = new File(MainActivity.mainActivity.getFilesDir() + File.separator + mounted.getName() + "/xprize-menu/index.html");
            if (extendedFile.exists()) return "file:///" + extendedFile.getAbsolutePath();
        }
        return null;
    }


    @Override
    public int buttonFlags ()
    {
        return 0;
    }
}
