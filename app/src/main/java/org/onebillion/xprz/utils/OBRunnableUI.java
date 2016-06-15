package org.onebillion.xprz.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by alan on 26/12/15.
 */
public abstract class OBRunnableUI implements Runnable
{
    @Override
    public void run()
    {
        if(Looper.myLooper() == Looper.getMainLooper())
            ex();
        else
        {
            new Handler(Looper.getMainLooper()).post(
                    new Runnable()
                    {
                        public void run()
                        {
                            ex();
                        }
                    });
        }
    }

    public abstract void ex();

}
