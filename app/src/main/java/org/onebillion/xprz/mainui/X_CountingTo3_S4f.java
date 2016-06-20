package org.onebillion.xprz.mainui;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 20/06/16.
 */
public class X_CountingTo3_S4f extends XPRZ_Generic_Event
{
    public X_CountingTo3_S4f()
    {
        super();
    }

    @Override
    public String action_getScenesProperty()
    {
        return "scenes2";
    }

    @Override
    public String action_getObjectPrefix()
    {
        return "object";
    }

    @Override
    public String action_getContainerPrefix()
    {
        return "container";
    }



    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        //
        for(OBControl number : filterControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number);
        }
    }


    public void demo4f() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.7f);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now Look
        pointer_moveToObjectByName("object_1", -15, 0.6f, EnumSet.of(Anchor.ANCHOR_MIDDLE), true);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // One thing for the one box
        OBControl object = objectDict.get("object_1");
        PointF destination = objectDict.get("container_1").position();
        pointer_moveToPointWithObject(object, destination, -25, 0.6f, false);
        playSfxAudio("dropObject", false);
        waitForSecs(0.3);
        pointer_moveToObjectByName("number_1", -15, 0.4f, EnumSet.of(Anchor.ANCHOR_BOTTOM), true);
        waitForSecs(0.7);
        //
        thePointer.hide();
        waitAudio();
        waitForSecs(0.3);
        //
        action_moveObjectToOriginalPosition(object, true);
        //
        nextScene();
    }




    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
                                             @Override
                                             public void run() throws Exception {
                                                 checkDragTarget(c, pt);
                                             }
                                         }
                );
            }
        }
    }


    public Boolean action_isEventOver()
    {
        List<OBControl> containers = filterControls(action_getContainerPrefix() + ".*");
        for (OBControl container : containers)
        {
            List<OBControl> containedObjects = (List<OBControl>) container.propertyValue("contained");
            if (containedObjects == null) containedObjects = new ArrayList<OBControl>();
            int correctQuantity = Integer.parseInt((String)container.attributes().get("correctQuantity"));
            if (containedObjects.size() != correctQuantity) return false;
        }
        return true;
    }


    @Override
    public void checkDragAtPoint(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        //
        OBControl container = finger(-1, 2, filterControls(action_getContainerPrefix() + ".*"), pt, true);
        OBControl dragged = target;
        target = null;
        //
        if (container != null)
        {
            int correctQuantity = Integer.parseInt((String)container.attributes().get("correctQuantity"));
            List<OBControl> containedObjects = (List<OBControl>) container.propertyValue("contained");
            if (containedObjects == null) containedObjects = new ArrayList<OBControl>();
            if (containedObjects.size() < correctQuantity)
            {
                action_moveObjectIntoContainer(dragged, container);
                dragged.disable();
                //
                try {
                    gotItRightBigTick(false);
                    waitForSecs(0.3);
                    //
                    if (action_isEventOver())
                    {
                        //
                        gotItRightBigTick(true);
                        //
                        //
                        playAudioQueuedScene(currentEvent(), "CORRECT", true);
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
                    System.out.println("X_CountingTo3_S4f.exception caught: " + e.toString());
                    e.printStackTrace();
                }

            }
            else
            {
                gotItWrongWithSfx();
                //
                action_moveObjectToOriginalPosition(dragged, true);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            action_moveObjectToOriginalPosition(dragged, true);
            setStatus(STATUS_AWAITING_CLICK);
        }
        /*
        [self statusSetLocked];
        NSArray *containers = [self filterControls:@"container.*"];
        OBControl *container = [self finger:0 to:2 objects:containers point:pt];
        OBControl *object = self.target;
        self.target = nil;
        if (container != nil)
        {
            int correctQuantity = [container.attributes[@"correctQuantity"] intValue];
            if ([self totalObjectsContained:container] < correctQuantity)
            {
                if ([self addObjectAndRearrangeContainer:object container:container maxObjectsPerContainer:[container.attributes[@"correctQuantity"] intValue] keepInBound:NO])
                {
                    [object disable];
                }
                //check for end scene condition
                if ([self isEventComplete])
                {
                    [self waitForSecs:0.3];
                    //
                    [self gotItRightBigTick:NO];
                    [self waitSFX];
                    [self waitForSecs:0.3];
                    //
                    [self gotItRightBigTick:YES];
                    [self waitForSecs:0.3];
                    //
                    [self playSceneAudio:@"CORRECT" wait:YES];
                    [self waitForSecs:0.3];
                    //
                    [self playSceneAudio:@"FINAL" wait:YES];
                    [self waitForSecs:0.3];
                    //
                    [self nextScene];
                    return;
                } else {
                    DoSafeBlockOnThread(^{
                        [self waitForSecs:0.3];
                        [self gotItRightBigTick:NO];
                        [self waitSFX];
                    });
                }
            } else {
                [self moveObjectToOriginalPosition:object withSound:NO keepInBound:YES];
                [self gotItWrongWithSfx];
            }
        } else {
            [self moveObjectToOriginalPosition:object withSound:YES keepInBound:YES];
        }
        [self statusSetWaitClick];
         */

//        setStatus(STATUS_CHECKING);
//        //
//        OBControl dragged = this.target;
//        this.target = null;
//        //
//        List<OBControl> containers = filterControls(action_getContainerPrefix() + ".*");
//        OBControl container = finger(0, 2, containers, pt, true);
//        //
//        if (container != null)
//        {
//            if (container.attributes().get("correct_number").equals(dragged.attributes().get("number")))
//            {
//                try {
//                    action_moveObjectIntoContainer(dragged, container);
//                    dragged.disable();
//                    //
//                    playAudioQueuedSceneIndex(currentEvent(), "CORRECT", Integer.parseInt((String) dragged.attributes().get("number")), true);
//                    waitForSecs(0.3);
//                    //
//                    OBControl platform = objectDict.get(String.format("platform_%d", Integer.parseInt((String) dragged.attributes().get("number"))));
//                    action_animatePlatform(platform, false);
//                    //
//                    gotItRightBigTick(true);
//                    waitForSecs(0.3);
//                    //
//                    if (action_isEventOver()) {
//                        playAudioQueuedScene(currentEvent(), "FINAL", true);
//                        //
//                        nextScene();
//                    } else {
//                        setStatus(STATUS_AWAITING_CLICK);
//                    }
//                }
//                catch (Exception e)
//                {
//                    System.out.println("X_CountingTo3_S4.exception caught:" + e.toString());
//                    e.printStackTrace();
//                }
//            }
//            else
//            {
//                gotItWrongWithSfx();
//                action_moveObjectToOriginalPosition(dragged);
//                //
//                setStatus(STATUS_AWAITING_CLICK);
//            }
//        }
//        else
//        {
//            action_moveObjectToOriginalPosition(dragged);
//            //
//            setStatus(STATUS_AWAITING_CLICK);
//        }
    }
}
