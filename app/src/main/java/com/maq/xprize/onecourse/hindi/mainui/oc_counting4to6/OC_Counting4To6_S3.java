package com.maq.xprize.onecourse.hindi.mainui.oc_counting4to6;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 11/07/16.
 */
public class OC_Counting4To6_S3 extends OC_Generic_SelectCorrectObject
{
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
        List<OBGroup> controls = (List<OBGroup>) (Object) filterControls("obj.*");
        for (OBGroup control : controls)
        {
            String number = ((String) control.attributes().get("id")).replace("obj_", "");
            String layerName = String.format("set_%s_%s", currentEvent(), number);
            OBGroup layer = (OBGroup) control.objectDict.get(layerName);
            if (layer != null) layer.show();
            //
            String strokeColour = (String) control.attributes().get("colour_stroke");
            if (strokeColour != null)
            {
                control.substituteStrokeForAllMembers(".*", OBUtils.colorFromRGBString(strokeColour));
            }
        }
    }

    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        lockScreen();
        control.highlight();
        for (OBControl c : filterControls(String.format("aux_%s.*", (String) control.attributes().get("id"))))
        {
            c.highlight();
        }
        unlockScreen();
    }


    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        lockScreen();
        control.lowlight();
        for (OBControl c : filterControls(String.format("aux_%s.*", (String) control.attributes().get("id"))))
        {
            c.lowlight();
        }
        unlockScreen();
    }


    public void demo3a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        OC_Generic.pointer_moveToObjectByName("obj_3", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // This card has six spots.
        OC_Generic.pointer_moveToObjectByName("obj_3", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        OBControl control = objectDict.get("obj_3");
        playSfxAudio("correct", false);
        control.highlight();
        waitSFX();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }
}