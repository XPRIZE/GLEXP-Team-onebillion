package org.onebillion.onecourse.mainui.oc_2dshapes;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/03/2018.
 */

public class OC_2dShapes_S5 extends OC_SectionController
{
    OBControl currentLine;
    List<OBControl> targets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        targets = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        for(OBControl control : filterControls("obj_.*"))
        {
            control.setZPosition(20);
            ((OBPath)control).sizeToBoundingBoxIncludingStroke();
        }
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5a();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        targets.clear();
        if (!OBUtils.getBooleanValue(eventAttributes.get("touch_mode")) && eventAttributes.get("line") != null)
        {
            currentLine = objectDict.get(eventAttributes.get("line"));
            if(objectDict.get("drop") != null)
            {
                OBControl drop = objectDict.get("drop");
                PointF loc = OBMisc.copyPoint(drop.position());
                loc.x = currentLine.position().x;
                drop.setPosition(loc);
            }
        }
        for (String name : eventAttributes.get("target").split(","))
            targets.add(objectDict.get(name));
    }

    public void doMainXX() throws Exception
    {
        startScene(true);
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
        else if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl cont = finger(0, 1, filterControls("obj_.*"), pt);
            if (cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget(cont);
                    }
                });
            }
        }
    }


    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target != null)
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

    public void checkTarget(OBControl cont) throws Exception
    {
        playAudio(null);
        if (targets.contains(cont))
        {
            gotItRight();
            if (!performSel("demoFin", currentEvent()))
            {
                displayTick();
                waitForSecs(0.3f);
                playAudioQueuedScene("FINAL", 300, true);

            }
            waitForSecs(0.5f);
            nextScene();

        } else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if (time == statusTime)
                playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void checkDrop(OBControl targ) throws Exception
    {
        boolean dropped = objectDict.get("drop").frame() .contains(targ.position().x, targ.position().y);
        if(dropped && targets.contains(targ))
        {
            targ.disable();
            playSfxAudio("drop",false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(locationForLine(currentLine, targ) ,targ))                                  ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            boolean complete = true;
            for(OBControl con : targets)
            {
                if(con.isEnabled())
                {
                    complete = false;
                    break;
                }
            }
            gotItRight();
            if(complete)
            {
                if(targets.size() > 1)
                    rearrangeTargets();
                targ.setZPosition(targ.zPosition()-10);
                displayTick();
                waitForSecs(0.3f);
                if(targets.size() > 1 && !OBUtils.getBooleanValue(eventAttributes.get("skip_drop")))
                    flyTargetsBack();
                nextScene();
            }
            else
            {
                targ.setZPosition(targ.zPosition()-10);
                setStatus(STATUS_WAITING_FOR_DRAG);
            }
        }
        else
        {
            if(dropped)
                gotItWrongWithSfx();
            flyControlBack(targ);
            setStatus(STATUS_WAITING_FOR_DRAG);
            if(dropped)
            {
                waitSFX();
                playAudioQueuedScene("INCORRECT",300,false);
            }
        }
    }

    public void startScene(boolean showLine) throws Exception
    {
        boolean touchMode = OBUtils.getBooleanValue(eventAttributes.get("touch_mode"));
        if(targets.size() == 1 && !touchMode && showLine)
        {
            showLine();
        }
        OBMisc.doSceneAudio(4, currentEvent(),
                touchMode?setStatus(STATUS_AWAITING_CLICK) :setStatus(STATUS_WAITING_FOR_DRAG), this);
    }

    public void showLine() throws Exception
    {
        currentLine.show();
        playSfxAudio("single_beep",true);
        waitForSecs(0.3f);
    }

    public void animateStart() throws Exception
    {
        List<OBAnim> anims  = new ArrayList<>();
        for(OBControl control: filterControls("obj_.*"))
        {
            PointF loc = OBMisc.copyPoint(control.position());
            anims.add(OBAnim.moveAnim(loc,control));
            if(control.attributes().get("start").equals("left"))
            {
                loc.x -= bounds().width();
                anims.add(OBAnim.rotationAnim((float)Math.toRadians(360) ,control));
            }
            else
            {
                loc.x += bounds().width();
                anims.add(OBAnim.rotationAnim((float)Math.toRadians(-360) ,control));
            }
            control.setPosition(loc);
            control.show();
        }
        playSfxAudio("shapes_on",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
    }

    public void animateDrop(boolean shrink) throws Exception
    {
        objectDict.get("bottombar").show();
        playSfxAudio("multi_pops",false);
        List<OBControl> objs = filterControls("obj_.*");
        objs = OBUtils.randomlySortedArray(objs);
        for(int i=0; i<objs.size(); i++)
        {
            OBControl control = objs.get(i);
            final List<OBAnim> anims  = new ArrayList<>();
            PointF loc = objectDict.get(control.attributes().get("drop")).position();
            control.setProperty("start_loc",OBMisc.copyPoint(loc));
            anims.add(OBAnim.moveAnim(loc,control));
            if(shrink)
                anims.add(OBAnim.scaleAnim(0.71f,control));
            control.setZPosition(10+i);
            control.enable();
            final OC_SectionController controller = this;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    OBAnimationGroup.runAnims(anims, 0.4, true, OBAnim.ANIM_EASE_IN_EASE_OUT, controller);
                }
                });
                waitForSecs(0.15f);

            }
            for(OBControl control : filterControls("obj_.*"))
            {
                control.setZPosition(10);
            }
            waitForSecs(1f);
            waitSFX();
        }

    public void flyControlBack(OBControl targ) throws Exception
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)targ.propertyValue("start_loc") ,targ))
                ,0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        targ.setZPosition(10);
    }

    public PointF locationForLine(OBControl line,OBControl targ)
    {
        PointF loc = OBMisc.copyPoint(line.position());
        if(targets.size() > 1)
        {
            loc.x = targ.position().x;
            if(targ.left() < currentLine.left())
            {
                loc.x = currentLine.left() + 0.5f*targ.width();
            }
            else if(targ.right() > currentLine.right())
            {
                loc.x = currentLine.right() - 0.5f*targ.width();
            }
        }
        else
        {
            loc = OBMisc.copyPoint(line.position());
        }
        loc.y -= targ.frame.height() * 0.5f + applyGraphicScale(10);
        return loc;
    }

    public void flyTargetsBack() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl targ : targets)
            anims.add(OBAnim.moveAnim((PointF)targ.propertyValue("start_loc") ,targ));
        OBAnimationGroup.runAnims(anims
                ,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        for(OBControl targ : targets)
        {
            targ.setZPosition(10);
            targ.enable();
        }
    }

    public void animateLinesJoin() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        OBControl line = objectDict.get("line");
        line.setOpacity(0);
        line.show();
        anims.add(OBAnim.propertyAnim("left",line.left(),objectDict.get("line_1")));
        anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.3f,0.5f,line.frame()) ,objectDict.get("line_2")));
        anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.4f,0.5f,line.frame()) ,objectDict.get("line_3")));
        anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.6f,0.5f,line.frame()) ,objectDict.get("line_4")));
        anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.7f,0.5f,line.frame()) ,objectDict.get("line_5")));
        anims.add(OBAnim.propertyAnim("right",line.right(),objectDict.get("line_6")));
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1,line)),0.5,true,OBAnim.ANIM_LINEAR,this);
        hideControls("line_.*");
    }

    public void rearrangeTargets() throws Exception
    {
        if(targets.get(0).frame().intersect(targets.get(1).frame()))
        {
            PointF loc1 = OBMisc.copyPoint(targets.get(0).position());
            PointF loc2 = OBMisc.copyPoint(targets.get(1).position());
            boolean swap = false;
            if(loc1.x < loc2.x)
                swap = true;
            loc1.x = OB_Maths.locationForRect(swap? 0.3f : 0.7f,0f,objectDict.get("line").frame()).x;
            loc2.x = OB_Maths.locationForRect(swap? 0.7f : 0.3f,0f,objectDict.get("line").frame()).x;
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc1,targets.get(0)) ,
                    OBAnim.moveAnim(loc2,targets.get(1))),0.25,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
    }

    public void demo5a() throws Exception
    {
        animateStart();
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.2f,0.9f,objectDict.get("obj_5") .frame()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.15f,1.1f,objectDict.get("obj_4") .frame()),-10,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(-0.05f,1f,objectDict.get("obj_6") .frame()),-5,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        animateDrop(true);
        waitForSecs(0.3f);
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.2f,0.9f,objectDict.get("obj_5") .frame()),-10,0.5f,"DEMO",3,0.3f);
        showLine();
        moveScenePointer(OB_Maths.locationForRect(0.6f,3f,objectDict.get("line_1") .frame()),-35,0.5f,"DEMO",4,0.5f);
        thePointer.hide();
        startScene(false);
    }

    public void demo5h() throws Exception
    {
        animateDrop(false);
        animateLinesJoin();
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.6f,3f,objectDict.get("line") .frame()),-15,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        startScene(false);
    }

    public void demo5m() throws Exception
    {
        List<OBAnim> anims =new ArrayList<>();
        for(OBControl con : filterControls("obj_.*"))
        {
            if(!targets.contains(con))
            {
                anims.add(OBAnim.opacityAnim(0,con));
            }
        }
        anims.add(OBAnim.opacityAnim(0,objectDict.get("bottombar")));
        anims.add(OBAnim.opacityAnim(0,objectDict.get("line")));
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_LINEAR,this);
        PointF loc1 = OBMisc.copyPoint(targets.get(0).position());
        PointF loc2 = OBMisc.copyPoint(targets.get(1).position());
        boolean swap = false;
        if(loc1.x < loc2.x)
            swap = true;
        anims.clear();
        anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.33f,0.5f,this.bounds()) ,targets.get(swap?0:1)));
        anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(0.66f,0.5f,this.bounds()) ,targets.get(swap?1:0)));
        for(OBControl con : targets)
        {
            anims.add(OBAnim.propertyAnim("bottom",this.bounds().height()*0.7f,con));
            anims.add(OBAnim.scaleAnim(1.7f,con));
        }
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        loadPointer(POINTER_MIDDLE);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.73f,this.bounds()),-10,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.05f,targets.get(swap?0:1).frame()),-15,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.05f,targets.get(swap?1:0).frame()),-5,0.5f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.2f,1.2f,targets.get(swap?1:0).frame()),0,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        nextScene();
    }

    public void demoFin5o() throws Exception
    {
        final OBPath square =(OBPath)objectDict.get("obj_4");
        OBPath rectangle =(OBPath)objectDict.get("obj_3");
        final float startHeight = square.height();
        final float targetHeight = rectangle.height();
        final float difHeight = targetHeight - startHeight;
        final RectF startBounds = square.bounds();
        square.setAnchorPoint(new PointF(0.5f, 1));
        final PointF startPoint = square.position();
        for(int i=0; i<6; i++)
        {
            playSfxAudio(i%2==0?"stretch":"shrink",false);
            final int j = i;
            OBAnim anim = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float height = j%2==0? startHeight + frac*difHeight : targetHeight - frac*difHeight;
                    RectF bounds = new RectF(startBounds.left, startBounds.top,
                            startBounds.right, startBounds.top + height/square.scale());
                    Path path = new Path();
                    path.addRect(bounds, Path.Direction.CCW);
                    square.setPath(path);
                    square.setBounds(bounds);
                    square.sizeToBoundingBoxIncludingStroke();
                    square.setAnchorPoint(new PointF(0.5f, 1));
                    square.setPosition(startPoint);
                }
            };
            OBAnimationGroup.runAnims(Arrays.asList(anim),0.5f ,true,
                    OBAnim.ANIM_EASE_IN_EASE_OUT,null, this);
            waitSFX();
            waitForSecs(0.3f);
        }
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.05f,square.frame()),-30,
                0.5f,"FINAL",0,0.3f);
        playAudioScene("FINAL",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
    }



}