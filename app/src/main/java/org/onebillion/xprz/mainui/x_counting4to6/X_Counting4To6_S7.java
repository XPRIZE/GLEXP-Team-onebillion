package org.onebillion.xprz.mainui.x_counting4to6;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Tracing;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 18/07/16.
 */
public class X_Counting4To6_S7 extends XPRZ_Generic_Tracing
{
    List<Integer> numberSequence;
    int sequenceIndex;
    int colouredObjects;
    int traceColour;

    public X_Counting4To6_S7 ()
    {
        super(false);
    }

    @Override
    public void doAudio (String scene) throws Exception
    {
        doAudio_chooseDot();
    }

    @Override
    public long switchStatus (String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        numberSequence = new ArrayList();
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(4, 5, 6)));
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(4, 5, 6)));
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(4, 5, 6)));
        sequenceIndex = 0;
        //
        deleteControls("trace");
        deleteControls("dash");
        //
        if (redraw)
        {
            List<OBGroup> dots = (List<OBGroup>) (Object) filterControls("dot.*");
            for (OBGroup dot : dots)
            {
                XPRZ_Generic.colourObject(dot, Color.BLACK);
            }
            //
            List<OBGroup> stars = (List<OBGroup>) (Object) filterControls("star.*");
            for (OBGroup star : stars)
            {
                XPRZ_Generic.colourObject(star, Color.WHITE);
                star.enable();
            }
            //
//            int numberColour = OBUtils.colorFromRGBString(eventAttributes.get("font_colour"));
//            for (OBControl label : filterControls("label.*"))
//            {
//                OBLabel number = action_createLabelForControl(label, 1.2f, false);
//                number.setColour(numberColour);
//                label.hide();
//            }
        }
    }


    public void action_answerIsCorrect () throws Exception
    {
        gotItRightBigTick(true);
        //
        doAudio_colourStars();
        setStatus(STATUS_WAITING_FOR_OBJ_COLOUR_CLICK);
    }


    public void action_updateProgress ()
    {
        lockScreen();
        for (OBControl control : filterControls("dot.*"))
        {
            XPRZ_Generic.colourObject(control, OBUtils.colorFromRGBString(eventAttributes.get("progress_off")));
        }
        for (int i = 0; i <= sequenceIndex; i++)
        {
            OBControl control = objectDict.get(String.format("progress_%d", i));
            XPRZ_Generic.colourObject(control, OBUtils.colorFromRGBString(eventAttributes.get("progress_on")));
        }
        unlockScreen();
    }



    public void doAudio_chooseDot() throws Exception
    {
        if (sequenceIndex == 0)
        {
            playSceneAudioIndex("PROMPT", 0, false);
            List<Object> replayAudio = (List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT");
            setReplayAudio(Arrays.asList(replayAudio.get(0)));
        }
        else
        {
            playSceneAudioIndex("PROMPT", 1, false);
            List<Object> replayAudio = (List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT");
            setReplayAudio(Arrays.asList(replayAudio.get(1)));
        }
    }


    public void doAudio_traceNumber() throws Exception
    {
        playSceneAudio("PROMPT2", false);
        List<Object> replayAudio = (List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT2");
        setReplayAudio(replayAudio);
    }


    public void doAudio_colourStars() throws Exception
    {
        playSceneAudio("PROMPT3", false);
        List<Object> replayAudio = (List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT3");
        setReplayAudio(replayAudio);
    }



    public void checkDot(PointF pt)
    {
        List<OBControl> dots = filterControls("dot.*");
        OBControl c = finger(-1, 1, dots, pt, true);
        if (c != null)
        {
            try
            {
                playSfxAudio("select_dot", false);
                //
                OBGroup currentDot = (OBGroup) c;
                int number = numberSequence.get(sequenceIndex);
                traceColour = OBUtils.colorFromRGBString((String)currentDot.attributes().get("colour"));
                //
                lockScreen();
                for (OBControl star : filterControls("star.*"))
                {
                    star.enable();
                    XPRZ_Generic.colourObject(star, Color.WHITE);
                }
                colouredObjects = 0;
                hideControls("trace.*");
                hideControls("dash.*");
                tracing_reset(number, traceColour);
                XPRZ_Generic.colourObject(currentDot, traceColour);
                currentDot.disable();
                unlockScreen();
                //
                setStatus(STATUS_WAITING_FOR_TRACE);
                doAudio_traceNumber();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }



    public void checkStar(PointF pt)
    {
        List<OBControl> stars = filterControls("star.*");
        OBControl c = finger(-1, 1, stars, pt, true);
        if (c != null)
        {
            try
            {
                OBGroup star = (OBGroup) c;
                playSfxAudio("select_star", false);
                //
                lockScreen();
                star.disable();
                XPRZ_Generic.colourObject(star, traceColour);
                unlockScreen();
                //
                int number = numberSequence.get(sequenceIndex);
                if (++colouredObjects == number)
                {
                    setStatus(STATUS_CHECKING);
                    waitSFX();
                    waitForSecs(0.3);
                    //
                    gotItRightBigTick(true);
                    sequenceIndex++;
                    //
                    if (sequenceIndex == numberSequence.size())
                    {
                        playSceneAudio("FINAL", true);
                        waitForSecs(0.3);
                        //
                        nextScene();
                    }
                    else
                    {
                        doAudio_chooseDot();
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_WAITING_FOR_TRACE)
        {
            checkTraceStart(pt);
        }
        else if (status() == STATUS_AWAITING_CLICK)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    checkDot(pt);
                }
            });

        }
        else if (status() == STATUS_WAITING_FOR_OBJ_COLOUR_CLICK)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    checkStar(pt);
                }
            });

        }
    }



    public void demo7a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.8f, 0, 0.3f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Choose a black circle.
        XPRZ_Generic.pointer_moveToObjectByName("dot_1", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("select_dot", false);
        //
        OBGroup dot = (OBGroup) objectDict.get("dot_1");
        traceColour = OBUtils.colorFromRGBString((String)dot.attributes().get("colour"));
        lockScreen();
        XPRZ_Generic.colourObject(dot, traceColour);
        tracing_reset();
        tracing_setup(6, traceColour);
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Trace the number.
        pointer_demoTrace(false);
        action_playNextDemoSentence(true); // Six
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Now touch six stars, to match your number
        XPRZ_Generic.pointer_moveToObject(dash, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitForSecs(0.3);
        //
        for (int i = 0; i < 6; i++)
        {
            OBGroup star = (OBGroup) objectDict.get(String.format("star_%d", i+1));
            XPRZ_Generic.pointer_moveToObject(star, -10, 0.4f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            playSfxAudio("select_star", false);
            lockScreen();
            XPRZ_Generic.colourObject(star, traceColour);
            unlockScreen();
            waitForSecs(0.1);
        }
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Six stars.
        waitForSecs(0.3);
        //
        thePointer.hide();
        nextScene();
    }
}