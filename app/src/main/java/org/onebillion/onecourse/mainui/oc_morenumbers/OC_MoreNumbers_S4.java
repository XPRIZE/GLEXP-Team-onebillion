package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/04/2017.
 */

public class OC_MoreNumbers_S4 extends OC_SectionController
{
    int targetNum, currentNum;
    OBLabel counter;
    List<OBControl> eventObjs;
    boolean buttonMode;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master4");
        createCounter(Color.BLACK);
        setUpButton(1);
        setUpButton(10);
        eventObjs = new ArrayList<>();
        counter.setString(String.format("%d", 0));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBControl button = objectDict.get("button_10");
        PointF loc = OBMisc.copyPoint(button.position());
        button.setProperty("startloc", OBMisc.copyPoint(loc));
        loc.x = bounds().width() / 2.0f;
        button.setPosition(loc);
        button.show();
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo4a();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        currentNum = 0;
        targetNum = OBUtils.getIntValue(eventAttributes.get("num"));
        if(eventAttributes.get("mode") != null && eventAttributes.get("mode").equals("button"))
        {
            buttonMode = true;
            if(counter == null)
            {
                OBControl shutter = objectDict.get("shutter");
                createCounter(shutter.fillColor());
                objectDict.get("numbox").setZPosition(1);
                counter.setZPosition(1.5f);
                shutter.setZPosition(2);
                shutter.setAnchorPoint(new PointF(0, 0.5f));
                shutter.setProperty("startwidth",shutter.width());
                counter.hide();
                OC_MoreNumbers_Additions.buttonSet(2,this);
            }
            counter.setString (String.format("%d", targetNum));
        }
        else
        {
            buttonMode = false;

        }
        for(OBControl cont : eventObjs)
            detachControl(cont);
        eventObjs.clear();
        OBGroup obj = (OBGroup)objectDict.get("obj");
        OBMisc.colourObjectFromAttributes(obj);
        obj.hide();
        PointF loc = OBMisc.copyPoint(objectDict.get("workrect").position());
        loc.x = 0.5f * bounds().width();
        objectDict.get("workrect").setPosition(loc);
        int size = 70;
        if(!buttonMode)
        {
            size = targetNum%10 == 0 ? targetNum : (int)Math.ceil(targetNum/10.0)*10;

        }
        for(int i=0; i< (buttonMode ? size : targetNum); i++)
        {
            OBControl objcopy = obj.copy();
            objcopy.setPosition (OB_Maths.locationForRect((i%10)/9.0f,(float)Math.floor(i/10.0)*(1.0f/((size/10)-1)),objectDict.get("workrect").frame()));
            attachControl(objcopy);
            eventObjs.add(objcopy);
            objcopy.hide();
        }
    }

    public void doMainXX() throws Exception
    {
        startScene(true);
    }

    public void fin()
    {
        goToCard(OC_MoreNumbers_S4s.class, "event4");
    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            int addValue = 0;
            OBGroup buttonControl = null;
            OBControl btn1 = objectDict.get("button_1"), btn10 = objectDict.get("button_10");
            if(finger(0,1,Arrays.asList(btn10),pt) != null)
            {
                buttonControl = (OBGroup)btn10;
                addValue = 10;

            }
            else if(finger(0,1,Arrays.asList(btn1),pt) != null)
            {
                buttonControl = (OBGroup)btn1;
                addValue = 1;

            }
            if(buttonControl != null)
            {
                setStatus(STATUS_BUSY);
                final OBGroup btn = buttonControl;
                final int add = addValue;

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkButton1(btn, add);
                    }
                });


            }
            else if(buttonMode)
            {
                if(finger(0,2,Arrays.asList(objectDict.get("button_arrow")),pt) != null)
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
                else if(finger(0,2,eventObjs,pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            resetScene(false);
                            setStatus(STATUS_AWAITING_CLICK);
                        }
                    });
                }
            }
        }
    }

    public void checkButton1(OBGroup buttonControl, int addValue) throws Exception
    {
        OBGroup btn1 = (OBGroup)objectDict.get("button_1"), btn10 = (OBGroup)objectDict.get("button_10");
        lockScreen();
        btn1.objectDict.get("hilight").hide();
        btn10.objectDict.get("hilight").hide();
        buttonControl.objectDict.get("hilight").show();

        unlockScreen();
        if(!buttonMode)
        {
            if((addValue == 1 && currentNum + 10 <= targetNum) || (currentNum + addValue > targetNum))
            {
                gotItWrongWithSfx();
                long time = setStatus(STATUS_AWAITING_CLICK);
                waitSFX();
                if(time == statusTime)
                {
                    playAudioQueuedScene(addValue == 10 ? "INCORRECT2" : "INCORRECT",300,false);
                    buttonControl.objectDict.get("hilight").hide();
                }
            }
            else
            {
                addEventObjectsToScreen(addValue);
                if(currentNum == targetNum)
                {
                    waitSFX();
                    buttonControl.objectDict.get("hilight").hide();
                    playCurrentNumAudio();
                    waitAudio();
                    gotItRightBigTick(true);
                    waitForSecs(0.2f);
                    demoFinPointNum(getAudioForScene(currentEvent(),"DEMO"));
                    nextScene();
                }
                else
                {
                    long time = setStatus(STATUS_AWAITING_CLICK);
                    waitSFX();
                    if(time == statusTime)
                    {
                        buttonControl.objectDict.get("hilight").hide();
                        playCurrentNumAudio();

                    }
                }
            }

        }
        else
        {
            if( (addValue == 10 && ((currentNum+10 > 70) || (currentNum%10 != 0))) ||
                    (addValue == 1 && (((currentNum+1)%10 == 0) ||
                            (currentNum+1 >70) || (currentNum + 10 < targetNum))))
            {
                gotItWrongWithSfx();
                long time = setStatus(STATUS_AWAITING_CLICK);
                waitSFX();
                if(time == statusTime)
                {
                    if(addValue == 1 && currentNum != 70 && ((currentNum+1)%10 != 0))
                        playAudioQueuedScene("INCORRECT",300,false);
                    buttonControl.objectDict.get("hilight").hide();

                }

            }
            else
            {
                addEventObjectsToScreen(addValue);
                long time = setStatus(STATUS_AWAITING_CLICK);
                waitSFX();
                if(time == statusTime)
                    buttonControl.objectDict.get("hilight").hide();

            }

        }
    }

    public void checkButton2() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(1,this);
        if(currentNum == targetNum)
        {
            playAudio(null);
            gotItRightBigTick(true);
            waitForSecs(0.2f);
            OC_MoreNumbers_Additions.buttonSet(2,this);
            demoBox();
            resetScene(true);
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            if(currentNum > 0)
            {
                resetScene(false);
                waitForSecs(0.2f);
            }
            playAudioQueuedScene("INCORRECT2",300,false);
            OC_MoreNumbers_Additions.buttonSet(0,this);
            setStatus(STATUS_AWAITING_CLICK);

        }
    }

    public void addEventObjectsToScreen(int addValue) throws Exception
    {
        lockScreen();
        for(int i=currentNum; i<currentNum+addValue; i++)
            eventObjs.get(i).show();
        if(!buttonMode)
            counter.setString ( String.format("%d", currentNum+addValue));
        playSfxAudio(addValue == 1 ? "1ns_btn" : "10ns_btn",false);

        unlockScreen();
        currentNum += addValue;

    }
    public void playCurrentNumAudio() throws Exception
    {
        if(!buttonMode)
        {
            if(currentNum%10 == 0)
            {
                playAudioScene("FINAL",(currentNum/10)-1,false);
            }
            else
            {
                playAudioScene("FINAL2",(currentNum%10)-1,false);
            }
        }
    }

    public void startScene(boolean withShutter) throws Exception
    {
        if(withShutter && buttonMode)
            animateShutter(true);
        if(buttonMode)
            OC_MoreNumbers_Additions.buttonSet(0,this);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }

    public void setUpButton(int num)
    {
        OBGroup button = (OBGroup)objectDict.get(String.format("button_%d", num));
        OBMisc.colourObjectFromAttributes(button);
        button.objectDict.get("reflection").hide();
        OC_MoreNumbers_Additions.insertIntoGroup(button,num, 70*objectDict.get("numbox").height()/90.0f,
                OBUtils.colorFromRGBString((String)button.attributes().get("num_colour")),OB_Maths.locationForRect(0.5f,0.5f,button.frame()), this);
        button.objectDict.get("hilight").setZPosition(2);
        button.objectDict.get("number").setZPosition(1.5f);

    }

    public void markCounterNum(int num)
    {
        counter.setHighRange(num,num+1,Color.RED);
    }

    public void colourEntireCounter(int colour)
    {
        counter.setHighRange(-1,-1,Color.BLACK);
        counter.setColour(colour);
    }

    public void createCounter(int colour)
    {
        OBControl box = objectDict.get("numbox");
        float fontSize = 90*box.height()/90.0f;
        counter = new OBLabel(String.format("%d",888), OBUtils.standardTypeFace(),fontSize);
        counter.setPosition(box.position());
        counter.setColour(colour);
        attachControl(counter);
    }

    public void animateShutter(boolean open) throws Exception
    {
        OBControl shutter = objectDict.get("shutter");
        if(!open)
            shutter.show();
        playSfxAudio(open ? "shutter_on" : "shutter_off",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("width",open ? 1 : (float)shutter.propertyValue("startwidth"),shutter)), 0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        waitSFX();
        if(open)
            shutter.hide();
    }

    public void demoFinPointNum(List<String> audio) throws Exception
    {
        if(thePointer.hidden)
            loadPointer(POINTER_LEFT);
        int count = audio.size();
        for(int i=0; i<count; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect((i==0 ? 0.4f : 0.7f),0.95f,objectDict.get("numbox").frame()),(i==0 ? -30 : -25),(i==0 ? 0.6f : 0.3f),true);
            markCounterNum(i);
            playAudio(audio.get(i));
            waitAudio();
            waitForSecs(i==count-1 ? 0.3 : 0.7);
            lockScreen();
            colourEntireCounter(Color.BLACK);
            if(i==count-1)
                thePointer.hide();
            unlockScreen();
        }
        waitForSecs(1f);
        resetScene(true);
        waitForSecs(0.5f);
    }

    public void resetScene(boolean full) throws Exception
    {
        currentNum = 0;
        lockScreen();
        for(OBControl cont : eventObjs)
            cont.hide();
        if(!buttonMode)
            counter.setString("0");

        if(full)
        {
            if (OBUtils.getBooleanValue(eventAttributes.get("reset")))
            {
                detachControl(objectDict.get("numbox"));
                detachControl(counter);
                counter = null;
            }


            if (currentEvent() == events.get(events.size() - 1))
            {
                counter.hide();
                objectDict.get("shutter").hide();
                objectDict.get("numbox").hide();
                objectDict.get("button_10").hide();
                objectDict.get("button_1").hide();
            }
        }
        playSfxAudio("pop", false);
        unlockScreen();
        waitSFX();
    }

    public void pointerClickButton(int num) throws Exception
    {
        OBGroup button = (OBGroup)objectDict.get(String.format("button_%d",num));
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,button.frame()),-35,0.2f,true);
        button.objectDict.get("hilight").show();
        addEventObjectsToScreen(num);
        waitSFX();
        button.objectDict.get("hilight").hide();
        movePointerToPoint(OB_Maths.locationForRect(1f,0.7f,button.frame()),-35,0.2f,true);

    }
    public void demo4a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_10").frame()),-35,0.5f,"DEMO",0,0.3f);
        pointerClickButton(10);
        playAudioScene("DEMO",1,true);
        pointerClickButton(10);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.5f,1.5f,eventObjs.get(14).frame()),-30,0.5f,"DEMO",3,0.3f);
        List<String> audios = getAudioForScene(currentEvent(),"DEMO");
        demoFinPointNum(Arrays.asList(audios.get(4)));
        nextScene();
    }

    public void demo4e() throws Exception
    {
        OBGroup btn10 = (OBGroup)objectDict.get("button_10");
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)btn10.propertyValue("startloc"),btn10))
            ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.2f);
        objectDict.get("button_1").show();
        playSfxAudio("1ns_button_on",true);
        waitForSecs(0.2f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_1").frame()),-35,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(3f,0.7f,objectDict.get("button_1").frame()),-35,0.5f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_10").frame()),-35,0.35f,true);
        pointerClickButton(10);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_1").frame()),-35,0.35f,true);
        for(int i=0; i<6; i++)
        {
            pointerClickButton(1);
            playAudioScene("DEMO",i+3,true);
        }
        waitForSecs(0.3f);
        List<String> audios = getAudioForScene(currentEvent(),"DEMO");
        demoFinPointNum(Arrays.asList(audios.get(9),audios.get(10)));
        nextScene();
    }

    public void demoBox() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        int startColour = objectDict.get("shutter").fillColor();
        ((OBPath)objectDict.get("numbox")).setStrokeColor(Color.RED);
        waitForSecs(0.3f);
        int count = getAudioForScene(currentEvent(),"FINAL").size() -1;
        for(int i=0; i<count; i++)
        {
            markCounterNum(i);
            playAudioScene("FINAL",i+1,true);
            lockScreen();
            colourEntireCounter(startColour);

            unlockScreen();
            waitForSecs(i==count-1 ? 0.3 : 0.5);
        }
        ((OBPath)objectDict.get("numbox")).setStrokeColor(startColour);
        waitForSecs(0.5f);
        animateShutter(false);
        waitForSecs(0.5f);
    }

    public void demo4k() throws Exception
    {
        lockScreen();
        counter.show();
        objectDict.get("shutter").show();
        objectDict.get("numbox").show();
        playSfxAudio("pop_on",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.6f,objectDict.get("numbox").frame()),-40,0.5f,"DEMO",0,0.3f);
        animateShutter(true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.7f,objectDict.get("button_1").frame()),-35,0.5f,"DEMO",2,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_10").frame()),-35,0.35f,true);
        for(int i=0; i<3; i++)
        {
            pointerClickButton(10);
            waitForSecs(0.3f);
        }
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_1").frame()),-35,0.35f,true);
        for(int i=0; i<4; i++)
        {
            pointerClickButton(1);
            waitForSecs(0.3f);
        }
        playAudioScene("DEMO",4,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.7f,objectDict.get("numbox").frame()),-35,0.5f,true);
        int startColour = objectDict.get("shutter").fillColor();
        ((OBPath)objectDict.get("numbox")).setStrokeColor(Color.RED);
        waitForSecs(0.3f);
        for(int i=0; i<2; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect((i==0 ? 0.4f : 0.7f),0.95f,objectDict.get("numbox").frame()),(i==0 ? -30 : -25),(i==0 ? 0.6f : 0.3f),true);
            markCounterNum(i);
            playAudioScene("DEMO",i+5,true);
            waitForSecs(i==1 ? 0.3 : 0.5);
            lockScreen();
            colourEntireCounter(startColour);
            if(i==1)
                thePointer.hide();

            unlockScreen();

        }
        waitForSecs(0.3f);
        ((OBPath)objectDict.get("numbox")).setStrokeColor(startColour);
        waitForSecs(1f);
        animateShutter(false);
        waitForSecs(0.3f);
        resetScene(true);
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo4l() throws Exception
    {
        animateShutter(true);
        waitForSecs(0.3f);
        objectDict.get("button_arrow").show();
        playSfxAudio("pop_on",true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,0.7f,objectDict.get("numbox").frame()),-40,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()),-25,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,0.7f,objectDict.get("button_arrow").frame()),-25,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        startScene(false);
    }

}
