package com.maq.xprize.onecourse.utils;

/**
 * OBAnimBlock
 * Special OBAnim class for animations that require more complex animation than the ones provided in the constructors
 *
 * @see OBAnim
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