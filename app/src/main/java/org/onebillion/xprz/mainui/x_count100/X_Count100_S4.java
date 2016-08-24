package org.onebillion.xprz.mainui.x_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 24/08/16.
 */
public class X_Count100_S4 extends XPRZ_SectionController
{
    OBLabel counter;
    String child;
    int numColour, hiliteColour;
    int currentContainer;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master4");
        X_Count100_Additions.loadNumbersAudio(this);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBGroup machine = (OBGroup)objectDict.get("machine");
        OBGroup machineChildGroup = (OBGroup)machine.objectDict.get("child");
        machineChildGroup.objectDict.get("boy").hide();
        machineChildGroup.objectDict.get("girl").hide();

        objectDict.get("bottombar").setTop(machine.bottom());
        numColour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hiliteColour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(80);
        counter = new OBLabel("000",font,fontSize);
        counter.setColour(numColour);
        OBControl box = machine.objectDict.get("numbox");
        counter.setPosition(OB_Maths.worldLocationForControl(0.5f,0.5f,box));
        counter.setZPosition(1.5f);
        counter.setString("0");
        attachControl(counter);
        child = "girl";
        currentContainer = 1;

        for(OBControl con : machine.filterMembers("container.*"))
        {
            if(con.getClass() == OBPath.class)
                ((OBPath)con).sizeToBoundingBoxIncludingStroke();
        }

