package com.maq.xprize.onecourse.mainui.oc_caps;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.*;
import com.maq.xprize.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import com.maq.xprize.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.maq.xprize.onecourse.utils.*;

/**
 * Created by alan on 02/01/2018.
 */

public class OC_CapsMore extends OC_Wordcontroller
{
    List<String>letters,capitals;
    List<OBLabel>labels,capLabels;
    float fontSize;
    OBConditionLock audioLock;

    public void positionLabel(OBLabel lab,OBLabel cap,OBControl box)
    {
        float space = box.width() -(lab.width() + cap.width());
        lab.setTop(box.top());
        cap.setTop(lab.top());
        lab.setLeft(box.left() + space * 0.35f);
        cap.setRight(box.right() - space * 0.35f);
        lab.setZPosition(10);
        cap.setZPosition(10);
        lab.setColour(Color.BLACK);
        cap.setColour(Color.BLACK);
        attachControl(lab);
        attachControl(cap);
        PointF diff = OB_Maths.DiffPoints(cap.position(), box.position());
        cap.setProperty("labelpos",diff);
    }

    public void setUpLabels()
    {
        List caps = new ArrayList<>();
        List labs = new ArrayList<>();
        List caplabs = new ArrayList<>();
        OBFont font = OBUtils.StandardReadingFontOfSize(fontSize);
        int i = 0;
        for(String s : letters)
        {
            caps.add(s.toUpperCase());
            OBLabel lab = new OBLabel(s,font);
            labs.add(lab);
            OBLabel caplab = new OBLabel(s.toUpperCase(),font);
            caplabs.add(caplab);
            OBControl homebox = objectDict.get(String.format("box%d",i));
            positionLabel(lab,caplab,homebox);
            i++;
        }
        labels = labs;
        capLabels = caplabs;
        for(OBControl c : labels)
            c.hide();
        for(OBControl c : capLabels)
            c.hide();
    }
    public void miscSetUp()
    {
        loadEvent("mastera");
        String s;
        if((s = parameters.get("letters"))!=null)
            letters = Arrays.asList(s.split(","));
        if((s = eventAttributes.get("textsize"))!= null)
            fontSize = Float.parseFloat(s);
        if(letters.size() == 6)
            loadEvent("o6");
        else
            loadEvent("o5");
        for(OBControl c : filterControls("box.*"))
        {
            RectF f = new RectF(c.frame());
            float amt = applyGraphicScale(2);
            f.inset(-amt,-amt);
            c.setFrame(f);
        }
        hideControls("letterline");
        setUpLabels();
        hideControls("box.*");
        hideControls("bottomrect");
        currNo = -1;
        events = Arrays.asList("1,2,3,4,5".split(","));
    }

    public long switchStatus(String scene)
    {
        if("3".compareTo(scene) < 0)
            return setStatus(STATUS_WAITING_FOR_DRAG);
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        miscSetUp();
        doVisual(currentEvent());
    }

    public void setScene1()
    {
        targets = sortedFilteredControls("box.*");
    }

    public void mixUpLabels()
    {
        List<OBLabel> mixedlabs = OBUtils.VeryRandomlySortedArray(capLabels);
        RectF f = objectDict.get("letterline").frame();
        float left = f.left;
        float interval = f.width() /(mixedlabs.size() - 1);
        float y = f.top;
        for(OBLabel l : mixedlabs)
        {
            PointF pos = new PointF(left, y);
            left += interval;
            l.setProperty("origpos",pos);
        }
    }
    public void setScene3()
    {
        mixUpLabels();
        float w = bounds() .width();
        for(OBLabel l : capLabels)
        {
            l.setPosition((PointF)l.propertyValue("origpos"));
            l.setLeft(l.left() - w);
            l.show();
        }
    }

    public void setScene4()
    {
        List a = new ArrayList();
        a.addAll(capLabels);
        targets = a;
    }

    public void setScene5()
    {
        List a = new ArrayList();
        a.addAll(capLabels);
        targets = a;
    }

    public void doMainXX() throws Exception
    {
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        audioLock = playAudioQueuedScene("PROMPT",false);
        waitForSecs(0.3f);
    }

