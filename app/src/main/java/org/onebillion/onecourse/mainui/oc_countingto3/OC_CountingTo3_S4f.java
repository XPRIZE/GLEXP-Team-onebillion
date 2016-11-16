package org.onebillion.onecourse.mainui.oc_countingto3;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_DragObjectsToCorrectPlace;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 20/06/16.
 */
public class OC_CountingTo3_S4f extends OC_Generic_DragObjectsToCorrectPlace
{
    public OC_CountingTo3_S4f ()
    {
        super();
    }

    @Override
    public String action_getScenesProperty()
    {
        return "scenes2";
    }

    @Override
    public String action_getObjectPrefix()
    {
        return "object";
    }

    @Override
    public String action_getContainerPrefix()
    {
        return "container";
    }



    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        //
        for(OBControl number : filterControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number, 1.2f);
        }
    }


    public void demo4f() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.7f);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now Look
        OC_Generic.pointer_moveToObjectByName("object_1", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // One thing for the one box
        OBControl object = objectDict.get("object_1");
        PointF destination = objectDict.get("container_1").position();
        OC_Generic.pointer_moveToPointWithObject(object, destination, -25, 0.6f, false, this);
        playSfxAudio("dropObject", false);
        waitForSecs(0.3);
        OC_Generic.pointer_moveToObjectByName("number_1", -15, 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.7);
        //
        thePointer.hide();
        waitAudio();
        waitForSecs(0.3);
        //
        action_moveObjectToOriginalPosition(object, true);
        //
        nextScene();
    }



    public Boolean action_isEventOver()
    {
        List<OBControl> containers = filterControls(action_getContainerPrefix() + ".*");
        for (OBControl container : containers)
        {
            List<OBControl> containedObjects = (List<OBControl>) container.propertyValue("contained");
            if (containedObjects == null) containedObjects = new ArrayList<OBControl>();
            int correctQuantity = Integer.parseInt((String)container.attributes().get("correctQuantity"));
            if (containedObjects.size() != correctQuantity) return false;
        }
        return true;
    }

    public Boolean action_isPlacementCorrect(OBControl dragged, OBControl container)
    {
        int correctQuantity = Integer.parseInt((String)container.attributes().get("correctQuantity"));
        List<OBControl> containedObjects = (List<OBControl>) container.propertyValue("contained");
        if (containedObjects == null) containedObjects = new ArrayList<OBControl>();
        return containedObjects.size() < correctQuantity;
    }


    public void action_correctAnswer(OBControl dragged, OBControl container) throws Exception
    {
        if (action_isEventOver())
        {
            waitForSecs(0.3);
            //
            playAudioQueuedScene(currentEvent(), "CORRECT", true);
        }
    }

}
