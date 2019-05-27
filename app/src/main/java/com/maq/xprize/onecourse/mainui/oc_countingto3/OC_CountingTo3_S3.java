package com.maq.xprize.onecourse.mainui.oc_countingto3;


import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_DragMultipleObjectsToSameContainer;

import java.util.EnumSet;

/**
 * Created by pedroloureiro on 18/01/2017.
 */

public class OC_CountingTo3_S3 extends OC_Generic_DragMultipleObjectsToSameContainer
{

    public void demo3a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.8f, -15, 0.6f, false, this);
        action_playNextDemoSentence(false); // Look.
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); //  One thing goes into the green box.
        //
        OBControl object = objectDict.get("obj_1");
        OBControl container = objectDict.get("box_1");
        OC_Generic.pointer_moveToObject(object, -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        OC_Generic.pointer_moveToPointWithObject(object, container.getWorldPosition(), -10, 0.9f, true, this);
        OC_Generic.pointer_moveToObject(object, -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        action_moveObjectToOriginalPosition(object, true);
        waitForSecs(0.4);
        //
        nextScene();
    }
}
