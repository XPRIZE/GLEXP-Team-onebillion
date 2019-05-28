package com.maq.xprize.onecourse.hindi.mainui.oc_patterns;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

/**
 * Created by pedroloureiro on 17/03/2017.
 */

public class OC_Patterns_S5j extends OC_Generic_SelectCorrectObject
{


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "obj";
    }

    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        playSceneAudio("CORRECT", true);
        waitForSecs(0.3);
        //
        playSceneAudio("FINAL", true);
        //
        nextScene();
    }


    @Override
    public void action_answerIsWrong (final OBControl target) throws Exception
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                action_lowlight(target);
                //
                playSceneAudio("INCORRECT", false);
            }
        });
    }

}
