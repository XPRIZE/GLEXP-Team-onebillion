package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.utils.OBImageManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 30/08/2017.
 */

public class OC_Sm3a extends OC_Sm3
{
    String initialText,initialSoundId;

    public void setSceneXX(String  scene)
    {
        for(OBControl c : pics)
            detachControl(c);
        for(int i = 0;i < 3;i++)
            lowlightRect((OBPath)picrects.get(i),~0);
        if(label != null)
            detachControl(label);
        Map<String,Object> currWordDict = wordSets.get(currNo);
        List<String> wds = (List<String>)currWordDict.get(SM3_WORDLIST);
        currWordID = wds.get(0);
        if(currWordDict.get(SM3_PREFIX) != null )
        {
            initialText = (String)currWordDict.get(SM3_PREFIX);
            initialSoundId = initialText;
        }
    else
        {
            OBWord rw =(OBWord )wordDict.get(currWordID);
            initialText = rw.phonemes().get(0).text;
            initialSoundId = null;
        }
        pics = new ArrayList<>();
        for(String s : wds)
        {
            OBWord rw = (OBWord) wordDict.get(s);
            OBImage im = OBImageManager.sharedImageManager().imageForName(rw.imageName);
            im.setScale(picScale);
            pics.add(im);
            im.setProperty("word",s);
        }
        int i = 0;
        screenPics = OBUtils.randomlySortedArray(pics);
        List screenWords = new ArrayList<>();
        for(OBImage img : screenPics)
        {
            img.setPosition(picrects.get(i).position());
            img.setZPosition(8);
            attachControl(img);
            img.hide();
            screenWords.add(img.propertyValue("word"));
            i++;
        }
        chosenWords = screenWords;
        hideControls("picr.*");
        setUpLabel(initialText);
        attachControl(label);
    }

    public void doIntro() throws Exception
    {
        showStuff();
        waitForSecs(0.3f);
        if(currentAudio("INTRO") != null)
        {
            if(!currentEvent().equals("b") || !needDemo)
            {
                playAudioQueuedScene("INTRO",true);
                waitForSecs(0.35f);
            }
        }
        if(currentAudio("INTRO2")!= null)
        {
            playAudioQueuedScene("INTRO2",true);
            waitForSecs(0.35f);
        }
    }

    public void playSound(String  s) throws Exception
    {
        String sound = s;
        if(!sound.startsWith("is_"))
            sound = "is_" + sound;
        playAudioQueued(Arrays.asList((Object)sound),true);
    }

    public void stutter() throws Exception
    {
        for(int i = 0;i < 3;i++)
        {
            if(initialSoundId != null)
                playSound(initialSoundId);
            else
            {
                playFirstSoundOfWordId(currWordID, (OBWord) wordDict.get(currWordID));
                waitAudio();
            }
            waitForSecs(0.25f);
        }
    }

    public String showAudioName()
    {
        return "graphon";
    }

    public void demoa() throws Exception
    {
        adjustCardsForDemo();
        waitForSecs(0.3f);
        showStuff();
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        RectF f = new RectF(picrects.get(2).frame());
        float boty = f.bottom;
        RectF pic2f = new RectF(f);
        f.bottom = (bounds().height());
        f.top = (boty);
        PointF destpt = OB_Maths.locationForRect(0.5f,0.5f, f);
        PointF startpt = pointForDestPoint(destpt,15);
        startpt.y = (bounds().height() + 5);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-0.8f,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.3f);
        nameItemsToken(0);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.1f,0.7f, f),-0.8f,true);
        waitForSecs(0.3f);
        String demos = "DEMO3";
        if(initialText.length()  > 1)
            demos = "DEMO4";
        playAudioQueuedScene(demos,true);
        showLabelWithColour(true);
        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.45f,0.75f, pic2f),-0.8f,true);
        waitForSecs(0.1f);
        movePointerForwards(applyGraphicScale(20),0.03f);
        playSfxAudio("touch",false);
        highlightRect((OBPath)picrects.get(2),DO_FILL|DO_STROKE);
        waitForSecs(0.02f);
        movePointerForwards(applyGraphicScale(-20),0.03f);
        playAudioQueuedScene("DEMO5",true);
        lowlightRect((OBPath)picrects.get(2),DO_FILL|DO_STROKE);
        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f, bounds()),-0.8f,true);

        label.setColour(Color.RED);
        stutter();
        waitForSecs(0.3f);
        highlightRect((OBPath)picrects.get(2),DO_FILL|DO_STROKE);
        playAudioQueued(Arrays.asList((Object)currWordID),true);
        waitForSecs(0.4f);

        thePointer.hide();
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO6",true);
        waitForSecs(0.4f);
        nextScene();
    }



    public void checkTarget(OBControl targ,PointF pt)
    {
        takeSequenceLockInterrupt(true);
        sequenceLock.unlock();
        if(theStatus  != STATUS_AWAITING_CLICK)
            return;
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        try
        {
            playSfxAudio("touch",true);
            PointF p = pics.get(0).position();
            if(targ.frame().contains(p.x,p.y))
            {
                highlightRect((OBPath)targ,DO_STROKE|DO_FILL);
                waitSFX();
                gotItRightBigTick(true);
                lowlightRect((OBPath)targ,DO_STROKE|DO_FILL);
                waitForSecs(0.35f);
                redInitial();
                stutter();
                waitAudio();
                waitForSecs(0.3f);
                highlightRect((OBPath)targ,DO_STROKE|DO_FILL);
                playAudioQueued(Arrays.asList((Object)currWordID) ,true);
                waitForSecs(0.9f);
                nextScene();
            }
            else
            {
                highlightRect((OBPath)targ,DO_ERROR);
                waitSFX();
                gotItWrongWithSfx();
                waitSFX();
                setReplayAudio(saverep);
                lowlightRect((OBPath)targ,DO_ERROR);
                playAudioQueuedScene("INCORRECT",true);
                setStatus(saveStatus);
                replayAudio();
            }
        }
        catch(Exception exception)
        {
        }

    }

}
