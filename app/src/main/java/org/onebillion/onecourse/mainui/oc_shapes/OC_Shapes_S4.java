package org.onebillion.onecourse.mainui.oc_shapes;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_SelectCorrectObject;

import java.util.EnumSet;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class OC_Shapes_S4 extends OC_Generic_SelectCorrectObject
{

    @Override
    public String action_getObjectPrefix ()
    {
        return "obj";
    }

    public void demo4a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.6f, 0.6f, 0, 0.3f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        float angle = -20;
        for (OBControl control : sortedFilteredControls("obj.*"))
        {
            if(control.attributes().get("id").equals("obj_3")) continue;
            action_playNextDemoSentence(false); // A triangle
            OC_Generic.pointer_moveToObject(control, angle, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            waitAudio();
            angle += 5;
        }
        //
        action_playNextDemoSentence(false); // A circle
        OC_Generic.pointer_moveToObjectByName("obj_3", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // The circle is the odd one out
        OC_Generic.pointer_moveToObjectByName("obj_3", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        nextScene();
    }


}