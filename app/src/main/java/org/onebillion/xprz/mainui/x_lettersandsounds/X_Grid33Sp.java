package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArraySet;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUserPressedBackException;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alan on 05/08/16.
 */
public class X_Grid33Sp extends X_Grid33S
{
    int dashNormalColour,dashHiColour,squareColour;
    List<OBPath>dashes;
    PointF centrePos;
    List<OBLabel> wordLabels = new ArrayList<>();
    OBLabel mainLabel;
    Map<String,Map<String,String>> letterDict;
    int vowelColour;
    int letterNo;

    public void miscSetUp()
    {
        needDemo = false;
        if(parameters.get("demo") != null && parameters.get("demo").equals("true"))
            needDemo = true;
        events = new ArrayList<>();
        events.add("a");
        if(needDemo)
            events.add("b");
        events.add("c");
        events.add("d");
        events.add("e");
        wordDict = OBUtils.LoadWordComponentsXML(true);
        letterDict = LoadLetterXML(getLocalPath("letters.xml"));
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        bigTextSize = textSize;
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        dashes = (List<OBPath>)(Object)sortedFilteredControls("lne.*");
        for (OBPath d : dashes)
            d.sizeToBoundingBoxIncludingStroke();
        dashHiColour = dashes.get(0).strokeColor();
        dashNormalColour = dashes.get(1).strokeColor();
        centrePos = new PointF();
        centrePos.set(dashes.get(0).position());
        centrePos.x = ((dashes.get(0).left() + dashes.get(dashes.size() - 1) .right()) / 2);
        centrePos.y -= applyGraphicScale(8);
        OBPath vs = (OBPath) objectDict.get("vowelswatch");
        vowelColour = vs.fillColor();
        List<OBControl>ls = ((OBGroup)objectDict.get("grid")).filterMembers("squ.*",true);
        squares = (List<OBPath>)(Object)ls;
        squareColour = squares.get(0).fillColor();
        currNo = 0;
        int noscenes =(int)words.size();
        if(needDemo)
            noscenes++;
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        needClear = true;
    }

    public String letter(String l,String tag)
    {
        Map<String,String> dict = letterDict.get(l);
        return dict.get(tag);
    }

    public List pickLetters(OBWord rw)
    {
        Set usedSet = new ArraySet();
        for(int i = 0;i < rw.text.length();i++)
            usedSet.add(rw.text.substring(i, i + 1));
        int target =(int)squares.size();
        List<String> ls = (List<String>)(Object)Arrays.asList(letterDict.keySet().toArray());
        List<String> someLetters = OBUtils.randomlySortedArray(ls);
        for(String l : someLetters)
        {
            if(!usedSet.contains(l))
            {
                Map dict = letterDict.get(l);
                if(dict.get("vowel") != null)
                {
                    usedSet.add(l);
                    break;
                }
            }
        }
        int i = 0;
        while(usedSet.size()  < target )
        {
            String l = someLetters.get(i);
            if(!usedSet.contains(l))
            {
                Map<String,String> dict = letterDict.get(l);
                if(dict.get("repel") != null )
                {
                    List<String> rs = Arrays.asList(dict.get("repel").split(","));
                    if(rs.contains(l))
                    {
                        i++;
                        continue;
                    }
                }
                usedSet.add(l);
            }
            i++;
        }
        return OBUtils.randomlySortedArray(Arrays.asList(usedSet.toArray()));
    }

    public void highlightDash(int i)
    {
        for(OBPath p : dashes)
            p.setStrokeColor(dashNormalColour);
        if(i >= 0 && i < dashes.size() )
            dashes.get(i) .setStrokeColor(dashHiColour);
    }

    public OBLabel setUpWordLabel(String tx)
    {
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(tx,tf,bigTextSize);
        label.setColour(Color.BLACK);
        float h = label.baselineOffset();
        label.setAnchorPoint(0.5f, h / label.bounds().height());
        label.setZPosition(12);
        return label;
    }

    public void setUpWordLabels()
    {
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(currWord,tf,textSize);
        List<Float> lefts = new ArrayList<>();
        for(int i = 0;i < currWord.length();i++)
        {
            float f = label.textOffset(i);
            lefts.add(f);
        }

        List<OBLabel>ls = new ArrayList<>();
        for(int i = 0; i < currWord.length();i++)
        {
            String sound = currWord.substring(i,i + 1);
            OBLabel l = setUpWordLabel(sound);
            ls.add(l);
            PointF pt = new PointF();
            pt.set(dashes.get(i).position());
            pt.y -= applyGraphicScale(8);
            l.setPosition(pt);
            l.setLeft(mainLabel.left() + lefts.get(i));
            PointF pp = new PointF();
            pp.set(l.position());
            l.setProperty("endposition",pp);
            l.setPosition(pt);
            attachControl(l);
            //l.hide();
            l.setProperty("sound",sound);
        }
        wordLabels = ls;
    }

