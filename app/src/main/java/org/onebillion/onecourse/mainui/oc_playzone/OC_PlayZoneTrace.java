package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBTimer;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OC_PlayZoneTrace extends OC_SectionController
{
    String letter;
    float letterrecth;
    float dashlen0,dashlen1,lineWidth;
    OBImage blobImage;
    OBControl mask;
    Bitmap bitmapContext,bitmapContextR;
    List<OBGroup> groupList;
    boolean maskNeedsUpdate;
    OBTimer timer;
    double startTime;
    OBImage back,redLayer;
    OBControl hollow;
    OBConditionLock traceLock;
    float spacerwidth;
    int fillablePixelCount;
    int score;


    public String layoutName()
    {
        return "mastera";
    }

    public String tracingFileName()
    {
        return "tracingcapitalletters";
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_TRACE);
    }

    public void miscSetUp()
    {
        groupList = new ArrayList<>();
        letter = OBUtils.coalesce(parameters.get("letter") , "c");
        events = new ArrayList<>(Arrays.asList("a","b","remaining"));
        Map ed = loadXML(getConfigPath(String.format("%s.xml",tracingFileName())));
        eventsDict.putAll(ed);
        loadEvent("masterl");
        letterrecth = objectDict.get("letterrect").height();
        deleteControls("letterrect");
        OBControl spacer = objectDict.get("spacer");
        spacerwidth = spacer.width();
        OBControl blob = objectDict.get("blob");
        blobImage = blob.renderedImageControl();
        deleteControls("blob");
        traceLock = new OBConditionLock(0);
    }

    public void prepare()
    {
        super.prepare();
        loadEvent(layoutName());
        loadFingers();
        miscSetUp();
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

    public void endBody()
    {
        final long stt = statusTime();
        reprompt(stt, null, 6, new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        flashRemind(stt);
                    }
                });
    }


    public void cleanUpTrace()
    {
        if(bitmapContext !=null)
        {
            bitmapContext = null;
        }
        if(bitmapContextR != null)
        {
            bitmapContextR = null;
        }
        if(back != null)
            detachControl(back);
        if(redLayer != null)
            detachControl(redLayer);
    }

    public int countFilledPixels(Bitmap bcontext)
    {
        int w = (bcontext).getWidth();
        int h = (bcontext).getHeight();
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

    static int BACK_ZPOS = 50;

    public void setUpTraceLetter(int idx)
    {
        cleanUpTrace();
        traceLock.lock();
        traceLock.unlockWithCondition(0);
        startTime = 0;
        setLetterDashed(idx,false);
        hollow = hollowForGroup(groupList.get(idx));
        RectF f = new RectF(hollow.bounds());
        hollow.setFrame(f);
        RectF b = new RectF(f);

        bitmapContext = Bitmap.createBitmap((int)b.width(),(int) b.height(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapContext);
        canvas.drawARGB(255,0,0,0);
        hollow.drawLayer(canvas,0);

        back = new OBImage(bitmapContext);
        back.setBounds(b);
        back.setPosition(groupList.get(idx).position());
        back.setZPosition(BACK_ZPOS);
        fillablePixelCount = countFilledPixels(bitmapContext);

        bitmapContext = Bitmap.createBitmap((int)b.width(),(int) b.height(),Bitmap.Config.ARGB_8888);
        redLayer = new OBImage(bitmapContextR);
        redLayer.setBounds(b);
        redLayer.setPosition(groupList.get(idx).position());
        redLayer.setZPosition(BACK_ZPOS + 1);

        back.setMaskControl(hollow);
        //hollow.hide();
        attachControl(back);

        attachControl(redLayer);
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
                if(dashlen0 == 0)
                {
                    dashlen0 = 0.01f;
                    dashlen1 = lw * 0.5f * 1.2f;
                    lineWidth = lw;
                }
                memberlist.add(p);
                f.union(p.boundingBox());
            }
        }
        OBGroup g = new OBGroup(memberlist,f);
        return g;
    }


    public void setLetterDashed(int letteridx,boolean dashed)
    {
        List<OBPath>pathList = (List)groupList.get(letteridx).members;
        int col = dashed?Color.argb(255,230,230,230) :Color.BLACK;
        float lw = dashed?lineWidth * 0.5f:lineWidth;
        lockScreen();
        for(OBPath p : pathList)
        {
            if(dashed)
                p.setLineDashPattern(Arrays.asList((dashlen0) ,(dashlen1)));
            else
                p.setLineDashPattern(null);
            p.setStrokeColor(col);
            p.setLineWidth(lw);
        }
        unlockScreen();
    }

    public void setLetterHigh(int letteridx,boolean high)
    {
        List<OBPath>pathList = (List)groupList.get(letteridx).members;
        int col = high?Color.BLUE :Color.BLACK;
        lockScreen();
        for(OBPath p : pathList)
            p.setStrokeColor(col);
        unlockScreen();
    }

    public OBGroup hollowGroup(OBGroup gp)
    {
        OBGroup newgroup = (OBGroup)gp.copy();
        float strokediff = applyGraphicScale(3.0f);
        for(OBPath p : (List<OBPath>)(List)newgroup.members)
        {
            p.setStrokeColor(Color.WHITE);
            p.setLineWidth(p.lineWidth() - strokediff);
        }
        return newgroup;
    }

    public OBGroup hollowForGroup(OBGroup gp)
    {
        OBGroup phollow = (OBGroup) gp.propertyValue("hollow");
        if(phollow == null)
        {
            phollow = hollowGroup(gp);
            gp.setProperty("hollow",phollow);
            phollow.setZPosition(gp.zPosition() +1);
            //attachControl(phollow);
        }
        return phollow;
    }

    public void setLetterHollow(int letteridx,boolean hollow)
    {
        OBGroup gp = groupList.get(letteridx);
        OBGroup phollow = hollowForGroup(gp);
        phollow.setHidden(!hollow);
    }


    public void playLetterSound(String s)
    {
        playAudio(String.format("is_%s",s));
    }

    public void highlightAndPlay(int i) throws Exception
    {
        setLetterHollow(i,true);
        waitForSecs(0.3f);
        playLetterSound(letter);
        waitForSecs(0.2f);
        waitAudio();
        waitForSecs(0.5f);
        setLetterHollow(i,false);
    }

    public void updateBack()
    {
        back.setContents(bitmapContext);
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
        timer.invalidate();
        timer = null;
    }

    public void cleanUp()
    {
        stopTimer();
        super.cleanUp();
    }

    public boolean pixelsLeft(float threshold)
    {
        int w = bitmapContext.getWidth();
        int h = bitmapContext.getHeight();
        int pixelThreshold = (int)(fillablePixelCount * threshold);
        int tot = 0;
        int pixels[] = new int[w];
        for(int i = 0;i < h;i++)
        {
            bitmapContext.getPixels(pixels, 0, w, 0, i, w, 1);
            for(int j = 0;j < w;j++)
            {
                int px = pixels[j];
                if (Color.red(px) > 25)
                    tot++;
                if(tot > pixelThreshold)
                    return false;
            }
        }
        return true;
    }



    public void showRedParts()
    {
        int w = bitmapContext.getWidth();
        int h = bitmapContext.getHeight();
        int pixels[] = new int[w];
        for(int i = 0;i < h;i++)
        {
            bitmapContext.getPixels(pixels, 0, w, 0, i, w, 1);
            for(int j = 0;j < w;j++)
            {
                int px = pixels[j];
                if (Color.red(px) > 25)
                    bitmapContextR.setPixel(j,i, Color.RED);
                else
                    bitmapContextR.setPixel(j,i, 0);
            }
        }
        redLayer.setContents(bitmapContextR);
    }

    public void flashRed() throws Exception
    {
        for(int i = 0;i < 2;i++)
        {
            waitForSecs(0.3f);
            lockScreen();
            redLayer.hide();
            unlockScreen();
            waitForSecs(0.3f);
            lockScreen();
            redLayer.show();
            unlockScreen();
        }
    }

    static int     TRACING = 0,
            TIME_DONE = 1;

    static float PIXEL_THRESHOLD = 0.1f;

    public void finishTrace(Map d)
    {
        try
        {
            traceLock.lockWhenCondition(TIME_DONE);
            traceLock.unlock();
            Integer n = (Integer)d.get("n");
            if(n != null && n.intValue() == currNo)
            {
                d.remove("n");
                if(!pixelsLeft(PIXEL_THRESHOLD))
                {
                    lockScreen();
                    showRedParts();
                    unlockScreen();
                    flashRed();
                }
                finishLetter();
            }
        }
        catch(Exception e)
        {
        }
    }

    public void timesUp(Map d)
    {
        Integer n = (Integer)d.get("n");
        if(n != null && n.intValue() == currNo)
        {
            setStatus(STATUS_CHECKING);
            traceLock.lock();
            int cond = traceLock.conditionValue();
            traceLock.unlockWithCondition(cond | TIME_DONE);
        }
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

    static int TIME_THRESHOLD = 10;

    public void flashLetter(long stt) throws Exception
    {
        for(int i = 0;i < 2;i++)
        {
            waitForSecs(0.3f);
            lockScreen();
            redLayer.show();
            unlockScreen();
            waitForSecs(0.3f);
            lockScreen();
            redLayer.hide();
            unlockScreen();
        }
    }

    public void flashRemind(final long stt)
    {
        if(statusTime() == stt)
        {
            try
            {
                lockScreen();
                showRedParts();
                unlockScreen();
                flashLetter(stt);
                OBUtils.runOnOtherThreadDelayed(10,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        flashRemind(stt);
                    }
                });

            }
            catch(Exception e)
            {
            }
         }
    }

    public void nextLetter() throws Exception
    {
        if(++currNo >= groupList.size())
        {
            nextScene();
            return;
        }
        else
        {
            playSfxAudio("tap",false);
            lockScreen();
            setUpTraceLetter(currNo);
            unlockScreen();
            setStatus(STATUS_WAITING_FOR_TRACE);
            final long stt = statusTime();
            reprompt(stt, null, 5, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    flashRemind(stt);
                }
            });

        }
    }
    public void finishLetter() throws Exception
    {
        lockScreen();
        redLayer.hide();
        hollow.hide();
        unlockScreen();
        waitForSecs(0.4f);
        highlightAndPlay(currNo);
        waitForSecs(0.4f);
        nextLetter();
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_TRACING)
        {
            setStatus(STATUS_CHECKING);
            boolean done =(pixelsLeft(PIXEL_THRESHOLD));
            traceLock.lock();
            if(done)
                traceLock.unlockWithCondition(TIME_DONE);
            else
            {
                int cond = traceLock.conditionValue();
                traceLock.unlockWithCondition((cond & ~TRACING));
                setStatus(STATUS_WAITING_FOR_TRACE);
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
        Map trDict = new HashMap();
        trDict.put("n",currNo);
        final Map ftrDict = trDict;
        if(startTime == 0)
        {
            startTime = SystemClock.uptimeMillis();
            OBUtils.runOnOtherThreadDelayed(10,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    timesUp(ftrDict);
                }
            });
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    finishTrace(ftrDict);
                }
            });
        }
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_WAITING_FOR_TRACE)
        {
            traceLock.lock();
            int cond = traceLock.conditionValue();
            traceLock.unlockWithCondition(cond | TRACING);
            checkTraceStart(pt);
        }
    }

}


