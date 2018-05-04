package org.onebillion.onecourse.mainui.oc_diagnostics;

import android.graphics.Color;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBSyllable;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kParameterMaxWordLength;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectWord_InitialSyllable extends OC_Diagnostics_TouchCorrectObject
{


    public void doAudio(String scene) throws Exception
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        OBWord word = (OBWord) OC_DiagnosticsManager.sharedManager().WordComponents().get(currentQuestion.correctAnswers.get(0));
        OBSyllable firstSyllable = word.syllables.get(0);
        //
        List replayAudio = new ArrayList();
        replayAudio.addAll(getAudioForScene(scene, "PROMPT"));
        replayAudio.add(firstSyllable.audio());
        //
        setReplayAudio(replayAudio);
        MainActivity.log("Correct answer is %s.()", word.text);
        playAudioQueuedScene("PROMPT", 300, true);
        firstSyllable.playAudio(this, false);
    }


    public boolean isAnswerCorrect(OBLabel label, boolean saveInformation)
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        OBWord userAnswer = (OBWord) label.propertyValue("word");
        //
        if (saveInformation)
        {
            relevantParametersForRemedialUnits = new ArrayList();
            relevantParametersForRemedialUnits.addAll(currentQuestion.correctAnswers);
            relevantParametersForRemedialUnits.add(userAnswer.soundid);
        }
        return currentQuestion.correctAnswers.contains(userAnswer.soundid);
    }


    public void buildScene()
    {
        int totalParameters = filterControls("label.*").size();
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        float fontSize = bestFontSize();
        //
        MainActivity.log("OC_Diagnostics_TouchCorrectObject_InitialSyllable --> using font size %f.()", fontSize);
        for (int i = 0; i < totalParameters; i++)
        {
            OBControl labelBox = objectDict.get(String.format("label%d", i + 1));
            String wordUUID = currentQuestion.distractors.get(i);
            OBWord word = (OBWord) OC_DiagnosticsManager.sharedManager().WordComponents().get(wordUUID);
            if (word == null)
            {
                MainActivity.log("OC_Diagnostics_TouchCorrectObject_InitialSyllable --> ERROR: unable to find word with UUID %s.()", wordUUID);
                return;
            }
            OBLabel wordLabel = OC_Generic.action_createLabelForControl(labelBox, word.text, 1.0f, false, false, OBUtils.standardTypeFace(), Color.BLACK, this);
            wordLabel.setProperty("word", word);
            touchables.add(wordLabel);
            attachControl(wordLabel);
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
                OBWord word = (OBWord) OC_DiagnosticsManager.sharedManager().WordComponents().get(distractor);
                OBLabel label = OC_Generic.action_createLabelForControl(labelBox, word.text, 1.0f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
                fontSize = (fontSize == -1 || fontSize > label.fontSize()) ? label.fontSize() : fontSize;
            }
        }
        return fontSize;
    }


    public List generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        Map availableParameters = OC_DiagnosticsManager.sharedManager().unitsPerParameterForEvent(eventUUID);
        List allParameters = Arrays.asList(availableParameters.keySet().toArray());
        //
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        int possibleAnswerCount = Integer.parseInt((String) exerciseData.get(kTotalAvailableOptions));
        int maxLength = Integer.parseInt((String) exerciseData.get(kParameterMaxWordLength));
        //
        if (allParameters.size() < possibleAnswerCount)
        {
            MainActivity.log("OC_Diagnostics_TouchCorrectWord_InitialSyllable:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise %s.()", eventUUID);
            return null;
        }
        OBXMLManager xmlManager = new OBXMLManager();
        OBXMLNode syllableExclusionsXML = xmlManager.parseFile(OBUtils.getInputStreamForPath(getLocalPath("_syllable_exclusions.xml"))).get(0);
        //
        List<List<String>> syllableExclusions = new ArrayList<>();
        for (OBXMLNode node : syllableExclusionsXML.children)
        {
            List exclusionList = Arrays.asList(node.contents.split(","));
            syllableExclusions.add(exclusionList);
        }
        //
        for (int i = 0; i < totalQuestions; i++)
        {
            List<String> randomParameters = OBUtils.randomlySortedArray(allParameters);
            List<String> selectedParameters = new ArrayList<>();
            List selectedInitialSyllables = new ArrayList<>();
            //
            for (String wordUUID : randomParameters)
            {
                if (selectedParameters.size() == 0)
                {
                    selectedParameters.add(wordUUID);
                }
                else
                {

                    OBWord word = (OBWord) OC_DiagnosticsManager.sharedManager().WordComponents().get(wordUUID);
                    int length = word.text.length();
                    int occurrences_m = length - word.text.replace("m", "").length();
                    int occurrences_w = length - word.text.replace("w", "").length();
                    //
                    length += occurrences_m + occurrences_w;
                    //
                    if (maxLength > 0 && length > maxLength)
                    {
                        continue;
                    }
                    OBSyllable initialSyllable = word.syllables.get(0);
                    boolean initialSyllableMatched = false;
                    for (String selectedWordUUID : selectedParameters)
                    {
                        OBWord selectedWord = (OBWord) OC_DiagnosticsManager.sharedManager().WordComponents().get(selectedWordUUID);
                        OBPhoneme selectedWordInitialSyllable = selectedWord.syllables.get(0);
                        //
                        if (initialSyllable.equals(selectedWordInitialSyllable))
                        {
                            initialSyllableMatched = true;
                            break;
                        }
                    }
                    //
                    if (!initialSyllableMatched)
                    {
                        boolean exclusionTriggered = false;
                        for (List<String> exclusionList : syllableExclusions)
                        {
                            for (String syllable : exclusionList)
                            {
                                if (selectedInitialSyllables.contains(syllable) && exclusionList.contains(initialSyllable.text))
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
                        selectedParameters.add(wordUUID);
                        selectedInitialSyllables.add(initialSyllable.text);
                        if (selectedParameters.size() == possibleAnswerCount)
                        {
                            break;
                        }
                    }
                }
            }
            List unitsUsed = new ArrayList<>();
            for (String parameter : selectedParameters)
            {
                unitsUsed.addAll((List) availableParameters.get(parameter));
            }
            String correctAnswer = OBUtils.randomlySortedArray(selectedParameters).get(0);
            List distractors = OBUtils.randomlySortedArray(selectedParameters);
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            //
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, Arrays.asList(correctAnswer), distractors, uniqueUnits);
            result.add(question);
        }
        return result;
    }
}
