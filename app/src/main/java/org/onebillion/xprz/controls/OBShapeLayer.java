package org.onebillion.xprz.controls;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PixelFormat;
import android.graphics.Rect;

import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 21/04/16.
 */
public class OBShapeLayer extends OBLayer
{
    public Path path;
    public Paint fillPaint,strokePaint;
    public OBStroke stroke;
    public int fillColour;
    float strokeStart,strokeEnd;
    PathMeasure pathMeasure;
    public float currX,currY;

    public OBShapeLayer()
    {
        super();
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        strokePaint.setStyle(Paint.Style.STROKE);
        opacity = 1;
        stroke = new OBStroke();
        strokeStart = 0;
        strokeEnd = 1;
    }

    public OBShapeLayer(Path p)
    {
        this();
        if (p == null)
            path = new Path();
        else
            path = p;
    }

    @Override
    public OBLayer copy()
    {
        OBShapeLayer obj = (OBShapeLayer)super.copy();
        obj.path = new Path(path);
        obj.fillPaint = new Paint(fillPaint);
        obj.strokePaint = new Paint(strokePaint);
        obj.stroke = stroke.copy();
        obj.fillColour = fillColour;
        obj.strokeStart = strokeStart;
        obj.strokeEnd = strokeEnd;
        obj.currX = currX;
        obj.currY = currY;
        return obj;
    }

    @Override
    public void draw(Canvas canvas)
    {
        if (path != null)
        {
            setUpPaint();
            if (fillColour != 0)
                canvas.drawPath(path, fillPaint);
            if (stroke != null)
                canvas.drawPath(path, strokePaint);
        }
    }


    public void setUpPaint()
    {
        if (stroke != null)
        {
            strokePaint.setStrokeWidth(stroke.lineWidth);
            int col = OB_utils.applyColourOpacity(stroke.colour,opacity);
            strokePaint.setColor(col);
            strokePaint.setStrokeCap(stroke.paintLineCap());
            strokePaint.setStrokeJoin(stroke.paintLineJoin());
            DashPathEffect dpe = stroke.dashPathEffect();
            if (dpe == null)
                strokePaint.setPathEffect(null);
            else
                strokePaint.setPathEffect(dpe);
            strokePaint.setStyle(Paint.Style.STROKE);
        }
        if (fillColour != 0)
        {
            int col = OB_utils.applyColourOpacity(fillColour,opacity);
            fillPaint.setColor(col);
            //fillPaint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
            fillPaint.setStyle(Paint.Style.FILL);
        }
    }

    public PathMeasure pathMeasure()
    {
        if (pathMeasure == null)
            pathMeasure = new PathMeasure(path,false);
        return pathMeasure;
    }

    public float length()
    {
        return pathMeasure().getLength();
    }

    public float strokeEnd()
    {
        return strokeEnd;
    }

    public void setStrokeEnd(float f)
    {
        strokeEnd = OB_Maths.clamp01(f);
        float len = length();
        if (strokeEnd < 1)
        {
            List<Float> lst = new ArrayList<>(2);
            lst.add(strokeEnd * len);
            lst.add(32767f);
            stroke.dashes = lst;
        }
        else
            stroke.dashes = null;
    }

    public void moveToPoint(float x,float y)
    {
        path.moveTo(currX = x,currY = y);
    }

    public void addLineToPoint(float x,float y)
    {
        path.lineTo(currX = x,currY = y);
    }

}
