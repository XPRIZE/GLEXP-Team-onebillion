package com.maq.xprize.onecourse.mainui.oc_equationpic;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBImage;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 14/06/2018.
 */

public class OC_EquationWithPic extends OC_SectionController
{
    static int MODE_ADD = 1, MODE_SUBTRACT = 2, MODE_MIXED = 3;
    List<Map<String,Object>> eventData;
    int currentMode, maxVal, minVal;
    OBPath targetControl;
    OBGroup currentEquation;
    int eqColour;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eqColour = OBUtils.colorFromRGBString(eventAttributes.get("colour_equation"));
        int size = OBUtils.getIntValue(parameters.get("size"));
        loadEvent(String.format("grid_%d",size));
        currentMode = MODE_MIXED;
        if(parameters.get("mode").equals("add"))
        {
            currentMode = MODE_ADD;
        }
        else if(parameters.get("mode").equals("subtract"))
        {
            currentMode = MODE_SUBTRACT;
            mergeAudioScenesForPrefix("SUB");
        }
        else
        {
            mergeAudioScenesForPrefix("MIX");
        }
        String[] arr = parameters.get("range").split(",");
        minVal = OBUtils.getIntValue(arr[0]);
        maxVal = OBUtils.getIntValue(arr[1]);
        loadEventsForSize(size,minVal,maxVal);
        OBPath picFrame = (OBPath)objectDict.get("pic_frame");
        picFrame.setZPosition(0.5f);
        String[] picNums = parameters.get("pic").split(",");

        int picFrom = OBUtils.getIntValue(picNums[0]);
        int picTo = OBUtils.getIntValue(picNums[1]);
        if(picFrom>48)
            picFrom = 1;
        if(picTo>48)
            picTo = 48;

