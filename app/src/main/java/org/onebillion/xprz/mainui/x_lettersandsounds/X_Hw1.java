package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimBlock;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 22/07/16.
 */
public class X_Hw1 extends X_Hw
{
    OBPhoneme targetPhoneme;
    OBGroup exampleGroup, guideGroup, demoGuideGroup;

    public void prepare()
    {
        super.prepare();
        eraser.show();
        events = Arrays.asList("a1", "a2", "a3", "b1", "b2", "b3");
        this.loadAudioXML(getConfigPath("hw1audio.xml"));

        targetPhoneme = componentDict.get(parameters.get("target"));

        exampleGroup = loadPaths(targetPhoneme.text, Color.WHITE,applyGraphicScale(20),true);

        if(exampleGroup.width() > 0.35f*board.width())
            exampleGroup.setScale(0.35f*board.width()/exampleGroup.width());

        exampleGroup.setPosition(OB_Maths.locationForRect(0.25f,0.5f,board.frame()));
        exampleGroup.setZPosition(4);

        setupLinesFor(exampleGroup);

        guideGroup = (OBGroup)exampleGroup.copy();
        attachControl(guideGroup);
        showAllStrokes(guideGroup);
        guideGroup.setOpacity(0.3f);
        guideGroup.setPosition(OB_Maths.locationForRect(0.75f,0.5f,objectDict.get("board").frame()));
        guideGroup.hide();

        setGroupPaths(guideGroup,applyGraphicScale(15));

        if (targetPhoneme.text.length() > 1)
            mergeAudioScenesForPrefix("ALT");


        guideGroup.setScreenMaskControl(boardMask);
        lineTop.setScreenMaskControl(boardMask);
        lineBottom.setScreenMaskControl(boardMask);

        preparePaintForDrawing();
        preparePaintForErasing();


        setSceneXX(currentEvent());
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);

            playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene("finale", "DEMO"),300),true);
            for(int i=0; i<3; i++)
            {
                highlightPathsForGroup(exampleGroup,true);
                targetPhoneme.playAudio(this,true);
                highlightPathsForGroup(exampleGroup,false);
                waitForSecs(0.3f);
            }


            //FatController().completeEvent();
            displayAward();
            exitEvent();
        }
        catch (Exception exception)
        {
        }
    }

    public void arrowButtonClick() throws Exception
    {
        hideArrowButton();
        if(currentEvent().startsWith("a"))
        {
            playSfxAudio("guidewipe",false);
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("width", 1, boardMask),
                    new OBAnimBlock()
                    {
                        @Override
                        public void runAnimBlock(float frac)
                        {
                            lineBottom.invalidate();
                            lineTop.invalidate();
                            guideGroup.invalidate();
                        }
                    }),0.5f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            waitSFX();
            hideLines();
            guideGroup.hide();
            resetGuideMask();
            waitForSecs(0.3f);
            playAudioQueuedScene("FINAL",0.3f,true);

            waitForSecs(1.5f);
        }
        else
        {
            playAudioQueuedScene("FINAL",0.3f,true);
            waitForSecs(0.3f);
        }

        if(!currentEvent().equalsIgnoreCase(events.get(events.size()-1)))
                cleanUpBoard(currentEvent().startsWith("b")?false:true,false);

        waitForSecs(0.4f);
        nextScene();
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                demoa1();
            }
        });

    }

    public void setSceneXX(String sceneXX)
    {
        initScene();
        boardMask.setWidth((float) boardMask.settings.get("startWidth"));
    }

    public void doMainXX() throws Exception
    {
        if(currentEvent().startsWith("a"))
        {
            playAudioQueuedScene("DEMO",0.3f,true);
            waitForSecs(0.3f);
            demoLetterPaths();
            waitForSecs(1f);
            animateLinesOn();
            waitForSecs(0.7f);
            showGuide();
        }

        startScene();
    }

    public float drawPathWidth()
    {
        return applyGraphicScale(20)*guideGroup.scale();
    }

    public float arrowButtonTimeout()
    {
        return 1.5f;
    }

    public void showGuide()
    {
        if(guideGroup.hidden())
        {
            guideGroup.show();

        }
    }

    public void cleanUpBoard(boolean withExample,boolean withDemo) throws Exception
    {

        lockScreen();
        cleanUpDrawing();
        if(withDemo)
        {
            demoGuideGroup.hide();
            guideGroup.hide();
        }


        if(withExample)
        {
            List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);
            for(OBControl path : paths)
                ((OBPath)path).setStrokeEnd(0);

            hideLines();
        }
        hideArrowButton();

        if(!withDemo)
            playSfxAudio("alloff",false);

        unlockScreen();
        if(!withDemo)
            waitSFX();

    }


    public void demoLetterPaths() throws Exception
    {
        List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);
        for (OBControl p : paths)
        {
            animatePathDraw((OBPath)p);
            waitForSecs(0.4f);
        }
        targetPhoneme.playAudio(this,true);
    }

    public void resetLettersPaths()
    {
        List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);

        lockScreen();
        for(OBControl path : paths)
            ((OBPath)path).setStrokeEnd(0);

        guideGroup.hide();
        unlockScreen();
    }

    public void demoa1() throws Exception
    {
        presenterOpening();

        if(parameters.get("demo").equals("true"))
        {
            loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO2",0,0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.5f,0.9f,objectDict.get("board").frame()),-30,0.5f,"DEMO2",1,0.3f);
            demoLetterPaths();

            waitForSecs(0.3f);

            animateLinesOn();
            waitForSecs(0.7f);
            showGuide();
            waitForSecs(0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.8f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO3",0,0.3f);
            demoPointerTracePath();

            arrowButton.show();
            moveScenePointer(OB_Maths.locationForRect(-0.2f,0.6f,arrowButton.frame()),-10,0.5f,"DEMO3",1,0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,arrowButton.frame()),0.3f,true);
            arrowButton.highlight();
            waitForSecs(0.3f);
            cleanUpBoard(true,true);
            movePointerToPoint(OB_Maths.locationForRect(-0.2f,0.6f,arrowButton.frame()),0.3f,true);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO3",2,0.3f);

        }
        else
        {
            loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO4",0,0.3f);
        }

        demoLetterPaths();
        waitForSecs(0.3f);
        animateLinesOn();
        waitForSecs(0.7f);
        showGuide();

        moveScenePointer(OB_Maths.locationForRect(0.75f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO5",0,0.3f);
        waitSFX();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1f,objectDict.get("eraser").frame()),-30,0.5f,"DEMO6",0,0.3f);
        arrowButton.setOpacity(0.5f);
        arrowButton.show();
        moveScenePointer(OB_Maths.locationForRect(-0.2f,0.6f,arrowButton.frame()),-10,0.5f,"DEMO6",1,0.5f);
        thePointer.hide();
        hideArrowButton();
        arrowButton.setOpacity(1);
        startScene();
    }

    public void demoPointerTracePath() throws Exception
    {
        demoGuideGroup = (OBGroup)exampleGroup.copy();
        demoGuideGroup.setPosition( guideGroup.position());
        List<OBControl> paths = demoGuideGroup.filterMembers("Path.*",true);
        for(OBControl p : paths)
            ((OBPath)p).setStrokeEnd(0);

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


            OBAnimationGroup.runAnims(Collections.singletonList(anim), p.length()*4/theMoveSpeed,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
    }

    public void demob1() throws Exception
    {
        lockScreen();
        List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);
        for(OBControl path : paths)
            ((OBPath)path).setStrokeEnd(1);

        unlockScreen();
        animateLinesOn();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.75f,0.7f,objectDict.get("board").frame()),-40,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        arrowButton.hide();
        startScene();
    }


}
