package org.onebillion.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.controls.OBStroke;
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
 * Created by alan on 24/08/2017.
 */

public class OC_LTrace extends OC_Wordcontroller
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
    OBPresenter presenter;
    boolean dotFadeBegun;

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
            if (xbox != null)
            {
                xboxes.add(xbox);
                xbox.hide();
            }
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
                fp.offset(0,-f.top);
                //fp.top -= f.top;
                if(fp.top < miny)
                    miny = fp.top;
                float thismaxy = fp.bottom;
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

    public RectF pathsBounds()
    {
        RectF r = new RectF();
        for(OBPath p : paths)
            r.union(p.frame());
        return r;
    }

    float xIntersectionWithLineAtY(float px,float py,float dx,float dy,float y)
    {
        if(dy == 0)
            return 10000;
        float r = dx / dy;
        float xamt =(y - py) * r;
        return px + xamt;
    }

    float yIntersectionWithLineAtX(float px,float py,float dx,float dy,float x)
    {
        if(dx == 0)
            return 10000;
        float r = dy / dx;
        float yamt =(x - px) * r;
        return py + yamt;
    }

    public PointF pointToMovePointerForwards(float distance)
    {
        float ang = thePointer.rotation;
        float x = (float) Math.sin(ang);
        float y = (float) -Math.cos(ang);
        float len = (float) Math.sqrt(x * x + y * y);
        float ratio = distance / len;
        x *= ratio;
        y *= ratio;
        PointF pos = thePointer.position();
        return new PointF(pos.x + x,pos.y + y);
    }

    public PointF outerPointForPoint(PointF pt,float dist)
    {
        float ins = applyGraphicScale(30);
        RectF b = new RectF(pathsBounds());
        b.inset(-ins,-ins);
        PointF mpt = pointToMovePointerForwards(-dist);
        if(!b.contains(mpt.x, mpt.y))
            return mpt;
        float bottomy = b.bottom;
        float rightx = b.right;
        float bottomx = xIntersectionWithLineAtY(pt.x, pt.y, mpt.x - pt.x, mpt.y - pt.y, bottomy);
        float righty = yIntersectionWithLineAtX(pt.x, pt.y, mpt.x - pt.x, mpt.y - pt.y, rightx);
        PointF newpt = new PointF();
        if(bottomx > rightx)
        {
            newpt.x = (rightx);
            newpt.y = (righty);
        }
        else
        {
            newpt.x = (bottomx);
            newpt.y = (bottomy);
        }
        return newpt;
    }
    public String tracingFileName()
    {
        return "tracingletters";
    }

    public void frigEvents()
    {
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
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = new ArrayList<>(Arrays.asList(eva));
        frigEvents();

        Map<String, Object> ed = loadXML(getConfigPath(String.format("%s.xml",tracingFileName())));
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

    public void doMainXX() throws Exception
    {
        setReplayAudioScene(currentEvent(), "PROMPT.REPEAT");
        promptAudioLock = playAudioQueuedScene(currentEvent(), "PROMPT", false);
    }

    public void showLetter() throws Exception
    {
        playSfxAudio("letteron",false);
        lockScreen();
        for(OBPath p : greyPaths)
            p.show();
        unlockScreen();
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

    public void animatePathDraw(OBPath path)
    {
        float len = path.length();
        double duration = len * 2 / theMoveSpeed;
        OBAnim anim = OBAnim.propertyAnim("strokeEnd",1,path);
        path.show();
        OBAnimationGroup.runAnims(Collections.singletonList(anim),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void prepareForStroke()
    {
        lockScreen();
        for(OBPath p : paths)
        {
            p.setStrokeEnd(0.0f);
            p.setPosition((PointF) p.propertyValue("origpos"));
            p.hide();
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
        playFeedback(letter);
        waitAudio();
        waitForSecs(0.4f);
        slideOff();
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
        dot.setOpacity(1.0f);
        dotFadeBegun = false;
        unlockScreen();
        playSfxAudio("doton",true);
    }

    public void fadeOutDot(final int pathidx)
    {
        if (dotFadeBegun)
            return;
        dotFadeBegun = true;
        final OBSectionController fthis = this;
        OBUtils.runOnOtherThreadDelayed(0.2f,new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                if (pathidx == currPathIdx)
                {
                    OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.opacityAnim(0.0f, dot)), 0.2f, true, OBAnim.ANIM_LINEAR, fthis);
                    if (pathidx == currPathIdx)
                    {
                        lockScreen();
                        dot.setOpacity(1);
                        dot.hide();
                        unlockScreen();
                    }
                }
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
        if (currentAudio(demoN) != null)
        {
            playAudioQueuedSceneIndex(currentEvent(),demoN,0,true);
            waitForSecs(0.3f);
            if(currentAudio(demoN).size() > 1)
            {
                playAudioQueuedSceneIndex(currentEvent(),demoN,1,false);
                waitForSecs(0.1f);
            }
        }
        final OBPath p = paths.get(i);
        p.show();
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                p.setStrokeEnd(frac);
                thePointer.setPosition(convertPointFromControl(p.sAlongPath(frac, null), p));
            }
        };
        fadeOutDot(currPathIdx);
        float durationMultiplier = 2;
        float duration = p.length()  * 2 * durationMultiplier / theMoveSpeed;
        OBAnimationGroup.runAnims(Collections.singletonList(anim),duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("ping",true);
        waitAudio();
        //movePointerForwards(-applyGraphicScale(100),-1);
        PointF newpt = outerPointForPoint(thePointer.position(),applyGraphicScale(100f));
        movePointerToPoint(newpt,-1,true);

    }

    public void prepareForTrace(int i)
    {
        tSoFar = 0;
        currUPath = upaths.get(i);
        currPathLen = paths.get(i).length();
        tLookAhead = allowedDistance / currPathLen;
        traceComplete = false;
        paths.get(i).setStrokeEnd(0);
        //paths.get(i).show();
        if (greyPaths != null && greyPaths.size() > i)
            paths.get(i).setZPosition(greyPaths.get(i).zPosition() + 0.01f);
    }

    public void nextPath() throws Exception
    {
        if(++currPathIdx >= paths.size() )
        {
            waitForSecs(0.3f);
            playFeedback(letter);
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
    public void nextTracePath() throws Exception
    {
        lockScreen();
        greyPaths.get(currPathIdx).hide();
        unlockScreen();
        if(++currPathIdx >= paths.size() )
        {
            playFeedback(letter);
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
//        PointF ppt = convertPointToControl(pt,paths.get(currPathIdx));
        USubPath usp = currUPath.subPaths.get(0);
        OB_MutFloat distance = new OB_MutFloat(0);
        float endT = tSoFar + tLookAhead;
        if(endT > 1)
            endT = 1;
        float tryT = usp.nearestPointOnSubPathForPoint(pt,distance,allowedDistance,tSoFar,endT);
        if (paths.get(currPathIdx).hidden())
            paths.get(currPathIdx).show();
        if(tryT > tSoFar)
        {
            lockScreen();
            paths.get(currPathIdx).setStrokeEnd(tryT);
            unlockScreen();
            if(tryT >= 1)
                traceComplete = true;
            tSoFar = tryT;
            if(tSoFar > 0 && !dot.hidden() && dot.opacity() == 1 && !dotFadeBegun)
                fadeOutDot(currPathIdx);
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
                        {
                            finishPath();
                        }
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

    public void playFeedback(String l)
    {
        playLetterSound(l);
    }

    public void checkTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            highlightLetter(true);
            playFeedback(letter);
            waitForSecs(0.1);
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

    public void checkTarget2(OBControl targ,PointF pt)
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
                        checkTarget2(target,pt);
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
