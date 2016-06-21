package org.onebillion.xprz.mainui.x_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
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
            group.removeMember(label);
            attachControl(label);
            sendObjectToTop(label);
            label.show();
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




    public void action_bounceNumbers()
    {

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



    /*


-(void) action_bounceNumbers
{
    float g = 9.8 * 200;
    float floorHeight = self.bounds.size.height * 0.95;
    NSArray *labels = [self sortedFilteredControls:@"label.*"];
    int totalCount = (int) [labels count];
    //
    for (int i = 1; i <= totalCount; i++)
    {
        OBControl *object = labels[i-1];
        object.shouldRasterize = YES;
        OBControl *placement = self.objectDict[[NSString stringWithFormat:@"place_%d", i]];
        CGPoint destination = placement.position;
        CGPoint initialPosition = object.position;
        //
        float distanceX = destination.x - initialPosition.x;
        CGPoint midWay = CGPointMake(initialPosition.x + distanceX * 3 / 4, floorHeight);
        //
        float height = fabs(floorHeight - initialPosition.y);
        float flightHeight = 1.2 * height;
        float flightTime_phase1 = sqrtf((2 * (flightHeight - height)) / g);
        float freeFallTime = sqrtf((2 * flightHeight) / g);
        float totalTime_phase1 = flightTime_phase1 + freeFallTime;
        //
        float initialSpeedX_phase1 = (midWay.x - initialPosition.x) / totalTime_phase1;
        float initialSpeedY_phase1 = sqrtf((flightHeight - height) * 2 * g);
        CGPoint phase1 = CGPointMake(initialSpeedX_phase1, initialSpeedY_phase1);
        //
        float height_phase2 = fabs(floorHeight - destination.y);
        float initialSpeedY_phase2 = sqrtf(2 * g * height_phase2);
        float flightTime_phase2 =  initialSpeedY_phase2 / g;
        float initialSpeedX_phase2 = (destination.x - midWay.x) / flightTime_phase2;
        CGPoint phase2 = CGPointMake(initialSpeedX_phase2, initialSpeedY_phase2);
        //
        [object setProperty:@"phase1" value:[NSValue valueWithCGPoint:phase1]];
        [object setProperty:@"phase2" value:[NSValue valueWithCGPoint:phase2]];
        [object setProperty:@"startTime" value:[NSNumber numberWithDouble:CACurrentMediaTime()+ i * 0.10]];
        [object setProperty:@"floorCollision" value:@NO];
        [object setProperty:@"atRest" value:@NO];
        [object setProperty:@"initialPosition" value:[NSValue valueWithCGPoint:initialPosition]];
        [object setProperty:@"midway" value:[NSValue valueWithCGPoint:midWay]];
        [object setProperty:@"destination" value:[NSValue valueWithCGPoint:destination]];
        //
        [[OBAudioManager sharedAudioManager] prepare:audioScenes[@"sfx"][@"bounce"][0] onChannel:[NSString stringWithFormat:@"bounce_%d", i]];
    }
    //
    __block bool animationComplete = NO;
    //
    while (!animationComplete)
    {
        DoBlockWithScreenLocked(^{
            for (int i = 1; i <= totalCount; i++)
            {
                OBControl *object = labels[i-1];
                double startTime = [[object propertyValue:@"startTime"] doubleValue];
                bool floorCollision = [[object propertyValue:@"floorCollision"] boolValue];
                bool atRest = [[object propertyValue:@"atRest"] boolValue];
                CGPoint midWay = [[object propertyValue:@"midway"] CGPointValue];
                //
                double t = CACurrentMediaTime() - startTime;
                //
                if (t < 0) continue;
                if (atRest) continue;
                //
                CGPoint newPosition;
                if (!floorCollision)
                {
                    CGPoint phase1 = [[object propertyValue:@"phase1"] CGPointValue];
                    CGPoint initialPosition = [[object propertyValue:@"initialPosition"] CGPointValue];
                    //
                    float newX = initialPosition.x + phase1.x * t;
                    float newY = initialPosition.y + 0.5 * g * t * t - phase1.y * t;
                    newPosition = CGPointMake(newX, newY);
                    // NSLog(@"1. %d: %f %f - %f %f", i, newX, newY, initialPosition.x, initialPosition.y);
                    if (newPosition.y > floorHeight)
                    {
                        [object setProperty:@"startTime" value:[NSNumber numberWithDouble:CACurrentMediaTime()]];
                        [object setProperty:@"floorCollision" value:@YES];
                        newPosition = midWay;
                        //
                        [[OBAudioManager sharedAudioManager] playOnChannel:[NSString stringWithFormat:@"bounce_%d", i]];
                    }
                }
                else
                {
                    CGPoint phase2 = [[object propertyValue:@"phase2"] CGPointValue];
                    CGPoint destination = [[object propertyValue:@"destination"] CGPointValue];
                    float newX = midWay.x + phase2.x * t;
                    float newY = midWay.y - phase2.y * t + 0.5 * g * t * t;
                    newPosition = CGPointMake(newX, newY);
                    //
                    float distanceToTarget = fabs(newPosition.y - destination.y);
                    // NSLog(@"2. %d: %f %f - %f %f - [%f]", i, newX, newY, midWay.x, midWay.y, distanceToTarget);
                    if (distanceToTarget <= 2)
                    {
                        [object setProperty:@"atRest" value:@YES];
                        newPosition = destination;
                        //
                        animationComplete = YES;
                        for (OBControl *control in labels)
                        {
                            animationComplete = animationComplete && [[control propertyValue:@"atRest"] boolValue];
                        }
                    }
                }
                object.position = newPosition;
            }
        });
        [self waitForSecs:0.01];
    }
}



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
        OBGroup group = (OBGroup) targ;
        try
        {
            playSfxAudio("select_number", false);
            action_highlight(group);
            //
            if (group.equals(action_getCorrectAnswer()))
            {
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                action_lowlight(group);
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
                action_lowlight(group);
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
