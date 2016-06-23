package org.onebillion.xprz.mainui.generic;

import android.graphics.PointF;
import android.os.SystemClock;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class XPRZ_Generic
{
    public static enum Anchor
    {
        ANCHOR_MIDDLE,
        ANCHOR_LEFT,
        ANCHOR_RIGHT,
        ANCHOR_TOP,
        ANCHOR_BOTTOM;

        public final int anchor;

        Anchor()
        {
            this.anchor = 1 << this.ordinal();
        }
    }


    public static void pointer_moveToObjectByName(String controlName, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait, XPRZ_SectionController sc)
    {
        OBControl control = sc.objectDict.get(controlName);
        pointer_moveToObject(control, angle, secs, anchorFlags, wait, sc);
    }


    public static void pointer_moveToObject(OBControl control, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait, XPRZ_SectionController sc)
    {
        PointF position = copyPoint(control.position());
        //
        if (anchorFlags.contains(Anchor.ANCHOR_LEFT)) position.x -= control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_RIGHT)) position.x += control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_TOP)) position.y -= control.height() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_BOTTOM)) position.y += control.height() / 2;
        //
        sc.movePointerToPoint(position, angle, secs, wait);
    }


    public static void pointer_moveToPointWithObject(OBControl control, PointF destination, float rotation, float secs, Boolean wait, XPRZ_SectionController sc)
    {
        OBAnim anim = OBAnim.moveAnim(destination, control);
        OBAnimationGroup.runAnims(Arrays.asList(anim), secs, false, OBAnim.ANIM_EASE_IN_EASE_OUT, sc);
        sc.movePointerToPoint(destination, rotation, secs, true);
    }



    public static float getNextZPosition(XPRZ_SectionController sc)
    {
        float maxZPosition = 0.0f;
        for (OBControl control : sc.objectDict.values())
        {
            maxZPosition = Math.max(maxZPosition, control.zPosition());
        }
        return maxZPosition + 0.001f;
    }

    public static void sendObjectToTop(OBControl control, XPRZ_SectionController sc)
    {
        control.setZPosition(getNextZPosition(sc));
    }




    protected static Map<String,Object> loadObjectColours(XPRZ_SectionController sc)
    {
        String filePath = sc.getConfigPath("objectColours.xml");
        Map<String,Object> objectColoursDictionary = new HashMap<>();
        OBXMLNode xmlNode = null;
        try
        {
            OBXMLManager xmlManager = new OBXMLManager();
            List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(filePath));
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
                    for (OBXMLNode xml_layer: xml_layers)
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
            System.out.println("XPRZ_Generic.action_loadObjectColours.exception caught: " + e.toString());
            e.printStackTrace();
        }
        //
        return objectColoursDictionary;
    }



    public static void colourObject(OBControl control, int colour)
    {
        if (OBGroup.class.isInstance(control))
        {
            OBGroup group = (OBGroup) control;
            //
            if (group.objectDict.get("col.*") == null)
            {
                for (OBControl member : group.members)
                {
                    colourObject(member, colour);
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
            System.out.println("XPRZ_Generic.action_colourObject.unknown class for colouring");
        }
    }


    public static void colourObjectsWithScheme(XPRZ_SectionController sc)
    {
        Map<String,Object> objectColoursDictionary = loadObjectColours(sc);
        //
        for(OBControl control : sc.filterControls(".*"))
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
                Map<String,Object> schemes = (Map<String,Object>) objectColoursDictionary.get(objectID);
                Map<String,Object> layers = (Map<String,Object>) schemes.get(schemeID);
                //
                for (String layerID : layers.keySet())
                {
                    int colour = OBUtils.colorFromRGBString((String) layers.get(layerID));
                    OBControl layer = (OBControl) group.objectDict.get(layerID);
                    colourObject(layer, colour);
                }
            }
        }
    }




    public static PointF copyPoint(PointF original)
    {
        return new PointF(original.x, original.y);
    }


    public static double currentTime()
    {
        return (SystemClock.uptimeMillis() / (double) 1000);
    }

}
