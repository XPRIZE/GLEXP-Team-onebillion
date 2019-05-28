package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBImage;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.utils.OBImageManager;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 05/08/16.
 */
public class OC_Grid33S extends OC_Wordcontroller
{
    List<String>words,sounds;
    String currWord,firstSound;
    float textSize,bigTextSize;
    List<OBLabel> labels = new ArrayList<>();
    Map<String, OBPhoneme> wordDict;
    List<OBPath>squares;
    OBWord currReadingWord;
    boolean needClear;

    public void miscSetUp()
    {
        events = new ArrayList<>();
        events.add("a");
        events.add("b");
        events.add("c");
        events.add("d");
        wordDict = OBUtils.LoadWordComponentsXML(true);
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        sounds = Arrays.asList(parameters.get("sounds").split(","));
        setUpLabels(Color.WHITE);
        currNo = 0;
        int noscenes =(int)words.size()  + 1;
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");

        miscSetUp();
        doVisual(currentEvent());
    }

    public void setUpImage(String fileName)
    {
        OBImage im = OBImageManager.sharedImageManager().imageForName(fileName);
        OBControl pic = objectDict.get("pic");
        im.setScale(pic.scale());
        im.setPosition(pic.position());
        attachControl(im);
        objectDict.put("im",im);
        im.setZPosition(5);
    }

    public OBLabel setUpLabel(String tx,int col)
    {
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(tx,tf,textSize);
        label.setColour(col);
        label.setZPosition(12);
        return label;
    }

    public void setUpLabels(int col)
    {
        OBGroup grid = (OBGroup) objectDict.get("grid");
        squares = (List<OBPath>)(Object)grid.filterMembers("squ.*",true);
        List<OBLabel>ls = new ArrayList<>();
        int i = 0;
        sounds = OBUtils.randomlySortedArray(sounds);
        if(sounds.size()  > squares.size() )
            sounds = sounds.subList(0,squares.size());
        for(String sound : sounds)
        {
            OBLabel l = setUpLabel(sound,col);
            ls.add(l);
            OBControl s = squares.get(i);
            PointF pos = convertPointFromControl(s.position(),s.parent);
            l.setPosition(pos);
            attachControl(l);
            l.hide();
            l.setProperty("sound",sound);
            i++;
        }
        labels = ls;
    }

    public void setSceneXX(String  scene)
    {
        deleteControls("im");
        String wordid = words.get(currNo);
        currReadingWord = (OBWord) wordDict.get(wordid);
        currWord = currReadingWord.text;
        setUpImage(currReadingWord.imageName);
        firstSound = currReadingWord.phonemes().get(0).text;
        for(OBLabel l : labels)
            l.setProperty("correct",(l.propertyValue("sound").equals(firstSound)));
        objectDict.get("im").hide();
        targets = (List<OBControl>)(Object)squares;
    }


    public void playAudioPlusWord(List audio)
    {
        try
        {
            long stt = statusTime;
            playAudioQueued(audio,true);
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
    public void _replayAudio()
    {
        playAudioPlusWord(_replayAudio);
    }

    public void doAudio(String  scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", true);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
        OBWord crw = (OBWord) wordDict.get(words.get(currNo));
        playFirstSoundOfWordId(words.get(currNo),crw);
    }

    public void endBody()
    {
        try
        {
            List reminderAudio = currentAudio("PROMPT.REMINDER");
            if(reminderAudio != null)
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
                                OBWord crw = (OBWord) wordDict.get(words.get(currNo));
                                playFirstSoundOfWordId(words.get(currNo),crw);
                            }
                        }
                    });
            }
        }
        catch(Exception e)
        {

        }
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        for(OBLabel l : labels)
        {
            l.show();
            playSfxAudio("picon",false);
            waitForSecs(0.1f);
            waitSFX();
        }
        waitForSecs(0.3f);
        nextScene();
    }

    public void checkTarget(final OBControl targ,PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        targ.highlight();
        try
        {
            int idx = squares.indexOf(targ);
            if(((Boolean)labels.get(idx).propertyValue("correct")).booleanValue())
            {
                playAudioQueuedScene("correct",true);
                waitForSecs(0.2f);
                waitAudio();
                objectDict.get("im").show();
                waitForSecs(0.4f);
                targ.lowlight();
                OBWord crw = (OBWord) wordDict.get(words.get(currNo));
                playFirstSoundOfWordId(words.get(currNo),crw);
                waitForSecs(0.6f);
                playAudioQueued((List<Object>)(Object)Arrays.asList(words.get(currNo)),true);
                waitForSecs(1f);
                objectDict.get("im").hide();
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
                        playAudioPlusWord(currentAudio("INCORRECT"));
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

    public Object findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
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
