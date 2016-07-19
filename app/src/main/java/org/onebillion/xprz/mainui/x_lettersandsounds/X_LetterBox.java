package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.UCurve;
import org.onebillion.xprz.utils.ULine;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 29/06/16.
 */
public class X_LetterBox
{
    public OBGroup control;
    public OBGroup mask;
    private XPRZ_SectionController controller;
    private boolean animateGlow;


    public X_LetterBox(OBGroup group, XPRZ_SectionController sectionController)
    {
        control = group;
        controller = sectionController;

        OBGroup glow = (OBGroup)control.objectDict.get("glow");

        glow.show();
        glow.setOpacity(0);
        animateGlow = false;

        OBControl masktop = control.objectDict.get("masktop");
        RectF rect = controller.convertRectFromControl(masktop.bounds(), masktop);
        OBControl mask1 = new OBControl();
        mask1.setFrame(new RectF(0, rect.top,control.right(), controller.bounds().height()));
        mask1.setBackgroundColor(Color.BLACK);
        mask1.setZPosition(1);
        OBControl mask2 = new OBControl();
        mask2.setFrame(new RectF(sectionController.bounds()));
        mask2.setBackgroundColor(Color.BLACK);
        mask2.setOpacity(0);
        mask2.setZPosition(1);
        mask = new OBGroup(Arrays.asList(mask1,mask2));

        mask.setZPosition(1);
        control.setZPosition(2);

        control.show();
        mask.texturise(false,sectionController);

    }

    public void openLid(String sfx) throws Exception
    {
        controller.playSfxAudio(sfx,false);
        controller.animateFrames(OBUtils.getFramesList("lid_", 0, 4), 0.05f, control);
        controller.waitSFX();
    }

    public void closeLid(String sfx)  throws Exception
    {
        controller.playSfxAudio(sfx,false);
        controller.animateFrames(OBUtils.getFramesList("lid_", 4, 0), 0.05f, control);
        controller.waitSFX();
    }

    public void resetGlow()
    {


    }

