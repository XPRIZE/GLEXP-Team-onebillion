package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 15/08/16.
 */
public class OC_Crdp extends OC_Wordcontroller
{
    public final static int NO_CARDS = 8;
    Map<String,OBPhoneme> wordDict;
    List<String> words;
    List<OBGroup> cards,backs,randomCards;
    PointF pos0,pos11;
    OBControl firstHit;
    float textSize,bigTextSize;
    OBGroup back;
    String prefix;

    public void miscSetUp()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = OBUtils.randomlySortedArray(Arrays.asList(ws.split(",")));
        currNo = 0;
        needDemo = false;
        String pd = parameters.get("demo");
        if(pd != null && pd.equals("true") )
            needDemo = true;
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        String evs;
        if(needDemo)
            evs = "a,b,d";
        else
            evs = "a,c";
        events = Arrays.asList(evs.split(","));
        doVisual(currentEvent());
    }

    public PointF position(int i)
    {
        int row = i / 4;
        int col = i % 4;
        return new PointF(pos0.x + col * ((pos11.x - pos0.x)/3), pos0.y + row *((pos11.y - pos0.y)/1));
    }


    public void cardStuff()
    {
        bigTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("bigtextsize")));
        Typeface tf = OBUtils.standardTypeFace();

        OBGroup bigcard = (OBGroup) objectDict.get("bigcard");
        bigcard.show();
        float sc = objectDict.get("front1").frame().width() / bigcard.frame.width();
        List crds = new ArrayList<>();
        for(int i = 0;i < NO_CARDS / 2;i++)
        {
            OBGroup c = (OBGroup) bigcard.copy();
            c.setPosition(position(i*2));
            String wordid = words.get(i);
            c.setProperty("wordid",wordid);
            OBWord rw = (OBWord) wordDict.get(wordid);
            OBControl im = loadImageWithName(rw.imageName,new PointF(0, 0),boundsf(),false);
            if(im != null)
            {
                c.insertMember(im,0,"im");
                OBControl picbox = c.objectDict.get("picbox");
                im.setPosition(picbox.position());
                im.setScale(picbox.height() / im.height());
                im.setZPosition(12);
                OBPath stroke2 = (OBPath) c.objectDict.get("stroke").copy();
                stroke2.setFillColor(0);
                c.insertMember(stroke2,0,"stroke2");
                stroke2.setZPosition(13);
                stroke2.sizeToBoundingBoxIncludingStroke();
                c.sizeToMember(stroke2);
            }
            OBGroup c2 = (OBGroup) c.copy();
            c2.setPosition(position(i*2+1));

            OBLabel lab = new OBLabel(rw.text,tf,bigTextSize);
            lab.setColour(Color.BLACK);
            c.insertMember(lab,-1,"lab");
            lab.setPosition(c.objectDict.get("textbox").position());
            lab.setZPosition(10);
            rw.properties.put("label",lab);
            c.setProperty("readingword",rw);

            OBWord rw2 = rw.copy();
            OBLabel lab2 = new OBLabel(rw2.text,tf,bigTextSize);
            lab2.setColour(Color.BLACK);
            c2.insertMember(lab2,-1,"lab");
            lab2.setPosition(c2.objectDict.get("textbox").position());
            lab2.setZPosition(10);
            rw2.properties.put("label",lab2);
            c2.setProperty("readingword",rw2);

            c.setScale(sc);
            c2.setScale(sc);
            crds.add(c);
            crds.add(c2);
        }
        cards = crds;
        for(OBGroup g : cards)
        {
            attachControl(g);
            g.setDoubleSided(false);
            g.setYRotation((float) Math.PI);
        }
        hideControls("front.*");
        randomCards = OBUtils.randomlySortedArray(cards);
        int i = 0;
        for(OBControl c : randomCards)
        {
            c.setPosition(position(i++));
        }
        OBControl back0 = back.renderedImageControl();
        back0.setZPosition(20);
        List bcks = new ArrayList<>();
        for(int j = 0;j < NO_CARDS;j++)
        {
            OBControl b = back0.copy();
            b.setPosition(randomCards.get(j).position());
            attachControl(b);
            b.setDoubleSided(false);
            bcks.add(b);
            b.setProperty("down",true);
        }
        backs = bcks;
    }

    public void setScenea()
    {
        bigTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("bigtextsize")));
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        pos0 = new PointF();
        pos0.set(objectDict.get("front1").position());
        pos11 = new PointF();
        pos11.set(objectDict.get("front2").position());
        back = (OBGroup) objectDict.get("back");
        OBPath strk = (OBPath) back.objectDict.get("stroke");
        strk.sizeToBoundingBoxIncludingStroke();
        back.sizeToMember(strk);
        //RectF r = convertRectFromControl(strk.bounds(),strk);
        //back.sizeToBox(RectFInset(r, -3, -3));
        detachControl(back);
        cardStuff();
        targets = new ArrayList<OBControl>(backs);
        hideControls("bigcard");
        for(OBControl c : targets)
            c.hide();
    }

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }


    public int indexOfObjectWithIdenticalWordIDTo(OBControl obj)
    {
        int i = 0;
        for(OBControl obj2 : randomCards)
        {
            if(obj2 != obj)
                if(obj2.propertyValue("wordid").equals(obj.propertyValue("wordid")))
            return i;
            i++;
        }
        return -1;
    }

    public void showStuff() throws Exception
    {
        for(OBControl c : OBUtils.randomlySortedArray(targets))
        {
            c.show();
            playSfxAudio("splat",false);
            waitForSecs(0.1f);
        }
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.4f);
        showStuff();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        nextScene();
    }

    public void demob() throws Exception
    {
        float maxx=0,maxy=0;
        for(OBControl c : backs)
        {
            float f;
            if((f = c.right()) > maxx)
                maxx = f;
            if((f = c.bottom()) > maxy)
                maxy = f;
        }
        PointF destpoint = new PointF();
        destpoint.x = ((maxx + bounds().width()) / 2);
        destpoint.y = ((maxy + bounds().height()) / 2);
        PointF startpoint = new PointF();
        startpoint.set(destpoint);
        startpoint.y = (bounds().height() + 2);
        startpoint.x += applyGraphicScale(20);
        loadPointerStartPoint(startpoint,destpoint);

        int chosenIdx = 5;
        OBControl back1 = backs.get(chosenIdx);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, back1.frame()),-1,true);
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(20),0.1f);
        playSfxAudio("tap",false);
        waitForSecs(0.1f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,1.1f, back1 .frame()),-1,true);
        animateCard(Arrays.asList(chosenIdx),false);
        waitForSecs(0.7f);
        int chosenIdx2 = indexOfObjectWithIdenticalWordIDTo(randomCards.get(chosenIdx));
        OBControl back2 = backs.get(chosenIdx2);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, back2 .frame()),-1,true);
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(20),0.1f);
        playSfxAudio("tap",false);
        waitForSecs(0.1f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,1.1f, back2 .frame()),-1,true);
        animateCard(Arrays.asList(chosenIdx2),false);
        waitForSecs(0.7f);

        movePointerToPoint(destpoint,-1,true);

        targets.remove(back1);
        targets.remove(back2);
        animateMatchCard1(randomCards.get(chosenIdx),randomCards.get(chosenIdx2));
        waitForSecs(1f);
        flyAwayCard1(randomCards.get(chosenIdx));

        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO2",false);
        nextScene();
    }

    public void animateCard(final List<Integer> ixs,final boolean down)
    {
        OBUtils.runOnOtherThreadDelayed(0.3f,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                playSfxAudio("flip",false);
            }
        });
        for (int i : ixs)
        {
            OBControl card = randomCards.get(i);
            card.setProperty("origscale",(card.scale()));
        }
        final float Z_DIST = applyGraphicScale(1400);
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for (int i : ixs)
                {
                    OBControl card = randomCards.get(i);
                    OBControl backi = backs.get(i);
                    backi.setProperty("down",true);
                    float ang;
                    if(down)
                        ang = (float)Math.PI * frac;
                    else
                        ang = (float)(Math.PI + Math.PI * frac);
                    float sc = (Float)card.propertyValue("origscale");
                    card.m34 = (float)(1.0 / -Z_DIST);
                    card.setYRotation(ang);

                    if(down)
                        ang = (float)(Math.PI + Math.PI * frac);
                    else
                        ang = (float)Math.PI * frac;
                    backi.m34 = (float)(1.0 / -Z_DIST);
                    backi.setYRotation(ang);
                }
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(anim),0.7f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        for (int i : ixs)
        {
            OBControl backi = backs.get(i);
            backi.setProperty("down",true);
        }
     }

    public void nextPair(OBGroup card1) throws Exception
    {
        if(targets.size()  == 0)
        {
            lockScreen();
            card1.setZPosition(50);
            unlockScreen();
            if(!performSel("endOfScene",currentEvent()))
            {
                waitAudio();
                waitForSecs(0.5f);
                nextScene();
            }
        }
        else
        {
            flyAwayCard1(card1);
            switchStatus(currentEvent());
        }
    }

    public void flyAwayCard1(final OBGroup card1) throws Exception
    {
        playSfxAudio("flyaway",false);
        final PointF startpos = card1.position();
        final PointF offpos = OB_Maths.locationForRect(0.1f + (float)OB_Maths.rndom() * 0.8f, -1, boundsf());
        final float zrot = (float) Math.toRadians(35);
        final float yrot = (float) Math.toRadians(60);
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)
                new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        card1.setPosition(OB_Maths.tPointAlongLine(frac, startpos, offpos));
                        float zang = zrot * frac;
                        float yang = yrot * frac;
                        card1.m34 = (1.0f / applyGraphicScale(-700));
                        card1.yRotation = yang;
                        card1.rotation = zang;
                    }
                }
        ),1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);


        lockScreen();
        card1.hide();
        unlockScreen();

    }

    public void animateMatchCard1(OBGroup card1,OBGroup card2) throws Exception
    {
        lockScreen();
        card1.setZPosition(1051);
        card2.setZPosition(50);
        unlockScreen();
        PointF pos = objectDict.get("bigcard").position();
        OBAnimationGroup.runAnims(Arrays.asList(
                OBAnim.moveAnim(pos,card1),
                OBAnim.moveAnim(pos,card2)),
                0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        lockScreen();
        card2.hide();
        unlockScreen();

        playSfxAudio("flybig",false);
        OBAnimationGroup.runAnims(Arrays.asList(
                OBAnim.scaleAnim(1.0f,card1)),0.4f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        OBWord rw = (OBWord) card1.propertyValue("readingword");
        highlightAndSpeakSyllablesForWord(rw);
    }

    public void checkTarget2(Object targ)
    {
        if(targ == firstHit)
            return;
        setStatus(STATUS_CHECKING);
        List saveReplay = emptyReplayAudio();
        try
        {
            int idx = backs.indexOf(targ);
            int firstIdx = backs.indexOf(firstHit);
            playSfxAudio("tap",false);
            //waitForSecs(0.1f);
            animateCard(Arrays.asList(idx),false);
            waitForSecs(0.71f);
            if(randomCards.get(idx).propertyValue("wordid").equals(randomCards.get(firstIdx).propertyValue("wordid")))
            {
                gotItRightBigTick(false);
                targets.remove(targ);
                targets.remove(firstHit);
                animateMatchCard1(randomCards.get(idx),randomCards.get(firstIdx));
                waitForSecs(1f);
                firstHit = null;
                nextPair(randomCards.get(idx));
            }
            else
            {
                List<Integer> ixs = new ArrayList<>(Arrays.asList(idx));
                ixs.add(firstIdx);
                animateCard(ixs,true);
                waitAudio();
                setReplayAudio(saveReplay);
                firstHit = null;
                switchStatus(currentEvent());
            }
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
            firstHit = (OBControl) targ;
            int idx = backs.indexOf(targ);
            playSfxAudio("tap",false);
            animateCard(Arrays.asList(idx),false);
            setReplayAudio(saveReplay);
            setStatus(STATUS_AWAITING_CLICK2);
        }
        catch(Exception exception)
        {
        }
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
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
        else if(status()  == STATUS_AWAITING_CLICK2)
        {
            final Object obj = findTarget(pt);
            if(obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget2(obj);
                    }
                });
            }
        }
    }

}
