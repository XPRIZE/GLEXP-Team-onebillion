package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.controls.OBPresenter;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.maq.xprize.onecourse.hindi.utils.OBUtils.coalesce;

/**
 * Created by alan on 09/08/16.
 */
public class OC_Sm2 extends OC_Sm1
{
    String demoType;
    OBPresenter presenter;

    public void miscSetUp()
    {
        super.miscSetUp();
        demoType = coalesce(parameters.get("demotype"),"a2");
        String audioMode = parameters.get("audiomode");
        if(audioMode != null)
        {
            if (audioMode.equals("end"))
                mergeAudioScenesForPrefix("ALT");
            else if(audioMode.equals("middle"))
                mergeAudioScenesForPrefix("ALT2");
        }
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("restpos",new PointF(presenter.control.position().x,presenter.control.position().y));
        presenter.control.setRight(0);
        presenter.control.show();
    }

    public void setUpEvents()
    {
        events = new ArrayList<>();
        events.add("a0");
        if(needDemo)
        {
            if(demoType.equals("a1"))
                events.add("a1");
            else
                events.add("a2");
            events.add("a3");
        }
        else
            events.add("a2");
        int noscenes = Integer.parseInt(parameters.get("noscenes"));
        if(needDemo)
            noscenes--;
        List evs = new ArrayList<>(Arrays.asList("b","c","d"));
        while(evs.size() > noscenes)
            evs.remove(evs.size()-1);
        while(evs.size()  < noscenes)
            evs.add(evs.get(evs.size()-1));
        events.addAll(evs);
    }


    public void doMainXX() throws Exception
    {
        showStuff();
        waitForSecs(0.3f);
        if(currentAudio("INTRO") != null)
        {
            playAudioQueuedScene("INTRO", true);
            waitForSecs(0.35f);
        }
        nameItems();
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        List audio = currentAudio("PROMPT");
        if(audio != null)
        {
            waitForSecs(0.2f);
            playAudioQueued(OBUtils.insertAudioInterval(audio, 300), true);
            waitForSecs(0.2f);
        }
        waitForSecs(0.3f);
        playFirstSoundOfWordId(currWordID,(OBWord)wordDict.get(currWordID));
        audio = currentAudio("PROMPT2");
        if(audio != null)
        {
            waitAudio();
            waitForSecs(0.35f);
            playAudioQueued(audio,false);
        }
    }

    public void demoa0() throws Exception
    {
        waitForSecs(0.3f);
        showStuff();
        nextScene();
    }

    public void demoa1() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        presenter.moveHandToEarController(this);
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(1f);
        presenter.moveHandFromEarController(this);

        PointF leftBorder = convertPointFromControl(new PointF(),presenter.control.objectDict.get("front"));
        PointF currPos = presenter.control.position();
        PointF edgepos = OB_Maths.OffsetPoint(currPos,-leftBorder.x,0);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(2)),this);
        waitForSecs(0.2f);
        OBControl fr = presenter.control.objectDict.get("faceright");
        PointF dp = convertPointFromControl(new PointF(fr.width(),0),fr);
        PointF dp2 = convertPointFromControl(new PointF(),fr);
        PointF destpos = new PointF(-dp2.x , currPos.y);
        presenter.walk(destpos);
        nextScene();
    }
    public void demoa2() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        nextScene();
    }

    public void highlightRectFrame(OBPath rect)
    {
        rect.setStrokeColor(highlightswatch.strokeColor());
        rect.setLineWidth(highlightswatch.lineWidth());
    }

    public void lowlightRectFrame(OBPath rect)
    {
        rect.setStrokeColor(lowStroke);
        rect.setLineWidth(lowWidth);
    }

    public void highlightRect(OBPath rect)
    {
        rect.setFillColor(highlightswatch.fillColor());
        rect.setStrokeColor(highlightswatch.strokeColor());
        rect.setLineWidth(highlightswatch.lineWidth());
    }

    public void lowlightRect(OBPath rect)
    {
        rect.setFillColor(lowColour);
        rect.setStrokeColor(lowStroke);
        rect.setLineWidth(lowWidth);
    }

    public void nameItems() throws Exception
    {
        for(int i = 0;i < 3;i++)
        {
            highlightRectFrame(picrects.get(i));
            playAudioQueued(Arrays.asList((Object)chosenWords.get(i)),true);
            lowlightRectFrame(picrects.get(i));
            waitForSecs(0.4f);
        }
    }

    public void stutterWord(String wordID) throws Exception
    {
        for(int i = 0;i < 3;i++)
        {
            playFirstSoundOfWordId(wordID, (OBWord) wordDict.get(wordID));
            waitAudio();
            waitForSecs(0.25f);
        }
        playAudioQueued(Arrays.asList((Object)wordID) ,true);
    }
    public void demoa3() throws Exception
    {
        OBControl p3 = objectDict.get("picrect2");
        PointF startpos = OB_Maths.locationForRect(1,1,bounds());
        PointF destpos = OB_Maths.tPointAlongLine(0.6f,startpos,p3.bottomLeft());
        loadPointerStartPoint(startpos,destpos);
        movePointerToPoint(destpos,-1,true);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.65f);
        nameItems();
        waitForSecs(0.3f);
        movePointerForwards(applyGraphicScale(-30),-1);
        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.5f);

        playFirstSoundOfWordId(currWordID, (OBWord) wordDict.get(currWordID));
        waitAudio();
        waitForSecs(0.5f);

        int idx = chosenWords.indexOf(currWordID);

        movePointerToPoint(OB_Maths.locationForRect(0.6f, 0.7f,picrects.get(idx).frame()),-1,false);
        playAudioQueuedScene("DEMO3",true);

        movePointerToPoint(OB_Maths.OffsetPoint(thePointer.position(), 0, applyGraphicScale(150)),-1,true);
        highlightRect(picrects.get(idx));

        stutterWord(currWordID);
        waitForSecs(0.3f);
        lowlightRect(picrects.get(idx));

        thePointer.hide();
        waitForSecs(0.5f);
        clearOffOldThings();
        currNo++;
        nextScene();
    }

    public void setScenea1()
    {

    }
    public void setScenea2()
    {

    }
    public void setScenea3()
    {

    }

    public void setSceneb()
    {
        if(picrects.get(0).hidden())
            setSceneXX(currentEvent());
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
                    setScene(events.get(eventIndex));
                }
            });
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        highlightRect((OBPath)targ);
        try
        {
            playSfxAudio("touch",true);
            if(targ.frame.contains(pics.get(0).position().x, pics.get(0).position().y))
            {
                waitSFX();
                waitForSecs(0.35f);
                stutterWord(currWordID);
                waitAudio();
                waitForSecs(0.3f);
                gotItRightBigTick(true);
                waitAudio();
                waitForSecs(0.3f);
                lowlightRect((OBPath)targ);
                waitForSecs(0.3f);
                if(eventIndex < events.size()  - 1)
                    clearOffOldThings();
                currNo++;
                waitForSecs(0.3f);
                nextScene();
            }
            else
            {
                waitSFX();
                gotItWrongWithSfx();
                waitSFX();
                setReplayAudio(saverep);
                lowlightRect((OBPath)targ);
                setStatus(saveStatus);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                                                                                                                                                       public void run() throws Exception
                    {
                        long sttime = statusTime;
                        playAudioQueuedScene("INCORRECT",0.3f,true);
                        waitForSecs(0.3f);
                        if(sttime == statusTime)
                            playFirstSoundOfWordId(currWordID, (OBWord) wordDict.get(currWordID));
                    }
                });
            }
        }
        catch(Exception exception)
        {
        }

    }



}