    public void setUpMainLabel(String tx)
    {
        if(mainLabel != null)
            detachControl(mainLabel);
        Typeface tf = OBUtils.standardTypeFace();
        mainLabel = new OBLabel(tx,tf,bigTextSize);
        mainLabel.setColour(Color.BLACK);
        float h = mainLabel.baselineOffset();
        mainLabel.setAnchorPoint(0.5f, h / mainLabel.bounds().height());
        mainLabel.setPosition(centrePos);
        attachControl(mainLabel);
        mainLabel.hide();
    }

    public void setSceneXX(String  scene)
    {
        if(needClear)
        {
            for(OBLabel l : labels)
                detachControl(l);
            for(OBLabel l : wordLabels)
                detachControl(l);
            deleteControls("im");
            String wordid = words.get(currNo);
            currReadingWord = (OBWord) wordDict.get(wordid);
            currWord = currReadingWord.text;
            setUpImage(currReadingWord.imageName);
            sounds = pickLetters(currReadingWord);
            setUpMainLabel(currWord);
            setUpLabels(Color.WHITE);
            for(int i = 0;i < labels.size();i++)
            {
                String let = (String) labels.get(i).propertyValue("sound");
                if(letter(let,"vowel")!= null)
                    squares.get(i).setFillColor(vowelColour);
                else
                    squares.get(i).setFillColor(squareColour);
            }
            setUpWordLabels();
            for(OBLabel l : wordLabels)
                l.hide();
            letterNo = 0;
            highlightDash(letterNo);
            hideControls("lne.*");
            for(OBLabel l : labels)
                l.setProperty("correct",(l.propertyValue("sound").equals(firstSound)));
            objectDict.get("im").hide();
        }
        needClear = true;
        targets = (List<OBControl>)(Object)squares;
    }

    public void playCurrentWordWait(boolean w) throws OBUserPressedBackException
    {
        playAudioQueued(Collections.singletonList((Object)(words.get(currNo))),w);
    }

    public void doAudio(String  scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", true);
        playCurrentWordWait(true);
    }

    public void doMainXX() throws Exception
    {
        showStuff();
        doAudio(currentEvent());
        if(currentAudio("PROMPT2")!=null)
        {
            playAudioQueuedScene("PROMPT2",false);
            setReplayAudioScene(currentEvent(), "PROMPT2.REPEAT");
        }
    }

    public List squaresForSpelling(String word)
    {
        List<OBControl> squs = new ArrayList<>();
        for(int i = 0;i < word.length();i++)
        {
            String l = word.substring(i, i+1);
            for(int j = 0;j < labels.size();j++)
            {
                OBLabel lab = labels.get(j);
                String s = (String) lab.propertyValue("sound");
                if(s.equals(l))
                {
                    squs.add(squares.get(j));
                    break;
                }
            }
        }
        return squs;
    }

    public void blendLabels(List<OBLabel> labs) throws Exception
    {
        playSfxAudio("blend",false);
        List<OBAnim>anims = new ArrayList<>();
        for(OBLabel lab : labs)
            anims.add(OBAnim.moveAnim((PointF) lab.propertyValue("endposition"),lab));
        OBAnimationGroup.runAnims(anims,0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void showStuff() throws Exception
    {
        if(labels.get(0).hidden() )
        {
            playSfxAudio("splat",false);
            lockScreen();
            for(OBLabel l : labels)
                l.show();
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.2f);
            playSfxAudio("lines",false);
            lockScreen();
            showControls("lne.*");
            unlockScreen();
        }
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        showStuff();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        needClear = false;
        nextScene();
    }

    public void demob() throws Exception
    {
        waitForSecs(0.2f);

        OBControl lne = objectDict.get("lne2");
        PointF destpt = lne.bottomPoint();
        float ptrAng = 30;
        PointF startpt = pointForDestPoint(destpt,ptrAng);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.5f, startpt, destpt),-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.2f);
        playCurrentWordWait(true);
        waitForSecs(0.2f);

