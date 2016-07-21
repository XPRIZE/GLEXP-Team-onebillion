package org.onebillion.xprz.utils;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 06/02/16.
 */
public class USubPath
{
    public List<ULine> elements;
    public boolean closed;
    float length;
    PointF currPoint;
    public USubPath()
    {
        length = -1;
        elements = new ArrayList<>();
        currPoint = new PointF();
    }

    public static USubPath uSubPathFromPoints(List<PointF>arr)
    {
        USubPath usp = new USubPath();
        if(arr.size()  == 0)
            return usp;
        usp.moveToPoint(arr.get(0));
        for(int i = 1;i < arr.size();i++)
            usp.lineToPoint(arr.get(i));
        return usp;
    }
    public void moveToPoint(PointF pt)
    {
        currPoint.set(pt);
    }
    public void moveTo(float x,float y)
    {
        currPoint.set(x,y);
    }

    public void lineToPoint(PointF pt)
    {
        elements.add(new ULine(currPoint.x,currPoint.y,pt.x,pt.y));
        currPoint.set(pt);
    }

    public void lineTo(float x,float y)
    {
        elements.add(new ULine(currPoint.x,currPoint.y,x,y));
        currPoint.set(x,y);
    }


    public void curveToPoint(PointF pt,PointF cp0,PointF cp1)
    {
        elements.add(new UCurve(currPoint.x,currPoint.y,pt.x,pt.y,cp0.x,cp0.y,cp1.x,cp1.y));
        currPoint.set(pt);
    }

    public void cubicTo(float cp0x,float cp0y,float cp1x,float cp1y,float ptx,float pty)
    {
        elements.add(new UCurve(currPoint.x,currPoint.y,ptx,pty,cp0x,cp0y,cp1x,cp1y));
        currPoint.set(ptx,pty);
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
        for (ULine gl : elements)
            length += gl.length();
        float stt = 0;
        for (ULine gl : elements)
        {
            gl.spStartT = stt;
            gl.proportionalLength = gl.length() / length;
            gl.spEndT = stt + gl.proportionalLength;
            stt = gl.spEndT;
        }
    }

    public Path bezierPath()
    {
        Path bez = new Path();
        if (elements.size() > 0)
        {
            PointF pt = elements.get(0).pt0;
            bez.moveTo(pt.x,pt.y);
            for (ULine el : elements)
            {
                if (el instanceof UCurve)
                {
                    UCurve gc = (UCurve)el;
                    bez.cubicTo(gc.cp0.x,gc.cp0.y,gc.cp1.x,gc.cp1.y,gc.pt1.x,gc.pt1.y);
                }
                else
                    bez.lineTo(el.pt1.x,el.pt1.y);
            }
        }
        return bez;
    }

    public USubPath subPathFrom(float startS,float endS)
    {
        USubPath usp = new USubPath();
        float finishS = endS;
        if(endS > 1.0)
            endS = 1.0f;
        length();
        if(startS < 0.0)
        {
            PointF outVec = new PointF();
            elements.get(0).tAlongt(0.0f,outVec);
            float reqLen = length()  * -startS;
            outVec = OB_Maths.ScalarTimesPoint(reqLen, OB_Maths.NormalisedVector(outVec));
            PointF pt1 = elements.get(0).pt0;
            PointF pt0 = OB_Maths.OffsetPoint(pt1, -outVec.x, -outVec.y);
            usp.elements.add(new ULine(pt0.x,pt0.y,pt1.x,pt1.y));
        }
        float cumS = 0.0f;
        int startidx = 0;
        while(startidx < elements.size()  && cumS + elements.get(startidx).proportionalLength < startS)
        {
            cumS += elements.get(startidx).proportionalLength;
            startidx++;
        }
        ULine l = elements.get(startidx);
        float rems = startS - cumS;
        float propStartS = rems / l.proportionalLength;
        int endidx = startidx;
        while(endidx < elements.size()  && cumS + elements.get(endidx).proportionalLength < endS)
        {
            cumS += elements.get(endidx).proportionalLength;
            endidx++;
        }
        if(endidx == elements.size() )
        {
            //endidx--;
            rems = 0;
        }
        else
            rems = endS - cumS;
        if(startidx == endidx)
        {
            float propEndS = rems / l.proportionalLength;
            usp.elements.add(l.objectFromMinT(propStartS,propEndS));
        }
        else
        {
            usp.elements.add(l.objectFromMinT(propStartS,1.0f));
            int idx = startidx + 1;
            while(idx < endidx)
            {
                usp.elements.add(elements.get(idx).copy());
                idx++;
            }
            if(rems > 0)
            {
                l = elements.get(idx);
                float propEndS = rems / l.proportionalLength;
                usp.elements.add(l.objectFromMinT(0,propEndS));
            }
        }
        if(finishS > 1.0)
        {
            PointF outVec = new PointF();
            l = elements.get(elements.size()-1);
            l.tAlongt(1.0f,outVec);
            float reqLen = length()  * (finishS - 1.0f);
            outVec = OB_Maths.ScalarTimesPoint(reqLen,OB_Maths.NormalisedVector(outVec));
            PointF pt0 = elements.get(elements.size()-1).pt1;
            PointF pt1 = OB_Maths.OffsetPoint(pt0, outVec.x, outVec.y);
            usp.elements.add(new ULine(pt0.x,pt0.y,pt1.x,pt1.y));
        }
        return usp;
    }

    public float subPathTForPathElement(int idx,float t)
    {
        float cumT = 0;
        for (int i = 0;i < idx;i++)
            cumT += elements.get(i).proportionalLength;
        cumT += (t * elements.get(idx).proportionalLength);
        return cumT;
    }

    public float subPathSForPathElement(int idx,float s)
    {
        float cumS = 0;
        for (int i = 0;i < idx;i++)
            cumS += elements.get(i).proportionalLength;
        cumS += (s * elements.get(idx).proportionalLength);
        return cumS;
    }

    public void close()
    {
        closed = true;
    }

    public void transformByMatrix(Matrix t)
    {
        for (ULine ul : elements)
        {
            ul.transformByMatrix(t);
        }
    }


    public float nearestPointOnSubPathForPoint(PointF testPoint,OB_MutFloat distance,float threshold)
    {
        PointF hitPoint = new PointF();
        OB_MutFloat outt = new OB_MutFloat(0),outdistance = new OB_MutFloat(threshold);
        float closestDist = 1000000;
        float closestT = -1;
        length();
        for(ULine ul : elements)
        {
            boolean success = ul.nearestPointTestPt(testPoint,outt,hitPoint,outdistance,threshold);
            if(success && outdistance.value < closestDist)
            {
                closestDist = outdistance.value;
                closestT = OB_Maths.interpolateVal(ul.spStartT, ul.spEndT, outt.value);
                if(threshold > closestDist)
                    threshold = closestDist;
            }
        }
        if(closestT >=0 && distance != null)
            distance.value = closestDist;
        return closestT;
    }

    public float nearestPointOnSubPathForPoint(PointF testPoint,OB_MutFloat distance,float threshold,float startT,float endT)
    {
        USubPath sp = subPathFrom(startT,endT);
        OB_MutFloat outDistance = null;
        if (distance != null)
            outDistance = new OB_MutFloat(0);
        float t = sp.nearestPointOnSubPathForPoint(testPoint,outDistance,threshold);
        if(t >=0)
        {
            if (distance != null)
                distance.value = outDistance.value;
            t = OB_Maths.interpolateVal(startT, endT, t);
        }
        return t;
    }

}
