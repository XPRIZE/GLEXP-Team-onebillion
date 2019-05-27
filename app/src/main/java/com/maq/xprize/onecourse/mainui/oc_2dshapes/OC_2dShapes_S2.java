package com.maq.xprize.onecourse.mainui.oc_2dshapes;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 06/03/2018.
 */

public class OC_2dShapes_S2 extends OC_SectionController
{
    List<OBPath> correct;
    int currentIndex;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        correct = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo2a();
            }
        });
    }


    public void setSceneXX(String scene)
    {
        correct.clear();
        if(OBUtils.getBooleanValue(eventAttributes.get("delete")))
        {
            deleteControls("line_.*");

        }
        super.setSceneXX(scene);

        for(OBControl control : filterControls("line_.*"))
        {
            control.setShadowOpacity(0);
            control.enable();
            ((OBPath)control).sizeToBoundingBoxInset(control.lineWidth()*-4);

        }


        if(eventAttributes.get("correct") != null)
        {
            for (String num : eventAttributes.get("correct").split(","))
            {
                OBPath cont = (OBPath) objectDict.get(String.format("line_%s", num));
                correct.add(cont);
            }
        }
        currentIndex=0;
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            goToCard(OC_2dShapes_S2f.class, "event2");
        }catch (Exception e)
        {
        }
    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,filterControls("line_.*"),pt);
            if(targ != null && targ.isEnabled())
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget((OBPath) targ);
                    }
                });
            }
        }
    }

    public void checkTarget(OBPath targ) throws Exception
    {
        playAudio(null);
        targ.setShadowOpacity(1);
        if(correct.contains(targ))
        {
            targ.disable();
            currentIndex++;
            gotItRight();
            if(currentIndex == correct.size())
            {
                displayTick();
                waitForSecs(0.8f);
                nextScene();
            }
            else
            {
                playAudio("correct");
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            targ.setShadowOpacity(0);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void startScene() throws Exception
    {
       OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK), this);
    }


    public void showLines() throws Exception
    {
        for(int i=1; i<= filterControls("line_.*").size(); i++)
        {
            lockScreen();
            objectDict.get(String.format("line_%d",i)).show();
            playSfxAudio(String.format("note_%d",i),false);
            unlockScreen();
            waitForSecs(0.4f);
        }
        waitForSecs(0.3f);
    }

    public void demo2a() throws Exception
    {
        showLines();
        playAudioScene("DEMO",0,true);
        loadPointer(POINTER_LEFT);
        PointF loc = OBMisc.copyPoint(objectDict.get("line_2").position());
        loc.y += applyGraphicScale(50);
        moveScenePointer(loc,-15,0.5f,"DEMO",1,0.3f);
        loc = OBMisc.copyPoint(objectDict.get("line_1").position());
        loc.y += applyGraphicScale(50);
        moveScenePointer(loc,-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demo2d() throws Exception
    {
        showLines();
        startScene();
    }

    public void demo2e() throws Exception
    {
        showLines();
        startScene();
    }
}
