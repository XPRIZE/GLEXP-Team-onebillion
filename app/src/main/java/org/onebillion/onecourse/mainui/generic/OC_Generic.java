package org.onebillion.onecourse.mainui.generic;

import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.ULine;
import org.onebillion.onecourse.utils.UPath;
import org.onebillion.onecourse.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OC_Generic
 * Collection of functions that are common for all Maths and Literacy units.
 * <p>
 * Created by pedroloureiro on 23/06/16.
 */
public class OC_Generic
{

    public static void pointer_lower (OC_SectionController sc)
    {
        PointF currentLocation = OC_Generic.copyPoint(sc.thePointer.position());
        float delta = sc.bounds().height() * 0.03f;
        sc.movePointerToPoint(new PointF(currentLocation.x, currentLocation.y + delta), 0.3f, true);
    }


    public static void pointer_nudge (float x, float y, float angle, float time, Boolean wait, OC_SectionController sc)
    {
        PointF relativePosition = OB_Maths.relativePointInRectForLocation(sc.thePointer.position(), new RectF(sc.bounds()));
        PointF nudge = OB_Maths.locationForRect(OB_Maths.AddPoints(relativePosition, new PointF(x, y)), new RectF(sc.bounds()));
        sc.movePointerToPoint(nudge, angle, time, wait);
    }

    public static void pointer_moveToObjectByName (String controlName, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait, OC_SectionController sc)
    {
        OBControl control = sc.objectDict.get(controlName);
        pointer_moveToObject(control, angle, secs, anchorFlags, wait, sc);
    }

