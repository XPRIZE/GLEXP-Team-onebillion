package com.maq.xprize.onecourse.hindi.mainui;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.maq.xprize.onecourse.hindi.R;
import com.maq.xprize.onecourse.hindi.utils.OBConfigManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
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
        TextView textView = (TextView) MainActivity.mainActivity.findViewById(R.id.textview);
        textView.setTextColor(Color.BLACK);
        //
//        final WebView webView = (WebView) MainActivity.mainActivity.findViewById(R.id.webview);
//        webView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                return true;
//            }
//        });
//        webView.setLongClickable(false);
        //
        String fileName = "attributions.html";
//        String fileName = "ATTRIBUTIONS.md";
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
            String filePath = url;
            String contentHTML = readFileUsingBufferedReader(filePath);
            Spanned content = Html.fromHtml(contentHTML);
            textView.setText(content);
//            webView.loadUrl("file://" + url);
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

    private static String readFileUsingBufferedReader(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath)))
        {

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null)
            {
                contentBuilder.append(sCurrentLine).append("\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public void prepare()
    {
        setStatus(STATUS_IDLE);
        initScreen();
    }
}
