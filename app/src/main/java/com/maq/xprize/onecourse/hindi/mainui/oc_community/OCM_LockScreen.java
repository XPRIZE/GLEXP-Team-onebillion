package com.maq.xprize.onecourse.hindi.mainui.oc_community;

import android.content.Intent;
import android.graphics.PointF;
import android.support.v4.app.NotificationCompatSideChannelService;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPresenter;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.mainui.OC_Menu;
import com.maq.xprize.onecourse.hindi.utils.OBAnalytics;
import com.maq.xprize.onecourse.hindi.utils.OBAnalyticsManager;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBSystemsManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.OCM_FatController;
import com.maq.xprize.onecourse.hindi.utils.OCM_FatReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 03/08/2017.
 */

public class OCM_LockScreen extends OC_Menu implements OCM_FatReceiver
{
    OCM_FatController fatController;
    OBPresenter presenter;
    int lastCommand;
    boolean isCharging;

    @Override
    public String sectionName()
    {
        return "menu_lock";

    }
    @Override
    public String sectionAudioName()
    {
        return "szlock";

    }
    @Override
    public int buttonFlags()
    {
        return 0;

    }


    @Override
    public void receiveCommand(Map<String, Object> dict)
    {

    }

    public void prepare()
    {
        fatController = (OCM_FatController) MainActivity.mainActivity.fatController;
        fatController.menu = this;

        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        lastCommand = (int)fatController.getCurrentCommand().get("code");
        loadEvent("master");

        isCharging = OBSystemsManager.sharedManager.isBatteryCharging();

        if(lastCommand == OCM_FatController.OFC_BATTERY_LOW)
        {
            loadEvent("battery");
            OBControl cable = objectDict.get("cable");
            cable.setProperty("dest_loc", OBMisc.copyPoint(cable.position()));

            if(isCharging)
            {
                setTabletStatus(0);
            }
            else
            {
                cable.setRight(0);
                setTabletStatus(1);
            }
        }
        else
        {
            loadEvent("lock");
            loadNightSky(false);
        }


        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("start_loc", OBMisc.copyPoint(presenter.control.position()));
        presenter.faceFront();
        if(lastCommand == OCM_FatController.OFC_BATTERY_LOW)
        {
            presenter.control.setLeft(this.boundsf().right);
        }
        else
        {
            presenter.control.setRight(0);
        }
        presenter.control.setProperty("end_loc",OBMisc.copyPoint(presenter.control.position()));

    }
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                     waitForSecs(1);
                    if (lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
                    {
                        demo_presenter_locked();
                    }
                    else if((lastCommand == OCM_FatController.OFC_BATTERY_LOW))
                    {
                        if(isCharging)
                            demo_battery();
                        else
                            demo_presenter_battery();
                    }


            }
        });
    }


    public boolean checkCurrentCommand()
    {
        int curCommand = (int)fatController.getCurrentCommand().get("code");
        if(curCommand == OCM_FatController.OFC_BATTERY_LOW)
        {
            if(lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
            {
                OBAnalyticsManager.sharedManager.enteredScreen(OBAnalytics.Screen.LOW_BATTERY_SCREEN);
                closeThisMenuAndOpen(OCM_LockScreen.class);
                return true;
            }
            lastCommand=curCommand;
            return false;

        }
        else if(curCommand == OCM_FatController.OFC_SESSION_LOCKED)
        {
            if(lastCommand == OCM_FatController.OFC_BATTERY_LOW)
            {
                OBAnalyticsManager.sharedManager.enteredScreen(OBAnalytics.Screen.LOCK_SCREEN);
                closeThisMenuAndOpen(OCM_LockScreen.class);
                return true;
            }
            lastCommand=curCommand;
            return false;
        }
        else
        {
            closeThisMenuAndOpen(OCM_ChildMenu.class);
            return true;
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!checkCurrentCommand())
        {
            if (status() == STATUS_IDLE)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        waitForSecs(1);
                        if(lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
                            demo_locked();
                        else if(lastCommand == OCM_FatController.OFC_BATTERY_LOW)
                            demo_battery();
                    }
                });
            }
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (status() == STATUS_BUSY)
        {

        }
    }

    @Override
    public void onAlarmReceived(Intent intent)
    {
        super.onAlarmReceived(intent);
        checkCurrentCommand();
    }

    @Override
    public void onBatteryStatusReceived(float level, boolean charging)
    {
        super.onBatteryStatusReceived(level, charging);
        if(lastCommand == OCM_FatController.OFC_BATTERY_LOW && charging != isCharging)
        {
            closeThisMenuAndOpen(OCM_LockScreen.class);
        }
        else
        {
            checkCurrentCommand();
        }
    }

    public void closeThisMenuAndOpen(final Class<?> vcClass)
    {
        setStatus(STATUS_EXITING);
        killAnimations();
        playAudio(null);
        _aborting = true;
        fatController.menu = null;
        fatController.colourDict = null;
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                stopAllAudio();
                MainViewController().pushViewController(vcClass,false,false,null,true);
            }
        });
    }

    public void loadNightSky(boolean show)
    {
        if(show)
            showControls("night_.*");
        else
            hideControls("night_.*");

        lockScreen();
        int moonPhase = fatController.getCurrentMoonPhase();
        OBGroup moon = (OBGroup)objectDict.get("night_moon");
        moon.setRotation((float)Math.toRadians(70));
        OBControl cover3 = moon.objectDict.get("cover_3"); //moon colour
        OBControl cover2 = moon.objectDict.get("cover_2"); //night sky colour
        OBControl cover1 = moon.objectDict.get("cover_1"); // half moon
        OBControl moonDisc = moon.objectDict.get("moon");
        moon.showMembers("cover_.*");
        cover3.setScaleX(1);
        cover2.setScaleX(1);
        cover3.setOpacity(1);
        cover2.setOpacity(1);

        if(moonPhase == 0)
        {
            moon.setOpacity(0);
            unlockScreen();
            return;
        }
        else
        {
            moon.setOpacity(1);
        }

        if(moonPhase == 15)
        {
            moon.hideMembers("cover_.*");
        }
        else
        {
            if(moonPhase < 15)
            {
                if(moonPhase < 8)
                    cover3.setOpacity(0);
                else
                    cover2.setOpacity(0);

                cover1.setRight(moonDisc.position().x);

                if(moonPhase < 8)
                    cover2.setScaleX(OB_Maths.easeout(1.0f-(moonPhase/8.0f)));
                else
                    cover3.setScaleX(OB_Maths.easeout((moonPhase-7.0f)/8.0f));
            }
            else
            {
                if(moonPhase > 22)
                    cover3.setOpacity(0);
                else
                    cover2.setOpacity(0);

                cover1.setLeft(moonDisc.position().x);

                if(moonPhase > 22)
                    cover2.setScaleX(OB_Maths.easeout((moonPhase-22.0f)/8.0f));
                else
                    cover3.setScaleX(OB_Maths.easeout(1.0f-((moonPhase-15.0f)/8.0f)));
            }
        }

        for(OBControl star : filterControls("night_star_.*"))
            star.setOpacity(0.2f + 0.8f*(OB_Maths.randomInt(1,10)/10.0f));
        unlockScreen();
    }

    public void animateNightStarsTwinkle()
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl star : filterControls("night_star_.*"))
        {
            float targetOpacity = 1;
            if(star.opacity() <= 0.5)
                targetOpacity = 0.55f + 0.45f*(OB_Maths.randomInt(1,10)/10.0f);
            else
                targetOpacity = 0.5f - 0.4f*(OB_Maths.randomInt(1,10)/10.0f);

            anims.add(OBAnim.opacityAnim(targetOpacity,star));
        }
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_LINEAR,this);
    }

    public void animateNightSky()
    {
        for(int i=0; i<3; i++)
            animateNightStarsTwinkle();
    }

    public void animateNightSkyOn()
    {
        List<OBAnim> anims = new ArrayList<>();
        OBControl bg=objectDict.get("background_night");
        lockScreen();
        showControls("night_.*");
        for(OBControl con : filterControls("night_.*"))
        {
            anims.add(OBAnim.opacityAnim(con.opacity(),con));
            con.setOpacity(0);
        }
        bg.show();
        bg.setOpacity(0);

        unlockScreen();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1,bg)),0.7
                ,true,OBAnim.ANIM_LINEAR,this);
        OBAnimationGroup.runAnims(anims,0.7
                ,true,OBAnim.ANIM_LINEAR,this);
    }


    public void walkPresenterIn(PointF pt, boolean flipped)
    {
        if(Math.abs(pt.x- presenter.control.position().x) < applyGraphicScale(10))
            return;
        if(presenter.control.hidden)
        {
            presenter.control.setPosition((PointF)presenter.control.propertyValue("end_loc"));
            presenter.control.show();
        }
        presenter.walk(pt);

        if(flipped)
            presenter.faceFrontReflected();
        else
            presenter.faceFront();
    }

    public void walkPresenterOut()
    {
        presenter.walk((PointF)presenter.control.propertyValue("end_loc"));
        presenter.control.hide();

    }

    public void resetPresenter()
    {
        presenter.control.setPosition((PointF)presenter.control.propertyValue("end_loc"));
        presenter.selectArmIndex(0);
        presenter.control.hide();
    }

    public void demo_presenter_locked() throws Exception
    {
        setStatus(STATUS_BUSY);
        playSfxAudio("chime",true);
        walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"), false);
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getAudioForScene("lock","DEMO"),this);
        waitForSecs(0.3f);
        walkPresenterOut();
        animateNightSkyOn();
        animateNightSky();
        try
        {
            if(!this._aborting)
                OBSystemsManager.sharedManager.screenLock();
        }catch (Exception e)
        {

        }
        setStatus(STATUS_IDLE);
    }

    public void demo_locked() throws Exception
    {
        setStatus(STATUS_BUSY);
        animateNightSky();
        try
        {
            if(!this._aborting)
                OBSystemsManager.sharedManager.screenLock();
        }catch (Exception e)
        {

        }
        setStatus(STATUS_IDLE);
    }


    public void demo_presenter_battery() throws Exception
    {
        long time = setStatus(STATUS_BUSY);
        //playSfxAudio("chime",true);
        walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"), true);
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getAudioForScene("battery","DEMO"),this);
        waitForSecs(0.3f);
        presenter.moveHandfromIndex(0,4,0.5);
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                presenter.speak((List<Object>)(Object)getAudioForScene("battery","DEMO2"),controller);
            }
        });

        animateCable();

        walkPresenterOut();
        try
        {
            if(!this._aborting)
                OBSystemsManager.sharedManager.screenLock();
        }catch (Exception e)
        {

        }
        setTabletStatus(-1);
        setStatus(STATUS_IDLE);
    }

    public void demo_battery() throws Exception
    {
        setStatus(STATUS_BUSY);
        if(isCharging)
        {
            setTabletStatus(0);
            animateCharging();
        }
        else
        {
            setTabletStatus(1);
            animateCable();
        }

        try
        {
            if(!this._aborting)
                OBSystemsManager.sharedManager.screenLock();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        setTabletStatus(-1);
        setStatus(STATUS_IDLE);
    }

    public void animateCable() throws Exception
    {
        OBControl cable = objectDict.get("cable");
        for (int i = 0; i < 3; i++)
        {
            cable.setRight(0);
            cable.show();
            PointF targetLoc = (PointF) cable.propertyValue("dest_loc");
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(targetLoc, cable)), 2f, true, OBAnim.ANIM_EASE_OUT, this);
            waitForSecs(1);
        }
    }

    public void animateCharging() throws Exception
    {
        OBControl cable = objectDict.get("cable");
        cable.setPosition((PointF) cable.propertyValue("dest_loc"));
        cable.show();
        OBGroup tablet = (OBGroup)objectDict.get("tablet");
        OBControl chargingLight = tablet.objectDict.get("level_0");
        for (int i = 0; i < 3; i++)
        {
            chargingLight.hide();
            waitForSecs(0.5);
            chargingLight.show();
            waitForSecs(0.5);
        }
    }

    public void setTabletStatus(int status)
    {
        if(objectDict.containsKey("tablet"))
        {
            lockScreen();
            OBGroup tablet = (OBGroup)objectDict.get("tablet");
            tablet.hideMembers("level_.*");
            if(status >= 0)
                tablet.objectDict.get(String.format("level_%d",status)).show();
            unlockScreen();
        }
    }



}
