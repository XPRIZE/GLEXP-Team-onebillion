package org.onebillion.xprz.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 27/06/16.
 */
public class OBSyllable extends OBPhoneme
{
    public List<OBPhoneme> phonemes;
    public OBSyllable()
    {
        super();
    }
    public OBSyllable(String tx)
    {
        super(tx);
    }
    public OBSyllable(String sound,String text,List<OBPhoneme> lst)
    {
        super(sound,text);
        phonemes = lst;
    }

    public OBSyllable copy()
    {
        OBSyllable obj = (OBSyllable) super.copy();
        obj.phonemes = new ArrayList<>(phonemes);
        return obj;
    }
}
