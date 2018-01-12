package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/04/2017.
 */

public class OC_MoreNumbers_S4s extends OC_SectionController
{
    int targetNum;
    OBLabel counter, underline1,underline2;
    OBControl lastTarget;
    String currentNumString;
    List<OBControl> clickNums;
    List<OBControl> eventObjs;
    RectF startRect;
    boolean fingerDown;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master4s");
        clickNums = new ArrayList<>();
        eventObjs = new ArrayList<>();
        OBControl numbox = objectDict.get("numbox");
        float fontSize = 110*numbox.height()/130.0f;
        counter = new OBLabel("––", OBUtils.standardTypeFace(),fontSize);
        counter.setPosition(numbox.position());
        ((OBTextLayer)counter.layer).justification = OBTextLayer.JUST_LEFT;
        startRect = new RectF(counter.getWorldFrame());
        counter.setColour(Color.BLACK);
        attachControl(counter);
        counter.setString ( "");
        OBLabel underline = new OBLabel("––",OBUtils.standardTypeFace(),fontSize);
        underline.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 0.8f), startRect));
        ((OBTextLayer)underline.layer).justification = OBTextLayer.JUST_LEFT;
        RectF bb = OBUtils.getBoundsForSelectionInLabel(0,1,underline);
        underline1 = new OBLabel("–",OBUtils.standardTypeFace(),fontSize);
        underline1.setPosition(underline.position());
        underline1.setLeft(bb.left);
        underline1.hide();
        attachControl(underline1);

        counter.hide();

        bb = OBUtils.getBoundsForSelectionInLabel(1,2,underline);
        underline2 = new OBLabel("–",OBUtils.standardTypeFace(),fontSize);
        underline2.setPosition(underline.position());
        underline2.setLeft(bb.left);
        underline2.hide();
        attachControl(underline2);

        counter.hide();
        underline.hide();

        for(int i=0; i<10; i++)
        {
            String boxname = String.format("box_%d",i);
            OBControl box = objectDict.get(boxname);
            ((OBPath)box).sizeToBoundingBoxIncludingStroke();
            box.show();
            float fontSize2 = 75.0f*box.height()/78.0f;
            OBLabel label = new OBLabel(String.format("%d",i),OBUtils.standardTypeFace(),fontSize2);
            label.setColour(Color.BLACK);
            box.setZPosition(1);
            OBGroup labelGroup = new OBGroup(Arrays.asList((OBControl)label));
            labelGroup.sizeToTightBoundingBox();
            labelGroup.setZPosition(2);
            labelGroup.setPosition(box.position());
            OBGroup group = new OBGroup(Arrays.asList(box, labelGroup));
            attachControl(group);
            group.objectDict.put("label",label);
            group.setProperty("num_value",i);
            clickNums.add(group);
            group.hide();

        }
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        fingerDown = false;
        OC_MoreNumbers_Additions.buttonSet(2,this);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo4s();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        targetNum = OBUtils.getIntValue(eventAttributes.get("num"));
        OBGroup obj = (OBGroup)objectDict.get("obj");
        OBMisc.colourObjectFromAttributes(obj);
        obj.show();
        OBControl objimg = obj.renderedImageControl();
        objimg.setRotation ( obj.rotation);
        obj.hide();
        int rows = (int)Math.ceil(targetNum/10.0);
        float startY = -rows/2.0f + 1.0f;
        for(int i=0; i< targetNum; i++)
        {
            OBControl objcopy = objimg.copy();
            objcopy.setPosition (OB_Maths.locationForRect((i%10)/9.0f,startY + (float)Math.floor(i/10.0),objectDict.get("workrect").frame()));
            attachControl(objcopy);
            objcopy.hide();
            eventObjs.add(objcopy);
        }
    }

    public void doMainXX() throws Exception
    {
        showObjs();
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl numBox = finger(-1,-1,clickNums,pt);
            if(numBox != null && (currentNumString == null ||currentNumString.length() <= 1))
            {
                fingerDown = true;
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkNumBox(numBox);
                    }
                });

            }
            else if(objectDict.get("numbox").frame().contains(pt.x,pt.y) && currentNumString != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        typeNumber(null,null);
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                });
            }
            else if(finger(0,2,Arrays.asList(objectDict.get("button_arrow")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkButton();
                    }
                });

            }

        }



    }
    public void touchUpAtPoint(PointF pt, View v)
    {
        if(lastTarget != null)
        {
            setLabelInGroup((OBGroup)lastTarget,Color.BLACK);
            lastTarget = null;
        }
        fingerDown = false;
    }


    public void checkNumBox(OBControl numBox) throws Exception
    {
        int val = (int)numBox.propertyValue("num_value");
        typeNumber(String.format("%d",val), numBox);
        lastTarget = numBox;
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitSFX();
        if(numBox != null && (!fingerDown || statusTime != time))
        {
            setLabelInGroup((OBGroup)numBox,Color.BLACK);
            lastTarget = null;
        }
    }

    public void checkButton() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(1,this);
        if(currentNumString != null && Integer.valueOf(currentNumString) == targetNum)
        {
            playAudio(null);
            gotItRightBigTick(true);
            waitForSecs(0.2f);
            OC_MoreNumbers_Additions.buttonSet(2,this);
            demoNum();
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            if(currentNumString != null)
            {
                typeNumber(null,null);
                waitForSecs(0.2f);

            }
            playAudioQueuedScene("INCORRECT",300,false);
            OC_MoreNumbers_Additions.buttonSet(0,this);
            setStatus(STATUS_AWAITING_CLICK);

        }
    }

    public void startScene() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(0,this);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void typeNumber(String numString, OBControl numBox) throws Exception
    {
        if(currentNumString == null || numString == null)
        {
            currentNumString = numString;
        }
        else
        {
            currentNumString = String.format("%s%s",currentNumString,numString);
        }
        lockScreen();
        if(currentNumString == null)
        {
            underline2.show();
            underline1.show();
            counter.setString("");
            counter.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 0.5f), startRect));
        }
        else
        {
            if(currentNumString.length() > 1)
            {
                underline2.hide();
                counter.setString(currentNumString);
            }
            else
            {
                underline1.hide();
                counter.setString(currentNumString);
                if(currentNumString.equals("1"))
                    counter.setPosition(OB_Maths.locationForRect(new PointF(0.75f, 0.5f), startRect));

            }

        }
        if(numBox != null)
            setLabelInGroup((OBGroup)numBox, Color.RED);

        playSfxAudio(currentNumString == null ? "pop" : "type",false);
        unlockScreen();
    }

    public void removeObjsAndNum() throws Exception
    {
        lockScreen();
        underline1.show();
        underline2.show();
        counter.setString ( "");
        counter.setPosition ( OB_Maths.locationForRect(new PointF(0.5f, 0.5f), startRect));
        currentNumString = null;
        for(OBControl group : eventObjs)
            detachControl(group);
        playSfxAudio("pop",false);
        unlockScreen();
        waitSFX();
    }

    public void setLabelInGroup(OBGroup group,int colour)
    {
        OBLabel label = (OBLabel)group.objectDict.get("label");
        label.setColour(colour);
    }

    public void showObjs() throws Exception
    {
        lockScreen();
        for(OBControl obj : eventObjs)
            obj.show();
        playSfxAudio("pop_on",false);
        unlockScreen();
        waitSFX();
    }

    public void markCounterNum(int num)
    {
        counter.setHighRange(num, num+1, Color.RED);
    }

    public void colourEntireCounter(int colour)
    {
        counter.setHighRange(-1,-1,Color.BLACK);
        counter.setColour(colour);
    }

    public void demoNum() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        for(int i=0; i<2; i++)
        {
            markCounterNum(i);
            playAudioScene("FINAL",i+1,true);
            lockScreen();
            colourEntireCounter(Color.BLACK);
            unlockScreen();
            waitForSecs(i==0 ? 0.3 : 0.8);
        }
        if(currentEvent() != events.get(events.size()-1))
        {
            removeObjsAndNum();
            waitForSecs(0.5f);
        }
    }

    public void pointerTypeNum(int num) throws Exception
    {
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,clickNums.get(num).frame()),-30,0.5f,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,clickNums.get(num).frame()),-30,0.15f,true);
        typeNumber(String.format("%d",num),clickNums.get(num));
        waitSFX();
        setLabelInGroup((OBGroup)clickNums.get(num), Color.BLACK);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,clickNums.get(num).frame()),-30,0.15f,true);
    }

    public void demo4s() throws Exception
    {
        waitForSecs(0.5f);
        showObjs();
        waitForSecs(0.3f);
        lockScreen();
        for(OBControl obj : clickNums)
            obj.show();
        playSfxAudio("pop_on",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        lockScreen();
        counter.show();
        underline1.show();
        underline2.show();
        unlockScreen();
        playSfxAudio("pop_on",false);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.95f,0.8f,this.bounds()),-15,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.2f,eventObjs.get(14).frame()),-35,0.5f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.2f,eventObjs.get(0).frame()),-40,0.5f,true);
        playAudioScene("DEMO",2,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.2f,eventObjs.get(9).frame()),-10,1.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        pointerTypeNum(1);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.2f,eventObjs.get(10).frame()),-40,0.5f,true);
        playAudioScene("DEMO",4,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.2f,eventObjs.get(14).frame()),-20,1.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        pointerTypeNum(5);
        playAudioScene("DEMO",5,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1f,objectDict.get("numbox").frame()),-15,0.5f,"DEMO",6,0.3f);
        for(int i=0; i<2; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect((i==0 ? 0.4f : 0.7f),0.95f,objectDict.get("numbox").frame()),(i==0 ? -20 : -15),0.2f,true);
            markCounterNum(i);
            playAudioScene("DEMO",i + 7,true);
            colourEntireCounter(Color.BLACK);
            waitForSecs(0.5f);
        }
        thePointer.hide();
        waitForSecs(0.5f);
        removeObjsAndNum();
        waitForSecs(0.5f);
        nextScene();

    }

    public void demo4t() throws Exception
    {
        showObjs();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.95f,0.8f,this.bounds()),-15,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(4f,1.2f,eventObjs.get(22).frame()),-25,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.98f,this.bounds()),-30,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,underline2.frame()),-25,0.5f,"DEMO",3,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("button_arrow").frame()),-10,0.5f,"DEMO",4,0.5f);
        thePointer.hide();
        startScene();

    }



}
