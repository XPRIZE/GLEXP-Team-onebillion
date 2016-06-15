package org.onebillion.xprz.utils;

import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;

/**
 * Created by alan on 31/12/15.
 */
public class OBAnimParabola extends OBAnim
{
    float minY,maxY,fromX,toX,fromT,toT;
    PointF temppt;
    public OBAnimParabola(float _minY,float _maxY,float _fromX,float _toX,float _fromT,float _toT,OBControl obj)
    {
        super(obj,"position",ANIM_TYPE_POINT);
        minY = _minY;
        maxY = _maxY;
        fromX = _fromX;
        toX = _toX;
        fromT = _fromT;
        toT = _toT;
        temppt = new PointF();
    }

    public Object valueForT(float t)
    {
        float x = OB_Maths.interpolateVal(fromX, toX, t);
        float ft = OB_Maths.interpolateVal(fromT, toT, t);
        float y = ft * ft;
        y = OB_Maths.interpolateVal(minY, maxY, y);
        temppt.x = x;
        temppt.y = y;
        return temppt;
    }
}
