package com.maq.xprize.onecourse.hindi.mainui.oc_numberlines;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.view.View;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OC_SectionController;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimBlock;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 12/04/2017.
 */

public class OC_Numberlines_S1o extends OC_SectionController
{
    List<OBControl> blockOrder, targets;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master1o");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        blockOrder = new ArrayList<>();
        targets = new ArrayList<>();
        OBControl bottomBar = objectDict.get("bottombar");
        bottomBar.setBorderWidth((float) Math.ceil(bottomBar.lineWidth()));
        bottomBar.setZPosition(1);
        ((OBPath)bottomBar).sizeToBoundingBoxIncludingStroke();
        OBGroup workrect = (OBGroup) objectDict.get("workrect");
        workrect.setScale(1);
        float newHeight = bottomBar.top() - (MainViewController().topRightButton.position().y +
                MainViewController().topRightButton.height()) - bounds().height() * 0.03f;
        float newWidth = workrect.width() * newHeight / workrect.height();
        workrect.setAnchorPoint(new PointF(0.5f, 1));
        if (newWidth < 0.96 * bounds().width())
        {
            workrect.setHeight(newHeight);
            workrect.setWidth(newWidth);
        }
        for (int i = 1; i <= 10; i++)
        {
            createBlock(workrect.objectDict.get(String.format("blo%d", i)));
        }
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo1o();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        targets.clear();
        if(eventAttributes.get("target").equals("block"))
        {
            for(Integer num : OBMisc.stringToIntegerList(eventAttributes.get("block"),","))
            {
                targets.add(objectDict.get(String.format("block%d", num)));
            }
        }

        if(eventAttributes.get("target").equals("num"))
        {
            for(Integer num : OBMisc.stringToIntegerList(eventAttributes.get("correct"),","))
            {
                targets.add(objectDict.get(String.format("num%d", num)));
            }
        }
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

