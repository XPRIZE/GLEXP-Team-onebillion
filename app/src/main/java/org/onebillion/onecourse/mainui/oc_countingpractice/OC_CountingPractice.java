package org.onebillion.onecourse.mainui.oc_countingpractice;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.random;
import static java.lang.Math.round;
import static org.onebillion.onecourse.utils.OBAudioManager.AM_MAIN_CHANNEL;
import static org.onebillion.onecourse.utils.OBAudioManager.AM_SFX_CHANNEL;
import static org.onebillion.onecourse.utils.OB_Maths.AddPoints;
import static org.onebillion.onecourse.utils.OB_Maths.NormalisedVector;
import static org.onebillion.onecourse.utils.OB_Maths.PointDistance;
import static org.onebillion.onecourse.utils.OB_Maths.ScalarTimesPoint;
import static org.onebillion.onecourse.utils.OB_Maths.randomInt;

/**
 * Created by pedroloureiro on 04/01/2018.
 */


public class OC_CountingPractice extends OC_Generic_Event
{
    private List<OBControl> numberButtons;
    private List<OBLabel> numberButtonLabels;
    private List<OBGroup> clumps;
    private List<OBPath> dashes;
    private List<OBLabel> dashLabels;
    private List<OBControl> physicsControls;
    //
    private int colourTextNormal, colourTextHilite, colourTextDisabled, colourTextDraggable, colourTextCorrect, colourTextTarget;
    private int colourButtonNormal, colourButtonHilite, colourButtonDisabled;
    private int colourDashNormal, colourDashHilite;
    private int colourJarContainerBorder;
    //
    private boolean hasDemo;
    //
    private int totalObjectsForUnit;
    private int startingObjectsInContainer;
    private int clumpOfObjectsForUnit;
    private int maxClumpsOnScreen;
    //
    private String mode;
    //
    private List<OBLabel> dragLabels;
    private List distractors;
    private List numberSequence;
    private int hiddenNumberIndex;
    private int wrongAttempts;
    //
    private String currentAnswer;
    private String correctAnswer;
    //
    private OBGroup jar;
    private OBLabel jarLabel;
    private OBControl jarLabelContainer;
    private OBPath jarContainerPath;
    private OBControl dragBar;
    private OBControl dragLabel;
    private OBControl jarHotArea;
    private OBControl jarAppliedForce;
    private OBGroup clumpTemplate;
    private OBControl confirmArrow;
    private OBControl targetLabelContainer;
    private OBLabel targetLabel;
    //
    private boolean firstDropPlayed;
    private boolean isMode2Phase2;
    private boolean doingDemoOverride;
    //
    private List atRestObjects;
    private List<OBLabel> answerLabels;
    //
    private boolean contactThreadRunning;
    private boolean contactThread_aborting;
    private boolean contactThreadDead;
    private boolean physicsThreadRunning;
    private boolean physicsThread_aborting;
    private boolean physicsThreadDead;
    private boolean physicsAllDone;
    private boolean waitForClumpToComeToRest;
    //
    private static boolean showAppliedForces = false;               // show or hide the gravity, collision and reaction forces applied to each object in the container;
    private static double timeToDieAfterContact = 2.0f;              // the calculations on the object and their sibling (in the same clump) cease after this time;
    private static double timeToDieAfterContactFast = 1.0f;          //;
    private static double timeSpeedUpFactor = 3.0f;                  // action happens faster than normal time;
    private static double timeSpeedUpFactorFast = 20.0f;             //;
    private static double collisionCoeficientDepletion = 0.1f;       // how much of the collision coeficient is lost when object is coming to rest;
    private static double collisionCoeficientDepletionFast = 0.3f;   //;
    private static double physicsRefreshRate = 0.005f;               // time in seconds between each calculation for all objects in the physics engine;
    private static double physicsRefreshRateFast = 0.0025f;          //;
    private static double interactionForceFactor = 0.6f;             // the relative distance : width of the object that triggers the collision forces;
    private static double speedDecayX = 0.4f;                        // speed from previous snapshot is reduced by this ammount to simulate attrition in the X Axis;
    private static double speedDecayY = 0.8f;                        // speed from previous snapshot is reduced by this amount to simulate air drag in the Y Axis;
    private static double bouncinessReactionFactor = 1.2f;           // when the object collides with ground, how much of the speed at that point : time is reflected upward;
    //
    private static int kMaxWrongAttempts = 2;
    private static double kReminderDelaySeconds = 5.0;
    //
    private static String kModeAutomaticCount = "1";
    private static String kModeChildCountChooseAnswer = "2";
    private static String kModeChildCountReachTarget = "3";
    private static String kModeChildCountTimeAttack = "4";
    private static String kModeChildCountType = "5";
    private static String kModeChildCountTimeTimeAttack = "6";
    private static String kTotalObjects = "total";
    private static String kMode = "mode";
    private static String kDemo = "demo";
    private static String kStartingObjectsInContainer = "begin";
    private static String kClump = "clump";
    private static String kMaxClumps = "maxclumps";
    private static String kConstantTrue = "true";
    private static String kPhysicsFixed = "fixed";
    private static String kPhysicsWakeUpTime = "wakeUpTime";
    private static String kPhysicsSpeed = "speed";
    private static String kPhysicsGravity = "gravity";
    private static String kPhysicsCollision = "collision";
    private static String kPhysicsCollisionCoeficient = "collisionCoeficient";
    private static String kPhysicsCollisionWithAtRestObject = "collisionWithAtRestObject";
    private static String kPhysicsBounced = "bounced";
    private static String kPhysicsDescentStarted = "descentStarted";
    private static String kPhysicsReaction = "reaction";
    private static String kPhysicsNewPosition = "newPosition";
    private static String kPhysicsNewSpeed = "newSpeed";
    private static String kPhysicsContact = "contact";
    private static String kPhysicsTimeToDie = "timeToDie";
    private static String kPhysicsAbsoluteLeft = "absoluteLeft";
    private static String kPhysicsAbsoluteRight = "absoluteRight";
    private static String kPhysicsAbsoluteBottom = "absoluteBottom";
    private static String kPhysicsAbsoluteTop = "absoluteTop";


