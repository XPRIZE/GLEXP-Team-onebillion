package com.maq.xprize.onecourse.hindi.utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.Looper;

public abstract class OBRunnableSyncUI implements Runnable
{
    final ReentrantLock lock = new ReentrantLock();
    final Condition cond = lock.newCondition();
    Boolean done;

    @Override
    public void run ()
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            ex();
            return;
        }
        done = false;
        new Handler(Looper.getMainLooper()).post(
                new Runnable()
                {
                    public void run ()
                    {
                        lock.lock();
                        ex();
                        done = true;
                        cond.signalAll();
                        lock.unlock();
                    }
                });
        lock.lock();
        while (!done)
        {
            try
            {
                cond.await();
            }
            catch (InterruptedException e)
            {
            }
        }
        lock.unlock();
    }

    abstract public void ex ();
}
