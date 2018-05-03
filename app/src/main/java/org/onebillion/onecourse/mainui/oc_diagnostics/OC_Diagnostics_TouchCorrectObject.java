package org.onebillion.onecourse.mainui.oc_diagnostics;

/**
 * Created by pedroloureiro on 03/05/2018.
 */

public class OC_Diagnostics_TouchCorrectObject extends OC_Diagnostics
{
}

/*

public void prepare()
{
    super.prepare();
    doVisual(currentEvent);

}
public List<>  generateQuestionsForExercise(String  eventUUID)
{
    List<> result = new ArrayList<>();
    Map<> *exerciseData = OC_DiagnosticsManager.sharedManager.parametersForEvent(eventUUID);
    Map<> *availableParameters = OC_DiagnosticsManager.sharedManager.unitsPerParameterForEvent(eventUUID);
    List<> allParameters = availableParameters.keySet();
    int totalQuestions = (int)exerciseData.objectForKey(kTotalQuestions) ;
    int possibleAnswerCount = (int)exerciseData.objectForKey(kTotalAvailableOptions) ;
    int maxLength = (int)exerciseData.objectForKey(kParameterMaxWordLength) ;
    if (allParameters.size() < possibleAnswerCount)
{
        MainActivity.log("OC_Diagnostics_TouchCorrectObject:generateQuestionsForExercise --> ERROR: not enough parameters to run this exercise %s.()", eventUUID);
        return null;

}
    List<> pickedAnswers = new ArrayList<>();
    for (int i = 0;
 i < totalQuestions;
 i++)
{
        List<> randomParameters = OBUtils.randomlySortedArray(allParameters);
        List<> selectedParameters = new ArrayList<>();
        for (String phonemeUUID : randomParameters)
{
            OBPhoneme phoneme = OC_DiagnosticsManager.sharedManager.WordComponents.objectForKey(phonemeUUID);
            int length = (int) phoneme.text.length;
            List<> characterStatistics = new ArrayList<>();
            [phoneme.text enumerateSubstringsInRange:NSMakeRange(0, phoneme.text.length) options:StringEnumerationByComposedCharacterSequences usingBlock:^(String substring, NSRange substringRange, NSRange enclosingRange, boolean *stop)
{
                characterStatistics.add(substring);

}
];
            NSCountedSet *set = [NSCountedSet.alloc() initWithArray:characterStatistics];
            length += set.countForObject("m");
            length += set.countForObject("w");
            if (maxLength > 0 && length > maxLength) continue;
            selectedParameters.add(phonemeUUID);
            if (selectedParameters.size() == possibleAnswerCount)
{
                break;

}

}
        List<> unitsUsed = new ArrayList<>();
        for (String parameter : selectedParameters)
{
            [unitsUsed addAll:availableParameters.objectForKey(parameter)];

}
        String correctAnswer;
        do
{
            correctAnswer = OBUtils.randomlySortedArray(selectedParameters).firstObject;

}
        while(pickedAnswers.containsObject(correctAnswer));
        pickedAnswers.add(correctAnswer);
        List<> distractors = OBUtils.randomlySortedArray(selectedParameters);
        List<> uniqueUnits = unitsUsed.valueForKeyPath("@distinctUnionOfObjects.state");
        OC_DiagnosticsQuestion *question = [OC_DiagnosticsQuestion.alloc() initWithEvent:eventUUID withCorrectAnswers:Arrays.asList(correctAnswer) distractors:distractors andUnitsUsed:uniqueUnits];
        result.add(question);

}
    return result;

}
public void doAudio(String scene)
{
    OC_DiagnosticsQuestion *currentQuestion = questions.objectAtIndex(currNo);
    OBPhoneme phoneme = OC_DiagnosticsManager.sharedManager.WordComponents.objectForKey(currentQuestion.correctAnswers.firstObject);
    List<> replayAudio = [(List<Object>) (Object) getAudioForScene(scene,"PROMPT") mutableCopy];
    if (replayAudio == null) replayAudio = new ArrayList<>();
    replayAudio.add(phoneme.audio);
    setReplayAudio(replayAudio);
    MainActivity.log("Correct answer is %s.()", phoneme.text);
    playAudioQueuedScene("PROMPT",300,true);
    [phoneme playAudio:wait:false];

}
public void buildScene()
{
    int totalParameters = (int) filterControls("label.*").size();
    OC_DiagnosticsQuestion *currentQuestion = questions.objectAtIndex(currNo);
    float fontSize = bestFontSize();
    MainActivity.log("OC_Diagnostics_TouchCorrectObject --> using font size %f.()", fontSize);
    for (int i = 0;
 i < totalParameters;
 i++)
{
        OBControl labelBox = [objectDict objectForKey:String.format("label%d", i + 1)];
        String phonemeUUID = currentQuestion.distractors.objectAtIndex(i);
        OBPhoneme phoneme = OC_DiagnosticsManager.sharedManager.WordComponents.objectForKey(phonemeUUID);
        if (phoneme == null)
{
            MainActivity.log("OC_Diagnostics_TouchCorrectObject --> ERROR: unable to find phoneme with UUID %s.()", phonemeUUID);
            return;

}
        OBLabel phonemeLabel = createLabel_simple(labelBox text:phoneme.text colour:Color.blackColor withFinalResizeFactor:1.0 andFont:StandardFontOfSize(fontSize) allowFontResize:false);
        phonemeLabel.setProperty("phoneme",phoneme);
        touchables.add(phonemeLabel);
        attachControl(phonemeLabel);
        detachControl(labelBox);

}

}
public float bestFontSize()
{
    float fontSize = -1;
    OBControl labelBox = objectDict.objectForKey("label1");
    for (OC_DiagnosticsQuestion *question : questions)
{
        for (String distractor : question.distractors)
{
            OBPhoneme phoneme = OC_DiagnosticsManager.sharedManager.WordComponents.objectForKey(distractor);
            OBLabel phonemeLabel = createLabel_simple(labelBox text:phoneme.text colour:Color.blackColor);
            fontSize = (fontSize == -1 || fontSize > phonemeLabel.font.pointSize) ? phonemeLabel.font.pointSize : fontSize;

}

}
    return fontSize;

}
public void toggleTouchedObject(OBControl control, boolean value)
{
    OBLabel label = (OBLabel )control;
    label.setColour ( value ? Color.redColor : Color.blackColor);

}
public boolean isAnswerCorrect(OBControl  label, boolean saveInformation)
{
    OC_DiagnosticsQuestion *currentQuestion = questions.objectAtIndex(currNo);
    OBPhoneme userAnswer = label.propertyValue("phoneme");
    if (saveInformation)
{
        setRelevantParametersForRemedialUnits ( currentQuestion.correctAnswers.arrayByAddingObject(userAnswer.soundid));

}
    return currentQuestion.correctAnswers.containsObject(userAnswer.soundid);

}
public void showAnswerFeedback(OBControl  userAnswer)
{
    boolean answerCorrect = isAnswerCorrect(userAnswer saveRelevantInformation:false);
    lockScreen();
        if (answerCorrect)
{
            loadTickAtControl(userAnswer);

}
        else
{
            loadCrossAtControl(userAnswer);

}

unlockScreen();
    waitForSecs(1.2f);

}
public void checkObject(OBLabel  control)
{
    setStatus(STATUS_CHECKING);
    toggleTouchedObject(control withHighlight:true);
    if (isAnswerCorrect(control saveRelevantInformation:true))
{
        gotItRightBigTick(false);
        waitForSecs(0.3f);
        toggleTouchedObject(control withHighlight:false);
        showAnswerFeedback(control);
        currNo++;
        if (currNo >= questions.size())
{
            [OC_DiagnosticsManager.sharedManager markQuestion:eventUUID withValue:true andRelevantParameters:Arrays.asList()];
            return;

}
        else
{
            lockScreen();
                endScene();
                buildScene();

unlockScreen();
            doAudio(currentEvent);

}

}
    else
{
        gotItWrongWithSfx();
        waitForSecs(0.3f);
        toggleTouchedObject(control withHighlight:false);
        showAnswerFeedback(control);
        [OC_DiagnosticsManager.sharedManager markQuestion:eventUUID withValue:false andRelevantParameters:relevantParametersForRemedialUnits];
        return;

}
    setStatus(STATUS_AWAITING_CLICK);

}
public Object findObject(PointF pt)
{
    OBControl c = finger(0,2,touchables,pt, true);
    return c;

}
public void checkConfirmAnswerButton(OBLabel  control)
{
    setStatus(STATUS_CHECKING);
    if (isAnswerCorrect(control saveRelevantInformation:true))
{
        gotItRightBigTick(false);
        waitForSecs(0.3f);
        showAnswerFeedback(control);
        waitForSecs(0.6f);
        currNo++;
        if (currNo >= questions.size())
{
            [OC_DiagnosticsManager.sharedManager markQuestion:eventUUID withValue:true andRelevantParameters:Arrays.asList()];

}
        else
{
            lockScreen();
                endScene();
                buildScene();

unlockScreen();
            doAudio(currentEvent);

}

}
    else
{
        gotItWrongWithSfx();
        waitForSecs(0.3f);
        showAnswerFeedback(control);
        waitForSecs(0.6f);
        [OC_DiagnosticsManager.sharedManager markQuestion:eventUUID withValue:false andRelevantParameters:relevantParametersForRemedialUnits];

}
    setStatus(STATUS_AWAITING_CLICK);

}
public Object findConfirmAnswerButton(PointF pt)
{
    OBControl c = finger(0,2,Arrays.asList(confirmAnswerButton),pt, true);
    return c;

}
public void touchDownAtPoint(PointF pt, View  v)
{
    if (status() == STATUS_AWAITING_CLICK)
{
        updateLastActionTimeStamp(true);
        Object obj = findObject(pt);
        if (obj != null)
{
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
{
public void run() throws Exception
{
                checkObject(obj);

}
});

}
        else
{
            Object button = findConfirmAnswerButton(pt);
            if (button != null)
{
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
{
public void run() throws Exception
{
                    checkConfirmAnswerButton(button);

}
});

}

}

}

}

 */