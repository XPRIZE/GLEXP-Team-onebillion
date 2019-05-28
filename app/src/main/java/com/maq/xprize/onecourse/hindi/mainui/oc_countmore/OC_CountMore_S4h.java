package com.maq.xprize.onecourse.hindi.mainui.oc_countmore;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.mainui.OBView;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimBlock;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 15/03/2017.
 */

public class OC_CountMore_S4h extends OC_SectionController
{
    boolean runGame;
    List<OBControl> targets;
    float moveSpeed;
    OBControl clickedBalloon;

    public float graphicScale()
    {
        return this.bounds().width() / 1024;

    }

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        moveSpeed = applyGraphicScale(2);
        targets = new ArrayList<>();
        for (int i = 1; i <= 10; i++)
        {
            OBGroup control = (OBGroup) objectDict.get(String.format("obj_%d", i));
            //OBControl img = control.renderedImageControl();
            //control.setPosition(OBMisc.copyPoint(control.position()));
            OBLabel label = new OBLabel((String) control.attributes().get("num"), OBUtils.standardTypeFace(), 65.0f * control.height() / 131.0f);
            label.setColour(Color.BLACK);
            control.setZPosition(1);
            label.setZPosition(2);
            label.setPosition(OB_Maths.locationForRect(0.52f, 0.45f, control.frame()));

            OBGroup group = new OBGroup(Arrays.asList(control, label));
            group.setZPosition(3);
            attachControl(group);
            targets.add(group);
            group.show();
           // control.hide();
            float randRadian = (float) Math.toRadians(OB_Maths.randomInt(0, 359));
            group.setProperty("speedX", (float)Math.sin(randRadian));
            group.setProperty("speedY", (float)Math.cos(randRadian));

        }
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
        runGame = true;
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo4h();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
    }

    public void doMainXX() throws Exception
    {
        gameLoop();
        OBMisc.doSceneAudio(4, setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl cont = finger(0, 1, targets, pt);
            if (cont != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(cont);
                    }
                });
            }
        }
    }

    public void checkTarget(OBControl cont)
    {
        if(cont == targets.get(0))
        {
            gotItRight();
            clickedBalloon = cont;
        }
        else
        {
            gotItWrong();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        playSFX("wrong");
                    }
                    catch (Exception exception)
                    {
                    }
                }

            }) ;
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void gameLoop()
    {
        OBControl box = objectDict.get("game_box");
        final float left = box.left() + box.borderWidth;
        final float right = box.right() - box.borderWidth;
        final float top = box.top() + box.borderWidth;
        final float bottom = box.bottom() - box.borderWidth;
        final OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(OBControl balloon : targets)
                {
                    float speedX = (float)balloon.settings.get("speedX");
                    float speedY = (float)balloon.settings.get("speedY");
                    PointF loc = OBMisc.copyPoint(balloon.position());
                    loc.x += speedX*moveSpeed;
                    loc.y += speedY*moveSpeed;
                    balloon.setPosition ( loc);
                    if(balloon.left() < left)
                    {
                        balloon.setLeft ( left);
                        balloon.setProperty("speedX",-speedX);

                    }
                    if(balloon.right() > right)
                    {
                        balloon.setRight ( right);
                        balloon.setProperty("speedX",-speedX);

                    }
                    if(balloon.top() < top)
                    {
                        balloon.setTop ( top);
                        balloon.setProperty("speedY",-speedY);

                    }
                    if(balloon.bottom() > bottom)
                    {
                        balloon.setBottom ( bottom);
                        balloon.setProperty("speedY",-speedY);

                    }

                }
                checkBalloonTarget();
                for(OBControl balloon : targets)
                    balloonCollide(balloon);

                checkBalloonTarget();
            }
        };

        final OC_SectionController controller = this;

        OBAnimationGroup ag = new OBAnimationGroup();
        ag.applyAnimations(Arrays.asList(anim), 100, false, OBAnim.ANIM_LINEAR, -1, null, controller);
        registerAnimationGroup(ag, "gameLoop");


    }

    public void balloonCollide(OBControl balloon)
    {
        for(OBControl balloon2 : targets)
        {
            if(balloon2 == balloon) continue;
            PointF n = OB_Maths.DiffPoints(balloon.position(), balloon2.position());
            PointF v1 = new PointF((float)balloon.propertyValue("speedX"), (float)balloon.propertyValue("speedY"));
            PointF v2 = new PointF((float)balloon2.propertyValue("speedX"), (float)balloon2.propertyValue("speedY"));
            float colTr = n.x*(v2.x - v1.x) + n.y*(v2.y - v1.y);
            if(colTr <= 0) //if they are not on collision trajectory, no worries
                continue;
            if(balloon.intersectsWithn(balloon2))
            {
                n = OB_Maths.NormalisedVector(n);
                applySpeed(balloon, n.x, n.y);
                applySpeed(balloon2, -n.x, -n.y);
            }
        }
    }

    public void applySpeed(OBControl balloon,float speedX, float speedY)
    {
        PointF loc = OBMisc.copyPoint(balloon.position());
        balloon.setProperty("speedX",speedX);
        balloon.setProperty("speedY",speedY);
        loc.x += speedX * moveSpeed;
        loc.y += speedY * moveSpeed;
        balloon.setPosition(loc);
    }

    public void moveBalloonBySpeed(OBControl balloon)
    {
        PointF loc = OBMisc.copyPoint(balloon.position());
        loc.x += (float)balloon.settings.get("speedX") * moveSpeed;
        loc.y += (float)balloon.settings.get("speedY") * moveSpeed;
        balloon.setPosition(loc);
    }

    public void checkBalloonTarget()
    {
        if(clickedBalloon != null && status() == STATUS_BUSY)
        {
            playAudio(null);
            clickedBalloon.hide();
            targets.remove(clickedBalloon);
            final int count = targets.size();
            long stime = statusTime;
            if(targets.size()>0)
            {
                stime = setStatus(STATUS_AWAITING_CLICK);
            }
            final long time = stime;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        if(!statusChanged(time))
                            playSFX(String.format("note%d", 10 - targets.size()));
                        if(!statusChanged(time))
                            waitSFX();
                        if(!statusChanged(time))
                            playSFX(getAudioForScene(currentEvent(), "FINAL").get(9 - targets.size()));
                        if(!statusChanged(time))
                            waitSFX();
                        if (count == 0)
                        {
                            waitForSecs(0.3f);
                            displayTick();
                            waitForSecs(0.3f);
                            killAnimations();
                            nextScene();

                        }
                    } catch (Exception exception)
                    {

                    }

                }
            }) ;

            moveSpeed *=1.1;
            clickedBalloon = null;
        }

    }

    public void demo4h() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("game_box").frame()),-15,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.7f,objectDict.get("game_box").frame()),-25,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,targets.get(0).frame()),-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        nextScene();

    }

}
