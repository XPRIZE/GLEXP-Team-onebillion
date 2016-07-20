package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.OBStroke;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.OBSectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBConditionLock;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OB_MutFloat;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.USubPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 14/07/16.
 */
public class X_LetterTrace extends X_Wordcontroller
{
    String letter;
    List<OBPath> paths;
    List<OBPath> greyPaths = new ArrayList<>(),orangePaths;
    List<OBControl>xboxes;
    List<List<OBPath>>letterPaths;
    int normalColour,hiColour,orange,grey;
    OBControl dot;
    int currPathIdx;
    PointF lastTracePoint;
    float tSoFar,tLookAhead,currPathLen,allowedDistance;
    List<List<UPath>>letterupaths = new ArrayList<>();
    List<UPath>upaths = new ArrayList<>();
    UPath currUPath;
    boolean traceComplete;
    OBConditionLock promptAudioLock;
    OBPath hotPath;
    XPRZ_Presenter presenter;

    public void createConvexHull()
    {
        UPath up = new UPath();
        for(List<UPath>lup : letterupaths)
        {
            for (UPath upx : lup)
                up.subPaths.addAll(upx.subPaths);
        }
        USubPath uspch = up.convexHull();
        Path bez = uspch.bezierPath();
        bez.close();
        hotPath = new OBPath(bez);
        int col = Color.argb((int)(0.7*255),255,0,0);
        hotPath.setFillColor(col);
        hotPath.setStrokeColor(col);
        hotPath.setLineWidth(applyGraphicScale(paths.get(0).lineWidth() ));
        hotPath.setLineJoin(OBStroke.kCALineJoinRound);
        hotPath.sizeToBoundingBoxIncludingStroke();
        hotPath.setZPosition(100);
        //attachControl(hotPath);
    }

    public void loadLetters()
    {
        for(int i = 0;i < letter.length();i++)
        {
            for(String n : filterControlsIDs("Path.*|xbox"))
                objectDict.remove(n);
            String character = letter.substring(i,i + 1);
            String l = "_" + character;
            loadEvent(l);
            List<String> idlst = filterControlsIDs("Path.*");
            Collections.sort(idlst);
            List<UPath> lup = new ArrayList<>();
            for (String s : idlst)
            {
                OBPath p = (OBPath) objectDict.get(s);
                p.sizeToBoundingBoxIncludingStroke();
                UPath up = deconstructedPath(l,s);
                lup.add(up);
            }
            letterupaths.add(lup);
            List<OBPath>lpaths = (List<OBPath>)(Object)sortedFilteredControls("Path.*");
            letterPaths.add(lpaths);
            OBControl xbox = objectDict.get("xbox");
            xboxes.add(xbox);
            xbox.hide();
        }
        if(letter.length()  > 1)
        {
            float xboxeswidth = 0,maxy = 0,miny = bounds().height();
            for(int i = 0;i < xboxes.size();i++)
            {
                OBControl xb = xboxes.get(i);
                RectF f = xb .frame();
                xboxeswidth += f.width();
                List<OBPath> arr = letterPaths.get(i);
                RectF fp = OBUtils.PathsUnionRect(arr);
                fp.top -= f.top;
                if(fp.top < miny)
                    miny = fp.top;
                float thismaxy = fp.top + fp.height();
                if(thismaxy > maxy)
                    maxy = thismaxy;
            }
            float pathsheight = maxy - miny;
            float diff =(bounds().height() - pathsheight) / 2;
            float xboxtop = diff - miny;
            float left =(bounds().width() - xboxeswidth) / 2;
            for(int i = 0;i < xboxes.size();i++)
            {
                OBControl xb = xboxes.get(i);
                float xdiff = left - xb.left();
                float ydiff = xboxtop - xb.top();
                xb.setPosition(OB_Maths.OffsetPoint(xb.position(), xdiff, ydiff));
                for(OBPath p : letterPaths.get(i))
                    p.setPosition(OB_Maths.OffsetPoint(p.position(), xdiff, ydiff));

                Matrix m = new Matrix();
                m.postTranslate(xdiff,ydiff);
                List<UPath> lup = letterupaths.get(i);
                for (UPath up : lup)
                    up.transformByMatrix(m);
                left += xb.width();
            }
        }
        for(List arr : letterPaths)
            paths.addAll(arr);
        for(List arr : letterupaths)
            upaths.addAll(arr);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = new ArrayList<>(Arrays.asList(eva));
        String s;
        if((s = parameters.get("intro")) != null && s.equals("true"))
            events.add(0,"intro");
        if ((s = parameters.get("revision"))!=null && s.equals("true"))
            for(String ev : "b,c,e,f,g,g2,h,i,j".split(","))
                events.remove(ev);
        if((s = parameters.get("notraces"))!=null )
        {
            int noTraces = Integer.parseInt(s);
            if(noTraces > 3)
            {
                int i = events.indexOf("n");
                if(i >= 0)
                {
                    for(int j = 0;j < noTraces - 3;j++)
                        events.add(i,"m");
                }
            }
        }

        Map<String, Object>ed = loadXML(getConfigPath(String.format("%s.xml","tracingletters")));
        eventsDict.putAll(ed);
        letter = parameters.get("letter");
        if(letter == null)
            letter = "a";
        if(letter.length()  > 1)
            mergeAudioScenesForPrefix("ALT");
        letterPaths = new ArrayList<>();
        xboxes = new ArrayList<>();
        paths = new ArrayList<>();
        doVisual(currentEvent());
    }

