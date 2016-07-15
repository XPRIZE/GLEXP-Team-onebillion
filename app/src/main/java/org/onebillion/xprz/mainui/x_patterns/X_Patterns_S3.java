package org.onebillion.xprz.mainui.x_patterns;


import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_CompleteSequence;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_DragObjectsToCorrectPlace;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class X_Patterns_S3 extends XPRZ_Generic_CompleteSequence
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
        XPRZ_Generic.pointer_moveToObjectByName("dash_1", -10, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // That's it!
        OBControl control = objectDict.get("obj_9");
        XPRZ_Generic.pointer_moveToObject(control, -10, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3);
        //
        OBControl place = objectDict.get("place_1");
        OBControl dash = objectDict.get("dash_1");
        XPRZ_Generic.pointer_moveToPointWithObject(control, place.position(), -10, 0.6f, true, this);
        waitForSecs(0.3);
        //
        playSfxAudio("correct", false);
        dash.hide();
        XPRZ_Generic.pointer_moveToObject(control, -10, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        List<String> sequence = Arrays.asList("obj_1","obj_2","obj_3","obj_9","obj_4","obj_5","obj_6","obj_7");
        for (int i = 0; i < sequence.size(); i++)
        {
            XPRZ_Generic.pointer_moveToObjectByName(sequence.get(i), -25 + 5 * i , (i == 0 ? 0.6f : 0.2f), EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            action_playNextDemoSentence(true); // Shoe. Sock. Shoe. Sock. Shoe. Sock. Shoe. Sock. Shoe. Sock.
        }
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }

}
