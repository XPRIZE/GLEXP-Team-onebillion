package org.onebillion.onecourse.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBEmitter;
import org.onebillion.onecourse.controls.OBEmitterCell;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OC_FatController;
import org.onebillion.onecourse.utils.OC_FatReceiver;
import org.onebillion.onecourse.utils.ULine;
import org.onebillion.onecourse.utils.UPath;
import org.onebillion.onecourse.utils.USubPath;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 06/12/2016.
 */

public class OC_LoopMenu extends OC_Menu implements OC_FatReceiver
{
    int lastCommand, currentLevel;
    OBImage bigIcon;
    OC_FatController fatController;
    long startIndex;
    MlUnit lastUnit;
    OBLabel currentLevelLabel;


    @Override
    public String sectionName()
    {
        return "menu";
    }

    @Override
    public String sectionAudioName()
    {
        return "menu";
    }

    @Override
    public void receiveCommand(Map<String,Object> params)
    {
        if(params != null && params.get("code") != null)
        {
            lastCommand = (int)params.get("code");
            lastUnit = (MlUnit)params.get("unit");
        }
        else
        {
            lastCommand = -1;
        }

        if((lastCommand == OC_FatController.OFC_SUCCEEDED ||
                lastCommand == OC_FatController.OFC_FINISHED_LOW_SCORE) && bigIcon != null)
        {
            detachControl(bigIcon);
            bigIcon = null;
        }

    }


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();

        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(60);
        currentLevelLabel = new OBLabel("88888888888888888",font,fontSize);
        currentLevelLabel.setZPosition(100);
        currentLevelLabel.setString("1");
        currentLevelLabel.setPosition(OB_Maths.locationForRect(0.5f,0.1f,this.bounds()));
        currentLevelLabel.setColour(Color.BLACK);
        attachControl(currentLevelLabel);


        lastCommand = 0;
        currentLevel = 1;
        fatController = (OC_FatController)MainActivity.mainActivity.fatController;
        fatController.menu = this ;
        startIndex = fatController.getLastUnitIndex()-1;

    }

    @Override
    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON|OBMainViewController.SHOW_TOP_RIGHT_BUTTON;
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {

                 if(lastCommand == 0 || lastCommand == OC_FatController.OFC_SUCCEEDED || lastCommand == OC_FatController.OFC_FINISHED_LOW_SCORE)
                {
                    lastCommand = -1;
                    if(prepareNextEvent())
                    {
                        try
                        {
                            MlUnit unit = (MlUnit) bigIcon.settings.get("unit");
                            setCurrentLevel(unit);

                            demoNextIcon();
                            waitForSecs(0.3);
                            bigIcon.highlight();
                            waitForSecs(0.3);
                            startSectionForUnit(unit);
                            waitForSecs(1);
                            fatController.completeEvent3();
                        }catch (Exception e)
                        {
                            MainActivity.log(e.getLocalizedMessage());
                        }

                    }

                }

            }
        });

    }

    public boolean isFirstEvent()
    {
        return fatController.getLastUnitIndex() == -1;
    }


    public void setCurrentLevel(MlUnit unit)
    {
        String xmlid = unit.key.split("\\.")[0];
        String postfix = String.format("   %d", unit.level);
        String fullString = String.format("%s%s", xmlid,postfix);
        currentLevelLabel.setString(fullString);
        int st = fullString.lastIndexOf(postfix);
        currentLevelLabel.setHighRange(st, st + postfix.length(),Color.GREEN);

        currentLevel = unit.level;
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {

    }


    public void startSectionForUnit(MlUnit unit)
    {
        OBSystemsManager.sharedManager.printMemoryStatus("Starting Unit " + unit.unitid);
        //
        fatController.startSectionByUnit(unit);
    }



    public void loadBigIconForUnit(MlUnit unit)
    {
        lockScreen();

        String imgName = String.format("%s", unit.icon);
        String mlname = OBConfigManager.sharedManager.getMasterlist();
        //
        if (OBImageManager.sharedImageManager().getImgPath(imgName) != null)
            bigIcon = loadImageWithName(imgName, new PointF(0, 0), new RectF(bounds()));
        else
            bigIcon = loadImageWithName("icon_default_big", new PointF(0, 0), new RectF(bounds()));

        bigIcon.setScale(applyGraphicScale(1));
        bigIcon.setRasterScale(applyGraphicScale(1));
        //bigIcon= new OBGroup(Collections.singletonList(bigic));
        // bigIcon.objectDict.put("icon",bigic);
        bigIcon.setProperty("unit",unit);


        bigIcon.setOpacity(0);
        bigIcon.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
        bigIcon.show();
        //bigIcon.setScale(1.1f);
        bigIcon.setZPosition(20);
        setObjectShadow(bigIcon,1.5f);
        attachControl(bigIcon);
        unlockScreen();
    }

    public boolean prepareNextEvent()
    {
        MlUnit unit = (MlUnit)fatController.requestNextUnit();
        if(unit != null)
        {
            loadBigIconForUnit(unit);
            return true;
        }
        return false;
    }


    public void setObjectShadow(OBControl obj, float scale)
    {
        OBControl shadow = objectDict.get("shadow");
        obj.setShadow(shadow.getShadowRadius(),shadow.getShadowOpacity(),shadow.getShadowOffsetY()*scale,shadow.getShadowOffsetY()*scale,shadow.getShadowColour());
    }


    public void animateBigIconShow() throws Exception
    {
        float startScale = bigIcon.scale();
        bigIcon.setScale(startScale * 1.1f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(startScale,bigIcon), OBAnim.opacityAnim(1,bigIcon)),0.2,true,OBAnim.ANIM_EASE_OUT,this);
    }


    public void presenterSpeak(List<String> audioFiles) throws Exception
    {
        playAudioQueued(OBUtils.insertAudioInterval(audioFiles,300),true);
    }

    public void demoNextIcon() throws Exception
    {
        if(bigIcon != null)
        {
            waitForSecs(0.3f);

            animateBigIconShow();




        }
    }


}
