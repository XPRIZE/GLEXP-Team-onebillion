package com.maq.xprize.onecourse.mainui.oc_patterns;


import android.graphics.PointF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_CompleteSequence;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class OC_Patterns_S3 extends OC_Generic_CompleteSequence
{

    public void demo3a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        PointF destination = new PointF(bounds().width() * 0.6f, bounds().height() * 0.6f);
        movePointerToPoint(destination, -10, 0.6f, true);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Something is missing from the pattern.
        OC_Generic.pointer_moveToObjectByName("dash_1", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // That's it!
        OBControl control = objectDict.get("obj_9");
        OC_Generic.pointer_moveToObject(control, -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3);
        //
        OBControl place = objectDict.get("place_1");
        OBControl dash = objectDict.get("dash_1");
        OC_Generic.pointer_moveToPointWithObject(control, place.position(), -10, 0.6f, true, this);
        waitForSecs(0.3);
        //
        playSfxAudio("correct", false);
        dash.hide();
        OC_Generic.pointer_moveToObject(control, -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        List<String> sequence = Arrays.asList("obj_1","obj_2","obj_3","obj_9","obj_4","obj_5","obj_6","obj_7");
        for (int i = 0; i < sequence.size(); i++)
        {
            OC_Generic.pointer_moveToObjectByName(sequence.get(i), -25 + 5 * i , (i == 0 ? 0.6f : 0.2f), EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            action_playNextDemoSentence(true); // Shoe. Sock. Shoe. Sock. Shoe. Sock. Shoe. Sock. Shoe. Sock.
        }
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }

}
