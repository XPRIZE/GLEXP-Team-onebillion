package com.maq.xprize.onecourse.hindi.mainui.oc_counting7to10;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_AddRemoveObjectsToScene;

import java.util.EnumSet;

/**
 * Created by pedroloureiro on 08/09/16.
 */
public class OC_Counting7To10_S1 extends OC_Generic_AddRemoveObjectsToScene
{

    public OC_Counting7To10_S1 ()
    {
        super(true, false);
    }

    public String getSFX_placeObject()
    {
        return "place_object";
    }

    public void demo1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        demoButtons();
        waitForSecs(0.3);
        //
        OBControl frog = objectDict.get("container");
        OC_Generic.pointer_moveToObject(frog, -25, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), false, this);
        action_playNextDemoSentence(true); //  Now let’s put spots on the frog.
        waitForSecs(0.3);
        //
        for (int i = 1; i < 11; i++)
        {
            OBControl dot = objectDict.get(String.format("obj_%d", i));
            OC_Generic.pointer_moveToObject(dot, -25+OC_Generic.randomInt(0, 10), 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            playSfxAudio("place_object", false);
            dot.show();
            waitForSecs(0.1);
            action_playNextDemoSentence(true); //  One. Two. Three. Four. Five. Six. Seven. Eight. Nine. Ten.
        }
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToObject(frog, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Ten Spots.
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }




}
