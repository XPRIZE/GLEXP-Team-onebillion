package org.onebillion.onecourse.mainui.oc_diagnostics;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kParameterMaxWordLength;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

/**
 * Created by pedroloureiro on 03/05/2018.
 */

public class OC_Diagnostics_TouchCorrectObject extends OC_Diagnostics
{

    public void prepare()
    {
        super.prepare();
        doVisual(currentEvent());
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        Map<String, List> availableParameters = OC_DiagnosticsManager.sharedManager().unitsPerParameterForEvent(eventUUID);
        List allParameters = Arrays.asList(availableParameters.keySet().toArray());
        //
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        //
        String maxLengthString = (String) exerciseData.get(kParameterMaxWordLength);
        int maxLength = 0;
        if (maxLengthString != null)
        {
            maxLength = Integer.parseInt(maxLengthString);
        }
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectObject:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        List pickedAnswers = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            List<String> randomParameters = OBUtils.randomlySortedArray(allParameters);
            List<String> selectedParameters = new ArrayList<>();
            for (String phonemeUUID : randomParameters)
            {
                OBPhoneme phoneme = (OBPhoneme) OC_DiagnosticsManager.sharedManager().WordComponents().get(phonemeUUID);
                int length = phoneme.text.length();
                int occurrences_m = length - phoneme.text.replace("m", "").length();
                int occurrences_w = length - phoneme.text.replace("w", "").length();
                //
                length += occurrences_m + occurrences_w;
                //
                if (maxLength > 0 && length > maxLength)
                {
                    continue;
                }
                selectedParameters.add(phonemeUUID);
                if (selectedParameters.size() == possibleAnswerCount)
                {
                    break;
                }
            }
            List unitsUsed = new ArrayList<>();
            for (String parameter : selectedParameters)
            {
                unitsUsed.addAll(availableParameters.get(parameter));
            }
            String correctAnswer;
            do
            {
                correctAnswer = OBUtils.randomlySortedArray(selectedParameters).get(0);
            }
            while (pickedAnswers.contains(correctAnswer));
            //
            pickedAnswers.add(correctAnswer);
            //
            List distractors = OBUtils.randomlySortedArray(selectedParameters);
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            //
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            result.add(question);
        }
        return result;
    }


    public void doAudio(String scene) throws Exception
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        OBPhoneme phoneme = (OBPhoneme) OC_DiagnosticsManager.sharedManager().WordComponents().get(currentQuestion.correctAnswers.get(0));
        //
        List replayAudio = new ArrayList();
        replayAudio.addAll(getAudioForScene(scene, "PROMPT"));
        replayAudio.add(phoneme.audio());
        //
        setReplayAudio(replayAudio);
        MainActivity.log("Correct answer is [%s]", phoneme.text);
        playAudioQueuedScene("PROMPT", 0.3f, true);
        //
        phoneme.playAudio(this, false);
    }


    public void buildScene()
    {
        if (questions == null) return;
        //
        int totalParameters = filterControls("label.*").size();
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        float fontSize = bestFontSize();
        //
        MainActivity.log("OC_Diagnostics_TouchCorrectObject --> using font size %f.()", fontSize);
        for (int i = 0; i < totalParameters; i++)
        {
            OBControl labelBox = objectDict.get(String.format("label%d", i + 1));
            String phonemeUUID = currentQuestion.distractors.get(i);
            OBPhoneme phoneme = (OBPhoneme) OC_DiagnosticsManager.sharedManager().WordComponents().get(phonemeUUID);
            if (phoneme == null)
            {
                MainActivity.log("OC_Diagnostics_TouchCorrectObject --> ERROR: unable to find phoneme with UUID [%s]", phonemeUUID);
                return;
            }
            OBLabel phonemeLabel = OC_Generic.action_createLabelForControl(labelBox, phoneme.text, 1.0f, false, false, OBUtils.standardTypeFace(), fontSize, Color.BLACK, this);
            phonemeLabel.setFontSize(fontSize);
            phonemeLabel.sizeToBoundingBox();
            phonemeLabel.setProperty("phoneme", phoneme);
            touchables.add(phonemeLabel);
            attachControl(phonemeLabel);
            detachControl(labelBox);
        }
    }


    public float bestFontSize()
    {
        float fontSize = -1;
        OBControl labelBox = objectDict.get("label1");
        for (OC_DiagnosticsQuestion question : questions)
        {
            for (String distractor : question.distractors)
            {
                OBPhoneme phoneme = (OBPhoneme) OC_DiagnosticsManager.sharedManager().WordComponents().get(distractor);
                OBLabel phonemeLabel = OC_Generic.action_createLabelForControl(labelBox, phoneme.text, 1.0f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
                fontSize = (fontSize == -1 || fontSize > phonemeLabel.fontSize()) ? phonemeLabel.fontSize() : fontSize;
                detachControl(phonemeLabel);
            }
        }
        return fontSize;
    }


    public void toggleTouchedObject(OBControl control, boolean value)
    {
        OBLabel label = (OBLabel) control;
        label.setColour(value ? Color.RED : Color.BLACK);
    }


    public boolean isAnswerCorrect(OBControl label, boolean saveInformation)
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        OBPhoneme userAnswer = (OBPhoneme) label.propertyValue("phoneme");
        //
        if (saveInformation)
        {
            relevantParametersForRemedialUnits = new ArrayList();
            relevantParametersForRemedialUnits.addAll(currentQuestion.correctAnswers);
            relevantParametersForRemedialUnits.add(userAnswer.soundid);
        }
        return currentQuestion.correctAnswers.contains(userAnswer.soundid);
    }


    public void showAnswerFeedback(OBControl userAnswer) throws Exception
    {
        boolean answerCorrect = isAnswerCorrect(userAnswer, false);
        //
        lockScreen();
        if (answerCorrect)
        {
            loadTickAtControl(userAnswer);
        }
        else
        {
            loadCrossAtControl(userAnswer);
        }
        unlockScreen();
        //
        waitForSecs(1.2f);
    }


    public void checkObject(OBControl control) throws Exception
    {
        setStatus(STATUS_CHECKING);
        toggleTouchedObject(control, true);
        if (isAnswerCorrect(control, true))
        {
            gotItRightBigTick(false);
            waitForSecs(0.3f);
            //
            toggleTouchedObject(control, false);
            showAnswerFeedback(control);
            currNo++;
            if (currNo >= questions.size())
            {
                OC_DiagnosticsManager.sharedManager().markQuestion(eventUUID, true, Arrays.asList());
                return;
            }
            else
            {
                lockScreen();
                endScene();
                buildScene();
                unlockScreen();
                //
                doAudio(currentEvent());
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            toggleTouchedObject(control, false);
            showAnswerFeedback(control);
            OC_DiagnosticsManager.sharedManager().markQuestion(eventUUID, false, relevantParametersForRemedialUnits);
            return;
        }
        setStatus(STATUS_AWAITING_CLICK);
    }


    public Object findObject(PointF pt)
    {
        OBControl c = finger(0, 2, touchables, pt, true);
        return c;
    }


    public void checkConfirmAnswerButton(OBControl control) throws Exception
    {
        setStatus(STATUS_CHECKING);
        if (isAnswerCorrect(control, true))
        {
            gotItRightBigTick(false);
            waitForSecs(0.3f);
            //
            showAnswerFeedback(control);
            waitForSecs(0.6f);
            //
            currNo++;
            if (currNo >= questions.size())
            {
                OC_DiagnosticsManager.sharedManager().markQuestion(eventUUID, true, Arrays.asList());
            }
            else
            {
                lockScreen();
                endScene();
                buildScene();
                unlockScreen();
                //
                doAudio(currentEvent());
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            showAnswerFeedback(control);
            waitForSecs(0.6f);
            //
            OC_DiagnosticsManager.sharedManager().markQuestion(eventUUID, false, relevantParametersForRemedialUnits);
        }
        setStatus(STATUS_AWAITING_CLICK);
    }



    public Object findConfirmAnswerButton(PointF pt)
    {
        OBControl c = finger(0,2,Arrays.asList(confirmAnswerButton),pt, true);
        return c;

    }
    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            updateLastActionTimeStamp(true);
            final Object obj = findObject(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkObject((OBControl) obj);
                    }
                });
            }
            else
            {
                final Object button = findConfirmAnswerButton(pt);
                if (button != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkConfirmAnswerButton((OBControl) button);

                        }
                    });
                }
            }
        }
    }
}