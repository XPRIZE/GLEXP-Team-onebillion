package com.maq.xprize.onecourse.mainui.oc_diagnostics;

import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberComparison;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberComparisonLarger;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberComparisonSmaller;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kOptionsNumberRange;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectNumber_QuantityDiscrimination extends OC_Diagnostics_TouchCorrectNumber
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
        promptAudioLock = playAudioQueuedScene("PROMPT", 0.3f, false);
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID)
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        List<String> rangeArray = Arrays.asList(((String) exerciseData.get(kOptionsNumberRange)).split("-"));
        String comparison = (String) exerciseData.get(kNumberComparison);
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
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectNumber_QuantityDiscrimination:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        List usedPairs = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            List<String> selectedParameters = generateUniqueSet(allParameters, possibleAnswerCount, usedPairs);
            List unitsUsed = new ArrayList<>();
            String correctAnswer = OBUtils.randomlySortedArray(selectedParameters).get(0);
            //
            if (comparison.equals(kNumberComparisonLarger))
            {
                int maxValue = Integer.parseInt(selectedParameters.get(0));
                for (String value : selectedParameters)
                {
                    maxValue = Math.max(Integer.parseInt(value), maxValue);
                }
                correctAnswer = String.format("%d", maxValue);
            }
            else if (comparison.equals(kNumberComparisonSmaller))
            {
                int minValue = Integer.parseInt(selectedParameters.get(0));
                for (String value : selectedParameters)
                {
                    minValue = Math.min(Integer.parseInt(value), minValue);
                }
                correctAnswer = String.format("%d", minValue);
            }
            //
            List distractors = selectedParameters;
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            result.add(question);
        }
        return result;
    }
}
