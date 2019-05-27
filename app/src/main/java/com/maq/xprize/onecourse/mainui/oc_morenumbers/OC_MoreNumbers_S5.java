package com.maq.xprize.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/04/2017.
 */

public class OC_MoreNumbers_S5 extends OC_SectionController
{
    List<OBControl> eventTargets, objsOut, backgroundObjs, clouds;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master5");
        eventTargets = new ArrayList<>();
        objsOut = new ArrayList<>();
        backgroundObjs = new ArrayList<>();
        clouds = new ArrayList<>();
        for (OBControl cont : filterControls("cloud_.*"))
        {
            clouds.add(cont);
        }

        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        OBMisc.checkAndUpdateFinale(this);
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5a();
            }
        });

    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        eventTargets.clear();
        List<Integer> indexes = OBUtils.RandomIndexesTo(3);
        int index = 0;
        for(OBControl control : filterControls("obj_.*"))
        {
            int standardColour = OBUtils.colorFromRGBString((String)control.attributes().get("colour_background"));
            control.setProperty("startcol",standardColour);
            colourObj((OBGroup)control, false);
            OC_MoreNumbers_Additions.insertIntoGroup((OBGroup)control,OBUtils.getIntValue((String)control.attributes().get("num")),
                    120*control.height()/300.0f, Color.BLACK, OB_Maths.locationForRect(0.5f,0.3f,control.frame()), this);
            control.setZPosition ( 2);
            PointF loc = OBMisc.copyPoint(control.position());
            loc.y = loc.y + bounds().height() * 0.5f * (indexes.get(index++) +2);
            control.setPosition ( loc);
            control.show();
            eventTargets.add(control);
        }
    }

    public void doMainXX() throws Exception
    {
        animateBaloons(false);
        startScene();
    }


    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            OBControl balloon = finger(0,1,eventTargets,pt);
            if(balloon != null)
            {
                setStatus(STATUS_BUSY);
                final OBGroup bal = (OBGroup)balloon;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        colourObj(bal, true);
                        if(bal.attributes().get("correct") != null &&
                                bal.attributes().get("correct").equals("true"))
                        {
                            gotItRightBigTick(true);
                            waitForSecs(0.2f);
                            colourObj(bal, false);

                            performSel("demoFin",currentEvent());
                            nextScene();

                        }
                        else
                        {
                            gotItWrongWithSfx();
                            waitSFX();
                            colourObj(bal, false);
                            playAudioQueuedScene("INCORRECT",0.3f,false);
                            setStatus(STATUS_AWAITING_CLICK);

                        }
                    }
                });
            }
        }
    }


    public void startScene() throws Exception
    {
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);

    }
    public void colourObj(OBGroup obj, boolean hilighed)
    {
        int colour = hilighed ? OBUtils.highlightedColour((int)obj.propertyValue("startcol")) : (int)obj.propertyValue("startcol");
        lockScreen();
        obj.substituteFillForAllMembers("colour_.*",colour);
        unlockScreen();
    }

    public void animateBaloons(boolean finish) throws Exception
    {
        List<OBAnim> anims1 = new ArrayList<>();
        List<OBAnim> anims2 = new ArrayList<>();
        List<Integer> indexes = OBUtils.RandomIndexesTo(3);
        int index = 0;
        for(OBControl cont : objsOut)
        {
            PointF loc = OBMisc.copyPoint(cont.position());
            loc.y = (-0.2f - 0.3f*(indexes.get(index++) +1)) * bounds().height();
            anims1.add(OBAnim.moveAnim(loc,cont));
            anims1.add(OBAnim.sequenceAnim((OBGroup)cont,Arrays.asList("frame1", "frame2"),0.05f + OB_Maths.randomInt(0, 3)* 0.02f,true));

        }
        if(!finish)
        {
            for(OBControl cont : eventTargets)
            {
                PointF loc = OBMisc.copyPoint(cont.position());
                loc.y =0.5f * bounds().height();
                anims2.add(OBAnim.moveAnim(loc,cont));
                anims2.add(OBAnim.sequenceAnim((OBGroup)cont,Arrays.asList("frame1", "frame2"),0.05f + OB_Maths.randomInt(0, 3)* 0.02f,true));

            }

        }
        playSfxAudio("scene_change",false);
        List<List<OBAnim>> animsFull = new ArrayList<>();
        List<Float> durationFull = new ArrayList<>();
        List<Integer> timingFull = new ArrayList<>();
        if(anims1.size() > 0)
        {
            animsFull.add(anims1);
            durationFull.add(1.0f);
            timingFull.add(OBAnim.ANIM_EASE_IN);

        }
        if(anims2.size() > 0)
        {
            animsFull.add(anims2);
            durationFull.add(1.0f);
            timingFull.add(OBAnim.ANIM_EASE_OUT);
        }
        OBAnimationGroup.chainAnimations(animsFull, durationFull,  false, timingFull, 1, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if(backgroundObjs.size() > 0)
                {
                    for(OBControl obj : objsOut)
                        detachControl(obj);

                }
                objsOut = null;
                objsOut = new ArrayList<OBControl>(eventTargets);
            }
        }, this);
        waitForSecs(0.2f);
        animateClouds(finish);
        waitForSecs(0.4f);
        waitSFX();

    }

    public void animateClouds(boolean finish)
    {
        List<OBAnim> anims = new ArrayList<>();
        final List<OBControl> currentClouds = new ArrayList<>();
        if(backgroundObjs.size() > 0)
        {
            List<Integer> indexes =  OBUtils.RandomIndexesTo(4);
            int index = 0;
            for(OBControl cloud : backgroundObjs)
            {
                PointF loc = OBMisc.copyPoint(cloud.position());
                loc.y = loc.y - bounds().height() - (0.15f *indexes.get(index++)  * bounds().height());
                anims.add(OBAnim.moveAnim(loc,cloud));
            }
        }
        if(!finish)
        {
            List<Integer> indexes = OBUtils.RandomIndexesTo(4);
            List<Integer> indexArr = new ArrayList<>(OBUtils.RandomIndexesTo(3));
            indexArr.add(OB_Maths.randomInt(0, 2));
            List<Integer> cloudIndexes = OBUtils.randomlySortedArray(indexArr);
            List<Float> xLocs = OBUtils.randomlySortedArray(Arrays.asList(0.15f, 0.35f, 0.55f, 0.85f));
            List<Float> yLocs = OBUtils.randomlySortedArray(Arrays.asList(0.2f, 0.4f, 0.6f, 0.8f));
            for(int i=0; i<4; i++)
            {
                OBControl cloud = clouds.get(cloudIndexes.get(i)).copy();
                cloud.setScale(cloud.scale() - i *0.1f);
                currentClouds.add(cloud);
                cloud.setPosition (OB_Maths.locationForRect(xLocs.get(i),yLocs.get(i) + 1 + 0.15f *(indexes.get(i)), bounds()));
                anims.add(OBAnim.moveAnim(OB_Maths.locationForRect(xLocs.get(i),yLocs.get(i),this.bounds()),cloud));
                attachControl(cloud);
                cloud.show();
                cloud.setZPosition(0.5f);
            }

        }
        OBAnimationGroup.runAnims(anims, 1.6f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                {
                    for(OBControl cloud : backgroundObjs)
                        detachControl(cloud);
                }
                backgroundObjs = null;
                backgroundObjs = currentClouds;
            }
        }, this);

    }

    public void demo5a() throws Exception
    {
        waitForSecs(0.5f);
        animateBaloons(false);
        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.6f,this.bounds()),-15,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        waitForSecs(0.2f);
        startScene();
    }

    public void pointerPointBaloonNums(int num,int audioIndex, int numCount) throws Exception
    {
        OBGroup baloon = (OBGroup)objectDict.get(String.format("obj_%d",num));
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.5f,baloon.frame())
                ,((1-(baloon.position().x/bounds().width()*1.0f))*-40.0f)-10
                ,0.5f,"FINAL",audioIndex,0.3f);
        OBLabel label = (OBLabel)baloon.objectDict.get("label");
        if(numCount >=0)
        {
            for(int i=1; i<=numCount; i++)
            {
                label.setHighRange(i-1,i,Color.RED);
                playAudioScene("FINAL",audioIndex+i,true);
                waitForSecs(0.3f);
                label.setHighRange(-1, -1, Color.BLACK);;
            }

        }
        else
        {
            playAudioScene("FINAL",audioIndex+1,true);
            waitForSecs(0.3f);
        }
    }

    public void pointerPointBaloonHilite(int num,int audioIndex) throws Exception
    {
        OBGroup baloon = (OBGroup)objectDict.get(String.format("obj_%d",num));
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.7f,baloon.frame())
                ,((1-(baloon.position().x/bounds().width()*1.0f))*-40.0f)-10
                ,0.3f,true);
        colourObj(baloon,true);
        playAudioScene("FINAL",audioIndex,true);
        thePointer.hide();
        waitForSecs(0.4f);
        colourObj(baloon,false);
        waitForSecs(0.3f);
    }

    public void demoFin5a() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointBaloonNums(2,0,2);
        pointerPointBaloonNums(1,3,2);
        pointerPointBaloonHilite(1,6);
    }


    public void demoFin5b() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointBaloonNums(1,0,1);
        pointerPointBaloonNums(2,2,1);
        pointerPointBaloonHilite(2,4);

    }
    public void demoFin5c() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointBaloonNums(3,0,1);
        pointerPointBaloonNums(2,2,1);
        pointerPointBaloonNums(1,4,1);
        pointerPointBaloonHilite(1,6);
    }

    public void demoFin5d() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPointBaloonNums(3,0,1);
        pointerPointBaloonNums(1,2,1);
        pointerPointBaloonNums(2,4,0);
        pointerPointBaloonHilite(2,6);

    }



}
