package org.onebillion.onecourse.mainui.oc_diagnostics;


import android.graphics.Color;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kOptionsNumberRange;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kSequenceLength;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;


public class OC_Diagnostics_TouchCorrectNumber_MissingNumber extends OC_Diagnostics_TouchCorrectNumber
{
    public void doAudio(String scene) throws Exception
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String number = (String) currentQuestion.correctAnswers.get(0);
        //
        MainActivity.log("Correct answer is [%s]", number);
        //
        List promptAudio = getAudioForScene(scene, "PROMPT");
        setReplayAudio(promptAudio);
        //
        playAudioQueuedScene("PROMPT", 300, false);
    }


    public void buildScene()
    {
        int totalParameters = filterControls("label.*").size();
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String correctAnswer = (String) currentQuestion.correctAnswers.get(0);
        List<String> sequence = (List<String>) currentQuestion.additionalInformation.get("sequence");
        for (int i = 0; i < totalParameters; i++)
        {
            OBControl numberBox = objectDict.get(String.format("label%d", i + 1));
            String number = currentQuestion.distractors.get(i);
            OBLabel numberLabel = OC_Generic.action_createLabelForControl(numberBox, number, 1.0f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
            numberLabel.setProperty("number", number);
            //
            touchables.add(numberLabel);
            //
            attachControl(numberLabel);
            detachControl(numberBox);
        }
        //
        int totalSequenceElements = (int) filterControls("sequence.*").size();
        OBPath missingBox = (OBPath) objectDict.get("box");
        for (int i = 0; i < totalSequenceElements; i++)
        {
            OBControl numberBox = objectDict.get(String.format("sequence%d", i + 1));
            String number = sequence.get(i);
            //
            if (correctAnswer.equals(number))
            {
                missingBox.setPosition(numberBox.position());
            }
            else
            {
                OBLabel numberLabel = OC_Generic.action_createLabelForControl(numberBox, number, 1.0f, false, OBUtils.standardTypeFace(), missingBox.strokeColor(), this);
                numberLabel.setProperty("number", number);
                //
                inertObjects.add(numberLabel);
                //
                attachControl(numberLabel);
            }
            detachControl(numberBox);
        }
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID)
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        List<String> rangeArray = Arrays.asList(((String) exerciseData.get(kOptionsNumberRange)).split("-"));
        int sequenceLength = Integer.parseInt((String) exerciseData.get(kSequenceLength));
        int min = Integer.parseInt(rangeArray.get(0));
        int max = Integer.parseInt(rangeArray.get(1));
        //
        List allParameters = new ArrayList<>();
        for (int i = min; i <= max; i++)
        {
            allParameters.add(String.format("%d", i));
        }
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectNumber_MissingNumber:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        //
        List alreadyUsedStartingNumbers = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            int randomStart;
            do
            {
                randomStart = OB_Maths.randomInt(0, allParameters.size() - sequenceLength);
            }
            while (alreadyUsedStartingNumbers.contains(randomStart));
            //
            alreadyUsedStartingNumbers.add(randomStart);
            List<String> selectedParameters = allParameters.subList(randomStart, randomStart + sequenceLength);
            //
            List unitsUsed = new ArrayList<>();
            String correctAnswer = OBUtils.randomlySortedArray(selectedParameters).get(0);
            //
            List distractors = OBUtils.randomlySortedArray(allParameters);
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            question.additionalInformation.put("sequence", selectedParameters);
            result.add(question);
        }
        return result;
    }
}
