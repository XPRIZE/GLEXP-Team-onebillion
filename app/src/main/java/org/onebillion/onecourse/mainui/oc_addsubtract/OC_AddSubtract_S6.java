package org.onebillion.onecourse.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.glstuff.OBGLView;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 29/03/2017.
 */

public class OC_AddSubtract_S6 extends OC_SectionController
{
    Map<String,Integer> eventColour;
    List<OBControl> puzzleTargets;
    OBLabel currentLabel;
    int lowlightColour, highlightColour;
    int phase;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        //objectDict.get("space").hide();
        eventColour = OBMisc.loadEventColours(this);
        objectDict.get("eq_background").setZPosition(1);
        objectDict.get("eq_frame").setZPosition(5);
        ((OBPath)objectDict.get("eq_frame")).sizeToBoundingBoxIncludingStroke();
        ((OBPath)objectDict.get("frame")).sizeToBoundingBoxIncludingStroke();
        OBControl shutter =objectDict.get("eq_shutter");
        shutter.setZPosition ( 4);
        shutter.setProperty("start_width",shutter.width());
        shutter.setAnchorPoint(1,0.5f);
        OBGroup alien = (OBGroup)objectDict.get("alien");
        OBControl alienMask = objectDict.get("alien_mask");
        alienMask.show();
        alien.show();
        puzzleTargets = new ArrayList<>();
        detachControl(alienMask);
        alien.setScreenMaskControl(alienMask);
        float fontSize = 60 * objectDict.get("eq_background").height()/79.0f;
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        List<OBControl> controls =  OBUtils.randomlySortedArray(filterControls("obj_.*"));

        for(OBControl control : controls)
        {
            ((OBPath)control).sizeToBoundingBoxIncludingStroke();
            String[] eqParts = ((String)control.attributes().get("equation")).split(" ");
            OBLabel label = new OBLabel(eqParts[eqParts.length-1],OBUtils.standardTypeFace(),fontSize); label.setColour(eventColour.get("num"));
            String[] loc = ((String)control.attributes().get("num_loc")).split(",");
            label.setPosition(OB_Maths.locationForRect(Float.valueOf(loc[0]),Float.valueOf(loc[1]), control.getWorldFrame()));
            attachControl(label);
            label.setZPosition(5);
            objectDict.put(String.format("num_%s", eqParts[eqParts.length-1]), label);

            OBControl mask = new OBControl();
            mask.setFrame(control.getWorldFrame());
            mask.setBackgroundColor(Color.BLACK);
            mask.setZPosition(10);
            mask.texturise(false, this);
           // control.setScreenMaskControl(mask);
            control.setProperty("mask_control", mask);
            if(Integer.valueOf(eqParts[4]) == 3)
            {
                puzzleTargets.add(0,control);
            }
            else
            {
                puzzleTargets.add(control);
            }
        }

