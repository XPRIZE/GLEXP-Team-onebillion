package org.onebillion.xprz.mainui.x_counting4to6;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.XPRZ_Tracer;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Event;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_MutBoolean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class X_Counting4To6_S6 extends XPRZ_Tracer
{
    static float tracingSpeed = 2.0f;
    //
    int currentDemoAudioIndex;
    int currentTry;
    //
    OBGroup imageGroup;
    OBPath path1, path2;
    OBImage dash;
    OBControl trace_arrow;


    public X_Counting4To6_S6()
    {
        super();
    }


    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("master1");
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }


    public void start()
    {
        final long timeStamp = setStatus(STATUS_WAITING_FOR_TRACE);
        //
        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground(Void... params)
            {
                try
                {
                    if (!performSel("demo", currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                    //
                    OBUtils.runOnOtherThreadDelayed(3, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            if (!statusChanged(timeStamp))
                            {
                                playAudioQueuedScene(currentEvent(), "REMIND", false);
                            }
                        }
                    });
                }
                catch (Exception exception)
                {
                }
                return null;
            }
        }.execute();
    }


    public void setSceneXX(String scene)
    {
        ArrayList<OBControl> oldControls = new ArrayList<>(objectDict.values());
        //
        loadEvent(scene);
        //
        Boolean redraw = eventAttributes.get("redraw") != null && eventAttributes.get("redraw").equals("true");
        if (redraw)
        {
            if (imageGroup != null)
            {
                detachControl(imageGroup);
            }
            //
            for (OBControl control : oldControls)
            {
                if (control == null) continue;
                detachControl(control);
                objectDict.remove(control);
            }
            //
            for (OBControl control : filterControls(".*"))
            {
                String fit = (String) control.attributes().get("fit");
                if (fit != null && fit.equals("fitwidth"))
                {
                    float scale = bounds().width() / control.width();
                    PointF position = XPRZ_Generic.copyPoint(control.position());
                    float originalHeight = control.height();
                    control.setScale(scale * control.scale());
                    float heightDiff = control.height() - originalHeight;
                    position.y += heightDiff / 2;
                    control.setPosition(position);
                }
            }
        }
        currentTry = 1;
        trace_arrow = objectDict.get("trace_arrow");
        currentDemoAudioIndex = 0;
        //
        List<OBControl> images = filterControls("obj.*");
        imageGroup = new OBGroup(images);
        attachControl(imageGroup);
        //
        tracing_reset();
    }


    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }


    public void doMainXX() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        doAudio(currentEvent());
        //
        tracing_reset();
        //
        setStatus(STATUS_WAITING_FOR_TRACE);
    }


    // TRACING


    public void tracing_reset()
    {
        uPaths = null;
        if (subPaths != null)
        {
            for (OBControl c : subPaths)
                detachControl(c);
            subPaths = null;
        }
        //
        if (doneTraces != null)
        {
            for (OBControl c : doneTraces)
                detachControl(c);
        }
        //
        doneTraces = new ArrayList<>();
        if (currentTrace != null)
        {
            detachControl(currentTrace);
            currentTrace = null;
        }
        //
        finished = false;
        if (trace_arrow != null) trace_arrow.hide();
        //
        subPathIndex = 0;
        segmentIndex = 0;
        //
        new OBRunnableSyncUI()
        {
            public void ex()
            {
                tracing_setup();
                tracing_position_arrow();
            }
        }.run();
    }


    public void tracing_setup()
    {
        new OBRunnableSyncUI()
        {
            public void ex()
            {
                pathColour = Color.BLUE;
                OBGroup trace = (OBGroup) objectDict.get("trace");
                path1 = (OBPath) trace.objectDict.get("p1"); // only 1 path for 0,1,2,3,5,6
                path2 = (OBPath) trace.objectDict.get("p2"); // 2 paths for 4
                //
                dash = (OBImage) objectDict.get("dash");
                //
                uPaths = tracing_processDigit(trace); // only one number to trace
                subPaths = new ArrayList();
                subPaths.addAll(tracing_subpathControlsFromPath("trace"));
                trace.hide();
                for (OBControl c : subPaths) c.hide();
            }
        }.run();
    }


    List<OBPath> tracing_processDigit(OBGroup digit)
    {
        List<OBPath> arr = new ArrayList<>();
        for (int i = 1; i <= 2; i++)
        {
            OBPath p = (OBPath) digit.objectDict.get(String.format("p%d", i));
            if (p != null)
            {
                arr.add(p);
            }
            else
                break;
        }
        return arr;
    }