        OBPath marble = (OBPath)machine.objectDict.get("marble");
        marble.sizeToBoundingBoxIncludingStroke();

        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo();
            }
        });
    }

    @Override
    public void fin()
    {
        try
        {
            playSfxAudio("fin", false);
            for (int i = 0;i < 3; i++)
            {
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour", hiliteColour, counter)), 0.30, true, OBAnim.ANIM_LINEAR, this);
                waitForSecs(0.1f);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour", numColour, counter)), 0.30, true, OBAnim.ANIM_LINEAR, this);
            }
            waitSFX();
            waitForSecs(0.2f);
            playAudio(getAudioForScene("4k","DEMO").get(0));
            waitAudio();
            waitForSecs(0.2f);
            walkChildOut();
            waitForSecs(0.2f);
            goToCard(X_Count100_S1i.class,"event4");
        }
        catch (Exception e)
        {

        }

    }

    @Override
    public void doMainXX() throws Exception
    {
        childGrabLever();
        startScene();

    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        OBGroup machine = (OBGroup)objectDict.get("machine");
        if(status() == STATUS_AWAITING_CLICK && finger(0,2,machine.filterMembers("handle.*"),pt) != null)
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    playAudio(null);
                    childPushLever();
                    nextScene();
                }
            });
        }
    }



    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }


    public void walkChildIn() throws Exception
    {
        OBGroup childGroup= (OBGroup)objectDict.get(child);
        OBGroup machine = (OBGroup)objectDict.get("machine");
        OBGroup machineGroup = (OBGroup)machine.objectDict.get("child");
        childGroup.setRight ( 0);
        childGroup.show();
        playAudioQueued((List<Object>)(Object)Arrays.asList(getAudioForScene("sfx","step").get(0),100), false, -1);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("right",OB_Maths.worldLocationForControl(1f,0.5f,machineGroup).x,childGroup),
                OBAnim.sequenceAnim(childGroup,OBUtils.getFramesList("walk", 1, 8),0.05f,true))
                ,0.5,true,OBAnim.ANIM_LINEAR,this);
        playAudio(null);
        lockScreen();
        machineGroup.show();
        machineGroup.objectDict.get("girl").hide();
        machineGroup.objectDict.get("boy").hide();
        machineGroup.objectDict.get(child).show();
        childGroup.hide();
        unlockScreen();
    }

    public void walkChildOut() throws Exception
    {
        OBGroup childGroup = (OBGroup)objectDict.get(child);
        OBGroup machine = (OBGroup)objectDict.get("machine");
        OBGroup machineGroup = (OBGroup)machine.objectDict.get("child");
        childGroup.setRight(OB_Maths.worldLocationForControl(1f,0.5f,machineGroup).x);
        childGroup.setSequenceIndex(0);
        lockScreen();
        machineGroup.hide();
        machine.objectDict.get(child).hide();
        childGroup.show();
        unlockScreen();
        playAudioQueued((List<Object>)(Object)Arrays.asList(getAudioForScene("sfx","step").get(0),100), false, -1);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",bounds().width(),childGroup),
                OBAnim.sequenceAnim(childGroup,OBUtils.getFramesList("walk", 1, 8),0.05f,true))
                ,2.5,true,OBAnim.ANIM_LINEAR,this);
        playAudio(null);
    }

    public void childWave() throws Exception
    {
        List<String> arr = new ArrayList<>();
        arr.addAll(OBUtils.getFramesList("wave", 0, 4));
        arr.addAll(OBUtils.getFramesList("wave", 3, 2));
        arr.addAll(OBUtils.getFramesList("wave", 2, 4));
        arr.addAll(OBUtils.getFramesList("wave", 3, 2));
        arr.addAll(OBUtils.getFramesList("wave", 2, 4));
        arr.addAll(OBUtils.getFramesList("wave", 3, 2));
        arr.addAll(OBUtils.getFramesList("wave", 2, 4));
        arr.addAll(OBUtils.getFramesList("wave", 3, 0));
        animateFrames(arr,0.05f,(OBGroup)objectDict.get("machine"));
    }

    public void childPushLever() throws Exception
    {
        OBGroup machine = (OBGroup)objectDict.get("machine");
        if(OBUtils.getBooleanValue(eventAttributes.get("swap")))
        {
            childGrabLever();
            waitForSecs(0.1f);
        }

        lockScreen();
        machine.objectDict.get("smile").hide();
        machine.objectDict.get("frown").show();
        unlockScreen();

        playSfxAudio("handle1",false);
        animateFrames(OBUtils.getFramesList("handle", 4, 6),0.1f,machine);

        lockScreen();
        machine.objectDict.get("handle6").hide();
        machine.objectDict.get("handle3").show();
        machine.objectDict.get("wave0").show();
        unlockScreen();

        waitForSecs(0.1f);

        lockScreen();
        machine.objectDict.get("frown").hide();
        machine.objectDict.get("smile").show();
        unlockScreen();

        int marbleColour = OBUtils.colorFromRGBString(eventAttributes.get("col"));
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("fillColor",marbleColour,machine.objectDict.get("wheel")))
                ,0.2,true,OBAnim.ANIM_LINEAR,this);
        if(OBUtils.getBooleanValue(eventAttributes.get("fold")))
        {
            animateFrames(OBUtils.getFramesList("hands", 1, 3),0.05f,machine);
            waitForSecs(0.1f);

        }
        OBControl marble = machine.objectDict.get("marble");
        marble.setFillColor(marbleColour);
        OBControl cont = machine.objectDict.get(String.format("container%d", currentContainer));
        float bottomtarget = OB_Maths.worldLocationForControl(0f,1f,cont).y;
        for(int i=0;i<10;i++)
        {
            OBPath dropMarble = (OBPath)marble.copy();
            dropMarble.setScale(machine.scale());
            dropMarble.setPosition(OB_Maths.worldLocationForControl(0.5f,0.5f,cont));
            dropMarble.setBottom(OB_Maths.worldLocationForControl(0f,1f,machine.objectDict.get("pipeend")).y);
            dropMarble.setZPosition(-0.1f);
            attachControl(dropMarble);
            dropMarble.show();
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("bottom",bottomtarget,dropMarble)),0.4 - (0.02 *i),true,OBAnim.ANIM_EASE_IN,this);
            int currentNum = ((currentContainer -1)*10)+i+1;
            counter.setString(String.format("%d",currentNum));
            playSfxAudio("marble",true);
            waitForSecs(0.1f);
            X_Count100_Additions.playNumberAudio(currentNum,true,this);
            bottomtarget = dropMarble.top()+dropMarble.lineWidth();
        }
        playSfxAudio("end",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("fillColor", Color.WHITE,machine.objectDict.get("wheel")))
            ,0.2,true,OBAnim.ANIM_LINEAR,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("fillColor",marbleColour,machine.objectDict.get("wheel")))
                ,0.2,true,OBAnim.ANIM_LINEAR,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("fillColor",Color.WHITE,machine.objectDict.get("wheel")))
            ,0.2,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(0.2f);
        playSfxAudio("handle2",false);
        animateFrames(OBUtils.getFramesList("handle", 3, 1),0.05f,machine);
        waitForSecs(0.2f);
        if(OBUtils.getBooleanValue(eventAttributes.get("fold")))
        {
            animateFrames(OBUtils.getFramesList("hands", 3, 1),0.05f,machine);
            waitForSecs(0.2f);

        }
        if(currentContainer < 10)
        {
            playSfxAudio("pipe",false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",machine.objectDict.get(String.format("pipe%d",10-currentContainer)).left(),machine.objectDict.get("pipeend")))
                    ,0.35,true,OBAnim.ANIM_LINEAR,this);
            waitForSecs(0.2f);
            playSfxAudio("container",false);
            machine.objectDict.get(String.format("pipe%d", 10-currentContainer)).hide();
            machine.objectDict.get(String.format("container%d", currentContainer +1)).show();
            waitForSecs(0.2f);

        }
        currentContainer++;
    }

    public void childGrabLever()throws Exception
    {
        OBGroup machine = (OBGroup)objectDict.get("machine");
        animateFrames(OBUtils.getFramesList("wave", 0, 3),0.05f,machine);
        lockScreen();
        machine.objectDict.get("wave3").hide();
        machine.objectDict.get("handle1").hide();
        machine.objectDict.get("handle4").show();
        unlockScreen();
    }

    public void demo4g() throws Exception
    {
        playSfxAudio("alarm",true);
        waitForSecs(0.2f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        childWave();
        waitForSecs(0.2f);
        walkChildOut();
        if(child.equalsIgnoreCase("girl"))
            child = "boy";
        else
            child = "girl";
        waitForSecs(0.2f);
        walkChildIn();
        if(child.equalsIgnoreCase("girl"))
            playAudioScene("DEMO",2,true);
        else
            playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        startScene();
    }


    public void demo() throws Exception
    {
        waitForSecs(0.2f);
        walkChildIn();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.32f,0.8f,objectDict.get("machine").frame()),0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,counter.frame()),0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.2f,0.73f,objectDict.get("machine").frame()),0.5f,"DEMO",2,0.3f);
        thePointer.hide();
        waitForSecs(0.5f);
        startScene();
    }

}
