package org.onebillion.onecourse.mainui.oc_2dshapes;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 07/03/2018.
 */

public class OC_2dShapes_S6p extends OC_SectionController
{
    List<OBControl> eventTargets, eventPots;
    Map<String, OBControl> tagPots;
    OBGroup currentPot;
    int currentColour;
    boolean limitedMode;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        eventTargets = new ArrayList<>();
        tagPots = new ArrayMap<>();
        eventPots = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo6p();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        deleteControls("obj_.*");
        deleteControls("frame_.*");
        deleteControls("pot_.*");
        tagPots.clear();
        eventTargets.clear();
        eventPots.clear();
        super.setSceneXX(scene);
        eventTargets.addAll(filterControls("obj_.*"));
        eventPots.addAll(filterControls("pot_.*"));

        for(OBControl con : filterControls("obj_.*"))
            if(con instanceof OBPath)
                ((OBPath)con).sizeToBoundingBoxIncludingStroke();

        for(OBControl con : filterControls("frame_.*"))
            if(con instanceof OBPath)
                ((OBPath)con).sizeToBoundingBoxIncludingStroke();
        currentPot = null;
        currentColour = 0;
        limitedMode = OBUtils.getBooleanValue(eventAttributes.get("limited"));
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl pot = finger(0,1,eventPots,pt);


            if (pot != null && pot != currentPot)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkPot((OBGroup) pot);
                    }
                });
            }

            else if(currentPot != null)
            {
                final OBControl targ = finger(-1,-1,eventTargets,pt);
                if(targ != null )
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkPart(targ);
                        }
                    });
                }
            }
        }
    }

    public void checkPot(OBGroup pot) throws Exception
    {
        if(limitedMode)
        {
            if(tagPots.values().size() == 2 && !tagPots.values().contains(pot))
            {
                gotItWrongWithSfx();
                long time = setStatus(STATUS_AWAITING_CLICK);
                waitSFX();
                if(time == statusTime)
                    playAudioQueuedScene("INCORRECT2",300,false);
            }
            else
            {
                selectPot(pot);
                setStatus(STATUS_AWAITING_CLICK);
            }

        }
        else
        {
            selectPot(pot);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void checkPart(OBControl targ) throws Exception
    {
        String tag = (String)targ.attributes().get("tag");
        if(tagPots.values().contains(currentPot) && tagPots.get(tag) == null)
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT2",300,false);

        }
        else if(tagPots.get(tag) != null && tagPots.get(tag) != currentPot)
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",300,false);

        }
        else
        {
            if(tagPots.get(tag) == null)
                tagPots.put(tag,currentPot);
            lockScreen();
            targ.setFillColor(currentColour);
            playSfxAudio("colour_fill",false);

            unlockScreen();
            eventTargets.remove(targ);
            gotItRight();
            if(eventTargets.size() == 0)
            {
                waitSFX();
                waitForSecs(0.4f);
                displayTick();
                waitForSecs(1f);
                playAudioQueuedScene("FINAL",300,true);
                waitForSecs(1f);
                nextScene();
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }

        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void selectPot(OBGroup pot) throws Exception
    {
        lockScreen();
        if(currentPot != null)
            currentPot.objectDict.get("selector_frame").hide();
        pot.objectDict.get("selector_frame").show();
        playSfxAudio("pot",false);

        unlockScreen();
        currentPot = pot;
        currentColour = OBUtils.colorFromRGBString((String)pot.attributes().get("fill"));
    }

    public void pointerClickOnPot(int potNum) throws Exception
    {
        OBGroup pot =(OBGroup ) objectDict.get(String.format("pot_%d",potNum));
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,pot.frame()),-10,0.6f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,pot.frame()),-10,0.25f,true);
        selectPot(pot);
        waitSFX();
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,pot.frame()),-10,0.25f,true);
    }

    public void pointerColourShape(String shapeName,float x,float y) throws Exception
    {
        OBControl shape =objectDict.get(shapeName);
        PointF movePoint = OB_Maths.locationForRect(x,y+0.5f,shape.frame());
        float rotate = -10 - 20*(this.bounds().width() - movePoint.x)/this.bounds().width();
        movePointerToPoint(movePoint,rotate,0.6f,true);
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.locationForRect(x,y,shape.frame()),rotate,0.25f,true);
        lockScreen();
        shape.setFillColor(currentColour);
        playSfxAudio("colour_fill",false);

        unlockScreen();
        waitSFX();
        movePointerToPoint(movePoint,rotate,0.25f,true);

    }

    public void demo6p() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.9f,1.05f,objectDict.get("frame_1") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.25f,0.75f,objectDict.get("obj_1_1") .frame()),-30,0.7f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.75f,0.25f,objectDict.get("obj_1_2") .frame()),-25,0.4f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_3") .frame()),-20,0.4f,"DEMO",3,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0f,0f,objectDict.get("frame_1") .frame()),-35,0.6f,true);
        playAudioScene("DEMO",4,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("frame_1") .frame()),-35,2,true);
        waitForAudio();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.1f,1.4f,objectDict.get("pot_3") .frame()),-10,0.4f,"DEMO",5,0.3f);
        pointerClickOnPot(1);
        pointerColourShape("obj_1_1",0.25f,0.75f);
        pointerColourShape("obj_1_4",0.25f,0.75f);
        pointerClickOnPot(2);
        pointerColourShape("obj_1_2",0.75f,0.25f);
        pointerColourShape("obj_1_5",0.75f,0.25f);
        pointerClickOnPot(3);
        pointerColourShape("obj_1_3",0.5f,0.5f);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        lockScreen();
        objectDict.get("obj_1_1").setFillColor(Color.WHITE);
        objectDict.get("obj_1_2").setFillColor(Color.WHITE);
        objectDict.get("obj_1_3").setFillColor(Color.WHITE);
        objectDict.get("obj_1_4").setFillColor(Color.WHITE);
        objectDict.get("obj_1_5").setFillColor(Color.WHITE);
        currentPot.objectDict.get("selector_frame").hide();
        currentPot = null;

        unlockScreen();
        waitForSecs(0.5f);
        startScene();
    }

    public void demo6r() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.9f,1.05f,objectDict.get("frame_1") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_1") .frame()),-30,0.7f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_2") .frame()),-27,0.4f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_2_1") .frame()),-30,0.4f,"DEMO",3,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demo6s() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.9f,1.05f,objectDict.get("frame_1") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_1") .frame()),-30,0.7f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_2") .frame()),-27,0.4f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_3") .frame()),-30,0.4f,"DEMO",3,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demo6t() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.9f,this.bounds()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("obj_1_1") .frame()),-30,0.7f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.65f,objectDict.get("obj_1_2") .frame()),-25,0.4f,"DEMO",2,0.3f);
        playAudioScene("DEMO",3,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("obj_1_4") .frame()),-25,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("obj_2_2") .frame()),-30,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("obj_2_6") .frame()),-20,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("obj_3_2") .frame()),-25,0.5f,true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }




}
