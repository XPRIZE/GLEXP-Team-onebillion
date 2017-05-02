package org.onebillion.onecourse.mainui.generic;

import android.graphics.PointF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;

import static org.onebillion.onecourse.utils.OB_Maths.DiffPoints;

/**
 * Created by pedroloureiro on 30/03/2017.
 */

public class OC_Generic_DragNumbersToSlots extends OC_Generic_Event
{

    public void doMainXX () throws Exception
    {
        playSceneAudioIndex("DEMO", 0, true);
        doAudio(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void action_correctAnswer (OBControl target) throws Exception
    {
        gotItRightBigTick(false);
        playAudioScene("CORRECT", Integer.parseInt((String) target.attributes().get("number")), true);
    }


    public void action_wrongAnswer (OBControl target)
    {
        try
        {
            gotItWrongWithSfx();
            moveObjectToOriginalPosition(target, false, false);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_DragNumbersToSlots:action_wrongAnswer:exception caught");
            e.printStackTrace();
        }
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        action_addLabelsToObjects(String.format("%s.*", action_getNumberPrefix()));
    }


    public void action_addLabelsToObjects(String pattern)
    {
        List<OBControl> numbers = filterControls(pattern);
        List<OBLabel> createdLabels = new ArrayList<>();
        float smallestFontSize = 1000000000;
        //
        for (OBControl number : numbers)
        {
            OBLabel label = action_createLabelForControl(number, 1.2f);
            if (label.fontSize() < smallestFontSize) smallestFontSize = label.fontSize();
            //
            createdLabels.add(label);
        }
        //
        for (OBLabel label : createdLabels)
        {
            label.setFontSize(smallestFontSize);
            label.sizeToBoundingBox();
        }
    }

    public void action_placeObjectInContainer (OBControl target, OBControl container)
    {
        try
        {
            if (action_addObjectAndRearrangeContainer(target, container, 1, false))
            {
                target.disable();
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_DragNumbersToSlots:action_placeObjectInContainer:exception caught");
            e.printStackTrace();
        }
    }


    public boolean condition_isEventOver ()
    {
        List<OBControl> boxes = filterControls(String.format("%s.*", action_getContainerPrefix()));
        List<OBControl> allNumbers = filterControls(String.format("%s.*", action_getObjectPrefix()));
        //
        List<OBControl> enabledNumbers = new ArrayList();
        for (OBControl number : allNumbers)
        {
            if (number.isEnabled())
            {
                enabledNumbers.add(number);
            }
        }
        //
        return (boxes.size() == allNumbers.size() - enabledNumbers.size());
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }

    public String action_getNumberPrefix ()
    {
        return "number";
    }


    @Override
    public String action_getContainerPrefix ()
    {
        return "box";
    }


    public void checkDragAtPoint (PointF pt)
    {
        try
        {
            saveStatusClearReplayAudioSetChecking();
            //
            List<OBControl> containers = filterControls(String.format("%s.*", action_getContainerPrefix()));
            OBControl container = finger(0, 2, containers, pt);
            final OBControl number = target;
            target = null;
            //
            if (container != null)
            {
                playSfxAudio("drop_number", false);
                String correctNumberValue = (String) container.attributes().get("correct_number");
                String numberValue = (String) number.attributes().get("number");
                if (correctNumberValue.equalsIgnoreCase(numberValue))
                {
                    action_placeObjectInContainer(number, container);
                    //
                    if (condition_isEventOver())
                    {
                        waitForSecs(0.3f);
                        //
                        gotItRightBigTick(false);
                        waitSFX();
                        //
                        action_correctAnswer(number);
                        waitForSecs(0.3f);
                        //
                        gotItRightBigTick(true);
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
                        OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                gotItRightBigTick(false);
                                waitSFX();
                                action_correctAnswer(number);
                            }
                        });
                    }
                }
                else
                {
                    action_wrongAnswer(number);
                }
            }
            else
            {
                moveObjectToOriginalPosition(number, true, false);
            }
            revertStatusAndReplayAudio();
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_DragNumbersToSlots:checkDragAtPoint:exception caught");
            e.printStackTrace();
        }
    }


    public OBControl findTarget (PointF pt)
    {
        List<OBControl> objs = filterControls(String.format("%s.*", action_getObjectPrefix()));
        OBControl c = finger(0, 1, objs, pt, true);
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
        targ.animationKey = SystemClock.uptimeMillis();
        dragOffset = DiffPoints(targ.position(), pt);
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