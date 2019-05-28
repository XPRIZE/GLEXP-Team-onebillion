package com.maq.xprize.onecourse.hindi.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.mainui.oc_countmore.OC_CountMore_S6k;
import com.maq.xprize.onecourse.hindi.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.OB_MutInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 28/03/2017.
 */

public class OC_AddSubtract_S5 extends OC_SectionController
{
    List<OBGroup> screenObjs;
    Map<String, Integer> eventColour;
    List<OBGroup> numbers;
    int phase, correct;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColour = new ArrayMap<>();
        eventColour = OBMisc.loadEventColours(this);
        screenObjs = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        try
        {
            showFirstPhase();
        } catch (Exception e)
        {

        }
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5a();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        clearScene();
        super.setSceneXX(scene);
        if(eventAttributes.get("nums") != null)
        {
            String[] nums = eventAttributes.get("nums").split(",");
            if(numbers != null)
            {
                for(OBControl number : numbers)
                    detachControl(number);

            }
            numbers = OBMisc.loadNumbersInBoxes(Integer.valueOf(nums[0]),Integer.valueOf(nums[1]), eventColour.get("box"), Color.BLACK, "numbox", this);
        }
        screenObjs.clear();
        OBControl  eqBox = objectDict.get("eqbox");
        String[] eqParts = eventAttributes.get("equation").split(",");
        String equation = String.format("%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
        OC_Numberlines_Additions.loadEquation(equation,"equation",eqBox,Color.BLACK,false,1.5f,1,this);
        OBGroup eq = (OBGroup)objectDict.get("equation");
        eq.setAnchorPoint(OB_Maths.relativePointInRectForLocation(eq.objectDict.get("part2").getWorldPosition() , eq.frame()));
        PointF loc = OBMisc.copyPoint(eq.position());
        loc.x = bounds().width()*0.5f;
        eq.setPosition(loc);
        OC_Numberlines_Additions.hideEquation(eq,this);
        OBGroup obj = (OBGroup)objectDict.get("obj");
        String[] locs = eventAttributes.get("locs").split(",");
        List<OBControl> groupObjs = new ArrayList<>();
        for(int i=0; i<locs.length; i+=2)
        {
            OBControl copy = obj.copy();
            copy.show();
            copy.setPosition(OB_Maths.locationForRect(Float.valueOf(locs[i]),Float.valueOf(locs[i+1]), objectDict.get("box").frame()));
            groupObjs.add(copy);
        }
        OBGroup fullGroup = new OBGroup(groupObjs);
        fullGroup.setZPosition ( 5);
        fullGroup.hide();
        attachControl(fullGroup);
        screenObjs.add(fullGroup);
        PointF screenLoc = OB_Maths.relativePointInRectForLocation(fullGroup.position(), new RectF(bounds()));
        screenLoc.x =  1-screenLoc.x;
        OBGroup fullGroupCopy = (OBGroup)fullGroup.copy();
        fullGroupCopy.setPosition(OB_Maths.locationForRect(screenLoc.x,screenLoc.y,this.bounds()));
        fullGroupCopy.setZPosition(5);
        attachControl(fullGroupCopy);
        screenObjs.add(fullGroupCopy);
        OBPath line = (OBPath)objectDict.get("line");
        line.sizeToBox(new RectF(bounds()));
        line.sizeToBoundingBoxIncludingStroke();
        line.setWidth(0.8f*fullGroupCopy.width());
        line.setPosition(OB_Maths.locationForRect(0.5f, 0.9f, fullGroupCopy.frame()));
        phase=1;
        correct = Integer.valueOf(eqParts[2]);
    }

    public void doMainXX() throws Exception
    {
        showFirstPhase();
        startPhase();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            goToCard(OC_AddSubtract_S5k.class, "event5");
        }
        catch (Exception e)
        {

        }
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(phase==1)
            {
                if(finger(0,1,Arrays.asList(objectDict.get("line")),pt)!=null ||
                        screenObjs.get(screenObjs.size()-1).frame().contains( pt.x, pt.y))
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkTargetPhase1();
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
                                    checkTargetPhase2((OBGroup)box);
                                }
                            }
                    );


                }

            }

        }
    }

    public void checkTargetPhase1() throws Exception
    {
        showSecondPhase();
        waitForSecs(0.3f);
        phase = 2;
        startPhase();
    }

    public void checkTargetPhase2(OBGroup box) throws Exception
    {
        box.objectDict.get("box").setBackgroundColor(eventColour.get("highlight"));
        if((int)box.settings.get("num_val") == correct)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
            OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),4,5,"equation",this);
            waitForSecs(0.3f);
            playAudioQueuedScene("FINAL",0.3f,true);
            waitForSecs(0.8f);
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }

    public void startPhase() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK)
                ,phase == 1? "": String.format("%d",phase),this);

    }

    public void clearScene()
    {
        lockScreen();
        if(screenObjs.size() >0)
        {
            detachControl(screenObjs.get(0));
            detachControl(screenObjs.get(screenObjs.size()-1));

        }
        if(objectDict.get("equation") != null)
            detachControl(objectDict.get("equation"));
        if(objectDict.get("line") != null)
            objectDict.get("line").hide();

        unlockScreen();

    }

    public void showFirstPhase() throws Exception
    {
        lockScreen();
        screenObjs.get(0).show();
        objectDict.get("line").show();
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,1,null,this);
        unlockScreen();
    }

    public void showSecondPhase() throws Exception
    {
        lockScreen();
        screenObjs.get(screenObjs.size()-1).show();
        objectDict.get("line").hide();
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),2,3,null,this);
        playSfxAudio("equation",false);
        unlockScreen();
        waitSFX();
    }

    public void demo5a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.7f,screenObjs.get(0).frame()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation")).getWorldFrame()),-30,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.1f,screenObjs.get(screenObjs.size()-1).frame()),-20,0.5f,"DEMO",2,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1f,screenObjs.get(screenObjs.size()-1).frame()),-20,0.2f,true);
        showSecondPhase();
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,screenObjs.get(screenObjs.size()-1).frame()),-20,0.2f,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        OBGroup box = numbers.get(1);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,box.getWorldFrame()),-15,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.7f,box.getWorldFrame()),-15,0.2f,true);
        lockScreen();
        box.objectDict.get("box").setBackgroundColor(eventColour.get("highlight"));
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),4,5,null,this);
        playSfxAudio("equation",false);

        unlockScreen();
        waitSFX();
        box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,box.getWorldFrame()),-15,0.2f,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.2f,objectDict.get("equation").frame()),-25,0.6f,"DEMO",4,0.5f);
        thePointer.hide();
        waitForSecs(0.8f);
        nextScene();

    }


}
