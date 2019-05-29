package com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics;


import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBXMLManager;
import com.maq.xprize.onecourse.hindi.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectSyllable extends OC_Diagnostics_TouchCorrectObject
{
    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        Map availableParameters = OC_DiagnosticsManager.sharedManager().unitsPerParameterForEvent(eventUUID);
        //
        List allParameters = new ArrayList();
        allParameters.addAll(availableParameters.keySet());
        //
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectSyllable:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        List pickedAnswers = new ArrayList<>();
        OBXMLManager xmlManager = new OBXMLManager();
        OBXMLNode syllableExclusionsXML = xmlManager.parseFile(OBUtils.getInputStreamForPath(getLocalPath("_syllable_exclusions.xml"))).get(0);
        List<List<String>> syllableExclusions = new ArrayList<>();
        for (OBXMLNode node : syllableExclusionsXML.children)
        {
            List<String> exclusionList = Arrays.asList(node.contents.split(","));
            syllableExclusions.add(exclusionList);
        }
        for (int i = 0; i < totalQuestions; i++)
        {
            List<String> randomParameters = OBUtils.randomlySortedArray(allParameters);
            List<String> selectedParameters = new ArrayList<>();
            for (String randomSyllable : randomParameters)
            {
                boolean exclusionTriggered = false;
                for (List<String> exclusionList : syllableExclusions)
                {
                    for (String syllable : exclusionList)
                    {
                        if (selectedParameters.contains(syllable) && exclusionList.contains(randomSyllable))
                        {
                            exclusionTriggered = true;
                            break;
                        }
                    }
                    if (exclusionTriggered)
                    {
                        break;
                    }
                }
                if (exclusionTriggered)
                {
                    continue;
                }
                //
                selectedParameters.add(randomSyllable);
                if (selectedParameters.size() == possibleAnswerCount)
                {
                    break;
                }
            }
            List unitsUsed = new ArrayList<>();
            for (String parameter : selectedParameters)
            {
                unitsUsed.addAll((List<String>) availableParameters.get(parameter));
            }
            //
            String correctAnswer;
            do
            {
                correctAnswer = OBUtils.randomlySortedArray(selectedParameters).get(0);
            }
            while (pickedAnswers.contains(correctAnswer));
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

}
