package com.maq.xprize.onecourse.hindi.mainui.oc_counting4to6;

import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic_DragNumbersToSlots;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pedroloureiro on 02/05/2017.
 */

public class OC_Counting4To6_S4e extends OC_Generic_DragNumbersToSlots
{
    HashMap<String, OBControl> slots;
    HashMap<String, PointF> offsets;


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "object";
    }

    @Override
    public String action_getNumberPrefix ()
    {
        return "number";
    }

    public void demo4e () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false); //  Now look.
        OC_Generic.pointer_moveToObjectByName("object_3", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); //  Three oranges.
        OC_Generic.pointer_moveToObjectByName("object_3", -25, 0.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OBControl control = objectDict.get("object_3");
        OBControl replacement = objectDict.get("object_4");
        PointF destination = OB_Maths.locationForRect(new PointF(0.75f, 0.25f), objectDict.get("box_3").frame());
        playAudioScene("DEMO", 2, false); //  Three.
        OC_Generic.sendObjectToTop(control, this);
        OC_Generic.pointer_moveToPointWithObject(control, destination, -10, 0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_swapObjects(control, replacement, true);
        waitForSecs(0.7f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        action_swapObjects(control, replacement, false);
        control.enable();
        replacement.enable();
        waitForSecs(0.3f);
        //
        doAudio(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }


    public OBControl containerForObject (OBControl control)
    {
        for (String containerID : slots.keySet())
        {
            if (slots.get(containerID) == control) return objectDict.get(containerID);

        }
        return null;

    }

    public PointF objectPositionFromContainer (OBControl object, OBControl container)
    {
        return OB_Maths.AddPoints(container.position(), offsets.get(object.attributes().get("id")));
    }

    public boolean objectMatchesContainer (OBControl object)
    {
        OBControl container = containerForObject(object);
        int containerNumber = Integer.parseInt((String) container.attributes().get("correctNumber"));
        int objectNumber = Integer.parseInt((String) object.attributes().get("number"));
        return (containerNumber == objectNumber);
    }

    public void action_correctAnswer (final OBControl target, final OBControl otherTarget)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                if (objectMatchesContainer(target) || objectMatchesContainer(otherTarget))
                {
                    if (objectMatchesContainer(target)) target.disable();
                    if (objectMatchesContainer(otherTarget)) otherTarget.disable();
                    //
                    waitForSecs(0.3f);
                    gotItRightBigTick(false);
                    waitSFX();
                    //
                    if (objectMatchesContainer(target)) playAudioScene("CORRECT", Integer.parseInt((String) target.attributes().get("number")) - 4, true);
                    else if (objectMatchesContainer(otherTarget)) playAudioScene("CORRECT", Integer.parseInt((String) otherTarget.attributes().get("number")) - 4, true);
                }
            }
        });
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        slots = new HashMap<>();
        offsets = new HashMap<>();
        String[] sequence = eventAttributes.get("sequence").split(",");
        for (int i = 1; i <= sequence.length; i++)
        {
            String index = sequence[i - 1];
            OBControl box = objectDict.get(String.format("box_%d", i));
            OBControl frame = objectDict.get(String.format("frame_%s", index));
            OBControl object = objectDict.get(String.format("object_%s", index));
            //
            offsets.put((String) object.attributes().get("id"), getAbsoluteOffsetWithParent(frame));
            object.setPosition(objectPositionFromContainer(object, box));
            //
            slots.put((String) box.attributes().get("id"), object);
        }
        List<OBControl> controls = filterControls("object.*");
        for (OBControl control : controls)
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.position()));
        }
    }

    public void action_swapObjects (OBControl control1, OBControl control2, boolean forDemo) throws Exception
    {
        OBControl container1 = containerForObject(control1);
        OBControl container2 = containerForObject(control2);
        PointF destination1 = objectPositionFromContainer(control1, container2);
        PointF destination2 = objectPositionFromContainer(control2, container1);
        //
        control1.setProperty("originalPosition", destination1);
        control2.setProperty("originalPosition", destination2);
        playSfxAudio("swap", false);
        float newZPosition = OC_Generic.sendObjectToTop(control1, this);
        //
        if (forDemo)
        {
            moveObjectToPoint(control1, destination1, 0.1f, false);
        }
        else
        {
            moveObjectToPoint(control1, destination1, 0.3f, false);
        }
        //
        control2.setZPosition(newZPosition - 0.0001f);
        moveObjectToPoint(control2, destination2, 0.3f, false);
        //
        slots.put((String) container2.attributes().get("id"), control1);
        slots.put((String) container1.attributes().get("id"), control2);
        //
        if (objectMatchesContainer(control1)) control1.disable();
        if (objectMatchesContainer(control2)) control2.disable();

    }

    public boolean condition_isEventOver ()
    {
        for (OBControl control : slots.values())
        {
            if (!objectMatchesContainer(control)) return false;
        }
        return true;
    }

    public void showFinal (int index) throws Exception
    {
        OBGroup number = (OBGroup) objectDict.get(String.format("number_%d", index));
        OBGroup object = (OBGroup) objectDict.get(String.format("object_%d", index));
        OBGroup finalGroup = (OBGroup) objectDict.get(String.format("final_%d", index));
        //
        playSfxAudio(String.format("plink_%d", index), false);
        //
        lockScreen();
        object.objectDict.get(String.format("frame_%d", index)).hide();
        number.members.get(1).hide();
        finalGroup.show();
        unlockScreen();
    }

    public void animate_final () throws Exception
    {
        for (int i = 1; i <= 6; i++)
        {
            showFinal(i);
            waitSFX();
            waitForSecs(0.1f);
        }
    }


    public OBControl getEnabledObjectUnderFinger (PointF pt)
    {
        List<OBControl> controls = filterControls("object.*");
        for (OBControl control : controls)
        {
            if (control == target) continue;
            if (!control.isEnabled()) continue;
            if (control.frame.contains(pt.x, pt.y)) return control;
        }
        return null;
    }


    public void checkDragAtPoint (final PointF pt)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                setStatus(STATUS_BUSY);
                final OBControl alreadyPlacedControl = getEnabledObjectUnderFinger(pt);
                final OBControl dragged = target;
                target = null;
                if (alreadyPlacedControl != null)
                {
                    action_swapObjects(dragged, alreadyPlacedControl, false);
                    if (condition_isEventOver())
                    {
                        action_correctAnswer(dragged);
                        waitForSecs(0.3f);
                        //
                        gotItRightBigTick(true);
                        waitForSecs(0.3f);
                        //
                        animate_final();
                        waitForSecs(0.3f);
                        //
                        playSceneAudio("FINAL", true);
                        waitForSecs(0.3f);
                        //
                        nextScene();
                        return;
                    }
                    else
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                action_correctAnswer(dragged, alreadyPlacedControl);
                            }
                        });
                    }
                }
                else
                {
                    moveObjectToOriginalPosition(dragged, true, false);
                }
                setStatus(STATUS_AWAITING_CLICK);
            }
        });
    }


}

