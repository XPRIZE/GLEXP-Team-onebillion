package org.onebillion.xprz.mainui.x_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.SystemClock;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

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
        targets = sortedFilteredControls("number.*");
        //
        for (OBControl number : targets)
        {
            if (previous != null) number.setLeft(previous.right());
        }
        //
        alignmentGroup = new OBGroup(targets);
        alignmentGroup.setPosition(new PointF(bounds().width() / 2, (float) (0.925 * bounds().height())));
        attachControl(alignmentGroup);
        sendObjectToTop(alignmentGroup);
        alignmentGroup.show();
        alignmentGroup.setShouldTexturise(false);
        //
        correctAnswer = 1;
        hideControls("place.*");
    }




    public void demo4g() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playSfxAudio("move_bar",false);
        PointF destination = copyPoint(alignmentGroup.position());
        destination.y -= bounds().height() * 0.6;
        OBAnim moveAnim = OBAnim.moveAnim(destination, alignmentGroup);
        OBAnimationGroup.runAnims(Arrays.asList(moveAnim), 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        //
        lockScreen();
        for (OBControl number : targets)
        {
            OBGroup group = (OBGroup) number;
            OBLabel label = (OBLabel) group.objectDict.get("label");
            //
            PointF position = label.getWorldPosition();
            group.removeMember(label);
            group.setNeedsRetexture();
//            attachControl(label);
            sendObjectToTop(label);
            label.show();
            label.setPosition(position);
        }
        unlockScreen();
        //
        action_bounceNumbers();
        action_highlightCorrectAnswer();
        //
        loadPointer(POINTER_MIDDLE);
        action_playNextDemoSentence(false); // Now look. Five goes first.
        //
        OBLabel label =  (OBLabel) objectDict.get("label_1");
        OBGroup box = (OBGroup) objectDict.get("number_1");
        //
        destination = copyPoint(box.position());
        pointer_moveToObject(label, -15, 0.6f, EnumSet.of(Anchor.ANCHOR_MIDDLE), true);
        action_highlight(label);
        //
        playSfxAudio("correct",false);
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
        playAudioQueuedSceneIndex(currentEvent(), "DEMO2", 0, true); // Now count again with me.
        waitForSecs(0.3f);
        //
        for (int i = 1; i <= 10; i++)
        {
            OBLabel label = (OBLabel) objectDict.get(String.format("label_%d", i));
            //
            action_highlight(label);
            playAudioQueuedSceneIndex(currentEvent(), "DEMO2", i, true); // FIVE. TEN. FIFTEEN. TWENTY. TWENTY-FIVE. THIRTY. THIRTY-FIVE. FORTY. FORTY-FIVE. FIFTY.
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
        OBPath frame = (OBPath) group.objectDict.get("frame");
        frame.setFillColor(OBUtils.colorFromRGBString("255,234,121"));
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
        //
        List<OBControl> labels = sortedFilteredControls("label.*");
        int totalCount = (int) labels.size();
        //
        for (int i = 1; i <= totalCount; i++)
        {
            OBControl object = labels.get(i-1);
//            object.shouldRasterize = true;
            OBControl placement = objectDict.get(String.format("place_%d", i));
            PointF destination = copyPoint(placement.position());
            PointF initialPosition = copyPoint(object.position());
            //
            float distanceX = destination.x - initialPosition.x;
            PointF midWay = new PointF(initialPosition.x + distanceX * 3/4, floorHeight);
            //
            float height = Math.abs(floorHeight - initialPosition.y);
            float flightHeight = 1.2f * height;
            float flightTime_phase1 = (float) Math.sqrt((2 * (flightHeight - height)) / g);
            float freeFallTime = (float) Math.sqrt((2 * flightHeight) / g);
            float totalTime_phase1 = flightTime_phase1 + freeFallTime;
            //
            float initialSpeedX_phase1 = (midWay.x - initialPosition.x) / totalTime_phase1;
            float initialSpeedY_phase1 = (float) Math.sqrt((flightHeight - height) * 2 * g);
            PointF phase1 = new PointF(initialSpeedX_phase1, initialSpeedY_phase1);
            //
            float height_phase2 = Math.abs(floorHeight - destination.y);
            float initialSpeedY_phase2 = (float) Math.sqrt(2 * g * height_phase2);
            float flightTime_phase2 =  initialSpeedY_phase2 / g;
            float initialSpeedX_phase2 = (destination.x - midWay.x) / flightTime_phase2;
            PointF phase2 = new PointF(initialSpeedX_phase2, initialSpeedY_phase2);
            //
            object.setProperty("phase1", phase1);
            object.setProperty("phase2", phase2);
            object.setProperty("startTime", SystemClock.uptimeMillis() + i * 0.10);
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
        //
        while (!animationComplete)
        {
            lockScreen();
            //
            for (int i = 1; i <= totalCount; i++)
            {
                OBControl object = labels.get(i - 1);
                double startTime = (double) object.propertyValue("startTime");
                Boolean floorCollision = (Boolean) object.propertyValue("floorCollision");
                Boolean atRest = (Boolean) object.propertyValue("atRest");
                PointF midWay = (PointF) object.propertyValue("midway") ;
                //
                double t = SystemClock.uptimeMillis() - startTime;
                //
                if (t < 0) continue;
                if (atRest) continue;
                //
                PointF newPosition;
                if (!floorCollision)
                {
                    PointF phase1 = (PointF) object.propertyValue("phase1");
                    PointF initialPosition = (PointF) object.propertyValue("initialPosition");
                    //
                    float newX = (float) (initialPosition.x + phase1.x * t);
                    float newY = (float) (initialPosition.y + 0.5 * g * t * t - phase1.y * t);
                    newPosition = new PointF(newX, newY);
                    if (newPosition.y > floorHeight)
                    {
                        object.setProperty("startTime", SystemClock.uptimeMillis());
                        object.setProperty("floorCollision", true);
                        newPosition = midWay;
                        //
                        playSfxAudio("bounce", false);
                    }
                }
                else
                {
                    PointF phase2 = (PointF) object.propertyValue("phase2") ;
                    PointF destination = (PointF) object.propertyValue("destination") ;
                    float newX = (float) (midWay.x + phase2.x * t);
                    float newY = (float) (midWay.y - phase2.y * t + 0.5 * g * t * t);
                    newPosition = new PointF(newX, newY);
                    //
                    float distanceToTarget = Math.abs(newPosition.y - destination.y);
                    if (distanceToTarget <= 2)
                    {
                        object.setProperty("atRest", true);
                        newPosition = destination;
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
            waitForSecs(0.01f);
        }
    }



    /*

-(void) checkNumber:(OBLabel*)number
{
    [self statusSetChecking];
    //
    OBLabel *correctLabel = self.objectDict[[NSString stringWithFormat:@"label_%d", correctAnswer]];
    number.colour = [UIColor redColor];
    //
    if (correctLabel == number)
    {
        [self gotItRightBigTick:NO];
        //
        OBControl *slot = self.objectDict[[NSString stringWithFormat:@"number_%d", correctAnswer]];
        OBAnim *moveAnim = [OBAnim moveAnim:[self getWorldLocation:slot] obj:number];
        [OBAnimationGroup runAnims:@[moveAnim] duration:0.4 wait:NO timingFunction:ANIM_EASE_IN_EASE_OUT completionBlock:^{
            number.colour = [UIColor blackColor];
        }];
        correctAnswer++;
        [self action_highlightCorrectAnswer];
        //
        if (correctAnswer > 10)
        {
            [self waitForSecs:0.7];
            //
            [self gotItRightBigTick:YES];
            [self waitForSecs:0.3];
            //
            [self finDemo4g];
            //
            [self playSceneAudio:@"FINAL" wait:YES];
            [self nextScene];
        }
    }
    else
    {
        [self gotItWrongWithSfx];
        DoOnOtherThreadDelayed(0.3, ^{
            number.colour = [UIColor blackColor];
        });
    }
    //
    [self statusSetWaitClick];
}


     */




    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            playSfxAudio("select_number", false);
            action_highlight(targ);
            //
            if (targ.equals(action_getCorrectAnswer()))
            {
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                action_lowlight(targ);
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "FINAL", true);
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                action_lowlight(targ);
                action_answerIsWrong();
                //
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception exception)
        {
        }
    }
}
