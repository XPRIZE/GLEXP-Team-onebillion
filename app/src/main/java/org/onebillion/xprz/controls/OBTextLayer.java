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
    public static int JUST_CENTER = 0,
    JUST_LEFT = 1,
    JUST_RIGHT = 2;
    public StaticLayout stLayout;
    Typeface typeFace;
    float textSize;
    String text;
    int colour;
    TextPaint textPaint;
    float lineOffset;
    int hiStartIdx=-1,hiEndIdx=-1;
    int hiRangeColour;
    float letterSpacing;
    int justification = JUST_CENTER;
    Rect tempRect;
    SpannableString spanner;
    boolean displayObjectsValid = false;

    public OBTextLayer()
    {
        super();
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        tempRect = new Rect();
    }

    public OBTextLayer(Typeface tf,float size,int col,String s)
    {
        this();
        typeFace = tf;
        textSize = size;
        text = s;
        colour = col;
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
        obj.textPaint = new TextPaint(textPaint);
        obj.lineOffset = lineOffset;
        obj.letterSpacing = letterSpacing;
        obj.hiStartIdx = hiStartIdx;
        obj.hiEndIdx = hiEndIdx;
        obj.hiRangeColour = hiRangeColour;
        obj.justification = justification;
        return obj;
    }

    public void drawHighText(Canvas canvas)
    {
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(hiRangeColour),hiStartIdx,hiEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        TextPaint txpaint = new TextPaint(textPaint);
        txpaint.setColor(Color.RED);
        StaticLayout ly = new StaticLayout(ss,txpaint,tempRect.width(), Layout.Alignment.ALIGN_NORMAL,1,0,false);
        float l = 0;
        if (justification == JUST_CENTER)
            l = (bounds().right - ly.getLineWidth(0)) / 2f;
        canvas.save();
        canvas.translate(l,0);
        ly.draw(canvas);
        canvas.restore();
    }

    public void makeDisplayObjects()
    {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeFace);
        textPaint.setColor(colour);
        spanner = new SpannableString(text);
        if (hiStartIdx >= 0)
            spanner.setSpan(new ForegroundColorSpan(hiRangeColour),hiStartIdx,hiEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        stLayout = new StaticLayout(spanner,textPaint,4000, Layout.Alignment.ALIGN_NORMAL,1,0,false);
        displayObjectsValid = true;
    }
    @Override

    public void draw(Canvas canvas)
    {
        if (!displayObjectsValid)
        {
            makeDisplayObjects();
        }
        float l = 0;
        if (justification == JUST_CENTER)
            l = (bounds().right - stLayout.getLineWidth(0)) / 2f;
        canvas.save();
        canvas.translate(l,0);
        stLayout.draw(canvas);
        canvas.restore();
    }
    public void drawo(Canvas canvas)
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
    }

    public float baselineOffset()
    {
        if (!displayObjectsValid)
            makeDisplayObjects();
        return stLayout.getLineBaseline(0);
    }

    public float textWidth(String tx)
    {
        TextPaint tp = new TextPaint();
        tp.setTextSize(textSize);
        tp.setTypeface(typeFace);
        tp.setColor(colour);
        SpannableString ss = new SpannableString(tx);
        StaticLayout sl = new StaticLayout(ss,tp,4000, Layout.Alignment.ALIGN_NORMAL,1,0,false);
        return sl.getLineWidth(0);
    }
    public float textOffset(int idx)
    {
        if (idx == 0)
            return 0;
        return textWidth(text.substring(0,idx + 1)) - textWidth(text.substring(idx,idx + 1));
    }
    public void calcBounds(RectF bb)
    {
        if (!displayObjectsValid)
            makeDisplayObjects();
        bb.left = 0;
        bb.top = 0;
        bb.right = stLayout.getLineWidth(0);
        bb.bottom = stLayout.getLineBottom(0);
    }
    public void sizeToBoundingBox()
    {
        RectF b = new RectF();
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
        displayObjectsValid = false;
    }

    public void setHighRange(int st,int en,int colour)
    {
        hiStartIdx = st;
        hiEndIdx = en;
        hiRangeColour = colour;
        displayObjectsValid = false;
    }


    public Typeface typeFace()
    {
        return typeFace;
    }

    public void setTypeFace(Typeface typeFace)
    {
        this.typeFace = typeFace;
        displayObjectsValid = false;
    }

    public float textSize()
    {
        return textSize;
    }

    public void setTextSize(float textSize)
    {
        this.textSize = textSize;
        displayObjectsValid = false;
    }

    public String text()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
        displayObjectsValid = false;
    }

    public int colour()
    {
        return colour;
    }

    public void setColour(int colour)
    {
        this.colour = colour;
        displayObjectsValid = false;
    }

}
