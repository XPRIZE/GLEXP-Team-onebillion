package com.maq.xprize.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 15/03/2017.
 */

public class OC_CountMore_S6 extends OC_SectionController
{
    Map<String,Integer> eventColours;
    List<OBControl> eventObjs;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColours = new ArrayMap<>();
        eventObjs = new ArrayList<>();
        eventColours = OBMisc.loadEventColours(this);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo6a();
            }

        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(eventAttributes.get("start") != null )
            loadNumbers(OBUtils.getIntValue(eventAttributes.get("start")));

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.8f);
            goToCard(OC_CountMore_S6k.class, "event6");
        }
        catch (Exception e)
        {

        }
    }


    public void touchDownAtPoint(final PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl cont = finger(0, 1, eventObjs, pt);
            if (cont != null && !cont.isSelected())
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget((OBGroup)cont);
                    }
                });
            }
        }
    }

    public void checkTarget(OBGroup cont) throws Exception
    {
        colourCircle(cont, eventColours.get("highlight2"));
        if(eventAttributes.get("target").equals("even") == (boolean)cont.propertyValue("even"))
        {
            gotItRightBigTick(true);
            cont.select();
            waitForSecs(0.3f);
            int num = (int)eventObjs.indexOf(cont);
            playAudioScene("FINAL",num%2==0 ? num/2 : (num+1)/2 - 1 ,true);
            waitForSecs(0.3f);
            performSel("finDemo", currentEvent());
            if(OBUtils.getBooleanValue(eventAttributes.get("delete")))
            {
                waitForSecs(0.6f);
                lockScreen();
                for(OBControl con : eventObjs)
                    detachControl(con);
                unlockScreen();
                waitForSecs(0.5f);
            }
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            colourCircle(cont, Color.WHITE);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,true);

        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4, setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void loadNumbers(int start)
    {
        for(int i=1; i<11; i++)
        {
            OBPath circle = (OBPath)objectDict.get(String.format("obj_%d", i));
            circle.sizeToBoundingBoxIncludingStroke();
            OBControl copy = circle.copy();
            OBLabel label = new OBLabel(String.format("%d", start+i-1), OBUtils.standardTypeFace(),60.0f* circle.height()/84.0f);
            label.setColour(eventColours.get("num"));
            OBGroup numGroup =  new OBGroup(Arrays.asList((OBControl)label));
            numGroup.sizeToTightBoundingBox();
            numGroup.setPosition(OBMisc.copyPoint(circle.position()));
            copy.show();
            copy.setZPosition(1);
            numGroup.setZPosition(2);
            OBGroup full = new OBGroup(Arrays.asList(copy,numGroup));
            full.objectDict.put("background",copy);
            attachControl(full);
            full.setProperty("even", i%2 == 0 ? true : false);
            if(eventObjs.size()>i-1)
            {
                full.setPosition(OBMisc.copyPoint(eventObjs.get(i-1).position()));
                eventObjs.set(i-1, full);
            }
            else
            {
                eventObjs.add(full);
            }
            full.hide();
            full.enable();
        }
    }

    public void colourCircle(OBGroup group,int colour)
    {
        ((OBPath)group.objectDict.get("background")).setFillColor(colour);
    }

    public void hilightEvens(boolean on)
    {
        lockScreen();
        for(int i=0; i<5; i++)
            colourCircle((OBGroup)eventObjs.get(i*2 + 1), on?eventColours.get("highlight"):Color.WHITE);

        unlockScreen();

    }

    public void demoCount() throws Exception
    {
        for(int i=0; i<eventObjs.size(); i++)
        {
            eventObjs.get(i).show();
            playAudioScene("DEMO2",i,true);
            waitForSecs(0.1f);
        }
    }

    public void demoCount2() throws Exception
    {
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.65f,this.bounds()),-20,0.5f,"DEMO3",0,0.3f);
        for(int i=0; i<5; i++) {
            OBGroup group = (OBGroup)eventObjs.get((i*2+1));
            movePointerToPoint(OB_Maths.locationForRect(0.8f,1f,group.frame()),-15 - i*4,i==0 ? 0.5f : 0.3f,true);
            colourCircle(group, eventColours.get("highlight"));
            playAudioScene("DEMO3",1+i,true);
            waitForSecs(0.1f);

        }

    }
    public void demoColumns(String audio, int count) throws Exception
    {
        for(int i=0; i<count; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(1f,0f,eventObjs.get(i==0?1:0).frame()),i==0?-15:-20,0.5f,true);
            playAudioScene(audio,i,false);
            movePointerToPoint(OB_Maths.locationForRect(1f,1f,eventObjs.get(i==0?9:8).frame()),-30,2f,true);
            waitForAudio();
            waitForSecs(0.3f);
        }
        for(OBControl con : eventObjs)
            con.enable();
    }

    public void demo6a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-25,0.5f,"DEMO",0,0.3f);
        demoCount();
        demoCount2();
        List<OBAnim> anims= new ArrayList<>();
        for(int i=0; i<eventObjs.size(); i++)
        {
            OBGroup group = (OBGroup)eventObjs.get(i);
            anims.add(OBAnim.propertyAnim("right",i%2 == 0 ? group.right() - group.width()*0.7f : group.right() + group.width()*0.7f,group));

        }
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        demoColumns("DEMO4",2);
        waitForSecs(0.2f);
        thePointer.hide();
        hilightEvens(false);
        nextScene();
    }

    public void finDemo6c() throws Exception
    {
        waitForSecs(0.5f);
        hilightEvens(true);
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        demoColumns("FINAL2",1);
        waitForSecs(0.2f);
        thePointer.hide();
        hilightEvens(false);

    }

    public void demo6f() throws Exception
    {
        demoCount();
        loadPointer(POINTER_LEFT);
        demoCount2();
        demoColumns("DEMO4",2);
        waitForSecs(0.2f);
        thePointer.hide();
        hilightEvens(false);
        nextScene();
    }

    public void finDemo6h() throws Exception
    {
        waitForSecs(0.5f);
        hilightEvens(true);
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        demoColumns("FINAL2",2);
        waitForSecs(0.2f);
        thePointer.hide();
        hilightEvens(false);
    }

}
