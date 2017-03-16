package org.onebillion.onecourse.mainui.oc_patterns;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.text.style.UpdateAppearance;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.UPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Patterns_S4 extends OC_Generic_Event
{
    List correctAnswers;
    int correctCount;


    public void fin ()
    {
        goToCard(OC_Patterns_S4f.class, "event4");
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        String correctAnswer_string = eventAttributes.get("correctAnswer");
        if (correctAnswer_string != null)
        {
            correctAnswers = new ArrayList(Arrays.asList(correctAnswer_string.split(",")));
            correctCount = 0;
        }
    }

    public void action_highlightPath (OBPath target)
    {
        action_showShadow(target);
    }


    public void action_lowlightPath (OBPath target)
    {
        action_removeShadow(target);
    }


    public void setScene4a ()
    {
        setSceneXX(currentEvent());
        List<OBPath> lines = (List<OBPath>) (Object) filterControls("line.*");
        for (OBPath line : lines)
        {
            line.setStrokeEnd(0);
        }
    }

    public void demo4a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        //
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.2f, 0.6f), bounds()), -15, 0.6f, true);
        List<OBPath> lines = (List<OBPath>) (Object) sortedFilteredControls("line_straight.*");
        action_playNextDemoSentence(false); // Look at these lines.
        for (final OBPath line : lines)
        {
            PointF firstPoint = convertPointFromControl(line.firstPoint(), line);
            movePointerToPoint(firstPoint, -15, 0.3f, true);
            Path pointerPath = convertPathFromControl(line.path(), line);
            OBAnim anim1 = OBAnim.pathMoveAnim(thePointer, pointerPath, false, 0);
            OBAnim anim2 = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock (float frac)
                {
                    line.setStrokeEnd(frac);
                }
            };
            UPath path = deconstructedPath(currentEvent(), (String) line.attributes().get("id"));
            float duration = path.length() * 2 / theMoveSpeed;
            OBAnimationGroup.runAnims(Arrays.asList(anim1, anim2), duration, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // They are all STRAIGHT lines.
        //
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.6f, 0.6f), bounds()), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        lines = (List<OBPath>) (Object) sortedFilteredControls("line_curved.*");
        action_playNextDemoSentence(false); // Now look at these lines …
        for (final OBPath line : lines)
        {
            PointF firstPoint = convertPointFromControl(line.firstPoint(), line);
            movePointerToPoint(firstPoint, -15, 0.3f, true);
            Path pointerPath = convertPathFromControl(line.path(), line);
            OBAnim anim1 = OBAnim.pathMoveAnim(thePointer, pointerPath, false, 0);
            OBAnim anim2 = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock (float frac)
                {
                    line.setStrokeEnd(frac);
                }
            };
            UPath path = deconstructedPath(currentEvent(), (String) line.attributes().get("id"));
            float duration = path.length() * 2 / theMoveSpeed;
            OBAnimationGroup.runAnims(Arrays.asList(anim1, anim2), duration, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        waitAudio();
        waitForSecs(0.3f);
        //
        action_playNextDemoSentence(false); // They are CURVED.
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.6f, 0.8f), bounds()), -15, 0.6f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        nextScene();
    }

    public void checkLine (OBPath line) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        action_highlightPath(line);
        //
        String answer = ((String) line.attributes().get("id")).replaceAll("line_", "");
        if (correctAnswers.contains(answer))
        {
            gotItRightBigTick(false);
            line.disable();
            correctCount++;
            //
            if (correctCount == correctAnswers.size())
            {
                waitSFX();
                waitForSecs(0.3f);
                //
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("CORRECT", 300, true);
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("FINAL", 300, true);
                //
                lockScreen();
                for (OBPath path : (List<OBPath>) (Object) filterControls("line.*"))
                {
                    action_lowlightPath(path);
                }
                unlockScreen();
                nextScene();
                //
                return;
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("INCORRECT", 300, false);
            action_lowlightPath(line);
        }
        //
        revertStatusAndReplayAudio();
    }


    public Object findLine (PointF pt)
    {
        return finger(0, 2, filterControls("line.*"), pt, true);

    }

    public void touchDownAtPoint (final PointF pt, View v)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                if (status() == STATUS_AWAITING_CLICK)
                {
                    final OBPath obj = (OBPath) findLine(pt);
                    if (obj != null)
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                checkLine(obj);
                            }
                        });

                    }
                }
            }
        });
    }




}
