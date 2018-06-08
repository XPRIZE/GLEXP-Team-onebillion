package org.onebillion.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_BiggerNumberAudioRec extends OC_AudioRecSection
{
    List<OBLabel> screenLabels;
    OBPath screenLine;
    OBLabel targetLabel;
    List<Map<String,Object>> eventsData;
    int setSize;
    int highlightColour, flashColour, lineColour;


    public void prepare()
    {
        setSize = 2;
        setStatus(STATUS_BUSY);
        super.prepare();
        loadEvent("master");
        screenLine =(OBPath)objectDict.get("line");
        highlightColour = Color.BLUE;
        flashColour = OBUtils.colorFromRGBString("0,180,100");
        lineColour = screenLine.strokeColor();
        int group = OBUtils.getIntValue(parameters.get("group"));
        loadEventDataForGroup(group);
        //audioRecorder.setSoundTimming(2.5);
        setSceneXX(currentEvent());
        PointF loc = screenLine.position();
        loc.y = screenLabels.get(0).bottom() + applyGraphicScale(60);
        screenLine.setPosition(loc);
        screenLine.show();
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        Map<String,Object> eventData = eventsData.get(eventIndex);
        List<Integer> nums =(List<Integer>) eventData.get("numbers");
        int maxNum = -1;
        OBFont font = OBUtils.StandardReadingFontOfSize(160);
        List<OBLabel> numLabels = new ArrayList<>();
        PointF centerPoint = OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("work_rect") .frame());
        float halfDist =0;
        for(int i=0; i<nums.size(); i++)
        {
            int num = nums.get(i).intValue();
            OBLabel label = new OBLabel(String.format("%d",num) ,font);
            label.setZPosition(10);
            label.setPosition(OB_Maths.locationForRect(0.25f + i*0.5f,0.5f,objectDict.get("work_rect") .frame()));
            label.setColour(Color.BLACK);
            if(eventIndex > 0)
                label.hide();
            if(maxNum < num)
            {
                targetLabel = label;
                maxNum = num;
            }
            attachControl(label);
            numLabels.add(label);
            label.setProperty("audio",String.format("n_%d",num));
            label.setProperty("num_val",num);
            float curDist = 0;
            if(i == 0)
            {
                curDist = centerPoint.x - label.left();
            }
            else
            {
                curDist = label.right() - centerPoint.x;
            }
            if(curDist > halfDist)
                halfDist = curDist;

        }
        screenLine.setWidth(halfDist*2);
        screenLabels = numLabels;
        prepareForRecordingEvent(null);
    }

    @Override
    public void doMainXX() throws Exception
    {
        if(eventIndex != 0)
        {
            waitForSecs(0.3f);
            showLabels();
        }
        demoSceneStart(targetLabel);
        waitForSecs(0.3f);
        playStartAudio("PROMPT",true);
        waitForSecs(0.3f);
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

    @Override
    public void animateWordRecordStart(OBLabel curLabel,boolean pulse) throws Exception
    {
        lockScreen();
        screenLine.setStrokeColor(Color.RED);
        playSfxAudio("ping",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void animateWordRecordStop(OBLabel curLabel) throws Exception
    {
        recordingButton.stopRecordingAnimation();
        lockScreen();
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        screenLine.setStrokeColor(lineColour);
        playSfxAudio("click",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void recordingWrongLowlight(OBLabel label) throws Exception
    {
        screenLine.setStrokeColor(lineColour);
    }

    public void loadEventDataForGroup(int group)
    {
        List<List<Integer>> groupSets = new ArrayList<>();
        switch(group)
        {
            default:
            case 1:
                groupSets.add(Arrays.asList(1 ,8));
                groupSets.add(Arrays.asList(9 ,15));
                groupSets.add(Arrays.asList(16 ,25));
                groupSets.add(Arrays.asList(26 ,40));
                groupSets.add(Arrays.asList(41 ,55));
                groupSets.add(Arrays.asList(56 ,65));
                groupSets.add(Arrays.asList(66 ,75));
                groupSets.add(Arrays.asList(76 ,85));
                groupSets.add(Arrays.asList(86 ,95));
                groupSets.add(Arrays.asList(80 ,99));
                break;
            case 2:
                groupSets.add(Arrays.asList(1 ,9));
                groupSets.add(Arrays.asList(10 ,25));
                groupSets.add(Arrays.asList(26 ,45));
                groupSets.add(Arrays.asList(46 ,65));
                groupSets.add(Arrays.asList(66 ,85));
                groupSets.add(Arrays.asList(86 ,99));
                groupSets.add(Arrays.asList(100 ,300));
                groupSets.add(Arrays.asList(301 ,500));
                groupSets.add(Arrays.asList(501 ,750));
                groupSets.add(Arrays.asList(751 ,999));
                break;
        }
        int index = 0;
        eventsData = new ArrayList<>();
        List<String> eventsList = new ArrayList<>();
        for(int i=0; i<groupSets.size(); i++)
        {
            List<Integer> nums = groupSets.get(i);
            Map<String,Object> dict = new ArrayMap<>();
            int from = nums.get(0).intValue();
            int to = nums.get(1).intValue();
            List<Integer> arr = numListFrom(from,to,setSize);
            dict.put("numbers",arr);
            index++;
            eventsData.add(dict);
            String eventName = String.format("%d",i+1);
            if(audioScenes.get(eventName) == null)
                eventName = "default";
            eventsList.add(eventName);
        }
        events = eventsList;
    }

    public List<Integer> numListFrom(int from,int to,int size)
    {
        List<Integer> numList = OBMisc.integerList(from,to);
        return OBUtils.randomlySortedArray(numList).subList(0,size);
    }

    @Override
    public void playTargetAudio() throws Exception
    {
        String audio = (String)targetLabel.propertyValue("audio");
        playAudio(audio);
        waitForAudio();
    }

    @Override
    public void recordingEventFinished() throws Exception
    {
        targetLabel.setColour(highlightColour);
        playTargetAudio();
        waitForSecs(1f);
        playAudioQueuedScene("FINAL2",300,false);
        targetLabelFlash();
        waitForAudio();
        waitForSecs(1f);
        playAudioQueuedScene("FINAL3",300,true);
        waitForSecs(0.5f);
        if(!isLastEvent())
        {
            clearScene();
            waitForSecs(0.5f);
        }
        nextScene();
    }

    public void showLabels() throws Exception
    {
        lockScreen();
        for(OBLabel label : screenLabels)
            label.show();
        screenLine.show();
        playSfxAudio("numberson",false);
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
        screenLine.hide();
        playSfxAudio("numbersoff",false);
        unlockScreen();
        waitSFX();
    }

    public void targetLabelFlash() throws Exception
    {
        for(int i=0; i<3; i++)
        {
            targetLabel.setColour(flashColour);
            waitForSecs(0.25f);
            if(i < 2)
            {
                targetLabel.setColour(Color.BLACK);
                waitForSecs(0.25f);
            }
        }
    }

    @Override
    public void nextButtonPressed() throws Exception
    {

    }

    @Override
    public void demoIntro2() throws Exception
    {

    }

    @Override
    public void demoEvent1() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.8f,this.bounds()),-25,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
    }

    @Override
    public void demoEvent2() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",1,0.3f);
        thePointer.hide();
    }

    @Override
    public void demoExample(OBLabel curLabel) throws Exception
    {
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(0));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.75f,this.bounds()),-20,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(1));
        waitAudio();
        waitForSecs(0.3f);
        recordingButtonReady();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(2));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        animateWordRecordStart(curLabel,false);
        waitForSecs(0.3f);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_ACTIVE);
        recordingButton.startRecordingAnimation();
        waitForSecs(0.5f);
        targetLabel.setColour(highlightColour);
        playTargetAudio();
        waitAudio();
        waitForSecs(0.5f);
        playAudio(getAudioForScene("example","DEMO") .get(3));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,recordingButton.control.frame()),-30,0.2f,true);
        recordingButton.highlight();
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.2f,true);
        lockScreen();
        screenLine.setStrokeColor(lineColour);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        targetLabel.setColour(Color.BLACK);
        playSfxAudio("click",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,1.1f,targetLabel.frame()),-30,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(4));
        targetLabelFlash();
        waitForAudio();
        waitForSecs(0.3f);
        targetLabel.setColour(Color.BLACK);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.5f,true);
        playAudio(getAudioForScene("example","DEMO") .get(5));
        waitForAudio();
        waitForSecs(0.5f);
        thePointer.hide();
    }
}
