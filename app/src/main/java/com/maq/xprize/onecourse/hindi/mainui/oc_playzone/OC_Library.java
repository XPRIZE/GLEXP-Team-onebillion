package com.maq.xprize.onecourse.hindi.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.MutableBoolean;
import android.view.View;
import android.widget.Toast;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OBMainViewController;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.mainui.OC_Menu;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.MlUnit;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBConfigManager;
import com.maq.xprize.onecourse.hindi.utils.OBImageManager;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBRunnableSyncUI;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBXMLManager;
import com.maq.xprize.onecourse.hindi.utils.OBXMLNode;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;
import com.maq.xprize.onecourse.hindi.utils.OB_MutInt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.maq.xprize.onecourse.hindi.utils.OCM_FatController;
import com.maq.xprize.onecourse.hindi.utils.OCM_MlUnit;
import com.maq.xprize.onecourse.hindi.utils.OCM_MlUnitInstance;
import com.maq.xprize.onecourse.hindi.utils.ScrollingHelper;

/**
 * Created by michal on 13/03/2017.
 */

public class OC_Library extends OC_Menu
{
    final static int LIBRARY_TIMEOUT = 10*60;
    List<OBGroup> levelGroups;
    OBGroup currentGroup;
    OBControl currentIcon;
    PointF snapLoc;
    float limitLeft, limitRight, snapDist, touchLeft, touchRight;
    boolean firstStart;
    OCM_FatController fatController;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    public void prepare()
    {
        firstStart= true;
        timeoutHandler = new Handler();

        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        setSceneXX(currentEvent());

        OBGroup border1 = (OBGroup)objectDict.get("border_1");
        OBGroup border2 = (OBGroup)objectDict.get("border_2");
        OBControl icon = objectDict.get("icon");
        border1.setZPosition(100);
        border2.setZPosition(100);
        OBControl border1con = border1.objectDict.get("border_1");

        snapLoc = OB_Maths.locationForRect(1f,0.5f,border1con.getWorldFrame());
        float dist = icon.width()*0.4f;
        snapDist = icon.width()*0.2f;
        limitLeft = border1.right() + dist;
        limitRight = border2.left() - dist;
        touchLeft = border1.right();
        touchRight = border2.left();
        fatController = (OCM_FatController)MainActivity.mainActivity.fatController;;
        Map<Integer,List<OCM_MlUnit>> libraryUnits = fatController.getUnitsForLibrary();
        loadBooksForUnits(libraryUnits);
    }

    public void start()
    {
        startLibraryTimeout();
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(firstStart)
                {
                    animateBooksOn();
                    firstStart = false;
                }
                setStatus(STATUS_WAITING_FOR_DRAG);
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
    }

    public void doMainXX()
    {

    }

