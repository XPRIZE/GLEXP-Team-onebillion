package org.onebillion.xprz.mainui.x_countingto3;

import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Tracing;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 14/07/16.
 */
public class X_CountingTo3_S7 extends XPRZ_Generic_Tracing
{

    List numberSequence;
    int sequenceIndex;

    public X_CountingTo3_S7()
    {
        super(false);
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        numberSequence = new ArrayList();
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(0, 1, 2, 3)));
        numberSequence.addAll(OBUtils.randomlySortedArray(Arrays.asList(0, 2, 3)));
        sequenceIndex = 0;
        //
        deleteControls("trace");
        deleteControls("dash");
    }

    public void action_flashButton(Boolean turnedOn) throws Exception
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            OBGroup button = (OBGroup) objectDict.get("button");
            XPRZ_Generic.colourObject(button, OBUtils.colorFromRGBString((turnedOn) ? eventAttributes.get("button_on") : eventAttributes.get("button_off")));
            waitForSecs(0.6);
            action_flashButton(!turnedOn);
        }
    }

    public void action_spinWheel(int number)
    {
//        OBControl wheel = objectDict.get("wheel");
//        OContro
    }



}



/*


-(void)animate_spinWheel:(int) number
{
    OBGroup *wheel = self.objectDict[@"wheel"];
    OBGroup *arrow = wheel.objectDict[@"arrow"];
    int angle = 0 + 90 * number;
    OBAnim *rotateAnim = [OBAnim rotationAnim:RADIANS(1440+angle) obj:arrow];
    //
    [self playSfxAudio:@"wheel_spin" wait:NO];
    [OBAnimationGroup runAnims:@[rotateAnim] duration:1.0+(0.1*number) wait:YES timingFunction:ANIM_LINEAR completionBlock:^{
        [self playSfxAudio:@"wheel_stop" wait:NO];
    }];
}



-(void)prepareScene
{
    DoBlockWithScreenLocked(^{
        NSArray *oldObjs = [self filterControls:@".*"];

        [super setSceneXX:[self currentEvent]];

        if([self.eventAttributes[@"redraw"] isEqual:@"true"])
        {
            for(OBControl *obj in oldObjs)
            {
                [self detachControl:obj];
            }
            UIColor *numberColour = uicolorFromRGBString(self.eventAttributes[@"font_colour"]);
            for (OBControl *label in [self filterControls:@"label.*"])
            {
                OBLabel *number = [self createLabelForNumberBox:label finalResizeFactor:1.2];
                number.colour = numberColour;
                [self attachControl:number];
            }
        }
    });
    [self statusSetLocked];
}


-(void)demo7a
{
    [self statusSetLocked];
    [self loadPointer:POINTER_MIDDLE];
    //
    [self playSceneAudio:@"DEMO" atIndex:0 wait:NO]; //Look
    OBGroup *wheel = self.objectDict[@"wheel"];
    OBGroup *button = wheel.objectDict[@"button"];
    [self movePointerToObject:button anchor:ANCHOR_BOTTOM rotation:-15 time:0.6 wait:YES];
    //
    [self playSceneAudio:@"PROMPT" atIndex:sequenceIndex wait:NO];
    [self setReplayAudio:InsertAudioInterval(audioScenes[[self currentEvent]][@"REPEAT"][sequenceIndex], 300)];
    //
    [self waitAudio];
    [thePointer hide];
    //
    [self statusSetWaitClick];
    [self animate_flashButton];
}



-(void) showSequenceProgress
{
    DoBlockWithScreenLocked(^{
        NSArray *controls = [self filterControls:@"progress.*"];
        for(OBGroup *control in controls)
        {
            [self colourObjectWith:control colourRGB:self.eventAttributes[@"progress_off"]];
        }
        //
        for(int i = 0; i <= sequenceIndex; i++)
        {
            OBGroup *progress = self.objectDict[[NSString stringWithFormat:@"progress_%d", i]];
            [self colourObjectWith:progress colourRGB:self.eventAttributes[@"progress_on"]];
        }
    });
}



-(void)touchDownAtPoint:(CGPoint)pt view:(UIView*)v
{
    DoSafeBlockOnThread(^{
        if([self statusWaitTrace])
        {
            [self checkTraceStart:pt];
        } else if ([self statusWaitClick])
        {
            [self checkButtonClick:pt];
        }
    });
}


-(void)checkButtonClick:(CGPoint)pt
{
    OBGroup *wheel = self.objectDict[@"wheel"];
    OBGroup *button = wheel.objectDict[@"button"];
    OBControl *c =[self finger:0 to:1 objects:@[button] point:pt filterDisabled:NO];
    if (c)
    {
        [self statusSetWaitTrace];
        int number = [numberSequence[sequenceIndex] intValue];
        [self animate_spinWheel:number];
        [self showSequenceProgress];
        [self setUpTracing:number];
        if (sequenceIndex == 0)
        {
            [self playSceneAudio:@"PROMPT2" atIndex:0 wait:NO];
            [self setReplayAudio:InsertAudioInterval(audioScenes[[self currentEvent]][@"REPEAT2"][0], 300)];
        } else {
            [self playSceneAudio:@"PROMPT2" atIndex:1 wait:NO];
            [self setReplayAudio:InsertAudioInterval(audioScenes[[self currentEvent]][@"REPEAT2"][1], 300)];
        }
    }
}




-(void)nextSubpath
{
    @try
    {
        if (++subPathIndex >= [subPaths count])
        {
            [self gotItRightBigTick:YES];
            [self.target hide];
            self.targets = [self.targets arrayByRemovingObject:self.target];
            int number = [numberSequence[sequenceIndex] intValue];
            [self playSceneAudio:@"CORRECT" atIndex:number wait:YES];
            [self waitForSecs:0.3];
            //
            if (sequenceIndex == [numberSequence count] - 1)
            {
                [self playSceneAudio:@"FINAL" wait:YES];
                [self waitForSecs:0.3];
                [self nextScene];
            } else {
                DoBlockWithScreenLocked(^{
                    [self resetTracing];
                    [dash hide];
                });
                sequenceIndex++;
                [self playSceneAudio:@"PROMPT" atIndex:sequenceIndex wait:NO];
                [self setReplayAudio:InsertAudioInterval(audioScenes[[self currentEvent]][@"REPEAT"][sequenceIndex], 300)];
                [self statusSetWaitClick];
                [self animate_flashButton];
            }
        }
        else
        {
            if (currentTrace)
            {
                [doneTraces addObject:currentTrace];
                currentTrace = nil;
                currDrawingPath = nil;
                finished = NO;
                segmentIndex = 0;
                [self positionArrow];
            }
        }
    }
    @catch (NSException *exception) {
    }
}

 */
