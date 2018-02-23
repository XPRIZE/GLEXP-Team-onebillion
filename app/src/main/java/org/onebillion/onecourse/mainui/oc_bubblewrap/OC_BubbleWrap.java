package org.onebillion.onecourse.mainui.oc_bubblewrap;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import junit.framework.Test;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 19/01/2018.
 */

public class OC_BubbleWrap extends OC_SectionController
{
    List<List<OBGroup>> textBubbles,imgBubbles,currentPopArray;
    boolean animatePopping;
    long lastPopLoopTick;
    int  currentPhase, currentObjectId;
    OBImage screenImage, earthMap;
    List<OBLabel> textLabels;
    OBControl targetArea;

    public void prepare()
    {

        imgBubbles = new ArrayList<>();
        textBubbles = new ArrayList<>();
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        if(parameters.containsKey("image"))
            currentObjectId = OBUtils.getIntValue(parameters.get("image"));

        if(!parameters.containsKey("image") || currentObjectId < 1)
            currentObjectId = OB_Maths.randomInt(1, 51);

        /*for(int i=1; i<4; i++)
            OBAudioManager.audioManager.prepare("bubblewrap_pop",String.format("special%d",i));*/
        OBControl imageBox = objectDict.get("image_box");
        RectF fitRect = loadBubblesInBox(imageBox,10,6,imgBubbles);
        imageBox.setFrame(fitRect);
        OBControl textBox = objectDict.get("text_box");
        RectF fitRect2 = loadBubblesInBox(textBox,10,3,textBubbles);
        textBox.setFrame(fitRect2);
        loadElementsDataForId(currentObjectId);
        if(fitRect.width()/fitRect.height() < screenImage.width()/screenImage.height())
        {
            screenImage.setScale(fitRect.height()/screenImage.height());
        }
        else
        {
            screenImage.setScale(fitRect.width()/screenImage.width());
        }
        OBGroup imgGroup = new OBGroup(Arrays.asList((OBControl)screenImage));
        imgGroup.setFrame(fitRect);
        imgGroup.setMasksToBounds(true);
        imgGroup.setZPosition(1);
        attachControl(imgGroup);
        currentPhase = 1;
        currentPopArray = imgBubbles;
        targetArea = imgGroup;
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                waitForSecs(0.5f);
                showPhase1();
                startPopAnimLoop();
                setStatus(STATUS_WAITING_FOR_DRAG);
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
    }

    public void doMainXX()
    {
    }

    public void cleanUp()
    {
        super.cleanUp();
        stopPopLoop();
    }

