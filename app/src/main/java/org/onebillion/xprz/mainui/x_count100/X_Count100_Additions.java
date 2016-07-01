package org.onebillion.xprz.mainui.x_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.OBViewController;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by michal on 27/06/16.
 */
public class X_Count100_Additions
{


    public static void drawGrid(int count, OBControl rect, int borderColour, int textColour, boolean single, OBSectionController controller)
    {
        List<OBControl> allControls = new ArrayList<>();
        OBMainViewController mainViewController = OBMainViewController.MainViewController();
        while(rect.frame().intersect(mainViewController.topRightButton.frame())||
                rect.frame().intersect(mainViewController.topLeftButton.frame()))
            rect.setScale(rect.scale() * 0.99f);

        List<OBControl> boxes = new ArrayList<OBControl>();
        List<OBControl> numbers = new ArrayList<OBControl>();

        int lineSize = (int)Math.ceil((double)controller.applyGraphicScale(4f));

        int textSize = 50;

        float fontSize = 0;
        if(single)
        {
            fontSize = textSize * rect.height()/72;
        }
        else
        {
            if(count>5)
                textSize = 27;

            fontSize = textSize * rect.height()/360;
        }

        Typeface font = OBUtils.standardTypeFace();
        int height = Math.round(rect.height() +lineSize);
        int width = Math.round((float)Math.floor(0.1 * rect.width()) +lineSize);

        if(!single)
            height = Math.round((float)Math.floor((1.0/count) * height));



        PointF centrePoint = OB_Maths.locationForRect(0.5f,0.5f, rect.frame());

        float startX = (float)Math.floor(centrePoint.x - 5.0f*width + 5.0f*lineSize);
        float startY = (float)Math.floor(centrePoint.y - count*0.5f*height + (count-1)*0.5f*lineSize);

        if(single)
            startY=centrePoint.y-0.5f*height;

        float curX = startX;
        float curY = startY;

        int start = 0;
        if(single)
            start = count-1;

        for (int i = start;i < count;i++)
        {
            for (int j = 0;j < 10;j++)
            {
                RectF boxf = new RectF(0,0,width,height);
                OBControl box = new OBControl();
                box.setFrame(boxf);
                box.setLeft(curX);
                box.setTop(curY);
                //box.layer.allowsEdgeAntialiasing = false;
                box.setBorderWidth(lineSize);
                box.setBorderColor(borderColour);
                box.setBackgroundColor(Color.WHITE);
                box.setZPosition(1);
                boxes.add(box);
                int n = i * 10 + j + 1;
                OBLabel txt = new OBLabel(Integer.toString(n),font,fontSize);

                txt.setColour(textColour);

                txt.setPosition(box.position());

                numbers.add(txt);
                curX = box.right() - (float)(lineSize/2.0) ;
                box.setMasksToBounds(true);
                box.setProperty("num_value", n);
                txt.setProperty("num_value", n);
                txt.setZPosition(2);
                controller.objectDict.put(String.format("box_%d",n), box);
                controller.objectDict.put(String.format("num_%d",n), txt);

                controller.attachControl(box);
                controller.attachControl(txt);
                allControls.add(box);
                allControls.add(txt);
            }
            if(!single)
            {
                curX = startX;
                curY = boxes.get(10*(i)).bottom() - (float)(lineSize/2.0) ;
            }
        }



        for (int i = 0;i < 10;i++)
        {
            if(single)
            {
                OBLabel number = (OBLabel)numbers.get(i);
                number.setRight(boxes.get(i).right() - 0.17f*boxes.get(i).width());

            }
            else
            {
                curX = numbers.get((((count-1)*10)+i)).right();
                for (int j = 0;j < (count-1);j++)
                {
                    OBLabel number = (OBLabel)numbers.get(((10*j)+i));
                    number.setRight(curX);

                }
            }
        }

        if(single)
        {
            OBControl frame = new OBControl();
            RectF r = new RectF(boxes.get(0).frame());
            r.union(boxes.get(boxes.size()-1).frame());
            frame.setFrame(r);
            frame.setBorderWidth(lineSize *1.5f);
            //frame.layer.allowsEdgeAntialiasing = false;
            frame.setBorderColor(borderColour);
            controller.objectDict.put(String.format("frame_%d",count), frame);
            controller.attachControl(frame);
        }

        OBGroup group = new OBGroup(allControls);controller.attachControl(group);
        group.setPosition(OB_Maths.locationForRect(0.5f,0.5f,controller.bounds()));
        controller.objectDict.put("grid_group", group);
    }

    public static void loadNumbersAudio(OBSectionController controller)
    {
        Map<String,Object> audioScenes = controller.audioScenes;
        controller.loadAudioXML(controller.getConfigPath("numbers.xml"));
        controller.audioScenes.putAll(audioScenes);
    }
    public static void playNumberAudio(int num, boolean wait, OBSectionController controller) throws Exception
    {
        Map<String,Object> map = (Map<String,Object>)controller.audioScenes.get("extra");
        List<Object> audios = (List<Object>)map.get("numbers");

        if(audios.size() > num-1)
        {
            controller.playAudio((String)audios.get(num-1));
            if(wait)
                controller.waitAudio();
        }

    }

    public static int getEventColourForIndex(int index, OBSectionController controller)
    {
        Map<String,Object> colourEventDict =  (Map<String,Object>)controller.eventsDict.get("colours");
        Map<String,Object> eventAttrs =  (Map<String,Object>)colourEventDict.get("attrs");
        return OBUtils.colorFromRGBString((String)eventAttrs.get(String.format("col%",index)));
    }

}
