package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 06/05/16.
 */
public class X_Sorting_S7 extends XPRZ_SectionController
{
    List<OBControl>cards,backs,randomCards;
    float pos0x,pos0y,pos11x,pos11y;
    OBControl firstHit;

    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
        unlockScreen();
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
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

    PointF position(int i)
    {
        int row = i / 4;
        int col = i % 4;
        return new PointF(pos0x + col * ((pos11x - pos0x)/3), pos0y + row * ((pos11y - pos0y)/2));
    }

    void cardStuffAdjustTable(boolean adjustTable)
    {
        PointF pt = objectDict.get("group0").position();
        pos0x = pt.x;
        pos0y = pt.y;
        pt = objectDict.get("back").position();
        pos11x = pt.x;
        pos11y = pt.y;
        List<OBControl> crds = new ArrayList<>();
        for (int i = 0;i < 6;i++)
        {
            String nm = String.format("group%d",i);
            OBControl c = objectDict.get(nm);
            crds.add(c);
            c.setProperty("tag",i);
            c.setRasterScale(MainActivity.mainActivity.applyGraphicScale(c.scale()));
            c.texturise(true,this);
            c.m34 = -1/1200f;
            OBControl cx = c.copy();
            crds.add(cx);
            cx.texturise(true,this);
            attachControl(cx);
        }
        cards = crds;
        //deleteControls("group.*");
        List<OBControl> rcards = OB_utils.randomlySortedArray(cards);
        if (adjustTable)
        {
            rcards.remove(cards.get(0));
            rcards.remove(cards.get(1));
            rcards.add(0,cards.get(0));
            rcards.add(10,cards.get(1));
        }
        randomCards = rcards;
        int i = 0;
        for (OBControl c : randomCards)
        {
            c.setPosition(position(i++));
            //attachControl(c);
            //c.setYRotation((float)((i-1) * (Math.PI / 12)));
            c.setYRotation((float)Math.PI);
            c.setDoubleSided(false);
        }

        List<OBControl> bcks = new ArrayList<>();
        OBGroup back = (OBGroup)objectDict.get("back");
        for (i = 0;i < 12;i++)
        {
            OBControl b = back.copy();
            b.setPosition(randomCards.get(i).position());
            attachControl(b);
            bcks.add(b);
            b.setProperty("down",true);
            b.texturise(true,this);
            b.m34 = -1/1200f;
            b.setDoubleSided(false);
            //b.hide();
        }
        deleteControls("back");
        backs = bcks;
        targets = new ArrayList<>(backs);
        if (adjustTable)
        {
            targets.remove(backs.get(0));
            targets.remove(backs.get(10));
        }
    }

    void maskBack()
    {
        OBGroup back  = (OBGroup)objectDict.get("back");
        OBControl clipper = back.members.get(0);
        clipper = clipper.copy();
        back.setMaskControl(clipper);
    }
    public void setScene7a()
    {
        setSceneXX(currentEvent());
        maskBack();
        OBGroup brolly = (OBGroup)objectDict.get("group1");
        OBControl gcd = brolly.filterMembers("card.*").get(0);
        //brolly.sizeToBox(convertRectFromControl(gcd.frame(),gcd.parent));
        brolly.sizeToMember(gcd);
        cardStuffAdjustTable(true);
    }

