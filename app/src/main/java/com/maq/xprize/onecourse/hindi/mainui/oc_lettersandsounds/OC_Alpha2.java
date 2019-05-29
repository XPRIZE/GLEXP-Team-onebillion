package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.Path;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 16/08/16.
 */
public class OC_Alpha2 extends OC_Alpha1
{
    int letterLowColour,letterHighColour;
    List<OBControl> strayLetters;

    public void miscSetUp()
    {
        super.miscSetUp();
        OBControl c = objectDict.get("letterswatch");
        letterHighColour = c.fillColor();
        OBLabel l = labels.get(0);
        letterLowColour = l.colour();
        loadEvent("strayletters");
        strayLetters = sortedFilteredControls("strayletter.*");
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList<>();
        events.add("c");
        events.add("d");
        events.add("e");
        int noscenes = targetLetters.size();
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        if(needDemo)
            events.add(0,"b");
        events.add(0,"a");
        doVisual(currentEvent());
    }

    public void setSceneXX(String scene)
    {

    }

    public void doAudio(String scene)throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", true);
    }

    public void tumbleOne(OBLabel lab,PointF endpos,List anims)
    {
        PointF startpos = lab.position();
        float offset = applyGraphicScale(140);
        if(endpos.x > startpos.x)
            offset = -offset;
        Path bez = OBUtils.SimplePath(startpos, endpos, offset);
        anims.add(OBAnim.pathMoveAnim(lab,bez,false,0));
        float angle = (float)(2.0 * Math.PI);
        if(endpos.x < bounds().width() / 2.0)
            angle = -angle;
        anims.add(OBAnim.rotationAnim(angle,lab));
    }

    public void tumbleOut() throws Exception
    {
        List<String> looseletters = OBUtils.randomlySortedArray(targetLetters);
        List<OBLabel> looselabels = new ArrayList<>();
        List<OBAnim> anims = new ArrayList<>();
        int i = 0;
        for(String lt : looseletters)
        {
            int idx = letters.indexOf(lt);
            OBLabel lab = labels.get(idx);
            looselabels.add(lab);
            OBControl strayletter = strayLetters.get(i);

            tumbleOne(lab,strayletter.position(),anims);
            i++;
        }
        lockScreen();
        for(OBLabel lab : looselabels)
            lab.setColour(letterHighColour);
        unlockScreen();
        waitForSecs(0.2f);
        playSfxAudio("rippleout",false);
        for(int j = 0;j < anims.size() /2;j++)
        {
            OBAnimationGroup.runAnims(anims.subList(j*2,j*2+2),0.6f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitForSecs(0.08f);
        }
        waitForSecs(0.8f);
        for(OBControl lab : looselabels)
        {
            PointF pt = new PointF();
            pt.set(lab.position());
            lab.setProperty("origpos",pt);
        }
        targets = new ArrayList<OBControl>(looselabels);
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        showLetters();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        tumbleOut();
        nextScene();
    }

    public void demob() throws Exception
    {
        waitForSecs(0.3f);
        String targetLetter = targetLetters.get(currNo);
        int idx = letters.indexOf(targetLetter);
        OBControl box = boxes.get(idx);
        OBPath rope = (OBPath) objectDict.get("ropeend");

        PointF destpt = OB_Maths.locationForRect(1.9f, 1f, rope.frame);
        PointF startpt = pointForDestPoint(destpt,15);
        startpt.y = (bounds().height() + 1);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-0.4f,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        OBLabel lab = labels.get(idx);
        PointF lpos = new PointF();
        lpos.set(lab.position());
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, lab .frame()),-0.7f,true);
        playSfxAudio("tap",true);
        waitForSecs(0.2f);
        moveObjects(Arrays.asList(lab,thePointer),box.position(),-0.5f,OBAnim.ANIM_EASE_IN_EASE_OUT);
        playSfxAudio("oneletterin",true);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",letterLowColour,lab)),0.3f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        movePointerToPoint(destpt,-0.6f,true);
        waitForSecs(0.2f);
        thePointer.hide();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",letterHighColour,lab)),0.3f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(0.2f);
        List anims = new ArrayList<>();
        tumbleOne(lab,lpos,anims);
        playSfxAudio("rippleout",false);
        OBAnimationGroup.runAnims(anims,0.6f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playAudioScene("DEMO",1,true);
        nextScene();
    }

    public int boxIndexForPt(PointF pt)
    {
        for(int i = 0;i < boxes.size();i++)
        {
            OBControl b = boxes.get(i);
            if (b.frame().contains(pt.x, pt.y))
                return i;
        }
        return -1;
    }

    public void letterSequenceForI(int idx) throws Exception
    {
        int startidx = idx - 1;
        if(startidx < 0)
            startidx = 0;
        else if(startidx > labels.size()  - 3)
            startidx = labels.size()  - 3;
        for(int i = startidx;i < startidx + 3;i++)
        {
            highlightBox(boxes.get(i),true);
            playAudioQueued(Arrays.asList((Object)String.format("alph_%s",letters.get(i))),true);
            waitForSecs(0.1f);
            highlightBox(boxes.get(i),false);
            waitForSecs(0.3f);
        }
    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            setStatus(STATUS_CHECKING);
            final OBSectionController fthis = this;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    int labidx = labels.indexOf(target);
                    int fidx = boxIndexForPt(pt);
                    int oidx = boxIndexForPt(target.position());
                    if(fidx == -1 && oidx == -1)
                    {
                        PointF destpos = (PointF) target.propertyValue("origpos");
                        moveObjects(Arrays.asList(target),destpos,-1.3f,OBAnim.ANIM_EASE_IN_EASE_OUT);
                        switchStatus(currentEvent());
                        return;
                    }
                    if(fidx == labidx || oidx == labidx)
                    {
                        playSfxAudio("oneletterin",false);
                        moveObjects(Arrays.asList(target),boxes.get(labidx).position(),-1.3f,OBAnim.ANIM_EASE_IN_EASE_OUT);
                        waitSFX();
                        waitForSecs(0.3f);
                        letterSequenceForI(labidx);
                        waitForSecs(0.3f);
                        gotItRightBigTick(true);
                        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.colourAnim("colour",letterLowColour,labels.get(labidx))),0.3f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,fthis);
                        targets.remove(labels.get(labidx));
                        nextScene();
                    }
                    else
                    {
                        gotItWrongWithSfx();
                        PointF destpos = (PointF) target.propertyValue("origpos");
                        moveObjects(Arrays.asList(target),destpos,-1.3f,OBAnim.ANIM_EASE_IN_EASE_OUT);
                        switchStatus(currentEvent());
                    }
                }
            });
            return;
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            PointF newpos = OB_Maths.AddPoints(pt, dragOffset);
            target.setPosition(newpos);
        }
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        targ.setZPosition(13);
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }


}
