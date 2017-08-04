package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBCameraManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBVideoRecorder;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 11/07/16.
 */
public class OC_WordVideoRec extends OC_SectionController
{
    static final int MODE_WORD = 1;

    OBLabel wordLabel;
    double expectedAudioLength;
    int  currentWord, currentMode;
    OBPresenter presenter;
    List<Map<String,Object>> eventsData;
    OBVideoRecorder videoRecorder;
    OBVideoPlayer videoPlayer;
    OBCameraManager cameraManager;
    OBGroup nextButton;
    OBPhoneme currentPhoneme;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");

        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.faceFront();

        Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);

        eventsData = new ArrayList<>();

        for(String wordid : parameters.get("words").split(","))
        {
            Map<String,Object> dict = new ArrayMap<>();


            int mode = -1;

            mode = MODE_WORD;
            OBWord readingWord = (OBWord)componentDict.get(wordid);
            dict.put("word",readingWord);

            dict.put("mode",mode);
            eventsData.add(dict);
        }

        currentWord = 1;

        OBControl videoBox = objectDict.get("videobox");
        if(parameters.get("demo").equalsIgnoreCase("true") && parameters.get("presenter").equalsIgnoreCase("true"))
        {
            showControls("presenter.*");
            OBControl mask = new OBControl();

            mask.setFrame(new RectF(videoBox.frame()));
            mask.setBackgroundColor(Color.BLACK);
            mask.setOpacity(1);
            mask.texturise(false,this);

            presenter.control.setScreenMaskControl(mask);
        }

        videoRecorder = new OBVideoRecorder(OBUtils.getFilePathForTempFile(this),this);
        videoPlayer = new OBVideoPlayer(videoBox.frame(),this);
        videoPlayer.setZPosition(videoBox.zPosition());
        videoBox.hide();

        cameraManager = new OBCameraManager(this);
        attachControl(videoPlayer);

        nextButton = loadVectorWithName("arrow_next", new PointF(0, 0), new RectF(bounds()));
        nextButton.setScale((bounds().height() * 0.1f) / nextButton.height());
        nextButton.setBottom(bounds().height() - applyGraphicScale(10));
        nextButton.setRight(bounds().width() - applyGraphicScale(10));
        attachControl(nextButton);
        nextButton.hide();


        setWordScene(currentWord);
    }

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON|0|0|0;
    }

    @Override
    public void cleanUp()
    {
        try
        {
            cameraManager.onPause();
            videoRecorder.onPause();
            videoPlayer.cleanUp(MainActivity.mainActivity.renderer);
        } catch(Exception e)
        {
            e.printStackTrace();
        }
        OBUtils.cleanUpTempFiles(this);

        super.cleanUp();
    }


    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                doMainXX();
            }
        });

    }


    public void setWordScene(int wordNum)
    {

        Map<String,Object> curEvent = eventsData.get(wordNum-1);
        currentPhoneme =  (OBPhoneme)curEvent.get("word");
        currentMode = (int)curEvent.get("mode") ;
        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(currentMode == MODE_WORD ? 120 : 100);

        wordLabel = new OBLabel(currentPhoneme.text,font,fontSize);
        wordLabel.setColour(Color.BLACK);
        OBControl textBox = objectDict.get(currentMode==MODE_WORD ? "textbox" : "textboxcaption");
        textBox.setZPosition(0.1f);
        wordLabel.setZPosition(1);
        wordLabel.setPosition(textBox.position());
       // float singleLineHeight = wordLabel.height();
        if(wordLabel.width()>textBox.width())
            wordLabel.setScale(1.0f - ((wordLabel.width()-textBox.width())*1.0f/wordLabel.width()));

        wordLabel.hide();
        attachControl(wordLabel);
        OBAudioManager.audioManager.prepareForChannel(currentPhoneme.audio(), "special");

        expectedAudioLength = OBAudioManager.audioManager.durationForChannel("special") * 1.5 + 1;

        if(currentMode != MODE_WORD)
            expectedAudioLength *= 1.2;
    }

    public void doMainXX() throws Exception
    {
        if(currentWord == 1)
        {
            demoPresenter();
        }
        else
        {
            showVideoPreview();
            waitForSecs(0.5f);
            wordShow();
        }
        waitForSecs(0.3f);
        playSceneAudio("PROMPT",true);
        waitForSecs(0.3f);
        performRecordingEvent(0);
    }



    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK &&
                finger(0,2,(List<OBControl>)(Object) Collections.singletonList(nextButton),pt) != null)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    nextButtonClicked();
                }
            });
        }
    }

    public void showVideoPreview()
    {
        if(!_aborting)
            cameraManager.startPreviewForRecording(videoPlayer,videoRecorder);
    }


    public void startVideoRecorder(double audioLength)
    {
        if(!_aborting)
            videoRecorder.startMediaRecorderAndTimer(audioLength);
    }

    public void waitForVideoRecorder() throws Exception
    {
        videoRecorder.waitForRecord();
        videoRecorder.stopRecording();
        cameraManager.stopPreview();
    }

    public void replayRecordedVideo()
    {
        videoRecorder.playVideoRecording(videoPlayer);
        videoPlayer.waitForVideo();
    }

    public void performRecordingEvent(int count) throws Exception
    {
        setStatus(STATUS_AWAITING_CLICK);
        wordRecordStart(true);
        startVideoRecorder(expectedAudioLength);
        waitForVideoRecorder();
        checkSuspendLock();
        if(!videoRecorder.audioRecorded() && count < 2)
        {
            showVideoPreview();
            setStatus(STATUS_BUSY);
            wordLabel.setColour(Color.BLACK);

            if(count == 0 || getSceneAudio("REMINDER2") == null)
            {
                playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("REMINDER"),300),true);
            }
            else
            {
                playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("REMINDER2"),300),true);
            }

            count++;
            waitForSecs(0.5f);
            performRecordingEvent(count);
        }
        else
        {
            setStatus(STATUS_BUSY);
            wordRecordStop();
            playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("FINAL"),300),true);
            waitForSecs(0.5f);
            replayRecordedVideo();
            waitForSecs(0.5f);
            doAudioAndHighlight(-1);
            waitForSecs(1.5f);

            if(!isLastWord())
            {
                showLeftButtons();
                playSfxAudio("arrowon",true);
                playSceneAudio("ARROW",false);
                flashNextButton(setStatus(STATUS_AWAITING_CLICK));
            }
            else
            {
                nextWord();
            }
        }
    }

    public void checkSequenceToken(long token) throws Exception
    {
        if(token>0)
            super.checkSequenceToken(token);
    }

    public void doAudioAndHighlight(long token) throws Exception
    {
        if(currentMode == MODE_WORD)
        {
            wordLabel.setColour(Color.BLUE);
            waitForSecs(0.3f);
            checkSequenceToken(token);
            currentPhoneme.playAudio(this,true);
            waitForSecs(0.3f);
            checkSequenceToken(token);
            wordLabel.setColour(Color.BLACK);
        }
        else
        {
           // playAudioAndHighlightWordsToken(token);
        }
    }

    public void replayWordAudio()
    {
        long token = takeSequenceLockInterrupt(true);

        try
        {
            if (token == sequenceToken)
            {
                doAudioAndHighlight(token);
            }
        }
        catch (Exception exception)
        {

        }
        wordLabel.setColour(Color.BLACK);
        sequenceLock.unlock();

    }

    public void replayAudio()
    {
        if(status() != STATUS_BUSY)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    replayWordAudio();
                }
            });
        }
    }


    public void clearScene()
    {
        lockScreen();
        detachControl(wordLabel);
        hideAndResetLeftButtons();
        unlockScreen();
    }

    public void wordShow() throws Exception
    {
        playSfxAudio("texton",false);
        wordLabel.show();
        waitSFX();
    }

    public void wordRecordStart(boolean pulse) throws Exception
    {
        playSfxAudio("ping",false);
        wordLabel.setColour(Color.RED);
        if(pulse)
            animateWordPulse();
        waitSFX();
    }

    public void wordRecordStop() throws Exception
    {
        playSfxAudio("click",false);
        wordLabel.setColour(Color.BLACK);
        waitSFX();
    }

    public void presenterSpeak(List<String> audioFiles)
    {
        presenter.speak((List<Object>)(Object)audioFiles,this);
    }

    public void nextButtonClicked() throws Exception
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            setStatus(STATUS_BUSY);
            takeSequenceLockInterrupt(true);
            sequenceLock.unlock();
            playAudio(null);
            if(!isLastWord())
                clearScene();

            nextWord();
        }

    }

    public void animateWordPulse()
    {
        float startScale = wordLabel.scale();
        OBAnimationGroup.chainAnimations(Arrays.asList(Collections.singletonList(OBAnim.scaleAnim(startScale*1.2f,wordLabel)),
                Collections.singletonList(OBAnim.scaleAnim(startScale,wordLabel))),
                Arrays.asList(0.2f,0.2f), false,
                Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT),1,this);
    }


    public void showLeftButtons()
    {
        lockScreen();
        nextButton.show();
        nextButton.setOpacity(1);
        MainViewController().topRightButton.show();
        MainViewController().topRightButton.setOpacity(1);


        unlockScreen();
    }

    public void hideAndResetLeftButtons()
    {
        lockScreen();
        nextButton.hide();
        nextButton.lowlight();
        nextButton.setOpacity(1);
        MainViewController().topRightButton.hide();
        MainViewController().topRightButton.setOpacity(0);
        unlockScreen();
    }


    public List<String> getSceneAudio(String audio)
    {
        String currentScene = String.format("%d%s",currentWord, currentMode == MODE_WORD ? "w" : "c");
        if(audioScenes.get(currentScene) != null)
        {
            return getAudioForScene(currentScene,audio);
        }
        else
        {
            return getAudioForScene( String.format("default%s", currentMode == MODE_WORD ? "w" : "c"),audio);
        }
    }

    public void playSceneAudio(String name, boolean wait) throws Exception
    {
       // setReplayAudio(OBUtils.insertAudioInterval(getSceneAudio(String.format("%s.REPEAT",name)),300));
        playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio(name),300),wait);
    }

    public void nextWord() throws Exception
    {
        currentWord++;
        if(currentWord > eventsData.size())
        {
            fin();
        }
        else
        {
            lockScreen();
            setWordScene(currentWord);
            unlockScreen();
            doMainXX();
        }
    }


    public boolean isLastWord()
    {
        return currentWord == eventsData.size();
    }



    @Override
    public void onResume()
    {
        videoRecorder.onResume();
        videoPlayer.onResume();
        cameraManager.onResume();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            cameraManager.onPause();
            videoRecorder.onPause();
            videoPlayer.onPause();


        } catch(Exception e)
        {

        }

    }


    public void demoPresenter() throws Exception
    {
        waitForSecs(0.2f);

        if(parameters.get("presenter").equalsIgnoreCase("true"))
        {
            presenterSpeak(getSceneAudio("DEMO"));
            waitForSecs(0.5f);
            PointF loc = OC_Generic.copyPoint(presenter.control.position());
            loc.x = this.bounds().width();
            presenter.walk(loc);
            waitForSecs(0.5f);
            showVideoPreview();
            lockScreen();
            hideControls("presenter.*");
            unlockScreen();
        }
        else
        {
            showVideoPreview();
            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("DEMO2"),300),true);
            waitForSecs(0.3f);

        }

        if(parameters.get("demo").equalsIgnoreCase("true"))
        {
            List<String> aud =  getSceneAudio("DEMO3");
            loadPointer(POINTER_LEFT);
            movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,objectDict.get("frame").frame()),0.5f,true);
            playAudio(aud.get(0));
            waitAudio();
            waitForSecs(0.8f);
            movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-40,0.5f,true);
            waitForSecs(0.3f);

            wordShow();
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,wordLabel.frame()),-40,0.5f,true);

            playAudio(aud.get(1));
            waitAudio();
            waitForSecs(0.3f);
            thePointer.hide();
            waitForSecs(1f);
        }
        else
        {

            waitForSecs(0.5f);
            wordShow();
            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("DEMO4"),300),true);

        }
    }

    public void flashNextButton(final long time)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                try
                {
                    waitForSecs(3f);
                    if(time == statusTime && !_aborting)
                    {
                        nextButton.setOpacity(1);
                    }
                    waitForSecs(0.5f);
                    for (int i = 0;i < 2;i++)
                    {
                        if(time == statusTime && !_aborting)
                        {
                            nextButton.setOpacity(0.2f);
                        }
                        waitForSecs(0.3f);
                        if(time == statusTime && !_aborting)
                        {
                            nextButton.setOpacity(1);
                        }
                        waitForSecs(0.3f);
                    }
                    if(time == statusTime && !_aborting)
                    {
                        flashNextButton(statusTime);
                    }

                }
                catch (Exception exception)
                {
                    nextButton.setOpacity(1);
                }
            }
        });
    }
}
