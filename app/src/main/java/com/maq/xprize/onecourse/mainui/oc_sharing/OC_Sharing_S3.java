package com.maq.xprize.onecourse.mainui.oc_sharing;

import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pedroloureiro on 09/05/2017.
 */

public class OC_Sharing_S3 extends OC_Generic_Event
{
    HashMap<String, List> containers;
    int maxContainers;
    int maxCapacity;


    public void setSceneXX (String scene)
    {
        deleteControls("object.*");
        super.setSceneXX(scene);
        action_hiliteNumber(null);
    }


    public void demo3a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look.  FOUR birds.;
        movePointerToPoint(action_getMiddleOfGroup("object.*"), -25, 0.9f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // I share them into two equal sets.;
        for (int i = 1; i <= 4; i++)
        {
            OBControl control = objectDict.get(String.format("object_%d", i));
            OBControl place = objectDict.get(String.format("place_%d", i));
            OC_Generic.pointer_moveToObject(control, -20, (i == 1) ? 0.3f : 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            OC_Generic.pointer_moveToPointWithObject(control, place.getWorldPosition(), -15, 0.6f, true, this);
            playSfxAudio("place_object", false);
            waitForSecs(0.3f);
        }
        playAudioScene("DEMO", 2, false); // There are TWO birds : each set.;
        OC_Generic.pointer_moveToObjectByName("container_1", -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("container_2", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_lower(this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void setScene3a ()
    {
        deleteControls("container.*");
        loadEvent("master1");
        setSceneXX(currentEvent());
        action_createNumbers();
    }


    public void setScene3j ()
    {
        deleteControls("container.*");
        loadEvent("master2");
        setSceneXX(currentEvent());
        action_createNumbers();
    }


    public void setScene3n ()
    {
        deleteControls("container.*");
        loadEvent("master3");
        setSceneXX(currentEvent());
        action_createNumbers();
    }


    public void finDemo3o () throws Exception
    {
        List<OBAnim> anims = new ArrayList();
        for (OBControl control : filterControls("object.*"))
        {
            PointF destination = (PointF) control.propertyValue("originalPosition");
            OBAnim anim = OBAnim.moveAnim(destination, control);
            anims.add(anim);
        }
        playSfxAudio("return_objects", false);
        OBAnimationGroup.runAnims(anims, 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public void action_createNumbers ()
    {
        for (OBControl number : filterControls("number.*"))
        {
            action_createLabelForControl(number, 1.2f);
            number.show();
        }
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        int total_objects = (int) filterControls("object.*").size();
        int total_containers = (int) filterControls("container.*").size();
        maxContainers = (eventAttributes.get("max_containers") == null) ? total_containers : Integer.parseInt(eventAttributes.get("max_containers"));
        maxCapacity = (maxContainers == 0) ? 0 : total_objects / maxContainers;
        containers = new HashMap<>();
        for (OBControl container : filterControls("container.*"))
        {
            OBPath containerPath = (OBPath) container;
            containerPath.sizeToBoundingBoxIncludingStroke();
            //
            containers.put((String) container.attributes().get("id"), new ArrayList<>(maxCapacity));
        }
        for (OBControl control : filterControls("object.*"))
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.position()));
            control.setProperty("originalScale", control.scale());
        }
        //
        for (OBControl control : filterControls("place.*"))
        {
            control.hide();
        }
    }


    public boolean action_isContainerAvailable (OBControl container)
    {
        List<OBControl> containersUsed = new ArrayList<>();
        for (OBControl control : filterControls("container.*"))
        {
            if (containers.get(control.attributes().get("id")).size() > 0)
            {
                containersUsed.add(control);
            }
        }
        //
        if (containersUsed.size() >= maxContainers)
        {
            if (containersUsed.contains(container) && containers.get(container.attributes().get("id")).size() < maxCapacity)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }


    public void action_placeObjectInContainer (OBControl object, OBControl container) throws Exception
    {
        List<OBControl> placements = action_getPlacementsForContainer(container);
        float bestDistance = -1;
        OBControl bestPlacement = null;
        for (OBControl placement : placements)
        {
            if (placement.isEnabled())
            {
                float distance = OB_Maths.PointDistance(placement.getWorldPosition(), object.getWorldPosition());
                if (bestDistance == -1 || distance < bestDistance)
                {
                    bestDistance = distance;
                    bestPlacement = placement;
                }
            }
        }
        if (bestPlacement != null)
        {
            object.disable();
            bestPlacement.disable();
            playSfxAudio("place_object", false);
            moveObjectToPoint(object, bestPlacement.getWorldPosition(), 0.3f, false);
            containers.get(container.attributes().get("id")).add(object);
        }
        else
        {
            action_returnObject(object);
        }
    }


    public List<OBControl> action_getPlacementsForContainer (OBControl container)
    {
        List<OBControl> result = new ArrayList<>();
        for (OBControl place : filterControls("place.*"))
        {
            if (action_getOverlap(place, container) > 0)
            {
                result.add(place);
            }
        }
        return result;
    }


    public float action_getOverlap (OBControl first, OBControl second)
    {
        float width = Math.min(first.right(), second.right()) - Math.max(first.left(), second.left());
        if (width < 0) return 0;
        //
        float height = Math.min(first.bottom(), second.bottom()) - Math.max(first.top(), second.top());
        if (height < 0) return 0;
        //
        float overlap = (height * width) / (first.height() * first.width());
        //
        return overlap;
    }



    public boolean action_isPlacementOver ()
    {
        List<OBControl> controls = filterControls("object.*");
        for (OBControl control : controls)
        {
            if (control.isEnabled()) return false;
        }
        return true;
    }



    public void action_fin()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("CORRECT", 0.3f, true);
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("FINAL", 0.3f, true);
                waitForSecs(0.3f);
                //
                if (performSel("finDemo", currentEvent()))
                {
                    waitForSecs(0.3f);
                }
                //
                nextScene();
            }
        });
    }



    public void action_wrongAnswer (OBControl target) throws Exception
    {
        gotItWrongWithSfx();
        playAudioQueuedScene("INCORRECT", 0.3f, false);
    }


    public void action_returnObject (OBControl object) throws Exception
    {
        PointF originalPosition = (PointF) object.propertyValue("originalPosition");
        moveObjectToPoint(object, originalPosition, 0.3f, false);
    }


    public void action_hiliteNumber (OBGroup number)
    {
        lockScreen();
        for (OBGroup control : (List<OBGroup>) (Object) filterControls("number.*"))
        {
            if (control.equals(number))
            {
                control.objectDict.get("selected").show();
            }
            else
            {
                control.objectDict.get("selected").hide();
            }
        }
        unlockScreen();
    }


    public void checkDragAtPoint (PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBControl object = target;
            target = null;
            OBControl container = findContainer(pt);
            if (container != null)
            {
                if (action_isContainerAvailable(container))
                {
                    action_placeObjectInContainer(object, container);
                    if (action_isPlacementOver())
                    {
                        waitSFX();
                        gotItRightBigTick(false);
                        waitSFX();
                        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
                        playAudioQueuedScene("PROMPT2", 0.3f, false);
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


    public void checkNumber (OBGroup number)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            action_hiliteNumber(number);
            int selectedValue = Integer.parseInt((String) number.attributes().get("number"));
            if (selectedValue == maxCapacity)
            {
                action_fin();
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("INCORRECT2", 0.3f, false);
                action_hiliteNumber(null);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBControl findTarget (PointF pt)
    {
        return finger(0, 2, filterControls("object.*"), pt, true);

    }


    public OBControl findContainer (PointF pt)
    {
        return finger(0, 2, filterControls("container.*"), pt);

    }


    public OBControl findNumber (PointF pt)
    {
        return finger(0, 2, filterControls("number.*"), pt);

    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (action_isPlacementOver())
            {
                final OBGroup obj = (OBGroup) findNumber(pt);
                if (obj != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            checkNumber(obj);
                        }
                    });
                }
            }
            else
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
