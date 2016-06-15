package org.onebillion.xprz.mainui;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 20/05/16.
 */
public class X_Sorting_S1 extends XPRZ_SectionController
{
    class SortInterval
    {
        float startx, endx;
        OBControl object;
    }
    List<OBControl> objectsLeftToPlace,intersectingObjects;
    List<OBGroup> boxes;

    void resetIntervalsForBox(OBControl box)
    {
        List<SortInterval> intervals = new ArrayList<>();
        SortInterval si = new SortInterval();
        si.startx = si.endx = 0;
        intervals.add(si);
        si = new SortInterval();
        si.startx = si.endx = box.frame().width();
        intervals.add(si);
        box.setProperty("intervals",intervals);
    }


    void setUpBoxes()
    {
        boxes = (List<OBGroup>)(Object)sortedFilteredControls("bigBox.*");
        for (OBGroup box : boxes)
        {
            OBControl outline = box.members.get(0);
            box.sizeToMember(outline);
        }
    }

    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        setUpBoxes();
        doVisual(currentEvent());
        unlockScreen();
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

    public void setSceneXX(String scene)
    {
        deleteControls("obj.*");
        super.setSceneXX(scene);
        for (OBControl box : boxes)
            resetIntervalsForBox(box);
        setUpTargets();
        setDropBoxTags();

    }

    public void setScene1b()
    {

    }

    public void setScene1c()
    {
        setUpTargets();
        setDropBoxTags();
    }

    private void setUpTargets()
    {
        targets = filterControls("obj.*");
        for (OBControl obj : targets)
        {
            obj.setProperty("origpos",new PointF(obj.position().x,obj.position().y));
        }
        objectsLeftToPlace = new ArrayList<>(targets);

    }

    void nextObj() throws Exception
    {
        if (objectsLeftToPlace.isEmpty())
        {
            waitForSecs(0.5);
            gotItRightBigTick(true);
            clearDropBoxTags();
            nextScene();
        }
        else
            switchStatus(currentEvent());
    }

    boolean insertIntervalIntoIntervals(SortInterval interval,List<SortInterval>intervals)
    {
        for (int i = 0;i < intervals.size();i++)
        {
            SortInterval si = intervals.get(i);
            if (si.startx  >= interval.endx)
            {
                intervals.add(i,interval);
                return true;
            }
            if (si.endx > interval.startx)
                return false;
        }
        return false;
    }

    RectF frameForObject(OBControl obj)
    {
        float amt = applyGraphicScale(-4);
        RectF r = new RectF(obj.frame());
        r.inset(amt,0);
        return r;
    }

    void clearDropBoxTags()
    {
        for (OBControl box : filterControls("bigBox.*"))
            box.setProperty("tag","");
    }

