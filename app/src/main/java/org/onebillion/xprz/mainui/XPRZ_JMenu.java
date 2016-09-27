package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Size;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBTextLayer;
import org.onebillion.xprz.controls.OBVideoPlayer;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;
import org.onebillion.xprz.utils.OB_Maths;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
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
    public float tabTextSize,subheadtextSize,subsubheadtextSize,itemHeadTextSize,itemBodyTextSize,videoHeadTextSize,videoBodyTextSize;
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
    OBControl highlightedIcon = null;

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

    public void loadMasterList()
    {
        InputStream pis;
        try
        {
            pis = MainActivity.mainActivity.getAssets().open("x-jmenu/config/junits.xml");
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

    public void prepare()
    {
        super.prepare();
        loadMasterList();
        saveConfig = (String)Config().get(MainActivity.CONFIG_APP_CODE);
        loadEvent("mastera");
        tabTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("tabtextsize")));
        subheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subheadtextsize")));
        subsubheadtextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subsubheadtextsize")));
        itemHeadTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itemheadtextsize")));
        itemBodyTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("itembodytextsize")));
        videoHeadTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("videoheadtextsize")));
        videoBodyTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("videobodytextsize")));
        tabs = sortedFilteredControls("tab.*");
        for (OBControl t : tabs)
            t.setZPosition(t.zPosition()+60);
        OBControl obl = objectDict.get("obl");
        obl.setZPosition(obl.zPosition()+60);
        setUpTabTitles();

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
                src = src + "_big";
                OBControl im = loadImageWithName(src,pt,boundsf(),true);
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
            OBLabel label = new OBLabel(String.format("Level %d",i),tf,subsubheadtextSize);
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
            OBPath vs = (OBPath) objectDict.get("video_selector");
            int col = vs.fillColor();
            deleteControls("video_selector");
            OBControl p1 = objectDict.get("video_preview1");
            OBControl p2 = objectDict.get("video_preview2");
            float videoPreviewX = p1.position().x;
            float videoPreviewTopY = p1.position().y;
            float videoPreviewYOffset = p2.position().y - videoPreviewTopY;
            float videoPreviewHeight = p1.height();
            int idx = 0;
            float zpos = objectDict.get(tabstring + "_background").zPosition() + 10;
            videoPreviewImages = new ArrayList<>();
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
                idx++;
            }
            OBControl selector = new OBControl();
            selector.setBackgroundColor(col);
            selector.setFrame(videoPreviewImages.get(0).frame());
            selector.setZPosition(1);
            objectDict.put("numeracy_selector",selector);
            List<OBControl> lstgp = new ArrayList<>(videoPreviewImages);
            lstgp.add(selector);

            OBControl mask = objectDict.get("video_mask");
            OBControl mc = mask.copy();
            lstgp.add(mc);
            videoPreviewGroup = new OBGroup(lstgp);
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
        videoPlayer.setFrame(boundsf());
    }

    public void goSmallScreen()
    {
        OBControl placeHolder = objectDict.get("video_video");
        videoPlayer.setFrame(placeHolder.frame());
    }
    public void setUpVideoPlayerForIndex(int idx,boolean play)
    {
        if (idx == 0 && !play)
            intro_video_state = ivs_before_play;
        String tabstring = "video";
        OBXMLNode tab = tabXmlDict.get(tabstring);
        List<OBXMLNode> targs = tab.childrenOfType("video");
        OBXMLNode movienode = targs.get(idx).childrenOfType("movie").get(0);
        String movieName = OBUtils.stringByAppendingPathComponent(movieFolder,movienode.contents);
        OBControl placeHolder = objectDict.get("video_video");
        lockScreen();
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
            videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FIT);
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
        videoPlayer.setPreviewSize(new Size((int)videoPlayer.width(),(int)videoPlayer.height()));
        unlockScreen();
        videoPlayer.playAfterPrepare = play;
        videoPlayer.startPlayingAtTime(OBUtils.getAssetFileDescriptorForPath(movieName),0);
    }

    public void populateVideo()
    {
        String tabstring = "video";
        OBXMLNode tab = tabXmlDict.get(tabstring);
        populateVideoPreviews(tabstring,tab);
        setUpVideoPlayerForIndex(videoPreviewIdx,false);
    }

    public void setUpGroup()
    {
        scrollGroup = new OBGroup(filterControls(currentTab+".*"));
        scrollGroup.setShouldTexturise(false);
        attachControl(scrollGroup);
    }

    public void switchTo(String s)
    {
        if (s == currentTab)
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
        else if (s.equals("video"))
            populateVideo();
        if (scrollable)
        {
            maximumY = scrollGroup.position().y;
            minimumY = maximumY - (scrollGroup.bottom() - boundsf().bottom);
        }
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

    public void touchUpAtPoint(PointF pto,View v)
    {
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
                            highlightedIcon.lowlight();
                            highlightedIcon = null;
                        }
                    });
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
                        highlightedIcon.lowlight();
                        highlightedIcon = null;
                    }
                }
            }
            if (highlightedIcon != null)
            {
                RectF r = convertRectFromControl(highlightedIcon.bounds(),highlightedIcon);
                if (r.contains(pt.x,pt.y))
                    highlightedIcon.highlight();
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
            c.highlight();
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
            switchTo(titles[idx]);
            setStatus(STATUS_IDLE);
            return;
        }
        if (currentTab.equals("video"))
        {
            lastPoint.set(pt);
            processVideoTouch(pt);
            return;
        }
        c = findIcon(pt);
        if (c != null)
        {
            c.highlight();
            highlightedIcon = c;
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

    public void onResume()
    {
        videoPlayer.onResume();
        if (currentTab.equals("video") && videoPreviewIdx >= 0)
        {
            setUpVideoPlayerForIndex(videoPreviewIdx,false);
        }
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

}


