package org.onebillion.onecourse.mainui.oc_playzone;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.View;

import org.apache.commons.io.FileUtils;
import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBRadialGradientPath;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_Menu;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBExpansionManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OC_FatController;

import java.io.File;
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
    List<OBGroup> mediaIcons, menuButtons;
    Map<String, OBGroup> mediaTextThemes;
    List<OC_PlayZoneAsset> mediaAssets;
    OBControl currentMediaLayer, playBackLayer;
    OBGroup mediaIconsGroup, buttonsMaskGroup;
    long dragStartTime, lastFloatLoopTick, lastTouchTime, iconShakeStartTime;
    PointF lastTouchPoint;
    OBVideoPlayer videoPlayer;
    RectF scrollHitBox;
    float dragTravelDistance;
    float leftLimit, rightLimit, topLimit, bottomLimit;
    OC_FatController fatController;

    public String sectionName()
    {
        return "menu";
    }

    public String sectionAudioName()
    {
        return "menu";
    }

    public void prepare()
    {
        fatController = (OC_FatController) MainActivity.mainActivity.fatController;
        mediaTextThemes = new ArrayMap<>();
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();

        OBControl btmLeft = objectDict.get("bottom_bar_left");
        OBControl btmRight = objectDict.get("bottom_bar_right");
        scrollHitBox = new RectF(btmLeft.right(), btmLeft.top(), btmRight.left() , btmLeft.bottom());

        menuButtons = new ArrayList<>();
        OBGroup menuButton = (OBGroup)objectDict.get("menu_button");
        List<String> colours = OBUtils.randomlySortedArray(Arrays.asList(eventAttributes.get("button_colours").split(";")));
        int index = 0;
        List<Float> scaleOptions = OBUtils.randomlySortedArray(Arrays.asList(0.95f,0.975f,1.0f,1.025f,1.05f));
       // boxTouchMode = parameters.get("intro") != null;
        boxTouchMode = false;

        for(OBControl iconCont : filterControls("menu_icon_.*"))
        {
            iconCont.show();
            OBGroup icon = new OBGroup(Arrays.asList(iconCont));
            OBGroup button = (OBGroup)menuButton.copy();
            attachControl(button);
            button.setPosition(menuButton.position());
            button.setScale(menuButton.scale());
            button.show();
            int colour = OBUtils.colorFromRGBString(colours.get(index%colours.size()));
            button.objectDict.get("colour").setFillColor(colour);
            OBRadialGradientPath topGradient = (OBRadialGradientPath)button.objectDict.get("top_gradient");
            int[] gradientColours = topGradient.gradientLayer.colours;
            gradientColours[gradientColours.length - 1] = colour;
            topGradient.gradientLayer.colours = gradientColours;
            OBControl iconMask = button.objectDict.get("icon").copy();
            iconMask.setPosition(menuButton.position());
            iconMask.show();

            PointF relativePoint2 = OB_Maths.relativePointInRectForLocation(icon.position(), button.getWorldFrame());
            button.insertMember(icon,2,"button_icon");
            icon.setScale(icon.scale()/button.scale());
            icon.setPosition(OB_Maths.locationForRect(relativePoint2, button.bounds()));
            icon.setZPosition(2);

            PointF relativePoint = OB_Maths.relativePointInRectForLocation(iconMask.position(), icon.getWorldFrame());
            iconMask.setScale(1.0f/icon.scale());
            iconMask.setPosition(OB_Maths.locationForRect(relativePoint, icon.bounds()));
            icon.setMaskControl(iconMask);

            button.objectDict.get("top_layer").setZPosition(3);
            button.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
            button.setZPosition(10);
            button.setScale(menuButton.scale() * scaleOptions.get(index%scaleOptions.size()));
            menuButtons.add(button);
            prepareForSpeedMeasure(button);
            button.setProperty("start_scale",button.scale());
            button.setProperty("radius",button.width()*0.5f);
            button.setProperty("icon",iconCont);
            index++;
            if(boxTouchMode)
                button.hide();
        }

        Collections.reverse(menuButtons);
        objectDict.get("bottom_bar").setZPosition(20);
        objectDict.get("bottom_bar_gradient_left").setZPosition(49);
        objectDict.get("bottom_bar_gradient_right").setZPosition(49);
        objectDict.get("bottom_bar_left").setZPosition(50);
        objectDict.get("bottom_bar_right").setZPosition(50);
        if(boxTouchMode)
        {
            loadEvent("box");
            loadBoxAndButtonGroup();
        }
        else
        {
            showControls("bottom_bar.*");
            showControls("cloud_.*");
        }
        newMediaAdded = false;
        mediaIcons = new ArrayList<>();
        mediaAssets = fatController.getPlayZoneAssetForCurrentUser();
        if(mediaAssets.size() == 0)
        {

            File copyDir = MainActivity.mainActivity.getDir(OC_PlayZoneAsset.ASSET_FOLDER, Context.MODE_PRIVATE);

            for(String assetFile : OBUtils.filesAtPath("oc-playzone/preload"))
            {
                try
                {
                    InputStream inputStream =  OBUtils.getInputStreamForPath("oc-playzone/preload/"+assetFile);
                    File file =  new File(copyDir.getPath() + "/" + assetFile);
                    OBUtils.copyInputStreamToFile(inputStream, file);

                }
                catch (Exception e)
                {
                    MainActivity.log(e.getMessage());
                }
            }
            for(int i=0; i<4 ; i++)
            {
                Map<String, String> params = new ArrayMap<>();
                params.put("doodle", "doodle_car.png");
                params.put("theme", "transport");
                fatController.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_DOODLE, "thumb_doodle_car.png", params);


                params = new ArrayMap<>();
                params.put("theme", "beach");
                params.put("font", "AklatanicTSO.ttf");
                params.put("text", "\t\tDada anasona kitabu kizuli kuhusu wanyama wa pori");
                fatController.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_TEXT,
                        null, params);

                params = new ArrayMap<>();
                params.put("doodle", "doodle_butterfly.png");
                params.put("theme", "animals");
                fatController.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_DOODLE,
                        "thumb_doodle_butterfly.png", params);

                params = new ArrayMap<>();
                params.put("video", "plane_throw.mov");
                fatController.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_VIDEO, "thumb_plane_throw.jpg", params);

                params = new ArrayMap<>();
                params.put("theme", "sea");
                params.put("font", "AnotherTypewriter.ttf");
                params.put("text", "\t\tJana mama yanku alienda kasini, yeye ni mwalimu");
                fatController.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_TEXT, null, params);
            }
            mediaAssets = fatController.getPlayZoneAssetForCurrentUser();

        }
        videoPlayer = null;
        playBackLayer = new OBControl();
        playBackLayer.setFrame( new RectF(this.bounds()));
        attachControl(playBackLayer);
        playBackLayer.setZPosition(100);
        playBackLayer.hide();
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
                if(mediaIcons.size() > 0 && scrollHitBox.contains( pt.x, pt.y))
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
                setMediaScrollableBoxLoc(OB_Maths.AddPoints(pt, dragOffset));
                measureDragSpeed(target);
            }
            else if(iconsScrollMode)
            {
                setMediaIconsGroupLoc(OB_Maths.AddPoints(pt, dragOffset));
                measureDragSpeed(target);
            }
            else
            {
                setButtonLoc((OBGroup)target,OB_Maths.AddPoints(pt, dragOffset));
                measureDragSpeed(target);
            }
            unlockScreen();
        }
    }


    public void touchUpAtPoint(final PointF pt, View v)
    {
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
                mediaIconsGroup.enable();
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
                    OBControl icon = finger(-1,-1,(List<OBControl>)(Object)mediaIcons,pt);
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
                                if(targ != null)                                    targ.enable();
                                setStatus(STATUS_AWAITING_CLICK);

                            }

                        }
                        else
                        {
                            setStatus(STATUS_BUSY);
                            setControlSpeed(targ,0.0f,0.0f);
                            animateFloat = false;
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
                            checkSectionButton((OBGroup)targ);
                        }
                    });
                }
            }
            else
            {
                if(mediaIsPlaying && currentMediaLayer != null && currentMediaLayer.propertyValue("scrollable_control") != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            startCurrentMediaScroll();
                        }
                    });
                }
                else
                {
                    targ.enable();
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
                if(currentMediaLayer.propertyValue("scrollable_control") != null)
                {
                    OBControl scrollableControl = (OBControl) currentMediaLayer.propertyValue("scrollable_control");
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

    public void startMediaIconsDrag(PointF pt)
    {
        setStatus(STATUS_BUSY);
        OBControl icon = finger(-1, -1, (List<OBControl>)(Object)mediaIcons, pt);
        OBMisc.prepareForDragging(mediaIconsGroup, pt, this);
        prepareForSpeedMeasure(mediaIconsGroup);
        mediaIconsGroup.disable();
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
        OBControl btn = finger(0,0,(List<OBControl>)(Object)menuButtons,pt);
        if(btn != null)
        {
            setStatus(STATUS_BUSY);
            btn.disable();
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
        if(currentMediaLayer == playBackLayer)
        {
            videoPlayer.stop();
        }
        else
        {
            animateMedia(currentMediaLayer,false);
            finishMediaPlaying();
        }
    }

    public void checkSectionButton(OBGroup targ) throws Exception
    {
        OBControl highlight = targ.objectDict.get("highlight");
        highlight.show();
        boolean started = startSectionButton(targ);
        waitForSecs(0.5f);
        targ.enable();
        highlight.hide();
        if(!started)
            setStatus(STATUS_AWAITING_CLICK);

    }

    public void startCurrentMediaScroll() throws Exception
    {
        long time = setStatus(STATUS_AWAITING_CLICK);
        OBControl scrollableControl = (OBControl)currentMediaLayer.propertyValue("scrollable_control");
        long currentTime, lastLoopTime;
        currentTime = lastLoopTime = System.currentTimeMillis();
        float scrollSpeed = (float)scrollableControl.propertyValue("speedy");
        while(time == statusTime && !_aborting && Math.abs(scrollSpeed) > applyGraphicScale(0.01f))
        {
            currentTime = System.currentTimeMillis();
            float frameFrac = (currentTime - lastLoopTime)*1.0f/(TICK_VALUE*1000.0f) ;
            moveScrollableBox(scrollableControl,frameFrac);
            lastLoopTime = currentTime;
            waitForSecs(0.001f);
        }
    }

    public void measureDragSpeed(OBControl con)
    {
        if(con == null)
            return;
        long currentTime = System.currentTimeMillis();
        long lastTime = (long)con.propertyValue("last_action");
        if(currentTime == lastTime)
            return;
        float lastSpeedX = (float)con.propertyValue("speedx");
        float lastSpeedY = (float)con.propertyValue("speedy");
        PointF lastLoc = (PointF)con.propertyValue("last_drag");
        PointF currentLoc = OBMisc.copyPoint(con.position());
        dragTravelDistance += OB_Maths.PointDistance(lastLoc, currentLoc);
        float ticks =  (currentTime - lastTime)*1.0f/(TICK_VALUE*1000.0f);
        float speedX = 0.8f * ((currentLoc.x - lastLoc.x) / ticks) + 0.2f * lastSpeedX;
        float speedY = 0.8f * ((currentLoc.y - lastLoc.y) / ticks) + 0.2f * lastSpeedY;
        setControlSpeed(con,speedX,speedY);
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

    public void floatLoop()
    {
        long currentTime = System.currentTimeMillis();
        float frameFrac = (currentTime - lastFloatLoopTick)/(TICK_VALUE*1000.0f);
        for(OBGroup button : menuButtons)
        {
            if(button.isEnabled())
                moveButtonBySpeed(button,frameFrac);
            checkButtonsForCollisions();

        }
        if(mediaIconsGroup.isEnabled())
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

    public boolean checkButton(OBControl button,OBControl button2)
    {
        if(button2 == button)
            return false;
        float dist = OB_Maths.PointDistance(button.position(), button2.position());
        return dist < collisionDistanceButton(button,button2);
    }

    public void checkButtonsForCollisions()
    {
        for(int i=0; i< menuButtons.size(); i++)
        {
            OBGroup button = menuButtons.get(i);
            if(!(boolean)button.propertyValue("collide"))
                continue;
            boolean precollided = false;
            for(int j=i+1; j< menuButtons.size(); j++)
            {
                OBGroup button2 = menuButtons.get(j);
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
                        if(button.isEnabled())
                            setControlSpeed(button,r1.x,r1.y);
                        if(button2.isEnabled())
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
        setButtonLoc((OBGroup)button,loc ,(boolean)button.propertyValue("collide"));
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

    public void setButtonLoc(OBGroup button,PointF loc)
    {
        setButtonLoc(button,loc,true);
    }

    public boolean setButtonLoc(OBGroup button,PointF loc, boolean collide)
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

    public void buttonSquish(OBGroup button,float angle, float frac)
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


    public boolean startSectionButton(OBControl button)
    {
        OBControl icon = (OBControl)button.propertyValue("icon");
        String target = (String)icon.attributes().get("target");
        String config = (String)icon.attributes().get("config");
        String params = (String)icon.attributes().get("params");
        if(target != null)
        {
            animateFloat = false;

            if(config != null)
                MainActivity.mainActivity.updateConfigPaths(config, false, "en_GB");
            if(MainViewController().pushViewControllerWithNameConfig(target,config,true,true,params))
            {
                return true;
            }
            return false;

        }
        else
        {
            return false;
        }
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
        if(asset.type == OC_PlayZoneAsset.ASSET_VIDEO)
        {
            mbg.setFillColor(Color.RED);
            thumbnail = OBImageManager.sharedImageManager().imageForPath(OC_PlayZoneAsset.pathToAsset(asset.thumbnail));
        }
        else if(asset.type == OC_PlayZoneAsset.ASSET_DOODLE)
        {
            Map<String,String> dataDict = asset.paramsDictionary();
            mbg.setFillColor(Color.BLUE);
            OBImage doodleThumb  = OBImageManager.sharedImageManager().imageForPath(OC_PlayZoneAsset.pathToAsset(asset.thumbnail));
            OBImage gradBg = OBImageManager.sharedImageManager().imageForName("thumbnail_doodle_gradient");
            OBImage overlay = OBImageManager.sharedImageManager().imageForName(String.format("thumbnail_doodle_%s",dataDict.get("theme")));
            overlay.setZPosition(1);
            OBMisc.scaleControlToControl(doodleThumb,gradBg,false);
            gradBg.setPosition(OB_Maths.locationForRect(0.5f,0.5f,overlay.frame()));
            gradBg.setZPosition(2);
            doodleThumb.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 0.5f), gradBg.bounds));
            gradBg.setMaskControl(doodleThumb);
            thumbnail = new OBGroup((List<OBControl>)(Object)Arrays.asList(overlay,gradBg));
        }
        else if(asset.type == OC_PlayZoneAsset.ASSET_TEXT)
        {
            mbg.setFillColor(Color.GREEN);
            Map<String,String> dataDict = asset.paramsDictionary();
            OBImage overlay = loadImageWithName(String.format("thumbnail_type_%s",dataDict.get("theme")),new PointF(0.5f, 0.5f),new RectF(this.bounds()));
            overlay.setZPosition(3);
            String text = dataDict.get("text");
            Typeface currentFont =  OBUtils.TypefaceForFile(dataDict.get("font"));
            float fontSize = applyGraphicScale(13);
            OBLabel thumbnailText = new OBLabel(text,currentFont,fontSize);
            thumbnailText.setFrame(0,0,overlay.width()*0.7f, overlay.height()*0.9f);
            thumbnailText.setPosition(OB_Maths.locationForRect(0.5f,0.5f,overlay.frame()));
            thumbnailText.setTop(overlay.top() + applyGraphicScale(10));
            //(CATextLayer*)thumbnailText.layer.setWrapped(true);
            // (CATextLayer*)thumbnailText.layer.setAlignmentMode(kCAAlignmentLeft);
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
        }
        mrect.setPosition(thumbnail.position());
        mrect.setZPosition(1);
        thumbnail.setZPosition(2);
        OBMisc.scaleControlToControl(thumbnail,mrect,true);
        OBControl crossCopy = objectDict.get("media_delete_cross").copy();
        crossCopy.setRight(mrect.right());
        crossCopy.setTop(mrect.top());
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
                OC_PlayZoneAsset asset = (OC_PlayZoneAsset)icon.propertyValue("asset");
                if (asset.type == OC_PlayZoneAsset.ASSET_VIDEO)
                {
                    loadVideoForAsset(asset, icon);
                } else if (asset.type == OC_PlayZoneAsset.ASSET_TEXT)
                {
                    loadTextForAsset(asset, icon);
                } else if (asset.type == OC_PlayZoneAsset.ASSET_DOODLE)
                {
                    loadDoodleForAsset(asset, icon);

                }
                mediaIsPlaying = true;
                setStatus(STATUS_AWAITING_CLICK);
            }
        });
    }

    public void loadDoodleForAsset(OC_PlayZoneAsset asset,OBControl icon)
    {
        Map<String,String> params = asset.paramsDictionary();
        //  OC_DoodleGradientManager gradientManager = null;
        lockScreen();
        OBImage currentImage = OBImageManager.sharedImageManager().imageForPath(OC_PlayZoneAsset.pathToAsset(params.get("doodle")));
        List<OBControl> controls = loadEvent(String.format("doodle_%s",params.get("theme")));
        OBPath blackboard = (OBPath)objectDict.get("blackboard");
        // gradientManager = [OC_DoodleGradientManager.alloc()initWithPath:blackboard controller:;
        OBMisc.scaleControlToControl(currentImage,blackboard,true);
        currentImage.setPosition ( OB_Maths.locationForRect(new PointF(0.5f, 0.5f), blackboard.bounds));
        // gradientManager.control.setMaskControl ( currentImage);
        // controls.add(gradientManager.control);
        OBPath topborder = (OBPath)blackboard.copy();
        topborder.setFillColor(-1);
        topborder.setZPosition(100);
        controls.add(topborder);
        OBGroup imageGroup = new OBGroup(controls);
        imageGroup.setBounds (  objectDict.get("obj_background") .frame());
        RectF iconFrame = icon.getWorldFrame();
        imageGroup.setPosition(icon.getWorldPosition());
        imageGroup.setScale(iconFrame.height()/imageGroup.height());
        imageGroup.setProperty("parent_icon",icon);
        imageGroup.setProperty("delete_after",true);
        //imageGroup.setProperty("gradient_manager",gradientManager);
        imageGroup.setProperty("target_scale",1.0f);
        imageGroup.setZPosition ( 100);
        imageGroup.show();
        attachControl(imageGroup);
        currentMediaLayer = imageGroup;
        unlockScreen();
        animateMedia(currentMediaLayer,true);
        // gradientManager.startGradientAnimation();
    }

    public void loadVideoForAsset(OC_PlayZoneAsset asset,OBControl icon)
    {
        final Map<String,String> params = asset.paramsDictionary();
        if(videoPlayer == null)
        {
            videoPlayer = new OBVideoPlayer(playBackLayer.frame(),this);
            attachControl(videoPlayer);
            videoPlayer.setZPosition(100);
        }

        RectF videoIconFrame = icon.getWorldFrame();
        float targetScale = videoIconFrame.height()/videoPlayer.height();
        PointF videoIconLoc = icon.getWorldPosition();
        videoPlayer.setPosition(videoIconLoc);
        videoPlayer.setScale(targetScale);
        videoPlayer.setProperty("parent_icon",icon);
        videoPlayer.show();

        animateMedia(videoPlayer, true);
        currentMediaLayer = videoPlayer;
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(params.get("video"));


                videoPlayer.startPlayingAtTime(afd, 0, new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                setStatus(STATUS_BUSY);
                                animateMedia(videoPlayer, false);
                                videoPlayer.setScale(1);
                                finishMediaPlaying();
                            }
                        });
                    }
                });
            }
        });
    }

    public void loadTextForAsset(OC_PlayZoneAsset asset,OBControl icon)
    {
        Map<String,String> params = asset.paramsDictionary();
        String theme = params.get("theme");
        String font = params.get("font");
        String text = params.get("text");
        lockScreen();
        Typeface currentFont =  OBUtils.TypefaceForFile(font);
        OBGroup fullGroup = null;
        if(mediaTextThemes.get(theme) == null)
        {
            List<OBControl> controls = loadEvent(String.format("typewrite_%s",theme));
            /*for(OBControl con : controls)
            {
                con.setRasterScale(con.scale());
            }*/
            OBControl bg = objectDict.get("text_box_bg");
            float zPosition = bg.zPosition();
            float fontSize = applyGraphicScale(60);

            OBLabel textBox = new OBLabel(text,currentFont,fontSize);
            textBox.setProperty("start_width",bg.width() - applyGraphicScale(40));
            // (CATextLayer*)textBox.layer.setWrapped(true);
            // (CATextLayer*)textBox.layer.setAlignmentMode(kCAAlignmentLeft);
            textBox.setZPosition ( 5);
            textBox.setColour(Color.BLACK);
            // textBox.setFrame ( [frameForTextBox:textBox attributedString:currentText]);
            OBControl gradientTop = objectDict.get("text_box_gradient_top");
            OBControl gradientBottom = objectDict.get("text_box_gradient_bottom");
            gradientTop.setWidth(bg.width()-bg.lineWidth());
            gradientBottom.setWidth(bg.width()-bg.lineWidth());
            OBGroup group = new OBGroup((List<OBControl>)(Object)Arrays.asList(textBox));
            group.setBounds(new RectF(0, 0, bg.bounds.width(), gradientBottom.bottom()-gradientTop.top()));
            textBox.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 0.5f), textBox.parent.bounds));
            textBox.setTop(objectDict.get("text_box_gradient_top").height());
            group.setMasksToBounds(true);
            group.setZPosition(zPosition+0.0001f);
            group.setPosition(bg.position());
            group.setTop(gradientTop.top());
            textBox.setProperty("top_limit",textBox.top());
            textBox.setProperty("bottom_limit",group.height()/2.0f);
            controls.add(group);
            fullGroup = new OBGroup(controls);
            RectF frame = fullGroup.frame();
            fullGroup.setBounds(new RectF(-frame.left,-frame.top,this.bounds().width(),this.bounds().height()));
            fullGroup.setMasksToBounds(true);
            fullGroup.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
            fullGroup.setProperty("text_box",textBox);
            attachControl(fullGroup);
            mediaTextThemes.put(theme,fullGroup);
            fullGroup.setZPosition(100);
            fullGroup.setScale(icon.height()/fullGroup.height());

        }
        else
        {
            fullGroup = mediaTextThemes.get(theme);
            OBLabel textBox = (OBLabel)fullGroup.propertyValue("text_box");
            textBox.setTypeFace(currentFont);
            // textBox.setFrame  ( [frameForTextBox:textBox attributedString:currentText]);
            textBox.setString(text);
            textBox.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 0.5f), textBox.parent.bounds));
            textBox.setTop((float)textBox.propertyValue("top_limit"));

        }
        fullGroup.setPosition(icon.getWorldPosition());
        currentMediaLayer = fullGroup;
        unlockScreen();
        currentMediaLayer.setProperty("scrollable_control",currentMediaLayer.propertyValue("text_box"));
        currentMediaLayer.setProperty("parent_icon",icon);
        animateMedia(currentMediaLayer,true);
    }

    /*public RectF frameForTextBox(OBControl textBox, String string)
    {
        CGRect textBoxSize = (float)string boundingRectWithSize:CGSizeMake([textBox.propertyValue("start_width"),  CGFLOAT_MAX)
        options:(StringDrawingUsesLineFragmentOrigin|StringDrawingUsesFontLeading) context:null];
        return CGRectMake(textBoxSize.origin.x, textBoxSize.origin.y, (float)textBox.propertyValue("start_width"), textBoxSize.size.height + applyGraphicScale(10));
    }*/

    public void finishMediaPlaying()
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(currentMediaLayer.propertyValue("gradient_manager") != null)
                {
                   /* OC_DoodleGradientManager *gradManager = currentMediaLayer.propertyValue("gradient_manager");
                    gradManager.stopGradientAnimation();
                    gradManager.cleanUp();
                    currentMediaLayer.setProperty("gradient_manager",null);*/

                }
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
        OBControl icon = (OBControl)mediaLayer.propertyValue("parent_icon");
        float targetScale = mediaLayer.propertyValue("target_scale") != null ? (float)mediaLayer.propertyValue("target_scale")  : 1;
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(zoomIn ? targetScale : icon.height()/(mediaLayer.height()/targetScale),mediaLayer),
                OBAnim.moveAnim(zoomIn ? OB_Maths.locationForRect(0.5f,0.5f, this.bounds()) : icon.getWorldPosition() ,mediaLayer))
                ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        if(!zoomIn)
            mediaLayer.hide();
    }

    public boolean setMediaScrollableBoxLoc(PointF loc)
    {
        if(currentMediaLayer == null || currentMediaLayer.propertyValue("scrollable_control") == null)
            return true;
        OBControl scrollableControl = (OBControl)currentMediaLayer.propertyValue("scrollable_control");
        boolean endReached = false;
        if(scrollableControl.parent.bounds.height() > scrollableControl.height())
            return true;
        loc.x = scrollableControl.position().x;
        float scrollTopLimit = (float)scrollableControl.propertyValue("top_limit") ;
        float scrollBottomLimit = (float)scrollableControl.propertyValue("bottom_limit") ;
        lockScreen();
        scrollableControl.setPosition(loc);
        if(scrollableControl.top() > scrollTopLimit)
        {
            endReached = true;
            scrollableControl.setTop ( scrollTopLimit);
        }
        else if(scrollableControl.bottom() < scrollBottomLimit)
        {
            endReached = true;
            scrollableControl.setBottom ( scrollBottomLimit);
        }
        unlockScreen();
        return endReached;
    }

    public void moveScrollableBox(OBControl scrollable,float frameFrac)
    {
        float speedY = (float)scrollable.propertyValue("speedy");
        PointF loc = scrollable.position();
        loc.y += speedY * frameFrac;
        float decay = (float)Math.pow(0.99f,frameFrac);
        if(Math.abs(speedY) > applyGraphicScale(0.01f))
            speedY *= decay;
        else
            speedY = 0;
        if(setMediaScrollableBoxLoc(loc))
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
            List<OC_PlayZoneAsset> newMediaAssets = fatController.getPlayZoneAssetForCurrentUser();
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

    public void loadBoxAndButtonGroup()
    {
        OBGroup box = (OBGroup)objectDict.get("box");
        box.setBottom(bounds().height());
        RectF maskFrame = box.objectDict.get("mask_frame").getWorldFrame();
        OBControl maskPart1 = new OBControl();
        maskPart1.setFrame(new RectF(0, 0, maskFrame.left, bounds().height()));
        maskPart1.setBackgroundColor(Color.BLUE);
        OBControl maskPart2 = maskPart1.copy();
        maskPart2.setFrame(new RectF(0, 0,  bounds().width(), maskFrame.right));
        OBControl maskPart3 = maskPart1.copy();
        maskPart3.setFrame(new RectF(maskFrame.right, 0,  bounds().width() - maskFrame.right, bounds().height()));
        PointF centrePoint = box.objectDict.get("mask_frame").getWorldPosition();
        OBGroup mask = new OBGroup(Arrays.asList(maskPart1,maskPart2,maskPart3));
        OBControl frameCont = new OBControl();
        frameCont.setFrame(new RectF(bounds()));
        List<OBControl> controls = new ArrayList<>((List<OBControl>)(Object)menuButtons);
        controls.add(frameCont);
        buttonsMaskGroup=  new OBGroup(controls);
        buttonsMaskGroup.setZPosition(10);
        attachControl(buttonsMaskGroup);
        buttonsMaskGroup.setMaskControl(mask);
        lockScreen();
        for(OBControl menuButton : menuButtons)
        {
            menuButton.hide();
            menuButton.setPosition ( centrePoint);
        }
        box.setPosition ( OB_Maths.locationForRect(0.5f,0.4f,this.bounds()));
        box.show();
        unlockScreen();
    }

    public void demoPlayZoneIntro() throws Exception
    {
        playAudio("box_drop");
        final OBGroup box = (OBGroup)objectDict.get("box");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("bottom",bounds().height(),box),
                OBAnim.rotationAnim((float)Math.toRadians(-15),box))
                ,0.3,true,OBAnim.ANIM_EASE_IN,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("bottom",bounds().height(),box),
                OBAnim.rotationAnim((float)Math.toRadians(0),box))
                ,0.1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        final OBSectionController sectionController = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                waitSFX();
                List<OBAnim> cloudAnim = new ArrayList<>();
                for (OBControl cloud : filterControls("cloud_.*"))
                {
                    PointF loc = cloud.position();
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
                OBAnimationGroup.runAnims(cloudAnim, 1, true, OBAnim
                        .ANIM_EASE_IN_EASE_OUT, sectionController);
                playSfxAudio("lid_open", false);
                animateFrames(OBUtils.getFramesList("lid_", 0, 4),
                        0.05f, box);
                waitSFX();
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1, box.objectDict.get("glow")))
                        , 0.2, true, OBAnim.ANIM_LINEAR,
                        sectionController);
                for (OBControl menuButton : menuButtons)
                {
                    menuButton.setScale(0.15f);
                    menuButton.disable();
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
                    menuButton.enable();
                    OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim((float) menuButton.propertyValue("start_scale"), menuButton)),
                            0.5, false,
                            OBAnim.ANIM_EASE_IN, new OBUtils.RunLambda()
                            {
                                @Override
                                public void run() throws Exception
                                {
                                    menuButton.setProperty("collide", true);
                                }
                            }, sectionController);
                }
                waitForSecs(0.2f);
            }
        });
        waitForSecs(0.5f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,box.objectDict.get("glow")))
                ,0.2,true,OBAnim.ANIM_LINEAR,this);
        animateFrames(OBUtils.getFramesList("lid_", 4, 0),0.05f,box);
        buttonsMaskGroup.setMaskControl ( null);
        waitForSecs(0.3f);
        boolean animateMediaIcons = mediaIcons.size() > 0;
        PointF loc = OBMisc.copyPoint(mediaIconsGroup.position());
        if(animateMediaIcons)
        {
            PointF loc2 = OBMisc.copyPoint(mediaIconsGroup.position());
            loc2.x += objectDict.get("media_bg").width();
            mediaIconsGroup.setPosition ( loc2);

        }
        List<OBAnim> anims = new ArrayList<>();
        lockScreen();
        for(OBControl con : filterControls("bottom_bar.*"))
        {
            con.setOpacity ( 0);
            con.show();
            anims.add(OBAnim.opacityAnim(1,con));
        }
        showControls("bottom_bar.*");
        if(animateMediaIcons)
        {
            mediaIconsGroup.setOpacity ( 0);
            mediaIconsGroup.show();
        }

        unlockScreen();
        anims.add(OBAnim.opacityAnim(0,box));
        anims.add(OBAnim.propertyAnim("top",bounds().height(),box));
        if(animateMediaIcons)
        {
            anims.add(OBAnim.opacityAnim(1,mediaIconsGroup));
            anims.add(OBAnim.moveAnim(loc,mediaIconsGroup));

        }
        waitForSecs(0.3f);
        OBAnimationGroup.runAnims(anims,0.5
                ,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        boxTouchMode = false;
        setStatus(STATUS_AWAITING_CLICK);

    }

}
