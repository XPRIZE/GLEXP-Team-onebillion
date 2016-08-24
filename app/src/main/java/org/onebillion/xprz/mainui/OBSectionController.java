package org.onebillion.xprz.mainui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Layout;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.glstuff.*;
import org.onebillion.xprz.utils.*;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

public class OBSectionController extends OBViewController
{
    public static final int STATUS_NIL = 0,
            STATUS_AWAITING_CLICK = 1,
            STATUS_AWAITING_CLICK2 = 2,
            STATUS_DRAGGING = 3,
            STATUS_CHECKING = 4,
            STATUS_DOING_DEMO = 5,
            STATUS_WAITING_FOR_TRACE = 6,
            STATUS_TRACING = 7,
            STATUS_WAITING_FOR_POT_CLICK = 8,
            STATUS_WAITING_FOR_OBJ_CLICK = 9,
            STATUS_WAITING_FOR_OBJ_COLOUR_CLICK = 10,
            STATUS_WAITING_FOR_BUTTON_CLICK = 11,
            STATUS_WAITING_FOR_RESUME = 12,
            STATUS_WAITING_FOR_DRAG = 13,
            STATUS_AWAITING_ARROW_CLICK = 14,
            STATUS_EXITING = 15,
            STATUS_SHOWING_POP_UP = 16,
            STATUS_EDITING = 17,
            STATUS_IDLE = 18,
            STATUS_BUSY = 19,
            STATUS_WAITING_FOR_ANSWER = 20;
    public static final int POINTER_ZPOS = 1000;
    public static final int POINTER_BOTLEFT = 0,
            POINTER_BOTRIGHT = 1,
            POINTER_LEFT = 2,
            POINTER_MIDDLE = 3,
            POINTER_RIGHT = 4;
    public static int PROCESS_DONE = 1,
        PROCESS_NOT_DONE = 2;
    public List<OBControl> objects, nonobjects, attachedControls, buttons, sortedAttachedControls;
    public List<String> events;
    public Map<String, Object> audioScenes, eventsDict;
    public Map<String, OBControl> miscObjects, objectDict;
    public Map<String, String> eventAttributes, parameters;
    public int targetNo, currNo;
    public Object params;
    public OBControl target;
    public boolean _aborting, sortedAttachedControlsValid, initialised;
    public OBControl thePointer, tick;
    protected int eventIndex, replayAudioIndex, theStatus, theMoveSpeed;
    protected List<Object> _replayAudio;
    protected long audioQueueToken, sequenceToken, statusTime;
    protected Lock sequenceLock;

    float topColour[] = {1, 1, 1, 1};
    float bottomColour[] = {1, 1, 1, 1};
    List<Integer> busyStatuses =Arrays.asList(STATUS_BUSY,STATUS_DOING_DEMO,STATUS_DRAGGING,STATUS_CHECKING);

    public OBSectionController (Activity a)
    {
        this(a, true);
    }


    public OBSectionController (Activity a, Boolean requiresOpenGL)
    {
        super(a);
        objects = new ArrayList<OBControl>();
        buttons = new ArrayList<OBControl>();
        nonobjects = new ArrayList<OBControl>();
        attachedControls = new ArrayList<OBControl>();
        sortedAttachedControls = Collections.synchronizedList(new ArrayList<OBControl>());
        events = new ArrayList<String>();
        eventAttributes = new HashMap<String, String>();
        objectDict = new HashMap<String, OBControl>();
        miscObjects = new HashMap<String, OBControl>();
        _aborting = false;
        sequenceLock = new ReentrantLock();
        sortedAttachedControlsValid = true;
        this.requiresOpenGL = requiresOpenGL;
    }


    public static Map<String, Object> dictForObject (OBXMLNode e)
    {
        Map<String, Object> objectDict = new HashMap<String, Object>();
        Map<String, String> attrs = e.attributes;
        if (attrs != null)
        {
            objectDict.put("attrs", attrs);
            String objid = attrs.get("id");
            if (objid != null)
                objectDict.put("id", objid);
        }
        List<OBXMLNode> chs = e.children;
        if (chs.size() > 0)
        {
            List<Map<String, Object>> children = new ArrayList<Map<String, Object>>();
            for (OBXMLNode ch : chs)
                children.add(dictForObject(ch));
            objectDict.put("children", children);
        }
        objectDict.put("nodetype", e.nodeName);
        if (e.contents != null)
            objectDict.put("contents", e.contents);
        return objectDict;
    }


    static Map<String, Object> Config ()
    {
        return MainActivity.mainActivity.Config();
    }

    static float floatOrZero (Map<String, Object> attrs, String s)
    {
        if (attrs.get(s) != null)
            return Float.parseFloat((String) attrs.get(s));
        return 0;
    }

    static List<List<Object>> gradientStopsFromArray (List<Map<String, Object>> children)
    {
        int col = Color.BLACK;
        List<List<Object>> elements = new ArrayList<>();
        if (children != null)
        {
            for (Map<String, Object> stopdict : children)
            {
                Map<String, String> stopattrs = (Map<String, String>) stopdict.get("attrs");
                String s = stopattrs.get("offset");
                float stopf = OBUtils.floatOrPercentage(s);
                s = stopattrs.get("stop-color");
                if (s != null)
                {
                    col = OBUtils.svgColorFromRGBString(s);
//                    col = OBUtils.colorFromRGBString(s);
                }
                float alpha = 1;
                s = stopattrs.get("stop-opacity");
                if (s != null)
                {
                    alpha = Float.parseFloat(s);
                    col = Color.argb((int) (alpha * 255), Color.red(col), Color.green(col), Color.blue(col));
                }
                List<Object> tmpl = new ArrayList<>();
                tmpl.add(col);
                tmpl.add(stopf);
                elements.add(tmpl);
            }
        }
        return elements;
    }

    public static UGradient gradientFromAttributes (Map<String, Object> attrs)
    {
        UGradient grad = new UGradient();
        String s;
        if ((s = (String) attrs.get("x1")) != null)
            grad.x1 = OBUtils.floatOrPercentage(s);
        if ((s = (String) attrs.get("x2")) != null)
            grad.x2 = OBUtils.floatOrPercentage(s);
        if ((s = (String) attrs.get("y1")) != null)
            grad.y1 = OBUtils.floatOrPercentage(s);
        if ((s = (String)
                attrs.get("y2")) != null)
            grad.y2 = OBUtils.floatOrPercentage(s);
        grad.stops = gradientStopsFromArray((List<Map<String, Object>>) (attrs.get("children")));
        return grad;
    }

    public static URadialGradient radialGradientFromAttributes (Map<String, Object> attrs)
    {
        URadialGradient grad = new URadialGradient();
        String s;
        if ((s = (String) attrs.get("cx")) != null)
            grad.cx = OBUtils.floatOrPercentage(s);
        if ((s = (String) attrs.get("fx")) != null)
            grad.fx = OBUtils.floatOrPercentage(s);
        if ((s = (String) attrs.get("cy")) != null)
            grad.cy = OBUtils.floatOrPercentage(s);
        if ((s = (String) attrs.get("fy")) != null)
            grad.fy = OBUtils.floatOrPercentage(s);
        if ((s = (String) attrs.get("r")) != null)
            grad.r = OBUtils.floatOrPercentage(s);
        grad.stops = gradientStopsFromArray((List<Map<String, Object>>) (attrs.get("children")));
        return grad;
    }

