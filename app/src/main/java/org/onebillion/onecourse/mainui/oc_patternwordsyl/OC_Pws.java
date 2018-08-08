package org.onebillion.onecourse.mainui.oc_patternwordsyl;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.io.WriteAbortedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 15/01/2018.
 */

public class OC_Pws extends OC_SectionController
{
    final int MODE_MATCH = 0,
            MODE_FIND = 1,
            MODE_BAG = 2;

    int currentMode, targetIndex;
    boolean boxTouchMode, showDemo;


    public Map<String,OBPhoneme> componentDict;
    public List<OBLabel> screenLabels;
    public List<OBLabel> targetLabels;
    public Map<String,Integer> eventColours;
    public Map<String,OBLabel> targetLabelsDict;
    public OC_WordBag wordBag;
    public boolean animateWobble;


    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        showDemo = OBUtils.getBooleanValue(parameters.get("demo"));
        eventColours = new ArrayMap<>();
        eventColours = OBMisc.loadEventColours(this);
        componentDict = OBUtils.LoadWordComponentsXML(true);
        loadEvent(eventName());
        eventColours.putAll(OBMisc.loadEventColours(this));
        wordBag = new OC_WordBag((OBGroup)objectDict.get("word_bag"),this);
        setSceneXX(currentEvent());

