package com.maq.xprize.onecourse.controls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;

import com.maq.xprize.onecourse.utils.OB_Maths;

/**
 * Created by alan on 23/04/16.
 */
public class OBGradientLayer extends OBLayer
{
    public int colours[];
    public float locations[];
    public float startx=0.5f,starty=0,endx=0.5f,endy=1;


    public OBGradientLayer()
    {
        colours =  new int[2];
        colours[0] = Color.WHITE;
        colours[1] = Color.BLACK;
        locations = new float[2];
        locations[0] = 0;
        locations[1] = 1;
    }
    @Override
    public OBLayer copy()
    {
        OBGradientLayer obj = (OBGradientLayer)super.copy();
        obj.colours = colours.clone();
        obj.locations = locations.clone();
        obj.startx = startx;
        obj.starty = starty;
        obj.endx = endx;
        obj.endy = endy;
        return obj;
    }
    public void draw(Canvas canvas)
    {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        PointF ps = OB_Maths.locationForRect(startx,starty,bounds());
        PointF pe = OB_Maths.locationForRect(endx,endy,bounds());
        LinearGradient lg = new LinearGradient(ps.x,ps.y,pe.x,pe.y,colours,locations, Shader.TileMode.CLAMP);
        p.setShader(lg);
        canvas.drawRect(bounds(),p);
    }
}
