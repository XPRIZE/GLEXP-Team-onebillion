package org.onebillion.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
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
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 15/03/2017.
 */

public class OC_CountMore_S6k extends OC_SectionController
{
    Map<Integer,OBLabel> oddBox, evenBox;
    List<PointF> evenLocs, oddLocs;
    List<OBLabel> eventTargets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        oddBox = new ArrayMap<>();
        evenBox = new ArrayMap<>();
        eventTargets = new ArrayList<>();
        evenLocs = new ArrayList<>();
        oddLocs = new ArrayList<>();
        OBPath box1 = (OBPath)objectDict.get("box_1");
        OBPath box2 = (OBPath)objectDict.get("box_2");
        box1.setZPosition(1);
        box2.setZPosition(1);
        box1.sizeToBoundingBoxIncludingStroke();
        box2.sizeToBoundingBoxIncludingStroke();
        List<Float> xLocs = Arrays.asList(0.15f,0.5f,0.85f);
        List<Float> yLocs = Arrays.asList(0.2f,0.5f,0.8f);
        for(int i = 0; i<9; i++)
        {
            float xLoc = xLocs.get(i%3) ;
            float yLoc = yLocs.get((int)Math.round(Math.floor(i/3))) ;
            oddLocs.add(OB_Maths.locationForRect(xLoc,yLoc,objectDict.get(String.format("box_%d", 1)).frame()));
            evenLocs.add(OB_Maths.locationForRect(xLoc,yLoc,objectDict.get(String.format("box_%d", 2)).frame()));

        }
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo6k();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(eventAttributes.get("nums") != null)
            loadNumbers();

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl cont = finger(0,2,(List<OBControl>)(Object)eventTargets,pt);
            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                checkTargetForDrag((OBLabel)cont, pt);
            }
        }
    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            target.setPosition (OB_Maths.AddPoints(pt, dragOffset));

        }
    }

    public void touchUpAtPoint(final PointF pt, View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            final OBLabel label = (OBLabel) target;
            target = null;
            OBUtils.runOnOtherThread(
                    new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkTargetDrop(label);
                        }
                    }
            );
        }
    }

    public void checkTargetForDrag(OBLabel cont, PointF pt)
    {
        removeFromDictionary(oddBox, cont);
        removeFromDictionary(evenBox, cont);
        OBMisc.prepareForDragging(cont,pt,this);
        setStatus(STATUS_DRAGGING);
    }

    public void checkTargetDrop(OBLabel label) throws Exception
    {
        boolean inBox = false;
        boolean complete = false;
        int wrongBox = -1;
        for(int j=1 ; j<=2; j++)
        {
            OBControl boxCont = objectDict.get(String.format("box_%d",j));
            if(boxCont.frame().contains( label.position().x, label.position().y))
            {
                if((boolean)label.propertyValue("even") != (j==2))
                {
                    wrongBox = j;
                }
                else
                {
                    gotItRight();
                    inBox = true;
                    PointF targetPoint = null;
                    Map<Integer,OBLabel> box = j==1 ? oddBox : evenBox;
                    float dist = -1;
                    int closest=-1;
                    for(int i=0; i<9; i++)
                    {
                        if(box.get(i) == null)
                        {
                            PointF loc =  j==1 ? oddLocs.get(i) :evenLocs.get(i);
                            float dist2 = OB_Maths.PointDistance(label.position(), loc);
                            if(closest == -1 || dist > dist2)
                            {
                                closest = i;
                                dist = dist2;
                                targetPoint = loc;
                            }
                        }
                    }
                    box.put(closest,label);
                    playSfxAudio("drop",false);
                    OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(targetPoint,label)),0.15f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                    label.setZPosition(label.zPosition() - 10);
                    if((evenBox.size() + oddBox.size()) == eventTargets.size())
                        complete = true;
                }
                break;
            }
        }
        if(complete)
        {
            waitSFX();
            waitForSecs(0.3f);
            displayTick();
            waitForSecs(0.5f);
            if(currentEvent() != events.get(events.size()-1))
            {
                lockScreen();
                for(OBControl con : eventTargets)
                    detachControl(con);
                eventTargets.clear();
                oddBox.clear();
                evenBox.clear();

                unlockScreen();

            }
            nextScene();

        }
        else
        {
            if(wrongBox > 0)
                gotItWrongWithSfx();
            if(inBox == false)
            {
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)label.propertyValue("start_loc"),label))
                        ,0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                label.setZPosition(label.zPosition() - 10);

            }
            long time = setStatus(STATUS_WAITING_FOR_DRAG);
            if(wrongBox > 0)
            {
                waitSFX();
                if(time == statusTime)
                    playAudioQueuedScene(wrongBox == 1 ? "INCORRECT" : "INCORRECT2",0.3f,false);
            }
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4, setStatus(STATUS_WAITING_FOR_DRAG), this);
    }

    public void removeFromDictionary(Map<Integer,OBLabel> dict,OBLabel label)
    {
        for(Integer key : dict.keySet())
        {
            if(dict.get(key) == label)
            {
                dict.remove(key);
                gotItWrong();
                break;
            }
        }
    }


    public void loadNumbers()
    {
        String[] nums = eventAttributes.get("nums").split(",");
        String[] locs = eventAttributes.get("locs").split(",");
        int index = 0;
        for(String num : nums)
        {
            OBLabel label = new OBLabel(num, OBUtils.standardTypeFace(), 70.0f * objectDict.get("box_1").height()/250.0f);
            label.setColour(Color.BLACK);
            label.setPosition(OB_Maths.locationForRect(Float.valueOf(locs[index]),Float.valueOf(locs[index+1]), objectDict.get("bottombar").frame()));
            attachControl(label);
            label.setZPosition(4);
            label.show();
            label.setProperty("even", Integer.valueOf(num) % 2==0 ? true : false);
            index += 2;
            label.setProperty("start_loc",OBMisc.copyPoint(label.position()));
            eventTargets.add(label);
        }
    }

    public void demo6k() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.6f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        for(int i=0; i<2; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.75f,eventTargets.get(1-i).frame()),-35,0.5f,true);
            playAudioScene("DEMO",i+1,false);
            OBMisc.moveControlWithAttached(eventTargets.get(1-i),Arrays.asList(thePointer), i==0 ? evenLocs.get(4) : oddLocs.get(4), 2, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForAudio();
            waitForSecs(0.3f);
            playSfxAudio("drop",false);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.5f,eventTargets.get(1-i).frame()),-35,0.3f,true);
            waitSFX();
            waitForSecs(0.3f);

        }

        oddBox.put(4,eventTargets.get(0));
        evenBox.put(4,eventTargets.get(1));
        waitForSecs(0.3f);
        thePointer.hide();
        nextScene();

    }




}
