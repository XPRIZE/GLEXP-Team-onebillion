package org.onebillion.onecourse.mainui.oc_playzone;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBRadialGradientPath;
import org.onebillion.onecourse.controls.OBScrollingText;
import org.onebillion.onecourse.controls.OBShaderControl;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_Menu;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBAnalytics;
import org.onebillion.onecourse.utils.OBAnalyticsManager;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OCM_FatController;
import org.onebillion.onecourse.utils.OCM_MlUnit;
import org.onebillion.onecourse.utils.OCM_MlUnitInstance;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 03/07/2017.
 */

public class OC_PlayZoneMenu extends OC_Menu
{
    static int VIDEO_BAR_SIZE = 20;
    static float MAX_SPEED = 0.6f;
    static float TICK_VALUE = 0.0025f;
    static float BUTTON_DEFORM_SCALE = 0.1f;
    static float TOUCH_TOLERANCE  = 20;

    boolean newMediaAdded, mediaIsPlaying, animateFloat, iconsScrollMode, iconsDeleteMode, boxTouchMode;
    List<OBGroup> mediaIcons;
    List<OBImage> menuButtons;
    List<OC_PlayZoneAsset> mediaAssets;
    OBControl currentMediaLayer;
    OBGroup mediaIconsGroup;
    long lastFloatLoopTick, lastTouchTime, iconShakeStartTime;
    PointF lastTouchPoint;
    OBVideoPlayer videoPlayer;
    RectF scrollHitBox;
    float dragTravelDistance;
    float leftLimit, rightLimit, topLimit, bottomLimit;
    OCM_FatController fatController;

