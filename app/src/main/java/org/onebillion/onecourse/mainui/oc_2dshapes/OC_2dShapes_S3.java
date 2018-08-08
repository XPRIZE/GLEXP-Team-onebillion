package org.onebillion.onecourse.mainui.oc_2dshapes;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;
import org.onebillion.onecourse.utils.ULine;
import org.onebillion.onecourse.utils.UPath;
import org.onebillion.onecourse.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 06/03/2018.
 */

public class OC_2dShapes_S3 extends OC_SectionController
{
    List<OBPath> lines,  linesPart;
    List<OBControl> hotAreas;
    OBPath lastLine;
    int highlightColour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        lines = new ArrayList<>();
        linesPart = new ArrayList<>();
        hotAreas = new ArrayList<>();
        lastLine = null;
        highlightColour = OBUtils.colorFromRGBString(eventAttributes.get("colour_highlight"));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3a();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        if(eventAttributes.get("reload_skip") == null && currentEvent() != events.get(1))
            detachControl(objectDict.get("obj"));
        super.setSceneXX(scene);
        if(eventAttributes.get("reload_skip") == null)
        {
            for(OBPath lin : lines)
                detachControl(lin);
            for(OBPath lin : linesPart)
                detachControl(lin);
            lines.clear();
            linesPart.clear();
            OBPath path =(OBPath)objectDict.get("obj");
            PointF movePoint = new PointF(0,0);
            if(path.propertyValue("event") == null)
            {
                path.setProperty("event", currentEvent());
                path.setProperty("start_loc", OBMisc.copyPoint(path.position()));
                path.sizeToBoundingBoxIncludingStroke();
            }
            else
            {
                PointF startLoc = (PointF)path.propertyValue("start_loc");
                movePoint = OB_Maths.DiffPoints(path.position(),startLoc);
            }
            float lineWidth = path.lineWidth()*4.0f;
            UPath upath = deconstructedPath((String)path.propertyValue("event"),"obj");
            USubPath subpath = upath.subPaths.get(0);
            for(ULine element : subpath.elements)
            {
                float len = path.lineWidth()*0.5f/element.length();
                PointF pt1 = element.tAlongt(0 - len,null);
                PointF pt2 = element.tAlongt(1 + len,null);
                OBPath line = new OBPath(pt1,pt2);
                //line.setPosition(OB_Maths.AddPoints(line.position(),movePoint));
                line.setLineWidth(lineWidth);
                line.setStrokeColor(highlightColour);
                attachControl(line);
                line.setZPosition(10);
                lines.add(line);
                line.hide();
                line.sizeToBoundingBoxIncludingStroke();
            }

            for(int i=0; i<subpath.elements.size(); i++)
            {
                OBPath partPath = null;
                if(i+1 == subpath.elements.size())
                {
                    partPath = (OBPath)path.copy();
                }
                else
                {
                    USubPath sp = new USubPath();
                    for(int j=0; j<=i; j++)
                    {
                        ULine element = subpath.elements.get(j);
                        float len = path.lineWidth()*0.5f/element.length();
                        PointF pt1 = element.tAlongt(j==0? 0-len : 0, null);
                        PointF pt2 = element.tAlongt(j==i? 1+len : 1,null);
                        sp.elements.add(new ULine(pt1.x,pt1.y,pt2.x, pt2.y));
                    }
                    partPath = new OBPath();
                    partPath.setPath(sp.bezierPath());
                    partPath.setPosition(OB_Maths.AddPoints(partPath.position(),movePoint));
                }
                partPath.setLineWidth(lineWidth);
                partPath.setStrokeColor(highlightColour);
                partPath.setFillColor(Color.TRANSPARENT);
                partPath.setZPosition(10);
                partPath.sizeToBoundingBoxIncludingStroke();
                attachControl(partPath);
                linesPart.add(partPath);
                partPath.hide();
            }


        }

