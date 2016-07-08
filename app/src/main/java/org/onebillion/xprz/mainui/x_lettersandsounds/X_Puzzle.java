package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBConditionLock;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alan on 05/07/16.
 */
public class X_Puzzle extends X_Wordcontroller
{
    static final int SHOW_TEXT_NONE = 0,
    SHOW_TEXT_INITIAL = 1,
    SHOW_TEXT_WORD =2;

    List<String> words;
    String currWord;
    List<OBGroup> pieces;
    List<OBPath> positions,puzzlePieces,swatches;
    List<RectF> homeRects;
    OBGroup puzzle;
    boolean gotoStage2;
    float textSize;
    OBControl textBox;
    OBLabel label;
    Map<String,String> wordDict;
    Map<String,OBPhoneme> componentDict;

    ReentrantLock finishLock;
    boolean animDone,preAssembled,firstTime;
    int showText;
    OBConditionLock audioLock;
    boolean firstTimeIn;

    public void miscSetUp()
    {
        firstTimeIn = true;
        //wordDict = LoadFlashcardXML(getLocalPath("flashcards.xml"));
        componentDict = OBUtils.LoadWordComponentsXML(true);

        String s = eventAttributes.get("textsize");
        if (s != null)
            textSize = Float.parseFloat(s);
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        currNo = 0;
        finishLock = new ReentrantLock();
        swatches = (List<OBPath>)(Object)OBUtils.randomlySortedArray(filterControls("swatch.*"));
        s = parameters.get("showtext");
        if (s != null)
        {
            if (s.equals("initial"))
                showText = SHOW_TEXT_INITIAL;
            else if (s.equals("word"))
                showText = SHOW_TEXT_WORD;
        }
        if (showText > 0)
            loadEvent("text");
        else
            loadEvent("notext");
        needDemo = false;
        if ((s = parameters.get("demo"))!= null)
            needDemo = s.equals("true");
        if ((s = parameters.get("preassembled"))!= null)
            preAssembled = s.equals("true");
        textBox = objectDict.get("textbox");
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        //events = "c,d,e"componentsSeparatedByString.(",")mutableCopy.();
        events = new ArrayList<>();
        events.add("c");
        events.add(preAssembled?"d1":"d2");
        events.add("e");
        while (events.size() < words.size())
            events.add(events.get(events.size()-1));
        while (events.size() > words.size())
            events.remove(events.size()-1);
        if (needDemo)
        {
            String format = parameters.get("format");
            boolean p9 = format.equals("puzzle9");
            events.add(0,p9?"b2":"b1");
        }
        events.add(0,preAssembled?"a1":"a2");
        doVisual(currentEvent());
        firstTime = true;
    }

    public void maskImage(OBGroup g,int idx)
    {
        List<OBPath> pcs = (List<OBPath>)(Object)g.filterMembers("piece.*",true);
        for (int i = 0;i < pcs.size();i++)
            if (i != idx)
                pcs.get(i).hide();
        OBPath np = (OBPath)pcs.get(idx).copy();
        np.setFillColor(Color.WHITE);
        np.setStrokeColor(0);
        OBPath background = (OBPath) g.objectDict.get("background");
        //background.fillColor = currentSwatch().fillColor;
        background.parent.setMaskControl(np);
        PointF pt = convertPointFromControl(pcs.get(idx).position(),pcs.get(idx).parent);
        PointF offset = OB_Maths.DiffPoints(g.position(),pt);
        PointF destpos = positions.get(idx).position();
        PointF pos = OB_Maths.AddPoints(offset, destpos);
        g.setProperty("origpos",pos);
    }

    public OBPath currentSwatch()
    {
        return swatches.get(currNo % swatches.size());
    }

    public void setUpImage(String fileName)
    {
        OBPath swatch = currentSwatch();
        OBImage im = OBImageManager.sharedImageManager().imageForName(fileName);
        puzzle = (OBGroup) objectDict.get("puzzle");
        puzzlePieces = (List<OBPath>)(Object)puzzle.filterMembers("piece.*",true);
        OBPath background = (OBPath)puzzle.objectDict.get("background");
        background.setFillColor(swatch.fillColor());
        background.setStrokeColor(0);
        float scale = puzzle.bounds().height() / im.bounds().height();
        im.setScale(scale);
        im.setPosition(OB_Maths.locationForRect(0.5f, 0.5f, background.bounds()));
        im.setZPosition(1);
        background.parent.insertMember(im,0,"image") ;
        im.setProperty("name","image") ;
        List<RectF> prects = new ArrayList<>();
        for(OBPath p : puzzlePieces)
        {
            p.setZPosition(12);
            p.setFillColor(0);
            RectF r = convertRectFromControl(p.bounds,p) ;
            prects.add(r) ;
        }
        homeRects = prects;
        List<OBGroup>newpieces = new ArrayList<>();
        for(int i = 0;i < puzzlePieces.size();i++)
        {
            OBGroup g = (OBGroup) puzzle.copy();
            attachControl(g);
            maskImage(g,i) ;
            newpieces.add(g);
            g.setOpacity(0);
        }
        pieces = newpieces;
        for(OBControl c : puzzlePieces)
            c.hide() ;
        puzzle.setZPosition(11);
    }

