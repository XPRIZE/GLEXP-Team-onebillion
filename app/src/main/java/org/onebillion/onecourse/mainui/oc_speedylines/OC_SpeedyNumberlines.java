package org.onebillion.onecourse.mainui.oc_speedylines;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 14/06/2018.
 */

public class OC_SpeedyNumberlines extends OC_SectionController
{
    static int TARGET_EQ1 = 1, TARGET_EQ2 = 2, TARGET_ANS = 3;

    List<Map<String,Object>> eventsData;
    boolean playFeedback;
    Map<String,Integer> eventColours;
    int currentMode, currentTarget;
    OBPath mainLine;
    boolean addMode;
    OBGroup currentEquation;
    List<OBLabel> targetControls;
    List<OBPath> currentLines;
    List<OBLabel> targetLabels;
    int eqPart1, eqPart2, eqPart3;
    int answerCount;
    OBPath eqLine;

    @Override
    public String sectionAudioName()
    {
        return String.format("spnl%s",parameters.get("mode"));
    }

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColours = OBMisc.loadEventColours(this);
        answerCount = 3;
        OBControl box = objectDict.get("line_box");
        PointF startLoc = OB_Maths.locationForRect(0f,0.5f,box.frame());
        PointF endLoc = OB_Maths.locationForRect(1f,0.5f,box.frame());
        currentMode = OBUtils.getIntValue(parameters.get("mode"));
        addMode = currentMode == 1 || currentMode == 3;
        if(addMode)
        {
            loadEvent("add");
            endLoc.x = this.bounds().width() + 10;
        }
        else
        {
            loadEvent("sub");
            startLoc.x = -10;
        }
        ((OBPath)objectDict.get("eq_box")).sizeToBoundingBoxIncludingStroke();
        mainLine = lineFrom(startLoc,endLoc,"main");
        mainLine.hide();
        eqLine =(OBPath)objectDict.get("eq_line");
        eqLine.sizeToBoundingBoxIncludingStroke();
        loadEventEquations();
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
                performSel("demo", currentEvent());
            }
        });
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        Map<String,Object> eventData = eventsData.get(eventIndex);
        currentTarget = (int)eventData.get("target");
        playFeedback = (boolean)eventData.get("feedback");
        if(eventData.get("eq_1") != null)
        {
            int num1 = (int)eventData.get("eq_1");
            int num2 = (int)eventData.get("eq_2");
            loadLineAndEquationNum1(num1,num2);
        }

        if(currentTarget == TARGET_EQ1)
        {
            targetControls = Arrays.asList(OC_Numberlines_Additions.getLabelForEquation(1,currentEquation));
        }
        else if(currentTarget == TARGET_EQ2)
        {
            targetControls =  Arrays.asList(OC_Numberlines_Additions.getLabelForEquation(3,currentEquation));
        }
        else
        {
            targetControls = targetLabels;
        }
    }

    @Override
    public void doMainXX() throws Exception
    {
        if(currentTarget == TARGET_EQ1)
        {
            showEquation();
            waitForSecs(0.3f);
            showNumberLine();
            waitForSecs(0.3f);
            playAudioQueuedScene("DEMO",0.3f,true);
            waitForSecs(0.3f);
            colourEquationPart(1);
            waitForSecs(0.3f);
        }
        else if(currentTarget == TARGET_EQ2)
        {
            colourEquationPart(3);
            waitForSecs(0.3f);
        }
        else
        {
            playAudioQueuedScene("DEMO",0.3f,true);
            waitForSecs(0.3f);
            animateNumbersSlide();
        }
        startScene();
    }

    public void startScene() throws Exception
    {
        long time = setStatus(STATUS_AWAITING_CLICK);
        OBMisc.doSceneAudio(7,time,this);
        flashCurrentTarget(time,14);
    }

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,(List<OBControl>)(Object)targetControls,pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        checkTarget((OBLabel)targ);
                    }
                });
            }
        }
    }

    public void checkTarget(OBLabel label) throws Exception
    {
        playAudio(null);
        if(currentTarget == TARGET_ANS)
        {
            if((boolean)label.propertyValue("correct"))
            {
                lockScreen();
                colourTargetLabels(eventColours.get("num2"));
                label.setColour(eventColours.get("highlight2"));
                unlockScreen();

                gotItRightBigTick(true);
                waitForSecs(0.5f);
                animateShowAnswer();
                waitForSecs(0.3f);
                if(playFeedback)
                {
                    demoFeedback(true);
                    flashFinalEquation();
                }
                playAudioQueuedScene("CORRECT",0.3f,true);
                waitForSecs(1.3f);
            }
            else
            {
                lockScreen();
                colourTargetLabels(eventColours.get("num2"));
                label.setColour(Color.GRAY);
                unlockScreen();

                gotItWrongWithSfx();
                waitSFX();

                playAudioQueuedScene("INCORRECT",0.3f,false);
                flashCorrectLabel();
                waitForAudio();
                demoFeedback(false);
                animateShowAnswer();
                flashFinalEquation();
                waitForSecs(2f);
            }
            clearScene();
            nextScene();
        }
        else
        {
            label.setColour(eventColours.get("highlight"));
            if(currentTarget == TARGET_EQ1)
            {
                animateShowFirstNumber();
            }
            else
            {
                animateShowLines();
                label.setColour(eventColours.get("num"));
            }
            waitForSecs(0.3f);
            nextScene();
        }
    }

    public void clearScene() throws Exception
    {
        if(eventIndex == events.size()-1)
            return;
        lockScreen();
        detachControl(currentEquation);
        for(OBControl con : currentLines)
        {
            if(con.propertyValue("label") != null)
                detachControl((OBControl)con.propertyValue("label"));
            detachControl(con);

        }
        for(OBControl con : targetLabels)
            detachControl(con);
        mainLine.hide();
        playSfxAudio("alloff",false);
        unlockScreen();
        waitSFX();
    }

    public void loadEventEquations()
    {
        int count = OBUtils.getIntValue(parameters.get("count"));
        String[] ranges = parameters.get("ranges").split(",");
        int x1 = OBUtils.getIntValue(ranges[0]) , x2 = OBUtils.getIntValue(ranges[1]);
        int y1 = OBUtils.getIntValue(ranges[2]) , y2 = OBUtils.getIntValue(ranges[3]);
        List<Integer> numList = OBMisc.integerList(x1,x2);
        numList = OBUtils.randomlySortedArray(numList);
        if(count > numList.size())
            count = numList.size();
        List<Integer> selectedNumbers = numList.subList(0, count);
        Collections.sort(selectedNumbers);

        eventsData = new ArrayList<>();
        List<String> eventsList = new ArrayList<>();
        if(OBUtils.getBooleanValue(parameters.get("example")))
        {
            eventsList.add("example");
            Map<String,Object> dict = new ArrayMap<>();
            int exNum1, exNum2;
            if(currentMode == 1)
            {
                exNum1 = 12;
                exNum2 = 6;
            }
            else if(currentMode == 2)
            {
                exNum1 = 15;
                exNum2 = 8;
            }
            else if(currentMode == 3)
            {
                exNum1 = 17;
                exNum2 = 23;
            }
            else
            {
                exNum1 = 57;
                exNum2 = 38;
            }
            dict.put("eq_1",exNum1);
            dict.put("eq_2",exNum2);
            dict.put("target",-1);
            dict.put("feedback",true);
            eventsData.add(dict);
        }

        for(int i=0; i<selectedNumbers.size(); i++)
        {
            int num1 = selectedNumbers.get(i).intValue();
            int toRand = y2;
            if(!addMode && toRand > num1)
                toRand = num1-1;
            int num2 = OB_Maths.randomInt(y1, toRand);
            for(int j=0; j<3; j++)
            {
                Map<String,Object> dict = new ArrayMap<>();
                dict.put("feedback",i<2);
                if(j == 0)
                {
                    dict.put("eq_1",num1);
                    dict.put("eq_2",num2);
                    dict.put("target",TARGET_EQ1);
                }
                else if(j == 1)
                {
                    dict.put("target",TARGET_EQ2);
                }
                else
                {
                    dict.put("target",TARGET_ANS);
                }
                eventsData.add(dict);
                String eventName = String.format("%d_%d",i+1,j+1);
                if(i == selectedNumbers.size()-1)
                    eventName = String.format("last_%d",j+1);
                if(audioScenes.get(eventName) == null)
                {
                    eventName = String.format("default_%d",j+1);
                }
                eventsList.add(eventName);
            }
        }
        events = eventsList;
    }


    public void loadLineAndEquationNum1(int num1,int num2)
    {
        eqPart1 = num1;
        eqPart2 = num2;
        String equationString;
        if(addMode)
        {
            eqPart3 = eqPart1 + eqPart2;
            equationString = String.format("%d + %d = %d",eqPart1,eqPart2,eqPart3);
        }
        else
        {
            eqPart3 = eqPart1 - eqPart2;
            equationString = String.format("%d – %d = %d",eqPart1,eqPart2,eqPart3);
        }
        OBControl equationBg = objectDict.get("eq_box").copy();
        attachControl(equationBg);
        OC_Numberlines_Additions.loadEquation(equationString,"equation",equationBg,eventColours.get("num") ,false,0,1,this);
        OBGroup equation =(OBGroup)objectDict.get("equation");
        equation.setZPosition(2);
        equationBg.setWidth(equation.width() + applyGraphicScale(80));
        equationBg.setZPosition(1);
        equationBg.show();
        currentEquation = new OBGroup(Arrays.asList(equation, equationBg));
        attachControl(currentEquation);
        currentEquation.settings.putAll(equation.settings);
        currentEquation.objectDict.putAll(equation.objectDict);
        currentEquation.objectDict.put("bg",equationBg);
        currentEquation.setZPosition(1);
        OC_Numberlines_Additions.hideEquation(currentEquation,this);
        OC_Numberlines_Additions.showEquation(currentEquation,1,4,this);
        RectF frame = currentEquation.objectDict.get("part5").getWorldFrame();
        eqLine.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 1), frame));
        eqLine.setZPosition(2);
        eqLine.hide();
        currentEquation.hide();
        boolean groupVals = currentMode > 2;
        int bigMarkCount = 0, smallMarkCount = 0;
        if(groupVals)
        {
            if(eqPart2 > 10)
                bigMarkCount = (int)Math.floor(eqPart2/10.0);
            smallMarkCount = eqPart2 - bigMarkCount * 10;
        }
        else
        {
            smallMarkCount = eqPart2;
        }
        OBControl box = objectDict.get("line_box");
        float xLoc = addMode?0:1;
        float dist = 1.0f/12;
        OBPath firstLine = lineFrom(OB_Maths.locationForRect(xLoc,0.4f,box.frame()),OB_Maths.locationForRect(xLoc,1f,box.frame()),"small");
        firstLine.setProperty("num",eqPart1);
        firstLine.setProperty("count",0);
        firstLine.setProperty("tens",false);
        firstLine.hide();
        currentLines = new ArrayList<>();
        currentLines.add(firstLine);
        for(int i=0; i<bigMarkCount+smallMarkCount; i++)
        {
            boolean bigMark = i < bigMarkCount;
            int num = 0;
            int count = 10 * (int)OB_Maths.clamp(0,bigMarkCount,i+1) + (int)OB_Maths.clamp(0,smallMarkCount,i+1-bigMarkCount);
            if(addMode)
            {
                xLoc =(i+1) *dist;
                num = eqPart1 + count;
            }
            else
            {
                xLoc = 1.0f -(i+1) *dist;
                num = eqPart1 - count;
            }
            String template = bigMark ? "big" : "small";
            OBPath line = lineFrom(OB_Maths.locationForRect(xLoc,0.4f,box.frame()),OB_Maths.locationForRect(xLoc,0.85f,box.frame()),template);
            line.setProperty("num",num);
            line.setProperty("tens",bigMark);
            line.setProperty("count",count);
            currentLines.add(line);
            line.hide();
        }
        loadLabelForLine(currentLines.get(0));
        loadLabelForLine(currentLines.get(currentLines.size()-1));
        List<Integer> choices = new ArrayList<>();
        for(int i=eqPart3-4; i<=eqPart3+4; i++)
        {
            if(i!=eqPart3 && i>0)
                choices.add(i);
        }
        List<Integer> randChoices = OBUtils.randomlySortedArray(choices);
        List<Integer> screenChoices = new ArrayList<>();
        screenChoices.addAll(randChoices.subList(0, answerCount-1));
        screenChoices.add(eqPart3);
        randChoices = OBUtils.randomlySortedArray(screenChoices);
        targetLabels = new ArrayList<>();
        for(int i=0; i<randChoices.size(); i++)
        {
            int num = randChoices.get(i).intValue();
            OBLabel label = new OBLabel(String.format("%d",num),OBUtils.StandardReadingFontOfSize(70));
            label.setPosition (OB_Maths.locationForRect(0.1f + (i+1)*0.8f/(answerCount+1),0.5f,objectDict.get("bottom_bar").frame()));
            label.setColour(eventColours.get("num2"));
            attachControl(label);
            label.setProperty("correct", num == eqPart3);
            targetLabels.add(label);
            label.hide();
            label.setZPosition(4);
        }
        mainLine.hide();
    }

    public OBPath lineFrom(PointF pt1,PointF pt2,String template)
    {
        OBPath lineTemplate =(OBPath)objectDict.get(String.format("line_%s",template));
        OBPath line = new OBPath(pt1,pt2);
        line.setLineWidth(lineTemplate.lineWidth());
        line.setStrokeColor(lineTemplate.strokeColor());
        line.sizeToBoundingBoxIncludingStroke();
        line.setProperty("stroke_colour",lineTemplate.strokeColor());
        attachControl(line);
        return line;
    }

    public void animateNumbersSlide() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl con : targetLabels)
        {
            PointF loc = OBMisc.copyPoint(con.position());
            con.setLeft(con.left() - this.bounds().width());
            anims.add(OBAnim.moveAnim(loc,con));
            con.show();
        }
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_OUT,this);
        waitSFX();
    }

    public void loadLabelForLine(OBPath line)
    {
        int num = (int)line.propertyValue("num");
        OBLabel label = new OBLabel(String.format("%d",num) ,OBUtils.StandardReadingFontOfSize(60));
        PointF loc = OBMisc.copyPoint(line.position());
        loc.y = OB_Maths.locationForRect(0f,1.3f,objectDict.get("line_box").frame()).y;
        label.setPosition(loc);
        label.setColour(eventColours.get("num"));
        attachControl(label);
        label.hide();
        line.setProperty("label",label);
    }

    public void showEquation() throws Exception
    {
        lockScreen();
        eqLine.show();
        currentEquation.show();
        playSfxAudio("eqnon",false);
        unlockScreen();
        waitSFX();
    }

    public void showNumberLine() throws Exception
    {
        lockScreen();
        mainLine.show();
        currentLines.get(0).show();
        playSfxAudio("line",false);
        unlockScreen();
        waitSFX();
    }

    public void colourEquationPart(int num) throws Exception
    {
        OC_Numberlines_Additions.colourEquation(currentEquation,num,num,Color.RED,this);
        playSfxAudio("red",true);
    }

    public void playAudioForNum(int num,boolean wait) throws Exception
    {
        playAudio(String.format("n_%d",num));
        if(wait)
            waitForAudio();
    }

    public void animateShowFirstNumber() throws Exception
    {
        OBPath line = currentLines.get(0);
        OBLabel label = (OBLabel)line.propertyValue("label");
        label.setColour(Color.RED);
        label.show();
        playSfxAudio("start",true);
        waitForSecs(0.5f);
        lockScreen();
        label.setColour(eventColours.get("num"));
        OC_Numberlines_Additions.colourEquation(currentEquation,1,1,eventColours.get("num") ,this);
        unlockScreen();
    }

    public void animateShowLines() throws Exception
    {
        for(int i=1; i<currentLines.size(); i++)
        {
            OBPath line = currentLines.get(i);
            int count = (int)line.propertyValue("count");
            boolean tens = (boolean)line.propertyValue("tens");
            line.show();
            playSfxAudio(tens ? "ten" : "unit",true);
            waitForSecs(0.1f);
        }
    }

    public void animateHighlightAndPlayLine(OBPath line,boolean lowLight) throws Exception
    {
        int num = (int)line.propertyValue("num");
        boolean tens = (boolean)line.propertyValue("tens");
        int colour = line.strokeColor();
        OBLabel label = (OBLabel)line.propertyValue("label");
        if(line == currentLines.get(0))
        {
            lockScreen();
            line.setStrokeColor(Color.RED);
            label.setColour(Color.RED);
            unlockScreen();
        }
        else
        {
            line.setStrokeColor(Color.RED);
        }
        playAudioForNum(num,true);
        if(lowLight)
        {
            if(label != null)
                label.setColour(eventColours.get("num"));
            line.setStrokeColor(colour);
        }
    }

    public void showAnswerOnNumberLine() throws Exception
    {
        OBPath line = currentLines.get(currentLines.size() - 1);
        OBLabel label = (OBLabel)line.propertyValue("label");
        lockScreen();
        label.setColour(Color.RED);
        line.setStrokeColor(Color.RED);
        label.show();
        for(OBControl con : targetLabels)
            con.hide();
        playSfxAudio("ans",false);
        unlockScreen();
        waitSFX();
    }

    public void showAnswerOnEquation() throws Exception
    {
        lockScreen();
        OC_Numberlines_Additions.colourEquation(currentEquation,5,5,Color.RED,this);
        OC_Numberlines_Additions.showEquation((OBGroup) currentEquation,1,5,null,this);
        eqLine.hide();
        playSfxAudio("final",false);
        unlockScreen();
        waitSFX();
    }

    public void lowLightAnswer() throws Exception
    {
        OBPath line = currentLines.get(currentLines.size() - 1);
        OBLabel label = (OBLabel)line.propertyValue("label");
        lockScreen();
        line.setStrokeColor((int)line.propertyValue("stroke_colour"));
        label.setColour(eventColours.get("num"));
        OC_Numberlines_Additions.colourEquation(currentEquation,1,5,eventColours.get("num") ,this);
        unlockScreen();
    }

    public void animateShowAnswer() throws Exception
    {
        showAnswerOnNumberLine();
        waitForSecs(0.3f);
        showAnswerOnEquation();
        waitForSecs(2f);
        lowLightAnswer();
    }

    public void flashFinalEquation() throws Exception
    {
        OBPath lastMark = currentLines.get(currentLines.size()-1);
        int markColour = lastMark.strokeColor();
        OBLabel label = (OBLabel)lastMark.propertyValue("label");
        for(int i=0; i<3; i++)
        {
            lockScreen();
            OC_Numberlines_Additions.colourEquation(currentEquation,5,5,Color.RED,this);
            label.setColour(Color.RED);
            lastMark.setStrokeColor(Color.RED);
            unlockScreen();

            waitForSecs(0.3f);

            lockScreen();
            OC_Numberlines_Additions.colourEquation(currentEquation,5,5,eventColours.get("num") ,this);
            label.setColour(eventColours.get("num"));
            lastMark.setStrokeColor(markColour);
            unlockScreen();

            waitForSecs(0.3f);
        }
    }

    public void animateFeedback(boolean lowLight) throws Exception
    {
        for(int i=0; i<currentLines.size(); i++)
        {
            animateHighlightAndPlayLine(currentLines.get(i),lowLight || i!=currentLines.size()-1);

        }
    }

    public void flashCorrectLabel() throws Exception
    {
        for(int i=0; i<3; i++)
        {
            for(OBLabel label : targetLabels)
            {
                if((boolean)label.propertyValue("correct"))
                {
                    label.setColour(eventColours.get("flash"));
                    waitForSecs(0.3f);
                    label.setColour(eventColours.get("num2"));
                    waitForSecs(0.3f);
                    if(i==2)
                        label.setColour(eventColours.get("flash"));
                }
            }
        }
    }

    public void colourCurrentLinesFrom(int from,int colour)
    {
        for(int i=from; i<currentLines.size(); i++)
        {
            OBPath line = currentLines.get(i);
            line.setStrokeColor(colour);
        }
    }

    public void colourTargetLabels(int colour)
    {
        for(OBLabel lab : targetControls)
            lab.setColour(colour);
    }

    public void flashCurrentTarget(final long time,final float delay) throws Exception
    {
        int col1, col2;
        if(currentTarget == TARGET_ANS)
        {
            col1 = Color.RED;
            col2 = eventColours.get("num2");
        }
        else
        {
            col1 = eventColours.get("num");
            col2 = Color.RED;
        }
        final int colour1 = col1, colour2 = col2;
        OBUtils.runOnOtherThreadDelayed(delay,new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                try
                {
                    if(statusChanged(time))
                        return;
                    while (!statusChanged(time))
                    {
                        for (int i = 0; i < 3 && !statusChanged(time); i++) {
                            lockScreen();
                            colourTargetLabels(colour1);
                            unlockScreen();

                            if (statusChanged(time)) break;
                            waitForSecs(0.4f);
                            if (statusChanged(time)) break;

                            lockScreen();
                            colourTargetLabels(colour2);
                            unlockScreen();

                            if (statusChanged(time)) break;
                            waitForSecs(0.4f);
                            if (statusChanged(time)) break;

                        }
                        if (!statusChanged(time))
                            waitForSecs(7f);

                    }

                    colourTargetLabels(colour2);

                } catch (Exception exception) {

                }
            }
        });
    }


    public void demoFeedback(boolean lowLight) throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBPath line = currentLines.get(0);
        OBLabel label = (OBLabel)line.propertyValue("label");
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,label.frame()),-30,0.5f,"FINAL",1,0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        animateFeedback(lowLight);
    }

    public void demo1_1() throws Exception
    {
        showEquation();
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,1.1f,currentEquation.frame()),-30,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-30,0.5f,true);
        showNumberLine();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.6f,this.bounds()),-20,0.5f,"DEMO",1,0.3f);
        int index = 2;
        if(!addMode)
        {
            PointF loc = OBMisc.copyPoint(currentLines.get(0).position());
            loc.x += applyGraphicScale(5);
            loc.y += applyGraphicScale(50);
            moveScenePointer(loc,-20,0.5f,"DEMO",index,0.3f);
            index++;
        }
        moveScenePointer(OB_Maths.locationForRect(0.7f,2.2f,currentEquation.objectDict.get("part1").getWorldFrame()),-25,0.5f,"DEMO",index,0.3f);
        colourEquationPart(1);
        waitForSecs(0.3f);
        thePointer.hide();
        startScene();
    }

    public void demo1_2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,2.2f,currentEquation.objectDict.get("part3").getWorldFrame()),-20,0.5f,"DEMO",0,0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        colourEquationPart(3);
        startScene();
    }

    public void demo1_3() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        OBPath line = currentLines.get(0);
        OBLabel label = (OBLabel)line.propertyValue("label");
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,label.frame()),-25,0.5f,true);
        int colour = line.strokeColor();

        lockScreen();
        line.setStrokeColor(Color.RED);
        label.setColour(Color.RED);
        unlockScreen();

        playAudioScene("DEMO",1,true);
        lockScreen();
        line.setStrokeColor(colour);
        label.setColour(eventColours.get("num"));
        unlockScreen();

        waitForSecs(0.5f);
        int index = 2;
        if(currentMode > 2)
        {
            moveScenePointer(OB_Maths.locationForRect(1f,1.5f,label.frame()) ,-30,0.5f,"DEMO",index,0.3f);
            index++;
        }
        animateNumbersSlide();
        movePointerToPoint(OB_Maths.locationForRect(0.1f,0.9f,objectDict.get("bottom_bar") .frame()),-40,0.5f,true);
        playAudioScene("DEMO",index,false);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("bottom_bar") .frame()),-20,2f,true);
        waitForAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        startScene();
    }

    public void demo2_3() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        animateNumbersSlide();
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.7f,objectDict.get("bottom_bar") .frame()),-20,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoIntro1() throws Exception
    {
        OBMisc.standardDemoIntro1(this);
    }

    public void demoIntro2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-30,0.5f,true);
        playAudio(getAudioForScene("intro2","DEMO") .get(0));
        waitAudio();
        waitForSecs(0.3f);
    }

    public void demoexample() throws Exception
    {
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        showEquation();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.9f,1.1f,currentEquation.frame()),-15,0.5f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-30,0.5f,true);
        showNumberLine();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.7f,this.bounds()),-25,0.5f,"DEMO",2,0.3f);
        if(!addMode)
        {
            PointF loc = OBMisc.copyPoint(currentLines.get(0).position());
            loc.x += applyGraphicScale(5);
            loc.y += applyGraphicScale(50);
            moveScenePointer(loc,-25,0.5f,"DEMO",3,0.3f);
        }
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.9f,this.bounds()),-30,0.5f,"DEMO2",0,0.3f);
        demoPointerTouchEqNum(1,"DEMO2",1);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.7f,this.bounds()),-10,0.5f,true);
        animateShowFirstNumber();
        OBPath line = currentLines.get(0);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,((OBLabel)line.propertyValue("label")).frame()),-30,0.5f,"DEMO2",2,0.3f);
        demoPointerTouchEqNum(3,"DEMO3",0);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.7f,this.bounds()),-10,0.5f,true);
        animateShowLines();
        if(currentMode <= 2)
        {
            moveScenePointer(OB_Maths.locationForRect(0.6f,0.7f,this.bounds()),-30,0.5f,"DEMO4",0,0.3f);

            lockScreen();
            colourCurrentLinesFrom(1,Color.RED);
            playSfxAudio("red",false);
            unlockScreen();
            waitSFX();

            waitForSecs(0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.7f,1.4f,currentEquation.objectDict.get("part3").getWorldFrame()),-15,0.5f,"DEMO4",1,0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-30,0.5f,true);
        }
        else
        {
            PointF loc = OBMisc.copyPoint(currentLines.get(1).position());
            loc.x += applyGraphicScale(5);
            loc.y += applyGraphicScale(40);
            moveScenePointer(loc,-25,0.5f,"DEMO4",0,0.3f);

            lockScreen();
            colourCurrentLinesFrom(1,Color.RED);
            playSfxAudio("red",false);

            unlockScreen();
            waitForSecs(0.3f);
            loc.y += applyGraphicScale(20);
            moveScenePointer(loc,-25,0.5f,"DEMO4",1,0.3f);
            playAudioScene("DEMO4",2,true);
            waitForSecs(0.3f);
        }
        waitForSecs(0.3f);
        for(int i=0; i<4; i++)
        {
            waitForSecs(0.3f);

            lockScreen();
            OC_Numberlines_Additions.colourEquation(currentEquation,3,3,eventColours.get("num") ,this);
            colourCurrentLinesFrom(1,objectDict.get("line_small").strokeColor());
            unlockScreen();

            if(i<3)
            {
                waitForSecs(0.3f);

                lockScreen();
                OC_Numberlines_Additions.colourEquation(currentEquation,3,3,Color.RED,this);
                colourCurrentLinesFrom(1,Color.RED);
                unlockScreen();
            }
        }
        waitForSecs(0.5f);
        moveScenePointer(OB_Maths.locationForRect(0.9f,1.2f,currentEquation.frame()),-15,0.5f,"DEMO5",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,((OBLabel)line.propertyValue("label")).frame()),-30,0.5f,"DEMO5",1,0.3f);
        playAudioScene("DEMO5",2,true);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-30,0.5f,true);
        animateFeedback(false);
        waitForSecs(0.3f);
        showAnswerOnNumberLine();
        line = currentLines.get(currentLines.size()-1);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,((OBLabel)line.propertyValue("label")).frame()),-30,0.5f,"DEMO6",0,0.3f);
        showAnswerOnEquation();
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.2f,currentEquation.objectDict.get("part5").getWorldFrame()),-15,0.5f,"DEMO7",0,0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        lowLightAnswer();
        waitForSecs(2f);
        clearScene();
        waitForSecs(0.3f);
        playAudioScene("DEMO7",1,true);
        waitForSecs(0.3f);
        nextScene();
    }

    public void demoPointerTouchEqNum(int num,String cat,int index) throws Exception
    {
        OBControl eqPart = currentEquation.objectDict.get(String.format("part%d",num));
        movePointerToPoint(OB_Maths.locationForRect(0.7f,2.3f,eqPart.getWorldFrame()),-20,0.5f,true);
        waitForSecs(0.3f);
        colourEquationPart(num);
        waitForSecs(0.3f);
        playAudioScene(cat,index,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,eqPart.getWorldFrame()),-20,0.3f,true);
        OC_Numberlines_Additions.colourEquation(currentEquation,num,num,eventColours.get("highlight") ,this);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.4f,eqPart.getWorldFrame()),-20,0.2f,true);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(currentEquation,num,num,Color.RED,this);
    }
}
