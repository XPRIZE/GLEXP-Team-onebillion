package org.onebillion.xprz.controls;


import android.graphics.*;

import org.onebillion.xprz.utils.OBXMLNode;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_MutInt;
import org.onebillion.xprz.utils.UGradient;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.URadialGradient;
import org.onebillion.xprz.utils.USubPath;

import java.util.*;

public class OBPath extends OBControl
{
    static Class classForSettings(Map<String, Object> settings)
    {
        Object f = settings.get("fill");
        if (f != null && f instanceof String)
        {
            String s = (String) f;
            if (s.startsWith("url("))
            {
                String url = s.substring(5, s.length() - 1);
                Map<String, Object> defs = (Map<String, Object>) settings.get("defs");
                Object obj = defs.get(url);
                if (obj != null)
                {
                    if (obj instanceof URadialGradient)
                        return OBRadialGradientPath.class;
                    if (obj instanceof UGradient)
                        return OBGradientPath.class;
                    if (obj instanceof OBPattern)
                        return OBPatternPath.class;
                }
            }
        }
        return OBPath.class;
    }

    protected static final String svgPathDelimChars = "\\s|,",
            svgFloatChars = "[0-9.e]",
            svgCommandChars = "[zZMmLlHhVvCcSsQqTt]";
    float strokeStart, strokeEnd;

    static boolean validFloatChar(char ch)
    {
        return (Character.isDigit(ch) || ch == '.' || ch == 'e');
    }

    static boolean isCommandChar(char ch)
    {
        for (int i = 1; i < svgCommandChars.length() - 1; i++)
            if (ch == svgCommandChars.charAt(i))
                return true;
        return false;
    }

    static int skipdelims(String str, int idx)
    {
        while (idx < str.length())
        {
            char ch = str.charAt(idx);
            if (!(ch <= ' ' || ch == ','))
                return idx;
            idx++;
        }
        return idx;
    }

    static Object[] getFloat(String str, int idx)
    {
        StringBuilder rstr = new StringBuilder();
        idx = skipdelims(str, idx);
        if (idx < str.length())
        {
            if (str.substring(idx, idx + 1).equals("-"))
            {
                idx++;
                rstr.append("-");
            }
        }
        while (idx < str.length())
        {
            String ch = str.substring(idx, idx + 1);
            if (!ch.matches(svgFloatChars))
                break;
            rstr.append(ch);
            idx++;
        }
        float f = Float.parseFloat(rstr.toString());
        Object[] res = {Float.valueOf(f), Integer.valueOf(idx)};
        return res;
    }

    static float getFloat(String str, OB_MutInt midx)
    {
        int idx = skipdelims(str, midx.value);
        int startidx = idx;
        if (idx < str.length())
        {
            if (str.charAt(idx) == '-')
                idx++;
        }
        while (idx < str.length())
        {
            char ch = str.charAt(idx);
            if (!validFloatChar(ch))
                break;
            idx++;
        }
        float f = Float.parseFloat(str.substring(startidx, idx));
        midx.value = idx;
        return f;
    }

