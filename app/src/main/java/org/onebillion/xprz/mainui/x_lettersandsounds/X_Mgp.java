package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 04/07/16.
 */
public class X_Mgp extends XPRZ_SectionController
{
    static int PHASE_A1=0,
            PHASE_A2=1,
            PHASE_B1=2,
            PHASE_B2=3,
            PHASE_C=4;
    X_LetterBox box;
    XPRZ_Presenter presenter;
    int currentPhase, currentEvent;
    List<Map<String,Object>> eventsData;
    List<OBControl> eventTargets;
    OBPhoneme targetPhoneme;
    List<String> phaseDict;
    boolean useAltAudio, presenterShow;
    Map<String,List<PointF>> letterLocs;
    OBControl screenImage;


    public void prepare()
    {
        super.prepare();
        phaseDict = Arrays.asList("a","a","b","b","c");
        setStatus(STATUS_BUSY);


        loadFingers();
        loadEvent("master");
        eventTargets = new ArrayList<>();
        letterLocs = new ArrayMap<>();
        List<String> locNames = Arrays.asList("loc_a","loc_c_4","loc_c_5","loc_c_6");
        for(String loc : locNames)
        {
            String locNums = eventAttributes.get(loc);
            List<PointF> locs = new ArrayList<>();
            for(String num : locNums.split(","))
            {
                OBControl con = objectDict.get(String.format("loc_%s",num));
                locs.add(XPRZ_Generic.copyPoint(con.position()));
            }
            letterLocs.put(loc,locs);
        }

        presenter = XPRZ_Presenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("startloc", XPRZ_Generic.copyPoint(presenter.control.position()));
        presenter.control.setRight(0);

        box = new X_LetterBox((OBGroup)objectDict.get("box"),this);
        presenterShow = OBUtils.getBooleanValue(parameters.get("presenter")) || parameters.get("phasea") != null;
        if(presenterShow)
        {
            box.control.hide();
            presenter.control.show();
        }

        Map<String, OBPhoneme> componentsDict = OBUtils.LoadWordComponentsXML(true);

        String targAudString = parameters.get("targetletter");
        targetPhoneme = componentsDict.get(targAudString);
        useAltAudio = targetPhoneme.text.length() != 1;
        eventsData = new ArrayList<>();

        if(parameters.get("phasea")  != null)
        {
            List<OBPhoneme> letters = new ArrayList<>();
            for(String let : parameters.get("phasea").split(","))
                if(componentsDict.get(let) != null)
                    letters.add(componentsDict.get(let));

            if(letters.size() >5)
                letters = letters.subList(0,5);

            addEventPhase(PHASE_A1, phaseDict.get(PHASE_A1),OBUtils.randomlySortedArray(letters),null);

            for(int i=0; i<letters.size(); i++)
            {
                addEventPhase(PHASE_A2,String.format("%s%d", phaseDict.get(PHASE_A2), i+1), null, null);
            }
        }
        else
        {
            addEventPhase(PHASE_B1, phaseDict.get(PHASE_B1), Collections.singletonList(targetPhoneme), null);
        }

        String[] arr2 = parameters.get("phaseb").split(",");

        addEventPhase(PHASE_B2, String.format("%s0",phaseDict.get(PHASE_B2)),parameters.get("phasea")  != null ? Collections.singletonList(targetPhoneme) : null, null);


        int audioIndx = 1; int eventIndx = eventsData.size();
        for(int i=0; i<arr2.length; i++)
        {
            OBWord word = (OBWord)componentsDict.get(arr2[i]);
            addEventPhase(PHASE_B2,
                i == arr2.length-1 ? String.format("%slast",phaseDict.get(PHASE_B2)):
                String.format("%s%d",phaseDict.get(PHASE_B2),i+1), null, word);

            if(word != null)
            {
                if(i == 0)
                {
                    if(word.text.startsWith(targetPhoneme.text))
                        audioIndx = 0;
                    else if (word.text.endsWith(targetPhoneme.text))
                        audioIndx = 2;
                    else
                        audioIndx = 1;
                }
                else
                {
                    if((word.text.startsWith(targetPhoneme.text) && audioIndx != 0)
                            ||(word.text.endsWith(targetPhoneme.text) && audioIndx != 2))
                        audioIndx = 1;
                }
            }
        }

        eventsData.get(eventIndx).put("audioIndex",audioIndx);

        List<OBPhoneme> distractors = new ArrayList<>();
        List<String> parms = new ArrayList<>(Arrays.asList(parameters.get("phasec").split(",")));
        parms.remove(targetPhoneme.soundid);
        for(String disc : parms)
        {
            distractors.add(componentsDict.get(disc));
        }


        List<Integer> order = Arrays.asList(4,5,5,6,6);
        PointF lastLoc = new PointF(0, 0);
        for(int i=0; i<order.size(); i++)
        {
            List<OBPhoneme> disct = new ArrayList<>();
            int letCount = order.get(i);
            distractors = OBUtils.randomlySortedArray(distractors);
            disct.addAll(distractors.subList(0,letCount-1));
            disct.add(targetPhoneme);
            List<OBPhoneme> lets = OBUtils.randomlySortedArray(disct);
            List<PointF> locs = letterLocs.get(String.format("loc_c_%d",letCount));
            PointF targetLoc = locs.get(lets.indexOf(targetPhoneme));
            if(lastLoc.equals(targetLoc))
            {
                lastLoc = locs.get((lets.indexOf(targetPhoneme)+1)%lets.size());
            }
            else
            {
                lastLoc = targetLoc;
            }
            addEventPhase(PHASE_C,
                    i==order.size()-1 ? String.format("%slast",phaseDict.get(PHASE_C)): String.format("%s%d",phaseDict.get(PHASE_C),i+1),
                    lets,null);

            eventsData.get(eventsData.size()-1).put("fontmode",i);
        }

        currentEvent = 0;

        OBPath textbox = (OBPath)objectDict.get("textbox");
        textbox.setProperty("frameColour", textbox.strokeColor());
        textbox.setLineWidth(textbox.lineWidth()*2);
        setEvent(currentEvent);
    }



    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(currentPhase == PHASE_A1)
                {
                    demoStarta();
                }
                else
                {
                    demoStartb();
                }
            }

        });

    }

    public void finEvent() throws Exception
    {

            box.closeLid("lid_open");
            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(((Map<String,List<String>>)audioScenes.get("finale")).get(targetPhoneme.text.length() == 1 ? "DEMO" : "ALT.DEMO"),300), true);
            waitForSecs(0.3f);
            playTargetLetter();
            waitForSecs(0.3f);
           // (XPRZ_FatController*)FatController().completeEvent(;
            displayAward();
            exitEvent();

    }

    public void replayAudio()
    {
        if(status() != STATUS_BUSY)
        {

            final XPRZ_SectionController controller = this;
            if(currentPhase == PHASE_A1 || currentPhase == PHASE_A2)
            {
               OBUtils.runOnOtherThread(new OBUtils.RunLambda()
               {
                   @Override
                   public void run() throws Exception
                   {
                       statusTime = System.nanoTime();
                       playAudio(null);
                       presenter.speak(_replayAudio, controller);
                   }
               });

            }
            else if(currentPhase == PHASE_C)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        long time = System.nanoTime();
                        statusTime = time;
                        playAudio(null);
                        playAudioQueued(_replayAudio, true);
                        if (statusTime == time)
                            playTargetLetter();
                    }
                });
            }
            else
            {
                super.replayAudio();
            }
        }
    }

    public void exitEvent()
    {
        box.stopGemsGlowPulse(false);
        super.exitEvent();
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(currentPhase == PHASE_A1 || currentPhase == PHASE_B1)
            {

                if(finger(0,2,(List<OBControl>)(Object)Collections.singletonList(box.control),pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            playAudio(null);
                            box.stopGemsGlowPulse(true);
                            if(currentPhase == PHASE_A1)
                                presenter.onlyHeadLeft();

                            box.openLid("lid_open");
                            box.flyObjects(eventTargets,true,false,"letters_out");

                            if(currentPhase == PHASE_B1)
                                objectDict.get("textbox").show();

                            waitForSecs(0.2f);
                            if(currentPhase == PHASE_A1)
                                presenter.onlyHeadFront();

                            nextEvent();
                        }
                    });

                }
            }
            else
            {
                OBLabel targ1 = (OBLabel)finger(0,2,eventTargets,pt);

                if(targ1 == null && currentPhase == PHASE_B2 && finger(0,1,Collections.singletonList(objectDict.get("textbox")),pt) != null)
                     targ1 = (OBLabel)eventTargets.get(0);

                if(targ1 != null)
                {
                    final OBLabel targ = targ1;
                    final XPRZ_SectionController controller = this;
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            playAudio(null);
                            if(currentPhase == PHASE_A2)
                            {
                                targ.setColour(Color.RED);
                                OBPhoneme pho = (OBPhoneme)targ.propertyValue("audio");
                                pho.playAudio(controller,true);
                                waitForSecs(0.3f);
                                targ.setColour(Color.BLACK);
                                eventTargets.remove(targ);
                                box.flyObjects((List<OBControl>)(Object)Collections.singletonList(targ),false,false,"letters_home");
                                if(isLastInSet())
                                {
                                    demoPresenterOff();
                                }
                                nextEvent();
                            }
                            else if(currentPhase == PHASE_B2)
                            {
                                targ.setColour(Color.RED);
                                stutterAndShowImage();

                                if(isLastInSet())
                                {
                                    targ.setColour(Color.BLACK);
                                    waitForSecs(0.3f);
                                    demoFinb(targ);
                                }
                                else
                                {
                                    lockScreen();
                                            targ.setColour(Color.BLACK);
                                    if(screenImage != null)
                                        detachControl(screenImage);
                                    unlockScreen();
                                    waitForSecs(0.3f);
                                }

                                nextEvent();
                            }
                            else if(currentPhase == PHASE_C)
                            {
                                if((boolean)targ.propertyValue("correct"))
                                {
                                    lockScreen();
                                    for(OBControl con : eventTargets)
                                    {
                                        ((OBLabel)con).setColour( con == targ ? Color.RED : Color.GRAY);
                                    }
                                    unlockScreen();

                                    gotItRightBigTick(true);
                                    waitForSecs(1f);

                                    lockScreen();
                                    for(OBControl lab : eventTargets)
                                        ((OBLabel)lab).setColour( Color.BLACK);
                                    unlockScreen();
                                    box.flyObjects(eventTargets,false,false,"letters_home");


                                    nextEvent();
                                }
                                else
                                {
                                    targ.setColour(Color.GRAY);
                                    gotItWrongWithSfx();
                                    List<String> wrongAudio = getCurrentAudio("INCORRECT");
                                    long time = setStatus(STATUS_AWAITING_CLICK);
                                    waitSFX();
                                    targ.setColour(Color.BLACK);

                                    if(time == statusTime)
                                    {
                                        playAudioQueued(OBUtils.insertAudioInterval(wrongAudio,300), true);
                                        if(time == statusTime)
                                            playTargetLetter();
                                    }
                                }
                            }

                        }
                    });

                }
            }
        }

    }


    public void addEventPhase(int phase, String scene, List<OBPhoneme> letters, OBWord word)
    {
        Map<String,Object> dict = new ArrayMap<>();
        dict.put("phase",phase);
        dict.put("scene",scene);

        if(letters != null)
            dict.put("letters",letters);

        if(word  != null)
            dict.put("word",word);

        eventsData.add(dict);
    }

    public void startScene() throws Exception
    {
        if(currentPhase == PHASE_C)
            box.flyObjects(eventTargets,true,false,"letters_out");


        setReplayAudio(OBUtils.insertAudioInterval(getCurrentAudio("PROMPT.REPEAT"),300));

        final long time = setStatus(STATUS_AWAITING_CLICK);
        List<String> aud = getCurrentAudio("PROMPT");
        if(currentPhase == PHASE_A1 || currentPhase == PHASE_A2)
        {
            presenter.speak((List<Object>)(Object)aud,this);
            final XPRZ_SectionController controller = this;
            final List<String> remind = getCurrentAudio("REMINDER");
            OBUtils.runOnOtherThreadDelayed(4, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    if(!statusChanged(time) && remind != null)
                        presenter.speak((List<Object>)(Object)remind,controller);
                }
            });

        }
        else
        {
            final List<String> remind = getCurrentAudio("REMINDER");
            playAudioQueued(OBUtils.insertAudioInterval(aud,300),true);
            if(currentPhase == PHASE_C && statusTime == time)
                playTargetLetter();

            if(currentPhase == PHASE_B2)
                flashTextBox(time);

            OBUtils.runOnOtherThreadDelayed(4, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    if(statusTime == time)
                    {
                        if(remind != null)
                            playAudioQueued(OBUtils.insertAudioInterval(remind,300),true);

                        if(currentPhase == PHASE_C && statusTime == time)
                            playTargetLetter();
                    }
                }
            });
        }

        if(currentPhase == PHASE_A1 || currentPhase == PHASE_B1)
            box.startGemsGlowPulse(1);

    }

    public void setEvent(int eventNum)
    {
        Map<String,Object> data = eventsData.get(eventNum);
        currentPhase = (int)data.get("phase");

        if(currentPhase == PHASE_A1)
        {
            float fontSize = applyGraphicScale(140);
            Typeface font = OBUtils.standardTypeFace();
            List<OBPhoneme> letters = (List<OBPhoneme>)data.get("letters");
            List<PointF> locs = letterLocs.get("loc_a");
            prepareTargets(letters,locs,font,fontSize);
        }
        else if(currentPhase == PHASE_B1)
        {
            setSingleTarget(((List<OBPhoneme>)data.get("letters")).get(0));
        }
        else if(currentPhase == PHASE_B2)
        {
            if(data.get("letters") != null)
                setSingleTarget(((List<OBPhoneme>)data.get("letters")).get(0));

            if(data.get("word") != null)
            {
                OBWord word  = (OBWord)data.get("word");
                screenImage = loadImageWithName(word.ImageFileName(), new PointF(0,0),new RectF(bounds()));
                if(screenImage != null)
                {
                    screenImage.setScale(applyGraphicScale(1));
                    OBControl bg = objectDict.get("imagebox");
                    if (screenImage.width() > bg.width())
                    {
                        screenImage.setScale(screenImage.scale() * (bg.width() - 10.0f) / screenImage.width());
                    }
                    screenImage.setZPosition(5);
                    screenImage.setPosition(bg.position());
                    screenImage.hide();
                    screenImage.setProperty("audio", word);
                }
            }
            else
            {
                screenImage = null;
            }
        }
        else if(currentPhase == PHASE_C)
        {
            clearTargets();
            int fontmode = (int)data.get("fontmode");

            Typeface font = null;
            float fontSize = 0;

            switch(fontmode)
            {
                case 1:
                    font = OBUtils.standardTypeFace();
                    fontSize = applyGraphicScale(180);
                    break;
                case 2:
                    font  = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "fonts/Sen-Bold.otf");
                    fontSize = applyGraphicScale(140);
                    break;
                case 3:
                    font  = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "fonts/Sen-Bold.otf");
                    fontSize = applyGraphicScale(160);
                    break;
                default:
                    font = OBUtils.standardTypeFace();
                    fontSize = applyGraphicScale(140);
                    break;
            }

            List<OBPhoneme> letters = (List<OBPhoneme> )data.get("letters");
            List<PointF> locs = letterLocs.get(String.format("loc_c_%d",letters.size()));
            prepareTargets(letters,locs,font,fontSize);
        }



    }

    public void clearTargets()
    {
        for(OBControl targ : eventTargets)
        {
            detachControl(targ);
        }

        eventTargets.clear();
    }

    public void setSingleTarget(OBPhoneme letter)
    {
        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale(160);
        clearTargets();
        OBControl textBox = objectDict.get("textbox");
        PointF loc = XPRZ_Generic.copyPoint(textBox.position());
        loc.y-=OBUtils.getFontCapHeight(font,fontSize)/2.0 - OBUtils.getFontXHeight(font,fontSize)/2.0;
        prepareTargets(Collections.singletonList(letter), Collections.singletonList(loc), font, fontSize);

    }

    public void prepareTargets(List<OBPhoneme> letters, List<PointF> locs, Typeface font, float fontSize)
    {
        for(int i=0; i<letters.size(); i++)
        {
            if(locs.size() <= i)
                break;
            OBPhoneme pho = letters.get(i);
            OBLabel letterLabel = new OBLabel(pho.text,font,fontSize);
            letterLabel.setColour(Color.BLACK);
            letterLabel.setZPosition(20);
            letterLabel.setProperty("start_scale",letterLabel.scale());
            letterLabel.setProperty("audio",pho);
            letterLabel.setProperty("correct",targetPhoneme == pho);
            letterLabel.hide();
            letterLabel.setProperty("drop_loc",XPRZ_Generic.copyPoint(locs.get(i)));
            letterLabel.setPosition(OB_Maths.locationForRect(0.5f,0.5f,bounds()));
            attachControl(letterLabel);
            eventTargets.add(letterLabel);
            letterLabel.setReversedScreenMaskControl(box.mask);
        }

        OBControl frameCont = new OBControl();
        frameCont.setFrame(new RectF(bounds()));
        List<OBControl> targs = new ArrayList<>(eventTargets);

    }

    public void nextEvent() throws Exception
    {
        currentEvent++;
        if(currentEvent < eventsData.size())
        {
            lockScreen();
            setEvent(currentEvent);
            unlockScreen();

            if(!performSel("demo", (String)(eventsData.get(currentEvent).get("scene"))))
                startScene();
        }
        else
        {
            finEvent();
        }

    }

    public boolean isLastInSet()
    {
        if(currentEvent +1 < eventsData.size())
        {
            if((int)(eventsData.get(currentEvent+1)).get("phase") != currentPhase)
                return true;
            else
                return false;
        }
        return true;
    }

    public void stutterAndShowImage() throws Exception
    {

        for (int i = 0;i < 3;i++)
        {
            playTargetLetter();
            waitForSecs(0.6f);
        }
        if(screenImage!=null)
        {
            OBPhoneme pho = (OBPhoneme)screenImage.settings.get("audio");
            screenImage.show();
            playSfxAudio("image_on",true);
            waitForSecs(0.3f);
            pho.playAudio(this,true);
            waitForSecs(1f);
        }
        else
        {
            waitForSecs(0.5f);
        }
    }

    public List<String> getCurrentAudio(String audio)
    {
        String scene = (String)(eventsData.get(currentEvent)).get("scene");
        if(audioScenes.get(scene) == null)
        {
            scene = String.format("%sdefault", phaseDict.get(currentPhase));
        }

        List<String> arr = getAudioForScene(scene,String.format("ALT.%s", audio));
        if(useAltAudio && arr != null)
            return arr;
        else
            return getAudioForScene(scene,audio);

    }

    public void playTargetLetter() throws Exception
    {
        targetPhoneme.playAudio(this,true);
    }

    public void flashTextBox(final long time)
    {
       OBUtils.runOnOtherThread(new OBUtils.RunLambda()
       {
           @Override
           public void run() throws Exception
           {
               OBPath textbox = (OBPath)objectDict.get("textbox");
               int frameColour = (int)textbox.settings.get("frameColour");
               try
               {
                   waitForSecs(3f);
                   if(time == statusTime && !_aborting)
                   {
                       textbox.setStrokeColor(frameColour);
                   }
                   waitForSecs(0.5f);
                   for (int i = 0;i < 3;i++)
                   {
                       if(time == statusTime && !_aborting)
                       {
                           textbox.setStrokeColor(Color.RED);
                       }
                       waitForSecs(0.3f);
                       if(time == statusTime && !_aborting)
                       {
                           textbox.setStrokeColor(frameColour);
                       }
                       waitForSecs(0.3f);
                   }
                   if(time == statusTime && !_aborting)
                   {
                       flashTextBox(time);
                   }
                   else
                   {
                       textbox.setStrokeColor(frameColour);
                   }
               }
               catch (Exception exception)
               {
                   textbox.setStrokeColor(frameColour);
               }
           }
       });

    }

    public void slideBoxIn() throws Exception
    {
        PointF loc =  XPRZ_Generic.copyPoint(box.control.position());
        box.control.setRight(0);
        box.control.show();
        playSfxAudio("box_slide",false);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(loc, box.control)),0.5f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
    }


    public void demoStarta() throws Exception
    {
        PointF presenterLoc = (PointF)presenter.control.propertyValue("startloc");
        PointF loc = XPRZ_Generic.copyPoint(presenterLoc);
        loc.x = 0.5f*bounds().width();
        presenter.walk(loc);
        presenter.faceFrontReflected();
        slideBoxIn();
        waitForSecs(0.3f);
        presenter.moveHandfromIndex(0,2,0.2f);
        presenter.speak((List<Object>)(Object)getCurrentAudio("DEMO"),this);
        waitForSecs(0.3f);
        presenter.moveHandfromIndex(2,0,0.2f);
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getCurrentAudio("DEMO2"),this);
        presenter.faceFront();
        presenter.moveHandToEarController(this);
        waitForSecs(0.3f);
        presenter.speak((List<Object>)(Object)getCurrentAudio("DEMO3"), this);
        waitForSecs(0.3f);
        presenter.moveHandFromEarController(this);
        waitForSecs(0.3f);
        presenter.walk(presenterLoc);
        presenter.faceFrontReflected();
        presenter.moveHandfromIndex(0,2,0.2f);
        presenter.speak((List<Object>)(Object)getCurrentAudio("DEMO4"), this);
        presenter.moveHandfromIndex(2,0,0.2f);
        startScene();
    }

    public void demoPresenterOff() throws Exception
    {
        presenter.speak((List<Object>)(Object)getCurrentAudio("FINAL"),this);
        waitForSecs(0.3f);
        PointF loc = XPRZ_Generic.copyPoint(presenter.control.position());
        loc.x = bounds().width() *1.2f ;
        presenter.walk(loc);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,bounds()),0.5f,true);
        playAudioQueued(OBUtils.insertAudioInterval(getCurrentAudio("FINAL2"), 300), true);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoStartb() throws Exception
    {
        if(presenterShow)
        {
            PointF presenterLoc = (PointF)presenter.control.settings.get("startloc");
            PointF loc = XPRZ_Generic.copyPoint(presenterLoc);
            loc.x =  0.5f *bounds().width();
            presenter.walk(loc);
            presenter.faceFront();
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)getCurrentAudio("DEMO"), this);
            waitForSecs(0.3f);
            presenter.walk(presenterLoc);
            presenter.faceFront();
            presenter.speak((List<Object>)(Object)getCurrentAudio("DEMO2"), this);
            waitForSecs(0.3f);
            loc.x =  1.2f *bounds().width();
            presenter.walk(loc);
            presenter.control.hide();
            slideBoxIn();
        }

        List<String> arr = getCurrentAudio("DEMO3");
        playAudio(arr.get(0));
        waitAudio();
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.85f,0.8f,box.control.frame()), 0.5f, true);
        playAudio(arr.get(1));
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();

        startScene();
    }

    public void demob0() throws Exception
    {
        if(objectDict.get("textbox").hidden)
        {
            box.flyObjects(eventTargets,true,false,"letters_out");
            objectDict.get("textbox").show();
        }

        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.75f,1.05f,objectDict.get("textbox").frame()),0.5f,true);
        playAudioQueued(OBUtils.insertAudioInterval(getCurrentAudio("DEMO"),300),true);
        waitForSecs(0.5f);
        thePointer.hide();

        startScene();
    }

    public void demob1() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.75f,1.05f,objectDict.get("textbox").frame()),0.5f,true);

        List<String> aud = getCurrentAudio("DEMO");

        int audioIndx = (int)(eventsData.get(currentEvent)).get("audioIndex");
        playAudio(aud.get(audioIndx));
        waitAudio();
        waitForSecs(0.5f);

        playAudioQueued(OBUtils.insertAudioInterval(getCurrentAudio("DEMO2"),300),true);
        waitForSecs(0.5f);

        thePointer.hide();
        startScene();
    }

    public void democ1() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.85f,0.8f,box.control.frame()), 0.5f, true);
        playAudioQueued(OBUtils.insertAudioInterval(getCurrentAudio("DEMO"),300),true);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFinb(OBLabel label) throws Exception
    {
        playAudioQueued(OBUtils.insertAudioInterval(getCurrentAudio("FINAL"),300),true);
        waitForSecs(1f);
        lockScreen();
        label.setColour( Color.BLACK);
        detachControl(screenImage);
        objectDict.get("textbox").hide();
        unlockScreen();

        box.flyObjects(eventTargets,false,false,"letters_home");

    }

}