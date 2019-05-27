package com.maq.xprize.onecourse.mainui.oc_sharing;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBConfigManager;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by pedroloureiro on 05/05/2017.
 */

public class OC_Sharing_S2 extends OC_Generic_Event
{
    Map<String, List> containers;
    List<OBLabel> numbers;
    int maxCapacity;
    String placementType;

    public void setSceneXX (String scene)
    {
        deleteControls(String.format("%s.*", action_getObjectPrefix()));
        super.setSceneXX(scene);
        for (OBGroup container : (List<OBGroup>) (Object) filterControls(String.format("%s.*", action_getContainerPrefix())))
        {
            container.showMembers(".*");
            container.hideMembers("smile_happy");
            container.hideMembers("drop");
            container.hideMembers("place.*");
        }
        if (numbers != null)
        {
            for (OBLabel number : numbers)
            {
                number.hide();
            }
            action_hiliteNumber(null);
        }
        else
        {
            numbers = new ArrayList();
            for (OBControl number : filterControls("number.*"))
            {
                OBLabel label = action_createLabelForControl(number, 1.2f, false);
                label.setProperty("number", number.attributes().get("number"));
                numbers.add(label);
                number.hide();
                label.hide();
            }
        }
    }

    public void demo2a () throws Exception
    {
        List audio = (List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT");
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
        playAudioQueued(audio, false);
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void midDemo2a () throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Now … how many clocks does each child have?;
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.6f), bounds()), -15, 0.9f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("number_1", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        playAudioScene("DEMO", 1, false); // Touch the number.;
        OC_Generic.pointer_moveToObjectByName("number_5", 10, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.3f);
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
        int total_objects = filterControls(String.format("%s_.*", action_getObjectPrefix())).size();
        int total_containers = (int) filterControls(String.format("%s_.*", action_getContainerPrefix())).size();
        maxCapacity = (total_containers == 0) ? 0 : (int) total_objects / total_containers;
        placementType = eventAttributes.get("placement");
        containers = new HashMap<>();
        int skinColourOffset = 0;
        for (OBGroup container : (List<OBGroup>) (Object) filterControls(String.format("%s_.*", action_getContainerPrefix())))
        {
            containers.put((String) container.attributes().get("id"), new ArrayList(maxCapacity));
            container.objectDict.get("smile_normal").show();
            container.objectDict.get("smile_happy").hide();
            int childColour = OBConfigManager.sharedManager.getSkinColour(skinColourOffset++);
            container.substituteFillForAllMembers("colour.*", childColour);
            //
            for (OBControl placement : container.filterMembers("place.*"))
            {
                placement.enable();
            }
        }
        for (OBControl control : filterControls(String.format("%s_.*", action_getObjectPrefix())))
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.position()));
            control.setProperty("originalScale", control.scale());
        }
    }


    public boolean action_isContainerAvailable (OBControl container)
    {
        return (containers.get(container.attributes().get("id")).size() < maxCapacity);
    }


    public void action_placeObjectInContainer (OBControl object, OBGroup container) throws Exception
    {
        List<OBControl> placements = container.filterMembers("place.*");
        float bestDistance = -1;
        OBControl bestPlacement = null;
        //
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
        //
        if (bestPlacement != null)
        {
            object.disable();
            bestPlacement.disable();
            playSfxAudio("place_object", false);
            moveAndScaleObject((OBGroup) object, bestPlacement);
            containers.get(container.attributes().get("id")).add(object);
            sortObjectsInContainer(container);
        }
        else
        {
            MainActivity.log("failsafe, if no placement is found, then return object to original location");
            action_returnObject(object);
        }
        if (!action_isContainerAvailable(container))
        {
            lockScreen();
            container.objectDict.get("smile_normal").hide();
            container.objectDict.get("smile_happy").show();
            unlockScreen();
        }
    }


    public void sortObjectsInContainer (OBGroup container)
    {
        List<OBControl> arr = containers.get(container.attributes().get("id"));
        Collections.sort(arr, new Comparator<OBControl>()
        {
            @Override
            public int compare (OBControl obj1, OBControl obj2)
            {
                return obj1.YPositionCompare(obj2);
            }
        });
        Collections.reverse(arr);
        //
        lockScreen();
        for (OBControl control : arr)
        {
            OC_Generic.sendObjectToTop(control, this);
        }
        unlockScreen();
    }


    public float action_getOverlap (OBControl first, OBControl second)
    {
        float width = Math.min(first.right(), second.right()) - Math.max(first.left(), second.left());
        if (width < 0) return 0;
        float height = Math.min(first.bottom(), second.bottom()) - Math.max(first.top(), second.top());
        if (height < 0) return 0;
        float overlap = (height * width) / (first.height() * first.width());
        return overlap;
    }


    public void moveAndScaleObject (OBGroup control, OBControl placement)
    {
        OBControl bottomBar = objectDict.get("background_bottom");
        PointF destination = placement.getWorldPosition();
        if (control.objectDict.get("placed") != null)
        {
            lockScreen();
            control.objectDict.get("normal").hide();
            control.objectDict.get("placed").show();
            unlockScreen();
        }
        //
        float factor = Math.min(0, ((destination.y - bottomBar.top()) / bounds().height()) * 1.2f);
        float oldScale = (float) control.propertyValue("originalScale");
        float newScale = oldScale * (1 + factor);
        if (placementType.equals("bottom"))
        {
            float newHeight = newScale * control.height() / oldScale;
            destination.y = destination.y + placement.height() / 2 - newHeight / 1.5f;
        }
        OBAnim anim1 = OBAnim.moveAnim(destination, control);
        OBAnim anim2 = OBAnim.scaleAnim(newScale, control);
        OBAnimationGroup.runAnims(Arrays.asList(anim1, anim2), 0.3, true, OBAnim.ANIM_LINEAR, this);
    }


    public boolean action_isPlacementOver ()
    {
        List<OBControl> controls = filterControls(String.format("%s_.*", action_getObjectPrefix()));
        for (OBControl control : controls)
        {
            if (control.isEnabled()) return false;
        }
        return true;
    }


    public void action_fin () throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3f);
        //
        playSceneAudio("CORRECT", true);
        waitForSecs(0.3f);
        //
        playSceneAudio("FINAL", true);
        waitForSecs(0.3f);
        //
        nextScene();
    }


    public void action_wrongAnswer (OBControl target) throws Exception
    {
        gotItWrongWithSfx();
        playSceneAudio("INCORRECT", false);
    }


    public void action_returnObject (OBControl object) throws Exception
    {
        PointF originalPosition = (PointF) object.propertyValue("originalPosition");
        object.setScale((float) object.propertyValue("originalScale"));
        moveObjectToPoint(object, originalPosition, 0.3f, false);
    }


    public void action_hiliteNumber (OBLabel number)
    {
        lockScreen();
        for (OBLabel label : numbers)
        {
            if (number != null && number.equals(label))
            {
                label.setColour(Color.RED);
            }
            else
            {
                label.setColour(Color.BLACK);
            }
        }
        unlockScreen();
    }

    public void checkDropAtPoint (PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBControl object = target;
            target = null;
            OBGroup container = (OBGroup) findContainer(pt);
            if (container != null)
            {
                if (action_isContainerAvailable(container))
                {
                    action_placeObjectInContainer(object, container);
                    if (action_isPlacementOver())
                    {
                        gotItRightBigTick(false);
                        //
                        waitSFX();
                        //
                        lockScreen();
                        for (OBControl number : numbers) number.show();
                        unlockScreen();
                        //
                        if (!performSel("midDemo", currentEvent()))
                        {
                            setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
                            playAudioQueuedScene("PROMPT2", 0.3f, false);
                        }
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

    public void checkNumber (OBControl number)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            action_hiliteNumber((OBLabel) number);
            int selectedValue = Integer.parseInt((String)number.propertyValue("number"));
            if (selectedValue == maxCapacity)
            {
                action_fin();
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.3f);
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
        List<OBControl> objs = filterControls(String.format("%s.*", action_getObjectPrefix()));
        OBControl c = finger(0, 2, objs, pt, true);
        return c;
    }


    public Object findDropZone (PointF pt, OBGroup container)
    {
        OBControl control;
        //
        lockScreen();
        container.showMembers("drop");
        control = finger(0, 2, container.filterMembers("drop.*"), pt);
        container.hideMembers("drop");
        unlockScreen();
        //
        return control;
    }

    public OBControl findContainer (PointF pt)
    {
        return finger(2, 2, filterControls(String.format("%s.*", action_getContainerPrefix())), pt);
    }

    public OBControl findNumber (PointF pt)
    {
        return finger(0, 2, (List<OBControl>) (Object) numbers, pt);

    }

    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (action_isPlacementOver())
            {
                final OBControl obj = findNumber(pt);
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
                    checkDropAtPoint(pt);
                }
            });
        }
    }


}
