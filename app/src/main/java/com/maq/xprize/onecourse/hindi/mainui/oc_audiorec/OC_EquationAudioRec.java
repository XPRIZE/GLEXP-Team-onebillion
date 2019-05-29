package com.maq.xprize.onecourse.hindi.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_EquationAudioRec extends OC_AudioRecSection
{
    boolean additionMode;
    List<List<Integer>> eventsData;
    OBGroup currentEquation;
    OBPath targetLabelBox;
    int boxBorderColour;
    OBPhoneme currentPhoneme;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadEvent("master");
        targetLabelBox = (OBPath)objectDict.get("box");
        targetLabelBox.sizeToBoundingBoxIncludingStroke();
        boxBorderColour = targetLabelBox.strokeColor();
        additionMode = parameters.get("mode").equals("add");
        if(!additionMode)
            mergeAudioScenesForPrefix("ALT");
        int level = OBUtils.getIntValue(parameters.get("level"));
        loadEquationsList(level,additionMode);
        setSceneXX(currentEvent());
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        List<Integer> data = eventsData.get(eventIndex);
        loadEquationAndNumbers(data.get(0),data.get(1),additionMode);
        prepareForRecordingEvent(currentPhoneme.audio());
    }

    @Override
    public void doMainXX() throws Exception
    {
        waitForSecs(0.3f);
        showCurrentEquation();
        demoSceneStart(targetLabel);
        waitForSecs(0.3f);
        playStartAudio("PROMPT",true);
        waitForSecs(0.3f);
        startRecordingEvent(targetLabel);
    }

    public void loadEquationsList(int level,boolean addMode)
    {
        eventsData = new ArrayList<>();
        int x, y, z;
        if(addMode)
        {
            if(level <= 1)
            {
                List<List<Integer>> eqList = new ArrayList<>();
                z = OB_Maths.randomInt(3, 5);
                x = OB_Maths.randomInt((int)Math.floor(z/2.0) , z-1);
                y = z-x;
                equationAddTo(eqList,x,y);
                for(int i=0; i<3; i++)
                {
                    do
                    {
                        z = OB_Maths.randomInt(6, 10);
                        x = OB_Maths.randomInt((int)Math.floor(z/2.0) , z-1);
                        y = z-x;
                    }
                    while(!equationAddTo(eqList,x,y));
                }
                eventsData.addAll(OBUtils.randomlySortedArray(eqList));
                eqList = new ArrayList<>();
                do
                {
                    z = OB_Maths.randomInt(6, 10);
                    x = OB_Maths.randomInt(1, (int)Math.floor(z/2.0));
                    y = z-x;
                }
                while(!equationAddTo(eqList,x,y));
                eventsData.add(OB_Maths.randomInt(1, 4),eqList.get(0));
                eqList = new ArrayList<>();
                for(int i=0; i<5; i++)
                {
                    do
                    {
                        z = OB_Maths.randomInt(11, 19);
                        if(i==0)
                        {
                            x = OB_Maths.randomInt(10, z-1);
                            y = z-x;
                        }
                        else if(i==1)
                        {
                            x = OB_Maths.randomInt(1, 9);
                            y = z-x;
                        }
                        else if(i==2)
                        {
                            x = OB_Maths.randomInt(4, 9);
                            y = OB_Maths.randomInt(x+1, 10);
                        }
                        else
                        {
                            x = OB_Maths.randomInt(5, 10);
                            y = OB_Maths.randomInt(4, x-1);
                        }
                    }
                    while(!equationAddTo(eqList,x,y));
                }
                eventsData.addAll(OBUtils.randomlySortedArray(eqList));
            }
            else
            {
                for(int i=0; i<2; i++)
                {
                    do
                    {
                        y = OB_Maths.randomInt(5,9);
                        x = OB_Maths.randomInt(y+1, 26-y);
                    }
                    while(!equationAddTo(eventsData,x,y));
                }
                List<List<Integer>> eqList = new ArrayList<>();
                for(int i=0; i<3; i++)
                {
                    do
                    {
                        if(i==0)
                        {
                            y = OB_Maths.randomInt(11, 60);
                            x = OB_Maths.randomInt(11, 70-y);
                        }
                        else
                        {
                            x = OB_Maths.randomInt(11, 60);
                            y = OB_Maths.randomInt(11, 70-x);
                        }
                    }
                    while(!equationAddTo(eqList,x,y));
                }
                eventsData.addAll(OBUtils.randomlySortedArray(eqList));
            }
        }
        else
        {
            if(level <= 1)
            {
                for(int i=0; i<5; i++)
                {
                    do
                    {
                        x = OB_Maths.randomInt(3,10);
                        y = OB_Maths.randomInt(1, x-1);
                    } while(!equationAddTo(eventsData,x,y));

                }
                for(int i=0; i<5; i++)
                {
                    do
                    {
                        x = OB_Maths.randomInt(12, 18);
                        y = OB_Maths.randomInt(1, 10);
                    }
                    while(!equationAddTo(eventsData,x,y));
                }
            }
            else
            {
                for(int i=0; i<2; i++)
                {
                    do
                    {
                        x = OB_Maths.randomInt(18, 25);
                        y = OB_Maths.randomInt(4, 10);
                    }
                    while(!equationAddTo(eventsData,x,y));
                }
                for(int i=0; i<3; i++)
                {
                    do
                    {
                        x = OB_Maths.randomInt(30, 50);
                        y = OB_Maths.randomInt(11, 19);
                    }
                    while(!equationAddTo(eventsData,x,y));
                }
            }
        }

        if(level == 0)
        {
            int splitIndex = eventsData.size()/2;
            List<List<Integer>> subList1 = OBUtils.randomlySortedArray(eventsData.subList(0,splitIndex)).subList(0,3);
            List<List<Integer>> subList2 = OBUtils.randomlySortedArray(eventsData.subList(splitIndex,eventsData.size())).subList(0,3);
            eventsData = subList1;
            eventsData.addAll(subList2);
        }

        List<String> eventsList = new ArrayList<>();
        for(int i=0; i<eventsData.size(); i++)
        {
            String eventName = String.format("%d",i+1);
            if(audioScenes.get(eventName) == null)
                eventName = "default";
            eventsList.add(eventName);
        }
        events = eventsList;
    }

    public boolean equationAddTo(List<List<Integer>> eqList,int x,int y)
    {
        boolean add = true;
        for(List<Integer> arr : eqList)
        {
            if(x == arr.get(0) && y == arr.get(1))
            {
                add = false;
                break;
            }
        }
        if(add)
            eqList.add(Arrays.asList(x, y));
        return add;
    }

    public void showCurrentEquation() throws Exception
    {
        lockScreen();
        currentEquation.show();
        targetLabelBox.show();
        playSfxAudio("equationon",false);
        unlockScreen();
        waitSFX();
    }

    public void loadEquationAndNumbers(int num1,int num2,boolean addition)
    {
        if(currentEquation!=null)
            detachControl(currentEquation);
        int result = addition ? num1 + num2 : num1 - num2;
        String sign = addition ? "+" : "–";
        OC_Numberlines_Additions.loadEquation(String.format("%d %s %d = %d",num1,sign,num2,result) ,"equation",objectDict.get("eq_box") , Color.BLACK,false,this);
        currentEquation =(OBGroup)objectDict.get("equation");

        OC_Numberlines_Additions.showEquation(currentEquation,1,5,this);
        OC_Numberlines_Additions.colourEquation(currentEquation,5,5,Color.BLUE,this);
        currentEquation.hide();
        targetLabel = OC_Numberlines_Additions.getLabelForEquation(5, currentEquation);
        targetLabel.hide();
        String audioId = String.format("n_%d",result);
        currentPhoneme = new OBPhoneme(String.format("%d",result), audioId);
        RectF part3Frame = currentEquation.objectDict.get("part3").getWorldFrame();
        RectF part4Frame = currentEquation.objectDict.get("part4").getWorldFrame();
        float leftDist = part4Frame.left -(part3Frame.left + part3Frame.width());
        targetLabelBox.setPosition(currentEquation.position());
        targetLabelBox.setLeft(part4Frame.left + part4Frame.width() + leftDist);
        float eqLeft = currentEquation.left();
        float eqRight = targetLabelBox.right();
        float eqWidth = eqRight - eqLeft;
        float alignLeft = eqLeft - (this.bounds().width()*0.5f - (eqWidth * 0.5f));
        currentEquation.setLeft(currentEquation.left() - alignLeft);
        targetLabelBox.setLeft(targetLabelBox.left() - alignLeft);
        PointF loc = currentEquation.objectDict.get("part5").getWorldPosition();
        float left = currentEquation.objectDict.get("part5").left() +(targetLabelBox.position().x - loc.x);
        currentEquation.objectDict.get("part5").setLeft(left);
        currentEquation.setZPosition(10);
        targetLabelBox.setZPosition(1);
    }

    @Override
    public void demoExampleShowLabel() throws Exception
    {
        targetLabel.show();
    }

    @Override
    public void prepareForRecordingEvent(String audio)
    {
        recordDuration = 6;
    }

    @Override
    public void playTargetAudio() throws Exception
    {
        currentPhoneme.playAudio(this,true);
    }

    @Override
    public void recordingEventFinished() throws Exception
    {
        targetLabel.show();
        currentPhoneme.playAudio(this,true);
        waitForSecs(1f);
        lockScreen();
        targetLabel.setColour(Color.BLACK);
        targetLabelBox.setStrokeColor(boxBorderColour);
        unlockScreen();
        waitForSecs(0.5f);
        List<String> audio = getAudioForScene(currentEvent() ,"FINAL2");
        if(audio != null)
        {
            playAudio(audio.get(0));
            waitForAudio();
        }
        waitForSecs(1f);
        if(!isLastEvent())
        {
            lockScreen();
            currentEquation.hide();
            targetLabelBox.hide();
            playSfxAudio("equationoff",false);
            unlockScreen();
            waitSFX();
            waitForSecs(0.3f);
        }
        nextScene();
    }

    @Override
    public void animateWordRecordStart(OBLabel curLabel,boolean pulse) throws Exception
    {
        lockScreen();
        targetLabelBox.setStrokeColor(Color.RED);
        playSfxAudio("ping",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void animateWordRecordStop(OBLabel curLabel) throws Exception
    {
        recordingButton.stopRecordingAnimation();
        lockScreen();
        targetLabel.hide();
        targetLabelBox.setStrokeColor(boxBorderColour);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        playSfxAudio("cancel",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void demoEvent1() throws Exception
    {
        List<String> demoAudio = getAudioForScene(currentEvent() ,"DEMO");
        if(demoAudio != null)
        {
            waitForSecs(0.3f);
            if(thePointer == null || thePointer.hidden)
                loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",0,0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,targetLabelBox.frame()),-30,0.5f,"DEMO",1,0.3f);
            moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",2,0.5f);
        }
        thePointer.hide();
    }

    @Override
    public void demoEvent2() throws Exception
    {
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.7f,currentEquation.frame()),-20,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
    }

}
