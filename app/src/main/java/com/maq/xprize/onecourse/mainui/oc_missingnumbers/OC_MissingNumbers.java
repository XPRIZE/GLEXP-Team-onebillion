package com.maq.xprize.onecourse.mainui.oc_missingnumbers;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.Layout;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Pair;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBConditionLock;
import com.maq.xprize.onecourse.utils.OBFont;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 09/11/2017.
 */

public class OC_MissingNumbers extends OC_Generic_Event
{
    private List<OBControl> numberButtons;
    private List<OBLabel> numberButtonLabels;
    private List<OBControl> numberBoxes;
    private List<OBLabel> numberBoxLabels;
    private List<OBPath> lines;
    private List<OBPath> dashes;
    private List<OBLabel> dashLabels;
    private int colourTextNormal, colourTextHilite, colourTextDisabled, colourTextDraggable;
    private int colourButtonNormal, colourButtonHilite, colourButtonDisabled;
    private int colourDashNormal, colourDashHilite;
    //
    private boolean usesBigBox;
    private boolean forcesRightAnswer;
    private boolean useRandomSeed;
    private boolean hasDemo;
    private boolean showDashes;
    //
    private Integer lowEnd;
    private Integer highEnd;
    private Integer totalQuestions;
    private Integer totalDistractors;
    private String increment;
    private String mode;
    private String difficulty;
    //
    private Integer currentQuestionCounter;
    private Integer currentShownQuestion;
    private List<OBLabel> dragLabels;
    private List<Integer> distractors;
    private List<Integer> numberSequence;
    private Integer hiddenNumberIndex;
    private Integer wrongAttempts;
    private String currentAnswer;
    private String correctAnswer;
    private long lastActionTakenTimestamp;

    private static Integer kMaxWrongAttempts = 2;
    private static double kReminderDelaySeconds = 5.0;
    private static String kModeDrag = "drag";
    private static String kModeType = "type";
    private static String kBigBox = "bigbox";
    private static String kDifficultyEasy = "easy";
    private static String kDifficultyHard = "hard";
    private static String kShowDashes = "dashes";
    private static String kTotalQuestions = "questions";
    private static String kTotalDistractors = "distractors";
    private static String kLowEnd = "min";
    private static String kHighEnd = "max";
    private static String kIncrement = "increment";
    private static String kMode = "mode";
    private static String kDifficulty = "difficulty";
    private static String kRandomSeed = "random";
    private static String kDemo = "demo";
    private static String kConstantTrue = "true";

