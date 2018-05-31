package org.onebillion.onecourse.mainui.oc_flipping;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.generic.OC_Generic.action_createLabelForControl;
import static org.onebillion.onecourse.mainui.generic.OC_Generic.adjustColour;
import static org.onebillion.onecourse.mainui.generic.OC_Generic.randomInt;
import static org.onebillion.onecourse.utils.OBAnim.ANIM_EASE_IN_EASE_OUT;

/**
 * Created by pedroloureiro on 30/05/2018.
 */

public class OC_FlippingEquations extends OC_SectionController
{
    OBPresenter presenter;
    List<Map<String, Object>> equationNumbers;
    int colourTextReversable;
    int colourTextNormal;
    int colourTextHilited;
    int colourTextMovable;
    int colourBoxHilited;
    int colourBoxNormal;
    int colourUnderlineNormal;
    int colourUnderlineHilited;
    List<OBLabel> addedLabels;
    List<OBLabel> equation1Labels;
    List<OBLabel> equation2Labels;
    List<OBLabel> optionLabels;
    double lastActionTimestamp;
    double reminderDelay;
    OBPath underline;
    OBPath phase1Box;
    OBPath phase2Box;
    List<String> phase1Events;
    List<String> phase2Events;
    List<String> phase3Events;
    List<String> flippingEquationEvents;
    boolean equationWasFlipped;
    OBAnimationGroup flippingAnimation;

    public void prepare()
    {
        super.prepare();
        super.prepare();
        setStatus(STATUS_BUSY);
        buildEquationNumbers();
        buildScene();
        buildEvents();
        equationWasFlipped = false;
        reminderDelay = 5.0;
        doVisual(currentEvent());

    }

    public void buildEquationNumbers()
    {
        List<Map<String, Object>> equations = new ArrayList<>();
        //
        Map<String, Object> equation1 = new HashMap<>();
        equation1.put("part1", "2");
        equation1.put("part2", "3");
        equation1.put("total", "5");
        equation1.put("options", Arrays.asList("8,5,1".split(",")));
        equations.add(equation1);
        //
        Map<String, Object> equation2 = new HashMap<>();
        equation2.put("part1", "1");
        equation2.put("part2", "9");
        equation2.put("total", "10");
        equation2.put("options", Arrays.asList("10,5,8".split(",")));
        equations.add(equation2);
        //
        Map<String, Object> equation3 = new HashMap<>();
        equation3.put("part1", "3");
        equation3.put("part2", "12");
        equation3.put("total", "15");
        equation3.put("options", Arrays.asList("4,9,15".split(",")));
        equations.add(equation3);
        //
        Map<String, Object> equation4 = new HashMap<>();
        equation4.put("part1", "1");
        equation4.put("part2", "11");
        equation4.put("total", "12");
        equation4.put("options", Arrays.asList("10,13,12".split(",")));
        equations.add(equation4);
        //
        int loopThreshold = 10000;              // just a failsafe, in case my limits and conditions overlook a certain pair
        int loopCounter = 0;
        while (equations.size() < 7)
        {
            if (++loopCounter > loopThreshold)
            {
                MainActivity.log("OC_FlippingEquation:buildEquationNumbers: loop has been killed");
                break;
            }
            int b = randomInt(6, 15);
            int a = randomInt(1, b - 5);
            int c = a + b;
            //
            if (c > 20) continue;
            if (b - a <= 4) continue;
            //
            String part1 = String.format("%d", a);
            String part2 = String.format("%d", b);
            String total = String.format("%d", c);
            //
            boolean alreadyUsed = false;
            for (Map<String, Object> equation : equations)
            {
                if (part1.equals(equation.get("part1")) && part2.equals(equation.get("part2")))
                {
                    alreadyUsed = true;
                    break;
                }
            }
            if (alreadyUsed) continue;
            //
            int distractor1 = 0;
            while (distractor1 == 0 || distractor1 == c || distractor1 < c - 10 || distractor1 > c + 4)
            {
                if (++loopCounter > loopThreshold)
                {
                    MainActivity.log("OC_FlippingEquation:buildEquationNumbers: loop has been killed");
                    break;
                }
                distractor1 = randomInt(0, 20);
            }
            //
            int distractor2 = 0;
            while (distractor2 == 0 || distractor2 == c || distractor2 < c - 10 || distractor2 > c + 4 || distractor1 == distractor2)
            {
                if (++loopCounter > loopThreshold)
                {
                    MainActivity.log("OC_FlippingEquation:buildEquationNumbers: loop has been killed");
                    break;
                }
                distractor2 = randomInt(0, 20);
            }
            //
            Map<String, Object> equation = new HashMap();
            equation.put("part1", part1);
            equation.put("part2", part2);
            equation.put("total", total);
            equation.put("options", OBUtils.randomlySortedArray(Arrays.asList(String.format("%d,%d,%d", distractor2, distractor1, c).split(","))));
            ;
            equations.add(equation);
        }
        equationNumbers = new ArrayList<>();
        equationNumbers.addAll(equations);
    }

