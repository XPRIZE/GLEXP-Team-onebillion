package org.onebillion.xprz.mainui.x_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 21/06/16.
 */
public class X_Counting5and10_S4g extends XPRZ_Generic_SelectCorrectObject
{
    OBGroup alignmentGroup;
    int correctAnswer;


    public X_Counting5and10_S4g()
    {
        super();
    }


    public String action_getScenesProperty()
    {
        return "scenes2";
    }


    public String action_getObjectPrefix()
    {
        return "label";
    }


    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        //
        for (OBControl number : filterControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number, 1.0f);
            number.setProperty("label", label);
        }
        //
        OBControl previous = null;
        List<OBControl> groups = sortedFilteredControls("number.*");
        //
        for (OBControl number : groups)
        {
            if (previous != null) number.setLeft(previous.right());
        }
        //
        alignmentGroup = new OBGroup(groups);
        alignmentGroup.setPosition(new PointF(bounds().width() / 2, (float) (0.925 * bounds().height())));
        attachControl(alignmentGroup);
        XPRZ_Generic.sendObjectToTop(alignmentGroup, this);
        alignmentGroup.show();
        alignmentGroup.setShouldTexturise(false);
        //
        correctAnswer = 1;
        hideControls("place.*");
        //
        targets = filterControls("label.*");
    }


    public void demo4g() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playSfxAudio("move_bar", false);
        PointF destination = XPRZ_Generic.copyPoint(alignmentGroup.position());
        destination.y -= bounds().height() * 0.6;
        OBAnim moveAnim = OBAnim.moveAnim(destination, alignmentGroup);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        //
        lockScreen();
        List<OBControl> groups = sortedFilteredControls("number.*");
        for (OBControl number : groups)
        {
            OBGroup group = (OBGroup) number;
            OBLabel label = (OBLabel) group.objectDict.get("label");
            //
            PointF position = label.getWorldPosition();
            group.removeMember(label);
            group.setNeedsRetexture();
            attachControl(label);
            XPRZ_Generic.sendObjectToTop(label, this);
            label.show();
            label.setPosition(position);
            label.setProperty("originalPosition", position);
        }
        unlockScreen();
        //
        action_bounceNumbers();
        action_highlightCorrectAnswer();
        //
        loadPointer(POINTER_MIDDLE);
        action_playNextDemoSentence(false); // Now look. Five goes first.
        //
        OBLabel label = (OBLabel) objectDict.get("label_1");
        OBGroup box = (OBGroup) objectDict.get("number_1");
        //
        destination = XPRZ_Generic.copyPoint(box.getWorldPosition());
        XPRZ_Generic.pointer_moveToObject(label, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_highlight(label);
        //
        playSfxAudio("correct", false);
        moveAnim = OBAnim.moveAnim(destination, label);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        action_lowlight(label);
        //
        correctAnswer++;
        action_highlightCorrectAnswer();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void finDemo4g() throws Exception
    {
        playSceneAudioIndex("DEMO2", 0, true); // Now count again with me.
        waitForSecs(0.3f);
        //
        for (int i = 1; i <= 10; i++)
        {
            OBLabel label = (OBLabel) objectDict.get(String.format("label_%d", i));
            //
            action_highlight(label);
            playSceneAudioIndex("DEMO2", i, true); // FIVE. TEN. FIFTEEN. TWENTY. TWENTY-FIVE. THIRTY. THIRTY-FIVE. FORTY. FORTY-FIVE. FIFTY.
            action_lowlight(label);
            waitForSecs(0.3f);
        }
    }


    public void action_highlightCorrectAnswer()
    {
        lockScreen();
        for (OBControl control : filterControls("number.*"))
        {
            OBGroup group = (OBGroup) control;
            OBPath frame = (OBPath) group.objectDict.get("frame");
            frame.setFillColor(Color.WHITE);
        }
        OBGroup group = (OBGroup) objectDict.get(String.format("number_%d", correctAnswer));
        if (group != null)
        {
            OBPath frame = (OBPath) group.objectDict.get("frame");
            frame.setFillColor(OBUtils.colorFromRGBString("255,234,121"));
        }
        unlockScreen();
    }


    public void action_highlight(OBLabel label) throws Exception
    {
        lockScreen();
        if (label != null)
        {
            label.setColour(Color.RED);
        }
        unlockScreen();
    }


    public void action_lowlight(OBLabel label) throws Exception
    {
        lockScreen();
        if (label != null)
        {
            label.setColour(Color.BLACK);
        }
        unlockScreen();
    }


    public void action_bounceNumbers() throws Exception
    {
        float g = 9.8f * 200;
        float floorHeight = bounds().height() * 0.95f;
        List<OBControl> labels = sortedFilteredControls("label.*");
        int totalCount = (int) labels.size();
        //
        for (int i = 1; i <= totalCount; i++)
        {
            OBControl object = labels.get(i - 1);
            OBControl placement = objectDict.get(String.format("place_%d", i));
            PointF initialPosition = new PointF(object.position().x, object.position().y);
            PointF destination = new PointF(placement.position().x, placement.position().y);
//            PointF destination = new PointF(object.position().x, placement.position().y);
            //
            float distanceX = destination.x - initialPosition.x;
            PointF midWay = new PointF(initialPosition.x + distanceX * 0.75f, floorHeight);
            //
            float height = Math.abs(floorHeight - initialPosition.y);
            float flightHeight = 1.2f * height;
            float heightDiff = flightHeight - height;
            double initialSpeedY_phase1 = Math.sqrt(2 * g * heightDiff);
            double flightTime_phase1 = initialSpeedY_phase1 / g;
            double freeFallTime = Math.sqrt((2 * flightHeight) / g);
            double totalTime_phase1 = flightTime_phase1 + freeFallTime;
            double initialSpeedX_phase1 = (midWay.x - initialPosition.x) / totalTime_phase1;
            PointF phase1 = new PointF((float) initialSpeedX_phase1, (float) initialSpeedY_phase1);
            //
            double height_phase2 = Math.abs(floorHeight - destination.y);
            double initialSpeedY_phase2 = Math.sqrt(2 * g * height_phase2);
            double flightTime_phase2 = initialSpeedY_phase2 / g;
            double initialSpeedX_phase2 = (destination.x - midWay.x) / flightTime_phase2;
            PointF phase2 = new PointF((float) initialSpeedX_phase2, (float) initialSpeedY_phase2);
            //
            object.setProperty("phase1", phase1);
            object.setProperty("phase2", phase2);
            object.setProperty("startTime", new Double(XPRZ_Generic.currentTime() + i * 0.1));
            object.setProperty("floorCollision", false);
            object.setProperty("atRest", false);
            object.setProperty("initialPosition", initialPosition);
            object.setProperty("midway", midWay);
            object.setProperty("destination", destination);
            //
//            [OBAudioManager.sharedAudioManager() prepare:audioScenes"sfx".()"bounce".()0.() onChannel:String.format("bounce_%d", i)];
        }
        //
        Boolean animationComplete = false;
        Map<String,List<String>> soundEffects = (Map<String,List<String>>)audioScenes.get("sfx");
        List<Object> list = (List<Object>)(Object)soundEffects.get("bounce");
        String sfx = (String) list.get(0);
        //
        while (!animationComplete)
        {
            lockScreen();
            //
            for (int i = 1; i <= totalCount; i++)
            {
                OBControl object = labels.get(i - 1);
                double startTime = ((Double) object.propertyValue("startTime")).doubleValue();
                Boolean floorCollision = (Boolean) object.propertyValue("floorCollision");
                Boolean atRest = (Boolean) object.propertyValue("atRest");
                PointF midWay = (PointF) object.propertyValue("midway");
                //
                double t = (XPRZ_Generic.currentTime() - startTime);
                //
                if (t < 0 || atRest)
                {
                    continue;
                }
                //
                PointF newPosition = new PointF();
                //
                if (!floorCollision)
                {
                    PointF phase1 = (PointF) object.propertyValue("phase1");
                    PointF initialPosition = (PointF) object.propertyValue("initialPosition");
                    //
                    float newX = (float) (initialPosition.x + phase1.x * t);
                    float newY = (float) (initialPosition.y + 0.5f * g * t * t - phase1.y * t);
                    newPosition.set(newX, newY);
                    if (newPosition.y > floorHeight)
                    {
                        object.setProperty("startTime", new Double(XPRZ_Generic.currentTime()));
                        object.setProperty("floorCollision", true);
                        newPosition.set(midWay);
                        //
                        OBAudioManager.audioManager.startPlaying(sfx, String.format("bounce_%d", i));
                        playSfxAudio("bounce", false);
                    }
                }
                else
                {
                    PointF phase2 = (PointF) object.propertyValue("phase2");
                    PointF destination = (PointF) object.propertyValue("destination");
                    float newX = (float) (midWay.x + phase2.x * t);
                    float newY = (float) (midWay.y + 0.5f * g * t * t - phase2.y * t);
                    newPosition.set(newX, newY);
                    //
                    float distanceToTarget = Math.abs(newPosition.y - destination.y);
                    if (distanceToTarget <= 2)
                    {
                        object.setProperty("atRest", true);
                        newPosition.set(destination);
                        //
                        animationComplete = true;
                        for (OBControl control : labels)
                        {
                            animationComplete = animationComplete && (Boolean) control.propertyValue("atRest");
                        }
                    }
                }
                object.setPosition(newPosition);
            }
            //
            unlockScreen();
            //
            waitForSecs(0.01);
        }
    }




    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            final OBLabel label = (OBLabel) targ;
            OBLabel correctLabel = (OBLabel) objectDict.get(String.format("label_%d", correctAnswer));
            //
            if (label.equals(correctLabel))
            {
                action_highlight(label);
                //
                gotItRightBigTick(false);
                //
                OBControl slot = objectDict.get(String.format("number_%d", correctAnswer));
                final OBAnim moveAnim = OBAnim.moveAnim(slot.getWorldPosition(), label);
                final OBSectionController sc = this;
                //
                if (correctAnswer >= 10)
                {
                    correctAnswer++;
                    action_highlightCorrectAnswer();
                    //
                    OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, sc);
                    action_lowlight(label);
                    //
                    waitForSecs(0.7f);
                    //
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    finDemo4g();
                    //
                    playAudioQueuedScene(currentEvent(), "FINAL", true);
                    nextScene();
                }
                else
                {
                    correctAnswer++;
                    action_highlightCorrectAnswer();
                    //
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, sc);
                            action_lowlight(label);
                        }
                    });
                    //
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
            else
            {
                gotItWrongWithSfx();
                //
                OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        action_lowlight(label);
                    }
                });
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception e)
        {
            System.out.println("X_Counting5and10_S4g.exception caught: " + e.toString());
            e.printStackTrace();
        }
    }
}