    public static OBControl objectWithMaxZpos (List<OBControl> arr)
    {
        OBControl maxobj = null;
        float maxzpos = -100;
        for (OBControl c : arr)
            if (c.zPosition() > maxzpos)
            {
                maxobj = c;
                maxzpos = c.zPosition();
            }
        return maxobj;
    }

    public static String getLocalPath (String fileName)
    {
        for (String path : (List<String>) Config().get(MainActivity.CONFIG_AUDIO_SEARCH_PATH))
        {
            String fullPath = OBUtils.stringByAppendingPathComponent(path, fileName);
            if (OBUtils.fileExistsAtPath(fullPath)) return fullPath;
//            if (OBUtils.fileExistsAtPath(fullPath))
//                return fullPath;
        }
        return null;
    }

    public static float baselineOffsetForText(String tx, Typeface ty, float textsize)
    {
        TextPaint tp = new TextPaint();
        tp.setTextSize(textsize);
        tp.setTypeface(ty);
        tp.setColor(Color.BLACK);
        SpannableString ss = new SpannableString(tx);
        StaticLayout sl = new StaticLayout(ss,tp,4000, Layout.Alignment.ALIGN_NORMAL,1,0,false);
        return sl.getLineBaseline(0);
    }

    public Map<String, Object> loadXML (String xmlPath)
    {
        Map<String, Object> eventsDict = new HashMap<>();
        OBXMLNode xmlNode = null;
        try
        {
            if (xmlPath != null)
            {
                Map<String, Object> mstr = null;
                OBXMLManager xmlManager = new OBXMLManager();
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
//                List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(xmlPath));
                xmlNode = xl.get(0);
                List<OBXMLNode> xmlevents = xmlNode.childrenOfType("event");
                for (OBXMLNode xmlevent : xmlevents)
                {
                    String key = xmlevent.attributeStringValue("id");
                    if (key == null)
                        key = "";
                    Map<String, Object> eventDict = new HashMap<>();
                    eventDict.put("attrs", xmlevent.attributes);
                    List<Object> objs = new ArrayList<>();
                    Map<String, Object> objectsDict = new HashMap<>();
                    for (OBXMLNode e : xmlevent.children)
                    {
                        Map<String, Object> objectDict = dictForObject(e);
                        String objid = (String) objectDict.get("id");
                        if (objid != null)
                            objectsDict.put(objid, objectDict);
                        objs.add(objectDict);
                    }
                    eventDict.put("objects", objs);
                    eventDict.put("objectsdict", objectsDict);
                    eventsDict.put(key, eventDict);
                    if (mstr == null)
                        mstr = eventDict;
                }
                if (eventsDict.get("master") == null)
                    eventsDict.put("master", mstr);
            }
        }
        catch (Exception e)
        {

        }
        return eventsDict;
    }

