package org.onebillion.xprz.mainui.x_counting7to10;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBShapeLayer;
import org.onebillion.xprz.controls.OBStroke;
import org.onebillion.xprz.controls.OBTextLayer;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Event;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class X_Counting7To10_S5 extends XPRZ_Generic_Event
{
    OBPath drawingSurface;
    List<PointF> points;
    //
    int number;
    OBControl correct_object;
    OBLabel label;


    public X_Counting7To10_S5 ()
    {
        super();
    }


    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
        //
        if (label != null)
        {
            detachControl(label);
        }
        number = Integer.parseInt(eventAttributes.get("number"));
        correct_object = objectDict.get(String.format("obj_%s", eventAttributes.get("correct_object")));
        //
        OBControl title = (OBControl) objectDict.get("title");
        if (title != null)
        {
            Typeface tf = OBUtils.standardTypeFace();
            label = new OBLabel(String.format("%d", number), tf, applyGraphicScale(150));
            label.setColour(Color.BLACK);
            label.setPosition(title.position());
            label.setZPosition(XPRZ_Generic.getNextZPosition(this));
            label.texturise(false, this);
            attachControl(label);
            //
            title.hide();
        }
        //
        for (OBControl control : filterControls("obj.*"))
        {
            OBGroup group = (OBGroup) control;
            OBPath rectangle = (OBPath) group.filterMembers("Rectangle15.*").get(0);
            float factor = - 0.05f;
            RectF rect = new RectF(rectangle.frame());
            rect.inset(factor * rectangle.width(), factor * rectangle.height());
            rectangle.sizeToBox(rect);
            group.sizeToMember(rectangle);
            rectangle.setNeedsRetexture();
        }
        //
        XPRZ_Generic.colourObjectsWithScheme(this);
        points = new ArrayList<PointF>();
        //
        hideControls("draw");
        if (drawingSurface != null) detachControl(drawingSurface);
    }


    public Boolean action_intersectsCorrectly ()
    {
        if (drawingSurface == null) return false;
        RectF frame = correct_object.frame();
        //
        Boolean correctIntersection = false;
        for (PointF pt : points)
        {
            correctIntersection = correctIntersection || frame.contains(pt.x, pt.y);
        }
        //
        for (OBControl control : filterControls("obj.*"))
        {
            Boolean controlIntersects = drawingSurface.intersectsWith(control);
            if (correct_object.equals(control))
            {
                if (!controlIntersects && !correctIntersection) return false;
            }
            else if (controlIntersects) return false;
        }
        return correctIntersection;
    }



    // DEMOS

    public void demo5a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        XPRZ_Generic.pointer_moveToObject(label, 0, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Five.
        waitForSecs(0.3);
        //
        for (int i = 1; i <= 3; i++)
        {
            XPRZ_Generic.pointer_moveToObjectByName(String.format("obj_%d", i), -25, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
            action_playNextDemoSentence(true); // Five counters. Five Counter. Six Counter.
            waitForSecs(0.3);
        }
        //
        drawingSurface = (OBPath) objectDict.get("draw");
        PointF outvec = new PointF();
        PointF startPoint = convertPointFromControl(drawingSurface.sAlongPath(0.0f, outvec), drawingSurface);
        action_playNextDemoSentence(false); // This set does not match the number.
        movePointerToPoint(startPoint, -10, 0.4f, true);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // So cross it out.
        long starttime = SystemClock.uptimeMillis();
        float duration = drawingSurface.length() * 2 * 1.0f / theMoveSpeed;
        float frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (float) (currtime - starttime) / (duration * 1000);
            final float t = (frac);
            new OBRunnableSyncUI()
            {
                public void ex ()
                {
                    drawingSurface.show();
                    drawingSurface.setStrokeEnd(t);
                    thePointer.setPosition(convertPointFromControl(drawingSurface.sAlongPath(t, null), drawingSurface));
                    thePointer.setRotation((float) Math.toRadians((1.0 - t) * -15.0f));
                    drawingSurface.setNeedsRetexture();
                }
            }.run();
            waitForSecs(0.02f);
        }
        waitAudio();
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    // CHECKS AND TOUCHES


    public void checkTrace ()
    {
        saveStatusClearReplayAudioSetChecking();
        //
        try
        {
            if (action_intersectsCorrectly())
            {
                gotItRightBigTick(true);
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "CORRECT", true);

                //
                if (audioSceneExists("FINAL"))
                {
                    waitForSecs(0.3);
                    playAudioQueuedScene(currentEvent(), "FINAL", true);
                }
                else
                {
                    waitForSecs(0.7);
                }
                nextScene();
            }
            else
            {
                if (drawingSurface != null) drawingSurface.hide();
                //
                gotItWrongWithSfx();
                waitForSecs(0.3);
                //
                playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                //
                revertStatusAndReplayAudio();
            }
        }
        catch (Exception e)
        {
            System.out.println("X_Counting7To10_S5.exception caught: " + e.toString());
            e.printStackTrace();
        }
    }





    public void checkTraceStart (PointF pt)
    {
        setStatus(STATUS_CHECKING);
        //
        lockScreen();
        OBPath template = (OBPath) objectDict.get("draw");
        //
        if (drawingSurface != null) detachControl(drawingSurface);

        drawingSurface = new OBPath(new Path(), bounds().width(), bounds().height(), bounds().width() / 2.0f, bounds().height() / 2.0f);
        drawingSurface.setLineCap(OBStroke.kCGLineCapRound);
        drawingSurface.setLineJoin(OBStroke.kCALineJoinRound);
        drawingSurface.setLineWidth(applyGraphicScale(template.lineWidth()));
        drawingSurface.setFillColor(template.fillColor());
        drawingSurface.setStrokeColor(template.strokeColor());
        drawingSurface.setZPosition(40);
        //
        PointF cpt = convertPointToControl(pt, drawingSurface);
        drawingSurface.shapeLayer().path.reset();
        drawingSurface.shapeLayer().path.moveTo(cpt.x, cpt.y);
        drawingSurface.shapeLayer().path.lineTo(cpt.x + 1, cpt.y);
        //
        points.clear();
        points.add(new PointF(cpt.x, cpt.y));
        //
        drawingSurface.show();
        attachControl(drawingSurface);
        unlockScreen();
        //
        setStatus(STATUS_TRACING);
    }




    public void touchUpAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_TRACING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    checkTrace();
                }
            });
        }
        else
        {
            setStatus(STATUS_WAITING_FOR_TRACE);
        }
    }




    public void touchMovedToPoint (PointF pt, View v)
    {
        if (status() == STATUS_TRACING)
        {
            if (drawingSurface != null)
            {
                lockScreen();
                PointF cpt = convertPointToControl(pt, drawingSurface);
                drawingSurface.addLineToPoint(cpt.x, cpt.y);
                unlockScreen();
                //
                points.add(new PointF(cpt.x, cpt.y));
            }
        }
    }




    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK || status() == STATUS_WAITING_FOR_TRACE)
        {
            checkTraceStart(pt);

        }
    }


}
