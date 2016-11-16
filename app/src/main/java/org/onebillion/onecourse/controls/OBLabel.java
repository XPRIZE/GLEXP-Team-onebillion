package org.onebillion.onecourse.controls;

import android.graphics.Color;
import android.graphics.Typeface;

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

    public void sizeToBoundingBox()
    {
        ((OBTextLayer)layer).sizeToBoundingBox();
        setNeedsRetexture();
        invalidate();
    }
    public void setColour(int col)
    {
        ((OBTextLayer)layer).setColour(col);
//        if (texture != null)
//        {
            setNeedsRetexture();
//        }
        invalidate();
    }

    public int colour()
    {
        return ((OBTextLayer)layer).colour();
    }

    public void setString(String s)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        if (! tl.text().equals(s))
        {
            invalidate();
            synchronized (this)
            {
                tl.setText(s);
                if (texture != null)
                {
                    setNeedsRetexture();
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

    public void setHighRange(int st,int en,int colour)
    {
        if (layer != null)
        {
            ((OBTextLayer)layer).setHighRange(st,en,colour);
            setNeedsRetexture();
            invalidate();
        }
     }

    public String text()
    {
        return ((OBTextLayer)layer).text;
    }

    public Typeface typeface()
    {
        return ((OBTextLayer)layer).typeFace;
    }

    public void setTypeFace(Typeface tf)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        tl.typeFace = tf;
        tl.displayObjectsValid = false;
    }
    public float fontSize()
    {
        return ((OBTextLayer)layer).textSize;
    }

    public void setFontSize(float f)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        if (tl.textSize != f)
        {
            synchronized (this)
            {
                tl.setTextSize(f);
                if (texture != null)
                {
                    setNeedsRetexture();
                }
            }
            invalidate();
        }
    }

    public float baselineOffset()
    {
        OBTextLayer tl = (OBTextLayer)layer;
        return tl.baselineOffset();
    }

    public float textOffset(int idx)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        return tl.textOffset(idx);
    }

    public void setMaxWidth(float f)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        tl.maxWidth = f;
        setNeedsLayout();
    }
    public void setJustification(int j)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        tl.justification = j;
    }

    public void setNeedsLayout()
    {
        OBTextLayer tl = (OBTextLayer)layer;
        tl.displayObjectsValid = false;
    }

    public float lineSpaceMultiplier()
    {
        OBTextLayer tl = (OBTextLayer)layer;
        return tl.lineSpaceMultiplier;
    }

    public void setLineSpaceMultiplier(float f)
    {
        OBTextLayer tl = (OBTextLayer)layer;
        tl.lineSpaceMultiplier = f;
    }
}
