package org.onebillion.onecourse.mainui.generic;

import org.onebillion.onecourse.controls.OBControl;

import java.util.List;

/**
 * OC_Generic_CompleteSequence
 * Generic Event designed for Activities where the Child needs to complete a sequence of objects in a line, by dragging the correct object to its place
 * Special case of OC_Generic_DragObjectsToCorrectPlace, where the objects are lined up in screen and a specific type of object is considered to be the correct answer.
 *
 * @see OC_Generic_DragObjectsToCorrectPlace
 * Created by pedroloureiro on 12/07/16.
 */
public class OC_Generic_CompleteSequence  extends OC_Generic_DragObjectsToCorrectPlace
{
    public OC_Generic_CompleteSequence()
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
