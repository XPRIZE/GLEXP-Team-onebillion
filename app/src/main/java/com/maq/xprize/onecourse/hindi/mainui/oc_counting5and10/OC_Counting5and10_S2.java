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
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 15/03/2017.
 */

public class OC_Counting5and10_S2 extends OC_Generic_Event
{
    int correctNumber;
    OBGroup alignmentGroup;

    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
        //
        if (eventAttributes.get("redraw") != null && eventAttributes.get("redraw").equals("true"))
        {
            OBControl previous = null;
            List<OBControl> controls = sortedFilteredControls("number.*");
            //
            for (OBControl number : controls)
            {
                if (previous != null) number.setLeft(previous.right() - previous.lineWidth() * 2);
                previous = number;
            }
            //
            alignmentGroup = new OBGroup(controls);
            alignmentGroup.setPosition(new PointF(bounds().width() / 2, bounds().height() / 3));
            attachControl(alignmentGroup);
            alignmentGroup.show();
            //
            int count = 1;
            for (OBControl number : controls)
            {
                OBLabel label = action_createLabelForControl(number);
                label.setPosition(number.getWorldPosition());
                OC_Generic.sendObjectToTop(label, this);
                label.hide();
                number.show();
                //
                objectDict.put(String.format("label_%d", count), label);
                count++;
            }
            //
            hideControls("place.*");
        }
        correctNumber = 1;
    }


    public void demo2a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(false); // Let's count in tens again.
        waitAudio();
        waitForSecs(0.3);
        //
        List<OBControl> labels = sortedFilteredControls("label.*");
        for (int i = 1; i <= 10; i++)
        {
            action_playNextDemoSentence(false); // TEN. TWENTY. THIRTY. FORTY. FIFTY. SIXTY. SEVENTY. EIGHTY. NINETY. ONE HUNDRED.
            //
            OBLabel label = (OBLabel) labels.get(i - 1);
            lockScreen();
            label.setScale(1.5f);
            label.setOpacity(0.0f);
            label.show();
            unlockScreen();
            //
            OBAnim scaleAnim = OBAnim.scaleAnim(1.0f, label);
            OBAnim opacityAnim = OBAnim.opacityAnim(1.0f, label);
            List<OBAnim> animations = new ArrayList();
            animations.add(scaleAnim);
            animations.add(opacityAnim);
            OBAnimationGroup.runAnims(animations, 0.4f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            //
            waitAudio();
            waitForSecs(0.1);
            //
        }
        //
        action_bounceNumbers();
        //
        loadPointer(POINTER_MIDDLE);
        action_playNextDemoSentence(false); // Now look.
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // TEN goes in the first box.
        OBControl object = labels.get(0);
        OC_Generic.pointer_moveToObject(object, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3);
        playSfxAudio("correct", false);
        //
        object.setColourOverlay(Color.RED);
        OBControl place = objectDict.get("number_1");
        OBAnim moveAnim = OBAnim.moveAnim(place.getWorldPosition(), object);
        //
        List<OBAnim> animations = new ArrayList<>();
        animations.add(moveAnim);
        //
        OBAnimationGroup.runAnims(animations, 0.4f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        object.setColourOverlay(Color.BLACK);
        //
        correctNumber++;
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void finDemo2a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        List<OBControl> labels = sortedFilteredControls("label.*");
        for (int i = 0; i < 10; i++)
        {
            playAudioQueuedSceneIndex(currentEvent(), "DEMO2", i, false); // TEN. TWENTY. THIRTY. FORTY. FIFTY. SIXTY. SEVENTY. EIGHTY. NINETY. ONE HUNDRED.
            //
            OBLabel label = (OBLabel) labels.get(i);
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
        nextScene();
    }


    public void fin ()
    {
        goToCard(OC_Counting5and10_S2b.class, "event2");
    }


    public void action_bounceNumbers ()
    {
        List<OBControl> labels = sortedFilteredControls("label.*");
        List<OBControl> placements = sortedFilteredControls("place.*");
        //
        physics_bounceObjectsWithPlacements(labels, placements, "bounce_new");
    }

    public OBLabel findNumber (PointF pt)
    {
        return (OBLabel) finger(0, 2, filterControls("label.*"), pt, true);
    }


    public void checkNumber (final OBLabel number) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        List labels = sortedFilteredControls("label.*");
        OBLabel correctLabel = (OBLabel) labels.get(correctNumber - 1);
        number.setColour(Color.RED);
        //
        if (correctLabel.equals(number))
        {
            gotItRightBigTick(false);
            //
            OBControl slot = objectDict.get(String.format("number_%d", correctNumber));
            OBAnim moveAnim = OBAnim.moveAnim(slot.getWorldPosition(), number);
            //
            List animations = new ArrayList();
            animations.add(moveAnim);
            //
            OBAnimationGroup.runAnims(animations, 0.4f, false, OBAnim.ANIM_EASE_IN_EASE_OUT, new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    number.setColour(Color.BLACK);
                }
            }, this);
            //
            correctNumber++;
            //
            if (correctNumber > 10)
            {
                waitForSecs(0.7);
                //
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                finDemo2a();
                //
                return;
            }
        }
        else
        {
            gotItWrongWithSfx();
            OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    number.setColour(Color.BLACK);
                }
            });
        }
        revertStatusAndReplayAudio();
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
