package com.maq.xprize.onecourse.mainui.oc_counting5and10;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Counting5and10_S1 extends OC_Generic_Event
{
    OBLabel number;
    int counter;
    int startingNumber;




    public void fin ()
    {
        goToCard(OC_Counting5and10_S1f.class, "event1");
    }




    public void setSceneXX (String scene)
    {
        deleteControls("group.*");
        deleteControls("line.*");
        //
        super.setSceneXX(scene);
        //
        OC_Generic.colourObjectsWithScheme(this);
        //
        if (number == null)
        {
            OBControl frame = objectDict.get("number");
            number = action_createLabelForControl(frame, 1.2f, false);
            number.setString("30");
            frame.hide();
            OC_Generic.sendObjectToTop(number, this);
        }
        //
        hideControls("group.*");
        hideControls("line.*");
        objectDict.get("line_1").show();
        number.hide();
        counter = 0;
        startingNumber = 0;
    }




    public void doAudio (String scene) throws Exception
    {
        List audio = getAudioForScene(currentEvent(), "PROMPT");
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT"));
        //
        playAudioQueued(audio, false);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                action_flashLine(counter);
            }
        });
        //
        final long statusTimeControl = statusTime;
        //
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run () throws Exception
            {
                waitForSecs(1.0f); //needs a bit of a delay to start playing the PROMPT audio
                //
                waitAudio();
                waitForSecs(5.0f);
                //
                if (!statusChanged(statusTimeControl))
                {
                    playAudioQueuedScene("REMINDER", 0.3f, false);
                }
            }
        });
    }



    public void demo1a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        demoButtons();
        //
        action_playNextDemoSentence(false); // Now …let's count : TENS!
        //
        OC_Generic.pointer_moveToObjectByName("line_1", -5f, 0.9f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("group_1", -5f, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio(eventAttributes.get("sound"), false);
        //
        lockScreen();
        showControls("group_1");
        OBControl line = objectDict.get("line_1");
        line.disable();
        line = objectDict.get("line_2");
        line.show();
        number.setString("10");
        number.show();
        counter++;
        unlockScreen();
        //
        OC_Generic.pointer_moveToObjectByName("group_1", -5f, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Ten birds.
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(number, 10f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // TEN.
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("line_1", -5f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        OC_Generic.pointer_moveToObjectByName("group_2", -5f, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        playSfxAudio(eventAttributes.get("sound"), false);
        //
        lockScreen();
        showControls("group_2");
        line = objectDict.get("line_2");
        line.disable();
        line = objectDict.get("line_3");
        line.show();
        number.setString("20");
        counter++;
        unlockScreen();
        //
        OC_Generic.pointer_moveToObjectByName("group_2", -5f, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Twenty birds.
        waitAudio();
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(number, 10f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // TWENTY.
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        //
        doAudio(currentEvent());
    }


    public void action_flashLine (int counterCheck) throws Exception
    {
        if (counterCheck == counter)
        {
            OBControl line = objectDict.get(String.format("line_%d", counter + 1));
            if (line != null)
            {
                line.enable();
                line.setOpacity(0.5f);
                waitForSecs(0.3f);
                //
                line.setOpacity(1.0f);
                waitForSecs(0.3f);
                //
                action_flashLine(counterCheck);
            }
        }
    }



    public void checkLine () throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        OBGroup group = (OBGroup) objectDict.get(String.format("group_%d", counter + 1));
        OBControl oldLine = objectDict.get(String.format("line_%d", counter + 1));
        oldLine.disable();
        OBControl line = objectDict.get(String.format("line_%d", counter + 2));
        //
        lockScreen();
        if (group != null) group.show();
        number.setString(String.format("%d", startingNumber + 10 * (counter + 1)));
        number.show();
        unlockScreen();
        //
        playSfxAudio(eventAttributes.get("sound"), false);
        int audioIndex = (currentEvent().equals("1a")) ? counter - 2 : counter;
        playAudioScene("CORRECT", audioIndex, false);
        counter++;
        waitAudio();
        //
        if (line == null)
        {
            waitForSecs(0.3f);
            //
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            //
            playAudioQueuedScene("FINAL", 0.3f, true);
            nextScene();
        }
        else
        {
            lockScreen();
            line.show();

            unlockScreen();
            //
            revertStatusAndReplayAudio();
            //
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    waitForSecs(1.0f);
                    action_flashLine(counter);

                }
            });
        }
    }




    public Object findLine (PointF pt)
    {
        return finger(0, 2, filterControls("line.*"), pt, true);
    }




    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            Object line = findLine(pt);
            if (line != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkLine();

                    }
                });
            }
        }
    }




}
