package com.maq.xprize.onecourse.hindi.mainui.oc_countingto1000;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.controls.OBPresenter;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBConditionLock;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic.darkerColor;

public class OC_CountingTo1000 extends OC_SectionController
{
    public OBPresenter presenter;
    public int colourButtonDisabledFill;
    public int colourButtonDisabledStroke;
    public int colourTextNormal;
    public int colourTextButtonNormal;
    public int colourTextHilited;
    public int colourNumberBoxHilited;
    public int colourNumberBoxNormal;
    public int colourNumberBoxSelected;
    public OBPath button1;
    public OBLabel button1Label;
    public OBPath block1Template;
    public OBGroup block1;
    public OBPath button10;
    public OBLabel button10Label;
    public OBPath block10Template;
    public OBGroup block10;
    public OBPath button100;
    public OBLabel button100Label;
    public OBPath block100Template;
    public OBGroup block100;
    public OBPath titleBox;
    public OBLabel titleBoxLabel;
    public OBPath numberBox;
    public List<OBPath> numberGrid;
    public List<OBLabel> numberGridLabels;
    public List<String> numbers;
    public List<String> questionsForThirdPhase;
    public int questionIndexForThirdPhase;
    public int thirdPhaseWrongAnswerCount;
    public int hilitedCellIndex;
    public int blocksAddedPerButtonPress;
    public int totalBlocksInScene;
    public List<OBControl> addedSetsOfBlocks;
    public double lastActionTimestamp;
    public double reminderDelay;
    public List<String> eventsForHilitedCell;
    public List<String> eventsForThirdPhase;


    public void prepare()
    {
        super.prepare();
        setStatus(STATUS_BUSY);
        buildNumbers();
        buildScene();
        buildEvents();
        doVisual(this.currentEvent());

    }

    public void buildNumbers()
    {
        numbers = new ArrayList();
        for (int i = 0; i < 10; i++)
        {
            String number = String.format("%d", (i + 1) * 100);
            numbers.add(number);

        }

    }

    public void buildEvents()
    {

    }


