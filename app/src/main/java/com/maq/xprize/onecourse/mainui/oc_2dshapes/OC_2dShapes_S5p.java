package com.maq.xprize.onecourse.mainui.oc_2dshapes;

import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBAudioManager;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/03/2018.
 */

public class OC_2dShapes_S5p extends OC_SectionController
{
    List<OBControl> cardsFront, cardsBack;
    OBControl lastCard;
    boolean startScene;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2");
        cardsFront = new ArrayList<>();
        cardsBack = new ArrayList<>();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        startScene = true;
        lastCard = null;
        //OBAudioManager.sharedAudioManager().prepare(audioScenes.get("sfx") .get("card_turn") .get(0) onChannel:"special");
        loadCards();
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5p();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
    }

    public void doMainXX() throws Exception
    {

    }

    public void touchDownAtPoint(PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(startScene)
            {
                OBControl targ = finger(0,1,cardsBack,pt);
                if(targ != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            startScene = false;
                            distributeCards();
                            demo5p2();
                        }
                    });
                }
            }
            else
            {
                final OBControl targ = finger(0,1,cardsBack,pt);
                if(targ != null && (boolean)targ.propertyValue("back"))
                {
                    setStatus(STATUS_BUSY);
                    targ.setProperty("back",false);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkCard(targ);
                        }
                    });
                }
            }
        }
    }

    public void checkCard(OBControl targ) throws Exception
    {
        if(lastCard == null)
        {
            flipCard(Arrays.asList(cardsBack.indexOf(targ)),false,false,true);
            lastCard = targ;
            setStatus(STATUS_AWAITING_CLICK);
        }
        else
        {
            flipCard(Arrays.asList(cardsBack.indexOf(targ)),false,true,true);
            if(targ.settings.get("tag") == lastCard.settings.get("tag"))
            {
                gotItRightBigTick(false);
                waitSFX();
                lastCard = null;
                playAudioScene("FINAL",(int)targ.propertyValue("tag") -1,false);
                if(cardsComplete())
                {
                    waitForAudio();
                    waitForSecs(0.3f);
                    displayTick();
                    waitForSecs(0.3f);
                    nextScene();
                }
                else
                {
                    setStatus(STATUS_AWAITING_CLICK);
                }
            }
            else
            {
                gotItWrongWithSfx();
                targ.setProperty("back",true);
                lastCard.setProperty("back",true);
                flipCard(Arrays.asList(cardsBack.indexOf(targ) , cardsBack.indexOf(lastCard)),true,true,false);
                lastCard = null;
                setStatus(STATUS_AWAITING_CLICK);
            }
        }
    }

    public void loadCards()
    {
        OBGroup card =(OBGroup ) objectDict.get("card");
        OBControl cardBack = card.copy();
        card.objectDict.get("pattern").hide();
        card.objectDict.get("background_back").hide();
        card.objectDict.get("background_front").show();
        OBControl cardFront = card.copy();
        card.hide();
        List<OBControl> shapes = OBUtils.randomlySortedArray(filterControls("shape_.*"));
        OBControl shape1 = objectDict.get("shape_1");
        OBControl shape2 = objectDict.get("shape_7");
        shapes.remove(shape1);
        shapes.remove(shape2);
        shapes.add(0,shape1);
        shapes.add(6, shape2);
        for(int i=0; i<3; i++)
        {
            for(int j=0; j<4; j++)
            {
                OBControl crd1 = cardBack.copy();
                crd1.setZPosition(10);
                crd1.setPosition(OB_Maths.locationForRect(j/3.0f,i/2.0f,objectDict.get("workrect").frame()));
                attachControl(crd1);
                crd1.setDoubleSided(false);
                crd1.setProperty("back",true);
                crd1.texturise(true,this);
                crd1.m34 = -1/1200f;
                cardsBack.add(crd1);
                OBControl shape = shapes.get(i*4 + j);
                shape.setZPosition(5);
                OBControl crd = cardFront.copy();
                crd.setZPosition(1);
                int tag = OBUtils.getIntValue((String)shape.attributes().get("tag"));
                crd.setPosition(crd1.position());
                shape.setPosition(crd1.position());
                if(shape instanceof OBPath)
                    ((OBPath)shape).sizeToBoundingBoxIncludingStroke();
                OBGroup crd2 = new OBGroup(Arrays.asList(shape,crd));
                crd2.setProperty("tag",tag);
                crd1.setProperty("tag",tag);
                attachControl(crd2);
                crd2.setDoubleSided(false);
                crd2.setYRotation((float)Math.PI);
                crd2.texturise(false,this);
                crd2.m34 = -1/1200f;
                cardsFront.add(crd2);
                crd2.hide();
            }
        }

        for(int i=1; i<cardsBack.size(); i++)
        {
            OBControl crd1 = cardsBack.get(i-1);
            OBControl crd2 = cardsBack.get(i);
            PointF loc = OBMisc.copyPoint(crd1.position());
            loc.x += applyGraphicScale(2);
            loc.y += applyGraphicScale(2);
            crd2.setPosition(loc);
        }
    }

    public void distributeCards() throws Exception
    {
        for(int i=cardsBack.size()-1; i>0; i--)
        {
            playSfxAudio("card_deal",false);
            OBControl crd = cardsBack.get(i);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(cardsFront.get(i).position() ,crd))
            ,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
        for(OBControl crd : cardsFront)
            crd.show();
    }

    public void flipCard(final List<Integer> ixs, final boolean down, boolean wait,boolean audio) throws Exception
    {
        if(audio)
            playSFX("special");

        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(int i : ixs)
                {
                    OBGroup card = (OBGroup)cardsFront.get(i);
                    OBControl back = cardsBack.get(i);
                    double ang;
                    if(down)
                        ang = Math.PI * frac;
                    else
                        ang = Math.PI  + Math.PI  * frac;
                    card.setYRotation((float)ang);
                    if(down)
                        ang = Math.PI  + Math.PI  * frac;
                    else
                        ang = Math.PI  * frac;
                    back.setYRotation((float)ang);
                }
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(anim),0.4,wait,OBAnim.ANIM_EASE_IN_EASE_OUT,null,this);
    }


    public boolean cardsComplete()
    {
        for(OBControl cont : cardsBack)
        {
            if((boolean)cont.propertyValue("back"))
                return false;
        }
        return true;
    }


    public void demo5p() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.7f,0.7f,cardsBack.get(cardsBack.size()-1).frame()),
                -10,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        OBMisc.doSceneAudio(0,setStatus(STATUS_AWAITING_CLICK), this);
    }

    public void demo5p2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        for(int i=0; i<2; i++)
        {
            int index = i==0?0:6;
            OBControl card = cardsBack.get(index);
            movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,card.frame()),-25,0.7f,true);
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,card.frame()),-25,0.2f,true);
            flipCard(Arrays.asList(index),false,true,true);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,card.frame()),-25,0.2f,"DEMO2",0,0.5f);
            card.setProperty("back",false);
        }
        thePointer.hide();
        OBMisc.doSceneAudio(0,currentEvent(),setStatus(STATUS_AWAITING_CLICK),"2",this);
    }

}