    public void buildEvents()
    {
        String demoString = parameters.get("demo");
        boolean demo = demoString != null && demoString.equals("true");
        //
        List<String> newEvents = new ArrayList<>();
        //
        newEvents.add((demo) ? "intro1" : "intro2");
        newEvents.addAll(Arrays.asList("a,b,c,d,e,f,g,h,i,j,k,l,m,n".split(",")));
        //
        phase1Events = Arrays.asList("a,d,g".split(","));
        phase2Events = Arrays.asList("b,e,h".split(","));
        phase3Events = Arrays.asList("c,f,i".split(","));
        flippingEquationEvents = Arrays.asList("j,k,l,m,n".split(","));
        //
        events = new ArrayList<>();
        events.addAll(newEvents);
        //
        String eventsViaParameters = parameters.get("events");
        if (eventsViaParameters != null)
        {
            events = new ArrayList<>();
            events.addAll(Arrays.asList(eventsViaParameters.split(",")));
        }
    }

    public void buildScene()
    {
        loadFingers();
        loadEvent("master1");
        loadEvent("equations");
        //
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restpos", OC_Generic.copyPoint(presenter.control.position()));
            presenter.control.setRight(0);
            presenter.control.show();
        }
        colourTextReversable = objectDict.get("colour_text_reversable").fillColor();
        colourTextNormal = objectDict.get("colour_text_normal").fillColor();
        colourTextHilited = objectDict.get("colour_text_hilited").fillColor();
        colourTextMovable = objectDict.get("colour_text_movable").fillColor();
        colourBoxHilited = objectDict.get("colour_box_hilited").fillColor();
        colourBoxNormal = objectDict.get("colour_box_normal").fillColor();
        colourUnderlineNormal = objectDict.get("colour_underline_normal").fillColor();
        colourUnderlineHilited = objectDict.get("colour_underline_hilited").fillColor();
        //
        underline = (OBPath) objectDict.get("underline");
        underline.hide();
        //
        phase1Box = (OBPath) objectDict.get("phase1_equation_total");
        phase1Box.hide();
        //
        phase2Box = (OBPath) objectDict.get("phase2_equation_total");
        phase2Box.hide();
        //
        hideControls("colour.*");
        hideControls("phase1.*");
        hideControls("phase2.*");
        hideControls("phase3.*");
        hideControls("background.*");
        hideControls("path.*");
        //
        OBControl bottomBar = objectDict.get("background_bottom_bar");
        bottomBar.show();
    }

    public void start()
    {
        setStatus(STATUS_BUSY);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if (!performSel("demo", currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                    MainActivity.log("OC_FlippingEquations --> Exception caught %s", exception.getMessage());
                    exception.printStackTrace();
                }
            }
        });
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                boolean statusWasIdle = true;
                while (!_aborting)
                {
                    if (status() != STATUS_AWAITING_CLICK)
                    {
                        statusWasIdle = false;
                    }
                    else
                    {
                        if (!statusWasIdle)
                        {
                            statusWasIdle = true;
                            updateLastActionTimeStamp();
                        }
                        double currentTimeStamp = OC_Generic.currentTime();
                        double idleTime = currentTimeStamp - lastActionTimestamp;
                        //
                        if (idleTime > reminderDelay)
                        {
                            if (currentEvent().equals("j"))
                            {
                                flashUnderline();
                            }
                            else
                            {
                                flashBox();
                            }
                            updateLastActionTimeStamp();
                        }

                    }
                    Thread.sleep(1000);
                }
            }
        });
    }

    public boolean isPhase(int phase, String scene)
    {
        if (scene == null)
        {
            scene = currentEvent();
        }
        if (phase == 1)
        {
            return phase1Events.contains(scene);
        }
        else if (phase == 2)
        {
            return phase2Events.contains(scene);
        }
        else if (phase == 3)
        {
            return phase3Events.contains(scene);
        }
        return false;
    }


    public void setSceneXX(String scene)
    {
        List<String> doNothingEvents = Arrays.asList("intro1,intro2".split(","));
        if (doNothingEvents.contains(scene)) return;
        //
        List<String> equation1 = Arrays.asList("a,b,c".split(","));
        List<String> equation2 = Arrays.asList("d,e,f".split(","));
        List<String> equation3 = Arrays.asList("g,h,i".split(","));
        List<String> equation4 = Arrays.asList("j,k".split(","));
        //
        boolean isPhase1 = isPhase(1, scene);
        boolean isPhase2 = isPhase(2, scene);
        boolean isPhase3 = isPhase(3, scene);
        boolean isFlippingPhase = flippingEquationEvents.contains(scene);
        //
        int equationIndex = -1;
        if (equation1.contains(scene)) equationIndex = 0;
        else if (equation2.contains(scene)) equationIndex = 1;
        else if (equation3.contains(scene)) equationIndex = 2;
        else if (equation4.contains(scene)) equationIndex = 3;
        else
            equationIndex = flippingEquationEvents.indexOf(scene) + 4 - 2;     // +4 for the right equation index and -2 for the two demo events displacement
        //
        Map<String, Object> equation = equationNumbers.get(equationIndex);
        if (addedLabels != null)
        {
            if (addedLabels.size() > 0)
            {
                for (OBLabel label : addedLabels)
                {
                    detachControl(label);
                }
            }
        }
        //
        addedLabels = new ArrayList<>();
        equation1Labels = new ArrayList<>();
        equation2Labels = new ArrayList<>();
        optionLabels = new ArrayList<>();
        //
        float resizeFactorForEquation = 1.2f;
        OBControl part1Box = objectDict.get("phase1_equation_part_1");
        OBLabel part1Label = action_createLabelForControl(part1Box, (String) equation.get("part1"), resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        part1Label.hide();
        addedLabels.add(part1Label);
        equation1Labels.add(part1Label);
        //
        OBControl signBox = objectDict.get("phase1_equation_sign");
        OBLabel signLabel = action_createLabelForControl(signBox, "+", resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        signLabel.hide();
        addedLabels.add(signLabel);
        equation1Labels.add(signLabel);
        //
        OBControl part2Box = objectDict.get("phase1_equation_part_2");
        OBLabel part2Label = action_createLabelForControl(part2Box, (String) equation.get("part2"), resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        part2Label.hide();
        addedLabels.add(part2Label);
        equation1Labels.add(part2Label);
        //
        OBControl equalsBox = objectDict.get("phase1_equation_equals");
        OBLabel equalsLabel = action_createLabelForControl(equalsBox, "=", resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        equalsLabel.hide();
        addedLabels.add(equalsLabel);
        equation1Labels.add(equalsLabel);
        //
        OBControl totalBox = objectDict.get("phase1_equation_total");
        totalBox.setProperty("correct_number", equation.get("total"));
        totalBox.hide();
        OBLabel totalLabel = action_createLabelForControl(totalBox, (String) equation.get("total"), resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        totalLabel.hide();
        totalBox.setProperty("label", totalLabel);
        addedLabels.add(totalLabel);
        equation1Labels.add(totalLabel);
        //
        underline.sizeToBox(boundsf());
        //
        PointF firstPoint = underline.firstPoint();
        firstPoint.x = part1Box.left();
        PointF lastPoint = underline.lastPoint();
        lastPoint.x = part2Box.right();
        //
        Path newPath = new Path();
        newPath.moveTo(firstPoint.x, firstPoint.y);
        newPath.lineTo(lastPoint.x, lastPoint.y);
        //
        underline.setPath(newPath);
        underline.sizeToBoundingBoxIncludingStroke();
        //
        if (isPhase2 || isPhase3)
        {
            part1Box = objectDict.get("phase2_equation_part_1");
            part1Label = action_createLabelForControl(part1Box, (String) equation.get("part2"), resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            part1Label.hide();
            addedLabels.add(part1Label);
            equation2Labels.add(part1Label);
            //
            signBox = objectDict.get("phase2_equation_sign");
            signLabel = action_createLabelForControl(signBox, "+", resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            signLabel.hide();
            addedLabels.add(signLabel);
            equation2Labels.add(signLabel);
            //
            part2Box = objectDict.get("phase2_equation_part_2");
            part2Label = action_createLabelForControl(part2Box, (String) equation.get("part1"), resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            part2Label.hide();
            addedLabels.add(part2Label);
            equation2Labels.add(part2Label);
            //
            equalsBox = objectDict.get("phase2_equation_equals");
            equalsLabel = action_createLabelForControl(equalsBox, "=", resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            equalsLabel.hide();
            addedLabels.add(equalsLabel);
            equation2Labels.add(equalsLabel);
            //
            totalBox = objectDict.get("phase2_equation_total");
            totalBox.setProperty("correct_number", equation.get("total"));
            totalBox.hide();
            totalLabel = action_createLabelForControl(totalBox, (String) equation.get("total"), resizeFactorForEquation, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            totalLabel.hide();
            totalBox.setProperty("label", totalLabel);
            addedLabels.add(totalLabel);
            equation2Labels.add(totalLabel);
        }
        //
        if (!isPhase3)
        {
            OBControl bottomBar = objectDict.get("background_bottom_bar");
            //
            float resizeFactorForOptions = 0.9f;
            int optionIndex = 0;
            List<String> options = (List<String>) equation.get("options");
            //
            for (OBControl placement : filterControls("background_place_.*"))
            {
                String number = options.get(optionIndex);
                OBLabel optionLabel = action_createLabelForControl(placement, number, resizeFactorForOptions, false, OBUtils.standardTypeFace(), colourTextMovable, this);
                PointF newPosition = OC_Generic.copyPoint(optionLabel.position());
                optionLabel.setProperty("original_position", OC_Generic.copyPoint(newPosition));
                optionLabel.setProperty("original_colour", colourTextMovable);
                optionLabel.setZPosition(bottomBar.zPosition() + 1.0f);
                //
                if (isPhase1 || isFlippingPhase)
                {
                    newPosition.x -= bounds().width();
                    optionLabel.setPosition(newPosition);
                }
                addedLabels.add(optionLabel);
                optionLabels.add(optionLabel);
                optionIndex++;
            }
        }
        //
        for (OBLabel label : addedLabels)
        {
            attachControl(label);

        }
        //
        if (isPhase2 || isPhase3)
        {
            for (OBLabel label : equation1Labels)
            {
                label.show();
            }
        }
        if (isPhase3)
        {
            for (OBLabel label : equation2Labels)
            {
                label.show();
            }
        }
    }

    public void setScenek()
    {
        setSceneXX(currentEvent());
        for (int i = 0; i < equation1Labels.size() - 1; i++)
        {
            OBLabel label = equation1Labels.get(i);
            label.show();
        }
        phase1Box.show();
        hiliteEquation(equation1Labels, false, true);
        OBLabel part1 = equation1Labels.get(0);
        OBLabel part2 = equation1Labels.get(2);
        part1.setPosition((PointF) part2.propertyValue("original_position"));
        part2.setPosition((PointF) part1.propertyValue("original_position"));
    }


    public void doMainXX() throws Exception
    {
        if (currentEvent().equals("a")) return;
        //
        playAudioQueuedScene("DEMO", 0.3f, true);
        //
        if (isPhase(1, currentEvent()) || flippingEquationEvents.contains(currentEvent()))
        {
            popInEquationWithBox(equation1Labels);
            waitForSecs(0.3f);
            //
            animateOptions(true);
            if (flippingEquationEvents.contains(currentEvent()))
            {
                waitForSecs(0.3f);
                //
                popInUnderline();
            }
        }
        else if (isPhase(2, currentEvent()))
        {
            popInEquationWithBox(equation2Labels);
            waitForSecs(0.6f);
        }
        doAudio(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void doAudio(String scene) throws Exception
    {
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT"));
        List audio = getAudioForScene(currentEvent(), "PROMPT");
        playAudioQueued(audio, false);
    }

    public void replayAudio()
    {
        updateLastActionTimeStamp();
        super.replayAudio();
    }

    public void animateOptions(boolean enter) throws Exception
    {
        List<OBAnim> animations = new ArrayList<>();
        for (OBLabel label : optionLabels)
        {
            PointF destination = OC_Generic.copyPoint((PointF) label.propertyValue("original_position"));
            if (!enter)
            {
                destination.x += bounds().width();
            }
            OBAnim moveAnim = OBAnim.moveAnim(destination, label);
            animations.add(moveAnim);
        }
        playSfxAudio("slide", false);
        OBAnimationGroup.runAnims(animations, 0.6, true, ANIM_EASE_IN_EASE_OUT, this);
    }

    public void flipEquation(final boolean value, final OBUtils.RunLambda completionBlock) throws Exception
    {
        if (value && equationWasFlipped) return;
        if (!value && !equationWasFlipped) return;
        //
        if (flippingAnimation != null && flippingAnimation.lock != null && flippingAnimation.lock.conditionValue() != PROCESS_DONE)
        {
            MainActivity.log("OC_FlippingEquations: flipEquation:completionBlock --> animation not done yet. Ignoring request to flip");
            return;
        }
        equationWasFlipped = value;
        //
        List<OBAnim> animations = new ArrayList<>();
        OBPath path1 = (OBPath) objectDict.get("path1");
        path1.sizeToBox(boundsf());
        //
        OBPath path2 = (OBPath) objectDict.get("path2");
        path2.sizeToBox(boundsf());
        //
        OBLabel equationPart1 = equation1Labels.get(0);
        OBLabel equationPart2 = equation1Labels.get(2);
        //
        if (value)
        {
            OBAnim path2to1 = OBAnim.pathMoveAnim(equationPart2, path1.path(), false, 0);
            animations.add(path2to1);
            //
            OBAnim path1to2 = OBAnim.pathMoveAnim(equationPart1, path2.path(), false, 0);
            animations.add(path1to2);
        }
        else
        {
            OBAnim path2to1 = OBAnim.pathMoveAnim(equationPart1, path1.path(), false, 0);
            animations.add(path2to1);
            //
            OBAnim path1to2 = OBAnim.pathMoveAnim(equationPart2, path2.path(), false, 0);
            animations.add(path1to2);
        }
        playSfxAudio("flip", false);
        flippingAnimation = OBAnimationGroup.runAnims(animations, 0.6f, true, ANIM_EASE_IN_EASE_OUT, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                hiliteEquation(equation1Labels, false, value);
                //
                if (completionBlock != null)
                {
                    completionBlock.run();

                }
            }
        }, this);
        waitForSecs(0.2f);
    }


    public void hiliteEquation(List<OBLabel> equationLabels, boolean audioPlaying, boolean identicalShowing)
    {
        lockScreen();
        int limit = equationLabels.size();
        if (identicalShowing) limit -= 2;
        for (int i = 0; i < limit; i++)
        {
            OBLabel label = equationLabels.get(i);
            label.setColour((audioPlaying) ? colourTextHilited : (identicalShowing) ? colourTextReversable : colourTextNormal);
        }
        unlockScreen();
    }


    public void hiliteIdenticalEquations(boolean value)
    {
        lockScreen();
        for (int i = 0; i < 3; i++)
        {
            OBLabel label1 = equation1Labels.get(i);
            label1.setColour((value) ? colourTextReversable : colourTextNormal);
            //
            OBLabel label2 = equation2Labels.get(i);
            label2.setColour((value) ? colourTextReversable : colourTextNormal);
        }
        unlockScreen();
    }


    public void hiliteIdenticalResult(boolean value)
    {
        lockScreen();
        OBLabel label1 = equation1Labels.get(4);
        label1.setColour((value) ? colourTextHilited : colourTextNormal);
        //
        OBLabel label2 = equation2Labels.get(4);
        label2.setColour((value) ? colourTextHilited : colourTextNormal);
        unlockScreen();
    }

    public void hideAllEquations() throws Exception
    {
        playSfxAudio("alloff", false);
        //
        lockScreen();
        for (OBLabel label : equation1Labels)
        {
            detachControl(label);
        }
        //
        for (OBLabel label : equation2Labels)
        {
            detachControl(label);
        }
        //
        for (OBLabel label : optionLabels)
        {
            detachControl(label);
        }
        //
        underline.hide();
        unlockScreen();
    }

    public void popInEquationWithBox(List<OBLabel> equationLabels) throws Exception
    {
        playSfxAudio("equationon", false);
        lockScreen();
        for (int i = 0; i < equationLabels.size() - 1; i++)
        {
            OBLabel label = (OBLabel) equationLabels.get(i);
            label.show();
        }
        //
        if (equationLabels.equals(equation1Labels)) phase1Box.show();
        else if (equationLabels.equals(equation2Labels)) phase2Box.show();
        unlockScreen();
    }

    public void popInUnderline() throws Exception
    {
        playSfxAudio("lineon", false);
        //
        lockScreen();
        underline.show();
        underline.enable();
        unlockScreen();
    }

    public void flashBox() throws Exception
    {
        OBPath box = phase1Box;
        if (isPhase(2, null)) box = phase2Box;
        for (int i = 0; i < 3; i++)
        {
            if (_aborting) return;
            //
            lockScreen();
            box.setStrokeColor(colourBoxHilited);
            unlockScreen();
            waitForSecs(0.2f);
            //
            lockScreen();
            box.setStrokeColor(colourBoxNormal);
            unlockScreen();
            waitForSecs(0.2f);
        }
    }


    public void flashUnderline() throws Exception
    {
        for (int i = 0; i < 3; i++)
        {
            if (_aborting) return;
            //
            lockScreen();
            underline.setStrokeColor(colourUnderlineHilited);
            unlockScreen();
            waitForSecs(0.2f);
            //
            lockScreen();
            underline.setStrokeColor(colourUnderlineNormal);
            unlockScreen();
            waitForSecs(0.2f);
        }
    }

    public Object findOptionLabel(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) optionLabels, pt, true);
        return c;
    }

    public Object findUnderline(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) Arrays.asList(underline), pt, true);
        return c;
    }

    public void checkOptionLabel(final OBLabel label) throws Exception
    {
        setStatus(STATUS_CHECKING);
        //
        String correctBoxUUID = "phase1_equation_total";
        if (isPhase(2, null)) correctBoxUUID = "phase2_equation_total";
        //
        OBControl correctBox = objectDict.get(correctBoxUUID);
        String correctAnswer = (String) correctBox.propertyValue("correct_number");
        String userAnswer = label.text();
        final int originalColour = (int) label.propertyValue("original_colour");
        //
        lockScreen();
        label.setColour(adjustColour(originalColour, -0.4f));
        unlockScreen();
        //
        if (correctAnswer.equals(userAnswer))
        {
            playSfxAudio("answer", false);
            //
            lockScreen();
            OBLabel totalLabel = (OBLabel) correctBox.propertyValue("label");
            correctBox.hide();
            totalLabel.show();
            unlockScreen();
            waitSFX();
            waitForSecs(0.3f);
            //
            label.setColour(originalColour);
            if (isPhase(1, null))
            {
                gotItRightBigTick(false);
            }
            else
            {
                gotItRightBigTick(true);
            }
            waitForSecs(0.3f);
            //
            if (isPhase(2, currentEvent()))
            {
                animateOptions(false);
                waitForSecs(0.3f);
            }
            //
            List finalAudio = getAudioForScene(currentEvent(), "FINAL");
            if (finalAudio != null)
            {
                waitForSecs(1.0f);
                //
                playAudioQueuedScene("FINAL", 0.3f, true);
                waitForSecs(1.5f);
            }
            else
            {
                if (flippingEquationEvents.contains(currentEvent()))
                {
                    waitForSecs(1.0f);
                    //
                    hideAllEquations();
                }
            }
            waitForSecs(0.6f);
            //
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    label.setColour(originalColour);
                }
            });
        }
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkUnderline(OBPath line) throws Exception
    {
        setStatus(STATUS_CHECKING);
        //
        lockScreen();
        underline.setStrokeColor(adjustColour(colourUnderlineNormal, -0.4f));
        unlockScreen();
        //
        if (currentEvent().equals("j"))
        {
            flipEquation(true, null);
            lockScreen();
            underline.setStrokeColor(colourUnderlineNormal);
            unlockScreen();
            //
            nextScene();

        }
        else
        {
            underline.disable();
            flipEquation(true, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    OBUtils.runOnOtherThreadDelayed(1.5f, new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            flipEquation(false, null);
                            //
                            lockScreen();
                            underline.setStrokeColor(colourUnderlineNormal);
                            unlockScreen();
                            //
                            underline.enable();
                        }
                    });
                }
            });
        }
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            updateLastActionTimeStamp();
            final Object label = findOptionLabel(pt);
            if (label != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkOptionLabel((OBLabel) label);
                    }
                });
            }
            else
            {
                final Object line = findUnderline(pt);
                if (line != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkUnderline((OBPath) line);
                        }
                    });
                }
            }
        }
    }

    public void demointro1() throws Exception
    {
        setStatus(STATUS_BUSY);
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        List audioFiles = getAudioForScene(currentEvent(), "DEMO");
        String presenterAudio = (String) audioFiles.get(0);                         // Let’s add numbers!;
        presenter.speak((List<Object>) (Object) Arrays.asList(presenterAudio), 0.3f, this);
        waitForSecs(0.7f);
        //
        presenterAudio = (String) audioFiles.get(1);                                // It’s easier if the bigger number is first.;
        presenter.speak((List<Object>) (Object) Arrays.asList(presenterAudio), 0.3f, this);
        waitForSecs(0.3f);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        OBControl front = presenter.control.objectDict.get("front");
        PointF destPos = new PointF(bounds().width() - 1.5f * front.width(), currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        presenterAudio = (String) audioFiles.get(2);                                // You’ll see!;
        presenter.speak((List<Object>) (Object) Arrays.asList(presenterAudio), 0.3f, this);
        waitForSecs(0.3f);
        //
        currPos = presenter.control.position();
        destPos = new PointF(1.25f * bounds().width() + front.width(), currPos.y);
        presenter.walk(destPos);
        //
        nextScene();
    }

    public void demointro2() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                           // Let’s add numbers!;
        OC_Generic.movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.6f);
        //
        playAudioScene("DEMO", 1, false);                                           // You’ll learn about putting the bigger one first.;
        waitAudio();
        waitForSecs(0.3f);
        //
        nextScene();
    }

    public void demoa() throws Exception
    {
        setStatus(STATUS_BUSY);
        if (thePointer == null)
        {
            loadPointer(POINTER_MIDDLE);
        }
        popInEquationWithBox(equation1Labels);
        waitForSecs(0.3f);
        //
        waitForSecs(0.7f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 0, false);                                           // What’s the answer?;
        PointF destination = phase1Box.bottomPoint();
        destination.y += phase1Box.height() * 0.2f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.6f);
        //
        movePointerToPoint(new PointF(0.95f * bounds().width(), 0.6f * bounds().height()), 0.6f, true);
        waitForSecs(0.3f);
        //
        animateOptions(true);
        waitForSecs(0.3f);
        //
        movePointerToPoint(new PointF(0.95f * bounds().width(), 0.9f * bounds().height()), 0.6f, true);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                           // It’s in here. Touch it!;
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.hidePointer(this);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void democ() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                           // Now watch!;
        OC_Generic.movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                           // two plus three equals five;
        hiliteEquation(equation1Labels, true, false);
        waitAudio();
        hiliteEquation(equation1Labels, false, false);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);                                           // three plus two equals five;
        hiliteEquation(equation2Labels, true, false);
        waitAudio();
        hiliteEquation(equation2Labels, false, false);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 3, false);                                           // These are the same.;
        OBLabel label = equation2Labels.get(1);
        PointF destination = label.bottomPoint();
        destination.y += label.height() * 1.0;
        movePointerToPoint(destination, 0.6f, true);
        hiliteIdenticalEquations(true);
        waitAudio();
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 4, false);                                           // two plus three;
        hiliteEquation(equation1Labels, true, true);
        waitAudio();
        hiliteEquation(equation1Labels, false, true);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 5, false);                                           // three plus two;
        hiliteEquation(equation2Labels, true, true);
        waitAudio();
        hiliteEquation(equation2Labels, false, true);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 6, false);                                           // They give the same answer.;
        label = equation2Labels.get(4);
        destination = label.bottomPoint();
        destination.y += label.height() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 7, false);                                           // Five.;
        hiliteIdenticalResult(true);
        waitAudio();
        hiliteIdenticalResult(false);
        waitForSecs(0.3f);
        //
        OC_Generic.hidePointer(this);
        waitForSecs(0.3f);
        //
        waitForSecs(1.5f);                                                          // RoseMarie's additional delay;
        //
        hideAllEquations();
        waitForSecs(0.3f);
        //
        nextScene();
    }

    public void demof() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                           // Watch!;
        OC_Generic.movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                           // one plus nine equals ten;
        hiliteEquation(equation1Labels, true, false);
        waitAudio();
        hiliteEquation(equation1Labels, false, false);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);                                           // nine plus one equals ten;
        hiliteEquation(equation2Labels, true, false);
        waitAudio();
        hiliteEquation(equation2Labels, false, false);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 3, false);                                           // These are the same.;
        OBLabel label = equation2Labels.get(1);
        PointF destination = label.bottomPoint();
        destination.y += label.height() * 1.0;
        movePointerToPoint(destination, 0.6f, true);
        hiliteIdenticalEquations(true);
        waitAudio();
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 4, false);                                           // one plus nine;
        hiliteEquation(equation1Labels, true, true);
        waitAudio();
        hiliteEquation(equation1Labels, false, true);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 5, false);                                           // nine plus one;
        hiliteEquation(equation2Labels, true, true);
        waitAudio();
        hiliteEquation(equation2Labels, false, true);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 6, false);                                           // They give the same answer.;
        label = equation2Labels.get(4);
        destination = label.bottomPoint();
        destination.y += label.height() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 7, false);                                           // Ten.;
        hiliteIdenticalResult(true);
        waitAudio();
        hiliteIdenticalResult(false);
        waitForSecs(0.3f);
        //
        OC_Generic.hidePointer(this);
        waitForSecs(0.3f);
        //
        waitForSecs(1.5f);                                                          // RoseMarie's additional delay;
        hideAllEquations();
        waitForSecs(0.3f);
        //
        nextScene();
    }

    public void demoi() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                           // Watch!;
        OC_Generic.movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                           // three plus twelve equals fifteen;
        hiliteEquation(equation1Labels, true, false);
        waitAudio();
        hiliteEquation(equation1Labels, false, false);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);                                           // twelve plus three equals fifteen;
        hiliteEquation(equation2Labels, true, false);
        waitAudio();
        hiliteEquation(equation2Labels, false, false);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 3, false);                                           // These are the same.;
        OBLabel label = equation2Labels.get(1);
        PointF destination = label.bottomPoint();
        destination.y += label.height() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        hiliteIdenticalEquations(true);
        waitAudio();
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 4, false);                                           // three plus twelve;
        hiliteEquation(equation1Labels, true, true);
        waitAudio();
        hiliteEquation(equation1Labels, false, true);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 5, false);                                           // twelve plus three;
        hiliteEquation(equation2Labels, true, true);
        waitAudio();
        hiliteEquation(equation2Labels, false, true);
        waitForSecs(0.3f);
        //
        waitForSecs(0.6f);                                                          // RoseMarie's additional delay;
        //
        playAudioScene("DEMO", 6, false);                                           // They give the same answer.;
        label = equation2Labels.get(4);
        destination = label.bottomPoint();
        destination.y += label.height() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 7, false);                                           // Fifteen;
        hiliteIdenticalResult(true);
        waitAudio();
        hiliteIdenticalResult(false);
        waitForSecs(0.3f);
        //
        OC_Generic.hidePointer(this);
        waitForSecs(0.3f);
        //
        waitForSecs(1.5f);                                                          // RoseMarie's additional delay;
        //
        hideAllEquations();
        waitForSecs(0.3f);
        //
        nextScene();
    }

    public void demoj() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        popInEquationWithBox(equation1Labels);
        playAudioScene("DEMO", 0, false);                                           // Now look at this one …;
        movePointerToPoint(new PointF(0.5f * bounds().width(), 0.5f * bounds().height()), 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                           // The smaller number is first!;
        OBLabel label = equation1Labels.get(0);
        PointF destination = label.bottomPoint();
        destination.y += label.height() * 0.25f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        popInUnderline();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);                                           // Touch the line!;
        destination = underline.bottomPoint();
        destination.y += bounds().height() * 0.075f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.hidePointer(this);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demok() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                           // Now the bigger number is first.;
        OBLabel label = equation1Labels.get(2);
        PointF destination = label.bottomPoint();
        destination.y += label.height() * 0.5f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                           // And adding is easier!;
        label = equation1Labels.get(1);
        destination = label.bottomPoint();
        destination.y += label.height() * 0.25f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        movePointerToPoint(new PointF(0.95f * bounds().width(), 0.6f * bounds().height()), 0.6f, false);
        flipEquation(false, null);
        waitForSecs(0.3f);
        //
        animateOptions(true);
        playAudioScene("DEMO", 2, false);                                           // Find the answer!;
        movePointerToPoint(new PointF(0.95f * bounds().width(), 0.9f * bounds().height()), 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false);                                           // You can touch the line if you wish!;
        destination = underline.bottomPoint();
        destination.y += bounds().height() * 0.05f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.hidePointer(this);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void updateLastActionTimeStamp()
    {
        lastActionTimestamp = OC_Generic.currentTime();
    }

}
