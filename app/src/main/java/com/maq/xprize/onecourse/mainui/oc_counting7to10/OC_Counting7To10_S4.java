package com.maq.xprize.onecourse.mainui.oc_counting7to10;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_DragNumbersToSlots;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static com.maq.xprize.onecourse.utils.OBUtils.SimplePath;
import static com.maq.xprize.onecourse.utils.OB_Maths.PointDistance;

/**
 * Created by pedroloureiro on 02/05/2017.
 */

public class OC_Counting7To10_S4 extends OC_Generic_DragNumbersToSlots
{

    public void demo4a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); //  Look.
        waitForSecs(0.3f);
        int audio_index = 1;
        for (int i = 0; i < 4; i++)
        {
            OBControl dots = objectDict.get(String.format("dots_%d", i + 7));
            OC_Generic.pointer_moveToObject(dots, -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
            waitForSecs(0.3f);
            //
            dots.show();
            waitForSecs(0.3f);
            //
            playAudioScene("DEMO", audio_index++, false);  // Seven / Eight / Nine / Ten counters.
            OC_Generic.pointer_moveToObject(dots, -10, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
            waitAudio();
            waitForSecs(0.3f);
            //
            OBControl number = objectDict.get(String.format("number_%d", i + 1));
            OBControl box = objectDict.get(String.format("box_%d", i + 1));
            OC_Generic.pointer_moveToObject(box, -5, 0.6f - i * 0.1f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
            //
            lockScreen();
            number.setPosition(box.position().x, box.position().y);
            number.show();
            box.show();
            unlockScreen();
            //
            playAudioScene("DEMO", audio_index++, false);
            waitAudio();
            waitForSecs(0.6f);
        }
        for (int i = 1; i <= 4; i++)
        {
            OBControl number = objectDict.get(String.format("number_%d", i));
            PointF originalPosition = (PointF) number.propertyValue("originalPosition");
            float distance = PointDistance(number.position(), originalPosition);
            Path path = SimplePath(number.position(), originalPosition, -distance / 5);
            OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
            OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.5, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.1f);
        }
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 9, false); //  Now look
        OBControl dots = objectDict.get("dots_9");
        OC_Generic.pointer_moveToObject(dots, -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 10, false); //  There are nine yellow counters.
        OC_Generic.pointer_moveToObject(dots, -15, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OBControl number = objectDict.get("number_3");
        OC_Generic.pointer_moveToObject(number, -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3f);
        //
        OBControl box = objectDict.get("box_3");
        OC_Generic.pointer_moveToPointWithObject(number, box.position(), -5, 0.6f, true, this);
        playSfxAudio("drop_number", false);
        playAudioScene("DEMO", 11, true); //  Nine!
        waitForSecs(0.3f);
        //
        thePointer.hide();
        PointF originalPosition = (PointF) number.propertyValue("originalPosition");
        float distance = PointDistance(number.position(), originalPosition);
        Path path = SimplePath(number.position(), originalPosition, -distance / 5);
        OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
        OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.5, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.7f);
        //
        nextScene();
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        List<OBGroup> dots = (List<OBGroup>) (Object) filterControls("dots.*");
        for (OBGroup dotGroup : dots)
        {
            for (OBPath dot : (List<OBPath>) (Object) dotGroup.members)
            {
                dot.sizeToBoundingBoxIncludingStroke();
            }
            dotGroup.setShouldTexturise(false);
            if (scene.equalsIgnoreCase("4a"))
            {
                dotGroup.hide();
            }
        }
        //
        List<OBControl> boxes = filterControls("box.*");
        for (OBControl box : boxes)
        {
            box.show();
        }
        //
        List<OBControl> numbers = filterControls("number.*");
        for (OBControl number : numbers)
        {
            if (scene.equalsIgnoreCase("4a"))
            {
                number.hide();
            }
            OC_Generic.sendObjectToTop(number, this);
        }
    }


    public void action_correctAnswer (OBControl target) throws Exception
    {
        if (audioForScene("CORRECT").size() == 1)
        {
            playAudioQueuedScene("CORRECT", 0.3f, true);
        }
        else
        {
            playAudioScene("CORRECT", Integer.parseInt((String) target.attributes().get("number")) - 7, true);
        }
    }

}
