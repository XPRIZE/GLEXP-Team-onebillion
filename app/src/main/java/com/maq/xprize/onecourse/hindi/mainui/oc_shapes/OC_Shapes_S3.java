package com.maq.xprize.onecourse.hindi.mainui.oc_shapes;

import android.graphics.Color;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class OC_Shapes_S3 extends OC_Generic_SelectCorrectObject
{

    List<OBLabel> labels;

    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        if (redraw)
        {
            if (labels != null)
            {
                for (OBLabel label : labels)
                {
                    detachControl(label);
                }
                labels.clear();
            }
            labels = new ArrayList<OBLabel>();
            for (OBControl control : filterControls("label.*"))
            {
                OBLabel label = action_createLabelForControl(control, 1.2f);
                labels.add(label);
                control.hide();
                objectDict.put(String.format("number_%s", control.attributes().get("text")), label);
            }
        }
    }

    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        lockScreen();
        for (OBLabel label : labels)
        {
            label.setColour((label.equals(control)) ? OBUtils.colorFromRGBString("240,0,0") : Color.BLACK);
        }
        unlockScreen();
    }


    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        action_highlight(null);
    }


    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        List<OBControl> filteredControls = new ArrayList();
        List criterias = Arrays.asList(eventAttributes.get("criteria").split(","));
        List<OBControl> allControls = filterControls("obj.*");
        for (OBControl control : allControls)
        {
            if (criterias.contains(control.attributes().get("type")))
            {
                filteredControls.add(control);
            }
        }
        //
        if (eventAttributes.get("correctAnswer").equals("0"))
        {
            playSceneAudio("CORRECT", true);
        }
        else
        {
            int i = 0;
            for (OBControl control : filteredControls)
            {
                playSceneAudioIndex("CORRECT", i, false);
                //
                OBPath clone = (OBPath) control.copy();
                OBPath path = (OBPath) control.copy();
                path.setStrokeColor(Color.BLACK);
                path.setLineWidth(applyGraphicScale(24));
                path.setMaskControl(clone);
                path.sizeToBox(new RectF(bounds()));
                //
                lockScreen();
                OC_Generic.sendObjectToTop(path, this);
                attachControl(path);
                path.show();
                unlockScreen();
                waitAudio();
                //
                lockScreen();
                path.hide();
                detachControl(path);
                unlockScreen();
                waitForSecs(0.3);
                i++;
            }
        }
        playSceneAudio("FINAL", true);
        //
        action_lowlight(target);
        //
        nextScene();
    }
}