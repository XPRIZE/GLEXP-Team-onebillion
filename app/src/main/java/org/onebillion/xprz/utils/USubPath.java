package org.onebillion.xprz.utils;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 06/02/16.
 */
public class USubPath
{
    float length;
    List<ULine> elements;
    PointF currPoint;
    public boolean closed;
    public USubPath()
    {
        length = -1;
        elements = new ArrayList<>();
        currPoint = new PointF();
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
        for (ULine gl : elements)
            gl.proportionalLength = gl.length() / length;
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

}
