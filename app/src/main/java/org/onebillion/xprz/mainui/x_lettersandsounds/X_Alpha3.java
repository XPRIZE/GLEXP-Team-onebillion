package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 30/06/16.
 */
public class X_Alpha3 extends X_Alpha
{
    XPRZ_Presenter presenter;
    int phase, firstBox, lastBox;
    long flashBoxTimeStamp;

    public X_Alpha3 ()
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
            presenter = XPRZ_Presenter.characterWithGroup(presenterControl);
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
            audio = currentAudio ("PROMPT");
            replayAudio = currentAudio ("REPEAT");
        }
        else
        {
            audio = currentAudio (String.format("PROMPT%d", phase + 1));
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


    public void demoa() throws Exception
    {
//        setStatus(STATUS_DOING_DEMO);
//        waitForSecs(0.3);
//        //
//        List aud = currentAudio("DEMO");
//        PointF position = XPRZ_Generic.copyPoint((PointF) presenter.control.propertyValue("restPos"));
//        presenter.walk(position);
//        presenter.faceFront();
//        waitForSecs(0.3);
//        //
//        presenter.speak(Arrays.asList(aud.get(0)), this); // You have a name. Letters have names too!
//        waitForSecs(0.8);
//        //
//        presenter.speak(Arrays.asList(aud.get(1)), this); // Let’s hear the names of the letters.
//        waitForSecs(0.3);
//        //
//        PointF currPos = XPRZ_Generic.copyPoint(presenter.control.getWorldPosition());
//        PointF destPos = new PointF(currPos.x - bounds().width() * 0.2f, currPos.y);
//        presenter.walk(destPos);
//        presenter.faceFront();
//        waitForSecs(0.3);
//        //
//        presenter.speak(Arrays.asList(aud.get(2)), this); // Are you ready?
//        waitForSecs(0.3);
//        //
//        currPos = XPRZ_Generic.copyPoint(presenter.control.getWorldPosition());
//        OBControl side = presenter.control.objectDict.get("faceright");
//        destPos = new PointF(- side.width() * 1.2f, currPos.y);
//        presenter.walk(destPos);
        //
        nextScene();
    }



    public void demob() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.9f, 1.3f, 0f, 0.1f, true, this);
        //
        OBControl box = boxes.get(lastBox - 1);
        PointF position = XPRZ_Generic.copyPoint(box.position());
        position.y += 0.75 * box.height();
        XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.9f, 0.85f, -5f, 0.6f, false, this);
        //
        action_playNextDemoSentence(false); // When a row flashes, touch it.
        movePointerToPoint(position, -5, -0.9f, true);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        setReplayAudioScene("REPEAT", currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
        //
        action_flashRow();
    }


    public void action_flashRow()
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

    public void action_flashRow_stop()
    {
        flashBoxTimeStamp = 0;
    }


    public void action_flashRowWithStatusTime(final long timeStamp) throws Exception
    {
        lockScreen();
        for (int i = firstBox; i < lastBox; i++)
        {
            OBPath box = boxes.get(i);
            box.setFillColor(boxHighColour);
            box.setLineWidth(applyGraphicScale(5));
            box.setStrokeColor(boxHighColour);
            box.sizeToBoundingBoxIncludingStroke();
            //
            OBGroup back = backs.get(i);
            OBControl frame = back.objectDict.get("frame");
            if (frame != null)
            {
                frame.hide();
            }
            back.setNeedsRetexture();
        }
        unlockScreen();
        waitForSecs(0.3);
        //
        lockScreen();
        for (int i = firstBox; i < lastBox; i++)
        {
            OBPath box = boxes.get(i);
            box.setFillColor(boxLowColour);
            box.setLineWidth(applyGraphicScale(1));
            box.setStrokeColor(boxLowColour);
            box.sizeToBoundingBoxIncludingStroke();
            //
            OBGroup back = backs.get(i);
            OBControl frame = back.objectDict.get("frame");
            if (frame != null)
            {
                frame.show();
            }
            back.setNeedsRetexture();
        }
        unlockScreen();
        waitForSecs(0.3);
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {

            @Override
            public void run () throws Exception
            {
                action_flashRowWithStatusTime(timeStamp);
            }
        });
    }



    public void action_introLetters_beat() throws Exception
    {
        float beatsPerMinute = Float.parseFloat(parameters.get("bpm"));
        float waitTime = 60 / beatsPerMinute;
        String sfx = parameters.get("sfx");
        //
        int counter = 1;
        int extraBeats = 0;
        double startTime = XPRZ_Generic.currentTime();
        for (int i = firstBox - extraBeats; i < lastBox + extraBeats; i++)
        {
            Boolean valid = i >= firstBox && i < lastBox;
            if (valid && i > 1 && i % boxesPerRow == 0 && currentEvent().equals(events.get(0)))
            {
                lockScreen();
                for (int j = 0; j < i; j++)
                {
                    labels.get(j).setOpacity(0.3f);
                }
                unlockScreen();
            }
            playSfxAudio(sfx, false);
            //
            if (valid)
            {
                OBLabel label = labels.get(i);
                lockScreen();
                label.setOpacity(1.0f);
                label.setColour(Color.RED);
                label.show();
                unlockScreen();
            }
            //
            playAudioQueued(new ArrayList(Arrays.asList(String.format("alph_%s", letters.get(i).toLowerCase()))), false);
            //
            double elapsed = XPRZ_Generic.currentTime() - startTime;
            double nextTimeFrame = waitTime * counter++;
            double waitForNextLoop = nextTimeFrame - elapsed;
            waitForSecs(waitForNextLoop);
            //
            if (i == lastBox - 1)
            {
                lockScreen();
                for (int j = firstBox; j < lastBox; j++)
                {
                    OBLabel label = labels.get(j);
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
            else if (valid)
            {
                lockScreen();
                labels.get(i).setColour(Color.BLACK);
                unlockScreen();
            }
        }
    }



    public void action_introLetters_rhythm() throws Exception
    {
        float beatsPerMinute = Float.parseFloat(parameters.get("bpm"));
        float waitTime = 60 / beatsPerMinute;
        final String rhythm = parameters.get("rhythm");
        //
        double startTime = XPRZ_Generic.currentTime();
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
                    playBackgroundAudio(rhythm, true);
                }
            }
        });
        //
        double elapsed = XPRZ_Generic.currentTime() - startTime;
        double nextTimeFrame = waitTime * counter++;
        double waitForNextLoop = nextTimeFrame - elapsed;
        waitForSecs(waitForNextLoop);
        //
        for (int i = firstBox; i < lastBox; i++)
        {
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
            elapsed = XPRZ_Generic.currentTime() - startTime;
            nextTimeFrame = waitTime * counter++;
            waitForNextLoop = nextTimeFrame - elapsed;
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


    public void action_introLetters() throws Exception
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


    public void action_showLetters() throws Exception
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
                OBGroup group = new OBGroup(new ArrayList(Arrays.asList(back)));
                attachControl(group);
                group.setZPosition(back.zPosition() + 0.1f);
                group.show();
                group.setMasksToBounds(true);
                //
                PointF destination = XPRZ_Generic.copyPoint(back.position());
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



    public Boolean action_isCorrectBox(OBGroup box)
    {
        for (int i = firstBox; i < lastBox; i++)
        {
            if (box.equals(boxes.get(i))) return true;
        }
        return false;
    }


    public void checkBox(OBGroup box)
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


    public OBGroup findBox(PointF pt)
    {
        return (OBGroup) finger(-1, 2, (List<OBControl>) (Object) boxes, pt, true);
    }


    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBGroup obj = findBox(pt);
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
