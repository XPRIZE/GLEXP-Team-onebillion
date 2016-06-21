package org.onebillion.xprz.mainui.generic;

import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBTextLayer;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 20/06/16.
 */
public class XPRZ_Generic_Event extends XPRZ_SectionController
{
    int currentDemoAudioIndex;
    public Map<String,Object> objectColoursDictionary;

    public enum Anchor
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


    public XPRZ_Generic_Event()
    {
        super();
    }

    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("master1");
        action_loadObjectColours();
        String[] eva = ((String)eventAttributes.get(action_getScenesProperty())).split(",");
        events = Arrays.asList(eva);

        doVisual(currentEvent());
        unlockScreen();
    }


    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
            }
        });
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }


    public void doMainXX() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        //
        doAudio(currentEvent());
        //
        setStatus(STATUS_AWAITING_CLICK);
    }


    @Override
    public void setSceneXX(String scene)
    {
        ArrayList<OBControl> oldControls = new ArrayList<>(objectDict.values());
        //
        loadEvent(scene);
        //
        Boolean redraw = eventAttributes.get("redraw") != null && eventAttributes.get("redraw").equals("true");
        if (redraw)
        {
            for(OBControl control : oldControls)
            {
                if (control == null) continue;
                detachControl(control);
                objectDict.remove(control);
            }
            //
            for (OBControl control : filterControls(".*"))
            {
                String fit = (String) control.attributes().get("fit");
                if (fit != null && fit.equals("fitwidth"))
                {
                    float scale = bounds().width() / control.width();
                    PointF position = copyPoint(control.position());
                    float originalHeight = control.height();
                    control.setScale(scale * control.scale());
                    float heightDiff = control.height() - originalHeight;
                    position.y += heightDiff / 2;
                    control.setPosition(position);
                }
            }
        }
        //
        targets = filterControls(action_getObjectPrefix() + ".*");
        //
        for(OBControl control : targets)
        {
            PointF originalPosition = new PointF(control.position().x, control.position().y);
            control.setProperty("originalPosition", originalPosition);
        }
        //
        currentDemoAudioIndex = 0;
        //
        action_colourObjectsWithScheme();
        //
        action_prepareScene(scene, redraw);
    }


    public void action_loadObjectColours()
    {
        String filePath = getConfigPath("objectColours.xml");
        objectColoursDictionary = new HashMap<>();
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
    }



    public void action_colourObject(OBControl control, int colour)
    {
        if (OBGroup.class.isInstance(control))
        {
            OBGroup group = (OBGroup) control;
            //
            if (group.objectDict.get("col.*") == null)
            {
                for (OBControl member : group.members)
                {
                    action_colourObject(member, colour);
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
            System.out.println("XPRZ_Generic_Event.action_colourObject.unknown class for colouring");
        }
    }


    public void action_colourObjectsWithScheme()
    {
        for(OBControl control : filterControls(".*"))
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
                    action_colourObject(layer, colour);
                }
            }
        }
    }


    public void pointer_moveToObjectByName(String controlName, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait)
    {
        OBControl control = objectDict.get(controlName);
        pointer_moveToObject(control, angle, secs, anchorFlags, wait);
    }


    public void pointer_moveToObject(OBControl control, float angle, float secs, EnumSet<Anchor> anchorFlags, Boolean wait)
    {
        PointF position = copyPoint(control.position());
        //
        if (anchorFlags.contains(Anchor.ANCHOR_LEFT)) position.x -= control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_RIGHT)) position.x += control.width() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_TOP)) position.y -= control.height() / 2;
        if (anchorFlags.contains(Anchor.ANCHOR_BOTTOM)) position.y += control.height() / 2;
        //
        movePointerToPoint(position, angle, secs, wait);
    }


    public void pointer_moveToPointWithObject(OBControl control, PointF destination, float rotation, float secs, Boolean wait)
    {
        OBAnim anim = OBAnim.moveAnim(destination, control);
        OBAnimationGroup.runAnims(Arrays.asList(anim), secs, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        movePointerToPoint(destination, rotation, secs, true);
    }


    public void action_prepareScene(String scene, Boolean redraw)
    {

    }


    public void action_playNextDemoSentence(Boolean waitAudio) throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", currentDemoAudioIndex, waitAudio);
        currentDemoAudioIndex++;
    }


    public void action_moveObjectToOriginalPosition(OBControl control, Boolean wait)
    {
        OBAnim anim = OBAnim.moveAnim((PointF)control.propertyValue("originalPosition"), control);
        OBAnimationGroup og = new OBAnimationGroup();
        og.applyAnimations(Arrays.asList(anim), 0.3, wait, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public void action_moveObjectIntoContainer(OBControl control, OBControl container)
    {
        List<OBControl> contained = (List<OBControl>) container.propertyValue("contained");
        if (contained == null) contained = new ArrayList<OBControl>();
        //
        if (contained.contains(control))
        {
            action_moveObjectToOriginalPosition(control, false);
        }
        else
        {
            contained.add(control);
            container.setProperty("contained", contained);
            //
            List<OBAnim> animations = new ArrayList<OBAnim>();
            // horizontal displacement
            float deltaH = container.width() / (contained.size() + 1);
            for (int i = 0; i < contained.size(); i++)
            {
                PointF newPosition = copyPoint(container.position());
                newPosition.x = container.left() + deltaH * (i + 1);
                OBControl placedObject = contained.get(i);
                animations.add(OBAnim.moveAnim(newPosition, placedObject));
            }
            OBAnimationGroup.runAnims(animations, 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        }
    }


    public void action_animatePlatform(OBControl platform, boolean wait) throws Exception
    {
        String platformName = (String) platform.attributes().get("id");
        String platformNumber = platformName.split("_")[1];
        List<OBControl> controls = filterControls(".*_" + platformNumber + "_.*");
        //
        List<OBAnim> list_animMove1 = new ArrayList<OBAnim>();
        List<OBAnim> list_animMove2 = new ArrayList<OBAnim>();
        //
        for(OBControl item : controls)
        {
            PointF startPosition = new PointF();
            startPosition.set(item.position());
            //
            PointF endPosition = new PointF();
            endPosition.set(startPosition);
            endPosition.y -= 1.25 * item.height();
            //
            list_animMove1.add(OBAnim.moveAnim(endPosition, item));
            list_animMove1.add(OBAnim.rotationAnim((float) Math.toRadians(-180.0f), item));
            list_animMove2.add(OBAnim.moveAnim(startPosition, item));
            list_animMove1.add(OBAnim.rotationAnim((float) Math.toRadians(-360.0f), item));
//            OBAnim anim_move1 = OBAnim.moveAnim(endPosition, item);
//            OBAnim anim_move2 = OBAnim.moveAnim(startPosition, item);
//            OBAnim anim_rotate1 = OBAnim.rotationAnim((float) Math.toRadians(-180.0f), item);
//            OBAnim anim_rotate2 = OBAnim.rotationAnim((float) Math.toRadians(-360.0f), item);
//            OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(anim_move1,anim_rotate1),Arrays.asList(anim_move2,anim_rotate2)), Arrays.asList(0.4f,0.4f), false, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, this);
//            waitForSecs(0.05);
        }
        OBAnimationGroup og = new OBAnimationGroup();
        og.chainAnimations(Arrays.asList(list_animMove1, list_animMove2), Arrays.asList(0.4f,0.4f), wait, Arrays.asList(OBAnim.ANIM_EASE_IN, OBAnim.ANIM_EASE_OUT), 1, this);
    }



    public OBControl action_getCorrectAnswer()
    {
        String correctString = action_getObjectPrefix() + "_" + eventAttributes.get("correctAnswer");
        return objectDict.get(correctString);
    }


    public String action_getObjectPrefix()
    {
        return "number";
    }


    public String action_getContainerPrefix()
    {
        return "box";
    }


    public String action_getScenesProperty()
    {
        return "scenes";
    }


    public OBLabel action_createLabelForControl(OBControl control, float finalResizeFactor)
    {
        try {
            Boolean autoResize = eventAttributes.get("textSize") == null;
            float textSize = 1;
            //
            if (!autoResize)
            {
                textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textSize")));
            }
            String content = (String) control.attributes().get("text");
            if (content == null) content = (String) control.attributes().get("number");
            if (content == null) content = "";
            //
            Typeface tf = OBUtils.standardTypeFace();
            OBLabel label = new OBLabel(content, tf, textSize);
            //
            if (autoResize)
            {
                OBTextLayer textLayer = (OBTextLayer)label.layer;
                textLayer.sizeToBoundingBox();
                while(label.height() > 0 && label.height() < control.bounds.height())
                {
                    textLayer.textSize++;
                    textLayer.sizeToBoundingBox();
                }
                //
                textLayer.textSize = textLayer.textSize * finalResizeFactor;
                textLayer.sizeToBoundingBox();
            }
            //
            label.setPosition(control.position());
            label.setZPosition(getNextZPosition());
            label.texturise(false, this);
            //
            if (OBGroup.class.isInstance(control)) {
                OBGroup group = (OBGroup) control;
                attachControl(label);
                group.insertMember(label, 0, "label");
            }
            else {
                OBGroup group = new OBGroup(Arrays.asList(control, label));
                attachControl(group);
                group.objectDict.put("frame", control);
                group.objectDict.put("label", label);
                String controlID = (String)control.attributes().get("id");
                objectDict.put(controlID, group);
                String components[] = controlID.split("_");
                String labelID = "label_" + components[1];
                objectDict.put(labelID, label);
            }
            return label;
        }
        catch (Exception e)
        {
            System.out.println("XPRZ_Generic_Event:action_createLabelForControl:exception" + e.toString());
        }
        return null;
    }


    // AUX FUNCTIONS


    public float getNextZPosition()
    {
        float maxZPosition = 0.0f;
        for (OBControl control : objectDict.values())
        {
            maxZPosition = Math.max(maxZPosition, control.zPosition());
        }
        return maxZPosition + 0.001f;
    }

    public void sendObjectToTop(OBControl control)
    {
        control.setZPosition(getNextZPosition());
    }

    public PointF copyPoint(PointF original)
    {
        return new PointF(original.x, original.y);
    }




    // Finger and Touch functions

    public OBControl findTarget(PointF pt)
    {
        return finger(-1, 2, targets, pt, true);
    }


}
