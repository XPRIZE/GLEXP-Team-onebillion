package org.onebillion.onecourse.mainui.oc_count100;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 24/08/16.
 */
public class OC_CountingRobot
{
    public OBGroup robot;
    public final int ROBOT_HAPPY = 0,
            ROBOT_SAD = 1,
            ROBOT_ANGRY = 2,
            ROBOT_ANGRY2 = 3,
            ROBOT_SIDE_HAPPY = 4,
            ROBOT_SIDE_SAD = 5,
            ROBOT_SIDE_OPEN = 6,
            ROBOT_NEUTRAL = 7;


    private int currentFace;
    private int _buttonColour;
    private OC_SectionController controller;

    public OC_CountingRobot (OBGroup group, OC_SectionController cont)
    {
        controller = cont;
        robot = group;
        setArmAnchor("rightarm");
        setArmAnchor("rightarm2");
        setArmAnchor("leftarm");
        setArmAnchor("leftarm2");
        setArmAnchor("sidearm");
        setArmAnchor("sidearm2");
        robot.objectDict.get("righteyeball").masksToBounds();
        robot.objectDict.get("lefteyeball").masksToBounds();
        robot.objectDict.get("righteyeball").setProperty("originposition",robot.objectDict.get("righteyeball").position());
        robot.objectDict.get("lefteyeball").setProperty("originposition",robot.objectDict.get("lefteyeball").position());
    }

