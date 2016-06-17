package org.onebillion.xprz.controls;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Created by alan on 21/04/16.
 */
public class OBTextLayer extends OBLayer
{

    public Typeface typeFace;
    public float textSize;
    public String text;
    public int colour;
    public Paint textPaint;
    float lineOffset;


    float letterSpacing;
    Rect tempRect;

    public OBTextLayer(Typeface tf,float size,int col,String s)
    {
        super();
        typeFace = tf;
        textSize = size;
        text = s;
        colour = col;
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        tempRect = new Rect();
    }
    @Override
    public OBLayer copy()
    {
        OBTextLayer obj = (OBTextLayer)super.copy();
        obj.tempRect = new Rect();
        obj.typeFace = typeFace;
        obj.textSize = textSize;
        obj.text = text;
        obj.colour = colour;
        obj.textPaint = new Paint(textPaint);
        obj.lineOffset = lineOffset;
        obj.letterSpacing = letterSpacing;
        return obj;
    }

    @Override
    public void draw(Canvas canvas)
    {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeFace);
        textPaint.setColor(colour);
        if (letterSpacing != 0)
            textPaint.setLetterSpacing(letterSpacing / textSize);
        textPaint.getTextBounds(text, 0, text.length(), tempRect);
        if (letterSpacing != 0)
            tempRect.right += (text.length() * letterSpacing);
        float textStart = (bounds().right - tempRect.right) / 2;
        canvas.drawText(text,textStart,lineOffset,textPaint);
    }


    public void calcBounds(RectF bb)
    {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float maxAscender = fm.top;
        float maxDescender = fm.bottom;
        lineOffset = -maxAscender;
        Rect b = new Rect();
        textPaint.getTextBounds(text,0,text.length(),b);
        float ex = b.left;
        bb.right = b.right /*- b.left*/;
        if (letterSpacing != 0)
            bb.right += (text.length() * letterSpacing);
        bb.right = textPaint.measureText(text) + ex;
        if (letterSpacing != 0)
            bb.right += (text.length() * letterSpacing);
        bb.left = 0;
        bb.top = 0;
        bb.bottom = (int)(Math.ceil((double)(maxDescender - maxAscender)));
    }
    public void sizeToBoundingBox()
    {
        RectF b = new RectF();
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeFace);
        if (letterSpacing != 0)
            textPaint.setLetterSpacing(letterSpacing / textSize);
        calcBounds(b);
        setBounds(b);
    }

    public float letterSpacing()
    {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing)
    {
        this.letterSpacing = letterSpacing;
    }

}
