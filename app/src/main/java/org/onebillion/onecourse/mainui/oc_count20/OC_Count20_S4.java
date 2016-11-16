package org.onebillion.onecourse.mainui.oc_count20;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimParabola;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 29/12/15.
 */
public class OC_Count20_S4 extends OC_SectionController
{
    List<OBControl> numbers,circles;

    public OC_Count20_S4 ()
    {
        super();
    }
    void createNumbers()
    {
        float textSize = Float.parseFloat(eventAttributes.get("textsize"));
        Typeface tf = OBUtils.standardTypeFace();
        numbers = new ArrayList<>();
        circles = new ArrayList<>();
        for (int i = 0;i < 10;i++)
        {
            int j = i + 11;
            OBGroup circle = (OBGroup)objectDict.get(String.format("obj%d",i+1));
            OBPath cp = (OBPath)circle.objectDict.get("col");
            int col = cp.fillColor();
            circle.setProperty("origcolour", col);
            OBLabel txt = new OBLabel(String.format("%d",j),tf,textSize);
            txt.setColour(Color.BLACK);
            txt.setPosition(circle.position());
            numbers.add(txt);
            circles.add(circle);
            circle.insertMember(txt, -1, "txt");
            txt.setPosition(circle.bounds().width() / 2, circle.bounds().height() / 2);
            circle.setRasterScale(circle.scale());
            circle.texturise(false,this);
            circle.hide();
        }
    }
    public void prepare()
    {
        lockScreen();
        super.prepare();
        //view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        createNumbers();
        doVisual(currentEvent());
        unlockScreen();
    }

    boolean currentSceneIsPriorToScene(String sc)
    {
        return eventIndex < events.indexOf(sc);
    }

    public long switchStatus(String scene)
    {
        if (currentSceneIsPriorToScene("4l"))
            return setStatus(STATUS_AWAITING_CLICK);
        return setStatus(STATUS_AWAITING_CLICK2);
    }

