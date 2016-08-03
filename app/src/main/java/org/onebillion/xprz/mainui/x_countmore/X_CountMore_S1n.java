package org.onebillion.xprz.mainui.x_countmore;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 02/08/16.
 */
public class X_CountMore_S1n extends XPRZ_SectionController
{
    List<OBGroup> childrenOrder;
    List<Float> childrenLocs;
    List<OBLabel> labels;
    float bottomLoc;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master1n");
        childrenOrder = new ArrayList<>();
        childrenLocs = new ArrayList<>();
        labels = new ArrayList<>();

        OBGroup stop = (OBGroup)objectDict.get("bus_stop");
        bottomLoc = objectDict.get("child_1").bottom();

        Typeface font = OBUtils.standardTypeFace();
        float fontSize = 70.0f * stop.height()/338.0f;
        for(int i=0; i<5; i++)
        {
            OBLabel label = new OBLabel(String.format("%d",i+1), font,fontSize);
            label.setColour(Color.BLACK);
            attachControl(label);
            label.hide();
            labels.add(label);

            OBGroup child = (OBGroup)objectDict.get(String.format("child_%d",i+1));
            child.setAnchorPoint(new PointF(0.5f, 1));
            PointF loc = XPRZ_Generic.copyPoint(child.position());
            loc.y = bottomLoc;
            childrenLocs.add(loc.x);
            child.setPosition(loc);
            childrenOrder.add(child);
        }

        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1n();
            }
        });
    }

    @Override
    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
    }

    @Override
    public void doMainXX() throws Exception
    {
        startScene();
    }

    @Override
    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl child = finger(0,1,filterControls("child_.*"),pt);
            if(child !=null)
            {
                setStatus(STATUS_BUSY);
                playAudio(null);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkChildClick((OBGroup)child);
                    }
                });

            }
        }
        else if(status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl child = finger(0,1,filterControls("child_.*"),pt);
            if(child !=null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkChildDrag((OBGroup)child, pt);
                    }
                });

            }
        }
    }

    private void checkChildDrag(OBGroup child, PointF pt) throws Exception
    {
        if(child == objectDict.get(eventAttributes.get("target")))
        {
            gotItRight();
            child.setProperty("start_loc", XPRZ_Generic.copyPoint(child.position()));
            OBMisc.prepareForDragging(child,pt,this);
            setStatus(STATUS_DRAGGING);
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            setStatus(STATUS_WAITING_FOR_DRAG);
            playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }

    private void checkChildClick(OBGroup child) throws Exception
    {
        X_CountMore_S1.hiliteClothes(child,true,this);
        if(child == objectDict.get(String.format("child_%s",eventAttributes.get("correct"))))
        {
            gotItRight();
            if(OBUtils.getBooleanValue(eventAttributes.get("tick")))
            {
                displayTick();
            }
            else
            {
                playAudio("correct");
                waitAudio();
            }

            if(currentEvent() != events.get(events.size()-1))
            {
                waitForSecs(0.3f);
                OBLabel label = showChildLabel(child);
                playAudioQueuedScene("FINAL",0.3f,true);
                waitForSecs(0.3f);
                label.hide();
            }
            else
            {
                playAudioQueuedScene("FINAL",0.3f,true);

            }

            X_CountMore_S1.hiliteClothes(child,false,this);
            waitForSecs(0.3f);
            if(!OBUtils.getBooleanValue(eventAttributes.get("tick")))
            {
                displayTick();
                waitForSecs(0.3f);
            }

            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            X_CountMore_S1.hiliteClothes(child,false,this);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    @Override
    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    @Override
    public void touchUpAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_BUSY);
            final OBGroup child = (OBGroup) target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkChildDrop(child);
                }
            });
        }
    }

    private void checkChildDrop(OBGroup child) throws Exception
    {
        boolean swapPossible = false;
        int childNum = Integer.valueOf(eventAttributes.get("correct"));
        if (childNum == 1)
        {
            List<OBControl> children = filterControls("child_.*");
            children.remove(child);
            OBControl cont = OBMisc.nearestControlToPoint(children, child.position());
            if (cont == childrenOrder.get(childNum - 1))
                swapPossible = true;
        } else
        {
            swapPossible = (childNum < childrenOrder.size())
                    && (childrenOrder.get(childNum - 1).position().x > child.position().x)
                    && (childrenOrder.get(childNum - 2).position().x < child.position().x);
        }

        if (swapPossible)
        {
            gotItRight();
            playAudio("click");
            reorderChildren(child, childrenOrder.get(childNum - 1));
            waitAudio();
            waitForSecs(0.3f);
            OBLabel label = showChildLabel(child);
            playAudioQueuedScene("FINAL", 0.3f, true);
            waitForSecs(0.3f);
            label.hide();
            nextScene();
        } else
        {
            gotItWrongWithSfx();
            moveControlBack(child);
            waitSFX();
            setStatus(STATUS_WAITING_FOR_DRAG);
            playAudioQueuedScene("INCORRECT", 0.3f, false);
        }
    }


    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4, setStatus(OBUtils.getBooleanValue(eventAttributes.get("drag")) ? STATUS_WAITING_FOR_DRAG : STATUS_AWAITING_CLICK), this);
    }


    public void reorderChildren(OBGroup child, OBControl nearest) throws Exception
    {
        childrenOrder.remove(child);
        childrenOrder.add(childrenOrder.indexOf(nearest),child);
        List<OBAnim> arr = new ArrayList<>();
        for(int i=0; i<childrenOrder.size(); i++)
        {
            PointF destLoc = new PointF((float)childrenLocs.get(i) , bottomLoc);
            OBGroup con = (OBGroup)childrenOrder.get(i);

            if(destLoc.x != con.position().x)
            {
                arr.add(OBAnim.moveAnim(destLoc,con));
            }
        }
        playSfxAudio("drop",false);
        OBAnimationGroup.runAnims(arr,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        child.setZPosition(child.zPosition() - 10);
    }

    public void moveControlBack(OBControl targ) throws Exception
    {
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim((PointF)targ.propertyValue("start_loc"),targ)),0.25f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        targ.setZPosition(targ.zPosition() - 10);

    }

    public OBLabel showChildLabel(OBGroup child)
    {
        OBLabel label = labels.get(childrenOrder.indexOf(child));
        PointF loc = new PointF(child.position().x, child.top() - label.height()*0.7f);
        label.setPosition(loc);
        label.show();
        return label;
    }


    public void demoCountChildren(boolean withLabels) throws Exception
    {
        for(int i=0; i<childrenOrder.size(); i++)
        {
            OBGroup child= childrenOrder.get(i);
            OBLabel label=null;
            if(withLabels)
            {
                label = showChildLabel(child);
            }
            else
            {
                X_CountMore_S1.hiliteClothes(child,true,this);
            }
            playAudioScene("DEMO",i,true);
            if(!withLabels)
            {
                X_CountMore_S1.hiliteClothes(child,false,this);
            }
            waitForSecs(0.3f);
        }

        waitForSecs(0.5f);
        if(withLabels)
        {
            lockScreen();
            for(OBLabel lab : labels)
            {
                lab.hide();
            }
            unlockScreen();
            waitForSecs(0.5f);
        }
        nextScene();
    }


    public void demo1n()throws Exception
    {
        playAudioQueuedScene("DEMO",0.3f,true);
        waitForSecs(0.3f);
        startScene();
    }

    public void demo1q()throws Exception
    {
        demoCountChildren(true);
    }

    public void demo1w()throws Exception
    {
        demoCountChildren(false);
    }



}
