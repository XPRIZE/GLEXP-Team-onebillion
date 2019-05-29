package com.maq.xprize.onecourse.hindi.mainui.oc_diagnostics;

import android.graphics.PointF;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBConditionLock;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.OBMainViewController.SHOW_TOP_LEFT_BUTTON;
import static com.maq.xprize.onecourse.hindi.mainui.OBMainViewController.SHOW_TOP_RIGHT_BUTTON;

/**
 * Created by pedroloureiro on 03/05/2018.
 */

public class OC_Diagnostics extends OC_SectionController
{
    public boolean timeoutEnabled;              // flag to enable or disable the timeout that triggers the repeat audio and marking the question as wrong;
    public List questionIcons;
    Map masterObjects;
    Map unitsUsedForParameter;                  // For each parameter used : the exercise we have an array of units where that parameter is used;
    List<OBControl> touchables;                 // created objects that are allowed to interact with user;
    List<OBControl> inertObjects;               // objects that are created as part of the question but do not interact with the user;
    OBControl confirmAnswerButton;              // bottom right button for questions with multiple answers;
    OBGroup smallTick;                          // tick for when the user picks the correct answer;
    OBGroup smallCross;                         // cross for when the user picks an incorrect answer or doesnt pick the correct answer;
    String eventUUID;                           // eventUUID from the Diagnostics Manager;
    List<OC_DiagnosticsQuestion> questions;     // questions generated at the start of the exercise;
    List relevantParametersForRemedialUnits;    // parameters that will be used to further filter the remedial units based on the correct answer and what the user answered;
    OBConditionLock promptAudioLock;            // lock for the doAudio prompt

    double lastActionTimestamp;
    boolean promptWasRepeated;

