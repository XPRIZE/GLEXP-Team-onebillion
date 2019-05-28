package com.maq.xprize.onecourse.hindi.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimBlock;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.UPath;
import com.maq.xprize.onecourse.hindi.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 27/03/2017.
 */

public class OC_AddSubtract_S4e extends OC_SectionController
{
    OBGroup selectedEquation;
    boolean animateShake;
    List<OBControl> targets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        targets = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        for(OBControl con : filterControls("box_.*"))
            ((OBPath)con).sizeToBoundingBoxIncludingStroke();

        animateShake = false;
    }


    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo4e();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(OBUtils.getBooleanValue(eventAttributes.get("reload")))
        {
            targets.clear();
            for (int i = 1; i <= 6; i++)
            {
                OBControl eqBox = objectDict.get(String.format("eq_box_%d", i));
                String[] eqParts = ((String) eqBox.attributes().get("equation")).split(",");
                String equation = String.format("%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);

                String equationName = String.format("equation_%d", i);
                OC_Numberlines_Additions.loadEquation(equation, equationName, eqBox, Color.BLACK, false, 0, 1, this);
                OBGroup eq = (OBGroup) objectDict.get(equationName);
                eq.setAnchorPoint(OB_Maths.relativePointInRectForLocation(eq.objectDict.get("part3").getWorldPosition(), eq.frame));
                OC_Numberlines_Additions.hideEquation(eq, this);
                OC_Numberlines_Additions.showEquation(eq, 1, 3, this);
                eq.setProperty("num_val", Integer.valueOf(eqParts[2]));
                eq.setProperty("colour", eqBox.fillColor());
                eq.setProperty("start_loc", OBMisc.copyPoint(eq.position()));
                targets.add(eq);
                eq.enable();
            }
        }
        selectedEquation = null;

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {


        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl equation = finger(0,1,targets,pt);
            if(equation != null && equation.isEnabled())
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(
                        new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkTarget((OBGroup)equation);
                            }
                        }
                );
            }
        }
    }


    public void checkTarget(OBGroup equation) throws Exception
    {
        if(selectedEquation == null)
        {
            selectEquation(equation);
            selectedEquation = equation;
            setStatus(STATUS_AWAITING_CLICK);

        }
        else if((int)equation.propertyValue("num_val") == (int)selectedEquation.propertyValue("num_val"))
        {
            selectEquation(equation);
            waitAudio();
            lockScreen();
            OC_Numberlines_Additions.showEquation(selectedEquation,4,5,null,this);
            OC_Numberlines_Additions.showEquation(equation,4,5,null,this);

            unlockScreen();
            waitForSecs(0.3f);
            gotItRightBigTick(true);
            waitForSecs(0.1f);
            lockScreen();
            OC_Numberlines_Additions.colourEquation(equation,1,5,(int)equation.propertyValue("colour"),this);
            OC_Numberlines_Additions.colourEquation(selectedEquation,1,5,(int)equation.propertyValue("colour"),this);
            unlockScreen();
            nextScene();

        }
        else
        {
            long time = setStatus(STATUS_AWAITING_CLICK);
            gotItWrongWithSfx();
            waitSFX();
            waitForSecs(0.3f);
            if(time == statusTime)
                playAudioScene("INCORRECT",0,false);

        }
    }

    public void selectEquation(OBGroup equation) throws Exception
    {
        equation.disable();
        OC_Numberlines_Additions.colourEquation(equation,1,5,Color.RED,this);
        playSfxAudio("touch", false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)equation.propertyValue("start_loc"),equation))
            ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

    }

    public void stopAnimation()
    {
        killAnimations();
    }

    public void startScene() throws Exception
    {
        if(OBUtils.getBooleanValue(eventAttributes.get("reload")))
            startAnimation();

        long  time = setStatus(STATUS_AWAITING_CLICK);
        OBMisc.doSceneAudio(-1,time,this);

    }


    public void startAnimation()
    {
        List<Integer> angleArray = OBUtils.randomlySortedArray(Arrays.asList(0,60,120,180,240,300));
        final Map<OBGroup,List<PointF>> rotationPoints = new ArrayMap<>();
        final Map<OBGroup,List<Integer>> rotationAngles = new ArrayMap<>();
        final Map<OBGroup,Float> rotationRadius = new ArrayMap<>();

        for(int i=0; i<targets.size(); i++)
        {
            List<Integer> angles = new ArrayList<>();
            List<PointF> points = new ArrayList<>();
            OBGroup equation = (OBGroup)targets.get(i);

            int angle =(int)angleArray.get(i);
            float radius = OB_Maths.randomInt(1,2)/1.0f * equation.height()/6.0f;
            for(int j=1; j<OB_Maths.randomInt(40, 100); j++)
            {
                angle += 30;
                PointF loc = OBMisc.copyPoint(equation.position());
                loc.x -= Math.cos((float)Math.toRadians(angle))*radius;
                loc.y -= Math.sin((float)Math.toRadians(angle))*radius;
                points.add(loc);
                angles.add(angle);
            }
            rotationRadius.put(equation,radius);
            rotationPoints.put(equation, points);
            rotationAngles.put(equation, angles);
        }


        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                if(_aborting)
                    killAnimations();

                for(int i=0; i<targets.size(); i++)
                {
                    OBGroup equation = (OBGroup)targets.get(i);
                    List<PointF> points = rotationPoints.get(equation);
                    List<Integer> angles = rotationAngles.get(equation);
                    float radius = rotationRadius.get(equation);

                    if(!equation.isEnabled())
                        continue;

                    float semifrac = OB_Maths.easeout((float)((double)frac % (double)(1.0/points.size()))*points.size());
                    semifrac *= semifrac;
                    int index = (int)Math.floor(frac*points.size());
                    int currentIndex = index>=points.size()?points.size()-1:index;
                    PointF currentPoint = points.get(currentIndex);
                    if(!equation.isEnabled())
                        continue;

                    equation.setPosition(new PointF(currentPoint.x + radius*(float)Math.cos(Math.toRadians(angles.get(currentIndex)+semifrac*360)),
                            currentPoint.y + radius*(float)Math.sin(Math.toRadians(angles.get(currentIndex)+semifrac*360))));

                }
            }
        };

        OBAnimationGroup ag = new OBAnimationGroup();
        ag.applyAnimations(Arrays.asList(anim),100,false,OBAnim.ANIM_LINEAR,-1,null, this);
        registerAnimationGroup(ag, "gameLoop");
    }


    public void moveEquationsToBoxes() throws Exception
    {
        waitForSecs(0.3f);
        List<OBAnim> anims = new ArrayList<>();
        for(int i=1; i<=3; i++)
        {
            OBControl box = objectDict.get(String.format("box_%d",i));
            for(int j=1; j<=2; j++)
            {
                OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d",(i-1)*2 + j));
                anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.5f,0.25f + (j-1)*0.5f,box.frame()),equation));
            }
        }
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
    }

    public void demoEquations(boolean full,boolean withEnd) throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        for(int boxNum=1; boxNum<=3; boxNum++)
        {
            OBControl box = objectDict.get(String.format("box_%d",boxNum));
            box.show();
            String audioName = String.format("FINAL%d",boxNum+1);
            List<OBGroup> eqArray = new ArrayList<>();
            for(int i=1; i<=2; i++)
            {
                OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d", (boxNum-1)*2 + i));
                eqArray.add(equation);
                movePointerToPoint(OB_Maths.locationForRect(0.35f,1.15f,equation.frame()),-15,i==1?0.7f:0.4f,true);
                OC_Numberlines_Additions.colourEquation(equation,1,full?5:3,Color.RED,this);
                playAudioScene(audioName,i-1,true);
                waitForSecs(0.1f);
            }
            int eqColour = (int)eqArray.get(0).propertyValue("colour");
            lockScreen();
            for(OBGroup equation : eqArray)
                OC_Numberlines_Additions.colourEquation(equation,1,5,eqColour,this);

            unlockScreen();
            if(withEnd)
            {
                movePointerToPoint(OB_Maths.locationForRect(0.8f,1.15f,eqArray.get(eqArray.size()-1).frame()),-15,0.4f,true);
                lockScreen();
                for(OBGroup equation : eqArray)
                    OC_Numberlines_Additions.colourEquation(equation,4,5,Color.RED,this);

                unlockScreen();
                playAudioScene(audioName,2,true);
                waitForSecs(0.3f);
                lockScreen();
                for(OBGroup equation : eqArray)
                    OC_Numberlines_Additions.colourEquation(equation,4,5,eqColour,this);

                unlockScreen();
            }
            else
            {
                waitForSecs(0.5f);
            }
            box.hide();
        }
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        if(currentEvent() != events.get(events.size()-1))
        {
            lockScreen();
            deleteControls("equation_.*");
            deleteControls("eq_box_.*");
            unlockScreen();
        }
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo4e() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.15f,0.6f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        for(int i=1; i<=2; i++)
        {
            String eqName = String.format("equation_%d",i);
            movePointerToPoint(OB_Maths.locationForRect(0.3f,1.1f,objectDict.get(eqName).frame()),-15,0.5f,true);
            movePointerToPoint(OB_Maths.locationForRect(0.3f,0.5f,objectDict.get(eqName).frame()),-15,0.2f,true);
            playSfxAudio("touch",false);
            OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get(eqName),1,5,Color.RED,this);
            waitSFX();
            moveScenePointer(OB_Maths.locationForRect(0.3f,1.1f,objectDict.get(eqName).frame()),-15,0.2f,"DEMO",i,0.3f);
        }
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.4f,objectDict.get("equation_2").frame()),-15,0.4f,"DEMO",3,0.3f);
        lockScreen();
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation_1"),4,5,null,this);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation_2"),4,5,null,this);
        playAudio("correct");

        unlockScreen();
        waitForAudio();
        waitForSecs(0.5f);
        lockScreen();
        int eqColour = (int)objectDict.get("equation_1").propertyValue("colour");
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_1"),1,5,eqColour,this);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation_2"),1,5,eqColour,this);

        unlockScreen();
        targets.remove(objectDict.get("equation_1"));
        targets.remove(objectDict.get("equation_2"));
        waitForSecs(0.3f);
        thePointer.hide();
        startAnimation();
        nextScene();
    }

    public void demo4h() throws Exception
    {
        stopAnimation();
        moveEquationsToBoxes();
        waitForSecs(0.3f);
        demoEquations(false, true);
    }

    public void demo4l() throws Exception
    {
        stopAnimation();
        moveEquationsToBoxes();
        waitForSecs(0.3f);
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(0.3f);
        demoEquations(true, false);
    }

    public void demo4p() throws Exception
    {
        stopAnimation();
        moveEquationsToBoxes();
        waitForSecs(0.3f);
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(0.3f);
        demoEquations(false, false);
    }

}
