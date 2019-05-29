package com.maq.xprize.onecourse.hindi.mainui.oc_playzone;

import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.utils.OBConditionLock;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.utils.OBUtils.LoadWordComponentsXML;
import static com.maq.xprize.onecourse.hindi.utils.OB_Maths.OffsetPoint;

public class OC_PlayZoneTrace2 extends OC_PlayZoneTrace
{
    String word,wordText;
    Map<String,OBPhoneme> wordDict;
    List bottomGroupList;

    public String layoutName()
    {
        return "masterb";
    }

    public void miscSetUp()
    {
        groupList = new ArrayList<>();
        word = OBUtils.coalesce(parameters.get("word") , "fc_dog");
        wordDict = LoadWordComponentsXML(true);
        wordText = wordDict.get(word).text;

        events = new ArrayList(Arrays.asList("word","b","worddone"));
        Map ed = loadXML(getConfigPath(String.format("%s.xml",tracingFileName())));
        eventsDict.putAll(ed);
        loadEvent("masterl");
        letterrecth = objectDict.get("letterrect").height();
        deleteControls("letterrect");
        OBControl blob = objectDict.get("blob");
        blobImage = blob.renderedImageControl();
        deleteControls("blob");
        traceLock = new OBConditionLock(0);
    }

    public void loadWord()
    {
        List<OBControl> xboxes = new ArrayList<>();
        float bottom = objectDict.get("letterrect0").bottom();
        for(int i = 0;i < wordText.length();i++)
        {
            for(String n : filterControlsIDs("Path.*|xbox"))
                objectDict.remove(n);
            String character = wordText.substring(i,i + 1);
            String l = "_" + (character);
            OBGroup g = letterGroup(l,"letterrect0");
            groupList.add(g);
            attachControl(g);
            OBControl xbox = objectDict.get("xbox");
            if(xbox != null)
            {
                xboxes.add(xbox);
                float diff = xbox.bottom() - bottom;
                if(diff != 0)
                {
                    PointF pos = g.position();
                    pos.y -= diff;
                    g.setPosition(pos);
                }
                xbox.hide();
            }
        }
        float xboxeswidth = 0;
        for(int i = 0;i < xboxes.size();i++)
        {
            OBControl xb = xboxes.get(i);
            RectF f = xb.frame();
            xboxeswidth += f.width();
        }
        float left =(bounds() .width() - xboxeswidth) / 2;
        for(int i = 0;i < xboxes.size();i++)
        {
            OBControl xb = xboxes.get(i);
            float xdiff = left - xb.left();
            OBControl g = groupList.get(i);
            g.setPosition(OffsetPoint(g.position(), xdiff, 0));
            left += xb.width();
        }
        bottomGroupList = new ArrayList<>();
        float bottomdiff = objectDict.get("letterrect1").bottom() - bottom;
        for(OBGroup g : groupList)
        {
            OBGroup gc = (OBGroup) g.copy();
            gc.setBottom(gc.bottom() + bottomdiff);
            attachControl(gc);
            bottomGroupList.add(gc);
        }
        hideControls("letterrect.*");
        groupList.addAll(bottomGroupList);
    }

    public void setSceneword()
    {
        loadWord();
        lockScreen();
        for(int i = 0;i < wordText.length();i++)
        {
            setLetterDashed(i +(int) wordText.length(),true);
        }
        unlockScreen();
    }

    public void highlightAndPlay(int idx) throws Exception
    {
        int len =(int) wordText.length();
        lockScreen();
        for (int i = idx * len;i <(idx + 1) * len;i++)
            setLetterHigh(i,true);
        unlockScreen();
        waitForSecs(0.3f);
        playAudioQueued(Arrays.asList((Object)word),true);
        waitForSecs(0.5f);
        lockScreen();
        for (int i = idx * len;i <(idx + 1) * len;i++)
            setLetterHigh(i,false);
        unlockScreen();
    }

    public void replayAudio()
    {
        if(busyStatuses.contains((status())))
            return;
        setStatus(status());
        try
        {
            playAudioQueued(Arrays.asList((Object)word),false);
        }
        catch(Exception e)
        {
        }
    }

    public void finishLetter() throws Exception
    {
        lockScreen();
        redLayer.hide();
        hollow.hide();
        back.hide();
        unlockScreen();
        waitForSecs(0.4f);
        nextLetter();
    }

    public void demoword() throws Exception
    {
        waitForSecs(0.5f);
        highlightAndPlay(0);
        waitForSecs(0.4f);
        nextScene();
    }

    public void demoworddone() throws Exception
    {
        waitForSecs(0.5f);
        highlightAndPlay(1);
        waitForSecs(0.4f);
        score = 10;
        nextScene();
    }

    public void setSceneb()
    {

    }

    public void doMainb()
    {
        currNo =(int) wordText.length() - 1;
        try
        {
            nextLetter();
        }
        catch(Exception e)
        {
        }
    }


}
