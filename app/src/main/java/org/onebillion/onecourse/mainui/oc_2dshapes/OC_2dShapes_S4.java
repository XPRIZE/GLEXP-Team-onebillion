package org.onebillion.onecourse.mainui.oc_2dshapes;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
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
 * Created by michal on 07/03/2018.
 */

public class OC_2dShapes_S4 extends OC_SectionController
{
    List<OBControl> marks;
    List<OBControl> targets;
    boolean numMode;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        marks = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        ((OBPath)objectDict.get("mark")).sizeToBoundingBoxIncludingStroke();
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo4a();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        if(marks.size() > 0)
        {
            for(OBControl con : marks)
                detachControl(con);
            marks.clear();
        }
        if(objectDict.get("obj") != null)
            detachControl(objectDict.get("obj"));
        deleteControls("obj_.*");
        super.setSceneXX(scene);
        numMode = eventAttributes.get("target").equals("num");
        if(numMode)
        {
            if(eventAttributes.get("nums") != null)
            {
                deleteControls("num_.*");
                String[] nums = eventAttributes.get("nums").split(",");
                OBMisc.loadNumbersFrom(OBUtils.getIntValue(nums[0]),OBUtils.getIntValue(nums[1]),
                        Color.BLACK,"numbox","num_", this);
                if(currentEvent() != events.get(0))
                    showControls("num_.*");
                targets = filterControls("num_.*");
            }
            else
            {
                for(OBControl num : filterControls("num_.*"))
                    ((OBLabel)num).setColour(Color.BLACK);
            }

            OBControl mark = objectDict.get("mark");
            OBPath path =(OBPath)objectDict.get("obj");
            PointF pt = OB_Maths.locationForRect(0.5f,0.5f,this.bounds());
            pt.y = objectDict.get("bottombar").top() * 0.5f;
            PointF movePoint = OB_Maths.DiffPoints(pt,path.position());
            path.setProperty("move_loc", movePoint);
            path.setPosition(pt);
            if(OBUtils.getIntValue(eventAttributes.get("correct")) != 0)
            {
                UPath upath = deconstructedPath(currentEvent(), "obj");
                USubPath subpath = upath.subPaths.get(0);
                for (ULine element : subpath.elements)
                {
                    PointF pt1 = element.tAlongt(1, null);
                    OBControl copy = mark.copy();
                    copy.setPosition(OB_Maths.AddPoints(pt1, movePoint));
                    attachControl(copy);
                    marks.add(copy);
                    copy.setZPosition(12);
                }
            }

            path.sizeToBoundingBoxIncludingStroke();
        }
        else
        {
            hideControls("num_.*");
            objectDict.get("bottombar").hide();
            targets = filterControls("obj_.*");
            for(OBControl con : targets)
                if(con instanceof OBPath)
                    ((OBPath)con).sizeToBoundingBoxIncludingStroke();
        }
    }

    public void doMainXX() throws Exception
    {
        if(!numMode)
        {
            waitForSecs(0.5f);
            showObjs();
        }
        startScene();
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (numMode)
            {
                final OBControl targ = finger(0, 1, targets, pt);
                if (targ != null)
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
            else
            {
                final OBControl targ = finger(0, 1, targets, pt);
                if (targ != null && targ.isEnabled())
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkPath((OBPath)targ);
                        }
                    });
                }
            }
        }

    }

    public void checkNum(OBLabel targ) throws Exception
    {
        playAudio(null);
        targ.setColour(Color.RED);
        if ((int)targ.propertyValue("num_value") == OBUtils.getIntValue(eventAttributes.get("correct")))
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            if (getAudioForScene(currentEvent(),"FINAL") != null)
            {
                playAudioQueuedScene("FINAL", 300, true);
                waitForSecs(0.3f);
            }

            if (getAudioForScene(currentEvent(),"FINAL2") != null)
            {
                for (int i = 0; i < marks.size(); i++)
                {
                    marks.get(i).show();
                    playAudioScene("FINAL2", i, true);
                    waitForSecs(0.2f);
                }
            }
            playAudioQueuedScene("FINAL3", 300, true);
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.setColour(Color.BLACK);
            if (time == statusTime)
                playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void checkPath(OBPath targ) throws Exception
    {
        playAudio(null);
        int fillColour = targ.fillColor();
        targ.setFillColor(OBUtils.highlightedColour(fillColour));
        if (OBUtils.getBooleanValue((String)targ.attributes().get("correct")))
        {
            targ.disable();
            if (checkComplete())
            {
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                playAudioQueuedScene("FINAL", 300, true);
                waitForSecs(0.3f);
                nextScene();
            }
            else
            {
                gotItRightBigTick(false);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.setFillColor(fillColour);
            if (time == statusTime)
                playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK), this);
    }

    public List<OBPath> createDemoLines() throws Exception
    {
        List<OBPath> linesPart = new ArrayList<>();
        OBPath path =(OBPath)objectDict.get("obj");
        PointF movePoint = (PointF)path.propertyValue("move_loc");
        UPath upath = deconstructedPath(currentEvent(),"obj");
        USubPath subpath = upath.subPaths.get(0);
        List<ULine> elements = Arrays.asList(subpath.elements.get(0),subpath.elements.get(subpath.elements.size()-1));
        for(int i=0; i<elements.size(); i++)
        {
            OBPath partPath = null;
            USubPath sp = new USubPath();
            for(int j=0; j<=i; j++)
            {
                ULine element = elements.get(j);
                float len = path.lineWidth()*0.5f/element.length();
                PointF pt1 = OB_Maths.AddPoints(element.tAlongt(j==0? 1+len : 1 ,null),movePoint);
                PointF pt2 = OB_Maths.AddPoints(element.tAlongt(j==i? 0-len : 0,null),movePoint);
                sp.elements.add(new ULine(pt1.x, pt1.y,pt2.x, pt2.y));
            }
            partPath = new OBPath();
            partPath.setPath(sp.bezierPath());
            partPath.setLineWidth(path.lineWidth()*3.0f);
            partPath.setStrokeColor(((OBPath)objectDict.get("mark")).strokeColor());
            partPath.setFillColor(Color.TRANSPARENT);
            partPath.setZPosition(10);
            partPath.hide();
            partPath.sizeToBoundingBoxIncludingStroke();
            attachControl(partPath);
            linesPart.add(partPath);
        }
        return linesPart;
    }

    public boolean checkComplete() throws Exception
    {
        boolean complete = true;
        for(OBControl con : targets)
        {
            if(con.isEnabled() == OBUtils.getBooleanValue((String)con.attributes().get("correct")))
            {
                complete = false;
                break;
            }
        }
        return complete;
    }

    public void showObjs() throws Exception
    {
        List<OBControl> objs = filterControls("obj_.*");
        objs = OBUtils.randomlySortedArray(objs);
        for(int i=0; i<objs.size(); i++)
        {
            objs.get(i).show();
            playSfxAudio(String.format(objs.size()>5? "note2_%d" : "note_%d",i+1),true);
            waitForSecs(0.2f);
        }
    }

    public void demo4a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        List<OBPath> demoLines = createDemoLines();
        OBPath obj =(OBPath ) objectDict.get("obj");
        RectF frame = convertRectFromControl(obj.bounds,obj);
        moveScenePointer(OB_Maths.locationForRect(new PointF(1.1f, 0.6f) , frame)
                ,-30,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.05f, 0.6f) , frame)
                ,-35,0.5f,true);
        demoLines.get(0).show();
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 1.05f) , frame)
                ,-35,0.5f,true);

        lockScreen();
        demoLines.get(0).hide();
        demoLines.get(1).show();
        unlockScreen();

        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.1f,1.1f) , frame)
                ,-30,0.5f,"DEMO",3,0.3f);
        marks.get(3).show();
        waitForSecs(1f);
        thePointer.hide();
        waitForSecs(0.3f);
        lockScreen();
        objectDict.get("bottombar").show();
        showControls("num_.*");
        marks.get(3).hide();
        demoLines.get(1).hide();

        unlockScreen();
        waitForSecs(0.3f);
        startScene();

    }



}
