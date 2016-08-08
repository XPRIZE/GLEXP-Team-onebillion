package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.SpannableString;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBConditionLock;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 08/08/16.
 */
public class X_Wm1 extends X_Wordcontroller
{
    public final static int MSE_DN = 0,
        MSE_UP = 1;
    Map<String,OBPhoneme> wordDict;
    List<List>wordSets;
    OBPath wordLine,wordsRect;
    float picScale;
    PointF picPosition,dashTargetPosition;
    String currWordID;
    int textColour;
    List<OBLabel> labels = new ArrayList<>();
    float textSize;
    OBConditionLock finishLock;


    public void miscSetUp()
    {
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        String[] wss = ws.split(";");
        wordSets = new ArrayList<>();
        for(String w : wss)
            wordSets.add(Arrays.asList(w.split(",")));
        currNo = 0;
        OBPath textswatch = (OBPath) objectDict.get("textswatch");
        textColour = textswatch.fillColor();
        needDemo = parameters.get("demo") != null && parameters.get("demo").equals("true");
        OBControl pic = objectDict.get("pic");
        picScale = pic.scale();
        picPosition = pic.position();
        deleteControls("pic");
        wordsRect = (OBPath) objectDict.get("wordsrect");
        wordLine = (OBPath) objectDict.get("wordline");

        dashTargetPosition = new PointF();
        finishLock = new OBConditionLock();
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
        events.addAll(Arrays.asList("c,d,e".split(",")));
        while(events.size()  > wordSets.size())
            events.remove(events.size()-1);
        while(events.size()  < wordSets.size())
            events.add(events.get(events.size()-1));
        events.add(0,"a");
        doVisual(currentEvent());
    }

    public OBLabel setUpLabel(String tx)
    {
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(tx,tf,textSize);
        label.setColour(Color.BLACK);
        return label;
    }

    public void distributeLabels(List<OBLabel>labs,RectF r)
    {
        int gaps = labs.size()  + 1;
        float gap = r.height();
        for(OBLabel l : labs)
            gap -= l.height();
        gap = gap / gaps;
        float y = r.top + gap;
        for(OBLabel l : labs)
        {
            l.setTop(y);
            y = l.bottom();
            y += gap;
        }
        float maxwidth = 0;
        for(OBLabel l : labs)
            if(l.width() > maxwidth)
                maxwidth = l.width();
        gap = r.width() - maxwidth;
        float lmargin = r.left + gap / 2;
        for(OBLabel l : labs)
        {
            l.setLeft(lmargin);
            l.setZPosition(10);
            l.setColour(textColour);
            attachControl(l);
            l.hide();
            l.setProperty("origpos",new PointF(l.position().x,l.position().y));
        }
    }

    public void setScenea()
    {

    }

    public static float baselineOffsetForText(String tx,Typeface ty,float textsize)
    {
        TextPaint tp = new TextPaint();
        tp.setTextSize(textsize);
        tp.setTypeface(ty);
        tp.setColor(Color.BLACK);
        SpannableString ss = new SpannableString(tx);
        StaticLayout sl = new StaticLayout(ss,tp,4000, Layout.Alignment.ALIGN_NORMAL,1,0,false);
        return sl.getLineBaseline(0);
    }

    public void setSceneXX(String  scene)
    {
        for(OBControl c : labels)
            detachControl(c);
        deleteControls("pic");
        List<String>warr = wordSets.get(currNo);
        currWordID = warr.get(0);
        OBWord orw = (OBWord) wordDict.get(currWordID);
        OBImage im = OBImageManager.sharedImageManager().imageForName(orw.imageName);
        im.setScale(picScale);
        im.setPosition(picPosition);
        objectDict.put("pic",im);
        attachControl(im);
        im.setRight(-1);
        List labs = new ArrayList<>();
        for(String wid : warr)
        {
            OBWord rw = (OBWord) wordDict.get(wid);
            labs.add(setUpLabel(rw.text));
        }
        distributeLabels(OBUtils.randomlySortedArray(labs),wordsRect.frame());
        labels = labs;
        if(dashTargetPosition.equals(new PointF()))
        {
            float h = baselineOffsetForText("My",OBUtils.standardTypeFace(),textSize);
            dashTargetPosition = wordLine.position();
            dashTargetPosition.y = wordLine.top() + h + applyGraphicScale(12);
        }
        targets = (List<OBControl>)(Object)labels;
    }

    public void showStuff() throws Exception
    {
        waitForSecs(0.3f);
        playSfxAudio("slideon",false);
        moveObjects(Arrays.asList(objectDict.get("pic")),picPosition,-1,OBAnim.ANIM_EASE_OUT);
        playSfxAudio("wordson",false);
        List<OBLabel>labs = new ArrayList<>(labels);
        Collections.sort(labs, new Comparator<OBLabel>()
        {
            @Override
            public int compare (OBLabel obj1, OBLabel obj2)
            {
                if(obj1.position().y < obj2.position().y)
                    return -1;
                if(obj1.position().y > obj2.position().y)
                    return 1;
                return 0;
            }
        });
        for(OBLabel lab : labs)
        {
            lab.show();
            waitForSecs(0.2f);
        }
        waitSFX();
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        nextScene();
    }

    public void highlightDash(boolean high)
    {
        int col = high?Color.RED :Color.BLACK;
        wordLine.setStrokeColor(col);
    }

    public void doMainXX() throws Exception
    {
        showStuff();
        waitForSecs(0.2f);
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        List audio = currentAudio("PROMPT");
        if(audio != null)
            playAudioQueued(OBUtils.insertAudioInterval(audio,300),false);
    }

