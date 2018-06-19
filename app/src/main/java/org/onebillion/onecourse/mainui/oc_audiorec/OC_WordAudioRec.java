package org.onebillion.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBAudioRecorder;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 07/07/16.
 */
public class OC_WordAudioRec extends OC_AudioRecSection
{

    static int MODE_LETTER=0,
            MODE_SYLLABLE=1,
            MODE_WORD=2;

    OBLabel targetLabel, feedbackLabel;
    List<Map<String,Object>> eventsData;
    OBControl screenImage;
    OBPhoneme targetPhoneme, feedbackPhoneme;
    boolean wordFeedback;
    int currentMode;




    public void prepare()
    {
        super.prepare();

        Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);

        objectDict.get("frame").setZPosition(12);
        OBControl shutter = objectDict.get("shutter");
        shutter.setZPosition(10);
        shutter.setAnchorPoint(new PointF(0f, 0.5f));
        shutter.setProperty("start_width",shutter.width());

        objectDict.get("bg").show();

        if(parameters.get("mode").equals("letter"))
        {
            currentMode = MODE_LETTER;

        }
        else if(parameters.get("mode").equals("syllable"))
        {
            currentMode = MODE_SYLLABLE;

        }
        else
        {
            currentMode = MODE_WORD;

        }

        eventsData = new ArrayList<>();

        wordFeedback = OBUtils.getBooleanValue(parameters.get("wordfeedback"));

        loadAudioXML(getConfigPath(currentMode == MODE_WORD ? "wrec1audio.xml" : "wrec2audio.xml"));

        String[] arr = parameters.get("words").split(";");
        List<String> eventsList = new ArrayList<>();
        int index = 1;
        for(String param : arr)
        {
            String[] par = param.split(",");
            Map<String,Object> dict = new ArrayMap();

            if(!componentDict.containsKey(par[0]))
                continue;
            OBPhoneme word = componentDict.get(par[0]);
            dict.put("image",word);
            if(par.length == 1)
            {
                dict.put("target",word);

            }
            else
            {
                OBPhoneme pho = componentDict.get(par[1]);
                if(pho == null)
                    continue;
                dict.put("target",pho);
                dict.put("feedback",word);
            }
            eventsList.add(eventForSceneNum(index));
            index++;
            eventsData.add(dict);
        }
        events = eventsList;
        setSceneXX(currentEvent());
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        Map<String,Object> curWord = eventsData.get(eventIndex);

        Typeface font = OBUtils.standardTypeFace();
        float textSize = currentMode == MODE_WORD ? applyGraphicScale(120) : applyGraphicScale(140);
        OBFont obFont = new OBFont(font,textSize);
        if(screenImage != null)
            detachControl(screenImage);

       // hideAndResetLeftButtons();

        targetPhoneme = (OBPhoneme) curWord.get("target");
        if(targetLabel == null)
        {
            targetLabel = labelForText(targetPhoneme.text, obFont);
        }


        if(currentMode != MODE_WORD)
        {
            feedbackPhoneme = (OBPhoneme)curWord.get("feedback");
            feedbackLabel =  labelForText(feedbackPhoneme.text, obFont);

            if(wordFeedback)
            {
                int index = feedbackPhoneme.text.indexOf(targetPhoneme.text);
                int len = targetPhoneme.text.length();
                RectF bb = OBUtils.getBoundsForSelectionInLabel(index,index+len,feedbackLabel);
                float left = bb.left;

                feedbackLabel.setHighRange(index,index+len,Color.BLUE);
                targetLabel.setProperty("dest_left",left);
            }
        }

        OBWord imagePhoneme = (OBWord)curWord.get("image");
        screenImage = loadImageWithName(imagePhoneme.ImageFileName(), new PointF(0,0), new RectF(bounds()));
        OBControl bg = objectDict.get("bg");
        screenImage.setScale(applyGraphicScale(1));
        if(screenImage.width() > bg.width())
        {
            screenImage.setScale(screenImage.scale()*(bg.width()-10.0f)/screenImage.width());
        }
        screenImage.setZPosition(5);
        screenImage.setPosition(bg.position());

