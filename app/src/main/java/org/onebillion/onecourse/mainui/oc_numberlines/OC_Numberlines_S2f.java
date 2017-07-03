package org.onebillion.onecourse.mainui.oc_numberlines;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 12/04/2017.
 */

public class OC_Numberlines_S2f extends OC_SectionController
{
    List<OBControl> eventTargets;
    List<PointF> divPositions;
    List<OBLabel> correctNums;
    boolean singleMode, orderMode;
    int hiliteColour, numColour;
    OBControl hiliteNum;
    OBPath tempLine;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master2f");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        eventTargets = new ArrayList<>();
        divPositions = new ArrayList<>();
        correctNums = new ArrayList<>();
        singleMode = orderMode = false;
        hiliteColour = OBUtils.colorFromRGBString(eventAttributes.get("hilitecolour"));
        numColour = OBUtils.colorFromRGBString(eventAttributes.get("numcolour"));
        //objectDict.get("workrect").show();
        OC_Numberlines_Additions.drawNumberLine(objectDict.get("numberline"),11,0,OBUtils.colorFromRGBString(eventAttributes.get("maincolour")),
                OBUtils.colorFromRGBString(eventAttributes.get("divcolour")),numColour, this);
        OBPath mainline = (OBPath)objectDict.get("mainline");
        mainline.hide();
        mainline.setStrokeEnd(0);

        OBPath divLine = (OBPath)this.objectDict.get("divline_0");
        OBControl arrow = objectDict.get("arrow");
        PointF topPoint = OBMisc.copyPoint(divLine.position());
        topPoint.y =  arrow.top();
        tempLine = new OBPath(divLine.position(), topPoint);
        tempLine.setLineWidth(divLine.lineWidth());
        tempLine.setStrokeColor(divLine.strokeColor());
        tempLine.setZPosition(10);
        tempLine.sizeToBoundingBoxIncludingStroke();
        attachControl(tempLine);
        tempLine.setAnchorPoint(0.5f,1);
        tempLine.hide();

        objectDict.get("dot1").setPosition(OB_Maths.locationForRect(0f,0.5f,objectDict.get("numberline").frame()));
        objectDict.get("dot1").show();
        objectDict.get("dot1").setOpacity(0);
        objectDict.get("dot2").setPosition(OB_Maths.locationForRect(1f,0.5f,objectDict.get("numberline").frame()));
        objectDict.get("dot2").show();
        objectDict.get("dot2").setOpacity(0);
        for(int i=0; i<=10; i++)
        {
            OBControl con = objectDict.get(String.format("divline_%d", i));
            divPositions.add(OBMisc.copyPoint(con.position()));
            con.setPosition(OB_Maths.locationForRect(0.15f+(i/10.0f)*0.7f,0.5f,objectDict.get("bottombar").frame()));
            con.setProperty("startpos",OBMisc.copyPoint(con.position()));
            con.hide();
            OBControl num = objectDict.get(String.format("num_%d", i));
            num.setProperty("destpos",OBMisc.copyPoint(num.position()));
            num.hide();
        }

