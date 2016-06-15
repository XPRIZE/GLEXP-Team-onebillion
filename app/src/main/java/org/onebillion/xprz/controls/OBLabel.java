package org.onebillion.xprz.controls;

import android.graphics.Color;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alan on 12/12/15.
 */
public class OBLabel extends OBControl
{
    public OBLabel()
    {
        super();

    }

    public OBLabel(String s,Typeface tf,float sz)
    {
        this();
        layer = new OBTextLayer(tf,sz, Color.BLACK,s);
        ((OBTextLayer)layer).sizeToBoundingBox();
    }

    @Override
    public OBControl copy()
    {
        OBLabel obj = (OBLabel)super.copy();
        return obj;
    }

    public void setColour(int col)
    {
        ((OBTextLayer)layer).colour = col;
        invalidate();
        if (texture != null)
        {
            needsRetexture = true;
        }
    }

    public void setString(String s)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        if (! tl.text.equals(s))
        {
            invalidate();
            synchronized (this)
            {
                tl.text = s;
                if (texture != null)
                {
                    needsRetexture = true;
                }
            }
            invalidate();
        }
    }
    public boolean needsTexture()
    {
        return true;
    }

    public float letterSpacing()
    {
        return ((OBTextLayer)layer).letterSpacing();
    }
    public void setLetterSpacing(float l)
    {
        ((OBTextLayer)layer).setLetterSpacing(l);
    }
}
