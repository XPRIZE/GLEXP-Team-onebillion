package com.maq.xprize.onecourse.hindi.mainui.oc_addtakeaway;

import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_AddRemoveObjects_SelectCorrectNumber;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 04/05/2017.
 */

public class OC_AddTakeAway_S4 extends OC_Generic_AddRemoveObjects_SelectCorrectNumber
{
    public OC_AddTakeAway_S4 ()
    {
        super(true);
    }


    public boolean hasAlreadyVisibleObjects ()
    {
        return true;
    }


    public void action_answerIsCorrect (OBLabel target) throws Exception
    {
        action_resizeNumber(target, true, true);
        //
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        phase = 2;
        action_showEquationForPhase();
        //
        playSceneAudio("CORRECT", true);
        waitForSecs(0.3);
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



    public void action_revealObject (OBControl control, PointF pt) throws Exception
    {
        // no movement into place for events where you need to select the number afterwards
        playSfxAudio(getSFX_placeObject(), false);
        //
        lockScreen();
        control.show();
        //
        // find the closest visible dash to the object that is about to be revealed
        OBControl bestMatch = null;
        Float bestDistance = null;
        //
        for (OBControl dash : filterControls("dash_.*"))
        {
            if (!dash.hidden())
            {
                float distance = OB_Maths.PointDistance(dash.getWorldPosition(), control.position());
                if (bestDistance == null || bestDistance > distance)
                {
                    bestMatch = dash;
                    bestDistance = distance;
                }
            }
        }
        if (bestMatch != null)
        {
            bestMatch.hide();
        }
        unlockScreen();
    }


    public void demo4a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look.;
        OC_Generic.pointer_moveToObjectByName("obj_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, true); // TWO frogs.;
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Touch the line to add one more frog.;
        OC_Generic.pointer_moveToObjectByName("dash_1", -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("dash_1", -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("add_object", false);
        //
        lockScreen();
        OBControl obj = objectDict.get("obj_3");
        OBControl dash = objectDict.get("dash_1");
        obj.show();
        dash.hide();
        unlockScreen();
        //
        OC_Generic.pointer_moveToObjectByName("dash_1", -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, true); // Now there are THREE frogs.;
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 4, false); // Touch three.;
        OC_Generic.pointer_moveToObject(numbers.get(2), -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(numbers.get(2), -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("correct", false);
        //
        lockScreen();
        action_highlightObject(numbers.get(2));
        for (OBControl control : equation)
        {
            control.show();

        }
        unlockScreen();
        //
        OC_Generic.pointer_moveToObject(numbers.get(2), -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(equation.get(0), -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        playAudioScene("DEMO", 5, false); // Two add one equals three.;
        OC_Generic.pointer_moveToObject(equation.get(4), 0, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitForAudio();
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

}
