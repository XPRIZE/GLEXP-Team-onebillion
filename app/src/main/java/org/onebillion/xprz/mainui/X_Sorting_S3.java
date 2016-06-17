package org.onebillion.xprz.mainui;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 19/05/16.
 */
public class X_Sorting_S3 extends XPRZ_SectionController
{
    int targetCount;
    List<OBControl> bigObjectsFull;
    List<OBControl> smallObjectsFull;
    List<OBControl> bigObjectsHalf;
    List<OBControl> smallObjectsHalf;
    List<OBControl> dropBoxArray;

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
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

    public void setUpScene()
    {
        setSceneXX(currentEvent());
        hideSmile();
        setUpTargets();
    }

    public void setScene3a()
    {
        setSceneXX(currentEvent());
        bigSmile();
        smallSmile();
        hideSmile();
    }

    public void setScene3b()
    {
        deleteControls("obja3");
        setUpScene();
        setUpDragObjectsForEvent("3b-2");
        hideSmile();
        setDropBoxCount();
    }

    public void setScene3c()
    {
        deleteControls("obja.*");
        setUpScene();
        setUpDragObjectsForEvent("3c-2");
        hideSmile();
        setDropBoxCount();
    }

    public void setScene3d()
    {
        deleteControls("objb.*");
        deleteControls("obja.*");
        deleteControls("objd.*");
        setUpScene();
        setUpDragObjectsForEvent("3d-2");
        bigSmile();
        smallSmile();
        hideSmile();
        setDropBoxCount();
    }

    public void setScene3e()
    {
        deleteControls("objb.*");
        deleteControls("obja.*");
        deleteControls("objd.*");
        setUpScene();
        setUpDragObjectsForEvent("3e-2");

        OBGroup bigGirl = (OBGroup) objectDict.get("objd1");
        OBControl fullGirlSmile = bigGirl.objectDict.get("mouth_2");

        bigObjectsFull = Collections.singletonList(fullGirlSmile);

        OBGroup smallGirl = (OBGroup) objectDict.get("objd2");
        OBControl fullSmallGirlSmile = smallGirl.objectDict.get("mouth_2");
        smallObjectsFull = Arrays.asList(fullSmallGirlSmile);

        OBControl halfGirlSmile = bigGirl.objectDict.get("mouth_1");
        bigObjectsHalf = Collections.singletonList(halfGirlSmile);

        OBControl halfSmallGirlSmile = smallGirl.objectDict.get("mouth_1");
        smallObjectsHalf = Collections.singletonList(halfSmallGirlSmile);

        bigSmile();
        smallSmile();
        hideSmile();
        setDropBoxCount();
    }

    public void setScene3f()
    {
        deleteControls("obja.*");
        setSceneXX(currentEvent());

        setUpDragObjectsForEvent("3f-2");

        OBGroup bigGirl = (OBGroup) objectDict.get("objd1");
        OBControl fullGirlSmile = bigGirl.objectDict.get("mouth_2");
        OBControl closedHand = bigGirl.objectDict.get("handClosed");

        bigObjectsFull = Arrays.asList(fullGirlSmile, closedHand);

        OBGroup smallGirl = (OBGroup) objectDict.get("objd2");
        OBControl fullSmallGirlSmile = smallGirl.objectDict.get("mouth_2");
        OBControl closedSmallHand = smallGirl.objectDict.get("handClosed");

        smallObjectsFull = Arrays.asList(fullSmallGirlSmile, closedSmallHand);

        OBControl halfGirlSmile = bigGirl.objectDict.get("mouth_1");
        OBControl openHand = bigGirl.objectDict.get("handOpen");

        bigObjectsHalf = Arrays.asList(halfGirlSmile, openHand);


        OBControl halfSmallGirlSmile = smallGirl.objectDict.get("mouth_1");
        OBControl openSmallHand = smallGirl.objectDict.get("handOpen");

        smallObjectsHalf = Arrays.asList(halfSmallGirlSmile, openSmallHand);

        hideSmile();
        setUpTargets();
        setDropBoxCount();
    }

