package org.onebillion.onecourse.mainui.oc_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 23/08/16.
 */
public class OC_Count100_S3 extends OC_SectionController
{
    List<OBControl> eventObjects;
    List<OBLabel> largeNums;
    int numColour;
    int correctNum,  currentNum;
    boolean buttonMode;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();

        loadFingers();

        String startEvent = parameters.get("start");
        loadEvent(String.format("master%s",startEvent));
        buttonMode = false;
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        eventObjects = new ArrayList<>();
        largeNums = new ArrayList<>();


        numColour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        OC_Count100_Additions.drawGrid(5,this.objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")), numColour, false, false, this);

        objectDict.get("grid_box").setOpacity(0);

        setSceneXX(currentEvent());

        correctNum =  Integer.valueOf(eventAttributes.get("cnum"));

        if(startEvent.equals("3a"))
        {
            for (int i = 1; i <= correctNum; i++)
                eventObjects.get(i - 1).show();

            loadLargeNums();
        }

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                performSel("demo",currentEvent());
            }
        });

    }

    public void pointerClickButton(String name,int count, String audio) throws Exception
    {
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get(name).frame()),0.15f,true);
        playSfxAudio(name,false);
        hiliteButton(name,true);
        showObjectsNum(count);
        waitForSecs(0.2f);
        hiliteButton(name,false);
        movePointerToPoint(OB_Maths.locationForRect(1.2f,0.5f,objectDict.get(name).frame()),0.15f,true);
        waitSFX();
        playAudio(audio);
        waitAudio();
        waitForSecs(0.4f);
    }


    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(OBUtils.getBooleanValue(eventAttributes.get("redraw")))
        {

            for(OBControl  con : eventObjects)
            {
                detachControl(con);
            }

            eventObjects.clear();

            for(int i=1; i<51; i++)
            {
                OBControl cont =  objectDict.get("obj").copy();
                cont.texturise(true,this);
                cont.setZPosition(0.5f);
                cont.setPosition ( objectDict.get(String.format("box_%d",i)).getWorldPosition());
                eventObjects.add(cont);
                attachControl(cont);
            }
            correctNum = 0;
        }

        if(OBUtils.getBooleanValue(eventAttributes.get("button")))
        {
            buttonMode = true;
            showControls("button.*");
            for(OBControl con : largeNums)
                con.hide();
        }
    }


    @Override
    public void doMainXX() throws Exception
    {
        int lastNum = correctNum;
        correctNum = Integer.valueOf(eventAttributes.get("cnum"));
        currentNum = 0;
        if(buttonMode == false)
        {
            while(lastNum < correctNum)
            {
                lockScreen();

                for(int i= lastNum%10 +1; i<=10; i++)
                {
                    if(lastNum+i-lastNum%10 <= correctNum)
                    {
                        eventObjects.get(lastNum+i-1-lastNum%10).show();
                    }
                    else
                    {
                        break;
                    }
                }

                if(lastNum+10-lastNum%10 >= correctNum)
                {
                    loadLargeNums();
                }


                unlockScreen();
                lastNum = lastNum +10-lastNum%10;

                playSfxAudio("show",true);
                waitForSecs(0.5f);
            }
        }

        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }


    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {

            if(buttonMode == false)
            {

                final OBControl target  = finger(0,2,(List<OBControl>)(Object)largeNums,pt);
                if(target != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkNum((OBLabel)target);
                        }
                    });

                }
            }
            else
            {

                if(finger(0,0, Collections.singletonList(objectDict.get("button1")),pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkButton1();
                        }
                    });


                }
                else if(finger(0,0,Collections.singletonList(objectDict.get("button2")),pt) != null)
                {
                    setStatus(STATUS_BUSY);

                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkButton2();
                        }
                    });

                }

            }
        }
    }

    private void checkButton2() throws Exception
    {
        hiliteButton("button2",true);
        if(currentNum + 10 > correctNum)
        {
            gotItRight();
            playSfxAudio("button2",false);
            showObjectsNum(currentNum + 1);
            waitSFX();
            hiliteButton("button2",false);
            playAudioScene("FINAL2",(currentNum%10)-1,false);
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            hiliteButton("button2",false);
            if(currentNum == 0){
                playAudioQueuedScene("INCORRECT",0.3f,false);
            }else{
                playAudioQueuedScene("INCORRECT3",0.3f,false);
            }
        }
        checkButtonFin();
    }

    private void checkButton1() throws Exception
    {
        hiliteButton("button1",true);
        if(currentNum + 10 <= correctNum)
        {
            gotItRight();
            playSfxAudio("button1",false);
            showObjectsNum(currentNum + 10);
            waitSFX();
            hiliteButton("button1",false);
            playAudioScene("FINAL",(currentNum/10)-1,false);
        }else{
            gotItWrongWithSfx();
            waitSFX();
            hiliteButton("button1",false);
            playAudioQueuedScene("INCORRECT2",0.3f,false);
        }
        checkButtonFin();
    }

    private void checkButtonFin() throws Exception
    {
        if(currentNum == correctNum)
        {
            waitAudio();
            displayTick();
            resetButtons();
            playAudioQueuedScene("FINAL3",0.3f,true);
            waitForSecs(0.3f);
            animateGrid();

            if(events.get(events.size()-1) != currentEvent())
            {
                popAllObjects();
            }
            nextScene();

        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    private void checkNum(OBLabel target) throws Exception
    {
        target.setColour(Color.RED);
        if((boolean)target.propertyValue("correct"))
        {
            List<OBAnim> anims = new ArrayList<>();
            for(OBControl con : largeNums)
            {
                if(con != target)
                    anims.add(OBAnim.opacityAnim(0.3f,con));
            }

            OBAnimationGroup.runAnims(anims,0.1,true,OBAnim.ANIM_LINEAR,this);
            waitForSecs(0.2f);

            gotItRightBigTick(true);
            playAudioQueuedScene("FINAL",0.3f,true);
            waitForSecs(0.3f);
            animateGrid();
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            target.setColour(numColour);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);


        }
    }


    public void loadLargeNums()
    {
        for(OBControl  con : largeNums)
        {
            detachControl(con);
        }

        largeNums.clear();


        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(80);

        int index = 1;

        for(String num : eventAttributes.get("nums").split(","))
        {
            OBLabel label = new OBLabel(num,font,fontSize);
            label.setColour(numColour);
            label.setPosition(OB_Maths.locationForRect(new PointF(0.25f*index, 0.4f), objectDict.get("bottombar") .frame()));
            label.setZPosition(1.5f);


            attachControl(label);
            largeNums.add(label);
            label.setProperty("correct",num.equalsIgnoreCase(eventAttributes.get("cnum")));

            index++;
        }
    }


    public void popAllObjects() throws Exception
    {
        lockScreen();
        for(int i=1;i<=correctNum;i++)
        {
            eventObjects.get(i-1).hide();
        }

        unlockScreen();
        playSfxAudio("pop",true);
        waitForSecs(0.3f);
    }

    public void resetButtons()
    {
        hiliteButton("button1",false);
        hiliteButton("button2",false);
    }


    public void showObjectsNum(int num)
    {
        lockScreen();
        for(int i=currentNum+1; i<=num; i++)
        {
            eventObjects.get(i-1).show();
        }
        unlockScreen();
        currentNum = num;
    }

    public void hiliteButton(String name,boolean state)
    {
        OBGroup button = (OBGroup)objectDict.get(name);
        if(state)
        {
            button.objectDict.get("highlight").show();
        }
        else
        {
            button.objectDict.get("highlight").hide();
        }
    }


    public void animateGrid() throws Exception
    {
        List<OBAnim> anim1 = new ArrayList<>();
        List<OBAnim> anim2 = new ArrayList<>();


        hideControls("box_.*");
        hideControls("num_.*");
        for(int i = 1; i<=correctNum; i++)
        {
            objectDict.get(String.format("num_%d",i)).show();
            objectDict.get(String.format("box_%d",i)).show();
        }
        anim1.add(OBAnim.opacityAnim(1,objectDict.get("grid_box")));
        anim2.add(OBAnim.opacityAnim(0,objectDict.get("grid_box")));
        playSfxAudio("grid1",false);
        OBAnimationGroup.runAnims(anim1,0.6,true,OBAnim.ANIM_LINEAR,this);
        waitSFX();
        waitForSecs(1.2f);
        playSfxAudio("grid2",false);
        OBAnimationGroup.runAnims(anim2,0.6,true,OBAnim.ANIM_LINEAR,this);
        waitSFX();
        waitForSecs(1f);

    }


    public void demo3a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.2f,1.5f,eventObjects.get(4).frame()),-30,0.5f,"DEMO",0, 0.3f);

        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);

        moveScenePointer(OB_Maths.locationForRect(0.9f,0.5f,objectDict.get("bottombar").frame()),-30,0.5f,"DEMO",2, 0.5f);

        thePointer.hide();
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }


    public void demo3j() throws Exception
    {
        waitForSecs(0.4f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.5f,objectDict.get("button1").frame()),-30,0.5f,"DEMO",1,0.3f);

        List<String> audios = getAudioForScene(currentEvent(),"DEMO");
        pointerClickButton("button1",10,audios.get(2));
        pointerClickButton("button1",20,audios.get(3));

        moveScenePointer(OB_Maths.locationForRect(1.2f,0.5f,objectDict.get("button2").frame()),-30,0.5f,"DEMO",4,0.3f);


        pointerClickButton("button2",21,audios.get(5));
        pointerClickButton("button2",22,audios.get(6));
        pointerClickButton("button2",23,audios.get(7));

        playAudioScene("DEMO",8,true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        correctNum = 23;
        popAllObjects();
        nextScene();

    }
}
