package org.onebillion.onecourse.mainui.oc_phrases;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBReadingWord.WORD_SPEAKABLE;
import static org.onebillion.onecourse.utils.OBUtils.LoadWordComponentsXML;
import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;
import static org.onebillion.onecourse.utils.OBUtils.UnscaledReadingFontOfSize;
import static org.onebillion.onecourse.utils.OBUtils.coalesce;

/**
 * Created by alan on 03/11/2017.
 */

public class OC_ClozePhrase extends OC_Cloze
{
    public void miscSetUp()
    {
        loadEvent("mastera");
        currNo = -1;
        needDemo = coalesce(parameters.get("demo"),"false").equals("true");
        showPic = coalesce(parameters.get("showpic"),"true").equals("true");

        wordDict = LoadWordComponentsXML(true);

        OBControl tb = objectDict.get("textbox");
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        textBox.setShouldTexturise(false);
        attachControl(textBox);
        textBox.setZPosition(70);

        if((phraseMode = parameters.get("phrases")  != null))
        {
            componentDict = loadComponent("phrase",getLocalPath("phrases.xml"));
            componentList = Arrays.asList(parameters.get("phrases").split(";"));
            loadEvent("phrase");
        }
    else
        {
            componentDict = loadComponent("sentence",getLocalPath("sentences.xml"));
            componentList = Arrays.asList(parameters.get("sentences").split(";"));
            loadEvent("sentence");
        }

        fontSize  = Float.parseFloat(coalesce(eventAttributes.get("textsize"), "60"));

        OBControl greyswatch = objectDict.get("greyswatch");
        OBControl bottomRect = objectDict.get("bottomrect");
        bottomRect.setFillColor(greyswatch.fillColor() );
        greyswatch.hide();
        OBControl dash = objectDict.get("underline");
        dashWidth = dash.width();
        dash.hide();
        setUpEvents();
    }

    public void setUpEvents()
    {
        events = new ArrayList<>();
        events.addAll(Arrays.asList("c,d,e".split(",")));
        int noscenes = componentList.size();
        while(events.size()  > noscenes)
            events.remove(events.size()-1);
        while(events.size()  < noscenes)
            events.add(events.get(events.size()-1));
        if(needDemo)
            events.add(0,"b");
        events.add(0,"a");
    }

    public void setUpScene()
    {
        cleanScene();
        List<String> thisparm = Arrays.asList(componentList.get(currNo).split(","));
        currComponentKey = thisparm.get(0);
        processParms(thisparm);
        Map currPhraseDict = componentDict.get(currComponentKey);
        String imageName = (String) currPhraseDict.get("imagename");
        if(mainPic != null)
            detachControl(mainPic);
        mainPic = loadImageWithName(imageName,new PointF(0.5f, 0.5f),boundsf());
        objectDict.put("mainpic",mainPic);
        mainPic.setZPosition(objectDict.get("textbox").zPosition() - 0.01f);
        scalePicToBox();
        mainPic.hide();

        String ptext = (String) currPhraseDict.get("contents");
        OBReadingPara para = new OBReadingPara(ptext,1);
        List<OBReadingWord> mwarr = new ArrayList<>();
        int idx = 0;
        for(OBReadingWord rw : para.words )
        {
            if((rw.flags & WORD_SPEAKABLE) != 0)
            {
                if(missingIndexes.contains(idx))
                {
                    rw.settings.put("missing",true);
                    mwarr.add(rw);
                }
                idx++;
            }
        }
        missingWords = mwarr;
        paragraphs = Arrays.asList(para);

        layOutText();
        updatePositions();
        calcWordFrames();
        adjustTextPosition();
        for(OBReadingWord rw : para.words)
        {
            if (rw.label != null)
                rw.label.hide();
            if(rw.settings.get("dash") != null )
                ((OBControl)rw.settings.get("dash")).hide();
        }

        createBottomLabels();
        positionBottomLabels();

        int i = 1;
        for(OBReadingPara pa : paragraphs)
        {
            loadTimingsPara(pa,getLocalPath(String.format("%s_%d.etpa",currComponentKey,i)));
            i++;
        }

    }

    public float paraWidth(OBReadingPara para,Map attrs)
    {
        float w = 0.0f;
        String ms = new String();
        int nodashes = 0;
        for(OBReadingWord rw : para.words)
        {
            if(rw.settings.get("missing")  == null)
                ms += (rw.text);
        else
            nodashes++;
        }
        w = WidthOfText(ms,attrs,0);
        return w + nodashes * dashWidth;
    }

