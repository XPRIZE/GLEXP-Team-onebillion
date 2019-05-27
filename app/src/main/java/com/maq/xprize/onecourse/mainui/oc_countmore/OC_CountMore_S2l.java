package com.maq.xprize.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 21/02/2017.
 */

public class OC_CountMore_S2l extends OC_SectionController
{
    Map<String, Integer> eventColour;
    String currentColour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2l");
        eventColour = new ArrayMap<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo2l();
            }
        });
    }


    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        if (eventAttributes.get("recolour") != null && eventAttributes.get("recolour").equals("true"))
        {
            for (String key : eventAttributes.keySet())
            {
                if (key.startsWith("colour_"))
                {
                    int col = OBUtils.colorFromRGBString(eventAttributes.get(key));
                    String name = key.substring(7);
                    eventColour.put(name, col);

                    OBGroup pot = (OBGroup) objectDict.get(name);
                    OBPath colourPath = (OBPath) pot.objectDict.get("colour");
                    colourPath.setFillColor(col);
                    pot.show();
                }

            }
        }
        hideAllSelects();
        currentColour = null;

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if (status() == STATUS_AWAITING_CLICK)
        {
            final int num = Integer.valueOf(eventAttributes.get("pot"));
            final OBControl pot = finger(0, 0, filterControls("pot_.*"), pt);
            if (pot != null && (currentColour == null || num == 0) && (currentColour == null || !currentColour.equalsIgnoreCase((String)pot.attributes().get("id"))))
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkPot((OBGroup)pot, num);
                    }
                });


            } else if (currentColour != null)
            {
                final OBGroup necklace = (OBGroup) objectDict.get("necklace");
                final OBControl bead = finger(0, 0, necklace.filterMembers("bead_.*"), pt);
                if (bead != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkBead((OBPath)bead, necklace);
                        }
                    });

                }

            }

        }



    }

    public void checkPot(OBGroup pot, int num) throws Exception
    {
        playAudio(null);
        if (num == 0 || pot == objectDict.get(String.format("pot_%d", num)))
        {
            if (num != 0)
                gotItRight();
            selectPot(pot);
            currentColour = (String)pot.attributes().get("id");
            if (getAudioForScene(currentEvent(), "PROMPT2.REPEAT") != null)
                setReplayAudio((List<Object>)(Object)getAudioForScene(currentEvent(), "PROMPT2.REPEAT"));
            setStatus(STATUS_AWAITING_CLICK);

        } else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if (time == statusTime)
                playAudioQueuedScene("INCORRECT", 0.3f, false);

        }
    }

    public void checkBead(OBPath bead, OBGroup necklace) throws Exception
    {
        playAudio(null);
        if (bead == necklace.objectDict.get(String.format("bead_%s", eventAttributes.get("bead"))))
        {
            lockScreen();
            bead.setFillColor(eventColour.get(currentColour));
            gotItRightBigTick(false);

            unlockScreen();
            waitSFX();
            waitForSecs(0.2f);
            if (currentEvent() == events.get(events.size() - 1))
            {
                lockScreen();
                hideControls("pot_.*");

                unlockScreen();
                displayTick();
                waitForSecs(0.3f);
                playAudioQueuedScene("FINAL", 0.3f, true);
                waitForSecs(0.5f);

            } else
            {
                waitForSecs(0.3f);

            }
            nextScene();

        } else
        {
            playSFX("wrong");
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if (time == statusTime)
                playAudioQueuedScene("INCORRECT2", 0.3f, false);

        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void selectPot(OBGroup pot) throws Exception
    {
        lockScreen();
        hideAllSelects();
        pot.objectDict.get("selector_frame").show();
        playSfxAudio("click",false);
        unlockScreen();

    }

    public void hideAllSelects()
    {
        for(OBControl con : filterControls("pot_.*"))
            ((OBGroup)con).objectDict.get("selector_frame").hide();

    }

    public void demo2l() throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBGroup necklace = (OBGroup)objectDict.get("necklace");
        OBPath bead = (OBPath)necklace.objectDict.get("bead_1");
        moveScenePointer(OB_Maths.locationForRect(0.75f,0.75f,objectDict.get("necklace").frame()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.05f,bead.getWorldFrame()),-15,0.7f,"DEMO",1,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.05f,objectDict.get("pot_4").frame()) ,-30,0.7f,true);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.7f,objectDict.get("pot_4").frame()) ,-30,0.2f,true);
        selectPot((OBGroup)objectDict.get("pot_4"));
        waitSFX();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.05f,objectDict.get("pot_4").frame()) ,-30,0.2f,true);
        movePointerToPoint(OB_Maths.locationForRect(0.7f,1.05f,bead.getWorldFrame())  ,-15,0.7f,true);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.6f,bead.getWorldFrame()) ,-15,0.2f,true);
        lockScreen();
        bead.setFillColor(eventColour.get("pot_4"));
        playSFX("correct");

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1.5f,bead.getWorldFrame()),-15,0.25f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        nextScene();

    }

    public void demo2m() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.3f);
        startScene();
    }

}
