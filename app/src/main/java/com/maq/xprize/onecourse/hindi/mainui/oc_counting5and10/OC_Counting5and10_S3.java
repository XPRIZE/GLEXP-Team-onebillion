package com.maq.xprize.onecourse.hindi.mainui.oc_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 14/03/2017.
 */

public class OC_Counting5and10_S3 extends OC_Generic_SelectCorrectObject
{
    public List<OBLabel> numbers;
    public List<OBControl> places;
    public List<OBControl> removedControls;
    public int correctAnswer;
    public boolean needsSorting = false;
    public boolean needsRemoving = false;
    public int phase;
    public OBGroup alignmentGroup;


    public String action_getScenesProperty ()
    {
        return "scenes_all";
    }


    public String getObjectPrefix ()
    {
        if (needsSorting)
        {
            return "object";
        }
        else
        {
            return "group";
        }
    }


    public String getNumberPrefix ()
    {
        return "number";
    }


    public String getPlacementPrefix ()
    {
        return "place";
    }


    public int getNumberColor_normal ()
    {
        return Color.BLACK;
    }


    public int getNumberColor_highlight ()
    {
        return Color.RED;
    }

    public int getTotalShownObjects ()
    {
        int count = 0;
        for (OBControl control : filterControls(getObjectPrefix() + ".*"))
        {
            if (!control.hidden()) count++;
        }
        return count;
    }


    public void doMainXX () throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        //
        action_doIntro();
        //
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void doAudio (String scene) throws Exception
    {
//        MainActivity.log("OC_Counting5and10_S3:doAudio:" + phase);
        if (phase == 0)
        {
            setReplayAudioScene(currentEvent(), "REPEAT");
            playAudioQueuedScene(scene, "PROMPT", false);
        }
        else if (phase == 1)
        {
            setReplayAudioScene(currentEvent(), "REPEAT2");
            playAudioQueuedScene(scene, "PROMPT2", false);
        }
    }


    public void action_doIntro () throws Exception
    {
        if (needsSorting || needsRemoving)
        {
            // No need for this intro if the objects need sorting or removing
            return;
        }
        //
        String soundEffect = eventAttributes.get("sound");
        //
        String startingNumber_string = eventAttributes.get("startingNumber");
        int startingNumber = startingNumber_string == null ? 0 : Integer.parseInt(startingNumber_string);
        //
        List sortedControls = sortedFilteredControls(getObjectPrefix() + ".*");
        for (int i = startingNumber; i < sortedControls.size(); i++)
        {
            OBControl control = (OBControl) sortedControls.get(i);
            control.show();
            playSfxAudio(soundEffect, true);
            waitForSecs(0.3);

        }
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        phase = 0;
        //
        String correctAnswer_string = eventAttributes.get("correctAnswer");
        correctAnswer = Integer.parseInt(correctAnswer_string);
        //
        if (alignmentGroup != null)
        {
            detachControl(alignmentGroup);
        }
        //
        if (numbers != null)
        {
            for (OBLabel number : numbers)
            {
                detachControl(number);
            }
        }
        //
        numbers = new ArrayList<>();
        for (OBControl number_frame : sortedFilteredControls(getNumberPrefix() + ".*"))
        {
            OBLabel number = action_createLabelForControl(number_frame);
            number.setProperty("number", number_frame.attributes().get("number"));
            number.setProperty("frame", number_frame.attributes().get("id"));
            number.setColour(getNumberColor_normal());
            numbers.add(number);
        }
        //
        for (OBLabel number : numbers)
        {
            try
            {
                action_lowlightNumber(number);
            }
            catch (Exception e)
            {
                // nothing to do here
            }
        }
        //
        if (places != null)
        {
            for (OBControl place : places)
            {
                detachControl(place);
            }
        }
        //
        needsSorting = eventAttributes.get("needsSorting") != null && eventAttributes.get("needsSorting").equals("true");
        needsRemoving = eventAttributes.get("needsRemoving") != null && eventAttributes.get("needsRemoving").equals("true");
        //
        places = new ArrayList<>();
        for (OBControl place : sortedFilteredControls(getPlacementPrefix() + ".*"))
        {
            places.add(place);
            String objectForPlacementID = ((String) place.attributes().get("id")).replaceAll(getPlacementPrefix(), getObjectPrefix());
            OBControl object = objectDict.get(objectForPlacementID);
            object.setPosition(place.getWorldPosition());
            place.hide();
        }
        //
        if (needsRemoving || needsSorting)
        {
            showControls(getObjectPrefix() + ".*");
        }
        else
        {
            hideControls(getObjectPrefix() + ".*");
            //
            String startingNumber_string = eventAttributes.get("startingNumber");
            int startingNumber = startingNumber_string == null ? 0 : Integer.parseInt(startingNumber_string);
            //
            List sortedControls = sortedFilteredControls(getObjectPrefix() + ".*");
            for (int i = 0; i < startingNumber; i++)
            {
                OBControl control = (OBControl) sortedControls.get(i);
                control.show();
            }
        }
        //
        if (removedControls != null)
        {
            for (OBControl control : removedControls)
            {
                control.hide();
            }
        }
        else
        {
            removedControls = new ArrayList<>();
        }
        //
        if (scene.endsWith("a"))
        {
            List<OBControl> frames = sortedFilteredControls(getNumberPrefix() + ".*");
            //
            alignmentGroup = new OBGroup(frames);
            alignmentGroup.setProperty("originalPosition", alignmentGroup.getWorldPosition());
            //
            alignmentGroup.setPosition(bounds().width() * 0.5f, 0.3f * bounds().height());
            attachControl(alignmentGroup);
//            OC_Generic.sendObjectToTop(alignmentGroup, this);
            alignmentGroup.show();
            //
            //
            for (OBControl frame : frames)
            {
                frame.hide();
            }
            //
            for (OBControl number : numbers)
            {
                String frameID = (String) number.propertyValue("frame");
                OBControl frame = objectDict.get(frameID);
                number.setProperty("originalPosition", number.getWorldPosition());
                number.setPosition(frame.getWorldPosition());
                number.setScale(1.0f);
                number.hide();
                OC_Generic.sendObjectToTop(number, this);
            }
        }
    }


