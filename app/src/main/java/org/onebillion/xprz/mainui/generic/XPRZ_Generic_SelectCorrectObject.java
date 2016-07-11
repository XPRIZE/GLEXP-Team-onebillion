package org.onebillion.xprz.mainui.generic;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.utils.OBUtils;

/**
 * Created by pedroloureiro on 20/06/16.
 */
public class XPRZ_Generic_SelectCorrectObject extends XPRZ_Generic_Event
{

    public XPRZ_Generic_SelectCorrectObject ()
    {
        super();
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "platform";
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
        setStatus(STATUS_CHECKING);
        try
        {
            action_highlight(targ);
            //
            if (action_isAnswerCorrect(targ))
            {
                action_answerIsCorrect(targ);
                //
                nextScene();
            }
            else
            {
                action_answerIsWrong(targ);
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
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
