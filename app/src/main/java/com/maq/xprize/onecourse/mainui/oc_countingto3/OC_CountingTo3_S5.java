package com.maq.xprize.onecourse.mainui.oc_countingto3;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_SelectCorrectObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 11/07/16.
 */
public class OC_CountingTo3_S5 extends OC_Generic_SelectCorrectObject
{

    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        if (audioSceneExists("CORRECT"))
        {
            playSceneAudio("CORRECT", true);
            waitForSecs(0.7);
        }
        //
        if (audioSceneExists("FINAL"))
        {
            playSceneAudio("FINAL", true);
            waitForSecs(0.7);
        }
        //
        nextScene();
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        if (redraw)
        {
            for (OBControl number : filterControls("number.*"))
            {
                action_createLabelForControl(number, 1.2f);
            }
        }
        action_selectNumber(null);
    }


    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        lockScreen();
        OBGroup group = (OBGroup) control;
        List<OBControl> controls = (List<OBControl>) new ArrayList(group.objectDict.values());
        for (OBControl c : controls)
        {
            if (OBControl.class.isInstance(c))
            {
                action_selectNumber(group);
            }
        }
        unlockScreen();
    }

    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        lockScreen();
        OBGroup group = (OBGroup) control;
        List<OBControl> controls = (List<OBControl>) new ArrayList(group.objectDict.values());
        for (OBControl c : controls)
        {
            if (OBControl.class.isInstance(c))
            {
                action_selectNumber(null);
            }
        }
        unlockScreen();
    }


    public void action_selectNumber(OBGroup selectedNumber)
    {
        List<OBGroup> numbers = (List<OBGroup>) (Object) filterControls("number_.*");
        for (OBGroup number : numbers)
        {
            number.objectDict.get("selected").setHidden(!number.equals(selectedNumber));
        }
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }
}