    public void action_highlightNumber (OBLabel label) throws Exception
    {
        lockScreen();
        String frameID = (String) label.propertyValue("frame");
        OBControl frame = objectDict.get(frameID);
        frame.highlight();
//        label.setColour(getNumberColor_highlight());
        unlockScreen();
    }


    public void action_lowlightNumber (OBLabel label) throws Exception
    {
        lockScreen();
        String frameID = (String) label.propertyValue("frame");
        OBControl frame = objectDict.get(frameID);
        frame.lowlight();
//        label.setColour(getNumberColor_normal());
        unlockScreen();
    }


    public Boolean check_isAnswerCorrect (OBLabel label)
    {
        OBLabel correctLabel = numbers.get(correctAnswer - 1);
        //
        return correctLabel.equals(label);
    }


    public void action_answerIsCorrect (OBLabel target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        playAudioQueuedScene("CORRECT", true);
        waitForSecs(0.3);
        //
        playAudioQueuedScene("FINAL", true);
        //
        nextScene();
    }


    public void action_answerIsWrong (OBLabel target) throws Exception
    {
        gotItWrongWithSfx();
        waitForSecs(0.3);
        //
        action_lowlightNumber(target);
        playAudioQueuedScene(currentEvent(), "INCORRECT", false);
    }