    public void doMain5() throws Exception
    {
        List anims = new ArrayList<>();
        for(OBLabel l : capLabels)
            anims.add(OBAnim.moveAnim((PointF)l.propertyValue("origpos"),l));
        playSfxAudio("home",false);
        OBAnimationGroup.runAnims(anims,0.7,true,OBAnim.ANIM_EASE_IN,null);

        List<PointF> poses = new ArrayList<>();
        List<OBControl> boxes = sortedFilteredControls("box.*");
        for(OBControl c : boxes)
        {
            PointF p = new PointF();
            p.set(c.position());
            poses.add(p);
        }
        List<PointF> vposes = OBUtils.VeryRandomlySortedArray(poses);
        anims.clear();
        for(int i = 0;i < labels.size();i++)
        {
            OBLabel l = labels.get(i);
            OBControl box = boxes.get(i);
            PointF newpos = vposes.get(i);
            anims.add(OBAnim.moveAnim(newpos,box));
            PointF diff = OB_Maths.DiffPoints(newpos, box.position());
            PointF pt = OB_Maths.AddPoints(l.position(), diff);
            anims.add(OBAnim.moveAnim(pt,l));
        }
        playSfxAudio("boxmove",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_OUT,null);
        doMainXX();
    }

    public void flashLetters(long sttime) throws Exception
    {
        if(statusChanged(sttime))
            return;
        for(int i = 0;i < 3;i++)
        {
            lockScreen();
            for(OBLabel l : labels)
            {
                l.setColour(Color.RED);
            }
            unlockScreen();
            waitForSecs(0.2f);
            lockScreen();
            for(OBLabel l : labels)
            {
                l.setColour(Color.BLACK);
            }
            unlockScreen();
            waitForSecs(0.2f);
        }
    }

