package org.onebillion.xprz.mainui.x_counting5and10;

import android.graphics.Color;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Event;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class X_Counting5and10_S6 extends XPRZ_Generic_Event
{
    int currentAnswer;
    int selectedColour;
    List<OBControl> colourableObjects;


    public X_Counting5and10_S6 ()
    {
        super();
    }


    public void setSceneXX (String scene)
    {
        deleteControls(".*");
        //
        super.setSceneXX(scene);
        //
        int count = 0;
        for (OBControl number : sortedFilteredControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number, 1.2f);
            objectDict.put(String.format("label_%d", count), label);
            number.hide();
            count++;
        }
        //
        for (OBControl control : filterControls("paint.*"))
        {
            OBGroup group = (OBGroup) control;
            group.hideMembers("selector_frame");
        }
        //
        for (OBControl control : filterControls("path.*"))
        {
            OBPath path = (OBPath) control;
            path.setLineWidth(applyGraphicScale(path.lineWidth()));
            path.sizeToBoundingBoxIncludingStroke();
            path.hide();
        }
        //
        for (OBControl control : filterControls("permanent_.*"))
        {
            OBPath path = (OBPath) control;
            path.setLineWidth(applyGraphicScale(path.lineWidth()));
            path.sizeToBoundingBoxIncludingStroke();
        }
        //
        currentAnswer = 0;
        colourableObjects = new ArrayList<OBControl>();
        OBGroup object = (OBGroup) objectDict.get("object");
        if (object != null)
        {
            List<OBControl> bodyParts = XPRZ_Generic.controlsSortedFrontToBack(object, "c_.*");
            colourableObjects.addAll(bodyParts);
            OBControl background = objectDict.get("background");
            if (background != null)
            {
                colourableObjects.add(background);
            }
        }
    }


    public void setScene6d ()
    {
        setSceneXX(currentEvent());
        //
        hideControls("fly.*");
    }


    public void demo6a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        XPRZ_Generic.pointer_moveToObjectByName("dot_0", -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Zero.
        waitAudio();
        waitForSecs(0.3);
        //
        XPRZ_Generic.pointer_moveToObjectByName("dot_1", -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Ten.
        waitAudio();
        waitForSecs(0.3);
        //
        XPRZ_Generic.pointer_moveToObjectByName("dot_2", -5, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Twenty.
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void demo6b () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(true); // Good!
        waitForSecs(0.3);
        //
        loadPointer(POINTER_MIDDLE);
        action_playNextDemoSentence(false); // Now colour in ALL the white parts of the picture.
        XPRZ_Generic.pointer_moveToObjectByName("object", 5, 0.9f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        XPRZ_Generic.pointer_moveToObjectByName("paint_1", 10, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_LEFT), true, this);
        action_playNextDemoSentence(false); // Choose colours from here.
        XPRZ_Generic.pointer_moveToObjectByName("paint_6", 10, 0.9f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void finDemo6b () throws Exception
    {
        OBGroup object = (OBGroup) objectDict.get("object");
        lockScreen();
        OBControl finalObject = object.objectDict.get("final");
        if (finalObject != null)
        {
            finalObject.show();
        }
        unlockScreen();
        //
        OBAnim bodyAnim = OBAnim.sequenceAnim(object, Arrays.asList("c_body", "body_2", "body_3", "body_4", "body_3", "body_2"), 0.1f, true);
        playSfxAudio("owl", false);
        OBAnimationGroup.runAnims(Arrays.asList(bodyAnim), 1.5f, true, OBAnim.ANIM_LINEAR, this);
        OBAudioManager.audioManager.stopPlayingSFX();
        waitForSecs(0.3);
    }


    public void finDemo6d () throws Exception
    {
        OBGroup frog = (OBGroup) objectDict.get("object");
        final OBGroup fly = (OBGroup) objectDict.get("fly");
        final OBPath path = (OBPath) objectDict.get("fly_path");
        path.sizeToBox(new RectF(bounds()));
        //
        OBAnim pathAnim = OBAnim.pathMoveAnim(fly, path.path(), false, 0);
        OBAnim flyAnim = OBAnim.sequenceAnim(fly, Arrays.asList("frame_1", "frame_2"), 0.1f, true);
        //
        playSfxAudio("frog", false);
        OBAnimationGroup flyAnimations = new OBAnimationGroup();
        flyAnimations.applyAnimations(Arrays.asList(pathAnim, flyAnim), 2.5, false, OBAnim.ANIM_LINEAR, this);
        //
        fly.show();
        waitForSecs(0.3);
        //
        OBAnim blinkAnim = OBAnim.sequenceAnim(frog, Arrays.asList("head_1", "c_head"), 0.1f, false);
        OBAnimationGroup.runAnims(Arrays.asList(blinkAnim), 0.2f, true, OBAnim.ANIM_LINEAR, this);
        waitForSecs(0.3);
        //
        OBAnimationGroup.runAnims(Arrays.asList(blinkAnim), 0.2f, true, OBAnim.ANIM_LINEAR, this);
        //
        lockScreen();
        frog.hideMembers("c_eyes");
        frog.hideMembers("c_head");
        frog.showMembers("head_1");
        unlockScreen();
        waitForSecs(0.6);
        //
        lockScreen();
        frog.hideMembers("head_1");
        frog.showMembers("head_2");
        unlockScreen();
        waitForSecs(0.4);
        //
        OBAnim frogAnim = OBAnim.sequenceAnim(frog, Arrays.asList("head_2", "head_3", "head_4"), 0.1f, false);
        OBAnimationGroup frogAnimations = new OBAnimationGroup();
        frogAnimations.applyAnimations(Arrays.asList(frogAnim), 0.4, true, OBAnim.ANIM_LINEAR, this);
        //
        OBControl position1 = objectDict.get("fly_position_1");
        OBControl position2 = objectDict.get("fly_position_2");
        OBControl position3 = objectDict.get("fly_position_3");
        //
        lockScreen();
        frog.hideMembers("head_4");
        frog.showMembers("head_5");
        fly.setPosition(position1.position());
        fly.setScale(0.7f * fly.scale());
        unlockScreen();
        waitForSecs(0.1);
        //
        lockScreen();
        frog.hideMembers("head_5");
        frog.showMembers("head_6");
        fly.setPosition(position2.position());
        fly.setScale(0.7f * fly.scale());
        unlockScreen();
        waitForSecs(0.1);
        //
        lockScreen();
        frog.hideMembers("head_6");
        frog.showMembers("head_2");
        fly.setPosition(position3.position());
        fly.setScale(0.7f * fly.scale());
        unlockScreen();
        waitForSecs(0.1);
        //
        lockScreen();
        frog.hideMembers("head_2");
        frog.showMembers("head_1");
        frog.showMembers("c_eyes");
        fly.setPosition(position3.position());
        fly.setScale(0.7f * fly.scale());
        fly.hide();
        unlockScreen();
        waitForSecs(0.1);
        //
        blinkAnim = OBAnim.sequenceAnim(frog, Arrays.asList("head_1", "c_head"), 0.1f, false);
        OBAnimationGroup.runAnims(Arrays.asList(blinkAnim), 0.2, true, OBAnim.ANIM_LINEAR, this);
        OBAnimationGroup.runAnims(Arrays.asList(blinkAnim), 0.2, true, OBAnim.ANIM_LINEAR, this);
    }


    public void action_tracePath (int pathNumber) throws Exception
    {
        final OBPath line = (OBPath) objectDict.get(String.format("path_%d", pathNumber));
        if (line == null) return;
        //
        line.setStrokeEnd(0.0f);
        line.setStrokeColor(Color.RED);
        line.show();
        //
        playSfxAudio("draw_line", false);
        float duration = line.length() / applyGraphicScale(400);
        //
        OBAnim pathAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                line.setStrokeEnd(frac);
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(pathAnim), duration, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
//        long starttime = SystemClock.uptimeMillis();
//        float frac = 0;
//        while (frac <= 1.0)
//        {
//            try
//            {
//                long currtime = SystemClock.uptimeMillis();
//                frac = (float) (currtime - starttime) / (duration * 1000);
//                final float t = (frac);
//                new OBRunnableSyncUI()
//                {
//                    public void ex ()
//                    {
//                        line.setStrokeEnd(t);
//                    }
//                }.run();
//                waitForSecs(0.02f);
//            }
//            catch (Exception e)
//            {
//                break;
//            }
//        }
        //
        line.setStrokeColor(Color.BLACK);
        OBAudioManager.audioManager.stopPlayingSFX();
    }


    public OBControl findDot (PointF pt)
    {
        return finger(-1, 2, filterControls("dot.*"), pt, true);
    }


    public void checkDot (OBControl dot)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        OBControl correctDot = objectDict.get(String.format("dot_%d", currentAnswer));
        String dotID = (String) dot.attributes().get("id");
        OBLabel selectedNumber = (OBLabel) objectDict.get(dotID.replace("dot", "label"));
        OBLabel previousNumber = (OBLabel) objectDict.get(String.format("label_%d", currentAnswer - 1));
        //
        try
        {
            if (dot.equals(correctDot))
            {
                gotItRightBigTick(false);
                dot.disable();
                //
                lockScreen();
                for (OBControl control : filterControls("dot.*"))
                {
                    ((OBPath) control).setFillColor(Color.BLACK);
                }
                ((OBPath) dot).setFillColor(Color.RED);
                selectedNumber.setColour(Color.RED);
                if (previousNumber != null)
                {
                    previousNumber.hide();
                }
                unlockScreen();
                waitForSecs(0.3);
                //
                action_tracePath(currentAnswer);
                playSceneAudioIndex("CORRECT", currentAnswer, false);
                currentAnswer++;
                //
                if (currentAnswer > 10)
                {
                    waitForSecs(0.1);
                    waitAudio();
                    waitForSecs(0.3);
                    //
                    gotItRightBigTick(true);
                    waitForSecs(0.3);
                    //
                    nextScene();
                }
                else
                {
                    revertStatusAndReplayAudio();
                }
            }
            else
            {
                lockScreen();
                ((OBPath) dot).setFillColor(Color.RED);
                selectedNumber.setColour(Color.RED);
                unlockScreen();
                //
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                lockScreen();
                ((OBPath) dot).setFillColor(Color.BLACK);
                selectedNumber.setColour(Color.BLACK);
                unlockScreen();
                //
                revertStatusAndReplayAudio();
            }
        }
        catch (Exception e)
        {
            System.out.println("X_Counting5and10_S6.checkDot.exception caught: " + e.toString());
            e.printStackTrace();
        }
    }


    public OBControl findPainPot (PointF pt)
    {
        return finger(-1, 2, filterControls("paint.*"), pt);
    }


    public void checkPaintPot (OBControl paintpot)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            playSfxAudio("select_colour", false);
            //
            lockScreen();
            for (OBControl control : filterControls("paint.*"))
            {
                ((OBGroup) control).objectDict.get("selector_frame").setHidden(!control.equals(paintpot));
            }
            unlockScreen();
            //
            selectedColour = OBUtils.colorFromRGBString((String) paintpot.attributes().get("fill"));
            //
            revertStatusAndReplayAudio();
        }
        catch (Exception e)
        {
            System.out.println("X_Counting5and10_S6.checkPaintPot.exception caught: " + e.toString());
            e.printStackTrace();
        }
    }


    public OBControl findColourableObject (PointF pt)
    {
        OBGroup object = (OBGroup) objectDict.get("object");
        if (object != null)
        {
            List<OBControl> bodyParts = XPRZ_Generic.controlsSortedFrontToBack(object, "c_.*");
            //
            OBControl bodyPart = finger(-1, 2, bodyParts, pt, true);
            if (bodyPart != null) return bodyPart;
            //
            OBControl background = finger(-1, 2, filterControls("background.*"), pt, true);
            if (background != null) return background;
        }
        //
        return null;
    }


    public void checkColourableObject (OBControl selectedObject, PointF pt)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            if (selectedColour == 0)
            {
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                revertStatusAndReplayAudio();
            }
            else
            {
                if (colourableObjects.contains(selectedObject))
                {
                    playSfxAudio("colour_object", false);
                    //
                    if (OBGroup.class.isInstance(selectedObject))
                    {
                        OBGroup group = (OBGroup) selectedObject;
                        group.substituteFillForAllMembers("colour.*", selectedColour);
                        //
                        if (currentEvent().equals("6b"))
                        {
                            String layerName = (String) group.settings.get("name");
                            if (layerName.equals("c_beak"))
                            {
                                OBGroup object = (OBGroup) objectDict.get("object");
                                for (String name : Arrays.asList("beak_open"))
                                {
                                    group = (OBGroup) object.objectDict.get(name);
                                    group.substituteFillForAllMembers("colour.*", selectedColour);
                                }
                            }
                            else if (layerName.equals("c_body"))
                            {
                                OBGroup object = (OBGroup) objectDict.get("object");
                                for (String name : Arrays.asList("body_2", "body_3", "body_4"))
                                {
                                    group = (OBGroup) object.objectDict.get(name);
                                    group.substituteFillForAllMembers("colour.*", selectedColour);
                                }
                            }
                        }
                        else if (currentEvent().equals("6d"))
                        {
                            String layerName = (String) group.settings.get("name");
                            //
                            if (layerName.equals("c_head"))
                            {
                                OBGroup object = (OBGroup) objectDict.get("object");
                                for (String name : Arrays.asList("head_1", "head_2", "head_3", "head_4", "head_5", "head_6"))
                                {
                                    group = (OBGroup) object.objectDict.get(name);
                                    group.substituteFillForAllMembers("colour.*", selectedColour);
                                }
                            }
                        }
                    }
                    else if (OBPath.class.isInstance(selectedObject))
                    {
                        OBPath path = (OBPath) selectedObject;
                        path.setFillColor(selectedColour);
                    }
                    //
                    colourableObjects.remove(selectedObject);
                    selectedObject.disable();
                    //
                    if (colourableObjects.size() == 0)
                    {
                        waitForSecs(0.3);
                        //
                        gotItRightBigTick(true);
                        waitForSecs(0.3);
                        //
                        performSel("finDemo", currentEvent());
                        //
                        playAudioQueuedScene(currentEvent(), "FINAL", true);
                        nextScene();
                    }
                    else
                    {
                        revertStatusAndReplayAudio();
                    }
                }
                else
                {
                    revertStatusAndReplayAudio();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("X_Counting5and10_S6.checkColourableObject.exception caught: " + e.toString());
            e.printStackTrace();
        }
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl dot = findDot(pt);
            final OBControl paintpot = findPainPot(pt);
            final OBControl colourableObject = findColourableObject(pt);
            //
            if (dot != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkDot(dot);
                    }
                });
            }
            else if (paintpot != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkPaintPot(paintpot);
                    }
                });
            }
            else if (colourableObject != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkColourableObject(colourableObject, pt);
                    }
                });
            }
        }
    }

}
