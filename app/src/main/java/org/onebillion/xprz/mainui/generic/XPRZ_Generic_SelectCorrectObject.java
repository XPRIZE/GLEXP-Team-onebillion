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

    public XPRZ_Generic_SelectCorrectObject()
    {
        super();
    }


    @Override
    public String action_getObjectPrefix()
    {
        return "platform";
    }



    public void action_highlight(OBControl control) throws Exception
    {
        lockScreen();
        control.highlight();
        unlockScreen();
    }



    public void action_lowlight(OBControl control) throws Exception
    {
        lockScreen();
        control.lowlight();
        unlockScreen();
    }


    public OBControl action_getCorrectAnswer()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }


    public void action_answerIsCorrect(OBControl target) throws Exception
    {
        playAudioQueuedScene(currentEvent(), "CORRECT", true);
    }



    public void action_answerIsWrong() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "INCORRECT", false);
    }


    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            action_highlight(targ);
            //
            if (targ.equals(action_getCorrectAnswer()))
            {
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                action_answerIsCorrect(targ);
                //
                action_lowlight(targ);
                //
                playAudioQueuedScene(currentEvent(), "FINAL", true);
                //
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                action_answerIsWrong();
                //
                action_lowlight(targ);
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        } catch (Exception exception) {
        }
    }


    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget(c);
                    }
                });
            }
        }
    }

}
