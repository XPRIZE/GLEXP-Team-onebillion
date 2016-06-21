package org.onebillion.xprz.utils;

import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.SystemClock;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.mainui.OBSectionController;

import java.util.List;

import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by alan on 20/11/15.
 */

public class OBAnimationGroup
{
    public final static int ANIM_CANCEL = 1;
    public int flags;
    List<OBAnim> animations;
    double startTime,duration;
    long startms;
    OBSectionController owner;
    Interpolator interpolator;
    RectF r1;
    RectF r2;

    public void OBAnimationGroup()
    {

    }

    public static OBAnimationGroup runAnims(final List<OBAnim> anims,final double secs,boolean wait,final int timingFunction,final OBSectionController vc)
    {
        final OBAnimationGroup ag = new OBAnimationGroup();
        if (wait)
        {
            ag.applyAnimations(anims,secs,timingFunction,vc);
        }
        else
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    ag.applyAnimations(anims,secs,timingFunction,vc);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

        }

        return ag;
    }

    public static OBAnimationGroup chainAnimations(List<List<OBAnim>> animArrays,List<Float>durations,boolean wait,List<Integer>timingFunctions,int noLoops,OBSectionController vc)
    {
        OBAnimationGroup ag = new OBAnimationGroup();
        ag.chainAnimations(animArrays,durations,timingFunctions,noLoops,vc,wait);
        return ag;
    }

    void doFrame(double frac)
    {
        final float t = (float)OB_Maths.clamp01(interpolator.getInterpolation((float)frac));
        //final float t = OB_Maths.bezef((float)frac);
        new OBRunnableSyncUI(){public void ex()
        {
            for (OBAnim anim : animations)
            {
                Object val = anim.valueForT(t);
                if (anim.typeType == OBAnim.ANIM_TYPE_BLOCK)
                    ((OBAnimBlock)anim).runAnimBlock(t);
                else
                {
                    try
                    {
                        anim.setter.invoke(anim.object,val);
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
        }.run();

    }
    void startAnimations(int timingFunction)
    {
        if (timingFunction == OBAnim.ANIM_EASE_IN_EASE_OUT)
            interpolator = new AccelerateDecelerateInterpolator();
        else if (timingFunction == OBAnim.ANIM_EASE_IN)
            interpolator = new AccelerateInterpolator();
        else if (timingFunction == OBAnim.ANIM_EASE_OUT)
            interpolator = new DecelerateInterpolator();
        else
            interpolator = new LinearInterpolator();
        r1 = new RectF();
        r2 = new RectF();
        for (OBAnim anim : animations)
        {
            anim.retrieveInitialValue();
        }
        startms = SystemClock.uptimeMillis();
    }

    public void applyAnimations(List<OBAnim>anims,double dur,int timingFunction,OBSectionController vc)
    {
        duration = dur;
        animations = anims;
        owner = vc;
        startAnimations(timingFunction);
        double frac = 0;
        while (frac <= 1.0 && (flags & ANIM_CANCEL) == 0)
        {
            long currtime = SystemClock.uptimeMillis();
            if (duration == 0)
                frac = 1.1;
            else
                frac = (currtime - startms) / (duration * 1000);
            doFrame(OB_Maths.clamp01(frac));
            if (owner == null)
                try
                {
                    Thread.sleep(20);
                }
                catch (InterruptedException e)
                {
                }
            else
                owner.waitForSecsNoThrow(0.02);
        }
    }

    public void applyAnimations(final List<OBAnim>anims,final double dur,boolean wait,final int timingFunction,final OBSectionController vc)
    {
        if (wait)
            applyAnimations(anims,dur,timingFunction,vc);
        else
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    applyAnimations(anims, dur,timingFunction,vc);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

    }

    public void chainAnimations(List<List<OBAnim>> animArrays,List<Float>durations,List<Integer>timingFunctions,int noLoops,OBSectionController vc)
    {
        while (noLoops != 0)
        {
            for (int i = 0;i < animArrays.size();i++)
            {
                if ((flags & ANIM_CANCEL) != 0)
                {
                    noLoops = 0;
                    break;
                }
                applyAnimations(animArrays.get(i),durations.get(i),timingFunctions.get(i),vc);
            }
            if (noLoops > 0)
                noLoops--;
        }
    }

    public void chainAnimations(List<List<OBAnim>> animArrays,List<Float>durations,List<Integer>timingFunctions,int noLoops,OBSectionController vc,boolean wait)
    {
        if (wait)
            chainAnimations(animArrays,durations,timingFunctions,noLoops,vc);
        else
        {
            final List<List<OBAnim>> fanimArrays = animArrays;
            final List<Float>fdurations = durations;
            final List<Integer>ftimingFunctions = timingFunctions;
            final int fnoLoops = noLoops;
            final OBSectionController fvc = vc;
            new AsyncTask<Void, Void, Void>()
            {
                protected Void doInBackground(Void... params)
                {
                    chainAnimations(fanimArrays, fdurations, ftimingFunctions, fnoLoops, fvc);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

}
