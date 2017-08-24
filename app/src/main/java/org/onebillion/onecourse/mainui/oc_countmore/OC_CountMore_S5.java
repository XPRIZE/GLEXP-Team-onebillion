package org.onebillion.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 15/03/2017.
 */

public class OC_CountMore_S5 extends OC_SectionController
{
    int correct;
    OBLabel topLabel;
    List<PointF> dropLocs;
    Map<Integer, OBGroup> dropObjs;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        topLabel = new OBLabel("888", OBUtils.standardTypeFace(), 50.0f * objectDict.get("loop").height() / 250.0f);
        topLabel.setColour(Color.BLACK);
        topLabel.setPosition(OB_Maths.locationForRect(0.5f, 1.1f, objectDict.get("loop").frame()));
        topLabel.setBottom(objectDict.get("loop").top() - topLabel.height() * 0.1f);
        dropLocs = new ArrayList<>();
        dropObjs = new ArrayMap<>();
        String[] locs = eventAttributes.get("locs").split(",");
        for (int i = 0; i < locs.length; i += 2)
        {
            dropLocs.add(OB_Maths.locationForRect(Float.valueOf(locs[i]), Float.valueOf(locs[i + 1]), objectDict.get("loop").frame()));
        }

        topLabel.hide();
        attachControl(topLabel);
        setSceneXX(currentEvent());
        showControls("obj_.*");
        OBPath loop = (OBPath)objectDict.get("loop");
        loop.sizeToBoundingBoxIncludingStroke();

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
        super.setSceneXX(scene);
        if(OBUtils.getBooleanValue(eventAttributes.get("reload")))
        {
            for(OBControl control : filterControls("obj_.*"))
            {
                control.setProperty("start_loc",OBMisc.copyPoint(control.position()));
                ((OBGroup)control).recalculateFrameForPath(((OBGroup)control).members);
            }
        }
        correct = OBUtils.getIntValue(eventAttributes.get("correct"));
        topLabel.setString(String.format("%d",correct));
        topLabel.setColour(Color.BLACK);
    }

    public void doMainXX() throws Exception
    {
        if(OBUtils.getBooleanValue(eventAttributes.get("reload")))
        {
            lockScreen();
            topLabel.show();
            showControls("obj_.*");
            playSfxAudio("number_on",false);
            unlockScreen();

            waitSFX();
        }
        else
        {
            topLabel.show();
            playSfxAudio("number_on",true);
        }
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",300,true);
        waitForSecs(0.3f);
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl cont = finger(0, 2, filterControls("obj_.*"), pt);
            if (cont != null)
            {
                setStatus(STATUS_BUSY);
                checkTargetDrag((OBGroup)cont, pt);
            }
            else if (finger(0, 2, Arrays.asList((OBControl)topLabel), pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget();
                    }
                });
            }
        }
    }



    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
        }
    }

    public void touchUpAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            final OBControl drop = target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkTargetDrop((OBGroup)drop);
                }
            });
        }
    }


    public void checkTargetDrag(OBGroup cont, PointF pt)
    {
        removeFromDictionary(dropObjs,cont);
        OBMisc.prepareForDragging(cont,pt,this);
        setStatus(STATUS_DRAGGING);
    }

    public void checkTarget() throws Exception
    {
        topLabel.setColour(Color.RED);
        if (dropObjs.size() * 2 == correct)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            playAudioQueuedScene("FINAL", 300, true);
            waitForSecs(0.3f);
            if (events.get(events.size()-1) != currentEvent())
            {
                if (OBUtils.getBooleanValue(eventAttributes.get("delete")))
                {
                    lockScreen();
                    topLabel.hide();
                    deleteControls("obj_.*");
                    dropObjs.clear();
                    unlockScreen();
                    waitForSecs(0.3f);

                }
                else
                {
                    topLabel.hide();
                    waitForSecs(0.1f);
                    returnAllCounters();
                    waitForSecs(0.3f);
                }
            }
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            topLabel.setColour(Color.BLACK);
            if (dropObjs.size() > 0)
                returnAllCounters();

            long time = setStatus(STATUS_WAITING_FOR_DRAG);
            waitSFX();
            if (time == statusTime) playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void checkTargetDrop(OBGroup drop) throws Exception
    {
        if(objectDict.get("loop").intersectsWith(drop))
        {
            PointF targetPoint = null;
            float dist = -1;
            int closest=-1;
            for(int i=0;i<dropLocs.size();i++)
            {
                if(dropObjs.get(i) == null)
                {
                    PointF loc = dropLocs.get(i) ;
                    float dist2 = OB_Maths.PointDistance(drop.position(), loc);
                    if(closest == -1 || dist > dist2)
                    {
                        closest = i;
                        dist = dist2;
                        targetPoint = loc;
                    }
                }
            }
            dropObjs.put(closest,drop);
            playSFX("click");
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(targetPoint,drop)),0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            drop.setZPosition(drop.zPosition() - 10);
            long time = setStatus(STATUS_WAITING_FOR_DRAG);
            if(dropObjs.size()*2 == correct && getAudioForScene(currentEvent(),"REMIND2") != null)
                reprompt(time, OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMIND2"), 300),4);

        }
        else
        {
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim((PointF)drop.propertyValue("start_loc"),drop))
                    ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            drop.setZPosition(drop.zPosition() - 10);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(5, setStatus(STATUS_WAITING_FOR_DRAG), this);
    }

    public void removeFromDictionary(Map<Integer,OBGroup> dict, OBGroup obj)
    {
        for(Integer key : dict.keySet())
        {
            if(dict.get(key) == obj)
            {
                dict.remove(key);
                break;
            }
        }
    }


    public void returnAllCounters() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl cont : dropObjs.values())
            anims.add(OBAnim.moveAnim((PointF)cont.propertyValue("start_loc") ,cont));

        dropObjs.clear();
        playSfxAudio("number_off",false);
        OBAnimationGroup.runAnims(anims,0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void demo5a() throws Exception
    {
        waitForSecs(0.3f);
        OBPath path = (OBPath)objectDict.get("demo_path");
        path.sizeToBox(objectDict.get("loop").bounds());
        loadPointer(POINTER_LEFT);
        lockScreen();
        topLabel.show();
        playSfxAudio("number_on",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.2f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.05f,topLabel.frame()),-15,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(path.firstPoint(),0.15f,true);
        playAudioScene("DEMO",1,false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(thePointer,path.path(),false,0)),2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitAudio();
        waitForSecs(0.3f);
        List<Integer> nums = Arrays.asList(2, 7, 4);
        for(int i=0; i<nums.size(); i++)
        {
            OBGroup cont = (OBGroup)objectDict.get(String.format("obj_%d",nums.get(i)));
            movePointerToPoint(cont.position(),0.5f,true);
            int drop = nums.get(i);
            OBMisc.moveControlWithAttached(thePointer,Arrays.asList((OBControl)cont),dropLocs.get(drop),0.5f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            dropObjs.put(nums.get(i),cont);
            playAudio("click");
            waitAudio();
            waitForSecs(0.3f);
            playAudioScene("DEMO",2+i,true);
            waitForSecs(0.3f);

        }
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.9f,objectDict.get("loop").frame()),-25,0.5f,"DEMO",5,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.05f,topLabel.frame()),-15,0.5f,"DEMO",6,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.6f,topLabel.frame()),0.2f,true);
        topLabel.setColour(Color.RED);
        playAudio("correct");
        waitAudio();
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.05f,topLabel.frame()),-15,0.5f,"DEMO",7,0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        topLabel.hide();
        waitForSecs(0.1f);
        returnAllCounters();
        waitForSecs(0.3f);
        nextScene();
    }


}