    public void setScene7c()
    {
        textureDictionary.clear();
        for (OBControl c : cards)
            detachControl(c);
        for (OBControl c : backs)
            detachControl(c);
        deleteControls("group.*");
        setSceneXX(currentEvent());
        maskBack();
        cardStuffAdjustTable(false);
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

    void doFrame(int[]indexes,boolean down,float frac)
    {
        for (int i : indexes)
        {
            OBControl card = randomCards.get(i);
            OBControl back = backs.get(i);
            float ang;
            if (down)
                ang = (float) Math.PI * frac;
            else
                ang = (float) Math.PI + (float) Math.PI * frac;
            card.setYRotation(ang);

            if (down)
                ang = (float) Math.PI + (float) Math.PI * frac;
            else
                ang = (float) Math.PI * frac;
            back.setYRotation(ang);
        }
    }
    public void animateCards(final int[]indexes,final boolean down) throws Exception
    {
        long starttime = SystemClock.uptimeMillis();
        float duration = 0.5f;
        float frac = 0;
        while (frac <= 1.0)
        {
            long currtime = SystemClock.uptimeMillis();
            frac = (float)(currtime - starttime) / (duration * 1000);
            final float t = (frac);
            new OBRunnableSyncUI(){public void ex()
            {
                doFrame(indexes,down,t);
            }
             }.run();
            waitForSecs(0.02f);
        }
    }

    public void demo7a() throws Exception
    {
        OBControl table0 = cards.get(0);
        OBControl table1 = cards.get(1);
        PointF targPt2 = OB_Maths.locationForRect(0.5f, 1.0f,table1.frame());
        PointF startpt = new PointF(targPt2.x,targPt2.y);
        startpt.y = bounds().height();
        PointF targPt = OB_Maths.locationForRect(1.0f, 1.0f,table0.frame());
        loadPointerStartPoint(startpt,targPt);
        movePointerToPoint(targPt,-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.2f);
        movePointerForwards(MainActivity.mainActivity.applyGraphicScale(50),0.1f);
        waitForSecs(0.05f);
        movePointerForwards(MainActivity.mainActivity.applyGraphicScale(-50),0.1f);
        int onecard[] = {0};
        animateCards(onecard,false);
        movePointerForwards(MainActivity.mainActivity.applyGraphicScale(60),0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.2f);
        movePointerToPoint(targPt2,-1,true);
        waitForSecs(0.2f);
        movePointerForwards(MainActivity.mainActivity.applyGraphicScale(50),0.1f);
        waitForSecs(0.05f);
        movePointerForwards(MainActivity.mainActivity.applyGraphicScale(-50),0.1f);
        onecard[0] = 10;
        animateCards(onecard,false);
        movePointerForwards(MainActivity.mainActivity.applyGraphicScale(60),0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(1f);
        thePointer.hide();
        waitForSecs(1f);
        nextScene();
    }

    void nextObj() throws Exception
    {
        if (targets.size() == 0)
        {
            waitAudio();
            gotItRightBigTick(true);
            waitForSecs(0.5f);
            nextScene();
        }
        else
        {
            switchStatus(currentEvent());
        }
    }

    public void checkTarget(OBControl targ)
    {
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            playSfxAudio("tap",false);
            firstHit = targ;
            int idx = backs.indexOf(targ);
            int arr[] = {idx};
            animateCards(arr,false);
            setReplayAudio(saveReplay);
            setStatus(STATUS_AWAITING_CLICK2);
        }
        catch (Exception exception)
        {
        }
    }

    public void checkTarget2(OBControl targ)
    {
        if (targ == firstHit)
            return;
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            playSfxAudio("tap",false);
            int idx = backs.indexOf(targ);
            int firstIdx = backs.indexOf(firstHit);
            int arr[] = {idx};
            animateCards(arr,false);

            if (randomCards.get(idx).propertyValue("tag").equals(randomCards.get(firstIdx).propertyValue("tag")))
            {
                gotItRightBigTick(false);
                targets.remove(targ);
                targets.remove(firstHit);
            }
            else
            {
                gotItWrongWithSfx();
                int arr2[] = {idx,firstIdx};
                animateCards(arr2,true);
                waitSFX();
                setReplayAudio(saveReplay);
            }
            firstHit = null;
            nextObj();
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
        if (status() == STATUS_AWAITING_CLICK || status() == STATUS_AWAITING_CLICK2)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        if (status() == STATUS_AWAITING_CLICK)
                            checkTarget(c);
                        else
                            checkTarget2(c);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }

    }
}
