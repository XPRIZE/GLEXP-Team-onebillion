package org.onebillion.onecourse.mainui.oc_countingto1000;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.onebillion.onecourse.mainui.generic.OC_Generic.generateBezierPathForControl;
import static org.onebillion.onecourse.mainui.generic.OC_Generic.hidePointer;
import static org.onebillion.onecourse.mainui.generic.OC_Generic.movePointerToRestingPosition;

public class OC_CountingTo1000_Revision extends OC_CountingTo1000
{
    public List eventsForDraggingNumbers;


    public void buildNumbers()
    {
        super.buildNumbers();
    }

    public void buildEvents()
    {
        String demoString = parameters.get("demo");
        boolean demo = demoString != null && demoString.equals("true");
        List newEvents = new ArrayList();
        newEvents.add((demo) ? "intro1" : "intro2");
        newEvents.addAll(Arrays.asList("a,b,c".split(",")));
        eventsForHilitedCell = Arrays.asList("a".split(","));
        eventsForDraggingNumbers = Arrays.asList("b".split(","));
        events = newEvents;
    }

    public void start()
    {
        setStatus(STATUS_BUSY);
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
                catch (Exception exception)
                {
                    MainActivity.log("OC_CountingTo1000 --> Exception caught " + exception.toString());
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
                        if (idleTime > reminderDelay)
                        {
                            List flashCellEvents = Arrays.asList("a,b".split(","));
                            if (flashCellEvents.contains(currentEvent()))
                            {
                                for (int i = 0; i < 3; i++)
                                {
                                    OBPath cell = numberGrid.get(hilitedCellIndex);
                                    lockScreen();
                                    highlightCell(cell, true, false, true);
                                    unlockScreen();
                                    waitForSecs(0.2f);
                                    //
                                    lockScreen();
                                    highlightCell(cell, true, false, false);
                                    unlockScreen();
                                    waitForSecs(0.2f);
                                }
                            }
                            updateLastActionTimeStamp();
                        }
                    }
                    Thread.sleep(1000);
                }
            }
        });
    }

    public void setSceneXX(String scene)
    {
        List firstPhase = Arrays.asList("intro1,intro2".split(","));
        List doNothingEvents = Arrays.asList("a".split(","));
        List secondPhase = Arrays.asList("b,c".split(","));
        titleBox.hide();
        titleBoxLabel.hide();
        button1.hide();
        button1Label.hide();
        button10.hide();
        button10Label.hide();
        button100.hide();
        button100Label.hide();
        setFirstHilitedCell();
        if (doNothingEvents.contains(scene))
        {
            // do nothing
        }
        else if (firstPhase.contains(scene))
        {
            for (OBPath cell : numberGrid)
            {
                cell.show();
            }
        }
        else if (secondPhase.contains(scene))
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

    public void setFirstHilitedCell()
    {
        hilitedCellIndex = 0;
    }

    public void nextHilitedCell()
    {
        hilitedCellIndex++;
    }

    public boolean isFirstHilitedCell()
    {
        return hilitedCellIndex == 0;
    }

    public boolean isCompleteHilitingCells()
    {
        return hilitedCellIndex >= 10;
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBLabel label = (OBLabel) target;
            target = null;
            OBPath anyCell = (OBPath) findCell(pt);
            if (anyCell == null)
            {
                PointF originalPosition = OC_Generic.copyPoint((PointF) label.propertyValue("original_position"));
                OBAnim moveAnim = OBAnim.moveAnim(originalPosition, label);
                OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                setStatus(STATUS_AWAITING_CLICK);
            }
            else
            {
                OBPath hilitedCell = (OBPath) findHilitedCell(pt);
                //
                if (hilitedCell != null)
                {
                    String correctAnswer = (String) hilitedCell.propertyValue("number");
                    String userAnswer = label.text();
                    if (correctAnswer.equals(userAnswer))
                    {
                        highlightCell(hilitedCell, false, false, true);
                        label.moveToPoint(hilitedCell.position(), 0.1f, false);
                        gotItRightBigTick(false);
                        waitForSecs(0.3f);
                        //
                        nextHilitedCell();
                        if (isCompleteHilitingCells())
                        {
                            hiliteCurrentCell();
                            gotItRightBigTick(true);
                            waitForSecs(0.3f);
                            //
                            nextScene();
                        }
                        else
                        {
                            thirdPhaseWrongAnswerCount = 0;
                            playSfxAudio("fill", false);
                            hiliteCurrentCell();
                            setStatus(STATUS_AWAITING_CLICK);
                        }
                    }
                    else
                    {
                        gotItWrongWithSfx();
                        thirdPhaseWrongAnswerCount++;
                        PointF originalPosition = OC_Generic.copyPoint((PointF) label.propertyValue("original_position"));
                        OBAnim moveAnim = OBAnim.moveAnim(originalPosition, label);
                        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                        setStatus(STATUS_AWAITING_CLICK);
                        if (thirdPhaseWrongAnswerCount > 2)
                        {
                            OBLabel correctLabel = (OBLabel) hilitedCell.propertyValue("label");
                            for (int i = 0; i < 3; i++)
                            {
                                lockScreen();
                                correctLabel.setColour(colourNumberBoxHilited);
                                unlockScreen();
                                waitForSecs(0.3f);
                                //
                                lockScreen();
                                correctLabel.setColour(colourTextNormal);
                                unlockScreen();
                                waitForSecs(0.3f);
                            }
                        }
                    }
                }
                else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
        }
        catch (Exception e)
        {
            MainActivity.log("Exception caught: " + e.toString());
            e.printStackTrace();
        }
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            updateLastActionTimeStamp();
            if (eventsForDraggingNumbers.contains(currentEvent()) && numberGridLabels != null)
            {
                final Object label = findLabel(pt);
                if (label != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkDragTarget((OBControl) label, pt);
                        }
                    });
                }
            }
            else
            {
                super.touchDownAtPoint(pt, v);
            }
        }
    }


    public void demointro1() throws Exception
    {
        setStatus(STATUS_BUSY);
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        List<String> audioFiles = getAudioForScene(currentEvent(), "DEMO");
        String presenterAudio = audioFiles.get(0);                          // Let’s practise counting in hundreds, to one thousand;
        presenter.speak((List<Object>) (Object) presenterAudio, 0.3f, this);
        waitForSecs(0.3f);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        OBControl front = presenter.control.objectDict.get("front");
        PointF destPos = new PointF(bounds().width() - 1.5f * front.width(), currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        presenterAudio = audioFiles.get(1);                                 // Are you ready?;
        presenter.speak((List<Object>) (Object) presenterAudio, 0.3f, this);
        waitForSecs(0.3f);
        //
        currPos = presenter.control.position();
        destPos = new PointF(1.25f * bounds().width() + front.width(), currPos.y);
        presenter.walk(destPos);
        nextScene();
    }

    public void demointro2() throws Exception
    {
        setStatus(STATUS_BUSY);
        //
        loadPointer(POINTER_MIDDLE);
        //
        movePointerToRestingPosition(0.6f, false, this);
        playAudioQueuedScene("DEMO", 0.3f, true);                            // Let’s practise counting in hundreds, to one thousand.;
        waitForSecs(0.7f);
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
        playSfxAudio("fill", false);
        hiliteCurrentCell();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 0, false);                                   // Touch the yellow box.;
        OBPath firstBox = numberGrid.get(hilitedCellIndex);
        PointF destination = firstBox.bottomPoint();
        destination.y += firstBox.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        hidePointer(this);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demob() throws Exception
    {
        setStatus(STATUS_BUSY);
        List randomLabels = OBUtils.randomlySortedArray(numberGridLabels);
        List animations = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
        {
            OBLabel label = (OBLabel) randomLabels.get(i - 1);
            OBControl placement = objectDict.get(String.format("position%d", i));
            PointF destination = OC_Generic.copyPoint(placement.position());
            Path animationPath = generateBezierPathForControl(label, destination);
            label.setProperty("original_position", destination);
            OBAnim anim = OBAnim.pathMoveAnim(label, animationPath, false, 0);
            animations.add(anim);
        }
        playSfxAudio("bounce", false);
        OBAnimationGroup.runAnims(animations, 0.8, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                   // Now put the numbers into place again … : the right order!;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        setFirstHilitedCell();
        playSfxAudio("fill", false);
        hiliteCurrentCell();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                   // Drag to the yellow box;
        OBPath firstBox = numberGrid.get(hilitedCellIndex);
        PointF destination = firstBox.bottomPoint();
        destination.y += firstBox.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        hidePointer(this);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void democ() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                                   // Good!;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                                   // Let’s hear the numbers again.;
        waitAudio();
        waitForSecs(0.6f);
        //
        playAudioScene("DEMO", 2, false);                                   // You can say them too!;
        waitAudio();
        hidePointer(this);
        waitForSecs(0.6f);
        //
        int originalStrokeColour = -1;
        setFirstHilitedCell();
        while (!isCompleteHilitingCells())
        {
            OBPath cell = numberGrid.get(hilitedCellIndex);
            OBLabel label = (OBLabel) cell.propertyValue("label");
            String number = String.format("n_%s", cell.propertyValue("number"));
            if (originalStrokeColour == -1)
            {
                originalStrokeColour = cell.strokeColor();
            }
            label.setColour(colourTextHilited);
            playAudio(number);
            waitAudio();
            waitForSecs(0.3f);
            //
            label.setColour(colourTextNormal);
            nextHilitedCell();
        }
        playSfxAudio("flash", false);
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
        playAudioScene("DEMO", 3, true);                                    // Great! We counted to one thousand … : hundreds.;
        waitForSecs(0.3f);
        //
        nextScene();
    }

}
