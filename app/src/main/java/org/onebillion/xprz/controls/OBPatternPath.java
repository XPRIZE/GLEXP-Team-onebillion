package org.onebillion.xprz.controls;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import org.onebillion.xprz.utils.OBUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by alan on 10/05/16.
 */
public class OBPatternPath extends OBPath
{
    public OBShapeLayer strokeLayer,maskLayer;
    public OBLayer patternLayer;
    public OBPattern pattern;
    public Matrix objectTransform,patternTransform,tileTransform;
    public RectF patternRect;
    Bitmap cache;

    public OBPatternPath()
    {
        layer = new OBLayer();
        strokeLayer = new OBShapeLayer();
        maskLayer = new OBShapeLayer();
        patternLayer = new OBLayer();

        objectTransform = new Matrix();
        patternTransform = new Matrix();
        tileTransform = new Matrix();
    }

    public OBPatternPath(Path p)
    {
        this();
        setPath(p);
    }
    public OBControl copy()
    {
        OBPatternPath obj = (OBPatternPath)super.copy();
        obj.pattern = pattern;
        obj.objectTransform = new Matrix(objectTransform);
        obj.patternTransform = new Matrix(patternTransform);
        obj.tileTransform = new Matrix(tileTransform);
        obj.patternRect = new RectF(patternRect);
        obj.adjustLayers();
        //
        obj.strokeLayer = (OBShapeLayer) strokeLayer.copy();
        obj.maskLayer = (OBShapeLayer) maskLayer.copy();
        obj.patternLayer = patternLayer.copy();
        //
        return obj;
    }

