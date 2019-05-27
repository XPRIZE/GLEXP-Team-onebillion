package com.maq.xprize.onecourse.mainui.oc_numbers1to10;

import android.graphics.Color;
import android.graphics.PointF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_CompleteSequence;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class OC_Numbers1To10_S2 extends OC_Generic_CompleteSequence
{

    @Override
    public String action_getObjectPrefix ()
    {
        return "obj";
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        List<OBLabel> createdLabels = new ArrayList<>();
        //
        OBControl templateBox = objectDict.get("label_template");
        OBLabel templateLabel = action_createLabelForControl(templateBox);
        detachControl(templateLabel);
        detachControl(templateBox);
        //
        float defaultFontSize = templateLabel.fontSize() * 0.8f;
        //
        for (OBControl number : filterControls("obj.*"))
        {
            OBLabel label = action_createLabelForControl(number);
            label.setFontSize(defaultFontSize);
            label.sizeToBoundingBox();
            label.setPosition(number.position());
            //
            label.setProperty("originalPosition", OC_Generic.copyPoint(label.position()));
            OC_Generic.sendObjectToTop(label, this);
            label.setProperty("number", number.attributes().get("number"));
            //
            String locked = (String) number.attributes().get("locked");
            //
            if (locked == null || locked.equals("yes") || locked.equals("true"))
            {
                label.disable();
            }
            else
            {
                label.enable();
            }
            objectDict.put((String) number.attributes().get("id"), label);
            detachControl(number);
            //
            createdLabels.add(label);
        }
    }

    public Boolean action_isPlacementCorrect(OBControl dragged, OBControl container)
    {
        return container.attributes().get("number").equals(dragged.propertyValue("number"));
    }


    public void action_finalAnimation() throws Exception
    {
        if (currentEvent().equals("2n"))
        {
            for (int i = 1; i <= 10; i++)
            {
                for (OBControl number : filterControls("obj.*"))
                {
                    if (Integer.parseInt((String)number.propertyValue("number")) == i)
                    {
                        playSceneAudioIndex("FINAL", i-1, false);
                        float scale = number.scale();
                        OBAnim anim1 = OBAnim.scaleAnim(1.75f * scale, number);
                        OBAnim anim2 = OBAnim.scaleAnim(scale, number);
                        OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(anim1),Arrays.asList(anim2)), Arrays.asList(0.15f,0.15f), true, Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT, OBAnim.ANIM_EASE_IN_EASE_OUT), 1, this);
                        waitAudio();
                        waitForSecs(0.1);
                    }
                }
            }
            playSceneAudioIndex("FINAL", 10, true);
        }
        else
        {
            playSceneAudio("FINAL", true);
        }
    }


    public void demo2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.8f, 0.8f, -5f, 0.6f, true, this);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Two goes here
        OBControl number = objectDict.get("obj_10");
        OC_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3);
        //
        OBControl place = objectDict.get("place_1");
        OC_Generic.pointer_moveToPointWithObject(number, place.position(), -25, 0.6f, true, this);
        waitForSecs(0.3);
        //
        playSfxAudio("correct", false);
        hideControls("dash_1");
        OC_Generic.pointer_moveToObject(number, -25, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }

}