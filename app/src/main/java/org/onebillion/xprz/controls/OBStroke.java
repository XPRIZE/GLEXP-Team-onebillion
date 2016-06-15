package org.onebillion.xprz.controls;

import java.util.*;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import org.onebillion.xprz.utils.OB_utils;

public class OBStroke
{
    public static int     kCGLineCapButt=0,
            kCGLineCapRound=1,
            kCGLineCapSquare=2,
            kCALineJoinMiter = 0,
            kCALineJoinRound = 1,
            kCALineJoinBevel = 2;

    public int colour;
    public List<Float>dashes;
    public float lineWidth,dashPhase;
    public int lineCap,lineJoin;


    public OBStroke()
    {
        super();
    }

    public OBStroke copy()
    {
        OBStroke obj = new OBStroke();
        obj.colour = colour;
        obj.dashes = dashes;
        obj.lineWidth = lineWidth;
        obj.dashPhase = dashPhase;
        obj.lineCap = lineCap;
        obj.lineJoin = lineJoin;
        return obj;
    }
    public OBStroke(Map<String, String> attrs)
    {
        this();
        if ((attrs.get("stroke")==null) && (attrs.get("stroke-opacity")==null) && (attrs.get("stroke-linecap")==null) && (attrs.get("stroke-linejoin")==null) && (attrs.get("stroke-width")==null) && (attrs.get("stroke-miterlimit")==null) && (attrs.get("stroke-dasharray")==null) && (attrs.get("stroke-dashoffset")==null))
            return;
        String str = attrs.get("stroke");
        if (str != null)
            colour = OB_utils.svgColorFromRGBString(str);
        else
            colour = Color.BLACK;
        float opacity = 1.0f;
        String n = attrs.get("stroke-opacity");
        if (n != null)
            opacity = Float.parseFloat(n);
        colour = (colour & 0xFFFFFF);
        colour = colour | (((int)(opacity*255))<<24);
        lineWidth = 1.0f;
        n = attrs.get("stroke-width");
        if (n!=null)
            lineWidth = Float.parseFloat(n);
        String lc = attrs.get("stroke-linecap");
        if (lc != null)
        {
            if (lc.equals("butt"))
                lineCap= kCGLineCapButt;
            else if (lc.equals("round"))
                lineCap =kCGLineCapRound;
            else if (lc.equals("square"))
                lineCap = kCGLineCapSquare;
        }
        String lj = attrs.get("stroke-linejoin");
        if (lj != null)
        {
            if (lj.equals("miter"))
                lineJoin= kCALineJoinMiter;
            else if (lj.equals("round"))
                lineJoin =kCALineJoinRound;
            else if (lj.equals("bevel"))
                lineJoin = kCALineJoinBevel;
        }
        n = attrs.get("stroke-dashoffset");
        if (n!=null)
            dashPhase = Float.parseFloat(n);
        n = attrs.get("stroke-dasharray");
        if (n!=null)
        {
            String[]arr = n.split("[ ,]+", 0);
            dashes = new ArrayList<>();
            for (String s : arr)
            {
                s = s.trim();
                if (s.length() > 0)
                    dashes.add(Float.parseFloat(s));
            }
            if ((dashes.size() & 1) != 0)
            {
                dashes.addAll(dashes);
            }
            if (dashes.size() == 0)
                dashes = null;
        }
    }
    public OBStroke(Map<String, Object> attrs,boolean progBased)//non-svg
    {
        if ((attrs.get("stroke")==null) && (attrs.get("stroke-opacity")==null) && (attrs.get("stroke-linecap")==null) && (attrs.get("stroke-linejoin")==null) && (attrs.get("stroke-width")==null) && (attrs.get("stroke-miterlimit")==null) && (attrs.get("stroke-dasharray")==null) && (attrs.get("stroke-dashoffset")==null))
            return;
        String str = (String)attrs.get("stroke");
        if (str != null)
            colour = OB_utils.colorFromRGBString(str);
        else
            colour = Color.BLACK;
        float opacity = 1.0f;
        String n = (String)attrs.get("strokeopacity");
        if (n != null)
            opacity = Float.parseFloat(n);
        colour = colour | (((int)(opacity*255))<<24);
        lineWidth = 1.0f;
        n = (String)attrs.get("strokewidth");
        if (n!=null)
            lineWidth = Float.parseFloat(n);
        String lc = (String)attrs.get("linecap");
        if (lc != null)
        {
            if (lc.equals("butt"))
                lineCap= kCGLineCapButt;
            else if (lc.equals("round"))
                lineCap =kCGLineCapRound;
            else if (lc.equals("square"))
                lineCap = kCGLineCapSquare;
        }
        String lj = (String)attrs.get("linejoin");
        if (lj != null)
        {
            if (lj.equals("miter"))
                lineJoin= kCALineJoinMiter;
            else if (lj.equals("round"))
                lineJoin =kCALineJoinRound;
            else if (lj.equals("bevel"))
                lineJoin = kCALineJoinBevel;
        }
        String da = (String)attrs.get("stroke-dasharray");
        if (da != null)
        {
            String[] arr = da.split(",");
            dashes = new ArrayList<Float>();
            for (String s : arr)
                dashes.add(Float.parseFloat(s));
        }
        String sd = (String)attrs.get("stroke-dashoffset");
        if (sd != null)
            dashPhase = Float.parseFloat(sd);
    }

    public Paint.Cap paintLineCap()
    {
        if (lineCap == kCGLineCapButt)
            return Paint.Cap.BUTT;
        if (lineCap == kCGLineCapRound)
            return Paint.Cap.ROUND;
        return Paint.Cap.SQUARE;
    }

    public Paint.Join paintLineJoin()
    {
        if (lineJoin == kCALineJoinMiter)
            return Paint.Join.MITER;
        if (lineJoin == kCALineJoinRound)
            return Paint.Join.ROUND;
        return Paint.Join.BEVEL;
    }

    public DashPathEffect dashPathEffect()
    {
        if (dashes == null || dashes.size() == 0)
            return null;
        float dshes[] = new float[dashes.size()];
        for (int i = 0;i < dashes.size();i++)
            dshes[i] = dashes.get(i);
        return new DashPathEffect(dshes,dashPhase);
    }

    public void setLineJoin(int lj)
    {
        lineJoin = lj;
    }
}
