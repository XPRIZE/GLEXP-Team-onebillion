package com.maq.xprize.onecourse.hindi.mainui.oc_handwriting;

import android.graphics.Color;
import android.graphics.PointF;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimBlock;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OBWord;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 26/07/16.
 */
public class OC_Hw2 extends OC_Hw
{
    OBWord currentWord;
    OBGroup guideGroup;
    OBControl picture, picturebg, picturepin;
    List<OBWord> words;

    @Override
    public String sectionAudioName()
    {
        return "hw2";
    }

    public void prepare()
    {
        super.prepare();
        eraser.show();
        events = new ArrayList<>();
        picturepin = objectDict.get("picture_pin");
        picturebg = objectDict.get("picture_bg");

        picturepin.setZPosition(40);
        picturebg.setZPosition(38);

        words = new ArrayList<>();
        String[] wordIds = parameters.get("words").split(",");
        int index = 1;
        for(int i=0; i<wordIds.length; i++)
        {
            if(componentDict.get(wordIds[i]) != null)
            {
                words.add((OBWord)componentDict.get(wordIds[i]));
                String eventName = String.format("trace_%d",index);
                if(index == wordIds.length)
                {
                    events.add("trace_last");
                }
                else if(audioScenes.get(eventName) != null)
                {
                    events.add(eventName);
                }
                else
                {
                    events.add("trace_default");
                }
                index++;
            }
        }


        setSceneXX(currentEvent());

        preparePaintForErasing();
    }


    public void arrowButtonClick() throws Exception
    {
        gotItRight();
        playSfxAudio("arrow", true);
        waitForSecs(0.3f);
        hideArrowButton();
        animateGuideWipe(guideGroup);
        waitForSecs(0.3f);
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(3f);

        if(currentEvent() != events.get(events.size()-1))
            cleanUpBoard();

        waitForSecs(0.5f);
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

    public void setSceneXX(String scene)
    {
        initScene();
        currentWord = words.get(eventIndex);

        if(guideGroup != null)
            detachControl(guideGroup);

        guideGroup = loadPaths(currentWord.text, Color.WHITE, applyGraphicScale(20), false);

        if(guideGroup.height() > 0.72f*board.height())
            guideGroup.setScale(0.72f*board.height()/guideGroup.height());

        if(guideGroup.width() > 0.9f*board.width())
            guideGroup.setScale(guideGroup.scale() * 0.9f*board.width()/guideGroup.width());

        guideGroup.setPosition(OB_Maths.locationForRect(0.5f,0.5f,board.frame()));
        alighGroupAroundXbox(guideGroup);
        guideGroup.setZPosition(4);


        setupLinesForGroup(guideGroup);

        guideGroup.setOpacity(0.3F);

        setGroupPaths(guideGroup,applyGraphicScale(15));

        picture = loadImageWithName(currentWord.ImageFileName(), new PointF(0,0), picturebg.bounds());
        picture.setScale(picturebg.bounds.width()/(picture.width()+applyGraphicScale(4)));
        picture.setRotation(picturebg.rotation());
        picture.setPosition(picturebg.position());
        picture.setZPosition(39);
        picture.hide();

        guideGroup.setScreenMaskControl(boardMask);

        guideGroup.hide();

        preparePaintForDrawing();
    }


    public void doMainXX() throws Exception
    {
        demoNewWord();
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

    public void cleanUpBoard() throws Exception
    {
        lockScreen();
        cleanUpDrawing();

        detachControl(guideGroup);

        detachControl(picture);
        detachControl(boardMask);

        picturebg.hide();
        picturepin.hide();

        hideLines();
        unlockScreen();
        playSfxAudio("alloff",true);
    }

    public void showPicture() throws Exception
    {
        lockScreen();
        picture.show();
        picturebg.show();
        picturepin.show();
        playSfxAudio("picon",false);
        unlockScreen();
        waitSFX();
    }

    public void animateLinesOn() throws Exception
    {
        playSfxAudio("lineson",false);
        super.animateLinesOn();
        waitSFX();
    }


    public void showLettersAndPlay() throws Exception
    {
        lockScreen();
        guideGroup.show();
        playSfxAudio("wordon",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
    }

    public void demotrace_1() throws Exception
    {
        presenterOpening();

        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.8f,1f,board.frame()),-30,0.5f,"DEMO2",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,board.frame()),-25,0.5f,"DEMO2",1,0.3f);

        showPicture();
        waitForSecs(0.3f);
        currentWord.playAudio(this,true);
        waitForSecs(0.5f);

        showLettersAndPlay();
        animateLinesOn();

        moveScenePointer(OB_Maths.locationForRect(0.6f,1.05f,convertRectFromControl(guideGroup.objectDict.get("Path1_1").frame(),guideGroup)),-40,0.5f,"DEMO3",0,0.3f);
        playAudioScene("DEMO3",1,false);
        PointF loc = OC_Generic.copyPoint(thePointer.position());
        loc.x = OB_Maths.locationForRect(1f,1f,guideGroup.frame()).x;
        movePointerToPoint(loc,-25,1f,true);
        waitAudio();
        waitForSecs(0.3f);
        arrowButton.setOpacity(0.5f);
        arrowButton.show();
        moveScenePointer(OB_Maths.locationForRect(-0.2f,0.6f,arrowButton.frame()),-10,0.5f,"DEMO3",2,0.5f);
        arrowButton.hide();
        arrowButton.setOpacity(1);
        thePointer.hide();
        startScene();
    }

    public void demoNewWord() throws Exception
    {

        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        showPicture();
        waitForSecs(0.3f);
        currentWord.playAudio(this,true);
        waitForSecs(0.5f);
        showLettersAndPlay();

        animateLinesOn();
    }


}
