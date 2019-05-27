package com.maq.xprize.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.oc_count100.OC_Count100_Additions;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 06/04/2017.
 */

public class OC_MoreNumbers_S2 extends OC_SectionController
{
    int textcolour, hilitecolour, textcolour2, numcolour, maskcolour;
    int currentPhase;
    List<OBControl> targetMasks;
    List<Integer> targetNums, hiliteNums;
    boolean firstHand;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        textcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        textcolour2 = OBUtils.colorFromRGBString(eventAttributes.get("textcolour2"));
        OC_Count100_Additions.drawGrid(10, objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")), textcolour, false, this);
        hiliteNums = new ArrayList<>();
        targetNums = new ArrayList<>();
        targetMasks = new ArrayList<>();
        hideControls("num_.*");
        hideControls("box_.*");
        firstHand = true;
        setSceneXX(currentEvent());
    }

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


    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        targetMasks.clear();
        targetNums.clear();
        hiliteNums.clear();
        currentPhase = 0;

        targetNums = OBMisc.stringToIntegerList(eventAttributes.get("num"), ",");
        hiliteNums = OBMisc.stringToIntegerList(eventAttributes.get("hilitenum"), ",");
        if(eventAttributes.get("numcolour")  != null)
            numcolour = OBUtils.colorFromRGBString(eventAttributes.get("numcolour"));
        if(eventAttributes.get("maskcolour")  != null)
            maskcolour = OBUtils.colorFromRGBString(eventAttributes.get("maskcolour"));
        if(eventAttributes.get("target").equals("box"))
        {
            currentPhase = 1;
        }

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(eventAttributes.get("target").equals("box"))
        {
            if(status() == STATUS_AWAITING_CLICK)
            {
                OBControl cont  = null;
                for(OBControl box : filterControls("box_.*"))
                {
                    if (box.getWorldFrame().contains(pt.x, pt.y))
                    {
                        cont = box;
                        break;

                    }

                }
                if(cont != null && cont.isEnabled())
                {
                    setStatus(STATUS_BUSY);
                    final OBControl box  = cont;
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkBox(box);
                        }
                    });
                }

            }

        }
        else if(eventAttributes.get("target").equals("mask"))
        {
            if(status() == STATUS_AWAITING_CLICK)
            {
                final OBControl mask = finger(0,1,targetMasks,pt);
                if(mask != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkMask(mask);
                        }
                    });

                }

            }

        }
    }

    public void checkBox(OBControl cont) throws Exception
    {
        hiliteBox((int)cont.propertyValue("num_value"), Color.RED, false);
        if(cont == objectDict.get(String.format("box_%s", targetNums.get(currentPhase-1))))
        {
            gotItRightBigTick(true);
            if(firstHand)
            {
                loadPointer(POINTER_MIDDLE);
                movePointerToPoint(OB_Maths.locationForRect(0.5f,1,objectDict.get(String.format("box_%s", hiliteNums.get(currentPhase-1))).getWorldFrame())
                        ,0.6f,true);

            }
            playAudioQueuedScene(getPhase(),"FINAL", 0.3f,true);
            waitForSecs(0.1f);
            hiliteBox(hiliteNums.get(currentPhase-1), textcolour2, true);
            waitForSecs(0.2f);
            if(firstHand)
            {
                firstHand = false;
                waitForSecs(0.3f);
                thePointer.hide();
            }
            nextPhase();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            lockScreen();
            unhiliteBoxes(Arrays.asList((int)cont.propertyValue("num_value")));
            unlockScreen();
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene(getPhase(),"INCORRECT", 0.3f,false);
        }
    }

    public void checkMask(OBControl mask) throws Exception
    {
        mask.setBackgroundColor(OBUtils.highlightedColour(maskcolour));
        if((boolean)mask.propertyValue("correct"))
        {
            peelMask(mask,0.4f);
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            hiliteBox(hiliteNums.get(0),textcolour2,true);
            waitForSecs(2f);
            if(currentEvent() != events.get(events.size()-1))
            {
                lockScreen();
                unhiliteBoxes(Arrays.asList(targetNums.get(0),hiliteNums.get(0)));
                for(OBControl cont : targetMasks)
                    detachControl(cont);

                unlockScreen();
            }
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            mask.setBackgroundColor ( maskcolour);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }


    public void peelMask(OBControl mask,float duration) throws Exception
    {
        playSfxAudio("peel",false);
        mask.setAnchorPoint(new PointF(1, 0.5f));
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("width",0,mask)),duration,true,OBAnim.ANIM_EASE_OUT,this);
        waitSFX();

    }

    public void startScene() throws Exception
    {
        if(eventAttributes.get("target").equals("box"))
        {
            OBMisc.doSceneAudio(-1,getPhase(),setStatus(STATUS_AWAITING_CLICK),this);

        }
        else if(eventAttributes.get("target").equals("mask"))
        {
            createMasks();
            OBMisc.doSceneAudio(-1,setStatus(STATUS_AWAITING_CLICK),this);
        }
    }

    public void createMasks() throws Exception
    {
        lockScreen();
        for(int maskNum : targetNums)
        {
            OBControl newMask = new OBControl();
            newMask.setProperty("correct",false);
            if(maskNum == targetNums.get(0))
            {
                ((OBLabel)objectDict.get(String.format("num_%d",maskNum))).setColour(Color.RED);
                objectDict.get(String.format("box_%d",maskNum)).setBackgroundColor(hilitecolour);
                newMask.setProperty("correct",true);
                newMask.setProperty("value",maskNum);

            }
            OBControl alingBox = objectDict.get(String.format("box_%d", maskNum));
            RectF frame = new RectF(alingBox.getWorldFrame());
            frame.inset(alingBox.borderWidth/2.0f, alingBox.borderWidth/2.0f);
            newMask.setFrame(frame);
            newMask.setFillColor(maskcolour);
            newMask.setZPosition(50);
            targetMasks.add(newMask);
            attachControl(newMask);
        }
        unlockScreen();
        playSfxAudio("cover_on",true);
    }

    public String getPhase()
    {
        return String.format("%s%d", currentEvent(), currentPhase);
    }

    public void nextPhase() throws Exception
    {
        currentPhase++;
        if(currentPhase > targetNums.size())
        {
            waitForSecs(1f);
            lockScreen();
            unhiliteBoxes(targetNums);
            unhiliteBoxes(hiliteNums);

            unlockScreen();
            waitForSecs(0.2f);
            nextScene();

        }
        else
        {
            startScene();
        }
    }

    public void hiliteBox(int num, int colour, boolean audio) throws Exception
    {
        lockScreen();
        OBControl cont = objectDict.get(String.format("box_%d", num));
        cont.setBackgroundColor ( hilitecolour);
        cont.disable();
        ((OBLabel)objectDict.get(String.format("num_%d", num))).setColour(colour);

        unlockScreen();
        if(audio)
            playSfxAudio("num_mark",true);
    }

    public void unhiliteBoxes(List<Integer> nums)
    {
        for(int num : nums)
        {
            OBControl cont = objectDict.get(String.format("box_%d", num));
            cont.setBackgroundColor(Color.WHITE);
            cont.enable();
            ((OBLabel)objectDict.get(String.format("num_%s", num))).setColour(textcolour);
        }
    }

    public void demo2a() throws Exception
    {
        waitForSecs(0.4f);
        lockScreen();
        showControls("box_.*");
        playSfxAudio("grid_appears",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.4f);
        for(int i=1; i<=10; i++)
        {
            lockScreen();
            for(int j=0; j<10; j++)
            {
                objectDict.get(String.format("num_%d",i+j*10)).show();

            }
            playSFX(String.format("note_%d",i));

            unlockScreen();
            waitForSecs(0.35f);
            waitSFX();

        }
        loadPointer(POINTER_MIDDLE);
        demoPointerBox();
        waitForSecs(0.5f);
        thePointer.hide();
        nextPhase();

    }

    public void demoPointerBox() throws Exception
    {
        List<String> demoAudio = getAudioForScene(getPhase(),"DEMO");

        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_45").getWorldFrame()),0.5f,demoAudio.get(0),0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.75f,1f,objectDict.get(String.format("box_%d",hiliteNums.get(0))).getWorldFrame()),0.5f,demoAudio.get(1),0f);
        hiliteBox(hiliteNums.get(0),textcolour2,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.75f,1f,objectDict.get(String.format("box_%d",targetNums.get(0))).getWorldFrame()),0.5f,demoAudio.get(2),0f);
        hiliteBox(targetNums.get(0),Color.RED,true);
    }

    public void demo2f() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        demoPointerBox();
        waitForSecs(0.5f);
        thePointer.hide();
        nextPhase();

    }
    public void demo2j() throws Exception
    {
        loadPointer(POINTER_LEFT);
        demoPointerBox();
        waitForSecs(0.3f);
        List<String> demoAudio = getAudioForScene(getPhase(),"DEMO");
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get(String.format("box_%s",targetNums.get(0))).getWorldFrame()),0.5f,demoAudio.get(3),0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,objectDict.get(String.format("box_%s",targetNums.get(0))).getWorldFrame()),0.5f,demoAudio.get(4),0.5f);
        thePointer.hide();
        nextPhase();

    }
    public void demo2n() throws Exception
    {
        loadPointer(POINTER_LEFT);
        demoPointerBox();
        waitForSecs(0.3f);
        List<String> demoAudio = getAudioForScene(getPhase(),"DEMO");
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get(String.format("box_%s",targetNums.get(0))).getWorldFrame()),0.5f,demoAudio.get(3),0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,objectDict.get(String.format("box_%s",hiliteNums.get(0))).getWorldFrame()),0.5f,demoAudio.get(4),0.5f);
        thePointer.hide();
        nextPhase();

    }

    public void demo2q() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_45").getWorldFrame()),0,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(2f,1f,objectDict.get("box_45").getWorldFrame()),0,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        waitForSecs(0.4f);
        startScene();

    }



}
