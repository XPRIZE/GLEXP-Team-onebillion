package org.onebillion.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 21/02/2017.
 */

public class OC_CountMore_S2 extends OC_SectionController
{
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        for (OBControl con : filterControls("house_.*"))
        {
            OBGroup control = (OBGroup) con;
            OBPath path = (OBPath) control.objectDict.get("background");
            path.setFillColor(OBUtils.colorFromRGBString((String) control.attributes().get("colour")));
            OBMisc.insertLabelIntoGroup(control, Integer.valueOf((String) control.attributes().get("num")), 45.0f * control.height() / 105.0f,
                    Color.BLACK, control.objectDict.get("door").getWorldPosition(), this);
            control.objectDict.get("number").hide();
            control.highlight();
            control.lowlight();
        }

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
        super.setSceneXX(scene);

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
            goToCard(OC_CountMore_S2l.class, "event2");
        } catch (Exception e)
        {

        }
    }

    public float graphicScale()
    {
        return bounds().width() / 1024;
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl house = finger(-1, 0, filterControls("house_.*"), pt);
            if (house != null)
            {
                setStatus(STATUS_BUSY);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget((OBGroup) house);

                    }

                });
            }

        }


    }

    public void checkTarget(OBGroup house) throws Exception
    {
        playAudio(null);
        if (house == objectDict.get(String.format("house_%s", eventAttributes.get("correct"))))
        {
            gotItRight();
            showHouseNum(house);
            nextScene();

        }
        else
        {
            house.highlight();
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            house.lowlight();
            if (time == statusTime)
                playAudioQueuedScene("INCORRECT", 0.3f, false);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void showHouseNum(OBGroup house) throws Exception
    {
        lockScreen();
        house.highlight();
        house.objectDict.get("number").show();
        playSfxAudio("choosehouse",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        house.lowlight();

    }

    public void demo2a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.2f,objectDict.get("road").frame()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.05f,objectDict.get("house_1").frame()),-15,0.5f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.7f,objectDict.get("house_1").frame()) ,-15,0.2f,true);
        showHouseNum((OBGroup)objectDict.get("house_1"));
        moveScenePointer(OB_Maths.locationForRect(0.5f,1.05f,objectDict.get("house_1").frame()),-15,0.2f,"DEMO",2,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,2.5f,objectDict.get("house_1").frame()),-15,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        nextScene();

    }
    public void demo2b() throws Exception
    {
        playAudioQueuedScene("DEMO",300,true);
        waitForSecs(0.3f);
        startScene();

    }
    public void demo2k() throws Exception
    {
        displayTick();
        waitForSecs(0.3f);
        OBGroup house =null;
        for(int i=1;
            i<11;
            i++)
        {
            house = (OBGroup)objectDict.get(String.format("house_%d",i));
            house.highlight();
            playAudioScene("DEMO",i-1,true);
            house.lowlight();

        }
        for(int i=1;
            i<3;
            i++)
        {
            waitForSecs(0.2f);
            house.highlight();
            waitForSecs(0.2f);
            house.lowlight();

        }
        waitForSecs(0.5f);
        nextScene();

    }



}
