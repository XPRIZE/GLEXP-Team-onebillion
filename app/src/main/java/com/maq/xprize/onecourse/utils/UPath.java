package com.maq.xprize.onecourse.utils;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 06/02/16.
 */
public class UPath
{
    public List<USubPath> subPaths;
    float length;

    public UPath()
    {
        length = -1;
        subPaths = new ArrayList<>();
    }

    public float length()
    {
        if (length < 0)
            calcLength();
        return length;
    }

    public void calcLength()
    {
        length = 0;
        for (USubPath sp : subPaths)
            length += sp.length();
    }

    public Path bezierPath()
    {
        Path bez = new Path();
        for (USubPath sp : subPaths)
            bez.addPath(sp.bezierPath());
        return bez;
    }

    public void transformByMatrix(Matrix t)
    {
        for (USubPath usp : subPaths)
            usp.transformByMatrix(t);
    }

    public USubPath convexHull()
    {
        List<PointF>points = new ArrayList<>();
        for(USubPath usp : subPaths )
        {
            if(usp.elements.size()  > 0)
            {
                ULine ulx = usp.elements.get(0);
                points.add(ulx.pt0);
                for(ULine ul : usp.elements)
                {
                    points.add(ul.pt1);
                    if(ul instanceof UCurve)
                    {
                        UCurve uc =(UCurve)ul;
                        if(!(uc.cp0.equals(ul.pt0)))
                            points.add(uc.cp0);
                        if(!(uc.cp1.equals(ul.pt1)))
                            points.add(uc.cp1);
                    }
                }
            }
        }
        List<PointF> hull = OBUtils.convexHullFromPoints(points);
        return USubPath.uSubPathFromPoints(hull);
    }

}
