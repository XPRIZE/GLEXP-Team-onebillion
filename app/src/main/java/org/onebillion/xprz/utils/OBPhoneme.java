package org.onebillion.xprz.utils;

import org.onebillion.xprz.mainui.OBSectionController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 28/06/16.
 */
public class OBPhoneme
{
    public String text;
    public List<Object> timings;

    Map<String, Object> properties;
    String audio, soundID;

    public OBPhoneme ()
    {
        this(null, null, null, null);
    }

    public OBPhoneme (String text)
    {
        this(text, null, null, null);
    }

    public OBPhoneme (String text, String soundID)
    {
        this(text, soundID, null, null);
    }


    public OBPhoneme (String text, String soundID, List<Object> timings)
    {
        this(text, soundID, timings, null);
    }


    public OBPhoneme (String text, String soundID, List<Object> timings, String audio)
    {
        this (text, soundID, timings, audio, new HashMap<String, Object>());
    }


    public OBPhoneme (String text, String soundID, List<Object> timings, String audio, HashMap<String, Object> properties)
    {
        super();
        this.text = text;
        this.soundID = soundID;
        this.audio = audio;
        this.timings = (timings == null) ? new ArrayList<Object>() : new ArrayList<Object>(timings);
        this.properties = (properties == null) ? new HashMap<String, Object>() : new HashMap<String, Object>(properties);
    }


    public Boolean hasAudio()
    {
        return (audio != null);
    }


    public String audioFilename()
    {
        if (!hasAudio())
        {
            return null;
        }
        else if (!audio.equals("true"))
        {
            return audio;
        }
        else
        {
            return soundID;
        }
    }


    public void playAudio (OBSectionController sc, Boolean wait) throws Exception
    {
        if (timings != null && timings.size() > 1)
        {
            sc.playAudioFromTo(audioFilename(), (Double) timings.get(0), (Double) timings.get(1));
        }
        else if (audioFilename() != null)
        {
            sc.playAudio(audioFilename());
        }
        if (audioFilename() != null && wait)
        {
            sc.waitAudio();
        }
    }


    public OBPhoneme copy ()
    {
        List<Double> timingsClone = new ArrayList<Double>();
        for (Double timing : (List<Double>) (Object) timings)
        {
            timingsClone.add(new Double(timing));
        }
        return new OBPhoneme(text, soundID, (List<Object>) (Object) timingsClone, audio);
    }
}
