package org.onebillion.onecourse.mainui.oc_diagnostics;

import android.graphics.Color;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kParameterMaxWordLength;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kScenarios;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalAvailableOptions;
import static org.onebillion.onecourse.mainui.oc_diagnostics.OC_DiagnosticsManager.kTotalQuestions;

public class OC_Diagnostics_TouchCorrectObject_ShapeIdentification extends OC_Diagnostics_TouchCorrectObject
{


    public void doAudio(String scene) throws Exception
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        String scenario = (String) currentQuestion.additionalInformation.get("scenario");
        List replayAudio = new ArrayList();
        replayAudio.addAll(getAudioForScene(scenario, "PROMPT"));
        //
        MainActivity.log("Correct answer is [%s]", currentQuestion.correctAnswers);
        MainActivity.log("Prompt Audio is [%s]", replayAudio);
        promptAudioLock = playAudioQueued(replayAudio, false);
    }


    public void buildScene()
    {
        touchables = filterControls("shape.*");
        for (OBControl shape : touchables)
        {
            toggleTouchedObject(shape, false);
            //
            OBPath path = (OBPath) shape;
            path.sizeToBoundingBoxIncludingStroke();
        }
    }


    public void toggleTouchedObject(OBControl control, boolean value)
    {
        OBPath path = (OBPath) control;
        path.setFillColor(value ? path.strokeColor() : Color.WHITE);
        path.setProperty("selected", value);
    }


    public boolean isAnswerCorrect(OBControl label, boolean saveInformation)
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        //
        List correctAnswer = new ArrayList();
        correctAnswer.addAll(currentQuestion.correctAnswers);
        //
        List userAnswer = new ArrayList<>();
        //
        for (OBControl shape : touchables)
        {
            String shapeType = (String) shape.attributes().get("type");
            if (shapeType == null)
            {
                MainActivity.log("OC_Diagnostics_TouchCorrectObject_ShapeIdentification --> ERROR: shape type is null for shape [%s]", shape.attributes().get("id"));
            }
            else if ((Boolean) shape.propertyValue("selected"))
            {
                userAnswer.add(shapeType);
            }
        }
        while (correctAnswer.size() == userAnswer.size() && correctAnswer.size() > 0)
        {
            String value = (String) correctAnswer.get(0);
            if (!userAnswer.contains(value))
            {
                return false;
            }
            correctAnswer.remove(0);
            userAnswer.remove(userAnswer.indexOf(value));
        }
        if (saveInformation)
        {
            relevantParametersForRemedialUnits = new ArrayList();
            relevantParametersForRemedialUnits.addAll(currentQuestion.correctAnswers);
            relevantParametersForRemedialUnits.addAll(userAnswer);
        }
        //
        return correctAnswer.size() == userAnswer.size();
    }


    public void showAnswerFeedback(OBControl selectedShape) throws Exception
    {
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        List<String> correctAnswer = new ArrayList();
        correctAnswer.addAll(currentQuestion.correctAnswers);
        //
        List userAnswer = new ArrayList<>();
        //
        for (OBControl shape : touchables)
        {
            String shapeType = (String) shape.attributes().get("type");
            if (shapeType == null)
            {
                MainActivity.log("OC_Diagnostics_TouchCorrectObject_ShapeIdentification --> ERROR: shape type is null for shape [%s]", shape.attributes().get("id"));

            }
            else if ((Boolean) shape.propertyValue("selected"))
            {
                userAnswer.add(shapeType);
            }
        }
        //
        List<String> matchingShapes = new ArrayList<>();
        for (String shape : correctAnswer)
        {
            if (userAnswer.contains(shape))
            {
                matchingShapes.add(shape);
                userAnswer.remove(userAnswer.indexOf(shape));
            }
        }
        for (String shape : matchingShapes)
        {
            correctAnswer.remove(correctAnswer.indexOf(shape));
        }
        //
        lockScreen();
        for (OBControl control : touchables)
        {
            String shapeType = (String) control.attributes().get("type");
            Boolean selected = (Boolean) control.propertyValue("selected");
            if (selected)
            {
                if (matchingShapes.contains(shapeType))
                {
                    loadTickAtControl(control);
                    matchingShapes.remove(matchingShapes.indexOf(shapeType));
                }
                else
                {
                    loadCrossAtControl(control);
                }
            }
        }
        unlockScreen();
        //
        waitForSecs(2.1f);
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        List result = new ArrayList<>();
        Map exerciseData = OC_DiagnosticsManager.sharedManager().parametersForEvent(eventUUID);
        Map scenarioCorrectAnswer = (Map) exerciseData.get(kScenarios);
        List scenarios = new ArrayList();
        scenarios.addAll(Arrays.asList(scenarioCorrectAnswer.keySet().toArray()));
        //
        int totalQuestions = Integer.parseInt((String) exerciseData.get(kTotalQuestions));
        //
        for (int i = 0; i < totalQuestions; i++)
        {
            String randomScenario = (String) OBUtils.randomlySortedArray(scenarios).get(0);
            List correctAnswer = (List) scenarioCorrectAnswer.get(randomScenario);
            List unitsUsed = new ArrayList<>();
            List uniqueUnits = new ArrayList(new HashSet(unitsUsed));
            //
            OC_DiagnosticsQuestion question = new OC_DiagnosticsQuestion(eventUUID, correctAnswer, Arrays.asList(), uniqueUnits);
            question.additionalInformation.put("scenario", randomScenario);
            result.add(question);
            //
            scenarios.remove(randomScenario);
        }
        return result;
    }


    public void checkObject(OBControl control) throws Exception
    {
        setStatus(STATUS_CHECKING);
        //
        OC_DiagnosticsQuestion currentQuestion = questions.get(currNo);
        toggleTouchedObject(control, true);
        //
        List<String> userAnswer = new ArrayList<>();
        for (OBControl shape : touchables)
        {
            String shapeType = (String) shape.attributes().get("type");
            if (shapeType == null)
            {
                MainActivity.log("OC_Diagnostics_TouchCorrectObject_ShapeIdentification.checkObject --> ERROR: shape type is null for shape [%s]", shape.attributes().get("id"));
            }
            else if ((Boolean) shape.propertyValue("selected"))
            {
                userAnswer.add(shapeType);
            }
        }
        //
        boolean answerIsCorrect = true;
        List<String> correctAnswer = new ArrayList<>();
        correctAnswer.addAll(currentQuestion.correctAnswers);
        //
        while (userAnswer.size() > 0)
        {
            String value = userAnswer.get(0);
            if (!correctAnswer.contains(value))
            {
                answerIsCorrect = false;
                break;
            }
            userAnswer.remove(0);
            correctAnswer.remove(correctAnswer.indexOf(value));
        }
        //
        boolean answerIsPartial = correctAnswer.size() > userAnswer.size();
        if (answerIsCorrect)
        {
            if (!answerIsPartial)
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
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


}