    @Override
    public int buttonFlags()
    {
        return SHOW_TOP_LEFT_BUTTON | SHOW_TOP_RIGHT_BUTTON;
        //return SHOW_TOP_RIGHT_BUTTON;
    }


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        //
        super.prepare();
        //
        timeoutEnabled = true;
        //
        loadFingers();
        loadEvent("master1");
        //
        masterObjects = new ArrayMap();
        masterObjects.putAll(objectDict);
        //
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenterControl.hide();
        }
        //
        hideControls("background.*");
        //
        confirmAnswerButton = objectDict.get("confirm_answer");
        if (confirmAnswerButton != null)
        {
            confirmAnswerButton.hide();
        }
        //
        smallTick = (OBGroup) objectDict.get("tick");
        if (smallTick != null)
        {
            smallTick.hide();
        }
        //
        smallCross = (OBGroup) objectDict.get("cross");
        if (smallCross != null)
        {
            smallCross.hide();
        }
        //
        unitsUsedForParameter = new ArrayMap<>();
        touchables = new ArrayList<>();
        inertObjects = new ArrayList<>();
        eventUUID = OC_DiagnosticsManager.sharedManager().CurrentEvent();
        //
        if (eventUUID != null)
        {
            events = new ArrayList<>();
            events.add(eventUUID);
        }
        else
        {
            MainActivity.log("OC_Diagnostics --> WARNING: unable to find event in parameters");
        }
        try
        {
            questions = generateQuestionsForExercise(eventUUID);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Diagnostics --> ERROR: exception caught: " + e.toString());
            e.printStackTrace();
        }
    }


    public void start()
    {
        setStatus(STATUS_BUSY);
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if (!performSel("demo", currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception e)
                {
                    MainActivity.log("OC_Diagnostics --> Exception caught %s", e.toString());
                    e.printStackTrace();
                }
            }
        });
    }


    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
        //
        updateLastActionTimeStamp(true);
        //
        if (timeoutEnabled)
        {
            final String currentEventUUID = eventUUID;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    if (promptAudioLock != null)
                    {
                        MainActivity.log("OC_Diagnotics.waiting for promptAudioLock to finish");
                        waitAudioQueue(promptAudioLock);
                        //
                        MainActivity.log("OC_Diagnotics.promptAudioLock finished");
                        updateLastActionTimeStamp(true);
                    }
                    //
                    boolean statusWasIdle = true;
                    //
                    promptWasRepeated = false;
                    while (!_aborting && currentEventUUID.equals(eventUUID))
                    {
                        if (status() != STATUS_AWAITING_CLICK)
                        {
                            statusWasIdle = false;
                        }
                        else
                        {
                            if (!statusWasIdle)
                            {
                                statusWasIdle = true;
                                updateLastActionTimeStamp(true);
                            }
                            double currentTimeStamp = OC_Generic.currentTime();
                            double idleTime = currentTimeStamp - lastActionTimestamp;
                            if (idleTime > OC_DiagnosticsManager.sharedManager().IdleTimeout())
                            {
                                if (promptWasRepeated)
                                {
                                    MainActivity.log("OC_Diagnostics:doMainXX:Timeout occurred, marking as wrong answer");
                                    gotItWrongWithSfx();
                                    OC_DiagnosticsManager.sharedManager().markQuestion(currentEventUUID, false, Arrays.asList());
                                    return;
                                }
                                else
                                {
                                    MainActivity.log("OC_Diagnostics:doMainXX:Timeout occurred, repeating PROMPT");
                                    updateLastActionTimeStamp(false);
                                    //
                                    doAudio(currentEvent());
                                    //
                                    if (promptAudioLock != null)
                                    {
                                        MainActivity.log("OC_Diagnotics.waiting for promptAudioLock to finish");
                                        waitAudioQueue(promptAudioLock);
                                        //
                                        MainActivity.log("OC_Diagnotics.promptAudioLock finished");
                                    }
                                    //
                                    promptWasRepeated = true;
                                    updateLastActionTimeStamp(false);
                                }
                            }
                        }
                        Thread.sleep(250);
                    }
                    MainActivity.log("Prompt loop now killed for event " + currentEventUUID + "/" + eventUUID + " aborting? " + _aborting);
                }
            });
        }
    }


    public void setSceneXX(String scene)
    {
        endScene();
        showQuestionProgress(true);
        //
        loadEvent(OC_DiagnosticsManager.sharedManager().layoutForEvent(eventUUID));
        //
        loadAudioXML(getConfigPath(String.format("event%saudio.xml", eventUUID)));
        //
        hideControls("background.*");
        hideControls("confirm_answer");
        //
        OBControl background = objectDict.get(String.format("background_%s", OC_DiagnosticsManager.sharedManager().backgroundForEvent(eventUUID)));
        ;
        //
        if (background != null)
        {
            background.show();
        }
        //
        buildScene();
    }


    public void buildScene()
    {
    }


    public float bestFontSize()
    {
        return -1;
    }


    public void endScene()
    {
        for (OBControl touchable : touchables)
        {
            detachControl(touchable);
        }
        //
        touchables.clear();
        for (OBControl touchable : inertObjects)
        {
            detachControl(touchable);
        }
        inertObjects.clear();
    }


    public List<OC_DiagnosticsQuestion> generateQuestionsForExercise(String eventUUID) throws Exception
    {
        return null;
    }


    public void showQuestionProgress(boolean showCurrentAnswer)
    {
        float availableHeight = bounds().height() * 0.9f;
        float slottedHeight = availableHeight / OC_DiagnosticsManager.sharedManager().TotalQuestions();
        float startingY = (bounds().height() - availableHeight) / 2;
        int totalQuestions = OC_DiagnosticsManager.sharedManager().TotalQuestions();
        //
        lockScreen();
        if (questionIcons == null)
        {
            questionIcons = new ArrayList<>();
            OBGroup originalIcon = (OBGroup) objectDict.get("question_button");
            originalIcon.hide();
            //
            PointF originalPosition = OC_Generic.copyPoint(originalIcon.position());
            for (int i = 0; i < totalQuestions; i++)
            {
                OBGroup questionIcon = (OBGroup) originalIcon.copy();
                originalPosition.y = startingY + slottedHeight * i - 1 + slottedHeight * 0.5f;
                //
                questionIcon.setPosition(originalPosition);
                questionIcon.setZPosition(originalIcon.zPosition() + 1);
                questionIcon.show();
                //
                questionIcons.add(questionIcon);
                attachControl(questionIcon);
            }
        }
        List progress = OC_DiagnosticsManager.sharedManager().Progress();
        for (int i = 0; i < totalQuestions; i++)
        {
            OBGroup icon = (OBGroup) questionIcons.get(i);
            if (i >= progress.size())
            {
                setQuestionIconInactive(icon);
            }
            else
            {
                setQuestionIconComplete(icon);
            }
        }
        if (showCurrentAnswer && OC_DiagnosticsManager.sharedManager().CurrentQuestion() < questionIcons.size())
        {
            setQuestionIconActive((OBGroup) questionIcons.get(OC_DiagnosticsManager.sharedManager().CurrentQuestion()));
        }
        unlockScreen();
    }


    public void setQuestionIconInactive(OBGroup icon)
    {
        lockScreen();
        icon.hideMembers(".*");
        icon.showMembers("inactive");
        OBPath inactive = (OBPath) icon.objectDict.get("inactive");
        inactive.sizeToBoundingBoxIncludingStroke();
        icon.sizeToMember(inactive);
        unlockScreen();
    }


    public void setQuestionIconActive(OBGroup icon)
    {
        lockScreen();
        icon.hideMembers(".*");
        icon.showMembers("active");
//        icon.outdent(applyGraphicScale(10));
        OBPath active = (OBPath) icon.objectDict.get("active");
        active.sizeToBoundingBoxIncludingStroke();
        icon.sizeToMember(active);
        unlockScreen();
    }


    public void setQuestionIconComplete(OBGroup icon)
    {
        lockScreen();
        icon.hideMembers(".*");
        icon.showMembers("complete");
        OBPath complete = (OBPath) icon.objectDict.get("complete");
        complete.sizeToBoundingBoxIncludingStroke();
        icon.sizeToMember(complete);
        unlockScreen();
    }


    public void showAnswerFeedback(OBControl userAnswer) throws Exception
    {
    }


    public void loadTickAtControl(OBControl control)
    {
        OBGroup newTick = (OBGroup) smallTick.copy();
        attachControl(newTick);
        newTick.show();
        newTick.setTop(control.position().y + control.height() * 0.25f);
        newTick.setLeft(control.right() + newTick.width() * 0.25f);
        newTick.setZPosition(10);
        newTick.outdent(applyGraphicScale(10));
        inertObjects.add(newTick);
    }


    public void loadCrossAtControl(OBControl control)
    {
        OBGroup newCross = (OBGroup) smallCross.copy();
        attachControl(newCross);
        newCross.show();
        newCross.setTop(control.position().y + control.height() * 0.25f);
        newCross.setLeft(control.right() + newCross.width() * 0.25f);
        newCross.setZPosition(10);
        newCross.outdent(applyGraphicScale(10));
        inertObjects.add(newCross);
    }


    public List generateUniqueSet(List sourceSet, int setLength, List<List> usedSets)
    {
        List randomParameters, selectedParameters;
        boolean alreadyUsed;
        do
        {
            randomParameters = OBUtils.randomlySortedArray(sourceSet);
            selectedParameters = randomParameters.subList(0, Math.min(randomParameters.size(), setLength));
            alreadyUsed = false;
            for (List<String> usedPair : usedSets)
            {
                List array1 = new ArrayList();
                array1.addAll(usedPair);
                Arrays.sort(array1.toArray());
                //
                List array2 = new ArrayList();
                array2.addAll(selectedParameters);
                Arrays.sort(array2.toArray());
                //
                if (Arrays.equals(array1.toArray(), array2.toArray()))
                {
                    alreadyUsed = true;
                    break;
                }
            }
        }
        while (alreadyUsed);
        //
        usedSets.add(selectedParameters);
        //
        return selectedParameters;
    }


    public List generateUniqueSet(List sourceSet1, List sourceSet2, List<List> usedSets)
    {
        List randomParameters1, randomParameters2, selectedParameters;
        boolean alreadyUsed;
        //
        do
        {
            randomParameters1 = OBUtils.randomlySortedArray(sourceSet1);
            randomParameters2 = OBUtils.randomlySortedArray(sourceSet2);
            selectedParameters = Arrays.asList(randomParameters1.get(0), randomParameters2.get(0));
            alreadyUsed = false;
            //
            for (List<List> usedPair : usedSets)
            {
                List array1 = new ArrayList();
                array1.addAll(usedPair);
                Arrays.sort(array1.toArray());
                //
                List array2 = new ArrayList();
                array2.addAll(selectedParameters);
                Arrays.sort(array2.toArray());
                //
                if (Arrays.equals(array1.toArray(), array2.toArray()))
                {
                    alreadyUsed = true;
                    break;
                }
            }
        }
        while (alreadyUsed);
        //
        usedSets.add(selectedParameters);
        return selectedParameters;
    }


    public void updateLastActionTimeStamp(boolean resetPrompt)
    {
        lastActionTimestamp = OC_Generic.currentTime();
        if (resetPrompt)
        {
            promptWasRepeated = false;
        }
    }

    public void replayAudio()
    {
        updateLastActionTimeStamp(true);
        super.replayAudio();
    }


    public static double evalExpression(final String str)
    {
        return new Object()
        {
            int pos = -1, ch;

            void nextChar()
            {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat)
            {
                while (ch == ' ')
                {
                    nextChar();
                }
                if (ch == charToEat)
                {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse()
            {
                nextChar();
                double x = parseExpression();
                if (pos < str.length())
                {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression()
            {
                double x = parseTerm();
                for (; ; )
                {
                    if (eat('+'))
                    {
                        x += parseTerm(); // addition
                    }
                    else if (eat('-'))
                    {
                        x -= parseTerm(); // subtraction
                    }
                    else
                    {
                        return x;
                    }
                }
            }

            double parseTerm()
            {
                double x = parseFactor();
                for (; ; )
                {
                    if (eat('*'))
                    {
                        x *= parseFactor(); // multiplication
                    }
                    else if (eat('/'))
                    {
                        x /= parseFactor(); // division
                    }
                    else
                    {
                        return x;
                    }
                }
            }

            double parseFactor()
            {
                if (eat('+'))
                {
                    return parseFactor(); // unary plus
                }
                if (eat('-'))
                {
                    return -parseFactor(); // unary minus
                }

                double x;
                int startPos = this.pos;
                if (eat('('))
                { // parentheses
                    x = parseExpression();
                    eat(')');
                }
                else if ((ch >= '0' && ch <= '9') || ch == '.')
                { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.')
                    {
                        nextChar();
                    }
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                }
                else if (ch >= 'a' && ch <= 'z')
                { // functions
                    while (ch >= 'a' && ch <= 'z')
                    {
                        nextChar();
                    }
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt"))
                    {
                        x = Math.sqrt(x);
                    }
                    else if (func.equals("sin"))
                    {
                        x = Math.sin(Math.toRadians(x));
                    }
                    else if (func.equals("cos"))
                    {
                        x = Math.cos(Math.toRadians(x));
                    }
                    else if (func.equals("tan"))
                    {
                        x = Math.tan(Math.toRadians(x));
                    }
                    else
                    {
                        throw new RuntimeException("Unknown function: " + func);
                    }
                }
                else
                {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^'))
                {
                    x = Math.pow(x, parseFactor()); // exponentiation
                }

                return x;
            }
        }.parse();
    }

}



/*



 */
