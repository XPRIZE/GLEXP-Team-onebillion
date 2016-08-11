package org.onebillion.xprz.mainui.x_lettersandsounds;

import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alan on 11/08/16.
 */
public class X_G3x3syl1 extends X_Grid33Sp
{
    Set syllableSet;
    int gridColour;
    boolean hasPicture;
    int componentNo;
    boolean needRegen;

    public void buildSyllableSet()
    {
        Set set = new HashSet();
        for(String w : words)
        {
            OBWord rw = (OBWord) wordDict.get(w);
            for(OBSyllable syll : rw.syllables() )
                set.add(syll.text);
        }
        if(set.size()  < squares.size() )
        {
            String ws = parameters.get("distractors");
            if (ws != null)
                for(String sys : ws.split(","))
                    set.add(sys);
        }
        syllableSet = set;
    }

    public void buildEvents()
    {
        events = new ArrayList<>();
        events.add("a");
        if(needDemo)
            events.add("b");
        events.add("c");
        events.add("d");
        events.add("e");
        int noscenes =(int)words.size();
        if(needDemo)
            noscenes++;
        while(events.size()  > noscenes)
            events.remove(events.size() - 1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
    }

    public void processWords()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
    }

    public void loadMainScene()
    {
        String pp = parameters.get("pictures");
        if (pp == null)
            pp = "";
        if((hasPicture = !pp.equals("false")))
            loadEvent("horiz");
        else
            loadEvent("vert");
        OBPath sw = (OBPath) objectDict.get("swatch");
        squareColour = sw.fillColor();
        gridColour = sw.strokeColor();
    }

    public void miscSetUp()
    {
        needDemo = false;
        String pd = parameters.get("demo");
        if(pd != null && pd.equals("true") )
            needDemo = true;
        needRegen = true;
        loadMainScene();
        processWords();
        OBGroup grid = (OBGroup) objectDict.get("grid");
        squares = (List<OBPath>)(Object)grid.filterMembers("squ.*",true);
        buildSyllableSet();
        buildEvents();
        OBGroup gtl = (OBGroup) objectDict.get("gridtext");
        OBLabel tl = (OBLabel) gtl.objectDict.get("t");
        textSize = tl.fontSize()  * 1 / graphicScale();
        bigTextSize = textSize * 1.5f;
        dashes = (List<OBPath>)(Object)sortedFilteredControls("lne.*");
        dashHiColour = dashes.get(0).strokeColor();
        dashNormalColour = dashes.get(1).strokeColor();

        centrePos = dashes.get(0).position();
        centrePos.x = ((dashes.get(0).left() + dashes.get(dashes.size()-1).right()) / 2);
        centrePos.y -= applyGraphicScale(8);
        for(OBPath s : squares)
            s.setFillColor(squareColour);
        OBPath back = (OBPath) grid.objectDict.get("backrect");
        back.setFillColor(gridColour);
        currNo = 0;
    }

    public List pickSyllables(OBWord rw)
    {
        Set sylls = new HashSet();
        sylls.addAll(rw.syllables());
        List allSyls = Arrays.asList(syllableSet);
        int i = 0;
        while(sylls.size()  < squares.size() )
        {
            sylls.add(allSyls.get(i++) );
        }
        return Arrays.asList(sylls);
    }

}
