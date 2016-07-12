package org.onebillion.xprz.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 28/06/16.
 */
public class OBSyllable extends OBPhoneme
{
    public List<OBPhoneme> phonemes;

    public OBSyllable ()
    {
        this(null, null, null, null);
    }

    public OBSyllable (String text)
    {
        this(text, null, null, null);
    }

    public OBSyllable (String text, String soundID)
    {
        this(text, soundID, null, null);
    }

    public OBSyllable (String text, String soundID, List<OBPhoneme> phonemes)
    {
        this(text, soundID, null, phonemes);
    }


    public OBSyllable (String text, String soundID, List<Object> timings, List<OBPhoneme> phonemes)
    {
        super(text, soundID, timings);
        this.phonemes = (phonemes == null) ? new ArrayList<OBPhoneme>() : new ArrayList<OBPhoneme>(phonemes);
    }


    public OBSyllable copy ()
    {
        List<OBPhoneme> phonemesClone = new ArrayList<OBPhoneme>();
        //
        for (OBPhoneme phoneme : phonemes)
        {
            phonemesClone.add(phoneme.copy());
        }
        return new OBSyllable(text, soundid, timings, phonemesClone);
    }


}
