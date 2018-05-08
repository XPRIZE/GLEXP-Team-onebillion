package org.onebillion.onecourse.mainui.oc_diagnostics;


import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperator;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kOptionsNumberRange;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectNumber_Counters extends OC_Diagnostics_TouchCorrectNumber
{


    public void doAudio(String scene) throws Exception
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String number = (String) currentQuestion.correctAnswers.get(0);
        List replayAudio = getAudioForScene(scene, "PROMPT");
        setReplayAudio(replayAudio);
        //
        MainActivity.log("Correct answer is [%s]", number);
        //
        playAudioQueuedScene("PROMPT", 300, false);
    }


    public void buildScene()
    {
        super.buildScene();
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        int correctAnswer = Integer.parseInt((String) currentQuestion.correctAnswers.get(0));
        List<OBControl> counters = OBUtils.randomlySortedArray(filterControls("counter.*"));
        hideControls("counter.*");
        //
        for (int i = 0; i < correctAnswer; i++)
        {
            OBControl counter = counters.get(i);
            counter.show();
        }
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID)
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        List<String> rangeArray = Arrays.asList(((String) exerciseData.get(kOptionsNumberRange)).split("-"));
        String operator = (String) exerciseData.get(kNumberOperator);
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
            MainActivity.log("OC_Diagnostics_TouchCorrectNumber_Counters:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        //
        List usedPairs = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            List selectedParameters = generateUniqueSet(allParameters, 1, usedPairs);
            int correctValue = Integer.parseInt((String) selectedParameters.get(0));
            //
            List unitsUsed = new ArrayList<>();
            String correctAnswer = String.format("%d", correctValue);
            List possibleDistractors = new ArrayList();
            possibleDistractors.addAll(allParameters);
            possibleDistractors.remove(correctAnswer);
            possibleDistractors = OBUtils.randomlySortedArray(possibleDistractors);
            //
            List distractors = possibleDistractors.subList(0, possibleAnswerCount - 1);
            distractors.add(correctAnswer);
            distractors = OBUtils.randomlySortedArray(distractors);
            //
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            result.add(question);
        }
        return result;
    }
}
