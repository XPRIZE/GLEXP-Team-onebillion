package org.onebillion.xprz.mainui.x_count20;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.XPRZ_Tracer;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_MutBoolean;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alan on 24/01/16.
 */
public class X_Count20_S5 extends XPRZ_Tracer
{
    RectF ballFrame;
    double pixelsPerSecond;
    OBGroup p1,p2;

    OBGroup box;
    OBPath screenMask;
    List<OBControl> counters,numbers,randomCounters;
    double lastTime;
    long animToken = 0;

    public X_Count20_S5()
    {
        super();
    }

    void createBalls()
    {
        box.setMasksToBounds(true);
        OBControl st = objectDict.get("box_mask");
        List<OBControl>emptylist = new ArrayList<>();
        OBGroup parent = new OBGroup(emptylist,st.frame());
        parent.setMasksToBounds(true);
        parent.setZPosition(2.0005f);
        box.insertMember(parent,2,"counter_parent");
        float graphicScale = graphicScale();
        float textSize = Float.parseFloat(eventAttributes.get("textsize")) / graphicScale;
        textSize = MainActivity.mainActivity.applyGraphicScale(textSize);
        Typeface tf = OBUtils.standardTypeFace();
        float x = screenMask.position().x;
        counters = new ArrayList<>();
        numbers = new ArrayList<>();
        for (int i = 1;i <= 10;i++)
        {
            String counterName = String.format("counter%d",i);
            OBGroup cou = (OBGroup)objectDict.get(counterName);
            //detachControl(cou);
            counters.add(cou);
            Map<String,String> attrs = (Map<String, String>) cou.propertyValue("attrs");
            String colour = attrs.get("col");
            if (colour != null)
            {
                int col = OBUtils.colorFromRGBString(colour);
                cou.substituteFillForAllMembers("col.*",col);
            }
            float sc = cou.scale();
            //cou.setScale(1.0f);
            OBLabel txt = new OBLabel(String.format("%d",i+10),tf,textSize);
            txt.setColour(Color.BLACK);
            txt.setPosition(cou.position());
            numbers.add(txt);
            cou.insertMember(txt, -1, "txt");
            txt.setPosition(cou.bounds().width() / 2, cou.bounds().height() / 2);
            //cou.setRasterScale(2.0f);
            //cou.enCache();
            //parent.insertMember(cou,-1,counterName);
            cou.setProperty("n",i);
            //cou.setScale(sc);
            cou.setScreenMaskControl(screenMask);
        }
        float y = 0;
        targets = new ArrayList<>(counters);
        randomCounters = OBUtils.randomlySortedArray(counters);
        for (OBControl cou : randomCounters)
        {
            cou.setPosition(x, y);
            cou.setZPosition(5);
            RectF f = cou.frame();
            y += f.height() * 1.5;
        }
        ballFrame = new RectF(box.bounds());
        OBControl c = counters.get(0);
        float h  = c.frame().height() * 1.5f * 10;
        ballFrame.top = -(c.frame.height() * 1.5f);
        ballFrame.bottom = ballFrame.top + h;

    }
    void setUpBox()
    {
        box = (OBGroup)objectDict.get("box_group");
        OBControl mask = objectDict.get("box_mask");
        OBControl shape = objectDict.get("box_shape");
        OBPath boxstroke = (OBPath) objectDict.get("box_stroke");
        mask.parent.removeMember(mask);
        screenMask = (OBPath) mask.copy();
        screenMask.setFillColor(Color.BLUE);
        boxstroke.parent.removeMember(boxstroke);
        attachControl(boxstroke);
        boxstroke.setZPosition(30f);
        //screenMask.setLineWidth(boxstroke.lineWidth());
        //screenMask.setStrokeColor(boxstroke.strokeColor());
        //attachControl(screenMask);
        mask.setPosition(new PointF(shape.bounds().width() / 2, shape.bounds().height() / 2));
        mask.enCache();
        shape.setMaskControl(mask);
        box.texturise(false,this);
        //mask.hide();
        shape.enCache();
        for (OBControl c : filterControls("circle_p.*"))
        {
            OBGroup prog = (OBGroup)c;
            mask = prog.objectDict.get("circle_mask");
            shape = prog.objectDict.get("circle_shape");
            shape.setMaskControl(mask);
            shape.enCache();
            //mask.hide();
        }
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        setUpBox();
        createBalls();
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void start()
    {
        setStatus(0);
        startAnimations();
        new AsyncTask<Void, Void,Void>()
        {
            protected Void doInBackground(Void... params) {
                try
                {
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.execute();
    }

    public void setSceneXX(String scene)
    {
        deleteControls("p.*");
        deleteControls("n[0-9]*");
        for (OBControl counter: counters)
            counter.lowlight();
        uPaths = null;
        if (subPaths != null)
        {
            for (OBControl c : subPaths)
                detachControl(c);
            subPaths = null;
        }
        if (doneTraces != null)
        {
            for (OBControl c : doneTraces)
                detachControl(c);
        }
        doneTraces = new ArrayList<>();
        if (currentTrace != null)
        {
            detachControl(currentTrace);
            currentTrace = null;
        }
        finished = false;
        hideControls("tracearrow");
        showControls("circle_progress.*");
        super.setSceneXX(scene);
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doAudioStage2(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "STAGE2.REPEAT");
        playAudioQueuedScene(scene, "STAGE2", false);
    }

    public void doMainXX() throws Exception
    {
        startAnimations();
        doAudio(currentEvent());
    }

    void stopAnimations()
    {
        animToken = 0;
    }

    void doFrame()
    {
        if (_aborting)
        {
            stopAnimations();
            return;
        }
        final OBSectionController obs = this;
        lockScreen();
        long currTime = SystemClock.uptimeMillis();
        double distance = (currTime - lastTime) * pixelsPerSecond / 1000;
        for (OBControl counter : counters)
        {
            PointF pt = counter.position();
            float y = pt.y;
            y -= distance;
            if (y < ballFrame.top)
            {
                float diff = y - ballFrame.top;
                y = ballFrame.bottom + diff;
            }
            counter.setPosition(pt.x,y);
        }
        lastTime = currTime;
        unlockScreen();
    }

    void startAnimations()
    {
        lastTime = animToken = SystemClock.uptimeMillis();
        final long tok = animToken;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                long token = tok;
                pixelsPerSecond = bounds().height() / 4;
                while (token == animToken)
                {
                    doFrame();
                    waitForSecs(0.02);
                }
            }
        });
    }

    void swapCounterPositions(final OBControl c1,final OBControl c2)
    {
        if (c1 == c2)
            return;
        lockScreen();
        PointF pt = c1.position();
        float x = pt.x;
        float y = pt.y;
        c1.setPosition(c2.position());
        c2.setPosition(x,y);
        unlockScreen();

    }

    OBControl firstInvisibleCounter()
    {
        float bot = (box.bottom());
        float minTop = 100000;
        OBControl minC = null;
        for (OBControl c : counters)
            if (c.top() > bot && c.top() < minTop)
            {
                minTop = c.top();
                minC = c;
            }
        return minC;
    }

    List<OBPath>processDigit(OBGroup digit)
    {
        List<OBPath> arr = new ArrayList<>();
        for (int i = 1;i <=2;i++)
        {
            OBPath p = (OBPath)digit.objectDict.get(String.format("p%d",i));
            if (p != null)
            {
                arr.add(p);
            }
            else
                break;
        }
        return arr;
    }

    List<OBGroup>subpathControlsFromPath(String str)
    {
        List<OBGroup> arr = new ArrayList<>();
        OBGroup p = (OBGroup)objectDict.get(str);
        for (int i = 1;i < 10;i++)
        {
            String pp = String.format("p%d",i);
            OBPath characterfragment = (OBPath)p.objectDict.get(pp);
            if (characterfragment != null)
            {
                float savelw = characterfragment.lineWidth();
                characterfragment.setLineWidth(swollenLineWidth);
                characterfragment.sizeToBoundingBoxIncludingStroke();
                characterfragment.setLineWidth(savelw);
                //characterPieceControls.add(characterfragment);
                OBGroup g = splitPath(characterfragment);
                g.setScale(p.scale());
                g.setPosition(p.position());
                g.setPosition(convertPointFromControl(characterfragment.position(),characterfragment.parent));
                attachControl(g);
                arr.add(g);
            }
            else
                break;
        }
        return arr;
    }

    void setUpTracingForNumber(final int n)
    {
        new OBRunnableSyncUI(){public void ex()
        {
            int actualNumber = n + 10;
            loadEvent(String.format("%d",actualNumber));
            p1 = (OBGroup)objectDict.get("p1");
            p2 = (OBGroup)objectDict.get("p2");
            uPaths = processDigit(p1);
            uPaths.addAll(processDigit(p2));
            //characterPieceControls = new ArrayList<OBControl>();
            List<OBGroup> spArray = new ArrayList();
            spArray.addAll(subpathControlsFromPath("p1"));
            spArray.addAll(subpathControlsFromPath("p2"));
            p1.hide();
            p2.hide();
            subPaths = spArray;
            for (OBControl c : subPaths)
                c.hide();
        }}.run();

    }

    void tracePointerAlongPath(final OBPath p,float durationMultiplier) throws Exception

    {
        lockScreen();
        //p.setLineWidth(traceLineWidth);
        p.setStrokeColor(Color.RED);
        p.setStrokeEnd(0.0f);
        p.setOpacity(1.0f);
        p.parent.primogenitor().show();
        p.show();
        objectDict.get("tracearrow").hide();
        unlockScreen();
        long starttime = SystemClock.uptimeMillis();
        float duration = p.length() * 2 * durationMultiplier / theMoveSpeed;
        float frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (float)(currtime - starttime) / (duration * 1000);
            final float t = (frac);
            lockScreen();
            p.setStrokeEnd(t);
            thePointer.setPosition(convertPointFromControl(p.sAlongPath(t, null), p));
            p.parent.primogenitor().setNeedsRetexture();
            unlockScreen();
            waitForSecs(0.02f);
        }

    }
    public void demo5a() throws Exception
    {
        PointF targPt = OB_Maths.locationForRect(-0.2f, 0.8f, box.frame());
        PointF startpt = new PointF(targPt.x,bounds().height()+1);
        loadPointerStartPoint(startpt, targPt);
        PointF restPt = OB_Maths.tPointAlongLine(0.5f,startpt,targPt);
        movePointerToPoint(restPt,-1,true);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true);
        movePointerToPoint(targPt, -1, true);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(0.3f);
        final OBGroup counter = (OBGroup)objectDict.get("counter2");
        final OB_MutBoolean done = new OB_MutBoolean(false);
        while (!done.value)
        {
            new OBRunnableSyncUI(){public void ex()
            {
                done.value = !(counter.bottom() > 0 && counter.top() < box.bottom());
            }}.run();
            waitForSecs(0.02f);
        }
        OBControl c2 = firstInvisibleCounter();
        if (c2 != null && c2 != counter)
            swapCounterPositions(counter,c2);
        PointF destPt = OB_Maths.locationForRect(0.5f, 0.5f,box.frame());
        //double dur = (counter.position().y - convertPointToControl(destPt,box).y) / pixelsPerSecond;
        double dur = (counter.position().y - destPt.y) / pixelsPerSecond;
        waitForSecs(dur * 0.75f);
        movePointerToPoint(destPt, (float) dur * 0.25f, true);
        stopAnimations();
        playSFX("tap");
        counter.highlight();
        waitForSecs(0.2f);
        movePointerToPoint(targPt, (float) dur * 0.25f, true);
        setUpTracingForNumber(2);
        waitForSecs(0.2f);
        playAudioQueuedScene(currentEvent(), "DEMO2", true);
        OBControl traceArrow = objectDict.get("tracearrow");

