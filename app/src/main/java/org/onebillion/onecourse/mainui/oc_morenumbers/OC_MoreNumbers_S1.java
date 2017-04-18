package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.animation.ArgbEvaluator;
import android.animation.TypeEvaluator;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_count100.OC_Count100_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 05/04/2017.
 */

public class OC_MoreNumbers_S1 extends OC_SectionController
{
    int textcolour, hilitecolour, hilitecolour2, numcolour, maskcolour;
    int currentPhase, targetColumn, currentBoxIndex;
    List<OBControl> targetBoxes, targetMasks;
    List<Integer>  targetNums;
    OBControl rowBorder;


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master1");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        textcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        hilitecolour2 = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour2"));
        OC_Count100_Additions.drawGrid(10,objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")),textcolour,false,this);

        targetBoxes = new ArrayList<>();
        targetNums = new ArrayList<>();
        targetMasks = new ArrayList<>();
        rowBorder = new OBControl();
        rowBorder.setBorderWidth(objectDict.get("box_1").borderWidth);
        rowBorder.setBorderColor(Color.RED);
        rowBorder.hide();
        rowBorder.setZPosition ( 2);
        attachControl(rowBorder);
        hideControls("num_.*");
        hideControls("box_.*");
        //eventIndex = 8;
        //FIX PLS
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1a();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        targetBoxes.clear();
        targetNums.clear();
        targetMasks.clear();
        currentPhase = 0;
        currentBoxIndex = 0;
        if(eventAttributes.get("column")  != null)
        {
            targetColumn = OBUtils.getIntValue(eventAttributes.get("column"));
            for(int i=0; i<10; i++)
                targetBoxes.add(objectDict.get(String.format("box_%d", i*10 + targetColumn)));

        }
        if(eventAttributes.get("numcolour")  != null)
            numcolour = OBUtils.colorFromRGBString(eventAttributes.get("numcolour"));
        if(eventAttributes.get("maskcolour")  != null)
            maskcolour = OBUtils.colorFromRGBString(eventAttributes.get("maskcolour"));

        else if(eventAttributes.get("target") != null &&
                eventAttributes.get("target").equals("box"))
        {
            String[] nums = eventAttributes.get("num").split(",");
            for(String num : nums)
                targetNums.add(Integer.valueOf(num));

            currentPhase = 1;
        }
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }


    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(eventAttributes.get("target").equals("column"))
        {
            if(status() == STATUS_WAITING_FOR_DRAG)
            {
                setStatus(STATUS_BUSY);
                final OBControl obj = findColumnTarget(pt,currentBoxIndex);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkColumn(pt,obj);
                    }
                });
            }

        }
        else if(eventAttributes.get("target").equals("box"))
        {
            if(status() == STATUS_AWAITING_CLICK)
            {
                OBControl cont  = null;
                for(OBControl box : filterControls("box_.*"))
                {
                    if(box.getWorldFrame().contains( pt.x, pt.y))
                    {
                        cont = box;
                        break;

                    }

                }
                if(cont != null)
                {
                    final OBControl box = cont;
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkBox(box);
                                }
                            }
                    );

                }

            }

        }
        else if(eventAttributes.get("target").equals("mask"))
        {
            if(status() == STATUS_AWAITING_CLICK)
            {
                final OBControl mask = finger(0,1,targetMasks,pt);
                if(mask != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkMask(mask);
                                }
                            }
                    );

                }
            }

        }
    }

    public void touchMovedToPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_BUSY);
            final OBControl obj = finger(0,1,targetBoxes,pt);
            OBUtils.runOnOtherThread(
                    new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkDrag(pt,obj);
                        }
                    }
            );
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    sendReprompt(setStatus(STATUS_WAITING_FOR_DRAG));
                }

            });

        }
    }


    public void checkColumn(PointF pt, OBControl obj) throws Exception
    {
        if (obj != null)
        {
            if(checkBoxPt(pt))
            {
                gotItRightBigTick(true);
                completeColumn(false);
                nextScene();
            }
            else
            {
                setStatus(STATUS_DRAGGING);

            }
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            long time = setStatus(STATUS_WAITING_FOR_DRAG);
            playAudioQueuedScene("INCORRECT",300,true);
            sendReprompt(time);
        }
    }

    public void checkBox(OBControl cont) throws Exception
    {

        int prevColour = cont.backgroundColor;
        cont.setBackgroundColor(hilitecolour);
        if(cont == objectDict.get(String.format("box_%s", targetNums.get(currentPhase-1))))
        {
            gotItRightBigTick(true);
            nextPhase();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            cont.setBackgroundColor(prevColour);
            setStatus(STATUS_AWAITING_CLICK);

            playAudioQueued((List<Object>)(Object)getAudioForScene(getPhase(), "INCORRECT"));

        }
    }

    public void checkMask(OBControl mask) throws Exception
    {
        mask.setBackgroundColor(OBUtils.highlightedColour(maskcolour));
        if((boolean)mask.propertyValue("correct"))
        {
            gotItRightBigTick(true);
            peelMask(mask,0.4f);
            waitForSecs(0.3f);
            playAudioQueuedScene("FINAL",300,true);
            waitForSecs(0.1f);
            hiliteColumn(targetColumn);
            waitForSecs(2f);
            resetColumn(targetColumn, (int)mask.propertyValue("value"), numcolour);
            waitForSecs(0.3f);
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            mask.setBackgroundColor ( maskcolour);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",300,false);

        }
    }

    public void checkDrag(PointF pt, OBControl obj) throws Exception
    {
        if (obj != null)
        {
            if(checkBoxPt(pt))
            {
                gotItRightBigTick(true);
                completeColumn(false);
                nextScene();
            }
            else
            {
                setStatus(STATUS_DRAGGING);

            }
        }
        else
        {
            gotItWrongWithSfx();
            long interval = setStatus(STATUS_WAITING_FOR_DRAG);
            waitSFX();
            sendReprompt(interval);
        }
    }

    public void peelMask(OBControl mask,float duration) throws Exception
    {
        playSfxAudio("peel",false);
        mask.setAnchorPoint(new PointF(1, 0.5f));
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("width",0,mask)),duration,true,OBAnim.ANIM_EASE_OUT,this);
        waitSFX();

    }

    public void hiliteColumn(int column) throws Exception
    {
        lockScreen();
        RectF rect = new RectF(objectDict.get(String.format("box_%d", column)).getWorldFrame());
        rect.union(objectDict.get(String.format("box_%d", 90+column)).getWorldFrame());

        rowBorder.setFrame(rect);
        rowBorder.show();
        for(int i=0; i<10; i++)
        {
            markLastNum((OBLabel)objectDict.get(String.format("num_%d", i*10 + column)));
            objectDict.get(String.format("box_%d", i*10 + column)).setBackgroundColor(hilitecolour);

        }
        unlockScreen();
        playSfxAudio("num_mark",true);

    }

    public void resetColumn(int column, int mark, int colour)
    {
        lockScreen();
        rowBorder.hide();
        for(int i=0; i<10; i++)
        {
            int num = i*10 + column;
            OBControl label = objectDict.get(String.format("num_%d", num));
            if(mark != -1)
            {
                if(mark == num)
                    colourEntireLabel((OBLabel)label,colour);
                else
                    colourEntireLabel((OBLabel)label,textcolour);

            }
            else
            {
                colourEntireLabel((OBLabel)label,colour);
            }
            objectDict.get(String.format("box_%d", num)).setBackgroundColor(Color.WHITE);

        }
        if(targetMasks.size() > 0)
        {
            for(OBControl cont : targetMasks)
                detachControl(cont);

        }
        unlockScreen();
    }

    public void startScene() throws Exception
    {
        if(eventAttributes.get("target").equals("column"))
        {
            OBMisc.doSceneAudio(-1,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else if(eventAttributes.get("target").equals("box"))
        {
            OBMisc.doSceneAudio(-1,getPhase(),setStatus(STATUS_AWAITING_CLICK),this);

        }
        else if(eventAttributes.get("target").equals("mask"))
        {
            createMasks();
            OBMisc.doSceneAudio(-1,setStatus(STATUS_AWAITING_CLICK),this);

        }
    }

    public void createMasks() throws Exception
    {
        lockScreen();
        String[] masks = eventAttributes.get("masks").split(",");
        for(String maskNum : masks)
        {
            OBControl newMask = new OBControl();
            newMask.setProperty("correct",false);
            if(maskNum == masks[0])
            {
                ((OBLabel)objectDict.get(String.format("num_%s",maskNum))).setColour(Color.RED);
                objectDict.get(String.format("box_%s",maskNum)).setBackgroundColor(hilitecolour);
                newMask.setProperty("correct",true);
                newMask.setProperty("value" ,Integer.valueOf(maskNum));

            }
            OBControl alingBox = objectDict.get(String.format("box_%s", maskNum));
            RectF frame = new RectF(alingBox.getWorldFrame());
            frame.inset(alingBox.borderWidth/2.0f, alingBox.borderWidth/2.0f);
            newMask.setFrame(frame);
            newMask.setFillColor(maskcolour);
            targetMasks.add(newMask);
            newMask.setZPosition(50);
            attachControl(newMask);
        }
        unlockScreen();
        playSfxAudio("cover_on",true);
    }


    public String getPhase()
    {
        return String.format("%s%d", currentEvent(), currentPhase);

    }
    public void nextPhase() throws Exception
    {
        if(currentPhase+1 > targetNums.size())
        {
            completeColumn(true);
            nextScene();

        }
        else
        {
            currentPhase++;
            startScene();
        }

    }

    public void sendReprompt(long startStatusTime)
    {
        reprompt(startStatusTime, OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMIND"), 300),3);
    }

    public OBControl findColumnTarget(PointF pt,int max)
    {
        List<OBControl> targets =targetBoxes.subList(0, max<9 ? max+2 : 10);
        OBControl c =finger(0,1,targets,pt);
        return c;

    }

    public boolean checkBoxPt(PointF pt)
    {
        OBControl box = targetBoxes.get(currentBoxIndex);
        if(currentBoxIndex == 0)
        {
            box.setFillColor(hilitecolour);

        }
        if(pt.y > box.getWorldFrame().bottom)
        {
            currentBoxIndex++;
            targetBoxes.get(currentBoxIndex).setFillColor(hilitecolour);

        }
        if(currentBoxIndex >=9)
        {
            return true;

        }
        else
        {
            return false;

        }
    }

    public void markLastNum(OBLabel label)
    {
        label.setHighRange(label.text().length() - 1, label.text().length(), Color.RED);
    }

    public void colourEntireLabel(OBLabel label,int colour)
    {
        label.setHighRange(-1, -1, Color.BLACK);
        label.setColour(colour);
    }

    public OBAnim firstNumsAnim(final OBLabel label,int colour)
    {
        final ArgbEvaluator evaluator = new ArgbEvaluator();
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                label.setHighRange(0,label.text().length()-1,(int)evaluator.evaluate(frac, Color.BLACK, Color.RED));
            }
        };

        return anim;
    }

    public OBAnim pointerHiliteAnim(final int column)
    {

        return new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(int i=0; i<10; i++)
                {
                    OBControl box = objectDict.get(String.format("box_%d", i*10 + column));
                    if(box.backgroundColor != hilitecolour && thePointer.position().y > box.getWorldFrame().top)
                        box.setBackgroundColor(hilitecolour);

                }
            }
        };

    }

    public void completeColumn(boolean pointerHilite) throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_MIDDLE);
        List<String> demoAudio = currentPhase == 0 ?  getAudioForScene(currentEvent(),"DEMO") : getAudioForScene(getPhase(),"DEMO");
        PointF startPoint = OB_Maths.locationForRect((targetColumn == 10 ? 0.7f : 0.5f),0,objectDict.get(String.format("num_%d",targetColumn)).getWorldFrame());
        List<OBAnim> pointerAnims = new ArrayList<>();
        movePointerToPoint(startPoint,0.5f,true);
        waitForSecs(0.1f);
        playAudio(demoAudio.get(0));
        startPoint.y = objectDict.get("box_100").getWorldFrame().bottom;
        pointerAnims.add(OBAnim.moveAnim(startPoint,thePointer));
        if(pointerHilite)
            pointerAnims.add(pointerHiliteAnim(targetColumn));
        OBAnimationGroup.runAnims(pointerAnims,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitAudio();
        hiliteColumn(targetColumn);
        waitForSecs(0.2f);
        thePointer.hide();
        playAudio(demoAudio.get(1));
        waitAudio();
        waitForSecs(0.3f);
        for(int i = 0; i < 10; i++)
        {
            playAudio(demoAudio.get(i+2));
            int labelNum = i*10 + targetColumn;
            OBLabel label = (OBLabel)objectDict.get(String.format("num_%d",labelNum));
            lockScreen();
            label.setColour(Color.RED);
            label.setHighRange(0,label.text().length()-1, Color.BLACK);
            unlockScreen();

            OBAnimationGroup.runAnims(Arrays.asList(firstNumsAnim(label,Color.RED),
                    OBAnim.colourAnim("backgroundColor",hilitecolour2,objectDict.get(String.format("box_%d",labelNum))))
                    ,0.3f,true,OBAnim.ANIM_LINEAR,this);
            waitAudio();
        }
        waitForSecs(1f);
        resetColumn(targetColumn, -1, numcolour);
        if(OBUtils.getBooleanValue(eventAttributes.get("reset")))
        {
            waitForSecs(1f);
            lockScreen();
            for(OBControl control : filterControls("num_.*"))
                ((OBLabel)control).setColour(textcolour);

            for(OBControl control : filterControls("box_.*"))
                control.setBackgroundColor(Color.WHITE);

            unlockScreen();
            waitForSecs(0.2f);
        }

    }

    public void pointerPointColumn(int column,int audioIndex, boolean hide) throws Exception
    {
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0,objectDict.get(String.format("num_%d", column)).getWorldFrame()),0.5f,true);
        playAudioScene("DEMO",audioIndex,false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.5f,1.05f,objectDict.get(String.format("num_%d", column+90)).getWorldFrame()),thePointer),
                pointerHiliteAnim(column)),1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(1.5f);
        waitAudio();
        if(hide)
        {
            lockScreen();
            for(int i=0; i<10; i++)
            {
                objectDict.get(String.format("box_%d",i*10 + column)).setBackgroundColor(Color.WHITE);
            }
            unlockScreen();

        }

    }

    public void demo1a() throws Exception
    {
        waitForSecs(0.4f);
        lockScreen();
        showControls("box_.*");
        playSfxAudio("grid_appears",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.4f);
        playSfxAudio("numbers_appear",false);
        for(OBControl num : OBUtils.randomlySortedArray(filterControls("num_.*")))
        {
            num.show();
            waitForSecs(0.025f);
        }
        waitSFX();
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_45").getWorldFrame()),0,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("box_1").getWorldFrame()),0.5f,true);
        objectDict.get("box_1").setFillColor(hilitecolour);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.2f);
        objectDict.get("box_1").setFillColor(Color.WHITE);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("box_100").getWorldFrame()),0.8f,true);
        objectDict.get("box_100").setFillColor(hilitecolour);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.2f);
        objectDict.get("box_100").setFillColor(Color.WHITE);
        pointerPointColumn(1,3,true);
        pointerPointColumn(5,4,true);
        pointerPointColumn(8,5,false);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.5f,objectDict.get("num_98").getWorldFrame()),0,0.5f,"DEMO",6,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,objectDict.get("box_98").getWorldFrame()),0,0.5f,"DEMO",7,0.3f);
        hiliteColumn(8);
        waitForSecs(2f);
        resetColumn(8,-1,textcolour);
        pointerPointColumn(4,8,false);
        waitForSecs(0.3f);
        hiliteColumn(4);
        waitForSecs(2f);
        resetColumn(4,-1,textcolour);
        waitForSecs(0.5f);
        thePointer.hide();
        nextScene();
    }

    public void demo1k() throws Exception
    {
        createMasks();
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_45").frame()),0,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(2f,2.6f,objectDict.get("num_45").frame()),0,0.5f,"DEMO",1,0.3f);
        OBControl mask = targetMasks.get(0);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.1f,mask.frame()),0,0.5f,"DEMO",2,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,mask.frame()),0.25f,true);
        mask.setBackgroundColor(OBUtils.highlightedColour(maskcolour));
        playAudio("click");
        movePointerToPoint(OB_Maths.locationForRect(1.2f,1.1f,mask.frame()),0.25f,true);
        waitAudio();
        peelMask(mask,0.4f);
        waitForSecs(0.3f);
        PointF loc = OB_Maths.locationForRect(0.5f,0f,objectDict.get(String.format("num_%d",targetColumn)).frame());
        movePointerToPoint(loc,0.5f,true);
        waitForSecs(0.1f);
        playAudioScene("DEMO",4,false);
        loc.y = objectDict.get("box_100").bottom();
        movePointerToPoint(loc,1f,true);
        waitAudio();
        waitForSecs(0.1f);
        hiliteColumn(targetColumn);
        waitForSecs(2f);
        resetColumn(targetColumn,-1,textcolour);
        waitForSecs(0.5f);
        thePointer.hide();
        nextScene();

    }


}
