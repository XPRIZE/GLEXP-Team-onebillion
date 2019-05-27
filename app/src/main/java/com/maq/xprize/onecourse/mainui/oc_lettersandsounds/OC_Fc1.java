package com.maq.xprize.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBImage;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OBSectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OBWord;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 03/08/16.
 */
public class OC_Fc1 extends OC_Wordcontroller
{
    final static int STATUS_AWAITING_CLICK3 = 1010;
    final static float Z_DIST = 1400;
    float zDist;
    Map<String,OBPhoneme> wordDict;
    List<String> words;
    float textSize;
    OBGroup cardBack,cardFront;
    PointF cardPos;
    boolean inFinalSection;
    String prefix;
    OBWord currWord;

    public void miscSetUp()
    {
        zDist = applyGraphicScale(Z_DIST);
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = OBUtils.randomlySortedArray(Arrays.asList(ws.split(",")));
        currNo = 0;
        cardStuff();
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList<String>(Arrays.asList("a,b,c".split(",")));
        String nc = parameters.get("nocards");
        int noscenes = 0;
        if (nc != null)
            noscenes = Integer.parseInt(nc);
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        events.remove(events.size()-1);
        events.add("d");
        doVisual(currentEvent());
    }

    public void setUpCard(int wno)
    {
        Typeface tf = OBUtils.standardTypeFace();
        cardBack.setYRotation(0);
        cardFront.setYRotation(0);
        cardFront.setPosition(cardPos);
        if(inFinalSection)
        {
            cardBack.setRight(-2);
            cardFront.setRight(-2);
        }
        else
        {
            cardFront.setYRotation((float)Math.PI);
            cardBack.setLeft(bounds().width() + 2);
        }
        cardFront.setDoubleSided(false);
        cardBack.setDoubleSided(false);
        String wordId = words.get(wno);
        currWord = (OBWord) wordDict.get(wordId);

        OBImage im = (OBImage) cardFront.objectDict.get("im");
        if(im != null)
            cardFront.removeMember(im);
        OBLabel lab = (OBLabel) cardFront.objectDict.get("lab");
        if(lab != null)
            cardFront.removeMember(lab);

        lab = new OBLabel(currWord.text,tf,textSize);
        lab.setColour(Color.BLACK);
        cardFront.insertMember(lab,-1,"lab");
        lab.setPosition(cardFront.objectDict.get("textbox").position());
        lab.setZPosition(10);
        if(!inFinalSection)
            lab.hide();
        currWord.properties.put("label",lab);
        im = loadImageWithName(currWord.imageName,new PointF(0,0),new RectF(bounds()),false);
        if(im != null)
        {
            OBControl picBox =(OBControl)cardFront.objectDict.get("picbox");
            im.setScale(picBox.bounds.width() / im.bounds.width());
            cardFront.insertMember(im,-1,"im");
            im.setPosition(picBox.position());
            im.setZPosition(12);
        }
    }

    public void cardStuff()
    {
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        cardBack = (OBGroup) objectDict.get("cardback");
        /*OBControl backfront = cardBack.filterMembers("back").get(0);
        RectF r = convertRectFromControl(backfront.bounds(),backfront);
        r.inset(-4,-4);
        cardBack.sizeToBox(r);*/
        cardBack.setDoubleSided(false);

        cardFront = (OBGroup) objectDict.get("cardfront");
        cardPos = new PointF(cardFront.position().x,cardFront.position().y);
        RectF f = new RectF(cardFront.bounds());
        f.inset(-4,-4);
        cardFront.sizeToBox(f);
        cardFront.setDoubleSided(false);

        OBPath stroke2 = (OBPath) cardFront.objectDict.get("stroke").copy();
        stroke2.setFillColor(0);
        cardFront.insertMember(stroke2,-2,"stroke2");
        stroke2.setZPosition(13);
        cardFront.objectDict.get("stroke").hide();

    }

    public void setSceneXX(String  scene)
    {
        setUpCard(currNo);
        targets = Collections.singletonList((OBControl)cardBack);
    }

