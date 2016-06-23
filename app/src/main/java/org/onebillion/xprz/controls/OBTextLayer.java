package org.onebillion.xprz.controls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;

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
    int hiStartIdx=-1,hiEndIdx=-1;
    int hiRangeColour;


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

    public void drawHighText(Canvas canvas)
    {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(hiRangeColour),hiStartIdx,hiEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextPaint txpaint = new TextPaint(textPaint);
        txpaint.setColor(Color.RED);
        StaticLayout ly = new StaticLayout(ss,txpaint,tempRect.width(), Layout.Alignment.ALIGN_NORMAL,1,0,false);
        float textStart = (bounds().right - tempRect.right) / 2;
        canvas.save();
        canvas.translate(0,0);
        ly.draw(canvas);
        canvas.restore();
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
        hiStartIdx = 0;hiEndIdx = text.length();
        //if (hiStartIdx >= 0)
          //  drawHighText(canvas);
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

    public void setHighRange(int st,int en,int colour)
    {
        hiStartIdx = st;
        hiEndIdx = en;
        hiRangeColour = colour;
    }
}
