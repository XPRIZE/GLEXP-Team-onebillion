package org.onebillion.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBAudioRecorder;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 07/06/2018.
 */

public class OC_AudioRecSection extends OC_SectionController
{
    public OBAudioRecorder audioRecorder;
    public float recordDuration;
    public OC_AudioRecordingButton recordingButton;
    public OBLabel targetLabel;
    public boolean showPresenter, showExample;
    public OBControl nextButton;

    private boolean recordingActive;
    private int currentRecordingAttempt;


    public static void flashNextButton(final OBControl button, final long time, final OBSectionController controller) throws Exception
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                if (time == controller.statusTime() && !controller._aborting)
                    controller.waitForSecs(5f);
                if (time == controller.statusTime() && !controller._aborting) {
                    button.setOpacity(1);
                    controller.waitForSecs(0.5f);
                }
                for (int i = 0; i < 2; i++) {
                    if (time == controller.statusTime() && !controller._aborting) {
                        button.setOpacity(0.4f);
                        controller.waitForSecs(0.3f);
                    }
                    if (time == controller.statusTime() && !controller._aborting) {
                        button.setOpacity(1);
                        controller.waitForSecs(0.3f);
                    }
                }
                button.setOpacity(1);
                if (time == controller.statusTime() && !controller._aborting)
                    flashNextButton(button, time, controller);
            }
        } );
    }

    public static float getExpectedAudioLength(float recordDuration)
    {
        if(recordDuration < 1)
            recordDuration = 1;
        float expectedAudioLength = 2.5f + (recordDuration*2.5f);
        if(expectedAudioLength < recordDuration*3)
            expectedAudioLength = recordDuration*3;
        return expectedAudioLength;
    }

    public static OBControl loadNextButton(OBSectionController controller)
    {
        OBControl nextButton = controller.loadVectorWithName("arrow_next",new PointF(0, 0),controller.boundsf());
        nextButton.setScale(controller.applyGraphicScale(100) /nextButton.height());
        nextButton.setBottom(controller.bounds().height() - controller.applyGraphicScale(10));
        nextButton.setRight(controller.bounds().width() - controller.applyGraphicScale(10));
        nextButton.hide();
        return nextButton;
    }

    public long statusSetWaitRecordStart()
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public long statusSetWaitRecordStop()
    {
        return setStatus(STATUS_DRAGGING);
    }

    public long statusSetWaitNextButton()
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public boolean statusWaitRecordStart()
    {
        return status() == STATUS_WAITING_FOR_DRAG;
    }

    public boolean statusWaitRecordStop()
    {
        return status() == STATUS_DRAGGING;
    }

    public boolean statusWaitNextButton()
    {
        return status() == STATUS_AWAITING_CLICK;
    }


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        showPresenter = OBUtils.getBooleanValue(parameters.get("presenter"));
        showExample = OBUtils.getBooleanValue(parameters.get("example"));
        audioRecorder = new OBAudioRecorder(OBUtils.getFilePathForTempFile(this),this);
        nextButton = loadNextButton(this);
        recordingButton = new OC_AudioRecordingButton((OBGroup) objectDict.get("recording_button"),this);
        recordingButton.control.hide();
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        recordingActive = false;
    }


    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {

                demoIntro1();
                demoInitScreen();
                doMainXX();
            }
        } );

    }

    public int buttonFlags()
    {
        return MainViewController().SHOW_TOP_LEFT_BUTTON|MainViewController().SHOW_TOP_RIGHT_BUTTON|0|0;
    }

    public void nextButtonPressed() throws Exception
    {

    }

    public void replayAudio()
    {
        if(statusWaitNextButton())
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    replayModelAudio();
                }
            } );
        }
        else if(statusWaitRecordStart())
        {
            super.replayAudio();

        }

    }

    @Override
    public void cleanUp()
    {
        onPause();
        OBUtils.cleanUpTempFiles(this);
        super.cleanUp();
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
                        performRecordingEvent(targetLabel);
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
                        nextButtonPressed();
                        nextScene();
                    }
                } );
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

    public void cleanUpRecorder()
    {
        OBUtils.cleanUpTempFiles(this);
    }

    public void nextScene()
    {
        cleanUpRecorder();
        super.nextScene();
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        nextButton.hide();
        currentRecordingAttempt = 1;
    }

    public OBLabel labelForText(String text,OBFont font)
    {
        OBControl textBox = objectDict.get("textbox");
        OBLabel label = new OBLabel(text,font);
        label.setColour(Color.BLACK);
        label.setPosition(textBox.position());
        label.hide();
        attachControl(label);
        if(label.width()>textBox.width())
            label.setScale(1.0f -((label.width()-textBox.width())*1.0f/label.width()));
        return label;
    }

    public void animateWordRecordStart(OBLabel curLabel,boolean pulse) throws Exception
    {
        lockScreen();
        curLabel.setColour(Color.RED);
        playSfxAudio("ping",false);

        unlockScreen();
        if(pulse)
            animateWordPulse(curLabel);
        waitSFX();
    }

    public void animateWordRecordStop(OBLabel curLabel) throws Exception
    {
        recordingButton.stopRecordingAnimation();
        lockScreen();
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        curLabel.setColour(Color.BLACK);
        playSfxAudio("cancel",false);
        unlockScreen();
        waitSFX();
    }

    public void startRecording()
    {
        audioRecorder.startRecording(getExpectedAudioLength(recordDuration));
    }

    public void stopRecording()
    {
        audioRecorder.stopRecording();

    }
    public void prepareForRecordingEvent(String audio)
    {
        OBAudioManager.audioManager.prepareForChannel(audio,"special");
        recordDuration = (float)OBAudioManager.audioManager.durationForChannel("special");
    }

    public void startRecordingEvent(OBLabel curLabel) throws Exception
    {
        recordingButtonReady();
        targetLabel = curLabel;
        long time = statusSetWaitRecordStart();
        recordingButton.flash(time,8);
    }


    public void performRecordingEvent(OBLabel curLabel) throws Exception
    {
        animateWordRecordStart(curLabel,true);
        waitForSecs(0.3f);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_ACTIVE);
        recordingButton.startRecordingAnimation();
        startRecording();
        statusSetWaitRecordStop();
        recordingActive = true;
        audioRecorder.waitForRecord();
        recordingButton.stopRecordingAnimation();
        setStatus(STATUS_BUSY);
        animateWordRecordStop(curLabel);
        waitForSecs(0.5f);
        if(currentRecordingAttempt < 3 && !audioRecorder.audioRecorded())
        {
            if(currentRecordingAttempt == 1 || getAudioForScene(currentEvent() ,"INCORRECT2") == null)
            {
                playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene(currentEvent() ,"INCORRECT") , 300),true);
            }
            else
            {
                playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene(currentEvent() ,"INCORRECT2") , 300),true);
            }
            OBUtils.cleanUpTempFiles(this);
            currentRecordingAttempt++;
            recordingButtonReady();
            statusSetWaitRecordStart();
        }
        else
        {
            if(getAudioForScene(currentEvent() ,"FINAL") != null)
            {
                playAudio(getAudioForScene(currentEvent(),"FINAL").get(0));
                waitAudio();
            }
            waitForSecs(0.5f);
            audioRecorder.playRecording();
            waitForSecs(0.7f);
            recordingEventFinished();
        }
    }

    public void recordingEventFinished() throws Exception
    {
        if(!isLastEvent())
        {
            List<String> audio = getAudioForScene(currentEvent() ,"FINAL2");
            if(audio != null)
            {
                playAudio(audio.get(0));
                waitForAudio();
                waitForSecs(1.5f);
            }
            waitForSecs(1f);
            showNextButtonWithAudio();
        }
        else
        {
            nextScene();
        }
    }

    public void showNextButtonWithAudio() throws Exception
    {
        nextButton.lowlight();
        nextButton.setOpacity(1);
        nextButton.show();
        playSfxAudio("arrowon",true);
        playAudioQueuedScene("ARROW",300,false);
        flashNextButton(nextButton,statusSetWaitNextButton(),this);
    }

    public void recordingWrongLowlight(OBLabel label) throws Exception
    {
        label.setColour(Color.BLACK);
    }

    public boolean isLastEvent() throws Exception
    {
        return eventIndex == events.size()-1;
    }

    public void animateWordPulse(OBLabel wordLabel)
    {
        float startScale = wordLabel.scale();
        OBAnimationGroup.chainAnimations(Arrays.asList(Collections.singletonList(OBAnim.scaleAnim(startScale*1.2f,wordLabel)),
                Collections.singletonList(OBAnim.scaleAnim(startScale,wordLabel))),
                Arrays.asList(0.2f,0.2f), false,
                Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT),1,this);
    }

    public void replayModelAudio() throws Exception
    {

    }

    public void playStartAudio(String name,boolean wait) throws Exception
    {
        setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent() ,String.format("%s.REPEAT",name)) , 300));
        playAudioQueuedScene(name,300,true);
    }

    public void playTargetAudio() throws Exception
    {

    }

    public void demoExampleShowLabel() throws Exception
    {

    }

    public void recordingButtonShow() throws Exception
    {
        recordingButton.control.show();
        playSfxAudio("buttonon",true);
    }

    public void recordingButtonReady() throws Exception
    {
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_NORMAL);
        playSfxAudio("buttonready",true);
    }

    public void demoInitScreen() throws Exception
    {
        waitForSecs(0.3f);
        recordingButtonShow();
        waitForSecs(0.3f);

    }
    public void demoIntro1() throws Exception
    {
        if(showPresenter)
        {
            OBMisc.standardDemoIntro1(this);
        }
    }

    public void demoIntro2() throws Exception
    {
        if(!showPresenter)
        {
            OBMisc.standardDemoIntro2(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),this);

        }
    }

    public void demoSceneStart(OBLabel curLabel) throws Exception
    {
        targetLabel = curLabel;
        if(eventIndex == 0)
        {
            demoIntro2();
            if(showExample)
            {
                demoExample(curLabel);
                waitForSecs(0.3f);

            }
            performSel("demoEvent",currentEvent());
        }
        else
        {
            waitForSecs(0.3f);
            performSel("demoEvent",currentEvent());
        }
    }

    public void demoPreExample() throws Exception
    {

    }

    public void demoEvent1() throws Exception
    {
        demoEvent1AudioCategory("DEMO");
    }

    public void demoEvent1AudioCategory(String audio) throws Exception
    {
        List<String> demoAudio = getAudioForScene(currentEvent() ,audio);
        if(demoAudio != null)
        {
            waitForSecs(0.3f);
            if(thePointer == null || thePointer.hidden)
                loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,audio,0,0.3f);
            moveScenePointer(OB_Maths.locationForRect(1.15f,0.9f,targetLabel.frame()),-30,0.5f,audio,1,0.3f);
            moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,audio,2,0.5f);
        }
        thePointer.hide();
        waitForSecs(0.5f);
    }

    public void demoEvent2() throws Exception
    {
        demoPointForLabel(targetLabel,"DEMO");
        waitForSecs(0.5f);
    }

    public void demoPointForLabel(OBLabel curLabel,String audioCat) throws Exception
    {
        List<String> demoAudio = getAudioForScene(currentEvent() ,audioCat);
        if(demoAudio != null)
        {
            waitForSecs(0.3f);
            if(thePointer == null || thePointer.hidden)
                loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(1.15f,0.9f,curLabel.frame()),-30,0.5f,audioCat,0,0.3f);
        }
        thePointer.hide();
    }

    public void demoExample(OBLabel curLabel) throws Exception
    {
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-20,0.5f,true);
        playAudio(getAudioForScene("example","DEMO").get(0));
        waitAudio();
        demoPreExample();
        waitForSecs(0.3f);
        recordingButtonReady();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO").get(1));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        animateWordRecordStart(curLabel,true);
        waitForSecs(0.3f);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_ACTIVE);
        recordingButton.startRecordingAnimation();
        waitForSecs(0.5f);
        demoExampleShowLabel();
        playTargetAudio();
        waitAudio();
        waitForSecs(0.5f);
        playAudio(getAudioForScene("example","DEMO").get(2));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        animateWordRecordStop(curLabel);
        waitForSecs(0.5f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO").get(3));
        waitAudio();
        waitForSecs(0.3f);
    }

}
