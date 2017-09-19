package org.onebillion.onecourse.mainui.oc_community;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.InputType;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_Menu;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutFloat;
import org.onebillion.onecourse.utils.OCM_FatController;
import org.onebillion.onecourse.utils.OCM_FatReceiver;
import org.onebillion.onecourse.utils.OCM_MlUnit;
import org.onebillion.onecourse.utils.OCM_MlUnitInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by michal on 03/08/2017.
 */

public class OCM_ChildMenu extends OC_Menu implements OCM_FatReceiver
{
    final static String SETUP_MENU_PASSWORD = "obxprize";

    final static int TARGET_BUTTON = 1,
            TARGET_STUDY = 2,
            TARGET_COMMUNITY = 3;

    int lastCommand, lastUnitOrder;
    int currentDay;
    OBImage currentBigIcon, previousBigIcon;
    OCM_FatController fatController;
    OCM_MlUnitInstance lastUnitInstance;
    OBLabel currentLevelLabel;
    OBPresenter presenter;
    int currentTarget;
    Map<String,Integer> coloursDict;
    OBControl emitter;
    boolean communityModeActive, playZoneOpened;
    List<OBGroup> communityModeIcons;

    RectF secretBoxLeft, secretBoxRight;
    List<RectF> secretBoxUnlockList;
    int secretBoxTouchIndex;

