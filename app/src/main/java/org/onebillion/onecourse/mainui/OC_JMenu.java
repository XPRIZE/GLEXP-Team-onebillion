package org.onebillion.onecourse.mainui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 01/09/16.
 */
public class OC_JMenu extends OC_Menu
{
    public static final String titles[] = {"video","reading","stories","numeracy","writing","design","tech"};
    static float intervalSecs = 60 * 60;
    long lastMessageShowTime;
    private Runnable messageCheckRunnable;
    private Handler messageCheckHandler = new Handler();
    String currentTab = null;
    List<OBControl> tabs;
    Map<String,OBXMLNode>tabXmlDict;
    public float tabTextSize,subheadtextSize,subsubheadtextSize,itemHeadTextSize,itemBodyTextSize,videoHeadTextSize,videoBodyTextSize,
    toggleTextSize,videoPreviewTextHeadingSize,videoPreviewSubTextSize;
    static Typeface plain,bold,italic,boldItalic;
    boolean scrollable;
    float originalY,maximumY,minimumY;
    OBControl scrollGroup;
    PointF lastPoint = new PointF();
    PointF lastLastPoint = new PointF();
    PointF firstPoint = new PointF();
    long lastMoveEvent,lastlastMoveEvent;
    float scrollSpeed;
    int videoPreviewIdx = 0;
    List<OBControl> videoPreviewImages;
    OBGroup videoPreviewGroup;
    int  videoScrollState;
    int intro_video_state = 0;
    boolean full_screen_enabled = true;
    final static int ivs_act_normal = 0,
    ivs_before_play = 1,
    ivs_playing_full_screen = 2;
    final static int VIDEO_SCROLL_NONE=0,
        VIDEO_SCROLL_TOUCH_DOWNED = 1,
        VIDEO_SCROLL_MOVED = 2;
    PointF videoTouchDownPoint = new PointF();
    OBVideoPlayer videoPlayer;
    String movieFolder;
    boolean slowingDown;
    List<OBXMLNode>masterList;
    List<String>languages,languageNames;
    OBControl highlightedIcon = null;
    OBControl scrollMask = null;
    int chosenLanguage;
    boolean inited = false,showingMessage = false;

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

    public int buttonFlags ()
    {
        return 0;
    }

    public void loadMasterList(String lang)
    {
        InputStream pis;
        try
        {
            pis = OBUtils.getInputStreamForPath("oc-jmenu/config/junits_"+lang+".xml");
//            pis = MainActivity.mainActivity.getAssets().open("oc-jmenu/config/junits_"+lang+".xml");
            OBXMLManager xmlManager = new OBXMLManager();
            List<OBXMLNode> xmlNodes = xmlManager.parseFile(pis);
            OBXMLNode xmlNode = xmlNodes.get(0);
            List<OBXMLNode>levellist = xmlNode.childrenOfType("level");
            masterList = levellist.get(0).childrenOfType("unit");
        }
        catch (Exception e)
        {

        }
    }

