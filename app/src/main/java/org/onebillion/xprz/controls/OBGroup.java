package org.onebillion.xprz.controls;


import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.*;
import android.opengl.GLES20;

import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.mainui.OBViewController;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.UGradient;
import org.onebillion.xprz.utils.URadialGradient;

public class OBGroup extends OBControl
{
    public List<OBControl> members,sortedAttachedControls;
    public Map<String,OBControl>objectDict;
    boolean sortedAttachedControlsValid;
    float opacity = 1f;

    public OBGroup()
    {
        objectDict = new HashMap<String,OBControl>();
        members = new ArrayList<OBControl>();
        sortedAttachedControls = new ArrayList<OBControl>();
        sortedAttachedControlsValid = false;
    }

    public OBGroup(List<OBControl> _members, RectF f)
    {
        this();
        frame = new RectF(f.left, f.top, f.right, f.bottom);
        for (OBControl c : _members)
        {
            PointF pos = OB_Maths.OffsetPoint(c.position, -f.left, -f.top);
            c.position = pos;
            members.add(c);
            if (c.controller != null)
                ((OBSectionController)c.controller).detachControl(c);
            c.parent = this;
        }
        position = new PointF(f.left + (f.right - f.left) / 2.0f, f.top + (f.bottom - f.top) / 2.0f);
        bounds.set(0,0,f.right - f.left,f.bottom - f.top);
        //
        settings = new HashMap<String, Object>();
        settings.put("attrs", new HashMap<String, String>());

    }

    public OBGroup(List<OBControl> _members) {
        this(_members, OBGroup.frameUnion(_members));
    }

    public static RectF frameUnion(List<OBControl> _members)
    {
        if (_members.size() == 0)
            return new RectF();
        RectF r = new RectF(_members.get(0).frame());
        for (OBControl c : _members)
            r.union(c.frame());
        return r;
    }

    static Object fillFromNodeAttributes(Map<String, String> attrs)
    {
        if ((attrs.get("fill") == null) && (attrs.get("fill-opacity") == null))
            return null;
        int col = 0;
        String str = attrs.get("fill");
        if (str == null)
            col = Color.BLACK;
        else
        {
            if (str.startsWith("url"))
                return str;
            if (str.startsWith("none"))
                return str;
            col = OBUtils.svgColorFromRGBString(str);
        }
        float opacity = 1.0f;
        String n = attrs.get("fill-opacity");
        if (n != null)
            opacity = Float.parseFloat(n);
        if (opacity > 0.0f)
        {
            int intop = Math.round(opacity * 255f);
            col = col | (intop << 24);
        }
        return new Integer(col);
    }

    public static void GetObjectIdsFromArray(List<OBControl> arr,Map<String,OBControl> dict)
    {
        for (OBControl c : arr)
        {
            String str = (String)c.propertyValue("name");
            if (str != null && !str.equals(""))
                dict.put(str,c);
            if (OBGroup.class.isInstance(c))
            {
                OBGroup g = (OBGroup)c;
                GetObjectIdsFromArray(g.members, dict);
            }
        }
    }

    static int svgFunction(String str, List<Object> list, int index)
    {
        while ((index) < str.length() && str.substring(index, index + 1).matches("\\s"))
            index++;
        int startidx = index;
        while ((index) < str.length() && !str.substring(index, index + 1).matches("\\s|\\("))
            index++;
        if (startidx == index)
            return index;
        String cmd = str.substring(startidx, index);
        list.add(cmd);
        while ((index) < str.length() && !str.substring(index, index + 1).matches("\\("))
            index++;
        index++;
        if (index >= str.length())
            return index;
        startidx = index;
        while ((index) < str.length() && !str.substring(index, index + 1).matches("\\)"))
            index++;
        if (index >= str.length())
            return index;
        String paramString = str.substring(startidx, index);
        index++;
        String params[] = paramString.split("\\s|,");
        List<String> prams = new ArrayList<String>();
        for (int i = 0; i < params.length; i++)
            prams.add(params[i]);
        list.add(prams);
        return index;
    }

