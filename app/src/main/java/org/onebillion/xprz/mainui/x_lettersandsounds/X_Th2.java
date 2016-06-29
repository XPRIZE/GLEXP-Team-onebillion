package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_WordsEvent;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.security.PolicySpi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by pedroloureiro on 29/06/16.
 */
public class X_Th2 extends XPRZ_Generic_WordsEvent
{
    static final float FIRST_REMINDER_DELAY = 6.0f;
    static final float SECOND_REMINDER_DELAY = 4.0f;

    XPRZ_Presenter presenter;
    List<String> answers;
    OBLabel label;
    List<List<String>> sets;
    List<OBGroup> touchables;
    String mode;
    Boolean showPresenterIntro, showText, showTick;


    public void pickCorrectAnswers ()
    {
        String param_answers = parameters.get("answers");
        //
        if (param_answers == null)
        {
            answers = new ArrayList<String>();
            for (int i = 0; i < sets.size(); i++)
            {
                List<String> pickedWords = new ArrayList<String>();
                for (String word : sets.get(i))
                {
                    if (pickedWords.contains(word)) pickedWords.remove(word);
                    else pickedWords.add(word);
                }
                answers.add(pickedWords.get(0));
            }
        }
        else
        {
            String array_answers[] = param_answers.split(",");
            answers = new ArrayList<String>();
            for (String answer : array_answers)
            {
                answers.add(answer);
            }
        }
    }


