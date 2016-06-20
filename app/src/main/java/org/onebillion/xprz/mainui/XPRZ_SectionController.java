package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBEmitter;
import org.onebillion.xprz.controls.OBEmitterCell;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBImageManager;
import org.onebillion.xprz.utils.OBRunnableSyncUI;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alan on 23/11/15.
 */
public class XPRZ_SectionController extends OBSectionController {

    public List<OBControl> targets, fingers;
    public Map<String,OBAnimationGroup> animations = new HashMap<>();
    PointF dragOffset;
    long animToken;
    boolean needsRounding;

    public XPRZ_SectionController() {
        super(MainActivity.mainActivity);

    }

    public static String StrAndNo(String s,int n)
    {
        if (n == 1)
            return s;
        return String.format("%s%d",s,n);
    }

    public void setSceneXX(String scene)
    {
        loadEvent(scene);
    }

    public void playAudioQueuedScene(String scene,String event,boolean wait) throws Exception
    {
        if (audioScenes == null)
            return;
        Map<String,List<String>> sc = (Map<String,List<String>>)audioScenes.get(scene);
        if (sc != null)
        {
            List<Object> arr = (List<Object>)(Object)sc.get(event); //yuk!
            if (arr != null)
                playAudioQueued(arr, wait);
        }
    }

    public void playAudioQueuedScene(String audioCategory,boolean wait) throws Exception
    {
        playAudioQueuedScene(currentEvent(),audioCategory,wait);
    }

    public void playAudioScene(String scene,String event,int idx) throws Exception
    {
        Map<String,List<String>> sc = (Map<String,List<String>>)audioScenes.get(scene);
        if (sc != null)
        {
            List<String> arr = sc.get(event); //yuk!
            if (arr != null)
                playAudio(arr.get(idx));
        }
    }

    public void playAudioQueuedSceneIndex(String scene,String event,int idx,boolean wait) throws Exception
    {
        Map<String,List<String>> sc = (Map<String,List<String>>)audioScenes.get(scene);
        List<String> arr = sc.get(event); //yuk!
        playAudioQueued(Arrays.asList((Object)arr.get(idx)), wait);
    }

    OBPath StarWithScale(float scale,boolean shadow)
    {
        Path starpath = new Path();
        boolean outer = true;
        PointF pt = new PointF();
        for (double ang = -(Math.PI);ang < Math.PI;ang += (2.0 * Math.PI / 10.0))
        {
            double cosang = Math.cos(ang);
            double sinang = Math.sin(ang);
            if (outer)
                pt.set((float) cosang, (float) sinang);
            else
                pt.set((float)cosang*0.5f, (float)sinang*0.5f);
            pt.x += 1.0;
            pt.y += 1.0;
            pt.x *= scale;
            pt.y *= scale;
            outer = !outer;
            if (starpath.isEmpty())
                starpath.moveTo(pt.x,pt.y);
            else
                starpath.lineTo(pt.x,pt.y);
        }
        starpath.close();
        Matrix m = new Matrix();
        m.postRotate(28.87f);
        starpath.transform(m);
        OBPath p = new OBPath(starpath);
        p.sizeToBoundingBoxIncludingStroke();
        float r = 1.0f,g = 216.0f/255.0f,b = 0.0f;
        p.setFillColor(Color.argb(255, (int) (r * 255), (int) (g * 255), (int) (b * 255)));
        p.setStrokeColor(Color.argb((int)(0.7*255),(int)(r*0.7*255),(int)(g*0.7*255),(int)(b*0.7*255)));
        p.setZPosition(200);
        return p;
    }

    public void displayAward() throws Exception
    {
        if (_aborting)
            return;
        OBPath star = StarWithScale(bounds().height() * 0.4f,true);
        star.setPosition(bounds().width() / 2, bounds().height() / 2);
        star.setScale(0.04f);
        attachControl(star);
        miscObjects.put("star", star);

        playAudio((String) Config().get(MainActivity.CONFIG_AWARDAUDIO));
        double duration = OBAudioManager.audioManager.duration();
        completeDisplay((duration < 1.0) ? 1.0 : duration);

    }