    public OBShapeLayer shapeLayer()
    {
        return strokeLayer;
    }
    public void adjustLayers()
    {
        patternLayer.setBounds(bounds());
        strokeLayer.setBounds(bounds());
        maskLayer.setBounds(bounds());
    }
    public void setFrame(RectF f)
    {
        super.setFrame(f);
        adjustLayers();
    }
    public void setPath(Path p)
    {
        strokeLayer.path = p;
        maskLayer.path = p;
    }
    public void createCache()
    {
        float sc = OBUtils.scaleFromTransform(objectTransform);
        if (sc < 1)
            sc = 1;
        RectF b = pattern.patternContents.bounds();
        int width = (int) Math.ceil(b.width() * sc);
        int height = (int) Math.ceil(b.height() * sc);
        if (width == 0 || height == 0)
            Log.i("error","createCache");
        cache = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cache);
        Matrix m = new Matrix();
        m.preScale(sc, sc);
        m.preConcat(tileTransform);
        canvas.concat(m);
        if (patternLayer != null)
        {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            pattern.patternContents.drawLayer(canvas);
        }
    }


    public void drawLayer(Canvas canvas)
    {
        if (layer != null)
        {
            canvas.save();
            if (cache == null)
                createCache();
            if (maskLayer != null)
                canvas.clipPath(maskLayer.path);
            canvas.save();
            canvas.concat(objectTransform);
            canvas.concat(patternTransform);
            //canvas.concat(tileTransform);
            RectF clipRect = new RectF(canvas.getClipBounds());
            float leftx = patternRect.left;
            float pw = patternRect.width();
            while (leftx > clipRect.left)
                leftx -= pw;
            while (leftx + pw < clipRect.left)
                leftx += pw;
            float topy = patternRect.top;
            float ph = patternRect.height();
            while (topy > clipRect.top)
                topy -= ph;
            while (topy + ph < clipRect.top)
                topy += ph;
            BitmapShader shader = new BitmapShader(cache, Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.FILL);
            p.setShader(shader);
            canvas.drawRect(leftx,topy,clipRect.right,clipRect.bottom,p);
            canvas.restore();
            if (strokeLayer != null)
                strokeLayer.draw(canvas);
            canvas.restore();
        }
    }

    public void takeValuesFrom(OBPattern patt, List<Map<String,Object>> settingsStack)
    {
        pattern = patt;
        Map<String, Object> settings = settingsStack.get(settingsStack.size() - 1);
        RectF originalbounds = (RectF) settings.get("originalbounds");
        Matrix t = (Matrix) settings.get("transform");
        if (t == null)
            objectTransform = new Matrix();
        else
            objectTransform = new Matrix(t);
        Matrix ot = new Matrix();
        ot.setTranslate(-originalbounds.left,-originalbounds.top);
        objectTransform.preConcat(ot);
        patternTransform = pattern.transform;
        patternRect = new RectF(pattern.x,pattern.y,pattern.x + pattern.patternWidth,pattern.y + pattern.patternHeight);
        if (pattern.useBboxUnitsForPatternUnits)
        {
            float elements[] = {1,0,0,0,1,0,0,0,1};
            elements[0] = pattern.patternWidth;
            elements[4] = pattern.patternHeight;
            elements[2] = pattern.x;
            elements[5] = pattern.y;
            Matrix tr = new Matrix();
            tr.setValues(elements);
            tr.mapRect(patternRect);
        }
        if (!(pattern.viewBox == null || pattern.viewBox.isEmpty()))
        {
            RectF vb  = pattern.viewBox;
            float wratio = vb.width() / pattern.patternWidth;
            float hratio = vb.height() / pattern.patternHeight;
            t = new Matrix();
            if (pattern.preserveAspectRatio == OBPattern.PAR_NONE || wratio == hratio)
            {
            }
            else if (pattern.preserveAspectRatio == OBPattern.PAR_SLICE)
            {
                if (wratio > hratio)
                {
                    float newheight = pattern.patternHeight * wratio;
                    float extra = newheight - vb.height();
                    if (pattern.yAlign == OBPattern.PAR_ALIGN_MAX)
                        vb.top += extra;
                    else if (pattern.yAlign == OBPattern.PAR_ALIGN_MIN)
                        vb.top -= extra;
                    else
                        vb.top -= (extra / 2);
                    hratio = wratio;
                }
                else
                {
                    float newwidth = pattern.patternWidth * hratio;
                    float extra = newwidth - vb.width();
                    if (pattern.xAlign == OBPattern.PAR_ALIGN_MAX)
                        vb.left += extra;
                    else if (pattern.xAlign == OBPattern.PAR_ALIGN_MIN)
                        vb.left -= extra;
                    else
                        vb.left -= (extra / 2);
                    wratio = hratio;
                }
            }
            else
            {
                if (wratio > hratio)
                {
                    float newheight = pattern.patternHeight * wratio;
                    float extra = newheight - vb.height();
                    if (pattern.yAlign == OBPattern.PAR_ALIGN_MAX)
                        vb.top += extra;
                    else if (pattern.yAlign == OBPattern.PAR_ALIGN_MIN)
                        vb.top -= extra;
                    else
                        vb.top -= (extra / 2);
                    wratio = hratio;
                }
                else
                {
                    float newwidth = pattern.patternWidth * hratio;
                    float extra = newwidth - vb.width();
                    if (pattern.xAlign == OBPattern.PAR_ALIGN_MAX)
                        vb.left += extra;
                    else if (pattern.xAlign == OBPattern.PAR_ALIGN_MIN)
                        vb.left -= extra;
                    else
                        vb.left -= (extra / 2);
                    hratio = wratio;
                }

            }
            t = new Matrix();
            t.setTranslate(-vb.left, -vb.top);
            t.preScale(1/wratio, 1/hratio);
            tileTransform = t;
        }
        else
        {
            if (pattern.useBboxUnitsForPatternContentUnits)
            {
                float xscale = originalbounds.width() / pattern.patternWidth;
                float yscale = originalbounds.height() / pattern.patternHeight;
                tileTransform = new Matrix();
                tileTransform.setScale(xscale, yscale);
            }
            else
                tileTransform = new Matrix();
        }

    }
}
