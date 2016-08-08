package org.onebillion.xprz.mainui;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.onebillion.xprz.R;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 05/08/16.
 */
public class XPRZ_ListMenu extends OBSectionController
{
    private ListView listView;
    private List<OBXMLNode> masterList;
    private OBUnitAdapter unitAdapter;
    private OBXMLNode[] array;

    public XPRZ_ListMenu ()
    {
        super(MainActivity.mainActivity, false);
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
            OBXMLManager xmlManager = new OBXMLManager();
            //
            InputStream is = OBUtils.getInputStreamForPath("xprize-menu/units.xml");
            List<OBXMLNode> xml = xmlManager.parseFile(is);
            //
            OBXMLNode rootNode = xml.get(0);
            masterList = new ArrayList<OBXMLNode>();
            for (OBXMLNode levelNode : rootNode.childrenOfType("level"))
            {
                masterList.addAll(levelNode.childrenOfType("unit"));
            }
            array = masterList.toArray(new OBXMLNode[masterList.size()]);
            unitAdapter = new OBUnitAdapter(MainActivity.mainActivity, array, this);
            //
            if (listView == null)
            {
                listView = new ListView(MainActivity.mainActivity);
                listView.setAdapter(unitAdapter);
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
        MainActivity.mainActivity.setContentView(listView);
    }


    @Override
    public int buttonFlags ()
    {
        return 0;
    }
}
