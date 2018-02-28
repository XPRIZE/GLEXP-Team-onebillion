package org.onebillion.onecourse.mainui.oc_phrases;

import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.utils.*;
import org.onebillion.onecourse.controls.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBReadingWord.WORD_SPEAKABLE;

/**
 * Created by alan on 13/12/17.
 */

public class OC_PhraseSentenceRead extends OC_PhraseSentence
{
    final static int STATUS_ALMOST_FINISHED = 9998;
    final static int FLASH_DELAY = 5;
    protected boolean showIntro;
    protected boolean lrDemoRequired;

    public void cleanUpScene()
    {
        if (paragraphs != null)
        {
            for(OBReadingPara para : paragraphs)
                for(OBReadingWord w : para.words)
                    if(w.label != null)
                        w.label.parent.removeMember(w.label);
        }
        textBox.setPosition(textBoxOriginalPos);
    }

    public void adjustTextPosition()
    {
        List wlines = wordExtents();
        if(wlines.size() <= 1)
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
        mainPic.setZPosition(objectDict.get("textbox").zPosition() -0.01f);
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
        int i = 1;
        for(OBReadingPara p : paragraphs)
        {
            loadTimingsPara(p,getLocalPath(String.format("%s_%d.etpa",currComponentKey,i)),false);
            loadTimingsPara(p,getLocalPath(String.format("%s_%d.etpa",SlowVersion(currComponentKey,true),i)),true);
            i++;
        }

        mainPic.hide();
        textBox.hide();
        wordIdx = 0;
        setUpDecorationForWord(words.get(wordIdx));

    }

    public void replayAudio()
    {
        if(theStatus  != STATUS_ALMOST_FINISHED)
        {
            super.replayAudio();
            return;
        }
        setStatus(STATUS_BUSY);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    finishScene(false);
                }
                catch(Exception exception)
                {
                }

            }
        });

    }

    public void doIntro() throws Exception
    {
        showPic();
        waitForSecs(0.3f);
        if(currentAudio("INTRO") != null && currentAudio("INTRO").size() > 0)
        {
            playAudioScene("INTRO",0,true);
            waitForSecs(0.3f);
        }
        boolean showedWords = showWords();
        waitForSecs(0.2f);
        if(currentAudio("INTRO") != null && currentAudio("INTRO").size() > 1)
        {
            playAudioScene("INTRO",1,true);
            waitForSecs(0.35f);
        }
        else if(showedWords)
            waitForSecs(0.5f);
    }

    public void doMainXX() throws Exception
    {
        doIntro();
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",false);
        waitForSecs(0.3f);
        showWordEtc();
    }

    void doRem(final long stt)
    {
        doReminder(stt,currentAudio("PROMPT.REMINDER"));
    }

    public void endBody()
    {
        //doReminder(switchStatus(currentEvent()),currentAudio("PROMPT.REMINDER"));
        doRem(switchStatus(currentEvent()));
    }

    public void stage2()
    {
        currPara = 0;
        readPage();
    }

    public void presenterDemo() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(1.5f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.5f);
        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = presenter.control.position();
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds().width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(2)),this);
        waitForSecs(0.2f);
        OBControl faceright = presenter.control.objectDict.get("faceright");
        PointF lp = presenter.control.convertPointFromControl(new PointF(0, 0),faceright);
        PointF destpos = new PointF(boundsf().width() + lp.x + 1f, currPos.y);
        presenter.walk(destpos);
        detachControl(presenter.control);
        presenter = null;
        nextScene();
    }

    public void lrDemo(String scene) throws Exception
    {
        List<List>wordExtents = wordExtents();
        List<OBReadingWord>warray = wordExtents.get(0);
        RectF f = warray.get(0).frame;
        float x = f.centerX();
        float y = f.bottom + applyGraphicScale(20);
        PointF destpoint = new PointF(x, y);
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        Map<String, List> eventd = (Map<String, List>) audioScenes.get(scene);
        List aud = eventd.get("DEMO");
        playAudioQueued(Arrays.asList(aud.get(0)),true);
        waitForSecs(0.4f);
        playAudioQueued(Arrays.asList(aud.get(1)),true);

        while(wordExtents.size()  > 0)
        {
            warray = wordExtents.get(0);
            f = warray.get(0).frame;
            destpoint = OB_Maths.locationForRect(0.1f, 1f, f);
            if(!(destpoint.equals(thePointer.position())))
                movePointerToPoint(destpoint,-0.8f,true);
            f = warray.get(1).frame;
            PointF destpoint2 = OB_Maths.locationForRect(0.9f, 1f, f);
            if(!destpoint.equals(destpoint2))
                movePointerToPoint(destpoint2,-0.6f,true);
            wordExtents.remove(0);
        }
        waitAudio();
        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.4f);
    }

    public void considerLrDemo(boolean primary)throws Exception
    {

    }
    public void finishScene(boolean primary)throws Exception
    {
        stage2();
        considerLrDemo(primary);
        long st = setStatus(STATUS_ALMOST_FINISHED);
        waitForSecs(1f);
        if(statusChanged(st))
            return;
        setStatus(STATUS_BUSY);

        if(eventIndex < events.size()  - 1)
            clearOff();
        nextScene();
    }

    public void clearOff()throws Exception
    {
        if(!mainPic.hidden() )
        {
            playSfxAudio("alloff",false);
            lockScreen();
            mainPic.hide();
            textBox.hide();
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
        }
    }

    public void nextWord() throws Exception
    {
        wordIdx++;
        if(wordIdx >= words.size() )
        {
            waitForSecs(0.4f);
            lockScreen();
            wordback.hide();
            wordback2.hide();
            unlockScreen();

            finishScene(true);

        }
        else
        {
            showWordEtc();
            doReminder(switchStatus(currentEvent()),null);
        }
    }

    public void showWordEtc()
    {
        lockScreen();
        setUpDecorationForWord(words.get(wordIdx));
        wordback.show();
        wordback2.show();
        unlockScreen();
    }


    public void doReminder(final long st,final List audio)
    {
        OBUtils.runOnOtherThreadDelayed(FLASH_DELAY,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(audio != null && st == statusTime)
                    playAudioQueued(audio);
                remindBox(st,FLASH_DELAY);
            }
        });
    }

    public void checkTarget(OBReadingWord rw,PointF pt)
    {
        try
        {
            highlightAndSpeakWord(rw,true);
            nextWord();
        }
        catch(Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        if(wordIdx >= 0 && wordIdx < words.size() )
        {
            OBLabel lab = words.get(wordIdx).label;
            if(finger(0,2,Arrays.asList((OBControl)lab),pt) != null)
                return words.get(wordIdx);
        }
        return null;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
        {
            final Object obj = findTarget(pt);
            if(obj != null && obj instanceof OBReadingWord)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget((OBReadingWord)obj,pt);
                    }
                });
            }
        }
    }

}
