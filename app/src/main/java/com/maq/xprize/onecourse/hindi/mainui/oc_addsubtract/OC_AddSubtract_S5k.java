package com.maq.xprize.onecourse.hindi.mainui.oc_addsubtract;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 28/03/2017.
 */

public class OC_AddSubtract_S5k extends OC_SectionController
{
    int targetNumber;
    int highlightColour;
    Map<String, Integer> eventColours;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        Typeface typeface = OBUtils.standardTypeFace();
        for(OBControl con : filterControls("num_.*"))
        {
            ((OBPath)con).sizeToBoundingBoxIncludingStroke();
            float fontSize = 50 * con.height()/65.0f;
            OBLabel label = new OBLabel(String.format("%s", con.attributes().get("num")),typeface,fontSize);
            label.setColour(Color.BLACK);
            attachControl(label);
            label.setZPosition(4);
            label.setPosition(con.position());

        }

        for(OBControl con : filterControls("eq_box_.*"))
        {
            String[] eqParts = ((String)con.attributes().get("equation")).split(",");
            String equation = String.format("%s + %s = %s", eqParts[0], eqParts[1], eqParts[2]);
            String name = String.format("equation_%s",eqParts[0]);
            OC_Numberlines_Additions.loadEquation(equation,name,con,Color.BLACK,false,0,1,this);
            objectDict.get(name).hide();

        }
        eventColours = OBMisc.loadEventColours(this);

        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        setCircle(targetNumber, eventColours.get("highlight"));
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                startScene();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        targetNumber = OBUtils.getIntValue(eventAttributes.get("target"));

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,filterControls("num_.*"),pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget(targ);
                    }
                });




            }

        }

    }

    public void checkTarget(OBControl targ) throws Exception
    {
        if(Integer.valueOf((String)targ.attributes().get("num")) == targetNumber)
        {
            if(targetNumber == 6)
                movePreviousEquations();
            gotItRight();
            OBGroup equation = (OBGroup)objectDict.get(String.format("equation_%d",targetNumber));
            OC_Numberlines_Additions.colourEquation(equation,1,5,Color.RED,this);
            equation.show();
            playAudioQueuedScene("FINAL",0.3f,true);
            waitForSecs(0.3f);
            lockScreen();
            OC_Numberlines_Additions.colourEquation(equation,1,5,Color.BLACK,this);
            setCircle(targetNumber, eventColours.get("inactive"));
            if(targetNumber<10)
                setCircle(targetNumber+1, eventColours.get("highlight"));

            unlockScreen();
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            setStatus(STATUS_AWAITING_CLICK);

        }
    }

    public void setCircle(int num,int colour)
    {
        OBPath control = (OBPath)objectDict.get(String.format("num_%d",num));
        control.setFillColor(colour);

    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void movePreviousEquations() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(int i=1; i<6; i++)
        {
            OBGroup eq = (OBGroup)objectDict.get(String.format("equation_%d",i));
            PointF loc = OBMisc.copyPoint(eq.position());
            loc.x -= bounds().width();
            anims.add(OBAnim.moveAnim(loc,eq));

        }
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN,this);
    }

}
