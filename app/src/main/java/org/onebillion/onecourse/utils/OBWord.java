package org.onebillion.onecourse.utils;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 28/06/16.
 */
public class OBWord extends OBSyllable
{

    public List<OBSyllable> syllables;
    public String imageName;
    Boolean syllablesChecked, phonemesChecked;


    public OBWord (String text)
    {
        this(text, null, null, null, null);

    }

    public OBWord (String text, String soundID)
    {
        this(text, soundID, null, null, null);
    }


    public OBWord (String text, String soundID, List<OBSyllable> syllables)
    {
        this(text, soundID, null, syllables, null);
    }


    public OBWord (String text, String soundID, List<Object> timings, List<OBSyllable> syllables, String imageName)
    {
        super(text, soundID, timings, null);
        this.syllablesChecked = false;
        this.phonemesChecked = false;
        this.syllables = (syllables == null) ? new ArrayList<OBSyllable>() : new ArrayList<OBSyllable>(syllables);
//        this.imageName = (imageName == null) ? null : (imageName.equals("true")) ? soundID : imageName;
        this.imageName = (imageName == null) ? soundID : imageName;
    }



    public String ImageFileName()
    {
        if (this.imageName == null)
        {
            MainActivity.log("OBWord.MISSING IMAGE FOR WORD " + this.soundid);
        }
        return this.imageName;
    }



    public List<OBSyllable> syllables()
    {
        if (!syllablesChecked)
        {
            String partSylWordAudio = new String(soundid).replace("fc_", "fc_syl_");
            String fullpath = locationInSameDirectory(soundid, partSylWordAudio);
            if (fullpath != null)
            {
                fullpath = String.format("%s%s.%s", fullpath, partSylWordAudio, "etpa");
                List<List<Double>> sylTiming = OBUtils.ComponentTimingsForWord(fullpath);
                //
                if (sylTiming.size() > 0)
                {
                    List timingSyllables = new ArrayList();
                    int index = 0;
                    for (OBSyllable syllable : syllables)
                    {
                        OBSyllable sylCopy = syllable.copy();
                        sylCopy.timings = (List<Object>) (Object) sylTiming.get(index);
                        sylCopy.soundid = partSylWordAudio;
                        timingSyllables.add(sylCopy);
                        index++;
                    }
                    syllables = timingSyllables;
                }
                syllablesChecked = true;
            }
        }
        return syllables;
    }


    public List<OBPhoneme> phonemes()
    {
        if (!phonemesChecked)
        {
            if (phonemes.size() == 0)
            {
                for (OBSyllable syllable : syllables())
                {
                    phonemes.addAll(syllable.phonemes);
                }
            }
            String partPhoWordAudio = new String(soundid).replace("fc_", "fc_let_");
            String fullpath = locationInSameDirectory(soundid, partPhoWordAudio);
            if (fullpath != null)
            {
                fullpath = String.format("%s%s.%s", fullpath, partPhoWordAudio, "etpa");
                List<List<Double>> phoTiming = OBUtils.ComponentTimingsForWord(fullpath);
                //
                if (phoTiming.size() > 0 && phoTiming.size() >= phonemes.size())
                {
                    List timingPhonemes = new ArrayList();
                    int index = 0;
                    for (OBPhoneme phoneme : phonemes)
                    {
                        OBPhoneme phoCopy = phoneme.copy();
                        phoCopy.timings = (List<Object>) (Object) phoTiming.get(index);
                        phoCopy.soundid = partPhoWordAudio;
                        timingPhonemes.add(phoCopy);
                        index++;
                    }
                    phonemes = timingPhonemes;
                }
                phonemesChecked = true;
            }
        }
        return phonemes;
    }


    public OBWord copy()
    {
        List<OBSyllable> syllablesClone = new ArrayList<OBSyllable>();
        //
        for (OBSyllable syllable : syllables)
        {
            syllablesClone.add(syllable.copy());
        }
        return new OBWord(text, soundid, timings, syllablesClone, imageName);
    }

    public static String locationInSameDirectory(String item1,String item2)
    {
        List<String> audioExtensions = OBConfigManager.sharedManager.getAudioExtensions();
        String p1 = OBSectionController.getLocalPath(String.format("%s.%s", item1,  audioExtensions.get(0)));
        if (p1 == null)
        {
            return null;
        }
        String p2 = OBSectionController.getLocalPath(String.format("%s.%s",item2,  audioExtensions.get(0)));
        if (p2 == null)
        {
            return null;
        }
        if(OBUtils.stringByDeletingLastPathComponent(p1).equals(OBUtils.stringByDeletingLastPathComponent(p2)))
        {
            return OBUtils.stringByDeletingLastPathComponent(p1) + "/";
        }
        else
        {
            return null;
        }
    }

}
