package org.onebillion.xprz.mainui.x_moreaddsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.x_numberlines.X_Numberlines_Additions;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 28/07/16.
 */
public class X_MoreAddSubtract_S6i extends XPRZ_SectionController
{
    Map<String,Integer> eventColour;
    List<OBControl> dragTargets, dropTargets;
    int currentPhase;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master6i");

        dragTargets = new ArrayList<>();
        dropTargets = new ArrayList<>();
        eventColour = OBMisc.loadEventColours(this);

        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        PointF loc = XPRZ_Generic.copyPoint(objectDict.get("eqbox1").position());
        loc.x = OB_Maths.locationForRect(-0.5f,0f,this.bounds()).x;
        objectDict.get("eqbox1").setPosition(loc);

        PointF loc2 =  XPRZ_Generic.copyPoint(objectDict.get("eqbox2").position());
        loc2.x = OB_Maths.locationForRect(1.5f,0f,this.bounds()).x;
        objectDict.get("eqbox2").setPosition(loc2);

        OBPath dragBox =((OBPath)objectDict.get("box_drag"));
        dragBox.sizeToBoundingBoxIncludingStroke();

        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo();
            }
        });
    }

    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        deleteAllControls(dragTargets);

        String[] eqParts = eventAttributes.get("equ").split(",");

        detachControl(objectDict.get("equation_1"));
        String equation1 = String.format( "%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
        X_Numberlines_Additions.loadEquation(equation1,"equation_1",objectDict.get("eqbox1"),eventColour.get("equation"),false,0,0.9f,this);

        detachControl(objectDict.get("equation_2"));
        String equation2 = String.format( "%s – %s = %s", eqParts[2], eqParts[1], eqParts[0]);
        X_Numberlines_Additions.loadEquation(equation2,"equation_2",objectDict.get("eqbox2"),eventColour.get("equation"),false,0,0.9f,this);

        OBLabel eqLabel = X_Numberlines_Additions.getLabelForEquation(1,(OBGroup)objectDict.get("equation_1"));

        int index = 0;
        for(String num : eventAttributes.get("nums").split(","))
        {
            index++;
            OBLabel label = new OBLabel(num, eqLabel.typeface(), eqLabel.fontSize());
            label.setColour(Color.BLACK);
            label.setZPosition(2);
            OBControl box = objectDict.get("box_drag").copy();
            box.show();
            box.setZPosition(1.5f);
            label.setPosition(box.position());
            OBGroup group = new OBGroup(Arrays.asList(label,box));
            group.setPosition ( OB_Maths.locationForRect(0.2f + index*(0.6f/4),0.6f,objectDict.get("topbar").frame()));

            attachControl(group);
            group.setProperty("num_value",num);
            group.setProperty("start_loc",XPRZ_Generic.copyPoint(group.position()));
            group.setZPosition ( 3);
            dragTargets.add(group);
            group.hide();
        }
        currentPhase = 1;

    }

    @Override
    public void doMainXX() throws Exception
    {
        startPhase();
    }

    @Override
    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl cont = finger(0,1, dragTargets,pt);
            if(cont != null)
            {
                setStatus(STATUS_BUSY);
                OBMisc.prepareForDragging(cont,pt,this);
                setStatus(STATUS_DRAGGING);
            }
        }
    }

    @Override
    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    @Override
    public void touchUpAtPoint(PointF pt, View v)
    {

                if (status() == STATUS_DRAGGING && target!=null){
                    setStatus(STATUS_BUSY);
                    final OBControl targ = target;
                    target = null;

                    OBControl searchDrop = null;
                    for(OBControl con : dropTargets)
                    {

                        if(con.frame().intersect(targ.frame()))
                        {
                            searchDrop = con;
                            break;
                        }
                    }

                    final OBControl drop = searchDrop;

                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkDrop(targ, drop);
                        }
                    });
                }
    }

    private void checkDrop(OBControl targ, OBControl drop) throws Exception
    {
        if(drop != null)
        {
            if(drop.settings.get("num_value").equals(targ.settings.get("num_value")))
            {
                playAudio(null);
                OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(drop.position(),targ)),0.2f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                waitForSecs(0.1f);
                snapObject(targ,drop);
                if(dropTargets.size() == 0)
                {
                    gotItRightBigTick(true);
                    if(currentPhase == 1)
                    {
                        currentPhase++;
                        startPhase();
                    }
                    else
                    {
                        countEquations();
                        if(currentEvent() != events.get(events.size()-1))
                        {
                            hideScene();
                        }
                        waitForSecs(0.3f);
                        nextScene();
                    }
                }
                else
                {
                    gotItRightBigTick(false);
                    setStatus(STATUS_WAITING_FOR_DRAG);
                }
            }
            else
            {
                gotItWrongWithSfx();
                moveControlBack(targ);
                long time = setStatus(STATUS_WAITING_FOR_DRAG);
                waitSFX();
                if(time == statusTime)
                    playAudioQueuedScene(currentPhase == 1 ? "INCORRECT" : "INCORRECT2",300,false);
            }
        }
        else
        {
            moveControlBack(targ);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public void createPhaseMissing()
    {
        lockScreen();
        deleteAllControls(dropTargets);
        String[] missing = eventAttributes.get(currentPhase == 1 ? "top_missing" : "bottom_missing").split(",");
        for(String num : missing)
        {
            OBGroup box = (OBGroup)objectDict.get("box_drop").copy();
            box.show();
            OBLabel targetLabel = X_Numberlines_Additions.getLabelForEquation(Integer.valueOf(num),(OBGroup)objectDict.get(String.format("equation_%d", currentPhase)));
            box.setPosition(targetLabel.getWorldPosition());
            box.setProperty("num_value",targetLabel.text());
            attachControl(box);
            dropTargets.add(box);
            box.setZPosition(2.5f);
        }
        unlockScreen();

    }

    public void slideEquation() throws Exception
    {
        OBControl equation = objectDict.get(String.format("equation_%d",currentPhase));
        PointF loc = XPRZ_Generic.copyPoint(equation.position());
        loc.x = OB_Maths.locationForRect(0.5f,0f,this.bounds()).x;
        playSfxAudio("slide",false);
        OBMisc.moveControlWithAttached(equation,dropTargets,loc,0.65f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.3f);
    }

    public void showDragTargets() throws Exception
    {
        lockScreen();
        for(OBControl con : dragTargets)
            con.show();
        unlockScreen();
        playSfxAudio("mcnumpopon",true);
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_WAITING_FOR_DRAG),currentPhase == 1 ? "" : "2",this);
    }

    public void startPhase() throws Exception
    {
        createPhaseMissing();
        if(!performSel("demo",String.format("%s%d",currentEvent(), currentPhase)))
        {
            if(currentPhase == 1)
            {
                playAudioQueuedScene("DEMO",300,true);
            }
            slideEquation();
            showDragTargets();
        }
        startScene();
    }


    public void deleteAllControls(List<OBControl> array)
    {
        for(OBControl obj : array)
            detachControl(obj);

        array.clear();
    }

    public void moveControlBack(OBControl targ)
    {
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim((PointF)targ.propertyValue("start_loc"),targ)) ,0.25f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        targ.setZPosition(targ.zPosition() - 10);

    }

    public void countEquations() throws Exception
    {
        for(int i = 0; i<2; i++)
        {
            OBGroup equ = (OBGroup)objectDict.get(String.format("equation_%d", i+1));
            X_Numberlines_Additions.colourEquation(equ,1,5,Color.RED,this);
            playAudioScene("FINAL",i,true);
            waitForSecs(0.3f);
            X_Numberlines_Additions.colourEquation(equ,1,5,eventColour.get("equation"),this);
            waitForSecs(0.3f);
        }
        waitForSecs(0.7f);
    }

    public void snapObject(OBControl targ,OBControl drop) throws Exception
    {
        lockScreen();
        detachControl(drop);
        targ.hide();
        targ.setPosition((PointF)targ.propertyValue("start_loc"));

        unlockScreen();
        playSfxAudio("snapin",true);
        dropTargets.remove(drop);
    }

    public void hideScene()
    {
        lockScreen();
        for(OBControl con : dragTargets)
            con.hide();

        objectDict.get("equation_1").hide();
        objectDict.get("equation_2").hide();
        unlockScreen();
    }


    public void demo() throws Exception
    {
        createPhaseMissing();
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        slideEquation();
        waitForSecs(0.3f);

        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,dropTargets.get(0).frame()),-30,0.5f,"DEMO",1,0.3f);
        showDragTargets();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.58f,1.1f,objectDict.get("topbar").frame()),-20,0.5f,"DEMO",2,0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.6f,dragTargets.get(2).frame()) ,-10,0.3f,true);
        OBMisc.moveControlWithAttached(dragTargets.get(2),Collections.singletonList(thePointer),dropTargets.get(0).position(), 0.6f, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(0.2f);
        snapObject(dragTargets.get(2),dropTargets.get(0));
        waitForSecs(0.2f);
        playAudio("correct");
        waitAudio();

        movePointerToPoint(OB_Maths.locationForRect(0.7f,0.7f,this.bounds()),-35,0.3f,true);
        waitForSecs(0.3f);
        currentPhase++;
        createPhaseMissing();
        slideEquation();
        showDragTargets();
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.1f,dropTargets.get(0).frame()),-35,0.5f,"DEMO",3,0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,dragTargets.get(0).frame()) ,-35,0.5f,true);
        OBMisc.moveControlWithAttached(dragTargets.get(0),Collections.singletonList(thePointer),dropTargets.get(0).position(), 0.7f, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(0.2f);
        snapObject(dragTargets.get(0),dropTargets.get(0));
        waitForSecs(0.2f);
        playAudio("correct");
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();

        countEquations();

        hideScene();
        waitForSecs(0.3f);
        nextScene();
    }


}
