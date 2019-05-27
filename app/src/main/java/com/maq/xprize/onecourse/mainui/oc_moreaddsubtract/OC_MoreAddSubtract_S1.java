package com.maq.xprize.onecourse.mainui.oc_moreaddsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.Arrays;

/**
 * Created by michal on 16/03/2017.
 */

public class OC_MoreAddSubtract_S1 extends OC_SectionController
{
    int textcolour1, textcolour2, hilitecolour, buttoncolour;
    int currentEquation;
    String currentTargetType;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master1");
        textcolour1 = OBUtils.colorFromRGBString(eventAttributes.get("textcolour1"));
        textcolour2 = OBUtils.colorFromRGBString(eventAttributes.get("textcolour2"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        buttoncolour = OBUtils.colorFromRGBString(eventAttributes.get("buttoncolour"));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        loadEquations();
        loadNumbers();
        setSceneXX(currentEvent());
        objectDict.get("targetbox").setZPosition(5);

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
        currentTargetType = eventAttributes.get("target");
        if(currentTargetType == null)
            currentTargetType = "";
        if(OBUtils.getBooleanValue(eventAttributes.get("load")))
        {
            loadEquations();
            if(currentTargetType.equals("button"))
            {
                for(OBControl obj : filterControls("obj_.*"))
                {
                    OBPath path = (OBPath)obj;
                    path.sizeToBoundingBoxIncludingStroke();
                }
            }
        }

        if(currentTargetType.equals("num"))
        {
            int part = OBUtils.getIntValue(eventAttributes.get("part"));
            int correct = OBUtils.getIntValue(eventAttributes.get("correct"));
            if(part == 3)
            {
                currentEquation =correct;
            }
            else
            {
                currentEquation =10 - correct;
            }
        }
    }


    public void doMainXX() throws Exception
    {
        if(currentTargetType.equals("num"))
        {
            showEquationWithBox();
        }
        else if(currentTargetType.equals("button"))
        {
            showButton();
        }
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            if(currentTargetType.equals("row"))
            {
                final OBControl row = finger(0,1,filterControls("row_.*"),pt);
                if(row != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkRow((OBGroup)row);
                        }
                    });
                }
            }
            else if(currentTargetType.equals("num"))
            {
                final OBControl box = finger(-1,-1,filterControls("num_.*"),pt);
                if(box != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkNumBox((OBGroup)box);
                        }
                    });
                }
            }
            else if(currentTargetType.equals("button"))
            {
                if(finger(0,1,Arrays.asList(objectDict.get("button")),pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkButton((OBGroup)objectDict.get("button"));
                        }
                    });
                }
            }
        }
    }

    public void checkRow(OBGroup row) throws Exception
    {
        row.highlight();
        if(row == objectDict.get(String.format("row_%s", eventAttributes.get("correct"))))
        {
            boolean showTick = eventAttributes.get("last") != null;
            gotItRightBigTick(showTick);
            if(!showTick)
                waitSFX();
            row.lowlight();
            waitForSecs(0.3f);
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            row.lowlight();
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void checkNumBox(OBGroup box) throws Exception
    {
        box.objectDict.get("box").setBackgroundColor(hilitecolour);
        if(box == objectDict.get(String.format("num_%s", eventAttributes.get("correct"))))
        {
            gotItRightBigTick(false);
            waitAudio();
            waitForSecs(0.2f);
            lockScreen();
            objectDict.get("targetbox").hide();
            box.objectDict.get("box").setBackgroundColor(Color.WHITE);
            playSfxAudio("inbox",false);

            unlockScreen();
            waitSFX();
            waitForSecs(0.3f);
            if(!performSel("demoFin",currentEvent()))
            {
                OBGroup equ = (OBGroup)objectDict.get(String.format("box_%d",currentEquation));
                OC_Numberlines_Additions.colourEquation(equ, 1, 5, Color.RED, this);
                playAudioQueuedScene("FINAL",0.3f,true);
                waitForSecs(0.3f);
                resetEquation(equ);
            }
            waitForSecs(0.3f);
            if(eventAttributes.get("reset") != null)
            {
                displayTick();
                waitForSecs(0.5f);
                lockScreen();
                deleteControls("box_.*");
                deleteControls("row_.*");
                if(eventAttributes.get("reset").equals("full"))
                    deleteControls("num_.*");

                unlockScreen();
                waitForSecs(0.3f);

            }
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            box.objectDict.get("box").setBackgroundColor(Color.WHITE);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void checkButton(OBGroup button) throws Exception
    {
        OBGroup equation = (OBGroup)objectDict.get(String.format("box_%s",eventAttributes.get("correct")));
        lockScreen();
        button.objectDict.get("background").setFillColor(buttoncolour);
        equation.show();
        playSfxAudio("repeatequon",false);
        unlockScreen();
        waitSFX();

        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(equation, 1, 5, Color.RED, this);
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(0.3f);
        resetEquation(equation);
        waitForSecs(1f);

        lockScreen();
        button.hide();
        equation.hide();
        for(int i=1; i<11; i++)
            objectDict.get(String.format("obj_%s_%d",eventAttributes.get("correct"), i)).setOpacity(0.35f);
        unlockScreen();

        nextScene();
    }

    public void resetEquation(OBGroup equ)
    {
        lockScreen();
        OC_Numberlines_Additions.colourEquation(equ, 1, 5, Color.BLACK, this);
        OC_Numberlines_Additions.colourEquation(equ, 1, 1, textcolour1, this);
        OC_Numberlines_Additions.colourEquation(equ, 3, 3, textcolour2, this);
        unlockScreen();
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void loadEquations()
    {
        lockScreen();
        for(OBControl control : filterControls("box_.*"))
        {
            String name = (String)control.attributes().get("id");
            OC_Numberlines_Additions.loadEquation((String)control.attributes().get("equation"), name, control, Color.BLACK, true, 0,
                    OBUtils.getBooleanValue(eventAttributes.get("large_equation"))? 1.2f : 1.0f, this);
            OBGroup box = (OBGroup)objectDict.get(name);
            OC_Numberlines_Additions.colourEquation(box, 1, 1, textcolour1, this);
            OC_Numberlines_Additions.colourEquation(box, 3, 3, textcolour2, this);
            box.hide();
        }
        unlockScreen();
    }

    public void loadNumbers()
    {
        OBControl numbox = objectDict.get("numbox");
        for(int i = 0; i<11; i++)
        {
            OBControl box = new OBControl();
            box.setFrame(new RectF(0, 0, numbox.height(), numbox.height()));
            box.setBackgroundColor(Color.WHITE);
            box.setBorderColor(Color.BLACK);
            box.setBorderWidth(applyGraphicScale(2));
            box.setPosition(OB_Maths.locationForRect(1/11.0f * i,0.5f,numbox.frame()));
            box.setLeft ( numbox.position().x - (5.5f-i)*(box.width() - box.borderWidth));
            OBLabel label = new OBLabel(String.format("%d",i),OBUtils.standardTypeFace(),105.0f*numbox.height()/130.0f);
            label.setColour(Color.BLACK);
            label.setPosition(OBMisc.copyPoint(box.position()));
            OBGroup group = new OBGroup(Arrays.asList(box,label));
            group.objectDict.put("label",label);
            group.objectDict.put("box",box);
            attachControl(group);
            objectDict.put(String.format("num_%d",i),group);
            group.hide();
        }
    }


    public void showEquationWithBox() throws Exception
    {
        OBGroup group = (OBGroup)objectDict.get(String.format("box_%d",currentEquation));
        OBControl box = objectDict.get("targetbox");
        box.setPosition(group.objectDict.get(String.format("part%s", eventAttributes.get("part"))).getWorldPosition());

        lockScreen();
        group.show();
        box.show();
        playSfxAudio("equon",false);
        unlockScreen();
        waitSFX();

    }


    public void showButton() throws Exception
    {
        OBGroup group = (OBGroup)objectDict.get("button");
        if(group.hidden())
        {
            group.objectDict.get("background").setFillColor(Color.RED);
            PointF loc = OBMisc.copyPoint(objectDict.get(String.format("obj_%s_1", eventAttributes.get("correct"))).position());
            PointF oldloc = OBMisc.copyPoint(group.position());
            oldloc.y = loc.y;
            group.setPosition(oldloc);
            group.show();
            playSfxAudio("rowon",true);
        }
    }

    public void pointerPointEquation(OBGroup equation,int at, String audio, float duration) throws Exception
    {
        PointF point = OB_Maths.locationForRect(0.7f,1f,equation.objectDict.get(String.format("part%d",at)).getWorldFrame());
        point.y = equation.bottom() + equation.height()*0.2f;
        movePointerToPoint(point,-30,duration,true);
        OC_Numberlines_Additions.colourEquation(equation, at, at, Color.RED, this);
        playAudio(audio);
        waitAudio();
        waitForSecs(0.3f);
        if(at != 1 && at!= 3)
            OC_Numberlines_Additions.colourEquation(equation, at, at, Color.BLACK, this);
        else
            OC_Numberlines_Additions.colourEquation(equation, at, at, at==1?textcolour1:textcolour2, this);
    }

    public void demo1a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        demoButtons();
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.5f,true);
        for(int i=0; i<4; i++)
        {
            objectDict.get(String.format("row_%d",i)).show();
            playSfxAudio("rowon",true);
            waitForSecs(0.1f);
        }
        moveScenePointer(OB_Maths.locationForRect(1.05f,1f,objectDict.get("row_2").frame()),-30,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.05f,0f,objectDict.get("row_0").frame()),-20,0.5f,true);
        playAudioScene("DEMO",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1.05f,1f,objectDict.get("row_3").frame()),-35,1.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.05f,0.5f,objectDict.get("row_0").frame()),-20,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

    public void demo1f() throws Exception
    {
        loadPointer(POINTER_LEFT);
        waitForSecs(0.2f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        showEquationWithBox();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("targetbox").frame()),-30,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,3.1f,objectDict.get("targetbox").frame()),-35,0.5f,"DEMO",2,0.3f);
        lockScreen();
        for(int i=0;
            i<11;
            i++)
        {
            objectDict.get(String.format("num_%d",i)).show();

        }
        playSfxAudio("gridon",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.95f,1f,objectDict.get("row_0").frame()),-30,0.5f,"DEMO",3,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.75f,1.2f,objectDict.get("num_0").frame()),-15,0.5f,"DEMO",4,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.76f,0.6f,objectDict.get("num_0").frame()),-15,0.2f,true);
        OBGroup numbox = (OBGroup)objectDict.get("num_0");
        numbox.objectDict.get("box").setBackgroundColor(hilitecolour);
        objectDict.get("targetbox").hide();
        playSfxAudio("inbox",true);
        waitForSecs(0.2f);
        numbox.objectDict.get("box").setBackgroundColor(Color.WHITE);
        movePointerToPoint(OB_Maths.locationForRect(0.75f,1.2f,objectDict.get("num_0").frame()),-15,0.2f,true);
        waitForSecs(0.4f);
        for(int i=1; i<6; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("box_0"), i, getAudioForSceneIndex(currentEvent(),"DEMO",4+i),i==1?0.5f:0.3f);
        }
        waitForSecs(0.3f);
        nextScene();

    }
    public void demo1g() throws Exception
    {
        moveScenePointer(OB_Maths.locationForRect(1f,1.2f,objectDict.get("box_0").frame()),-30,0.5f,"DEMO",0,0.3f);
        waitForSecs(0.3f);
        showEquationWithBox();
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.2f,objectDict.get("box_1").frame()),-30,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();

    }

    public void demoFin1g() throws Exception
    {
        loadPointer(POINTER_LEFT);
        for(int i=1; i<6; i++)
        {
            pointerPointEquation((OBGroup)objectDict.get("box_1"), i, getAudioForSceneIndex(currentEvent(),"FINAL",i-1), i==1?0.7f:0.3f);
        }
        thePointer.hide();

    }

    public void demo1j() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.2f);
        for(int i=4; i<8; i++)
        {
            objectDict.get(String.format("row_%d",i)).show();
            playSfxAudio("rowon",true);
            waitForSecs(0.1f);
        }
        waitForSecs(0.4f);
        showEquationWithBox();
        waitForSecs(0.1f);
        startScene();
    }

    public void demo1n() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.2f);
        for(int i=8; i<11; i++)
        {
            objectDict.get(String.format("row_%d",i)).show();
            playSfxAudio("rowon",true);
            waitForSecs(0.1f);
        }
        waitForSecs(0.4f);
        showEquationWithBox();
        waitForSecs(0.1f);
        startScene();

    }

    public void demo1q() throws Exception
    {
        for(int i=0; i<11; i++)
        {
            lockScreen();
            for(int j=1; j<11; j++)
            {
                objectDict.get(String.format("obj_%d_%d",i,j)).show();
            }
            playSfxAudio(String.format("note_%d",i),false);
            unlockScreen();
            waitForSecs(0.45f);
        }
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.75f,objectDict.get("workrect").frame()),-25,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.4f,0f,objectDict.get("obj_0_10").frame()),-20,0.5f,true);
        playAudioScene("DEMO",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1.4f,1.1f,objectDict.get("obj_10_10").frame()),-30,1.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        showButton();
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.2f,objectDict.get("button").frame()),-20,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.75f,objectDict.get("workrect").frame()),-25,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        nextScene();
    }


}
