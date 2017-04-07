package org.onebillion.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
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
 * Created by michal on 06/04/2017.
 */

public class OC_MoreNumbers_S3na extends OC_SectionController
{
    List<OBControl> eventTargets;
    int textcolour, hilitecolour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master3na");
        textcolour = OBUtils.colorFromRGBString(eventAttributes.get("textcolour"));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        eventTargets = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        OC_MoreNumbers_Additions.buttonSet(2, this);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3na();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        loadBoxes();
    }

    public void doMainXX() throws Exception
    {
        animateBoxes(true);
        startScene();
    }

    public void fin()
    {
        goToCard(OC_MoreNumbers_S3o.class, "event3");
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl cont = finger(0, 2, eventTargets, pt);
            if (cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkBox((OBGroup) cont);
                    }
                });

            } else if (finger(0, 2, Arrays.asList(objectDict.get("button_arrow")), pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkArrowButton();
                    }
                });

            }


        }
    }


    public void checkBox(OBGroup cont) throws Exception
    {
        OBControl box = cont.objectDict.get("box");
        setStatus(STATUS_BUSY);
        playSfxAudio("click",false);
        if(cont.isSelected())
        {
            cont.enable();
            box.setFillColor(Color.WHITE);
        }
        else
        {
            cont.select();
            box.setFillColor ( hilitecolour);
        }
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkArrowButton() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(1,this);
        boolean correct = true;
        for(OBControl cont : eventTargets)
        {
            if(cont.isSelected() != (boolean)cont.propertyValue("correct"))
                correct=false;
        }
        if(correct)
        {
            gotItRightBigTick(true);
            OC_MoreNumbers_Additions.buttonSet(2,this);
            animateBoxes(false);
            nextScene();
        }
        else
        {
            List<OBControl> wrongObjs = new ArrayList<>();
            lockScreen();
            for(OBControl cont: eventTargets)
            {
                if(cont.isSelected() && !(boolean)cont.propertyValue("correct"))
                {
                    wrongObjs.add(cont);
                    OBLabel label = (OBLabel)((OBGroup)cont).objectDict.get("label");
                    label.setColour(Color.RED);
                }
            }

            unlockScreen();
            gotItWrongWithSfx();
            waitSFX();
            lockScreen();
            for(OBControl cont : wrongObjs)
            {
                cont.enable();
                ((OBGroup)cont).objectDict.get("box").setFillColor(Color.WHITE);
                OBLabel label = (OBLabel)((OBGroup)cont).objectDict.get("label");
                label.setColour(textcolour);

            }
            OC_MoreNumbers_Additions.buttonSet(0,this);

            unlockScreen();
            playAudioQueuedScene("INCORRECT",300,false);
            setStatus(STATUS_AWAITING_CLICK);

        }

    }

    public void startScene() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(0,this);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void loadBoxes()
    {
        eventTargets.clear();
        for(OBControl control : filterControls("box_.*"))
        {

            float fontSize = 58*control.height()/62.0f;
            int val = OBUtils.getIntValue((String)control.attributes().get("num"));
            boolean correct = OBUtils.getBooleanValue((String)control.attributes().get("correct"));
            OBLabel numLabel = new OBLabel(String.format("%d",val),OBUtils.standardTypeFace(),fontSize);
            control.setZPosition(1);
            numLabel.setZPosition(2);
            numLabel.setColour(textcolour);
            numLabel.setPosition(OBMisc.copyPoint(control.position()));
            ((OBPath)control).sizeToBoundingBoxIncludingStroke();
            control.show();

            OBGroup group = new OBGroup(Arrays.asList(control,numLabel));
            group.setProperty("correct",correct);
            group.setProperty("destloc",OBMisc.copyPoint(group.position()));
            group.objectDict.put("box",control);
            group.objectDict.put("label",numLabel);
            group.setPosition(OB_Maths.locationForRect(-0.3f,0.5f,this.bounds()));
            eventTargets.add(group);
            attachControl(group);
            group.enable();

        }
    }

    public void animateBoxes(boolean boxesIn) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        float dist = 0.3f * this.bounds().height();
        for(OBControl group : eventTargets)
        {
            PointF startPoint = OBMisc.copyPoint(group.position());
            PointF endPoint = boxesIn ? (PointF)group.propertyValue("destloc") : OB_Maths.locationForRect(1.3f,0.5f,this.bounds());
            Path path = new Path();
            path.moveTo(startPoint.x, startPoint.y);
            path.cubicTo(startPoint.x, startPoint.y - dist,endPoint.x, endPoint.y - dist, endPoint.x,endPoint.y);
            anims.add(OBAnim.pathMoveAnim(group,path,false,0));
        }
        playSfxAudio(boxesIn ? "boxes_on" : "boxes_off", false);
        OBAnimationGroup.runAnims(anims,1,true,boxesIn ? OBAnim.ANIM_EASE_OUT : OBAnim.ANIM_EASE_IN,this);
        waitSFX();
    }


    public void demo3na() throws Exception
    {
        objectDict.get("button_arrow").show();
        playSfxAudio("button_appear",true);
        waitForSecs(0.5f);
        animateBoxes(true);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.85f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.95f,this.bounds()),-15,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("button_arrow").frame()),-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.2f);
        startScene();
    }


}
