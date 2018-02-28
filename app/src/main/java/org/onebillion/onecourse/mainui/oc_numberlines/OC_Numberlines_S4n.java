package org.onebillion.onecourse.mainui.oc_numberlines;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.Matrix;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 11/05/2017.
 */

public class OC_Numberlines_S4n extends OC_SectionController
{
    int eqColour, numColour, divColour, mainColour;
    int currentJump, targetNum, startNum, currentTarget;
    final int TARGET_DIV = 1, TARGET_NUM = 2, TARGET_BOX = 3;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        String startEvent = parameters.get("start");
        loadEvent(String.format("master2%s",startEvent));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);



        numColour = OBUtils.colorFromRGBString(eventAttributes.get("numcolour"));
        divColour = OBUtils.colorFromRGBString(eventAttributes.get("divcolour"));
        mainColour = OBUtils.colorFromRGBString(eventAttributes.get("maincolour"));
        eqColour = OBUtils.colorFromRGBString(eventAttributes.get("eqcolour"));
        setSceneXX(currentEvent());

        if(startEvent.startsWith("a"))
        {
            objectDict.get("mainline").hide();
            hideControls("divline_.*");
        }

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                waitForSecs(0.5f);
                performSel("demo", currentEvent());
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(eventAttributes.get("target").equals("divline"))
        {
            if(eventAttributes.get("equation") != null)
            {
                deleteControls("equation");
                OC_Numberlines_Additions.loadEquation(eventAttributes.get("equation"),"equation",objectDict.get("eqbox"),eqColour,false,this);
                String[] eqParts = eventAttributes.get("equation").split(" ");
                currentJump = startNum = OBUtils.getIntValue(eqParts[0]);
                targetNum = OBUtils.getIntValue(eqParts[eqParts.length-1]);
                redrawScreen(0);
                OC_Numberlines_Additions.hideEquation((OBGroup)objectDict.get("equation"),this);
            }
            currentTarget = TARGET_DIV;
        }
        else if(eventAttributes.get("target").equals("box"))
        {
            currentTarget = TARGET_BOX;
            for(OBControl box : filterControls("eqbox_.*"))
            {
                String name = (String)box.attributes().get("id");
                box.show();
                box.setZPosition(0.5f);
                OBPath boxPath = (OBPath)box;
                boxPath.setLineWidth(boxPath.lineWidth()*2.0f);
                boxPath.sizeToBoundingBoxIncludingStroke();
                boxPath.setLineWidth(boxPath.lineWidth()/2.0f);
                OC_Numberlines_Additions.loadEquation((String)box.attributes().get("equation"),name,box, Color.BLACK,true,this);
                objectDict.get(name).setProperty("correct",false);
                if(OBUtils.getBooleanValue((String)box.attributes().get("correct")))
                {
                    String[] eqParts = ((String)box.attributes().get("equation")).split(" ");
                    startNum = OBUtils.getIntValue(eqParts[0]);
                    targetNum = OBUtils.getIntValue(eqParts[eqParts.length-1]);
                    objectDict.get(name).setProperty("correct",true);
                    OBControl boxCopy = box.copy();
                    boxCopy.setScale(2);
                    boxCopy.setBorderColor(Color.RED);
                    OC_Numberlines_Additions.loadEquation((String)box.attributes().get("equation"),"animbox",boxCopy, Color.BLACK,true,this);
                    objectDict.get("animbox").setPosition(OB_Maths.locationForRect(0.5f,0.35f,this.bounds()));
                    objectDict.get("animbox").hide();
                }
                objectDict.get(name).hide();
            }
            redrawScreen(targetNum);
        }
    }

    public void doMainXX() throws Exception
    {
        if(currentTarget == TARGET_DIV)
            demoEquation(0);
        if(currentTarget == TARGET_BOX)
            demoBoxes();
        startScene();
    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK && currentTarget == TARGET_NUM)
        {
            final OBControl cont = finger(0,1,filterControls("num_.*") ,pt);
            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkNum((OBLabel)cont);
                    }
                });

            }
        }
        if(status() == STATUS_AWAITING_CLICK && currentTarget == TARGET_DIV)
        {
            OBControl targ = finger(0,2,filterControls("divline_.*") ,pt);
            if(targ == null)
                targ = finger(0,1,filterControls("num_.*") ,pt);

            if(targ != null)
            {
                final OBControl cont = targ;
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDivLine(cont);
                    }
                });

            }
        }
        else if(status() == STATUS_AWAITING_CLICK && currentTarget == TARGET_BOX)
        {
            final OBControl cont = finger(0,1,filterControls("eqbox_.*") ,pt);
            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkBox(cont);
                    }
                });

            }
        }
    }


    public void checkNum(OBLabel cont) throws Exception
    {
        playAudio(null);
        cont.setColour(Color.RED);
        if((int)cont.settings.get("num_value")  == targetNum)
        {
            playAudio(null);
            gotItRightBigTick(true);
            demoFinEquation();
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            cont.setColour(numColour);
            playAudioQueuedScene("INCORRECT4",300,false);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void checkDivLine(OBControl cont) throws Exception
    {
        playAudio(null);
        if((int)cont.settings.get("num_value")  == currentJump)
        {
            playSfxAudio(currentJump<targetNum ? "arc_grow2" : "arc_reduce2",false);
            OC_Numberlines_Additions.animateCurve(currentJump,currentJump<targetNum,0.5f,false,true,this);
            playSFX(null);
            if(currentJump<targetNum)
            {
                currentJump++;
            }
            else
            {
                currentJump--;
            }
            gotItRight();
            if(currentJump == targetNum)
            {
                waitForSecs(0.3f);
                displayTick();
                waitForSecs(0.5f);
                currentTarget = TARGET_NUM;
                doSpecialSceneAudio(4,setStatus(STATUS_AWAITING_CLICK));
            }
            else
            {
                doSpecialSceneAudio(Math.abs(currentJump - startNum)+1,setStatus(STATUS_AWAITING_CLICK));
            }
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if(time == statusTime)
            {
                if(currentJump != startNum)
                {
                    String audioName = String.format("INCORRECT%d", Math.abs(currentJump-startNum)+1);
                    if(getAudioForScene(currentEvent(),audioName) != null)
                    {
                        playAudioQueuedScene(audioName,300,false);
                    }
                    else
                    {
                        playAudioQueuedScene("INCORRECT",300,false);
                    }
                }
                else
                {
                    playAudioQueuedScene("INCORRECT",300,false);
                }
            }
        }
    }

    public void checkBox(OBControl cont) throws Exception
    {
        playAudio(null);
        hiliteBox((OBGroup)cont);
        if((boolean)cont.propertyValue("correct"))
        {
            playAudio(null);
            gotItRightBigTick(true);
            waitForSecs(0.5f);
            demoFinBox((OBGroup)cont);
            if(currentEvent() != events.get(events.size()-1))
            {
                resetScreen();
                playSfxAudio("pop_off",true);
            }
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            unhiliteBox((OBGroup)cont);
            playAudioQueuedScene("INCORRECT",300,false);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void startScene() throws Exception
    {
        if(currentTarget == TARGET_DIV)
            doSpecialSceneAudio(1,setStatus(STATUS_AWAITING_CLICK));
        else
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void  doSpecialSceneAudio(int num,long statusTime) throws Exception
    {
        String repeat = " ", prompt = " ";
        if(num > 1)
        {
            if(num > 3 && currentTarget == TARGET_DIV)
                num = 3;
            String repeatAudioCat = String.format("PROMPT%d.REPEAT",num);
            if(getAudioForScene(currentEvent(),repeatAudioCat) != null)
            {
                repeat = repeatAudioCat;
                prompt = String.format("PROMPT%d", num);
            }
            else
            {
                repeat = "PROMPT.REPEAT";
            }
        }
        else
        {
            repeat = "PROMPT.REPEAT";
            prompt = "PROMPT";
        }
        setReplayAudio(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),repeat),300));
        if(getAudioForScene(currentEvent(),prompt) != null)
        {
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),prompt),300),true);

        }
        reprompt(statusTime, OBUtils.insertAudioInterval(getAudioForScene(currentEvent(),repeat),300),5);
    }

    public void showNums() throws Exception
    {
        if(startNum < targetNum)
        {
            int index = 1;
            for (int i=startNum+1; i<startNum+9; i++)
            {
                objectDict.get(String.format("num_%d",i)).show();
                playSfxAudio(String.format("scale%d",index),true);
                index++;
            }
        }
        else
        {
            int index = 7;
            for (int i=startNum-1; i>startNum-9; i--)
            {
                objectDict.get(String.format("num_%d",i)).show();
                playSfxAudio(String.format("scale%d",index),true);
                index--;
            }
        }
    }

    public void hideCnums()
    {
        lockScreen();
        for(OBControl control : filterControls("cnum_.*"))
        {
            control.hide();
            ((OBLabel)control).setColour(numColour);
        }
        unlockScreen();
    }

    public void resetScreen()
    {
        lockScreen();
        deleteControls("num_.*");
        deleteControls("linelabel_.*");
        deleteControls("curveline_.*");
        deleteControls("equation");
        deleteControls("eqbox_.*");
        deleteControls("animbox");
        for(OBControl control : filterControls("divline_.*"))
        {
            ((OBPath)control).setStrokeStart(0);

        }
        objectDict.get("arrowhead").hide();

        unlockScreen();
    }

    public void redrawScreen(int jumpNum)
    {
        lockScreen();
        if(objectDict.get("mainline") != null)
        {
            deleteControls("mainline");
            deleteControls("divline_.*");
        }
        OC_Numberlines_Additions.drawNumberLine(objectDict.get("numberline"),9,startNum<targetNum ? startNum : startNum-8,mainColour,divColour,numColour,this);
        hideControls("num_.*");
        if(jumpNum > 0)
        {
            if(jumpNum > startNum)
            {
                for(int i=startNum; i<jumpNum; i++)
                {
                    OC_Numberlines_Additions.drawCurveLine(i,true,true,this);
                }
            }
            else
            {
                for(int i=startNum; i>jumpNum; i--)
                {
                    OC_Numberlines_Additions.drawCurveLine(i,false,true,this);
                }
            }
            OBPath lastCurve = (OBPath)objectDict.get(String.format("curveline_%d",jumpNum > startNum ? jumpNum-1 : jumpNum +1));
            lastCurve.sizeToBox(new RectF(this.bounds()));
            objectDict.get("arrowhead").setPosition(lastCurve.lastPoint());
            objectDict.get("arrowhead").setRotation((float)Math.toRadians(180));
        }
        unlockScreen();
    }

    public void showFullNumberline() throws Exception
    {
        lockScreen();
        objectDict.get("mainline").show();
        objectDict.get("arrowhead").show();
        showControls("divline_.*");
        showControls("num_.*");
        showControls("curveline_.*");
        showControls("linelabel_.*");
        if(startNum<targetNum)
            for(int i = startNum; i<=targetNum; i++)
                OC_Numberlines_Additions.shortenDiv(i,this);
        else
            for(int i = startNum; i>=targetNum; i--)
                OC_Numberlines_Additions.shortenDiv(i,this);
        unlockScreen();
        playSfxAudio("pop_on",true);
    }

    public void showBoxes() throws Exception
    {
        int index = 1;
        OBControl cont = objectDict.get(String.format("eqbox_%d",index));
        while(cont != null)
        {
            cont.show();
            playSfxAudio("sum_appear",true);
            cont = objectDict.get(String.format("eqbox_%d",++index));
        }
    }

    public void hiliteBox(OBGroup cont)
    {
        lockScreen();
        OBPath box = (OBPath)cont.objectDict.get("box");
        box.setStrokeColor(Color.RED);
        box.setLineWidth(box.lineWidth() * 2.0f);
        unlockScreen();
    }

    public void unhiliteBox(OBGroup cont)
    {
        lockScreen();
        OBPath box = (OBPath)cont.objectDict.get("box");
        box.setStrokeColor(Color.BLACK);
        box.setLineWidth(box.lineWidth() / 2.0f);
        unlockScreen();
    }

    public void colourLineLabel(int num, int colour, boolean equation)
    {
        lockScreen();
        OBGroup group = (OBGroup)objectDict.get(String.format("linelabel_%d",num));
        ((OBLabel)group.objectDict.get("eqLabel")).setColour(colour);
        ((OBLabel)group.objectDict.get("numLabel")).setColour(colour);
        if(equation)
            OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("animbox"),3,3,colour,this);
        unlockScreen();
    }

    public void colourNum(int num, int part, int colour)
    {
        lockScreen();
        ((OBLabel)objectDict.get(String.format("num_%d",num))).setColour(colour);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("animbox"),part,part,colour,this);
        unlockScreen();
    }

    public void pointerPointEquation(OBGroup equation, int from, int to, List<String> audio, float duration, float wait) throws Exception
    {
        PointF point = OB_Maths.locationForRect(0.6f,1f,equation.objectDict.get(String.format("part%d",to)).getWorldFrame());
        point.y = equation.bottom() + equation.height()*0.2f;
        movePointerToPoint(point,-20,duration,true);
        OC_Numberlines_Additions.colourEquation(equation,from,to,Color.BLACK,this);
        if(audio != null)
            playAudioQueued(OBUtils.insertAudioInterval(audio,300),true);
        if(wait>0)
            waitForSecs(wait);
        OC_Numberlines_Additions.colourEquation(equation,from,to,eqColour,this);
    }

    public void demoEquation(int audioStart) throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,4,"numbers_on",this);
        waitForSecs(0.5f);
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.4f,1.4f,objectDict.get("equation").frame()),-20,0.5f,"DEMO",audioStart,0.3f);
        for(int i = 1; i<=3; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"),i,i,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(audioStart+i)),0.3f,0f);
        }
        waitForSecs(0.5f);
        if(getAudioForScene(currentEvent(),"DEMO").size() < 7)
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"),1,1,null,0.3f,0f);
            audioStart--;
        }
        else
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"),1,1,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(audioStart+4)),0.3f,0.5f);
        }
        playAudioScene("DEMO",audioStart+5,false);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.2f,objectDict.get(String.format("num_%d",startNum)).frame())
                ,startNum<targetNum ? -35 : -15,1f,true);
        waitForAudio();
        waitForSecs(0.3f);
        objectDict.get(String.format("num_%d",startNum)).show();
        playSfxAudio(startNum<targetNum ? "scale0" : "scale8",true);
        waitForSecs(0.3f);
        thePointer.hide();
        showNums();
        loadPointer(POINTER_LEFT);
        pointerPointEquation((OBGroup)objectDict.get("equation"),2,3,Arrays.asList(getAudioForScene(currentEvent(),"DEMO").get(audioStart+6)),0.6f,0f);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoFinEquation() throws Exception
    {
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),5,5,"numbers_on",this);
        waitForSecs(0.5f);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        for(int i = 1; i<=5; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("equation"),i,i,Arrays.asList(getAudioForScene(currentEvent(),"FINAL").get(i)), i==1 ? 0.8f : 0.3f,0f);
        }
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        if(currentEvent() != events.get(events.size()-1))
            resetScreen();
        waitForSecs(0.5f);
    }

    public void demo4n() throws Exception
    {
        playSfxAudio("number_line_on",false);
        OC_Numberlines_Additions.animateNumberLineShow(1,this);
        waitSFX();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.75f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        demoEquation(1);
        startScene();
    }

    public void demo4s() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.95f,this.bounds()),-20,0.5f,"DEMO",1,0.3f);
        showFullNumberline();

        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("num_14").frame()),-40,0.5f,"DEMO",2,0.3f);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        for(int i=4; i<8; i++)
        {
            moveScenePointer(OB_Maths.locationForRect(0.6f,2f,objectDict.get(String.format("linelabel_%d",i+10)).frame()),-30,0.35f,"DEMO",i,0.1f);
        }
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("num_18").frame()),-40,0.5f,"DEMO",8,0.3f);
        waitForSecs(0.5f);
        showBoxes();
        waitForSecs(0.5f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()),-20,0.5f,"DEMO",9,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("eqbox_1").frame()),-20,0.5f,true);
        waitForSecs(0.15f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("eqbox_2").frame()),-15,0.5f,true);
        waitForSecs(0.3f);
        thePointer.hide();
        startScene();
    }

    public void demoBoxes() throws Exception
    {
        showFullNumberline();
        waitForSecs(0.5f);
        playAudioScene("DEMO",0,true);
        showBoxes();
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
    }

    public void demoFinBox(OBGroup cont) throws Exception
    {
        OBControl box = objectDict.get("animbox");
        List<OBAnim> arr = new ArrayList<>();
        for(OBControl control: filterControls("eqbox_.*"))
        {
            if(control != cont)
                arr.add(OBAnim.opacityAnim(0,control));
        }
        OBAnimationGroup.runAnims(arr,0.5,true,OBAnim.ANIM_LINEAR,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(box.position(),cont),
                OBAnim.scaleAnim(2f,cont),
                OBAnim.propertyAnim("lineWidth",((OBPath)cont.objectDict.get("box")).lineWidth()/2.0f,cont.objectDict.get("box"))),
                0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        box.show();
        cont.hide();
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get(String.format("num_%d",startNum)).frame()),startNum<targetNum ? -40:-15,0.5f,true);
        colourNum(startNum,1,Color.RED);
        playAudioScene("FINAL",1,true);
        colourNum(startNum,1,Color.BLACK);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("animbox"),2,2,Color.RED,this);
        playAudioScene("FINAL",2,true);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("animbox"),2,2,Color.BLACK,this);
        int index = 3;
        if(startNum<targetNum)
        {
            for(int i=startNum; i<targetNum; i++)
            {
                movePointerToPoint(OB_Maths.locationForRect(0.6f,2f,objectDict.get(String.format("linelabel_%d",i)).frame()),
                        startNum<targetNum ? -30:-22,0.35f,true);
                colourLineLabel(i,Color.RED, i == (targetNum-1) ? true : false);
                playAudioScene("FINAL",index++,true);
                if(i == (targetNum-1))
                    waitForSecs(0.3f);
                colourLineLabel(i,Color.BLACK, i == (targetNum-1) ? true : false);
            }
        }
        else
        {
            for(int i=startNum; i>targetNum; i--)
            {
                movePointerToPoint(OB_Maths.locationForRect(0.6f,2f,objectDict.get(String.format("linelabel_%d",i)).frame()),
                        startNum<targetNum ? -30:-22,0.35f,true);
                colourLineLabel(i,Color.RED, i == (targetNum+1) ? true : false);
                playAudioScene("FINAL",index++,true);
                if(i == (targetNum+1))
                    waitForSecs(0.3f);
                colourLineLabel(i,Color.BLACK, i == (targetNum+1) ? true : false);
            }
        }
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("animbox"),4,4,Color.RED,this);
        playAudioScene("FINAL",index++,true);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("animbox"),4,4,Color.BLACK,this);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get(String.format("num_%d",targetNum)).frame()),
                startNum<targetNum ? -40:-15,0.5f,true);
        colourNum(targetNum,5,Color.RED);
        playAudioScene("FINAL",index++,true);
        colourNum(targetNum,5,Color.BLACK);
        waitForSecs(1f);
        thePointer.hide();
    }

    public void demo5r() throws Exception
    {
        playSfxAudio("number_line_on",false);
        OC_Numberlines_Additions.animateNumberLineShow(1,this);
        waitSFX();
        demoEquation(0);
        startScene();
    }

    public void demo5w() throws Exception
    {
        loadPointer(POINTER_LEFT);

        moveScenePointer(OB_Maths.locationForRect(0.5f,0.95f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        showFullNumberline();
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("num_10").frame()),-15,0.5f,"DEMO",1,0.3f);
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        for(int i=3; i<6; i++)
        {
            moveScenePointer(OB_Maths.locationForRect(0.6f,2f,objectDict.get(String.format("linelabel_%d",13-i)).frame()),-25,0.35f,"DEMO",i,0.1f);
        }
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("num_7").frame()),-20,0.5f,"DEMO",6,0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        showBoxes();
        waitForSecs(0.3f);
        startScene();
    }
}
