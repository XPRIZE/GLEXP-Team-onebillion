package com.maq.xprize.onecourse.mainui.oc_phrases;

import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBFont;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBReadingPara;
import com.maq.xprize.onecourse.utils.OBReadingWord;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.maq.xprize.onecourse.utils.OBUtils.StandardReadingFontOfSize;
import static com.maq.xprize.onecourse.utils.OBUtils.UnscaledReadingFontOfSize;

/**
 * Created by alan on 02/11/2017.
 */

public class OC_Cloze extends OC_PhraseSentence
{
    List<Integer> missingIndexes;
    List<OBReadingWord>missingWords;
    List<OBControl>dashes;
    List<OBLabel>bottomLabels;
    List<String>distractors;
    float dashWidth;
    Map<String,OBPhoneme> wordDict;
    boolean needDemo,showPic,phraseMode;
    List reminderAudio;

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }


    public void adjustTextPosition()
    {
        List wlines = wordExtents();
        if(wlines.size() <= 1)
            return;
        List<OBReadingWord> arr = (List) wlines.get(0);
        OBReadingWord w = arr.get(0);
        float centy = w.frame.centerY();
        arr = (List) wlines.get(wlines.size()-1);
        w = arr.get(0);
        float y2 = w.frame.centerY();
        float midy =(centy + y2) / 2;
        float diff = centy - midy;
        if(diff != 0)
        {
            PointF pos = new PointF();
            pos.set(textBox.position());
            pos.y += diff;
            textBox.setPosition(pos);
            calcWordFrames();
        }
    }

