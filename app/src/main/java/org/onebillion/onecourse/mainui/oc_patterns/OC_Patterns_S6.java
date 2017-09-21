package org.onebillion.onecourse.mainui.oc_patterns;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Patterns_S6 extends OC_Generic_Event
{
    int totalObjects;
    int placedObjects;

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        List<OBControl> controls = filterControls("obj.*");
        for (OBControl control : controls)
        {
            if (OBGroup.class.isInstance(control))
            {
                OBGroup group = (OBGroup) control;
                group.outdent(10f);
            }
        }
        totalObjects = (int) controls.size();
        placedObjects = 0;
        //
        for (OBControl control : filterControls("complete.*"))
        {
            control.show();
            control.setOpacity(0.0f);
        }
        for (OBControl control : filterControls("back.*"))
        {
            control.show();
            control.setOpacity(0.0f);

        }
        hideControls("place.*");
        showControls("obj.*");
        showControls("dotted.*");

    }

    public void setScene6d ()
    {
        setSceneXX(currentEvent());
        for (OBGroup control : (List<OBGroup>) (Object) filterControls("dotted.*"))
        {
            for (OBControl layer : control.objectDict.values())
            {
                layer.hide();
            }
            OBControl dotted = control.objectDict.get("dotted");
            dotted.show();
        }
    }

    public void finalDemo6d ()
    {
        List<OBAnim> animations = new ArrayList<>();
        for (OBControl control : filterControls("dotted.*"))
        {
            OBAnim anim = OBAnim.opacityAnim(0.0f, control);
            animations.add(anim);

        }
        for (OBControl control : filterControls("complete.*"))
        {
            OC_Generic.sendObjectToTop(control, this);
            OBAnim anim = OBAnim.opacityAnim(1.0f, control);
            animations.add(anim);

        }
        for (OBControl control : filterControls("back.*"))
        {
            OBAnim anim = OBAnim.opacityAnim(1.0f, control);
            animations.add(anim);

        }
        lockScreen();
        OBControl bus = objectDict.get("obj_5");
        OC_Generic.sendObjectToTop(bus, this);
        unlockScreen();
        //
        OBAnimationGroup.runAnims(animations, 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public OBControl getBestPlacementMatch (PointF pt)
    {
        lockScreen();
        showControls("place.*");
        OBControl placement = finger(0, 2, filterControls("place.*"), pt, true);
        hideControls("place.*");
        unlockScreen();
        //
        return placement;
    }

    public void checkDragAtPoint (PointF pt)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            OBControl placement = getBestPlacementMatch(pt);
            OBControl control = target;
            target = null;
            //
            if (placement != null)
            {
                if (placement.attributes().get("type").equals(control.attributes().get("type")))
                {
                    gotItRightBigTick(false);
                    placement.disable();
                    control.disable();
                    control.moveToPoint(placement.position(), 0.1f, true);
                    //
                    placedObjects++;
                    if (placedObjects >= totalObjects)
                    {
                        waitSFX();
                        gotItRightBigTick(true);
                        waitForSecs(0.3f);
                        //
                        playSfxAudio("reveal", false);
                        //
                        if (!performSel("finalDemo", currentEvent()))
                        {
                            List<OBAnim> animations = new ArrayList<>();
                            for (OBControl controlAlt : filterControls("dotted.*"))
                            {
                                OBAnim anim = OBAnim.opacityAnim(0.0f, controlAlt);
                                animations.add(anim);

                            }
                            for (OBControl controlAlt : filterControls("complete.*"))
                            {
                                OBAnim anim = OBAnim.opacityAnim(1.0f, controlAlt);
                                animations.add(anim);

                            }
                            for (OBControl controlAlt : filterControls("back.*"))
                            {
                                OBAnim anim = OBAnim.opacityAnim(1.0f, controlAlt);
                                animations.add(anim);

                            }
                            OBAnimationGroup.runAnims(animations, 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                        }
                        waitSFX();
                        //
                        playAudioQueuedScene("CORRECT", true);
                        waitForSecs(0.7f);
                        //
                        playAudioQueuedScene("FINAL", true);
                        //
                        nextScene();
                        //
                        return;
                    }
                }
                else
                {
                    gotItWrongWithSfx();
                    PointF value = (PointF) control.propertyValue("originalPosition");
                    control.moveToPoint(value, 0.1f, true);
                }
            }
            else
            {
                PointF value = (PointF) control.propertyValue("originalPosition");
                control.moveToPoint(value, 0.1f, true);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Patterns_S6:checkDragAtPoint:exception caught");
            e.printStackTrace();
        }
        //
        revertStatusAndReplayAudio();
        setStatus(STATUS_AWAITING_CLICK);
    }


    public Object findObject (PointF pt)
    {
        return finger(0, 2, filterControls("obj.*"), pt, true);

    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl obj = (OBControl) findObject(pt);
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
        super.checkDragTarget(targ, pt);
        OC_Generic.sendObjectToTop(targ, this);
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