    public void start()
    {
        setStatus(0);
        new AsyncTask<Void, Void,Void>()
        {
            protected Void doInBackground(Void... params) {
                try
                {
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.execute();
    }

    public void setSceneXX(String scene)
    {
        if (currentSceneIsPriorToScene("4l"))
        {
            currNo++;
            OBControl obj = objectDict.get(String.format("obj%d",currNo+1));
            RectF f = obj.frame;
            PointF currPos = obj.position();
            PointF origPos = (PointF)obj.propertyValue("origpos");
            PointF offset = OB_Maths.DiffPoints(origPos, currPos);
            f.offset(offset.x, offset.y);
            OBControl dottedline = objectDict.get("dottedline");
            PointF pt = OBUtils.pointFromString(((Map<String,String>)dottedline.propertyValue("attrs")).get("pos"));
            pt = OB_Maths.locationForRect(pt, f );
            dottedline.setPosition(pt);
            dottedline.show();
        }
        else
        {
            loadEvent(scene);
            String s = eventAttributes.get("targetno");
            if (s != null)
                currNo = Integer.parseInt(s)-11;
        }
    }

    public void setScene4a()
    {
        setUpObjectDestsForEvent("4a");
        currNo = 0;
        targets = circles;
    }

    public void setScene4m()
    {
        OBControl dottedLine = objectDict.get("dottedline");
        dottedLine.hide();
    }

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
    }

    public PointF pointFromObjectsDict(Map<String,Map<String,Object>>objectsdict,String objectName)
    {
        Map<String,Object> target = objectsdict.get(objectName);
        Map<String,Object> attrs = (Map<String,Object>)target.get("attrs");
        String parentName = (String)attrs.get("parent");
        PointF relpt = OBUtils.pointFromString((String)attrs.get("pos"));
        View v = glView();
        RectF r = new RectF(v.getLeft(),v.getTop(),v.getRight(),v.getBottom());
        if (parentName != null)
            r = objectDict.get(parentName).frame();
        PointF pt = OB_Maths.locationForRect(relpt, r);
        if (attrs.get("anchor") != null)
        {
            PointF anc = OBUtils.pointFromString((String) attrs.get("anchor"));
            OBControl im = objectDict.get(objectName);
            RectF f = OB_Maths.frameForPosition(im.frame(), pt);
            PointF destPoint = OB_Maths.locationForRect(anc, f);
            PointF vec = OB_Maths.DiffPoints(pt, destPoint);
            PointF newPoint = OB_Maths.AddPoints(pt, vec);
            pt = newPoint;
        }
        return pt;
    }

    void setUpObjectDestsForEvent(String ev)
    {
        Map<String,Object>event = (Map<String,Object>)eventsDict.get(ev);
        Map<String,Map<String,Object>> objectsdict = (Map<String,Map<String,Object>>)event.get("objectsdict");
        for (String k : objectsdict.keySet())
        {
            OBControl c = objectDict.get(k);
            PointF pos = c.position();
            c.setProperty("origpos",new PointF(pos.x,pos.y));
            c.setProperty("destpos",pointFromObjectsDict(objectsdict,k));
        }
    }

    public void demo4a() throws Exception
    {
        waitForSecs(0.1);
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        waitForSecs(0.4);
        for (int i = 0;i < 10;i++)
        {
            OBControl obj = objectDict.get(String.format("obj%d",i+1));
            obj.show();
            playAudioQueuedSceneIndex(currentEvent(), "DEMO2", i, true);
        }
        waitForSecs(0.3);
        List<OBControl> objs = OBUtils.randomlySortedArray(filterControls("obj.*"));
        float delay = 0.9f;
        for (OBControl obj : objs)
        {
            obj.setZPosition(10);
            animDrop(obj, 1);
            waitForSecs(delay + 0.1f);
            delay = delay * delay;
        }
        waitForSecs(2);
        playAudioQueuedScene("sfx", "tap", false);
        objectDict.get("dottedline").show();
        waitAudio();
        OBControl obj = objectDict.get("obj1");
        PointF targPt = OB_Maths.locationForRect(0.8f, 0.8f, obj.frame());
        float dy = bounds().height() - targPt.y + 1;
        PointF startpt = OB_Maths.OffsetPoint(targPt, dy, dy);
        loadPointerStartPoint(startpt,targPt);
        moveObjects(Arrays.asList(thePointer), targPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.3f);
        playAudioQueuedScene("sfx", "tap", false);
        targPt = (PointF)obj.propertyValue("origpos");
        moveObjects(Arrays.asList(obj), targPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        objectDict.get("dottedline").hide();
        playAudioQueuedScene("sfx", "tap", false);
        waitAudio();
        playAudioQueuedScene(currentEvent(), "DEMO3", true);
        waitForSecs(1.5f);
        thePointer.hide();
        nextScene();
    }

    public void demo4l() throws Exception
    {
        OBControl dottedline = objectDict.get("dottedline");
        dottedline.hide();
        waitForSecs(0.5f);

        for (int i = 0;i < circles.size();i++)
        {
            OBControl obj = circles.get(i);
            obj.highlight();
            playAudioQueuedSceneIndex(currentEvent(), "DEMO", i, true);
            obj.lowlight();
        }
        waitForSecs(0.3f);
        nextScene();
    }

    public void highlightCircle(int i,boolean high)
    {
        final OBGroup obj = (OBGroup)objectDict.get(String.format("obj%d",i));
        obj.unCache();
        int c;
        if (high)
            c = Color.WHITE;
        else
            c  = (Integer)obj.propertyValue("origcolour");
        for (OBControl p : obj.filterMembers("col"))
            ((OBPath)p).setFillColor(c);
        obj.setNeedsRetexture();
     }

    public void highlightText(int i,boolean high)
    {
        final OBGroup obj = (OBGroup)objectDict.get(String.format("obj%d",i));
        obj.unCache();
        OBLabel txt = (OBLabel)obj.objectDict.get("txt");
        int c;
        if (high)
            c = Color.RED;
        else
            c  = Color.BLACK;
        txt.setColour(c);
        txt.setNeedsRetexture();
     }

    public void demo4m() throws Exception
    {
        OBGroup obj = (OBGroup)objectDict.get("obj2");
        PointF destPt = OB_Maths.locationForRect(0.8f, 0.8f, obj.frame());
        loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.0f, new RectF(bounds())), destPt);
        PointF restPt = OB_Maths.tPointAlongLine(0.5f, thePointer.position(), destPt);
        moveObjects(Arrays.asList(thePointer), restPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        playAudioScene(currentEvent(), "DEMO", 0);
        waitForSecs(0.2);
        waitAudio();

        moveObjects(Arrays.asList(thePointer), destPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.6);
        playAudioQueuedScene("sfx", "tap", true);
        highlightCircle(2, true);
        waitAudio();
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(0.6f);
        obj = (OBGroup)objectDict.get("obj3");
        moveObjects(Arrays.asList(thePointer), OB_Maths.locationForRect(0.8f, 0.8f, obj.frame()), -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.2f);
        playAudioQueuedScene("sfx", "tap", false);
        highlightCircle(3, true);
        highlightText(3, true);
        waitAudio();
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 2, true);
        waitForSecs(0.2f);
        moveObjects(Arrays.asList(thePointer), OB_Maths.locationForRect(1.7f, 1.7f, obj.frame()), -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 3, true);
        waitForSecs(0.5f);
        thePointer.hide();
        highlightCircle(3, false);
        highlightText(3, false);
        highlightCircle(2, false);
        nextScene();
    }

    public void demo4s() throws Exception
    {
        OBGroup obj = (OBGroup)objectDict.get("obj2");
        PointF destPt = OB_Maths.locationForRect(0.8f, 0.8f, obj.frame());
        loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.0f, new RectF(bounds())), destPt);
        PointF restPt = OB_Maths.tPointAlongLine(0.5f, thePointer.position(), destPt);
        moveObjects(Arrays.asList(thePointer), restPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        playAudioScene(currentEvent(), "DEMO", 0);
        waitForSecs(0.2);
        waitAudio();

        moveObjects(Arrays.asList(thePointer), destPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.6);
        playAudioQueuedScene("sfx", "tap", true);
        highlightCircle(2, true);
        waitAudio();
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(0.6);

        obj = (OBGroup)objectDict.get("obj1");
        moveObjects(Arrays.asList(thePointer), OB_Maths.locationForRect(0.8f, 0.8f, obj.frame()), -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.2);
        playAudioQueuedScene("sfx", "tap", false);
        highlightCircle(1, true);
        highlightText(1, true);
        waitAudio();
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 2, true);
        waitForSecs(0.2);
        moveObjects(Arrays.asList(thePointer), OB_Maths.locationForRect(1.7f, 1.7f, obj.frame()), -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 3, true);
        waitForSecs(0.5);
        thePointer.hide();
        highlightCircle(2, false);
        highlightText(1, false);
        highlightCircle(1, false);
        nextScene();
    }

    public void demo4y() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        waitForSecs(0.4);
        for (int i = 0;i < 10;i++)
        {
            OBControl obj = circles.get(circles.size()-i-1);
            obj.highlight();
            playAudioQueuedSceneIndex(currentEvent(), "DEMO2", i, true);
            obj.lowlight();
        }
        waitForSecs(0.4);
        nextScene();
    }

    public void animDrop(OBControl obj,float secs)
    {
        PointF startPos = obj.position();
        PointF endPos = (PointF)obj.propertyValue("destpos");
        float bottomy = glView().getHeight() - obj.height() / 2;
        OBControl topRect = objectDict.get("toprect");
        float maxbouncey = topRect.height() + obj.height() / 2;
        float dropy = bottomy - startPos.y;
        float attenuation = (bottomy - maxbouncey) / dropy;
        float seconddrop = endPos.y - maxbouncey;
        float seconddropN = seconddrop / (bottomy - maxbouncey);
        float seconddropxN = (float)Math.sqrt((double)seconddropN);
        float t1 = 1.0f + seconddropxN;
        float totalT = 1.0f + t1 * attenuation;
        float secspert = secs / totalT;
        float pixelspert = (endPos.x - startPos.x) / totalT;

        float endX0 = startPos.x + pixelspert * 1.0f;
        OBAnimParabola anim0 = new OBAnimParabola(startPos.y,bottomy,startPos.x,endX0,0,1,obj);
        float duration0 = secspert * 1f;
        OBAnimParabola anim1 = new OBAnimParabola(maxbouncey,bottomy,endX0,endPos.x,-1,t1 - 1,obj);
        float duration1 = secspert * t1 * attenuation;
        List<OBAnim> lob0 = Arrays.asList((OBAnim)anim0);
        List<OBAnim> lob1 = Arrays.asList((OBAnim)anim1);
        OBAnimationGroup.chainAnimations(Arrays.asList(lob0, lob1), Arrays.asList(duration0, duration1), false, Arrays.asList(OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR), 1, this);
    }

    public void checkTarget(OBControl targ)
    {
        int saveStatus = status();
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            int idx = targets.indexOf(targ);
            if (idx == currNo)
            {
                playAudio("correct");
                PointF targPt = (PointF)targ.propertyValue("origpos");
                moveObjects(Arrays.asList(targ), targPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
                waitAudio();
                waitForSecs(0.2f);
                playAudioQueuedScene(currentEvent(),"CORRECT",true);
                nextScene();
            }
            else
            {
                playAudio("wrong");
                waitAudio();
                setReplayAudio(saveReplay);
                playAudioQueuedScene(currentEvent(),"INCORRECT",false);
                setStatus(saveStatus);
            }
        }
        catch (Exception exception)
        {
        }
    }
    public void checkTarget2(OBControl targ)
    {
        int saveStatus = status();
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            int idx = targets.indexOf(targ);
            if (idx == currNo)
            {
                playAudio("tap");
                highlightCircle(idx + 1, true);
                highlightText(idx + 1, true);
                waitAudio();
                displayTick();
                waitForSecs(0.2f);
                int dir = 1;
                if (currentSceneIsPriorToScene("4s"))
                    dir = -1;
                highlightCircle(idx + 1 + dir, true);
                playAudioQueuedScene(currentEvent(), "CORRECT", true);
                highlightCircle(idx + 1, false);
                highlightText(idx + 1, false);
                highlightCircle(idx + 1 + dir, false);
                nextScene();
            }
            else
            {
                targ.highlight();
                playAudio("wrong");
                waitAudio();
                targ.lowlight();
                setReplayAudio(saveReplay);
                playAudioQueuedScene(currentEvent(),"INCORRECT",false);
                setStatus(saveStatus);
            }
        }
        catch (Exception exception)
        {
        }
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        checkTarget(c);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }
        else if (status() == STATUS_AWAITING_CLICK2)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        checkTarget2(c);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }

    }

}
