package org.onebillion.onecourse.mainui.generic;

import android.animation.Animator;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_countingpractice.OC_CountingPractice;
import org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBAudioPlayer;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUserPressedBackException;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.onebillion.onecourse.utils.OB_Maths.PointDistance;
import static org.onebillion.onecourse.utils.OB_Maths.randomInt;
import static org.onebillion.onecourse.utils.OB_Maths.relativePointInRectForLocation;

/**
 * OC_Generic_Event
 * Main Class for generic Events that are present in Maths and Letter and Sounds activities
 * Has a set of standard functions to handle touches on the screen, dragging objects, handling correct and wrong answers
 * <p>
 * Created by pedroloureiro on 20/06/16.
 */
public class OC_Generic_Event extends OC_SectionController
{
    public static float FIRST_REMINDER_DELAY = 6.0f;
    public static float SECOND_REMINDER_DELAY = 4.0f;
    int currentDemoAudioIndex;
    int savedStatus;
    List<Object> savedReplayAudio;
    OBAnimationGroup returnToOriginalPositionAnimation;
    //
    public double lastActionTakenTimestamp;


    public interface RunLambdaWithTimestamp
    {
        void run (double timestamp) throws Exception;
    }

    public OC_Generic_Event ()
    {
        super();
    }


    public void updateLastActionTakenTimeStamp ()
    {
        lastActionTakenTimestamp = getCurrentTimeDouble();
    }

    public void prepare ()
    {
        super.prepare();
        loadFingers();
        loadEvent("master1");
        String scenes = (String) eventAttributes.get(action_getScenesProperty());
        if (scenes != null)
        {
            String[] eva = scenes.split(",");
            events = Arrays.asList(eva);
        }
        else
        {
            events = new ArrayList<>();
        }
        //
        if (currentEvent() != null)
        {
            doVisual(currentEvent());
        }
        //
        updateLastActionTakenTimeStamp();
    }


    public void start ()
    {
        final long timeStamp = setStatus(STATUS_WAITING_FOR_TRACE);
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                try
                {
                    if (!performSel("demo", currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                    //
                    OBUtils.runOnOtherThreadDelayed(3, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            if (!statusChanged(timeStamp))
                            {
                                playAudioQueuedScene(currentEvent(), "REMIND", false);
                            }
                        }
                    });
                }
                catch (OBUserPressedBackException e)
                {
                    stopAllAudio();
                    throw e;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void doAudio (String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }


    public void doReminder () throws Exception
    {
        long stTime = statusTime;
        waitForSecs(FIRST_REMINDER_DELAY);
        doReminderWithStatusTime(stTime, true);
    }


    public void doReminderWithStatusTime (final long stTime, Boolean playAudio) throws Exception
    {
        if (statusChanged(stTime)) return;
        //
        List reminderAudio = (List<String>) ((Map<String, Object>) audioScenes.get(currentEvent())).get("REMINDER");
        if (reminderAudio != null)
        {
            if (playAudio)
            {
                playAudioQueued(reminderAudio, true);
            }
            OBUtils.runOnOtherThreadDelayed(SECOND_REMINDER_DELAY, new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    doReminderWithStatusTime(stTime, false);
                }
            });
        }
    }


    public boolean isStatusIdle ()
    {
        return status() == STATUS_IDLE || status() == STATUS_WAITING_FOR_DRAG || status() == STATUS_AWAITING_CLICK || status() == STATUS_WAITING_FOR_TRACE || status() == STATUS_WAITING_FOR_TRACE;
    }


    public String getReminderKey ()
    {
        return this.currentEvent();
    }


