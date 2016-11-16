package org.onebillion.onecourse.mainui;

/**
 * Created by alan on 11/10/15.
 */
import android.content.Context;
import android.graphics.*;
import android.view.View;
import android.view.ViewGroup;

public class OBView extends ViewGroup
{
    public static class LayoutParams extends ViewGroup.LayoutParams
    {
        float left,right,top,bottom;
        public LayoutParams(float l,float t,float r,float b)
        {
            super(0,0);
            left = l;
            top = t;
            right = r;
            bottom = b;
        }
        public LayoutParams(RectF r)
        {
            this(r.left,r.top,r.right,r.bottom);
        }
    }
    public OBViewController controller;
    public OBView(Context context, OBViewController c)
    {
        super(context);
        controller = c;
        setWillNotDraw(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        if (controller != null)
            controller.viewWasLaidOut(changed, l, t, r, b);
    }

    @Override
    public boolean shouldDelayChildPressedState()
    {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        controller.drawControls(canvas);
    }

    void addSubviewAtPosition(View sv,float cx,float cy)
    {
        addView(sv);
        sv.measure(getMeasuredWidth(), getMeasuredHeight());
        positionSubview(sv, cx, cy);
    }

    void positionSubview(View sv,float cx,float cy)
    {
        float h = sv.getMeasuredHeight();
        float w = sv.getMeasuredWidth();
        float top = cy - h / 2.0f;
        float left = cx - w / 2.0f;
        int itop = (int)top;
        int ileft = (int)left;
        sv.layout(ileft,itop,ileft+(int)w,itop+(int)h);
        sv.setLayoutParams(new OBView.LayoutParams(ileft,itop,ileft+(int)w,itop+(int)h));
    }
    void addSubviewWithFrame(View sv,Rect f)
    {
        addView(sv);
        sv.layout(f.left,f.top,f.right,f.bottom);
    }


}
