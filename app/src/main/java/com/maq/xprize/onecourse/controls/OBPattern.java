package com.maq.xprize.onecourse.controls;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by alan on 10/05/16.
 */
public class OBPattern
{
    public final static int PAR_NONE=0,
            PAR_SLICE=1,
            PAR_MEET=2,
            PAR_ALIGN_MIN=0,
            PAR_ALIGN_MID=1,
            PAR_ALIGN_MAX=2;
    public OBControl patternContents;
    public float x,y,patternWidth,patternHeight;
    public Matrix transform;
    public RectF viewBox;
    public boolean useBboxUnitsForPatternUnits,useBboxUnitsForPatternContentUnits,par_slice;
    public int preserveAspectRatio,xAlign,yAlign;

    public OBPattern()
    {
        useBboxUnitsForPatternUnits = true;
        useBboxUnitsForPatternContentUnits = false;
        transform = new Matrix();
    }

    public OBPattern copy()
    {
        OBPattern p = new OBPattern();
        p.patternContents = patternContents;
        p.patternWidth = patternWidth;
        p.patternHeight = patternHeight;
        p.x = x;
        p.y = y;
        p.transform = new Matrix(transform);
        p.viewBox = new RectF(viewBox);
        p.useBboxUnitsForPatternUnits = useBboxUnitsForPatternUnits;
        p.useBboxUnitsForPatternContentUnits = useBboxUnitsForPatternContentUnits;
        p.par_slice = par_slice;
        p.preserveAspectRatio = preserveAspectRatio;
        p.xAlign = xAlign;
        p.yAlign = yAlign;
        return p;
    }
}
