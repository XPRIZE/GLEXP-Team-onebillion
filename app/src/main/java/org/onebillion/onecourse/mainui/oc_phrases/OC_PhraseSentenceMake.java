package org.onebillion.onecourse.mainui.oc_phrases;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBReadingWord.WORD_SPEAKABLE;

/**
 * Created by alan on 14/12/17.
 */

public class OC_PhraseSentenceMake extends OC_PhraseSentence
{
    protected boolean showIntro;
    protected OBReadingWord hitWord;
    protected List incorrectAudio;
    protected OBConditionLock highlightLock;

    public void cleanUpScene()
    {
        if (paragraphs != null)
        {
            for(OBReadingPara para : paragraphs)
                for(OBReadingWord w : para.words)
                    if(w.label != null && w.label.parent != null)
                        w.label.parent.removeMember(w.label);
        }
        textBox.setPosition(textBoxOriginalPos);
    }

    public void changeLabelParents()
    {
        for(OBReadingWord w : words)
        {
            OBLabel l = w.label;
            PointF pos = convertPointFromControl(l.position(),textBox);
            textBox.removeMember(l);
            attachControl(l);
            l.setPosition(pos);
            l.setZPosition(50);
        }
    }

    List<OBReadingWord> WordsInSizeOrder(List words,final boolean desc)
    {
        List a = new ArrayList(words);
        Collections.sort(a, new Comparator<OBReadingWord>()
        {
            public int compare(OBReadingWord o1, OBReadingWord o2)
            {
                float w1 = o1.frame.width();
                float w2 = o2.frame.width();
                int result = (int)Math.signum(w1-w2);
                if(desc)
                    result = -result;
                return result;
            }
        });
        return a;
    }

    List<List> distributeLabels()
    {
        if(words.size()  <= 2)
        {
            List a = OBUtils.randomlySortedArray(words);
            return Arrays.asList(a,new ArrayList());
        }

        List arr1 = new ArrayList<>();
        List arr2 = new ArrayList<>();
        List<OBReadingWord> sizeWords = WordsInSizeOrder(words, true);
        float sz1 = 0,sz2 = 0;
        while(sizeWords.size() > 0)
        {
            OBReadingWord w = sizeWords.get(0);
            if(sz1 > sz2)
            {
                arr2.add(w);
                sz2 += w.frame.width();
            }
            else
            {
                arr1.add(w);
                sz1 += w.frame.width();
            }
            sizeWords.remove(0);
        }

        return Arrays.asList(OBUtils.randomlySortedArray(arr1),OBUtils.randomlySortedArray(arr2));
    }

    void positionLabels(List<OBReadingWord>wds,RectF f)
    {
        float totalWidth = 0;
        for(OBReadingWord w : wds)
            totalWidth += w.frame.width();
        float gapSize = (f.width() - totalWidth) /(wds.size() + 1);
        float left = gapSize + f.left;
        for(OBReadingWord w : wds)
        {
            OBLabel l = w.label;
            l.setTop(f.top);
            l.setLeft(left);
            left +=(l.width() + gapSize);
        }
    }

    public void positionMixedUpLabels(List<List>tuple)
    {
        List<OBReadingWord>arr1 = tuple.get(0),arr2 = tuple.get(1);
        RectF f = new RectF(objectDict.get("muddlebox").frame());
        positionLabels(arr1,f);
        f.top +=(lineHeightMultiplier * fontSize);
        positionLabels(arr2,f);
    }

    public void positionUnderline() throws Exception
    {
        OBControl ul = objectDict.get("underline");
        if(ul.hidden())
            playSfxAudio("line",false);
        OBLabel l = words.get(wordIdx).label;
        PointF pt = words.get(wordIdx).homePosition;
        RectF hf = new RectF(l.bounds());
        float left = pt.x - hf.width() / 2;
        float bottom = pt.y + hf.height() / 2;
        lockScreen();
        ul.setLeft(left);
        ul.setBottom(bottom);
        ul.show();
        unlockScreen();
    }

