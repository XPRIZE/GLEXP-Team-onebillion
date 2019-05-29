package com.maq.xprize.onecourse.hindi.mainui.oc_onsetandrime;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.*;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.oc_reading.OC_Reading;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBConditionLock;
import com.maq.xprize.onecourse.hindi.utils.OBFont;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBReadingWord;
import com.maq.xprize.onecourse.hindi.utils.OBUserPressedBackException;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds.OC_Wordcontroller.boundingBoxForText;
import static com.maq.xprize.onecourse.hindi.utils.OBAudioManager.AM_MAIN_CHANNEL;
import static com.maq.xprize.onecourse.hindi.utils.OBUtils.LoadWordComponentsXML;
import static com.maq.xprize.onecourse.hindi.utils.OBUtils.StandardReadingFontOfSize;

/**
 * Created by alan on 15/03/2018.
 */

public class OC_Onset extends OC_Reading
{
    boolean showDemo,showPic;
    String rime;
    List<String> wordIDs;
    PointF midright0;
    float y0,yinc,imageOutset;
    OBControl dash;
    OBLabel rimeLabel,completeLabel;
    OBFont font;
    Map <String,OBPhoneme>  wordDict;
    List<String>prefixes;
    List<OBLabel>bottomLabels;
    OBImage image;
    List<OBLabel> labelList;
    List<Integer>finalList;
    boolean reminder1Done;
    OBConditionLock mainAudioLock;

    public boolean showNextButton()
    {
        return false;
    }

    public boolean showRAButton()
    {
        return true;
    }

    public void miscSetUp()
    {
        loadEvent("mastera");
        showDemo = OBUtils.coalesce(parameters.get("demo"),"false").equals("true");
        showPic = OBUtils.coalesce(parameters.get("showpic"),"true").equals("true");
        rime = parameters.get("rime");
        wordIDs = Arrays.asList(parameters.get("words").split(","));
        fontSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize"),"56"));
        letterSpacing = 0;
        font = StandardReadingFontOfSize(fontSize);
        wordDict = LoadWordComponentsXML(true);

        currNo = 0;
        events = new ArrayList<>(Arrays.asList("c2,d,e".split(",")));
        while(events.size()  < wordIDs.size() )
            events.add(events.get(events.size()-1));
        while(events.size()  > wordIDs.size() )
            events.remove(events.size()-1);
        events.add(0,"c");
        if(showDemo)
            events.add(0,"b");
        events.add(0,"a");
        events.addAll(Arrays.asList("f,g,h,i,i2".split(",")));
        reminder1Done = false;
    }