    public void prepare ()
    {
        super.prepare();
        contactThreadDead = true;
        physicsThreadDead = true;
        doingDemoOverride = false;
        mode = parameters.get(kMode);
        startingObjectsInContainer = Integer.parseInt(parameters.get(kStartingObjectsInContainer));
        clumpOfObjectsForUnit = Integer.parseInt(parameters.get(kClump));
        maxClumpsOnScreen = Integer.parseInt(parameters.get(kMaxClumps));
        //
        if (mode.equals(kModeAutomaticCount) || mode.equals(kModeChildCountReachTarget))
        {
            totalObjectsForUnit = Integer.parseInt(parameters.get(kTotalObjects));
            hasDemo = parameters.get(kDemo).equalsIgnoreCase(kConstantTrue);
        }
        else if (mode.equals(kModeChildCountChooseAnswer))
        {
            totalObjectsForUnit = clumpOfObjectsForUnit * maxClumpsOnScreen;
            hasDemo = true;
        }
        //
        loadAudioXML(getConfigPath(String.format("event%saudio.xml", mode)));
        //
        colourTextNormal = objectDict.get("colour_text_normal").fillColor();
        colourTextHilite = objectDict.get("colour_text_hilite").fillColor();
        colourTextDisabled = objectDict.get("colour_text_disabled").fillColor();
        colourTextDraggable = objectDict.get("colour_text_draggable").fillColor();
        colourTextCorrect = objectDict.get("colour_text_correct").fillColor();
        colourTextTarget = objectDict.get("colour_text_target").fillColor();
        colourButtonNormal = objectDict.get("colour_number_button_normal").fillColor();
        colourButtonHilite = objectDict.get("colour_number_button_hilite").fillColor();
        colourButtonDisabled = objectDict.get("colour_number_button_disabled").fillColor();
        colourDashNormal = objectDict.get("colour_dash_normal").fillColor();
        colourDashHilite = objectDict.get("colour_dash_hilite").fillColor();
        //
        jar = (OBGroup) objectDict.get("jar");
        jarContainerPath = (OBPath) jar.objectDict.get("border_container");
        colourJarContainerBorder = jarContainerPath.strokeColor();
        //
        jarLabelContainer = jar.objectDict.get("label");
        jarLabelContainer.setOpacity(0.5f);
        //
        confirmArrow = objectDict.get("confirm_arrow");
        //
        targetLabelContainer = objectDict.get("target_box");
        targetLabel = action_createLabelForControl(targetLabelContainer, "8888", colourTextTarget, 1.2f);
        //
        attachControl(targetLabel);
        confirmArrow.hide();
        targetLabel.hide();
        targetLabelContainer.hide();
        //
        dragLabel = objectDict.get("drag_label");
        dragBar = objectDict.get("drag_bar");
        jarHotArea = objectDict.get("jar_hot_area");
        jarAppliedForce = objectDict.get("jar_applied_force");
        //
        events = new ArrayList<>();
        if (mode.equals(kModeAutomaticCount))
        {
            waitForClumpToComeToRest = true;
            dragBar.hide();
            PointF jarPosition = OC_Generic.copyPoint(jar.position());
            jarPosition.set(jarPosition.x, 0.5f * bounds().height());
            jar.setPosition(jarPosition);
            events.add("a");
            if (hasDemo) events.add("b");
            events.add("c");
            events.add("end");
        }
        else if (mode.equals(kModeChildCountChooseAnswer))
        {
            waitForClumpToComeToRest = false;
            dragBar.show();
            events.add("a");
            events.add("b");
        }
        else if (mode.equals(kModeChildCountReachTarget))
        {
            waitForClumpToComeToRest = false;
            dragBar.hide();
            confirmArrow.show();
            targetLabel.show();
            targetLabelContainer.show();
        }
        //
        jarLabel = action_createLabelForControl(jarLabelContainer, "8888", colourTextNormal, 1.2f);
        jarLabel.setString("");
        jarLabel.setZPosition(jar.zPosition() + 0.1f);
        attachControl(jarLabel);
        //
        clumps = new ArrayList<>();
        if (startingObjectsInContainer > 0)
        {
            OBControl peaTemplate = objectDict.get("pea");
            double absoluteLeft = jar.left() + 0.8f * peaTemplate.width();
            double absoluteRight = jar.right() - 0.8f * peaTemplate.width();
            double absoluteBottom = jar.bottom() - peaTemplate.height();
            PointF newPosition = new PointF((float) absoluteLeft, (float) absoluteBottom);
            for (int i = 0; i < startingObjectsInContainer; i++)
            {
                OBControl newPea = peaTemplate.copy();
                newPea.setPosition(newPosition);
                newPea.show();
                newPea.disable();
                attachControl(newPea);
                startPhysicsWithControl(newPea, false);
                clumps.add((OBGroup) newPea);
                newPosition.x += interactionForceFactor * peaTemplate.width() * 0.7f;
                if (newPosition.x > absoluteRight)
                {
                    newPosition.set((float) absoluteLeft, newPosition.y);
                    newPosition.y -= interactionForceFactor * peaTemplate.width() * 0.6f;
                }
            }
        }
        //
        jarLabel.setString(String.format("%d", startingObjectsInContainer));
        //
        hideControls("clump.*");
        hideControls("colour.*");
        hideControls("number_dash.*");
        hideControls("number_button");
        hideControls("pea");
        hideControls("place.*");
        //
        jarHotArea.hide();
        jarAppliedForce.hide();
        dragLabel.hide();
        //
        contactThreadRunning = false;
    }


    public void nextScene ()
    {
        contactThread_aborting = true;
        physicsThread_aborting = true;
        while (!contactThreadDead || !physicsThreadDead)
        {
            waitForSecsNoThrow(0.1f);

        }
        super.nextScene();
    }


