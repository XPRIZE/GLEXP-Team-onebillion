package org.onebillion.onecourse.utils;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OBSectionController;

import static org.onebillion.onecourse.mainui.MainActivity.PREFERENCES_SETUP_COMPLETE;

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
        String isSetupComplete = MainActivity.mainActivity.getPreferences(MainActivity.PREFERENCES_SETUP_COMPLETE);
        //
        if (isSetupComplete == null || !isSetupComplete.equals("true"))
        {
            MainActivity.mainActivity.updateConfigPaths(MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_SETUP_FOLDER), true, null);
            //
            String setupClassName = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_SETUP_CLASS);
            if (setupClassName != null)
            {
                MainActivity.mainViewController.pushViewControllerWithName(setupClassName, false, true, "menu");
            }
        }
        else
        {
            MainActivity.mainActivity.updateConfigPaths(MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_APP_CODE), true, null);
            //
            String menuClassName = MainActivity.mainActivity.configStringForKey(MainActivity.CONFIG_MENU_CLASS);
            if (menuClassName != null)
            {
                MainActivity.mainViewController.pushViewControllerWithName(menuClassName, false, true, "menu");
            }
        }
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
