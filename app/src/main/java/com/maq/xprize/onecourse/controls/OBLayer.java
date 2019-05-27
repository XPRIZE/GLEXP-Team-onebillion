package com.maq.xprize.onecourse.controls;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;

import java.lang.reflect.Constructor;

/**
 * Created by alan on 21/04/16.
 */
public class OBLayer
{
    float opacity = 1.0f;
    RectF bounds = new RectF();
    ColorFilter colourFilter;
    Bitmap contents;

    public OBLayer copy()
    {
        OBLayer obj;
        try
        {
            Constructor<?> cons;
            cons = getClass().getConstructor();
            obj = (OBLayer)cons.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        obj.opacity = opacity;
        obj.bounds.set(bounds);
        obj.colourFilter = colourFilter;
        obj.contents = contents;
        return obj;
    }
    public RectF bounds()
    {
        return bounds;
    }

    public void setBounds(RectF bounds)
    {
        this.bounds.set(bounds);
    }
    public void setBounds(float l,float t,float r,float b)
    {
        this.bounds.set(l,t,r,b);
    }

    public void draw(Canvas canvas)
    {
        if (contents != null)
        {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (colourFilter != null)
                p.setColorFilter(colourFilter);
            canvas.drawBitmap(contents,0,0,p);
        }
    }

    public float opacity()
    {
        return opacity;
    }

    public void setOpacity(float opacity)
    {
        this.opacity = opacity;
    }

    public Bitmap getContents()
    {
        return contents;
    }

    public void setContents(Bitmap contents)
    {
        this.contents = contents;
    }

    public ColorFilter colourFilter()
    {
        return colourFilter;
    }

    public void setColourFilter(ColorFilter colourFilter)
    {
        this.colourFilter = colourFilter;
    }
}
