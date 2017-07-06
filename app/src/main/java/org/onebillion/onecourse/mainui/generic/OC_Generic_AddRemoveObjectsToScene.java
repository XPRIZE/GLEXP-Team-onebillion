package org.onebillion.onecourse.mainui.generic;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.concurrent.ThreadFactory;

/**
 * OC_Generic_AddRemoveObjectsToScene
 * Generic Event designed for activities where the child has to add or remove objects to the scene by touching a container or the screen
 * <p>
 * Created by pedroloureiro on 21/07/16.
 */
public class OC_Generic_AddRemoveObjectsToScene extends OC_Generic_Event
{
    Boolean isAdd, canUndo;
    int objectDeltaCount;


    public OC_Generic_AddRemoveObjectsToScene (Boolean isAdd, Boolean canUndo)
    {
        this.isAdd = isAdd;
        this.canUndo = canUndo;
    }

    public String getObjectPrefix ()
    {
        return "obj";
    }

    public String getSFX_hideObject ()
    {
        return "hideObject";
    }

    public String getSFX_placeObject ()
    {
        return "placeObject";
    }

    public boolean hasAlreadyVisibleObjects()
    {
        String type = eventAttributes.get("type");
        return (type != null);
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        objectDeltaCount = 0;
        //
        String type = eventAttributes.get("type");
        if (type != null)
        {
            isAdd = type.equalsIgnoreCase("add");
            // do not touch the visibility of the objects for the scenes that have the [type] event property
        }
        else if (isAdd && !hasAlreadyVisibleObjects())
        {
            for (OBControl control : filterControls(getObjectPrefix() + ".*"))
            {
                control.hide();
            }
        }
    }


    public OBControl action_getClosestHiddenObject (PointF pt)
    {
        OBControl bestMatch = null;
        Float bestDistance = null;
        //
        for (OBControl control : filterControls(getObjectPrefix() + ".*"))
        {
            if (control.hidden())
            {
                float distance = OB_Maths.PointDistance(control.getWorldPosition(), pt);
                if (bestDistance == null || bestDistance > distance)
                {
                    bestMatch = control;
                    bestDistance = distance;
                }
            }
        }
        return bestMatch;
    }


    public void action_revealClosestHiddenObject (PointF pt)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            OBControl control = action_getClosestHiddenObject(pt);
            //
            if (hasAlreadyVisibleObjects())
            {
                // if there is a type in the event attributes, then the user needs to touch in the proximity of the object to reveal it
                float distance = OB_Maths.PointDistance(pt, control.position());
                float factorWidth = (control.width() < control.height() * 0.5f) ? control.height() / control.width() : 1.0f;
                float factorHeight = (control.height() < control.width() * 0.5f) ? control.width() / control.height() : 1.0f;
                float threshold = (float) Math.sqrt(control.height() * factorHeight * control.width() * factorWidth) * 0.5f * 1.2f;
                if (distance > threshold)
                {
                    // if the touch is further than 120% of the radius of the object, then it's nulled;
                    control = null;
                }
            }
            //
            if (control != null)
            {
                action_revealObject(control, pt);
                //
                check_correctNumberObjectsShown_viaShow();
            }
            else
            {
                revertStatusAndReplayAudio();
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void action_hideObject (OBControl control)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            if (control != null)
            {
                playSfxAudio(getSFX_hideObject(), false);
                control.hide();
                //
                action_moveObjectToOriginalPosition(control, false);
                //
                action_playCorrectAudioAfterHidingObject();
                objectDeltaCount++;
                //
                check_correctNumberObjectsShown_viaHide();
            }
            else
            {
                revertStatusAndReplayAudio();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void action_playCorrectAudioAfterHidingObject () throws Exception
    {
        playSceneAudioIndex("CORRECT", objectDeltaCount, false);
    }


    public void check_correctNumberObjectsShown_viaHide () throws Exception
    {
        if (getTotalShownObjects() == 0)
        {
            waitForSecs(0.7);
            //
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            action_finale();
        }
        else
        {
            revertStatusAndReplayAudio();
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public void check_correctNumberObjectsShown_viaShow () throws Exception
    {
        if (getTotalHiddenObjects() == 0)
        {
            waitForSecs(0.7);
            //
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            action_finale();
        }
        else
        {
            revertStatusAndReplayAudio();
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public int getTotalHiddenObjects ()
    {
        int count = 0;
        for (OBControl control : filterControls(getObjectPrefix() + ".*"))
        {
            if (control.hidden()) count++;
        }
//        MainActivity.log("hidden objects " + count);
        return count;
    }


    public int getTotalShownObjects ()
    {
        int count = 0;
        for (OBControl control : filterControls(getObjectPrefix() + ".*"))
        {
            if (!control.hidden()) count++;
        }
//        MainActivity.log("shown objects " + count);
        return count;
    }


    public void action_revealObject (OBControl control, PointF pt) throws Exception
    {
        control.setPosition(pt);
        playSfxAudio(getSFX_placeObject(), false);
        control.show();
        //
        OBAnim anim = OBAnim.moveAnim((PointF) control.propertyValue("originalPosition"), control);
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        //
        playSceneAudioIndex("CORRECT", objectDeltaCount, false);
        objectDeltaCount++;
    }


    public void action_finale () throws Exception
    {
        performSel("finalAnimation", currentEvent());
        waitForSecs(0.3);
        //
        playSceneAudio("FINAL", true);
        waitForSecs(0.7);
        //
        nextScene();
    }


    @Override
    public OBControl findTarget (PointF pt)
    {
        return finger(0, 1, filterControls(getObjectPrefix() + ".*"), pt, true);
    }


    public Boolean condition_isValidTouch (PointF pt)
    {
        return true;
    }


    @Override
    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl control = findTarget(pt);
            if (control != null)
            {
                if (isAdd && canUndo || !isAdd)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            action_hideObject(control);
                        }
                    });
                    return;
                }
            }
            if (isAdd)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        if (condition_isValidTouch(pt))
                        {
                            action_revealClosestHiddenObject(pt);
                        }
                    }
                });
            }
        }
    }
}
