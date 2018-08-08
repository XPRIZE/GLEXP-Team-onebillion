package org.onebillion.onecourse.mainui.oc_diagnostics;

import android.graphics.Color;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.onebillion.onecourse.mainui.OBMainViewController.SHOW_TOP_LEFT_BUTTON;

/**
 * Created by pedroloureiro on 03/05/2018.
 */

public class OC_DiagnosticsDebug extends OC_Diagnostics
{

    public void prepare()
    {
        super.prepare();
        //
        timeoutEnabled = false;
        //
        loadEvent("debug");
        hideControls("presenter");
        hideControls("background.*");
        hideControls("question_button");
        //
        events = new ArrayList<>();
        events.add("debug");
        //
        doVisual(currentEvent());
    }


    @Override
    public int buttonFlags()
    {
        return SHOW_TOP_LEFT_BUTTON;
    }




    public void setSceneXX(String  scene)
    {
        MainActivity.log("Debug Scene %s", scene);
        List remedialUnits = OC_DiagnosticsManager.sharedManager().RemedialUnits();
        List remedialUnits_day1 = (List) remedialUnits.get(0);
        List remedialUnits_day2 = (List) remedialUnits.get(remedialUnits.size() - 1);
        //
        hideControls("label.*");
        //
        for (int i = 1; i <= 15; i++)
        {
            OBControl labelBox = objectDict.get(String.format("label%d_day1", i));
            if (labelBox == null) continue;
            //
            String unit = remedialUnits_day1.size() >= i ? (String) remedialUnits_day1.get(i-1) : "";
            if (unit.length() == 0) continue;
            //
            OBLabel label = OC_Generic.action_createLabelForControl(labelBox, unit, 0.7f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
            attachControl(label);
            label.setLeft(labelBox.left());
            labelBox.hide();
        }
        //
        for (int i = 1; i <= 15; i++)
        {
            OBControl labelBox = objectDict.get(String.format("label%d_day2", i));
            if (labelBox == null) continue;
            //
            String unit = remedialUnits_day2.size() >= i ? (String) remedialUnits_day2.get(i-1) : "";
            if (unit.length() == 0) continue;
            //
            OBLabel label = OC_Generic.action_createLabelForControl(labelBox, unit, 0.7f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
            attachControl(label);
            label.setLeft(labelBox.left());
            labelBox.hide();
        }
    }
}