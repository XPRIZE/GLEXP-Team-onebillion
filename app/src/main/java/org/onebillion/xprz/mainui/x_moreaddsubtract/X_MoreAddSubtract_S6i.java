package org.onebillion.xprz.mainui.x_moreaddsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 28/07/16.
 */
public class X_MoreAddSubtract_S6i extends XPRZ_SectionController
{
    Map<String,Integer> eventColour;
    List<OBGroup> dragTargets, dropTargets;
    int currentPhase;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master6i");

        dragTargets = new ArrayList<>();
        dropTargets = new ArrayList<>();
        eventColour = OBMisc.loadEventColours(this);

        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        PointF loc = XPRZ_Generic.copyPoint(objectDict.get("eqbox1").position());
        loc.x = OB_Maths.locationForRect(-0.5f,0f,this.bounds()).x;
        objectDict.get("eqbox1").setPosition(loc);

        PointF loc2 =  XPRZ_Generic.copyPoint(objectDict.get("eqbox2").position());
        loc2.x = OB_Maths.locationForRect(1.5f,0f,this.bounds()).x;
        objectDict.get("eqbox2").setPosition(loc2);

        OBPath dragBox =((OBPath)objectDict.get("box_drag"));
        dragBox.sizeToBoundingBoxIncludingStroke();
        objectDict.get("box_drag").show();
        objectDict.get("box_drop").show();
        setSceneXX(currentEvent());
    }
}
