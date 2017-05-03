package org.onebillion.onecourse.mainui.oc_shapes;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 03/05/2017.
 */

public class OC_Shapes_S7 extends OC_Shapes_S6
{
    public void demo7a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look at the duck and its eggs. You can colour in the picture.
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.1f, 0.9f), bounds()), -25, 0.6f, true);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.7f, 0.4f), bounds()), -5, 1.8f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // Like this.;
        OC_Generic.pointer_moveToObjectByName("paint_1", 0, 0.4f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Triangle.
        action_selectPaint(objectDict.get("paint_1"));
        waitAudio();
        //
        OC_Generic.pointer_moveToObjectByName("obj_right_foot", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playAudioScene("DEMO", 3, false); // Triangle.
        action_colourObject(objectDict.get("obj_right_foot"));
        waitAudio();
        //
        OC_Generic.pointer_moveToObjectByName("obj_right_foot", -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void finDemo7b () throws Exception
    {
        OBControl shell_top = objectDict.get("shell_top");
        OBControl shell_background = objectDict.get("shell_background");
        OBControl bird = objectDict.get("small_bird");
        OBGroup beak_open = (OBGroup) objectDict.get("beak_open");
        OBControl beak_closed = objectDict.get("beak_closed");
        OBPath shell_path = (OBPath) objectDict.get("shell_path");
        shell_path.sizeToBox(boundsf());
        List<OBAnim> fadeAnimations = new ArrayList<>();
        //
        lockScreen();
        for (OBControl control : filterControls("shell.*"))
        {
            if (control == shell_path || control == shell_background) continue;
            OBAnim fadeAnim = OBAnim.opacityAnim(1.0f, control);
            fadeAnimations.add(fadeAnim);
            control.show();
            control.setOpacity(0.0f);
        }
        hideControls("obj_beak");
        shell_path.hide();
        beak_closed.show();
        bird.hide();
        shell_background.hide();
        unlockScreen();
        //
        OBAnimationGroup.runAnims(fadeAnimations, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.5f);
        //
        lockScreen();
        shell_background.show();
        bird.show();
        unlockScreen();
        //
        OBAnim shellTop_moveAnim = OBAnim.pathMoveAnim(shell_top, shell_path.path(), false, 0);
        OBAnim shellTop_rotateAnim = OBAnim.rotationAnim((float) Math.toRadians(180), shell_top);
        OBAnimationGroup.runAnims(Arrays.asList(shellTop_moveAnim, shellTop_rotateAnim), 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        PointF destination = OC_Generic.copyPoint(bird.position());
        destination.y = bird.top();
        OBAnim birdMove = OBAnim.moveAnim(destination, bird);
        OBAnimationGroup.runAnims(Collections.singletonList(birdMove), 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        playSfxAudio("quack", false);
        //
        for (int i = 0; i < 3; i++)
        {
            lockScreen();
            beak_open.show();
            beak_closed.hide();
            unlockScreen();
            //
            waitForSecs(0.25f);
            //
            lockScreen();
            beak_open.hide();
            beak_closed.show();
            unlockScreen();
            //
            waitForSecs(0.275f);
        }
        waitSFX();
        waitForSecs(0.5f);
    }

    public void finDemo7c () throws Exception
    {
        List<OBControl> groupArray = new ArrayList<>();
        groupArray.addAll(filterControls("obj.*"));
        groupArray.addAll(filterControls("driver"));
        groupArray.addAll(filterControls("coal.*"));
        groupArray.addAll(filterControls("axis"));
        //
        OBGroup group;
        lockScreen();
        showControls("coal.*");
        showControls("axis");
        group = new OBGroup(groupArray);
        attachControl(group);
        OC_Generic.sendObjectToTop(group, this);
        unlockScreen();
        //
        PointF destination = OC_Generic.copyPoint(group.position());
        destination.x = (destination.x - group.width() * 2);
        OBAnim anim = OBAnim.moveAnim(destination, group);
        waitForSecs(0.5f);
        //
        playSfxAudio("train", false);
        OBAnimationGroup.runAnims(Collections.singletonList(anim), 4.0, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitSFX();
    }

    public void finDemo7d () throws Exception
    {
        OBControl door = objectDict.get("obj_7");
        OBControl knob = objectDict.get("knob");
        OBControl girl_1 = objectDict.get("girl_frame1");
        OBControl girl_2 = objectDict.get("girl_frame2");
        //
        PointF originalPosition = OC_Generic.copyPoint(girl_1.position());
        PointF destination = OB_Maths.locationForRect(new PointF(0.5f, 0.5f), door.frame);
        OBAnim girlMoveToDoor = OBAnim.moveAnim(destination, girl_1);
        OBAnimationGroup.runAnims(Collections.singletonList(girlMoveToDoor), 1.0, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        girl_2.setPosition(destination);
        //
        lockScreen();
        door.hide();
        knob.hide();
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        for (int i = 0; i < 8; i++)
        {
            lockScreen();
            girl_1.hide();
            girl_2.show();
            unlockScreen();
            //
            waitForSecs(0.2f);
            //
            lockScreen();
            girl_1.show();
            girl_2.hide();
            unlockScreen();
            //
            waitForSecs(0.2f);
        }
        lockScreen();
        door.show();
        knob.show();
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        OBAnim girlMoveFromDoor = OBAnim.moveAnim(originalPosition, girl_1);
        OBAnimationGroup.runAnims(Collections.singletonList(girlMoveFromDoor), 1.0, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


}
