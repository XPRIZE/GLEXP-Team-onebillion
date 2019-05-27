package com.maq.xprize.onecourse.mainui.oc_counting7to10;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_ColourObjects;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_ColourObjectsWithoutPaintpots;

/**
 * Created by pedroloureiro on 17/01/2017.
 */

public class OC_Counting7To10_S2 extends OC_Generic_ColourObjectsWithoutPaintpots
{

    public void demo2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        OC_Generic.pointer_moveToRelativePointOnScreen(0.8f, 0.8f, -15f, 0.6f, false, this);
        action_playNextDemoSentence(false); // Look.
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // You can touch a square to colour it in.
        OBControl object = objectDict.get("obj_3");
        movePointerToPoint(object.position(), -10, 0.9f, true);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Like this.
        action_refreshSelectedColour();
        action_playColourObjectSoundEffect(false);
        action_colourObjectWithSelectedColour(object);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }
}
