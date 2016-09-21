package org.onebillion.xprz.mainui.x_shapes;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBStroke;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.ULine;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.USubPath;

import java.security.acl.Group;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by pedroloureiro on 22/06/16.
 */
public class X_Shapes_S2 extends XPRZ_Generic_SelectCorrectObject
{
    String eventType;
    List<OBControl> demosDone;
    List<OBControl> shownShapes;
    int correctAnswerCount;


    public String action_getObjectPrefix ()
    {
        return "obj";
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        demosDone = new ArrayList<OBControl>();
        eventType = eventAttributes.get("type");
        correctAnswerCount = 0;
        //
        for (OBControl control : filterControls(".*"))
        {
            if (OBPath.class.isInstance(control))
            {
                OBPath path = (OBPath) control;
                path.sizeToBoundingBoxIncludingStroke();
            }
        }
        //
        if (eventType.equals("choose"))
        {
            for (OBControl control : filterControls("obj.*"))
            {
                control.enable();
                control.setOpacity(1);
            }
        }
        else if (eventType.equals("showFill"))
        {
            if (shownShapes == null) shownShapes = new ArrayList<OBControl>();
            //
            for (OBControl control : filterControls("obj.*"))
            {
                OBPath path = (OBPath) control;
                //
                if (!shownShapes.contains(path))
                {
                    if (control.propertyValue("colour") == null)
                    {
                        int fillColour = path.fillColor();
                        path.setProperty("colour", fillColour);
                        path.setFillColor(Color.WHITE);
                    }
                }
            }
        }
        else if (eventType.equals("reveal"))
        {
            if (shownShapes != null)
            {
                for (OBControl control : shownShapes)
                {
                    detachControl(control);
                }
            }
            shownShapes = new ArrayList<OBControl>();
            //
            for (OBControl control : filterControls("obj.*"))
            {
                control.enable();
            }
        }
    }


    public void action_toggleShape (OBPath shape, Boolean value)
    {
        if (value)
        {
            OBPath clone = (OBPath) shape.copy();
            clone.setLineWidth(applyGraphicScale(10));
            clone.setFillColor(OBUtils.colorFromRGBString("251,9,250"));
            shape.setProperty("clone", clone);
            clone.disable();
            clone.sizeToBoundingBoxIncludingStroke();
            //
            lockScreen();
            XPRZ_Generic.sendObjectToTop(clone, this);
            attachControl(clone);
            clone.show();
            unlockScreen();
            //
            shownShapes.add(clone);
        }
        else
        {
            OBPath clone = (OBPath) shape.propertyValue("clone");
            if (clone != null)
            {
                lockScreen();
                clone.hide();
                detachControl(clone);
                unlockScreen();
                //
                shownShapes.remove(clone);
            }
        }

    }


    public void demo2a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playSceneAudioIndex("DEMO", 0, false); // Touch each shape, to hear its name.
        setReplayAudioScene(currentEvent(), "REPEAT");
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demo2l () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(true); // Now look at this picture.
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // The sun is like a circle.
        waitForSecs(0.3);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void demo_circle () throws Exception
    {
        playSceneAudioIndex("DEMO2", 0, false); // A circle. It is round.
        waitForSecs(0.01);
        //
        OBPath circle = (OBPath) objectDict.get("obj_circle");
        OBPath clone = (OBPath) circle.copy();
        //
        OBPath path = (OBPath) circle.copy();
        path.setStrokeColor(Color.BLACK);
        path.setLineWidth(applyGraphicScale(24));
        path.setMaskControl(clone);
        path.sizeToBox(new RectF(bounds()));
        //
        lockScreen();
        XPRZ_Generic.sendObjectToTop(path, this);
        attachControl(path);
        path.show();
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        path.hide();
        detachControl(path);
        unlockScreen();
        waitForSecs(0.3);
    }


    public void demo_oval () throws Exception
    {
        playSceneAudioIndex("DEMO3", 0, false); // An oval. It is like a stretched circle.
        //
        OBPath oval = (OBPath) objectDict.get("obj_oval");
        OBPath clone = (OBPath) oval.copy();
        //
        OBPath path = (OBPath) oval.copy();
        path.setStrokeColor(Color.BLACK);
        path.setLineWidth(applyGraphicScale(24));
        path.setMaskControl(clone);
        path.sizeToBox(new RectF(bounds()));
        //
        lockScreen();
        XPRZ_Generic.sendObjectToTop(path, this);
        attachControl(path);
        path.show();
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        path.hide();
        detachControl(path);
        unlockScreen();
        waitForSecs(0.3);
    }


