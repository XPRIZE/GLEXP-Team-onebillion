package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by pedroloureiro on 17/06/16.
 */
public class X_CountingTo3_S1 extends XPRZ_Generic_Event
{
    public X_CountingTo3_S1()
    {
        super();
    }


    @Override
    public String action_getObjectPrefix()
    {
        return "platform";
    }


    public void demo1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        demoButtons();
        waitForSecs(0.7);
        //
        action_playNextDemoSentence(false); // Now Look
        movePointerToPoint(objectDict.get("platform_1").position(), -10, 0.6f, true);
        waitAudio();
        //
        placeObjectWithSFX("frog_1_1");
        action_playNextDemoSentence(true); // One frog on a rock
        waitForSecs(0.2);
        //
        movePointerToPoint(objectDict.get("platform_2").position(), -10, 0.6f, true);
        placeObjectWithSFX("frog_2_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("frog_2_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Two frogs on a rock
        waitForSecs(0.2);
        //
        movePointerToPoint(objectDict.get("platform_3").position(), -10, 0.6f, true);
        placeObjectWithSFX("frog_3_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("frog_3_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        placeObjectWithSFX("frog_3_3");
        action_playNextDemoSentence(true); // Three
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Three frogs on a rock
        waitForSecs(0.2);
        //
        thePointer.hide();
        nextScene();
    }


    public void demo1e() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        //
        placeObjectWithSFX("bird_1_1");
        action_playNextDemoSentence(true); // One bird on a branch
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_2_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_2_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Two birds on a branch
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_3");
        action_playNextDemoSentence(true); // Three
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Three birds on a branch
        waitForSecs(0.2);
        //
        nextScene();
    }

    public void demo1j() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        //
        placeObjectWithSFX("ladybug_1_1");
        action_playNextDemoSentence(true); // One ladybug on a leaf
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_2_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_2_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Two ladybugs on a leaf
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_3");
        action_playNextDemoSentence(true); // Three
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Three ladybug on a leaf
        waitForSecs(0.2);
        //
        nextScene();
    }


    public void placeObjectWithSFX(String object) throws Exception
    {
        playSfxAudio("placeObject", false);
        objectDict.get(object).show();
        waitSFX();
    }





    public void action_highlight(OBControl control) throws Exception
    {
        lockScreen();
        String controlName = (String) control.attributes().get("id");
        String platformNumber = controlName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        for(OBControl item : controls)
        {
            item.highlight();
        }
        control.highlight();
        unlockScreen();
    }


    public void action_lowlight(OBControl control) throws Exception
    {
        lockScreen();
        String controlName = (String) control.attributes().get("id");
        String platformNumber = controlName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        for(OBControl item : controls)
        {
            item.lowlight();
        }
        control.lowlight();
        unlockScreen();
    }

    public OBControl action_getCorrectAnswer()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }


    public void checkTarget(OBControl targ) {
        setStatus(STATUS_CHECKING);
        try {
            action_highlight(targ);
            //
            if (targ.equals(action_getCorrectAnswer())) {
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "CORRECT", false);
                action_animatePlatform(targ, true);
                waitAudio();
                //
                playAudioQueuedScene(currentEvent(), "FINAL", true);
                //
                nextScene();
            } else {
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                //
                action_lowlight(targ);
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        } catch (Exception exception) {
        }
    }


    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
                                             @Override
                                             public void run() throws Exception {
                                                 checkTarget(c);
                                             }
                                         }
                );
            }
        }

    }
}
