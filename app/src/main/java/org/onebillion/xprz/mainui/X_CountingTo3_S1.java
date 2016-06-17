package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OB_utils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;

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
        OB_utils.runOnOtherThread(()->{
            if (!performSel("demo",currentEvent()))
            {
                doBody(currentEvent());
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
        Collection<OBControl> oldControls = objectDict.values();
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


    public void action_highlight(OBControl control) throws Exception
    {
        control.highlight();
    }


    public void action_lowlight(OBControl control) throws Exception
    {
        control.lowlight();
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
                playAudioQueuedScene(currentEvent(), "CORRECT", true);
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
                OB_utils.runOnOtherThread(()->{
                    checkTarget(c);
                });

            }
        }

    }
}
