package com.maq.xprize.onecourse.hindi.mainui.oc_numbers1to10;

import android.graphics.Color;
import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class OC_Numbers1To10_S7 extends OC_Generic_SelectCorrectObject
{
    Map<String, OBLabel> numbers;
    OBLabel selectedNumber;

    public void action_resizeNumber (OBLabel label, Boolean increase, Boolean hilited)
    {
        lockScreen();
        float resizeFactor = (increase) ? 1.5f : 1.0f;
        PointF position = OC_Generic.copyPoint(label.position());
        float scale = (float) label.propertyValue("originalScale");
        label.setScale(scale * resizeFactor);
        label.setPosition(position);
        label.setColour((hilited) ? Color.RED : Color.BLACK);
        unlockScreen();
    }


    public void action_createNumbers ()
    {
        List<OBPath> controls = (List<OBPath>) (Object) filterControls("number.*");
        if (numbers != null)
        {
            for (OBLabel label : numbers.values())
            {
                detachControl(label);
            }
            numbers.clear();
        }
        numbers = new HashMap<String, OBLabel>();
        List<OBLabel> createLabels = new ArrayList();
        float smallestFontSize = 10000;
        //
        for (OBPath control : controls)
        {
            OBLabel label = action_createLabelForControl(control, 1.0f, false);
            numbers.put((String) control.attributes().get("id"), label);
            label.setProperty("originalScale", label.scale());
            control.hide();
            createLabels.add(label);
            smallestFontSize = Math.min(smallestFontSize, label.fontSize());
        }
        //
        for (OBLabel label : createLabels)
        {
            label.setFontSize(smallestFontSize);
            label.sizeToBoundingBox();
        }
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, true);
        //
        action_createNumbers();
        String selected = eventAttributes.get("selected_number");
        if (selected != null)
        {
            selectedNumber = numbers.get(String.format("number_%s", selected));
        }
    }


    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(false);
        lockScreen();
        action_resizeNumber((OBLabel) target, true, true);
        action_resizeNumber(selectedNumber, true, false);
        unlockScreen();
        waitSFX();
        //
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        nextScene();
    }


    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        action_resizeNumber((OBLabel) control, false, true);
    }


    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        action_resizeNumber((OBLabel) control, false, false);
    }

    @Override
    public OBControl findTarget (PointF pt)
    {
        List<OBControl> targets = (List<OBControl>) (Object) new ArrayList<>(numbers.values());
        return finger(-1, 2, targets, pt);
    }


    @Override
    public OBControl action_getCorrectAnswer ()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correct_answer");
        return numbers.get(correctString);
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }


    public void demo7a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        OBLabel number = numbers.get("number_5");
        OC_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Five.
        action_resizeNumber(number, true, false);
        waitAudio();
        waitForSecs(0.3);
        //
        number = numbers.get("number_6");
        action_playNextDemoSentence(false); // Six.
        OC_Generic.pointer_moveToObject(number, -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_resizeNumber(number, true, false);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Six is one more than five.
        action_resizeNumber(number, true, true);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo7g () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        for (int i = 1; i <= 10; i++)
        {
            OBLabel number = numbers.get(String.format("number_%d", i));
            action_playNextDemoSentence(false); // One. Two. Three. Four. Five. Six. Seven. Eight. Nine. Ten
            OBAnim anim = OBAnim.scaleAnim(1.5f, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitAudio();
            anim = OBAnim.scaleAnim(1.0f, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        //
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now look.
        OBLabel number = numbers.get("number_3");
        OC_Generic.pointer_moveToObject(number, -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Three
        action_resizeNumber(number, true, false);
        waitAudio();
        waitForSecs(0.3);
        //
        number = numbers.get("number_2");
        OC_Generic.pointer_moveToObject(number, -25, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Two
        action_resizeNumber(number, true, false);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Two is one less than three
        action_resizeNumber(number, true, true);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo7m () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        for (int i = 10; i > 0; i--)
        {
            OBLabel number = numbers.get(String.format("number_%d", i));
            action_playNextDemoSentence(false); // Ten. Nine. Eight. Seven. Six. Five. Four. Three. Two. One.
            OBAnim anim = OBAnim.scaleAnim(1.5f, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitAudio();
            anim = OBAnim.scaleAnim(1.0f, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.1);
        }
        //
        action_playNextDemoSentence(true); // VERY GOOD! You showed one more than, and one less than, for different numbers.
        //
        nextScene();
    }
}
