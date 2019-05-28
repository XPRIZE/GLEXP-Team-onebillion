package com.maq.xprize.onecourse.hindi.mainui.oc_numbers1to10;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.AddPoints;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.NormalisedVector;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.PointDistance;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.ScalarTimesPoint;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.lperp;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.rperp;

/**
 * Created by pedroloureiro on 03/05/2017.
 */

public class OC_Numbers1To10_S1 extends OC_Generic_Event
{
    List<OBLabel> numbers;
    int correctNumber, placedObjects;

    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        if (currentEvent().equals("1a")) correctNumber = 1;
        else correctNumber = 10;
        placedObjects = 0;
        //
        hideControls("place.*");
        hideControls("number.*");
        //
        if (numbers != null)
        {
            for (OBControl number : numbers)
            {
                detachControl(number);
            }
        }
        numbers = action_addLabelsToObjects("number.*", 1.2f, false);
        //
        for (OBLabel numberLabel : numbers)
        {
            numberLabel.hide();
            OBControl place = objectDict.get(String.format("place_%s", numberLabel.text()));
            numberLabel.setPosition(OC_Generic.copyPoint(place.position()));
        }
    }

    public void action_placeNumber (OBLabel control) throws Exception
    {
        gotItRightBigTick(false);
        control.disable();
        OBControl place = objectDict.get(String.format("place_%s", control.text()));
        OBAnim anim = OBAnim.moveAnim(place.getWorldPosition(), control);
        OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }

    public Path action_getPathForNumber (OBLabel number)
    {
        PointF from = OC_Generic.copyPoint(number.position());
        PointF to = (PointF) number.propertyValue("originalPosition");
        float offset = PointDistance(from, to);
        Path path = new Path();
        path.moveTo(from.x, from.y);
        PointF c1 = OB_Maths.tPointAlongLine(0.30f, from, to);
        PointF c2 = OB_Maths.tPointAlongLine(0.70f, from, to);
        PointF lp1 = ScalarTimesPoint(offset / 2, NormalisedVector(lperp(OB_Maths.DiffPoints(to, from))));
        PointF lp2 = ScalarTimesPoint(offset / 4, NormalisedVector(rperp(OB_Maths.DiffPoints(to, from))));
        PointF cp1 = AddPoints(c1, lp1);
        PointF cp2 = AddPoints(c2, lp2);
        path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, to.x, to.y);
        return path;
    }

    public void demo1a () throws Exception
    {
        setStatus(STATUS_BUSY);
        demoButtons();
        thePointer.hide();
        playAudioScene("DEMO", 0, false); // Now let’s get started.;
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 1; i <= 10; i++)
        {
            OBLabel number = numbers.get(i - 1);
            playAudioScene("DEMO", i, false); // One. Two. Three. Four. Five. Six. Seven. Eight. Nine. Ten;
            float scale = number.scale();
            //
            lockScreen();
            number.setScale(1.75f * scale);
            number.show();
            unlockScreen();
            //
            OBAnim anim = OBAnim.scaleAnim(scale, number);
            OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitAudio();
            waitForSecs(0.1f);
        }
        for (int i = 1; i <= 10; i++)
        {
            OBLabel number = numbers.get(i - 1);
            Path path = action_getPathForNumber(number);
            OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
            OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.5, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.15f);
        }
        waitForSecs(0.9f);
        playAudioScene("DEMO", 11, false); // Look. One comes first.;
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToObject(numbers.get(0), -25, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_placeNumber(numbers.get(0));
        playAudioScene("DEMO", 12, false); // One.;
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        correctNumber++;
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }

    public void setScene1b ()
    {
        setSceneXX(currentEvent());
        //
        for (OBLabel control : numbers)
        {
            control.show();
        }
    }


    public void demo1b () throws Exception
    {
        setStatus(STATUS_BUSY);
        for (int i = 1; i <= 10; i++)
        {
            OBLabel number = numbers.get(i - 1);
            playAudioScene("DEMO", i - 1, false); // One. Two. Three. Four. Five. Six. Seven. Eight. Nine. Ten;
            float scale = number.scale();
            //
            lockScreen();
            number.setScale(1.75f * scale);
            number.show();
            unlockScreen();
            //
            OBAnim anim = OBAnim.scaleAnim(scale, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitAudio();
            waitForSecs(0.1f);
        }
        playAudioScene("DEMO", 10, true); // Now let’s count backwards.;
        waitForSecs(0.3f);
        for (int i = 1; i <= 10; i++)
        {
            OBLabel number = numbers.get(10 - i);
            playAudioScene("DEMO", 10 + i, false); // Ten. Nine. Eight. Seven. Six. Five. Four. Three. Two. One.;
            float scale = number.scale();
            //
            lockScreen();
            number.setScale(1.75f * scale);
            number.show();
            unlockScreen();
            //
            OBAnim anim = OBAnim.scaleAnim(scale, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitAudio();
            waitForSecs(0.1f);
        }
        for (int i = 1; i <= 10; i++)
        {
            OBLabel number = numbers.get(i - 1);
            Path path = action_getPathForNumber(number);
            OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.5, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.15f);
        }
        waitForSecs(0.9f);
        playAudioScene("DEMO", 21, false); // Look.  Start with ten.;
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToObject(numbers.get(9), -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_placeNumber(numbers.get(9));
        playAudioScene("DEMO", 22, false); // Ten.;
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        correctNumber--;
        setStatus(STATUS_AWAITING_CLICK);
        List replayAudio = Arrays.asList(getAudioForScene(currentEvent(), "REPEAT").get(0));
        setReplayAudio(replayAudio);
        //
        List audio = Arrays.asList(getAudioForScene(currentEvent(), "PROMPT").get(0));
        playAudioQueued(audio, false);
    }

    public void checkNumber (OBLabel target) throws Exception
    {
        int number = Integer.parseInt(target.text());
        placedObjects = 0;
        for (OBControl control : numbers)
        {
            if (!control.isEnabled()) placedObjects++;
        }
        if (correctNumber == number)
        {
            action_placeNumber(target);
            placedObjects++;
            String correctAudio;
            if (currentEvent().equals("1a"))
            {
                correctNumber++;
                correctAudio = getAudioForScene(currentEvent(), "CORRECT").get(placedObjects - 2);
            }
            else
            {
                correctNumber--;
                correctAudio = getAudioForScene(currentEvent(), "CORRECT").get(placedObjects - 2);
            }
            if (placedObjects >= numbers.size())
            {
                setReplayAudio(null);
                //
                playAudio(correctAudio);
                waitAudio();
                waitForSecs(0.3f);
                //
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                if (currentEvent().equals("1b"))
                {
                    for (int i = 1; i <= 10; i++)
                    {
                        OBLabel numberLabel = numbers.get(10 - i);
                        playAudioScene("FINAL", i - 1, false); // Ten. Nine. Eight. Seven. Six. Five. Four. Three. Two. One.;
                        float scale = numberLabel.scale();
                        //
                        lockScreen();
                        numberLabel.setScale(1.75f * scale);
                        numberLabel.show();
                        unlockScreen();
                        //
                        OBAnim anim = OBAnim.scaleAnim(scale, numberLabel);
                        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                        waitAudio();
                        waitForSecs(0.1f);
                    }
                    playAudioScene("FINAL", 10, true); // WELL DONE! You put one to ten, and ten to one, : the right order.;
                }
                nextScene();
                return;
            }
            else
            {
                if (currentEvent().equals("1a"))
                {
                    List replayAudio = Arrays.asList(getAudioForScene(currentEvent(), "REPEAT2").get(placedObjects - 2));
                    setReplayAudio(replayAudio);
                    List audio = Arrays.asList(correctAudio, getAudioForScene(currentEvent(), "PROMPT2").get(placedObjects - 2));
                    playAudioQueued(audio, false);
                }
                else
                {
                    List replayAudio = Arrays.asList(getAudioForScene(currentEvent(), "REPEAT").get(placedObjects - 1));
                    setReplayAudio(replayAudio);
                    List audio = Arrays.asList(correctAudio, getAudioForScene(currentEvent(), "PROMPT").get(placedObjects - 1));
                    playAudioQueued(audio, false);
                }
            }
        }
        else
        {
            gotItWrongWithSfx();
            if (currentEvent().equals("1a"))
            {
                playAudioScene("INCORRECT", placedObjects - 1, false);
            }
            else
            {
                playAudioScene("INCORRECT", placedObjects - 1, false);
            }
        }
    }

    public OBControl findNumber (PointF pt)
    {
        return finger(0, 2, (List<OBControl>) (Object) numbers, pt, true);

    }

    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBLabel obj = (OBLabel) findNumber(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkNumber(obj);
                    }
                });
            }
        }
    }


}
