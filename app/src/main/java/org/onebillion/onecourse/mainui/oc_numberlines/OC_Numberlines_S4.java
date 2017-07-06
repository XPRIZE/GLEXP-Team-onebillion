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
 * Created by michal on 11/05/2017.
 */

public class OC_Numberlines_S4 extends OC_SectionController
{
    int eqColour, numColour, divColour, mainColour;
    int currentJump, targetNum, startNum, dropCount, maxNum;
    List<OBControl> eventTargets;

    public float graphicScale()
    {
        return bounds().width()/1024;
    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("masterStart");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        eventTargets = new ArrayList<>();
        numColour = OBUtils.colorFromRGBString(eventAttributes.get("numcolour"));
        divColour = OBUtils.colorFromRGBString(eventAttributes.get("divcolour"));
        mainColour = OBUtils.colorFromRGBString(eventAttributes.get("maincolour"));
        eqColour = OBUtils.colorFromRGBString(eventAttributes.get("eqcolour"));
        OBMisc.loadNumbersFrom(OBUtils.getIntValue(eventAttributes.get("num1")), OBUtils.getIntValue(eventAttributes.get("num2")), numColour, "numbox", "cnum_", this);
        OC_Numberlines_Additions.drawNumberLine(objectDict.get("numberline"),5,OBUtils.getIntValue(eventAttributes.get("startnum")),mainColour,divColour,numColour, this);

        objectDict.get("mainline").hide();
        hideControls("divline_.*");
        hideControls("num_.*");
        if(objectDict.get("bag_front") != null)
        {
            PointF anchor = OB_Maths.relativePointInRectForLocation(objectDict.get("bag_front").position(),objectDict.get("bag_back").frame());
            objectDict.get("bag_back").setAnchorPoint(anchor);
            PointF anchor2 = OB_Maths.relativePointInRectForLocation(objectDict.get("bag_front").position(),objectDict.get("bag_drop").frame());
            objectDict.get("bag_drop").setAnchorPoint(anchor2);
            objectDict.get("bag_front").setZPosition(5);
            objectDict.get("bag_back").setZPosition(3);

        }
        setSceneXX(currentEvent());
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

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        eventTargets.clear();
        if(eventAttributes.get("equation") != null)
        {
            deleteControls("equation");
            OC_Numberlines_Additions.loadEquation(eventAttributes.get("equation"),"equation",objectDict.get("eqbox"),eqColour,false,1.5f,1,this);
            OC_Numberlines_Additions.hideEquation((OBGroup)objectDict.get("equation"),this);
        }
        if(eventAttributes.get("num") != null)
            targetNum = OBUtils.getIntValue(eventAttributes.get("num"));
        if(eventAttributes.get("startnum") != null)
            startNum = currentJump = OBUtils.getIntValue(eventAttributes.get("startnum"));
        if(eventAttributes.get("target") != null)
        {
            if (eventAttributes.get("target").equals("nest"))
            {
                eventTargets.addAll(filterControls("aobj_.*"));
            } else if (eventAttributes.get("target").equals("tomato"))
            {
                for (OBControl control : filterControls("obj_.*"))
                {
                    control.setProperty("startrot", control.rotation);
                    control.setProperty("startpos", OBMisc.copyPoint(control.position()));
                }
                eventTargets.addAll(filterControls("obj_.*"));
                maxNum = (int) eventTargets.size();
                dropCount = 0;
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
            waitForSecs(0.5f);
        }
        catch (Exception e)
        {

        }
        goToCard(OC_Numberlines_S4n.class,(String)this.params);
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("nest"))
        {
            if (finger(0, 1, filterControls("container.*"), pt) != null)
            {
                final OBControl cont = OBMisc.nearestControlToPoint(eventTargets, pt);
                if (cont != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkNest(cont, pt);
                        }
                    });
                }
            }

        }
        else if (status() == STATUS_AWAITING_CLICK && (eventAttributes.get("target").equals("cnum")
                || eventAttributes.get("target").equals("num")))
        {
            final OBControl cont = finger(0, 1, filterControls(String.format("%s_.*", eventAttributes.get("target"))), pt);
            if (cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkNum((OBLabel) cont);
                    }
                });
            }
        }
        else if (status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("divline"))
        {
            OBControl tar = finger(0, 2, filterControls("divline_.*"), pt);
            if (tar == null)
                tar = finger(0, 1, filterControls("num_.*"), pt);

            if (tar != null)
            {
                final OBControl cont = tar;
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDivLine(cont);
                    }
                });
            }
        }
        else if (status() == STATUS_WAITING_FOR_DRAG && eventAttributes.get("target").equals("tomato"))
        {
            final OBControl cont = finger(0, 1, eventTargets, pt);
            if (cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTomato(cont, pt);
                    }
                });
            }
        }
    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void touchUpAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
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


    public void checkNest(OBControl cont, PointF pt) throws Exception
    {
        playAudio(null);
        spawnObject(cont,pt,0.35f,"egg_pop");
        eventTargets.remove(cont);
        if(eventTargets.size() == 0)
        {
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void checkNum(OBLabel cont) throws Exception
    {
        playAudio(null);
        cont.setColour(Color.RED);
        if((int)cont.settings.get("num_value")  == targetNum)
        {
            playAudio(null);
            gotItRightBigTick(true);
            performSel("demoFin",currentEvent());
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            cont.setColour ( numColour);
            playAudioQueuedScene("INCORRECT",300,false);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void checkDivLine(OBControl cont) throws Exception
    {
        playAudio(null);
        if((int)cont.settings.get("num_value")  == currentJump)
        {
            OC_Numberlines_Additions.animateCurve(currentJump, currentJump<targetNum, 0.65f, true, true, this);
            if(currentJump<targetNum)
            {
                currentJump++;
            }
            else
            {
                currentJump--;
            }
            gotItRight();
            if(currentJump == targetNum)
            {
                waitForSecs(0.3f);
                displayTick();
                waitForSecs(0.5f);
                nextScene();
            }
            else
            {
                String audioName = String.format("PROMPT%d.REPEAT", Math.abs(currentJump-startNum)+1);
                if(getAudioForScene(currentEvent(),audioName) != null)
                    setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),audioName), 300));
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if(time == statusTime)
            {
                if(currentJump != startNum)
                {
                    String audioName = String.format("INCORRECT%d", Math.abs(currentJump-startNum)+1);
                    if(getAudioForScene(currentEvent(),audioName) != null)
                    {
                        playAudioQueuedScene(audioName,300,false);

                    }
                    else
                    {
                        playAudioQueuedScene("INCORRECT",300,false);
                    }
                }
                else
                {
                    playAudioQueuedScene("INCORRECT",300,false);
                }
            }
        }
    }

    public void checkTomato(OBControl cont, PointF pt) throws Exception
    {
        playAudio(null);
        OBMisc.prepareForDragging(cont, pt, this);
        cont.setRotation(0);
        playSfxAudio("drag", false);
        setStatus(STATUS_DRAGGING);
    }

    public void checkDrop() throws Exception
    {
        playAudio(null);
        OBControl cont = target;
        target = null;

        if(objectDict.get("bag_drop") .frame().contains(cont.position().x, cont.position().y))
        {
            playAudio("correct");
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.5f,-0.25f,objectDict.get("bag_front").frame()),cont))
                    ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            cont.setZPosition ( 4);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("bag_front").frame()),cont))
                    ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            playSfxAudio("bag_shiver",false);
            animateBag(6,0.025f);
            for(int i=0; i<3; i++)
            {
                animateBag(-6,0.05f);
                animateBag(6,0.05f);
            }
            animateBag(0,0.025f);
            playSFX(null);
            cont.hide();
            eventTargets.remove(cont);
            objectDict.remove(cont.attributes().get("id"));
            detachControl(cont);
            if(eventTargets.size() == targetNum)
            {
                displayTick();
                waitForSecs(0.5f);
                hideBag();
                waitForSecs(0.2f);
                nextScene();

            }
            else
            {
                String audioName = String.format("PROMPT%d.REPEAT", maxNum - (int)eventTargets.size()+1);
                if(getAudioForScene(currentEvent(),audioName) != null)
                    setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),audioName), 300));
                setStatus(STATUS_WAITING_FOR_DRAG);

            }
        }
        else
        {
            playAudio("wrong");
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)cont.propertyValue("startpos") ,cont),
                    OBAnim.rotationAnim((float)cont.propertyValue("startrot"),cont)),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            cont.setZPosition(cont.zPosition()-10);
            waitForAudio();
            setStatus(STATUS_WAITING_FOR_DRAG);
            String audioName = String.format("INCORRECT%d", maxNum - (int)eventTargets.size() + 1);
            if(getAudioForScene(currentEvent(),audioName) != null)
                playAudioQueuedScene(audioName,300,false);
            else
                playAudioQueuedScene("INCORRECT",300,false);

        }
    }


    public void startScene() throws Exception
    {
        if(eventAttributes.get("target").equals("tomato"))
            OBMisc.doSceneAudio(4,setStatus(STATUS_WAITING_FOR_DRAG),this);
        else
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void spawnObject(OBControl obj,PointF source, float duration, String audio) throws Exception
    {
        PointF point = OBMisc.copyPoint(obj.position());
        float rotation = obj.rotation;
        float zposition = obj.zPosition();
        obj.setPosition(source);
        obj.setRotation(0);
        if(audio != null)
            playSfxAudio(audio,false);
        obj.setZPosition(5);
        obj.show();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(point,obj),
                OBAnim.rotationAnim(rotation,obj))
                ,duration,true,OBAnim.ANIM_EASE_OUT,this);
        obj.setZPosition(zposition);
        waitSFX();
    }

    public void showNums(int start,boolean reverse) throws Exception
    {
        if(!reverse)
        {
            int index =1;
            for (int i=start; i<start+4; i++)
            {
                playSfxAudio(String.format("scale%d",index++),false);
                objectDict.get(String.format("num_%d",i)).show();
                waitSFX();
            }
        }
        else
        {
            int index =7;
            for (int i=start; i>start-4; i--)
            {
                playSfxAudio(String.format("scale%d",index--),false);
                objectDict.get(String.format("num_%d",i)).show();
                waitSFX();
            }
        }
    }

    public void hideCnums()
    {
        lockScreen();
        for(OBControl control : filterControls("cnum_.*"))
        {
            control.hide();
            ((OBLabel)control).setColour(numColour);
        }
        unlockScreen();
    }

    public void redrawScreen(int start,boolean skip)
    {
        lockScreen();
        int size = (int)objectDict.get("mainline").propertyValue("num_size");
        deleteControls("mainline");
        deleteControls("divline_.*");
        deleteControls("num_.*");
        deleteControls("linelabel_.*");
        deleteControls("curveline_.*");
        if(!skip)
            deleteControls("obj_.*");
        deleteControls("aobj_.*");
        deleteControls("equation");
        objectDict.get("arrowhead").hide();
        OC_Numberlines_Additions.drawNumberLine(objectDict.get("numberline"),size,start,mainColour,divColour,numColour,this);
        hideControls("num_.*");
        unlockScreen();
    }

    public void animateBag(int angle,float duration) throws Exception
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(angle),objectDict.get("bag_front")),
                OBAnim.rotationAnim((float)Math.toRadians(angle),objectDict.get("bag_back")))
                ,duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void showBag()
    {
        lockScreen();
        objectDict.get("bag_front").show();
        objectDict.get("bag_back").show();
        unlockScreen();
    }

    public void hideBag()
    {
        lockScreen();
        objectDict.get("bag_front").hide();
        objectDict.get("bag_back").hide();
        unlockScreen();
    }

    public void pointerTouchDiv(int num, boolean hilite, boolean clockwise, int audioIndex) throws Exception
    {
        PointF clickLoc = OB_Maths.locationForRect(0.5f,0.8f,objectDict.get(String.format("divline_%d",num)).frame());
        PointF prevLoc = OB_Maths.locationForRect(0.5f,1f,objectDict.get(String.format("divline_%d",num)).frame());
        prevLoc.x = prevLoc.x+(objectDict.get("numberline").width()/18.0f);
        movePointerToPoint(prevLoc,-40+(num*5),0.5f,true);
        if(audioIndex>=0)
            playAudioScene("DEMO",audioIndex,true);
        waitForSecs(0.3f);
        movePointerToPoint(clickLoc,-40+(num*5),0.2f,true);
        if(hilite)
            ((OBLabel)objectDict.get(String.format("num_%d",num))).setColour(Color.RED);
        playSfxAudio("div_line",true);
        OC_Numberlines_Additions.animateCurve(num,clockwise,0.65f,true,false,this);
        waitForSecs(0.1f);
    }

    public void pointerPointEquation(OBGroup equation,int from, int to, List<String> audio, float duration) throws Exception
    {
        PointF point = OB_Maths.locationForRect(0.6f,1f,equation.objectDict.get(String.format("part%d",to)).getWorldFrame());
        point.y = equation.bottom() + equation.height()*0.2f;
        movePointerToPoint(point,-20,duration,true);
        OC_Numberlines_Additions.colourEquation(equation,from,to,Color.BLACK,this);
        playAudioQueued(OBUtils.insertAudioInterval(audio,300),true);
        OC_Numberlines_Additions.colourEquation(equation,from,to,eqColour,this);
    }

    public void demo4a() throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.85f,0.5f,objectDict.get("container1").frame()),-35,0.4f,"DEMO",0,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        spawnObject(objectDict.get("obj_1"),OB_Maths.locationForRect(0.3f,0.3f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_2"),OB_Maths.locationForRect(0.7f,0.3f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_3"),OB_Maths.locationForRect(0.3f,0.7f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        waitForSecs(0.3f);
        lockScreen();
        showControls("cnum_.*");
        unlockScreen();
        startScene();
    }

    public void demo4c() throws Exception
    {
        hideCnums();
        waitForSecs(0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,1,"numbers_on",this);
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        startScene();
    }

    public void demo4d() throws Exception
    {
        waitForSecs(0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),2,3,"numbers_on",this);
        waitForSecs(0.5f);
        lockScreen();
        showControls("cnum_.*");
        unlockScreen();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoEquation(boolean hidePointer) throws Exception
    {
        waitForSecs(0.5f);
        hideCnums();
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),4,5,"numbers_on",this);
        waitForSecs(0.3f);
        playAudioScene("FINAL",0,true);
        loadPointer(POINTER_LEFT);
        for(int i=1; i<6; i++)
            pointerPointEquation((OBGroup)objectDict.get("equation"),i,i,
                    Arrays.asList(getAudioForScene(currentEvent(),"FINAL").get(i)), i==1 ? 0.6f : 0.3f);
        if(hidePointer)
        {
            waitForSecs(0.3f);
            thePointer.hide();
        }
        waitForSecs(1f);
    }

    public void demoFin4d() throws Exception
    {
        demoEquation(false);
    }

    public void demo4e() throws Exception
    {
        movePointerToPoint(OB_Maths.locationForRect(0.85f,0.5f,objectDict.get("container1").frame()),-40,0.5f,true);
        List<OBControl> arr = new ArrayList<>();
        arr.addAll(filterControls("obj_.*"));
        arr.addAll(filterControls("aobj_.*"));
        arr.add(objectDict.get("equation"));
        arr.add(objectDict.get("eqbox"));
        arr.add(thePointer);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("top",bounds().height()*0.08f,objectDict.get("container1")),
                OBMisc.attachedAnim(objectDict.get("container1"),arr))
                ,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.2f,objectDict.get("equation").frame()),-20,0.5f,"DEMO",0,0.3f);
        playSfxAudio("number_line_on",false);
        OC_Numberlines_Additions.animateNumberLineShow(1, this);
        waitSFX();
        waitForSecs(0.4f);
        pointerPointEquation((OBGroup)objectDict.get("equation"),1,1,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(1),getAudioForScene(currentEvent(),"DEMO").get(2)),0.5f);
        waitForSecs(0.3f);
        playAudioScene("DEMO",3,false);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1f,objectDict.get("num_3").frame()),-45,0.5f,true);
        waitForAudio();
        waitForSecs(0.3f);
        playSfxAudio("scale0",false);
        objectDict.get("num_3").show();
        waitSFX();
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1f,this.bounds()),-30,0.5f,true);
        showNums(4,false);
        waitForSecs(0.3f);
        pointerPointEquation((OBGroup)objectDict.get("equation"),2,3
                ,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(4)),0.5f);
        waitForSecs(0.3f);
        PointF clickLoc = OB_Maths.locationForRect(0.5f,0.8f,objectDict.get("divline_3").frame());
        PointF prevLoc = OB_Maths.locationForRect(0.5f,1f,objectDict.get("divline_3").frame());
        prevLoc.x =  prevLoc.x+objectDict.get("numberline").width()/18.0f;
        playAudioScene("DEMO",5,false);
        movePointerToPoint(prevLoc,-45,0.5f,true);
        waitForAudio();
        waitForSecs(0.3f);
        movePointerToPoint(clickLoc,-45,0.2f,true);
        playSfxAudio("div_line",true);
        OC_Numberlines_Additions.animateCurve(3,true,0.65f, true, true, this);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        OC_Numberlines_Additions.resetNumberLine(false,false,this);
        nextScene();
    }

    public void demo4h() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_3").frame()),-45,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_3").frame()),-35,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_4").frame()),-40,0.5f,"DEMO",2,0.3f);
        for(int i=1; i<6; i++)        pointerPointEquation((OBGroup)objectDict.get("equation"),i,i
                ,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(i+2)),i==1? 0.5f : 0.3f);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        redrawScreen(OBUtils.getIntValue(eventAttributes.get("redraw")),false);
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo4i() throws Exception
    {
        spawnObject(objectDict.get("obj_1"),OB_Maths.locationForRect(0.2f,0.2f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_2"),OB_Maths.locationForRect(0.5f,0.2f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_3"),OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_4"),OB_Maths.locationForRect(0.2f,0.5f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.85f,0.5f,objectDict.get("container1").frame()),-35,0.4f,"DEMO",0,0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,1,"numbers_on",this);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,objectDict.get("num_4").frame()),-45,0.5f,"DEMO",2,0.3f);
        playSfxAudio("scale0",false);
        objectDict.get("num_4").show();
        waitSFX();
        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(0.5f);
        showNums(5,false);
        waitForSecs(0.5f);
        startScene();
    }

    public void demo4k() throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,2,4,"numbers_on",this);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        pointerPointEquation((OBGroup)objectDict.get("equation"),2,3
                ,getAudioForScene(currentEvent(),"DEMO"),0.5f);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();

    }

    public void demo4m() throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,5,5,"numbers_on",this);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_4").frame()),-45,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_4").frame()),-35,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_5").frame()),-30,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_6").frame()),-35,0.5f,"DEMO",3,0.3f);
        for(int i=1; i<6; i++)
            pointerPointEquation((OBGroup)objectDict.get("equation"),i,i
                    ,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(i+3)),i==1 ? 0.5f : 0.3f);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        nextScene();
    }

    public void demo5a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("container1").frame()),-35,0.4f,"DEMO",0,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        lockScreen();
        showControls("cnum_.*");
        unlockScreen();
        startScene();
    }

    public void demo5c() throws Exception
    {
        hideCnums();
        waitForSecs(0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,1,1,"numbers_on",this);
        waitForSecs(0.3f);
        showBag();
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        startScene();

    }

    public void demo5d() throws Exception
    {
        waitForSecs(0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,2,3,"numbers_on",this);
        waitForSecs(0.5f);
        lockScreen();
        showControls("cnum_.*");
        unlockScreen();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoFin5d() throws Exception
    {
        demoEquation(false);
    }

    public void demo5e() throws Exception
    {
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.8f,objectDict.get("container1").frame()),-40,0.5f,true);
        List<OBControl> arr = new ArrayList<>();
        arr.addAll(filterControls("obj_.*"));
        arr.add(objectDict.get("equation"));
        arr.add(objectDict.get("eqbox"));
        arr.add(thePointer);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("top",bounds().height()*0.08f,objectDict.get("container1")),
                OBMisc.attachedAnim(objectDict.get("container1"),arr))
                ,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        PointF bagloc = OB_Maths.locationForRect(0.8f,0.5f,this.bounds());
        objectDict.get("bag_front").setPosition(bagloc);
        objectDict.get("bag_back").setPosition(bagloc);
        objectDict.get("bag_drop").setPosition(bagloc);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.2f,objectDict.get("equation").frame()),-20,0.5f,"DEMO",0,0.3f);
        playSfxAudio("number_line_on",false);
        OC_Numberlines_Additions.animateNumberLineShow(1,this);
        waitSFX();
        waitForSecs(0.4f);
        pointerPointEquation((OBGroup)objectDict.get("equation"),1,1,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(1)),0.5f);
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,false);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1f,objectDict.get("num_5").frame()),-25,0.5f,true);
        waitForAudio();
        waitForSecs(0.3f);
        playSfxAudio("scale8",false);
        objectDict.get("num_5").show();
        waitSFX();
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.95f,this.bounds()),-30,0.5f,true);
        showNums(4,true);
        waitForSecs(0.3f);
        pointerPointEquation((OBGroup)objectDict.get("equation"),2,3
                ,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(3)),0.5f);
        waitForSecs(0.3f);
        PointF clickLoc = OB_Maths.locationForRect(0.5f,0.8f,objectDict.get("divline_5").frame());
        PointF prevLoc = OB_Maths.locationForRect(0.5f,1f,objectDict.get("divline_5").frame());
        prevLoc.x = prevLoc.x+objectDict.get("numberline").width()/18.0f;
        playAudioScene("DEMO",5,false);
        movePointerToPoint(prevLoc,-25,0.5f,true);
        waitForAudio();
        waitForSecs(0.3f);
        movePointerToPoint(clickLoc,-25,0.2f,true);
        playSfxAudio("div_line",true);
        OC_Numberlines_Additions.animateCurve(5,false,0.65f,true,true,this);
        waitForSecs(0.5f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.8f,objectDict.get("divline_4").frame()),-28,0.4f,true);
        playSfxAudio("div_line",true);
        OC_Numberlines_Additions.animateCurve(4,false,0.65f,true,true,this);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        OC_Numberlines_Additions.resetNumberLine(false,false,this);
        nextScene();
    }

    public void demo5i() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_5").frame()),-25,0.5f,"DEMO",0,0.3f);
        playAudioScene("DEMO",1,false);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_5").frame()),-30,0.4f,true);
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_4").frame()),-30,0.4f,true);
        waitForAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,true);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_3").frame()),-25,0.5f,"DEMO",3,0.3f);
        playAudioScene("DEMO",4,true);
        for(int i=1; i<6; i++)
            pointerPointEquation((OBGroup)objectDict.get("equation"),i,i,
                    Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(i+4)),i==1 ? 0.5f : 0.3f);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        redrawScreen(OBUtils.getIntValue(eventAttributes.get("redraw")),false);
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo5j() throws Exception
    {
        spawnObject(objectDict.get("obj_1"),OB_Maths.locationForRect(0.5f,0.2f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_2"),OB_Maths.locationForRect(0.3f,0.35f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_3"),OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_4"),OB_Maths.locationForRect(0.7f,0.35f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_5"),OB_Maths.locationForRect(0.3f,0.65f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_6"),OB_Maths.locationForRect(0.5f,0.8f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        spawnObject(objectDict.get("obj_7"),OB_Maths.locationForRect(0.7f,0.65f,objectDict.get("container1").frame()),0.3f,"egg_pop");
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("container1").frame()),-35,0.4f,"DEMO",0,0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,1,"numbers_on",this);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,objectDict.get("num_7").frame()),-45,0.5f,"DEMO",2,0.3f);
        playSfxAudio("scale8",false);
        objectDict.get("num_7").show();
        waitSFX();
        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(0.5f);
        showNums(6,true);
        waitForSecs(0.5f);
        showBag();
        startScene();
    }

    public void demo5l() throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,2,4,"numbers_on",this);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        pointerPointEquation((OBGroup)objectDict.get("equation"),2,3,
                Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(0),getAudioForScene(currentEvent(),"DEMO").get(1)),0.5f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_7").frame()),-20,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo5m() throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,5,5,"numbers_on",this);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("num_7").frame()),-45,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_7").frame()),-35,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_6").frame()),-30,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.6f,objectDict.get("linelabel_5").frame()),-30,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin5m() throws Exception
    {
        demoEquation(true);
        redrawScreen(OBUtils.getIntValue(eventAttributes.get("redraw")),true);
    }

    public void demo5n() throws Exception
    {
        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("container1").frame()),-35,0.4f,"DEMO",0,0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,1,"numbers_on",this);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,objectDict.get("num_4").frame()),-45,0.5f,"DEMO",2,0.3f);
        playSfxAudio("scale8",false);
        objectDict.get("num_4").show();
        waitSFX();
        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(0.5f);
        showNums(3,true);
        waitForSecs(0.5f);
        showBag();
        nextScene();
    }

    public void demo5p() throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation") ,2,4,"numbers_on",this);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        pointerPointEquation((OBGroup)objectDict.get("equation"),2,3,
                getAudioForScene(currentEvent(),"DEMO"),0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo5q() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,objectDict.get("equation").frame()),-25,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin5q() throws Exception
    {
        demoEquation(true);
    }

}
