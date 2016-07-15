package org.onebillion.xprz.mainui.x_countingto3;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;

import java.util.List;

/**
 * Created by pedroloureiro on 17/06/16.
 */
public class X_CountingTo3_S1 extends XPRZ_Generic_SelectCorrectObject
{
    public X_CountingTo3_S1()
    {
        super();
    }


    @Override
    public String action_getObjectPrefix()
    {
        return "platform";
    }


    public void placeObjectWithSFX(String object) throws Exception
    {
        playSfxAudio("placeObject", false);
        objectDict.get(object).show();
        waitSFX();
    }

    @Override
    public void action_highlight(OBControl control) throws Exception
    {
        lockScreen();
        String controlName = (String) control.attributes().get("id");
        String platformNumber = controlName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        for(OBControl item : controls)
        {
            item.highlight();
        }
        control.highlight();
        unlockScreen();
    }


    @Override
    public void action_lowlight(OBControl control) throws Exception
    {
        lockScreen();
        String controlName = (String) control.attributes().get("id");
        String platformNumber = controlName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        for(OBControl item : controls)
        {
            item.lowlight();
        }
        control.lowlight();
        unlockScreen();
    }


    @Override
    public void action_answerIsCorrect(OBControl target) throws Exception
    {
        playAudioQueuedScene(currentEvent(), "CORRECT", false);
        action_animatePlatform(target, true);
        waitAudio();
        //
        //
        nextScene();
    }




    // DEMOS


    public void demo1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        demoButtons();
        waitForSecs(0.7);
        //
        action_playNextDemoSentence(false); // Now Look
        movePointerToPoint(objectDict.get("platform_1").position(), -10, 0.6f, true);
        waitAudio();
        //
        placeObjectWithSFX("frog_1_1");
        action_playNextDemoSentence(true); // One frog on a rock
        waitForSecs(0.2);
        //
        movePointerToPoint(objectDict.get("platform_2").position(), -10, 0.6f, true);
        placeObjectWithSFX("frog_2_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("frog_2_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Two frogs on a rock
        waitForSecs(0.2);
        //
        movePointerToPoint(objectDict.get("platform_3").position(), -10, 0.6f, true);
        placeObjectWithSFX("frog_3_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("frog_3_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        placeObjectWithSFX("frog_3_3");
        action_playNextDemoSentence(true); // Three
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Three frogs on a rock
        waitForSecs(0.2);
        //
        thePointer.hide();
        nextScene();
    }



    public void demo1e() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        //
        placeObjectWithSFX("bird_1_1");
        action_playNextDemoSentence(true); // One bird on a branch
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_2_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_2_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Two birds on a branch
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        placeObjectWithSFX("bird_3_3");
        action_playNextDemoSentence(true); // Three
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Three birds on a branch
        waitForSecs(0.2);
        //
        nextScene();
    }



    public void demo1j() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        //
        placeObjectWithSFX("ladybug_1_1");
        action_playNextDemoSentence(true); // One ladybug on a leaf
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_2_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_2_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Two ladybugs on a leaf
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_1");
        action_playNextDemoSentence(true); // One
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_2");
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.2);
        //
        placeObjectWithSFX("ladybug_3_3");
        action_playNextDemoSentence(true); // Three
        waitForSecs(0.2);
        //
        action_playNextDemoSentence(true); // Three ladybug on a leaf
        waitForSecs(0.2);
        //
        nextScene();
    }

}