    @Override
    public void replayAudio()
    {
        super.replayAudio();
        final long time = setStatus(status());
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                playAudio(null);
                demoFinal(false, time);
            }
        });

    }

    @Override
    public int buttonFlags()
    {
        return  OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if(status() == STATUS_WAITING_FOR_DRAG)
        {

            setStatus(STATUS_DRAGGING);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    OBGroup targ = getBubbleForPoint(pt);
                    if(targ != null)
                    {
                        popBubble((OBGroup)targ);
                    }
                }
            });

        }
    }

    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            final OBGroup targ = getBubbleForPoint(pt);
            if (targ != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        popBubble((OBGroup)targ);
                    }
                });
            }
        }
    }


    public void touchUpAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_DRAGGING)
            setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public OBGroup getBubbleForPoint(PointF pt)
    {
        OBGroup targ = null;
        for(List<OBGroup> arr : currentPopArray)
        {
            for(OBGroup bubble : arr)
            {
                if(bubble.isEnabled() && bubble.frame.contains(pt.x, pt.y))
                {
                    targ = bubble;
                    break;
                }
            }
        }
        return targ;
    }

    public RectF loadBubblesInBox(OBControl box,int columns,int rows,List<List<OBGroup>> bubblesArray)
    {
        RectF unionRect = new RectF();
        OBControl bubble = objectDict.get("bubble");
        OBGroup bubbleBg =(OBGroup)objectDict.get("bubble_bg");
        bubble.setScale(1);
        bubbleBg.setScale(1);
        bubbleBg.setScale((box.width()/columns) /bubbleBg.width());
        bubble.setScale(bubbleBg.scale());
        bubbleBg.setWidth((int)bubbleBg.width());
        bubbleBg.setHeight((int)bubbleBg.height());
        bubbleBg.setZPosition(2);
        bubble.setZPosition(3);
        float startLeft = box.left();
        float startTop = box.top();
        for(int i=0; i<rows; i++)
        {
            List<OBGroup> arr = new ArrayList<>();
            startLeft = box.left();
            for(int j=0; j<columns; j++)
            {
                OBGroup bubbleBgCopy = (OBGroup)bubbleBg.copy();
                bubbleBgCopy.setProperty("pop",false);
                OBControl bubbleCopy = bubble.copy();
                attachControl(bubbleBgCopy);
                attachControl(bubbleCopy);
                bubbleBgCopy.setLeft(startLeft-1);
                bubbleBgCopy.setZPosition(10);
                bubbleCopy.setZPosition(11);
                bubbleBgCopy.setOpacity(0.95f);
                startLeft = bubbleBgCopy.right();
                bubbleBgCopy.setTop(startTop-1);
                bubbleCopy.setPosition(bubbleBgCopy.position());
                bubbleBgCopy.setProperty("bubble",bubbleCopy);
                bubbleBgCopy.objectDict.get("colour_con_l").setAnchorPoint(new PointF(0, 0.5f));
                bubbleBgCopy.objectDict.get("colour_con_r").setAnchorPoint(new PointF(1, 0.5f));
                bubbleBgCopy.objectDict.get("colour_con_t").setAnchorPoint(new PointF(0.5f, 0));
                bubbleBgCopy.objectDict.get("colour_con_b").setAnchorPoint(new PointF(0.5f, 1));
                arr.add(bubbleBgCopy);
                if(j == columns-1)
                    startTop = bubbleBgCopy.bottom();

                unionRect.union(bubbleBgCopy.frame());
            }
            bubblesArray.add(arr);
        }
        for(int i=0; i<rows; i++)
        {
            for(int j=0; j<columns; j++)
            {
                OBGroup bubbleBg2 = bubblesArray.get(i).get(j);
                if(j > 0)
                    bubbleBg2.objectDict.get("colour_con_l").setProperty("near",bubblesArray.get(i).get(j-1).objectDict.get("colour_con_r"));
                if(j < columns-1)
                    bubbleBg2.objectDict.get("colour_con_r").setProperty("near",bubblesArray.get(i).get(j+1).objectDict.get("colour_con_l"));
                if(i > 0)
                    bubbleBg2.objectDict.get("colour_con_t").setProperty("near",bubblesArray.get(i-1).get(j).objectDict.get("colour_con_b"));
                if(i < rows-1)
                    bubbleBg2.objectDict.get("colour_con_b").setProperty("near" ,bubblesArray.get(i+1).get(j).objectDict.get("colour_con_t"));
                bubbleBg2.objectDict.get("colour_con_l").setProperty("scale_x",true);
                bubbleBg2.objectDict.get("colour_con_r").setProperty("scale_x",true);
                bubbleBg2.objectDict.get("colour_con_t").setProperty("scale_x",false);
                bubbleBg2.objectDict.get("colour_con_b").setProperty("scale_x",false);
            }
        }
        return unionRect;
    }

    public void popBubble(OBGroup bubbleBg) throws Exception
    {
        OBControl con = (OBControl)bubbleBg.propertyValue("bubble");
        con.hide();
        bubbleBg.disable();
        bubbleBg.objectDict.get("colour_bg").hide();
        bubbleBg.setProperty("pop",true);
        playSFX("bubblewrap_pop");
    }

    public void startPopAnimLoop() throws Exception
    {
        animatePopping = true;
        OBAnimationGroup ag = new OBAnimationGroup();
        OBAnim anim = new OBAnimBlock()
        {

            @Override
            public void runAnimBlock(float frac)
            {
                if(animatePopping)
                    popLoop();
            }
        };
        lastPopLoopTick = System.currentTimeMillis();
        ag.applyAnimations(Arrays.asList(anim),100,false,OBAnim.ANIM_LINEAR,-1,null,this);
        registerAnimationGroup(ag,"poploop");
    }

    public void stopPopLoop()
    {
        animatePopping = false;
        deregisterAnimationGroupWithName("poploop");
    }

    public void popLoop()
    {
        float TICK_VALUE = 0.0025f;
        float POP_SPEED = 0.01f;
        long currentTime = System.currentTimeMillis();
        float frameFrac =(currentTime - lastPopLoopTick) /(TICK_VALUE*1000.0f);
        boolean popComplete = true;
        for(List<OBGroup> arr : currentPopArray)
        {
            for(OBGroup bubbleBg : arr)
            {
                if((boolean)bubbleBg.propertyValue("pop"))
                {
                    for(OBControl con : bubbleBg.filterMembers("colour_con_*."))
                    {
                        float destScale = 0.1f;
                        if(con.propertyValue("dest_scale") == null)
                        {
                            con.setProperty("dest_scale",destScale);
                        }
                        destScale = (float)con.propertyValue("dest_scale");
                        boolean scaleX = (boolean) con.propertyValue("scale_x");
                        float currentScale = 0;
                        if(scaleX)
                            currentScale = con.scaleX;
                        else
                            currentScale = con.scaleY;
                        if(currentScale > destScale)
                        {
                            currentScale -= frameFrac * POP_SPEED;
                            if(currentScale < destScale)
                            {
                                currentScale = destScale;
                                if(currentScale > 0)
                                {
                                    OBControl near = (OBControl)con.propertyValue("near");
                                    if(near != null)
                                    {
                                        if((boolean)near.parent.propertyValue("pop"))
                                        {
                                            near.setProperty("dest_scale",0.0f);
                                            con.setProperty("dest_scale",0.0f);
                                        }
                                    }
                                    else
                                    {
                                        con.setProperty("dest_scale",0.0f);
                                    }
                                }
                            }
                            if(scaleX)
                                con.setScaleX(currentScale);
                            else
                                con.setScaleY(currentScale);
                            if(currentScale == 0)
                                con.hide();

                            if(currentScale != 0)
                                popComplete = false;
                        }
                    }
                    bubbleBg.setNeedsRetexture();
                }
                else
                {
                    popComplete = false;
                }
            }
        }
        if(popComplete)
        {
            animatePopping = false;
            lockScreen();
            for(List<OBGroup> cons : currentPopArray)
            {
                for (OBGroup con : cons)
                    con.hide();
            }
            unlockScreen();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    nextPhase();
                }
            });
        }

        lastPopLoopTick = currentTime;
        if(this._aborting || !animatePopping)
            deregisterAnimationGroupWithName("poploop");

    }

    public void loadElementsDataForId(int dataId)
    {
        OBXMLNode elementNode = null;
        try
        {
            OBXMLManager xmlman = new OBXMLManager();
            List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(getLocalPath("bubblewrapdata.xml")));

            OBXMLNode rootNode = xl.get(0);

            for (OBXMLNode currentNode : rootNode.childrenOfType("element"))
            {
                long elementId = currentNode.attributeIntValue("id");
                if (elementId == dataId)
                {
                    elementNode = currentNode;
                    break;
                }
            }
        }
        catch (Exception e)
        {

        }
        String pinLocString = elementNode.attributeStringValue("pinloc");
        String[] locs = pinLocString.split(",");
        PointF pinLoc = new PointF(Float.valueOf(locs[0]) , Float.valueOf(locs[1]));
        OBXMLNode titleNode =  elementNode.childrenOfType("title") .get(0);
        OBXMLNode locationNode =  elementNode.childrenOfType("location").get(0);
        OBXMLNode descNode =  elementNode.childrenOfType("description").get(0);
        OBControl imageBox = objectDict.get("image_box");
        screenImage = loadImageWithName(elementNode.attributeStringValue("image"),new PointF(0.5f, 0.5f),imageBox.frame());
        screenImage.hide();
        OBControl textBox = objectDict.get("text_box");
        float fitWidth = textBox.width()*0.7f;
        float fitHeight = textBox.height()*0.45f;
        textLabels = new ArrayList<>();
        float textLeftDist = 0.015f * textBox.width();
        OBLabel titleLabel = new OBLabel(titleNode.contents,OBUtils.StandardReadingFontOfSize(50));
        if(titleLabel.width() > fitWidth)
            titleLabel.setScale(fitWidth/titleLabel.width());
        titleLabel.setPosition(OB_Maths.locationForRect(0.5f,0.12f,textBox.frame()));
        titleLabel.setLeft(textBox.left() + textLeftDist);
        titleLabel.setZPosition(5);
        titleLabel.setColour(Color.BLACK);
        titleLabel.hide();
        attachControl(titleLabel);
        textLabels.add(titleLabel);
        OBLabel locationLabel = new OBLabel(locationNode.contents,OBUtils.StandardReadingFontOfSize(titleLabel.scale()*38));
        if(locationLabel.width() > fitWidth)
            locationLabel.setScale(fitWidth/locationLabel.width());

        locationLabel.setPosition(OB_Maths.locationForRect(0.5f,0.35f,textBox.frame()));
        locationLabel.setLeft(textBox.left() + textLeftDist);
        locationLabel.setZPosition(5);
        locationLabel.setColour(Color.BLACK);
        locationLabel.hide();
        locationLabel.setTop(titleLabel.bottom());
        attachControl(locationLabel);
        textLabels.add(locationLabel);
        OBLabel descLabel = new OBLabel(descNode.contents,OBUtils.StandardReadingFontOfSize(titleLabel.scale()*locationLabel.scale()*30));
        descLabel.setMaxWidth(textBox.width()*0.7f);
        OBTextLayer textLayer = (OBTextLayer)descLabel.layer;
        textLayer.justification = OBTextLayer.JUST_LEFT;
        descLabel.sizeToBoundingBox();
        if(descLabel.height() > fitHeight)
        {
            for(int i=1; i<=6; i++)
            {
                descLabel.setFontSize(applyGraphicScale(30 - i*2));
                descLabel.sizeToBoundingBox();
                if(descLabel.height() <= fitHeight)
                    break;
            }

            if(descLabel.height() > fitHeight)
                descLabel.setScale(descLabel.scale() * fitHeight/descLabel.height());
        }

        descLabel.setPosition(OB_Maths.locationForRect(0.5f,0.7f,textBox.frame()));
        descLabel.setLeft(textBox.left() + textLeftDist);
        descLabel.setZPosition(5);
        descLabel.setColour(Color.BLACK);
        descLabel.hide();
        attachControl(descLabel);
        textLabels.add(descLabel);
        float top = textLabels.get(0).top();
        float bottom = textLabels.get(textLabels.size()-1).bottom();
        float dif = (textBox.height() - (bottom-top))/2.0f;
        float moveDist = dif - (top-textBox.top());
        for(OBLabel label: textLabels)
            label.setTop(label.top()+moveDist);

        float dist = textBox.bottom() - locationLabel.bottom();
        descLabel.setPosition(descLabel.position().x, locationLabel.bottom() + (dist/2.0f) - locationLabel.height()*0.05f);

        earthMap = loadImageWithName(elementNode.attributeStringValue("map"),new PointF(0.5f, 0.5f),imageBox.frame());
        earthMap.setScale(imageBox.height()/earthMap.height());
        earthMap.setZPosition(50);
        earthMap.hide();
        attachControl(earthMap);
        OBControl pin = objectDict.get("earthmap_pin");
        pin.setZPosition(51);
        pin.setAnchorPoint(new PointF(0.5f, 1));
        pin.setPosition(OB_Maths.locationForRect(pinLoc, earthMap.frame));

    }

    public void showPhase1() throws Exception
    {
        lockScreen();
        screenImage.show();
        showBubblesForArray(imgBubbles);
        playSFX("snap");
        unlockScreen();
        waitSFX();
    }

    public void showPhase2() throws Exception
    {
        lockScreen();
        objectDict.get("text_box").show();
        for(OBLabel label : textLabels)
            label.show();
        showBubblesForArray(textBubbles);
        playSFX("snap");
        unlockScreen();
        waitSFX();
    }

    public void showBubblesForArray(List<List<OBGroup>> bubblesArray)
    {
        for(List<OBGroup> arr : bubblesArray)
        {
            for(OBGroup bubbleBg : arr)
            {
                OBControl bubble = (OBControl)bubbleBg.propertyValue("bubble");
                bubble.show();
                bubbleBg.show();
            }
        }
    }

    public void nextPhase() throws Exception
    {
        currentPhase++;
        if(currentPhase == 2)
        {
            waitForSecs(0.5f);
            currentPopArray = textBubbles;
            targetArea = objectDict.get("text_box");
            showPhase2();
            startPopAnimLoop();
            setStatus(STATUS_WAITING_FOR_DRAG);
        }
        else
        {
            setStatus(STATUS_BUSY);
            waitForSecs(0.5f);
            demoFinal(true,-1);
            setStatus(STATUS_AWAITING_CLICK);
        }
    }

    public void animateEarthAndPin() throws Exception
    {
        final OBControl pin = objectDict.get("earthmap_pin");
        pin.setProperty("start_scale",pin.scale());
        final float bottom = pin.bottom();
        pin.setBottom(0);
        pin.setScale(1.5f);
        lockScreen();
        earthMap.show();
        pin.show();
        playSFX("stapler");

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        final float bounce1 = bottom*0.15f;
        final float bounce2 = bottom*0.1f;
        final float bounce3 = bottom*0.05f;
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {


                float currentBottom = 0;
                if(frac < 0.5)
                {
                    float curFrac = OB_Maths.easein(frac/0.5f);
                    currentBottom = curFrac * bottom;

                }
                else if(frac < 0.75)
                {
                    float curFrac = OB_Maths.bezef((frac-0.5f) /0.25f);
                    curFrac = 2*curFrac - 1;
                    currentBottom =(bottom-bounce1) + Math.abs(curFrac) * bounce1;

                }
                else if(frac < 0.9)
                {
                    float curFrac = OB_Maths.bezef((frac-0.75f) /0.15f);
                    curFrac = 2*curFrac - 1;
                    currentBottom =(bottom-bounce2) + Math.abs(curFrac) * bounce2;

                }
                else
                {
                    float curFrac = OB_Maths.bezef((frac-0.9f) /0.1f);
                    curFrac = 2*curFrac - 1;
                    currentBottom =(bottom-bounce3) + Math.abs(curFrac) * bounce3;

                }
                pin.setBottom(currentBottom);

            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(blockAnim),1,true,OBAnim.ANIM_LINEAR,this);
        waitForSecs(0.3f);
    }

    public void animateEarthAndPinShrink() throws Exception
    {
        final OBControl pin = objectDict.get("earthmap_pin");
        final PointF pinLoc = OB_Maths.relativePointInRectForLocation(pin.position(), earthMap.frame());
        float startScale =  (float)pin.propertyValue("start_scale");
        OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                pin.setPosition(OB_Maths.locationForRect(pinLoc, earthMap.frame));
            }
        };
        float targetScale = objectDict.get("text_box").width() *0.25f /(earthMap.height()/earthMap.scale());
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(targetScale,earthMap) ,
                OBAnim.scaleAnim(startScale,pin) ,
                OBAnim.moveAnim(OB_Maths.locationForRect(0.865f,0.5f,objectDict.get("text_box") .frame()) ,earthMap) ,
                blockAnim),1.5,true
                ,OBAnim.ANIM_EASE_IN_EASE_OUT,this);

    }

    public void demoFinal(boolean first, long time) throws Exception
    {
        if(!first)
        {
            for(int i=0; i<textLabels.size(); i++)
            {
                OBLabel label = textLabels.get(i);
                label.setColour(Color.BLACK);
            }
        }
        for(int i=0; i<textLabels.size(); i++)
        {
            if(!first && statusChanged(time))
                break;
            if(first && i == 1)
                animateEarthAndPin();
            OBLabel label = textLabels.get(i);
            label.setColour(Color.RED);
            playAudio(String.format("%d_%d",currentObjectId,i+1));
            waitAudio();
            if(!first && statusChanged(time))
                break;
            waitForSecs(0.3f);
            if(!first && statusChanged(time))
                break;
            if(first && i == 1)
                animateEarthAndPinShrink();
            label.setColour(Color.BLACK);
            waitForSecs(0.5f);
            if(!first && statusChanged(time))
                break;
        }

        if(first)
        {
            lockScreen();
            textLabels.get(0).hide();
            textLabels.get(0).show();
            MainViewController().topRightButton.show();
            MainViewController().topRightButton.setOpacity(1);
            unlockScreen();
        }
    }
}