    public void doAudio(String  scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }
    public void doMainXX() throws Exception
    {
        flyOnCard();
        waitForSecs(0.2f);
        doAudio(currentEvent());
    }

    public void flyOnCard() throws Exception
    {
        OBControl card = inFinalSection?cardFront:cardBack;
        if(card.position().x < 0 || card.position().x > bounds().width())
        {
            playSfxAudio("slide",false);
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(cardPos,card)),0.4,true,OBAnim.ANIM_EASE_OUT,this);
        }
    }

    public void flyOffCard() throws Exception
    {
        PointF pt = new PointF(0,cardFront.position().y);
        playSfxAudio("slide",false);
        if(inFinalSection)
            pt.x =((cardFront.width()/2) + 2 + bounds().width());
        else
            pt.x = -(cardFront.width()/2  - 2);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(pt,cardFront)),0.4,true,OBAnim.ANIM_EASE_IN,this);
    }

    public void highlightWord(OBWord w,boolean h,boolean withBackground)
    {
        lockScreen();
        OBLabel label = (OBLabel) w.properties.get("label");
        if(h)
        {
            label.setColour(Color.RED);
            if(withBackground)
                label.setBackgroundColor(Color.argb(255,255,243,243));
        }
        else
        {
            label.setColour(Color.BLACK);
            if(withBackground)
                label.setBackgroundColor(0);
        }
        unlockScreen();
    }

    public void highlightAndSpeakWord(OBWord w,String wordID)
    {
        try
        {
            lockScreen();
            highlightWord(w,true,false);
            unlockScreen();
            playAudioQueued(Collections.singletonList((Object)wordID) ,true);
            lockScreen();
            highlightWord(w,false,false);
            unlockScreen();
        }
        catch(Exception exception)
        {
        }
    }

    public void fin()
    {
        try
        {
            for(int i = currNo - 1;i >= 0;i--)
            {
                String wordID = words.get(i);
                lockScreen();
                setUpCard(i);
                unlockScreen();
                flyOnCard();
                waitForSecs(0.3f);
                highlightAndSpeakWord(currWord,wordID);
                if(i > 0)
                    flyOffCard();
            }
            waitForSecs(0.4f);
            super.fin();
        }
        catch (Exception e)
        {

        }
    }

    public void cancelAnimationsWithKeys(List<String> animkeys)
    {
        for(String k : animkeys)
        {
            OBAnimationGroup gp = animations.get(k);
            gp.flags = (gp.flags | OBAnimationGroup.ANIM_CANCEL);
            animations.remove(k);
        }
    }

    public void cancelAllAnimations()
    {
        List<String>l = new ArrayList<>(animations.keySet());
        cancelAnimationsWithKeys(l);
    }

    public void deployPulseAnim()
    {
        final long sttime = statusTime;
        final OBPath path = (OBPath) cardFront.filterMembers("stroke2").get(0);
        final OBSectionController fthis = this;
        OBUtils.runOnOtherThreadDelayed(3,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(statusTime  == sttime)
                {
                    OBAnim a1 = OBAnim.colourAnim("strokeColor",Color.argb(255,253,187,9),path);
                    OBAnim a2 = OBAnim.colourAnim("strokeColor",Color.BLACK,path);
                    OBAnimationGroup gp = new OBAnimationGroup();
                    registerAnimationGroup(gp,"anim");
                    List<OBAnim> lob0 = Arrays.asList((OBAnim)a1);
                    List<OBAnim> lob1 = Arrays.asList((OBAnim)a2);
                    OBAnimationGroup.chainAnimations(Arrays.asList(lob0,lob1),Arrays.asList(0.6f,0.6f),true,Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT),2,fthis);
                    path.setStrokeColor(Color.BLACK);
                }
            }
        });
    }

    public void animateCardDown(final boolean down) throws Exception
    {
        playSfxAudio("flip",false);
        OBAnimBlock bloc = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                OBControl card = cardFront;
                OBControl back = cardBack;
                float ang;
                if(down)
                    ang = (float)Math.PI * frac;
                else
                    ang = (float)Math.PI + (float)Math.PI * frac;
                card.m34 = (1.0f / -zDist);
                card.setYRotation(ang);

                if(down)
                    ang = (float)Math.PI + (float)Math.PI * frac;
                else
                    ang = (float)Math.PI * frac;
                back.m34 = (1.0f / -zDist);
                back.setYRotation(ang);
            }
        };
        OBAnimationGroup.runAnims(Collections.singletonList((OBAnim)bloc),0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.5f);
        flyOnCard();
        waitForSecs(0.5f);


        PointF destpt = OB_Maths.locationForRect(0.8f,0.8f, cardBack.frame());
        PointF startpt = pointForDestPoint(destpt,35);
        startpt.y += 5;
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.3f, startpt, destpt),-0.8f,true);

        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        movePointerToPoint(destpt,-1,true);
        playSfxAudio("tap",false);
        waitForSecs(0.05f);
        movePointerForwards(applyGraphicScale(-120),-1);
        waitSFX();
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);

        animateCardDown(false);
        waitForSecs(0.1f);

        playAudioScene("DEMO",2,true);
        movePointerToPoint(destpt,-1,true);
        playSfxAudio("tap",false);
        waitForSecs(0.05f);
        movePointerForwards(applyGraphicScale(-120),-1);
        waitSFX();
        waitForSecs(0.3f);

        cardFront.objectDict.get("lab").show();

        highlightAndSpeakSyllablesForWord(currWord);
        waitForSecs(1f);


        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        movePointerToPoint(destpt,-1,true);
        playSfxAudio("tap",false);
        waitForSecs(0.05f);
        movePointerForwards(applyGraphicScale(-120),-1);
        waitSFX();
        waitForSecs(0.3f);

        waitForSecs(0.3f);
        flyOffCard();
        waitForSecs(0.3f);
        thePointer.hide();
        nextScene();
    }

    public void nextScene()
    {
        if(++eventIndex >= events.size() )
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        inFinalSection = true;
                        flyOffCard();
                        fin();
                    }
                    catch(Exception exception)
                    {
                    }

                }
            });
        else
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        flyOffCard();
                        currNo++;
                        setScene(events.get(eventIndex));
                    }
                    catch(Exception exception)
                    {
                    }
                }
            });
    }

    public void checkTarget3(Object targ)
    {
        setStatus(STATUS_CHECKING);
        emptyReplayAudio();
        try
        {
            cancelAllAnimations();
            playSfxAudio("tap",false);
            waitSFX();
            if(eventIndex == events.size()  - 1)
                inFinalSection = true;
            nextScene();
        }
        catch(Exception exception)
        {
        }
    }

    public void checkTarget2(Object targ)
    {
        setStatus(STATUS_CHECKING);
        List saveReplay = emptyReplayAudio();
        try
        {
            cancelAllAnimations();
            playSfxAudio("tap",false);
            cardFront.objectDict.get("lab").show();
            waitSFX();
            waitForSecs(0.5f);
            String wordID = words.get(currNo);
            highlightAndSpeakSyllablesForWord(currWord);
            setStatus(STATUS_AWAITING_CLICK3);
            setReplayAudio(saveReplay);
            deployPulseAnim();
        }
        catch(Exception exception)
        {
        }
    }

    public void checkTarget(Object targ)
    {
        setStatus(STATUS_CHECKING);
        List saveReplay = emptyReplayAudio();
        try
        {
            playSfxAudio("tap",false);
            waitSFX();
            animateCardDown(false);
            setReplayAudio(saveReplay);
            setStatus(STATUS_AWAITING_CLICK2);
            deployPulseAnim();
        }
        catch(Exception exception)
        {
        }
    }


    public void touchDownAtPoint(PointF pt,View v)
    {
        final Object obj = findTarget(pt);
        if(obj != null)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    if(status()  == STATUS_AWAITING_CLICK)
                        checkTarget(obj);
                    else if(status()  == STATUS_AWAITING_CLICK2)
                        checkTarget2(obj);
                    else if(status()  == STATUS_AWAITING_CLICK3)
                        checkTarget3(obj);
                }
            });
        }
    }

}
