package org.onebillion.onecourse.mainui.oc_moreaddsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 16/03/2017.
 */

public class OC_MoreAddSubtract_S2 extends OC_SectionController
{
    OBControl leftControl, rightControl;
    List<OBLabel> targetNums;
    List<OBControl> leftTargets, rightTargets;
    int equationColour, overlayColour;
    int currentNum, currentPhase, correctNum, incorrectCount;
    boolean freeMode, demoMode, buttonPressed, subtractMode;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        String startEvent = parameters.get("start");
        loadEvent(String.format("master%s",startEvent));
        buttonPressed = false;
        subtractMode = OBUtils.getBooleanValue(eventAttributes.get("subtractmode"));
        targetNums = new ArrayList<>();
        leftTargets = new ArrayList<>();
        rightTargets = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        equationColour = OBUtils.colorFromRGBString(eventAttributes.get("equationcolour"));
        if (eventAttributes.get("overlaycolour") != null)
            overlayColour = OBUtils.colorFromRGBString(eventAttributes.get("overlaycolour"));
        OBPath box = (OBPath)objectDict.get("box");
        box.sizeToBoundingBoxIncludingStroke();
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                performSel("demo", currentEvent());
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        currentPhase = 0;
        currentNum = 0;
        incorrectCount = 0;
        objectDict.get("button").setOpacity(1);
        if(eventAttributes.get("colourright") != null || eventAttributes.get("colourleft") != null)
        {
            int colourleft = 0;
            int colourright = 0;
            if(eventAttributes.get("colourleft") != null)
                colourleft = OBUtils.colorFromRGBString(eventAttributes.get("colourleft"));
            if(eventAttributes.get("colourright") != null)
                colourright = OBUtils.colorFromRGBString(eventAttributes.get("colourright"));

            if(objectDict.get("obj").getClass() == OBGroup.class)
            {
                OBGroup obj = (OBGroup)objectDict.get("obj");
                if(eventAttributes.get("colourleft") != null)
                {
                    leftControl = obj.copy();
                    colourControl((OBGroup)leftControl, colourleft);
                }
                if(eventAttributes.get("colourright") != null)
                {
                    rightControl = obj.copy();
                    colourControl((OBGroup)rightControl, colourright);
                }

            }
            else
            {
                OBControl obj = objectDict.get("obj");
                obj.show();
                if(obj.getClass() == OBPath.class)
                    ((OBPath)obj).sizeToBoundingBoxIncludingStroke();

                if(eventAttributes.get("colourleft") != null)
                {
                    leftControl = obj.copy();
                    leftControl.setFillColor(colourleft);
                }
                if(eventAttributes.get("colourright") != null)
                {
                    rightControl = obj.copy();
                    rightControl.setFillColor(colourright);
                }
                obj.hide();

            }

        }

        if(eventAttributes.get("left") != null)
            distributeObj(leftControl, eventAttributes.get("left"), leftTargets);
        if(eventAttributes.get("right") != null)
            distributeObj(rightControl, eventAttributes.get("right"), rightTargets);


        String[] eqParts =  eventAttributes.get("equation").split(" ") ;
        correctNum = Integer.valueOf(eqParts[2]);
        if(eventAttributes.get("nums") != null)
        {
            freeMode = false;
            loadNumbers(eventAttributes.get("nums"));
        }
        else
        {
            freeMode = true;
        }
        eqParts[2] = "88";
        String equationString = OBUtils.stringJoin(eqParts, " ");
        detachControl(objectDict.get("equation"));
        OC_Numberlines_Additions.loadEquation(equationString, "equation",
                objectDict.get("eqbox"),equationColour, false, 0, 1, this);

        for(int i=1; i<6; i++)
            OC_Numberlines_Additions.getLabelForEquation(i,(OBGroup)objectDict.get("equation")).hide();