    public void doMainc() throws Exception
    {
        showStuff();
        waitForSecs(0.2f);
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));

        PointF destpt = OB_Maths.locationForRect(0.5f, 1.1f, wordsRect.frame);
        PointF startpt = pointForDestPoint(destpt,15);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-0.8f,true);
        playAudioScene("PROMPT",0,true);
        movePointerToPoint(OB_Maths.locationForRect(0.5f, 1.1f, wordLine.frame),-1,true);
        playAudioScene("PROMPT",1,true);
        thePointer.hide();
    }

    public void endBody()
    {
        List reminderAudio = currentAudio("PROMPT.REMINDER");
        if(reminderAudio != null)
        {
            try
            {
                long sttime = statusTime;
                waitForSecs(0.3f);
                waitAudio();
                if(sttime == statusTime)
                    reprompt(sttime,reminderAudio,6);

            }
            catch(Exception e)
            {

            }
         }
    }

    public void placeLabel(OBLabel label) throws Exception
    {
        moveObjects(Arrays.asList((OBControl)label),dashTargetPosition,-0.8f,OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.1f);
        highlightDash(true);
        playSfxAudio("wordonline",true);
    }

    public void clearOff() throws Exception
    {
        playSfxAudio("bigoff",false);
        lockScreen();
        for(OBControl c : labels)
            detachControl(c);
        deleteControls("pic");
        highlightDash(false);
        wordLine.setOpacity(1);
        unlockScreen();
        waitForSecs(0.1f);
        waitSFX();
    }

    public void demob() throws Exception
    {
        showStuff();
        waitForSecs(0.3f);
        PointF destpt = OB_Maths.locationForRect(0.6f, 0.8f, labels.get(0).frame());
        PointF startpt = pointForDestPoint(destpt,15);
        loadPointerStartPoint(startpt,destpt);
        movePointerForwards(applyGraphicScale(80),-1);
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        movePointerToPoint(destpt,-0.8f,true);
        waitForSecs(0.3f);
        List<OBControl> lst = Arrays.asList(labels.get(0),thePointer);
        moveObjects(lst,dashTargetPosition,-0.8f,OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.1f);
        highlightDash(true);
        playSfxAudio("wordonline",false);
        movePointerForwards(applyGraphicScale(-80),-1);
        OBAnimationGroup.runAnims(Arrays.asList(
                OBAnim.opacityAnim(0.0f,wordLine) ,
                OBAnim.scaleAnim(1.3f,labels.get(0)) ,
                OBAnim.colourAnim("colour",Color.BLACK,labels.get(0)) ,
                OBAnim.opacityAnim(0.4f,labels.get(1)) ,
                OBAnim.opacityAnim(0.4f,labels.get(2))),
        0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(0.3f);
        highlightLabel(labels.get(0),true);
        playAudioQueued(Arrays.asList((Object)currWordID) ,true);
        highlightLabel(labels.get(0),false);
        waitForSecs(0.7f);
        thePointer.hide();
        waitForSecs(0.3f);
        playAudioScene("DEMO2",1,true);
        waitForSecs(0.3f);
        nextScene();
    }

    public void nextScene()
    {
        if(++eventIndex >= events.size() )
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    fin();
                }
            });
        else
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        if(labels.size()  > 0)
                        {
                            clearOff();
                            currNo++;
                        }
                    }
                    catch(Exception exception)
                    {
                    }
                    setScene(events.get(eventIndex));
                }
            });
    }

    public void goHome()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                PointF pt = (PointF) target.propertyValue("origpos");
                moveObjects(Arrays.asList(target),pt,-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
                switchStatus(currentEvent());
                target = null;
            }
        });
    }

    public void stage2Check()
    {
        finishLock.lockWhenCondition(MSE_UP);
        finishLock.unlock();
        if(target == labels.get(0))
        {
            try
            {
                OBAnimationGroup.runAnims(Arrays.asList(
                        OBAnim.opacityAnim(0.0f,wordLine) ,
                        OBAnim.scaleAnim(1.3f,labels.get(0)) ,
                        OBAnim.colourAnim("colour",Color.BLACK,labels.get(0)) ,
                        OBAnim.opacityAnim(0.4f,labels.get(1)) ,
                        OBAnim.opacityAnim(0.4f,labels.get(2))),
                        0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
                waitForSecs(0.3f);
                highlightLabel(labels.get(0),true);
                playAudioQueued(Arrays.asList((Object)currWordID) ,true);
                highlightLabel(labels.get(0),false);
                waitForSecs(0.3f);
                gotItRightBigTick(true);
                waitForSecs(0.7f);
                nextScene();
            }
            catch(Exception e)
            {

            }
        }
        else
        {
            gotItWrongWithSfx();
            highlightDash(false);
            goHome();
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        finishLock.lock();
        finishLock.unlockWithCondition(MSE_UP);
        if(status()  == STATUS_DRAGGING)
        {
            setStatus(STATUS_CHECKING);
            RectF r = new RectF(wordLine.frame);
            RectF tr = new RectF(target.frame);
            r.top -= tr.height();
            if(r.intersect(tr))
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        placeLabel((OBLabel)target);
                        stage2Check();
                    }
                });
            }
            else
            {
                goHome();
            }
            return;
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
        {
            PointF newpos = OB_Maths.AddPoints(pt, dragOffset);
            target.setPosition(newpos);

            if(target.frame.contains(wordLine.position().x, wordLine.position().y))
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        try
                        {
                            placeLabel((OBLabel)target);
                            stage2Check();
                        }
                        catch(Exception exception) {
                        }
                    }
                });
            }
        }
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        targ.setZPosition(13);
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
        finishLock.lock();
        finishLock.unlockWithCondition(MSE_DN);
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

