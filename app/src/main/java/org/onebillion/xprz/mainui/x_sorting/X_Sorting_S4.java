package org.onebillion.xprz.mainui.x_sorting;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 06/05/16.
 */
public class X_Sorting_S4 extends XPRZ_SectionController
{
    int totalObjectCount = 0;
    OBControl correctPaintPot,currentPaintPot;
    List<OBControl> correctObjArray,pots;
    int correctColour;
    List<OBControl> boyObj;
    List<OBControl> girlObj;
    OBPath collar = null;

    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        //eva[0] = "4n";
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

    public void setSceneXX(String scene)
    {
        if (totalObjectCount == 0)
        {
            deleteControls("obj.*");
            if (collar != null)
                collar.hide();
        }
        super.setSceneXX(scene);

        currentPaintPot = null;
        String potName = eventAttributes.get("targetpot");
        if (potName != null)
        {
            correctPaintPot = objectDict.get(potName);
            Map<String,Object> targetAttrs = correctPaintPot.attributes();
            correctColour = OBUtils.colorFromRGBString((String)targetAttrs.get("fillcolour"));
            correctObjArray = filterControls(String.format("%s.*",eventAttributes.get("targetitem")));
        }
        targets = filterControls("objj.*");
        if (totalObjectCount == 0)
        {
            totalObjectCount = targets.size();
            for (OBControl obj : targets)
            {
                obj.setDoubleSided(true);
                obj.texturise(true,this);
            }
        }
        pots = filterControls("objpot.*");
        if (collar == null)
        {
            float w = pots.get(0).width()+applyGraphicScale(4f);
            Path p = new Path();
            p.addCircle(w/2, w/2, w/2, Path.Direction.CCW);
            collar = new OBPath(p);
            //collar.setLineWidth(applyGraphicScale(4f));
            collar.sizeToBoundingBoxIncludingStroke();
            collar.setFillColor(Color.GRAY);
            collar.setZPosition(pots.get(0).zPosition()-0.01f);
            attachControl(collar);
            collar.hide();
        }
    }

