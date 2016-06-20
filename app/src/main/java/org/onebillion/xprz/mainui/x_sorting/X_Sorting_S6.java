package org.onebillion.xprz.mainui.x_sorting;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 23/04/16.
 */
public class X_Sorting_S6 extends XPRZ_SectionController
{

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
        deleteControls("obj.*");
        clearDropBoxTags();
        super.setSceneXX(scene);
        targets = filterControls("obja.*");
        setUpDragObjectsForEvent(scene + "-2");
        setDropBoxTags();
        invalidateView(0, 0, bounds().width(),bounds().height());
    }

    public void setScene6b()
    {

    }
    void setUpDragObjectsForEvent(String ev)
    {
        Map<String,Object>event = (Map<String, Object>) eventsDict.get(ev);
        Map<String,Object> objectsdict = (Map<String, Object>) event.get("objectsdict");

        for (String k : objectsdict.keySet())
        {
            OBControl c = objectDict.get(k);
            PointF pt = c.position();
            c.setProperty("origpos",new PointF(pt.x,pt.y));

            Map<String,Object> target = (Map<String, Object>) objectsdict.get(k);
            Map<String,String> attrs = (Map<String, String>) target.get("attrs");
            PointF relpt = OBUtils.pointFromString(attrs.get("pos"));

            c.setProperty("destpos",new PointF(relpt.x,relpt.y));
        }
    }

    void demo6a() throws Exception
    {
        RectF r = new RectF(0,0,right(),bottom());
        PointF restPt = OB_Maths.locationForRect(0.6f,0.5f,r);
        PointF startPt = OB_Maths.locationForRect(0.6f,1.01f,r);
        loadPointerStartPoint(startPt,restPt);
        theMoveSpeed = (int)r.width();
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);

        PointF firstPt = objectDict.get("obj1").position();
        movePointerToPoint(firstPt,-25,-1,true);

        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.5f);
        waitForSecs(0.4f);

        PointF secondPt = OB_Maths.locationForRect(0.5f, 0.7f,objectDict.get("obja1").frame());
        movePointerToPoint(secondPt,-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",2,true);
        waitForSecs(0.5f);

        OBControl box = objectDict.get("bigBox1");
        OBControl obj = objectDict.get("obja1");
        PointF pt = (PointF) obj.propertyValue("destpos");
        PointF destpt = OB_Maths.locationForRect(pt.x,pt.y,box.frame());
        List<OBControl>ar = new ArrayList<>();
        ar.add(obj);
        ar.add(thePointer);
        moveObjects(ar,destpt,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",3,true);
        waitForSecs(0.4f);
        targets.remove(obj);

        thePointer.hide();
        nextScene();
    }


    void nextObj() throws Exception
    {
        if (targets.size() == 0)
        {
            waitForSecs(0.1f);
            waitAudio();
            waitForSecs(0.5f);
            gotItRightBigTick(true);
            nextScene();
        }
        else
            switchStatus(currentEvent());
    }

    void clearDropBoxTags()
    {
        for (OBControl box : filterControls("bigBox.*"))
        {
            box.setProperty("tag","");
        }
    }

    void setDropBoxTags()
    {
        for (OBControl box : filterControls("bigBox.*"))
        {
            for (OBControl target : filterControls("obj.*"))
            {
                PointF pt = target.position();
                if (box.frame().contains(pt.x,pt.y))
                {
                    String targetTag = (String) target.attributes().get("tag");
                    box.setProperty("tag",targetTag);
                    targets.remove(target);
                }
            }
        }
    }

    void animateSelected(OBControl targ)
    {
        OBAnimationGroup grp = new OBAnimationGroup();
        float amt = MainActivity.mainActivity.applyGraphicScale(10);
        PointF pt = new PointF(targ.position().x,targ.position().y);
        PointF rpt = OB_Maths.OffsetPoint(targ.position(),amt,0);
        PointF lpt = OB_Maths.OffsetPoint(targ.position(),-amt,0);
        List<List<OBAnim>> arr = new ArrayList<>();
        arr.add(Arrays.asList(OBAnim.moveAnim(rpt,targ)));
        arr.add(Arrays.asList(OBAnim.moveAnim(lpt,targ)));
        arr.add(Arrays.asList(OBAnim.moveAnim(rpt,targ)));
        arr.add(Arrays.asList(OBAnim.moveAnim(lpt,targ)));
        arr.add(Arrays.asList(OBAnim.moveAnim(pt,targ)));
        Float durations[] = {0.025f,0.05f,0.05f,0.05f,0.025f};
        Integer timingFs[] = {OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT};
        grp.chainAnimations(arr,Arrays.asList(durations),Arrays.asList(timingFs),1,this);

    }
    boolean tagAlreadyAssigned(String tag)
    {
        for (OBControl dropBox : filterControls("bigBox.*"))
        {
            String dropBoxTag =String.format("%s",dropBox.propertyValue("tag"));

            if (tag.equals(dropBoxTag))
            {
                return true;
            }
        }
        return false;
    }

    OBControl chosenBox(PointF pt)
    {
        for (OBControl box : filterControls("bigBox.*"))
        {
            if (box.frame().contains(pt.x,pt.y))
                return box;
        }
        return null;
    }

    boolean objectsLeftWithTag(String tag)
    {
        for (OBControl targ : targets)
        {
            Map<String,Object> attrs = (Map<String, Object>) targ.propertyValue("attrs");

            String targTag = (String) attrs.get("tag");

            if (tag.equals(targTag))
            {
                return true;
            }
        }
        return false;
    }

    void playCorrectAudio(String targetTag) throws Exception
    {
        List<String> aud =  currentAudio("FINAL");
        String as[] = {"c","b","a"};
        int idx = Arrays.asList(as).indexOf(targetTag);
        idx = aud.size() - 1 - idx;
        if (idx >= 0)
        {
            waitForSecs(0.4f);
            playAudioQueued(Arrays.asList((Object)aud.get(idx)),false);
        }
    }

    void moveToOriginalPosition()
    {
        PointF targOriginalPoint = (PointF) target.propertyValue("origpos");
        moveObjects(Arrays.asList(target),targOriginalPoint,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
        target.setZPosition(target.zPosition() - 30);
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBControl box = chosenBox(pt);
            if (box != null)
            {
                String boxTag = String.format("%s",box.propertyValue("tag"));
                Map<String,Object> attrs = (Map<String, Object>) target.propertyValue("attrs");
                String targetTag = (String) attrs.get("tag");

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
                    }
                }

                boxTag = (String) box.propertyValue("tag");

                if (boxTag.equals(targetTag))
                {
                    gotItRightBigTick(false);
                    targets.remove(target);
                    PointF pta = (PointF) target.propertyValue("destpos");
                    PointF destpt = OB_Maths.locationForRect(pta.x,pta.y,box.frame());
                    moveObjects(Arrays.asList(target),destpt,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                    target.setZPosition(target.zPosition() - 30);
                    if (!objectsLeftWithTag(targetTag))
                        playCorrectAudio(targetTag);
                    nextObj();
            }
            else
            {
                gotItWrongWithSfx();
                moveToOriginalPosition();
            }
            switchStatus(currentEvent());
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


    public void touchUpAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            new AsyncTask<Void, Void, Void>()
            {
                protected Void doInBackground(Void... params)
                {
                    checkDragAtPoint(pt);
                    return null;
                }
            }.execute();
        }
    }
    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {
        for (OBControl box : filterControls("bigBox.*"))
        {
            if (box.frame().contains(pt.x, pt.y))
            {
                for (OBControl targ : filterControls("obj.*"))
                    if (targ.frame().contains(pt.x,pt.y))
                    {
                        gotItWrongWithSfx();
                        animateSelected(targ);
                        break;
                    }
                break;
            }
        }
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