    public void demo_triangle () throws Exception
    {
        playSceneAudioIndex("DEMO4", 0, true); // A triangle. It has THREE sides.
        waitForSecs(0.3f);
        //
        OBPath triangle = (OBPath) objectDict.get("obj_triangle");
        UPath upath = deconstructedPath(currentEvent(), "obj_triangle");
        USubPath subPath = upath.subPaths.get(0);
        for (int i = 0; i < 3; i++)
        {
            ULine line = subPath.elements.get(i);
            Path bezier = new Path();
            bezier.moveTo(line.pt0.x, line.pt0.y);
            bezier.lineTo(line.pt1.x, line.pt1.y);
            //
            OBPath path = new OBPath(bezier);
            path.setStrokeColor(Color.BLACK);
            path.setLineWidth(applyGraphicScale(24));
            path.sizeToBox(new RectF(bounds()));
            //
            OBPath clone = (OBPath) triangle.copy();
            path.setMaskControl(clone);
            ;
            //
            playSceneAudioIndex("DEMO4", i + 1, false); // One. Two. Three.
            lockScreen();
            XPRZ_Generic.sendObjectToTop(path, this);
            attachControl(path);
            path.show();
            unlockScreen();
            waitAudio();
            //
            lockScreen();
            path.hide();
            detachControl(path);
            unlockScreen();
            waitForSecs(0.3);
        }
    }


    public void demo_rectangle () throws Exception
    {
        playSceneAudioIndex("DEMO5", 0, true); // A rectangle. It has FOUR sides.
        waitForSecs(0.3);
        //
        OBPath rectangle = (OBPath) objectDict.get("obj_rectangle");
        UPath uPath = deconstructedPath(currentEvent(), "obj_rectangle");
        USubPath subPath = uPath.subPaths.get(0);
        List<OBControl> sides = new ArrayList<OBControl>();
        //
        for (int i = 0; i < 4; i++)
        {
            ULine line = subPath.elements.get(i);
            Path bezier = new Path();
            bezier.moveTo(line.pt0.x, line.pt0.y);
            bezier.lineTo(line.pt1.x, line.pt1.y);
            //
            OBPath path = new OBPath(bezier);
            path.setStrokeColor(Color.BLACK);
            path.setLineWidth(applyGraphicScale(24));
            path.sizeToBox(new RectF(bounds()));
            //
            OBPath clone = (OBPath) rectangle.copy();
            path.setMaskControl(clone);
            ;
            //
            playSceneAudioIndex("DEMO5", i + 1, false); // One. Two. Three. Four.
            XPRZ_Generic.sendObjectToTop(path, this);
            attachControl(path);
            sides.add(path);
            path.show();
            waitAudio();
            //
            path.hide();
            waitForSecs(0.3);
        }
        //
        playSceneAudioIndex("DEMO5", 5, false); // These two are the same length.
        lockScreen();
        sides.get(0).show();
        sides.get(2).show();
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        sides.get(0).hide();
        sides.get(2).hide();
        unlockScreen();
        waitForSecs(0.3);
        //
        playSceneAudioIndex("DEMO5", 6, false); // And these two are the same length
        lockScreen();
        sides.get(1).show();
        sides.get(3).show();
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        sides.get(1).hide();
        sides.get(3).hide();
        unlockScreen();
        waitForSecs(0.3);
        //
        for (OBControl control : sides)
        {
            detachControl(control);
        }
    }


    public void demo_square () throws Exception
    {
        playSceneAudioIndex("DEMO6", 0, true); // A square. It has four sides.
        waitForSecs(0.3);
        //
        OBPath square = (OBPath) objectDict.get("obj_square");
        UPath uPath = deconstructedPath(currentEvent(), "obj_square");
        USubPath subPath = uPath.subPaths.get(0);
        //
        List<OBControl> sides = new ArrayList<OBControl>();
        for (int i = 0; i < 4; i++)
        {
            ULine line = subPath.elements.get(i);
            Path bezier = new Path();
            bezier.moveTo(line.pt0.x, line.pt0.y);
            bezier.lineTo(line.pt1.x, line.pt1.y);
            //
            OBPath path = new OBPath(bezier);
            path.setStrokeColor(Color.BLACK);
            path.setLineWidth(applyGraphicScale(24));
            path.sizeToBox(new RectF(bounds()));
            //
            OBPath clone = (OBPath) square.copy();
            path.setMaskControl(clone);
            ;
            //
            playSceneAudioIndex("DEMO6", i + 1, false); // One. Two. Three. Four.
            XPRZ_Generic.sendObjectToTop(path, this);
            attachControl(path);
            sides.add(path);
            path.show();
            waitAudio();
            //
            path.hide();
            waitForSecs(0.3);
        }
        //
        playSceneAudioIndex("DEMO6", 5, false); // They are ALL the same length.
        lockScreen();
        for (OBControl side : sides)
        {
            side.show();
        }
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        for (OBControl side : sides)
        {
            side.hide();
            detachControl(side);
        }
        unlockScreen();
        waitForSecs(0.3);
    }


