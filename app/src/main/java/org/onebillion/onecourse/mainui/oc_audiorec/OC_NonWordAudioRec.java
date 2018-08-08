package org.onebillion.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.SystemClock;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBSyllable;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_NonWordAudioRec extends OC_AudioRecSection
{
    OBLabel targetLabel;
    List<OBWord> targetWords;
    OBGroup avatar;
    OBWord targetWord;
    double wordAudioDuration;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        //hideControls("indicator.*");
        Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);
        objectDict.get("frame").setZPosition(12);
        objectDict.get("bg").show();
        targetWords = new ArrayList<>();
        String[] arr = parameters.get("words").split(",");
        int index = 1;
        List<String> eventsList = new ArrayList<>();
        for(String param : arr)
        {
            OBWord word = (OBWord)componentDict.get(param);
            if(word != null)
                targetWords.add(word);
            eventsList.add(eventForSceneNum(index));
            index++;
        }
        events = eventsList;
        OBGroup window = (OBGroup)objectDict.get("window");
        float top = window.top();
        window.recalculateFrameForPath(Arrays.asList(objectDict.get("frame")));
        window.setTop(top);
        avatar =(OBGroup) window.objectDict.get("avatar");
        avatarShowSmile();
        setSceneXX(currentEvent());
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        OBFont font = OBUtils.StandardReadingFontOfSize(120);
        targetWord = targetWords.get(eventIndex);
        targetLabel = labelForText(targetWord.text,font);
        OBAudioManager.audioManager.prepareForChannel(targetWord.audio(),"special");
        wordAudioDuration = OBAudioManager.audioManager.durationForChannel("special");
        prepareForRecordingEvent(targetWord.audio());

    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.3f);
        wordShow(targetLabel);
        demoSceneStart(targetLabel);
        waitForSecs(0.3f);
        playStartAudio("PROMPT",true);
        waitForSecs(0.3f);
        startRecordingEvent(targetLabel);
    }

    public void playTargetAudio() throws Exception
    {
        avatarSayTargetWord(-1);
    }

    public void recordingEventFinished() throws Exception
    {
        mainLabelAudioAndHighlight();
        waitForSecs(0.7f);
        super.recordingEventFinished();
    }

    public void mainLabelAudioAndHighlight() throws Exception
    {
        targetLabel.setColour(Color.BLUE);
        waitForSecs(0.3f);
        avatarSayTargetWord(-1);
        waitForSecs(0.5f);
        targetLabel.setColour(Color.BLACK);
    }

    public void replayModelAudio() throws Exception
    {
        long  token = takeSequenceLockInterrupt(true);
        try
        {
            if(token == sequenceToken)
            {
                targetLabel.setColour(Color.BLUE);
                avatarSayTargetWord(token);
            }
        }
        catch(Exception exception)
        {

        }
        targetLabel.setColour(Color.BLACK);
        sequenceLock.unlock();
    }

    public void wordShow(OBLabel curLabel) throws Exception
    {
        if(curLabel.hidden)
        {
            playSfxAudio("wordon",false);
            curLabel.show();
            waitSFX();
        }
    }

    public void nextButtonPressed() throws Exception
    {
        if(!isLastEvent())
        {
            lockScreen();
            avatarShowSmile();
            detachControl(targetLabel);
            targetLabel = null;
            nextButton.hide();
            unlockScreen();
            waitForSecs(0.5f);
        }
    }


    public String eventForSceneNum(int sceneNum)
    {
        String currentScene = String.format("%d",sceneNum);
        if(audioScenes.get(currentScene) != null)
        {
            return currentScene;
        }
        else
        {
            return "default";
        }
    }

    public void avatarSayTargetWord(long token) throws Exception
    {
        double timePerSyllable = wordAudioDuration / (double) targetWord.syllables().size();
        targetWord.playAudio(this,false);
        double startTime = SystemClock.uptimeMillis();
        int i = 0;
        for(OBSyllable syllable : targetWord.syllables)
        {
            double currTime = SystemClock.uptimeMillis() - startTime;
            double waitTime = timePerSyllable * i - currTime/1000.0;
            if(waitTime > 0.0)
                waitForSecs(waitTime);

            avatarShowMouthFrameForText(syllable.text,false);
            waitForSecs(0.1);
            if(i < targetWord.syllables.size())
                avatarShowMouthFrameForText(syllable.text,true);
            i++;
            if(token > -1)
                checkSequenceToken(token);
        }
        waitAudio();
        if(token > -1)
            checkSequenceToken(token);
        waitForSecs(0.1f);
        avatarShowSmile();
    }

    public void avatarShowMouthFrame (String frame)
    {
        lockScreen();
        avatar.hideMembers("mouth_.*");
        OBGroup window = (OBGroup)objectDict.get("window");
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

    public void avatarShowSmile()
    {
        lockScreen();
        avatar.hideMembers("mouth_.*");
        avatar.objectDict.get("mouth_0").show();
        unlockScreen();
    }

    public void demoInitScreen() throws Exception
    {
        waitForSecs(0.3f);
        playSfxAudio("tilly_on",false);
        objectDict.get("window").show();
        waitSFX();
        super.demoInitScreen();

    }
}