    public void doReminderWithKey (final String key, final double reminderDelay, final RunLambdaWithTimestamp block) throws Exception
    {
        if (key == null) return;
        //
        if (!key.equals(getReminderKey())) return;
        //
        if (lastActionTakenTimestamp == -1) return;
        //
        double timestamp = lastActionTakenTimestamp;
        double elapsed = getCurrentTimeDouble() - lastActionTakenTimestamp;
        if (this.isStatusIdle() && elapsed > reminderDelay)
        {
            if (block != null)
            {
                block.run(timestamp);
            }
        }
        else
        {
            OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            doReminderWithKey(key, reminderDelay, block);
                        }
                    });
                }
            });
        }
    }



    public void doMainXX () throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        //
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    @Override
    public void setSceneXX (String scene)
    {
        MainActivity.log("OC_Generic_Event.setSceneXX");
        //
        ArrayList<OBControl> oldControls = new ArrayList<>(objectDict.values());
        //
        loadEvent(scene);
        //
        String masterEvent = eventAttributes.get("master");
        if (masterEvent != null)
        {
            Map<String, String> currentEventAttributes = new HashMap<>(eventAttributes);
            loadEvent(masterEvent);
            //
            eventAttributes = currentEventAttributes;
        }
        //
        Boolean redraw = eventAttributes.get("redraw") != null && eventAttributes.get("redraw").equals("true");
        if (redraw)
        {
            for (OBControl control : oldControls)
            {
                if (control == null) continue;
                detachControl(control);
                String id = (String) control.attributes().get("id");
                if (id != null)
                {
                    OBControl remainder = objectDict.get(id);
                    if (remainder != null && remainder.equals(control))
                    {
                        objectDict.remove(id);
                    }
                }
            }
            //
            for (OBControl control : filterControls(".*"))
            {
                Map attributes = control.attributes();
                if (attributes != null)
                {
                    String fit = (String) attributes.get("fit");
                    if (fit != null && fit.equals("fitwidth"))
                    {
                        float scale = bounds().width() / control.width();
                        PointF position = OC_Generic.copyPoint(control.position());
                        float originalHeight = control.height();
                        control.setScale(scale * control.scale());
                        float heightDiff = control.height() - originalHeight;
                        position.y += heightDiff / 2;
                        control.setPosition(position);
                    }
                }
            }
        }
        //
        targets = filterControls(action_getObjectPrefix() + ".*");
        //
//        for (OBControl control : targets)
//        {
//            PointF originalPosition = new PointF(control.position().x, control.position().y);
//            control.setProperty("originalPosition", originalPosition);
//        }
        //
        currentDemoAudioIndex = 0;
        //
        OC_Generic.colourObjectsWithScheme(this);
        //
        action_prepareScene(scene, redraw);
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        for (OBControl control : filterControls(".*"))
        {
            control.setProperty("originalPosition", OC_Generic.copyPoint(control.getWorldPosition()));
            //control.setProperty("originalPosition", OC_Generic.copyPoint(control.position()));
            //
            if (OBPath.class.isInstance(control))
            {
                OBPath path = (OBPath) control;
//                PointF position = path.getWorldPosition();
                PointF position = path.position();
                path.sizeToBoundingBoxIncludingStroke();
                path.setPosition(position);
            }
        }
    }


    public void action_playNextDemoSentence (Boolean waitAudio) throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentDemoAudioIndex, waitAudio);
        currentDemoAudioIndex++;
        waitForSecs(0.05);
    }


    public PointF closestDestinationForPosition (PointF origin, List<PointF> destinations)
    {
        PointF bestMatch = origin;
        float bestDistance = -1;
        for (PointF destination : destinations)
        {
            float distance = PointDistance(origin, destination);
            if (bestDistance == -1 || distance < bestDistance)
            {
                bestMatch = destination;
                bestDistance = distance;
            }
        }
        return bestMatch;
    }


    public PointF getInboundPosition (OBControl object, PointF position, OBControl container, float margin)
    {
        PointF result = position;
        float marginX = container.width() * margin;
        float marginY = container.height() * margin;
        float width = object.width();
        float height = object.height();
        float left = result.x - width / 2 - marginX;
        float right = result.x + width / 2 + marginX;
        float top = result.y - height / 2 - marginY;
        float bottom = result.y + height / 2 + marginY;
        if (left < container.left())
        {
            result.x += container.left() - left;
        }
        if (right > container.right())
        {
            result.x -= right - container.right();
        }
        if (top < container.top())
        {
            result.y += container.top() - top;
        }
        if (bottom > container.bottom())
        {
            result.y -= bottom - container.bottom();
        }
        return result;
    }


    public void action_moveObjectsToPosition (Map<String, PointF> positions, OBControl container, float time, boolean keepInBound)
    {
        Set<String> keys = positions.keySet();
        // play drop sound
        for (String key : keys)
        {
            OBControl control = objectDict.get(key);
            PointF relativePosition = positions.get(key);
            PointF destination = OB_Maths.locationForRect(relativePosition, container.frame());
            if (keepInBound)
            {
                destination = getInboundPosition(control, destination, container, 0.03f);
            }
            control.moveToPoint(destination, time, false);
            control.setProperty("currentPosition", OC_Generic.copyPoint(destination));
        }
    }


    public void action_repositionObjectsInContainer (OBControl container, boolean keepInBound, OBControl addedObject)
    {
        List<OBControl> objectsContained = (List<OBControl>) container.propertyValue("objects");
        int totalObjects = objectsContained.size();
        //
        int totalLines = 1;
        if (eventAttributes.get("lines") != null)
        {
            totalLines = Integer.parseInt(eventAttributes.get("lines"));
        }
        List positions = new ArrayList();
        for (int i = 0; i < totalObjects; i++)
        {
            float x = (float) (i + 1) / (totalObjects + 1);
            float y = 0.5f;
            if (totalLines > 1) y = (float) (i + 1) / (totalObjects + 1);
            positions.add(new PointF(x, y));
        }
        //
        Map<String, PointF> repositioning = new HashMap();
        //
        if (addedObject != null)
        {
            PointF destination = closestDestinationForPosition(relativePointInRectForLocation(addedObject.position(), container.frame), positions);
            positions.remove(destination);
            String key = (String) addedObject.attributes().get("id");
            //
            repositioning.put(key, OC_Generic.copyPoint(destination));
        }
        //
        for (OBControl control : objectsContained)
        {
            if (addedObject == control) continue;
            PointF destination = closestDestinationForPosition(relativePointInRectForLocation(control.position(), container.frame), positions);
            positions.remove(destination);
            String key = (String) addedObject.attributes().get("id");
            //
            repositioning.put(key, OC_Generic.copyPoint(destination));
        }
        action_moveObjectsToPosition(repositioning, container, 0.2f, keepInBound);
    }


    public void action_removeObjectFromContainers (OBControl object, boolean keepInBound)
    {
        List<OBControl> containers = filterControls("box.*");
        for (OBControl container : containers)
        {
            List<OBControl> containedObjects = (List<OBControl>) container.propertyValue("objects");
            if (containedObjects != null && containedObjects.contains(object))
            {
                containedObjects.remove(object);
                container.setProperty("objects", containedObjects);
                action_repositionObjectsInContainer(container, keepInBound, null);
            }
        }
    }


    public void moveObjectToPoint (OBControl control, PointF position, float time, boolean withSound) throws Exception
    {
        if (withSound) playSfxAudio("dropObject", false);
        control.moveToPoint(position, time, false);
        control.setProperty("currentPosition", OC_Generic.copyPoint(position));
    }


    public void moveObjectToOriginalPosition (OBControl control, boolean withSound, boolean keepInBound) throws Exception
    {
        action_removeObjectFromContainers(control, keepInBound);
        PointF originalPosition = (PointF) control.propertyValue("originalPosition");
        moveObjectToPoint(control, originalPosition, 0.3f, withSound);
    }


    public boolean action_addObjectAndRearrangeContainer (OBControl object, OBControl container, int maxObjectsPerContainer, boolean keepInBound) throws Exception
    {
        List<OBControl> objectsContained = (List<OBControl>) container.propertyValue("objects");
        if (objectsContained == null)
        {
            objectsContained = new ArrayList<>();
        }
        int objectCount = (int) objectsContained.size();
        if (objectsContained.contains(object))
        {
            moveObjectToOriginalPosition(object, true, keepInBound);
        }
        else
        {
            if (objectCount == maxObjectsPerContainer)
            {
                moveObjectToOriginalPosition(object, true, keepInBound);
            }
            else
            {
                action_removeObjectFromContainers(object, keepInBound);
                objectsContained.add(object);
                container.setProperty("objects", objectsContained);
                //
                action_repositionObjectsInContainer(container, keepInBound, object);
                return true;
            }
        }
        return false;
    }


    public void action_moveObjectToOriginalPosition (OBControl control, Boolean wait)
    {
        if (returnToOriginalPositionAnimation != null)
        {
            returnToOriginalPositionAnimation.flags = OBAnimationGroup.ANIM_CANCEL;
        }
        OBAnim anim = OBAnim.moveAnim((PointF) control.propertyValue("originalPosition"), control);
        returnToOriginalPositionAnimation = OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, wait, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public void action_moveObjectIntoContainer (OBControl control, OBControl container)
    {
        List<OBControl> contained = (List<OBControl>) container.propertyValue("contained");
        if (contained == null) contained = new ArrayList<OBControl>();
        //
        if (contained.contains(control))
        {
            action_moveObjectToOriginalPosition(control, false);
        }
        else
        {
            contained.add(control);
            container.setProperty("contained", contained);
            //
            List<OBAnim> animations = new ArrayList<OBAnim>();
            // horizontal displacement
            float deltaH = container.width() / (contained.size() + 1);
            for (int i = 0; i < contained.size(); i++)
            {
                PointF newPosition = OC_Generic.copyPoint(container.position());
                newPosition.x = container.left() + deltaH * (i + 1);
                OBControl placedObject = contained.get(i);
                animations.add(OBAnim.moveAnim(newPosition, placedObject));
            }
            OBAnimationGroup.runAnims(animations, 0.15, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
    }


    public void action_animatePlatform (OBControl platform, boolean wait) throws Exception
    {
        String platformName = (String) platform.attributes().get("id");
        String platformNumber = platformName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        //
        List<OBAnim> list_animMove1 = new ArrayList<OBAnim>();
        List<OBAnim> list_animMove2 = new ArrayList<OBAnim>();
        //
        for (OBControl item : controls)
        {
            PointF startPosition = new PointF();
            startPosition.set(item.position());
            //
            PointF endPosition = new PointF();
            endPosition.set(startPosition);
            endPosition.y -= 1.25 * item.height();
            //
            list_animMove1.add(OBAnim.moveAnim(endPosition, item));
            list_animMove1.add(OBAnim.rotationAnim((float) Math.toRadians(-180.0f), item));
            list_animMove2.add(OBAnim.moveAnim(startPosition, item));
            list_animMove1.add(OBAnim.rotationAnim((float) Math.toRadians(-360.0f), item));
//            OBAnim anim_move1 = OBAnim.moveAnim(endPosition, item);
//            OBAnim anim_move2 = OBAnim.moveAnim(startPosition, item);
//            OBAnim anim_rotate1 = OBAnim.rotationAnim((float) Math.toRadians(-180.0f), item);
//            OBAnim anim_rotate2 = OBAnim.rotationAnim((float) Math.toRadians(-360.0f), item);
//            OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(anim_move1,anim_rotate1),Arrays.asList(anim_move2,anim_rotate2)), Arrays.asList(0.4f,0.4f), false, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, this);
//            waitForSecs(0.05);
        }
        OBAnimationGroup og = new OBAnimationGroup();
        og.chainAnimations(Arrays.asList(list_animMove1, list_animMove2), Arrays.asList(0.4f, 0.4f), wait, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, this);
    }


    public OBControl action_getCorrectAnswer ()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }


    public String action_getObjectPrefix ()
    {
        return "number";
    }


    public String action_getContainerPrefix ()
    {
        return "box";
    }


    public String action_getScenesProperty ()
    {
        return "scenes";
    }


    public OBLabel action_createLabelForControl (OBControl control)
    {
        return action_createLabelForControl(control, 1.0f, false);
    }


    public OBLabel action_createLabelForControl (OBControl control, float finalResizeFactor)
    {
        return action_createLabelForControl(control, finalResizeFactor, true);
    }


    public OBLabel action_createLabelForControl (OBControl control, float finalResizeFactor, Boolean insertIntoGroup)
    {
        return OC_Generic.action_createLabelForControl(control, finalResizeFactor, insertIntoGroup, this);
    }

    public OBLabel action_createLabelForControl (OBControl control, String text, int colour, float finalResizeFactor)
    {
        control.setProperty("text", text);
        OBLabel result = OC_Generic.action_createLabelForControl(control, finalResizeFactor, false, this);
        result.setColour(colour);
        return result;
    }


    public List breakdownLabel (OBLabel mLabel)
    {
        List result = new ArrayList<>();
        String text = mLabel.text();
        Typeface tf = mLabel.typeface();
        float size = mLabel.fontSize();
        int totalLength = 0;
        //
        for (int i = 0; i < mLabel.text().length(); i++)
        {
            totalLength += 1;
            String subtx = text.substring(0, totalLength);
            RectF r = OC_Wordcontroller.boundingBoxForText(subtx, tf, size);
            float f = r.width();
            //
            OBLabel l = new OBLabel(mLabel.text().substring(i, i + 1), tf, size);
            l.sizeToBoundingBox();
            l.setZPosition(mLabel.zPosition() + 0.01f);
            l.setColour(mLabel.colour());
            l.sizeToBoundingBox();
            l.setPosition(mLabel.position());
            l.setRight(mLabel.left() + f);
            l.setProperty("original_position", l.getWorldPosition());
            result.add(l);
        }
        return result;
    }

    // Finger and Touch functions

    public OBControl findTarget (PointF pt)
    {
        return finger(-1, 2, targets, pt, true);
    }


    public Boolean audioSceneExists (String audioScene)
    {
        if (audioScenes == null) return false;
        //
        Map<String, List<String>> sc = (Map<String, List<String>>) audioScenes.get(currentEvent());
        //
        if (sc == null) return false;
        //
        List<Object> arr = (List<Object>) (Object) sc.get(audioScene);
        return arr != null;
    }


    public List<Object> audioForScene (String audioScene)
    {
        if (audioScenes == null) return null;
        //
        Map<String, List<String>> sc = (Map<String, List<String>>) audioScenes.get(currentEvent());
        //
        if (sc == null) return null;
        //
        return (List<Object>) (Object) sc.get(audioScene);
    }


    // Miscelaneous Functions
    public void playSceneAudio (String scene, Boolean wait) throws Exception
    {
        playAudioQueuedScene(currentEvent(), scene, wait);
        if (!wait) waitForSecs(0.01);
    }

    public void playSceneAudioIndex (String scene, int index, Boolean wait) throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), scene, index, wait);
        if (!wait) waitForSecs(0.1);
    }


    // CHECKING functions

    public void saveStatusClearReplayAudioSetChecking ()
    {
        savedStatus = status();
        setStatus(STATUS_CHECKING);
        //
        savedReplayAudio = _replayAudio;
        setReplayAudio(null);
    }

    public long revertStatusAndReplayAudio ()
    {
        long time = setStatus(savedStatus);
        setReplayAudio(savedReplayAudio);
        return time;
    }


    public void setReplayAudioScene (String scene, String event)
    {
        Map<String, List<String>> sc = (Map<String, List<String>>) audioScenes.get(scene);
        if (sc != null)
        {
            List<Object> arr = (List<Object>) (Object) sc.get(event); //yuk!
            if (arr != null)
            {
                setReplayAudio(arr);
                savedReplayAudio = arr;
            }
        }
    }


    public void physics_verticalDrop (List<OBControl> objects, float maxDelta, String soundEffectOnCollision)
    {
        try
        {
            Boolean floorCollision = false;
            Boolean atRest = false;
            float g = 9.8f * 200;
            double startTime = OC_Generic.currentTime();
            float initialSpeed = 0;
            //
            for (OBControl control : objects)
            {
                control.setProperty("initial", control.getWorldPosition());
            }
            //
            while (!atRest)
            {
                double t = OC_Generic.currentTime() - startTime;
                //
                float deltaY = (float) (initialSpeed * t + 0.5f * g * t * t);
                //
                if (!floorCollision && deltaY >= maxDelta)
                {
                    playSfxAudio(soundEffectOnCollision, false);
                    startTime = OC_Generic.currentTime();
                    initialSpeed = (float) (-0.3f * g * t);
                    floorCollision = true;
                    //
                    for (OBControl control : objects)
                    {
                        control.setProperty("initial", control.getWorldPosition());
                    }
                    deltaY = maxDelta;
                }
                else if (floorCollision && deltaY >= 0)
                {
                    atRest = true;
                    deltaY = 0;
                }
                //
                if (atRest)
                {
                    lockScreen();
                    //
                    for (OBControl control : objects)
                    {
                        PointF originalPosition = OC_Generic.copyPoint((PointF) control.propertyValue("originalPosition"));
                        control.setPosition(originalPosition);
                    }
                    unlockScreen();
                }
                else
                {
                    lockScreen();
                    for (OBControl control : objects)
                    {
                        PointF initial = OC_Generic.copyPoint((PointF) control.propertyValue("initial"));
                        initial.y += deltaY;
                        control.setPosition(initial);
                    }
                    unlockScreen();
                }
                //
                waitForSecs(0.01);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_Event:physics_verticalDrop:exception caught");
            e.printStackTrace();
        }
    }


    public void physics_bounceObjectsWithPlacements (List<OBControl> objects, List<OBControl> placements, String soundEffectOnFloorCollision)
    {
        try
        {
            float g = 9.8f * 200;
            float floorHeight = bounds().height() * 0.95f;
            int totalCount = objects.size();
            //
            for (int i = 1; i <= totalCount; i++)
            {
                OBControl control = objects.get(i - 1);
                control.setShouldTexturise(true);
                //
                OBControl placement = placements.get(i - 1);
                PointF destination = placement.getWorldPosition();
                PointF initialPosition = control.getWorldPosition();
                //
                float distanceX = destination.x - initialPosition.x;
                PointF midway = new PointF(initialPosition.x + distanceX * 0.75f, floorHeight);
                //
                float height = Math.abs(floorHeight - initialPosition.y);
                float flightHeight = 1.2f * height;
                float flightTime_phase1 = (float) Math.sqrt((2 * (flightHeight - height)) / g);
                float freeFallTime = (float) Math.sqrt((2 * flightHeight) / g);
                float totalTime_phase1 = flightTime_phase1 + freeFallTime;
                //
                float initialSpeedX_phase1 = (midway.x - initialPosition.x) / totalTime_phase1;
                float initialSpeedY_phase1 = (float) Math.sqrt((flightHeight - height) * 2 * g);
                PointF phase1 = new PointF(initialSpeedX_phase1, initialSpeedY_phase1);
                //
                float height_phase2 = Math.abs(floorHeight - destination.y);
                float initialSpeedY_phase2 = (float) Math.sqrt(2 * g * height_phase2);
                float flightTime_phase2 = initialSpeedY_phase2 / g;
                float initalSpeedX_phase2 = (destination.x - midway.x) / flightTime_phase2;
                PointF phase2 = new PointF(initalSpeedX_phase2, initialSpeedY_phase2);
                //
                control.setProperty("phase1", OC_Generic.copyPoint(phase1));
                control.setProperty("phase2", OC_Generic.copyPoint(phase2));
                control.setProperty("startTime", OC_Generic.currentTime() + i * 0.1);
                control.setProperty("floorCollision", false);
                control.setProperty("atRest", false);
                control.setProperty("initialPosition", OC_Generic.copyPoint(initialPosition));
                control.setProperty("midway", OC_Generic.copyPoint(midway));
                control.setProperty("destination", OC_Generic.copyPoint(destination));
                //
                Map<String, Object> sfx_map = (Map<String, Object>) audioScenes.get("sfx");
                List<String> sfx_array = (List<String>) sfx_map.get(soundEffectOnFloorCollision);
                String soundEffect = sfx_array.get(0);
//                MainActivity.log("OC_Generic_Event:physics:bounceObjectsWithPlacements:soundEffect:" + soundEffect);
                OBAudioManager.audioManager.prepareForChannel(soundEffect, String.format("bounce_%d", i));
            }
            //
            boolean animationComplete = false;
            //
            while (!animationComplete)
            {
                lockScreen();
                for (int i = 1; i <= totalCount; i++)
                {
                    OBControl control = objects.get(i - 1);
                    double startTime = (double) control.propertyValue("startTime");
                    boolean floorCollision = (boolean) control.propertyValue("floorCollision");
                    boolean atRest = (boolean) control.propertyValue("atRest");
                    PointF midway = (PointF) control.propertyValue("midway");
                    //
                    double t = OC_Generic.currentTime() - startTime;
                    //
                    if (t < 0) continue;
                    if (atRest) continue;
                    //
                    PointF newPosition;
                    if (!floorCollision)
                    {
                        PointF phase1 = (PointF) control.propertyValue("phase1");
                        PointF initialPosition = (PointF) control.propertyValue("initialPosition");
                        //
                        float newX = (float) (initialPosition.x + phase1.x * t);
                        float newY = (float) (initialPosition.y + 0.5f * g * t * t - phase1.y * t);
                        newPosition = new PointF(newX, newY);
                        //
                        if (newPosition.y > floorHeight)
                        {
                            control.setProperty("startTime", OC_Generic.currentTime());
                            control.setProperty("floorCollision", true);
                            newPosition = OC_Generic.copyPoint(midway);
                            //
                            OBAudioPlayer player = OBAudioManager.audioManager.playerForChannel(String.format("bounce_%d", i));
                            player.play();
                        }
                    }
                    else
                    {
                        PointF phase2 = (PointF) control.propertyValue("phase2");
                        PointF destination = (PointF) control.propertyValue("destination");
                        float newX = (float) (midway.x + phase2.x * t);
                        float newY = (float) (midway.y - phase2.y * t + 0.5f * g * t * t);
                        newPosition = new PointF(newX, newY);
                        //
                        float distanceToTarget = Math.abs(newPosition.y - destination.y);
                        if (distanceToTarget <= 2)
                        {
                            control.setProperty("atRest", true);
                            newPosition = OC_Generic.copyPoint(destination);
                            //
                            animationComplete = true;
                            for (OBControl otherControl : objects)
                            {
                                animationComplete = animationComplete && (boolean) otherControl.propertyValue("atRest");
                            }
                        }
                    }
                    //
                    control.setPosition(OC_Generic.copyPoint(newPosition));
                }
                unlockScreen();
                //
                waitForSecs(0.01f);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_Event:physics_bounce:exception caught");
            e.printStackTrace();
        }
    }


    public void action_showShadow (OBControl control)
    {
        float shadowRadius = 5;
        List<OBControl> members = new ArrayList<>();
        members.add(control);
        //
        lockScreen();
        //
        OBGroup shadowGroup = new OBGroup(members);
        shadowGroup.outdent(shadowRadius);
        attachControl(shadowGroup);
        control.setProperty("shadowGroup", shadowGroup);
        control.setShadow(shadowRadius, 1.0f, shadowRadius / 2, shadowRadius / 2, Color.DKGRAY);
        shadowGroup.setShouldTexturise(true);
        //
        unlockScreen();
    }


    public void action_removeShadow (OBControl control)
    {
        lockScreen();
        //
        OBGroup shadowGroup = (OBGroup) control.propertyValue("shadowGroup");
        if (shadowGroup != null)
        {
            shadowGroup.removeMember(control);
            attachControl(control);
            detachControl(shadowGroup);
            control.setShadow(0, 0, 0, 0, Color.BLACK);
        }
        //
        unlockScreen();
    }


    public List<OBLabel> action_addLabelsToObjects (String pattern, float finalResizeFactor, boolean insertIntoGroup)
    {
        List<OBControl> numbers = sortedFilteredControls(pattern);
        List<OBLabel> createdLabels = new ArrayList<>();
        float smallestFontSize = 1000000000;
        //
        for (OBControl number : numbers)
        {
            OBLabel label = action_createLabelForControl(number, finalResizeFactor, insertIntoGroup);
            if (label.fontSize() < smallestFontSize) smallestFontSize = label.fontSize();
            //
            createdLabels.add(label);
        }
        //
        for (OBLabel label : createdLabels)
        {
            label.setFontSize(smallestFontSize);
            label.sizeToBoundingBox();
            //
            label.setProperty("originalPosition", OC_Generic.copyPoint(label.getWorldPosition()));
            if (!insertIntoGroup)
            {
                attachControl(label);
            }
        }
        return createdLabels;
    }


    public PointF getRelativePositionForObject (OBControl control)
    {
        PointF position = control.position();
        OBControl parent = control.parent;
        RectF dimensions;
        if (parent != null) dimensions = parent.bounds;
        else dimensions = boundsf();
        //
        PointF relativePosition = new PointF(position.x / dimensions.width(), position.y / dimensions.height());
        return relativePosition;
    }


    public PointF getAbsoluteOffsetWithParent (OBControl control)
    {
        PointF relativePosition = getRelativePositionForObject(control);
        PointF relativeOffset = OB_Maths.DiffPoints(new PointF(0.5f, 0.5f), relativePosition);
        RectF dimensions = control.parent.bounds();
        PointF absoluteOffset = new PointF(relativeOffset.x * dimensions.width(), relativeOffset.y * dimensions.height());
        return absoluteOffset;
    }


    public PointF action_getMiddleOfGroup (String pattern)
    {
        PointF average = new PointF(0f, 0f);
        List<OBControl> controls = filterControls(pattern);
        for (OBControl control : controls)
        {
            average = OB_Maths.AddPoints(control.getWorldPosition(), average);
        }
        average = new PointF(average.x / controls.size(), average.y / controls.size());
        return average;
    }

    public int randomNumberBetween(int min, int max)
    {
        return randomInt(min, max);

    }


    public double getCurrentTimeDouble()
    {
        return (new Date().getTime() / 1000.0);
    }


    @Override
    public void replayAudio ()
    {
        updateLastActionTakenTimeStamp();
        //
        super.replayAudio();
    }
}
