package org.onebillion.onecourse.controls;

import android.graphics.Canvas;
import android.os.AsyncTask;

import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OBViewController;
import org.onebillion.onecourse.utils.OBRunnableSyncUI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alan on 19/02/16.
 */
public class OBEmitter extends OBControl
{
    public List<OBEmitterCell>cells;
    private boolean running,birthCells;
    protected Lock emitterLoc;
    protected Condition condition;

    public OBEmitter()
    {
        super();
        emitterLoc = new ReentrantLock();
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
                    stillGoing = ec.doCycle(birthCells()) || stillGoing;
                setRunning(stillGoing);
                invalidate();
            }
        }.run();
    }

    public synchronized void setRunning(boolean b)
    {
        running = b;
        if(!b)
            finishWait();
    }

    public synchronized boolean running()
    {
        return running;
    }

    private synchronized void setBirthRate(boolean b)
    {
        birthCells = b;
    }

    private synchronized boolean birthCells()
    {
        return birthCells;
    }

    public void stop()
    {
        setBirthRate(false);
    }

    private void start()
    {
        condition = emitterLoc.newCondition();
        setRunning(true);
        setBirthRate(true);
        for (OBEmitterCell ec : cells)
            ec.start(this);
    }

    public void drawLayer(Canvas canvas, int flags)
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
                    if(controller != null)
                    {
                        OBSectionController c = (OBSectionController) controller;
                        c.waitForSecsNoThrow(0.02);
                    }
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

    public void cleanUp()
    {
        stop();
        setRunning(false);
        controller = null;
    }

    public void waitForCells()
    {
        if(condition == null)
            return;

        emitterLoc.lock();
        try
        {
            condition.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            emitterLoc.unlock();

        }
    }

    private void finishWait()
    {
        if(condition == null)
            return;

        emitterLoc.lock();
        try
        {
            condition.signalAll();
        }
        finally
        {
            emitterLoc.unlock();

        }
        condition = null;

    }


}