    private void setDropBoxTags()
    {
        for (OBControl box : boxes)
        {
            for (OBControl target : filterControls("obj.*"))
            {
                PointF targPoint = (PointF) target.propertyValue("origpos");

                if (box.frame().contains(targPoint.x,targPoint.y))
                {
                    Map<String,Object> attrs = (Map<String, Object>) target.propertyValue("attrs");
                    String targetTag = (String) attrs.get("tag");
                    box.setProperty("tag",targetTag);
                    objectsLeftToPlace.remove(target);
                    List<SortInterval> intervals = (List<SortInterval>) box.propertyValue("intervals");
                    RectF objframe = frameForObject(target);
                    objframe.left -= box.frame().left;
                    objframe.right -= box.frame().left;
                    float y = box.frame().centerY();
                    target.setPosition(target.position().x,y);
                    SortInterval si = new SortInterval();
                    si.startx = objframe.left;
                    si.endx = objframe.right;
                    si.object = target;
                    insertIntervalIntoIntervals(si,intervals);
                }
            }
        }

    }

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }

    public void demo1a() throws Exception
    {
        demoButtons();
        PointF st = OB_Maths.locationForRect(1,1,new RectF(bounds()));
        PointF en = OB_Maths.locationForRect(0,0,new RectF(bounds()));
        //loadPointerStartPoint(st,en);
        //movePointerToPoint(en,1,true);
        waitForSecs(0.5);
        nextScene();
    }

    public void demo1b() throws Exception
    {
        PointF firstPt = objectDict.get("obj2").position();
        movePointerToPoint(firstPt,-25,-1,true);

        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.5);
        waitForSecs(0.4);

        OBControl secondPointBoxOne = objectDict.get("bigBox1");
        OBControl obj = objectDict.get("obj2");
        moveObjects(Arrays.asList(obj,thePointer), OB_Maths.locationForRect(0.7f,0.5f,secondPointBoxOne.frame()),-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.4);

        PointF thirdPt = objectDict.get("obj1").position();
        movePointerToPoint(thirdPt,-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);

        waitForSecs(0.5);

        movePointerToPoint(OB_Maths.locationForRect(0.7f,0.5f,secondPointBoxOne.frame()),-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",2,true);
        waitForSecs(0.5);
        waitForSecs(0.4);

        thePointer.hide();

        nextScene();
    }

    OBControl boxForPoint(PointF pt)
    {
        for (OBControl box : filterControls("bigBox.*"))
            if (box.frame().contains(pt.x,pt.y))
                return box;
        return null;
    }

    static float MoveRight(List<SortInterval>intervals,int startidx,float amt)
    {
        if (startidx == 0 || startidx == intervals.size() - 1)
            return 0;
        SortInterval thisSI = intervals.get(startidx);
        int nextidx = startidx + 1;
        SortInterval nextSI = intervals.get(nextidx);
        float diff = nextSI.startx - thisSI.endx;
        if (diff >= amt)
        {
            thisSI.startx += amt;
            thisSI.endx += amt;
            return amt;
        }
        float newAmt = MoveRight(intervals, nextidx, amt - diff);
        newAmt += diff;
        thisSI.startx += newAmt;
        thisSI.endx += newAmt;
        return newAmt;
    }

    static float MoveLeft(List<SortInterval>intervals,int startidx,float amt)
    {
        if (startidx == 0 || startidx == intervals.size() - 1)
            return 0;
        SortInterval thisSI = intervals.get(startidx);
        int previdx = startidx - 1;
        SortInterval prevSI = intervals.get(previdx);
        float diff = thisSI.startx - prevSI.endx;
        if (diff >= amt)
        {
            thisSI.startx -= amt;
            thisSI.endx -= amt;
            return amt;
        }
        float newAmt = MoveLeft(intervals, previdx, amt - diff);
        newAmt += diff;
        thisSI.startx -= newAmt;
        thisSI.endx -= newAmt;
        return newAmt;
    }

    static List<Integer> OverlappingIntervals(List<SortInterval>intervals,SortInterval interval)
{
    List<Integer> arr = new ArrayList<>();
    for (int i = 0;i < intervals.size();i++)
    {
        SortInterval si = intervals.get(i);
        if (si.startx >= interval.endx)
            break;
        if (si.endx > interval.startx)
            arr.add(i);
    }
    return arr;
}
    void placeObject(OBControl obj,OBControl box)
    {
        List<SortInterval> intervals = (List<SortInterval>) box.propertyValue("intervals");
        RectF objframe = frameForObject(obj);
        objframe.left -= box.frame().left;
        objframe.right -= box.frame().left;
        SortInterval si = new SortInterval();
        si.startx = objframe.left;
        si.endx = objframe.right;
        si.object = obj;
        if (si.startx < 0)
        {
            si.endx += -si.startx;
            si.startx = 0;
        }
        else
        {
            SortInterval si2 = intervals.get(intervals.size()-1);
            if (si.endx > si2.endx)
            {
                si.startx -= (si.endx - si2.endx);
                si.endx = si2.endx;
            }
        }
        List<Integer> oi = OverlappingIntervals(intervals, si);
        while (oi.size() > 0)
        {
            int idx = oi.get(oi.size()-1);
            SortInterval si2 = intervals.get(idx);
            if ((si2.startx + si2.endx)/2 > (si.startx + si.endx)/2)
            {
                float overlap = si.endx - si2.startx;
                float achieved = MoveRight(intervals, idx, overlap);
                if (achieved < overlap)
                {
                    float amt = overlap - achieved;
                    si.startx -= amt;
                    si.endx -= amt;
                }
            }
            else
            {
                float overlap = si2.endx - si.startx;
                float achieved = MoveLeft(intervals, idx, overlap);
                if (achieved == 0)
                {
                    float amt = si2.endx - si.startx;
                    si.startx += amt;
                    si.endx += amt;
                }
                else if (achieved < overlap)
                {
                    float amt = overlap - achieved;
                    si.startx += amt;
                    si.endx += amt;
                }
            }
            oi = OverlappingIntervals(intervals, si);
        }
        insertIntervalIntoIntervals(si,intervals);
        float y = box.frame().centerY();
        for (SortInterval sint : intervals)
        {
            if (sint.object != null)
            {
                float x = (sint.startx + sint.endx) / 2;
                x += box.frame().left;
                PointF pt = new PointF(x, y);
                if (!(pt.equals(sint.object.position())))
                {
                    final OBAnim runAnim = OBAnim.moveAnim(pt,sint.object);
                    final OBSectionController lthis = this;
                    OBAnimationGroup.runAnims(Collections.singletonList(runAnim),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,lthis);
                }
            }
        }
    }

    void vibrate(OBControl targ)
    {
        OBAnimationGroup ag = new OBAnimationGroup();
        PointF pt = targ.position(),pt1 = new PointF(pt.x,pt.y),pt2 = new PointF(pt.x,pt.y);
        pt = new PointF(pt.x,pt.y);
        pt1.x = pt.x + applyGraphicScale(5);
        pt2.x = pt.x - applyGraphicScale(5);
        List<OBAnim>l1 = Collections.singletonList(OBAnim.moveAnim(pt1,targ)),
                l2 = Collections.singletonList(OBAnim.moveAnim(pt2,targ));
        ag.chainAnimations(Arrays.asList(l1,l2),Arrays.asList(0.05f,0.05f),
                Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT),
                1,this
                );
        targ.setPosition(pt);
    }

    boolean tagAlreadyAssigned(String tag)
    {
        for (OBControl dropBox : filterControls("bigBox.*"))
        {
            String dropBoxTag = (String)dropBox.propertyValue("tag");

            if (tag.equals(dropBoxTag))
            {
                return true;
            }
        }
        return false;
    }

    void moveObjWithinContainer(OBControl box)
    {
        if (!box.frame().contains(target.frame()))
        {
            float boxYpos =  box.position().y;
            float targXpos = target.frame().left;
            OBAnim moveAnim = OBAnim.moveAnim(new PointF(targXpos,boxYpos),target);
            OBAnimationGroup.runAnims(Collections.singletonList(moveAnim),0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
    }

    void moveToOriginalPosition()
    {
        PointF targOriginalPoint = (PointF) target.propertyValue("origpos");
        moveObjects(Collections.singletonList(target),targOriginalPoint,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    public void checkDragTarget(OBControl targ,PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            if (!objectsLeftToPlace.contains(targ))
            {
                gotItWrongWithSfx();
                vibrate(targ);
                switchStatus(currentEvent());
                return;
            }
            super.checkDragTarget(targ,pt);
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
            OBControl box = boxForPoint(pt);
            if (box != null)
            {
                String boxTag = (String) box.propertyValue("tag");
                String targetTag = (String) target.attributes().get("tag");

                if (boxTag.equals(""))
                {
                    if (tagAlreadyAssigned(targetTag))
                    {
                        gotItWrongWithSfx();
                        moveToOriginalPosition();
                        switchStatus(currentEvent());
                        return;
                    }
                    else
                    {
                        box.setProperty("tag",targetTag);
                        boxTag = targetTag;
                    }
                }

                if (boxTag.equals(targetTag))
                {
                    gotItRightBigTick(false);
                    placeObject(target,box);
                    objectsLeftToPlace.remove(target);
                    nextObj();
                }
                else
                {
                    gotItWrongWithSfx();
                    moveToOriginalPosition();
                    switchStatus(currentEvent());
                }
            }
            else
            {
                moveToOriginalPosition();
                switchStatus(currentEvent());
            }
        }

        catch (Exception exception)
        {
        }
    }

    @Override
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
                        checkDragTarget(c,pt);
                        return null;
                    }
                }.execute();
            }
        }
    }

}


