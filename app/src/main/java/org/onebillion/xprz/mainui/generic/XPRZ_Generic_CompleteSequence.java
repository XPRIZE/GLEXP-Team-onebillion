package org.onebillion.xprz.mainui.generic;

import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class XPRZ_Generic_CompleteSequence  extends XPRZ_Generic_DragObjectsToCorrectPlace
{
    public XPRZ_Generic_CompleteSequence()
    {
        super();
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        for (OBControl control : filterControls(action_getObjectPrefix() + ".*"))
        {
            String locked = (String) control.attributes().get("locked");
            //
            if (locked == null || locked.equals("yes") || locked.equals("true"))
            {
                control.disable();
            }
            else
            {
                control.enable();
            }
            control.show();
        }
        //
        for (OBControl control : filterControls("place.*"))
        {
            control.hide();
        }
    }


    public String action_getObjectPrefix()
    {
        return "obj";
    }



    public String action_getContainerPrefix()
    {
        return "place";
    }


    public Boolean action_isPlacementCorrect(OBControl dragged, OBControl container)
    {
        return container.attributes().get("type").equals(dragged.attributes().get("type"));
    }



    public Boolean action_isEventOver()
    {
        List<OBControl> controls = filterControls(action_getContainerPrefix() + ".*");
        for (OBControl control : controls)
        {
            if (control.isEnabled()) return false;
        }
        return true;
    }



    public void action_correctAnswer(OBControl dragged, OBControl container) throws Exception
    {
        for (OBControl dash : filterControls("dash.*"))
        {
            String parent = (String) dash.attributes().get("parent");
            if (parent.equals(container.attributes().get("id")))
            {
                dash.hide();
            }
        }
        container.disable();
    }

}
