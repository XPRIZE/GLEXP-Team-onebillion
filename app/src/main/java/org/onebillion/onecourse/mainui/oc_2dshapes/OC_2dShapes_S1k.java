package org.onebillion.onecourse.mainui.oc_2dshapes;

import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by michal on 05/03/2018.
 */

public class OC_2dShapes_S1k extends OC_SectionController
{
    float currentStrokeT;
    OBPath mainPath;
    OBLabel caption;
    long scanTime;
    int textColour;
    boolean playIncorrectAudio;

    public float graphicScale()
    {
        return bounds().width()/1024;
    }
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        textColour = OBUtils.colorFromRGBString(eventAttributes.get("colour_text"));
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1k();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        currentStrokeT=0;
        OBGroup trace =(OBGroup)objectDict.get("trace");
        trace.recalculateFrameForPath(trace.members);
        mainPath =(OBPath ) trace.objectDict.get("path");
        mainPath.setStrokeEnd(0);
        mainPath.show();
        OBControl box = objectDict.get("text_box");
        caption = new OBLabel(eventAttributes.get("caption"), OBUtils.StandardReadingFontOfSize(90));
        caption.setColour(textColour);
        attachControl(caption);
        caption.setPosition(box.position());
        caption.hide();
        playIncorrectAudio = true;
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            if ((currentStrokeT == 0 && finger(0, 1, Arrays.asList(objectDict.get("arrow")), pt) != null) || currentStrokeT > 0)
            {
                playAudio(null);
                checkClosestPoint(pt);
            }
            else if (currentStrokeT == 0)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        playAudio("wrong");
                        waitAudio();
                        playAudioQueuedScene("INCORRECT", 300, false);
                    }
                });
            }
        }
    }


    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
        {
            checkClosestPoint(pt);
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
        {
            playIncorrectPrompt(setStatus(STATUS_WAITING_FOR_DRAG));
        }
    }

    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,currentEvent(),setStatus(STATUS_WAITING_FOR_DRAG),this);
    }

    public PointF pointForMainPathT(float t)
    {
        return convertPointFromControl(mainPath.sAlongPath(t,null),objectDict.get("trace"));
    }

    public void playIncorrectPrompt(final long time)
    {
        if (playIncorrectAudio)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    waitForSecs(3f);
                    if (playIncorrectAudio && time == statusTime)
                    {
                        playIncorrectAudio = false;
                        playAudioQueuedScene("INCORRECT2", 300, false);
                    }
                }
            });
        }
    }

    public void checkClosestPoint(PointF pt)
    {
        setStatus(STATUS_BUSY);
        float tolerance = applyGraphicScale(100);
        float minDistance = bounds().width();
        float currentDistance =-1, minT = -1;
        PointF currentPoint = pointForMainPathT(currentStrokeT);
        if(OB_Maths.PointDistance(pt, currentPoint) <= tolerance)
        {
            float maxT = currentStrokeT+0.1f;
            for(float i=currentStrokeT; i<=(maxT>1 ? 1 : maxT); i+=0.001)
            {
                currentPoint = pointForMainPathT(i);
                currentDistance = OB_Maths.PointDistance(pt, currentPoint);
                if(currentDistance < minDistance)
                {
                    minDistance = currentDistance;
                    minT = i;
                }
                else if(minT > 0)
                {
                    break;
                }
            }
            if(minT > currentStrokeT)
            {
                if(currentStrokeT == 0)
                    objectDict.get("arrow").hide();
                mainPath.setStrokeEnd(minT);
                currentStrokeT = minT;
                if(minT >= 0.98)
                {
                    mainPath.setStrokeEnd(1);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            waitForSecs(0.3f);
                            gotItRightBigTick(true);
                            waitForSecs(0.3f);
                            playAudioScene("FINAL", 0, true);
                            OBGroup trace = (OBGroup) objectDict.get("trace");
                            lockScreen();
                            trace.objectDict.get("dash").hide();
                            trace.objectDict.get("background").show();

                            unlockScreen();
                            if (!performSel("demoFin", currentEvent()))
                                waitForSecs(1.5f);
                            if (events.get(events.size() - 1) != currentEvent())
                            {
                                lockScreen();
                                detachControl(trace);
                                detachControl(objectDict.get("arrow"));
                                detachControl(caption);
                                unlockScreen();
                            }
                            nextScene();

                        }

                    });
                }
                else
                {
                    setStatus(STATUS_DRAGGING);
                }

            }
            else
            {
                setStatus(STATUS_DRAGGING);
            }

        }
        else
        {
            if(currentStrokeT >0 && playIncorrectAudio)
            {
                playIncorrectAudio = false;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        playAudioQueuedScene("INCORRECT2",300,false);
                    }
                });
            }
            setStatus(STATUS_DRAGGING);
        }
    }

    public void showCaption() throws Exception
    {
        lockScreen();
        caption.show();
        playSfxAudio("text_up",false);
        unlockScreen();
        waitSFX();
    }

    public void demo1k() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.25f,0.5f,objectDict.get("trace") .frame()),-35,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(pointForMainPathT(0),-20,0.5f,true);
        objectDict.get("arrow").hide();
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                mainPath.setStrokeEnd(frac);
                thePointer.setPosition(pointForMainPathT(frac));
                thePointer.setRotation((float)Math.toRadians(-35.0f + (float)Math.abs(0.5f-frac) * 40.0f));
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(blockAnim),2,true, OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        moveScenePointer(OB_Maths.locationForRect(1.25f,0.5f,objectDict.get("trace") .frame()),-35,0.5f,"DEMO",1,0.3f);
        caption.show();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,caption.frame()),-35,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        lockScreen();
        mainPath.setStrokeEnd(0);
        objectDict.get("arrow").show();
        unlockScreen();
        startScene();
    }

    public void demoCaption() throws Exception
    {
        waitForSecs(0.5f);
        caption.show();
        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,1f,caption.frame()) ,-35,0.5f,"FINAL",1,0.5f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demoFin1l() throws Exception
    {
        demoCaption();
    }

    public void demoFin1m() throws Exception
    {
        demoCaption();
    }

    public void demoFin1n() throws Exception
    {
        demoCaption();
    }

}
