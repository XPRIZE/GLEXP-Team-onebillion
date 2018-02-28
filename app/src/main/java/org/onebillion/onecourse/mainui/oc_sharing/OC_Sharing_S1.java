package org.onebillion.onecourse.mainui.oc_sharing;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pedroloureiro on 05/05/2017.
 */

public class OC_Sharing_S1 extends OC_Generic_Event
{
    HashMap<String, List> containers;
    int maxCapacity;
    String objectType;


    public void setSceneXX (String scene)
    {
        deleteControls(String.format("%s.*", action_getObjectPrefix()));
        //
        super.setSceneXX(scene);
        //
        for (OBGroup container : (List<OBGroup>) (Object) filterControls(String.format("%s.*", action_getContainerPrefix())))
        {
            container.hideMembers(".*");
            container.showMembers("colour.*");
            container.showMembers("body");
            container.showMembers("smile_normal");
            container.showMembers("nothing");
        }
    }


    public void demo1a () throws Exception
    {
        setStatus(STATUS_BUSY);
        demoButtons();
        playAudioScene("DEMO", 0, false); // Now look. There are two apples.;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.9f), bounds()), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        OBControl control = objectDict.get("object_1");
        OBGroup container = (OBGroup) objectDict.get("container_1");
        playAudioScene("DEMO", 1, false); // One for her.;
        OC_Generic.pointer_moveToObject(control, -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.1f);
        //
        OC_Generic.pointer_moveToPointWithObject(control, OB_Maths.worldLocationForControl(0.3f, 0.5f, container), -25, 0.6f, true, this);
        action_placeObjectInContainer(control, container);
        waitAudio();
        waitForSecs(0.3f);
        //
        control = objectDict.get("object_2");
        container = (OBGroup) objectDict.get("container_2");
        playAudioScene("DEMO", 2, false); // And one for him.;
        OC_Generic.pointer_moveToObject(control, -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.1f);
        //
        OC_Generic.pointer_moveToPointWithObject(control, OB_Maths.worldLocationForControl(0.3f, 0.5f, container), -5, 0.6f, true, this);
        action_placeObjectInContainer(control, container);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false); // They have one apple each.;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.6f), bounds()), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void setScene1g ()
    {
        deleteControls("container.*");
        loadEvent("master2");
        setSceneXX(currentEvent());
    }


    public void setScene1k ()
    {
        deleteControls("container.*");
        loadEvent("master3");
        setSceneXX(currentEvent());
    }


    public void setScene1n ()
    {
        deleteControls("container.*");
        loadEvent("master4");
        setSceneXX(currentEvent());
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "object";
    }


    @Override
    public String action_getContainerPrefix ()
    {
        return "container";
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        objectType = eventAttributes.get("object");
        maxCapacity = Integer.parseInt(eventAttributes.get("maxCapacity"));
        containers = new HashMap();
        //
        int skinColourOffset = 0;
        for (OBGroup container : (List<OBGroup>) (Object) filterControls(String.format("%s_.*", action_getContainerPrefix())))
        {
            containers.put((String) container.attributes().get("id"), new ArrayList<>(maxCapacity));
            container.objectDict.get("nothing").show();
            container.objectDict.get("smile_normal").show();
            int childColour = OBConfigManager.sharedManager.getSkinColour(skinColourOffset++);
            container.substituteFillForAllMembers("colour.*", childColour);
        }
        for (OBControl control : filterControls(String.format("%s_.*", action_getObjectPrefix())))
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.position()));
        }
    }


    public boolean action_isContainerAvailable (OBControl container)
    {
        return containers.get(container.attributes().get("id")).size() < maxCapacity;
    }


    public void action_placeObjectInContainer (OBControl object, OBGroup container) throws Exception
    {
        playSfxAudio("place_object", false);
        //
        lockScreen();
        object.disable();
        object.hide();
        container.objectDict.get("nothing").hide();
        container.objectDict.get(objectType).show();
        container.objectDict.get("smile_normal").hide();
        container.objectDict.get("smile_happy").show();
        unlockScreen();
        //
        containers.get(container.attributes().get("id")).add(object);
    }


    public boolean action_isEventOver ()
    {
        List<OBControl> controls = filterControls(String.format("%s_.*", action_getObjectPrefix()));
        for (OBControl control : controls)
        {
            if (control.isEnabled()) return false;
        }
        return true;
    }



    public void action_wrongAnswer (OBControl target) throws Exception
    {
        gotItWrongWithSfx();
        playAudioQueuedScene("INCORRECT", 300, false);
    }


    public void action_returnObject (OBControl object) throws Exception
    {
        PointF originalPosition = (PointF) object.propertyValue("originalPosition");
        moveObjectToPoint(object, originalPosition, 0.3f, false);
    }


    public void checkDragAtPoint (PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            //
            List<OBControl> containers = filterControls(String.format("%s_.*", action_getContainerPrefix()));
            OBControl container = finger(0, 2, containers, pt);
            OBControl object = target;
            target = null;
            if (container != null)
            {
                if (action_isContainerAvailable(container))
                {
                    action_placeObjectInContainer(object, (OBGroup) container);
                    if (action_isEventOver())
                    {
                        waitForSecs(0.3f);
                        //
                        gotItRightBigTick(true);
                        playSceneAudio("CORRECT", true);
                        waitForSecs(0.3f);
                        //
                        playSceneAudio("FINAL", true);
                        waitForSecs(0.3f);
                        //
                        nextScene();
                        return;
                    }
                }
                else
                {
                    action_wrongAnswer(object);
                    action_returnObject(object);
                }
            }
            else
            {
                action_returnObject(object);
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBControl findTarget (PointF pt)
    {
        List<OBControl> objs = filterControls(String.format("%s.*", action_getObjectPrefix()));
        OBControl c = finger(0, 2, objs, pt, true);
        return c;
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl obj = findTarget(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkDragTarget(obj, pt);
                    }
                });
            }
        }
    }


    public void checkDragTarget (OBControl targ, PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target = targ;
        OC_Generic.sendObjectToTop(targ, this);
        targ.animationKey = (long) OC_Generic.currentTime();
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }


    public void touchUpAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }

}
