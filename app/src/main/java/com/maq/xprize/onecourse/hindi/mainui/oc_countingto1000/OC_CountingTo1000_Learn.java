package com.maq.xprize.onecourse.hindi.mainui.oc_countingto1000;

import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBConditionLock;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic.hidePointer;
import static com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic.movePointerToRestingPosition;
import static com.maq.xprize.onecourse.hindi.utils.OBAnim.ANIM_EASE_IN_EASE_OUT;

public class OC_CountingTo1000_Learn extends OC_CountingTo1000
{

    public void buildEvents()
    {
        String demoString = parameters.get("demo");
        boolean demo = demoString != null && demoString.equals("true");
        //
        List newEvents = new ArrayList();
        newEvents.add((demo) ? "intro1" : "intro2");
        newEvents.addAll(Arrays.asList("a,b,c,d,e,f,g".split(",")));
        //
        eventsForHilitedCell = Arrays.asList("e".split(","));
        eventsForThirdPhase = Arrays.asList("f".split(","));
        //
        events = newEvents;
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
                catch (Exception exception)
                {
                    MainActivity.log("OC_CountingTo1000 --> Exception caught: ", exception.toString());
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
                            List flashButtonEvents = Arrays.asList("a,b,c,d".split(","));
                            List flashCellEvents = Arrays.asList("e".split(","));
                            List replayAudioEvents = Arrays.asList("f".split(","));
                            //
                            if (flashButtonEvents.contains(currentEvent()))
                            {
                                for (int i = 0; i < 3; i++)
                                {
                                    lockScreen();
                                    highlightButton(button1, true);
                                    highlightButton(button10, true);
                                    highlightButton(button100, true);
                                    unlockScreen();
                                    waitForSecs(0.2f);
                                    //
                                    lockScreen();
                                    highlightButton(button1, false);
                                    highlightButton(button10, false);
                                    highlightButton(button100, false);
                                    unlockScreen();
                                    waitForSecs(0.2f);
                                }
                            }
                            else if (flashCellEvents.contains(currentEvent()))
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
                            else if (replayAudioEvents.contains(currentEvent()))
                            {
                                final OBConditionLock audioLock = playAudioQueued(_replayAudio, false);
                                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                {
                                    public void run() throws Exception
                                    {
                                        waitAudioQueue(audioLock);
                                        updateLastActionTimeStamp();
                                    }
                                });
                            }
                        }
                        updateLastActionTimeStamp();
                    }
                }
                Thread.sleep(1000);
            }
        });
    }


    public void demointro1() throws Exception
    {
        setStatus(STATUS_BUSY);
        //
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        List<String> audioFiles = getAudioForScene(currentEvent(), "DEMO");
        String presenterAudio = audioFiles.get(0);                      // Now we’ll count to one thousand … : hundreds!
        presenter.speak((List<Object>) (Object) Arrays.asList(presenterAudio), 0.3f, this);
        waitForSecs(0.7f);
        //
        presenterAudio = audioFiles.get(1);                             // It’s a big number.
        presenter.speak((List<Object>) (Object) Arrays.asList(presenterAudio), 0.3f, this);
        waitForSecs(0.3f);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        OBControl front = presenter.control.objectDict.get("front");
        PointF destPos = new PointF(bounds().width() - 1.5f * front.width(), currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        presenterAudio = audioFiles.get(2);                   // Ready?;
        presenter.speak((List<Object>) (Object) Arrays.asList(presenterAudio), 0.3f, this);
        waitForSecs(0.3f);
        //
        currPos = presenter.control.position();
        destPos = new PointF(1.25f * bounds().width() + front.width(), currPos.y);
        presenter.walk(destPos);
        //
        nextScene();
    }

    public void demointro2() throws Exception
    {
        setStatus(STATUS_BUSY);
        //
        loadPointer(POINTER_MIDDLE);
        OBConditionLock lock = playAudioQueuedScene("DEMO", 0.3f, false);
        movePointerToRestingPosition(0.6f, true, this);
        waitAudioQueue(lock);
        waitForSecs(0.3f);
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
        playAudioScene("DEMO", 0, false);                       //  First, let’s count to a hundred … : tens.;
        movePointerToPoint(new PointF(bounds().width() * 0.7f, bounds().height() * 0.6f), 0.6f, false);
        waitAudio();
        waitForSecs(0.3f);
        //
        PointF destination = titleBox.bottomPoint();
        destination.y += titleBox.height() * 0.1f;
        playAudioScene("DEMO", 1, false);                       //  This box is for the number.;
        movePointerToPoint(destination, 0.6f, false);
        waitAudio();
        destination = button10.rightPoint();
        destination.x += button10.width() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        playAudioScene("DEMO", 2, false);                       //  Touch the button.;
        waitForAudio();
        hidePointer(this);
        waitForSecs(0.6f);
        //
        toggleButton(true);
        playSfxAudio("buttonactive", false);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demob() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        OBGroup set = (OBGroup) addedSetsOfBlocks.get(0);
        OBPath firstBlock = (OBPath) set.members.get(0);
        OBPath lastBlock = (OBPath) set.members.get(set.members.size() - 1);
        PointF destination = convertPointFromControl(firstBlock.rightPoint(), set);
        destination.x += firstBlock.width() * 0.5f;
        movePointerToPoint(destination, 0.6f, true);
        playAudioScene("DEMO", 0, false);                           // Ten little squares.;
        destination = convertPointFromControl(lastBlock.rightPoint(), set);
        destination.x += lastBlock.width() * 0.5f;
        movePointerToPoint(destination, 1.2f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                           // Ten.;
        destination = titleBox.bottomPoint();
        destination.y += titleBox.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);                           // Keep going … to one hundred.;
        destination = button10.rightPoint();
        destination.x += button10.width() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        hidePointer(this);
        playSfxAudio("buttonactive", false);
        toggleButton(true);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void democ() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        OBGroup lastSet = (OBGroup) addedSetsOfBlocks.get(addedSetsOfBlocks.size() - 1);
        PointF destination = lastSet.rightPoint();
        destination.x += lastSet.width() * 1.0f;
        playAudioScene("DEMO", 0, false);                       // One hundred little squares.;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                       // One hundred.;
        destination = titleBox.bottomPoint();
        destination.y += titleBox.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);                       // Now watch.;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        List animations = new ArrayList<>();
        for (int i = 0; i < 10; i++)
        {
            OBGroup set = (OBGroup) addedSetsOfBlocks.get(i);
            destination = new PointF(bounds().width() * 0.5f, bounds().height() * 0.5f);
            destination.x -= (set.width() - block10Template.lineWidth() * 2) * (5 - i);
            OBAnim moveAnim = OBAnim.moveAnim(destination, set);
            animations.add(moveAnim);
            for (OBPath block : (List<OBPath>) (Object) set.members)
            {
                OBAnim colourAnimFill = OBAnim.colourAnim("fillColor", button100.fillColor(), block);
                animations.add(colourAnimFill);
                OBAnim colourAnimStroke = OBAnim.colourAnim("strokeColor", button100.strokeColor(), block);
                animations.add(colourAnimStroke);
            }
        }
        playSfxAudio("makeblock", false);
        OBAnimationGroup.runAnims(animations, 0.6, true, OBAnim.ANIM_EASE_OUT, this);
        waitForSecs(0.5f);
        //
        List<OBControl> newBlocks = generateBlocks(100, 10);
        OBControl firstSet = newBlocks.get(0);
        OBGroup allBlocks;
        //
        lockScreen();
        allBlocks = new OBGroup(addedSetsOfBlocks);
        attachControl(allBlocks);
        unlockScreen();
        //
        OBAnim moveAnim = OBAnim.moveAnim(firstSet.position(), allBlocks);
        OBAnim scaleAnim = OBAnim.scaleAnim(firstSet.width() / allBlocks.width(), allBlocks);
        playSfxAudio("slide", false);

        final OBGroup finalAllBlocks = allBlocks;
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim, scaleAnim), 0.5, true, ANIM_EASE_IN_EASE_OUT, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                lockScreen();
                showBlocks(100, 1, false);
                detachControl(finalAllBlocks);
                unlockScreen();
            }
        }, this);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false);                           // One hundred little squares.;
        destination = allBlocks.bottomPoint();
        destination.y += allBlocks.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playSfxAudio("buttonon", false);
        lockScreen();
        switchToButton(100);
        toggleButton(false);
        unlockScreen();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 4, false);                           // Touch this button to add more hundreds.;
        destination = button100.rightPoint();
        destination.x += button100.width() * 1.0f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        playAudioScene("DEMO", 5, false);                           // And watch the numbers change!;
        destination = titleBox.bottomPoint();
        destination.y += titleBox.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        hidePointer(this);
        waitForSecs(0.3f);
        //
        playSfxAudio("buttonactive", false);
        toggleButton(true);
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demod() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false);                       // One thousand little squares!;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                       // One thousand!;
        PointF destination = titleBox.bottomPoint();
        destination.y += titleBox.height() * 0.1f;
        movePointerToPoint(destination, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        hidePointer(this);
        waitForSecs(0.6f);
        //
        gotItRightBigTick(true);
        waitForSecs(1.2f);
        //
        playSfxAudio("alloff", false);
        lockScreen();
        for (OBControl set : addedSetsOfBlocks)
        {
            detachControl(set);
        }
        titleBox.hide();
        titleBoxLabel.hide();
        button100.hide();
        unlockScreen();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void demoe() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playSfxAudio("gridon", false);
        //
        lockScreen();
        for (OBPath box : numberGrid)
        {
            box.show();
        }
        unlockScreen();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 0, false);                       // Let’s see all those numbers together.;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playSfxAudio("fill", false);
        hiliteCurrentCell();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                       // Touch the yellow box.;
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

    public void demof() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                   // Next … touch all the numbers you hear!;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playGridQuestion();
        hidePointer(this);
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demog() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false);                   // Good!;
        movePointerToRestingPosition(0.6f, true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                   // Let’s hear the numbers again.;
        waitAudio();
        waitForSecs(0.6f);
        //
        playAudioScene("DEMO", 2, false);                   // You can say them too!;
        waitAudio();
        hidePointer(this);
        waitForSecs(0.6f);
        //
        int originalStrokeColour = -1;
        for (OBPath cell : numberGrid)
        {
            String number = String.format("n_%s", cell.propertyValue("number"));
            OBLabel label = (OBLabel) cell.propertyValue("label");
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
        playAudioScene("DEMO", 3, true);                // Great! We counted to one thousand … : hundreds.;
        waitForSecs(0.3f);
        //
        nextScene();
    }
}
