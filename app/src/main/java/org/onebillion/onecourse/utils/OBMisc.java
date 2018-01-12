package org.onebillion.onecourse.utils;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OBSectionController;
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

    public static void doSceneAudio(float remindDelay,String event, long statusTime, OC_SectionController controller) throws Exception
    {
        doSceneAudio(remindDelay,event,statusTime,"",controller);
    }

    public static void doSceneAudio(float remindDelay, long statusTime, OC_SectionController
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

    public static List<String> arrayOfStringsWithRoot(String root,int startidx,int endidx)
    {
        List<String> arr = new ArrayList<>();
        for (int i = startidx;i <= endidx;i++)
            arr.add(String.format("%s%d",root,i));
        return arr;

    }

    public static List<OBGroup> loadNumbersInBoxes(int from, int to, int boxColour, int labelColour, String name, OBSectionController controller)
    {
        List<OBGroup> numbers = new ArrayList<>();
        OBControl numbox = controller.objectDict.get(name);
        for(int i = from; i<=to; i++)
        {
            OBControl box = new OBControl();
            box.setFrame(new RectF(0, 0, numbox.width()/ (to-from+1.0f), numbox.height()));
            box.setBackgroundColor(boxColour);
            box.setBorderColor(Color.BLACK);
            box.setBorderWidth(controller.applyGraphicScale(2));
            box.setPosition(OB_Maths.locationForRect(1.0f/(to-from+1)*(i-from),0.5f,numbox.frame()));
            box.setLeft(numbox.position().x - ((to-from+1)/2.0f *(box.width() - box.borderWidth)) + (box.width() - box.borderWidth)*(i-from)*1.0f);
            OBLabel label = new OBLabel(String.format("%d",i),OBUtils.standardTypeFace(),65.0f*numbox.height()/85.0f); label.setColour(Color.BLACK);
            label.setPosition(copyPoint(box.position()));
            label.setColour(labelColour);
            OBGroup group = new OBGroup(Arrays.asList(box,label));
            group.objectDict.put("label",label);
            group.objectDict.put("box",box);
            controller.attachControl(group);
            group.setProperty("num_val",i);
            numbers.add(group);
        }
        return numbers;
    }


    public static void setFirstLastPoints(OBPath path, PointF firstPoint, PointF lastPoint, OC_SectionController sectionController)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return;
        UPath deconPath = sectionController.deconstructedPath(sectionController.currentEvent(), name);

        USubPath subPath = deconPath.subPaths.get(0);
        ULine line = subPath.elements.get(0);
        line.pt0 = firstPoint;

        USubPath subPath2 = deconPath.subPaths.get(deconPath.subPaths.size() - 1);
        ULine line2 = subPath2.elements.get(subPath2.elements.size() - 1);
        line2.pt1 = lastPoint;
        path.setPath(deconPath.bezierPath());
    }

    public static List<Integer> stringToIntegerList(String numbers, String component)
    {
        List<Integer> result = new ArrayList<>();
        if(numbers != null)
        {
            String[] strings = numbers.split(component);
            for (String num : strings)
                result.add(Integer.valueOf(num));
        }
        return result;
    }

    public static List<Float> stringToFloatList(String numbers, String component)
    {
        List<Float> result = new ArrayList<>();
        if(numbers != null)
        {
            String[] strings = numbers.split(component);
            for (String num : strings)
                result.add(Float.valueOf(num));
        }
        return result;
    }

    public static void colourObjectFromAttributes(OBGroup obj)
    {
        for (String key : obj.attributes().keySet())
        {
            if(key.startsWith("colour_"))
            {
                int colour = OBUtils.colorFromRGBString((String)obj.attributes().get(key));
                String layer = key.replaceAll("colour_","");
                for (OBControl con: obj.filterMembers(String.format("%s.*", layer)))
                {
                    if (con.getClass() == OBPath.class)
                    {
                        ((OBPath) con).setFillColor(colour);
                    }
                    if (con.getClass() == OBGroup.class)
                    {
                        for (OBControl con2 : ((OBGroup) con).members)
                            if (con2.getClass() == OBPath.class)
                                ((OBPath) con2).setFillColor(colour);
                    }
                }
            }
        }
    }

    public static void scaleControlToControl(OBControl con1,OBControl con2, boolean widthOnly)
    {
        boolean scaleHeight = con1.width()/con1.height() < con2.width()/con2.height();
        if(widthOnly != scaleHeight)
            con1.setScale(con2.height()/con1.height());
        else
            con1.setScale(con2.width()/con1.width());

    }

    public static List<Integer> integerList(int from, int to)
    {
        List<Integer> list = new ArrayList<>();
        for(int i=from; i <= to; i++)
            list.add(i);

        return list;
    }

    public static void pathAddCurveToPoint(Path path, PointF toPoint, PointF controlPoint1, PointF controlPoint2)
    {
        path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, toPoint.x, toPoint.y);
    }

    public static void checkAndUpdateFinale(OC_SectionController sectionController)
    {
        String finaleScene = String.format("finale%s",sectionController.events.get(sectionController.events.size()-1));
        List<String> audio = sectionController.getAudioForScene(finaleScene, "DEMO");
        if(audio != null)
        {
            sectionController.audioScenes.put("finale", sectionController.audioScenes.get(finaleScene));
        }

    }


}
