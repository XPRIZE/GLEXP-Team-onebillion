package org.onebillion.onecourse.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 27/03/2017.
 */

public class OC_AddSubtract_S4 extends OC_SectionController
{
    List<List<PointF>> dropLocs;
    int currentBox, currentIndex;

    public float graphicScale()
    {
        return this.bounds().width() / 1024.0f;
    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        dropLocs = new ArrayList<>();
        dropLocs.add(new ArrayList<PointF>());
        dropLocs.add(new ArrayList<PointF>());
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        showPhase();
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
        if (OBUtils.getBooleanValue(eventAttributes.get("reload")))
        {
            int index = 0;
            for (int i = 1; i <= 2; i++)
            {
                dropLocs.get(i - 1).clear();
                OBControl eqBox = objectDict.get(String.format("eq_box_%d", i));
                String[] eqParts = ((String) eqBox.attributes().get("equation")).split(",");
                String equation = String.format("%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
                String equationName = String.format("equation_%d", i);
                OC_Numberlines_Additions.loadEquation(equation, equationName, eqBox, Color.BLACK, false, 0, 1, this);
                OC_Numberlines_Additions.hideEquation((OBGroup) objectDict.get(equationName), this);
                for (int j = 1; j <= Integer.valueOf(eqParts[1]); j++)
                {
                    OBGroup obj = (OBGroup) objectDict.get(String.format("drag_%d_%d", i, j));
                    dropLocs.get(i - 1).add(OBMisc.copyPoint(obj.position()));
                    PointF loc = OB_Maths.locationForRect(0.5f, 0.5f, objectDict.get("bottombar").frame());
                    loc.x -= (Integer.valueOf(eqParts[2]) - 1) * 0.75f * obj.width() - index * 1.5f * obj.width();
                    index++;
                    obj.setPosition(loc);
                    obj.setProperty("start_loc", OBMisc.copyPoint(loc));
                    obj.enable();
                    obj.show();

                }

            }

        }
        currentBox = OBUtils.getIntValue(eventAttributes.get("start"));
    }

    public void doMainXX() throws Exception
    {
        showPhase();
        startPhase(null);
    }

    public void fin()
    {

        try
        {
            waitForSecs(0.3f);
            goToCard(OC_AddSubtract_S4e.class, "event4");
        } catch (Exception e)
        {

        }
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl cont = finger(0, 1, filterControls("drag_.*"), pt);
            if (cont != null && cont.isEnabled())
            {
                setStatus(STATUS_BUSY);
                OBMisc.prepareForDragging(cont, pt, this);
                setStatus(STATUS_DRAGGING);

            }

        }

    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            final OBControl targ = target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDropTarget(targ);
                }
            });
        }

    }

    public void checkDropTarget(OBControl targ) throws Exception
    {
        if(objectDict.get(String.format("box_%d", currentBox)).frame().contains(targ.position().x, targ.position().y))
        {
            gotItRight();
            playSfxAudio("drop",false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(dropLocs.get(currentBox-1).get(currentIndex),targ))
                ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            targ.setZPosition(targ.zPosition() - 10);
            targ.disable();
            currentIndex++;
            if(currentIndex >= dropLocs.get(currentBox-1).size())
            {
                waitSFX();
                playAudio(null);
                waitForSecs(0.2f);
                OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get(String.format("equation_%d",currentBox)),2,5,"equation",this);
                waitForSecs(0.5f);
                if(currentBox==2)
                {
                    if(!performSel("demoFin",currentEvent()))
                        demoEquation(true);
                    waitForSecs(1f);
                    clearScene(currentEvent()==events.get(events.size()-1));
                    nextScene();

                }
                else
                {
                    currentBox = 2;
                    showPhase();
                    startPhase("2");
                }

            }
                else
            {
                setStatus(STATUS_WAITING_FOR_DRAG);

            }

        }
        else
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)targ.propertyValue("start_loc"),targ))
                ,0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            targ.setZPosition(targ.zPosition() - 10);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }

    }


    public void startPhase(String phase) throws Exception
    {
        currentIndex=0;
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_WAITING_FOR_DRAG)
                ,phase == null? "":phase,this);
    }


    public void clearScene(boolean withBar)
    {
        lockScreen();
        deleteControls("obj_.*");
        deleteControls("drag_.*");
        deleteControls("box_.*");
        deleteControls("equation_.*");
        if(withBar)            objectDict.get("bottombar").hide();

        unlockScreen();
    }

    public void showPhase()
    {
        lockScreen();
        if (currentBox == 1)
            showControls("drag_.*");
        showControls(String.format("obj_%d_.*", currentBox));
        objectDict.get(String.format("box_%d", currentBox)).show();
        OC_Numberlines_Additions.showEquation((OBGroup) objectDict.get(String.format("equation_%d", currentBox)), 1, 1,  this);
        unlockScreen();
    }

    public void demo4a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.4f,1.05f,objectDict.get("box_1").frame()),-15,0.6f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.05f,objectDict.get("box_1").frame()),-12,0.6f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("drag_1_1").frame()),-15,0.6f,"DEMO",2,0.3f);
        OBControl dragCon = objectDict.get("drag_1_1");
        OBMisc.moveControlWithAttached(dragCon,Arrays.asList(thePointer),dropLocs.get(0).get(0) ,0.6f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        dragCon.disable();
        playSfxAudio("drop",false);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,1.05f,objectDict.get("box_1").frame()),0.3f,true);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get(String.format("equation_%d",currentBox)),2,5,"equation",this);
        waitForSecs(0.5f);
        thePointer.hide();
        nextScene();
    }

    public void demo4b() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showPhase();
        waitForSecs(0.3f);
        startPhase(null);

    }

    public void demoEquation(boolean load) throws Exception
    {
        if(load)
            loadPointer(POINTER_LEFT);
        for(int i=1; i<=2; i++)
        {
            OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d", i));
            float duration = load?0.6f:0.4f;
            if(i!=1)
            {
                OBAudioManager.audioManager.prepareForChannel(getAudioForSceneIndex(currentEvent(),"FINAL",1), OBAudioManager.AM_MAIN_CHANNEL);
                duration = (float)OBAudioManager.audioManager.durationForChannel(OBAudioManager.AM_MAIN_CHANNEL);
                OBAudioManager.audioManager.playerForChannel(OBAudioManager.AM_MAIN_CHANNEL).play();

            }
            movePointerToPoint(OB_Maths.locationForRect(0.35f,1.15f,equation.frame()),-15,duration,true);
            if(i!=1)
            {
                waitAudio();
                waitForSecs(0.1f);

            }
            OC_Numberlines_Additions.colourEquation(equation,1,3,Color.RED,this);
            playAudioScene("FINAL",i==1?0:2,true);
            waitForSecs(0.1f);

        }
        lockScreen();
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_1"),1,3,Color.BLACK,this);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_2"),1,3,Color.BLACK,this);

        unlockScreen();
        movePointerToPoint(OB_Maths.locationForRect(0.8f,1.15f,objectDict.get("equation_2").frame()),-15,0.4f,true);
        lockScreen();
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_1"),4,5,Color.RED,this);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_2"),4,5,Color.RED,this);

        unlockScreen();
        playAudioScene("FINAL",3,true);
        waitForSecs(0.3f);
        lockScreen();
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_1"),4,5,Color.BLACK,this);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_2"),4,5,Color.BLACK,this);

        unlockScreen();
        waitForSecs(0.5f);
        thePointer.hide();

    }

    public void demoFin4b() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.95f,0.4f,this.bounds()),-10,0.5f,"DEMO2",0,0.3f);
        for(int i=1;
            i<=2;
            i++)
        {
            OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d", i));
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.15f,equation.frame()),-15,0.4f,true);
            OC_Numberlines_Additions.colourEquation(equation,1,5,Color.RED,this);
            playAudioScene("DEMO2",i,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation(equation,1,5,Color.BLACK,this);

        }
        demoEquation(false);

    }


}
