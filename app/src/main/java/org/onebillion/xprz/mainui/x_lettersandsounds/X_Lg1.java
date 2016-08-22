package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBReadingWord;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 19/07/16.
 */
public class X_Lg1 extends XPRZ_SectionController
{
    final int TARGET_WORD=0,
    TARGET_SYLLABLE=1,
    TARGET_LETTER=2;

    int maxTrials,rows,columns,currentEvent,currentPhase;
    OBPhoneme targetPhoneme;
    List<List<OBGroup>> controlsGrid;
    Map<String,Integer> failedTargets;
    List<OBPhoneme> distractors;
    List<OBLabel> targets;
    List<OBGroup> shutters;
    List<List<Map<String,Object>>> eventsData;
    XPRZ_Presenter presenter;

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

        presenter = XPRZ_Presenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("startloc", XPRZ_Generic.copyPoint(presenter.control.position()));
        presenter.control.setRight(0);
        presenter.control.show();

        failedTargets = new ArrayMap<>();

        Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);

        List<String> wordParams1a = getEventMode("mode1a");
        List<String> wordParams1b = getEventMode("mode1b");
        List<String> wordParams2 = getEventMode("mode2");

        distractors = new ArrayList<>();
        for(String distractorid : parameters.get("distractors").split(","))
            if(componentDict.get(distractorid) != null)
                 distractors.add(componentDict.get(distractorid));

        maxTrials = -1;

        int size = OBUtils.getIntValue(parameters.get("size"));

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
                mask.setFrame(shutter.frame());
                mask.setZPosition(30);
                mask.setBackgroundColor(Color.BLUE);
                mask.setProperty("startWidth",mask.width());
                mask.setAnchorPoint(0, 0.5f);
                shutter.setScreenMaskControl(mask);

                controlsGrid.get(i).add(shutter);

                OBControl bg = new OBControl();
                bg.setFrame(new RectF(frame));
               // bg.setPosition(shutter.position());
                bg.setBackgroundColor(bgColour);
                bg.setZPosition(1);
                attachControl(bg);
                bg.setScreenMaskControl(mask.copy());
                bg.hide();

                shutter.setProperty("bg",bg);
            }
        }

        eventsData  = new ArrayList<>();

        if(wordParams1a != null)
        {
            List<Map<String,Object>> arr = new ArrayList<>();

            for(String targId : wordParams1a)
                if(componentDict.get(targId) != null)
                    distractors.add(componentDict.get(targId));

            List<Map<String,Object>> events1 = new ArrayList<>();
            wordParams1a = OBUtils.randomlySortedArray(wordParams1a);
            for(int i=0; i<wordParams1a.size(); i++)
            {
                OBPhoneme phoneme = componentDict.get(wordParams1a.get(i));
                if(phoneme == null)
                    continue;

                for(int j=2; j<size; j++)
                    arr.add(eventForData(j,phoneme));


                for(int j=size-1; j<=size; j++)
                    events1.add(eventForData(j,phoneme));

            }

            arr.addAll(OBUtils.randomlySortedArray(events1));
            eventsData.add(arr);
        }

        if(wordParams1b != null)
        {
            maxTrials = OBUtils.getIntValue(parameters.get("trials"));

            for(String targId : wordParams1b)
                if(componentDict.get(targId) != null)
                    distractors.add(componentDict.get(targId));

            List<Map<String,Object>> events2 = new ArrayList<>();
            if(wordParams1a != null)
                wordParams1b.addAll(wordParams1a);
            for(int i=0; i<maxTrials; i++)
            {
                if(i%wordParams1b.size() == 0)
                    wordParams1b = OBUtils.randomlySortedArray(wordParams1b);

                OBPhoneme phoneme = componentDict.get(wordParams1b.get(i%wordParams1b.size()));
                if(phoneme == null)
                    continue;

                events2.add(eventForData(OB_Maths.randomInt(size-1,size), phoneme));
            }

            eventsData.add(OBUtils.randomlySortedArray(events2));
        }


        if(wordParams2 != null)
        {
            for(int i=0; i<wordParams2.size(); i++)
            {
                OBPhoneme phoneme = componentDict.get(wordParams2.get(i));
                if(phoneme == null)
                    continue;

                distractors.add(phoneme);
                List<Map<String,Object>> arr = new ArrayList<>();
                List<OBPhoneme> parts = null;
                if(phoneme.getClass() == OBSyllable.class)
                {
                    parts = ((OBSyllable)phoneme).phonemes;
                }
                else if(phoneme.getClass() == OBWord.class)
                {
                    parts = (List<OBPhoneme>)(Object)((OBWord)phoneme).syllables();
                }

                for(OBPhoneme pho : parts)
                {
                    arr.add(eventForData(size,pho));
                }
                arr.add(eventForData(size,phoneme));

                eventsData.add(arr);

            }

            List<OBPhoneme> extraDistractors = new ArrayList<>();

            for(OBPhoneme distract : distractors)
            {
                if(distract.getClass() == OBSyllable.class)
                {
                    for(OBPhoneme pho : ((OBSyllable)distract).phonemes)
                    extraDistractors.add(pho);
                }
                if(distract.getClass() == OBWord.class)
                {
                    for(OBPhoneme pho : ((OBWord)distract).syllables())
                    extraDistractors.add(pho);
                }
            }

            distractors.addAll(extraDistractors);
        }

        List<OBPhoneme> filteredDistractors = new ArrayList<>();
        for(OBPhoneme distractor : distractors)
        {
            boolean add = true;
            for(OBPhoneme pho : filteredDistractors)
            {
                if(pho.text.equalsIgnoreCase(distractor.text))
                {
                    add = false;
                    break;
                }
            }

            if(add)
                filteredDistractors.add(distractor);
        }

        distractors = filteredDistractors;
        shutters = new ArrayList<>();
        targets = new ArrayList<>();
        currentEvent=0;
        currentPhase=0;
        setEvent(currentEvent);
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

        float fontSize = 65;
        if(type == TARGET_LETTER)
            fontSize = 120;
        else if(type == TARGET_SYLLABLE)
            fontSize = 90;

        fontSize = applyGraphicScale(fontSize);
        Typeface font = OBUtils.standardTypeFace();
        int size = (int)data.get("size");

        List<OBPhoneme> eventPhonemes = new ArrayList<>();
        eventPhonemes.add(targetPhoneme);

        List<OBPhoneme> eventDistractors =null;
        List<OBPhoneme> possibleDistractors = new ArrayList<>();

        for(OBPhoneme pho : distractors)
            if(!pho.text.equalsIgnoreCase(targetPhoneme.text) && pho.getClass() == targetPhoneme.getClass())
                possibleDistractors.add(pho);

        eventDistractors = OBUtils.randomlySortedArray(possibleDistractors);
        eventDistractors = eventDistractors.subList(0,size);

        eventPhonemes.addAll(eventDistractors);
        eventPhonemes = OBUtils.randomlySortedArray(eventPhonemes);

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

            shutters.add(controlsGrid.get(r).get(c));
            OBPhoneme phon = eventPhonemes.get(i);
            OBLabel letterLabel = new OBLabel(phon.text,font,fontSize);
            letterLabel.setColour(Color.WHITE);
            letterLabel.setZPosition(10);
            OBControl shutter = controlsGrid.get(r).get(c);
            PointF loc = XPRZ_Generic.copyPoint(((OBControl)shutter.propertyValue("bg")).position());
            loc.y-=OBUtils.getFontCapHeight(font,fontSize)/2.0 - OBUtils.getFontXHeight(font,fontSize)/2.0;
            letterLabel.setPosition(loc);
            attachControl(letterLabel);
            letterLabel.setProperty("correct",phon == targetPhoneme);
            letterLabel.setProperty("bg",shutter.propertyValue("bg"));
            if(letterLabel.width() > shutter.width())
            {
                letterLabel.setScale((shutter.width())*0.9f/letterLabel.width());
            }

            if(letterLabel.scale() < minScale)
                minScale = letterLabel.scale();

            targets.add(letterLabel);
        }

        for(OBControl obj : targets)
            obj.setScale(minScale);

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
            OBLabel targ1 = null;
            for(OBLabel con : targets)
            {
                if(((OBControl)con.settings.get("bg")).frame().contains(pt.x, pt.y) && !con.hidden())
                {
                    targ1 = con;
                    break;
                }
            }

            final OBLabel targ = targ1;

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

    public void checkTarget(OBLabel targ) throws Exception
    {
        playAudio(null);
        targ.setColour(Color.RED);
        if((boolean)targ.settings.get("correct"))
        {
            gotItRight();
            nextEvent();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
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


    public void nextEvent() throws Exception
    {
        animateShutters(false);
        currentEvent++;
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
            eventsData.get(currentPhase).add(currentEvent+1,eventForData(size,(OBPhoneme)data.get("target")));
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
                if ((i * controlsGrid.get(i).size() + j) % 2 == 0)
                {
                    OBGroup shutter = controlsGrid.get(i).get(j);


                    OBGroup starCopy = (OBGroup) star.copy();
                    OBPath starPath = (OBPath) starCopy.objectDict.get("star");
                    starPath.setFillColor((int) shutter.propertyValue("colour"));
                    starPath.setStrokeColor(OBUtils.highlightedColour((int) shutter.propertyValue("colour")));

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
