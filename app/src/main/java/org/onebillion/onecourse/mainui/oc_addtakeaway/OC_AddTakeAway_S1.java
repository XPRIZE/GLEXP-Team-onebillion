package org.onebillion.onecourse.mainui.oc_addtakeaway;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_SelectCorrectObject;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 11/07/16.
 */
public class OC_AddTakeAway_S1 extends OC_Generic_SelectCorrectObject
{

    OBLabel bigNumber;
    Map<String, OBLabel> numbers;


    @Override
    public OBControl action_getCorrectAnswer ()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return numbers.get(correctString);
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        if (redraw)
        {
            List<OBControl> controls = filterControls("number.*");
            float smallestFontSize = 1000000000;
            numbers = new HashMap<String, OBLabel>();
            for (OBControl control : controls)
            {
                OBLabel label = action_createLabelForControl(control, 1.2f, false);
                control.hide();
                numbers.put((String) control.attributes().get("id"), label);
                if (label.fontSize() < smallestFontSize) smallestFontSize = label.fontSize();
            }
            for (OBLabel label : numbers.values())
            {
                label.setFontSize(smallestFontSize);
                label.sizeToBoundingBox();
            }
            //
            OBPath bigNumberBox = (OBPath) objectDict.get("big_number");
            bigNumber = action_createLabelForControl(bigNumberBox, 1.2f);
            bigNumberBox.hide();
            bigNumber.setColour(OBUtils.colorFromRGBString("225,0,0"));
            bigNumber.hide();
            objectDict.put("bigNumber_label", bigNumber);
            //
            List<OBGroup> dominos = (List<OBGroup>) (Object) filterControls("domino.*");
            for (OBGroup domino : dominos)
            {
                String values[] = ((String) domino.attributes().get("value")).split(",");
                action_showDotsOnDomino((OBGroup)domino.objectDict.get("left"), Integer.parseInt(values[0]));
                action_showDotsOnDomino((OBGroup)domino.objectDict.get("right"), Integer.parseInt(values[1]));
            }
        }
    }


    public void action_showDotsOnDomino(OBGroup side, int number)
    {
        side.objectDict.get("dot_4").setHidden(number % 2 == 0);
        side.objectDict.get("dot_2").setHidden(number <= 1);
        side.objectDict.get("dot_6").setHidden(number <= 1);
        side.objectDict.get("dot_1").setHidden(number <= 3);
        side.objectDict.get("dot_7").setHidden(number <= 3);
        side.objectDict.get("dot_3").setHidden(number != 6);
        side.objectDict.get("dot_5").setHidden(number != 6);
    }


    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        OBLabel label = (OBLabel) control;
        label.setColour(OBUtils.colorFromRGBString("225,0,0"));
    }

    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        OBLabel label = (OBLabel) control;
        label.setColour(Color.BLACK);
    }



    public void action_answerIsCorrect(OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        bigNumber.show();
        waitForSecs(0.3);
        //
        playAudioQueuedScene(currentEvent(), "CORRECT", true);
        //
        if (audioSceneExists("FINAL"))
        {
            waitForSecs(0.3);
            //
            playSceneAudio("FINAL", true);
        }
        else
        {
            waitForSecs(0.7);
        }
        //
        nextScene();
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "number";
    }


    public void demo1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        demoButtons();
        //
        action_playNextDemoSentence(false); // Now let’s count the spots on the domino.
        OC_Generic.pointer_moveToObjectByName("domino", -15, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        List<OBControl> dots = new ArrayList<>();
        OBGroup domino = (OBGroup) objectDict.get("domino");
        OBGroup left_side = (OBGroup) domino.objectDict.get("left");
        OBGroup right_side = (OBGroup) domino.objectDict.get("right");
        dots.add((left_side.objectDict.get("dot_1")));
        dots.add((left_side.objectDict.get("dot_2")));
        dots.add((left_side.objectDict.get("dot_6")));
        dots.add((left_side.objectDict.get("dot_7")));
        dots.add((right_side.objectDict.get("dot_6")));
        dots.add((right_side.objectDict.get("dot_2")));
        //
        for (OBControl dot : dots)
        {
            PointF absolutePosition = dot.getWorldPosition();
            movePointerToPoint(absolutePosition, -15-OC_Generic.randomInt(0,5), 0.3f + (dot.equals(dots.get(0)) ? 0.3f : 0.0f), true);
            action_playNextDemoSentence(true); // One. Two. Three. Four. Five. Six.
            waitForSecs(0.3);
        }
        //
        OBLabel number = numbers.get("number_6");
        action_playNextDemoSentence(false); // Touch six.
        OC_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToObject(number, -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_highlight(number);
        playSfxAudio("correct", false);
        bigNumber.show();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // There are six spots altogether.
        OC_Generic.pointer_moveToObject(bigNumber, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }



    public void demo1i() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now there are TWO dominoes;
        OC_Generic.pointer_moveToObjectByName("domino2", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Let's count the spots.
        OC_Generic.pointer_moveToObjectByName("domino2", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        List<OBControl> dots = new ArrayList<>();
        OBGroup domino = (OBGroup) objectDict.get("domino1");
        OBGroup left_side = (OBGroup) domino.objectDict.get("left");
        OBGroup right_side = (OBGroup) domino.objectDict.get("right");
        dots.add((left_side.objectDict.get("dot_6")));
        dots.add((left_side.objectDict.get("dot_2")));
        dots.add((right_side.objectDict.get("dot_4")));
        domino = (OBGroup) objectDict.get("domino2");
        left_side = (OBGroup) domino.objectDict.get("left");
        right_side = (OBGroup) domino.objectDict.get("right");
        dots.add((left_side.objectDict.get("dot_6")));
        dots.add((left_side.objectDict.get("dot_4")));
        dots.add((left_side.objectDict.get("dot_2")));
        dots.add((right_side.objectDict.get("dot_4")));
        //
        for (OBControl dot : dots)
        {
            PointF absolutePosition = dot.getWorldPosition();
            movePointerToPoint(absolutePosition, -15-OC_Generic.randomInt(0,5), 0.3f + (dot.equals(dots.get(0)) ? 0.3f : 0.0f), true);
            action_playNextDemoSentence(true); // One. Two. Three. Four. Five. Six. Seven.
            waitForSecs(0.3);
        }
        //
        OBControl number = numbers.get("number_7");
        action_playNextDemoSentence(false); // Touch seven.
        OC_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToObject(number, -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_highlight(number);
        playSfxAudio("correct", false);
        bigNumber.show();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // There are seven spots altogether.
        OC_Generic.pointer_moveToObject(bigNumber, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }

    @Override
    public OBControl findTarget (PointF pt)
    {
        List<OBPath> values = new ArrayList(numbers.values());
        return finger(0, 2, (List<OBControl>) (Object) values, pt);
    }
}
