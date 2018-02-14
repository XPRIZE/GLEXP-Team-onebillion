package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_morenumbers.OC_MoreNumbers_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 10/04/2017.
 */

public class OC_MoreNumbers_S6 extends OC_SectionController
{
    final static int MODE_ROW = 1,
            MODE_ROW2 = 2,
            MODE_CIRCLE = 3,
            MODE_NUM = 4,
            MODE_NUM2 = 5,
            MODE_DRAG = 6;
    int textcolour, hilitecolour, startcolour, startcolour2, currentcolour, wrongcolour;
    int currentIndex, currentMode;
    OBGroup removedObj;
    List<OBControl> eventTargets;
    List<PointF> dropLocs;
    Map<Integer,OBControl> dropTargets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master6");
        textcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        startcolour = OBUtils.colorFromRGBString(eventAttributes.get("startcolour"));
        startcolour2 = OBUtils.colorFromRGBString(eventAttributes.get("startcolour2"));
        wrongcolour = OBUtils.colorFromRGBString(eventAttributes.get("wrongcolour"));
        removedObj=null;
        eventTargets = new ArrayList<>();
        dropTargets = new ArrayMap<>();
        dropLocs  = new ArrayList<>();
        loadRoundGrid();
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

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        eventTargets.clear();
        dropTargets.clear();
        dropLocs.clear();
        currentIndex = 0;
        if(eventAttributes.get("circlecolour") != null)
            currentcolour = OBUtils.colorFromRGBString(eventAttributes.get("circlecolour"));
        String target = eventAttributes.get("target");
        if(target.equals("row"))
            currentMode = MODE_ROW;
        if(target.equals("row2"))
            currentMode = MODE_ROW2;
        else if(target.equals("circle"))
            currentMode = MODE_CIRCLE;
        else if(target.equals("num"))
            currentMode = MODE_NUM;
        else if(target.equals("num2"))
            currentMode = MODE_NUM2;
        else if(target.equals("drag"))
            currentMode = MODE_DRAG;
        List<Integer> nums = new ArrayList<>();
        if(eventAttributes.get("num") != null)
        {
            nums = OBMisc.stringToIntegerList(eventAttributes.get("num"),",");
        }
        else
        {
            int firsttNum = OBUtils.getIntValue(eventAttributes.get("firstnum"));
            int lastNum = OBUtils.getIntValue(eventAttributes.get("lastnum"));
            if(firsttNum < lastNum)
            {
                for(int i = firsttNum; i <= lastNum; i++)
                    nums.add(i);

            }
            else
            {
                for(int i = firsttNum; i >= lastNum; i--)
                    nums.add(i);
            }
        }
        for(Integer num : nums)
            eventTargets.add(objectDict.get(String.format("circle_%d",num)));