    void scheduleMessageHandler()
    {
        long lastEventTime;
        if (lastMessageShowTime > MainViewController().lastTouchActivity)
            lastEventTime = lastMessageShowTime;
        else
            lastEventTime = MainViewController().lastTouchActivity;
        long nextEventTime = lastEventTime + (long)(intervalSecs * 1000);
        long interval = nextEventTime - System.currentTimeMillis();
        if (interval > 0)
        {
            if (messageCheckRunnable == null)
            {
                messageCheckRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        checkMessageNow();
                    }
                };
            }
            messageCheckHandler.removeCallbacks(messageCheckRunnable);
            messageCheckHandler.postDelayed(messageCheckRunnable,interval);
        }
    }

    void showMessage()
    {
        if (showingMessage)
            return;
        showingMessage = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.mainActivity);
        builder.setMessage("This is a demo of onebillion’s onecourse app for reading, writing and numeracy. " +
                "You can try a selection of learning units from all ten levels of onecourse, and see videos of children learning.\n" +
                "\n" +
                "The child’s experience of onecourse is different. They begin at level 1 and work through their learning journey guided by Anna, our teacher figure. " +
                "You can try the child’s experience of onecourse Swahili on the other tablet provided by XPRIZE.")
                .setTitle("Note for XPRIZE judges");
        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                showingMessage = false;
                lastMessageShowTime = System.currentTimeMillis();
                scheduleMessageHandler();
            }});
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }
    void createToggleLabels()
    {
        OBLabel label = new OBLabel(languageNames.get(0).toUpperCase(),plainFont(),toggleTextSize);
        label.setColour(Color.argb((int)(0.8f * 255),255,255,255));
        OBControl placeHolder = objectDict.get("toggleleft");
        label.setPosition(placeHolder.position());
        label.setZPosition(placeHolder.zPosition()+10);
        attachControl(label);
        objectDict.put("toggletextl",label);
        label = new OBLabel(languageNames.get(1).toUpperCase(),plainFont(),toggleTextSize);
        label.setColour(Color.argb((int)(0.8f * 255),255,255,255));
        placeHolder = objectDict.get("toggleright");
        label.setPosition(placeHolder.position());
        label.setZPosition(placeHolder.zPosition()+10);
        attachControl(label);
        objectDict.put("toggletextr",label);
    }
    public void prepare()
    {
        super.prepare();
        languages = (List<String>)Config().get("languages");
        languageNames = (List<String>)Config().get("languagenames");
        loadMasterList(languages.get(0));
        saveConfig = (String)Config().get(MainActivity.CONFIG_APP_CODE);
        loadEvent("mastera");
        tabTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("tabtextsize")));
        subheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subheadtextsize")));
        subsubheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subsubheadtextsize")));
        itemHeadTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itemheadtextsize")));
        itemBodyTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itembodytextsize")));
        videoHeadTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("videoheadtextsize")));
        videoBodyTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("videobodytextsize")));
        toggleTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("toggletextsize")));
        videoPreviewTextHeadingSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("videopreviewtextheadingsize")));
        videoPreviewSubTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("videopreviewsubtextsize")));
        tabs = sortedFilteredControls("tab.*");
        for (OBControl t : tabs)
            t.setZPosition(t.zPosition()+60);
        OBControl obl = objectDict.get("onecourse");
        obl.setZPosition(obl.zPosition()+60);
        setUpTabTitles();
        createToggleLabels();
        hideControls("toggle.*");
        setToggleTo(0);
    }

    public void videoinit()
    {
        OBRenderer rn = MainActivity.mainActivity.renderer;

        while (rn.colourProgram == null)
            try
            {
                waitForSecs(0.1);
            }
            catch(Exception e)
            {

            }
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                switchTo("video",false);
                showMessage();

            }
        });
    }
    public void start()
    {
        super.start();
        setStatus(STATUS_IDLE);
        //blankTextureID(2);
        if (videoPlayer != null)
            videoPlayer.frameIsAvailable = false;
        if (!inited)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    videoinit();
                }
            });
            inited = true;
        }
    }

    void setToggleTo(int i)
    {
        OBControl onc = objectDict.get("toggleleft");
        OBControl offc = objectDict.get("toggleright");
        if (i == 1)
        {
            OBControl swap = offc;
            offc = onc;
            onc = swap;
        }
        onc.setOpacity(0.3f);
        offc.setOpacity(0);
        if (currentTab != null)
            switchTo(currentTab,true);
    }

    String currentLang()
    {
        return languages.get(chosenLanguage);
        /*if (chosenLanguage == 1)
            return "en";
        else
            return "sw";*/
    }
    void chooseToggle(int i)
    {
        chosenLanguage = i;
        String l = currentLang();
        loadMasterList(l);
        loadTabContents(l);
        setToggleTo(i);
    }

    void loadTabContents(String lang)
    {
        tabXmlDict = new HashMap<>();
        String xmlPath = getConfigPath("tabcontent_"+lang+".xml");
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
        Typeface tf = plainFont();
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
            OBControl flag = objectDict.get(tabstring + String.format("_flag%d",i));
            float y = flag.position().y;
            label.setPosition(label.position().x,y);
            i++;
        }
        List<OBControl> lst = sortedFilteredControls(tabstring+"_ssh.*");
        for (int j = 1;j < lst.size();j++)
        {
            OBLabel l = (OBLabel) lst.get(j);
            l.setTypeFace(plainFont());
        }
    }

    static void setPropNotNull(OBXMLNode node,OBControl cnt,String name)
    {
        String s = node.attributeStringValue(name);
        if (s != null)
            cnt.setProperty(name,s);
    }

    void populateTargets(String tabstring,OBXMLNode tab,List<String> iconBoxStrings,int startidx)
    {
        Typeface tf = plainFont();
        Typeface tfb = boldFont();
        List<OBXMLNode> targs = tab.childrenOfType("targets");
        float screenBottom = boundsf().bottom;
        if (targs.size() > 0)
        {
            List<OBXMLNode> nodeTargs = targs.get(0).childrenOfType("target");
            int idx = 0;
            PointF pt = new PointF();
            float zpos = objectDict.get(tabstring+"_background").zPosition()+10;
            for (OBXMLNode n : nodeTargs)
            {
                OBXMLNode levelnode = masterList.get(startidx++);

                String src = levelnode.attributeStringValue("icon");
                String srcb = src + currentLang() +  "_big";
                OBControl im = loadImageWithName(srcb,pt,boundsf(),true);
                if (im == null)
                {
                    srcb = src +  "_big";
                    im = loadImageWithName(srcb,pt,boundsf(),true);
                }
                im.setZPosition(zpos);
                objectDict.put(tabstring+String.format("_iconi%d",idx+1),im);
                String boxname = iconBoxStrings.get(idx);
                OBControl iconbox = objectDict.get(boxname);
                float ratio = iconbox.height() / im.height();
                im.setScale(ratio);
                im.setPosition(iconbox.position());
                setPropNotNull(levelnode,im,"target");
                setPropNotNull(levelnode,im,"params");
                setPropNotNull(levelnode,im,"config");
                setPropNotNull(levelnode,im,"lang");
                if (im.bottom() > screenBottom)
                    scrollable = true;
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
                        label.setLineSpaceMultiplier(1.2f);
                        label.setJustification(centred?OBTextLayer.JUST_CENTRE:OBTextLayer.JUST_LEFT);
                        label.sizeToBoundingBox();
                        label.setTop(top);
                        label.setLeft(textbox.left());
                        label.setZPosition(zpos);
                        label.setColour(Color.WHITE);
                        top = label.bottom() + applyGraphicScale(9);
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
                        label2.setLineSpaceMultiplier(1.4f);
                        label2.setMaxWidth(textbox.width());
                        label2.sizeToBoundingBox();
                        label2.setTop(top);
                        label2.setLeft(textbox.left());
                        label2.setZPosition(zpos);
                        label2.setColour(Color.WHITE);
                        label2.setJustification(centred?OBTextLayer.JUST_CENTRE:OBTextLayer.JUST_LEFT);

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
        showControls("toggle.*");
        String tabstring = "reading";
        //populateSubHead(tabstring);
        OBXMLNode tab = tabXmlDict.get(tabstring);
        Typeface tf = plainFont();
        Typeface tfb = boldFont();
        List<String> targetNames = sortedFilteredControlIDs("reading_iconbox.*");
        populateTargets(tabstring,tab,targetNames,0);
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
        setUpGroup();
    }

    public void populateStories()
    {
        showControls("toggle.*");
        String tabstring = "stories";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        targetNames.addAll(sortedFilteredControlIDs(tabstring+"_smicon.*"));
        populateTargets(tabstring,tab,targetNames,12);

        List<OBControl> nums = sortedFilteredControls(tabstring+"_num.*");
        int i = 1;
        for (OBControl c : nums)
        {
            OBLabel label = new OBLabel(String.format("Reading %d",i),tf,subsubheadtextSize);
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
        setUpGroup();
    }
    public void populateNumeracy()
    {
        showControls("toggle.*");
        String tabstring = "numeracy";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        targetNames.addAll(sortedFilteredControlIDs(tabstring+"_smicon.*"));
        populateTargets(tabstring,tab,targetNames,31);
        populateSubSubHeads(tabstring);
        setUpGroup();
    }

    public void populateWriting()
    {
        showControls("toggle.*");
        String tabstring = "writing";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        populateTargets(tabstring,tab,targetNames,43);
        populateSubSubHeads(tabstring);
        setUpGroup();
    }

    public void populateDesign()
    {
        showControls("toggle.*");
        String tabstring = "design";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        Typeface tf = plainFont();
        Typeface tfb = boldFont();

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        populateTargets(tabstring,tab,targetNames,47);
        populateSubSubHeads(tabstring);
        setUpGroup();
    }

    public void populateTech()
    {
        hideControls("toggle.*");
        String tabstring = "tech";
        OBXMLNode tab = tabXmlDict.get(tabstring);

        List<String> targetNames = sortedFilteredControlIDs(tabstring+"_iconbox.*");
        populateTargets(tabstring,tab,targetNames,53);
        populateSubSubHeads(tabstring);
        populateMiscTexts(tabstring);
        setUpGroup();
    }

    void selectPreview(int i)
    {
        OBXMLNode tab = tabXmlDict.get("video");
        OBXMLNode vnode = tab.childrenOfType("video").get(i);
        videoPreviewIdx = i;
        Typeface tf = plainFont();
        Typeface tfb = boldFont();
        OBControl selector = objectDict.get("numeracy_selector");
        OBControl pim = videoPreviewImages.get(videoPreviewIdx);
        RectF f = new RectF();
        f.set(pim.frame());
        float amt = applyGraphicScale(-4);
        f.inset(amt,amt);
        selector.setFrame(f);

        OBControl textbox = objectDict.get("video_textbox");
        if (textbox != null)
        {
            List<OBXMLNode> textnodes = vnode.childrenOfType("head");
            float top = textbox.top();
            if (textnodes.size() > 0)
            {
                OBXMLNode headnode = textnodes.get(0);
                String key = "video_head";
                deleteControls(key);
                OBLabel label = new OBLabel(headnode.contents,tfb,videoHeadTextSize);
                label.setMaxWidth(textbox.width());
                label.setJustification(OBTextLayer.JUST_LEFT);
                label.setLineSpaceMultiplier(1.2f);
                label.sizeToBoundingBox();
                label.setTop(top);
                label.setLeft(textbox.left());
                label.setZPosition(videoPreviewGroup.zPosition());
                label.setColour(Color.BLACK);
                top = label.bottom() + applyGraphicScale(8);
                //label.setBorderColor(Color.BLACK);
                //label.setBorderWidth(2f);
                objectDict.put(key,label);
                attachControl(label);

            }

            List<OBXMLNode>nodes = vnode.childrenOfType("body");
            if (nodes.size() > 0)
            {
                String key = "video_body";
                deleteControls(key);
                OBXMLNode bodynode = nodes.get(0);
                OBLabel label2 = new OBLabel(bodynode.contents,tf,videoBodyTextSize);
                label2.setMaxWidth(textbox.width());
                label2.setLineSpaceMultiplier(1.4f);
                label2.sizeToBoundingBox();
                label2.setTop(top);
                label2.setLeft(textbox.left());
                label2.setZPosition(videoPreviewGroup.zPosition());
                label2.setColour(Color.BLACK);
                label2.setJustification(OBTextLayer.JUST_LEFT);

                objectDict.put(key,label2);
                attachControl(label2);
            }
        }
    }


    void populateVideoPreviews(String tabstring,OBXMLNode tab)
    {
         List<OBXMLNode> targs = tab.childrenOfType("video");
        if (targs.size() > 0)
        {
            OBControl vs = objectDict.get("video_selector");
            int col = vs.fillColor();
            deleteControls("video_selector");
            OBControl p1 = objectDict.get("video_preview1");
            OBControl p2 = objectDict.get("video_preview2");
            OBControl r1 = objectDict.get("video_HeadingR");
            OBControl r2 = objectDict.get("video_SubHeadingR");
            float toplabelleft = r1.left() - p1.left();
            float toplabeltop = r1.top() - p1.top();
            float bottomlabelright = p1.right() - r2.right();
            float bottomlabelbottom = p1.bottom() - r2.bottom();
            float videoPreviewX = p1.position().x;
            float videoPreviewTopY = p1.position().y;
            float videoPreviewYOffset = p2.position().y - videoPreviewTopY;
            float videoPreviewHeight = p1.height();
            int idx = 0;
            float zpos = objectDict.get(tabstring + "_background").zPosition() + 10;
            videoPreviewImages = new ArrayList<>();
            List<OBControl> lstgp = new ArrayList<>();
            for (OBXMLNode v : targs)
            {
                OBXMLNode imgnode = v.childrenOfType("preview").get(0);
                String src = imgnode.contents;
                OBImage im = loadImageWithName(src,new PointF(),new RectF(),false);
                if (movieFolder == null)
                {
                    String f = OBImageManager.sharedImageManager().getImgPath(src);
                    f = OBUtils.stringByDeletingLastPathComponent(f);
                    f = OBUtils.stringByDeletingLastPathComponent(f);
                    movieFolder = OBUtils.stringByAppendingPathComponent(f,"movies");
                }
                videoPreviewImages.add(im);
                im.setPosition(videoPreviewX,videoPreviewTopY + idx * videoPreviewYOffset);
                im.setScale(videoPreviewHeight / im.height());
                im.setZPosition(5);

                im.setHighlightColour(Color.argb(80,0,0,0));

                OBLabel label = new OBLabel(String.format("%d",idx+1),boldFont(),videoPreviewTextHeadingSize);
                label.setColour(Color.WHITE);
                label.setLeft(im.left() + toplabelleft);
                label.setTop(im.top() + toplabeltop);
                label.setZPosition(7);
                lstgp.add(label);

                OBXMLNode n = v.childrenOfType("thumbnaillabel").get(0);
                OBLabel label2 = new OBLabel(n.contents,plainFont(),videoPreviewTextHeadingSize);
                label2.setColour(Color.WHITE);
                label2.setLeft(label.right() + toplabelleft);
                label2.setTop(im.top() + toplabeltop);
                label2.setZPosition(7);
                lstgp.add(label2);

                n = v.childrenOfType("language").get(0);
                OBLabel label3 = new OBLabel(n.contents,plainFont(),videoPreviewSubTextSize);
                label3.setColour(Color.WHITE);
                label3.setRight(im.right() - bottomlabelright);
                label3.setBottom(im.bottom() - bottomlabelbottom);
                label3.setZPosition(7);
                lstgp.add(label3);

                n = v.childrenOfType("duration").get(0);
                OBLabel label4 = new OBLabel(n.contents,plainFont(),videoPreviewSubTextSize);
                label4.setColour(Color.WHITE);
                label4.setRight(label3.left() - bottomlabelright);
                label4.setBottom(label3.bottom());
                label4.setZPosition(7);
                lstgp.add(label4);

                idx++;
            }
            lstgp.addAll(videoPreviewImages);
            OBControl selector = new OBControl();
            selector.setBackgroundColor(col);
            selector.setFrame(videoPreviewImages.get(0).frame());
            selector.setZPosition(1);
            objectDict.put("numeracy_selector",selector);
            lstgp.add(selector);

            OBControl mask = objectDict.get("video_mask");
            OBControl mc = mask.copy();
            lstgp.add(mc);
            RectF r = OBGroup.frameUnion(lstgp);
            r.bottom += applyGraphicScale(24);
            videoPreviewGroup = new OBGroup(lstgp,r);
            videoPreviewGroup.removeMember(mc);
            attachControl(videoPreviewGroup);
            videoPreviewGroup.setZPosition(zpos);
            objectDict.put("videoPreviewGroup",videoPreviewGroup);
            detachControl(mask);
            videoPreviewGroup.setScreenMaskControl(mask);
            maximumY = videoPreviewGroup.position().y;
            minimumY = maximumY - (videoPreviewGroup.bottom() - mask.bottom());

            selectPreview(videoPreviewIdx);
        }
    }

    public boolean shouldShowVideoFullScreen(int idx)
    {
        if (!full_screen_enabled)
            return false;
        if (idx > 0)
            return false;
        if (intro_video_state == ivs_act_normal)
            return false;
        if (intro_video_state == ivs_before_play)
            return true;
        if (intro_video_state == ivs_playing_full_screen)
            return true;
        return false;
    }

    public boolean videoIsFullScreen()
    {
        if (videoPlayer != null)
        {
            return videoPlayer.frame().equals(boundsf());
        }
        return false;
    }

    public void goFullScreen()
    {
        lockScreen();
        videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FIT);
        videoPlayer.setFrame(boundsf());
        unlockScreen();
    }

    public void goSmallScreen()
    {
        lockScreen();
        OBControl placeHolder = objectDict.get("video_video");
        videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FILL);
        videoPlayer.setFrame(placeHolder.frame());
        unlockScreen();
    }
    public void setUpVideoPlayerForIndex(int idx,boolean play)
    {
        if (idx == 0 && !play)
            intro_video_state = ivs_before_play;
        else
            intro_video_state = ivs_act_normal;
        String tabstring = "video";
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> targs = tab.childrenOfType("video");
        OBXMLNode movienode = targs.get(idx).childrenOfType("movie").get(0);
        String movieName = OBUtils.stringByAppendingPathComponent(movieFolder,movienode.contents);
        OBControl placeHolder = objectDict.get("video_video");
        lockScreen();
        //blankTextureID(2);
        if (videoPlayer != null)
        {
            detachControl(videoPlayer);
            videoPlayer = null;
        }
        if (videoPlayer == null)
        {
            RectF r = new RectF();
            r.set(placeHolder.frame());
            r.left = (int)r.left;
            r.top = (int)r.top;
            r.right = (float) Math.ceil(r.right);
            r.bottom = (float) Math.ceil(r.bottom);
            placeHolder.setFrame(r);
            videoPlayer = new OBVideoPlayer(r,this,false,false);
            videoPlayer.stopOnCompletion = false;
            videoPlayer.setZPosition(190);
            videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FILL);
            //videoPlayer.setZPosition(placeHolder.zPosition()+1);
            attachControl(videoPlayer);
        }
        else
        {
            if (!attachedControls.contains(videoPlayer))
                attachControl(videoPlayer);
            videoPlayer.stop();
            if (idx == 0 && play)
                goFullScreen();
            else
                goSmallScreen();
            //videoPlayer.setFrame(placeHolder.frame());

        }
       // videoPlayer.setPreviewSize(new Size((int)videoPlayer.width(),(int)videoPlayer.height()));
        unlockScreen();
        videoPlayer.playAfterPrepare = play;
        videoPlayer.startPlayingAtTime(OBUtils.getAssetFileDescriptorForPath(movieName),0);
    }

    public void populateVideo()
    {
        hideControls("toggle");
        String tabstring = "video";
        OBXMLNode tab = tabXmlDict.get(tabstring);
        populateVideoPreviews(tabstring,tab);
        setUpVideoPlayerForIndex(videoPreviewIdx,false);
        scrollPreviewToVisible(videoPreviewIdx,false);
    }

    public void setUpGroup()
    {
        List<OBControl> kids = filterControls(currentTab+".*");
        OBControl back = objectDict.get(currentTab+"_background");
        kids.remove(back);
        back.setZPosition(-0.1f);
        scrollGroup = new OBGroup(kids);
        //scrollGroup.setShouldTexturise(false);
        if (scrollMask == null)
        {
            scrollMask = objectDict.get("maskscroll");
            scrollMask.show();
            detachControl(scrollMask);
        }
        attachControl(scrollGroup);
        if (scrollable)
            scrollGroup.setScreenMaskControl(scrollMask);
    }

    public void switchTo(String s,boolean force)
    {
        if (s == currentTab && !force)
            return;
        scrollable = false;
        lockScreen();
        if (currentTab != null)
        {
            if (scrollGroup != null)
                detachControl(scrollGroup);
            if (videoPlayer != null)
            {
                videoPlayer.stop();
                detachControl(videoPlayer);
            }
            deleteControls(currentTab + ".*");
        }
        currentTab = s;
        loadEvent(s);
        if (tabXmlDict == null)
            loadTabContents(currentLang());
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
        else if (s.equals("video"))
            populateVideo();
        if (scrollable)
        {
            maximumY = scrollGroup.position().y;
            minimumY = maximumY - (scrollGroup.bottom() - boundsf().bottom);
        }
        OBControl back = objectDict.get(currentTab+"_background");
        objectDict.get("toggleref").setFillColor(back.fillColor());
        unlockScreen();
    }

    public OBControl findTab(PointF pt)
    {
        for (OBControl tab : tabs)
            if (tab.frame().contains(pt.x,pt.y))
                return tab;
        return null;
    }

    public OBControl findIcon(PointF pt)
    {
        for (OBControl ic : filterControls(".*icon.*"))
            if (!ic.hidden())
            {
                RectF r = convertRectFromControl(ic.bounds(),ic);
                if (r.contains(pt.x, pt.y))
                    return ic;
            }
        return null;
    }

    public OBControl findBackground(PointF pt)
    {
        OBControl background = objectDict.get(currentTab+"_background");
        if (background != null)
        {
            RectF r = convertRectFromControl(background.bounds(),background);
            if (r.contains(pt.x, pt.y))
                return background;
        }
        return null;
    }

    void scrollPreviewToVisible(int idx,boolean animate)
    {
        OBControl preview = videoPreviewImages.get(idx);
        OBControl mask = objectDict.get("video_mask");
        RectF maskFrame = convertRectToControl(mask.frame(),videoPreviewGroup);
        float diff = 0;
        if (preview.top() < maskFrame.top)
        {
            float requiredY = 2 * preview.height() + maskFrame.top;
            diff = requiredY - preview.position().y;
        }
        else if (preview.bottom() > maskFrame.bottom)
        {
            float requiredY = maskFrame.bottom - 2 * preview.height();
            diff = requiredY - preview.position().y;
        }
        if (diff == 0)
            return;
        float newY = videoPreviewGroup.position().y + diff;
        if (newY > maximumY)
            newY = maximumY;
        else if (newY < minimumY)
            newY = minimumY;
        if (newY != videoPreviewGroup.position().y)
        {
            if (animate)
            {
                PointF pt = new PointF(videoPreviewGroup.position().x,newY);
                OBAnim anim = OBAnim.moveAnim(pt,videoPreviewGroup);
                OBAnimationGroup grp = new OBAnimationGroup();
                registerAnimationGroup(grp,"videoscrollanim");
                grp.applyAnimations(Collections.singletonList(anim),0.4,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            }
            else
                videoPreviewGroup.setPosition(videoPreviewGroup.position().x,newY);
        }

    }
    public void touchUpAtPoint(PointF pto,View v)
    {
        if (status() == 0)
            return;
        if (currentTab.equals("video"))
        {
            if (videoScrollState > 0)
            {
                boolean mustSelect = videoScrollState != VIDEO_SCROLL_MOVED;
                videoScrollState = VIDEO_SCROLL_NONE;
                if (mustSelect)
                {
                    for (int i = 0;i < videoPreviewImages.size();i++)
                    {
                        OBControl im = videoPreviewImages.get(i);
                        RectF f = convertRectFromControl(im.bounds(),im);
                        if (f.contains(pto.x,pto.y))
                        {
                            if (videoPlayer != null)
                                videoPlayer.stop();
                            selectPreview(i);
                            scrollPreviewToVisible(i,true);
                            setStatus(STATUS_IDLE);
                            setUpVideoPlayerForIndex(i,true);
                            return;
                        }
                    }
                }
                else
                {
                    float dist = lastPoint.y - lastLastPoint.y;
                    float time = (lastMoveEvent - lastlastMoveEvent)/ 1000.0f;
                    final float speed = dist / time;
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            slowDown(speed,videoPreviewGroup);
                        }
                    });
                }
            }
            setStatus(STATUS_IDLE);
            return;
        }
        if(status() == STATUS_DRAGGING)
        {
            if (scrollable)
            {
                float dist = lastPoint.y - lastLastPoint.y;
                float time = (lastMoveEvent - lastlastMoveEvent) / 1000.0f;
                final float speed = dist / time;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        slowDown(speed, scrollGroup);
                    }
                });
            }
            if (highlightedIcon != null)
            {
                RectF r = convertRectFromControl(highlightedIcon.bounds(),highlightedIcon);
                if (r.contains(pto.x,pto.y))
                {
                    goToTarget(highlightedIcon);
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            lockScreen();
                            highlightedIcon.lowlight();
                            unlockScreen();
                            setHighlightedIcon(null);
                        }
                    });
                }
                else
                {
                    lockScreen();
                    highlightedIcon.lowlight();
                    unlockScreen();
                    setHighlightedIcon(null);

                }

            }
            setStatus(STATUS_IDLE);
        }
    }

    void slowDown(float ySpeed,OBControl group)
    {
        slowingDown = true;
        try
        {
            while (slowingDown)
            {
                if (Math.abs(ySpeed) < 1)
                {
                    slowingDown = false;
                    return;
                }
                ySpeed *= 0.99f;
                float dist = ySpeed * 0.02f;
                float y = group.position().y;
                y += dist;
                boolean fin = false;
                if (y > maximumY)
                {
                    y = maximumY;
                    fin = true;
                }
                else if (y < minimumY)
                {
                    y = minimumY;
                    fin = true;
                }
                group.setPosition(group.position().x,y);
                if (fin)
                    slowingDown = false;
                waitForSecs(0.02);
            }
        }
        catch (Exception e)
        {
            slowingDown = false;
        }
    }
    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == 0)
            return;
        if (currentTab.equals("video"))
        {
            if (videoScrollState == VIDEO_SCROLL_TOUCH_DOWNED)
                if (videoScrollState != VIDEO_SCROLL_MOVED && OB_Maths.PointDistance(videoTouchDownPoint,pt)>applyGraphicScale(8))
                    videoScrollState = VIDEO_SCROLL_MOVED;
            if (videoScrollState > 0)
            {
                float dy = pt.y - lastPoint.y;
                float newY = videoPreviewGroup.position().y + dy;
                if (newY <= maximumY && newY >= minimumY)
                {
                    videoPreviewGroup.setPosition(videoPreviewGroup.position().x,newY);
                }
                lastLastPoint.y = lastPoint.y;
                lastlastMoveEvent = lastMoveEvent;
                lastPoint.y = pt.y;
                lastMoveEvent = System.currentTimeMillis();
            }
        }
        else if(status() == STATUS_DRAGGING)
        {
            if (scrollable)
            {
                float dy = pt.y - lastPoint.y;
                float newY = scrollGroup.position().y + dy;
                if (newY <= maximumY && newY >= minimumY)
                {
                    scrollGroup.setPosition(scrollGroup.position().x, newY);
                }
                lastLastPoint.y = lastPoint.y;
                lastlastMoveEvent = lastMoveEvent;
                lastPoint.y = pt.y;
                lastMoveEvent = System.currentTimeMillis();
                if (highlightedIcon != null)
                {
                    if (OB_Maths.PointDistance(firstPoint,pt) > 6)
                    {
                        lockScreen();
                        highlightedIcon.lowlight();
                        unlockScreen();
                        setHighlightedIcon(null);
                    }
                }
            }
            if (highlightedIcon != null)
            {
                RectF r = convertRectFromControl(highlightedIcon.bounds(),highlightedIcon);
                if (r.contains(pt.x,pt.y))
                    //highlightedIcon.highlight();
                    highlightIcon(highlightedIcon);
                else
                    highlightedIcon.lowlight();
                return;
            }

        }
    }

    void handleVideoPress(PointF pt)
    {
        if (intro_video_state == ivs_before_play)
        {
            goFullScreen();
            videoPlayer.start();
            intro_video_state = ivs_playing_full_screen;
            return;
        }
        if (videoIsFullScreen())
        {
            goSmallScreen();
            intro_video_state = ivs_act_normal;
            return;
        }
        if (videoPlayer.isPlaying())
            videoPlayer.pause();
        else
            videoPlayer.start();
    }
    void processVideoTouch(PointF pt)
    {
        videoScrollState = VIDEO_SCROLL_NONE;
        RectF f = videoPreviewGroup.frame();
        if (!videoIsFullScreen() && f.contains(pt.x,pt.y))
        {
            videoScrollState = VIDEO_SCROLL_TOUCH_DOWNED;
            videoTouchDownPoint.set(pt);
        }
        else
        {
            if (videoPlayer != null && videoPlayer.frame().contains(pt.x,pt.y))
            {
                handleVideoPress(pt);
            }
        }
    }

    void goToTarget(OBControl c)
    {
        final String target = (String) c.propertyValue("target");
        if (target != null)
        {
            final String parm = (String) c.propertyValue("params");
            setStatus(STATUS_BUSY);
            //c.highlight();
            highlightIcon(c);
            String languageName = (String) c.propertyValue("lang");
            String configName = (String) c.propertyValue("config");
            if (configName == null)
            {
                String appDir = (String) Config().get("app_code");
                String[] comps = appDir.split("/");
                configName = comps[0];
            }
            else
                MainActivity.mainActivity.updateConfigPaths(configName, false,languageName);
            if (!MainActivity.mainViewController.pushViewControllerWithNameConfig(target, configName, true, true, parm))
                setStatus(STATUS_IDLE);

        }
    }

    void highlightIcon(OBControl c)
    {
        c.setHighlightColour(Color.argb(100,0,0,0));
    }
    public void touchDownAtPoint(PointF pt, View v)
    {
        lastPoint.set(pt);
        lastLastPoint.set(pt);
        firstPoint.set(pt);
        if (videoIsFullScreen())
        {
            handleVideoPress(pt);
            return;
        }
        slowingDown = false;
        if (status() != STATUS_IDLE)
            return;
        OBControl c = findTab(pt);
        if (c != null)
        {
            int idx = tabs.indexOf(c);
            setStatus(STATUS_BUSY);
            switchTo(titles[idx],false);
            setStatus(STATUS_IDLE);
            return;
        }
        if (currentTab.equals("video"))
        {
            lastPoint.set(pt);
            processVideoTouch(pt);
            return;
        }
        OBControl rct = objectDict.get("toggleref");
        if (rct.frame().contains(pt.x,pt.y))
        {
            int chosen = 0;
            if (objectDict.get("toggleright").frame().contains(pt.x,pt.y))
                chosen = 1;
            if (chosen != chosenLanguage)
                chooseToggle(chosen);
            return;
        }
        c = findIcon(pt);
        if (c != null)
        {
            //c.highlight();
            highlightIcon(c);
            setHighlightedIcon(c);
            setStatus(STATUS_DRAGGING);
            return;
        }
        if (scrollable)
        {
            c = findBackground(pt);
            if (c != null)
            {
                setStatus(STATUS_DRAGGING);
                return;
            }
        }
    }

    void checkMessageNow()
    {
        long lastEventTime;
        if (lastMessageShowTime > MainViewController().lastTouchActivity)
            lastEventTime = lastMessageShowTime;
        else
            lastEventTime = MainViewController().lastTouchActivity;
        long untouchedInterval = System.currentTimeMillis() - lastEventTime;
        float utsecs = untouchedInterval / 1000f;
        if (utsecs > intervalSecs)
            showMessage();
        else
            scheduleMessageHandler();
    }

    public void onResume()
    {
        videoPlayer.onResume();
        if (currentTab.equals("video") && videoPreviewIdx >= 0)
        {
            setUpVideoPlayerForIndex(videoPreviewIdx,false);
        }
        checkMessageNow();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            videoPlayer.onPause();

        } catch(Exception e)
        {

        }

    }

    void setHighlightedIcon(OBControl c)
    {
        highlightedIcon = c;
    }
}