    public void setScene4n()
    {
        deleteControls("obj.*");
        super.setSceneXX(currentEvent());
        correctObjArray = filterControls("objj(a|b)[0-9]*");
        targets = OBControl.controlsSortedFrontToBack(correctObjArray);
        pots = filterControls("objpot.*");

        currentPaintPot = null;

        boyObj = filterControls("objjb.*");
        girlObj = filterControls("objja.*");
    }


    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }

    void demoPointer() throws Exception
    {
        PointF restPt = OB_Maths.locationForRect(0.6f, 0.5f, new RectF(bounds()));
        PointF startPt = OB_Maths.locationForRect(0.6f, 1.01f, new RectF(bounds()));
        loadPointerStartPoint(startPt, restPt);
        movePointerToPoint(restPt, -1, true);
        waitForSecs(0.01f);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true);
        waitForSecs(0.5f);
    }

    public void demo4a() throws Exception
    {
        demoPointer();

        OBControl yellowPot = objectDict.get("objpot3");
        PointF firstPt = yellowPot.position();
        movePointerToPoint(firstPt,-1,true);

        playSFX("correct");

        selectPaintPot(yellowPot);

        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(0.5f);

        OBGroup sockOne = (OBGroup)objectDict.get("objja1");
        PointF secondPt = OB_Maths.locationForRect(0.8f, 0.8f, sockOne.frame());
        movePointerToPoint(secondPt, -1, true);
        waitForSecs(0.4f);
        int col = 0xfffff344;
        sockOne.substituteFillForAllMembers("col.*",col);

        OBGroup sockTwo = (OBGroup)objectDict.get("objja2");
        PointF thirdPt = OB_Maths.locationForRect(0.5f, 0.6f, sockTwo.frame());
        movePointerToPoint(thirdPt, -1, true);
        sockTwo.substituteFillForAllMembers("col.*",col);
        waitForSecs(1.5f);
        thePointer.hide();

        removeAllBorders();
        currentPaintPot = null;

        sockOne.substituteFillForAllMembers("col.*",Color.WHITE);
        sockTwo.substituteFillForAllMembers("col.*",Color.WHITE);
        nextScene();
    }

    void checkCorrectObj(OBGroup targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            if (currentPaintPot != correctPaintPot)
            {
                gotItWrongWithSfx();
                nextObj();
                return;
            }
            if (correctObjArray.contains(targ))
            {
                gotItRightBigTick(false);
                targ.substituteFillForAllMembers("col.*",correctColour);
                targets.remove(targ);
                correctObjArray.remove(targ);
                totalObjectCount--;
            }
            else
            {
                gotItWrongWithSfx();
            }
            nextObj();
        }
        catch (Exception exception)
        {
        }
    }

    void selectPaintPot(OBControl paintPot)
    {
        removeAllBorders();
        currentPaintPot = paintPot;
        addColourBorder(paintPot);
    }

    void checkPaintPot(OBControl paintPot)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            if (paintPot != currentPaintPot)
            {
                selectPaintPot(paintPot);
                if (paintPot == correctPaintPot)
                    gotItRightBigTick(false);
                else
                    gotItWrongWithSfx();
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception exception)
        {
        }
    }

    void checkPaintPot4n(OBControl paintPot)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            if (paintPot != currentPaintPot)
            {
                selectPaintPot(paintPot);
                playSfxAudio("click",false);
                correctPaintPot = paintPot;
                Map<String,Object>targetAttrs = correctPaintPot.attributes();
                correctColour = OBUtils.colorFromRGBString((String)targetAttrs.get("fillcolour"));
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception exception)
        {
        }
    }

    void changeFinalSceneObjColour(OBGroup targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            if (currentPaintPot != null && correctObjArray.contains(targ))
            {
                gotItRightBigTick(false);
                targ.substituteFillForAllMembers("col.*",correctColour);
                correctObjArray.remove(targ);

                if (correctObjArray.isEmpty())
                {
                    finalAnimation();
                    nextScene();
                    return;
                }
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception exception)
        {
        }
    }

    void finalAnimation()
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                PointF endPt = OB_Maths.locationForRect(0.5f,-1,new RectF(bounds()));
                moveObjects(girlObj,endPt,2, OBAnim.ANIM_EASE_IN);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                PointF endPt = OB_Maths.locationForRect(0.5f,-1,new RectF(bounds()));
                waitForSecsNoThrow(0.3);
                moveObjects(boyObj,endPt,2, OBAnim.ANIM_EASE_IN);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        try
        {
            waitForSecs(2.3);
        }
        catch (Exception e)
        {
        }
    }

    void nextObj() throws Exception
    {
        if (correctObjArray.isEmpty())
        {
            if (totalObjectCount == 0)
                sceneComplete();
            else
                sceneSectionComplete();
        }
        else
            switchStatus(currentEvent());
    }

    void sceneSectionComplete() throws Exception
    {
        waitForSecs(0.3f);
        nextScene();
    }

    void sceneComplete() throws Exception
    {
        waitForSecs(0.5f);
        gotItRightBigTick(true);
        nextScene();
    }

    void addColourBorder(OBControl colour)
    {
        lockScreen();
        //colour.setBorderWidth(MainActivity.mainActivity.applyGraphicScale(8.0f));
        //colour.setCornerRadius(colour.width() / 2f);
        //colour.setBorderColor(Color.GRAY);
        collar.setPosition(colour.position());
        collar.setZPosition(colour.zPosition()-0.0001f);
        collar.show();
        unlockScreen();
    }

    void removeAllBorders()
    {
        lockScreen();
        for (OBControl borderObject : pots)
        {
            //borderObject.setBorderWidth(0);
            //borderObject.setBorderColor(0);
        }
        collar.hide();
        unlockScreen();
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    OBControl findPot(PointF pt)
    {
        return finger(-1,2,pots,pt);
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            new AsyncTask<Void, Void, Void>()
            {
                protected Void doInBackground(Void... params)
                {
                    if (currentEvent().equals("4n"))
                    {
                        if ((target = findPot(pt))!=null)
                            checkPaintPot4n(target);
                        else if ((target = findTarget(pt))!=null)
                            changeFinalSceneObjColour((OBGroup)target);
                    }
                    else
                    {
                        if ((target = findPot(pt))!=null)
                            checkPaintPot(target);
                        else if ((target = findTarget(pt))!=null)
                            checkCorrectObj((OBGroup)target);
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

        }

    }



}
