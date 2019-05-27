package com.maq.xprize.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.utils.OBSyllable;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OBWord;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alan on 11/08/16.
 */
public class OC_G3x3syl1 extends OC_Grid33Sp
{
    Set syllableSet;
    int gridColour;
    boolean hasPicture;
    int componentNo;
    boolean needRegen;

    public void buildSyllableSet()
    {
        Set set = new HashSet();
        for(String w : words)
        {
            OBWord rw = (OBWord) wordDict.get(w);
            for(OBSyllable syll : rw.syllables() )
                set.add(syll.text);
        }
        if(set.size()  < squares.size() )
        {
            String ws = parameters.get("distractors");
            if (ws != null)
                for(String sys : ws.split(","))
                    set.add(sys);
        }
        syllableSet = set;
    }

    public void buildEvents()
    {
        events = new ArrayList<>();
        events.add("a");
        if(needDemo)
            events.add("b");
        events.add("c");
        events.add("d");
        events.add("e");
        int noscenes =(int)words.size();
        if(needDemo)
            noscenes++;
        while(events.size()  > noscenes)
            events.remove(events.size() - 1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
    }

    public void processWords()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
    }

    public void loadMainScene()
    {
        String pp = parameters.get("pictures");
        if (pp == null)
            pp = "";
        if((hasPicture = !pp.equals("false")))
            loadEvent("horiz");
        else
            loadEvent("vert");
        OBPath sw = (OBPath) objectDict.get("swatch");
        squareColour = sw.fillColor();
        gridColour = sw.strokeColor();
    }

    public void miscSetUp()
    {
        needDemo = false;
        String pd = parameters.get("demo");
        if(pd != null && pd.equals("true") )
            needDemo = true;
        needRegen = true;
        loadMainScene();
        processWords();
        OBGroup grid = (OBGroup) objectDict.get("grid");
        squares = (List<OBPath>)(Object)grid.filterMembers("squ.*",true);
        buildSyllableSet();
        buildEvents();
        OBGroup gtl = (OBGroup) objectDict.get("gridtext");
        OBLabel tl = (OBLabel) gtl.objectDict.get("t");
        textSize = tl.fontSize();
        bigTextSize = textSize * 1.5f;
        dashes = (List<OBPath>)(Object)sortedFilteredControls("lne.*");
        for (OBPath d : dashes)
            d.sizeToBoundingBoxIncludingStroke();
        dashHiColour = dashes.get(0).strokeColor();
        dashNormalColour = dashes.get(1).strokeColor();

        centrePos = new PointF();
        centrePos.set(dashes.get(0).position());
        centrePos.x = ((dashes.get(0).left() + dashes.get(dashes.size()-1).right()) / 2);
        centrePos.y -= applyGraphicScale(8);
        for(OBPath s : squares)
            s.setFillColor(squareColour);
        OBPath back = (OBPath) grid.objectDict.get("backrect");
        back.setFillColor(gridColour);
        currNo = 0;
    }

    public List pickSyllables(OBWord rw)
    {
        Set<String> sylls = new HashSet();
        for (OBSyllable syl : rw.syllables())
            sylls.add(syl.text);
        List allSyls = Arrays.asList(syllableSet.toArray());
        int i = 0;
        while(sylls.size()  < squares.size() )
        {
            sylls.add((String) allSyls.get(i++));
        }
        return Arrays.asList(sylls.toArray());
    }

    public void setUpWordLabels()
    {
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(currWord,tf,bigTextSize);
        List<Float> lefts = new ArrayList<>();
        int i = 0;
        for(OBSyllable syl : currReadingWord.syllables())
        {
            String sound = syl.text;
            float f = label.textOffset(i);
            lefts.add(f);
            i += syl.text.length();
        }

        i = 0;
        List<OBLabel>ls = new ArrayList<>();
        for(OBSyllable syl : currReadingWord.syllables())
        {
            String sound = syl.text;
            OBLabel l = setUpWordLabel(sound);
            ls.add(l);
            PointF pt = new PointF();
            pt.set(dashes.get(i).position());
            pt.y -= applyGraphicScale(8);
            l.setPosition(pt);
            l.setLeft(mainLabel.left() + lefts.get(i));
            PointF pt2 = new PointF();
            pt2.set(l.position());
            l.setProperty("endposition",pt2);
            l.setPosition(pt);
            attachControl(l);
            //l.hide();
            l.setProperty("sound",sound);
            i++;
        }
        wordLabels = ls;
    }

