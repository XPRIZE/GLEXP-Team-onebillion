package com.maq.xprize.onecourse.hindi.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.provider.Telephony;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.mainui.oc_count100.OC_CountingRobot;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 06/04/2017.
 */

public class OC_MoreNumbers_S3o extends OC_SectionController
{
    OC_CountingRobot robotController;
    Map<Integer,OBControl> oddBox, evenBox, eventTargets;
    List<PointF> evenLocs, oddLocs;
    int textcolour, evencolour, oddcolour, buttonActive, buttonInactive;
    int currentPhase, robotTap;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master3o");
        robotController = new OC_CountingRobot((OBGroup)objectDict.get("robot"),this);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        robotController.robot.setZPosition (2);
        oddBox = new ArrayMap<>();
        evenBox = new ArrayMap<>();
        eventTargets = new ArrayMap<>();
        evenLocs = new ArrayList<>();
        oddLocs = new ArrayList<>();
        objectDict.get("box_1").setZPosition(1);
        objectDict.get("box_2").setZPosition(1);
       // ((OBPath) objectDict.get("box_1")).sizeToBoundingBoxIncludingStroke();
        //((OBPath) objectDict.get("box_2")).sizeToBoundingBoxIncludingStroke();
        List<Float> xLocs = Arrays.asList(0.22f,0.54f,0.86f);
        List<Float> yLocs = Arrays.asList(0.2f,0.5f,0.8f);
        for(int i = 0;
            i<9;
            i++)
        {
            float xLoc = xLocs.get(i%3) ;
            float yLoc = yLocs.get((int)Math.round(Math.floor(i/3))) ;
            oddLocs.add(OB_Maths.locationForRect(xLoc,yLoc,objectDict.get(String.format("box_%d", 1)).getWorldFrame()));
            evenLocs.add(OB_Maths.locationForRect(xLoc,yLoc,objectDict.get(String.format("box_%d", 2)).getWorldFrame()));

        }
        textcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        evencolour = OBUtils.colorFromRGBString(eventAttributes.get("evencolour"));
        oddcolour = OBUtils.colorFromRGBString(eventAttributes.get("oddcolour"));
        buttonActive = OBUtils.colorFromRGBString(eventAttributes.get("buttonactive"));
        buttonInactive = OBUtils.colorFromRGBString(eventAttributes.get("buttoninactive"));
        robotController.showFront();
        robotController.setButtonColour ( buttonInactive);
        OC_MoreNumbers_Additions.buttonSet(2,this);
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3o();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        eventTargets.clear();
        oddBox.clear();
        evenBox.clear();
        currentPhase = 1;
        robotTap = 0;
        List<Float> locs = OBMisc.stringToFloatList(eventAttributes.get("locs"),",");
        if(eventAttributes.get("num") != null)
            createLabels(OBMisc.stringToIntegerList(eventAttributes.get("num"),","),locs);
        if(eventAttributes.get("random") != null)
        {
            List<Integer> nums = OBMisc.stringToIntegerList(eventAttributes.get("random"), ",");
            createLabels(getRandomNums(nums.get(0),nums.get(1),nums.get(2),nums.get(3)), locs);
        }

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(currentPhase == 1 && status() == STATUS_AWAITING_CLICK)
        {
            if(finger(0,1,Arrays.asList(robotController.robot.objectDict.get("buttonlayer")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkRobotButton();
                    }
                });


            }

        }
        else if(currentPhase == 2 && status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl cont = finger(0,2, new ArrayList(eventTargets.values()),pt);
            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                removeFromDictionary(oddBox,cont);
                removeFromDictionary(evenBox,cont);
                OBMisc.prepareForDragging(cont,pt,this);
                setStatus(STATUS_DRAGGING);
            }
            else if(finger(0,1,Arrays.asList(robotController.robot.objectDict.get("buttonlayer")),pt) != null)
            {
                robotTap++;
                if(robotTap%5 == 0)
                    robotAngry(false);

            }
            else if(finger(0,2,Arrays.asList(objectDict.get("button_arrow")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkArrowButton();
                    }
                });
            }

        }

    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            target.setPosition ( OB_Maths.AddPoints(pt, dragOffset));
            robotController.eyesTrackPoint(target.position(),true);
        }
    }

    public void touchUpAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            final OBLabel label = (OBLabel)target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop(label);
                }
            });

        }
    }


    public void checkRobotButton() throws Exception
    {
        playSfxAudio("robot_click",false);
        robotController.setButtonColour(OBUtils.highlightedColour(buttonActive));
        throwNumbers();
        currentPhase = 2;
        startScene();
    }

    public void checkArrowButton() throws Exception
    {
        if(oddBox.size() + evenBox.size() < eventTargets.size())
        {
            playAudioQueuedScene(getPhase(), "INCORRECT",0.3f, false);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
        else
        {
            OC_MoreNumbers_Additions.buttonSet(1, this);
            List<OBControl> wrongNums = new ArrayList<>();

            List<OBControl> boxes = new ArrayList<>(evenBox.values());

            for(OBControl cont : boxes)
            {
                if(!(boolean)cont.propertyValue("is_even"))
                {
                    removeFromDictionary(evenBox, cont);
                    wrongNums.add(cont);
                }

            }

            boxes = new ArrayList<>(oddBox.values());

            for(OBControl cont : boxes)
            {
                if((boolean)cont.propertyValue("is_even"))
                {
                    removeFromDictionary(oddBox, cont);
                    wrongNums.add(cont);

                }

            }
            if(wrongNums.size() == 0)
            {
                gotItRightBigTick(true);
                OC_MoreNumbers_Additions.buttonSet(2,this);
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_HAPPY);
                playAudioQueuedScene(getPhase(), "FINAL",0.3f, true);
                if(currentEvent() != events.get(events.size()-1))
                {
                    waitForSecs(0.7f);
                    lockScreen();
                    for(OBControl label : eventTargets.values())
                        detachControl(label);
                    playSfxAudio("pop",false);

                    unlockScreen();
                    waitSFX();
                    waitForSecs(0.5f);

                }
                nextScene();

            }
            else
            {
                colourNums(wrongNums, Color.RED);
                robotAngry(true);
                gotItWrongWithSfx();
                waitSFX();
                colourNums(wrongNums, textcolour);
                List<OBAnim> anims = new ArrayList<>();
                for(OBControl label : wrongNums)
                    anims.add(OBAnim.moveAnim((PointF)label.propertyValue("startloc") ,label));
                OBAnimationGroup.runAnims(anims,0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                OC_MoreNumbers_Additions.buttonSet(0,this);
                playAudioQueuedScene(getPhase(), "INCORRECT2",0.3f, false);
                setStatus(STATUS_WAITING_FOR_DRAG);
            }

        }

    }


    public void checkDrop(OBLabel label) throws Exception
    {

        boolean inBox = false;
        for(int j=1 ; j<=2; j++)
        {
            if(objectDict.get(String.format("box_%d",j)).getWorldFrame().contains(label.position().x, label.position().y))
            {
                inBox = true;
                PointF targetPoint = null;
                Map<Integer,OBControl> box = j==1? oddBox : evenBox;
                float dist = -1;
                int closest=-1;
                for(int i=0; i<9; i++)
                {
                    if(box.get(i) == null)
                    {
                        PointF loc =  j==1? oddLocs.get(i) : evenLocs.get(i) ;
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
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(targetPoint,label)),0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                break;
            }
        }
        if(inBox == false)
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)label.propertyValue("startloc"),label)  )
                    ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            label.setZPosition(label.zPosition()- 10);
        }
        setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public void robotAngry(boolean withSad)
    {
        switch(withSad ? OB_Maths.randomInt(1, 3) : OB_Maths.randomInt(2, 3))
        {
            case 1:
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_SAD);
                break;
            case 2:
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_ANGRY);
                break;
            case 3:
                robotController.setFaceExpression(OC_CountingRobot.ROBOT_ANGRY2);
                break;

        }

    }

    public void startScene() throws Exception
    {
        if(currentPhase==1)
        {
            robotController.animateArmsDown();
            robotController.animateFrontButtonColour(buttonActive, 0.2f);
        }
        else
        {
            OC_MoreNumbers_Additions.buttonSet(0, this);

        }
        performSel("demo",String.format("%s%d",currentEvent(),currentPhase));
        OBMisc.doSceneAudio(4,getPhase(),currentPhase==1?setStatus(STATUS_AWAITING_CLICK): setStatus(STATUS_WAITING_FOR_DRAG),this);
    }


    public void colourNums(List<OBControl> nums, int colour)
    {
        lockScreen();
        for(OBControl label : nums)
            ((OBLabel)label).setColour(colour);

        unlockScreen();
    }

    public void removeFromDictionary(Map<Integer,OBControl> dict, OBControl obj)
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

    public List<Integer> getRandomNums(int from, int to, int size, int groupSize)
    {
        List<Integer> evenNums = new ArrayList<>();
        List<Integer> oddNums = new ArrayList<>();
        List<Integer> selectedNums = new ArrayList<>();
        for(int i = from; i < to; i++)
        {
            if(i%2 == 0)
                evenNums.add(i);
            else
                oddNums.add(i);

        }
        evenNums = new ArrayList<>(OBUtils.randomlySortedArray(evenNums));
        oddNums = new ArrayList<>(OBUtils.randomlySortedArray(oddNums));
        for(int i = 0; i < groupSize; i++)
        {
            selectedNums.add(evenNums.get(0));
            selectedNums.add(oddNums.get(0));
            evenNums.remove(0);
            oddNums.remove(0);
        }
        int currentSize = selectedNums.size();
        for(int i=currentSize; i <size; i++)
        {
            if(OB_Maths.randomInt(0,1) == 0)
            {
                selectedNums.add(evenNums.get(0));
                evenNums.remove(0);

            }
            else
            {
                selectedNums.add(oddNums.get(0));
                oddNums.remove(0);
            }
        }
        return OBUtils.randomlySortedArray(selectedNums);
    }


    public void createLabels(List<Integer> num, List<Float> locs)
    {
        float fontSize = 57*robotController.robot.height()/180.0f;
        for(int i=0; i<num.size(); i++)
        {
            int value = (int)num.get(i) ;
            OBLabel numLabel = new OBLabel(String.format("%d",value),OBUtils.standardTypeFace(),fontSize);
            numLabel.setProperty("num_value", value);
            numLabel.setProperty("is_even", value%2 == 0);
            eventTargets.put(value,numLabel);
            PointF startloc = OB_Maths.locationForRect(locs.get(i*2),locs.get((i*2)+1), bounds());
            numLabel.setProperty("startloc",(PointF)startloc);
            numLabel.setColour(textcolour);
            numLabel.setPosition(OB_Maths.worldLocationForControl(0.5f,0.5f,robotController.robot.objectDict.get("face")));
            numLabel.setScale(0.2f*robotController.robot.height()/numLabel.height());
            attachControl(numLabel);
            numLabel.hide();
            numLabel.setZPosition(1.5f);
        }
    }


    public void throwNumbers() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        robotController.setFaceExpression(OC_CountingRobot.ROBOT_SIDE_OPEN);
        for(OBControl numLabel : eventTargets.values())
        {
            numLabel.show();
            PointF originalPosition = OBMisc.copyPoint((PointF)numLabel.propertyValue("startloc"));
            float distance = OB_Maths.PointDistance(numLabel.position(), originalPosition);
            Path path = OBUtils.SimplePath(numLabel.position(), originalPosition, -distance / 5);
            anims.add(OBAnim.pathMoveAnim(numLabel,path,false,0));
            anims.add(OBAnim.rotationAnim((float)Math.toRadians(360)*OB_Maths.randomInt(1, 4),numLabel));
            anims.add(OBAnim.scaleAnim(1,numLabel));

        }
        OBAnimationGroup.runAnims(anims,0.75,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        robotController.setFaceExpression(OC_CountingRobot.ROBOT_SIDE_HAPPY);
        robotController.eyesTrackPoint(OB_Maths.locationForRect(0.5f,0.8f,this.bounds()), true);
        robotController.animateFrontButtonColour(buttonInactive, 0.1f);
        robotController.animateArmsUp();
    }


    public String getPhase()
    {
        return String.format("%s%d", currentEvent(), currentPhase);

    }


    public void demo3o() throws Exception
    {
        waitForSecs(0.5f);
        List<String> demoAudio = getAudioForScene(getPhase(),("DEMO"));
        playAudio(demoAudio.get(0));
        waitAudio();
        waitForSecs(0.3f);
        PointF loc = OBMisc.copyPoint(robotController.robot.position());
        robotController.robot.setRight ( 0);
        robotController.robot.show();
        lockScreen();
        objectDict.get("box_1").show();
        objectDict.get("box_2").show();

        unlockScreen();
        waitForSecs(0.5f);
        playSfxAudio("robot_move",false);
        robotController.moveRobot(loc,1);
        playSFX(null);
        playSfxAudio("robot_wave",false);
        robotController.animateWave();
        waitSFX();
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.8f,robotController.robot.objectDict.get("buttonlayer").getWorldFrame())
                ,-40,0.5f,true);
        playAudio(demoAudio.get(1));
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();

    }

    public void pointerDragNum(int num, int boxNum, String audio) throws Exception
    {
        OBControl label = eventTargets.get(num);
        OBControl box = objectDict.get(String.format("box_%d", boxNum));
        movePointerToPoint(OB_Maths.locationForRect(0.6f, 1.1f, box.getWorldFrame())
                , -30, 0.7f, true);
        playAudio(audio);
        waitAudio();
        waitForSecs(0.5f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f, 0.5f, label.frame())
                , -30, 0.7f, true);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.07f, 0.14f, box.getWorldFrame()), thePointer),
                OBMisc.attachedAnim(thePointer, Arrays.asList(label)))
                , 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        movePointerToPoint(OB_Maths.locationForRect(1.5f, 0.5f, label.frame()), -30, 0.3f, true);
        ((OBLabel) label).setColour(boxNum == 1 ? oddcolour : evencolour);
        eventTargets.remove(num);
        waitForSecs(0.5f);
    }

    public void demo3o2() throws Exception
    {
        List<String> demoAudio = getAudioForScene(getPhase(),"DEMO");
        loadPointer(POINTER_LEFT);
        pointerDragNum(5,1,demoAudio.get(0));
        pointerDragNum(2,2,demoAudio.get(1));
        movePointerToPoint(OB_Maths.locationForRect(0.95f,0.8f,this.bounds()),-30,0.3f,true);
        playAudio(demoAudio.get(2));
        waitAudio();
        waitForSecs(0.3f);
        playAudio(demoAudio.get(3));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1f,objectDict.get("button_arrow").frame()),-30,0.3f,true);
        playAudio(demoAudio.get(4));
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
    }

}