        if(status() == STATUS_AWAITING_CLICK)
        {
            if(eventAttributes.get("target").equals("block"))
            {
                final OBControl tar = finger(0,1,targets,pt);
                if(tar != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkBlock((OBGroup)tar);
                        }
                    });

                }

            }
            else if(eventAttributes.get("target").equals("num"))
            {
                final OBControl tar = finger(0,1,filterControls("num.*"),pt);
                if(tar != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkNum((OBLabel)tar);
                        }
                    });
                }
            }
            else if(eventAttributes.get("target").equals("bee"))
            {
                OBControl tar = finger(0,2,Arrays.asList(objectDict.get("bee")),pt);
                if(tar != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            playAudio(null);
                            performSel("demoFin",currentEvent());
                            nextScene();
                        }
                    });
                }
            }
        }
        else
        if(status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl tar = finger(0,1,blockOrder,pt);
            if(tar != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDrag(tar, pt);
                    }
                });

            }
        }
    }

    public void touchMovedToPoint(PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING && target!=null)
        {
            float x = OB_Maths.AddPoints(pt, dragOffset).x;
            target.setPosition(x, target.position().y);
        }
    }

    public void touchUpAtPoint(PointF pt, View v)
    {

        if (status() == STATUS_DRAGGING && target!=null)
        {
            setStatus(STATUS_BUSY);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkDrop();
                }
            });
        }
    }


    public void checkBlock(OBGroup tar) throws Exception
    {
        playAudio(null);
        tar.highlight();
        if(tar == objectDict.get(String.format("block%s",eventAttributes.get("correct"))))
        {
            gotItRightBigTick(true);
            tar.lowlight();
            performSel("demoFin",currentEvent());
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            tar.lowlight();
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void checkNum(OBLabel tar) throws Exception
    {
        playAudio(null);
        tar.setColour(Color.RED);
        if(targets.contains(tar))
        {
            gotItRightBigTick(true);
            waitForSecs(0.3f);
            tar.setColour(Color.BLACK);
            nextScene();
        }
        else
        {
            gotItWrongWithSfx();
            waitSFX();
            tar.setColour(Color.BLACK);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public void checkDrag(OBControl tar, PointF pt) throws Exception
    {
        playSfxAudio("drag",false);
        OBMisc.prepareForDragging(tar,pt,this);
        setStatus(STATUS_DRAGGING);
    }


    public void checkDrop() throws Exception
    {
        playAudio(null);
        if(reorderBlocks())
        {
            waitForSecs(0.2f);
            gotItRightBigTick(true);
            nextScene();
        }
        else
        {
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public boolean reorderBlocks() throws Exception
    {
        OBControl block = target;
        OBControl cont = closestBlock((OBGroup)block);
        target = null;
        if(cont != null)
        {
            blockOrder.remove(block);
            if(cont.position().x < block.position().x)
            {
                blockOrder.add(blockOrder.indexOf(cont)+1,block);
            }
            else
            {
                blockOrder.add(blockOrder.indexOf(cont),block);
            }
        }

        OBGroup workrect = (OBGroup)objectDict.get("workrect");
        List<OBAnim> arr = new ArrayList<>();
        boolean complete = true;
        for(int i=0; i<blockOrder.size(); i++)
        {
            PointF destLoc = workrect.objectDict.get(String.format("blo%d",i+1)).getWorldPosition();
            OBControl blockToAnim = blockOrder.get(i);
            if(destLoc.x != blockToAnim.position().x)
            {
                destLoc.y  = blockToAnim.position().y;
                arr.add(OBAnim.moveAnim(destLoc,blockToAnim));
            }
            if(blockOrder.get(i) != objectDict.get(String.format("block%d",i+1)))
                complete = false;
        }
        playSfxAudio("drop",false);
        OBAnimationGroup.runAnims(arr,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        block.setZPosition(1.5f);
        return complete;
    }

    public OBControl closestBlock(OBGroup block)
    {
        float distance = bounds().width();
        OBControl res = null;
        for(OBControl control : filterControls("block.*"))
        {
            if(block != control)
            {
                float temp = Math.abs(control.position().x - block.position().x);
                if(temp < distance)
                {
                    distance = temp;
                    res = control;
                }
            }
        }
        return res;
    }

    public void startScene() throws Exception
    {
        if(OBUtils.getBooleanValue(eventAttributes.get("drag")))
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
        }
    }

    public void animateNumsColour(List<Integer> arr, int colour)
    {
        List<OBAnim> anims = new ArrayList<>();
        for(Integer num : arr)
        {
            OBLabel numLabel = (OBLabel)objectDict.get(String.format("num%d",num));
            anims.add(OBAnim.colourAnim("colour",colour,numLabel));
        }
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_LINEAR,this);
    }

    public void loadNums()
    {
        lockScreen();
        for(int i=1; i<= 10; i++)
        {
            OBGroup block = (OBGroup)objectDict.get(String.format("block%d",i));
            OBLabel numLabel = (OBLabel)block.objectDict.get("label").copy();
            numLabel.setPosition(block.objectDict.get("label").getWorldPosition());
            attachControl(numLabel);
            objectDict.put(String.format("num%d",i),numLabel);
            numLabel.setOpacity (0);
        }
        unlockScreen();
    }

    public OBAnim beeFlyBlock()
    {
        final OBGroup bee = (OBGroup) objectDict.get("bee");
        return new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for (OBControl control : filterControls("block.*"))
                {

                    float flyDist = Math.abs(control.position().x - bee.position().x);
                    if (flyDist <= 0.5 * control.width())
                    {
                        if (flyDist <= 0.15 * control.width())
                        {
                            control.setOpacity(1);
                        }
                        else
                        {
                            control.setOpacity(1.0f - OB_Maths.clamp(0, 0.9f, 0.9f * (((flyDist - 0.15f * control.width()) / (0.35f * control.width())))));
                        }
                    }
                    else
                    {
                        control.setOpacity(0.1f);
                    }
                }
            }
        };
    }

    public void animateBlocks(List<Integer> blocks, float opacity)
    {
        List<OBAnim> anim = new ArrayList<>();
        for(Integer val : blocks)
        {
            anim.add(OBAnim.opacityAnim(opacity,objectDict.get(String.format("block%d", val))));
        }
        OBAnimationGroup.runAnims(anim,0.5,true,OBAnim.ANIM_LINEAR,this);
    }

    public void animateLabelsFrom(int from,int to, float opacity)
    {
        List<OBAnim> anim = new ArrayList<>();
        for(int i=from; i<=to; i++)
        {
            anim.add(OBAnim.opacityAnim(opacity,objectDict.get(String.format("num%d", i))));
        }
        OBAnimationGroup.runAnims(anim,0.5,true,OBAnim.ANIM_LINEAR,this);
    }

    public void showBlockNum(int num) throws Exception
    {
        OBGroup group = (OBGroup)objectDict.get(String.format("block%d",num));
        group.objectDict.get("label").show();
        playSfxAudio("show_num",true);
    }

    public void createBlock(OBControl control)
    {
        ((OBPath)control).sizeToBoundingBoxIncludingStroke();
        control.setBottom(objectDict.get("bottombar").top());
        int size = OBUtils.getIntValue((String)control.attributes().get("size"));
        List<OBControl> arr = new ArrayList<>();
        for(int i = 1; i <= size; i++)
        {
            OBControl con = control.copy();
            con.setPosition(control.getWorldPosition());
            con.show();
            if(i>1)
            {
                con.setBottom(arr.get(arr.size()-1).top() + con.lineWidth()*2);
            }
            else
            {
                con.setBottom(objectDict.get("bottombar").top()+ objectDict.get("bottombar").lineWidth());
            }
            arr.add(con);
        }

        OBLabel label = new OBLabel(String.format("%d",size), OBUtils.standardTypeFace(), 60*arr.get(0).height()/41.5f);
        label.setPosition(OB_Maths.locationForRect(0.5f,2f,arr.get(0).getWorldFrame()));
        label.setZPosition(1.5f);
        label.setColour(Color.BLACK);
        arr.add(label);
        OBGroup block = new OBGroup(arr);
        block.objectDict.put("label",label);
        block.objectDict.put("block",arr.get(0));
        block.setZPosition(1.5f);
        attachControl(block);
        objectDict.put(String.format("block%d", size),block);
        blockOrder.add(block);
        label.hide();
        block.highlight();
        block.lowlight();
        block.hide();
    }


    public void demo1o() throws Exception
    {
        waitForSecs(0.5f);
        List<Float> arr = Arrays.asList(0.2f,0.22f,0.28f,0.24f,0.24f,0.24f,0.18f,0.23f,0.31f,0.4f);
        playSfxAudio("show_stack",false);
        for(int i=0; i<10; i++)
        {
            objectDict.get(String.format("block%d",i+1)).show();
            waitForSecs(arr.get(i));
        }
        waitSFX();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.2f,objectDict.get("bottombar").frame()),-40,0.5f,"DEMO",0,0.3f);
        animateBlocks(Arrays.asList(1,2,3,4,5,6,7,8,9,10),0.1f);
        pointAndHilite(1,-50,1,true);
        pointAndHilite(2,-40,2,true);
        OBGroup block = (OBGroup)objectDict.get("block2");
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.5f,block.objectDict.get("label").getWorldFrame()),-30,0.5f,"DEMO",3,0.5f);
        thePointer.hide();
        animateBlocks(Arrays.asList(1,2),0.1f);
        animateBlocks(Arrays.asList(7,10),1);
        startScene();
    }

    public void demo1q() throws Exception
    {
        animateBlocks(Arrays.asList(3,4,5,8),1);
        loadPointer(POINTER_LEFT);
        pointAndHilite(3,-50,0,false);
        thePointer.hide();
        startScene();
    }

    public void demo1r() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointAndHilite(6,-40,0,true);
        pointAndHilite(5,-50,1,true);
        OBGroup block = (OBGroup)objectDict.get("block5");
        moveScenePointer(OB_Maths.locationForRect(1.2f,0.5f,block.objectDict.get("label").getWorldFrame()),-40,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
        animateBlocks(Arrays.asList(5),0.1f);
        animateBlocks(Arrays.asList(8,9,10),1);
        loadPointer(POINTER_LEFT);
        pointAndHilite(9,-50,3,false);
        thePointer.hide();
        startScene();
    }

    public void demo1t() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        final OBGroup block = (OBGroup)objectDict.get("block10");
        block.setZPosition ( 2);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,block.objectDict.get("block").getWorldFrame()),-40,0.5f,"DEMO",1,0.3f);
        playSfxAudio("drag",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("right", objectDict.get("block9").right() + 0.5f * block.width(), block),
                new OBAnimBlock()
                {
                    @Override
                    public void runAnimBlock(float frac)
                    {
                        thePointer.setPosition ( OB_Maths.locationForRect(0.5f,0.5f,block.objectDict.get("block").getWorldFrame()));
                    }
                },
                OBAnim.rotationAnim((float) Math.toRadians(0), thePointer))
                ,0.8,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        target = block;
        waitForSecs(0.2f);
        reorderBlocks();
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demo1u() throws Exception
    {
        animateBlocks(Arrays.asList(1,2,3,4,5,6,7,8,9,10),0.1f);
        OBGroup bee = (OBGroup)objectDict.get("bee");
        bee.setLeft ( 0);
        bee.show();
        bee.setRotation ( (float)Math.toRadians(-20));
        float left = objectDict.get("block1").position().x + 0.5f*bee.width();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("right",left,bee),
                OBAnim.propertyAnim("bottom",objectDict.get("block1").top(),bee),
                OBAnim.sequenceAnim(bee,Arrays.asList("frame2","frame3"),0.05f,true),
                OBAnim.rotationAnim((float)Math.toRadians(0),bee),
                beeFlyBlock())
                ,0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        animateFrames(Arrays.asList("frame2","frame1"),0.05f,bee);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("bee").frame()),-40,0.5f,"DEMO",0,0.3f);
        thePointer.hide();
        startScene();
    }

    public void demo1v() throws Exception
    {
        lockScreen();
        for(int i =1; i<=10; i++)
        {
            objectDict.get("bottombar").setZPosition(2.1f);
            OBGroup block =  (OBGroup)objectDict.get(String.format("block%d",i));
            block.objectDict.get("label").hide();
            OBControl numLabel = objectDict.get(String.format("num%d",i));
            numLabel.setOpacity(1);
            numLabel.setZPosition(2.2f);
        }
        unlockScreen();

        List<OBAnim> anim = new ArrayList<>();
        anim.add(OBAnim.propertyAnim("top",bounds().height() +10,objectDict.get("block10")));
        anim.add(new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for(int i=1; i<=9; i++)
                {
                    OBGroup block = (OBGroup)objectDict.get(String.format("block%d",i));
                    if(block.top() <= objectDict.get("block10").top())
                        block.setTop(objectDict.get("block10").top());
                }
                if(objectDict.get("bottombar").top() < objectDict.get("block10").top())
                    objectDict.get("bottombar").setTop(objectDict.get("block10").top());
            }
        });

        for(OBControl control : filterControls("num.*"))
        {
            PointF loc = OBMisc.copyPoint(control.position());
            loc.y = 0.5f * bounds().height();
            anim.add(OBAnim.moveAnim(loc,control));
        }
        playSfxAudio("hide_stack",false);
        OBAnimationGroup.runAnims(anim,2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.3f);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        animateNumsColour(Arrays.asList(2,5),Color.RED);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(1.5f);
        animateNumsColour(Arrays.asList(2,5),Color.BLACK);
        waitForSecs(0.3f);
        startScene();
    }

    public void demo1zb1() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        animateNumsColour(Arrays.asList(4,7),Color.RED);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,true);
        waitForSecs(1.5f);
        animateNumsColour(Arrays.asList(4,7),Color.BLACK);
        waitForSecs(0.3f);
        startScene();
    }

    public void demoFin1o() throws Exception
    {
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        showBlockNum(10);
        waitForSecs(0.5f);
        playAudioScene("FINAL",1,true);
        waitForSecs(0.3f);
        showBlockNum(7);
        waitForSecs(0.5f);
        playAudioScene("FINAL",2,true);
        animateBlocks(Arrays.asList(7,10),0.1f);
    }

    public void demoFin1q() throws Exception
    {
        animateBlocks(Arrays.asList(5,8),0.1f);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        showBlockNum(4);
        waitForSecs(0.5f);
        animateBlocks(Arrays.asList(3,4),0.1f);
    }

    public void demoFin1r() throws Exception
    {
        animateBlocks(Arrays.asList(6,10),0.1f);
        playAudioScene("FINAL",0,true);
        waitForSecs(0.3f);
        showBlockNum(8);
        waitForSecs(0.5f);
        animateBlocks(Arrays.asList(1,2,3,4,5,6,7,10),1);
    }

    public void demoFin1u() throws Exception
    {
        loadNums();
        OBGroup bee = (OBGroup)objectDict.get("bee");
        PointF flyPos1 = OBMisc.copyPoint(bee.position());
        flyPos1.y -= bee.height();
        playAudioScene("FINAL",0,true);
        animateFrames(Arrays.asList("frame1","frame2"),0.05f,bee);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(flyPos1,bee),
                OBAnim.sequenceAnim(bee,Arrays.asList("frame3","frame2"),0.05f,true),OBAnim.rotationAnim((float)Math.toRadians(-20),bee))
                ,0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        PointF flyPos2 =  OBMisc.copyPoint(objectDict.get("block10").position());
        flyPos2.y = objectDict.get("block10").top() - (objectDict.get("block1").top() - flyPos1.y);
        playAudioScene("FINAL",1,false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(flyPos2,bee),
                OBAnim.sequenceAnim(bee,Arrays.asList("frame2","frame3"),0.05f,true), beeFlyBlock())
                ,7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitAudio();
        OBGroup first = (OBGroup)objectDict.get("block1");
        OBGroup last = (OBGroup)objectDict.get("block10");
        animateLabelsFrom(1,9,1);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,first.objectDict.get("label").getWorldFrame()) ,0.5f,true);
        playAudioScene("FINAL",2,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,last.objectDict.get("label").getWorldFrame()) ,3f,true);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        animateLabelsFrom(1,9,0);
        waitForSecs(0.3f);
        bee.flipHoriz();
        playAudioScene("FINAL",3,false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(flyPos1,bee),
                OBAnim.sequenceAnim(bee,Arrays.asList("frame2","frame3"),0.05f,true),
                OBAnim.rotationAnim((float)Math.toRadians(0),bee),
                beeFlyBlock())
                ,7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitAudio();
        animateLabelsFrom(2,10,1);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,last.objectDict.get("label").getWorldFrame()) ,0.5f,true);
        playAudioScene("FINAL",4,false);
        movePointerToPoint(OB_Maths.locationForRect(1f,1f,first.objectDict.get("label").getWorldFrame()) ,3f,true);
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        animateLabelsFrom(2,10,0);
        waitForSecs(0.3f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(new PointF(-0.1f, 0.5f), bounds()),bee),
                OBAnim.sequenceAnim(bee,Arrays.asList("frame2","frame3"),0.05f,true),
                OBAnim.rotationAnim((float)Math.toRadians(20),bee),
                beeFlyBlock())
                ,1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        animateBlocks(Arrays.asList(1,2,3,4,5,6,7,8,9,10),1);
    }

    public void pointAndHilite(int num,float rotation, int index, boolean hilite) throws Exception
    {
        OBGroup group = (OBGroup)objectDict.get(String.format("block%d",num));
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,group.objectDict.get("block").getWorldFrame()),rotation,0.5f,true);
        if(hilite)
            animateBlocks(Arrays.asList(num),1);
        waitForSecs(0.2f);
        playAudioScene("DEMO",index,true);
        waitForSecs(0.3f);
        showBlockNum(num);
        waitForSecs(0.5f);
    }


}
