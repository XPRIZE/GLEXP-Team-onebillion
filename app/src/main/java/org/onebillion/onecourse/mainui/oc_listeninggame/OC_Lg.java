package org.onebillion.onecourse.mainui.oc_listeninggame;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBSyllable;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 19/07/16.
 */
public class OC_Lg extends OC_SectionController
{
    final int TARGET_WORD=0,
            TARGET_SYLLABLE=1,
            TARGET_LETTER=2;

    int maxTrials,rows,columns,currentEvent,currentPhase;
    OBPhoneme targetPhoneme;
    List<List<OBGroup>> controlsGrid;
    Map<String,Integer> failedTargets;
    List<OBPhoneme> distractors;
    List<OBControl> targets;
    List<OBGroup> shutters;
    List<List<Map<String,Object>>> eventsData;
    public OBPresenter presenter;

    Map<String,List<String>> wrongMap;

    public ArrayList<String> getEventMode(String mode)
    {
        ArrayList<String> wordParams = null;
        if(parameters.get(mode) != null)
            wordParams = new ArrayList<String>(Arrays.asList(parameters.get(mode).split(",")));

        return wordParams;
    }

    public Map<String,Object> eventForData(int size, OBPhoneme target)
    {
        Map<String,Object> map = new ArrayMap<>();
        map.put("size",size);
        map.put("target",target);
        return map;
    }


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");

        wrongMap = new ArrayMap<>();

        if(parameters.get("pattern") != null)
        {
            setSceneXX(parameters.get("pattern"));
        }
        else
        {
            String type = "small";
            if(parameters.get("type") != null)
                type = parameters.get("type");

            String[] designs = eventAttributes.get(type).split(",");
            setSceneXX(designs[(OB_Maths.randomInt(0, designs.length-1))]);
        }

        failedTargets = new ArrayMap<>();
        distractors = new ArrayList<>();

        maxTrials = -1;
        rows = OBUtils.getIntValue(eventAttributes.get("rows"));
        columns = OBUtils.getIntValue(eventAttributes.get("columns"));

        int bgColour = OBUtils.colorFromRGBString(eventAttributes.get("bg_colour"));

        controlsGrid = new ArrayList<>();
        for(int i=0; i<rows; i++)
        {
            controlsGrid.add(new ArrayList<OBGroup>());
            for(int j=0; j<columns; j++)
            {
                OBGroup shutter = (OBGroup)objectDict.get(String.format("top_%d_%d",i,j));
                OBGroup strokes = (OBGroup)shutter.objectDict.get("strokes");
                RectF frame = new RectF(shutter.frame());
                frame.left = Math.round(frame.left);
                frame.right = Math.round(frame.right);
                frame.top = Math.round(frame.top);
                frame.bottom = Math.round(frame.bottom);
                shutter.setWidth(frame.width());
                shutter.setHeight(frame.height());
                shutter.setPosition(frame.centerX(),frame.centerY());

                if(strokes != null)
                {
                    strokes.recalculateFrameForPath(strokes.members);
                }

                shutter.setZPosition(20);

                OBPath shutterPath = (OBPath)shutter.objectDict.get("colour");
                shutter.setProperty("colour", shutterPath.fillColor());
                OBControl mask = new OBControl();
                mask.setFrame(frame);
                mask.setZPosition(30);
                mask.setBackgroundColor(Color.BLUE);
                mask.setProperty("startWidth",mask.width());
                mask.setAnchorPoint(0, 0.5f);
                shutter.setScreenMaskControl(mask);
                shutter.setHeight(shutter.height()+applyGraphicScale(2));
                shutter.setWidth(shutter.width()+applyGraphicScale(2));
                controlsGrid.get(i).add(shutter);

                OBControl bg = new OBControl();
                bg.setFrame(new RectF(frame));
                // bg.setPosition(shutter.position());
                bg.setBackgroundColor(bgColour);
                bg.setZPosition(1);
                attachControl(bg);
                //bg.setScreenMaskControl(mask.copy());
                bg.hide();

                shutter.setProperty("bg",bg);
            }
        }