        if(currentMode == MODE_DRAG)
        {
            int count = eventTargets.size();
            float xLocStart = count == 5 ? 0.15f : 0.1f;
            List<Float> yLocs = OBUtils.randomlySortedArray(Arrays.asList(0.4f,0.4f,0.5f,0.5f,0.6f,0.6f));
            List<Float> xLocs = new ArrayList<>();
            for(int i=0; i<count; i++)
            {
                xLocs.add(xLocStart + i*0.7f/count);

            }

            xLocs = new ArrayList<>(OBUtils.randomlySortedArray(xLocs));
            for(int i=0; i<count; i++)
            {
                OBControl circle = eventTargets.get(i);
                dropLocs.add(OBMisc.copyPoint(circle.position()));
                PointF startLoc = OB_Maths.locationForRect(xLocs.get(i),yLocs.get(i), objectDict.get("bottombar").frame());
                circle.setProperty("startloc", OBMisc.copyPoint(startLoc));
                circle.setZPosition(4);
            }
        }
    }

    public void doMainXX() throws Exception
    {
        if(currentMode == MODE_ROW)
        {
            colourCircle((OBGroup)eventTargets.get(0),hilitecolour);
        }
        else if(currentMode == MODE_CIRCLE)
        {
            colourAllCircles(eventTargets,currentcolour);
        }
        else if(currentMode == MODE_DRAG)
        {
            animateCirclesFall();
        }
        startScene();
    }

    @Override
    public void replayAudio()
    {
        long lastTime = statusTime;
        super.replayAudio();
        statusTime = lastTime;
    }

    public void touchDownAtPoint(PointF pt,View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(-1, 0, filterControls("circle_.*"), pt);
            if (targ != null)
            {
                if (currentMode == MODE_ROW)
                {
                    if (targ == eventTargets.get(currentIndex))
                    {
                        setStatus(STATUS_BUSY);
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkRow1((OBGroup) targ);
                            }
                        });

                    }

                } else if (currentMode == MODE_ROW2)
                {
                    if (targ == eventTargets.get(0))
                    {
                        setStatus(STATUS_BUSY);

                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkRow2((OBGroup) targ);
                            }
                        });

                    }

                } else if (currentMode == MODE_CIRCLE)
                {
                    if (targ == eventTargets.get(currentIndex))
                    {
                        setStatus(STATUS_BUSY);
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkCircle((OBGroup) targ);
                            }
                        });

                    }
                }
                else if (currentMode == MODE_NUM || currentMode == MODE_NUM2)
                {
                        setStatus(STATUS_BUSY);
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkNum((OBGroup) targ);
                            }
                        });


                }

            }
        }
        else if(status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl targ = finger(0,0,eventTargets,pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                removeFromDictionary(dropTargets,targ);
                OBMisc.prepareForDragging(targ,pt, this);
                setStatus(STATUS_DRAGGING);

            }
            else if(finger(0,2,Arrays.asList(objectDict.get("button_arrow")),pt) != null)
            {
                if(dropTargets.size() == eventTargets.size())
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkDragTarget((OBGroup)targ);
                        }
                    });


                }
                else
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            playAudioQueuedScene("INCORRECT",300,false);
                        }
                    });
                }
            }
        }

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
                    checkDropTarget();
                }
            });
        }
    }


    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
        }
    }



    public void checkRow1(OBGroup targ) throws Exception
    {
        playAudio(null);
        OBLabel label = (OBLabel)targ.objectDict.get("label");
        lockScreen();
        colourCircle(targ, Color.WHITE);
        label.setColour(Color.RED);
        label.show();
        unlockScreen();
        playAudioScene("FINAL",currentIndex,true);
        currentIndex++;
        lockScreen();
        label.setColour ( textcolour);
        if(eventTargets.size() > currentIndex)
            colourCircle((OBGroup)eventTargets.get(currentIndex),hilitecolour);

        unlockScreen();
        if(eventTargets.size() > currentIndex)
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            waitForSecs(0.6f);
            nextScene();
        }
    }

    public void checkRow2(OBGroup targ) throws Exception
    {
        playAudio(null);
        colourCircle(targ,hilitecolour);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        for(int i=0; i<10; i++)
        {
            OBGroup circle = (OBGroup)eventTargets.get(i);
            lockScreen();
            circle.objectDict.get("label").show();
            colourCircle(circle,Color.WHITE);
            unlockScreen();
            playAudioScene("FINAL",i+1,true);
        }
        waitForSecs(0.5f);
        nextScene();
    }

    public void checkCircle(OBGroup targ) throws Exception
    {
        colourCircle(targ,Color.WHITE);
        playAudioScene("FINAL",currentIndex,false);
        currentIndex++;
        if(eventTargets.size() > currentIndex)
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            waitForAudio();
            waitForSecs(0.3f);
            nextScene();
        }
    }

    public void checkNum(OBGroup targ) throws Exception
    {
        if(targ == eventTargets.get(currentIndex))
        {
            gotItRightBigTick(false);
            colourCircle(targ,currentcolour);
            waitSFX();
            if(currentMode == MODE_NUM2)
            {
                waitForSecs(0.3f);
                colourCircle(targ,Color.WHITE);
            }
            nextPhase();
        }
        else
        {
            gotItWrongWithSfx();
            int prevColour = targ.objectDict.get("background").fillColor();
            colourCircle(targ,wrongcolour);
            waitSFX();
            colourCircle(targ,prevColour);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene(getPhase(),"INCORRECT", false);

        }
    }

    public void checkDragTarget(OBGroup targ) throws Exception
    {
        playAudio(null);
        OC_MoreNumbers_Additions.buttonSet(1,this);
        List<OBAnim> wrongAnims = new ArrayList<>();
        for(int i=0; i<eventTargets.size(); i++)
        {
            if(eventTargets.get(i) != dropTargets.get(i))
            {
                OBGroup circle = (OBGroup)dropTargets.get(i);
                removeFromDictionary(dropTargets,circle);
                wrongAnims.add(OBAnim.moveAnim((PointF)circle.propertyValue("startloc") ,circle));

            }

        }
        if(wrongAnims.size() == 0)
        {
            gotItRightBigTick(true);
            OC_MoreNumbers_Additions.buttonSet(2, this);
            playAudioQueuedScene("FINAL",300,true);
            if(OBUtils.getBooleanValue(eventAttributes.get("reset")))
            {
                waitForSecs(0.5f);
                lockScreen();
                hideControls("circle_.*");
                objectDict.get("bottombar").hide();
                objectDict.get("button_arrow").hide();
                playSfxAudio("pop",false);

                unlockScreen();
                waitSFX();

            }
            else
            {
                waitForSecs(0.2f);
                if(removedObj != null)
                {
                    eventTargets.add(removedObj);
                    removedObj = null;

                }
                colourAllCircles(eventTargets,Color.WHITE);
                for(OBControl obj : eventTargets)
                    obj.setZPosition(2);
            }
            waitForSecs(0.3f);
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            OBAnimationGroup.runAnims(wrongAnims,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            OC_MoreNumbers_Additions.buttonSet(0,this);
            setStatus(STATUS_WAITING_FOR_DRAG);
            playAudioQueuedScene("INCORRECT2",300,false);

        }
    }

    public void checkDropTarget() throws Exception
    {
        OBGroup circle = (OBGroup)target;
        target = null;
        int dropIndex = -1;
        for(int i=0; i<eventTargets.size(); i++)
        {
            if(OB_Maths.PointDistance(circle.position(), dropLocs.get(i))< 0.8f*circle.width())
            {
                dropIndex = i;
                break;
            }

        }
        if(dropIndex != -1)
        {
            OBGroup prevCircle = (OBGroup)dropTargets.get(dropIndex);
            if(prevCircle != null)
            {
                prevCircle.setZPosition(prevCircle.zPosition()+10);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)prevCircle.propertyValue("startloc"),prevCircle))
                        ,0.3,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            }
            dropTargets.put(dropIndex,circle);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(dropLocs.get(dropIndex),circle))
                    ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            playSfxAudio("number_click",false);
            circle.setZPosition(circle.zPosition()-10);
            if(prevCircle != null)
            {
                waitForSecs(0.15f);
                prevCircle.setZPosition(prevCircle.zPosition()-10);
            }
            setStatus(STATUS_WAITING_FOR_DRAG);

        }
        else
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)circle.propertyValue("startloc"),circle))
                    ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            circle.setZPosition(circle.zPosition()-10);
            setStatus(STATUS_WAITING_FOR_DRAG);

        }
    }

    public void startScene() throws Exception
    {
        if(currentMode == MODE_DRAG)
        {
            OC_MoreNumbers_Additions.buttonSet(0,this);
            OBMisc.doSceneAudio(-1,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else
        {
            long time = setStatus(STATUS_AWAITING_CLICK);
            if(currentMode == MODE_ROW2)
                flashCircle((OBGroup)eventTargets.get(0),time);
            if(currentMode != MODE_NUM && currentMode != MODE_NUM2)
                OBMisc.doSceneAudio(4,time,this);
            else
                OBMisc.doSceneAudio(-1,getPhase(),time,this);

        }

    }
    public String getPhase()
    {
        return String.format("%s%d", currentEvent(), currentIndex+1);

    }

    public void removeFromDictionary(Map<Integer,OBControl> dict, OBControl circle)
    {
        for(Integer key : dict.keySet())
        {
            if(dict.get(key) == circle)
            {
                dict.remove(key);
                break;
            }
        }
    }

    public void animateCirclesFall() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl circle : eventTargets)
        {
            PointF startPoint = OBMisc.copyPoint(circle.position());
            PointF endPoint = (PointF) circle.propertyValue("startloc") ;
            circle.setZPosition ( 2);
            Path path = new Path();
            PointF cp1 = new PointF(startPoint.x+ (startPoint.x > endPoint.x ? -2.5f * circle.width() : 2.5f * circle.width() ), startPoint.y);
            PointF cp2 = new PointF(endPoint.x, endPoint.y - circle.width() * 2);
            path.moveTo(startPoint.x, startPoint.y);
            path.cubicTo(cp1.x,cp1.y,cp2.x,cp2.y, endPoint.x, endPoint.y);
            anims.add(OBAnim.pathMoveAnim(circle,path,false,0));
            anims.add(OBAnim.colourAnim("fillColor",currentcolour,((OBGroup)circle).objectDict.get("background")));
        }
        playSfxAudio("number_fly",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN,this);
        waitSFX();
    }

    public void nextPhase() throws Exception
    {
        currentIndex++;
        if(eventTargets.size() <= currentIndex)
        {
            if(currentMode == MODE_NUM)
            {
                waitForSecs(0.3f);
                displayTick();
                waitForSecs(0.5f);
                lockScreen();
                for(OBControl group : eventTargets)
                    colourCircle((OBGroup)group,Color.WHITE);
                unlockScreen();
                waitForSecs(0.3f);

            }
            nextScene();
        }
        else
        {
            startScene();

        }
    }

    public void flashCircle(final OBGroup circle,final long time) throws Exception
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                while(statusTime == time)
                {
                    waitForSecs(0.5f);
                    if(statusTime != time)
                        break;
                    colourCircle(circle,hilitecolour);
                    waitForSecs(0.5f);
                    if(statusTime != time)
                        break;
                    colourCircle(circle,startcolour2);
                }
            }
        });

    }

    public void loadRoundGrid()
    {
        OBControl circle = objectDict.get("obj");
        ((OBPath)circle).sizeToBoundingBoxIncludingStroke();
        float fontSize = 43 * circle.height()/90.0f;
        for(int i=0; i<3; i++)
        {
            for(int j=0; j<10; j++)
            {
                int num = i*10+j+91;
                OBControl circleCopy = circle.copy();
                circleCopy.setPosition ( OB_Maths.locationForRect(j/9.0f,i/2.0f,objectDict.get("workrect").frame()));
                attachControl(circleCopy);
                circleCopy.show();
                circleCopy.setZPosition ( 1);
                OBLabel label = new OBLabel(String.format("%d",num),OBUtils.standardTypeFace(),fontSize);
                label.setColour ( textcolour);
                OBGroup groupNum = new OBGroup(Arrays.asList((OBControl)label));
                groupNum.sizeToTightBoundingBox();
                groupNum.setPosition(circleCopy.position());
                if(num%10 == 1)
                {
                    groupNum.setRight ( OB_Maths.locationForRect(0.7f,0.5f,circleCopy.frame()).x);

                }
                else if(num%10 == 0)
                {
                    groupNum.setRight ( OB_Maths.locationForRect(0.85f,0.5f,circleCopy.frame()).x);

                }
                else
                {
                    groupNum.setRight ( OB_Maths.locationForRect(0.8f,0.5f,circleCopy.frame()).x);

                }
                attachControl(groupNum);
                groupNum.setZPosition ( 2);
                OBGroup fullGroup = new OBGroup(Arrays.asList(circleCopy, groupNum));
                fullGroup.objectDict.put("background",circleCopy);
                fullGroup.objectDict.put("label",label);
                fullGroup.setProperty("startloc",OBMisc.copyPoint(fullGroup.position()));
                fullGroup.setProperty("num_value",num);
                fullGroup.hide();
                attachControl(fullGroup);
                objectDict.put(String.format("circle_%d",num),fullGroup);
            }
        }
    }

    public void animateRow(int row, int colour) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(int i=1; i<=10; i++)
        {
            OBGroup circle = (OBGroup)objectDict.get(String.format("circle_%d",80+(row*10) +i));
            circle.setLeft(circle.left()- bounds().width());
            circle.show();
            colourCircle(circle, colour);
            circle.objectDict.get("label").hide();
            anims.add(OBAnim.propertyAnim("left",circle.left() + bounds().width(),circle));

        }
        playSfxAudio("slide_on",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN,this);
        waitSFX();
    }

    public void colourAllCircles(List<OBControl> circles, int colour)
    {
        lockScreen();
        for(OBControl circle : circles)
            colourCircle((OBGroup)circle,colour);

        unlockScreen();

    }

    public void colourCircle(OBGroup circleGroup, int colour)
    {
        OBControl circle = circleGroup.objectDict.get("background");
        circle.setFillColor(colour);
    }

    public void demo6a() throws Exception
    {
        waitForSecs(0.2f);
        animateRow(1,startcolour);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.25f,1.1f,objectDict.get("circle_95").frame()),-30,0.5f,"DEMO",1,0.3f);
        colourCircle((OBGroup)objectDict.get("circle_91"),hilitecolour);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("circle_91").frame()),-40,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        startScene();

    }
    public void demo6b() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("circle_100").frame()),-20,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,3f,objectDict.get("circle_100").frame()),-20,0.5f,true);
        animateRow(2,startcolour);
        colourCircle((OBGroup)objectDict.get("circle_101"),hilitecolour);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("circle_101").frame()),-40,0.65f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();

    }

    public void demo6c() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("circle_110").frame()),-20,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,3f,objectDict.get("circle_110").frame()),-20,0.5f,true);
        animateRow(3,startcolour);
        colourCircle((OBGroup)objectDict.get("circle_111"),hilitecolour);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.2f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        startScene();

    }

    public void demo6d() throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("circle_91").frame()),-40,0.65f,"DEMO",1,0.5f);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("circle_120").frame()),-20,0.5f,"DEMO",2,0.5f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-20,0.5f,"DEMO",3,0.3f);
        colourAllCircles(eventTargets,currentcolour);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.65f,this.bounds()),-20,0.5f,"DEMO",4,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.9f,objectDict.get("circle_98").frame()),-22,0.65f,"DEMO",5,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo6g() throws Exception
    {
        colourAllCircles(eventTargets,currentcolour);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.9f,objectDict.get("circle_110").frame()),-20,0.65f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();

    }
    public void demo6i() throws Exception
    {
        playAudio(getAudioForSceneIndex(getPhase(), "DEMO", 0));
        waitAudio();
        waitForSecs(0.3f);
        startScene();

    }
    public void demo6zb() throws Exception
    {
        waitForSecs(0.7f);
        lockScreen();
        objectDict.get("bottombar").setZPosition(1);
        objectDict.get("button_arrow").setZPosition(1.1f);
        objectDict.get("bottombar").show();
        objectDict.get("button_arrow").show();
        OC_MoreNumbers_Additions.buttonSet(2,this);
        playSfxAudio("ping",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        animateCirclesFall();
        OBGroup circle =(OBGroup) eventTargets.get(eventTargets.size()-1);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.7f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        circle.setZPosition ( 14);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.6f,circle.frame()),-30,0.4f,true);
        waitForSecs(0.2f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(dropLocs.get(dropLocs.size()-1),circle),
                OBMisc.attachedAnim(circle,Arrays.asList(thePointer)))
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("number_click",true);
        moveScenePointer(OB_Maths.locationForRect(0.6f,2.5f,circle.frame()),-30,0.5f,"DEMO",1,0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        circle.setZPosition ( 4);
        removedObj = circle;
        dropLocs.remove(dropLocs.size()-1);
        eventTargets.remove(eventTargets.size()-1);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("button_arrow").frame()),-20,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo6zda() throws Exception
    {
        waitForSecs(0.5f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        animateRow(1,startcolour2);
        startScene();

    }

    public void demo6zdb() throws Exception
    {
        animateRow(2,startcolour2);
        startScene();

    }
    public void demo6zdc() throws Exception
    {
        animateRow(3,startcolour2);
        startScene();

    }

}




