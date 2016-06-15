package org.onebillion.xprz.utils;

import android.graphics.Matrix;

import java.util.List;

/**
 * Created by alan on 28/11/15.
 */
public class URadialGradient
{
    public float cx,cy,r,fx,fy;
    public boolean useBboxUnits;
    public Matrix transform;
    public String spreadMethod;
    public List<List<Object>> stops;
    public URadialGradient()
    {
        useBboxUnits = true;
        cx = cy = r = fx = fy = 0.5f;
        transform = new Matrix();
        spreadMethod = "pad";
    }
}
