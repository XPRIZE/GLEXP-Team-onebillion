package org.onebillion.onecourse.mainui.oc_egrid;

import android.graphics.Color;

import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 17/01/2018.
 */

public class OC_Egrid2 extends OC_Egrid
{
    boolean demo;
    int currentPhonemeIndex;
    List<OBPhoneme> targetPhonemes;
    OBLabel demoLabel;

    public void prepare()
    {
        super.prepare();
        events = Arrays.asList("1");
        loadAudioXML(getConfigPath("egrid2audio.xml"));
        currentPhonemeIndex = 0;
        targetPhonemes = new ArrayList<>();
        demo = OBUtils.getBooleanValue(parameters.get("demo"));
        demoLabel = null;
        for(OBLabel label : OBUtils.randomlySortedArray(wordLabels))
        {
            if(demoLabel == null && demo)
                demoLabel = label;
            else
                targetPhonemes.add((OBPhoneme)label.propertyValue("phoneme"));
        }
        currentPhonemeIndex = 0;
        targetPhoneme = targetPhonemes.get(0);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo();
            }
        });
    }

    public void wordTouched(OBLabel label) throws Exception
    {
        checkWordWithTarget(label);
    }

    public void nextTarget() throws Exception
    {
        wrongCount = 0;
        currentPhonemeIndex++;
        if(currentPhonemeIndex >= targetPhonemes.size())
        {
            displayTick();
            waitForSecs(0.3f);
            fin();
        }
        else
        {
            resetLabels(false);
            waitForSecs(0.3f);
            targetPhoneme = targetPhonemes.get(currentPhonemeIndex);
            String nextPhonemeAudio = targetPhoneme.audio();
            setReplayAudio(audioWithPhoneme("PROMPT.REPEAT"));
            playAudio(nextPhonemeAudio);
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitAudio();
            reprompt(time,audioWithPhoneme("REMIND"),5);
        }
    }

    public void startScene() throws Exception
    {
        setReplayAudio(audioWithPhoneme("PROMPT.REPEAT"));
        playAudioQueued(audioWithPhoneme("PROMPT"),true);
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitAudio();
        reprompt(time,audioWithPhoneme("REMIND"),5);
    }

    public void demo() throws Exception
    {
        animateGridShow();
        waitForSecs(0.3f);
        if(demo && demoLabel != null)
        {
            loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.95f,0.95f,objectDict.get("work_rect") .frame()),-15,0.5f,"DEMO",0,0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.6f,0.9f,objectDict.get("work_rect") .frame()),-25,0.5f,"DEMO",1,0.3f);
            playAudioForLabel(demoLabel);
            waitForSecs(0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,demoLabel.frame()),-35,0.5f,"DEMO",2,0.3f);
            playAudioScene("DEMO",3,true);
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,demoLabel.frame()),0.2f,true);
            demoLabel.setColour(Color.RED);
            playAudio("correct");
            waitForSecs(0.2f);
            movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,demoLabel.frame()),0.2f,true);
            waitAudio();
            disableLabel(demoLabel);
            waitForSecs(0.5f);
            thePointer.hide();
            playAudioScene("DEMO",4,true);
            waitForSecs(0.3f);
        }
        else
        {
            playAudioQueuedScene("DEMO2",0.3f,true);
            waitForSecs(0.3f);
        }
        startScene();
    }
}
