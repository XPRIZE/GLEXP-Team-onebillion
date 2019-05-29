package com.maq.xprize.onecourse.hindi.mainui.oc_listeninggame;

import android.graphics.Color;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.mainui.oc_shapesearch.ShapesManager;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 06/06/2018.
 */

public class OC_Lg5 extends OC_Lg
{
    Map<String,OBPhoneme> shapesPhonemesDict;
    List<Integer> shapesColours;
    int shapesColourIndex;
    ShapesManager shapesManager;

    public String sectionAudioName()
    {
        return "lg5";

    }
    public void prepare()
    {
        super.prepare();
        shapesManager = new ShapesManager(getConfigPath(String.format("%s.xml","shapes")));
        if(parameters.containsKey("trials"))
            setMaxTrials(OBUtils.getIntValue(parameters.get("trials")));
        if(OBUtils.getBooleanValue(parameters.get("outlines")))
        {
            shapesManager.outlineWidth = applyGraphicScale(4);
            shapesManager.outlineColour = Color.BLACK;
            shapesManager.randomFill = false;
            shapesManager.fillColour = Color.TRANSPARENT;
        }
        else
        {
            shapesManager.outlineWidth = 0;
            shapesManager.outlineColour = Color.TRANSPARENT;
            shapesManager.randomFill = true;
        }
        loadShapesDict();
        String[] shapeParams1a = null;
        String[] shapeParams1b = null;
        if(parameters.containsKey("mode1a"))
            shapeParams1a = parameters.get("mode1a").split(",");
        if(parameters.containsKey("mode1b"))
             shapeParams1b = parameters.get("mode1b").split(",");
        if(parameters.containsKey("distractors"))
            addToDistrators(shapesWithPicturesForIds(parameters.get("distractors").split(",")));
        int size = OBUtils.getIntValue(parameters.get("size"));
        if(shapeParams1a != null)
        {
            setupEventForLearning(shapesWithPicturesForIds(shapeParams1a),size);
        }
        if(shapeParams1b != null)
        {
            setupEventForPractice(shapesWithPicturesForIds(shapeParams1b),size);
        }
        finalisePrepare();
    }

    public List<OBPhoneme> shapesWithPicturesForIds(String[] shapes)
    {
        List<OBPhoneme> arr = new ArrayList<>();
        for(String shape : shapes)
        {
            OBPhoneme pho = shapesPhonemesDict.get(shape);
            arr.add(pho);
        }
        return arr;
    }

    public boolean fitControls()
    {
        return false;
    }

    public OBControl loadTargetForPhoneme(OBPhoneme phon, OBControl bg, int type, Map<String,Object> data)
    {
        boolean retry = data.containsKey("retry") ? (boolean)data.get("retry") : false;
        retry = retry &&(phon == data.get("target"));
        OBGroup group = null;
        if(!retry)
        {
            group = shapesManager.loadNextShapeForName((String)phon.properties.get("shape_name"), bg.frame(), this);
            group.setScale (group.scale() * (OB_Maths.randomInt(0, 2) * 0.1f + 0.8f));
            group.setRasterScale (group.scale());
            phon.properties.put("last_control",group);
        }
        else
        {
            OBGroup lastControl = (OBGroup)((OBGroup)phon.properties.get("last_control")).copy();
            lastControl.setPosition(bg.position());
            attachControl(lastControl);
            group = lastControl;
        }
        return group;
    }

    public void highlightTarget(OBControl control)
    {
        lockScreen();
        OBControl bg = (OBControl)control.propertyValue("bg");
        bg.setBorderColor(Color.RED);
        bg.setBorderWidth(applyGraphicScale(4));
        unlockScreen();
    }

    public void lowlightTarget(OBControl control)
    {
        lockScreen();
        OBControl bg = (OBControl)control.propertyValue("bg");
        bg.setBorderWidth(0);
        unlockScreen();
    }

    public boolean keepTargetsSameSize()
    {
        return false;
    }

    public void loadShapesDict()
    {
        shapesPhonemesDict = new ArrayMap<>();
        for(Map<String,Object> shapeData : shapesManager.shapesDict.values())
        {
            String name = (String)shapeData.get("name");
            String category = (String)shapeData.get("category");
            String audio = (String)shapeData.get("audio");
            OBPhoneme phon = new OBPhoneme(category,audio);
            phon.properties.put("shape_name",name);
            shapesPhonemesDict.put(name,phon);
        }
    }

}
