package org.onebillion.onecourse.mainui.oc_makewordsyl;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.oc_meetletter.OC_LetterBox;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBSyllable;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 29/06/16.
 */
public class OC_MakeSylWord extends OC_SectionController
{
    static int MODE_WORD = 0,
            MODE_SYLLABLE = 1;
    OC_LetterBox box;
    List<OBLabel> parts;
    List<Map<String,Object>> wordEventDicts;
    int currentMode, currentWord, currentPart, currentRepeat;
    int totalRepeats;
    OBLabel fullWordLabel;
    boolean showDemo, showPicture, useSyllables;
    OBControl screenImage;
    OBPresenter presenter;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");

        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("startloc", OC_Generic.copyPoint(presenter.control.position()));
        presenter.control.setRight(0);

        showDemo = OBUtils.getBooleanValue(parameters.get("demo"));
        showPicture = OBUtils.getBooleanValue(parameters.get("picture"));
        useSyllables = OBUtils.getBooleanValue(parameters.get("syllables"));
        box = new OC_LetterBox((OBGroup)objectDict.get("box"),this);
        if(showDemo)
        {
            box.control.hide();
            presenter.control.show();
        }

        wordEventDicts = new ArrayList<>();
        parts = new ArrayList<>();
        totalRepeats = OBUtils.getIntValue(parameters.get("repeats"));
        Map<String, OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);

        List<OBPhoneme> phonemes = new ArrayList<>();
        String[] wordList = parameters.get("words").split(",");
        for(String phoid :wordList)
        {
            if(componentDict.get(phoid) != null)
                phonemes.add(componentDict.get(phoid));
        }


        for(OBPhoneme phon : phonemes)
        {
            Map<String,Object> dict = new ArrayMap<>();

            if(phon.getClass() == OBWord.class)
            {
                OBWord word = (OBWord)phon;

                dict.put("parts", useSyllables ? word.syllables() : word.phonemes());

                dict.put("main", word);
                dict.put("mode", MODE_WORD);
                dict.put("image", word.ImageFileName());

            }
            else if(phon.getClass() == OBSyllable.class)
            {
                OBSyllable syl = (OBSyllable)phon;
                dict.put("parts", syl.phonemes);
                dict.put("main", syl);
                dict.put("mode", MODE_SYLLABLE);
            }
            else
            {
                continue;
            }


            wordEventDicts.add(dict);
        }

        currentWord = 0;
        /*
        OBGroup button = (OBGroup)objectDict.get("button");
        button.show();
        button.highlight();
        button.lowlight();
        button.hide();
*/
        setWordScene(currentWord);

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {

                demoStart();

            }
        });

    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
    }

    public void setWordScene(int index)
    {
        currentPart = -1;
        currentRepeat = 0;
        parts.clear();

        Map<String,Object> curWord = wordEventDicts.get(index);

        currentMode = (int)curWord.get("mode");
        List<OBPhoneme> textParts = (List<OBPhoneme>)curWord.get("parts");
        Typeface font = OBUtils.standardTypeFace();
        float fontSize = applyGraphicScale((currentMode == MODE_WORD) ? 120 : 140);

        OBControl textBox = objectDict.get("textbox");

        OBPhoneme main = (OBPhoneme)curWord.get("main");

        fullWordLabel = new OBLabel(main.text ,font,fontSize);
        fullWordLabel.setColour(Color.BLACK);
        fullWordLabel.setZPosition(10);
        fullWordLabel.setPosition(textBox.position());
        fullWordLabel.hide();
        attachControl(fullWordLabel);
        if(fullWordLabel.width()>textBox.width())
            fullWordLabel.setScale(1.0f - ((fullWordLabel.width()-textBox.width())*1.0f/fullWordLabel.width()));

        fullWordLabel.setProperty("audio",main);

        fullWordLabel.setPosition(OB_Maths.locationForRect(0.5f,0.4f,this.bounds()));


        int searchStart =0;
        for(int i=0; i<textParts.size(); i++)
        {
            OBPhoneme textPart = textParts.get(i);

            int rangeStart = main.text.indexOf(textPart.text,searchStart);
            if(rangeStart != -1)
            {
                RectF bb = OBUtils.getBoundsForSelectionInLabel(rangeStart,rangeStart+textPart.text.length(),fullWordLabel);

                float left = bb.left;

                OBLabel partLabel = new OBLabel(textPart.text,font,fontSize);
                partLabel.setColour(Color.BLACK);
                partLabel.setPosition(fullWordLabel.position());
                partLabel.setScale(fullWordLabel.scale());
                partLabel.setZPosition(fullWordLabel.zPosition() + 1);
                partLabel.setLeft(left);
                partLabel.setProperty("start_scale", partLabel.scale());
                partLabel.setProperty("word_loc", OC_Generic.copyPoint(partLabel.position()));
                partLabel.setProperty("audio", textPart);
                parts.add(partLabel);
                partLabel.hide();
                attachControl(partLabel);
                searchStart += textPart.text.length();
                partLabel.setReversedScreenMaskControl(box.mask);
            }
        }

        float gap = 0.58f * fontSize * fullWordLabel.scale();
        float thisWidth = fullWordLabel.width() + (parts.size() - 1) * gap;
        float left = fullWordLabel.position().x - thisWidth / 2;
        for (int i = 0;i < parts.size();i++)
        {
            OBLabel label = parts.get(i);
            label.setLeft(left + label.left() - fullWordLabel.left()  + gap * i);
            label.setProperty("drop_loc", OC_Generic.copyPoint(label.position()));
        }
        OBControl frameCont = new OBControl();
        frameCont.setFrame(new RectF(this.bounds()));
        if(currentMode == MODE_WORD && showPicture)
        {
            screenImage = loadImageWithName(((OBWord)main).ImageFileName(),new PointF(0,0), new RectF(this.bounds()));
            screenImage.setScale(applyGraphicScale(1));
            OBControl bg = objectDict.get("imagebox");
            if(screenImage.width() > bg.width())
            {
                screenImage.setScale(screenImage.scale() *(bg.width()-10.0f)/screenImage.width());
            }
            screenImage.setZPosition(5);
            screenImage.setPosition( bg.position());
            screenImage.hide();
        }
    }

    public void doMainXX()
    {

    }


    public void finEvent() throws Exception
    {

            box.closeLid("lid_open");
            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(audioForScene("finale", "DEMO"), 300), true);
            MainActivity.mainActivity.fatController.completeEvent(this);

    }

    public void replayAudio()
    {
        if(this.status() != STATUS_BUSY)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {

                @Override
                public void run() throws Exception
                {
                    playAudioQueued(_replayAudio);
                }
            });

        }

    }

    @Override
    public void cleanUp()
    {
        box.stopGemsGlowPulse(false);
        super.cleanUp();
    }


    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {

            if(currentPart == -1)
            {
                if(finger(0,2, (List<OBControl>)(Object)Collections.singletonList(box.control), pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {

                        @Override
                        public void run() throws Exception
                        {

                            playAudio(null);
                            box.stopGemsGlowPulse(true);
                            if (currentWord == 0)
                            {
                                box.openLid("lid_open");
                            }
                            box.flyObjects((List<OBControl>)(Object)parts,true,false, "letters_out");
                            waitForSecs(0.2f);
                            nextWordPart();
                        }
                    });
                }
            }
            else
            {
                final OBControl button = objectDict.get("button");
                final OC_SectionController controller = this;
                if(finger(0,2, Collections.singletonList(button), pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {

                        @Override
                        public void run() throws Exception
                        {
                            playAudio(null);
                            OBLabel label = currentLabel();
                            lockScreen();
                            label.setColour(Color.BLUE);
                            button.setOpacity(1);
                            button.highlight();
                            unlockScreen();

                            OBPhoneme sound = (OBPhoneme)label.propertyValue("audio");
                            sound.playAudio(controller,true);

                            waitForSecs(0.3f);
                            lockScreen();
                            label.setColour(Color.BLACK);
                            button.lowlight();
                            button.hide();
                            unlockScreen();
                            waitForSecs(0.3f);
                            nextWordPart();
                        }
                    });
                }
            }
        }
    }


    public void startScene() throws Exception
    {
        if(currentPart == -1)
        {
            List<Object> repeatAudio = getSceneAudio("BOX.REPEAT");
            if(repeatAudio == null)
            {
                Map<String,List> eventd = (Map<String, List>) audioScenes.get("default");
                repeatAudio = eventd.get("BOX.REPEAT");
            }
            setReplayAudio(repeatAudio);
            long time = setStatus(STATUS_AWAITING_CLICK);

            playAudioQueued(getSceneAudio("BOX"), true);

            box.startGemsGlowPulse(1);

        }
        else
        {
            String scene = null;
            if(currentPart == 0)
            {
                scene = "BUTTON";
            }
            else
            {
                scene = String.format("BUTTON%d",currentPart+1);
            }
            List<Object> repeatAudio = scene == null ? null : getSceneAudio(String.format("%s.REPEAT",scene));
            if(repeatAudio == null)
            {
                Map<String,List> eventd = (Map<String, List>) audioScenes.get("default");
                repeatAudio = eventd.get("BUTTON.REPEAT");
            }

            setReplayAudio(repeatAudio);
            long time = setStatus(STATUS_AWAITING_CLICK);
            playAudioQueued(getSceneAudio(scene),true);
            startButtonFlash(time);
        }
    }

    public void nextWordPart() throws Exception
    {
        currentPart++;
        if(currentPart <= parts.size())
        {
            if(currentPart == parts.size())
            {
                playAudioQueued(getSceneAudio("DEMO3"),true);
                waitForSecs(0.3f);
                animateLabelsJoin();
                waitForSecs(0.3f);
            }
            else if(currentPart == 0 && currentRepeat > 0)
            {
                playAudioQueued(getSceneAudio("DEMO"),true);
                waitForSecs(0.3f);
                animateLabelsSplit();
                waitForSecs(0.3f);
            }

            showButtonForLabel(currentLabel());
            waitForSecs(0.15f);

            if(currentWord == 0 && currentPart == 0 && currentRepeat == 0)
                demoPointButton();

            startScene();
        }
        else
        {
            if(currentWord == 0 && currentRepeat == 0)
                demoFinLabel();

            if(currentRepeat+1 >= totalRepeats)
            {
                if(currentMode == MODE_WORD)
                {
                    if(showPicture)
                    {
                        waitForSecs(0.5f);
                        screenImage.show();
                        playSfxAudio("image_on",true);
                        waitForSecs(0.3f);
                        OBPhoneme pho = (OBPhoneme)fullWordLabel.propertyValue("audio");
                        pho.playAudio(this,true);
                        waitForSecs(1.5f);
                    }
                    else
                    {
                        waitForSecs(1.5f);
                    }

                }
                else
                {
                    if(getSceneAudio("FINAL") == null)
                        playAudioQueued(getSceneAudio("FINAL2"), true);
                    waitForSecs(1f);
                }

                lockScreen();
                fullWordLabel.hide();
                for(OBControl con : parts)
                    con.show();

                if(showPicture)
                    screenImage.hide();
                unlockScreen();

                box.flyObjects((List<OBControl>)(Object)parts,false, true,"letters_home");
                nextWord();
            }
            else
            {
                currentRepeat++;
                currentPart = -1;
                nextWordPart();
            }
        }
    }

    public void nextWord() throws Exception
    {
        lockScreen();
        for(OBControl con : parts)
            detachControl(con);
        parts.clear();
        detachControl(fullWordLabel);
        unlockScreen();
        currentWord++;
        if(currentWord >= wordEventDicts.size())
        {
            finEvent();
        }
        else
        {
            lockScreen();
            setWordScene(currentWord);
            unlockScreen();
            demoLetterBox();
            box.flyObjects((List<OBControl>)(Object)parts,true,false,"letters_out");
            waitForSecs(0.2f);
            nextWordPart();
        }
    }

    public OBLabel currentLabel()
    {
        if(currentPart < parts.size())
        {
            return parts.get(currentPart);
        }
        else
        {
            return fullWordLabel;
        }
    }


    public void showButtonForLabel(OBLabel label) throws Exception
    {
        lockScreen();
        OBControl button = objectDict.get("button");
        button.setPosition(label.position());
        button.setTop(label.bottom() + applyGraphicScale(5));
        button.lowlight();
        button.show();

        unlockScreen();
        playSfxAudio("button_on",true);

    }

    public void animateLabelsJoin() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl part : parts)
        {
            anims.add(OBAnim.moveAnim((PointF)part.propertyValue("word_loc"), part));
        }

        playSfxAudio("blend",false);
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        lockScreen();
        fullWordLabel.show();
        for(OBControl part : parts)
            part.hide();

        unlockScreen();
        waitSFX();
    }

    public void animateLabelsSplit() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl part : parts)
        {
            anims.add(OBAnim.moveAnim((PointF)part.propertyValue("drop_loc"), part));
        }

        playSfxAudio("segment",false);
        lockScreen();
        for(OBControl part : parts)
            part.show();

        fullWordLabel.hide();
        unlockScreen();

        OBAnimationGroup.runAnims(anims,0.5,true, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
    }

    public List<Object> getSceneAudio(String audio)
    {
        if(audio == null)
            return null;
        String currentScene = String.format("%d%d",currentWord + 1, currentRepeat + 1);


        List<String> list = audioForScene(currentScene,audio);
        if(list == null)
            return null;

        return OBUtils.insertAudioInterval(list, 300);
    }

    public List<String> audioForScene(String scene, String audio)
    {
        if(scene == null || audio == null)
            return null;
        if(audioScenes.get(scene) != null && ((Map<String,Object>)audioScenes.get(scene)).get(audio) != null)
        {
            List<String> arr = (List<String>)((Map<String,Object>)audioScenes.get(scene)).get(String.format("ALT.%s",audio));
            if(currentMode == MODE_WORD && arr != null)
                return arr;
            else
                return (List<String>)((Map<String,Object>)audioScenes.get(scene)).get(audio);
        }
        return null;
    }

    public void startButtonFlash(final long time) throws Exception
    {

        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                waitForSecs(3f);
                while(time == statusTime && !_aborting)
                {
                    flashButton(time);
                    waitForSecs(4f);
                }
            }
        });

    }

    public void flashButton(long time)
    {
        OBGroup button = (OBGroup)objectDict.get("button");
        try
        {
            for (int i = 0;i < 2;i++)
            {
                if(time == statusTime && !_aborting)
                {
                    button.setOpacity(0.2f);
                }
                waitForSecs(0.3f);
                if(time == statusTime && !_aborting)
                {
                    button.setOpacity(1);
                }
                waitForSecs(0.3f);
            }

        }
        catch (Exception exception)
        {
            button.setOpacity(1);
        }

    }


    public void demoStart() throws Exception
    {
        if(showDemo)
        {
            List<String> audio = audioForScene("demo", "DEMO");
            PointF presenterLoc = (PointF)presenter.control.propertyValue("startloc");
            presenter.walk(presenterLoc);
            presenter.faceFrontReflected();
            waitForSecs(0.3f);
            presenter.speak((List<Object>)(Object)audio, this);
            waitForSecs(0.3f);
            PointF loc = OC_Generic.copyPoint(box.control.position());
            box.control.setRight(0);
            box.control.show();
            playSfxAudio("box_slide",false);
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(loc,box.control)),0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            waitForSecs(0.3f);
            presenter.moveHandfromIndex(0,2,0.2f);
            presenter.speak((List<Object>)(Object)audioForScene("demo", "DEMO2"), this);
            waitForSecs(0.3f);
            presenter.moveHandfromIndex(2,0,0.2f);
            presenterLoc.x = 0.9f * this.bounds().width();

            presenter.walk(presenterLoc);
            presenter.faceFrontReflected();
            presenter.speak((List<Object>)(Object)audioForScene("demo", "DEMO3"), this);
            waitForSecs(0.3f);
            presenterLoc.x = 1.25f *bounds().width();
            presenter.walk(presenterLoc);
            presenter.control.hide();
        }
        else
        {
            playAudioQueued(OBUtils.insertAudioInterval(audioForScene("introduction", "DEMO"), 300),true);
        }
        if(currentWord < 2)
        {
            demoLetterBox();
        }
        else if(currentWord == 2)
        {
            demoPointCorner();
        }
        startScene();
    }

    public void demoLetterBox() throws Exception
    {
        List<Object> arr = getSceneAudio("DEMO");
        if(arr != null)
        {
            loadPointer(POINTER_LEFT);
            movePointerToPoint(OB_Maths.locationForRect(0.85f,0.8f,box.control.frame()),0.5f,true);
            playAudioQueued(arr,true);
            waitForSecs(0.5f);
            thePointer.hide();
        }
    }

    public void demoPointCorner() throws Exception
    {
        List<Object> arr = getSceneAudio("DEMO");
        if(arr != null)
        {
            loadPointer(POINTER_LEFT);
            movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,this.bounds()),0.5f,true);
            playAudioQueued(arr,true);
            waitForSecs(0.5f);
            thePointer.hide();
        }
    }

    public void demoPointButton() throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,objectDict.get("button").frame()),-15,0.5f,true);
        playAudioQueued(getSceneAudio("DEMO2"),true);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void demoFinLabel() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.1f,fullWordLabel.frame()) ,-35,0.5f,true);
        playAudioQueued(getSceneAudio("FINAL"),true);
        waitForSecs(0.5f);
        thePointer.hide();
    }


}