    public float workOutWordWidths(List<OBReadingWord>wds,Map attrs)
    {
        String text = new String();
        for(OBReadingWord rw : wds)
        {
            text += (rw.text);
            if(!rw.text.startsWith(" "))
            {
                float thisWidth = WidthOfText(rw.text, attrs, spaceExtra);
                float widthToEnd = WidthOfText(text, attrs, spaceExtra);
                rw.settings.put("wordwidth",thisWidth);
                rw.settings.put("startx",(widthToEnd - thisWidth));
            }
        }
        for(int i = wds.size()  - 2;i >= 1;i--)
        {
            OBReadingWord rw = wds.get(i);
            if(rw.text.startsWith(" "))
            {
                float widthToEnd = (Float) wds.get(i+1).settings.get("startx");
                float startx = (Float)wds.get(i-1).settings.get("startx") + (Float)wds.get(i-1).settings.get("wordwidth");
                rw.settings.put("wordwidth",(widthToEnd - startx));
                rw.settings.put("startx",(startx));
            }
        }
        int idx = 0;
        float offset = 0;
        for(OBReadingWord rw : wds)
        {
            float startx = coalesce((Float)rw.settings.get("startx"),0f);
            rw.settings.put("startx",(startx + offset));
            if((rw.flags & WORD_SPEAKABLE) != 0)
            {
                if(coalesce(((Boolean)rw.settings.get("missing")),false))
                {
                    rw.settings.put("width",(dashWidth));
                    offset += (dashWidth - coalesce((Float)rw.settings.get("wordwidth"),0f));
                }
            else
                rw.settings.put("width",coalesce(rw.settings.get("wordwidth"),new Float(0)));
                idx++;
            }
        }
        OBReadingWord rw = wds.get(wds.size()-1);
        return coalesce((Float)rw.settings.get("startx"),0f) + coalesce((Float)rw.settings.get("width"),0f);
    }

    public List array(List arr,Object element)
    {
        int idx = arr.indexOf(element);
        if(idx >= 0)
        {
            return arr.subList(0, idx + 1);
        }
        return arr;
    }

    public OBLabel createLabel(OBReadingWord rw, String text, OBFont font)
    {
        OBLabel lab;
        lab = new OBLabel(text,font);
        lab.setColour(Color.BLACK);
        lab.setLetterSpacing(letterSpacing);
        if((rw.flags & WORD_SPEAKABLE) != 0)
            lab.setZPosition(LABEL_ZPOS);
        else
            lab.setZPosition(LABEL_ZPOS - 3);
        return lab;
    }

    public void layOutLine(List<OBReadingWord> wordarr,float leftEdge,float rightEdge,float y,int justification,OBFont font,String paraText)
    {
        List<OBReadingWord> wds = new ArrayList(wordarr);
        while(wds.size()  > 0 && wds.get(wds.size()-1).text.startsWith(" "))
            wds.remove(wds.size() - 1);
        if(wds.size() == 0)
            return;
        numberOfTextLines++;
        Map attributes = lineAttributes(font);
        float totalWordWidth = workOutWordWidths(wds,attributes);
        float lineStart = 0;
        if(justification == TEXT_JUSTIFY_CENTRE)
            lineStart =(rightEdge - totalWordWidth) / 2;
        for(OBReadingWord rw : wds)
        {
            if(rw.label == null)
            {
                rw.label = (createLabel(rw,rw.text,font));
                textBox.insertMember(rw.label,0,"");
            }
            PointF pos = new PointF(0,y);
            pos.x = ((Float)rw.settings.get("startx") + lineStart + rw.label.width()  / 2);
            rw.label.setProperty("pos",new PointF(pos.x,pos.y));
            if(coalesce((Boolean)rw.settings.get("missing"),false))
            {
                rw.label.hide();
                OBControl dash = (OBControl) rw.settings.get("dash");
                if(dash == null)
                {
                    OBControl d = objectDict.get("underline");
                    dash = d.copy();
                    rw.settings.put("dash",dash);
                    textBox.insertMember(dash,0,"");
                    dash.show();
                }
                pos.y += 0.35 * rw.label.height();
                pos.x +=(dash.width()  - rw.label.width() ) / 2;
                dash.setProperty("pos",pos);
                if(!phraseMode && rw.settings.get("firstword") != null  && !rw.text.equals(rw.text.toLowerCase() ) )
                {
                    OBLabel label = (OBLabel)rw.settings.get("lclabel");
                    if(label == null)
                    {
                        OBLabel l = createLabel(rw,rw.text.toLowerCase(),font);
                        rw.settings.put("lclabel",l);
                        textBox.insertMember(l,0,"");
                        l.hide();
                    }
                }
            }
            if(rw.settings.get("lclabel") != null )
            {
                OBLabel label = (OBLabel) rw.settings.get("lclabel");
                PointF npos = new PointF();
                npos.set((PointF)rw.label.propertyValue("pos"));
                float diff = rw.label.frame().width() / 2f;
                float right = npos.x + diff;
                npos.x = right - label.frame().width() / 2f;
                label.setProperty("pos",npos);
            }
        }
    }

