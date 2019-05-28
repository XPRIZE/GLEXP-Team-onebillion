package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 08/08/16.
 */
public class OC_Alpha1 extends OC_Wordcontroller
{
    float textSize;
    int boxesPerRow;
    List<String> letters,targetLetters;
    List<OBControl> boxes;
    List<OBLabel> labels;
    float yinc;
    int boxHighColour,boxLowColour;

    public void layOutBoxes()
    {
        OBPath c = (OBPath) objectDict.get("_box1");
        PointF topleft = c.position();
        c = (OBPath) objectDict.get("_box2");
        PointF bottomright = c.position();
        boxLowColour = c.fillColor();
        List<OBControl> mBoxes = new ArrayList<>();
        int i = 0,row=0,col=0;
        float y = topleft.y;
        yinc = bottomright.y - y;
        while(i < letters.size() )
        {
            OBControl box = c.copy();
            float x = OB_Maths.interpolateVal(topleft.x, bottomright.x, col * 1.0f /(boxesPerRow - 1));
            box.setPosition(x, y);
            attachControl(box);
            mBoxes.add(box);
            i++;
            col++;
            if(col >= boxesPerRow)
            {
                row++;
                col = 0;
                y += yinc;
            }
        }
        boxes = mBoxes;
        c = (OBPath) objectDict.get("boxhiswatch");
        boxHighColour = c.fillColor();
    }

    public void layOutRope()
    {
        OBControl mainRect = objectDict.get("mainrect");
        OBControl cornerRect = objectDict.get("cornerrect");
        float w = cornerRect.width();
        boolean finished = false;
        int startBox = 0;
        Path p = new Path();
        OBControl bx = boxes.get(0);
        p.moveTo(bx.position().x,bx.position().y);
        while(!finished)
        {
            int lastBox = startBox + boxesPerRow - 1;
            if(lastBox >= boxes.size() )
            {
                lastBox = boxes.size()  - 1;
                if(lastBox <= startBox)
                    break;
            }
            bx = boxes.get(lastBox);
            p.lineTo(bx.position().x,bx.position().y);
            if(lastBox == boxes.size()  - 1)
                break;
            float y = boxes.get(lastBox).position().y;
            float nexty = y + yinc * 0.5f;
            float nx = mainRect.right() - w;
            p.lineTo(nx, y);
            float toy = y + w;
            float cp1x = nx + w * 0.5f,cp1y = y;
            float cp2x = mainRect.right(),cp2y = toy - w * 0.5f;
            p.cubicTo(cp1x,cp1y,cp2x,cp2y,mainRect.right(), toy);
            toy = nexty - w;
            p.lineTo(mainRect.right(), toy);
            cp1x = mainRect.right();cp1y = toy + w * 0.5f;
            cp2x = mainRect.right() - w * 0.5f;cp2y = nexty;
            p.cubicTo(cp1x,cp1y,cp2x,cp2y,mainRect.right() - w,nexty);
            p.lineTo(mainRect.left() + w, nexty);

            toy = nexty + w;
            cp1x = mainRect.left() + w * 0.5f;cp1y = nexty;
            cp2x = mainRect.left(); cp2y = nexty + w * 0.5f;
            p.cubicTo(cp1x,cp1y,cp2x,cp2y,mainRect.left(), toy);
            nexty = nexty + yinc * 0.5f;
            p.lineTo(mainRect.left(), nexty-w);
            cp1x = mainRect.left();cp1y = nexty - w * 0.5f;
            cp2x = mainRect.left() + w * 0.5f;cp2y = nexty;
            p.cubicTo(cp1x,cp1y,cp2x,cp2y,mainRect.left() + w, nexty);


            startBox =(int)lastBox + 1;
        }
        OBPath rope = (OBPath) objectDict.get("ropestart");
        OBPath path = new OBPath(p);
        path.setFillColor(0);
        path.setStrokeColor(rope.strokeColor());
        path.setLineWidth(rope.lineWidth());
        path.sizeToBoundingBoxIncludingStroke();
        attachControl(path);
        objectDict.put("path",path);
        PointF pt = convertPointFromControl(rope.firstPoint(),rope);
        PointF diff = OB_Maths.DiffPoints(boxes.get(0).position(), pt);
        rope.setPosition(OB_Maths.AddPoints(rope.position(), diff));
        rope = (OBPath) objectDict.get("ropeend");
        pt = convertPointFromControl(rope.firstPoint(),rope);
        diff = OB_Maths.DiffPoints(boxes.get(boxes.size()  - 1).position(), pt);
        rope.setPosition(OB_Maths.AddPoints(rope.position(), diff));
    }

    public void layOutLabels()
    {
        Typeface tf = OBUtils.standardTypeFace();
        List<OBLabel> mlabels = new ArrayList<>();
        //
        float fontSize = textSize * 0.85f;
        //
        for(int i = 0;i < letters.size();i++)
        {
            String text = letters.get(i);
            //
            if (text.toUpperCase().equals(text))
            {
                text = OC_Generic.toTitleCase(text);
                fontSize = textSize * 0.75f;
            }
            //
            OBLabel label = new OBLabel(text, tf, fontSize);
            label.setColour(Color.BLACK);
            label.setPosition(boxes.get(i).position());
            label.setZPosition(5);
            attachControl(label);
            mlabels.add(label);
            label.hide();
        }
        labels = mlabels;
    }

