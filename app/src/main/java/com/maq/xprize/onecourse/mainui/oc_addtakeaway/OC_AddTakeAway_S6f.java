package com.maq.xprize.onecourse.mainui.oc_addtakeaway;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_Event;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by pedroloureiro on 04/05/2017.
 */

public class OC_AddTakeAway_S6f extends OC_Generic_Event
{
    List<OBGroup> equation;
    List<OBGroup> signs;
    OBControl correctDropDestination;
    int phase;
    String action;


    @Override
    public String action_getScenesProperty ()
    {
        return "scenes2";
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
    }

    public void setSceneXX (String scene)
    {
        List<OBControl> oldControls = filterControls(".*");
        super.setSceneXX(currentEvent());
        if (eventAttributes.get("redraw").equals("true"))
        {
            for (OBControl control : oldControls)
            {
                detachControl(control);
                String controlID = (String) control.attributes().get("id");
                if (objectDict.get(controlID) != null && objectDict.get(controlID).equals(control))
                {
                    objectDict.remove(controlID);
                }
            }
            List<OBControl> controls = sortedFilteredControls("label.*");
            equation = new ArrayList<>();
            for (OBControl control : controls)
            {
                String text = (String) control.attributes().get("text");
                if (text != null && text.length() != 0)
                {
                    OBGroup group = createGroupWithLabelFromControl(control, true);
                    objectDict.put(String.format("group_%s", control.attributes().get("id")), group);
                    equation.add(group);
                }
            }
            signs = new ArrayList<>();
            controls = sortedFilteredControls("sign.*");
            for (OBControl control : controls)
            {
                String text = (String) control.attributes().get("text");
                if (text != null && text.length() != 0)
                {
                    OBGroup group = createGroupWithLabelFromControl(control, false);
                    objectDict.put(String.format("group_%s", control.attributes().get("id")), group);
                    group.setProperty("originalPosition", OC_Generic.copyPoint(group.position()));
                    signs.add(group);
                }
            }
        }
        phase = 0;
        OC_Generic.colourObjectsWithScheme(this);
        //
        hideControls("obj.*");
        hideControls("dash.*");
        //
        for (OBControl control : equation) control.hide();
        for (OBControl control : signs) control.hide();
        //
        action = eventAttributes.get("action");
    }


    public OBGroup createGroupWithLabelFromControl (OBControl control, boolean hideFrame)
    {
        OBLabel label = action_createLabelForControl(control, 1.2f, false);
        if (hideFrame) control.hide();
        else control.show();
        //
        OBGroup labelGroup = new OBGroup(Arrays.asList((OBControl) label));
        labelGroup.setFrame(label.frame());
        labelGroup.objectDict.put("label", label);
        OC_Generic.sendObjectToTop(labelGroup, this);
//        labelGroup.sizeToTightBoundingBox();
        labelGroup.setPosition(OC_Generic.copyPoint(control.getWorldPosition()));
        //
        OBGroup group = new OBGroup(Arrays.asList(control, labelGroup));
        group.objectDict.put("label", labelGroup);
        group.objectDict.put("frame", control);
        attachControl(group);
        //
        OC_Generic.sendObjectToTop(group, this);
        group.setProperty("currentPosition", OC_Generic.copyPoint(group.position()));
        group.setProperty("text", control.attributes().get("text"));
        return group;
    }

    public PointF getSignCurrentPosition (OBControl sign)
    {
        return (PointF) sign.propertyValue("currentPosition");

    }

