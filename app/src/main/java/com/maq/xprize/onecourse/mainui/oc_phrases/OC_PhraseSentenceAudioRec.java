package com.maq.xprize.onecourse.mainui.oc_phrases;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.OBMainViewController;
import com.maq.xprize.onecourse.mainui.oc_audiorec.OC_AudioRecSection;
import com.maq.xprize.onecourse.mainui.oc_audiorec.OC_AudioRecordingButton;
import com.maq.xprize.onecourse.utils.OBAudioManager;
import com.maq.xprize.onecourse.utils.OBAudioRecorder;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBReadingPara;
import com.maq.xprize.onecourse.utils.OBReadingWord;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 11/06/2018.
 */

public class OC_PhraseSentenceAudioRec extends OC_PhraseSentence
{
    boolean sentenceMode;
    int currentRecordingAttempt;
    OBAudioRecorder audioRecorder;
    float recordDuration;
    List<String> currentAudios;
    OC_AudioRecordingButton recordingButton;
    OBControl nextButton;

    public long statusSetWaitRecordStart()
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public long statusSetWaitRecordStop()
    {
        return setStatus(STATUS_RECORDING);
    }

    public long statusSetWaitNextButton()
    {
        return setStatus(STATUS_AWAITING_ARROW_CLICK);
    }

    public boolean statusWaitRecordStart()
    {
        return status() == STATUS_AWAITING_CLICK;
    }

    public boolean statusWaitRecordStop()
    {
        return status() == STATUS_RECORDING;
    }

    public boolean statusWaitNextButton()
    {
        return status() == STATUS_AWAITING_ARROW_CLICK;
    }

    @Override
    public float getFontSize()
    {
        if(sentenceMode)
            return 0.8f * super.getFontSize();
        return super.getFontSize();
    }

    @Override
    public void cleanUp()
    {
        OBUtils.cleanUpTempFiles(this);
        super.cleanUp();
    }

    @Override
    public void miscSetUp()
    {
        setStatus(STATUS_BUSY);
        loadEvent("mastera");
        nextButton = OC_AudioRecSection.loadNextButton(this);
        audioRecorder = new OBAudioRecorder(OBUtils.getFilePathForTempFile(this), this);
        audioRecorder.silenceTimming = 5.0f;
        recordingButton =  new OC_AudioRecordingButton((OBGroup)objectDict.get("recording_button"),this);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        Map<String,Map<String,Object>> components = new ArrayMap<>();
        if(parameters.get("mode").equals("phrase"))
        {
            sentenceMode = false;
            components.putAll(loadComponent("phrase",getLocalPath("phrases.xml")));
        }
        else
        {
            sentenceMode = true;
            components.putAll(loadComponent("sentence",getLocalPath("sentences.xml")));
            mergeAudioScenesForPrefix("ALT");
        }
        componentDict = components;
        textBox = new OBGroup(Arrays.asList(objectDict.get("textbox")));
        textBox.setShouldTexturise(false);
        attachControl(textBox);
        textBoxOriginalPos = OBMisc.copyPoint(textBox.position());
        currNo = -1;
        componentList = Arrays.asList(parameters.get("targets").split(","));
        List<String> eventsArray = new ArrayList<>();
        int index = 1;
        for(String component : componentList)
        {
            if(componentDict.containsKey(component))
            {
                String indexString = String.format("%d", index);
                if(audioScenes.containsKey(indexString))
                {
                    eventsArray.add(indexString);
                }
                else
                {
                    eventsArray.add("default");
                }
                index++;
            }
        }
        events = eventsArray;
        highlightColour = Color.BLUE;
        setUpWordStuff();
    }

    public void cleanUpScene()
    {
        if (paragraphs != null)
        {
            for(OBReadingPara para : paragraphs)
                for(OBReadingWord w : para.words)
                    if(w.label != null)
                        w.label.parent.removeMember(w.label);
        }
        textBox.setPosition(textBoxOriginalPos);
    }

    @Override
    public void setUpScene()
    {
        cleanUpScene();
        currComponentKey = componentList.get(currNo);
        Map<String, Object> currPhraseDict = componentDict.get(currComponentKey);
        String imageName = (String)currPhraseDict.get("imagename");
        if(mainPic != null)
            detachControl(mainPic);
        mainPic = loadImageWithName(imageName, new PointF(0.5f, 0.5f),this.boundsf());
        objectDict.put("mainpic",mainPic);
        mainPic.setZPosition(60);
        scalePicToBox();
        String ptext = (String)currPhraseDict.get("contents");
        OBReadingPara par = new OBReadingPara(ptext,1);
        paragraphs = Arrays.asList(par);
        List<OBReadingWord> wds = new ArrayList<>();
        for(OBReadingPara p : paragraphs)
        {
            for(OBReadingWord rw : p.words)
            {
                if((rw.flags & OBReadingWord.WORD_SPEAKABLE) != 0)
                    wds.add(rw);
            }
        }
        words = wds;
        layOutText();
        calcWordFrames();
        adjustTextPosition();
        int i = 1;
        recordDuration = 0;
        currentAudios = new ArrayList<>();
        for(OBReadingPara para : paragraphs)
        {
            loadTimingsPara(para, getLocalPath(String.format("%s_%d.etpa",currComponentKey,i)),false);
            loadTimingsPara(para, getLocalPath(String.format("%s_%d.etpa",SlowVersion(currComponentKey,true) ,i)),true);
            String audioName = String.format("%s_%d",currComponentKey,i);
            currentAudios.add(audioName);
            OBAudioManager.audioManager.prepareForChannel(audioName,"special");
            recordDuration += OBAudioManager.audioManager.durationForChannel("special");
            i++;
        }
        mainPic.hide();
        textBox.hide();
        wordIdx = 0;
    }

