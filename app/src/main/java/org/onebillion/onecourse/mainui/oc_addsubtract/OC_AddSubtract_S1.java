package org.onebillion.onecourse.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
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
 * Created by michal on 16/03/2017.
 */

public class OC_AddSubtract_S1 extends OC_SectionController
{
    Map<String,Integer> eventColour;
    List<OBControl> leftControls, rightControls, baggedItems;
    List<PointF> dropLocs;
    List<OBGroup> numbers;
    List<String> eqParts;
    boolean subtractMode;
    int currentIndex, currentPhase;
    int correct;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColour  = new ArrayMap<>();
        eventColour = OBMisc.loadEventColours(this);
        leftControls = new ArrayList<>();
        rightControls = new ArrayList<>();
        dropLocs = new ArrayList<>();
        baggedItems = new ArrayList<>();
        subtractMode = OBUtils.getBooleanValue(eventAttributes.get("subtract"));
        if(subtractMode)
        {
            objectDict.get("bag_back").setZPosition(20);
            objectDict.get("bag_front").setZPosition(22);
        }
        numbers = OBMisc.loadNumbersInBoxes(subtractMode ? 0 : 1, 10, eventColour.get("box"), Color.BLACK, "numbox", this);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        if(objectDict.get("box") != null)
            ((OBPath)objectDict.get("box")).sizeToBoundingBoxIncludingStroke();
        setSceneXX(currentEvent());
        showFirst();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(!performSel("demo",currentEvent()))
                    startPhase();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        currentIndex  = 0;
        currentPhase = subtractMode ? 0 : 1;
        eqParts = Arrays.asList(eventAttributes.get("equation").split(","));
        if(!subtractMode)
        {
            String locs1[] = eventAttributes.get("locs1").split(",");
            String locs2[] = eventAttributes.get("locs2").split(",");
            loadObjects(objectDict.get("obj_1"), locs1, leftControls);
            loadObjects(objectDict.get("obj_2"), locs2, rightControls);
        }
        else
        {
            baggedItems.clear();
            dropLocs.clear();
            String[] locs = eventAttributes.get("locs").split(",");
            for(int i = 0; i<locs.length; i+=2)
                dropLocs.add(OB_Maths.locationForRect( Float.valueOf(locs[i]), Float.valueOf(locs[i+1]),objectDict.get("bag_front").frame()));

            for(OBControl cont : filterControls("obj_.*"))
                cont.setAnchorPoint(new PointF(0.5f, 1));
        }

