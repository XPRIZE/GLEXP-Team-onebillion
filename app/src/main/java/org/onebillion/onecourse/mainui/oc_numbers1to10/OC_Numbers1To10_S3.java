package org.onebillion.onecourse.mainui.oc_numbers1to10;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
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
 * Created by pedroloureiro on 03/05/2017.
 */

public class OC_Numbers1To10_S3 extends OC_Generic_Event
{
    int currentDot, lineToDrawOffsetIndex;
    OBPath shape;
    OBGroup selectedPaintpot, finalDemoGroup;
    List<OBPath> drawnLines;
    List<OBLabel> numbers;

    public void action_prepareScene (String scene, Boolean redraw)
    {
        if (finalDemoGroup != null) detachControl(finalDemoGroup);
        if (numbers != null)
        {
            for (OBControl control : numbers)
            {
                detachControl(control);
            }
        }
        numbers = action_addLabelsToObjects("number.*", 1.2f, false);
        for (OBLabel number : numbers)
        {
            number.disable();
        }
        shape = (OBPath) objectDict.get("colour_shape");
        shape.hide();
        //
        hideControls("number.*");
        hideControls("sidebar");
        hideControls("paint_.*");
        //
        for (OBGroup paintpot : (List<OBGroup>) (Object) filterControls("paint_.*"))
        {
            OBControl frame = paintpot.objectDict.get("selector_frame");
            frame.hide();
        }
        //
        selectedPaintpot = null;
        if (drawnLines != null)
        {
            for (OBControl line : drawnLines)
            {
                detachControl(line);
            }
        }
        drawnLines = new ArrayList<>();
    }



    public OBPath action_generateLine (int subPathIndex)
    {
        UPath mainPath = deconstructedPath(currentEvent(), (String) shape.attributes().get("id"));
        USubPath subPath = mainPath.subPaths.get(0);
        if (subPathIndex < 0 || subPathIndex > subPath.elements.size())
        {
            return null;
        }
        ULine line = subPath.elements.get(subPathIndex);
        Path bezier = new Path();
        bezier.moveTo(line.pt0.x, line.pt0.y);
        bezier.lineTo(line.pt1.x, line.pt1.y);
        OBPath lineToDraw = new OBPath();
        lineToDraw.setPath(bezier);
        lineToDraw.sizeToBox(new RectF(bounds()));
//        lineToDraw.setFrame(shape.frame());
        lineToDraw.setStrokeColor(Color.BLACK);
        lineToDraw.setStrokeEnd(1.0f);
        lineToDraw.setLineWidth(applyGraphicScale(6));
        attachControl(lineToDraw);
        lineToDraw.setZPosition(shape.zPosition());
        lineToDraw.show();
        drawnLines.add(lineToDraw);
        return lineToDraw;
    }

    public void action_generateLine_animated (OBControl selectedDot)
    {
        final OBPath lineToDraw;
        lockScreen();
        lineToDraw = action_generateLine(currentDot + lineToDrawOffsetIndex);
        if (lineToDraw != null) lineToDraw.setStrokeEnd(0.0f);
        for (OBControl dot : filterControls("obj.*"))
        {
            if (dot.equals(selectedDot))
            {
                dot.setFillColor(Color.RED);
            }
            else
            {
                dot.setFillColor(Color.BLACK);
            }
        }
        unlockScreen();
        //
        if (lineToDraw != null)
        {
            OBAnim anim = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock (float frac)
                {
                    lockScreen();
                    lineToDraw.setStrokeEnd(frac);
                    unlockScreen();
                }
            };
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
    }

    public void setScene3a ()
    {
        setSceneXX(currentEvent());
        action_generateLine(9);
        currentDot = 2;
        lineToDrawOffsetIndex = -2;
    }