    public void demo6f () throws Exception
    {
        setStatus(STATUS_BUSY);
        playAudioScene("PROMPT", 0, false); // Look. Three counters.;
        playSfxAudio("add_object", false);
        //
        lockScreen();
        equation.get(0).show();
        objectDict.get("obj_1").show();
        objectDict.get("obj_2").show();
        objectDict.get("obj_3").show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("PROMPT", 1, false); // Add three more counters, on the lines.;
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT")); // Add three more counters, on the lines.;
        playSfxAudio("add_object", false);
        //
        lockScreen();
        showControls("dash.*");
        unlockScreen();
        //
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void demo6g () throws Exception
    {
        setStatus(STATUS_BUSY);
        playAudioScene("PROMPT", 0, false); // Six counters.;
        playSfxAudio("add_object", false);
        //
        lockScreen();
        equation.get(0).show();
        for (OBControl control : filterControls("obj.*")) control.show();
        unlockScreen();
        //
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("PROMPT", 1, false); // Touch two counters to take them away.;
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT")); // Touch two counters to take them away.;
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void action_startPhase2 () throws Exception
    {
        phase++;
        waitSFX();
        waitForSecs(0.3f);
        //
        playSfxAudio("add_object", false);
        //
        lockScreen();
        for (OBControl control : signs) control.show();
        correctDropDestination = ((OBGroup) equation.get(1)).objectDict.get("frame");
        correctDropDestination.show();
        ((OBGroup) equation.get(1)).objectDict.get("label").hide();
        equation.get(1).show();
        equation.get(2).show();
        unlockScreen();
        //
        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT2"));
        playAudioQueued((List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT2"), false);
    }


    public void checkDash (OBControl target) throws Exception
    {
        setStatus(STATUS_BUSY);
        OBControl dash = (OBControl) target;
        OBControl control = objectDict.get(dash.attributes().get("parent"));
        playSfxAudio("add_object", false);
        //
        lockScreen();
        control.show();
        dash.hide();
        control.disable();
        dash.disable();
        unlockScreen();
        //
        int hiddenControls = 0;
        for (OBControl object : filterControls("obj.*"))
        {
            if (object.hidden())
            {
                hiddenControls++;
            }
        }
        if (hiddenControls == 0)
        {
            action_startPhase2();

        }
        setStatus(STATUS_AWAITING_CLICK);

    }

    public void checkObject (OBControl target) throws Exception
    {
        setStatus(STATUS_BUSY);
        OBControl obj = (OBControl) target;
        playSfxAudio("remove_object", false);
        obj.hide();
        int hiddenControls = 0;
        for (OBControl control : filterControls("obj.*"))
        {
            if (control.hidden())
            {
                hiddenControls++;
            }
        }
        String text = (String) ((OBGroup) equation.get(2)).propertyValue("text");
        int correctNumber = Integer.parseInt(text);
        if (correctNumber == hiddenControls)
        {
            action_startPhase2();

        }
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkDragTarget (OBControl targ, PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target = targ;
        OC_Generic.sendObjectToTop(target, this);
        targ.animationKey = (long) OC_Generic.currentTime();
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }


    public void checkDragAtPoint (PointF pt)
    {
        try
        {
            setStatus(STATUS_BUSY);
            OBGroup sign = (OBGroup) target;
            OBControl destination = finger(0, 2, Arrays.asList(correctDropDestination), pt);
            if (destination != null)
            {
                target = null;
                String text_1 = (String) destination.attributes().get("text");
                String text_2 = (String) ((OBControl) sign.objectDict.get("frame")).attributes().get("text");
                if (text_1.equals(text_2))
                {
                    gotItRightBigTick(false);
                    moveObjectToPoint(sign, destination.getWorldPosition(), 0.2f, false);
                    waitForSecs(0.3f);
                    //
                    OBAnim dissolveAnim_1 = OBAnim.opacityAnim(0, correctDropDestination);
                    OBAnim dissolveAnim_2 = OBAnim.opacityAnim(0, ((OBGroup) sign).objectDict.get("frame"));
                    OBAnimationGroup.runAnims(Arrays.asList(dissolveAnim_1, dissolveAnim_2), 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    waitSFX();
                    waitForSecs(0.3f);
                    //
                    if (phase++ == 1)
                    {
                        playSfxAudio("add_object", false);
                        //
                        lockScreen();
                        correctDropDestination = ((OBGroup) equation.get(3)).objectDict.get("frame");
                        correctDropDestination.show();
                        ((OBGroup) equation.get(3)).objectDict.get("label").hide();
                        equation.get(3).show();
                        equation.get(4).show();
                        unlockScreen();
                        //
                        waitSFX();
                        waitForSecs(0.3f);
                        //
                        setReplayAudio((List<Object>) (Object) getAudioForScene(currentEvent(), "REPEAT3"));
                        playAudioQueued((List<Object>) (Object) getAudioForScene(currentEvent(), "PROMPT3"), false);
                    }
                    else
                    {
                        playAudioQueuedScene("CORRECT", 0.3f, true);
                        waitForSecs(0.3f);
                        //
                        playAudioQueuedScene("FINAL", 0.3f, true);
                        //
                        nextScene();
                        //
                        return;
                    }
                }
                else
                {
                    gotItWrongWithSfx();
                    moveObjectToPoint(sign, (PointF) sign.propertyValue("originalPosition"), 0.4f, false);
                }
            }
            else
            {
                moveObjectToPoint(sign, (PointF) sign.propertyValue("originalPosition"), 0.4f, false);
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public OBControl findDash (PointF pt)
    {
        OBControl c = finger(0, 2, filterControls("dash.*"), pt);
        return c;
    }


    public OBControl findSign (PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) signs, pt);
        return c;
    }


    public OBControl findObject (PointF pt)
    {
        OBControl c = finger(0, 2, filterControls("obj.*"), pt);
        return c;
    }


    public void touchDownAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl dash = findDash(pt);
            if (dash != null && action.equals("add") && phase == 0)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run () throws Exception
                    {
                        checkDash(dash);
                    }
                });
            }
            else
            {
                final OBControl sign = findSign(pt);
                if (sign != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            checkDragTarget(sign, pt);
                        }
                    });
                }
                else
                {
                    final OBControl obj = findObject(pt);
                    if (obj != null && action.equals("remove") && phase == 0)
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run () throws Exception
                            {
                                checkObject(obj);
                            }
                        });
                    }
                }
            }
        }
    }


    public void touchUpAtPoint (final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run () throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }


}
