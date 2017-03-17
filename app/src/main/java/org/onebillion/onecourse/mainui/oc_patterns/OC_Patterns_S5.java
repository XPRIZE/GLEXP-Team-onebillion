package org.onebillion.onecourse.mainui.oc_patterns;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.ULine;
import org.onebillion.onecourse.utils.UPath;
import org.onebillion.onecourse.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 17/03/2017.
 */

public class OC_Patterns_S5 extends OC_Generic_Event
{
    int totalLines;
    int placedLines;


    public void fin ()
    {
        goToCard(OC_Patterns_S5j.class, "event5");
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        List<OBControl> lines = filterControls("obj.*");
        totalLines = lines.size();
        placedLines = 0;
        //
        for (OBControl control : filterControls("complete.*"))
        {
            control.show();
            control.setOpacity(0.0f);

        }
        showControls("place.*");
        showControls("obj.*");
    }

    public void demo5a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now let 's make a shape.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.6f, 0.6f, -10, 0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // Like this.
        OBControl control = objectDict.get("obj_1");
        OC_Generic.pointer_moveToObject(control, 5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToPointWithObject(control, objectDict.get("place_2").getWorldPosition(), -15, 0.6f, true, this);
        playSfxAudio("correct", false);
        waitForSecs(0.3f);
        //
        control = objectDict.get("obj_2");
        OC_Generic.pointer_moveToObject(control, 5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToPointWithObject(control, objectDict.get("place_3").getWorldPosition(), -15, 0.6f, true, this);
        playSfxAudio("correct", false);
        waitForSecs(0.3f);
        //
        control = objectDict.get("obj_3");
        OC_Generic.pointer_moveToObject(control, 5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToPointWithObject(control, objectDict.get("place_1").getWorldPosition(), -15, 0.6f, true, this);
        playSfxAudio("correct", false);
        waitForSecs(0.3f);
        //
        List<OBAnim> animations = new ArrayList();
        for (OBControl controlAlt : filterControls("obj.*"))
        {
            OBAnim anim = OBAnim.opacityAnim(0.0f, controlAlt);
            animations.add(anim);

        }
        for (OBControl place : filterControls("place.*"))
        {
            OBAnim anim = OBAnim.opacityAnim(0.0f, place);
            animations.add(anim);

        }
        for (OBControl controlAlt : filterControls("complete.*"))
        {
            OBAnim anim = OBAnim.opacityAnim(1.0f, controlAlt);
            animations.add(anim);

        }
        OBAnimationGroup.runAnims(animations, 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        action_playNextDemoSentence(false); // This shape is called a TRIANGLE.
        //
        OC_Generic.pointer_moveToObjectByName("complete_1", -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        //
        action_playNextDemoSentence(false); // It has three straight sides.
        waitAudio();
        waitForSecs(0.3f);
        //
        control = objectDict.get("complete_1");
        UPath upath = deconstructedPath(currentEvent(), (String) control.attributes().get("id"));
        USubPath subPath = upath.subPaths.get(0);
        for (int i = 0; i < 3; i++)
        {
            ULine line = subPath.elements.get(i);
            Path bezier = new Path();
            PointF newPt0 = line.pt0;
            PointF newPt1 = line.pt1;
            bezier.moveTo(newPt0.x, newPt0.y);
            bezier.lineTo(newPt1.x, newPt1.y);
            //
            OBPath path = new OBPath();
            path.setPath(bezier);
            path.setStrokeColor(Color.BLACK);
            path.setLineWidth(applyGraphicScale(24));
            OC_Generic.sendObjectToTop(path, this);
            //
            OBPath clone = (OBPath) control.copy();
            OBGroup group = new OBGroup((List<OBControl>) (Object) Arrays.asList(clone, path));
            group.setMaskControl(clone);
            //
            action_playNextDemoSentence(false); // One. Two. Three.
            //
            lockScreen();
            attachControl(group);
            OC_Generic.sendObjectToTop(group, this);
            group.show();
            group.setBorderColor(Color.RED);
            group.setBorderWidth(2.0f);
            unlockScreen();
            //
            waitAudio();
            //
            lockScreen();
            group.hide();
            unlockScreen();
            //
            waitForSecs(0.3f);
        }
        thePointer.hide();
        waitForSecs(0.7f);

        nextScene();
    }


    public void finalDemo5d () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        playAudioScene("FINAL", 0, false); // This shape is called a square.
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("FINAL", 1, false); // It has four straight sides.
        waitAudio();
        waitForSecs(0.3f);
        //
        OBControl control = objectDict.get("complete_1");
        UPath upath = deconstructedPath(currentEvent(), (String) control.attributes().get("id"));
        USubPath subPath = upath.subPaths.get(0);
        //
        List<OBControl> paths = new ArrayList();
        for (int i = 0; i < 4; i++)
        {
            ULine line = subPath.elements.get(i);
            Path bezier = new Path();
            bezier.moveTo(line.pt0.x, line.pt0.y);
            bezier.lineTo(line.pt1.x, line.pt1.y);
            //
            OBPath path = new OBPath();
            path.setPath(bezier);
            path.setStrokeColor(Color.BLACK);
            path.setLineWidth(applyGraphicScale(24));
            OC_Generic.sendObjectToTop(path, this);
            paths.add(path.copy());
            //
            OBPath clone = (OBPath) control.copy();
            OBGroup group = new OBGroup((List<OBControl>) (Object) Arrays.asList(clone, path));
            group.setMaskControl(clone);
            OC_Generic.sendObjectToTop(group, this);
            //
            playAudioScene("FINAL", 2 + i, false); // One. Two. Three. Four.
            //
            lockScreen();
            attachControl(group);
            group.show();
            unlockScreen();
            //
            waitAudio();
            //
            lockScreen();
            group.hide();
            unlockScreen();
            //
            waitForSecs(0.3f);
        }
        playAudioScene("FINAL", 6, false); // They are all the same length.
        OBPath clone = (OBPath) control.copy();
        List membersForGroup = new ArrayList();
        membersForGroup.add(clone);
        for (OBControl path : paths) membersForGroup.add(path);
        OBGroup group = new OBGroup(membersForGroup);
        group.setMaskControl(clone);
        OC_Generic.sendObjectToTop(group, this);
        //
        lockScreen();
        attachControl(group);
        group.show();
        unlockScreen();
        //
        waitAudio();
        //
        lockScreen();
        group.hide();
        detachControl(group);
        unlockScreen();
        //
        waitForSecs(0.7f);
    }


    public void finalDemo5f () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        OBControl control = objectDict.get("complete_1");
        playAudioScene("FINAL", 0, false);  // This shape is called a rectangle.
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("FINAL", 1, false); // It has four straight sides.
        waitAudio();
        waitForSecs(0.3f);
        //
        UPath upath = deconstructedPath(currentEvent(), (String) control.attributes().get("id"));
        USubPath subPath = upath.subPaths.get(0);
        //
        List<OBControl> paths = new ArrayList();
        //
        for (int i = 0; i < 4; i++)
        {
            ULine line = subPath.elements.get(i);
            Path bezier = new Path();
            bezier.moveTo(line.pt0.x, line.pt0.y);
            bezier.lineTo(line.pt1.x, line.pt1.y);
            //
            OBPath path = new OBPath();
            path.setPath(bezier);
            path.setStrokeColor(Color.BLACK);
            path.setLineWidth(applyGraphicScale(24));
            OC_Generic.sendObjectToTop(path, this);
            paths.add(path.copy());
            //
            OBPath clone = (OBPath) control.copy();
            OBGroup group = new OBGroup((List<OBControl>) (Object) Arrays.asList(clone, path));
            group.setMaskControl(clone);
            OC_Generic.sendObjectToTop(group, this);
            //
            playAudioScene("FINAL", 2 + i, false); // One.Two.Three.Four.
            //
            lockScreen();
            attachControl(group);
            group.show();
            unlockScreen();
            //
            waitAudio();
            //
            lockScreen();
            group.hide();
            detachControl(group);
            unlockScreen();
            //
            waitForSecs(0.3f);
        }
        playAudioScene("FINAL", 6, false); // These two sides are longer …
        //
        OBPath clone = (OBPath) control.copy();
        List membersForGroup = new ArrayList();
        membersForGroup.add(clone);
        membersForGroup.add(paths.get(0));
        membersForGroup.add(paths.get(2));
        OBGroup group = new OBGroup(membersForGroup);
        group.setMaskControl(clone);
        OC_Generic.sendObjectToTop(group, this);
        //
        lockScreen();
        attachControl(group);
        group.show();
        unlockScreen();
        //
        waitAudio();
        //
        lockScreen();
        group.hide();
        detachControl(group);
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        playAudioScene("FINAL", 7, false); // …than these two.
        //
        clone = (OBPath) control.copy();
        membersForGroup = new ArrayList();
        membersForGroup.add(clone);
        membersForGroup.add(paths.get(1));
        membersForGroup.add(paths.get(3));
        group = new OBGroup(membersForGroup);
        group.setMaskControl(clone);
        OC_Generic.sendObjectToTop(group, this);
        //
        lockScreen();
        attachControl(group);
        group.show();
        unlockScreen();
        //
        waitAudio();
        //
        lockScreen();
        group.hide();
        detachControl(group);
        unlockScreen();
        //
        waitForSecs(0.7f);
    }

    public void finalDemo5h () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        OBControl control = objectDict.get("complete_1");
        playAudioScene("FINAL", 0, false); // This shape is called a circle.
        waitAudio();
        waitForSecs(0.3f);
        //
        OBPath clone = (OBPath) control.copy();
        OBPath path = (OBPath) control.copy();
        path.setStrokeColor(Color.BLACK);
        path.setLineWidth(applyGraphicScale(24));
        OC_Generic.sendObjectToTop(path, this);
        //
        OBGroup group = new OBGroup((List<OBControl>) (Object) Arrays.asList(path, clone));
        group.setMaskControl(clone);
        OC_Generic.sendObjectToTop(group, this);
        //
        playAudioScene("FINAL", 1, false); // It is round.
        //
        lockScreen();
        attachControl(group);
        group.show();
        unlockScreen();
        //
        waitAudio();
        //
        lockScreen();
        group.hide();
        detachControl(group);
        unlockScreen();
        //
        waitForSecs(0.7f);
    }

    public void checkDragAtPoint (PointF pt)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            OBControl placement = finger(0, 2, filterControls("place.*"), pt, true);
            OBControl control = target;
            target = null;
            if (placement != null)
            {
                if (placement.attributes().get("type").equals(control.attributes().get("type")))
                {
                    control.moveToPoint(placement.getWorldPosition(), 0.1f, false);
                    //
                    gotItRightBigTick(false);
                    //
                    placedLines++;
                    if (placedLines >= totalLines)
                    {
                        waitSFX();
                        gotItRightBigTick(true);
                        waitForSecs(0.3f);
                        //
                        List<OBAnim> animations = new ArrayList();
                        for (OBControl controlAlt : filterControls("obj.*"))
                        {
                            OBAnim anim = OBAnim.opacityAnim(0.0f, controlAlt);
                            animations.add(anim);

                        }
                        for (OBControl place : filterControls("place.*"))
                        {
                            OBAnim anim = OBAnim.opacityAnim(0.0f, place);
                            animations.add(anim);

                        }
                        for (OBControl controlAlt : filterControls("complete.*"))
                        {
                            OBAnim anim = OBAnim.opacityAnim(1.0f, controlAlt);
                            animations.add(anim);
                        }
                        OBAnimationGroup.runAnims(animations, 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                        playAudioQueuedScene("CORRECT", true);
                        waitForSecs(0.3f);
                        //
                        if (!performSel("finalDemo", currentEvent()))
                        {
                            playAudioQueuedScene("FINAL", true);
                        }
                        nextScene();
                        //
                        return;
                    }
                }
                else
                {
                    gotItWrongWithSfx();
                    PointF value = (PointF) control.propertyValue("originalPosition");
                    control.moveToPoint(value, 0.3f, false);
                }
            }
            else
            {
                PointF value = (PointF) control.propertyValue("originalPosition");
                control.moveToPoint(value, 0.3f, false);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Patterns_S5:checkDragAtPoint:exception caught");
            e.printStackTrace();
        }
        //
        revertStatusAndReplayAudio();
        setStatus(STATUS_AWAITING_CLICK);
    }


    public Object findObject (PointF pt)
    {
        return finger(0, 2, filterControls("obj.*"), pt, true);
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl obj = (OBControl) findObject(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkDragTarget(obj, pt);
                    }
                });
            }
        }
    }


    public void checkDragTarget (OBControl targ, PointF pt)
    {
        super.checkDragTarget(targ, pt);
        OC_Generic.sendObjectToTop(targ, this);
    }


    public void touchUpAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }


}