    static void setAttributesFromStylesForNode(OBXMLNode child,Map<String, Object> settings)
    {
        if (child.attributes.get("style") == null)
            return;
        Map<String, String> mdict = null;
        String svgstyles[] = child.attributes.get("style").split(";");
        for (String style : svgstyles)
        {
            if (style != null && style.length() > 2)
            {
                String  arr[] = style.split(":");
                if (arr.length > 1)
                {
                    String sty = arr[0];
                    String val = arr[1];
                    sty = sty.trim();
                    val = val.trim();
                    if (sty.length() > 0)
                    {
                        if (mdict == null)
                        {
                            mdict = new HashMap<>();
                            mdict.putAll(child.attributes);
                        }
                        mdict.put(sty,val);
                    }
                }
            }
        }
        if (mdict != null)
            child.attributes = mdict;
    }

    static Matrix transformFromString(String transformString)
    {
        Matrix transform;
        transform = new Matrix();
        int index = 0;
        while (index < transformString.length())
        {
            List<Object> arr = new ArrayList<>();
            index = svgFunction(transformString,arr,index);
            if (arr.size() < 2)
                break;
            String ttype = (String)arr.get(0);
            List<String> params = (List<String>)arr.get(1);
            if (ttype.equals("translate"))
            {
                float dx = 0,dy = 0;
                if (params.size() > 0)
                    dx = Float.parseFloat(params.get(0));
                if (params.size() > 1)
                    dy = Float.parseFloat(params.get(1));
                transform.preTranslate(dx, dy);
            }
            else if (ttype.equals("scale"))
            {
                float sx = 1;
                if (params.size() > 0)
                    sx = Float.parseFloat(params.get(0));
                float sy = sx;
                if (params.size() > 1)
                    sy = Float.parseFloat(params.get(1));
                transform.preScale(sx, sy);
            }
            else if (ttype.equals("rotate"))
            {
                float ang = 0;
                if (params.size() > 0)
                    ang = Float.parseFloat(params.get(0));
                float cx = 0,cy = 0;
                if (params.size() > 1)
                    cx = Float.parseFloat(params.get(1));
                if (params.size() > 2)
                    cy = Float.parseFloat(params.get(2));
                if (cx != 0.0 || cy != 0.0)
                    transform.postTranslate(cx, cy);
                transform.postRotate(ang);
                if (cx != 0.0 || cy != 0.0)
                    transform.preTranslate(-cx, -cy);
            }
            else if (ttype.equals("matrix"))
            {
                float of[] = {0,0,0,0,0,0,0,0,1};
                float inf[] = {0,0,0,0,0,0,0,0,0};
                for (int i = 0;i < params.size();i++)
                    inf[i] = Float.parseFloat(params.get(i));
                of[0] = inf[0];
                of[1] = inf[2];
                of[2] = inf[4];
                of[3] = inf[1];
                of[4] = inf[3];
                of[5] = inf[5];
                transform.setValues(of);
            }
        }
        return transform;
    }

    static void attributesFromSVGNode(OBXMLNode child, Map<String, Object> settings)
    {
        setAttributesFromStylesForNode(child,settings);
        OBStroke stroke = new OBStroke(child.attributes);
        if (stroke != null)
            settings.put("stroke", stroke);
        Object of = fillFromNodeAttributes(child.attributes);
        if (of != null)
        {
            if (of.equals("none"))
                settings.remove("fill");
            else //if (of instanceof Integer)
                settings.put("fill", of);
        }
        String transformString = child.attributes.get("transform");
        if (transformString != null)
        {
            int index = 0;
            Matrix otransform = (Matrix) settings.get("transform");
            Matrix transform = new Matrix(otransform);
            Matrix newtransform = transformFromString(transformString);
            transform.preConcat(newtransform);
            settings.put("transform", transform);
        }
        String v = child.attributes.get("visibility");
        if (v != null)
        {
            if (v.equals("hidden"))
                settings.put("hidden",true);
            else if (v.equals("inherit"))
                settings.remove("hidden");
        }
        v = child.attributes.get("display");
        if (v != null)
        {
            if (v.equals("none"))
                settings.put("hidden",true);
        }
        String o = child.attributes.get("opacity");
        if (o != null)
        {
            float alpha = Float.parseFloat(o);
            settings.put("opacity",alpha);
        }
    }

