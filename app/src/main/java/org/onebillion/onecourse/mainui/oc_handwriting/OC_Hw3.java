package org.onebillion.onecourse.mainui.oc_handwriting;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.glstuff.ColorShaderProgram;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 10/01/2018.
 */

public class OC_Hw3 extends OC_Hw
{
    OBGroup exampleGroup, guideGroup, demoGuideGroup;
    OBControl guideMask;
    List<OBGroup> menuItems;
    int currentPhase;
    OBGroup currentMenuItem;
    int highlightColour;
    boolean animateMenuItems;
    boolean numbersMode;


    public void prepare()
    {
        super.prepare();
        eraser.show();
        audioScenes = new ArrayMap<>();
        String mode = parameters.get("mode");
        boolean dualLetterMode = mode.equals("capitals");
        numbersMode = mode.equals("numbers");
        loadAudioXML(getConfigPath(numbersMode ? "hw3baudio.xml" :  "hw3aaudio.xml"));
        if(numbersMode)
        {
            Map<String,Object> ed = loadXML(getConfigPath(String.format("%s.xml","tracingnumbers")));
            eventsDict.putAll(ed);
        }

        menuItems = new ArrayList<>();

        events = new ArrayList<>();
        events.add("intro");

        highlightColour = OBUtils.colorFromRGBString(eventAttributes.get("colour_highlight"));
        String[] eventTargets = parameters.get("targets").split(";");
        OBControl lettersArea = objectDict.get("extra_area");
        float minScale = -1;
        for(int i =0; i< eventTargets.length; i++)
        {
            String eventTarget = eventTargets[i];
            String[] arr = eventTarget.split(",");
            if(arr.length > 0)
            {
                OBPhoneme pho = null;
                if(!numbersMode)
                {
                    pho = componentDict.get(arr[0]);
                }
                else
                {
                    String audioId = String.format("n_%s",arr[0]);
                    pho = new OBPhoneme(arr[0],audioId);
                }

                if(pho != null)
                {
                    String text = pho.text;
                    if(dualLetterMode)
                    {
                        text = String.format("%s %s", text.toUpperCase(), text);
                    }
                    else if(arr.length > 1)
                    {
                        boolean capitalise = arr[1].equals("c");
                        if(capitalise)
                        {
                            String cap = text.substring(0,1).toUpperCase();
                            if(cap.length()>1)
                                text = cap + text.substring(1, text.length()+1);
                            else
                                text = cap;
                        }
                    }

                    OBGroup group = loadPaths(text, Color.BLACK,applyGraphicScale(20),false);
                    group.setProperty("audio",pho.audio());
                    group.setProperty("play_separate",dualLetterMode);
                    group.setProperty("text",text);
                    group.setProperty("completed",false);
                    group.setScale(lettersArea.height()*0.5f/group.height());
                    group.setPosition(OB_Maths.locationForRect(0.5f,0.5f,lettersArea.frame()));
                    if(minScale < 0)
                    {
                        minScale = group.scale();
                    }
                    else if(group.scale() < minScale)
                    {
                        minScale = group.scale();
                    }
                    attachControl(group);
                    group.hide();
                    menuItems.add(group);

                    if(i == eventTargets.length-1)
                    {
                        events.add("menu_last");
                    }
                    else
                    {
                        String eventName = String.format("menu_%d",i+1);
                        if(audioScenes.get(eventName) != null)
                        {
                            events.add(eventName);
                        }
                        else
                        {
                            events.add("menu_default");
                        }
                    }
                }
            }
        }

        float fullWidth = 0;
        for(OBGroup gr : menuItems)
        {
            gr.setScale(minScale);
            fullWidth += gr.width();
        }

        float grDist =(lettersArea.width() - fullWidth) *1.0f/(menuItems.size() +1);
        float lastRight = lettersArea.left();
        for(int i=0; i<menuItems.size(); i++)
        {
            OBGroup curGr = menuItems.get(i);
            PointF pt = OBMisc.copyPoint(curGr.objectDict.get("xbox1").getWorldPosition());
            float dist =  pt.y - curGr.position().y;
            PointF loc = OBMisc.copyPoint(curGr.position());
            loc.y -= dist;
            curGr.setPosition(loc);
            curGr.setLeft(lastRight + grDist);
            lastRight = curGr.right();
        }
        eraser.hide();
        animateMenuItems = false;
        preparePaintForErasing();
    }


    public void doMainXX() throws Exception
    {
        startMenuScene();
    }

