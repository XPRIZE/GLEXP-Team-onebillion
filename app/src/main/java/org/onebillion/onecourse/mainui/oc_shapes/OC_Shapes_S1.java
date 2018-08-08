package org.onebillion.onecourse.mainui.oc_shapes;

import android.graphics.PointF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.onebillion.onecourse.utils.OBAnim.ANIM_EASE_IN_EASE_OUT;
import static org.onebillion.onecourse.utils.OBAnim.ANIM_EASE_OUT;

/**
 * Created by pedroloureiro on 02/05/2017.
 */

public class OC_Shapes_S1 extends OC_Generic_Event
{
    int currentPlace;
    List<OBControl> objectSequence;


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        objectSequence = new ArrayList<>();
        hideControls("place.*");
        for (OBControl control : sortedFilteredControls("obj.*"))
        {
            String lockedValue = (String) control.attributes().get("locked");
            if (lockedValue != null && lockedValue.equalsIgnoreCase("true"))
            {
                objectSequence.add(control);
                control.disable();
            }
            else
            {
                control.setProperty("currentPosition", OC_Generic.copyPoint(control.position()));
            }
        }
        currentPlace = 1;
        action_moveDashToPlacement(false);
    }

    public boolean action_isPlacementOver ()
    {
        List places = filterControls("place.*");
        return currentPlace >= places.size();
    }

    public void action_moveDashToPlacement (boolean withAnimation)
    {
        float bottom = -1;
        for (OBControl control : filterControls("place_.*"))
        {
            bottom = Math.max(control.bottom(), bottom);
        }
        bottom += bounds().height() * 0.04;
        OBControl place = objectDict.get(String.format("place_%d", currentPlace));
        OBControl dash = objectDict.get("dash");
        PointF destination = OC_Generic.copyPoint(place.getWorldPosition());
        destination.y = bottom;
        if (withAnimation)
        {
            OBAnim moveAnim = OBAnim.moveAnim(destination, dash);
            OBAnimationGroup.runAnims(Collections.singletonList(moveAnim), 0.2, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        else
        {
            dash.setPosition(destination);
        }

    }

    public void demo1a () throws Exception
    {
        setStatus(STATUS_BUSY);
        demoButtons();
        playAudioScene("DEMO", 0, false); // Now look.
        OC_Generic.pointer_moveToObjectByName("obj_1", -25, 0.8f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, true); // Pink.
        OC_Generic.pointer_moveToObjectByName("obj_2", -20, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        //
        playAudioScene("DEMO", 2, true); // Yellow.
        OC_Generic.pointer_moveToObjectByName("obj_3", -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        //
        playAudioScene("DEMO", 3, true); // Pink.
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 4, false); // Yellow goes next.
        OC_Generic.pointer_moveToObjectByName("obj_4", -20, 0.5f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        OBControl place = objectDict.get("place_1");
        OBControl control = objectDict.get("obj_4");
        //
        lockScreen();
        OC_Generic.sendObjectToTop(control, this);
        OC_Generic.sendObjectToTop(thePointer, this);
        unlockScreen();
        //
        OC_Generic.pointer_moveToPointWithObject(control, place.position(), -10, 0.5f, true, this);
        OC_Generic.pointer_moveToObjectByName("obj_4", -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
        playSfxAudio("correct", false);
        currentPlace++;
        action_moveDashToPlacement(true);
        waitSFX();
        waitForSecs(0.7f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void checkDropAtPoint (PointF pt) throws Exception
    {
        setStatus(STATUS_BUSY);
        OBControl control = (OBControl) target;
        OBControl correctPlaceControl = objectDict.get(String.format("place_%d", currentPlace));
        OBControl bottomBar = objectDict.get("bottom_bar");
        if (!bottomBar.frame.contains(pt.x, pt.y)) // outside resting position;
        {
            String control_type = (String) control.attributes().get("type");
            String place_type = (String) correctPlaceControl.attributes().get("type");
            if (control_type.equals(place_type)) // correct type;
            {
                correctPlaceControl.disable();
                control.disable();
                //
                objectSequence.add(control);
                gotItRightBigTick(false);
                //
                control.moveToPoint(correctPlaceControl.position(), 0.2f, true);
                playAudioQueuedScene("CORRECT", 0.3f, false);
                //
                if (action_isPlacementOver())
                {
                    hideControls("dash");
                    waitSFX();
                    waitForSecs(0.3f);
                    //
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    float factor = 1.2f;
                    for (OBControl animatedControl : objectSequence)
                    {
                        String key = String.format("type_%s", animatedControl.attributes().get("type"));
                        String note = eventAttributes.get(key);
                        float scaleX = animatedControl.scaleX;
                        float scaleY = animatedControl.scaleY;
                        //
                        lockScreen();
                        OC_Generic.sendObjectToTop(control, this);
                        animatedControl.setScaleX(animatedControl.scaleX * factor);
                        animatedControl.setScaleY(animatedControl.scaleY * factor);
                        unlockScreen();
                        //
                        playSfxAudio(note, true);
                        //
                        lockScreen();
                        animatedControl.setScaleX(scaleX);
                        animatedControl.setScaleY(scaleY);
                        unlockScreen();
                        //
                        waitForSecs(0.025f);
                    }
                    waitForSecs(0.3f);
                    //
                    playAudioQueuedScene("FINAL", 0.3f, true);
                    nextScene();
                }
                else
                {
                    currentPlace++;
                    action_moveDashToPlacement(true);
                }
            }
            else
            {
                gotItWrongWithSfx();
                PointF destination = (PointF) control.propertyValue("currentPosition");
                control.moveToPoint(destination, 0.3f, true);
            }
        }
        else
        {
            PointF destination = (PointF) control.propertyValue("currentPosition");
            control.moveToPoint(destination, 0.3f, true);
        }
        setStatus(STATUS_AWAITING_CLICK);

    }

    public Object findObject (PointF pt)
    {
        return finger(0, 2, filterControls("obj.*"), pt, true);
    }


    public void checkDragTarget(OBControl targ,PointF pt)
    {
        super.checkDragTarget(targ, pt);
        //
        OC_Generic.sendObjectToTop(targ, this);
    }

    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING) setStatus(STATUS_AWAITING_CLICK);
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = (OBControl) findObject(pt);
            if (targ != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkDragTarget(targ, pt);
                    }
                });
            }
        }
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