        animateWobble = false;
        boxTouchMode = true;
        currentMode = MODE_BAG;
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demobag();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        targetIndex++;
    }

    public void setScenematch_1()
    {
        currentMode = MODE_MATCH;
        targetIndex = 0;

    }

    public void setScenefind_1()
    {
        currentMode = MODE_FIND;
        targetLabels = OBUtils.randomlySortedArray(targetLabels);
        for(OBControl con : targetLabels)
            con.enable();
        targetIndex = 0;

    }

    public void doMainXX() throws Exception
    {
        if(currentMode == MODE_MATCH)
        {
            wordBag.flyObjects(Arrays.asList((OBControl)currentTarget()),true,false,"outofbag");
        }
        startScene();
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_WAITING_FOR_DRAG)
        {
            final OBControl curTarget = currentTarget();
            if (finger(0, 1, Arrays.asList(curTarget), pt) != null)
            {
                setStatus(STATUS_BUSY);
                final OC_SectionController controller = this;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        OBMisc.prepareForDragging(curTarget, pt, controller);
                        curTarget.setProperty("anim_scale", 1.2f);
                        setStatus(STATUS_DRAGGING);
                    }
                });
            }
        }
        else if (status() == STATUS_AWAITING_CLICK)
        {
            if (currentMode == MODE_BAG)
            {
                final OBControl targ = finger(0, 1, Arrays.asList((OBControl) wordBag.control), pt);
                if (targ != null)
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            wordBag.highlight();
                            nextScene();
                        }
                    });
                }
            }
            else
            {
                final OBControl targ = finger(0, 1, (List<OBControl>)(Object)targetLabels, pt);
                if (targ != null && targ.isEnabled())
                {
                    setStatus(STATUS_BUSY);
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            playAudio(null);
                            if (targ == currentTarget())
                            {
                                targetTouchCorrect((OBLabel)targ);
                            }
                            else
                            {
                                targetTouchWrong();
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public void touchMovedToPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target != null)
            target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
    }

    @Override
    public void touchUpAtPoint(PointF pt,View v)
    {
        if (status() == STATUS_DRAGGING && target != null)
        {
            setStatus(STATUS_BUSY);
            final OBLabel curTarget = (OBLabel)target;
            target = null;
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    if(checkTargetDrop(curTarget))
                    {
                        targetDropCorrect(curTarget);
                    }
                    else
                    {
                        targetDropWrong(curTarget);
                    }
                }
            });
        }
    }

    public void targetTouchCorrect(OBLabel label) throws Exception
    {
        label.setColour(eventColours.get("highlight"));
        gotItRightBigTick(false);
        waitSFX();
        waitForSecs(0.3f);
        label.setColour(eventColours.get("normal"));
        label.disable();
        wordBag.flyObjects(Arrays.asList((OBControl)label),false,false,"backtobag");
        waitForSecs(0.3f);
        if(label == targetLabels.get(targetLabels.size()-1))
        {
            displayTick();
            waitForSecs(0.3f);
        }
        nextScene();
    }

    public void targetTouchWrong() throws Exception
    {
        gotItWrongWithSfx();
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitSFX();
        if(!statusChanged(time))
            playAudioQueued(findAudioForScene("INCORRECT",(String)currentTarget().propertyValue("audio")));
    }

    public boolean checkTargetDrop(OBLabel target)
    {
        OBLabel dropTarget = (OBLabel)target.propertyValue("target");
        return target.intersectsWith(dropTarget);
    }

    public int audioRepeatCount()
    {
        return 1;
    }

    public void targetDropCorrect(OBLabel target) throws Exception
    {
        gotItRight();
        animateWobble = false;
        snapTarget(target);
        waitSFX();
        waitForSecs(0.3f);
        for(int i=0; i<audioRepeatCount(); i++)
        {
            highlightAndAudio(target);
        }
        String imageFileName = (String)target.propertyValue("image");
        if(imageFileName != null)
        {
            waitForSecs(0.3f);
            OBControl imageBox = objectDict.get("image_box");
            OBImage image = loadImageInBox(imageFileName);
            if(image != null)
            {
                lockScreen();
                image.show();
                imageBox.show();
                unlockScreen();
                playSfxAudio("picon",true);
                waitForSecs(0.3f);
                highlightAndAudio(target);
                detachControl(image);
                lockScreen();
                detachControl(image);
                imageBox.hide();
                unlockScreen();
                playSfxAudio("picoff",true);
                waitForSecs(0.4f);
            }
            waitForSecs(0.3f);
        }
        if(target == targetLabels.get(targetLabels.size()-1))
        {
            displayTick();
            waitForSecs(0.3f);
        }
        nextScene();
    }

    public void targetDropWrong(OBLabel target) throws Exception
    {
        target.setProperty("anim_scale",1.0f);
        boolean playAudio = shouldPlayIncorrectAudio(target);
        if(playAudio)
            gotItWrongWithSfx();
        PointF dropLoc = (PointF)target.propertyValue("drop_loc");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(dropLoc,target))
                ,0.2,true,OBAnim.ANIM_EASE_IN_EASE_OUT
                ,this);
        long time = setStatus(STATUS_WAITING_FOR_DRAG);
        waitSFX();
        if(playAudio && !statusChanged(time))
        {
            playAudioQueuedScene("INCORRECT",0.3f,false);
        }
    }

    public boolean shouldPlayIncorrectAudio(OBControl target)
    {
        boolean playAudio = false;
        for(OBControl con : screenLabels)
        {
            if(!con.hidden && target.intersectsWith(con))
            {
                playAudio = true;
                break;
            }
        }
        return playAudio;
    }

    public OBImage loadImageInBox(String image) throws Exception
    {
        OBControl imageBox = objectDict.get("image_box");
        OBImage screenImage = loadImageWithName(image, new PointF(0,0),new RectF(this.bounds()),false);
        if(screenImage == null)
            return null;
        if(screenImage.width() > imageBox.width())
        {
            screenImage.setScale(imageBox.width()/screenImage.width());
        }
        screenImage.hide();
        screenImage.setPosition(imageBox.position());
        imageBox.setZPosition(1);
        screenImage.setZPosition(2);
        attachControl(screenImage);
        return screenImage;
    }

    public void snapTarget(OBControl target) throws Exception
    {
        OBControl dropTarget = (OBControl)target.propertyValue("target");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(dropTarget.position(),target),
                OBAnim.scaleAnim(dropTarget.scale(),target))
                ,0.1,true,OBAnim.ANIM_EASE_IN_EASE_OUT
                ,this);
        lockScreen();
        target.disable();
        target.setScale(dropTarget.scale());
        target.setRotation(0);
        dropTarget.hide();
        playSfxAudio("match",false);
        unlockScreen();
        waitSFX();
    }

    public String eventName()
    {
        return "";
    }

    public void highlightAndAudio(OBLabel label) throws Exception
    {
        label.setHighRange(-1,-1,eventColours.get("normal"));
        label.setColour(eventColours.get("highlight"));
        playAudio((String)label.propertyValue("audio"));
        waitAudio();
        waitForSecs(0.3f);
        label.setColour(eventColours.get("normal"));
        waitForSecs(0.3f);
    }

    public void startScene() throws Exception
    {
        if(currentMode == MODE_BAG)
        {
            OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
        }
        else if(currentMode == MODE_MATCH)
        {
            animateTargetWobble();
            OBMisc.doSceneAudio(4,setStatus(STATUS_WAITING_FOR_DRAG),this);
        }
        else if(currentMode == MODE_FIND)
        {
            playAudioQueuedScene("DEMO",0.3f,true);
            waitForSecs(0.3f);
            OBControl targ = currentTarget();
            String wordAudio = (String)targ.propertyValue("audio");
            setReplayAudio(findAudioForScene("PROMPT.REPEAT",wordAudio));
            playAudioQueued(findAudioForScene("PROMPT",wordAudio),true);
            long time = setStatus(STATUS_AWAITING_CLICK);
            reprompt(time,findAudioForScene("REMIND",wordAudio),4);
        }
    }

    public List<Object> findAudioForScene(String scene,String wordAudio) throws Exception
    {
        List<String> audios = new ArrayList<>();
        List<String> sceneAudio = getAudioForScene(currentEvent(),scene);
        if(sceneAudio != null)
            audios.addAll(sceneAudio);
        audios.add(wordAudio);
        return OBUtils.insertAudioInterval(audios, 300);
    }

    public void animateTargetWobble() throws Exception
    {
        final OBControl con = currentTarget();
        if(animateWobble)
            return;
        animateWobble = true;
        final float scaleDif = 0.05f;
        final float rotationDif = 2.5f;
        final OBAnim blockAnim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                if(con.isEnabled() && animateWobble)
                {
                    float scaleFrac = (float)(Math.sin(frac*4*Math.PI));
                    float animScaleVal = (float)con.propertyValue("anim_scale");
                    float startScale = (float)con.propertyValue("start_scale");
                    con.setScale (animScaleVal *(startScale + startScale*scaleDif*scaleFrac));
                    float rotateFrac =(float)(Math.sin(frac*2*Math.PI));
                    con.setRotation((float)(Math.toRadians(rotationDif*rotateFrac)));
                }
            }
        };

        final OC_SectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                while(con.isEnabled() && animateWobble && !controller._aborting)
                {
                    OBAnimationGroup.runAnims(Arrays.asList(blockAnim),1.5,true
                            ,OBAnim.ANIM_LINEAR,controller);
                }
            }
        } );
    }

    public OBLabel currentTarget()
    {
        return targetLabels.get(targetIndex);
    }

    public void prepareLabelsForPhonemeIds(List<String> phonemes,boolean withImages)
    {
        screenLabels = new ArrayList<>();
        targetLabels = new ArrayList<>();
        targetLabelsDict = new ArrayMap<>();
        OBFont font = OBUtils.StandardReadingFontOfSize(60);
        int index = 1;
        float minScale = 1;
        for(String phonemeid : phonemes)
        {
            if(componentDict.containsKey(phonemeid))
            {
                OBPhoneme pho = componentDict.get(phonemeid);
                OBLabel targetLabel = new OBLabel(pho.text,font);
                targetLabel.setColour(eventColours.get("normal"));
                targetLabel.enable();
                targetLabel.setProperty("audio",pho.audio());
                targetLabel.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
                if(withImages && pho.getClass() == OBWord.class)
                {
                    String imageName =((OBWord)pho).imageName;
                    if(imageName != null)
                        targetLabel.setProperty("image",imageName);

                }
                OBControl locControl = objectDict.get(String.format("loc_%d",index));
                if(locControl.width() < targetLabel.width())
                    targetLabel.setScale(locControl.width()/targetLabel.width());
                OBLabel screenLabel = (OBLabel)targetLabel.copy();
                screenLabel.setPosition(locControl.position());
                screenLabel.setColour(eventColours.get("inactive"));
                targetLabel.setProperty("target",screenLabel);

                attachControl(screenLabel);
                screenLabels.add(screenLabel);
                targetLabel.setProperty("drop_loc",OBMisc.copyPoint((PointF)objectDict.get("drop_area").position()));
                targetLabel.setProperty("anim_scale",1.0f);

                targetLabels.add(targetLabel);
                targetLabelsDict.put(phonemeid,targetLabel);
                targetLabel.setZPosition(10);
                attachControl(targetLabel);
                index++;
                if(minScale > targetLabel.scale())
                    minScale = targetLabel.scale();
            }
        }
        scaleLabels(targetLabels,minScale);
        scaleLabels(screenLabels,minScale);
        targetLabels = OBUtils.randomlySortedArray(targetLabels);
        wordBag.prepareWithObjects((List<OBControl>)(Object)targetLabels);
    }

    public void scaleLabels(List<OBLabel> labels,float scale)
    {
        for(OBLabel label : labels)
        {
            label.setScale(scale);
            label.setProperty("start_scale",scale);
        }
    }

    public void loadEventList(boolean withPattern, boolean withFind)
    {
        events = new ArrayList<>();
        events.add("bag");
        List<String> matchEvents = new ArrayList<>();
        List<String> findEvents = new ArrayList<>();
        for(int i=0; i<targetLabels.size(); i++)
        {
            OBControl con = targetLabels.get(i);
            String matchEvent = String.format("match_%d",i+1);
            if(audioScenes.get(matchEvent) != null)
                matchEvents.add(matchEvent);
            else
                matchEvents.add("match_default");
            if(withFind)
            {
                String findEvent = String.format("find_%d",i+1);
                if(audioScenes.get(findEvent) != null)
                    findEvents.add(findEvent);
                else
                    findEvents.add("find_default");
            }
        }
        events.addAll(matchEvents);
        if(withPattern)
            events.add("pattern");
        if(withFind)
            events.addAll(findEvents);
    }


    public void demobag() throws Exception
    {
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.5f,"DEMO",0,0.3f);
        PointF loc = OBMisc.copyPoint(wordBag.control.position());
        wordBag.control.setRight(0);
        wordBag.control.show();
        playSfxAudio("bagon",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc,wordBag.control))                     ,0.5,true,OBAnim.ANIM_EASE_OUT,this);
        playSFX(null);
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.6f,wordBag.control.frame()),-30,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
        startScene();

    }
    public void demomatch_1() throws Exception
    {
        wordBag.animateFillWithObjects((List<OBControl>)(Object)targetLabels,false,"bagshake");
        wordBag.lowlight();
        waitForSecs(0.3f);
        wordBag.flyObjects(Arrays.asList((OBControl)currentTarget()),true,false,"outofbag");
        animateTargetWobble();
        waitForSecs(0.3f);
        loadPointer(POINTER_LEFT);
        if(showDemo)
        {
            moveScenePointer(OB_Maths.locationForRect(0.7f, 0.9f, this.bounds()), -20, 0.5f, "DEMO", 0, 0.3f);

            OBLabel curTarget = currentTarget();
            movePointerToPoint(curTarget.position(), -30, 0.3f, true);
            curTarget.setProperty("anim_scale", 1.2f);
            OBLabel dropTarget = (OBLabel) curTarget.propertyValue("target");
            OBMisc.moveControlWithAttached(curTarget, Arrays.asList(thePointer), demomatchDropTargetLoc(curTarget), 0.5f, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            animateWobble = false;
            snapTarget(curTarget);
            movePointerToPoint(OB_Maths.locationForRect(0.9f, 0.9f, this.bounds()), -30, 0.5f,
                    true);
            if (demomatchPlayAudio())
            {
                waitForSecs(0.3f);
                highlightAndAudio(curTarget);
                waitForSecs(0.3f);
            } else
            {
                waitForSecs(1f);
            }
            lockScreen();
            curTarget.setProperty("anim_scale", 1.0f);
            curTarget.setHighRange(-1, -1, eventColours.get("normal"));
            curTarget.setColour(eventColours.get("normal"));
            curTarget.enable();
            if (demomatchShowDropTarget())
                dropTarget.show();
            else
                dropTarget.hide();
            unlockScreen();
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim((PointF) curTarget.propertyValue("drop_loc"), curTarget))

                    , 0.3f, true, OBAnim.ANIM_EASE_OUT, this);
            playAudioScene("DEMO", 1, true);
            waitForSecs(0.3f);
        }
        moveScenePointer(OB_Maths.locationForRect(0.6f,0.9f,this.bounds()),-20,0.5f,"DEMO2",0,0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        startScene();
    }

    public void demopattern() throws Exception
    {
        nextScene();
    }

    public boolean demomatchShowDropTarget() throws Exception
    {
        return true;
    }

    public boolean demomatchPlayAudio() throws Exception
    {
        return false;
    }

    public PointF demomatchDropTargetLoc(OBLabel target)
    {
        OBControl dropTarget = (OBControl)target.propertyValue("target");
        return OBMisc.copyPoint(dropTarget.position());
    }



}
