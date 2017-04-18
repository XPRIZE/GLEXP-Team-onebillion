package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;
import android.widget.Toast;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.MlUnit;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michal on 13/03/2017.
 */

public class OC_Library extends OC_SectionController
{
    List<List<MlUnit>> bookUnits;
    List<OBGroup> levelGroups;
    OBGroup currentGroup;
    OBControl currentIcon;
    PointF snapLoc;
    float limitLeft, limitRight, snapDist;
    final static String LIBRARY_MASTERLIST = "library";
    boolean firstStart;

    public void prepare()
    {
        firstStart= true;
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        setSceneXX(currentEvent());
        String xmlPath = String.format("masterlists/%s/units.xml", LIBRARY_MASTERLIST);
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
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {

                        OBGroup group = (OBGroup) level.propertyValue("books");
                        currentIcon = finger(-1, -1, group.members, pt);
                        setStatus(STATUS_BUSY);
                        dragOffset = OB_Maths.DiffPoints(group.position(), pt);
                        currentGroup = group;
                        currentGroup.setProperty("scrollSpeed", 0.0f);
                        currentGroup.setProperty("touch_loc", pt);
                        currentGroup.setProperty("last_loc", OBMisc.copyPoint(currentGroup.position()));
                        long time = setStatus(STATUS_DRAGGING);
                        currentGroup.setProperty("time", time);
                        currentGroup.setProperty("last_scroll", System.currentTimeMillis());
                       /* if (group.isEnabled())
                            startSpeedMeasure(time, currentGroup);*/

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
            measureGroupSpeed(currentGroup);
        }

    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && currentGroup != null)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    final OBGroup curGroup = currentGroup;
                    final OBControl curIcon = currentIcon;
                    currentGroup = null;
                    currentIcon = null;
                    long startTime = System.currentTimeMillis();
                    PointF lastPosition = (PointF)curGroup.propertyValue("touch_loc");
                    float scrollSpeed = (float) curGroup.propertyValue("scrollSpeed");
                    boolean bookStarted = false;
                    if (curIcon != null && (startTime - (long) curGroup.propertyValue("time")) < 1000
                            && Math.abs(lastPosition.x - pt.x) < snapDist && scrollSpeed < applyGraphicScale(1))
                    {
                        bookStarted= true;
                        curIcon.highlight();
                        //curIcon.setHighlightColour(Color.argb(125,0,0,0));
                        MlUnit unit = (MlUnit)curIcon.propertyValue("unit");
                       // startSectionByUnit(unit);
                        setStatus(STATUS_WAITING_FOR_DRAG);
                        OBUtils.runOnOtherThreadDelayed(0.5f, new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                curIcon.lowlight();
                                snapClosest(0, curGroup, true);
                            }
                        });

                    }
                    if(!bookStarted)
                    {
                        long time = setStatus(STATUS_WAITING_FOR_DRAG);
                        if (!curGroup.isEnabled()) return;
                        curGroup.setProperty("time", time);
                        float distance = scrollSpeed * 1000 * 0.5f;
                        float targetX = Math.round(curGroup.position().x - distance );
                        while (Math.abs(scrollSpeed) > applyGraphicScale(0.5f) && time == (long) curGroup.propertyValue("time"))
                        {
                            waitForSecs(0.005f);
                            long currTime = System.currentTimeMillis();
                            float  delta = distance * (float)Math.exp(-(currTime - startTime) / 325.0f);
                            if (Math.abs(delta) > applyGraphicScale(10f) && time == (long) curGroup.propertyValue("time"))
                            {
                                if (!setBookLine(curGroup, targetX + delta))
                                    scrollSpeed = 0;
                            }
                            else
                            {
                                break;
                            }
                        }
                        snapClosest(time, curGroup, false);
                    }
                }
            });
        }
    }

    public void startSectionByUnit(final MlUnit unit)
    {
        final String lastAppCode = (String) MainActivity.mainActivity.config.get(MainActivity.CONFIG_APP_CODE);

        new OBRunnableSyncUI()
        {
            @Override
            public void ex ()
            {
                try
                {
                    MainActivity.mainActivity.updateConfigPaths(unit.config, false, unit.lang);
                    if(MainViewController().pushViewControllerWithNameConfig(unit.target,unit.config,true,true,unit.params))
                    {
                        //unit started
                    }
                    else
                    {
                        if (MainActivity.mainActivity.isDebugMode())
                        {
                            Toast.makeText(MainActivity.mainActivity, unit.target + " hasn't been converted to Android yet.", Toast.LENGTH_LONG).show();
                            MainActivity.mainActivity.updateConfigPaths(lastAppCode, false, null);
                        }
                    }
                }
                catch (Exception exception)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "Error in runOnMainThread", exception);

                    MainActivity.mainActivity.updateConfigPaths(lastAppCode, false, null);
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
                        List<MlUnit> levelUnits = new ArrayList<>();
                        List<OBXMLNode> xmlunits = xmlLevel.childrenOfType("unit");
                        for (OBXMLNode n : xmlunits)
                        {
                            MlUnit m = MlUnit.mlUnitFromXMLNode(n);
                            m.key = n.attributeStringValue("id");
                            levelUnits.add(m);


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
        for(List<MlUnit> booksLevel : bookUnits)
        {
            OBControl levelControl = objectDict.get(String.format("level_%d",level));
            PointF startPoint = OBMisc.copyPoint(levelControl.position());
            startPoint.x = icon.position().x;
            List<OBControl> bookIcons = new ArrayList<>();
            int index = 0;
            for(MlUnit unit : booksLevel)
            {
                OBImage unitIcon = loadIconForUnit(unit);
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

    public OBImage loadIconForUnit(MlUnit unit)
    {
        String iconsPath = String.format("masterlists/%s/icons", LIBRARY_MASTERLIST);
        String imgName = String.format("%s_small.png",unit.icon);
        OBImage bigic = null;
        String completePath = String.format("%s/%s",iconsPath,imgName);

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


}
