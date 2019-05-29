package com.maq.xprize.onecourse.hindi.mainui.oc_patternwordsyl;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimBlock;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.UCurve;
import com.maq.xprize.onecourse.hindi.utils.ULine;
import com.maq.xprize.onecourse.hindi.utils.UPath;
import com.maq.xprize.onecourse.hindi.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 15/01/2018.
 */

public class OC_WordBag
{
    public OBGroup control;
    public OBControl mask;

    OC_SectionController controller;
    List<OBGroup> dots;
    List<Integer> dotColours;
    boolean dotsAnimated;
    boolean highlighted;

    public OC_WordBag(OBGroup group,OC_SectionController sectionController)
    {
        control = group;
        controller = sectionController;
        dots = new ArrayList<>();
        dotColours = new ArrayList<>();
        for(OBControl dotsGroup : control.filterMembers("dots_.*"))
        {
            dots.add((OBGroup)dotsGroup);
            OBPath dot = (OBPath)((OBGroup) dotsGroup).members.get(0);
            dotColours.add(dot.fillColor());
        }

        RectF maskTopFrame = control.objectDict.get("mask").getWorldFrame();
        OBControl maskTop = control.objectDict.get("mask").copy();
        maskTop.setScale(control.scale());
        OBControl maskBg = new OBControl();
        maskBg.setFrame(maskTopFrame.left, maskTopFrame.top, maskTopFrame.right, sectionController.bounds().height());
        maskBg.setBackgroundColor(Color.BLACK);
        maskBg.setZPosition(1);
        mask = new OBGroup(Arrays.asList(maskBg, maskTop));
        maskTop.setLeft(0);
        maskTop.setTop(0);
        maskTop.show();
        maskBg.setReversedMaskControl(maskTop);

        mask.setZPosition(50);
        control.setZPosition(2);
        dotsAnimated = false;
        OBPath bg =(OBPath)control.objectDict.get("colour_body");
        bg.setProperty("lowlight_colour",bg.fillColor());
        highlighted = false;
    }

    public void setDotsGroup(OBGroup dotsGroup,int colour) throws Exception
    {
        for(OBControl path : dotsGroup.members)
        {
            ((OBPath)path).setFillColor(colour);
        }
    }

    public void changeDotsColours(int step) throws Exception
    {
        controller.lockScreen();
        for(int i=0; i<dots.size(); i++)
        {
            OBGroup dotsGroup = dots.get(i);
            int colour = dotColours.get((i+step) % dotColours.size());
            setDotsGroup(dotsGroup,colour);
        }
        controller.unlockScreen();
    }

