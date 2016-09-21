package org.onebillion.xprz.mainui.x_miniapp6;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.XPRZ_Menu;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.DBSQL;
import org.onebillion.xprz.utils.MlUnit;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBSystemsManager;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.XPRZ_FatController;
import org.onebillion.xprz.utils.XPRZ_FatReceiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/09/16.
 */
public class X_miniapp6_menu extends XPRZ_Menu implements XPRZ_FatReceiver
{
    private enum TouchTargets
    {
        TARGET_BUTTON,
        TARGET_STAR,
        TARGET_UNIT
    };

    private int lastCommand;
    private OBImage bigIcon;
    private XPRZ_FatController fatController;
    private MlUnit lastUnit;
    private String currentSection;
    private OBLabel currentLevelLabel;
    private XPRZ_Presenter presenter;
    private TouchTargets currentTarget;
    private Map<String, Integer> coloursDict;
    private OBControl screenOverlay;
    private int skippedStartAudioIndex;

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
    public void receiveCommand(Map<String, Object> params)
    {
        if (params != null && params.get("code") != null)
        {
            lastCommand = (int) params.get("code");
            lastUnit = (MlUnit) params.get("unit");
        } else
        {
            lastCommand = -1;

        }
        prepareSectionForLastUnit(lastUnit, lastCommand);
        if (bigIcon != null)
        {
            if (lastCommand != -1 && lastCommand != XPRZ_FatController.OFC_TIMED_OUT)
            {
                detachControl(bigIcon);
                bigIcon = null;
            }
        }
    }

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();

        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(20);
        currentLevelLabel = new OBLabel("8888888", font, fontSize);
        //currentLevelLabel.hide();
        currentLevelLabel.setString("1");
        currentLevelLabel.setPosition(OB_Maths.locationForRect(0.97f, 0.8f, this.bounds()));
        currentLevelLabel.setRight(this.bounds().width() - applyGraphicScale(10));
        currentLevelLabel.setColour(Color.BLACK);
        attachControl(currentLevelLabel);

        coloursDict = OBMisc.loadEventColours(this);

        fatController = (XPRZ_FatController) MainActivity.mainActivity.fatController;
        fatController.menu = this;

        presenter = XPRZ_Presenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("start_loc", XPRZ_Generic.copyPoint(presenter.control.position()));
        presenter.faceFront();
        presenter.control.setRight(0);
        presenter.control.setProperty("end_loc", XPRZ_Generic.copyPoint(presenter.control.position()));
        setObjectShadow(presenter.control, 1);

        receiveCommand(fatController.getLastCommand());

        loadChoiceStars();

        reloadEntireScreen();

        List<OBControl> stars = new ArrayList<>();
        stars.addAll(filterControls("top_bar_star_.*"));
        stars.addAll(filterControls("choice_star_.*"));
        for (OBControl star : stars)
            star.setProperty("start_loc", XPRZ_Generic.copyPoint(star.position()));

        skippedStartAudioIndex = -1;

