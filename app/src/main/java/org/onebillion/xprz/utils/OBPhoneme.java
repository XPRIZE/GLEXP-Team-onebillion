package org.onebillion.xprz.utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 27/06/16.
 */
public class OBPhoneme
{
    String text, audio, soundid;
    List<List<Float>> timings = new ArrayList<>();
    Map<String,Object> properties = new HashMap<>();

    public OBPhoneme()
    {
        super();
    }
    public OBPhoneme(String text)
    {
        super();
        this.text = text;
    }

    public OBPhoneme(String sound,String text)
    {
        this(text);
        this.audio = sound;
        this.soundid = sound;
    }
    public OBPhoneme(String sound,String text,List<List<Float>> tims)
    {
        this(sound,text);
        timings = tims;
    }
    public OBPhoneme copy()
    {
        OBPhoneme obj;
        try
        {
            Constructor<?> cons;
            cons = getClass().getConstructor();
            obj = (OBPhoneme)cons.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        obj.text = text;
        obj.audio = audio;
        obj.soundid = soundid;
        obj.timings = new ArrayList<>(timings);
        obj.properties = new HashMap<>(properties);
        return obj;
    }
}