    public void completeDisplay(double duration) throws Exception
    {
        float scale = MainActivity.mainActivity.applyGraphicScale(1);
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.scaleAnim(1.0f, miscObjects.get("star"))), 0.4, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        OBEmitter emitter = new OBEmitter();
        emitter.setFrame(0,0,bounds().width(),bounds().height());
        OBEmitterCell cell = new OBEmitterCell();
        cell.birthRate = 40;
        cell.velocity = 200 * scale;
        cell.velocityRange = 80 * scale;
        cell.lifeTime = 5;
        cell.emissionRange = (float)(2 * Math.PI);
        cell.scale = 0.7f;
        cell.scaleSpeed = -0.1f;
        //cell.spin = (float)(Math.PI / 12);
        cell.spinRange = (float)(2 * Math.PI / 2);
        cell.posX = bounds().width()/2;
        cell.posY = bounds().height()/2;
        cell.alphaSpeed = -0.2f;
        OBPath star = StarWithScale(bounds().height() * 0.1f,true);
        star.enCache();
        cell.contents = star.cache;
        emitter.cells.add(cell);
        emitter.setZPosition(90);
        attachControl(emitter);
        emitter.run();
        waitForSecs(duration);
        emitter.stop();
        waitForSecs(0.4f);

    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3);
            playAudioQueuedScene("finale", "DEMO", true);
            //displayAward();
            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void checkDragAtPoint(PointF pt)
    {
    }

    public void touchUpAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            target.setZPosition(target.zPosition() - 30);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDragAtPoint(pt);
                }
            });
        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }


    public void checkDragTarget(OBControl targ,PointF pt)
    {
        setStatus(STATUS_DRAGGING);
        target = targ;
        targ.setZPosition(targ.zPosition()+ 30 + currNo);
        targ.animationKey = SystemClock.uptimeMillis();
        dragOffset = OB_Maths.DiffPoints(targ.position(), pt);
    }

    public void doMainXX() throws Exception
    {

    }

    public void endBody()
    {

    }

    public void doBody(String scene) throws Exception
    {
        if (!performSel("doMain",scene))
            doMainXX();
        switchStatus(scene);
        endBody();
    }

    public void setScene(String scene)
    {
        doVisual(scene);
        try
        {
            if (!performSel("demo",scene))
            {
                doBody(scene);
            }
        }
        catch (Exception exception)
        {
        }
    }

    public void loadFingers()
    {
        List<OBControl> fs = new ArrayList<>();
        float graphicScale = graphicScale();
        for (int i = 1;i <= 3;i++)
        {
            String nm = "finger"+((new Integer(i)).toString());
            OBImage oim = OBImageManager.sharedImageManager().imageForName(nm);
            if (oim != null)
            {
                if (graphicScale != 1.0)
                    oim.setScale(graphicScale);
                fs.add(oim);
            }
        }
        fingers = fs;
    }

    public OBControl finger(int startidx,int endidx,List<OBControl> targets,PointF pt)
    {
        return finger(startidx,endidx,targets,pt,false);
    }

    public OBControl finger(int startidx,int endidx,List<OBControl> targets,PointF pt,boolean filterDisabled)
    {
        for (int i = startidx;i <= endidx;i++)
        {
            OBControl finger = null;
            if (i >= 0)
                finger = fingers.get(i);
            for (OBControl c : targets)
            {
                if (filterDisabled && !c.isEnabled())
                    continue;
                PointF lpt = pt;
                if (finger != null)
                {
                    finger.setPosition(lpt);
                    if (finger.intersectsWith(c))
                        return c;
                }
                else
                {
                    lpt = convertPointToControl(pt,c);
                    if (c.alphaAtPoint(lpt.x,lpt.y) > 0)
                        return c;
                }
            }
        }
        return null;
    }

    public void demoButtons() throws Exception
    {
        OBControl trb = MainActivity.mainViewController.topRightButton;
        final PointF butPt = OB_Maths.locationForRect(0.5f, 1.1f,trb.frame());
        final PointF offpt = new PointF(butPt.x,butPt.y);
        offpt.y = glView().getHeight();

        new OBRunnableSyncUI(){public void ex()
        {
            loadPointerStartPoint(offpt,butPt);
        }
        }.run();

        theMoveSpeed = glView().getWidth();
        moveObjects(Collections.singletonList(thePointer),butPt,-1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.01f);
        playAudioScene("intro", "DEMO", 0);
        waitAudio();
        waitForSecs(0.5f);
    }

    public void gotItRight()
    {
    }

    public void gotItRightBigTick(boolean bigTick) throws Exception
    {
        gotItRight();
        stopAllAudio();
        if (bigTick)
            displayTick();
        else
            playSFX("correct");
    }

    public void gotItWrong()
    {
    }

    public void gotItWrongWithSfx()
    {
        gotItWrong();
        stopAllAudio();
        playSFX("wrong");
    }

    public void registerAnimationGroup(OBAnimationGroup gp,String animgpname)
    {
        deregisterAnimationGroupWithName(animgpname);
        synchronized(animations)
        {
            animations.put(animgpname,gp);
        }
    }

    public void deregisterAnimationGroupWithName(String pattern)
    {
        synchronized(animations)
        {
            pattern = String.format("^%s$",pattern);
            List<String> arr = new ArrayList<>();
            Pattern p = Pattern.compile(pattern);
            if (p != null)
            {
                for (String k : animations.keySet())
                {
                    Matcher matcher = p.matcher(k);
                    matcher.find();
                    if (matcher.matches())
                        arr.add(k);
                }
            }
            for (String animgpname : arr)
            {
                OBAnimationGroup ogp = animations.get(animgpname);
                if (ogp != null)
                    ogp.flags = ogp.flags | OBAnimationGroup.ANIM_CANCEL;
                animations.remove(animgpname);
            }
        }
    }

    public void killAnimations()
    {
        deregisterAnimationGroupWithName(".*");
    }

    public PointF pointFromObjectsDict(Map<String,Map<String,Object>>objectsdict,String objectName)
    {
        Map<String,Object> target = objectsdict.get(objectName);
        Map<String,Object> attrs = (Map<String, Object>) target.get("attrs");
        String parentName = (String) attrs.get("parent");
        PointF relpt = OBUtils.pointFromString((String) attrs.get("pos"));
        RectF r = new RectF(bounds());
        if (parentName != null)
            r = objectDict.get(parentName).frame();
        PointF pt = OB_Maths.locationForRect(relpt, r);
        if (attrs.get("anchor") != null)
        {
            PointF anc = OBUtils.pointFromString((String) attrs.get("anchor"));
            OBControl im = objectDict.get(objectName);
            RectF f = OB_Maths.frameForPosition(im.frame(), pt);
            PointF destPoint = OB_Maths.locationForRect(anc, f);
            PointF vec = OB_Maths.DiffPoints(pt,destPoint);
            PointF newPoint = OB_Maths.AddPoints(pt, vec);
            pt = newPoint;
        }
        return pt;
    }

}