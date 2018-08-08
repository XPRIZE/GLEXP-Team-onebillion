package org.onebillion.onecourse.mainui;

import android.content.res.AssetFileDescriptor;
import android.text.Html;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class OBSetupMenu_AttributionsView extends OBSectionController
{

    public OBSetupMenu_AttributionsView ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void initScreen()
    {
        MainActivity.mainActivity.setContentView(R.layout.layout_web);
        final WebView webView = (WebView) MainActivity.mainActivity.findViewById(R.id.webview);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        webView.setLongClickable(false);
        //
        String fileName = "attributions.html";
        String url = null;
        //
        for (File externalDir : OBConfigManager.sharedManager.getExternalAssetsSearchPaths())
        {
            try
            {
                String externalDirPath = externalDir.getPath();
                if (!externalDirPath.endsWith("/")) externalDirPath = externalDirPath + "/";
                //
                File externalAssetPath = new File(externalDirPath + fileName);
                //
                Boolean fileExists = externalAssetPath.exists();
                if (fileExists)
                {
                    url = externalAssetPath.getAbsolutePath();
                    break;
                }
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
        //
        if (url != null)
        {
            webView.loadUrl("file://" + url);
        }
        //
        Button backButton = (Button) MainActivity.mainActivity.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MainActivity.log("Back arrow triggered!");
                MainActivity.mainViewController.pushViewControllerWithNameConfig("OBSetupMenu", "", false, false, "menu", true);
            }
        });

    }

    public void prepare()
    {
        setStatus(STATUS_IDLE);
        initScreen();
    }
}
