package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 10/08/16.
 */
public class X_Segblendx extends X_Wordcontroller
{
    List<String>words,sounds;
    String currWord,firstSound;
    float textSize;
    List<OBLabel>labels;
    Map wordDict;
    OBWord currReadingWord;
    int buttonCurrentColour,buttonDoneColour;
    List<OBGroup>buttons;
    List<OBLabel> wordLabels = new ArrayList<>();
    PointF centrePos;
    OBLabel mainLabel;
    List<Float>leftOffsets;
    boolean syllableMode;
    boolean onebuttonMode;

    public void doButtons()
    {
        buttons = (List<OBGroup>)(Object)sortedFilteredControls("but.*");
        if(onebuttonMode)
        {
            deleteControls("but[^4]");
            buttons = Arrays.asList(buttons.get(4));
        }
        else
        {
            while(words.size()  < buttons.size() )
            {
                detachControl(buttons.get(buttons.size()-1));
                buttons.remove(buttons.get(buttons.size()-1));
            }
        }
        targets = (List<OBControl>)(Object)buttons;

    }

    public void miscSetUp()
    {
        String par = parameters.get("onebutton");
        onebuttonMode = par != null && par.equals("true");
        wordDict = OBUtils.LoadWordComponentsXML(true);
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        OBPath p = (OBPath) objectDict.get("swatchcurrent");
        buttonCurrentColour = p.fillColor();
        p = (OBPath) objectDict.get("swatchdone");
        buttonDoneColour = p.fillColor();
        doButtons();
        currNo = 0;
        centrePos = objectDict.get("textrect").position();
        String sm = parameters.get("mode");
        if (sm != null)
            syllableMode =(sm.startsWith("syl"));
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        //events = [eventAttributes"scenes".().split(",") mutableCopy];
        events = new ArrayList(Arrays.asList("b,c,d".split(",")));
        miscSetUp();
        if(onebuttonMode)
        {
            while(events.size()  < words.size() )
                events.add(events.get(events.size()-1) );
            while(events.size()  > words.size() )
                events.remove(events.size()-1);
        }
        else
        {
            for(int i = 0;i < 20;i++)
                events.add(events.get(events.size()-1) );
        }
        events.add(0,"a");
        doVisual(currentEvent());
    }

    public void setSceneXX(String  scene)
    {
    }

    public void setUpImage(String fileName)
    {
        OBImage im = OBImageManager.sharedImageManager().imageForName(fileName);
        OBControl pic = objectDict.get("pic");
        im.setScale(pic.scale());
        im.setPosition(pic.position());
        attachControl(im);
        objectDict.put("im",im);
        im.setZPosition(5);
    }

