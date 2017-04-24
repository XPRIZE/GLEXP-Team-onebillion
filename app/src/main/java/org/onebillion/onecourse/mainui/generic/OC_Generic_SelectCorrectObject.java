package org.onebillion.onecourse.mainui.generic;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.utils.OBUtils;

/**
 * OC_Generic_SelectCorrectObject
 * Generic Events for Units where the Child needs to select(tap) the correct object, highlighting it
 *
 * Created by pedroloureiro on 20/06/16.
 */
public class OC_Generic_SelectCorrectObject extends OC_Generic_Event
{

    public OC_Generic_SelectCorrectObject ()
    {
        super();
    }

    @Override
    public String action_getObjectPrefix ()
    {
        return "platform";
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
    }

    public void action_highlight (OBControl control) throws Exception
    {
        lockScreen();
        control.highlight();
        unlockScreen();
    }


    public void action_lowlight (OBControl control) throws Exception
    {
        lockScreen();
        control.lowlight();
        unlockScreen();
    }


    public Boolean action_isAnswerCorrect(OBControl c)
    {
        return c.equals(action_getCorrectAnswer());
    }


    public OBControl action_getCorrectAnswer ()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }


    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        playAudioQueuedScene(currentEvent(), "CORRECT", true);
        //
        action_lowlight(target);
        //
        playAudioQueuedScene(currentEvent(), "FINAL", true);
        //
        nextScene();
    }


    public void action_answerIsWrong (OBControl target) throws Exception
    {
        gotItWrongWithSfx();
        waitForSecs(0.3);
        //
        action_lowlight(target);
        playAudioQueuedScene(currentEvent(), "INCORRECT", false);
    }


    public void checkTarget (OBControl targ)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            action_highlight(targ);
            //
            if (action_isAnswerCorrect(targ))
            {
                action_answerIsCorrect(targ);
            }
            else
            {
                action_answerIsWrong(targ);
                //
                revertStatusAndReplayAudio();
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    @Override
    public OBControl findTarget (PointF pt)
    {
        targets = filterControls(String.format("%s.*", action_getObjectPrefix()));
        return super.findTarget(pt);
    }

    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkTarget(c);
                    }
                });
            }
        }
    }


}