    @Override
    public void adjustTextPosition()
    {
        List wlines = wordExtents();
        if(wlines.size() <= 1)
            return;
        List<OBReadingWord> arr = (List) wlines.get(0);
        OBReadingWord w = arr.get(0);
        float centy = (w.frame).centerY();
        arr = (List) wlines.get(wlines.size() - 1);
        w = arr.get(0);
        float y2 = (w.frame).centerY();
        float midy = (centy + y2) / 2;
        float diff = centy - midy;
        if(diff != 0)
        {
            PointF pos = new PointF();
            pos.set(textBox.position());
            pos.y += diff;
            textBox.setPosition(pos);
            calcWordFrames();
        }
    }

    @Override
    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON|OBMainViewController.SHOW_TOP_RIGHT_BUTTON|0|0;
    }

    @Override
    public void setSceneXX(String scene)
    {
        currPara = 0;
        currNo++;
        setUpScene();
    }

    public void clearOff() throws Exception
    {
        if(!mainPic.hidden())
        {
            playSfxAudio("alloff",false);
            lockScreen();
            nextButton.hide();
            nextButton.lowlight();
            nextButton.setOpacity(1);
            mainPic.hide();
            textBox.hide();
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
        }
    }

    @Override
    public long switchStatus(String scene)
    {
        return 0;
    }

    public void doIntro() throws Exception
    {
        if(eventIndex == 0)
        {
            demoIntro1();
        }
        showPic();
        waitForSecs(0.3f);
        if(eventIndex == 0)
        {
            recordingButton.control.show();
            playSfxAudio("buttonon",true);
        }
        showWords();
        if(eventIndex == 0)
        {
            demoIntro2();
            if(OBUtils.getBooleanValue(parameters.get("example")))
            {
                demoExample();
                waitForSecs(0.3f);

            }
            demoEvent1();
        }
        else
        {
            if(eventIndex == 1)
                demoEvent2();
        }
    }

    @Override
    public void doMainXX() throws Exception
    {
        setStatus(STATUS_BUSY);
        currentRecordingAttempt = 1;
        doIntro();
        List<String> repeat = getAudioForScene(currentEvent() ,"PROMPT.REPEAT");
        if(repeat != null)
            setReplayAudio(OBUtils.insertAudioInterval(repeat,300));
        playAudioQueuedScene("PROMPT",0.3f,true);
        waitForSecs(0.3f);
        recordingButtonReady();
        recordingButton.flash(statusSetWaitRecordStart(),8);
    }

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if(statusWaitRecordStart())
        {
            if(finger(0,1,Arrays.asList((OBControl)recordingButton.control),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        playAudio(null);
                        recordingButton.highlight();
                        performRecordingEvent();
                    }
                });
            }
        }
        else if(statusWaitRecordStop())
        {
            if(finger(0,1,Arrays.asList((OBControl)recordingButton.control),pt) != null)
            {
                setStatus(STATUS_BUSY);
                recordingButton.highlight();
                stopRecording();
            }
        }
        else if(statusWaitNextButton())
        {
            if(finger(0,1,Arrays.asList(nextButton),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        nextButton.highlight();
                        takeSequenceLockInterrupt(true);
                        sequenceLock.unlock();
                        moveOnToNextWord();
                    }
                });
            }
        }
    }

    @Override
    public void touchMovedToPoint(PointF pt,View v)
    {

    }

    @Override
    public void touchUpAtPoint(PointF pt,View v)
    {

    }

    public void recordingButtonReady() throws Exception
    {
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_NORMAL);
        playSfxAudio("buttonready",true);
    }

    @Override
    public void replayAudio()
    {
        if(statusWaitNextButton())
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    currPara = 0;
                    playAudio(null);
                    readPage();
                }
            });
        }
        else if(statusWaitRecordStart())
        {
            super.replayAudio();
        }
    }

    public void performRecordingEvent() throws Exception
    {
        animateWordRecordStart();
        waitForSecs(0.3f);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_ACTIVE);
        recordingButton.startRecordingAnimation();
        startRecording();
        statusSetWaitRecordStop();
        audioRecorder.waitForRecord();
        animateWordRecordStop();
        if(currentRecordingAttempt < 3 && !audioRecorder.audioRecorded())
        {
            if(currentRecordingAttempt == 1 || currentAudio("INCORRECT2") == null)
            {
                playAudioQueued(OBUtils.insertAudioInterval(currentAudio("INCORRECT") , 300),true);
            }
            else
            {
                playAudioQueued(OBUtils.insertAudioInterval(currentAudio("INCORRECT2") , 300),true);
            }
            OBUtils.cleanUpTempFiles(this);
            currentRecordingAttempt++;
            recordingButtonReady();
            statusSetWaitRecordStart();
        }
        else
        {
            if(audioRecorder.audioRecorded())
            {
                playAudioQueuedScene("FINAL",0.3f,true);
                waitAudio();
                waitForSecs(0.5f);
                audioRecorder.playRecording();
            }
            waitForSecs(0.7f);
            currPara = 0;
            readPage();
            waitForSecs(1f);
            statusSetWaitNextButton();
            if(eventIndex < events.size()-1)
            {
                List<String> audio = currentAudio("FINAL2");
                if(audio != null)
                {
                    playAudio(audio.get(0));
                    waitForAudio();
                    waitForSecs(1.5f);
                }
                nextButton.show();
                playSfxAudio("arrowon",true);
                playAudioQueuedScene("ARROW",0.3f,false);
                OC_AudioRecSection.flashNextButton(nextButton,statusSetWaitNextButton(),this);
            }
            else
            {
                moveOnToNextWord();
            }
        }
    }

    public void moveOnToNextWord() throws Exception
    {
        setStatus(STATUS_BUSY);
        if(currentEvent() != events.get(events.size()-1))
        {
            clearOff();
            waitForSecs(0.4f);
        }
        nextScene();
    }

    public void animateWordRecordStart() throws Exception
    {
        lockScreen();
        highlightCurrentParagraph(Color.RED);
        playSfxAudio("ping",false);
        unlockScreen();
        waitSFX();
    }

    public void animateWordRecordStop() throws Exception
    {
        recordingButton.stopRecordingAnimation();
        lockScreen();
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        highlightCurrentParagraph(Color.BLACK);
        playSfxAudio("cancel",false);
        unlockScreen();
        waitSFX();
    }

    public void startRecording()
    {
        audioRecorder.startRecording(OC_AudioRecSection.getExpectedAudioLength(recordDuration) *1.3);
    }

    public void stopRecording()
    {
        audioRecorder.stopRecording();
    }

    public void highlightCurrentParagraph(int colour)
    {
        OBReadingPara para = paragraphs.get(0);
        for(OBReadingWord w : para.words)
        {
            if(w.label != null)
                w.label.setColour(colour);
        }
    }

    @Override
    public void nextPage()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if (statusWaitNextButton())
                {
                    setStatus(STATUS_BUSY);
                    takeSequenceLockInterrupt(true);
                    sequenceLock.unlock();
                    moveOnToNextWord();
                }
            }
        });
    }

    public RectF getTextRect()
    {
        RectF rect = new RectF();
        for(int i=0; i<paragraphs.size(); i++)
        {
            OBReadingPara readingPara = paragraphs.get(i);
            for(int j=0; j<readingPara.words.size(); j++)
            {
                OBReadingWord readingWord = readingPara.words.get(j);
                if(readingWord.frame != null)
                {
                    RectF wordFrame = readingWord.frame;
                    if (i == 0 && j == 0)
                        rect = new RectF(wordFrame);
                    else
                        rect.union(wordFrame);
                }
            }
        }
        return rect;
    }

    public void demoIntro1() throws Exception
    {
        if(OBUtils.getBooleanValue(parameters.get("presenter")))
        {
            OBMisc.standardDemoIntro1(this);
        }
    }

    public void demoIntro2() throws Exception
    {
        if(!OBUtils.getBooleanValue(parameters.get("presenter")))
        {
            OBMisc.standardDemoIntro2(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),this);
        }
    }

    public void demoExample() throws Exception
    {
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-20,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(0));
        waitAudio();
        waitForSecs(0.3f);
        recordingButtonReady();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(1));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        animateWordRecordStart();
        waitForSecs(0.3f);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_ACTIVE);
        recordingButton.startRecordingAnimation();
        waitForSecs(0.5f);
        playAudioQueued(OBUtils.insertAudioInterval(currentAudios, 300),true);
        waitAudio();
        waitForSecs(0.5f);
        playAudio(getAudioForScene("example","DEMO") .get(2));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        animateWordRecordStop();
        waitForSecs(0.5f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(3));
        waitAudio();
        waitForSecs(0.3f);
    }

    public void demoEvent1() throws Exception
    {
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.8f, 1.05f) , getTextRect()),-30,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
    }

    public void demoEvent2() throws Exception
    {
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-30,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
    }
}
