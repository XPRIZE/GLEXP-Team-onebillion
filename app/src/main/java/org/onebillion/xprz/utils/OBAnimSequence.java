package org.onebillion.xprz.utils;

import android.os.SystemClock;

import org.onebillion.xprz.controls.OBGroup;

import java.util.List;

/**
 * Created by michal on 16/06/16.
 */
public class OBAnimSequence extends OBAnim{
    List<String> frames;
    float interFrameDelay;
    int index;
    long lastFrameChange;
    boolean repeat;

    public  OBAnimSequence(OBGroup obj, List<String> frames, float interFrameDelay, boolean repeat) {
        super(obj, "sequenceIndex", ANIM_TYPE_SEQUENCE);
        obj.setSequence(frames);
        this.frames = frames;
        this.interFrameDelay = interFrameDelay;
        this.repeat = repeat;
        index=-1;
    }

    public Object valueForT(float t)
    {
        long currTime = SystemClock.uptimeMillis();
        long diff = (currTime - lastFrameChange)/100;
        double noFrames = Math.floor(diff/interFrameDelay);
        int size = frames.size();
        if (noFrames >= 1)
        {
            index = index + 1;
            if (index >= size)
            {
                if (repeat)
                    index = 0;
                else
                    index = size - 1;
            }
            lastFrameChange = currTime;
            return index;
        }
        else
            return index;
    }

}