        OBPath path = (OBPath)objectDict.get("button_start");
        path.sizeToBoundingBoxIncludingStroke();
       // prepareSectionForLastUnit(lastUnit, lastCommand);
    }


    public void reloadEntireScreen()
    {
        resetPresenter();
        int currentLevel = (lastUnit == null ? 1 : ((lastUnit.awardStar == 10) ? lastUnit.level+1 : lastUnit.level));
        loadTrophies(currentLevel-1);
        loadTopBar(currentLevel, lastUnit != null);
        hideControls("choice_star_.*");

        if(lastCommand == XPRZ_FatController.OFC_NEW_SESSION)
            showNewDayScreen();
    }

    @Override
    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {


                if (screenOverlay != null)
                    detachControl(screenOverlay);

                if (bigIcon == null && (prepareNextEvent() || (lastUnit.level == 10 && lastUnit.awardStar == 10)))
                {
                    refreshCurrentLabel();
                    lastCommand = -1;
                    performSel("demo", currentSection);
                } else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
        });
    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (currentTarget == TouchTargets.TARGET_STAR)
            {
                final OBControl star = finger(0, 1, filterControls("choice_star_.*"), pt);
                if (star != null)
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    star.highlight();
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkChoiceStar(star);
                        }
                    });
                }
            } else if (currentTarget == TouchTargets.TARGET_BUTTON)
            {
                final OBControl button = finger(0, 1, Collections.singletonList(objectDict.get("button_start")), pt);
                if (button != null)
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            button.highlight();
                            waitForSecs(0.5);
                            hideNewDayScreen();
                            button.lowlight();
                            currentTarget = TouchTargets.TARGET_UNIT;
                            performSel("demostart", currentSection);
                        }
                    });
                }
            } else if (bigIcon != null && bigIcon.frame.contains(pt.x, pt.y))
            {
                setStatus(STATUS_BUSY);
                bigIcon.highlight();
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkBigButton();
                    }
                });
            }
        }
    }

    @Override
    public void onResume()
    {
        if (screenOverlay != null)
            detachControl(screenOverlay);

        if (newDayCheck())
            return;

        if (status() == STATUS_AWAITING_CLICK)
        {
            setStatus(STATUS_AWAITING_CLICK);

            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    replayReminders();
                }
            });
        } else if (status() == STATUS_IDLE && currentSection.equalsIgnoreCase("timeout"))
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    demotimeout2();
                }
            });

        }
    }

    @Override
    public void onAlarmReceived(Intent intent)
    {
        newDayCheck();
    }


    private void replayReminders() throws Exception
    {
        waitForSecs(1);
        if (currentTarget == TouchTargets.TARGET_BUTTON)
        {
            playAudioQueued((List<Object>) (Object) getAudioForScene("button", "REMINDER"), true);
        } else if (currentTarget == TouchTargets.TARGET_STAR)
        {
            presenter.speak((List<Object>) (Object) getAudioForScene((lastUnit.level == 1 && lastUnit.awardStar == 1) ? "1" : "default", "REMINDER"), this);
        } else
        {
            playAudioQueued((List<Object>) (Object) getAudioForScene("unit", "REMINDER"), true);
        }
    }

    private void checkChoiceStar(OBControl star) throws Exception
    {
        animateChoiceStars(false, star);
        star.lowlight();
        fatController.saveStarForUnit(lastUnit, (String) star.propertyValue("star_colour"));
        animateStarFlyToBar(star, lastUnit.awardStar);
        currentTarget = TouchTargets.TARGET_UNIT;
        demostar2();
    }

    private void checkBigButton() throws Exception
    {
        playAudio(null);
        MlUnit unit = (MlUnit) bigIcon.settings.get("unit");
        startSectionForUnit(unit);
        bigIcon.lowlight();
    }

    public void resetBarStars()
    {
        for (OBControl star : filterControls("top_bar_star_.*"))
        {
            star.setPosition((PointF) star.propertyValue("start_loc"));
            star.hide();
            star.setOpacity(1);
        }
    }

    public boolean newDayCheck()
    {

        if(fatController.checkAndStartNewSession())
        {
            setStatus(STATUS_EXITING);
            killAnimations();
            playAudio(null);
            _aborting = true;
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    stopAllAudio();
                    MainViewController().pushViewController(X_miniapp6_menu.class,false,false,null,true);
                }
            });
            return true;

        }
        return false;

    }

    public void refreshCurrentLabel()
    {
        MlUnit unit = (MlUnit)bigIcon.propertyValue("unit");
        if (unit == null)
            return;

        String[] arr = unit.key.split("\\.");
        currentLevelLabel.setString(arr.length > 0 ? arr[0] : "");
    }

    public void showNewDayScreen()
    {
        objectDict.get("overlay").show();
        objectDict.get("button_start").setOpacity(0);
        objectDict.get("button_start").show();
    }

    public void hideNewDayScreen()
    {
        objectDict.get("overlay").hide();
        objectDict.get("button_start").hide();
        objectDict.get("onecourse_logo").hide();
    }

    public void prepareSectionForLastUnit(MlUnit unit, int command)
    {
        currentTarget = TouchTargets.TARGET_UNIT;
        if (command == XPRZ_FatController.OFC_NEW_SESSION)
        {
            currentTarget = TouchTargets.TARGET_BUTTON;
            if (unit != null)
            {
                currentSection = "day";
            } else
            {
                currentSection = "unit1";
            }
        } else if (unit.unitid == 0)
        {
            currentSection = "unit2";
        } else if ((command == XPRZ_FatController.OFC_SUCCEEDED || command == XPRZ_FatController.OFC_FINISHED_LOW_SCORE) && fatController.shouldAwardStar(unit))
        {
            currentTarget = TouchTargets.TARGET_STAR;
            currentSection = "star";

        } else if (command == XPRZ_FatController.OFC_SESSION_TIMED_OUT || fatController.currentSessionFinished())
        {
            currentSection = "timeout";
        } else
        {
            currentSection = "unitdefault";

        }
        loadAudioXML(getConfigPath(String.format("event%saudio.xml", currentSection)));
    }

    public void startSectionForUnit(MlUnit unit)
    {
        fatController.startSectionByUnit(unit);
    }

    public void playUnitButtonAudio(String extraAudio) throws Exception
    {
        List<String> aud = new ArrayList<>();
        if (extraAudio != null)
            aud.add(extraAudio);

        List<String> prompt = getAudioForScene("unit", "PROMPT");
        if (prompt != null)
            aud.addAll(prompt);

        List<String> remindAudio = getAudioForScene("unit", "REMINDER");
        long time = setStatus(STATUS_AWAITING_CLICK);
        playAudioQueued(OBUtils.insertAudioInterval(aud, 300), true);

        if (remindAudio != null)
            reprompt(time, OBUtils.insertAudioInterval(remindAudio, 300), 4);
    }

    public void loadBigIconForUnit(MlUnit unit)
    {
        lockScreen();
        String imgName = String.format("%s_big", unit.icon);

        if (OBImageManager.sharedImageManager().getImgPath(imgName) != null)
            bigIcon = loadImageWithName(imgName, new PointF(0, 0), new RectF(bounds()));
        else
            bigIcon = loadImageWithName("icon_default_big", new PointF(0, 0), new RectF(bounds()));

        bigIcon.setScale(applyGraphicScale(1));
        bigIcon.setRasterScale(applyGraphicScale(1));
        bigIcon.setProperty("unit", unit);
        bigIcon.setOpacity(0);
        bigIcon.setPosition(OB_Maths.locationForRect(0.5f, 0.5f, this.bounds()));
        bigIcon.show();
        bigIcon.setZPosition(20);
        setObjectShadow(bigIcon, 1.5f);
        attachControl(bigIcon);
        unlockScreen();
    }

    public boolean prepareNextEvent()
    {
        MlUnit unit = fatController.requestNextUnit();
        if (unit != null)
        {
            loadBigIconForUnit(unit);
            return true;
        }
        return false;
    }

    public void setObjectShadow(OBControl obj, float scale)
    {
        OBControl shadow = objectDict.get("shadow");
        obj.setShadow(shadow.getShadowRadius(), shadow.getShadowOpacity(), shadow.getShadowOffsetY() * scale, shadow.getShadowOffsetY() * scale, shadow.getShadowColour());
    }

    public void loadTrophies(int level)
    {
        for (int i = 1; i <= 10; i++)
        {
            OBGroup trophy = (OBGroup) objectDict.get(String.format("trophy_%d", i));
            trophy.setProperty("level", i);
            trophy.setProperty("start_loc", XPRZ_Generic.copyPoint(trophy.position()));
            trophy.show();
            if (i > level)
            {
                lowlightTrophy(trophy, true);
            }

        }
        PointF moveLoc = trophySlideLocForLevel(level);
        float moveDist = moveLoc.x - objectDict.get("trophy_1").position().x;
        for (int i = 1; i <= 10; i++)
        {
            OBGroup trophy = (OBGroup) objectDict.get(String.format("trophy_%d", i));
            trophy.setRight(trophy.right() + moveDist);
        }
    }

    public PointF trophySlideLocForLevel(int level)
    {
        if(level > 10)
            level = 10;
        if (level == 0)
        {
            OBControl con = objectDict.get("trophy_1");
            return new PointF(bounds().width() + 0.5f * con.width(), con.position().y);
        } else
        {
            OBControl con = objectDict.get(String.format("trophy_%d", 11 - level));
            PointF loc = (PointF) con.propertyValue("start_loc");
            return loc;
        }
    }

    private int getStartAudio()
    {
        MlUnit nextUnit = (MlUnit)bigIcon.propertyValue("unit");
        int audioIndex = nextUnit.startAudio;
        int unitCount = fatController.currentSessionUnitCount();
        if(unitCount < 2 && audioIndex > -1)
        {
            skippedStartAudioIndex = audioIndex;
            audioIndex = -1;
        }
        else if(skippedStartAudioIndex > -1 && unitCount >=2)
        {
            audioIndex = skippedStartAudioIndex;
            skippedStartAudioIndex = -1;
        }
        if(nextUnit.startAudio == -2)
            return -1;

        return audioIndex;
    }

    public void lowlightTrophy(OBGroup trophy, boolean on)
    {
        if (on)
        {
            trophy.setColourOverlay(coloursDict.get("lowlight"));
        } else
        {
            trophy.lowlight();
        }
    }

    public OBGroup loadBigTrophy(int level)
    {
        OBGroup trophy = (OBGroup) objectDict.get(String.format("trophy_%d", level));
        lockScreen();
        lowlightTrophy(trophy, false);
        OBGroup bigTrophy = (OBGroup) trophy.copy();
        bigTrophy.setRasterScale(2);
        bigTrophy.setProperty("trophy", trophy);
        lowlightTrophy(bigTrophy, false);
        OBControl icon = objectDict.get("icon");
        bigTrophy.setPosition(OB_Maths.locationForRect(0.5f, 0.5f, icon.frame()));
        bigTrophy.setScale(0.8f * (icon.height() * bigTrophy.scaleY / bigTrophy.height()));
        lowlightTrophy(trophy, true);
        bigTrophy.setZPosition(2);
        unlockScreen();
        bigTrophy.hide();
        attachControl(bigTrophy);
        setObjectShadow(bigTrophy, 0.5f);
        return bigTrophy;
    }

    public void loadTopBar(int level, boolean show)
    {
        if(level > 10)
            level = 10;

        Map<Integer, String> starsDict = fatController.starsForLevel(level);
        OBControl topBar = objectDict.get("top_bar");
        int barColour = coloursDict.get(String.format("bar_%d", level));
        topBar.setProperty("dest_colour", barColour);
        if (show)
            topBar.setFillColor(barColour);

        //int maxStars = MlUnit.awardNumForLevel(level,lastUnitID-1);
        for (int i = 1; i <= 10; i++)
        {
            OBGroup star = (OBGroup) objectDict.get(String.format("top_bar_star_%d", i));
            OBPath path = (OBPath) star.objectDict.get("star");
            String colour = starsDict.get(i);
            if (colour != null)
            {
                colourStar(star, colour);
            } else
            {
                path.setFillColor(Color.WHITE);
                path.setStrokeColor(OBUtils.highlightedColour(barColour));
            }
            if (show)
                star.show();
        }
    }

    public void colourStar(OBGroup star, String colourName)
    {
        int starColour = coloursDict.get(String.format("star_%s", colourName));
        OBPath path = (OBPath) star.objectDict.get("star");
        path.setFillColor(starColour);
        path.setStrokeColor(OBUtils.highlightedColour(starColour));
        star.setProperty("star_colour", colourName);
    }

    public void loadChoiceStars()
    {
        for (int i = 1; i <= 5; i++)
        {
            OBGroup con = (OBGroup) objectDict.get(String.format("choice_star_%d", i));
            colourStar(con, String.format("%d", i));
        }
    }

    public void prepareScreenLock() throws Exception
    {
        if (screenOverlay != null)
            detachControl(screenOverlay);
        screenOverlay = new OBControl();
        screenOverlay.setFrame(new RectF(bounds()));
        screenOverlay.setFillColor(Color.BLACK);
        screenOverlay.setZPosition(100);
        screenOverlay.setOpacity(0);
        attachControl(screenOverlay);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.opacityAnim(1, screenOverlay)), 2, true, OBAnim.ANIM_LINEAR, this);
        objectDict.get("overlay").show();
        OBSystemsManager.sharedManager.screenLock();

    }

    public void animateTopBar() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
        {
            final OBGroup star = (OBGroup) objectDict.get(String.format("top_bar_star_%d", 11 - i));
            final PointF destLoc = XPRZ_Generic.copyPoint(star.position());
            star.setLeft(star.left() - bounds().width());
            final PointF startLoc = XPRZ_Generic.copyPoint(star.position());
            star.show();
            final int index = i;
            anims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float frac2 = OB_Maths.bezef(frac / (0.5f + 0.05f * index));
                    star.setPosition(OB_Maths.tPointAlongLine(frac2, startLoc, destLoc));
                    star.setRotation((float) Math.toRadians(frac2 * 720));
                }
            });
        }
        OBControl topBar = objectDict.get("top_bar");
        int destColour = (int) topBar.propertyValue("dest_colour");
        playSfxAudio("topbaron", false);
        topBar.setFillColor(destColour);
        waitSFX();
        waitForSecs(0.5f);
        playSfxAudio("topbarstarslide", false);
        OBAnimationGroup.runAnims(anims, 1, true, OBAnim.ANIM_LINEAR, this);
    }

    public void animateStarsSpinWithTrophy(OBGroup trophy) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        List<OBAnim> anims2 = new ArrayList<>();
        OBControl icon = objectDict.get("icon");
        final PointF center = XPRZ_Generic.copyPoint(icon.position());
        final float destRadius = icon.height() * 0.55f;
        for (int i = 0; i < 10; i++)
        {
            final OBControl star = objectDict.get(String.format("top_bar_star_%d", 10 - i));
            final float startRadius = OB_Maths.PointDistance(star.position(), center);
            final float startAngle = (float) Math.toDegrees(Math.atan2(star.position().y - center.y, star.position().x - center.x));
            final float snapAngle = 36 * i;
            final float destAngle = 720 - snapAngle;
            anims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float angleDif = 720 * frac;
                    if (angleDif >= snapAngle)
                    {
                        float angleFrac = OB_Maths.bezef((angleDif - snapAngle) / destAngle);
                        float currentAngle = startAngle + angleFrac * (destAngle - startAngle);
                        float radiusFrac = OB_Maths.clamp(0, 1, (angleDif - snapAngle) / 150.0f);
                        float currentRadius = startRadius + radiusFrac * (destRadius - startRadius);
                        star.setPosition(center.x + currentRadius * (float) Math.cos(Math.toRadians(currentAngle)), center.y + currentRadius * (float) Math.sin(Math.toRadians(currentAngle)));
                    }
                }
            });

            PointF destPoint = new PointF(center.x + destRadius * 2 * (float) Math.cos(Math.toRadians(destAngle)), center.y + destRadius * 2 * (float) Math.sin(Math.toRadians(destAngle)));
            anims2.add(OBAnim.moveAnim(destPoint, star));
            anims2.add(OBAnim.rotationAnim((float) Math.toRadians(-360), star));
            anims2.add(OBAnim.opacityAnim(0, star));

        }
        float trophyScale = trophy.scale();
        trophy.setOpacity(0);
        trophy.setScale(trophyScale * 1.2f);
        trophy.show();
        anims2.add(OBAnim.opacityAnim(1, trophy));
        anims2.add(OBAnim.scaleAnim(trophyScale, trophy));
        playSfxAudio("awardfly", false);
        OBAnimationGroup.runAnims(anims, 4, true, OBAnim.ANIM_LINEAR, this);
        waitSFX();
        waitForSecs(0.5f);
        playSfxAudio("awardon", false);
        OBAnimationGroup.runAnims(anims2, 0.5, true, OBAnim.ANIM_EASE_IN, this);
        resetBarStars();
    }

    public void animateStarsOff()
    {
        List<OBAnim> anims = new ArrayList<>();
        OBControl icon = objectDict.get("icon");
        PointF centre = icon.position();
        for (int i = 1; i <= 10; i++)
        {
            OBControl star = objectDict.get(String.format("top_bar_star_%d", i));
            float destRadius = OB_Maths.PointDistance(star.position(), centre) * 1.5f;
            float angle = 36 * (i - 1);
            PointF destPoint = new PointF(centre.x + destRadius * (float) Math.cos(Math.toRadians(angle)), centre.y + destRadius * (float) Math.sin(Math.toRadians(angle)));
            anims.add(OBAnim.moveAnim(destPoint, star));
            anims.add(OBAnim.rotationAnim((float) Math.toRadians(-360), star));
            anims.add(OBAnim.opacityAnim(0, star));

        }
        OBAnimationGroup.runAnims(anims, 1, true, OBAnim.ANIM_EASE_IN, this);
        resetBarStars();
    }

    public void animateChoiceStars(final boolean enter, OBControl skipStar) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        OBControl icon = objectDict.get("icon");
        final PointF center = XPRZ_Generic.copyPoint(icon.position());
        final float startRadius = icon.height() * (enter ? 0.8f : 0.4f);
        final float targetRadius = icon.height() * (enter ? 0.4f : 0.8f);
        lockScreen();
        for (int i = 1; i <= 5; i++)
        {
            final OBControl star = objectDict.get(String.format("choice_star_%d", i));
            if (skipStar == star)
                continue;
            star.setOpacity(0);
            star.show();
            final float angle = 180 + (i - 1) * 72;
            star.setPosition(new PointF(center.x + startRadius * (float) Math.sin(Math.toRadians(angle)), center.y + startRadius * (float) Math.cos(Math.toRadians(angle))));
            anims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float currentRadius = startRadius + frac * (targetRadius - startRadius);
                    star.setPosition(new PointF(center.x + currentRadius * (float) Math.sin(Math.toRadians(angle)), center.y + currentRadius * (float) Math.cos(Math.toRadians(angle))));
                    star.setOpacity(enter ? frac : 1.0f - frac);
                }
            });
        }
        unlockScreen();
        if (enter)
            playSfxAudio("starson", false);
        OBAnimationGroup.runAnims(anims, 0.5, true, OBAnim.ANIM_EASE_OUT, this);
        if (enter)
            waitForSecs(0.3f);
    }

    public void animateTrophyShrink(OBGroup trophy) throws Exception
    {
        OBGroup smallTrophy = (OBGroup) trophy.propertyValue("trophy");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(smallTrophy.scaleY, trophy), OBAnim.moveAnim(smallTrophy.position(), trophy)), 0.5, true, OBAnim.ANIM_EASE_OUT, this);
        playSfxAudio("awardslot", false);
        lockScreen();
        lowlightTrophy(smallTrophy, false);
        detachControl(trophy);
        unlockScreen();
        waitSFX();
    }

    public void animateTrophiesSlide(boolean slideIn) throws Exception
    {
        PointF loc = trophySlideLocForLevel(slideIn ? 10 : lastUnit.level);
        OBControl con = objectDict.get("trophy_1");
        List<OBControl> trophies = new ArrayList<>();
        trophies.addAll(filterControls("trophy_.*"));
        trophies.remove(con);

        playSfxAudio("trophiesslide", false);
        OBMisc.moveControlWithAttached(con, trophies, loc, 0.7f, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitSFX();
    }

    public void animateStarFlyToBar(final OBControl star, int starNum) throws Exception
    {
        OBGroup target = (OBGroup) objectDict.get(String.format("top_bar_star_%d", starNum));
        float startScale = star.scale();
        playSfxAudio("starfly", false);
        final PointF center = XPRZ_Generic.copyPoint(star.position());
        final float startAngle = (float) Math.toDegrees(Math.atan2(target.position().y - center.y, target.position().x - center.x));
        final float destRadius = OB_Maths.PointDistance(target.position(), center);

        List<OBAnim> anims = new ArrayList<>();
        anims.add(new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float currentRadius = frac * destRadius;
                float currentAngle = startAngle + OB_Maths.easeout(OB_Maths.easeout(frac)) * 360;
                star.setPosition(center.x + currentRadius * (float) Math.cos(Math.toRadians(currentAngle)), center.y + currentRadius * (float) Math.sin(Math.toRadians(currentAngle)));
            }
        });
        anims.add(OBAnim.rotationAnim((float) Math.toRadians(720), star));
        anims.add(OBAnim.scaleAnim(target.scale(), star));

        OBAnimationGroup.runAnims(anims, 1, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);

        lockScreen();
        star.setScale(startScale);
        star.hide();
        star.setRotation(0);
        String colourName = (String) star.propertyValue("star_colour");
        colourStar(target, colourName);
        unlockScreen();
        waitSFX();
    }

    public void animateBigIconShow() throws Exception
    {
        playSfxAudio("button", false);
        float startScale = bigIcon.scale();
        bigIcon.setScale(startScale * 1.1f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(startScale, bigIcon), OBAnim.opacityAnim(1, bigIcon)), 0.5, true, OBAnim.ANIM_EASE_OUT, this);
    }


    public void animateLogoOn() throws Exception
    {
        List<OBAnim> logoWriteAnims = new ArrayList<>();

        OBGroup logo = (OBGroup)this.objectDict.get("onecourse_logo");
        lockScreen();
        logo.show();
        for(OBControl letter : logo.filterMembers("letter_.*",true))
        {
            OBGroup group = (OBGroup)letter;
            for(OBControl letterPart : group.filterMembers("path_.*",true))
            {
                OBPath path = (OBPath)letterPart;
                path.setStrokeEnd(0.5f);
                path.setStrokeStart(0.5f);
                logoWriteAnims.add(OBAnim.propertyAnim("strokeEnd",1,path));
                logoWriteAnims.add(OBAnim.propertyAnim("strokeStart",0,path));
            }
        }
        unlockScreen();

        OBAnimationGroup.runAnims(logoWriteAnims, 1, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.opacityAnim(1,objectDict.get("button_start"))),0.5,true,OBAnim.ANIM_LINEAR,this);

    }