        int picNum = OB_Maths.randomInt(picFrom ,picTo);
        OBImage screenImage = loadImageWithName(String.format("eqpic_%d",picNum),new PointF(0.5f, 0.5f),picFrame.frame());
        if(screenImage == null)
            screenImage = loadImageWithName(String.format("eqpic_%d",OB_Maths.randomInt(1,48)),new PointF(0.5f, 0.5f),picFrame.frame());
        if(picFrame.width()/picFrame.height() < screenImage.width()/screenImage.height())
        {
            screenImage.setScale(picFrame.height()/screenImage.height());
        }
        else
        {
            screenImage.setScale(picFrame.width()/screenImage.width());
        }
        OBGroup imgGroup = new OBGroup(Arrays.asList((OBControl)screenImage));
        imgGroup.setFrame(picFrame.frame);
        imgGroup.setMasksToBounds(true);
        imgGroup.setZPosition(1);
        attachControl(imgGroup);
        picFrame.sizeToBoundingBoxIncludingStroke();
        setSceneXX(currentEvent());
    }


    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                if (OBUtils.getBooleanValue(parameters.get("presenter"))) {
                    demoIntro1();
                } else {
                    demoIntro2();
                }
                demoPointer();
                doMainXX();
            }
        });
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        if(currentEquation != null)
            detachControl(currentEquation);
        Map<String,Object> eventDict = eventData.get(eventIndex);
        targetControl =(OBPath) eventDict.get("obj");
        boolean addMode = (boolean)eventDict.get("add");
        int result = (int)eventDict.get("num");
        int num1, num2;
        if(addMode)
        {
            num1 = OB_Maths.randomInt(minVal, result-1);
            num2 = result - num1;
        }
        else
        {
            num1 = OB_Maths.randomInt(result+1, maxVal);
            num2 = num1 - result;
        }
        String sign = addMode ? "+" : "–";
        OBPath eqBox =(OBPath) objectDict.get("eq_box");
        OC_Numberlines_Additions.loadEquation(String.format("%d %s %d = %d",num1,sign,num2,result) ,
                "equation",eqBox,eqColour,false,this);
        currentEquation =(OBGroup)objectDict.get("equation");
        currentEquation.setZPosition(5);
        OC_Numberlines_Additions.hideEquation(currentEquation,this);

        if (eventIndex == 0)
            OC_Numberlines_Additions.showEquation(currentEquation, 1, 4, this);

    }

    @Override
    public void doMainXX() throws Exception
    {
        if(eventIndex != 0)
        {
            waitForSecs(0.3f);
            OC_Numberlines_Additions.showEquation(currentEquation,1,4,"eqnon",this);

        }
        long time = setStatus(STATUS_AWAITING_CLICK);
        OBMisc.doSceneAudio(8,time,this);
        flashEquationTime(time,8);
    }

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,0,filterControls("obj_.*"),pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        checkTarget((OBPath)targ);
                    }
                });
            }
        }
    }

    public void checkTarget(OBPath targ) throws Exception
    {
        int fillColour = (int)targ.propertyValue("fill_colour");
        targ.setFillColor(OBUtils.highlightedColour(fillColour));
        if(targ == targetControl)
        {
            gotItRightBigTick(false);
            waitSFX();
            waitForSecs(0.3f);

            lockScreen();
            OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,5,null,this);
            OBLabel label = (OBLabel)targetControl.propertyValue("label");
            label.hide();
            playSfxAudio("answerin",false);
            unlockScreen();
            waitSFX();

            waitForSecs(0.3f);
            animateCoverHide(targ);
            playAudioQueuedScene("FINAL",0.3f,true);
            if(eventIndex < events.size()-1)
            {
                currentEquation.hide();
                playSfxAudio("eqnoff",true);
            }
            else
            {
                waitForSecs(0.5f);
                OBControl picFrame = objectDict.get("pic_frame");
                playSfxAudio("fullpic",false);
                for(int i=0; i<3; i++)
                {
                    picFrame.show();
                    waitForSecs(0.2f);
                    picFrame.hide();
                    waitForSecs(0.2f);
                }
                picFrame.show();
                waitForSecs(0.5f);
            }
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.setFillColor(fillColour);
        }
    }

    public void animateCoverHide(final OBPath targ) throws Exception
    {
        targ.setZPosition(10);
        OBAnim anim = new OBAnimBlock() {
            @Override
            public void runAnimBlock(float frac) {
                targ.setYRotation((float)-Math.PI  * frac);
            }
        };
        playSfxAudio("reveal",false);
        OBAnimationGroup.runAnims(Arrays.asList(anim),0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        targ.hide();
    }

    public void loadEventsForSize(int size,int from,int to)
    {
        List<Map<String,Object>> fullEventData = new ArrayList<>();
        List<String> eventList = new ArrayList<>();
        List<Integer> numList = new ArrayList<>();
        numList = OBMisc.integerList(from,to);

        List<Integer> randomNumList = OBUtils.randomlySortedArray(numList);
        int pickSize = currentMode == MODE_MIXED ? (int)(size/2.0) : size;
        if(currentMode == MODE_MIXED || currentMode == MODE_SUBTRACT)
        {
            List<Map<String,Object>> eventArr = new ArrayList<>();
            List<Integer> partList = new ArrayList<>(randomNumList);
            int index = 0;
            while(eventArr.size() < pickSize)
            {
                int num = partList.get(index).intValue();
                if(num != to)
                {
                    Map<String,Object> eventDict = new ArrayMap<>();
                    eventDict.put("add",false);
                    eventDict.put("num",num);
                    eventArr.add(eventDict);
                    randomNumList.remove(partList.get(index));
                }
                index++;
            }
            fullEventData.addAll(eventArr);
        }

        if(currentMode == MODE_MIXED || currentMode == MODE_ADD)
        {
            List<Map<String,Object>> eventArr = new ArrayList<>();
            int index = 0;
            while(eventArr.size() < pickSize)
            {
                int num = randomNumList.get(index).intValue();
                if(num != from)
                {
                    Map<String,Object> eventDict = new ArrayMap<>();
                    eventDict.put("add",true);
                    eventDict.put("num",num);
                    eventArr.add(eventDict);
                }
                index++;
            }
            fullEventData.addAll(eventArr);
        }

        eventData = OBUtils.randomlySortedArray(fullEventData);
        for(int i=0; i<eventData.size(); i++)
        {
            Map<String,Object> eventDict = eventData.get(i);
            OBPath path =(OBPath)objectDict.get(String.format("obj_%d",i+1));
            path.setProperty("fill_colour",path.fillColor());
            path.sizeToBoundingBoxIncludingStroke();
            path.setDoubleSided(false);
            path.m34 = -1/2000f;
            path.setZPosition(2);
            path.setAnchorPoint(new PointF(0, 0.5f));

            int num = (int)eventDict.get("num");
            OBLabel label = new OBLabel(String.format("%d",num) , OBUtils.StandardReadingFontOfSize(size > 10 ? 60 : 80));
            label.setColour(Color.WHITE);
            label.setPosition(path.position());
            attachControl(label);
            label.setZPosition(3);
            label.setPosition(OBUtils.centroidForPath(String.format("grid_%d",size), (String)path.attributes().get("id"), this));

            path.setProperty("label",label);

            eventDict.put("obj",path);
            String indexString = String.format("%d", i+1);
            if(audioScenes.containsKey(indexString))
            {
                eventList.add(indexString);
            }
            else
            {
                eventList.add("default");
            }
        }
        eventData = OBUtils.randomlySortedArray(eventData);
        events = eventList;
    }

    public void flashEquationTime(final long time, final float delay) throws Exception
    {
        final OBGroup equation = currentEquation;
        final OC_SectionController controller = this;
        OBUtils.runOnOtherThreadDelayed(delay,new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                try {
                    while (!statusChanged(time) && equation == currentEquation && !controller._aborting)
                    {
                        for (int i = 0; i < 3 && !statusChanged(time) && equation == currentEquation && !controller._aborting; i++)
                        {
                            OC_Numberlines_Additions.colourEquation(equation, 1, 4, Color.RED, controller);
                            if (statusChanged(time))
                                break;
                            waitForSecs(0.4f);
                            OC_Numberlines_Additions.colourEquation(equation, 1, 4, eqColour, controller);
                            if (statusChanged(time))
                                break;
                            waitForSecs(0.4f);
                        }
                        if (equation == currentEquation)
                            OC_Numberlines_Additions.colourEquation(equation, 1, 4, eqColour, controller);
                        waitForSecs(5f);
                    }
                }
                catch (Exception exception)
                {
                }
                finally
                {
                    if (equation == currentEquation)
                        OC_Numberlines_Additions.colourEquation(equation, 1, 4, eqColour, controller);
                }
            }
        });
    }

    public void  demoIntro1() throws Exception
    {
        OBMisc.standardDemoIntro1(this);
    }

    public void demoIntro2() throws Exception
    {
        OBMisc.standardDemoIntro2(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),
                OB_Maths.locationForRect(1.05f,1.05f,objectDict.get("pic_frame") .frame()),this);
    }

    public void demoPointer() throws Exception
    {
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,objectDict.get("eq_box") .frame()),-30,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0f,0f,objectDict.get("pic_frame") .frame()),-40,0.5f,true);
        playAudioScene("DEMO",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get("pic_frame") .frame()),-20,1f,true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
    }


}
