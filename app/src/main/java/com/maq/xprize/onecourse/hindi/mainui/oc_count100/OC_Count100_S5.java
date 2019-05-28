package com.maq.xprize.onecourse.hindi.mainui.oc_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 27/06/16.
 */
public class OC_Count100_S5 extends OC_SectionController
{

    int hilitecolour, numcolour;
    int correctNum, currentBoxIndex;
    List<OBControl> targetBoxes, targetNums, targetMasks, prevTargetMasks;
    OBControl rowBorder;
    boolean maskMode;


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master5");

        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);

        numcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));

        OC_Count100_Additions.drawGrid(10,objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")),numcolour,false,this);
        hideControls("num_.*");
        hideControls("box_.*");

        targetBoxes = new ArrayList<OBControl>();
        targetNums =  new ArrayList<OBControl>();
        targetMasks =  new ArrayList<OBControl>();

        OC_Count100_Additions.loadNumbersAudio(this);
        rowBorder = new OBControl();
        rowBorder.setBorderWidth(objectDict.get("box_1").borderWidth);
        rowBorder.setBorderColor(Color.RED);
        rowBorder.hide();
        rowBorder.setZPosition(2);
        attachControl(rowBorder);

        maskMode = false;

        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                demo();
            }
        });

    }


    public void demo() throws Exception
    {
        waitForSecs(0.4f);

        lockScreen();
        showControls("box_.*");
        unlockScreen();

        playSfxAudio("grid",true);
        waitForSecs(0.4f);

        playSfxAudio("numbers",false);
        for(OBControl num : OBUtils.randomlySortedArray(filterControls("num_.*")))
        {
            lockScreen();
            num.show();
            unlockScreen();
            waitForSecs(0.025f);
        }
        waitSFX();

        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()),0.5f,"DEMO",0,0.3f);

        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,0.5f,objectDict.get("box_1")),0.5f,true);
        objectDict.get("box_1").setBackgroundColor(hilitecolour);
        playAudioScene(currentEvent(),"DEMO",1);
        waitAudio();
        waitForSecs(0.2f);
        objectDict.get("box_1").setBackgroundColor(Color.WHITE);

        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,0.5f,objectDict.get("box_100")),0.8f,true);
        objectDict.get("box_100").setBackgroundColor(hilitecolour);
        playAudioScene(currentEvent(),"DEMO",2);
        waitAudio();
        waitForSecs(0.2f);
        objectDict.get("box_100").setBackgroundColor(Color.WHITE);

        demoRow(1,"DEMO",3);
        demoRow(5,"DEMO",4);
        demoRow(8,"DEMO",5);

        waitForSecs(0.4f);
        thePointer.hide();
        startScene();
    }


    public void demo5e() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        doMasks();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,0.5f,objectDict.get("box_13")) ,0.5f,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",false);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,0.5f,objectDict.get("box_35")) ,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,0.5f,objectDict.get("box_68")) ,0.5f,true);
        waitAudio();
        waitForSecs(0.4f);
        thePointer.hide();
        startScene();

    }


    public void demo5k() throws Exception
    {

        doMasks();
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_MIDDLE);

        OBControl corMask = new OBControl();
        for(OBControl mask : targetMasks)
        {
            if((boolean)mask.propertyValue("correct"))
            {
                corMask = mask;
                break;
            }
        }

        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,corMask.frame()) ,0.6f,true);
        corMask.setBackgroundColor(OBUtils.highlightedColour(corMask.backgroundColor()));

        peelMask(corMask,0.2f);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_25")) ,0.2f,true);
        playAudioScene("DEMO",2,true);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_24")) ,0.3f,true);
        completeSingleNum();
        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(0.2f);
        nextScene();

    }


    public void demoFin5l() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_11")) ,0.6f,true);
        playAudioScene("FINAL",0,true);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_12")) ,0.3f,true);
        completeSingleNum();
        waitForSecs(0.4f);
        thePointer.hide();
    }

    public void demoFin5m()  throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_43")) ,0.6f,true);
        playAudioScene("FINAL",0,true);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_42")) ,0.3f,true);
        completeSingleNum();
        waitForSecs(0.4f);
        thePointer.hide();
    }


    public void demoFin5n()  throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_57")) ,0.6f,true);
        playAudioScene("FINAL",0,true);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,1f,objectDict.get("box_58")) ,0.3f,true);
        completeSingleNum();
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.worldLocationForControl(1f,0.7f,objectDict.get("box_69")) ,0.5f,true);
        playAudioScene("FINAL",1,true);
        waitForSecs(0.4f);
        thePointer.hide();
    }

    public void demoRow(int row, String audio, int index)  throws Exception
    {
        OBControl firstBox = objectDict.get(String.format("box_%d", 1 + (row-1)*10));
        OBControl lastBox = objectDict.get(String.format("box_%d", row*10));

        movePointerToPoint(OB_Maths.worldLocationForControl(0.5f,0.5f,firstBox),0.5f,true);
        waitForSecs(0.2f);
        playAudioScene(audio,index,false);
        movePointerToPoint(OB_Maths.worldLocationForControl(0.25f,0.5f,lastBox),1f,false);


        int lastRow = 1;
        while(lastRow <= 10)
        {
            OBControl nextControl = objectDict.get(String.format("box_%d", lastRow + (row-1)*10));
            if(convertRectFromControl(nextControl.frame(),nextControl.parent).left < thePointer.position().x)
            {
                nextControl.setBackgroundColor(hilitecolour);
                lastRow++;
            }
        }

        waitAudio();
        waitForSecs(0.4f);
        List<OBAnim> resetAnim = new ArrayList<>();
        for(int i = 1; i<=10; i++)
        {
            OBControl cBox = objectDict.get(String.format("box_%d", i + (row-1)*10 ));
            resetAnim.add(OBAnim.colourAnim("backgroundColor",Color.WHITE,cBox));
        }

        OBAnimationGroup.runAnims(resetAnim,0.4,true,OBAnim.ANIM_LINEAR,this);

    }


    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        correctNum = Integer.valueOf(eventAttributes.get("correct"));

        if(eventAttributes.get("single") == null )
        {
            targetBoxes.clear();
            targetNums.clear();
            for(int i=1; i<=10; i++)
            {
                targetBoxes.add(objectDict.get(String.format("box_%d", i + (correctNum - 1)*10)));
                targetNums.add(objectDict.get(String.format("num_%d", i + (correctNum - 1)*10)));
            }

            RectF rect = new RectF(convertRectFromControl(targetBoxes.get(0).frame(),targetBoxes.get(0).parent));
            rect.union(convertRectFromControl(targetBoxes.get(targetBoxes.size()-1).frame(),targetBoxes.get(targetBoxes.size()-1).parent));
            rowBorder.setFrame(rect);
            currentBoxIndex = 0;
        }

        if(eventAttributes.get("mask") != null)
        {
            maskMode = true;

            prevTargetMasks = new ArrayList<>(targetMasks);

            targetMasks.clear();

            String[] maskNums = eventAttributes.get("mask").split(",");
            int col = OBUtils.colorFromRGBString(eventAttributes.get("maskcol"));

            if (eventAttributes.get("single") == null)
            {

                RectF maskRect = new RectF(rowBorder.frame());
                maskRect.inset(rowBorder.borderWidth / 2, rowBorder.borderWidth / 2);
                for (String maskNum : maskNums)
                {
                    OBControl newMask = new OBControl();
                    int maskNumInt = Integer.valueOf(maskNum);
                    if (maskNumInt == correctNum)
                        newMask.setProperty("correct", true);

                    newMask.setFrame(maskRect);
                    newMask.setBackgroundColor(col);

                    OBControl alignBox = objectDict.get(String.format("box_%d", 1 + (maskNumInt - 1) * 10));
                    RectF alignRect = alignBox.getWorldFrame();
                    newMask.setLeft(alignRect.left + alignBox.borderWidth / 2);
                    newMask.setTop(alignRect.top + alignBox.borderWidth / 2);
                    targetMasks.add(newMask);
                    newMask.setZPosition(10);
                    newMask.hide();
                    attachControl(newMask);
                }
            }
            else
            {
                for (String maskNum : maskNums)
                {
                    OBControl newMask = new OBControl();
                    int maskNumInt = Integer.valueOf(maskNum);
                    if (maskNumInt == correctNum)
                    {
                        newMask.setProperty("correct", true);
                    }

                    OBControl alignBox = objectDict.get(String.format("box_%d", maskNumInt));
                    RectF maskRect = convertRectFromControl(alignBox.frame(),alignBox.parent);
                    maskRect.inset(alignBox.borderWidth / 2.0f, alignBox.borderWidth / 2.0f);
                    newMask.setBackgroundColor(col);
                    newMask.setZPosition(10);
                    newMask.setFrame(maskRect);
                    targetMasks.add(newMask);
                    newMask.hide();
                    attachControl(newMask);
                }

            }
        }

    }

    public void doMasks() throws Exception
    {
        if(maskMode)
        {
            lockScreen();

            for (OBControl mask : prevTargetMasks)
                detachControl(mask);

            for(OBControl con : targetMasks)
                con.show();

            if (eventAttributes.get("single") != null)
            {
                OBLabel label = (OBLabel) objectDict.get(String.format("num_%d", correctNum));
                label.setColour(Color.RED);
            }


            unlockScreen();

            playSfxAudio("mask",true);

        }

    }

    public void doMainXX() throws Exception
    {
        doMasks();
        startScene();
    }

    public void startScene() throws Exception
    {

        setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REPEAT"),300));
        final long time = setStatus(maskMode ? STATUS_AWAITING_CLICK : STATUS_WAITING_FOR_DRAG);
        playAudioQueuedScene("PROMPT",true);

        reprompt(time, OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"PROMPT2"),300),4);
    }


    public void peelMask(OBControl mask, float duration) throws Exception
    {
        playSfxAudio("peel",false);
        mask.setAnchorPoint(1, 0.5f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("width",0,mask)),duration,true,OBAnim.ANIM_EASE_OUT,this);
        waitSFX();
    }

    public void setSecondAudio(long startStatusTime)
    {
        Map<String,Object> audio = (Map<String,Object>)audioScenes.get(currentEvent());
        setReplayAudio(OBUtils.insertAudioInterval(audio.get("REPEAT2"),300));
        reprompt(startStatusTime, OBUtils.insertAudioInterval(audio.get("PROMPT2"),300),4);
    }

    public void animateGridReset() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();

        for(OBControl lab : filterControls("num_.*"))
        {
            if(((OBLabel)lab).colour() != numcolour)
            {
                anims.add(OBAnim.colourAnim("colour",numcolour,(OBLabel)lab));
            }
        }

        for(OBControl mask : targetMasks)
            anims.add(OBAnim.opacityAnim(0,mask));

        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(0.5f);
    }

    public void rowTick() throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.2f);
        playSfxAudio("border",false);
        rowBorder.show();
        waitSFX();
        waitForSecs(0.2f);
    }

    public void completeRow() throws Exception
    {
        List<String> aud = getAudioForScene(currentEvent(),"FINAL");

        if(aud == null)
             waitForSecs(0.3f);
        else
            playAudioQueued(OBUtils.insertAudioInterval(aud,300));

        int col = OBUtils.colorFromRGBString(eventAttributes.get("numcol"));
        List<OBAnim> anims = new ArrayList<>();
        int index =  1;
        for(OBControl num : targetNums)
        {
            OC_Count100_Additions.playNumberAudio(index+(correctNum-1)*10,false,this);
            index++;
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(1.1f,num),OBAnim.colourAnim("colour",Color.RED,num)),0.2f,true,OBAnim.ANIM_LINEAR,this);

            anims.add(OBAnim.scaleAnim(1,num));
            anims.add(OBAnim.colourAnim("colour",col,num));
            waitAudio();
        }

        for(OBControl box : targetBoxes)
        {
            anims.add(OBAnim.colourAnim("backgroundColor",Color.WHITE,box));
        }
        anims.add(OBAnim.opacityAnim(0,rowBorder));

        waitForSecs(1f);
        OBAnimationGroup.runAnims(anims ,0.5,true,OBAnim.ANIM_LINEAR,this);

        rowBorder.hide();
        rowBorder.setOpacity(1);

        if(OBUtils.getBooleanValue(eventAttributes.get("reset")))
        {
            waitForSecs(1f);
            animateGridReset();
        }

        nextScene();
    }

    public void completeSingleNum() throws Exception
    {
        waitForSecs(0.4f);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.colourAnim("colour",
                OBUtils.colorFromRGBString(eventAttributes.get("numcol")),
                objectDict.get(String.format("num_%d", correctNum)))),
                0.25f,true,OBAnim.ANIM_LINEAR,this);
    }


    public OBControl findRowTarget(PointF pt, int max)
    {
        List<OBControl> targets = targetBoxes.subList(0,max+1);
        OBControl c = finger(0,1,targets,pt);
        return c;
    }

    public OBControl findMaskTarget(PointF pt)
    {
        OBControl c = finger(0,1,targetMasks,pt);
        return c;
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {


        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            setStatus(STATUS_BUSY);
            final OBControl obj = findRowTarget(pt,currentBoxIndex);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {

                @Override
                public void run() throws Exception
                {
                    if (obj != null)
                    {
                        if(checkBoxPt(pt))
                        {
                            rowTick();
                            completeRow();
                        }
                        else
                        {
                            setStatus(STATUS_DRAGGING);
                        }

                    }
                    else
                    {
                        gotItWrongWithSfx();
                        long interval =  setStatus(STATUS_WAITING_FOR_DRAG);
                        waitSFX();
                        if(currentBoxIndex == 0)
                            playAudioQueuedScene("INCORRECT",false);
                        else
                            setSecondAudio(interval);
                    }
                }
            });


        }
        else if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl obj = findMaskTarget(pt);
            if (obj != null)
            {
                setStatus(STATUS_BUSY);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {

                    @Override
                    public void run() throws Exception
                    {
                        int normalColour = obj.backgroundColor();
                        obj.setBackgroundColor(OBUtils.highlightedColour(normalColour));


                        if(obj.propertyValue("correct") != null &&
                                (boolean)obj.propertyValue("correct"))
                        {
                            if(eventAttributes.get("single") == null)
                            {
                                rowTick();
                                peelMask(obj, 0.4f);
                                waitForSecs(0.3f);
                                completeRow();
                            }
                            else
                            {
                                gotItRightBigTick(true);
                                peelMask(obj, 0.2f);
                                if(!performSel("demoFin",currentEvent()))
                                {
                                    completeSingleNum();
                                }
                                nextScene();
                            }
                        }
                        else
                        {
                            gotItWrongWithSfx();
                            waitSFX();
                            obj.setBackgroundColor(normalColour);
                            setStatus(STATUS_AWAITING_CLICK);
                            playAudioQueuedScene("INCORRECT",false);
                        }
                    }
                });


            }

        }



    }


    @Override
    public void touchUpAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    setSecondAudio(setStatus(STATUS_WAITING_FOR_DRAG));
                }
            });

        }

    }

    @Override
    public void touchMovedToPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_BUSY);
            OBControl obj = findRowTarget(pt,9);
            if (obj != null)
            {
                if(checkBoxPt(pt))
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            rowTick();
                            completeRow();

                        }
                    });

                }else
                {
                    setStatus(STATUS_DRAGGING);
                }
            }
            else
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        gotItWrongWithSfx();
                        long interval =  setStatus(STATUS_WAITING_FOR_DRAG);
                        waitSFX();
                        setSecondAudio(interval);

                    }
                });

            }
        }
    }


    public boolean checkBoxPt(PointF pt)
    {
        OBControl box = targetBoxes.get(currentBoxIndex);
        if(currentBoxIndex == 0)
        {
            box.setBackgroundColor(hilitecolour);
        }

        if(pt.x > convertRectFromControl(box.frame(),box.parent).right)
        {
            currentBoxIndex++;
            if(currentBoxIndex< targetBoxes.size())
                targetBoxes.get(currentBoxIndex).setBackgroundColor(hilitecolour);
        }

        if(currentBoxIndex >=9){
            return true;
        }else{
            return false;
        }

    }

}
