package com.maq.xprize.onecourse.mainui.oc_countingto3;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_ColourObjects;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 17/01/2017.
 */

public class OC_CountingTo3_S2 extends OC_Generic_ColourObjects
{
    public OC_CountingTo3_S2()
    {
        super();
    }


    public String value_paintPotPrefix()
    {
        return "paintpot";
    }

    public String value_objectPrefix()
    {
        return "obj";
    }

    public Boolean value_canReplaceColours()
    {
        return false;
    }

    public Boolean value_mustPickCorrectColour()
    {
        return true;
    }

    public void demo2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now let’s colour some stars. Like this.
        OBGroup paintpot = (OBGroup)objectDict.get("paintpot_1");
        movePointerToPoint(paintpot.position(), -10, 1.2f, true);
        waitAudio();
        //
        action_playSelectPaintpotSoundEffect(false);
        action_selectPaintPoint(paintpot);
        //
        OBControl object = objectDict.get("obj_3");
        movePointerToPoint(object.position(), -10, 0.6f, true);
        action_playColourObjectSoundEffect(false);
        action_colourObjectWithSelectedColour(object);
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }



}
