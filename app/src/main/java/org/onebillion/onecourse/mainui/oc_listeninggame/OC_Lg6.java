package org.onebillion.onecourse.mainui.oc_listeninggame;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 07/06/2018.
 */

public class OC_Lg6 extends OC_Lg
{
    static int MODE_ADD = 1, MODE_SUBTRACT = 2, MODE_MIXED = 3;
    int maxNumberLength;
    int currentMode;
    int minVal, maxVal;
    int eqColour, numColour;
    OBGroup currentEquation;

    public String sectionAudioName()
    {
        return "lg6";
    }

    public void replayAudio()
    {
        try
        {
            if(status() != STATUS_BUSY)
            {
                playAudio(null);
                playAudioQueued(_replayAudio);
            }
        }
        catch (Exception e) {
        }
    }

    public void prepare()
    {
        super.prepare();
        maxNumberLength = 0;
        currentMode = MODE_MIXED;
        numColour = objectDict.get("eq_box").fillColor();
        eqColour = OBUtils.colorFromRGBString(eventAttributes.get("bg_colour"));
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
        if(parameters.containsKey("trials"))
            setMaxTrials(OBUtils.getIntValue(parameters.get("trials")));
        String[] arr = parameters.get("range").split(",");
        int rangeMinVal = minVal = OBUtils.getIntValue(arr[0]);
        int rangeMaxVal = maxVal = OBUtils.getIntValue(arr[1]);
        if(currentMode == MODE_ADD)        rangeMinVal++;
        if(currentMode == MODE_SUBTRACT)        rangeMaxVal--;
        List<OBPhoneme> numberList = phonemesForRangeFrom(rangeMinVal,rangeMaxVal);
        addToDistrators(numberList);
        OBPath eqBox =(OBPath) objectDict.get("eq_box");
        eqBox.sizeToBoundingBoxIncludingStroke();
        int size = OBUtils.getIntValue(parameters.get("size"));
        setupEventForEquations(numberList,size);
        finalisePrepare();
    }

    public void startScene() throws Exception
    {
        List<String> promptAudio = audioForCategory("PROMPT");
        List<String> repeatAudio = audioForCategory("PROMPT.REPEAT");
        setReplayAudio(OBUtils.insertAudioInterval(repeatAudio, 300));
        playAudioQueued(OBUtils.insertAudioInterval(promptAudio, 300));
        long time = setStatus(STATUS_AWAITING_CLICK);
        flashEquationTime(time,7);
    }

    public void flashEquationTime(final long time, float delay) throws Exception {
        final OBGroup equation = currentEquation;
        final OC_SectionController controller = this;
        OBUtils.runOnOtherThreadDelayed(delay, new OBUtils.RunLambda() {
            public void run() throws Exception {
                try {
                    while (!statusChanged(time) && equation == currentEquation) {
                        for (int i = 0; i < 3 && !statusChanged(time) && equation == currentEquation; i++) {
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

                } catch (Exception exception) {

                } finally {
                    if (equation == currentEquation)
                        OC_Numberlines_Additions.colourEquation(equation, 1, 4, eqColour, controller);
                }
            }

        });
    }

    public List<String> audioForCategory(String cat)
    {
        String eventName = "e";
        if(currentEvent == 0)
            eventName = "c";
        else if(currentEvent == 0)
            eventName = "d";
        List<String> audio = getAudioForScene(eventName,cat);
        if(audio != null)
            return audio;
        else
            return null;
    }

    public void setEvent(int eventNum)
    {
        if(currentEquation != null)
            detachControl(currentEquation);
        super.setEvent(eventNum);
        int result = OBUtils.getIntValue(targetPhoneme.text);
        boolean addMode = true;
        if(currentMode == MODE_SUBTRACT)
        {
            addMode = false;
        }
        else if(currentMode == MODE_MIXED)
        {
            addMode = eventNum % 2 == 0;
            if((addMode && result == minVal) ||(!addMode && result == maxVal))
                addMode = !addMode;
        }
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
        OC_Numberlines_Additions.loadEquation(String.format("%d %s %d = %d",num1,sign,num2,result) ,"equation",eqBox,eqColour,false,this);
        currentEquation =(OBGroup) objectDict.get("equation");
        currentEquation.setZPosition(5);
        if(currentEquation.width() > 0.85*eqBox.width())
            currentEquation.setScale(0.85f * eqBox.width() / currentEquation.width());
        OC_Numberlines_Additions.hideEquation(currentEquation,this);
    }

    public void setupEventForEquations(List<OBPhoneme> numberList,int size)
    {
        List<Map<String,Object>> eventList = new ArrayList<>();
        numberList = OBUtils.randomlySortedArray(numberList);
        for(int i=0; i<numberList.size(); i++)
        {
            eventList.add(eventDataSize(size,numberList.get(i)));
        }
        addToEventData(eventList);
    }

    public List<OBPhoneme> phonemesForRangeFrom(int from,int to)
    {
        List<OBPhoneme> arr = new ArrayList<>();
        for(int i=from; i<=to; i++)
        {
            String number = String.format("%d",i);
            if(number.length() > maxNumberLength)
                maxNumberLength =  number.length();
            OBPhoneme pho = new OBPhoneme(number,number);
            arr.add(pho);
        }
        return arr;
    }

    public OBControl loadTargetForPhoneme(OBPhoneme phon, OBControl bg, int type, Map<String,Object> data)
    {
        OBLabel label =(OBLabel)super.loadTargetForPhoneme(phon,bg,type,data);
        label.setColour(numColour);
        return label;
    }

    public float fontSizeForType(int type)
    {
        return applyGraphicScale(60+Math.abs(80-(maxNumberLength*20)));
    }

    public boolean repeatFailedQuestion()
    {
        return false;
    }

    public void correctTarget() throws Exception
    {
        gotItRightBigTick(false);
        waitSFX();
        waitForSecs(0.3f);
        OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,5,"answerin",this);
        waitForSecs(0.5f);
    }

    public void animateShutters(boolean on) throws Exception
    {
        if(on)
        {
            OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,4,"eqnon",this);
            waitForSecs(0.3f);
        }
        super.animateShutters(on);
        if(!on)
        {
            waitForSecs(0.3f);
            currentEquation.hide();
            playSfxAudio("eqnoff",true);
            waitForSecs(0.3f);
        }
    }

    public void demo() throws Exception
    {
        demoPresenter();
        demoPointer();
        startScene();
    }

    public void demoPresenter() throws Exception
    {
        if(OBUtils.getBooleanValue(parameters.get("presenter")))
        {
            PointF presenterLoc = (PointF)presenter.control.settings.get("startloc");
            presenter.walk(presenterLoc);
            presenter.faceFront();
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)getAudioForScene("a","DEMO"), this);
            waitForSecs(0.3f);
            presenterLoc.x = 0.85f*this.bounds().width();
            presenter.walk(presenterLoc);
            presenter.faceFront();
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)getAudioForScene("a","DEMO2"),this);
            waitForSecs(0.3f);
            presenterLoc.x = 1.2f*this.bounds().width();
            presenter.walk(presenterLoc);
        }
    }

    public void demoPointer() throws Exception
    {
        animateShutters(true);
        List<String> audio = getAudioForScene("b","DEMO");
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,objectDict.get("eq_box") .frame()),0.5f,true);
        playAudio(audio.get(0));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.3f,0.9f,objectDict.get("eq_box") .frame()),0.5f,true);
        playAudio(audio.get(1));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("top_2_4") .getWorldFrame()),0.5f,true);
        playAudio(audio.get(2));
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
    }


}
