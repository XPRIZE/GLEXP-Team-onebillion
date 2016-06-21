package org.onebillion.xprz.mainui.x_counting5and10;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 21/06/16.
 */
public class X_Counting5and10_S4 extends XPRZ_Generic_SelectCorrectObject
{

    OBGroup childrenMask, alignmentGroup;
    int correctAnswer;

    public X_Counting5and10_S4()
    {
        super();
    }


    public String action_getObjectPrefix()
    {
        return "number";
    }



    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        //
        action_resetHands();
        //
        for (OBControl number : filterControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number, 1.0f);
            number.setProperty("label", label);
        }
    }



    public void setScene4a()
    {
        setSceneXX(currentEvent());
        //
        OBGroup child = (OBGroup) objectDict.get("child");
        child.hideMembers("hand.*");
        child.showMembers("hand_0");
        child.substituteFillForAllMembers("colour.*", OBUtils.SkinColour(0));
    }



    public void setScene4b()
    {
        setSceneXX(currentEvent());
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
        int count = 0;
        List<OBControl> children = filterControls("child.*");
        for (OBControl child : children)
        {
            OBGroup group = (OBGroup) child;
            group.substituteFillForAllMembers("colour.*", OBUtils.SkinColour(count * 2));
            count++;
        }
        //
        List<OBControl> groupArray = new ArrayList<OBControl>(children);
        OBControl container = objectDict.get("container");
        OBControl mask = container.copy();
        //
        groupArray.add(container);
        groupArray.add(mask);
        //
        childrenMask = new OBGroup(groupArray);
        attachControl(childrenMask);
        childrenMask.show();
        childrenMask.maskControl = mask;
        //
        correctAnswer = Integer.parseInt(eventAttributes.get("correctAnswer"));
        action_resetHands();
        //
        hideControls("child.*");
        objectDict.get("child_1").show();
        hideControls("label.*");
    }





    public void demo4a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        OBGroup child = (OBGroup) objectDict.get("child");
        child.texturise(true, this);
        action_playNextDemoSentence(true); // Look. A hand has five fingers.
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 5; i++)
        {
            action_playNextDemoSentence(false); // One. Two. Three. Four. Five.
            lockScreen();
            child.hideMembers("hand.*");
            child.showMembers(String.format("hand_%d", i));
            child.setNeedsRetexture();
            unlockScreen();
            waitAudio();
            waitForSecs(0.15);
        }
        waitForSecs(0.3);
        //
        nextScene();
    }





    public void demo4b() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        action_playNextDemoSentence(true); // So let's use hands to count : FIVES.
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 10; i++)
        {
            OBGroup number = (OBGroup) objectDict.get(String.format("number_%d", i * 5));
            //
            action_playNextDemoSentence(false); // FIVE. TEN. FIFTEEN. TWENTY. TWENTY-FIVE. THIRTY. THIRTY-FIVE. FORTY. FORTY-FIVE. FIFTY.
            lockScreen();
            action_highlight(number);
            action_showHands(5 * i, true);
            unlockScreen();
            //
            waitAudio();
            action_lowlight(number);
            waitForSecs(0.3f);
        }
        action_playNextDemoSentence(true); // FIFTY fingers!
        waitForSecs(0.3f);
        //
        lockScreen();
        action_resetHands();
        unlockScreen();
        //
        action_playNextDemoSentence(true); // Count with me!
        waitForSecs(0.3f);
        //
        for (int i = 1; i <= 10; i++)
        {
            OBGroup number = (OBGroup) objectDict.get(String.format("number_%d", i * 5));
            //
            action_playNextDemoSentence(false); // FIVE. TEN. FIFTEEN. TWENTY. TWENTY-FIVE. THIRTY. THIRTY-FIVE. FORTY. FORTY-FIVE. FIFTY.
            lockScreen();
            action_highlight(number);
            action_showHands(5 * i, false);
            unlockScreen();
            waitAudio();
            //
            action_lowlight(number);
            waitForSecs(0.3f);
        }
        //
        lockScreen();
        action_resetHands();
        unlockScreen();
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }




    public void finDemo4b() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO2", 0, true); // Let's see fifteen fingers!
        waitForSecs(0.3);
        //
        finDemoXX(1, "DEMO2");
    }




    public void demo4c() throws Exception
    {
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }




    public void finDemo4c() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true); // Look!
        waitForSecs(0.3);
        //
        finDemoXX(1, "DEMO");
    }



    public void demo4f() throws Exception
    {
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
        //
    }



    public void finDemo4f() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true); // Let's see FIFTY fingers!
        waitForSecs(0.3);
        //
        finDemoXX(1, "DEMO");
    }




    public void finDemoXX(int offset, String audioScene) throws Exception
    {
        int correctAnswer = Integer.parseInt(eventAttributes.get("correctAnswer"));
        int loops = correctAnswer / 5;
        //
        for (int i = 1; i <= loops; i++)
        {
            OBGroup number = (OBGroup) objectDict.get(String.format("number_%d", 5 * i));
            OBPath box = (OBPath) number.objectDict.get("frame");
            //
            lockScreen();
            action_showHands(i * 5, false);
            box.setFillColor(OBUtils.colorFromRGBString("255,234,121"));
            unlockScreen();
            //
            playAudioQueuedSceneIndex(currentEvent(), audioScene, offset + i - 1, true); // FIVE. TEN. FIFTEEN. TWENTY. TWENTY-FIVE. THIRTY. THIRTY-FIVE. FORTY. FORTY-FIVE. FIFTY.
            //
            lockScreen();
            box.setFillColor(Color.WHITE);
            unlockScreen();
            //
            waitForSecs(0.3);
        }
    }




    public void action_resetHands()
    {
        for (OBControl control : filterControls("child.*"))
        {
            OBGroup group = (OBGroup) control;
            group.hideMembers("frame.*");
            group.showMembers("frame_0");
            group.setNeedsRetexture();
        }
        if (childrenMask != null)
            childrenMask.setNeedsRetexture();
    }



    public void action_showHands(int number, Boolean showChild)
    {
        for (int i = 1; i <= 5; i++)
        {
            OBGroup child = (OBGroup) objectDict.get(String.format("child_%d", i));
            child.hideMembers("frame.*");
            //
            int totalHands = number / 5;
            if (totalHands >= 2*i)
            {
                child.showMembers("frame_2");
                if (showChild) child.show();
            }
            else if (totalHands == 2*i-1)
            {
                child.showMembers("frame_1");
                if (showChild) child.show();
            }
            else
            {
                child.showMembers("frame_0");
            }
            child.setNeedsRetexture();
        }
        childrenMask.setNeedsRetexture();
    }



    public void action_highlight(OBGroup control) throws Exception
    {
        lockScreen();
        OBLabel label = (OBLabel) control.objectDict.get("label");
        if (label != null)
        {
            label.setColour(Color.RED);
        }
        control.setNeedsRetexture();
        unlockScreen();
    }



    public void action_lowlight(OBGroup control) throws Exception
    {
        lockScreen();
        OBLabel label = (OBLabel) control.objectDict.get("label");
        if (label != null)
        {
            label.setColour(Color.BLACK);
        }
        control.setNeedsRetexture();
        unlockScreen();
    }




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
                if (!performSel("finDemo", currentEvent()))
                {
                    finDemoXX(0, "CORRECT");
                }
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