        OBPath p = (OBPath)p1.objectDict.get("p1");
        PointF outVec = new PointF();
        PointF spt = p.sAlongPath(0,outVec);
        destPt = convertPointFromControl(spt,p);
        traceArrow.setPosition(destPt);
        traceArrow.pointAt(OB_Maths.AddPoints(destPt, outVec));
        traceArrow.show();
        movePointerToPoint(destPt, -30, -1, true);
        tracePointerAlongPath(p, 3);
        waitForSecs(0.8f);

        p = (OBPath)p2.objectDict.get("p1");
        spt = p.sAlongPath(0,outVec);
        destPt = convertPointFromControl(spt, p);
        traceArrow.setPosition(destPt);
        traceArrow.pointAt(OB_Maths.AddPoints(destPt, outVec));
        traceArrow.show();
        movePointerToPoint(destPt, -10, -1, true);
        tracePointerAlongPath(p, 3);
        waitForSecs(0.4f);
        movePointerForwards(-60, 0.3f);
        waitForSecs(0.2f);
        playAudioQueuedScene(currentEvent(), "DEMO3", true);
        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

    void fillProgress(int i)
    {
        OBGroup prog = (OBGroup)objectDict.get(String.format("circle_progress%d",i));
        OBPath p = (OBPath)prog.objectDict.get("circle_stroke");
        p.setFillColor(Color.RED);
    }

