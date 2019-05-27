package com.maq.xprize.onecourse.mainui.oc_addtakeaway;


import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_AddRemoveObjects_SelectCorrectNumber;

import java.util.EnumSet;
import java.util.List;


/**
 * Created by pedroloureiro on 14/03/2017.
 */

public class OC_AddTakeAway_S5 extends OC_Generic_AddRemoveObjects_SelectCorrectNumber
{

    public OC_AddTakeAway_S5()
    {
        super(false);
    }


    public void demo5a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        OC_Generic.pointer_moveToObjectByName("obj_3", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // THREE apples.
        //
        action_playNextDemoSentence(false); // Take away ONE apple.
        OC_Generic.pointer_moveToObjectByName("obj_3", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_hideObject(objectDict.get("obj_3"));
        //
        action_playNextDemoSentence(false); // That leaves TWO apples.
        OC_Generic.pointer_moveToObjectByName("obj_2", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Touch two.
        OC_Generic.pointer_moveToObjectByName("number_2", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_highlightNumber(numbers.get(2));
        phase = 2;
        action_showEquationForPhase();
        //
        action_playNextDemoSentence(false); // Look how we show it.
        OC_Generic.pointer_moveToObject(equation.get(0), -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE, OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // This sign means TAKE AWAY.
        OC_Generic.pointer_moveToObject(equation.get(1), -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE, OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 0; i < 5; i++)
        {
            action_playNextDemoSentence(false); // Three … //  … take away  … // … one… // … equals … // … two.
            OC_Generic.pointer_moveToObject(equation.get(i), -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE, OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            waitAudio();
            waitForSecs(0.3);
        }
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }

}
