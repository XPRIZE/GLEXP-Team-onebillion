package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBStroke;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutFloat;
import org.onebillion.onecourse.utils.UPath;
import org.onebillion.onecourse.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 14/07/16.
 */
public class OC_LetterTrace extends OC_LTrace
{

    public long switchStatus(String scene)
    {
        if(scene.compareTo("g") < 0)
            return setStatus(STATUS_AWAITING_CLICK);
        if(scene.compareTo("k") < 0)
            return setStatus(STATUS_AWAITING_CLICK2);
        return setStatus(STATUS_WAITING_FOR_TRACE);
    }

    public void setSceneintro()
    {
        super.setSceneXX(currentEvent());
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF pt = presenter.control.position();
        presenter.control.setProperty("restpos",new PointF(pt.x,pt.y));
        presenter.control.setRight(0);
        presenter.control.show();
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

    public void setScenea2()
    {
        targets = Collections.singletonList((OBControl)hotPath);
    }

    public void setSceneb()
    {
        targets = Collections.singletonList((OBControl)hotPath);
    }

    public void setScenec()
    {
        targets = Collections.singletonList((OBControl)hotPath);
    }

    public void setSceneg()
    {
        setSceneXX(currentEvent());
        prepareForStroke();
        dot = objectDict.get("dot");
        ((OBPath)dot).sizeToBoundingBoxIncludingStroke();
        dot.setZPosition(30);
        PointF pt = paths.get(0).sAlongPath(0,null);
        pt = convertPointFromControl(pt,paths.get(0));
        dot.setPosition(pt);
        dot.hide();
    }

    public void setSceneg2()
    {
        currPathIdx = 0;
        targets = Collections.singletonList(dot);
        prepareForStroke();
    }

    public void setSceneh()
    {
        setSceneg2();
    }

    public void setScenei()
    {
        setSceneg2();
    }

    public void setScenej()
    {
        deleteControls("dot");
        setSceneXX("j");
        OBPath greyswatch = (OBPath) objectDict.get("greyswatch");
        OBPath orangeswatch = (OBPath) objectDict.get("orangeswatch");
        grey = greyswatch.fillColor();
        orange = orangeswatch.fillColor();
        List<OBPath>gryPaths = new ArrayList<>();
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

            p.hide();
            p.setStrokeEnd(0);
        }
        greyPaths = gryPaths;
        orangePaths = orngePaths;
        dot = objectDict.get("dot2");
        ((OBPath)dot).sizeToBoundingBoxIncludingStroke();
        dot.setZPosition(10);
        dot.hide();
    }

    public void setScenek()
    {
        if(greyPaths.size()  == 0)
            setScenej();
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



    public void setScenek2()
    {
        currPathIdx = 0;
        prepareForTrace(currPathIdx);
    }

    public void setScenel()
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

    public void setScenem()
    {
        setScenel();
    }

    public void setScenen()
    {
        setScenel();
    }


    public void doMainh() throws Exception
    {
        waitForSecs(0.3f);
        PointF pt = paths.get(0).sAlongPath(0,null);
        pt = convertPointFromControl(pt,paths.get(0));
        dot.setPosition(pt);
        dot.show();
        playSfxAudio("doton",true);
        waitForSecs(0.3f);
        doMainXX();
    }

    public void doMaini() throws Exception
    {
        doMainh();
    }


    public void doMainl() throws Exception
    {
        showLetter();
        waitSFX();
        waitForSecs(0.3f);
        doMainXX();
        waitForSecs(0.3f);
        preTrace(0);
    }
    public void doMainm() throws Exception
    {
        doMainl();
    }

    public void doMainn() throws Exception
    {
        doMainl();
    }

    public void demointro() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List<Object> aud = (List<Object>)(Object)currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(1.5f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.5f);

        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(),0),head);
        PointF currPos = presenter.control.position();
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds().width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(2)),this);
        waitForSecs(0.2f);
        OBControl faceright = presenter.control.objectDict.get("faceright");
        PointF lp = presenter.control.convertPointFromControl(new PointF(0, 0),faceright);
        PointF destpos = new PointF(bounds().width() + lp.x + 1, currPos.y);
        presenter.walk(destpos);
        detachControl(presenter.control);
        presenter = null;
        nextScene();
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
        waitForSecs(0.6f);
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }


    public void demod() throws Exception
    {
        slideOff();
        PointF destpt = OB_Maths.locationForRect(0.6f, 0.7f, bounds());
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
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        demoStroke();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demog() throws Exception
    {
        PointF destpt = OB_Maths.locationForRect(0.6f, 0.7f, bounds());
        PointF startpt = pointForDestPoint(destpt,35);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.3f);
        dot.show();
        playSfxAudio("doton",true);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.8f, thePointer.position(), dot.bottomRight()),-1,true);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
        nextScene();
    }


    public void demoj() throws Exception
    {
        showLetter();
        waitAudio();
        waitForSecs(0.3f);
        //PointF destpt = OB_Maths.locationForRect(1, 1.2, [paths.lastObject() frame]);
        PointF destpt = paths.get(0).sAlongPath(0,null);
        destpt = convertPointFromControl(destpt,paths.get(0));

        PointF startpt = pointForDestPoint(destpt,25);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.4f, startpt, destpt),-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",0,true);
        waitForSecs(0.3f);
        //movePointerToPoint(TPointAlongLine(0.8, startpt, destpt),-1,true);
        waitForSecs(0.3f);
        playAudioQueuedSceneIndex(currentEvent(),"DEMO",1,true);
        waitForSecs(0.3f);
        for(int i = 0;i < paths.size();i++)
        {
            waitForSecs(0.3f);
            demoSubPathStroke(i);
            waitForSecs(0.4f);
        }
        waitForSecs(0.3f);
        playLetterSound(letter);
        waitAudio();
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1f);
        slideOff();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demok() throws Exception
    {
        showLetter();
        waitAudio();
        waitForSecs(0.5f);
        PointF destpt = paths.get(0).sAlongPath(0,null);
        destpt = convertPointFromControl(destpt,paths.get(0));
        PointF startpt = pointForDestPoint(destpt,25);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.4f, startpt, destpt),-1,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        preTrace(0);
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.tPointAlongLine(0.8f, startpt, destpt),-1,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.3f);
        thePointer.hide();
        nextScene();
    }

}

