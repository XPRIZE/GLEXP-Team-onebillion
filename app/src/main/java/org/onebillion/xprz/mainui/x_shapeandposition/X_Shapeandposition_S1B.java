package org.onebillion.xprz.mainui.x_shapeandposition;

import android.os.AsyncTask;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.XPRZ_SectionController;

import java.util.Arrays;

/**
 * Created by alan on 25/08/16.
 */
public class X_Shapeandposition_S1B extends XPRZ_SectionController
{
    public static int NO_DOTS_SHOWING = 0,
            DOT1_SHOWING = 1,
            DOT2_SHOWING = 2,
            BOTH_DOTS_SHOWING = 3;
    OBControl firstDot,secondDot;
    int dotMode;

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("masterb");
        OBPath line = (OBPath) objectDict.get("line");
        line.setZPosition(20);
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
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


}
