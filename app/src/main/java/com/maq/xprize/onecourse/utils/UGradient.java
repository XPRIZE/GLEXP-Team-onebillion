package com.maq.xprize.onecourse.utils;

import android.graphics.Matrix;

import java.util.List;

/**
 * Created by alan on 24/11/15.
 */
public class UGradient
{
    public float x1,y1,x2,y2;
    public boolean useBboxUnits;
    public String spreadMethod;
    public Matrix transform;
    public List<List<Object>> stops;
    public UGradient()
    {
        useBboxUnits = true;
        x1 = y1 = y2 = 0;
        x2 = 1;
        transform = new Matrix();
        spreadMethod = "pad";
    }
}
