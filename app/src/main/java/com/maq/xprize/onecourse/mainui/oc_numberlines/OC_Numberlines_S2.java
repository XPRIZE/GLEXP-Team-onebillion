package com.maq.xprize.onecourse.mainui.oc_numberlines;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 12/04/2017.
 */

public class OC_Numberlines_S2 extends OC_SectionController
{
    List<OBControl> dragTargets;
    List<Integer> eventTargets;

    public float graphicScale()
    {
        return bounds().width()/1024.0f;
    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        for(OBControl control : filterControls("peg.*"))
        {
            ((OBGroup)control).objectDict.get("open").show();
            ((OBGroup)control).objectDict.get("close").hide();
        }

        OC_Numberlines_Additions.setUpBasket(this);
        dragTargets = new ArrayList<>();
        float fontSize = 50*objectDict.get("numbox0").height()/62.0f;
        for(int i=0; i<=10; i++)
        {
            OBLabel num = new OBLabel(String.format("%d",i), OBUtils.standardTypeFace(),fontSize);
            OBControl cont = objectDict.get(String.format("numbox%d", i));
            num.setPosition(cont.position());
            cont.show();
            num.setColour(Color.BLACK);
            cont.setZPosition(2);
            num.setZPosition(2.1f);

            OBGroup group = new OBGroup(Arrays.asList(num,cont));
            attachControl(group);
            objectDict.put(String.format("numbox%d", i),group);
            group.show();
            group.setZPosition(1.2f);
            group.setProperty("startpos", OBMisc.copyPoint(group.position()));
            group.setPosition(OB_Maths.locationForRect(0.5f,0.75f,objectDict.get("box_front").frame()));
            group.setRotation((float)Math.toRadians(i*10));
            group.setProperty("target",String.format("peg%d",i+1));
            group.setProperty("value",i);
            dragTargets.add(group);
        }
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                setSceneXX(currentEvent());
                waitForSecs(0.2f);
                walkMomIn();
                demo2a();
                startScene();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        if(eventAttributes.get("nums") != null)
            eventTargets = OBMisc.stringToIntegerList(eventAttributes.get("nums"),",");

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        try
        {
            List<OBAnim> arr = new ArrayList<>();
            for(OBControl cont : objectDict.values())
            {
                arr.add(OBAnim.opacityAnim(0,cont));
            }
            objectDict.get("box_back").hide();
            OBAnimationGroup.runAnims(arr,0.5,true, OBAnim.ANIM_LINEAR,this);
        }
        catch (Exception e)
        {

        }
        goToCard(OC_Numberlines_S2f.class,"event2");
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_WAITING_FOR_DRAG && eventAttributes.get("target").equals("num"))
        {
            final OBControl cont = finger(0,1,dragTargets,pt);
            if(cont != null )
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDrag(cont, pt);
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("box"))
        {
            if(finger(0,1,filterControls("box_.*"),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        throwNumbers();
                        demoFin2b();
                        nextScene();
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("wheel"))
        {
            if(finger(0,0,Arrays.asList(((OBGroup)objectDict.get("pole1")).objectDict.get("wheel2")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        pulleyAnimate();
                        walkMomOut();
                        nextScene();
                    }
                });
            }
        }
    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target != null)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void touchUpAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop();
                }
            });
        }

    }

    public void checkDrag(OBControl cont, PointF pt) throws Exception
    {
        playSfxAudio("drag",false);
        OBMisc.prepareForDragging(cont, pt, this);
        setStatus(STATUS_DRAGGING);
    }

    public  void checkDrop() throws Exception
    {
        playAudio(null);
        OBControl cont = target;
        target =  null;
        OBGroup peg = (OBGroup)objectDict.get(cont.settings.get("target"));
        RectF hotRect = new RectF(peg.frame());
        hotRect.inset(-1.5f* peg.width(), -0.5f * peg.height());
        if(eventTargets.contains(cont.propertyValue("value")) && RectF.intersects(cont.frame(),hotRect))
        {
            float left = peg.position().x - (cont.width()/2);
            if(cont.top() < peg.bottom())
            {
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",left,cont),
                        OBAnim.propertyAnim("top",peg.bottom()+peg.height(),cont))
                        ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                peg.setZPosition(11);
                cont.setZPosition(3);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("top",objectDict.get("rope").bottom(),cont))
                        ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            }
            else
            {
                peg.setZPosition(11);
                cont.setZPosition(3);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",left,cont),
                        OBAnim.propertyAnim("top",objectDict.get("rope").bottom(),cont))
                        ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            }
            closePeg(peg);
            gotItRight();
            eventTargets.remove(cont.settings.get("value"));
            dragTargets.remove(cont);
            if(eventTargets.size() == 0)
            {
                waitForSecs(0.2f);
                displayTick();
                waitForSecs(0.2f);
                performSel("demoFin",currentEvent());
                nextScene();
            }
            else
            {
                setStatus(STATUS_WAITING_FOR_DRAG);
            }
        }
        else
        {
            OBControl rope = objectDict.get("rope");
            RectF rect = new RectF(cont.frame());
            rect.inset( 0, -4*rope.height());
            boolean remind = RectF.intersects(cont.frame(),rect);
            gotItWrongWithSfx();
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)cont.propertyValue("startpos"),cont))
                    ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            if(remind)
                playAudioQueuedScene("INCORRECT",0.3f,false);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }


    public void closePeg(OBGroup peg) throws Exception
    {
        playSfxAudio("peg_close",false);
        lockScreen();
        peg.objectDict.get("open").hide();
        peg.objectDict.get("close").show();
        unlockScreen();
        waitSFX();
    }


    public void startScene() throws Exception
    {
        if(eventAttributes.get("target").equals("num"))
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
        }
    }


    public void walkMomIn() throws Exception
    {
        OBGroup mom = (OBGroup)objectDict.get("mom");
        PointF loc = OBMisc.copyPoint(mom.getWorldPosition());
        mom.setRight(0);
        mom.show();
        playAudioQueued(Arrays.asList((Object)getAudioForSceneIndex("sfx","footstep",0), 100),false, -1);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc,mom),
                OBAnim.sequenceAnim(mom,OBUtils.getFramesList("walk", 1, 8),0.05f,true))
                ,0.5,true,OBAnim.ANIM_LINEAR,this);
        playAudio(null);
        lockScreen();
        OBGroup pulley = (OBGroup)objectDict.get("pole1");
        mom.hide();
        pulley.objectDict.get("mom").show();
        unlockScreen();
    }

    public void walkMomOut() throws Exception
    {
        OBGroup mom = (OBGroup)objectDict.get("mom");
        lockScreen();
        mom.show();
        mom.flipHoriz();
        OBGroup pulley = (OBGroup)objectDict.get("pole1");
        pulley.objectDict.get("mom").hide();

        unlockScreen();
        playAudioQueued(Arrays.asList((Object)getAudioForSceneIndex("sfx","footstep",0), 100),false, -1);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("right",0,mom),
                OBAnim.sequenceAnim(mom,OBUtils.getFramesList("walk", 1, 8),0.05f,true))
                ,0.5f,true,OBAnim.ANIM_LINEAR,this);
        playAudio(null);
    }

    public void pulleyAnimate() throws Exception
    {
        OBGroup pulley = (OBGroup)objectDict.get("pole1");
        OBGroup mom = (OBGroup)pulley.objectDict.get("mom");
        lockScreen();
        pulley.objectDict.get("starthand").hide();
        pulley.objectDict.get("startrope").hide();
        pulley.objectDict.get("hand6").show();

        unlockScreen();
        List<OBControl> objs = new ArrayList<>();

        objs.addAll(filterControls("peg.*"));
        objs.addAll(filterControls("npeg.*"));
        objs.addAll(filterControls("obj.*"));
        objs.add(objectDict.get("rope"));
        float dist = objectDict.get("peg2").left() - objectDict.get("peg1").left();
        List<OBAnim> objAnims1 = new ArrayList<>();
        List<OBAnim> objAnims2 = new ArrayList<>();
        for(OBControl cont : filterControls("obj.*"))
        {
            objAnims1.add(OBAnim.rotationAnim((float)Math.toRadians(-3),cont));
            objAnims2.add(OBAnim.rotationAnim((float)Math.toRadians(0),cont));
        }
        OBGroup pulley2 = (OBGroup)objectDict.get("pole2");
        waitForSecs(0.2f);
        for(int i=0; i<12; i++)
        {
            lockScreen();
            pulley.objectDict.get("hand6").hide();
            pulley.objectDict.get("hand1").show();
            unlockScreen();
            playSfxAudio("pulley",false);
            List<OBAnim> moveAnims = new ArrayList<>();
            moveAnims.add(getWheelAnim(pulley.objectDict.get("wheel1"),180));
            moveAnims.add(getWheelAnim(pulley.objectDict.get("wheel2"),90));
            moveAnims.add(getWheelAnim(pulley2.objectDict.get("wheel"),180));
            for(OBControl cont : objs)
            {
                moveAnims.add(OBAnim.moveAnim(new PointF(cont.position().x-dist, cont.position().y),cont));
            }
            OBAnimationGroup.runAnims(moveAnims,0.24,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            OBAnimationGroup.runAnims(objAnims1,0.12,false,OBAnim.ANIM_EASE_IN,this);
            animateFrames(Arrays.asList("hand1", "hand2", "hand3"),0.08f,pulley);
            OBAnimationGroup.runAnims(objAnims2,0.12,true,OBAnim.ANIM_EASE_IN,this);
            waitForSecs(0.15f);
            animateFrames(Arrays.asList("hand3","hand4", "hand5", "hand6"),0.08f,pulley);
        }
        waitForSecs(0.1f);
        lockScreen();
        pulley.objectDict.get("starthand").show();
        pulley.objectDict.get("startrope").show();
        pulley.objectDict.get("hand6").hide();
        unlockScreen();
        waitForSecs(0.1f);
    }

    public void throwNumbers() throws Exception
    {
        playSfxAudio("basket_open",false);
        animateFrames(Arrays.asList("frame1", "frame2", "frame3"),0.1f,(OBGroup)objectDict.get("box_top"));
        playSFX(null);
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl control : filterControls("numbox.*"))
        {
            PointF endPoint = (PointF)control.settings.get("startpos");
            Path path = new Path();
            path.moveTo(control.position().x, control.position().y);
            PointF cp1 = OB_Maths.locationForRect(0.5f,-1.8f,objectDict.get("box_front").getWorldFrame());
            PointF cp2 = OB_Maths.AddPoints(endPoint ,new PointF(applyGraphicScale(5),applyGraphicScale(5)));
            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, endPoint.x, endPoint.y);
            anims.add(OBAnim.pathMoveAnim(control,path,false,0));
            anims.add(OBAnim.rotationAnim((float)Math.toRadians(360),control));
        }

        playSfxAudio("numbers_fly_out",false);
        OBAnimationGroup.runAnims(anims,3,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(1f);
        lockScreen();

        for(OBControl control : filterControls("numbox.*"))
        {
            control.setZPosition(3);
        }

        unlockScreen();
        waitForSecs(2.2f);
        playSFX(null);
        animateFrames(Arrays.asList("frame3", "frame2", "frame1"),0.1f,(OBGroup)objectDict.get("box_top"));
    }


    public OBAnim getWheelAnim(OBControl cont, float degrees)
    {
        return OBAnim.rotationAnim(cont.rotation()-(float)Math.toRadians(degrees),cont);
    }

    public void demo2a() throws Exception
    {
        waitForSecs(0.2f);
        loadPointer(POINTER_LEFT);
        OBGroup pole = (OBGroup)objectDict.get("pole1");
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.6f,pole.objectDict.get("wheel2").getWorldFrame()),-30,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
    }

    public void demoFin2b() throws Exception
    {
        waitForSecs(0.2f);
        loadPointer(POINTER_LEFT);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        playAudioScene("FINAL",1,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.5f,objectDict.get("numbox0").frame()),-45,0.5f,"FINAL",2,0.3f);
        demoDrag(objectDict.get("numbox0"),0.2f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("numbox0").frame()),-30,0.2f,"FINAL",3,0.3f);
        demoDrag(objectDict.get("numbox6"),0.6f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("numbox6").frame()),-30,0.2f,"FINAL",4,0.3f);
        demoDrag(objectDict.get("numbox10"),0.6f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,objectDict.get("numbox10").frame()),-30,0.2f,"FINAL",5,0.5f);
        thePointer.hide();
    }

    public void demoFin2d() throws Exception
    {
        waitForSecs(0.2f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f, 0.6f), bounds()),-30,0.3f,"FINAL",0,0.3f);
        playAudioScene("FINAL",1,true);
        waitForSecs(0.6f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demoDrag(OBControl box, float duration) throws Exception
    {
        movePointerToPoint(box.position(),-45,duration,true);
        playSfxAudio("drag",false);
        OBGroup peg = (OBGroup)objectDict.get(box.settings.get("target"));
        float left = peg.position().x - (box.width()/2);
        peg.setZPosition ( 5);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",left,box),
                OBAnim.propertyAnim("top",objectDict.get("rope").bottom(),box),
                OBAnim.moveAnim(new PointF(peg.position().x,objectDict.get("rope").bottom() + (box.height()/2)),thePointer),
                OBAnim.rotationAnim((float)Math.toRadians(-30),thePointer))
        ,0.7f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        closePeg(peg);
        dragTargets.remove(box);
        waitForSecs(0.2f);
    }

}
