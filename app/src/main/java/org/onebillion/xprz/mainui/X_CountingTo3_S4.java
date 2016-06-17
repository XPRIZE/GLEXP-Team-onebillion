package org.onebillion.xprz.mainui;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 17/06/16.
 */
public class X_CountingTo3_S4 extends XPRZ_SectionController
{

    public X_CountingTo3_S4()
    {
        super();
    }

    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("master1");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);

        doVisual(currentEvent());
        unlockScreen();
    }


    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
            }
        });
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        //
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    @Override
    public void setSceneXX(String scene)
    {
        ArrayList<OBControl> oldControls = new ArrayList<>(objectDict.values());
        //
        loadEvent(scene);
        //
        Boolean redraw = eventAttributes.get("redraw").equals("true");
        if (redraw)
        {
            for(OBControl control : oldControls)
            {
                detachControl(control);
                objectDict.remove(control);
            }
        }
        //
        targets = filterControls(action_getObjectPrefix() + ".*");
        //
        for(OBControl control : targets)
        {
            PointF originalPosition = new PointF(control.position().x, control.position().y);
            control.setProperty("originalPosition", originalPosition);
        }
    }


    public void demo4a() throws Exception
    {
//        setStatus(STATUS_DOING_DEMO);
//        demoButtons();
//        waitForSecs(0.7);
//        //
//        int currentAudioIndex = 0;
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false);    // Now Look
//        currentAudioIndex++;
//        movePointerToPoint(objectDict.get("platform_1").position(), -10, 0.6f, true);
//        waitAudio();
//        //
//        placeObjectWithSFX("frog_1_1");
//        waitForSecs(0.2);
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // One frog on a rock
//        currentAudioIndex++;
//        //
//        movePointerToPoint(objectDict.get("platform_2").position(), -10, 0.6f, true);
//        placeObjectWithSFX("frog_2_1");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // One
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        placeObjectWithSFX("frog_2_2");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Two
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Two frogs on a rock
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        movePointerToPoint(objectDict.get("platform_3").position(), -10, 0.6f, true);
//        placeObjectWithSFX("frog_3_1");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // One
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        placeObjectWithSFX("frog_3_2");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Two
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        placeObjectWithSFX("frog_3_3");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Three
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Three frogs on a rock
//        currentAudioIndex++;
//        waitForSecs(0.2);
//
//        thePointer.hide();
//        nextScene();
    }


    public void demo4b() throws Exception
    {
//        setStatus(STATUS_DOING_DEMO);
//        int currentAudioIndex = 0;
//        //
//        waitForSecs(0.7);
//        //
//        placeObjectWithSFX("bird_1_1");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One bird on a branch
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        placeObjectWithSFX("bird_2_1");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        placeObjectWithSFX("bird_2_2");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two birds on a branch
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        placeObjectWithSFX("bird_3_1");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        placeObjectWithSFX("bird_3_2");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        placeObjectWithSFX("bird_3_3");
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Three
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Three birds on a branch
//        currentAudioIndex++;
//        waitForSecs(0.2);
//        //
//        nextScene();
    }


    public void placeObjectWithSFX(String object) throws Exception
    {
        playSfxAudio("placeObject", false);
        objectDict.get(object).show();
        waitSFX();
    }


    public void action_moveObjectToOriginalPosition(OBControl control)
    {
        OBAnim anim = OBAnim.moveAnim((PointF)control.propertyValue("originalPosition"), control);
        OBAnimationGroup og = new OBAnimationGroup();
        og.applyAnimations(Arrays.asList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public void action_moveObjectIntoContainer(OBControl control, OBControl container)
    {
        OBAnim anim = OBAnim.moveAnim(container.position(), control);
        OBAnimationGroup og = new OBAnimationGroup();
        og.applyAnimations(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
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
