package org.onebillion.onecourse.controls;

import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Created by alan on 08/07/17.
 */

public class OBScrollingText extends OBLabel
{
    public OBScrollingText(RectF frme)
    {
        super();
        layer = new OBScrollingTextLayer(frme.width());
        setFrame(frme);
    }

    public OBScrollingText(String s, Typeface tf, float sz,RectF frme)
    {
        super();
        layer = new OBScrollingTextLayer(tf,sz, Color.BLACK,s,frme.width());
        setFrame(frme);
    }

    public float yOffset()
    {
        return ((OBScrollingTextLayer)layer).yOffset();
    }

    public void setYOffset(float yOffset)
    {
        ((OBScrollingTextLayer)layer).setYOffset(yOffset);
    }

    public StringBuffer textBuffer()
    {
        return ((OBScrollingTextLayer)layer).textBuffer();
    }

    public void appendString(String s)
    {
        ((OBScrollingTextLayer)layer).appendString(s);
    }

    public int charLength()
    {
        return textBuffer().length();
    }

    
}