    public void endBody()
    {
        try
        {
            if(currentAudio("PROMPT.REMINDER") != null)
            {
                final long stt = statusTime();
                waitForSecs(0.2f);
                waitAudioQueue(audioLock);
                reprompt(stt,(List)currentAudio("PROMPT.REMINDER"),5);
            }
            else
            {
                if(currentEvent().equals("2"))
                {
                    final long tin = statusTime();
                    OBUtils.runOnOtherThreadDelayed(5,new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            flashLetters(tin);
                        }
                    });
                }
            }
        }
        catch(Exception e)
        {

        }
    }

    public void demo1() throws Exception
    {
        waitForSecs(0.2f);
        playSfxAudio("boxon",false);
        lockScreen();
        showControls("box.*");
        unlockScreen();
        waitSFX();
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);
        for(OBLabel l : labels)
        {
            l.show();
            playSfxAudio("letteron",true);
        }
        nextScene();
    }

    public void demo3() throws Exception
    {
        playSfxAudio("boxon",false);
        showControls("bottomrect");
        List anims = new ArrayList<>();
        for(OBLabel l : capLabels)
            anims.add(OBAnim.moveAnim((PointF) l.propertyValue("origpos"),l));
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_OUT,null);
        waitForSecs(1f);
        if(currentAudio("DEMO")!= null)
        {
            PointF destpoint = OB_Maths.locationForRect(new PointF(0.9f, 0.9f) ,bounds());
            PointF startpt = pointForDestPoint(destpoint,30);
            loadPointerStartPoint(startpt,destpoint);
            movePointerToPoint(destpoint,-1,true);
            waitForSecs(0.2f);
            playAudioQueuedScene("DEMO",true);
            waitForSecs(0.2f);
            OBLabel l = capLabels.get(2);
            movePointerToPoint(l.position(),-1,true);
            waitForSecs(0.3f);
            PointF diff = (PointF) l.propertyValue("labelpos");
            PointF pos = OB_Maths.AddPoints(diff, objectDict.get("box2").position());
            moveObjects(Arrays.asList(l,thePointer),pos,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
            playSfxAudio("match",false);
            movePointerForwards(applyGraphicScale(-40),-1);
            waitForSecs(0.5f);
            moveObjects(Arrays.asList((OBControl)l),(PointF)l.propertyValue("origpos"),-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
            waitForSecs(0.4f);
            thePointer.hide();
        }
        nextScene();
    }

    public void finishTouchScene() throws Exception
    {
        waitForSecs(1f);
        playSfxAudio("lettersalloff",false);
        lockScreen();
        for(OBLabel l : capLabels)
            l.hide();
        unlockScreen();
        waitForSecs(1f);
    }

    public void nextTouchLabel() throws Exception
    {
        if(targets.size() == 0)
        {
            finishTouchScene();
            nextScene();
        }
        else
            switchStatus(currentEvent());
    }

    public void highlightBothInTurn(int idx) throws Exception
    {
        String letterName = capLabels.get(idx).text().toLowerCase();
        labels.get(idx).setColour(Color.RED);
        playLetterName(letterName);
        waitForSecs(0.2f);
        waitAudio();
        labels.get(idx).setColour(Color.BLACK);
        waitForSecs(0.1f);
        capLabels.get(idx).setColour(Color.RED);
        playLetterName(letterName);
        waitForSecs(0.2f);
        waitAudio();
        capLabels.get(idx).setColour(Color.BLACK);
    }
    public void checkTarget(Object targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            int idx = sortedFilteredControls("box.*").indexOf(targ);
            capLabels.get(idx).show();
            gotItRightBigTick(false);
            playSfxAudio("letteron",true);
            targets.remove(targ);

            highlightBothInTurn(idx);

            nextTouchLabel();
        }
        catch(Exception exception)
        {
        }
    }

    public void nextDragLabel() throws Exception
    {
        if(targets.size() == 0)
        {
            waitSFX();
            gotItRightBigTick(true);
            nextScene();
        }
        else
            switchStatus(currentEvent());
    }

    public int indexOfChosenBox(OBControl obj)
    {
        List boxes = sortedFilteredControls("box.*");
        OBControl c = finger(-1,2,boxes,obj.position());
        if(c != null)
            return(int) boxes.indexOf(c);
        return -1;
    }
    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            OBLabel targ =(OBLabel) target;
            setStatus(STATUS_CHECKING);
            int boxidx = indexOfChosenBox(targ);
            if(boxidx < 0)
            {
                moveObjects(Arrays.asList((OBControl)targ),(PointF)targ.propertyValue("origpos"),-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                switchStatus(currentEvent());
                targ.setZPosition(targ.zPosition()- 30);
                return;
            }
            else
            {
                int labidx =(int) capLabels.indexOf(targ);
                OBControl box = sortedFilteredControls("box.*") .get(boxidx);
                box.highlight();
                if(labidx == boxidx)
                {
                    PointF diff = (PointF)targ.propertyValue("labelpos");
                    PointF pos = OB_Maths.AddPoints(diff, box.position());

                    moveObjects(Arrays.asList((OBControl)targ),pos,-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
                    playSfxAudio("match",false);
                    targets.remove(targ);
                    targ.setZPosition(targ.zPosition()- 30);
                    box.lowlight();
                    waitSFX();

                    highlightBothInTurn(labidx);

                    nextDragLabel();
                    return;
                }
                else
                {
                    gotItWrongWithSfx();
                    moveObjects(Arrays.asList((OBControl)targ),(PointF)targ.propertyValue("origpos"),-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                    playAudioQueuedScene("INCORRECT",false);
                    box.lowlight();
                    switchStatus(currentEvent());
                    targ.setZPosition(targ.zPosition() - 30);
                    return;
                }
            }
        }
        catch(Exception exception)
        {
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }


    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }

    public void checkDragTargetPoint(PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target.setZPosition(target.zPosition()+ 30);
        dragOffset = OB_Maths.DiffPoints(target.position(), pt);
    }


    public Object findTarget(PointF pt)
    {
        OBControl c =finger(0,2,targets,pt);
        return c;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final Object obj = findTarget(pt);
            if(obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(obj);
                    }
                });
            }
        }
        else if(status() == STATUS_WAITING_FOR_DRAG)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkDragTargetPoint(pt);
                    }
                });
            }
        }

    }

}
