package org.onebillion.xprz.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 28/06/16.
 */
public class OBSyllable extends OBPhoneme
{
    public List<OBPhoneme> phonemes;

    public OBSyllable()
    {
        phonemes = new ArrayList<OBPhoneme>();
    }

    public OBSyllable(String text)
    {
        super(text);
    }

    public OBSyllable(String text, String soundID)
    {
        super(text, soundID);
    }

    public OBSyllable(String text, String soundID, List<OBPhoneme> phonemes)
    {
        super(text, soundID);
        this.phonemes = new ArrayList<OBPhoneme>(phonemes);
    }


    public OBSyllable copy()
    {
        List<OBPhoneme> phonemesClone = new ArrayList<OBPhoneme>();
        //
        for (OBPhoneme phoneme : phonemes)
        {
            phonemesClone.add(phoneme.copy());
        }
        return new OBSyllable(text, soundID, phonemesClone);
    }


}
