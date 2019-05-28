package com.maq.xprize.onecourse.hindi.mainui;

import com.maq.xprize.onecourse.hindi.utils.OBBrightnessManager;
import com.maq.xprize.onecourse.hindi.utils.OBConfigManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

public class OC_SplashScreen extends OBSectionController
{

    public OC_SplashScreen()
    {
        super(MainActivity.mainActivity, true);
    }

    public void prepare()
    {
        setStatus(STATUS_IDLE);
    }

    @Override
    public int buttonFlags()
    {
        return 0;
    }

    public void start()
    {
        OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                OBUtils.runOnMainThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        String menuClassName = OBConfigManager.sharedManager.getMenuClassName();
                        String appCode = OBConfigManager.sharedManager.getCurrentActivityFolder();
                        //
                        if (menuClassName != null && appCode != null)
                        {
                            OBBrightnessManager.sharedManager.onContinue();
                            if (!MainViewController().pushViewControllerWithNameConfig(menuClassName, appCode, false, true, null, true))
                            {
                                MainActivity.log("Error loading " + menuClassName + " " + appCode);
                            }
                        }
                    }
                });
            }
        });
    }

}
