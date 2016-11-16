package org.onebillion.onecourse.mainui.oc_shapeandposition;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 25/08/16.
 */
public class OC_Shapeandposition_S1B extends OC_SectionController
{
    public static int NO_DOTS_SHOWING = 0,
            DOT1_SHOWING = 1,
            DOT2_SHOWING = 2,
            BOTH_DOTS_SHOWING = 3;
    static int Z_DIST = 2800;
    static float fold_dur = 0.8f;
    OBControl firstDot,secondDot;
    int dotMode;

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("masterb");
        OBPath line = (OBPath) objectDict.get("line");
        line.setZPosition(20);
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void start()
    {
        setStatus(0);
        new AsyncTask<Void, Void,Void>()
        {
            protected Void doInBackground(Void... params) {
                try
                {
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.execute();
    }

    public void setSceneXX(String scene)
    {
        deleteControls("obj.*");
        deleteControls("dot.*");
        super.setSceneXX(scene);
        dotMode = 0;
        OBControl dot = objectDict.get("dot1");
        if(!dot.hidden() )
            dotMode |= DOT1_SHOWING;
        dot = objectDict.get("dot2");
        if(!dot.hidden() )
            dotMode |= DOT2_SHOWING;
        hideControls("dot.*");
        hideControls("line");
        objectDict.get("obj").setZPosition(objectDict.get("line").zPosition() -0.001f);
        List ts = new ArrayList<>();
        if((dotMode & DOT1_SHOWING) != 0)
            ts.add(objectDict.get("dot1"));
        if((dotMode & DOT2_SHOWING) != 0)
            ts.add(objectDict.get("dot2"));
        targets = ts;
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene("PROMPT",true);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
        waitForSecs(0.2f);
        if(dotMode != 0)
        {
            playSfxAudio("dotspopon",false);
            lockScreen();
            if((dotMode & DOT1_SHOWING) != 0)
                showControls("dot1");
            if((dotMode & DOT2_SHOWING) != 0)
                showControls("dot2");
            unlockScreen();
        }
        waitForSecs(0.2f);
        List<Object> audio = (List<Object>)(Object)currentAudio("PROMPT");
        audio.remove(0);
        playAudioQueued(OBUtils.insertAudioInterval(audio,300));
    }


    public boolean lineIsVertical()
    {
        PointF p1 = objectDict.get("dot1").position();
        PointF p2 = objectDict.get("dot2").position();
        return Math.abs(p2.x - p1.x) < Math.abs(p2.y - p1.y);
    }

    public void foldObject(OBGroup obj, boolean usePerspective) throws Exception
    {
        final boolean isVert = lineIsVertical();
        playSfxAudio("shapefolding",false);
        OBControl wholeobj = obj.objectDict.get("whole");
        final OBControl side1 = obj.objectDict.get("side1");
        final OBControl side2 = obj.objectDict.get("side2");
        OBPath side1c,side2c,wholec;
        if(wholeobj instanceof OBGroup )
        {
            wholec = (OBPath) ((OBGroup)wholeobj).objectDict.get("subwhole");
            side1c = (OBPath) ((OBGroup)side1).objectDict.get("subside");
            side2c = (OBPath) ((OBGroup)side2).objectDict.get("subside");
        }
        else
        {
            wholec =(OBPath)wholeobj;
            side1c =(OBPath)side1;
            side2c =(OBPath)side2;
        }
        int hiColor = side1c.fillColor();
        float xv = 0,yv = 1,zv = 0;
        if(!isVert)
        {
            xv = -1;yv = 0;
        }
        lockScreen();
        side1c.setFillColor(wholec.fillColor());
        side2c.setFillColor(wholec.fillColor());
        wholeobj.hide();
        side1.show();
        side1.setZPosition(10);
        side2.show();
        side2.setZPosition(9);
        if(isVert)
            side1.setAnchorPoint(1, 0.5f);
        else
            side1.setAnchorPoint(0.5f, 1);
        unlockScreen();

        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float ang = (float)(Math.PI / 2f * frac);
                side1.m34 = (float)(1.0 / -Z_DIST);
                if (isVert)
                    side1.setYRotation(ang);
                else
                    side1.setXRotation(ang);

            }
        }
        ),fold_dur,true,OBAnim.ANIM_EASE_IN,null);
        lockScreen();
        side1c.setFillColor(hiColor);
        unlockScreen();
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float ang = (float)(Math.PI / 2f + Math.PI / 2f * frac);
                    side1.m34 = (float)(1.0 / -Z_DIST);
                    if (isVert)
                        side1.setYRotation(ang);
                    else
                        side1.setXRotation(ang);
                }
            }
        ),fold_dur,true,OBAnim.ANIM_EASE_OUT,null);
    }

    public void unfoldObject(OBGroup obj, boolean usePerspective) throws Exception
    {
        final boolean isVert = lineIsVertical();
        playSfxAudio("shapefolding",false);
        OBControl wholeobj = obj.objectDict.get("whole");
        final OBControl side1 = obj.objectDict.get("side1");
        final OBControl side2 = obj.objectDict.get("side2");
        OBPath side1c,side2c,wholec;
        if(wholeobj instanceof OBGroup )
        {
            wholec = (OBPath) ((OBGroup)wholeobj).objectDict.get("subwhole");
            side1c = (OBPath) ((OBGroup)side1).objectDict.get("subside");
            side2c = (OBPath) ((OBGroup)side2).objectDict.get("subside");
        }
        else
        {
            wholec =(OBPath)wholeobj;
            side1c =(OBPath)side1;
            side2c =(OBPath)side2;
        }
        int hiColor = side1c.fillColor();
        float xv = 0,yv = 1,zv = 0;
        if(!isVert)
        {
            xv = -1;yv = 0;
        }

        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        float ang = (float)(Math.PI / 2f + Math.PI / 2f * (1-frac));
                        side1.m34 = (float)(1.0 / -Z_DIST);
                        if (isVert)
                            side1.setYRotation(ang);
                        else
                            side1.setXRotation(ang);

                    }
                }
        ),fold_dur,true,OBAnim.ANIM_EASE_IN,null);
        lockScreen();
        side1c.setFillColor(hiColor);
        unlockScreen();
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        float ang = (float)(Math.PI / 2f * (1 - frac));
                        side1.m34 = (float)(1.0 / -Z_DIST);
                        if (isVert)
                            side1.setYRotation(ang);
                        else
                            side1.setXRotation(ang);
                    }
                }
        ),fold_dur,true,OBAnim.ANIM_EASE_OUT,null);
    }

    public void adjustLineForPt1(PointF pt1,PointF pt2)
    {
        OBPath line = (OBPath)objectDict.get("line");
        Path bez = new Path();
        PointF pt = convertPointToControl(pt1,line);
        bez.moveTo(pt.x,pt.y);
        pt = convertPointToControl(pt2,line);
        bez.lineTo(pt.x,pt.y);
        line.setPath(bez);
    }

    public void animateLineForPt1(final PointF pt1,final PointF pt2,final PointF dragPt)
    {
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        PointF ptm = OB_Maths.tPointAlongLine(frac, dragPt, pt2);
                        adjustLineForPt1(pt1,ptm);
                        thePointer.setPosition(ptm);
                    }
                }
        ),0.2f,true,OBAnim.ANIM_EASE_OUT,null);
    }

    public void demo1h() throws Exception
    {
        OBGroup obj = (OBGroup) objectDict.get("obj");
        PointF destpt = OB_Maths.locationForRect(0.8f, 0.9f, obj.frame);
        PointF startpt = pointForDestPoint(destpt,15);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        playSfxAudio("dotspopon",false);

        lockScreen();
        showControls("dot.*");
        unlockScreen();
        waitForSecs(0.3f);
        OBControl dot = objectDict.get("dot1");
        PointF pt1 = dot.position();
        dot = objectDict.get("dot2");
        PointF pt2 = dot.position();

        movePointerToPoint(pt1,-1,true);
        playAudioScene("DEMO",1,false);
        waitForSecs(0.2f);
        float duration = (float)OBAudioManager.audioManager.duration() - 0.4f;
        OBPath line = (OBPath) objectDict.get("line");
        lockScreen();
        line.setPath(null);
        line.show();
        unlockScreen();
        final PointF px1 = pt1;
        final PointF px2 = pt2;

        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        PointF ptm = OB_Maths.tPointAlongLine(frac, px1, px2);
                        adjustLineForPt1(px1,px2);
                        thePointer.setPosition(ptm);
                    }
                }
        ),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);

        waitAudio();
        waitForSecs(0.3f);
        movePointerForwards(applyGraphicScale(-30),0.5f);
        lockScreen();
        hideControls("dot.*");
        unlockScreen();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO2",false);
        waitForSecs(0.2f);

        movePointerToPoint(OB_Maths.locationForRect(0.6f, 0.6f, obj.frame),-1,true);
        waitAudio();
        waitForSecs(0.2f);
        movePointerToPoint(destpt,-1,true);

        foldObject(obj,true);
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO3",true);
        waitForSecs(0.2f);
        unfoldObject(obj,true);
        waitForSecs(0.2f);
        movePointerToPoint(pt2,-1,true);
        playAudioQueuedScene("DEMO4",true);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo1n() throws Exception
    {
        OBGroup obj = (OBGroup) objectDict.get("obj");
        PointF destpt = OB_Maths.locationForRect(1.1f,0.7f, obj.frame);
        PointF startpt = pointForDestPoint(destpt,15);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        OBControl dot1 = objectDict.get("dot1");
        OBControl dot2 = objectDict.get("dot2");
        movePointerToPoint(dot1.position(),-20,-1,true);
        waitForSecs(0.2f);
        playAudioScene("DEMO",1,false);
        waitForSecs(0.2f);
        float duration = (float)OBAudioManager.audioManager.duration() - 0.4f;
        OBPath line = (OBPath) objectDict.get("line");
        lockScreen();
        line.setPath(null);
        line.show();
        unlockScreen();
        PointF pt1 = dot1.position();
        PointF pt2 = dot2.position();

        final PointF px1 = pt1;
        final PointF px2 = pt2;
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        PointF ptm = OB_Maths.tPointAlongLine(frac, px1, px2);
                        adjustLineForPt1(px1,px2);
                        thePointer.setPosition(ptm);
                    }
                }
        ),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);

        waitAudio();
        waitForSecs(0.2f);

        movePointerForwards(applyGraphicScale(-40),-1);
        waitForSecs(0.2f);
        playAudioScene("DEMO",2,false);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, obj.frame),-15,-1,true);
        waitForSecs(0.2f);
        waitAudio();
        waitForSecs(0.2f);
        movePointerToPoint(destpt,-15,-1,true);
        foldObject(obj,false);
        waitForSecs(0.2f);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",4,true);
        waitForSecs(0.3f);

        unfoldObject(obj,false);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        nextScene();
    }

    public void endScene() throws Exception
    {
        List aud = currentAudio("FINAL");
        OBGroup obj = (OBGroup) objectDict.get("obj");
        foldObject(obj,true);
        waitForSecs(0.1f);
        displayTick();
        waitForSecs(0.1f);
        playAudioQueued(Arrays.asList(aud.get(0)),true);
        unfoldObject(obj,true);
        waitForSecs(0.1f);
        if(aud.size()  > 1)
            playAudioQueued(Arrays.asList(aud.get(1)),true);
    }

    public void checkFinalTarget(OBControl targ,PointF pt) throws Exception
    {
        setStatus(STATUS_CHECKING);
        emptyReplayAudio();
        endScene();
        waitForSecs(0.9f);
        nextScene();
    }
    public void stage2() throws Exception
    {
        setReplayAudioScene(currentEvent(), "STAGE2.REPEAT");
        playAudioQueuedScene("STAGE2",true);

        targets = filterControls("obj");
        setStatus(STATUS_AWAITING_CLICK2);
    }

    public boolean checkPt(PointF pt,OBControl dot1,OBControl dot2)
    {
        if(OB_Maths.PointDistance(dot2.position() , pt) > 0.5 * OB_Maths.PointDistance(dot1.position() , dot2.position() ))
            return false;
        Path p = new Path();
        p.moveTo(pt.x,pt.y);
        p.lineTo(dot2.position().x,dot2.position().y);
        OBPath path = new OBPath(p);
        path.setLineWidth(applyGraphicScale(3.0f));
        path.setFillColor(0);
        path.setStrokeColor(Color.BLACK);
        path.sizeToBoundingBoxIncludingStroke();
        if(path.intersectsWith(objectDict.get("obj")))
            return false;
        PointF mainVec = OB_Maths.NormalisedVector(OB_Maths.DiffPoints(dot2.position() , dot1.position() ));
        PointF thisVec = OB_Maths.NormalisedVector(OB_Maths.DiffPoints(pt, dot1.position() ));
        double ang = Math.toDegrees(Math.acos(OB_Maths.dot_product(mainVec, thisVec)));
        return(Math.abs(ang) < 5);
    }

    public boolean checkDragPoint(PointF pt)
    {
        if(dotMode == BOTH_DOTS_SHOWING)
            return finger(2,2 ,Arrays.asList(secondDot),pt)  != null;
        else
        {
            return checkPt(pt,firstDot,secondDot);
        }
    }

    public void checkDragAtPoint(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            if(checkDragPoint(pt))
            {
                gotItRightBigTick(false);
                animateLineForPt1(firstDot.position(),secondDot.position(),pt);
                lockScreen();
                hideControls("dot.*");
                unlockScreen();
                waitSFX();
                waitForSecs(0.1f);
                stage2();
            }
            else
            {
                OBPath line = (OBPath) objectDict.get("line");
                gotItWrongWithSfx();
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("strokeEnd",0.0f,line)),
                0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                hideControls("line");
                line.setStrokeEnd(1.0f);
                switchStatus(currentEvent());
                long stt = statusTime;
                waitSFX();
                waitForSecs(0.2f);
                if(stt == statusTime)
                    playAudioQueuedScene("INCORRECT",false);
            }
        }
        catch (Exception e)
        {

        }
    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            adjustLineForPt1(firstDot.position(),pt);
        }
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        List<OBControl> dots = filterControls("dot.");
        int idx = dots.indexOf(targ);
        firstDot = targ;
        secondDot = dots.get((idx + 1)%2);
        OBPath line = (OBPath) objectDict.get("line");
        lockScreen();
        line.setPath(null);
        line.show();
        unlockScreen();
    }

}
