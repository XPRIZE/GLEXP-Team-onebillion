package org.onebillion.onecourse.mainui.generic;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 14/03/2017.
 */

public class OC_Generic_AddRemoveObjects_SelectCorrectNumber extends OC_Generic_AddRemoveObjectsToScene
{
    public List<OBLabel> numbers;
    public List<OBLabel> equation;
    public int correctAnswer;
    public int initialShownObjects;
    public int phase;


    public OC_Generic_AddRemoveObjects_SelectCorrectNumber (boolean isAdd)
    {
        super(isAdd, false);
    }


    @Override
    public String getObjectPrefix ()
    {
        return "obj";
    }


    public String getNumberPrefix ()
    {
        return "number";
    }


    public String getEquationPrefix ()
    {
        return "label";
    }


    public String getSFX_hideObject ()
    {
        return "remove_object";
    }

    public String getSFX_placeObject ()
    {
        return "add_object";
    }

    public int getEquationColor_normal ()
    {
        return OBUtils.colorFromRGBString("119,201,33");
    }

    public int getEquationColor_highlight ()
    {
        return Color.RED;
    }

    public int getNumberColor_normal ()
    {
        return Color.BLACK;
    }

    public int getNumberColor_highlight ()
    {
        return Color.RED;
    }


    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        String correctAnswer_string = eventAttributes.get("correctAnswer");
        if (correctAnswer_string == null)
            correctAnswer_string = eventAttributes.get("correct_answer");
        //
        correctAnswer = Integer.parseInt(correctAnswer_string);
        //
        phase = 0;
        //
        if (numbers != null && redraw)
        {
            for (OBLabel number : numbers)
            {
                detachControl(number);
            }
        }
        numbers = new ArrayList<>();
        for (OBControl number_frame : sortedFilteredControls(getNumberPrefix() + ".*"))
        {
            OBLabel number = action_createLabelForControl(number_frame);
            number.setProperty("number", number_frame.attributes().get("number"));
            number.setProperty("originalScale", number.scale());
            number_frame.hide();
            number.setColour(getNumberColor_normal());
            numbers.add(number);
        }
        //
        if (equation != null && redraw)
        {
            for (OBLabel label : equation)
            {
                detachControl(label);
            }
        }
        equation = new ArrayList<>();
        for (OBControl label : sortedFilteredControls(getEquationPrefix() + ".*"))
        {
            OBLabel equationElement = action_createLabelForControl(label);
            label.hide();
            equationElement.hide();
            equationElement.setColour(getEquationColor_normal());
            equation.add(equationElement);
        }
        //
        action_showEquationForPhase();
        initialShownObjects = getTotalShownObjects();
        //
        // disabling already visible objects in the scenes where you need to add more objects to the scene
        for (OBControl object : filterControls(getObjectPrefix() + ".*"))
        {
            if (!object.hidden() && !canUndo && isAdd)
            {
                object.disable();
            }
        }
    }


    @Override
    public void check_correctNumberObjectsShown_viaHide () throws Exception
    {
        if (getTotalShownObjects() == correctAnswer)
        {
            phase = 1;
            action_showEquationForPhase();
            //
            waitAudio();
            waitForSecs(0.3);
            //
            setReplayAudioScene(currentEvent(), "REPEAT2");
            playAudioQueuedScene(currentEvent(), "PROMPT2", false);
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            revertStatusAndReplayAudio();
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    @Override
    public void check_correctNumberObjectsShown_viaShow () throws Exception
    {
        if (getTotalHiddenObjects() == 0)
        {
            phase = 1;
            action_showEquationForPhase();
            //
            waitAudio();
            waitForSecs(0.3);
            //
            setReplayAudioScene(currentEvent(), "REPEAT2");
            playAudioQueuedScene(currentEvent(), "PROMPT2", false);
            //
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            revertStatusAndReplayAudio();
            setStatus(STATUS_AWAITING_CLICK);
        }
    }


    public void action_showEquationForPhase ()
    {
        if (equation.size() == 0) return;
        //
        lockScreen();
        equation.get(0).show();
        if (phase > 0)
        {
            equation.get(1).show();
            equation.get(2).show();
        }
        //
        if (phase > 1)
        {
            equation.get(3).show();
            equation.get(4).show();
        }
        unlockScreen();
    }


    @Override
    public void action_playCorrectAudioAfterHidingObject () throws Exception
    {

    }


    public void action_resizeNumber (OBLabel number, boolean increase, boolean hilited)
    {
        lockScreen();
        float scale = (float) number.propertyValue("originalScale");
        float resizeFactor = (increase) ? 1.5f : 1.0f;
        PointF position = OC_Generic.copyPoint(number.position());
        number.setScale(scale * resizeFactor);
        number.setPosition(position);
        number.setColour((hilited) ? Color.RED : Color.BLACK);
        unlockScreen();
    }


    public void action_highlightObject (OBControl selectedObject)
    {
        OBLabel label = (OBLabel) selectedObject;
        label.setColour(OBUtils.colorFromRGBString("225,0,0"));

    }

    public void action_lowlightObject (OBControl selectedObject)
    {
        OBLabel label = (OBLabel) selectedObject;
        label.setColour(OBUtils.colorFromRGBString("0,0,0"));

    }


    public void action_selectNumber (OBLabel number)
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            action_highlightNumber(number);
            //
            if (check_isAnswerCorrect(number))
            {
                action_answerIsCorrect(number);
            }
            else
            {
                action_answerIsWrong(number);
                //
                revertStatusAndReplayAudio();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }


    public void action_answerIsCorrect (OBLabel target) throws Exception
    {
        action_resizeNumber(target, true, true);
        //
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        phase = 2;
        action_showEquationForPhase();
        //
        if (equation.size() > 0)
        {
            List<Object> audioList = audioForScene("CORRECT");
            for (int i = 0; i < audioList.size(); i++)
            {
                OBLabel label = equation.get(i);
                //
                playSceneAudioIndex("CORRECT", i, false);
                //
                lockScreen();
                label.setColour(getEquationColor_highlight());
                unlockScreen();
                //
                waitAudio();
                waitForSecs(0.3);
                //
                lockScreen();
                label.setColour(getEquationColor_normal());
                unlockScreen();
            }
            //
            action_lowlightNumber(target);
        }
        else
        {
            playSceneAudio("CORRECT", true);
            waitForSecs(0.3);
        }
        //
        playAudioQueuedScene(currentEvent(), "FINAL", true);
        //
        for (OBLabel label : numbers)
        {
            action_resizeNumber(label, false, false);
        }
        //
        nextScene();
    }


    public void action_answerIsWrong (OBLabel target) throws Exception
    {
        gotItWrongWithSfx();
        waitForSecs(0.3);
        //
        action_lowlightNumber(target);
        playAudioQueuedScene(currentEvent(), "INCORRECT", false);
    }


    public void action_highlightNumber (OBLabel label) throws Exception
    {
        lockScreen();
        label.setColour(getNumberColor_highlight());
        unlockScreen();
    }


    public void action_lowlightNumber (OBLabel label) throws Exception
    {
        lockScreen();
        label.setColour(getNumberColor_normal());
        unlockScreen();
    }


    public void action_revealObject (OBControl control, PointF pt) throws Exception
    {
        // no movement into place for events where you need to select the number afterwards
        playSfxAudio(getSFX_placeObject(), false);
        //
        lockScreen();
        control.show();
        //
        // find the closest visible dash to the object that is about to be revealed
        OBControl bestMatch = null;
        Float bestDistance = null;
        //
        for (OBControl dash : filterControls("dash_.*"))
        {
            if (!dash.hidden())
            {
                float distance = OB_Maths.PointDistance(dash.getWorldPosition(), control.position());
                if (bestDistance == null || bestDistance > distance)
                {
                    bestMatch = dash;
                    bestDistance = distance;
                }
            }
        }
        if (bestMatch != null)
        {
            bestMatch.hide();
        }
        unlockScreen();
        //
        List audio = getAudioForScene(currentEvent(), "CORRECT");
        if (audio.size() > objectDeltaCount)
        {
            playSceneAudioIndex("CORRECT", objectDeltaCount, false);
        }
        objectDeltaCount++;
    }


    public Boolean check_isAnswerCorrect (OBLabel label)
    {
        String number_string = (String) label.propertyValue("number");
        int number = Integer.parseInt(number_string);
        //
        return correctAnswer == number;
    }


    public OBLabel findNumber (PointF pt)
    {
        return (OBLabel) finger(-1, 2, (List<OBControl>) (Object) numbers, pt, true);
    }


    @Override
    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (phase == 0)
            {
                super.touchDownAtPoint(pt, v);
            }
            else if (phase == 1)
            {
                final OBLabel number = findNumber(pt);
                if (number != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            action_selectNumber(number);
                        }
                    });
                    return;
                }
            }
        }
    }
}
