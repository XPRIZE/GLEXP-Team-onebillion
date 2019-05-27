package com.maq.xprize.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.utils.OBFont;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_MissingNumberAudioRec extends OC_AudioRecSection
{
    List<OBControl> screenBoxes;
    List<OBLabel> screenLabels;
    List<Map<String,Object>> eventsData;
    int setSize;
    boolean playFeedback;
    int highlightColour;

    public void prepare()
    {
        setSize = 4;
        setStatus(STATUS_BUSY);
        super.prepare();
        loadEvent("master");
        highlightColour = Color.BLUE;
        loadBoxes();
        int group = OBUtils.getIntValue(parameters.get("group"));
        playFeedback = OBUtils.getBooleanValue(parameters.get("feedback"));
        loadEventDataForGroup(group);
        setSceneXX(currentEvent());
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        Map<String,Object> eventData = eventsData.get(eventIndex);
        List<Integer> nums =(List<Integer>) eventData.get("numbers");
        int targetIndex = (int)eventData.get("index");
        OBFont font = OBUtils.StandardReadingFontOfSize(70);
        List<OBLabel> numLabels = new ArrayList<>();
        for(int i=0; i<nums.size(); i++)
        {
            int num = nums.get(i).intValue();
            OBLabel label = new OBLabel(String.format("%d",num) ,font);
            OBControl box = screenBoxes.get(i);
            label.setZPosition(10);
            label.setPosition(box.position());
            label.setColour(Color.BLACK);
            if(i==targetIndex)
            {
                targetLabel = label;
                targetLabel.setColour(highlightColour);
                label.hide();
            }
            if(eventIndex > 0)
                label.hide();
            attachControl(label);
            numLabels.add(label);
            label.setProperty("audio",String.format("n_%d",num));
            label.setProperty("num_val",num);
            box.setProperty("label",label);
            label.setProperty("box",box);
        }
        screenLabels = numLabels;
    }

    public void doMainXX() throws Exception
    {
        if(eventIndex != 0)
        {
            showLabels();
            waitForSecs(0.3f);
        }
        demoSceneStart(targetLabel);
        waitForSecs(0.3f);
        playStartAudio("PROMPT",false);
        startRecordingEvent(targetLabel);
    }


    public void prepareForRecordingEvent(String audio)
    {
        int num =(int)targetLabel.propertyValue("num_val");
        if(num <= 10)
            recordDuration = 3;
        else if(num <100)
            recordDuration = 4;
        else
            recordDuration = 6;
    }

    public void animateWordRecordStart(OBLabel curLabel,boolean pulse) throws Exception
    {
        OBPath box = (OBPath)curLabel.propertyValue("box");
        lockScreen();
        recordingButton.highlight();
        highlightBox(box,true);
        playSfxAudio("ping",false);
        unlockScreen();
        waitSFX();
    }

    public void animateWordRecordStop(OBLabel curLabel) throws Exception
    {
        recordingButton.stopRecordingAnimation();
        OBPath box = (OBPath)curLabel.propertyValue("box");
        lockScreen();
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        highlightBox(box,false);
        playSfxAudio("click",false);
        unlockScreen();
        waitSFX();
    }

    public void recordingWrongLowlight(OBLabel label)
    {
        OBPath box = (OBPath)label.propertyValue("box");
        highlightBox(box,false);
    }

    public void loadBoxes()
    {
        List<OBControl> boxes = new ArrayList<>();
        OBPath box = (OBPath)objectDict.get("box");
        OBControl workRect = objectDict.get("work_rect");
        float lineWidth = box.borderWidth;
        PointF centrePoint = OB_Maths.locationForRect(0.5f,0.5f,workRect.frame());
        float left = (float)Math.floor(centrePoint.x - (setSize/2.0f) *box.width() +(setSize/2.0f) *lineWidth);
        for(int i=1; i<=setSize; i++)
        {
            OBPath boxCopy = (OBPath)box.copy();
            attachControl(boxCopy);
            boxCopy.setPosition(centrePoint);
            boxCopy.setLeft(left);
            left = boxCopy.right() - lineWidth;
            boxCopy.show();
            boxes.add(boxCopy);
            boxCopy.setZPosition(5);
            boxCopy.sizeToBoundingBoxIncludingStroke();
        }
        screenBoxes = boxes;
    }

    public void loadEventDataForGroup(int group)
    {
        List<List<Integer>> groupSets = new ArrayList<>();
        switch(group)
        {
            default:
            case 1:
                groupSets.add(Arrays.asList(1 ,10 ,1));
                groupSets.add(Arrays.asList(10 ,20 ,1));
                groupSets.add(Arrays.asList(2 ,10 ,2));
                groupSets.add(Arrays.asList(10 ,20 ,2));
                groupSets.add(Arrays.asList(5 ,20 ,5));
                groupSets.add(Arrays.asList(10 ,1 ,1));
                groupSets.add(Arrays.asList(20 ,10 ,1));
                groupSets.add(Arrays.asList(10 ,2 ,2));
                groupSets.add(Arrays.asList(20 ,10 ,2));
                groupSets.add(Arrays.asList(20 ,5 ,5));
                break;
            case 2:
                groupSets.add(Arrays.asList(1 ,15 ,1));
                groupSets.add(Arrays.asList(16 ,50 ,1));
                groupSets.add(Arrays.asList(10 ,50 ,10));
                groupSets.add(Arrays.asList(2 ,30 ,2));
                groupSets.add(Arrays.asList(5 ,50 ,5));
                groupSets.add(Arrays.asList(15 ,1 ,1));
                groupSets.add(Arrays.asList(50 ,16 ,1));
                groupSets.add(Arrays.asList(50 ,10 ,10));
                groupSets.add(Arrays.asList(30 ,2 ,2));
                groupSets.add(Arrays.asList(50 ,5 ,5));
                break;
            case 3:
                groupSets.add(Arrays.asList(1 ,30 ,1));
                groupSets.add(Arrays.asList(30 ,100 ,1));
                groupSets.add(Arrays.asList(10 ,100 ,10));
                groupSets.add(Arrays.asList(2 ,30 ,2));
                groupSets.add(Arrays.asList(5 ,100 ,5));
                groupSets.add(Arrays.asList(100 ,30 ,1));
                groupSets.add(Arrays.asList(100 ,10 ,10));
                groupSets.add(Arrays.asList(30 ,2 ,2));
                groupSets.add(Arrays.asList(100 ,5 ,5));
                groupSets.add(Arrays.asList(OB_Maths.randomInt(1, 4) ,20 ,5));
                break;
            case 4:
                groupSets.add(Arrays.asList(1 ,20 ,1));
                groupSets.add(Arrays.asList(20 ,100 ,1));
                groupSets.add(Arrays.asList(10 ,100 ,10));
                groupSets.add(Arrays.asList(100 ,900 ,100));
                groupSets.add(Arrays.asList(2 ,40 ,2));
                groupSets.add(Arrays.asList(100 ,999 ,1));
                groupSets.add(Arrays.asList(40 ,2 ,2));
                groupSets.add(Arrays.asList(5 ,100 ,5));
                groupSets.add(Arrays.asList(990 ,100 ,10));
                groupSets.add(Arrays.asList(OB_Maths.randomInt(1, 4) ,30 ,5));
                break;
        }
        List<Integer> indexList = OBUtils.RandomIndexesTo(setSize);
        int index = 0;
        eventsData = new ArrayList<>();
        List<String> eventsList = new ArrayList<>();
        for(int i=0; i<groupSets.size(); i++)
        {
            List<Integer> nums = groupSets.get(i);
            if(index >= indexList.size())
            {
                index = 0;
                indexList = OBUtils.randomlySortedArray(indexList);
            }
            Map<String,Object> dict = new ArrayMap();
            List<Integer> arr = numListFrom(nums.get(0),nums.get(1),nums.get(2),setSize);
            dict.put("numbers",arr);
            dict.put("index",indexList.get(index));
            index++;
            eventsData.add(dict);
            String eventName = String.format("%d",i+1);
            if(audioScenes.get(eventName) == null)
                eventName = "default";
            eventsList.add(eventName);
        }
        events = eventsList;
    }

    public List<Integer> numListFrom(int from, int to, int increment, int size)
    {
        List<Integer> numList = new ArrayList<>();
        if(from < to)
        {
            for(int i=from; i<=to; i+=increment)
                numList.add(i);
        }
        else
        {
            for(int i=from; i>=to; i-=increment)
                numList.add(i);
        }
        int startIndex = OB_Maths.randomInt(0, numList.size()-size-1);
        if(startIndex < 0)
            startIndex = 0;
        return numList.subList(startIndex, startIndex+size);
    }

    public void playTargetAudio() throws Exception
    {
        String audio = (String)targetLabel.propertyValue("audio");
        playAudio(audio);
        waitForAudio();
    }

    public void recordingEventFinished() throws Exception
    {
        playTargetAudio();
        waitForSecs(0.3f);
        showTargetLabel();
        waitForSecs(0.5f);
        targetLabel.setColour(Color.BLACK);
        waitForSecs(0.5f);
        if(getAudioForScene(currentEvent() ,"FINAL2") != null)
        {
            playAudioQueuedScene("FINAL2",0.3f,true);
            waitForSecs(1f);
        }
        if(playFeedback)
            demoFeedback();
        waitForSecs(0.5f);
        if(!isLastEvent())
        {
            clearScene();
            waitForSecs(0.4f);
        }
        nextScene();
    }

    public void showTargetLabel() throws Exception
    {
        playSfxAudio("numberin",false);
        targetLabel.show();
        waitSFX();
    }

    public void hideTargetLabel() throws Exception
    {
        playSfxAudio("numberoff",false);
        targetLabel.hide();
        waitSFX();
    }

    public void showLabels() throws Exception
    {
        lockScreen();
        for(OBLabel label : screenLabels)
            if(label != targetLabel)
                label.show();
        playSfxAudio("newnumberson",false);
        unlockScreen();
        waitSFX();
    }

    public void clearScene() throws Exception
    {
        lockScreen();
        for(OBLabel label : screenLabels)
            detachControl(label);
        screenLabels = null;
        targetLabel = null;
        playSfxAudio("allnumbersoff",false);
        unlockScreen();
        waitSFX();
    }

    public void highlightBox(OBPath box,boolean on)
    {
        if(on)
        {
            box.setZPosition(6);
            box.setStrokeColor(Color.RED);
        }
        else
        {
            OBPath orgBox =(OBPath)objectDict.get("box");
            box.setStrokeColor(orgBox.strokeColor());
            box.setZPosition(5);
        }
    }

    public void nextButtonPressed()
    {

    }


    public void demoExample(OBLabel curLabel)
            throws Exception
    {
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.3f,screenBoxes.get(0).frame()),-40,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(0));
        movePointerToPoint(OB_Maths.locationForRect(1f,1.3f,screenBoxes.get(screenBoxes.size()-1).frame()),-20,2,true);
        waitAudio();
        OBPath box = (OBPath)curLabel.propertyValue("box");
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.05f,box.frame()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(1));
        waitForAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-20,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(2));
        waitAudio();
        waitForSecs(0.3f);
        recordingButtonReady();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(3));
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
        playTargetAudio();
        waitAudio();
        waitForSecs(0.5f);
        playAudio(getAudioForScene("example","DEMO") .get(4));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        animateWordRecordStop(curLabel);
        waitForSecs(0.5f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(5));
        waitAudio();
        waitForSecs(0.3f);
    }

    public void demoEvent1() throws Exception
    {
        OBPath box = (OBPath)targetLabel.propertyValue("box");
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.05f,box.frame()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.5f,box.frame()),-25,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
    }

    public void demoEvent2() throws Exception
    {
        OBPath box = (OBPath)targetLabel.propertyValue("box");
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.05f,box.frame()),-20,0.5f,"DEMO",0,0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",2,0.3f);
        thePointer.hide();
    }

    public void demoEvent3() throws Exception
    {
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.3f);
        thePointer.hide();
    }

    public void demoEvent4() throws Exception
    {
        demoEvent3();
    }

    public void demoFeedback() throws Exception
    {
        playAudioQueuedScene("FINAL3",0.3f,true);
        for(OBLabel label : screenLabels)
        {
            String audio = (String)label.propertyValue("audio");
            playAudio(audio);
            label.setColour(Color.RED);
            waitForAudio();
            waitForSecs(0.1f);
            label.setColour(Color.BLACK);
            waitForSecs(0.1f);
        }
        waitForSecs(0.5f);
        lockScreen();
        for(OBLabel label : screenLabels)
            label.setColour(Color.BLACK);
        unlockScreen();
    }

}
