package org.onebillion.onecourse.mainui.oc_segblend;

import org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 * Created by alan on 15/01/2018.
 */

public class OC_Segblend3 extends OC_Wordcontroller
{
    boolean syllableMode;
    List<String>words;
    List<OBPhoneme>components;
    Map wordDict;
    float textSize;
    PointF centrePos;
    OBGroup button;
    OBFont font;
    boolean showPic;
    OBImage image;
    List<Float>leftOffsets;
    OBLabel mainLabel;
    List<OBLabel>wordLabels;
    OBWord currReadingWord;
    String currWord;
    List controlsToDestroy;

    public void miscSetUp()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        showPic = OBUtils.coalesce(parameters.get("showpic") , "true").equals("true");
        textSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize") , "82"));
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        currNo = 0;
        centrePos = objectDict.get("textrect").position();
        syllableMode = OBUtils.coalesce(parameters.get("mode") , "pho").startsWith("syl");
        button =(OBGroup) objectDict.get("button");
        font = OBUtils.StandardReadingFontOfSize(textSize);
        controlsToDestroy = new ArrayList<>();
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList(Arrays.asList("b,c,d".split(",")));
        while(events.size() < words.size())
            events.add(events.get(events.size()-1));
        while(events.size() > words.size())
            events.remove(events.size()-1);
        events.add(0,"a");
        doVisual(currentEvent());
    }

    public void doMainXX() throws Exception
    {
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        if(!buttonActive())
        {
            setButtonActive();
            playSfxAudio("buttonactive",true);
            waitForSecs(0.1f);
        }
        List audio = currentAudio("PROMPT");
        playAudioQueued(audio,false);
    }

    public void flashButton(long sttime)
    {
        long token = -1;
        try
        {
            token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                for(int i = 0;i < 3 && sttime == statusTime;i++)
                {
                    checkSequenceToken(token);
                    waitForSecs(0.3f);
                    checkSequenceToken(token);
                    setButtonSelected();
                    waitForSecs(0.3f);
                    setButtonActive();
                }
            }
        }
        catch(Exception exception)
        {
        }
        setButtonActive();
        sequenceLock.unlock();
    }

    public void reprompt(final long sttime)
    {
        if(sttime == statusTime)
            reprompt(sttime,null,6, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    if(sttime == statusTime)
                    {
                        flashButton(sttime);
                        reprompt(sttime);
                    }
                }
            });
    }

    public void doReminder() throws Exception
    {
        long sttime = statusTime;
        waitForSecs(0.2f);
        waitAudio();
        reprompt(sttime);
    }

    public void endBody()
    {
        try
        {
            doReminder();
        }
        catch(Exception e)
        {

        }
    }

    public List layOutComponents(List<OBPhoneme> comps,OBLabel mLabel,OBFont font)
    {
        List<Float> rights = new ArrayList<>();
        String text = mLabel.text();
        int cumlength = 0;
        for(int i = 0;i < comps.size();i++)
        {
            cumlength += comps.get(i).text.length();
            String subtx = text.substring(0,cumlength);
            RectF r = boundingBoxForText(subtx,font);
            float f = r.width();
            rights.add(f);
        }
        List labs = new ArrayList<>();
        int idx = 0,i = 0;
        List syllableLefts = new ArrayList<>();
        for(OBPhoneme phoneme : comps)
        {
            OBLabel l = new OBLabel(phoneme.text,font);
            l.setColour(Color.BLACK);
            l.setPosition(mLabel.position());
            if (i == 0)
                syllableLefts.add(0f);
            else
                syllableLefts.add(rights.get(i-1));
            l.setRight(mLabel.left() + rights.get(i));
            l.setProperty("origpos",new PointF(l.position().x,l.position().y));
            labs.add(l);
            attachControl(l);
            i++;
        }
        leftOffsets = syllableLefts;
        return labs;
    }

    public void setUpMainLabel(String tx)
    {
        if(mainLabel != null)
            detachControl(mainLabel);
        OBFont font = OBUtils.StandardReadingFontOfSize(textSize);
        mainLabel = new OBLabel(tx,font);
        mainLabel.setColour(Color.BLACK);
        RectF bb = boundingBoxForText(mainLabel.text(),font);
        float h = bb.height() + bb.top;
        mainLabel.setAnchorPoint(new PointF(0.5f, h / mainLabel.bounds().height()));
        mainLabel.setPosition(centrePos);
        attachControl(mainLabel);
        mainLabel.hide();
    }

    public void setSceneXX(String scene)
    {
        setUpScene();
    }

    public void setUpScene()
    {
        if(image != null)
        {
            controlsToDestroy.add(image);
            image = null;
        }
        if (wordLabels != null)
            controlsToDestroy.addAll(wordLabels);

        String wordid = words.get(currNo);
        currReadingWord = (OBWord) wordDict.get(wordid);
        currWord = currReadingWord.text;
        if(syllableMode)
            components = (List)currReadingWord.syllables();
        else
            components = currReadingWord.phonemes();


        if(showPic)
        {
            OBWord word = (OBWord) wordDict.get(words.get(currNo));
            OBImage im = loadImageWithName(word.imageName,new PointF(0, 0),boundsf(),false);
            if(im != null)
            {
                OBControl picbox = objectDict.get("picrect");
                im.setPosition(picbox.position());
                im.setScale(picbox.height() / im.height());
                im.setZPosition(12);
                im.setBackgroundColor(Color.WHITE);
                attachControl(im);
            }
            image = im;
            if (image != null)
                image.hide();
        }
        setUpMainLabel(currWord);
        wordLabels = layOutComponents(components,mainLabel,OBUtils.StandardReadingFontOfSize(textSize));
        for(OBLabel l : wordLabels)
            l.hide();
    }


    public void setButtonActive()
    {
        lockScreen();
        button.setOpacity(1.0f);
        button.objectDict.get("selected").hide();
        button.objectDict.get("active").show();
        unlockScreen();
    }

    public void setButtonInactive()
    {
        lockScreen();
        button.setOpacity(0.6f);
        button.objectDict.get("selected").hide();
        button.objectDict.get("active").show();
        unlockScreen();
    }

    public void setButtonSelected()
    {
        lockScreen();
        button.setOpacity(1.0f);
        button.objectDict.get("selected").show();
        button.objectDict.get("active").hide();
        unlockScreen();
    }

    public boolean buttonActive()
    {
        return button.opacity() == 1.0 && button.objectDict.get("selected").hidden();
    }

    public void setScenea()
    {
    }

    public void demoa() throws Exception
    {
        PointF destpt = OB_Maths.locationForRect(new PointF(0.5f, 0.66f) , bounds());
        PointF startpt = new PointF(destpt.x,0);
        startpt.y = (bounds() .height() + applyGraphicScale(8));
        loadPointerStartPoint(startpt,destpt);

        movePointerToPoint(destpt,-1,true);

        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);

        destpt = OB_Maths.locationForRect(new PointF(0.5f, 1.5f) , button.frame());
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO2",0,true);
        destpt.y = (0.9f * bounds() .height());
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO2",1,true);

        waitForSecs(0.3f);

        thePointer.hide();
        nextScene();
    }


    public void segmentComponents(final List<OBLabel> componentLabels,final List<Float> lftOffsets,final OBLabel wholeLabel) throws Exception
    {
        final float normalWidth = wholeLabel.width();
        final float maxgap = 0.46f * textSize;

        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        float gap = maxgap * frac;
                        float thisWidth = normalWidth +(components.size() - 1) * gap;
                        float left = wholeLabel.position().x - thisWidth / 2;
                        for(int i = 0;i < components.size();i++)
                        {
                            OBLabel l = componentLabels.get(i);
                            l.setLeft(left + lftOffsets.get(i).floatValue() + gap * i);
                        }

                    }
                }
        ),0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);

    }

    public void joinComponents(final List<OBLabel> componentLabels,final List<Float> lftOffsets,final OBLabel wholeLabel) throws Exception
    {
        final float normalWidth = wholeLabel.width();
        final float maxgap = 0.46f * textSize;

        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        float gap = maxgap *(1 - frac);
                        float thisWidth = normalWidth +(components.size() - 1) * gap;
                        float left = wholeLabel.position().x - thisWidth / 2;
                        for(int i = 0;i < components.size();i++)
                        {
                            OBLabel l = componentLabels.get(i);
                            l.setLeft(left + leftOffsets.get(i).floatValue() + gap * i);
                        }
                    }
                }
        ),0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
     }

    public void highlightAndSpeakWord() throws Exception
    {
        lockScreen();
        for(OBLabel l : wordLabels)
            highlightLabel(l,true);
        unlockScreen();
        playAudioQueued((List)Arrays.asList(words.get(currNo)),true);
        waitForSecs(0.7f);
        lockScreen();
        for(OBLabel l : wordLabels)
            highlightLabel(l,false);
        unlockScreen();
    }

    public void showThings() throws Exception
    {
        if(controlsToDestroy.size() > 0)
        {
            lockScreen();
            detachControls(controlsToDestroy);
            unlockScreen();
            playSfxAudio("alloff",true);
            controlsToDestroy.clear();
            waitForSecs(0.4f);
        }
        if(image != null)
        {
            image.show();
            playSfxAudio("picon",true);
            waitForSecs(0.4f);
        }
        lockScreen();
        for(OBLabel l : wordLabels)
            l.show();
        unlockScreen();
        playSfxAudio("wordon",true);
        waitForSecs(0.4f);
        highlightAndSpeakWord();
        waitForSecs(0.4f);


        playSfxAudio("segment",false);
        segmentComponents(wordLabels,leftOffsets,mainLabel);
        String infix = syllableMode?"_syl_":"_let_";
        String fileName = words.get(currNo).replace("_",infix);
        if(!syllableMode && OBAudioManager.audioManager.getAudioPath(fileName) == null)
        {
            List<String>phs = new ArrayList<>();
            for(OBSyllable syl : currReadingWord.syllables)
                for(OBPhoneme obp : syl.phonemes)
                    phs.add(obp.soundid);
            highlightAndSpeakIndividualPhonemes(wordLabels,phs);
        }
        else
            highlightAndSpeakComponents(wordLabels,words.get(currNo),currReadingWord.text,fileName);
        waitForSecs(0.5f);
        playAudio("blend");
        joinComponents(wordLabels,leftOffsets,mainLabel);
        highlightAndSpeakWord();
        waitForSecs(0.5f);
        waitForSecs(0.45f);
    }


    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            takeSequenceLockInterrupt(true);
            sequenceLock.unlock();
            playSfxAudio("touchbutton",false);
            setButtonSelected();
            OBUtils.runOnOtherThreadDelayed(0.4f,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    setButtonInactive();
                }
            });
            showThings();
            currNo++;
            nextScene();
        }
        catch(Exception exception)
        {
        }

    }


    public Object findTarget(PointF pt)
    {
        OBControl c = finger(-1,2,Arrays.asList((OBControl)button),pt);
        return c;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            target = (OBControl)findTarget(pt);
            if(target != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(target,pt);
                    }
                });
            }
        }
    }

}
