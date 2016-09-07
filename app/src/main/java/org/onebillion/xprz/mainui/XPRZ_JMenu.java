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
    public static final String titles[] = {"video","reading","stories","numeracy","writing","design","tech"};
    String currentTab = null;
    List<OBControl> tabs;
    Map<String,OBXMLNode>tabXmlDict;
    public float tabTextSize,subheadtextSize,subsubheadtextSize,itemHeadTextSize,itemBodyTextSize;
    static Typeface plain,bold,italic,boldItalic;

    static Typeface plainFont()
    {
        if (plain == null)
            plain = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(),"fonts/F37Ginger-Regular.otf");
        return plain;
    }
    static Typeface boldFont()
    {
        if (bold == null)
            bold = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(),"fonts/F37Ginger-Bold.otf");
        return bold;
    }
    static Typeface italicFont()
    {
        if (italic == null)
            italic = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(),"fonts/F37Ginger-Italic.otf");
        return italic;
    }
    static Typeface boldItalicFont()
    {
        if (boldItalic == null)
            boldItalic = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(),"fonts/F37Ginger-BoldItalic.otf");
        return boldItalic;
    }
    Typeface jFont(boolean isBold,boolean isItalic)
    {
        if (isBold)
            if (isItalic)
                return boldItalicFont();
            else
                return italicFont();
        else
            if (isItalic)
                return italicFont();
        return plainFont();

    }
    public void prepare()
    {
        super.prepare();
        saveConfig = (String)Config().get(MainActivity.CONFIG_APP_CODE);
        loadEvent("mastera");
        tabTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("tabtextsize")));
        subheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subheadtextsize")));
        subsubheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subsubheadtextsize")));
        itemHeadTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itemheadtextsize")));
        itemBodyTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itembodytextsize")));
        tabs = sortedFilteredControls("tab.*");
        setUpTabTitles();

        //switchTo("video");

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

    void setUpTabTitles()
    {
        for (int i = 1;i < tabs.size();i++)
        {
            OBLabel label = new OBLabel(titles[i],plainFont(),tabTextSize);
            label.setColour(Color.WHITE);
            OBControl placeHolder = tabs.get(i);
            label.setPosition(placeHolder.position());
            label.setZPosition(placeHolder.zPosition()+10);
            attachControl(label);
            objectDict.put("tabtitle_"+titles[i],label);
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
        if (placeHolder != null)
        {
            label.setLeft(placeHolder.left());
            label.setTop(placeHolder.top());
            label.setZPosition(objectDict.get(tabstring+"_background").zPosition()+10);
            attachControl(label);
            objectDict.put(tabstring+"sh",label);
        }
    }

    public void populateSubSubHeads(String tabstring)
    {
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> subsubheadnodes = tab.childrenOfType("subsubhead");
        Typeface tf = boldFont();
        int i = 1;
        for (OBXMLNode subsubheadnode : subsubheadnodes)
        {
            String s = "";
            for (OBXMLNode tspan : subsubheadnode.childrenOfType("tspan"))
            {
                s += tspan.contents;
            }
            OBLabel label = new OBLabel(s,tf,subsubheadtextSize);
            label.setColour(Color.WHITE);
            OBControl placeHolder = objectDict.get(tabstring + String.format("_subsubhead%d",i));
            label.setLeft(placeHolder.left());
            label.setTop(placeHolder.top());
            label.setZPosition(objectDict.get(tabstring+"_background").zPosition()+10);
            attachControl(label);
            objectDict.put(tabstring+String.format("_ssh%d",i),label);
            i++;
        }
    }

    public void populateMiscTexts(String tabstring)
    {
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> textnodes = tab.childrenOfType("misctext");
        Typeface tf = boldFont();
        int i = 1;
        for (OBXMLNode textnode : textnodes)
        {
            String s = "";
            for (OBXMLNode tspan : textnode.childrenOfType("tspan"))
            {
                s += tspan.contents;
            }
            float textsize = 12f;
            String attr = textnode.attributeStringValue("textsize");
            if (attr != null)
                textsize = applyGraphicScale(Float.parseFloat(attr));
            OBLabel label = new OBLabel(s,tf,textsize);
            label.setColour(Color.WHITE);
            OBControl placeHolder = objectDict.get(tabstring + String.format("_misctext%d",i));
            label.setLeft(placeHolder.left());
            label.setTop(placeHolder.top());
            label.setZPosition(objectDict.get(tabstring+"_background").zPosition()+10);
            attachControl(label);
            objectDict.put(tabstring+String.format("__misctext%d",i),label);
            i++;
        }
    }

    void populateTargets(String tabstring,OBXMLNode tab,List<String> iconBoxStrings)
    {
        Typeface tf = plainFont();
        Typeface tfb = boldFont();
        List<OBXMLNode> targs = tab.childrenOfType("targets");
        if (targs.size() > 0)
        {
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
                //String boxname = String.format(tabstring+"_iconbox_%d",idx+1);
                String boxname = iconBoxStrings.get(idx);
                OBControl iconbox = objectDict.get(boxname);
                float ratio = iconbox.height() / im.height();
                im.setScale(ratio);
                im.setPosition(iconbox.position());

                //boxname = String.format(tabstring+"_textbox_%d",idx+1);
                boxname = boxname.replaceFirst("icon","text");
                OBControl textbox = objectDict.get(boxname);
                if (textbox != null)
                {
                    OBXMLNode textnode = n.childrenOfType("text").get(0);
                    String s = textnode.attributeStringValue("align");
                    Boolean centred = (s != null && s.equals("centre"));
                    OBXMLNode headnode = textnode.childrenOfType("head").get(0);
                    float top = textbox.top();
                    if (headnode != null)
                    {
                        OBLabel label = new OBLabel(headnode.contents,tfb,itemHeadTextSize);
                        label.setMaxWidth(textbox.width());
                        label.setJustification(centred?OBTextLayer.JUST_CENTER:OBTextLayer.JUST_LEFT);
                        label.sizeToBoundingBox();
                        label.setTop(top);
                        label.setLeft(textbox.left());
                        label.setZPosition(zpos);
                        label.setColour(Color.WHITE);
                        top = label.bottom() + applyGraphicScale(8);
                        //label.setBorderColor(Color.BLACK);
                        //label.setBorderWidth(2f);
                        objectDict.put(tabstring+String.format("_head%d",idx+1),label);
                        attachControl(label);

                    }

                    List<OBXMLNode>nodes = textnode.childrenOfType("body");
                    if (nodes.size() > 0)
                    {
                        OBXMLNode bodynode = nodes.get(0);
                        OBLabel label2 = new OBLabel(bodynode.contents,tf,itemBodyTextSize);
                        label2.setMaxWidth(textbox.width());
                        label2.sizeToBoundingBox();
                        label2.setTop(top);
                        label2.setLeft(textbox.left());
                        label2.setZPosition(zpos);
                        label2.setColour(Color.WHITE);
                        label2.setJustification(centred?OBTextLayer.JUST_CENTER:OBTextLayer.JUST_LEFT);

                        objectDict.put(tabstring+String.format("_bod%d",idx+1),label2);
                        attachControl(label2);
                    }
                }
                idx++;
            }
        }

    }
    public void populateReading()
    {
        String tabstring = "reading";
        //populateSubHead(tabstring);
        OBXMLNode tab = tabXmlDict.get(tabstring);
        Typeface tf = plainFont();
        Typeface tfb = boldFont();
        List<String> targetNames = sortedFilteredControlIDs("reading_iconbox.*");
        populateTargets(tabstring,tab,targetNames);
        List<OBControl> nums = sortedFilteredControls("reading_num.*");
        int i = 1;
        for (OBControl c : nums)
        {
            OBLabel label = new OBLabel(String.format("%d",i),tf,subheadtextSize);
            label.sizeToBoundingBox();
            label.setPosition(c.position());
            label.setZPosition(c.zPosition());
            label.setColour(Color.WHITE);
            //label.setJustification(OBTextLayer.JUST_CENTER);
            objectDict.put("reading_num_"+String.format("_%d",i),label);
            attachControl(label);
            i++;
        }
    populateSubSubHeads(tabstring);
    }

    public void populateStories()
    {
        String tabstring = "stories";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        targetNames.addAll(sortedFilteredControlIDs(tabstring+"_smicon.*"));
        populateTargets(tabstring,tab,targetNames);

        List<OBControl> nums = sortedFilteredControls(tabstring+"_num.*");
        int i = 1;
        for (OBControl c : nums)
        {
            OBLabel label = new OBLabel(String.format("Level %d",i),tf,subheadtextSize);
            label.sizeToBoundingBox();
            label.setPosition(c.position());
            label.setZPosition(c.zPosition());
            label.setColour(Color.WHITE);
            //label.setJustification(OBTextLayer.JUST_CENTER);
            objectDict.put(tabstring+"_num_"+String.format("_%d",i),label);
            attachControl(label);
            i++;
        }
        populateSubSubHeads(tabstring);
    }
    public void populateNumeracy()
    {
        String tabstring = "numeracy";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        targetNames.addAll(sortedFilteredControlIDs(tabstring+"_smicon.*"));
        populateTargets(tabstring,tab,targetNames);
        populateSubSubHeads(tabstring);
    }

    public void populateWriting()
    {
        String tabstring = "writing";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        targetNames.addAll(sortedFilteredControlIDs(tabstring+"_smicon.*"));
        populateTargets(tabstring,tab,targetNames);
        populateSubSubHeads(tabstring);
    }

    public void populateDesign()
    {
        String tabstring = "design";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        populateTargets(tabstring,tab,targetNames);
        populateSubSubHeads(tabstring);
    }
    public void populateTech()
    {
        String tabstring = "tech";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        populateTargets(tabstring,tab,targetNames);
        populateMiscTexts(tabstring);
        populateSubSubHeads(tabstring);
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
        else if (s.equals("stories"))
            populateStories();
        else if (s.equals("writing"))
            populateWriting();
        else if (s.equals("design"))
            populateDesign();
        else if (s.equals("tech"))
            populateTech();
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


