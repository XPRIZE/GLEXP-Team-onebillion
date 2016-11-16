package org.onebillion.onecourse.utils;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * Created by alan on 01/02/16.
 */
public class ULine
{
    float length,proportionalLength,spStartT,spEndT;;
    public PointF pt0,pt1;
    public ULine(float x0,float y0,float x1,float y1)
    {
        super();
        pt0 = new PointF(x0,y0);
        pt1 = new PointF(x1,y1);
        length = -1;
    }

    public ULine copy()
    {
        ULine obj = new ULine(pt0.x,pt0.y,pt1.x,pt1.y);
        obj.length = length;
        obj.proportionalLength = proportionalLength;
        obj.spStartT = spStartT;
        obj.spEndT = spEndT;
        return obj;
    }

    public float length()
    {
        if (length < 0.0)
            calculateLength();
        return length;
    }

    void calculateLength()
    {
        length = OB_Maths.PointDistance(pt0,pt1);
    }

    public PointF tAlongt(float t,PointF outvec)
    {
        PointF pt = OB_Maths.tPointAlongLine(t, pt0, pt1);
        if (outvec != null)
            outvec.set(OB_Maths.DiffPoints(pt1, pt0));
        return pt;
    }

    public PointF sAlongS(float s,PointF outvec)
    {
        float t = tForS(s);
        return tAlongt(t,outvec);
    }

    public boolean nearestPointTestPt(PointF testpt,OB_MutFloat t,PointF hitpoint,
                                      OB_MutFloat distance, float threshold)
    {
        return OB_Maths.nearestPointOnLine(pt0, pt1, testpt, t, hitpoint, distance, threshold);
    }

    public float sForT(float t)
    {
        return t;
    }

    public float tForS(float s)
    {
        return s;
    }

    ULine objectFromMinT(double minT,double maxT)
    {
        PointF p0 = OB_Maths.tPointAlongLine((float)minT, pt0, pt1);
        PointF p1 = OB_Maths.tPointAlongLine((float)maxT, pt0, pt1);
        return new ULine(p0.x,p0.y,p1.x,p1.y);
    }

    public void transformByMatrix(Matrix t)
    {
        float ps[] = new float[4];
        ps[0] = pt0.x;
        ps[1] = pt0.y;
        ps[2] = pt1.x;
        ps[3] = pt1.y;
        t.mapPoints(ps);
        pt0.x = ps[0];
        pt0.y = ps[1];
        pt1.x = ps[2];
        pt1.y = ps[3];
    }

}
