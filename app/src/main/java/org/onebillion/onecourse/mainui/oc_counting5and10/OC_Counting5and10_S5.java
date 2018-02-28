package org.onebillion.onecourse.mainui.oc_counting5and10;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.onebillion.onecourse.utils.OBUtils.RectOverlapRatio;

/**
 * Created by pedroloureiro on 16/03/2017.
 */

public class OC_Counting5and10_S5 extends OC_Generic_Event
{
    int correctAnswer;
    int visibleObjectCount;
    OBLabel bigNumber;


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes";
    }


//    public void fin ()
//    {
//        goToCard(OC_Counting5and10_S5h.class, "event5");
//    }


    public void setSceneXX (String scene)
    {
        if (bigNumber != null)
        {
            detachControl(bigNumber);
        }
        deleteControls(".*");
        //
        super.setSceneXX(scene);
        //
        OBControl frame = objectDict.get("bigNumber");
        bigNumber = action_createLabelForControl(frame, 1.0f, false);
        frame.hide();
        bigNumber.setString(eventAttributes.get("correctAnswer"));
        //
        for (OBGroup group : (List<OBGroup>) (Object) filterControls("group.*"))
        {
            group.setProperty("originalPosition", OC_Generic.copyPoint(group.position()));
            group.hide();
        }
        //
        correctAnswer = Integer.parseInt(eventAttributes.get("correctAnswer"));
        visibleObjectCount = 1;
    }


    public void setScene5a ()
    {
        setSceneXX(currentEvent());
        //
        bigNumber.setString("10");
    }


    public void demo5a () throws Exception
    {
        setStatus(STATUS_BUSY);
        //
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.I want TEN cakes.
        OC_Generic.pointer_moveToObject(bigNumber, 5f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3f);
        //
        OBGroup group = (OBGroup) objectDict.get("group_1");
        OC_Generic.pointer_moveToObject(group, -10f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_playNextDemoSentence(false); // Five.
        playSfxAudio("cake_show", false);
        group.show();
        waitAudio();
        waitForSecs(0.3f);
        //
        group = (OBGroup) objectDict.get("group_2");
        OC_Generic.pointer_moveToObject(group, -10f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        action_playNextDemoSentence(false); // Ten.
        playSfxAudio("cake_show", false);
        group.show();
        waitAudio();
        waitForSecs(0.3f);
        //
        group = (OBGroup) objectDict.get("group_1");
        OC_Generic.pointer_moveToObject(group, -10f, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        action_playNextDemoSentence(false); // Ten cakes.
        waitAudio();
        waitForSecs(0.3f);
        //
        thePointer.hide();
        waitForSecs(0.3f);
        //
        playSfxAudio("object_hide", false);
        //
        lockScreen();
        hideControls("group.*");
        bigNumber.hide();
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        playSfxAudio("number_show", false);
        //
        lockScreen();
        bigNumber.setString(eventAttributes.get("correctAnswer"));
        bigNumber.show();
        unlockScreen();
        //
        waitForSecs(0.3f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        //
        doAudio(currentEvent());
    }


    public boolean action_isPositionValid (PointF pt)
    {
        OBControl container = objectDict.get("container");
        for (OBGroup group : (List<OBGroup>) (Object) filterControls("group.*"))
        {
            if (group.hidden()) continue;
            RectF rect1 = new RectF(pt.x, pt.y, group.width(), group.height());
            PointF worldPosition = group.getWorldPosition();
            RectF rect2 = new RectF(worldPosition.x, worldPosition.y, group.width(), group.height());
            if (RectOverlapRatio(rect1, rect2) > 0.05) return false;
            if (pt.y - 0.5 * group.height() < container.top()) return false;
            if (pt.y + 0.5 * group.height() > container.bottom()) return false;
        }
        return true;

    }


    public Object findContainer (PointF pt)
    {
        return finger(0, 2, filterControls("container"), pt);

    }


    public void checkContainer (PointF pt) throws Exception
    {
        saveStatusClearReplayAudioSetChecking();
        //
        OBControl container = objectDict.get("container");
        OBGroup group = (OBGroup) objectDict.get(String.format("group_%d", visibleObjectCount));
        if (action_isPositionValid(pt))
        {
            playSfxAudio("cake_show", false);
            //
            lockScreen();
            //
            group.setPosition(OC_Generic.copyPoint(pt));
            group.setLeft(Math.max(group.left(), container.left()));
            group.setRight(Math.min(group.right(), container.right()));
            group.setTop(Math.max(group.top(), container.top()));
            group.setBottom(Math.min(group.bottom(), container.bottom()));
            group.show();
            //
            unlockScreen();
            //
            if (audioSceneExists("CORRECT"))
            {
                waitForSecs(0.3f);
                playAudioScene("CORRECT", visibleObjectCount - 1, false);
            }
            //
            if (visibleObjectCount * 5 == correctAnswer)
            {
                waitAudio();
                List animations = new ArrayList();
                //
                for (int i = 1; i <= visibleObjectCount; i++)
                {
                    group = (OBGroup) objectDict.get(String.format("group_%d", i));
                    OBAnim moveAnim = OBAnim.moveAnim((OC_Generic.copyPoint((PointF) group.propertyValue("originalPosition"))), group);
                    animations.add(moveAnim);

                }
                playSfxAudio("object_lineup", false);
                OBAnimationGroup.runAnims(animations, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                waitForSecs(0.3f);
                //
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                //
                playAudioQueuedScene("FINAL", 300, true);
                nextScene();
                //
                return;
            }
            else
            {
                visibleObjectCount++;
                revertStatusAndReplayAudio();
            }
        }
        else
        {
            revertStatusAndReplayAudio();
        }
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            Object container = findContainer(pt);
            if (container != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkContainer(pt);

                    }
                });
            }
        }
    }
}