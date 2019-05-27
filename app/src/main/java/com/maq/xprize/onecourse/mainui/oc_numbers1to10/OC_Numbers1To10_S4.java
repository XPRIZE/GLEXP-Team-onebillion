package com.maq.xprize.onecourse.mainui.oc_numbers1to10;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 11/07/16.
 */
public class OC_Numbers1To10_S4 extends OC_Generic_SelectCorrectObject
{
    int phase, totalPhases;
    Map<String, OBControl> numbers;

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
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        phase = 1;
        String phases = eventAttributes.get("phases");
        totalPhases = (phases == null) ? 1 : Integer.parseInt(phases);
        //
        if (redraw)
        {
            numbers = new HashMap();
            for (OBControl number : filterControls("number.*"))
            {
                OBLabel control = action_createLabelForControl(number, 1.2f);
                number.show();
            }
            //
            for (OBControl control : filterControls("position.*"))
            {
                control.hide();
            }
        }
    }


    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(false);
        waitSFX();
        phase++;
        //
        if (phase > totalPhases)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            playSceneAudio("CORRECT", true);
            waitForSecs(0.3);
            //
            if (audioSceneExists("FINAL"))
            {
                playSceneAudio("FINAL", true);
            }
            //
            nextScene();
        }
        else
        {
            action_selectNumber(null);
            //
            List animations = new ArrayList();
            for (OBControl control : filterControls("obj_.*"))
            {
                String objectNumber = ((String) control.attributes().get("id")).split("_")[1];
                OBControl referenceObject = objectDict.get(String.format("position_%s_%d", objectNumber, phase));
                if (referenceObject != null)
                {
                    animations.add(OBAnim.moveAnim(OC_Generic.copyPoint(referenceObject.position()), control));
                }
            }
            if (animations.size() > 0)
            {
                OBAnimationGroup.runAnims(animations, 0.6f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            }
            //
            List audio = getAudioForScene(currentEvent(), String.format("PROMPT%d", phase));
            List replayAudio = getAudioForScene(currentEvent(), String.format("REPEAT%d", phase));
            setReplayAudio(replayAudio);
            playAudioQueued(audio, false);
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }
}