    public void setScene3g()
    {
        deleteControls("obj.*");
        setUpScene();
        setUpDragObjectsForEvent("3g-2");

        OBGroup bigBoy = (OBGroup) objectDict.get("objf1");
        OBControl fullSmile = bigBoy.objectDict.get("mouth_2");
        bigObjectsFull = Collections.singletonList(fullSmile);
        OBControl halfSmile = bigBoy.objectDict.get("mouth_1");
        bigObjectsHalf = Collections.singletonList(halfSmile);

        hideSmile();
        setDropBoxCount();
    }

    public void setScene3h()
    {
        deleteControls("obj.*");
        setUpScene();
        setUpDragObjectsForEvent("3h-2");
        setDropBoxCount();
        OBControl smallPicture = objectDict.get("objd3");
        smallPicture.setMasksToBounds(true);
    }


    void demoPointer() throws Exception
    {
        PointF restPt = objectDict.get("obja3").position();
        PointF startPt = OB_Maths.locationForRect(1.2f, 1.01f, new RectF(bounds()));
        loadPointerStartPoint(startPt, restPt);
        waitForSecs(0.01f);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true);
        waitForSecs(0.5f);
    }

    public void demo3a() throws Exception
    {
        hideSmile();
        hideSmallSmile();
        OBGroup bigBoy = (OBGroup) objectDict.get("objd1");
        OBControl smallboy = objectDict.get("objd2");
        OBControl redShirt = objectDict.get("obja3");
        demoPointer();
        PointF firstPt = objectDict.get("obja3").position();
        movePointerToPoint(firstPt, -1, true);

        PointF restPt = OB_Maths.locationForRect(0.6f,0.7f, new RectF(bounds()));
        waitForSecs(0.5f);
        List<OBControl>objs = Arrays.asList(redShirt,thePointer);
        moveObjects(objs,OB_Maths.locationForRect(0.5f,0.53f,smallboy.frame()),-1, OBAnim.ANIM_EASE_IN_EASE_OUT);

        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(0.5f);

        movePointerToPoint(restPt,-1,true);
        waitForSecs(0.2f);

        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.53f,smallboy.frame()),-1,true);
        waitForSecs(0.3f);
        moveObjects(objs,OB_Maths.locationForRect(0.5f,0.43f,bigBoy.frame()),-1, OBAnim.ANIM_EASE_IN_EASE_OUT);

        showSmile("a");

        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 2, true);
        waitForSecs(0.5f);

        thePointer.hide();
        waitForSecs(0.5f);
        nextScene();
    }
    void setUpDragObjectsForEvent(String ev)
    {
        Map<String,Object> event = (Map<String, Object>) eventsDict.get(ev);
        Map<String, Map<String, Object>> objectsdict = (Map<String, Map<String, Object>>) event.get("objectsdict");

        for (String k : objectsdict.keySet())
        {
            OBControl c = objectDict.get(k);
            PointF pt = c.position();
            c.setProperty("origpos",new PointF(pt.x,pt.y));

            c.setProperty("destpos",pointFromObjectsDict(objectsdict,k));
        }
    }

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }
    void setDropBoxCount()
    {
        dropBoxArray = filterControls("objd.*");
    }

    void showSmile(String tag)
    {
        lockScreen();
        List<OBControl>full,half;
        OBControl character;
        if (tag.equals("a"))
        {
            full = bigObjectsFull;
            half = bigObjectsHalf;
        }
        else
        {
            full = smallObjectsFull;
            half = smallObjectsHalf;
        }
        for (OBControl obj : full)
            obj.setHidden(false);

        for (OBControl obj : half)
            obj.setHidden(true);
        if (full.size() > 0)
        {
            character = full.get(0).primogenitor();
            if (character != null)
            {
                character.setNeedsRetexture();
                character.invalidate();
            }
        }
        unlockScreen();
    }

    void hideSmile()
    {
        lockScreen();
        for (OBControl obj : bigObjectsFull)
        {
            obj.hidden = true;
        }

        for (OBControl obj : bigObjectsHalf)
        {
            obj.hidden = false;
        }

        for (OBControl obj : smallObjectsFull)
        {
            obj.hidden = true;
        }

        for (OBControl obj : smallObjectsHalf)
        {
            obj.hidden = false;
        }
        OBControl character = objectDict.get("objd1");
        if (character != null)
            character.setNeedsRetexture();
        character = objectDict.get("objd2");
        if (character != null)
            character.setNeedsRetexture();
        unlockScreen();
    }

    void hideSmallSmile()
    {
        for (OBControl obj : smallObjectsFull)
        {
            obj.hidden = true;
        }

        for (OBControl obj : smallObjectsHalf)
        {
            obj.hidden = false;
        }
        OBControl character = objectDict.get("objd2");
        if (character != null)
            character.setNeedsRetexture();
    }

    void bigSmile()
    {
        OBGroup bigBoy = (OBGroup)objectDict.get("objd1");
        OBControl fullSmile = bigBoy.objectDict.get("mouth_2");
        bigObjectsFull = Collections.singletonList(fullSmile);

        OBControl halfSmile = bigBoy.objectDict.get("mouth_1");
        bigObjectsHalf = Collections.singletonList(halfSmile);
    }

    void smallSmile()
    {
        OBGroup smallBoy = (OBGroup)objectDict.get("objd2");
        OBControl fullSmile = smallBoy.objectDict.get("mouth_2");
        smallObjectsFull = Collections.singletonList(fullSmile);

        OBControl halfSmile = smallBoy.objectDict.get("mouth_1");
        smallObjectsHalf = Collections.singletonList(halfSmile);
    }

    void setUpTargets()
    {
        targets = filterControls("obja.*");
        targetCount = targets.size();

        for (OBControl obj : targets)
        {
            obj.setProperty("origpos",new PointF(obj.position().x,obj.position().y));
        }
    }

    void nextObj() throws Exception
    {
        if (dropBoxArray.size() > 0)
            switchStatus(currentEvent());
        else
        {
            sceneComplete();
        }
    }

    void sceneComplete() throws Exception
    {
        waitForSecs(0.5f);
        gotItRightBigTick(true);
        nextScene();
    }

    void flyHome()
    {
        PointF targPoint = (PointF) target.propertyValue("origpos");
        moveObjects(Arrays.asList(target),targPoint,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
    }

    OBControl dropPointUnderPoint(PointF pt)
    {
        for (OBControl dropPoint : filterControls("objd.*"))
        {
            if (dropPoint.frame().contains(pt.x,pt.y))
                return dropPoint;
        }
        return null;
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);

            OBControl dropPoint = dropPointUnderPoint(pt);
            if (dropPoint == null)
            {
                flyHome();
                switchStatus(currentEvent());
                return;
            }
            if (dropPoint.attributes().get("tag").equals(target.attributes().get("tag")))
            {
                gotItRightBigTick(false);
                PointF correctPt = (PointF) target.propertyValue("destpos");
                moveObjects(Collections.singletonList(target),correctPt,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                target.setZPosition(Float.parseFloat((String) target.attributes().get("zpos")));
                showSmile((String) dropPoint.attributes().get("tag"));
                targets.remove(target);
                dropBoxArray.remove(dropPoint);
                nextObj();
            }
            else
            {
                gotItWrongWithSfx();
                flyHome();
                switchStatus(currentEvent());
            }
            return;
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
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                new AsyncTask<Void, Void, Void>()
                {
                    protected Void doInBackground(Void... params)
                    {
                        checkDragTarget(c, pt);
                        return null;
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

            }
        }
    }

}
