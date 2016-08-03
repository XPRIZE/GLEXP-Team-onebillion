package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBReadingWord;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 03/08/16.
 */
public class X_Fc1 extends X_Wordcontroller
{
    final static int STATUS_AWAITING_CLICK3 = 1010;
    Map<String,OBPhoneme> wordDict;
    List<String> words;
    float textSize;
    OBGroup cardBack,cardFront;
    PointF cardPos;
    boolean inFinalSection;
    String prefix;
    OBReadingWord currWord;

    public void miscSetUp()
    {
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
        String wordId = words.get(wno);
        //currWord = wordDict.get(wordId).toReadingWord();

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
        currWord.label=lab;
        //String imagename = wordId.substringFromIndex(2);
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
        textSize = Float.parseFloat(eventAttributes.get("textsize"));
        cardBack = (OBGroup) objectDict.get("cardback");
        OBControl backfront = cardBack.filterMembers("back").get(0);
        RectF r = convertRectFromControl(backfront.bounds(),backfront);
        //cardBack.size(r.inset( -4, -4));
        cardBack.setDoubleSided(false);

        cardFront = (OBGroup) objectDict.get("cardfront");
        cardPos = cardFront.position();
        cardFront.setDoubleSided(false);

        OBPath stroke2 = (OBPath) cardFront.objectDict.get("stroke").copy();
        stroke2.setFillColor(0);
        cardFront.insertMember(stroke2,-2,"stroke2");
        stroke2.setZPosition(13);
        cardFront.objectDict.get("stroke").hide();

    }


}