        eventsData  = new ArrayList<>();
        shutters = new ArrayList<>();
        targets = new ArrayList<>();
        currentEvent=0;
        currentPhase=0;
        loadPresenter();
    }



    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                demo();
            }
        });

    }

    @Override
    public void fin()
    {
        if (shouldCollectMiscData())
        {
            if(wrongMap.size() > 0)
                collectMiscData("wrong", wrongMap);
        }
        super.fin();
    }

    public void doMainXX()
    {

    }

    public void setEvent(int eventNum)
    {
        shutters.clear();
        targets.clear();

        Map<String,Object> data = eventsData.get(currentPhase).get(eventNum);

        targetPhoneme = (OBPhoneme)data.get("target");

        int type = targetPhoneme.getClass() == OBPhoneme.class ? TARGET_LETTER : targetPhoneme.getClass() == OBSyllable.class ? TARGET_SYLLABLE : TARGET_WORD;
        int size = (int)data.get("size");


        List<OBPhoneme> eventPhonemes = new ArrayList<>();
        eventPhonemes.add(targetPhoneme);

        if(size > 1)
        {
            List<OBPhoneme> eventDistractors = null;
            List<OBPhoneme> possibleDistractors = new ArrayList<>();

            for (OBPhoneme pho : distractors)
                if (!pho.text.equalsIgnoreCase(targetPhoneme.text) && pho.getClass() == targetPhoneme.getClass())

                    possibleDistractors.add(pho);

            eventDistractors = OBUtils.randomlySortedArray(possibleDistractors);
            eventDistractors = eventDistractors.subList(0, size - 1);

            eventPhonemes.addAll(eventDistractors);
            eventPhonemes = OBUtils.randomlySortedArray(eventPhonemes);
        }

        int r = OB_Maths.randomInt(0,rows-1), c = OB_Maths.randomInt(0, columns-1);
        float minScale = 2;
        for(int i=0; i<eventPhonemes.size(); i++)
        {
            while(shutters.size()>0 && shutters.contains((controlsGrid.get(r).get(c))))
            {
                if(OB_Maths.randomInt(0, 1) == 0)
                {
                    if(OB_Maths.randomInt(r==0 ? 1 : 0, r==rows-1 ? 0 : 1) == 0)
                        r--;
                    else
                        r++;
                }
                else
                {
                    if(OB_Maths.randomInt(c==0 ? 1 : 0, c==columns-1 ? 0 : 1) == 0)
                        c--;
                    else
                        c++;
                }
            }

            OBGroup shutter = controlsGrid.get(r).get(c);
            shutters.add(shutter);
            OBPhoneme phon = eventPhonemes.get(i);
            PointF loc = OC_Generic.copyPoint(((OBControl)shutter.propertyValue("bg")).position());
            OBControl control = loadTargetForPhoneme(phon,(OBControl)shutter.propertyValue("bg"), type, data);
            control.setZPosition(10);

            attachControl(control);
            control.setProperty("correct",phon == targetPhoneme);
            control.setProperty("shutter",shutter);
            control.setProperty("bg",shutter.propertyValue("bg"));
            if(fitControls())
            {
                if (control.width() > shutter.width()) {
                    control.setScale((shutter.width()) * 0.9f / control.width());
                }

                if (control.scale() < minScale)
                    minScale = control.scale();
            }

            targets.add(control);
        }

        if(keepTargetsSameSize())
            for(OBControl obj : targets)
                obj.setScale(minScale);

     //   setReplayAudio(Arrays.asList((Object)targetPhoneme.soundid));

    }

    public void replayAudio()
    {
        if(status() != STATUS_BUSY)
        {
            try
            {
                statusTime = System.nanoTime();
                targetPhoneme.playAudio(this, false);
            }catch (Exception e)
            {

            }
        }
    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            OBControl targ1 = null;
            for(OBControl con : targets)
            {
                if(((OBControl)con.settings.get("bg")).frame().contains(pt.x, pt.y) && !con.hidden())
                {
                    targ1 = con;
                    break;
                }
            }

            final OBControl targ = targ1;

            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget(targ);
                    }
                });

            }
        }
    }

    public void checkTarget(OBControl targ) throws Exception
    {
        playAudio(null);
        highlightTarget(targ);
        if((boolean)targ.settings.get("correct"))
        {
            gotItRight();
            nextEvent(targ);
        }
        else
        {
            if(shouldCollectMiscData() && targetPhoneme.text != null)
            {
                if(!wrongMap.containsKey(targetPhoneme.text))
                    wrongMap.put(targetPhoneme.text, new ArrayList<String>());
                OBPhoneme pho = (OBPhoneme)targ.propertyValue("phoneme");
                wrongMap.get(targetPhoneme.text).add(pho.text);
            }
            gotItWrongWithSfx();
            waitSFX();
            lowlightTarget(targ);
            lockScreen();
            for(OBControl con : targets)
            {
                if(!(boolean)con.settings.get("correct"))
                    con.hide();
            }
            insertEasierEventForCurrent();
            unlockScreen();

            setStatus(STATUS_AWAITING_CLICK);
            waitForSecs(0.3f);
            targetPhoneme.playAudio(this,false);
        }
    }


    public OBControl loadTargetForPhoneme(OBPhoneme phon, OBControl bg, int type, Map<String,Object> data)
    {
        Typeface font  = OBUtils.standardTypeFace();
        float fontSize = fontSizeForType(type);
        OBLabel letterLabel = new OBLabel(phon.text,font,fontSize);
        letterLabel.setColour(Color.WHITE);
        PointF loc = OBMisc.copyPoint(bg.position());
        if(!phon.text.substring(0,1).toUpperCase().equals(phon.text.substring(0,1)))
            loc.y -= OBUtils.getFontCapHeight(font, fontSize) / 2.0 - OBUtils.getFontXHeight(font, fontSize) / 2.0;

        letterLabel.setPosition(loc);
        return letterLabel;
    }

    public float fontSizeForType(int type)
    {
        float fontSize = 65;
        if(type == TARGET_LETTER)
            fontSize = 120;
        else if(type == TARGET_SYLLABLE)
            fontSize = 90;
        return applyGraphicScale(fontSize);
    }

    public boolean keepTargetsSameSize()
    {
        return true;
    }

    public void highlightTarget(OBControl control)
    {
        if(control.getClass() == OBLabel.class)
        {
            ((OBLabel)control).setColour(Color.RED);
        }
    }

    public void lowlightTarget(OBControl control)
    {

    }


    public void setMaxTrials(int trials)
    {
        maxTrials = trials;
    }

    public Map<String,Object> eventDataSize(int size,OBPhoneme target)
    {
        Map<String,Object> map = new ArrayMap<>();
        map.put("size",size);
        map.put("target", target);
        return map;
    }

    public void addToEventData(List<Map<String,Object>> eventData)
    {
        eventsData.add(eventData);
    }

    public void addToDistrators(List<OBPhoneme> dist)
    {
        distractors.addAll(dist);
    }

    public void setupEventForLearning(List<OBPhoneme> phonemeList, int size)
    {
        List<Map<String,Object>> events1 = new ArrayList<>();
        List<Map<String,Object>> events2 = new ArrayList<>();

        for(OBPhoneme phoneme : phonemeList)
            distractors.add(phoneme);


        phonemeList = OBUtils.randomlySortedArray(phonemeList);
        for(int i=0; i<phonemeList.size(); i++)
        {
            for(int j=2; j<size; j++)
                events1.add(eventDataSize(j,phonemeList.get(i)));

            for(int j=size-1; j<=size; j++)
                events2.add(eventDataSize(j,phonemeList.get(i)));
        }
        events1.addAll(OBUtils.randomlySortedArray(events2));
        addToEventData(events1);
    }

    public void setupEventForPractice(List<OBPhoneme> phonemeList,int size)
    {
        for(OBPhoneme phoneme : phonemeList)
            distractors.add(phoneme);

        List<Map<String,Object>> events1 = new ArrayList<>();
        for(int i=0; i<maxTrials; i++)
        {
            if(i%phonemeList.size() == 0)
                phonemeList = OBUtils.randomlySortedArray(phonemeList);

            events1.add(eventDataSize(OB_Maths.randomInt(size-1, size),
                    phonemeList.get(i%phonemeList.size())));
        }

        addToEventData(OBUtils.randomlySortedArray(events1));
    }

    public void finalisePrepare()
    {
        List<OBPhoneme> filteredDistractors = new ArrayList<>();
        for(OBPhoneme distractor : distractors)
        {
            boolean add = true;
            for(OBPhoneme pho : filteredDistractors)
            {
                if(pho.text.equals(distractor.text))
                {
                    add = false;
                    break;
                }
            }

            if(add)
                filteredDistractors.add(distractor);
        }

        distractors = filteredDistractors;

        setEvent(currentEvent);
    }


    public void startScene() throws Exception
    {
        targetPhoneme.playAudio(this,false);
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitAudio();
        waitForSecs(4f);
        if(statusTime == time)
        {
            if(currentPhase ==0 && currentEvent == 0)
            {
                List<String> audio = getAudioForScene("c","REMINDER");
                playAudio(audio.get(0));
                waitAudio();
                waitForSecs(0.3f);
            }

            if(statusTime == time)
                targetPhoneme.playAudio(this, false);
        }

    }


    public void animateShutters(boolean open) throws Exception
    {
        animateShutters(open, 0.3f,open ? "shutter_open" : "shutter_close");
        waitSFX();
    }

    public void animateShutters(final boolean open,float duration, String audio) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        lockScreen();
        for(final OBGroup con : shutters)
        {

            if(!open)
            {
                con.show();
            }
            else
            {
                if(con.propertyValue("bg") != null)
                    ((OBControl)con.propertyValue("bg")).show();
            }


            // anims.add(OBAnim.propertyAnim("width",open ? 1 : (float) con.maskControl.propertyValue("startWidth"), con.maskControl));
            final float widthDif = (float) con.maskControl.propertyValue("startWidth") - 1.0f;


            anims.add(new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    con.maskControl.setWidth(open ? 1.0f + (1.0f-frac) * widthDif :1.0f + (frac*widthDif) );
                    con.invalidate();
                }
            });



        }
        unlockScreen();
        playSfxAudio(audio,false);
        OBAnimationGroup.runAnims(anims,duration,true,open ? OBAnim.ANIM_EASE_IN : OBAnim.ANIM_EASE_OUT,this);
        lockScreen();
        for(OBControl con : shutters)
        {
            if(open)
            {
                con.hide();
            }
            else
            {
                if (con.propertyValue("bg") != null)
                    ((OBControl) con.propertyValue("bg")).hide();
            }
        }
        unlockScreen();

    }


    public void nextEvent(OBControl lastControl) throws Exception
    {
        animateShutters(false);
        currentEvent++;
        if(lastControl != null)
            lowlightTarget(lastControl);
        if(currentEvent >= eventsData.get(currentPhase).size())
        {
            if(currentPhase+1 <  eventsData.size())
            {
                failedTargets.clear();
                currentPhase++;
                currentEvent = 0;
            }
        }

        if(currentPhase == eventsData.size()-1 && ((maxTrials > 0 && currentEvent >= maxTrials) || currentEvent >= eventsData.get(currentPhase).size()))
        {
            finalAnimation();
            fin();
        }
        else
        {
            lockScreen();
            for(OBControl con : targets)
                detachControl(con);

            setEvent(currentEvent);
            unlockScreen();
            animateShutters(true);
            startScene();
        }
    }

    void loadPresenter()
    {
        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("startloc", OC_Generic.copyPoint(presenter.control.position()));
        presenter.control.setRight(0);
        presenter.control.show();

    }

    public void insertEasierEventForCurrent()
    {
        int fails = 1;
        if(failedTargets.get(targetPhoneme.text) != null)
        {
            fails = (int)failedTargets.get(targetPhoneme.text);
            fails++;
        }
        failedTargets.put(targetPhoneme.text,fails);
        if(fails <= 3)
        {
            Map<String,Object> data = eventsData.get(currentPhase).get(currentEvent);
            int size = (int)data.get("size")-1;
            if(size < 1)
                size = 1;
            Map<String,Object> insertData = eventForData(size,(OBPhoneme)data.get("target"));
            insertData.put("retry",true);
            eventsData.get(currentPhase).add(currentEvent+1,insertData);
        }
        else
        {
            List<Map<String,Object>> eventsDelete = new ArrayList<>();
            for(int i = currentEvent+1; i< eventsData.get(currentPhase).size(); i++)
            {
                Map<String,Object> dat = eventsData.get(currentPhase).get(i);
                if(dat.get("target") == targetPhoneme)
                    eventsDelete.add(dat);
            }
            eventsData.get(currentPhase).removeAll(eventsDelete);
        }

    }


    public void finalAnimation() throws Exception
    {
        waitForSecs(0.5f);
        shutters.clear();
        List<OBControl> stars = new ArrayList<>();
        lockScreen();
        OBGroup star = loadVectorWithName("card_star", new PointF(0, 0), new RectF(bounds()));


        for (OBControl con : targets)
            detachControl(con);

        for (int i = 0; i < controlsGrid.size(); i++)
        {
            for (int j = 0; j < controlsGrid.get(i).size(); j++)
            {
                if ((i+j)%2 != 0)
                {
                    OBGroup shutter = controlsGrid.get(i).get(j);


                    OBGroup starCopy = (OBGroup) star.copy();
                    OBPath starPath = (OBPath) starCopy.objectDict.get("star");
                    if(shutter.propertyValue("colour") != null)
                    {
                        starPath.setFillColor((int) shutter.propertyValue("colour"));
                        starPath.setStrokeColor(OBUtils.highlightedColour((int) shutter.propertyValue("colour")));
                    }

                    attachControl(starCopy);

                    starCopy.setPosition(((OBControl) shutter.propertyValue("bg")).position());
                    starCopy.setZPosition(2);
                    starCopy.setScale(0.7f * shutter.height() / starCopy.height());
                    starCopy.setRasterScale(starCopy.scale());
                    starCopy.setOpacity(0.5f);

                    shutters.add(shutter);
                    shutter.setProperty("bg", null);
                    stars.add(starCopy);
                }
            }
        }
        detachControl(star);
        unlockScreen();
        animateStarsTwinkle(stars);
        animateShutters(true, 0.5f, "stars");

        for (int i = 0; i < controlsGrid.size(); i++)
        {
            for (int j = 0; j < controlsGrid.get(i).size(); j++)
            {
                controlsGrid.get(i).get(j).maskControl = null;
            }
        }
        waitForSecs(1f);
    }

    public void animateStarsTwinkle(final List<OBControl> stars)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                startsAnimationLoop(stars);
            }
        });

    }

    public void startsAnimationLoop(List<OBControl> stars) throws Exception
    {
        while(!_aborting)
        {
            List<OBAnim> anims = new ArrayList<>();

            for(OBControl star : stars)
                anims.add(OBAnim.opacityAnim(0.1f+0.8f/OB_Maths.randomInt(1, 10),star));

            OBAnimationGroup.runAnims(anims,0.3,true,OBAnim.ANIM_LINEAR,this);
        }
    }

    public boolean fitControls()
    {
        return true;
    }

    public void demo() throws Exception
    {
        if(parameters.get("presenter").equalsIgnoreCase("true"))
        {
            PointF presenterLoc = (PointF)presenter.control.settings.get("startloc");
            presenter.walk(presenterLoc);
            presenter.faceFront();
            List<String>  audio = getAudioForScene("a", "DEMO");
            presenter.speak((List<Object>)(Object)Collections.singletonList(audio.get(0)),this);
            waitForSecs(0.3f);
            presenter.moveHandToEarController(this);
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)Collections.singletonList(audio.get(1)),this);
            waitForSecs(0.3f);
            presenter.moveHandFromEarController(this);
            waitForSecs(0.3f);
            presenterLoc = new PointF(0.85f*this.bounds().width(), presenterLoc.y);
            presenter.walk(presenterLoc);
            presenter.faceFront();
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)Collections.singletonList(audio.get(2)),this);
            waitForSecs(0.3f);
            presenterLoc = new PointF(1.2f*this.bounds().width(), presenterLoc.y);
            presenter.walk(presenterLoc);
        }

        List<String> audio2 = getAudioForScene("b", "DEMO");

        loadPointer(POINTER_MIDDLE);
        movePointerToPoint(OB_Maths.locationForRect(0.95f,0.85f,this.bounds()),0.3f,true);
        playAudio(audio2.get(0));
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f,1.1f), MainViewController().topRightButton.frame),0.5f,true);
        playAudio(audio2.get(1));
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        animateShutters(true);
        startScene();
    }


}