        OBPath path = (OBPath)puzzleTargets.get(0);
        lowlightColour = path.fillColor();
        highlightColour = OBUtils.highlightedColour(path.fillColor());
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo6a();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        try
        {
            setCurrentEquation();
        }catch (Exception e)
        {

        }
        phase = 1;
    }

    public void doMainXX() throws Exception
    {
        waitSFX();
        startPhase();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(phase == 1)
            {
                if(finger(0,1,filterControls("eq_.*"),pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkTargetEquation();
                                }
                            }
                    );

                }

            }
            else
            {
                final OBControl targ = finger(0,1,puzzleTargets,pt);
                if(targ != null)
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    OBUtils.runOnOtherThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    checkTarget((OBPath)targ);
                                }
                            }
                    );

                }

            }

        }

    }

    public void checkTargetEquation() throws Exception
    {
        playAudio(null);
        animateShutterOpen();
        phase = 2;
        performSel("demo2",currentEvent());
        startPhase();
    }


    public void checkTarget(OBPath targ) throws Exception
    {
        targ.setFillColor(highlightColour);
        if(targ == puzzleTargets.get(0))
        {
            gotItRight();
            lockScreen();
            currentLabel.hide();
            OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),5,5,null,this);
            playSfxAudio("equation",false);

            unlockScreen();
            waitSFX();
            animateMaskMove(targ);
            puzzleTargets.remove(0);
            waitForSecs(0.4f);
            showShutter();
            if(eventAttributes.get("targets") != null && eventAttributes.get("targets").equals("all"))
            {
                if(puzzleTargets.size() > 0)
                {
                    setCurrentEquation();
                    phase = 1;
                    waitSFX();
                    startPhase();
                }
                else
                {
                    waitForSecs(0.3f);
                    displayTick();
                    waitForSecs(0.3f);
                    animateAlien();
                    waitForSecs(0.3f);
                    nextScene();
                }
            }
            else
            {
                nextScene();
            }

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.setFillColor ( lowlightColour);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",300,false);

        }
    }

    public void startPhase() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK)
                ,phase == 1? "": String.format("%d",phase),this);
    }

    public void animateMaskMove(final OBPath cont) throws Exception
    {
        lockScreen();
        cont.setScreenMaskControl((OBControl)cont.propertyValue("mask_control"));
        unlockScreen();
        playSfxAudio("revealpic",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left", cont.right(), cont.maskControl), new OBAnimBlock()

                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        cont.invalidate();
                    }
                })
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        cont.hide();
    }

    public void animateAlien() throws Exception
    {
        OBGroup alien = (OBGroup)objectDict.get("alien");
        float move = alien.top();
        playSfxAudio("alien",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("bottom", move, alien)), 1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null,this);

        List<String> blink = Arrays.asList("blink_1","blink_2","blink_3","blink_4","blink_3","blink_2","blink_1");
        animateFrames(blink,0.05f,alien);
        animateFrames(Arrays.asList("mouth_1", "mouth_2"),0.04f,alien);
        animateFrames(blink,0.05f,alien);
    }

    public void setCurrentEquation() throws Exception
    {
        lockScreen();
        if(objectDict.get("equation") != null)
            detachControl(objectDict.get("equation"));
        OBControl control = puzzleTargets.get(0);
        String[] eqParts = ((String)control.attributes().get("equation")).split(" ");
        currentLabel = (OBLabel)objectDict.get(String.format("num_%s",eqParts[eqParts.length-1]));
        OC_Numberlines_Additions.loadEquation((String)control.attributes().get("equation"),"equation",objectDict.get("eq_background"),eventColour.get("equation"),false,1.5f,1,this);
        objectDict.get("equation").setZPosition(2);
        OC_Numberlines_Additions.colourEquation((OBGroup)objectDict.get("equation"),5,5,eventColour.get("num"),this);
        OC_Numberlines_Additions.hideEquation((OBGroup)objectDict.get("equation"),this);
        OC_Numberlines_Additions.showEquation((OBGroup)objectDict.get("equation"),1,4,null,this);

        unlockScreen();
    }

    public void animateShutterOpen() throws Exception
    {
        final OBControl shutter = objectDict.get("eq_shutter");
        playSfxAudio("shutteropening",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("width",1,shutter))
                ,0.5,true,OBAnim.ANIM_EASE_IN,this);

        shutter.hide();
    }

    public void showShutter() throws Exception
    {
        OBControl shutter = objectDict.get("eq_shutter");
        float width = (float)shutter.settings.get("start_width") ;
        shutter.setWidth(width);
        lockScreen();
        shutter.show();
        objectDict.get("equation").show();
        playSfxAudio("shutterclosing",false);
        unlockScreen();
    }

    public void demo6a() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.05f,0.5f,objectDict.get("eq_frame").frame()),-60,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        startPhase();

    }

    public void demo26a() throws Exception
    {
        OBPath path = (OBPath)objectDict.get("finger_path");
        path.sizeToBox(objectDict.get("space").frame());
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.05f,0.5f,objectDict.get("eq_frame").frame()),-60,0.5f,"DEMO2",0,0.3f);
        playAudioScene("DEMO2",1,false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(path.firstPoint(),thePointer)),0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(thePointer,path.path(),false,0)),2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        startPhase();
    }
}
