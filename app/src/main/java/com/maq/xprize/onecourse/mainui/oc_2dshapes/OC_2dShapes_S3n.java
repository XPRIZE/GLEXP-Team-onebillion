package com.maq.xprize.onecourse.mainui.oc_2dshapes;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;
import com.maq.xprize.onecourse.utils.ULine;
import com.maq.xprize.onecourse.utils.UPath;
import com.maq.xprize.onecourse.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/03/2018.
 */

public class OC_2dShapes_S3n extends OC_SectionController
{
    List<OBPath> lines;
    List<OBPath> flyLines;
    int fillColour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master3");
        lines = new ArrayList<>();
        flyLines = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3n();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        if(objectDict.get("overlay") != null)
            detachControl(objectDict.get("overlay"));
        deleteControls("obj_.*");
        super.setSceneXX(scene);
        lines.clear();
        flyLines.clear();
        OBControl align = null;
        OBPath path =(OBPath) objectDict.get("overlay");
        float lineWidth = path.lineWidth();
        UPath upath = deconstructedPath(currentEvent(), "overlay");
        USubPath subpath = upath.subPaths.get(0);
        int index = 0;
        String[] borders = eventAttributes.get("borders").split(",");
        for(ULine element : subpath.elements)
        {
            float len = path.lineWidth()*0.5f/element.length();
            PointF pt1 = element.tAlongt(0 - len,null);
            PointF pt2 = element.tAlongt(1 + len,null);
            OBPath line = new OBPath(pt1,pt2);
            line.setLineWidth(lineWidth);
            line.setStrokeColor(path.strokeColor());
            attachControl(line);
            line.setZPosition(10);
            line.sizeToBoundingBoxIncludingStroke();
            lines.add(line);
            line.hide();
            PointF outvec = OB_Maths.DiffPoints(pt2,pt1);
            float degrees = 180 - (float)Math.toDegrees(Math.atan2(outvec.y, outvec.x));
            if(Math.abs(degrees) >= 180)
                degrees -= Math.floor(degrees/180) *180.0;
            float radians = (float)Math.toRadians(degrees);
            OBPath line2 = (OBPath)line.copy();
            line2.setRotation(radians);
            attachControl(line2);
            int y = OBUtils.getIntValue(borders[index]);
            line2.setPosition(OB_Maths.locationForRect(0.5f,y*1.0f/(subpath.elements.size()+1),objectDict.get("bottombar").frame()));
            if(y == 1)
                align = line2;
            index++;
            line2.hide();
            flyLines.add(line2);
        }

        for(OBControl line : flyLines)
            line.setLeft(align.left());
        fillColour = path.fillColor();
        path.setFillColor(Color.TRANSPARENT);
        path.sizeToBoundingBoxIncludingStroke();
        for(OBControl control : filterControls("obj_.*"))
        {
            control.setColourOverlay(Color.argb(125,255,255,255));
            control.lowlight();
            control.hide();
        }
    }

    public void doMainXX() throws Exception
    {
        showScene();
        startScene();
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            List<OBControl> targs = filterControls("obj_.*");
            OBControl targ = finger(0,1,targs,pt);
            if(targ == null)
            {
                for(OBControl con : targs)
                {
                    if(con.frame() .contains(pt.x, pt.y))
                    {
                        targ = con;
                        break;
                    }
                }
            }
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                final OBGroup targGroup = (OBGroup)targ;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget(targGroup);
                    }
                });
            }
        }
    }

    public void checkTarget(OBGroup targ) throws Exception
    {
        playAudio(null);
        targ.setColourOverlay(Color.argb(125,255,255,255));;
        if(targ.attributes().get("correct") != null  && targ.attributes().get("correct").equals("true"))
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            if(!performSel("demoFin",currentEvent()))
            {
                animateLines();
                playAudioQueuedScene("FINAL",0.3f,true);
            }
            waitForSecs(0.3f);
            if(events.get(events.size()-1) != currentEvent())
            {
                lockScreen();
                hideControls("obj_.*");
                objectDict.get("overlay").hide();
                unlockScreen();
                waitForSecs(0.3f);
            }
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.lowlight();
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void showScene() throws Exception
    {
        lockScreen();
        showControls("obj_.*");
        for(OBControl con : flyLines)
            con.show();
        playSfxAudio("show_2",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
    }

    public void animateLines() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(int i=0; i<flyLines.size(); i++)
        {
            anims.addAll(getLineAnimationForIndex(i));
        }
        OBAnimationGroup.runAnims(anims,0.7,true, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        showFill();
    }

    public List<OBAnim> getLineAnimationForIndex(int index) throws Exception
    {
        OBControl flyLine = flyLines.get(index);
        OBControl targetLine = lines.get(index);
        return Arrays.asList(OBAnim.moveAnim(targetLine.position(),flyLine),
                OBAnim.rotationAnim(targetLine.rotation,flyLine));
    }

    public void showFill() throws Exception
    {
        OBPath obj =(OBPath ) objectDict.get("overlay");
        lockScreen();
        for(OBControl line : flyLines)
            detachControl(line);
        obj.show();
        unlockScreen();
        waitForSecs(0.3f);
        obj.setFillColor(fillColour);
        waitForSecs(0.3f);
    }

    public void demo3n() throws Exception
    {
        waitForSecs(0.5f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        showScene();
        loadPointer(POINTER_LEFT);
        OBPath line = flyLines.get(2);
        RectF frame = convertRectFromControl(line.bounds(),line);
        moveScenePointer(OB_Maths.locationForRect(new PointF(1.2f, 0.5f) , frame)
                ,-40,0.5f,"DEMO",1,0.3f);
        PointF loc = OB_Maths.locationForRect(new PointF(1.5f, 0.5f) , frame);
        loc.y = (objectDict.get("bottombar").top() - applyGraphicScale(20));
        moveScenePointer(loc,-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin3o() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        int strokeColour = flyLines.get(0).strokeColor();
        playAudioScene("FINAL",0,true);
        int zPosition = 30;
        for(int i=0; i<=1; i++)
        {
            anims.clear();
            int index1 = i==0?1:0;
            int index2 = i==0?3:2;
            anims.addAll(getLineAnimationForIndex(index1));
            anims.addAll(getLineAnimationForIndex(index2));
            OBPath line1 =flyLines.get(index1);
            OBPath line2 =flyLines.get(index2);
            line1.setZPosition(zPosition);
            line2.setZPosition(zPosition);
            OBAnimationGroup.runAnims(anims,0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            lockScreen();
            line1.setStrokeColor(Color.GRAY);
            line2.setStrokeColor(Color.GRAY);
            unlockScreen();
            playAudioScene("FINAL",i+1,true);
            waitForSecs(0.3f);
            lockScreen();
            line1.setStrokeColor(strokeColour);
            line2.setStrokeColor(strokeColour);
            unlockScreen();
            zPosition++;
            waitForSecs(0.3f);
        }
        showFill();
        waitForSecs(0.5f);
    }


}