        if(eventAttributes.get("target").equals("edge") && hotAreas.size() == 0)
        {
            for(int i=0; i<lines.size(); i++)
            {
                OBPath copyLine = (OBPath)lines.get(i).copy();
                copyLine.show();
                hotAreas.add(copyLine);
            }
        }
        else if(eventAttributes.get("nums") != null)
        {
            deleteControls("num_.*");
            List<String> nums = Arrays.asList(eventAttributes.get("nums").split(","));
            OBMisc.loadNumbersFrom(OBUtils.getIntValue(nums.get(0)),OBUtils.getIntValue(nums.get(1)), Color.BLACK,"numbox","num_", this);
            if(OBUtils.getIntValue(nums.get(0)) > 1)
                showControls("num_.*");
        }
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
             goToCard(OC_2dShapes_S3i.class , "event3");
        }catch (Exception e)
        {

        }
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            OBControl obj = objectDict.get("obj");
            if(eventAttributes.get("target").equals("edge"))
            {
                final OBControl targ = finger(0,2,hotAreas,pt);
                if(targ != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkEdge((OBPath)targ);
                        }
                    });
                }
                else if(obj.getWorldFrame().contains(pt.x, pt.y))
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            wrongAction(null);
                        }
                    });
                }
            }
            else
            {
                final OBControl targ = finger(0,1,filterControls("num_.*"),pt);
                if(targ != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkNum((OBLabel)targ);
                        }
                    });
                }
            }
        }
    }

    public void checkEdge(OBPath targ) throws Exception
    {
        playAudio(null);
        targ = lines.get(hotAreas.indexOf(targ));
        targ.show();
        if(targ != lastLine)
        {
            gotItRightBigTick(true);
            lastLine = targ;
            waitForSecs(0.5f);
            targ.hide();
            performSel("demoFin",currentEvent());
            nextScene();
        }
        else
        {
            wrongAction(targ);
        }
    }

    public void checkNum(OBLabel targ) throws Exception
    {
        playAudio(null);
        targ.setColour(Color.RED);
        if((int)targ.propertyValue("num_value")
                == OBUtils.getIntValue(eventAttributes.get("correct")))
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            demoCountEdges();
            targ.setColour(Color.BLACK);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.setColour(Color.BLACK);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void wrongAction(OBPath targ) throws Exception
    {
        gotItWrongWithSfx();
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitSFX();
        if(targ != null)
            targ.hide();
        if(time == statusTime)
            playAudioQueuedScene("INCORRECT",0.3f,false);
    }


    public void demo3a() throws Exception
    {
        lockScreen();
        objectDict.get("obj").show();
        playSfxAudio("show",false);

        unlockScreen();
        waitSFX();
        loadPointer(POINTER_LEFT);
        OBControl obj = objectDict.get("obj");
        RectF frame = new RectF(obj.getWorldFrame());
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.7f, 0.7f) , frame),-45,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.1f, 0.55f) , frame),0.5f,true);
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0f, 0.55f) , frame),0.2f,true);
        lockScreen();
        linesPart.get(0).show();
        playSfxAudio("show",false);

        unlockScreen();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.1f, 0.55f) , frame),-45,0.2f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.55f, 0.1f) , frame),-30,0.5f,true);
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.55f, 0) , frame),0.2f,true);
        lockScreen();
        linesPart.get(0).hide();
        linesPart.get(1).show();
        playSfxAudio("show",false);

        unlockScreen();
        waitSFX();
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.55f, 0.1f) , frame),-30,0.2f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        linesPart.get(1).hide();
        nextScene();
    }

    public void demoFin3c() throws Exception
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(objectDict.get("loc").position() ,objectDict.get("obj"))),0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void demo3d() throws Exception
    {
        lockScreen();
        showControls("num_.*");
        objectDict.get("bottombar").show();
        playSfxAudio("show",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        startScene();
    }

    public void demoCountEdges() throws Exception
    {
        loadPointer(POINTER_LEFT);
        OBControl obj = objectDict.get("obj");
        moveScenePointer(OB_Maths.locationForRect(new PointF(1.2f, 0.7f) , obj.getWorldFrame()),-45,0.5f,"FINAL",0,0.3f);
        for(int i=0; i<lines.size(); i++)
        {
            PointF loc = OBMisc.copyPoint(lines.get(i).position());
            loc.x += applyGraphicScale(25);
            loc.y += applyGraphicScale(15);
            movePointerToPoint(loc,i==0 ? 0.5f : 0.3f,true);
            lockScreen();
            if(i > 0)
                linesPart.get(i-1).hide();
            linesPart.get(i).show();

            unlockScreen();
            playAudioScene("FINAL",i+1,true);
            waitForSecs(0.15f);
        }
        if(!performSel("demoFin",currentEvent()))
        {
            waitForSecs(0.5f);
            thePointer.hide();
            waitForSecs(0.5f);
            linesPart.get(lines.size()-1).hide();
            waitForSecs(0.5f);
        }
    }

    public void demoEdgesAudio() throws Exception
    {
        OBControl obj = objectDict.get("obj");
        movePointerToPoint(OB_Maths.locationForRect(new PointF(1.2f, 0.7f) ,obj.getWorldFrame()),0.5f,true);
        playAudioQueuedScene("FINAL2",0.3f,true);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        linesPart.get(lines.size()-1).hide();
        waitForSecs(0.5f);
    }

    public void demoFin3d() throws Exception
    {
        demoEdgesAudio();
    }

    public void demoFin3e() throws Exception
    {
        OBControl obj = objectDict.get("obj");
        moveScenePointer(OB_Maths.locationForRect(new PointF(1.2f, 0.7f) , obj.getWorldFrame()),-45,0.5f,"FINAL2",0,0.5f);
        linesPart.get(lines.size()-1).hide();
        waitForSecs(0.5f);
        lockScreen();
        lines.get(0).show();
        lines.get(2).show();

        unlockScreen();
        playAudioScene("FINAL2",1,true);
        waitForSecs(0.3f);
        lockScreen();
        lines.get(0).hide();
        lines.get(2).hide();
        lines.get(1).show();
        lines.get(3).show();

        unlockScreen();
        playAudioScene("FINAL2",2,true);
        waitForSecs(0.3f);
        lockScreen();
        lines.get(1).hide();
        lines.get(3).hide();

        unlockScreen();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);

    }

    public void demoFin3f() throws Exception
    {
        demoEdgesAudio();
    }

}
