package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 05/07/16.
 */
public class OC_Puzzle extends OC_Wordcontroller
{
    static final int SHOW_TEXT_NONE = 0,
    SHOW_TEXT_INITIAL = 1,
    SHOW_TEXT_WORD =2;
    final static int  MSE_DN =0,
        MSE_UP =1;

    List<String> words;
    String currWord;
    List<OBGroup> pieces;
    List<OBControl> positions;
    List<OBPath> puzzlePieces,swatches;
    List<RectF> homeRects;
    OBGroup puzzle;
    boolean gotoStage2;
    float textSize;
    OBControl textBox;
    OBLabel label;
    Map<String,String> wordDict;
    Map<String,OBPhoneme> componentDict;

    OBConditionLock finishLock;
    boolean animDone,preAssembled,firstTime;
    int showText;
    OBConditionLock audioLock;
    boolean firstTimeIn;

    public void miscSetUp()
    {
        firstTimeIn = true;
        componentDict = OBUtils.LoadWordComponentsXML(true);

        String s = eventAttributes.get("textsize");
        if (s != null)
            textSize = applyGraphicScale(Float.parseFloat(s));
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        currNo = 0;
        finishLock = new OBConditionLock();
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
        OBControl np = pcs.get(idx).copy();
        np.setFillColor(Color.WHITE);
        //np.setStrokeColor(0);
        OBControl background = g.objectDict.get("background");
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
            OBControl picBack = objectDict.get("backrect");
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
        positions = (List<OBControl>)(Object)sortedFilteredControls("pos_.*");
        positions = OBUtils.randomlySortedArray(positions);
        OBWord rw = (OBWord) componentDict.get(currWord);
        setUpImage(rw.imageName);
        targets = new ArrayList<>();
        targets.addAll(pieces);
        if(showText > 0)
        {
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
            OBControl picBack = objectDict.get("backrect");
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

    public void nextObj() throws Exception
    {
        if(targets.size()  == 0)
        {
            waitAudio();
            stage2();
        }
        else
        {
            switchStatus(currentEvent());
        }
    }

    public void stage2() throws Exception
    {
        finishLock.lockWhenCondition(MSE_UP);
        finishLock.unlock() ;
        lockScreen();
        puzzle.show() ;
        if (label != null)
        {
            label.setOpacity(0);
            label.show() ;
        }
        unlockScreen();
        waitForSecs(0.3f);

        playSfxAudio("linesoff",false);
        lockScreen();
        for(OBControl p : pieces)
            detachControl(p);
        puzzle.setOpacity(1);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);

        String word = currWord;
        if(showText == SHOW_TEXT_NONE)
        {
            playAudio(word);
        }
        else
        {
            playSfxAudio("letteron",false);
            lockScreen();
            label.setOpacity(1);
            label.show() ;
            unlockScreen();
            waitSFX();
            waitForSecs(0.3f);
            if(showText == SHOW_TEXT_INITIAL)
            {
                highlightLabel(label,true) ;
                playFirstSoundOfWordId(currWord,(OBWord)componentDict.get(currWord));
                waitForSecs(0.2f);
                waitAudio();
                waitForSecs(0.3f);
                playAudio(word);
                waitAudio();
                highlightLabel(label,false) ;
            }
            else
            {
                OBWord rw = (OBWord) componentDict.get(currWord);
                rw.properties.put("label",label);
                highlightAndSpeakSyllablesForWord(rw);
            }
        }

        waitAudio();
        waitForSecs(1f);
        currNo++;
        nextScene();
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        playAudioQueued((List<Object>)(Object)currentAudio("DEMO"),true);
        waitForSecs(0.4f);
        if(!animDone && preAssembled)
        {
            openingAnim1();
            waitForSecs(0.3f);
            openingAnim2();
        }
        nextScene();
    }

    public void demoa1() throws Exception
    {
        demoa();
    }

    public void demoa2() throws Exception
    {
        demoa();
    }

    public void demob() throws Exception
    {
        waitForSecs(0.5f);
        OBControl piece = pieces.get(pieces.size()-1) ;
        PointF destpt = OB_Maths.locationForRect(0.9f, 0.9f, piece.frame());
        PointF startpt = new PointF(destpt.x,destpt.y);
        startpt.y = (bounds().height() + 1);
        loadPointerStartPoint(startpt,destpt);
        movePointerForwards(applyGraphicScale(60),-1) ;
        List<String> aud = currentAudio("DEMO");
        playAudioQueued(Collections.singletonList((Object)aud.get(0)),true);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.3f);
        piece.setZPosition(13);
        moveObjects(Arrays.asList(piece,thePointer),puzzle.position(),-0.8f,OBAnim.ANIM_EASE_IN_EASE_OUT) ;
        piece.setZPosition(12);
        targets.remove(piece);
        playSfxAudio("snap",false);
        waitForSecs(0.02f);
        movePointerForwards(applyGraphicScale(-100),0.1f) ;
        waitSFX();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        playAudioQueued(Collections.singletonList((Object)aud.get(1)),true);
        nextScene();
    }

    public void demob1() throws Exception
    {
        demob();
    }

    public void demob2() throws Exception
    {
        demob();
    }

    public void touchUpAtPoint(PointF pto,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            setStatus(STATUS_CHECKING);
            final PointF pt = (PointF)target.propertyValue("origpos");
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    moveObjects(Collections.singletonList(target),pt,-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
                    switchStatus(currentEvent());
                    target.setZPosition(2);
                    target = null;
                }
            });
            return;
        }
        finishLock.lock();
        finishLock.unlockWithCondition(MSE_UP);
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            PointF newpos = OB_Maths.AddPoints(pt, dragOffset);
            target.setPosition(newpos);
            int idx = pieces.indexOf(target);
            RectF r = homeRects.get(idx);
            if(r.contains(pt.x, pt.y))
            {
                setStatus(STATUS_CHECKING);
                targets.remove(target);
                final OBControl c = target;
                c.setZPosition(13);
                final OBSectionController fthis = this;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        try
                        {
                            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(puzzle.position(),c)),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,fthis);
                            c.setZPosition(12);
                            playSfxAudio("snap",false);
                            nextObj();
                        }
                        catch(Exception exception)
                        {
                        }
                    }
                });
            }


        }
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        targ.setZPosition(13);
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
        finishLock.lock();
        finishLock.unlockWithCondition(MSE_DN);
    }

    public Object findTarget(PointF pt)
    {
        for(OBGroup g : (List<OBGroup>)(Object)targets)
        {
            OBGroup gp = g.objectDict.get("background").parent;
            OBPath mask =(OBPath)gp.maskControl;
            PointF lpt = convertPointToControl(pt,gp);
            if(mask.frame.contains(lpt.x, lpt.y))
            {
                lpt.x -= mask.frame.left;
                lpt.y -= mask.frame.top;
                if(mask.alphaAtPoint(lpt.x,lpt.y) > 0.0)
                    return g;
            }
        }
        return null;
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
