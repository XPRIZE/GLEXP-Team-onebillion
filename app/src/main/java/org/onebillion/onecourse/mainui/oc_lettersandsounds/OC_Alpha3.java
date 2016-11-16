package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 30/06/16.
 */
public class OC_Alpha3 extends OC_Alpha
{
    OBPresenter presenter;
    int phase, firstBox, lastBox;
    long flashBoxTimeStamp;

    public OC_Alpha3 ()
    {
        super();
    }

    public void miscSetup ()
    {
        super.miscSetup();
        //
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup(presenterControl);
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restPos", presenter.control.getWorldPosition());
            presenter.control.setRight(0);
            presenter.control.show();
        }
    }


    @Override
    public void prepare ()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetup();
        events = new ArrayList(Arrays.asList("a", "b", "c", "d", "e", "f"));
        doVisual(currentEvent());
    }

    @Override
    public void doMainXX () throws Exception
    {
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }

    @Override
    public void doAudio (String scene) throws Exception
    {
        List audio, replayAudio;
        if (phase == 0)
        {
            audio = currentAudio("PROMPT");
            replayAudio = currentAudio("REPEAT");
        }
        else
        {
            audio = currentAudio(String.format("PROMPT%d", phase + 1));
            replayAudio = currentAudio(String.format("REPEAT%d", phase + 1));
        }
        setReplayAudio(replayAudio);
        playAudioQueued(audio, false);
        //
        if (flashBoxTimeStamp == 0) action_flashRow();
        //
        if (phase > 0)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    doReminder();
                }
            });
        }
    }


    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
        //
        phase = 0;
        flashBoxTimeStamp = 0;
        firstBox = 0;
        lastBox = letters.size();
        //
        if (!scene.equals(events.get(events.size() - 1)))
        {
            for (int i = 0; i < events.size(); i++)
            {
                int idx = (i == 0) ? 1 : i;
                if (currentEvent().equals(events.get(i)))
                {
                    firstBox = (idx - 1) * boxesPerRow;
                    lastBox = Math.min(idx * boxesPerRow, lastBox);
                    break;
                }
            }
        }
    }


    public void demoa () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.3);
        //
        List aud = currentAudio("DEMO");
        PointF position = OC_Generic.copyPoint((PointF) presenter.control.propertyValue("restPos"));
        presenter.walk(position);
        presenter.faceFront();
        waitForSecs(0.3);
        //
        presenter.speak(Arrays.asList(aud.get(0)), this); // You have a name. Letters have names too!
        waitForSecs(0.8);
        //
        presenter.speak(Arrays.asList(aud.get(1)), this); // Let’s hear the names of the letters.
        waitForSecs(0.3);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.getWorldPosition());
        PointF destPos = new PointF(currPos.x - bounds().width() * 0.2f, currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3);
        //
        presenter.speak(Arrays.asList(aud.get(2)), this); // Are you ready?
        waitForSecs(0.3);
        //
        currPos = OC_Generic.copyPoint(presenter.control.getWorldPosition());
        OBControl side = presenter.control.objectDict.get("faceright");
        destPos = new PointF(-side.width() * 1.3f, currPos.y);
        presenter.walk(destPos);
        //
        nextScene();
    }


    public void demob () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 1.3f, 0f, 0.1f, true, this);
        //  q
        OBControl box = boxes.get(lastBox - 1);
        PointF position = OC_Generic.copyPoint(box.position());
        position.y += 0.75 * box.height();
        //
        playSceneAudio("PROMPT", false); // When a row flashes, touch it.
        movePointerToPoint(position, -5, -1.2f, true);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        setReplayAudioScene(currentEvent(), "REPEAT");
        setStatus(STATUS_AWAITING_CLICK);
        //
        action_flashRow();
    }


    public void action_flashRow ()
    {
        flashBoxTimeStamp = statusTime;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                action_flashRowWithStatusTime(flashBoxTimeStamp);
            }
        });
    }

    public void action_flashRow_stop ()
    {
        flashBoxTimeStamp = 0;
    }


    public void action_flashRowWithStatusTime (final long timeStamp) throws Exception
    {
        if (flashBoxTimeStamp != timeStamp) return;
        //
        lockScreen();
        for (int i = firstBox; i < lastBox; i++)
        {
            OBPath stroke = strokes.get(i);
            stroke.show();
        }
        unlockScreen();
        waitForSecs(0.3);
        //
        lockScreen();
        for (int i = firstBox; i < lastBox; i++)
        {
            OBPath stroke = strokes.get(i);
            stroke.hide();
        }
        unlockScreen();
        waitForSecs(0.3);
        //
        action_flashRowWithStatusTime(timeStamp);
    }


    public void action_introLetters_beat () throws Exception
    {
        float beatsPerMinute = Float.parseFloat(parameters.get("bpm"));
        float waitTime = 60 / beatsPerMinute;
        String sfx = parameters.get("sfx");
        //
        int counter = 1;
        double startTime = OC_Generic.currentTime();
        double currentTime, elapsed, waitForNextLoop;
        //
        for (int i = firstBox; i < lastBox; i++)
        {
            checkSuspendLock();
            //
            currentTime = OC_Generic.currentTime();
            //
            playSfxAudio(sfx, false);
            //
            lockScreen();
            if (i > 1 && i % boxesPerRow == 0 && currentEvent().equals(events.get(0)))
            {
                for (int j = 0; j < i; j++)
                {
                    labels.get(j).setOpacity(0.3f);
                    labels.get(j).setColour(Color.BLACK);
                }
            }
            //
            OBLabel label = labels.get(i);
            label.setOpacity(1.0f);
            label.setColour(Color.RED);
            label.show();
            unlockScreen();
            //
            playAudioQueued(new ArrayList(Arrays.asList(String.format("alph_%s", letters.get(i).toLowerCase()))), false);
            //
            elapsed = OC_Generic.currentTime() - currentTime;
//            elapsed = OC_Generic.currentTime() - startTime;
//            nextTimeFrame = waitTime * counter++;
//            waitForNextLoop = nextTimeFrame - elapsed;
            waitForNextLoop = waitTime - elapsed;
            waitForSecs(waitForNextLoop);
            //
            lockScreen();
            if (i == lastBox - 1)
            {
                for (int j = firstBox; j < lastBox; j++)
                {
                    label = labels.get(j);
                    OBPath box = boxes.get(j);
                    //
                    label.setOpacity(1.0f);
                    label.setColour(Color.BLACK);
                    label.setScale(1.0f);
                    box.setScale(1.0f);
                    box.setFillColor(boxLowColour);
                }
            }
            else
            {
                labels.get(i).setColour(Color.BLACK);
            }
            unlockScreen();
        }
    }


    public void action_introLetters_rhythm () throws Exception
    {
        float beatsPerMinute = Float.parseFloat(parameters.get("bpm"));
        float waitTime = 60 / beatsPerMinute;
        final String rhythm = parameters.get("rhythm");
        //
        double startTime = OC_Generic.currentTime();
        int counter = 1;
        int totalBoxes = lastBox - firstBox;
        final int loops = (int) (totalBoxes / (float) 4) + 1;
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                for (int i = 0; i < loops; i++)
                {
                    checkSuspendLock();
                    //
                    playBackgroundAudio(rhythm, true);
                }
            }
        });
        //
        double currentTime;
        double elapsed = OC_Generic.currentTime() - startTime;
        double nextTimeFrame = waitTime * counter++;
        double waitForNextLoop = nextTimeFrame - elapsed;
        waitForSecs(waitForNextLoop);
        //
        for (int i = firstBox; i < lastBox; i++)
        {
            checkSuspendLock();
            //
            currentTime = OC_Generic.currentTime();
            //
            if (i > 1 && i % boxesPerRow == 0 && currentEvent().equals(events.get(0)))
            {
                lockScreen();
                for (int j = 0; j < i; j++)
                {
                    labels.get(j).setOpacity(0.3f);
                }
                unlockScreen();
            }
            OBLabel label = labels.get(i);
            lockScreen();
            label.setOpacity(1.0f);
            label.setColour(Color.RED);
            label.show();
            unlockScreen();
            //
            playAudioQueued(new ArrayList(Arrays.asList(String.format("alph_%s", letters.get(i).toLowerCase()))), false);
            //
            elapsed = OC_Generic.currentTime() - currentTime;
//            elapsed = OC_Generic.currentTime() - startTime;
//            nextTimeFrame = waitTime * counter++;
//            waitForNextLoop = nextTimeFrame - elapsed;
            waitForNextLoop = waitTime - elapsed;
            waitForSecs(waitForNextLoop);
            //
            if (i == lastBox - 1)
            {
                lockScreen();
                for (int j = firstBox; j < lastBox; j++)
                {
                    label = labels.get(j);
                    OBPath box = boxes.get(j);
                    //
                    label.setOpacity(1.0f);
                    label.setColour(Color.BLACK);
                    label.setScale(1.0f);
                    box.setScale(1.0f);
                    box.setFillColor(boxLowColour);
                }
                unlockScreen();
            }
            else
            {
                lockScreen();
                label = labels.get(i);
                label.setColour(Color.BLACK);
                unlockScreen();
            }
        }
        OBAudioManager.audioManager.stopPlayingBackground();
    }


    public void action_introLetters () throws Exception
    {
        String sfx = parameters.get("sfx");
        String rhythm = parameters.get("rhythm");
        //
        if (sfx != null)
        {
            action_introLetters_beat();
        }
        else if (rhythm != null)
        {
            action_introLetters_rhythm();
        }
    }


    public void action_showLetters () throws Exception
    {
        List<OBAnim> animations = new ArrayList();
        //
        lockScreen();
        for (int i = 0; i < labels.size(); i++)
        {
            OBLabel label = labels.get(i);
            if (i >= firstBox && i < lastBox)
            {
                OBGroup back = backs.get(i);
                OBControl clone = back.copy();
                back.setScreenMaskControl(clone);
//                OBGroup group = new OBGroup(new ArrayList(Arrays.asList(back)));
//                attachControl(group);
//                group.setZPosition(back.zPosition() + 0.1f);
//                group.show();
//                group.setMasksToBounds(true);
                //
                PointF destination = OC_Generic.copyPoint(back.position());
                destination.x -= 1.1 * back.width();
                OBAnim animation = OBAnim.moveAnim(destination, back);
                animations.add(animation);
                label.setOpacity(1.0f);
            }
            else
            {
                label.setOpacity(0.3f);
            }
        }
        unlockScreen();
        //
        playSfxAudio("reveal", false);
        double duration = OBAudioManager.audioManager.durationSFX();
        OBAnimationGroup.runAnims(animations, duration, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.3);
    }


    public Boolean action_isCorrectBox (OBPath box)
    {
        for (int i = firstBox; i < lastBox; i++)
        {
            if (box.equals(boxes.get(i))) return true;
        }
        return false;
    }


    public void checkBox (OBPath box)
    {
        setStatus(STATUS_CHECKING);
        //
        try
        {
            Boolean correctBox = action_isCorrectBox(box);
            if (correctBox)
            {
                action_flashRow_stop();
                waitForSecs(0.6);
                //
                if (phase == 0 && !currentEvent().equals(events.get(events.size() - 1)))
                {
                    action_showLetters();
                    playSceneAudio("CORRECT", true); // Listen!
                    waitForSecs(0.3);
                }
                //
                action_introLetters();
                waitForSecs(0.3);
                phase++;
                if (currentEvent().equals(events.get(events.size() - 1)))
                {
                    waitForSecs(0.3);
                    //
                    playSceneAudio("FINAL", true);
                    waitForSecs(0.3);
                    //
                    nextScene();
                }
                else if (phase > 2)
                {
                    List<String> finalAudio = currentAudio("FINAL");
                    if (finalAudio != null)
                    {
                        lockScreen();
                        for (OBLabel label : labels)
                        {
                            label.setOpacity(1.0f);
                        }
                        unlockScreen();
                        gotItRightBigTick(true);
                        playSceneAudio("FINAL", true);
                        waitForSecs(0.3);
                    }
                    nextScene();
                }
                else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                    doAudio(currentEvent());
                }
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBPath findBox (PointF pt)
    {
        return (OBPath) finger(-1, 2, (List<OBControl>) (Object) boxes, pt, true);
    }


    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBPath obj = findBox(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkBox(obj);
                    }
                });
            }
        }
    }
}
