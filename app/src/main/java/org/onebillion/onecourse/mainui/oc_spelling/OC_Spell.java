package org.onebillion.onecourse.mainui.oc_spelling;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;

/**
 * Created by alan on 12/12/17.
 */

public class OC_Spell extends OC_Wordcontroller
{
    static final int OCSP_LETTER_AUDIO_NONE = 0,
            OCSP_LETTER_AUDIO_NAME = 1,
            OCSP_LETTER_AUDIO_SOUND = 2;
    Map<String,OBPhoneme> wordDict;
    List<String> words;
    float picScale;
    PointF picPosition,mainTextPosition;
    Boolean showPic,RADemo;
    int letterAudio;
    String currWordID;
    List<String>distractors;
    float textSize,dashWidth,dashSpace;
    List<OBLabel>labels,staticLabels;
    List<OBControl>dashes;
    OBFont font;
    OBControl hiSwatch,greySwatch;
    int letterIdx;
    List reminderAudio;
    Map<String,Map<String,String>> letterDict;
    OBConditionLock highlightLock;


    public void miscSetUp()
    {
        wordDict = OBUtils.LoadWordComponentsXML(true);
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(";"));
        currNo = 0;
        needDemo = OBUtils.coalesce(parameters.get("demo"),"false").equals("true");
        showPic = OBUtils.coalesce(parameters.get("showpic"),"true").equals("true");
        RADemo = OBUtils.coalesce(parameters.get("rademo"),"false").equals("true");

        letterAudio = OCSP_LETTER_AUDIO_NAME;
        String laud = parameters.get("letteraudio");
        if(laud != null)
        {
            if(laud.equals("none"))
                letterAudio = OCSP_LETTER_AUDIO_NONE;
            else if(laud.equals("lettersound"))
                letterAudio = OCSP_LETTER_AUDIO_SOUND;
        }

        textSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize"),"112"));
        font = StandardReadingFontOfSize(textSize);

        if(showPic)
        {
            loadEvent("pic");
            OBControl pic = objectDict.get("pic");
            picScale = pic.scale();
            picPosition = pic.position();
            deleteControls("pic");
        }
        else
        {
            loadEvent("nopic");
        }
        OBControl tr = objectDict.get("textrect");
        mainTextPosition = new PointF(tr.position().x,tr.position().y);

