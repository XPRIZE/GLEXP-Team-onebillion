package org.onebillion.onecourse.mainui.oc_prepr4trace;

import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBTimer;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class OC_PrepR4Trace extends OC_SectionController
{
    public static int TRACING = 0,
            TIME_DONE = 1;
    String setext;
    Map<String,String> sentenceDict;
    List<String> words;
    List timings;
    float letterrecth;
    OBImage blobImage;
    List<OBGroup> groupList,hollowList;
    float spaceWidth;
    OBControl back;
    Bitmap bitmapContext,bitmapContextR;
    boolean maskNeedsUpdate;
    OBTimer timer;
    OBGroup hollowGroupGroup;
    OBConditionLock traceLock;
    int fillablePixelCount,fillablePixelCountPeriod;
    OBControl redLayer;
    OBControl arrow;
    String sentenceid;

    public String tracingFileName()
    {
        return "tracingcapitalletters";
    }

    Map  LoadSentencesXML(String xmlPath)
    {
        Map dict = new HashMap();
        try
        {
            if(xmlPath != null)
            {
                OBXMLManager xmlman =  new OBXMLManager();
                List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                OBXMLNode root = xl.get(0);

                for(OBXMLNode pNode : root.childrenOfType("sentence"))
                {
                    String sid = pNode.attributeStringValue("Object");
                    dict.put(sid,pNode.contents.trim());
                }
            }
        }
        catch(Exception e)
        {
        }
        return dict;
    }

    public void prepare()
    {
        super.prepare();
        traceLock = new OBConditionLock(0);
        sentenceDict = LoadSentencesXML(getLocalPath("prepr4_sentences.xml"));
        sentenceid =  OBUtils.coalesce(parameters.get("sentenceid") , "1");
        setext = sentenceDict.get(sentenceid);
        loadFingers();
        loadEvent("mastera");
        events = Arrays.asList("tracing");
        Map ed = loadXML(getConfigPath(String.format("%s.xml",tracingFileName())));
        eventsDict.putAll(ed);
        loadEvent("masterl");
        letterrecth = objectDict.get("letterrect").height();
        deleteControls("letterrect");
        OBControl blob = objectDict.get("blob");
        blobImage = blob.renderedImageControl();
        deleteControls("blob");
        spaceWidth = spaceWidth();
        doVisual(currentEvent());
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_TRACE);
    }

    public OBGroup hollowGroup(OBGroup gp)
    {
        OBGroup newgroup = (OBGroup) gp.copy();
        newgroup.setZPosition(gp.zPosition() + 1);
        float strokediff = applyGraphicScale(3.0f);
        for(OBControl c : newgroup.members)
        {
            OBPath p = (OBPath)c;
            p.setStrokeColor(Color.WHITE);
            p.setLineWidth(p.lineWidth() - strokediff);
        }
        return newgroup;
    }

    public float spaceWidth()
    {
        objectDict.put("letterrect",objectDict.get("letterrect0"));
        loadEvent("_n");
        OBControl xbox = objectDict.get("xbox");
        float f = xbox.width();
        deleteControls("(Path|xbox) .*");
        return f;
    }

    public OBGroup letterGroup(String l,String rectName)
    {
        OBControl lrect = objectDict.get(rectName);
        float hfrac = lrect.height() / letterrecth;
        objectDict.put("letterrect",lrect);
        List arr = loadEvent(l);
        List memberlist = new ArrayList<>();
        RectF f = new RectF();
        for(OBControl c : filterControls("Path.*"))
        {
            if(arr.contains(c))
            {
                OBPath p =(OBPath) c;
                float lw = p.lineWidth() * hfrac;
                p.setLineWidth(lw);
                memberlist.add(p);
                RectF pf = new RectF(p.frame);
                pf.inset(-lw,-lw);
                f.union(pf);
            }
        }
        OBGroup g = new OBGroup(memberlist,f);
        return g;
    }

    public void positionGroup(OBGroup g,OBControl xbox,float left,float bottom)
    {
        float ydiff = xbox.bottom() - bottom;
        float xdiff = xbox.left() - left;
        if(xdiff != 0 || ydiff != 0)
        {
            PointF pos = g.position();
            pos.y -= ydiff;
            pos.x -= xdiff;
            g.setPosition(pos);
            xbox.setLeft(left);
            xbox.setBottom(bottom);
        }
    }

    public void layOutSentence(String sentence)
    {
        List<OBControl> xboxes = new ArrayList<>();
        float bottom = objectDict.get("letterrect0").bottom();
        groupList = new ArrayList<>();
        words = Arrays.asList(sentence.split(" "));
        for(String wordText : words)
        {
            for(int i = 0;i < wordText.length();i++)
            {
                String character = wordText.substring(i, i + 1);
                String l = "_"+character;
                OBGroup g = letterGroup(l,"letterrect0");
                groupList.add(g);
                attachControl(g);
                OBControl xbox = objectDict.get("xbox");
                if(xbox != null)
                {
                    xboxes.add(xbox);
                    xbox.hide();
                }
            }
        }
        float left = objectDict.get("letterrect0").left();
        float spwidth = spaceWidth();
        float rlimit = objectDict.get("textarea").right();

        int chidx = 0;
        for(String wordText : words)
        {
            boolean xplaced = false;
            while(!xplaced)
            {
                int chi = chidx;
                for(int i = 0;i < wordText.length();i++)
                {
                    chi = chidx + i;
                    OBControl xb = xboxes.get(chi);
                    OBGroup g = groupList.get(chi);
                    positionGroup(g,xb,left,bottom);
                    if(g.right() > rlimit)
                    {
                        xplaced = false;
                        break;
                    }
                    left += xb.width();
                    xplaced = true;
                }
                if(xplaced)
                {
                    left += spwidth;
                    chidx = chi + 1;
                }
                else
                {
                    left = objectDict.get("letterrect0").left();
                    bottom += objectDict.get("letterrect0").height() * 1.5;
                }
            }
        }
        hollowList = new ArrayList<>();
        for(OBGroup g : groupList)
        {
            OBGroup hg = hollowGroup(g);
            hollowList.add(hg);
        }
        hollowGroupGroup = new OBGroup((List)hollowList);
        hollowGroupGroup.setZPosition(150);
        // *createBackFromGroup(hollowGroupGroup);
    }
