package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
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
import java.util.concurrent.ThreadFactory;

/**
 * Created by michal on 07/04/2017.
 */

public class OC_MoreNumbers_S5e extends OC_SectionController
{
    boolean starMode;
    OBGroup currentControl;
    List<OBControl> dragTargets, dropTargets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master5e");
        dragTargets = new ArrayList<>();
        dropTargets = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5e();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        dragTargets.clear();
        dropTargets.clear();
        starMode = eventAttributes.get("target").equals("star");
        for(int i=1; i<=4; i++)
        {
            OBGroup control = (OBGroup)objectDict.get(String.format("obj_%d",i));
            if(control == null)            break;
            int standardColour = OBUtils.colorFromRGBString((String)control.attributes().get("colour_background"));
            control.substituteFillForAllMembers("colour_.*", standardColour);
            if(starMode)
            {
                control.setProperty("startcol",standardColour);
                control.setProperty("hilight_control","colour_.*");
            }
            else
            {
                control.setProperty("startcol", Color.WHITE);
                control.setProperty("hilight_control","background.*");
            }
            control.setProperty("hilighted",false);
            control.setProperty("snaploc", OB_Maths.locationForRect(0.5f,(starMode ? 0.55f : 0.5f),control.frame()));
            control.setZPosition ( 2);
            PointF loc = OBMisc.copyPoint(control.position());
            loc.x =  loc.x - bounds().width();
            control.setPosition ( loc);
            control.show();
            dropTargets.add(control);

        }
        String[] nums= eventAttributes.get("num").split(",");
        float fontSize = applyGraphicScale(115);
        int index = 1;
        for(String num : nums)
        {
            OBLabel label = new OBLabel(num,OBUtils.standardTypeFace(),fontSize);
            label.setColour(Color.BLACK);
            OBGroup groupNum = new OBGroup(Arrays.asList((OBControl)label));
            groupNum.sizeToTightBoundingBox();
            groupNum.objectDict.put("label",label);
            groupNum.setPosition(OB_Maths.locationForRect((1.0f/(nums.length+1))*index++,0.5f,objectDict.get("bottombar").frame()));
            groupNum.setProperty("startloc",OBMisc.copyPoint(groupNum.position()));
            attachControl(groupNum);
            groupNum.hide();
            groupNum.setZPosition(3);
            groupNum.setProperty("num_value",num);
            dragTargets.add(groupNum);
        }
    }

    public void doMainXX() throws Exception
    {
        animateScene(true);
        startScene();
    }

    public void fin()
    {
        try
        {
            goToCard(OC_MoreNumbers_S5k.class, "event5");
        }
        catch (Exception e)
        {

        }
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {

        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl dragTarget = finger(-1,0,dragTargets,pt);
            if(dragTarget != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget(dragTarget, pt);

                    }
                });

            }

        }

    }


    public void touchUpAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
            final OBGroup groupNum = (OBGroup)target;
            target = null;
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDropTarget(groupNum);
                }
            });
        }



    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
            checkForHilight(target.position());
        }
    }



    public void checkTarget(OBControl dragTarget, PointF pt) throws Exception
    {
        OBMisc.prepareForDragging(dragTarget,pt,this);
        setStatus(STATUS_DRAGGING);
    }

    public void checkDropTarget(OBGroup groupNum) throws Exception
    {
        if(currentControl != null)
        {
            colourObj(currentControl, false);
            if(currentControl.attributes().get("num") != null &&
                    currentControl.attributes().get("num").equals(groupNum.settings.get("num_value")))
            {
                gotItRightBigTick(false);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)currentControl.propertyValue("snaploc"),groupNum)  )
                        ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                currentControl.setProperty("attached_control",groupNum);
                dragTargets.remove(groupNum);
                dropTargets.remove(currentControl);
                currentControl = null;
                if(dragTargets.size() == 0)
                {
                    waitSFX();
                    waitForSecs(0.2f);
                    displayTick();
                    waitForSecs(0.2f);
                    if(!performSel("demoFin",currentEvent()))
                        demoFinStandard();
                    animateScene(false);
                    waitForSecs(0.3f);
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
                currentControl = null;
                flyObjBack(groupNum);
                waitSFX();
                playAudioQueuedScene("INCORRECT",300,false);
                setStatus(STATUS_WAITING_FOR_DRAG);
            }

        }
        else
        {
            flyObjBack(groupNum);
            setStatus(STATUS_WAITING_FOR_DRAG);

        }

    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(-1,setStatus(STATUS_WAITING_FOR_DRAG),this);
    }

    public void animateScene(boolean start) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        if(starMode)
        {
            List<Float> yLocs = Arrays.asList(0.5f,0.7f,0.4f);
            float dist = 0.3f * bounds().height();
            for(int i=1; i<=3; i++)
            {
                OBGroup obj = (OBGroup)objectDict.get(String.format("obj_%d",i));
                PointF startPoint = OBMisc.copyPoint(obj.position());
                PointF endPoint = OBMisc.copyPoint(startPoint);
                endPoint.x += bounds().width();
                if(start)
                {
                    startPoint.y = OB_Maths.locationForRect(0,yLocs.get(i-1), bounds()).y;
                    obj.setPosition(startPoint);

                }
                else
                {
                    endPoint.y = (float)OB_Maths.locationForRect(0,yLocs.get(i-1), bounds()).y;
                }
                Path path = new Path();
                path.moveTo(obj.position().x, obj.position().y);
                PointF cp1 = new PointF(startPoint.x, startPoint.y - dist);
                PointF cp2 = new PointF(endPoint.x, endPoint.y - dist);
                path.cubicTo(cp1.x, cp1.y,cp2.x, cp2.y, endPoint.x, endPoint.y);
                anims.add(OBAnim.pathMoveAnim(obj,path,false,0));
                if(!start)
                    anims.add(OBMisc.attachedAnim(obj, Arrays.asList((OBControl)obj.propertyValue("attached_control"))));

            }

        }
        else
        {
            List<Float> yLocs = Arrays.asList(0.4f,0.8f,0.2f,0.6f);
            for(int i=1; i<=4; i++)
            {
                OBGroup obj = (OBGroup)objectDict.get(String.format("obj_%d",i));
                PointF startPoint = OBMisc.copyPoint(obj.position());
                PointF endPoint = OBMisc.copyPoint(startPoint);
                endPoint.x += bounds().width();
                if(start)
                {
                    startPoint.y = OB_Maths.locationForRect(0, yLocs.get(i-1), bounds()).y;
                    obj.setPosition(startPoint);

                }
                else
                {
                    endPoint.y = OB_Maths.locationForRect(0, yLocs.get(i-1), bounds()).y;

                }
                anims.add(OBAnim.moveAnim(endPoint,obj));
                anims.add(OBAnim.rotationAnim((float)Math.toRadians(-360),obj));
                if(!start)
                    anims.add(OBMisc.attachedAnim(obj,Arrays.asList((OBControl)obj.propertyValue("attached_control"))));

            }

        }
        if(start)
            playSfxAudio("scene_change",false);
        OBAnimationGroup.runAnims(anims,1,true,(start ?  OBAnim.ANIM_EASE_OUT :  OBAnim.ANIM_EASE_IN),this);
        if(start)
        {
            waitSFX();
            waitForSecs(0.3f);
            int index = 1;
            for(OBControl groupNum : dragTargets)
            {
                groupNum.show();
                playSfxAudio(String.format("note_%d",index++),true);
            }

        }
        else
        {
            for(OBControl obj : filterControls("obj_.*"))
            {
                detachControl((OBControl)obj.propertyValue("attached_control"));
                detachControl(obj);

            }

        }
    }

    public void flyObjBack(OBGroup groupNum)
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)groupNum.propertyValue("startloc"),groupNum))
            ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        groupNum.setZPosition(groupNum.zPosition()-10);
    }

    public void checkForHilight(PointF pt)
    {
        OBGroup cont = (OBGroup)finger(-1,-1,dropTargets,pt);
        if(currentControl != cont)
        {
            if(cont == null)
            {
                colourObj(currentControl,false);
                currentControl = null;

            }
            else
            {
                if(currentControl != null)
                    colourObj(currentControl,false);
                    colourObj(cont,true);
                currentControl = cont;
            }
        }
    }


    public void colourObj(OBGroup obj, boolean hilight)
    {
        if(obj.propertyValue("hilighted") != null
                || (boolean)obj.propertyValue("hilighted") != hilight)
        {
            int colour = (int)obj.propertyValue("startcol");
            if(hilight)
            {
                obj.substituteFillForAllMembers((String)obj.propertyValue("hilight_control"), OBUtils.highlightedColour(colour));
                obj.setProperty("hilighted",true);
            }
            else
            {
                obj.substituteFillForAllMembers((String)obj.propertyValue("hilight_control"), colour);
                obj.setProperty("hilighted",false);

            }

        }

    }
    public void demo5e() throws Exception
    {
        waitForSecs(0.2f);
        objectDict.get("bottombar").show();
        playSfxAudio("ping",true);
        waitForSecs(0.5f);
        animateScene(true);
        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        final OBControl drag = dragTargets.get(1);
        OBControl drop = dropTargets.get(0);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.7f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,drag.frame()),-30,0.5f,true);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF) drop.propertyValue("snaploc"), drag),
            OBMisc.attachedAnim(drag, Arrays.asList(thePointer)),
            new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    checkForHilight(drag.position());
                }
            }),0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        playAudio("correct");
        waitAudio();
        colourObj((OBGroup)drop,false);
        movePointerToPoint(OB_Maths.locationForRect(1.5f,1.5f,drag.frame()),-30,0.5f,true);
        drop.settings.put("attached_control",drag);
        dragTargets.remove(drag);
        dropTargets.remove(drop);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();

    }

    public void pointerPointObj(int num,int audioIndex) throws Exception
    {
        OBGroup obj = (OBGroup)objectDict.get(String.format("obj_%d",num));
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.2f,obj.frame())
                ,((1-(obj.position().x/bounds().width()*1.0f))*-40.0f)-10
                ,0.4f,true);
        playAudioScene("FINAL",audioIndex,true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
    }

    public void pointerPointObjNum(int num,int audioIndex, int numCount) throws Exception
    {
        OBGroup obj = (OBGroup)objectDict.get(String.format("obj_%d",num));
        movePointerToPoint(OB_Maths.locationForRect(0.7f,0.8f,obj.frame())
                ,((1-(obj.position().x/bounds().width()*1.0f))*-40.0f)-10
                ,0.5f,true);
        if(numCount >=0)
        {
            OBGroup attached = (OBGroup)obj.propertyValue("attached_control");
            OBLabel label = (OBLabel)attached.objectDict.get("label");
            for(int i=1; i<=numCount; i++)
            {
                label.setHighRange(i-1, i, Color.RED);
                playAudioScene("FINAL",audioIndex+i-1,true);
                waitForSecs(0.3f);
                lockScreen();
                label.setHighRange(-1, -1, Color.BLACK);
                label.setColour(Color.BLACK);
                unlockScreen();
            }

        }
        else
        {
            playAudioScene("FINAL",audioIndex,true);
            waitForSecs(0.3f);

        }

    }

    public void demoFin5e() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointObjNum(1,0,-1);
        pointerPointObjNum(2,1,1);
        pointerPointObjNum(3,2,1);
        pointerPointObj(3,3);

    }

    public void demoFinStandard() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointObjNum(1,0,1);
        pointerPointObjNum(2,1,1);
        pointerPointObjNum(3,2,1);
        pointerPointObj(3,3);
    }

    public void demoFin5h() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointObjNum(1,0,2);
        pointerPointObjNum(2,2,2);
        pointerPointObjNum(3,4,1);
        pointerPointObjNum(4,5,1);
        pointerPointObj(4,6);

    }

    public void demoFin5i() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointObjNum(1,0,-1);
        pointerPointObjNum(2,1,2);
        pointerPointObjNum(3,3,2);
        pointerPointObjNum(4,5,1);
        pointerPointObj(4,6);

    }

    public void demoFin5j() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointObjNum(1,0,1);
        pointerPointObjNum(2,1,1);
        pointerPointObjNum(3,2,1);
        pointerPointObjNum(4,3,1);
        pointerPointObj(4,4);
    }




}
