package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.utils.OBSyllable;
import com.maq.xprize.onecourse.hindi.utils.OBUserPressedBackException;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 12/08/16.
 */
public class OC_G3x3syl2 extends OC_G3x3syl1
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
                String syllID = String.format("isyl_%s", syllable);
                OBSyllable syll = (OBSyllable) wd.get(syllID);
                syllables.add(syll);
            }
            String tx = w.replace(",","");
            OBWord word = new OBWord(tx,tx,syllables);
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
            String s = sy.text;
            marr.add(String.format("isyl_%s", s));
            //marr.add(sy.soundid);
        }
        return marr;
    }
    public void playCurrentWordWait(boolean w) throws OBUserPressedBackException
    {
        playAudioQueued(arrayOfCurrentWord(),w);
    }

    public void highlightAndSpeakComponents(List<OBLabel>labs,String wordID,String s,String fileName)
    {
        List wds = arrayOfCurrentWord();
        try
        {
            for(int i = 0; i < labs.size();i++)
            {
                highlightLabel(labs.get(i),true);
                playAudioQueued(Arrays.asList(wds.get(i)),true);
                waitForSecs(0.2f);
                highlightLabel(labs.get(i),false);

            }
            waitForSecs(0.3f);
        }
        catch(Exception exception)
        {
        }
        lockScreen();
        for(OBLabel l : labs)
            highlightLabel(l,false);
        unlockScreen();
    }

}