    public void setArmAnchor(String groupName)
    {
        OBGroup group = (OBGroup)robot.objectDict.get(groupName);
        OBControl anchor = group.objectDict.get(String.format("%s%s",groupName, "anchor"));
        group.setAnchorPoint( new PointF(anchor.position().x/group.bounds.width(), anchor.position().y/group.bounds.height()));

    }
    public void animateArm()
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(360),robot.objectDict.get("sidearm"))
                ,OBAnim.rotationAnim((float)Math.toRadians(720),robot.objectDict.get("sidearm2")))
                ,4,true,OBAnim.ANIM_LINEAR,controller);

    }
    public void showFront()
    {
        controller.lockScreen();
        robot.objectDict.get("bodyside").hide();
        robot.objectDict.get("bodyfront").show();
        doFace(ROBOT_HAPPY);
        controller.unlockScreen();
    }

    public void showSide()
    {
        controller.lockScreen();
        robot.objectDict.get("bodyfront").hide();
        robot.objectDict.get("bodyside").show();
        doFace(ROBOT_SIDE_HAPPY);
        controller.unlockScreen();
    }

    public void doFace(int expression)
    {
        List<OBControl> faces = new ArrayList<>();
        faces.addAll(((OBGroup)robot.objectDict.get("expression")).objectDict.values());
        faces.addAll(((OBGroup)robot.objectDict.get("expressionside")).objectDict.values());
        for(OBControl cont :faces)
            cont.hide();

        robot.objectDict.get("faceside").hide();
        robot.objectDict.get("facefront").hide();
        robot.objectDict.get("face").hide();
        robot.objectDict.get("facesideopen").hide();
        resetEyePosition();
        switch(expression)
        {
            case ROBOT_SAD:
                robot.objectDict.get("face").show();
                robot.objectDict.get("facefront").show();
                robot.objectDict.get("sad").show();
                break;
            case ROBOT_ANGRY:
                robot.objectDict.get("face").show();
                robot.objectDict.get("facefront").show();
                robot.objectDict.get("angry").show();
                break;
            case ROBOT_NEUTRAL:
                robot.objectDict.get("face").show();
                robot.objectDict.get("facefront").show();
                robot.objectDict.get("neutral").show();
                break;
            case ROBOT_ANGRY2:
                robot.objectDict.get("face").show();
                robot.objectDict.get("facefront").show();
                robot.objectDict.get("angry2").show();
                break;
            case ROBOT_SIDE_HAPPY:
                robot.objectDict.get("face").show();
                robot.objectDict.get("faceside").show();
                robot.objectDict.get("sidehappy").show();
                break;
            case ROBOT_SIDE_SAD:
                robot.objectDict.get("face").show();
                robot.objectDict.get("faceside").show();
                robot.objectDict.get("sidesad").show();
                break;
            case ROBOT_SIDE_OPEN:
                robot.objectDict.get("facesideopen").show();
                break;
            case ROBOT_HAPPY:
            default:
                robot.objectDict.get("face").show();
                robot.objectDict.get("facefront").show();
                robot.objectDict.get("happy").show();
            break;

        }
        currentFace = expression;

    }
    public void setFaceExpression(int expression)
    {
        controller.lockScreen();
        doFace(expression);
        controller.unlockScreen();

    }
    public int faceExpression()
    {
        return currentFace;
    }

    public void animateArmsUp()
    {
        OBAnimationGroup.runAnims(Arrays.asList(getAngleAnimation(-10,"leftarm"),
        getAngleAnimation(-20,"leftarm2"),
        getAngleAnimation(15,"rightarm"),
        getAngleAnimation(30,"rightarm2"))
        ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);

    }
    public void animateArmsDown()
    {
        OBAnimationGroup.runAnims(Arrays.asList(getAngleAnimation(0,"leftarm"),
        getAngleAnimation(0,"leftarm2"),
        getAngleAnimation(0,"rightarm"),
        getAngleAnimation(0,"rightarm2"))
        ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);

    }
    public void animateWave()
    {
        controller.lockScreen();
        showFront();
        setFaceExpression(ROBOT_HAPPY);
        controller.unlockScreen();
        OBAnimationGroup.runAnims(Arrays.asList(getAngleAnimation(-90,"rightarm"),
        getAngleAnimation(-60,"rightarm2"))
        ,0.25,true,OBAnim.ANIM_EASE_IN,controller);
        for(int i=0;i<3;i++)
        {
            OBAnimationGroup.runAnims(Collections.singletonList(getAngleAnimation(-160,"rightarm2"))
            ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);
            OBAnimationGroup.runAnims(Collections.singletonList(getAngleAnimation(-60,"rightarm2"))
            ,0.15,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);

        }
        OBAnimationGroup.runAnims(Arrays.asList(getAngleAnimation(0,"rightarm"),
        getAngleAnimation(0,"rightarm2"))
        ,0.25,true,OBAnim.ANIM_EASE_OUT,controller);

    }
    public void animateArmsNormal()
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(0),robot.objectDict.get("sidearm"))
                ,OBAnim.rotationAnim((float)Math.toRadians(0),robot.objectDict.get("sidearm2")))
                ,4,true,OBAnim.ANIM_LINEAR,controller);

    }

    public OBAnim getAngleAnimation(int degrees, String arm)
    {
        return OBAnim.rotationAnim((float)Math.toRadians(degrees),robot.objectDict.get(arm));
    }

    public void moveRobot(PointF position,float duration)
    {
        showSide();
        OBAnimationGroup.runAnims(Arrays.asList(robotShakeAnim(), OBAnim.moveAnim(position,robot)),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);
        showFront();
    }

    public OBAnim robotShakeAnim()
    {
        final OBGroup face = (OBGroup)robot.objectDict.get("face");
        final OBGroup arm = (OBGroup)robot.objectDict.get("sidearm");
        final PointF facePoint = face.position();
        final PointF armPoint = arm.position();
        final float jump = 8;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                if(frac >= 1)
                {
                    face.setPosition(facePoint);
                    arm.setPosition(armPoint);

                }
                else
                {
                    switch((int)(frac*100)%4)
                    {
                        case 0:
                            face.setPosition(new PointF(facePoint.x,facePoint.y + jump));
                            arm.setPosition(new PointF(armPoint.x + jump,armPoint.y));
                            break;
                        case 1:
                            face.setPosition(new PointF(facePoint.x,facePoint.y));
                            arm.setPosition(new PointF(armPoint.x ,armPoint.y+jump));
                            break;
                        case 2:
                            face.setPosition(new PointF(facePoint.x,facePoint.y - jump));
                            arm.setPosition(new PointF(armPoint.x - jump,armPoint.y));
                            break;
                        case 3:
                            face.setPosition(new PointF(facePoint.x,facePoint.y));
                            arm.setPosition(new PointF(armPoint.x,armPoint.y - jump));
                            break;

                    }

                }
            }
        };

        return blockAnim;
    }

    public void eyesTrackPoint(PointF pt,boolean side)
    {
        controller.lockScreen();
        PointF facePt = OB_Maths.locationForRect(0.5f,0.5f, robot.objectDict.get("face").getWorldFrame());
        PointF p = OB_Maths.DiffPoints(pt, facePt);
        float ang = (float)Math.atan2(p.x, -p.y) - (float)Math.toRadians(90);
        float dist = OB_Maths.PointDistance(pt, facePt);
        float switchDist = 0.35f*controller.bounds().width();
        if(currentFace == ROBOT_ANGRY || currentFace == ROBOT_ANGRY2)
            doFace(ROBOT_SAD);
        if(side && dist > switchDist && Math.toDegrees(ang)> -50)
        {
            if(faceIsFront(currentFace))
                doFace(getOpositeFace(currentFace));

            robot.objectDict.get("sideeye").setRotation(ang);

        }
        else
        {
            if(!faceIsFront(currentFace))
                doFace(getOpositeFace(currentFace));

            float locX = OB_Maths.clamp(0.32f, 0.68f, 0.5f + 0.22f * (pt.x-facePt.x)/switchDist);
            float locY = OB_Maths.clamp(0.42f, 0.62f, 0.5f + 0.2f * (pt.y-facePt.y)/switchDist);
            robot.objectDict.get("righteyeball").setPosition(OB_Maths.locationForRect(new PointF(locX, locY), robot.objectDict.get("righteyeball").parent.bounds()));
            robot.objectDict.get("lefteyeball").setPosition(OB_Maths.locationForRect(new PointF(locX, locY), robot.objectDict.get("lefteyeball").parent.bounds()));

        }
        controller.unlockScreen();
    }

    public int getOpositeFace(int expression)
    {
        switch(expression)
        {
            case ROBOT_HAPPY:
                return ROBOT_SIDE_HAPPY;
            case ROBOT_SAD:
                return ROBOT_SIDE_SAD;
            case ROBOT_ANGRY:
                return ROBOT_SIDE_SAD;
            case ROBOT_ANGRY2:
                return ROBOT_SIDE_SAD;
            case ROBOT_SIDE_HAPPY:
                return ROBOT_HAPPY;
            case ROBOT_SIDE_SAD:
                return ROBOT_SAD;
            case ROBOT_SIDE_OPEN:
            default:
                return ROBOT_HAPPY;
            case ROBOT_NEUTRAL:
                return ROBOT_SIDE_SAD;

        }
    }


    public void resetEyePosition()
    {
        OBControl righteye = robot.objectDict.get("righteyeball");
        OBControl lefteye = robot.objectDict.get("lefteyeball");
        righteye.setPosition((PointF)righteye.settings.get("originposition"));
        lefteye.setPosition((PointF)lefteye.settings.get("originposition"));
        robot.objectDict.get("sideeye").setRotation(0);
    }

    public boolean faceIsFront(int expression)
    {
        switch(expression)
        {
            case ROBOT_SIDE_HAPPY:
            case ROBOT_SIDE_SAD:
            case ROBOT_SIDE_OPEN:
                return false;
            case ROBOT_HAPPY:
            case ROBOT_SAD:
            case ROBOT_ANGRY:
            case ROBOT_ANGRY2:
            case ROBOT_NEUTRAL:
            default:
                return true;

        }

    }
    public int buttonColour()
    {
        return _buttonColour;
    }

    public void setButtonColour(int colour)
    {
        _buttonColour = colour;
        robot.objectDict.get("sidebuttoncolour").setFillColor(colour);
        robot.objectDict.get("buttoncolour").setFillColor(colour);

    }

    public void animateFrontButtonColour(int colour,float duration)
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("fillColor",colour,robot.objectDict.get("buttoncolour")))
                ,duration,true,OBAnim.ANIM_LINEAR,controller);
        robot.objectDict.get("sidebuttoncolour").setFillColor(colour);
        _buttonColour = colour;

    }

}
