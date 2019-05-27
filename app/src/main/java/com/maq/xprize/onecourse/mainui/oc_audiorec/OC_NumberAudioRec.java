package com.maq.xprize.onecourse.mainui.oc_audiorec;

import android.graphics.Color;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.utils.OBFont;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_NumberAudioRec extends OC_AudioRecSection
{
    OBLabel numberLabel;
    OBPhoneme currentPhoneme;
    List<OBPhoneme> eventsData;


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadEvent("master");
        eventsData = new ArrayList<>();
        String[] arr = parameters.get("targets").split(",");
        int index = 1;
        List<String> eventsList = new ArrayList<>();
        for(String param : arr)
        {
            OBPhoneme pho = new OBPhoneme(param,String.format("n_%s",param));
            eventsData.add(pho);
            String eventName = String.format("%d",index);
            if(audioScenes.get(eventName) == null)
                eventName = "default";
            eventsList.add(eventName);
            index++;
        }
        events = eventsList;
        setSceneXX(currentEvent());
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        OBPhoneme pho = eventsData.get(eventIndex);
        OBFont font = OBUtils.StandardReadingFontOfSize(250);
        OBControl textBox = objectDict.get("textbox");
        numberLabel = new OBLabel(pho.text,font);
        numberLabel.setColour(Color.BLACK);
        numberLabel.setPosition(textBox.position());
        numberLabel.hide();
        attachControl(numberLabel);
        currentPhoneme = pho;
        prepareForRecordingEvent(null);
    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.3f);
        playSfxAudio("numberon",false);
        numberLabel.show();
        waitSFX();
        demoSceneStart(numberLabel);
        waitForSecs(0.3f);
        playStartAudio("PROMPT",true);
        waitForSecs(0.3f);
        startRecordingEvent(numberLabel);
    }

    public void prepareForRecordingEvent(String audio)
    {
        int num = OBUtils.getIntValue(currentPhoneme.text);
        if(num <= 10)
            recordDuration = 3;
        else if(num <100)
            recordDuration = 4;
        else
            recordDuration = 8;
    }

    public void playTargetAudio() throws Exception
    {
        currentPhoneme.playAudio(this,true);
    }

    public void recordingEventFinished() throws Exception
    {
        numberLabel.setColour(Color.BLUE);
        waitForSecs(0.3f);
        currentPhoneme.playAudio(this, true);
        waitForSecs(0.3f);
        numberLabel.setColour(Color.BLACK);
        waitForSecs(0.7f);
        super.recordingEventFinished();
    }

    public void replayModelAudio() throws Exception
    {
        long token = takeSequenceLockInterrupt(true);
        try
        {
            if(token == sequenceToken)
            {
                numberLabel.setColour(Color.BLUE);
                currentPhoneme.playAudio(this,true);
                checkSequenceToken(token);
                waitForSecs(0.3f);
                checkSequenceToken(token);
            }
        }
        catch(Exception exception){

        }
        numberLabel.setColour(Color.BLACK);
        sequenceLock.unlock();
    }

    public void nextButtonPressed() throws Exception
    {
        if(!isLastEvent())
        {
            lockScreen();
            detachControl(numberLabel);
            numberLabel = null;
            nextButton.hide();
            unlockScreen();
            waitForSecs(0.5f);
        }
    }

}
