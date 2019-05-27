package com.maq.xprize.onecourse.mainui.oc_counting4to6;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Tracing;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 18/07/16.
 */
public class OC_Counting4To6_S7 extends OC_Generic_Tracing
{
    List<Integer> numberSequence;
    int sequenceIndex;
    int colouredObjects;

    public OC_Counting4To6_S7 ()
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
                OC_Generic.colourObject(dot, Color.BLACK);
            }
            //
            List<OBGroup> stars = (List<OBGroup>) (Object) filterControls("star.*");
            for (OBGroup star : stars)
            {
                OC_Generic.colourObject(star, Color.WHITE);
                star.enable();
            }
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
            OC_Generic.colourObject(control, OBUtils.colorFromRGBString(eventAttributes.get("progress_off")));
        }
        for (int i = 0; i <= sequenceIndex; i++)
        {
            OBControl control = objectDict.get(String.format("progress_%d", i));
            OC_Generic.colourObject(control, OBUtils.colorFromRGBString(eventAttributes.get("progress_on")));
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
                pathColour = OBUtils.colorFromRGBString((String)currentDot.attributes().get("colour"));
                //
                lockScreen();
                for (OBControl star : filterControls("star.*"))
                {
                    star.enable();
                    OC_Generic.colourObject(star, Color.WHITE);
                }
                colouredObjects = 0;
                hideControls("trace.*");
                hideControls("dash.*");
                tracing_reset(number);
                OC_Generic.colourObject(currentDot, pathColour);
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
                OC_Generic.colourObject(star, pathColour);
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
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.8f, 0, 0.3f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Choose a black circle.
        OC_Generic.pointer_moveToObjectByName("dot_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("select_dot", false);
        //
        OBGroup dot = (OBGroup) objectDict.get("dot_1");
        lockScreen();
        pathColour = OBUtils.colorFromRGBString((String)dot.attributes().get("colour"));
        OC_Generic.colourObject(dot, pathColour);
        tracing_reset(6);
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
        OC_Generic.pointer_moveToObject(dash1, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitForSecs(0.3);
        //
        for (int i = 0; i < 6; i++)
        {
            OBGroup star = (OBGroup) objectDict.get(String.format("star_%d", i+1));
            OC_Generic.pointer_moveToObject(star, -10, 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            playSfxAudio("select_star", false);
            lockScreen();
            OC_Generic.colourObject(star, pathColour);
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