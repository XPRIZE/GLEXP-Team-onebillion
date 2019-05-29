package com.maq.xprize.onecourse.hindi.mainui.oc_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.List;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Counting5and10_S1f extends OC_Generic_SelectCorrectObject
{
    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }


    public void demo1e () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(false); // Count again with me !
        waitAudio();
        waitForSecs(0.3f);
        //
        for (int i = 1;  i <= 10; i++)
        {
            action_playNextDemoSentence(false); // TEN. TWENTY. THIRTY. FORTY. FIFTY. SIXTY. SEVENTY. EIGHTY. NINETY. ONE HUNDRED.
            OBControl control = objectDict.get(String.format("number_%d", i));
            control.show();
            //
            OBLabel label = (OBLabel) control.propertyValue("label");
            label.show();
            waitAudio();
            //
            waitForSecs(0.3f);
        }
        nextScene();
    }

    public void action_wrongAnswer () throws Exception
    {
        gotItWrongWithSfx();
        waitForSecs(0.3f);
        //
        playAudioQueuedScene("INCORRECT", false);
    }

    public void action_correctAnswer (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3f);
        //
        playAudioQueuedScene("CORRECT", true);
        waitForSecs(0.3f);
        //
        action_lowlightObject(target);
        playAudioQueuedScene("FINAL", true);
        nextScene();
    }

    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        if (redraw)
        {
            OBControl previous = null;
            List<OBControl> controls = sortedFilteredControls("number.*");
            for (OBControl control : controls)
            {
                if (previous != null)
                {
                    control.setLeft(previous.right() - control.lineWidth() * 2);
                }
                previous = control;
            }
            //
            OBGroup group = new OBGroup(controls);
            attachControl(group);
            //
            OC_Generic.sendObjectToTop(group, this);
            group.setPosition(new PointF(bounds().width() / 2, bounds().height() / 2));
            for (OBControl control : sortedFilteredControls("number.*"))
            {
                OBLabel label = action_createLabelForControl(control, 1.0f, false);
                label.setPosition(control.getWorldPosition());
                control.setProperty("label", label);
                label.hide();
            }
        }
    }

    public void action_highlightObject (OBControl selectedObject)
    {
        selectedObject.setFillColor(OBUtils.colorFromRGBString("89,245,255"));

    }

    public void action_lowlightObject (OBControl selectedObject)
    {
        selectedObject.setFillColor(Color.WHITE);
    }


}