    public void prepare ()
    {
        super.prepare();
        totalQuestions = Integer.parseInt(parameters.get(kTotalQuestions));
        totalDistractors = (parameters.get(kTotalDistractors) != null) ? Integer.parseInt(parameters.get(kTotalDistractors)) : 0;
        lowEnd = Integer.parseInt(parameters.get(kLowEnd));
        highEnd = Integer.parseInt(parameters.get(kHighEnd));
        increment = parameters.get(kIncrement);
        mode = parameters.get(kMode);
        difficulty = parameters.get(kDifficulty);
        useRandomSeed = parameters.get(kRandomSeed).equals(kConstantTrue);
        hasDemo = parameters.get(kDemo) != null && parameters.get(kDemo).equals(kConstantTrue);
        usesBigBox = parameters.get(kBigBox) != null && parameters.get(kBigBox).equals(kConstantTrue);
        showDashes = parameters.get(kShowDashes) != null && parameters.get(kShowDashes).equals(kConstantTrue);
        forcesRightAnswer = true;
        currentQuestionCounter = 1;
        events = new ArrayList<>();
        events.add("c");
        events.add("d");
        for (int i = 0; i < totalQuestions - 2; i++)
        {
            events.add("e");
        }
        events.add("f");
        if (hasDemo) events.add(0, "b");
        events.add(0, "a");
        //
        String fileForMode = (usesBigBox) ? String.format("eventlistenthen%saudio.xml", mode) : String.format("event%saudio.xml", mode);
        String filePath = getConfigPath(fileForMode);
        loadAudioXML(filePath);
        colourTextNormal = (objectDict.get("colour_text_normal")).fillColor();
        colourTextHilite = (objectDict.get("colour_text_hilite")).fillColor();
        colourTextDisabled = (objectDict.get("colour_text_disabled")).fillColor();
        colourTextDraggable = (objectDict.get("colour_text_draggable")).fillColor();
        colourButtonNormal = (objectDict.get("colour_number_button_normal")).fillColor();
        colourButtonHilite = (objectDict.get("colour_number_button_hilite")).fillColor();
        colourButtonDisabled = (objectDict.get("colour_number_button_disabled")).fillColor();
        colourDashNormal = (objectDict.get("colour_dash_normal")).fillColor();
        colourDashHilite = (objectDict.get("colour_dash_hilite")).fillColor();
        hideControls("colour.*");
        //
        if (mode.equals(kModeType))
        {
            hideControls("drag_bar");
            if (numberButtons == null || numberButtonLabels == null)
            {
                numberButtons = new ArrayList<>();
                numberButtonLabels = new ArrayList<>();
                OBControl buttonTemplate = objectDict.get("number_button");
                float allocatedWidth = 0.9f * bounds().width();
                float slottedWidth = allocatedWidth / 10;
                float startingX = (bounds().width() - allocatedWidth + slottedWidth - buttonTemplate.width()) / 2;
                float y = 0.9f * bounds().height();
                for (int i = 0; i < 10; i++)
                {
                    OBPath newButton = (OBPath) buttonTemplate.copy();
                    newButton.sizeToBoundingBoxIncludingStroke();
                    newButton.setPosition(new PointF(startingX + i * slottedWidth + newButton.width() / 2f, y));
                    newButton.setProperty("original_position", newButton.getWorldPosition());
                    OBLabel newButtonLabel = action_createLabelForControl(newButton, String.format("%d", i), colourTextNormal, 1.2f);
                    newButton.setProperty("label", newButtonLabel);
                    numberButtons.add(newButton);
                    numberButtonLabels.add(newButtonLabel);
                }
                buttonTemplate.hide();
            }
            for (OBControl control : numberButtons) attachControl(control);
            for (OBControl control : numberButtonLabels) attachControl(control);
        }
        else
        {
            hideControls("number_button");
            if (numberButtons != null)
            {
                for (OBControl control : numberButtons)
                {
                    detachControl(control);
                }
            }
            if (numberButtonLabels != null)
            {
                for (OBControl control : numberButtonLabels)
                {
                    detachControl(control);
                }
            }
        }
        if (numberBoxes == null && numberBoxLabels == null && lines == null)
        {
            numberBoxes = new ArrayList<>();
            numberBoxLabels = new ArrayList<>();
            lines = new ArrayList<>();
            //
            if (usesBigBox)
            {
                hideControls("line_.*");
                hideControls("number_box");
                int number = (numberSequence == null) ? 0 : numberSequence.get(0);
                String number_string = String.format("%d", number);
                OBControl bigBox = objectDict.get("number_box_big");
                bigBox.setPosition(new PointF(0.5f * bounds().width(), 0.4f * bounds().height()));
                OBLabel bigBoxLabel = action_createLabelForControl(bigBox, number_string, colourTextNormal, 0.8f);
                bigBox.setProperty("label", bigBoxLabel);
                numberBoxes.add(bigBox);
                numberBoxLabels.add(bigBoxLabel);
            }
            else
            {
                hideControls(".*_big");
                OBControl boxTemplate = objectDict.get("number_box");
                OBPath verticalLineTemplate = (OBPath) objectDict.get("line_vertical");
                float allocatedWidth = 0.9f * bounds().width();
                float slottedWidth = allocatedWidth / 4;
                float startingX = (bounds().width() - allocatedWidth + slottedWidth - boxTemplate.width()) / 2;
                float y = 0.5f * bounds().height();
                //
                for (int i = 0; i < 4; i++)
                {
                    OBControl newBox = boxTemplate.copy();
                    newBox.setPosition(new PointF(startingX + i * slottedWidth + newBox.width() / 2f, y));
                    //
                    int number = (numberSequence == null) ? 0 : numberSequence.get(i);
                    String number_string = String.format("%d", number);
                    OBLabel newBoxLabel = action_createLabelForControl(newBox, number_string, colourTextNormal, 0.8f);
                    newBox.setProperty("label", newBoxLabel);
                    OBPath newVerticalLine = (OBPath) verticalLineTemplate.copy();
                    newVerticalLine.sizeToBoundingBoxIncludingStroke();
                    newVerticalLine.setPosition(new PointF(newBoxLabel.position().x, newBoxLabel.position().y - newBoxLabel.height() / 2f - newVerticalLine.height() / 2f));
                    newVerticalLine.setZPosition(newBox.zPosition() - 0.001f);
                    numberBoxes.add(newBox);
                    numberBoxLabels.add(newBoxLabel);
                    lines.add(newVerticalLine);
                }
                boxTemplate.hide();
                verticalLineTemplate.hide();
                OBPath horizontalLine = (OBPath) objectDict.get("line_horizontal");
                PointF firstPoint = OC_Generic.copyPoint(OB_Maths.AddPoints(lines.get(0).position(), lines.get(0).firstPoint()));
                PointF lastPoint = OC_Generic.copyPoint(OB_Maths.AddPoints(lines.get(lines.size() - 1).position(), lines.get(lines.size()  -1).firstPoint()));
                lastPoint = OB_Maths.DiffPoints(lastPoint, firstPoint);
                lastPoint.set(bounds().width(), lastPoint.y);
                firstPoint = new PointF(0,0);
                Path newPath = new Path();
                newPath.moveTo(firstPoint.x, firstPoint.y);
                newPath.lineTo(lastPoint.x, lastPoint.y);
                horizontalLine.setPath(newPath);
                horizontalLine.sizeToBoundingBoxIncludingStroke();
                horizontalLine.setLeft(0);
                horizontalLine.setTop(lines.get(0).position().y - lines.get(0).height() / 2);
                attachControl(horizontalLine);
            }
        }
        if (numberBoxes != null) for (OBControl control : numberBoxes) attachControl(control);
        if (numberBoxLabels != null) for (OBControl control : numberBoxLabels) attachControl(control);
        if (lines != null) for (OBControl control : lines) attachControl(control);
        if (numberBoxLabels != null) for (OBControl control : numberBoxLabels) control.hide();
        if (dashes != null) for (OBControl control : dashes) control.hide();
        hideControls("number_dash.*");
    }