    /*
    OC_Section functions
     */

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
        lastUnitInstance = null;
        if(params != null && params.get("code") != null)
        {
            lastCommand = (int)params.get("code");
            if(lastCommand == OCM_FatController.OFC_SESSION_LOCKED ||
                    lastCommand == OCM_FatController.OFC_BATTERY_LOW ||
                    lastCommand == OCM_FatController.OFC_SESSION_NEW)
            {
                objectDict.get("overlay_button").show();

            }
            else if(params.get("instance") != null)
            {
                lastUnitInstance = (OCM_MlUnitInstance)params.get("instance");
                if(lastCommand == OCM_FatController.OFC_UNIT_FAILED && lastUnitInstance.starColour >0)
                    colourStarNum(lastUnitOrder,lastUnitInstance.starColour);
            }
        }
        else
        {
            lastCommand = -1;
        }
    }

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        objectDict.get("overlay_button").setZPosition(60);
        objectDict.get("button_start").setZPosition(60.5f);

        this.localisations = loadLocalisations(getLocalPath("_localisations.xml"));
        currentBigIcon = null;
        currentLevelLabel = new OBLabel("88888888888888888888",OBUtils.standardTypeFace(),applyGraphicScale(30));
        currentLevelLabel.show();
        currentLevelLabel.setString("1");
        currentLevelLabel.setPosition(OB_Maths.locationForRect(0.97f,0.9f,this.bounds()));
        currentLevelLabel.setRight(this.bounds().width() - applyGraphicScale(10));
        currentLevelLabel.setColour(Color.BLACK);
        boolean showUnitID = MainActivity.mainActivity.configBooleanForKey(MainActivity.CONFIG_SHOW_UNIT_ID);
        if (showUnitID)
        {
            attachControl(currentLevelLabel);
        }
        coloursDict = OBMisc.loadEventColours(this);
        fatController = (OCM_FatController)MainActivity.mainActivity.fatController;
        fatController.menu = this;

        fatController.loadBatteryIcon(this);
        fatController.colourDict = coloursDict;
        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("start_loc",OBMisc.copyPoint(presenter.control.position()));
        presenter.faceFront();
        presenter.control.setRight ( 0);
        presenter.control.setProperty("end_loc",OBMisc.copyPoint(presenter.control.position()));
        lastUnitOrder = -1;
        lastUnitInstance = null;
        refreshCurrentDayAndAudio();
        receiveCommand(fatController.getCurrentCommand());
        communityModeActive = fatController.communityModeActive();
        playZoneOpened = false;
        initScreen();

        for(OBControl star : filterControls("top_bar_star_.*"))
            star.setProperty("start_loc",OBMisc.copyPoint(star.position()));
        previousBigIcon = null;
        OBGroup box = (OBGroup)objectDict.get("box");
        box.setScale(applyGraphicScale(0.9f));
        OBControl lidOverlay = box.objectDict.get("lid_glow_overlay");
        lidOverlay.setProperty("start_height",lidOverlay.height());
        lidOverlay.setProperty("start_top",lidOverlay.top());

        float boxSize = 0.1f*this.bounds().height();
        secretBoxLeft = new RectF(0,this.bounds().bottom -boxSize,boxSize,this.bounds().bottom);
        secretBoxRight = new RectF(this.bounds().right - boxSize,this.bounds().bottom -boxSize,this.bounds().right,this.bounds().bottom);
        secretBoxUnlockList = Arrays.asList(secretBoxLeft,secretBoxRight,secretBoxLeft,secretBoxLeft,secretBoxRight);
        secretBoxTouchIndex = 0;
    }

    public void refreshCurrentDayAndAudio()
    {
        currentDay = fatController.getCurrentDay();
        loadAudioForDay(currentDay);
    }

    public void initScreen()
    {
        resetPresenter();
        if(lastCommand == OCM_FatController.OFC_SESSION_LOCKED || lastCommand == OCM_FatController.OFC_BATTERY_LOW)
        {
            return;

        }
        if(lastCommand == OCM_FatController.OFC_SESSION_NEW && !communityModeActive)
        {
            showNewDayScreen();
            loadEmptyStarBar(false);
            currentTarget = TARGET_BUTTON;

        }
        else if(communityModeActive)
        {
            hideStarBar();
            loadTopBar(true,true);
            loadIconsGridForUnits(fatController.getUnitsForGrid(),true);
            loadPlayZoneBox(true);
            if(lastCommand == OCM_FatController.OFC_SESSION_NEW)
            {
                showNewDayScreen();
                currentTarget = TARGET_BUTTON;
            }
            else
            {
                currentTarget = TARGET_COMMUNITY;
            }
        }
        else
        {
            loadStarBar();
            currentTarget = TARGET_STUDY;
        }

    }

    @Override
    public void viewWillAppear(Boolean animated)
    {
        super.viewWillAppear(animated);
        if(objectDict.get("overlay_box") != null)
            objectDict.get("overlay_box").hide();
        if(objectDict.get("box") != null)
            stopBoxGlowAnimate();
    }

    @Override
    public int buttonFlags()
    {
        return 0;
    }

    @Override
    public void start()
    {
        if(checkCurrentCommand())
            return;
        startNextEvent();
    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (currentTarget == TARGET_BUTTON)
            {
                final OBControl button = finger(0, 1, Arrays.asList(objectDict.get("button_start")), pt);
                if (button != null)
                {
                    setStatus(STATUS_BUSY);
                    secretBoxTouchIndex = 0;
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            playAudio(null);
                            button.highlight();
                            if(fatController.startCurrentSession())
                                refreshCurrentDayAndAudio();
                            waitForSecs(0.5f);
                            hideNewDayScreen();
                            button.lowlight();
                            currentTarget = communityModeActive ? TARGET_COMMUNITY : TARGET_STUDY;
                            startNextEvent();
                        }
                    });

                }
            }
            else if (currentTarget == TARGET_STUDY &&
                    currentBigIcon != null && currentBigIcon.frame.contains(pt.x, pt.y))
            {
                setStatus(STATUS_BUSY);
                secretBoxTouchIndex = 0;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkBigButton();
                    }

                });
            }
            else if(currentTarget ==  TARGET_COMMUNITY)
            {
                final OBControl smallButton = finger(0,1,(List<OBControl>)(Object)communityModeIcons,pt);
                if(smallButton != null)
                {

                    setStatus(STATUS_BUSY);
                    secretBoxTouchIndex = 0;
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            playAudio(null);
                            stopBoxGlowAnimate();
                            checkSmallButton((OBGroup)smallButton);
                        }
                    });
                }
                else if(finger(0,1,Arrays.asList(objectDict.get("box")),pt) != null)
                {

                    if(checkPlayZoneBoxStatus())
                    {
                        setStatus(STATUS_BUSY);
                        secretBoxTouchIndex = 0;
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                playAudio(null);
                                stopBoxGlowAnimate();
                                demo_playzone();
                                playZoneOpened = true;
                                resetCommunityGrid();
                            }
                        });
                    }
                    else
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            @Override
                            public void run() throws Exception
                            {
                                playAudioQueued((List<Object>)(Object)getAudioForScene("grid","INCORRECT"));
                            }
                        });
                    }
                }
            }
            checkSecretTouchBox(pt);
        }
    }

    public void delayedSecretIndexReset(final long time)
    {
        OBUtils.runOnOtherThreadDelayed(2, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(time == statusTime)
                    secretBoxTouchIndex = 0;
            }
        });
    }

    public void checkBigButton() throws Exception
    {
        currentBigIcon.highlight();
        OCM_MlUnit unit = (OCM_MlUnit)currentBigIcon.settings.get("unit");
        fatController.startSectionByUnit(unit, true, new OCM_FatController.SectionOpeningCallback()
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
                        currentBigIcon.lowlight();
                        resetPresenter();
                        if(!success)
                        {
                            fatController.triggerTimeoutUnit();
                            start();
                        }
                    }
                });
            }
        });
    }


    public void checkSmallButton(OBGroup button) throws Exception
    {
        final OBControl icon = button.objectDict.get("icon");
        icon.setHighlightColour(Color.argb(100,0,0,0));
        OCM_MlUnit unit = (OCM_MlUnit)button.settings.get("unit");
        fatController.startSectionByUnit(unit, false, new OCM_FatController.SectionOpeningCallback()
        {
            @Override
            public void run(final OCM_MlUnitInstance unitInstance,final boolean success)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        playAudio(null);
                        waitForSecs(0.5f);
                        icon.lowlight();
                        resetPresenter();
                        if(!success)
                        {
                            fatController.triggerTimeoutUnit();
                            start();
                        }
                    }
                });
            }
        });

    }

    @Override
    public void cleanUp()
    {
        super.cleanUp();
        fatController.menu = null;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (checkCurrentCommand())
            return;
        if (status() == STATUS_AWAITING_CLICK)
        {
            final long time = setStatus(STATUS_AWAITING_CLICK);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    if (communityModeActive)
                        stopBoxGlowAnimate();
                    replayReminders(time);
                }
            });
        }
    }

    @Override
    public void onAlarmReceived(Intent intent)
    {
        super.onAlarmReceived(intent);
        checkCurrentCommand();
    }

    @Override
    public void onBatteryStatusReceived(final float level, final boolean charging)
    {
        super.onBatteryStatusReceived(level,charging);
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                fatController.refreshBatteryStatus(level,charging,controller);
            }
        });
    }

    /*
    Event functions
     */

    public void startNextEvent()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if (lastCommand == OCM_FatController.OFC_UNIT_TIMED_OUT && currentBigIcon != null)
                {
                    waitForSecs(0.3f);
                    playUnitButtonAudio(null);
                }
                else if (lastCommand == OCM_FatController.OFC_SESSION_NEW && currentTarget == TARGET_BUTTON)
                {
                    demo_button();
                }
                else if (lastCommand == OCM_FatController.OFC_SESSION_LOCKED)
                {
                    // demo_presenter_timeout();
                }
                else
                {
                    if (lastCommand >= 0 && lastCommand != OCM_FatController.OFC_UNIT_FAILED &&
                            lastUnitInstance != null && lastUnitInstance.starColour > 0)
                    {
                        if (communityModeActive)
                        {
                            demo_community_star_award();

                        }
                        else
                        {
                            demo_study_star_award();
                        }
                        lastCommand = -1;
                    }
                    if (!communityModeActive)
                    {
                        Map<String,Object> unitData = fatController.getNextUnitData();
                        if (unitData.get("community")!= null && (boolean)unitData.get("community"))
                        {
                            communityModeActive = true;
                            loadIconsGridForUnits(fatController.getUnitsForGrid(),false);
                            loadTopBar(false,currentDay % 7 != 0);
                            loadPlayZoneBox(false);
                            currentTarget = TARGET_COMMUNITY;
                            demo_grid();
                            return;
                        }
                        else if (unitData.get("unit") != null)
                        {
                            loadBigIconForUnit((OCM_MlUnit)unitData.get("unit"));
                            lastUnitOrder = (int) unitData.get("unitOrder");
                        }
                        if(unitData.get("unitOrder") != null)
                        {
                            String eventName = String.format("unit_%d", (int) unitData.get("unitOrder"));
                            String demoPrefix = currentDay > 4 ? "demo_default_" : "demo_start_";
                            if (!(audioAvailable(eventName) && performSel(demoPrefix, eventName)))
                            {
                                animateBigIconSlide();
                                playUnitButtonAudio("unit_default");
                            }
                        }
                        else
                        {
                            animateBigIconSlide();
                            playUnitButtonAudio("unit_default");
                        }

                    } else
                    {
                        long time = setStatus(STATUS_AWAITING_CLICK);
                        checkPlayZoneBoxStatus();
                        if (playZoneOpened)
                        {
                            playZoneOpened = false;
                            demo_playzone_back(time);
                            animateNextGridIconShake(time);
                        }
                        else
                        {
                            animateNextGridIconShake(time);
                        }
                    }
                }
            }
        });
    }

    public boolean audioAvailable(String eventName)
    {
        return audioScenes.containsKey(eventName);

    }
    public void replayReminders(long time) throws Exception
    {
        waitForSecs(1);
        if(communityModeActive)
        {
            animateNextGridIconShake(time);
        }
        if (currentTarget == TARGET_BUTTON)
        {
            playAudioQueued((List<Object>)(Object)getAudioForScene("button","REMIND"),true);
        }
        else if(!communityModeActive)
        {
            playAudioQueued((List<Object>)(Object)getAudioForScene("unit_default","REMIND"),true);
        }
    }

    public void resetBarStars()
    {
        for(OBControl star : filterControls("top_bar_star_.*"))
        {
            star.setPosition((PointF)star.propertyValue("start_loc"));
            star.hide();
            star.setOpacity ( 1);
        }

    }

    public boolean checkCurrentCommand()
    {
        if(status() == STATUS_EXITING || _aborting)
            return false;
        int code = (int)fatController.getCurrentCommand().get("code") ;
        if(code == OCM_FatController.OFC_SESSION_LOCKED || code == OCM_FatController.OFC_BATTERY_LOW)
        {
            closeThisMenuAndOpen(OCM_LockScreen.class);
            return true;
        }
        if(currentTarget == TARGET_BUTTON && fatController.currentSessionReadyToStart())
            return false;
        if(fatController.checkAndPrepareNewSession())
        {
            closeThisMenuAndOpen(OCM_ChildMenu.class);
            return true;
        }
        objectDict.get("overlay_button").hide();
        if(status() == STATUS_AWAITING_CLICK && currentTarget == TARGET_COMMUNITY)
        {
            checkPlayZoneBoxStatus();
        }
        return false;
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

    public void refreshCurrentLabel(OCM_MlUnit unit)
    {
        if (unit == null)
            return;
        currentLevelLabel.setString ( String.format("%d - %d", currentDay, unit.unitIndex));
    }

    public void refreshCurrentLabelArray(List<OCM_MlUnit> units)
    {
        OCM_MlUnit unit1 = units.get(0);
        OCM_MlUnit unit2 = units.get(units.size()-1);
        currentLevelLabel.setString ( String.format("%d - %d - %d", currentDay, unit1.unitIndex,  unit2.unitIndex));
    }

    public void showNewDayScreen()
    {
        objectDict.get("overlay_button").show();
        objectDict.get("button_start").setOpacity(0);
        objectDict.get("button_start").show();
    }

    public void hideNewDayScreen()
    {
        objectDict.get("overlay_button").hide();
        objectDict.get("button_start").hide();
    }

    public void loadAudioForDay(int day)
    {
        String sectionName = String.format("sz%s", day > 4 ? "default" : "start");
        String path = getConfigPath(String.format("%saudio.xml", sectionName));
        if(path == null)
            return;
        loadAudioXML(path);
    }

    public void playUnitButtonAudio(String event) throws Exception
    {
        playUnitButtonAudio(event,setStatus(STATUS_AWAITING_CLICK));
    }

    public void playUnitButtonAudio(String event,long time) throws Exception
    {
        OBMisc.doSceneAudio(4,event,time,this);
    }

    public void loadIconsGridForUnits(List<OCM_MlUnit> unitsList,boolean show)
    {
        List<OBGroup> list = new ArrayList<>();
        OBControl workrect = objectDict.get("work_rect");
        OBGroup smallStar = (OBGroup)objectDict.get("icon_small_star");
        PointF relativeStarLoc = OB_Maths.relativePointInRectForLocation(smallStar.position(), objectDict.get("icon_small").frame());
        int index = 0;
        for(OCM_MlUnit unit : unitsList)
        {
            OBImage unitIcon = loadIconForUnit(unit,true);
            unitIcon.setPosition(OB_Maths.locationForRect(1/4.0f * (index%5),1/2.0f * (float)Math.floor(index/5) ,workrect.frame()));
            OBGroup star = (OBGroup)smallStar.copy();

            unitIcon.setZPosition(1);
            star.setZPosition(2);

            star.setPosition(OB_Maths.locationForRect(relativeStarLoc, unitIcon.frame()));
            if(show)
                star.show();
            colourStar(star,-1);
            OBGroup groupUnit = new OBGroup(Arrays.asList((OBControl)unitIcon));
            OBGroup buttonGroup = new OBGroup(Arrays.asList((OBControl)groupUnit,star));
            buttonGroup.objectDict.put("star",star);
            buttonGroup.objectDict.put("icon",unitIcon);
            setObjectShadow(groupUnit, unitIcon);
            unitIcon.setShadowOpacity(0);
            groupUnit.sizeBoundsToShadow();
            buttonGroup.setProperty("completed",false);
            if(!show)
                buttonGroup.hide();
            buttonGroup.setProperty("unit",unit);
            attachControl(buttonGroup);
            list.add(buttonGroup);
            index++;
        }
        communityModeIcons = list;
        refreshCurrentLabelArray(unitsList);
    }


    public void resetCommunityGrid()
    {
        for(OBGroup button : communityModeIcons)
        {
            OBGroup star = (OBGroup)button.objectDict.get("star");
            colourStar(star,-1);
            button.setProperty("completed",false);
        }
    }

    public void loadBigIconForUnit(OCM_MlUnit unit)
    {
        previousBigIcon = currentBigIcon;
        currentBigIcon = loadIconForUnit(unit,false);
        currentBigIcon.setZPosition(20);
        currentBigIcon.setLeft(currentBigIcon.left() + this.bounds().width());
        currentBigIcon.show();
        attachControl(currentBigIcon);
        refreshCurrentLabel(unit);
    }

    public OBImage loadIconForUnit(OCM_MlUnit unit,boolean small)
    {
        OBImage iconImage = null;
        String iconPath = unit.pathToIcon(small);

        if(iconPath != null)
        {
            OB_MutFloat fileScale = new OB_MutFloat(1.0f);
            Bitmap bitmap = OBImageManager.sharedImageManager().bitmapForPath(iconPath,fileScale);
            if(bitmap != null)
                iconImage = new OBImage(bitmap);
        }

        if(iconImage == null)
        {
            iconImage =loadImageWithName("icon_default",new PointF(0,0),this.boundsf());
        }

        iconImage.setProperty("unit",unit);
        iconImage.show();
        OBControl templateControl = objectDict.get(small ? "icon_small" : "icon_big");
        iconImage.setPosition(templateControl.position());
        OBMisc.scaleControlToControl(iconImage, templateControl, false);
        setObjectShadow(iconImage,templateControl);
        return iconImage;

    }

    public void setObjectShadow(OBControl obj,OBControl shadow)
    {
        obj.setShadow(shadow.getShadowRadius(), shadow.getShadowOpacity(), shadow.getShadowOffsetX(), shadow.getShadowOffsetY(), shadow.getShadowColour());
    }

    public void loadTopBar(boolean show,boolean fill)
    {
        OBGroup topBar = (OBGroup)objectDict.get("top_bar");
        int week = fatController.getCurrentWeek();
        if(currentDay%7 == 0)
            week++;
        for(int i=1; i<week; i++)
        {
            OBControl weekSquare = topBar.objectDict.get(String.format("week_%d",i));
            if(weekSquare != null)
            {
                int fillCol = coloursDict.get(String.format("week_%d",(i%4)+1));
                weekSquare.setProperty("fill",fillCol);
                if(fill)
                    weekSquare.setFillColor(fillCol);
            }
        }
        topBar.setNeedsRetexture();
        if(show)
            topBar.show();
    }

    public void loadEmptyStarBar(boolean show)
    {
        for(int i=0; i<OCM_FatController.SESSION_UNIT_COUNT; i++)
        {
            colourStarNum(i+1,-1);

        }
        if(show)
            showControls("top_bar_star_.*");
    }

    public void loadStarBar()
    {
        List<Integer> starList = fatController.getStarsForSession();
        for(int i=0; i<OCM_FatController.SESSION_UNIT_COUNT; i++)
        {
            colourStarNum(i+1,starList.size() > i ? starList.get(i) :-1);
        }
        if(starList.size() == 0)
            hideControls("top_bar_star_.*");
        else
            showControls("top_bar_star_.*");
    }

    public void hideStarBar()
    {
        hideControls("top_bar_star_.*");
    }

    public void colourStarNum(int starNum,int colourNum)
    {
        OBGroup star = (OBGroup)objectDict.get(String.format("top_bar_star_%d",starNum));
        if(star == null)        return;
        colourStar(star,colourNum);
    }

    public void colourStar(OBGroup star,int colourNum)
    {
        OBPath path = (OBPath)star.objectDict.get("star");
        if(colourNum > 0)
        {
            int starColour = coloursDict.get(String.format("star_%d",colourNum));
            path.setFillColor(starColour);
            path.setStrokeColor(Color.TRANSPARENT);
        }
        else
        {
            path.setFillColor(Color.WHITE);
            path.setStrokeColor(coloursDict.get("star_border"));
        }
        star.setProperty("star_colour",colourNum);
    }

    public OBLabel loadDayOfTheWeekLabel(int dayNum)
    {
        OBControl box = objectDict.get("text_box");
        String dayName = this.localisations.get(String.format("day_%d",dayNum));
        OBLabel label = new OBLabel(dayName,OBUtils.standardTypeFace(),box.height()/82.0f*80);
        label.setColour(Color.BLACK);
        label.setPosition(box.position());
        label.hide();
        attachControl(label);
        return label;

    }
    public void loadPlayZoneBox(boolean show)
    {
        boolean active = fatController.playZoneActive();
        OBGroup box = (OBGroup)objectDict.get("box");
        box.setProperty("touched",false);
        box.setOpacity(active ? 1 : 0.5f);
        if(!active)
            box.disable();
        for(OBControl gemControl : box.filterMembers("gem_.*"))
        {
            OBGroup gemGroup = (OBGroup)gemControl;
            for(OBControl cont : gemGroup.filterMembers("glow.*"))
            {
                cont.setOpacity(0);
            }
        }
        if(show)
            box.show();
    }

    public void enablePlayZoneBox()
    {
        lockScreen();
        OBGroup box = (OBGroup)objectDict.get("box");
        box.enable();
        box.setOpacity(1);
        unlockScreen();
    }

    public boolean checkPlayZoneBoxStatus()
    {
        OBGroup box = (OBGroup)objectDict.get("box");
        if(!box.isEnabled())
        {
            if(fatController.playZoneActive())
            {
                enablePlayZoneBox();
                return true;
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    /*
    Animation functions
     */

    public void animateNextGridIconShake(final long time) throws Exception
    {
        OBControl iconSearch = null;
        for(OBControl con : communityModeIcons)
        {
            if(con.propertyValue("completed") == null || !(boolean)con.propertyValue("completed"))
            {
                iconSearch = con;
                break;
            }
        }
        final OBControl nextIcon = iconSearch;
        if(nextIcon != null)
        {
            final OBSectionController sectionController = this;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    waitForSecs(0.5f);
                    float animAngle = 7;
                    while (!statusChanged(time)
                            && status() == STATUS_AWAITING_CLICK)
                    {
                        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float) Math.toRadians(animAngle), nextIcon)),
                                0.05, true, OBAnim.ANIM_EASE_OUT, sectionController);
                        if (statusChanged(time))
                            break;
                        for (int i = 0; i < 3; i++)
                        {
                            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float) Math.toRadians(-animAngle), nextIcon)),
                                    0.1, true, OBAnim.ANIM_EASE_IN_EASE_OUT, sectionController);
                            if (statusChanged(time)) break;
                            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float) Math.toRadians(animAngle), nextIcon)),
                                    0.1, true, OBAnim.ANIM_EASE_IN_EASE_OUT, sectionController);
                            if (statusChanged(time)) break;
                        }
                        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim(0, nextIcon)), 0.05, true, OBAnim.ANIM_EASE_OUT, sectionController);
                        if (statusChanged(time)) break;
                        waitForSecs(1f);
                    }
                    nextIcon.setRotation(0);
                }
            });
        }
        else if(fatController.playZoneActive())
        {
            shakePlayZoneBoxOnce(true);
        }

    }

    public void animateAwardStar(int starNum,int colourNum) throws Exception
    {
        OBGroup target = (OBGroup)objectDict.get(String.format("top_bar_star_%d",starNum));
        if(target.hidden)
            showControls("top_bar_star_.*");
        OBGroup star = (OBGroup)target.copy();
        star.setShadowOpacity(0);
        star.setZPosition(50);
        star.setScale(star.scale() * 5);
        colourStar(star,colourNum);
        star.setPosition(OB_Maths.locationForRect(1.1f,0.5f,this.bounds()));
        attachControl(star);
        OBControl iconRect = objectDict.get("icon_big");
        float extend = iconRect.width() * 0.1f;
        Path path = new Path();
        path.moveTo(star.position().x, star.position().y);
        OBMisc.pathAddCurveToPoint(path,new PointF(iconRect.position().x, iconRect.bottom() + extend),
                new PointF(star.position().x - extend, star.position().y),
                new PointF(iconRect.position().x + extend*5, iconRect.bottom() + extend));

        OBMisc.pathAddCurveToPoint(path,new PointF(iconRect.left() - extend, iconRect.position().y),
                new PointF(iconRect.position().x - extend*5, iconRect.bottom() + extend),
                new PointF(iconRect.left() - extend, iconRect.position().y + extend*5));

        OBMisc.pathAddCurveToPoint(path,new PointF(iconRect.right() + extend, iconRect.position().y),
                new PointF(iconRect.left() - extend, iconRect.position().y - extend*7),
                new PointF(iconRect.right() + extend, iconRect.position().y - extend*7));

        OBMisc.pathAddCurveToPoint(path,new PointF(iconRect.left() - extend, iconRect.position().y),
                new PointF(iconRect.right() + extend,  iconRect.position().y + extend*6),
                new PointF(iconRect.left() - extend,  iconRect.position().y + extend*6));

        float alignLeft = extend * (starNum < 5 ? (6- starNum)*0.75f : 1);
        OBMisc.pathAddCurveToPoint(path,target.position(),
                new PointF(iconRect.left() - alignLeft, iconRect.position().y - extend*6),
                new PointF(target.position().x,  target.position().y + extend*4));
        playAudio(getAudioForScene("sfx","onestar").get(0));

        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(star,path,false,0),
                OBAnim.rotationAnim((float)Math.toRadians(1440),star),
                OBAnim.scaleAnim(target.scale(),star))
                ,1.5,true,OBAnim.ANIM_EASE_OUT,this);
        playSfxAudio("starsettle",false);
        lockScreen();
        colourStar(target,colourNum);
        unlockScreen();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,star), OBAnim.scaleAnim(star.scale()*1.5f,star))
                ,0.4,true,OBAnim.ANIM_LINEAR,this);
        detachControl(star);
        waitSFX();
    }

    public void animateAwardStar2(OBGroup button,int colourNum) throws Exception
    {
        OBGroup target = (OBGroup)button.objectDict.get("star");
        button.setProperty("completed",true);
        PointF targetLoc = target.getWorldPosition();
        OBGroup star = (OBGroup)target.copy();
        star.setShadowOpacity(0);
        star.setZPosition(50);
        star.setRotation(0);
        star.setScale(star.scale() * 5);
        star.setPosition(targetLoc);
        star.setLeft(this.bounds().width());
        star.setShadowOpacity(0);
        colourStar(star,colourNum);
        attachControl(star);
        playAudio(getAudioForScene("sfx","onestar2").get(0));
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(targetLoc,star),
                OBAnim.rotationAnim((float)Math.toRadians(720)+target.rotation,star),
                OBAnim.scaleAnim(objectDict.get("icon_small_star").scale(),star))
                ,1,true,OBAnim.ANIM_EASE_OUT,this);
        playSfxAudio("starsettle",false);
        lockScreen();
        colourStar(target,colourNum);
        unlockScreen();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,star), OBAnim.scaleAnim(star.scale()*1.5f,star))
                ,0.4,true,OBAnim.ANIM_LINEAR,this);
        detachControl(star);
        waitSFX();
    }

    public void animateBigIconSlide() throws Exception
    {
        if(currentBigIcon != null)
        {
            playSfxAudio("bigbutton", false);
            List<OBAnim> anims = new ArrayList<>();
            anims.add(OBAnim.propertyAnim("left", currentBigIcon.left() - this.bounds().width(), currentBigIcon));

            if (previousBigIcon != null)
                anims.add(OBAnim.propertyAnim("left", previousBigIcon.left() - this.bounds().width(), previousBigIcon));
            OBAnimationGroup.runAnims(anims, 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            if (previousBigIcon != null)
                detachControl(previousBigIcon);
        }
    }

    public void animateLogoOn() throws Exception
    {
        playSFX("ting");
        OBControl button = objectDict.get("button_start");
        float startScale = button.scale();
        button.setScale( button.scale() * 1.2f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(startScale,button),
                OBAnim.opacityAnim(1,button)),0.5,true,OBAnim.ANIM_EASE_OUT,this);
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

    public void shakePlayZoneBoxOnce(boolean delayed) throws Exception
    {
        OBGroup box = (OBGroup)objectDict.get("box");
        final OBControl glow = box.objectDict.get("lid_glow");
        glow.setOpacity(0);
        glow.show();
        List<List<OBAnim>> gemAnimsOn = new ArrayList<>();
        List<List<OBAnim>> gemAnimsOff = new ArrayList<>();
        for(int i = 1; i<6; i++)
        {
            OBGroup gem = (OBGroup)box.objectDict.get(String.format("gem_%d",i));
            List<OBAnim> onAnim = new ArrayList<>();
            List<OBAnim> offAnim = new ArrayList<>();
            for(OBControl cont : gem.filterMembers("glow.*"))
            {
                onAnim.add(OBAnim.opacityAnim(1,cont));
                offAnim.add(OBAnim.opacityAnim(0,cont));
            }
            gemAnimsOn.add(onAnim);
            gemAnimsOff.add(offAnim);
        }
        final OBControl lidOverlay = box.objectDict.get("lid_glow_overlay");
        final float startHeight = box.objectDict.get("lid_0").height();
        final float endHeight = lidOverlay.height();
        final float heightDif = lidOverlay.height() - startHeight;
        final float lidTop = lidOverlay.top();
        lidOverlay.setHeight(startHeight);
        lidOverlay.setTop(lidTop);

        lockScreen();
        box.objectDict.get("lid_glow_group").show();
        box.objectDict.get("lid_0").hide();
        unlockScreen();

        OBAnim lidOpenAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                lidOverlay.setHeight (startHeight + frac * heightDif);
                lidOverlay.setTop(lidTop);
                glow.setOpacity(frac);
            }
        };

        OBAnim lidCloseAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                lidOverlay.setHeight(endHeight - frac * heightDif);
                lidOverlay.setTop(lidTop);
                glow.setOpacity(1-frac);
            }
        };
        box.setAnchorPoint(OB_Maths.relativePointInRectForLocation(box.objectDict.get("mask_frame").getWorldPosition(), box.getWorldFrame()));
        OBAnim boxShakeLeft = OBAnim.rotationAnim((float)Math.toRadians(-5),box);
        OBAnim boxShakeRight = OBAnim.rotationAnim((float)Math.toRadians(5),box);
        OBAnim boxShakeReset = OBAnim.rotationAnim((float)Math.toRadians(0),box);
        List<List<OBAnim>> fullAnims = new ArrayList<>();
        List<Float> animDurations = new ArrayList<>();
        List<Integer> animEasings = new ArrayList<>();
        int animSize = 8;
        for(int i=0; i<animSize; i++)
        {
            List<OBAnim> anims = new ArrayList<>();
            if(i == animSize-1)
            {
                anims.add(boxShakeReset);
            }
            else
            {
                anims.add(i%2 == 0 ? boxShakeLeft : boxShakeRight);

            }
            anims.add(i%2 == 0 ? lidOpenAnim : lidCloseAnim);
            if(i > 0 && gemAnimsOn.size() > i-1)
                anims.addAll(gemAnimsOn.get(i-1));
            if(i > 1 && gemAnimsOff.size() > i-2)
                anims.addAll(gemAnimsOff.get(i-2));
            fullAnims.add(anims);
            animDurations.add((i==0 || i == animSize-1) ? 0.05f : 0.1f);
            animEasings.add(OBAnim.ANIM_EASE_IN_EASE_OUT);

        }
        if(delayed)
            waitForSecs(0.5f);

        registerAnimationGroup(OBAnimationGroup.chainAnimations(fullAnims, animDurations, false, animEasings, 1,this), "glowAnim1");
    }


    public void stopBoxGlowAnimate()
    {
        deregisterAnimationGroupWithName("glowAnim.*");
        OBGroup box = (OBGroup)objectDict.get("box");
        lockScreen();
        OBControl lidOverlay = box.objectDict.get("lid_glow_overlay");
        OBControl lid0 = box.objectDict.get("lid_0");
        lidOverlay.setHeight ( (float)lidOverlay.propertyValue("start_height") );
        lidOverlay.setTop ( (float)lidOverlay.propertyValue("start_top") );
        lid0.show();
        box.setRotation ( 0);
        box.objectDict.get("lid_glow_group").hide();
        for(OBControl gemControl : box.filterMembers("gem_.*"))
        {
            OBGroup gemGroup = (OBGroup)gemControl;
            for(OBControl cont : gemGroup.filterMembers("glow.*"))
            {
                cont.setOpacity(0);
            }
        }
        unlockScreen();
    }

    public void animateStarBar() throws Exception
    {
        int starCount = 15;
        float singleAnimTime = 0.5f;
        float animDelay = 0.1f;
        float animTime = singleAnimTime + starCount * animDelay;
        final float animDelayFrac = animDelay / animTime;
        final float singleAnimFrac = singleAnimTime / animTime;
        for(OBControl con : filterControls("top_bar_star_.*"))
            con.setProperty("start_scale",con.scale());
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(int i=0; i<15; i++)
                {
                    float partFrac = OB_Maths.clamp01((frac - animDelayFrac *i)/singleAnimFrac);
                    OBControl con = objectDict.get(String.format("top_bar_star_%d",i+1));
                    float startScale = (float)con.propertyValue("start_scale") ;
                    con.setScale(startScale + 0.5f*startScale*OB_Maths.bezef((float)OB_Maths.clamp01((1.0-Math.abs((partFrac * 2) - 1)))));
                }
            }
        };

        playSfxAudio("starflash",false);
        OBAnimationGroup.runAnims(Arrays.asList(blockAnim),animTime
                ,true,OBAnim.ANIM_LINEAR,this);
        waitSFX();
    }

    public void animateCommunityGrid() throws Exception
    {
        final int moveDist = this.bounds().width();
        for(int i=0; i<3; i++)
        {
            final List<OBGroup> objs = communityModeIcons.subList(i*5, (i*5) + 5);
            final List<Float> startLefts = new ArrayList<>();
            for(int j=0; j < objs.size(); j++)
            {
                OBControl obj = objs.get(j);
                startLefts.add(obj.left());
                obj.setLeft(obj.left() + moveDist);
                obj.show();
            }

            OBAnim blockAnim = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    for(int j=0; j < objs.size(); j++)
                    {
                        OBControl obj = objs.get(j);
                        float startLeft = (float)startLefts.get(j) ;
                        float partFrac = OB_Maths.bezef((float)OB_Maths.clamp01((frac - ((0.5/objs.size())*j))/0.5));
                        obj.setLeft(startLeft + moveDist * (1-partFrac));
                    }
                }
            };

            playSfxAudio("rowin",false);
            OBAnimationGroup.runAnims(Arrays.asList(blockAnim),1,true,OBAnim.ANIM_LINEAR,this);

        }
        //simple for now
        final List<OBAnim> moveAnims = new ArrayList<>();
        final List<OBAnim> rotateAnims = new ArrayList<>();
        for(int i=0; i<communityModeIcons.size(); i++)
        {
            OBControl star = objectDict.get(String.format("top_bar_star_%d",i+1));
            OBGroup targetButton= communityModeIcons.get(i);
            OBControl targetStar = targetButton.objectDict.get("star");

            OBAnim moveAnim = OBAnim.moveAnim(targetStar.getWorldPosition(),star);
            moveAnim.retrieveInitialValue();
            moveAnims.add(moveAnim);

            OBAnim rotateAnim = OBAnim.rotationAnim((float)Math.toRadians(720)+targetStar.rotation,star);
            rotateAnim.retrieveInitialValue();
            rotateAnims.add(rotateAnim);
        }

        for(int i=0; i<3; i++)
        {
            final int currentInt = i;
            OBAnim anim = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    for(int j=0; j<5; j++)
                    {
                        int currentNum = (currentInt*5)+j;
                        OBControl star = objectDict.get(String.format("top_bar_star_%d",currentNum+1));
                        if(star != null)
                        {
                            float partFrac = OB_Maths.bezef((float)OB_Maths.clamp01((frac - ((0.5/4)*j))/0.5));

                            star.setPosition((PointF) moveAnims.get(currentNum).valueForT(partFrac));
                            star.setRotation((float) rotateAnims.get(currentNum).valueForT(partFrac));
                        }
                    }
                }
            };

            playSFX("cf_belltree");
            OBAnimationGroup.runAnims(Arrays.asList(anim),1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
        waitSFX();
        waitForSecs(0.5f);
        lockScreen();
        hideControls("top_bar_star_.*");
        for(OBGroup iconGroup : communityModeIcons)
            iconGroup.objectDict.get("star").show();
        playSfxAudio("starblank",false);
        unlockScreen();
        waitSFX();
    }


    public void animateTopBarFill() throws Exception
    {
        OBGroup topBar = (OBGroup)objectDict.get("top_bar");
        final List<OBControl> animSquares = new ArrayList<>();
        for(int i=1; i<=68; i++)
        {
            OBControl square = topBar.objectDict.get(String.format("week_%d",i));
            if(square.propertyValue("fill") != null)
            {
                int col = (int)square.propertyValue("fill");
                OBControl squareCopy = new OBControl();
                squareCopy.setFrame(square.getWorldFrame());
                squareCopy.setScale(2);
                squareCopy.setOpacity(0);
                squareCopy.setBackgroundColor(col);
                squareCopy.setZPosition(topBar.zPosition() + 1);
                attachControl(squareCopy);
                squareCopy.setProperty("square",square);
                animSquares.add(squareCopy);
            }
        }
        float singleAnimTime = 0.5f;
        float animDelay = 0.1f;
        float animTime = singleAnimTime + animSquares.size() * animDelay;
        final float animDelayFrac = animDelay / animTime;
        final float singleAnimFrac = singleAnimTime/animTime;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(int i=0; i<animSquares.size(); i++)
                {
                    float partFrac = OB_Maths.easeout(OB_Maths.clamp01((frac - animDelayFrac *i)/singleAnimFrac));
                    OBControl con = animSquares.get(i);
                    con.setOpacity ( partFrac);
                    con.setScale ( 2 - partFrac);

                }
            }
        };

        if(animSquares.size() == 1)
        {
            playSfxAudio("progress_note_1",false);
        }
        else
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    playSfxAudio("progress",false);
                    waitForSecs(0.1 * animSquares.size());
                    playSFX(null);

                }
            }) ;
            OBAnimationGroup.runAnims(Arrays.asList(blockAnim),animTime
                    ,true,OBAnim.ANIM_LINEAR,this);
            lockScreen();
            for(OBControl con : animSquares)
            {
                OBControl square = (OBControl)con.propertyValue("square");
                square.setFillColor(con.backgroundColor);
                detachControl(con);

            }
            topBar.setNeedsRetexture();
            unlockScreen();
        }
    }

    /*
    Demos
     */

    public void demo_grid() throws Exception
    {
        if(currentBigIcon != null)
        {
            playSfxAudio("bigbutton",false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",currentBigIcon.left() - bounds().width(),currentBigIcon))
                    ,0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            detachControl(currentBigIcon);
            currentBigIcon = null;
            waitSFX();
        }
        animateStarBar();
        animateCommunityGrid();
        waitForSecs(0.3f);
        playSfxAudio("prog_sq_on",false);
        objectDict.get("top_bar").show();
        waitSFX();
        walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"));
        waitForSecs(0.3f);
        if(currentDay%7 == 0)
        {
            int week = currentDay/7;
            if(week <= 68)
            {
                List<String> weekAudio1 = null;
                List<String> weekAudio2 = null;
                if(week <3 || week == 35 || week == 68)
                {
                    weekAudio1 = getAudioForScene(String.format("week_%d",week),"DEMO");
                    weekAudio2 =  getAudioForScene(String.format("week_%d",week),"DEMO2");
                }
                else
                {
                    String scene = null;
                    if(week < 35)
                        scene = "week_default";
                    else if(week < 64)
                        scene = "week_default2";
                    else
                        scene = "week_default3";
                    weekAudio1 = getAudioForScene(scene,"DEMO");
                    List<String> audioArr = getAudioForScene(scene,"DEMO2");
                    weekAudio2 = Arrays.asList(audioArr.get(currentDay%audioArr.size()));
                }
                waitForSecs(0.3f);
                presenter.moveHandfromIndex(0,6,0.5);
                waitForSecs(0.3f);
                presenter.speak((List<Object>)(Object)weekAudio1,this);
                waitForSecs(0.3f);
                animateTopBarFill();
                waitForSecs(0.3f);
                presenter.moveHandfromIndex(6,0,0.5);
                waitForSecs(0.3f);
                presenter.speak((List<Object>)(Object)weekAudio2,this);
                waitForSecs(1f);
            }
        }
        presenter.moveHandfromIndex(0,4,0.4);
        presenter.speak((List<Object>)(Object)getAudioForScene("grid","DEMO"),this);
        waitForSecs(0.3f);
        OBControl box = objectDict.get("box");
        float startScale = box.scale();
        float startOpacity = box.opacity();
        box.setScale(box.scale() * 2);
        box.setOpacity(0);
        box.show();
        playSfxAudio("pzicon",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(startScale,box),
                OBAnim.opacityAnim(startOpacity,box))
                ,0.5,true,OBAnim.ANIM_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.3f);
        presenter.moveHandfromIndex(4,2,0.2);
        presenter.speak((List<Object>)(Object)getAudioForScene("grid","DEMO2"),this);
        waitForSecs(0.3f);
        if(!fatController.playZoneActive())
        {
            presenter.speak((List<Object>)(Object)getAudioForScene("grid","DEMO3"),this);
        }
        presenter.moveHandfromIndex(2,0,0.3);
        waitForSecs(0.3f);
        long time = setStatus(STATUS_AWAITING_CLICK);
        checkPlayZoneBoxStatus();
        walkPresenterOut();
        animateNextGridIconShake(time);
    }

    public void demo_study_star_award() throws Exception
    {
        String eventName = String.format("unit_%d", lastUnitOrder);
        if(!audioAvailable(eventName))
        {
            eventName = "unit_default";

        }
        boolean playDemo = getAudioForScene(eventName,"FINAL") != null;
        animateAwardStar(lastUnitOrder,lastUnitInstance.starColour);
        if(playDemo)
        {
            walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"));
            presenter.speak((List<Object>)(Object)getAudioForScene(eventName,"FINAL"),this);
        }
    }

    public void demo_community_star_award() throws Exception
    {
        OBGroup awardIcon = null;
        for(OBGroup con : communityModeIcons)
        {
            if(con.propertyValue("unit") == lastUnitInstance.mlUnit)
            {
                awardIcon = con;
                break;
            }

        }
        if(awardIcon != null)
            animateAwardStar2(awardIcon,lastUnitInstance.starColour);

    }

    public void demo_button() throws Exception
    {
        waitForSecs(0.5f);
        animateLogoOn();
        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,objectDict.get("button_start").frame()),0.3f,true);
        playAudio(getAudioForScene("button","DEMO").get(0));
        long time = setStatus(STATUS_AWAITING_CLICK);
        if(!statusChanged(time))
            waitAudio();
        thePointer.hide();
        if(!statusChanged(time))
            waitForSecs(4f);
        if(!statusChanged(time))
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene("button","REMIND"),300),true);

    }

    public void demo_start_unit_1() throws Exception
    {
        PointF loc = new PointF(0.5f*this.bounds().width(), presenter.control.position().y);
        playSfxAudio("musical_sting",false);
        walkPresenterIn(loc);
        waitSFX();
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getAudioForScene("unit_1","DEMO"),this);
        waitForSecs(0.5f);
        presenter.walk((PointF)presenter.control.propertyValue("start_loc"));
        presenter.faceFront();
        waitForSecs(0.3f);

        lockScreen();
        showControls("top_bar_star_.*");
        playSfxAudio("allstar",false);
        unlockScreen();

        waitSFX();
        waitForSecs(0.3f);
        animateBigIconSlide();
        presenter.moveHandfromIndex(0,3,0.2);
        waitForSecs(0.3f);
        long time = setStatus(STATUS_AWAITING_CLICK);
        presenter.speak((List<Object>)(Object)getAudioForScene("unit_1","DEMO2"),this);
        if(!statusChanged(time))
            waitForSecs(0.3f);
        if(!statusChanged(time))
            presenter.moveHandfromIndex(3,0,0.2);
        if(!statusChanged(time))
            walkPresenterOut();
        if(!statusChanged(time))
            playUnitButtonAudio("unit_1",time);

    }

    public void demo_start_unit_2() throws Exception
    {
        walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"));
        animateBigIconSlide();
        presenter.moveHandfromIndex(0,3,0.2f);
        waitForSecs(0.3f);
        long time = setStatus(STATUS_AWAITING_CLICK);
        if(!statusChanged(time))
            presenter.speak((List<Object>)(Object)getAudioForScene("unit_2","DEMO"),this);
        if(!statusChanged(time))
            waitForSecs(0.3f);
        if(!statusChanged(time))
            presenter.moveHandfromIndex(3,0,0.2f);
        if(!statusChanged(time))
            walkPresenterOut();
        if(!statusChanged(time))
            playUnitButtonAudio("unit_2",time);

    }

    public void demo_start_unit_3() throws Exception
    {
        animateBigIconSlide();
        walkPresenterIn:presenter.control.propertyValue("start_loc");
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getAudioForScene("unit_3","DEMO"),this);
        presenter.moveHandfromIndex(0,3,0.2f);
        waitForSecs(0.3f);
        long time = setStatus(STATUS_AWAITING_CLICK);
        if(!statusChanged(time))
            presenter.speak((List<Object>)(Object)getAudioForScene("unit_3","DEMO2"),this);
        if(!statusChanged(time))
            waitForSecs(0.3f);
        if(!statusChanged(time))
            presenter.moveHandfromIndex(3,0,0.2f);
        if(!statusChanged(time))
            walkPresenterOut();
        if(!statusChanged(time))
            playUnitButtonAudio("unit_3",time);

    }

    public void demo_start_unit_4() throws Exception
    {
        animateBigIconSlide();
        playUnitButtonAudio("unit_4");
    }

    public void demo_default_unit_1() throws Exception
    {
        PointF loc = new PointF(0.5f*this.bounds().width(), presenter.control.position().y);
        playSfxAudio("musical_sting",false);
        walkPresenterIn(loc);
        waitSFX();
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getAudioForScene("unit_1","DEMO"),this);
        waitForSecs(0.3f);
        OBLabel dayLabel = null;
        if(!fatController.currentTimeIsDirty())
        {
            int dayNum = fatController.getCurrentDayOfWeek();
            dayLabel = loadDayOfTheWeekLabel(dayNum);
            playSfxAudio("dayon", false);
            dayLabel.show();
            waitSFX();
            waitForSecs(0.3f);
            presenter.speak((List<Object>) (Object) Arrays.asList(getAudioForScene("unit_1", "DEMO2").get(dayNum - 1)), this);

            waitForSecs(0.3f);
        }
        List<String> audios =  getAudioForScene("unit_1","DEMO3");
        presenter.speak((List<Object>)(Object)Arrays.asList(audios.get(OB_Maths.randomInt(0,audios.size()-1))),this);
        waitForSecs(0.3f);
        presenter.walk((PointF)presenter.control.propertyValue("start_loc"));
        presenter.faceFront();
        waitForSecs(0.3f);
        playSfxAudio("dayoff",false);
        if(dayLabel != null)
            detachControl(dayLabel);
        waitSFX();
        waitForSecs(0.3f);
        lockScreen();
        showControls("top_bar_star_.*");
        playSfxAudio("allstar",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        animateBigIconSlide();
        presenter.moveHandfromIndex(0,3,0.2f);
        waitForSecs(0.3f);
        long time = setStatus(STATUS_AWAITING_CLICK);
        if(!statusChanged(time))
            presenter.speak((List<Object>)(Object)getAudioForScene("unit_1","DEMO4"),this);
        if(!statusChanged(time))
            waitForSecs(0.3f);
        if(!statusChanged(time))
            presenter.moveHandfromIndex(3,0,0.2f);
        if(!statusChanged(time))
            walkPresenterOut();
        if(!statusChanged(time))
            playUnitButtonAudio("unit_1",time);

    }


    public void demo_playzone() throws Exception
    {
        OBGroup box = (OBGroup)objectDict.get("box");
        OBControl bg = objectDict.get("overlay_box");
        bg.setOpacity ( 0);
        bg.show();
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl gemControl : box.filterMembers("gem_.*"))
        {
            OBGroup gemGroup = (OBGroup)gemControl;
            for(OBControl cont : gemGroup.filterMembers("glow.*"))
            {
                anims.add(OBAnim.opacityAnim(1,cont));
            }
        }
        anims.add(OBAnim.opacityAnim(1,bg));
        OBAnimationGroup.runAnims(anims
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        fatController.startPlayZone(true, box.propertyValue("touched") != null ? !(boolean)box.propertyValue("touched") : false);
        box.setProperty("touched",true);
    }

    public void demo_playzone_back(long time) throws Exception
    {
        List<String> audio = getAudioForScene("grid","PROMPT2");
        if(!statusChanged(time))
        {
            playAudio(audio.get(0));
            waitAudio();
        }

        if(!statusChanged(time))
        {
            playAudio(audio.get(1));
            waitAudio();
        }

        if(!statusChanged(time))
        {
            playAudio(audio.get(2));
            shakePlayZoneBoxOnce(false);
            waitAudio();
        }

        if(!statusChanged(time))
            animateNextGridIconShake(time);

    }


    public void checkSecretTouchBox(PointF pt)
    {
        if(secretBoxUnlockList.size() > secretBoxTouchIndex)
        {

            if(secretBoxUnlockList.get(secretBoxTouchIndex).contains(pt.x,pt.y))
            {
                setStatus(STATUS_BUSY);
                secretBoxTouchIndex++;
                if(secretBoxTouchIndex >= secretBoxUnlockList.size())
                {
                    showPasswordCheckDialog();
                    secretBoxTouchIndex = 0;
                }
                long time = setStatus(STATUS_AWAITING_CLICK);
                delayedSecretIndexReset(time);

            }
            else if(secretBoxUnlockList.get(0).contains(pt.x,pt.y))
            {
                setStatus(STATUS_BUSY);
                secretBoxTouchIndex = 1;
                long time = setStatus(STATUS_AWAITING_CLICK);
                delayedSecretIndexReset(time);
            }
            else
            {
                secretBoxTouchIndex = 0;
            }

        }
    }


    public void showPasswordCheckDialog()
    {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.mainActivity);
        final EditText input = new EditText(MainActivity.mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alert.setView(input);
        alert.setTitle("Password Required");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String pass = (input.getText()).toString();
                checkPasswordAndProceed(pass);
            }
        });
        final AlertDialog dialog = alert.show();
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String pass = (input.getText()).toString();
                    checkPasswordAndProceed(pass);
                    if(dialog != null && dialog.isShowing())
                    {
                        dialog.cancel();
                    }

                }
                return false;
            }
        });

        OBUtils.runOnOtherThreadDelayed(15, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(dialog != null && dialog.isShowing())
                {
                    dialog.cancel();
                }
            }
        });

    }

    public void checkPasswordAndProceed(String pass)
    {
        if(pass.equals(SETUP_MENU_PASSWORD))
        {
            final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.mainActivity);
            final EditText input = new EditText(MainActivity.mainActivity);
            input.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
            alert.setTitle("Data Deletion Warning");
            alert.setMessage("This action will RESET progress on this device and resturn to setup menu. Do you wish to continue?");
            alert.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    fatController.deleteDBProgressAndReboot();
                }
            });

            alert.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });

            final AlertDialog dialog = alert.show();
            OBUtils.runOnOtherThreadDelayed(10, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    if(dialog != null && dialog.isShowing())
                    {
                        dialog.cancel();
                    }
                }
            });
        }
    }


}
