package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBTextLayer;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 01/09/16.
 */
public class XPRZ_JMenu extends XPRZ_Menu
{
    public static final String titles[] = {"video","numeracy","reading","writing","design","technology"};
    String currentTab = null;
    List<OBControl> tabs;
    Map<String,OBXMLNode>tabXmlDict;
    public float tabTextSize,subheadtextSize,itemHeadTextSize,itemBodyTextSize;

    public void prepare()
    {
        super.prepare();
        saveConfig = (String)Config().get(MainActivity.CONFIG_APP_CODE);
        loadEvent("mastera");
        tabTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("tabtextsize")));
        subheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subheadtextsize")));
        itemHeadTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itemheadtextsize")));
        itemBodyTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itembodytextsize")));
        tabs = sortedFilteredControls("tab.*");
        switchTo("video");

    }

    void loadTabContents()
    {
        tabXmlDict = new HashMap<>();
        String xmlPath = getConfigPath("tabcontent.xml");
        try
        {
            OBXMLManager xmlManager = new OBXMLManager();
            List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
            OBXMLNode xmlNode = xl.get(0);
            List<OBXMLNode> xmltabs = xmlNode.childrenOfType("tab");
            for (OBXMLNode n : xmltabs)
            {
                String key = n.attributeStringValue("id");
                tabXmlDict.put(key,n);
            }
        }
        catch (Exception e)
        {

        }
    }

    public void populateSubHead(String tabstring)
    {
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> subheadnodes = tab.childrenOfType("subhead");
        if (subheadnodes.size() == 0)
            return;
        OBXMLNode subheadnode = subheadnodes.get(0);
        String s = "";
        for (OBXMLNode tspan : subheadnode.childrenOfType("tspan"))
        {
            s += tspan.contents;
        }
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(s,tf,subheadtextSize);
        label.setColour(Color.WHITE);
        OBControl placeHolder = objectDict.get(tabstring +"_subhead");
        label.setLeft(placeHolder.left());
        label.setTop(placeHolder.top());
        label.setZPosition(objectDict.get(tabstring+"_background").zPosition()+10);
        attachControl(label);
        objectDict.put(tabstring+"sh",label);
    }

    public void populateReading()
    {
        String tabstring = "reading";
        populateSubHead(tabstring);
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> targs = tab.childrenOfType("targets");
        if (targs.size() > 0)
        {
            Typeface tf = OBUtils.standardTypeFace();
            List<OBXMLNode> nodeTargs = targs.get(0).childrenOfType("target");
            int idx = 0;
            PointF pt = new PointF();
            float zpos = objectDict.get(tabstring+"_background").zPosition()+10;
            for (OBXMLNode n : nodeTargs)
            {
                OBXMLNode iconnode = n.childrenOfType("icon").get(0);
                String src = iconnode.attributeStringValue("src");
                OBControl im = loadImageWithName(src,pt,boundsf(),true);
                im.setZPosition(zpos);
                objectDict.put(tabstring+String.format("_i%d",idx+1),im);
                String boxname = String.format(tabstring+"_iconbox_%d",idx+1);
                OBControl iconbox = objectDict.get(boxname);
                float ratio = iconbox.height() / im.height();
                im.setScale(ratio);
                im.setPosition(iconbox.position());

                boxname = String.format(tabstring+"_textbox_%d",idx+1);
                OBControl textbox = objectDict.get(boxname);
                OBXMLNode textnode = n.childrenOfType("text").get(0);
                OBXMLNode headnode = textnode.childrenOfType("head").get(0);

                OBLabel label = new OBLabel(headnode.contents,tf,itemHeadTextSize);
                label.setMaxWidth(textbox.width());
                label.sizeToBoundingBox();
                label.setTop(textbox.top());
                label.setLeft(textbox.left());
                label.setZPosition(zpos);
                label.setColour(Color.WHITE);
                label.setJustification(OBTextLayer.JUST_LEFT);
                objectDict.put(tabstring+String.format("_head%d",idx+1),label);
                attachControl(label);

                OBXMLNode bodynode = textnode.childrenOfType("body").get(0);
                if (bodynode != null)
                {
                    OBLabel label2 = new OBLabel(bodynode.contents,tf,itemBodyTextSize);
                    label2.setMaxWidth(textbox.width());
                    label2.sizeToBoundingBox();
                    label2.setTop(label.bottom());
                    label2.setLeft(textbox.left());
                    label2.setZPosition(zpos);
                    label2.setColour(Color.WHITE);
                    label2.setJustification(OBTextLayer.JUST_LEFT);

                    objectDict.put(tabstring+String.format("_bod%d",idx+1),label2);
                    attachControl(label2);
                }
                idx++;
            }
        }

    }
    public void populateNumeracy()
    {
        String tabstring = "numeracy";
        populateSubHead(tabstring);
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> targs = tab.childrenOfType("targets");
        if (targs.size() > 0)
        {
            Typeface tf = OBUtils.standardTypeFace();
            List<OBXMLNode> nodeTargs = targs.get(0).childrenOfType("target");
            int idx = 0;
            PointF pt = new PointF();
            float zpos = objectDict.get(tabstring+"_background").zPosition()+10;
            for (OBXMLNode n : nodeTargs)
            {
                OBXMLNode iconnode = n.childrenOfType("icon").get(0);
                String src = iconnode.attributeStringValue("src");
                OBControl im = loadImageWithName(src,pt,boundsf(),true);
                im.setZPosition(zpos);
                objectDict.put(tabstring+String.format("_i%d",idx+1),im);
                String boxname = String.format(tabstring+"_iconbox_%d",idx+1);
                OBControl iconbox = objectDict.get(boxname);
                float ratio = iconbox.height() / im.height();
                im.setScale(ratio);
                im.setPosition(iconbox.position());

                boxname = String.format(tabstring+"_textbox_%d",idx+1);
                OBControl textbox = objectDict.get(boxname);
                OBXMLNode textnode = n.childrenOfType("text").get(0);
                OBXMLNode headnode = textnode.childrenOfType("head").get(0);

                OBLabel label = new OBLabel(headnode.contents,tf,itemHeadTextSize);
                label.setMaxWidth(textbox.width());
                label.sizeToBoundingBox();
                label.setTop(textbox.top());
                label.setLeft(textbox.left());
                label.setZPosition(zpos);
                label.setColour(Color.WHITE);
                label.setJustification(OBTextLayer.JUST_LEFT);
                objectDict.put(tabstring+String.format("_head%d",idx+1),label);
                attachControl(label);

                OBXMLNode bodynode = textnode.childrenOfType("body").get(0);
                OBLabel label2 = new OBLabel(bodynode.contents,tf,itemBodyTextSize);
                label2.setMaxWidth(textbox.width());
                label2.sizeToBoundingBox();
                label2.setTop(label.bottom());
                label2.setLeft(textbox.left());
                label2.setZPosition(zpos);
                label2.setColour(Color.WHITE);
                label2.setJustification(OBTextLayer.JUST_LEFT);

                objectDict.put(tabstring+String.format("_bod%d",idx+1),label2);
                attachControl(label2);
                idx++;
            }
        }
    }

    public void switchTo(String s)
    {
        if (s == currentTab)
            return;
        lockScreen();
        if (currentTab != null)
            deleteControls(currentTab+".*");
        loadEvent(s);
        if (tabXmlDict == null)
            loadTabContents();
        if (s.equals("numeracy"))
            populateNumeracy();
        else if (s.equals("reading"))
            populateReading();
        unlockScreen();
        currentTab = s;
    }

    public OBControl findTab(PointF pt)
    {
        for (OBControl tab : tabs)
            if (tab.frame().contains(pt.x,pt.y))
                return tab;
        return null;
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() != STATUS_IDLE)
            return;
        OBControl c = findTab(pt);
        if (c != null)
        {
            int idx = tabs.indexOf(c);
            switchTo(titles[idx]);
        }
    }

}


