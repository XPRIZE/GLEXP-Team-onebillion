package com.maq.xprize.onecourse.mainui.oc_count20;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBConfigManager;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 24/08/16.
 */
public class OC_Count20_S6 extends OC_SectionController
{
    List<OBControl> numbers = new ArrayList<>();
    List<OBControl> boxes = new ArrayList<>();
    public OC_Count20_S6 ()
    {
        super();
    }

    void createBoxAndNumbers()
    {
        OBControl numberBox = objectDict.get("numberbox");
        int col0 = OBUtils.colorFromRGBString(eventAttributes.get("col0"));
        int col1 = OBUtils.colorFromRGBString(eventAttributes.get("col1"));
        int colline = OBUtils.colorFromRGBString(eventAttributes.get("colline"));
        float textSize = Float.parseFloat(eventAttributes.get("textsize"));
        textSize = OBConfigManager.sharedManager.applyGraphicScale(textSize);
        Typeface tf = OBUtils.standardTypeFace();
        RectF frame = numberBox.frame();
        float minX = frame.left;
        float minY = frame.top;
        float midY = (frame.top + frame.bottom) / 2;
        float maxY = frame.bottom;
        Path p = new Path();
        p.addRect(frame, Path.Direction.CCW);
        p.moveTo(minX, midY);
        p.lineTo(frame.right, midY);
        for (int i = 1; i < 10; i++)
        {
            float x = (float)Math.floor(minX + i * frame.width() / 10);
            p.moveTo(x, minY);
            p.lineTo(x, maxY);
        }
        OBPath path = new OBPath(p);
        path.sizeToBoundingBox();
        path.setFillColor(0);
        path.setStrokeColor(colline);
        path.setLineWidth(2 * graphicScale());
        float y1 = minY,y2 = midY;
        int cols[] = {col0,col1};
        for (int i = 0;i < 2;i++)
        {
            for (int j = 0;j < 10;j++)
            {
                float x1 = (float)Math.floor(minX + j * frame.width() / 10);
                float x2 = (float)Math.floor(minX + (j+1) * frame.width() / 10);
                OBControl box = new OBControl();
                box.setFrame(x1,y1,x2,y2);
                box.setBackgroundColor(cols[j % 2]);
                boxes.add(box);
                int n = i * 10 + j + 1;
                OBLabel txt = new OBLabel(String.format("%d",n),tf,textSize);
                txt.setColour(Color.BLACK);
                txt.setPosition(box.position());
                numbers.add(txt);
            }
            y1 = y2;
            y2 = maxY;
        }
        OBGroup gp = new OBGroup(boxes);
        attachControl(gp);
        attachControl(path);
        for (OBControl txt : numbers)
        {
            attachControl(txt);
            PointF pt = new PointF();
            pt.set(txt.position());
            txt.setProperty("origpos",pt);
        }
        objectDict.put("numberbox", path);
        objectDict.put("gp", gp);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = eventAttributes.get("scenes").split(",");
        events = Arrays.asList(eva);
        createBoxAndNumbers();
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

    public void setSceneXX(String scene)
    {
        deleteControls("pos.*");
        super.setSceneXX(scene);
        String[] strs = eventAttributes.get("targetno").split(",");
        List arr = new ArrayList<>();
        int i = 1;
        for(String str : strs)
        {
            int n = Integer.parseInt(str);
            OBLabel txt = (OBLabel) numbers.get(n-1);
            OBControl pos = objectDict.get(String.format("pos%d",i));
            txt.setPosition(OB_Maths.RoundPoint(pos.position()));
            PointF npt = new PointF();
            npt.set(txt.position());
            txt.setProperty("predragpos",npt);
            arr.add(txt);
            i++;
        }
        targets = arr;
        deleteControls("pos.*");
    }

    public void demo6a() throws Exception
    {
        OBControl cont = targets.get(0);
        OBControl box = boxes.get(11);
        PointF destPoint = OB_Maths.locationForRect(0.5f, 0.7f, convertRectFromControl(box.frame,box.parent));
        PointF startpt = new PointF(destPoint.x,boundsf().height() + 2);
        loadPointerStartPoint(startpt,destPoint);
        movePointerToPoint(destPoint,-1,true);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.4f);

        playAudioScene("DEMO",1,true);
        waitForSecs(0.2f);
        movePointerToPoint(cont.position(),30,0.8f,true);
        waitForSecs(0.3f);
        movePointerToPointWithObject(cont,(PointF)cont.propertyValue("origpos"),0,0.8f,true);
        waitForSecs(0.3f);
        movePointerForwards(applyGraphicScale(-60),0.3f);

        waitForSecs(0.7f);
        thePointer.hide();
        waitForSecs(0.7f);
        nextScene();
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


    public void nextObj() throws Exception
    {
        currNo += 1;
        if(targets.size()  == 0)
        {
            if(!performSel("endOfScene",currentEvent()) )
            {
                waitAudio();
                gotItRightBigTick(true);
                waitForSecs(0.5f);
                nextScene();
            }
        }
        else
        {
            switchStatus(currentEvent());
        }
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            int idx =numbers.indexOf(target);
            OBControl dropbox = boxes.get(idx);
            RectF f = convertRectFromControl(dropbox.frame(),dropbox.parent);
            if(f.contains( pt.x, pt.y) || f.contains(target.position().x, target.position().y))
            {
                PointF targPoint = (PointF) target.propertyValue("origpos");
                moveObjects(Arrays.asList(target),targPoint,-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
                gotItRightBigTick(false);
                target.setZPosition(10);
                targets.remove(target);
                nextObj();
                return;
            }
            OBControl numberBox = objectDict.get("numberbox");
            f = numberBox.frame;
            if(f.contains( pt.x, pt.y) || f.contains(target.position().x, target.position().y))
                gotItWrongWithSfx();
            OBControl targ = target;
            PointF destpt = (PointF) targ.propertyValue("predragpos");
            moveObjects(Arrays.asList(targ),destpt,-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
            targ.setZPosition(10);
            switchStatus(currentEvent());
        }
        catch(Exception exception)
        {
        }
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_WAITING_FOR_DRAG)
        {
            target =  findTarget(pt);
            if(target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkDragTarget(target,pt);
                    }
                });
            }
        }
    }


}

