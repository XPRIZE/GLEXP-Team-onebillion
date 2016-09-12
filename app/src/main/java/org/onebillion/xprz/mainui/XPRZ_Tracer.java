package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBStroke;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.USubPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 01/02/16.
 */
public class XPRZ_Tracer extends XPRZ_SectionController
{
    public List<OBControl> doneTraces;                 //Traces already drawn
    public OBPath currentTrace;                       //Control for the trace being drawn
    public List<OBGroup> subPaths;                          //Groups of split segments
    public int subPathIndex;
    public int segmentIndex;
    public List<OBPath> uPaths;
    public boolean finished;
    public int pathColour;
    public float swollenLineWidth;
    public float pathWidth;

    public XPRZ_Tracer()
    {
        swollenLineWidth = 100;
        pathColour = Color.RED;
        pathWidth = 20;

    }
    float traceLineWidth()
    {
        return pathWidth;
    }


    int traceColour()
    {
        return pathColour;
    }

    public List<OBPath> splitInto(OBPath obp,PathMeasure pm,int n,float overlap)
    {
        float len = pm.getLength();
        List<OBPath> arr = new ArrayList<>();
        RectF bb = obp.bounds();
        PointF pos = obp.position();
        for (int i = 0;i < n;i++)
        {
            float startS = 1.0f / n * i - overlap;
            if (i == 0)
                startS -= overlap;
            float endS = 1.0f / n * (i + 1) + overlap;
            Path p = new Path();
            if (startS < 0)
            {
                float[] xy = new float[2];
                float[] txy = new float[2];
                pm.getPosTan(0,xy,txy);
                if (txy[0] == 0 && txy[1] == 0)
                {
                    pm.getPosTan(1,txy,null);
                    txy[0] -= xy[0];
                    txy[1] -= xy[1];
                }
                float ltxy = (float)Math.sqrt(txy[0]*txy[0] + txy[1]*txy[1]);
                txy[0] = txy[0] / ltxy * startS * len;
                txy[1] = txy[1] / ltxy * startS * len;
                p.moveTo(xy[0] + txy[0],xy[1] + txy[1]);
                p.lineTo(xy[0],xy[1]);
                Path p2 = new Path();
                pm.getSegment(0,endS * len,p,false);
                //p.addPath(p2);
            }
            else if (endS > 1)
            {
                float[] xy = new float[2];
                float[] txy = new float[2];
                pm.getPosTan(len,xy,txy);
                if (txy[0] == 0 && txy[1] == 0)
                {
                    pm.getPosTan(len-1,txy,null);
                    txy[0] = xy[0] - txy[0];
                    txy[1] = xy[1] - txy[1];
                }
                float ltxy = (float)Math.sqrt(txy[0]*txy[0] + txy[1]*txy[1]);
                txy[0] = txy[0] / ltxy * overlap * len;
                txy[1] = txy[1] / ltxy * overlap * len;
                pm.getSegment(startS * len,len,p,true);
                p.lineTo(xy[0] + txy[0],xy[1]+txy[1]);
            }
            else
                pm.getSegment(startS * len,endS * len,p,true);
            OBPath nobp = new OBPath(p,bb.width(),bb.height(), pos.x, pos.y);
            arr.add(nobp);
        }

        return arr;
    }
    public OBGroup splitPath(OBPath obp)
    {
        PathMeasure pm = new PathMeasure(obp.path(),false);
        float splen = pm.getLength();
        int noSplits = (int)(splen / (100));
        List<OBPath> newOBPaths = splitInto(obp, pm, noSplits, 1.0f / (noSplits * 4));
        for (OBPath newOBPath : newOBPaths)
        {
            //newOBPath.setBounds(obp.bounds);
            //newOBPath.setPosition(obp.position());
            //newOBPath.sizeToBox(obp.bounds());
            newOBPath.setStrokeColor(Color.argb((int)(255 * 0.4f),255,0,0));
            newOBPath.setLineJoin(OBStroke.kCALineJoinRound);
            newOBPath.setFillColor(0);
            newOBPath.setLineWidth(swollenLineWidth);
        }
        OBGroup grp = new OBGroup((List<OBControl>)(Object)newOBPaths);
        return grp;
    }

