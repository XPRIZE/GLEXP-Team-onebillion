package org.onebillion.onecourse.mainui.oc_prepr4trace;

import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
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
    List<List<Float>> timings;
    float letterrecth;
    OBImage blobImage;
    List<OBGroup> groupList,hollowList;
    float spaceWidth;
    OBImage back;
    Bitmap bitmapContext,bitmapContextR;
    boolean maskNeedsUpdate;
    OBTimer timer;
    OBGroup hollowGroupGroup;
    OBConditionLock traceLock;
    int fillablePixelCount,fillablePixelCountPeriod;
    OBImage redLayer;
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
                    String sid = pNode.attributeStringValue("id");
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
        deleteControls("(Path|xbox).*");
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
            PointF pos = new PointF();
            pos.set(g.position());
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
        createBackFromGroup(hollowGroupGroup);
    }

    public void showRedPartsInclude(RectF includeRect,RectF excludeRect)
    {
        int x0,y0,xmax,ymax;
        x0 = (int)includeRect.left;
        y0 = (int)includeRect.top;
        xmax = (int)(x0 + includeRect.width());
        ymax = (int)(y0 + includeRect.height());
        int w = xmax - x0;
        int pixels[] = new int[w];
        for(int i = y0;i < ymax;i++)
        {
            bitmapContext.getPixels(pixels, 0, bitmapContext.getWidth(), x0, i, w, 1);

            for(int j = x0;j < xmax;j++)
            {
                if(!excludeRect.contains(j,i))
                {
                    //int px = bitmapContext.getPixel(j,i);
                    int px = pixels[j - x0];
                    if (Color.red(px) > 25)
                        bitmapContextR.setPixel(j,i, Color.RED);
                    else
                        bitmapContextR.setPixel(j,i, 0);
                }
            }
        }
        redLayer.setContents(bitmapContextR);
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
                    bitmapContextR.setPixel(i,j,Color.RED);
                }
            else
                {
                    bitmapContextR.setPixel(i,j,0);
                }
            }
        }
        redLayer.setContents(bitmapContextR);
    }

    public void createRedLayerWithFrame(RectF f)
    {
        bitmapContextR = Bitmap.createBitmap((int)f.width(), (int)f.height(), Bitmap.Config.ARGB_8888);
        redLayer = new OBImage(bitmapContextR);
        redLayer.setFrame(f);
        redLayer.setZPosition(back.zPosition() + 1);
        attachControl(redLayer);
    }

    public void createBackFromGroup(OBGroup g)
    {
        RectF f = new RectF(g.frame());
        g.setFrame(g.bounds());
        RectF b = g.bounds();
        bitmapContext = Bitmap.createBitmap((int)f.width(), (int)f.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapContext);
        canvas.drawARGB(255,0,0,0);
        g.drawLayer(canvas,0);
        fillablePixelCount = countFilledPixels(bitmapContext);
        fillablePixelCountPeriod = countFilledPeriodPixels(bitmapContext);
        back = new OBImage(bitmapContext);
        back.setFrame(f);
        back.setZPosition(100);
        back.setMaskControl(hollowGroupGroup);
        attachControl(back);
    }

    public void setUpScene()
    {
        layOutSentence(setext);
        arrow = objectDict.get("arrow");
        arrow.hide();
    }

    public List<List<Float>> loadTimings(String xmlPath)
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
                    List<List<Float>> mtimings = new ArrayList<>();
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

    public String filePrefix()
    {
        return "prepr4s1_";
    }

    public void fadeInHollow() throws Exception
    {
        lockScreen();
        back.show();
        for(OBGroup g : hollowList)
            for(OBPath p : (List<OBPath>)(List)g.members)
            {
                p.setProperty("origlw",(p.lineWidth()));
                p.setLineWidth(0);
            }
        unlockScreen();
        OBAnim blockAnim = new OBAnimBlock() {
            @Override
            public void runAnimBlock(float frac) {
                frac = OB_Maths.clamp01(frac);
                for(OBGroup g : hollowList)
                    for(OBPath p : (List<OBPath>)(List)g.members)
                    {
                        float lw = (Float)p.propertyValue("origlw");
                        p.setLineWidth(lw * frac);
                    }
                back.setNeedsRetexture();
                back.invalidate();
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(blockAnim),0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void bringOnThings(String audioFileName) throws Exception
    {
        timings = loadTimings(getLocalPath(String.format("%s%s.etpa",filePrefix() ,sentenceid)));
        playAudio(audioFileName);
        long startTime = SystemClock.uptimeMillis();
        int nextch = 0;
        for(int i = 0;i < words.size();i++)
        {
            double currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
            List<Float> times = timings.get(i);
            float st = times.get(0).floatValue();
            double waitTime = st - currTime;
            if(waitTime > 0.0)
                waitForSecs(waitTime);
            int wlen = words.get(i).length();
            List<OBGroup> wordGroups = groupList.subList(nextch, nextch + wlen);
            lockScreen();
            for(OBControl g : wordGroups)
                g.show();
            unlockScreen();
            nextch += wlen;
        }
        waitAudio();
        waitForSecs(1f);
        fadeInHollow();
    }

    public void doMaintracing() throws Exception
    {
        waitForSecs(0.2f);
        playAudioQueuedScene("PROMPT",true);
        waitForSecs(0.4f);
        String fileName = String.format("%s%s",filePrefix() ,sentenceid);
        bringOnThings(fileName);
        setReplayAudio(Arrays.asList((Object)fileName));
        OBUtils.runOnOtherThreadDelayed(5,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                arrow.show();
            }
        });
    }

    public void updateBack()
    {
        back.setContents(bitmapContext);
        back.setNeedsRetexture();
        back.invalidate();
    }

    public void doFrame(OBTimer tmr)
    {
        if(maskNeedsUpdate)
        {
            updateBack();
            maskNeedsUpdate = false;
        }
    }

    public void startTimer()
    {
        timer = new OBTimer(0.02f) {
            @Override
            public int timerEvent(OBTimer timer) {
                doFrame(timer);
                return 1;
            }
        };
        timer.scheduleTimerEvent();
    }

    public void stopTimer()
    {
        if (timer != null)
        {
            timer.invalidate();
            timer = null;
        }
        /*if(bitmapContext != null)
        {
            bitmapContext = null;
        }*/
    }

    public void cleanUp()
    {
        stopTimer();
        super.cleanUp();
    }

    public void stampImage(OBImage img,PointF point)
    {
        RectF f = back.bounds();
        Canvas canvas = new Canvas(bitmapContext);
        canvas.translate( point.x, point.y);
        canvas.scale(img.scaleX(),img.scaleY());
        canvas.translate(-img.width()/2f,-img.height()/2f);
        img.drawLayer(canvas,0);

        maskNeedsUpdate = true;
    }

    public int countFilledPixels(Bitmap bcontext)
    {
        int w = bcontext.getWidth();
        int h = bcontext.getHeight();
        int tot = 0;
        int pixels[] = new int[w];
        for(int i = 0;i < h;i++)
        {
            bcontext.getPixels(pixels, 0, w, 0, i, w, 1);
            for(int j = 0;j < w;j++)
            {
                int px = pixels[j];
                if(Color.red(px) > 25)
                {
                    tot++;
                }
            }
        }
        return tot;
    }

    public int countFilledPeriodPixels(Bitmap bcontext)
    {
        OBControl period = hollowList.get(hollowList.size()-1);
        RectF f = hollowGroupGroup.convertRectFromControl(period.bounds(),period);
        int tot = 0;
        for(int i = (int)f.top;i < f.top + f.height();i++)
        {
            for(int j = (int)f.left;j < f.left + f.width();j++)
            {
                int px = bcontext.getPixel(j,i);
                if(Color.red(px) > 25)
                {
                    tot++;
                }
            }
        }
        return tot;
    }

    public void findUnfilledPixels()
    {
        RectF f = back.bounds();
        float minx = f.width();
        float miny = f.height();
        float maxx = 0;
        float maxy = 0;
        for(int i = (int)f.top;i < f.top + f.height();i++)
        {
            for(int j = (int)f.left;j < f.left + f.width();j++)
            {
                int px = bitmapContext.getPixel(j,i);
                if(Color.red(px) > 25)                {
                    if(j < minx)
                        minx = j;
                    if(j > maxx)
                        maxx = j;
                    if(i < miny)
                        miny = i;
                    if(i > maxy)
                        maxy = i;
                }
            }
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status() == STATUS_TRACING)
        {
            PointF tpt = convertPointToControl(pt,back);
            stampImage(blobImage,tpt);
        }
    }

    public void checkTraceStart(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        startTimer();
        PointF tpt = convertPointToControl(pt,back);
        stampImage(blobImage,tpt);
        setStatus(STATUS_TRACING);
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_TRACING)
        {
            setStatus(STATUS_CHECKING);
            traceLock.lock();
            int cond = traceLock.conditionValue();
            traceLock.unlockWithCondition((cond & ~TRACING));
            setStatus(STATUS_WAITING_FOR_TRACE);
        }
    }

    public void flashRed() throws Exception
    {
        for(int i = 0;i < 5;i++)
        {
            redLayer.show();
            waitForSecs(0.3f);
            redLayer.hide();
            waitForSecs(0.3f);
        }
    }

    static float PIXEL_THRESHOLD = 0.03f;

    public void finishTrace()
    {
        //findUnfilledPixels();
        final int allPeriodUnfilledPixels = countFilledPeriodPixels(bitmapContext);
        final int allUnfilledPixels = countFilledPixels(bitmapContext) - allPeriodUnfilledPixels;
        final int origUnfilledPixels = fillablePixelCount - fillablePixelCountPeriod;
        final int origUnfilledPixelsPeriod = fillablePixelCountPeriod;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(allUnfilledPixels * 1.0 / origUnfilledPixels < PIXEL_THRESHOLD && allPeriodUnfilledPixels * 1.0 / origUnfilledPixelsPeriod < PIXEL_THRESHOLD)
                    {
                        gotItRightBigTick(true);
                        waitForSecs(0.3f);
                        playAudioQueuedScene("CORRECT",true);
                    }
                    else
                    {
                        OBControl period = hollowList.get(hollowList.size()-1);
                        RectF pf = hollowGroupGroup.convertRectFromControl(period.bounds(),period);
                        if(allUnfilledPixels * 1.0 / origUnfilledPixels >= PIXEL_THRESHOLD)
                        {
                            lockScreen();
                            createRedLayerWithFrame(back.frame());
                            showRedPartsInclude(new RectF(0, 0, (bitmapContext.getWidth()) , (bitmapContext.getHeight())),pf);
                            redLayer.hide();
                            unlockScreen();
                            playAudioQueuedScene("INCORRECT",true);
                            flashRed();
                        }
                        if(allPeriodUnfilledPixels * 1.0 / origUnfilledPixelsPeriod >= PIXEL_THRESHOLD)
                        {
                            lockScreen();
                            createRedLayerWithFrame(back.frame());
                            showRedPartsInclude(pf,new RectF());
                            redLayer.hide();
                            unlockScreen();
                            playAudioQueuedScene("INCORRECT2",true);
                            flashRed();
                        }
                    }
                    waitForSecs(0.5f);
                    back.hide();
                    nextScene();

                }
                catch(Exception e)
                {
                    MainActivity.log("here");
                }
            }
        });
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_WAITING_FOR_TRACE)
        {
            if(!arrow.hidden())
            {
                if(finger(-1,3,Arrays.asList(arrow),pt) != null)
                {
                    setStatus(STATUS_CHECKING);
                    finishTrace();
                    return;
                }
            }
            traceLock.lock();
            int cond = traceLock.conditionValue();
            traceLock.unlockWithCondition(cond | TRACING);
            checkTraceStart(pt);
        }
    }

}
