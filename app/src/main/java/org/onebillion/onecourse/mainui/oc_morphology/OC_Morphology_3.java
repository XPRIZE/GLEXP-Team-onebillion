package org.onebillion.onecourse.mainui.oc_morphology;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.onebillion.onecourse.utils.OBReadingWord.WORD_SPEAKABLE;

public class OC_Morphology_3 extends OC_Morphology
{
    List<OBLabel> originalLabels,substitutedLabels,bottomLabels;
    List<OBControl> labelsAndDash;
    List<OBReadingWord> words;
    OBLabel substitutedLabel;
    int indexOfSubstitutedLabel;
    float dashWidth;
    OBControl dash;
    boolean playFirstAudio;
    Boolean annaIntro;
    public class ocm_sinstance extends Object
    {
         String sid,imageName;
         ocm_sentence sentence;
         int wordNo;
         int rst,ren;
    }
    List<ocm_sinstance> instances;

    public void miscSetUp()
    {
        super.miscSetUp();
        highlightColour = Color.RED;
        //sharedLock = new OBConditionLock(PROCESS_DONE);

        OBControl tb = objectDict.get("textbox");
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        textBox.setShouldTexturise(false);
        attachControl(textBox);
        textBox.setZPosition(70);

        annaIntro = OBUtils.coalesce(parameters.get("annaintro"),"false").equals("true");
        playFirstAudio = OBUtils.coalesce(parameters.get("playhint") , "true").equals("true");
        List<String> gps = Arrays.asList(parameters.get("sentencegroups").split(";"));
        instances = new ArrayList<>();
        for(String gp : gps)
        {
            String[] comps = gp.split(",");
            ocm_sinstance sin = new ocm_sinstance();
            sin.sid = (comps[0]);
            sin.imageName = (comps[1]);
            sin.sentence = sentenceDict.get(sin.sid);
            String groupname = comps[2];
            String mtype = comps[3];
            String mkey = String.format("%s+%s",mtype,groupname);
            List<List<Integer>> av = sin.sentence.markups.get(mkey);
            List<Integer>v = av.get(0);
            int st = v.get(0);
            int en = v.get(1);
            sin.rst = st;
            sin.ren = en;
            instances.add(sin);
        }
        events = new ArrayList<>();
        events.add(annaIntro?"a":"b");
        events.add("n");
        events.add("o");
        List ev2 = new ArrayList(Arrays.asList("o2","p","q","r"));
        while(ev2.size() < instances.size())
            ev2.add(ev2.get(ev2.size() - 1));
        while(ev2.size() > instances.size())
            ev2.remove(ev2.size()-1);
        events.addAll(ev2);
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public void scalePicToBox()
    {
        OBControl picbox = objectDict.get("imagebox");
        float wratio = mainPic.width() / picbox.width();
        float hratio = mainPic.height() / picbox.height();
        float ratio = wratio > hratio?wratio:hratio;
        mainPic.setScale(1 / ratio);
        mainPic.setPosition(picbox.position());
    }

    public void setUpPic(String imageName)
    {
        if(mainPic != null)
            detachControl(mainPic);
        mainPic = loadImageWithName(imageName,new PointF(0.5f, 0.5f),boundsf());
        mainPic.setZPosition(objectDict.get("textbox").zPosition() -0.01f);
        scalePicToBox();
        mainPic.hide();
    }

    static int IndexOfWordContainingRange(List<OBReadingWord> words,int rst,int ren)
    {
        int i = 0;
        for(OBReadingWord rw : words)
        {
            if(rst >= rw.index && rw.index + rw.text.length() > rst)
                return i;
            i++;
        }
        return -1;
    }

    public List<OBReadingWord> word(OBReadingWord rw,int rst,int ren)
    {
        rst -= rw.index;
        ren -= rw.index;
        OBReadingWord rw0 = OBReadingWord.wordFromString(rw.text,0, rst, WORD_SPEAKABLE,0);
        OBReadingWord rw1 = OBReadingWord.wordFromString(rw.text,rst,ren,WORD_SPEAKABLE,0);
        OBReadingWord rw2 = OBReadingWord.wordFromString(rw.text,ren,rw.text.length(),WORD_SPEAKABLE,0);
        OBLabel lab;
        OBFont fnt = rw.label.font();
        lab = new OBLabel(rw0.text,fnt);
        lab.setColour(Color.BLACK);
        lab.setLeft(rw.label.left());
        lab.setTop(rw.label.top());
        rw0.label = (lab);
        lab = new OBLabel(rw1.text,fnt);
        lab.setColour(Color.BLACK);
        lab.setLeft(WidthOfText(rw0.text, lineAttributes(fnt), 0) + rw.label.left());
        lab.setTop(rw.label.top());
        lab.setProperty("dashed",true);
        rw1.label = (lab);
        lab = new OBLabel(rw2.text,fnt);
        lab.setColour(Color.BLACK);
        lab.setRight(rw.label.right());
        lab.setTop(rw.label.top());
        rw2.label = (lab);
        return Arrays.asList(rw0,rw1,rw2);
    }

    public OBControl setUpDash()
    {
        OBControl d = objectDict.get("dash");
        OBControl dsh = new OBControl();
        dsh.setBounds(new RectF(0, 0, dashWidth, d.height()));
        dsh.setFillColor(d.fillColor());
        return dsh;
    }


    public void setUpScene()
    {
        ocm_sinstance instance = instances.get(currNo);
        for (int i = textBox.members.size()-1;i >= 0;i--)
        {
            textBox.removeMemberAtIndex(i);
        }
        if (bottomLabels != null)
            for(OBControl bl : bottomLabels)
                detachControl(bl);
        setUpPic(instance.imageName);
        ocm_sentence se = instance.sentence;
        OBReadingPara para = new OBReadingPara(se.text,1);
        paragraphs = Arrays.asList(para);
        List wds = new ArrayList<>();
        for(OBReadingPara p : paragraphs)
            wds.addAll(p.words);
        words = wds;
        loadTimingsPara(para,getLocalPath(String.format("%s.etpa",se.sid)),false);
        fontSize = applyGraphicScale(textSize);
        textJustification = TEXT_JUSTIFY_CENTRE;
        layOutText();
        int widx = IndexOfWordContainingRange(words, instance.rst,instance.ren);
        indexOfSubstitutedLabel = widx;
        List<OBReadingWord> substitutionwords = word(words.get(widx),instance.rst,instance.ren);
        for(OBReadingWord subrw : substitutionwords)
        {
            textBox.insertMember(subrw.label,0,"");
        }
        OBLabel lab = substitutionwords.get(1).label;
        PointF pt = convertPointFromControl(lab.position(),textBox);
        lab.setProperty("globalpos",pt);

        words.get(widx).label.hide();
        List ol = new ArrayList<>();
        for(OBReadingWord rw : words)
        {
            ol.add(rw.label);
            PointF p = new PointF();
            p.set(rw.label.position());
            rw.label.setProperty("originalpos",p);
        }
        List sl = new ArrayList<>();
        for(OBReadingWord rw : substitutionwords)
        {
            PointF p = new PointF();
            p.set(rw.label.position());
            rw.label.setProperty("originalpos",p);
            rw.label.setProperty("sub",true);
            sl.add(rw.label);
        }
        originalLabels = ol;
        words.get(widx).label.setProperty("subbed",true);
        List<OBLabel> marr = new ArrayList<>(originalLabels);
        marr.remove(widx);
        marr.addAll(widx,sl);
        substitutedLabels = marr;
        dashWidth = WidthOfText("m", lineAttributes(font), 0) * 1.5f;
        dash = setUpDash();
        dash.setTop(fontSize);
        textBox.insertMember(dash,0,"");
        textBox.hide();

        List cntrls = new ArrayList<>();
        for(OBLabel l : substitutedLabels)
        {
            if(l.propertyValue("dashed") != null)
            {
                l.hide();
                cntrls.add(dash);
            }
            else
                cntrls.add(l);
        }
        labelsAndDash = cntrls;
        layOutControls(cntrls,textBox.width());
        layOutBottomLabels();
        targets = (List)bottomLabels;
    }

    public OBLabel dashedLabel()
    {
        for(OBLabel l : substitutedLabels)
        {
            if(l.propertyValue("dashed") != null)
                return l;
        }
        return null;
    }
    public List bottomLabels()
    {
        Set<String> usedSet = new HashSet<>();
        List<OBLabel>labs = new ArrayList<>();
        ocm_sinstance sin = instances.get(currNo);
        String fragment = sin.sentence.text.substring(sin.rst,sin.ren);
        OBLabel lab = new OBLabel(fragment,font);
        lab.setProperty("correct",true);
        labs.add(lab);
        usedSet.add(fragment);
        for(ocm_sinstance sinn : instances)
        {
            String frag = sinn.sentence.text.substring(sinn.rst,sinn.ren);
            if(!usedSet.contains(frag))
            {
                usedSet.add(frag);
                OBLabel labb = new OBLabel(frag,font);
                labs.add(labb);
            }
        }
        float zpos = objectDict.get("bottombar").zPosition() + 1;
        int col = objectDict.get("labelswatch").fillColor();
        for(OBLabel labi : labs)
        {
            labi.setColour(col);
            labi.setZPosition(zpos);
            labi.hide();
            attachControl(labi);
        }
        return labs;
    }

    public void layOutBottomLabels()
    {
        bottomLabels = bottomLabels();
        List<OBLabel> blabs = OBUtils.randomlySortedArray(bottomLabels);
        List<Map> comps = new ArrayList<>();
        Map m = new HashMap();
        m.put("fixed",false);
        m.put("width",2f);
        comps.add(m);
        Map spacer = null;
        for(OBLabel l : blabs)
        {
            if(spacer != null)
                comps.add(spacer);
            Map d = new HashMap();
            d.put("fixed",true);
            d.put("width",l.width());
            d.put("obj",l);
            comps.add(d);
            spacer = new HashMap();
            spacer.put("fixed",false);
            spacer.put("width",1f);
        }
        comps.add(m);
        RectF frme = objectDict.get("bottombar").frame();
        float totwidth = 0;
        float totvariable = 0;
        for(Map d : comps)
        {
            boolean fixed = (Boolean)d.get("fixed");
            if(fixed)
                totwidth += (Float)d.get("width");
            else
                totvariable += (Float)d.get("width");
        }
        float multiplier =(frme.width() - totwidth) / totvariable;
        float y = frme.top + frme.height() / 2;
        float x = 0;
        for(Map d : comps)
        {
            OBLabel l = (OBLabel) d.get("obj");
            if(l != null)
            {
                float tempx = x + l.width() / 2;
                PointF pos = new PointF(tempx, y);
                l.setPosition(pos);
                l.setProperty("botpos",pos);
                x += l.width();
            }
            else
            {
                x += (Float)d.get("width") * multiplier;
            }
        }
    }

    public void layOutControls(List<OBControl> cntrls,float width)
    {
        float w = 0;
        for(OBControl c : cntrls)
            w += c.width();
        float lft =(width - w) / 2;
        for(OBControl c : cntrls)
        {
            c.setLeft(lft);
            lft += c.width();
        }
    }

    public void setScenea()
    {
        loadEvent("anna");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("anna"));
        presenter.control.setZPosition(200);
        PointF pos = new PointF();
        pos.set(presenter.control.position());
        presenter.control.setProperty("restpos",pos);
        presenter.control.setRight(0);
        presenter.control.show();
    }