    public void doMainXX () throws Exception
    {
        boolean needsNewScene = currentShownQuestion != currentQuestionCounter;
        lockScreen();
        hideControls("number_dash.*");
        if (needsNewScene)
        {
            resetScene();
        }
        unlockScreen();
        if (needsNewScene)
        {
            introScene(false);
        }
        try
        {
            doAudio(currentEvent());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // do nothing
        }
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void doAudio (String scene, boolean sayNumber) throws Exception
    {
        List audio = getAudioForScene(scene, "PROMPT");
        List replayAudio = new ArrayList();
        replayAudio.addAll(getAudioForScene(scene, "REPEAT"));
        //
        final String correctAudio = String.format("n_%s", correctAnswer);
        if (usesBigBox)
        {
            replayAudio.add(correctAudio);
        }
        setReplayAudio(replayAudio);
        //
        OBConditionLock lock = playAudioQueued(audio, false);
        if (usesBigBox)
        {
            if (audio != null) waitAudioQueue(lock);
            waitForSecs(0.3f);
            if (sayNumber)
            {
                if (mode.equals(kModeType) && showDashes)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            playAudio(correctAudio);
                            waitAudio();
                            //
                            playSfxAudio("dash_hilite", false);
                            //
                            lockScreen();
                            refreshDashes();
                            unlockScreen();
                        }
                    });
                }
                else
                {
                    waitForSecs(0.3f);
                    playAudio(correctAudio);
                }
            }
        }
    }



    public void doAudio (String scene)
    {
        try
        {
            doAudio(scene, true);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_MissingNumbers:doAudio:Exception caught");
            e.printStackTrace();
        }
    }




    public List breakdownLabel (OBLabel label)
    {
        List result = new ArrayList<>();
        Typeface font = label.typeface();
        float fontSize = label.fontSize();
        //
        for (int i = 0; i < label.text().length(); i++)
        {
            String digitString = String.format("%c", label.text().charAt(i));
            OBLabel newLabel = new OBLabel(digitString, font, fontSize);
            newLabel.setZPosition(label.zPosition() + 0.1f);
            newLabel.setColour(colourTextNormal);
            if (i == 0)
            {
                newLabel.setProperty("original_left", label.left());
                newLabel.setLeft(label.left());
            }
            else
            {
                OBLabel auxLabel = new OBLabel(label.text().substring(0, i + 1), font, fontSize);
                auxLabel.setZPosition(label.zPosition() + 0.1f);
                auxLabel.setLeft(label.left());
                newLabel.setProperty("original_right", auxLabel.right());
                newLabel.setRight(auxLabel.right());
            }
            newLabel.setTop(label.top());
            newLabel.setProperty("original_position", OC_Generic.copyPoint(newLabel.position()));
            result.add(newLabel);
        }
        return result;
    }




    public Pair<List<Integer>, List<Integer>> generateNumbersFrom (Integer min, Integer max, String incrementKey, int totalNumbers, boolean randomSeed)
    {
        List<Integer> randomDistractors = new ArrayList<>();
        List<Integer> result = new ArrayList<>();
        List<Integer> allowedValues = new ArrayList<>();
        Integer currentValue = min;
        Integer incrementJump = (incrementKey.equals("even") || incrementKey.equals("odd")) ? 2 : Integer.parseInt(incrementKey);
        //
        if (incrementJump < 0)
        {
            if (randomSeed)
            {
                currentValue = randomNumberBetween(max + incrementJump, max);
            }
            if (incrementKey.equals("even"))
            {
                if (currentValue % 2 == 0) currentValue--;
            }
            else if (incrementKey.equals("odd"))
            {
                if (currentValue % 2 != 0) currentValue--;
            }
            //
            currentValue = Math.max(min, max);
            //
            while (currentValue >= min)
            {
                allowedValues.add(currentValue);
                currentValue += incrementJump;
            }
        }
        else
        {
            if (randomSeed)
            {
                currentValue = randomNumberBetween(min, min + incrementJump);
            }
            if (incrementKey.equals("even"))
            {
                if (currentValue % 2 == 0) currentValue++;
            }
            else if (incrementKey.equals("odd"))
            {
                if (currentValue % 2 != 0) currentValue++;
            }
            while (currentValue <= max)
            {
                allowedValues.add(currentValue);
                currentValue += incrementJump;
            }
        }
        Integer index = randomNumberBetween(0, allowedValues.size() - totalNumbers);
        for (int i = 0; i < totalNumbers; i++)
        {
            result.add(allowedValues.get(index + i));
        }
        while (randomDistractors.size() < totalDistractors)
        {
            Integer randomIndex = randomNumberBetween(0, allowedValues.size() - 1);
            Integer number = allowedValues.get(randomIndex);
            if (result.contains(number)) continue;
            if (randomDistractors.contains(number)) continue;
            randomDistractors.add(number);
        }
        return new Pair(result, randomDistractors);
    }



    public void refreshDashes ()
    {
        if (showDashes)
        {
            int shownDashes = Math.max(1, Math.min(currentAnswer.length() + 1, numberSequence.get(hiddenNumberIndex).toString().length()));
            for (OBPath dash : dashes)
            {
                dash.setStrokeColor(colourDashNormal);
            }
            OBPath dash = dashes.get(shownDashes - 1);
            dash.setStrokeColor(colourDashHilite);
        }
    }


    public void resetScene ()
    {
        int quantity = (usesBigBox) ? 1 : 4;
        Pair result = generateNumbersFrom(lowEnd, highEnd, increment, quantity, useRandomSeed);
        //
        numberSequence = (List<Integer>) result.first;
        distractors = (List<Integer>) result.second;
        //
        hiddenNumberIndex = (usesBigBox) ? 0 : randomNumberBetween(0, 3);
        correctAnswer = numberSequence.get(hiddenNumberIndex).toString();
        currentAnswer = "";
        wrongAttempts = 0;
        //
        for (int i = 0; i < numberBoxLabels.size(); i++)
        {
            Integer number = numberSequence.get(i);
            String number_string = String.format("%d", number);
            OBLabel label = numberBoxLabels.get(i);
            label.setString(number_string);
            PointF position = OC_Generic.copyPoint(label.position());
            OBControl box = numberBoxes.get(i);
            label.sizeToBoundingBox();
            label.setPosition(position);
        }
        OBControl emptyNumberBox = numberBoxes.get(hiddenNumberIndex);
        OBLabel emptyNumberBoxLabel = (OBLabel) emptyNumberBox.propertyValue("label");
        if (mode.equals(kModeType))
        {
            int totalDashes = numberSequence.get(hiddenNumberIndex).toString().length();
            for (OBControl control : numberButtons)
            {
                toggleNumberButton(control, false);
            }
            if (dashes != null)
            {
                for (OBControl control : dashes) detachControl(control);
            }
            if (dashLabels != null)
            {
                for (OBControl control : dashLabels) detachControl(control);
            }
            dashes = new ArrayList<>();
            dashLabels = new ArrayList<>();
            OBPath dashTemplate = (usesBigBox) ? (OBPath ) objectDict.get("number_dash_big") :  (OBPath ) objectDict.get("number_dash");
            dashTemplate.sizeToBoundingBoxIncludingStroke();
            //
            float allocatedFactor = Math.max(0.5f, Math.min(0.9f, 0.25f * totalDashes));
            float allocatedWidth = allocatedFactor * emptyNumberBox.width();
            float slottedWidth = allocatedWidth / totalDashes;
            float startingX = (emptyNumberBox.width() - allocatedWidth + slottedWidth - dashTemplate.width()) / 2;
            float y = 0.85f * emptyNumberBox.height();
            dashLabels = breakdownLabel(emptyNumberBoxLabel);
            for (int i = 0; i < totalDashes; i++)
            {
                OBPath newDash = (OBPath) dashTemplate.copy();
                newDash.setPosition(OB_Maths.AddPoints(new PointF(emptyNumberBox.frame.left, emptyNumberBox.frame.top), new PointF(startingX + (i * slottedWidth) + (newDash.width() / 2), y)));
                newDash.setStrokeColor(colourDashNormal);
                dashes.add(newDash);
                OBLabel newDashLabel = dashLabels.get(i);
                newDashLabel.setString("");
                newDashLabel.setPosition(new PointF(newDash.position().x, newDashLabel.position().y));
                newDashLabel.setTop(emptyNumberBoxLabel.top());
            }
            dashTemplate.hide();
            for (OBControl control : dashes)
            {
                attachControl(control);
                control.show();
            }
            for (OBControl control : dashLabels)
            {
                attachControl(control);
                control.show();
            }
        }
        else if (mode.equals(kModeDrag))
        {
            if (dragLabels != null)
            {
                for (OBControl control : dragLabels)
                {
                    detachControl(control);
                }
            }
            dragLabels = new ArrayList<>();
            OBControl boxTemplate = objectDict.get("number_box");
            OBLabel labelTemplate = action_createLabelForControl(boxTemplate, correctAnswer, colourTextNormal, 0.8f);
            //
            for (Integer distractor : distractors)
            {
                OBLabel distractorLabel = (OBLabel) labelTemplate.copy();
                distractorLabel.setString(distractor.toString());
                distractorLabel.setColour(colourTextDraggable);
                distractorLabel.sizeToBoundingBox();
                dragLabels.add(distractorLabel);
            }
            labelTemplate.hide();
            //
            OBLabel distractorLabel = (OBLabel) labelTemplate.copy();
            distractorLabel.setString(emptyNumberBoxLabel.text());
            distractorLabel.setColour(colourTextDraggable);
            distractorLabel.sizeToBoundingBox();
            Integer randomIndex = randomNumberBetween(0, dragLabels.size());
            dragLabels.add(randomIndex, distractorLabel);
            float allocatedWidth = 0.9f * bounds().width();
            float slottedWidth = allocatedWidth / dragLabels.size();
            float startingX = (bounds().width() - allocatedWidth + slottedWidth - distractorLabel.width()) / 2;
            float y = 0.9f * bounds().height();
            for (int i = 0; i < dragLabels.size(); i++)
            {
                OBLabel label = dragLabels.get(i);
                label.setPosition(new PointF(startingX + i * slottedWidth + distractorLabel.width() / 2f, y));
                label.setProperty("original_position", label.getWorldPosition());
                label.setZPosition(objectDict.get("drag_bar").zPosition() + 0.1f);
                label.hide();
                attachControl(label);
            }
        }
        emptyNumberBoxLabel.setString("");
        for (OBControl control : numberBoxLabels) control.hide();
        currentShownQuestion = currentQuestionCounter;
    }

    public void introScene (final Boolean playDashAudio) throws Exception
    {
        if (usesBigBox)
        {
            lockScreen();
            for (OBControl control : numberBoxLabels) control.show();
            unlockScreen();
        }
        else
        {
            playSfxAudio("number_box_appear", false);
            lockScreen();
            for (OBControl control : numberBoxLabels) control.show();
            unlockScreen();
        }
        waitForSecs(0.3f);
        //
        if (mode.equals(kModeDrag))
        {
            List<OBAnim> animations = new ArrayList<>();
            lockScreen();
            for (OBControl control : dragLabels)
            {
                PointF destination = OC_Generic.copyPoint(control.position());
                control.show();
                control.setLeft(control.left() + bounds().width());
                OBAnim moveAnim = OBAnim.moveAnim(destination, control);
                animations.add(moveAnim);
            }
            unlockScreen();
            //
            playSfxAudio("number_appear", false);
            for (OBAnim animation : animations)
            {
                OBAnimationGroup.runAnims(Arrays.asList(animation), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                waitForSecs(randomNumberBetween(5, 10) * 0.01);
            }
            waitForSecs(0.3f);
        }
        else if (mode.equals(kModeType))
        {
            OBUtils.runOnOtherThreadDelayed(0.5f, new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    if (playDashAudio)
                    {
                        playSfxAudio("dash_hilite", false);
                    }
                    lockScreen();
                    refreshDashes();
                    unlockScreen();
                }
            });
        }
        lastActionTakenTimestamp = new Date().getTime();
        OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                doReminder(currentQuestionCounter);
            }
        });
    }

    public void doReminder (final Integer currentQuestion) throws Exception
    {
        if (currentQuestion != currentQuestionCounter) return;
        if (lastActionTakenTimestamp == -1) return;
        long timestamp = lastActionTakenTimestamp;
        double elapsed = new Date().getTime() - lastActionTakenTimestamp;
        //
        if (status() == STATUS_AWAITING_CLICK && elapsed > kReminderDelaySeconds)
        {
            if (mode.equals(kModeDrag))
            {
                OBPath emptyNumberBox = (OBPath) numberBoxes.get(hiddenNumberIndex);
                int originalColour = emptyNumberBox.strokeColor();
                float originalLineWidth = emptyNumberBox.lineWidth();
                for (int i = 0; i < 3; i++)
                {
                    lockScreen();
                    emptyNumberBox.setStrokeColor(Color.YELLOW);
                    emptyNumberBox.setLineWidth(originalLineWidth * 4);
                    unlockScreen();
                    waitForSecs(0.3f);
                    //
                    if (timestamp != lastActionTakenTimestamp)
                    {
                        break;
                    }
                    lockScreen();
                    emptyNumberBox.setStrokeColor(originalColour);
                    emptyNumberBox.setLineWidth(originalLineWidth);
                    unlockScreen();
                    waitForSecs(0.3f);
                    //
                    if (timestamp != lastActionTakenTimestamp)
                    {
                        break;
                    }
                }
                lockScreen();
                emptyNumberBox.setStrokeColor(originalColour);
                emptyNumberBox.setLineWidth(originalLineWidth);
                unlockScreen();
            }
            else if (mode.equals(kModeType))
            {
                int shownDashes = Math.max(1, Math.min(currentAnswer.length() + 1, numberSequence.get(hiddenNumberIndex).toString().length()));
                OBPath currentDash = dashes.get(shownDashes - 1);
                for (int i = 0; i < 3; i++)
                {
                    lockScreen();
                    for (OBPath dash : dashes)
                    {
                        dash.setStrokeColor(colourDashNormal);
                    }
                    unlockScreen();
                    waitForSecs(0.3f);
                    //
                    if (timestamp != lastActionTakenTimestamp)
                    {
                        break;
                    }
                    lockScreen();
                    for (OBPath dash : dashes)
                    {
                        if (dash.equals(currentDash))
                        {
                            dash.setStrokeColor(colourDashHilite);
                        }
                        else
                        {
                            dash.setStrokeColor(colourDashNormal);
                        }
                    }
                    unlockScreen();
                    waitForSecs(0.3f);
                    //
                    if (timestamp != lastActionTakenTimestamp)
                    {
                        break;
                    }
                }
                lockScreen();
                refreshDashes();
                unlockScreen();
            }
            lastActionTakenTimestamp = new Date().getTime();
        }
        //
        if (this.aborting()) return;
        //
        OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                doReminder(currentQuestion);
            }
        });
    }


    public void hideDashesJoinDigitsAndPlaySequence () throws Exception
    {
        lastActionTakenTimestamp = -1;
        //
        OBControl emptyNumberBox = numberBoxes.get(hiddenNumberIndex);
        OBLabel emptyNumberBoxLabel = (OBLabel) emptyNumberBox.propertyValue("label");
        if (mode.equals(kModeType))
        {
            lockScreen();
            for (OBControl control : dashes) control.hide();
            unlockScreen();
            //
            List<OBAnim> animations = new ArrayList<>();
            for (OBControl control : dashLabels)
            {
                PointF originalPosition = OC_Generic.copyPoint((PointF) control.propertyValue("original_position"));
                OBAnim animation = OBAnim.moveAnim(originalPosition, control);
                animations.add(animation);
            }
            OBAnimationGroup.runAnims(animations, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.3f);
            //
            lockScreen();
            for (OBControl control : dashLabels) control.hide();
            emptyNumberBoxLabel.setString(correctAnswer);
            unlockScreen();
        }
        //
        else if (mode.equals(kModeDrag))
        {
            if (usesBigBox)
            {
                OBLabel draggedLabel = null;
                for (OBLabel control : dragLabels)
                {
                    if (!control.isEnabled())
                    {
                        draggedLabel = control;
                        break;

                    }

                }
                final OBLabel draggedLabel_final = draggedLabel;
                final float finalPointSize = emptyNumberBoxLabel.fontSize();
                final float initialPointSize = draggedLabel.fontSize();
                OBAnim fontAnim = new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock (float frac)
                    {
                        float newPointSize = initialPointSize + (finalPointSize - initialPointSize) * frac;
                        PointF position = OC_Generic.copyPoint(draggedLabel_final.position());
                        draggedLabel_final.setFontSize(newPointSize);
                        draggedLabel_final.sizeToBoundingBox();
                        draggedLabel_final.setPosition(position);
                    }
                };
                OBAnimationGroup.runAnims(Arrays.asList(fontAnim), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            }
            lockScreen();
            for (OBControl control : dragLabels)
            {
                if (!control.isEnabled())
                {
                    control.hide();

                }

            }
            emptyNumberBoxLabel.setString(correctAnswer);
            unlockScreen();
        }
        //
        for (OBLabel label : numberBoxLabels)
        {
            playAudio(String.format("n_%s", label.text()));
            label.setColour(colourTextHilite);
            waitAudio();
            label.setColour(colourTextNormal);
            waitForSecs(0.3f);
        }
    }


    public void endScene () throws Exception
    {
        playSfxAudio("number_box_hide", false);
        //
        lockScreen();
        if (numberBoxLabels != null) for (OBControl control : numberBoxLabels) control.hide();
        if (dashLabels != null) for (OBControl control : dashLabels) control.hide();
        if (dragLabels != null)
        {
            for (OBControl control : dragLabels)
            {
                if (!control.isEnabled())
                {
                    control.hide();
                }
            }
        }
        unlockScreen();
        waitForSecs(0.3f);
        //
        if (mode.equals(kModeDrag))
        {
            List<OBAnim> animations = new ArrayList<>();
            for (OBControl control : dragLabels)
            {
                if (control.isEnabled())
                {
                    OBAnim moveAnim = OBAnim.moveAnim(new PointF(control.position().x - bounds().width(), control.position().y), control);
                    animations.add(moveAnim);
                }
            }
            playSfxAudio("number_appear", false);
            for (OBAnim animation : animations)
            {
                OBAnimationGroup.runAnims(Arrays.asList(animation), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                waitForSecs(randomNumberBetween(5, 10) * 0.01);
            }
            waitForSecs(0.3f);
        }
    }


    public void toggleNumberButton (OBControl button, boolean value)
    {
        int backColour = (button.isEnabled()) ? (value) ? colourButtonHilite : colourButtonNormal : colourButtonDisabled;
        int textColour = (button.isEnabled()) ? (value) ? colourTextHilite : colourTextNormal : colourTextDisabled;
        PointF buttonPosition = OC_Generic.copyPoint((PointF) button.propertyValue("original_position"));
        if (value) buttonPosition.y += button.height() * 0.1;
        OBLabel label = (OBLabel) button.propertyValue("label");
        button.setFillColor(backColour);
        button.setPosition(buttonPosition);
        label.setColour(textColour);
        label.setPosition(buttonPosition);
    }


    public void checkNumberButton (final OBControl target) throws Exception
    {
        setStatus(STATUS_CHECKING);
        playSfxAudio("type_digit", false);
        //
        lockScreen();
        for (OBControl button : numberButtons)
        {
            toggleNumberButton(button, button.equals(target));
        }
        for (OBPath dash : dashes)
        {
            dash.setStrokeColor(colourDashNormal);
        }
        unlockScreen();
        //
        OBLabel numberButtonLabel = (OBLabel) target.propertyValue("label");
        String newAnswer = String.format("%s%s", currentAnswer, numberButtonLabel.text());
        if (newAnswer.length() <= dashes.size())
        {
            int charIndex = newAnswer.length() - 1;
            char correctChar = numberSequence.get(hiddenNumberIndex).toString().charAt(charIndex);
            if (numberButtonLabel.text().charAt(0) != correctChar)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        gotItWrongWithSfx();
                        waitSFX();
                        waitForSecs(0.3f);
                        //
                        playAudioQueuedScene("INCORRECT", 0.3f, false);
                    }
                });
                //
                wrongAttempts += 1;
                if (wrongAttempts >= kMaxWrongAttempts)
                {
                    for (OBControl button : numberButtons)
                    {
                        OBLabel label = (OBLabel) button.propertyValue("label");
                        if (!label.text().equals(String.format("%c", correctChar)))
                        {
                            label.setColour(colourTextDisabled);
                            button.setFillColor(colourButtonDisabled);
                            button.disable();
                        }
                    }
                }
            }
            else
            {
                currentAnswer = newAnswer;
                wrongAttempts = 0;
                //
                lockScreen();
                for (OBControl button : numberButtons)
                {
                    OBLabel label = (OBLabel) button.propertyValue("label");
                    label.setColour(colourTextNormal);
                    button.setFillColor(colourButtonNormal);
                    button.enable();
                }
                unlockScreen();
            }
        }
        //
        lockScreen();
        for (int i = 0; i < currentAnswer.length(); i++)
        {
            OBLabel label = dashLabels.get(i);
            label.setString(String.format("%c", currentAnswer.charAt(i)));
            label.sizeToBoundingBox();
            attachControl(label);
        }
        refreshDashes();
        unlockScreen();
        //
        if (currentAnswer.equals(numberSequence.get(hiddenNumberIndex).toString()))
        {
            OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    try
                    {
                        hideDashesJoinDigitsAndPlaySequence();
                        gotItRightBigTick(true);
                        //
                        OBUtils.runOnOtherThreadDelayed(1.5f, new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                try
                                {
                                    currentQuestionCounter++;
                                    if (currentQuestionCounter >= totalQuestions)
                                    {
                                        nextScene();
                                    }
                                    else
                                    {
                                        endScene();
                                        waitForSecs(0.3f);
                                        nextScene();
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                    // do nothing
                                }
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        // do nothing
                    }
                }
            });
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
        //
        OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                try
                {
                    lockScreen();
                    toggleNumberButton(target, false);
                    unlockScreen();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }


    public Object findNumberButton (PointF pt)
    {
        return finger(0, 2, numberButtons, pt, true);
    }


    public Object findDraggableNumber (PointF pt)
    {
        return finger(0, 2, (List<OBControl>) (Object) dragLabels, pt, true);
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            lastActionTakenTimestamp = new Date().getTime();
            //
            if (mode.equals(kModeType))
            {
                final OBControl obj = (OBControl) findNumberButton(pt);
                if (obj != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            try
                            {
                                checkNumberButton(obj);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
            else if (mode.equals(kModeDrag))
            {
                final OBControl obj = (OBControl) findDraggableNumber(pt);
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
    }


    public void checkDragTarget (OBControl targ, PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target = targ;
        OC_Generic.sendObjectToTop(targ, this);
        targ.animationKey = SystemClock.uptimeMillis();
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }


    public void checkDragAtPoint (PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBLabel targetLabel = (OBLabel) target;
            OBControl emptyNumberBox = numberBoxes.get(hiddenNumberIndex);
            OBLabel emptyNumberBoxLabel = (OBLabel) emptyNumberBox.propertyValue("label");
            float overlap = OBUtils.RectOverlapRatio(target.frame, emptyNumberBox.frame);
            if (overlap > 0.3)
            {
                if (targetLabel.text().equals(correctAnswer))
                {
                    playSfxAudio("number_drop", false);
                    targetLabel.disable();
                    targetLabel.setColour(colourTextNormal);
                    OBAnim moveAnim = OBAnim.moveAnim(emptyNumberBoxLabel.position(), target);
                    OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            try
                            {
                                hideDashesJoinDigitsAndPlaySequence();
                                gotItRightBigTick(true);
                                OBUtils.runOnOtherThreadDelayed(0.7f, new OBUtils.RunLambda()
                                {
                                    public void run () throws Exception
                                    {
                                        try
                                        {
                                            currentQuestionCounter++;
                                            if (currentQuestionCounter >= totalQuestions)
                                            {
                                                nextScene();
                                            }
                                            else
                                            {
                                                endScene();
                                                waitForSecs(0.3f);
                                                nextScene();
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            e.printStackTrace();
                                            // do nothing
                                        }
                                    }
                                });
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                // do nothing
                            }
                        }
                    });
                }
                else
                {
                    gotItWrongWithSfx();
                    PointF destination = OC_Generic.copyPoint((PointF) target.propertyValue("original_position"));
                    OBAnim moveAnim = OBAnim.moveAnim(destination, target);
                    OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
            else
            {
                PointF destination = OC_Generic.copyPoint((PointF) target.propertyValue("original_position"));
                OBAnim moveAnim = OBAnim.moveAnim(destination, target);
                OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // do nothing
        }
    }


    public void touchUpAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            lastActionTakenTimestamp = new Date().getTime();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }


    private void demo_dragNumberToBoxWithAudio (List audio, boolean sayNumberWhenDone) throws Exception
    {
        OBLabel correctLabel = null;
        for (OBLabel label : dragLabels)
        {
            if (label.text().equals(correctAnswer))
            {
                correctLabel = label;
                break;
            }
        }
        //
        OBControl correctBox = numberBoxes.get(hiddenNumberIndex);
        OC_Generic.pointer_moveToObject(correctLabel, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        OC_Generic.pointer_moveToPointWithObject(correctLabel, correctBox.position(), 0, 0.6f, true, this);
        playSfxAudio("number_drop", false);
        OBLabel correctBoxLabel = (OBLabel) correctBox.propertyValue("label");
        final float finalPointSize = correctBoxLabel.fontSize();
        final float initialPointSize = correctLabel.fontSize();
        final OBLabel correctLabel_final = correctLabel;
        //
        if (usesBigBox)
        {
            OBAnim fontAnim = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock (float frac)

                {
                    float newPointSize = initialPointSize + (finalPointSize - initialPointSize) * frac;
                    PointF position = OC_Generic.copyPoint(correctLabel_final.position());
                    correctLabel_final.setFontSize(newPointSize);
                    correctLabel_final.sizeToBoundingBox();
                    correctLabel_final.setPosition(position);
                }
            };
            OBAnimationGroup.runAnims(Arrays.asList(fontAnim), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            correctLabel.setColour(colourTextNormal);
        }
        PointF pointerNudge = OC_Generic.copyPoint(OB_Maths.AddPoints(correctBox.position(), new PointF(0f, correctBox.height() / 2f)));
        movePointerToPoint(pointerNudge, 0, 0.3f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        if (sayNumberWhenDone)
        {
            correctLabel.setColour(colourTextHilite);
            playAudio(String.format("n_%s", correctAnswer));
            waitAudio();
            //
            correctLabel.setColour(colourTextNormal);
            waitForSecs(0.3f);

        }
        playAudioQueued(audio);
        moveScenePointer(new PointF(0.7f * bounds().width(), 0.7f * bounds().height()), 0.6f, null, 0.3f);
        PointF destination = OC_Generic.copyPoint((PointF) correctLabel.propertyValue("original_position"));
        //
        if (usesBigBox)
        {
            lockScreen();
            PointF position = OC_Generic.copyPoint(correctLabel.position());
            correctLabel.setFontSize(initialPointSize);
            correctLabel.sizeToBoundingBox();
            correctLabel.setPosition(position);
            correctLabel.setColour(colourTextDraggable);
            unlockScreen();
        }
        OBAnim moveAnim = OBAnim.moveAnim(destination, correctLabel);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    private void demo_typeInNumberWithAudio (List audio, boolean sayNumberWhenDone) throws Exception
    {
        waitAudio();
        waitForSecs(0.3f);
        //
        playSfxAudio("dash_hilite", false);
        //
        lockScreen();
        refreshDashes();
        unlockScreen();
        waitForSecs(0.3f);
        //
        for (int i = 0; i < correctAnswer.length(); i++)
        {
            String digit = String.format("%c", correctAnswer.charAt(i));
            for (OBControl button : numberButtons)
            {
                OBLabel label = (OBLabel) button.propertyValue("label");
                if (label.text().equals(digit))
                {
                    OC_Generic.pointer_moveToObject(button, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
                    playSfxAudio("type_digit", false);
                    lockScreen();
                    toggleNumberButton(button, true);
                    OBLabel dashLabel = dashLabels.get(i);
                    dashLabel.setString(digit);
                    int dashIndex = Math.min(correctAnswer.length() - 1, i + 1);
                    OBControl hilitedDash = dashes.get(dashIndex);
                    for (OBPath dash : dashes)
                    {
                        dash.setStrokeColor((dash.equals(hilitedDash)) ? colourDashHilite : colourDashNormal);
                    }
                    unlockScreen();
                    waitForSecs(0.3f);
                    //
                    lockScreen();
                    toggleNumberButton(button, false);
                    unlockScreen();
                }
            }
        }
        //
        lockScreen();
        for (OBControl control : dashes)
        {
            control.hide();
        }
        unlockScreen();
        //
        OBControl emptyNumberBox = numberBoxes.get(hiddenNumberIndex);
        OBLabel emptyNumberBoxLabel = (OBLabel) emptyNumberBox.propertyValue("label");
        List animations = new ArrayList<>();
        for (OBControl control : dashLabels)
        {
            control.setProperty("previous_position", control.getWorldPosition());
            PointF originalPosition = OC_Generic.copyPoint((PointF) control.propertyValue("original_position"));
            OBAnim animation = OBAnim.moveAnim(originalPosition, control);
            animations.add(animation);
        }
        OBAnimationGroup.runAnims(animations, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.3f);
        lockScreen();
        for (OBControl control : dashLabels) control.hide();
        emptyNumberBoxLabel.setString(correctAnswer);

        unlockScreen();
        if (sayNumberWhenDone)
        {
            playAudio(String.format("n_%s", correctAnswer));
            emptyNumberBoxLabel.setColour(colourTextHilite);
            waitAudio();
            emptyNumberBoxLabel.setColour(colourTextNormal);
            waitForSecs(0.3f);

        }
        waitAudio();
        waitForSecs(0.7f);
        playAudioQueued(audio);

    }


    public void demoa () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioQueuedScene("DEMO", 0.3f, false);
        moveScenePointer(new PointF(0.9f * bounds().width(), 0.7f * bounds().height()), 0.6f, null, 0.0f);
        waitAudio();
        waitForSecs(0.3f);
        //
        nextScene();
    }

    public void demob () throws Exception
    {
        setStatus(STATUS_BUSY);
        lockScreen();
        resetScene();
        unlockScreen();
        //
        introScene(false);
        OBPath box = (OBPath) numberBoxes.get(hiddenNumberIndex);
        if (usesBigBox)
        {
            playAudioScene("DEMO", 0, false); // Listen
            moveScenePointer(new PointF(0.9f * bounds().width(), 0.6f * bounds().height()), 0.6f, null, 0.3f);
            waitAudio();
            waitForSecs(0.3f);
            //
            playAudio(String.format("n_%s", correctAnswer));
            waitAudio();
            waitForSecs(0.3f);
            //
            playAudioScene("DEMO", 1, false); // Now watch me.;
            if (mode.equals(kModeDrag))
            {
                String audioForDemo = getAudioForScene(currentEvent(), "DEMO").get(2); // Your Turn!;
                demo_dragNumberToBoxWithAudio(Arrays.asList(audioForDemo), true);
            }
            else if (mode.equals(kModeType))
            {
                String audioForDemo = getAudioForScene(currentEvent(), "DEMO").get(2); // Your Turn!
                demo_typeInNumberWithAudio(Arrays.asList(audioForDemo), true);
            }
        }
        else
        {
            playAudioScene("DEMO", 0, false); // Look. The number is missing.;
            OC_Generic.pointer_moveToObject(box, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
            waitAudio();
            waitForSecs(0.3f);
            //
            playAudioScene("DEMO", 1, false); // Watch me.
            if (mode.equals(kModeDrag))
            {
                String audioForDemo = getAudioForScene(currentEvent(), "DEMO").get(2); // Your Turn!;
                demo_dragNumberToBoxWithAudio(Arrays.asList(audioForDemo), false);
            }
            else if (mode.equals(kModeType))
            {
                String audioForDemo = getAudioForScene(currentEvent(), "DEMO").get(2); // Your Turn!
                demo_typeInNumberWithAudio(Arrays.asList(audioForDemo), false);
            }
        }
        //
        moveScenePointer(new PointF(0.7f * bounds().width(), 0.7f * bounds().height()), 0.6f, null, 0.3f);
        //
        lockScreen();
        if (dashes != null)
        {
            for (OBPath dash : dashes)
            {
                dash.show();
                dash.setStrokeColor(colourDashNormal);
            }
        }
        if (dashLabels != null)
        {
            for (OBLabel label : dashLabels)
            {
                PointF previousPosition = OC_Generic.copyPoint((PointF) label.propertyValue("previous_position"));
                label.setPosition(previousPosition);
                label.setString("");
                label.show();
            }
        }
        //
        OBControl emptyNumberBox = numberBoxes.get(hiddenNumberIndex);
        OBLabel emptyNumberBoxLabel = (OBLabel) emptyNumberBox.propertyValue("label");
        emptyNumberBoxLabel.setString("");
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        nextScene();
    }


    public void democ () throws Exception
    {
        setStatus(STATUS_BUSY);
        if (!events.contains("b"))
        {
            lockScreen();
            resetScene();
            unlockScreen();
            //
            introScene(false);
        }
        OBPath box = (OBPath) numberBoxes.get(hiddenNumberIndex);
        if (mode.equals(kModeDrag))
        {
            if (usesBigBox)
            {
                playAudioScene("DEMO", 0, false); // Listen! Then choose the number you heard.;
                moveScenePointer(new PointF(0.75f * bounds().width(), 0.6f * bounds().height()), 0.6f, null, 0.3f);
                waitAudio();
                waitForSecs(0.3f);
                //
                playAudioScene("DEMO", 1, false); //Drag it to the box.;
                OC_Generic.pointer_moveToObject(box, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
                waitAudio();
                waitForSecs(0.3f);
                //
                playAudioScene("DEMO", 2, false); // This might help!;
                PointF butPt = OB_Maths.locationForRect(new PointF(0.5f, 1.1f), MainViewController().topRightButton.frame);
                movePointerToPoint(butPt, 0.6f, false);
                waitAudio();
                waitForSecs(0.3f);
                //
                thePointer.hide();
                waitForSecs(0.3f);
                //
                doAudio(currentEvent(), true);
            }
            else
            {
                moveScenePointer(new PointF(0.1f * bounds().width(), 0.9f * bounds().height()), 0.3f, null, 0f);
                playAudioScene("DEMO", 0, false); // Choose the correct number.;
                moveScenePointer(new PointF(0.9f * bounds().width(), 0.9f * bounds().height()), 1.2f, null, 0f);
                waitAudio();
                waitForSecs(0.3f);
                //
                playAudioScene("DEMO", 1, false); // Drag it to the empty box.;
                OC_Generic.pointer_moveToObject(box, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
                waitAudio();
                waitForSecs(0.3f);
                //
                thePointer.hide();
                waitForSecs(0.3f);
                //
                doAudio(currentEvent(), false);
            }
        }
        //
        else if (mode.equals(kModeType))
        {
            if (usesBigBox)
            {
                playAudioScene("DEMO", 0, false); // Listen to the number! Then type it!
                moveScenePointer(new PointF(0.75f * bounds().width(), 0.6f * bounds().height()), 0.6f, null, 0.3f);
                waitAudio();
                waitForSecs(0.3f);
                //
                moveScenePointer(new PointF(0.1f * bounds().width(), 0.95f * bounds().height()), 0.3f, null, 0f);
                playAudioScene("DEMO", 1, false); // Use these keys!
                moveScenePointer(new PointF(0.9f * bounds().width(), 0.95f * bounds().height()), 1.2f, null, 0f);
                waitAudio();
                waitForSecs(0.3f);
                //
                playAudioScene("DEMO", 2, false); // This might help!
                PointF butPt = OB_Maths.locationForRect(new PointF(0.5f, 1.1f), MainViewController().topRightButton.frame);
                movePointerToPoint(butPt, 0.6f, false);
                waitAudio();
                waitForSecs(0.3f);
                //
                thePointer.hide();
                waitForSecs(0.3f);
                //
                doAudio(currentEvent(), true);

            }
            else
            {
                playAudioScene("DEMO", 0, false); // Type in the missing number.
                OC_Generic.pointer_moveToObject(box, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
                waitAudio();
                waitForSecs(0.3f);
                //
                moveScenePointer(new PointF(0.1f * bounds().width(), 0.95f * bounds().height()), 0.3f, null, 0f);
                playAudioScene("DEMO", 1, false); // Use these!
                moveScenePointer(new PointF(0.9f * bounds().width(), 0.95f * bounds().height()), 1.2f, null, 0f);
                waitAudio();
                waitForSecs(0.3f);
                //
                thePointer.hide();
                waitForSecs(0.3f);
                //
                doAudio(currentEvent(), false);
            }
        }
        //
        lastActionTakenTimestamp = new Date().getTime();
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demof () throws Exception
    {
        playAudioQueuedScene("FINAL", 0.3f, true);
        waitForSecs(0.3f);
        //
        nextScene();
    }

}