    public void doReminderWithTimeStamp (long timeStamp) throws Exception
    {
        boolean isFirstPhaseOfMode2 = mode.equals(kModeChildCountChooseAnswer) && !isMode2Phase2;
        //
        if (mode.equals(kModeAutomaticCount) || isFirstPhaseOfMode2)
        {
            for (int i = 0; i < 3; i++)
            {
                lockScreen();
                jarContainerPath.setFillColor(Color.argb(0, 255, 255, 255));
                jarContainerPath.setStrokeColor(Color.rgb(255, 140, 0));
                jarContainerPath.setLineWidth(applyGraphicScale(5.0f));
                jarContainerPath.setOpacity(1.0f);
                unlockScreen();
                //
                waitForSecs(0.2f);
                if (_aborting) return;
                if (lastActionTakenTimestamp != timeStamp) break;
                //
                lockScreen();
                jarContainerPath.setStrokeColor(colourJarContainerBorder);
                jarContainerPath.setLineWidth(applyGraphicScale(1.0f));
                jarContainerPath.setOpacity(0.0f);
                unlockScreen();
                //
                waitForSecs(0.2f);
                if (_aborting) return;
                if (lastActionTakenTimestamp != timeStamp) break;
            }
            //
            lockScreen();
            jarContainerPath.setStrokeColor(colourJarContainerBorder);
            jarContainerPath.setLineWidth(applyGraphicScale(1.0f));
            jarContainerPath.setOpacity(0.0f);
            unlockScreen();
            //
            updateLastActionTakenTimeStamp();
            //
            OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            doReminderWithKey(currentEvent(), kReminderDelaySeconds, new RunLambdaWithTimestamp()
                            {
                                @Override
                                public void run (long timestamp) throws Exception
                                {
                                    doReminderWithTimeStamp(timestamp);
                                }
                            });
                        }
                    });
                }
            });
        }
        else if (mode.equals(kModeChildCountChooseAnswer))
        {
            playAudioScene("REMINDER2", 0, true);           // How many peas are in the jar?
            if (_aborting) return;
            if (lastActionTakenTimestamp != timeStamp) return;
            //
            waitForSecs(0.3f);
            if (_aborting) return;
            if (lastActionTakenTimestamp != timeStamp) return;
            //
            playAudioScene("REMINDER2", 1, false);          // Touch the correct number.
            for (int i = 0; i < 3; i++)
            {
                lockScreen();
                for (OBLabel label : answerLabels)
                {
                    label.setColour(colourTextHilite);

                }
                unlockScreen();
                //
                waitForSecs(0.3f);
                if (_aborting) return;
                if (lastActionTakenTimestamp != timeStamp) break;
                //
                lockScreen();
                for (OBLabel label : answerLabels)
                {
                    label.setColour(colourTextNormal);
                }
                unlockScreen();
                //
                waitForSecs(0.3f);
                if (_aborting) return;
                if (lastActionTakenTimestamp != timeStamp) break;
            }
            //
            lockScreen();
            for (OBLabel label : answerLabels)
            {
                label.setColour(colourTextNormal);

            }
            unlockScreen();
            //
            updateLastActionTakenTimeStamp();
        }
    }


    public void doMainXX () throws Exception
    {
        if (mode.equals(kModeAutomaticCount))
        {
            doAudio(currentEvent());
        }
        else if (mode.equals(kModeChildCountChooseAnswer))
        {
            isMode2Phase2 = false;
            if (currentEvent().equals("c"))
            {
                for (OBControl clump : clumps)
                {
                    clump.setProperty(kPhysicsFixed, true);
                }
            }
            else if (currentEvent().equals("d"))
            {
                playSfxAudio("pop_off", false);
                List objectsToBeRemoved = new ArrayList<>();
                List clumpsToBeRemoved = new ArrayList<>();
                //
                lockScreen();
                for (OBControl control : physicsControls)
                {
                    if ((boolean) control.propertyValue(kPhysicsFixed)) continue;
                    control.hide();
                    detachControl(control);
                    objectsToBeRemoved.add(control);
                    OBControl clump = (OBControl) control.propertyValue("clump");
                    if (!clumpsToBeRemoved.contains(clump))
                    {
                        clumpsToBeRemoved.add(clump);
                    }
                }
                //
                synchronized (physicsControls)
                {
                    physicsControls.removeAll(objectsToBeRemoved);
                }
                //
                clumps.removeAll(clumpsToBeRemoved);
                //
                synchronized (atRestObjects)
                {
                    atRestObjects.removeAll(objectsToBeRemoved);
                }
                //
                jarLabel.setString(String.format("%d", (int) physicsControls.size()));
                jarLabel.setColour(colourTextNormal);
                //
                unlockScreen();
                waitSFX();
            }
            maxClumpsOnScreen = totalClumpsForSecondStage();
            totalObjectsForUnit += clumpOfObjectsForUnit * maxClumpsOnScreen;
            populatePeasWithAnimation();
            if (currentEvent().equals("c"))
            {
                demo_previousAnswerCorrect();
            }
            else if (currentEvent().equals("d"))
            {
                demo_previousAnswerIncorrect();
            }
        }
        firstDropPlayed = false;
        //
        setStatus(STATUS_AWAITING_CLICK);
        updateLastActionTakenTimeStamp();
        //
        doReminderWithKey(currentEvent(), kReminderDelaySeconds, new RunLambdaWithTimestamp()
        {
            @Override
            public void run (long timestamp) throws Exception
            {
                doReminderWithTimeStamp(timestamp);
            }
        });
    }


    public int totalClumpsForSecondStage ()
    {
        int startingClumps = round(startingObjectsInContainer / clumpOfObjectsForUnit);
        int draggedClumps = maxClumpsOnScreen - startingClumps;
        int result = draggedClumps;
        if (clumpOfObjectsForUnit == 1)
        {
            if (draggedClumps <= 12) result -= 2;
            else if (draggedClumps <= 18) result -= 4;
            else result -= 6;
        }
        else if (clumpOfObjectsForUnit == 2)
        {
            if (draggedClumps <= 11) result -= 3;
            else if (draggedClumps <= 15) result -= 4;
            else result -= 5;
        }
        else if (clumpOfObjectsForUnit == 5)
        {
            if (draggedClumps <= 9) result -= 3;
            else if (draggedClumps <= 12) result -= 4;
            else result -= 5;
        }
        else if (clumpOfObjectsForUnit == 10)
        {
            if (draggedClumps <= 6) result -= 1;
            else result -= 2;
        }
        return result;
    }


    public List createDistractorsAndCorrectAnswer (int correctAnswer)
    {
        List result = new ArrayList<>();
        List allValues = new ArrayList<>();
        //
        if (clumpOfObjectsForUnit == 1)
        {
            allValues.add(correctAnswer + 12);
            allValues.add(correctAnswer - 5);
            allValues.add(correctAnswer + 7);
            allValues.add(correctAnswer - 3);
        }
        else if (clumpOfObjectsForUnit == 2)
        {
            allValues.add(correctAnswer + 14);
            allValues.add(correctAnswer - 10);
            allValues.add(correctAnswer + 8);
            allValues.add(correctAnswer - 4);
        }
        else if (clumpOfObjectsForUnit == 5)
        {
            allValues.add(correctAnswer + 30);
            allValues.add(correctAnswer - 10);
            allValues.add(correctAnswer + 15);
            allValues.add(correctAnswer - 15);
        }
        else if (clumpOfObjectsForUnit == 10)
        {
            allValues.add(correctAnswer + 50);
            allValues.add(correctAnswer - 30);
            allValues.add(correctAnswer + 20);
            allValues.add(correctAnswer - 10);
        }
        else
        {
            MainActivity.log("ERROR --> Unable to process clumpOfObjectsForUnit %d", clumpOfObjectsForUnit);
            return null;
        }
        List<Integer> randomAnswers = OBUtils.randomlySortedArray(allValues).subList(0, 2);
        randomAnswers.add(correctAnswer);
        //
        for (Integer answer : randomAnswers)
        {
            OBLabel label = action_createLabelForControl(dragLabel, answer.toString(), colourTextNormal, 1.2f);
            result.add(label);
        }
        return OBUtils.randomlySortedArray(result);
    }


    public void killAllObjects () throws Exception
    {
        synchronized (physicsControls)
        {
            synchronized (atRestObjects)
            {
                for (OBControl control : physicsControls)
                {
                    control.setProperty(kPhysicsTimeToDie, 1.0);
                    if (!atRestObjects.contains(control))
                    {
                        atRestObjects.add(control);
                    }
                }
            }
        }
        waitForSecs(0.3f);
    }


    public void returnObjectsToGroup (final OBGroup originalGroup, boolean waitForAnimation)
    {
        List<OBAnim> animations = new ArrayList<>();
        List<OBGroup> controlsToReturnToGroup = new ArrayList<>();
        List<OBControl> controls;
        //
        synchronized (physicsControls)
        {
            controls = new ArrayList();
            controls.addAll(physicsControls);
        }
        //
        for (OBControl control : controls)
        {
            if (control.propertyValue("original_group").equals(originalGroup))
            {
                PointF destination = OC_Generic.copyPoint((PointF) control.propertyValue("original_position"));
                OBAnim moveAnim = OBAnim.moveAnim(destination, control);
                animations.add(moveAnim);
                controlsToReturnToGroup.add((OBGroup) control);
            }
        }
        //
        synchronized (physicsControls)
        {
            physicsControls.removeAll(controlsToReturnToGroup);
        }
        //
        synchronized (atRestObjects)
        {
            atRestObjects.removeAll(controlsToReturnToGroup);
        }
        final List<OBGroup> controlsToReturnToGroup_final = controlsToReturnToGroup;
        //
        OBAnimationGroup.runAnims(animations, 0.3f, waitForAnimation, OBAnim.ANIM_EASE_IN_EASE_OUT, new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                lockScreen();
                originalGroup.setPosition((PointF) originalGroup.propertyValue("original_position"));
                for (OBGroup group : controlsToReturnToGroup_final)
                {
                    OBControl body = group.objectDict.get("body");
                    body.setRotation(0.0f);
                    group.setZPosition((float) group.propertyValue("original_zPosition"));
                    originalGroup.insertMember(group, 0, "pea");
                }
                originalGroup.enable();
                unlockScreen();
            }
        }, this);
    }


    public void populatePeasWithAnimation () throws Exception
    {
        if (physicsControls.size() >= totalObjectsForUnit)
        {
            return;
        }
        clumpTemplate = (OBGroup) objectDict.get(String.format("clump_%d", clumpOfObjectsForUnit));
        double safeRadius = 1.3f * Math.max(clumpTemplate.width(), clumpTemplate.height());
        //
        List<OBControl> newControls = new ArrayList<>();
        //
        lockScreen();
        boolean staticPlacement = true;
        if (staticPlacement)
        {
            List<OBControl> placements = sortedFilteredControls("place.*");
            for (int i = 0; i < maxClumpsOnScreen; i++)
            {
                if (physicsControls.size() + newControls.size() * clumpOfObjectsForUnit > totalObjectsForUnit)
                    break;
                //
                OBControl place = placements.get(i);
                OBControl newClump = clumpTemplate.copy();
                newClump.setPosition(OC_Generic.copyPoint(place.position()));
                newClump.setZPosition(jar.zPosition() + 0.1f);
                newClump.setProperty("original_position", OC_Generic.copyPoint(newClump.position()));
                for (OBControl control : newControls)
                {
                    double distance = PointDistance(control.position(), newClump.position());
                    if (distance < safeRadius)
                    {
                        PointF nudgeVector = OC_Generic.copyPoint(NormalisedVector(OB_Maths.DiffPoints(newClump.position(), control.position())));
                        PointF newPosition = OC_Generic.copyPoint(AddPoints(newClump.position(), ScalarTimesPoint((float) (safeRadius - distance), nudgeVector)));
                        newClump.setPosition(newPosition);
                    }
                }
                OBGroup clumpGroup = (OBGroup) newClump;
                boolean isClump = clumpGroup.filterMembers("pea") != null;
                if (isClump)
                {
                    for (OBControl sibling : clumpGroup.members)
                    {
                        sibling.setProperty("original_position", sibling.getWorldPosition());
                        sibling.setProperty("original_group", clumpGroup);
                        sibling.setProperty("original_zPosition", sibling.zPosition());
                    }
                }
                newControls.add(newClump);
            }
        }
        unlockScreen();
        //
        clumps.addAll((List<OBGroup>) (Object) newControls);
        //
        playSfxAudio("peas_on", false);
        lockScreen();
        for (OBControl pea : newControls)
        {
            attachControl(pea);
            pea.show();
        }
        unlockScreen();
        //
        waitSFX();
    }


    public void startContactThread ()
    {
        if (contactThreadRunning) return;
        //
        contactThreadRunning = true;
        contactThread_aborting = false;
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                try
                {
                    MainActivity.log("contactThread is now RUNNING");
                    //
                    contactThreadDead = false;
                    int counter = atRestObjects.size();
                    //
                    while (!_aborting && !contactThread_aborting)
                    {
                        if (atRestObjects == null)
                        {
                            waitForSecs(physicsRefreshRate);
                            continue;
                        }
                        //
                        boolean wasIncremented = false;
                        boolean wasDecremented = false;
                        synchronized (atRestObjects)
                        {
                            if (atRestObjects.size() >= counter + clumpOfObjectsForUnit)
                            {
                                wasIncremented = true;
                                counter = atRestObjects.size();
                            }
                            else if (atRestObjects.size() < counter)
                            {
                                wasDecremented = true;
                                counter = atRestObjects.size();
                            }
                        }
                        //
                        if (wasIncremented)
                        {
                            synchronized (physicsControls)
                            {
                                physicsAllDone = true;
                            }
                            if (doingDemoOverride)
                            {
                                waitForSecs(physicsRefreshRate);
                                continue;
                            }
                            //
                            if (mode.equals(kModeAutomaticCount))
                            {
                                final int counter_final = counter;
                                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                {
                                    public void run () throws Exception
                                    {
                                        playNumberIncrement();
                                        playNumberAudio();
                                        //
                                        lockScreen();
                                        jarLabel.setString(String.format("%d", counter_final));
                                        jarLabel.setColour(colourTextHilite);
                                        unlockScreen();
                                        //
                                        MainActivity.log("startContactThread: waiting for audio");
                                        //
                                        while (true)
                                        {
                                            waitForSecs(0.1f);
                                            if (!OBAudioManager.audioManager.isPlayingChannel(AM_MAIN_CHANNEL))
                                                break;
                                        }
                                        //
                                        MainActivity.log("startContactThread: waiting for audio DONE");
                                        //
                                        lockScreen();
                                        jarLabel.setColour(colourTextNormal);
                                        unlockScreen();
                                        //
                                        if (mode.equals(kModeAutomaticCount) && counter_final >= totalObjectsForUnit)
                                        {
                                            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                            {
                                                public void run () throws Exception
                                                {
                                                    animation_glowLabel();
                                                    nextScene();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else if (mode.equals(kModeChildCountChooseAnswer))
                            {
                                playSfxAudio("into_jar", false);
                                if (!firstDropPlayed)
                                {
                                    firstDropPlayed = true;
                                    playNumberAudio();
                                }
                            }
                            else
                            {
                                playSfxAudio("into_jar", true);
                            }
                        }
                        else if (wasDecremented)
                        {
                            if (mode.equals(kModeAutomaticCount))
                            {
                                playSfxAudio("number_decrement", false);
                                //
                                lockScreen();
                                jarLabel.setString(String.format("%d", (int) physicsControls.size()));
                                unlockScreen();
                            }
                        }
                        waitForSecs(physicsRefreshRate);
                    }
                }
                catch (Exception e)
                {
                    MainActivity.log("startContactThread:exception caught %", e.getMessage());
                    e.printStackTrace();
                }
                //
                if (!contactThread_aborting)
                {
                    MainActivity.log("contactThread crashed!");
                }
                //
                contactThreadDead = true;
                contactThreadRunning = false;
                //
                MainActivity.log("contactThread is now DEAD");
            }
        });
    }


    public void physicsEngine_step1 (List<OBControl> controls, double timeReference, double elapsedTime)
    {
        for (OBControl object : controls)
        {
            double timeToDie = (double) object.propertyValue(kPhysicsTimeToDie);
            if (timeToDie > 0 && timeToDie < timeReference) continue;
            PointF gravity = new PointF(0f, 9.8f);
            PointF collision = new PointF(0f, 0f);
            PointF reaction = new PointF(0f, 0f);
            PointF speed = (PointF) object.propertyValue(kPhysicsSpeed);
            PointF position = OC_Generic.copyPoint(object.position());
            PointF appliedForces = gravity;
            appliedForces = AddPoints(appliedForces, collision);
            appliedForces = AddPoints(appliedForces, reaction);
            PointF newSpeed = AddPoints(speed, ScalarTimesPoint((float) elapsedTime, appliedForces));
            PointF newPosition = OC_Generic.copyPoint(AddPoints(position, newSpeed));
            object.setProperty(kPhysicsGravity, gravity);
            object.setProperty(kPhysicsCollision, collision);
            object.setProperty(kPhysicsReaction, reaction);
            object.setProperty(kPhysicsNewSpeed, newSpeed);
            object.setProperty(kPhysicsNewPosition, newPosition);
        }
    }


    public void physicsEngine_step2 (List<OBControl> controls, double timeReference, double elapsedTime)
    {
        for (OBControl object : controls)
        {
            double timeToDie = (double) object.propertyValue(kPhysicsTimeToDie);
            //
            if (timeToDie > 0 && timeToDie < timeReference) continue;
            //
            double forceInteractionDistance = object.width() * interactionForceFactor;
            PointF collision = new PointF(0, 0);
            PointF speed = (PointF) object.propertyValue(kPhysicsSpeed);
            PointF position = OC_Generic.copyPoint(object.position());
            //
            for (OBControl otherObject : controls)
            {
                if (otherObject.equals(object)) continue;
                //
                PointF otherObjectPosition = OC_Generic.copyPoint(otherObject.position());
                double distance = PointDistance(position, otherObjectPosition);
                //
                if (distance < forceInteractionDistance)
                {
                    double otherTimeToDie = (double) otherObject.propertyValue(kPhysicsTimeToDie);
                    boolean collisionWithAtRestObject = false;
                    //
                    synchronized (atRestObjects)
                    {
                        collisionWithAtRestObject = atRestObjects.contains(otherObject) && (otherTimeToDie > 0 && otherTimeToDie < timeReference);
                    }
                    //
                    if (collisionWithAtRestObject)
                    {
                        object.setProperty(kPhysicsCollisionWithAtRestObject, true);
                    }
                    //
                    double collisionFactor = forceInteractionDistance - distance;
                    PointF collisionVector = OC_Generic.copyPoint(ScalarTimesPoint((float) collisionFactor, NormalisedVector(OB_Maths.DiffPoints(position, otherObjectPosition))));
                    collision = AddPoints(collision, collisionVector);
                }
            }
            //
            if (!(boolean) object.propertyValue(kPhysicsCollisionWithAtRestObject))
            {
                collision.set(collision.x * 0.25f, 0.0f);
            }
            else if (abs(collision.x) > 0 || abs(collision.y) > 0)
            {
                collision = AddPoints(collision, ScalarTimesPoint(0.1f, new PointF((float) (-speed.x / elapsedTime), (float) (-speed.y / elapsedTime))));
            }
            object.setProperty(kPhysicsCollision, collision);
        }
    }


    public void physicsEngine_step3 (List<OBGroup> controls, double timeReference, double elapsedTime)
    {
        for (OBGroup object : controls)
        {
            double timeToDie = (double) object.propertyValue(kPhysicsTimeToDie);
            //
            if (timeToDie > 0 && timeToDie < timeReference) continue;
            //
            double absoluteLeft = (double) object.propertyValue(kPhysicsAbsoluteLeft);
            double absoluteRight = (double) object.propertyValue(kPhysicsAbsoluteRight);
            double absoluteBottom = (double) object.propertyValue(kPhysicsAbsoluteBottom);
            double absoluteTop = (double) object.propertyValue(kPhysicsAbsoluteTop);
            double collisionCoeficient = (double) object.propertyValue(kPhysicsCollisionCoeficient);
            //
            PointF gravity = (PointF) object.propertyValue(kPhysicsGravity);
            PointF collision = (PointF) object.propertyValue(kPhysicsCollision);
            PointF reaction = (PointF) object.propertyValue(kPhysicsReaction);
            PointF speed = (PointF) object.propertyValue(kPhysicsSpeed);
            PointF position = OC_Generic.copyPoint(object.position());
            //
            boolean bounced = (boolean) object.propertyValue(kPhysicsBounced);
            boolean contact = (boolean) object.propertyValue(kPhysicsContact);
            //
            PointF appliedForces = gravity;
            appliedForces = AddPoints(appliedForces, collision);
            appliedForces = AddPoints(appliedForces, reaction);
            PointF newSpeed = AddPoints(speed, ScalarTimesPoint((float) elapsedTime, appliedForces));
            PointF newPosition = OC_Generic.copyPoint(AddPoints(position, newSpeed));
            //
            if (newPosition.y < absoluteTop && bounced && contact)
            {
                if (collision.x > 0 || collision.y > 0)
                {
                    collision = new PointF(0, 0);
                    reaction = new PointF(0, 0);
                    //
                    double depletionFactor = (doingDemoOverride) ? collisionCoeficientDepletionFast : collisionCoeficientDepletion;
                    //
                    collisionCoeficient -= depletionFactor;
                    //
                    if (collisionCoeficient < 0) collisionCoeficient = 0;
                }
            }
            //
            if (newPosition.y > absoluteBottom)
            {
                reaction = AddPoints(reaction, new PointF(gravity.x, (float) (-(gravity.y + bouncinessReactionFactor * (speed.y / elapsedTime)))));
                if (collision.y > 0)
                {
                    reaction = AddPoints(reaction, new PointF(0f, -collision.y));

                }
                physicsEngine_putClumpToRest(object, timeReference);
            }
            if (newPosition.x < absoluteLeft)
            {
                reaction = AddPoints(reaction, new PointF((float) (-speed.x / elapsedTime), 0f));
                if (collision.x < 0)
                {
                    reaction = AddPoints(reaction, new PointF(-collision.x, 0f));
                }
            }
            if (newPosition.x > absoluteRight)
            {
                reaction = AddPoints(reaction, new PointF((float) (-speed.x / elapsedTime), 0f));
                if (collision.x > 0)
                {
                    reaction = AddPoints(reaction, new PointF(-collision.x, 0f));
                }
            }
            //
            object.setProperty(kPhysicsReaction, reaction);
            object.setProperty(kPhysicsCollision, collision);
            object.setProperty(kPhysicsCollisionCoeficient, collisionCoeficient);
        }
    }


    public void physicsEngine_step4 (List<OBGroup> controls, double timeReference, double elapsedTime)
    {
        lockScreen();
        for (OBGroup object : controls)
        {
            double timeToDie = (double) object.propertyValue(kPhysicsTimeToDie);
            //
            if (timeToDie > 0 && timeToDie < timeReference)
            {
                synchronized (atRestObjects)
                {
                    if (!atRestObjects.contains(object)) atRestObjects.add(object);
                }
                continue;
            }
            //
            PointF gravity = (PointF) object.propertyValue(kPhysicsGravity);
            PointF collision = (PointF) object.propertyValue(kPhysicsCollision);
            PointF reaction = (PointF) object.propertyValue(kPhysicsReaction);
            PointF speed = (PointF) object.propertyValue(kPhysicsSpeed);
            PointF position = OC_Generic.copyPoint(object.position());
            //
            double collisionCoeficient = (double) object.propertyValue(kPhysicsCollisionCoeficient);
            double wakeUpTime = (double) object.propertyValue(kPhysicsWakeUpTime);
            boolean bounced = (boolean) object.propertyValue(kPhysicsBounced);
            boolean descentStarted = (boolean) object.propertyValue(kPhysicsDescentStarted);
            boolean contact = (boolean) object.propertyValue(kPhysicsContact);
            boolean collisionWithAtRestObject = (boolean) object.propertyValue(kPhysicsCollisionWithAtRestObject);
            //
            PointF appliedForces = new PointF(0, 0);
            //
            if (wakeUpTime <= timeReference)
            {
                appliedForces = gravity;
                appliedForces = AddPoints(appliedForces, collision);
                appliedForces = AddPoints(appliedForces, reaction);
                if (appliedForces.x == 0)
                {
                    speed.x *= speedDecayX;
                }
                if (appliedForces.y == 0)
                {
                    speed.y *= speedDecayY;
                }
            }
            //
            speed = AddPoints(speed, ScalarTimesPoint((float) (collisionCoeficient * elapsedTime), appliedForces));
            position = AddPoints(position, speed);
            //
            object.setProperty(kPhysicsSpeed, speed);
            //
            if (wakeUpTime <= timeReference)
            {
                if (!descentStarted)
                {
                    boolean hasDescentStartedNow = position.y > object.position().y;
                    object.setProperty(kPhysicsDescentStarted, hasDescentStartedNow);
                }
                else
                {
                    if (!bounced)
                    {
                        boolean hasItBouncedNow = position.y < object.position().y;
                        object.setProperty(kPhysicsBounced, hasItBouncedNow);
                    }
                    else
                    {
                        if (!contact && appliedForces.y <= 0 && collisionWithAtRestObject)
                        {
                            physicsEngine_putClumpToRest(object, timeReference);
                        }
                    }
                }
            }
            //
            double deltaX = position.x - object.position().x;
            double angle = (deltaX * 2f * (double) PI) / (2f * (double) PI * object.width() / 2f);         // i know the 2 * PI cancel each other, but it's for the sake of the proper equation;
            OBControl body = object.objectDict.get("body");
            body.rotation += angle;
            //
            double absoluteLeft = (double) object.propertyValue(kPhysicsAbsoluteLeft);
            double absoluteRight = (double) object.propertyValue(kPhysicsAbsoluteRight);
            double absoluteBottom = (double) object.propertyValue(kPhysicsAbsoluteBottom);
            //
            if (position.x < absoluteLeft) position.x = (float) absoluteLeft;
            if (position.x > absoluteRight) position.x = (float) absoluteRight;
            if (position.y > absoluteBottom) position.y = (float) absoluteBottom;
            //
            object.setPosition(position);
            //
            if (showAppliedForces)
            {
                physicsEngine_showAppliedForces(object);
            }
        }
        unlockScreen();
    }


    public void physicsEngine_putClumpToRest (OBGroup object, double timeReference)
    {
        List<OBGroup> siblings = (List<OBGroup>) object.propertyValue("siblings");
        //
        double timeToDie = (doingDemoOverride) ? timeToDieAfterContactFast : timeToDieAfterContact;
        //
        if (siblings != null)
        {
            object.setProperty(kPhysicsContact, true);
            for (OBControl sibling : siblings)
            {
                double currentTimeToDie = (double) sibling.propertyValue(kPhysicsTimeToDie);
                //
                if (currentTimeToDie > 0) continue;
                //
                sibling.setProperty(kPhysicsContact, true);
                sibling.setProperty(kPhysicsTimeToDie, timeReference + timeToDie);
                //
                synchronized (atRestObjects)
                {
                    if (!atRestObjects.contains(sibling)) atRestObjects.add(sibling);
                }
            }
        }
        else
        {
            object.setProperty(kPhysicsContact, true);
            object.setProperty(kPhysicsTimeToDie, timeReference + timeToDie);
            synchronized (atRestObjects)
            {
                if (!atRestObjects.contains(object)) atRestObjects.add(object);
            }
        }
    }


    public void physicsEngine_showAppliedForces (OBControl object)
    {
        PointF gravity = (PointF) object.propertyValue(kPhysicsGravity);
        PointF collision = (PointF) object.propertyValue(kPhysicsCollision);
        PointF reaction = (PointF) object.propertyValue(kPhysicsReaction);
        PointF position = (PointF) OC_Generic.copyPoint(object.position());
        //
        object.setBorderColor(Color.rgb(255, 140, 0));
        object.setBorderWidth(2.0f);
        PointF centreOfMass = new PointF(object.width() / 2f, object.height() / 2f);
        Path newPathForGravity = new Path();
        newPathForGravity.moveTo(centreOfMass.x, centreOfMass.y);
        PointF destination = AddPoints(centreOfMass, gravity);
        newPathForGravity.lineTo(destination.x, destination.y);
        //
        OBPath gravityForce = (OBPath) object.propertyValue("gravityForce");
        if (gravityForce == null)
        {
            gravityForce = new OBPath(newPathForGravity, object.width(), object.height(), position.x, position.y);
            gravityForce.setStrokeColor(Color.rgb(255, 0, 0));
            gravityForce.setLineWidth(2.0f);
            gravityForce.setZPosition(object.zPosition() + 0.1f);
            attachControl(gravityForce);
            object.setProperty("gravityForce", gravityForce);
        }
        else
        {
            gravityForce.setPath(newPathForGravity);
            gravityForce.setPosition(position);
        }
        //
        Path newPathForCollision = new Path();
        newPathForCollision.moveTo(centreOfMass.x, centreOfMass.y);
        destination = AddPoints(centreOfMass, collision);
        newPathForCollision.lineTo(destination.x, destination.y);
        //
        OBPath collisionForce = (OBPath) object.propertyValue("collisionForce");
        if (collisionForce == null)
        {
            collisionForce = new OBPath(newPathForCollision, object.width(), object.height(), position.x, position.y);
            collisionForce.setStrokeColor(Color.rgb(0, 255, 0));
            collisionForce.setLineWidth(2.0f);
            collisionForce.setZPosition(object.zPosition() + 0.1f);
            attachControl(collisionForce);
            object.setProperty("collisionForce", collisionForce);
        }
        else
        {
            collisionForce.setPath(newPathForCollision);
            collisionForce.setPosition(position);
        }
        //
        Path newPathForReaction = new Path();
        newPathForReaction.moveTo(centreOfMass.x, centreOfMass.y);
        destination = AddPoints(centreOfMass, reaction);
        newPathForReaction.lineTo(destination.x, destination.y);
        OBPath reactionForce = (OBPath) object.propertyValue("reactionForce");
        if (reactionForce == null)
        {
            reactionForce = new OBPath(newPathForReaction, object.width(), object.height(), position.x, position.y);
            reactionForce.setStrokeColor(Color.rgb(0, 0, 255));
            reactionForce.setLineWidth(2.0f);
            reactionForce.setZPosition(object.zPosition() + 0.1f);
            attachControl(reactionForce);
            object.setProperty("reactionForce", reactionForce);
        }
        else
        {
            reactionForce.setPath(newPathForReaction);
            reactionForce.setPosition(position);
        }
    }


    public void physicsEngine_waitUntilAllAtRest ()
    {
        while (true)
        {
            boolean isDone;
            synchronized (physicsControls)
            {
                isDone = physicsAllDone;
            }
            if (isDone)
            {
                MainActivity.log("physicsEngine_waitUntilAllAtRest:DONE");
                return;
            }
            waitForSecsNoThrow(0.05f);
        }
    }


    public void startPhysicsWithControl (OBControl control)
    {
        startPhysicsWithControl(control, true);
    }


    public void startPhysicsWithControl (OBControl control, boolean startContactThread)
    {
        if (physicsControls == null) physicsControls = new ArrayList<>();
        //
        OBGroup clump = (OBGroup) control;
        List<OBControl> objectsToBeAddedToPhysicsControls = new ArrayList<>();
        List<OBControl> objectsInClump = clump.filterMembers("pea");
        //
        if (objectsInClump.size() > 0)
        {
            List<OBControl> members = new ArrayList(clump.members);
            for (OBControl object : members)
            {
                clump.removeMember(object);
                attachControl(object);
                object.setProperty("siblings", members);
                object.setProperty("clump", control);
                //
                objectsToBeAddedToPhysicsControls.add(object);
            }
        }
        else
        {
            objectsToBeAddedToPhysicsControls.add(control);
        }
        //
        if (atRestObjects != null)
        {
            synchronized (atRestObjects)
            {
                atRestObjects.removeAll(objectsToBeAddedToPhysicsControls);
            }
        }
        //
        if (physicsControls != null)
        {
            synchronized (physicsControls)
            {
                physicsControls.removeAll(objectsToBeAddedToPhysicsControls);
            }
        }
        //
        for (OBControl objectToBeAdded : objectsToBeAddedToPhysicsControls)
        {
            PointF initialSpeed = new PointF(0, 0);
            double timeReference = getCurrentTimeDouble();
            boolean descentStarted = false;
            double timeToDie = 0.0;
            boolean fixed = false;
            //
            if (startContactThread)
            {
                double initialSpeedFactor = clumpOfObjectsForUnit * 0.25;
                PointF vectorToAppliedForce = OC_Generic.copyPoint(OB_Maths.DiffPoints(jarAppliedForce.position(), objectToBeAdded.position()));
                initialSpeed = ScalarTimesPoint((float) initialSpeedFactor, NormalisedVector(vectorToAppliedForce));
                initialSpeed.x *= 0.5;
                double distanceToAppliedForce = PointDistance(jarAppliedForce.position(), objectToBeAdded.position());
                timeReference += 0.1 * distanceToAppliedForce / (jar.width() * 0.5);
            }
            else
            {
                descentStarted = true;
                timeToDie = 1.0;
                fixed = true;
            }
            //
            double absoluteLeft = jar.left() + 0.8 * objectToBeAdded.width();
            double absoluteRight = jar.right() - 0.8 * objectToBeAdded.width();
            double absoluteBottom = jar.bottom() - objectToBeAdded.height();
            double absoluteTop = 0;
            //
            objectToBeAdded.setProperty(kPhysicsFixed, fixed);
            objectToBeAdded.setProperty(kPhysicsContact, false);
            objectToBeAdded.setProperty(kPhysicsBounced, false);
            objectToBeAdded.setProperty(kPhysicsDescentStarted, descentStarted);
            objectToBeAdded.setProperty(kPhysicsCollisionWithAtRestObject, false);
            objectToBeAdded.setProperty(kPhysicsTimeToDie, timeToDie);
            objectToBeAdded.setProperty(kPhysicsWakeUpTime, timeReference);
            objectToBeAdded.setProperty(kPhysicsSpeed, initialSpeed);
            objectToBeAdded.setProperty(kPhysicsGravity, new PointF(0f, 0f));
            objectToBeAdded.setProperty(kPhysicsCollision, new PointF(0f, 0f));
            objectToBeAdded.setProperty(kPhysicsCollisionCoeficient, 1.0);
            objectToBeAdded.setProperty(kPhysicsReaction, new PointF(0f, 0f));
            objectToBeAdded.setProperty(kPhysicsNewPosition, new PointF(0f, 0f));
            objectToBeAdded.setProperty(kPhysicsNewSpeed, new PointF(0f, 0f));
            objectToBeAdded.setProperty(kPhysicsAbsoluteLeft, absoluteLeft);
            objectToBeAdded.setProperty(kPhysicsAbsoluteRight, absoluteRight);
            objectToBeAdded.setProperty(kPhysicsAbsoluteBottom, absoluteBottom);
            objectToBeAdded.setProperty(kPhysicsAbsoluteTop, absoluteTop);
            objectToBeAdded.setZPosition(jar.zPosition() - 0.1f - randomInt(1, 100) * 0.00001f);
        }
        //
        synchronized (physicsControls)
        {
            physicsControls.addAll(objectsToBeAddedToPhysicsControls);
            physicsAllDone = false;
        }
        //
        if (startContactThread)
        {
            startContactThread();
            if (mode.equals(kModeChildCountChooseAnswer) && !doingDemoOverride)
            {
                lockScreen();
                jarLabel.hide();
                jarLabelContainer.hide();
                unlockScreen();
            }
        }
        //
        preparePlayNumberIncrement();
        prepareNumberAudio((int) physicsControls.size());
        //
        if (physicsThreadRunning) return;
        //
        physicsThreadRunning = true;
        physicsThread_aborting = false;
        //
        if (atRestObjects == null) atRestObjects = new ArrayList<>();
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                try
                {
                    MainActivity.log("physicsThread is now RUNNING");
                    //
                    physicsThreadDead = false;
                    double timeReference = getCurrentTimeDouble();
                    //
                    double refreshInterval = (doingDemoOverride) ? physicsRefreshRateFast : physicsRefreshRate;
                    //
                    waitForSecs(refreshInterval);
                    //
                    while (!_aborting && !physicsThread_aborting)
                    {
                        List controls;
                        synchronized (physicsControls)
                        {
                            controls = new ArrayList(physicsControls);
                        }
                        //
                        double newTimeReference = getCurrentTimeDouble();
                        double speedUpFactor = (doingDemoOverride) ? timeSpeedUpFactorFast : timeSpeedUpFactor;
                        double elapsedTime = (newTimeReference - timeReference) * speedUpFactor;
                        //
                        timeReference = newTimeReference;
                        //
                        physicsEngine_step1(controls, timeReference, elapsedTime);
                        physicsEngine_step2(controls, timeReference, elapsedTime);
                        physicsEngine_step3(controls, timeReference, elapsedTime);
                        physicsEngine_step4(controls, timeReference, elapsedTime);
                    }
                    waitForSecs(refreshInterval);
                }
                catch (Exception e)
                {
                    MainActivity.log("startPhysicsWithControl: Exception caught %s", e.getMessage());
                    e.printStackTrace();
                }
                //
                if (!physicsThread_aborting)
                {
                    MainActivity.log("physicsThread crashed!");
                }
                //
                physicsThreadDead = true;
                physicsThreadRunning = false;
                //
                MainActivity.log("physicsThread is now DEAD");
            }
        });
    }


    public void mode2_phase2 () throws Exception
    {
        setStatus(STATUS_BUSY);
        //
        isMode2Phase2 = true;
        loadPointer(POINTER_MIDDLE);
        //
        answerLabels = createDistractorsAndCorrectAnswer(physicsControls.size());
        double totalWidth = bounds().width();
        double consumedWidth = 0;
        for (OBLabel label : answerLabels) consumedWidth += label.width();
        double availableWidth = totalWidth - consumedWidth;
        double slottedWidth = availableWidth / (answerLabels.size() + 1);
        double newX = slottedWidth;
        //
        List<OBAnim> animations = new ArrayList<>();
        //
        lockScreen();
        for (OBLabel label : answerLabels)
        {
            newX += label.width() / 2;
            label.setPosition(new PointF((float) newX, dragBar.position().y));
            label.show();
            label.setProperty("original_position", OC_Generic.copyPoint(label.position()));
            OBAnim moveAnim = OBAnim.moveAnim(label.position(), label);
            animations.add(moveAnim);
            PointF newPosition = OC_Generic.copyPoint(label.position());
            newPosition.x += bounds().width();
            label.setPosition(newPosition);
            label.setZPosition(dragBar.zPosition() + 0.1f);
            attachControl(label);
            newX += label.width() / 2 + slottedWidth;
        }
        unlockScreen();
        //
        playAudioScene("PROMPT", 0, false);             // How many peas are : the jar now?;
        OC_Generic.pointer_moveToObject(jar, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        double pointerY = dragBar.position().y + (answerLabels.get(0)).height() * 0.5f;
        playSfxAudio("slide", false);
        movePointerToPoint(new PointF(0.1f * bounds().width(), (float) pointerY), 0.3f, false);
        for (OBAnim animation : animations)
        {
            OBAnimationGroup.runAnims(Arrays.asList(animation), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(randomNumberBetween(5, 10) * 0.01f);
        }
        waitForSecs(0.3f);
        //
        playAudioScene("PROMPT", 1, false);             // Choose the correct number.;
        movePointerToPoint(new PointF(0.9f * bounds().width(), (float) pointerY), 0.6f, true);
        waitAudio();
        //
        thePointer.hide();
        List replayAudio = getAudioForScene(currentEvent(), "REPEAT2");
        setReplayAudio(replayAudio);
        updateLastActionTakenTimeStamp();
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public Object findPea (PointF pt)
    {
        return finger(0, 2, (List<OBControl>) (Object) clumps, pt, true);
    }

    public Object findJar (PointF pt)
    {
        return finger(0, 2, (List<OBControl>) (Object) Arrays.asList(jar), pt, true);
    }

    public Object findLabel (PointF pt)
    {
        return finger(0, 2, (List<OBControl>) (Object) answerLabels, pt, true);
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            updateLastActionTakenTimeStamp();
            final Object pea = findPea(pt);
            if (pea != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkDragTarget((OBControl) pea, pt);
                    }
                });
            }
            else
            {
                final Object label = findLabel(pt);
                if (label != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            checkLabel((OBLabel) label);
                        }
                    });
                }
            }
        }
    }


    public void checkDragTarget (OBControl targ, PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target = targ;
        OC_Generic.sendObjectToTop(targ, this);
        targ.animationKey = new Date().getTime();
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }


    public void checkLabel (OBLabel label) throws Exception
    {
        setStatus(STATUS_CHECKING);
        label.disable();
        if (Integer.parseInt(label.text()) == physicsControls.size())
        {
            playSfxAudio("ping", false);
            //
            lockScreen();
            jarLabel.setString(String.format("%d", (int) physicsControls.size()));
            jarLabel.setColour(colourTextNormal);
            jarLabel.show();
            jarLabelContainer.show();
            unlockScreen();
            //
            waitSFX();
            //
            playAudio(String.format("n_%d", (int) physicsControls.size()));
            waitAudio();
            //
            gotItRightBigTick(true);
            //
            playSfxAudio("number_off", false);
            //
            lockScreen();
            jarLabel.setColour(colourTextCorrect);
            for (OBLabel otherLabel : answerLabels)
            {
                otherLabel.hide();
            }
            unlockScreen();
            //
            waitForSecs(0.3f);
            //
            if (!events.contains("end"))
            {
                events.add("c");
                events.add("end");
            }
            //
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            //
            lockScreen();
            label.setColour(colourTextDisabled);
            unlockScreen();
            //
            waitSFX();
            waitForSecs(0.3f);
            //
            mode2_wrongAnswer();
            //
            if (!events.contains("end"))
            {
                events.add("d");
                events.add("end");
            }
            nextScene();
        }
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void checkClumpDragAtPoint (PointF pt) throws Exception
    {
        setStatus(STATUS_CHECKING);
        OBGroup clump = (OBGroup) target;
        RectF dropZone = jarHotArea.frame;
        if (dropZone.contains(clump.position().x, clump.position().y))
        {
            clump.disable();
            startPhysicsWithControl(clump);
            if (waitForClumpToComeToRest)
            {
                physicsEngine_waitUntilAllAtRest();
            }
            boolean allGone = true;
            for (OBControl control : clumps)
            {
                if (control.isEnabled())
                {
                    allGone = false;
                    break;
                }
            }
            if (allGone)
            {
                if (mode.equals(kModeAutomaticCount))
                {
                    waitForSecs(0.3f);
                    waitAudio();
                    waitForSecs(0.3f);
                    populatePeasWithAnimation();
                }
                else if (mode.equals(kModeChildCountChooseAnswer))
                {
                    mode2_phase2();
                }
            }
        }
        else
        {
            PointF destination = OC_Generic.copyPoint((PointF) clump.propertyValue("original_position"));
            OBAnim moveAnim = OBAnim.moveAnim(destination, clump);
            OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void touchUpAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            updateLastActionTakenTimeStamp();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    checkClumpDragAtPoint(pt);
                }
            });
        }
    }


    public void demoa () throws Exception
    {
        setStatus(STATUS_BUSY);
        startContactThread();
        if (mode.equals(kModeAutomaticCount) || mode.equals(kModeChildCountChooseAnswer))
        {
            intro_mode1_mode2();
        }
        nextScene();
    }


    public void demob () throws Exception
    {
        setStatus(STATUS_BUSY);
        if (mode.equals(kModeAutomaticCount))
        {
            demo_mode1();
        }
        else if (mode.equals(kModeChildCountChooseAnswer))
        {
            demo_mode2();
        }
    }


    public void intro_mode1_mode2 () throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false);                     // Let’s practice counting!                     ||   Now drag these peas to the jar, and keep counting.;
        movePointerToRestingPosition(0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        populatePeasWithAnimation();
        //
        lockScreen();
        jarLabel.setString(String.format("%d", physicsControls.size()));
        unlockScreen();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);                     // This shows the number of peas in the jar.   ||  Remember, we are starting from this number!;
        OC_Generic.pointer_moveToObject(jarLabel, -5, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForAudio();
        waitForSecs(0.3f);
        //
        playAudio(String.format("n_%d", physicsControls.size()));
        lockScreen();
        jarLabel.setColour(colourTextHilite);
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        jarLabel.setColour(colourTextNormal);
        unlockScreen();
        waitForSecs(0.3f);
        //
        if (clumpOfObjectsForUnit > 1)
        {
            int audioIndex = (clumpOfObjectsForUnit == 2) ? 0 : (clumpOfObjectsForUnit == 5) ? 1 : 2;
            playAudioScene("DEMO2", audioIndex, false);     // These peas are in twos.                       || These peas are in fives.              | These peas are in tens.;
            movePointerToPoint(new PointF(0.7f * bounds().width(), 0.8f * bounds().height()), 0, 0.6f, true);
            waitAudio();
            waitForSecs(0.3f);
        }
        if (!hasDemo)
        {
            hidePointer();
        }
    }


    public void demo_mode1 () throws Exception
    {
        setStatus(STATUS_BUSY);
        playAudioScene("DEMO", 0, false);           // Now watch me!
        waitAudio();
        waitForSecs(0.3f);
        //
        List<OBGroup> randomArray = (List<OBGroup>) (Object) OBUtils.randomlySortedArray(clumps);
        OBGroup clump1 = null;
        for (OBControl control : randomArray)
        {
            if (control.isEnabled())
            {
                clump1 = (OBGroup) control;
                break;
            }
        }
        clump1.disable();
        PointF dropPosition = getRandomDropPointInJarForControl(clump1);
        //
        doingDemoOverride = true;
        OC_Generic.pointer_moveToObject(clump1, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        OC_Generic.pointer_moveToPointWithObject(clump1, dropPosition, 0, 0.9f, true, this);
        movePointerToRestingPosition(0.6f, false);
        //
        startPhysicsWithControl(clump1);
        physicsEngine_waitUntilAllAtRest();
        //
        playNumberIncrement();
        //
        lockScreen();
        jarLabel.setString(String.format("%d", (int) physicsControls.size()));
        jarLabel.setColour(colourTextHilite);
        unlockScreen();
        //
        playNumberAudio();
        waitAudio();
        //
        lockScreen();
        jarLabel.setColour(colourTextNormal);
        unlockScreen();
        waitForSecs(0.3f);
        //
        OBGroup clump2 = null;
        for (OBControl control : randomArray)
        {
            if (control.isEnabled())
            {
                clump2 = (OBGroup) control;
                break;
            }
        }
        clump2.disable();
        dropPosition = getRandomDropPointInJarForControl(clump2);
        OC_Generic.pointer_moveToObject(clump2, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        OC_Generic.pointer_moveToPointWithObject(clump2, dropPosition, 0, 0.9f, true, this);
        movePointerToRestingPosition(0.6f, false);
        //
        startPhysicsWithControl(clump2);
        physicsEngine_waitUntilAllAtRest();
        //
        playNumberIncrement();
        //
        lockScreen();
        jarLabel.setString(String.format("%d", (int) physicsControls.size()));
        jarLabel.setColour(colourTextHilite);
        unlockScreen();
        //
        playNumberAudio();
        waitAudio();
        //
        lockScreen();
        jarLabel.setColour(colourTextNormal);
        unlockScreen();
        waitForSecs(0.3f);
        //
        doingDemoOverride = false;
        killAllObjects();
        playAudioScene("DEMO", 1, false); // Your turn!;
        movePointerToRestingPosition(0.6f, true);
        //
        returnObjectsToGroup(clump1, true);
        returnObjectsToGroup(clump2, true);
        hidePointer();
        waitAudio();
        waitForSecs(0.3f);
        //
        nextScene();
    }


    public void demo_mode2 () throws Exception
    {
        setStatus(STATUS_BUSY);
        playAudioScene("DEMO", 0, false);                 // You must drag all the peas to the jar!;
        movePointerToPoint(new PointF(0.9f * bounds().width(), 0.7f * bounds().height()), 0, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);               //And count by yourself
        movePointerToPoint(new PointF(0.9f * bounds().width(), 0.8f * bounds().height()), 0, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        hidePointer();
        //
        playAudioScene("DEMO", 2, false);               //Get ready!;
        waitAudio();
        waitForSecs(0.3f);
        //
        List replayAudio = (List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT");
        setReplayAudio(replayAudio);
        //
        setStatus(STATUS_AWAITING_CLICK);
        updateLastActionTakenTimeStamp();
        //
        doReminderWithKey(currentEvent(), kReminderDelaySeconds, new RunLambdaWithTimestamp()
        {
            @Override
            public void run (long timestamp) throws Exception
            {
                doReminderWithTimeStamp(timestamp);
            }
        });
    }


    public void demoend () throws Exception
    {
        int audioIndex = 0;                                 // Good! We practised counting!;
        if (clumpOfObjectsForUnit >= 2) audioIndex++;       // Good! We practised counting : twos!;
        if (clumpOfObjectsForUnit >= 5) audioIndex++;       // Good! We practised counting : fives!;
        if (clumpOfObjectsForUnit >= 10) audioIndex++;      // Good! We practised counting : tens!;
        playAudioScene("DEMO", audioIndex, true);
        waitForSecs(0.3f);
        //
        nextScene();
    }


    public void animation_glowLabel () throws Exception
    {
        OBAnim glowAnimOn = OBAnim.colourAnim("colour", colourTextHilite, jarLabel);
        OBAnim glowAnimOff = OBAnim.colourAnim("colour", colourTextNormal, jarLabel);
        playSfxAudio("peas_final", false);
        for (int i = 0; i < 3; i++)
        {
            OBAnimationGroup.runAnims(Arrays.asList(glowAnimOn), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            OBAnimationGroup.runAnims(Arrays.asList(glowAnimOff), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
    }


    public void mode2_wrongAnswer () throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        playAudioScene("INCORRECT", 0, false);                  // Let’s find the correct answer.;
        waitAudio();
        waitForSecs(0.3f);
        //
        playSfxAudio("peas_out", false);
        for (OBGroup group : clumps)
        {
            if ((boolean) group.propertyValue(kPhysicsFixed)) continue;
            returnObjectsToGroup(group, false);
        }
        waitForSecs(0.5f);
        //
        playSfxAudio("label_on", false);
        lockScreen();
        jarLabel.show();
        jarLabelContainer.show();
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        //
        playAudioScene("INCORRECT", 1, false);                  // The jar had this many peas.;
        OC_Generic.pointer_moveToObject(jarLabelContainer, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudio(String.format("n_%d", physicsControls.size()));
        lockScreen();
        jarLabel.setColour(colourTextHilite);
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        jarLabel.setColour(colourTextNormal);
        unlockScreen();
        waitForSecs(0.3f);
        //
        playAudioScene("INCORRECT", 2, false);                  // You dragged all these.;
        movePointerToPoint(new PointF(0.9f * bounds().width(), 0.7f * bounds().height()), 0, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("INCORRECT", 3, false);                  //Now look and listen!;
        hidePointer();
        waitAudio();
        waitForSecs(0.3f);
        //
        PointF destination = jar.getWorldPosition();
        doingDemoOverride = true;
        int startingCounter = physicsControls.size();
        int groupCounter = 1;
        for (OBGroup group : clumps)
        {
            if ((boolean) group.propertyValue(kPhysicsFixed)) continue;
            //
            OBAnim moveAnim = OBAnim.moveAnim(destination, group);
            OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            startPhysicsWithControl(group);
            physicsEngine_waitUntilAllAtRest();
            playSfxAudio("number_increment", false);
            lockScreen();
            jarLabel.setString(String.format("%d", (int) (startingCounter + groupCounter * clumpOfObjectsForUnit)));
            jarLabel.setColour(colourTextHilite);
            unlockScreen();
            //
            playAudio(String.format("n_%d", (startingCounter + groupCounter * clumpOfObjectsForUnit)));
            waitAudio();
            //
            lockScreen();
            jarLabel.setColour(colourTextNormal);
            unlockScreen();
            groupCounter++;
            waitForSecs(0.3f);
        }
        doingDemoOverride = false;
        waitForSecs(0.3f);
        //
        animation_glowLabel();
        OBLabel correctLabel = null;
        for (OBLabel label : answerLabels)
        {
            if (label.text().equals(jarLabel.text()))
            {
                correctLabel = label;
                break;
            }
        }
        playAudioScene("INCORRECT", 4, false);              // This is the correct answer.;
        OC_Generic.pointer_moveToObject(correctLabel, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), false, this);
        for (int i = 0; i < 3; i++)
        {
            lockScreen();
            correctLabel.setColour(colourTextCorrect);
            unlockScreen();
            waitForSecs(0.3f);
            //
            lockScreen();
            correctLabel.setColour(colourTextNormal);
            unlockScreen();
            waitForSecs(0.3f);
        }
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudio(String.format("n_%d", physicsControls.size()));
        //
        lockScreen();
        correctLabel.setColour(colourTextHilite);
        unlockScreen();
        waitAudio();
        //
        lockScreen();
        correctLabel.setColour(colourTextNormal);
        unlockScreen();
        waitForSecs(0.3f);
        //
        hidePointer();
        waitForSecs(0.7f);
        //
        playSfxAudio("number_off", false);
        //
        lockScreen();
        jarLabel.setColour(colourTextNormal);
        for (OBLabel otherLabel : answerLabels)
        {
            otherLabel.hide();
        }
        unlockScreen();
        waitForSecs(0.3f);
    }


    public void demo_previousAnswerCorrect () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false);         // Now drag these peas to the jar, and keep counting.
        movePointerToRestingPosition(0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        hidePointer();
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demo_previousAnswerIncorrect () throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false);         // Now drag these peas to the jar, and count.;
        movePointerToRestingPosition(0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);         // Remember, we are starting from this number!;
        OC_Generic.pointer_moveToObject(jarLabelContainer, 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudio(String.format("n_%d", physicsControls.size()));
        //
        lockScreen();
        jarLabel.setColour(colourTextHilite);
        unlockScreen();
        //
        waitAudio();
        //
        lockScreen();
        jarLabel.setColour(colourTextNormal);
        unlockScreen();
        waitForSecs(0.3f);
        //
        hidePointer();
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void hidePointer ()
    {
        movePointerToPoint(new PointF(1.1f * bounds().width(), 1.1f * bounds().height()), 0, 0.6f, false);
    }


    public void movePointerToRestingPosition (double time, boolean wait)
    {
        movePointerToPoint(new PointF(0.85f * bounds().width(), 0.8f * bounds().height()), (float) time, wait);
    }


    public PointF getRandomDropPointInJarForControl (OBControl control)
    {
        PointF dropPosition = new PointF(0, 0);
        RectF dropFrame = jarContainerPath.getWorldFrame();
        RectF labelContainerFrame = jarLabelContainer.getWorldFrame();
        double delta = labelContainerFrame.left - dropFrame.left;
        dropFrame.left = labelContainerFrame.left;
        dropFrame.right -= delta;
        while (!_aborting)
        {
            dropPosition.set(dropFrame.left + randomInt(1, 100) * 0.01f * dropFrame.right, dropFrame.top + randomInt(1, 100) * 0.01f * dropFrame.bottom);
            RectF futureFrame = new RectF(dropPosition.x - control.width() / 2, dropPosition.y - control.height() / 2, control.width(), control.height());
            if (labelContainerFrame.intersect(futureFrame))
            {
                continue;
            }
            else if (!dropFrame.contains(futureFrame))
            {
                continue;
            }
            break;
        }
        return dropPosition;
    }


    public void playNumberIncrement ()
    {
        OBAudioManager.audioManager.playOnChannel(AM_SFX_CHANNEL);
    }


    public void preparePlayNumberIncrement ()
    {
        String audioFile = getAudioForScene("sfx", "number_increment").get(0);
        OBAudioManager.audioManager.prepareForChannel(audioFile, AM_SFX_CHANNEL);
    }

    public void prepareNumberAudio (int number)
    {

        OBAudioManager.audioManager.prepareForChannel(String.format("n_%d", number), AM_MAIN_CHANNEL);
    }

    public void playNumberAudio ()
    {
        OBAudioManager.audioManager.playOnChannel(AM_MAIN_CHANNEL);
    }

}