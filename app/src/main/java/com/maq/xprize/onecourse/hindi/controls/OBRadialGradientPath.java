package com.maq.xprize.onecourse.hindi.controls;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.URadialGradient;

import java.util.List;
import java.util.Map;

/**
 * Created by alan on 24/04/16.
 */
public class OBRadialGradientPath extends OBGradientPath
{
    public OBRadialGradientPath()
    {
        super();
        gradientLayer = new OBRadialGradientLayer();

    }
    public OBRadialGradientPath(Path p)
    {
        super(p);
        gradientLayer = new OBRadialGradientLayer();
        adjustLayers();
    }

    public OBRadialGradientPath(Path p,float cx,float cy,float radius,int ccol,int ecol)
    {
        super(p);
        OBRadialGradientLayer gl = new OBRadialGradientLayer();
        gl.cx = cx;
        gl.cy = cy;
        gl.radius = radius;
        int cols[] = new int[2];
        cols[0] = ccol;
        cols[1] = ecol;
        gl.colours = cols;
        gradientLayer = gl;
        adjustLayers();
    }


    public void takeValuesFrom(URadialGradient ugradient, List<Map<String,Object>> settingsStack)
    {
        Map<String, Object> settings = settingsStack.get(settingsStack.size() - 1);
        RectF originalbounds = (RectF) settings.get("originalbounds");
        PointF p1;
        float radius;
        if (ugradient.useBboxUnits)
        {
            p1 = OB_Maths.locationForRect(ugradient.cx, ugradient.cy, originalbounds);
            radius = ugradient.r * originalbounds.width();
        }
        else
        {
            p1 = new PointF(ugradient.cx, ugradient.cy);
            radius = ugradient.r;
        }
        Matrix m = (Matrix) settings.get("transform");
        float s[] = new float[2],d[] = new float[2];
        if (m != null)
        {
            s[0] = p1.x;s[1] = p1.y;
            m.mapPoints(d,s);
            p1.set(d[0],d[1]);
        }
        if (!(ugradient.transform.isIdentity()))
        {
            s[0] = p1.x;s[1] = p1.y;
            ugradient.transform.mapPoints(d,s);
            p1.set(d[0],d[1]);
        }
        PointF startPoint = OB_Maths.relativePointInRectForLocation(p1, frame);
        OBRadialGradientLayer rgl = (OBRadialGradientLayer) gradientLayer;
        rgl.cx = startPoint.x;
        rgl.cy = startPoint.y;
        rgl.radius = radius;
        setStops(ugradient.stops);

    }

}
