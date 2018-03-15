package org.onebillion.onecourse.mainui.oc_sharing;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;


/**
 * Created by pedroloureiro on 09/05/2017.
 */

public class OC_Sharing_S4 extends OC_Generic_Event
{
    int correct_answer;
    List<OBControl> numbers;

    public void demo4a() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO", 0, false); // Look. FOUR children.;
        OC_Generic.pointer_moveToObjectByName("place_1", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_TOP), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false); // Touch the button to put them : groups of two.;
        OC_Generic.pointer_moveToObjectByName("button", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_RIGHT), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void finDemo4a() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToObjectByName("place_2", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_TOP), true, this);
        playAudioScene("DEMO2", 0, true); // One …;
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("place_3", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_TOP), true, this);
        playAudioScene("DEMO2", 1, true); // … two;
        //
        playAudioScene("DEMO2", 2, false); // TWO groups, with two children : each group.;
        OC_Generic.pointer_lower(this);
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
    }

    public void setScene4a()
    {
        setSceneXX(currentEvent());
        int skinColourOffset = 0;
        for (OBGroup control : (List<OBGroup>) (Object) filterControls("object.*"))
        {
            int childColour = OBConfigManager.sharedManager.getSkinColour(skinColourOffset++);
            control.substituteFillForAllMembers("colour.*", childColour);
        }
    }


    public void setSceneXX(String scene)
    {
        deleteControls("object.*");
        deleteControls("place.*");
        deleteControls("path.*");
        deleteControls("group.*");
        //
        super.setSceneXX(scene);
        //
        action_hiliteNumber(null);
        hideControls("place.*");
        objectDict.get("place_1").show();
        hideControls("path.*");
        for (OBControl control : numbers) control.hide();
    }

    public void action_prepareScene(String scene, Boolean redraw)
    {
        if (numbers == null)
        {
            numbers = new ArrayList<>();
            for (OBControl number : sortedFilteredControls("number.*"))
            {
                OBLabel label = action_createLabelForControl(number);
                number.show();
                //
                OBGroup group = new OBGroup(Arrays.asList(label, number));
                attachControl(group);
                OC_Generic.sendObjectToTop(group, this);
                group.show();
                group.objectDict.put("label", label);
                group.objectDict.put("frame", number);
                numbers.add(group);
            }
            float left = 0.0f;
            for (OBControl number : numbers)
            {
                number.setLeft(left);
                left += number.width();
            }
            OBGroup group = new OBGroup(numbers);
            attachControl(group);
            group.show();
            group.setPosition(new PointF(bounds().width() / 2f, group.position().y));
        }
        List<OBControl> controls = filterControls("place.*");
        for (OBControl control : controls)
        {
            OBLabel label = action_createLabelForControl(control);
            String label_id = (String) control.attributes().get("id");
            if (objectDict.get(label_id) == control) objectDict.remove(label_id);
            objectDict.put(label_id, label);
            detachControl(control);
        }
        correct_answer = Integer.parseInt(eventAttributes.get("correct_answer"));
    }


    public void action_hiliteButton(OBGroup button, boolean value)
    {
        if (value)
        {
            OC_Generic.colourObject(button, OBUtils.colorFromRGBString("255,50,50"));
        }
        else
        {
            OC_Generic.colourObject(button, OBUtils.colorFromRGBString("193,33,40"));
        }

    }


    public void action_hiliteNumber(OBGroup number)
    {
        lockScreen();
        for (OBGroup control : (List<OBGroup>) (Object) numbers)
        {
            if (control.equals(number))
            {
                control.objectDict.get("frame").setFillColor(Color.YELLOW);
            }
            else
            {
                control.objectDict.get("frame").setFillColor(Color.WHITE);
            }
        }
        unlockScreen();
    }


    public Object findButton(PointF pt)
    {
        return finger(0, 2, filterControls("button.*"), pt, true);

    }

    public Object findNumber(PointF pt)
    {
        return finger(0, 1, numbers, pt, true);

    }

    public void checkButton(OBGroup button) throws Exception
    {
        setStatus(STATUS_CHECKING);
        playAudio(null);
        playSfxAudio("separate_objects", false);
        action_hiliteButton(button, true);
        //
        List animations = new ArrayList<>();
        //
        lockScreen();
        for (OBGroup group : (List<OBGroup>) (Object) filterControls("group.*"))
        {
            group.setAnchorPoint(new PointF(0.5f, 1f));
            String pathID = ((String) group.attributes().get("id")).replaceAll("group", "path");
            OBPath path = (OBPath) objectDict.get(pathID);
            //
            if (path != null)
            {
                path.sizeToBox(this.boundsf());
                OBAnim anim = OBAnim.pathMoveAnim(group, path.path(), false, 0);
                animations.add(anim);
            }
        }
        unlockScreen();
        //
        objectDict.get("place_1").hide();
        OBAnimationGroup.runAnims(animations, 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        action_hiliteButton(button, false);
        waitForSecs(0.3f);
        //
        lockScreen();
        for (int i = 1; i <= filterControls("group.*").size(); i++)
        {
            objectDict.get(String.format("place_%d", i + 1)).show();
        }
        unlockScreen();
        //
        waitSFX();
        waitForSecs(1.0f);
        //
        lockScreen();
        for (OBControl control : numbers) control.show();
        unlockScreen();
        //
        OBUtils.runOnOtherThreadDelayed(1.2f, new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                lockScreen();
                for (int i = 1; i <= filterControls("group.*").size(); i++)
                {
                    objectDict.get(String.format("place_%d", i + 1)).hide();
                }
                unlockScreen();
            }
        });
        setStatus(STATUS_WAITING_FOR_OBJ_CLICK);
        //
        playAudioQueuedScene("PROMPT2", 300, false);
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
    }


    public void checkNumber(OBGroup number) throws Exception
    {
        setStatus(STATUS_CHECKING);
        action_hiliteNumber(number);
        int answer = Integer.parseInt((String)number.objectDict.get("frame").attributes().get("number"));
        //
        if (correct_answer == answer)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            if (!performSel("finDemo", currentEvent()))
            {
                playAudioQueuedScene("CORRECT", 300, true);
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("FINAL", 300, true);
            }
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("INCORRECT", 300, false);
            action_hiliteNumber(null);
            setStatus(STATUS_WAITING_FOR_OBJ_CLICK);
        }
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final Object button = findButton(pt);
            if (button != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton((OBGroup) button);
                    }
                });
            }
        }
        else if (status() == STATUS_WAITING_FOR_OBJ_CLICK)
        {
            final Object number = findNumber(pt);
            if (number != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkNumber((OBGroup) number);
                    }
                });
            }
        }
    }


}
