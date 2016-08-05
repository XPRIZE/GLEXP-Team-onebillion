package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBReadingWord;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 04/08/16.
 */
public class X_Crdw extends X_Wordcontroller
{
    public static int NO_CARDS = 12;
    Map<String,OBPhoneme> wordDict;
    List<String> words;
    List<OBGroup>cards,randomCards;
    PointF pos0,pos11;
    OBGroup firstHit;
    float textSize,bigTextSize;
    int highlightColour;

    public void miscSetUp()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = OBUtils.randomlySortedArray(Arrays.asList(ws.split(",")));
        currNo = 0;
        needDemo = false;
        String s = parameters.get("demo");
        if(s != null && s.equals("true"))
            needDemo = true;
        OBPath swatch = (OBPath) objectDict.get("highswatch");
        highlightColour = swatch.fillColor();
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        String evs;
        if(needDemo)
            evs = "a,b,d,e,f,f,f";
        else
            evs = "a,d,e,f,f,f,f";
        events = new ArrayList<String>(Arrays.asList(evs.split(",")));
        doVisual(currentEvent());
    }

    public PointF position(int i)
    {
        int row = i / 3;
        int col = i % 3;
        float x = pos0.x + col * ((pos11.x - pos0.x)/2f);
        float y = pos0.y + row *((pos11.y - pos0.y)/3f);
        return new PointF(x,y);
    }



    public void cardStuff()
    {
        bigTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("bigtextsize")));
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        Typeface tf = OBUtils.standardTypeFace();
        pos0 = objectDict.get("front1").position();
        pos11 = objectDict.get("front2").position();
        OBControl front = objectDict.get("front1");
        List crds = new ArrayList<OBControl>();
        for(int i = 0;i < NO_CARDS / 2;i++)
        {
            OBControl c = front.copy();
            c.setPosition(position(i*2));
            String wordid = words.get(i);
            OBWord rw = (OBWord) wordDict.get(wordid);
            String word = rw.text;
            OBLabel lab = new OBLabel(word,tf,textSize);
            lab.setColour(Color.BLACK);
            lab.setPosition(c.position());
            rw.properties.put("label",lab);
            OBControl c2 = c.copy();
            c2.setPosition(position(i*2+1));
            OBLabel lab2 = (OBLabel) lab.copy();
            lab2.setPosition(c2.position());
            OBWord rw2 = rw.copy();
            rw2.properties.put("label",lab2);
            lab.setZPosition(5);
            lab2.setZPosition(5);
            OBGroup g = new OBGroup(Arrays.asList(c,lab));
            g.objectDict.put("stroke",c);
            g.objectDict.put("label",lab);
            g.setProperty("wordid",wordid);
            g.setProperty("readingword",rw);
            OBGroup g2 = new OBGroup(Arrays.asList(c2,lab2));
            g2.objectDict.put("stroke",c2);
            g2.objectDict.put("label",lab2);
            g2.setProperty("wordid",wordid);
            g2.setProperty("readingword",rw2);
            crds.add(g);
            crds.add(g2);
            attachControl(g);
            attachControl(g2);
        }
        cards = crds;
        deleteControls("front.*");
        randomCards = OBUtils.randomlySortedArray(cards);
        int i = 0;
        for(OBControl c : randomCards)
        {
            c.setPosition(position(i++));
        }
    }

    public void getOrigPathAttrs(OBGroup obj)
    {
        OBPath path = (OBPath) obj.objectDict.get("stroke");
        Map<String,Object> attrs = path.attributes();
        float x = Float.parseFloat((String) attrs.get("x"));
        float y = Float.parseFloat((String) attrs.get("y"));
        float w = Float.parseFloat((String) attrs.get("width"));
        float h = Float.parseFloat((String) attrs.get("height"));
        RectF f = OB_Maths.denormaliseRect(new RectF(x,y,x+w,y+h),new RectF(bounds()));
        if(attrs.get("widthtracksheight") != null && attrs.get("widthtracksheight").equals("true"))
        {
            float origheight = Float.parseFloat((String) attrs.get("pxheight"));
            if(origheight > 0)
            {
                float ratio = f.height() / origheight;
                float newWidth = Float.parseFloat((String) attrs.get("pxwidth")) * ratio;
                float diff = newWidth - f.width();
                f.right += diff / 2;
                f.left -= diff / 2;
            }
        }
        f = convertRectFromControl(path.bounds(),path);
        obj.setProperty("strokerect",f);
        if(attrs.get("cornerradius") != null)
        {
            float cr = Float.parseFloat((String) attrs.get("cornerradius"));
            cr *= f.height();
            obj.setProperty("cornerradius",cr);
        }
    }

    public void setScenea()
    {
        cardStuff();
        targets = (List<OBControl>)(Object)cards;
        objectDict.get("bigcard").hide();
        getOrigPathAttrs((OBGroup) objectDict.get("bigcard"));
        for(OBControl c : targets)
            c.hide();
    }

    public void showStuff() throws Exception
    {
        for(OBControl c : OBUtils.randomlySortedArray(targets))
        {
            c.show();
            playSfxAudio("splat",false);
            waitForSecs(0.09f);
        }
    }

    public void doAudio(String  scene) throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }
    public void doMainXX() throws Exception
    {
        waitForSecs(0.2f);
        doAudio(currentEvent());
    }

    public void demoa() throws Exception
    {
        waitForSecs(0.3f);
        showStuff();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        nextScene();
    }

    public void highlightCard(OBGroup card,boolean high)
    {
        int col = high?highlightColour:Color.WHITE;
        OBPath p = (OBPath) card.objectDict.get("stroke");
        p.setFillColor(col);
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

    public void demob() throws Exception
    {
        waitForSecs(0.3f);
        float maxx=0,maxy=0;
        for(OBControl c : cards)
        {
            float f;
            if((f = c.right()) > maxx)
                maxx = f;
            if((f = c.bottom()) > maxy)
                maxy = f;
        }
        PointF destpoint = new PointF();
        destpoint.x = (maxx + bounds().width()) / 2;
        destpoint.y = (maxy + bounds().height()) / 2;
        PointF startpoint = new PointF(destpoint.x,0);
        startpoint.y = (bounds().height() + 2);
        startpoint.x += applyGraphicScale(20);
        loadPointerStartPoint(startpoint,destpoint);

        playAudioQueuedScene("DEMO",true);

        OBGroup card = randomCards.get(7);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, card .frame()),-1,true);
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(20),0.1f);
        playSfxAudio("tap",false);
        highlightCard(card,true);
        waitForSecs(0.1f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,1.1f, card .frame()),-1,true);
        waitForSecs(0.5f);

        int chosenIdx2 = indexOfObjectWithIdenticalWordIDTo(randomCards.get(7));
        OBGroup card2 = randomCards.get(chosenIdx2);
        movePointerToPoint(OB_Maths.locationForRect(0.7f, 0.7f, card2 .frame()),-1,true);
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(20),0.1f);
        playSfxAudio("tap",false);
        highlightCard(card2,true);
        waitForSecs(0.1f);
        movePointerToPoint(destpoint,-1,true);

        animateMatchCard1(card,card2);
        waitForSecs(0.7f);

        thePointer.hide();
        playAudioQueuedScene("DEMO2",true);
        nextScene();
    }

    public void animateMatchCard1(OBGroup card1,OBGroup card2) throws Exception
    {
        lockScreen();
        card1.setZPosition(50);
        card2.setZPosition(50);
        unlockScreen();
        OBAnim anim1 = new OBAnim(card1,"bottomPoint",OBAnim.ANIM_TYPE_POINT);
        OBAnim anim2 = new OBAnim(card2,"bottomPoint",OBAnim.ANIM_TYPE_POINT);
        OBGroup bigCard = (OBGroup) objectDict.get("bigcard").copy();
        objectDict.put("bigcardc",bigCard);
        attachControl(bigCard);
        anim1.value = (bigCard.bottomPoint());
        anim2.value = (bigCard.bottomPoint());
        OBAnimationGroup.runAnims(Arrays.asList(anim1,anim2),0.5f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        getOrigPathAttrs(card1);

        final OBLabel label = (OBLabel) card1.objectDict.get("label");
        lockScreen();
        bigCard.insertMember(label,-1,"label");
        label.setZPosition(55);
        unlockScreen();
        final PointF labelstartpos = label.position();
        final PointF labelendpos = bigCard.objectDict.get("textbox").position();
        final OBPath bigcardstroke = (OBPath) bigCard.objectDict.get("stroke");
        RectF bigcardrect = (RectF) bigCard.propertyValue("strokerect");
        bigcardrect = convertRectToControl(bigcardrect,bigcardstroke);
        final float bigcornerradius = (Float)bigCard.propertyValue("cornerradius");
        RectF smallcardrect = (RectF) card1.propertyValue("strokerect");
        smallcardrect = convertRectToControl(smallcardrect,bigcardstroke);
        final float smallcornerradius = (Float)bigCard.propertyValue("cornerradius");
        lockScreen();
        card2.hide();
        bigCard.show();
        bigCard.setZPosition(2500);
        Path path = new Path();
        path.addRoundRect(smallcardrect,smallcornerradius,smallcornerradius, Path.Direction.CCW);
        bigcardstroke.setPath(path);
        card1.objectDict.get("stroke").hide();
        unlockScreen();
        final float adjTextSize = applyGraphicScale(textSize);
        final float adjBigTextSize = applyGraphicScale(bigTextSize);
        final RectF fsmallcardrect = smallcardrect;
        final RectF fbigcardrect = bigcardrect;
        OBAnimBlock blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                float x = OB_Maths.interpolateVal(fsmallcardrect.left, fbigcardrect.left, frac);
                float y = OB_Maths.interpolateVal(fsmallcardrect.top, fbigcardrect.top, frac);
                float w = OB_Maths.interpolateVal(fsmallcardrect.width(), fbigcardrect.width(), frac);
                float h = OB_Maths.interpolateVal(fsmallcardrect.height(), fbigcardrect.height(), frac);
                float cr = OB_Maths.interpolateVal(smallcornerradius, bigcornerradius, frac);
                RectF r = new RectF(x, y, x+w, x+h);
                Path path = new Path();
                path.addRoundRect(r,cr,cr, Path.Direction.CCW);
                bigcardstroke.setPath(path);
                label.setFontSize(OB_Maths.interpolateVal(adjTextSize, adjBigTextSize, frac));
                label.sizeToBoundingBox();
                label.setPosition(OB_Maths.tPointAlongLine(frac, labelstartpos, labelendpos));
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)blockAnim),0.6f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        waitForSecs(0.2f);
        OBControl picbox = bigCard.objectDict.get("picbox");
        PointF picpt = picbox.position();
        picpt = convertPointFromControl(picpt,bigCard);
        picpt = OB_Maths.relativePointInRectForLocation(picpt, boundsf());
        OBWord rw = (OBWord) card1.propertyValue("readingword");
        String imagename = rw.imageName;
        playSfxAudio("pop",false);
        lockScreen();
        OBControl im = loadImageWithName(imagename,picpt,boundsf());
        if(im != null)
        {
            im.setScale(picbox.height() / im.height());

            bigCard.insertMember(im,0,"im");
            im.setZPosition(12);
            OBPath stroke2 = (OBPath) bigcardstroke.copy();
            stroke2.setFillColor(0);
            bigCard.insertMember(stroke2,0,"stroke2");
            stroke2.setZPosition(13);

        }
        unlockScreen();
        waitForSecs(0.5f);

        highlightAndSpeakSyllablesForWord(rw);

        waitForSecs(0.4f);

        if(targets.size()  > 0)
            flyOff(bigCard,card1);
        else
            bigCard.setZPosition(50);
    }

    public void flyOff(final OBControl bigCard,OBControl card1) throws Exception
    {
        PointF s = bigCard.position();
        final PointF startpos = new PointF(s.x,s.y);
        final PointF offpos = OB_Maths.locationForRect(0.1f + (float)OB_Maths.rndom() * 0.8f, -1, boundsf());
        final float zrot = (float) Math.toRadians(35);
        final float yrot = (float) Math.toRadians(60);
        playSfxAudio("flyaway",false);
        OBAnimationGroup.runAnims(Arrays.asList((OBAnim)
                new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        bigCard.setPosition(OB_Maths.tPointAlongLine(frac, startpos, offpos));
                        float zang = zrot * frac;
                        float yang = yrot * frac;
                        bigCard.m34 = (1.0f / applyGraphicScale(-700));
                        bigCard.yRotation = yang;
                        bigCard.rotation = zang;
                    }
                }
        ),1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        lockScreen();
        card1.hide();
        deleteControls("im");
        deleteControls("bigcardc");
        unlockScreen();

    }

    public void checkTarget2(OBControl targ)
    {
        if(targ == firstHit)
            return;
        setStatus(STATUS_CHECKING);
        List saveReplay = emptyReplayAudio();
        try
        {
            playSfxAudio("tap",false);
            highlightCard((OBGroup)targ,true);
            waitForSecs(0.7f);
            if(firstHit.propertyValue("wordid").equals(targ.propertyValue("wordid")))
            {
                gotItRightBigTick(false);
                targets.remove(firstHit);
                animateMatchCard1((OBGroup)targ,firstHit);
                firstHit = null;
                nextScene();
            }
            else
            {
                gotItWrongWithSfx();
                highlightCard((OBGroup)firstHit,false);
                highlightCard((OBGroup)targ,false);
                waitSFX();
                setReplayAudio(saveReplay);
                firstHit = null;
                switchStatus(currentEvent());
            }
        }
        catch(Exception exception)
        {
        }
    }

    public void checkTarget(OBControl targ)
    {
        setStatus(STATUS_CHECKING);
        List saveReplay = emptyReplayAudio();
        try
        {
            firstHit = (OBGroup) targ;
            playSfxAudio("tap",false);
            highlightCard((OBGroup)firstHit,true);
            setReplayAudio(saveReplay);
            setStatus(STATUS_AWAITING_CLICK2);
        }
        catch(Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
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
                        checkTarget((OBControl) obj);
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
                        checkTarget2((OBControl) obj);
                    }
                });
            }
        }
    }

}
