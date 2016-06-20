package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 17/06/16.
 */
public class XPRZ_SectionIT extends XPRZ_SectionController
{
    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
        unlockScreen();
    }

    public long switchStatus(String scene)
    {
        if (Arrays.asList("it6","it7").contains(scene))
            return setStatus(STATUS_WAITING_FOR_DRAG);
        if (Arrays.asList("it9").contains(scene))
            return setStatus(STATUS_AWAITING_ARROW_CLICK);
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
            }});
    }

    public void setSceneXX(String scene)
    {
        if (!Arrays.asList("it1","it6","it9").contains(scene))
            deleteControls(".*");
        super.setSceneXX(scene);
        targets = filterControls("obj.*");
    }

    public void setSceneit5()
    {
        deleteControls(".*");
        loadEvent("masterb");
        super.setSceneXX(currentEvent());
        setUpDragObjectsForEvent("it5-2");
        targets = filterControls("obj.*");
    }

    public void setSceneit7()
    {
        deleteControls(".*");
        loadEvent("masterb");
        super.setSceneXX(currentEvent());
        setUpDragObjectsForEvent("it7-2");
        targets = filterControls("obj.*");
    }

    public void setSceneit8()
    {
        super.setSceneXX(currentEvent());
    }

    public void setSceneit9()
    {
        targets = filterControls("confirm");
    }

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }

    public void setUpDragObjectsForEvent(String ev)
    {
        Map<String,Object> event = (Map<String, Object>) eventsDict.get(ev);
        Map<String, Map<String, Object>> objectsdict = (Map<String, Map<String, Object>>) event.get("objectsdict");

        for (String k : objectsdict.keySet())
        {
            OBControl c = objectDict.get(k);
            PointF pt = c.position();
            c.setProperty("origpos",new PointF(pt.x,pt.y));
            c.setProperty("destpos",pointFromObjectsDict(objectsdict,k));
            Map<String,Object>odict = objectsdict.get(k);
            Map<String,Object>oattrs = (Map<String, Object>) odict.get("attrs");
            String fstr = (String) oattrs.get("scalex");
            if (fstr != null)
            {
                c.setProperty("scale",c.scale());
                float f = Float.parseFloat(fstr);
                c.setProperty("destscale",f);
            }
            c.setProperty("destcontainer",oattrs.get("parent"));
        }
    }

    public void demoit0() throws Exception 
    {
        PointF restPt = OB_Maths.locationForRect(0.5f,0.8f,new RectF(bounds()));
        PointF startPt = pointForDestPoint(restPt,15);
        loadPointerStartPoint(startPt,restPt);

        movePointerToPoint(restPt,-1,true);
        waitForSecs(0.1f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.5f);

        PointF butPt = OB_Maths.locationForRect(0.5f,1.1f,MainViewController().topRightButton.frame);

        float angle = thePointer.rotation();
        float angle2 = angle - (float)Math.toRadians(5);
        movePointerToPoint(butPt,0,-1,true);

        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.5f);

        movePointerToPoint(restPt,(float)Math.toDegrees(angle2),-1,true);

        waitAudio();
        waitForSecs(0.5);

        OBControl obj = objectDict.get("obj");
        playSfxAudio("tock",false);
        obj.show();


        waitAudio();
        waitForSecs(0.3);

        playAudioQueuedScene(currentEvent(),"DEMO2",true);
        waitAudio();

        waitForSecs(0.2);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,obj.frame()),(float)Math.toDegrees(angle),-1,true);


        waitForSecs(0.01);
        obj.highlight();
        playSfxAudio("tap",false);
        waitAudio();
        waitForSecs(0.6);
        obj.lowlight();
        movePointerToPoint(restPt,0.3f, true);
        waitForSecs(0.4);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

    public void demoit5() throws Exception
    {
        OBControl obj = objectDict.get("obj");
        PointF cpt = obj.position();
        PointF restPt = OB_Maths.locationForRect(1.0f,1.2f,obj.frame());
        PointF vec = OB_Maths.DiffPoints(restPt, cpt);
        float h = bounds().height();
        float sc = (h + 1.0f - cpt.y) / vec.y;
        vec = OB_Maths.ScalarTimesPoint(sc,vec);
        PointF offpt = OB_Maths.AddPoints(cpt, vec);

        loadPointerStartPoint(offpt,cpt);

        movePointerToPoint(restPt,-1,true);
        waitForSecs(0.01f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitAudio();

        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.6f,obj.frame()),-1,true);
        waitForSecs(0.01f);
        waitAudio();

        OBControl dropbox = objectDict.get("dropbox");
        PointF destPt = (PointF) obj.propertyValue("destpos");

        moveObjects(Arrays.asList(obj,thePointer),destPt,-0.6f,OBAnim.ANIM_EASE_IN_EASE_OUT);
        playAudio("correct");
        waitAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.2f,1.2f,dropbox.frame()),-1,true);

        waitForSecs(0.3f);

        thePointer.hide();

        moveObjects(Arrays.asList(obj), (PointF) obj.propertyValue("origpos"),0.3f,OBAnim.ANIM_EASE_IN_EASE_OUT);

        waitForSecs(0.6f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitAudio();

        nextScene();
    }

    public void demoit8() throws Exception
    {
        PointF destPoint = OB_Maths.locationForRect(-0.1f, 0.3f, objectDict.get("confirm").frame());
        loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.1f, new RectF(bounds())),destPoint);
        movePointerToPoint(destPoint,-1,true);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.5f);
        thePointer.hide();
        nextScene();
    }

    public void endBody()
    {
        if (!currentEvent().equals("it9"))
            return;
        final long sttime = statusTime;
        OBUtils.runOnOtherThreadDelayed(2f, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if (sttime == statusTime)
                {
                    OBControl confirm = objectDict.get("confirm");
                    try
                    {
                        for (int i = 0;i < 2;i++)
                        {
                            confirm.setOpacity(0.5f);
                            waitForSecs(0.3f);
                            confirm.setOpacity(1);
                            waitForSecs(0.3f);
                        }
                    }
                    catch (Exception exception)
                    {
                        confirm.setOpacity(1);
                    }
                }
            }
        });
    }

    public void nextObj() throws Exception
    {
        currNo += 1;
        waitAudio();
        if (targets.size() == 0)
        {
            if (!performSel("endOfScene",currentEvent()))
            {
                waitForSecs(1f);
                nextScene();
            }
        }
        else
        {
            performSel("doMain",currentEvent());
            switchStatus(currentEvent());
        }
    }

    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        emptyReplayAudio();
        try
        {
            playSfxAudio("tap",false );
            targ.highlight();
            waitSFX();
            gotItRightBigTick(true);
            waitForSecs(0.5f);
            nextScene();
        }
        catch (Exception exception)
        {
        }
    }

    public void checkArrow(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        emptyReplayAudio();
        try
        {
            playSfxAudio("tap",false );
            targ.highlight();
            waitSFX();
            gotItRightBigTick(true);
            waitForSecs(0.5f);
            targ.hide();
            nextScene();
        }
        catch (Exception exception)
        {
        }
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            List<Object> saveReplay = emptyReplayAudio();
            OBControl dropbox = objectDict.get("dropbox");
            if (dropbox.frame().contains(pt.x,pt.y))
            {
                PointF targPoint = (PointF) target.propertyValue("destpos");
                moveObjects(Collections.singletonList(target),targPoint,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                gotItRightBigTick(false);
                waitForSecs(0.2);
                target.setZPosition(10);
                targets.remove(target);
                setReplayAudio(saveReplay);
                nextObj();
                return;
            }
            OBControl targ = target;
            PointF destpt = (PointF) targ.propertyValue("origpos");
            moveObjects(Collections.singletonList(target),destpt,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
            target.setZPosition(10);
            setReplayAudio(saveReplay);
            switchStatus(currentEvent());
        }
        catch (Exception exception)
        {
        }
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    public void touchDownAtPoint(final PointF pt,View v)
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
        else if (status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        checkDragTarget(c,pt);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }
        else if (status() == STATUS_AWAITING_ARROW_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        checkArrow(c);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }

    }

}
