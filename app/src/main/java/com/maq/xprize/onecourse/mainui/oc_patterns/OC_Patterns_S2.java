package com.maq.xprize.onecourse.mainui.oc_patterns;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_ColourObjectsFollowingPattern;

import java.util.EnumSet;

/**
 * Created by pedroloureiro on 18/01/2017.
 */

public class OC_Patterns_S2 extends OC_Generic_ColourObjectsFollowingPattern
{

    public void demo2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Let's copy the pattern of colours.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.5f, -15, 0.9f, false, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Like this.
        OBControl paintpot = objectDict.get("paint_2");
        OC_Generic.pointer_moveToObject(paintpot, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_playSelectPaintpotSoundEffect(false);
        action_selectPaintPoint((OBGroup)paintpot);
        //
        OBControl object = objectDict.get("obj_11");
        OC_Generic.pointer_moveToObject(object, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_playColourObjectSoundEffect(false);
        action_colourObjectWithSelectedColour(object);
        OC_Generic.pointer_moveToObject(object, -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3);
        //
        paintpot = objectDict.get("paint_3");
        OC_Generic.pointer_moveToObject(paintpot, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_playSelectPaintpotSoundEffect(false);
        action_selectPaintPoint((OBGroup)paintpot);
        //
        object = objectDict.get("obj_12");
        OC_Generic.pointer_moveToObject(object, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_playColourObjectSoundEffect(false);
        action_colourObjectWithSelectedColour(object);
        OC_Generic.pointer_moveToObject(object, -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3);
        //
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }
}