    public void prepare()
    {
        theMoveSpeed = bounds().width();
        inited = true;
        processParams();
        eventsDict = loadXML(getConfigPath(sectionName() + ".xml"));
        loadAudioXML(getConfigPath(sectionAudioName() + "audio.xml"));
        lineHeightMultiplier = 1.33f;
        textJustification = TEXT_JUSTIFY_CENTRE;
        initialised = true;
        loadFingers();
        initialised = true;
        miscSetUp();
        doVisual(currentEvent());
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public long switchStatus(String scene)
    {
        if(scene.equals("i2"))
            return setStatus(STATUS_AWAITING_CLICK);
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public OBLabel createLabelText(String text,OBFont fnt,int col)
    {
        OBLabel lab;
        lab = new OBLabel(text,fnt);
        lab.setColour(Color.BLACK);
        if(letterSpacing != 0.0)
            lab.setLetterSpacing(letterSpacing);
        lab.setZPosition(LABEL_ZPOS);
        return lab;
    }

    public List<String> prefixes()
    {
        List parray = new ArrayList<>();
        int rct =(int) rime.length();
        for(String k : wordIDs)
        {
            OBWord rw =(OBWord)wordDict.get(k);
            String pref = rw.text.substring(0,rw.text.length()  - rct);
            parray.add(pref);
        }
        return parray;
    }

    public float widestPrefixWidth()
    {
        Map attributes = lineAttributes(font);
        float maxlen = 0;
        for(String pref : prefixes)
        {
            float w = WidthOfText(pref, attributes, spaceExtra);
            if(w > maxlen)
                maxlen = w;
        }
        return maxlen;
    }

    public float mDashWidth()
    {
        Map attributes = lineAttributes(font);
        float w = WidthOfText("m", attributes, spaceExtra);
        return w;
    }

    public void createBottomLabels(List<String>fixes)
    {
        List<OBLabel>botlabs = new ArrayList<>();
        int labelcol = objectDict.get("bottomlabelswatch").fillColor();
        for(String s : fixes)
        {
            OBLabel lab = new OBLabel(s,font);
            lab.setColour(labelcol);
            lab.setZPosition(10);
            botlabs.add(lab);
        }
        for(OBLabel lab : botlabs)
        {
            attachControl(lab);
            lab.hide();
        }
        bottomLabels = botlabs;
    }

    private class OARSpacer
    {
        boolean fixed;
        float width;
        OBLabel obj;
        OARSpacer(boolean f,float w,OBLabel o)
        {
            fixed = f;
            width = w;
            obj = o;
        }
    }
    public void positionBottomLabels()
    {
        List<OBLabel> labs = OBUtils.randomlySortedArray(bottomLabels);
        List<OARSpacer> comps = new ArrayList<>();
        comps.add(new OARSpacer(false,2f,null));
        OARSpacer spacer = null;
        for(OBLabel l : labs)
        {
            if(spacer != null)
                comps.add(spacer);
            comps.add(new OARSpacer(true,l.width(),l));
            spacer = new OARSpacer(false,1.0f,null);
        }
        comps.add(new OARSpacer(false,2f,null));
        RectF frme = objectDict.get("bottomrect") .frame();
        float totwidth = 0;
        float totvariable = 0;
        for(OARSpacer d : comps)
        {
            if(d.fixed)
                totwidth += d.width;
            else
                totvariable += d.width;
        }
        float multiplier =(frme.width() - totwidth) / totvariable;
        float y = frme.top + frme.height() / 2;
        float x = 0;
        for(OARSpacer d : comps)
        {
            OBLabel l = d.obj;
            if(l != null)
            {
                float tempx = x + l.width()  / 2;
                PointF pos = new PointF(tempx, y);
                l.setPosition(pos);
                l.setProperty("botpos",pos);
                x += l.width();
            }
            else
            {
                x += d.width  * multiplier;
            }
        }
    }

    public void setupImageBack()
    {
        OBControl backRect = objectDict.get("imageback");
        RectF imagef = objectDict.get("imagerect").frame();
        RectF backf = objectDict.get("imageback").frame();
        imageOutset =(imagef.height() - backf.height()) / 2;
        OBControl br = new OBControl();
        br.setFillColor(backRect.fillColor());
        br.setZPosition(backRect.zPosition());
        br.setFrame(backf);
        detachControl(backRect);
        objectDict.put("imageback",br);
        attachControl(br);

    }

    public void setUpFirstScreen()
    {
        loadEvent("a");

        setupImageBack();

        OBControl r = objectDict.get("rect0");
        midright0 = new PointF(r.right(),r.frame().centerY());
        y0 = objectDict.get("rect1").frame().centerY();
        yinc = objectDict.get("rect2").frame().centerY() - y0;
        rimeLabel = createLabelText(rime,font,Color.BLACK);
        rimeLabel.setPosition(midright0);
        rimeLabel.setRight(midright0.x);
        attachControl(rimeLabel);

        prefixes = prefixes();
        OBControl d = objectDict.get("dash");
        dash = new OBControl();
        dash.setFrame(d.frame());
        dash.setWidth(mDashWidth());
        dash.setRight(rimeLabel.left() );
        dash.setFillColor(d.fillColor() );
        dash.setZPosition(d.zPosition() );

        RectF bb = boundingBoxForText(rime, font);
        dash.setTop(rimeLabel.bottom()  + bb.top);

        attachControl(dash);

        createBottomLabels(prefixes);
        positionBottomLabels();

        labelList = new ArrayList<>();
        setUpScene();
    }

    public void setUpScene()
    {
        if(image != null)
            detachControl(image);

        OBControl backRect = objectDict.get("imageback");

        if(showPic)
        {
            OBImage im = loadImageWithName(wordIDs.get(currNo),new PointF(),boundsf(),false);
            if(im != null)
            {
                OBControl picbox = objectDict.get("imagerect");
                im.setPosition(picbox.position());
                im.setScale(picbox.height() / im.height());
                im.setZPosition(12);
                im.setBackgroundColor(Color.WHITE);
                attachControl(im);
                RectF ir = new RectF(im .frame());
                ir.inset(imageOutset, imageOutset);
                backRect.setFrame(ir);
                im.hide();
            }
            image = im;
        }
        backRect.hide();
        completeLabel = createCompleteLabel(wordDict.get(wordIDs.get(currNo)).text);
        completeLabel.hide();
    }

    public OBLabel createCompleteLabel(String tx)
    {
        OBLabel lab = createLabelText(tx,font,Color.BLACK);
        lab.setPosition(midright0);
        lab.setRight(midright0.x);
        lab.setZPosition(rimeLabel.zPosition() + 1);
        attachControl(lab);
        return lab;
    }

    public void setScenea()
    {
        setUpFirstScreen();
    }

    public void setSceneb()
    {
    }

    public void slideInLabels() throws Exception
    {
        float w = bounds().width();
        List anims = new ArrayList<>();
        lockScreen();
        for(OBLabel lab : bottomLabels)
        {
            PointF bpos = (PointF)lab.propertyValue("botpos");
            PointF pos = new PointF(bpos.x,bpos.y);
            pos.x -= w;
            lab.setPosition(pos);
            lab.show();
            anims.add(OBAnim.moveAnim(bpos,lab));
        }
        unlockScreen();
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.4f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        waitSFX();
        waitForSecs(0.5f);
    }

    public void setScenec()
    {
    }

    public void setScenec2()
    {
    }

    public void setScened()
    {
        setUpScene();
    }

    public void setScenee()
    {
        setUpScene();
    }

    public void setSceneXX(String  scene)
    {
        loadEvent(scene);
    }

    public void setScenef()
    {
        detachControl(rimeLabel);
        rimeLabel = null;
        setSceneXX(currentEvent());
        float rt = objectDict.get("rect0").right();
        OBControl r = objectDict.get("rectb0");
        r.setRight(rt);
        r = objectDict.get("rectb1");
        r.setRight(rt);
    }

    public void setScenei2()
    {
        finalList = OBUtils.RandomIndexesTo(labelList.size() );
        currNo = 0;
    }

    public void setScenefinale()
    {
        loadEvent("finale");
        fontSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize"),"180"));
        font = StandardReadingFontOfSize(fontSize);

        OBControl r = objectDict.get("rect0");
        rimeLabel = createLabelText(rime,font,Color.BLACK);
        rimeLabel.setLeft(r.left() );
        rimeLabel.setTop(r.top() );
        rimeLabel.setZPosition(20);

        attachControl(rimeLabel);

        prefixes = Arrays.asList("M");
        OBControl d = objectDict.get("dash");
        dash = new OBControl();
        dash.setFrame(d .frame());
        dash.setWidth(widestPrefixWidth());
        dash.setRight(rimeLabel.left() );
        dash.setFillColor(d.fillColor() );
        dash.setZPosition(d.zPosition() );
        attachControl(dash);

        RectF bb = boundingBoxForText(rime, font);
        dash.setTop(rimeLabel.bottom()  + bb.top);

        float w1 = dash.left();
        float w2 = bounds() .width() - rimeLabel.right();
        float offset =(w1 + w2) / 2 - w1;
        dash.setLeft(dash.left() + offset);
        rimeLabel.setLeft(rimeLabel.left() + offset);
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            playSfxAudio("bigryme",false);
            lockScreen();
            setScenefinale();
            unlockScreen();
            waitForSecs(0.3f);
            Map fnl = (Map) audioScenes.get("finale");
            playAudioQueued((List)fnl.get("DEMO"),true);
            waitForSecs(0.1f);
            String syllID = String.format("isyl_%s", rime);
            lockScreen();
            rimeLabel.setColour(Color.RED);
            unlockScreen();
            playAudioQueued(Arrays.asList((Object)syllID),true);

            waitForSecs(0.8f);
            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch(Exception exception)
        {
        }
    }

    public void showStuff()
    {
        if(rimeLabel != null && rimeLabel.hidden() )
        {
            float bot = Math.min(dash.bottom(), rimeLabel.bottom());
            PointF dashpos = new PointF();
            dashpos.set(dash.position());
            PointF rimepos = new PointF();
            rimepos.set(rimeLabel.position());
            lockScreen();
            dash.show();
            rimeLabel.show();
            dash.setBottom(dash.bottom()  - bot);
            rimeLabel.setBottom(rimeLabel.bottom()  - bot);
            unlockScreen();
            List anims = Arrays.asList(OBAnim.moveAnim(dashpos,dash),OBAnim.moveAnim(rimepos,rimeLabel));
            OBAnimationGroup.runAnims(anims,0.3f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        }
    }

    public void replayAudio()
    {
        if(busyStatuses.contains(status()))
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

    public List arrayWithCategory(String cat,String wID)
    {
        List aud = currentAudio(cat);
        List oaud = new ArrayList<>();
        if(aud != null)
        {
            oaud.addAll(aud);
            oaud.add(300);
        }
        oaud.add(wID);
        return oaud;
    }

    public void doMainXX()throws Exception
    {
        List aud = currentAudio("PROMPT");
        if(aud != null)
        {
            playAudioQueued(aud,true);
            waitForSecs(0.2f);
        }
        showStuff();
        setReplayAudio(arrayWithCategory("PROMPT.REPEAT",wordIDs.get(currNo)));
        mainAudioLock = playAudioQueued(Arrays.asList((Object)wordIDs.get(currNo)),false);
    }

    public List raForI2()
    {
        List rep = currentAudio("PROMPT.REPEAT");
        List ra = new ArrayList<>();
        if(rep != null)
        {
            ra.addAll(rep);
            ra.add(300);
        }
        int idx = finalList.get(currNo);
        ra.add(wordIDs.get(idx));
        return ra;
    }

    public void doMaini2() throws Exception
    {
        List aud = currentAudio("PROMPT");
        if(aud != null)
        {
            playAudioQueued(aud,true);
            waitForSecs(0.2f);
        }
        setReplayAudio(raForI2());
        int idx = finalList.get(currNo);
        playAudioQueued(Arrays.asList((Object)wordIDs.get(idx)),false);
    }

    public void sayAndFlashDash(long sttime,List audio) throws Exception
    {
        OBConditionLock lck = playAudioQueued(audio,false);
        while(lck.conditionValue()  == PROCESS_NOT_DONE && statusTime  == sttime)
        {
            dash.setOpacity(0.01f);
            waitForSecs(0.2f);
            dash.setOpacity(1);
            waitForSecs(0.2f);
        }
    }

    public void repromptDash(final long sttime,final List audio,float secs)
    {
        if(statusChanged(sttime))
            return;
        OBUtils.runOnOtherThreadDelayed(secs,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(!statusChanged(sttime))
                {
                    if(audio != null)
                    {
                        sayAndFlashDash(sttime,audio);
                    }
                }
            }
        });
    }

    public int wordIdIdxForScene(String scene)
    {
        if(scene.equals("i2"))
            return finalList.get(currNo).intValue();
        return currNo;
    }

    public void endBody()
    {
        if(currentAudio("PROMPT.REMINDER") != null || currentEvent().equals("i2"))
        {
            try
            {
                long stt = statusTime();
                waitForSecs(0.2f);
                waitAudioQueue(mainAudioLock);
                repromptDash(stt,arrayWithCategory("PROMPT.REMINDER",wordIDs.get(wordIdIdxForScene(currentEvent()))),6);
            }
            catch (Exception e)
            {

            }
        }
    }

    static RectF AllLabelsFrame(List<OBLabel>  labs)
    {
        RectF f = new RectF();
        for(OBControl l : labs)
        {
            f.union(l.frame());
        }
        return f;
    }

    public void shuffleLabels() throws Exception
    {
        OBControl r0 = objectDict.get("rectb0");
        OBControl r1 = objectDict.get("rectb1");
        float top0 = r0.top();
        float right = r0.right();
        float inc = r1.top() - r0.top();
        int labct = labelList.size();
        List<Float> ys = new ArrayList(labct);
        for(int i = 0;i < labct;i++)
        {
            ys.add(top0 + i * inc);
        }

        List<String> ls = (List<String>)((Map)audioScenes.get("sfx")).get("reshuffle");
        String fn = ls.get(0);
        OBAudioManager.audioManager.prepare(fn);
        float secs = (float)OBAudioManager.audioManager.duration();
        OBAudioManager.audioManager.playOnChannel(AM_MAIN_CHANNEL);
        for(int i = 0;i < labct;i++)
        {
            List anims = new ArrayList<>();
            OBLabel lab = labelList.get(i);
            float x = lab.position().x;
            anims.add(OBAnim.moveAnim(new PointF(x,ys.get(i)),lab));
            for(int j = i + 1;j < labct;j++)
            {
                lab = labelList.get(j);
                float xx = lab.position().x;
                float y = ys.get(labct - j + i);
                anims.add(OBAnim.moveAnim(new PointF(xx, y),lab));
            }
            OBAnimationGroup.runAnims(anims,secs/labct - 0.05,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
            waitForSecs(0.05f);
        }
    }

    public void  scatterLabels()throws Exception
    {
        RectF largerect = objectDict.get("largerect").frame();
        List<OBControl>rects = OBUtils.randomlySortedArray(filterControls("smallrect.*").subList(0, labelList.size()));
        List anims = new ArrayList<>();
        for(int i = 0;i < labelList.size();i++)
        {
            OBLabel lab = labelList.get(i);
            PointF pos = new PointF();
            pos.set(rects.get(i).position());
            float w = lab.width() / 2;
            if(pos.x - w < largerect.left)
                pos.x = (largerect.left + w);
            else if(pos.x + w > (largerect.right))
                pos.x = ((largerect.right) - w);
            float offset = applyGraphicScale(250);
            Path bez = OBUtils.SimplePath(lab.position() , pos, offset);
            OBAnim anim = OBAnim.pathMoveAnim(lab,bez,false,0);
            anims.add(anim);
        }
        playSfxAudio("scatter",false);
        OBAnimationGroup.runAnims(anims,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void actionsForDemoa() throws Exception
    {
        PointF destpt = OB_Maths.locationForRect(new PointF(0.9f, -0.1f) ,objectDict.get("bottomrect").frame());
        PointF startpt = pointForDestPoint(destpt,10);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.1f);
        playAudioQueuedScene("DEMO",true);
        slideInLabels();
        waitForSecs(0.4f);
    }

    public void actionsForDemoc() throws Exception
    {

        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);

        PointF destpt = OB_Maths.locationForRect(new PointF(0.5f, 2) , dash.frame());
        movePointerToPoint(destpt,-1,true);
        playAudioScene("DEMO",1,true);

        RectF f = AllLabelsFrame(bottomLabels);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0, 0.6f) , f),-35,-1,true);
        playAudioScene("DEMO",2,false);
        waitForSecs(0.5f);

        movePointerToPoint(OB_Maths.locationForRect(new PointF(1, 0.6f) , f),-5,-1,true);
        waitForSecs(0.8f);

        PointF butPt;
        lockScreen();
        butPt = OB_Maths.locationForRect(new PointF(0.5f,1.1f) , MainViewController() .topRightButton.frame);
        unlockScreen();
        movePointerToPoint(butPt,0,-1,true);

        playAudioScene("DEMO",3,true);

        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.65f);
    }

    public void actionsForDemof() throws Exception
    {
        playSfxAudio("paneloff",false);
        objectDict.get("bottomrect").hide();
        waitForSecs(0.2f);
        shuffleLabels();
    }

    public void actionsForDemog(String text,boolean isPref) throws Exception
    {
        int rimeColour = objectDict.get("rimeswatch").fillColor();
        PointF destpt = OB_Maths.locationForRect(new PointF(0.9f, -0.1f) ,objectDict.get("bottomrect").frame());
        PointF startpt = pointForDestPoint(destpt,10);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.2f);
        int textlen =(int) text.length();
        for(OBLabel lab : labelList)
        {
            playSfxAudio("pattern",false);
            lockScreen();
            lab.setColour(rimeColour);
            if(isPref)
                lab.setHighRange(textlen,lab.text().length(),Color.BLACK);
            else
                lab.setHighRange(0 , lab.text().length() - textlen,Color.BLACK);
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.2f);
        }
    }

    public void actionsForDemoh() throws Exception
    {
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",true);
        int i = 0;
        for(OBLabel lab : labelList)
        {
            lockScreen();
            lab.setColour(Color.RED);
            unlockScreen();
            playAudioQueued(Arrays.asList((Object)wordIDs.get(i)),true);
            lockScreen();
            lab.setColour(Color.BLACK);
            unlockScreen();

            waitForSecs(0.2f);
            i++;
        }
        waitForSecs(0.8f);
        scatterLabels();
    }

    public void actionsForDemoi() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        thePointer.hide();
    }


    public void  playAudioWithHighlightAtEnd(boolean atEnd)throws Exception
    {
        lockScreen();
        completeLabel.setColour(Color.RED);
        unlockScreen();
        playAudioQueued(Arrays.asList((Object)wordIDs.get(currNo)),true);
        waitForSecs(0.2f);
        int labelcol = objectDict.get("bottomlabelswatch").fillColor();
        lockScreen();
        completeLabel.setColour(Color.BLACK);
        if (atEnd)
            completeLabel.setHighRange(staticLabel().text().length(),completeLabel.text().length(),labelcol);
        else
            completeLabel.setHighRange(0,bottomLabels.get(currNo).text().length(),labelcol);
        unlockScreen();
    }

    public void  nextItem()throws Exception
    {
        if(++currNo >= wordIDs.size())
            gotItRightBigTick(true);
        nextScene();
    }

    public void  moveLabels()throws Exception
    {
        int labelcol = objectDict.get("greyswatch").fillColor();
        completeLabel.setHighRange(-1,-1,0);
        completeLabel.setColour(labelcol);
        List anims = new ArrayList<>();
        PointF newpos = new PointF();
        newpos.set(completeLabel.position());
        newpos.y = (y0);
        anims.add(OBAnim.moveAnim(newpos,completeLabel));

        for(OBLabel l : labelList)
        {
            newpos = new PointF();
            newpos.set(l.position());
            newpos.y += yinc;
            anims.add(OBAnim.moveAnim(newpos,l));
        }
        playSfxAudio("rowdown",false);
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        labelList.add(completeLabel);
    }

    public void  flyOffWord(OBControl lab)
    {
        PointF startpos = lab.position();
        PointF endpos = new PointF(bounds() .width() / 2,-lab.height()* 2);
        float offset = applyGraphicScale(140);
        if(startpos.x > bounds() .width() / 2)
            offset = -offset;
        Path bez = OBUtils.SimplePath(startpos, endpos, offset);
        OBAnim anim = OBAnim.pathMoveAnim(lab,bez,false,0);
        OBAnimationGroup.runAnims(Arrays.asList(anim),0.35,true,OBAnim.ANIM_EASE_IN,null);
    }

    public void  nextLab()throws Exception
    {
        if(++currNo >= labelList.size())
        {
            gotItRightBigTick(true);
            waitForSecs(0.2f);
            nextScene();
        }
        else
        {
            setReplayAudio(raForI2());
            long stt = switchStatus(currentEvent());
            int idx = finalList.get(currNo).intValue();
            playAudioQueued(Arrays.asList((Object)wordIDs.get(idx)),true);
            repromptDash(stt,Arrays.asList((Object)wordIDs.get(idx)),6);
        }
    }

    public void checkTouchTarget(OBControl targ)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            int idx = finalList.get(currNo).intValue();

            if(labelList.indexOf(targ) == idx)
            {
                gotItRightBigTick(false);
                flyOffWord(targ);
                nextLab();
                return;
            }
            gotItWrongWithSfx();
            waitForSecs(0.1f);
            waitSFX();
            waitForSecs(0.2f);

            playAudioQueued(Arrays.asList((Object)wordIDs.get(idx)),false);

            switchStatus(currentEvent());
        }
        catch(Exception exception)
        {
        }
    }

    public PointF destPointForDraggedLabel(OBLabel targ)
    {
        return new PointF(completeLabel.left() +(targ.width() / 2) ,completeLabel.position() .y);
    }

    public OBLabel staticLabel()
    {
        return rimeLabel;
    }

    public String dragInSFX()
    {
        return "onset";
    }

    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            setStatus(STATUS_CHECKING);
            OBLabel targ =(OBLabel) target;
            target = null;
            RectF targRect = new RectF(dash.frame());
            targRect.union(completeLabel.frame());
            RectF tf = new RectF(targ.frame());
            tf.inset(-tf.height(), -tf.height());
            if(!RectF.intersects(targRect,tf))
            {
                moveObjects(Arrays.asList((OBControl)targ),(PointF)targ.propertyValue("botpos"),0.2f,OBAnim.ANIM_EASE_IN_EASE_OUT);
                switchStatus(currentEvent());
                return;
            }
            int idx = bottomLabels.indexOf(targ);
            if(idx == currNo)
            {
                PointF destpt = destPointForDraggedLabel(targ);
                moveObjects(Arrays.asList((OBControl)targ),destpt,0.2f,OBAnim.ANIM_EASE_IN_EASE_OUT);
                playSfxAudio(dragInSFX(),false);
                int labelcol = objectDict.get("bottomlabelswatch").fillColor();
                lockScreen();
                int len = bottomLabels.get(idx).text().length();
                if (targ.left() > staticLabel().left())
                    completeLabel.setHighRange(staticLabel().text().length(),completeLabel.text().length(),labelcol);
                else
                    completeLabel.setHighRange(0,len,labelcol);
                completeLabel.show();
                targ.hide();
                staticLabel().hide();
                dash.hide();
                unlockScreen();
                waitForSecs(0.2f);
                waitSFX();
                waitForSecs(0.2f);
                playAudioWithHighlightAtEnd(targ.left() > staticLabel().left());
                waitForSecs(0.2f);
                if(image != null)
                {
                    playSfxAudio("picon",false);
                    lockScreen();
                    image.show();
                    objectDict.get("imageback").show();
                    unlockScreen();
                    playAudioWithHighlightAtEnd(targ.left() > staticLabel().left());
                    waitForSecs(1.15f);
                    playSfxAudio("picoff",false);
                    lockScreen();
                    image.hide();
                    objectDict.get("imageback").hide();
                    unlockScreen();
                    waitForSecs(0.2f);
                    waitSFX();
                }
                waitForSecs(0.2f);
                moveLabels();
                nextItem();
                return;
            }
            gotItWrongWithSfx();

            PointF destpt = (PointF)targ.propertyValue("botpos");
            moveObjects(Arrays.asList((OBControl)targ),destpt,-2f,OBAnim.ANIM_EASE_IN_EASE_OUT);
            targ.setZPosition(targ.zPosition()-30);
            waitSFX();

            playAudioQueued(arrayWithCategory("INCORRECT",wordIDs.get(currNo)),false);

            switchStatus(currentEvent());
        }
        catch(Exception exception)
        {
        }
    }

    public void  touchMovedToPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void  touchUpAtPoint(final PointF pt,View  v)
    {
        if(status() == STATUS_DRAGGING)
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
        target.setZPosition(target.zPosition()+30);
        dragOffset = OB_Maths.DiffPoints(target.position(), pt);
    }

    public Object findTarget(PointF pt)
    {
        OBControl br = objectDict.get("bottomrect");
        if(br != null && !br.hidden())
        {
            if(br.frame() .contains(pt.x,pt.y))
                return finger(-1,3,(List<OBControl>)(Object)bottomLabels,pt);
        }
        else
        {
            return finger(-1,3,(List<OBControl>)(Object)labelList,pt);
        }
        return null;
    }

    public void  touchDownAtPoint(final PointF pt,View  v)
    {
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            target = (OBControl) findTarget(pt);
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
        else if(status() == STATUS_AWAITING_CLICK)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTouchTarget(target);
                    }
                });
            }
        }

    }
}
