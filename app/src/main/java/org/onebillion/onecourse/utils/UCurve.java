package org.onebillion.onecourse.utils;

import android.graphics.Matrix;
import android.graphics.PointF;

/**
 * Created by alan on 06/02/16.
 */
public class UCurve extends ULine
{
    public PointF cp0,cp1;
    public UCurve(float x0, float y0, float x1, float y1,float cx0,float cy0,float cx1,float cy1)
    {
        super(x0, y0, x1, y1);
        cp0 = new PointF(cx0,cy0);
        cp1 = new PointF(cx1,cy1);
    }


    public ULine objectFromMinT(double minT,double maxT)
    {
        PointF c1EndPt = new PointF(),c1CP0 = new PointF(),c1CP1 = new PointF(),c2CP0 = new PointF(),c2CP1 = new PointF(),endPt = new PointF();
        OB_Maths.splitCurveByT(pt0,pt1,cp0,cp1,(float)minT,c1EndPt,c1CP0,c1CP1,c2CP0,c2CP1);
        if (maxT >= 1.0)
            return new UCurve(c1EndPt.x,c1EndPt.y,pt1.x,pt1.y,c2CP0.x,c2CP0.y,c2CP1.x,c2CP1.y);
        float tempT = (float)((maxT - minT) / (1.0 - minT));
        OB_Maths.splitCurveByT(c1EndPt,pt1, c2CP0, c2CP1, tempT,endPt,c1CP0,c1CP1,c2CP0,c2CP1);
        return new UCurve(c1EndPt.x,c1EndPt.y,endPt.x,endPt.y,c1CP0.x,c1CP0.y,c1CP1.x,c1CP1.y);
    }

    public UCurve copy()
    {
        UCurve obj = new UCurve(pt0.x,pt0.y,pt1.x,pt1.y,cp0.x,cp0.y,cp1.x,cp1.y);
        obj.length = length;
        obj.proportionalLength = proportionalLength;
        obj.spStartT = spStartT;
        obj.spEndT = spEndT;
        return obj;
    }


    public void calculateLength()
    {
        length = OB_Maths.curveLength(pt0, pt1, cp0, cp1);
    }

    public PointF tAlongt(float t,PointF outvec)
    {
        PointF b10 ,b11,b12,b20,b21,b30,d = new PointF();
        b10 = OB_Maths.tPointAlongLine(t, pt0, cp0);
        b11 = OB_Maths.tPointAlongLine(t, cp0, cp1);
        b12 = OB_Maths.tPointAlongLine(t, cp1, pt1);
        b20 = OB_Maths.tPointAlongLine(t, b10, b11);
        b21 = OB_Maths.tPointAlongLine(t, b11, b12);
        b30 = OB_Maths.tPointAlongLine(t, b20, b21);
        d.x = b21.x - b20.x;
        d.y = b21.y - b20.y;
        if (outvec != null)
        {
            if (Math.abs(d.x) < 0.0001 && Math.abs(d.y) < 0.0001)
            {
                if (t == 0.0)
                {
                    d.x = cp1.x - pt0.x;
                    d.y = cp1.y - pt0.y;
                    if (Math.abs(d.x) < 0.0001 && Math.abs(d.y) < 0.0001)
                    {
                        d.x = cp1.x - pt1.x;
                        d.y = cp1.y - pt1.y;
                    }
                }
                else if (t == 1.0)
                {
                    d.x = pt1.x - cp0.x;
                    d.y = pt1.y - cp0.y;
                    if (Math.abs(d.x) < 0.0001 && Math.abs(d.y) < 0.0001)
                    {
                        d.x = pt1.x - pt0.x;
                        d.y = pt1.y - pt0.y;
                    }
                }
            }
            outvec.set(d);
        }
        return b30;
    }

    public boolean nearestPointTestPt(PointF testpt,OB_MutFloat t,PointF hitpoint,OB_MutFloat distance,float threshold)
    {
        return OB_Maths.nearestPointOnCurve(pt0,pt1,cp0,cp1,testpt, t, hitpoint, distance, threshold, 0.0f, 1.0f, 0.1f);
    }

    public float sForT(float t)
    {
        return OB_Maths.sForT(pt0, pt1, cp0, cp1, t);
    }

    public float tForS(float s)
    {
        return (float)OB_Maths.tForS(pt0, pt1, cp0, cp1, 64, s,length());
    }

    public void transformByMatrix(Matrix t)
    {
        super.transformByMatrix(t);
        float ps[] = new float[4];
        ps[0] = cp0.x;
        ps[1] = cp0.y;
        ps[2] = cp1.x;
        ps[3] = cp1.y;
        t.mapPoints(ps);
        cp0.x = ps[0];
        cp0.y = ps[1];
        cp1.x = ps[2];
        cp1.y = ps[3];
    }



}