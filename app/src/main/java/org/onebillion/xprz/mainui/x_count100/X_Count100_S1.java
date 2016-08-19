package org.onebillion.xprz.mainui.x_count100;

import android.animation.ArgbEvaluator;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBMisc;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.ULine;
import org.onebillion.xprz.utils.UPath;
import org.onebillion.xprz.utils.USubPath;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 16/06/16.
 */
public class X_Count100_S1  extends XPRZ_SectionController
{

    int curTarget;
    int maxTarget;
    OBControl line;
    OBLabel counter;

    @Override
    public void prepare()
    {
        lockScreen();
        super.prepare();
        loadFingers();
        loadEvent("master1");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));

        line = objectDict.get("line");
        Typeface tf = OBUtils.standardTypeFace();
        float textSize = MainActivity.mainActivity.applyGraphicScale(80);
        counter = new OBLabel("000",tf,textSize);
        counter.setColour(Color.BLACK);
        counter.setPosition(objectDict.get("numbox").position());
        counter.setZPosition((float)1.5);
        counter.setString("0");
        attachControl(counter);
        setSceneXX(currentEvent());
        unlockScreen();
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                demo1a();

            }
        });
    }

    @Override
    public void setSceneXX(String scene)
    {
        deleteControls("sea");
        super.setSceneXX(scene);

        OBControl sea = objectDict.get("sea");
        if(sea != null)
            sea.setTop(this.bounds().height());

        curTarget = Integer.valueOf(eventAttributes.get("start"));
        maxTarget = Integer.valueOf(eventAttributes.get("end"));
        if(eventAttributes.get("redraw") != null)
        {
            deleteControls("obj.*");

            OBGroup loadImage = (OBGroup) objectDict.get("image");
            loadImage.show();

            for(OBControl con : loadImage.filterMembers("frame.*"))
                con.hide();


            if(loadImage.objectDict.get("frame1") != null)
                loadImage.objectDict.get("frame1").show();

            String locs[] =  ((String)eventAttributes.get("loc")).split(",");
            float x = Float.valueOf(locs[0]);
            float y = Float.valueOf(locs[1]);
            float d1 =(1-2*x)/9.0f;
            float d2=Float.valueOf(locs[2]);
            int redraw = Integer.valueOf(eventAttributes.get("redraw"));
            OBControl workrect = objectDict.get("workrect");

            for(int i=1; i<=redraw; i++)
            {
                OBGroup cont = (OBGroup) loadImage.copy();

                float x1 = x+((i-1)%10)*d1;
                float y1 = (float)(y+(Math.ceil(i/10f)-1)*d2);
                cont.setPosition(OB_Maths.locationForRect(x1,y1, workrect.frame));
                objectDict.put("obj"+i,cont);
                attachControl(cont);
                cont.setZPosition(1f);
                cont.lowlight();
                if(i >= curTarget)
                    cont.hide();
            }
            loadImage.hide();
            counter.setString(String.valueOf(curTarget-1));

        }

    }

    @Override
    public void doMainXX() throws Exception
    {
        startScene(true);
    }

    @Override
    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            goToCard(X_Count100_S1i.class,"event1");
        }
        catch (Exception e)
        {

        }
    }

    void startScene(boolean demo) throws Exception
    {
        if(demo)
        {
            playAudioScene(currentEvent(),"DEMO", 0);
            waitAudio();
            setupLine();
        }

        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }

    void setupLine() throws Exception
    {
        lockScreen();
        OBControl targ = objectDict.get("obj"+curTarget);
        line.setPosition(targ.position().x,0);
        line.setTop(targ.bottom());
        line.show();
        unlockScreen();
        playSfxAudio("snap",true);
    }


    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK) {

            final OBControl targ = finger(-1,2,Collections.singletonList(line),pt);

            if(targ != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        moveLine();
                    }
                });
            }
        }
    }


    void moveLine() throws Exception
    {
        setStatus(STATUS_BUSY);
        OBControl tar = objectDict.get("obj"+curTarget);
        tar.show();
        counter.setString(String.valueOf(curTarget));
        curTarget++;
        if(curTarget > maxTarget)
        {
            line.hide();
            playSfxAudio("put", true);
            playAudioScene(currentEvent(),"FINAL",((curTarget-2)%10));
            waitAudio();
            playAudioQueuedScene(currentEvent(),"FINAL2",true);
            if(eventAttributes.get("audio") != null && eventAttributes.get("audio").equalsIgnoreCase("true"))
            {
                if(curTarget == 51)
                {
                    playSfxAudio("flash2", false);
                }
                else
                {
                    playSfxAudio("flash1", false);
                }
            }

            performSel("demoFin",currentEvent());
            waitForSecs(0.3);
            nextScene();
        }
        else
        {
            playSfxAudio("put",false);
            OBControl targNext = objectDict.get("obj"+curTarget);
            PointF loc = new PointF();
            loc.set(targNext.position().x,line.position().y);
            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.moveAnim(loc,line)),0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            playAudioScene(currentEvent(),"FINAL",((curTarget-2)%10));
            setStatus(STATUS_AWAITING_CLICK);
        }

    }



    void demo1a() throws Exception
    {
        demoButtons();
        movePointerToPoint(OB_Maths.locationForRect(0.6f,0.6f,new RectF(this.bounds())),-25,0.5f,true);
        playAudioScene(currentEvent(),"DEMO",0);
        waitAudio();
        waitForSecs(0.2f);
        setupLine();
        movePointerToPoint(OB_Maths.locationForRect(0.5f,4,line.frame()),-25,0.5f,true);
        playAudioScene(currentEvent(),"DEMO",1);
        waitAudio();
        thePointer.hide();
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }


    public void flashObjects()
    {
        List<OBAnim> anims1 = new ArrayList<OBAnim>();
        List<OBAnim> anims2 = new ArrayList<OBAnim>();

        OBPath numbox= (OBPath) objectDict.get("numbox");

        anims1.add(OBAnim.colourAnim("fillColor",Color.argb(255,175,175,175),numbox));
        anims2.add(OBAnim.colourAnim("fillColor",Color.argb(255,255,255,255),numbox));

        int col1 = Color.argb(255,127,127,127);
        int col2 = Color.argb(255,255,255,255);
        for(int i=1; i<=maxTarget; i++)
        {
            OBGroup group = (OBGroup) objectDict.get(String.format("obj%d", i));
            anims1.add(OBAnim.colourAnim("highlightColour",col1,group));
            anims2.add(OBAnim.colourAnim("highlightColour",col2,group));
        }

        OBAnimationGroup.chainAnimations(Arrays.asList(anims1,anims2,anims1,anims2),Arrays.asList(0.2f,0.2f,0.2f,0.2f),true,
                Arrays.asList(OBAnim.ANIM_LINEAR,OBAnim.ANIM_LINEAR,OBAnim.ANIM_LINEAR,OBAnim.ANIM_LINEAR),1,this);


    }

    public void demoFin1c() throws Exception
    {
        flashObjects();
        waitSFX();
        playSfxAudio("bee",false);
        OBGroup bee = (OBGroup)objectDict.get("bee");
        bee.setZPosition(2);
        OBControl workrect = objectDict.get("workrect");
        OBPath path1 = (OBPath)objectDict.get("path1");

        OBPath path2 = (OBPath)objectDict.get("path2");

        path1.sizeToBox(workrect.bounds());
        path2.sizeToBox(workrect.bounds());

        XPRZ_Generic.setFirstPoint(path1, new PointF(-1*bee.width(), path1.firstPoint().y), this);
        setFirstLastPoints(path2,path1.lastPoint(),new PointF(path2.lastPoint().x, -1*bee.width()));

        bee.setPosition(path1.firstPoint());

        bee.show();

        OBAnim beeAnim = OBAnim.sequenceAnim(bee,Arrays.asList("frame2","frame1"),0.08f,true);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(bee,path1.path(),true,0f),beeAnim),1,true,OBAnim.ANIM_EASE_OUT,this);
        OBAnimationGroup.runAnims(Arrays.asList(beeAnim),1,true,OBAnim.ANIM_LINEAR,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(bee,path2.path(),true,0f),beeAnim),3,true,OBAnim.ANIM_EASE_OUT,this);
        bee.hide();
        List<String> frames = Arrays.asList("frame2","frame3","frame2","frame1","frame4","frame5","frame4","frame1");
        List<OBAnim> anims = new ArrayList<OBAnim>();
        for(OBControl con : this.filterControls("obj.*"))
            anims.add(OBAnim.sequenceAnim((OBGroup) con,frames,0.07f,true));

        playSfxAudio("flowers",false);
        OBAnimationGroup.runAnims(anims,2f,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(1f);

    }

    public void demoFin1d() throws Exception
    {
        flashObjects();
        waitSFX();
        playSfxAudio("apples1",false);
        for(int i=1; i<11; i++)
        {
            OBControl apple = objectDict.get(String.format("obj%d", i));
            OBAnim rotateAnim = OBAnim.rotationAnim((float) Math.toRadians(180),apple);
            OBAnim rotateAnim2 = OBAnim.rotationAnim((float) Math.toRadians(360),apple);

            PointF startPoint = apple.position();
            PointF endPoint = new PointF();
            endPoint.x = startPoint.x;
            endPoint.y = (float) (startPoint.y - 1.5*apple.height());

            OBAnim moveAnim = OBAnim.moveAnim(endPoint,apple);
            OBAnim moveAnim2 =  OBAnim.moveAnim(startPoint,apple);

            OBAnimationGroup.chainAnimations(Arrays.asList(Arrays.asList(moveAnim,rotateAnim), Arrays.asList(moveAnim2,rotateAnim2)),Arrays.asList(0.4f,0.4f),false,Arrays.asList(OBAnim.ANIM_EASE_IN,OBAnim.ANIM_EASE_OUT),1,this);
            waitForSecs(0.05f);
        }
        waitForSecs(1f);

        List<String> frames = Arrays.asList("frame3","frame2","frame1");
        List<OBAnim> anims = new ArrayList<OBAnim>();
        for(OBControl con : this.filterControls("obj.*"))
            anims.add(OBAnim.sequenceAnim((OBGroup) con,frames,0.07f,true));

        playSfxAudio("apples2",false);
        OBAnimationGroup.runAnims(anims,2f,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(1f);
    }

    public void  demoFin1e() throws Exception
    {
        flashObjects();
        waitSFX();
        playSfxAudio("waves",false);

        OBGroup sea = (OBGroup)objectDict.get("sea");
        sea.setZPosition(-0.1f);
        sea.setTop(bounds().height());
        sea.setHeight(bounds().height()- objectDict.get("numbox").bottom());
        sea.setWidth(bounds().width() + applyGraphicScale(10));
        sea.show();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("top",objectDict.get("numbox").bottom(),sea),
                OBAnim.sequenceAnim(sea,Arrays.asList("frame2","frame1"),0.3f,true)),
                2f,true,OBAnim.ANIM_EASE_OUT,this);

        OBControl workrect = objectDict.get("workrect");
        OBPath path1 = (OBPath)objectDict.get("path1");
        path1.sizeToBox(workrect.bounds());
        OBPath path2 = (OBPath)objectDict.get("path2");
        path2.sizeToBox(workrect.bounds());

        OBGroup fish1 = (OBGroup) objectDict.get("obj3");
        OBGroup fish2 = (OBGroup) objectDict.get("obj7");
        fish1.setZPosition(2f);
        fish2.setZPosition(2.1f);

        setFirstLastPoints(path1, fish1.position(),fish2.position());
        setFirstLastPoints(path2, fish2.position(),fish1.position());


        fish2.flipHoriz();
        playSfxAudio("fish1",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(fish1,path1.path(),false,0),
                OBAnim.sequenceAnim(fish1,Arrays.asList("frame2","frame1"),0.1f,true),
                OBAnim.pathMoveAnim(fish2,path2.path(),false,0),
                OBAnim.sequenceAnim(fish2,Arrays.asList("frame2","frame1"),0.1f,true)),3f,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

        waitForSecs(2.5f);
        playSfxAudio("fish2",false);
        waitForSecs(1f);

        List<OBControl> fishArray = filterControls("obj.*");
        int count = fishArray.size();
        if (count > 1)
        {
            for (int i = count - 1; i > 0; --i)
            {
                Collections.swap(fishArray,i,OB_Maths.randomInt(0,i));
            }
        }

        lockScreen();
        for(int i=0; i<11; i++)
        {
            OBControl fish = fishArray.get(i);
            if(fish != fish2)
            {
                fish.flipHoriz();
            }
        }

        unlockScreen();
        playSfxAudio("fish3",true);
        waitForSecs(1f);

        List<String> frames = Arrays.asList("frame2","frame1");
        List<OBAnim> anims = new ArrayList<OBAnim>();
        for(OBControl con : this.filterControls("obj.*"))
            anims.add(OBAnim.sequenceAnim((OBGroup) con,frames,0.1f,true));

        anims.add(OBAnim.sequenceAnim((OBGroup) sea,frames,0.1f,true));


        playSfxAudio("waves",false);
        OBAnimationGroup.runAnims(anims,3f,true,OBAnim.ANIM_LINEAR,this);
        playSFX(null);

    }


    public void demoFin1f() throws Exception
    {
        flashObjects();
        waitSFX();
        playSfxAudio("ladybug",false);
        OBControl workrect = objectDict.get("workrect");
        OBPath path1 = (OBPath)objectDict.get("path1");
        path1.sizeToBox(workrect.bounds());
        OBGroup ladybug = (OBGroup)objectDict.get("obj7");
        setFirstLastPoints(path1,ladybug.position(),ladybug.position());

        ladybug.setZPosition(2);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.pathMoveAnim(ladybug,path1.path(),true,0),
                OBAnim.sequenceAnim(ladybug,Arrays.asList("frame2", "frame3", "frame2", "frame1"),0.1f,true)),3.5f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);



        List<String> frames = Arrays.asList("frame2", "frame3", "frame2", "frame1");
        List<OBAnim> anims = new ArrayList<OBAnim>();
        for(OBControl con : this.filterControls("obj.*"))
            anims.add(OBAnim.sequenceAnim((OBGroup) con,frames,0.1f,true));


        playSfxAudio("ladybug",false);
        OBAnimationGroup.runAnims(anims,3f,true,OBAnim.ANIM_LINEAR,this);
        playSFX(null);
    }

    public void demoFin1h() throws Exception
    {
        flashObjects();
        waitSFX();
        OBControl workrect = objectDict.get("workrect");
        OBPath path1 = (OBPath)objectDict.get("path1");
        path1.sizeToBox(workrect.bounds());
        OBPath path2 = (OBPath)objectDict.get("path2");
        path2.sizeToBox(workrect.bounds());
        OBPath path3 = (OBPath)objectDict.get("path3");
        path3.sizeToBox(workrect.bounds());
        OBPath path4 = (OBPath)objectDict.get("path4");
        path4.sizeToBox(workrect.bounds());

        OBControl ball = objectDict.get("obj21");

        PointF loc1 = new PointF(path1.lastPoint().x, bounds().height() - 0.5f*ball.height());
        PointF loc2 = new PointF(bounds().width() - 0.5f*ball.width(), path2.lastPoint().y);
        PointF loc3 = new PointF(path3.lastPoint().x, 0.5f*ball.height());
        setFirstLastPoints(path1,ball.position(),loc1);
        setFirstLastPoints(path2,loc1,loc2);
        setFirstLastPoints(path3,loc2,loc3);
        setFirstLastPoints(path4,loc3,ball.position());

        ball.setZPosition(2);
        boolean playSound = false;
        for(OBPath path : Arrays.asList(path1,path2,path3,path4))
        {
            if(playSound)
                playSfxAudio("ball1",false);

            OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.pathMoveAnim(ball,path.path(),false,0)),1f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            playSound=true;
        }

        List<OBAnim> anims = new ArrayList<OBAnim>();
        for(int i = 0; i<5; i++)
        {
            for(int j = 0; j<5; j++)
            {
                if(i%2 == 0)
                {
                    anims.add(OBAnim.rotationAnim((float)Math.toRadians(360),objectDict.get(String.format("obj%d",(i*10)+(j*2)+1))));
                }else{
                    anims.add(OBAnim.rotationAnim((float)Math.toRadians(360),objectDict.get(String.format("obj%d",(i*10)+(j*2)))));
                }

            }

        }
        playSfxAudio("ball2",false);
        OBAnimationGroup.runAnims(anims,3f,true,OBAnim.ANIM_LINEAR,this);
        playSFX(null);

    }



    public void flashLine(long prevStatusTime)
    {
        try {
            boolean flash = true;
            while(!statusChanged(prevStatusTime)){
                if(flash){
                    line.hide();
                }else{
                    line.show();
                }
                flash = !flash;
                waitForSecs(0.5f);
            }
        }
        catch (Exception exception) {

        }
    }


    public void setFirstLastPoints(OBPath path, PointF firstPoint, PointF lastPoint)
    {
        String name = (String) path.attributes().get("id");
        if (name == null) name = (String) path.settings.get("name");
        if (name == null) return;
        UPath deconPath = deconstructedPath(currentEvent(), name);

        USubPath subPath = deconPath.subPaths.get(0);
        ULine line = subPath.elements.get(0);
        line.pt0 = firstPoint;

        USubPath subPath2 = deconPath.subPaths.get(deconPath.subPaths.size() - 1);
        ULine line2 = subPath2.elements.get(subPath2.elements.size() - 1);
        line2.pt1 = lastPoint;
        path.setPath(deconPath.bezierPath());
    }

}