    public void checkTarget_demo(OBControl target)
    {
        if (!demosDone.contains(target))
        {
            demosDone.add(target);
            List<OBAnim> anims = new ArrayList<OBAnim>();
            for (OBControl control : filterControls("obj.*"))
            {
                if (control.equals(target)) continue;
                anims.add(OBAnim.opacityAnim(0.3f, control));
            }
            OBAnimationGroup.runAnims(anims, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            //
            String demo = ((String) target.attributes().get("id")).replace("obj", "demo");
            performSel(demo, "");
            //
            anims.clear();
            for (OBControl control : filterControls("obj.*"))
            {
                if (demosDone.contains(control))
                {
                    anims.add(OBAnim.opacityAnim(0.3f, control));
                    control.disable();
                }
                else
                {
                    anims.add(OBAnim.opacityAnim(1.0f, control));
                    control.enable();
                }
            }
            OBAnimationGroup.runAnims(anims, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            //
            List<OBControl> shapes = filterControls("obj.*");
            if (shapes.size() == demosDone.size())
            {
                anims.clear();
                for (OBControl control : filterControls("obj.*"))
                {
                    anims.add(OBAnim.opacityAnim(1.0f, control));
                }
                //
                OBAnimationGroup.runAnims(anims, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                //
                nextScene();
                return;
            }
        }
        revertStatusAndReplayAudio();
    }



    public void checkTarget_choose(OBControl target) throws Exception
    {
        action_highlight(target);
        //
        if (target.equals(action_getCorrectAnswer()))
        {
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            playAudioQueuedScene(currentEvent(), "CORRECT", true);
            waitForSecs(0.3);
            //
            playAudioQueuedScene(currentEvent(), "FINAL", true);
            action_lowlight(target);
            //
            nextScene();
            return;
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3);
            action_lowlight(target);
            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
            revertStatusAndReplayAudio();
        }
    }


    public void checkTarget_showFill(OBControl target) throws Exception
    {
        action_highlight(target);
        //
        if (target.equals(action_getCorrectAnswer()))
        {
            OBPath path = (OBPath) target;
            path.setFillColor((Integer) path.propertyValue("colour"));
            shownShapes.add(path);
            //
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            playAudioQueuedScene(currentEvent(), "CORRECT", true);
            waitForSecs(0.3);
            //
            playAudioQueuedScene(currentEvent(), "FINAL", true);
            action_lowlight(target);
            //
            nextScene();
            return;
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3);
            action_lowlight(target);
            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
            revertStatusAndReplayAudio();
        }
    }


    public void checkTarget_reveal(OBControl target) throws Exception
    {
        String wrongAudio = (currentEvent().equals("2l")) ? "INCORRECT2" : "INCORRECT";
        OBPath path = (OBPath) target;
        //
        if (currentEvent().equals("2l") && target.attributes().get("id").equals("obj_sun"))
        {
            gotItWrongWithSfx();
            waitForSecs(0.3);
            action_lowlight(target);
            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
            revertStatusAndReplayAudio();
        }
        else if (target != null && target.attributes().get("type").equals(eventAttributes.get("correctAnswer")))
        {
            action_toggleShape(path, true);
            //
            correctAnswerCount++;
            int correctQuantity = Integer.parseInt(eventAttributes.get("correctQuantity"));
            if (correctQuantity == 1)
            {
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                if (currentEvent().equals("2n"))
                {
                    String targetID = (String) target.attributes().get("id");
                    if (targetID.contains("door")) playSceneAudioIndex("CORRECT", 0, true);
                    else if (targetID.contains("house"))
                        playSceneAudioIndex("CORRECT", 1, true);
                    else if (targetID.contains("trunk"))
                        playSceneAudioIndex("CORRECT", 2, true);
                }
                else
                {
                    playAudioQueuedScene(currentEvent(), "CORRECT", true);
                }
                waitForSecs(0.3f);
                //
                playAudioQueuedScene(currentEvent(), "FINAL", true);
                //
                nextScene();
                return;
            }
            else
            {
                gotItRightBigTick(false);
                waitSFX();
                target.disable();
                //
                playSceneAudioIndex("CORRECT", correctAnswerCount - 1, false);
                if (correctAnswerCount == correctQuantity)
                {
                    waitAudio();
                    waitForSecs(0.3f);
                    //
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    playAudioQueuedScene(currentEvent(), "CORRECT2", true);
                    waitForSecs(0.3f);
                    //
                    playAudioQueuedScene(currentEvent(), "FINAL", true);
                    //
                    nextScene();
                    return;
                }
                else
                {
                    revertStatusAndReplayAudio();
                }
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3);
            action_lowlight(target);
            playAudioQueuedScene(currentEvent(), wrongAudio, false);
            revertStatusAndReplayAudio();
        }
    }


    public void checkTarget (OBControl targ)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            if (eventType.equals("demo"))
            {
                checkTarget_demo(targ);
            }
            else if (eventType.equals("choose"))
            {
                checkTarget_choose(targ);
            }
            else if (eventType.equals("showFill"))
            {
                checkTarget_showFill(targ);
            }
            else if (eventType.equals("reveal"))
            {
                checkTarget_reveal(targ);
            }
        }
        catch (Exception e)
        {
            Log.i("exception caught", e.toString());
            e.printStackTrace();
        }

    }


    public OBControl findTarget (PointF pt)
    {
        List invertedControls = zPositionSortedFilteredControls("obj.*");
        Collections.reverse(invertedControls);
        return finger(-1, 2, invertedControls, pt, true);
    }


}