        String equation = String.format(subtractMode ? "%s – %s = %s" : "%s + %s = %s", eqParts.get(0), eqParts.get(1), eqParts.get(2));
        OC_Numberlines_Additions.loadEquation(equation,"equation",objectDict.get("eqbox"),eventColour.get("equation"),false,0,1,this);
        OC_Numberlines_Additions.hideEquation((OBGroup)objectDict.get("equation"),this);
    }

    public void doMainXX() throws Exception
    {
        showFirst();
        startPhase();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

                if(status() == STATUS_AWAITING_CLICK)
                {
                    if(currentPhase == 1)
                    {
                        if(subtractMode)
                        {
                            final OBControl cont = finger(0,1,filterControls("obj_.*"),pt);
                            if(cont != null && !baggedItems.contains(cont))
                            {
                                setStatus(STATUS_BUSY);
                                playAudio(null);
                                OBUtils.runOnOtherThread(
                                        new OBUtils.RunLambda()
                                        {
                                            @Override
                                            public void run() throws Exception
                                            {
                                                checkTarget(cont);
                                            }
                                        }
                                );
                            }
                        }
                        else if(objectDict.get("box").frame().contains( pt.x, pt.y))
                        {
                            setStatus(STATUS_BUSY);
                            playAudio(null);
                            OBUtils.runOnOtherThread(
                                    new OBUtils.RunLambda()
                                    {
                                        @Override
                                        public void run() throws Exception
                                        {
                                            checkTarget2(pt);
                                        }
                                    }
                            );
                        }
                    }
                    else
                    {
                        final OBControl box = finger(-1,-1,(List<OBControl>)(Object)numbers,pt);
                        if(box != null)
                        {
                            setStatus(STATUS_BUSY);
                            playAudio(null);
                            OBUtils.runOnOtherThread(
                                    new OBUtils.RunLambda()
                                    {
                                        @Override
                                        public void run() throws Exception
                                        {
                                            checkBox((OBGroup)box);
                                        }
                                    }
                            );
                        }

                    }

                }

        }

        public void checkTarget(OBControl cont) throws Exception
        {
            flyControlToBag(cont);
            if(currentIndex == correct)
            {
                finishItemPhase();
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }
        }

        public void checkTarget2(PointF pt) throws Exception
        {
            if(spawnRightControl(pt))
            {
                finishItemPhase();
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);

            }
        }

        public void checkBox(OBGroup box) throws Exception
        {
            box.objectDict.get("box").setBackgroundColor(eventColour.get("highlight"));
            if((int)box.settings.get("num_val") == correct)
            {
                if(currentPhase != 0)
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);

                    OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),4,5,"equation",this);
                    waitForSecs(0.3f);
                    performSel("demoFin",String.format("%s%d",currentEvent(),currentPhase));
                    demoFinEquation();
                    waitForSecs(0.5f);
                    if(currentEvent() != events.get(events.size()-1))
                        hideAll();

                    box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
                    waitForSecs(0.3f);
                    nextScene();
                }
                else
                {
                    gotItRight();
                    OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,1,"equation",this);
                    waitSFX();
                    performSel("demoFin",String.format("%s%d",currentEvent(),currentPhase));
                    box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
                    currentPhase++;
                    startPhase();
                }
            }
            else
            {
                gotItWrongWithSfx();
                long time = setStatus(STATUS_AWAITING_CLICK);
                waitSFX();
                box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
                if(time == statusTime)
                {
                    int phase = subtractMode ? currentPhase : 0;
                    playAudioQueuedScene(phase == 0 ? "INCORRECT" :  "INCORRECT2",0.3f,false);
                }
            }
        }


    public void startPhase() throws Exception
    {
        correct = OBUtils.getIntValue(eqParts.get(currentPhase));
        int phase = currentPhase;
        if(!subtractMode)
            phase--;
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK)
                ,phase == 0? "": String.format("%d",phase+1),this);
    }

    public void loadObjects(OBControl main,String[] locs, List<OBControl> arr)
    {

        for(int i=0; i < locs.length; i+=2)
        {
            OBControl cont = main.copy();
            cont.setPosition(OB_Maths.locationForRect(Float.valueOf(locs[i]),Float.valueOf(locs[i+1]),objectDict.get("box").frame()));
            attachControl(cont);
            cont.setZPosition(3);
            cont.hide();
            arr.add(cont);
        }
    }

    public boolean spawnRightControl(PointF pt) throws Exception
    {
        OBControl cont = rightControls.get(currentIndex);
        PointF startLoc = OBMisc.copyPoint(cont.position());
        cont.setPosition(pt);
        currentIndex++;
        boolean complete = false;
        if(currentIndex == rightControls.size())
            complete = true;

        cont.show();
        playSfxAudio("spawn",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(startLoc,cont)),0.3f
                ,complete,OBAnim.ANIM_EASE_OUT,this);
        return complete;
    }

    public void flyControlToBag(OBControl control) throws Exception
    {
        baggedItems.add(control);
        Path path = new Path();
        path.moveTo(control.position().x, control.position().y);
        PointF destLoc = dropLocs.get(currentIndex);
        currentIndex++;
        lockScreen();
        control.setZPosition(21 + 0.01f * currentIndex);
        if(OBUtils.getBooleanValue((String)control.attributes().get("top_zpos")))
            control.setZPosition(control.zPosition() + 0.3f);

        if(OBUtils.getBooleanValue((String)control.attributes().get("rescale")))
            control.setScale(objectDict.get("obj_1").scale());
        unlockScreen();

        float midX = (destLoc.x+control.position().x)/2.0f;
        float bottom = objectDict.get("numbox").bottom();
        path.cubicTo(midX, bottom, destLoc.x, bottom, destLoc.x, destLoc.y);
        playSfxAudio("takeaway",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(control,path,false,0)),0.4f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        PointF point = OBMisc.copyPoint(objectDict.get("bag_front").position());
        PointF leftPoint = OBMisc.copyPoint(point);
        leftPoint.x -= applyGraphicScale(7);
        PointF rightPoint = OBMisc.copyPoint(point);
        rightPoint.x += applyGraphicScale(7);
        List<OBControl> allControls = new ArrayList<>();
        allControls.addAll(baggedItems);
        allControls.add(objectDict.get("bag_back"));
        for(int i=0; i<2; i++)
        {
            OBMisc.moveControlWithAttached(objectDict.get("bag_front"),allControls
                    ,i%2 == 1 ? leftPoint : rightPoint ,0.03f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        }
        OBMisc.moveControlWithAttached(objectDict.get("bag_front"),allControls
                ,point,0.03f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

    }


    public void showFirst()
    {
        lockScreen();
        if(subtractMode)
        {
            showControls("obj_.*");
            showControls("bg_.*");
        }
        else
        {
            for(OBControl cont : leftControls)
                cont.show();

            OC_Numberlines_Additions.showEquation((OBGroup) objectDict.get("equation"), 1, 1, this);

        }
        unlockScreen();
    }


    public void hideAll()
    {
        lockScreen();
        if(subtractMode)
        {
            deleteControls("obj_.*");
            deleteControls("bg_.*");

        }
        else
        {
            List<OBControl> allControls = new ArrayList<>();
            allControls.addAll(leftControls);
            allControls.addAll(rightControls);
            for(OBControl con : allControls)
                detachControl(con);
            leftControls.clear();
            rightControls.clear();

        }
        detachControl(objectDict.get("equation"));
        unlockScreen();
    }

    public void finishItemPhase() throws Exception
    {
        waitForSecs(0.2f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),2,3,"equation",this);
        performSel("demoFin",String.format("%s%d",currentEvent(),currentPhase));
        currentPhase++;
        waitForSecs(0.3f);
        startPhase();
    }

    public void pointerPointEquationAt(int at, String audio, float duration) throws Exception
    {
        OBGroup equation = (OBGroup)objectDict.get("equation");
        PointF point = OB_Maths.locationForRect(0.6f,1f,equation.objectDict.get(String.format("part%d",at)).getWorldFrame());
        point.y =  equation.bottom() + equation.height()*0.2f;
        movePointerToPoint(point,-15,duration,true);
        OC_Numberlines_Additions.colourEquation(equation,at,at,Color.RED,this);
        playAudio(audio);
        waitAudio();
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(equation,at,at,eventColour.get("equation"),this);
    }

    public void demoFinEquation() throws Exception
    {
        for(int i=1; i<6; i++)
        {
            OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation"),i,i,Color.RED,this);
            playAudioScene("FINAL",i-1,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation"),i,i,eventColour.get("equation"),this);

        }

    }
    public void demo1b() throws Exception
    {
        demoButtons();
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("box").frame()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.75f,objectDict.get("box").frame()),-25,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,objectDict.get("box").frame()),-25,0.5f,"DEMO",2,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.76f,objectDict.get("box").frame()),0.2f,true);
        spawnRightControl(OBMisc.copyPoint(thePointer.position()));
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,objectDict.get("box").frame()),0.2f,true);
        waitSFX();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        rightControls.get(0).hide();
        currentIndex = 0;
        startPhase();
    }

    public void demoFin1b1() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("box").frame()),-25,0.5f,"DEMO2",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.2f,objectDict.get("numbox").frame()),-20,0.5f,"DEMO2",1,0.5f);
        thePointer.hide();
    }

    public void demoFin1b2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("box").frame()),-25,0.5f,"DEMO3",0,0.3f);
        pointerPointEquationAt(2,getAudioForSceneIndex(currentEvent(), "DEMO3",1), 0.6f);
        pointerPointEquationAt(4,getAudioForSceneIndex(currentEvent(), "DEMO3",2), 0.4f);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoFin1g2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("box").frame()),-25,0.5f,"DEMO3",0,0.3f);
        pointerPointEquationAt(2,getAudioForSceneIndex(currentEvent(), "DEMO3",1), 0.6f);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoFin2a2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointEquationAt(2,getAudioForSceneIndex(currentEvent(), "DEMO2",0), 0.6f);
        pointerPointEquationAt(4,getAudioForSceneIndex(currentEvent(), "DEMO2",1), 0.4f);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoFin2e() throws Exception
    {
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        pointerPointEquationAt(2,getAudioForSceneIndex(currentEvent(), "DEMO2",0), 0.6f);
        waitForSecs(0.5f);
        thePointer.hide();
    }


}
