package org.onebillion.onecourse.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;
import org.onebillion.onecourse.utils.ULine;
import org.onebillion.onecourse.utils.UPath;
import org.onebillion.onecourse.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 17/03/2017.
 */

public class OC_AddSubtract_S3 extends OC_SectionController
{
    List<OBGroup> numbers;
    Map<String, Integer> eventColour;
    int rightColour, leftColour;
    int currentNum, correct, currentIndex, currentPhase;

    public float graphicScale()
    {
        return this.bounds().width() / 1024.0f;
    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColour = OBMisc.loadEventColours(this);
        numbers = OBMisc.loadNumbersInBoxes(0, 5, eventColour.get("box"), Color.BLACK, "numbox", this);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
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
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(eventAttributes.get("reload") != null)
        {
            correct = OBUtils.getIntValue(eventAttributes.get("reload"));
            OBGroup obj1 = (OBGroup)objectDict.get("obj_1");
            OBGroup obj2 = (OBGroup)objectDict.get("obj_2");

            OBGroup leftCont = (OBGroup)obj1.copy();
            OBGroup rightCont = (OBGroup)obj2.copy();

            leftColour = OBUtils.colorFromRGBString((String)obj1.attributes().get("fill"));
            rightColour = OBUtils.colorFromRGBString((String)obj2.attributes().get("fill"));
            for(int i=0; i<=correct; i++)
            {
                OBControl eqBox = objectDict.get(String.format("eq_box_%d",i));
                String[] eqParts = ((String)eqBox.attributes().get("equation")).split(",");
                String equation = String.format("%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
                String equationName = String.format("equation_%d",i);
                OC_Numberlines_Additions.loadEquation(equation,equationName,eqBox,Color.BLACK,false,0,1,this);
                OC_Numberlines_Additions.hideEquation((OBGroup)objectDict.get(equationName),this);
                OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get(equationName),1,1,leftColour,this);
                OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get(equationName),3,3,rightColour,this);
                objectDict.get(equationName).setZPosition(20-i);
                OBControl box = objectDict.get(String.format("box_%d",i));
                for(int j=0; j<correct; j++)
                {
                    OBGroup copy = null;
                    if(i<correct-j)
                    {
                        copy = (OBGroup)leftCont.copy();
                        copy.disable();
                    }
                    else
                    {
                        OBControl cont = rightCont.copy();
                        OBPath line = (OBPath)objectDict.get("line").copy();
                        line.sizeToBoundingBoxIncludingStroke();
                        line.setWidth(cont.width()*0.9f);
                        line.setPosition(OB_Maths.locationForRect(0.5f,1f,cont.frame()));

                        copy = new OBGroup(Arrays.asList(cont,line));
                        copy.objectDict.put("control",cont);
                        copy.objectDict.put("line",line);
                        cont.hide();
                        line.show();
                        copy.enable();
                    }

                    attachControl(copy);
                    copy.setPosition ( OB_Maths.locationForRect(j*1.0f/(correct-1),0.5f,box.frame()));
                    copy.setZPosition ( 20-i);
                    copy.hide();
                    objectDict.put(String.format("con_%d_%d",i,j+1),copy);
                }
            }
        }
        if(eventAttributes.get("current") != null)
        {
            currentNum = OBUtils.getIntValue(eventAttributes.get("current"));
            try
            {
                showRow(currentNum);
            }
            catch (Exception E)
            {

            }
        }
        currentIndex = 0;
        currentPhase = 1;
    }


    public void doMainXX() throws Exception
    {
        startPhase();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(currentPhase == 1)
            {
                List<OBControl> targets = filterControls(String.format("con_%d_.*",currentNum));
                OBControl targ = null;
                for(OBControl con : targets)
                {
                    RectF rect = new RectF(con.frame());
                    rect.inset(-0.2f * con.width(), -0.2f * con.height());
                    if(con.isEnabled() && rect.contains(pt.x,pt.y))
                    {
                        targ = con;
                        break;
                    }
                }

                if(targ != null && status() == STATUS_AWAITING_CLICK)
                {
                    final OBGroup targetGroup = (OBGroup)targ;
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkTarget(targetGroup);
                                }
                            }
                    );
                }
            }
            else
            {
                final OBControl box = finger(-1,-1,(List<OBControl>)(Object)numbers,pt);
                if(box != null)
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkBox((OBGroup)box);
                                }
                            }
                    );

                }

            }

        }
    }

    public void checkTarget(OBGroup targ) throws Exception
    {
        lockScreen();
        targ.disable();
        targ.objectDict.get("control").show();
        targ.objectDict.get("line").hide();
        playSfxAudio("spawn",false);
        unlockScreen();

        currentIndex++;
        if(currentIndex == currentNum)
        {
            waitSFX();
            playAudio(null);
            waitForSecs(0.3f);
            showSecondPhase();
            waitForSecs(0.1f);
            performSel("demoFin",String.format("%s%d",currentEvent(),currentPhase));
            currentPhase = 2;
            startPhase();
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void checkBox(OBGroup box) throws Exception
    {
        box.objectDict.get("box").setBackgroundColor(eventColour.get("highlight"));
        if((int)box.settings.get("num_val") == correct)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
            OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get(String.format("equation_%d",currentNum)),4,5,"equation",this);
            waitForSecs(0.8f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            box.objectDict.get("box").setBackgroundColor(eventColour.get("box"));
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT2",0.3f,false);
        }
    }

    public void startPhase() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK)
                ,currentPhase == 1? "": String.format("%d",currentPhase),this);
    }

    public void showRow(int row) throws Exception
    {
        lockScreen();
        showControls(String.format("con_%d_.*",row));
        OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d",row));
        OC_Numberlines_Additions.showEquation(equation,1,1,null,this);
        OBControl stripe = objectDict.get("stripe");
        PointF loc = OBMisc.copyPoint(equation.position());
        loc.x = stripe.position().x;
        stripe.setPosition ( loc);
        stripe.show();
        unlockScreen();
    }

    public void clearnScene()
    {
        lockScreen();
        deleteControls("con_.*");
        deleteControls("equation_.*");
        objectDict.get("stripe").hide();
        unlockScreen();
    }

    public void showSecondPhase() throws Exception
    {
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get(String.format("equation_%d",currentNum)),2,3,"equation",this);
    }

    public void demo3a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.5f,1.4f,objectDict.get("con_1_1").frame()),-25,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation_1")).getWorldFrame()),-15, 0.5f, "DEMO", 1 ,0.5f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.1f,objectDict.get("con_1_3").frame()),-20,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        startPhase();

    }
    public void demoFin3a1() throws Exception
    {
        playAudioScene("DEMO2",0,true);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.95f,1.15f,objectDict.get("numbox").frame()),-30,0.5f,"DEMO2",1,0.5f);
        thePointer.hide();

    }
    public void demo3d() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        showSecondPhase();
        waitForSecs(0.3f);
        currentPhase = 2;
        startPhase();

    }
    public void demoEquation() throws Exception
    {
        for(int i=0; i<=correct; i++)
        {
            OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d",i));
            movePointerToPoint(OB_Maths.locationForRect(1.05f,0.9f,equation.frame()),-20,0.5f,true);
            OC_Numberlines_Additions.colourEquation(equation,1,5,Color.RED,this);
            playAudioScene("DEMO2",i,true);
            waitForSecs(0.3f);
            lockScreen();
            OC_Numberlines_Additions.colourEquation(equation,1,5,Color.BLACK,this);
            OC_Numberlines_Additions.colourEquation(equation,1,1,leftColour,this);
            OC_Numberlines_Additions.colourEquation(equation,3,3,rightColour,this);
            unlockScreen();
        }
    }


    public void demoCount(boolean rearrange) throws Exception
    {
        objectDict.get("stripe").hide();
        if(rearrange)
        {
            OBGroup eq1 = (OBGroup)objectDict.get("equation_0");
            OBAnim attAnim = OBMisc.attachedAnim(eq1, filterControls(String.format("con_0_.*")));
            for(int i=1; i<4; i++)
            {
                List<OBAnim> anim = new ArrayList<>();
                OBGroup eq2 = (OBGroup)objectDict.get(String.format("equation_%d",4-i));
                anim.add(OBMisc.attachedAnim(eq2,filterControls(String.format("con_%d_.*",4-i))));
                anim.add(attAnim);
                anim.add(OBAnim.moveAnim(eq2.position(),eq1));
                anim.add(OBAnim.moveAnim(eq1.position(),eq2));
                playSfxAudio("switch",false);
                OBAnimationGroup.runAnims(anim,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                waitSFX();
                waitForSecs(0.3f);
            }
        }
        waitForSecs(0.2f);
        loadPointer(POINTER_LEFT);

        moveScenePointer(OB_Maths.locationForRect(0.15f,1f,objectDict.get("equation_0").frame()),-20 ,0.5f,"DEMO",0,0.3f);
        playAudioScene("DEMO",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,objectDict.get(String.format("equation_%d",correct)).frame()),-5,2f,true);
        waitAudio();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.95f,0.35f,this.bounds()),-15,0.5f,"DEMO",2,0.3f);
        demoEquation();
        waitForSecs(0.3f);
        thePointer.hide();
        if(events.get(events.size()-1) != currentEvent())
        {
            waitForSecs(1f);
            clearnScene();
        }
        waitForSecs(0.3f);
        nextScene();
    }

    public void demo3f() throws Exception
    {
        demoCount(true);
    }

    public void demo3g() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1.1f,objectDict.get("con_0_2").frame()),-25,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation_0")).getWorldFrame()),-15,0.5f,"DEMO",1,0.3f);
        showSecondPhase();
        waitForSecs(0.5f);
        thePointer.hide();
        currentPhase = 2;
        startPhase();
    }

    public void demo3l() throws Exception
    {
        demoCount(false);
    }

    public void demo3m() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1.1f,objectDict.get("con_0_3").frame()),-25,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation_0")).getWorldFrame()) ,-15,0.5f,"DEMO",1,0.3f);
        showSecondPhase();
        waitForSecs(0.5f);
        thePointer.hide();
        currentPhase = 2;
        startPhase();

    }

    public void demo3s() throws Exception
    {
        demoCount(false);
    }
}
