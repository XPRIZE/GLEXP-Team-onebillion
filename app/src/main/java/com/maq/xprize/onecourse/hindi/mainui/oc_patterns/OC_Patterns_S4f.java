package com.maq.xprize.onecourse.hindi.mainui.oc_patterns;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.UPath;

import java.util.Collections;
import java.util.EnumSet;

import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.bezef;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.tPointAlongLine;

/**
 * Created by pedroloureiro on 16/03/2017.
 */


public class OC_Patterns_S4f extends OC_Generic_Event
{
    protected OBControl start, stop;
    protected OBPath line;
    protected float frac;
    protected PointF fingerLocation;
    protected boolean showBlink;


    public void fin ()
    {
        goToCard(OC_Patterns_S4m.class, "event4");
    }


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        theMoveSpeed = (int) (bounds().width() * 0.005);
        //
        start = objectDict.get("start");
        stop = objectDict.get("stop");
        //
        line = (OBPath) objectDict.get("line");
        if (line != null)
        {
            line.sizeToBox(boundsf());
            start.setPosition(OC_Generic.copyPoint(line.firstPoint()));
            stop.setPosition(OC_Generic.copyPoint(line.lastPoint()));
            line.setStrokeEnd(0.0f);
        }
        frac = 0.0f;
        showBlink = false;
    }


    public void action_blink ()
    {
        if (showBlink)
        {
            action_showShadow(start);
            waitForSecsNoThrow(0.3);
            action_removeShadow(start);
            waitForSecsNoThrow(0.3);
        }
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                action_blink();

            }
        });
    }


    public void action_startScene ()
    {
        action_blink();

    }

    public void action_endTracing () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        gotItRightBigTick(false);
        //
        lockScreen();
        line.setStrokeEnd(1.0f);
        start.hide();
        stop.hide();
        unlockScreen();
        //
        waitSFX();
        gotItRightBigTick(true);
        waitForSecs(0.3f);
        //
        playAudioQueuedScene("CORRECT", true);
        waitForSecs(0.3f);
        //
        playAudioQueuedScene("FINAL", true);
        //
        nextScene();
    }


    public float action_getStep ()
    {
        UPath path = deconstructedPath(currentEvent(), (String) line.attributes().get("id"));
        return theMoveSpeed / path.length();
    }


    public Object findStart (PointF pt, int precision)
    {
        return finger(0, precision, Collections.singletonList(start), pt);

    }


    public void action_pathTracing () throws Exception
    {
        if (status() == STATUS_DRAGGING)
        {
            if (findStart(fingerLocation, 2) != null)
            {
                frac += action_getStep();
                final float t = bezef(frac);
                OBUtils.runOnMainThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        line.setStrokeEnd(t);
                        start.setPosition(line.sAlongPath(t, null));
                    }
                });
                //
                waitForSecs(0.01f);
                if (t < 1)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            action_pathTracing();

                        }
                    });
                }
                else
                {
                    action_endTracing();
                }
            }
            else
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        action_pathTracing();

                    }
                });
            }
        }
    }

    public void demo4f () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now look.
        OC_Generic.pointer_moveToObjectByName("start", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // Now look. I am following the red circle, with my finger.

        UPath path = deconstructedPath(currentEvent(), (String) line.attributes().get("id"));
        float step = action_getStep();
        frac = 0.0f;
        while (frac <= 1)
        {
            frac += step / 1.2;
            final float t = bezef(frac);
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    lockScreen();
                    line.setStrokeEnd(t);
                    start.setPosition(line.sAlongPath(t, null));
                    thePointer.setPosition(OC_Generic.copyPoint(start.position()));
                    thePointer.setRotation((float) Math.toRadians(25 * frac - 25));
                    unlockScreen();
                }
            });
            waitForSecs(0.01f);
        }
        playSfxAudio("correct", false);
        //
        lockScreen();
        start.hide();
        stop.hide();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // It's a straight line.
        OC_Generic.pointer_moveToObjectByName("stop", 0, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void touchMovedToPoint (final PointF pt, View v)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                if (status() == STATUS_DRAGGING)
                {
                    fingerLocation = pt;
                }
            }
        });
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            Object target = findStart(pt, 2);
            //
            if (target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        setStatus(STATUS_DRAGGING);
                        fingerLocation = pt;
                        action_pathTracing();
                        showBlink = false;
                    }
                });
            }
        }
    }

    public void touchUpAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_AWAITING_CLICK);
            float t = bezef(frac);
            if (t > 1 - 5 * action_getStep())
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        action_endTracing();
                    }
                });
            }
            else
            {
                showBlink = true;
            }
        }
    }


}

