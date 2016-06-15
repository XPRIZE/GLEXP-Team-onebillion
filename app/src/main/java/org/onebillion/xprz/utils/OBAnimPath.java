package org.onebillion.xprz.utils;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;

import java.util.Arrays;

/**
 * Created by alan on 07/06/16.
 */
public class OBAnimPath extends OBAnim
{
    float angleOffset;
    boolean changeAngle;
    Path path;
    PathMeasure pathMeasure;
    float pathLength;
    public OBAnimPath(Object obj, String ky, int ty)
    {
        super(obj, ky, ty);
    }
    public OBAnimPath(Object  obj,Path p,boolean changle,float radians)
    {
        this(obj,"position",ANIM_TYPE_POINT);
        path = p;
        pathMeasure = new PathMeasure(p,false);
        pathLength = pathMeasure.getLength();
        changeAngle = changle;
        angleOffset = radians;
    }

    public Object valueForT(float t)
    {
        float thisLen = t * pathLength;
        float pos[] = {0,0},ftan[] = {0,0};
        pathMeasure.getPosTan(thisLen,pos,ftan);
        PointF vpt = new PointF(pos[0],pos[1]);
        if (changeAngle)
        {
            float radians = (float)Math.atan2(ftan[1],ftan[0]);
            radians += angleOffset;
            return Arrays.asList(vpt,radians);
        }
        return vpt;
    }
}
