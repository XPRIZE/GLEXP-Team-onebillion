package org.onebillion.xprz.mainui.x_countingto3;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Event;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

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


    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        //
        for(OBControl number : filterControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number, 1.2f);
        }
    }

    public void setScene4a()
    {
        setSceneXX(currentEvent());
        //
        for (OBControl control : filterControls("frog.*"))
        {
            control.hide();
        }
        //
        for(int i = 0; i <= 3; i++)
        {
            OBControl number = objectDict.get("number_" + i);
            OBControl box = objectDict.get("box_" + i);
            number.setPosition(box.position());
            number.hide();
        }
    }


    public void demo4a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look
        XPRZ_Generic.pointer_moveToObjectByName("platform_0", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();

        action_playNextDemoSentence(false); // No frogs on this rock.
        XPRZ_Generic.pointer_moveToObjectByName("box_0", -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
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
            XPRZ_Generic.pointer_moveToObjectByName(platformName, -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            action_playNextDemoSentence(false);
            lockScreen();
            showControls(controls);
            unlockScreen();
            waitAudio();
            //
            XPRZ_Generic.pointer_moveToObjectByName(boxName, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            action_playNextDemoSentence(false);
            lockScreen();
            showControls(numberName);
            unlockScreen();
            waitAudio();
            waitForSecs(0.3f);
        }

        thePointer.hide();
        waitForSecs(0.3);
        //
        List<OBControl> numbers = filterControls("number.*");
        for (OBControl number : numbers)
        {
            PointF originalPosition = XPRZ_Generic.copyPoint((PointF)number.propertyValue("originalPosition"));
            float distance = OB_Maths.PointDistance(originalPosition, number.position());
            Path path = OBUtils.SimplePath(number.position(), originalPosition, distance / 5f);
            OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        waitForSecs(0.5);
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
        XPRZ_Generic.pointer_moveToObjectByName("platform_2", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        action_playNextDemoSentence(true); // Two frogs.
        waitForSecs(0.3f);
        //
        OBControl number = objectDict.get("number_2");
        XPRZ_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        PointF destination = objectDict.get("box_2").position();
        XPRZ_Generic.pointer_moveToPointWithObject(number, destination, -25, 0.6f, true, this);
        waitForSecs(0.3f);
        //
        XPRZ_Generic.pointer_moveToObject(number, -15, 0.3f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        PointF originalPosition = XPRZ_Generic.copyPoint((PointF)number.propertyValue("originalPosition"));
        float distance = OB_Maths.PointDistance(originalPosition, number.position());
        Path path = OBUtils.SimplePath(number.position(), originalPosition, distance / 5f);
        OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.3f);
        //
        nextScene();
    }

    @Override
    public String action_getObjectPrefix()
    {
        return "number";
    }

    @Override
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




    @Override
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
            if (container.attributes().get("correct_number").equals(dragged.attributes().get("number")))
            {
                try
                {
                    int number = Integer.parseInt((String) dragged.attributes().get("number"));
                    action_moveObjectIntoContainer(dragged, container);
                    dragged.disable();
                    //
                    gotItRightBigTick(false);
                    waitForSecs(0.3);
                    //
                    playAudioQueuedSceneIndex(currentEvent(), "CORRECT", number, false);
                    //
                    OBControl platform = objectDict.get(String.format("platform_%d", number));
                    action_animatePlatform(platform, false);
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
                    System.out.println("X_CountingTo3_S4.exception caught:" + e.toString());
                    e.printStackTrace();
                    //
                    setStatus(STATUS_AWAITING_CLICK);
                    System.out.println("X_CountingTo3_S4.setting status to awaiting click");
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




    @Override
    public void fin()
    {
        goToCard(X_CountingTo3_S4f.class, "event4");
    }
}