    public void buildScene()
    {
        loadFingers();
        loadEvent("master1");
        loadEvent("blocks");
        reminderDelay = 5.0;
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restpos", OC_Generic.copyPoint(presenter.control.position()));
            presenter.control.setRight(0);
            presenter.control.show();
        }
        //
        colourButtonDisabledFill = objectDict.get("colour_button_disabled").fillColor();
        colourButtonDisabledStroke = objectDict.get("colour_button_disabled").strokeColor();
        colourTextButtonNormal = objectDict.get("colour_text_button_normal").fillColor();
        colourTextNormal = objectDict.get("colour_text_normal").fillColor();
        colourTextHilited = objectDict.get("colour_text_hilited").fillColor();
        colourNumberBoxHilited = objectDict.get("colour_number_box_hilited").fillColor();
        colourNumberBoxNormal = objectDict.get("colour_number_box_normal").fillColor();
        colourNumberBoxSelected = objectDict.get("colour_number_box_selected").fillColor();
        //
        titleBox = (OBPath) objectDict.get("title_box");
        titleBox.sizeToBoundingBoxIncludingStroke();
        //
        titleBoxLabel = OC_Generic.action_createLabelForControl(titleBox, "0000", 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        titleBoxLabel.setZPosition(titleBox.zPosition() + 0.1f);
        attachControl(titleBoxLabel);
        //
        button1 = (OBPath) objectDict.get("button_1");
        button1.setPosition(new PointF(bounds().width() * 0.5f, bounds().height() * 0.9f));
        button1.setProperty("blocks_added", 1);
        button1.setProperty("original_fill", button1.fillColor());
        button1.setProperty("original_stroke", button1.strokeColor());
        //
        button1Label = OC_Generic.action_createLabelForControl(button1, "1", 0.6f, false, OBUtils.standardTypeFace(), colourTextButtonNormal, this);
        button1Label.setZPosition(button1.zPosition() + 0.1f);
        attachControl(button1Label);
        button1Label.hide();
        button1.hide();
        button1.sizeToBoundingBoxIncludingStroke();
        //
        button10 = (OBPath) objectDict.get("button_10");
        button10.setPosition(new PointF(bounds().width() * 0.5f, bounds().height() * 0.9f));
        button10.setProperty("blocks_added", 10);
        button10.setProperty("original_fill", button10.fillColor());
        button10.setProperty("original_stroke", button10.strokeColor());
        //
        button10Label = OC_Generic.action_createLabelForControl(button10, "10", 0.6f, false, OBUtils.standardTypeFace(), colourTextButtonNormal, this);
        button10Label.setZPosition(button10.zPosition() + 0.1f);
        attachControl(button10Label);
        button10Label.hide();
        button10.hide();
        button10.sizeToBoundingBoxIncludingStroke();
        //
        button100 = (OBPath) objectDict.get("button_100");
        button100.setPosition(new PointF(bounds().width() * 0.5f, bounds().height() * 0.9f));
        button100.setProperty("blocks_added", 100);
        button100.setProperty("original_fill", button100.fillColor());
        button100.setProperty("original_stroke", button100.strokeColor());
        //
        button100Label = OC_Generic.action_createLabelForControl(button100, "100", 0.6f, false, OBUtils.standardTypeFace(), colourTextButtonNormal, this);
        button100Label.setZPosition(button100.zPosition() + 0.1f);
        attachControl(button100Label);
        button100Label.hide();
        button100.hide();
        button100.sizeToBoundingBoxIncludingStroke();
        //
        block1Template = (OBPath) objectDict.get("block_1");
        block10Template = (OBPath) objectDict.get("block_10");
        block100Template = (OBPath) objectDict.get("block_100");
        OBPath newBlock1 = (OBPath) block1Template.copy();
        //
        block1 = new OBGroup(Arrays.asList((OBControl) newBlock1));
        block1.shouldTexturise = true;
        //
        List blocks = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            OBPath newBlock = (OBPath) block10Template.copy();
            newBlock.setPosition(new PointF(0f, i * newBlock.height() - newBlock.lineWidth() * 0.5f));
            blocks.add(newBlock);
        }
        //
        block10 = new OBGroup(blocks);
        block10.outdent(block10Template.lineWidth());
        block10.shouldTexturise = true;
        //
        blocks = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < 10; j++)
            {
                OBPath newBlock = (OBPath) block100Template.copy();
                newBlock.setPosition(new PointF(j * newBlock.width() - newBlock.lineWidth() * 0.5f, i * newBlock.height() - newBlock.lineWidth() * 0.5f));
                blocks.add(newBlock);
            }
        }
        block100 = new OBGroup(blocks);
        block100.outdent(block100Template.lineWidth());
        block100.shouldTexturise = true;
        //
        block1Template.hide();
        block10Template.hide();
        block100Template.hide();
        //
        numberBox = (OBPath) objectDict.get("number_box");
        OBLabel numberBoxLabelTemplate = OC_Generic.action_createLabelForControl(numberBox, "0000", 0.9f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        numberGrid = new ArrayList<>();
        numberGridLabels = new ArrayList<>();
        //
        for (int i = 0; i < 10; i++)
        {
            String number = numbers.get(i);
            OBPath box = (OBPath) numberBox.copy();
            box.setPosition(new PointF(i * box.width() - box.lineWidth() * 0.5f, 0f));
            box.setProperty("number", number);
            box.setFillColor(colourNumberBoxNormal);
            box.sizeToBoundingBoxIncludingStroke();
            numberGrid.add(box);
            attachControl(box);
        }
        //
        questionsForThirdPhase = OBUtils.randomlySortedArray(numbers).subList(0, 5);
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
        //
        for (OBControl box : numberGrid)
        {
            OBLabel label = (OBLabel) numberBoxLabelTemplate.copy();
            label.setPosition(box.position());
            label.setString((String) box.propertyValue("number"));
            label.setProperty("original_position", OC_Generic.copyPoint(label.position()));
            label.setProperty("box", box);
            box.setProperty("label", label);
            numberGridLabels.add(label);
            attachControl(label);
            label.hide();
        }
        //
        numberBox.hide();
        hideControls("colour.*");
        hideControls("position.*");
        //
        numberBoxLabelTemplate.hide();
    }


    public void setSceneXX(String scene)
    {
        List firstPhase = Arrays.asList("intro1,intro2,a".split(","));
        List doNothingEvents = Arrays.asList("b,c,d".split(","));
        List secondPhase = Arrays.asList("e".split(","));
        List thirdPhase = Arrays.asList("f,g,h".split(","));
        if (doNothingEvents.contains(scene))
        {
            // do nothing
        }
        else if (firstPhase.contains(scene))
        {
            MainActivity.log("First Phase - build blocks");
            totalBlocksInScene = 0;
            switchToButton(10);
            try
            {
                showBlocks(0, 0, false);
            }
            catch (Exception e)
            {
                MainActivity.log("Exception caught: " + e.toString());
                e.printStackTrace();
            }
            toggleButton(false);
        }
        else if (secondPhase.contains(scene))
        {
            MainActivity.log("Second Phase - build 1x10 Grid");
            titleBox.hide();
            titleBoxLabel.hide();
            button1.hide();
            button1Label.hide();
            button10.hide();
            button10Label.hide();
            button100.hide();
            button100Label.hide();
            setFirstHilitedCell();
        }
        else if (thirdPhase.contains(scene))
        {
            MainActivity.log("Third Phase - build 1x10 Grid");
            titleBox.hide();
            titleBoxLabel.hide();
            button1.hide();
            button1Label.hide();
            button10.hide();
            button10Label.hide();
            button100.hide();
            button100Label.hide();
            for (OBPath cell : numberGrid)
            {
                cell.show();
            }
            for (OBLabel label : numberGridLabels)
            {
                label.show();
            }
            questionIndexForThirdPhase = 0;
            thirdPhaseWrongAnswerCount = 0;
        }
    }


    public void highlightButton(OBPath button, boolean value)
    {
        lockScreen();
        int fillColour = (int) button.propertyValue("original_fill");
        int strokeColour = (int) button.propertyValue("original_stroke");
        if (value)
        {
            fillColour = darkerColor(fillColour);
            strokeColour = darkerColor(strokeColour);
        }
        button.setFillColor(fillColour);
        button.setStrokeColor(strokeColour);
        unlockScreen();
    }


    public void highlightCell(OBPath cell, boolean value, boolean darkerColour, boolean hilitedColour)
    {
        lockScreen();
        int fillColour = (value) ? colourNumberBoxSelected : colourNumberBoxNormal;
        if (darkerColour)
        {
            fillColour = darkerColor(fillColour);
        }
        else if (hilitedColour)
        {
            fillColour = colourNumberBoxHilited;
        }
        cell.setFillColor(fillColour);
        unlockScreen();
    }


    public void switchToButton(int value)
    {
        lockScreen();
        button1.setHidden((value != 1));
        button1Label.setHidden((value != 1));
        button10.setHidden((value != 10));
        button10Label.setHidden((value != 10));
        button100.setHidden((value != 100));
        button100Label.setHidden((value != 100));
        unlockScreen();
        //
        blocksAddedPerButtonPress = value;
    }


    public void toggleButton(boolean value)
    {
        lockScreen();
        if (blocksAddedPerButtonPress == 1)
        {
            int fillColor = (value) ? (int) button1.propertyValue("original_fill") : colourButtonDisabledFill;
            int strokeColor = (value) ? (int) button1.propertyValue("original_stroke") : colourButtonDisabledStroke;
            button1.setStrokeColor(strokeColor);
            button1.setFillColor(fillColor);
            button1Label.setColour(colourTextButtonNormal);
        }
        else if (blocksAddedPerButtonPress == 10)
        {
            int fillColor = (value) ? (int) button10.propertyValue("original_fill") : colourButtonDisabledFill;
            int strokeColor = (value) ? (int) button10.propertyValue("original_stroke") : colourButtonDisabledStroke;
            button10.setStrokeColor(strokeColor);
            button10.setFillColor(fillColor);
            button10Label.setColour(colourTextButtonNormal);
        }
        else if (blocksAddedPerButtonPress == 100)
        {
            int fillColor = (value) ? (int) button100.propertyValue("original_fill") : colourButtonDisabledFill;
            int strokeColor = (value) ? (int) button100.propertyValue("original_stroke") : colourButtonDisabledStroke;
            button100.setStrokeColor(strokeColor);
            button100.setFillColor(fillColor);
            button100Label.setColour(colourTextButtonNormal);
        }
        unlockScreen();
    }


    public void showBlocks(int numberOfBlocksPerSet, int totalSets, boolean playAudio) throws Exception
    {
        lockScreen();
        if (addedSetsOfBlocks != null)
        {
            for (OBControl set : addedSetsOfBlocks)
            {
                detachControl(set);
            }
            addedSetsOfBlocks.clear();
        }
        addedSetsOfBlocks = generateBlocks(numberOfBlocksPerSet, totalSets);
        for (OBControl set : addedSetsOfBlocks)
        {
            attachControl(set);
        }
        totalBlocksInScene = numberOfBlocksPerSet * totalSets;
        unlockScreen();
        //
        if (playAudio)
        {
            waitForSecs(0.3f);
            playSfxAudio("numberinbox", false);
        }
        updateTitleBox(totalBlocksInScene, playAudio);
    }


    public List<OBControl> generateBlocks(int numberOfBlocksPerSet, int totalSets)
    {
        PointF middleScreen = new PointF(bounds().width() * 0.5f, bounds().height() * 0.5f);
        List<OBControl> result = new ArrayList<>();
        RectF setFrame = null;
        if (numberOfBlocksPerSet == 1)
        {

        }
        else if (numberOfBlocksPerSet == 10)
        {
            for (int i = 0; i < 10; i++)
            {
                OBGroup set = (OBGroup) block10.copy();
                set.setPosition(new PointF(set.width() * 1.6f * i, 0f));
                set.setHidden(i >= totalSets);
                result.add(set);
                if (setFrame == null)
                {
                    setFrame = set.frame();
                }
                else
                {
                    setFrame.union(set.frame());
                }
            }
        }
        else if (numberOfBlocksPerSet == 100)
        {
            OBImage block100RenderedImage = new OBImage(block100.renderedImage());
            for (int i = 0; i < 2; i++)
            {
                for (int j = 0; j < 5; j++)
                {
                    OBImage set = (OBImage) block100RenderedImage.copy();
                    set.setPosition(new PointF(set.width() * 1.1f * j, set.height() * 1.1f * i));
                    set.setHidden((5 * i + j) >= totalSets);
                    result.add(set);
                    //
                    if (setFrame == null)
                    {
                        setFrame = set.frame();
                    }
                    else
                    {
                        setFrame.union(set.frame());
                    }
                }
            }
        }
        if (setFrame != null)
        {
            OBGroup completeSet = new OBGroup(result, setFrame);
            completeSet.setPosition(middleScreen);
            for (OBControl set : result)
            {
                completeSet.removeMember(set);
            }
            //
//            completeSet.setBorderColor(Color.YELLOW);
//            completeSet.setBorderWidth(3.0f);
//            attachControl(completeSet);
        }
        return result;
    }


    public void updateTitleBox(int totalBlocks, boolean playAudio) throws Exception
    {
        lockScreen();
        if (totalBlocks >= 0)
        {
            titleBox.show();
            titleBoxLabel.show();
            titleBoxLabel.setString(String.format("%d", totalBlocks));
        }
        else
        {
            titleBoxLabel.hide();
            titleBox.hide();
        }

        unlockScreen();
        if (playAudio)
        {
            playAudio(String.format("n_%d", totalBlocks));
            waitAudio();
        }
    }


    public void hiliteCurrentCell()
    {
        lockScreen();
        for (int i = 0; i < numberGrid.size(); i++)
        {
            int fillColor = (hilitedCellIndex == i) ? colourNumberBoxSelected : colourNumberBoxNormal;
            OBPath cell = numberGrid.get(i);
            cell.setFillColor(fillColor);
        }
        unlockScreen();
    }

    public void updateLastActionTimeStamp()
    {
        lastActionTimestamp = OC_Generic.currentTime();
    }


    public void playGridQuestion() throws Exception
    {
        String number = String.format("n_%s", questionsForThirdPhase.get(questionIndexForThirdPhase));
        playAudio(number);
        List replayAudio = new ArrayList();
        replayAudio.addAll(((Map<String, List>) audioScenes.get(currentEvent())).get("REPEAT"));
        replayAudio.add(number);
        setReplayAudio(OBUtils.insertAudioInterval(replayAudio, 300));
    }


    public Object findButton(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) Arrays.asList(button1, button10, button100), pt, true);
        return c;
    }

    public Object findHilitedCell(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) Arrays.asList(numberGrid.get(hilitedCellIndex)), pt, true);
        return c;
    }

    public Object findCell(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) numberGrid, pt, true);
        return c;
    }

    public Object findLabel(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) numberGridLabels, pt, true);
        return c;
    }


    public void checkButton(final OBPath button) throws Exception
    {
        setStatus(STATUS_CHECKING);
        highlightButton(button, true);
        //
        OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                lockScreen();
                highlightButton(button, false);
                toggleButton(false);
                unlockScreen();
            }
        });
        //
        int blocksPerSet = (int) button.propertyValue("blocks_added");
        int totalSetsInScene = (totalBlocksInScene / blocksPerSet) + 1;
        if (blocksPerSet == 10)
        {
            playSfxAudio("tensq", false);
        }
        else if (blocksPerSet == 100)
        {
            playSfxAudio("hundredsq", false);
        }
        //
        if (totalSetsInScene == 1 || totalSetsInScene == 10)
        {
            if (totalSetsInScene == 10)
            {
                OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        highlightButton(button, false);
                        waitForSecs(0.3f);
                        playSfxAudio("buttonoff", false);
                        switchToButton(0);
                    }
                });
            }
            //
            showBlocks(blocksPerSet, totalSetsInScene, true);
            waitForAudio();
            waitForSecs(0.3f);
            //
            nextScene();
        }
        else
        {
            showBlocks(blocksPerSet, totalSetsInScene, true);
            lockScreen();
            toggleButton(true);
            unlockScreen();
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public void checkCellForThirdPhase(OBPath cell) throws Exception
    {
        setStatus(STATUS_CHECKING);
        //
        OBLabel label = (OBLabel) cell.propertyValue("label");
        String userAnswer = label.text();
        String correctAnswer = questionsForThirdPhase.get(questionIndexForThirdPhase);
        if (userAnswer.equals(correctAnswer))
        {
            highlightCell(cell, true, true, false);
            waitForSecs(0.3f);
            //
            gotItRightBigTick(false);
            waitForSecs(0.3f);
            //
            highlightCell(cell, false, false, false);
            questionIndexForThirdPhase++;
            if (questionIndexForThirdPhase >= questionsForThirdPhase.size())
            {
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                nextScene();
            }
            else
            {
                thirdPhaseWrongAnswerCount = 0;
                playGridQuestion();
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            highlightCell(cell, false, true, false);
            waitForSecs(0.3f);
            //
            thirdPhaseWrongAnswerCount++;
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            highlightCell(cell, false, false, false);
            if (thirdPhaseWrongAnswerCount < 2)
            {
                playGridQuestion();
            }
            else
            {
                OBPath correctCell = null;
                //
                for (OBPath numberCell : numberGrid)
                {
                    String cellNumber = (String) numberCell.propertyValue("number");
                    if (cellNumber.equals(correctAnswer))
                    {
                        correctCell = numberCell;
                    }
                }
                String wrongAudio = getAudioForScene(currentEvent(), "INCORRECT").get(0);
                List audio = OBUtils.insertAudioInterval(Arrays.asList(wrongAudio, String.format("n_%s", questionsForThirdPhase.get(questionIndexForThirdPhase))), 300);
                //
                OBConditionLock audioLock = playAudioQueued(audio, false);
                while (audioLock.conditionValue() != PROCESS_DONE)
                {
                    highlightCell(correctCell, true, false, true);
                    waitForSecs(0.2f);
                    //
                    highlightCell(correctCell, false, false, false);
                    waitForSecs(0.2f);
                }
                questionIndexForThirdPhase++;
                wrongAudio = getAudioForScene(currentEvent(), "INCORRECT").get(1);
                if (questionIndexForThirdPhase >= questionsForThirdPhase.size())
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    nextScene();
                }
                else
                {
                    audio = OBUtils.insertAudioInterval(Arrays.asList(wrongAudio, String.format("n_%s", questionsForThirdPhase.get(questionIndexForThirdPhase))), 300);
                    //
                    thirdPhaseWrongAnswerCount = 0;
                    playAudioQueued(audio);
                }
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public void checkHilitedCell(OBPath cell) throws Exception
    {
        setStatus(STATUS_CHECKING);
        highlightCell(cell, true, false, true);
        waitForSecs(0.3f);
        //
        OBLabel label = (OBLabel) cell.propertyValue("label");
        playSfxAudio("numberingrid", false);
        //
        lockScreen();
        label.show();
        unlockScreen();
        waitForSecs(0.3f);
        //
        playAudio(String.format("n_%s", label.text()));
        waitAudio();
        //
        if (isFirstHilitedCell())
        {
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("PROMPT", 0.3f, false);
        }
        //
        nextHilitedCell();
        //
        lockScreen();
        highlightCell(cell, false, false, false);
        hiliteCurrentCell();
        unlockScreen();
        //
        if (isCompleteHilitingCells())
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            //
            nextScene();
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            updateLastActionTimeStamp();
            final Object obj = findButton(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton((OBPath)obj);
                    }
                });
            }
            else if (eventsForHilitedCell.contains(currentEvent()) && numberGrid != null)
            {
                final Object hilitedCell = findHilitedCell(pt);
                if (hilitedCell != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkHilitedCell((OBPath) hilitedCell);
                        }
                    });
                }
            }
            else if (eventsForThirdPhase.contains(currentEvent()))
            {
                final Object cell = findCell(pt);
                if (cell != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            checkCellForThirdPhase((OBPath) cell);
                        }
                    });
                }
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

    public void touchUpAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }


}
