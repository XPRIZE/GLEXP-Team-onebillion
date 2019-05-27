package com.maq.xprize.onecourse.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by michal on 20/02/2017.
 */

public class OC_CountMore_S3  extends OC_SectionController
{
    List<OBGroup> numbers;
    List<OBControl> sceneObjs;
    int hilitecolour;
    int correct;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        loadNumbers();
        sceneObjs = new ArrayList<>();
        hilitecolour = OBUtils.colorFromRGBString(eventAttributes.get("colour_highlight"));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());

    }
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo3a();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        correct = Integer.valueOf(eventAttributes.get("correct"));
        if(eventAttributes.get("reload") != null)
        {
            for(OBControl con : sceneObjs)            detachControl(con);
            sceneObjs.clear();
            String[] vals = eventAttributes.get("reload").split(",");
            int val1 = Integer.valueOf(vals[0]);
            int val2 = Integer.valueOf(vals[1]);
            OBGroup obj = (OBGroup)objectDict.get("obj");
            obj.show();
            OBGroup group = new OBGroup(Arrays.asList(obj.copy()));
            obj.hide();
            group.highlight();
            group.lowlight();
            PointF rloc = OB_Maths.relativePointInRectForLocation(obj.position(), objectDict.get("workrect").frame());
            float distance = (1.0f-(rloc.x*2.0f))/(val1-1);
            for(int i=0;i<val2;i++)
            {
                OBGroup screenObj = (OBGroup)group.copy();
                PointF objLoc = new PointF(rloc.x + (i%val1) *distance, rloc.y);
                screenObj.setPosition ( OB_Maths.locationForRect(objLoc.x,objLoc.y,objectDict.get("workrect").frame()));
                if(i>=val1)
                {
                    screenObj.setTop (sceneObjs.get(0).bottom()+0.3f*screenObj.height());
                }
                attachControl(screenObj);
                sceneObjs.add(screenObj);

            }

        }
        for(int i=0;i<sceneObjs.size();i++)
            sceneObjs.get(i).hide();
        for(int i=0;i<correct;i++)
            sceneObjs.get(i).show();
        for(OBGroup con : numbers)
            con.objectDict.get("box").setBackgroundColor(Color.WHITE);

    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBGroup box = (OBGroup)finger(-1,-1,(List<OBControl>)(Object)numbers, pt);
            if(box != null)
            {
                setStatus(STATUS_BUSY);

                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(box);
                    }
                });
            }
        }
    }


    public void checkTarget(OBGroup box) throws Exception
    {
        playAudio(null);
        box.objectDict.get("box").setBackgroundColor(hilitecolour);
        if((int)box.settings.get("num_val") == correct)
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            playAudioQueuedScene("FINAL",0.3f,true);
            waitForSecs(0.5f);
            nextScene();

        }
        else
        {
            gotItWrongWithSfx();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitSFX();
            box.objectDict.get("box").setBackgroundColor(Color.WHITE);
            if(time == statusTime)
                playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }

    public void startScene() throws Exception
    {
        demoCount();
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void loadNumbers()
    {
        numbers = new ArrayList<>();
        OBControl numbox = objectDict.get("numbox");
        float fontSize = 65.0f*numbox.height()/85.0f;
        for(int i = 0;i<10;i++)
        {
            OBControl box = new OBControl();
            box.setFrame(new RectF(0, 0, numbox.width()/10.0f, numbox.height()));
            box.setBackgroundColor(Color.WHITE);
            box.setBorderColor(Color.BLACK);
            box.setBorderWidth(applyGraphicScale(2));
            box.setPosition(OB_Maths.locationForRect(1/10.0f * i,0.5f,numbox.frame()));
            box.setLeft(numbox.position().x - (5-i)*(box.width() - box.borderWidth));
            OBLabel label = new OBLabel(String.format("%d",(i+1)*2),OBUtils.standardTypeFace(), fontSize);
            label.setColour(Color.BLACK);
            label.setPosition(box.position());

            OBGroup group = new OBGroup(Arrays.asList(box,label));
            group.objectDict.put("label",label);
            group.objectDict.put("box",box);
            attachControl(group);
            group.setProperty("num_val",i+1);
            numbers.add(group);

        }

    }

    public void demoCount() throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.3f);
        if(getAudioForScene(currentEvent(),"DEMO2") != null)
        {
            for(int i=0;i<correct;i++)
            {
                OBGroup cont = (OBGroup)sceneObjs.get(i);
                cont.highlight();
                playAudioScene("DEMO2",i,true);
                waitForSecs(0.3f);
                cont.lowlight();

            }
            waitForSecs(0.3f);
        }
    }


    public void demo3a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.8f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.3f,0.8f,this.bounds()),-30,0.5f,"DEMO",1,0.3f);
        for(int i=0;i<5;i++)
        {
            OBGroup cont = (OBGroup)sceneObjs.get(i);
            movePointerToPoint(OB_Maths.locationForRect(0.55f,1.05f,cont.frame()) ,-30+(i*4),0.5f,true);
            cont.highlight();
            playAudioScene("DEMO2",i,true);
            waitForSecs(0.3f);
            cont.lowlight();

        }
        OBGroup targetBox = numbers.get(4);
        movePointerToPoint(OB_Maths.locationForRect(0.55f,1.05f,targetBox.frame()) ,-20,0.5f,true);
        movePointerToPoint(OB_Maths.locationForRect(0.55f,0.7f,targetBox.frame()) ,-20,0.2f,true);
        targetBox.objectDict.get("box").setBackgroundColor(hilitecolour);
        playAudio("correct");
        waitAudio();
        movePointerToPoint(OB_Maths.locationForRect(0.55f,1.05f,targetBox.frame()) ,-20,0.2f,true);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.8f,this.bounds()),-15,0.5f,"DEMO3",0,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        nextScene();

    }


}
