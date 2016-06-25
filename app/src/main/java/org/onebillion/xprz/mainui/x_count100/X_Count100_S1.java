package org.onebillion.xprz.mainui.x_count100;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OB_Maths;
import org.onebillion.xprz.utils.OBUtils;

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

    public void prepare()
    {
        lockScreen();
        super.prepare();
        loadFingers();
        loadEvent("master1");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);

        line = objectDict.get("line");
        Typeface tf = OBUtils.standardTypeFace();
        float textSize = MainActivity.mainActivity.applyGraphicScale(80);
        counter = new OBLabel("000",tf,textSize);
        counter.setColour(Color.RED);
        counter.setPosition(objectDict.get("numbox").position());
        counter.setZPosition((float)1.5);

        counter.setString("0");
        attachControl(counter);
        setSceneXX(currentEvent());
        unlockScreen();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                //demo1a();
                startScene(true);

            }
        });
    }

    public void setSceneXX(String scene)
    {
        deleteControls("sea");
        super.setSceneXX(scene);

        curTarget = Integer.valueOf(eventAttributes.get("start"));
        maxTarget = Integer.valueOf(eventAttributes.get("end"));
        if(eventAttributes.get("redraw") != null)
        {
            deleteControls("obj.*");
            //colourObjectFromAttributes:self.objectDict[@"image"]];

            OBGroup loadImage = (OBGroup) objectDict.get("image");
            loadImage.show();
            loadImage.texturise(true,this);
            /*NSMutableArray *arr = [NSMutableArray array];
            NSArray *frames = [loadImage filterMembers:@"frame.*"];
            OBGroup *loadImage2;
            if([frames count] >0)
            {
                for(OBControl *con in frames)
                {
                    [con hide];
                }

                for(OBControl *con in frames)
                {
                    [con show];
                    OBControl *frame = [loadImage renderedImageControl];
                    frame.frame = loadImage.frame;
                    [frame setProperty:@"name" value:con.settings[@"name"]];
                    [frame setParent:self.objectDict[@"workrect"]];
                    [arr addObject:frame];
                    [con hide];
                }

                loadImage2 = [[OBGroup alloc] initWithMembers:arr];
                for(OBControl *con in loadImage2.members)
                {
                    [loadImage2.objectDict setObject:con forKey:con.settings[@"name"]];
                    [con hide];
                }

                [loadImage2.objectDict[@"frame1"] show];

            }
            else
            {
                loadImage2 = [[OBGroup alloc] initWithMembers:@[[loadImage renderedImageControl]]];
                loadImage2.frame = loadImage.frame;
            }
*/

            loadImage.hide();
            String locs[] =  ((String)eventAttributes.get("loc")).split(",");
            float x = Float.valueOf(locs[0]);
            float y = Float.valueOf(locs[1]);
            float d1 =(1-2*x)/9.0f;
            float d2=Float.valueOf(locs[2]);
            int redraw = Integer.valueOf(eventAttributes.get("redraw"));
            OBControl workrect = objectDict.get("workrect");
            workrect.hide();
            for(int i=1; i<=redraw; i++)
            {
                OBGroup cont = (OBGroup) loadImage.copy();
                cont.texturise(true,this);
                float x1 = x+((i-1)%10)*d1;
                float y1 = (float)(y+(Math.ceil(i/10f)-1)*d2);
                cont.setPosition(OB_Maths.locationForRect(x1,y1, workrect.frame));
                objectDict.put("obj"+i,cont);
                attachControl(cont);

                if(i >= curTarget)
                    cont.hide();
            }

            counter.setString(String.valueOf(curTarget-1));

        }

    }

    public void doMainXX() throws Exception
    {
        startScene(true);
    }


    void startScene(boolean demo) throws Exception
    {
        if(demo)
        {
            playAudioScene(currentEvent(),"DEMO", 0);
            waitAudio();
            setupLine();
        }

        setReplayAudioScene(currentEvent(),"REPEAT");
        final long time = setStatus(STATUS_AWAITING_CLICK);
        playAudioQueuedScene(currentEvent(),"PROMPT",true);

        OBUtils.runOnOtherThreadDelayed(3, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(!statusChanged(time))
                {
                    playAudioQueuedScene(currentEvent(),"REMIND",true);
                }
            }
        });

    }

    void setupLine() throws Exception
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                OBControl targ = objectDict.get("obj"+curTarget);
                line.setPosition(targ.position().x,0);
                line.setTop(targ.bottom());
                line.show();
            }
        });

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
            shake();
            line.hide();
            playSfxAudio("put", true);
            playAudioScene(currentEvent(),"FINAL",((curTarget-2)%10));
            waitAudio();
            playAudioQueuedScene(currentEvent(),"FINAL2",true);
            if(eventAttributes.get("audio").equalsIgnoreCase("true"))
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




    void shake()
    {
        List<String> frames = Arrays.asList("frame2","frame3","frame2","frame1","frame4","frame5","frame4","frame1");
        List<OBAnim> anims = new ArrayList<OBAnim>();
        for(OBControl con : this.filterControls("obj.*"))
            anims.add(OBAnim.sequenceAnim((OBGroup) con,frames,0.07f,true));

        OBAnimationGroup.runAnims(anims,10,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
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
    }
}
