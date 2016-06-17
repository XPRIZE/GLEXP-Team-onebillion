package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 24/01/16.
 */
public class X_Count20_S3 extends XPRZ_SectionController
{
    List<OBControl> numbers = new ArrayList<>();
    List<OBControl> boxes = new ArrayList<>();
    int highCol;
    public X_Count20_S3()
    {
        super();
    }

    void createBoxAndNumbers()
    {
        OBControl numberBox = objectDict.get("numberbox");
        highCol = OBUtils.colorFromRGBString(eventAttributes.get("highcol"));
        float textSize = Float.parseFloat(eventAttributes.get("smalltextsize"));
        textSize = MainActivity.mainActivity.applyGraphicScale(textSize);
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
        path.setLineWidth(3 * graphicScale());
        float y1 = minY,y2 = midY;
        for (int i = 0;i < 2;i++)
        {
            for (int j = 0;j < 10;j++)
            {
                float x1 = (float)Math.floor(minX + j * frame.width() / 10);
                float x2 = (float)Math.floor(minX + (j+1) * frame.width() / 10);
                OBControl box = new OBControl();
                box.setFrame(x1,y1,x2,y2);
                box.setBackgroundColor(Color.WHITE);
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
    }

    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        createBoxAndNumbers();
        float textSize = Float.parseFloat(eventAttributes.get("largetextsize"));
        textSize = MainActivity.mainActivity.applyGraphicScale(textSize);
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel txt = new OBLabel("20",tf,textSize);
        txt.setColour(Color.BLACK);
        OBControl textbox = objectDict.get("textbox");
        txt.setPosition(textbox.position());
        objectDict.put("label",txt);
        attachControl(txt);
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
        deleteControls("obj.*");
        super.setSceneXX(scene);
        String colstr = eventAttributes.get("col");
        List<OBControl> objs = filterControls("obj.*");
        if (colstr != null)
        {
            int col = OBUtils.colorFromRGBString(colstr);
            for (OBControl gp : objs)
                ((OBGroup)gp).substituteFillForAllMembers("col.*",col);
        }
        targetNo = objs.size();
        targets = numbers;
        OBLabel label = (OBLabel)objectDict.get("label");
        label.setString(String.format("%d", targetNo));
        label.hide();
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

    void highlightBoxNo(int i,boolean high)
    {
        OBControl box = boxes.get(i);
        if (high)
            box.setBackgroundColor(highCol);
        else
            box.setBackgroundColor(Color.WHITE);
    }

    public void demo3a() throws Exception
    {
        loadPointer(POINTER_MIDDLE);
        PointF restPt = OB_Maths.locationForRect(0, 2,objectDict.get("obj9").frame());
        movePointerToPoint(restPt, -1, true);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 0, true);
        waitForSecs(0.4);
        OBControl box = boxes.get(11);
        PointF numPt = OB_Maths.locationForRect(0.8f, 0.6f,convertRectFromControl(box.frame(),box.parent));
        movePointerToPoint(numPt,-45,-1,true);
        waitForSecs(0.2);
        playAudioQueuedScene("sfx","tap",false);
        highlightBoxNo(11,true);
        objectDict.get("label").show();
        waitAudio();
        highlightBoxNo(11,false);
        playAudioQueuedSceneIndex(currentEvent(), "DEMO", 1, true);
        waitForSecs(1);
        thePointer.hide();
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
            highlightBoxNo(idx, true);
            if (idx == targetNo - 1)
            {
                objectDict.get("label").show();
                playAudioQueuedScene("sfx", "tap", true);
                gotItRightBigTick(true);
                playAudioQueuedScene(currentEvent(), "CORRECT", true);
                highlightBoxNo(idx, false);
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                waitAudio();
                setReplayAudio(saveReplay);
                highlightBoxNo(idx, false);
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