    public static USubPath uSubPathFromSVGPath(String str)
    {
        USubPath path = new USubPath();
        OB_MutInt midx = new OB_MutInt(skipdelims(str, 0));
        float currx = 0f, curry = 0f, cx1, cy1, cx2, cy2, dx, dy, qx, qy;
        char implicitCommand = 'M', lastCommand = 0, uc = 0;
        PointF lastCurvePoint = new PointF(0f, 0f);
        while (midx.value < str.length())
        {
            lastCommand = uc;
            uc = str.charAt(midx.value);
            if (isCommandChar(uc))
                midx.value++;
            else
                uc = implicitCommand;
            switch (uc)
            {
                case 'M':
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.moveTo(currx, curry);
                    implicitCommand = 'L';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'm':
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.moveTo(currx, curry);
                    implicitCommand = 'l';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'L':
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.lineTo(currx, curry);
                    lastCurvePoint.set(0f, 0f);
                    implicitCommand = 'L';
                    break;
                case 'l':
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'l';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'H':
                    currx = getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'H';
                    break;
                case 'h':
                    currx += getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'h';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'V':
                    curry = getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'V';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'v':
                    curry += getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'v';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'C':
                    cx1 = getFloat(str, midx);
                    cy1 = getFloat(str, midx);
                    cx2 = getFloat(str, midx);
                    cy2 = getFloat(str, midx);
                    lastCurvePoint.set(cx2, cy2);
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'C';
                    break;
                case 'c':
                    cx1 = currx + getFloat(str, midx);
                    cy1 = curry + getFloat(str, midx);
                    cx2 = currx + getFloat(str, midx);
                    cy2 = curry + getFloat(str, midx);
                    lastCurvePoint.set(cx2, cy2);
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'c';
                    break;
                case 'S':
                    if ("CcSs".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[CcSs]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    cx1 = currx + dx;
                    cy1 = curry + dy;
                    cx2 = getFloat(str, midx);
                    cy2 = getFloat(str, midx);
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    lastCurvePoint.set(cx2, cy2);
                    implicitCommand = 'S';
                    break;
                case 's':
                    if ("CcSs".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[CcSs]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    cx1 = currx + dx;
                    cy1 = curry + dy;
                    cx2 = currx + getFloat(str, midx);
                    cy2 = curry + getFloat(str, midx);
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    lastCurvePoint.set(cx2, cy2);
                    implicitCommand = 's';
                    break;
                case 'Q':
                    qx = getFloat(str, midx);
                    qy = getFloat(str, midx);
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'Q';
                    break;
                case 'q':
                    qx = currx + getFloat(str, midx);
                    qy = curry + getFloat(str, midx);
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'q';
                    break;
                case 'T':
                    if ("QqTt".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[QqTt]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    qx = currx + dx;
                    qy = curry + dy;
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'T';
                    break;
                case 't':
                    if ("QqTt".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[QqTt]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    qx = currx + dx;
                    qy = curry + dy;
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 't';
                    break;
                case 'z':
                case 'Z':
                    path.close();
                    break;
                default:
                    break;
            }
            midx.value = skipdelims(str, midx.value);
        }
        return path;
    }

    public static Path pathFromSVGPath(String str)
    {
        Path path = new Path();
        OB_MutInt midx = new OB_MutInt(skipdelims(str, 0));
        float currx = 0f, curry = 0f, cx1, cy1, cx2, cy2, dx, dy, qx, qy;
        char implicitCommand = 'M', lastCommand = 0, uc = 0;
        PointF lastCurvePoint = new PointF(0f, 0f);
        while (midx.value < str.length())
        {
            lastCommand = uc;
            uc = str.charAt(midx.value);
            if (isCommandChar(uc))
                midx.value++;
            else
                uc = implicitCommand;
            switch (uc)
            {
                case 'M':
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.moveTo(currx, curry);
                    implicitCommand = 'L';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'm':
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.moveTo(currx, curry);
                    implicitCommand = 'l';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'L':
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.lineTo(currx, curry);
                    lastCurvePoint.set(0f, 0f);
                    implicitCommand = 'L';
                    break;
                case 'l':
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'l';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'H':
                    currx = getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'H';
                    break;
                case 'h':
                    currx += getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'h';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'V':
                    curry = getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'V';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'v':
                    curry += getFloat(str, midx);
                    path.lineTo(currx, curry);
                    implicitCommand = 'v';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'C':
                    cx1 = getFloat(str, midx);
                    cy1 = getFloat(str, midx);
                    cx2 = getFloat(str, midx);
                    cy2 = getFloat(str, midx);
                    lastCurvePoint.set(cx2, cy2);
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'C';
                    break;
                case 'c':
                    cx1 = currx + getFloat(str, midx);
                    cy1 = curry + getFloat(str, midx);
                    cx2 = currx + getFloat(str, midx);
                    cy2 = curry + getFloat(str, midx);
                    lastCurvePoint.set(cx2, cy2);
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'c';
                    break;
                case 'S':
                    if ("CcSs".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[CcSs]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    cx1 = currx + dx;
                    cy1 = curry + dy;
                    cx2 = getFloat(str, midx);
                    cy2 = getFloat(str, midx);
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    lastCurvePoint.set(cx2, cy2);
                    implicitCommand = 'S';
                    break;
                case 's':
                    if ("CcSs".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[CcSs]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    cx1 = currx + dx;
                    cy1 = curry + dy;
                    cx2 = currx + getFloat(str, midx);
                    cy2 = curry + getFloat(str, midx);
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    lastCurvePoint.set(cx2, cy2);
                    implicitCommand = 's';
                    break;
                case 'Q':
                    qx = getFloat(str, midx);
                    qy = getFloat(str, midx);
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'Q';
                    break;
                case 'q':
                    qx = currx + getFloat(str, midx);
                    qy = curry + getFloat(str, midx);
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'q';
                    break;
                case 'T':
                    if ("QqTt".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[QqTt]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    qx = currx + dx;
                    qy = curry + dy;
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'T';
                    break;
                case 't':
                    if ("QqTt".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[QqTt]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    qx = currx + dx;
                    qy = curry + dy;
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    path.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 't';
                    break;
                case 'z':
                case 'Z':
                    path.close();
                    break;
                default:
                    break;
            }
            midx.value = skipdelims(str, midx.value);
        }
        return path;
    }

    public static UPath upathFromSVGPath(String str)
    {
        UPath path = new UPath();
        USubPath subPath = new USubPath();
        path.subPaths.add(subPath);

        OB_MutInt midx = new OB_MutInt(skipdelims(str, 0));
        float currx = 0f, curry = 0f, cx1, cy1, cx2, cy2, dx, dy, qx, qy;
        char implicitCommand = 'M', lastCommand = 0, uc = 0;
        PointF lastCurvePoint = new PointF(0f, 0f);
        while (midx.value < str.length())
        {
            lastCommand = uc;
            uc = str.charAt(midx.value);
            if (isCommandChar(uc))
                midx.value++;
            else
                uc = implicitCommand;
            switch (uc)
            {
                case 'M':
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    subPath.moveTo(currx, curry);
                    implicitCommand = 'L';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'm':
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    subPath.moveTo(currx, curry);
                    implicitCommand = 'l';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'L':
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    subPath.lineTo(currx, curry);
                    lastCurvePoint.set(0f, 0f);
                    implicitCommand = 'L';
                    break;
                case 'l':
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    subPath.lineTo(currx, curry);
                    implicitCommand = 'l';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'H':
                    currx = getFloat(str, midx);
                    subPath.lineTo(currx, curry);
                    implicitCommand = 'H';
                    break;
                case 'h':
                    currx += getFloat(str, midx);
                    subPath.lineTo(currx, curry);
                    implicitCommand = 'h';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'V':
                    curry = getFloat(str, midx);
                    subPath.lineTo(currx, curry);
                    implicitCommand = 'V';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'v':
                    curry += getFloat(str, midx);
                    subPath.lineTo(currx, curry);
                    implicitCommand = 'v';
                    lastCurvePoint.set(0f, 0f);
                    break;
                case 'C':
                    cx1 = getFloat(str, midx);
                    cy1 = getFloat(str, midx);
                    cx2 = getFloat(str, midx);
                    cy2 = getFloat(str, midx);
                    lastCurvePoint.set(cx2, cy2);
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'C';
                    break;
                case 'c':
                    cx1 = currx + getFloat(str, midx);
                    cy1 = curry + getFloat(str, midx);
                    cx2 = currx + getFloat(str, midx);
                    cy2 = curry + getFloat(str, midx);
                    lastCurvePoint.set(cx2, cy2);
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'c';
                    break;
                case 'S':
                    if ("CcSs".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[CcSs]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    cx1 = currx + dx;
                    cy1 = curry + dy;
                    cx2 = getFloat(str, midx);
                    cy2 = getFloat(str, midx);
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    lastCurvePoint.set(cx2, cy2);
                    implicitCommand = 'S';
                    break;
                case 's':
                    if ("CcSs".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[CcSs]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    cx1 = currx + dx;
                    cy1 = curry + dy;
                    cx2 = currx + getFloat(str, midx);
                    cy2 = curry + getFloat(str, midx);
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    lastCurvePoint.set(cx2, cy2);
                    implicitCommand = 's';
                    break;
                case 'Q':
                    qx = getFloat(str, midx);
                    qy = getFloat(str, midx);
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'Q';
                    break;
                case 'q':
                    qx = currx + getFloat(str, midx);
                    qy = curry + getFloat(str, midx);
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'q';
                    break;
                case 'T':
                    if ("QqTt".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[QqTt]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    qx = currx + dx;
                    qy = curry + dy;
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx = getFloat(str, midx);
                    curry = getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 'T';
                    break;
                case 't':
                    if ("QqTt".indexOf(lastCommand) < 0)
                        //if (!new String(new int[]{lastCommand},0,1).matches("[QqTt]"))
                        lastCurvePoint.set(0f, 0f);
                    dx = currx - lastCurvePoint.x;
                    dy = curry - lastCurvePoint.y;
                    qx = currx + dx;
                    qy = curry + dy;
                    cx1 = currx + (2.0f / 3.0f * (qx - currx));
                    cy1 = curry + (2.0f / 3.0f * (qy - curry));
                    currx += getFloat(str, midx);
                    curry += getFloat(str, midx);
                    cx2 = currx + (2.0f / 3.0f * (qx - currx));
                    cy2 = curry + (2.0f / 3.0f * (qy - curry));
                    lastCurvePoint.set(qx, qy);
                    subPath.cubicTo(cx1, cy1, cx2, cy2, currx, curry);
                    implicitCommand = 't';
                    break;
                case 'z':
                case 'Z':
                    subPath.close();
                    break;
                default:
                    break;
            }
            midx.value = skipdelims(str, midx.value);
        }
        return path;
    }

    public static OBPath pathWithPath(Path p, List<Map<String, Object>> settingsStack)
    {
        Map<String, Object> settings = settingsStack.get(settingsStack.size() - 1);
        RectF b = new RectF();
        p.computeBounds(b, true);
        if (b.width() == 0 && b.height() == 0)
            return null;
        settings.put("originalbounds", b);
        Matrix t = (Matrix) settings.get("transform");
        if (t != null)
            p.transform(t);
        Class c = classForSettings(settings);
        try
        {
            OBPath path = (OBPath) c.getConstructor(Path.class).newInstance(p);
            path.sizeToBoundingBox();
            return path;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static OBPath pathWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        String pathString = xmlnode.attributeStringValue("d");
        Path p = pathFromSVGPath(pathString);
        return pathWithPath(p, settingsStack);
    }

    public static OBPath polylineWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        String pathString = xmlnode.attributeStringValue("points");
        Path p = pathFromSVGPath(pathString);
        return pathWithPath(p, settingsStack);
    }

    public static OBPath polygonWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        String pathString = xmlnode.attributeStringValue("points");
        Path p = pathFromSVGPath(pathString);
        p.close();
        return pathWithPath(p, settingsStack);
    }

    public static OBPath pathlineWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        float x1 = (float) xmlnode.attributeFloatValue("x1");
        float x2 = (float) xmlnode.attributeFloatValue("x2");
        float y1 = (float) xmlnode.attributeFloatValue("y1");
        float y2 = (float) xmlnode.attributeFloatValue("y2");
        Path p = new Path();
        p.moveTo(x1, y1);
        p.lineTo(x2, y2);
        return pathWithPath(p, settingsStack);
    }

    public static OBPath ellipseWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        float cx = (float) xmlnode.attributeFloatValue("cx");
        float cy = (float) xmlnode.attributeFloatValue("cy");
        float rx = (float) xmlnode.attributeFloatValue("rx");
        float ry = (float) xmlnode.attributeFloatValue("ry");
        Path p = new Path();
        RectF rf = new RectF(cx - rx, cy - ry, cx + rx, cy + ry);
        p.addOval(rf, Path.Direction.CCW);
        return pathWithPath(p, settingsStack);
    }

    public static OBPath circleWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        float cx = (float) xmlnode.attributeFloatValue("cx");
        float cy = (float) xmlnode.attributeFloatValue("cy");
        float rad = (float) xmlnode.attributeFloatValue("r");
        Path p = new Path();
        p.addCircle(cx, cy, rad, Path.Direction.CCW);
        return pathWithPath(p, settingsStack);
    }

    public static OBPath rectWithSVGNode(OBXMLNode xmlnode, List<Map<String, Object>> settingsStack)
    {
        float x = (float) xmlnode.attributeFloatValue("x");
        float y = (float) xmlnode.attributeFloatValue("y");
        float w = (float) xmlnode.attributeFloatValue("width");
        float h = (float) xmlnode.attributeFloatValue("height");
        Path p = new Path();
        p.addRect(x, y, x + w, y + h, Path.Direction.CCW);
        return pathWithPath(p, settingsStack);
    }

    public OBPath()
    {
        super();
        layer = new OBShapeLayer();
        strokeStart = 0.0f;
        strokeEnd = 1.0f;
    }

    public OBPath(Path p)
    {
        this();
        ((OBShapeLayer) layer).path = p;
        if (p != null)
        {
            RectF bb = new RectF();
            p.computeBounds(bb, true);
            frame.set(bb);
            Matrix tr = new Matrix();
            tr.setTranslate(-bb.left, -bb.top);
            p.transform(tr);

            layer.setBounds(0, 0, (frame.right - frame.left), (frame.bottom - frame.top));
            PointF pt = OB_Maths.midPoint(frame);
            setPosition(pt);
        }
    }

    public OBPath(Path p, float w, float h, float posx, float posy)
    {
        layer = new OBShapeLayer(p);
        layer.setBounds(0, 0, (w), (h));
        RectF bb = new RectF(0, 0, w, h);
        frame.set(bb);

        setPosition(posx, posy);
    }

    @Override
    public OBControl copy()
    {
        OBPath obj = (OBPath) super.copy();
        obj.strokeStart = strokeStart;
        obj.strokeEnd = strokeEnd;
        return obj;
    }

    public void setPath(Path p)
    {
        invalidate();
        ((OBShapeLayer) layer).path = p;
        setNeedsRetexture();
//        needsRetexture = true;
        invalidate();
    }


    public OBShapeLayer shapeLayer()
    {
        return (OBShapeLayer) layer;
    }

    public Path path()
    {
        return shapeLayer().path;
    }

    public RectF boundingBox()
    {
        OBShapeLayer obv = shapeLayer();
        Path p = obv.path;
        RectF bb = new RectF();
        p.computeBounds(bb, true);
        //    obv.setBounds((int) bb.left, (int) bb.top, (int) bb.right, (int) bb.bottom);
        frame();
        bb.offset(frame.left, frame.top);
        return bb;
    }

    public void sizeToBox(RectF bb)
    {
        Path bez = path();
        RectF f = new RectF(frame());
        Matrix tr = new Matrix();
        tr.setTranslate(f.left - bb.left, f.top - bb.top);
        bez.transform(tr);
        setFrame(bb);
        //PointF pt = OB_Maths.midPoint(f);
        //setPosition(pt);
    }

    public void sizeToBoundingBox()
    {
        sizeToBox(boundingBox());
    }

    public void sizeToBoundingBoxIncludingStroke()
    {
        RectF bb = boundingBox();
        OBStroke s = ((OBShapeLayer) layer).stroke;
        if (s != null)
        {
            float w = s.lineWidth;
            bb.inset(-w, -w);
        }
        sizeToBox(bb);
    }

    public int fillColor()
    {
        return ((OBShapeLayer) layer).fillColour;
    }

    public void setFillColor(int col)
    {
        shapeLayer().fillColour = col;
//        if (texture != null)
//        {
        setNeedsRetexture();
//        }
        invalidate();
    }

    public int strokeColor()
    {
        return shapeLayer().stroke.colour;
    }

    public void setStrokeColor(int col)
    {
        shapeLayer().stroke.colour = col;
//        if (texture != null)
//        {
        setNeedsRetexture();
//        }
        invalidate();
    }

    public float lineWidth()
    {
        return shapeLayer().stroke.lineWidth;
    }

    public void setLineWidth(float lw)
    {
        shapeLayer().stroke.lineWidth = lw;
        setNeedsRetexture();
        invalidate();
    }

    public void setLineJoin(int lj)
    {
        shapeLayer().stroke.lineJoin = lj;
        invalidate();
    }

    public void setLineCap(int lc)
    {
        shapeLayer().stroke.lineCap = lc;
        invalidate();
    }

    public void setStroke(OBStroke s)
    {
        shapeLayer().stroke = s;
        setNeedsRetexture();
        invalidate();
    }

    public void setLineDashPattern(List<Float> lf)
    {
        shapeLayer().stroke.dashes = lf;
        invalidate();
    }

    public PointF sAlongPath(float s, PointF outVector)
    {
        OBShapeLayer dr = shapeLayer();
        float len = dr.pathMeasure().getLength();
        float pos[] = {0, 0};
        float tan[] = {0, 0};
        dr.pathMeasure().getPosTan(s * len, pos, tan);
        PointF pt = new PointF(pos[0], pos[1]);
        if (outVector != null)
        {
            if (tan[0] == 0 && tan[1] == 0)
            {
                if (s <= 0)
                {
                    float pos2[] = new float[2];
                    dr.pathMeasure().getPosTan(1, pos2, tan);
                    tan[0] = pos2[0] - pos[0];
                    tan[1] = pos2[1] - pos[1];
                    float ltan = (float) Math.sqrt(tan[0] * tan[0] + tan[1] * tan[1]);
                    if (ltan > 0)
                    {
                        tan[0] = tan[0] / ltan;
                        tan[1] = tan[1] / ltan;
                    }
                }
                else if (s >= 1)
                {
                    float pos2[] = new float[2];
                    dr.pathMeasure().getPosTan(len - 1, pos2, tan);
                    tan[0] = pos[0] - pos2[0];
                    tan[1] = pos[1] - pos2[1];
                    float ltan = (float) Math.sqrt(tan[0] * tan[0] + tan[1] * tan[1]);
                    if (ltan > 0)
                    {
                        tan[0] = tan[0] / ltan;
                        tan[1] = tan[1] / ltan;
                    }
                }
            }
            outVector.set(tan[0], tan[1]);
        }
        return pt;
    }

    public float length()
    {
        OBShapeLayer dr = shapeLayer();
        return dr.length();
    }

    public void setStrokeEnd(float f)
    {
        OBShapeLayer dr = shapeLayer();
        dr.setStrokeEnd(f);
        setNeedsRetexture();
        invalidate();
    }

    public void moveToPoint(float x, float y)
    {
        OBShapeLayer dr = shapeLayer();
        dr.path.moveTo(x, y);
        setNeedsRetexture();
        invalidate();
    }

    public void addLineToPoint(float x, float y)
    {
        OBShapeLayer dr = (OBShapeLayer) layer;
        dr.path.lineTo(x, y);
        setNeedsRetexture();
        invalidate();
    }

    public PointF currentPoint()
    {
        OBShapeLayer dr = shapeLayer();
        return new PointF(dr.currX, dr.currY);
    }

    public boolean needsTexture()
    {
        return true;
    }

}
