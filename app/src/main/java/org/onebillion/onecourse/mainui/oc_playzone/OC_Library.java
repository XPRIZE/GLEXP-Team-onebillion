package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.MutableBoolean;
import android.view.View;
import android.widget.Toast;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBRunnableSyncUI;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;

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
import org.onebillion.onecourse.utils.ScrollingHelper;

/**
 * Created by michal on 13/03/2017.
 */

public class OC_Library extends OC_SectionController
{
    List<List<Map<String,String>>> bookUnits;
    List<OBGroup> levelGroups;
    OBGroup currentGroup;
    OBControl currentIcon;
    PointF snapLoc;
    float limitLeft, limitRight, snapDist;
    boolean firstStart;
    String libraryMasterlist;

    @Override
    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    public void prepare()
    {
        firstStart= true;
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        setSceneXX(currentEvent());
        libraryMasterlist = OBConfigManager.sharedManager.getMasterlistForLibrary();
        String xmlPath = String.format("masterlists/%s/units.xml", libraryMasterlist);
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
        loadBookUnitsXML(xmlPath);
        loadIcons();
        int index = 0;
        for(OBGroup bookLine : levelGroups)
        {
            if(bookLine.isEnabled())
            {
                setBookLine(bookLine, lineLocation(bookLine,1).x);
                index++;
            }
        }
    }

    public void start()
    {
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

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl level = finger(-1, -1, filterControls("level_.*"), pt);
            if (level != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        OBGroup group = (OBGroup) level.propertyValue("books");
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

    public  void checkGroupIcon(PointF pt, final OBGroup curGroup, final OBControl curIcon) throws Exception
    {
        long currTime = SystemClock.currentThreadTimeMillis();
        PointF lastPosition = (PointF) curGroup.propertyValue("touch_loc");
        if (curIcon != null && (currTime - (long) curGroup.propertyValue("time")) < 1
                && Math.abs(lastPosition.x - pt.x) < snapDist)
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

    public void openBookForIcon(OBControl icon)
    {
        final Map<String,String> bookData = (Map<String,String>)icon.propertyValue("data");
        final String lastAppCode = OBConfigManager.sharedManager.getMainFolder();

        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    OBConfigManager.sharedManager.updateConfigPaths(bookData.get("config"), false, bookData.get("lang"));
                    if(MainViewController().pushViewControllerWithNameConfig(bookData.get("target"),bookData.get("config"),true,true,bookData.get("params")))
                    {
                        //unit started
                    }
                    else
                    {
                        if (OBConfigManager.sharedManager.isDebugEnabled())
                        {
                            Toast.makeText(MainActivity.mainActivity, bookData.get("target") + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, null);
                        }
                    }
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);
                    OBConfigManager.sharedManager.updateConfigPaths(lastAppCode, false, null);
                }
            }
        }.run();

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

    public void startSpeedMeasure(final long time,final OBGroup bookLine)
    {

        final Timer recordingTimer = new Timer();
        bookLine.setProperty("scrollSpeed", 1.0f);
        recordingTimer.scheduleAtFixedRate(new TimerTask()
    {

        @Override
        public void run()
        {
            if(time == (long)bookLine.propertyValue("time"))
            {
                float scrollSpeed = (float)bookLine.propertyValue("scrollSpeed");
                PointF lastPosition = (PointF)bookLine.propertyValue("last_loc") ;
                scrollSpeed = ((bookLine.position().x - lastPosition.x) - scrollSpeed)/-2.0f ;
                lastPosition = OBMisc.copyPoint(bookLine.position());
                bookLine.setProperty("last_loc",lastPosition);
                bookLine.setProperty("scrollSpeed",scrollSpeed);
            }
            else
            {
                recordingTimer.cancel();
            }
        }
    }, 5, 5);

    }