    public void setSceneXX(String  scene)
    {
        animDone = false;
        if(firstTimeIn)
        {
            OBPath swatch = currentSwatch();
            OBPath picBack = (OBPath) objectDict.get("backrect");
            picBack.setFillColor(swatch.strokeColor());
            firstTimeIn = false;
        }
        deleteControls("oldpuzzle.*");
        if(objectDict.get("puzzle") != null)
        {
            objectDict.put("oldpuzzle",objectDict.get("puzzle"));
            objectDict.remove("puzzle");
            objectDict.get("oldpuzzle").setZPosition(12);
        }
        deleteControls("pos.*");
        deleteControls("puzzle.*");
        if(label != null)
            detachControl(label);
        String format = parameters.get("format") ;
        if(format != null)
            super.setSceneXX(format);
        else
            super.setSceneXX("puzzle9");
        currWord = words.get(currNo) ;
        positions = (List<OBPath>)(Object)sortedFilteredControls("pos_.*");
        positions = OBUtils.randomlySortedArray(positions);
        setUpImage(currWord);
        targets = (List<OBControl>)(Object)pieces;
        if(showText > 0)
        {
            OBWord rw = (OBWord) componentDict.get(currWord);
            String word;
            if(showText == SHOW_TEXT_INITIAL)
                //word = rw.firstSound() ;
                word = rw.syllables().get(0).phonemes.get(0).text;
            else
                word = rw.text ;
            setUpLabel(word);
            attachControl(label);
        }
        if(!preAssembled)
        {
            for(OBControl c : pieces)
            {
                c.setPosition((PointF) c.propertyValue("origpos"));
                //c.show() ;
                c.setOpacity(1);
            }
            puzzle.setOpacity(0);
            //animDone = true;
        }
    }

    public void setSceneb1()
    {
    }

    public void setSceneb2()
    {
    }

    public void setScenec()
    {
    }

    public void setUpLabel(String tx)
    {
        Typeface tf = OBUtils.standardTypeFace();
        label = new OBLabel(tx,tf,textSize);
        label.setColour(Color.BLACK);
        label.setPosition(textBox.position());
        label.hide() ;
    }

    public void openingAnim1() throws Exception
    {
        playSfxAudio("lineson",false);
        lockScreen();
        for(OBControl p : pieces)
            p.setOpacity(1);
        puzzle.setOpacity(0);
        unlockScreen();
        waitSFX();
    }

    public void openingAnim2() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        playSfxAudio("flyout",false);
        for(OBControl p : pieces)
        {
            PointF startpos = p.position();
            PointF endpos = (PointF) p.propertyValue("origpos");
            float offset = applyGraphicScale(40);
            if(endpos.x > startpos.x)
                offset = -offset;
            Path bez = OBUtils.SimplePath(startpos, endpos, offset);
            anims.add(OBAnim.pathMoveAnim(p,bez,false,0));
        }
        OBAnimationGroup.runAnims(anims,0.75f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        animDone = true;
    }

    public void doAudio(String  scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        audioLock = playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());

        if(firstTime)
            firstTime = false;
        else if(!animDone || !preAssembled)
        {
            //waitAudio();
            audioLock.lockWhenCondition(PROCESS_DONE);
            audioLock.unlock();

            lockScreen();
            objectDict.get("oldpuzzle").hide() ;
            OBPath swatch = currentSwatch();
            OBPath picBack = (OBPath) objectDict.get("backrect");
            picBack.setFillColor(swatch.strokeColor());
            unlockScreen();
            playSfxAudio("imageon",true);
        }
        if(!animDone && preAssembled)
        {
            waitForSecs(0.4f);
            openingAnim1();
            waitForSecs(0.4f);
            openingAnim2();
        }
    }

}
