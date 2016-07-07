package org.onebillion.xprz.controls;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.AsyncTask;

import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.OBViewController;
import org.onebillion.xprz.utils.OBRunnableSyncUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 19/02/16.
 */
public class OBEmitter extends OBControl
{
    boolean running;
    public List<OBEmitterCell>cells;
    public OBEmitter()
    {
        super();
        cells = new ArrayList<>();
    }
    public void doCycle()
    {
        new OBRunnableSyncUI()
        {
            public void ex()
            {
                boolean stillGoing = false;
                for (OBEmitterCell ec : cells)
                    stillGoing = stillGoing || ec.doCycle();
                setRunning(stillGoing);
                invalidate();
            }
        }.run();
    }
    public synchronized void setRunning(boolean b)
    {
        running = b;
    }
    public synchronized boolean running()
    {
        return running;
    }
    public void stop()
    {
        for (OBEmitterCell ec : cells)
            ec.birthRate = 0;
    }
    public void start()
    {
        setRunning(true);
        for (OBEmitterCell ec : cells)
            ec.start();
    }
    public void drawLayer(Canvas canvas)
    {
        for (OBEmitterCell ec : cells)
            ec.draw(canvas);
    }
    public void run()
    {
        start();
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                while (running())
                {
                    doCycle();
                    OBSectionController c = (OBSectionController) controller;
                    c.waitForSecsNoThrow(0.02);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void render(OBRenderer renderer, OBViewController vc, float[] modelViewMatrix)
    {
        for (OBEmitterCell ec : cells)
            ec.render(renderer,vc,modelViewMatrix);
    }

}
