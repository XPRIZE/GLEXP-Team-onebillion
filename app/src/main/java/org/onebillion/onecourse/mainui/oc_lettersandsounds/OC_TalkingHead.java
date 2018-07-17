package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_WordsEvent;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBSyllable;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 28/06/16.
 */
public class OC_TalkingHead extends OC_Generic_WordsEvent
{
    protected static final float FIRST_REMINDER_DELAY = 6.0f;
    protected static final float SECOND_REMINDER_DELAY = 4.0f;


    List<List<OBPhoneme>> words;
    List<OBLabel> labels;
    List<OBPhoneme> answers;
    Boolean breakdown_phoneme, breakdown_syllable;
    Boolean phase2, showTick;
    OBGroup button, avatar, window;
    Boolean isReplayAudioPlaying;

    public OC_TalkingHead ()
    {
        super();
    }


    public void miscSetup ()
    {
        wordComponents = OBUtils.LoadWordComponentsXML(true);
        //
        textSize = Float.parseFloat(eventAttributes.get("textsize"));
        //
        String breakdown = parameters.get("breakdown");
        breakdown_phoneme = breakdown != null && breakdown.equals("phoneme");
        breakdown_syllable = breakdown != null && breakdown.equals("syllable");
        //
        words = new ArrayList<List<OBPhoneme>>();
        String ws = parameters.get("words");
        String sets[] = ws.split(";");
        for (String set : sets)
        {
            String items[] = set.split(",");
            List<OBPhoneme> options = new ArrayList<OBPhoneme>();
            for (String item : items)
            {
                options.add((OBPhoneme) wordComponents.get(item));
            }
            words.add(options);
        }
        //
        answers = new ArrayList<OBPhoneme>();
        String as = parameters.get("answers");
        if (as == null)
        {
            for (int i = 0; i < words.size(); i++)
            {
                int index = OC_Generic.randomInt(0, words.get(i).size() - 1);
                answers.add(words.get(i).get(index));
            }
        }
        else
        {
            String answers_array[] = as.split(",");
            for (String answer : answers_array)
            {
                answers.add(wordComponents.get(answer));
            }
        }
        //
        needDemo = parameters.get("demo").equals("true");
        showTick = true;
        currNo = 0;
        button = (OBGroup) objectDict.get("button");
        avatar = (OBGroup) objectDict.get("avatar");
        window = (OBGroup) objectDict.get("window");
        //
        isReplayAudioPlaying = false;
    }


    @Override
    public void prepare ()
    {
        super.prepare();
        loadFingers();
        loadEvent("master");
        miscSetup();
        //
        int totalEvents = (needDemo) ? words.size() - 1 : words.size();
        events = new ArrayList<>(Arrays.asList("c", "d", "e"));
        while (events.size() < totalEvents) events.add(events.get(events.size() - 1));
        while (events.size() > totalEvents) events.remove(events.size() - 1);
        //
        events.add("finale");
        if (needDemo) events.add(0, "b");
        events.add(0, "a");
        //
        doVisual(currentEvent());
        //
        setStatus(STATUS_BUSY);
    }


    @Override
    public void setSceneXX (String scene)
    {
        int presenterColour = OBConfigManager.sharedManager.getSkinColour(0);
        avatar.substituteFillForAllMembers("colour.*", presenterColour);
        //
        buttonShowState("inactive");
        avatarShowMouthFrame("mouth_0");
        phase2 = false;
        //
        hideControls("pos.*");
        if (scene.equals("a")) return;
        if (scene.equals("finale")) return;
        //
        OBControl frame = objectDict.get("frame");
        float midWayX = frame.right() + (bounds().width() - frame.right()) / 2;
        //
        if (labels != null)
        {
            for (OBLabel label : labels)
            {
                detachControl(label);
            }
        }
        //
        labels = new ArrayList<>();
        //
        List<OBPhoneme> set = words.get(currNo);
        int wordsPerSet = set.size();
        for (int i = 1; i <= wordsPerSet; i++)
        {
            OBPhoneme word = set.get(i - 1);
            if (word != null)
            {
                OBLabel label = action_setupLabel(word.text);
                labels.add(label);
                OBControl marker = objectDict.get(String.format("pos_%d_%d", wordsPerSet, i));
                label.setPosition(new PointF(midWayX, marker.position().y));
            }
        }
    }

