package org.onebillion.onecourse.utils;

import android.os.Handler;

public class OBTimer
{
    private Runnable timerRunnable;
    private Handler timerHandler = new Handler();
    float delay;
    public OBTimer(float delaysecs,Runnable runner)
    {
        delay = delaysecs;
        timerRunnable = runner;
    }

    public void timerEvent()
    {

    }
    public void scheduleTimerEvent()
    {
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable,(int)(delay * 1000));
    }
}