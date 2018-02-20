package org.onebillion.onecourse.utils;

import org.onebillion.onecourse.mainui.OBSectionController;

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
    public String soundid;
    public Map<String, Object> properties;


    public OBPhoneme ()
    {
        this(null, null, null);
    }

    public OBPhoneme (String text)
    {
        this(text, null, null);
    }

    public OBPhoneme (String text, String soundID)
    {
        this(text, soundID, null);
    }


    public OBPhoneme (String text, String soundID, List<Object> timings)
    {
        this(text, soundID, timings, new HashMap<String, Object>());
    }


    public OBPhoneme (String text, String soundID, List<Object> timings, HashMap<String, Object> properties)
    {
        super();
        this.text = text;
        this.soundid = soundID;
        this.timings = (timings == null) ? new ArrayList<Object>() : new ArrayList<Object>(timings);
        this.properties = (properties == null) ? new HashMap<String, Object>() : new HashMap<String, Object>(properties);
    }


    public String audio ()
    {
        return soundid;
    }


  /* public void playAudio (OBSectionController sc, Boolean wait) throws Exception
    {
        if (timings != null && timings.size() > 1)
        {
            sc.playAudioFromTo(audio(), (Double) timings.get(0), (Double) timings.get(1));
        }
        else if (audio() != null)
        {
            sc.playAudio(audio());
        }
        if (audio() != null && wait)
        {
            sc.waitAudio();
        }
    }*/

    public void playAudio (OBSectionController sc, Boolean wait) throws Exception
    {
        if (timings != null && timings.size() > 1)
        {
            OBAudioBufferPlayer player =  sc.playAudioFromToP(audio(), (Double) timings.get(0), (Double) timings.get(1));
            if(wait)
                player.waitAudio();
        }
        else if (audio() != null)
        {
            sc.playAudio(audio());
            if (wait)
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
        return new OBPhoneme(text, soundid, (List<Object>) (Object) timingsClone);
    }
}
