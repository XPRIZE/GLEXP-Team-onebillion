package org.onebillion.xprz.mainui;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 17/06/16.
 */
public class X_CountingTo3_S4 extends XPRZ_Generic_Event
{

    public X_CountingTo3_S4()
    {
        super();
    }


    public void demo4a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look
        pointer_moveToObjectByName("platform_0", -25, 0.6f, EnumSet.of(Anchor.ANCHOR_MIDDLE), true);
        waitAudio();
        //
        action_playNextDemoSentence(false); // No frogs on this rock.
        pointer_moveToObjectByName("box_0", -15, 0.6f, EnumSet.of(Anchor.ANCHOR_BOTTOM), true);
        waitAudio();
        action_playNextDemoSentence(false); // Zero
        showControls("number_0");
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 3; i++)
        {
            String numberName = "number_" + i;
            String boxName = "box_" + i;
            String platformName = "platform_" + i;
            String controls = "frog_" + i + "_.*";
            //
            pointer_moveToObjectByName(platformName, -25, 0.6f, EnumSet.of(Anchor.ANCHOR_MIDDLE), true);
            action_playNextDemoSentence(false);
            lockScreen();
            showControls(controls);
            unlockScreen();
            waitAudio();
            //
            pointer_moveToObjectByName(boxName, -15, 0.6f, EnumSet.of(Anchor.ANCHOR_BOTTOM), true);
            action_playNextDemoSentence(false);
            lockScreen();
            showControls(numberName);
            unlockScreen();
            waitAudio();
            waitForSecs(0.3f);
        }
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        List<OBControl> numbers = filterControls("number.*");
        for (OBControl number : numbers)
        {
            PointF originalPosition = copyPoint((PointF)number.propertyValue("originalPosition"));
//            float distance = OB_Maths.PointDistance(originalPosition, number.position());
            OBAnim anim = OBAnim.moveAnim(originalPosition, number);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        //
        waitForSecs(1.0);
        //
        nextScene();
    }


    public void demo4b() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7f);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now Look.
        waitForSecs(0.3f);
        //
        pointer_moveToObjectByName("platform_2", -25, 0.6f, EnumSet.of(Anchor.ANCHOR_MIDDLE), true);
        waitAudio();
        action_playNextDemoSentence(true); // Two frogs.
        waitForSecs(0.3f);
        //
        OBControl number = objectDict.get("number_2");
        pointer_moveToObject(number, -15, 0.6f, EnumSet.of(Anchor.ANCHOR_MIDDLE), true);
        PointF destination = objectDict.get("box_2").position();
        pointer_moveToPointWithObject(number, destination, -25, 0.6f, true);
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        PointF originalPosition = copyPoint((PointF)number.propertyValue("originalPosition"));
//            float distance = OB_Maths.PointDistance(originalPosition, number.position());
        OBAnim anim = OBAnim.moveAnim(originalPosition, number);
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.3f);
        //
        nextScene();
    }


    public OBControl action_getCorrectAnswer()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }

    public String action_getObjectPrefix()
    {
        return "number";
    }

    public String action_getContainerPrefix()
    {
        return "box";
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





    OBControl findTarget(PointF pt)
    {
        return finger(-1, 2, targets, pt);
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


    @Override
    public void checkDragAtPoint(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        //
        OBControl dragged = this.target;
        this.target = null;
        //
        List<OBControl> containers = filterControls(action_getContainerPrefix() + ".*");
        OBControl container = finger(0, 2, containers, pt, true);
        //
        if (container != null)
        {
            if (container.attributes().get("correct_number") == (dragged.attributes().get("number")))
            {
                action_moveObjectIntoContainer(dragged, container);
                dragged.disable();
                //
                if (action_isEventOver())
                {
                    try
                    {
                        gotItRightBigTick(true);
                        waitForSecs(0.3);
                        //
                        playAudioQueuedScene(currentEvent(), "FINAL", true);
                    }
                    catch (Exception e)
                    {
                    }
                }
                else
                {
                    try
                    {
                        gotItRight();
                        waitForSecs(0.3);
                        //
                        playAudioQueuedSceneIndex(currentEvent(), "CORRECT", Integer.parseInt((String)dragged.attributes().get("number")), true);
                    }
                    catch (Exception e)
                    {
                    }
                }
                //
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                action_moveObjectToOriginalPosition(dragged);
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            action_moveObjectToOriginalPosition(dragged);
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
    }




    @Override
    public void fin()
    {

    }
}
