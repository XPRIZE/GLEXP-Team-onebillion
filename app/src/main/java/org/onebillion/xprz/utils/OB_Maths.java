package org.onebillion.xprz.utils;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class OB_Maths
{
    static final double TOLERANCE = 0.0000001;  // Application specific tolerance
    public static float FLAT_THRESHOLD = 0.01f;

    public static double clamp01(double t)
    {
        if (t < 0.0)
            return 0.0;
        if (t > 1.0)
            return 1.0;
        return t;
    }

    public static PointF bezease(float t)
    {
        float p0x = 0f,p0y = 0f;
        float c0x = 0.42f, c0y = 0.0f;
        float c1x = 0.58f, c1y = 1.0f;
        float p1x = 1.0f, p1y = 1.0f;
        float tprime = 1.0f - t;
        float tprime2 = tprime * tprime;
        float t2 = t * t;
        float f0x = p0x * tprime * tprime2,f0y = p0y * tprime * tprime2;
        float fc0x = c0x * tprime2 * 3 * t,fc0y = c0y * tprime2 * 3 * t;
        float fc1x = c1x * tprime * t2 * 3,fc1y = c1y * tprime * t2 * 3;
        float f1x = t2 * t * p1x,f1y = t2 * t * p1y;
        return new PointF(f0x+fc0x+fc1x+f1x,f0y+fc0y+fc1y+f1y);
    }

    public static float bezef(float t)
    {
        return clamp01(bezease(clamp01(t)).y);
    }

    public static float easein(float t)
    {
        return clamp01((bezease(t*0.5f).y)*2.0f);
    }

    public static float easeout(float t)
    {
        return clamp01(((bezease(t*0.5f+0.5f).y)-0.5f)*2.0f);
    }

    public static float  clamp(float min,float max,float t)
    {
        if (t < min)
            return min;
        if (t > max)
            return max;
        return t;

    }

    public static float clamp01(float t)
    {
        if (t < 0.0)
            return 0.0f;
        if (t > 1.0)
            return 1.0f;
        return t;
    }

    public static Rect roundRect(RectF rf)
    {
        int w = Math.round(rf.width());
        int h = Math.round(rf.height());
        int l = Math.round(rf.left);
        int t = Math.round(rf.top);
        return new Rect(l, t, l+w, t+h);
    }

    public static PointF locationForRect(float x,float y,RectF rect)
    {
        PointF opt = new PointF();
        opt.x = rect.left;
        opt.y = rect.top;
        float w = rect.right - rect.left;
        float h = rect.bottom - rect.top;
        opt.x += x * w;
        opt.y += y * h;
        return opt;
    }

    public static PointF locationForRect(PointF pt,RectF rect)
    {
        PointF opt = new PointF();
        opt.x = rect.left;
        opt.y = rect.top;
        float w = rect.right - rect.left;
        float h = rect.bottom - rect.top;
        opt.x += pt.x * w;
        opt.y += pt.y * h;
        return opt;
    }

    public static PointF locationForRect(PointF pt,Rect rect)
    {
        return locationForRect(pt, new RectF(rect));
    }

    public static PointF locationForRect(float x,float y,Rect rect)
    {
        return locationForRect(x, y, new RectF(rect));
    }

    public static PointF relativePointInRectForLocation(PointF loc,RectF r)
    {
        loc.x -= r.left;
        loc.y -= r.top;
        return new PointF(loc.x / r.width(), loc.y / r.height());
    }

    public static RectF denormaliseRect(RectF inRect,RectF refRect)
    {
        RectF outRect = new RectF();
        PointF origin = new PointF(inRect.left,inRect.top);
        origin = locationForRect(origin, refRect);
        outRect.left = origin.x;
        outRect.top = origin.y;
        float width = inRect.width() * refRect.width();
        float height = inRect.height() * refRect.height();
        outRect.right = outRect.left + width;
        outRect.bottom = outRect.top + height;
        return outRect;
    }

    public static RectF NormaliseRect(RectF inRect,RectF refRect)
    {
        PointF origin = relativePointInRectForLocation(new PointF(inRect.left,inRect.top), refRect);
        PointF botright = relativePointInRectForLocation(new PointF(inRect.right,inRect.bottom), refRect);
        return new RectF(origin.x,origin.y,botright.x,botright.y);
    }

    public static PointF midPoint(RectF r)
    {
        float w = r.right - r.left;
        float h = r.bottom - r.top;
        return new PointF(r.left + w / 2f,r.top + h / 2f);
    }

    public static float interpolateVal(float start,float end,float t)
    {
        return start + (end - start) * t;
    }

    public static PointF tPointAlongLine(float t,PointF p0,PointF p1)
    {
        float dx = p1.x - p0.x;
        float dy = p1.y - p0.y;
        dx = dx * t;
        dy = dy * t;
        return new PointF(p0.x + dx,p0.y + dy);
    }

    public static void tPointAlongLine(float t,PointF p0,PointF p1,PointF pout)
    {
        float dx = p1.x - p0.x;
        float dy = p1.y - p0.y;
        dx = dx * t;
        dy = dy * t;
        pout.set(p0.x+dx, p0.y+dy);
    }

    public static void splitCurveByT(PointF startPt,PointF endPt,PointF controlPt1,PointF controlPt2,float t,
                       PointF c1EndPt,PointF c1CP1,PointF c1CP2,
                       PointF c2CP1,PointF c2CP2)
    {
        PointF pt10 = tPointAlongLine(t,startPt,controlPt1);
        PointF pt11 = tPointAlongLine(t,controlPt1,controlPt2);
        PointF pt12 = tPointAlongLine(t,controlPt2,endPt);
        PointF pt21 = tPointAlongLine(t,pt10,pt11);
        PointF pt22 = tPointAlongLine(t,pt11,pt12);
        PointF pt33 = tPointAlongLine(t,pt21,pt22);
        c1EndPt.set(pt33);
        c1CP1.set(pt10);
        c1CP2.set(pt21);
        c2CP1.set(pt22);
        c2CP2.set(pt12);
    }

    public static float sForT(PointF startPt,PointF endPt,PointF controlPt1,PointF controlPt2,float t)
    {
        double wholelen = curveLength(startPt, endPt, controlPt1, controlPt2);
        PointF c1EndPt = new PointF(),c1CP1 = new PointF(),c1CP2 = new PointF(),c2CP1 = new PointF(),c2CP2 = new PointF();
        splitCurveByT(startPt, endPt, controlPt1, controlPt2, t, c1EndPt, c1CP1, c1CP2, c2CP1, c2CP2);
        double leftlen = curveLength(startPt, c1EndPt, c1CP1, c1CP2);
        return (float)(leftlen / wholelen);
    }

    static double tForS(PointF startPt,PointF endPt,PointF controlPt1,PointF controlPt2,int steps,double s,double arclength)
    {
        if (s == 1.0 || s == 0 || arclength == 0.0)
            return s;
        double lbound=0.0,rbound=1.0,lboundS=0.0,rboundS=1.0;
        double guessT,valueS,epsilon;
        guessT = s;
        PointF c1EndPt = new PointF(),c1CP1 = new PointF(),c1CP2 = new PointF(),c2CP1 = new PointF(),c2CP2 = new PointF();
        splitCurveByT(startPt, endPt, controlPt1, controlPt2, (float)guessT, c1EndPt, c1CP1, c1CP2, c2CP1, c2CP2);
        valueS = curveLength(startPt, c1EndPt, c1CP1, c1CP2) / arclength;
        epsilon = valueS - s;
        for (int i = 0;i < steps && Math.abs(epsilon) >= TOLERANCE;i++)
        {
            if (valueS == lboundS)
                return guessT;
            double ratio = (s - lboundS) / (valueS - lboundS);
            double nextGuess = lbound + (guessT - lbound) * ratio;
            if (epsilon < 0.0)
            {
                lboundS = valueS;
                lbound = guessT;
            }
            else
            {
                rboundS = valueS;
                rbound = guessT;
            }
            guessT = nextGuess;
            splitCurveByT(startPt, endPt, controlPt1, controlPt2, (float)guessT, c1EndPt, c1CP1, c1CP2, c2CP1, c2CP2);
            valueS = curveLength(startPt, c1EndPt, c1CP1, c1CP2) / arclength;
            epsilon = valueS - s;
        }
        return guessT;
    }

    public static float dot_product(PointF pt1,PointF pt2)
    {
        return (pt1.x * pt2.x + pt1.y * pt2.y);
    }

    public static PointF OffsetPoint(PointF pt,float x,float y)
    {
        return new PointF(pt.x + x,pt.y + y);
    }

    public static PointF DiffPoints(PointF pt2,PointF pt1)
    {
        return new PointF(pt2.x - pt1.x,pt2.y - pt1.y);
    }

    public static PointF AddPoints(PointF pt2,PointF pt1)
    {
        return new PointF(pt2.x + pt1.x,pt2.y + pt1.y);
    }

    public static PointF ScalarTimesPoint(float sc,PointF pt1)
    {
        return new PointF(sc * pt1.x,sc * pt1.y);
    }

    public static PointF lperp(PointF d)						//returns difference vector perpendicular to d (left-hand)
    {
        PointF od = new PointF();
        od.x = -d.y;
        od.y = d.x;
        return od;
    }

    public static PointF rperp(PointF d)						//returns difference vector perpendicular to d (right-hand)
    {
        PointF od = new PointF();
        od.x = d.y;
        od.y = -d.x;
        return od;
    }

    public static float SquaredDistance(PointF p0,PointF p1)
    {
        PointF dp = DiffPoints(p0,p1);
        return (dp.x * dp.x + dp.y * dp.y);
    }

    public static float PointDistance(PointF p0,PointF p1)
    {
        return (float)Math.sqrt(SquaredDistance(p0,p1));
    }

    public static PointF NormalisedVector(PointF v)
    {
        float len = (float)Math.sqrt(v.x * v.x + v.y * v.y);
        return new PointF(v.x / len, v.y / len);
    }

    public static float squaredPointDistanceFromLineSegment(PointF linePt0,PointF linePt1,PointF testPoint,
                                              OB_MutFloat outt,PointF hitPointOnLine)
    {
        float t = outt.value;
        PointF d = DiffPoints(linePt1,linePt0);										//direction vector
        PointF YmP0 = DiffPoints(testPoint,linePt0);
        float localt = dot_product(d,YmP0);
        float DdD = dot_product(d,d);
        t = localt / DdD;
        if (t <= 0)
        {
            outt.value = t;
            hitPointOnLine.set(linePt0);
            return (dot_product(YmP0,YmP0));
        }
        if (t >= 1f)
        {
            hitPointOnLine.set(linePt1);
            outt.value = t;
            PointF YmP1 = DiffPoints(testPoint,linePt1);
            return (dot_product(YmP1,YmP1));
        }
        hitPointOnLine.x = linePt0.x + d.x * t;
        hitPointOnLine.y = linePt0.y + d.y * t;
        outt.value = t;
        return (dot_product(YmP0,YmP0) - localt * t);
    }

    public static float pointDistanceFromLineSegment(PointF linePt0,PointF linePt1,PointF testPoint,
                                       OB_MutFloat t,PointF hitPointOnLine)
    {
        return (float)Math.sqrt(squaredPointDistanceFromLineSegment(linePt0,linePt1,testPoint,
                t,hitPointOnLine));
    }

    public static PointF MidPoint(PointF p0,PointF p1)
    {
        return new PointF(p0.x + (p1.x - p0.x)/2,p0.y + (p1.y - p0.y)/2);
    }

    public static void expandRectToPoint(RectF r,PointF p)
    {
        if (p.x < r.left)
        {
            r.left = p.x;
        }
        else
        {
            if (p.x > r.right)
                r.right = p.x;
        }
        if (p.y < r.top)
        {
            r.top = p.y;
        }
        else
        {
            if (p.y > r.right)
                r.right = p.y;
        }
    }

    public static RectF curveBounds(PointF p1,PointF p2,PointF p3,PointF p4)
    {
        RectF r = new RectF();
        r.left = r.right = p1.x;
        r.top = r.bottom = p1.y;
        expandRectToPoint(r,p2);
        expandRectToPoint(r,p3);
        expandRectToPoint(r,p4);
        if (r.right == r.left)
            r.right = r.left + 1.0f;
        if (r.bottom == r.top)
            r.bottom = r.top + 1.0f;
        return r;
    }

    public static boolean nearestPointOnCurve(PointF startPt,PointF endPt,PointF controlPt1,PointF controlPt2,PointF testPoint,
                                              OB_MutFloat t,PointF hitPointOnCurve,OB_MutFloat distance,float threshold,float leftT,float rightT,float flatThreshold)
    {
        RectF r = curveBounds(startPt,endPt,controlPt1,controlPt2);
        r.inset(-threshold,-threshold);
        if (!r.contains(testPoint.x,testPoint.y))
            return false;
        if (flatness(startPt,endPt,controlPt1,controlPt2) < flatThreshold)
        {
            OB_MutFloat localt = new OB_MutFloat(0);
            PointF localpt = new PointF();
            float pdist = pointDistanceFromLineSegment(startPt,endPt,testPoint,localt,localpt);
            if (pdist < distance.value)
            {
                distance.value = pdist;
                hitPointOnCurve = localpt;
                t.value = interpolateVal(leftT, rightT, localt.value);
                return true;
            }
            else
                return false;
        }
        PointF pt10 = MidPoint(startPt,controlPt1);
        PointF pt11 = MidPoint(controlPt1,controlPt2);
        PointF pt12 = MidPoint(controlPt2,endPt);
        PointF pt21 = MidPoint(pt10,pt11);
        PointF pt22 = MidPoint(pt11,pt12);
        PointF pt33 = MidPoint(pt21,pt22);
        float midT = leftT+(rightT-leftT)/2;
        boolean b1 = nearestPointOnCurve(startPt,pt33,pt10,pt21,testPoint,t,hitPointOnCurve,distance,threshold,leftT,midT,flatThreshold);
        boolean b2 = nearestPointOnCurve(pt33,endPt,pt22,pt12,testPoint,t,hitPointOnCurve,distance,threshold,midT,rightT,flatThreshold);
        return b1 || b2;
    }

    public static boolean nearestPointOnLine(PointF pt0,PointF pt1,PointF testPoint,OB_MutFloat t,
                                             PointF hitpointonline,OB_MutFloat distance,float threshold)
    {
        return pointDistanceFromLineSegment(pt0,pt1,testPoint,t,hitpointonline) < threshold;
    }

    public static float flatness3(PointF centrepoint,PointF pt0,PointF pt1)
    {
        OB_MutFloat dummyFloat = new OB_MutFloat(0);
        PointF dummyPoint = new PointF();
        float f1 = squaredPointDistanceFromLineSegment(pt0,pt1,centrepoint,dummyFloat,dummyPoint);
        return f1;
    }

    public static float flatness(PointF startPt,PointF endPt,PointF controlPt1,PointF controlPt2)
    {
        return Math.abs(flatness3(controlPt1,startPt,endPt)) + Math.abs(flatness3(controlPt2,startPt,endPt));
    }

    public static float curveLength(PointF startPt,PointF endPt,PointF controlPt1,PointF controlPt2)
    {
        if (flatness(startPt, endPt, controlPt1, controlPt2) < FLAT_THRESHOLD)
            return PointDistance(startPt, endPt);
        PointF c1EndPt = new PointF(),c1CP1 = new PointF(),c1CP2 = new PointF(),c2CP1 = new PointF(),c2CP2 = new PointF();
        splitCurveByT(startPt, endPt, controlPt1, controlPt2, 0.5f, c1EndPt, c1CP1, c1CP2, c2CP1, c2CP2);
        return curveLength(startPt, c1EndPt, c1CP1, c1CP2) + curveLength(c1EndPt, endPt, c2CP1, c2CP2);
    }

    public static RectF frameForPosition(RectF bounds,PointF pos)
    {
        RectF res = new RectF(bounds);
        res.left = pos.x - bounds.width() / 2;
        res.top = pos.y - bounds.height() / 2;
        return res;
    }


    public static double rndom()
    {
        return ((double)Math.random());
    }

    public static int randomInt(int minval,int maxval)
    {
        double dval = rndom();
        return (int)Math.round(minval + (maxval - minval) * dval);
    }






}