        prepareForRecordingEvent(targetPhoneme.audio());
    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.3f);
        wordShow(targetLabel);
        waitForSecs(0.3f);
        demoSceneStart(targetLabel);
        waitForSecs(0.3f);
        if(getAudioForScene(currentEvent() ,"PROMPT2") != null && displayWordSameAsPrevious(eventIndex))
        {
            playStartAudio("PROMPT2",true);

        }
        else
        {
            playStartAudio("PROMPT",true);

        }
        waitForSecs(0.3f);
        startRecordingEvent(targetLabel);
    }

    public void fin()
    {
        List<String> audio = getEventAudio("finale","DEMO");
        try
        {
            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(audio,300),true);
            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch(Exception exception)
        {

        }
    }

    public void playTargetAudio() throws Exception
    {
        targetPhoneme.playAudio(this,true);
    }

    public void animateShutter(boolean open) throws Exception
    {
        OBControl shutter = objectDict.get("shutter");
        playSfxAudio(open?"shutteropen":"shutterclosed",false);
        if(!open)
            shutter.show();
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.propertyAnim("width",open?1:(float)shutter.settings.get("start_width"),shutter)),0.3f,true,open?OBAnim.ANIM_EASE_IN:OBAnim.ANIM_EASE_OUT,this);
        if(open)
            shutter.hide();
        waitSFX();
    }

    public boolean displayWordSameAsPrevious(int index)
    {
        if(index == 0 || eventsData.size() <index)
            return false;
        Map<String,Object> cur = eventsData.get(index);
        Map<String,Object> prev = eventsData.get(index-1);
        if(cur.get("target") == null || prev.get("target") == null)
            return false;
        return cur.get("target") == prev.get("target");
    }

    public void recordingEventFinished() throws Exception
    {
        if(currentMode != MODE_WORD)
        {
            mainLabelAudioAndHighlight();
            waitForSecs(0.7f);
            List<String> audio = getAudioForScene(currentEvent() ,"FINAL2");
            if(audio != null)
            {
                playAudio(audio.get(0));
                waitForAudio();
                waitForSecs(1.5f);
            }
        }
        animateShutter(true);
        waitForSecs(0.3f);
        if(currentMode == MODE_WORD)
        {
            mainLabelAudioAndHighlight();
            targetLabel.setColour(Color.BLACK);
            waitForSecs(1f);
            super.recordingEventFinished();
        }
        else
        {
            if(wordFeedback)
                animatePartWordSlide();
            waitForSecs(0.3f);
            targetPhoneme.playAudio(this,true);
            waitForSecs(0.5f);
            feedbackPhoneme.playAudio(this,true);
            waitForSecs(0.3f);
            if(!wordFeedback)
                targetLabel.setColour(Color.BLACK);
            if(!isLastEvent())
            {
                waitForSecs(1f);
                showNextButtonWithAudio();
            }
            else
            {
                nextScene();
            }
        }
    }

    public void mainLabelAudioAndHighlight() throws Exception
    {
        targetLabel.setColour(Color.BLUE);
        waitForSecs(0.3f);
        targetPhoneme.playAudio(this, true);
        waitForSecs(0.5f);
    }

    public void replayModelAudio() throws Exception
    {
        boolean colourLabel =(currentMode == MODE_WORD) || !wordFeedback;
        long  token = takeSequenceLockInterrupt(true);
        try
        {
            if(token == sequenceToken)
            {
                if(colourLabel)
                    targetLabel.setColour(Color.BLUE);
                targetPhoneme.playAudio(this,true);
                checkSequenceToken(token);
                waitForSecs(0.3f);
                checkSequenceToken(token);
                if(wordFeedback)
                {
                    feedbackPhoneme.playAudio(this, true);
                    checkSequenceToken(token);
                    waitForSecs(0.3f);
                    checkSequenceToken(token);
                }
            }
        }
        catch(Exception exception) {
        }
        if(colourLabel)
            targetLabel.setColour(Color.BLACK);
        sequenceLock.unlock();
    }

    public void wordShow(OBLabel curLabel) throws Exception
    {
        if (curLabel.hidden)
        {
            playSfxAudio("texton", false);
            curLabel.show();
            waitSFX();
        }
    }

    public void nextButtonPressed() throws Exception
    {
        if(!isLastEvent())
        {
            lockScreen();
            if(!displayWordSameAsPrevious(eventIndex+1) || wordFeedback)
            {
                detachControl(targetLabel);
                targetLabel = null;
            }
            if(feedbackLabel != null)
            {
                detachControl(feedbackLabel);
                feedbackLabel = null;
            }
            nextButton.hide();

            unlockScreen();
            waitForSecs(0.5f);
            animateShutter(false);
            waitForSecs(0.5f);
        }
    }

    public void animatePartWordSlide() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        if(feedbackLabel.scale() != targetLabel.scale())
        {
            anims.add(OBAnim.scaleAnim(feedbackLabel.scale(),targetLabel));
        }
        anims.add(OBAnim.propertyAnim("left",(float)targetLabel.propertyValue("dest_left") ,targetLabel));
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        lockScreen();
        feedbackLabel.show();
        targetLabel.hide();
        playSfxAudio("texton",false);
        unlockScreen();
        waitSFX();
    }

    public List<String> getSceneAudio(String audio)
    {
        return getEventAudio(currentEvent(),audio);
    }

    public List<String> getEventAudio(String event,String category)
    {
        if(currentMode == MODE_WORD)
        {
            return getAudioForScene(event,category);
        }
        else
        {
            String prefix =(currentMode == MODE_LETTER) ? "ALT" : "ALT2";

            List<String> altAudio = getAudioForScene(event,String.format("%s.%s", prefix, category));
            if(((currentMode == MODE_LETTER && targetPhoneme.text.length() > 1) ||
                    currentMode == MODE_SYLLABLE) && altAudio != null)
            {
                return altAudio;
            }
            else
            {
                return getAudioForScene(event,category);
            }
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

    public void playStartAudio(String name,boolean wait) throws Exception
    {
        setReplayAudio(OBUtils.insertAudioInterval(getSceneAudio(String.format("%s.REPEAT",name)) , 300));
        playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio(name) , 300),wait);
    }

    public String audioNameForCategory(String category)
    {
        if(currentMode != MODE_WORD)
            category = String.format((currentMode == MODE_LETTER) ? "ALT.%s" : "ALT2.%s", category);
        return category;
    }

    public void demoEvent1() throws Exception
    {
        demoEvent1AudioCategory(audioNameForCategory("DEMO"));
    }

    public void demoEvent2() throws Exception
    {
        if(getAudioForScene(currentEvent() ,"DEMO2") != null && displayWordSameAsPrevious(eventIndex))
        {
            demoPointForLabel(targetLabel,"DEMO2");
        }
        else
        {
            demoPointForLabel(targetLabel,audioNameForCategory("DEMO"));
        }
    }

    @Override
    public void onResume()
    {
        audioRecorder.onResume();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            audioRecorder.onPause();

        } catch(Exception e)
        {
        }
    }

}
