package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by pedroloureiro on 17/06/16.
 */
public class X_CountingTo3_S1 extends XPRZ_SectionController
{
    public X_CountingTo3_S1()
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
        targets = filterControls("platform.*");
    }


    public void demo1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        demoButtons();
        waitForSecs(0.7);
        //
        int currentAudioIndex = 0;
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false);    // Now Look
        currentAudioIndex++;
        movePointerToPoint(objectDict.get("platform_1").position(), -10, 0.6f, true);
        waitAudio();
        //
        placeObjectWithSFX("frog_1_1");
        waitForSecs(0.2);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // One frog on a rock
        currentAudioIndex++;
        //
        movePointerToPoint(objectDict.get("platform_2").position(), -10, 0.6f, true);
        placeObjectWithSFX("frog_2_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // One
        currentAudioIndex++;
        waitForSecs(0.2);
        placeObjectWithSFX("frog_2_2");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Two
        currentAudioIndex++;
        waitForSecs(0.2);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Two frogs on a rock
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        movePointerToPoint(objectDict.get("platform_3").position(), -10, 0.6f, true);
        placeObjectWithSFX("frog_3_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // One
        currentAudioIndex++;
        waitForSecs(0.2);
        placeObjectWithSFX("frog_3_2");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Two
        currentAudioIndex++;
        waitForSecs(0.2);
        placeObjectWithSFX("frog_3_3");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Three
        currentAudioIndex++;
        waitForSecs(0.2);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, false); // Three frogs on a rock
        currentAudioIndex++;
        waitForSecs(0.2);

        thePointer.hide();
        nextScene();
    }


    public void demo1e() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        int currentAudioIndex = 0;
        //
        waitForSecs(0.7);
        //
        placeObjectWithSFX("bird_1_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One bird on a branch
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_2_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_2_2");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two birds on a branch
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_2");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_3");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Three
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Three birds on a branch
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        nextScene();
    }

    public void demo1j() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        int currentAudioIndex = 0;
        //
        waitForSecs(0.7);
        //
        placeObjectWithSFX("ladybug_1_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One ladybug on a leaf
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_2_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_2_2");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two ladybugs on a leaf
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_1");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // One
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_2");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Two
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_3");
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Three
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentAudioIndex, true); // Three ladybug on a leaf
        currentAudioIndex++;
        waitForSecs(0.2);
        //
        nextScene();
    }


    public void placeObjectWithSFX(String object) throws Exception
    {
        playSfxAudio("placeObject", false);
        objectDict.get(object).show();
        waitSFX();
    }


    public void action_animatePlatform(OBControl platform) throws Exception
    {
        String platformName = (String) platform.attributes().get("id");
        String platformNumber = platformName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        //
        List<OBAnim> list_animMove1 = new ArrayList<OBAnim>();
        List<OBAnim> list_animMove2 = new ArrayList<OBAnim>();
        //
        for(OBControl item : controls)
        {
            PointF startPosition = new PointF();
            startPosition.set(item.position());
            //
            PointF endPosition = new PointF();
            endPosition.set(startPosition);
            endPosition.y -= 1.25 * item.height();
            //
            list_animMove1.add(OBAnim.moveAnim(endPosition, item));
            list_animMove1.add(OBAnim.rotationAnim((float) Math.toRadians(-180.0f), item));
            list_animMove2.add(OBAnim.moveAnim(startPosition, item));
            list_animMove1.add(OBAnim.rotationAnim((float) Math.toRadians(-360.0f), item));
//            OBAnim anim_move1 = OBAnim.moveAnim(endPosition, item);
//            OBAnim anim_move2 = OBAnim.moveAnim(startPosition, item);
//            OBAnim anim_rotate1 = OBAnim.rotationAnim((float) Math.toRadians(-180.0f), item);
//            OBAnim anim_rotate2 = OBAnim.rotationAnim((float) Math.toRadians(-360.0f), item);
//            OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(anim_move1,anim_rotate1),Arrays.asList(anim_move2,anim_rotate2)), Arrays.asList(0.4f,0.4f), false, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, this);
//            waitForSecs(0.05);
        }
        OBAnimationGroup og = new OBAnimationGroup();
        og.chainAnimations(Arrays.asList(list_animMove1, list_animMove2), Arrays.asList(0.4f,0.4f), true, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, this);
    }


    public void action_highlight(OBControl control) throws Exception
    {
        lockScreen();
        String controlName = (String) control.attributes().get("id");
        String platformNumber = controlName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        for(OBControl item : controls)
        {
            item.highlight();
        }
        control.highlight();
        unlockScreen();
    }


    public void action_lowlight(OBControl control) throws Exception
    {
        lockScreen();
        String controlName = (String) control.attributes().get("id");
        String platformNumber = controlName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        for(OBControl item : controls)
        {
            item.lowlight();
        }
        control.lowlight();
        unlockScreen();
    }

    public OBControl action_getCorrectAnswer()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }

    public String action_getObjectPrefix()
    {
        return "platform";
    }


    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            action_highlight(targ);
            //
            if (targ.equals(action_getCorrectAnswer()))
            {
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "CORRECT", false);
                action_animatePlatform(targ);
                waitAudio();
                //
                playAudioQueuedScene(currentEvent(), "FINAL", true);
                //
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                //
                action_lowlight(targ);
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception exception)
        {
        }
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1, 2, targets, pt);
    }






    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
                                             @Override
                                             public void run() throws Exception {
                                                 checkTarget(c);
                                             }
                                         }
                );
            }
        }

    }
}
