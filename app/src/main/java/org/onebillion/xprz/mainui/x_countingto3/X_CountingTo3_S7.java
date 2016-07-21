package org.onebillion.xprz.mainui.x_countingto3;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Tracing;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 14/07/16.
 */
public class X_CountingTo3_S7 extends XPRZ_Generic_Tracing
{

    List<Integer> numberSequence;
    int sequenceIndex;

    public X_CountingTo3_S7 ()
    {
        super(false);
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        numberSequence = new ArrayList();
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(0, 1, 2, 3)));
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(0, 2, 3)));
        sequenceIndex = 0;
        //
        deleteControls("trace");
        deleteControls("dash");
        //
        if (redraw)
        {
            int numberColour = OBUtils.colorFromRGBString(eventAttributes.get("font_colour"));
            for (OBControl label : filterControls("label.*"))
            {
                OBLabel number = action_createLabelForControl(label, 1.2f, false);
                number.setColour(numberColour);
                label.hide();
            }
        }
    }

    public void action_flashButton (Boolean turnedOn) throws Exception
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            OBGroup wheel = (OBGroup) objectDict.get("wheel");
            OBControl button = wheel.objectDict.get("button");
            XPRZ_Generic.colourObject(button, OBUtils.colorFromRGBString((turnedOn) ? eventAttributes.get("button_on") : eventAttributes.get("button_off")));
            waitForSecs(0.6);
            action_flashButton(!turnedOn);
        }
    }

    public void action_spinWheel (final int number) throws Exception
    {
        OBGroup wheel = (OBGroup) objectDict.get("wheel");
        OBControl arrow = wheel.objectDict.get("arrow");
        Integer previousNumber = (Integer) arrow.propertyValue("previousNumber");
        float angle = 90 * number;
        if (previousNumber != null)
        {
            angle += Math.toDegrees(arrow.rotation()) - 90 * previousNumber;
        }
        arrow.setProperty("previousNumber", number);
        OBAnim rotateAnim = OBAnim.rotationAnim((float) Math.toRadians(1440 + angle), arrow);
        playSfxAudio("wheel_spin", false);
        OBAnimationGroup.runAnims(Arrays.asList(rotateAnim), 1.0 + (0.1 * number), false, OBAnim.ANIM_EASE_IN_EASE_OUT, new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                playSfxAudio("wheel_stop", false);
                //
                action_updateProgress();
                tracing_setup(number);
                //
                if (sequenceIndex == 0)
                {
                    List<String> replayAudioList = getAudioForScene(currentEvent(), "REPEAT2");
                    List replayAudio = new ArrayList();
                    replayAudio.add(replayAudioList.get(0));
                    setReplayAudio(replayAudio);
                    playSceneAudioIndex("PROMPT2", 0, false);
                }
                else
                {
                    List<String> replayAudioList = getAudioForScene(currentEvent(), "REPEAT2");
                    List replayAudio = new ArrayList();
                    replayAudio.add(replayAudioList.get(1));
                    setReplayAudio(replayAudio);
                    playSceneAudioIndex("PROMPT2", 1, false);
                }
                //
                setStatus(STATUS_WAITING_FOR_TRACE);
            }
        }, this);
    }

    public void action_updateProgress ()
    {
        lockScreen();
        for (OBControl control : filterControls("progress.*"))
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


    public void action_touchDown (PointF pt)
    {
        try
        {
            saveStatusClearReplayAudioSetChecking();
            //
            OBGroup wheel = (OBGroup) objectDict.get("wheel");
            OBControl button = wheel.objectDict.get("button");
            OBControl c = finger(-1, 2, Arrays.asList(button), pt);
            if (c != null)
            {
                int number = numberSequence.get(sequenceIndex);
                action_spinWheel(number);
            }
            else
            {
                revertStatusAndReplayAudio();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void action_answerIsCorrect () throws Exception
    {
        gotItRightBigTick(true);
        //
        int number = numberSequence.get(sequenceIndex);
        playSceneAudioIndex("CORRECT", number, true);
        waitForSecs(0.3);
        //
        if (sequenceIndex == numberSequence.size() - 1)
        {
            playSceneAudio("FINAL", true);
            waitForSecs(0.3);
            //
            nextScene();
        }
        else
        {
            lockScreen();
            if (dash1 != null) dash1.hide();
            if (dash2 != null) dash2.hide();
            tracing_reset();
            unlockScreen();
            //
            sequenceIndex++;
            List<String> replayAudioList = getAudioForScene(currentEvent(), "REPEAT");
            List replayAudio = new ArrayList();
            replayAudio.add(replayAudioList.get(sequenceIndex));
            setReplayAudio(replayAudio);
            //
            playSceneAudioIndex("PROMPT", sequenceIndex, false);
            setStatus(STATUS_AWAITING_CLICK);
            //
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    action_flashButton(true);
                }
            });
        }
    }



    public void demo7a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look
        OBGroup wheel = (OBGroup) objectDict.get("wheel");
        OBControl button = wheel.objectDict.get("button");
        XPRZ_Generic.pointer_moveToObject(button, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        //
        List<String> replayAudioList = getAudioForScene(currentEvent(), "REPEAT");
        List replayAudio = new ArrayList();
        replayAudio.add(replayAudioList.get(sequenceIndex));
        setReplayAudio(replayAudio);
        //
        playSceneAudioIndex("PROMPT", sequenceIndex, true);
        //
        thePointer.hide();
        //
        setStatus(STATUS_AWAITING_CLICK);
        action_flashButton(true);
    }


}