        objectDict.get("bottombar").setZPosition(1);
        OC_Numberlines_Additions.setUpBasket(this);
        objectDict.get("box_front").setProperty("destleft",objectDict.get("box_front").left());
        objectDict.get("box_front").setLeft(bounds().width() +10);
        OC_Numberlines_Additions.moveBasketParts(this);
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo2f();
            }
        });
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        eventTargets.clear();
        correctNums.clear();
        if(eventAttributes.get("target").equals("divline"))
        {
            eventTargets.addAll(filterControls("divline_.*"));
        }
        else if(eventAttributes.get("target").equals("dot"))
        {
            eventTargets.addAll(filterControls("dot.*"));
        }
        else if(eventAttributes.get("target").equals("num"))
        {
            if(OBUtils.getBooleanValue(eventAttributes.get("order")))
                orderMode = true;
            else
                orderMode = false;
            for(int i=0; i<=10; i++)
            {
                eventTargets.add(objectDict.get(String.format("num_%d", i)));
            }
        }
        else if(eventAttributes.get("target").equals("tnum"))
        {
            eventTargets.addAll(filterControls("num_.*"));
            hiliteNum = objectDict.get(String.format("num_%s",eventAttributes.get("hilitenum")));
            if(OBUtils.getBooleanValue(eventAttributes.get("single")))
                singleMode = true;
            else
                singleMode = false;
            for(String num : eventAttributes.get("num").split(","))
            {
                correctNums.add((OBLabel)objectDict.get(String.format("num_%s",num)));
            }
        }
        else if(eventAttributes.get("target").equals("box"))
        {
            String[] dropLocs = eventAttributes.get("droplocs").split(",");
            int startnum = OBUtils.getIntValue(eventAttributes.get("startnum"));
            for(int i=0; i<=10; i++)
            {
                PointF startPoint = OB_Maths.locationForRect(Float.valueOf(dropLocs[i*2]),Float.valueOf(dropLocs[(i*2)+1]), objectDict.get("bottombar").frame());
                OBLabel num = (OBLabel)objectDict.get(String.format("num_%d", i));
                num.hide();
                num.setString(String.format("%d", startnum+i));
                num.setProperty("startpos",OBMisc.copyPoint(startPoint));
                num.setRotation((float)Math.toRadians(i*10));
            }
        }
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {

            if (status() == STATUS_WAITING_FOR_DRAG && eventAttributes.get("target").equals("num"))
            {
                final OBControl cont = finger(0, 1, eventTargets, pt);
                if (cont != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkDrag(cont, pt);
                        }
                    });

                }
            } else if (status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("box"))
            {
                if (finger(0, 1, filterControls("box_.*"), pt) != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkBox();
                        }
                    });
                }
            } else if (status() == STATUS_WAITING_FOR_DRAG && eventAttributes.get("target").equals("divline"))
            {
                final OBControl cont = finger(0, 1, eventTargets, pt);
                if (cont != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkDrag(cont, pt);
                        }
                    });
                }
            } else if (status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("tnum"))
            {
                final OBControl cont = finger(0, 1, eventTargets, pt);
                if (cont != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkDragNum((OBLabel)cont);
                        }
                    });

                }
            } else if (status() == STATUS_AWAITING_CLICK && eventAttributes.get("target").equals("dot"))
            {
                final OBControl cont = finger(0, 2, eventTargets, pt);
                if (cont != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkDot((OBPath)cont);
                        }
                    });
                }

            }


    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    public void touchUpAtPoint(PointF pt,View v)
    {
            if (status() == STATUS_DRAGGING && target != null)
            {
                setStatus(STATUS_BUSY);
                final OBControl cont = this.target;
                this.target = null;
                if (eventAttributes.get("target").equals("divline"))
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkLineDrop(cont);
                        }
                    });
                }
                else if (eventAttributes.get("target").equals("num"))
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            checkNumDrop(cont);
                        }
                    });
                }
            }
    }

    public void checkDrag(OBControl cont, PointF pt) throws Exception
    {
        playSfxAudio("drag", false);
        OBMisc.prepareForDragging(cont, pt, this);
        setStatus(STATUS_DRAGGING);
    }

    public void checkBox() throws Exception
    {
        throwNumbers();
        moveBasket(false);
        nextScene();
    }

    public void checkDragNum(OBLabel cont) throws Exception
    {
        cont.setColour(hiliteColour);
        if (correctNums.contains(cont))
        {
            gotItRightBigTick(false);
            eventTargets.remove(cont);
            if (eventTargets.size() == 11 - correctNums.size() || singleMode)
            {
                waitSFX();
                waitForSecs(0.3f);
                if (!performSel("demoFin", currentEvent()))
                {
                    resetNumbers(correctNums);
                }
                nextScene();
            } else
            {
                setStatus(STATUS_AWAITING_CLICK);
            }

        } else
        {
            gotItWrongWithSfx();
            waitSFX();
            if (cont == hiliteNum)
                cont.setColour(Color.RED);
            else
                cont.setColour(numColour);
            setStatus(STATUS_AWAITING_CLICK);
            playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void checkDot(OBPath cont) throws Exception
    {
        playAudio(null);
        cont.setFillColor(OBUtils.highlightedColour(cont.fillColor()));
        playSfxAudio("dot_touch", false);
        eventTargets.remove(cont);
        if (eventTargets.size() == 0)
        {
            waitSFX();
            waitForSecs(0.2f);
            OBPath mainline = (OBPath)objectDict.get("mainline");
            mainline.show();
            playSfxAudio("draw_line", false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("strokeEnd", 1, mainline))

                    , 1.5, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            playSFX(null);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,
                    objectDict.get("dot1")),
                    OBAnim.opacityAnim(0, objectDict.get("dot2")))
                    , 0.5, true, OBAnim.ANIM_LINEAR, this);
            nextScene();
        } else
        {
            setStatus(STATUS_AWAITING_CLICK);

        }
    }

    public void checkLineDrop(OBControl cont) throws Exception
    {
        OBControl workRect = objectDict.get("numberline");
        RectF targetRect = new RectF(workRect.frame());

        targetRect.inset(-1.1f * workRect.width(), 0.3f * workRect.height());

        if (RectF.intersects(targetRect, cont.frame()))
        {

            playAudio("correct");
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(divPositions.get(11 - eventTargets.size()), cont)), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            objectDict.put(String.format("divline_%d", (int) (11 - eventTargets.size())), cont);
            continueDrag(cont);
        }
        else
        {
            playAudio(null);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)cont.propertyValue("startpos"), cont)), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            cont.setZPosition(cont.zPosition() - 10);
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public void checkNumDrop(OBControl cont) throws Exception
    {
        OBControl workRect = objectDict.get("numberline");
        PointF destPos = (PointF)cont.propertyValue("destpos");
        float rectWidth = workRect.width()* 0.08f;
        float rectHeight = workRect.height() * 0.35f;
        float left = destPos.x - rectWidth / 2.0f;
        float top = destPos.y - rectHeight * 0.7f;
        RectF targetRect = new RectF(left, top, left+rectWidth, top+rectHeight);
        if (RectF.intersects(targetRect, cont.frame()) && (!orderMode || (orderMode && cont == eventTargets.get(eventTargets.size()-1))))
        {
            gotItRightBigTick(false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(destPos, cont)), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            continueDrag(cont);
        }
        else
        {
            boolean doAudio = workRect.frame().contains(cont.frame()) || RectF.intersects(targetRect, cont.frame);
            if (doAudio) gotItWrongWithSfx();
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)cont.propertyValue("startpos"), cont)), 0.2, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            cont.setZPosition(cont.zPosition() - 10);
            if(doAudio)
                waitSFX();
            setStatus(STATUS_WAITING_FOR_DRAG);
            if(doAudio)
                playAudioQueuedScene("INCORRECT", 300, false);
        }
    }

    public void startScene() throws Exception
    {
        if(eventAttributes.get("target").equals("box"))
        {
            moveBasket(true);
        }
        if(eventAttributes.get("target").equals("num")||eventAttributes.get("target").equals("divline"))
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
        }
    }

    public void continueDrag(OBControl cont) throws Exception
    {
        eventTargets.remove(cont);
        if(eventTargets.size() == 0)
        {
            waitForSecs(0.2f);
            displayTick();
            waitForSecs(0.2f);
            if(!performSel("demoFin",currentEvent()))
            {
                playAudioQueuedScene("FINAL",300,true);
                waitForSecs(0.5f);

            }
            nextScene();
        }
        else
        {
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
    }

    public void throwNumbers() throws Exception
    {
        playSfxAudio("basket_open",false);
        animateFrames(Arrays.asList("frame1", "frame2", "frame3"),0.1f,(OBGroup)objectDict.get("box_top"));
        playSFX(null);
        List<OBAnim> anims = new ArrayList<>();
        lockScreen();
        for(OBControl control : filterControls("num_.*"))
        {
            control.setOpacity(1);
            control.setPosition(OB_Maths.locationForRect(0.5f,0.75f,objectDict.get("box_front").frame()));
            PointF endPoint = (PointF)control.settings.get("startpos");
            Path path = new Path();
            path.moveTo(control.position().x, control.position().y);
            PointF cp1 = OB_Maths.locationForRect(0.5f,-1.8f,objectDict.get("box_front").frame());
            PointF cp2 = OB_Maths.AddPoints(endPoint ,new PointF(applyGraphicScale(5),applyGraphicScale(5)));
            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, endPoint.x, endPoint.y);
            control.setZPosition(1.2f);
            control.show();
            anims.add(OBAnim.pathMoveAnim(control,path,false,0));
            anims.add(OBAnim.rotationAnim((float)Math.toRadians(360),control));
        }
        unlockScreen();
        playSfxAudio("numbers_fly_out",false);
        OBAnimationGroup.runAnims(anims,3,false,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(1f);
        lockScreen();
        for(OBControl control : filterControls("num_.*"))
        {
            control.setZPosition(5);
        }
        unlockScreen();
        waitForSecs(2.2f);
        playSFX(null);
        animateFrames(Arrays.asList("frame3", "frame2", "frame1"),0.1f,(OBGroup)objectDict.get("box_top"));
    }


    public void moveBasket(boolean inside) throws Exception
    {
        OBControl front = objectDict.get("box_front");
        final OBControl top = objectDict.get("box_top");
        playSfxAudio("basket_slide",false);
        final OBSectionController sectionController = this;
        OBAnim blockAnim =   new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                OC_Numberlines_Additions.moveBasketParts(sectionController);
                switch((int)(frac*100)%4)
                {
                    case 0:
                        top.setRotation( (float)Math.toRadians(0));
                        break;
                    case 1:
                        top.setRotation( (float)Math.toRadians(3));
                        break;
                    case 2:
                        top.setRotation( (float)Math.toRadians(0));
                        break;
                    case 3:
                        top.setRotation( (float)Math.toRadians(-3));
                        break;

                }
            }
        };

        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("left",inside ? (float)front.propertyValue("destleft") : bounds().width()+applyGraphicScale(10),front),blockAnim),
                1.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT ,this);
        playSFX(null);
    }

    public void resetNumbers(List<OBLabel> nums) throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBLabel num : nums)
        {
            if(num.colour() != numColour)
                anims.add(OBAnim.colourAnim("colour",numColour,num));

        }
        OBAnimationGroup.runAnims(anims,0.25,true,OBAnim.ANIM_LINEAR,this);
    }

    public void hideNumbers() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl num : filterControls("num_.*"))
        {
            anims.add(OBAnim.opacityAnim(0,num));
        }
        OBAnimationGroup.runAnims(anims,0.3,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(0.5f);
    }

    public void flashArrow(boolean forward) throws Exception
    {
        OBControl arrow = objectDict.get("arrow");
        arrow.setOpacity(0);
        arrow.show();
        OBPath divline = (OBPath)objectDict.get(String.format("divline_%s", eventAttributes.get("hilitenum")));
        tempLine.setPosition(divline.position());
        tempLine.setStrokeEnd(0);
        tempLine.show();
        if(forward)
        {
            arrow.setLeft(divline.position().x);
        }
        else
        {
            arrow.flipHoriz();
            arrow.setRight(divline.position().x);
        }
        float startHeight = divline.height();
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("strokeEnd",1,tempLine)),0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1,arrow)),0.5,true,OBAnim.ANIM_LINEAR,this);
        playAudioQueuedScene("FINAL",300,true);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(0,arrow)),0.5,true,OBAnim.ANIM_LINEAR,this);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("strokeEnd",0,tempLine)),0.6,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        tempLine.hide();
        arrow.hide();
        if(!forward)
            arrow.flipHoriz();
    }

    public void pointerDragNumber(int num,float duration) throws Exception
    {
        OBControl numControl = objectDict.get(String.format("num_%d",num));
        movePointerToPoint(numControl.position(),-30,duration,true);
        playSfxAudio("drag",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF)numControl.propertyValue("destpos") ,numControl),
                OBMisc.attachedAnim(numControl,Arrays.asList(thePointer)))
                ,0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("div_line",true);
        eventTargets.remove(numControl);

    }
    public void pointerPoint(int num,int colour)
    {
        OBLabel label = (OBLabel)objectDict.get(String.format("num_%d", num));
        movePointerToPoint(OB_Maths.locationForRect(0.8f,0.8f,label.frame()),-40,0.5f,true);
        label.setColour(colour);
    }

    public void demo2f() throws Exception
    {
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.opacityAnim(1,objectDict.get("dot1")),
                OBAnim.opacityAnim(1,objectDict.get("dot2")))
                ,0.5,true,OBAnim.ANIM_LINEAR,this);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        startScene();
    }

    public void demo2g() throws Exception
    {
        lockScreen();
        showControls("divline_.*");
        objectDict.get("bottombar").show();

        unlockScreen();
        playSfxAudio("pop_on",true);
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("bottombar").frame()),-30,0.4f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,1f,objectDict.get("divline_0").frame()),-30,0.4f,"DEMO",1,0.3f);
        OBControl divline = objectDict.get("divline_0");
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,divline.frame()),-30,0.2f,true);
        playSfxAudio("drag",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(divPositions.get(0) ,divline),
                OBMisc.attachedAnim(divline,Arrays.asList(thePointer)))
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("div_line",true);
        eventTargets.remove(divline);
        OBControl divline2 = objectDict.get("divline_1");
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,divline2.frame()),-30,0.4f,true);
        playSfxAudio("drag",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(OB_Maths.locationForRect(0.15f,0.65f,objectDict.get("numberline").frame()),divline2),
                OBMisc.attachedAnim(divline2,Arrays.asList(thePointer)))
                ,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("div_line",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(divPositions.get(1),divline2))
                ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        eventTargets.remove(divline2);
        waitForSecs(0.4f);
        thePointer.hide();
        startScene();
    }

    public void demo2i2() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,0.6f,objectDict.get("num_0").frame()),-30,0.4f,"DEMO",1,0.3f);
        pointerDragNumber(0,0.2f);
        pointerDragNumber(1,0.45f);
        waitForSecs(0.4f);
        thePointer.hide();
        startScene();
    }

    public void demo2m() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        pointerPoint(OBUtils.getIntValue(eventAttributes.get("hilitenum")),Color.RED);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.3f);
        pointerPoint(3,hiliteColour);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.5f);
        resetNumbers(Arrays.asList((OBLabel)objectDict.get("num_3")));
        thePointer.hide();
        startScene();
    }

    public void demoFin2o() throws Exception
    {
        waitForSecs(0.3f);
        resetNumbers((List<OBLabel>)(Object)filterControls("num_.*"));
    }


    public void demo2p() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPoint(OBUtils.getIntValue(eventAttributes.get("hilitenum")),Color.RED);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin2p() throws Exception
    {
        flashArrow(true);
        resetNumbers((List<OBLabel>)(Object)filterControls("num_.*"));
    }

    public void demo2r() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPoint(OBUtils.getIntValue(eventAttributes.get("hilitenum")),Color.RED);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin2r() throws Exception
    {
        flashArrow(false);
        resetNumbers((List<OBLabel>)(Object)filterControls("num_.*"));
        waitForSecs(0.3f);
        hideNumbers();
    }

    public void demo2u() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,0.6f,objectDict.get("num_0").frame()),-30,0.4f,"DEMO",1,0.3f);
        pointerDragNumber(0,0.2f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.65f,0.8f,objectDict.get("bottombar").frame()),-30,0.4f,"DEMO",3,0.3f);
        playAudioScene("DEMO",4,true);
        waitForSecs(0.3f);
        pointerDragNumber(1,0.45f);
        pointerDragNumber(2,0.45f);
        waitForSecs(0.4f);
        thePointer.hide();
        startScene();
    }

    public void demoFin2u() throws Exception
    {
        waitForSecs(0.2f);
        hideNumbers();
    }

    public void demo2x() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,0.6f,objectDict.get("num_0").frame()),-30,0.4f,"DEMO",1,0.3f);
        pointerDragNumber(0,0.2f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.3f);
        pointerDragNumber(9,0.45f);
        playAudioScene("DEMO",3,true);
        waitForSecs(0.4f);
        thePointer.hide();
        startScene();
    }

    public void demo2y() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        pointerPoint(OBUtils.getIntValue(eventAttributes.get("hilitenum")),Color.RED);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin2y() throws Exception
    {
        flashArrow(true);
        resetNumbers((List<OBLabel>)(Object)filterControls("num_.*"));
    }

    public void demo2za() throws Exception
    {
        loadPointer(POINTER_LEFT);
        pointerPoint(OBUtils.getIntValue(eventAttributes.get("hilitenum")),Color.RED);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.5f);
        thePointer.hide();
        startScene();
    }

    public void demoFin2za() throws Exception
    {
        flashArrow(false);
        resetNumbers((List<OBLabel>)(Object)filterControls("num_.*"));
        waitForSecs(0.3f);
        hideNumbers();
    }

    public void demo2zd() throws Exception
    {
        loadPointer(POINTER_LEFT);
        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1f,0.6f,objectDict.get("num_10").frame()),-30,0.4f,"DEMO",1,0.3f);
        pointerDragNumber(10,0.2f);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.4f);
        thePointer.hide();
        startScene();
    }
}
