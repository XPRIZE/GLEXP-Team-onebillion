package com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics;

import android.graphics.Color;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kAudioOffset;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kOptionsNumberRange;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectNumber extends OC_Diagnostics_TouchCorrectObject
{

    public void doAudio(String scene) throws Exception
    {
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        int audioOffset = Integer.parseInt((String) exerciseData.get(kAudioOffset));
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        int number = Integer.parseInt((String) currentQuestion.correctAnswers.get(0));
        //
        MainActivity.log("Correct answer is [%s]", number);
        //
        String promptAudio = getAudioForScene(scene, "PROMPT").get(number + audioOffset);
        setReplayAudio((List<Object>) (Object) Arrays.asList(promptAudio));
        promptAudioLock = playAudioQueued((List<Object>) (Object) Arrays.asList(promptAudio), false);
    }


    public void buildScene()
    {
        if (questions == null) return;
        //
        int totalParameters = filterControls("label.*").size();
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        for (int i = 0; i < totalParameters; i++)
        {
            OBControl numberBox = objectDict.get(String.format("label%d", i + 1));
            String number = currentQuestion.distractors.get(i);
            OBLabel numberLabel = OC_Generic.action_createLabelForControl(numberBox, number, 1.0f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
            numberLabel.setProperty("number", number);
            touchables.add(numberLabel);
            //
            attachControl(numberLabel);
            detachControl(numberBox);
        }
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        List rangeArray = Arrays.asList(((String) exerciseData.get(kOptionsNumberRange)).split("-"));
        //
        int min = Integer.parseInt((String) rangeArray.get(0));
        int max = Integer.parseInt((String) rangeArray.get(1));
        //
        List<String> allParameters = new ArrayList<>();
        for (int i = min; i <= max; i++)
        {
            allParameters.add(String.format("%d", i));
        }
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectNumber:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        List pickedAnswers = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            List randomParameters = OBUtils.randomlySortedArray(allParameters);
            List selectedParameters = randomParameters.subList(0, Math.min(randomParameters.size(), possibleAnswerCount));
            List unitsUsed = new ArrayList<>();
            //
            String correctAnswer;
            do
            {
                correctAnswer = (String) OBUtils.randomlySortedArray(selectedParameters).get(0);
            }
            while (pickedAnswers.contains(correctAnswer));
            //
            pickedAnswers.add(correctAnswer);
            List distractors = OBUtils.randomlySortedArray(allParameters);
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            //
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            result.add(question);
        }
        return result;
    }


    public boolean isAnswerCorrect(OBControl label, boolean saveInformation)
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String correctAnswer = (String) currentQuestion.correctAnswers.get(0);
        String userAnswer = (String) label.propertyValue("number");
        if (saveInformation)
        {
            relevantParametersForRemedialUnits = new ArrayList();
            relevantParametersForRemedialUnits.addAll(currentQuestion.correctAnswers);
            relevantParametersForRemedialUnits.add(userAnswer);
        }
        return correctAnswer.equals(userAnswer);
    }

}


/*

 */