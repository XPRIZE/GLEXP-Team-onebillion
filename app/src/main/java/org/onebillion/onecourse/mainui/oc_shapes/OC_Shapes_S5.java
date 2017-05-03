package org.onebillion.onecourse.mainui.oc_shapes;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.EnumSet;
import java.util.List;

import static org.onebillion.onecourse.utils.OBUtils.RectOverlapRatio;

/**
 * Created by pedroloureiro on 03/05/2017.
 */

public class OC_Shapes_S5 extends OC_Generic_Event
{
    int correctPlacements;

    public void demo5a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look.
        OC_Generic.pointer_moveToObjectByName("container", -15, 0.5f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO", 1, false); // This one goes …
        OC_Generic.pointer_moveToObjectByName("obj_3", -5, 0.5f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO", 2, false); // … here.
        OBControl control = objectDict.get("obj_3");
        OBControl place = objectDict.get("place_3");
        OC_Generic.pointer_moveToPointWithObject(control, place.position(), -25, 0.4f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        gotItRightBigTick(false);
        waitSFX();
        thePointer.hide();
        waitForSecs(0.7f);
        nextScene();

    }


    public void fin5g ()
    {
        lockScreen();
        objectDict.get("container").hide();
        objectDict.get("containerComplete").show();
        hideControls("obj.*");
        unlockScreen();
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        hideControls("place.*");
        correctPlacements = 0;
        for (OBControl control : filterControls("obj.*"))
        {
            control.setProperty("currentPosition", OC_Generic.copyPoint(control.position()));

        }

    }

    public void checkDragTarget (OBControl targ, PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target = targ;
        OC_Generic.sendObjectToTop(targ, this);
        targ.animationKey = (long) (OC_Generic.currentTime());
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);

    }

    public void checkDragAtPoint (PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBControl targetControl = target;
            target = null;
            List<OBControl> placements = filterControls("place.*");
            for (OBControl place : placements)
            {
                if (RectOverlapRatio(place.frame, targetControl.frame) > 0.3)
                {
                    if (place.attributes().get("type").equals(targetControl.attributes().get("type")))
                    {
                        targetControl.disable();
                        gotItRightBigTick(false);
                        targetControl.moveToPoint(place.position(), 0.1f, true);
                        correctPlacements++;
                        if (correctPlacements == placements.size())
                        {
                            waitSFX();
                            waitForSecs(0.1f);
                            //
                            gotItRightBigTick(true);
                            waitForSecs(0.3f);
                            //
                            playAudioQueuedScene("CORRECT", 300, true);
                            waitForSecs(0.3f);
                            //
                            String finalDemo = String.format("fin%s", currentEvent());
                            performSel(finalDemo, "");
                            playSceneAudio("FINAL", true);
                            //
                            nextScene();
                        }
                        else
                        {
                            setStatus(STATUS_AWAITING_CLICK);

                        }
                        return;
                    }
                }
            }
            gotItWrongWithSfx();
            PointF destination = (PointF) targetControl.propertyValue("currentPosition");
            targetControl.moveToPoint(destination, 0.3f, true);
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public OBControl findTarget (PointF pt)
    {
        OBControl c = finger(0, 1, filterControls("obj.*"), pt, true);
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
