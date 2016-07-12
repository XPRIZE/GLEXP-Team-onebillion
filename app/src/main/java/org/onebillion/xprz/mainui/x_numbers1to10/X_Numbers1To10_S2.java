package org.onebillion.xprz.mainui.x_numbers1to10;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_CompleteSequence;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class X_Numbers1To10_S2 extends XPRZ_Generic_CompleteSequence
{

    @Override
    public String action_getObjectPrefix ()
    {
        return "obj";
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        for (OBControl number : filterControls("obj.*"))
        {
            OBLabel label = action_createLabelForControl(number);
            label.setProperty("originalPosition", XPRZ_Generic.copyPoint(label.position()));
            XPRZ_Generic.sendObjectToTop(label, this);
            label.setProperty("number", number.attributes().get("number"));
            //
            String locked = (String) number.attributes().get("locked");
            //
            if (locked == null || locked.equals("yes") || locked.equals("true"))
            {
                label.disable();
            }
            else
            {
                label.enable();
            }
            objectDict.put((String) number.attributes().get("id"), label);
            detachControl(number);
        }
    }

    public Boolean action_isPlacementCorrect(OBControl dragged, OBControl container)
    {
        return container.attributes().get("number").equals(dragged.propertyValue("number"));
    }


    public void action_finalAnimation() throws Exception
    {
        if (currentEvent().equals("2n"))
        {
            for (int i = 1; i <= 10; i++)
            {
                for (OBControl number : filterControls("obj.*"))
                {
                    if (Integer.parseInt((String)number.propertyValue("number")) == i)
                    {
                        playSceneAudioIndex("FINAL", i-1, false);
                        float scale = number.scale();
                        OBAnim anim1 = OBAnim.scaleAnim(1.75f * scale, number);
                        OBAnim anim2 = OBAnim.scaleAnim(scale, number);
                        OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(anim1),Arrays.asList(anim2)), Arrays.asList(0.15f,0.15f), true, Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT, OBAnim.ANIM_EASE_IN_EASE_OUT), 1, this);
                        waitAudio();
                        waitForSecs(0.1);
                    }
                }
            }
            playSceneAudioIndex("FINAL", 10, true);
        }
        else
        {
            playSceneAudio("FINAL", true);
        }
    }


    public void demo2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.8f, 0.8f, -5f, 0.6f, true, this);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Two goes here
        OBControl number = objectDict.get("obj_10");
        XPRZ_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3);
        //
        OBControl place = objectDict.get("place_1");
        XPRZ_Generic.pointer_moveToPointWithObject(number, place.position(), -25, 0.6f, true, this);
        waitForSecs(0.3);
        //
        playSfxAudio("correct", false);
        hideControls("dash_1");
        XPRZ_Generic.pointer_moveToObject(number, -25, 0.3f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }

}