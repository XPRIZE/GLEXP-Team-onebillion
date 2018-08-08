package org.onebillion.onecourse.mainui.oc_numbers1to10;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_AddRemoveObjects_SelectCorrectNumber;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 04/05/2017.
 */

public class OC_Numbers1To10_S6 extends OC_Generic_AddRemoveObjects_SelectCorrectNumber
{

    public OC_Numbers1To10_S6 ()
    {
        super(true);
    }

    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        OBLabel bigNumber = numbers.get(initialShownObjects - 1);
        action_resizeNumber(bigNumber, true, true);
    }


    public void action_objectPlacedOrRemoved ()
    {
        OBLabel bigNumber = numbers.get(initialShownObjects - 1);
        action_resizeNumber(bigNumber, true, false);
    }


    @Override
    public void check_correctNumberObjectsShown_viaHide () throws Exception
    {
        super.check_correctNumberObjectsShown_viaHide();
        action_objectPlacedOrRemoved();
    }


    @Override
    public void check_correctNumberObjectsShown_viaShow() throws Exception
    {
        super.check_correctNumberObjectsShown_viaShow();
        action_objectPlacedOrRemoved();
    }


    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        lockScreen();
        action_highlightObject(target);
        action_resizeNumber((OBLabel) target, true, true);
        unlockScreen();
        //
        gotItRightBigTick(true);
        waitForSecsNoThrow(0.3);
        //
        playAudioQueuedScene("CORRECT", 0.3f, true);
        if (((List<Object>) (Object) getAudioForScene(currentEvent(), "FINAL")).size() > 0)
        {
            waitForSecsNoThrow(0.3);
            playAudioQueuedScene("FINAL", 0.3f, true);
        }
        else
        {
            waitForSecsNoThrow(0.7);
        }
        nextScene();
    }


    public void demo6a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look. There are THREE cakes.;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.4f, 0.6f), bounds()), -10, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // Touch the line to add one more.;
        OC_Generic.pointer_moveToObjectByName("dash_1", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Like this.;
        OC_Generic.pointer_moveToObjectByName("obj_4", -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        playSfxAudio("add_object", false);
        //
        lockScreen();
        objectDict.get("obj_4").show();
        objectDict.get("dash_1").hide();
        action_resizeNumber(numbers.get(2), true, false);
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false); // How many cakes are there now?;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.6f), bounds()), -10, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 1; i <= 4; i++)
        {
            String objectName = String.format("obj_%d", i);
            float rotation = -25 + i * 5;
            float time = (i == 1 ? 0.6f : 0.3f);
            OC_Generic.pointer_moveToObjectByName(objectName, rotation, time, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            playAudioScene("DEMO", 3 + i, true);
        }
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 8, false); // Touch four.;
        OC_Generic.pointer_moveToObject(numbers.get(3), 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        //
        lockScreen();
        action_resizeNumber(numbers.get(3), true, true);
        unlockScreen();
        //
        playSfxAudio("correct", false);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("obj_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        playAudioScene("DEMO", 9, false); // THREE add ONE gives FOUR.;
        OC_Generic.pointer_moveToObjectByName("obj_4", -5, 1.5f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void demo6g () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Now let’s take things away. There are FOUR bottles of orange juice.;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.6f), bounds()), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // Touch one to take it away.;
        OC_Generic.pointer_moveToObjectByName("obj_4", -10, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Like this.;
        OC_Generic.pointer_moveToObjectByName("obj_4", -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("remove_object", false);
        //
        lockScreen();
        action_resizeNumber(numbers.get(3), true, false);
        objectDict.get("obj_4").hide();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false); // There are THREE bottles left.;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.4f, 0.6f), bounds()), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 4, false); // Touch THREE.;
        OC_Generic.pointer_moveToObject(numbers.get(2), 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        lockScreen();
        action_resizeNumber(numbers.get(2), true, true);
        unlockScreen();
        //
        playSfxAudio("correct", false);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("obj_3", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        playAudioScene("DEMO", 5, false); // FOUR take away ONE  gives  THREE.;
        OC_Generic.pointer_moveToObjectByName("obj_2", -10, 1.5f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }



}
