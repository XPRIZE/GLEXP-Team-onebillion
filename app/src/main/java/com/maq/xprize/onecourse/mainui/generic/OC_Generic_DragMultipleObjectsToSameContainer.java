package com.maq.xprize.onecourse.mainui.generic;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 18/01/2017.
 */

public class OC_Generic_DragMultipleObjectsToSameContainer extends OC_Generic_DragObjectsToCorrectPlace
{
    int placedObjectInSameEvent;
    Map<String,PointF> previousObjectPositions;



    public String value_objectPrefix()
    {
        return "obj";
    }

    public String value_containerPrefix()
    {
        return "box";
    }

    public String value_placementsPrefix()
    {
        return "placement";
    }

    public Boolean value_usesDynamicPlacements()
    {
        return true;
    }




    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        String repositionObjectsString = eventAttributes.get("repositionObjects");
        if (previousObjectPositions == null || (repositionObjectsString != null && repositionObjectsString.equals("false"))) previousObjectPositions = new HashMap<>();
        //
        if (repositionObjectsString != null && repositionObjectsString.equals("true"))
        {
            for (OBControl object : filterControls(String.format("%s_.*", value_objectPrefix())))
            {
                PointF previousPosition = previousObjectPositions.get(object.attributes().get("id"));
                if (previousPosition != null)
                {
                    object.setPosition(previousPosition);
                    object.setProperty("originalPosition", OC_Generic.copyPoint(object.position()));
                    object.disable();
                }
            }
        }
        //
        for (OBControl placement : filterControls(String.format("%s_.*", value_placementsPrefix())))
        {
            placement.hide();
        }
        //
        for (OBControl container : filterControls(String.format("%s_.*", value_containerPrefix())))
        {
            String colourString = (String) container.attributes().get("colour");
            if (colourString != null)
            {
                OBPath frame = (OBPath) ((OBGroup)container).objectDict.get("frame");
                if (frame != null)
                {
                    frame.setStrokeColor(OBUtils.colorFromRGBString(colourString));
                }
            }
        }
        placedObjectInSameEvent = 0;
    }



    public Boolean action_isPlacementCorrect(OBControl dragged, OBControl container)
    {
        String correctContainerString = eventAttributes.get("correctContainer");
        if (correctContainerString != null)
        {
            OBControl correctContainer = objectDict.get(String.format("%s_%s", value_containerPrefix(), correctContainerString));
            if (correctContainer == null)
            {
                MainActivity.log("OC_Generic_DragMultipleObjectsToSameContainer:action_isPlacementCorrect:unknown correct container for this event: " + correctContainerString);
                return false;
            }
            return container.equals(correctContainer);
        }
        else
        {
            return container.attributes().get("correct_number").equals(dragged.attributes().get("number"));
        }
    }



    public void action_correctAnswer(OBControl dragged, OBControl container) throws Exception
    {
        placedObjectInSameEvent++;
    }



    public Boolean action_isEventOver()
    {
        String correctPlacementQuantityString = eventAttributes.get("correctQuantity");
        if (correctPlacementQuantityString != null)
        {
            return Integer.parseInt(correctPlacementQuantityString) == placedObjectInSameEvent;
        }
        return false;
    }


    public void action_finalAnimation()
    {
        // nothing to do here
    }


    public void action_moveObjectIntoContainer (OBControl control, OBControl container)
    {
        if (value_usesDynamicPlacements())
        {
            String containerID = (String) container.attributes().get("id");
            OBGroup placement = (OBGroup) objectDict.get(String.format("%s_%s", value_placementsPrefix(), containerID.split("_")[1]));
            //
            List<OBControl> contained = (List<OBControl>) container.propertyValue("contained");
            if (contained == null) contained = new ArrayList<OBControl>();
            //
            if (contained.contains(control))
            {
                action_moveObjectToOriginalPosition(control, false);
            }
            else
            {
                contained.add(control);
                container.setProperty("contained", contained);
                //
                List usedPlacements = new ArrayList();
                if (contained.size() == 1)
                {
                    usedPlacements.add(new Integer(3));
                }
                else if (contained.size() == 2)
                {
                    usedPlacements.add(new Integer(2));
                    usedPlacements.add(new Integer(4));
                }
                else if (contained.size() == 3)
                {
                    usedPlacements.add(new Integer(1));
                    usedPlacements.add(new Integer(3));
                    usedPlacements.add(new Integer(5));
                }
                //
                List<OBAnim> animations = new ArrayList<OBAnim>();
                for (int i = 0; i < contained.size(); i++)
                {
                    OBControl subPlacement = objectDict.get(String.format("%s_%d", placement.attributes().get("id"), usedPlacements.get(i)));
                    PointF newPosition = OC_Generic.copyPoint(subPlacement.getWorldPosition());
                    //
                    OBControl placedObject = contained.get(i);
                    animations.add(OBAnim.moveAnim(newPosition, placedObject));
                    //
                    previousObjectPositions.put((String)placedObject.attributes().get("id"), OC_Generic.copyPoint(newPosition));
                }
                //
                OBAnimationGroup.runAnims(animations, 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            }
        }
        else
        {
            super.action_moveObjectIntoContainer(control, container);
            previousObjectPositions.put((String)control.attributes().get("id"), OC_Generic.copyPoint(control.position()));
        }
    }






    @Override
    public void checkDragAtPoint(PointF pt)
    {
        setStatus(STATUS_AWAITING_CLICK);
        saveStatusClearReplayAudioSetChecking();
        //
        OBControl dragged = target;
        target = null;
        //
        List<OBControl> containers = filterControls(value_containerPrefix() + ".*");
        OBControl container = finger(0, 1, containers, pt, true, true);
        //
        if (container != null)
        {
            if (action_isPlacementCorrect(dragged, container))
            {
                try
                {
                    action_moveObjectIntoContainer(dragged, container);
                    dragged.disable();
                    //
                    gotItRightBigTick(false);
                    //
                    action_correctAnswer(dragged, container);
                    //
                    if (action_isEventOver())
                    {
                        waitAudio();
                        waitForSecs(0.3);
                        //
                        playSceneAudio("CORRECT", true);
                        waitForSecs(0.3);
                        //
                        gotItRightBigTick(true);
                        waitForSecs(0.3);
                        //
                        if (audioSceneExists("FINAL"))
                        {
                            playSceneAudio("FINAL", true);
                            waitForSecs(0.3);
                        }
                        //
                        action_finalAnimation();
                        //
                        nextScene();
                    }
                    else
                    {
                        revertStatusAndReplayAudio();

                    }
                }
                catch (Exception e)
                {
                    System.out.println("OC_Generic_DragMultipleObjectsToSameContainer.exception caught:" + e.toString());
                    e.printStackTrace();
                    //
                    revertStatusAndReplayAudio();
                    System.out.println("OC_Generic_DragMultipleObjectsToSameContainer.setting status to awaiting click");
                }
            }
            else
            {
                gotItWrongWithSfx();
                action_moveObjectToOriginalPosition(dragged, false);
                //
                revertStatusAndReplayAudio();
            }
        }
        else
        {
            action_moveObjectToOriginalPosition(dragged, false);
            //
            revertStatusAndReplayAudio();
        }
    }


    public OBControl findObject (PointF pt)
    {
        targets = filterControls(String.format("%s.*", value_objectPrefix()));
        return finger(-1, 2, targets, pt, true);
    }



    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findObject(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDragTarget(c, pt);
                    }
                });
            }
        }
    }
}
