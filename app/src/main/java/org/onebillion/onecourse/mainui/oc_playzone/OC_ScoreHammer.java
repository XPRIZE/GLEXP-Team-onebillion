package org.onebillion.onecourse.mainui.oc_playzone;

import org.onebillion.onecourse.mainui.OC_SectionController;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 12/01/2018.
 */

public class OC_ScoreHammer extends OC_SectionController
{
    int score;

    public String layoutName()
    {
        return "mastera";
    }

    public void miscSetUp()
    {
        score = Integer.parseInt(OBUtils.coalesce(parameters.get("score") ,"0"));
    }

    public void prepare()
    {
        super.prepare();
        loadEvent(layoutName());
        loadFingers();
        events = Arrays.asList("a");
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

    public OBLabel smallLabelForNumber(int n,OBControl bg,OBFont fnt)
    {
        String s = String.format("%d",n);
        OBLabel l = new OBLabel(s,fnt);
        l.setZPosition(bg.zPosition() +1);
        l.setPosition(bg.position());
        l.setColour(Color.WHITE);
        ((OBGroup) bg.parent).insertMember(l,0,s);
        return l;
    }

    public void setUpFinale()
    {
        loadEvent("finale");
        float txsize = Float.parseFloat(eventAttributes.get("textsize"));
        OBFont fnt = OBUtils.UnscaledReadingFontOfSize(txsize);
        OBGroup machine =(OBGroup) objectDict.get("machine");
        OBControl swatch = objectDict.get("swatch");
        int greycol = swatch.fillColor();
        for(int i = 1;i <= 10;i++)
        {
            OBControl sq = machine.objectDict.get(String.format("squ_%d",i));
            smallLabelForNumber(i,sq,fnt);
            sq.setProperty("origcol",sq.fillColor());
            sq.setFillColor(greycol);
            OBControl star = machine.objectDict.get(String.format("star_%d",i));
            star.setProperty("origcol",star.fillColor());
            star.setFillColor(greycol);
        }
    }

    public void setSceneXX(String scene)
    {
        setUpFinale();
    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.5f);
        playAudioQueuedScene("finale","DEMO",true);
        waitForSecs(0.5f);
        animateFinale();
        waitForSecs(0.5f);
        exitEvent();
    }

    public void animateHammerStrike() throws Exception
    {
        OBControl hammer = objectDict.get("hammer");
        OBControl hammer2 = objectDict.get("hammer2");
        float angle1 = hammer.rotation();
        float angle2 = hammer2.rotation();
        PointF pos1 = new PointF();
        pos1.set(hammer.position());
        PointF pos2 = new PointF();
        pos2.set(hammer2.position());
        moveObjects(Arrays.asList(hammer),pos2,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
        OBAnim rotAnim = OBAnim.rotationAnim(angle2,hammer);
        OBAnimationGroup.runAnims(Arrays.asList(rotAnim),0.1,true,OBAnim.ANIM_EASE_IN,null);
        playSfxAudio("hit",false);
        waitForSecs(0.02f);
        rotAnim = OBAnim.rotationAnim(angle1,hammer);
        OBAnimationGroup.runAnims(Arrays.asList(rotAnim),0.1,true,OBAnim.ANIM_EASE_OUT,null);
        moveObjects(Arrays.asList(hammer),pos1,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
    }

    static String CatNameForIdx(String c,int idx)
    {
        if(idx > 1)
            return String.format("%s%d",c,idx);
        return c;
    }
    public void animateFinale() throws Exception
    {
        animateHammerStrike();
        waitForSecs(0.2f);
        OBGroup machine =(OBGroup) objectDict.get("machine");
        for(int i = 1;i <= score;i++)
        {
            OBControl sq = machine.objectDict.get(String.format("squ_%d",i));
            OBControl star = machine.objectDict.get(String.format("star_%d",i));
            playSfxAudio("ding",false);
            lockScreen();
            sq.setFillColor((Integer)sq.propertyValue("origcol"));
            star.setFillColor((Integer)star.propertyValue("origcol"));
            unlockScreen();
            waitForSecs(0.5f);
        }
        waitForSecs(0.5f);
        String cat = CatNameForIdx("SUMMARY",score + 1);
        OBConditionLock audioLock = playAudioQueuedScene("finale",cat,false);
        if(score > 0)
        {
            OBControl swatch = objectDict.get("swatch");
            int greycol = swatch.fillColor();
            OBControl sq = machine.objectDict.get(String.format("squ_%d",score));
            int col = (Integer)sq.propertyValue("origcol");
            for(int i = 0;i < 5;i++)
            {
                sq.setFillColor(greycol);
                waitForSecs(0.3f);
                sq.setFillColor(col);
                waitForSecs(0.3f);
            }
        }
        waitAudioQueue(audioLock);
        waitForSecs(0.3f);
    }

}
