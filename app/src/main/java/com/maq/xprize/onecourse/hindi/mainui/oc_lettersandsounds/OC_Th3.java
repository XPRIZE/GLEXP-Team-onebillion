package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 29/06/16.
 */
public class OC_Th3 extends OC_Th2
{
    List pairedObjects;


    public OC_Th3 ()
    {
        super();
    }


    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
        //
        pairedObjects = new ArrayList();
    }




    public void pickCorrectAnswers ()
    {
        answers = new ArrayList<String>();
        for (int i = 0; i < sets.size(); i++)
        {
            List<String> pickedWords = new ArrayList<String>();
            for (String word : sets.get(i))
            {
                if (pickedWords.contains(word))
                {
                    answers.add(word);
                    break;
                }
                else
                {
                    pickedWords.add(word);
                }
            }
        }
    }


    public void demoa () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playSceneAudio("DEMO", true);
        nextScene();
    }


    public void demob () throws Exception
    {
        long timestamp = setStatus(STATUS_DOING_DEMO);
        //
        action_popup();
        //
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 1.3f, 0f, 0.1f, true, this);
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 0.85f, -5f, 0.6f, false, this);
        //
        action_playNextDemoSentence(true); // Listen!
        waitForSecs(0.3);
        //
        action_intro(showText, false, timestamp);
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Which two said the same?
        OC_Generic.pointer_moveToRelativePointOnScreen(0.6f, 0.8f, -5f, 1.2f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        String correctAnswer = answers.get(currNo);
        List<OBGroup> correctHeads = new ArrayList<OBGroup>();
        for (OBGroup head : touchables)
        {
            String answer = (String) head.propertyValue("value");
            if (correctAnswer.equals(answer)) correctHeads.add(head);
        }
        OBGroup obj = correctHeads.get(0);
        action_playNextDemoSentence(false); // This one!
        PointF middle = OB_Maths.locationForRect(0.5f, 0.65f, new RectF(obj.frame()));
        movePointerToPoint(middle, 0, 0.6f, true);
        //
        playSfxAudio("touch", false);
        lockScreen();
        action_showState(obj, "paired");
        unlockScreen();
        waitAudio();
        //
        PointF position = OC_Generic.copyPoint(obj.position());
        position.y += 0.3 * bounds().height();
        movePointerToPoint(position, 0, 0.9f, true);
        waitForSecs(0.3);
        //
        obj = correctHeads.get(1);
        action_playNextDemoSentence(false); // And this one!
        middle = OB_Maths.locationForRect(0.5f, 0.65f, new RectF(obj.frame()));
        movePointerToPoint(middle, 0, 0.6f, true);
        //
        playSfxAudio("touch", false);
        lockScreen();
        action_showState(obj, "paired");
        unlockScreen();
        waitAudio();
        //
        position = OC_Generic.copyPoint(obj.position());
        position.y += 0.3 * bounds().height();
        movePointerToPoint(position, 0, 0.9f, true);
        waitForSecs(0.3);
        //
        if (mode.equals("word"))
        {
            if (showText)
            {
                label.show();
                waitForSecs(0.3);
            }
        }
        //
        for (OBGroup correctHead : correctHeads)
        {
            action_playSound(correctHead, timestamp, true);
            waitForSecs(0.3);
        }
        //
        lockScreen();
        for (OBGroup correctHead : correctHeads)
        {
            action_showMouthFrame(correctHead, "mouth_7");
        }
        unlockScreen();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Remember, this lets you listen again.
        PointF replayAudioPosition = OB_Maths.locationForRect(0.5f, 1.1f, new RectF(MainViewController().topRightButton.frame));
        movePointerToPoint(replayAudioPosition, 10f, 1.2f, true);
        waitAudio();
        waitForSecs(0.7);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        action_playNextDemoSentence(true); // Your Turn!
        //
        currNo++;
        //
        nextScene();
    }


    public void checkObject(OBGroup object)
    {
        long timestamp = setStatus(STATUS_CHECKING);
        //
        try
        {
            String correctAnswer = answers.get(currNo);
            String value = (String) object.propertyValue("value");
            Boolean answerIsCorrect = correctAnswer.equals(value);
            //
            playSfxAudio("touch", false);
            //
            if (answerIsCorrect)
            {
                lockScreen();
                action_showState(object, "paired");
                unlockScreen();
                //
                if (pairedObjects.contains(object))
                {
                    setStatus(STATUS_AWAITING_CLICK);
                    return;
                }
                //
                pairedObjects.add(object);
                //
                if (pairedObjects.size() < 2)
                {
                    playSceneAudio("CORRECT", false);
                    //
                    setStatus(STATUS_AWAITING_CLICK);
                    return;
                }
                //
                waitForSecs(0.3);
                //
                if (mode.equals("word"))
                {
                    if (showText)
                    {
                        label.show();
                        waitForSecs(0.3);
                    }
                }
                for (OBGroup head : (List<OBGroup>) pairedObjects)
                {
                    action_playSound(head, timestamp, true);
                    waitForSecs(0.3);
                }
                //
                lockScreen();
                for (OBGroup head : (List<OBGroup>) pairedObjects)
                {
                    action_showMouthFrame(head, "mouth_7");
                }
                unlockScreen();
                waitForSecs(0.3);
                //
                gotItRightBigTick(showTick);
                waitForSecs(0.3);
                //
                currNo++;
                if (currNo < sets.size()) waitForSecs(0.3);
                //
                pairedObjects.clear();
                nextScene();
            }
            else
            {
                object.highlight();
                waitForSecs(0.3);
                //
                gotItWrongWithSfx();;
                waitForSecs(0.3);
                //
                lockScreen();
                for (OBGroup head : touchables)
                {
                    action_showState(head, "normal");
                }
                object.lowlight();
                unlockScreen();
                //
                pairedObjects.clear();
                final long final_timestamp = setStatus(STATUS_AWAITING_CLICK);
                //
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        long st = statusTime;
                        playSceneAudioIndex("INCORRECT", 0, true);
                        if (statusChanged(st)) return;
                        waitForSecs(0.3);
                        //
                        action_intro(showText, false, final_timestamp);
                        //
                        playSceneAudioIndex("INCORRECT", 1, true);
                        if (statusChanged(st)) return;
                        waitForSecs(0.3);
                        //
                        playSceneAudioIndex("INCORRECT", 2, true);
                        if (statusChanged(st)) return;
                        waitForSecs(0.3);
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}