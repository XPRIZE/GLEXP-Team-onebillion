package org.onebillion.xprz.mainui.x_miniapp6;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.XPRZ_FatController;

/**
 * Created by michal on 08/09/16.
 */
public class X_TestEvent extends XPRZ_SectionController
{
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                setStatus(STATUS_AWAITING_CLICK);

            }

        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);

    }

    public void doMainXX()
    {

    }


    public void touchDownAtPoint(final PointF pt, View v)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if (status() == STATUS_AWAITING_CLICK)
                {
                    OBControl targ = finger(0, 1, filterControls("button_.*"), pt);
                    if (targ != null)
                    {
                        targ.setFillColor(OBUtils.highlightedColour(targ.fillColor()));
                        XPRZ_FatController fatController = (XPRZ_FatController) MainActivity.mainActivity.fatController;
                        fatController.updateScores();
                        if (targ == objectDict.get("button_correct"))
                        {
                            fatController.signalSectionSucceeded();

                        } else if (targ == objectDict.get("button_wrong"))
                        {
                            fatController.signalUnitTimedOut();

                        } else
                        {
                            fatController.signalSessionTimedOut();

                        }
                        exitEvent();

                    }

                }

            }

        });

    }
}
