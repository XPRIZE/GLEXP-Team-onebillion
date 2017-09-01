package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 10/03/2017.
 */

public class OC_Sm3 extends OC_Sm2
{
    public final String SM3_PREFIX = "prefix";
    public final String SM3_WORDLIST = "wordlist";
    public final int DO_FILL = 1,
        DO_STROKE = 2,
        DO_ERROR = 4;

    OBLabel label;
    float textSize;
    OBControl textBox;
    List<Map> wordSets;

    public List<Map>wordSets(List<String> wordLists)
    {
        List<Map>wordSets = new ArrayList<>();
        for(String ws : wordLists)
        {
            Map dict = new HashMap(5);
            String c[] = ws.split(":");
            String wordlist[] = c[c.length-1].split(",");
            if(c.length  > 1)
                dict.put(SM3_PREFIX,c[0]);
            dict.put(SM3_WORDLIST,Arrays.asList(wordlist));
            wordSets.add(dict);
        }
        return wordSets;
    }

    public List otherEvents()
    {
        List<String> sc = new ArrayList<>();
        sc.addAll((Collection<? extends String>) audioScenes.get("__events"));
        sc.removeAll(Arrays.asList("sfx","finale","a"));
        return sc;
    }

    public void setUpEvents()
    {
        events = new ArrayList<>();
        if(needDemo)
            events.add("a");
        events.addAll(otherEvents());
        int noscenes =(int)wordSets.size();
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1) );
    }

    public void miscSetUp()
    {
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        wordSets = wordSets(Arrays.asList(ws.split(";")));
        currNo = 0;
        highlightswatch = (OBPath) objectDict.get("highlightswatch");
        incorrectswatch = (OBPath) objectDict.get("incorrectswatch");
        String par = parameters.get("demo");
        needDemo = par != null && par.equals("true");
        OBControl pic = objectDict.get("pic");
        picScale = pic.scale();
        deleteControls("pic");
        picrects = (List<OBPath>)(Object)sortedFilteredControls("picrect.*");
        targets = (List<OBControl>)(Object)picrects;
        lowColour = picrects.get(0).fillColor();
        lowStroke = picrects.get(0).strokeColor();
        lowWidth = picrects.get(0).lineWidth();
        textBox = objectDict.get("textbox");
    }


    public void prepare()
    {
        theMoveSpeed = bounds().width();
        initialised = true;
        processParams();
        eventsDict = loadXML(getConfigPath(sectionName() + ".xml"));
        loadAudioXML(getConfigPath(sectionAudioName() + "audio.xml"));
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        setUpEvents();
        doVisual(currentEvent());
    }

    public String substituteAudio(String defaultAudio)
    {
        String result = defaultAudio;
        if(label.text().length()  > 1 && currentAudio(String.format("ALT.%",defaultAudio))!= null)
            result = String.format("ALT.%",defaultAudio);
        return result;
    }

    public void highlightRect(OBPath rect,int fl)
    {
        lockScreen();
        if((fl & DO_FILL) != 0)
            rect.setFillColor(highlightswatch.fillColor());
        if((fl & DO_ERROR) != 0)
            rect.setFillColor(incorrectswatch.fillColor());
        if((fl & DO_STROKE) != 0)
        {
            rect.setStrokeColor(highlightswatch.strokeColor());
            rect.setLineWidth(highlightswatch.lineWidth());
        }
        unlockScreen();
    }

    public void lowlightRect(OBPath rect,int fl)
    {
        lockScreen();
        if ((fl &(DO_FILL|DO_ERROR))!= 0)
            rect.setFillColor(lowColour);
        if((fl & DO_STROKE) != 0)
        {
            rect.setStrokeColor(lowStroke);
            rect.setLineWidth(lowWidth);
        }
        unlockScreen();
    }

    public void setUpLabel(String tx)
    {
        Typeface tf = OBUtils.standardTypeFace();
        label = new OBLabel(tx,tf,textSize);
        label.setColour(Color.BLACK);
        label.setPosition(textBox.position());
        label.hide();
    }

    public void clearOffOldThings()
    {
        try
        {
            playSfxAudio("popoff", false);
        }
        catch(Exception e)
        {
        }
        lockScreen();
        for(OBImage pic : pics)
            pic.hide();
        hideControls("picr.*");
        label.hide();
        unlockScreen();
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
                    currNo++;
                    setScene(events.get(eventIndex));
                }
            });
    }

    public void nameItemsToken(long token) throws Exception
    {
        for(int i = 0;i < 3;i++)
        {
            highlightRect(picrects.get(i),DO_FILL);
            if(token > 0)
                checkSequenceToken(token);
            List<Object> l = (List<Object>)(Object)Collections.singletonList(chosenWords.get(i));
            playAudioQueued(l,true);
            lowlightRect(picrects.get(i),DO_FILL);
            if(token > 0)
                checkSequenceToken(token);
            waitForSecs(0.4f);
        }
    }

    public String showAudioName()
    {
        return "wordon";
    }

    public void showLabelWithColour(boolean withCol) throws Exception
    {
        playSfxAudio(showAudioName(),false);
        lockScreen();
        if(withCol)
            label.setColour(Color.RED);
        label.show();
        unlockScreen();
        waitForSecs(0.2f);
        waitSFX();
        label.setColour(Color.BLACK);
    }

    public void adjustCardsForDemo()
    {
        PointF pt = pics.get(0).position();
        if(picrects.get(2).frame.contains(pt.x,pt.y))
            return;
        List<OBImage>xorderedpics = new ArrayList<>(pics);
        Collections.sort(xorderedpics, new Comparator<OBImage>()
        {
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
        PointF temppos = new PointF();
        temppos.set(pics.get(0).position());
        pics.get(0).setPosition(xorderedpics.get(2).position());
        xorderedpics.get(2).setPosition(temppos);
        unlockScreen();
        Collections.sort(xorderedpics, new Comparator<OBImage>()
        {
            public int compare (OBImage obj1, OBImage obj2)
            {
                if(obj1.position().x < obj2.position().x)
                    return -1;
                if(obj1.position().x > obj2.position().x)
                    return 1;
                return 0;
            }
        });
        List cw = new ArrayList<>();
        for(int i = 0;i < 3;i++)
            cw.add(xorderedpics.get(i).propertyValue("word"));
        chosenWords = cw;
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
        if(currentAudio("INTRO2") != null)
        {
            playAudioQueuedScene("INTRO2",true);
            waitForSecs(0.35f);
        }
    }

    public void setSceneb()
    {
        setSceneXX(currentEvent());
    }

    public void doMainXX() throws Exception
    {
        doIntro();
        nameItemsToken(0);
        setReplayAudio(Collections.EMPTY_LIST);
        List audio = currentAudio(substituteAudio("PROMPT"));
        if(audio != null)
        {
            waitForSecs(0.5f);
            playAudioQueued(OBUtils.insertAudioInterval(audio,300),true);
            waitForSecs(0.2f);
        }
        showLabelWithColour(true);
        waitForSecs(0.3f);

        audio = currentAudio("PROMPT2");
        if(audio != null)
        {
            waitAudio();
            waitForSecs(0.35f);
            playAudioQueued(audio,false);
        }
    }

    public void redInitial() throws Exception
    {
        playSfxAudio("red",false);
        lockScreen();
        label.setColour(Color.RED);
        label.show();
        unlockScreen();
        waitForSecs(0.2f);
        waitSFX();
    }

    public void promptSequence()
    {
        long token = -1;
        try
        {
            token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                nameItemsToken(token);
                checkSequenceToken(token);
                List audio = currentAudio(substituteAudio("PROMPT"));
                if(audio != null)
                {
                    waitForSecs(0.2f);
                    playAudioQueued(OBUtils.insertAudioInterval(audio,300),true);
                    waitForSecs(0.2f);
                    checkSequenceToken(token);
                }
                redInitial();
                checkSequenceToken(token);
                waitForSecs(0.3f);

                audio = currentAudio("PROMPT2");
                if(audio != null)
                {
                    waitAudio();
                    checkSequenceToken(token);
                    waitForSecs(0.35f);
                    playAudioQueued(audio,false);
                }
            }
        }
        catch(Exception exception)
        {
        }
        lockScreen();
        for(int i = 0;i < 3;i++)
        {
            lowlightRect(picrects.get(i),DO_FILL|DO_STROKE);
        }
        label.setColour(Color.BLACK);
        unlockScreen();
        sequenceLock.unlock();
    }

    public void _replayAudio()
    {
        promptSequence();
    }

    public void endBody()
    {
        if(currentEvent().equals("b"))
        {
            try
            {
                final long sttime = statusTime;
                waitForSecs(0.3f);
                waitAudio();
                if(sttime == statusTime)
                    reprompt(sttime,null,6, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            if(sttime == statusTime)
                            {
                                replayAudio();
                            }
                        }
                    });
            }
            catch (Exception e)
            {
            }
        }
    }

    public void highlightAndSpeakSylls(String wid,boolean leaveHighlighted)
    {
        OBWord w = (OBWord) wordDict.get(currWordID);
        highlightAndSpeakSyllablesForWord(w,leaveHighlighted);
    }

}
