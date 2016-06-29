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
    public String text, audio, soundID;
    public List<Object> timings;
    Map<String, Object> properties;

    public OBPhoneme ()
    {
        super();
        this.properties = new HashMap<String, Object>();
    }

    public OBPhoneme (String text)
    {
        this();
        this.text = text;
    }

    public OBPhoneme (String text, String soundID)
    {
        this(text);
        this.soundID = soundID;
    }


    public OBPhoneme (String text, String soundID, List<Double> timings)
    {
        this(text, soundID);
        this.timings = new ArrayList<Object>(timings);
    }


    public void playAudio (OBSectionController sc, Boolean wait) throws Exception
    {
        if (timings != null && timings.size() > 1)
        {
            sc.playAudioFromTo(audio, (Double) timings.get(0), (Double) timings.get(1));
        }
        else if (audio != null)
        {
            sc.playAudio(audio);
        }
        if (audio != null && wait)
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
        return new OBPhoneme(text, soundID, timingsClone);
    }
}
