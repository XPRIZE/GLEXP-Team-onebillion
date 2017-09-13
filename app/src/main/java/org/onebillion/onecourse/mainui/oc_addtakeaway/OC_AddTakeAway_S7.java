package org.onebillion.onecourse.mainui.oc_addtakeaway;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 27/06/16.
 */
public class OC_AddTakeAway_S7 extends OC_Generic_Event
{
    static int minSnakes = 1;
    static int maxSnakes = 3;
    static int maxDiceValue = 7;
    OBControl currentCell;
    OBControl lastCell;
    OBControl correctCell;
    int number_plays, currentPosition, nextPlayValue, totalSnakes;
    Boolean tumbler_locked;
    OBAnimationGroup counterMovementAnimation;

    public OC_AddTakeAway_S7 ()
    {
        super();
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        totalSnakes = 0;
        number_plays = 0;
        currentPosition = 1;
        tumbler_locked = false;
        //
        action_buildMap();
        action_linkSnakesAndLadders();
        //
        hideControls("grid");
        hideControls(".*_path");
        //
        for (OBControl control : filterControls("dice.*"))
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.getWorldPosition()));
        }
    }


    public void action_buildMap ()
    {
        OBControl referenceGrid = objectDict.get("grid");
        //
        int totalRows = Integer.parseInt(eventAttributes.get("total_rows"));
        int totalColumns = Integer.parseInt(eventAttributes.get("total_columns"));
        int colour1 = OBUtils.colorFromRGBString(eventAttributes.get("colour_1"));
        int colour2 = OBUtils.colorFromRGBString(eventAttributes.get("colour_2"));
        int colour3 = OBUtils.colorFromRGBString(eventAttributes.get("colour_3"));
        float borderSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("border_size")));
        int borderColour = OBUtils.colorFromRGBString(eventAttributes.get("border_colour"));
        //
        float width = referenceGrid.width() / (float) totalColumns;
        float height = referenceGrid.height() / (float) totalRows;
        int cellNumber = 1;
        //
        //
        for (int j = 0; j < totalRows; j++)
        {
            int i = (j == 2) ? totalColumns - 1 : (j == 1 ? totalColumns - 1 : 0);
            Boolean breakCondition = false;
            do
            {
                action_buildMap_createCell(width, height, i, j, totalRows, totalColumns, referenceGrid, borderSize, borderColour, colour1, colour2, colour3, cellNumber);
                cellNumber++;
                //
                if (j == 2) i--;
                else i++;
                //
                breakCondition = (j == 2 ? i >= 0 : i < (j == 3 ? 1 : totalColumns));
            } while (breakCondition);
        }
        //
        OBControl counter = objectDict.get("counter");
        OBControl firstCell = objectDict.get("cell_1");
        counter.setPosition(firstCell.position());
        counter.setProperty("currentPosition", counter.position());
    }


    public void action_buildMap_createCell (float width, float height, int i, int j, int totalRows, int totalColumns, OBControl referenceGrid, float borderSize, int borderColour, int colour1, int colour2, int colour3, int cellNumber)
    {
        OBControl cell = new OBControl();
        cell.setFrame(new RectF(0, 0, width, height));
        cell.setLeft(referenceGrid.left() + i * (width - borderSize));
        cell.setTop(referenceGrid.bottom() - (j + 1) * (height - borderSize));
        cell.setBorderWidth(borderSize);
        cell.borderColour = borderColour;
        //
        if (cellNumber == 1 || (j == totalRows - 1 && i == totalColumns - 1))
        {
            cell.setBackgroundColor(colour3);
        }
        else if (cellNumber % 2 == 0)
        {
            cell.setBackgroundColor(colour1);
        }
        else
        {
            cell.setBackgroundColor(colour2);
        }
        //
        OBGroup group = new OBGroup(Arrays.asList(cell));
        String cell_id = String.format("cell_%d", cellNumber);
        group.setProperty("id", cell_id);
        group.setProperty("number", cellNumber);
        //
        objectDict.put(cell_id, group);
        attachControl(group);
        //
        lastCell = group;
    }


    public void action_linkSnakesAndLadders ()
    {
        String links[] = eventAttributes.get("links").split(",");
        for (String link_id : links)
        {
            String link[] = eventAttributes.get(String.format("link_%s", link_id)).split(",");
            OBControl first_cell = objectDict.get(String.format("cell_%d", Integer.parseInt(link[0])));
            OBControl last_cell = objectDict.get(String.format("cell_%d", Integer.parseInt(link[1])));
            OBPath path = (OBPath) objectDict.get(String.format("%s_path", link_id));
            //
            path.sizeToBox(new RectF(bounds()));
            //
            PointF firstCell_delta = OB_Maths.DiffPoints(first_cell.getWorldPosition(), OB_Maths.AddPoints(path.topLeft(), OC_Generic.firstPoint(path, this)));
            PointF newFirstPoint = OB_Maths.AddPoints(OC_Generic.firstPoint(path, this), firstCell_delta);
            OC_Generic.setFirstPoint(path, newFirstPoint, this);
            //
            PointF lastCell_delta = OB_Maths.DiffPoints(last_cell.getWorldPosition(), OB_Maths.AddPoints(path.topLeft(), OC_Generic.lastPoint(path, this)));
            PointF newLastPoint = OB_Maths.AddPoints(OC_Generic.lastPoint(path, this), lastCell_delta);
            OC_Generic.setLastPoint(path, newLastPoint, this);
            //
            first_cell.setProperty("path", path);
            first_cell.setProperty("destination", last_cell);
            //
            path.sizeToBox(new RectF(bounds()));
        }
    }


    public void action_setDiceSpots (int first, int last)
    {
        OBGroup dice_1 = (OBGroup) objectDict.get("dice_1");
        OBGroup dice_2 = (OBGroup) objectDict.get("dice_2");
        //
        List<OBControl> layers = dice_1.filterMembers("roll_[0-9]+");
        for (OBControl layer : layers) layer.hide();
        //
        layers = dice_2.filterMembers("roll_[0-9]+");
        for (OBControl layer : layers) layer.hide();
        //
        dice_1.objectDict.get(String.format("roll_%d", first)).show();
        dice_2.objectDict.get(String.format("roll_%d", last)).show();
        //
        dice_1.show();
        dice_2.show();
        //
        dice_1.setNeedsRetexture();
        dice_2.setNeedsRetexture();
    }


    public void action_shakeTumbler (int firstDice, int lastDice) throws Exception
    {
        OBControl tumbler = objectDict.get("tumbler");
        OBControl dice_1 = objectDict.get("dice_1");
        OBControl dice_2 = objectDict.get("dice_2");
        //
        PointF originalPosition_dice_1 = OC_Generic.copyPoint((PointF) dice_1.propertyValue("originalPosition"));
        PointF originalPosition_dice_2 = OC_Generic.copyPoint((PointF) dice_2.propertyValue("originalPosition"));
        //
        OBAnim moveAnim1 = OBAnim.moveAnim(tumbler.position(), dice_1);
        OBAnim moveAnim2 = OBAnim.moveAnim(tumbler.position(), dice_2);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim1, moveAnim2), 0.2, true, OBAnim.ANIM_LINEAR, this);
        //
        action_setDiceSpots(firstDice, lastDice);
        //
        OBAnim rotateAnim1 = OBAnim.rotationAnim((float) Math.toRadians(-5), tumbler);
        OBAnim rotateAnim2 = OBAnim.rotationAnim((float) Math.toRadians(+5), tumbler);
        playSfxAudio("tumbler_shake", false);
        List<OBAnim> anim1 = Arrays.asList(rotateAnim1);
        List<OBAnim> anim2 = Arrays.asList(rotateAnim2);
        Float duration = 0.1f;
        Integer timing = OBAnim.ANIM_EASE_IN_EASE_OUT;
        //
        List<List<OBAnim>> animations = Arrays.asList(anim1, anim2, anim1, anim2, anim1, anim2, anim1);
        List<Float> durations = Arrays.asList(duration, duration, duration, duration, duration, duration, duration);
        List<Integer> timings = Arrays.asList(timing, timing, timing, timing, timing, timing, timing);
        OBAnimationGroup.chainAnimations(animations, durations, true, timings, 1, this);
        //
        moveAnim1 = OBAnim.moveAnim(originalPosition_dice_1, dice_1);
        moveAnim2 = OBAnim.moveAnim(originalPosition_dice_2, dice_2);
        rotateAnim1 = OBAnim.rotationAnim((float) Math.toRadians(-OC_Generic.randomInt(0, 10)), dice_1);
        rotateAnim2 = OBAnim.rotationAnim((float) Math.toRadians(-OC_Generic.randomInt(0, 10)), dice_2);
        playSfxAudio("tumbler_drop", false);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim1, moveAnim2, rotateAnim1, rotateAnim2), 0.3f, true, OBAnim.ANIM_LINEAR, this);
    }


    public void action_highlightObject (OBControl selectedObject)
    {
        lockScreen();
        selectedObject.highlight();
        unlockScreen();
    }


    public void action_lowlightObject (OBControl selectedObject)
    {
        lockScreen();
        selectedObject.lowlight();
        unlockScreen();
    }


    public void nextPlay () throws Exception
    {
        nextPlayValue = randomPlay();
        int value1 = (int) (nextPlayValue / (float) 2);
        int value2 = nextPlayValue - value1;
        //
        String nextCellName = String.format("cell_%d", nextPlayValue + currentPosition);
        correctCell = objectDict.get(nextCellName);
        number_plays++;
        action_shakeTumbler(value1, value2);
    }


    public Boolean isPlayerGoingToGetSnake ()
    {
        float factor = 75 / (float) maxSnakes;
        int threshold = (int) (factor * maxSnakes - factor * totalSnakes);
        int percentage = OC_Generic.randomInt(0, 100);
        return (percentage < threshold);
    }


    public int randomPlay ()
    {
        int lastPosition = (Integer) lastCell.propertyValue("number");
        //
        List<Integer> positions = new ArrayList<Integer>();
        if (isPlayerGoingToGetSnake())
        {
            for (int i = currentPosition + 2; i < lastPosition - 1; i++)
            {
                if (isCellSnake(i) && i - currentPosition <= maxDiceValue)
                {
                    positions.add(i);
                }
            }
            if (positions.size() > 0) totalSnakes++;
        }
        else
        {
            for (int i = currentPosition + 2; i < lastPosition - 1; i++)
            {
                if (!isCellSnake(i) && i - currentPosition <= maxDiceValue)
                {
                    positions.add(i);
                }
            }
        }
        if (positions.size() > 0)
        {
            int index = OC_Generic.randomInt(0, positions.size() - 1);
            return positions.get(index) - currentPosition;
        }
        else if (lastPosition - currentPosition <= maxDiceValue)
        {
            return lastPosition - currentPosition;
        }
        else
        {
            return OC_Generic.randomInt(2, Math.min(lastPosition - currentPosition, maxDiceValue));
        }
    }


    public Boolean isCellSnake (int number)
    {
        OBControl cell = objectDict.get(String.format("cell_%d", number));
        OBPath path = (OBPath) cell.propertyValue("path");
        if (path != null)
        {
            String id = (String) path.attributes().get("id");
            if (id != null)
            {
                return id.contains("snake");
            }
        }
        return false;
    }


    public void setScene7a ()
    {
        setSceneXX(currentEvent());
        //
        for (OBControl control : filterControls(".*"))
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.getWorldPosition()));
        }
        //
        hideControls("dice.*");
    }


    public void demo7a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look at this counter.
        OC_Generic.pointer_moveToObjectByName("counter", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // You must move it to here.
        OC_Generic.pointer_moveToObjectByName("cell_23", -5, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // So touch the cup to shake it
        OC_Generic.pointer_moveToObjectByName("tumbler", -20, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OC_Generic.pointer_simulateClick(this);
        action_shakeTumbler(2, 1);
        //
        action_playNextDemoSentence(false); // Count the spots on the dice.
        OC_Generic.pointer_moveToObjectByName("dice_2", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM, OC_Generic.Anchor.ANCHOR_LEFT), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OBGroup dice = (OBGroup) objectDict.get("dice_1");
        OBGroup layer = (OBGroup) dice.objectDict.get("roll_2");
        OBControl spot = layer.objectDict.get("dot_2_1");
        OC_Generic.pointer_moveToObject(spot, -25, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(true); // One.
        //
        spot = layer.objectDict.get("dot_2_2");
        OC_Generic.pointer_moveToObject(spot, -23, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(true); // Two.
        //
        dice = (OBGroup) objectDict.get("dice_2");
        layer = (OBGroup) dice.objectDict.get("roll_1");
        spot = layer.objectDict.get("dot_1_1");
        OC_Generic.pointer_moveToObject(spot, -21, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(true); // Three.
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Move the counter three places
        OC_Generic.pointer_moveToObjectByName("counter", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OBControl counter = objectDict.get("counter");
        OBControl cell_1 = objectDict.get("cell_2");
        action_playNextDemoSentence(false); // One.
        OC_Generic.pointer_moveToPointWithObject(counter, cell_1.position(), -20, 0.3f, true, this);
        playSfxAudio("counter_move", false);
        cell_1.highlight();
        waitAudio();
        waitForSecs(0.3);
        //
        OBControl cell_2 = objectDict.get("cell_3");
        action_playNextDemoSentence(false); // Two.
        OC_Generic.pointer_moveToPointWithObject(counter, cell_2.position(), -15, 0.3f, true, this);
        playSfxAudio("counter_move", false);
        lockScreen();
        cell_1.lowlight();
        cell_2.highlight();
        unlockScreen();
        waitAudio();
        waitForSecs(0.3);
        //
        OBControl cell_3 = objectDict.get("cell_4");
        action_playNextDemoSentence(false); // Three.
        OC_Generic.pointer_moveToPointWithObject(counter, cell_3.position(), -10, 0.3f, true, this);
        playSfxAudio("counter_move", false);
        lockScreen();
        cell_2.lowlight();
        cell_3.highlight();
        unlockScreen();
        waitAudio();
        waitForSecs(0.3);
        //
        playSfxAudio("correct", false);
        lockScreen();
        cell_3.lowlight();
        unlockScreen();
        waitSFX();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        OBControl tumbler = objectDict.get("tumbler");
        OBGroup dice_1 = (OBGroup) objectDict.get("dice_1");
        OBGroup dice_2 = (OBGroup) objectDict.get("dice_2");
        //
        OBAnim moveAnim1 = OBAnim.moveAnim(tumbler.position(), dice_1);
        OBAnim moveAnim2 = OBAnim.moveAnim(tumbler.position(), dice_2);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim1, moveAnim2), 0.2, false, OBAnim.ANIM_LINEAR, this);
        //
        action_moveObjectToOriginalPosition(counter, true);
        waitForSecs(0.7);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
//        playSceneAudio("PROMPT", false);
    }


    public void checkDragAtPoint (PointF pt)
    {
        if (status() != STATUS_DRAGGING)
        {
            lockScreen();
            for (OBControl cell : filterControls("cell.*"))
            {
                cell.lowlight();
            }
            unlockScreen();
            return;
        }
        //
        try
        {
            OBControl closestCell = finger(-1, 2, filterControls("cell.*"), pt);
            if (closestCell != null && currentCell != closestCell)
            {
                currentCell = closestCell;
                lockScreen();
                for (OBControl cell : filterControls("cell.*"))
                {
                    cell.lowlight();
                }
                closestCell.highlight();
                unlockScreen();
                playSfxAudio("counter_move", false);
            }
        }
        catch (Exception e)
        {
            System.out.println("OC_AddTakeAway_S6.checkDragAtPoint.exception caught: " + e.toString());
            e.printStackTrace();
        }
    }


    public void checkDropAtPoint (PointF pt)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        lockScreen();
        for (OBControl cell : filterControls("cell.*"))
        {
            cell.lowlight();
        }
        unlockScreen();
        //
        try
        {
            OBControl closestCell = finger(-1, 2, filterControls("cell.*"), pt);
            OBControl counter = target;
            if (closestCell != null)
            {
                target = null;
                //
                if (closestCell == correctCell)
                {
                    gotItRightBigTick(false);
                    currentPosition += nextPlayValue;
                    tumbler_locked = false;
                    //
                    OBAnim moveAnim = OBAnim.moveAnim(closestCell.position(), counter);
                    if (counterMovementAnimation != null) counterMovementAnimation.flags = OBAnimationGroup.ANIM_CANCEL;
                    counterMovementAnimation = OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.2f, true, OBAnim.ANIM_EASE_IN, this);
                    //
                    OBPath path = (OBPath) closestCell.propertyValue("path");
                    OBControl destination = (OBControl) closestCell.propertyValue("destination");
                    //
                    if (path != null)
                    {
                        waitForSecs(0.3);
                        //
                        Boolean isSnake = ((String) path.attributes().get("id")).contains("snake");
                        String audio = isSnake ? "snake" : "ladder";
                        playSfxAudio(audio, false);
                        //
                        OBAnim animToPath = OBAnim.moveAnim(path.firstPoint(), counter);
                        OBAnim anim = OBAnim.pathMoveAnim(counter, path.path(), false, 0.0f);
                        OBAnim animFromPath = OBAnim.moveAnim(destination.getWorldPosition(), counter);
                        if (counterMovementAnimation != null) counterMovementAnimation.flags = OBAnimationGroup.ANIM_CANCEL;
                        counterMovementAnimation = OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(animToPath),Arrays.asList(anim),Arrays.asList(animFromPath)), Arrays.asList(0.2f,1.5f,0.2f), true, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_LINEAR, OBAnim.ANIM_EASE_OUT), 1, this);
                        //
                        currentPosition = (Integer) destination.propertyValue("number");
                        counter.setProperty("originalPosition", counter.getWorldPosition());
                    }
                    //
                    int lastPosition = (Integer) lastCell.propertyValue("number");
                    if (currentPosition == lastPosition)
                    {
                        waitForSecs(0.3);
                        //
                        playSfxAudio("game_complete", true);
                        waitForSecs(0.3);
                        //
                        playSceneAudio("FINAL", true);
                        nextScene();
                        return;
                    }
                    else
                    {
                        if (number_plays == 1)
                        {
                            playSceneAudio("PROMPT2", false);
                            setReplayAudioScene(currentEvent(), "REPEAT3"); // Shake the cup again
                        }
                        else if (number_plays == 2)
                        {
                            playSceneAudio("PROMPT3", false);
                            setReplayAudioScene(currentEvent(), "REPEAT4"); // Continue in the same way, shaking the cup and moving the counter
                        }
                        else
                        {
                            setReplayAudioScene(currentEvent(), "REPEAT3"); // Shake the cup again.
                        }
                    }
                }
                else
                {
                    gotItWrongWithSfx();
                    //
                    currentCell = objectDict.get(String.format("cell_%d", currentPosition));
                    OBAnim moveAnim = OBAnim.moveAnim(currentCell.position(), counter);
                    if (counterMovementAnimation != null) counterMovementAnimation.flags = OBAnimationGroup.ANIM_CANCEL;
                    counterMovementAnimation = OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.2, true, OBAnim.ANIM_EASE_IN, this);
                    waitForSecs(0.3);
                    //
                    playSceneAudio("INCORRECT", false);
                    setReplayAudioScene(currentEvent(), "REPEAT2");
                }
            }
            else
            {
                currentCell = objectDict.get(String.format("cell_%d", currentPosition));
                OBAnim moveAnim = OBAnim.moveAnim(currentCell.position(), counter);
                if (counterMovementAnimation != null) counterMovementAnimation.flags = OBAnimationGroup.ANIM_CANCEL;
                counterMovementAnimation = OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.2, true, OBAnim.ANIM_EASE_IN, this);
            }
        }
        catch (Exception e)
        {
            System.out.println("OC_AddTakeAway_S7.checkDropAtPoint.exception caught: " + e.toString());
            e.printStackTrace();
        }
        //
        revertStatusAndReplayAudio();
    }


    public OBControl findCounter (PointF pt)
    {
        return finger(-1, 2, filterControls("counter"), pt);
    }


    public OBControl findTumbler (PointF pt)
    {
        return finger(-1, 2, filterControls("tumbler"), pt);
    }


    public void checkTumbler (OBControl targ)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            nextPlay();
            tumbler_locked = true;
            if (number_plays == 1)
            {
                playSceneAudio("REPEAT2", false);
            }
        }
        catch (Exception e)
        {
            System.out.println("OC_AddTakeAway_S7.checkTumbler.exception caught: " + e.toString());
            e.printStackTrace();
        }
        //
        revertStatusAndReplayAudio();
        setReplayAudioScene(currentEvent(), "REPEAT2"); // Count the spots. Then move the counter that number of places.
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING) setStatus(STATUS_AWAITING_CLICK);
        //
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl tumbler = findTumbler(pt);
            if (tumbler != null && !tumbler_locked)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkTumbler(tumbler);
                    }
                });
            }
            else
            {
                final OBControl counter = findCounter(pt);
                if (counter != null && tumbler_locked)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            checkDragTarget(counter, pt);
                        }
                    });
                }
            }
        }
    }


    public void touchUpAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    checkDropAtPoint(pt);
                }
            });
        }
    }


    public void touchMovedToPoint (final PointF pt, View v)
    {
        super.touchMovedToPoint(pt, v);
        //
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }


}
