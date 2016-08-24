package org.onebillion.xprz.mainui.x_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 03/08/16.
 */
public class X_Count100_S1i extends XPRZ_SectionController
{
    int hilitecolour, numcolour;
    int currentrow, maxrow;
    List<OBControl> largenums;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();

        loadEvent("masterGrid");

        largenums = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));

        numcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        maxrow = OBUtils.getIntValue(eventAttributes.get("rowcount"));

        X_Count100_Additions.drawGrid(maxrow,objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")),numcolour,false,this);

        OBLabel label = (OBLabel)objectDict.get("num_1");

        Typeface font = OBUtils.standardTypeFace();
        for(int i=0; i<maxrow; i++)
        {
            float fontSize =label.fontSize()+applyGraphicScale((i+2)*10);
            if(i == 9)
                fontSize = fontSize + applyGraphicScale(50);

            int lastnum = 10*(i+1);


            OBControl box = objectDict.get(String.format("box_%d",lastnum));
            OBLabel numLabelLarge = new OBLabel(String.format("%d",lastnum),font,fontSize);

            numLabelLarge.setColour(hilitecolour);

            OBGroup largenum = new OBGroup(Collections.singletonList((OBControl)numLabelLarge));
            largenum.objectDict.put("num",numLabelLarge);
            largenum.sizeToTightBoundingBox();


            OBGroup testGroup = new OBGroup(Collections.singletonList(objectDict.get(String.format("num_%d",lastnum)).copy()));
            testGroup.sizeToTightBoundingBox();

            largenum.setProperty("dest_scale", testGroup.height()*1.0f/largenum.height());
            largenum.setProperty("dest_loc", testGroup.position());
            largenum.setPosition(box.getWorldPosition());
            RectF rect = box.getWorldFrame();
            if(largenum.right()>rect.right){
                largenum.setRight(rect.right);
            }
            if(largenum.bottom()>rect.bottom){
                largenum.setBottom(rect.bottom);
            }

            largenum.setZPosition(20);
            largenums.add(largenum);

            attachControl(largenum);
            largenum.hide();
        }
        hideControls("box_.*");
        hideControls("num_.*");

        X_Count100_Additions.loadNumbersAudio(this);
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1i();
            }
        });
    }


    public List<Integer> getDiagonals(int col)
    {
        int from, to;
        if(col <= 10){
            from = 1;
            to =col;
        }else{
            from = col - 10;
            to=5;
        }

        List<Integer> returnList = new ArrayList<>();

        for(int i = from; i<=to; i++)
        {
            int val;
            if(col <= 10){
                val = (10*(i-1))+(col-(i-1));
            }else{
                val = (10*i)+(col-i);
            }
            if(val <= 50){
                returnList.add(val);
            }else{
                break;
            }
        }

        return returnList;
    }

    public void demo1i() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        showRow(currentrow);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.5f), bounds()),0.5f,true);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.5f);
        startScene();

    }

    @Override
    public void fin()
    {
        try
        {
            if(maxrow == 5)
            {
                playSfxAudio("piano",false);
                for(int i = 1; i<=14; i++){
                    List<OBAnim> onAnims = new ArrayList<>();
                    List<OBAnim> offAnims = new ArrayList<>();
                    for(int boxNum : getDiagonals(i))
                    {
                        onAnims.add(OBAnim.colourAnim("backgroundColor", Color.YELLOW,objectDict.get(String.format("box_%d",boxNum))));
                        offAnims.add(OBAnim.colourAnim("backgroundColor",Color.WHITE,objectDict.get(String.format("box_%d",boxNum))));
                    }
                    OBAnimationGroup.chainAnimations(Arrays.asList(onAnims, offAnims),Arrays.asList(0.12f,0.12f),false,Arrays.asList(OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR),1, this);
                    waitForSecs(0.05f);
                }
                waitForSecs(1f);
                waitSFX();
            }
            gotItRight();
            super.fin();
        }
        catch (Exception e)
        {

        }

    }

    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        currentrow = OBUtils.getIntValue(eventAttributes.get("row"));

    }

    @Override
    public void doMainXX() throws Exception
    {
        showRow(currentrow);
        startScene();
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK), this);
    }



    public void showRow(int rowNum) throws Exception
    {

        lockScreen();
        for(int i = (rowNum-1)*10; i<rowNum*10; i++)
            objectDict.get(String.format("box_%d",i+1)).show();

        unlockScreen();
        playSfxAudio(String.format("note%d",rowNum),true);

    }


    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkTouch();
                }
            });

        }

    }

    private void checkTouch() throws Exception
    {
        int audioNum = 0;
        OBGroup largenum = (OBGroup)largenums.get(currentrow-1);
        for(int i = (currentrow-1)*10; i<currentrow*10; i++)
        {
            OBLabel num = (OBLabel)objectDict.get(String.format("num_%d",i+1));


            if((i+1)%10==0){
                largenum.show();
            }else{
                num.setColour ( hilitecolour);
                num.show();
            }

            X_Count100_Additions.playNumberAudio(i+1,true,this);

            if((i+1)%10==0){

                if(i+1 == 100){
                    playSfxAudio("lastnum",false);
                    for(int j=0; j<3; j++)
                    {
                        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",numcolour,largenum.objectDict.get("num"))),0.15,true,OBAnim.ANIM_LINEAR,this);
                        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",hilitecolour,largenum.objectDict.get("num"))),0.15,true,OBAnim.ANIM_LINEAR,this);
                    }
                    waitSFX();
                }

                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim((float)largenum.propertyValue("dest_scale"),largenum),
                        OBAnim.moveAnim((PointF) largenum.propertyValue("dest_loc"),largenum)),0.3f,true,OBAnim.ANIM_LINEAR,this);


                double timing = 0.04;
                for(int j =0; j<4; j++){

                    OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(20),largenum)),timing,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

                    timing = 0.08;
                    OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(-20),largenum)),timing,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);



                }
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(0),largenum)) ,0.04,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                lockScreen();
                largenum.hide();
                num.show();
                unlockScreen();
                waitForSecs(0.2f);

            }else{
                num.setColour ( numcolour);
            }

            audioNum++;
        }
        waitForSecs(0.1f);
        nextScene();
    }


}
