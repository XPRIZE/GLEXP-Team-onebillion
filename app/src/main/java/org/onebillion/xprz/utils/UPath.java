package org.onebillion.xprz.utils;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 06/02/16.
 */
public class UPath
{
    float length;
    List<USubPath> subPaths;

    public UPath()
    {
        length = -1;
        subPaths = new ArrayList<>();
    }

    public float length()
    {
        if (length < 0)
            calcLength();
        return length;
    }

    public void calcLength()
    {
        length = 0;
        for (USubPath sp : subPaths)
            length += sp.length();
    }

    public Path bezierPath()
    {
        Path bez = new Path();
        for (USubPath sp : subPaths)
            bez.addPath(sp.bezierPath());
        return bez;
    }


}
