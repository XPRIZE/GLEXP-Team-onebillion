package org.onebillion.onecourse.utils;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OBSectionController;

/**
 * Created by alan on 27/02/16.
 */
public class OBFatController
{
    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON | OBMainViewController.SHOW_TOP_RIGHT_BUTTON | OBMainViewController.SHOW_BOTTOM_LEFT_BUTTON | OBMainViewController.SHOW_BOTTOM_RIGHT_BUTTON;
    }

    public void gotAnswerRight(int amount)
    {

    }

    public void gotAnswerWrong(int amount)
    {

    }

    public void initScores()
    {

    }

    public void updateScores()
    {

    }

    public void completeEvent(OBSectionController cont)
    {
        try
        {
            updateScores();
            cont.displayAward();
            cont.exitEvent();
        }
        catch (Exception e)
        {

        }
        cont.exitEvent();
    }


    public void startUp()
    {
        String menuClassName = (String)MainActivity.mainActivity.config.get("menuclass");
        if (menuClassName != null)
            MainActivity.mainViewController.pushViewControllerWithName(menuClassName,false,true,"menu");
    }

    public void onPause(OBSectionController cont)
    {

    }

    public void onResume(OBSectionController cont)
    {

    }

    public void onExitSection(OBSectionController cont)
    {

    }

}
