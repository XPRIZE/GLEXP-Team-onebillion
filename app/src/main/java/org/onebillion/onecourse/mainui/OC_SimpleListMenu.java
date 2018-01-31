package org.onebillion.onecourse.mainui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.onebillion.onecourse.R;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 11/12/17.
 */

public class OC_SimpleListMenu extends OBSectionController
{
    private ListView listView;
    List<MlUnit> masterList;

    public OC_SimpleListMenu ()
    {
        super(MainActivity.mainActivity, false);
    }

    public void initScreen()
    {
        MainActivity.mainActivity.setContentView(R.layout.simple_list_menu);
        listView = (ListView)MainActivity.mainActivity.findViewById(R.id.OB_simplelistview);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                MlUnit m = masterList.get(position);
                setStatus(STATUS_BUSY);
                String configName = m.config;
                if (configName == null)
                {
                    String appDir = OBConfigManager.sharedManager.getCurrentActivityFolder();
                    String[] comps = appDir.split("/");
                    configName = comps[0];
                }
                else
                {
                    OBConfigManager.sharedManager.updateConfigPaths(configName, false,m.lang);
                }
                if (!MainActivity.mainViewController.pushViewControllerWithNameConfig(m.target,configName,true,true,m.params))
                    setStatus(STATUS_IDLE);

            }
        });
        List<String> sarray = new ArrayList<>();
        for (MlUnit ml : masterList)
        {
            String s =  String.format("%s/%s/%s",ml.key,ml.target,ml.params);
            sarray.add(s);
        }
        //
        TextView buildNumber = (TextView) MainActivity.mainActivity.findViewById(R.id.buildNumber);
        if (buildNumber != null)
        {
            String buildNumberValue = OBConfigManager.sharedManager.getBuildNumber();
            if (buildNumberValue == null)
            {
                buildNumberValue = "Missing BuildNo";
            }
            buildNumber.setText(buildNumberValue);
            buildNumber.setBackgroundColor(Color.WHITE);
        }
        //
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.mainActivity,android.R.layout.simple_list_item_1,sarray)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position,convertView,parent);
                // Get the Layout Parameters for ListView Current Item View
                ViewGroup.LayoutParams params = view.getLayoutParams();
                // Set the height of the Item View
                params.height = 175;
                view.setLayoutParams(params);
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
    }

    List<MlUnit> loadUnitXML(String xmlPath)
    {
        List<MlUnit> arr = new ArrayList<>();
        if(xmlPath != null)
    {
        OBXMLManager xmlManager = new OBXMLManager();
        OBXMLNode xmlNode = null;
        try
        {
            List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
            xmlNode = xl.get(0);
            for(OBXMLNode xmlLevel :  xmlNode.childrenOfType("level"))
            {
                List<OBXMLNode> xmlunits = xmlLevel.childrenOfType("unit");
                for(OBXMLNode n : xmlunits)
                {
                    MlUnit m = MlUnit.mlUnitFromXMLNode(n);
                    m.key = n.attributeStringValue("id");
                    arr.add(m);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    return arr;
    }

    void loadUnits()
    {
        String mlname = OBConfigManager.sharedManager.getMasterlist();
        masterList = loadUnitXML(String.format("masterlists/%s/units.xml", mlname));
    }

    public void prepare()
    {
        setStatus(STATUS_IDLE);
        loadUnits();
        initScreen();
    }

    }
