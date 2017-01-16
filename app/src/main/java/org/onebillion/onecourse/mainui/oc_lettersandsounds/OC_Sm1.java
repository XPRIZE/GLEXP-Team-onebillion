package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.PointF;
import android.util.Log;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 09/08/16.
 */
public class OC_Sm1 extends OC_Wordcontroller
{
    public Map<String,OBPhoneme> wordDict;
    public List<String> words;
    public OBPath highlightswatch;
    public float picScale;
    public String currWordID;
    public List<OBImage>pics = new ArrayList<>();
    public List<OBPath>picrects;
    public int lowColour,lowStroke;
    public float lowWidth;
    public List<OBImage>screenPics;
    public List<String>chosenWords;

    public void miscSetUp()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = OBUtils.randomlySortedArray(Arrays.asList(ws.split(",")));
        currNo = 0;
        highlightswatch = (OBPath)objectDict.get("highlightswatch");
        needDemo = false;
        if(parameters.get("demo") != null && parameters.get("demo").equals("true"))
            needDemo = true;
        OBControl pic = objectDict.get("pic");
        picScale = pic.scale();
        deleteControls("pic");
        picrects = (List<OBPath>)(Object)sortedFilteredControls("picrect.*");
        targets = (List<OBControl>)(Object)picrects;
        lowColour = picrects.get(0).fillColor();
        lowStroke = picrects.get(0).strokeColor();
        lowWidth = picrects.get(0).lineWidth();
        float hiwidth = highlightswatch.lineWidth();
        for (OBPath p : picrects)
        {
            p.setLineWidth(hiwidth);
            p.sizeToBoundingBoxIncludingStroke();
            p.setLineWidth(lowWidth);
        }
    }

    public void setUpEvents()
    {
        events = new ArrayList<>();
        if(needDemo)
        {
            events.add("a");
            events.add("b1");
        }
        else
            events.add("b2");
        events.add("c");
        events.add("d");
        int noscenes = Integer.parseInt(parameters.get("noscenes"));
        while(events.size()  > noscenes)
            events.remove(events.size() - 1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));

    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        setUpEvents();
        doVisual(currentEvent());
    }

    public List<String> wordsNotStartingWith(int nowords,String sound)
    {
        List<String> result = new ArrayList<>();
        List<String> wids = OBUtils.VeryRandomlySortedArray(words);
        for(String wid : wids)
        {
            OBWord rw =(OBWord )wordDict.get(wid);
            if(!rw.phonemes().get(0).text.equals(sound))
            {
                result.add(wid);
                if(result.size()  == nowords)
                    return result;
            }
        }
        return result;
    }

    public void setSceneXX(String  scene)
    {
        for(OBControl c : pics)
            detachControl(c);
        currWordID = words.get(currNo);
        OBWord rw = (OBWord) wordDict.get(currWordID);
        pics = new ArrayList<>();
        OBImage im = OBImageManager.sharedImageManager().imageForName(rw.imageName);
        im.setScale(picScale);
        im.setProperty("word",currWordID);
        Log.i("%",currWordID);
        pics.add(im);

        String firstsound = rw.phonemes().get(0).text;
        List<String> wds = wordsNotStartingWith(2,firstsound);
        for(String s : wds)
        {
            Log.i("%",s);
            rw = (OBWord) wordDict.get(s);
            im = OBImageManager.sharedImageManager().imageForName(rw.imageName);
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
    }

    public void showStuff() throws Exception
    {
        if(picrects.get(0).hidden() )
        {
            waitForSecs(0.3f);
            playSfxAudio("splat",false);
            lockScreen();
            for(OBImage pic : pics)
                pic.show();
            showControls("picr.*");
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.2f);
        }
    }

    public void _replayAudio()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    long stt = statusTime;
                    playAudioQueued(_replayAudio,true);
                    if(stt == statusTime)
                    {
                        OBWord crw = (OBWord) wordDict.get(words.get(currNo));
                        playFirstSoundOfWordId(words.get(currNo),crw);
                    }
                }
                catch(Exception exception)
                {
                }
            }
        });
    }

    public void doMainXX() throws Exception
    {
        showStuff();
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        List audio = currentAudio("PROMPT");
        if(audio != null)
        {
            playAudioQueued(OBUtils.insertAudioInterval(audio, 300));
            waitForSecs(0.1);
            waitAudio();
        }
        waitForSecs(0.3f);
        playFirstSoundOfWordId(currWordID,(OBWord)wordDict.get(currWordID));
    }

    public void endBody()
    {
        List reminderAudio = currentAudio("PROMPT.REMINDER");
        if(reminderAudio != null)
        {
            try
            {
                final long sttime = statusTime;
                waitForSecs(0.3f);
                waitAudio();
                if(sttime == statusTime)
                    reprompt(sttime, reminderAudio, 6, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            if(sttime == statusTime)
                            {
                                playFirstSoundOfWordId(currWordID, (OBWord) wordDict.get(currWordID));
                            }
                        }
                    });
            }
            catch(Exception e)
            {
            }
        }
    }

    public void highlightRect(OBPath rect)
    {
        rect.setFillColor(highlightswatch.fillColor());
    }

    public void lowlightRect(OBPath rect)
    {
        rect.setFillColor(lowColour);
    }

    public void clearOffOldThings() throws Exception
    {
        playSfxAudio("popoff",false);
        lockScreen();
        for(OBImage pic : pics)
            pic.hide();
        hideControls("picr.*");
        unlockScreen();
    }


    public void adjustCardsForDemo()
    {
        if(picrects.get(1).frame().contains(pics.get(0).position().x,pics.get(0).position().y))
            return;

        List<OBImage>xorderedpics = new ArrayList<>(pics);
        Collections.sort(xorderedpics, new Comparator<OBImage>()
        {
            @Override
            public int compare (OBImage obj1, OBImage obj2)
            {
                if(obj1.position().x < obj2.position().x)
                    return -1;
                if(obj1.position().x > obj2.position().x)
                    return 1;
                 return 0;
            }
        });

        lockScreen();
        PointF temppos = new PointF(pics.get(0).position().x,pics.get(0).position().y);
        pics.get(0).setPosition(xorderedpics.get(1).position());
        xorderedpics.get(1).setPosition(temppos);
        unlockScreen();
    }

    public void demoa()throws Exception
    {
        adjustCardsForDemo();
        waitForSecs(0.3f);
        showStuff();
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        PointF destpt = OB_Maths.locationForRect(0.6f,0.7f, picrects.get(1).frame);
        PointF startpt = pointForDestPoint(destpt,15);
        startpt.y = (bounds().height() + 5);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.3f, startpt, destpt),-0.8f,true);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        playFirstSoundOfWordId(currWordID,(OBWord )wordDict.get(currWordID));
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.6f, startpt, destpt),-0.8f,true);
        waitForSecs(0.2f);
        playAudioScene("DEMO",2,true);
        movePointerToPoint(destpt,-0.4f,true);
        highlightRect((OBPath)picrects.get(1));
        playSfxAudio("touch",false);
        waitForSecs(0.05f);
        movePointerForwards(applyGraphicScale(-60),-1);
        waitSFX();
        waitForSecs(0.2f);
        playFirstSoundOfWordId(currWordID,(OBWord )wordDict.get(currWordID));
        waitAudio();
        waitForSecs(0.4f);
        playAudioQueued(Collections.singletonList((Object)currWordID) ,true);
        waitForSecs(0.3f);

        lowlightRect((OBPath)picrects.get(1));
        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
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
                    clearOffOldThings();
                    setScene(events.get(eventIndex));
                }
            });
    }

    public void checkTarget(final OBControl targ,PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        highlightRect((OBPath)targ);
        try
        {
            if(targ.frame.contains(pics.get(0).position().x, pics.get(0).position().y))
            {
                playSfxAudio("touch",true);
                playFirstSoundOfWordId(currWordID,(OBWord )wordDict.get(currWordID));
                waitAudio();
                waitForSecs(0.4f);
                playAudioQueued(Collections.singletonList((Object)currWordID) ,true);
                waitForSecs(0.3f);
                gotItRightBigTick(true);
                lowlightRect((OBPath)targ);
                currNo++;
                nextScene();
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
                        final long sttime = statusTime;
                        playAudioQueuedScene("INCORRECT",0.3f,true);
                        waitForSecs(0.3f);
                        waitAudio();
                        if(sttime == statusTime)
                            playFirstSoundOfWordId(currWordID, (OBWord) wordDict.get(currWordID));
                    }
                });
                OBUtils.runOnOtherThreadDelayed(0.2f,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        lowlightRect((OBPath)targ);
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
