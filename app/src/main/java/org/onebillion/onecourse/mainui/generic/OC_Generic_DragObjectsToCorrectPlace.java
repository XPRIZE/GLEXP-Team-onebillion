package org.onebillion.onecourse.mainui.generic;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.List;

/**
 * OC_Generic_DragObjectsToCorrectPlace
 * Generic Event designed for Activities where the Child needs to drag objects to the correct containers
 *
 * Created by pedroloureiro on 23/06/16.
 */
public class OC_Generic_DragObjectsToCorrectPlace extends OC_Generic_Event
{

    public OC_Generic_DragObjectsToCorrectPlace()
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
        waitForSecs(0.1);
        //
        OBControl platform = objectDict.get(String.format("platform_%d", number));
        action_animatePlatform(platform, false);
    }



    public void action_finalAnimation() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "FINAL", true);
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
        List<OBControl> containers = filterControls(action_getContainerPrefix() + ".*");
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
                        gotItRightBigTick(true);
                        waitForSecs(0.3);
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
                    System.out.println("OC_Generic_DragObjectsToCorrectPlace.exception caught:" + e.toString());
                    e.printStackTrace();
                    //
                    revertStatusAndReplayAudio();
                    System.out.println("OC_Generic_DragObjectsToCorrectPlace.setting status to awaiting click");
                }
            }
            else
            {
                gotItWrongWithSfx();
                // wait for end of animation needs to be enabled to prevent the animation from being cancelled too early
                action_moveObjectToOriginalPosition(dragged, true);
                //
                revertStatusAndReplayAudio();
            }
        }
        else
        {
            // wait for end of animation needs to be enabled to prevent the animation from being cancelled too early
            action_moveObjectToOriginalPosition(dragged, true);
            //
            revertStatusAndReplayAudio();
        }
    }


    @Override
    public OBControl findTarget (PointF pt)
    {
        targets = filterControls(String.format("%s.*", action_getObjectPrefix()));
        return finger(-1, 2, targets, pt, true);
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
