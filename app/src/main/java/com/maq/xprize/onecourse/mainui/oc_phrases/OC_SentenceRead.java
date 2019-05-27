package com.maq.xprize.onecourse.mainui.oc_phrases;

import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.controls.*;
import com.maq.xprize.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import com.maq.xprize.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by alan on 14/12/17.
 */

public class OC_SentenceRead extends OC_PhraseSentenceRead
{
    final static int MODE_CAPS = 0,
    MODE_COMMA = 1,
    MODE_QUESTION = 2,
    MODE_EXCLAMATION = 3,
    MODE_QUOTE = 4;
    final static int upsidedownqm = 191,
    upsidedownexc = 161,
    rightquote = 8221,
    leftquote = 8220;
    int sentenceMode;
    String infix;
    boolean optDemoRequired;

    public void substituteEvents()
    {
        String bpref = infix;
        events.set(events.indexOf("b"),bpref);
        if(events.contains("c"))
        {
            String cpref = bpref + "opt";
            events.set(events.indexOf("c"),cpref);
        }
        String dpref = bpref + "fw";
        events.set(events.indexOf("d"),dpref);
        String finaleprefix = "finale" + infix;
        audioScenes.put("finale",audioScenes.get(finaleprefix));
    }

    public void miscSetUp()
    {
        loadEvent("mastera");
        showIntro = OBUtils.coalesce(parameters.get("intro"),"false").equals("true");
        optDemoRequired = OBUtils.coalesce(parameters.get("optdemo"),"false").equals("true");
        lrDemoRequired = OBUtils.coalesce(parameters.get("lrdemo"),"false").equals("true");
        String s = OBUtils.coalesce(parameters.get("mode"),"caps");
        sentenceMode = Arrays.asList("caps","comma","question","exclamation","quote").indexOf(s);
        if(sentenceMode < 0)
            sentenceMode = 0;
        infix = (new String[]{"capsfs","comma","qm","exc","quote"}[sentenceMode]);
        componentDict = loadComponent("sentence",getLocalPath("sentences.xml"));
        fontSize = applyGraphicScale(60);

        OBControl tb = objectDict.get("textbox");
        textBoxOriginalPos = new PointF();
        textBoxOriginalPos.set(tb.position());
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        textBox.setShouldTexturise(false);
        attachControl(textBox);

        currNo = -1;
        componentList = Arrays.asList(parameters.get("sentences").split(","));
        events = new ArrayList(Arrays.asList("e,g,h".split(",")));
        while(events.size()  > componentList.size() )
            events.remove(events.size()-1);
        while(events.size()  < componentList.size() )
            events.add(events.get(events.size() - 1));
        events.add(0,"d2");
        events.add(0,"d");
        if(optDemoRequired)
            events.add(0,"c");
        events.add(0,"b");
        if(showIntro)
            events.add(0,"a");
        substituteEvents();
        setUpWordStuff();
    }

    public boolean newPicRequiredForScene(String scene)
    {
        if(Arrays.asList("a","d2","e").contains(scene))
        return false;
        if(scene.endsWith("opt") || scene.endsWith("fw"))
            return false;
        return true;
    }

    public void setSceneXX(String scene)
    {
        if(newPicRequiredForScene(scene))
        {
            currNo++;
            setUpScene();
        }
    }

    public void setScenea()
    {
        setSceneXX(currentEvent());
        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF rp = new PointF();
        rp.set(presenter.control.position());
        presenter.control.setProperty("restpos",rp);
        presenter.control.setRight(0);
        presenter.control.show();
    }


    public void doMaind2() throws Exception
    {
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",false);
    }


    public void doMaine() throws Exception
    {
        waitForSecs(0.5f);
        lockScreen();
        wordback.hide();
        wordback2.hide();
        unlockScreen();
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",true);
        waitForSecs(0.3f);
        nextWord();
    }

    public void nextWord() throws Exception
    {
        if(currentEvent().equals("d2"))
        {
            nextScene();
            return;
        }
        super.nextWord();
    }

