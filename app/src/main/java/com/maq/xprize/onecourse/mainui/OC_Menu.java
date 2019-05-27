package com.maq.xprize.onecourse.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.utils.OBConfigManager;
import com.maq.xprize.onecourse.utils.OBFatController;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OCM_FatController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 06/12/15.
 */
public class OC_Menu extends OC_SectionController
{
    String saveConfig;
    String saveLanguage;

    public void prepare()
    {
        if(params == null)
            params = "menu";
        //
        super.prepare();
        //
        saveConfig = OBConfigManager.sharedManager.getCurrentActivityFolder();
        saveLanguage = OBConfigManager.sharedManager.getCurrentLanguage();
        //
        loadEvent("main");
        for (OBControl but : filterControls("but.*"))
        {
            if (but instanceof OBGroup)
            {
                ((OBGroup) but).outdent(applyGraphicScale(8));
            }
            //
            String labelValue = (String) but.attributes().get("label");
            if (labelValue != null)
            {
                OBControl background = new OBControl();
                background.setFrame(but.frame());
                background.setPosition(OC_Generic.copyPoint(but.position()));
                background.setBackgroundColor(Color.WHITE);
                background.setBorderColor(Color.BLUE);
                background.setBorderWidth(2.0f);
                background.setCornerRadius(20.0f);
                background.setZPosition(5.0f);
                background.disable();
                attachControl(background);
                //
                OBLabel label = OC_Generic.action_createLabelForControl(but, labelValue, 0.9f, false, OBUtils.standardTypeFace(), Color.BLACK, this);
                label.setZPosition(6.0f);
                label.disable();
                label.sizeToBoundingBox();
                attachControl(label);
            }
        }
        for (OBControl c : attachedControls)
            c.texturise(false,this);

        boolean permission1 = MainActivity.mainActivity.isAllPermissionGranted();
        //
        // Should you need to use the OCM_FatController for a normal build, this is how to prevent the constant session invalidation
        /*
        OBFatController fatcontroller = MainActivity.mainActivity.fatController;
        if (fatcontroller instanceof OCM_FatController)
        {
            MainActivity.log("OC_Menu. Session starting");
            OCM_FatController ocmfatcontroller = (OCM_FatController) fatcontroller;
            ocmfatcontroller.startCurrentSession();
        }
        */
    }

    public int buttonFlags()
    {
        List<OBSectionController> arr = MainActivity.mainViewController.viewControllers;
        if (arr.indexOf(this) > 0)
            return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
        return 0;
    }

    public void setSceneXX()
    {

    }

    /*public void setButtons()
    {
        Drawable d = OBImageManager.sharedImageManager().imageNamed("backNormal");
        MainActivity.mainViewController.topLeftButton.setImageDrawable(d);
        d = OBImageManager.sharedImageManager().imageNamed("replayAudioNormal");
        MainActivity.mainViewController.topRightButton.setImageDrawable(d);
    }*/

    @Override
    public void viewWillAppear (Boolean animated)
    {
        super.viewWillAppear(animated);
        for (OBControl c : filterControls("button.*"))
            c.lowlight();
        if (saveConfig != null)
        {
            OBConfigManager.sharedManager.updateConfigPaths(saveConfig, false, saveLanguage);
        }
        //setButtons();
    }

    public void start()
    {
        super.start();
        setStatus(STATUS_IDLE);
    }

    OBControl findTarget(PointF pt)
    {
        for (OBControl c : sortedFilteredControls("button.*"))
        {
            if (c.containsPoint(pt))
                return c;
        }
        return null;
    }

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() != STATUS_IDLE)
            return;
        OBControl c = findTarget(pt);
        if (c != null)
        {
            Map<String,Object> attrs = (Map<String,Object>)c.propertyValue("attrs");
            final String target = (String)attrs.get("target");
            if (target != null)
            {
                final String parm = (String)attrs.get("parm");
                final String lang = (String)attrs.get("lang");
                setStatus(STATUS_BUSY);
                c.highlight();
                String configName = (String)attrs.get("config");
                if (configName == null)
                {
                    String appDir = OBConfigManager.sharedManager.getCurrentActivityFolder();
                    String[] comps = appDir.split("/");
                    configName = comps[0];
                }
                else
                {
                    OBConfigManager.sharedManager.updateConfigPaths(configName, false,lang);
                }
                if (!MainActivity.mainViewController.pushViewControllerWithNameConfig(target,configName,true,true,parm))
                    setStatus(STATUS_IDLE);
                c.lowlight();
            }
        }
    }

}