    public void repositionLabels()
    {
        for(OBReadingWord w : words)
        {
            OBLabel l = w.label;
            if (w.homePosition == null)
                w.homePosition = new PointF();
            w.homePosition.set(l.position());
            l.setProperty("homeframe",new RectF(l.frame()));
        }
        List<List> wordlines = distributeLabels();
        positionMixedUpLabels(wordlines);
        calcWordFrames();
        for(OBReadingWord w : words)
        {
            OBLabel l = w.label;
            PointF pf = new PointF();
            pf.set(l.position());
            l.setProperty("predragpos",pf);
        }
    }

    public void adjustTextPosition()
    {
        List wlines = wordExtents();
        if(wlines.size()  <=1)
            return;
        List<OBReadingWord> arr = (List) wlines.get(0);
        OBReadingWord w = arr.get(0);
        float centy = (w.frame).centerY();
        arr = (List) wlines.get(wlines.size() - 1);
        w = arr.get(0);
        float y2 = (w.frame).centerY();
        float midy = (centy + y2) / 2;
        float diff = centy - midy;
        if(diff != 0)
        {
            PointF pos = new PointF();
            pos.set(textBox.position());
            pos.y += diff;
            textBox.setPosition(pos);
            calcWordFrames();
        }
    }

    public void uncapitaliseWord(OBReadingWord rw)
    {
        OBLabel l = rw.label;
        float r = l.right();
        l.setProperty("right",r);
        String s = l.text();
        String lcs = s.toLowerCase();
        if(s.equals(lcs))
            return;
        PointF pos = l.position();
        rw.settings.put("ucstring",s);
        l.setProperty("ucposition",new PointF(pos.x,pos.y));
        l.setString(lcs);
        l.setPosition(pos);
        l.setRight(r);
        rw.homePosition = new PointF(l.position().x,l.position().y);
    }

    public void capitaliseWord(OBReadingWord rw)
    {
        OBLabel l = rw.label;
        float r = l.right();
        String ucs = (String) rw.settings.get("ucstring");
        if(ucs == null)
            return;
        PointF pos = l.position();
        float x = pos.x;
        float y = pos.y;
        l.setString(ucs);
        l.sizeToBoundingBox();
        l.setPosition(x,y);
        l.setRight(r);
    }

    public void setUpScene()
    {
        cleanUpScene();
        currComponentKey = componentList.get(currNo);
        Map currPhraseDict = componentDict.get(currComponentKey);
        String imageName = (String)currPhraseDict.get("imagename");
        if(mainPic != null)
            detachControl(mainPic);
        mainPic = loadImageWithName(imageName,new PointF(0.5f, 0.5f),boundsf());
        objectDict.put("mainpic",mainPic);
        mainPic.setZPosition(60);
        scalePicToBox();
        String ptext = (String) currPhraseDict.get("contents");
        OBReadingPara para = new OBReadingPara(ptext,1);
        paragraphs = Arrays.asList(para);
        List wds = new ArrayList<>();
        for(OBReadingPara p : paragraphs)
            for (OBReadingWord rw : p.words)
            {
                if ((rw.flags & WORD_SPEAKABLE) != 0)
                    wds.add(rw);
            }
        words = wds;

        layOutText();
        calcWordFrames();
        adjustTextPosition();
        changeLabelParents();
        uncapitaliseWord(words.get(0));

        repositionLabels();
        int i = 1;
        for(OBReadingPara p : paragraphs)
        {
            loadTimingsPara(p,getLocalPath(String.format("%s_%d.etpa",currComponentKey,i)),false);
            loadTimingsPara(p,getLocalPath(String.format("%s_%d.etpa",SlowVersion(currComponentKey,true),i)),true);
            i++;
        }

        mainPic.hide();
        wordIdx = 0;
        objectDict.get("underline").hide();
        for(OBReadingWord w : words)
            if (w.label != null)
                w.label.setRight(-5);
        for(OBReadingWord w : unspeakableWords() )
            if (w.label != null)
                w.label.hide();
    }

    public boolean newPicRequiredForScene(String scene)
    {
        return true;
    }

    public void setSceneXX(String scene)
    {
        if(newPicRequiredForScene(scene))
        {
            currNo++;
            setUpScene();
        }
    }

    public void clearOff()throws Exception
    {
        if(mainPic != null && !mainPic.hidden() )
        {
            playSfxAudio("alloff",false);
            lockScreen();
            mainPic.hide();
            for(OBReadingWord w : words)
            {
                if (w.label != null)
                    w.label.hide();
            }
            for(OBReadingWord w : unspeakableWords() )
            {
                if (w.label != null)
                    w.label.hide();
            }
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
        }
    }