    static Map<String, Object> cloneSettings(Map<String, Object> settings)
    {
        Map<String, Object> newSettings = new HashMap<String, Object>();
        newSettings.putAll(settings);
        newSettings.remove("hidden");
        newSettings.remove("opacity");
        return newSettings;
    }

    public static float floatOrPercentage(String str)
    {
        str = str.trim();
        if (str.length() == 0)
            return 0;
        boolean ispc = false;
        if (str.substring(str.length()-1).equals("%"))
        {
            ispc = true;
            str = str.substring(0,str.length());
        }
        float f = Float.parseFloat(str);
        if (ispc)
            f = f / 100;
        return f;
    }

    static UGradient gradientFromSVGNode(OBXMLNode child,List<Map<String, Object>>settingsStack)
    {
        setAttributesFromStylesForNode(child,null);
        Map<String,String> attrs = child.attributes;
        UGradient grad = new UGradient();
        String s = attrs.get("gradientUnits");
        if (s != null)
        {
            if (s.toLowerCase().equals("userspaceonuse"))
                grad.useBboxUnits = false;
        }
        s = attrs.get("spreadMethod");
        if (s != null)
            grad.spreadMethod = s;
        if ((s = attrs.get("x1")) != null)
            grad.x1 = floatOrPercentage(s);
        if ((s = attrs.get("x2")) != null)
            grad.x2 = floatOrPercentage(s);
        if ((s = attrs.get("y1")) != null)
            grad.y1 = floatOrPercentage(s);
        if ((s = attrs.get("y2")) != null)
            grad.y2 = floatOrPercentage(s);
        if ((s = attrs.get("gradientTransform")) != null)
            grad.transform = transformFromString(s);
        int col = Color.BLACK;
        List<List<Object>> elements = new ArrayList<>();
        for (OBXMLNode stopnode : child.childrenOfType("stop"))
        {
            setAttributesFromStylesForNode(stopnode,null);
            s = stopnode.attributes.get("offset");
            float stopf = floatOrPercentage(s);
            s = stopnode.attributes.get("stop-color");
            if (s != null)
                col = OBUtils.svgColorFromRGBString(s);
            float alpha = 1;
            s = stopnode.attributes.get("stop-opacity");
            if (s != null)
            {
                alpha = Float.parseFloat(s);
                if (alpha > 0.0f)
                {
                    int intop = Math.round(alpha * 255f);
                    col = col | (intop << 24);
                }
            }
            List<Object> ol = new ArrayList<>();
            ol.add(col);
            ol.add(stopf);
            elements.add(ol);
        }
        grad.stops = elements;
        return grad;
    }

    static URadialGradient radialGradientFromSVGNode(OBXMLNode child,List<Map<String, Object>>settingsStack)
    {
        setAttributesFromStylesForNode(child,null);
        Map<String,String> attrs = child.attributes;
        URadialGradient grad = new URadialGradient();
        String s = attrs.get("gradientUnits");
        if (s != null)
        {
            if (s.toLowerCase().equals("userspaceonuse"))
                grad.useBboxUnits = false;
        }
        s = attrs.get("spreadMethod");
        if (s != null)
            grad.spreadMethod = s;
        if ((s = attrs.get("cx")) != null)
            grad.cx = floatOrPercentage(s);
        if ((s = attrs.get("fx")) != null)
            grad.fx = floatOrPercentage(s);
        if ((s = attrs.get("cy")) != null)
            grad.cy = floatOrPercentage(s);
        if ((s = attrs.get("fy")) != null)
            grad.fy = floatOrPercentage(s);
        if ((s = attrs.get("r")) != null)
            grad.r = floatOrPercentage(s);
        if ((s = attrs.get("gradientTransform")) != null)
            grad.transform = transformFromString(s);
        int col = Color.BLACK;
        List<List<Object>> elements = new ArrayList<>();
        for (OBXMLNode stopnode : child.childrenOfType("stop"))
        {
            setAttributesFromStylesForNode(stopnode,null);
            s = stopnode.attributes.get("offset");
            float stopf = floatOrPercentage(s);
            s = stopnode.attributes.get("stop-color");
            if (s != null)
                col = OBUtils.svgColorFromRGBString(s);
            float alpha = 1;
            s = stopnode.attributes.get("stop-opacity");
            if (s != null)
            {
                alpha = Float.parseFloat(s);
                if (alpha > 0.0f)
                {
                    int intop = Math.round(alpha * 255f);
                    col = col | (intop << 24);
                }
            }
            List<Object> ol = new ArrayList<>();
            ol.add(col);
            ol.add(stopf);
            elements.add(ol);
        }
        grad.stops = elements;
        return grad;
    }

