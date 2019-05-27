package com.maq.xprize.onecourse.mainui.oc_count100;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.OBSectionController;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBConfigManager;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 30/08/16.
 */
public class OC_Count100_S6 extends OC_SectionController
{
    OC_CountingRobot robotController;
    int numColour, lineColour, buttonActive, buttonInactive, hiliteColour;
    List<String> numList, correctNums;
    int currentNumIndex, wrongCount, correctCount, touchCount, score;
    List<String> rows;
    Typeface numFont;
    float numFontSize;
    OBControl currentTarget;
    OBLabel counter;
    boolean scoreMode;
    PointF robotStartPosition;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master6");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);

        robotController = new OC_CountingRobot((OBGroup)objectDict.get("robot"), this);
        lineColour = OBUtils.colorFromRGBString(eventAttributes.get("linecolour"));
        numColour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        buttonActive = OBUtils.colorFromRGBString(eventAttributes.get("buttonactive"));
        buttonInactive = OBUtils.colorFromRGBString(eventAttributes.get("buttoninactive"));
        hiliteColour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        numList = new ArrayList<>();
        correctNums = new ArrayList<>();
        setSceneXX(currentEvent());
        showRow(Integer.valueOf(rows.get(0)));
        OBLabel label = (OBLabel)this.objectDict.get("num_6");
        label.hide();
        numFont = label.typeface();
        numFontSize = label.fontSize();
        objectDict.get("scorebar").setAnchorPoint(new PointF(0.5f, 1));
        objectDict.get("scorebar").setHeight(0.1f);
        objectDict.get("scorebar2").setAnchorPoint(new PointF(0.5f, 1));
        objectDict.get("scorebar2").setHeight(0.1f);
        PointF anchor = OB_Maths.relativePointInRectForLocation(objectDict.get("bagfront").position(),objectDict.get("bagback").frame());
        objectDict.get("bagback").setAnchorPoint(anchor);
        robotController.setButtonColour(buttonActive);
        counter = new OBLabel("000",OBUtils.standardTypeFace(), OBConfigManager.sharedManager.applyGraphicScale(86));
        counter.setColour(numColour);
        counter.setPosition(objectDict.get("numbox").position());
        counter.setZPosition(1.5f);
        counter.setString("0");
        attachControl(counter);
        counter.hide();
        scoreMode = false;
        robotStartPosition = OC_Generic.copyPoint(robotController.robot.position());
        robotController.robot.setRight(0);
        robotController.showSide();
        robotController.setFaceExpression(OC_CountingRobot.ROBOT_SIDE_HAPPY);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demoStart();
                startScene();

            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        numList.clear();
        correctNums.clear();
        correctNums = createRandomListForDescription(Arrays.asList(eventAttributes.get("correct").split(",")));
        List<String> wrongNums =createRandomListForDescription(Arrays.asList(eventAttributes.get("wrong").split(",")));
        int correctIndex = 0, wrongIndex = 0;
        List<String> temp = new ArrayList<>();
        List<String> order = Arrays.asList(eventAttributes.get("order").split(","));
        for(int i=0;i<order.size();i+=2)
        {
            for(int j=0;j<Integer.valueOf(order.get(i));j++)
            {
                temp.add(correctNums.get(correctIndex));
                correctIndex++;
            }
            for(int j=0;j<Integer.valueOf(order.get(i+1));j++)
            {
                temp.add(wrongNums.get(wrongIndex));
                wrongIndex++;

            }
            numList.addAll(OBUtils.randomlySortedArray(temp));
            temp.clear();
        }
        numList.add(correctNums.get(correctNums.size()-1));
        rows = Arrays.asList(eventAttributes.get("row").split(","));
        for(int i=0;i<rows.size();i++)
        {
            objectDict.get("gridrect").setPosition(OB_Maths.locationForRect(0.5f,(1.0f/(rows.size()+1))*(i+1),objectDict.get("workrect").frame()));
            OC_Count100_Additions.drawGrid(Integer.valueOf(rows.get(i)),objectDict.get("gridrect"),lineColour,numColour,true,this);
        }
        hideControls("num_.*");
        hideControls("box_.*");
        hideControls("frame_.*");
        currentNumIndex = wrongCount = touchCount = correctCount = 0;

    }

    public void doMainXX() throws Exception
    {

        for(int i=0;i<rows.size();i++)
        {
            showRow(Integer.valueOf(rows.get(i)));
            playSfxAudio("show",true);
            waitForSecs(0.2f);

        }
        robotController.setFaceExpression(OC_CountingRobot.ROBOT_HAPPY);
        robotController.animateArmsDown();
        startScene();
    }

    public void setScene6f()
    {
        scoreMode = true;
    }

    public void demo6f() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("scorebar").frame()),0,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    String num = (String)target.settings.get("value");
                    if(correctNums.contains(num))
                    {
                        if(pointWithinBoxes(target.position(),Collections.singletonList(num)))
                        {
                            gotItRightBigTick(false);
                            targetSnapToGrid(0.15f);
                            waitSFX();
                            correctNums.remove(num);
                            checkFin(true);
                        }
                        else if(pointWithinBoxes(target.position(),correctNums) || pointWithinBag(pt))
                        {
                            gotItWrongWithSfx();
                            ((OBLabel)target).setColour(Color.RED);
                            targetSnapToGrid(0.2f);
                            waitSFX();
                            checkFin(false);
                        }
                        else
                        {
                            flyTargetWithTracking(target,objectDict.get("droparea").position(),0.2f);
                            setStatus(STATUS_WAITING_FOR_DRAG);
                        }

                    }
                    else
                    {
                        if(pointWithinBag(pt))
                        {
                            gotItRightBigTick(false);
                            targetTrash(0.15f);
                            waitSFX();
                            correctNums.remove(num);
                            checkFin(true);

                        }
                        else if(pointWithinBoxes(target.position(),correctNums))
                        {
                            gotItWrongWithSfx();
                            ((OBLabel)target).setColour(Color.RED);
                            targetTrash(0.25f);
                            waitSFX();
                            checkFin(false);
                        }
                        else
                        {
                            flyTargetWithTracking(target,objectDict.get("droparea").position(),0.2f);
                            setStatus(STATUS_WAITING_FOR_DRAG);

                        }

                    }

                }

            } ) ;

        }
    }



    public void touchDownAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if(scoreMode)
            {
                if(findScoreTarget(pt) != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkScoreTarget();
                        }
                    });

                }
            }
            else
            {
                if(findButtonTarget(pt) != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkButtonTarget();
                        }
                    });
                }
            }
        }
        else
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl obj = findDragTarget(pt);
            if(obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                         {
                                             @Override
                                             public void run() throws Exception
                                             {
                                                 checkObjectDrag(obj, pt);
                                             }
                                         }
                );

            }
            else if(findButtonTarget(pt) != null)
            {
                touchCount++;
                if(touchCount > 5)
                {
                    robotController.setFaceExpression(OC_CountingRobot.ROBOT_ANGRY);
                    touchCount = 0;

                }

            }
        }

    }


    public void checkButtonTarget() throws Exception
    {
        setStatus(STATUS_BUSY);
        playSfxAudio("button",false);
        robotController.animateFrontButtonColour(buttonInactive,0.1f);
        throwNumber(numList.get(currentNumIndex));
        doPhaseAudio(2);
        setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public void checkObjectDrag(OBControl obj, PointF pt) throws Exception
    {
        if(obj != null)
        {
            setStatus(STATUS_BUSY);
            playSfxAudio("drag",false);
            OBMisc.prepareForDragging(obj,pt,this);
            setStatus(STATUS_DRAGGING);

        }
    }

    public void checkScoreTarget() throws Exception
    {
        setStatus(STATUS_BUSY);
        animateScore();
        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,objectDict.get("numbox").frame()),0.5f,true);
        if(score==50)
        {
            playAudioQueuedScene("FINAL",0.3f,true);

        }
        else if(score>37)
        {
            playAudioQueuedScene("FINAL2",0.3f,true);

        }
        else if(score>27)
        {
            playAudioQueuedScene("FINAL3",0.3f,true);

        }
        else
        {
            playAudioQueuedScene("FINAL4",0.3f,true);

        }
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        playSfxAudio("robot2",false);
        robotController.animateWave();
        waitSFX();
        nextScene();
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            target.setPosition( OB_Maths.AddPoints(pt, dragOffset));
            robotController.eyesTrackPoint(target.position(),true);
        }

    }

    void startScene() throws Exception
    {
        robotController.animateFrontButtonColour(buttonActive, 0.2f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void animateScore() throws Exception
    {
        lockScreen();
        objectDict.get("numbox").show();
        counter.show();

        unlockScreen();
        playSfxAudio("flag",true);
        waitForSecs(0.2f);
        objectDict.get("scorebar2").show();
        playSfxAudio("score",false);
        for(int i=1;i<=(score*2);i++)
        {
            counter.setString(String.format("%d",i));
            objectDict.get("scorebar2").setHeight((i/100.0f)*objectDict.get("scoreborder").height());
            waitForSecs(0.02f);

        }
        playSFX(null);
        if(score==50)
        {
            playSfxAudio("score1",false);
            scoreFlash(3);

        }
        else if(score >=20)
        {
            playSfxAudio("score2",false);
            scoreFlash(2);

        }
        else
        {
            playSfxAudio("score3",false);
            scoreFlash(1);

        }
        waitSFX();
    }

    public void scoreFlash(int count)
    {
        for(int i=0;i<count;i++)
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",Color.RED,counter)),0.35,true,OBAnim.ANIM_LINEAR,this);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",numColour,counter)),0.35,true,OBAnim.ANIM_LINEAR,this);
        }
    }

    public void checkFin(boolean correct) throws Exception
    {
        currentNumIndex++;
        if(correct)
        {
            score++;
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("height",(score/50.0f)*objectDict.get("scoreborder").height(),objectDict.get("scorebar"))),0.05,true,OBAnim.ANIM_LINEAR,this);
            wrongCount=0;
            correctCount++;
            if(correctCount >= 3)
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_HAPPY);

        }
        else
        {
            correctCount=0;
            wrongCount++;
            if(wrongCount >= 3)
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_ANGRY2);

        }
        if(numList.size() <= currentNumIndex)
        {
            if(wrongCount > 1)
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_ANGRY2);
            else
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_HAPPY);
            playSfxAudio("complete",false);
            for(int i=10;i>0;i--)
            {
                List<OBAnim> onAnims = new ArrayList<>();
                List<OBAnim> offAnims = new ArrayList<>();
                for(String row : rows)
                {
                    int rowNum = Integer.valueOf(row);
                    final OBControl box = objectDict.get(String.format("box_%d",(rowNum -1)*10 + i));
                    onAnims.add(OBAnim.colourAnim("backgroundColor",hiliteColour,box));
                    offAnims.add(OBAnim.colourAnim("backgroundColor",Color.WHITE,box));
                }
                OBAnimationGroup.chainAnimations(Arrays.asList(onAnims,offAnims),Arrays.asList(0.1f, 0.1f),false ,Arrays.asList(OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR),1,this);
                waitForSecs(0.05f);
            }
            waitForSecs(0.2f);
            waitSFX();
            playAudioQueuedScene("FINAL",0.3f,true);
            displayTick();
            waitForSecs(0.6f);
            lockScreen();
            deleteControls("box_.*");
            deleteControls("num_.*");
            deleteControls("frame_.*");

            unlockScreen();
            playSfxAudio("hide",true);
            waitForSecs(0.2f);
            nextScene();

        }
        else
        {
            robotController.animateArmsDown();
            robotController.animateFrontButtonColour(buttonActive,0.2f);
            doPhaseAudio(3);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public boolean pointWithinBoxes(PointF pt, List<String> boxNums)
    {
        for(String boxnum : boxNums)
        {
            OBControl box = objectDict.get(String.format("box_%s",boxnum));
            RectF boxFrame = new RectF(box.getWorldFrame());
            boxFrame.inset(-0.35f * box.width(), -0.35f * box.height());
            if(boxFrame.contains(pt.x, pt.y))
                return true;
        }
        return false;

    }
    public boolean pointWithinBag(PointF pt)
    {
        if(objectDict.get("bagdrop").frame().contains(pt.x,pt.y))
            return true;
        else
            return false;

    }

    public void doPhaseAudio(int phase) throws Exception
    {
        setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),String.format("REPEAT%d",phase)),300));
        if(currentNumIndex == 1 || currentNumIndex == 0)
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),String.format("PROMPT%d",phase)),300),false);

    }

    public OBControl findDragTarget(PointF pt)
    {
        OBControl c =finger(0,2,Collections.singletonList(currentTarget),pt);
        return c;

    }

    public OBControl findButtonTarget(PointF pt)
    {
        OBControl c =finger(0,1,Collections.singletonList(robotController.robot.objectDict.get("buttonlayer")),pt);
        return c;

    }

    public OBControl findScoreTarget(PointF pt)
    {
        OBControl c =finger(0,2,Collections.singletonList(objectDict.get("scorebg")),pt);
        return c;

    }

    public List<String> getRandomNumsFrom(int from,int to, int count)
    {
        List<String> num = new ArrayList<>();
        for(int i=from; i <= to;i++)
        {
            num.add(String.valueOf(i));
        }
        return OBUtils.randomlySortedArray(num).subList(0,count);

    }

    public List<String> createRandomListForDescription(List<String> description)
    {
        List<String> nums =new ArrayList<>();
        for(int i=0;i<description.size();i+=3)
            nums.addAll(getRandomNumsFrom(Integer.valueOf(description.get(i)),Integer.valueOf(description.get(i+1)),Integer.valueOf(description.get(i+2))));

        return OBUtils.randomlySortedArray(nums);
    }

    public void showRow(int rowNum)
    {
        lockScreen();
        for(int j=1;j<=10;j++)
        {
            int num = (rowNum-1) *10 + j;
            objectDict.get(String.format("box_%d",num )).show();
            if(!correctNums.contains(String.valueOf(num)))
                objectDict.get(String.format("num_%d",num )).show();

        }
        objectDict.get(String.format("frame_%d", rowNum)).show();
        unlockScreen();
    }

    public void throwNumber(String num)
    {
        OBLabel numControl = new OBLabel(num,numFont,numFontSize);
        numControl.setColour(numColour);
        numControl.hide();
        attachControl(numControl);
        numControl.setPosition ( OB_Maths.worldLocationForControl(0.5f,0.5f,robotController.robot.objectDict.get("face")));
        numControl.setZPosition(-0.1f);
        numControl.show();
        robotController.setFaceExpression(OC_CountingRobot.ROBOT_SIDE_OPEN);
        PointF originalPosition = objectDict.get("droparea").position();
        float distance = OB_Maths.PointDistance(numControl.position(), originalPosition);
        Path path = OBUtils.SimplePath(numControl.position(), originalPosition, -distance / 5);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(numControl,path,false,0),
                OBAnim.rotationAnim((float)Math.toRadians(720),numControl)),
                0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        if(wrongCount <3)
        {
            robotController.setFaceExpression(OC_CountingRobot.ROBOT_SIDE_HAPPY);
        }
        else
        {
            robotController.setFaceExpression(OC_CountingRobot.ROBOT_SIDE_SAD);
        }
        robotController.eyesTrackPoint(numControl.position(),true);
        robotController.animateArmsUp();
        numControl.setProperty("value",num);
        numControl.setZPosition ( 2);
        currentTarget = numControl;
    }

    public void flyTargetWithTracking(OBControl number, PointF pt, float duration)
    {
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                robotController.eyesTrackPoint(target.position(),true);
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(pt,target),blockAnim),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void targetSnapToGrid(float duration)
    {
        if(target != null)
        {
            String numVal = (String)target.settings.get("value");
            OBLabel snapControl = (OBLabel)objectDict.get(String.format("num_%s",numVal));

            flyTargetWithTracking(target, snapControl.position(), 0.2f);

            lockScreen();
            snapControl.setColour(((OBLabel)target).colour());
            target.hide();
            snapControl.show();
            detachControl(target);
            target = currentTarget = null;

            unlockScreen();
        }
    }

    public void targetTrash(float duration) throws Exception
    {
        if(target != null)
        {
            flyTargetWithTracking(target,OB_Maths.worldLocationForControl(0.5f,-0.2f,objectDict.get("bagfront")),duration);
            target.setZPosition(objectDict.get("bagback").zPosition());
            flyTargetWithTracking(target,OB_Maths.worldLocationForControl(0.5f,0.5f,objectDict.get("bagfront")),0.1f);
            playSfxAudio("bag",false);
            animateBag(10, 0.03f);
            for(int i=0;i<3;i++)
            {
                animateBag(-10,0.06f);
                animateBag(10,0.06f);

            }
            animateBag(0, 0.03f);
            waitSFX();
            target.hide();
            detachControl(target);
            target = currentTarget = null;

        }
    }

    public void animateBag(int angle,float duration)
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(angle),objectDict.get("bagfront")),
                OBAnim.rotationAnim((float)Math.toRadians(angle),objectDict.get("bagback")))
                ,duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void demoStart() throws Exception
    {
        playSfxAudio("robot",false);
        robotController.moveRobot(robotStartPosition,1);
        playSFX(null);
        playSfxAudio("robot2",false);
        robotController.animateWave();
        waitSFX();
        waitForSecs(0.2f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f,0.5f), bounds()),-30,0.6f,"DEMO",0,0.4f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.1f,objectDict.get("box_1").getWorldFrame()),-40,0.5f,"DEMO",1,0.4f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.1f,objectDict.get("box_10").getWorldFrame()),-20,0.7f,"DEMO",2,0.4f);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f,0.5f), bounds()),-30,0.4f,"DEMO",3,0.4f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.65f,robotController.robot.frame()),-50,0.5f,"DEMO",4,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.65f,robotController.robot.frame()),0.1f,true);
        playSfxAudio("button",false);
        robotController.animateFrontButtonColour(buttonInactive, 0.1f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.65f,robotController.robot.frame()),0.1f,true);
        throwNumber("6");

        playAudioScene("DEMO",5,true);
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,currentTarget.frame()),-30,0.5f,true);
        playSfxAudio("drag",false);
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                thePointer.setPosition(OC_Generic.copyPoint(currentTarget.position()));
                robotController.eyesTrackPoint(currentTarget.position(),true);
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(objectDict.get("num_6").position(),currentTarget), blockAnim),0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        lockScreen();
        currentTarget.hide();
        objectDict.get("num_6").show();
        detachControl(currentTarget);
        currentTarget = null;

        unlockScreen();
        playAudio("correct");
        waitAudio();
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.1f,objectDict.get("num_6").frame()),-30,0.3f,"DEMO",6,0.3f);
        robotController.animateArmsDown();
        robotController.animateFrontButtonColour(buttonActive,0.2f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.65f,robotController.robot.frame()),0.5f,true);
        playSfxAudio("button",false);
        robotController.animateFrontButtonColour(buttonInactive,0.1f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.65f,robotController.robot.frame()),-50,0.1f,true);
        throwNumber("28");

        playAudioScene("DEMO",7,true);
        waitForSecs(0.2f);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f,0.5f), bounds()),-30,0.4f,"DEMO",8,0.4f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,currentTarget.frame()),-30,0.5f,"DEMO",9,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,currentTarget.frame()),0.1f,true);
        playSfxAudio("drag",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.5f,-0.5f,objectDict.get("bagfront").frame()),currentTarget), blockAnim),0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        target = currentTarget;
        playAudio("correct");
        targetTrash(0.2f);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        robotController.animateArmsDown();
    }

}