    /*
                        [levelUnits addObject:@{@"config":[node attributeStringValue:@"config"],
                                            @"target":[node attributeStringValue:@"target"],
                                            @"params":[node attributeStringValue:@"params"],
                                            @"icon":[node attributeStringValue:@"icon"],
                                            @"lang":[node attributeStringValue:@"lang"]}];
     */
    public void loadBookUnitsXML(String xmlPath)
    {
        if (xmlPath != null)
        {
            try
            {
                bookUnits = new ArrayList<>();
                OBXMLManager xmlman = new OBXMLManager();
                InputStream is = OBUtils.getInputStreamForPath(xmlPath);
                List<OBXMLNode> xml = xmlman.parseFile(is);
                if(xml.size() > 0)
                {
                    OBXMLNode rootNode = xml.get(0);
                    List<OBXMLNode> xmlLevels = rootNode.childrenOfType("level");
                    for(OBXMLNode xmlLevel : xmlLevels)
                    {
                        List<Map<String,String>> levelUnits = new ArrayList<>();
                        List<OBXMLNode> xmlunits = xmlLevel.childrenOfType("unit");
                        for (OBXMLNode n : xmlunits)
                        {
                            Map<String,String> map = new ArrayMap<>();
                            map.put("config", n.attributeStringValue("config"));
                            map.put("target", n.attributeStringValue("target"));
                            map.put("params", n.attributeStringValue("params"));
                            map.put("icon", n.attributeStringValue("icon"));
                            map.put("lang", n.attributeStringValue("lang"));
                            levelUnits.add(map);

                        }
                        bookUnits.add(levelUnits);
                    }

                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

    }

    public void loadIcons()
    {
        levelGroups = new ArrayList<>();
        OBControl icon = objectDict.get("icon");
        int level = 1;
        int locIndex = 0;
        for(List<Map<String,String>> booksLevel : bookUnits)
        {
            OBControl levelControl = objectDict.get(String.format("level_%d",level));
            PointF startPoint = OBMisc.copyPoint(levelControl.position());
            startPoint.x = icon.position().x;
            List<OBControl> bookIcons = new ArrayList<>();
            int index = 0;
            for(Map<String,String> bookData : booksLevel)
            {
                OBImage unitIcon = loadIconForData(bookData);
                PointF loc = OBMisc.copyPoint(startPoint);
                loc.x += index*1.2*unitIcon.width();
                unitIcon.setPosition(loc);
                bookIcons.add(unitIcon);
                index++;
            }
            OBGroup group = new OBGroup(bookIcons);
            group.setShouldTexturise(false);


            attachControl(group);

            group.setZPosition(20);
            levelGroups.add(group);
            if(group.width() <  limitRight - limitLeft)
            {
                PointF loc = OBMisc.copyPoint(group.position());
                loc.x =  OB_Maths.locationForRect(0.5f,0.5f,this.bounds()).x;
                group.setPosition(loc);
                group.disable();

            }
            else
            {
                setBookLine(group,lineLocation(group,locIndex==1?2:0).x);
                locIndex++;
                group.enable();

            }
            levelControl.setProperty("books",group);
            group.setProperty("snap_loc",startPoint);
            group.setProperty("start_loc",OBMisc.copyPoint(group.position()));
            level++;
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

    public OBImage loadIconForData(Map<String,String> bookData)
    {
        String iconsPath = String.format("masterlists/%s/icons", libraryMasterlist);
        String imgName = String.format("%s_small.png",bookData.get("icon"));
        OBImage bigic = null;
        String completePath = String.format("%s/%s",iconsPath,imgName);

        bigic = OBImageManager.sharedImageManager().imageForPath(completePath);
        bigic.setRasterScale(2);
        if(bigic == null)
        {
            bigic = loadImageWithName("icon_default", new PointF(0,0), boundsf());
        }
        bigic.setProperty("data",bookData);
        attachControl(bigic);
        bigic.setZPosition(20);
        OBControl icon = objectDict.get("icon");
        bigic.setScale(bigic.scale() * icon.width()/bigic.width());
        return bigic;

    }


}
