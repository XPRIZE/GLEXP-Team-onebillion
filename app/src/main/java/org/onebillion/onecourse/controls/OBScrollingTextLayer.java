package org.onebillion.onecourse.controls;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;

/**
 * Created by alan on 08/07/17.
 */

public class OBScrollingTextLayer extends OBTextLayer
{
    SpannableStringBuilder textBuffer;
    float yOffset = 0,width;
    public DynamicLayout dynLayout;

    public OBScrollingTextLayer(float w)
    {
        super();
        width = w;
    }

    public OBScrollingTextLayer(Typeface tf, float size, int col, String s,float w)
    {
        this(w);
        typeFace = tf;
        textSize = size;
        textBuffer = new SpannableStringBuilder(s);
        colour = col;
    }

    public void makeDisplayObjects(float maxw,int just)
    {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeFace);
        textPaint.setColor(colour);
        dynLayout = new DynamicLayout(textBuffer,textPaint,(int)width, Layout.Alignment.ALIGN_NORMAL,1,0,true);
        displayObjectsValid = true;
    }

    public void draw(Canvas canvas)
    {
        if (!displayObjectsValid)
        {
            makeDisplayObjects(width,justification);
        }
        float l = 0;
         canvas.save();
        canvas.translate(l,yOffset);
        dynLayout.draw(canvas);
        canvas.restore();
    }

    public String text()
    {
        if (textBuffer == null)
            return "";
        return textBuffer.toString();
    }

    public void setText(String text)
    {
        if (textBuffer == null)
        {
            textBuffer = new SpannableStringBuilder(text);
            displayObjectsValid = false;
        }
        else
            textBuffer.replace(0,textBuffer.length(),text);
    }

    public SpannableStringBuilder textBuffer()
    {
        return textBuffer;
    }

    public void setTextBuffer(SpannableStringBuilder textBuffer)
    {
        this.textBuffer = textBuffer;
        displayObjectsValid = false;
    }

    public float yOffset()
    {
        return yOffset;
    }

    public void setYOffset(float yOffset)
    {
        this.yOffset = yOffset;
    }

    public float width()
    {
        return width;
    }

    public void setWidth(float width)
    {
        this.width = width;
        displayObjectsValid = false;
    }

    public void appendString(String s)
    {
        textBuffer.append(s);
    }

    public DynamicLayout layout()
    {
        if (dynLayout == null)
            makeDisplayObjects(width,0);
        return dynLayout;
    }

}