    @Override
    public void doAudio (String scene) throws Exception
    {
        List audio, replayAudio;
        if (phase2)
        {
            audio = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("PROMPT2");
            replayAudio = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("REPEAT2");
            if (replayAudio == null)
            {
                replayAudio = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("REPEAT");
            }
        }
        else
        {
            audio = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("PROMPT");
            replayAudio = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("REPEAT");
        }
        setReplayAudio(replayAudio);
        setStatus(STATUS_AWAITING_CLICK);
        playAudioQueued(audio, false);
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                doReminder(true, statusTime);
            }
        });
    }


    @Override
    public void doMainXX () throws Exception
    {
        action_wordsIntro();
        playSceneAudio("DEMO", true);
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void demoa () throws Exception
    {
        try
        {
            setStatus(STATUS_DOING_DEMO);
            //
            //OBConditionLock lock = playSceneAudio("DEMO", false); // Let's listen, then pick out what she said!
            //waitForSecs(0.3);
            //waitAudioQueue(lock);
            //
            playSceneAudio("DEMO", true); // Let's listen, then pick out what she said!
            //
            nextScene();
        }
        catch (Exception e)
        {
            MainActivity.log("HELLO");
        }
    }


    public void demob () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_wordsIntro();
        //
        loadPointer(POINTER_MIDDLE);
        action_playNextDemoSentence(false); // I touch the button, and listen.
        OC_Generic.pointer_moveToObject(button, -15, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToObject(button, -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        //
        playSfxAudio("touchbutton", false);
        lockScreen();
        buttonShowState("selected");
        unlockScreen();
        waitForSecs(0.3);
        //
        PointF position = OC_Generic.copyPoint(thePointer.position());
        position.x += bounds().width() * 0.1;
        OBAnim moveAnim = OBAnim.moveAnim(position, thePointer);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        //
        avatarSay_word(false);
        waitForSecs(0.3);
        //
        lockScreen();
        buttonShowState("active");
        unlockScreen();
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Now I pick out what she said.
        position.x += bounds().width() * 0.1;
        moveAnim = OBAnim.moveAnim(position, thePointer);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 1.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitAudio();
        //
        OBLabel correctLabel = action_getCorrectLabel();
        OC_Generic.pointer_moveToObject(correctLabel, 5, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        selectWord(correctLabel);
        //
        lockScreen();
        buttonShowState("inactive");
        unlockScreen();
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToObject(correctLabel, 5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        if (breakdown_phoneme)
        {
            avatarSay_sounds();
        }
        else if (breakdown_syllable)
        {
            avatarSay_syllables();
        }
        else
        {
            avatarSay_word(true);
        }
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        action_wordsExit();
        action_playNextDemoSentence(true); // Your Turn!
        //
        currNo++;
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


    public OBLabel action_getCorrectLabel ()
    {
        for (int i = 0; i < words.get(currNo).size(); i++)
        {
            if (answers.get(currNo).equals(words.get(currNo).get(i)))
            {
                return labels.get(i);
            }
        }
        return null;
    }


    public void doReminder (Boolean playReminder, long timestamp) throws Exception
    {
        if (statusTime != timestamp) return;
        //
        long stTime = System.nanoTime();
        if (playReminder)
        {
            waitForSecs(FIRST_REMINDER_DELAY);
        }
        else
        {
            waitForSecs(SECOND_REMINDER_DELAY);
        }
        doReminderWithStatusTime(stTime, playReminder);
    }


    public void doReminderWithStatusTime (long stTime, Boolean playReminder) throws Exception
    {
        if (statusChanged(stTime)) return;
        //
        if (playReminder)
        {
            List<Object> reminder;
            if (phase2)
                reminder = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("REMINDER2");
            else
                reminder = (List<Object>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("REMINDER");
            //
            if (reminder != null) playAudioQueued(reminder, false);
        }
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                for (int i = 0; i < 2; i++)
                {
                    lockScreen();
                    buttonShowState("selected");
                    unlockScreen();
                    waitForSecs(0.3);
                    //
                    lockScreen();
                    buttonShowState("active");
                    unlockScreen();
                    waitForSecs(0.3);
                }
            }
        });
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                doReminder(false , statusTime);
            }
        });
    }


    public void buttonShowState (String state)
    {
        button.objectDict.get("selected").hide();
        button.objectDict.get("inactive").hide();
        OBControl layer = button.objectDict.get(state);
        if (layer != null) layer.show();
        if (state.equals("inactive")) button.disable();
        else button.enable();
    }

    public void action_wordsIntro () throws Exception
    {
        playSfxAudio("wordon", false);
        lockScreen();
        for (OBLabel label : labels)
        {
            label.show();
        }
        buttonShowState("active");
        unlockScreen();
        waitForSecs(0.5);
    }


    public void action_wordsExit () throws Exception
    {
        playSfxAudio("wordsoff", false);
        lockScreen();
        for (OBLabel label : labels)
        {
            label.hide();
        }
        unlockScreen();
        waitForSecs(0.5);
    }


    public void avatarSay_sounds () throws Exception
    {
        OBWord correctAnswer = (OBWord) answers.get(currNo);
        OBLabel label = action_getCorrectLabel();
        //
        List<OBPhoneme> breakdown = correctAnswer.phonemes();
        //
        String filename = new String(correctAnswer.audio()).replace("fc_", "fc_let_");
        AssetFileDescriptor fd = OBAudioManager.audioManager.getAudioPathFD(filename);
        if (fd == null)
        {
            Integer startRange = 0;
            //
            for (OBPhoneme sound : breakdown)
            {
                playAudio(sound.audio());
                //
                Integer endRange = startRange + sound.text.length();
                action_highlightWord(correctAnswer, label, startRange, endRange, true);
                //
                avatarShowMouthFrameForText(sound.text, false);
                waitForSecs(0.15);
                if (_aborting) return;
                //
                avatarShowMouthFrameForText(sound.text, true);
                waitAudio();
                if (_aborting) return;
                //
                waitForSecs(0.15);
                if (_aborting) return;
                //
                avatarShowMouthFrame("mouth_2");
                //
                startRange += sound.text.length();
            }
        }
        else
        {
            playAudio(filename);
            //
            Double startTime = OC_Generic.currentTime();
            Integer startRange = 0;
            //
            for (OBPhoneme sound : breakdown)
            {
                Double timeStart = (Double) sound.timings.get(0);
                Double timeEnd = (Double) sound.timings.get(1);
                //
                Double currTime = OC_Generic.currentTime() - startTime;
                Double waitTime = timeStart - currTime;
                if (waitTime > 0.0) waitForSecs(waitTime);
                if (_aborting) return;
                //
                Integer endRange = startRange + sound.text.length();
                action_highlightWord(correctAnswer, label, startRange, endRange, true);
                //
                avatarShowMouthFrameForText(sound.text, false);
                waitForSecs(0.15);
                if (_aborting) return;
                //
                avatarShowMouthFrameForText(sound.text, true);
                waitForSecs(0.15);
                if (_aborting) return;
                //
                currTime = OC_Generic.currentTime();
                waitTime = timeEnd - currTime;
                if (waitTime > 0) waitForSecs(waitTime);
                if (_aborting) return;
                //
                avatarShowMouthFrame("mouth_2");
                //
                startRange += sound.text.length();
            }
            waitAudio();
            if (_aborting) return;
        }
        //
        action_highlightWord(correctAnswer, label, 0, correctAnswer.text.length(), false);
        avatarShowMouthFrame("mouth_0");
        //
        waitForSecs(0.3);
        if (_aborting) return;
        //
        avatarSay_word(true);
        //
        waitForSecs(0.3);
        if (_aborting) return;
    }


    public void avatarSay_syllables ()
    {
        try
        {
            OBWord correctAnswer = (OBWord) answers.get(currNo);
            OBLabel label = action_getCorrectLabel();
            //
            List<OBSyllable> breakdown = correctAnswer.syllables();
            //
            String filename = new String(correctAnswer.audio()).replace("fc_", "fc_syl_");
            playAudio(filename);
            //
            Double startTime = OC_Generic.currentTime();
            Integer startRange = 0;
            //
            for (OBSyllable syllable : breakdown)
            {
                Double timeStart = (Double) syllable.timings.get(0);
                Double timeEnd = (Double) syllable.timings.get(1);
                //
                Double currTime = OC_Generic.currentTime() - startTime;
                Double waitTime = timeStart - currTime;
                if (waitTime > 0.0) waitForSecs(waitTime);
                if (_aborting) return;
                //
                Integer endRange = startRange + syllable.text.length();
                action_highlightWord(correctAnswer, label, startRange, endRange, true);
                //
                avatarShowMouthFrameForText(syllable.text, false);
                waitForSecs(0.15);
                if (_aborting) return;
                //
                avatarShowMouthFrameForText(syllable.text, true);
                waitForSecs(0.15);
                if (_aborting) return;
                //
                currTime = OC_Generic.currentTime();
                waitTime = timeEnd - currTime;
                if (waitTime > 0) waitForSecs(waitTime);
                if (_aborting) return;
                //
                avatarShowMouthFrame("mouth_2");
                //
                startRange += syllable.text.length();
            }
            waitAudio();
            action_highlightWord(correctAnswer, label, 0, correctAnswer.text.length(), false);
            avatarShowMouthFrame("mouth_0");
            //
            waitForSecs(0.3);
            if (_aborting) return;
            //
            avatarSay_word(true);
            //
            waitForSecs(0.3);
            if (_aborting) return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void avatarSay_word (Boolean highlight) throws Exception
    {
        OBPhoneme answer = answers.get(currNo);
        OBLabel label = action_getCorrectLabel();
        //
        if (OBWord.class.isInstance(answer))
        {
            OBWord correctAnswer = (OBWord) answer;
            //
            double startTime = OC_Generic.currentTime();
            playAudio(correctAnswer.audio());
            double duration = OBAudioManager.audioManager.duration();
            double timePerSyllable = duration / (double) correctAnswer.syllables().size();
            action_highlightLabel(label, highlight);
            //
            int i = 0;
            for (OBSyllable syllable : correctAnswer.syllables())
            {
                Double currTime = OC_Generic.currentTime() - startTime;
                Double waitTime = timePerSyllable * i - currTime;
                if (waitTime > 0) waitForSecs(waitTime);
                if (_aborting) return;
                //
                avatarShowMouthFrameForText(syllable.text, false);
                waitForSecs(0.1);
                if (_aborting) return;
                if (i < correctAnswer.syllables().size())
                    avatarShowMouthFrameForText(syllable.text, true);
                i++;
            }
            waitAudio();
            if (_aborting) return;
            action_highlightLabel(label, false);
        }
        else
        {
            playAudio(answer.audio());
            action_highlightLabel(label, highlight);
            avatarShowMouthFrameForText(answer.text, false);
            waitAudio();
            if (_aborting) return;
            avatarShowMouthFrameForText(answer.text, true);
            waitForSecs(0.1);
            if (_aborting) return;
            //
            waitAudio();
            action_highlightLabel(label, false);
        }
        avatarShowMouthFrame("mouth_0");
    }


    public void avatarShowMouthFrame (String frame)
    {
        lockScreen();
        avatar.hideMembers("mouth_.*");
        OBControl mouth = avatar.objectDict.get(frame);
        if (mouth != null)
        {
            mouth.show();
            mouth.setNeedsRetexture();
            if (window != null)
            {
                window.setNeedsRetexture();
            }
            avatar.setNeedsRetexture();
        }
        unlockScreen();
    }


    public void avatarShowMouthFrameForText (String text, Boolean endFrame)
    {
        List<String> vowels = Arrays.asList("a", "e", "i", "o", "u");
        for (String vowel : vowels)
        {
            if (text.contains(vowel))
            {
                String frame = String.format("mouth_%s%s", vowel, (endFrame ? "_end" : ""));
                avatarShowMouthFrame(frame);
                return;
            }
        }
        String frame = (endFrame ? "mouth_2" : "mouth_1");
        avatarShowMouthFrame(frame);
    }


    public void action_highlightWord (OBWord word, OBLabel label, Integer startRange, Integer endRange, Boolean high)
    {
        lockScreen();
        if (high)
        {
            ((OBTextLayer) label.layer).setHighRange(startRange, endRange, Color.RED);
            label.setNeedsRetexture();
        }
        else
        {
            ((OBTextLayer) label.layer).setHighRange(-1, -1, Color.BLACK);
            label.setColour(Color.BLACK);
            label.setNeedsRetexture();
        }
        unlockScreen();
    }


    public Boolean selectWord (OBLabel targetLabel) throws Exception
    {
        OBLabel correctLabel = action_getCorrectLabel();
        Boolean answerIsCorrect = targetLabel != null && targetLabel.equals(correctLabel);
        //
        if (targetLabel != null) playSfxAudio("touchword", false);
        //
        lockScreen();
        for (OBLabel label : labels)
        {
            if (targetLabel == null)
            {
                label.setOpacity(1.0f);
            }
            else if (targetLabel.equals(correctLabel))
            {
                float opacity = (targetLabel.equals(label)) ? 1.0f : 0.3f;
                label.setOpacity(opacity);
            }
            else
            {
                float opacity = (targetLabel.equals(label)) ? 0.3f : 1.0f;
                label.setOpacity(opacity);
            }
        }
        unlockScreen();
        //
        if (targetLabel != null) waitForSecs(0.3);
        //
        return answerIsCorrect;
    }


    public void checkButton ()
    {
        try
        {
            setStatus(STATUS_CHECKING);
            playSfxAudio("touchbutton", false);
            lockScreen();
            buttonShowState("selected");
            unlockScreen();
            waitForSecs(0.6);
            //
            avatarSay_word(false);
            for (OBLabel label : labels) label.enable();
            //
            if (!phase2)
            {
                phase2 = true;
                doAudio(currentEvent());
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }
            waitForSecs(0.3);
            //
            lockScreen();
            buttonShowState("active");
            unlockScreen();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBControl findButton (PointF pt)
    {
        return finger(-1, 2, Arrays.asList((OBControl) button), pt, true);
    }


    public void checkLabel (OBLabel label)
    {
        setStatus(STATUS_CHECKING);
        //
        try
        {
            if (selectWord(label))
            {
                waitSFX();
                //
                lockScreen();
                buttonShowState("inactive");
                unlockScreen();
                //
                if (breakdown_phoneme) avatarSay_sounds();
                else if (breakdown_syllable) avatarSay_syllables();
                else avatarSay_word(true);
                //
                waitAudio();
                waitForSecs(0.3);
                //
                gotItRightBigTick(showTick);
                waitForSecs(0.3);
                //
                currNo++;
                if (currNo < words.size())
                {
                    waitForSecs(0.7);
                    action_wordsExit();
                    waitForSecs(0.3);
                }
                nextScene();
            }
            else
            {
                waitSFX();
                //
                gotItWrongWithSfx();
                waitForSecs(0.3);
                selectWord(null);
                playSceneAudio("INCORRECT", false);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    OBLabel findLabel (PointF pt)
    {
        return (OBLabel) finger(-1, 2, (List<OBControl>) (Object) labels, pt, true);
    }


    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            OBControl obj = findButton(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkButton();
                    }
                });
            }
            else
            {
                final OBLabel label = findLabel(pt);
                if (label != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            checkLabel(label);
                        }
                    });
                }
            }
        }
    }

    @Override
    public void replayAudio ()
    {
        final OC_TalkingHead sc = this;
        if (!isReplayAudioPlaying && status() == STATUS_AWAITING_CLICK)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    if (_replayAudio != null)
                    {
                        isReplayAudioPlaying = true;
                        //
                        setStatus(status());
                        new AsyncTask<Void, Void, Void>()
                        {
                            protected Void doInBackground (Void... params)
                            {
                                _replayAudio();
                                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                {
                                    @Override
                                    public void run () throws Exception
                                    {
                                        try
                                        {
                                            sc.doReminder(true, statusTime);
                                        }
                                        catch (Exception e)
                                        {

                                        }
                                    }
                                });
                                isReplayAudioPlaying = false;
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    }
                    else
                    {
                        MainActivity.log("OC_TalkingHead: Replay Audio is empty");
                    }
                }
            });
        }
        else
        {
            MainActivity.log("OC_TalkingHead: Replay Audio is still playing or status is not waiting for click");
        }
    }

//    @Override
//    protected void _replayAudio ()
//    {
//        try
//        {
//            playAudioQueued(_replayAudio, true);
//            //
//            doReminder(true);
//        }
//        catch (Exception exception)
//        {
//        }
//        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
//        {
//            @Override
//            public void run () throws Exception
//            {
//                playAudioQueued(_replayAudio, true);
//                //
//                doReminder(true);
//            }
//        });
//    }


}
