package org.onebillion.onecourse.mainui.oc_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 02/08/16.
 */
public class OC_Count100_S2 extends OC_SectionController
{

    int hiliteColour,numColour,numPaintColour;
    List<String> nums;
    int currentIndex;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();

        loadFingers();

        loadEvent("master2");

        events = Arrays.asList(eventAttributes.get("scenes1").split(","));

        numColour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hiliteColour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        OC_Count100_Additions.drawGrid(5,objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")),numColour,false,this);

        OC_Count100_Additions.loadNumbersAudio(this);

        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo2a();
            }
        });

    }



    @Override
    public void fin()
    {
        try
        {
            animateGridReset();
            goToCard(OC_Count100_S2j.class,"event2");
        }
        catch (Exception e)
        {

        }

    }

    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        currentIndex = 1;
        nums = Arrays.asList(eventAttributes.get("num").split(","));
        numPaintColour = getEventColourForIndex(eventAttributes.get("cindex"),this);
    }

    @Override
    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4, currentPhase(),setStatus(STATUS_AWAITING_CLICK), this);
    }

    public String currentPhase()
    {
        return String.format("%s-%d",currentEvent(), currentIndex);
    }


    public void nextPhase() throws Exception
    {
        if(currentIndex+1 > nums.size())
        {
            displayTick();
            performSel("demoFin",currentEvent());
            nextScene();
        }
        else
        {
            currentIndex++;
            startScene();
        }
    }


    public void demo2a() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.9f, 2), objectDict.get("box_48") .getWorldFrame()),0.5f,true);
        List<String> audio = getAudioForScene(currentPhase(), "DEMO");
        playAudio(audio.get(0));
        waitAudio();
        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.9f, 0.9f), objectDict.get("box_21") .getWorldFrame()),-25 ,0.5f,true);
        animateSelection(21);
        playAudio(audio.get(1));
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.5f);
        nextPhase();
    }

    public void demoCount(int startIndex) throws Exception
    {
        int i = startIndex;
        float time = 0.5f;
        for(String num : nums)
        {
            movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.9f), objectDict.get(String.format("box_%s",num)).getWorldFrame()),time,true);
            OC_Count100_Additions.playNumberAudio(Integer.valueOf(num),true,this);
            time = 0.3f;
            i++;

        }
    }

    public void demoType1(int startIndex) throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        demoCount(startIndex);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.85f, 1), objectDict.get(String.format("box_%s",nums.get(nums.size()-1))).getWorldFrame()),0.2f,true);
        playAudioScene(currentPhase(),"FINAL",startIndex+3);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.5f);
    }

    public void demoType2() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        demoCount(0);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.7f, 0.8f), objectDict.get(String.format("box_%s",nums.get(0))).getWorldFrame()),0.8f,true);
        playAudioScene(currentPhase(),"FINAL",5);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.7f, 1), objectDict.get(String.format("box_%s",nums.get(nums.size()-1))).getWorldFrame()),1f,true);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.5f);
    }

    public void demoFin2a() throws Exception
    {
        playAudioScene(currentPhase(),"FINAL",0);
        waitAudio();
        demoType1(1);
    }

    public void demoFin2b() throws Exception
    {
        demoType1(0);
    }

    public void demoFin2c() throws Exception
    {
        demoType1(0);
        animateGridReset();
        waitForSecs(0.3f);
    }

    public void demoFin2d() throws Exception
    {
        demoType2();
    }

    public void demoFin2e() throws Exception
    {
        demoType2();
    }
    public void demoFin2f() throws Exception
    {
        demoType2();
        animateGridReset();
        waitForSecs(0.3f);
    }



    public OBControl findTarget(PointF pt)
    {
        List<OBControl> arr = filterControls("box_.*");
        for(OBControl cont : arr)
        {
            if(cont.getWorldFrame().contains(pt.x,pt.y))
                return cont;
        }
        return null;
    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl obj = findTarget(pt);
            if (obj != null)
            {

                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkObject(obj);
                    }
                });

            }

        }


    }

    private void checkObject(OBControl obj) throws Exception
    {
        int targetNum = Integer.valueOf(nums.get(currentIndex-1));
        if(obj == objectDict.get(String.format("box_%d",targetNum)))
        {
            animateSelection(targetNum);
            nextPhase();

        }
        else
        {
            gotItWrongWithSfx();
            setStatus(STATUS_AWAITING_CLICK);
            long sTime = statusTime;
            waitSFX();
            if(!statusChanged(sTime))
            {
                playAudioQueuedScene(currentPhase(),"INCORRECT",0.3f,false);
            }

        }
    }

    public void animateSelection(int targetNum) throws Exception
    {
        gotItRightBigTick(false);
        OBControl obj = objectDict.get(String.format("box_%d",targetNum));
        obj.setBackgroundColor ( hiliteColour);
        waitSFX();

        OBLabel boxNum = (OBLabel)objectDict.get(String.format("num_%d",targetNum));

        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("backgroundColor",Color.WHITE,obj),OBAnim.colourAnim("colour",numPaintColour,boxNum)),0.25,true,OBAnim.ANIM_LINEAR,this);

    }

    public void animateGridReset()
    {
        List<OBAnim> arr = new ArrayList<>();

        for(OBControl con : filterControls("num_.*"))
        {
            arr.add(OBAnim.colourAnim("colour", numColour, con));
        }

        OBAnimationGroup.runAnims(arr,0.5,true,OBAnim.ANIM_LINEAR,this);

    }


    public static int getEventColourForIndex(String index, OC_SectionController controller)
    {
        Map<String,Object> map = (Map<String,Object>)(Object)controller.eventsDict.get("colours");
        Map<String,String> attrs = (Map<String,String>)(Object)map.get("attrs");

        return OBUtils.colorFromRGBString(attrs.get(String.format("col%s",index)));
    }
}
