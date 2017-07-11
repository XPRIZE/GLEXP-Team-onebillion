package org.onebillion.onecourse.controls;

import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.DynamicLayout;
import android.text.SpannableStringBuilder;

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
        setNeedsRetexture();
        invalidate();
    }

    public SpannableStringBuilder textBuffer()
    {
        return ((OBScrollingTextLayer)layer).textBuffer();
    }

    public DynamicLayout layout()
    {
        return ((OBScrollingTextLayer)layer).layout();
    }

    public void appendString(String s)
    {
        ((OBScrollingTextLayer)layer).appendString(s);
        setNeedsRetexture();
        invalidate();
    }

    public void deleteCharacters(int start,int end)
    {
        textBuffer().delete(start,end);
        setNeedsRetexture();
        invalidate();
    }

    public int charLength()
    {
        return textBuffer().length();
    }

    public int indexOfLastLine()
    {
        return layout().getLineCount() - 1;
    }

    public float yOfLastLine()
    {
        return layout().getLineBottom(indexOfLastLine());
    }

    public float midYOfLastLine()
    {
        int i = indexOfLastLine();
        float offset = ((OBScrollingTextLayer)layer).yOffset;
        return (layout().getLineTop(i) + layout().getLineBaseline(i)) / 2f + offset;
    }

    public float topOffsetOfLastLine()
    {
        int i = indexOfLastLine();
        return layout().getLineTop(i);
    }

    public float xOfLastLine()
    {
        //int i = indexOfLastLine();
        //return layout().getLineWidth(i);
        //float f = layout().getLineRight(i);
        //f = layout().getParagraphLeft(i);
        //f = layout().getOffsetToRightOf(charLength());
        //return layout().getLineRight(i) + layout().getParagraphLeft(i);
        return layout().getPrimaryHorizontal(charLength());
    }

    public float capHeight()
    {
        return (layout().getLineBaseline(0) - layout().getLineTop(0));
    }

    public void scrollCursorToVisible()
    {
        float offset = ((OBScrollingTextLayer)layer).yOffset;
        float amountToScroll = 0;
        int i = indexOfLastLine();
        float top = layout().getLineTop(i);
        if (top + offset < 0)
        {
            amountToScroll = top + offset;
        }
        else
        {
            float bottom = layout().getLineBottom(i);
            if (bottom + offset > height())
            {
                amountToScroll = -(bottom + offset - height());
            }
        }
        if (amountToScroll != 0)
        {
            ((OBScrollingTextLayer)layer).setYOffset(offset + amountToScroll);
            invalidate();
            setNeedsRetexture();
        }
    }
}
