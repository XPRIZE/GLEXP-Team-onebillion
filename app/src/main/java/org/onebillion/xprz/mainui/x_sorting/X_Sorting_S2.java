package org.onebillion.xprz.mainui.x_sorting;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 13/03/16.
 */
public class X_Sorting_S2 extends XPRZ_SectionController
{
    int currentCount;
    List<OBControl> validObjects;
    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        //String[] eva = "2k,2l,2m".split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void exitEvent()
    {
        killAnimations();
        super.exitEvent();
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

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
    }

    public void setSceneXX(String scene)
    {
        if (eventIndex <= events.indexOf("2k"))
        {
            deleteControls("obj.*");
            super.setSceneXX(scene);
            setUpTargets();
        }
        invalidateView(0, 0, bounds().width(),bounds().height());
    }

    public void setScene2b()
    {
        //setUpTargets();
    }

    void createCollar()
    {
        OBControl obja1 = objectDict.get("obja1");
        float w = obja1.width() + graphicScale() * 20;
        float radius = w/2;
        Path p = new Path();
        p.addCircle(radius,radius,radius,Path.Direction.CCW);
        OBPath obp = new OBPath(p,w,w,0f,0f);
        obp.setFillColor(Color.LTGRAY);
        attachControl(obp);
        objectDict.put("collar",obp);
        obp.setZPosition(-0.1f);
        obp.hide();
    }
    public void setScene2k()
    {
        setSceneXX("2k");
        createCollar();
        currentCount = 0;
    }

    public void setUpTargets()
    {
        validObjects = filterControls("obja.*");
        targets = filterControls("obj.*");
        for (OBControl c : targets)
        {
            float sc = (float)Math.abs(c.scale());
            c.setRasterScale((float)Math.ceil(sc));
            c.enCache();
            String tag = (String)c.attributes().get("tag");
            if (tag != null && tag.equals("b"))
                c.anchorPoint.set(0.5f, 0.8f);
        }
    }

    public void animate(OBControl targ)
    {
        OBAnimationGroup ag = new OBAnimationGroup();
        float rad = (float)Math.toRadians(7);
        List<OBAnim>anims = new ArrayList<>();
        anims.add(OBAnim.rotationAnim(targ.rotation + rad,targ));
        List<List<OBAnim>> oanims = new ArrayList<>();
        oanims.add(anims);

        anims = new ArrayList<>();
        anims.add(OBAnim.rotationAnim(targ.rotation - rad, targ));
        oanims.add(anims);

        Float durs[] = {0.2f,0.2f};
        Integer tims[] = {OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT};
        ag.chainAnimations(oanims, Arrays.asList(durs), Arrays.asList(tims), -1, this, false);
        registerAnimationGroup(ag, (String) targ.attributes().get("id"));
    }

    public void demoPointer()throws Exception
    {
        PointF restPt = objectDict.get("obja2").position();
        PointF startPt = OB_Maths.locationForRect(0.6f, 1.01f, new RectF(bounds()));
        loadPointerStartPoint(startPt, restPt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.5f, startPt, restPt),-1,true);
        waitForSecs(0.01f);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true);
        waitAudio();
        waitForSecs(0.05f);
    }

    public void demo2a() throws Exception
    {
        demoPointer();

        PointF firstPt = objectDict.get("obja2").position();
        movePointerToPoint(firstPt,-1,true);

        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(0.5f);

        OBControl redSock = objectDict.get("obja1");
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.7f,redSock.frame()),-50,-1,true);

        playAudioQueuedSceneIndex(currentEvent(),"DEMO",2,true);
        waitForSecs(0.5f);

        thePointer.hide();
        nextScene();
    }

    public void nextObj() throws Exception
    {
        if (validObjects.size() == 0)
        {
            waitForSecs(0.3f);
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            killAnimations();
            waitForSecs(0.02f);
            nextScene();
        }
        else
            switchStatus(currentEvent());
    }

    public void checkTarget2k(final OBControl targ)
    {
        int saveStatus = status();
        int targInt = Integer.parseInt((String) targ.attributes().get("tag"));

        try
        {
            if (targInt == currentCount)
            {
                final OBControl collar = objectDict.get("collar");
                new OBRunnableSyncUI()
                {
                    public void ex()
                    {
                        //targ.unCache();
                        //targ.setBorderWidth(8.0f);
                        //targ.cornerRadius = 60;
                        //targ.setBorderColor(Color.LTGRAY);
                        collar.setPosition(targ.position());
                        collar.show();
                    }
                }.run();



                gotItRightBigTick(true);
                waitForSecs(0.5f);

                collar.hide();
                new OBRunnableSyncUI()
                {
                    public void ex()
                    {
                        //targ.borderWidth = 0;
                        //targ.setBorderColor(0);
                    }
                }.run();

                currentCount ++;
                validObjects.remove(targ);
                nextScene();

            }
            else
            {
                gotItWrongWithSfx();
                setStatus(saveStatus);
            }
        }
        catch(Exception e)
        {
        }

    }

    public void checkTarget(OBControl targ)
    {
        int saveStatus = status();
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            playSfxAudio("tap",false);
            if (validObjects.contains(targ))
            {
                animate(targ);
                validObjects.remove(targ);
                nextObj();
            }
            else
            {
                gotItWrongWithSfx();
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
                        if (eventIndex < events.indexOf("2k"))
                            checkTarget(c);
                        else
                            checkTarget2k(c);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }

    }

}
