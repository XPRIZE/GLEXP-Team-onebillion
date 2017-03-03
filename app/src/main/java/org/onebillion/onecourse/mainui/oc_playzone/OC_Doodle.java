package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.Path;
import android.os.Handler;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGradientLayer;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBRadialGradientLayer;
import org.onebillion.onecourse.controls.OBRadialGradientPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 02/03/2017.
 */

public class OC_Doodle extends OC_SectionController
{
    class DoodleGradient
    {
        float[] HSV = new float[3];
        float x,y;
        float vectorx,vectory;
        float hInc = 1;
        OBRadialGradientPath gradient;
        float slowFactor = 0.1f;
        public DoodleGradient(int col,float px,float py,float vx,float vy,OBPath template)
        {
            Path p = template.path();
            x = px;
            y = py;
            vectorx = vx * slowFactor;
            vectory = vy * slowFactor;
            Color.colorToHSV(col,HSV);
            gradient = new OBRadialGradientPath(p,x,y,1500, Color.HSVToColor(HSV),Color.HSVToColor(0,HSV));
            gradient.setZPosition(template.zPosition() + 0.02f);
            gradient.sizeToBoundingBox();
            gradient.setPosition(template.position());
        }
        void doStep()
        {
            x += vectorx;
            y += vectory;
            if (x < -0.2f)
                vectorx = Math.abs(vectorx);
            else if (x > 1.2f)
                vectorx = -Math.abs(vectorx);
            if (y < -0.2f)
                vectory = Math.abs(vectory);
            else if (y > 1.2f)
                vectory = -Math.abs(vectory);
            OBRadialGradientLayer l = (OBRadialGradientLayer) gradient.gradientLayer;
            l.cx = x;
            l.cy = y;
            HSV[0] += hInc;
            if (HSV[0] > 360.0f)
                HSV[0] -= 360.0f;
            int rgb = Color.HSVToColor(HSV);
            l.colours[0] = rgb;
            gradient.setNeedsRetexture();
        }
    }
    List<DoodleGradient> gradients = new ArrayList();
    private Runnable messageCheckRunnable;
    private Handler messageCheckHandler = new Handler();

    public OBControl rad(OBControl c,float x,float y,int col1,int col2)
    {
        Path p = new Path();
        p.addRect(c.frame(), Path.Direction.CCW);
        OBRadialGradientPath gp1 = new OBRadialGradientPath(p,x,y,1500, col1,col2);
        gp1.setZPosition(10);
        gp1.sizeToBoundingBox();
        gp1.setPosition(OB_Maths.locationForRect(0.5f,0.5f,boundsf()));
        return gp1;
    }
    public void miscSetUp()
    {
        OBPath blackboard = (OBPath) objectDict.get("blackboard");
        OBPath blackborder = (OBPath) blackboard.copy();
        blackborder.setFillColor(0);
        blackborder.setLineWidth(applyGraphicScale(6));
        blackborder.setZPosition(blackboard.zPosition()+ 0.1f);
        blackborder.outdent(blackborder.lineWidth());
        attachControl(blackborder);

        DoodleGradient dg = new DoodleGradient(Color.RED,0.25f,0.8f,0.01f,0.03f,blackboard);
        gradients.add(dg);
        attachControl(dg.gradient);
        dg = new DoodleGradient(Color.BLUE,0.85f,0.2f,0.01f,0.03f,blackboard);
        gradients.add(dg);
        attachControl(dg.gradient);
        dg = new DoodleGradient(Color.YELLOW,0.5f,0.7f,0.01f,0.03f,blackboard);
        gradients.add(dg);
        attachControl(dg.gradient);
        scheduleTimerEvent();

/*
        OBControl r = rad(blackboard,0.25f,0.8f,Color.RED,Color.argb(0,255,0,0));
        attachControl(r);
        r = rad(blackboard,0.85f,0.2f,Color.BLUE,Color.argb(0,0,0,255));
        attachControl(r);
        r = rad(blackboard,0.5f,0.7f,Color.YELLOW,Color.argb(0,0,255,255));
        attachControl(r);*/
    }
    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList<>();
        events.addAll(Arrays.asList("c,d,e".split(",")));
        events.add(0,"a");
        doVisual(currentEvent());
    }

    void timerEvent()
    {
        for (DoodleGradient dg : gradients)
        {
            dg.doStep();
            invalidateControl(dg.gradient);
            scheduleTimerEvent();
        }
    }
    void scheduleTimerEvent()
    {
        if (messageCheckRunnable == null)
        {
            messageCheckRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    timerEvent();
                }
            };
        }
        messageCheckHandler.removeCallbacks(messageCheckRunnable);
        messageCheckHandler.postDelayed(messageCheckRunnable,100);
    }

}
