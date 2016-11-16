package org.onebillion.onecourse.mainui.oc_countingto3;

import android.graphics.Path;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_DragObjectsToCorrectPlace;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 17/06/16.
 */
public class OC_CountingTo3_S4 extends OC_Generic_DragObjectsToCorrectPlace
{

    public OC_CountingTo3_S4 ()
    {
        super();
    }


    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        //
        for(OBControl number : filterControls("number.*"))
        {
            OBLabel label = action_createLabelForControl(number, 1.0f);
        }
    }

    public void setScene4a()
    {
        setSceneXX(currentEvent());
        //
        for (OBControl control : filterControls("frog.*"))
        {
            control.hide();
        }
        //
        for(int i = 0; i <= 3; i++)
        {
            OBControl number = objectDict.get("number_" + i);
            OBControl box = objectDict.get("box_" + i);
            number.setPosition(box.position());
            number.hide();
        }
    }




    public String action_getObjectPrefix()
    {
        return "number";
    }



    public String action_getContainerPrefix()
    {
        return "box";
    }




    public void fin()
    {
        goToCard(OC_CountingTo3_S4f.class, "event4");
    }




    public void demo4a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look
        OC_Generic.pointer_moveToObjectByName("platform_0", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();

        action_playNextDemoSentence(false); // No frogs on this rock.
        OC_Generic.pointer_moveToObjectByName("box_0", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        action_playNextDemoSentence(false); // Zero
        showControls("number_0");
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 3; i++)
        {
            String numberName = "number_" + i;
            String boxName = "box_" + i;
            String platformName = "platform_" + i;
            String controls = "frog_" + i + "_.*";
            //
            OC_Generic.pointer_moveToObjectByName(platformName, -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            action_playNextDemoSentence(false); // No Frogs. One Frog. Two Frogs. Three Frogs
            lockScreen();
            showControls(controls);
            unlockScreen();
            waitAudio();
            //
            OC_Generic.pointer_moveToObjectByName(boxName, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            action_playNextDemoSentence(false); // Zero. One. Two. Three.
            lockScreen();
            showControls(numberName);
            unlockScreen();
            waitAudio();
            waitForSecs(0.3f);
        }

        thePointer.hide();
        waitForSecs(0.3);
        //
        List<OBControl> numbers = filterControls("number.*");
        for (OBControl number : numbers)
        {
            PointF originalPosition = OC_Generic.copyPoint((PointF)number.propertyValue("originalPosition"));
            float distance = OB_Maths.PointDistance(originalPosition, number.position());
            Path path = OBUtils.SimplePath(number.position(), originalPosition, distance / 5f);
            OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
            OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
        waitForSecs(0.5);
        //
        nextScene();
    }


    public void demo4b() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        waitForSecs(0.7f);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now Look.
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObjectByName("platform_2", -25, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        action_playNextDemoSentence(true); // Two frogs.
        waitForSecs(0.3f);
        //
        OBControl number = objectDict.get("number_2");
        OC_Generic.pointer_moveToObject(number, -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        PointF destination = objectDict.get("box_2").position();
        OC_Generic.pointer_moveToPointWithObject(number, destination, -25, 0.6f, true, this);
        waitForSecs(0.3f);
        //
        OC_Generic.pointer_moveToObject(number, -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(true); // Two
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.7f);
        //
        PointF originalPosition = OC_Generic.copyPoint((PointF)number.propertyValue("originalPosition"));
        float distance = OB_Maths.PointDistance(originalPosition, number.position());
        Path path = OBUtils.SimplePath(number.position(), originalPosition, distance / 5f);
        OBAnim anim = OBAnim.pathMoveAnim(number, path, false, 0);
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.3f);
        //
        nextScene();
    }
}