    public void updatePositions()
    {
        for(OBReadingPara para : paragraphs)
            for(OBReadingWord rw : para.words)
            {
                if(rw.label != null && rw.label.propertyValue("pos") != null)
                {
                    rw.label.setPosition((PointF)rw.label.propertyValue("pos"));
                }
                if(rw.settings.get("dash")  != null)
                {
                    OBControl dash = (OBControl)rw.settings.get("dash");
                    if(dash.propertyValue("pos") != null)
                    {
                        dash.setPosition((PointF)dash.propertyValue("pos"));
                    }
                }
            }
    }

    public void animatePositions()
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBReadingPara para : paragraphs)
            for(OBReadingWord rw : para.words)
            {
                if(rw.label.propertyValue("pos") != null)
                {
                    PointF pos = (PointF)rw.label.propertyValue("pos");
                    anims.add(OBAnim.moveAnim(pos,rw.label));
                }
                if(rw.settings.get("lclabel") != null )
                {
                    OBLabel label = (OBLabel) rw.settings.get("lclabel");
                    PointF pos = (PointF)label.propertyValue("pos");
                    anims.add(OBAnim.moveAnim(pos,label));
                }
                if(rw.settings.get("dash") != null)
                {
                    OBControl dash = (OBControl)rw.settings.get("dash");
                    if(dash.propertyValue("pos") != null)
                    {
                        PointF pos = (PointF)dash.propertyValue("pos");
                        anims.add(OBAnim.moveAnim(pos,dash));
                    }
                }
            }
        OBAnimationGroup.runAnims(anims,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public float layOutText()
    {
        OBFont font = StandardReadingFontOfSize(fontSize);
        Map attributes = lineAttributes(font);
        float lineHeight = fontSize;
        float topY = 0;
        float indent = 0;
        float rightEdge = textBox.bounds.width();
        textJustification = TEXT_JUSTIFY_CENTRE;
        for(OBReadingPara para : paragraphs)
        {
            List<OBReadingWord> currLine = new ArrayList<>();
            PointF pos = new PointF(indent, topY);
            int lineStartIndex = 0;
            float lineStart = indent;
            boolean markFirstWord = !phraseMode;
            for(OBReadingWord w : para.words)
            {
                List<OBReadingWord> warr = new ArrayList<>(currLine);
                warr.add(w);
                float width = workOutWordWidths(warr,attributes);
                if((w.flags & WORD_SPEAKABLE) != 0)
                {
                    if(markFirstWord)
                    {
                        w.settings.put("firstword",true);
                        markFirstWord = false;
                    }
                    if(width + lineStart > rightEdge)
                    {
                        OBReadingWord lastw = currLine.get(currLine.size()-1);
                        if(IsLeftHanger(lastw.text))
                            currLine.remove(currLine.size()-1);
                        else
                            lastw = null;
                        layOutLine(currLine,0,rightEdge,topY,textJustification,font,para.text);
                        currLine.clear();
                        if(lastw != null)
                            currLine.add(lastw);
                        topY +=(lineHeight * lineHeightMultiplier);
                        pos.y = (topY);
                        lineStart = 0;
                        lineStartIndex =(int)w.index;
                    }
                }
                currLine.add(w);
            }
            if(currLine.size()  > 0)
                layOutLine(currLine,0,rightEdge,topY,textJustification,font,para.text);
            topY +=(lineHeight * lineHeightMultiplier * paraMultiplier);
        }
        return topY;
    }

    public boolean newPicRequiredForScene(String scene)
    {
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

    public void setSceneb()
    {

    }

    public void setScenec()
    {
        reminderAudio = currentAudio("PROMPT.REMINDER");
    }

    public void considerReprompt()
    {
        if(reminderAudio != null)
        {
            long sttime = statusTime;
            try
            {
                waitForSecs(0.3f);
                waitAudio();
                if(sttime == statusTime)
                    reprompt(sttime,reminderAudio,7,null);
            }
            catch (Exception e)
            {
            }
        }
    }

    public void endBody()
    {
        considerReprompt();
    }


    public void demoa() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);
        if(showPic)
        {
            showPic();
            waitForSecs(0.4f);
        }
        showPhrase();
        waitForSecs(0.4f);
        slideInLabels();
        waitForSecs(0.4f);
        nextScene();
    }

    public float rightMostLabelX()
    {
        float maxx = 0;
        for(OBLabel l : bottomLabels)
        {
            float r = l.right();
            if(r > maxx)
                maxx = r;
        }
        return maxx;
    }

    public void flyLabelsToBottom(List<OBLabel> labs) throws Exception
    {
        lockScreen();
        for(OBLabel l : labs)
            l.show();
        unlockScreen();
        RectF botrect = objectDict.get("bottomrect") .frame();
        List anims = new ArrayList<>();
        for(OBLabel l : labs)
        {
            if(!botrect.contains(l.position().x, l.position().y))
            {
                anims.add(OBAnim.moveAnim((PointF)l.propertyValue("botpos"),l));
            }
        }
        if(anims.size()  > 0)
        {
            OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
            waitForSecs(0.5f);
        }
    }

    public void demob() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        playAudioQueuedScene("DEMO",true);
        RectF botrect = objectDict.get("bottomrect") .frame();
        PointF destpt = new PointF((rightMostLabelX()  + botrect.width() / 2f),botrect.centerY());
        PointF startpt = pointForDestPoint(destpt,45);
        loadPointerStartPoint(startpt,destpt);

        List<OBReadingWord> rwArray = new ArrayList<>();
        OBReadingWord rw = dashedWord();
        while(rw != null)
        {
            OBLabel l = candidateLabelWithText(rw.text.toLowerCase());
            movePointerToPoint(OB_Maths.locationForRect(0.5f, 0.7f, l .frame()),-1,true);
            PointF pos = (PointF) labelOfTargetWord(rw).settings.get("pos");
            pos = convertPointFromControl(pos,textBox);
            l.setZPosition(l.zPosition()  + 30);
            moveObjects(Arrays.asList(l,thePointer),pos,-0.5f,OBAnim.ANIM_EASE_IN_EASE_OUT);
            l.setZPosition(l.zPosition()  - 30);
            OBLabel wLabel = labelOfTargetWord(rw);
            lockScreen();
            wLabel.setZPosition(l.zPosition() );
            wLabel.setPosition(convertPointToControl(l.position(),textBox));
            wLabel.setColour(l.colour() );
            wLabel.show();
            l.hide();
            unlockScreen();
            rw.settings.remove("missing");
            layOutText();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    movePointerForwards(applyGraphicScale(-100),0.2f);
                }
            });
            animatePositions();
            playSfxAudio("wordin",false);
            lockScreen();
            ((OBControl)rw.settings.get("dash")).hide();
            wLabel.setColour(Color.BLACK);
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
            if(rw.settings.get("lclabel") != null)
            {
                waitForSecs(0.2f);
                playSfxAudio("caps",false);
                lockScreen();
                ((OBControl)rw.settings.get("lclabel")).hide();
                rw.label.show();
                unlockScreen();
                waitForSecs(0.1f);
                waitSFX();
                waitForSecs(0.2f);
            }
            rwArray.add(rw);
            rw = dashedWord();
        }
        sequenceToken = 0;
        readParagraph(0,0,false);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
        for(OBReadingWord rwd : rwArray)
        {
            rwd.settings.put("missing",true);
            rwd.label.hide();
        }
        flyLabelsToBottom(bottomLabels);
        layOutText();
        animatePositions();
        for(OBReadingWord rwd : rwArray)
        {
            ((OBControl)rwd.settings.get("dash")).show();
        }
        waitForSecs(0.6f);
        playAudioQueuedScene("DEMO2",true);
        nextScene();
    }

    public OBControl hitDash(OBControl targ)
    {
        List<OBControl>overlaps = new ArrayList<>();
        RectF f = targ .frame();
        float h = applyGraphicScale(fontSize) * 0.75f;
        for(OBControl dash : dashes())
        {
            RectF f2 = convertRectFromControl(dash.bounds(),dash);
            float diff = h - f2.height();
            f2.top -= diff;
            if(RectF.intersects(f2,f))
                overlaps.add(dash);
        }
        if(overlaps.size()  == 0)
            return null;
        if(overlaps.size()  == 1)
            return overlaps.get(0);
        float maxOverlap = 0;
        OBControl maxOverlapper = null;
        for(OBControl dash : overlaps)
        {
            RectF f2 = convertRectFromControl(dash.bounds(),dash);
            RectF r = new RectF();
            r.setIntersect(f, f2);
            float area = r.width() * r.height();
            if(area > maxOverlap)
            {
                maxOverlap = area;
                maxOverlapper = dash;
            }
        }
        return maxOverlapper;
    }

    public void doMainXX() throws Exception
    {
        showStuff();
        boolean plural = dashes().size() > 1;
        setReplayAudio((List<Object>)(Object) coalesce(plural?currentAudio("PROMPT2.REPEAT"):null,currentAudio("PROMPT.REPEAT")));
        List<String>aud = coalesce(plural?currentAudio("PROMPT2"):null,currentAudio("PROMPT"));
        if(aud != null)
            playAudioQueued((List<Object>)(Object)aud,false);
    }

    public void nextDash() throws Exception
    {
        if(dashes().size() == 0)
        {
            gotItRightBigTick(true);
            waitForSecs(0.2f);
            sequenceToken = 0;
            readParagraph(0,0,false);
            waitForSecs(1.3f);
            nextScene();
        }
    else
        {
            switchStatus(currentEvent());
            considerReprompt();
        }
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBLabel targ =(OBLabel)target;
            target = null;
            OBControl targetDash = hitDash(targ);
            if(targetDash == null)
            {
                moveObjects(Arrays.asList((OBControl)targ),(PointF)targ.propertyValue("botpos"),0.2f,OBAnim.ANIM_EASE_IN_EASE_OUT);
                switchStatus(currentEvent());
                return;
            }
            OBReadingWord rw = wordForDash(targetDash);
            String lctext = rw.text.toLowerCase();
            if(lctext.equals(targ.text()))
            {
                OBLabel wLabel = labelOfTargetWord(rw);
                lockScreen();
                wLabel.setZPosition(targ.zPosition() );
                wLabel.setPosition(convertPointToControl(targ.position(),textBox));
                wLabel.setColour(targ.colour() );
                wLabel.show();
                targ.hide();
                unlockScreen();
                rw.settings.remove("missing");
                rw.settings.remove("dash");
                layOutText();
                animatePositions();
                playSfxAudio("wordin",false);
                lockScreen();
                //detachControl(targetDash);
                textBox.removeMember(targetDash);
                wLabel.setColour(Color.BLACK);
                unlockScreen();
                waitForSecs(0.2f);
                waitSFX();
                if(rw.settings.get("lclabel") != null)
                {
                    waitForSecs(0.2f);
                    playSfxAudio("caps",false);
                    lockScreen();
                    ((OBControl)rw.settings.get("lclabel")).hide();
                    rw.settings.remove("lclabel");
                    rw.label.show();
                    unlockScreen();
                    waitForSecs(0.1f);
                    waitSFX();
                    waitForSecs(0.2f);
                }
                nextDash();
                return;
            }
            gotItWrongWithSfx();

            PointF destpt = (PointF) targ.propertyValue("botpos");
            moveObjects(Arrays.asList((OBControl)targ),destpt,-2f,OBAnim.ANIM_EASE_IN_EASE_OUT);
            targ.setZPosition(targ.zPosition() - 30);
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
        target.setZPosition(target.zPosition() + 30);
        dragOffset = OB_Maths.DiffPoints(target.position(), pt);
    }

    public OBControl findTarget(PointF pt)
    {
        if(objectDict.get("bottomrect").frame().contains(pt.x,pt.y))
        for(OBLabel l : bottomLabels)
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
            target = findTarget(pt);
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
