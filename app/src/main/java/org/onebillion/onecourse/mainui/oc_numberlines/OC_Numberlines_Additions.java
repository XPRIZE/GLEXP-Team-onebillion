package org.onebillion.onecourse.mainui.oc_numberlines;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 27/07/16.
 */
public class OC_Numberlines_Additions
{

    public static void drawNumberLine(OBControl rect, int size, int start, int mainColour, int divColour, int numColour, OC_SectionController controller)
    {
        float lineSize = (float)Math.ceil(controller.applyGraphicScale(7));
        OBPath mainLine = new OBPath(OB_Maths.worldLocationForControl(0,0.5f,rect), OB_Maths.worldLocationForControl(1,0.5f,rect));
        mainLine.setLineWidth(lineSize);
        mainLine.setStrokeColor(mainColour);
        mainLine.setZPosition(3);
        mainLine.setProperty("num_size",size);
        mainLine.setProperty("num_start",start);
        mainLine.sizeToBoundingBoxIncludingStroke();
        controller.attachControl(mainLine);
        controller.objectDict.put("mainline",mainLine);

        float fontSize = 60*rect.height()/185;
        mainLine.setProperty("fontsize",fontSize);
        for(int i=0; i<size; i++)
        {
            OBPath divLine = new OBPath(OB_Maths.worldLocationForControl(i*1.0f/(size-1),0.44f,rect),OB_Maths.worldLocationForControl(i*1.0f/(size-1),0.68f,rect));
            divLine.setLineWidth(lineSize);
            divLine.setStrokeColor(divColour);
            divLine.setZPosition(4);
            divLine.setProperty("num_value",start+i);
            divLine.sizeToBoundingBoxIncludingStroke();
            divLine.setAnchorPoint(new PointF(0.5f,1));
            controller.attachControl(divLine);
            controller.objectDict.put(String.format("divline_%d", start+i),divLine);

            OBLabel numLabel = new OBLabel("88", OBUtils.standardTypeFace(), fontSize);
            numLabel.setString(String.format("%d",start+i));
            numLabel.setPosition(OB_Maths.worldLocationForControl(i*1.0f/(size-1),0.86f,rect));
            numLabel.setColour(numColour);
            numLabel.setProperty("num_value",start+i);
            numLabel.setProperty("num_colour",numColour);
            numLabel.setZPosition(5);
                controller.attachControl(numLabel);
            controller.objectDict.put(String.format("num_%d", start+i),numLabel);
        }

        if(controller.objectDict.get("arrowhead") != null)
            controller.objectDict.get("arrowhead").setZPosition(5.5f);
    }


    public static void animateCurve(int num,boolean clockwise ,float duration, boolean audio, boolean label, OC_SectionController controller) throws Exception
    {
        animateCurve(num,clockwise,duration,audio, label, clockwise? OBUtils.colorFromRGBString("255,252,0") : OBUtils.colorFromRGBString("255,83,0"), controller);
    }

