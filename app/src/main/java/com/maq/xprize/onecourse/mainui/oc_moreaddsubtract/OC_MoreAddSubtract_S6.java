package com.maq.xprize.onecourse.mainui.oc_moreaddsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.ArrayMap;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 27/07/16.
 */
public class OC_MoreAddSubtract_S6 extends OC_SectionController
{
    Map<String,Integer> eventColour;
    List<OBControl> leftTargets, rightTargets, numTargets;
    List<Integer>  correctNums;
    boolean subtractMode, freeMode;
    int currentPhase;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");

        eventColour = new ArrayMap<>();
        leftTargets = new ArrayList<>();
        rightTargets = new ArrayList<>();
        numTargets = new ArrayList<>();

        eventColour = OBMisc.loadEventColours(this);

        ((OBPath)objectDict.get("box")).sizeToBoundingBoxIncludingStroke();
        ((OBPath)objectDict.get("cover")).sizeToBoundingBoxIncludingStroke();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                startPhase();
            }
        });

    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);

        String[] eqParts = eventAttributes.get("equ").split(",");
        correctNums = Arrays.asList(Integer.valueOf(eqParts[2]),Integer.valueOf(eqParts[0]));

        detachControl(objectDict.get("equation_1"));
        String equation1 = String.format( "%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
        OC_Numberlines_Additions.loadEquation(equation1,"equation_1",objectDict.get("eqbox1"),eventColour.get("equation"),false,0,1,this);
        objectDict.get("equation_1").hide();

        detachControl(objectDict.get("equation_2"));
        String equation2 = String.format( "%s – %s = %s", eqParts[2], eqParts[1], eqParts[0]);
        OC_Numberlines_Additions.loadEquation(equation2,"equation_2",objectDict.get("eqbox2"),eventColour.get("equation"),false,0,1,this);
        objectDict.get("equation_2").hide();

        prepareObjects(objectDict.get("left"),eventAttributes.get("left"),leftTargets);
        prepareObjects(objectDict.get("right"),eventAttributes.get("right"),rightTargets);

        for(OBControl con : leftTargets)
            con.show();


      /*  for(int i=0; i<rightTargets.size(); i++)
        {
            OBControl con = rightTargets.get(i);
            con.show();
            OBImage img = new OBImage(con.renderedImageOverlay(eventColour.get("overlay")));
            img.setPosition(con.position());
            img.setZPosition(1.5f);
            con.setZPosition(2);
            attachControl(img);
            OBGroup group = new OBGroup(Arrays.asList(img,con));
            attachControl(group);
            group.objectDict.put("image",con);
            group.hide();
        }*/
        loadNumbers(eventAttributes.get("nums"));
        currentPhase = 1;
    }

    @Override
    public void doMainXX() throws Exception
    {

        startPhase();
    }


    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {
        final OC_SectionController controller = this;

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(currentPhase == 1 || currentPhase == 3)
            {
                if(objectDict.get("box").frame().contains(pt.x, pt.y))
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            playAudio(null);
                            boxTouched();
                            currentPhase++;
                            waitForSecs(0.3f);
                            showPhaseEquation();
                            startPhase();
                        }
                    });
                }
            }
            else
            {
                final OBControl conLabel = finger(0,1,numTargets,pt);
                if(conLabel != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkTarget((OBLabel) conLabel);
                        }
                    });
                }
            }
        }

    }

    private void checkTarget(OBLabel conLabel) throws Exception
    {
        playAudio(null);
        OBLabel label = conLabel;
        label.setColour(Color.RED);

        if((int)label.settings.get("num_value") == correctNums.get(currentPhase <= 2 ? 0 : 1))
        {
            lockScreen();
            objectDict.get("cover").hide();
            OC_Numberlines_Additions.getLabelForEquation(5,(OBGroup)objectDict.get(String.format("equation_%d",currentPhase <= 2 ? 1 : 2))).show();
            unlockScreen();
            gotItRightBigTick(true);
            currentPhase++;
            waitForSecs(0.3f);


            if(currentPhase == 5)
            {
                label.setColour(Color.BLACK);
                for(int i = 0; i<2; i++)
                {
                    OBGroup equ = (OBGroup)objectDict.get(String.format("equation_%d", i+1));
                    OC_Numberlines_Additions.colourEquation(equ,1,5,Color.RED, this);
                    playAudioScene("FINAL",i,true);
                    waitForSecs(0.3f);
                    OC_Numberlines_Additions.colourEquation(equ,1,5,eventColour.get("equation"), this);
                    waitForSecs(0.3f);
                }
                waitForSecs(0.7f);

                nextScene();
            }
            else
            {
                lockScreen();
                label.setColour(Color.BLACK);
                for(OBControl con : numTargets)
                    con.hide();
                unlockScreen();
                startPhase();
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            label.setColour(Color.BLACK);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene(currentPhase == 1 ? "INCORRECT" : "INCORRECT2",0.3f, false);
        }
    }


    public void prepareObjects(OBControl obj,String list, List<OBControl> container)
    {
        deleteAllControls(container);

        if(list == null)
            return;

        OBControl clone = null;
        if(obj.getClass() == OBGroup.class)
        {
            clone = obj.copy();
            OBMisc.colourObjectFromAttributes((OBGroup)clone);
            clone.texturise(true, this);
        }
        else if(obj.getClass() == OBPath.class)
        {
            ((OBPath)obj).sizeToBoundingBoxIncludingStroke();
            clone = obj;
        }
        else
        {
            clone = obj;
        }
        obj.hide();
        String[] array = list.split(",");

        int index = 1;
        for(int i=0; i< array.length; i+=2)
        {
            OBControl newObj = clone.copy();
            newObj.setPosition(OB_Maths.locationForRect(Float.valueOf(array[i]),Float.valueOf(array[i+1]), objectDict.get("box").frame()));
            attachControl(newObj);
            container.add(newObj);
            index++;
            newObj.hide();
        }

        detachControl(obj);
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),currentPhase==1 ? "":String.format("%d", currentPhase), this);
    }

    public void startPhase() throws Exception
    {
        if(!performSel("demo",String.format("%s%d",currentEvent(), currentPhase)))
        {
            waitForSecs(0.3f);
            playAudioQueuedScene(currentPhase == 1 ? "DEMO" : String.format("DEMO%d",currentPhase),0.3f,true);
            waitForSecs(0.3f);
            if(currentPhase == 1 || currentPhase == 3)
                showPhaseEquation();
            else
                showTopNums();

            waitForSecs(0.3f);
        }
        startScene();
    }


    public void deleteAllControls(List<OBControl> array)
    {
        for(OBControl obj : array)
            detachControl(obj);

        array.clear();
    }

    public void showPhaseEquation() throws Exception
    {
        String eqname = String.format("equation_%d",currentPhase <= 2 ? 1 : 2);
        lockScreen();
        OBGroup equation = (OBGroup)objectDict.get(eqname);
        if(currentPhase == 1 || currentPhase == 3)
        {
            equation.show();
            for(int i=2; i<6; i++)
                OC_Numberlines_Additions.getLabelForEquation(i,equation).hide();
        }
        else
        {
            for(int i=2; i<5; i++)
                OC_Numberlines_Additions.getLabelForEquation(i,equation).show();

            OBControl cover = objectDict.get("cover");
            cover.setPosition(OC_Numberlines_Additions.getLabelForEquation(5,equation).getWorldPosition());
            cover.show();
        }
        unlockScreen();
        playSfxAudio("numpopon",true);
    }

    public void loadNumbers(String list)
    {
        deleteAllControls(numTargets);
        String[] array = list.split(",");
        OBControl numbox = objectDict.get("numbox");
        int index =0;
        Typeface font = OBUtils.standardTypeFace();
        for(String numString : array)
        {

            OBLabel label = new OBLabel(numString,font, 70.0f*numbox.height()/100.0f);
            label.setColour(Color.BLACK);
            label.setProperty("num_value",Integer.valueOf(numString));
            label.setPosition(OB_Maths.locationForRect(index *1.0f/(array.length-1),0.5f,numbox.frame()));
            attachControl(label);
            numTargets.add(label);
            label.hide();
            index++;
        }
    }

    public void showTopNums() throws Exception
    {
        lockScreen();
        for(OBControl con : numTargets)
            con.show();
        unlockScreen();
        playSfxAudio("mcnumpopon",true);
    }

    public void boxTouched() throws Exception
    {
        lockScreen();
        if(currentPhase == 1)
        {
            for(OBControl con : rightTargets)
                con.show();
        }
        else
        {
            for(OBControl con : rightTargets)
                con.setColourOverlay(eventColour.get("overlay"));
        }

        unlockScreen();
        playSfxAudio(currentPhase == 1 ? "objpopon" : "objpopoff",true);
    }


    public void demo6a1() throws Exception
    {
        waitForSecs(0.5f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.4f,1.1f,objectDict.get("box").frame()),-35,0.5f,"DEMO",1 ,0.3f);
        showPhaseEquation();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.7f,objectDict.get("box").frame()),-20,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
    }


    public void demo6a2() throws Exception
    {
        waitForSecs(0.3f);
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        showTopNums();
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0f,1.1f,numTargets.get(0).frame()),-25,0.8f,true);
        playAudioScene("DEMO2",1,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1.1f,numTargets.get(2).frame()),-10,1.5f,true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demo6a3() throws Exception
    {
        waitForSecs(0.3f);
        showPhaseEquation();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBLabel label = OC_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation_2"));
        moveScenePointer(OB_Maths.worldLocationForControl(1.1f,0.7f,label),-35,0.5f,"DEMO3",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,1.2f,objectDict.get("box").frame()),-20,0.5f,"DEMO3",1,0.5f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1.05f,objectDict.get("box").frame()),-30,0.5f,"DEMO3",2,0.5f);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.9f,objectDict.get("box").frame()),-30,0.2f,true);
        boxTouched();
        movePointerToPoint(OB_Maths.locationForRect(0.8f,1.05f,objectDict.get("box").frame()),-30,0.2f,true);
        waitForSecs(0.3f);
        currentPhase++;
        showPhaseEquation();
        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(0.3f);
        playAudioScene("DEMO3",3,true);
        waitForSecs(0.3f);
        showTopNums();
        waitForSecs(0.3f);
    }

    public void demo6b1() throws Exception
    {
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        showPhaseEquation();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.5f,objectDict.get("box").frame()),-25,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
    }


    public void demo6b3() throws Exception
    {
        waitForSecs(0.3f);
        playAudioScene("DEMO3",0,true);
        waitForSecs(0.3f);
        showPhaseEquation();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.05f,0.75f,objectDict.get("box").frame()),-35,0.5f,"DEMO3",1,0.5f);
        thePointer.hide();
    }

}
