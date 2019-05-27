package com.maq.xprize.onecourse.controls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;

import com.maq.xprize.onecourse.utils.OB_Maths;
import com.maq.xprize.onecourse.utils.UGradient;

import java.util.List;
import java.util.Map;

/**
 * Created by alan on 23/04/16.
 */
public class OBGradientPath extends OBPath
{
    public int flags;
    public OBShapeLayer strokeLayer,maskLayer;
    public OBGradientLayer gradientLayer;
    public OBGradientPath()
    {
        super(null);
        strokeLayer = new OBShapeLayer();
        maskLayer = new OBShapeLayer();
        gradientLayer = new OBGradientLayer();
    }

    public OBGradientPath(Path p)
    {
        super(p);
        strokeLayer = new OBShapeLayer(p);
        maskLayer = new OBShapeLayer(p);
        gradientLayer = new OBGradientLayer();
        adjustLayers();
    }

    @Override
    public OBControl copy()
    {
        OBGradientPath obj = (OBGradientPath)super.copy();
        obj.strokeLayer = (OBShapeLayer)strokeLayer.copy();
        obj.maskLayer = (OBShapeLayer)maskLayer.copy();
        obj.gradientLayer = (OBGradientLayer)gradientLayer.copy();
        return obj;
    }

    public void adjustLayers()
    {
        gradientLayer.setBounds(layer.bounds());
        strokeLayer.setBounds(layer.bounds());
        maskLayer.setBounds(layer.bounds());
    }
    public void setStops(List<List<Object>> l)
    {
        int ct = l.size();
        int colours[] = new int[ct];
        float locations[] = new float[ct];
        for (int i = 0;i < ct;i++)
        {
            List<Object>lo = l.get(i);
            int col = (Integer)lo.get(0);
            colours[i] = col;
            float loc = (Float)lo.get(1);
            locations[i] = loc;
        }
        gradientLayer.colours = colours;
        gradientLayer.locations = locations;
    }
    public OBShapeLayer shapeLayer()
    {
        return strokeLayer;
    }

    public void setFrame(RectF r)
    {
        super.setFrame(r);
        adjustLayers();
    }

    public void setPath(Path path)
    {
        shapeLayer().path = path;
        maskLayer.path = path;
        maskLayer.fillColour = Color.BLACK;
    }

    public void takeValuesFrom(UGradient ugradient)
    {
        gradientLayer.startx = ugradient.x1;
        gradientLayer.starty = ugradient.y1;
        gradientLayer.endx = ugradient.x2;
        gradientLayer.endy = ugradient.y2;
        setStops(ugradient.stops);
    }

    public void takeValuesFrom(UGradient ugradient,List<Map<String,Object>>settingsStack)
    {
        Map<String,Object> settings = settingsStack.get(settingsStack.size()-1);
        RectF originalbounds = (RectF) settings.get("originalbounds");
        PointF p1,p2;
        if (ugradient.useBboxUnits)
        {
            p1 = OB_Maths.locationForRect(ugradient.x1, ugradient.y1, originalbounds);
            p2 = OB_Maths.locationForRect(ugradient.x2, ugradient.y2, originalbounds);
        }
        else
        {
            p1 = new PointF(ugradient.x1, ugradient.y1);
            p2 = new PointF(ugradient.x2, ugradient.y2);
        }
        Matrix m = (Matrix) settings.get("transform");
        float s[] = new float[4],d[] = new float[4];
        if (m != null)
        {
            s[0] = p1.x;s[1] = p1.y;
            s[2] = p2.x;s[3] = p2.y;
            m.mapPoints(d,s);
            p1.set(d[0],d[1]);
            p2.set(d[2],d[3]);
        }
        if (!(ugradient.transform.isIdentity()))
        {
            s[0] = p1.x;s[1] = p1.y;
            s[2] = p2.x;s[3] = p2.y;
            ugradient.transform.mapPoints(d,s);
            p1.set(d[0],d[1]);
            p2.set(d[2],d[3]);
        }
        PointF startPoint = OB_Maths.relativePointInRectForLocation(p1, frame);
        gradientLayer.startx = startPoint.x;
        gradientLayer.starty = startPoint.y;
        PointF endPoint = OB_Maths.relativePointInRectForLocation(p2, frame);
        gradientLayer.endx = endPoint.x;
        gradientLayer.endy = endPoint.y;
        setStops(ugradient.stops);
    }

    public void drawLayer(Canvas canvas, int flags)
    {
        if (gradientLayer != null)
        {
            boolean needsRestore = false;
            canvas.save();
            if (highlightColour != 0)
            {
                gradientLayer.setColourFilter(new PorterDuffColorFilter(highlightColour, PorterDuff.Mode.SRC_ATOP));
            }
            if (needsRestore = (opacity() != 1.0f))
                canvas.saveLayerAlpha(bounds(), (int) (opacity() * 255));
            if (maskLayer != null)
                canvas.clipPath(maskLayer.path);
            gradientLayer.draw(canvas);
            if (strokeLayer != null)
                strokeLayer.draw(canvas);

            if (needsRestore)
                canvas.restore();
            canvas.restore();
        }
    }
}
