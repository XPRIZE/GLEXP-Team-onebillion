package org.onebillion.onecourse.utils;

import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OC_SectionController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 28/07/16.
 */
public class OBMisc
{
    public static Map<String,Integer> loadEventColours(OC_SectionController controller)
    {

        Map<String,Integer> map = new ArrayMap<>();
        for(String key : controller.eventAttributes.keySet())
        {
            if(key.startsWith("colour_"))
            {
                map.put(key.substring(7),OBUtils.colorFromRGBString(controller.eventAttributes.get(key)));
            }
        }

        return map;
    }

    public static void doSceneAudio(float remindDelay, String event, long statusTime, String postfix, OC_SectionController controller) throws Exception
    {
        String repeat = String.format("PROMPT%s.REPEAT",postfix);
        String prompt = String.format("PROMPT%s",postfix);
        String remind = String.format("REMIND%s",postfix);

        List<String> repeatAudio = controller.getAudioForScene(event,repeat);
        if(repeatAudio != null && repeatAudio.size() > 0)
            controller.setReplayAudio(OBUtils.insertAudioInterval(repeatAudio,300));
        else
            controller.setReplayAudio(OBUtils.insertAudioInterval(controller.getAudioForScene(event,String.format("REPEAT%s",postfix)),300));

        controller.playAudioQueuedScene(event,prompt,0.3f,true);

        List<String> remindAudio = controller.getAudioForScene(event,remind);
        if(remindDelay > 0 && remindAudio != null && remindAudio.size() > 0)
            controller.reprompt(statusTime,OBUtils.insertAudioInterval(remindAudio,300),remindDelay);

    }

    public static void  doSceneAudio(float remindDelay,String event, long statusTime, OC_SectionController controller) throws Exception
    {
        doSceneAudio(remindDelay,event,statusTime,"",controller);
    }

    public static void  doSceneAudio(float remindDelay, long statusTime, OC_SectionController
            controller) throws Exception
    {
        doSceneAudio(remindDelay,controller.currentEvent(),statusTime,controller);
    }

    public static void prepareForDragging(OBControl cont, PointF pt, OC_SectionController controller)
    {
        controller.target = cont;
        cont.setZPosition(cont.zPosition()+10);
        controller.dragOffset = OB_Maths.DiffPoints(cont.position(), pt);
    }

    public static void moveControlWithAttached(final OBControl mainControl, final List<OBControl> attached, PointF point, float duration, int easing, OC_SectionController controller)
    {
        final List<PointF> points = new ArrayList<>();
        for(int i=0; i<attached.size(); i++)
        {
            points.add(OB_Maths.DiffPoints(attached.get(i).position(), mainControl.position()));
        }

        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(point, mainControl),
                new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        for(int i=0; i<attached.size(); i++)
                        {
                            attached.get(i).setPosition(OB_Maths.AddPoints(mainControl.position(), points.get(i)));
                        }
                    }
                }),duration,true,easing, controller);
    }

    public static void loadNumbersFrom(int from,int to, int colour, String name, String prefix, OC_SectionController controller)
    {
        OBControl box = controller.objectDict.get(name);
        Typeface font = OBUtils.standardTypeFace();
        float fontSize = 60*box.height()/62.0f;

        for(int i = from; i <= to; i++)
        {
            OBLabel numLabel = new OBLabel(String.format("%d",i),font,fontSize);
            numLabel.setPosition(OB_Maths.locationForRect((i-from+1.0f)*(1.0f/(to-from+2)),0.5f,box.frame()));
            controller.attachControl(numLabel);
            numLabel.setColour(colour);
            numLabel.setProperty("num_value",i);
            controller.objectDict.put(String.format("%s%d",prefix,i),numLabel);
            numLabel.hide();
            numLabel.setZPosition(10);
        }

    }

    public static OBAnim attachedAnim(final OBControl mainControl, final List<OBControl> attached)
    {

        final List<PointF> points = new ArrayList<>();
        for(int i=0; i<attached.size(); i++)
        {
            points.add(OB_Maths.DiffPoints(attached.get(i).position(), mainControl.position()));
        }

        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(int i=0; i<attached.size(); i++)
                {
                    attached.get(i).setPosition(OB_Maths.AddPoints(mainControl.position(), points.get(i)));
                }
            }
        };

        return anim;
    }

    public static void insertLabelIntoGroup(OBGroup group, int num, float size, int colour, PointF position, OC_SectionController controller)
    {
        Typeface font = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(String.format("%d",num),font,size);
        label.setColour(colour);
        label.setScale(1.0f/group.scale());
        OBGroup numGroup = new OBGroup(Collections.singletonList((OBControl)label));
        numGroup.sizeToTightBoundingBox();
        controller.attachControl(numGroup);
        numGroup.setPosition(position);

        group.insertMember(numGroup,0,"number");
        numGroup.setZPosition(10);
        group.objectDict.put("label",label);
    }

    public static OBControl nearestControlToPoint(List<OBControl> controls, PointF point)
    {
        OBControl result = null;

        if(controls == null || controls.size() == 0)
            return null;

        if(controls.size() == 1)
            return controls.get(0);

        float dist =  -1;
        for(OBControl control : controls)
        {
            float currentDist = OB_Maths.PointDistance(point, control.position());
            if(result == null || dist > currentDist || dist == -1)
            {
                result = control;
                dist = currentDist;
            }
        }
        return result;
    }

    public static PointF copyPoint(PointF original)
    {
        return new PointF(original.x, original.y);
    }
}
