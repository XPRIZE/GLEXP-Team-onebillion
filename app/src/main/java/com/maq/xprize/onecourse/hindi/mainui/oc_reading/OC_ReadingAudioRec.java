package com.maq.xprize.onecourse.hindi.mainui.oc_reading;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPresenter;
import com.maq.xprize.onecourse.hindi.mainui.oc_audiorec.OC_AudioRecSection;
import com.maq.xprize.onecourse.hindi.mainui.oc_audiorec.OC_AudioRecordingButton;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBAudioRecorder;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBReadingPara;
import com.maq.xprize.onecourse.hindi.utils.OBReadingWord;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 11/06/2018.
 */

public class OC_ReadingAudioRec extends OC_Reading
{
    OBAudioRecorder audioRecorder;
    OBLabel wordLabel;
    float recordDuration;
    int currentRecordingAttempt;
    OC_AudioRecordingButton recordingButton;

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
    public void prepare()
    {
        setStatus(STATUS_DOING_DEMO);
        super.prepare();
        objectDict.get("wordback").hide();
        highlightColour = Color.BLUE;
        audioRecorder = new OBAudioRecorder(OBUtils.getFilePathForTempFile(this), this);
        audioRecorder.silenceTimming = 5.0f;
        OBGroup buttonControl = loadVectorWithName("recording_button",new PointF(0.5f,0.93f), this.boundsf());
        buttonControl.setScale(applyGraphicScale(0.632f));
        recordingButton = new OC_AudioRecordingButton(buttonControl,this);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        if(pageNo == 0)
            recordingButton.control.hide();
        else
            recordingButton.control.show();
    }

    @Override
    public void cleanUp()
    {
        OBUtils.cleanUpTempFiles(this);
        super.cleanUp();
    }

    @Override
    public boolean showRAButton()
    {
        return true;
    }

    @Override
    public boolean showNextButton()
    {
        return false;
    }

