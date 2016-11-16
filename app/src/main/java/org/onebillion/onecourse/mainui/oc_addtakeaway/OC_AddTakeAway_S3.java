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
public class OC_AddTakeAway_S3 extends OC_Generic_SelectCorrectObject
{
    Map<String, OBLabel> numbers;
    List<OBLabel> equation;

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
            numbers = new HashMap<String, OBLabel>();
            for (OBControl control : controls)
            {
                OBLabel label = action_createLabelForControl(control, 1.2f, false);
                control.hide();
                numbers.put((String) control.attributes().get("id"), label);
            }
            //
            controls = sortedFilteredControls("label.*");
            equation = new ArrayList<>();
            for (OBControl control : controls)
            {
                Map attributes = control.attributes();
                if (attributes != null)
                {
                    String text = (String) attributes.get("text");
                    if (text != null)
                    {
                        OBLabel label = action_createLabelForControl(control, 1.0f, false);
                        String colour = (String) control.attributes().get("colour");
                        label.setColour(OBUtils.colorFromRGBString(colour));
                        label.setProperty("colour", colour);
                        equation.add(label);
                        //
                        objectDict.put(String.format("%s_label", control.attributes().get("id")), label);
                        label.setProperty("originalScale", label.scale());
                        label.hide();
                    }
                }
                control.hide();
            }
            //
            List<OBPath> loops = (List<OBPath>) (Object) filterControls("loop.*");
            for (OBPath loop : loops)
            {
                loop.sizeToBoundingBoxIncludingStroke();
            }
        }
        hideControls("group.*");
        hideControls("loop.*");
    }


    public void action_resizeLabel(OBLabel label, Boolean increase)
    {
        lockScreen();
        float resizeFactor = (increase) ? 1.5f : 1.0f;
        PointF position = OC_Generic.copyPoint(label.position());
        float scale = (float) label.propertyValue("originalScale");
        label.setScale(scale * resizeFactor);
        label.setPosition(position);
        label.setColour((increase) ? OBUtils.colorFromRGBString("225,0,0") : OBUtils.colorFromRGBString((String) label.propertyValue("colour")));
        unlockScreen();
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


    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        playSfxAudio("add_object", false);
        lockScreen();
        for (OBControl item : equation)
        {
            item.show();
        }
        for (OBControl loop : filterControls("loop.*"))
        {
            loop.hide();
        }
        objectDict.get("loop_big").show();
        unlockScreen();
        waitForSecs(0.3);
        //
        for (int i = 0; i < getAudioForScene(currentEvent(), "CORRECT").size(); i++)
        {
            playSceneAudioIndex("CORRECT", i, false);
            action_resizeLabel(equation.get(i), true);
            waitAudio();
            waitForSecs(0.3);
            //
            action_resizeLabel(equation.get(i), false);
        }
        //
        if (audioSceneExists("FINAL"))
        {
            waitForSecs(0.3);
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


    @Override
    public void doMainXX () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(false);
        playSfxAudio("add_object", false);
        lockScreen();
        objectDict.get("group_1").show();
        objectDict.get("loop_1").show();
        equation.get(0).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false);
        playSfxAudio("add_object", false);
        lockScreen();
        objectDict.get("group_2").show();
        objectDict.get("loop_2").show();
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        doAudio(currentEvent());
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demo3a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.5f, 0.7f, 0.0f, 0.4f, true, this);
        waitAudio();
        //
        action_playNextDemoSentence(false); // Two cakes
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_1");
        showControls("loop_1");
        equation.get(0).show();
        unlockScreen();
        //
        OC_Generic.pointer_moveToObjectByName("loop_1", -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // add one cake.
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_2");
        showControls("loop_2");
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        //
        OC_Generic.pointer_moveToObjectByName("loop_2", -10, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Now there are three cakes.
        OC_Generic.pointer_moveToObjectByName("loop_big", -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OBGroup group1 = (OBGroup) objectDict.get("group_1");
        OBGroup group2 = (OBGroup) objectDict.get("group_2");
        //
        action_playNextDemoSentence(false); // One.
        OC_Generic.pointer_moveToObject(group1.objectDict.get("obj_2"), -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Two.
        OC_Generic.pointer_moveToObject(group1.objectDict.get("obj_1"), -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Three.
        OC_Generic.pointer_moveToObject(group2, -10, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OBControl number = numbers.get("number_3");
        action_playNextDemoSentence(false); // Touch three.
        OC_Generic.pointer_moveToObject(number, -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToObject(number, -20, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio("correct", false);
        //
        lockScreen();
        action_highlight(number);
        hideControls("loop.*");
        showControls("loop_big");
        equation.get(3).show();
        equation.get(4).show();
        unlockScreen();
        //
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Look how we show it
        OC_Generic.pointer_moveToObject(equation.get(2), 0, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // This means ADD
        OC_Generic.pointer_moveToObject(equation.get(1), -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // This means GIVES or EQUALS
        OC_Generic.pointer_moveToObject(equation.get(3), -5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 0; i < 5; i++)
        {
            OC_Generic.pointer_moveToObject(equation.get(i), -20+i*5, (i == 0 ? 0.6f : 0.3f), EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            action_playNextDemoSentence(false);
            action_resizeLabel(equation.get(i), true);
            waitAudio();
            action_resizeLabel(equation.get(i), false);
        }
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo3b() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(true); // Now your turn.
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Two cakes
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_1");
        showControls("loop_1");
        equation.get(0).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // add two cakes
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_2");
        showControls("loop_2");
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demo3j() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(false);
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_1");
        showControls("loop_1");
        equation.get(0).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false);
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_2");
        showControls("loop_2");
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false);
        playSfxAudio("add_object", false);
        lockScreen();
        showControls("group_3");
        showControls("loop_3");
        equation.get(3).show();
        equation.get(4).show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3);
        //
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void demo3k() throws Exception
    {
        demo3j();
    }


    public OBControl findTarget (PointF pt)
    {
        List<OBPath> values = new ArrayList(numbers.values());
        return finger(0, 2, (List<OBControl>) (Object) values, pt);
    }
}
