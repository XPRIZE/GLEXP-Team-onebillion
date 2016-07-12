package org.onebillion.xprz.mainui.x_counting4to6;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;

import java.util.List;

/**
 * Created by pedroloureiro on 21/06/16.
 */
public class X_Counting4To6_S5 extends XPRZ_Generic_SelectCorrectObject
{
    public X_Counting4To6_S5()
    {
        super();
    }

    @Override
    public String action_getObjectPrefix()
    {
        return "number";
    }


    @Override
    public void action_prepareScene(String scene, Boolean redraw)
    {
        if (redraw)
        {
            for (OBControl number : filterControls("number.*"))
            {
                OBLabel label = action_createLabelForControl(number, 1.2f);
            }
        }
    }


    @Override
    public void action_highlight(OBControl control) throws Exception
    {
        lockScreen();
        control.highlight();
        unlockScreen();
    }


    @Override
    public void action_lowlight(OBControl control) throws Exception
    {
        lockScreen();
        control.lowlight();
        unlockScreen();
    }


    @Override
    public void action_answerIsCorrect(OBControl target) throws Exception
    {
        playAudioQueuedScene(currentEvent(), "CORRECT", false);
        action_animatePlatform(target, true);
        waitAudio();
        //
        nextScene();
    }
}