/*

    -(NSArray*)processDigit:(OBPath*)digit
    {
        NSMutableArray *arr = [NSMutableArray array];
        [digit setLineDashPattern:nil];
        [digit setOpacity:0.3];
        [digit setLineWidth:[self swollenLineWidth]];
        [digit sizeToBoundingBoxIncludingStroke];
        UPath *uPath = DeconstructedPath(digit.path);
        [arr addObject:uPath];
        return arr;
    }
*/


    List<OBGroup> tracing_subpathControlsFromPath(String str)
    {
        List<OBGroup> arr = new ArrayList<>();
        OBGroup p = (OBGroup) objectDict.get(str);
        for (int i = 1; i < 10; i++)
        {
            String pp = String.format("p%d", i);
            OBPath characterfragment = (OBPath) p.objectDict.get(pp);
            if (characterfragment != null)
            {
                float savelw = characterfragment.lineWidth();
                characterfragment.setLineWidth(swollenLineWidth);
                characterfragment.sizeToBoundingBoxIncludingStroke();
                characterfragment.setLineWidth(savelw);
                OBGroup g = splitPath(characterfragment);
                g.setScale(p.scale());
                g.setPosition(p.position());
                g.setPosition(convertPointFromControl(characterfragment.position(), characterfragment.parent));
                attachControl(g);
                arr.add(g);
            }
            else
                break;
        }
        return arr;
    }


    public void tracing_nextSubpath()
    {
        try
        {
            if (++subPathIndex >= subPaths.size())
            {
                setStatus(STATUS_CHECKING);
                //
                if (currentTry == 1)
                {
                    gotItRightBigTick(false);
                    waitSFX();
                    waitForSecs(0.3);
                    //
                    playAudioQueuedScene(currentEvent(), "CORRECT", true);
                    waitForSecs(0.7);
                    //
                    currentTry++;
                    playAudioQueuedScene(currentEvent(), "PROMPT2", false);
                    setStatus(STATUS_AWAITING_CLICK);
                }
                else
                {
                    gotItRightBigTick(true);
                    //
                    playAudioQueuedScene(currentEvent(), "CORRECT", true);
                    waitForSecs(0.3);
                    //
                    playAudioQueuedScene(currentEvent(), "FINAL", true);
                    waitForSecs(0.3);
                    //
                    nextScene();
                }
            }
            else
            {
                if (currentTrace != null)
                {
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


    void pointer_traceAlongPath(final OBPath p, float durationMultiplier) throws Exception
    {
//        new OBRunnableSyncUI()
//        {
//            public void ex()
//            {
        //p.setLineWidth(traceLineWidth);
        p.setStrokeColor(Color.BLUE);
        p.setStrokeEnd(0.0f);
        p.setOpacity(1.0f);
        //
        p.parent.primogenitor().show();
        p.show();
        trace_arrow.hide();
//            }
//        }.run();
        long starttime = SystemClock.uptimeMillis();
        float duration = p.length() * 2 * durationMultiplier / theMoveSpeed;
        float frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (float) (currtime - starttime) / (duration * 1000);
            final float t = (frac);
            new OBRunnableSyncUI()
            {
                public void ex()
                {
                    p.setStrokeEnd(t);
                    thePointer.setPosition(convertPointFromControl(p.sAlongPath(t, null), p));
                    p.parent.primogenitor().setNeedsRetexture();
                }
            }.run();
            waitForSecs(0.02f);
        }
    }


    public PointF tracing_position_arrow()
    {
        PointF outvec = new PointF();
        OBPath p = uPaths.get(subPathIndex);
        PointF arrowpoint = convertPointFromControl(p.sAlongPath(0.0f, outvec), p);
        trace_arrow.setPosition(arrowpoint);
        trace_arrow.rotation = (float) Math.atan2(outvec.x, -outvec.y);
        trace_arrow.show();
        trace_arrow.setZPosition(50);
        trace_arrow.setOpacity(1.0f);
        return arrowpoint;
    }


    // DEMOS


    public void demo6a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Four cats.
        XPRZ_Generic.pointer_moveToObject(imageGroup, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        path1.setStrokeEnd(0.0f);
        path2.setStrokeEnd(0.0f);
        //
        action_playNextDemoSentence(false); // We write four like this.
        PointF destination = tracing_position_arrow();
        movePointerToPoint(destination, -25, 0.6f, true);
        trace_arrow.hide();
        pointer_traceAlongPath(path1, tracingSpeed);
        subPathIndex++;
        //
        destination = tracing_position_arrow();
        movePointerToPoint(destination, -25, 0.6f, true);
        pointer_traceAlongPath(path2, tracingSpeed);
        //
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Four
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo6c() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Five birds. Six tomatoes.
        XPRZ_Generic.pointer_moveToObject(imageGroup, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        path1.setStrokeEnd(0.0f);
        //
        PointF destination = tracing_position_arrow();
        movePointerToPoint(destination, -25, 0.6f, true);
        //
        action_playNextDemoSentence(false); // Five. Six
        trace_arrow.hide();
        pointer_traceAlongPath(path1, tracingSpeed);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo6e() throws Exception
    {
        demo6c();
    }


    // ACTIONS

    public void action_playNextDemoSentence(Boolean waitAudio) throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentDemoAudioIndex, waitAudio);
        currentDemoAudioIndex++;
    }


    // TOUCHES AND CHECKS


    public OBControl findTarget(PointF pt)
    {
        if (!dash.frame.contains(pt.x, pt.y))
        {
            return null;
        }
        OBControl c = finger(-1, 2, targets, pt);
        return c;
    }



    public void checkTraceStart(PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        boolean ok = pointInSegment(pt, segmentIndex);
        if (!ok && currentTrace != null)
        {
            ok = pointInSegment(lastTracedPoint(), segmentIndex + 1) && pointInSegment(pt, segmentIndex + 1);
            if (ok)
                segmentIndex++;
        }
        if (ok)
        {
            trace_arrow.hide();
            //
            if (currentTrace == null)
            {
                startNewSubpath();
                PointF cpt = convertPointToControl(pt, currentTrace);
                currentTrace.moveToPoint(cpt.x, cpt.y);
            }
            else
            {
                PointF cpt = convertPointToControl(pt, currentTrace);
                currentTrace.addLineToPoint(cpt.x, cpt.y);
            }
            setStatus(STATUS_TRACING);
        }
        else
        {
            setStatus(saveStatus);
        }
    }




    public void touchUpAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_TRACING)
        {
            setStatus(STATUS_CHECKING);
            effectMoveToPoint(pt);
            if (finished)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        tracing_nextSubpath();
                    }
                });
            }
            else
            {
                setStatus(STATUS_WAITING_FOR_TRACE);
            }
        }
    }


    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_TRACING)
        {
            effectMoveToPoint(pt);
        }
    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            OBControl dash = objectDict.get("dash");
            OBControl closestTarget = finger(-1, 2, filterControls("dash"), pt);
            if (closestTarget != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        tracing_reset();
                        tracing_position_arrow();
                        setStatus(STATUS_WAITING_FOR_TRACE);
                    }
                });
            }
        }
        else if (status() == STATUS_WAITING_FOR_TRACE)
        {
            checkTraceStart(pt);
        }
    }

}
