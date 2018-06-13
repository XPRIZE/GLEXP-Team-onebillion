package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.onebillion.onecourse.utils.OB_Maths.randomInt;

/**
 * Created by pedroloureiro on 12/02/2018.
 */

public class OC_Th7 extends OC_TalkingHead
{
    OBConditionLock finishLock;
    OBConditionLock promptAudioLock;

    public void miscSetUp ()
    {
        wordComponents = OBUtils.LoadWordComponentsXML(true);
        //
        textSize = Float.parseFloat(eventAttributes.get("textsize"));
        breakdown_syllable = parameters.get("breakdown").equals("syllable");
        words = new ArrayList<>();
        String ws = parameters.get("words");
        List<String> sets = Arrays.asList(ws.split(";"));
        for (String set : sets)
        {
            String items[] = set.split(",");
            List options = new ArrayList<>();
            for (String item : items)
            {
                options.add(wordComponents.get(item));
            }
            words.add(options);
        }
        answers = new ArrayList<>();
        String as = parameters.get("answers");
        if (as == null)
        {
            for (int i = 0; i < words.size(); i++)
            {
                int index = randomInt(0, words.get(i).size() - 1);
                answers.add(words.get(i).get(index));
            }
        }
        else
        {
            String[] answers_array = as.split(",");
            for (int i = 0; i < answers_array.length; i++)
            {
                answers.add(wordComponents.get(answers_array[i]));
            }
        }
        needDemo = parameters.get("demo").equals("true");
        showTick = true;
        this.currNo = 0;
        //finishLock = [NSConditionLock.alloc() init];
        button = (OBGroup) objectDict.get("button");
        avatar = (OBGroup) objectDict.get("avatar");
    }

    public void setSceneXX (String scene)
    {
        int presenterColour = OBConfigManager.sharedManager.getSkinColour(OBConfigManager.sharedManager.getPresenterColourIndex());
        avatar.substituteFillForAllMembers("colour.*", presenterColour);
        buttonShowState("inactive");
        avatarShowMouthFrame("mouth_0");
        phase2 = false;
        hideControls("pos.*");
        if (scene.equals("finale")) return;
        OBControl frame = objectDict.get("frame");
        float midWayX = frame.right() + (bounds().width() - frame.right()) / 2;
        if (scene.equals("b") || (!events.contains("b") && scene.equals("c")))
        {
            if (scene.equals("c"))
            {
                thePointer.hide();
            }
            return;
        }
        if (labels != null)
        {
            for (OBLabel label : labels)
            {
                detachControl(label);
            }
        }
        labels = new ArrayList<>();
        List set = words.get(currNo);
        int wordsPerSet = set.size();
        for (int i = 1; i <= wordsPerSet; i++)
        {
            OBPhoneme word = (OBPhoneme) set.get(i - 1);
            OBLabel label = action_setupLabel(word.text);
            labels.add(label);
            OBControl marker = objectDict.get(String.format("pos_%d_%d", wordsPerSet, i));
            label.setPosition(new PointF(midWayX, marker.position().y));
        }
    }

    public void setupEvents ()
    {
        loadFingers();
        loadEvent("master");
        miscSetUp();
        int totalEvents = (needDemo) ? words.size() - 1 : words.size();
        //
        String events_string = "c,d,e";
        events = Arrays.asList(events_string.split(","));
        while (events.size() < totalEvents) events.add(events.get(events.size() - 1));
        while (events.size() > totalEvents) events.remove(events.size() - 1);
        events.add("finale");
        if (needDemo) events.add(0, "b");
        events.add(0, "a");
        doVisual(currentEvent());
    }

    public void _replayAudio ()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                promptAudioLock = playAudioQueued(_replayAudio, false);
                doReminder(true, statusTime);
            }
        });
    }


    public void doMainXX () throws Exception
    {
        if (!performSel("demo", currentEvent()))
        {
            if (events.contains("b") || !currentEvent().equals("c"))
            {
                action_wordsIntro();
            }
            else
            {
                buttonShowState("active");
            }
            playAudioQueuedScene("DEMO", 0.3f, true);
            setStatus(STATUS_AWAITING_CLICK);
            doAudio(currentEvent());
        }
    }


    public void demoa () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Tilly makes up words!;
        OBControl frame = objectDict.get("frame");
        movePointerToPoint(new PointF(frame.bottomRight().x + bounds().width() * 0.05f, frame.bottomRight().y), -15, 0.9f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_wordsIntro();
        playAudioScene("DEMO", 1, false); // Look. She made up these words.;
        OBControl firstLabel = labels.get(0);
        movePointerToPoint(new PointF(firstLabel.bottomRight().x + bounds().width() * 0.05f, firstLabel.bottomRight().y), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // They are not real words;
        OBControl lastLabel = labels.get(labels.size() - 1);
        movePointerToPoint(new PointF(lastLabel.bottomRight().x + bounds().width() * 0.05f, lastLabel.bottomRight().y), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        if (!needDemo)
        {
            movePointerToPoint(new PointF(bounds().width() * 0.5f, bounds().height() * 1.1f), 0, 0.6f, true);
            thePointer.hide();
        }
        nextScene();
    }


    public void demob () throws Exception
    {
        setStatus(STATUS_BUSY);
        lockScreen();
        buttonShowState("active");
        unlockScreen();
        //
        playAudioScene("DEMO", 0, false); // Now watch. I touch the button.;
        OC_Generic.pointer_moveToObject(button, -15, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(button, -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("touchbutton", false);
        lockScreen();
        buttonShowState("selected");
        unlockScreen();
        waitForSecs(0.3f);
        //
        PointF position = OC_Generic.copyPoint(thePointer.position());
        position.x += bounds().width() * 0.1;
        thePointer.moveToPoint(position, 0.6f, false);
        avatarSay_word(false);
        waitForSecs(0.3f);
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        lockScreen();
        buttonShowState("active");
        unlockScreen();
        playAudioScene("DEMO", 1, false); // I pick out the made-up word she said.;
        position.x += bounds().width() * 0.1;
        thePointer.moveToPoint(position, 1.2f, true);
        waitAudio();
        OBLabel correctLabel = action_getCorrectLabel();
        OC_Generic.pointer_moveToObject(correctLabel, 5, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        selectWord(correctLabel);
        lockScreen();
        buttonShowState("inactive");
        unlockScreen();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(correctLabel, 5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        if (breakdown_syllable)
        {
            avatarSay_syllables();
        }
        else
        {
            avatarSay_word(true);
        }
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        action_wordsExit();
        playAudioScene("DEMO", 2, true); // Your turn!;
        waitForSecs(0.3f);
        //
        currNo++;
        nextScene();
    }


    public void demofinale() throws Exception
    {
        setStatus(STATUS_BUSY);
        playAudioQueuedScene("FINAL", 0.3f, true);
        waitForSecs(0.3f);
        //
        nextScene();
    }

    }