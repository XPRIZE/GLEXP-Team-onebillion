package com.maq.xprize.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.maq.xprize.onecourse.controls.*;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Created by michal on 01/08/16.
 */
public class OC_CountMore_S1 extends OC_SectionController
{
    Map<String,Integer> eventColours;

    float maxRunningDuration;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColours = new ArrayMap<>();
        eventColours= OBMisc.loadEventColours(this);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBGroup track = (OBGroup)objectDict.get("race_track");
        track.setLeft(bounds().width()*0.2f);
        track.setProperty("left",track.left());
        setSceneXX(currentEvent());

    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1b();
            }
        });
    }

    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);

        if(OBUtils.getBooleanValue(eventAttributes.get("load_place")))
            loadChildrenPlaces();
    }

    @Override
    public void doMainXX() throws Exception
    {
        startScene();
    }

    @Override
    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            goToCard(OC_CountMore_S1n.class, "event1");
        }
        catch (Exception e)
        {

        }

    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(eventAttributes.get("target").equals("button"))
            {

                if(finger(0,1, Collections.singletonList(objectDict.get("button")),pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkButton();
                        }
                    });
                }
            }
            else if(eventAttributes.get("target").equals("child"))
            {
                final OBControl child = finger(0,1,filterControls("child_.*"),pt);
                if(child != null)
                {
                    setStatus(STATUS_BUSY);
                    playAudio(null);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkChild((OBGroup)child);
                        }
                    });

                }
            }
        }

    }

    private void checkButton() throws Exception
    {
        OBGroup button = (OBGroup)objectDict.get("button");
        button.objectDict.get("highlight").show();
        playSfxAudio("button",true);
        button.hide();
        button.objectDict.get("highlight").hide();
        startRace();
        waitForSecs(0.3f);
        nextScene();
    }

    private void checkChild(OBGroup child) throws Exception
    {
        if(child == objectDict.get(String.format("child_%s", eventAttributes.get("correct"))))
        {
            gotItRightBigTick(true);
            child.setProperty("animate",false);
            lockScreen();
            child.hide();
            objectDict.get(String.format("place_%s", child.attributes().get("id"))).show();
            unlockScreen();
            waitForSecs(0.3f);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }


    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }


    public void startRace() throws Exception
    {
        playSfxAudio("run",false);
        float maxDuration = 0;
        lockScreen();
        List<OBAnim> anims = new ArrayList<>();
        for(int i = 1; i<=filterControls("child_.*").size(); i++)
        {
            final OBGroup child = (OBGroup)objectDict.get(String.format("child_%d", i));
            float duration =  Float.valueOf((String)child.attributes().get("duration"));
            float end = 0.72f + 0.06f*(i-1);

            final OBGroup track = (OBGroup)objectDict.get("race_track");
            OBPath finish = (OBPath)track.objectDict.get("finish");
            final PointF loc = OB_Maths.relativePointInRectForLocation(child.position(), track.frame);
            final float start = loc.x;
            final float dif = end - loc.x;
            PointF cross = new PointF();
            OB_Maths.lineSegmentsIntersect(new PointF(child.position().x, child.bottom()),
                    new PointF(OB_Maths.locationForRect(end,0f,track.frame()).x,child.bottom()),
                    convertPointFromControl(finish.firstPoint(), finish),
                    convertPointFromControl(finish.lastPoint(),finish), cross);
            final PointF relCross = OB_Maths.relativePointInRectForLocation(cross, track.frame);

            float runDist = OB_Maths.PointDistance(child.position(), cross);
            final float fullDist = OB_Maths.PointDistance(child.position(), OB_Maths.locationForRect(new PointF(end, loc.y),track.frame));

            final float fullDuration = duration*fullDist/runDist;
            if(maxDuration < fullDuration)
                maxDuration = fullDuration;
            child.setProperty("running",true);
            animateFrames(Arrays.asList("run0", "run1"),0,child);
            anims.add(OBAnim.sequenceAnim(child,OBUtils.getFramesList("run",1,10),duration/100.0f,true));
            anims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    float frac2 = OB_Maths.clamp(0,1,frac*maxRunningDuration/fullDuration);
                    float x = start + dif*frac2;
                    child.setPosition ( OB_Maths.locationForRect(new PointF(x, loc.y),track.frame));
                    if((boolean)child.propertyValue("running") &&
                            child.objectDict.get("front").getWorldPosition().x > OB_Maths.locationForRect(relCross.x,relCross.y,track.frame()).x)
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                playSfxAudio("finish",false);
                            }
                        });
                        child.setProperty("running",false);
                    }

                    if(frac2 == 1)
                    {
                        hideSublayers(child,"run");
                        child.objectDict.get("stand").show();
                        child.objectDict.get("wave0").show();
                    }

                }
            });

        }
        maxRunningDuration = maxDuration;
        unlockScreen();
        final OBControl raceTrack = objectDict.get("race_track");
        final float raceTrackRightStart = raceTrack.right();
        final float raceTrackRightEnd = bounds().width()*1.08f;
        final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        anims.add(new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                raceTrack.setRight(raceTrackRightStart +  OB_Maths.clamp(0,1,interpolator.getInterpolation(OB_Maths.clamp(0,1,2 * frac)))*(raceTrackRightEnd-raceTrackRightStart));
            }
        });
        OBAnimationGroup.runAnims(anims,maxRunningDuration,true,OBAnim.ANIM_LINEAR,this);
        playSFX(null);
        waitForSecs(0.3f);
        animateChildrenStand();

    }


    public void walkChildIn(final OBGroup child)
    {
        hideAllSublayers(child);
        child.objectDict.get("stand").hide();

        PointF loc = (PointF)child.propertyValue("start_loc");
        child.show();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc, child), OBAnim.sequenceAnim(child, OBUtils.getFramesList("walk", 1, 8), 0.1f, true)), 2, false, OBAnim.ANIM_LINEAR,
                new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                lockScreen();
                                hideSublayers(child, "walk");
                                child.objectDict.get("walk0").show();
                                unlockScreen();
                            }
                        });
                    }
                },
                this);

    }

    public void hideSublayers(OBGroup child, String layerName)
    {
        for (String key : child.objectDict.keySet())
        {
            if(key.startsWith(layerName))
                child.objectDict.get(key).hide();
        }
    }

    public void hideAllSublayers(OBGroup child)
    {
        for (String key : child.objectDict.keySet())
        {
            child.objectDict.get(key).hide();
        }
    }


    public void loadChildAndWalkIn() throws Exception
    {
        List<String> loadChildren = OBUtils.randomlySortedArray(Arrays.asList(eventAttributes.get("load_run").split(",")));
        boolean audioPlayed = false;
        for(String num : loadChildren)
        {
            setSceneXX(String.format("%s_child_%s", currentEvent(), num));
            if(!audioPlayed)
                playAudioQueued((List<Object>)(Object)Arrays.asList(getAudioForScene("sfx","walk").get(0),new Integer(100)), false, -1);

            audioPlayed= true;
            String childName = String.format("child_%s", num);
            OBGroup child = (OBGroup)objectDict.get(childName);
            child.setZPosition(5-OBUtils.getIntValue(num));
            PointF loc = OC_Generic.copyPoint(child.position());
            OBControl reload = objectDict.get(String.format("reload_%s",child.attributes().get("id")));
            if(reload == null)
            {
                child.setProperty("start_loc",loc);
            }
            else
            {
                hideAllSublayers(child);
                loc = (PointF)child.propertyValue("start_loc");
                child.attributes().put("duration",reload.attributes().get("duration"));
                child.attributes().put("stand", reload.attributes().get("stand"));
            }

            child.setPosition(loc.x - bounds().width() * 0.35f, loc.y);

            walkChildIn(child);
        }
        waitForSecs(2f);
        playAudio(null);
        waitForSecs(0.5f);
        for(String num : OBUtils.randomlySortedArray(loadChildren))
        {
            String childName = String.format("child_%s", num);
            OBGroup child = (OBGroup)objectDict.get(childName);
            animateFrames(Arrays.asList("walk0", "run0"),0.1f,child);
            waitForSecs(0.25f);
        }

    }

    public void walkChildrenIn() throws Exception
    {

        List<OBControl> children = OBUtils.randomlySortedArray(filterControls("child_.*"));
        for(OBControl child : children)
        {
            walkChildIn((OBGroup)child);
            waitForSecs(0.5f);
        }
        waitForSecs(1.5f);
        playAudio(null);
        waitForSecs(0.3f);

    }


    public void animateChildrenStand() throws Exception
    {
       for(final OBControl control : filterControls("child_.*"))
       {
        if(control.attributes().get("stand").equals("cross"))
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    animateFrames(Arrays.asList("wave0", "cross1", "cross2"),0.2f,(OBGroup)control);
                }
            });
        }
        else if(((String)control.attributes().get("stand")).startsWith("wave"))
        {
            control.setProperty("animate",true);
            final  List<String> frames = new ArrayList<>();
            frames.add("wave0");
            frames.addAll(OBUtils.getFramesList(String.format("%s_",control.attributes().get("stand")), 1, 3));
            if(control.attributes().get("stand").equals("wave1"))
                frames.addAll(Arrays.asList("wave1_2", "wave1_1"));

            List<String> reversedFrames = new ArrayList<>(frames);
            Collections.reverse(frames);
            reversedFrames.remove(0);
            frames.addAll(reversedFrames);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    while((boolean)control.propertyValue("animate"))
                    {
                        waitForSecs(0.2f);
                        if((boolean)control.propertyValue("animate"))
                            animateFrames(frames,0.15f,(OBGroup)control);
                    }
                }
            });
        }
    }
        waitForSecs(0.6f);
    }

    public void loadChildrenPlaces()
    {
        OBGroup orgShield = (OBGroup)objectDict.get("place");
        float scale = orgShield.scale();
        orgShield.setScale(1);
        for(OBControl control : filterControls("place_child_.*"))
        {
            int place = Integer.valueOf((String)control.attributes().get("place"));
            OBGroup shield = (OBGroup)orgShield.copy();
            shield.setScale(scale);
            OBPath path = (OBPath)shield.objectDict.get("place");
            path.setFillColor(eventColours.get(String.format("place%d",place)));
            shield.show();
            attachControl(shield);
            OBMisc.insertLabelIntoGroup(shield,place,75.0f*shield.height()/95.0f, Color.BLACK,shield.getWorldPosition(),this);
            control.setZPosition(20);
            shield.setZPosition(21);
            PointF loc = OC_Generic.copyPoint(shield.position());
            loc.x = control.position().x;
            shield.setPosition(loc);
            control.show();
            OBGroup newGroup = new OBGroup(Arrays.asList(control,shield));
            newGroup.objectDict = ((OBGroup)control).objectDict;
            attachControl(newGroup);
            newGroup.hide();
            objectDict.put((String)control.attributes().get("id"),newGroup);
        }

        orgShield.setScale(scale);
    }



    public void demo1b() throws Exception
    {
        demoButtons();
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-35,0.5f,true);
        loadChildAndWalkIn();
        moveScenePointer(OB_Maths.locationForRect(0.1f,1f,objectDict.get("race_track").frame()),-40,0.5f,"DEMO",0,0.3f);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("button").frame()),-40,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo1h() throws Exception
    {
        OBGroup track = (OBGroup)objectDict.get("race_track");
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.propertyAnim("left",(float)track.propertyValue("left"),track)),1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        loadChildAndWalkIn();
        objectDict.get("button").show();
        startScene();
    }

    public void demoCountChildren(List<Integer> nums, boolean fin) throws Exception
    {
        for(int i=0; i<nums.size(); i++)
        {
            OBGroup child= (OBGroup)objectDict.get(String.format("place_child_%d", nums.get(i)));
            hiliteClothes(child,true,this);
            playAudioScene("DEMO",i,true);
            waitForSecs(0.3f);
            hiliteClothes(child,false,this);
            waitForSecs(0.3f);
        }
        waitForSecs(0.5f);
        if(!fin)
        {
            lockScreen();
            deleteControls("place_child_.*");
            unlockScreen();
        }
        waitForSecs(0.5f);
        nextScene();
    }

    public void demo1g() throws Exception
    {
        demoCountChildren(Arrays.asList(2,3,1),false);
    }

    public void demo1m() throws Exception
    {
        demoCountChildren(Arrays.asList(3,2,1,4),true);
    }


    public static void hiliteClothes(OBGroup child,boolean on, OC_SectionController controller)
    {
        controller.lockScreen();
        for (String key : child.objectDict.keySet())
        {
            if(key.startsWith("highlight"))
            {
                OBPath path = (OBPath)child.objectDict.get(key);
                if (on)
                {
                    path.setProperty("start_colour", path.fillColor());
                    path.setFillColor(OBUtils.highlightedColour(path.fillColor()));
                } else
                {
                    path.setFillColor((int)path.settings.get("start_colour"));
                }
            }
        }
        controller.unlockScreen();
    }
}
