package org.onebillion.onecourse.mainui.oc_patterns;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.UPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.onebillion.onecourse.mainui.generic.OC_Generic.randomInt;
import static org.onebillion.onecourse.utils.OB_Maths.bezef;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Patterns_S4m extends OC_Patterns_S4f
{
    OBGroup dinosaur;
    int currentLine, totalLines;
    List<String> strokeColours;


    @Override
    public void fin ()
    {
        try
        {
            waitForSecs(0.3);
            playAudioQueuedScene("finale", "DEMO", true);
            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String action_getScenesProperty ()
    {
        return "scenes3";
    }



    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        currentLine = 1;
        dinosaur = (OBGroup) objectDict.get("dinosaur");
        dinosaur.hide();
        //
        List<OBPath> paths = (List<OBPath>) (Object) filterControls("path.*");
        totalLines = paths.size();
        //
        for (OBPath path : paths)
        {
            path.sizeToBox(boundsf());
            path.hide();
        }
        //
        strokeColours = new ArrayList();
        strokeColours.add("255,0,0");       // red
        strokeColours.add("0,255,0");       // green
        strokeColours.add("255,161,12");    // purple
        strokeColours.add("166,12,255");    // orange
        strokeColours.add("0,0,255");       // blue
        //
        action_setupCurrentLine();
        //
        hideControls("background.*");
        OC_Generic.sendObjectToTop(stop, this);
        OC_Generic.sendObjectToTop(start, this);
    }


    public void action_setupCurrentLine ()
    {
        String strokeColour = strokeColours.get(randomInt(0, strokeColours.size() - 1));
        strokeColours.remove(strokeColour);
        //
        line = (OBPath) objectDict.get(String.format("path_%d", currentLine));
        line.sizeToBox(boundsf());
        start.setPosition(line.firstPoint());
        //
        start.setFillColor(OBUtils.colorFromRGBString(strokeColour));
        stop.setPosition(line.lastPoint());
        //
        line.setStrokeEnd(0.0f);
        line.setStrokeColor(OBUtils.colorFromRGBString(strokeColour));
        frac = 0.0f;
        //
        start.show();
        stop.show();
        line.show();

    }




    public void action_endTracing () throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        gotItRightBigTick(false);
        //
        lockScreen();
        start.hide();
        stop.hide();
        unlockScreen();
        //
        float hsv[] = new float[3];
        Color.colorToHSV(line.strokeColor(), hsv);
        final float originalBrightness = hsv[2];
        //
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock (float frac)
            {
                float hsv[] = new float[3];
                Color.colorToHSV(line.strokeColor(), hsv);
                hsv[2] = (float) Math.max(0.0f, originalBrightness * (1.0 - frac));
                line.setStrokeColor(Color.HSVToColor(hsv));
            }
        };
        OBAnimationGroup.runAnims(Collections.singletonList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitSFX();
        //
        currentLine++;
        //
        if (currentLine > totalLines)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            //
            List animations = new ArrayList();
            //
            lockScreen();
            for (OBControl control : filterControls("background.*"))
            {
                anim = OBAnim.opacityAnim(1.0f, control);
                control.show();
                control.setOpacity(0.0f);
                animations.add(anim);
            }
            //
            for (OBControl control : filterControls("path.*"))
            {
                anim = OBAnim.opacityAnim(0.0f, control);
                animations.add(anim);
            }
            //
            dinosaur.show();
            dinosaur.setOpacity(0.0f);
            anim = OBAnim.opacityAnim(1.0f, dinosaur);
            animations.add(anim);
            //
            unlockScreen();
            //
            playSfxAudio("water", false);
            //
            OBAnimationGroup.runAnims(animations, 0.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitSFX();
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("CORRECT", true);
            waitForSecs(0.3f);
            //
            OBControl frame1 = ((OBGroup) dinosaur.objectDict.get("complete")).objectDict.get("frame1");
            OBControl frame2 = ((OBGroup) dinosaur.objectDict.get("complete")).objectDict.get("frame2");
            playSfxAudio("dinosaur", false);
            //
            lockScreen();
            frame1.hide();
            frame2.show();
            unlockScreen();
            //
            waitSFX();
            //
            lockScreen();
            frame1.show();
            frame2.hide();
            unlockScreen();
            //
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("FINAL", true);
            //
            nextScene();
            //
            return;
        }
        else
        {
            action_setupCurrentLine();
            //
            List audio = audioForScene(String.format("PROMPT%d", currentLine));
            setReplayAudio(audioForScene(String.format("REPEAT%d", currentLine)));
            //
            playAudioQueued(audio, false);
            //
            revertStatusAndReplayAudio();
        }
    }


}