    public void  setSceneXX(String scene)
    {
        currentPhase = 0;
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            playAudioQueuedScene("finale", "DEMO", true);

            MainActivity.mainActivity.fatController.completeEvent(this);
            //displayAward();
            //exitEvent();
        }
        catch(Exception exception)
        {
        }
    }


    public void arrowButtonClick() throws Exception
    {
        playSfxAudio("arrow",true);
        waitForSecs(0.3f);
        hideArrowButton();
        if(currentPhase < 3)
        {
            animateGuideWipe(guideGroup);
            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForPhase("FINAL"),300),true);
            waitForSecs(1.5f);
        }
        else
        {
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForPhase("FINAL"),300),true);
            waitForSecs(0.3f);
        }

        if(!(currentPhase >= 5 && eventIndex >= events.size()-1))
            cleanUpBoard(currentPhase < 3 || currentPhase >= 5 ,currentPhase >= 5,null);

        waitForSecs(0.4f);
        nextPhase();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demointro();
            }
        });

    }


    public float drawPathWidth()
    {
        return applyGraphicScale(20) * guideGroup.scale();
    }

    public float arrowButtonTimeout()
    {
        return 1.5f;
    }



    public void touchDownAtPoint(final PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl cont = finger(0,1,(List<OBControl>)(Object)menuItems,pt);

            if(cont != null && !(boolean)cont.propertyValue("completed"))
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        setStatus(STATUS_BUSY);
                        animateMenuItems = false;
                        playSfxAudio("choose",true);
                        setupEventForPathGroup((OBGroup)cont);
                    }
                });
            }
        }
        else
        {
            super.touchDownAtPoint(pt,v);
        }
    }

    public void setupEventForPathGroup(OBGroup pathGroup) throws Exception
    {
        currentMenuItem = pathGroup;

        String text = (String)currentMenuItem.propertyValue("text");
        currentMenuItem.setProperty("completed",true);

        lockScreen();
        colourPaths(highlightColour,currentMenuItem);
        setupEventLetterForText(text);
        unlockScreen();
        startPhase();
    }

    public void animateMenuItemsFlash() throws Exception
    {
        animateMenuItems = true;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                while(animateMenuItems)
                {
                    for(OBGroup menuItem : menuItems)
                    {
                        if(!(boolean)menuItem.propertyValue("completed"))
                        {
                            lockScreen();
                            colourPaths(Color.RED,menuItem);
                            unlockScreen();

                            waitForSecs(0.2f);
                            if(!animateMenuItems)
                                break;
                            lockScreen();
                            colourPaths(Color.BLACK,menuItem);
                            unlockScreen();
                            waitForSecs(0.2f);
                            if(!animateMenuItems)
                                break;
                        }
                    }
                    if(animateMenuItems)
                        waitForSecs(1f);
                }

                lockScreen();
                for(OBGroup menuItem : menuItems)
                {
                    if(!(boolean)menuItem.propertyValue("completed"))
                        colourPaths(Color.BLACK,menuItem);
                }
                unlockScreen();
            }
        });
    }


    public void colourPaths(int colour,OBGroup pathGroup)
    {
        List<OBControl> paths = pathGroup.filterMembers("Path.*",true);

        for(OBControl path : paths)
            ((OBPath)path).setStrokeColor(colour);
    }

    public void setupPhase()
    {
        initScene();

    }

    public void nextPhase() throws Exception
    {
        currentPhase++;
        if(currentPhase >= 6)
        {
            gotItRight();
            lockScreen();
            colourPaths(Color.BLACK,currentMenuItem);
            currentMenuItem.setOpacity(0.3f);
            unlockScreen();
            nextScene();
        }
        else
        {
            startPhase();
        }
    }

    public void startMenuScene() throws Exception
    {
        animateMenuItemsFlash();
        OBMisc.doSceneAudio(-1,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void startPhase() throws Exception
    {
        setupPhase();
        performSel("demo",getCurrentPhaseName());

        setReplayAudio(OBUtils.insertAudioInterval(getAudioForPhase("PROMPT.REPEAT"),300));
        long time = setStatus(STATUS_WAITING_FOR_DRAG);
        playAudioQueued(OBUtils.insertAudioInterval(getAudioForPhase("PROMPT"),300),true);

        List<String> remindAudio = getAudioForPhase("REMIND");
        if(remindAudio != null)
        {
            reprompt(time,(List<Object>)(Object)remindAudio,4);
        }
    }

    public String getCurrentPhaseName()
    {
        String prefix = currentPhase < 3 ? "trace" : "write";
        int currentCount = currentPhase < 3 ? currentPhase + 1 : currentPhase - 2;
        String scene = String.format("%s_%d_%d", prefix, eventIndex, currentCount);
        if(audioScenes.get(scene) != null)
        {
            return scene;
        }
        else
        {
            return String.format("%s_default_%d", prefix, currentCount);
        }
    }

    public List<String> getAudioForPhase(String category)
    {
        String scene = getCurrentPhaseName();
        boolean useAltAudio = !numbersMode && ((String)currentMenuItem.propertyValue("text")).length()  > 1;
        if(useAltAudio)
        {
            String altCategory = String.format("ALT.%s",category);
            List<String> audio = getAudioForScene(scene,altCategory);
            if(audio != null)
                return audio;
        }
        return getAudioForScene(scene,category);
    }



    public void setupEventLetterForText(String text)
    {
        if(exampleGroup != null)
            detachControl(exampleGroup);

        exampleGroup = loadPaths(text,Color.WHITE,applyGraphicScale(20),true);

        if(exampleGroup.width() > 0.35f*board.width())
            exampleGroup.setScale(0.35f*board.width()/exampleGroup.width());

        exampleGroup.setPosition(OB_Maths.locationForRect(0.25f,0.5f,board.frame()));
        exampleGroup.setZPosition(4);
        String firstChar = text.substring(0,1);
        setupLinesForGroup(exampleGroup,numbersMode || !firstChar.equals(firstChar.toUpperCase()));

        if(guideGroup != null)
            detachControl(guideGroup);

        guideGroup = (OBGroup)exampleGroup.copy();
        attachControl(guideGroup);
        guideGroup.setOpacity(0.3f);
        guideGroup.setPosition(OB_Maths.locationForRect(0.75f,0.5f,objectDict.get("board").frame()));
        guideGroup.hide();
        showAllStrokes(guideGroup);

        setGroupPaths(guideGroup,applyGraphicScale(15));

        guideGroup.setScreenMaskControl(boardMask);
        preparePaintForDrawing();
        setSceneXX(currentEvent());
    }


    public void cleanUpBoard(boolean withExample,boolean withGuide,OBGroup menuItem) throws Exception
    {
        lockScreen();
        cleanUpDrawing();
        if(demoGuideGroup != null)
        {
            detachControl(demoGuideGroup);
            demoGuideGroup = null;
        }
        if(withGuide)
            guideGroup.hide();
        if(withExample)
        {
            List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);
            for(OBControl path : paths)
            {
                path.hide();
                ((OBPath) path).setStrokeEnd(0);
            }
            hideLines();
        }
        hideArrowButton();
        if(menuItem != null)
            colourPaths(Color.BLACK,menuItem);
        playSfxAudio("alloff",false);
        unlockScreen();
        waitSFX();
    }

    public void demoLetterPaths() throws Exception
    {
        int index = 1;
        boolean playSeparate = (boolean)currentMenuItem.propertyValue("play_separate");
        while(exampleGroup.objectDict.get(String.format("xbox%d",index)) != null)
        {
            List<OBControl> paths = exampleGroup.filterMembers(String.format("Path%d.*",index),true);
            if(paths.size() > 0)
            {
                for(OBControl p : paths)
                {
                    animatePathDraw((OBPath)p);
                    waitForSecs(0.4f);

                }
                if(playSeparate)
                {
                    playAudio((String)currentMenuItem.propertyValue("audio"));
                    waitAudio();
                    waitForSecs(0.3f);
                }
            }
            index++;
            //paths = exampleGroup.filterMembers(String.format("Path%d.*",index),true);

        }
        if(!playSeparate)
        {
            playAudio((String)currentMenuItem.propertyValue("audio"));
            waitAudio();
        }
    }

    public void demomenu_1() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.9f,objectDict.get("extra_area") .frame()),-20,0.5f,"DEMO",0,0.3f);
        thePointer.hide();
        startMenuScene();
    }

    public void demointro() throws Exception
    {
        waitForSecs(0.8f);
        playSfxAudio("eraser",false);
        eraser.show();
        waitSFX();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        lockScreen();
        for(OBGroup gr : menuItems)
            gr.show();
        playSfxAudio("menu",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        if(OBUtils.getBooleanValue(parameters.get("demo")))
        {
            loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.5f,"DEMO2",0,0.8f);
            moveScenePointer(OB_Maths.locationForRect(0.6f,0.9f,this.bounds()),-30,0.5f,"DEMO2",1,0.3f);
            OBGroup menuItem = menuItems.get(0);
            movePointerToPoint(OB_Maths.locationForRect(0.7f,1.1f,menuItem.frame()),-40,0.5f,true);
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,menuItem.frame()),-40,0.2f,true);
            lockScreen();
            colourPaths(highlightColour,menuItem);
            setupEventLetterForText((String)menuItem.propertyValue("text"));
            currentMenuItem = menuItem;

            unlockScreen();
            playSfxAudio("choose",true);
            movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.7f,true);
            waitForSecs(0.3f);
            demoLetterPaths();
            waitForSecs(0.3f);
            showLinesAndGuide(guideGroup);
            waitForSecs(0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.8f,0.9f,objectDict.get("board") .frame()),-30,0.5f,"DEMO2",2,0.3f);
            demoPointerTracePath();
            arrowButton.show();
            moveScenePointer(OB_Maths.locationForRect(-0.8f,0.6f,arrowButton.frame()),-20,0.5f,"DEMO2",3,0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,arrowButton.frame()),0.2f,true);
            arrowButton.highlight();
            playSfxAudio("arrow",true);
            movePointerToPoint(OB_Maths.locationForRect(-0.8f,0.6f,arrowButton.frame()),0.2f,true);
            waitForSecs(0.5f);
            cleanUpBoard(true,true,menuItem);
            waitForSecs(0.3f);
            thePointer.hide();
            hideArrowButton();
            arrowButton.setOpacity(1);
            currentMenuItem = null;
            playAudioScene("DEMO2",4,true);
            waitForSecs(0.3f);
        }
        nextScene();
    }

    public void demotrace_1_1() throws Exception
    {
        demoLetterPaths();
        waitForSecs(0.3f);
        showLinesAndGuide(guideGroup);
        loadPointer(POINTER_LEFT);
        List<String> audio = getAudioForPhase("DEMO");
        movePointerToPoint(OB_Maths.locationForRect(0.75f,0.9f,objectDict.get("board") .frame()),-40,0.5f,true);
        playAudio(audio.get(0));
        waitAudio();
        waitForSecs(0.3f);
        waitSFX();
        waitForSecs(0.3f);
        arrowButton.setOpacity(0.5f);
        arrowButton.show();
        List<String> audio2 = getAudioForPhase("DEMO2");
        movePointerToPoint(OB_Maths.locationForRect(-0.2f,0.6f,arrowButton.frame()),-40,0.5f,true);
        playAudio(audio2.get(0));
        waitAudio();
        waitForSecs(0.3f);
        thePointer.hide();
        hideArrowButton();
        arrowButton.setOpacity(1);
    }

    public void demotrace_default_1() throws Exception
    {
        demotrace2();
    }

    public void demotrace_1_2() throws Exception
    {
        demotrace2();
    }

    public void demotrace_1_3() throws Exception
    {
        demotrace2();
    }

    public void demotrace_default_2() throws Exception
    {
        demotrace2();
    }

    public void demotrace_default_3() throws Exception
    {
        demotrace2();
    }

    public void demotrace2() throws Exception
    {
        playAudioQueued(OBUtils.insertAudioInterval(getAudioForPhase("DEMO"),300),true);
        waitForSecs(0.3f);
        demoLetterPaths();
        waitForSecs(0.3f);
        showLinesAndGuide(guideGroup);
    }

    public void demowrite_1_1() throws Exception
    {
        demowrite(true);
    }

    public void demowrite_default_1() throws Exception
    {
        demowrite(false);
    }

    public void demowrite(boolean pointer) throws Exception
    {
        lockScreen();
        List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);
        for(OBControl path : paths)
        {
            path.show();
            ((OBPath) path).setStrokeEnd(1);
        }
        unlockScreen();

        playSfxAudio("guideon",false);
        animateLinesOn();
        waitSFX();
        if(pointer)
        {
            loadPointer(POINTER_LEFT);
            List<String> audio = getAudioForPhase("DEMO");
            movePointerToPoint(OB_Maths.locationForRect(0.75f,0.7f,objectDict.get("board") .frame()),-40,0.5f,true);
            playAudio(audio.get(0));
            waitAudio();
            waitForSecs(0.3f);
            thePointer.hide();
        }
        if(!pointer)
        {
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForPhase("DEMO"),300),true);
            waitForSecs(0.3f);
        }
    }

    public void demoPointerTracePath() throws Exception
    {
        demoGuideGroup = (OBGroup)exampleGroup.copy();
        demoGuideGroup.setPosition( guideGroup.position());
        List<OBControl> paths = demoGuideGroup.filterMembers("Path.*",true);
        for(OBControl p : paths)
        {
            p.hide();
            ((OBPath) p).setStrokeEnd(0);
        }

        demoGuideGroup.setZPosition(5);
        attachControl(demoGuideGroup);

        for(int i=0; i<paths.size(); i++)
        {
            final OBPath p = (OBPath)paths.get(i);
            movePointerToPoint(convertPointFromControl(p.sAlongPath(0,null),p),i==0?0.5f:0.3f,true);

            OBAnim anim = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    p.setStrokeEnd(frac);
                    thePointer.setPosition(convertPointFromControl(p.sAlongPath(frac,null),p));
                }
            };

            p.show();
            OBAnimationGroup.runAnims(Collections.singletonList(anim), p.length()*4/theMoveSpeed,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
    }


}
