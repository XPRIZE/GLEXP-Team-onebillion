package org.onebillion.onecourse.mainui.oc_countingpractice;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static org.onebillion.onecourse.mainui.generic.OC_Generic.adjustColour;
import static org.onebillion.onecourse.mainui.generic.OC_Generic.hidePointer;
import static org.onebillion.onecourse.mainui.generic.OC_Generic.movePointerToRestingPosition;

public class OC_CountingPractice_Grid extends OC_SectionController
{
    private int sequenceStart;
    private int sequenceIncrement;
    private OBPath numberBox;
    private List<String> numbers;
    private List<OBPath> numberGrid;
    private List<OBLabel> numberGridLabels;
    private int totalQuestionsPhase2;
    private List<String> numbersForPhase2;
    private int phase2QuestionIndex;
    private int phase2WrongAnswerCount;
    private List<String> numbersForPhase3;
    private List<OBLabel> draggableLabelsForPhase3;
    private int phase3QuestionIndex;
    private int colourTextNormal;
    private int colourTextMovable;
    private int colourTextHilited;
    private int colourNumberBoxNormal;
    private int colourNumberBoxSelected;
    private int colourNumberBoxHilited;
    private int originalStrokeColour;
    private double lastActionTimestamp;
    private double defaultReminderDelay;
    private double pickNumbersReminderDelay;
    private boolean reminderAudioWasPlayed;
    private OBPresenter presenter;
    private List<String> demoEvents;
    private List<String> countingEvents;
    private List<String> pickNumberEvents;
    private List<String> dragNumberEvents;
    private boolean debugModeActive;


