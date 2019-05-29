package com.maq.xprize.onecourse.hindi.mainui.oc_typing;

import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 13/06/2018.
 */

public class OC_Twrd2 extends OC_Twrd_Text
{
    boolean withDemo;

    @Override
    public String sectionAudioName() {
        return "twrd2";
    }

    @Override
    public void prepare()
    {
        super.prepare();
        withDemo = OBUtils.getBooleanValue(parameters.get("demo"));
        keyAudio = OBUtils.getBooleanValue(parameters.get("keyaudio"));
        if(!withDemo)
            mergeAudioScenesForPrefix("ALT");
        Map<String,OBPhoneme> wordComponents = OBUtils.LoadWordComponentsXML(true);
        eventsData = new ArrayList<>();
        String[] words = parameters.get("words").split(",");
        int index = withDemo ? 1 : 2;
        for(int i=0; i<words.length; i++)
        {
            String wordid = words[i];
            if(wordComponents.containsKey(wordid))
            {
                OBWord word = (OBWord)wordComponents.get(wordid);
                for(int j=1; j<3; j++)
                {
                    if(withDemo && index == 1 && j>1)
                        continue;
                    Map<String, Object> data = new ArrayMap();
                    String eventName = String.format("%d%d",index,j);
                    data.put("text",word.text);
                    data.put("audio",word.audio());
                    if(imageMode && word.imageName != null)
                        data.put("image",word.imageName);
                    data.put("hidden",j==2);
                    data.put("replayWrong",j==2);
                    data.put("feedback",j==2?FEEDBACK_TICK:FEEDBACK_NONE);
                    if(i == words.length-1 && words.length > 2)
                    {
                        data.put("event",String.format("last%d",j));
                    }
                    else if(audioScenes.get(eventName) != null)
                    {
                        data.put("event",eventName);
                    }
                    else
                    {
                        data.put("event",String.format("default%d",j));
                    }
                    eventsData.add(data);
                }
                index++;
            }
        }
        setCurrentTextEvent();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                if (withDemo)
                    performSel("demo", currentEvent());
                else
                    startCurrentScene();
            }
        });
    }

    public void demo11() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.95f,this.bounds()),-20,0.5f,"DEMO",1,0.3f);
        showText();
        waitForSecs(0.3f);
        highlightTextWithAudio();
        lowLightText();
        waitForSecs(0.3f);
        showLine();
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.8f,objectDict.get("word_box") .frame()),-10,0.5f,"DEMO",2,0.3f);
        demoTypeLabels();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.8f,objectDict.get("word_box") .frame()),-20,0.5f,true);
        highlightTextWithAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        hideText();
        waitForSecs(0.5f);
        nextText();
    }

    public void demo21() throws Exception
    {
        if(withDemo)
        {
            showText();
            waitForSecs(0.3f);
            playAudioQueuedScene("DEMO",0.3f,true);
        }
        else
        {
            playAudioScene("DEMO",0,true);
            waitForSecs(0.3f);
            showText();
            waitForSecs(0.3f);
            playAudioScene("DEMO",1,true);
        }
    }

}
