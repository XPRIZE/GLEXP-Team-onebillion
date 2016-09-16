package org.onebillion.xprz.mainui.x_miniapp5;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.XPRZ_Menu;
import org.onebillion.xprz.utils.MlUnit;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBSystemsManager;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.ULine;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.USubPath;
import org.onebillion.xprz.utils.XPRZ_FatController;
import org.onebillion.xprz.utils.XPRZ_FatReceiver;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 17/08/16.
 */
public class X_miniapp5_menu extends XPRZ_Menu implements XPRZ_FatReceiver
{
    OBEmitter emitter;
    int lastCommand, currentLevel;
    OBImage bigIcon;
    XPRZ_FatController fatController;
    long startIndex;
    MlUnit lastUnit;
    String currentAudio;
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
        currentAudio="default";

        if(params != null && params.get("code") != null)
        {
            lastCommand = (int)params.get("code");
            lastUnit = (MlUnit)params.get("unit");
        }
        else
        {
            lastCommand = -1;
        }

        if((lastCommand == XPRZ_FatController.OFC_SUCCEEDED || lastCommand == XPRZ_FatController.OFC_FINISHED_LOW_SCORE) && bigIcon != null)
        {
            detachControl(bigIcon);
            bigIcon = null;
        }

    }

    public void replayAudio()
    {
        if(status() != STATUS_BUSY)
        {
            final List<String> audio = getSceneAudio("PROMPT.REPEAT");
            if(audio != null && audio.size()>0)
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        setStatus(status());
                        playAudio(null);
                        presenterSpeak(audio);
                    }
                });
        }
        else
        {
            playAudio(null);
        }
    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();

        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(60);
        currentLevelLabel = new OBLabel("8888888888",font,fontSize);
        currentLevelLabel.setString("1");
        currentLevelLabel.setPosition(OB_Maths.locationForRect(0.95f,0.95f,this.bounds()));
        currentLevelLabel.setRight(bounds().width() - applyGraphicScale(10));
        currentLevelLabel.setColour(Color.BLACK);
        attachControl(currentLevelLabel);

        loadEmitter();

        lastCommand = 0;
        currentLevel = 1;
        fatController = (XPRZ_FatController)MainActivity.mainActivity.fatController;
        fatController.menu = this ;
        startIndex = fatController.getLastUnitId()-1;
        currentAudio="1";

        OBGroup star = (OBGroup)objectDict.get("complete_star");
        setObjectShadow(star, 1f);
        star.setZPosition (200);
        star.setProperty("start_scale",star.scale());
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
                int previousLevel = currentLevel;

                if(lastCommand == 0)
                {
                    lastCommand = -1;
                    prepareNextEvent();
                    MlUnit unit = (MlUnit)bigIcon.settings.get("unit");
                    setCurrentLevel(unit);
                    demoPresenter();
                }
                else if(lastCommand == XPRZ_FatController.OFC_SUCCEEDED || lastCommand == XPRZ_FatController.OFC_FINISHED_LOW_SCORE)
                {
                    lastCommand = -1;
                    if(prepareNextEvent())
                    {
                        MlUnit unit = (MlUnit)bigIcon.settings.get("unit");
                        setCurrentLevel(unit);
                        if(previousLevel == currentLevel)
                        {
                            demoNextIcon();
                        }
                        else
                        {
                            currentAudio="level";
                            demoStar();
                        }
                    }
                    else
                    {
                        demoStar2();
                    }

                }
                else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
        });

    }

    public boolean isFirstEvent()
    {
        return fatController.getLastUnitId() == -1;
    }

    public void waitAndRemind(long time) throws Exception
    {
        waitForSecs(5f);
        List<String> arr = getSceneAudio("REMINDER");
        if(time == statusTime && arr!=null && arr.size()>0)
            presenterSpeak(arr);
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
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(status() == STATUS_AWAITING_CLICK)
                {
                    if(bigIcon != null && bigIcon.frame.contains(pt.x, pt.y))
                    {
                        setStatus(STATUS_BUSY);
                        setEmitter(false);
                        bigIcon.highlight();
                        MlUnit unit = (MlUnit)bigIcon.settings.get("unit");
                        startSectionForUnit(unit);
                        playAudio(null);
                        waitForSecs(0.5f);
                        bigIcon.lowlight();
                    }
                }

            }
        });
    }


    public void startSectionForUnit(MlUnit unit)
    {
        OBSystemsManager.sharedManager.printMemoryStatus("Starting Unit " + unit.unitid);
        //
        fatController.startSectionByUnit(unit);
    }

    public List<String> getSceneAudio(String audio)
    {
        return getAudioForScene(currentAudio,audio);

    }

    public void playSceneAudio(String name,boolean wait) throws Exception
    {
        setReplayAudio(OBUtils.insertAudioInterval(getSceneAudio(String.format("%s.REPEAT",name)),300));
        playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio(name),300));
    }


    public void loadEmitter()
    {
        emitter = new OBEmitter();
        emitter.setBounds(0,0,64,64);
        emitter.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
        emitter.cells.addAll(Arrays.asList(emitterCell(0,OBUtils.colorFromRGBString("46,190,44")),
        emitterCell(1,OBUtils.colorFromRGBString("255,207,0")),
        emitterCell(2,OBUtils.colorFromRGBString("255,0,0")),
        emitterCell(3 ,OBUtils.colorFromRGBString("0,143,255"))));

        emitter.setZPosition(100);
        objectDict.put("starEmitter", emitter);
        attachControl(emitter);
    }


    public OBEmitterCell emitterCell(int i,int colour)
    {
        OBGroup star = (OBGroup)objectDict.get("emitter_star").copy();
        star.show();
        star.setScale(0.7f);
        OBPath path = (OBPath)star.objectDict.get("star");
        path.setFillColor(colour);
        path.setStrokeColor(OBUtils.highlightedColour(colour));
        OBEmitterCell ec = new OBEmitterCell();
        ec.birthRate = 15;
        ec.lifeTime = 5.0f;
        ec.lifeTimeRange = 0.5f;
        star.enCache();
        ec.contents = star.cache;
        ec.name = String.format("fire%d",i);
        ec.velocity = 200;
        ec.velocityRange = 60;
        ec.emissionRange = (float)(Math.PI * 2.0);
        ec.alphaSpeed = -0.2f;
        ec.spin = 0;
        ec.spinRange = 3.0f;
        ec.position = OB_Maths.locationForRect(0.5f,0.5f,emitter.bounds());
        ec.scale = 1.0f;
        ec.scaleRange = -(ec.scale / 2.0f);
        ec.scaleSpeed = ec.scaleRange / 2.0f;
        return ec;
    }

    public void setEmitter(boolean on) throws Exception
    {
        if(on)
        {
            emitter.run();
        }
        else
        {
            emitter.stop();
        }
    }

    public void loadBigIconForUnit(MlUnit unit)
    {
        lockScreen();
        String imgName = String.format("%s_big",unit.icon);

        if(OBImageManager.sharedImageManager().getImgPath(imgName) != null)
            bigIcon = loadImageWithName(imgName,new PointF(0,0), new RectF(bounds()));
        else
            bigIcon = loadImageWithName("icon_default_big",new PointF(0,0), new RectF(bounds()));

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


    public void setObjectShadow(OBControl obj,float scale)
    {
        OBControl shadow = objectDict.get("shadow");
        obj.setShadow(shadow.getShadowRadius(),shadow.getShadowOpacity(),shadow.getShadowOffsetY()*scale,shadow.getShadowOffsetY()*scale,shadow.getShadowColour());
    }


    public void animateBigIconShow() throws Exception
    {
        playSfxAudio("button_show",false);
        float startScale = bigIcon.scale();
        bigIcon.setScale(startScale * 1.1f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(startScale,bigIcon), OBAnim.opacityAnim(1,bigIcon)),0.5,true,OBAnim.ANIM_EASE_OUT,this);
    }


    public void presenterSpeak(List<String> audioFiles) throws Exception
    {
        playAudioQueued(OBUtils.insertAudioInterval(audioFiles,300),true);
    }


    public void animateBigStar() throws Exception
    {
        final OBGroup star = (OBGroup)objectDict.get("complete_star");
        star.setOpacity ( 1);


        float rotation = star.rotation;
        PointF targetLoc = OB_Maths.locationForRect(0.5f,0.5f,this.bounds());
        star.setScale(1);
        OBControl workrect = objectDict.get("work_rect");
        OBPath path = (OBPath)objectDict.get("big_star_path");
        path.sizeToBox(workrect.bounds());
        setFirstLastPoints(path, OB_Maths.locationForRect(1.1f,0.1f,this.bounds()), targetLoc);

        emitter.setPosition(path.firstPoint());
        star.setPosition(path.firstPoint());
        star.show();
        setEmitter(true);
        playSfxAudio("star",false);
        final RectF shadowOffset = new RectF(0,0,star.getShadowOffsetX(),star.getShadowOffsetY());
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                emitter.setPosition(star.position());
            }
        }
        ;
       OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(star, path.path(), false, 0),
               OBAnim.scaleAnim((float) star.settings.get("start_scale"), star),
               OBAnim.rotationAnim(rotation - (float) Math.toRadians(360 * 5), star),
               blockAnim
               ),3,true,OBAnim.ANIM_EASE_OUT,this);
        waitForSecs(0.5f);
        setEmitter(false);
        emitter.waitForCells();
    }

    public void animateStarOff()
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,objectDict.get("complete_star"))),0.5,true,OBAnim.ANIM_LINEAR,this);
    }

    public void setFirstLastPoints(OBPath path, PointF firstPoint, PointF lastPoint)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return;
        UPath deconPath = deconstructedPath("master", name);

        USubPath subPath = deconPath.subPaths.get(0);
        ULine line = subPath.elements.get(0);
        line.pt0 = firstPoint;

        USubPath subPath2 = deconPath.subPaths.get(deconPath.subPaths.size() - 1);
        ULine line2 = subPath2.elements.get(subPath2.elements.size() - 1);
        line2.pt1 = lastPoint;
        path.setPath(deconPath.bezierPath());
    }


    public void demoStar() throws Exception
    {
        waitForSecs(0.3f);
        animateBigStar();
        presenterSpeak(getSceneAudio("DEMO"));
        animateStarOff();
        waitForSecs(0.3f);
        animateBigIconShow();

        long time = setStatus(STATUS_AWAITING_CLICK);

        presenterSpeak(getSceneAudio("DEMO2"));
    }

    public void demoStar2() throws Exception
    {
        waitForSecs(0.3f);
        animateBigStar();
        presenterSpeak(getSceneAudio("DEMO"));
    }


    public void demoNextIcon() throws Exception
    {
        if(bigIcon != null)
        {
            waitForSecs(0.3f);
            presenterSpeak(getSceneAudio("DEMO"));
            waitForSecs(0.3f);
            animateBigIconShow();

            long time = setStatus(STATUS_AWAITING_CLICK);

            presenterSpeak(getSceneAudio("DEMO2"));


        }
    }

    public void demoPresenter() throws Exception
    {
        waitForSecs(0.3f);
        animateBigIconShow();
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitForSecs(0.3f);

        presenterSpeak(getSceneAudio("DEMO"));
    }
}
