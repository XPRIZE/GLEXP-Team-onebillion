package org.onebillion.xprz.mainui.x_lettersandsounds;

import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBUserPressedBackException;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alan on 12/08/16.
 */
public class X_G3x3syl2 extends X_G3x3syl1
{
    public void processWords()
    {
        Map wd = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(";"));
        List mw = new ArrayList<>();
        for(String w : words)
        {
            List<String> syllables_array = Arrays.asList(w.split(","));
            List syllables = new ArrayList();
            for(String syllable : syllables_array)
            {
                String syllID = String.format("isyl_%", syllable);
                OBSyllable syll = (OBSyllable) wd.get(syllID);
                syllables.add(syll);
            }
            OBWord word = new OBWord(null,w.replace(",",""),syllables);
            mw.add(word.text);
            wd.put(word.text,word);
        }
        words = mw;
        wordDict = wd;
    }

    public void loadMainScene()
    {
        loadEvent("vert");
        OBPath sw = (OBPath) objectDict.get("swatch2");
        squareColour = sw.fillColor();
        gridColour = sw.strokeColor();
    }

    public List arrayOfCurrentWord()
    {
        OBWord rw = (OBWord) wordDict.get(words.get(currNo));
        List marr = new ArrayList<>();
        for(OBSyllable sy : rw.syllables() )
        {
            //String s = sy.text;
            //marr.add(String.format("isyl_%", s));
            marr.add(sy.soundid);
        }
        return marr;
    }
    public void playCurrentWordWait(boolean w) throws OBUserPressedBackException
    {
        playAudioQueued(arrayOfCurrentWord(),w);
    }

}
