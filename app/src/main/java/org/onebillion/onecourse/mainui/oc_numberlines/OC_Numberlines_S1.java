package org.onebillion.onecourse.mainui.oc_numberlines;

import android.graphics.Color;
import android.graphics.PointF;
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
import java.util.List;

/**
 * Created by michal on 12/04/2017.
 */

public class OC_Numberlines_S1 extends OC_SectionController
{
    int largeNumColour;
    List<OBLabel> largeNums;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.loadNumbersFrom(1,9, Color.BLACK, "rectnum", "num", this);
        largeNumColour = OBUtils.colorFromRGBString(eventAttributes.get("largenumcolour"));
        largeNums = new ArrayList<>();
        setSceneXX(currentEvent());
        showControls("obj.*");

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1a();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);

        OBControl image = objectDict.get("image");
        if(eventAttributes.get("loc1") !=null && eventAttributes.get("loc2") != null)
        {
            deleteControls("obj.*");
            List<List<Float>> arrs = Arrays.asList(OBMisc.stringToFloatList(eventAttributes.get("loc1"),","),
                    OBMisc.stringToFloatList(eventAttributes.get("loc2"),","));
            int index = 1;
            for(int i=0; i<2; i++)
            {
                for(int j=0; j<arrs.get(i).size(); j+=2)
                {
                    OBControl screenImage = image.copy();
                    screenImage.setPosition(OB_Maths.locationForRect(arrs.get(i).get(j), arrs.get(i).get(j+1),
                            objectDict.get(String.format("container%d",i+1)).getWorldFrame()));
                    attachControl(screenImage);
                    screenImage.hide();
                    screenImage.setZPosition (2);
                    objectDict.put(String.format("obj%d", index),screenImage);
                    index++;
                }
            }
        }
        for(int i=1; i<=2; i++)
        {
            OBGroup group = (OBGroup)objectDict.get(String.format("container%d", i));
            if(group.objectDict.get("_highlight") == null)
            {
                group.show();
                group.highlight();
                group.lowlight();

            }

        }
        objectDict.get("container1").setZPosition(1);
        objectDict.get("container2").setZPosition(1);
        objectDict.get("container1").highlight();
        objectDict.get("container2").highlight();
        objectDict.get("container1").lowlight();
        objectDict.get("container2").lowlight();
        if(eventAttributes.get("largenum") !=null)
        {
            for(OBControl con : largeNums)
                detachControl(con);
            largeNums.clear();
            List<Integer> arr = OBMisc.stringToIntegerList(eventAttributes.get("largenum"),",");
            PointF rloc = OB_Maths.relativePointInRectForLocation(objectDict.get("locnum").getWorldPosition(), objectDict.get("container1").getWorldFrame());
            float fontSize = ((OBLabel)objectDict.get("num1")).fontSize() * 1.7f;
            for(int i = 1; i <= 2; i++)
            {
                OBLabel numLabel = new OBLabel(String.format("%d",arr.get(i-1)),OBUtils.standardTypeFace(),fontSize);
                numLabel.setPosition(OB_Maths.locationForRect(rloc.x,rloc.y,objectDict.get(String.format("container%d",i)).getWorldFrame()));
                numLabel.setColour(largeNumColour);
                numLabel.setZPosition(3);
                attachControl(numLabel);
                largeNums.add(numLabel);
                numLabel.hide();
            }
        }
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        try
        {
            goToCard(OC_Numberlines_S1o.class, "event1");
        }
        catch (Exception e)
        {

        }
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(eventAttributes.get("target").equals("container"))
            {
                final OBControl tar = finger(0,1,filterControls("container.*"),pt);
                if(tar != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkTarget1((OBGroup)tar);
                        }
                    });
                }

            }
            else
            {
                if(eventAttributes.get("target").equals("num"))
                {
                    final OBControl tar = finger(0,2,filterControls("num.*"),pt);
                    if(tar != null)
                    {
                        setStatus(STATUS_BUSY);
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkTarget2((OBLabel)tar);
                            }
                        });

                    }
                }
            }
        }
    }

    public void checkTarget1(OBGroup tar) throws Exception
    {
        playAudio(null);
        tar.highlight();
        if(tar == objectDict.get(String.format("container%s",eventAttributes.get("correct"))))
        {
            gotItRightBigTick(true);
            finishScene(tar);
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            tar.lowlight();
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void checkTarget2(OBLabel tar) throws Exception
    {
        playAudio(null);
        tar.setColour(Color.RED);
        if(tar == objectDict.get(String.format("num%s",eventAttributes.get("correct"))))
        {
            gotItRightBigTick(true);
            finishScene2(tar);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            tar.setColour(Color.BLACK);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void demoHilite(int num) throws Exception
    {
        OBControl cont = objectDict.get(String.format("container%d",num));
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,1f,cont.frame()),0.4f,true);
        cont.highlight();
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.2f);
        thePointer.hide();
        startScene();
    }

    public void demoLargeNumPoint(List<Integer> arr) throws Exception
    {
        loadPointer(POINTER_LEFT);
        int index = 0;
        for(Integer num : arr)
        {
            moveScenePointer(OB_Maths.locationForRect(1.1f,0.7f,largeNums.get(num).frame()),-40,0.5f,"FINAL",index,0.2f);
            largeNums.get(num).show();
            playSfxAudio("show_num",true);
            waitForSecs(0.2f);
            index++;

        }
        thePointer.hide();
        playAudioScene("FINAL",2,true);
        waitForSecs(0.8f);
    }

    public void showObjects(boolean all) throws Exception
    {
        lockScreen();
        showControls("obj.*");
        if(all)
            showControls("num.*");

        unlockScreen();
        playSfxAudio("show",true);
        waitForSecs(0.3f);
    }

    public void hideObjects(boolean all) throws Exception
    {
        lockScreen();
        deleteControls("obj.*");
        for(OBControl con : largeNums)
            con.hide();
        objectDict.get("container1").lowlight();
        objectDict.get("container2").lowlight();
        if(all)
        {
            deleteControls("container.*");
            hideControls("num.*");

        }
        unlockScreen();
        playSfxAudio("hide",true);
        waitForSecs(0.5f);
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }


    public void demo1a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        demoButtons();
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f,0.7f), bounds()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("container2").frame()),-20,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        waitForSecs(0.4f);
        hideObjects(false);
        nextScene();
    }

    public void demo1b() throws Exception
    {
        showObjects(false);
        startScene();
    }

    public void demo1c() throws Exception
    {
        showObjects(false);
        startScene();
    }

    public void demo1d() throws Exception
    {
        showObjects(true);
        demoHilite(1);
    }

    public void demo1e() throws Exception
    {
        demoHilite(2);
    }

    public void demo1g() throws Exception
    {
        showObjects(true);
        demoHilite(1);
    }

    public void demo1h() throws Exception
    {
        demoHilite(2);
    }

    public void demo1j() throws Exception
    {
        waitForSecs(0.5f);
        showObjects(false);
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("container1").frame()),-30,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        waitForSecs(0.2f);
        hideObjects(false);
        nextScene();
    }

    public void demo1k() throws Exception
    {
        showObjects(false);
        startScene();
    }

    public void demo1l() throws Exception
    {
        showObjects(true);
        demoHilite(1);
    }

    public void demo1m() throws Exception
    {
        demoHilite(2);
    }

    public void demoFin1f() throws Exception
    {
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(0.5f);
        hideObjects(false);
    }

    public void demoFin1i() throws Exception
    {
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(0.5f);
        hideObjects(true);
        waitForSecs(0.5f);
    }

    public void demoFin1k() throws Exception
    {
        demoLargeNumPoint(Arrays.asList(0,1));
        hideObjects(false);
    }

    public void demoFin1n() throws Exception
    {
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(0.5f);
        hideObjects(true);
    }

    public void finishScene(OBControl target) throws Exception
    {
        if(!performSel("demoFin",currentEvent()))
        {
            demoLargeNumPoint(Arrays.asList(1,0));
            hideObjects(false);
        }
    }

    public void finishScene2(OBLabel target) throws Exception
    {
        OBLabel largeNum = null;
        for(OBLabel lab : largeNums)
        {
            if(lab.text().equals(target.text()))
                largeNum =lab;
        }

        float width = largeNum.width(), height = largeNum.height();
        PointF loc = OBMisc.copyPoint(largeNum.position());
        lockScreen();
        largeNum.setColour(target.colour());
        largeNum.setWidth(target.width());
        largeNum.setHeight(target.height());
        largeNum.setPosition(target.position());
        largeNum.setZPosition(10);
        target.setColour(Color.BLACK);

        unlockScreen();
        largeNum.show();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc,largeNum),
                OBAnim.propertyAnim("width",width,largeNum),
                OBAnim.propertyAnim("height",height,largeNum),
                OBAnim.colourAnim("colour",largeNumColour,largeNum)
        ),0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        lockScreen();
        objectDict.get("container1").lowlight();
        objectDict.get("container2").lowlight();
        unlockScreen();
        waitForSecs(0.3f);
    }

}