    public void prepare()
    {
        super.prepare();
        //
        debugModeActive = false;
        defaultReminderDelay = 5.0;
        pickNumbersReminderDelay = 9.0;
        //
        setStatus(STATUS_BUSY);
        //
        loadFingers();
        loadEvent("master1");
        loadEvent("grid");
        //
        colourTextNormal = objectDict.get("colour_text_normal").fillColor();
        colourTextMovable = objectDict.get("colour_text_movable").fillColor();
        colourTextHilited = objectDict.get("colour_text_hilited").fillColor();
        colourNumberBoxNormal = objectDict.get("colour_number_box_normal").fillColor();
        colourNumberBoxSelected = objectDict.get("colour_number_box_selected").fillColor();
        colourNumberBoxHilited = objectDict.get("colour_number_box_hilited").fillColor();
        //
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restpos", OC_Generic.copyPoint(presenter.control.position()));
            presenter.control.setRight(0);
            presenter.control.show();
        }
        sequenceStart = Integer.parseInt(parameters.get("start"));
        sequenceIncrement = Integer.parseInt(parameters.get("increment"));
        //
        buildGrid(30, 10, sequenceStart, sequenceIncrement);
        //
        originalStrokeColour = numberGrid.get(0).strokeColor();
        //
        String demoString = parameters.get("demo");
        boolean demo = demoString != null && demoString.equals("true");
        //
        List<String> newEvents = new ArrayList<>();
        newEvents.add((demo) ? "intro1" : "intro2");
        newEvents.addAll(Arrays.asList("a,b,c,d,e,f".split(",")));
        //
        events = newEvents;
        //
        demoEvents = Arrays.asList("intro1,intro2".split(","));
        countingEvents = Arrays.asList("a,b,c".split(","));
        pickNumberEvents = Arrays.asList("d".split(","));
        dragNumberEvents = Arrays.asList("e".split(","));
        //
        totalQuestionsPhase2 = 6;
        numbersForPhase2 = OBUtils.randomlySortedArray(numbers).subList(0, totalQuestionsPhase2);
        phase2QuestionIndex = 0;
        phase2WrongAnswerCount = 0;
        //
        List<String> phase3 = new ArrayList<>();
        phase3.addAll(OBUtils.randomlySortedArray(numbers.subList(0, 10)).subList(0, 3));
        phase3.addAll(OBUtils.randomlySortedArray(numbers.subList(10, 20)).subList(0, 3));
        phase3.addAll(OBUtils.randomlySortedArray(numbers.subList(20, 30)).subList(0, 3));
        //
        numbersForPhase3 = OBUtils.randomlySortedArray(phase3);
        phase3QuestionIndex = 0;
        //
        List<OBLabel> labelsForPhase3 = new ArrayList<>();
        for (String number : numbersForPhase3)
        {
            int index = numbers.indexOf(number);
            OBLabel label = numberGridLabels.get(index);
            labelsForPhase3.add(label);
        }
        draggableLabelsForPhase3 = new ArrayList<>(labelsForPhase3);
        doVisual(currentEvent());

    }

    public void start()
    {
        setStatus(STATUS_BUSY);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run()
            {
                try
                {
                    if (!performSel("demo", currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                    MainActivity.log("OC_CountingPractice_Grid --> Exception caught " + exception.toString());
                    exception.printStackTrace();
                }
            }
        });
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                boolean statusWasIdle = true;
                //
                while (!_aborting)
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
                            updateLastActionTimeStamp();
                        }
                        double currentTimeStamp = OC_Generic.currentTime();
                        double idleTime = currentTimeStamp - lastActionTimestamp;
                        double reminderDelay = defaultReminderDelay;
                        if (pickNumberEvents.contains(currentEvent()))
                        {
                            reminderDelay = pickNumbersReminderDelay;
                        }
                        if (idleTime > reminderDelay)
                        {
                            if (countingEvents.contains(currentEvent()))
                            {
                                int index = countingEvents.indexOf(currentEvent());
                                List<OBPath> grid = numberGrid.subList(index * 10, (index * 10) + 10);
                                for (int i = 0; i < 3; i++)
                                {
                                    lockScreen();
                                    for (OBPath cell : grid)
                                    {
                                        cell.setStrokeColor(colourNumberBoxHilited);
                                    }
                                    unlockScreen();
                                    waitForSecs(0.3f);
                                    //
                                    lockScreen();
                                    for (OBPath cell : grid)
                                    {
                                        cell.setStrokeColor(originalStrokeColour);
                                    }
                                    unlockScreen();
                                    waitForSecs(0.3f);
                                }
                            }
                            else if (pickNumberEvents.contains(currentEvent()))
                            {
                                waitAudio();
                                final OBConditionLock audioLock = playAudioQueued(_replayAudio, false);
                                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                {
                                    public void run()
                                    {
                                        waitAudioQueue(audioLock);
                                        updateLastActionTimeStamp();
                                    }
                                });
                            }
                            else if (dragNumberEvents.contains(currentEvent()))
                            {
                                if (!reminderAudioWasPlayed)
                                {
                                    waitAudio();
                                    playAudioQueuedScene("REMINDER", 0.3f, false);
                                    reminderAudioWasPlayed = true;
                                }
                                //
                                for (int i = 0; i < 3; i++)
                                {
                                    lockScreen();
                                    for (OBLabel label : draggableLabelsForPhase3)
                                    {
                                        if (label.isEnabled())
                                        {
                                            label.setColour(colourTextHilited);
                                        }
                                    }
                                    unlockScreen();
                                    waitForSecs(0.2f);
                                    //
                                    lockScreen();
                                    for (OBLabel label : draggableLabelsForPhase3)
                                    {
                                        if (label.isEnabled())
                                        {
                                            label.setColour(colourTextMovable);
                                        }
                                    }
                                    unlockScreen();
                                    waitForSecs(0.2f);
                                }
                            }
                            updateLastActionTimeStamp();
                        }
                    }
                }
                Thread.sleep(1000);
            }
        });
    }

    private void buildGrid(int totalCells, int cellsPerRow, int start, int increment)
    {
        numberBox = (OBPath) objectDict.get("number_box");
        OBLabel numberBoxLabelTemplate = OC_Generic.action_createLabelForControl(numberBox, "0000", 0.9f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        numberGrid = new ArrayList<>();
        numberGridLabels = new ArrayList<>();
        numbers = new ArrayList<>();
        //
        int totalRows = (int) Math.ceil(totalCells / cellsPerRow);
        int numberValue = start;
        //
        for (int row = 0; row < totalRows; row++)
        {
            for (int column = 0; column < cellsPerRow; column++)
            {
                String number = String.format("%d", numberValue);
                numbers.add(number);
                //
                OBPath box = (OBPath) numberBox.copy();
                box.setPosition(new PointF(column * (box.width() - box.lineWidth() * 0.5f), row * (box.height() - box.lineWidth() * 0.5f)));
                box.setProperty("number", number);
                box.setFillColor(colourNumberBoxNormal);
                numberGrid.add(box);
                attachControl(box);
                numberValue += increment;
            }
        }
        //
        OBGroup grid = new OBGroup((List<OBControl>) (Object) numberGrid);
        grid.setPosition(new PointF(bounds().width() * 0.5f, bounds().height() * 0.4f));
        attachControl(grid);
        //
        for (OBPath control : numberGrid)
        {
            grid.removeMember(control);
            attachControl(control);
            control.hide();

        }
        detachControl(grid);
        for (OBControl box : numberGrid)
        {
            OBLabel label = (OBLabel) numberBoxLabelTemplate.copy();
            label.setPosition(OC_Generic.copyPoint(box.position()));
            label.setString((String) box.propertyValue("number"));
            label.setProperty("original_position", OC_Generic.copyPoint(label.position()));
            label.setProperty("box", box);
            //
            box.setProperty("label", label);
            numberGridLabels.add(label);
            //
            attachControl(label);
            label.hide();
        }
        numberBox.hide();
        hideControls("colour.*");
        hideControls("position.*");
        numberBoxLabelTemplate.hide();
    }

    public void setSceneXX(String scene)
    {
        reminderAudioWasPlayed = false;
        if (demoEvents.contains(scene))
        {
            for (OBPath cell : numberGrid)
            {
                cell.hide();
            }
            //
            for (OBLabel label : numberGridLabels)
            {
                label.hide();
            }
            //
            for (int i = 0; i < 10; i++)
            {
                OBPath cell = numberGrid.get(i);
                cell.show();
            }
        }
        else if (countingEvents.contains(scene))
        {
            for (OBPath cell : numberGrid)
            {
                cell.hide();
            }
            //
            for (OBLabel label : numberGridLabels)
            {
                label.hide();
            }
            //
            int exposedCells = (countingEvents.indexOf(scene) + 1) * 10;
            for (int i = 0; i < exposedCells; i++)
            {
                OBPath cell = numberGrid.get(i);
                cell.show();
            }
            //
            int exposedLabels = (countingEvents.indexOf(scene)) * 10;
            for (int i = 0; i < exposedLabels; i++)
            {
                OBLabel label = numberGridLabels.get(i);
                label.show();
            }
        }
        else if (pickNumberEvents.contains(scene))
        {
            for (OBPath cell : numberGrid)
            {
                cell.show();
            }
            for (OBLabel label : numberGridLabels)
            {
                label.show();
            }
        }
        else if (dragNumberEvents.contains(scene))
        {
            for (OBPath cell : numberGrid)
            {
                cell.show();
            }
            for (OBLabel label : numberGridLabels)
            {
                label.show();
            }
        }

    }

    private void highlighGrid(boolean value)
    {
        lockScreen();
        int startCell = (countingEvents.indexOf(currentEvent())) * 10;
        for (int i = startCell; i < startCell + 10; i++)
        {
            OBPath cell = numberGrid.get(i);
            highlightCell(cell, false, value, false);
        }
        unlockScreen();

    }

    private Object findCell(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) numberGrid, pt, true);
        return c;
    }

    private Object findGrid(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) numberGrid, pt, true);
        return c;
    }

    private Object findDraggableLabel(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) draggableLabelsForPhase3, pt, true);
        return c;
    }

    private void checkGrid(OBPath button) throws Exception
    {
        setStatus(STATUS_CHECKING);
        highlighGrid(true);
        waitForSecs(0.3f);
        //
        highlighGrid(false);
        int startCell = countingEvents.indexOf(currentEvent()) * 10;
        for (int i = 0; i < 10; i++)
        {
            OBLabel label = numberGridLabels.get(startCell + i);
            playSfxAudio("numberingrid", false);
            //
            lockScreen();
            label.show();
            unlockScreen();
            //
            if (debugModeActive)
            {
                continue;
            }
            waitForSecs(0.3f);
            //
            String numberAudio = String.format("n_%s", numbers.get(startCell + i));
            playAudio(numberAudio);
            //
            lockScreen();
            label.setColour(colourTextHilited);
            unlockScreen();
            //
            waitAudio();
            //
            lockScreen();
            label.setColour(colourTextNormal);
            unlockScreen();
            waitForSecs(0.3f);
        }
        gotItRightBigTick(true);
        waitForSecs(0.3f);
        //
        nextScene();
    }

    private void checkCellForPhase2(OBPath cell) throws Exception
    {
        setStatus(STATUS_CHECKING);
        //
        OBLabel label = (OBLabel) cell.propertyValue("label");
        String userAnswer = label.text();
        String correctAnswer = numbersForPhase2.get(phase2QuestionIndex);
        //
        if (userAnswer.equals(correctAnswer))
        {
            highlightCell(cell, true, true, false);
            waitForSecs(0.3f);
            //
            gotItRightBigTick(false);
            waitForSecs(0.3f);
            //
            highlightCell(cell, false, false, false);
            phase2QuestionIndex++;
            if (phase2QuestionIndex >= numbersForPhase2.size())
            {
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                nextScene();
            }
            else
            {
                phase2WrongAnswerCount = 0;
                playGridQuestion();
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            highlightCell(cell, false, true, false);
            waitForSecs(0.3f);
            //
            phase2WrongAnswerCount++;
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            highlightCell(cell, false, false, false);
            if (phase2WrongAnswerCount < 2)
            {
                playGridQuestion();
            }
            else
            {
                OBPath correctCell = null;
                for (OBPath numberCell : numberGrid)
                {
                    String cellNumber = (String) numberCell.propertyValue("number");
                    if (cellNumber.equals(correctAnswer))
                    {
                        correctCell = numberCell;
                    }
                }
                String wrongAudio = getAudioForSceneIndex(currentEvent(), "INCORRECT", 0);                   //  It's this one;
                List<Object> audio = OBUtils.insertAudioInterval(Arrays.asList(wrongAudio, String.format("n_%s", numbersForPhase2.get(phase2QuestionIndex))), 300);
                OBConditionLock audioLock = playAudioQueued(audio, false);
                while (audioLock.conditionValue() != PROCESS_DONE)
                {
                    highlightCell(correctCell, true, false, true);
                    waitForSecs(0.2f);
                    //
                    highlightCell(correctCell, false, false, false);
                    waitForSecs(0.2f);
                }
                phase2QuestionIndex++;
                if (phase2QuestionIndex >= numbersForPhase2.size())
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    nextScene();
                    return;
                }
                else
                {
                    wrongAudio = getAudioForSceneIndex(currentEvent(), "INCORRECT", 1);                     // Now find the next one!;
                    audio = OBUtils.insertAudioInterval(Arrays.asList(wrongAudio, String.format("n_%s", numbersForPhase2.get(phase2QuestionIndex))), 300);
                    phase2WrongAnswerCount = 0;
                    playAudioQueued(audio);
                }
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    private void checkDragAtPointForPhase3(PointF pt) throws Exception
    {
        setStatus(STATUS_CHECKING);
        OBLabel label = (OBLabel) target;
        target = null;
        OBPath anyCell = (OBPath) findCell(pt);
        //
        if (anyCell == null)
        {
            PointF originalPosition = OC_Generic.copyPoint((PointF) label.propertyValue("original_position"));
            OBAnim moveAnim = OBAnim.moveAnim(originalPosition, label);
            OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            OBPath nearestCell = (OBPath) findCell(pt);
            String correctAnswer = (String) nearestCell.propertyValue("number");
            String userAnswer = label.text();
            //
            if (nearestCell != null && correctAnswer.equals(userAnswer))
            {
                highlightCell(nearestCell, false, false, true);
                label.moveToPoint(nearestCell.position(), 0.1f, false);
                label.setColour(colourTextNormal);
                label.disable();
                gotItRightBigTick(false);
                waitForSecs(0.3f);
                //
                highlightCell(nearestCell, false, false, false);
                boolean allPlaced = true;
                for (OBLabel draggableLabel : draggableLabelsForPhase3)
                {
                    if (draggableLabel.isEnabled())
                    {
                        allPlaced = false;
                        break;
                    }
                }
                if (allPlaced)
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.7f);
                    //
                    nextScene();
                }
                else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
            else
            {
                gotItWrongWithSfx();
                PointF originalPosition = OC_Generic.copyPoint((PointF) label.propertyValue("original_position"));
                OBAnim moveAnim = OBAnim.moveAnim(originalPosition, label);
                OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            updateLastActionTimeStamp();
            final Object obj = findGrid(pt);
            if (countingEvents.contains(currentEvent()) && obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkGrid((OBPath) obj);
                    }
                });
            }
            else if (pickNumberEvents.contains(currentEvent()))
            {
                final Object cell = findCell(pt);
                if (cell != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkCellForPhase2((OBPath) cell);
                        }
                    });
                }
            }
            else if (dragNumberEvents.contains(currentEvent()))
            {
                final Object label = findDraggableLabel(pt);
                if (label != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run()
                        {
                            checkDragTarget((OBControl) label, pt);
                        }
                    });
                }
            }
        }
    }

    public void touchUpAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkDragAtPointForPhase3(pt);
                }
            });
        }
    }

    private void playGridQuestion()
    {
        String number = String.format("n_%s", numbersForPhase2.get(phase2QuestionIndex));
        playAudio(number);
        List<String> replayAudio = new ArrayList<>();
        replayAudio.addAll(getAudioForScene(currentEvent(), "REPEAT"));
        replayAudio.add(number);
        //
        setReplayAudio(OBUtils.insertAudioInterval(replayAudio, 300));
    }

    public void demointro1() throws Exception
    {
        setStatus(STATUS_BUSY);
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        List<String> audioFiles = getAudioForScene(currentEvent(), "DEMO");
        List<Object> presenterPrompt = new ArrayList<>();
        presenterPrompt.add(audioFiles.get(0));
        presenterPrompt.add(700);
        presenterPrompt.addAll(audioFiles.subList(1, audioFiles.size() - 1));
        presenter.speak(presenterPrompt, 0.3f, this);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        OBControl front = presenter.control.objectDict.get("front");
        PointF destPos = new PointF(bounds().width() - 1.5f * front.width(), currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        audioFiles = new ArrayList<>();
        audioFiles.add(getAudioForSceneIndex(currentEvent(), "DEMO", 2));                   // Ready?;
        presenter.speak((List<Object>) (Object) audioFiles, 0.3f, this);
        currPos = presenter.control.position();
        destPos = new PointF(1.25f * bounds().width() + front.width(), currPos.y);
        presenter.walk(destPos);
        //
        nextScene();
    }

    public void demointro2() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        movePointerToRestingPosition(0.6f, false, this);
        playAudioQueuedScene("DEMO", 0.3f, true);
        //
        nextScene();
    }

    public void demoa() throws Exception
    {
        setStatus(STATUS_BUSY);
        if (thePointer == null)
        {
            loadPointer(POINTER_MIDDLE);
        }
        int eventIndex = countingEvents.indexOf(currentEvent());
        if (eventIndex > 0)
        {
            playSfxAudio("gridon", false);
        }
        playAudioScene("DEMO", 0, false);                                                       //  Touch this once … and count along!;
        PointF destination = new PointF(bounds().width() * 0.5f, bounds().height() * 0.4f);
        destination.y += numberBox.height() * eventIndex;
        movePointerToPoint(destination, 0.6f, false);
        waitAudio();
        waitForSecs(0.3f);
        //
        hidePointer(this);
        //
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demob() throws Exception
    {
        demoa();
    }

    public void democ() throws Exception
    {
        demoa();
    }

    public void demod() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioQueuedScene("DEMO", 0.3f, false);                                           // Next … touch all the numbers you hear.;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        //
        hidePointer(this);
        //
        playGridQuestion();
    }

    public void demoe() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false);                                                   // Now watch;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        List<OBAnim> animations = new ArrayList<>();
        int positionIndex = 1;
        for (OBLabel label : draggableLabelsForPhase3)
        {
            OBControl placement = objectDict.get(String.format("position%d", positionIndex));
            PointF destination = OC_Generic.copyPoint(placement.position());
            Path animationPath = OC_Generic.generateBezierPathForControl(label, destination);
            label.setProperty("original_position", destination);
            //
            OBAnim anim = OBAnim.pathMoveAnim(label, animationPath, false, 0);
            animations.add(anim);
            //
            OBAnim colourAnim = OBAnim.colourAnim("colour", colourTextMovable, label);
            animations.add(colourAnim);
            positionIndex++;
        }
        playSfxAudio("bounce", false);
        OBAnimationGroup.runAnims(animations, 0.8, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        movePointerToPoint(new PointF(bounds().width() * 0.025f, bounds().height() * 0.6f), 0.6f, true);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                                   // You must drag these back into place!;
        movePointerToPoint(new PointF(bounds().width() * 0.975f, bounds().height() * 0.9f), 1.2f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        List<Object> replayAudio = (List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        hidePointer(this);
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demof() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                                   // Good!;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                                   // Let’s hear the numbers again.;
        waitAudio();
        waitForSecs(0.6f);
        //
        playAudioScene("DEMO", 2, false);                                                   // You can say them too!;
        hidePointer(this);
        waitAudio();
        waitForSecs(0.3f);
        //
        for (OBLabel label : numberGridLabels)
        {
            String number = String.format("n_%s", label.text());
            playAudio(number);
            //
            lockScreen();
            label.setColour(colourTextHilited);
            unlockScreen();
            //
            waitAudio();
            //
            lockScreen();
            label.setColour(colourTextNormal);
            unlockScreen();
            //
            waitForSecs(0.3f);
        }
        playSfxAudio("gridflash", false);
        for (int i = 0; i < 3; i++)
        {
            lockScreen();
            for (OBPath cell : numberGrid)
            {
                cell.setStrokeColor(colourNumberBoxHilited);
            }
            unlockScreen();
            waitForSecs(0.3f);
            //
            lockScreen();
            for (OBPath cell : numberGrid)
            {
                cell.setStrokeColor(originalStrokeColour);
            }
            unlockScreen();
            waitForSecs(0.3f);
        }
        playAudioScene("DEMO", 3, true);                                                // Well done! We practised counting.;
        waitForSecs(0.3f);
        //
        nextScene();
    }

    private void updateLastActionTimeStamp()
    {
        lastActionTimestamp = OC_Generic.currentTime();
    }

    private void highlightCell(OBPath cell, boolean value, boolean darkerColour, boolean hilitedColour)
    {
        lockScreen();
        int fillColour = (value) ? colourNumberBoxSelected : colourNumberBoxNormal;
        if (darkerColour)
        {
            fillColour = adjustColour(fillColour, -0.1f);
        }
        else if (hilitedColour)
        {
            fillColour = colourNumberBoxHilited;
        }
        cell.setFillColor(fillColour);
        unlockScreen();
    }

    public void replayAudio()
    {
        updateLastActionTimeStamp();
        super.replayAudio();
    }

}
