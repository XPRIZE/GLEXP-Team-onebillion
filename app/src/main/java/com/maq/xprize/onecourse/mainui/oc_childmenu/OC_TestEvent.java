package com.maq.xprize.onecourse.mainui.oc_childmenu;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OCM_FatController;
import com.maq.xprize.onecourse.utils.OC_FatController;

/**
 * Created by michal on 08/09/16.
 */
public class OC_TestEvent extends OC_SectionController
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
        final OC_SectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if (status() == STATUS_AWAITING_CLICK)
                {
                    OBControl targ = finger(0, 1, filterControls("button_.*"), pt);
                    if (targ != null)
                    {
                        setStatus(STATUS_BUSY);
                        targ.setFillColor(OBUtils.highlightedColour(targ.fillColor()));
                        OCM_FatController fatController = (OCM_FatController) MainActivity.mainActivity.fatController;

                        if (targ == objectDict.get("button_correct"))
                        {
                            gotItRight();
                            fatController.completeEvent(controller);
                        }
                        else
                        {
                            fatController.triggerTimeoutUnit();
                        }


                    }

                }

            }

        });

    }
}
