package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.Arrays;

/**
 * Created by michal on 06/04/2017.
 */

public class OC_MoreNumbers_Additions
{
    public static void insertIntoGroup(OBGroup group, int num, float size, int colour, PointF position, OC_SectionController sectionController)
    {
        Typeface typeface = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(String.format("%d",num),typeface,size);
        label.setColour(colour);
        label.setScale(1.0f/group.scale());
        OBGroup numGroup = new OBGroup(Arrays.asList((OBControl)label));
        numGroup.sizeToTightBoundingBox();
        sectionController.attachControl(numGroup);
        numGroup.setPosition(position);
        group.insertMember(numGroup,0,"number");
        group.objectDict.put("label",label);
        numGroup.setZPosition(10);
    }

    public static void buttonSet(int state, OC_SectionController sectionController)
    {
        OBGroup button = (OBGroup)sectionController.objectDict.get("button_arrow");
        sectionController.lockScreen();
        button.objectDict.get("selected").hide();
        button.objectDict.get("inactive").hide();
        if(state == 1)
            button.objectDict.get("selected").show();
        else if(state == 2)
            button.objectDict.get("inactive").show();
        sectionController.unlockScreen();
    }

}
