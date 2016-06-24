package org.onebillion.xprz.mainui.generic;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.x_countingto3.X_CountingTo3_S4f;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class XPRZ_Generic_DragObjectsToCorrectPlace extends XPRZ_Generic_Event
{

    public XPRZ_Generic_DragObjectsToCorrectPlace()
    {
        super();
    }


    public Boolean action_isEventOver()
    {
        List<OBControl> controls = filterControls(action_getObjectPrefix() + ".*");
        for (OBControl control : controls)
        {
            if (control.isEnabled()) return false;
        }
        return true;
    }



    public Boolean action_isPlacementCorrect(OBControl dragged, OBControl container)
    {
        return container.attributes().get("correct_number").equals(dragged.attributes().get("number"));
    }




    public void action_correctAnswer(OBControl dragged, OBControl container) throws Exception
    {
        waitForSecs(0.3);
        //
        int number = Integer.parseInt((String) dragged.attributes().get("number"));
        playAudioQueuedSceneIndex(currentEvent(), "CORRECT", number, false);
        //
        OBControl platform = objectDict.get(String.format("platform_%d", number));
        action_animatePlatform(platform, false);
    }





    @Override
    public void checkDragAtPoint(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        //
        OBControl dragged = target;
        target = null;
        //
        List<OBControl> containers = filterControls(action_getContainerPrefix() + ".*");
        OBControl container = finger(0, 2, containers, pt, true);
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
                        gotItRightBigTick(true);
                        waitForSecs(0.3);
                        //
                        playAudioQueuedScene(currentEvent(), "FINAL", true);
                        //
                        nextScene();
                    }
                    else
                    {
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                }
                catch (Exception e)
                {
                    System.out.println("XPRZ_Generic_DragObjectsToCorrectPlace.exception caught:" + e.toString());
                    e.printStackTrace();
                    //
                    setStatus(STATUS_AWAITING_CLICK);
                    System.out.println("XPRZ_Generic_DragObjectsToCorrectPlace.setting status to awaiting click");
                }
            }
            else
            {
                gotItWrongWithSfx();
                action_moveObjectToOriginalPosition(dragged, false);
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            action_moveObjectToOriginalPosition(dragged, false);
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
    }





    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
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