    public String wholeString()
    {
        StringBuilder ms = new StringBuilder();
        for(OBReadingPara p : paragraphs)
            ms.append(p.text);
        return ms.toString();
    }

    public List allReadingWords()
    {
        List arr = new ArrayList<>();
        for(OBReadingPara p : paragraphs)
            arr.addAll(p.words);
        return arr;

    }
    static boolean PuncButNotFS(int u)
    {
        if (u == '.')
            return false;
        return !(Character.isLetterOrDigit(u) || Character.isWhitespace(u));
    }

    static boolean validCapsFS(String s)
    {
        s = s.trim();
        if(s.length() < 2)
            return false;
        int i = s.length()  - 1;
        while(i >= 0 && PuncButNotFS(s.codePointAt(i)))
            i--;
        if(i < 0 || s.codePointAt(i) != '.')
            return false;
        i = 0;
        while (i < s.length() && ! Character.isLetter(s.codePointAt(i)))
            i++;
        if (i == s.length())
            return false;
        return Character.isUpperCase(s.codePointAt(0));
    }

    public void demoa()throws Exception
    {
        presenterDemo();
    }

    public void demob()throws Exception
    {
        waitForSecs(0.3f);
        showPic();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);

        showWords();

        PointF destpoint = OB_Maths.locationForRect(0.9f, 0.9f,bounds());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);

        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.3f);
    }

    public void democ()throws Exception
    {
        OBReadingWord firstWord = firstWord();
        RectF f = firstWord.frame;

        PointF wordPoint = OB_Maths.locationForRect(0.5f, 1.2f, f);
        movePointerToPoint(wordPoint,-1,true);
        waitForSecs(0.3f);

        lockScreen();
        wordback.show();
        wordback2.show();
        unlockScreen();
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        movePointerForwards(applyGraphicScale(-40),-1);

        waitForSecs(0.3f);
        playAudioScene("DEMO",1,true);

        waitForSecs(0.4f);
        thePointer.hide();
    }

    public void democapsfs()throws Exception
    {
        demob();
        nextScene();
    }

    public RectF boundingBoxForLabel(OBLabel lab,int rngst,int rngend)
    {
        RectF r = new RectF(lab.bounds());
        float l = 0,w = 0;
        String s = lab.text();
        if(rngst > 0)
        {
            String bs = s.substring(0,rngst);
            RectF bb = OC_Wordcontroller.boundingBoxForText(bs,lab.font());
            l = bb.right;
        }
        String bs = s.substring(0,rngend);
        RectF bb = OC_Wordcontroller.boundingBoxForText(bs, lab.font());
        w = bb.width();

        r.left = l;
        r.right = w;
        return r;
    }

    public OBReadingWord searchBackWardsChar(int uc)
    {
        List<OBReadingWord>allWords = allReadingWords();
        String us = String.format("%c",uc);
        for(int i = allWords.size()  - 1;i >= 0;i--)
        {
            OBReadingWord w = allWords.get(i);
            String t = w.text;
            if(t.contains(us))
                return w;
        }
        return null;
    }

    public void democapsfsopt()throws Exception
    {
        if(!validCapsFS(wholeString() ))
            return;
        OBReadingWord fsWord = searchBackWardsChar('.');
        if(fsWord == null)
            return;
        OBReadingWord capWord = words.get(0);
        RectF bb = boundingBoxForLabel(capWord.label,0,1);
        PointF destpoint = OB_Maths.locationForRect(0.5f,1f,bb);
        destpoint = convertPointFromControl(destpoint,capWord.label);
        movePointerToPoint(destpoint,-1,true);
        playSfxAudio("red",false);
        highlightWord(capWord,0, 1,true,false);
        waitSFX();
        waitForSecs(0.2f);
        playAudioScene("DEMO",0,true);
        int rngst = fsWord.label.text().indexOf(".");
        bb = boundingBoxForLabel(fsWord.label,rngst,rngst+1);
        destpoint = OB_Maths.locationForRect(0.5f,1f,bb);
        destpoint = convertPointFromControl(destpoint,fsWord.label);

        movePointerToPoint(destpoint,-1,true);
        playSfxAudio("red",false);
        highlightWord(fsWord,rngst,rngst+1,true,false);
        waitSFX();
        waitForSecs(0.2f);
        playAudioScene("DEMO",1,true);

        waitForSecs(0.2f);
        waitForSecs(1f);
        highlightWord(capWord,0,1,false,false);
        highlightWord(fsWord,rngst,rngst+1,false,false);
        nextScene();

    }

    public void democapsfsfw()throws Exception
    {
        democ();
        nextScene();

    }

    public void democomma()throws Exception
    {
        demob();
        nextScene();
    }

    public void democommaopt()throws Exception
    {
        OBReadingWord commaWord = searchBackWardsChar(',');
        if(commaWord == null)
            return;
        int rngst = commaWord.label.text().indexOf(",");
        RectF bb = boundingBoxForLabel(commaWord.label,rngst,rngst+1);
        PointF destpoint = OB_Maths.locationForRect(0.5f, 1f,bb);
        destpoint = convertPointFromControl(destpoint,commaWord.label);

        movePointerToPoint(destpoint,-1,true);
        playSfxAudio("red",false);
        highlightWord(commaWord,rngst,rngst+1,true,false);
        waitSFX();
        waitForSecs(0.2f);
        playAudioScene("DEMO",0,true);

        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(-100),-1);
        waitForSecs(0.2f);
        playAudioScene("DEMO",1,true);
        waitForSecs(1f);
        highlightWord(commaWord,rngst,rngst+1,false,false);
        nextScene();
    }

    public void democommafw()throws Exception
    {
        democ();
        nextScene();

    }

    public void demoqm()throws Exception
    {
        demob();
        nextScene();
    }



    public void demoqmopt()throws Exception
    {
        OBReadingWord qmword = searchBackWardsChar('?');
        if(qmword == null)
            return;
        int rngqmst = qmword.label.text().indexOf("?");

        OBReadingWord updownqmword = searchBackWardsChar(upsidedownqm);
        int rngudqmst = updownqmword!=null?updownqmword.label.text().indexOf(String.format("%c",upsidedownqm)):-1;

        int rgst = rngqmst;
        OBReadingWord firstWord = qmword;
        String audioScene = "DEMO";
        if(updownqmword != null)
        {
            rgst = rngudqmst;
            firstWord = updownqmword;
            audioScene = "DEMO2";
        }
        RectF bb = boundingBoxForLabel(firstWord.label,rgst,rgst+1);
        PointF destpoint = OB_Maths.locationForRect(0.5f, 1f,bb);
        destpoint = convertPointFromControl(destpoint,firstWord.label);
        movePointerToPoint(destpoint,-1,true);

        playSfxAudio("red",false);
        lockScreen();
        highlightWord(qmword,rngqmst,rngqmst+1,true,false);
        if(updownqmword != null)
            highlightWord(updownqmword,rngudqmst,rngudqmst+1,true,false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.2f);
        playAudioScene(audioScene,0,true);

        if(updownqmword != null)
        {
            bb = boundingBoxForLabel(updownqmword.label,rngudqmst,rngudqmst+1);
            destpoint = OB_Maths.locationForRect(0.5f, 1f,bb);
            destpoint = convertPointFromControl(destpoint,updownqmword.label);
            movePointerToPoint(destpoint,-1,true);
        }
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(-50),-0.5f);
        waitForSecs(0.2f);

        playAudioScene(audioScene,1,true);
        waitForSecs(1f);
        lockScreen();
        highlightWord(qmword,rngqmst,rngqmst+1,false,false);
        if(updownqmword != null)
            highlightWord(updownqmword,rngudqmst,rngudqmst+1,false,false);
        unlockScreen();
        nextScene();
    }

    public void demoqmfw()throws Exception
    {
        democ();
        nextScene();

    }

    public void demoexc()throws Exception
    {
        demob();
        nextScene();
    }

    public void demoexcopt()throws Exception
    {
        OBReadingWord excword = searchBackWardsChar('!');
        if(excword == null)
            return;
        int rngexcst = excword.label.text().indexOf("!");

        OBReadingWord updownexcword = searchBackWardsChar(upsidedownexc);
        int rngudexcst = updownexcword!=null?updownexcword.label.text().indexOf(String.format("%c",upsidedownexc)):-1;

        int rgst = rngexcst;
        OBReadingWord firstWord = excword;
        String audioScene = "DEMO";
        if(updownexcword != null)
        {
            rgst = rngudexcst;
            firstWord = updownexcword;
            audioScene = "DEMO2";
        }
        RectF bb = boundingBoxForLabel(firstWord.label,rgst,rgst+1);
        PointF destpoint = OB_Maths.locationForRect(0.5f, 1f,bb);
        destpoint = convertPointFromControl(destpoint,firstWord.label);
        movePointerToPoint(destpoint,-1,true);

        playSfxAudio("red",false);
        lockScreen();
        highlightWord(excword,rngexcst,rngexcst+1,true,false);
        if(updownexcword != null)
            highlightWord(updownexcword,rngudexcst,rngudexcst+1,true,false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.2f);
        playAudioScene(audioScene,0,true);

        if(updownexcword != null)
        {
            bb = boundingBoxForLabel(updownexcword.label,rngudexcst,rngudexcst+1);
            destpoint = OB_Maths.locationForRect(0.5f, 1f,bb);
            destpoint = convertPointFromControl(destpoint,updownexcword.label);
            movePointerToPoint(destpoint,-1,true);
        }
        waitForSecs(0.2f);
        movePointerForwards(applyGraphicScale(-50),-0.5f);
        waitForSecs(0.2f);

        playAudioScene(audioScene,1,true);
        waitForSecs(1f);
        lockScreen();
        highlightWord(excword,rngexcst,rngexcst+1,false,false);
        if(updownexcword != null)
            highlightWord(updownexcword,rngudexcst,rngudexcst+1,false,false);
        unlockScreen();
        nextScene();

    }

    public void demoexcfw()throws Exception
    {
        democ();
        nextScene();

    }

    public void demoquote()throws Exception
    {
        demob();
        nextScene();
    }

    public void demoquoteopt()throws  Exception
    {
        OBReadingWord q2word = searchBackWardsChar(rightquote);
        if(q2word == null)
            return;
        OBReadingWord q1word = searchBackWardsChar(leftquote);
        if(q1word == null)
            return;
        int rngq1st = q1word.label.text().indexOf(String.format("%c",leftquote));
        int rngq2st = q2word.label.text().indexOf(String.format("%c",rightquote));

        RectF bb = boundingBoxForLabel(q1word.label,rngq1st,rngq1st+1);
        PointF destpoint = OB_Maths.locationForRect(0.5f, 0.5f,bb);
        destpoint = convertPointFromControl(destpoint,q1word.label);
        movePointerToPoint(destpoint,-1,true);

        playSfxAudio("red",false);
        lockScreen();
        highlightWord(q1word,rngq1st,rngq1st+1,true,false);
        highlightWord(q2word,rngq2st,rngq2st+1,true,false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.2f);
        playAudioScene("DEMO",0,false);

        bb = boundingBoxForLabel(q2word.label,rngq2st,rngq2st+1);
        destpoint = OB_Maths.locationForRect(0.5f, 0.5f,bb);
        destpoint = convertPointFromControl(destpoint,q2word.label);
        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.2f);
        waitAudio();
        movePointerForwards(applyGraphicScale(-50),-0.5f);
        waitForSecs(0.2f);

        playAudioScene("DEMO",1,true);
        //movePointerToPoint(lastpoint,-1,true);
        waitForSecs(1f);
        lockScreen();
        highlightWord(q1word,rngq1st,rngq1st+1,false,false);
        highlightWord(q2word,rngq2st,rngq2st+1,false,false);
        unlockScreen();
        nextScene();


    }

    public void demoquotefw()throws  Exception
    {
        democ();
        nextScene();

    }

    public void considerLrDemo(boolean primary)throws  Exception
    {
        if(primary && currentEvent().equals("e") && lrDemoRequired)
        {
            lrDemo("f");
        }
    }

}
