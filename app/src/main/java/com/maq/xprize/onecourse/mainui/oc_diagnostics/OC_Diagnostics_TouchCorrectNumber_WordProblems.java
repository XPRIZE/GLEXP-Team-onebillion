package com.maq.xprize.onecourse.mainui.oc_diagnostics;

import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kAnswerNumberRange;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kAudioOffsetParameter1;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kAudioOffsetParameter2;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperator;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperatorAddition;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperatorSubtraction;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kParameter1NumberRange;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kParameter2NumberRange;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectNumber_WordProblems extends OC_Diagnostics_TouchCorrectNumber_Equation
{

    public void doAudio(String scene) throws Exception
    {
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        //
        int audioOffsetNumber1 = Integer.parseInt((String) exerciseData.get(kAudioOffsetParameter1));
        int audioOffsetNumber2 = Integer.parseInt((String) exerciseData.get(kAudioOffsetParameter2));
        //
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        //
        int number1 = Integer.parseInt((String) currentQuestion.additionalInformation.get("number1"));
        int number2 = Integer.parseInt((String) currentQuestion.additionalInformation.get("number2"));
        //
        String introAudio = ((List<String>) ((Map<String, Object>) audioScenes.get(eventUUID)).get("DEMO")).get(0);
        Map sceneAudio = (Map<String, Object>) audioScenes.get(String.format("%s%d", eventUUID, currNo + 1));
        //
        String promptPart1 = ((List<String>) sceneAudio.get("PROMPT")).get(number1 + audioOffsetNumber1);
        String promptPart2 = ((List<String>) sceneAudio.get("PROMPT2")).get(number2 + audioOffsetNumber2);
        //
        List promptPart3 = (List<String>) sceneAudio.get("PROMPT3");
        List promptAudio = new ArrayList();
        promptAudio.add(introAudio);
        promptAudio.add(1000);
        promptAudio.add(promptPart1);
        promptAudio.add(promptPart2);
        promptAudio.addAll(promptPart3);
        //
        String number = (String) currentQuestion.correctAnswers.get(0);
        //
        MainActivity.log("Correct answer is [%s]", number);
        //
        setReplayAudio(promptAudio);
        promptAudioLock = playAudioQueued(promptAudio, false);
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        //
        List<String> answerRangeArray = Arrays.asList(((String) exerciseData.get(kAnswerNumberRange)).split("-"));
        int minAnswer = Integer.parseInt(answerRangeArray.get(0));
        int maxAnswer = Integer.parseInt(answerRangeArray.get(1));
        //
        String operator = (String) exerciseData.get(kNumberOperator);
        //
        List<String> rangeArrayParameter1 = Arrays.asList(((String) exerciseData.get(kParameter1NumberRange)).split("-"));
        int minParameter1 = Integer.parseInt(rangeArrayParameter1.get(0));
        int maxParameter1 = Integer.parseInt(rangeArrayParameter1.get(1));
        //
        List allValuesParameter1 = new ArrayList<>();
        for (int i = minParameter1; i <= maxParameter1; i++)
        {
            allValuesParameter1.add(String.format("%d", i));
        }
        //
        List<String> rangeArrayParameter2 = Arrays.asList(((String) exerciseData.get(kParameter2NumberRange)).split("-"));
        int minParameter2 = Integer.parseInt(rangeArrayParameter2.get(0));
        int maxParameter2 = Integer.parseInt(rangeArrayParameter2.get(1));
        //
        List allValuesParameter2 = new ArrayList<>();
        for (int i = minParameter2; i <= maxParameter2; i++)
        {
            allValuesParameter2.add(String.format("%d", i));
        }
        //
        List<String> distractorsRangeArray = Arrays.asList(((String) exerciseData.get(kAnswerNumberRange)).split("-"));
        int minDistractor = Integer.parseInt(distractorsRangeArray.get(0));
        int maxDistractor = Integer.parseInt(distractorsRangeArray.get(1));
        //
        List allDistractors = new ArrayList<>();
        for (int i = minDistractor; i <= maxDistractor; i++)
        {
            allDistractors.add(String.format("%d", i));
        }
        //
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        //
        List usedPairs = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            int number1 = 0, number2 = 0, correctValue = 0;
            if (operator.equals(kNumberOperatorAddition))
            {
                while (correctValue < minAnswer || correctValue > maxAnswer)
                {
                    List selectedParameters = generateUniqueSet(allValuesParameter1, allValuesParameter2, usedPairs);
                    number1 = Integer.parseInt((String) selectedParameters.get(0));
                    number2 = Integer.parseInt((String) selectedParameters.get(1));
                    correctValue = number1 + number2;
                }
            }
            else if (operator.equals(kNumberOperatorSubtraction))
            {
                while (correctValue < minAnswer || correctValue > maxAnswer)
                {
                    List selectedParameters = generateUniqueSet(allValuesParameter1, allValuesParameter2, usedPairs);
                    number1 = Integer.parseInt((String) selectedParameters.get(0));
                    number2 = Integer.parseInt((String) selectedParameters.get(1));
                    //
                    if (number2 > number1)
                    {
                        int temp = number1;
                        number1 = number2;
                        number2 = temp;

                    }
                    //
                    correctValue = number1 - number2;
                }
            }
            //
            List unitsUsed = new ArrayList<>();
            String correctAnswer = String.format("%d", correctValue);
            //
            List possibleDistractors = new ArrayList();
            possibleDistractors.addAll(allDistractors);
            possibleDistractors.remove(correctAnswer);
            possibleDistractors = OBUtils.randomlySortedArray(possibleDistractors);
            //
            List distractors = possibleDistractors.subList(0, possibleAnswerCount - 1);
            distractors.add(correctAnswer);
            distractors = OBUtils.randomlySortedArray(distractors);
            //
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            //
            question.additionalInformation.put("number1", String.format("%d", number1));
            question.additionalInformation.put("number2", String.format("%d", number2));
            result.add(question);
        }
        return result;
    }
}
