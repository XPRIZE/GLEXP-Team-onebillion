package org.onebillion.xprz.controls;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;

import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

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
    public float currX,currY;
    float strokeStart,strokeEnd;
    PathMeasure pathMeasure;

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
            strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            strokePaint.setStrokeWidth(stroke.lineWidth);
            //int col = OBUtils.applyColourOpacity(stroke.colour,opacity);
            strokePaint.setColor(stroke.colour);
            strokePaint.setStrokeCap(stroke.paintLineCap());
            strokePaint.setStrokeJoin(stroke.paintLineJoin());
            DashPathEffect dpe = stroke.dashPathEffect();
            if (dpe == null)
                strokePaint.setPathEffect(null);
            else
                strokePaint.setPathEffect(dpe);
            strokePaint.setStyle(Paint.Style.STROKE);
            if (colourFilter == null)
                strokePaint.setColorFilter(null);
            else
                strokePaint.setColorFilter(colourFilter);

        }
        if (fillColour != 0)
        {
            fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            //int col = OBUtils.applyColourOpacity(fillColour,opacity);
            fillPaint.setColor(fillColour);
            //fillPaint.setMaskFilter(new BlurMaskFilter(7, BlurMaskFilter.Blur.NORMAL));
            fillPaint.setStyle(Paint.Style.FILL);
            if (colourFilter == null)
                fillPaint.setColorFilter(null);
            else
                fillPaint.setColorFilter(colourFilter);
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
        setupStrokeDashes();
    }

    public void setStrokeStart(float f)
    {
        strokeStart = OB_Maths.clamp01(f);
        setupStrokeDashes();
    }

    private void setupStrokeDashes()
    {
        float len = length();
        if (strokeEnd < 1 || strokeStart > 0)
        {
            List<Float> lst = new ArrayList<>(4);
            lst.add(0f);
            lst.add(strokeStart * len);
            lst.add(OB_Maths.clamp01(strokeEnd-strokeStart) * len);
            lst.add(32767f);
            stroke.setDashes(lst);
        }
        else
            stroke.setDashes(null);
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
