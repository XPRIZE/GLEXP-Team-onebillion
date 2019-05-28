package com.maq.xprize.onecourse.hindi.mainui.oc_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.controls.OBStroke;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 15/03/2017.
 */

public class OC_Counting5and10_S2b extends OC_Generic_Event
{
    List<OBLabel> keys;
    List<OBLabel> numberLabels;
    //
    String correctAnswer, currentAnswer;
    OBLabel correctLabel;
    OBGroup alignmentGroup;

    @Override
    public void fin ()
    {
        goToCard(OC_Counting5and10_S2i.class, "event2");
    }

    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }



    @Override
    public void doAudio (String scene) throws Exception
    {
        MainActivity.log("OC_Counting5and10_S2b:doAudio");
        super.doAudio(scene);
        //
        action_refreshDash(null);
    }

    @Override
    public void setSceneXX (String scene)
    {
        MainActivity.log("OC_Counting5and10_S2b:setSceneXX");
        super.setSceneXX(scene);
        //
        if (eventAttributes.get("redraw") != null && eventAttributes.get("redraw").equals("true"))
        {
            OBControl previous = null;
            List<OBControl> controls = sortedFilteredControls("place.*");
            //
            for (OBControl number : controls)
            {
                if (previous != null) number.setLeft(previous.right() - previous.lineWidth() * 2);
                previous = number;
            }
            //
            alignmentGroup = new OBGroup(controls);
            alignmentGroup.sizeToTightBoundingBox();
            alignmentGroup.setPosition(new PointF(bounds().width() / 2, bounds().height() / 3));
            attachControl(alignmentGroup);
            alignmentGroup.show();
            //
            if (numberLabels == null)
            {
                numberLabels = new ArrayList();
                //
                for (OBControl number : sortedFilteredControls("place.*"))
                {
                    OBLabel label = action_createLabelForControl(number, 1.0f, false);
                    label.setPosition(number.getWorldPosition());
                    attachControl(label);
                    numberLabels.add(label);
                    //
                    OC_Generic.sendObjectToTop(label, this);
                    //
                    label.show();
                    number.show();
                }
            }
            //
            if (keys == null)
            {
                keys = new ArrayList();
                //
                for (OBControl control : sortedFilteredControls("number.*"))
                {
                    OBLabel label = action_createLabelForControl(control, 1.2f, false);
                    label.setPosition(control.getWorldPosition());
                    attachControl(label);
                    keys.add(label);
                    //
                    OC_Generic.sendObjectToTop(label, this);
                    label.setColour(OBUtils.colorFromRGBString("34,165,0"));
                    //
                    label.hide();
                    control.hide();
                }
            }
        }
        //
        int correctAnswer_int = Integer.parseInt(eventAttributes.get("correctAnswer"));
        correctLabel = numberLabels.get(correctAnswer_int - 1);
        correctAnswer = new String(correctLabel.text());
        currentAnswer = new String("");
        //
        if (currentEvent().equals("2b"))
        {
            hideControls("dash");
            hideControls("frame");
        }
    }


    public void demo2b () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        OBLabel label = numberLabels.get(5);
        action_refreshDash(label);
        //
        waitSFX();
        waitForSecs(0.3);
        //
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now look … a number is missing!
        OC_Generic.pointer_moveToObject(label, 0, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE, OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // I will fill it in.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.5f, -5, 0.6f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        playSfxAudio("show_keys", false);
        lockScreen();
        for (OBControl key : keys)
        {
            key.show();
        }
        showControls("frame.*");
        unlockScreen();
        waitSFX();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Six …
        OBControl key = keys.get(6);
//        MainActivity.log("OC_Counting5and10_S2b:key:worldPosition:" + key.getWorldPosition());
//        MainActivity.log("OC_Counting5and10_S2b:key:position:" + key.position());
        OC_Generic.pointer_moveToObject(key, 10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("show_number", false);
        lockScreen();
        action_hiliteNumber(keys.get(6));
        label.setString("6");
        unlockScreen();
        waitSFX();
        action_lowliteNumber(keys.get(6));
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // … zero.
        key = keys.get(0);
        OC_Generic.pointer_moveToObject(key, 10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("show_number", false);
        lockScreen();
        action_hiliteNumber(keys.get(0));
        label.setString("60");
        unlockScreen();
        waitSFX();
        action_lowliteNumber(keys.get(0));
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // SIXTY!
        OC_Generic.pointer_moveToObject(label, 10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE, OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.7);
        //
        thePointer.hide();
        ;
        waitForSecs(0.3);
        //
        setStatus(STATUS_AWAITING_CLICK);
        //
        doAudio(currentEvent());
    }


    public void demo2h () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        // do nothing
        //
        setStatus(STATUS_AWAITING_CLICK);
        //
        doAudio(currentEvent());
    }


    public void finDemo2h () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        for (int i = 0; i < 10; i++)
        {
            action_playNextDemoSentence(false); // TEN. TWENTY. THIRTY. FORTY. FIFTY. SIXTY. SEVENTY. EIGHTY. NINETY. ONE HUNDRED.
            //
            OBLabel label = numberLabels.get(i);
            label.setColour(Color.RED);
            OBAnim scaleAnim1 = OBAnim.scaleAnim(1.5f, label);
            OBAnim scaleAnim2 = OBAnim.scaleAnim(1.0f, label);
            //
            List animationSequence1 = new ArrayList();
            animationSequence1.add(scaleAnim1);
            List animationSequence2 = new ArrayList();
            animationSequence2.add(scaleAnim2);
            //
            List animations = new ArrayList();
            animations.add(animationSequence1);
            animations.add(animationSequence2);
            //
            List durations = new ArrayList();
            durations.add(0.2f);
            durations.add(0.2f);
            //
            List timings = new ArrayList();
            timings.add(OBAnim.ANIM_EASE_IN_EASE_OUT);
            timings.add(OBAnim.ANIM_EASE_IN_EASE_OUT);
            OBAnimationGroup.chainAnimations(animations, durations, true, timings, 1, this);
            //
            waitAudio();
            label.setColour(Color.BLACK);
            waitForSecs(0.1);
        }
        //
        playSfxAudio("hide_keys", false);
        //
        lockScreen();
        for (OBControl key : keys)
        {
            key.hide();
        }
        hideControls("frame.*");
        unlockScreen();
        //
        waitSFX();
        waitForSecs(0.3);
    }


    public void action_refreshDash(OBLabel label) throws Exception
    {
        if (label == null)
        {
            label = correctLabel;
        }
        if (label != null)
        {
            playSfxAudio("show_dash", false);
            //
            lockScreen();
            //
            OBPath dash = (OBPath) objectDict.get("dash");
            dash.setLineWidth(applyGraphicScale(2.0f));
            //
            dash.setPosition(OC_Generic.copyPoint(label.position()));
            dash.setBottom(label.bottom() - 0.05f * label.height());
            OC_Generic.sendObjectToTop(dash, this);
            label.setString("");
            dash.show();
            //
            unlockScreen();
        }
    }


    public void action_hiliteNumber (OBLabel label)
    {
        label.setColour(Color.RED);
    }

    public void action_lowliteNumber (OBLabel label)
    {
        label.setColour(OBUtils.colorFromRGBString("34,165,0"));
    }


    public OBLabel findNumber (PointF pt)
    {
        return (OBLabel) finger(0, 2, (List<OBControl>) (Object) keys, pt, true);
    }


    public void checkNumber (OBLabel number) throws Exception
    {
        try
        {
            saveStatusClearReplayAudioSetChecking();
            //
            String value = number.text();
            String newValue = currentAnswer.concat(String.format("%c", value.charAt(0)));
            lockScreen();
            correctLabel.setString(newValue);
            action_hiliteNumber(number);
            unlockScreen();
            //
            playSfxAudio("show_number", true);
            action_lowliteNumber(number);
            //
            if (newValue.length() == 1 && value.equals("0"))
            {
                gotItWrongWithSfx();
                ;
                waitForSecs(0.3);
                //
                playSceneAudioIndex("INCORRECT", 0, false);
                correctLabel.setString(currentAnswer);
            }
            else
            {
                char correctValue = correctAnswer.charAt(currentAnswer.length());
                if (value.charAt(0) == correctValue)
                {
                    currentAnswer = newValue;
                    //
                    if (correctAnswer.length() == currentAnswer.length())
                    {
                        hideControls("dash");
                        waitForSecs(0.3);
                        //
                        gotItRightBigTick(true);
                        waitForSecs(0.3);
                        //
                        playSceneAudio("CORRECT", true);
                        waitForSecs(0.3);
                        //
                        performSel("finDemo", currentEvent());
                        //
                        nextScene();
                        return;
                    }
                }
                else
                {
                    gotItWrongWithSfx();
                    waitForSecs(0.3);
                    //
                    playSceneAudioIndex("INCORRECT", 1, false);
                    correctLabel.setString(currentAnswer);
                }
            }
            revertStatusAndReplayAudio();
        }
        catch(Exception e)
        {
            MainActivity.log("OC_Counting5and10_S2b:checknumber:exception caught");
            e.printStackTrace();
        }
    }

    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBLabel number = findNumber(pt);
            if (number != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkNumber(number);
                    }
                });
            }
        }
    }

}
