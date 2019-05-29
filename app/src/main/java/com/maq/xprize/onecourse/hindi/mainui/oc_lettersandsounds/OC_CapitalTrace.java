package com.maq.xprize.onecourse.hindi.mainui.oc_lettersandsounds;

import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by alan on 24/08/2017.
 */

public class OC_CapitalTrace extends OC_LTrace
{
    public long switchStatus(String scene)
    {
        if(scene.compareTo("b") == 0)
            return setStatus(STATUS_AWAITING_CLICK);
         return setStatus(STATUS_WAITING_FOR_TRACE);
    }

    public String tracingFileName()
    {
        return "tracingcapitalletters";
    }

    public void frigEvents()
    {
        String s;
        if((s = parameters.get("notraces"))!=null )
        {
            int noTraces = Integer.parseInt(s);
            if(noTraces > 3)
            {
                int i = events.indexOf("k");
                if(i >= 0)
                {
                    for(int j = 0;j < noTraces - 3;j++)
                        events.add(i,"j");
                }
            }
        }
    }

    public void setScenea()
    {
        loadLetters();
        allowedDistance = paths.get(0).lineWidth() * 1.3f;
        createConvexHull();
        super.setSceneXX(currentEvent());
        OBPath swatch = (OBPath)objectDict.get("letterswatch");
        hiColour = swatch.fillColor();
        normalColour = paths.get(0).strokeColor();
        for(OBPath p : paths)
        {
            p.setStrokeColor(normalColour);
            PointF pos = new PointF();
            pos.set(p.position());
            p.setProperty("origpos",pos);
            p.setPosition(OB_Maths.OffsetPoint(pos, bounds().width(), 0));
            p.setZPosition(3);
        }
    }

    public void setSceneb()
    {
        targets = Collections.singletonList((OBControl)hotPath);
    }

    public void setScenef()
    {
        deleteControls("dot");
        setSceneXX("f");
        OBPath greyswatch = (OBPath) objectDict.get("greyswatch");
        OBPath orangeswatch = (OBPath) objectDict.get("orangeswatch");
        grey = greyswatch.fillColor();
        orange = orangeswatch.fillColor();
        List<OBPath> gryPaths = new ArrayList<>();
        List<OBPath>orngePaths = new ArrayList<>();
        for(OBPath p : paths)
        {
            p.setPosition((PointF) p.propertyValue("origpos"));
            OBPath pc = (OBPath)p.copy();
            pc.setZPosition(2);
            pc.setStrokeColor(orange);
            attachControl(pc);
            pc.hide();
            orngePaths.add(pc);

            pc = (OBPath)p.copy();
            pc.setZPosition(1);
            pc.setStrokeColor(grey);
            attachControl(pc);
            pc.hide();
            gryPaths.add(pc);

            p.show();
            p.setStrokeEnd(0);
        }
        greyPaths = gryPaths;
        orangePaths = orngePaths;
        dot = objectDict.get("dot2");
        ((OBPath)dot).sizeToBoundingBoxIncludingStroke();
        dot.setZPosition(10);
        dot.hide();
    }

    public void setSceneg()
    {
        if(greyPaths.size()  == 0)
            setScenef();
        for(OBPath p : paths)
        {
            p.setPosition((PointF) p.propertyValue("origpos"));
            p.setZPosition(3);
            p.hide();
        }
        for(OBPath p : greyPaths)
        {
            p.setStrokeColor(grey);
            p.setZPosition(1);
        }
    }

    public void setSceneh()
    {
        currPathIdx = 0;
        prepareForTrace(currPathIdx);
    }

    public void setScenei()
    {
        for(OBPath p : paths)
        {
            p.setPosition((PointF)p.propertyValue("origpos"));
            p.setZPosition(3);
            p.hide();
        }
        currPathIdx = 0;
        prepareForTrace(currPathIdx);
        for(OBPath p : greyPaths)
        {
            p.setStrokeColor(grey);
            p.setZPosition(1);
        }
    }

    public void setScenej()
    {
        setScenei();
    }

    public void setScenek()
    {
        setScenei();
    }

    public void doMaini() throws Exception
    {
        showLetter();
        waitSFX();
        waitForSecs(0.3f);
        doMainXX();
        waitForSecs(0.3f);
        preTrace(0);
    }
    public void doMainj() throws Exception
    {
        doMaini();
    }

    public void doMaink() throws Exception
    {
        doMaini();
    }

    public void demoa() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for (OBControl obj : paths)
            anims.add(OBAnim.moveAnim((PointF) obj.propertyValue("origpos"),obj));
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.6f,true,OBAnim.ANIM_EASE_OUT,this);
        waitForSecs(0.3f);

        float rt = hotPath.right() + paths.get(0).lineWidth();
        if (rt > 0.96 * bounds().right)
            rt = 0.96f * bounds().right;
        PointF destpt = OB_Maths.locationForRect(0, 0.6f, bounds());
        destpt.set(rt, destpt.y);

        PointF startpt = OB_Maths.locationForRect(1, 1, bounds());
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.4f, startpt, destpt),-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.3f);
        movePointerToPoint(destpt,-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.2f);
        thePointer.hide();
        nextScene();
    }

    public void doStrokeDemo() throws Exception
    {
        float rt = hotPath.right() + paths.get(0).lineWidth();
        PointF destpt = OB_Maths.locationForRect(0, 0.6f, bounds());
        destpt.set(rt, destpt.y);
        PointF startpt = pointForDestPoint(destpt,35);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.3f);

        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        demoStroke();
        waitForSecs(0.3f);
        nextScene();
    }

    public void democ() throws Exception
    {
        doStrokeDemo();
    }

    public void demod() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        demoStroke();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demoe() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        demoStroke();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demof() throws Exception
    {
        showLetter();
        waitAudio();
        waitForSecs(0.3f);
        float rt = hotPath.right() + paths.get(0).lineWidth();
        PointF destpt = OB_Maths.locationForRect(0, 0.6f, bounds());
        destpt.set(rt, destpt.y);

        PointF startpt = pointForDestPoint(destpt,25);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.3f);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.3f);
        for(int i = 0;i < paths.size();i++)
        {
            waitForSecs(0.3f);
            demoSubPathStroke(i);
            waitAudio();
        }
        waitForSecs(0.3f);
        playFeedback(letter);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        slideOff();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demog() throws Exception
    {
        showLetter();
        waitAudio();
        waitForSecs(0.5f);
        PointF destpt = paths.get(0).sAlongPath(0,null);
        destpt = convertPointFromControl(destpt,paths.get(0));
        PointF startpt = pointForDestPoint(destpt,25);
        loadPointerStartPoint(startpt,destpt);

        float rt = hotPath.right() + paths.get(0).lineWidth();
        PointF dpt = OB_Maths.locationForRect(0, 0.6f, bounds());
        dpt.set(rt, dpt.y);

        movePointerToPoint(dpt,-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.3f);
        preTrace(0);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.8f, startpt, destpt),-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.3f);
        thePointer.hide();
        nextScene();
    }
    public void playFeedback(String l)
    {
        playLetterName(l.toLowerCase());
    }

}
