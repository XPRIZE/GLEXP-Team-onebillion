package com.maq.xprize.onecourse.mainui.oc_diagnostics;

import android.graphics.Color;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kDistractorsNumberRange;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperator;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperatorAddition;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kNumberOperatorSubtraction;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kOptionsNumberRange;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static com.maq.xprize.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;


public class OC_Diagnostics_TouchCorrectNumber_Equation extends OC_Diagnostics_TouchCorrectNumber
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
        promptAudioLock = playAudioQueuedScene("PROMPT", 0.3f, false);
    }


    public void buildScene()
    {
        super.buildScene();
        //
        if (questions == null) return;
        //
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String equation = (String) currentQuestion.additionalInformation.get("equation");
        OBControl equationBox = objectDict.get("equation");
        if (equationBox == null) return;
        //
        OBLabel equationLabel = OC_Generic.action_createLabelForControl(equationBox, equation, 1.0f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
        inertObjects.add(equationLabel);
        //
        attachControl(equationLabel);
        detachControl(equationBox);
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        List<String> rangeArray = Arrays.asList(((String) exerciseData.get(kOptionsNumberRange)).split("-"));
        //
        String operator = (String) exerciseData.get(kNumberOperator);
        int min = Integer.parseInt(rangeArray.get(0));
        int max = Integer.parseInt(rangeArray.get(1));
        //
        List allParameters = new ArrayList<>();
        for (int i = min; i <= max; i++)
        {
            allParameters.add(String.format("%d", i));
        }
        //
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectNumber_Equation:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        //
        List usedPairs = new ArrayList<>();
        for (int i = 0; i < totalQuestions; i++)
        {
            List<String> selectedParameters = generateUniqueSet(allParameters, 2, usedPairs);
            String equation = "";
            int number1 = Integer.parseInt(selectedParameters.get(0));
            int number2 = Integer.parseInt(selectedParameters.get(1));
            int correctValue = 0;
            //
            if (operator.equals(kNumberOperatorAddition))
            {
                correctValue = number1 + number2;
            }
            else if (operator.equals(kNumberOperatorSubtraction))
            {
                if (number2 > number1)
                {
                    int temp = number1;
                    number1 = number2;
                    number2 = temp;
                }
                correctValue = number1 - number2;
            }
            //
            List<String> distractorsRangeArray = Arrays.asList(((String)exerciseData.get(kDistractorsNumberRange)).split("-"));
            //
            String minDistractorString = distractorsRangeArray.get(0);
            if (minDistractorString.contains("n"))
            {
                minDistractorString = minDistractorString.replace("n", String.format("%d", correctValue));
                minDistractorString = String.valueOf(Math.round(OC_Diagnostics.evalExpression(minDistractorString)));
            }
            //
            String maxDistractorString = distractorsRangeArray.get(1);
            if (maxDistractorString.contains("n"))
            {
                maxDistractorString = maxDistractorString.replace("n", String.format("%d", correctValue));
                maxDistractorString = String.valueOf(Math.round(OC_Diagnostics.evalExpression(maxDistractorString)));
            }
            //
            int minDistractor = Integer.parseInt(minDistractorString);
            int maxDistractor = Integer.parseInt(maxDistractorString);
            //;
            List allDistractors = new ArrayList<>();
            for (int j = minDistractor; j <= maxDistractor; j++)
            {
                allDistractors.add(String.format("%d", j));
            }
            //
            equation = String.format("%d %s %d =", number1, operator, number2);
            //
            List unitsUsed = new ArrayList<>();
            //
            // TODO: still missing the remedial units for numeracy exercises;
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
            question.additionalInformation.put("equation", equation);
            result.add(question);
        }
        return result;
    }


}