    static RectF rectFromString(String s)
    {
        String ra[] = s.split("[, ]", 0);
        if (ra.length < 4)
            return null;
        float x = Float.parseFloat(ra[0]);
        float y = Float.parseFloat(ra[1]);
        float w = Float.parseFloat(ra[2]);
        float h = Float.parseFloat(ra[3]);
        return new RectF(x,y,x+w,y+h);
    }

    static int map_align(char c)
    {
        if (c == 'x')
            return OBPattern.PAR_ALIGN_MAX;
        if (c == 'n')
            return OBPattern.PAR_ALIGN_MIN;
        return OBPattern.PAR_ALIGN_MID;
    }

    static OBPattern patternFromSVGNode(OBXMLNode child,List<Map<String, Object>>settingsStack)
    {
        setAttributesFromStylesForNode(child, null);
        Map<String, Object> defs = (Map<String, Object>)settingsStack.get(settingsStack.size()-1).get("defs");
        Map<String, String> attrs = child.attributes;
        OBGroup gp = groupFromSVGXML(child);
        OBPattern pat = null;
        String xlink = child.attributeStringValue("xlink:href");
        if (xlink != null)
        {
            xlink = xlink.substring(1);
            OBPattern xp = (OBPattern)defs.get(xlink);
            pat = xp.copy();
        }
        else
            pat = new OBPattern();
        if (gp != null && gp.members.size() > 0)
            pat.patternContents = gp;
        String s;
        if ((s = attrs.get("x")) != null)
            pat.x = floatOrPercentage(s);
        if ((s = attrs.get("y")) != null)
            pat.y = floatOrPercentage(s);
        if ((s = attrs.get("width")) != null)
            pat.patternWidth = floatOrPercentage(s);
        if ((s = attrs.get("height")) != null)
            pat.patternHeight = floatOrPercentage(s);
        if ((s = attrs.get("patternTransform")) != null)
            pat.transform = transformFromString(s);
        if ((s = attrs.get("patternUnits")) != null)
            pat.useBboxUnitsForPatternUnits = !(s.toLowerCase().equals("userspaceonuse"));
        if ((s = attrs.get("patternContentUnits")) != null)
            pat.useBboxUnitsForPatternContentUnits = !(s.toLowerCase().equals("userspaceonuse"));
        if ((s = attrs.get("viewBox")) != null)
            pat.viewBox = rectFromString(s);
        if ((s = attrs.get("preserveAspectRatio")) != null)
        {
            String pars[] = s.split("[ ]*",0);
            if (pars.length > 0)
            {
                String align = pars[0];
                if (align.equals("none"))
                    pat.preserveAspectRatio = OBPattern.PAR_NONE;
                else
                {
                    if (align.length() == 8)
                    {
                        char xc = align.charAt(3);
                        char yc = align.charAt(7);
                        pat.xAlign = map_align(xc);
                        pat.yAlign = map_align(yc);
                        pat.par_slice = (pars[1].equals("slice"));
                    }
                }
            }

        }
        return pat;
    }

