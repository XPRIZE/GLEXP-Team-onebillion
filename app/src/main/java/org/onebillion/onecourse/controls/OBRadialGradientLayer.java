package org.onebillion.onecourse.controls;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import org.onebillion.onecourse.utils.OB_Maths;

/**
 * Created by alan on 24/04/16.
 */
public class OBRadialGradientLayer extends OBGradientLayer
{
    public float cx=0.5f,cy=0.5f,radius=1;
    public OBRadialGradientLayer()
    {
        super();

    }

    @Override
    public OBLayer copy()
    {
        OBRadialGradientLayer obj = (OBRadialGradientLayer)super.copy();
        obj.cx = cx;
        obj.cy = cy;
        obj.radius = radius;
        return obj;
    }

    public void draw(Canvas canvas)
    {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PointF cs = OB_Maths.locationForRect(cx,cy,bounds());
        RadialGradient rg = new RadialGradient(cs.x,cs.y,radius,colours,locations, Shader.TileMode.CLAMP);
        p.setShader(rg);
        canvas.drawRect(bounds(),p);
    }

}