    @Override
    public void onPause()
    {
        super.onPause();
        if(currentMediaLayer != null)
        {
            if(currentMediaLayer.propertyValue("gradient_manager") != null)
                ((OBShaderControl)currentMediaLayer.propertyValue("gradient_manager")).stopAnimation();
            else if(currentMediaLayer == videoPlayer)
                videoPlayer.pause();

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(currentMediaLayer != null)
        {
            if(currentMediaLayer.propertyValue("gradient_manager") != null)
                ((OBShaderControl)currentMediaLayer.propertyValue("gradient_manager")).startAnimation(this);
            else if(currentMediaLayer == videoPlayer)
                videoPlayer.start();
        }
    }

    @Override
    public void onBatteryStatusReceived(final float level, final boolean charging)
    {
        super.onBatteryStatusReceived(level,charging);
        if (fatController != null)
        {
            final OBSectionController controller = this;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    fatController.setBatteryChargingLevel(level, charging, controller);
                }
            });
        }
    }

    public String sectionName()
    {
        return "menu";
    }

    public String sectionAudioName()
    {
        return "menu";
    }

    @Override
    public void render(OBRenderer renderer)
    {
        super.render(renderer);
    }

    public void prepare()
    {
        if (OCM_FatController.class.isInstance(MainActivity.mainActivity.fatController))
        {
            fatController = (OCM_FatController) MainActivity.mainActivity.fatController;
        }
        else
        {
            MainActivity.log("OC_PlayZoneMenu:prepare:WARNING --> current FatController setup in MainActivity IS NOT a OCM_FatController.");
            fatController = null;
        }
        //
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadAudioXML(getConfigPath("pzdefaultaudio.xml"));
        if (fatController != null) fatController.loadBatteryIcon(this);
        OBControl btmLeft = objectDict.get("bottom_bar_left");
        OBControl btmRight = objectDict.get("bottom_bar_right");
        scrollHitBox = new RectF(btmLeft.right(), btmLeft.top(), btmRight.left() , btmLeft.bottom());


        OBControl backButton = objectDict.get("button_back");
        backButton.setZPosition(50);
        backButton.show();
        backButton.setProperty("activated",false);

        if(parameters != null)
            boxTouchMode = OBUtils.getBooleanValue(parameters.get("intro"));
        else
            boxTouchMode = false;

        List<OCM_MlUnit> playzoneUnits = fatController.getUnitsForPlayzone();
        loadMenuButtonsForUnits(playzoneUnits);

        objectDict.get("bottom_bar").setZPosition(20);
        objectDict.get("bottom_bar_gradient_left").setZPosition(49);
        objectDict.get("bottom_bar_gradient_right").setZPosition(49);
        objectDict.get("bottom_bar_left").setZPosition(50);
        objectDict.get("bottom_bar_right").setZPosition(50);
        if(boxTouchMode)
        {
            loadEvent("box");
            OBGroup box = (OBGroup)this.objectDict.get("box");
            // box.setScale(applyGraphicScale(0.9f));
            box.show();
            //box.setRasterScale(1.4f*box.scale());
            for(OBControl gemControl : box.filterMembers("gem_.*"))
            {
                OBGroup gemGroup = (OBGroup)gemControl;
                for(OBControl cont : gemGroup.filterMembers("glow.*"))
                {
                    cont.setOpacity(1);
                }
            }
        }
        else
        {
            showControls("bottom_bar.*");
            showControls("cloud_.*");
        }
        newMediaAdded = false;
        mediaIcons = new ArrayList<>();
        mediaAssets = (fatController != null) ? fatController.getPlayZoneAssetForCurrentUser() : new ArrayList<OC_PlayZoneAsset>();

        videoPlayer = null;

        loadAllMediaIcons(mediaAssets);
        mediaIsPlaying = false;
        if(boxTouchMode)
            mediaIconsGroup.hide();

        setControlSpeed(mediaIconsGroup,0.0f,0.0f);
        leftLimit = 0;
        rightLimit = this.bounds().width();
        topLimit = 0;
        bottomLimit = objectDict.get("bottom_bar").top();

    }

    public void start()
    {

        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                refreshButtonsStatus();
                if (boxTouchMode)
                    demoPlayZoneIntro();
                else
                    startButtonEvent();
            }
        });
    }

    public void setSceneXX(String  scene)
    {

    }

    public void doMainXX()
    {

    }

    @Override
    public void viewWillAppear (Boolean animated)
    {
        super.viewWillAppear(animated);
        if(mediaIcons != null)
            checkForNewMedia();
        //setButtons();
    }

    @Override
    public int buttonFlags()
    {
        return 0;
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            if(mediaIsPlaying)
            {
                checkActiveMedia(pt);
            }
            else
            {
                dragTravelDistance = 0;
                if(!checkBackButtonDown(pt) &&
                        mediaIcons.size() > 0 && scrollHitBox.contains( pt.x, pt.y))
                {
                    startMediaIconsDrag(pt);
                }
                else
                {
                    startButtonDrag(pt);
                }
            }
        }
    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            lockScreen();
            if(mediaIsPlaying)
            {
                if(currentMediaLayer.propertyValue("scrollable_text") != null)
                {
                    OBScrollingText scrollingText  = (OBScrollingText)currentMediaLayer.propertyValue("scrollable_text");
                    PointF lastLoc = (PointF)scrollingText.propertyValue("last_drag");
                    PointF currentLoc = OB_Maths.AddPoints(pt,dragOffset);
                    setScrollableTextBoxOffset(scrollingText, scrollingText.yOffset() + (currentLoc.y - lastLoc.y));
                    measureDragSpeed(scrollingText,currentLoc);
                }
            }
            else if(iconsScrollMode)
            {
                setMediaIconsGroupLoc(OB_Maths.AddPoints(pt, dragOffset));
                measureDragSpeed(target);
            }
            else
            {
                setButtonLoc(target,OB_Maths.AddPoints(pt, dragOffset));
                measureDragSpeed(target);
            }
            unlockScreen();
        }
    }


    public void touchUpAtPoint(final PointF pt, View v)
    {
        if(checkBackButtonUp(pt))
            return;

        if(status() == STATUS_AWAITING_CLICK && mediaIsPlaying)
        {
            long time =  System.currentTimeMillis();
            if((time-lastTouchTime) < 1000 &&
                    OB_Maths.PointDistance(pt, lastTouchPoint) < applyGraphicScale(TOUCH_TOLERANCE))
            {
                setStatus(STATUS_BUSY);
                currentMediaTouch();
            }
        }
        else if (status() == STATUS_DRAGGING && target != null)
        {
            setStatus(STATUS_BUSY);
            final OBControl targ = target;
            target = null;
            if(iconsScrollMode)
                mediaIconsGroup.setProperty("animate", true);
            long time = System.currentTimeMillis();
            if((time-lastTouchTime) < 1000 &&
                    dragTravelDistance < applyGraphicScale(TOUCH_TOLERANCE))
            {
                if(mediaIsPlaying)
                {
                    currentMediaTouch();
                }
                else if(iconsScrollMode)
                {
                    OBControl icon = null;//finger(-1,-1,(List<OBControl>)(Object)mediaIcons,pt);
                    for(OBControl con : mediaIcons)
                    {
                        if(con.getWorldFrame().contains(pt.x,pt.y))
                        {
                            icon = con;
                            break;
                        }
                    }
                    if(icon != null)
                    {
                        final OBGroup iconGroup = (OBGroup)icon;
                        if(iconsDeleteMode)
                        {
                            if(finger(1,1,Arrays.asList(iconGroup.objectDict.get("cross")),pt) != null)
                            {
                                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                {
                                    public void run() throws Exception
                                    {
                                        stopDeleteMode();
                                        deleteIcon(iconGroup);
                                        setStatus(STATUS_AWAITING_CLICK);
                                    }
                                });
                            }
                            else
                            {
                                if(targ != null)
                                    targ.enable();
                                setStatus(STATUS_AWAITING_CLICK);

                            }

                        }
                        else
                        {
                            setStatus(STATUS_BUSY);
                            setControlSpeed(targ,0.0f,0.0f);
                            stopFloatLoop();
                            playMediaForIcon(iconGroup);
                        }
                    }
                    else
                    {
                        targ.enable();
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                }
                else
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkSectionButton(targ);
                        }
                    });
                }
            }
            else
            {
                if(mediaIsPlaying && currentMediaLayer != null && currentMediaLayer.propertyValue("scrollable_text") != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            startCurrentTextMediaScroll();
                        }
                    });
                }
                else
                {
                    targ.setProperty("animate",true);
                    long statusT = setStatus(STATUS_AWAITING_CLICK);
                    if(iconsDeleteMode)
                    {
                        postponeDeleteMode(statusT);
                    }
                }
            }
        }
    }

    public void checkActiveMedia(final PointF pt)
    {
        if(currentMediaLayer == null)
            return;

        setStatus(STATUS_BUSY);
        final OC_SectionController sectionController = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                lastTouchTime = System.currentTimeMillis();
                lastTouchPoint = pt;
                dragTravelDistance = 0;
                iconsScrollMode = false;
                if(currentMediaLayer.propertyValue("scrollable_text") != null)
                {
                    OBScrollingText scrollableControl = (OBScrollingText) currentMediaLayer.propertyValue("scrollable_text");
                    if (scrollableControl.getWorldFrame().contains(pt.x, pt.y))
                    {
                        prepareForSpeedMeasure(scrollableControl);
                        OBMisc.prepareForDragging(scrollableControl, pt, sectionController);
                        setStatus(STATUS_DRAGGING);
                    }
                    else
                    {
                        setStatus(STATUS_AWAITING_CLICK);
                    }

                }
                else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
        });
    }

    public boolean checkBackButtonDown(PointF pt)
    {
        OBControl backButton = objectDict.get("button_back");
        if(finger(0,1,Arrays.asList(backButton),pt) != null)
        {
            backButton.highlight();
            backButton.setProperty("activated",true);
            return true;

        }
        return false;

    }
    public boolean checkBackButtonUp(PointF pt)
    {
        OBControl backButton = objectDict.get("button_back");
        if(backButton.propertyValue("activated") != null &&
                (boolean)backButton.propertyValue("activated"))
        {
            backButton.lowlight();
            if(finger(0,1,Arrays.asList(backButton),pt) != null)
            {
                playAudio(null);
                OBAnalyticsManager.sharedManager.enteredScreen(OBAnalytics.Screen.COMMUNITY_MODE);
                goBack();
                return true;

            }
            else
            {
                backButton.setProperty("activated",false);

            }

        }
        return false;

    }


    public void startMediaIconsDrag(PointF pt)
    {
        setStatus(STATUS_BUSY);
        OBControl icon = finger(-1, -1, (List<OBControl>)(Object)mediaIcons, pt);
        OBMisc.prepareForDragging(mediaIconsGroup, pt, this);
        prepareForSpeedMeasure(mediaIconsGroup);
        mediaIconsGroup.setProperty("animate", false);
        mediaIconsGroup.setZPosition(mediaIconsGroup.zPosition() - 10);
        iconsScrollMode = true;
        lastTouchTime = System.currentTimeMillis();
        lastTouchPoint = OBMisc.copyPoint(mediaIconsGroup.position());
        final long time = setStatus(STATUS_DRAGGING);
        if (!iconsDeleteMode && icon != null)
        {
            OBUtils.runOnOtherThreadDelayed(1.5f, new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    if (time == statusTime &&
                            dragTravelDistance < applyGraphicScale(TOUCH_TOLERANCE))
                    {
                        stopIconShake();
                        startDeleteMode();
                        long statusT = setStatus(STATUS_AWAITING_CLICK);
                        postponeDeleteMode(statusT);
                    }
                }
            });
        }
    }

    public void startButtonDrag(PointF pt)
    {
        setStatus(STATUS_BUSY);
        if(iconsDeleteMode)
            stopDeleteMode();
        OBControl btn = finger(0,0,(List<OBControl>)(Object)menuButtons,pt,true);
        if(btn != null)
        {
            setStatus(STATUS_BUSY);
            btn.setProperty("animate", false);
            OBMisc.prepareForDragging(btn,pt,this);
            iconsScrollMode = false;
            btn.setZPosition(btn.zPosition() - 10);
            prepareForSpeedMeasure(btn);
            lastTouchTime = System.currentTimeMillis();
            lastTouchPoint = OBMisc.copyPoint(btn.position());
            setStatus(STATUS_DRAGGING);
        }
        else
        {
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void currentMediaTouch()
    {
        if(currentMediaLayer == videoPlayer)
        {
            try
            {
                videoPlayer.stop();
                finishVideoPlaying(videoPlayer);
            }
            catch (Exception e)
            {
                MainActivity.log("Error stopping video: " + e.getMessage());
            }
        }
        else
        {
            animateMedia(currentMediaLayer,false);
            finishMediaPlaying();
        }
    }

    public void checkSectionButton(final OBControl targ) throws Exception
    {
        //OBControl highlight = targ.objectDict.get("highlight");
        targ.highlight();
        stopFloatLoop();
        playAudio(null);
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                boolean started = startSectionButton(targ);
            }
        });


        //  if(!started)
        // setStatus(STATUS_AWAITING_CLICK);

    }

    public void startCurrentTextMediaScroll() throws Exception
    {
        long time = setStatus(STATUS_AWAITING_CLICK);
        OBScrollingText scrollableControl = (OBScrollingText) currentMediaLayer.propertyValue("scrollable_text");
        long currentTime, lastLoopTime;
        currentTime = lastLoopTime = System.currentTimeMillis();
        float scrollSpeed = (float)scrollableControl.propertyValue("speedy");
        while(time == statusTime && !_aborting && Math.abs(scrollSpeed) > applyGraphicScale(0.01f))
        {
            currentTime = System.currentTimeMillis();
            float frameFrac = (currentTime - lastLoopTime)*1.0f/(TICK_VALUE*1000.0f) ;
            moveScrollableText(scrollableControl,frameFrac);
            lastLoopTime = currentTime;
            waitForSecs(0.001f);
        }
    }

    public void measureDragSpeed(OBControl con)
    {
        if(con == null)
            return;
        PointF currentLoc = OBMisc.copyPoint(con.position());
        long currentTime = System.currentTimeMillis();
        long lastTime = (long)con.propertyValue("last_action");
        if(currentTime == lastTime)
            return;
        float lastSpeedX = (float)con.propertyValue("speedx");
        float lastSpeedY = (float)con.propertyValue("speedy");
        PointF lastLoc = (PointF)con.propertyValue("last_drag");

        dragTravelDistance += OB_Maths.PointDistance(lastLoc, currentLoc);
        float ticks =  (currentTime - lastTime)*1.0f/(TICK_VALUE*1000.0f);
        float speedX = 0.8f * ((currentLoc.x - lastLoc.x) / ticks) + 0.2f * lastSpeedX;
        float speedY = 0.8f * ((currentLoc.y - lastLoc.y) / ticks) + 0.2f * lastSpeedY;
        setControlSpeed(con,speedX,speedY);
        con.setProperty("last_drag",currentLoc);
        con.setProperty("last_action",currentTime);
    }

    public void measureDragSpeed(OBScrollingText con, PointF currentLoc)
    {
        if(con == null)
            return;

        long currentTime = System.currentTimeMillis();
        long lastTime = (long)con.propertyValue("last_action");
        if(currentTime == lastTime)
            return;
        float lastOffset = (float)con.propertyValue("last_offset");
        float currentOffset = con.yOffset();
        float lastSpeedY = (float)con.propertyValue("speedy");
        PointF lastLoc = (PointF)con.propertyValue("last_drag");

        dragTravelDistance += OB_Maths.PointDistance(lastLoc, currentLoc);
        float ticks =  (currentTime - lastTime)*1.0f/(TICK_VALUE*1000.0f);
        float speedY = 0.8f * ((currentOffset - lastOffset) / ticks) + 0.2f * lastSpeedY;
        setControlSpeed(con,0,speedY);
        con.setProperty("last_offset", con.yOffset());
        con.setProperty("last_drag",currentLoc);
        con.setProperty("last_action",currentTime);
    }

    public void prepareForSpeedMeasure(OBControl con)
    {
        con.setProperty("last_action",System.currentTimeMillis());
        con.setProperty("last_drag",OBMisc.copyPoint(con.position()));
        con.setProperty("speedx",0.0f);
        con.setProperty("speedy",0.0f);
        con.setProperty("collide",true);
    }


    public void prepareForSpeedMeasure(OBScrollingText con)
    {
        con.setProperty("last_action",System.currentTimeMillis());
        con.setProperty("last_drag",OBMisc.copyPoint(con.position()));
        con.setProperty("last_offset",con.yOffset());
        con.setProperty("speedx",0.0f);
        con.setProperty("speedy",0.0f);
        con.setProperty("collide",true);
    }

    public float randomBubbleSpeed()
    {
        float speed = 0.1f*MAX_SPEED + (float)OB_Maths.rndom() * MAX_SPEED *0.9f;
        return applyGraphicScale((OB_Maths.randomInt(0, 1)*2 -1) * speed);
    }

    public float buttonRadius(OBControl button)
    {
        return (float)button.propertyValue("radius");
    }

    public float buttonCollisionRadius(OBControl button)
    {
        return (float)button.propertyValue("radius") * (1.0f - BUTTON_DEFORM_SCALE);
    }

    public float collisionDistanceButton(OBControl button,OBControl button2)
    {
        return buttonCollisionRadius(button) + buttonCollisionRadius(button2);
    }

    public void startFloatLoop(boolean reloadMoves)
    {
        prepareForSpeedMeasure(mediaIconsGroup);
        animateFloat= true;
        lastFloatLoopTick =  System.currentTimeMillis();
        OBAnimationGroup ag = new OBAnimationGroup();
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                if(animateFloat)
                    floatLoop();
            }
        };

        for(OBControl button : menuButtons)
        {
            button.setProperty("squish_time",System.currentTimeMillis());
            button.setProperty("squish_angle",(float)Math.toRadians(OB_Maths.randomInt(1, 360)));
            if(reloadMoves)
                setControlSpeed(button,randomBubbleSpeed(),randomBubbleSpeed());

        }
        ag.applyAnimations(Arrays.asList(anim),100,false,OBAnim.ANIM_LINEAR,-1,null, this);
        registerAnimationGroup(ag, "floatloop");
    }

    public void stopFloatLoop()
    {
        animateFloat= false;
        deregisterAnimationGroupWithName("floatloop");
    }

    public void floatLoop()
    {
        long currentTime = System.currentTimeMillis();
        float frameFrac = (currentTime - lastFloatLoopTick)/(TICK_VALUE*1000.0f);
        for(OBControl button : menuButtons)
        {
            if((boolean) button.propertyValue("animate"))
                moveButtonBySpeed(button,frameFrac);
            checkButtonsForCollisions();

        }
        if((boolean)mediaIconsGroup.propertyValue("animate"))
            moveMediaIconsBySpeedFrac(frameFrac);
        if(iconsDeleteMode)
            animateIconsShake();
        lastFloatLoopTick = currentTime;
        if(_aborting || !animateFloat)
            deregisterAnimationGroupWithName("floatloop");

    }

    public void animateIconsShake()
    {
        float startAngle = (float)Math.toRadians(5);
        float endAngle = (float)Math.toRadians(-5);
        long shakeTime = 700;
        float frac = OB_Maths.clamp01(((System.currentTimeMillis()- iconShakeStartTime ) % shakeTime)*1.0f/shakeTime);
        float currentAngle = startAngle;
        float midFrac = 0;
        if(frac <= 0.5)
        {
            midFrac = OB_Maths.bezef(frac/0.5f);
        }
        else
        {
            midFrac = 1-OB_Maths.bezef((float)Math.abs(frac-0.5f)/0.5f);
        }
        currentAngle = startAngle + midFrac * (endAngle-startAngle);
        for(OBControl con : mediaIcons)
        {
            con.setRotation(currentAngle);
        }
    }

    /*public boolean checkButton(OBControl button,OBControl button2)
    {
        if(button2 == button)
            return false;
        float dist = OB_Maths.PointDistance(button.position(), button2.position());
        return dist < collisionDistanceButton(button,button2);
    }*/

    public void setButtonEnabled(OBControl button, boolean enabled)
    {
        if(enabled)
        {
            button.setOpacity(1);
            button.enable();
        }
        else
        {
            button.setOpacity(0.3f);
            button.disable();
        }
        button.setProperty("locked", false);
    }

    public void refreshButtonsStatus()
    {
        lockScreen();
        int unitid = fatController.getPlayzoneLockUnitId();

        for (OBControl button : menuButtons)
        {
            OCM_MlUnit unit = (OCM_MlUnit) button.propertyValue("unit");
            setButtonEnabled(button, unitid < 0 || unit.unitid == unitid);
            if(unit.unitid == unitid)
                button.setProperty("locked", true);
        }
        unlockScreen();

    }

    public void checkButtonsForCollisions()
    {
        for(int i=0; i< menuButtons.size(); i++)
        {
            OBControl button = menuButtons.get(i);
            if(!(boolean)button.propertyValue("collide"))
                continue;
            boolean precollided = false;
            for(int j=i+1; j< menuButtons.size(); j++)
            {
                OBControl button2 = menuButtons.get(j);
                float dist = OB_Maths.PointDistance(button.position(), button2.position());
                float preCollisionDistance = buttonRadius(button) + buttonRadius(button2);
                if(dist < preCollisionDistance)
                {
                    float collisionDistance =  collisionDistanceButton(button,button2);
                    precollided = true;
                    PointF n = OB_Maths.DiffPoints(button.position(), button2.position());
                    float angle = (float)Math.atan2(n.y, n.x);
                    float landFrac = OB_Maths.clamp01((preCollisionDistance-dist)*1.0f/(preCollisionDistance - collisionDistance));
                    if(checkButtonSquish(button,button2))
                        buttonSquish(button,angle,landFrac);
                    if(checkButtonSquish(button2,button))
                        buttonSquish(button2,angle,landFrac);
                    if(dist < collisionDistance)
                    {
                        PointF v1 = new PointF((float)button.propertyValue("speedx"), (float)button.propertyValue("speedy"));
                        PointF v2 = new PointF((float)button2.propertyValue("speedx"), (float)button2.propertyValue("speedy"));
                        float colTr = n.x*(v2.x - v1.x) + n.y*(v2.y - v1.y);
                        if(colTr <= 0) //if they are not on collision trajectory, no worries
                            continue;
                        if(n.x == 0 && n.y == 0)
                        {
                            n = new PointF(randomBubbleSpeed(), randomBubbleSpeed());
                        }
                        else
                        {
                            n = OB_Maths.NormalisedVector(n);
                        }
                        float dot1 = OB_Maths.dot_product(v1,n);
                        float dot2 = OB_Maths.dot_product(v2,n);
                        PointF r1 = OB_Maths.AddPoints(OB_Maths.ScalarTimesPoint(dot2-dot1,n),v1);
                        PointF r2 = OB_Maths.AddPoints(OB_Maths.ScalarTimesPoint(dot1-dot2,n),v2);
                        if((boolean)button.propertyValue("animate"))
                            setControlSpeed(button,r1.x,r1.y);
                        if((boolean)button2.propertyValue("animate"))
                            setControlSpeed(button2,r2.x,r2.y);
                    }
                }
            }
        }
    }

    public boolean checkButtonSquish(OBControl button,OBControl button2)
    {
        if(button.propertyValue("nearest_control") == null)
        {
            button.setProperty("nearest_control",button2);
            return true;
        }
        OBControl prevNearestControl = (OBControl)button.propertyValue("nearest_control");
        if(prevNearestControl == button2)        return true;
        float curDist = OB_Maths.PointDistance(button.position(), button2.position()) - collisionDistanceButton(button,button2);
        float prevDist = OB_Maths.PointDistance(button.position(), prevNearestControl.position())  - collisionDistanceButton(button,prevNearestControl);
        if(prevDist > curDist)
        {
            button.setProperty("nearest_control",button2);
            return true;
        }
        return false;
    }

    public boolean nearestButtonIsColliding(OBControl button)
    {
        OBControl nearestButton = (OBControl)button.propertyValue("nearest_control");
        if(nearestButton != null)
        {
            float curDist = OB_Maths.PointDistance(button.position(), nearestButton.position());
            return curDist <= (buttonRadius(button) + buttonRadius(nearestButton));

        }
        return false;
    }

    public void setControlSpeed(OBControl button,float speedX, float speedY)
    {
        float speedLimit = MAX_SPEED*30;
        if(speedX > speedLimit)
            speedX = speedLimit;
        else if(speedX < -speedLimit)
            speedX = -speedLimit;
        if(speedY > speedLimit)
            speedY = speedLimit;
        else if(speedY < -speedLimit)
            speedY = -speedLimit;
        button.setProperty("speedx",speedX);
        button.setProperty("speedy",speedY);
    }

    public void moveButtonBySpeed(OBControl button,float frameFrac)
    {
        float speedX = (float)button.propertyValue("speedx");
        float speedY = (float)button.propertyValue("speedy");
        if(speedX == 0 && speedY == 0)
            return;
        PointF loc = OBMisc.copyPoint(button.position());
        loc.x += speedX * frameFrac;
        loc.y += speedY * frameFrac;
        float decay = (float)Math.pow(0.99f,frameFrac);
        if(Math.abs(speedX) > MAX_SPEED)
            speedX *= decay;
        if(Math.abs(speedY) > MAX_SPEED)
            speedY *= decay;
        setControlSpeed(button,speedX,speedY);
        setButtonLoc(button,loc ,(boolean)button.propertyValue("collide"));
    }

    public void moveMediaIconsBySpeedFrac(float frameFrac)
    {
        float speedX = (float)mediaIconsGroup.propertyValue("speedx");
        if(speedX == 0)
            return;
        PointF loc = OBMisc.copyPoint(mediaIconsGroup.position());
        loc.x += speedX * frameFrac;
        float decay = (float)Math.pow(0.99f,frameFrac);
        if(Math.abs(speedX) > MAX_SPEED*0.1)
            speedX *= decay;
        else
            speedX = 0;
        if(!setMediaIconsGroupLoc(loc))
        {
            setControlSpeed(mediaIconsGroup,0.0f,0.0f);
        }
        else
        {
            setControlSpeed(mediaIconsGroup,speedX,0.0f);
        }
    }

    public boolean setMediaIconsGroupLoc(PointF loc)
    {
        if(mediaIconsGroup.width() < scrollHitBox.width())
            return true;
        loc.y = mediaIconsGroup.position().y;
        mediaIconsGroup.setPosition ( loc);
        float limitLeft = objectDict.get("bottom_bar_gradient_left").right();
        float limitRight = objectDict.get("bottom_bar_gradient_right").left();
        if(mediaIconsGroup.left() > limitLeft)
        {
            mediaIconsGroup.setLeft ( limitLeft);
            return false;

        }
        else if(mediaIconsGroup.right() < limitRight)
        {
            mediaIconsGroup.setRight ( limitRight);
            return false;
        }
        return true;
    }

    public void setButtonLoc(OBControl button,PointF loc)
    {
        setButtonLoc(button,loc,true);
    }

    public boolean setButtonLoc(OBControl button,PointF loc, boolean collide)
    {
        boolean didCollide = false;
        float speedX = (float)button.propertyValue("speedx");
        float speedY = (float)button.propertyValue("speedy");
        button.setPosition(loc);
        float radius = buttonRadius(button);
        float collisionRadius = buttonCollisionRadius(button);
        float extraLimitDistance = (radius - collisionRadius);
        if(collide)
        {
            if(loc.x - radius < leftLimit)
            {
                didCollide = true;
                buttonSquish(button,0,OB_Maths.clamp01(OB_Maths.clamp01((leftLimit-(loc.x-radius))/extraLimitDistance)));
                if((loc.x  - collisionRadius) < leftLimit)
                {
                    if(collide)
                        loc.x = collisionRadius;
                    button.setPosition ( loc);
                    button.setProperty("speedx",Math.abs(speedX));
                }
            }
            if(loc.x + radius > rightLimit)
            {
                didCollide = true;
                buttonSquish(button,0,OB_Maths.clamp01(((loc.x+radius)-rightLimit)/extraLimitDistance));
                if((loc.x + collisionRadius) > rightLimit)
                {
                    loc.x = rightLimit-collisionRadius;
                    button.setPosition ( loc);
                    button.setProperty("speedx",-Math.abs(speedX));
                }
            }
            if(loc.y - radius < topLimit)
            {
                didCollide = true;
                buttonSquish(button,(float)Math.PI*0.5f,OB_Maths.clamp01((topLimit-(loc.y - radius))/extraLimitDistance));
                if((loc.y - collisionRadius) < topLimit )
                {
                    loc.y = collisionRadius;
                    button.setPosition(loc);
                    button.setProperty("speedy",Math.abs(speedY));
                }
            }
            if(loc.y + radius > bottomLimit)
            {
                didCollide = true;
                buttonSquish(button,(float)Math.PI*0.5f,OB_Maths.clamp01(((loc.y + radius)-bottomLimit)/extraLimitDistance));
                if((loc.y + collisionRadius) > bottomLimit )
                {
                    loc.y = bottomLimit - collisionRadius;
                    button.setPosition(loc);
                    button.setProperty("speedy",-Math.abs(speedY));
                }
            }
            if(!didCollide && !nearestButtonIsColliding(button))
                buttonSquish(button,0,0);
        }
        return didCollide;
    }

    public void buttonSquish(OBControl button,float angle, float frac)
    {
        if(button.propertyValue("squish_frac") != null)
        {
            if((float)button.propertyValue("squish_frac") == frac &&   (float)button.propertyValue("squish_angle")  == angle)
                return;
        }
        button.setProperty("squish_frac",frac);
        button.setProperty("squish_angle",angle);
        android.opengl.Matrix.setIdentityM(button.multiplyMatrix, 0);
        android.opengl.Matrix.rotateM(button.multiplyMatrix, 0, (float)Math.toDegrees(angle), 0, 0, 1);
        android.opengl.Matrix.scaleM(button.multiplyMatrix, 0, 1.0f*(1.0f- (BUTTON_DEFORM_SCALE * frac)), 1, 1);
        android.opengl.Matrix.rotateM(button.multiplyMatrix, 0, (float)Math.toDegrees(-angle), 0, 0, 1);
    }


    public boolean startSectionButton(final OBControl button)
    {
        OCM_MlUnit unit = (OCM_MlUnit)button.propertyValue("unit");
        boolean locked = (boolean)button.propertyValue("locked");
        fatController.startUnit(unit,locked ? OCM_MlUnitInstance.INSTANCE_TYPE_PZ_LOCKED : OCM_MlUnitInstance.INSTANCE_TYPE_PLAYZONE ,
                new OCM_FatController.SectionOpeningCallback()
        {
            @Override
            public void run(final OCM_MlUnitInstance unitInstance, final boolean success)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        playAudio(null);
                        waitForSecs(0.5f);
                        button.setProperty("animate", true);
                        button.lowlight();
                        if(!success)
                        {
                            start();
                        }
                    }
                });
            }
        });
        return true;
    }

    public void startDeleteMode()
    {
        OBControl targ = target;
        target = null;
        if(targ != null)
            targ.enable();
        startIconShake();
        iconsDeleteMode = true;
    }

    public void stopDeleteMode()
    {
        iconsDeleteMode = false;
        stopIconShake();
    }

    public void postponeDeleteMode(final long time)
    {
        OBUtils.runOnOtherThreadDelayed(5, new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if (time == statusTime && iconsDeleteMode)
                {
                    setStatus(STATUS_BUSY);
                    target = null;
                    stopDeleteMode();
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
        });
    }

    public void startIconShake()
    {
        lockScreen();
        for(OBGroup icon : mediaIcons)
        {
            icon.objectDict.get("cross").show();
        }
        unlockScreen();
        iconShakeStartTime = System.currentTimeMillis();
    }

    public void stopIconShake()
    {
        lockScreen();
        for(OBGroup icon : mediaIcons)
        {
            icon.objectDict.get("cross").hide();
            icon.setRotation ( 0);
        }
        unlockScreen();
    }

    public void loadMenuButtonsForUnits(List<OCM_MlUnit> units)
    {

        menuButtons = new ArrayList<>();
        for(OCM_MlUnit unit : units)
        {
            String completePath = unit.pathToIcon(false);
            OBImage icon = OBImageManager.sharedImageManager().imageForPath(completePath);
            if(icon == null)
            {
                continue;
            }
            OBImage button = icon;

            menuButtons.add(button);
            attachControl(button);
            prepareForSpeedMeasure(button);
            button.setProperty("start_scale",button.scale());
            button.setProperty("radius",button.width()*0.5f);
            button.setProperty("unit",unit);
            button.setProperty("animate", true);
            button.setZPosition(10);
            if(boxTouchMode)
                button.hide();
        }

    }


    public OBGroup loadAssetIcon(OC_PlayZoneAsset asset)
    {
        OBControl mrect = objectDict.get("media_rect").copy();
        OBControl mbg = objectDict.get("media_bg").copy();
        OBControl grad1 = objectDict.get("media_gradient_top").copy();
        OBControl grad2 = objectDict.get("media_gradient_bottom").copy();
        grad1.show();
        grad2.show();
        mrect.show();
        mbg.show();
        mbg.setZPosition(0.5f);
        OBControl thumbnail = null;
        if(asset.typeid == OC_PlayZoneAsset.ASSET_VIDEO)
        {
            mbg.setFillColor(Color.RED);
            thumbnail = OBImageManager.sharedImageManager().imageForPath(OC_PlayZoneAsset.pathToAsset(asset.thumbnail));
        }
        else if(asset.typeid == OC_PlayZoneAsset.ASSET_DOODLE)
        {
            mbg.setFillColor(Color.BLUE);
            thumbnail = OBImageManager.sharedImageManager().imageForPath(OC_PlayZoneAsset.pathToAsset(asset.thumbnail));
        }
        else if(asset.typeid == OC_PlayZoneAsset.ASSET_TEXT)
        {
            mbg.setFillColor(Color.GREEN);
            thumbnail = OBImageManager.sharedImageManager().imageForPath(OC_PlayZoneAsset.pathToAsset(asset.thumbnail));
/*
            if (thumbnail == null)
            {
                Map<String,String> dataDict = asset.paramsDictionary();
                OBImage overlay = loadImageWithName(String.format("thumbnail_type_%s",dataDict.get("theme")),new PointF(0.5f, 0.5f),new RectF(this.bounds()));
                overlay.setZPosition(3);
                String text = dataDict.get("text");
                Typeface currentFont =  OBUtils.TypefaceForFile(dataDict.get("font"));
                float fontSize = applyGraphicScale(12);
                OBLabel thumbnailText = new OBLabel("",currentFont,fontSize);
                thumbnailText.setFrame(0,0,overlay.width()*0.7f, overlay.height()*0.9f);
                thumbnailText.setPosition(OB_Maths.locationForRect(0.5f,0.5f,overlay.frame()));
                thumbnailText.setTop(overlay.top() + applyGraphicScale(10));
                thumbnailText.setZPosition(2);
                thumbnailText.setColour(Color.BLACK);
                String trimmedString = text.replace("\t","");
                String shortString = trimmedString.length() > 50 ? trimmedString.substring(0,49) : trimmedString;
                thumbnailText.setString(shortString);
                OBControl background = new OBControl();
                background.setFrame(overlay.frame());
                background.setBackgroundColor(Color.WHITE);
                background.setZPosition (1);
                thumbnail = new OBGroup(Arrays.asList(background,thumbnailText,overlay));
            }*/
        }
        mrect.setPosition(thumbnail.position());
        mrect.setZPosition(1);
        thumbnail.setZPosition(2);
        OBMisc.scaleControlToControl(thumbnail,mrect,true);
        OBControl crossCopy = objectDict.get("media_delete_cross").copy();
        crossCopy.hide();
        grad1.setZPosition(4);
        grad2.setZPosition(4);
        grad1.setPosition(mrect.position());
        grad1.setTop(mrect.top());
        grad2.setPosition(mrect.position());
        grad2.setBottom(mrect.bottom());
        OBGroup frameGroup = new OBGroup(Arrays.asList(thumbnail,grad1,grad2));
        RectF rect = new RectF(frameGroup.bounds());
        rect.inset((frameGroup.bounds.width() - mrect.bounds.width())/2.0f,(frameGroup.bounds.height() - mrect.bounds.height())/2.0f );
        frameGroup.setBounds(rect);
        frameGroup.setMasksToBounds(true);
        frameGroup.setZPosition(2);
        PointF loc = frameGroup.position();
        mbg.setPosition(loc.x + frameGroup.bounds().left, loc.y + frameGroup.bounds().top);
        crossCopy.setRight(frameGroup.right());
        crossCopy.setTop(frameGroup.top());
        OBGroup iconGroup = new OBGroup(Arrays.asList(mbg,frameGroup,crossCopy));
        crossCopy.setZPosition(10);
        iconGroup.objectDict.put("cross",crossCopy);
        iconGroup.setProperty("asset",asset);
        // iconGroup.setRasterScale(1.0f);
        return iconGroup;
    }

    public void loadAllMediaIcons(List<OC_PlayZoneAsset> assetsData)
    {
        if(mediaIconsGroup != null)
        {
            detachControl(mediaIconsGroup);
        }
        mediaIcons.clear();
        OBControl mrect = objectDict.get("media_bg");
        int index = 0;
        for(OC_PlayZoneAsset asset : assetsData)
        {
            OBGroup icon = loadAssetIcon(asset);
            icon.setPosition(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("bottom_bar").frame()));
            icon.setLeft(objectDict.get("bottom_bar_gradient_left").right() + mrect.width()*1.2f*index);
            mediaIcons.add(icon);
            index++;
        }
        mediaIconsGroup = new OBGroup((List<OBControl>)(Object)mediaIcons);
        setMediaIconsGroupLoc(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("bottom_bar").frame()));
        attachControl(mediaIconsGroup);
        mediaIconsGroup.setProperty("animate", true);
        //RectF frame = mediaIconsGroup.frame();
        // mediaIconsGroup.setFrame(frame);
        mediaIconsGroup.setShouldTexturise(false);
        mediaIconsGroup.setZPosition ( 30);
    }

    public void playMediaForIcon(final OBControl icon)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                final OC_PlayZoneAsset asset = (OC_PlayZoneAsset)icon.propertyValue("asset");
                if (asset.typeid == OC_PlayZoneAsset.ASSET_VIDEO)
                {
                    loadVideoForAsset(asset, icon);
                    mediaIsPlaying = true;
                    setStatus(STATUS_AWAITING_CLICK);
                } else if (asset.typeid == OC_PlayZoneAsset.ASSET_TEXT)
                {
                    OBUtils.runOnMainThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    Map<String,String> par = asset.paramsDictionary();
                                    if(par.containsKey("theme") && par.containsKey("text"))
                                    {
                                        String params = "typewrite/readonly=true";
                                        params += "/theme=" + par.get("theme");
                                        params += "/text=" + par.get("text");
                                        MainViewController().pushViewController(OC_PlayZoneTypewrite.class, true, true, params, false, true, new RectF(icon.getWorldFrame()));

                                    }
                                    else
                                    {
                                        setStatus(STATUS_AWAITING_CLICK);
                                    }
                                }
                            }
                    );

                } else if (asset.typeid == OC_PlayZoneAsset.ASSET_DOODLE)
                {
                    OBUtils.runOnMainThread(
                            new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    Map<String,String> par = asset.paramsDictionary();
                                    if(par.containsKey("theme") && par.containsKey("doodle"))
                                    {
                                        String params = "doodle/readonly=true";
                                        params += "/theme=" + par.get("theme");
                                        params += "/doodle=" + par.get("doodle");
                                        MainViewController().pushViewController(OC_Doodle.class, true, true, params, false, true, new RectF(icon.getWorldFrame()));

                                    }
                                    else
                                    {
                                        setStatus(STATUS_AWAITING_CLICK);
                                    }
                                }
                            }
                    );

                }

            }
        });
    }


    public void loadVideoForAsset(OC_PlayZoneAsset asset,OBControl icon)
    {
        final Map<String,String> params = asset.paramsDictionary();
        if(videoPlayer == null)
        {
            videoPlayer = new OBVideoPlayer(this.boundsf(),this,false,false);
            attachControl(videoPlayer);
            videoPlayer.setZPosition(100);
        }
        videoPlayer.setDisplayMirrored(Boolean.valueOf(params.get("mirrored")));
        RectF videoIconFrame = icon.getWorldFrame();
        float targetScale = videoIconFrame.height()/videoPlayer.height();
        PointF videoIconLoc = icon.getWorldPosition();
        String videoFilePath = OC_PlayZoneAsset.pathToAsset(params.get("video"));

        AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(videoFilePath);
        videoPlayer.setPosition(videoIconLoc);
        videoPlayer.setScale(targetScale);

        videoPlayer.setProperty("parent_icon",icon);
        videoPlayer.prepareForPlaying(afd, 0, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        videoPlayer.show();
                        animateMedia(videoPlayer, true);
                        currentMediaLayer = videoPlayer;
                        OBUtils.runOnMainThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                videoPlayer.start(new OBUtils.RunLambda()
                                {
                                    @Override
                                    public void run() throws Exception
                                    {
                                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                                        {
                                            public void run() throws Exception
                                            {
                                                finishVideoPlaying(videoPlayer);
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });


    }

    public void finishVideoPlaying(OBVideoPlayer videoPlayer)
    {
        setStatus(STATUS_BUSY);
        animateMedia(videoPlayer, false);
        videoPlayer.setScale(1);
        finishMediaPlaying();
    }


    public void finishMediaPlaying()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {

                if(currentMediaLayer.propertyValue("delete_after") != null &&
                        (boolean)currentMediaLayer.propertyValue("delete_after"))
                    detachControl(currentMediaLayer);
                currentMediaLayer = null;
                mediaIsPlaying = false;
                startFloatLoop(false);
                setStatus(STATUS_AWAITING_CLICK);
            }
        });
    }

    public void animateMedia(OBControl mediaLayer,boolean zoomIn)
    {
        mediaLayer.show();

        if(mediaLayer.propertyValue("skip_texturise") == null )
            mediaLayer.setShouldTexturise(true);
        OBControl icon = (OBControl)mediaLayer.propertyValue("parent_icon");
        float zoomScale = mediaLayer.propertyValue("target_scale") != null ? (float)mediaLayer.propertyValue("target_scale")  : 1;
        float targetScale = zoomIn ? zoomScale : icon.height()/(mediaLayer.height()/zoomScale);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(targetScale,mediaLayer),
                OBAnim.moveAnim(zoomIn ? OB_Maths.locationForRect(0.5f,0.5f, this.bounds()) : icon.getWorldPosition() ,mediaLayer))
                ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        if(zoomIn)
        {
            mediaLayer.invalidate();
            if(mediaLayer.propertyValue("skip_texturise") == null )
                mediaLayer.setShouldTexturise(false);
        }
        else
        {
            mediaLayer.hide();
        }
    }

    public boolean setScrollableTextBoxOffset(OBScrollingText scrollingText, float offset)
    {
        if(offset == 0)
            return true;

        boolean endReached = false;

        float scrollTopLimit = 0;
        float scrollBottomLimit = (float)scrollingText.propertyValue("bottom_offset");

        if(scrollBottomLimit > -scrollingText.height())
            return true;

        lockScreen();
        if(offset < scrollBottomLimit)
        {
            endReached = true;
            scrollingText.setYOffset(scrollBottomLimit);
        }
        else if(offset > scrollTopLimit)
        {
            endReached = true;
            scrollingText.setYOffset(scrollTopLimit);
        }
        else
        {
            scrollingText.setYOffset(offset);
        }
        unlockScreen();
        return endReached;
    }

    public void moveScrollableText(OBScrollingText scrollable,float frameFrac)
    {
        float speedY = (float)scrollable.propertyValue("speedy");
        float offsetY = scrollable.yOffset();
        offsetY += speedY * frameFrac;
        float decay = (float)Math.pow(0.99f,frameFrac);
        if(Math.abs(speedY) > applyGraphicScale(0.01f))
            speedY *= decay;
        else
            speedY = 0;
        if(setScrollableTextBoxOffset(scrollable,offsetY))
        {
            setControlSpeed(scrollable,0.0f,0.0f);
        }
        else
        {
            setControlSpeed(scrollable,0.0f,speedY);
        }
    }

    public void checkForNewMedia()
    {
        if(mediaAssets.size() ==0 || !mediaAssets.get(0).isLatestAsset())
        {
            List<OC_PlayZoneAsset> newMediaAssets = (fatController != null) ? fatController.getPlayZoneAssetForCurrentUser() : new ArrayList<OC_PlayZoneAsset>();
            if(newMediaAssets.size() > 0)
            {
                loadAllMediaIcons(newMediaAssets);
                mediaIcons.get(0).hide();
                newMediaAdded = true;
                mediaAssets = newMediaAssets;
            }
        }
    }

    public void animateNewMediaIconOn()
    {
        if(mediaIcons.size() > 0)
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",objectDict.get("bottom_bar_gradient_left").right(),mediaIconsGroup))
                    ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        }
        OBGroup newIcon = loadAssetIcon(mediaAssets.get(0));
        newIcon.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
        newIcon.setScale(1.5f);
        newIcon.setZPosition(100);
        newIcon.setLeft(bounds().width());
        attachControl(newIcon);
        OBControl vidFrame = mediaIcons.get(0);
        float distance = OB_Maths.PointDistance(newIcon.position(), vidFrame.getWorldPosition());
        Path bezPath = OBUtils.SimplePath(newIcon.position(), vidFrame.getWorldPosition(), distance/5);

        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(newIcon,bezPath,false,0),
                OBAnim.scaleAnim(1f,newIcon),
                OBAnim.rotationAnim((float)Math.toRadians(-360),newIcon)),1.2,true,OBAnim.ANIM_EASE_IN,this);
        vidFrame.show();
        detachControl(newIcon);
    }

    public void deleteIcon(OBGroup icon)
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(-360),icon),
                OBAnim.scaleAnim(0f,icon))
                ,0.3,true,OBAnim.ANIM_EASE_IN,this);
        icon.hide();
        if(mediaIcons.size() > 1 && mediaIcons.get(mediaIcons.size()-1) != icon)
        {
            int index = mediaIcons.indexOf(icon);
            OBControl nextIcon = mediaIcons.get(index+1);
            List<OBGroup> icons = mediaIcons.subList(index+2, mediaIcons.size() );
            OBMisc.moveControlWithAttached(nextIcon,(List<OBControl>)(Object)icons,icon.position(),0.3f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        }
        mediaIcons.remove(icon);
        OC_PlayZoneAsset asset = (OC_PlayZoneAsset)icon.propertyValue("asset");
        asset.deleteAssetData();
        if(mediaIcons.size() != 0)
        {
            refreshMediaIconsFrame();
        }
        else
        {
            mediaIconsGroup.hide();
        }
        float limitLeft = objectDict.get("bottom_bar_gradient_left").right();
        float limitRight = objectDict.get("bottom_bar_gradient_right").left();
        if(mediaIconsGroup.left() >= limitLeft)
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",limitLeft,mediaIconsGroup))
                    ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
        else if(mediaIconsGroup.right() < limitRight)
        {
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("right",limitRight,mediaIconsGroup))
                    ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
    }

    public void refreshMediaIconsFrame()
    {
        RectF frame = mediaIconsGroup.members.get(0).getWorldFrame();
        for(int i=1; i<mediaIconsGroup.members.size(); i++)
            if(!mediaIconsGroup.members.get(i).hidden())
                frame.union(mediaIconsGroup.members.get(i).getWorldFrame());
        mediaIconsGroup.setFrame(frame);
    }

    public void startButtonEvent()
    {
        //CheckRecordingPermission();
        if(newMediaAdded)
        {
            animateNewMediaIconOn();
            newMediaAdded = false;

        }
        startFloatLoop(true);
        setStatus(STATUS_AWAITING_CLICK);
    }


    public void doButtonDemo(List<String> audio,long time) throws Exception
    {
        OBControl backButton = objectDict.get("button_back");
        playAudio(audio.get(0));
        for(int i=0; i<3; i++)
        {
            if(statusChanged(time))
                break;
            backButton.highlight();
            waitForSecs(0.3f);
            if(statusChanged(time))
                break;
            backButton.lowlight();
            waitForSecs(0.3f);
        }
        if(!statusChanged(time))
            waitAudio();
        backButton.lowlight();
        if(statusChanged(time))
            return;
        playAudio(audio.get(1));
        waitAudio();
    }


    public void setupBoxMask()
    {
        OBGroup box = (OBGroup)objectDict.get("box");

        OBControl maskFrame =  box.objectDict.get("mask_frame");
        RectF maskRect = maskFrame.getWorldFrame();
        OBControl mask = new OBControl();
        mask.setFrame(maskRect);
        mask.setBackgroundColor(Color.WHITE);
        mask.texturise(false,this);
        PointF centrePoint = maskFrame.getWorldPosition();
        lockScreen();
        for(OBControl menuButton : menuButtons)
        {
            menuButton.hide();
            menuButton.setPosition(centrePoint);
            menuButton.setReversedScreenMaskControl(mask);
        }
        box.show();
        unlockScreen();
    }

    public void demoPlayZoneIntro() throws Exception
    {

        boolean firstEnter = OBUtils.getBooleanValue(parameters.get("first"));
        String scene = firstEnter ? "playzone_1" : "playzone_default";

        final OBGroup box = (OBGroup)objectDict.get("box");
        box.setZPosition(9);
        List<OBAnim> cloudAnim = new ArrayList<>();
        for (OBControl cloud : filterControls("cloud_.*"))
        {
            PointF loc = OBMisc.copyPoint(cloud.position());
            int repos = Integer.valueOf((String) cloud.attributes().get("repos"));

            if (repos == 1)
            {
                cloud.setLeft(bounds().width());
            } else
            {
                cloud.setRight(0);
            }
            cloudAnim.add(OBAnim.moveAnim(loc, cloud));
            cloud.show();
        }
        cloudAnim.add(OBAnim.scaleAnim(box.scale()*2.0f,box));
        cloudAnim.add(OBAnim.propertyAnim("bottom",this.bounds().height(),box));

        OBAnimationGroup.runAnims(cloudAnim, 1, true, OBAnim
                .ANIM_EASE_IN_EASE_OUT, this);
        playAudioQueued((List<Object>)(Object)getAudioForScene(scene,"DEMO"),true);

        playSfxAudio("lid_open", false);
        animateFrames(OBUtils.getFramesList("lid_", 0, 4),
                0.05f, box);
        waitSFX();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1, box.objectDict.get("glow")))
                , 0.2, true, OBAnim.ANIM_LINEAR,
                this);
        setupBoxMask();
        for (OBControl menuButton : menuButtons)
        {
            menuButton.setScale(0.15f);
            menuButton.setProperty("animate", false);
            menuButton.show();
            menuButton.setProperty("collide", false);

        }
        startFloatLoop(false);
        playAudio("pop_combined");
        for (int i = 0; i < menuButtons.size(); i++)
        {
            final OBControl menuButton = menuButtons.get(i);
            float currentAngle = -125.0f + (90.0f / menuButtons
                    .size()) * i;
            setControlSpeed(menuButton, applyGraphicScale(5) * (float) Math.cos((float) Math.toRadians(currentAngle)),
                    applyGraphicScale(5) * (float) Math.sin((float) Math.toRadians(currentAngle)));
            menuButton.setProperty("animate", true);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim((float) menuButton.propertyValue("start_scale"), menuButton)),
                    0.5, false,
                    OBAnim.ANIM_EASE_IN, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            menuButton.setProperty("collide", true);
                        }
                    }, this);
            waitForSecs(0.2f);
        }

        waitForSecs(0.5f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,box.objectDict.get("glow")))
                ,0.2,true,OBAnim.ANIM_LINEAR,this);
        animateFrames(OBUtils.getFramesList("lid_", 4, 0),0.05f,box);

        waitForSecs(0.3f);
        boolean animateMediaIcons = mediaIcons.size() > 0;
        PointF loc = OBMisc.copyPoint(mediaIconsGroup.position());
        if(animateMediaIcons)
        {
            PointF loc2 = OBMisc.copyPoint(mediaIconsGroup.position());
            loc2.x += objectDict.get("media_bg").width();
            mediaIconsGroup.setPosition(loc2);

        }
        List<OBAnim> anims = new ArrayList<>();
        lockScreen();
        for(OBControl con : filterControls("bottom_bar.*"))
        {
            con.setOpacity(0);
            con.show();
            anims.add(OBAnim.opacityAnim(1,con));
        }
        showControls("bottom_bar.*");
        if(animateMediaIcons)
        {
            for(OBControl icon : mediaIcons)
            {
                icon.setOpacity(0);
                anims.add(OBAnim.opacityAnim(1,icon));
            }
            mediaIconsGroup.show();
            anims.add(OBAnim.moveAnim(loc,mediaIconsGroup));
        }

        unlockScreen();
        anims.add(OBAnim.opacityAnim(0,box));
        anims.add(OBAnim.propertyAnim("top",bounds().height(),box));

        waitForSecs(0.3f);
        OBAnimationGroup.runAnims(anims,0.5
                ,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        box.hide();
        boxTouchMode = false;

        long time = setStatus(STATUS_AWAITING_CLICK);
        List<String> audio = getAudioForScene(scene,"PROMPT");
        doButtonDemo(audio,time);
    }

}
