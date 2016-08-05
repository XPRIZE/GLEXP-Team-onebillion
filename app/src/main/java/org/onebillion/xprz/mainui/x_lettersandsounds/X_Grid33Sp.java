package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.PointF;
import android.util.ArraySet;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alan on 05/08/16.
 */
public class X_Grid33Sp extends X_Grid33S
{
    int dashNormalColour,dashHiColour,squareColour;
    List<OBPath>dashes;
    PointF centrePos;
    List<OBLabel> wordLabels;
    OBLabel mainLabel;
    Map<String,Map<String,String>> letterDict;
    int vowelColour;
    int letterNo;

    public void miscSetUp()
    {
        needDemo = false;
        if(parameters.get("demo") != null && parameters.get("demo").equals("true"))
            needDemo = true;
        events = new ArrayList<>();
        events.add("a");
        if(needDemo)
            events.add("b");
        events.add("c");
        events.add("d");
        events.add("e");
        wordDict = OBUtils.LoadWordComponentsXML(true);
        letterDict = LoadLetterXML(getLocalPath("letters.xml"));
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        bigTextSize = textSize;
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        dashes = (List<OBPath>)(Object)sortedFilteredControls("lne.*");
        dashHiColour = dashes.get(0).strokeColor();
        dashNormalColour = dashes.get(1).strokeColor();
        centrePos = dashes.get(0).position();
        centrePos.x = ((dashes.get(0).left() + dashes.get(dashes.size() - 1) .right()) / 2);
        centrePos.y -= applyGraphicScale(8);
        OBPath vs = (OBPath) objectDict.get("vowelswatch");
        vowelColour = vs.fillColor();
        List<OBControl>ls = ((OBGroup)objectDict.get("grid")).filterMembers("squ.*",true);
        squares = (List<OBPath>)(Object)ls;
        squareColour = squares.get(0).fillColor();
        currNo = 0;
        int noscenes =(int)words.size();
        if(needDemo)
            noscenes++;
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        needClear = true;
    }

    public String letter(String l,String tag)
    {
        Map<String,String> dict = letterDict.get(l);
        return dict.get(tag);
    }

    public List pickLetters(OBWord rw)
    {
        Set usedSet = new ArraySet();
        for(int i = 0;i < rw.text.length();i++)
            usedSet.add(rw.text.substring(i, i + 1));
        int target =(int)squares.size();
        List<String> ls = (List<String>)(Object)Arrays.asList(letterDict.keySet().toArray());
        List<String> someLetters = OBUtils.randomlySortedArray(ls);
        for(String l : someLetters)
        {
            if(!usedSet.contains(l))
            {
                Map dict = letterDict.get(l);
                if(dict.get("vowel") != null)
                {
                    usedSet.add(l);
                    break;
                }
            }
        }
        int i = 0;
        while(usedSet.size()  < target )
        {
            String l = someLetters.get(i);
            if(!usedSet.contains(l))
            {
                Map<String,String> dict = letterDict.get(l);
                if(dict.get("repel") != null )
                {
                    List<String> rs = Arrays.asList(dict.get("repel").split(","));
                    if(rs.contains(l))
                    {
                        i++;
                        continue;
                    }
                }
                usedSet.add(l);
            }
            i++;
        }
        return OBUtils.randomlySortedArray(Arrays.asList(usedSet.toArray()));
    }

    public void highlightDash(int i)
    {
        for(OBPath p : dashes)
            p.setStrokeColor(dashNormalColour);
        if(i >= 0 && i < dashes.size() )
            dashes.get(i) .setStrokeColor(dashHiColour);
    }


}