List<Integer>indicesFromArray(List<String>arr)
    {
        List<Integer> res = new ArrayList<>();
        for(String s : arr)
        {
            int i = Integer.parseInt(s);
            res.add(i);
        }
        return res;
    }

    public void processParms(List parms)
    {
        List<Integer> mIarr = new ArrayList<>();
        List<String> distarr = new ArrayList<>();
        Pattern p = Pattern.compile("\\d+");
        for(int i = 1;i < parms.size();i++)
        {
            String s = (String) parms.get(i);
            Matcher matcher = p.matcher(s);
            if(matcher.matches())
            {
                int mI = Integer.parseInt(s);
                mIarr.add(mI);
            }
            else
            {
                distarr.add(s);
            }
        }
        missingIndexes = mIarr;
        distractors = distarr;
    }

    public OBLabel labelOfTargetWord(OBReadingWord rw)
    {
        OBLabel l = (OBLabel) rw.settings.get("lclabel");
        if(l != null)
            return l;
        return rw.label;
    }

    public void createBottomLabels()
    {
        List<OBLabel>botlabs = new ArrayList<>();OBFont font = StandardReadingFontOfSize(fontSize);
        int labelcol = objectDict.get("bottomlabelswatch").fillColor();
        for(String s : distractors)
        {
            OBPhoneme p = wordDict.get(s);
            OBLabel lab = new OBLabel(p.text,font);
            lab.setColour(labelcol);
            botlabs.add(lab);
        }
        for(OBReadingWord rw : missingWords)
        {
            OBLabel lab;
            if(rw.settings.get("lclabel")  == null)
                lab = new OBLabel(rw.text,font);
            else
                lab = new OBLabel(rw.text.toLowerCase(),font);
            lab.setColour(labelcol);
            botlabs.add(lab);
        }
        for(OBLabel lab : botlabs)
        {
            attachControl(lab);
            lab.hide();
        }
        bottomLabels = botlabs;
    }

    public void positionBottomLabels()
    {
        List<OBLabel> labs = OBUtils.randomlySortedArray(bottomLabels);
        List<Map> comps = new ArrayList<>();
        Map m = new HashMap();
        m.put("fixed",false);
        m.put("width",2f);
        comps.add(m);
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
            spacer.put("width",1f);
        }
        comps.add(m);
        RectF frme = objectDict.get("bottomrect").frame();
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
                float tempx = x + l.width()  / 2;
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

    public void cleanScene()
    {
        if (bottomLabels != null)
            for(OBControl c : bottomLabels)
                detachControl(c);
        if (paragraphs != null)
            for(OBReadingPara para : paragraphs)
                for(OBReadingWord rw : para.words)
                {
                    if(rw.label != null)
                        detachControl(rw.label);
                }
        for (int i = textBox.members.size()-1;i >= 0;i--)
        {
            textBox.removeMemberAtIndex(i);
        }
    }

    public void showPic() throws Exception
    {
        OBControl im = mainPic;
        if(im != null  && im.hidden() )
        {
            im.show();
            playSfxAudio("picon",true);
        }
    }

    public void showPhrase() throws Exception
    {
        lockScreen();
        for(OBReadingPara para : paragraphs)
            for(OBReadingWord rw : para.words)
            {
                if(rw.settings.get("missing") == null)
                {
                    if (rw.label != null)
                        rw.label.show();
                }
                else
                {
                    OBControl dash = (OBControl) rw.settings.get("dash");
                    dash.show();
                }

            }
        unlockScreen();
        playSfxAudio("incomplete_on",true);
    }

    List<OBControl>dashes()
    {
        List<OBControl> arr = new ArrayList<>();
        for(OBReadingPara para : paragraphs)
        {
            for(OBReadingWord rw : para.words)
            {
                OBControl d = (OBControl) rw.settings.get("dash");
                if(d != null)
                    arr.add(d);
            }
        }
        return arr;
    }

    public OBReadingWord wordForDash(OBControl dash)
    {
        for(OBReadingPara para : paragraphs)
        {
            for(OBReadingWord rw : para.words)
            {
                OBControl d = (OBControl) rw.settings.get("dash");
                if(d == dash)
                    return rw;
            }
        }
        return null;
    }


    public void showStuff() throws Exception
    {
        if(showPic)
        {
            showPic();
            waitForSecs(0.4f);
        }
        if(bottomLabels.get(0).hidden() )
        {
            showPhrase();
            waitForSecs(0.4f);
            slideInLabels();
        }
    }

    public void hideAll() throws Exception
    {
        playSfxAudio("alloff",false);
        lockScreen();
        hideControls("mainpic");
        for(OBLabel lab : bottomLabels)
            lab.hide();
        for(OBReadingPara para : paragraphs)
            for(OBReadingWord rw : para.words)
            {
                if (rw.label != null)
                    rw.label.hide();
            }
        unlockScreen();
        waitForSecs(0.2f);
        waitSFX();
        waitForSecs(0.2f);
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
                    waitForSecs(0.3f);
                }
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        setScene(events.get(eventIndex));
                    }
                });
            } catch (Exception e)
            {
            }
         }
    }


    public void slideInLabels() throws Exception
    {
        float w = bounds().width();
        List<OBAnim> anims = new ArrayList<>();
        lockScreen();
        for(OBLabel lab : bottomLabels)
        {
            PointF bpos = (PointF) lab.propertyValue("botpos");
            PointF pos = new PointF(bpos.x,bpos.y);
            pos.x += w;
            lab.setPosition(pos);
            lab.show();
            anims.add(OBAnim.moveAnim(bpos,lab));
        }
        unlockScreen();
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        waitSFX();
        waitForSecs(0.5f);
    }

    public OBReadingWord dashedWord()
    {
        for(OBReadingPara para : paragraphs)
            for(OBReadingWord rw : para.words)
            {
                if(rw.settings.get("missing") != null)
                return rw;
            }
        return null;
    }

    List<OBLabel>candidateLabels()
    {
        RectF botrect = objectDict.get("bottomrect").frame();
        List<OBLabel> ls = new ArrayList<>();
        for(OBLabel l : bottomLabels)
            if(botrect.contains(l.position().x,l.position().y ) && !l.hidden() )
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

}