    public void burstBalloon(OBControl c)
    {

    }
    public void nextSubpath()
    {
        try
        {
            if (++subPathIndex >= subPaths.size())
            {
                gotItRightBigTick(true);
                burstBalloon(target);
                target.hide();
                targets.remove(target);
                waitForSecs(0.4);
                nextScene();
            }
            else
            {
                if (currentTrace != null)
                {
                    gotItRightBigTick(false);
                    waitSFX();
                    doneTraces.add(currentTrace);
                    currentTrace = null;
                    finished = false;
                    segmentIndex = 0;
                    positionArrow();
                }
                setStatus(STATUS_WAITING_FOR_TRACE);
            }
    }
        catch (Exception exception)
        {
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_TRACING)
        {
            setStatus(STATUS_CHECKING);
            effectMoveToPoint(pt);
            if (finished)
                new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        nextSubpath();
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            else
                setStatus(STATUS_WAITING_FOR_TRACE);
        }
    }


    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_TRACING)
        {
            effectMoveToPoint(pt);
        }
    }


    public void checkTraceStart(PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        boolean ok =  pointInSegment(pt,segmentIndex);
        if (!ok && currentTrace != null)
        {
            ok =  pointInSegment(lastTracedPoint(),segmentIndex+1) && pointInSegment(pt,segmentIndex+1);
            if (ok)
                segmentIndex++;
        }
        if (ok)
        {
            if (currentTrace == null)
            {
                startNewSubpath();
                PointF cpt = convertPointToControl(pt,currentTrace);
                //currDrawingPath.currPoint = cpt;
                currentTrace.moveToPoint(cpt.x,cpt.y);
            }
            else
            {
                //[currDrawingPath.elements addObject:[ULine ULineFrom:currDrawingPath.currPoint to:cpt]];
                PointF cpt = convertPointToControl(pt,currentTrace);
                currentTrace.addLineToPoint(cpt.x,cpt.y);
            }
            //[currentTrace setPath:[currentTracingPath CGPath]];
            setStatus(STATUS_TRACING);
        }
        else
        {
            setStatus(saveStatus);
        }
    }

    public void checkTarget(OBControl targ)
    {
        int saveStatus = status();
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            playSfxAudio("tap",false);
            target = targ;
            targ.highlight();
            stopAnimations();
            final int n = (int)counters.indexOf(targ) + 1;
            subPathIndex = 0;
            segmentIndex = 0;
            new OBRunnableSyncUI(){public void ex()
            {
                setUpTracingForNumber(n);
                positionArrow();
                fillProgress(eventIndex);
            }}.run();

            doAudioStage2(currentEvent());
            setStatus(STATUS_WAITING_FOR_TRACE);
        }
        catch (Exception exception)
        {
        }
    }

    public OBControl findTarget(PointF pt)
    {
        if (!box.frame().contains(pt.x,pt.y))
            return null;
        OBControl c = finger(-1,2,targets,pt);
        return c;
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        checkTarget(c);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }
        else if (status() == STATUS_WAITING_FOR_TRACE)
        {
            checkTraceStart(pt);
        }

    }

}
