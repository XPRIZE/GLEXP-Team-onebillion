package com.maq.xprize.onecourse.utils;

import android.os.Handler;

public abstract class OBTimer
{
    private Runnable timerRunnable;
    private Handler timerHandler = new Handler();
    float delay;
    boolean valid = true;
    public OBTimer(float delaysecs)
    {
        delay = delaysecs;
    }

    public abstract int timerEvent(OBTimer timer);

    public void scheduleTimerEvent()
    {
        if (timerRunnable == null)
        {
            final OBTimer thistimer = this;
            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (timerEvent(thistimer) > 0 && valid)
                        scheduleTimerEvent();
                }
            };
        }
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable,(int)(delay * 1000));
    }

    public void invalidate()
    {
        valid = false;
    }
}