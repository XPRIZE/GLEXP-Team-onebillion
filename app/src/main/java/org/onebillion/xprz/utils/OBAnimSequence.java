package org.onebillion.xprz.utils;

import java.util.List;

/**
 * Created by michal on 16/06/16.
 */
public class OBAnimSequence extends OBAnim{
    List<String> frames;
    float interFrameDelay;
    int index;
    double lastFrameChange;
    boolean repeat;

    public  OBAnimSequence(List<String> frames, float interFrameDelay, boolean repeat) {
        super(null, null, ANIM_TYPE_SEQUENCE);
        this.frames = frames;
        this.interFrameDelay = interFrameDelay;
        this.repeat = repeat;
        index=-1;
    }



}
