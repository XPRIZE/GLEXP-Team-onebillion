package org.onebillion.onecourse.mainui.oc_2dshapes;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 05/03/2018.
 */

public class OC_2dShapes_S1 extends OC_SectionController
{
    List<OBControl> targets;
    int currentIndex;
    boolean objMode;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        targets = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }


    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1b();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        targets.clear();
        objMode = eventAttributes.get("target").equals("obj");


        if (objMode)
        {
            String[] correct = eventAttributes.get("correct").split(",");
            for (String num : correct)
                targets.add(objectDict.get(String.format("obj_%s", num)));
        }
        else
        {
            targets.addAll(filterControls("obj_.*"));
        }

        for(OBControl obj : filterControls("obj_.*"))
        {
            if(obj instanceof OBPath &&  obj.propertyValue("fixed") == null)
            {
                ((OBPath) obj).sizeToBoundingBoxIncludingStroke();
                obj.setProperty("fixed", true);
            }
        }
        currentIndex = 0;
    }

    public void doMainXX() throws Exception
    {
        if (objMode)
            animateShapes();
        startScene();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
             goToCard(OC_2dShapes_S1k.class, "event1");
        } catch (Exception e)
        {
        }
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl cont = finger(0, 0, filterControls("obj_.*"), pt);
            if (cont != null && (cont.isEnabled() || !objMode))
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget((OBPath) cont);
                    }
                });
            }
        }
    }

    public void checkTarget(OBPath cont) throws Exception
    {
        setStatus(STATUS_BUSY);
        int fill = cont.fillColor();
        cont.setFillColor(OBUtils.highlightedColour(fill));
        if (targets.contains(cont))
        {
            cont.setProperty("start_colour", fill);
            if (cont.isEnabled())
                currentIndex++;
            cont.disable();
            if (!objMode)
            {
                int audio = OBUtils.getIntValue((String) cont.attributes().get("audio"));
                playAudioScene("FINAL", audio, true);
                lockScreen();
                cont.setFillColor(fill);
                cont.setOpacity(0.5f);
                unlockScreen();
            }

            if (targets.size() == currentIndex)
            {
                if (!objMode)
                {
                    waitForSecs(0.5f);
                } else
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    playAudioQueuedScene("FINAL", 300, true);
                    waitForSecs(0.3f);
                }

                lockScreen();

                for (OBControl obj : targets)
                {
                    obj.setFillColor((int) obj.propertyValue("start_colour"));
                    obj.enable();
                    obj.setOpacity(1);
                }

                unlockScreen();

                waitForSecs(0.3f);
                nextScene();
            }
            else
            {
                if (objMode)
                    gotItRightBigTick(false);
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            cont.setFillColor(fill);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void animateShapes() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl control : filterControls("loc_.*"))
        {
            OBControl obj = objectDict.get(control.attributes().get("obj"));
            obj.enable();
            anims.add(OBAnim.moveAnim(control.position(),obj));
            if(control.attributes().get("rotation") != null)
                anims.add(OBAnim.rotationAnim((float)Math.toRadians(Float.valueOf((String)control.attributes().get("rotation"))) ,obj));
            if(control.attributes().get("scale") != null)
                anims.add(OBAnim.scaleAnim(Float.valueOf((String)control.attributes().get("scale")),obj));
        }
        playSfxAudio("magic_harp",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
    }

    public void demo1b() throws Exception
    {
        demoButtons();
        playAudioScene("DEMO",0,false);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.85f,this.bounds()),-10,0.5f,true);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

}
