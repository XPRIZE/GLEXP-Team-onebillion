package org.onebillion.xprz.mainui.x_lettersandsounds;

import org.onebillion.xprz.utils.OBSyllable;
import org.onebillion.xprz.utils.OBWord;

import java.util.HashSet;
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

}
