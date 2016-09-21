package org.onebillion.xprz.mainui.generic;

import android.graphics.Color;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBStroke;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_Tracer;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBUserPressedBackException;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class XPRZ_Generic_Tracing extends XPRZ_Tracer
{
    protected static float tracingSpeed = 2.0f;
    //
    protected int currentDemoAudioIndex;
    protected int currentTry;
    //
    protected OBGroup path1, path2;
    protected OBImage dash1, dash2;
    protected OBControl trace_arrow;

    int savedStatus;
    List<Object> savedReplayAudio;
    private Boolean autoClean;
    private PointF lastPointAdded;


    public XPRZ_Generic_Tracing (Boolean autoClean)
    {
        this.autoClean = autoClean;
    }


    public void pointer_demoTrace (Boolean playAudio) throws Exception
    {
        List<OBPath> paths = (List<OBPath>) (Object) path1.filterMembers("p[0-9]+", true);
        //
        PointF destination = tracing_position_arrow();
        movePointerToPoint(destination, -25, 0.6f, true);
        //
        if (playAudio)
        {
            action_playNextDemoSentence(false);
        }
        //
        for (OBPath path : paths)
        {
            pointer_traceAlongPath(path, tracingSpeed);
            //
            subPathIndex++;
            destination = tracing_position_arrow();
            if (destination != null)
            {
                movePointerToPoint(destination, -25, 0.3f, true);
            }
        }
        //
        if (path2 != null)
        {
            paths = (List<OBPath>) (Object) path2.filterMembers("p[0-9]+", true);
            //
            for (OBPath path : paths)
            {
                pointer_traceAlongPath(path, tracingSpeed);
                //
                subPathIndex++;
                destination = tracing_position_arrow();
                if (destination != null)
                {
                    movePointerToPoint(destination, -25, 0.3f, true);
                }
            }
        }
        if (playAudio)
        {
            waitAudio();
        }
    }


    public void action_answerIsCorrect () throws Exception
    {
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
            //
            if (autoClean)
            {
                lockScreen();
                tracing_reset();
                unlockScreen();
                //
                revertStatusAndReplayAudio();
                setStatus(STATUS_WAITING_FOR_TRACE);
            }
            else
            {
                revertStatusAndReplayAudio();
                setStatus(STATUS_AWAITING_CLICK);
            }
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


    public void prepare ()
    {
        super.prepare();
        loadFingers();
        loadEvent("master1");
        String scenes = (String) eventAttributes.get(action_getScenesProperty());
        if (scenes != null)
        {
            String[] eva = scenes.split(",");
            events = Arrays.asList(eva);
        }
        else
        {
            events = new ArrayList<>();
        }
        //
        if (currentEvent() != null)
        {
            doVisual(currentEvent());
        }
        pathColour = Color.BLUE;
    }


    @Override
    public long switchStatus (String scene)
    {
        return setStatus(STATUS_WAITING_FOR_TRACE);
    }


    public void start ()
    {
        final long timeStamp = switchStatus(currentEvent());
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
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
                        public void run () throws Exception
                        {
                            if (!statusChanged(timeStamp))
                            {
                                playAudioQueuedScene(currentEvent(), "REMIND", false);
                            }
                        }
                    });
                }
                catch (OBUserPressedBackException e)
                {
                    stopAllAudio();
                    throw e;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    public void setSceneXX (String scene)
    {
        ArrayList<OBControl> oldControls = new ArrayList<>(objectDict.values());
        //
        loadEvent(scene);
        //
        Boolean redraw = eventAttributes.get("redraw") != null && eventAttributes.get("redraw").equals("true");
        if (redraw)
        {
            for (OBControl control : oldControls)
            {
                if (control == null) continue;
                detachControl(control);
                objectDict.remove(control);
            }
            //
            for (OBControl control : filterControls(".*"))
            {
                Map attributes = control.attributes();
                if (attributes != null)
                {
                    String fit = (String) attributes.get("fit");
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
        }
        //
        currentDemoAudioIndex = 0;
        //
        XPRZ_Generic.colourObjectsWithScheme(this);
        //
        action_prepareScene(scene, redraw);
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        currentTry = 1;
        trace_arrow = objectDict.get("trace_arrow");
        currentDemoAudioIndex = 0;
        //
        tracing_reset();
    }


    public void doAudio (String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }


    public void doMainXX () throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        doAudio(currentEvent());
        //
        tracing_reset();
        //
        setStatus(STATUS_WAITING_FOR_TRACE);
    }


    // TRACING

    public void tracing_reset ()
    {
        tracing_reset(null);
    }

    public void tracing_reset (final Integer number)
    {
        lastPointAdded = null;
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
            public void ex ()
            {
                tracing_setup(number);
                tracing_position_arrow();
            }
        }.run();
    }


    public void tracing_setup ()
    {
        tracing_setup(null);
    }

    public void tracing_setup (final Integer number)
    {
//        MainActivity.log("tracing_setup: " + number);
        new OBRunnableSyncUI()
        {
            public void ex ()
            {
                String traceControl = (number == null) ? "trace" : String.format("trace_%d", number);
                path1 = (OBGroup) objectDict.get(traceControl);
                //
                String dashControl = (number == null) ? "dash" : String.format("dash_%d", number);
                dash1 = (OBImage) objectDict.get(dashControl);
                if (dash1 != null)
                {
                    dash1.show();
                }
                //
                if (path1 != null)
                {
                    uPaths = tracing_processDigit(path1); // only one number to trace
                    subPaths = new ArrayList();
                    subPaths.addAll(tracing_subpathControlsFromPath(traceControl));
                    path1.hide();
                    for (OBControl c : subPaths) c.hide();
                }
            }
        }.run();
    }


    public List<OBPath> tracing_processDigit (OBGroup digit)
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


    public OBGroup splitPath(OBPath obp)
    {
        int lengthPerSplit = 120; //100
        PathMeasure pm = new PathMeasure(obp.path(),false);
        float splen = pm.getLength();
        int noSplits = (int)(splen / lengthPerSplit);
        List<OBPath> newOBPaths = splitInto(obp, pm, noSplits, 1.0f / (noSplits * 4));
        for (OBPath newOBPath : newOBPaths)
        {
            //newOBPath.setBounds(obp.bounds);
            //newOBPath.setPosition(obp.position());
            //newOBPath.sizeToBox(obp.bounds());
            newOBPath.setStrokeColor(Color.argb((int)(255 * 0.4f),255,0,0));
            newOBPath.setLineJoin(OBStroke.kCALineJoinRound);
            newOBPath.setFillColor(0);
            newOBPath.setLineWidth(swollenLineWidth);
        }
        MainActivity.log("Now serving " + newOBPaths.size());
        OBGroup grp = new OBGroup((List<OBControl>)(Object)newOBPaths);
        return grp;
    }


    public List<OBGroup> tracing_subpathControlsFromPath (String str)
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
                characterfragment.setStrokeEnd(0.0f);
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


    public void tracing_nextSubpath ()
    {
        try
        {
            if (++subPathIndex >= subPaths.size())
            {
                saveStatusClearReplayAudioSetChecking();
                //
                action_answerIsCorrect();
            }
            else
            {
                if (currentTrace != null)
                {
                    lastPointAdded = null;
                    doneTraces.add(currentTrace);
                    currentTrace = null;
                    finished = false;
                    segmentIndex = 0;
                    positionArrow();
                }
                revertStatusAndReplayAudio();
                setStatus(STATUS_WAITING_FOR_TRACE);
            }
        }
        catch (Exception exception)
        {
        }
    }


    public void pointer_traceAlongPath (final OBPath p, float durationMultiplier) throws Exception
    {
        p.setStrokeColor(pathColour);
        p.setStrokeEnd(0.0f);
        p.setOpacity(1.0f);
        //
        p.parent.primogenitor().show();
        p.show();
        trace_arrow.hide();
        //
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
                public void ex ()
                {
                    p.setStrokeEnd(t);
                    thePointer.setPosition(convertPointFromControl(p.sAlongPath(t, null), p));
                    p.parent.primogenitor().setNeedsRetexture();
                }
            }.run();
            waitForSecs(0.02f);
        }
    }


    public PointF tracing_position_arrow ()
    {
        try
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
        catch (Exception e)
        {
            return null;
        }
    }


    public void action_playNextDemoSentence (Boolean waitAudio) throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentDemoAudioIndex, waitAudio);
        currentDemoAudioIndex++;
    }


    public OBControl findTarget (PointF pt)
    {
        Boolean contained1 = dash1 != null && dash1.frame.contains(pt.x, pt.y);
        Boolean contained2 = dash2 != null && dash2.frame.contains(pt.x, pt.y);
        //
        if (!contained1 && !contained2)
        {
            return null;
        }
        OBControl c = finger(-1, 2, targets, pt);
        return c;
    }


    public void checkTraceStart (PointF pt)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        boolean ok = condition_isPointInSegment(pt);
        if (!ok && currentTrace != null)
        {
            ok = pointInSegment(lastTracedPoint(), segmentIndex + 1) && pointInSegment(pt, segmentIndex + 1);
            if (ok)
                segmentIndex++;
        }
        if (ok)
        {
            if (trace_arrow != null)
            {
                trace_arrow.hide();
            }
            //
            if (currentTrace == null)
            {
                startNewSubpath();
                PointF cpt = convertPointToControl(pt, currentTrace);
                currentTrace.moveToPoint(cpt.x, cpt.y);
                currentTrace.addLineToPoint(cpt.x + 1, cpt.y + 1);
            }
            else
            {
                addPointToTrace(pt, false);
            }
            revertStatusAndReplayAudio();
            setStatus(STATUS_TRACING);
        }
        else
        {
            addPointToTrace(pt, true);
            revertStatusAndReplayAudio();
        }
    }


    public void addPointToTrace (PointF pt, boolean searchForSegmentIndex)
    {
        if (currentTrace != null)
        {
            if (searchForSegmentIndex)
            {
                for (int i = segmentIndex; i < subPaths.get(subPathIndex).members.size(); i++)
                {
                    if (pointInSegment(lastTracedPoint(), i))
                    {
                        PointF cpt = convertPointToControl(pt, currentTrace);
                        currentTrace.addLineToPoint(cpt.x, cpt.y);
                        segmentIndex = i;
                        return;
                    }
                }
            }
            else
            {
                PointF cpt = convertPointToControl(pt, currentTrace);
                currentTrace.addLineToPoint(cpt.x, cpt.y);
            }
        }
    }


    public Boolean condition_isPointInSegment (PointF pt)
    {
        if (uPaths == null || uPaths.size() == 0) return false;
        //
        if (subPaths == null || subPaths.size() == 0) return false;
        //
        return pointInSegment(pt, segmentIndex);
    }


    public void touchUpAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_TRACING)
        {
            saveStatusClearReplayAudioSetChecking();
            //
            effectMoveToPoint(pt);
            if (finished)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        tracing_nextSubpath();
                    }
                });
            }
            else
            {
                revertStatusAndReplayAudio();
                setStatus(STATUS_WAITING_FOR_TRACE);
            }
        }
    }


    public void effectMoveToPoint (PointF pt)
    {
        if (!pointHitRelativeSeg(pt, 0))
        {
            if (segmentIndex + 1 < subPaths.get(subPathIndex).members.size())
            {
                if (pointHitRelativeSeg(pt, +1))
                {
                    segmentIndex++;
                }
                else
                {
                    new AsyncTask<Void, Void, Void>()
                    {
                        protected Void doInBackground (Void... params)
                        {
                            try
                            {
                                gotItWrongWithSfx();
                                setStatus(STATUS_WAITING_FOR_TRACE);
                            }
                            catch (Exception exception)
                            {
                            }
                            return null;
                        }
                    }.execute();

                    return;
                }
            }
            else
            {
                finished = true;
                return;
            }
        }
        if (segmentIndex + 1 == subPaths.get(subPathIndex).members.size())
            finished = true;
        //
        addPointToTrace(pt, false);
    }


    public void touchMovedToPoint (PointF pt, View v)
    {
        if (status() == STATUS_TRACING)
        {
            effectMoveToPoint(pt);
        }
    }


    public Boolean performTouchDown (PointF pt)
    {
        try
        {
            Method m = this.getClass().getMethod("action_touchDown", PointF.class);
            m.invoke(this, pt);
            return true;
        }
        catch (Exception e)
        {
            // ignore the exception
        }
        return false;
    }


    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (!performTouchDown(pt))
            {
                List<OBControl> controls = new ArrayList();
                if (dash1 != null) controls.add(dash1);
                if (dash2 != null) controls.add(dash2);
                OBControl closestTarget = finger(-1, 2, controls, pt);
                if (closestTarget != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            tracing_reset();
                            tracing_position_arrow();
                            setStatus(STATUS_WAITING_FOR_TRACE);
                        }
                    });
                }
            }
        }
        else if (status() == STATUS_WAITING_FOR_TRACE)
        {
            checkTraceStart(pt);
        }
    }


    public String action_getScenesProperty ()
    {
        return "scenes";
    }


    // Miscelaneous Functions
    public void playSceneAudio (String scene, Boolean wait) throws Exception
    {
        playAudioQueuedScene(currentEvent(), scene, wait);
        if (!wait) waitForSecs(0.01);
    }

    public void playSceneAudioIndex (String scene, int index, Boolean wait) throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), scene, index, wait);
        if (!wait) waitForSecs(0.01);
    }


    public OBLabel action_createLabelForControl (OBControl control)
    {
        return action_createLabelForControl(control, 1.0f, false);
    }


    public OBLabel action_createLabelForControl (OBControl control, float finalResizeFactor)
    {
        return action_createLabelForControl(control, finalResizeFactor, true);
    }


    public OBLabel action_createLabelForControl (OBControl control, float finalResizeFactor, Boolean insertIntoGroup)
    {
        return XPRZ_Generic.action_createLabelForControl(control, finalResizeFactor, insertIntoGroup, this);
    }


    // CHECKING functions

    public void saveStatusClearReplayAudioSetChecking ()
    {
        savedStatus = status();
        setStatus(STATUS_CHECKING);
        //
        savedReplayAudio = _replayAudio;
        setReplayAudio(null);
    }

    public void revertStatusAndReplayAudio ()
    {
        setStatus(savedStatus);
        setReplayAudio(savedReplayAudio);
    }

}