    public void miscSetup ()
    {
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = XPRZ_Presenter.characterWithGroup(presenterControl);
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restPos", presenter.control.getWorldPosition());
            presenter.control.setRight(0);
            presenter.control.show();
        }
        //
        wordComponents = OBUtils.LoadWordComponentsXML(true);
        textSize = Float.parseFloat(eventAttributes.get("textsize"));
        mode = parameters.get("mode");
        needDemo = parameters.get("demo") != null && parameters.get("demo").equals("true");
        showText = parameters.get("showText") != null && parameters.get("showText").equals("true");
        showPresenterIntro = parameters.get("presenter") != null && parameters.get("presenter").equals("true");
        showTick = true;
        //
        sets = new ArrayList<>();
        String ws = parameters.get("sets");
        String wordSets[] = ws.split(";");
        for (String set : wordSets)
        {
            ArrayList set_array = new ArrayList();
            for (String word : set.split(","))
            {
                set_array.add(word);
            }
            sets.add(set_array);
        }
        pickCorrectAnswers();
        currNo = 0;
        touchables = (List<OBGroup>) (Object) filterControls("obj.*");
    }

    @Override
    public void prepare ()
    {
        super.prepare();
        loadFingers();
        loadEvent("master");
        miscSetup();
        //
        int totalEvents = (needDemo) ? sets.size() - 1 : sets.size();
        events = new ArrayList<>(Arrays.asList("c", "d", "e"));
        while (events.size() < totalEvents) events.add(events.get(events.size() - 1));
        while (events.size() > totalEvents) events.remove(events.size() - 1);
        //
        events.add("finale");
        if (needDemo) events.add(0, "b");
        if (showPresenterIntro) events.add(0, "a");
        //
        doVisual(currentEvent());
    }

    @Override
    public void start ()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                action_popup();
                //
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
            }
        });
    }


    @Override
    public void setScene (final String scene)
    {
        new OBRunnableSyncUI(){public void ex()
        {
            doVisual(scene);
        }}.run();

        try
        {
            doMainXX();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    @Override
    public void replayAudio ()
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    setStatus(STATUS_CHECKING);
                    action_intro(showText, false);
                    setStatus(STATUS_AWAITING_CLICK);
                    //
                    _replayAudio();
                    //
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            long st = System.nanoTime();
                            waitAudio();
                            waitForSecs(FIRST_REMINDER_DELAY);
                            doReminderWithStatusTime(st);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void doAudio (String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "REPEAT");
        waitForSecs(0.3);
        //
        if (scene.equals("c"))
        {
            action_intro(showText, false);
            playSceneAudio("PROMPT", false);
        }
        else
        {
            playSceneAudio("PROMPT", true);
            waitForSecs(0.3);
            //
            action_intro(showText, false);
        }
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                doReminder();
            }
        });
    }


    public void doReminder () throws Exception
    {
        long stTime = statusTime;
        waitForSecs(FIRST_REMINDER_DELAY);
        doReminderWithStatusTime(stTime);
    }


    public void doReminderWithStatusTime (final long stTime) throws Exception
    {
        if (statusChanged(stTime)) return;
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                OBControl ra = MainViewController().topRightButton;
                try
                {
                    for (int i = 0; i < 3; i++)
                    {
                        lockScreen();
                        ra.setOpacity(0.3f);
                        invalidateControl(ra);
                        unlockScreen();
                        waitForSecs(0.2);
                        //
                        lockScreen();
                        ra.setOpacity(1.0f);
                        invalidateControl(ra);
                        unlockScreen();
                        waitForSecs(0.2);
                    }
                }
                catch (Exception e)
                {
                    ra.setOpacity(1.0f);
                }
            }
        });
        //
        OBUtils.runOnOtherThreadDelayed(SECOND_REMINDER_DELAY, new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                doReminderWithStatusTime(stTime);
            }
        });
    }


    public void action_popup() throws Exception
    {
        if (currentEvent().equals("b"))
        {
            waitForSecs(0.3);
            playSfxAudio("popon", false);
            lockScreen();
            showControls("obj.*");
            unlockScreen();
            waitForSecs(0.5);
        }
        if (showText)
        {
            waitForSecs(0.4);
            playSfxAudio("wordon", false);
            lockScreen();
            label.show();
            unlockScreen();
            waitForSecs(0.3);
        }
    }

    @Override
    public void doMainXX () throws Exception
    {
        action_popup();
        //
        if (!performSel("demo", currentEvent()))
        {
            setStatus(STATUS_AWAITING_CLICK);
            doAudio(currentEvent());
        }
    }


    public void setScenefinale ()
    {
        // do nothing
    }


    @Override
    public void setSceneXX (String scene)
    {
        if ((scene.equals("b") && !events.contains("a")) || scene.equals("a"))
        {
            hideControls("obj.*");
        }
        //
        hideControls("pos.*");
        //
        for (int i = 1; i <= touchables.size(); i++)
        {
            OBGroup control = (OBGroup) objectDict.get(String.format("obj_%d", i));
            action_showState(control, "normal");
            action_showMouthFrame(control, "mouth_0");
            control.setProperty("value", sets.get(currNo).get(i - 1));
        }
        //
        if (scene.equals("a") || scene.equals("finale")) return;
        //
        if (mode.equals("word"))
        {
            if (label != null) detachControl(label);
            OBPhoneme word = wordComponents.get(answers.get(currNo));
            label = action_setupLabel(word.text);
            //
            OBControl marker = objectDict.get("pos_1");
            label.setPosition(marker.position());
        }
    }


    public void demoa () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        List aud = currentAudio("DEMO");
        //
        PointF position = XPRZ_Generic.copyPoint((PointF) presenter.control.propertyValue("restPos"));
        presenter.walk(position);
        presenter.faceFront();
        ;
        waitForSecs(0.2);
        presenter.speak(Arrays.asList(aud.get(0)), this); // This is about listening!
        waitForSecs(0.2);
        //
        PointF currPos = XPRZ_Generic.copyPoint(presenter.control.getWorldPosition());
        PointF destPos = new PointF(currPos.x + bounds().width() * 0.3f, currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3);
        //
        OBControl head = presenter.control.objectDict.get("head");
        head.setAnchorPoint(0.5f, 0.9f);
        presenter.selectArmIndex(1);
        head.setRotation((float) Math.toRadians(5));
        waitForSecs(0.1);
        presenter.selectArmIndex(2);
        head.setRotation((float) Math.toRadians(10));
        waitForSecs(0.1);
        lockScreen();
        presenter.control.objectDict.get("leftarms").hide();
        presenter.control.objectDict.get("armlisten").show();
        head.setRotation((float) Math.toRadians(15));
        unlockScreen();
        waitForSecs(0.2);
        presenter.speak(Arrays.asList(aud.get(1)), this); // So get your ears ready!
        waitForSecs(0.1);
        lockScreen();
        presenter.control.objectDict.get("leftarms").show();
        presenter.control.objectDict.get("armlisten").hide();
        head.setRotation((float) Math.toRadians(10));
        unlockScreen();
        waitForSecs(0.1);
        presenter.selectArmIndex(1);
        head.setRotation((float) Math.toRadians(5));
        waitForSecs(0.1);
        presenter.selectArmIndex(0);
        head.setRotation((float) Math.toRadians(0));
        //
        currPos = XPRZ_Generic.copyPoint(presenter.control.getWorldPosition());
        OBControl front = presenter.control.objectDict.get("front");
        destPos = new PointF(1.2f * bounds().width() + front.width(), currPos.y);
        presenter.walk(destPos);
        //
        nextScene();
    }


    public void demob () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.9f, 1.3f, 0f, 0.1f, true, this);
        XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.9f, 0.85f, -5f, 0.6f, false, this);
        //
        action_playNextDemoSentence(true); // Listen!
        waitForSecs(0.3);
        //
        action_intro(showText, false);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Which one said something different?
        waitForSecs(0.3);
        //
        if (showText)
        {
            PointF position = XPRZ_Generic.copyPoint(label.getWorldPosition());
            position.x += label.width() / (float) 2 + 0.05f * bounds().width();
            movePointerToPoint(position, -5f, 0.6f, true);
            waitAudio();
            waitForSecs(0.3);
        }
        else
        {
            XPRZ_Generic.pointer_moveToRelativePointOnScreen(0.9f, 0.85f, -5f, 1.2f, true, this);
            waitAudio();
            waitForSecs(0.3);
        }
        //
        String correctAnswer = answers.get(currNo);
        OBGroup correctHead = null;
        for (OBControl head : touchables)
        {
            String answer = (String) head.propertyValue("value");
            if (correctAnswer.equals(answer)) correctHead = (OBGroup) head;
        }
        //
        action_playNextDemoSentence(false); // This one!
        PointF middle = OB_Maths.locationForRect(0.5f, 0.65f, correctHead.frame());
        movePointerToPoint(middle, 0, 0.6f, true);
        //
        playSfxAudio("touch", false);
        lockScreen();
        if (mode.equals("word") || parameters.get("answers") != null)
        {
            action_showState(correctHead, "matched");
        }
        else
        {
            action_showState(correctHead, "correct");
        }
        unlockScreen();
        waitAudio();
        //
        PointF position = XPRZ_Generic.copyPoint(correctHead.getWorldPosition());
        position.y += 0.3f * bounds().height();
        movePointerToPoint(position, 0, 0.9f, true);
        waitForSecs(0.3);
        //
        action_intro(showText, true);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Remember, this lets you listen again.
        PointF replayAudioPosition = OB_Maths.locationForRect(0.5f, 1.1f, new RectF(MainViewController().topRightButton.frame));
        movePointerToPoint(replayAudioPosition, 10f, 1.2f, true);
        waitAudio();
        waitForSecs(0.7);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        if (showText)
        {
            playSfxAudio("wordoff", false);
            lockScreen();
            label.hide();
            unlockScreen();
            waitForSecs(0.4);
        }
        else
        {
            waitForSecs(0.7);
        }
        //
        action_playNextDemoSentence(true); // Your Turn!
        //
        currNo++;
        //
        nextScene();
    }


    public void demofinale () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playSceneAudio("FINAL", true);
        waitForSecs(0.3);
        //
        nextScene();
    }


    public void action_highlightLabel (OBLabel label, Boolean high)
    {
        if (label == null) return;
        //
        int colour = 0;
        lockScreen();
        if (high)
        {
            if (mode.equals("word") && parameters.get("answers") != null)
            {
                colour = OBUtils.colorFromRGBString("124,201,119");
            }
            else
            {
                colour = Color.RED;
            }
        }
        else
        {
            colour = Color.BLACK;
        }
        action_setColourForLabel(label, colour);
        unlockScreen();
    }


    public void action_intro (Boolean showLabel, Boolean highlight) throws Exception
    {
        if (mode.equals("word"))
        {
            if (showLabel)
            {
                label.show();
                waitForSecs(0.3);
                //
                if (highlight)
                {
                    long st = statusTime;
                    String correctAnswer = answers.get(currNo);
                    //
                    for (int i = 1; i <= touchables.size(); i++)
                    {
                        OBGroup head = (OBGroup) objectDict.get(String.format("obj_%d", i));
                        String value = (String) head.propertyValue("value");
                        //
                        if (!value.equals(correctAnswer)) continue;
                        if (statusChanged(st)) break;
                        //
                        action_playSound(head, st, highlight);
                        //
                        if (statusChanged(st)) break;
                        waitForSecs(0.3);
                    }
                    return;
                }
            }
        }
        long st = statusTime;
        for (int i = 1; i <= touchables.size(); i++)
        {
            OBGroup head = (OBGroup) objectDict.get(String.format("obj_%d", i));
            //
            if (statusChanged(st)) break;
            action_playSound(head, st, highlight);
            //
            if (statusChanged(st)) break;
            waitForSecs(0.3);
        }
    }


    public void action_playSound (OBGroup object, long st, Boolean highlight) throws Exception
    {
        String value = (String) object.propertyValue("value");
        String correctAnswer = answers.get(currNo);
        //
        if (mode.equals("word") || mode.equals("syllable") || mode.equals("phoneme"))
        {
            OBPhoneme word = wordComponents.get(value);
            String audioFile = word.audio();
            //
            if (audioFile == null)
            {
                System.out.println("X_Th2.action_playSound.empty filename: " + word.toString());
            }
            String state = (String) object.propertyValue("state");
            OBGroup stateGroup = (OBGroup) object.objectDict.get(state);
            //
            lockScreen();
            OBControl talkingFrame = stateGroup.objectDict.get("talking");
            if (talkingFrame != null) talkingFrame.show();
            unlockScreen();
            //
            playAudio(audioFile);
            double duration = OBAudioManager.audioManager.duration();
            action_highlightLabel(label, highlight && value.equals(correctAnswer));
            //
            if (mode.equals("word"))
            {
                OBWord rw = (OBWord) wordComponents.get(value);
                double startTime = XPRZ_Generic.currentTime();
                double timePerSyllable = duration / (double) rw.syllables().size();
                int i = 0;
                //
                for (OBSyllable syllable : rw.syllables())
                {
                    Double currTime = XPRZ_Generic.currentTime() - startTime;
                    Double waitTime = timePerSyllable * i - currTime;
                    //
                    if (statusChanged(st)) break;
                    if (waitTime > 0) waitForSecs(waitTime);
                    if (statusChanged(st)) break;
                    //
                    action_showMouthFrameForText(object, syllable.text, false);
                    waitForSecs(0.1);
                    if (i < rw.syllables().size())
                        action_showMouthFrameForText(object, syllable.text, true);
                    i++;
                }
                if (!statusChanged(st)) waitAudio();
            }
            else
            {
                action_showMouthFrameForText(object, value, false);
                waitForSecs(0.15);
                action_showMouthFrameForText(object, value, true);
                waitForSecs(0.15);
                waitAudio();
            }
            //
            action_highlightLabel(label, false);
            //
            lockScreen();
            stateGroup.objectDict.get("talking").hide();
            action_showMouthFrame(object, "mouth_0");
            unlockScreen();
        }
        else
        {
            OBPath dot = (OBPath) object.objectDict.get("dot");
            float duration = 0.3f;
            playSFX(value);
            dot.setFillColor(Color.RED);
            //
            OBAnim scaleAnim = OBAnim.scaleAnim(1.2f, dot);
            OBAnimationGroup.runAnims(Arrays.asList(scaleAnim), duration * 0.5f, true, OBAnim.ANIM_LINEAR, this);
            //
            scaleAnim = OBAnim.scaleAnim(1.0f, dot);
            OBAnimationGroup.runAnims(Arrays.asList(scaleAnim), duration * 0.5f, true, OBAnim.ANIM_LINEAR, this);
            //
            waitSFX();
            dot.setFillColor(Color.BLACK);
        }
    }


    public void action_showState (OBGroup control, String state)
    {
        control.hideMembers("normal");
        control.hideMembers("paired");
        control.hideMembers("correct");
        control.hideMembers("matched");
        control.showMembers(state);
        //
        control.setProperty("state", state);
        //
        OBControl controlState = control.objectDict.get(state);
        if (controlState != null && OBGroup.class.isInstance(controlState))
        {
            OBGroup group = (OBGroup) controlState;
            group.hideMembers("talking");
        }
        controlState.setNeedsRetexture();
        control.setNeedsRetexture();
    }


    public void action_showMouthFrame (OBGroup control, String frame)
    {
        lockScreen();
        control.hideMembers("mouth_.*");
        OBControl controlFrame = control.objectDict.get(frame);
        if (controlFrame != null)
        {
            controlFrame.show();
        }
        control.setNeedsRetexture();
        unlockScreen();
    }


    public void action_showMouthFrameForText (OBGroup control, String text, Boolean endFrame)
    {
        List<String> vowels = Arrays.asList("a", "e", "i", "o", "u");
        for (String vowel : vowels)
        {
            if (text.contains(vowel))
            {
                String frame = String.format("mouth_%s%s", vowel, (endFrame ? "_end" : ""));
                action_showMouthFrame(control, frame);
                return;
            }
        }
        String frame = (endFrame ? "mouth_2" : "mouth_1");
        action_showMouthFrame(control, frame);
    }


    public void checkObject (OBGroup control)
    {
        setStatus(STATUS_CHECKING);
        //
        try
        {
            String correctAnswer = answers.get(currNo);
            String value = (String) control.propertyValue("value");
            Boolean answerIsCorrect = correctAnswer.equals(value);
            //
            playSfxAudio("touch", false);
            //
            if (answerIsCorrect)
            {
                lockScreen();
                if (mode.equals("word") && parameters.get("answers") != null)
                {
                    action_showState(control, "matched");
                }
                else
                {
                    action_showState(control, "correct");
                }
                unlockScreen();
                waitForSecs(0.3);
                //
                action_intro(showText, true);
                waitForSecs(0.3);
                //
                lockScreen();
                for (OBControl head : touchables)
                {
                    action_showMouthFrame((OBGroup) head, "mouth_7");
                }
                unlockScreen();
                waitForSecs(0.3);
                //
                gotItRightBigTick(showTick);
                waitForSecs(0.3);
                //
                currNo++;
                if (currNo < sets.size())
                {
                    waitForSecs(0.3);
                }
                //
                if (showText)
                {
                    playSfxAudio("wordoff", false);
                    lockScreen();
                    label.hide();
                    unlockScreen();
                    waitForSecs(0.3);
                }
                //
                nextScene();
            }
            else
            {
                control.highlight();
                waitForSecs(0.3);
                //
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                control.lowlight();
                setStatus(STATUS_AWAITING_CLICK);
                //
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        long st = statusTime;
                        playSceneAudioIndex("INCORRECT", 0, true);
                        if (statusChanged(st)) return;
                        waitForSecs(0.3);
                        //
                        action_intro(showText, false);
                        //
                        playSceneAudioIndex("INCORRECT", 1, true);
                        if (statusChanged(st)) return;
                        waitForSecs(0.3);
                        //
                        playSceneAudioIndex("INCORRECT", 2, true);
                        if (statusChanged(st)) return;
                        waitForSecs(0.3);
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBGroup findObject (PointF pt)
    {
        return (OBGroup) finger(-1, 2, (List<OBControl>) (Object) touchables, pt, true);
    }

    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBGroup obj = findObject(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkObject(obj);
                    }
                });

            }
        }
    }

}