    public static void pointer_moveToObject (OBControl control, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait, OC_SectionController sc)
    {
        PointF position = copyPoint(control.getWorldPosition());
        //
        if (anchorFlags.contains(Anchor.ANCHOR_LEFT)) position.x -= control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_RIGHT)) position.x += control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_TOP)) position.y -= control.height() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_BOTTOM)) position.y += control.height() / 2;
        //
        sc.movePointerToPoint(position, angle, secs, wait);
    }


    public static void pointer_moveToRelativePointOnScreen (float x, float y, float rotation, float secs, Boolean wait, OC_SectionController sc)
    {
        PointF destination = OB_Maths.locationForRect(x, y, new RectF(sc.bounds()));
        sc.movePointerToPoint(destination, rotation, secs, wait);
    }

    public static void pointer_moveToPointWithObject (OBControl control, PointF destination, float rotation, float secs, Boolean wait, OC_SectionController sc)
    {
        OBAnim anim = OBAnim.moveAnim(destination, control);
        OBAnimationGroup.runAnims(Arrays.asList(anim), secs, false, OBAnim.ANIM_EASE_IN_EASE_OUT, sc);
        sc.movePointerToPoint(destination, rotation, secs, true);
    }

    public static void pointer_simulateClick (OC_SectionController sc)
    {
        sc.movePointerForwards(sc.applyGraphicScale(10.0f), 0.1f);
        sc.movePointerForwards(-sc.applyGraphicScale(10.0f), 0.1f);
    }

    public static float getNextZPosition (OC_SectionController sc)
    {
        float maxZPosition = 0.0f;
        for (OBControl control : sc.objectDict.values())
        {
            float zPosition = control.zPosition();
            if (zPosition < 50)
            {
                maxZPosition = Math.max(maxZPosition, zPosition);
            }
        }
        return maxZPosition + 0.001f;
    }

    public static float sendObjectToTop (OBControl control, OC_SectionController sc)
    {
        float newZPosition = getNextZPosition(sc);
        control.setZPosition(newZPosition);
        return newZPosition;
    }

    protected static Map<String, Object> loadObjectColours (OC_SectionController sc)
    {
        String filePath = sc.getConfigPath("objectColours.xml");
        Map<String, Object> objectColoursDictionary = new HashMap<>();
        OBXMLNode xmlNode = null;
        try
        {
            OBXMLManager xmlManager = new OBXMLManager();
            List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(filePath));
//            List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(filePath));
            xmlNode = xl.get(0);
            List<OBXMLNode> xml_objects = xmlNode.childrenOfType("object");
            for (OBXMLNode xml_object : xml_objects)
            {
                String object_id = xml_object.attributeStringValue("id");
                List<OBXMLNode> xml_schemes = xml_object.childrenOfType("scheme");
                Map<String, Object> schemes_dictionary = new HashMap<>();
                //
                for (OBXMLNode xml_scheme : xml_schemes)
                {
                    String scheme_id = xml_scheme.attributeStringValue("id");
                    List<OBXMLNode> xml_layers = xml_scheme.childrenOfType("layer");
                    Map<String, Object> layer_dictionary = new HashMap<>();
                    //
                    for (OBXMLNode xml_layer : xml_layers)
                    {
                        String layer_id = xml_layer.attributeStringValue("id");
                        String colour = xml_layer.attributeStringValue("colour");
                        layer_dictionary.put(layer_id, colour);
                    }
                    //
                    schemes_dictionary.put(scheme_id, layer_dictionary);
                }
                //
                objectColoursDictionary.put(object_id, schemes_dictionary);
            }
        }
        catch (Exception e)
        {
            System.out.println("OC_Generic.action_loadObjectColours.exception caught: " + e.toString());
            e.printStackTrace();
        }
        //
        return objectColoursDictionary;
    }

    public static void colourObject (OBControl control, int colour)
    {
        if (OBGroup.class.isInstance(control))
        {
            OBGroup group = (OBGroup) control;
            //
            ArrayList<OBControl> colourableLayers = new ArrayList();
            for (String key : group.objectDict.keySet())
            {
                if (key.startsWith("col"))
                {
                    colourableLayers.add(group.objectDict.get(key));
                }
            }
            if (colourableLayers.isEmpty())
            {
                for (OBControl member : group.members)
                {
                    colourObject(member, colour);
                }
            }
            else
            {
                for (OBControl layer : colourableLayers)
                {
                    colourObject(layer, colour);
                }
            }
        }
        else if (OBPath.class.isInstance(control))
        {
            OBPath path = (OBPath) control;
            path.setFillColor(colour);
        }
        else
        {
            System.out.println("OC_Generic.action_colourObject.unknown class for colouring");
        }
    }

    public static void colourObjectsWithScheme (OC_SectionController sc)
    {
        Map<String, Object> objectColoursDictionary = loadObjectColours(sc);
        //
        for (OBControl control : sc.filterControls(".*"))
        {
            if (!(OBGroup.class.isInstance(control))) continue;
            //
            OBGroup group = (OBGroup) control;
            //
            if (group == null) continue;
            //
            String scheme = (String) group.attributes().get("scheme");
            if (scheme != null)
            {
                String parameters[] = scheme.split(" ");
                String objectID = parameters[0];
                String schemeID = parameters[1];
                Map<String, Object> schemes = (Map<String, Object>) objectColoursDictionary.get(objectID);
                //
                if (schemes != null)
                {
                    Map<String, Object> layers = (Map<String, Object>) schemes.get(schemeID);
                    //
                    if (layers != null)
                    {
                        boolean layerWasColoured = false;
                        for (String layerID : layers.keySet())
                        {
                            int colour = OBUtils.colorFromRGBString((String) layers.get(layerID));
                            OBControl layer = group.objectDict.get(layerID);
                            if (layer != null)
                            {
                                colourObject(layer, colour);
                                layerWasColoured = true;
                            }
                        }
                        //
                        if (!layerWasColoured)
                        {
                            String colourString = (String) layers.get("normal");
                            if (colourString != null)
                            {
                                int colour = OBUtils.colorFromRGBString(colourString);
                                colourObject(group, colour);
                            }
                        }
                    }
                }
            }
        }
    }

    public static List<OBControl> controlsSortedFrontToBack (OBGroup group, String pattern)
    {
        List<OBControl> result = new ArrayList<OBControl>();
        Pattern p = Pattern.compile(pattern);
        for (OBControl control : group.members)
        {
            String controlID = (String) control.attributes().get("id");
            if (controlID == null)
            {
                controlID = (String) control.settings.get("name");
            }
            if (controlID == null) continue;
            //
            Matcher matcher = p.matcher(controlID);
            matcher.find();
            if (matcher.matches())
            {
                result.add(control);
            }
        }
        Collections.reverse(result);
        //
        return result;
    }

    public static PointF copyPoint (PointF original)
    {
        return new PointF(original.x, original.y);
    }

    public static double currentTime ()
    {
        return (SystemClock.uptimeMillis() / (double) 1000);
    }

    public static PointF firstPoint (OBPath path, OC_SectionController sc)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return null;
        UPath deconPath = sc.deconstructedPath(sc.currentEvent(), name);
        USubPath subPath = deconPath.subPaths.get(0);
        ULine line = subPath.elements.get(0);
        return line.pt0;
    }

    public static PointF lastPoint (OBPath path, OC_SectionController sc)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return null;
        UPath deconPath = sc.deconstructedPath(sc.currentEvent(), name);
        USubPath subPath = deconPath.subPaths.get(deconPath.subPaths.size() - 1);
        ULine line = subPath.elements.get(subPath.elements.size() - 1);
        return line.pt1;
    }

    public static void setFirstPoint (OBPath path, PointF pt, OC_SectionController sc)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return;
        UPath deconPath = sc.deconstructedPath(sc.currentEvent(), name);
        USubPath subPath = deconPath.subPaths.get(0);
        ULine line = subPath.elements.get(0);
        line.pt0 = pt;
        path.setPath(deconPath.bezierPath());
    }

    public static void setLastPoint (OBPath path, PointF pt, OC_SectionController sc)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return;
        UPath deconPath = sc.deconstructedPath(sc.currentEvent(), name);
        USubPath subPath = deconPath.subPaths.get(deconPath.subPaths.size() - 1);
        ULine line = subPath.elements.get(subPath.elements.size() - 1);
        line.pt1 = pt;
        path.setPath(deconPath.bezierPath());
    }

    public static int randomInt (int min, int max)
    {
        double dval = Math.random();
        return (int) Math.round(min + (max - min) * dval);
    }

    public static OBLabel action_createLabelForControl (OBControl control, float finalResizeFactor, Boolean insertIntoGroup, OC_SectionController sc)
    {
        return action_createLabelForControl(control, finalResizeFactor, insertIntoGroup, sc, OBUtils.standardTypeFace());
    }

    public static OBLabel action_createLabelForControl (OBControl control, float finalResizeFactor, Boolean insertIntoGroup, OC_SectionController sc, Typeface tf)
    {
        try
        {
            RectF controlBounds = control.getWorldFrame();
            Boolean autoResize = sc.eventAttributes.get("textSize") == null;
            float textSize = 1;
            //
            if (!autoResize)
            {
                textSize = sc.applyGraphicScale(Float.parseFloat(sc.eventAttributes.get("textSize")));
            }
            String content = (String) control.attributes().get("text");
            if (content == null) content = (String) control.attributes().get("number");
            if (content == null) content = (String) control.propertyValue("text");
            if (content == null) content = "0000";
            //
            OBLabel label = new OBLabel(content, tf, textSize);
            label.setFrame(controlBounds);
            //
            if (autoResize)
            {
                OBTextLayer textLayer = (OBTextLayer) label.layer;
                textLayer.sizeToBoundingBox();
                while (label.height() > 0 && label.height() < controlBounds.height() && textLayer.textWidth(content) < controlBounds.width())
                {
                    textLayer.setTextSize(textLayer.textSize() + 1);
                    label.sizeToBoundingBox();
                }
                //
                float currentTextSize = textLayer.textSize();
                textLayer.setTextSize(currentTextSize * finalResizeFactor);
                label.sizeToBoundingBox();
                //
                if (textLayer.textWidth(content) > controlBounds.width())
                {
                    textLayer.setTextSize(currentTextSize);
                    label.sizeToBoundingBox();
                }
            }
            //
            label.setPosition(control.getWorldPosition());
            label.setZPosition(OC_Generic.getNextZPosition(sc));
            label.texturise(false, sc);
            //
            if (insertIntoGroup)
            {
                if (OBGroup.class.isInstance(control))
                {
                    OBGroup group = (OBGroup) control;
                    sc.attachControl(label);
                    group.insertMember(label, 0, "label");
                }
                else
                {
                    OBGroup group = new OBGroup(Arrays.asList(control, label));
                    sc.attachControl(group);
                    group.objectDict.put("frame", control);
                    group.objectDict.put("label", label);
                    String controlID = (String) control.attributes().get("id");
                    sc.objectDict.put(controlID, group);
                    String components[] = controlID.split("_");
                    String labelID = "label_" + components[1];
                    sc.objectDict.put(labelID, label);
                }
            }
            else
            {
                sc.attachControl(label);
            }
            return label;
        }
        catch (Exception e)
        {
            System.out.println("OC_Generic_Event:action_createLabelForControl:exception" + e.toString());
            e.printStackTrace();
        }
        return null;
    }


    public static enum Anchor
    {
        ANCHOR_MIDDLE,
        ANCHOR_LEFT,
        ANCHOR_RIGHT,
        ANCHOR_TOP,
        ANCHOR_BOTTOM;

        public final int anchor;

        Anchor ()
        {
            this.anchor = 1 << this.ordinal();
        }
    }


    public static enum AnimationType
    {
        IDLE,
        ACTIVE,
        MOVEMENT;
    }


    public static void animate_frogs (List<OBGroup> controls, AnimationType animationType, String specificEvent, OC_SectionController sc)
    {
        try
        {
            if (specificEvent.equals(sc.currentEvent()))
            {
                List<OBGroup> randomControls = OBUtils.randomlySortedArray(controls);
                for (OBGroup control : randomControls)
                {
                    if (animationType == AnimationType.IDLE)
                    {
                        OBGroup rightEye = (OBGroup) control.objectDict.get("right");
                        OBAnim blinkAnim1 = OBAnim.sequenceAnim(rightEye, Arrays.asList("eyelid_right_open", "eyelid_right_closed", "eyelid_right_open"), 0.1f, false);
                        OBGroup leftEye = (OBGroup) control.objectDict.get("left");
                        OBAnim blinkAnim2 = OBAnim.sequenceAnim(rightEye, Arrays.asList("eyelid_left_open", "eyelid_left_closed", "eyelid_left_open"), 0.1f, false);
                        OBAnimationGroup.runAnims(Arrays.asList(blinkAnim1, blinkAnim2), 0.3, true, OBAnim.ANIM_LINEAR, sc);
                        sc.waitForSecs(randomInt(50, 250) / (float) 1000);
                    }
                    else if (animationType == AnimationType.ACTIVE)
                    {
                        OBAnim rotateAnim1 = OBAnim.rotationAnim((float) Math.toRadians(-180f), control);
                        OBAnim rotateAnim2 = OBAnim.rotationAnim((float) Math.toRadians(-360f), control);
                        PointF startPoint = copyPoint(control.position());
                        PointF endPoint = new PointF(startPoint.x, startPoint.y - (1.25f * control.height()));
                        OBAnim moveAnim1 = OBAnim.moveAnim(endPoint, control);
                        OBAnim moveAnim2 = OBAnim.moveAnim(startPoint, control);
                        OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(moveAnim1, rotateAnim1), Arrays.asList(moveAnim2, rotateAnim2)), Arrays.asList(0.4f, 0.4f), false, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, sc);
                        sc.waitForSecs(0.05);
                    }
                    else if (animationType == AnimationType.MOVEMENT)
                    {
                        //
                    }
                }
                //
                if (animationType == AnimationType.IDLE)
                {
                    sc.waitForSecs(randomInt(1, 3));
                    animate_frogs(controls, animationType, specificEvent, sc);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void animate_birds (List<OBGroup> controls, AnimationType animationType, String specificEvent, OC_SectionController sc)
    {
        try
        {
            if (specificEvent.equals(sc.currentEvent()))
            {
                List<OBGroup> randomControls = OBUtils.randomlySortedArray(controls);
                for (OBGroup control : randomControls)
                {
                    if (animationType == AnimationType.IDLE)
                    {
                        OBAnim flapAnim = OBAnim.sequenceAnim(control, Arrays.asList("frame2", "frame1", "frame2", "frame1"), 0.1f, false);
                        OBAnimationGroup.runAnims(Arrays.asList(flapAnim), 0.6, true, OBAnim.ANIM_LINEAR, sc);
                        sc.waitForSecs(randomInt(50, 250) / (float) 1000);
                    }
                    else if (animationType == AnimationType.ACTIVE)
                    {
                        OBAnim flapAnim = OBAnim.sequenceAnim(control, Arrays.asList("frame2", "frame1", "frame2", "frame1"), 0.1f, false);
                        PointF startPoint = copyPoint(control.position());
                        PointF endPoint = new PointF(startPoint.x, startPoint.y - (1.0f * control.height()));
                        OBAnim moveAnim1 = OBAnim.moveAnim(endPoint, control);
                        OBAnim moveAnim2 = OBAnim.moveAnim(startPoint, control);
                        OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(moveAnim1, flapAnim), Arrays.asList(moveAnim2, flapAnim)), Arrays.asList(0.4f, 0.4f), false, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, sc);
                        sc.waitForSecs(0.05);
                    }
                    else if (animationType == AnimationType.MOVEMENT)
                    {
                        //
                    }
                }
                //
                if (animationType == AnimationType.IDLE)
                {
                    sc.waitForSecs(randomInt(1, 3));
                    animate_birds(controls, animationType, specificEvent, sc);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void animate_ladybirds (List<OBGroup> controls, AnimationType animationType, String specificEvent, OC_SectionController sc)
    {
        try
        {
            if (specificEvent.equals(sc.currentEvent()))
            {
                List<OBGroup> randomControls = OBUtils.randomlySortedArray(controls);
                for (OBGroup control : randomControls)
                {
                    if (animationType == AnimationType.IDLE)
                    {
                        OBAnim flapAnim = OBAnim.sequenceAnim(control, Arrays.asList("frame2", "frame3", "frame2", "frame1"), 0.1f, false);
                        OBAnimationGroup.runAnims(Arrays.asList(flapAnim), 0.6, true, OBAnim.ANIM_LINEAR, sc);
                        sc.waitForSecs(randomInt(50, 250) / (float) 1000);
                    }
                    else if (animationType == AnimationType.ACTIVE)
                    {
                        OBAnim rotateAnim1 = OBAnim.rotationAnim((float) Math.toRadians(-180f), control);
                        OBAnim rotateAnim2 = OBAnim.rotationAnim((float) Math.toRadians(-360f), control);
                        OBAnim flapAnim = OBAnim.sequenceAnim(control, Arrays.asList("frame2", "frame3", "frame2", "frame1"), 0.1f, false);
                        PointF startPoint = copyPoint(control.position());
                        PointF endPoint = new PointF(startPoint.x, startPoint.y - (0.75f * control.height()));
                        OBAnim moveAnim1 = OBAnim.moveAnim(endPoint, control);
                        OBAnim moveAnim2 = OBAnim.moveAnim(startPoint, control);
                        OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(moveAnim1, rotateAnim1, flapAnim), Arrays.asList(moveAnim2, rotateAnim2, flapAnim)), Arrays.asList(0.4f, 0.4f), false, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, sc);
                        sc.waitForSecs(0.05);
                    }
                    else if (animationType == AnimationType.MOVEMENT)
                    {
                        //
                    }
                }
                //
                if (animationType == AnimationType.IDLE)
                {
                    sc.waitForSecs(randomInt(1, 3));
                    animate_ladybirds(controls, animationType, specificEvent, sc);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static String toTitleCase (String str)
    {
        if (str == null || str.isEmpty())
            return "";

        if (str.length() == 1)
            return str.toUpperCase();

        //split the string by space
        String[] parts = str.split(" ");

        StringBuilder sb = new StringBuilder(str.length());

        for (String part : parts)
        {

            if (part.length() > 1)
                sb.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1).toLowerCase());
            else
                sb.append(part.toUpperCase());

            sb.append(" ");
        }

        return sb.toString().trim();
    }

}