    public void setSceneXX(String  scene)
    {
        if(needRegen)
        {
            for(OBLabel l : labels)
                detachControl(l);
            for(OBLabel l : wordLabels)
                detachControl(l);
            deleteControls("im");
            String wordid = words.get(currNo);
            currReadingWord = (OBWord) wordDict.get(wordid);
            currWord = currReadingWord.text;
            if(hasPicture)
                setUpImage(currReadingWord.imageName);
            sounds = pickSyllables(currReadingWord);
            setUpMainLabel(currWord);
            setUpLabels(Color.BLACK);
            setUpWordLabels();
            for(OBLabel l : wordLabels)
                l.hide();
            componentNo = 0;
            highlightDash(componentNo);
            hideControls("lne.*");
            for(OBLabel l : labels)
                l.setProperty("correct",l.propertyValue("sound").equals(firstSound));
            hideControls("im");
            targets = (List<OBControl>)(Object)squares;
        }
        needRegen = true;
    }

    public void setSceneb()
    {
        needRegen = true;
    }

    public List arrayOfCurrentWord()
    {
        return Arrays.asList(words.get(currNo));
    }

    public void _replayAudio()
    {
        List lst = new ArrayList(_replayAudio);
        lst.add(300);
        lst.addAll(arrayOfCurrentWord());
        try
        {
            playAudioQueued(lst,false);
        }
        catch(Exception exception)
        {
        }
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
        playAudioScene("DEMO",0,true);
        needRegen = false;
        nextScene();
    }

    public List<OBControl> syllableSquaresForWord(String wordId)
    {
        OBWord rw = (OBWord) wordDict.get(wordId);
        List<OBSyllable> sylls = rw.syllables();
        List<OBControl> squs = new ArrayList<>();
        for(int i = 0;i < sylls.size();i++)
        {
            OBSyllable syl = sylls.get(i);
            String l = syl.text;
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


    public void demob() throws Exception
    {
        waitForSecs(0.2f);

        OBControl lne = objectDict.get("lne1");
        PointF destpt = lne.bottomPoint();
        float ptrAng = 30;
        PointF startpt = pointForDestPoint(destpt,ptrAng);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.5f, startpt, destpt),-1,true);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        playCurrentWordWait(true);
        waitForSecs(0.2f);
        playAudioScene("DEMO",1,true);

        List<OBControl> squs = syllableSquaresForWord(words.get(currNo));
        float sp = -1;
        for(OBControl sq : squs)
        {
            PointF pt = convertPointFromControl(OB_Maths.locationForRect(0.7f, 0.7f, sq.bounds()),sq);
            movePointerToPoint(pt,sp,true);
            sp = -0.6f;
            waitForSecs(0.2f);
            playSfxAudio("touch",false);
            sq.highlight();
            movePointerToPoint(OB_Maths.locationForRect(1.1f, 1.1f, objectDict.get("grid").frame()),-1,true);

            playSfxAudio("letteron",false);
            wordLabels.get(componentNo).show();
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.4f);
            sq.lowlight();
            highlightDash(++componentNo);
        }
        waitForSecs(0.4f);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.5f, startpt, destpt),-1,true);
        waitForSecs(0.4f);
        lockScreen();
        hideControls("lne.*");
        thePointer.hide();
        unlockScreen();
        endOfScene(false);
        currNo++;
        waitForSecs(1f);
        playAudioQueuedScene("DEMO3",true);
        nextScene();

    }

    public void nextComponent()
    {
        componentNo++;
        if(componentNo >= currReadingWord.syllables().size())
        {
            try
            {
                endOfScene(hasPicture);
                gotItRightBigTick(true);
            }
            catch(Exception exception)
            {
            }
            currNo++;
            nextScene();
        }
        else
        {
            highlightDash(componentNo);
            switchStatus(currentEvent());
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
            int idx = squares.indexOf((Object)targ);
            if(labels.get(idx).propertyValue("sound").equals(currReadingWord.syllables().get(componentNo).text))
            {
                playSfxAudio("touch",false);
                waitForSecs(0.2f);
                waitSFX();
                playSfxAudio("letteron",false);
                wordLabels.get(componentNo).show();
                waitForSecs(0.4f);
                targ.lowlight();
                setReplayAudio(saverep);
                nextComponent();
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
                        List ws = new ArrayList(currentAudio("INCORRECT"));
                        ws.add(words.get(currNo));

                        playAudioQueued(ws);
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

    public void endOfScene(boolean showImage)
    {
        try
        {
            lockScreen();
            hideControls("lne.*");
            unlockScreen();

            waitForSecs(0.5f);
            String infix = "fc_syl_";
            String fileName = words.get(currNo).replaceFirst("fc_",infix);
            highlightAndSpeakComponents(wordLabels,words.get(currNo),currWord,fileName);

            blendLabels(wordLabels);

            waitForSecs(0.5f);

            lockScreen();
            for(OBLabel l : wordLabels)
                SetColourForLabel(l,Color.RED );
            unlockScreen();
            playCurrentWordWait(true);
            waitForSecs(0.3f);
            lockScreen();
            for(OBLabel l : wordLabels)
                SetColourForLabel(l,Color.BLACK );
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
            waitAudio();

        }
        catch (Exception e)
        {

        }

    }

}
