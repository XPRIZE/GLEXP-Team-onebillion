package com.maq.xprize.onecourse.hindi.mainui.oc_egrid;

import android.graphics.Color;

import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 17/01/2018.
 */

public class OC_Egrid1 extends OC_Egrid
{
    boolean searchMode, demo;
    List<OBLabel> wordLabels2;
    List<OBPhoneme> targetPhonemes;
    int currentPhonemeIndex;

    public void prepare()
    {
        super.prepare();
        events = Arrays.asList("1","2");
        loadAudioXML(getConfigPath("egrid1audio.xml"));
        searchMode = false;
        demo = OBUtils.getBooleanValue(parameters.get("demo"));
        wordLabels2 = new ArrayList<>();
        wordLabels2.addAll(wordLabels);
        targetPhonemes = new ArrayList<>();
        for(OBLabel label : OBUtils.randomlySortedArray(wordLabels))
        {
            targetPhonemes.add((OBPhoneme)label.propertyValue("phoneme"));
        }
    }

    public void setScene2() throws Exception
    {
        searchMode = true;
        wordLabels.addAll(wordLabels2);
        resetLabels(true);
        currentPhonemeIndex = 0;
        targetPhoneme = targetPhonemes.get(currentPhonemeIndex);
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

    public void nextTarget() throws Exception
    {
        wrongCount = 0;
        currentPhonemeIndex++;
        if(currentPhonemeIndex >= targetPhonemes.size())
        {
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

    public void  wordTouched(OBLabel label) throws Exception
    {
        if(!searchMode)
        {
            checkWordAudio(label);
        }
        else
        {
            checkWordWithTarget(label);
        }
    }

    public void  startScene() throws Exception
    {
        if(!searchMode)
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
        }
        else
        {
            setReplayAudio(audioWithPhoneme("PROMPT.REPEAT"));
            playAudioQueued(audioWithPhoneme("PROMPT"),true);
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitAudio();
            reprompt(time,audioWithPhoneme("REMIND"),5);
        }
    }

    public void  demo() throws Exception
    {
        animateGridShow();
        waitForSecs(0.3f);
        if(demo)
        {
            loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.95f,0.95f,objectDict.get("work_rect") .frame()),-15,0.5f,"DEMO",0,0.3f);
            OBLabel targetLabel = wordLabels.get(0);
            moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,targetLabel.frame()),-35,0.5f,"DEMO",1,0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,targetLabel.frame()),0.2f,true);
            targetLabel.setColour(Color.RED);
            waitForSecs(0.2f);
            movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,targetLabel.frame()),0.2f,true);
            playAudioForLabel(targetLabel);
            disableLabel(targetLabel);
            wordLabels.remove(targetLabel);
            waitForSecs(0.5f);
            thePointer.hide();
            playAudioScene("DEMO",2,true);
            waitForSecs(0.3f);
        }
        else
        {
            playAudioQueuedScene("DEMO2",0.3f,true);
            waitForSecs(0.3f);
        }
        startScene();
    }

    public void demo2() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.3f);
        startScene();

    }
}
