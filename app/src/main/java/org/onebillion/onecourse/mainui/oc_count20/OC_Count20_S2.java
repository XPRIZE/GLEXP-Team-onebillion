package org.onebillion.onecourse.mainui.oc_count20;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBConfigManager;
import org.onebillion.onecourse.utils.OBRunnableSyncUI;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 20/01/16.
 */
public class OC_Count20_S2 extends OC_SectionController
{
    List<OBControl> numbers = new ArrayList<>();
    List<OBControl> boxes = new ArrayList<>();
    OBControl highlighter;
    public OC_Count20_S2 ()
    {
        super();
    }

    void createBoxAndNumbers()
    {
        OBControl numberBox = objectDict.get("numberbox");
        int col0 = OBUtils.colorFromRGBString(eventAttributes.get("col0"));
        int col1 = OBUtils.colorFromRGBString(eventAttributes.get("col1"));
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
        path.setStrokeColor(0xff8f8f8f);
        path.setLineWidth(4 * graphicScale());
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
            attachControl(txt);
        objectDict.put("numberbox", path);
        objectDict.put("gp", gp);
        highlighter = new OBControl();
        highlighter.borderColour = Color.BLACK;
        highlighter.borderWidth = 2 * 4 * graphicScale();
        //highlighter.invalOutdent = highlighter.borderWidth / 2;
        attachControl(highlighter);
        highlighter.hide();
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

    public void doAudio(String scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(scene, "PROMPT", false);
    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
    }

    void highlightSquare(int i)
    {
        if (i < 0)
            highlighter.hide();
        else
        {
            RectF f = boxes.get(i).frame();
            final RectF ff = convertRectFromControl(f,boxes.get(i).parent);
            new OBRunnableSyncUI(){public void ex()
            {
                highlighter.setFrame(ff);
                highlighter.show();
            }
            }.run();
        }
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        String tn = eventAttributes.get("targetno");
        if (tn != null)
        {
            int n = Integer.parseInt(tn);
            targetNo = n - 1;
        }
         targets = numbers;
    }

    public void demo2a() throws Exception
    {
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.5);
        for (int i = 1;i <= 20;i++)
        {
            highlightSquare(i-1);
            playAudioQueuedSceneIndex(currentEvent(),"DEMO",i,true);
            highlightSquare(-1);
            waitForSecs(0.2);
        }
        highlightSquare(-1);
        waitForSecs(0.7);
        nextScene();
    }

    public void checkTarget(OBControl targ)
    {
        int saveStatus = status();
        List<Object> saveReplay = emptyReplayAudio();
        setStatus(STATUS_CHECKING);
        try
        {
            int idx = targets.indexOf(targ);
            highlightSquare(idx);
            if (idx == targetNo)
            {
                displayTick();
                playAudioQueuedScene(currentEvent(), "CORRECT", true);
                highlightSquare(-1);
                waitForSecs(0.3f);
                nextScene();
            }
            else
            {
                playAudio("wrong");
                waitAudio();
                setReplayAudio(saveReplay);
                highlightSquare(-1);
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
                }.execute();

            }
        }

    }
}
