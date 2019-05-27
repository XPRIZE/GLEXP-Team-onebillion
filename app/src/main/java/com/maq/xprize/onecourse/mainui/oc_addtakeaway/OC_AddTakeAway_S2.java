package com.maq.xprize.onecourse.mainui.oc_addtakeaway;

import android.graphics.PointF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_AddRemoveObjects_SelectCorrectNumber;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 04/05/2017.
 */

public class OC_AddTakeAway_S2 extends OC_Generic_AddRemoveObjects_SelectCorrectNumber
{
    public OC_AddTakeAway_S2 ()
    {
        super(true);
    }


    public void action_answerIsCorrect (OBLabel target) throws Exception
    {
        action_resizeNumber(target, true, true);
        //
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        phase = 2;
        //
        playAudioQueuedScene(currentEvent(), "FINAL", true);
        //
        for (OBLabel label : numbers)
        {
            action_resizeNumber(label, false, false);
        }
        //
        nextScene();
    }


    public void demo2a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look. There are three stars.;
        OC_Generic.pointer_moveToObjectByName("obj_2", -15, 0.8f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OBControl number = numbers.get(2);
        OC_Generic.pointer_moveToObject(number, -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        playAudioScene("DEMO", 1, true); // Three.;
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Add three more stars, on the lines.;
        OC_Generic.pointer_moveToObjectByName("dash_1", -5, 0.8f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 0; i < 3; i++)
        {
            OBControl obj = objectDict.get(String.format("obj_%d", i + 4));
            OBControl dash = objectDict.get(String.format("dash_%d", i + 1));
            OC_Generic.pointer_moveToObject(obj, -5 + i * 5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            playAudioScene("DEMO", i + 3, false); // One. Two. Three.;
            //
            lockScreen();
            obj.show();
            dash.hide();
            playSfxAudio("add_object", false);
            unlockScreen();
            //
            waitAudio();
        }
        playAudioScene("DEMO", 6, false); // How many stars are there now?;
        OC_Generic.pointer_moveToObjectByName("obj_4", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 0; i < 6; i++)
        {
            String objectName = String.format("obj_%d", i + 1);
            float rotation = -10 + i * 5;
            float time = (i == 0 ? 0.6f : 0.3f);
            OC_Generic.pointer_moveToObjectByName(objectName, rotation, time, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            playAudioScene("DEMO", i + 7, true); // One. Two. Three. Four. Five. Six;
        }
        //
        number = numbers.get(5);
        playAudioScene("DEMO", 13, false); // Touch six.;
        OC_Generic.pointer_moveToObject(number, -5, 0.8f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(number, -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("correct", false);
        //
        lockScreen();
        action_highlightObject(number);
        action_resizeNumber((OBLabel) number, true, false);
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 14, false); // Three add three gives six.;
        OC_Generic.pointer_moveToObjectByName("obj_4", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void demo2g () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Now, let’s TAKE AWAY things.;
        OC_Generic.pointer_moveToRelativePointOnScreen(0.4f, 0.55f, 0, 0.8f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // There are EIGHT shoes.;
        OC_Generic.pointer_moveToRelativePointOnScreen(0.6f, 0.55f, 0, 0.8f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OBControl number = numbers.get(7);
        OC_Generic.pointer_moveToObject(number, 5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        playAudioScene("DEMO", 2, true); // Eight.;
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false); // Take away four shoes.;
        OC_Generic.pointer_moveToObjectByName("obj_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 0; i < 4; i++)
        {
            OBControl obj = objectDict.get(String.format("obj_%d", i + 1));
            OC_Generic.pointer_moveToObject(obj, -25 + 5 * i, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            playAudioScene("DEMO", i + 4, false); // One. Two. Three. Four;
            playSfxAudio("remove_object", false);
            obj.hide();
            waitAudio();
        }
        playAudioScene("DEMO", 8, false); // There are four shoes left.;
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.8f, 0, 0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        number = numbers.get(3);
        playAudioScene("DEMO", 9, false); // Touch four.;
        OC_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(number, -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("correct", false);
        //
        lockScreen();
        action_highlightObject(number);
        action_resizeNumber((OBLabel) number, true, false);
        unlockScreen();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 10, false); // Eight take away four gives four.;
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.8f, 0, 0.6f, true, this);
        waitAudio();
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

}