    public void showPic()throws Exception
    {
        if(mainPic != null && mainPic.hidden() )
        {
            waitForSecs(0.3f);
            playSfxAudio("picon",false);
            lockScreen();
            mainPic.show();
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
        }
    }

    public void slideOnWords()throws Exception
    {
        playSfxAudio("slide",false);
        List arr = new ArrayList<>();
        for(OBReadingWord w : words)
        {
            OBLabel l = w.label;
            if (l != null)
            {
                PointF pos = (PointF) l.propertyValue("predragpos");
                OBAnim anim1 = OBAnim.moveAnim(pos,l);
                arr.add(anim1);
            }
        }
        OBAnimationGroup agp = new OBAnimationGroup();
        agp.applyAnimations(arr,0.5f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void doIntro()throws Exception
    {
        if(currentAudio("INTRO") != null && currentAudio("INTRO").size()  > 0)
        {
            playAudioScene("INTRO",0,true);
            waitForSecs(0.3f);
        }
        showPic();
        waitForSecs(0.3f);
        slideOnWords();
        waitForSecs(0.2f);
        positionUnderline();
        waitForSecs(0.3f);
        waitSFX();
        if(currentAudio("INTRO") != null && currentAudio("INTRO").size()  > 1)
        {
            playAudioScene("INTRO",1,true);
            waitForSecs(0.35f);
        }
    }

    public String objectAudio()
    {
        return String.format("%s_1",currComponentKey);
    }

    public void playObjectAudioWait(boolean wait) throws OBUserPressedBackException
    {
        playAudioQueued(Arrays.asList((Object)objectAudio()),wait);
    }

    public void doReminder(final long stt,List audio,final float secs)
    {
        if(statusTime != stt)
            return;
        OBControl ul = objectDict.get("underline");
        try
        {
            long token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                if(audio != null)
                {
                    playAudioQueued(Arrays.asList(audio.get(0)),true);
                    checkSequenceToken(token);
                    waitAndCheck(stt,0.1f,2);
                    playObjectAudioWait(true);
                    checkSequenceToken(token);
                    playAudioQueued(Arrays.asList(audio.get(1)),true);
                }
                for(int i = 0;i < 3;i++)
                {
                    ul.hide();
                    waitAndCheck(stt,0.1f,3);
                    ul.show();
                    waitAndCheck(stt,0.1f,3);
                    checkSequenceToken(token);
                }
                OBUtils.runOnOtherThreadDelayed(secs,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        doReminder(stt,null,secs);
                    }
                });
            }
        }
        catch(Exception exception)
        {
        }
        ul.show();
        sequenceLock.unlock();
    }

    public void endBody()
    {
        final long stt = statusTime;
        OBUtils.runOnOtherThreadDelayed(5,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                doReminder(stt,currentAudio("PROMPT.REMINDER"),7);
            }
        });
    }

    public void doMainXX() throws Exception
    {
        setReplayAudio(Arrays.asList((Object)objectAudio()));
        incorrectAudio = currentAudio("INCORRECT");
        if(mainPic.hidden() )
        {
            doIntro();
        }
        playObjectAudioWait(true);
        if(currentAudio("PROMPT") != null)
        {
            waitForSecs(0.4f);
            playAudioQueuedScene("PROMPT",false);
            waitForSecs(0.3f);
        }
    }

    public long switchStatus(String  scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    List<OBLabel>unspeakables()
    {
        List visibles = new ArrayList<>();
        for(OBReadingWord w : unspeakableWords() )
        {
            String s = w.label.text();
            if(s.matches("\\S"))
                visibles.add(w.label);
        }
        return visibles;
    }

    public List showUnspeakablesColour(int col)
    {
        List<OBLabel> visibles = unspeakables();
        if(visibles.size()  > 0)
        {
            lockScreen();
            for(OBLabel l : visibles)
            {
                if(col != 0)
                    l.setColour(col);
                l.show();
            }
            unlockScreen();
        }
        return visibles;
    }

    public void nextShow() throws Exception
    {
        if(showUnspeakablesColour(0) != null)
        {
            playSfxAudio("puncon",true);
            waitForSecs(0.2f);
        }
    }

    public void nextObj()throws Exception
    {
        wordIdx += 1;
        if(wordIdx >= words.size() )
        {
            waitAudio();
            waitForSecs(0.2f);
            nextShow();
            currPara = 0;
            readPage();
            waitForSecs(0.4f);
            gotItRightBigTick(true);
            waitForSecs(1f);
            nextScene();
        }
        else
        {
            final OBConditionLock lck = highlightLock = new OBConditionLock(PROCESS_NOT_DONE);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    lck.lockWhenCondition(PROCESS_DONE);
                    lck.unlock();
                    positionUnderline();
                }
            });
            switchStatus(currentEvent());
            final long stt = statusTime;
            waitAudio();
            waitForSecs(0.2f);
            lck.lock();
            lck.unlockWithCondition(PROCESS_DONE);
            OBUtils.runOnOtherThreadDelayed(5,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    doReminder(stt,null,7);
                }
            });
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
        {
            if(wordIdx > 0)
                try
                {
                    clearOff();
                }
                catch(Exception e)
                {
                }
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    setScene(events.get(eventIndex));
                }
            });
        }
    }

    public boolean correctWord(OBReadingWord w)
    {
        return w.text.compareToIgnoreCase(words.get(wordIdx).text) == 0;
    }

    public void swapLabelsIfNecessary(OBReadingWord hw1,OBReadingWord w2)
    {
        if(hw1 == w2)
            return;
        OBLabel templ = hw1.label;
        hw1.label = (w2.label);
        w2.label = (templ);
        hw1.frame = (hw1.label.frame);
        w2.frame = (w2.label.frame);
        hitWord = w2;
    }

    public RectF targetFrame()
    {
        RectF f = new RectF(objectDict.get("underline").frame());
        RectF lf = hitWord.label.frame();
        float w = f.width();
        f.left -= w / 2;
        f.right +=  w / 2;
        float h = lf.height();
        f.top -= h / 2;
        f.bottom += h / 2;
        return f;
    }

    public void clearIncorrectAudio()
    {

    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            target = null;
            if(correctWord(hitWord) && targetFrame().intersects(targetFrame() , hitWord.label .frame()))
            {
                swapLabelsIfNecessary(hitWord,words.get(wordIdx));
                objectDict.get("underline").hide();
                moveObjects(Arrays.asList((OBControl)hitWord.label),hitWord.homePosition,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                hitWord.label.setZPosition(hitWord.label.zPosition() - 30);
                speakWordAsPartial(hitWord,currComponentKey,false);
                nextObj();
                return;
            }
            float brtopy = objectDict.get("bottomrect").top();
            if(!correctWord(hitWord) || hitWord.label.position().y < brtopy)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        gotItWrongWithSfx();
                        waitForSecs(0.2f);
                        List arr = new ArrayList<>();
                        arr.addAll(incorrectAudio);
                        arr.add(200);
                        arr.add(objectAudio());
                        playAudioQueued(arr,false);
                        clearIncorrectAudio();
                    }
                });
            }
            OBControl targ = hitWord.label;
            PointF destpt = (PointF)targ.propertyValue("predragpos");
            moveObjects(Arrays.asList(targ),destpt,-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
            targ.setZPosition(targ.zPosition() - 30);
            switchStatus(currentEvent());
        }
        catch(Exception exception)
        {
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }


    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }

    public void checkDragTargetPoint(PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        if(highlightLock != null)
        {
            highlightLock.lock();
            highlightLock.unlockWithCondition(PROCESS_DONE);
        }
        target = hitWord.label;
        target.setZPosition(target.zPosition() + 30 + currNo);
        dragOffset = OB_Maths.DiffPoints(target.position(), pt);
    }

    public Object findTarget(PointF pt)
    {
        if(objectDict.get("bottomrect").frame().contains(pt.x,pt.y))
            for(OBReadingWord w : words)
            {
                if((w.label != null) && w.label.frame().contains(pt.x, pt.y))
                    return w;
            }
        return null;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_WAITING_FOR_DRAG)
        {
            hitWord = (OBReadingWord) findTarget(pt);
            if(hitWord != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkDragTargetPoint(pt);
                    }
                });
            }
        }
    }

}