        demoMode = eventAttributes.get("demo") != null;
    }

    public void doMainXX() throws Exception
    {
        startButtonPhase();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            if(currentPhase == 0 && objectDict.get("button").frame().contains(pt.x, pt.y) && !(demoMode && currentNum ==correctNum))
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(
                        new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkButton();
                            }
                        }
                );
            }
            else if(currentPhase == 1)
            {
                final OBControl cont = finger(0,1,(List<OBControl>)(Object)targetNums,pt);
                if(cont != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkLabel((OBLabel)cont);
                                }
                            }
                    );
                }
            }
            else if(freeMode && objectDict.get("box") .frame().contains( pt.x, pt.y)
                    && demoMode == false)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(
                        new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkBox();
                            }
                        }
                );

            }
            else if(freeMode && finger(0,1,Arrays.asList(objectDict.get("button_arrow")),pt) != null
                    && ((demoMode && currentNum ==correctNum) || !demoMode))
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(
                        new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                checkArrowButton();
                            }
                        }
                );

            }
        }
    }

    public void checkButton() throws Exception
    {
        OBGroup button = (OBGroup)objectDict.get("button");
        button.objectDict.get("highlight").show();
        if(currentNum < rightTargets.size())
        {
            showCurrentObject();
            currentNum++;
            if(freeMode == false && currentNum == correctNum)
            {
                waitSFX();
                playAudio(null);
                button.objectDict.get("highlight").hide();
                startNumberPhase();
            }
            else
            {
                buttonPressed = true;
                if(demoMode && currentNum ==correctNum)
                {
                    waitSFX();
                    playAudioScene("FEEDBACK", currentNum -1,true);
                    performSel("demoPhase", currentEvent());

                }
                else
                {
                    long time = setStatus(STATUS_AWAITING_CLICK);
                    waitSFX();
                    if(time == statusTime)
                    {
                        if(demoMode)
                        {
                            playAudioScene("FEEDBACK", currentNum -1,false);

                        }
                        else if(currentNum == 0)
                        {
                            playAudioQueuedScene("FIRSTPRESS",300,false);

                        }
                    }
                    if(currentNum == correctNum && getAudioForScene(currentEvent(),"REMIND2") != null)
                    {
                        reprompt(time,OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"REMIND"),300), 4);
                    }
                }
            }
        }
        else
        {
            buttonPressed = true;
            setStatus(STATUS_AWAITING_CLICK);
            playSFX("wrong");
        }
    }

    public void checkLabel(OBLabel cont) throws Exception
    {
        if(Integer.valueOf(cont.text()) == correctNum)
        {
            playAudio(null);
            lockScreen();
            cont.setColour(Color.RED);
            OBGroup equation = (OBGroup)objectDict.get("equation");
            OC_Numberlines_Additions.getLabelForEquation(4,equation).show();
            OC_Numberlines_Additions.getLabelForEquation(5,equation).show();
            unlockScreen();

            playSfxAudio("equcomplete",true);
            gotItRightBigTick(true);
            cont.setColour(Color.BLACK);
            playAudioQueuedScene("FINAL",300,true);
            waitForSecs(0.3f);
            finishScene();
        }
        else
        {
            cont.setColour(Color.RED);
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            cont.setColour(Color.BLACK);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",300,false);

        }
    }

    public void checkBox() throws Exception
    {
        OBControl box = objectDict.get("box");
        box.highlight();
        playSfxAudio("buttonpress",true);
        lockScreen();
        box.lowlight();
        resetScene();
        unlockScreen();
        currentNum = 0;
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkArrowButton() throws Exception
    {
        buttonSet(1);
        if(currentNum == correctNum)
        {
            gotItRightBigTick(true);

            lockScreen();
            OBGroup equation = (OBGroup)objectDict.get("equation");
            OC_Numberlines_Additions.getLabelForEquation(3,equation).setOpacity(1);
            objectDict.get("cover").hide();
            buttonSet(2);
            unlockScreen();

            waitForSecs(0.3f);
            finishScene();

        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            lockScreen();
            resetScene();
            unlockScreen();
            incorrectCount++;
            if(incorrectCount >= 2)
            {
                buttonSet(2);
                objectDict.get("button").setOpacity(0.5f);
                waitForSecs(0.3f);
                playAudioScene("INCORRECT2",0,true);
                waitForSecs(0.3f);
                int audioIndex = 1;
                while(currentNum != correctNum)
                {
                    showCurrentObject();
                    currentNum++;
                    playAudioScene("INCORRECT2",audioIndex,true);
                    audioIndex++;
                    waitForSecs(0.3f);

                }
                lockScreen();
                OC_Numberlines_Additions.getLabelForEquation(3,(OBGroup)objectDict.get("equation")).setOpacity(1);
                objectDict.get("cover").hide();

                unlockScreen();
                waitForSecs(0.5f);
                OBGroup equ =(OBGroup)objectDict.get("equation");
                OC_Numberlines_Additions.colourEquation(equ,1,5,Color.RED,this);
                playAudioQueuedScene("INCORRECT3",300,true);
                waitForSecs(0.3f);
                OC_Numberlines_Additions.colourEquation(equ,1,5,equationColour,this);
                clearAndNextScene();
            }
            else
            {
                buttonSet(0);
                setStatus(STATUS_AWAITING_CLICK);
                playAudioQueuedScene("INCORRECT",300,false);
            }
        }
    }

    public void touchUpAtPoint(PointF pt, View v)
    {
        if(buttonPressed)
        {
            OBGroup button = (OBGroup)objectDict.get("button");
            button.objectDict.get("highlight").hide();
            buttonPressed = false;
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4, setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void startNumberPhase() throws Exception
    {
        String[] eqParts =  eventAttributes.get("equation").split(" ");
        correctNum = Integer.valueOf(eqParts[4]);
        playAudioQueuedScene("DEMO2",300,true);
        waitForSecs(0.3f);
        showNumbers();
        waitForSecs(0.3f);
        currentPhase = 1;
        setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),"PROMPT2.REPEAT"), 300));
        playAudioQueuedScene(currentEvent(), "PROMPT2", 300, false);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void startButtonPhase() throws Exception
    {
        showFirstPart();
        playAudioQueuedScene("DEMO",300,true);
        waitForSecs(0.3f);
        if(freeMode)
        {
            showSecondPart();
            buttonSet(0);
            waitForSecs(0.3f);
        }
        startScene();
    }

    public void colourControl(OBGroup obj, int colour)
    {
        for(OBControl con : obj.filterMembers("background.*"))
        {
            con.setFillColor(colour);
        }
    }

    public void distributeObj(OBControl obj,String list, List<OBControl> container)
    {
        deleteAllControls(container);
        String[] array = list.split(",");
        int index = 1;
        for(int i=0;i< array.length; i+=2)
        {
            OBControl newObj = obj.copy();
            newObj.setPosition(OB_Maths.locationForRect(Float.valueOf(array[i]),Float.valueOf(array[i+1]),objectDict.get("box").frame()));
            attachControl(newObj);
            container.add(newObj);
            index++;
            newObj.hide();
        }
    }

    public void loadNumbers(String list)
    {
        deleteAllControls((List<OBControl>)(Object)targetNums);
        String[] array = list.split(",");
        OBControl numbox = objectDict.get("numbox");
        int index =0;
        for(String numString : array)
        {
            OBLabel label = new OBLabel(numString,OBUtils.standardTypeFace(),70.0f*numbox.height()/100.0f); label.setColour(Color.BLACK);
            label.setPosition ( OB_Maths.locationForRect(index*1.0f/(array.length-1),0.5f,numbox.frame()));
            attachControl(label);
            targetNums.add(label);
            label.hide();
            index++;
        }
    }

    public void deleteAllControls(List<OBControl> array)
    {
        for(OBControl obj : array)
            detachControl(obj);
        array.clear();

    }

    public void showFirstPart() throws Exception
    {
        lockScreen();
        for(OBControl cont : (subtractMode ? rightTargets : leftTargets))
            cont.show();

        OC_Numberlines_Additions.getLabelForEquation(1, (OBGroup)objectDict.get("equation")).show();

        playSfxAudio("objectson",false);
        unlockScreen();

        waitSFX();

    }

    public void showSecondPart() throws Exception
    {
        lockScreen();
        OBGroup equation = (OBGroup)objectDict.get("equation");
        OC_Numberlines_Additions.getLabelForEquation(2, equation).show();
        OC_Numberlines_Additions.getLabelForEquation(4, equation).show();
        OC_Numberlines_Additions.getLabelForEquation(5, equation).show();

        objectDict.get("cover").setPosition(OC_Numberlines_Additions.getLabelForEquation(3, equation).getWorldPosition());
        objectDict.get("cover").show();

        playSfxAudio("equcomplete",false);

        unlockScreen();
        waitSFX();
    }

    public void showNumbers() throws Exception
    {
        lockScreen();
        for(OBControl num : targetNums)
            num.show();
        playSfxAudio("numpopon",false);
        unlockScreen();
        waitSFX();
    }

    public void clearScene()
    {
        lockScreen();
        for(OBControl con : leftTargets)
            con.hide();
        for(OBControl con : rightTargets)
            con.hide();
        for(OBControl con : targetNums)
            con.hide();
        objectDict.get("equation").hide();

        unlockScreen();

    }

    public void finishScene() throws Exception
    {
        if(!performSel("demoFin",currentEvent()))
        {
            OBGroup equ = (OBGroup)objectDict.get("equation");
            OC_Numberlines_Additions.colourEquation(equ, 1, 5, Color.RED, this);

            playAudioQueuedScene(freeMode?"FINAL":"FINAL2",300,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation(equ, 1, 5, equationColour, this);

        }
        clearAndNextScene();
    }

    public void clearAndNextScene() throws Exception
    {
        waitForSecs(0.7f);
        if(currentEvent() != events.get(events.size()-1))
            clearScene();
        waitForSecs(0.3f);
        nextScene();
    }

    public void showCurrentObject() throws Exception
    {
        lockScreen();
        if(!subtractMode)
        {
            rightTargets.get(currentNum).show();
        }
        else
        {
            OBControl control = rightTargets.get(currentNum);
            control.setColourOverlay(overlayColour);
        }

        OBGroup equation = (OBGroup)objectDict.get("equation");
        if(currentNum == 0)
        {
            OC_Numberlines_Additions.getLabelForEquation(2, equation).show();
            OC_Numberlines_Additions.getLabelForEquation(3, equation).show();
        }

        playSfxAudio("buttonpress",false);
        OBLabel label = OC_Numberlines_Additions.getLabelForEquation(3, equation);
        label.setString(String.format("%d", currentNum +1));

        if(freeMode)
            label.setOpacity(0.5f);

        unlockScreen();
    }

    public void buttonSet(int state)
    {
        OBGroup button = (OBGroup)objectDict.get("button_arrow");
        lockScreen();
        button.objectDict.get("selected").hide();
        button.objectDict.get("inactive").hide();
        if(state == 1)
            button.objectDict.get("selected").show();
        else if(state == 2)
            button.objectDict.get("inactive").show();

        unlockScreen();
    }


    public void resetScene()
    {
        if(!subtractMode)
        {
            for(OBControl obj : rightTargets)
                obj.hide();
        }
        else
        {
            for(OBControl obj : rightTargets)
                obj.lowlight();
        }
        OC_Numberlines_Additions.getLabelForEquation(3, (OBGroup)objectDict.get("equation")).hide();
        currentNum = 0;
    }

    public void demoFinPoint() throws Exception
    {
        int audioCount = getAudioForScene(currentEvent(),"FINAL").size();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.9f,objectDict.get("box").frame()),-30,0.5f,"FINAL",0,0.3f);
        if(audioCount > 2)
        {
            playAudioScene("FINAL",1,true);
            waitForSecs(0.3f);
        }

        movePointerToPoint(OB_Maths.locationForRect(0.8f,1.1f,objectDict.get("equation").frame()),-35,0.5f,true);
        OBGroup equ = (OBGroup)objectDict.get("equation");
        OC_Numberlines_Additions.colourEquation(equ, 1, 5, Color.RED, this);
        playAudioScene("FINAL",audioCount-1,true);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(equ, 1, 5, equationColour, this);
        waitForSecs(0.3f);
        thePointer.hide();
    }

    public void pointerPointEquation(OBGroup equation, int at, String audio, float duration) throws Exception
    {
        PointF point = OBMisc.copyPoint(OB_Maths.locationForRect(0.6f,1f,equation.objectDict.get(String.format("part%d",at)).getWorldFrame()));
        point.y = equation.bottom() + equation.height() * 0.2f;
        movePointerToPoint(point, -30, duration, true);
        OC_Numberlines_Additions.colourEquation(equation, at, at, Color.RED, this);
        playAudio(audio);
        waitAudio();
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(equation, at, at, equationColour, this);
    }

    public void demo2a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        showFirstPart();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.4f,0.8f,objectDict.get("box").frame()),-30,0.5f,"DEMO",0,0.3f);
        objectDict.get("button").show();
        playSfxAudio("buttonpopon",true);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-25,0.5f,"DEMO",1,0.3f);
        OBGroup button = (OBGroup)objectDict.get("button");
        for(int i= 0; i < 5; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button").frame()),-25,0.2f,true);
            button.objectDict.get("highlight").show();
            showCurrentObject();
            currentNum++;
            waitSFX();
            button.objectDict.get("highlight").hide();
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-25,0.2f,true);
            waitForSecs(0.3f);
        }

        moveScenePointer(OB_Maths.locationForRect(0.9f,1.05f,objectDict.get("box").frame()),-30,0.5f,"DEMO",2,0.3f);
        showNumbers();
        waitForSecs(0.3f);
        playAudioScene("DEMO",3,false);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,targetNums.get(1).getWorldFrame()),-15,0.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,targetNums.get(1).getWorldFrame()),-15,0.2f,true);
        lockScreen();

        targetNums.get(1).setColour(Color.RED);

        OBGroup equation = (OBGroup)objectDict.get("equation");
        OC_Numberlines_Additions.getLabelForEquation(4, equation).show();
        OC_Numberlines_Additions.getLabelForEquation(5, equation).show();
        unlockScreen();
        playSfxAudio("equcomplete",true);

        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,targetNums.get(1).getWorldFrame()),-15,0.2f,true);

        targetNums.get(1).setColour(Color.BLACK);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        for(int i=1; i<6; i++)
        {
            OC_Numberlines_Additions.colourEquation(equation, i, i, Color.RED, this);
            playAudioScene("DEMO",3+i,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation(equation, i, i, equationColour, this);
        }
        waitForSecs(1.2f);
        clearScene();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demoShortStart() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showFirstPart();
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        startScene();

    }
    public void demo2b() throws Exception
    {
        demoShortStart();
    }

    public void demoFin2b() throws Exception
    {
        loadPointer(POINTER_LEFT);
        for(int i=0; i<5; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"),i+1, getAudioForSceneIndex(currentEvent(), "FINAL2", i), i==0?0.5f:0.3f);
        }
        waitForSecs(0.6f);
        thePointer.hide();
    }

    public void demo2k() throws Exception
    {
        ((OBPath)objectDict.get("cover")).sizeToBoundingBoxIncludingStroke();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-35,0.5f,"DEMO",0,0.3f);
        waitForSecs(0.3f);
        showFirstPart();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.9f,objectDict.get("box").frame()),-30,0.5f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.9f,objectDict.get("box").frame()),-20,0.5f,true);
        waitForSecs(0.3f);
        showSecondPart();
        waitForSecs(0.3f);
        OBGroup equ = (OBGroup)objectDict.get("equation");
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.8f,OC_Numberlines_Additions.getLabelForEquation(5,equ).getWorldFrame()), -35, 0.5f, "DEMO", 2, 0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-15,0.5f,"DEMO",3,0.3f);
        OBGroup button = (OBGroup)objectDict.get("button");
        for(int i= 0; i < 4; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button").frame()),-15,0.2f,true);
            button.objectDict.get("highlight").show();
            showCurrentObject();
            currentNum++;
            waitSFX();
            button.objectDict.get("highlight").hide();
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-15,0.2f,true);
            playAudioScene("DEMO",4+i,true);
            waitForSecs(0.3f);
        }
        buttonSet(0);
        objectDict.get("button_arrow").show();
        playSfxAudio("numpopon",true);
        moveScenePointer(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("button_arrow").frame()),-35,0.5f,"DEMO",8,0.4f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button_arrow").frame()),-35,0.2f,true);
        buttonSet(1);
        playAudio("correct");
        waitAudio();
        movePointerToPoint(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("button_arrow").frame()),-35,0.2f,true);
        waitForSecs(0.3f);
        lockScreen();
        OC_Numberlines_Additions.getLabelForEquation(3,equ).setOpacity(1);
        objectDict.get("cover").hide();
        buttonSet(2);

        unlockScreen();
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("box").frame()),-30,0.5f,"DEMO",9,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,1f,objectDict.get("equation").frame()),-35,0.5f,true);
        OC_Numberlines_Additions.colourEquation(equ, 1, 5, Color.RED, this);
        playAudioScene("DEMO",10,true);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(equ, 1, 5, equationColour, this);
        waitForSecs(0.7f);
        clearScene();
        thePointer.hide();
        waitForSecs(1f);
        nextScene();
    }

    public void demo2l() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showFirstPart();
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        showSecondPart();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBGroup equ = (OBGroup)objectDict.get("equation");
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.8f,OC_Numberlines_Additions.getLabelForEquation(5,equ).getWorldFrame()), -35, 0.5f, "DEMO", 2, 0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-25,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        buttonSet(0);
        startScene();
    }

    public void demoPhase2l() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("box").frame()),-30,0.5f,"DEMO2",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("button_arrow").frame()),-25,0.5f,"DEMO2",1,0.5f);
        thePointer.hide();
        buttonSet(0);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demoFin2l() throws Exception
    {
        demoFinPoint();
    }

    public void demo2m() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showFirstPart();
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        showSecondPart();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBGroup equ = (OBGroup)objectDict.get("equation");
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.8f,OC_Numberlines_Additions.getLabelForEquation(5,equ).getWorldFrame()), -35, 0.5f, "DEMO", 2, 0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("button_arrow").frame()),-25,0.5f,"DEMO",3,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("box").frame()),-25,0.5f,"DEMO",4,0.5f);
        thePointer.hide();
        buttonSet(0);
        startScene();

    }

    public void demoFin2m() throws Exception
    {
        demoFinPoint();
    }

    public void demoFin2o() throws Exception
    {
        demoFinPoint();
    }

    public void demoFin2s() throws Exception
    {
        demoFinPoint();
    }

    public void demo3a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        waitForSecs(0.3f);
        showFirstPart();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.4f,0.9f,objectDict.get("box").frame()),-30,0.5f,"DEMO",1,0.3f);
        objectDict.get("button").show();
        playSfxAudio("buttonpopon",true);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-15,0.5f,"DEMO",2,0.3f);
        OBGroup button = (OBGroup)objectDict.get("button");
        for(int i= 0; i < 2; i++)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,objectDict.get("button").frame()),-15,0.2f,true);
            button.objectDict.get("highlight").show();
            showCurrentObject();
            currentNum++;
            waitSFX();
            button.objectDict.get("highlight").hide();
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("button").frame()),-15,0.2f,true);
            waitForSecs(0.3f);

        }
        moveScenePointer(OB_Maths.locationForRect(0.2f,0.9f,objectDict.get("box").frame()),-30,0.5f,"DEMO",3,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.9f,objectDict.get("box").frame()),-25,0.5f,true);
        waitForSecs(0.3f);
        showNumbers();
        waitForSecs(0.3f);
        playAudioScene("DEMO",4,false);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,targetNums.get(0).getWorldFrame()),-15,0.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,targetNums.get(0).getWorldFrame()),-15,0.2f,true);
        lockScreen();
        targetNums.get(1).setColour(Color.RED);

        OBGroup equation = (OBGroup)objectDict.get("equation");
        OC_Numberlines_Additions.getLabelForEquation(4, equation).show();
        OC_Numberlines_Additions.getLabelForEquation(5, equation).show();

        unlockScreen();
        playSfxAudio("equcomplete",true);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.2f,targetNums.get(0).getWorldFrame()),-15,0.2f,true);
        targetNums.get(0).setColour(Color.BLACK);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        for(int i=1; i<6; i++)
        {
            OC_Numberlines_Additions.colourEquation(equation, i, i, Color.RED, this);
            playAudioScene("DEMO",4+i,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation(equation, i, i,equationColour, this);
        }
        waitForSecs(1.2f);
        clearScene();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demo3b() throws Exception
    {
        demoShortStart();
    }

    public void demoFin3b() throws Exception
    {
        OBGroup equation = (OBGroup)objectDict.get("equation");
        for(int i=1; i<6; i++)
        {
            OC_Numberlines_Additions.colourEquation(equation, i, i, Color.RED, this);
            playAudioScene("FINAL2",i-1,true);
            waitForSecs(0.3f);
            OC_Numberlines_Additions.colourEquation(equation, i, i, equationColour, this);
        }

    }

    public void demo3m() throws Exception
    {
        demo2k();
    }

    public void demo3n() throws Exception
    {
        demo2l();
    }

    public void demoPhase3n() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("button_arrow").frame()),-25,0.5f,"DEMO2",0,0.5f);
        thePointer.hide();
        buttonSet(0);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demoFin3n() throws Exception
    {
        demoFinPoint();
    }

    public void demo3o() throws Exception
    {
        demo2m();
    }

    public void demo3p() throws Exception
    {
        showFirstPart();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBGroup equation = (OBGroup)objectDict.get("equation");
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.8f,OC_Numberlines_Additions.getLabelForEquation(1,equation).getWorldFrame()), -35, 0.5f, "DEMO", 0, 0.3f);
        showSecondPart();
        waitForSecs(0.5f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.8f,OC_Numberlines_Additions.getLabelForEquation(5,equation).getWorldFrame()), -35, 0.5f, "DEMO", 1, 0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,1.1f,objectDict.get("button_arrow").frame()),-25,0.5f,"DEMO",2,0.3f);
        thePointer.hide();
        buttonSet(0);
        startScene();
    }

    public void demoFin3o() throws Exception
    {
        demoFinPoint();
    }

    public void demoFin3p() throws Exception
    {
        demoFinPoint();
    }

    public void demoFin3q() throws Exception
    {
        demoFinPoint();
    }

    public void demoFin3y() throws Exception
    {
        demoFinPoint();
    }

}




