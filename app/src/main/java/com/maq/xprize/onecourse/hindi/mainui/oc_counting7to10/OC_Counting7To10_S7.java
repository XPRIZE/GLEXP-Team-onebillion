package com.maq.xprize.onecourse.hindi.mainui.oc_counting7to10;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Tracing;
import com.maq.xprize.onecourse.hindi.utils.OBRunnableSyncUI;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 21/07/16.
 */
public class OC_Counting7To10_S7 extends OC_Generic_Tracing
{
    List<Integer> numberSequence, colourSequence;
    int sequenceIndex;
    int colouredObjects;


    public OC_Counting7To10_S7 ()
    {
        super(false);
    }


    @Override
    public void doAudio (String scene) throws Exception
    {
        String prompt = null;
        String repeat = null;
        //
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (sequenceIndex >= 0 && sequenceIndex < 3)
            {
                prompt = getAudioForScene(currentEvent(), "PROMPT").get(sequenceIndex);
                repeat = getAudioForScene(currentEvent(), "REPEAT").get(sequenceIndex);
            }
            else if (sequenceIndex == numberSequence.size() - 1)
            {
                prompt = null;
                repeat = getAudioForScene(currentEvent(), "REPEAT").get(4);
            }
            else
            {
                prompt = null;
                repeat = getAudioForScene(currentEvent(), "REPEAT").get(3);
            }
        }
        else if (status() == STATUS_WAITING_FOR_TRACE)
        {
            if (sequenceIndex == 0)
            {
                prompt = getAudioForScene(currentEvent(), "PROMPT2").get(0);
                repeat = getAudioForScene(currentEvent(), "REPEAT2").get(0);
            }
            else
            {
                prompt = getAudioForScene(currentEvent(), "PROMPT2").get(1);
                repeat = getAudioForScene(currentEvent(), "REPEAT2").get(1);
            }
        }
        List<Object> replayAudio = (List<Object>) (Object) new ArrayList<>(Arrays.asList(repeat));
        List<Object> queuedAudio = (List<Object>) (Object) new ArrayList<>(Arrays.asList(prompt));
        setReplayAudio(replayAudio);
        playAudioQueued(queuedAudio);
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
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(7, 8, 9, 10)));
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(7, 8, 9, 10)));
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(7, 8, 9, 10)));
        sequenceIndex = 0;
        //
        colourSequence = new ArrayList();
        colourSequence.add(OBUtils.colorFromRGBString("226,028,035"));
        colourSequence.add(OBUtils.colorFromRGBString("013,118,185"));
        colourSequence.add(OBUtils.colorFromRGBString("230,089,041"));
        colourSequence.add(OBUtils.colorFromRGBString("135,039,141"));
        colourSequence.add(OBUtils.colorFromRGBString("128,094,058"));
        colourSequence.add(OBUtils.colorFromRGBString("217,000,138"));
        colourSequence.add(OBUtils.colorFromRGBString("124,198,063"));
        colourSequence.add(OBUtils.colorFromRGBString("004,104,057"));
        colourSequence.add(OBUtils.colorFromRGBString("218,148,030"));
        colourSequence.add(OBUtils.colorFromRGBString("214,242,000"));
        colourSequence.add(OBUtils.colorFromRGBString("161,030,046"));
        colourSequence.add(OBUtils.colorFromRGBString("027,064,153"));
        colourSequence = OBUtils.randomlySortedArray(colourSequence);
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
        gotItRightBigTick(false);
        waitForSecs(0.3);
        //
        int number = numberSequence.get(sequenceIndex);
        int audioIndex = number - 7;
        playSceneAudioIndex("CORRECT", audioIndex, true);
        waitForSecs(0.3);
        //
        sequenceIndex++;
        if (sequenceIndex >= numberSequence.size())
        {
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            playSceneAudio("FINAL", true);
            nextScene();
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
            doAudio(currentEvent());
        }
    }


    public void checkBox(PointF pt)
    {
        List<OBControl> boxes = filterControls("box.*");
        OBControl box = finger(-1, 1, boxes, pt, true);
        if (box != null)
        {
            try
            {
                playSfxAudio("colour_object", false);
                //
                int number = numberSequence.get(sequenceIndex);
                pathColour = colourSequence.get(sequenceIndex);
                //
                lockScreen();
                deleteControls("trace.*");
                deleteControls("dash.*");
                tracing_reset(number);
                OC_Generic.colourObject(box, pathColour);
                box.disable();
                unlockScreen();
                //
                setStatus(STATUS_WAITING_FOR_TRACE);
                doAudio(currentEvent());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }



    public void tracing_setup (final Integer number)
    {
//        MainActivity.log("tracing_setup: " + number);
        new OBRunnableSyncUI()
        {
            public void ex ()
            {
                loadEvent(String.format("%d", number));
                path1 = (OBGroup) objectDict.get("trace_p1");
                path2 = (OBGroup) objectDict.get("trace_p2");
                //
                dash1 = (OBImage) objectDict.get("dash_p1");
                dash2 = (OBImage) objectDict.get("dash_p2");
                //
                trace_arrow = objectDict.get("trace_arrow");
                //
                if (dash1 != null) dash1.show();
                if (dash2 != null) dash2.show();
                //
                if (path1 != null)
                {
                    uPaths = tracing_processDigit(path1);
                    subPaths = new ArrayList();
                    subPaths.addAll(tracing_subpathControlsFromPath("trace_p1"));
                    path1.hide();
                    for (OBControl c : subPaths) c.hide();
                }
                if (path2 != null)
                {
                    uPaths.addAll(tracing_processDigit(path2));
                    subPaths.addAll(tracing_subpathControlsFromPath("trace_p2"));
                    path2.hide();
                    for (OBControl c : subPaths) c.hide();
                }
            }
        }.run();
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
                    checkBox(pt);
                }
            });

        }
    }

}