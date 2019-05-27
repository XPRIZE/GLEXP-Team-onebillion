package com.maq.xprize.onecourse.mainui.oc_count20;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBConfigManager;
import com.maq.xprize.onecourse.utils.OB_Maths;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 12/12/15.
 */
public class OC_Count20_S1 extends OC_SectionController
{
    public OC_Count20_S1 ()
    {
        super();
    }
    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        float textSize = Float.parseFloat(eventAttributes.get("textsize"));
        textSize = OBConfigManager.sharedManager.applyGraphicScale(textSize);
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel txt = new OBLabel("20",tf,textSize);
        txt.setColour(Color.BLACK);
        OBControl textbox = objectDict.get("textbox");
        txt.setPosition(textbox.position());
        objectDict.put("label", txt);
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
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
        }});
    }

    public void setScene1a()
    {
        setSceneXX(currentEvent());
        for (OBControl c : filterControls("obj.*"))
            c.texturise(true,this);
        OBLabel txt = (OBLabel)objectDict.get("label");
        txt.setString("10");
        txt.texturise(false,this);
        //txt.borderWidth = 1;
        //txt.borderColour = 0xFF000000;
        currNo = 11;
    }

    public void setScene1b()
    {
        targets = Collections.singletonList(objectDict.get("dottedline"));
    }

    public void setScene1c()
    {

    }

    public void setScene1d()
    {
        deleteControls("obj.*");
        setSceneXX(currentEvent());
        OBLabel txt = (OBLabel)objectDict.get("label");
        txt.setString("10");
        currNo = 11;
    }

    public void demo1a() throws Exception
    {
        demoButtons();
        PointF restPt = objectDict.get("obj15").position();
        movePointerToPoint(restPt, -25, 0.6f, true);
        List<String>audio = ((Map<String,List<String>>)audioScenes.get(currentEvent())).get("DEMO");
        playAudioQueued(Collections.singletonList((Object) audio.get(0)), true);
        waitForSecs(0.5);
        for (int i = 1;i <= 10;i++)
        {
            final OBControl counter = objectDict.get("obj"+i);
            counter.highlight();
            playAudioQueued(Collections.singletonList((Object) audio.get(i)), true);
            counter.lowlight();
            waitForSecs(0.2);
        }
        waitForSecs(0.4);
        final OBControl dash = objectDict.get("dottedline");
        final OBControl counter = objectDict.get("obj11");
        playAudioQueuedScene("sfx", "tap", true);
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {

            }
        });

        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                dash.setPosition(counter.bottomPoint());
                dash.show();
                invalidateControl(dash);
            }
        });
        waitAudio();
        movePointerToPoint(OB_Maths.locationForRect(1.1f, 1.1f, dash.frame()), -35, 0.6f, true);
        playAudioQueuedScene(currentEvent(), "DEMO2", true);
        waitForSecs(0.4);
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                thePointer.hide();
                invalidateControl(thePointer);
            }
        });
        nextScene();
    }

    public void demoXX() throws Exception
    {
        playAudioQueuedScene(currentEvent(), "DEMO", true);
        waitForSecs(0.3);
        for (int i = 1;i <= 20;i++)
        {
            final OBControl obj = objectDict.get(String.format("obj%d",i));


            OBUtils.runOnMainThread(new OBUtils.RunLambda() {
                @Override
                public void run() throws Exception {
                obj.highlight();
            }});
            playAudioScene(currentEvent(), "DEMO2", i - 1);
            waitAudio();
            OBUtils.runOnMainThread(new OBUtils.RunLambda() {
                @Override
                public void run() throws Exception {
                obj.lowlight();
            }});
            waitForSecs(0.2);

        }
        waitForSecs(0.4);
        displayTick();
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

    public void doMain1d() throws Exception
    {
        doMainXX();
        final OBControl obj = objectDict.get("obj11");
        final OBControl dash = objectDict.get("dottedline");
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
            dash.setPosition(obj.bottomPoint());
            dash.show();
            invalidateControl(dash);
        }});
    }
    public void doMain1c() throws Exception
    {
        demoXX();
    }

    public void doMain1e() throws Exception
    {
        demoXX();
    }

    public void nextObj() throws Exception
    {
        currNo ++;
        waitAudio();
        if (currNo > 20)
        {
            if (!performSel("endOfScene",currentEvent()))
            {
                waitForSecs(0.2);
                waitAudio();
                waitForSecs(0.5);
                nextScene();
            }
        }
        else
        {
            switchStatus(currentEvent());
        }
    }


    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            final OBControl currcounter = objectDict.get(String.format("obj%d",currNo));
            final OBControl nextcounter = objectDict.get(String.format("obj%d",currNo+1));
            final OBControl dash = objectDict.get("dottedline");
            final OBLabel txt = (OBLabel)objectDict.get("label");
            OBUtils.runOnMainThread(new OBUtils.RunLambda() {
                @Override
                public void run() throws Exception {
                    if (currcounter != null)
                        currcounter.show();
                    invalidateControl(currcounter);
                    if (nextcounter != null)
                        dash.setPosition(nextcounter.bottomPoint());
                    else
                        dash.hide();
                    invalidateControl(dash);
                    txt.setString(String.format("%d", currNo));
                    invalidateControl(txt);
            }});
            String audFeedback = StrAndNo("CORRECT",currNo - 10);
            playAudioQueuedScene(currentEvent(),audFeedback,false);
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

    @Override
    public void touchDownAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl c = findTarget(pt);
            if (c != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
                    @Override
                    public void run() throws Exception {
                        checkTarget(c);
                    }});

            }
        }

    }
}
