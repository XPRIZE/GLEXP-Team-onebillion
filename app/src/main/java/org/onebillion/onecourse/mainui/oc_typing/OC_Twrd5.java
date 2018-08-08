package org.onebillion.onecourse.mainui.oc_typing;

import android.graphics.Color;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by michal on 13/06/2018.
 */

public class OC_Twrd5 extends OC_Twrd_Text
{
    boolean withDemo;

    @Override
    public String sectionAudioName() {
        return "twrd5";
    }

    @Override
    public void prepare()
    {
        super.prepare();
        withDemo = OBUtils.getBooleanValue(parameters.get("demo"));
        keyAudio = false;
        replayAudioOnCorrect = false;
        eventsData = new ArrayList<>();
        String[] letters = parameters.get("letters").split(",");

        if(typewriterManager.capitalMode())
            mergeAudioScenesForPrefix("ALT");
        int index = withDemo ? 1 : 2;
        for(int i=0; i<letters.length; i++)
        {
            String letter = letters[i];
            Map<String, Object> data = new ArrayMap<>();
            String eventName = String.format("%d", index);
            data.put("text", letter);
            data.put("audio", String.format("alph_%s", letter.toLowerCase()));
            data.put("hidden", true);
            data.put("replayWrong", true);
            data.put("feedback", FEEDBACK_SFX);
            if (audioScenes.get(eventName) != null) {
                data.put("event", eventName);

            } else {
                data.put("event", "default");

            }
            eventsData.add(data);
            index++;
        }
        setCurrentTextEvent();
    }

    @Override
    public float getFontSize()
    {
        return applyGraphicScale(170);
    }

    @Override
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

    public void demo1() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForAudio();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.95f,this.bounds()),-20,0.5f,"DEMO2",0,0.3f);
        showLine();
        playCurrentAudio(true);
        waitForSecs(0.3f);
        OBGroup key = (OBGroup)currentWordData.get(0).get("key");
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.05f,key.frame()),-35,0.8f,"DEMO2",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.65f,key.frame()),-35,0.2f,true);
        lockScreen();
        demoKeyTouch(key);
        colourCurrentLetter(Color.BLACK);
        if(currentLetterIndex >= currentWordData.size()-1)
            screenLine.hide();
        unlockScreen();
        moveLineToNextLetter();
        waitForAudio();
        waitForSecs(0.3f);
        currentLetterIndex++;
        typewriterManager.lowlightKey(key);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.05f,key.frame()),-35,0.2f,true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        highlightTextWithAudio();
        waitForSecs(0.3f);
        hideText();
        waitForSecs(0.3f);
        playAudioScene("DEMO2",2,true);
        waitForSecs(0.3f);
        nextText();
    }

    public void demo2() throws Exception
    {
        if(withDemo)
        {
            playAudioQueuedScene("DEMO",0.3f,true);
        }
        else
        {
            playAudioQueuedScene("DEMO2",0.3f,true);
        }
    }

}