    static Object objectFromSVGNode(OBXMLNode child, List<Map<String, Object>> settingsStack)
    {
        Map<String, Object> currentSettings =  cloneSettings(settingsStack.get(settingsStack.size() - 1));
        settingsStack.add(currentSettings);
        attributesFromSVGNode(child, currentSettings);
        OBControl g = null;
        if (child.nodeName.equals("path"))
            g = OBPath.pathWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("polyline"))
            g = OBPath.polylineWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("polygon"))
            g = OBPath.polygonWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("line"))
            g = OBPath.pathlineWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("circle"))
            g = OBPath.circleWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("rect"))
            g = OBPath.rectWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("ellipse"))
            g = OBPath.ellipseWithSVGNode(child, settingsStack);
        else if (child.nodeName.equals("g"))
        {
            settingsStack.add(currentSettings);
            List<OBControl> subobjects = new ArrayList<OBControl>();
            for (OBXMLNode ch : child.children)
                processSVGNode(ch, settingsStack, subobjects);
            g = new OBGroup(subobjects);
            settingsStack.remove(settingsStack.size() - 1);
        }
        else if (child.nodeName.equals("defs"))
            processDefs(child,settingsStack);
        else if (child.nodeName.equals("linearGradient"))
            return gradientFromSVGNode(child,settingsStack);
        else if (child.nodeName.equals("radialGradient"))
            return radialGradientFromSVGNode(child,settingsStack);
        else if (child.nodeName.equals("pattern"))
            return patternFromSVGNode(child,settingsStack);
        if (g != null)
        {
            String idstr = child.attributeStringValue("id");
            if (idstr == null)
                idstr = "";
            g.setProperty("name", idstr);
            if (g instanceof OBPath)
            {
                OBPath p = (OBPath) g;
                OBStroke stroke = (OBStroke) currentSettings.get("stroke");
                if (stroke != null)
                {
                    p.shapeLayer().stroke = stroke;
                }
                Object of = currentSettings.get("fill");

                if (of != null)
                {
                    if (of instanceof String)
                    {
                        String s = (String)of;
                        if (s.startsWith("url("))
                        {
                            p.setFillColor(0);
                            String url = s.substring(5,s.length()-1);
                            Object obj = null;
                            Map<String,Object>defs = (Map<String, Object>) currentSettings.get("defs");
                            if (defs != null)
                                obj = defs.get(url);
                            if (obj != null)
                            {
                                if (obj instanceof URadialGradient)
                                {
                                    ((OBRadialGradientPath)g).takeValuesFrom((URadialGradient)obj,settingsStack);
                                }
                                else if (obj instanceof UGradient)
                                {
                                    ((OBGradientPath)g).takeValuesFrom((UGradient)obj,settingsStack);
                                }
                                else if (obj instanceof OBPattern)
                                {
                                    ((OBPatternPath)g).takeValuesFrom((OBPattern)obj,settingsStack);
                                }
                            }
                        }
                    }
                    else if (of instanceof Integer)
                    {
                        int col = (Integer)of;
                        if (idstr.startsWith("skin"))
                            col = OBUtils.SkinColour(OBUtils.SkinColourIndex());
                        else if (idstr.startsWith("cloth"))
                            col = ((Integer) MainActivity.mainActivity.configIntForKey(MainActivity.CONFIG_CLOTHCOLOUR)).intValue();
                        ((OBShapeLayer) (p.layer)).fillColour = col;
                    }
                }
            }
            Object o  = currentSettings.get("opacity");
            if (o != null)
                g.setOpacity((Float)o);
            o  = currentSettings.get("hidden");
            if (o != null && ((Boolean)o))
                g.hide();
            if (idstr.length() > 0)
            {
                Map<String, OBControl> odict = (Map<String, OBControl>)currentSettings.get("objectdict");
                odict.put(idstr,g);
            }
        }
        settingsStack.remove(settingsStack.size()-1);
        return g;
    }

    static void processDefs(OBXMLNode def, List<Map<String, Object>> settingsStack)
    {
        Map<String, Object> defs = (Map<String, Object>)settingsStack.get(settingsStack.size()-1).get("defs");
        for (OBXMLNode child : def.children)
        {
            Object obj = objectFromSVGNode(child,settingsStack);
            if (obj != null)
            {
                String idstr = child.attributeStringValue("id");
                if (idstr == null)
                    idstr = "";
                defs.put(idstr,obj);
            }
        }
    }

    static void processSVGNode(OBXMLNode child, List<Map<String, Object>> settingsStack, List<OBControl> objects)
    {
        Object g = objectFromSVGNode(child, settingsStack);
        if (g != null)
        {
            if (g instanceof OBControl)
                objects.add((OBControl)g);
            else
            {
                String nm = child.attributeStringValue("id");
                if (nm != null)
                {
                    Map<String, Object> defs = (Map<String, Object>)settingsStack.get(settingsStack.size()-1).get("defs");
                    defs.put(nm,g);
                }
            }
        }
    }

    public static OBGroup groupFromSVGXML(OBXMLNode root)
    {
        float w = (float) root.attributeFloatPrefixValue("width");
        float h = (float) root.attributeFloatPrefixValue("height");

        RectF f = new RectF(0f, 0f, w, h);
        List<Map<String, Object>> settingsStack = new ArrayList<Map<String, Object>>();
        Map<String, Object> svgSettings = new HashMap<String, Object>();
        svgSettings.put("fill", Integer.valueOf(Color.BLACK));
        svgSettings.put("defs", new HashMap<String, Object>());
        svgSettings.put("objectdict", new HashMap<String, OBControl>());
        Matrix t = new Matrix();
        svgSettings.put("transform", t);
        settingsStack.add(svgSettings);
        List<OBControl> objects = new ArrayList<OBControl>();
        for (OBXMLNode child : root.children)
            processSVGNode(child, settingsStack, objects);
        OBGroup g;
        if (f.width() > 0 && f.height() > 0)
            g = new OBGroup(objects, f);
        else
            g = new OBGroup(objects);
        g.objectDict = (Map<String, OBControl>)svgSettings.get("objectdict");
        return g;
    }

    public static OBGroup groupFromSVG(InputStream fileStream) {
        OBXMLManager xmlman = new OBXMLManager();
        OBXMLNode root;
        try {
            root = (xmlman.parseFile(fileStream)).get(0);
        } catch (Exception e) {
            return null;
        }
        if (!root.nodeName.equals("svg"))
            return null;
        return groupFromSVGXML(root);
    }

    public RectF bounds()
    {
        return bounds;
    }

    @Override
    public OBControl copy()
    {
        OBGroup obj = (OBGroup)super.copy();
        obj.objectDict = new HashMap<String,OBControl>();
        obj.members = new ArrayList<OBControl>();
        for (OBControl c : members)
        {
            OBControl cx = c.copy();
            obj.members.add(cx);
            cx.parent = obj;
        }
        sortedAttachedControls = new ArrayList<OBControl>();
        sortedAttachedControlsValid = false;
            return obj;
    }

    public PointF position() {
        return position;
    }

    public void setFrame(RectF f) {
        frame = f;
    }

    void populateSortedAttachedControls()
    {
        if (!sortedAttachedControlsValid)
        {
            sortedAttachedControls.clear();
            sortedAttachedControls.addAll(members);
            for (int i = 0;i < sortedAttachedControls.size();i++)
                sortedAttachedControls.get(i).tempSortInt = i;
            Collections.sort(sortedAttachedControls, new Comparator<OBControl>()
            {
                @Override
                public int compare(OBControl lhs, OBControl rhs)
                {
                    if (lhs.zPosition < rhs.zPosition)
                        return -1;
                    if (lhs.zPosition > rhs.zPosition)
                        return 1;
                    if (lhs.tempSortInt < rhs.tempSortInt)
                        return -1;
                    if (lhs.tempSortInt > rhs.tempSortInt)
                        return 1;
                    return 0;
                }
            });
            sortedAttachedControlsValid = true;
        }
    }

    public void render(OBRenderer renderer,OBViewController vc,float[] modelViewMatrix)
    {
        if (shouldTexturise)
        {
            super.render(renderer,vc,modelViewMatrix);
            return;
        }
        matrix3dForDraw();
        if (doubleSided)
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        else
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        android.opengl.Matrix.multiplyMM(tempMatrix,0,modelViewMatrix,0,modelMatrix,0);
        populateSortedAttachedControls();
        for (OBControl c : sortedAttachedControls)
            c.render(renderer,vc,tempMatrix);
    }

    public void drawLayer(Canvas canvas)
    {
        populateSortedAttachedControls();
        boolean needsRestore = false;
        if (needsRestore = (opacity() != 1.0f))
            canvas.saveLayerAlpha(bounds(), (int) (opacity() * 255));
        for (OBControl c : sortedAttachedControls)
            c.draw(canvas);
        if (needsRestore)
            canvas.restore();
    }

    public void highlight()
    {
        enCache();
        super.highlight();
    }

    public void buildObjectDict()
    {
        GetObjectIdsFromArray(members, objectDict);
    }

    public OBGroup primogenitor()
    {
        OBGroup dad = this;
        while (dad.parent != null)
        {
            dad = (OBGroup)dad.parent;
        }
        return dad;
    }

    public void sizeToMember(OBControl m)
    {
        PointF oldmpos = convertPointFromControl(m.position(),m.parent);
        RectF mframe = convertRectFromControl(m.bounds(),m);
        float dx = mframe.left;
        float dy = mframe.top;
        for (OBControl c : members)
            c.setPosition(OB_Maths.OffsetPoint(c.position(), -dx, -dy));
        setBounds(0,0,mframe.width(),mframe.height());
        PointF newmpos = convertPointFromControl(m.position(),m.parent);
        dx = newmpos.x - oldmpos.x;
        dy = newmpos.y - oldmpos.y;
        setPosition(position().x - dx,position().y - dy);
    }

    public void insertMember(OBControl c,int idx,String nm)
    {
        if (idx < 0 || idx > members.size())
            idx = members.size();
        PointF pt = c.position;
        if (c.parent != null)
        {
            pt = convertPointFromControl(pt, c.parent);
            c.position = pt;
        }
        else if(c.controller != null)
        {
            pt = ((OBSectionController)controller).convertPointToControl(pt,this);
            ((OBSectionController)c.controller).detachControl(c);
            c.position = pt;
        }
        members.add(idx,c);
        c.parent = this;
        objectDict.put(nm,c);
        primogenitor().objectDict.put(nm,c);
        sortedAttachedControlsValid = false;
    }

    public void removeMemberAtIndex(final int idx)
    {
        new OBRunnableSyncUI()
        {
            public void ex()
            {
                RectF r = frame();
                OBControl c = members.get(idx);
                members.remove(idx);
                c.parent = null;
                c.setPosition(OB_Maths.OffsetPoint(c.position(), r.left, r.top));
                invalidate();
                sortedAttachedControlsValid = false;
            }
        }.run();
    }

    public void removeMember(OBControl c)
    {
        int idx = members.indexOf(c);
        if (idx >= 0)
            removeMemberAtIndex(idx);
    }

    public List<String> filterMemberIDs(String pattern,boolean sorted)
    {
        List<String> arr = new ArrayList<String>();
        Pattern p = Pattern.compile(pattern);
        for (String k : objectDict.keySet())
        {
            Matcher matcher = p.matcher(k);
            matcher.find();
            if (matcher.matches())
                arr.add(k);
        }
        if (sorted)
        {
            Collections.sort(arr, new Comparator<String>() {
                @Override
                public int compare(String lhs, String rhs) {
                    return OBUtils.caseInsensitiveCompareWithNumbers(lhs,rhs);
                }
            });
        }
        return arr;
    }

    public List<OBControl> filterMembers(String pattern,boolean sorted)
    {
        List<OBControl> arr = new ArrayList<OBControl>();
        for (String name : filterMemberIDs(pattern, sorted))
            arr.add(objectDict.get(name));
        return arr;
    }

    public List<OBControl> filterMembers(String pattern)
    {
        List<OBControl> arr = new ArrayList<OBControl>();
        for (String name : filterMemberIDs(pattern,false))
            arr.add(objectDict.get(name));
        return arr;
    }

    public void hideMembers(String pattern)
    {
        for (OBControl c : filterMembers(pattern))
            c.hide();
    }

    public void showMembers(String pattern)
    {
        for (OBControl c : filterMembers(pattern))
            c.show();
    }

    public void substituteFillForAllMembers(String pattern,int fill)
    {
        for (OBControl c : filterMembers(pattern))
        {
            if (OBPath.class.isInstance(c))
                ((OBPath)c).setFillColor(fill);
        }
        if (texture != null)
            needsRetexture = true;
    }

    public boolean needsTexture()
    {
        return true;
    }
    float maxMemberZPosition()
    {
        if (members.size() == 0)
            return 0;
        float maxzp = members.get(0).zPosition;
        for (OBControl c : members)
            if (c.zPosition > maxzp)
                maxzp = c.zPosition;
        return maxzp;
    }

    public float opacity()
    {
        return opacity;
    }

    @Override
    public void setOpacity(float opacity)
    {
        this.opacity = opacity;
        invalidate();
    }
}