    public long switchStatus(String scene)
    {
        if(scene.compareTo("g") < 0)
            return setStatus(STATUS_AWAITING_CLICK);
        if(scene.compareTo("k") < 0)
            return setStatus(STATUS_AWAITING_CLICK2);
        return setStatus(STATUS_WAITING_FOR_TRACE);
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
                    if(!performSel("demo",currentEvent()) )
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public void setSceneintro()
    {
        super.setSceneXX(currentEvent());
        presenter = XPRZ_Presenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
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

    public void prepareForTrace(int i)
    {
        tSoFar = 0;
        currUPath = upaths.get(i);
        currPathLen = paths.get(i).length();
        tLookAhead = allowedDistance / currPathLen;
        traceComplete = false;
        paths.get(i).setStrokeEnd(0);
        paths.get(i).show();
        if (greyPaths != null && greyPaths.size() > i)
            paths.get(i).setZPosition(greyPaths.get(i).zPosition() + 0.01f);
    }

    public void setScenek2()
    {
/*    for(OBPath p : paths)
    {
        p.setPosition(p.propertyValue("origpos"));
        p.setZPosition(3);
        p.hide();
    }
    for(OBPath p : greyPaths)
    {
        p.setStrokeColor(grey);
        p.setZPosition(1);
    }*/
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

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        promptAudioLock = playAudioQueuedScene(currentEvent(), "PROMPT", false);
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

    public void showLetter() throws Exception
    {
        playSfxAudio("letteron",false);
        lockScreen();
        for(OBPath p : greyPaths)
            p.show();
        unlockScreen();
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

    public void endBody()
    {
        try
        {
            if(currentAudio("PROMPT.REMINDER") != null)
                doReminder();
            if(status()  == STATUS_AWAITING_CLICK2 || status()  == STATUS_WAITING_FOR_TRACE)
            {
                deployPulseAnim();
            }
        }
        catch (Exception e)
        {

        }
    }

    public void highlightLetter(boolean high)
    {
        int col = high?hiColour:normalColour;
        lockScreen();
        for(OBPath p : paths)
            p.setStrokeColor(col);
        unlockScreen();
    }

    public void deployPulseAnim() throws Exception
    {
        final long sttime = statusTime;
        waitForSecs(0.1f);
        waitAudio();
        final OBSectionController fthis = this;
        OBUtils.runOnOtherThreadDelayed(3,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(statusTime  == sttime)
                {
                    OBAnim a1 = OBAnim.scaleAnim(1.3f,dot);
                    OBAnim a2 = OBAnim.scaleAnim(1,dot);
                    OBAnimationGroup gp = new OBAnimationGroup();
                    registerAnimationGroup(gp,"anim");
                    gp.chainAnimations(Arrays.asList(Collections.singletonList(a1),(Collections.singletonList(a2))),
                            Arrays.asList(0.6f,0.6f),
                    Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT),-1,fthis);
                    dot.setScale(1);
                }
            }
        });
    }

    public void doReminder() throws Exception
    {
        List reminderAudio = currentAudio("PROMPT.REMINDER");
        if(reminderAudio != null)
        {
            Map<String,Object> asc = (Map<String, Object>) audioScenes.get(currentEvent());
            asc.remove("PROMPT.REMINDER");
            long sttime = statusTime;
            promptAudioLock.lockWhenCondition(PROCESS_DONE);
            promptAudioLock.unlock();
            if(sttime == statusTime)
            {
                waitForSecs(4f);
                if(sttime == statusTime)
                    playAudioQueued(reminderAudio,true);

                //reprompt(sttime audio:reminderAudio after:4);
            }
        }
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

    public void animatePathDraw(OBPath path)
    {
        float len = path.length();
        double duration = len * 2 / theMoveSpeed;
        OBAnim anim = OBAnim.propertyAnim("strokeEnd",1,path);
        OBAnimationGroup.runAnims(Collections.singletonList(anim),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void prepareForStroke()
    {
        lockScreen();
        for(OBPath p : paths)
        {
            p.setStrokeEnd(0.0f);
            p.setPosition((PointF) p.propertyValue("origpos"));
        }
        unlockScreen();
    }

    public void strokePath(OBPath p) throws Exception
    {
        animatePathDraw(p);
        playSfxAudio("ping",true);
    }

    public void demoStroke() throws Exception
    {
        prepareForStroke();
        for(OBPath p : paths)
        {
            strokePath(p);
            waitForSecs(0.4f);
        }
        waitForSecs(0.4f);
        playLetterSound(letter);
        waitAudio();
        waitForSecs(0.4f);
        slideOff();
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

    public void preTrace(int i) throws Exception
    {
        lockScreen();
        greyPaths.get(i).setStrokeColor(orange);
        greyPaths.get(i).setZPosition(5+i);
        paths.get(i).setZPosition(greyPaths.get(i).zPosition() + 0.1f);
        PointF pt = paths.get(i).sAlongPath(0,null);
        pt = convertPointFromControl(pt,paths.get(i));
        dot.setPosition(pt);
        dot.show();
        unlockScreen();
        playSfxAudio("doton",true);
    }

    public void fadeOutDot()
    {
        final OBSectionController fthis = this;
        OBUtils.runOnOtherThreadDelayed(0.2f,new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.opacityAnim(0.0f, dot)), 0.2f, true, OBAnim.ANIM_LINEAR, fthis);
                lockScreen();
                dot.setOpacity(1);
                dot.hide();
                unlockScreen();
            }
        });
;   }

    public void demoSubPathStroke(int i) throws Exception
    {
        waitForSecs(0.4f);
        preTrace(i);
        waitForSecs(0.4f);
        movePointerToPoint(dot.position(),-1,true);
        String demoN = String.format("DEMO%d",i+2);
        playAudioQueuedSceneIndex(currentEvent(),demoN,0,true);
        waitForSecs(0.3f);
        if(currentAudio(demoN).size() > 1)
        {
            playAudioQueuedScene(currentEvent(),demoN,false);
            waitForSecs(0.1f);
        }
        final OBPath p = paths.get(i);
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                p.setStrokeEnd(frac);
                thePointer.setPosition(convertPointFromControl(p.sAlongPath(frac, null), p));
            }
        };
        fadeOutDot();
        float durationMultiplier = 2;
        float duration = p.length()  * 2 * durationMultiplier / theMoveSpeed;
        OBAnimationGroup.runAnims(Collections.singletonList(anim),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("ping",true);
        movePointerForwards(-applyGraphicScale(100),-1);
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

    public void slideOff() throws Exception
    {
        lockScreen();
        for(OBControl p : greyPaths)
            p.hide();
        unlockScreen();
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl obj : paths)
            anims.add(OBAnim.moveAnim(OB_Maths.OffsetPoint(obj.position(),-bounds().width(), 0),obj));
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.8f,true,OBAnim.ANIM_EASE_OUT,this);
    }

    public void nextPath() throws Exception
    {
        if(++currPathIdx >= paths.size() )
        {
            waitForSecs(0.3f);
            playLetterSound(letter);
            waitAudio();
            waitForSecs(0.5f);
            if(currentEvent().equals("i"))
                gotItRightBigTick(true);
            waitForSecs(1f);
            slideOff();
            waitForSecs(0.3f);
            nextScene();
        }
        else
        {
            waitForSecs(0.5f);
            prepareForTrace(currPathIdx);
            PointF pt = paths.get(currPathIdx).sAlongPath(0,null);
            pt = convertPointFromControl(pt,paths.get(currPathIdx));
            dot.setPosition(pt);
            dot.show();
            playSfxAudio("doton",true);
            waitForSecs(0.3f);
            String audiocat = StrAndNo("PROMPT", currPathIdx+1);
            if(currentAudio(audiocat) != null )
                playAudioQueuedScene(audiocat,false);
            switchStatus(currentEvent());
            endBody();
        }
    }

    public void checkTargetg2(OBControl targ,PointF pt)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            killAnimations();
            dot.hide();
            strokePath(paths.get(currPathIdx));
            waitForSecs(0.3f);
            nextPath();
        }
        catch(Exception exception)
        {
        }

    }

    public void nextTracePath() throws Exception
    {
        if(++currPathIdx >= paths.size() )
        {
            playLetterSound(letter);
            waitAudio();
            waitForSecs(0.4f);
            if(eventIndex < events.size()  - 1)
                slideOff();
            else
                gotItRightBigTick(true);
            nextScene();
        }
        else
        {
            prepareForTrace(currPathIdx);
            preTrace(currPathIdx);
            String audiocat = StrAndNo("PROMPT", currPathIdx+1);
            if(currentAudio(audiocat) != null)
                playAudioQueuedScene(audiocat,false);
            setStatus(STATUS_WAITING_FOR_TRACE);
            endBody();
        }
    }

    public void finishPath() throws Exception
    {
        stopAllAudio();
        playSfxAudio("ping",true);
        lockScreen();
        dot.setOpacity(1);
        dot.hide();
        unlockScreen();
        waitForSecs(0.4f);
        nextTracePath();
    }

    public void effectMoveToPoint(PointF pt)
    {
        if(tSoFar >= 1)
            return;
        PointF ppt = convertPointToControl(pt,paths.get(currPathIdx));
        USubPath usp = currUPath.subPaths.get(0);
        OB_MutFloat distance = new OB_MutFloat(0);
        float endT = tSoFar + tLookAhead;
        if(endT > 1)
            endT = 1;
        float tryT = usp.nearestPointOnSubPathForPoint(ppt,distance,allowedDistance,tSoFar,endT);
        if(tryT > tSoFar)
        {
            paths.get(currPathIdx).setStrokeEnd(tryT);
            if(tryT >= 1)
                traceComplete = true;
            tSoFar = tryT;
            if(tSoFar > 0 && !dot.hidden() && dot.opacity() == 1)
                fadeOutDot();
        }
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
        if(status()  == STATUS_TRACING)
        {
            setStatus(STATUS_CHECKING);
            final OBSectionController fthis = this;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        if(traceComplete)
                            finishPath();
                        else if(tSoFar > 1.0 - tLookAhead)
                        {
                            float duration = (currPathLen *(1 - tSoFar)) / theMoveSpeed;
                            if(duration < 0.2)
                                duration = 0.2f;
                            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.propertyAnim("strokeEnd",1.0f,paths.get(currPathIdx))),
                            duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,fthis);
                            traceComplete = true;
                            finishPath();
                        }
                        else
                            setStatus(STATUS_WAITING_FOR_TRACE);
                    }
                    catch(Exception exception)
                    {
                    }
                }
            });
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if(status()  == STATUS_TRACING)
        {
            effectMoveToPoint(pt);
        }
    }


    public void checkTraceStart(PointF pt)
    {
        setStatus(STATUS_TRACING);
        killAnimations();
        effectMoveToPoint(pt);
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            highlightLetter(true);
            playLetterSound(letter);
            waitAudio();
            waitForSecs(0.7f);
            highlightLetter(false);
            waitForSecs(0.3f);
            nextScene();
        }
        catch(Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        OBControl c = finger(-1,2,targets,pt);
        return c;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(target,pt);
                    }
                });
            }
        }
        else if(status()  == STATUS_AWAITING_CLICK2)
        {
            target = (OBControl) findTarget(pt);
            if(target != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTargetg2(target,pt);
                    }
                });
            }
        }
        else if(status()  == STATUS_WAITING_FOR_TRACE)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkTraceStart(pt);
                }
            });
        }
    }

}