    public void demo3a () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look.;
        OC_Generic.pointer_moveToObjectByName("colour_shape", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO", 1, false); // Touch dot one.;
        OC_Generic.pointer_moveToObjectByName("obj_1", -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("correct", false);
        //
        lockScreen();
        OBControl dot = objectDict.get("obj_1");
        dot.setFillColor(Color.RED);
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false); // Touch dot two.;
        OC_Generic.pointer_moveToObjectByName("obj_2", -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("correct", false);
        action_generateLine_animated(objectDict.get("obj_2"));
        currentDot++;
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        List audio = getAudioForScene(currentEvent(), "PROMPT");
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT"));
        playAudioQueued(audio, false);
    }

    public void finalDemo3a () throws Exception
    {
        playSfxAudio("star", false);
        OBAnim anim1 = OBAnim.rotationAnim((float) Math.toRadians(1800), shape);
        OBAnimationGroup.runAnims(Arrays.asList(anim1), 5.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitSFX();

    }

    public void setScene3b ()
    {
        setSceneXX(currentEvent());
        action_generateLine(0);
        action_generateLine(1);
        action_generateLine(2);
        currentDot = 1;
        lineToDrawOffsetIndex = 1;

    }

    public void finalDemo3b () throws Exception
    {
        playSfxAudio("boat", false);
        OBControl sails = objectDict.get("colour_sails");
        //
        lockScreen();
        PointF position = OC_Generic.copyPoint(shape.position());
        finalDemoGroup = new OBGroup(Arrays.asList(shape, sails));
        finalDemoGroup.setFrame(shape.frame);
        attachControl(finalDemoGroup);
        finalDemoGroup.setZPosition(shape.zPosition());
        finalDemoGroup.setPosition(position);
        finalDemoGroup.setAnchorPoint(new PointF(0.5f, 1f));
        unlockScreen();
        //
        OBAnim anim1 = OBAnim.rotationAnim((float) Math.toRadians(5), finalDemoGroup);
        OBAnim anim2 = OBAnim.rotationAnim((float) Math.toRadians(-10), finalDemoGroup);
        OBAnim anim3 = OBAnim.rotationAnim((float) Math.toRadians(0), finalDemoGroup);
        OBAnimationGroup.runAnims(Arrays.asList(anim1), 1.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBAnimationGroup.runAnims(Arrays.asList(anim2), 2.0, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBAnimationGroup.runAnims(Arrays.asList(anim1), 2.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBAnimationGroup.runAnims(Arrays.asList(anim3), 1.0, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitSFX();
    }

    public void finalDemo3c () throws Exception
    {
        playSfxAudio("car", false);
        OBGroup group;
        //
        lockScreen();
        OBControl obj_shape = objectDict.get("colour_shape");
        OBControl obj_wheels = objectDict.get("colour_wheels");
        OBControl obj_passenger = objectDict.get("nocolour_passenger");
        group = new OBGroup(Arrays.asList(obj_shape, obj_wheels, obj_passenger));
        attachControl(group);
        OC_Generic.sendObjectToTop(group, this);
        unlockScreen();
        //
        OBAnim anim = OBAnim.moveAnim(new PointF(group.position().x - bounds().width(), group.position().y), group);
        waitForSecs(1.0f);
        //
        OBAnimationGroup.runAnims(Arrays.asList(anim), 2.5, true, OBAnim.ANIM_EASE_IN, this);
        waitSFX();
        //
        playAudioQueuedScene("FINAL", 0.3f, true);
    }

    public void setScene3c ()
    {
        setSceneXX(currentEvent());
        action_generateLine(7);
        currentDot = 1;
        lineToDrawOffsetIndex = -2;
    }

    public void colourDemo () throws Exception
    {
        lockScreen();
        showControls("sidebar");
        showControls("paint.*");
        hideControls("obj.*");
        for (OBControl control : numbers) control.hide();
        showControls("colour_.*");
        for (OBControl line : drawnLines) line.hide();
        unlockScreen();
        //
        if (currentEvent().equals("3a"))
        {
            waitForSecs(0.3f);
            //
            loadPointer(POINTER_MIDDLE);
            playAudioScene("DEMO2", 0, false); // Now colour :.;
            movePointerToPoint(OB_Maths.locationForRect(new PointF(0.8f, 0.5f), bounds()), -15, 0.9f, true);
            waitAudio();
            waitForSecs(0.3f);
            //
            playAudioScene("DEMO2", 1, false); // Like this …;
            OBGroup selected_paintpot = (OBGroup) objectDict.get("paint_2");
            OC_Generic.pointer_moveToObject(selected_paintpot, -10, 0.8f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            playSfxAudio("paint_select", false);
            //
            lockScreen();
            for (OBGroup paintpot : (List<OBGroup>) (Object) filterControls("paint.*"))
            {
                OBControl frame = paintpot.objectDict.get("selector_frame");
                if (paintpot.equals(selected_paintpot)) frame.show();
                else frame.hide();
            }
            unlockScreen();
            //
            OBControl obj_shape = objectDict.get("colour_shape");
            OC_Generic.pointer_moveToObject(obj_shape, -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            int selectedPaintpotColour = selected_paintpot.objectDict.get("colour").fillColor();
            gotItRightBigTick(false);
            obj_shape.setFillColor(selectedPaintpotColour);
            waitForSecs(0.3f);
            //
            movePointerToPoint(new PointF(thePointer.position().x, thePointer.position().y + bounds().height() * 0.075f), -20, 0.3f, true);
            waitForSecs(0.3f);
            //
            thePointer.hide();
            waitForSecs(0.7f);
            //
            OBAnim anim1 = OBAnim.colourAnim("fillColor", Color.WHITE, obj_shape);
            OBAnimationGroup.runAnims(Arrays.asList(anim1), 0.2, true, OBAnim.ANIM_LINEAR, this);
            obj_shape.enable();
            //
            setStatus(STATUS_AWAITING_CLICK);
            //
            List audio = getAudioForScene(currentEvent(), "PROMPT3");
            setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT3"));
            playAudioQueued(audio, false);
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
            List audio = getAudioForScene(currentEvent(), "PROMPT2");
            setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
            playAudioQueued(audio, false);

        }

    }

    public void checkDot (OBControl selectedDot) throws Exception
    {
        setStatus(STATUS_CHECKING);
        OBControl correctDot = objectDict.get(String.format("obj_%d", currentDot));
        if (selectedDot.equals(correctDot))
        {
            correctDot.disable();
            gotItRightBigTick(false);
            final OBPath lineToDraw;
            //
            lockScreen();
            lineToDraw = action_generateLine(currentDot + lineToDrawOffsetIndex);
            if (lineToDraw != null) lineToDraw.setStrokeEnd(0.0f);
            for (OBControl dot : filterControls("obj.*"))
            {
                if (dot.equals(correctDot)) dot.setFillColor(Color.RED);
                else dot.setFillColor(Color.BLACK);
            }
            unlockScreen();
            //
            if (lineToDraw != null)
            {
                OBAnim anim = new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock (float frac)
                    {
                        lineToDraw.setStrokeEnd(frac);
                    }
                };
                OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            }
            currentDot++;
            int totalDots = (int) filterControls("obj.*").size();
            if (currentDot > totalDots)
            {
                waitForSecs(0.3f);
                gotItRightBigTick(true);
                selectedDot.setFillColor(Color.BLACK);
                waitForSecs(0.3f);
                colourDemo();
                return;
            }
            else
            {
                if (currentEvent().equals("3a") && currentDot == 4)
                {
                    List audio = (List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT2");
                    setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
                    playAudioQueued(audio, false);
                }
            }
        }
        else
        {
            gotItWrongWithSfx();
        }
        setStatus(STATUS_AWAITING_CLICK);
    }


    public OBControl findDot (PointF pt)
    {
        return finger(0, 2, filterControls("obj_.*"), pt, true);
    }

    public OBControl findPaintPot (PointF pt)
    {
        return finger(0, 2, filterControls("paint_.*"), pt, true);

    }

    public OBControl findObjectToColour (PointF pt)
    {
        return finger(0, 1, zPositionSortedFilteredControls("colour_.*"), pt, true);

    }

    public void checkPaintPot (OBGroup selected) throws Exception
    {
        setStatus(STATUS_CHECKING);
        playSfxAudio("paint_select", false);
        lockScreen();
        for (OBGroup paintpot : (List<OBGroup>) (Object) filterControls("paint.*"))
        {
            OBControl frame = paintpot.objectDict.get("selector_frame");
            if (paintpot.equals(selected)) frame.show();
            else frame.hide();
        }
        unlockScreen();
        selectedPaintpot = selected;
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkObjectToColour(OBControl selectedObject) throws Exception
    {
        setStatus(STATUS_CHECKING);
        if (selectedPaintpot != null)
        {
            int selectedPaintpotColour = selectedPaintpot.objectDict.get("colour").fillColor();
            boolean colourBeingUsed = false;
            for (OBControl control : filterControls("colour_.*"))
            {
                if (control.fillColor() == selectedPaintpotColour)
                {
                    colourBeingUsed = true;
                }
            }
            if (colourBeingUsed)
            {
                gotItWrongWithSfx();
            }
            else
            {
                gotItRightBigTick(false);
                //
                selectedObject.disable();
                selectedObject.setFillColor(selectedPaintpotColour);
                //
                boolean allColoured = true;
                //
                for (OBControl control : filterControls("colour_.*"))
                {
                    if (control.isEnabled()) allColoured = false;
                }
                //
                if (allColoured)
                {
                    waitForSecs(0.3f);
                    //
                    gotItRightBigTick(true);
                    performSel("finalDemo", currentEvent());
                    waitForSecs(0.7f);
                    //
                    nextScene();
                    return;
                }
            }
        }
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl dot = findDot(pt);
            if (dot != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkDot(dot);

                    }
                });
            }
            else
            {
                final OBGroup paintpot = (OBGroup) findPaintPot(pt);
                if (paintpot != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkPaintPot(paintpot);
                        }
                    });
                }
                else
                {
                    final OBControl obj = findObjectToColour(pt);
                    if (obj != null)
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                checkObjectToColour(obj);
                            }
                        });
                    }
                }
            }
        }
    }


}