    public void setScenen()
    {
        setUpScene();
    }

    public void setScenep()
    {
        setUpScene();
    }

    public void setSceneq()
    {
        setUpScene();
    }

    public void setScener()
    {
        setUpScene();
    }

    public void demoa() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(0.5f);

        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = presenter.control.position();
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds() .width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.5f);
        OBControl faceright = presenter.control.objectDict.get("faceright");
        PointF lp = presenter.control.convertPointFromControl(new PointF(0, 0),faceright);
        PointF destpos = new PointF(bounds() .width() + lp.x + 1, currPos.y);
        presenter.walk(destpos);
        detachControl(presenter.control);
        presenter = null;

        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);

        nextScene();

    }

    public void demob() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);

        nextScene();
    }

    public OBLabel correctLabel()
    {
        for(OBLabel lab : bottomLabels)
            if(lab.propertyValue("correct") != null)
                return lab;
        return null;
    }

    public void demon() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.2f);
        bringOnThings();
        waitForSecs(0.2f);
        if(playFirstAudio)
        {
            playAudioQueuedScene("DEMO2",true);
            waitForSecs(0.2f);
            playSfxAudio("listen",true);
            waitForSecs(0.2f);
            readSentenceHighlightingAll(true);
            waitForSecs(0.2f);
        }
        playAudioScene("DEMO3",0,true);
        waitForSecs(0.2f);

        OBLabel dlab = correctLabel();
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.5f, 0.6f) , dlab.frame()),-1,true);
        waitForSecs(0.2f);
        //PointF dashpos = convertPointFromControl(dash.position(),textBox);

        OBLabel tl = dashedLabel();
        PointF dashpos =(PointF)tl.propertyValue("globalpos");

        moveObjects(Arrays.asList(dlab,thePointer),dashpos,-1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.2f);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
                movePointerToPoint(destpoint,-1,true);
            }
        });
        moveEverythingToCorrectPosition(dlab);

        waitForSecs(1.6f);

        moveEverythingBack(dlab);
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.5f);
        playAudioScene("DEMO3",1,true);
        nextScene();
    }

    public void demoo() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);

        List aud = currentAudio("DEMO");
        int i =(int) aud.size() - 3;

        if(i >= 0)
        {
            playAudioScene("DEMO",i,true);
            waitForSecs(0.3f);
        }
        i++;

        destpoint = convertPointFromControl(dash.position(),textBox);

        destpoint.y += applyGraphicScale(40);
        movePointerToPoint(destpoint,-1,true);
        playAudioScene("DEMO",i++,true);

        PointF pt = OB_Maths.locationForRect(new PointF(0.5f, 1.5f) , bottomLabels.get(0).frame());
        float minx = pt.x;
        float maxx =pt.x;
        for(OBControl c : bottomLabels)
        {
            float f;
            if((f = c.left()) < minx)
                minx = f;
            if((f = c.right()) > maxx)
                maxx = f;
        }
        pt.x = (minx);
        movePointerToPoint(pt,-1,true);
        playAudioScene("DEMO",i,false);
        pt.x = (maxx);
        movePointerToPoint(pt,-0.7f,true);
        waitForSecs(1f);
        thePointer.hide();
        nextScene();
    }

    public void slideOnBottomLabels() throws Exception
    {
        List anims = new ArrayList<>();
        lockScreen();
        for(OBLabel lab : bottomLabels)
        {
            PointF pos = new PointF();
            pos.set(lab.position());
            lab.setPosition(new PointF(pos.x + bounds() .width(), pos.y));
            anims.add(OBAnim.moveAnim(pos,lab));
            lab.show();
        }
        unlockScreen();
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void bringOnThings() throws Exception
    {
        if(mainPic != null && mainPic.hidden())
        {
            playSfxAudio("picon",false);
            mainPic.show();
        }
        waitForSecs(0.5f);

        if(textBox.hidden())
        {
            playSfxAudio("sentence",false);
            textBox.show();
            waitForSecs(0.3f);
            playSfxAudio("slide",false);
            slideOnBottomLabels();
        }
    }

    public void readSentenceHighlightingAll(boolean high) throws Exception
    {
        if(high)
            lockScreen();
        for(OBLabel lab : substitutedLabels)
            lab.setColour(Color.RED);
        unlockScreen();
        playAudioQueued((List)Arrays.asList(currentSentence().sid),true);
        if(high)
            lockScreen();
        for(OBLabel lab : substitutedLabels)
            lab.setColour(Color.BLACK);
        unlockScreen();
    }

    public void doMainXX() throws Exception
    {
        bringOnThings();

        if(currentAudio("PROMPT.REPEAT") != null)
            setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        else
            setReplayAudio((List)Arrays.asList(currentSentence().sid));
        playAudioQueuedScene("PROMPT",true);
        if(playFirstAudio)
            readSentenceHighlightingAll(true);
    }

    public void endBody()
    {
        doReminder(statusTime());
    }

    public void replayAudio()
    {
        if(busyStatuses.contains((status())))
        return;
        if(_replayAudio != null)
        {
            setStatus(status());
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    _replayAudio();
                }
            });
        }
    }

    public void doReminder(final long stt)
    {
        reprompt(stt, null, 8.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                try
                {
                    for(int i = 0;i < 3;i++)
                    {
                        dash.setOpacity(0.2f);
                        waitForSecs(0.4f);
                        dash.setOpacity(1);
                        waitForSecs(0.4f);
                    }
                }
                catch(Exception e)
                {
                }
                if(!aborting())
                    doReminder(stt);
            }
        });
    }

    public void moveToOriginalPosition(OBControl targ)
    {
        PointF targOriginalPoint =(PointF)targ.propertyValue("botpos");
        moveObjects(Arrays.asList(targ),targOriginalPoint,0.2f,OBAnim.ANIM_EASE_IN_EASE_OUT);
        //target.setZPosition(target.propertyValue("zpos").floatValue());
    }

    public void moveEverythingToCorrectPosition(OBControl targ) throws Exception
    {
        List anims = new ArrayList<>();
        OBLabel tl = dashedLabel();
        PointF targpos =(PointF)tl.propertyValue("globalpos");
        //targpos = convertPointFromControl(targpos,tl.parent());
        anims.add(OBAnim.moveAnim(targpos,targ));
        for(OBLabel lab : substitutedLabels)
        {

            lab.setProperty("tpos",new PointF(lab.position().x,lab.position().y));
            anims.add(OBAnim.moveAnim((PointF) lab.propertyValue("originalpos"),lab));
        }
        anims.add(OBAnim.opacityAnim(0,dash));
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        playSfxAudio("click",false);
    }

    public void moveEverythingBack(OBControl targ) throws Exception
    {
        List anims = new ArrayList<>();
        anims.add(OBAnim.moveAnim((PointF) targ.propertyValue("botpos"),targ));
        for(OBLabel lab : substitutedLabels)
            anims.add(OBAnim.moveAnim((PointF) lab.propertyValue("tpos"),lab));
        anims.add(OBAnim.opacityAnim(1,dash));
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

   /* public OBControl finger(final int startidx,final int endidx,final List targets,final PointF pt)
    {
        final Map<String,OBControl> resd = new HashMap<>();
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                resd.put("res",finger(startidx,endidx,targets,pt,false));

            }
        });
        return resd.get("res");
    }*/

    public void doIncorrect()
    {
        try
        {
            long token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                List aud = currentAudio("INCORRECT");
                if(aud.size() > 0)
                    playAudioQueued(Arrays.asList(aud.get(0)),true);
                checkSequenceToken(token);
                if(playFirstAudio)
                    readSentenceHighlightingAll(false);
                checkSequenceToken(token);
                if(aud.size() > 1)
                    playAudioQueued(Arrays.asList(aud.get(1)),true);
            }
        }
        catch(Exception e)
        {
        }
        sequenceLock.unlock();
    }

    public void clearOff() throws Exception
    {
        playSfxAudio("alloff",false);
        lockScreen();
        mainPic.hide();
        textBox.hide();
        for(OBControl c : bottomLabels)
            c.hide();
        unlockScreen();
    }
    public void nextSentence() throws Exception
    {
        currNo++;
        if(eventIndex < events.size() - 1)
        {
            clearOff();
            waitForSecs(0.4f);
        }
        nextScene();
    }

    static List  InANotB(List a,List b)
    {
        List marr = new ArrayList<>();
        for(Object obj : a)
            if(!b.contains(obj))
                marr.add(obj);
        return marr;
    }

    public void checkDragAtPoint(PointF pt)
    {
        setStatus(STATUS_CHECKING);
        takeSequenceLockInterrupt(true);
        sequenceLock.unlock();
        try
        {
            OBLabel targ =(OBLabel) target;
            //PointF pos = convertPointToControl(targ.position(),dash.parent);
            if(finger(-1,3,Arrays.asList(dash),pt) == null)
            {
                moveToOriginalPosition(targ);
                switchStatus(currentEvent());
                return;
            }
            if(targ.propertyValue("correct") == null)
            {
                gotItWrongWithSfx();
                moveToOriginalPosition(targ);
                waitForSecs(0.1f);
                waitSFX();
                switchStatus(currentEvent());
                doIncorrect();
                return;
            }
            moveEverythingToCorrectPosition(targ);
            waitForSecs(0.5f);
            gotItRightBigTick(true);
            waitForSecs(0.5f);
            if(! playFirstAudio)
            {
                sequenceToken = 0;
                for(OBControl c : (List<OBControl>)InANotB(originalLabels, substitutedLabels))
                    c.show();
                for(OBControl c : (List<OBControl>)InANotB(substitutedLabels, originalLabels))
                    c.hide();
                targ.hide();
                readParagraph(0,currentSentence().sid,0,false);
            }
            waitForSecs(0.5f);
            waitSFX();
            nextSentence();
        }
        catch(Exception exception)
        {
        }
    }

    public ocm_sentence currentSentence()
    {
        ocm_sinstance instance = instances.get(currNo);
        return instance.sentence;
    }
    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
        {
            setStatus(STATUS_CHECKING);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }

    public void checkDragTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        targ.setZPosition(targ.zPosition() + 20);
        target = targ;
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }


    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            final Object obj = findTarget(pt);
            if(obj != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkDragTarget((OBControl)obj,pt);
                    }
                });
            }
        }

    }



}
