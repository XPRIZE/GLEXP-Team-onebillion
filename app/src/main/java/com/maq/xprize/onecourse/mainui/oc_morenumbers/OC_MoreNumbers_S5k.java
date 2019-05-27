package com.maq.xprize.onecourse.mainui.oc_morenumbers;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;
import com.maq.xprize.onecourse.utils.OB_MutInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 07/04/2017.
 */

public class OC_MoreNumbers_S5k extends OC_SectionController
{
    List<OBControl> flagOrder, eventTargets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master5k");
        flagOrder = new ArrayList<>();
        eventTargets = new ArrayList<>();
        objectDict.get("bottombar").setZPosition(3);
        objectDict.get("button_arrow").setZPosition(4);
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo5k();
            }

        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        eventTargets.clear();
        flagOrder.clear();
        int count = filterControls("obj_.*").size();
        for(int i=1; i<=count; i++)
        {
            OBGroup flag = (OBGroup)objectDict.get(String.format("obj_%d", i));
            flag.substituteFillForAllMembers("colour_.*", OBUtils.colorFromRGBString((String)flag.attributes().get("colour_background")));
            OC_MoreNumbers_Additions.insertIntoGroup(flag,Integer.valueOf((String)flag.attributes().get("num")), 90*flag.height()/300.0f, Color.BLACK,flag.objectDict.get("colour_1").getWorldPosition(), this );
            flag.setTop(objectDict.get("workrect").bottom());
            PointF loc = OBMisc.copyPoint(flag.position());
            loc.x =  getFlagLocation(i,count).x;
            flag.setPosition(loc);
            flag.setZPosition(2);
            flag.show();
            eventTargets.add(flag);
            flagOrder.add(flag);
        }
    }

    public void doMainXX() throws Exception
    {
        moveFlags(true,0.6f);
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl flag = finger(-1,0,eventTargets,pt);
            if(flag != null)
            {
                OBMisc.prepareForDragging(flag,pt,this);
                setStatus(STATUS_DRAGGING);

            }
            else if(finger(0,2,Arrays.asList(objectDict.get("button_arrow")),pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkButton();
                    }
                });
                playAudio(null);


            }

        }


    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            PointF pointF = OB_Maths.AddPoints(pt, dragOffset);
            pointF.y = target.position().y;
            target.setPosition(pointF);
        }

    }

    public void touchUpAtPoint(PointF pt,View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    reorderFlags();
                    setStatus(STATUS_WAITING_FOR_DRAG);
                }
            });

        }


    }

    public void checkButton() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(1,this);
        if(checkFlags())
        {
            gotItRightBigTick(true);
            OC_MoreNumbers_Additions.buttonSet(2,this);
            waitForSecs(0.2f);
            playAudioQueuedScene("FINAL",0.3f,true);
            waitForSecs(0.3f);
            if(currentEvent() != events.get(events.size()-1))
            {
                moveFlags(false,0.6f);
                waitForSecs(0.4f);
            }
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            OC_MoreNumbers_Additions.buttonSet(0,this);
            setStatus(STATUS_WAITING_FOR_DRAG);
            playAudioQueuedScene("INCORRECT",0.3f,false);

        }
    }

    public PointF getFlagLocation(int num,int size)
    {
        float startLocx = size == 4 ? 0.2f : 0.12f;
        return OB_Maths.locationForRect(startLocx + ((1-startLocx*2.0f)/(size-1))*(num-1),1f,objectDict.get("workrect").frame());
    }

    public void startScene() throws Exception
    {
        OC_MoreNumbers_Additions.buttonSet(0,this);
        OBMisc.doSceneAudio(4,setStatus(STATUS_WAITING_FOR_DRAG),this);
    }

    public void reorderFlags() throws Exception
    {
        OBGroup flag =(OBGroup)target;
        List objs = new ArrayList(eventTargets);
        objs.remove(flag);
        OBControl cont = OBMisc.nearestControlToPoint(objs, flag.position());
        target = null;
        if(cont != null)
        {
            flagOrder.remove(flag);
            if(cont.position().x < flag.position().x)
            {
                flagOrder.add(flagOrder.indexOf(cont)+1, flag);
            }
            else
            {
                flagOrder.add(flagOrder.indexOf(cont), flag);
            }
        }
        List<OBAnim> arr = new ArrayList<>();
        for(int i=0; i<flagOrder.size(); i++)
        {
            PointF destLoc = getFlagLocation(i+1,flagOrder.size());
            OBControl flagOr = flagOrder.get(i);
            if(destLoc.x != flagOr.position().x)
            {
                destLoc.y = flagOr.position().y;
                arr.add(OBAnim.moveAnim(destLoc,flagOr));
            }

        }
        playSfxAudio("drop",false);
        OBAnimationGroup.runAnims(arr,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        flag.setZPosition ( 2);

    }

    public boolean checkFlags()
    {
        boolean complete = true;
        int lastNum = -1;
        for(int i=0; i<flagOrder.size(); i++)
        {
            OBControl flag = flagOrder.get(i);
            int currentNum =  Integer.valueOf((String)flag.attributes().get("num"));
            if(lastNum > currentNum)
                complete = false;
            lastNum = currentNum;
        }
        return complete;
    }

    public void moveFlags(boolean moveIn,float duration) throws Exception
    {
        List<OBAnim> arr = new ArrayList<>();
        float top = objectDict.get("bottombar").top();
        for(OBControl flag : eventTargets)
        {
            if(moveIn)
            {
                arr.add(OBAnim.propertyAnim("bottom",top,flag));
            }
            else
            {
                arr.add(OBAnim.propertyAnim("top",top,flag));
            }
        }
        playSfxAudio(moveIn ? "flags_on" : "flags_off", false);
        OBAnimationGroup.runAnims(arr,duration,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        if(!moveIn)
        {
            for(OBControl flag : eventTargets)
                detachControl(flag);

        }
        waitSFX();

    }


    public void demo5k() throws Exception
    {
        waitForSecs(0.5f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("top",objectDict.get("workrect").bottom(),objectDict.get("bottombar")))
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        objectDict.get("button_arrow").show();
        playSfxAudio("button_appear",true);
        waitForSecs(0.5f);
        moveFlags(true,0.6f);
        waitSFX();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.65f,0.3f,objectDict.get("bottombar").frame()),-40,0.5f,"DEMO",0,0.3f);
        movePointerToPoint(OB_Maths.locationForRect(0.75f,0.25f,objectDict.get("obj_3").frame()),-25,0.5f,true);
        waitForSecs(0.2f);
        objectDict.get("obj_3").setZPosition(3);
        target =  objectDict.get("obj_3");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.3f,0.5f,objectDict.get("obj_1").frame()),objectDict.get("obj_3")),
                OBMisc.attachedAnim(objectDict.get("obj_3"),Arrays.asList(thePointer)),
                OBAnim.rotationAnim((float)Math.toRadians(-40),thePointer))
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        reorderFlags();
        moveScenePointer(OB_Maths.locationForRect(0.75f,0.6f,objectDict.get("obj_3").frame()),-40,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.3f,objectDict.get("bottombar").frame()),-30,0.5f,"DEMO",2,0.3f);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("button_arrow").frame()),-30,0.5f,"DEMO",4,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();

    }


}
