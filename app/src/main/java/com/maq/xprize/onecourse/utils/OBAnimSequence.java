package com.maq.xprize.onecourse.utils;

import android.os.SystemClock;

import com.maq.xprize.onecourse.controls.OBGroup;

import java.util.List;

/**
 * OBAnimSequence
 * Animates an OBGroup, changing the visibility of layers in the OBGroup in a sequence specified in the constructor
 * All layers indicated in the constructor are hidden and reveal in the correct sequence.
 * Unmentioned layers in the OBGroup are left untouched
 *
 * @see OBGroup
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