/*
    public void showRedPartsInclude(RectF includeRect,RectF excludeRect)
    {
        int x0,y0,xmax,ymax;
        x0 = (int)includeRect.left;
        y0 = (int)includeRect.top;
        xmax = (int)(x0 + includeRect.width());
        ymax = (int)(y0 + includeRect.height());
        unsigned char *data = CGBitmapContextGetData(bitmapContext);
        unsigned char *data2 = CGBitmapContextGetData(bitmapContextR);
        int bytesPerRow = CGBitmapContextGetBytesPerRow(bitmapContext);
        for(int i = y0;i < ymax;i++)
        {
            unsigned char *row = data + i * bytesPerRow;
            unsigned char *row2 = data2 + i * bytesPerRow;
            unsigned char *ptr = row + x0 * 4;
            unsigned char *ptr2 = row2 + x0 * 4;
            for(int j = x0;j < xmax;j++)
            {
                if(!excludeRect.contains(new PointF(j, i.x, new PointF(j, i.y))))
                {
                    if(*ptr > 25)
                    {
                    *ptr2 = 255;
                    *(ptr2 + 3) = 255;
                    }
                else
                    {
                    *(ptr2) = 0;
                    *(ptr2 + 1) = 0;
                    *(ptr2 + 2) = 0;
                    *(ptr2 + 3) = 0;
                    }
                }
                ptr +=4;
                ptr2 +=4;
            }
        }
        CGImageRef iref = CGBitmapContextCreateImage(bitmapContextR);
        redLayer.layer.setContents((__bridge Object) iref);
        CGImageRelease(iref);

    }

    public void showRedParts()
    {
        int w = bitmapContext.getWidth();
        int h = bitmapContext.getHeight();
        for(int i = 0;i < h;i++)
        {
            for(int j = 0;j < w;j++)
            {
                int px = bitmapContext.getPixel(i,j);
                if(Color.red(px) > 25)
                {
                *ptr2 = 255;
                *(ptr2 + 3) = 255;
                }
            else
                {
                *(ptr2 + 3) = 0;
                }
                ptr +=4;
                ptr2 +=4;
            }
        }
        CGImageRef iref = CGBitmapContextCreateImage(bitmapContextR);
        redLayer.layer.setContents((__bridge Object) iref);
    }

    public void createRedLayerWithFrame(RectF f)
    {
        redLayer = OBControl.alloc().init();
        redLayer.setFrame(f);
        bitmapContextR = CreateBitmapContextWithColour(f.width(), f.height(),Color.clear());
        CGImageRef iref = CGBitmapContextCreateImage(bitmapContextR);
        redLayer.layer.setContents((__bridge Object) iref);
        CGImageRelease(iref);
        redLayer.setZPosition(back.zPosition() + 1);
        attachControl(redLayer);
    }

    public void createBackFromGroup(OBGroup g)
    {
        RectF f = g.frame();
        back = new OBControl();
        back.setFrame(f);
        g.setFrame(g.bounds());
        back.setZPosition(100);
        RectF b = g.bounds();
        bitmapContext = CreateBitmapContextWithColour(f.width(), f.height(),Color.black());
        CGContextDrawImage(bitmapContext, b, g.renderedImage().CGImage());
        CGContextSetBlendMode(bitmapContext, kCGBlendModeNormal);
        fillablePixelCount = countFilledPixels(bitmapContext);
        fillablePixelCountPeriod = countFilledPeriodPixels(bitmapContext);
        CGImageRef iref = CGBitmapContextCreateImage(bitmapContext);
        back.layer.setContents((__bridge Object) iref);
        CGImageRelease(iref);
        back.setMaskControl(hollowGroupGroup);
        attachControl(back);
    }
*/
    public void setUpScene()
    {
        layOutSentence(setext);
        arrow = objectDict.get("arrow");
        arrow.hide();
    }

    public List loadTimings(String xmlPath)
    {
        try
        {
            if(xmlPath != null && OBUtils.fileExistsAtPath(xmlPath))
            {
                List<OBXMLNode> lx = new OBXMLManager().parseFile(OBUtils.getInputStreamForPath(xmlPath));
                OBXMLNode xmlNode = lx.get(0);
                List<OBXMLNode> arr = xmlNode.childrenOfType("timings");
                if(arr.size() > 0)
                {
                    List mtimings = new ArrayList<>();
                    OBXMLNode elem = arr.get(0);
                    for(OBXMLNode tnode : elem.childrenOfType("timing"))
                    {
                        float startTime = (float)tnode.attributeFloatValue("start");
                        float endTime = (float)tnode.attributeFloatValue("end");
                        mtimings.add(Arrays.asList((startTime) ,(endTime)));
                    }
                    return mtimings;
                }
            }
        }
        catch(Exception e)
        {

        }
        return null;
    }

    public void setScenetracing()
    {
        setUpScene();
        for(OBControl c : groupList)
            c.hide();
        back.hide();
    }

}
