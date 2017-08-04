package org.onebillion.onecourse.mainui.oc_community;

import android.content.Intent;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_Menu;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OCM_FatController;
import org.onebillion.onecourse.utils.OCM_FatReceiver;

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
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        fatController = (OCM_FatController) MainActivity.mainActivity.fatController;
        fatController.menu = this;
        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("start_loc", OBMisc.copyPoint(presenter.control.position()));
        presenter.faceFront();
        presenter.control.setRight ( 0);
        presenter.control.setProperty("end_loc",OBMisc.copyPoint(presenter.control.position()));

    }
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if (!checkCurrentCommand())
                {
                    if (lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
                    {
                        demo_presenter_timeout();
                    }
                }
            }
        });
    }


    public boolean checkCurrentCommand()
    {
        lastCommand = (int)fatController.getCurrentCommand().get("code") ;
        if(lastCommand == OCM_FatController.OFC_BATTERY_LOW )
        {
            return false;

        }
        else if(lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
        {
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
        objectDict.get("foreground_night").hide();
        if (!checkCurrentCommand())
        {
            if (status() == STATUS_IDLE)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        demo_locked();
                    }
                });
            }
        }
    }

    @Override
    public void onAlarmReceived(Intent intent)
    {
        checkCurrentCommand();
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

    public void initScreen()
    {
        if(lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
        {
            loadNightSky(false);
            return;

        }

    }
    public void loadNightSky(boolean show)
    {
        if(show)
            showControls("night_.*");
        else
            hideControls("night_.*");

        lockScreen();
        showControls("night_.*");
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

        if(moonPhase == 0)
        {
            moon.hide();
            unlockScreen();
            return;
        }
        else
        {
            moon.show();
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
                    cover3.hide();
                else
                    cover2.hide();

                cover1.setRight(moonDisc.position().x);

                if(moonPhase < 8)
                    cover2.setScaleX(OB_Maths.easeout(1.0f-(moonPhase/8.0f)));
                else
                    cover3.setScaleX(OB_Maths.easeout((moonPhase-7.0f)/8.0f));
            }
            else
            {
                if(moonPhase > 22)
                    cover3.hide();
                else
                    cover2.hide();

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


    public void walkPresenterIn(PointF pt)
    {
        if(Math.abs(pt.x- presenter.control.position().x) < applyGraphicScale(10))
            return;
        if(presenter.control.hidden)
        {
            presenter.control.setPosition((PointF)presenter.control.propertyValue("end_loc"));
            presenter.control.show();
        }
        presenter.walk(pt);
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

    public void demo_presenter_timeout() throws Exception
    {
        setStatus(STATUS_BUSY);
        playSfxAudio("chime",true);
        walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"));
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getAudioForScene("timeout","DEMO"),this);
        waitForSecs(0.3f);
        walkPresenterOut();
        animateNightSkyOn();
        animateNightSky();
        OBSystemsManager.sharedManager.screenLock();
        setStatus(STATUS_IDLE);
    }

    public void demo_locked() throws Exception
    {
        setStatus(STATUS_BUSY);
        animateNightSky();
        OBSystemsManager.sharedManager.screenLock();
        setStatus(STATUS_IDLE);
    }



}