    public void changeDotsColours2(int step) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(int i=0; i<dots.size(); i++)
        {
            OBGroup dotsGroup = dots.get(i);
            int colour = dotColours.get((i+step) % dotColours.size());
            for(OBControl path : dotsGroup.members)
            {
                anims.add(OBAnim.colourAnim("fillColor",colour,path));
            }
        }
        OBAnimationGroup.runAnims(anims,0.05,true,OBAnim.ANIM_LINEAR,controller);
    }

    public void startDotsAnimation2() throws Exception
    {
        dotsAnimated = true;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                int index = 0;
                while (dotsAnimated && controller != null && !controller._aborting)
                {
                    index++;
                    changeDotsColours(index);
                    if (controller != null)
                        controller.waitForSecs(0.15);
                    if (index > 1000) index = 0;
                }
            }
        });
    }

    public void startDotsAnimation() throws Exception
    {
        if (dotsAnimated)
            return;
        dotsAnimated = true;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                int index = 0;
                while (dotsAnimated && controller != null &&
                        !controller._aborting)
                {
                    index++;
                    changeDotsColours2(index);
                    if (controller != null)
                        controller.waitForSecs(0.15);
                    if (index > 1000)
                        index = 0;
                }
            }
        });
    }

    public void stopDotsAnimation() throws Exception
    {
        dotsAnimated = false;
    }

    public void prepareWithObjects(List<OBControl> objects)
    {
        List<Integer> startRotations = OBUtils.randomlySortedArray(Arrays.asList(-15,15,10,-10,90));
        int index = 0;
        List<OBControl> reversedObjects = new ArrayList<>(objects);
        Collections.reverse(reversedObjects);
        for(OBControl cont :reversedObjects)
        {
            cont.setReversedScreenMaskControl(mask);
            cont.setProperty("start_scale",cont.scale());
            cont.setPosition(control.position());
            if(cont.width() > control.width()*0.9f)
                cont.setScale(cont.scale()*control.width()*0.9f/cont.width());
            int rotation = 0;
            if(index < startRotations.size())
            {
                rotation = startRotations.get(index).intValue();
            }
            else
            {
                rotation = OB_Maths.randomInt(90, 180) -90;
            }
            float height = cont.height()/3.0f;
            float moveUp = (float)(Math.abs(rotation)>45 ? 0 : Math.abs(height/Math.cos(Math.toRadians(rotation))));
            cont.setRotation((float)Math.toRadians(rotation));
            float dist = cont.left() - control.left() - control.width() * 0.05f;
            if(index < startRotations.size())
            {
                cont.setTop(control.top() - control.height()*0.02f - moveUp);
                if(rotation != 90)
                    cont.setLeft (cont.left() +((rotation>0 ? -1 : 1) * dist * Math.abs(rotation)/30.0f));
            }
            else
            {
                cont.setTop(control.top() - control.height()*0.02f - moveUp + OB_Maths.randomInt((int)(0.05*control.height()), (int)(0.2*control.height())));
                cont.setLeft(cont.left()+(((OB_Maths.randomInt(0, 1) *2) -1) * OB_Maths.randomInt(0, Math.abs((int) dist))));
            }
            index++;
            cont.setProperty("bag_loc", OBMisc.copyPoint(cont.position()));
            cont.setProperty("bag_rotation",cont.rotation());
            cont.setProperty("bag_scale",cont.scale());
            cont.setTop(control.top() + 0.4f/(OB_Maths.randomInt(1, 3)) * control.height());
            cont.hide();
        }
    }

    public void animateFillWithObjects(List<OBControl> objects, boolean finishFlashing, String sfx) throws Exception
    {
        final float shakeDist = control.width()*0.025f;
        final float startLeft = control.left();
        final int shakeCount = 10;
        List<OBAnim> anims = new ArrayList<>();
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float frac2 =(float)Math.sin(frac*shakeCount*Math.PI);
                control.setLeft(startLeft + shakeDist *frac2);
            }
        };
        anims.add(blockAnim);
        for(OBControl con : objects)
        {
            con.show();
            anims.add(OBAnim.moveAnim((PointF)con.propertyValue("bag_loc") ,con));
        }
        if(sfx != null)
            controller.playSfxAudio(sfx,false);
        startDotsAnimation();
        OBAnimationGroup.runAnims(anims,0.75,true,OBAnim.ANIM_LINEAR,controller);
        if(sfx != null)
            controller.waitSFX();
    }

    public void flyObjects(List<OBControl> objs ,boolean outside,boolean together,String sfx) throws Exception
    {
        controller.playSfxAudio(sfx,false);

        if(!outside)
        {
            controller.lockScreen();
            for(OBControl obj : objs)
                obj.setReversedScreenMaskControl(mask);
            controller.unlockScreen();
        }
        List<OBControl> objsArray =  new ArrayList<>(objs);
        if(outside)
            Collections.reverse(objsArray);
        List<OBAnim> allAnims = new ArrayList<>();
        startDotsAnimation();
        float maskWidth =(control.objectDict.get("mask")).getWorldFrame().width();
        for(int i = 0; i < objsArray.size(); i++)
        {
            OBControl obj = objsArray.get(i);
            PointF bagPoint1 =  (PointF)obj.propertyValue("bag_loc");
            PointF bagPoint2 =  OB_Maths.locationForRect(0.5f,-0.5f,control.frame());
            List<OBAnim> anims = new ArrayList<>();
            PointF pathStart = outside ? bagPoint2 : obj.position();
            PointF pathEnd = outside ? (PointF)obj.settings.get("drop_loc") : bagPoint2;

            PointF cp1 = OBMisc.copyPoint(pathStart);
            cp1.y -= 2*control.height();
            PointF cp2 = OBMisc.copyPoint(pathEnd);
            cp2.y -= 2*control.height();
            UCurve curve = new UCurve(pathStart.x,pathStart.y,pathEnd.x,pathEnd.y,cp1.x,cp1.y,cp2.x,cp2.y);
            USubPath subPath = new USubPath();
            if(outside)
            {
                ULine line = new ULine(obj.position().x,obj.position().y,pathStart.x,pathStart.y);
                subPath.elements.add(line);
                subPath.elements.add(curve);
            }
            else
            {
                ULine line = new ULine(pathEnd.x,pathEnd.y,bagPoint1.x,bagPoint1.y);
                subPath.elements.add(curve);
                subPath.elements.add(line);
            }

            UPath uPath = new UPath();
            uPath.subPaths.add(subPath);
            float rotation = 0;
            if(!outside)
                rotation = (float)obj.propertyValue("bag_rotation");
            anims.add(OBAnim.pathMoveAnim(obj,uPath.bezierPath(),false,0));
            anims.add(OBAnim.rotationAnim((float)Math.toRadians(360) * OB_Maths.randomInt(1, 2)+rotation,obj));
            anims.add(OBAnim.scaleAnim(outside ? (float)obj.propertyValue("start_scale") : (float)obj.propertyValue("bag_scale"),obj));
            if(!together)
            {
                OBAnimationGroup.runAnims(anims,0.6,i == objsArray.size()-1,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);
                stopDotsAnimation();
            }
            else
            {
                allAnims.addAll(anims);
            }
            if(!together && i != objsArray.size()-1)
                controller.waitForSecs(0.1);
        }
        if(together)
        {
            OBAnimationGroup.runAnims(allAnims,0.8,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);
        }
        if(outside)
        {
            controller.lockScreen();
            for(OBControl obj : objs)
                obj.setMaskControl(null);
            controller.unlockScreen();
        }
        stopDotsAnimation();
    }

    public void highlight()
    {
        if(highlighted)
            return;
        OBPath bg =(OBPath)control.objectDict.get("colour_body");
        bg.setFillColor(OBUtils.highlightedColour(bg.fillColor()));
        highlighted = true;
    }

    public void lowlight()
    {
        if(!highlighted)
            return;
        OBPath bg =(OBPath)control.objectDict.get("colour_body");
        bg.setFillColor((int)bg.propertyValue("lowlight_colour"));
        highlighted = false;
    }


}
