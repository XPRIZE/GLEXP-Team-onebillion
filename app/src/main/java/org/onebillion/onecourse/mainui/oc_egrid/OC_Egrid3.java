package org.onebillion.onecourse.mainui.oc_egrid;

import android.graphics.Color;

import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 17/01/2018.
 */

public class OC_Egrid3 extends OC_Egrid
{
    int currentMode;
    List<OBPhoneme> targetPhonemes;
    OBLabel currentLabel;
    List<String> eventNames;
    List<OBLabel> labelsOrder;

    final int SYLLABLE_MODE=0,
            WORD_MODE=1,
            NON_WORD_MODE=2;

    public void prepare()
    {
        targetPhoneme = null;
        eventNames = Arrays.asList("s", "w", "n");
        super.prepare();
        if(parameters.get("mode").equals("syllable"))
        {
            currentMode = SYLLABLE_MODE;
        }
        else if(parameters.get("mode").equals("nonword"))
        {
            currentMode = NON_WORD_MODE;
        }
        else
        {
            currentMode = WORD_MODE;
            loadAudioXML(getConfigPath("egrid3waudio.xml"));
        }
        loadAudioXML(getConfigPath(String.format("egrid3%saudio.xml", eventNames.get(currentMode)))) ;
        labelsOrder = new ArrayList<>();
        List<String> eventsArray = new ArrayList<>();
        int index =1;
        for(List<OBLabel> labels : labelsLayout)
        {
            labelsOrder.addAll(labels);
            for(OBLabel lab : labels)
            {
                String eventName = String.format("%d", index);
                if(audioScenes.get(eventName) != null)
                {
                    eventsArray.add(eventName);
                }
                else
                {
                    eventsArray.add("default");
                }
                index++;
            }
        }
        events = eventsArray;
        currentLabel = labelsOrder.get(eventIndex);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                performSel("demo", eventNames.get(currentMode));
            }
        });
    }

    public void setSceneXX(String scene)
    {
        currentLabel = labelsOrder.get(eventIndex);
    }

    public void doMainXX() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        highlightLabel(currentLabel);
        startScene();
    }

    public void wordTouched(OBLabel label) throws Exception
    {
        if(currentLabel != null && currentLabel == label)
        {
            playAudio(null);
            playSFX(null);
            label.setColour(Color.RED);
            playAudioForLabel(currentLabel);
            waitForSecs(0.3f);

            if(getAudioForScene(currentEvent(),"CORRECT") != null)
            {
                playAudioQueuedScene("CORRECT",0.3f,true);
                waitForSecs(2f);
            }
            disableLabel(currentLabel);
            waitForSecs(0.3f);
            nextScene();
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void startScene() throws Exception
    {
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitSFX();
        waitForSecs(0.3f);
        if(!statusChanged(time))
            OBMisc.doSceneAudio(5,time,this);
    }

    public void highlightLabel(OBLabel label) throws Exception
    {
        lockScreen();
        label.setColour(Color.BLUE);
        playSfxAudio("red",false);
        unlockScreen();
    }

    public void demos() throws Exception
    {
        animateGridShow();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.95f,0.95f,objectDict.get("work_rect") .frame()),-15,0.5f,"DEMO",0,0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1f,currentLabel.frame()),-35,0.5f,"DEMO",3,0.3f);
        highlightLabel(currentLabel);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demow() throws Exception
    {
        demos();
    }

    public void demon() throws Exception
    {
        animateGridShow();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0f,1f,labelsOrder.get(0).frame()),0.7f,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,labelsOrder.get(labelsOrder.size()-1).frame()),1.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.95f,0.95f,objectDict.get("work_rect") .frame()),-15,0.5f,"DEMO",1,0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1f,currentLabel.frame()),-35,0.5f,"DEMO",4,0.3f);
        highlightLabel(currentLabel);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();

    }


}
