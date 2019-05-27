package com.maq.xprize.onecourse.mainui.oc_shapesearch;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.OBSectionController;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OBXMLManager;
import com.maq.xprize.onecourse.utils.OBXMLNode;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 06/06/2018.
 */

public class ShapesManager
{
    public boolean randomFill;
    public int fillColour, outlineColour;
    public  float outlineWidth;
    public Map<String,Map<String,Object>> shapesDict;

    private int currentColourIndex;
    private List<Integer> colours;


    public ShapesManager(String xmlPath)
    {
        try {
            currentColourIndex = 0;
            shapesDict = new ArrayMap();
            OBXMLManager xmlman = new OBXMLManager();
            List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
            OBXMLNode rootNode = xl.get(0);
            OBXMLNode shapeNodes = rootNode.childrenOfType("shapes").get(0);
            for (OBXMLNode shapeNode : shapeNodes.childrenOfType("shape")) {
                Map<String, Object> shapeData = new ArrayMap<>();
                String shapeId = shapeNode.attributeStringValue("id");
                String shapeCategory = shapeNode.attributeStringValue("category");
                String shapeAudio = shapeNode.attributeStringValue("audio");
                shapeData.put("name", shapeId);
                shapeData.put("category", shapeCategory);
                if (shapeAudio != null)
                    shapeData.put("audio", shapeAudio);
                List<String> images = new ArrayList<>();
                for (OBXMLNode imgNode : shapeNode.childrenOfType("img")) {
                    images.add(imgNode.attributeStringValue("name"));
                }
                shapeData.put("images", OBUtils.randomlySortedArray(images));
                shapeData.put("image_index", 0);
                shapesDict.put(shapeId, shapeData);
            }
            OBXMLNode colourNodes = rootNode.childrenOfType("colours").get(0);
            List<Integer> coloursArray = new ArrayList<>();
            for (OBXMLNode colourNode : colourNodes.childrenOfType("colour")) {
                String rgbValue = colourNode.attributeStringValue("value");
                coloursArray.add(OBUtils.colorFromRGBString(rgbValue));
            }
            colours = OBUtils.randomlySortedArray(coloursArray);
            outlineColour = Color.TRANSPARENT;
            fillColour = Color.TRANSPARENT;
            randomFill = true;
            outlineWidth = 0;
        }
        catch (Exception e)
        {
            MainActivity.log("Error loading ShapesManager: "+e.getMessage());
        }
    }

    public OBGroup shapeForImageName(String imageName, float scale, float degrees, int fillColour,
                                     int borderColour, float borderWidth, RectF rect, OBSectionController controller)
    {
        OBGroup group =(OBGroup) controller.loadVectorWithName(imageName,new PointF(0.5f, 0.5f),rect);
        OBPath path =(OBPath) group.objectDict.get("colour");
        float fitWidth = rect.width()-2*borderWidth;
        float fitHeight = rect.height()-2*borderWidth;
        group.setScale(fitHeight/group.height());
        if(fitWidth < group.width())
            group.setScale(group.scale() * fitWidth/group.width());
        path.setFillColor(fillColour);
        path.setLineWidth(borderWidth/group.scale());
        path.setStrokeColor(borderColour);
        group.setRotation((float)Math.toRadians(degrees));
        return group;

    }

    public OBGroup loadNextShapeForName(String name, RectF rect, OBSectionController controller)
    {
        Map<String,Object> shapeData = this.shapesDict.get(name);
        List<String> images =(List<String>) shapeData.get("images");
        int imageIndex = (int)shapeData.get("image_index");
        String imageName = images.get(imageIndex);
        int fill = this.randomFill ? colours.get(currentColourIndex) : this.fillColour;
        int borderColour = this.outlineColour;
        float borderWidth = this.outlineWidth;
        imageIndex++;
        if(imageIndex >= images.size())
        {
            imageIndex = 0;
            shapeData.put("images",OBUtils.randomlySortedArray(images));

        }
        shapeData.put("image_index",imageIndex);
        if(randomFill)
        {
            currentColourIndex++;
            if(currentColourIndex >= colours.size())
            {
                currentColourIndex = 0;
                colours = OBUtils.randomlySortedArray(colours);
            }
        }
        return shapeForImageName(imageName,1.0f,OB_Maths.randomInt(0,7)*45.0f,fill,
                borderColour,borderWidth,rect,controller);

    }

}
