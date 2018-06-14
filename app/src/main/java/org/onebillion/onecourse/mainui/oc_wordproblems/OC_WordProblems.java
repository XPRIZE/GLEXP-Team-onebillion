package org.onebillion.onecourse.mainui.oc_wordproblems;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
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
 * Created by michal on 14/06/2018.
 */

public class OC_WordProblems extends OC_SectionController
{
    static String MODE_ADD = "add", MODE_SUBTRACT = "sub", MODE_DIVIDE = "div",
        MODE_MULTIPLY = "mul", MODE_REVERSE_SUBTRACT = "rsub", MODE_REVERSE_DIVIDE = "rdiv";

    List<String> audioSceneList;
    List<List<Integer>> questionParams;
    List<OBGroup> currentButtons;
    Map<String,Integer> eventColours;
    int currentQuestionType, currentQuestionNum;
    int answerCount;
    int eqPart1, eqPart2, eqPart3;
    String currentMode;
    OBGroup currentEquation, correctButton;
    List<String> questionAudio, feedbackAudio;
    int numColour;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        eventsDict = loadXML(getLocalPath(String.format("%s.xml",sectionName())));
        loadFingers();
        loadEvent("master");
        OBPath path = (OBPath) objectDict.get("button");
        path.sizeToBoundingBoxInset(-path.lineWidth() -path.getShadowOffsetY());
        ((OBPath)objectDict.get("counter")).sizeToBoundingBoxIncludingStroke();
        ((OBPath)objectDict.get("line")).sizeToBoundingBoxIncludingStroke();
        ((OBPath)objectDict.get("eq_box")).sizeToBoundingBoxIncludingStroke();
        eventColours = OBMisc.loadEventColours(this);
        answerCount = 4;
        audioSceneList = new ArrayList<>();
        questionParams = new ArrayList<>();
        events = new ArrayList<>();
        numColour = OBUtils.colorFromRGBString("85,85,85");
        List<String> eventList = new ArrayList<>();
        String[] questionGroups = parameters.get("questions").split(";");
        for(int i=0; i<questionGroups.length; i++)
        {
            String questionParam = questionGroups[i];
            String[] arr = questionParam.split(",");
            List<Integer> values = new ArrayList<>();
            for(String str : arr)
                values.add(OBUtils.getIntValue(str));

            if(values.size() > 1)
            {
                questionParams.add(values);
                String questionScene = String.format("%d_%d", values.get(0).intValue() , values.get(1).intValue());
                eventList.add(questionScene);
                String eventName = String.format("%d", audioSceneList.size()+1);
                if(i == questionGroups.length-1)
                    eventName = "last";
                else if(audioScenes.get(eventName) == null)
                    eventName = "default";
                audioSceneList.add(eventName);
            }
        }
        events = eventList;
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                if (OBUtils.getBooleanValue(parameters.get("presenter")))
                {
                    demoIntro1();
                }
                else
                {
                    demoIntro2();
                }
                doMainXX();
            }
        });
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        List<Integer> values = questionParams.get(eventIndex);
        currentQuestionType = values.get(0);
        currentQuestionNum = values.get(1);
        currentMode = eventAttributes.get("mode");
        List<String> eq1Arr = Arrays.asList(eventAttributes.get("str").split(","));
        List<String> eq2Arr = Arrays.asList(eventAttributes.get(currentMode).split(","));
        eq1Arr = OBUtils.randomlySortedArray(eq1Arr);
        eq2Arr = OBUtils.randomlySortedArray(eq2Arr);
        eqPart1 = OBUtils.getIntValue(eq1Arr.get(0));
        eqPart2 = OBUtils.getIntValue(eq2Arr.get(0));
        int type = OBUtils.getIntValue(eventAttributes.get("type"));
        String equationString = "";
        if(currentMode.equals(MODE_ADD))
        {
            eqPart3 = eqPart1 + eqPart2;
            equationString = String.format("%d + %d = %d",eqPart1,eqPart2,eqPart3);
        }
        else if(currentMode.equals(MODE_SUBTRACT))
        {
            eqPart3 = eqPart1 - eqPart2;
            if(type == 2)
                equationString = String.format("%d – %d = %d",eqPart1,eqPart2,eqPart3);
            else
                equationString = String.format("%d + %d = %d",eqPart2,eqPart3,eqPart1);

        }
        else if(currentMode.equals(MODE_REVERSE_SUBTRACT))
        {
            eqPart3 = eqPart2 - eqPart1;
            equationString = String.format("%d + %d = %d",eqPart3,eqPart1,eqPart2);
        }
        else if(currentMode.equals(MODE_MULTIPLY))
        {
            eqPart3 = eqPart1 * eqPart2;
            equationString = String.format("%d",eqPart2);
            for(int i=1; i<eqPart1; i++)
            {
                equationString += String.format(" + %d",eqPart2);
            }
            equationString += String.format(" = %d",eqPart3);
        }
        else if(currentMode.equals(MODE_DIVIDE))
        {
            eqPart3 = eqPart1/eqPart2;
            equationString = String.format("%d",eqPart3);
            for(int i=1; i<eqPart2; i++)
            {
                equationString += String.format(" + %d",eqPart3);
            }
            equationString += String.format(" = %d",eqPart1);
        }
        else  if(currentMode.equals(MODE_REVERSE_DIVIDE))
        {
            eqPart3 = eqPart2/eqPart1;
            equationString = String.format("%d",eqPart3);
            for(int i=1; i<eqPart1; i++)
            {
                equationString += String.format(" + %d",eqPart3);
            }
            equationString += String.format(" = %d",eqPart2);
        }
        String part1Audio = String.format("wpt%d_q%d_str_%d", currentQuestionType, currentQuestionNum, eqPart1);
        String part2Audio = String.format("wpt%d_q%d_%s_%d", currentQuestionType,currentQuestionNum,currentMode, eqPart2);
        String endAudio = String.format("wpt%d_q%d_end", currentQuestionType,currentQuestionNum);
        String answerAudio = String.format("wpt%d_q%d_ans_%d", currentQuestionType,currentQuestionNum, eqPart3);
        questionAudio  = Arrays.asList(part1Audio,part2Audio,endAudio);
        feedbackAudio  = Arrays.asList(part1Audio,part2Audio,answerAudio);
        List<Integer> choices = new ArrayList<>();
        for(int i=eqPart3-4; i<=eqPart3+4; i++)
        {
            if(i!=eqPart3 && i>0)
                choices.add(i);
        }
        currentButtons = new ArrayList<>();
        List<Integer> randChoices = OBUtils.randomlySortedArray(choices);
        List<Integer> screenChoices = new ArrayList<>();
        screenChoices.addAll(randChoices.subList(0, answerCount-1));
        screenChoices.add(eqPart3);
        List<Integer> randAnswers = OBUtils.randomlySortedArray(screenChoices);
        OBControl button = objectDict.get("button");
        for(int i=0; i<randAnswers.size(); i++)
        {
            int num = randAnswers.get(i);
            OBControl buttonCopy = button.copy();
            OBLabel numLabel = new OBLabel(String.format("%d",num), OBUtils.StandardReadingFontOfSize(70));
            numLabel.setColour(Color.BLACK);
            numLabel.setPosition(buttonCopy.position());
            numLabel.show();
            numLabel.setZPosition(2);
            buttonCopy.show();
            buttonCopy.setZPosition(1);
            OBGroup group = new OBGroup(Arrays.asList(numLabel, buttonCopy));
            group.objectDict.put("label",numLabel);
            group.objectDict.put("bg",buttonCopy);
            group.hide();
            attachControl(group);
            currentButtons.add(group);
            group.setPosition (OB_Maths.locationForRect((1.0f/(randAnswers.size()+1))*(i+1),0.5f,objectDict.get("ans_box") .frame()));
            if(num == eqPart3)
                correctButton = group;
        }
        OBPath equationBg = (OBPath)objectDict.get("eq_box").copy();
        attachControl(equationBg);
        OC_Numberlines_Additions.loadEquation(equationString,"equation",equationBg,numColour,false,this);
        OBGroup equation = (OBGroup)objectDict.get("equation");
        equation.setZPosition(2);
        equationBg.setWidth(equation.width() + applyGraphicScale(80));
        equationBg.setZPosition(1);
        equationBg.show();

        currentEquation = new OBGroup(Arrays.asList(equationBg,equation));
        currentEquation.settings.putAll(equation.settings);
        currentEquation.objectDict.putAll(equation.objectDict);
        currentEquation.objectDict.put("bg", equationBg);
        attachControl(currentEquation);
        currentEquation.hide();
    }

    @Override
    public void doMainXX() throws Exception
    {
        lockScreen();
        showControls("obj_.*");
        playSfxAudio("image",false);
        unlockScreen();
        waitSFX();

        waitForSecs(0.3f);
        List<String> audios = getAudioForCategory("PROMPT");
        if(audios != null)
        {
            playAudio(audios.get(eventIndex%audios.size()));
            waitAudio();
            waitForSecs(0.6f);
        }
        performSel("demoEvent",String.format("%d",eventIndex+1));
        waitForSecs(0.5f);
        List<Object> audio = OBUtils.insertAudioInterval(questionAudio, 1000);
        setReplayAudio(audio);
        playAudioQueued(audio,true);
        waitForSecs(1f);

        lockScreen();
        for(OBControl button : currentButtons)
            button.show();
        playSfxAudio("options",false);
        unlockScreen();
        waitSFX();

        waitForSecs(0.3f);
        performSel("demo2Event",String.format("%d",eventIndex+1));
        setStatus(STATUS_AWAITING_CLICK);
    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,(List<OBControl>)(Object)currentButtons,pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        checkTarget((OBGroup)targ);
                    }
                });
            }
        }
    }

    public void checkTarget(OBGroup targ) throws Exception
    {
        if(targ == correctButton)
        {
            highlightButton(targ,eventColours.get("correct"));
            gotItRightBigTick(true);
            waitForSecs(0.5f);
            lockScreen();
            for(OBControl con : currentButtons)
                con.hide();
            unlockScreen();
            waitForSecs(0.3f);
            playAudio(feedbackAudio.get(2));
            waitAudio();
            waitForSecs(1f);
            equationShow();
            waitForSecs(2.5f);
            clearScene(null);
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            highlightButton(targ,eventColours.get("incorrect"));
            gotItWrongWithSfx();
            waitSFX();
            waitForSecs(0.5f);
            lowlightWrongButtons();
            playCurrentAudio("INCORRECT",0,false);
            flashCorrectButton();
            waitAudio();
            waitForSecs(0.5f);
            playCurrentAudio("INCORRECT",1,true);
            waitForSecs(0.5f);
            performSel("demoFeedback",String.format("%d",currentQuestionType));
            nextScene();
        }
    }

    public void equationShow() throws Exception
    {
        currentEquation.show();
        playSfxAudio("eqnon",true);
    }

    public void flashCorrectButton() throws Exception
    {
        OBControl bg = correctButton.objectDict.get("bg");
        int colour = bg.fillColor();
        for(int i=0; i<2; i++)
        {
            bg.setFillColor(eventColours.get("flash"));
            waitForSecs(0.3f);
            bg.setFillColor(colour);
            waitForSecs(0.3f);
        }
        bg.setFillColor(eventColours.get("flash"));
    }

    public void lowlightWrongButtons() throws Exception
    {
        lockScreen();
        OBPath button =(OBPath)objectDict.get("button");
        for(OBGroup con : currentButtons)
        {
            if(con == correctButton)
                continue;
            OBPath bg =(OBPath)con.objectDict.get("bg");
            bg.setFillColor(button.fillColor());
            con.setOpacity(0.3f);
        }
        unlockScreen();
    }

    public void playCurrentAudio(String cat,int index,boolean wait) throws Exception
    {
        List<String> audios = getAudioForCategory(cat);
        if(audios != null && audios.size()>index)
        {
            playAudio(audios.get(index));
            if(wait)
            {
                waitAudio();
                waitForSecs(0.3f);}
        }
    }

    public List<String> getAudioForCategory(String cat)
    {
        if(audioScenes.get(audioSceneList.get(eventIndex)) != null)
        {
            return getAudioForScene(audioSceneList.get(eventIndex) ,cat);
        }
        return null;
    }

    public void clearScene(List<OBControl> extra) throws Exception
    {
        if(eventIndex == events.size()-1)
            return;
        lockScreen();
        for(OBControl con : currentButtons)
            detachControl(con);
        deleteControls("obj_.*");
        if(extra != null)
        {
            for(OBControl con : extra)
                detachControl(con);

        }
        detachControl(currentEquation);
        playSfxAudio("alloff",false);
        unlockScreen();
        waitSFX();
    }

    public void hideScreenControls(boolean withObj) throws Exception
    {
        lockScreen();
        if(withObj)
            hideControls("obj_.*");
        for(OBControl con : currentButtons)
            con.hide();
        unlockScreen();
    }

    public List<OBControl> loadRowOfCounters(int count,float y,float offset,float dist)
    {
        List<OBControl> counters = new ArrayList<>();
        OBControl con = objectDict.get("counter");
        float distance = con.width() * dist;
        OBControl feedBox = objectDict.get("feed_box");
        PointF loc = OB_Maths.locationForRect(0.5f,y,feedBox.frame());
        int size = 10;
        if(count > size)
            size = count;

        float leftStart = 0;
        if(offset >= 0)
        {
            leftStart = offset*feedBox.width() + loc.x - (size/2.0f)*con.width() - ((size/2.0f)-0.5f)*distance;
        }
        else
        {
            leftStart = loc.x - (count/2.0f)  *con.width() -((count/2.0f) -0.5f) *distance;
        }
        for(int i=0; i<count; i++)
        {
            OBControl conCopy = con.copy();
            attachControl(conCopy);
            conCopy.setPosition(loc);
            conCopy.setZPosition(1);
            conCopy.setLeft (leftStart +(i*(con.width()+distance)));
            counters.add(conCopy);
        }
        return counters;
    }

    public List<OBControl> loadRowOfCounters(int count,float y) throws Exception
    {
        return loadRowOfCounters(count,y,0,0.2f);
    }

    public void flashCounters(List<OBControl> counters,int count) throws Exception
    {
        for(int i=0; i<count; i++)
        {
            lockScreen();
            for(OBControl cont : counters)
                cont.setOpacity(0.2f);
            unlockScreen();

            waitForSecs(0.3f);

            lockScreen();
            for(OBControl cont : counters)
                cont.setOpacity(1);
            unlockScreen();

            waitForSecs(0.3f);

        }
        waitForAudio();
    }

    public void highlightButton(OBGroup con,int colour)
    {
        OBPath bg =(OBPath)con.objectDict.get("bg");
        bg.setFillColor(colour);
    }


    public void demoEvent1() throws Exception
    {
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-20,0.5f,true);
        playCurrentAudio("DEMO",0,true);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f,1.1f) , MainViewController() .topRightButton.frame) ,0,0.5f,true);
        playCurrentAudio("DEMO",1,true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.5f);
    }

    public void demo2Event1() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.1f,currentButtons.get(0).frame()),0.5f,true);
        playCurrentAudio("DEMO2",0,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.1f,currentButtons.get(currentButtons.size()-1).frame()),2,true);
        waitForAudio();
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoFeedback1() throws Exception
    {
        hideScreenControls(true);
        waitForSecs(0.3f);
        List<OBControl> counters1 = loadRowOfCounters(eqPart1,0.3f);
        List<OBControl> counters2 = loadRowOfCounters(eqPart2,0.55f);
        OC_Numberlines_Additions.hideEquation(currentEquation,this);
        OC_Numberlines_Additions.showEquation(currentEquation,1,1,null,this);
        playAudio(feedbackAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : counters1)
        {
            con.show();
            playSfxAudio("counter_1",false);
            waitForSecs(0.3f);
        }
        waitForSecs(1f);
        currentEquation.show();
        playSfxAudio("numbon",true);
        waitForSecs(1f);
        playAudio(feedbackAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : counters2)
        {
            con.show();
            playSfxAudio("counter_2",false);
            waitForSecs(0.3f);
        }
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation(currentEquation,1,3,"numbon",this);
        waitForSecs(1f);
        counters1.addAll(counters2);
        playAudio(feedbackAudio.get(2));
        waitForAudio();
        waitForSecs(0.3f);
        flashCounters(counters1,3);
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation(currentEquation,1,5,"numbon",this);
        waitForSecs(3f);
        clearScene(counters1);
        waitForSecs(0.4f);
    }

    public void demoFeedback2() throws Exception
    {
        hideScreenControls(true);
        waitForSecs(0.3f);
        List<OBControl> counters = loadRowOfCounters(eqPart1,0.3f);
        OC_Numberlines_Additions.hideEquation(currentEquation,this);
        OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,1,null,this);
        playAudio(feedbackAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : counters)
        {
            con.show();
            playSfxAudio("counter_1",false);
            waitForSecs(0.3f);
        }
        waitForSecs(0.5f);
        currentEquation.show();
        playSfxAudio("numbon",true);
        waitForSecs(1f);
        playAudio(feedbackAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        int index = 0;
        for(int i=0; i<eqPart2; i++)
        {
            OBControl counter = counters.get(eqPart1-i-1);
            counter.setZPosition(index+1);
            OBAnim anim = OBAnim.moveAnim(OB_Maths.locationForRect(0.9f-index*0.02f,0.95f,objectDict.get("feed_box") .frame()) ,counter);
            playSfxAudio("subtr",false);
            OBAnimationGroup.runAnims(Arrays.asList(anim),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            waitForSecs(0.1f);
            index++;
        }
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,3,"numbon",this);
        waitForSecs(1f);
        playAudio(feedbackAudio.get(2));
        waitForAudio();
        waitForSecs(0.3f);
        List<OBControl> flashCounters = counters.subList(0, eqPart3);
        flashCounters(flashCounters,3);
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,5,"numbon",this);
        waitForSecs(3f);
        clearScene(counters);
        waitForSecs(0.4f);
    }

    public void demoFeedback3() throws Exception
    {
        hideScreenControls(true);
        waitForSecs(0.3f);
        List<OBControl> counters = loadRowOfCounters(eqPart1,0.3f);
        playAudio(feedbackAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : counters)
        {
            con.show();
            playSfxAudio("counter_1",false);
            waitForSecs(0.3f);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        for(int i=0; i<eqPart2; i++)
        {
            playSfxAudio("match",true);
            OBControl counter = counters.get(i);
            counter.setZPosition(i+1);
            PointF loc = OBMisc.copyPoint(counter.position());
            loc.y += counter.height() * 1.05;
            OBAnim anim = OBAnim.moveAnim(loc,counter);
            playSfxAudio("slide",false);
            OBAnimationGroup.runAnims(Arrays.asList(anim),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            waitForSecs(0.1f);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(2));
        waitForAudio();
        waitForSecs(0.3f);
        List<OBControl> flashCounters = counters.subList(eqPart2, eqPart2 + eqPart3);
        flashCounters(flashCounters,3);
        waitForSecs(0.5f);
        currentEquation.show();
        playSfxAudio("eqnon",true);
        waitForSecs(3f);
        clearScene(counters);
        waitForSecs(0.4f);
    }

    public void demoFeedback4() throws Exception
    {
        hideScreenControls(true);
        waitForSecs(0.3f);
        List<OBControl> counters = loadRowOfCounters(eqPart2,0.3f);
        List<OBControl> counters1 = counters.subList(0, eqPart1);
        List<OBControl> counters2 = counters.subList(eqPart1, eqPart1+eqPart3);
        for(OBControl con : counters1)
        {
            PointF loc = OBMisc.copyPoint(con.position());
            loc.y += con.height() *1.05;
            con.setPosition(loc);
        }
        playAudio(feedbackAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : counters1)
        {
            con.show();
            playSfxAudio("counter_1",false);
            waitForSecs(0.3f);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : counters2)
        {
            con.show();
            playSfxAudio("counter_2",false);
            waitForSecs(0.3f);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(2));
        waitForAudio();
        waitForSecs(0.3f);
        flashCounters(counters2,3);
        waitForSecs(0.5f);
        currentEquation.show();
        playSfxAudio("eqnon",true);
        waitForSecs(3f);
        clearScene(counters);
        waitForSecs(0.4f);
    }

    public void demoFeedback5() throws Exception
    {
        hideScreenControls(true);
        waitForSecs(0.3f);
        List<List<OBControl>> countersArrays = new ArrayList<>();
        List<OBControl> children = new ArrayList<>();
        float maxHeight = -1;
        for(int i=1; i<=eqPart1; i++)
        {
            OBControl con = objectDict.get(String.format("obj_%d",i));
            if(con.height() > maxHeight)
                maxHeight = con.height();
            children.add(con);
        }
        OBControl feedBox = objectDict.get("feed_box");
        float targetHeight = feedBox.height()*0.9f*(1.0f/children.size());
        float rescale = targetHeight/maxHeight;
        float startY = 1.0f/(children.size()+2);
        for(int i=0; i<children.size(); i++)
        {
            OBControl con = children.get(i);
            con.setScale(con.scale() * rescale);
            float y = startY +(1.0f/children.size()) * i;
            con.setPosition(OB_Maths.locationForRect(0.1f,y,feedBox.frame()));
            con.setProperty("dest_loc", OBMisc.copyPoint(con.position()));
            con.setRight(0);
            con.show();
            List<OBControl> childCounters = loadRowOfCounters(eqPart3,y,0.1f,0.2f);
            countersArrays.add(childCounters);
        }

        for(int i=0; i<eqPart3; i++)
        {
            for(int j=0; j<countersArrays.size(); j++)
            {
                int index = i *countersArrays.size() + j;
                OBControl con = countersArrays.get(countersArrays.size()-j-1).get(eqPart3-i-1);
                con.setZPosition(1 + 0.1f * index);
                con.setProperty("dest_loc",OBMisc.copyPoint(con.position()));
                con.setPosition(OB_Maths.locationForRect(0.9f-index*0.02f,1f,objectDict.get("feed_box").frame()));
            }
        }
        playAudio(feedbackAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : children)
        {
            playSfxAudio("slide",false);
            OBAnim anim =OBAnim.moveAnim((PointF)con.propertyValue("dest_loc")  ,con);
            OBAnimationGroup.runAnims(Arrays.asList(anim),0.2,true,OBAnim.ANIM_EASE_OUT,this);
            waitSFX();
            waitForSecs(0.1f);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);

        lockScreen();
        for(List<OBControl> arr : countersArrays)
            for(OBControl con : arr)
                con.show();
        playSfxAudio("allcounterson",false);
        unlockScreen();
        waitSFX();

        waitForSecs(1f);
        for(int i=0; i<eqPart3; i++)
        {
            for(int j=0; j<countersArrays.size(); j++)
            {
                OBControl con = countersArrays.get(j) .get(i);
                OBAnim anim =OBAnim.moveAnim((PointF)con.propertyValue("dest_loc")  ,con);
                OBAnimationGroup.runAnims(Arrays.asList(anim),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                playSfxAudio("counter_1",false);
                waitForSecs(0.3f);
            }
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(2));
        waitForAudio();
        waitForSecs(0.3f);
        for(List<OBControl> arr : countersArrays)
        {
            flashCounters(arr,1);
        }
        waitForSecs(0.5f);
        currentEquation.show();
        playSfxAudio("eqnon",true);
        waitForSecs(3f);
        List<OBControl> allCounters = new ArrayList<>();
        for(List<OBControl> arr : countersArrays)
            allCounters.addAll(arr);
        clearScene(allCounters);
        waitForSecs(0.4f);
    }

    public void demoFeedback6() throws Exception
    {
        hideScreenControls(true);
        waitForSecs(0.3f);
        List<List<OBControl>> countersArrays = new ArrayList<>();
        List<OBControl> lines = new ArrayList<>();
        OBControl feedBox = objectDict.get("feed_box");
        for(int i=0; i<eqPart1; i++)
        {
            OBControl line = objectDict.get("line").copy();
            attachControl(line);
            float y = (1.0f/(eqPart1+1)) *(i+1);
            line.setWidth (objectDict.get("counter").width() * (1.2f * eqPart2 + 0.2f));
            line.setZPosition(0.1f);
            line.setPosition(OB_Maths.locationForRect(0.5f,y,feedBox.frame()));
            List<OBControl> lineCounters = loadRowOfCounters(eqPart2,y,-1,0.2f);
            countersArrays.add(lineCounters);
            lines.add(line);
        }
        playAudio(feedbackAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl line : lines)
        {
            line.show();
            playSfxAudio("allcounterson",true);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        for(List<OBControl> arr : countersArrays)
        {
            for(OBControl con : arr)
            {
                con.show();
                playSfxAudio("counter_1",true);
                waitForSecs(0.1f);
            }
            waitForSecs(0.5f);
        }
        waitForSecs(1f);
        playAudio(feedbackAudio.get(2));
        waitForAudio();
        waitForSecs(0.3f);
        List<OBControl> allObjects = new ArrayList<>();
        for(List<OBControl> arr : countersArrays)
            allObjects.addAll(arr);
        flashCounters(allObjects,3);
        waitForSecs(0.5f);
        currentEquation.show();
        playSfxAudio("eqnon",true);
        waitForSecs(3f);
        allObjects.addAll(lines);
        clearScene(allObjects);
        waitForSecs(0.4f);
    }

    public void demoIntro1() throws Exception
    {
        OBMisc.standardDemoIntro1(this);
    }

    public void demoIntro2() throws Exception
    {
        OBMisc.standardDemoIntro2(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),
                OB_Maths.locationForRect(0.8f,0.9f,this.bounds()), this);

    }



}
