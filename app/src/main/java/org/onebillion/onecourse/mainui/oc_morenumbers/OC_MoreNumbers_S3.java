package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
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

import java.security.cert.TrustAnchor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 06/04/2017.
 */

public class OC_MoreNumbers_S3 extends OC_SectionController
{
    int textcolour, hilitecolour, evencolour, oddcolour, currentcolour;
    List<Integer> targetNums;
    List<OBControl> activeBoxes, targetBoxes;
    int currentIndex;
    boolean firstHand;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master3");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        textcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        evencolour = OBUtils.colorFromRGBString(eventAttributes.get("evencolour"));
        oddcolour = OBUtils.colorFromRGBString(eventAttributes.get("oddcolour"));
        OC_Count100_Additions.drawGrid(10, objectDict.get("workrect"),OBUtils.colorFromRGBString(eventAttributes.get("linecolour")), textcolour, false, this);
        targetNums = new ArrayList<>();
        activeBoxes = new ArrayList<>();
        targetBoxes = new ArrayList<>();
        hideControls("num_.*");
        hideControls("box_.*");
        firstHand = true;
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3a();
            }

        }) ;
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        targetNums.clear();
        activeBoxes.clear();
        currentIndex = 0;
        targetNums = OBMisc.stringToIntegerList(eventAttributes.get("num"),",");
        if(eventAttributes.get("target").equals("num"))
        {
            int rowNum = OBUtils.getIntValue(eventAttributes.get("hiliterow"));
            for(int i=1; i<=10; i++)
                activeBoxes.add( objectDict.get(String.format("box_%d", (rowNum-1)*10 + i)));

        }
        if(eventAttributes.get("target").equals("box")||
                eventAttributes.get("target").equals("oddnum")||
                eventAttributes.get("target").equals("evennum"))
        {
            activeBoxes.addAll(filterControls("box_.*"));
        }
        if(eventAttributes.get("target").equals("column"))
        {
            for(int num : targetNums)
                activeBoxes.add( objectDict.get(String.format("box_%d", num)));
            if((int)targetNums.get(0) %2 == 0)
                currentcolour = evencolour;
            else
                currentcolour = oddcolour;

        }
    }

    public void doMainXX() throws Exception
    {
        startScene();

    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            OBControl cont  = null;
            for(OBControl box : activeBoxes)
            {
                if(box.getWorldFrame().contains( pt.x, pt.y))
                {
                    cont = box;
                    break;

                }

            }
            final OBControl box = cont;
            if(cont != null && cont.isEnabled())
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        if(eventAttributes.get("target").equals("box"))
                        {

                            checkBox(box);
                        }
                        else if(eventAttributes.get("target").equals("num"))
                        {
                            checkNum(box);
                        }
                        else if(eventAttributes.get("target").equals("evennum")
                                || eventAttributes.get("target").equals("oddnum"))
                        {
                            checkOddEvenNum(box);
                        }
                    }
                });

            }

        }
        else if(status() == STATUS_WAITING_FOR_DRAG)
        {
            setStatus(STATUS_BUSY);
            if(targetBoxes.size() == 0)
            {
                final OBControl cont = finger(0,0,activeBoxes,pt);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDragColumn1(cont);
                    }
                });

            }
            else
            {
                final OBControl obj = findColumnTarget(pt,currentIndex);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDragColumn2(obj, pt, false);
                    }
                });
            }
        }
    }

    public void touchMovedToPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {

            setStatus(STATUS_BUSY);
            final OBControl obj = finger(0,1,targetBoxes,pt);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDragColumn2(obj, pt, true);
                }
            });

        }
    }

    public void touchUpAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    playAudioQueuedScene("INCORRECT",0.3f,false);
                    setStatus(STATUS_WAITING_FOR_DRAG);
                }
            });
        }

    }


    public void checkBox(OBControl cont) throws Exception
    {
        if(cont == objectDict.get(String.format("box_%s", targetNums.get(currentIndex))))
        {
            cont.setBackgroundColor ( evencolour);
            playAudioScene("FINAL",currentIndex,false);
            currentIndex++;
            cont.disable();
            if(currentIndex >= targetNums.size())
            {
                waitAudio();
                gotItRightBigTick(true);
                if(!performSel("demoFin",currentEvent()))
                {
                    waitForSecs(0.2f);
                    playAudioQueuedScene("FINAL2",0.3f,true);
                    waitForSecs(0.4f);

                }
                nextScene();

            }
            else
            {
                reprompt(setStatus(STATUS_AWAITING_CLICK),OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMIND"), 300),4);
            }
        }
        else
        {
            cont.setBackgroundColor(hilitecolour);
            gotItWrongWithSfx();
            waitSFX();
            cont.setBackgroundColor(Color.WHITE);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void checkNum(OBControl cont) throws Exception
    {
        int val = (int)cont.propertyValue("num_value") ;
        OBLabel numCont = (OBLabel)objectDict.get(String.format("num_%d",val));
        numCont.setColour(Color.RED);
        if(val%2 == 0)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            numCont.setColour ( textcolour);
            countRow();
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            numCont.setColour ( textcolour);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }

    public void checkOddEvenNum(OBControl cont) throws Exception
    {
        int val = (int)cont.settings.get("num_value") ;
        boolean correct = false;
        if(val%2 == 0)
        {
            cont.setBackgroundColor ( evencolour);
            correct = eventAttributes.get("target").equals("evennum");

        }
        else
        {
            cont.setBackgroundColor ( oddcolour);
            correct = eventAttributes.get("target").equals("oddnum");

        }
        if(correct)
        {
            currentIndex++;
            cont.disable();
            if(currentIndex == 4)
            {
                gotItRightBigTick(true);
                waitForSecs(0.5f);
                playAudioQueuedScene("FINAL",0.3f,true);
                nextScene();

            }
            else
            {
                gotItRightBigTick(false);
                reprompt(setStatus(STATUS_AWAITING_CLICK),OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMIND"), 300),4);
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            cont.setBackgroundColor(Color.WHITE);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }


    public  void checkDragColumn1(OBControl cont) throws Exception
    {
        if(cont != null)
        {
            int val = (int)cont.settings.get("num_value") ;
            for(int i=0; i<10; i++)
                targetBoxes.add(objectDict.get(String.format("box_%d", val + i*10)));
            cont.setBackgroundColor ( currentcolour);
            setStatus(STATUS_DRAGGING);
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_WAITING_FOR_DRAG);
            waitSFX();
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT2",0.3f,false);

        }
    }


    public void checkDragColumn2(OBControl cont, PointF pt, boolean withReprompt) throws Exception
    {
        if (cont != null)
        {
            if(checkBoxPt(pt))
            {
                columnComplete();
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
            if(withReprompt)
                sendReprompt(interval);
        }
    }

    public void sendReprompt(long startStatusTime)
    {
        reprompt(startStatusTime,OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMIND"), 300),3);
    }

    public void markLastNum(OBLabel label)
    {
        label.setHighRange(label.text().length() - 1, label.text().length(), Color.RED);
    }

    public void colourEntireLabel(OBLabel label, int colour)
    {
        label.setHighRange(-1, -1, Color.BLACK);
        label.setColour(colour);
    }

    public void markLastNumsInColumn(int column)
    {
        lockScreen();
        for(int i=0; i<10; i++)
            markLastNum((OBLabel)objectDict.get(String.format("num_%d",column+i*10)));
        unlockScreen();
    }

    public void columnComplete() throws Exception
    {
        OBControl cont = targetBoxes.get(0);
        int val = (int)cont.propertyValue("num_value") ;
        playAudio("correct");
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("FINAL",targetNums.indexOf(val),true);
        waitForSecs(0.3f);
        markLastNumsInColumn(val);
        activeBoxes.remove(cont);
        currentIndex = 0;
        targetBoxes.clear();
        if(activeBoxes.size() == 0)
        {
            waitForSecs(0.5f);
            gotItRightBigTick(true);
            waitForSecs(0.2f);
            playAudioQueuedScene("FINAL2",0.3f,true);
            waitForSecs(0.5f);
            resetGrid(true);
            waitForSecs(0.5f);
            performSel("demoFin",currentEvent());
            nextScene();

        }
        else
        {
            sendReprompt(setStatus(STATUS_WAITING_FOR_DRAG));

        }
    }

    public void startScene() throws Exception
    {
        if(eventAttributes.get("target").equals("column"))
        {
            OBMisc.doSceneAudio(-1,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
        }
    }

    public OBControl findColumnTarget(PointF pt,int max)
    {
        List<OBControl> targets = targetBoxes.subList(0, max<9 ? max+2 : 10);
        OBControl c =finger(0,1,targets,pt);
        return c;
    }

    public boolean checkBoxPt(PointF pt)
    {
        OBControl box = targetBoxes.get(currentIndex);
        if(currentIndex == 0)
        {
            box.setFillColor(currentcolour);

        }
        if(pt.y > box.getWorldFrame().bottom)
        {
            currentIndex++;
            targetBoxes.get(currentIndex).setBackgroundColor(currentcolour);
        }
        if(currentIndex >=9)
        {
            return true;
        }
        else
        {
            return false;

        }
    }

    public void hiliteBox(int num, int colour)
    {
        OBControl cont = objectDict.get(String.format("box_%d", num));
        cont.setBackgroundColor(colour);
        cont.disable();
    }

    public void unhiliteBoxes(List<Integer> nums)
    {
        for(int num : nums)
        {
            OBControl cont = objectDict.get(String.format("box_%d", num));
            cont.setBackgroundColor(Color.WHITE);
            cont.enable();
            ((OBLabel)objectDict.get(String.format("num_%s", num))).setColour(textcolour);
        }

    }

    public void resetGrid(boolean withNum)
    {
        lockScreen();
        for(OBControl control : filterControls("box_.*"))
        {
            control.enable();
            control.setBackgroundColor(Color.WHITE);
        }

        if(withNum)
        {
            for(OBControl control : filterControls("num_.*"))
            {
                colourEntireLabel((OBLabel)control,textcolour);
            }
        }
        unlockScreen();
    }

    public void blendHalfGrid(boolean blendOut)
    {
        List<OBAnim> anims  = new ArrayList<>();
        for(int i=51; i<=100; i++)
        {
            OBControl box = objectDict.get(String.format("box_%d",i));
            if(blendOut)
                box.disable();
            else
                box.enable();
            anims.add(OBAnim.opacityAnim(blendOut ? 0.2f : 1, objectDict.get(String.format("num_%d",i))));

        }
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_LINEAR,this);
    }

    public float rotationForNum(int num)
    {
        PointF target = OBMisc.copyPoint(objectDict.get(String.format("box_%d", num)).getWorldPosition());
        return ((1-(target.x/this.bounds().width()*1.0f))*-40.0f)-10;
    }

    public void hiliteActiveBoxes(boolean on)
    {
        lockScreen();
        for(OBControl cont : activeBoxes)
            cont.setBackgroundColor ( on ? hilitecolour : Color.WHITE);

        unlockScreen();
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
                    if(box.backgroundColor != currentcolour && thePointer.position().y > box.getWorldFrame().top)
                        box.setBackgroundColor(currentcolour);
                }
            }
        };
    }

    public void hiliteColumns(List<Integer> columns, int colour)
    {
        lockScreen();
        for(int num : columns)
        {
            for(int i=0; i<10; i++)
            {
                OBControl box = objectDict.get(String.format("box_%d", i*10 + num));
                box.setBackgroundColor(colour);
            }
        }
        unlockScreen();
    }

    public void countRow() throws Exception
    {
        waitForSecs(0.5f);
        hiliteActiveBoxes(false);
        waitForSecs(0.3f);
        int index = 0;
        if(getAudioForScene(currentEvent(),"FINAL").size() > 5)
        {
            playAudioScene("FINAL",index++,true);
            waitForSecs(0.3f);

        }
        for(int i=1; i<=5; i++)
        {
            OBControl cont = activeBoxes.get((i*2)-1);
            cont.setBackgroundColor ( evencolour);
            cont.disable();
            playAudioScene("FINAL",index++,true);
            waitForSecs(0.3f);
        }
    }

    public void demo3a() throws Exception
    {
        waitForSecs(0.4f);
        lockScreen();
        showControls("box_.*");
        playSfxAudio("grid_appears",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.4f);
        for(int i=0; i<10; i++)
        {
            lockScreen();
            for(int j=1; j<=10; j++)
            {
                objectDict.get(String.format("num_%d",i*10+j)).show();

            }
            playSFX(String.format("note_%d",i));
            unlockScreen();
            waitForSecs(0.35f);
            waitSFX();

        }
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_45").getWorldFrame()),rotationForNum(45),0.5f,"DEMO",0,0.4f);
        int index = 1;
        for(int i=2; i<=20; i+=2)
        {
            OBControl box = objectDict.get(String.format("box_%d",i));
            movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,box.getWorldFrame()),rotationForNum(i),0.4f,true);
            box.setBackgroundColor ( evencolour);
            box.disable();
            playAudioScene("DEMO",index++,true);

        }
        movePointerToPoint(OB_Maths.locationForRect(1f,1.1f,objectDict.get("box_11").getWorldFrame()),rotationForNum(21),0.6f,true);
        playAudioScene("DEMO",index++,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.1f,objectDict.get("box_20").getWorldFrame()),rotationForNum(30),2f,true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        blendHalfGrid(true);
        startScene();
    }

    public void demo3c() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_75").getWorldFrame()),rotationForNum(75),0.5f,"DEMO",0,0.4f);
        int index = 1;
        for(int i=22; i<=30; i+=2)
        {
            OBControl box = objectDict.get(String.format("box_%d",i+10));
            movePointerToPoint(OB_Maths.locationForRect(1.1f,1f,box.getWorldFrame()),rotationForNum(i),0.5f,true);
            lockScreen();
            for(int j=0; j<5; j++)
            {
                markLastNum((OBLabel)objectDict.get(String.format("num_%d",(index*2)+ j*10)));

            }

            unlockScreen();
            playAudioScene("DEMO",index,true);
            waitForSecs(0.3f);
            lockScreen();
            for(int j=0; j<5; j++)
            {
                colourEntireLabel((OBLabel)objectDict.get(String.format("num_%d",(index*2)+ j*10)),textcolour);
            }
            unlockScreen();
            index++;

        }
        moveScenePointer(OB_Maths.locationForRect(1f,1.1f,objectDict.get("box_40").getWorldFrame()),rotationForNum(40),0.5f,"DEMO",index++,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.1f,objectDict.get("box_70").getWorldFrame()),rotationForNum(70),0.5f,true);
        waitForSecs(0.5f);
        blendHalfGrid(false);
        waitForSecs(0.5f);
        hiliteActiveBoxes(true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",index++,false);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.1f,objectDict.get("box_51").getWorldFrame()),rotationForNum(51),1f,true);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",index++,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.1f,objectDict.get("box_60").getWorldFrame()),rotationForNum(60),1.5f,true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();

    }

    public void demo3e() throws Exception
    {
        hiliteActiveBoxes(true);
        startScene();
    }

    public void demo3f() throws Exception
    {
        hiliteActiveBoxes(true);
        startScene();
    }

    public void demo3g() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_95").getWorldFrame()),rotationForNum(95),0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("box_82").getWorldFrame()),rotationForNum(82),0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3g() throws Exception
    {
        waitForSecs(0.4f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(objectDict.get("box_2").getWorldPosition(),-40,1f,true);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("FINAL2",0,false);
        movePointerToPoint(objectDict.get("box_100").getWorldPosition(),-5,1.5f,true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        resetGrid(false);
        waitForSecs(0.5f);

    }

    public void demo3h() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_94").getWorldFrame()),rotationForNum(94)+10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.8f,objectDict.get("box_2").getWorldFrame()),rotationForNum(2)+10,0.5f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.5f,objectDict.get("box_2").getWorldFrame()),0.2f,true);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.8f,1.1f,objectDict.get("box_92").getWorldFrame()),thePointer),pointerHiliteAnim(2)),
                1.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_92").getWorldFrame()),rotationForNum(92)+10,0.5f,"DEMO",2,0f);
        markLastNumsInColumn(2);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        startScene();
    }

    public void demo3j() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("box_45").getWorldFrame()),rotationForNum(45)+10,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.5f,objectDict.get("box_1").getWorldFrame()),0.5f,true);
        playAudioScene("DEMO",1,false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.8f,1.1f,objectDict.get("box_91").getWorldFrame()),thePointer),pointerHiliteAnim(1)),
                1.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitAudio();
        playAudioScene("DEMO",2,false);
        movePointerToPoint(OB_Maths.locationForRect(0.85f,0.5f,objectDict.get("box_1").getWorldFrame()),1.5f,true);
        waitAudio();
        PointF point = OBMisc.copyPoint(objectDict.get("num_1").position());
        movePointerToPoint(point ,0.2f,true);
        point.y = objectDict.get("box_91").getWorldFrame().bottom;
        playAudioScene("DEMO",3,false);
        movePointerToPoint(point,1.5f,true);
        waitAudio();
        markLastNumsInColumn(1);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        startScene();

    }

    public void demoFin3j() throws Exception
    {
        playAudioScene("DEMO2",0,true);
        hiliteColumns(Arrays.asList(2,4,6,8,10),evencolour);
        waitForSecs(1.5f);
        playAudioScene("DEMO2",1,true);
        hiliteColumns(Arrays.asList(1,3,5,7,9),oddcolour);
        waitForSecs(1.5f);
        playAudioScene("DEMO2",2,true);
        waitForSecs(2.5f);
        resetGrid(false);
        waitForSecs(0.5f);
    }

}