    public void animateGlowsShow()
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1,control.objectDict.get("glow")),
                OBAnim.opacityAnim(1,control.objectDict.get("gemsglow"))),0.2f,true,OBAnim.ANIM_LINEAR, controller);
    }


    public void animateGlowsHide()
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,control.objectDict.get("glow")),
                OBAnim.opacityAnim(0,control.objectDict.get("gemsglow"))),0.5f,true,OBAnim.ANIM_LINEAR, controller);

    }

    public void startGlowPulse(float delay) throws Exception
    {
        animateGlow = true;
        animateGlow(delay);
    }

    public void startGemsGlowPulse(float delay) throws Exception
    {
        animateGlow = true;
        animateGemsGlow(delay);
    }


    public void animateGlow(final float delay) throws Exception
    {

        if (animateGlow)
            controller.waitForSecs(delay);

        if (animateGlow)
        {
            controller.registerAnimationGroup(OBAnimationGroup.chainAnimations(
                    Arrays.asList(
                            Collections.singletonList(OBAnim.opacityAnim(0.2f, control.objectDict.get("glow"))),


                            Collections.singletonList(OBAnim.opacityAnim(1f, control
                                    .objectDict.get("glow"))),
                            Collections.singletonList(OBAnim.opacityAnim(0.2f, control.objectDict
                                    .get("glow"))),
                            Collections.singletonList(OBAnim.opacityAnim(1f, control.objectDict.get("glow")))
                    ),
                    Arrays.asList(0.5f, 0.5f, 0.5f, 0.5f),
                    false,
                    Arrays.asList(OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR),
                    1,
                    new OBUtils.RunLambda()
                    {

                        @Override
                        public void run() throws Exception
                        {
                            if (animateGlow)
                                animateGlow(delay);
                        }
                    },
                    controller),
                    "glowAnim");

        }
    }

    public void animateGemsGlow(final float delay) throws Exception
    {

        if (animateGlow)
            controller.waitForSecs(delay);

        if (animateGlow)
        {
            controller.registerAnimationGroup(OBAnimationGroup.chainAnimations(
                    Arrays.asList(
                            Collections.singletonList(OBAnim.opacityAnim(0, control.objectDict.get("gemsglow"))),
                            Collections.singletonList(OBAnim.opacityAnim(1, control.objectDict.get("gemsglow"))),
                            Collections.singletonList(OBAnim.opacityAnim(0, control.objectDict.get("gemsglow"))),
                            Collections.singletonList(OBAnim.opacityAnim(1, control.objectDict.get("gemsglow"))),
                            Collections.singletonList(OBAnim.opacityAnim(0, control.objectDict.get("gemsglow")))
                    ),
                    Arrays.asList(0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
                    false,
                    Arrays.asList(OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR),
                    1,
                    new OBUtils.RunLambda()
                    {

                        @Override
                        public void run() throws Exception
                        {
                            if (animateGlow)
                                animateGemsGlow(delay);
                        }
                    },
                    controller),
                    "glowAnim");

        }

    }


    public void stopGlowPulse()
    {
        animateGlow = false;
        controller.deregisterAnimationGroupWithName("glowAnim");
    }


    public void stopGemsGlowPulse(boolean showGlow)
    {
        animateGlow = false;
        controller.deregisterAnimationGroupWithName("glowAnim");
        control.objectDict.get("gemsglow").setOpacity(showGlow ? 1 : 0);
    }



    public void flyObjects(final List<OBControl> objs, final boolean outside, boolean together, String sfx) throws Exception
    {
        if(outside)
            animateGlowsShow();

        controller.playSfxAudio(sfx, false);
        List<OBControl> objsArray = new ArrayList<>(objs);
        if(outside)
            Collections.reverse(objsArray);

        List<OBAnim> allAnims = new ArrayList<>();
        for (int i = 0; i < objsArray.size(); i++)
        {

            PointF boxPoint1 =  OB_Maths.worldLocationForControl(0.5f,1,control);
            PointF boxPoint2 =   OB_Maths.worldLocationForControl(0.5f,0.2f,control);;
            final OBControl obj = objsArray.get(i);
            if(outside)
            {
                controller.lockScreen();
                obj.setPosition(boxPoint1);
                obj.show();
                controller.unlockScreen();
            }
            List<OBAnim> anims = new ArrayList<>();

            PointF pathStart = outside ? boxPoint2 : obj.position();
            PointF pathEnd = outside ? (PointF)obj.propertyValue("drop_loc") : boxPoint2;
            float distance = OB_Maths.PointDistance(pathStart, pathEnd);

            USubPath subPath = new USubPath();
            UCurve curve = OBUtils.SimpleUCurve(pathStart, pathEnd,(outside ? -1 : 1 ) * distance / 3.0f);

            if(outside)
            {
                ULine line = new ULine(obj.position().x, obj.position().y,pathStart.x, pathStart.y);
                subPath.elements.add(line);
                subPath.elements.add(curve);
            }
            else
            {
                ULine line = new ULine(pathEnd.x, pathEnd.y,boxPoint1.x, boxPoint1.y);
                subPath.elements.add(curve);
                subPath.elements.add(line);
            }

            UPath uPath = new UPath();
            uPath.subPaths.add(subPath);

            anims.add(OBAnim.pathMoveAnim(obj,uPath.bezierPath(),false,0));
            anims.add(OBAnim.rotationAnim(obj.rotation + (outside ? 1 : -1)*(float)Math.toRadians(360)*OB_Maths.randomInt(1,4),obj));

            if(outside)
                obj.setScale(0.3f);

            anims.add(OBAnim.scaleAnim(outside ? (float)obj.propertyValue("start_scale") : 0.3f,obj));

            if(!together)
            {
                OBAnimationGroup.runAnims(anims, 0.6f, i == objsArray.size() - 1, OBAnim.ANIM_EASE_IN_EASE_OUT,
                        new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                if(!outside)
                                    obj.hide();
                            }
                        }, controller);

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
            OBAnimationGroup.runAnims(allAnims, 0.8f, true, OBAnim.ANIM_EASE_IN_EASE_OUT,
                    new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            for(OBControl obj : objs)
                            {
                                if (!outside)
                                    obj.hide();

                            }
                        }
                    }, controller);
        }

        if(outside)
            animateGlowsHide();
    }

}
