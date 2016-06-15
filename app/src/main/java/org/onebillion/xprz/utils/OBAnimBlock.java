package org.onebillion.xprz.utils;

/**
 * Created by alan on 11/06/16.
 */
abstract public class OBAnimBlock extends OBAnim
{
    float f;
    public OBAnimBlock()
    {
        super(null, null, ANIM_TYPE_BLOCK);
    }
    abstract public void runAnimBlock(float frac);

}