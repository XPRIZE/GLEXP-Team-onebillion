package com.maq.xprize.onecourse.hindi.mainui.oc_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 15/03/2017.
 */

public class OC_Counting5and10_S2i extends OC_Generic_Event
{
    List<OBLabel> numberLabels;

    int correctNumber;
    boolean allNumbersPlaced;
    OBGroup alignmentGroup;


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes3";
    }

    @Override
    public void setSceneXX (String scene)
    {
        MainActivity.log("OC_Counting5and10_S2i:setSceneXX");
        //
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
            alignmentGroup.setPosition(new PointF(bounds().width() / 2, bounds().height() / 3));
            attachControl(alignmentGroup);
            alignmentGroup.show();
            //
            if (numberLabels == null)
            {
                numberLabels = new ArrayList();
                //
                for (OBControl number : sortedFilteredControls("number.*"))
                {
                    OBLabel label = action_createLabelForControl(number, 0.8f, false);
                    label.setPosition(number.getWorldPosition());
                    attachControl(label);
                    numberLabels.add(label);
                    //
                    OC_Generic.sendObjectToTop(label, this);
                    //
                    label.disable();
                    label.show();
                    number.show();
                }
            }
        }
        correctNumber = 10;
        allNumbersPlaced = false;
        //
        hideControls("block.*");
    }


    public void demo2i() throws Exception
    {
        try
        {
            setStatus(STATUS_DOING_DEMO);
            //
            loadPointer(POINTER_MIDDLE);
            //
            action_playNextDemoSentence(false); // Now you have to count BACKWARDS.
            OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.5f, -5f, 0.9f, true, this);
            waitAudio();
            waitForSecs(0.3);
            //
            action_playNextDemoSentence(false); // So … start with ONE HUNDRED!
            OC_Generic.pointer_moveToObject(numberLabels.get(9), 10f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            waitAudio();
            waitForSecs(0.3);
            //
            action_playNextDemoSentence(false); // But first, touch this red button.
            OC_Generic.pointer_moveToObjectByName("button", -20f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
            waitAudio();
            waitForSecs(0.3);
            //
            thePointer.hide();
            waitForSecs(0.3);
            //
            setStatus(STATUS_AWAITING_CLICK);
            //
            doAudio(currentEvent());
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Counting5and10_S2i:exception caught");
            e.printStackTrace();
        }
    }

    public void finDemo2i() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        for (int i = 0; i < 10; i++)
        {
            playSceneAudioIndex("DEMO2", i, false); // // ONE HUNDRED. NINETY. EIGHTY. SEVENTY. SIXTY. FIFTY. FORTY. THIRTY. TWENTY. TEN
            //
            OBLabel label = numberLabels.get(9-i);
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
        playSceneAudio("FINAL", true);
    }


    public void action_checkCollision(OBControl particle1, OBControl particle2)
    {
        if (particle1.equals(particle2)) return;
        if (!particle1.isEnabled()) return;
        if (!particle2.isEnabled()) return;
        //
        PointF position1 = OC_Generic.copyPoint((PointF) particle1.propertyValue("newPosition"));
        PointF position2 = OC_Generic.copyPoint((PointF) particle2.propertyValue("newPosition"));
        float collisionDistance = particle1.width() * 1.1f;
        float distance = OB_Maths.PointDistance(position1, position2);
        //
        if (distance < collisionDistance)
        {
            PointF collision = OB_Maths.NormalisedVector(OB_Maths.DiffPoints(position1, position2));
            PointF direction1 = OC_Generic.copyPoint(collision);
            PointF direction2 = new PointF(- collision.x, - collision.y);
            particle1.setProperty("direction", OC_Generic.copyPoint(direction1));
            particle2.setProperty("direction", OC_Generic.copyPoint(direction2));
            //
            while (distance < collisionDistance)
            {
                position1 = OB_Maths.AddPoints(OC_Generic.copyPoint(position1), direction1);
                position2 = OB_Maths.AddPoints(OC_Generic.copyPoint(position2), direction2);
                distance = OB_Maths.PointDistance(position1, position2);
            }
            //
            particle1.setProperty("newPosition", OC_Generic.copyPoint(position1));
            particle2.setProperty("newPosition", OC_Generic.copyPoint(position2));
        }
    }

    public void action_moveNumbers() throws Exception
    {
        try
        {
            float speed = applyGraphicScale(4.5f);
            float minX = 0.05f * bounds().width();
            float maxX = 0.95f * bounds().width();
            float minY = 0.05f * bounds().height();
            float maxY = 0.95f * bounds().height();
            double timePerRound = 0.01;
            //
            for (OBLabel number : numberLabels)
            {
                number.enable();
                PointF direction = new PointF(OB_Maths.randomInt(1, 2), OB_Maths.randomInt(3, 4));
                if (OB_Maths.randomInt(1, 2) == 1) direction.x = -direction.x;
                if (OB_Maths.randomInt(1, 2) == 1) direction.y = -direction.y;
                direction = OB_Maths.NormalisedVector(direction);
                //
                number.setProperty("direction", OC_Generic.copyPoint(direction));
                number.setProperty("newPosition", OC_Generic.copyPoint(number.position()));
                number.setShouldTexturise(true);
            }
            //
            while (!allNumbersPlaced)
            {
                for (OBControl number : numberLabels)
                {
                    if (!number.isEnabled()) continue;
                    //
                    PointF newPosition = OC_Generic.copyPoint(number.position());
                    PointF direction = (PointF) number.propertyValue("direction");
                    //
                    for (OBControl possibleCollision : numberLabels)
                    {
                        action_checkCollision(number, possibleCollision);
                    }
                    //
                    if (newPosition.x < minX || newPosition.x > maxX)
                    {
                        direction.x = -direction.x;
                        while (newPosition.x < minX || newPosition.x > maxX)
                        {
                            newPosition = OB_Maths.AddPoints(OC_Generic.copyPoint(newPosition), direction);
                        }
                        number.setProperty("direction", OC_Generic.copyPoint(direction));
                    }
                    //
                    if (newPosition.y < minY || newPosition.y > maxY)
                    {
                        direction.y = -direction.y;
                        while (newPosition.y < minY || newPosition.y > maxY)
                        {
                            newPosition = OB_Maths.AddPoints(OC_Generic.copyPoint(newPosition), direction);
                        }
                        number.setProperty("direction", OC_Generic.copyPoint(direction));
                    }
                    //
                    for (OBControl block : filterControls("block.*"))
                    {
                        float threshold = block.width() / 2 + number.width() / 2;
                        float distance = OB_Maths.PointDistance(block.position(), newPosition);
                        if (distance < threshold)
                        {
                            direction = OB_Maths.NormalisedVector(OB_Maths.DiffPoints(newPosition, block.position()));
                            //
                            while (distance < threshold)
                            {
                                newPosition = OB_Maths.AddPoints(OC_Generic.copyPoint(newPosition), direction);
                                distance = OB_Maths.PointDistance(block.position(), newPosition);
                            }
                            number.setProperty("direction", OC_Generic.copyPoint(direction));
                        }
                    }
                    //
                    number.setProperty("newPosition", newPosition);
                }
                //
                lockScreen();
                //
                double startTime = OC_Generic.currentTime();
                //
                for (OBLabel number : numberLabels)
                {
                    if (!number.isEnabled()) continue;
                    //
                    PointF direction = OC_Generic.copyPoint((PointF) number.propertyValue("direction"));
                    PointF position = OC_Generic.copyPoint((PointF) number.propertyValue("newPosition"));
                    //
                    // Prevent from moving just horizontally or vertically
                    if (direction.x == 0 || direction.y == 0)
                    {
                        direction = new PointF(OB_Maths.randomInt(1, 2), OB_Maths.randomInt(3, 4));
                        if (OB_Maths.randomInt(1, 2) == 1) direction.x = -direction.x;
                        if (OB_Maths.randomInt(1, 2) == 1) direction.y = -direction.y;
                        direction = OB_Maths.NormalisedVector(direction);
                        //
                        number.setProperty("direction", OC_Generic.copyPoint(direction));
                    }
                    //
                    PointF newPosition = OB_Maths.AddPoints(position, OB_Maths.ScalarTimesPoint(speed, direction));
                    number.setPosition(OC_Generic.copyPoint(newPosition));
                }
                //
                unlockScreen();
                //
                double elapsedTime = OC_Generic.currentTime() - startTime;
                double remainingTime = timePerRound - elapsedTime;
                if (remainingTime > 0)
                {
//                    MainActivity.log("OC_Counting5and10_S2i:remaining time:" + remainingTime);
                    waitForSecs(remainingTime);
                }
                else
                {
//                    MainActivity.log("OC_Counting5and10_S2i:negative remaining time:elapsed:" + elapsedTime);
                    waitForSecs(0.001);
                }
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Counting5and10_S2i:exception caught");
            e.printStackTrace();
        }
    }



    public void action_button_disable()
    {
        OBControl button = objectDict.get("button");
        button.disable();
        OC_Generic.colourObject(button, OBUtils.colorFromRGBString("171,97,99"));
    }

    public void action_button_enable()
    {
        OBControl button = objectDict.get("button");
        button.enable();
        OC_Generic.colourObject(button, OBUtils.colorFromRGBString("192,34,40"));
    }


    public void action_button_hilite()
    {
        OBControl button = objectDict.get("button");
        OC_Generic.colourObject(button, OBUtils.colorFromRGBString("120,14,20"));
    }


    public void action_hiliteCurrentBox()
    {
        OBControl box = objectDict.get(String.format("number_%d", correctNumber));
        lockScreen();
        for (OBControl number : filterControls("number.*"))
        {
            if (number.equals(box))
            {
                number.setFillColor(OBUtils.colorFromRGBString("189,255,124"));
            }
            else
            {
                number.setFillColor(OBUtils.colorFromRGBString("255,255,255"));
            }
        }
        unlockScreen();
    }


    public OBLabel findNumber(PointF pt)
    {
        return (OBLabel) finger(0, 2, (List<OBControl>) (Object) numberLabels, pt, true);
    }

    public void checkNumber(final OBLabel number) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        OBLabel correctLabel = numberLabels.get(correctNumber-1);
        number.setColour(Color.RED);
        //
        if (correctLabel.equals(number))
        {
            gotItRightBigTick(false);
            //
            number.disable();
            OBControl slot = objectDict.get(String.format("number_%d", correctNumber));
            number.moveToPoint(slot.getWorldPosition(), 0.3f, true);
            number.setColour(Color.BLACK);
            correctNumber--;
            //
            action_hiliteCurrentBox();
            //
            if (correctNumber <= 0)
            {
                allNumbersPlaced = true;
                waitForSecs(0.7);
                //
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                finDemo2i();
                nextScene();
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
        //
        revertStatusAndReplayAudio();
    }



    public OBControl findButton(PointF pt)
    {
        return finger(0, 2, filterControls("button"), pt, true);
    }



    public void checkButton() throws Exception
    {
        action_button_hilite();
        playSfxAudio("button_pressed", true);
        action_button_disable();
        //
        for (OBLabel number : numberLabels) number.enable();
        doAudio(currentEvent());
        //
        action_hiliteCurrentBox();
        action_moveNumbers();
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
            else
            {
                final OBControl button = findButton(pt);
                if (button != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            checkButton();
                        }
                    });
                }
            }
        }
        super.touchDownAtPoint(pt, v);
    }
}