    public void miscSetUp()
    {
        letters = Arrays.asList(parameters.get("letters").split(","));
        targetLetters = Arrays.asList(parameters.get("targetletters").split(","));
        boxesPerRow = Integer.parseInt(eventAttributes.get("boxesperrow"));
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        layOutBoxes();
        layOutRope();
        layOutLabels();
        currNo = 0;
        String d = parameters.get("demo");
        needDemo = (d != null) && d.equals("true");
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList<>();
        if(needDemo)
            events.add("b");
        events.add("c");
        events.add("d");
        events.add("e");
        events.add(0,"a");
        int noscenes = targetLetters.size() +1;
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        doVisual(currentEvent());
    }

    public void setSceneXX(String  scene)
    {
        targets = boxes;
    }

    public void doAudio(String  scene) throws Exception
    {
        List reparr = new ArrayList<>();
        reparr.addAll(currentAudio("PROMPT.REPEAT"));
        reparr.add(300);
        reparr.add(String.format("alph_%s",targetLetters.get(currNo).toLowerCase()));
        setReplayAudio(reparr);
        playAudioQueuedScene("PROMPT",true);

        waitForSecs(0.3f);
        List ls = Arrays.asList(String.format("alph_%s",targetLetters.get(currNo).toLowerCase()));
        playAudioQueued(ls,false);

    }

    public void doMainXX() throws Exception
    {
        doAudio(currentEvent());
    }

    public void endBody()
    {
        List reminderAudio = currentAudio("PROMPT.REMINDER");
        if(reminderAudio != null)
        {
            try
            {
                final long sttime = statusTime;
                waitForSecs(0.3f);
                waitAudio();
                if(sttime == statusTime)
                    reprompt(sttime, reminderAudio, 6, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            if(sttime == statusTime)
                            {
                                List ls = Arrays.asList(String.format("alph_%s",targetLetters.get(currNo).toLowerCase()));
                                playAudioQueued(ls,false);
                            }
                        }
                    });
            }
            catch(Exception e)
            {
            }
         }
    }

    public void showLetters()throws Exception
    {
        float dur;
        Map<String,Object> sfx = (Map<String,Object>) audioScenes.get("sfx");
        List<String> fn = (List<String>) sfx.get("fullalphin");
        OBAudioManager.audioManager.prepareForChannel(fn.get(0),OBAudioManager.AM_MAIN_CHANNEL);
        dur = (float) OBAudioManager.audioManager.durationForChannel(OBAudioManager.AM_MAIN_CHANNEL);
        if(dur == 0)
            dur = 0.3f;
        dur = dur / letters.size();
        playSfxAudio("fullalphin",false);
        for(OBLabel l : labels)
        {
            l.show();
            waitForSecs(dur);
        }
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        showLetters();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        nextScene();
    }

    public void highlightBox(OBControl box,boolean high)
    {
        int col = high?boxHighColour:boxLowColour;
        ((OBPath)box).setFillColor(col);
    }

    public void demob() throws Exception
    {
        waitForSecs(0.3f);
        String targetLetter = targetLetters.get(currNo);
        int idx = letters.indexOf(targetLetter);
        OBControl box = boxes.get(idx);
        OBPath rope = (OBPath) objectDict.get("ropeend");

        PointF destpt = OB_Maths.locationForRect(1.9f, 1, rope.frame);
        PointF startpt = pointForDestPoint(destpt,15);
        startpt.y = (bounds().height() + 1);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-0.4f,true);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        List ls = Arrays.asList(String.format("alph_%s",targetLetters.get(currNo).toLowerCase()));
        playAudioQueued(ls,false);

        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, box.frame()),-0.7f,true);
        waitForSecs(0.2f);
        highlightBox(box,true);
        playSfxAudio("click",true);
        movePointerForwards(applyGraphicScale(-20),-1);
        waitForSecs(0.3f);

        playAudioQueued(ls,false);
        waitForSecs(0.5f);

        highlightBox(box,false);
        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.4f);
        playAudioScene("DEMO",1,true);
        nextObj();
    }

    public void nextObj()
    {
        currNo++;
        nextScene();
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        int saveStatus = status();
        setStatus(STATUS_CHECKING);
        List saverep = emptyReplayAudio();
        highlightBox(targ,true);
        try
        {
            int idx = boxes.indexOf(targ);
            if(letters.get(idx).equals(targetLetters.get(currNo)))
            {
                playSfxAudio("click",true);
                waitForSecs(0.4f);
                List ls = Arrays.asList(String.format("alph_%s",letters.get(idx).toLowerCase()));
                playAudioQueued(ls,true);

                waitForSecs(0.4f);
                gotItRightBigTick(true);
                highlightBox(targ,false);
                nextObj();
            }
            else
            {
                gotItWrongWithSfx();
                waitSFX();
                setReplayAudio(saverep);
                highlightBox(targ,false);
                List ls = Arrays.asList(String.format("alph_%s",targetLetters.get(currNo).toLowerCase()));
                playAudioQueued(ls,false);
                setStatus(saveStatus);
            }
        }
        catch(Exception exception)
        {
        }

    }


    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(target,pt);
                    }
                });
            }
        }
    }

}