    public void loadAudioXML (String xmlPath)
    {
        try
        {
            audioScenes = OBAudioManager.loadAudioXML(OBUtils.getInputStreamForPath(xmlPath));
//            audioScenes = OBAudioManager.loadAudioXML(MainActivity.mainActivity.getAssets().open(xmlPath));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void viewWillAppear (Boolean animated)
    {
        _aborting = false;
    }

    public String getConfigPath (String cfgName)
    {
        for (String path : (List<String>) Config().get(MainActivity.CONFIG_CONFIG_SEARCH_PATH))
        {
            String fullPath = OBUtils.stringByAppendingPathComponent(path, cfgName);
            Boolean fileExists = OBUtils.fileExistsAtPath(fullPath);
            if (fileExists)
            {
                return fullPath;
            }
//            if (OBUtils.fileExistsAtPath(fullPath))
//                return fullPath;
        }
        return null;
    }

    public void prepare ()
    {
        super.prepare();
        theMoveSpeed = bounds().width();
        inited = true;
        processParams();
        eventsDict = loadXML(getConfigPath(sectionName() + ".xml"));
        loadAudioXML(getConfigPath(sectionAudioName() + "audio.xml"));
    }

    public void start ()
    {

    }

    public OBPath loadPath (Map<String, Object> attrs, RectF parentRect, float graphicScale, Map<String, Object> defs)
    {
        String pathString = (String) attrs.get("d");
        Path p = OBPath.pathFromSVGPath(pathString);
        Matrix t = new Matrix();
        t.preTranslate(parentRect.left, parentRect.top);
        t.preScale(parentRect.width(), parentRect.height());
        p.transform(t);
        OBPath path = makeShape(attrs, p, graphicScale, defs);
        return path;
    }

    public OBPath makeShape (Map<String, Object> attrs, Path p, float graphicScale, Map<String, Object> defs)
    {
        OBPath im = null;
        String fillstr = (String) attrs.get("fill");
        if (fillstr != null && fillstr.startsWith("url("))
        {
            Map<String, Object> settings = new HashMap<String, Object>();
            settings.putAll(attrs);
            settings.put("defs", defs);
            //
            Class c = OBPath.classForSettings(settings);
            try
            {
                im = (OBPath) c.getConstructor(Path.class).newInstance(p);
                im.sizeToBoundingBox();
                //
                im.setFillColor(0);
                String url = fillstr.substring(5, fillstr.length() - 1);
                Object obj = null;
                if (defs != null)
                {
                    obj = defs.get(url);
                }
                //
                if (obj != null)
                {
                    if (obj instanceof URadialGradient)
                    {
                        ((OBRadialGradientPath) im).takeValuesFrom((UGradient) obj);
                    }
                    else if (obj instanceof UGradient)
                    {
                        ((OBGradientPath) im).takeValuesFrom((UGradient) obj);
                    }
                }
                //
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            int fill = 0;
            if (fillstr != null)
            {
                fill = OBUtils.colorFromRGBString(fillstr);
                if (attrs.get("fillopacity") != null)
                {
                    float fo = Float.parseFloat((String) attrs.get("fillopacity"));
                    fill = Color.argb((int) (fo * 255), Color.red(fill), Color.green(fill), Color.blue(fill));
                }
            }
            if (p != null)
            {
                im = new OBPath(p);
                im.sizeToBoundingBox();
                im.setFillColor(fill);
            }
            else
            {
                im.backgroundColor = fill;
            }
        }
        return im;
    }

    public OBControl loadShape (Map<String, Object> attrs, String nodeType, float graphicScale, RectF r, Map<String, Object> defs)
    {
        OBControl im = null;
        float x = floatOrZero(attrs, "x");
        float y = floatOrZero(attrs, "y");
        float w = floatOrZero(attrs, "width");
        float h = floatOrZero(attrs, "height");
        RectF f = OB_Maths.denormaliseRect(new RectF(x, y, x + w, y + h), r);
        String s = (String) attrs.get("widthtracksheight");
        if (s != null && s.equals("true"))
        {
            float origheight = floatOrZero(attrs, "pxheight");
            if (origheight > 0)
            {
                float ratio = f.height() / origheight;
                float newWidth = floatOrZero(attrs, "pxwidth") * ratio;
                float diff = newWidth - f.width();
                f.left -= diff / 2;
                f.right = f.left + newWidth;
            }
        }
        else
        {
            s = (String) attrs.get("heighttrackswidth");
            if (s != null && s.equals("true"))
            {
                float origwidth = floatOrZero(attrs, "pxwidth");
                if (origwidth > 0)
                {
                    float ratio = f.width() / origwidth;
                    float newHeight = floatOrZero(attrs, "pxheight") * ratio;
                    float diff = newHeight - f.height();
                    f.top -= diff / 2;
                    f.bottom = f.top + newHeight;
                }
            }
        }
        String fit = (String) attrs.get("fit");
        if (fit != null)
        {
            String[] arr = fit.split(",");
            Set<String> fitattrs = new HashSet<String>();
            fitattrs.addAll(Arrays.asList(arr));
            RectF vf = new RectF(bounds());
            if (fitattrs.contains("fitwidth"))
            {
                f.left = 0;
                f.right = vf.width();
            }
            if (fitattrs.contains("fitheight"))
            {
                f.top = 0;
                f.bottom = vf.height();
            }
            if (fitattrs.contains("stretchtotop"))
            {
                f.top = 0;
            }
            if (fitattrs.contains("stretchtobottom"))
            {
                f.bottom = vf.bottom;
            }
        }
        Path p = null;
        RectF b = new RectF(f);
        b.bottom -= b.top;
        b.top = 0;
        b.right -= b.left;
        b.left = 0;
        if (nodeType.equals("circle"))
        {
            p = new Path();
            p.addOval(b, Path.Direction.CCW);
        }
        else
        {
            if (attrs.get("cornerradius") != null)
            {
                float cr = floatOrZero(attrs, "cornerradius");
                cr *= b.height();
                p = new Path();
                p.addRoundRect(b, cr, cr, Path.Direction.CCW);
            }
            else
            {
                p = new Path();
                p.addRect(b, Path.Direction.CCW);
            }
        }
        im = makeShape(attrs, p, graphicScale, defs);
        im.setPosition(f.centerX(), f.centerY());
        return im;
    }

    public Object loadImageFromDictionary (Map<String, Object> image, float graphicScale, Map<String, Object> defs)
    {
        boolean scalable = true;
        Map<String, Object> attrs = (Map<String, Object>) image.get("attrs");
        //
        RectF r = new RectF(bounds());
        String imageID = (String) attrs.get("id");
        String par = (String) attrs.get("parent");
        if (par != null)
        {
            OBControl c = objectDict.get(par);
            if (c == null)
                System.out.println("No parent object " + par + "for" + imageID);
            else
            {
                if (c.parent != null)
                    r = convertRectFromControl(c.bounds(), c);
                else
                    r = c.frame();
            }
        }
        String posstr = (String) attrs.get("pos");
        PointF pos = null;
        if (posstr != null)
        {
            pos = OBUtils.pointFromString(posstr);
        }
        //
        OBControl im = null;
        String nodeType = (String) image.get("nodetype");
        String srcname = (String) attrs.get("src");
        if (nodeType.equals("image"))
            im = loadImageWithName(srcname, pos, r, false);
        else if (nodeType.equals("vector"))
        {
            im = loadVectorWithName(srcname, pos, r, false);
            int skinOffset = 0;
            if (attrs.get("skinoffset") != null)
                skinOffset = Integer.parseInt((String) attrs.get("skinoffset"));
            int skincol = OBUtils.SkinColour(OBUtils.SkinColourIndex() + skinOffset);
            ((OBGroup) im).substituteFillForAllMembers("skin.*", skincol);
            if (attrs.get("fill") != null)
            {
                int col = OBUtils.colorFromRGBString((String) attrs.get("fill"));
                ((OBGroup) im).substituteFillForAllMembers("col.*", col);
            }
        }
        else if (nodeType.equals("path"))
        {
            scalable = false;
            im = loadPath(attrs, r, graphicScale, defs);
        }
        else if (nodeType.equals("linearGradient"))
        {
            Map<String, Object> d = new HashMap<String, Object>();
            d.putAll(attrs);
            d.putAll(image);
            return gradientFromAttributes(d);
        }
        else if (nodeType.equals("radialGradient"))
        {
            Map<String, Object> d = new HashMap<String, Object>();
            d.putAll(attrs);
            d.putAll(image);
            return radialGradientFromAttributes(d);
        }
        else if (nodeType.equals("group"))
        {
            scalable = false;
            List<Map<String, Object>> chs = (List<Map<String, Object>>) image.get("children");
            if (chs != null && chs.size() > 0)
            {
                List<OBControl> objs = new ArrayList<OBControl>();
                for (Map<String, Object> d : chs)
                {
                    Object o = loadImageFromDictionary(d, graphicScale, defs);
                    Map<String, Object> chattrs = (Map<String, Object>) d.get("attrs");
                    String objID = (String) chattrs.get("id");
                    if (OBControl.class.isInstance(o))
                    {
                        OBControl ch = (OBControl) o;
                        objs.add(ch);
                        if (objID != null)
                        {
                            objectDict.put(objID, ch);
                            ch.setProperty("name", objID);
                        }
                    }
                    else
                        defs.put(objID, o);
                }
                OBGroup grp = new OBGroup(objs);
                grp.buildObjectDict();
                im = grp;
                if (attrs.get("hasmask") != null)
                {
                    String hm = (String) attrs.get("hasmask");
                    if (hm.equals("true"))
                    {
                        OBControl mask = objectWithMaxZpos(grp.members);
                        im.setMaskControl(mask);
                        //
                        grp.members.remove(mask);
                        mask.parent = null;
//                        grp.removeMember(mask); // omg
                    }
                }
            }
            float gx = 1, gy = 1;
            if (attrs.get("scalex") != null)
            {
                gx = floatOrZero(attrs, "scalex");
                gy = gx;
            }
            if (attrs.get("scaley") != null)
            {
                gy = floatOrZero(attrs, "scaley");
            }
            if (!(gx == 1 && gy == 1))
            {
                im.setScaleX(gx);
                im.setScaleY(gy);
            }
            if (attrs.get("fill") != null)
            {
                int col = OBUtils.colorFromRGBString((String) attrs.get("fill"));
                ((OBGroup) im).substituteFillForAllMembers("col.*", col);
            }
        }
        else if (nodeType.equals("text"))
        {
            scalable = false;
            OBPath path = (OBPath) loadShape(attrs, "rectangle", graphicScale, r, defs);
            path.sizeToBoundingBox();
            if (attrs.get("stroke") != null)
            {
                OBStroke str = new OBStroke(attrs, true);
                path.setStroke(str);
            }
            RectF b = path.bounds();
            List<OBControl> mems = Arrays.asList((OBControl) path);
            OBGroup grp = new OBGroup(mems);
            List<Map<String, Object>> chs = (List<Map<String, Object>>) image.get("children");
            for (Map<String, Object> d : chs)
            {
                Map<String, String> objattrs = (Map<String, String>) d.get("attrs");
                String s, fontFamily = "Helvetica";
                if ((s = objattrs.get("font-family")) != null)
                    fontFamily = s;
                float fontSize = 20;
                if ((s = objattrs.get("font-size")) != null)
                    fontSize = applyGraphicScale(Float.parseFloat(s));
                boolean italic = false, bold = false;
                if ((s = objattrs.get("font-weight")) != null && s.equals("bold"))
                    bold = true;
                if ((s = objattrs.get("font-style")) != null && s.equals("italic"))
                    italic = true;
                int style = Typeface.NORMAL;
                if (bold)
                    if (italic)
                        style = Typeface.BOLD_ITALIC;
                    else
                        style = Typeface.BOLD;
                else if (italic)
                    style = Typeface.ITALIC;
                Typeface tf = Typeface.create(fontFamily, style);
                if (tf == null)
                    tf = Typeface.defaultFromStyle(style);
                OBLabel lab = new OBLabel((String) (d.get("contents")), tf, fontSize);
                if ((s = objattrs.get("fill")) != null)
                {
                    int c = OBUtils.svgColorFromRGBString(s);
                    lab.setColour(c);
                    //lab.borderColor = UIColor.redColor();
                    //lab.borderWidth = 1;
                }
                PointF pt = new PointF();
                pt.x = Float.parseFloat(objattrs.get("x"));
                pt.y = Float.parseFloat(objattrs.get("y"));
                pt = OB_Maths.locationForRect(pt, b);
                //CGRect tbounds = boundingBoxForString(lab.string, font, b.size);
                //pt.y -= (tbounds.size.height + tbounds.origin.y);
                RectF lf = lab.bounds();
                pt.x += lf.width() / 2;
                //pt.y -= fontSize / 2;
                pt.y = pt.y - lab.baselineOffset() + lf.height() / 2f;
                lab.setPosition(pt);
                lab.setZPosition(1f);
                //lab.setBorderColor(Color.BLACK);
                //lab.setBorderWidth(2f);
                grp.insertMember(lab, 0, "t");

            }
            grp.buildObjectDict();
            im = grp;
        }
        else if (nodeType.equals("rectangle"))
        {
            scalable = false;
            im = loadShape(attrs, nodeType, graphicScale, r, defs);
        }
        else if (nodeType.equals("circle"))
        {
            scalable = false;
            im = loadShape(attrs, nodeType, graphicScale, r, defs);
        }
        if (im != null)
        {
            float scx = 1, scy = 1, shadScale = 1;
            if (scalable)
            {
                scx = scy = graphicScale;
                if (attrs.get("scalex") != null)
                {
                    scx = floatOrZero(attrs, "scalex") * graphicScale;
                    scy = scx;
                }
                if (attrs.get("scaley") != null)
                {
                    scy = floatOrZero(attrs, "scaley") * graphicScale;
                }
                if (im instanceof OBImage)
                {
                    OBImage obim = (OBImage) im;
                    scx *= (1f / obim.intrinsicScale());
                    scy *= (1f / obim.intrinsicScale());
                }

                if (!(scx == 1 && scy == 1))
                {
                    im.setScaleX(scx);
                    im.setScaleY(scy);
                }

            }
            if (attrs.get("rotation") != null)
            {
                float rt = floatOrZero(attrs, "rotation");
                im.setRotation((float) Math.toRadians((double) -rt));
            }
            if (nodeType.equals("vector"))
            {
                OBControl anchor = (OBControl) ((OBGroup) im).objectDict.get("anchor");
                if (anchor != null)
                {
                    PointF pt = im.convertPointFromControl(anchor.position(), anchor.parent);
                    PointF rpt = OB_Maths.relativePointInRectForLocation(pt, im.bounds());
                    im.setAnchorPoint(rpt);
                }
            }
            if (attrs.get("anchor") != null && !nodeType.equals("rectangle"))
            {
                PointF anc = OBUtils.pointFromString((String) attrs.get("anchor"));
                PointF destPoint = OB_Maths.locationForRect(anc, im.frame());
                PointF vec = OB_Maths.DiffPoints(im.position(), destPoint);
                PointF newPoint = OB_Maths.AddPoints(im.position(), vec);
                im.setPosition(newPoint);
            }
            if (attrs.get("stroke") != null)
            {
                OBStroke str = new OBStroke(attrs, true);
                im.setStroke(str);
                if (im instanceof OBPath)
                    ((OBPath)im).setLineWidth(graphicScale * ((OBPath)im).lineWidth());
            }
            if (attrs.get("opacity") != null)
            {
                im.setOpacity(floatOrZero(attrs, "opacity"));
            }
            if (attrs.get("shadowcolour") != null)
            {
                int col = OBUtils.colorFromRGBString((String) attrs.get("shadowcolour"));
                float ratio = Math.abs(1 / shadScale);
                if (!scalable)
                    ratio = graphicScale;
                float opacity = 1, xoff = 0, yoff = 0, rad = 3;
                if (attrs.get("shadowopacity") != null)
                    opacity = Float.parseFloat((String) attrs.get("shadowopacity"));
                if (attrs.get("shadowradius") != null)
                    rad = Float.parseFloat((String) attrs.get("shadowradius")) * ratio;
                if (attrs.get("shadowxoffset") != null)
                    xoff = Float.parseFloat((String) attrs.get("shadowxoffset")) * ratio;
                if (attrs.get("shadowyoffset") != null)
                    yoff = Float.parseFloat((String) attrs.get("shadowyoffset"))* ratio;
                im.setShadow(rad, opacity, xoff, yoff, col);
            }
            im.setZPosition((floatOrZero(attrs, "zpos")));
            if ((attrs.get("hidden") != null && attrs.get("hidden").equals("true")) || (attrs.get("display") != null && attrs.get("display").equals("none")))
                im.hide();
            im.setProperty("attrs", attrs);
            if (srcname == null)
                srcname = "";
            im.textureKey = srcname;
            im.setRasterScale(Math.abs(im.scaleX()));
        }
        return im;
    }

    public float graphicScale ()
    {
        return (Float) (Config().get(MainActivity.mainActivity.CONFIG_GRAPHIC_SCALE));
    }

    public void loadEvent (String eventID)
    {
        float graphicScale = graphicScale();
        Map<String, Object> event = (Map<String, Object>) eventsDict.get(eventID);
        if (event == null)
            event = new HashMap<>();
        eventAttributes = (Map<String, String>) event.get("attrs");
        if (eventAttributes == null)
            eventAttributes = new HashMap<>();
        if (eventAttributes.get("colour") != null)
        {
            int col = (OBUtils.colorFromRGBString(eventAttributes.get("colour")));
            OBUtils.getFloatColour(col, topColour);
            OBUtils.getFloatColour(col, bottomColour);
        }
        if (eventAttributes.get("gradienttop") != null)
        {
            int col1 = OBUtils.colorFromRGBString(eventAttributes.get("gradienttop"));
            int col2 = OBUtils.colorFromRGBString(eventAttributes.get("gradientbottom"));
            OBUtils.getFloatColour(col1, topColour);
            OBUtils.getFloatColour(col2, bottomColour);
        }
        Map<String, Object> defs = new HashMap<String, Object>();
        List<Map<String, Object>> imageList = (List<Map<String, Object>>) event.get("objects");
        if (imageList != null)
            for (Map<String, Object> image : imageList)
            {
                Object im = loadImageFromDictionary(image, graphicScale, defs);
                if (im != null)
                {
                    Map<String, Object> attrs = (Map<String, Object>) image.get("attrs");
                    String objID = (String) attrs.get("id");
                    if (OBControl.class.isInstance(im))
                    {
                        objectDict.put(objID, (OBControl) im);
                        attachControl((OBControl) im);
                    }
                    else
                        defs.put(objID, im);
                }
            }
    }

    public void processParams ()
    {
        if (params != null)
        {
            if (String.class.isInstance(params))
            {
                Map<String, String> d = new HashMap<>();
                String components[] = ((String) params).split("/");
                for (int i = 0; i < components.length; i++)
                {
                    String pieces[] = components[i].split("=");
                    if (pieces.length == 1)
                        d.put((new Integer(i)).toString(), pieces[0]);
                    else if (pieces.length > 1)
                        d.put(pieces[0], pieces[1]);
                }
                parameters = d;
            }
        }
    }

    public String sectionName ()
    {
        String par0 = ((String) params).split("/")[0];
        String parr[] = par0.split(";");
        return parr[0];
    }

    public String sectionAudioName ()
    {
        String par0 = ((String) params).split("/")[0];
        String parr[] = par0.split(";");
        if (parr.length > 1)
            return parr[1];
        return parr[0];
    }

    public String currentEvent ()
    {
        try
        {
            return events.get(eventIndex);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public boolean performSel (String root, String suffix)
    {
        String str = root + suffix;
        try
        {
            Method m = this.getClass().getMethod(str);
            m.invoke(this);
            return true;
        }
        catch (NoSuchMethodException e)
        {
        }
        catch (InvocationTargetException e)
        {
        }
        catch (IllegalAccessException e)
        {
        }
        catch (Exception e)
        {
            System.out.println("OBSectionController.exception caught:" + e.toString());
            e.printStackTrace();
        }
        return false;
    }

    public void doVisual (String scene)
    {
        lockScreen();
        if (!performSel("setScene", scene))
            setSceneXX(scene);
        unlockScreen();
    }

    public void setSceneXX (String scene)
    {

    }

    public void doAudio (String scene) throws Exception
    {

    }

    public void fin ()
    {

    }

    public void setScene (final String scene)
    {
        new OBRunnableSyncUI()
        {
            public void ex ()
            {
                doVisual(scene);
            }
        }.run();

        try
        {
            doAudio(scene);
            switchStatus(scene);
        }
        catch (Exception exception)
        {
        }
    }

    public long switchStatus (String scene)
    {
        return 0;
    }

    public void nextScene ()
    {
        if (++eventIndex >= events.size())
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground (Void... params)
                {
                    fin();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
        else
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground (Void... params)
                {
                    setScene(events.get(eventIndex));
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    public void displayAward () throws Exception
    {

    }

    public void hideControls (String pattern)
    {
        for (OBControl c : filterControls(pattern))
        {
            if (c != null)
            {
                c.hide();
            }
        }

    }

    public void showControls (String pattern)
    {
        for (OBControl c : filterControls(pattern))
        {
            if (c != null)
            {
                c.show();
            }
        }
    }

    public void deleteControls (String pattern)
    {
        for (String s : filterControlsIDs(pattern))
        {
            OBControl c = objectDict.get(s);
            detachControl(c);
            objectDict.remove(s);
        }
    }

    public void attachControl (OBControl control)
    {
        if (control == null) return;
        //
        if (!attachedControls.contains(control))
        {
            attachedControls.add(control);
            control.controller = this;
            sortedAttachedControlsValid = false;
            RectF f = control.frame();
            invalidateView((int) f.left, (int) f.top, (int) f.right, (int) f.bottom);
        }
    }

    public void detachControl (OBControl control)
    {
        if (control == null) return;
        //
        RectF f = control.frame();
        attachedControls.remove(control);
        control.controller = null;
//        if(control.texture != null)
//            control.texture.cleanUp();
        invalidateView((int) f.left, (int) f.top, (int) f.right, (int) f.bottom);
        sortedAttachedControlsValid = false;
    }

    public List<String> filterControlsIDs (String pattern)
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
        return arr;
    }

    public List<OBControl> filterControls (String pattern)
    {
        List<OBControl> arr = new ArrayList<OBControl>();
        for (String name : filterControlsIDs(pattern))
            arr.add(objectDict.get(name));
        return arr;
    }

    public List<OBControl> sortedFilteredControls (String pattern)
    {
        List<String> arr = filterControlsIDs(pattern);
        Collections.sort(arr);
        List<OBControl> arr3 = new ArrayList<OBControl>();
        for (String k : arr)
            arr3.add(objectDict.get(k));
        return arr3;
    }

    public List<OBControl> zPositionSortedFilteredControls (String pattern)
    {
        List<String> arr = filterControlsIDs(pattern);
        Collections.sort(arr, new Comparator<String>()
        {
            @Override
            public int compare (String o1, String o2)
            {
                OBControl c1 = objectDict.get(o1);
                OBControl c2 = objectDict.get(o2);
                float z1 = c1.zPosition();
                float z2 = c2.zPosition();
                if (z1 < z2)
                    return -1;
                if (z1 > z2)
                    return 1;
                return 0;
            }
        });
        List<OBControl> carr = new ArrayList<OBControl>();
        for (String s : arr)
            carr.add(objectDict.get(s));
        return carr;
    }

    public void populateSortedAttachedControls()
    {
        if (!sortedAttachedControlsValid)
        {
            List<OBControl> tempList = new ArrayList<>(attachedControls);
            boolean quit = false;
            for (int i = 0;i < tempList.size();i++)
            {
                if(tempList.get(i) == null)
                {
                    Logger logger = Logger.getAnonymousLogger();
                    logger.log(Level.SEVERE, "ALAN SOMETHING HAPPENED WHILE WORKING WITH TEMP SORTEDATTACHEDCONTROL LIST");
                    quit= true;
                    break;
                }
                tempList.get(i).tempSortInt = i;
            }
            if(quit)
                return;
            Collections.sort(tempList, new Comparator<OBControl>()
            {
                @Override
                public int compare (OBControl lhs, OBControl rhs)
                {
                    if (lhs.zPosition() < rhs.zPosition())
                        return -1;
                    if (lhs.zPosition() > rhs.zPosition())
                        return 1;
                    if (lhs.tempSortInt < rhs.tempSortInt)
                        return -1;
                    if (lhs.tempSortInt > rhs.tempSortInt)
                        return 1;
                    return 0;
                }
            });

            synchronized(sortedAttachedControls)
            {
                sortedAttachedControls.clear();
                sortedAttachedControls.addAll(tempList);
                sortedAttachedControlsValid = true;
            }

        }
    }

    public void drawControls (Canvas canvas)
    {
        Rect clipb = canvas.getClipBounds();
        populateSortedAttachedControls();
        for (OBControl control : sortedAttachedControls)
        {
            if (control.frame().intersects(clipb.left, clipb.top, clipb.right, clipb.bottom))
                control.draw(canvas);
        }
    }

    public void renderBackground (OBRenderer renderer)
    {

        ((ColorShaderProgram) renderer.colourProgram).setUniforms(renderer.projectionMatrix);

        GradientRect gr = renderer.gradientRect;
        gr.draw(renderer, 0, 0, renderer.w, renderer.h, topColour, bottomColour);

    }

    public void renderBackgroundo (OBRenderer renderer)
    {
        int POSITION_COMPONENT_COUNT = 3;
        int COLOR_COMPONENT_COUNT = 3;
        int BYTES_PER_FLOAT = 4;
        int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
        float r1 = topColour[0],
                g1 = topColour[1],
                b1 = topColour[2];
        float r2 = bottomColour[0],
                g2 = bottomColour[1],
                b2 = bottomColour[2];
        float vertices[] = {
                -1, 1, 0, r1, g1, b1,
                -1, -1, 0, r2, g2, b2,
                1, -1, 0, r1, g1, b1,
                -1, 1, 0, r2, g2, b2
        };
        TextureRect.fillOutRectVertexData(vertices, 0, 0, renderer.w, renderer.h, POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT);
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexData.put(vertices);

        int aPositionLocation = ((ColorShaderProgram) renderer.colourProgram).getPositionAttributeLocation();
        int aColorLocation = ((ColorShaderProgram) renderer.colourProgram).getColorAttributeLocation();

        // Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_POSITION_LOCATION.
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);
// Bind our data, specified by the variable vertexData, to the vertex
        // attribute at location A_COLOR_LOCATION.
        vertexData.position(POSITION_COMPONENT_COUNT);
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);

        glEnableVertexAttribArray(aColorLocation);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void render (OBRenderer renderer)
    {
        renderLock.lock();
        renderBackground(renderer);
        TextureShaderProgram textureShader = (TextureShaderProgram) renderer.textureProgram;
        textureShader.useProgram();
        populateSortedAttachedControls();

        List<OBControl> clist = null;
        synchronized(sortedAttachedControls)
        {
            clist = sortedAttachedControls;
            for (OBControl control : clist)
            {
                if (!control.hidden())
                    control.render(renderer, this, renderer.projectionMatrix);
            }
        }

        renderLock.unlock();

    }

    public OBImage loadImageWithName (String nm, PointF pt, RectF r, boolean attach)
    {
        OBImage im = OBImageManager.sharedImageManager().imageForName(nm);
        if (im != null)
        {
            PointF pos = OB_Maths.locationForRect(pt, r);
            im.setPosition(pos);
            if (attach)
                attachControl(im);
            return im;
        }
        return null;
    }

    public OBImage loadImageWithName (String nm, PointF pt, RectF r)
    {
        return loadImageWithName(nm, pt, r, true);
    }

    public OBGroup loadVectorWithName (String nm, PointF pt, RectF r, boolean attach)
    {
        OBGroup im = OBImageManager.sharedImageManager().vectorForName(nm);
        if (im != null)
        {
            PointF pos = OB_Maths.locationForRect(pt, r);
            im.setPosition(pos);
            if (attach)
                attachControl(im);
            return im;
        }
        return null;
    }

    public OBGroup loadVectorWithName (String nm, PointF pt, RectF r)
    {
        return loadVectorWithName(nm, pt, r, true);
    }

    public long setStatus (int st)
    {
        long sttime;
        synchronized (events)
        {
            theStatus = st;
            sttime = System.nanoTime();
            statusTime = sttime;
        }
        return sttime;
    }

    public Boolean statusChanged (long sttime)
    {
        return sttime != statusTime;
    }

    public int status ()
    {
        return theStatus;
    }

    public void movePointerToPoint (PointF pt, float secs, boolean wait)
    {
        if (secs < 0)
        {
            float dist = OB_Maths.PointDistance(pt, thePointer.position());
            secs = dist / ((float) theMoveSpeed * Math.abs(secs));
        }
        if (Math.abs(pt.x) < 2.0 && Math.abs(pt.y) < 2.0)
            pt = OB_Maths.locationForRect(pt, bounds());
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(pt, thePointer)), secs, wait, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }

    public void movePointerToPoint (PointF pt, float angle, float secs, boolean wait)
    {
        if (secs < 0)
        {
            float dist = OB_Maths.PointDistance(pt, thePointer.position());
            secs = dist / ((float) theMoveSpeed * Math.abs(secs));
        }
        if (Math.abs(pt.x) < 2.0 && Math.abs(pt.y) < 2.0)
            pt = OB_Maths.locationForRect(pt, bounds());
        List<OBAnim> anims = new ArrayList<OBAnim>();
        anims.add(OBAnim.moveAnim(pt, thePointer));
        anims.add(OBAnim.rotationAnim((float) Math.toRadians((double) angle), thePointer));
        OBAnimationGroup.runAnims(anims, secs, wait, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }

    List AnimsForMoveToPoint(List<OBControl> objs,PointF pos)
    {
        OBControl obj = objs.get(0);
        PointF currPos = obj.position();
        PointF delta = OB_Maths.DiffPoints(pos, currPos);
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl ob : objs)
        {
            PointF objpos = ob.position();
            OBAnim anim = OBAnim.moveAnim(OB_Maths.AddPoints(objpos, delta),ob);
            anims.add(anim);
        }
        return anims;
    }


    public void movePointerToPointWithObject(OBControl control,PointF pt,float angle,float secs,boolean wait)
    {
        List<OBAnim> anims = AnimsForMoveToPoint(Arrays.asList(thePointer),pt);
        OBAnim moveObject = OBAnim.moveAnim(pt,control);
        anims.add(moveObject);
        OBAnim turnAnim = OBAnim.rotationAnim((float) Math.toRadians(angle),thePointer);
        anims.add(turnAnim);
        OBAnimationGroup.runAnims(anims,secs,wait,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void movePointerForwards (float distance, float secs)
    {
        float ang = thePointer.rotation;
        float x = (float) Math.sin((float) ang);
        float y = -(float) Math.cos((float) ang);
        float len = (float) Math.sqrt(x * x + y * y);
        float ratio = distance / len;
        x *= ratio;
        y *= ratio;
        PointF pos = new PointF(thePointer.position().x, thePointer.position().y);
        pos.x += x;
        pos.y += y;
        movePointerToPoint(pos, secs, true);
    }

    public void loadPointerStartPoint (final PointF startPoint, final PointF targetPoint)
    {
        final OBSectionController vc = this;
                lockScreen();
                if (thePointer == null)
                {
                    OBGroup arm = MainActivity.mainActivity.armPointer();
                    arm.setZPosition(POINTER_ZPOS);
                    float graphicScale = MainActivity.mainActivity.applyGraphicScale(1);
                    arm.scaleX = arm.scaleY = graphicScale;
                    arm.texturise(false, vc);
                    thePointer = arm;
                    attachControl(arm);
                }
                else
                {
                    thePointer.show();
                }
                thePointer.setPosition(startPoint);
                thePointer.pointAt(targetPoint);
                unlockScreen();
    }

    public void loadPointer (int orientation)
    {
        PointF startPoint, targetPoint;
        switch (orientation)
        {
            case POINTER_BOTLEFT:
                startPoint = new PointF(1, 0);
                targetPoint = new PointF(0, 1);
                break;
            case POINTER_BOTRIGHT:
                startPoint = new PointF(0, 0);
                targetPoint = new PointF(1, 1);
                break;
            case POINTER_LEFT:
                startPoint = new PointF(0.75f, 1.1f);
                targetPoint = new PointF(0, 0);
                break;
            case POINTER_MIDDLE:
                startPoint = new PointF(0.5f, 1.1f);
                targetPoint = new PointF(0.5f, 0);
                break;
            case POINTER_RIGHT:
                startPoint = new PointF(0.25f, 1.1f);
                targetPoint = new PointF(1, 0);
                break;
            default:
                startPoint = new PointF(0.5f, 1.1f);
                targetPoint = new PointF(0.5f, 0);
        }
        startPoint = OB_Maths.locationForRect(startPoint, bounds());
        targetPoint = OB_Maths.locationForRect(targetPoint, bounds());
        loadPointerStartPoint(startPoint, targetPoint);

    }

    void _playAudio (String fileName)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
            OBAudioManager.audioManager.startPlaying(fileName);
        else
        {
            if (fileName != null)
                fileName = new String(fileName);
            final String fn = fileName;
            new OBRunnableSyncUI()
            {
                public void ex ()
                {
                    _playAudio(fn);
                }
            }.run();
        }
    }

    void _playAudio (final String fileName, double atTime)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
            OBAudioManager.audioManager.startPlaying(fileName, atTime);
        else
        {
            new OBRunnableSyncUI()
            {
                public void ex ()
                {
                    _playAudio(fileName);
                }
            }.run();
        }
    }

    public void playAudioFromTo (final String fileName, final double fromTime, final double toTime)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
        {
            _playAudio(fileName, fromTime);
            final long t = (long) ((toTime - fromTime) * 1000);
            Handler h = new Handler();
            h.postDelayed(new Runnable()
            {
                @Override
                public void run ()
                {
                    OBAudioManager.audioManager.stopPlaying();
                }
            }, t);

        }
        else
        {
            new OBRunnableSyncUI()
            {
                public void ex ()
                {
                    playAudioFromTo(fileName, fromTime, toTime);
                }
            }.run();
        }
    }


    public void playSFX (final String fileName)
    {
        new OBRunnableSyncUI()
        {
            public void ex ()
            {
                OBAudioManager.audioManager.startPlayingSFX(fileName);
            }
        }.run();
    }

    public long takeSequenceLockInterrupt (boolean interrupt)
    {
        long token = SystemClock.uptimeMillis();
        if (interrupt)
        {
            sequenceToken = token;
            playAudio(null);
            sequenceLock.lock();
        }
        else
        {
            sequenceLock.lock();
            sequenceToken = token;
        }
        return token;
    }

    public void unlockSequenceLock ()
    {
        sequenceLock.unlock();
    }

    public void checkSequenceToken (long token) throws Exception
    {
        if (token != sequenceToken)
            throw new Exception("Sequence interrupted");
    }

    public long updateAudioQueueToken ()
    {
        synchronized (this)
        {
            audioQueueToken = SystemClock.uptimeMillis();
            return audioQueueToken;
        }
    }

    public void stopAllAudio ()
    {
        updateAudioQueueToken();
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                OBAudioManager.audioManager.stopAllAudio();
            }
        });
    }

    public void playAudio (String fileName)
    {
        audioQueueToken = SystemClock.uptimeMillis();
        _playAudio(fileName);
    }


    void _playBackgroundAudio (String fileName)
    {
        if (Looper.myLooper() == Looper.getMainLooper())
            OBAudioManager.audioManager.startPlaying(fileName, "1");
        else
        {
            final String fn = new String(fileName);
            new OBRunnableSyncUI()
            {
                public void ex ()
                {
                    _playBackgroundAudio(fn);
                }
            }.run();
        }
    }

    public void playBackgroundAudio (String fileName, Boolean wait) throws Exception
    {
        _playBackgroundAudio(fileName);
        if (wait)
        {
            waitBackground();
        }
    }


    public void waitBackground () throws Exception
    {
        waitAudioChannel(OBAudioManager.AM_BACKGROUND_CHANNEL);
    }

    public void waitSFX () throws Exception
    {
        waitAudioChannel(OBAudioManager.AM_SFX_CHANNEL);
    }


    public void playSfxAudio (String audioName, boolean wait) throws Exception
    {
        if (audioScenes == null)
            return;
        Map<String, List<String>> sc = (Map<String, List<String>>) audioScenes.get("sfx");
        if (sc != null)
        {
            List<Object> evl = (List<Object>) (Object) sc.get(audioName); //yuk!
            if (evl != null && evl.size() > 0)
                playSFX((String) evl.get(0));
            if (wait)
                waitSFX();
        }
    }

    public List<String> currentAudio (String audioCategory)
    {
        Map<String, List> eventd = (Map<String, List>) audioScenes.get(currentEvent());
        if (eventd == null)
            return null;
        return eventd.get(audioCategory);
    }

    public void playAudioQueuedo (List<Object> qu, final boolean wait) throws Exception
    {
        if(qu == null)
            return;

        Lock lock = null;
        Condition condition = null;
        if (wait)
        {
            lock = new ReentrantLock();
            condition = lock.newCondition();
        }
        long token;
        synchronized (this)
        {
            audioQueueToken = SystemClock.uptimeMillis();
            token = audioQueueToken;
        }
        final List<Object> fqu = qu;
        final long ftoken = token;
        final Lock flock = lock;
        final Condition fcondition = condition;
        final OB_MutBoolean fabort = new OB_MutBoolean(_aborting);
        new AsyncTask<Void, Void, Void>()
        {
            protected Void doInBackground (Void... params)
            {
                try
                {
                    for (Object obj : fqu)
                    {
                        if (ftoken != audioQueueToken)
                            break;
                        if (obj instanceof Integer)
                        {
                            Thread.sleep(((Integer) obj).intValue());
                        }
                        else
                        {
                            _playAudio((String) obj);
                            if (wait)
                                waitAudio();
                            else
                                waitAudioNoThrow();
                        }
                    }
                }
                catch (Exception exception)
                {
                    fabort.value = true;
                }
                if (flock != null)
                {
                    flock.lock();
                    fcondition.signalAll();
                    flock.unlock();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        if (wait)
        {
            lock.lock();
            condition.await();
            lock.unlock();
        }
        if (fabort.value)
        {
            _aborting = true;
            throw new OBUserPressedBackException();
//            throw new Exception("BackException");
        }
    }

    public OBConditionLock playAudioQueued(List<Object> qu,boolean wait) throws OBUserPressedBackException
    {
        return playAudioQueued(qu,wait,1);
    }

    public OBConditionLock playAudioQueued(final List<Object> qu,final boolean wait,final int noLoops) throws OBUserPressedBackException
    {
        final OBConditionLock lock = new OBConditionLock(PROCESS_NOT_DONE) ;
        if(qu == null)
            return lock;
        final long token = updateAudioQueueToken();
        final OBConditionLock flock = lock;
        final OB_MutBoolean fabort = new OB_MutBoolean(_aborting);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                try
                {
                    int _noLoops = noLoops;
                    while(_noLoops != 0 && token == audioQueueToken)
                    {
                        for (Object obj : qu)
                        {
                            if (token != audioQueueToken)
                                break;
                            if (obj instanceof Integer)
                            {
                                Thread.sleep(((Integer) obj).intValue());
                            }
                            else
                            {
                                _playAudio((String) obj);
                                if (wait)
                                    waitAudio();
                                else
                                    waitAudioNoThrow();
                            }
                        }
                        _noLoops--;
                    }
                }
                catch (Exception exception)
                {
                    fabort.value = true;
                }
                lock.lock() ;
                lock.unlockWithCondition(PROCESS_DONE);
           }
        });

        if(wait)
        {
            lock.lockWhenCondition(PROCESS_DONE);
            lock.unlock() ;
        }
        if (fabort.value)
        {
            _aborting = true;
            throw new OBUserPressedBackException();
        }
        return lock;
    }

    public void playAudioQueued (List<Object> qu) throws Exception
    {
        playAudioQueued(qu, false);
    }

    public void setReplayAudio (List<Object> arr)
    {
        if (arr != _replayAudio)
        {
            _replayAudio = arr;
        }
    }

    public void setReplayAudioScene (String scene, String event)
    {
        Map<String, List<String>> sc = (Map<String, List<String>>) audioScenes.get(scene);
        if (sc != null)
        {
            List<Object> arr = (List<Object>) (Object) sc.get(event); //yuk!
            if (arr != null)
                setReplayAudio(arr);
        }
    }

    public List<Object> emptyReplayAudio ()
    {
        List<Object> arr = _replayAudio;
        _replayAudio = null;
        return arr;
    }

    protected void _replayAudio ()
    {
        try
        {
            playAudioQueued(_replayAudio, true);
        }
        catch (Exception exception)
        {
        }
    }

    public void replayAudio()
    {
        if(busyStatuses.contains(status()))
            return;

        if (_replayAudio != null)
        {
            setStatus(status());
            new AsyncTask<Void, Void, Void>()
            {
                protected Void doInBackground (Void... params)
                {
                    _replayAudio();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    public void exitEvent ()
    {
        setStatus(STATUS_EXITING);
        playAudio(null);
        if (!_aborting)
        {
            _aborting = true;
            new OBRunnableUI()
            {
                public void ex ()
                {
                    stopAllAudio();
                    MainActivity.mainViewController.popViewController();
                }
            }.run();
        }
    }

    public void goBack ()
    {
        if (!_aborting && !MainActivity.mainViewController.navigating)
            exitEvent();
    }

    public void prevPage ()
    {

    }

    public void nextPage ()
    {

    }

    void waitAudioNoThrow ()
    {
        OBAudioManager.audioManager.waitAudio();
    }

    public boolean waitForAudio ()
    {
        if (_aborting)
            return false;
        OBAudioManager.audioManager.waitAudio();
        return !_aborting;
    }

    public void waitAudio () throws Exception
    {
        waitAudioChannel(OBAudioManager.AM_MAIN_CHANNEL);
    }


    public void waitAudioChannel (String ch) throws Exception
    {
        if (_aborting)
        {
            throw new OBUserPressedBackException();
//            throw new Exception("BackException");
        }
        OBAudioManager.audioManager.waitAudioChannel(ch);
        if (_aborting)
        {
            throw new OBUserPressedBackException();
//            throw new Exception("BackException");
        }
    }

    public void waitAudioAndCheck (long stTime) throws Exception
    {
        if (!statusChanged(stTime))
        {
            waitAudio();
        }
        if (statusChanged(stTime))
        {
            throw new OBUserPressedBackException();
//            throw new Exception("BackException");
        }

    }

    void _wait (double secs)
    {
        try
        {
            Thread.sleep((long) (secs * 1000));
        }
        catch (InterruptedException e)
        {
        }
    }

    public void waitForSecsNoThrow (double secs)
    {
        if (!_aborting)
            _wait(secs);
    }

    public void waitForSecs (double secs) throws Exception
    {
        if (!_aborting)
        {
            _wait(secs);
        }
        if (_aborting)
        {
            throw new OBUserPressedBackException();
//            throw new Exception("BackException");
        }
    }

    public void checkAbort() throws Exception
    {
        if (_aborting)
        {
            throw new OBUserPressedBackException();
        }
    }

    public void displayTick () throws Exception
    {
        new OBRunnableSyncUI()
        {
            public void ex ()
            {
                if (tick == null)
                {
                    tick = loadVectorWithName("tick", new PointF(0.5f, 0.5f), new RectF(bounds()), false);
                    tick.setScale(graphicScale());
                    tick.setZPosition(100);
                }
                else
                {
//                    MainActivity.mainActivity.log("say something pretty"); // MICHAL!!!
                }
                attachControl(tick);
            }
        }.run();
        playSFX("ting");
        waitForSecs(1);
        new OBRunnableSyncUI()
        {
            public void ex ()
            {
                invalidateControl(tick);
                detachControl(tick);
            }
        }.run();
    }

    public void invalidateControl (OBControl c)
    {
        RectF f = c.frame();
        invalidateView((int) (Math.floor((double) f.left)), (int) (Math.floor((double) f.top)),
                (int) (Math.floor((double) f.right)), (int) (Math.floor((double) f.bottom)));
    }


    public int buttonFlags ()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON | OBMainViewController.SHOW_TOP_RIGHT_BUTTON;
    }

    public Path convertPathFromControl (Path p, OBControl c)
    {
        return c.convertPathToControl(p, null);
    }

    public PointF convertPointFromControl (PointF pt, OBControl c)
    {
        return c.convertPointToControl(pt, null);
    }

    public PointF convertPointToControl (PointF pt, OBControl c)
    {
        return c.convertPointFromControl(pt, null);
    }

    public RectF convertRectFromControl (RectF r, OBControl c)
    {
        return c.convertRectToControl(r, null);
    }

    public RectF convertRectToControl (RectF r, OBControl c)
    {
        return c.convertRectFromControl(r, null);
    }

    public List<OBAnim> animsForMoveToPoint (List<OBControl> objs, PointF pos)
    {
        OBControl obj = objs.get(0);
        PointF currPos = obj.position();
        PointF delta = OB_Maths.DiffPoints(pos, currPos);
        List<OBAnim> anims = new ArrayList<>();
        for (OBControl o : objs)
        {
            PointF opos = o.position();
            OBAnim anim = OBAnim.moveAnim(OB_Maths.AddPoints(opos, delta), o);
            anims.add(anim);
        }
        return anims;
    }

    public void moveObjects (List<OBControl> objs, PointF pos, float duration, int timingFunction)
    {
        if (duration < 0)
        {
            OBControl c = objs.get(0);
            duration = OBUtils.durationForPointDist(c.position(), pos, theMoveSpeed * -duration);
        }
        OBAnimationGroup.runAnims(animsForMoveToPoint(objs, pos), duration, true, timingFunction, this);
    }

    public OBGLView glView ()
    {
        return MainActivity.mainViewController.glView();
    }

    public float right ()
    {
        return glView().getRight();
    }

    public float bottom ()
    {
        return glView().getBottom();
    }

    public PointF pointForDestPoint (PointF destpt, float degrees)
    {
        float h = bottom() + applyGraphicScale(10);
        float ydist = h - destpt.y;
        float xdist = (float) Math.tan(Math.toRadians(degrees)) * ydist;
        return new PointF(destpt.x + xdist, h);
    }

    public void goToCard (Class nextSection, String param)
    {
        goToCard(nextSection, param, false);
    }

    public void goToCard (final Class nextSection, final String param, final boolean withAnimation)
    {
        _aborting = true;
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                MainViewController().pushViewController(nextSection, withAnimation, true, param, true);
            }
        });
    }

    public void reprompt (final long sttime, final List<Object> audio, float delaySecs)
    {
        reprompt(sttime, audio, delaySecs, null);
    }

    public void reprompt (final long sttime, final List<Object> audio, float delaySecs, final OBUtils.RunLambda actionBlock)
    {
        if (statusChanged(sttime))
            return;
        OBUtils.runOnOtherThreadDelayed(delaySecs, new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                if (!statusChanged(sttime))
                {
                    if (audio != null)
                    {
                        boolean wait = (actionBlock != null);
                        playAudioQueued(audio, wait);
                    }
                    if (actionBlock != null)
                        actionBlock.run();

                }
            }
        });
    }

    public void mergeAudioScenesForPrefix(String mergePrefix)
    {
        mergePrefix = mergePrefix +".";
        for(String ksc : audioScenes.keySet())
        {
            Map<String,List> scene = (Map<String, List>) audioScenes.get(ksc);
            for(String kac : scene.keySet() )
                if(kac.startsWith(mergePrefix))
                {
                    String targPrefix = kac.substring(mergePrefix.length() );
                    scene.put(targPrefix,scene.get(kac));
                }
        }
    }


    public void onResume()
    {

    }

    public void onPause()
    {

    }
}

