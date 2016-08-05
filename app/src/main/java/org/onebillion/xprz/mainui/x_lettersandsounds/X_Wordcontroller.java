package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 27/06/16.
 */
public class X_Wordcontroller extends XPRZ_SectionController
{
    boolean needDemo;

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()) )
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    void SetColourForLabel(OBLabel lab,int col)
    {
        lab.setColour(col);
    }
    public void highlightLabel(OBLabel lab, boolean h)
    {
        lockScreen();
        int col;
        if(h)
            col = Color.RED;
        else
            col = Color.BLACK;
        SetColourForLabel(lab, col);
        unlockScreen();
    }

    public void playLetterSound(String s)
    {
        playAudio(String.format("is_%s",s));
    }

    public boolean itemsInSameDirectory(String item1,String item2)
    {
        String path1 = getLocalPath(String.format("%s.m4a",item1));
        if (path1 == null)
            return false;
        String p1 = OBUtils.stringByDeletingLastPathComponent(path1);
        if (p1 == null)
            return false;
        String path2 = getLocalPath(String.format("%s.m4a",item2));
        if (path2 == null)
            return false;
        String p2 = OBUtils.stringByDeletingLastPathComponent(path2);
        if (p2 == null)
            return false;
        return p1.equals(p2);
    }

    public void playFirstSoundOfWordId(String wordID,OBWord rw)
    {
        try
        {
            String fileName = wordID.replaceFirst("_","_let_") ;
            if(fileName != null && itemsInSameDirectory(fileName,wordID) )
            {
                List<List<Double>> timings = OBUtils.ComponentTimingsForWord(getLocalPath(fileName+".etpa"));
                if(timings.size()  > 0)
                {
                    List<Double> timing = timings.get(0);
                    double timeStart = timing.get(0) ;
                    double timeEnd = timing.get(1);
                    playAudioFromTo(fileName,timeStart,timeEnd);
                    return;
                }
            }
            if(rw != null)
            {
                playLetterSound(rw.syllables().get(0).phonemes.get(0).text);
            }
        }
        catch(Exception exception)
        {
            Log.i("itemsinsame","here");
        }
    }

    public void highlightAndSpeakSyllablesForWord(final OBWord w)
    {
        try
        {
            String wordID = w.soundid;
            String fileName = wordID.replace("fc_", "fc_syl_");
            List<List<Double>> timings = OBUtils.ComponentTimingsForWord(getLocalPath(fileName + ".etpa"));
            playAudio(fileName);
            long startTime = SystemClock.uptimeMillis();
            int i = 0;
            int rangelocation = 0,rangelength = 0;
            for (OBSyllable syllable : w.syllables())
            {
                double currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
                List<Double> timing = timings.get(i);
                double timeStart = timing.get(0);
                double timeEnd = timing.get(1);
                double waitTime = timeStart - currTime;
                if (waitTime > 0.0)
                    waitForSecs(waitTime);
                rangelength = syllable.text.length();
                highlightWrd(w,rangelocation,rangelocation+rangelength,true);
                currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
                waitTime = timeEnd - currTime;
                if (waitTime > 0.0)
                    waitForSecs(waitTime);
                highlightWrd(w,rangelocation,rangelocation+rangelength,false);

                rangelocation += rangelength;
                rangelength = 0;
                i++;
            }
            waitForSecs(0.3f);
            highlightWrd(w,0,w.text.length(),true);
            playAudioQueued(Collections.singletonList((Object)wordID),true);
            highlightWrd(w,0,w.text.length(),false);
            waitForSecs(0.3f);
        }
        catch (Exception exception)
        {
        }
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                highlightWrd(w,0,w.text.length(),false);
            }
        });
    }

    public void highlightWrd(OBWord w,int rangestart,int rangeend,boolean h)
    {
        OBLabel lab = (OBLabel) w.properties.get("label");
        lockScreen();
        if (h)
            lab.setHighRange(rangestart,rangeend,Color.RED);
        else
            lab.setHighRange(-1,-1,Color.BLACK);
        unlockScreen();
    }

    public Map LoadLetterXML(String xmlPath)
    {
    Map dict = new HashMap();
    if(xmlPath != null)
    {
        try
        {
            OBXMLManager xmlman = new OBXMLManager();
            List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
            OBXMLNode root = xl.get(0);
            for(OBXMLNode letterNode : root.childrenOfType("letter"))
            {
                String name = letterNode.attributeStringValue("Object");
                String tags = letterNode.attributeStringValue("tags");
                Map lttr = new HashMap();
                for(String tag : tags.split("/"))
                {
                    List<String> pars = Arrays.asList(tag.split("="));
                    String k = pars.get(0);
                    String val;
                    if(pars.size()  < 2)
                        val = "true";
                    else
                        val = pars.get(1);
                    lttr.put(k,val);
                }
                dict.put(name,lttr);
            }
        }
        catch(Exception e)
        {

        }
    }
    return dict;
}

}