    @Override
    public void cleanUp()
    {
        stopLibraryTimeout();
        super.cleanUp();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl level = finger(-1, -1, filterControls("level_.*"), pt);
            if (level != null)
            {
                setStatus(STATUS_BUSY);
                stopLibraryTimeout();
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        OBGroup group = (OBGroup) level.propertyValue("books");
                        if (group == null) return;
                        //
                        currentIcon = finger(-1, -1, group.members, pt);
                        dragOffset = OB_Maths.DiffPoints(group.position(), pt);
                        currentGroup = group;
                        currentGroup.setProperty("touch_loc", pt);
                        if(currentGroup.isEnabled())
                        {
                            ScrollingHelper.prepareForScrollMeasureTickValue(currentGroup, 0.0025f, 0.99f,
                                    applyGraphicScale(0.08f), applyGraphicScale(0.08f));

                        }
                        long time = setStatus(STATUS_DRAGGING);
                        currentGroup.setProperty("time", time);
                    }

                });
            }
        }

    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && currentGroup != null && currentGroup.isEnabled())
        {
            float x = OB_Maths.AddPoints(pt, dragOffset).x;
            setBookLine(currentGroup,x);
            ScrollingHelper.measureScrollSpeed(currentGroup);
        }

    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && currentGroup != null)
        {
            setStatus(STATUS_BUSY);
            final OBGroup curGroup = currentGroup;
            final OBControl curIcon = currentIcon;
            currentGroup = null;
            currentIcon = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkGroupIcon(pt, curGroup, curIcon);

                }
            });

        }

    }

    public void checkGroupIcon(PointF pt, final OBGroup curGroup, final OBControl curIcon) throws Exception
    {
        long currTime = SystemClock.currentThreadTimeMillis();
        PointF lastPosition = (PointF) curGroup.propertyValue("touch_loc");
        if (curIcon != null && (currTime - (long) curGroup.propertyValue("time")) < 1
                && Math.abs(lastPosition.x - pt.x) < snapDist && pointTouchAccepted(pt)
                && pointTouchAccepted(lastPosition))
        {
            curIcon.highlight();
            openBookForIcon(curIcon);
            OBUtils.runOnOtherThreadDelayed(1, new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    curIcon.lowlight();
                }
            });
        }
        long time = setStatus(STATUS_WAITING_FOR_DRAG);
        if (!curGroup.isEnabled())
            return;
        curGroup.setProperty("time", time);
        MutableBoolean finished = new MutableBoolean(false);
        while (!this._aborting && !finished.value && time == (long)curGroup.propertyValue("time"))
        {
            if (time == (long)curGroup.propertyValue("time"))
            {
                PointF pos = ScrollingHelper.nextScrollingLocation(curGroup, finished);
                if (!setBookLine(curGroup, pos.x))
                    finished.value = true;

            }
            waitForSecs(0.001f);
        }
        if (!this._aborting)
            snapClosest(time, curGroup, false);
    }

    public boolean pointTouchAccepted(PointF pt)
    {
        return pt.x > touchLeft && pt.x < touchRight;
    }

    public void openBookForIcon(final OBControl icon)
    {
        OCM_MlUnit unit = (OCM_MlUnit)icon.propertyValue("unit");
        fatController.startUnit(unit, OCM_MlUnitInstance.INSTANCE_TYPE_LIBRARY,new OCM_FatController.SectionOpeningCallback()
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
                        icon.lowlight();
                        if(!success)
                        {
                            start();
                        }
                    }
                });
            }
        });
    }

    public void animateBooksOn() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBGroup bookLine : levelGroups)
        {
            if(bookLine.isEnabled())
                anims.add(OBAnim.moveAnim((PointF)bookLine.propertyValue("start_loc") ,bookLine));
        }

        for(OBAnim anim : anims)
        {
            boolean isLast = anims.get(anims.size()-1) == anim;
            OBAnimationGroup.runAnims(Collections.singletonList(anim),1,isLast,OBAnim.ANIM_EASE_OUT,this);
            if(!isLast)
                waitForSecs(0.5f);

        }

    }

    public boolean setBookLine(OBGroup bookLine,float x)
    {
        PointF pos = OBMisc.copyPoint(bookLine.position());
        float width = bookLine.width() * 0.5f;
        if(x+width <= limitRight)
        {
            pos.x = limitRight-width;
            lockScreen();
            bookLine.setPosition(pos);
            unlockScreen();
            return false;
        }
        else if(x-width >= limitLeft)
        {
            pos.x = limitLeft+width;
            lockScreen();
            bookLine.setPosition(pos);
            unlockScreen();
            return false;
        }
        else
        {
            pos.x = x;
            lockScreen();
            bookLine.setPosition(pos);
            unlockScreen();
            return true;
        }
    }

    public void snapClosest(long time, OBGroup bookLine, boolean instant) throws Exception
    {
        if (bookLine.left() >= limitLeft - snapDist || bookLine.right() < (limitRight + snapDist))
            return;

        OBControl icon = OBMisc.nearestControlToPoint(bookLine.members, convertPointToControl(snapLoc, bookLine));
        if (icon == null)
        {
            return;
        }
        float dist = OB_Maths.locationForRect(0.5f, 0.5f, icon.getWorldFrame()).x - snapLoc.x;
        if (instant)
        {
            lockScreen();
            bookLine.setPosition(bookLine.position().x - dist , bookLine.position().y);
            unlockScreen();
        }
        else
        {
            float width = icon.width() * 0.5f;
            PointF startPos = OBMisc.copyPoint(bookLine.position());
            float frac = 0;
            long starttime = System.currentTimeMillis();
            float duration = 0.35f * Math.abs(dist) / width;
            if (duration <= 0)
                return;
            else if (duration < 0.05f)
                duration = 0.05f;

            while (time == (long) bookLine.propertyValue("time") && frac < 1)
            {
                frac = (System.currentTimeMillis() - starttime) / (duration * 1000);
                frac = OB_Maths.easein(frac);
                lockScreen();
                bookLine.setPosition(startPos.x - dist * frac, bookLine.position().y);
                unlockScreen();
                waitForSecs(0.005f);

            }
        }

    }

    public void measureGroupSpeed(OBGroup bookLine)
    {
        long time = System.currentTimeMillis();
        long lastTime = (long)bookLine.propertyValue("last_scroll");
        float lastScrollSpeed = (float)bookLine.propertyValue("scrollSpeed");
        PointF lastPosition = (PointF)bookLine.propertyValue("last_loc") ;
        PointF currPosition = OBMisc.copyPoint(bookLine.position());
        long elapsed = time - lastTime;
        float delta = currPosition.x - lastPosition.x;
        float v = delta / (1+elapsed);
        float scrollSpeed = 0.8f * v + 0.2f * lastScrollSpeed;
        bookLine.setProperty("last_loc",currPosition);
        bookLine.setProperty("scrollSpeed",-scrollSpeed);
        currentGroup.setProperty("last_scroll",time);
    }

    public void loadBooksForUnits(Map<Integer,List<OCM_MlUnit>> units)
    {
        levelGroups = new ArrayList<>();
        OBControl icon = objectDict.get("icon");
        int level = 1;
        int locIndex = 0;
        for(int i = 0; i<units.size(); i++)
        {
            List<OCM_MlUnit> booksLevel = units.get(i+1);
            OBControl levelControl = objectDict.get(String.format("level_%d", level));
            PointF startPoint = OBMisc.copyPoint(levelControl.position());
            startPoint.x = icon.position().x;
            List<OBControl> bookIcons = new ArrayList<>();
            int index = 0;
            for (OCM_MlUnit unit : booksLevel)
            {
                OBImage unitIcon = loadIconForUnit(unit);
                PointF loc = OBMisc.copyPoint(startPoint);
                loc.x += index * 1.2 * unitIcon.width();
                unitIcon.setPosition(loc);
                bookIcons.add(unitIcon);
                index++;
            }
            OBGroup group = new OBGroup(bookIcons);
            group.setShouldTexturise(false);

            attachControl(group);

            group.setZPosition(20);
            levelGroups.add(group);
            int currentLoc = OB_Maths.randomInt(1, booksLevel.size()>7 ? booksLevel.size()-6 : 1);
            if (group.width() < limitRight - limitLeft)
            {
                PointF loc = OBMisc.copyPoint(group.position());
                loc.x = OB_Maths.locationForRect(0.5f, 0.5f, this.bounds()).x;
                group.setPosition(loc);
                group.disable();
            }
            else
            {
                setBookLine(group, lineLocation(group,currentLoc).x);
                locIndex++;
                group.enable();
            }
            levelControl.setProperty("books", group);
            group.setProperty("snap_loc", startPoint);
            group.setProperty("start_loc", OBMisc.copyPoint(group.position()));
            level++;
            if(group.isEnabled())
                setBookLine(group, lineLocation(group,locIndex%2 == 0 ? currentLoc - 1 : currentLoc +1 ).x);

        }
    }

    public PointF lineLocation(OBGroup bookLine,int index)
    {
        PointF loc = OBMisc.copyPoint(bookLine.position());
        if(index < bookLine.members.size())
        {
            OBControl centerIcon = bookLine.members.get(index);
            PointF centerLoc =  centerIcon.getWorldPosition();
            loc.x -= centerLoc.x - snapLoc.x;
        }
        return loc;
    }

    public PointF lineLocationForIcon(OBGroup bookLine,OBControl icon)
    {
        PointF loc = OBMisc.copyPoint(bookLine.position());
        PointF centerLoc =  icon.getWorldPosition();
        loc.x -= centerLoc.x - OB_Maths.locationForRect(0.5f,0.5f,this.bounds()).x;
        return loc;
    }

    public OBImage loadIconForUnit(OCM_MlUnit unit)
    {
        OBImage bigic = null;
        String completePath =  unit.pathToIcon(true);

        bigic = OBImageManager.sharedImageManager().imageForPath(completePath);
        bigic.setRasterScale(2);
        if(bigic == null)
        {
            bigic = loadImageWithName("icon_default", new PointF(0,0), boundsf());
        }
        bigic.setProperty("unit",unit);
        attachControl(bigic);
        bigic.setZPosition(20);
        OBControl icon = objectDict.get("icon");
        bigic.setScale(bigic.scale() * icon.width()/bigic.width());
        return bigic;
    }

    public void startLibraryTimeout()
    {
        stopLibraryTimeout();
        timeoutRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                triggerLibraryTimeout();
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable,LIBRARY_TIMEOUT*1000);
    }

    public void stopLibraryTimeout()
    {
        if(timeoutRunnable != null && timeoutHandler != null)
        {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    public void triggerLibraryTimeout()
    {
        if(!_aborting)
        {
            fatController.closeCurrentUnitInstance(OCM_MlUnitInstance.STATUS_UNIT_TIMEOUT,this);
            exitEvent();
        }
    }


}
