package org.onebillion.onecourse.mainui.oc_sorting;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBRunnableSyncUI;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 29/03/16.
 */
public class OC_Sorting_S5 extends OC_SectionController
{
    OBPath drawingSurface;


    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    void setUpPathControl()
    {
        drawingSurface = (OBPath)objectDict.get("draw");
        RectF bb = drawingSurface.frame();

        drawingSurface.sizeToBox(new RectF(bounds()));
        drawingSurface.setStrokeEnd(0);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
        setUpPathControl();
    }

    public void start()
    {
        setStatus(0);
        new AsyncTask<Void, Void,Void>()
        {
            protected Void doInBackground(Void... params) {
                try
                {
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.execute();
    }

    public void setSceneXX(String scene)
    {
        if (drawingSurface != null)
        {
            drawingSurface.setPath(null);
            drawingSurface.texturise(false,this);
            drawingSurface.setLineDashPattern(null);
        }
        deleteControls("obj.*");
        super.setSceneXX(scene);
        invalidateView(0,0,bounds().width(),bounds().height());
        for (OBControl obj : filterControls("obj.*"))
        {
            if (obj instanceof OBPath)
            {
                OBPath obp = (OBPath)obj;
                ((OBPath) obj).sizeToBoundingBoxIncludingStroke();
            }
            obj.setRasterScale(Math.abs(obj.scale()));
            obj.setDoubleSided(true);
            obj.enCache();
        }
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
    }

    void tracePointerAlongPath(final OBPath p,float durationMultiplier) throws Exception

    {
        new OBRunnableSyncUI(){public void ex()
        {
            //p.setLineWidth(traceLineWidth);
            p.setStrokeColor(Color.BLUE);
            p.setStrokeEnd(0.0f);
            p.setOpacity(1.0f);
        }}.run();
        long starttime = SystemClock.uptimeMillis();
        float duration = p.length() * 2 * durationMultiplier / theMoveSpeed;
        float frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (float)(currtime - starttime) / (duration * 1000);
            final float t = (frac);
            new OBRunnableSyncUI(){public void ex()
            {
                p.setStrokeEnd(t);
                thePointer.setPosition(p.sAlongPath(t, null));
            }}.run();
            waitForSecs(0.02f);
        }

    }

    public void demo5a() throws Exception
    {
        List<OBControl>fish = sortedFilteredControls("objb.*");
        OBControl frog = objectDict.get("obja");
        PointF targPt = OB_Maths.locationForRect(0.5f, 0.8f, fish.get(0).frame());
        PointF startpt = OB_Maths.locationForRect(0.5f, 1f, new RectF(bounds()));
        loadPointerStartPoint(startpt, targPt);
        PointF restPt = OB_Maths.tPointAlongLine(0.3f, startpt, targPt);
        movePointerToPoint(restPt, -1, true);

        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true);
        waitForSecs(0.3f);
        float angs[] = {0,0,0,0};
        angs[0] = (float)Math.toRadians(thePointer.rotation());
        angs[2] = angs[0] + 20;
        List<OBControl> objs = new ArrayList(fish);
        objs.add(frog);
        for (int i = 0;i < 4;i++)
        {
            PointF pt = OB_Maths.locationForRect(0.5f, 0.8f, objs.get(i).frame());
            movePointerToPoint(pt,angs[i],-1,true);
            playAudioQueuedSceneIndex(currentEvent(), "DEMO", i+1, true);
        }
        waitForSecs(0.4f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f, 1.1f, frog.frame()), -1, true);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 5, true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 6, true);

        PointF outVec = new PointF();
        PointF spt = drawingSurface.sAlongPath(0,outVec);
        movePointerToPoint(spt, -30, -1, true);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 7, true);
        waitForSecs(0.1f);

        tracePointerAlongPath(drawingSurface, 3);
        waitForSecs(0.8f);

        waitForSecs(0.2f);
        thePointer.hide();
        waitForSecs(1f);
        nextScene();

    }

    boolean intersectsCorrectly()
    {
        OBControl target = objectDict.get("obja");
        if (!target.intersectsWith(drawingSurface))
            return false;
        for (OBControl c : filterControls("objb.*"))
            if (c.intersectsWith(drawingSurface))
                return false;
        return true;
    }

    public void checkTrace()
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        try
        {
            lockScreen();
            drawingSurface.sizeToBoundingBoxIncludingStroke();
            unlockScreen();
            if (intersectsCorrectly())
            {
                new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        try
                        {
                            gotItRightBigTick(true);
                            nextScene();
                        }
                        catch(Exception e)
                        {

                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
            else
            {
                drawingSurface.setPath(null);
                new AsyncTask<Void, Void, Void>()
                {
                    @Override
                    protected Void doInBackground(Void... params)
                    {
                        try
                        {
                            gotItWrongWithSfx();
                            waitSFX();
                            playAudioQueuedScene(currentEvent(), "INCORRECT", false);
                        }
                        catch(Exception e)
                        {

                        }
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        catch (Exception exception)
        {
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_TRACING)
        {
             checkTrace();
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_TRACING)
        {
            drawingSurface.addLineToPoint(pt.x, pt.y);
        }
    }

    public void checkTraceStart(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        lockScreen();
        drawingSurface.setFrame(0,0,bounds().width(),bounds().height());
        drawingSurface.setPath(new Path());
        drawingSurface.moveToPoint(pt.x, pt.y);
        unlockScreen();
        setStatus(STATUS_TRACING);
     }


    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            checkTraceStart(pt);
        }

    }

}