        movePointerForwards(applyGraphicScale(60),-1);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);

        List<OBPath>squs = squaresForSpelling(currWord);
        float sp = -1;
        for(OBPath sq : squs)
        {
            PointF pt = convertPointFromControl(OB_Maths.locationForRect(0.7f, 0.7f, sq.bounds),sq);
            movePointerToPoint(pt,sp,true);
            sp = -0.4f;
            waitForSecs(0.2f);
            playSfxAudio("touch",false);
            sq.highlight();
            waitSFX();
            playSfxAudio("letteron",false);
            wordLabels.get(letterNo).show();
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.4f);
            sq.lowlight();
            highlightDash(++letterNo);
        }
        waitForSecs(0.4f);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.5f, startpt, destpt),-1,true);
        waitForSecs(0.4f);
        lockScreen();
        hideControls("lne.*");
        thePointer.hide();
        unlockScreen();
        endOfScene(false);
        waitForSecs(1f);
        nextScene();
    }

    public void disappearStuff() throws Exception
    {
        playSfxAudio("wordoff",false);
        lockScreen();
        hideControls("im");
        for(OBLabel l : wordLabels)
            l.hide();
        for(OBLabel l : labels)
            l.hide();
        unlockScreen();
        waitForSecs(0.2f);
        waitSFX();
        waitForSecs(0.2f);
    }


    public void endOfScene(boolean showImage) throws Exception
    {
        lockScreen();
        hideControls("lne.*");
        unlockScreen();

        waitForSecs(0.5f);
        String infix = "_let_";
        String fileName = words.get(currNo).replaceFirst("_",infix);

        if(!itemsInSameDirectory(words.get(currNo),fileName))
        {
            List<String>phs = new ArrayList<>();
            for(OBSyllable syl : currReadingWord.syllables)
                for(OBPhoneme obp : syl.phonemes)
                    phs.add(obp.soundid);
            highlightAndSpeakIndividualPhonemes(wordLabels,phs);
        }
        else
            highlightAndSpeakComponents(wordLabels,words.get(currNo),currWord,fileName);

        blendLabels(wordLabels);

        waitForSecs(0.5f);

        lockScreen();
        for(OBLabel l : wordLabels)
            SetColourForLabel(l,Color.RED);
        unlockScreen();
        playCurrentWordWait(true);
        waitForSecs(0.3f);
        lockScreen();
        for(OBLabel l : wordLabels)
            SetColourForLabel(l, Color.BLACK);
        unlockScreen();
        waitForSecs(0.6f);
        if(showImage)
        {
            playSfxAudio("picon",false);
            showControls("im");
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.3f);
            playCurrentWordWait(true);
            waitForSecs(1f);
        }

    }

    public void nextScene()
    {
        if(++eventIndex >= events.size() )
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
                    try
                    {
                        if(needClear)
                        {
                            disappearStuff();
                            currNo++;
                        }
                        waitAudio();
                        waitForSecs(0.2f);
                        setScene(events.get(eventIndex));
                    }
                    catch(Exception exception)
                    {
                    }

                }
            });
    }

    public void nextLetter()
    {
        letterNo++;
        if(letterNo >= currWord.length() )
        {
            try
            {
                endOfScene(true);
            }
            catch(Exception exception)
            {
            }

            //currNo++;
            nextScene();
        }
        else
        {
            highlightDash(letterNo);
            switchStatus(currentEvent());
        }

    }

    public void playAudioPlusWord(List audio)
    {
        try
        {
            List<Object> lst = new ArrayList<>(audio);
            lst.add(300);
            lst.add(words.get(currNo));
            playAudioQueued(lst,true);
        }
        catch(Exception exception)
        {
        }

    }

    public void _replayAudio()
    {
        try
        {
            List<Object> lst = new ArrayList<>();
            lst.add(words.get(currNo));
            lst.add(300);
            lst.addAll(_replayAudio);
            playAudioQueued(lst,false);
        }
        catch(Exception exception)
        {
        }
    }

    public void checkTarget(final OBControl targ,PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        targ.highlight();
        try
        {
            int idx = squares.indexOf(targ);
            if(labels.get(idx).propertyValue("sound").equals(currWord.substring(letterNo,letterNo + 1)) )
            {
                playSfxAudio("touch",false);
                waitForSecs(0.2f);
                waitSFX();
                playSfxAudio("letteron",false);
                wordLabels.get(letterNo).show();
                waitForSecs(0.4f);
                targ.lowlight();
                setReplayAudio(saverep);
                nextLetter();
            }
            else
            {
                gotItWrongWithSfx();
                waitSFX();
                setReplayAudio(saverep);
                setStatus(saveStatus);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        playAudioPlusWord(currentAudio("INCORRECT"));
                    }
                });
                OBUtils.runOnOtherThreadDelayed(0.2f,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        targ.lowlight();
                    }
                });
            }
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
