package com.maq.xprize.onecourse.mainui.oc_handwriting;

import android.graphics.Color;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by michal on 22/07/16.
 */
public class OC_Hw1 extends OC_Hw
{
    OBPhoneme targetPhoneme;
    OBGroup exampleGroup, guideGroup, demoGuideGroup;

    @Override
    public String sectionAudioName()
    {
        return "hw1";
    }

    public void prepare()
    {
        super.prepare();
        eraser.show();
        events = Arrays.asList("trace_1", "trace_2", "trace_3", "write_1", "write_2", "write_3");
     //   this.loadAudioXML(getConfigPath("hw1audio.xml"));

        targetPhoneme = componentDict.get(parameters.get("target"));

        exampleGroup = loadPaths(targetPhoneme.text, Color.WHITE,applyGraphicScale(20),true);

        if(exampleGroup.width() > 0.35f*board.width())
            exampleGroup.setScale(0.35f*board.width()/exampleGroup.width());

        exampleGroup.setPosition(OB_Maths.locationForRect(0.25f,0.5f,board.frame()));
        alighGroupAroundXbox(exampleGroup);
        exampleGroup.setZPosition(4);

        setupLinesForGroup(exampleGroup);

        guideGroup = (OBGroup)exampleGroup.copy();
        attachControl(guideGroup);
        showAllStrokes(guideGroup);
        guideGroup.setOpacity(0.3f);
        guideGroup.setPosition(OB_Maths.locationForRect(0.75f,0.5f,objectDict.get("board").frame()));
        guideGroup.setBottom(exampleGroup.bottom());
        guideGroup.hide();

        setGroupPaths(guideGroup,applyGraphicScale(15));

        if (targetPhoneme.text.length() > 1)
            mergeAudioScenesForPrefix("ALT");

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

            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch (Exception exception)
        {
        }
    }

    public void arrowButtonClick() throws Exception
    {
        gotItRight();
        playSfxAudio("arrow",true);
        waitForSecs(0.3f);
        hideArrowButton();
        if(currentEvent().startsWith("trace"))
        {
            animateGuideWipe(guideGroup);
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
                cleanUpBoard(currentEvent().startsWith("trace"),false);

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
                demotrace_1();
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
        if(currentEvent().startsWith("trace"))
        {
            playAudioQueuedScene("DEMO",0.3f,true);
            waitForSecs(0.3f);
            demoLetterPaths();
            waitForSecs(1f);
            showLinesAndGuide(guideGroup);
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
            {
                path.hide();
                ((OBPath) path).setStrokeEnd(0);
            }

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

    public void demotrace_1() throws Exception
    {
        presenterOpening();

        if(parameters.get("demo").equals("true"))
        {
            loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO2",0,0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.5f,0.9f,objectDict.get("board").frame()),-30,0.5f,"DEMO2",1,0.3f);
            demoLetterPaths();

            waitForSecs(0.3f);

            showLinesAndGuide(guideGroup);
            waitForSecs(0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.8f,0.9f,objectDict.get("board").frame()),-40,0.5f,"DEMO3",0,0.3f);
            demoPointerTracePath();

            arrowButton.show();
            moveScenePointer(OB_Maths.locationForRect(-0.2f,0.6f,arrowButton.frame()),-10,0.5f,"DEMO3",1,0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.6f,arrowButton.frame()),0.3f,true);
            arrowButton.highlight();
            playSfxAudio("arrow",true);
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
        showLinesAndGuide(guideGroup);
        waitForSecs(0.3f);

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

    public void demowrite_1() throws Exception
    {
        lockScreen();
        List<OBControl> paths = exampleGroup.filterMembers("Path.*",true);
        for(OBControl path : paths)
        {
            path.show();
            ((OBPath) path).setStrokeEnd(1);
        }
        unlockScreen();

        animateLinesOn();
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.75f,0.7f,objectDict.get("board").frame()),-40,0.5f,"DEMO",0,0.5f);
        thePointer.hide();
        arrowButton.hide();
        startScene();
    }


}