    public List layOutComponents(List<String> syllables, OBLabel mLabel)
    {
        List<Float> rights = new ArrayList<>();
        String text = mLabel.text();
        Typeface tf = mLabel.typeface();
        float size = mLabel.fontSize();
        int cumlength = 0;
        for(int i = 0;i < syllables.size();i++)
        {
            cumlength += syllables.get(i).length();
            String subtx = text.substring(0,cumlength);
            RectF r = boundingBoxForText(subtx,tf,size);
            float f = r.width();
            rights.add(f);
        }
        List labs = new ArrayList<>();
        int i = 0;
        List syllableLefts = new ArrayList<>();
        for(String syllable : syllables)
        {
            OBLabel l = new OBLabel(syllable,tf,size);
            l.setColour(Color.BLACK);
            l.setPosition(mLabel.position());
            if (i == 0)
                syllableLefts.add(0);
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
        Typeface tf = OBUtils.standardTypeFace();
        mainLabel = new OBLabel(tx,tf,textSize);
        mainLabel.setColour(Color.BLACK);

        float h = baselineOffsetForText("My",OBUtils.standardTypeFace(),textSize);
        mainLabel.setAnchorPoint(0.5f, h / mainLabel.bounds.height());
        mainLabel.setPosition(centrePos);
        attachControl(mainLabel);
        mainLabel.hide();
    }

    public void setUpVisuals(int i)
    {
        lockScreen();
        deleteControls("im");
        detachControl(mainLabel);
        for(OBControl c : wordLabels)
            detachControl(c);
        String wordid = words.get(i);
        currReadingWord = (OBWord) wordDict.get(wordid);
        currWord = currReadingWord.text;
        sounds = new ArrayList<>();
        if(syllableMode)
        {
            for (OBSyllable obs : currReadingWord.syllables())
                sounds.add(obs.text);
        }
        else
        {
            for (OBPhoneme obp : currReadingWord.phonemes())
                sounds.add(obp.text);
        }
        setUpImage(currReadingWord.imageName);
        setUpMainLabel(currWord);
        wordLabels = layOutComponents(sounds,mainLabel);
        for(OBLabel l : wordLabels)
            l.hide();
        unlockScreen();
    }

    public void setButtonCurrent(OBGroup button)
    {
        OBPath main = (OBPath) button.objectDict.get("main");
        main.setFillColor(buttonCurrentColour);
    }

    public void setButtonDone(OBGroup button)
    {
        OBPath main = (OBPath) button.objectDict.get("main");
        main.setFillColor(buttonDoneColour);
    }

    public void highlightButtons(int idx)
    {
        for(int i = 0;i < buttons.size();i++)
            if(i == idx)
                setButtonCurrent(buttons.get(i));
            else
                buttons.get(i).setOpacity(0.4f);
    }

    public void lowlightButtons(int idx)
    {
        for(int i = 0;i < buttons.size();i++)
            if(i == idx)
                setButtonDone(buttons.get(i));
            else
                buttons.get(i).setOpacity(1);
    }

    public void segmentComponents(final List<OBLabel>components,final List<Float>lftOffsets,final OBLabel wholeLabel)
    {
        final float normalWidth = wholeLabel.width();
        final float maxgap = 0.46f * textSize;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float gap = maxgap * frac;
                float thisWidth = normalWidth +(components.size()  - 1) * gap;
                float left = wholeLabel.position().x - thisWidth / 2;
                for(int i = 0;i < components.size();i++)
                {
                    OBLabel l = components.get(i);
                    l.setLeft(left + lftOffsets.get(i).floatValue() + gap * i);
                }
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(blockAnim),0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void joinComponents(final List<OBLabel>components,final List<Float>lftOffsets,final OBLabel wholeLabel)
    {
        final float normalWidth = wholeLabel.width();
        final float maxgap = 0.46f * textSize;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float gap = maxgap * (1 -frac);
                float thisWidth = normalWidth +(components.size()  - 1) * gap;
                float left = wholeLabel.position().x - thisWidth / 2;
                for(int i = 0;i < components.size();i++)
                {
                    OBLabel l = components.get(i);
                    l.setLeft(left + lftOffsets.get(i).floatValue() + gap * i);
                }
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(blockAnim),0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void showThings(int i)throws Exception
    {
        objectDict.get("im").show();
        playSfxAudio("imageon",true);
        waitForSecs(0.4f);
        lockScreen();
        for(OBLabel l : wordLabels)
            l.show();
        unlockScreen();
        playSfxAudio("texton",true);
        waitForSecs(0.4f);
        playAudioQueued(Arrays.asList((Object)words.get(i),true));
        waitForSecs(0.4f);
        playSfxAudio("segment",false);
        segmentComponents(wordLabels,leftOffsets,mainLabel);
        String infix = syllableMode?"_syl_":"_let_";
        String fileName = words.get(i).replaceFirst(words.get(i),infix);
        highlightAndSpeakComponents(wordLabels,words.get(i),currReadingWord.text,fileName);
        waitForSecs(0.5f);
        playAudio("blend");
        joinComponents(wordLabels,leftOffsets,mainLabel);
        lockScreen();
        for(OBLabel l : wordLabels)
            highlightLabel(l,true);
        unlockScreen();
        playAudioQueued(Arrays.asList((Object)words.get(i)),true);
        waitForSecs(0.7f);
        lockScreen();
        for(OBLabel l : wordLabels)
            highlightLabel(l,false);
        unlockScreen();
    }

    public boolean finished()
    {
        if(onebuttonMode)
            return false;
        for(OBGroup button : buttons)
        {
            OBPath main = (OBPath) button.objectDict.get("main");
            if(main.fillColor()!=(buttonDoneColour))
                return false;
        }
        return true;
    }

    public void nextScene()
    {
        if(++eventIndex >= events.size()  || finished() )
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    fin();
                }
            });
        else
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    setScene(events.get(eventIndex));
                }
            });
    }

    public void doMainXX() throws Exception
    {
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        List audio = currentAudio("PROMPT");
        if(audio != null)
            playAudioQueued(OBUtils.insertAudioInterval(audio,300),false);
    }


    public void doReminder() throws Exception
    {
        List reminderAudio = currentAudio("PROMPT.REMINDER");
        long sttime = statusTime;
        waitForSecs(0.2f);
        waitAudio();
        if(sttime == statusTime)
            reprompt(sttime,reminderAudio,4);
    }

    public void endBody()
    {
        if(currentAudio("PROMPT.REMINDER") != null )
            try
            {
                doReminder();
            }
            catch(Exception e)
            {

            }
    }

    public void demoa() throws Exception
    {
        OBControl but = buttons.get(buttons.size()-1);
        PointF destpt = OB_Maths.locationForRect(0.5f, 1.2f, but.frame);
        PointF startpt = new PointF(destpt.x,destpt.y);
        startpt.y = (bounds().height() + applyGraphicScale(8));
        loadPointerStartPoint(startpt,destpt);
        playAudioScene("DEMO",0,true);
        movePointerToPoint(destpt,-1,true);
        playAudioScene("DEMO",1,true);
        movePointerForwards(applyGraphicScale(-100),-1);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        nextScene();
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_CHECKING);
        emptyReplayAudio();
        try
        {
            int idx = buttons.indexOf(targ);
            if(idx < words.size() )
            {
                highlightButtons(idx);
                int wordIdx = idx;
                if(onebuttonMode)
                    wordIdx = currNo++;
                setUpVisuals(wordIdx);
                showThings(wordIdx);
                lockScreen();
                lowlightButtons(idx);
                unlockScreen();
            }
            nextScene();
        }
        catch(Exception exception)
        {
        }

    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
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
