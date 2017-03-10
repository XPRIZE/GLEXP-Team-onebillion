package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;

import java.util.ArrayList;
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
            dict.put(SM3_WORDLIST,wordlist);
            wordSets.add(dict);
        }
        return wordSets;
    }


}
