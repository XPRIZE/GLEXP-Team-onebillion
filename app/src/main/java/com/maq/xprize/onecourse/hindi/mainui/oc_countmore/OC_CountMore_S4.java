package com.maq.xprize.onecourse.hindi.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimParabola;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 15/03/2017.
 */

public class OC_CountMore_S4 extends OC_SectionController
{
    List<OBControl> boxes;
    List<OBControl> numbers;
    int currentNum;
    int hilitecolour;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        loadNumbers();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("colour_highlight"));
        setSceneXX(currentEvent());
        currentNum = 9;
        OBPath line = (OBPath)objectDict.get("dot_line");
        line.sizeToBoundingBoxIncludingStroke();
        RectF frame = line.frame();
        frame.inset(applyGraphicScale(2), 0);
        line.setFrame(frame);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo4a();
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
            goToCard(OC_CountMore_S4h.class, "event4");
        } catch (Exception e)
        {

        }

    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl num = finger(0, 1, numbers, pt);
            if (num != null)
            {
                setStatus(STATUS_BUSY);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkNumber((OBLabel)num);
                    }
                });
            }
        }
    }

    public void checkNumber(OBLabel num) throws Exception
    {
        playAudio(null);
        if (num == numbers.get(currentNum))
        {
            flyNumberHome(num);
            gotItRight();

            if (currentNum < 0)
            {
                waitForSecs(0.3f);
                displayTick();
                waitForSecs(0.3f);
                nextScene();
            }
            else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            if (time == statusTime)
                playAudioScene("INCORRECT", 8 - currentNum, false);
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void flyNumberHome(OBLabel label)
    {
        playAudio("correct");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)label.propertyValue("start_loc"),label))
                ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        currentNum--;
        jumpLine();

    }

    public void jumpLine()
    {
        OBControl line = objectDict.get("dot_line");
        if(currentNum >= 0)
        {
            line.setZPosition(2);
            line.setPosition(OB_Maths.locationForRect(0.5f,0.85f,boxes.get(currentNum).frame()));
            line.show();
        }
        else
        {
            line.hide();
        }
    }

    public void loadNumbers()
    {
        numbers = new ArrayList<>();
        boxes = new ArrayList<>();
        OBControl numbox = objectDict.get("numbox");
        List<String> locs = OBUtils.randomlySortedArray(OBMisc.arrayOfStringsWithRoot("", 1, 10));
        for(int i = 0; i<10; i++)
        {
            OBControl box = new OBControl();
            box.setFrame(0, 0, numbox.width()/10.0f, numbox.height());
            box.setBackgroundColor(Color.WHITE);
            box.setBorderColor(Color.BLACK);
            box.setBorderWidth(applyGraphicScale(2));
            box.setPosition(OB_Maths.locationForRect(1/10.0f ,0.5f,numbox.frame()));
            box.setLeft(numbox.position().x - (5-i)*(box.width() - box.borderWidth));

            OBLabel label =  new OBLabel(String.format("%d",(i+1)*2),OBUtils.standardTypeFace(),65.0f*numbox.height()/85.0f);
            label.setColour(Color.BLACK);
            label.setPosition(OBMisc.copyPoint(box.position()));
            box.setZPosition(1.5f);
            label.setZPosition(2.5f);
            attachControl(label);
            label.setProperty("start_loc",OBMisc.copyPoint(label.position()));
            label.setProperty("dest_loc",OBMisc.copyPoint(objectDict.get(String.format("loc_%s",locs.get(i))).position()));
            attachControl(box);
            numbers.add(label);
            boxes.add(box);
        }
    }

    public void animdrop(final OBControl obj,float secs) throws Exception
    {
        obj.setZPosition(3.5f);
        PointF startpos = OBMisc.copyPoint(obj.position());
        PointF endpos = (PointF)obj.propertyValue("dest_loc");
        float bottomy = this.bounds().height() - obj.height()/2.0f;
        float maxbouncey = objectDict.get("numbox").height() + obj.frame.height()/2.0f;
        float dropy = bottomy - startpos.y;
        float attenuation = (bottomy - maxbouncey)/dropy;
        float seconddrop = endpos.y - maxbouncey;
        float seconddropN = seconddrop / (bottomy - maxbouncey);
        float seconddropxN = (float)Math.sqrt(seconddropN);
        //float t1 = sqrt(attenuation) + seconddropxN;
        float t1 = 1.0f + seconddropxN;
        float totalT = 1.0f + t1 * attenuation;
        float secspert = secs / totalT;
        float pixelspert = (endpos.x - startpos.x) / totalT;
        float endX0 = startpos.x + pixelspert * 1.0f;
        OBAnim anim0 = new OBAnimParabola(startpos.y,bottomy, startpos.x,endX0,0,1,obj);
        float duration0 = secspert * 1.0f;
        OBAnim anim1 = new OBAnimParabola(maxbouncey,bottomy, endX0,endpos.x,-1,t1 - 1,obj);
        float duration1 = secspert * t1 * attenuation;
        OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(anim0), Arrays.asList(anim1)), Arrays.asList(duration0, duration1),false,
                Arrays.asList(OBAnim.ANIM_LINEAR, OBAnim.ANIM_LINEAR),1,
                new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        obj.setZPosition(2.5f);
                    }
                }, this
        );

    }


    public void demo4a() throws Exception
    {
        demoCount(true,true);
        nextScene();
    }

    public void demoCount(boolean forward,boolean audio) throws Exception
    {
        if(audio)
            playAudioScene("DEMO",0,true);

        for(int i=0; i< boxes.size(); i++)
        {
            OBControl box = boxes.get(forward?i:9-i);
            box.setBackgroundColor ( hilitecolour);
            playAudioScene("DEMO",audio?1+i:i,true);
            waitForSecs(0.2f);
            box.setBackgroundColor( Color.WHITE);
        }

    }

    public void demo4b() throws Exception
    {
        demoCount(false,true);
        nextScene();

    }

    public void demo4d() throws Exception
    {
        float delay = 0.9f;
        for (OBControl obj : OBUtils.randomlySortedArray(numbers))
        {
            animdrop(obj,1);
            waitForSecs(delay + 0.1f);
            delay *= delay;
        }

        waitForSecs(1f);
        jumpLine();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        OBControl lastBox = boxes.get(boxes.size()-1);
        OBControl lastNumber = numbers.get(numbers.size()-1);
        moveScenePointer(OB_Maths.locationForRect(0.5f,1f,lastBox.frame()),-20,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,lastNumber.frame()),-25,0.5f,true);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.7f,lastNumber.frame()),-25,0.2f,true);
        flyNumberHome((OBLabel)lastNumber);
        waitForSecs(0.5f);
        thePointer.hide();
        nextScene();

    }
    public void demo4e() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.3f);
        startScene();

    }

    public void demo4g() throws Exception
    {
        demoCount(false,false);
        nextScene();

    }

}