/*
    public void animateLogoOn() throws Exception
    {
        List<OBAnim> logoWriteAnims = new ArrayList<>();
        List<OBAnim> tickShowAnim = new ArrayList<>();
        List<OBAnim> tickShrinkAnim = new ArrayList<>();
        OBGroup logo = (OBGroup)this.objectDict.get("onecourse_logo");
        lockScreen();
        logo.show();
        for(OBControl letter : logo.filterMembers("letter_.*",true))
        {
            OBGroup group = (OBGroup)letter;
            for(OBControl letterPart : group.filterMembers("path_.*",true))
            {
                OBPath path = (OBPath)letterPart;
                path.setStrokeEnd(0.5f);
                path.setStrokeStart(0.5f);
                logoWriteAnims.add(OBAnim.propertyAnim("strokeEnd",1,path));
                logoWriteAnims.add(OBAnim.propertyAnim("strokeStart",0,path));
            }
        }

        OBControl tick1 = this.objectDict.get("tick_1");
        final float targetY = tick1.position().y;
        final float targetRadius = tick1.bounds().width()*0.5f;
        for(int i=1; i<=4; i++)
        {
            final OBControl squareOld = this.objectDict.get(String.format("tick_%d",i));
            final OBControl square = new OBControl();
            square.setFrame(squareOld.bounds());
            square.setPosition(squareOld.position());
            square.setRotation(squareOld.rotation());
            square.setBackgroundColor(squareOld.fillColor());
            attachControl(square);
            square.setOpacity(0);
            square.show();
            square.setZPosition(200+i);
            square.setScale(0.3f);
            square.setShouldTexturise(true);
            final float fracdif = 0.1f * (i-1);
            tickShowAnim.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float frac2 = OB_Maths.clamp01((frac - fracdif)/0.6f);
                    square.setOpacity(OB_Maths.clamp01(frac2/0.35f));
                    float frac3 = OB_Maths.cubicbez(frac2,0.25f,0.91f,0.4f,1.4f).y;
                    square.setScale(frac3);
                }
            });


            OBControl targetCon = logo.objectDict.get(String.format("point_%d",i));

            final PointF destPoint = targetCon.getWorldPosition();
            final PointF startPoint = XPRZ_Generic.copyPoint(square.position());

            final float targetScale = targetCon.getWorldFrame().height()/square.bounds().height();
            tickShrinkAnim.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float frac2 = OB_Maths.clamp01((frac - fracdif)/0.6f);
                    square.setCornerRadius(OB_Maths.clamp01(frac2/0.5f)*targetRadius);
                    float frac3 = OB_Maths.cubicbez(frac2,0.07f,-0.89f,0.78f,-0.23f).y;
                    square.setScale(1 + frac3*(targetScale-1));
                    square.setPosition(OB_Maths.tPointAlongLine(OB_Maths.bezef(frac2),startPoint,destPoint));
                    square.setNeedsRetexture();
                    square.invalidate();

                }
            });

            logoWriteAnims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    square.setOpacity(1-OB_Maths.clamp01((frac-0.5f)/0.5f));
                }
            });
        }
        unlockScreen();
        OBAnimationGroup.runAnims(tickShowAnim, 1, true, OBAnim.ANIM_LINEAR, this);
        waitForSecs(0.5);
        OBAnimationGroup.runAnims(tickShrinkAnim, 1, true, OBAnim.ANIM_LINEAR, this);
        //waitForSecs(0.5);
        OBAnimationGroup.runAnims(logoWriteAnims, 1, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.opacityAnim(1,objectDict.get("button_start"))),0.5,true,OBAnim.ANIM_LINEAR,this);

    }
*/
    public void walkPresenterIn(PointF pt)
    {
        if (pt.x == presenter.control.position().x && pt.y == presenter.control.position().y)
            return;

        if (presenter.control.hidden)
        {
            presenter.control.setPosition((PointF) presenter.control.propertyValue("end_loc"));
            presenter.control.show();

        }
        presenter.walk(pt);
        presenter.faceFront();
    }

    public void walkPresenterOut()
    {
        presenter.walk((PointF) presenter.control.propertyValue("end_loc"));
        presenter.control.hide();
    }

    public void resetPresenter()
    {
        presenter.control.setPosition((PointF) presenter.control.propertyValue("end_loc"));
        presenter.selectArmIndex(0);
    }

    public void demoDayStartButton() throws Exception
    {

        if (bigIcon != null)
            bigIcon.setOpacity(0);

        waitForSecs(0.5);
        animateLogoOn();

        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 1.1f, objectDict.get("button_start").frame()), 0.3f, true);
        playAudioQueued((List<Object>) (Object) getAudioForScene("button", "DEMO"), true);
        waitForSecs(0.5f);
        thePointer.hide();

        final long time = setStatus(STATUS_AWAITING_CLICK);
        OBUtils.runOnOtherThreadDelayed(4, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if (time == statusTime)
                {
                    playAudioQueued((List<Object>) (Object) getAudioForScene("button", "REMINDER"), true);
                }
            }
        });
    }

    public void demounit1() throws Exception
    {
        demoDayStartButton();
    }

    public void demoday() throws Exception
    {
        demoDayStartButton();
    }

    public void demostartunit1() throws Exception
    {
        PointF loc = new PointF(0.5f * bounds().width(), presenter.control.position().y);
        playSfxAudio("musicalsting", false);
        walkPresenterIn(loc);
        waitSFX();
        waitForSecs(0.3f);
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO"), this);
        waitForSecs(0.5f);
        presenter.walk((PointF) presenter.control.propertyValue("start_loc"));
        presenter.faceFront();
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO2"), this);
        waitForSecs(0.3f);
        walkPresenterOut();
        animateBigIconShow();
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 1.1f, bigIcon.frame()), 0.3f, true);
        playAudioQueued((List<Object>) (Object) getAudioForScene("unit", "DEMO3"), true);
        waitForSecs(0.5f);
        thePointer.hide();

        playUnitButtonAudio(null);
    }

    public void demounit2() throws Exception
    {
        walkPresenterIn((PointF) presenter.control.propertyValue("start_loc"));
        waitForSecs(0.3f);
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO"), this);
        waitForSecs(0.3f);
        animateTopBar();
        waitForSecs(0.3f);
        presenter.moveHandfromIndex(0, 5, 0.3);
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO2"), this);
        waitForSecs(0.3f);
        presenter.moveHandfromIndex(5, 0, 0.3);
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO3"), this);
        waitForSecs(0.3f);
        walkPresenterOut();
        animateBigIconShow();

        playUnitButtonAudio(null);
    }


    public void demounitdefault() throws Exception
    {
        int audioIndex = getStartAudio();
        if (audioIndex > -1 )
        {
            walkPresenterIn((PointF) presenter.control.propertyValue("start_loc"));
            waitForSecs(0.3f);
            presenter.speak((List<Object>) (Object) Arrays.asList(getAudioForScene("unit", "DEMO").get(audioIndex)), this);

            waitForSecs(0.3f);
            walkPresenterOut();
        }
        animateBigIconShow();

        playUnitButtonAudio(OBUtils.randomlySortedArray(getAudioForScene("unit", "DEMO2")).get(0));
    }

    public void demostartday() throws Exception
    {
        getStartAudio();
        playSfxAudio("musicalsting", false);
        walkPresenterIn((PointF) presenter.control.propertyValue("start_loc"));
        waitSFX();
        waitForSecs(0.3f);
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO"), 0.5f, this);
        waitForSecs(0.5f);
        presenter.speak((List<Object>) (Object) getAudioForScene("unit", String.format("DEMO%d", OB_Maths.randomInt(2, 6))), 1.1f, this);

        waitForSecs(0.3f);
        walkPresenterOut();
        animateBigIconShow();

        playUnitButtonAudio(null);
    }

    public void demostar() throws Exception
    {

        final OBSectionController cont = this;

        walkPresenterIn((PointF) presenter.control.propertyValue("start_loc"));
        waitForSecs(0.3f);
        final String currentAudio = (lastUnit.level == 1 && lastUnit.awardStar == 1) ? "1" : "default";
        presenter.speak((List<Object>) (Object) getAudioForScene(currentAudio, "DEMO"), this);
        waitForSecs(0.3f);
        animateChoiceStars(true, null);
        final long time = setStatus(STATUS_AWAITING_CLICK);

        presenter.speak((List<Object>) (Object) getAudioForScene(currentAudio, "PROMPT"), this);

        OBUtils.runOnOtherThreadDelayed(4, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if (time == statusTime)
                    presenter.speak((List<Object>) (Object) getAudioForScene(currentAudio, "REMINDER"), cont);
            }
        });
    }

    public void demostar2() throws Exception
    {
        if (lastUnit.awardStar == 10)
        {
            List<String> audio = getAudioForScene("trophy", lastUnit.level == 1 ? "DEMO" : "DEMO2");
            presenter.speak((List<Object>) (Object) Collections.singletonList(audio.get(0)), this);
            waitForSecs(0.3f);
            OBGroup bigTrophy = loadBigTrophy(lastUnit.level);
            animateStarsSpinWithTrophy(bigTrophy);

            waitForSecs(0.3f);
            presenter.speak((List<Object>) (Object) Collections.singletonList(audio.get(1)), this);
            waitForSecs(1.3f);
            animateTrophiesSlide(true);
            animateTrophyShrink(bigTrophy);
            waitForSecs(0.3f);
            if (lastUnit.level == 1 || (lastUnit.level >= 5 && lastUnit.level < 10))
            {
                List<Integer> audioIndex = Arrays.asList(1, 5, 6, 7, 8, 9);
                int index = audioIndex.indexOf(lastUnit.level);
                if (index > -1)
                {
                    presenter.moveHandfromIndex(0, 1, 0.3);
                    presenter.speak((List<Object>) (Object) Collections.singletonList(getAudioForScene("trophy", "DEMO3").get(index)), this);
                    waitForSecs(1f);
                }

            }
            if (lastUnit.level == 10)
            {
                presenter.speak((List<Object>) (Object) getAudioForScene("trophy", "FINAL2"), this);
            } else
            {
                waitForSecs(0.5f);
                animateTrophiesSlide(false);
                waitForSecs(0.5f);
                loadTopBar(lastUnit.level + 1, false);
                presenter.moveHandfromIndex(1, 5, 0.3f);
                presenter.speak((List<Object>) (Object) getAudioForScene("trophy", "FINAL"), this);
                animateTopBar();
                presenter.moveHandfromIndex(4, 0, 0.3);
            }
        }

        if (fatController.currentSessionFinished())
        {
            prepareSectionForLastUnit(lastUnit, XPRZ_FatController.OFC_SESSION_TIMED_OUT);
            demopresentertimeout();
        } else
        {
            if (lastUnit.level != 10 || lastUnit.awardStar != 10)
            {
                waitForSecs(1.5f);
                presenter.speak((List<Object>) (Object) getAudioForScene("unit", "DEMO"), this);
                waitForSecs(0.3f);
                walkPresenterOut();
                animateBigIconShow();

                playUnitButtonAudio(null);
            } else
            {
                waitForSecs(0.3f);
                walkPresenterOut();
            }
        }

    }

    public void demotimeout() throws Exception
    {
        demopresentertimeout();
    }

    public void demopresentertimeout() throws Exception
    {
        setStatus(STATUS_BUSY);
        walkPresenterIn((PointF) presenter.control.propertyValue("start_loc"));
        waitForSecs(0.3f);
        presenter.speak((List<Object>) (Object) getAudioForScene("audio", "DEMO"), this);
        waitForSecs(0.3f);
        playSfxAudio("musicalsting", false);
        walkPresenterOut();
        prepareScreenLock();
        setStatus(STATUS_IDLE);

    }

    public void demotimeout2() throws Exception
    {

        setStatus(STATUS_BUSY);
        walkPresenterIn((PointF)presenter.control.propertyValue("start_loc"));
        waitForSecs(0.3f);
        presenter.speak((List<Object>) (Object) getAudioForScene("audio","DEMO2"), this);
        waitForSecs(0.3f);
        walkPresenterOut();
        prepareScreenLock();
        setStatus(STATUS_IDLE);
    }
}
