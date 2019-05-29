package com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics;


import android.graphics.Color;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBXMLManager;
import com.maq.xprize.onecourse.hindi.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kLetterCase;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kLowercase;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;
import static com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics.OC_DiagnosticsManager.kUppercase;

public class OC_Diagnostics_TouchCorrectLetter extends OC_Diagnostics_TouchCorrectObject
{


    public void doAudio(String scene) throws Exception
    {
        if (questions == null) return;
        //
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String letterName = (String) currentQuestion.correctAnswers.get(0);
        String phonemeAudio = String.format("is_%s", letterName.toLowerCase());
        //
        List replayAudio = new ArrayList();
        replayAudio.addAll(getAudioForScene(scene, "PROMPT"));
        replayAudio.add(phonemeAudio);
        setReplayAudio(replayAudio);
        //
        MainActivity.log("Correct answer is [%s]", letterName);
        //
        promptAudioLock = playAudioQueued(replayAudio, false);
    }


    public void buildScene()
    {
        if (questions == null) return;
        //
        int totalParameters = filterControls("label.*").size();
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        String letterCase = (String) exerciseData.get(kLetterCase);
        for (int i = 0; i < totalParameters; i++)
        {
            OBControl labelBox = objectDict.get(String.format("label%d", i + 1));
            String letterName = currentQuestion.distractors.get(i);
            if (letterCase.equals(kUppercase))
            {
                letterName = OC_Generic.toTitleCase(letterName);
            }
            else if (letterName.equals(kLowercase))
            {
                letterName = letterName.toLowerCase();
            }
            OBLabel letterLabel = OC_Generic.action_createLabelForControl(labelBox, letterName, 1.0f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
            letterLabel.setProperty("letter", letterName);
            //
            touchables.add(letterLabel);
            //
            attachControl(letterLabel);
            detachControl(labelBox);
        }
    }


    public static void convertAllToLowerCase(List<String> strings)
    {
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext())
        {
            iterator.set(iterator.next().toLowerCase());
        }
    }

    public boolean isAnswerCorrect(OBControl label, boolean saveInformation)
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        //
        List correctAnswers = new ArrayList();
        correctAnswers.addAll(currentQuestion.correctAnswers);
        convertAllToLowerCase(correctAnswers);
        //
        String userAnswer = ((String) label.propertyValue("letter")).toLowerCase();
        //
        if (saveInformation)
        {
            relevantParametersForRemedialUnits = new ArrayList();
            relevantParametersForRemedialUnits.addAll(correctAnswers);
            relevantParametersForRemedialUnits.add(userAnswer);
        }
        return correctAnswers.contains(userAnswer);
    }


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
        String letterCase = (String) exerciseData.get(kLetterCase);
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectLetter:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise [%s]", eventUUID);
            return null;
        }
        OBXMLManager xmlManager = new OBXMLManager();
        OBXMLNode letterExclusionsXML = xmlManager.parseFile(OBUtils.getInputStreamForPath(getLocalPath("_letter_exclusions.xml"))).get(0);
        List<List<String>> letterExclusions = new ArrayList<>();
        for (OBXMLNode node : letterExclusionsXML.children)
        {
            List<String> exclusionList = Arrays.asList(node.contents.split(","));
            letterExclusions.add(exclusionList);
        }
        //
        for (int i = 0; i < totalQuestions; i++)
        {
            List<String> randomParameters = OBUtils.randomlySortedArray(allParameters);
            List selectedParameters = new ArrayList<>();
            for (String letterName : randomParameters)
            {
                if (letterCase.equalsIgnoreCase(kLowercase))
                {
                    letterName = letterName.toLowerCase();
                }
                if (letterCase.equalsIgnoreCase(kUppercase))
                {
                    letterName = OC_Generic.toTitleCase(letterName);
                }
                //
                boolean exclusionTriggered = false;
                for (List<String> exclusionList : letterExclusions)
                {
                    for (String letter : exclusionList)
                    {
                        if (selectedParameters.contains(letter) && exclusionList.contains(letterName))
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
                if (selectedParameters.contains(letterName)) continue;
                //
                selectedParameters.add(letterName);
                if (selectedParameters.size() == possibleAnswerCount)
                {
                    break;
                }
            }
            //
            List unitsUsed = new ArrayList<>();
            String correctAnswer = (String) OBUtils.randomlySortedArray(selectedParameters).get(0);
            List distractors = OBUtils.randomlySortedArray(selectedParameters);
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID,Arrays.asList(correctAnswer), distractors, uniqueUnits);
            result.add(question);
        }
        return result;
    }

}



/*




 */