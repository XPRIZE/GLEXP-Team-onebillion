package com.maq.xprize.onecourse.mainui.oc_counting4to6;

import android.graphics.Path;
import android.graphics.PointF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_DragNumbersToSlots;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;

import java.util.Collections;
import java.util.EnumSet;

import static com.maq.xprize.onecourse.utils.OBUtils.SimplePath;
import static com.maq.xprize.onecourse.utils.OB_Maths.PointDistance;

/**
 * Created by pedroloureiro on 30/03/2017.
 */

public class OC_Counting4To6_S4 extends OC_Generic_DragNumbersToSlots
{

    @Override
    public void fin ()
    {
        goToCard(OC_Counting4To6_S4e.class, "event4");
    }


    public void demo4a () throws Exception
    {
        try
        {
            setStatus(STATUS_BUSY);
            //
            loadPointer(POINTER_MIDDLE);
            //
            action_playNextDemoSentence(false); //  Look.
            //
            for (int i = 4; i <= 6; i++)
            {
                OC_Generic.pointer_moveToObjectByName(String.format("obj_%d_1", i), -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
                waitAudio();
                waitForSecs(0.3f);
                //
                action_playNextDemoSentence(false); // There are four / five / six beads on this wire.
                OC_Generic.pointer_moveToObjectByName(String.format("obj_%d_%d", i, i), -10 + i, 1 + (i - 3) * 0.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
                waitAudio();
                waitForSecs(0.3f);
                //
                action_playNextDemoSentence(false); //  Four. Five. Six.
                OC_Generic.pointer_moveToObjectByName(String.format("number_%d", i - 3), 5, 0.8f - (i - 3) * 0.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
                waitForSecs(0.3f);
                //
                OBControl number = objectDict.get(String.format("number_%d", i - 3));
                number.show();
                waitAudio();
                waitForSecs(0.3f);
            }
            //
            for (int i = 1; i <= 3; i++)
            {
                OBControl number = objectDict.get(String.format("number_%d", i));
                PointF originalPosition = (PointF) number.propertyValue("originalPosition");
                float distance = PointDistance(number.position(), originalPosition);
                Path path = SimplePath(number.position(), originalPosition, -distance / 5);
                OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
                OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                waitForSecs(0.1f);
            }
            //
            action_playNextDemoSentence(false); //  Now let’s move a number.
            OC_Generic.pointer_moveToObjectByName("obj_4_1", -10, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            waitAudio();
            waitForSecs(0.3f);
            //
            action_playNextDemoSentence(false); //  Four beads.
            OC_Generic.pointer_moveToObjectByName("obj_4_4", -6, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            waitAudio();
            waitForSecs(0.3f);
            //
            OBControl number = objectDict.get("number_1");
            OC_Generic.pointer_moveToObjectByName("number_1", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            waitForSecs(0.3f);
            //
            OBControl box = objectDict.get("box_1");
            action_playNextDemoSentence(false); //  Four.
            OC_Generic.sendObjectToTop(number, this);
            //
            movePointerToPointWithObject(number, box.position(), 10, 0.6f, true);
            waitForAudio();
            waitForSecs(0.3f);
            //
            thePointer.hide();
            waitForSecs(0.3f);
            //
            PointF originalPosition = (PointF) number.propertyValue("originalPosition");
            float distance = PointDistance(number.position(), originalPosition);
            Path path = SimplePath(number.position(), originalPosition, -distance / 5);
            OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
            OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            //
            waitForSecs(0.7f);
            nextScene();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setScene4a ()
    {
        setSceneXX(currentEvent());
        //
        for (int i = 1; i <= 3; i++)
        {
            OBControl number = objectDict.get(String.format("number_%d", i));
            OBControl box = objectDict.get(String.format("box_%d", i));
            number.setPosition(box.position());
            number.hide();
        }
    }

    @Override
    public void action_correctAnswer (OBControl target) throws Exception
    {
        playAudioScene("CORRECT", Integer.parseInt((String) target.attributes().get("number")) - 4, true);
    }

}
