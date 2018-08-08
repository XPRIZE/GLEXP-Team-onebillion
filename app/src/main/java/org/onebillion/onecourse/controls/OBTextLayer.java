package org.onebillion.onecourse.controls;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 21/04/16.
 */
public class OBTextLayer extends OBLayer
{
    public static int JUST_CENTRE = 0,
    JUST_LEFT = 1,
    JUST_RIGHT = 2,
    JUST_FULL = 3;
    public StaticLayout stLayout;
    Typeface typeFace;
    float textSize;
    String text;
    int colour,bgColour;
    TextPaint textPaint;
    float lineOffset;
    float letterSpacing,lineSpaceMultiplier=1.0f,lineSpaceAdd;
    public int justification = JUST_CENTRE;
    Rect tempRect;
    SpannableString spanner;
    boolean displayObjectsValid = false;
    public float maxWidth = -1;
    List<List<Integer>> backgroundColourRanges;

    public class ColourRange
    {
        int st,en,col;
        public ColourRange(int colourStart,int colourEnd,int colour)
        {
            st = colourStart;
            en = colourEnd;
            col = colour;
        }
    }

    List<ColourRange> colourRanges = new ArrayList<>();

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
        obj.lineSpaceMultiplier = lineSpaceMultiplier;
        obj.lineSpaceAdd = lineSpaceAdd;
        obj.justification = justification;
        obj.maxWidth = maxWidth;
        obj.colourRanges = new ArrayList<>(colourRanges);
        return obj;
    }

    public void makeDisplayObjects(float maxw,int just)
    {
        textPaint.setTextSize(textSize);
        textPaint.setTypeface(typeFace);
        textPaint.setColor(colour);
        spanner = new SpannableString(text);

        //if (hiStartIdx >= 0)
          //  spanner.setSpan(new ForegroundColorSpan(hiRangeColour),hiStartIdx,hiEndIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        for (ColourRange cr : colourRanges)
            spanner.setSpan(new ForegroundColorSpan(cr.col),cr.st,cr.en, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (backgroundColourRanges != null && bgColour != 0)
        {
            for (List<Integer> range : backgroundColourRanges)
            {
                int st = range.get(0);
                int en = range.get(1);
                spanner.setSpan(new BackgroundColorSpan(bgColour),st,en,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        float mw = maxw > 0?maxw:(just==JUST_CENTRE)?bounds().width():4000;
        stLayout = new StaticLayout(spanner,textPaint,(int)Math.ceil(mw),
                (just==JUST_CENTRE)?Layout.Alignment.ALIGN_CENTER:Layout.Alignment.ALIGN_NORMAL,
                lineSpaceMultiplier,lineSpaceAdd,false);
        displayObjectsValid = true;
    }
    @Override

    public void draw(Canvas canvas)
    {
        if (!displayObjectsValid)
        {
            makeDisplayObjects(maxWidth,justification);
        }
        float l = 0;
        //if (justification == JUST_CENTER)
          //  l = (bounds().right - stLayout.getLineWidth(0)) / 2f;
        canvas.save();
        canvas.translate(l,0);
        stLayout.draw(canvas);
        canvas.restore();
    }

    public float baselineOffset()
    {
        if (!displayObjectsValid)
            makeDisplayObjects(maxWidth,justification);
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
        //if (!displayObjectsValid)
        makeDisplayObjects(maxWidth,JUST_LEFT);
        int linect = stLayout.getLineCount();
        bb.left = 0;
        bb.top = 0;
        float maxw = 0;
      /*  if (justification == JUST_CENTRE)
            maxw = maxWidth;
        else*/
        {
            for (int i = 0;i < linect;i++)
            {
                float w = stLayout.getLineWidth(i);
                float x = stLayout.getLineRight(i);
                Rect r = new Rect();
                if (w > maxw)
                    maxw = w;
            }
        }
        bb.right = maxw;
        bb.bottom = stLayout.getLineBottom(linect - 1);
    }
    public void sizeToBoundingBox()
    {
        RectF b = new RectF();
        calcBounds(b);
        setBounds(b);
        displayObjectsValid = false;
    }

    public void setBounds(RectF bounds)
    {
        //maxWidth = bounds.width();
        super.setBounds(bounds);
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
        //hiStartIdx = st;
        //hiEndIdx = en;
        //hiRangeColour = colour;
        colourRanges.clear();
        if (st >= 0)
            colourRanges.add(new ColourRange(st,en,colour));
        displayObjectsValid = false;
    }

    public void addColourRange(int st,int en,int colour)
    {
        if (st >= 0)
            colourRanges.add(new ColourRange(st,en,colour));
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

    public float lineSpaceAdd()
    {
        return lineSpaceAdd;
    }

    public void setLineSpaceAdd(float f)
    {
        this.lineSpaceAdd = f;
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
        colourRanges.clear();
        displayObjectsValid = false;
    }

    public void getSelectionPath(int start, int end, Path dest)
    {
        if (!displayObjectsValid)
            makeDisplayObjects(maxWidth,justification);
        stLayout.getSelectionPath(start,end,dest);
    }

    public void setJustification(int j)
    {
        justification = j;
    }

    public int justification()
    {
        return justification;
    }

    public void setBackgroundColourRanges(List<List<Integer>> backgroundColourRanges,int col)
    {
        this.backgroundColourRanges = backgroundColourRanges;
        bgColour = col;
        displayObjectsValid = false;
    }
}
