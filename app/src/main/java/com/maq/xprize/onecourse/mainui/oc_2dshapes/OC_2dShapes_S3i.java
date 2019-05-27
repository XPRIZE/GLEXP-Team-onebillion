package com.maq.xprize.onecourse.mainui.oc_2dshapes;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
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

import java.util.Arrays;

/**
 * Created by michal on 06/03/2018.
 */

public class OC_2dShapes_S3i extends OC_SectionController
{
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3i();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        deleteControls("obj_.*");
        deleteControls("num_.*");
        super.setSceneXX(scene);
        String[] nums = eventAttributes.get("nums").split(",");
        int index = 1;
        for(String num : nums)
        {
            OBControl drop = objectDict.get(String.format("drop_%d",index));
            drop.setProperty("num_val",OBUtils.getIntValue(num));
            OBLabel label = new OBLabel(num,OBUtils.StandardReadingFontOfSize(70));
            label.setColour(Color.BLACK);
            PointF loc = OBMisc.copyPoint(objectDict.get(String.format("line_%d",index)).position());
            loc.y = OB_Maths.locationForRect(0f,0.5f,objectDict.get("bottombar").frame()).y;
            label.setPosition(loc);
            attachControl(label);
            objectDict.put(String.format("num_%d", index), label);
            label.setZPosition(5);
            index++;
        }

        for(OBControl control : filterControls("obj_.*"))
        {
            float rotate = Float.valueOf((String)control.attributes().get("rotate"));
            control.setRotation((float)Math.toRadians(rotate));
            control.setProperty("start_loc",OBMisc.copyPoint(control.position()));
            control.enable();
            control.setZPosition(4);
            if(control instanceof OBPath)
                ((OBPath)control).sizeToBoundingBoxIncludingStroke();
        }

        for(OBControl control : filterControls("drop_.*"))
            control.enable();
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt,View v)
    {

        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            OBControl cont = finger(0, 1, filterControls("obj_.*"), pt);
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
        if (status() == STATUS_DRAGGING && target != null)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            final OBControl targ = target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop(targ, pt);
                }
            });
        }
    }

    public void checkDrop(OBControl targ, PointF pt) throws Exception
    {
        OBControl drop = null;
        for(OBControl con : filterControls("drop_.*"))
        {
            if(con.frame().contains(pt.x, pt.y) && con.isEnabled())
            {
                drop = con;
                break;
            }
        }
        if(drop != null && OBUtils.getIntValue((String)targ.attributes().get("sides")) == (int)drop.propertyValue("num_val"))
        {
            targ.disable();
            drop.disable();
            OBControl line = objectDict.get(String.format("line_%s",(String)drop.attributes().get("line")));
            OBLabel label =(OBLabel ) objectDict.get(String.format("num_%s",(String)drop.attributes().get("line")));
            label.setColour(Color.RED);
            playSfxAudio("drop",false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(locationForLine(line,targ) ,targ))
                    ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            if(targ.rotation != 0)
            {
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim(0,targ))
                        ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            }
            targ.setZPosition(2);
            boolean complete = true;
            for(OBControl control : filterControls("obj_.*"))
            {
                if(control.isEnabled())
                {
                    complete = false;
                    break;
                }
            }
            gotItRight();
            if(complete)
            {
                waitForSecs(0.3f);
                displayTick();
                waitForSecs(0.5f);
                nextScene();
            }
            else
            {
                setStatus(STATUS_WAITING_FOR_DRAG);
            }

        }
        else
        {
            if(drop != null)
                gotItWrongWithSfx();
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
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)targ.propertyValue("start_loc") ,targ))
        ,0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        targ.setZPosition(targ.zPosition()-10);
    }

    public PointF locationForLine(OBControl line,OBControl targ)
    {
        PointF loc = OBMisc.copyPoint(line.position());
        loc.y -= targ.bounds.height() * 0.5 + applyGraphicScale(10);
        return loc;
    }

    public void demo3i() throws Exception
    {
        loadPointer(POINTER_LEFT);
        OBControl obj = objectDict.get("obj_2");
        RectF frame = new RectF(obj.getWorldFrame());
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.7f, 1.15f) , frame),
                -30,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(obj.position(),-25,0.25f,true);
        waitForSecs(0.2f);
        OBMisc.moveControlWithAttached(obj,Arrays.asList(thePointer),locationForLine(objectDict.get("line_1"),obj),
                0.6f,OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBLabel label =(OBLabel ) objectDict.get("num_1");
        playSfxAudio("drop",false);
        label.setColour(Color.RED);
        waitSFX();
        frame = new RectF(obj.getWorldFrame());
        moveScenePointer(OB_Maths.locationForRect(new PointF(1.15f, 0.85f) , frame),
                -30,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        nextScene();
    }



}
