package com.maq.xprize.onecourse.mainui.oc_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 04/08/16.
 */
public class OC_Count100_S2j extends OC_SectionController
{
    List<String> numList, indexList;
    List<OBControl> dragTargets;
    int numColour,hiliteColour;
    int curCount,currentIndex;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();

        loadFingers();

        loadEvent("master2");

        events = Arrays.asList(eventAttributes.get("scenes2").split(","));
        numColour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hiliteColour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));

        OC_Count100_Additions.drawGrid(5,objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")),numColour,false,this);

        OC_Count100_Additions.loadNumbersAudio(this);

        dragTargets = new ArrayList<>();
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo();
            }
        });
    }

    public void showCovers() throws Exception
    {
        lockScreen();
        showControls("cover.*");
        unlockScreen();
        playSfxAudio("cover",true);
    }

    @Override
    public void doMainXX()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(eventAttributes.get("counting") == null)
                {
                    showCovers();
                    performSel("demoStart",currentEvent());
                    startScene();
                }
                else
                {
                    performSel("demoStart",currentEvent());
                    setReplayAudioScene(currentEvent(),"REPEAT");
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
        });
    }

    @Override
    public void setSceneXX(String  scene)
    {
        deleteControls("cover.*");
        if(eventAttributes.get("counting") == null)
        {
            lockScreen();

            super.setSceneXX(scene);
            for(OBControl control : filterControls("cover.*"))
            {
                String[] nums = ((String)control.attributes().get("num")).split(",");
                OBControl firstBox = objectDict.get(String.format("box_%s",nums[0]));
                OBControl lastBox = objectDict.get(String.format("box_%s",nums[nums.length-1]));
                RectF frm = new RectF(firstBox.getWorldFrame());
                frm.union(lastBox.getWorldFrame());

                control.setWidth(frm.width());
                control.setHeight(frm.height());
                control.setFrame(frm);
                control.show();

                OBGroup gr = new OBGroup(Collections.singletonList(control));

                frm.inset(firstBox.borderWidth/2.0f, firstBox.borderWidth/2.0f);
                gr.setFrame(frm);
                control.setTop(-firstBox.borderWidth/2.0f);
                control.setLeft(-firstBox.borderWidth/2.0f);
                gr.setMasksToBounds(true);
                gr.setZPosition(10);
                gr.settings = control.settings;

                objectDict.put((String)control.attributes().get("id"),gr);
                attachControl(gr);
                gr.hide();

            }
            unlockScreen();

        }

        currentIndex = 1;
    }

    @Override
    public void touchDownAtPoint(final PointF pt,View v)
    {

        if (status() == STATUS_WAITING_FOR_DRAG)
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
                        checkTargetDrag(obj,pt);
                    }
                });


            }

        }else if (status() == STATUS_AWAITING_CLICK)
        {
            OBControl c =finger(0,0,Arrays.asList(objectDict.get(String.format("box_%d",currentIndex))),pt);
            if(c != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTargetClick();
                    }
                });

            }
        }



    }

    @Override
    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }


    @Override
    public void touchUpAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING){
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop();
                }
            });


        }

    }


    public void nextPhase() throws Exception
    {

        if(currentIndex >= 4)
        {
            displayTick();
            waitForSecs(0.4f);
            animateGridReset();
            waitForSecs(0.3f);
            nextScene();
        }
        else
        {
            currentIndex++;
            setUpDragNum(currentPhase());
            startScene();
        }
    }

    public void setUpDragNum(String event) throws Exception
    {
        lockScreen();
        curCount= 0;
        Map<String,Object> eventData = (Map<String, Object>) eventsDict.get(event);
        Map<String, Object> attrs = (Map<String, Object>) eventData.get("attrs");

        indexList = Arrays.asList( ((String)attrs.get("index")).split(","));

        float posY = objectDict.get("workrect").bottom() + (bounds().height() - objectDict.get("workrect").bottom())/2;

        int count = 1;
        for(String index : OBUtils.randomlySortedArray(indexList))
        {
            OBControl control = objectDict.get(String.format("cover%s",index));
            String[] nums = ((String)control.attributes().get("num")).split(",");
            List<OBControl> numControls = new ArrayList<>();
            for(String num : nums)
            {
                numControls.add(objectDict.get(String.format("num_%s",num)));
                objectDict.get(String.format("box_%s",num)).setBackgroundColor(hiliteColour);
            }

            OBGroup gr = new OBGroup(numControls);

            dragTargets.add(gr);
            attachControl(gr);

            gr.setProperty("targetpos", OC_Generic.copyPoint(gr.position()));
            gr.setProperty("targetIndex", index);

            float posX = (bounds().width() * 0.2f) + ((bounds().width() * 0.6f)/(indexList.size()+1))*count;
            count++;
            gr.setPosition ( new PointF(posX, posY));
            gr.setProperty("startpos",OC_Generic.copyPoint(gr.position()));

        }
        unlockScreen();
        playSfxAudio("show_num",true);

    }


    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentPhase(),setStatus(STATUS_WAITING_FOR_DRAG),this);
    }

    public OBControl findTarget(PointF pt)
    {
        OBControl c =finger(0,2,dragTargets,pt);
        return c;
    }

    public String currentPhase()
    {
        return String.format("%s-%d",currentEvent(), currentIndex);
    }


    public void checkTargetClick() throws Exception
    {
        objectDict.get(String.format("num_%d",currentIndex)).show();
        OC_Count100_Additions.playNumberAudio(currentIndex,false,this);
        if(currentIndex == 1)
        {
            setReplayAudioScene(currentEvent(),"REPEAT2");
        }

        if(currentIndex == 50)
        {
            waitAudio();
            waitForSecs(0.2f);
            playSfxAudio("flash",false);

            OBAnimationGroup.runAnims(animForList(Arrays.asList(25,26),hiliteColour),0.2,true,OBAnim.ANIM_LINEAR,this);

            List<OBAnim> anims = animForList(Arrays.asList(25,26), Color.WHITE);
            anims.addAll(animForList(Arrays.asList(14,15,16,17,24,27,34,35,36,37),hiliteColour));
            OBAnimationGroup.runAnims(anims,0.2,true,OBAnim.ANIM_LINEAR,this);

            List<OBAnim> anims2 = animForList(Arrays.asList(14,15,16,17,24,27,34,35,36,37), Color.WHITE);
            anims2.addAll(animForList(Arrays.asList(3,4,5,6,7,8,13,23,33,18,28,38,43,44,45,46,47,48),hiliteColour));
            OBAnimationGroup.runAnims(anims2,0.2,true,OBAnim.ANIM_LINEAR,this);

            List<OBAnim> anims3 = animForList(Arrays.asList(3,4,5,6,7,8,13,23,33,18,28,38,43,44,45,46,47,48), Color.WHITE);
            anims3.addAll(animForList(Arrays.asList(2,12,22,32,42,9,19,29,39,49),hiliteColour));
            OBAnimationGroup.runAnims(anims3,0.2,true,OBAnim.ANIM_LINEAR,this);

            List<OBAnim> anims4 = animForList(Arrays.asList(2,12,22,32,42,9,19,29,39,49), Color.WHITE);
            anims4.addAll(animForList(Arrays.asList(1,11,21,31,41,10,20,30,40,50),hiliteColour));
            OBAnimationGroup.runAnims(anims4,0.2,true,OBAnim.ANIM_LINEAR,this);

            OBAnimationGroup.runAnims(animForList(Arrays.asList(1,11,21,31,41,10,20,30,40,50),Color.WHITE),0.2,true,OBAnim.ANIM_LINEAR,this);

            waitSFX();
            waitForSecs(0.4f);
            nextScene();

        }
        else
        {
            currentIndex++;
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public List<OBAnim> animForList(List<Integer> nums,int colour)
    {
        List<OBAnim> arr = new ArrayList<>();
        for(Integer num : nums)
        {
            arr.add(OBAnim.colourAnim("backgroundColor",colour,objectDict.get(String.format("box_%d",num))));
        }
        return arr;
    }



    public void completeDragNum(OBControl cont)
    {
        OBControl cover = objectDict.get(String.format("cover%s", cont.settings.get("targetIndex")));
        String[] nums =((String)cover.attributes().get("num")).split(",");
        List<OBAnim> numAnims = new ArrayList<>();
        for(String num : nums)
        {
            numAnims.add(OBAnim.colourAnim("colour", OC_Count100_S2.getEventColourForIndex((String)cover.attributes().get("cindex"),this),objectDict.get(String.format("num_%s",num))));
            numAnims.add(OBAnim.colourAnim("backgroundColor",Color.WHITE,objectDict.get(String.format("box_%s",num))));
        };

        OBAnimationGroup.runAnims(numAnims,0.5,true,OBAnim.ANIM_LINEAR,this);
    }

    public void clearDragNum(OBControl cont)
    {
        lockScreen();
        detachControl(cont);
        List<OBControl> objs = ((OBGroup)cont).ungroup();
        for (OBControl obj : objs)
        {
            obj.show();
            attachControl(obj);

        }
        unlockScreen();
        dragTargets.remove(cont);
    }


    private void checkDrop() throws Exception
    {
        OBControl cover = objectDict.get(String.format("cover%s", target.settings.get("targetIndex")));
        if(OBUtils.RectOverlapRatio(target.frame(), cover.frame()) > 0.15 )
        {

            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim((PointF)target.propertyValue("targetpos"),target)),0.1,true,OBAnim.ANIM_EASE_OUT,this);

            gotItRightBigTick(false);
            cover.hide();
            waitSFX();
            completeDragNum(target);


            if(!performSel("demoFin",currentEvent())){
                performSel("demoFin",String.format("%s%d",currentEvent(),currentIndex));
            };
            waitForSecs(0.3f);
            clearDragNum(target);
            this.target = null;
            if(dragTargets.size() >0)
            {
                setStatus(STATUS_WAITING_FOR_DRAG);
            }
            else
            {
                nextPhase();
            }

        }
        else
        {
            gotItWrongWithSfx();
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim((PointF)target.propertyValue("startpos"),target)),0.25,true,OBAnim.ANIM_EASE_OUT,this);
            target.setZPosition(target.zPosition()-10);
            this.target = null;
            setStatus(STATUS_WAITING_FOR_DRAG);
            waitSFX();
            playAudioScene(currentPhase(),"INCORRECT",0);
        }
    }

    public void checkTargetDrag(OBControl targ,PointF pt) throws Exception
    {
        playSfxAudio("drag",false);
        OBMisc.prepareForDragging(targ,pt,this);
        setStatus(STATUS_DRAGGING);
    }


    public void animateGridReset() throws Exception
    {
        List<OBAnim> arr = new ArrayList<>();

        for(OBControl control : filterControls("num_.*"))
        {
            if(((OBLabel)control).colour() != numColour)
                arr.add(OBAnim.colourAnim("colour",numColour,control));
        };

        OBAnimationGroup.runAnims(arr,0.5,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(0.5f);

    }


    public void demo() throws Exception
    {
        waitForSecs(0.3f);
        showCovers();
        waitForSecs(0.3f);
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.9f, 2), objectDict.get("box_48") .getWorldFrame()),0.3f,true);
        playAudioScene(currentEvent(),"DEMO",0);
        waitAudio();
        waitForSecs(0.3f);
        setUpDragNum(currentEvent());
        waitForSecs(0.3f);

        OBControl targ = dragTargets.get(0);

        targ.setZPosition(10 + targ.zPosition());
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.7f, 0.7f), targ.frame),-25 ,0.5f,true);
        playSfxAudio("drag",false);


        OBMisc.moveControlWithAttached(targ, Collections.singletonList(thePointer),(PointF)targ.propertyValue("targetpos"),0.5f, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playAudio("correct");
        objectDict.get(String.format("cover%s", targ.settings.get("targetIndex"))).hide();
        waitAudio();
        waitForSecs(0.3f);

        playAudioScene(currentEvent(),"DEMO",1);
        waitAudio();
        waitForSecs(0.3f);

        completeDragNum(targ);
        waitForSecs(0.3f);
        clearDragNum(targ);

        demoCount(Arrays.asList("1","2","3","4"));
        thePointer.hide();
        waitForSecs(0.5f);
        setUpDragNum(currentPhase());
        startScene();
    }



    public void demoCount(List<String> arr) throws Exception
    {
        float movetime = 0.5f;
        for(String num : arr)
        {
            OBControl box = objectDict.get(String.format("box_%s",num));
            movePointerToPoint(OB_Maths.locationForRect(new PointF(0.6f, 0.85f), box .getWorldFrame()),movetime,true);
            OC_Count100_Additions.playNumberAudio(Integer.valueOf(num),true,this);
            waitForSecs(0.3f);
            movetime=0.2f;
        }
    }


    public void demoStart2k() throws Exception
    {
        waitForSecs(0.3f);
        setUpDragNum(currentPhase());
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(1.34f, 0.5f), dragTargets.get(0) .frame()),0.3f,true);
        playAudioScene(currentPhase(),"DEMO",0);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
    }

    public void demoStart2l() throws Exception
    {
        waitForSecs(0.3f);
        setUpDragNum(currentPhase());
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(1.05f, 0.5f), objectDict.get("workrect") .frame()),0.6f,true);
        playAudioScene(currentPhase(),"DEMO",0);
        waitAudio();
        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(new PointF(1.2f, 0.7f), dragTargets.get(0) .frame()),0.3f,true);
        playAudioScene(currentPhase(),"DEMO",1);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
    }

    public void demoStart2m() throws Exception
    {
        playSfxAudio("reset_grid",false);
        for(int i =50; i>0; i--)
        {
            objectDict.get(String.format("num_%d", i)).hide();
            waitForSecs(0.01f);
        }
        waitSFX();
        waitForSecs(0.3f);
        loadPointer(POINTER_MIDDLE);

        moveScenePointer(OB_Maths.locationForRect(0.5f,1.1f,objectDict.get("workrect").frame()),0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.25f,1.1f,objectDict.get("workrect").frame()),0.5f,"DEMO",1,0.3f);
        playAudioScene("DEMO",2,true);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,objectDict.get("box_1").getWorldFrame()),0.5f,"DEMO",3,0.3f);

        thePointer.hide();
        waitForSecs(0.3f);


    }

    public void demoFin2j() throws Exception
    {
        loadPointer(POINTER_LEFT);
        OBControl cover = objectDict.get(String.format("cover%s", target.settings.get("targetIndex")));
        String[] nums = ((String)cover.attributes().get("num")).split(",");
        String firstnum = nums[0];
        List<String> fullList = new ArrayList<>();
        fullList.add(String.format("%d",Integer.valueOf(firstnum)-1));
        fullList.addAll(Arrays.asList(nums));
        demoCount(fullList);
        thePointer.hide();
    }

    public void demoFin2k() throws Exception
    {
        OBControl cover = objectDict.get(String.format("cover%s", target.settings.get("targetIndex")));
        String[] nums = ((String)cover.attributes().get("num")).split(",");
        String firstnum = nums[0];
        OBControl firstControl = objectDict.get(String.format("box_%s", firstnum));

        loadPointer(POINTER_MIDDLE);

        movePointerToPoint(new PointF(firstControl.getWorldPosition().x,OB_Maths.locationForRect(0, 1.02f, objectDict.get("workrect").frame()).y),0.3f,true);
        playAudioScene(currentPhase(),"FINAL",0);
        waitAudio();
        waitForSecs(0.3f);

        List<String> fullList = new ArrayList<>();
        fullList.add(String.format("%d",Integer.valueOf(firstnum)-10));
        fullList.addAll(Arrays.asList(nums));
        demoCount(fullList);
        thePointer.hide();
    }

    public void demoBox(int num,int audio)throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.6f, 0.9f), objectDict.get(String.format("box_%d",num)).getWorldFrame()),0.5f,true);
        playAudioScene(currentPhase(),"FINAL",audio);
        waitAudio();
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.6f, 0.9f), objectDict.get(String.format("box_%d",num-1)).getWorldFrame()),0.2f,true);
        waitForSecs(0.3f);
        thePointer.hide();

    }

    public void demoFin2l1() throws Exception
    {
        demoBox(26,0);
    }

    public void demoFin2l2() throws Exception
    {
        OBControl cover = objectDict.get(String.format("cover%s", target.settings.get("targetIndex")));
        if(Integer.valueOf((String)cover.attributes().get("audio")) == 1)
        {
            demoBox(13,0);
        }
        else
        {
            demoBox(50,1);
        }

        if(dragTargets.size() <= 1)
        {
            playAudioScene(currentPhase(),"FINAL",2);
            waitAudio();
            waitForSecs(0.3f);
        }

    }




}
