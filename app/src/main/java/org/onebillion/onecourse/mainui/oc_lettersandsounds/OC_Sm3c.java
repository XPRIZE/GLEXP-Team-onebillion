package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 30/08/2017.
 */

public class OC_Sm3c extends OC_Sm3
{
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
        setUpLabel(wordDict.get(currWordID).text);
        attachControl(label);
    }

    public void nameItemsToken(long token)
    {

    }

    public void _replayAudio()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                playAudioQueued(_replayAudio,true);
            }
        });
    }

    public void doMainXX() throws Exception
    {
        doIntro();
        nameItemsToken(0);
        setReplayAudio(new ArrayList(currentAudio("PROMPT.REPEAT")));
        List audio = currentAudio(substituteAudio("PROMPT"));
        if(audio != null)
        {
            waitForSecs(0.2f);
            playAudioQueuedScene(substituteAudio("PROMPT"),0.3f,true);
            waitForSecs(0.2f);
        }
        showLabelWithColour(false);
        waitForSecs(0.3f);

        audio = currentAudio("PROMPT2");
        if(audio != null)
        {
            waitAudio();
            waitForSecs(0.35f);
            playAudioQueued(audio,false);
        }
    }

    public void demoa()throws Exception
    {
        adjustCardsForDemo();
        waitForSecs(0.3f);
        showStuff();
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        RectF f = picrects.get(2) .frame();
        float boty = f.bottom;
        RectF pic2f = f;
        f.bottom = (bounds().height());
        f.top = (boty);
        PointF destpt = OB_Maths.locationForRect(0.5f,0.5f, f);
        PointF startpt = pointForDestPoint(destpt,15);
        startpt.y = (bounds().height() + 5);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-0.8f,true);
        waitForSecs(0.3f);

        playAudioQueuedScene("DEMO2",true);
        showLabelWithColour(false);
        waitForSecs(0.3f);

        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.45f,0.75f, pic2f),-0.8f,true);
        waitForSecs(0.1f);
        movePointerForwards(applyGraphicScale(20),0.03f);
        playSfxAudio("touch",false);
        highlightRect((OBPath)picrects.get(2),DO_FILL|DO_STROKE);
        waitForSecs(0.02f);
        movePointerForwards(applyGraphicScale(-20),0.03f);
        playAudioQueuedScene("DEMO3",true);
        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f, boundsf()),-0.8f,true);

        highlightAndSpeakSylls(currWordID,true);

        waitForSecs(0.3f);
        waitForSecs(0.4f);

        thePointer.hide();
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO4",true);
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
                waitForSecs(0.35f);
                highlightAndSpeakSylls(currWordID,true);

                waitAudio();
                waitForSecs(1.1f);
                nextScene();
            }
            else
            {
                highlightRect((OBPath)targ,DO_ERROR);
                waitSFX();
                gotItWrongWithSfx();
                waitSFX();
                setReplayAudio(saverep);
                lowlightRect((OBPath)targ);
                playAudioQueuedScene("INCORRECT",true);
                setStatus(saveStatus);
            }
        }
        catch(Exception exception)
        {
        }
    }


}