    public void positionArrow()
    {
        OBControl traceArrow = filterControls("trace.*arrow").get(0);
        PointF outvec = new PointF();
        OBPath p = uPaths.get(subPathIndex);
        PointF arrowpoint = convertPointFromControl(p.sAlongPath(0.0f,outvec),p);
        if (traceArrow != null)
        {
            traceArrow.setPosition(arrowpoint);
            traceArrow.rotation = (float) Math.atan2(outvec.x, -outvec.y);
//        traceArrow.pointAt(OB_Maths.AddPoints(arrowpoint, outvec));
            traceArrow.show();
        }
    }

    public void startNewSubpath()
    {
        OBGroup currSubPath = subPaths.get(subPathIndex);
        RectF f = currSubPath.bounds();
        currentTrace = new OBPath(new Path(),f.width(),f.height(),currSubPath.position().x,currSubPath.position().y);
        currentTrace.setLineCap(OBStroke.kCGLineCapRound);
        currentTrace.setLineJoin(OBStroke.kCALineJoinRound);
        currentTrace.setScale(currSubPath.scale());
        currentTrace.setLineWidth(traceLineWidth());
        currentTrace.setFillColor(0);
        currentTrace.setZPosition(40);
        currentTrace.setStrokeColor(traceColour());
        //currentTracingPath = [UIBezierPath bezierPath];
        attachControl(currentTrace);
        OBControl traceArrow = filterControls("trace.*arrow").get(0);
        if (traceArrow != null)
        {
            traceArrow.hide();
            traceArrow.setZPosition(50);
        }
        finished = false;
    }

    public boolean pointInSegment(PointF pt,int idx)
    {
        OBGroup currSubPath = subPaths.get(subPathIndex);
        if (idx >= currSubPath.members.size())
            return false;
        OBPath currSegment = (OBPath)currSubPath.members.get(idx);
        PointF cpt = convertPointToControl(pt,currSegment);
        return (currSegment.alphaAtPoint(cpt.x,cpt.y)> 0);
    }

    public PointF lastTracedPoint()
    {
        OBGroup currSubPath = subPaths.get(subPathIndex);
        OBPath currSegment = (OBPath)currSubPath.members.get(segmentIndex);
        return convertPointFromControl(currentTrace.currentPoint(),currSegment);
    }

    public boolean pointHitRelativeSeg(PointF pt,int i)
    {
        OBGroup currSubPath = subPaths.get(subPathIndex);
        int idx = segmentIndex + i;
        if (i >= 0)
        {
            OBPath currSegment = (OBPath)currSubPath.members.get(idx);
            PointF cpt = convertPointToControl(pt,currSegment);
            return (currSegment.alphaAtPoint(cpt.x,cpt.y) > 0);
        }
        else
            return false;
    }

    public void effectMoveToPoint(PointF pt)
    {
        if (!pointHitRelativeSeg(pt,0))
        {
            if (segmentIndex + 1 < subPaths.get(subPathIndex).members.size())
            {
                if (pointHitRelativeSeg(pt,+1))
                {
                    segmentIndex++;
                }
                else
                {
                    new AsyncTask<Void, Void,Void>()
                    {
                        protected Void doInBackground(Void... params) {
                            try
                            {
                                gotItWrongWithSfx();
                                setStatus(STATUS_WAITING_FOR_TRACE);                            }
                            catch (Exception exception)
                            {
                            }
                            return null;
                        }}.execute();

                    return;
                }
            }
            else
            {
                finished = true;
                return;
            }
        }
        if (segmentIndex + 1 == subPaths.get(subPathIndex).members.size())
            finished = true;
        PointF cpt = convertPointToControl(pt,currentTrace);
        currentTrace.addLineToPoint(cpt.x,cpt.y);
    }

}