    @Override
    public boolean showPrevButton()
    {
        return false;
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demoStart();
                startScene();
            }
        });
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
                    readTargetParagraph();
                }
            });
        }
        else if(statusWaitRecordStart())
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    _replayAudio();
                }
            });
        }
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
                    public void run() throws Exception {
                        playAudio(null);
                        recordingButton.highlight();
                        performRecordingEvent();
                    }
                });
            }
        }
        else if(statusWaitRecordStop())
        {
            if(finger(0,1, Arrays.asList((OBControl)recordingButton.control),pt) != null)
            {
                setStatus(STATUS_BUSY);
                recordingButton.highlight();
                stopRecording();
            }
        }
    }

    @Override
    public void setUpScene()
    {
        OBUtils.cleanUpTempFiles(this);
        super.setUpScene();
        recordDuration = 0;
        for(String audio : currentAudio()) {
            OBAudioManager.audioManager.prepareForChannel(audio, "special");
            recordDuration += (float) OBAudioManager.audioManager.durationForChannel("special");
        }
        showNextArrowAndRA(false);
    }

    @Override
    public void touchMovedToPoint(PointF pt,View v)
    {

    }

    @Override
    public void touchUpAtPoint(PointF pt,View v)
    {

    }

    @Override
    public void nextPage()
    {
        if(statusWaitNextButton())
            super.nextPage();
    }

    public void startScene() throws Exception
    {
        waitForSecs(0.5f);
        List<String> prompt = getPageAudio("PROMPT");
        List<String> repeat = getPageAudio("PROMPT.REPEAT");
        if(repeat != null)
            setReplayAudio(OBUtils.insertAudioInterval(repeat,300));
        if(prompt != null)
            playAudioQueued(OBUtils.insertAudioInterval(prompt, 300),true);
        waitForSecs(0.3f);
        recordingButtonReady();
        recordingButton.flash(statusSetWaitRecordStart(),8);
    }

    public List<String> currentAudio()
    {
        List<String> audios = new ArrayList<>();
        for(int i=0; i<paragraphs.size(); i++)
            audios.add(String.format("p%d_%d",pageNo,i+1));
        return audios;
    }

    public void highlightCurrentParagraph(int colour)
    {
        for(OBReadingPara para : paragraphs) {
            for (OBReadingWord w : para.words) {
                if (w.label != null)
                    w.label.setColour(colour);
            }
        }
    }

    public boolean readTargetParagraph()
    {
        long token = -1;
        try
        {
            reading = true;
            token = takeSequenceLockInterrupt(true);
            for(int i=0; i<paragraphs.size(); i++) {
                checkSequenceToken(token);
                readParagraph(i, token, true);
                for (int j = 0;j < 6;j++)
                {
                    checkSequenceToken(token);
                    waitForSecs(0.1);
                }
            }
        }
        catch(Exception exception)
        {
        }
        reading = false;
        sequenceLock.unlock();
        return(sequenceToken == token);
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
        if(currentRecordingAttempt <3 && !audioRecorder.audioRecorded())
        {
            if(currentRecordingAttempt == 1 || getPageAudio("INCORRECT2") == null)
            {
                playAudioQueued(OBUtils.insertAudioInterval(getPageAudio("INCORRECT") , 300),true);
            }
            else
            {
                playAudioQueued(OBUtils.insertAudioInterval(getPageAudio("INCORRECT2") , 300) ,true);
            }
            OBUtils.cleanUpTempFiles(this);
            currentRecordingAttempt++;
            recordingButtonReady();
            statusSetWaitRecordStart();
        }
        else
        {
            animateWordRecordStop();
            playAudioQueued(OBUtils.insertAudioInterval(getPageAudio("FINAL"), 300),true);
            waitForSecs(0.5f);
            audioRecorder.playRecording();
            waitForSecs(0.7f);
            readTargetParagraph();
            waitForSecs(1f);
            playAudioQueued(OBUtils.insertAudioInterval(getPageAudio("FINAL2"),300),true);
            waitForSecs(1.5f);

            showNextArrowAndRA(true);
            if(pageNo == 0)
                demoArrow();
            statusSetWaitNextButton();
            flashContinuouslyAfter(6.5f);

        }
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
        playSfxAudio("click",false);
        unlockScreen();
    }

    public void startRecording()
    {
        audioRecorder.startRecording(OC_AudioRecSection.getExpectedAudioLength(recordDuration) *1.3);
    }

    public void stopRecording()
    {
        audioRecorder.stopRecording();
    }

    public void recordingButtonReady() throws Exception
    {
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_NORMAL);
        playSfxAudio("buttonready",true);
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
            lockScreen();
            loadEvent("anna");
            OBGroup presenterGroup =(OBGroup ) objectDict.get("presenter");
            OBPresenter presenter = OBPresenter.characterWithGroup(presenterGroup);
            unlockScreen();
            PointF startLoc = presenter.control.position();
            presenter.control.setZPosition(200);
            presenter.control.setPosition(startLoc);
            presenter.control.setRight(0);
            PointF loc = OBMisc.copyPoint(presenter.control.position());
            presenter.control.show();
            loc.x = 0.5f*this.bounds().width();
            presenter.walk(loc);
            presenter.faceFront();
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)getAudioForScene("intro1","DEMO"), 0.3f,this);
            waitForSecs(0.3f);
            loc.x = 00.87f*this.bounds().width();
            presenter.walk(loc);
            presenter.faceFront();
            presenter.speak((List<Object>)(Object)getAudioForScene("intro1","DEMO2"), 0.3f, this);
            waitForSecs(0.3f);
            loc.x = 1.2f*this.bounds().width();
            presenter.walk(loc);
            presenter.control.hide();
            waitForSecs(0.4f);
        }
    }

    public void demoIntro2() throws Exception
    {
        if(!OBUtils.getBooleanValue(parameters.get("presenter")))
        {
            OBMisc.standardDemoIntro2(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),this);
        }
    }

    public void demoStart() throws Exception
    {
        if(pageNo == 0 )
        {
            demoIntro1();
            waitForSecs(0.3f);
            recordingButton.control.show();
            playSfxAudio("buttonon",true);
            waitForSecs(0.3f);
            demoIntro2();
            if(OBUtils.getBooleanValue(parameters.get("example")))
            {
                demoExample();
                waitForSecs(0.3f);
            }
        }

        performSel("demoPage",String.format("%d",pageNo));
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
        playAudioQueued(OBUtils.insertAudioInterval(currentAudio(), 300), true);
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

    public void demoPage0() throws Exception
    {
        List<String> demoAudio = getPageAudio("DEMO");
        if(demoAudio != null)
        {
            waitForSecs(0.3f);
            if(thePointer == null || thePointer.hidden)
                loadPointer(POINTER_LEFT);
            movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
            playAudio(demoAudio.get(0));
            waitAudio();
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(new PointF(0.75f, 1.1f) , getTextRect()),-10,0.5f,true);
            playAudio(demoAudio.get(1));
            waitAudio();
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
            playAudio(demoAudio.get(2));
            waitAudio();
            waitForSecs(0.5f);
        }
        thePointer.hide();
    }

    public void demoPage1() throws Exception
    {
        List<String> demoAudio = getPageAudio("DEMO");
        if(demoAudio != null)
        {
            waitForSecs(0.3f);
            if(thePointer == null || thePointer.hidden)
                loadPointer(POINTER_LEFT);
            movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-30,0.5f,true);
            playAudio(demoAudio.get(0));
            waitAudio();
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
            playAudio(demoAudio.get(1));
            waitAudio();
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(new PointF(0.75f, 1.1f) , getTextRect()),-10,0.5f,true);
            playAudio(demoAudio.get(2));
            waitAudio();
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
            playAudio(demoAudio.get(3));
            waitAudio();
            waitForSecs(0.5f);
        }
        thePointer.hide();
    }

    public void demoArrow() throws Exception
    {
        PointF destPoint = OB_Maths.locationForRect(new PointF(-0.1f, 0.3f) , MainViewController() .bottomRightButton .frame());
        loadPointer(POINTER_RIGHT);
        movePointerToPoint(destPoint,0.5f,true);
        playAudioQueued(OBUtils.insertAudioInterval(getPageAudio("ARROW"),300),true);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public List<String> getPageAudio(String audio)
    {
        String currentPage = String.format("%d_%d",pageNo, 1);
        if(audioScenes.containsKey(currentPage))
        {
            return getAudioForScene(currentPage,audio);
        }
        else
        {
            currentPage = String.format("%d_default",pageNo);
            if(audioScenes.get(currentPage) != null)
            {
                return getAudioForScene(currentPage,audio);

            }
            else
            {
                return getAudioForScene("default",audio);
            }
        }
    }

}
