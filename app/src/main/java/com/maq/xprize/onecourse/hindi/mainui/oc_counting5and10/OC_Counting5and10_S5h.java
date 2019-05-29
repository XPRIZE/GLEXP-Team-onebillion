package com.maq.xprize.onecourse.hindi.mainui.oc_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Counting5and10_S5h extends OC_Generic_Event
{
    OBLabel bigNumber;
    String currentAnswer, correctAnswer;


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }

    public void setSceneXX (String scene)
    {
        if (bigNumber != null)
        {
            detachControl(bigNumber);
        }
        //
        deleteControls(".*");
        //
        super.setSceneXX(scene);
        //
        int count = 0;
        for (OBControl control : sortedFilteredControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(control, 1.2f, false);
            control.hide();
            //
            objectDict.put(String.format("label_%d", count), label);
            OC_Generic.sendObjectToTop(label, this);
            count++;
        }

        OBControl frame = objectDict.get("bigNumber");
        bigNumber = action_createLabelForControl(frame, 1.0f, false);
        frame.hide();
        bigNumber.setString("");
        //
        OBControl dash = objectDict.get("dash");
        dash.setPosition(OC_Generic.copyPoint(bigNumber.getWorldPosition()));
        dash.setBottom(bigNumber.bottom() - bigNumber.height() * 0.15f);
        //
        correctAnswer = eventAttributes.get("correctAnswer");
        currentAnswer = "";
    }


    public void demo5h () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now look.
        OBGroup group = (OBGroup) objectDict.get("group_3");
        movePointerToPoint(new PointF(0.8f * bounds().width(), 0.7f * bounds().height()), -5, 0.6f, true);
        //
        OC_Generic.pointer_moveToObject(group, -5f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 1; i <= 3; i++)
        {
            action_playNextDemoSentence(false); // Five.Ten.Fifteen.
            group = (OBGroup) objectDict.get(String.format("group_%d", i));
            OC_Generic.pointer_moveToObject(group, -5f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            waitAudio();
            waitForSecs(0.3f);
        }
        //
        action_playNextDemoSentence(false); // Fifteen boats.
        group = (OBGroup) objectDict.get("group_3");
        OC_Generic.pointer_moveToObject(group, -5f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // I will show the number here.
        OC_Generic.pointer_moveToObjectByName("dash", 15f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // One …
        OBLabel label = (OBLabel) objectDict.get("label_1");
        OC_Generic.pointer_moveToObject(label, -15f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("number_down", false);
        //
        lockScreen();
        action_hiliteNumber(label);
        bigNumber.setString("1");
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        action_lowliteNumber(label);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // …five.
        label = (OBLabel) objectDict.get("label_5");
        OC_Generic.pointer_moveToObject(label, 10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("number_down", false);
        //
        lockScreen();
        action_hiliteNumber(label);
        bigNumber.setString("15");
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        action_lowliteNumber(label);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); //FIFTEEN !
        OBControl dash = objectDict.get("dash");
        dash.hide();
        OC_Generic.pointer_moveToObject(dash, 15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.3f);
        //
        playSfxAudio("object_hide", false);
        lockScreen();
        hideControls("group.*");
        bigNumber.hide();
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        playSfxAudio("number_show", false);
        //
        lockScreen();
        bigNumber.setString("");
        bigNumber.show();
        dash.show();
        showControls("group_1");
        showControls("group_2");
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void action_hiliteNumber (OBLabel label)
    {
        label.setColour(Color.RED);

    }

    public void action_lowliteNumber (OBLabel label)
    {
        label.setColour(Color.BLACK);

    }

    public Object findNumber (PointF pt)
    {
        return finger(0, 2, filterControls("label.*"), pt);

    }

    public void checkNumber (OBLabel number) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        String value = number.text();
        //
        String newValue = currentAnswer.concat(String.format("%c", value.charAt(0)));
        //
        lockScreen();
        bigNumber.setString(newValue);
        action_hiliteNumber(number);
        unlockScreen();
        //
        playSfxAudio("number_down", true);
        //
        action_lowliteNumber(number);
        if (newValue.length() == 1 && value.equals("0"))
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            playAudioScene("INCORRECT", 0, false);
            bigNumber.setString(currentAnswer);
        }
        else
        {
            char correctValue = correctAnswer.charAt(currentAnswer.length());
            if (value.charAt(0) == correctValue)
            {
                currentAnswer = newValue;
                if (correctAnswer.length() == currentAnswer.length())
                {
                    hideControls("dash");
                    waitForSecs(0.3f);
                    //
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    if (eventAttributes.get("lineup") != null && eventAttributes.get("lineup").equals("true"))
                    {
                        List<OBGroup> groups = (List<OBGroup>) (Object) filterControls("group.*");
                        float averageX = 0.0f;
                        for (OBGroup group : groups)
                        {
                            averageX += group.position().x;
                        }
                        averageX /= groups.size();
                        //
                        List animations = new ArrayList();
                        for (OBGroup group : groups)
                        {
                            OBAnim moveAnim = OBAnim.moveAnim(new PointF(averageX, group.position().y), group);
                            animations.add(moveAnim);
                        }
                        playSfxAudio("lineup", false);
                        OBAnimationGroup.runAnims(animations, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                        waitForSecs(0.3f);
                    }
                    playAudioQueuedScene("CORRECT", 0.3f, true);
                    waitForSecs(0.3f);
                    //
                    playAudioQueuedScene("FINAL", 0.3f, true);
                    //
                    nextScene();
                    //
                    return;
                }
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("INCORRECT2", 0.3f, false);
                //
                bigNumber.setString(currentAnswer);
            }
        }
        revertStatusAndReplayAudio();
    }


    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBLabel number = (OBLabel) findNumber(pt);
            if (number != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkNumber(number);
                    }
                });
            }
        }
    }
}

