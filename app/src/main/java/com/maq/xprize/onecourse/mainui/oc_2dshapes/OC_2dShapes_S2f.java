package com.maq.xprize.onecourse.mainui.oc_2dshapes;

import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;
import com.maq.xprize.onecourse.utils.ULine;
import com.maq.xprize.onecourse.utils.UPath;
import com.maq.xprize.onecourse.utils.USubPath;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 06/03/2018.
 */

public class OC_2dShapes_S2f extends OC_SectionController
{
    Map<String,OBControl> dropObjs;
    OBLabel caption;
    int textColour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        this.localisations = loadLocalisations(getLocalPath("_localisations.xml"));
        textColour = OBUtils.colorFromRGBString(eventAttributes.get("colour_text"));
        dropObjs = new ArrayMap<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBPath box = (OBPath)objectDict.get("box");

        box.setProperty("start_loc", OBMisc.copyPoint(box.position()));
        OBPath dot = (OBPath)objectDict.get("dot");
        dot.sizeToBoundingBoxInset(-applyGraphicScale(2));
        setSceneXX(currentEvent());
        box.sizeToBoundingBoxIncludingStroke();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5f();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        clearScene();
        if(caption != null)
            detachControl(caption);
        OBControl box = objectDict.get("box");
        box.setPosition((PointF)box.propertyValue("start_loc"));
        super.setSceneXX(scene);
        OBPath dot = (OBPath)objectDict.get("dot");
        OBPath path =(OBPath)objectDict.get("obj");
        //path.sizeToBoundingBoxIncludingStroke();
        UPath upath = deconstructedPath(currentEvent(), "obj");
        USubPath subpath = upath.subPaths.get(0);
        int index = 0;
        for(ULine element : subpath.elements)
        {
            index++;
            OBPath copy = (OBPath)dot.copy();
            copy.setPosition(element.tAlongt(0, null));
            attachControl(copy);
            copy.show();
            objectDict.put(String.format("dot_%d",index),copy);
        }

        path.sizeToBoundingBoxIncludingStroke();
        for(OBControl control : filterControls("line_.*"))
        {
            control.setProperty("start_loc",OBMisc.copyPoint(control.position()));
        }

        if(eventAttributes.get("caption") != null)
        {
            OBControl box2 = objectDict.get("text_box");
            caption = new OBLabel(this.localisations.get(eventAttributes.get("caption")),OBUtils.StandardReadingFontOfSize(90));
            caption.setColour(textColour);
            attachControl(caption);
            caption.setPosition(box2.position());
            caption.hide();
        }
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {

        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl cont = finger(0, 1, filterControls("line_.*"), pt);
            if (cont != null && cont.isEnabled())
            {
                setStatus(STATUS_BUSY);
                OBMisc.prepareForDragging(cont, pt, this);
                setStatus(STATUS_DRAGGING);
            }
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));

    }

    public void touchUpAtPoint(PointF pt,View v)
    {

        if (status() == STATUS_DRAGGING && target != null)
        {
            setStatus(STATUS_BUSY);
            final OBControl targ = target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop(targ);
                }
            });


        }


    }

    public void checkDrop(OBControl targ) throws Exception
    {
        OBControl drop = nearestEnabledControl(filterControls("drop_.*"),targ.position(),applyGraphicScale(75));
        if (drop != null)
        {
            boolean correct = false;
            for (String num : ((String)targ.attributes().get("drop")).split(","))
            {
                OBControl dropCon = objectDict.get(String.format("drop_%s", num));
                if (dropCon == drop && dropCon.isEnabled())
                {
                    correct = true;
                    break;
                }
            }
            if(correct)
            {
                drop.disable();
                targ.disable();
                playSfxAudio("drop", false);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(drop.position(), targ)), 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                gotItRight();
                if (checkFigureComplete())
                {
                    waitAudio();
                    waitForSecs(0.3f);
                    showObj();
                    waitForSecs(0.5f);
                    displayTick();
                    waitForSecs(0.5f);
                    playAudioScene("FINAL", 0, true);
                    if (eventAttributes.get("caption") != null) demoCaption();
                    waitForSecs(1f);
                    nextScene();
                }
                else
                {
                    setStatus(STATUS_WAITING_FOR_DRAG);
                }

            }
            else
            {
                gotItWrongWithSfx();
                flyControlBack(targ);
                long time = setStatus(STATUS_WAITING_FOR_DRAG);
                waitSFX();
                if (time == statusTime)
                    playAudioQueuedScene("INCORRECT", 0.3f, false);
            }
        }
        else
        {
            flyControlBack(targ);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_WAITING_FOR_DRAG), this);
    }

    public void flyControlBack(OBControl targ) throws Exception
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)targ.settings.get("start_loc") ,targ)),0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        targ.setZPosition(targ.zPosition()-10);
    }

    public boolean checkFigureComplete()
    {
        boolean complete = true;
        for(OBControl con : filterControls("drop_.*"))
        {
            if(con.isEnabled())
            {
                complete = false;
                break;
            }
        }
        return complete;
    }

    public void clearScene()
    {
        deleteControls("drop_*");
        detachControl(objectDict.get("obj"));
    }

    public OBControl nearestEnabledControl(List<OBControl> controls, PointF point, float tolerance)
    {
        OBControl result = null;
        float dist = bounds().width();
        for(OBControl control : controls)
        {
            float currentDist = OB_Maths.PointDistance(point, control.position());
            if(control.isEnabled() && currentDist <= tolerance &&(result == null || dist > currentDist))
            {
                result = control;
                dist = currentDist;
            }
        }
        return result;
    }

    public void showObj()
    {
        lockScreen();
        deleteControls("line_.*");
        deleteControls("dot_.*");
        objectDict.get("obj").show();
        unlockScreen();
    }

    public void demo5f() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.8f,objectDict.get("box") .frame()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.7f,objectDict.get("box") .frame()),-25,0.5f,"DEMO",1,0.3f);
        for(int i=1; i<4; i++)
        {
            OBControl line = objectDict.get(String.format("line_%d",i));
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,line.frame()),-20,0.5f,true);
            OBMisc.moveControlWithAttached(line,Arrays.asList(thePointer),objectDict.get(String.format("drop_%d",i)).position(),0.7f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            playSfxAudio("drop",true);
        }
        OBPath obj =(OBPath ) objectDict.get("obj");
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.8f, 1.1f) , convertRectFromControl(obj.bounds,obj)),-20,0.5f,true);
        showObj();
        waitForSecs(0.4f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1.5f);
        nextScene();
    }

    public void demoCaption() throws Exception
    {
        OBControl obj = objectDict.get("obj");
        PointF loc = OBMisc.copyPoint(obj.position());
        loc.x = 0.5f*bounds().width();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",bounds().width(),objectDict.get("box")) ,
                OBAnim.moveAnim(loc,obj))
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        loadPointer(POINTER_LEFT);
        waitForSecs(0.3f);
        caption.show();
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,caption.frame()),-35,0.8f,"FINAL",1,0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
    }

}
