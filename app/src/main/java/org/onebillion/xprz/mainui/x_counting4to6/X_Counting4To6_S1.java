package org.onebillion.xprz.mainui.x_counting4to6;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBUtils;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 11/07/16.
 */
public class X_Counting4To6_S1 extends XPRZ_Generic_SelectCorrectObject
{
    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        if (redraw)
        {
            List<OBGroup> controls = (List<OBGroup>) (Object) filterControls("obj.*");
            for (OBGroup control : controls)
            {
                for (String attribute : control.attributes().keySet())
                {
                    if (attribute.contains("colour_"))
                    {
                        String layer = attribute.replace("colour_", "");
                        control.substituteFillForAllMembers(String.format("%s.*", layer), OBUtils.colorFromRGBString((String)control.attributes().get(attribute)));
                    }
                }
            }
        }
    }

    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        action_lowlight(target);
        waitForSecs(0.3);
        int correctAnswer = Integer.parseInt(eventAttributes.get("correctAnswer"));
        //
        int i;
        for (i = 1; i <= correctAnswer; i++)
        {
            OBControl control = objectDict.get(String.format("obj_%d_%d", correctAnswer, i));
            control.highlight();
            playSceneAudioIndex("CORRECT", i-1, true);
            waitForSecs(0.3);
            control.lowlight();
        }
        //
        playSceneAudioIndex("CORRECT", i-1, true);
        waitForSecs(0.7);
        //
        playSceneAudio("FINAL", true);
    }


    public void demo1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        demoButtons();
        //
        action_playNextDemoSentence(false); // Now look;
        XPRZ_Generic.pointer_moveToObjectByName("obj_4_1", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 4; i++)
        {
            action_playNextDemoSentence(true); // One. Two. Three. Four.
            OBControl control = objectDict.get(String.format("obj_4_%d", i+1));
            if (control != null)
            {
                XPRZ_Generic.pointer_moveToObject(control, -25 + i * 2, 0.3f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            }
        }
        //
        XPRZ_Generic.pointer_nudge(0.075f, 0.0f, -5f, 0.3f, false, this);
        action_playNextDemoSentence(true); // Four Blue beads on a wire
        //
        XPRZ_Generic.pointer_moveToObjectByName("obj_5_1", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 5; i++)
        {
            action_playNextDemoSentence(true); // One. Two. Three. Four. Five.
            OBControl control = objectDict.get(String.format("obj_5_%d", i+1));
            if (control != null)
            {
                XPRZ_Generic.pointer_moveToObject(control, -25 + i * 2, 0.3f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            }
        }
        //
        XPRZ_Generic.pointer_nudge(0.075f, 0.0f, -5f, 0.3f, false, this);
        action_playNextDemoSentence(true); // Five Yellow beads on a wire
        //
        XPRZ_Generic.pointer_moveToObjectByName("obj_6_1", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 6; i++)
        {
            action_playNextDemoSentence(true); // One. Two. Three. Four. Five. Six.
            OBControl control = objectDict.get(String.format("obj_6_%d", i+1));
            if (control != null)
            {
                XPRZ_Generic.pointer_moveToObject(control, -25 + i * 2, 0.3f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            }
        }
        //
        XPRZ_Generic.pointer_nudge(0.075f, 0.0f, -5f, 0.3f, false, this);
        action_playNextDemoSentence(true); // Six pink beads on a wire
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public String action_getObjectPrefix()
    {
        return "container";
    }

    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        lockScreen();
        String[] components = ((String) control.attributes().get("id")).split("_");
        int objectNumber = Integer.parseInt(components[components.length - 1]);
        List<OBControl> controls = filterControls(String.format("obj_%d.*", objectNumber));
        for (OBControl c : controls)
        {
            c.highlight();
        }
        control.highlight();
        unlockScreen();
    }

    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        lockScreen();
        String[] components = ((String) control.attributes().get("id")).split("_");
        int objectNumber = Integer.parseInt(components[components.length - 1]);
        List<OBControl> controls = filterControls(String.format("obj_%d.*", objectNumber));
        for (OBControl c : controls)
        {
            c.lowlight();
        }
        control.lowlight();
        unlockScreen();
    }

}