    public void action_removeObjects (OBControl group) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        playSfxAudio("remove_object", false);
        //
        lockScreen();
        removedControls.add(group);
        group.hide();
        unlockScreen();
        //
        if (getTotalShownObjects() == correctAnswer)
        {
            needsRemoving = false;
            phase = 1;
            //
            doAudio(currentEvent());
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            revertStatusAndReplayAudio();
        }
    }


    public void action_sortObjects () throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        String soundEffect = eventAttributes.get("sound");
        //
        int sorted = 0;
        for (OBControl control : sortedFilteredControls(getObjectPrefix() + ".*"))
        {
            OBAnim anim = OBAnim.moveAnim((PointF) control.propertyValue("originalPosition"), control);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.1);
            //
            if (++sorted % 10 == 0)
            {
                playSfxAudio(soundEffect, false);
            }
        }
        //
        needsSorting = false;
        phase = 1;
        //
        revertStatusAndReplayAudio();
        //
        doAudio(currentEvent());
    }


    public void action_selectNumber (OBLabel number)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            playSfxAudio("select_number", false);
            //
            action_highlightNumber(number);
            //
            if (check_isAnswerCorrect(number))
            {
                action_answerIsCorrect(number);
            }
            else
            {
                action_answerIsWrong(number);
                //
                revertStatusAndReplayAudio();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    public OBLabel findNumber (PointF pt)
    {
        return (OBLabel) finger(-1, 2, (List<OBControl>) (Object) numbers, pt, true);
    }


    public OBControl findObjectForSorting (PointF pt)
    {
        return finger(-1, 2, filterControls(getObjectPrefix() + ".*"), pt, true);
    }


    @Override
    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (needsSorting)
            {
                final OBControl c = findObjectForSorting(pt);
                if (c != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            action_sortObjects();
                        }
                    });
                    return;
                }
            }
            else if (needsRemoving)
            {
                final OBControl c = findObjectForSorting(pt);
                if (c != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            action_removeObjects(c);
                        }
                    });
                    return;
                }
            }
            else
            {
                final OBLabel number = findNumber(pt);
                if (number != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            action_selectNumber(number);
                        }
                    });
                    return;
                }
            }
        }
    }


    public void action_dropNumbers ()
    {
        List<OBControl> objectsForAnimation = new ArrayList<>();
        objectsForAnimation.add(alignmentGroup);
        for (OBLabel label : numbers)
        {
            objectsForAnimation.add(label);
        }
        float maxDelta = 0.63f * bounds().height();
        //
        physics_verticalDrop(objectsForAnimation, maxDelta, "drop_numbers");
    }


    public void demo3a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(true); // Let's count again!
        //
        List<OBControl> sortedNumberFrames = sortedFilteredControls(getNumberPrefix() + ".*");
        for (int i = 0; i < numbers.size(); i++)
        {
            action_playNextDemoSentence(false); // TEN. TWENTY. THIRTY. FORTY. FIFTY. SIXTY. SEVENTY. EIGHTY. NINETY. ONE HUNDRED.
            //
            OBLabel number = numbers.get(i);
            OBControl frame = sortedNumberFrames.get(i);
            //
            lockScreen();
            number.setColour(Color.RED);
            number.show();
            frame.show();
            unlockScreen();
            //
            OBAnim scaleAnim1 = OBAnim.scaleAnim(1.5f, number);
            OBAnim scaleAnim2 = OBAnim.scaleAnim(1.0f, number);
            //
            List<OBAnim> animations_firstSequence = new ArrayList<>();
            animations_firstSequence.add(scaleAnim1);
            //
            List<OBAnim> animations_secondSequence = new ArrayList<>();
            animations_secondSequence.add(scaleAnim2);
            //
            List<List<OBAnim>> animations_array = new ArrayList<>();
            animations_array.add(animations_firstSequence);
            animations_array.add(animations_secondSequence);
            //
            List<Float> durations = new ArrayList<>();
            durations.add(0.2f);
            durations.add(0.2f);
            //
            List<Integer> easeAnimations = new ArrayList<>();
            easeAnimations.add(OBAnim.ANIM_EASE_IN_EASE_OUT);
            easeAnimations.add(OBAnim.ANIM_EASE_IN_EASE_OUT);
            //
            OBAnimationGroup.chainAnimations(animations_array, durations, true, easeAnimations, 1, this);
            waitAudio();
            //
            lockScreen();
            number.setColour(Color.BLACK);
            unlockScreen();
            //
            waitForSecs(0.1);
        }
        //
        action_dropNumbers();
        //
        action_playNextDemoSentence(false); // Now look.
        action_doIntro();
        waitForSecs(0.3);
        //
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // The birds are in tens.
        OC_Generic.pointer_moveToObjectByName("group_3", -15f, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Let's count.
        OC_Generic.pointer_moveToObjectByName("group_3", -15f, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 3; i++)
        {
            action_playNextDemoSentence(false); // TEN. TWENTY. THIRTY;
            OC_Generic.pointer_moveToObjectByName("group_" + i, -15f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
            waitAudio();
            waitForSecs(0.3);
        }
        //
        action_playNextDemoSentence(false); // Thirty birds.
        OC_Generic.pointer_moveToObjectByName("group_3", -15f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }
}