        hiSwatch = objectDict.get("hiswatch");
        greySwatch = objectDict.get("greyswatch");
        hideControls(".*swatch");
        letterDict = LoadLetterXML(getLocalPath("letters.xml"));
    }

    public void setUpEvents()
    {
        events = new ArrayList<>();
        events.addAll(Arrays.asList("c,d,e".split(",")));
        int noscenes = words.size();
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        if(needDemo)
            events.add(0,"b");
        events.add(0,"a");
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        setUpEvents();
        doVisual(currentEvent());
    }

    public long switchStatus(String  scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public void determineMixedUpPositionsForLabels(List<OBLabel>labs)
    {
        labs = OBUtils.randomlySortedArray(labs);
        List<Map> comps = new ArrayList<>();
        Map ds = new HashMap();
        ds.put("fixed",false);
        ds.put("width",2.0f);
        comps.add(ds);
        Map spacer = null;
        for(OBLabel l : labs)
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
            spacer.put("width",1.0f);
        }
        ds = new HashMap();
        ds.put("fixed",false);
        ds.put("width",2.0f);
        comps.add(ds);
        RectF frme = objectDict.get("bottomrect") .frame();
        float totwidth = 0;
        float totvariable = 0;
        for(Map d : comps)
        {
            boolean fixed = (Boolean)(d.get("fixed"));
            if(fixed)
                totwidth += (Float)(d.get("width"));
            else
                totvariable += (Float)(d.get("width"));
        }
        float multiplier =(frme.width() - totwidth) / totvariable;
        float y = frme.top + frme.height() / 2;
        float x = 0;
        for(Map d : comps)
        {
            OBLabel l = (OBLabel) d.get("obj");
            if(l != null)
            {
                float tempx = x + l.width()  / 2;
                l.setProperty("botpos",new PointF(tempx, y));
                x += l.width();
            }
            else
            {
                x += (Float)(d.get("width")) * multiplier;
            }
        }
    }

    public List layOutDashesForLabels(List labs)
    {
        List ds = new ArrayList<>();
        int len = labs.size();
        float totalWidth = len * dashWidth +(len - 1) * dashSpace;
        float lft =(bounds().width() - totalWidth) / 2 + dashWidth / 2;
        float y = objectDict.get("textrect").bottom();
        int col = greySwatch.fillColor();
        for(int i = 0;i < len;i++)
        {
            OBControl c = new OBControl();
            c.setFrame(new RectF(0, 0, dashWidth, objectDict.get("dash").height() ));
            c.setPosition(lft, y);
            c.setZPosition(10);
            c.setBackgroundColor(col);
            c.hide();
            attachControl(c);
            ds.add(c);
            lft +=(dashWidth + dashSpace);
        }
        return ds;
    }


    public float rightMostLabelX()
    {
        float maxx = 0;
        for(OBLabel l : labels)
        {
            float r = l.right();
            if(r > maxx)
                maxx = r;
        }
        return maxx;
    }

    public void cleanScene()
    {
        detachControls(labels);
        detachControls(staticLabels);
        deleteControls("pic");
    }

    public void setUpImageWithName(String nm)
    {
        if (showPic)
        {
            OBImage im = OBImageManager.sharedImageManager().imageForName(nm);
            im.setScale(picScale);
            im.setPosition(picPosition);
            objectDict.put("pic",im);
            attachControl(im);
            im.hide();
        }
    }

    public void setSceneXX(String  scene)
    {
        cleanScene();
        reminderAudio = null;
        List<String>cwa = Arrays.asList(words.get(currNo).split(","));
        currWordID = cwa.get(0);
        distractors = cwa.subList(1,cwa.size());
        letterIdx = 0;
        OBWord orw = (OBWord) wordDict.get(currWordID);
        setUpImageWithName(orw.imageName);

        OBLabel mainLabel = setUpMainLabel(orw.text);
        staticLabels = layOutString(orw.text,mainLabel);
        List<OBLabel>ls = new ArrayList<>();
        for(OBLabel l : staticLabels)
        {
            OBLabel lcopy = (OBLabel) l.copy();
            l.setColour(greySwatch.fillColor() );
            lcopy.setZPosition(l.zPosition()  + 20);
            lcopy.setProperty("origzpos",lcopy.zPosition());
            ls.add(lcopy);
            l.hide();
            lcopy.hide();
            attachControl(lcopy);
        }
        ls.addAll(distractorLabels(distractors));
        labels = ls;
        determineMixedUpPositionsForLabels(labels);
    }

    public OBLabel setUpMainLabel(String tx)
    {
        OBLabel mainLabel = new OBLabel(tx,font);
        mainLabel.setColour(Color.BLACK);
        RectF bb = boundingBoxForText(mainLabel.text(),font);
        float h = bb.height() + bb.top;
        float y = h / mainLabel.bounds().height();
        mainLabel.setAnchorPoint(0.5f, y);
        mainLabel.setPosition(mainTextPosition);
        return mainLabel;
    }

    List<OBLabel> distractorLabels(List<String>distrctors)
    {
        List labs = new ArrayList<>();
        for(String s : distrctors)
        {
            OBLabel l = new OBLabel(s,font);
            l.setColour(Color.BLACK);
            l.setZPosition(70);
            labs.add(l);
            l.setProperty("origzpos",l.zPosition());
            l.hide();
            attachControl(l);
        }
        return labs;
    }

    public int maxLetterLen()
    {
        int maxLen = 1;
        for(String s : letterDict.keySet() )
            if(s.length()  > maxLen)
                maxLen = s.length();
        return maxLen;
    }

    public int lengthOfNextLetterFromString(String text,int idx,int maxLen)
    {
        int endidx = idx + maxLen;
        if(endidx > text.length() )
            endidx = text.length();
        int len = endidx - idx;
        for(int j = len;j > 1;j--)
        {
            String tx = text.substring(idx,idx + j);
            if (letterDict.get(tx) != null)
                return j;
        }
        return 1;
    }

    List<String>arrayFromText(String text)
    {
        int maxlen = maxLetterLen();
        List<String>strarr = new ArrayList<>();
        int i = 0;
        while(i < text.length() )
        {
            int l = lengthOfNextLetterFromString(text,i,maxlen);
            strarr.add(text.substring(i,i + l));
            i += l;
        }
        return strarr;
    }

    public List layOutString(String text,OBLabel mLabel)
    {
        OBLabel label = new OBLabel(text,font);
        List<Float>lefts = new ArrayList<>();
        for(int i = 0;i < text.length();i++)
        {
            float f = label.textOffset(i);
            lefts.add(f);
        }
        List labs = new ArrayList<>();
        int i = 0;
        for(String s : arrayFromText(text))
        {
            OBLabel l = new OBLabel(s,font);
            l.setColour(Color.BLACK);
            l.setPosition(mLabel.position());
            l.setLeft(mLabel.left()  + lefts.get(i));
            l.setProperty("origpos",new PointF(l.position().x,l.position().y));
            l.setZPosition(50);
            l.setProperty("origzpos",l.zPosition());
            labs.add(l);
            attachControl(l);
            i+=s.length();
        }

        return labs;
    }

    public void showPic() throws Exception
    {
        OBControl im = objectDict.get("pic");
        if(im != null  && im.hidden() )
        {
            im.show();
            playSfxAudio("picon",true);
        }
    }

    public void showWord() throws Exception
    {
        lockScreen();
        for(OBLabel l : labels)
        {
            if(!l.hidden() )
                return;
            l.show();
        }
        unlockScreen();
        playSfxAudio("wordon",true);
    }

    public void colourLabels(List<OBLabel>labs,int col)
    {
        lockScreen();
        for(OBLabel l : labs)
            l.setColour(col);
        unlockScreen();
    }

    public List<OBLabel> speakableLabels()
    {
        RectF botrect = objectDict.get("bottomrect") .frame();
        List ls = new ArrayList<>();
        for(OBLabel lab : labels)
        {
            if(!botrect.contains(lab.position().x, lab.position().y))
            {
                ls.add(lab);
            }
        }
        return ls;
    }
    public void speakWord(String wordId) throws Exception
    {
        List<OBLabel> ls = speakableLabels();
        colourLabels(ls,Color.RED);
        playAudioQueued(Arrays.asList((Object)wordId) ,true);
        waitForSecs(0.3f);
        colourLabels(ls,Color.BLACK);
    }
    public void showWordAndSpeak(String wordId) throws Exception
    {
        showWord();
        waitForSecs(0.3f);
        if(wordId != null)
        {
            speakWord(wordId);
        }
    }

    public void flyWordToBottom() throws Exception
    {
        if(dashes == null)
        {
            lockScreen();
            for (OBLabel l : staticLabels)
                l.show();
            unlockScreen();
        }
        List anims = new ArrayList<>();
        for(OBLabel l : labels)
        {
            anims.add(OBAnim.moveAnim((PointF)l.propertyValue("botpos"),l));
            l.setProperty("staticLabelIndex",null);
        }
        playSfxAudio("panel",false);
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        waitSFX();
        waitForSecs(0.2f);
    }

    public void blendWord()
    {
        List anims = new ArrayList<>();
        for(OBLabel lab : labels)
        {
            Integer n = null;
            if((n = (Integer)lab.propertyValue("staticLabelIndex"))!=null)
            {
                int i = n.intValue();
                OBLabel sl = staticLabels.get(i);
                anims.add(OBAnim.moveAnim((PointF) sl.propertyValue("origpos"),lab));
            }
        }
        OBAnimationGroup.runAnims(anims,0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }
    public void highlightLetter(int idx) throws Exception
    {
        lockScreen();
        int i = 0;
        for(OBLabel l : staticLabels)
        {
            if(i == idx)
                l.setColour(hiSwatch.fillColor() );
            else
                l.setColour(greySwatch.fillColor() );
            i++;
        }
        unlockScreen();
        if(idx < staticLabels.size() )
            playSfxAudio("target",true);
    }

    public void highlightDash(int idx) throws Exception
    {
        lockScreen();
        int i = 0;
        for(OBControl c : dashes)
        {
            if(i == idx)
                c.setBackgroundColor(hiSwatch.fillColor() );
            else
                c.setBackgroundColor(greySwatch.fillColor() );
            i++;
        }
        unlockScreen();
        if(idx < staticLabels.size() )
            playSfxAudio("target",true);
    }

    List<OBLabel> candidateLabels()
    {
        RectF botrect = objectDict.get("bottomrect") .frame();
        List<OBLabel> ls = new ArrayList<>();
        for(OBLabel l : labels)
            if(botrect.contains(l.position().x, l.position().y))
                ls.add(l);
        return ls;
    }

    public OBLabel candidateLabelWithText(String text)
    {
        for(OBLabel l : candidateLabels() )
            if(l.text().equals(text) )
                return l;
        return null;
    }

    public void showDashes() throws Exception
    {
        lockScreen();
        for(OBControl c : dashes)
            c.show();
        unlockScreen();
        playSfxAudio("lineson",true);
    }

    public void showStuff() throws Exception
    {
        if(showPic)
        {
            showPic();
            waitForSecs(0.4f);
        }
        if(labels.get(0).hidden() )
        {
            showWordAndSpeak(currWordID);
            waitForSecs(0.4f);
            flyWordToBottom();
            highlightMarker();
        }
    }

    public void setUpReplay()
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
    }

    public void doMainXX() throws Exception
    {
        showStuff();
        setUpReplay();
        playAudioQueuedScene("PROMPT",false);
    }

    public void hideAll() throws Exception
    {
        playSfxAudio("alloff",false);
        lockScreen();
        hideControls("pic");
        for(OBLabel lab : labels)
            lab.hide();
        unlockScreen();
        waitForSecs(0.1f);
        waitSFX();
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
        {
            try
            {
                waitForSecs(0.3f);
                if(status()  != STATUS_DOING_DEMO)
                {
                    hideAll();
                    waitForSecs(0.2f);
                }
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        setScene(events.get(eventIndex));
                    }
                });
            }
            catch(Exception e)
            {

            }
        }
    }

    public void flashCurrLetter()
    {
        OBLabel l = staticLabels.get(letterIdx);
        try
        {
            for(int i = 0;i < 3;i++)
            {
                l.setColour(greySwatch.fillColor() );
                waitForSecs(0.2f);
                l.setColour(hiSwatch.fillColor() );
                waitForSecs(0.2f);
            }
        }
        catch(Exception exception)
        {
        }
        l.setColour(hiSwatch.fillColor() );
    }
    public void considerReprompt(final long sttime)
    {
        try
        {
            if(reminderAudio != null)
            {
                waitForSecs(0.3f);
                waitAudio();
                if(sttime == statusTime)
                    reprompt(sttime,reminderAudio,4, new OBUtils.RunLambda()
                    {
                        public void run () throws Exception
                        {
                            if (sttime == statusTime)
                            {
                                flashCurrLetter();
                                reminderAudio = null;
                            }
                        }
                    });
            }
        }
        catch(Exception e)
        {
        }
    }

    public void reprompt (final long sttime, final List<Object> audio, float delaySecs, final OBUtils.RunLambda actionBlock)
    {
        if (statusChanged(sttime))
            return;
        OBUtils.runOnOtherThreadDelayed(delaySecs, new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(!statusChanged(sttime))
                {
                    if(audio != null)
                    {
                        boolean wait = (actionBlock != null);
                        playAudioQueued(audio, wait);
                    }
                    if (actionBlock != null)
                        actionBlock.run();
                }
            }
        });
    }

    public void endBody()
    {
        considerReprompt(statusTime);
    }

    public void highlightMarker() throws Exception
    {
        highlightLetter(letterIdx);
    }

    public void buttonDemo() throws Exception
    {
        PointF butPt = OB_Maths.locationForRect(0.5f,1.1f, MainViewController().topRightButton.frame);
        PointF offpt = new PointF();
        offpt.set(butPt);
        offpt.y = (boundsf().height() + 1);

        loadPointerStartPoint(offpt,butPt);
        theMoveSpeed = bounds().width();
        moveObjects(Arrays.asList(thePointer),butPt,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.01f);
        playAudioQueuedScene("DEMO3",true);
        waitForSecs(0.4f);
        thePointer.hide();
    }

    public void nextLetter(OBLabel currLabel) throws Exception
    {
        if(++letterIdx >= staticLabels.size() )
        {
            waitForSecs(0.2f);
            waitAudio();
            currLabel.setColour(Color.BLACK);
            waitForSecs(0.2f);
            blendWord();
            waitForSecs(0.2f);
            speakWord(currWordID);
            waitForSecs(0.4f);
            gotItRightBigTick(true);
            currNo++;
            waitForSecs(0.4f);
            nextScene();
        }
        else
        {
            final OBConditionLock lck = highlightLock = new OBConditionLock(PROCESS_NOT_DONE);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    lck.lockWhenCondition(PROCESS_DONE);
                    lck.unlock();
                    waitForSecs(0.2f);
                    highlightMarker();
                }
            });
            switchStatus(currentEvent());
            long sttime = statusTime;
            waitForSecs(0.2f);
            waitAudio();
            lck.lock();
            lck.unlockWithCondition(PROCESS_DONE);
            currLabel.setColour(Color.BLACK);
            considerReprompt(sttime);
        }
    }

    public void hideCurrentMarker()
    {
        staticLabels.get(letterIdx).hide();
    }

    public void playLetterAudio(String tx)
    {
        if(letterAudio == OCSP_LETTER_AUDIO_SOUND)
            playLetterSound(tx);
        else if(letterAudio == OCSP_LETTER_AUDIO_NAME)
            playLetterName(tx);
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBLabel lab =(OBLabel)target;
            target = null;
            OBLabel currStaticLabel = staticLabels.get(letterIdx);
            RectF tframe = new RectF(currStaticLabel .frame());
            if(dashes != null)
                tframe.union(dashes.get(letterIdx).frame());
            if(!lab .frame().intersect(tframe))
            {
                moveObjects(Arrays.asList((OBControl)lab),(PointF)lab.propertyValue("botpos"),-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                lab.setZPosition((Float)lab.propertyValue("origzpos"));
                switchStatus(currentEvent());
                return;
            }
            if(lab.text().equals(currStaticLabel.text() ) )
            {
                moveObjects(Arrays.asList((OBControl)lab),currStaticLabel.position(),-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
                lab.setProperty("staticLabelIndex",letterIdx);
                playSfxAudio("letterin",false);
                lab.setZPosition((Float)lab.propertyValue("origzpos"));
                hideCurrentMarker();
                waitForSecs(0.1f);
                waitSFX();
                if(letterAudio != OCSP_LETTER_AUDIO_NONE)
                {
                    lab.setColour(Color.RED);
                    playLetterAudio(lab.text() );
                 }
                nextLetter(lab);
                return;
            }
            gotItWrongWithSfx();
            moveObjects(Arrays.asList((OBControl)lab),(PointF)lab.propertyValue("botpos"),-2,OBAnim.ANIM_EASE_IN_EASE_OUT);
            lab.setZPosition((Float)lab.propertyValue("origzpos"));
            switchStatus(currentEvent());
        }
        catch(Exception exception)
        {
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }


    public void touchUpAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_DRAGGING)
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
        if(highlightLock != null)
        {
            highlightLock.lock();
            highlightLock.unlockWithCondition(PROCESS_DONE);
        }
        target.setZPosition(target.zPosition()  + 30);
        dragOffset = OB_Maths.DiffPoints(target.position(), pt);
    }

    public Object findTarget(PointF pt)
    {
        if(objectDict.get("bottomrect").frame().contains(pt.x,pt.y))
            for(OBLabel l : labels)
            {
                if(l.frame().contains(pt.x, pt.y))
                    return l;
            }
        return null;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_WAITING_FOR_DRAG)
        {
            target = (OBControl)findTarget(pt);
            if(target != null)
            {
                setStatus(STATUS_CHECKING);
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
