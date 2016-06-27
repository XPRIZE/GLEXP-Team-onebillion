package org.onebillion.xprz.utils;

import org.onebillion.xprz.mainui.OBSectionController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 27/06/16.
 */
public class OBWord extends OBSyllable
{

    List<OBSyllable>syllables = new ArrayList<>();
    String imageName;
    boolean syllablesCheked = false,phonemesCheked = false;
    public OBWord()
    {
        super();
    }
    public OBWord(String tx)
    {
        super(tx);
    }

    public OBWord(String sound,String text,List<OBSyllable>syls)
    {
        super(sound,text,new ArrayList<OBPhoneme>());
        for(OBSyllable syl : syls)
            if(syl.phonemes != null)
                phonemes.addAll(syl.phonemes);

        syllables = syls;
    }

    public List<OBSyllable> syllables()
    {
        if(!syllablesCheked)
        {
            String partSylWordAudio = audio.replace("fc_","fc_syl_");
            List<List<Double>> sylTiming = OBUtils.ComponentTimingsForWord(audio, OBSectionController.getLocalPath(partSylWordAudio+".etpa"));

            if(sylTiming.size() > 0)
            {
                List<OBSyllable> timingSyllables = new ArrayList<>();
                int index = 0;
                for(OBSyllable syllable : syllables)
                {
                    OBSyllable sylCopy = syllable.copy();
                    sylCopy.audio = partSylWordAudio;
                    //sylCopy.timings = sylTiming.get(index);
                    timingSyllables.add(sylCopy);
                    index++;
                }
                syllables = timingSyllables;
            }
            syllablesCheked = true;
        }
        return syllables;
    }

}