    public static void animateCurve(int num, boolean clockwise , float duration, boolean audio, boolean label, int colour, final OC_SectionController controller) throws Exception
    {
        drawCurveLine(num,clockwise,label, colour, controller);

        final OBPath curveLine = (OBPath)controller.objectDict.get(String.format("curveline_%d", num));
        final OBControl arrowHead = controller.objectDict.get("arrowhead");
        arrowHead.setPosition(curveLine.convertPointToControl(curveLine.firstPoint(), null));
        arrowHead.setRotation(0);
        arrowHead.show();

        curveLine.setStrokeEnd(0);
        curveLine.show();
        shortenDiv(num, controller);

        List<OBAnim> anims = new ArrayList<>();
        anims.add(OBAnim.propertyAnim("strokeEnd",1,curveLine));
        anims.add(new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                arrowHead.setPosition(curveLine.convertPointToControl(curveLine.sAlongPath(frac,null), null));
            }
        });
        anims.add(OBAnim.rotationAnim((clockwise ? (float)Math.toRadians(180): (float)Math.toRadians(-180)),controller.objectDict.get("arrowhead")));


        if(label)
        {
            final OBGroup linelabel = (OBGroup)controller.objectDict.get(String.format("linelabel_%d",num));
            linelabel.setOpacity(0);
            linelabel.show();
            anims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    linelabel.setOpacity(OB_Maths.clamp01((frac-0.3f)/0.4f));
                }
            });
        }


        if(audio)
            controller.playSfxAudio(clockwise ? "arc_grow": "arc_reduce",false);

        OBAnimationGroup.runAnims(anims,duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,controller);

        shortenDiv(clockwise ? num+1 : num -1, controller);

        if(audio)
            controller.playSFX(null);
    }

    public static void shortenDiv(int num, OC_SectionController controller)
    {
        OBPath divline = (OBPath)controller.objectDict.get(String.format("divline_%d", num));
        divline.setStrokeStart(0.15f);
    }

    public static void resetNumberLine(boolean clearNum,boolean hideNum, OC_SectionController controller)
    {
        controller.lockScreen();
        for(OBControl con : controller.filterControls("divline_.*"))
        {
            ((OBPath)con).setStrokeStart(0);
        }

        controller.deleteControls("curveline_.*");
        controller.deleteControls("linelabel.*");

        if(controller.objectDict.get("arrowhead") != null)
            controller.objectDict.get("arrowhead").hide();

        if(clearNum)
        {
            for(OBControl con : controller.filterControls("num_.*"))
            {
                ((OBLabel)con).setColour((int)con.settings.get("num_colour"));
            }
        }

        if(hideNum)
            controller.hideControls("num_.*");

        controller.unlockScreen();
    }

    public static void drawCurveLine(int num,boolean clockwise, boolean label, OC_SectionController controller) throws Exception
    {
        drawCurveLine(num, clockwise,label,clockwise ? OBUtils.colorFromRGBString("255,252,0") : OBUtils.colorFromRGBString("255,83,0"), controller);
    }


    public static void drawCurveLine(int num,boolean clockwise, boolean label, int colour, OC_SectionController controller)
    {
        OBPath startDiv = (OBPath)controller.objectDict.get(String.format("divline_%d",clockwise ? num : num-1));
        OBPath endDiv = (OBPath)controller.objectDict.get(String.format("divline_%d", clockwise ? num+1 : num));
        OBPath mainLine = (OBPath)controller.objectDict.get("mainline");

        OBPath curveLine =  new OBPath();

        PointF centrePoint = new PointF(startDiv.position().x +(endDiv.position().x - startDiv.position().x)/2.0f , mainLine.position().y);
        float radius = Math.abs((endDiv.position().x - startDiv.position().x))/2.0f;
        Path path = new Path();

        RectF rect = new RectF(startDiv.position().x,startDiv.position().y-radius,endDiv.position().x,startDiv.position().y+radius);
        path.addArc(rect,clockwise ? 180 : 0, clockwise ? 180 : -180);
        curveLine.setPath(path);
        curveLine.setLineWidth(startDiv.lineWidth());

        curveLine.setStrokeColor(colour);
        curveLine.setZPosition(5);
        curveLine.sizeToBoundingBox();
        curveLine.setBottom(mainLine.top()+ 0.5f*mainLine.lineWidth());
        curveLine.sizeToBoundingBoxIncludingStroke();
        curveLine.hide();
        controller.attachControl(curveLine);
        controller.objectDict.put(String.format("curveline_%d", num),curveLine);

        if(label)
        {
            float fontSize = (float)mainLine.settings.get("fontsize")*0.7f;

            OBLabel eqLabel = new OBLabel(clockwise ? "+" : "–", OBUtils.standardTypeFace(), fontSize);
            OBLabel numLabel = new OBLabel("1", OBUtils.standardTypeFace(), fontSize);
            numLabel.setColour(Color.BLACK);
            eqLabel.setColour(Color.BLACK);
            OBGroup eqGroup = new OBGroup(Collections.singletonList((OBControl)eqLabel));
            eqGroup.sizeToTightBoundingBox();

            OBGroup numGroup =  new OBGroup(Collections.singletonList((OBControl)numLabel));
            numGroup.sizeToTightBoundingBox();

            eqGroup.setPosition (numGroup.position());
            eqGroup.setRight(numGroup.left() - eqGroup.width() * 0.3f);

            OBGroup fullGroup = new OBGroup((List<OBControl>)(Object)Arrays.asList(eqGroup, numGroup));

            fullGroup.setPosition ( centrePoint);
            fullGroup.setBottom(fullGroup.bottom()  - (fullGroup.height()*1.0f + radius));
            fullGroup.hide();

            fullGroup.objectDict.put("numLabel",numLabel);
            fullGroup.objectDict.put("eqLabel",eqLabel);

            controller.attachControl(fullGroup);
            controller.objectDict.put(String.format("linelabel_%d", num),fullGroup);

        }
    }


    public static void animateNumberLineShow(float duration, OC_SectionController controller) throws Exception
    {
        OBPath mainLine = (OBPath)controller.objectDict.get("mainline");
        int size = (int)mainLine.settings.get("num_size") ;
        int start = (int)mainLine.settings.get("num_start") ;
        mainLine.setStrokeEnd(0);
        mainLine.show();

        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.propertyAnim("strokeEnd",1,mainLine)),duration,false,OBAnim.ANIM_LINEAR,controller);

        for(int i=start; i<start+size; i++)
        {
            OBPath divLine = (OBPath)controller.objectDict.get(String.format("divline_%d", i));
            divLine.setStrokeEnd(0);
            divLine.show();
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.propertyAnim("strokeEnd",1,divLine)), duration*1.0f/size,true,OBAnim.ANIM_LINEAR,controller);
        }

        controller.waitForSecs(0.2f);

    }

    public static void setUpBasket(OC_SectionController controller)
    {
        controller.objectDict.get("box_back").setZPosition(1.1f);
        controller.objectDict.get("box_front").setZPosition(1.3f);
        controller.objectDict.get("box_top").setZPosition(1.4f);
        moveBasketParts(controller);
    }

    public static void moveBasketParts(OC_SectionController controller)
    {
        controller.objectDict.get("box_back").setPosition(controller.objectDict.get("box_front").position());
        controller.objectDict.get("box_top").setPosition(controller.objectDict.get("box_front").position());
    }


    public static void loadEquation(String string,String name, OBControl boxControl, int colour, boolean groupBox, OC_SectionController controller)
    {
        loadEquation(string,name,boxControl,colour,groupBox,1,1,controller);
    }


    public static void loadEquation(String string,String name, OBControl boxControl, int colour, boolean groupBox, float realign, float size, OC_SectionController controller)
    {
        float fontSize = size*105.0f*boxControl.height()/130.0f;
        float left = boxControl.left();
        int index = 1;
        Map<String,OBControl> parts = new ArrayMap<>();
        Typeface font = OBUtils.standardTypeFace();
        OBLabel spaceLabel = new OBLabel(" ", font,fontSize);
        float dist = spaceLabel.width()*realign;
        String[] eqParts =  string.split(" ");
        for(int i=0; i<eqParts.length; i++)
        {
            String part = eqParts[i];
            OBLabel eqLabel = new OBLabel(part,font,fontSize);
            eqLabel.setColour(colour);

            OBGroup labelGroup = new OBGroup(Collections.singletonList((OBControl)eqLabel));
            labelGroup.sizeToTightBoundingBox();
            labelGroup.objectDict.put("label",eqLabel);
            labelGroup.setZPosition(1.5f);

            if(realign>0)
            {
                labelGroup.setPosition(boxControl.position());
                labelGroup.setLeft(left);
                left = labelGroup.right() + dist;
            }
            else
            {
                labelGroup.setPosition(OB_Maths.locationForRect(1.0f/(eqParts.length+1) * (i+1),0.5f,boxControl.frame()));
            }

            parts.put(String.format("part%d",index++),labelGroup);
            labelGroup.setShouldTexturise(false);
            controller.attachControl(labelGroup);
            labelGroup.show();
            //parts.put(String.format("label_%d",index++),eqLabel);
        }

        List<OBControl> conts = new ArrayList<>(parts.values());
        OBGroup fullGroup = new OBGroup(conts);

        if(realign>0)
            fullGroup.setPosition(boxControl.position());

        if(groupBox)
        {
            parts.put("box",boxControl);
            boxControl.setZPosition(0);
            fullGroup.setZPosition(1.5f);
            OBGroup completeGroup = new OBGroup(Arrays.asList(boxControl, fullGroup));
            completeGroup.objectDict = parts;
            controller.attachControl(completeGroup);
            controller.objectDict.put(name,completeGroup);
        }
        else
        {
            fullGroup.setShouldTexturise(false);
            fullGroup.objectDict = parts;
            controller.attachControl(fullGroup);
            controller.objectDict.put(name,fullGroup);
        }
    }

    public static void showEquation(OBGroup equation,int from, int to, String audio, OC_SectionController controller) throws Exception
    {
        controller.lockScreen();
        for(int i = from; i<=to; i++)
        {
            equation.objectDict.get(String.format("part%d",i)).show();
        }

        if(audio != null)
            controller.playSfxAudio(audio,false);

        controller.unlockScreen();

        if(audio != null)
            controller.waitSFX();

    }

    public static void hideEquation(OBGroup equation, OC_SectionController controller)
    {

        controller.lockScreen();
        for(int i = 1; i<=5; i++)
        {
            equation.objectDict.get(String.format("part%d",i)).hide();
        }
        controller.unlockScreen();
    }


    public static void colourEquation(OBGroup equation, int from, int to, int colour, OC_SectionController controller)
    {
        controller.lockScreen();
        for(int i=from; i <= to; i++)
        {
            OBGroup group = (OBGroup)equation.objectDict.get(String.format("part%d",i));
            ((OBLabel)group.objectDict.get("label")).setColour(colour);
        }

        controller.unlockScreen();
    }

    public static OBLabel getLabelForEquation(int labelNum, OBGroup equation)
    {
        OBGroup group = (OBGroup)equation.objectDict.get(String.format("part%d",labelNum));
        return (OBLabel)group.objectDict.get("label");
    }


}
