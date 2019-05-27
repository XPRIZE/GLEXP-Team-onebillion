package com.maq.xprize.onecourse.mainui.oc_spelling;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.controls.*;
import com.maq.xprize.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by alan on 13/12/17.
 */

public class OC_Spell3 extends OC_Spell
{
    public void miscSetUp()
    {
        super.miscSetUp();

        loadEvent("dash");
        OBLabel wlabel = new OBLabel("w",font);
        RectF bb = boundingBoxForText(wlabel.text(),font);
        dashWidth = bb.width();
        dashSpace = dashWidth / 5;
    }


    public void cleanScene()
    {
        super.cleanScene();
        detachControls(dashes);
    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);
        dashes = layOutDashesForLabels(staticLabels);
        int i = 0;
        for(OBLabel sl : staticLabels)
        {
            PointF pt = new PointF();
            pt.set(sl.position());
            pt.x = dashes.get(i).position().x;
            sl.setPosition(pt);
            sl.setProperty("dashpos",pt);
            i++;
        }
    }

    public void setSceneb()
    {

    }

    public void setScenec()
    {
        List marr = currentAudio("PROMPT.REMINDER");
        marr.add(currWordID);
        reminderAudio = marr;
    }

    public void doMainc() throws Exception
    {
        setUpReplay();
        List marr = currentAudio("PROMPT");
        marr.add(currWordID);
        playAudioQueued(marr,false);
    }

    public void setUpReplay()
    {
        List marr = new ArrayList<>(currentAudio("PROMPT.REPEAT"));
        marr.add(currWordID);
        setReplayAudio(marr);
    }

    public void slideLettersIn() throws Exception
    {
        float w = bounds().width();
        List anims = new ArrayList<>();
        lockScreen();
        for(OBLabel lab : labels)
        {
            PointF bpos = (PointF) lab.propertyValue("botpos");
            PointF pos = new PointF();
            pos.set(bpos);
            pos.x += w;
            lab.setPosition(pos);
            lab.show();
            anims.add(OBAnim.moveAnim(bpos,lab));
        }
        unlockScreen();
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(anims,0.4f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        waitSFX();
        waitForSecs(0.5f);
    }

    public void showStuff()throws Exception
    {
        if(showPic)
        {
            showPic();
            waitForSecs(0.4f);
        }
        if(labels.get(0).hidden())
        {
            speakWord(currWordID);

            waitForSecs(0.4f);
            slideLettersIn();
            waitForSecs(0.4f);
            showDashes();
            waitForSecs(0.2f);
            highlightMarker();
        }
    }

    public void hideCurrentMarker()
    {
        dashes.get(letterIdx).hide();
    }

    public void highlightMarker() throws Exception
    {
        highlightDash(letterIdx);
    }

    public void demoa()throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);
        if(showPic)
        {
            showPic();
            waitForSecs(0.4f);
        }
        speakWord(currWordID);
        waitForSecs(0.4f);
        slideLettersIn();
        showDashes();
        highlightDash(letterIdx);
        waitForSecs(0.4f);
        nextScene();
    }

    public void demob()throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        RectF botrect = objectDict.get("bottomrect") .frame();
        PointF destpt = new PointF(rightMostLabelX()  + botrect.width() / 2,botrect.centerY());
        PointF startpt = pointForDestPoint(destpt,45);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);

        for(OBLabel sl : staticLabels)
        {
            String tx = sl.text();
            OBLabel targetlab = candidateLabelWithText(tx);
            movePointerToPoint(OB_Maths.locationForRect(0.5f, 0.7f, targetlab.frame()),-1,true);
            waitForSecs(0.2f);
            moveObjects(Arrays.asList(targetlab,thePointer),sl.position(),-0.7f,OBAnim.ANIM_EASE_IN_EASE_OUT);
            targetlab.setProperty("staticLabelIndex",letterIdx);
            playSfxAudio("letterin",false);
            dashes.get(letterIdx).hide();
            movePointerForwards(applyGraphicScale(-100),-1);
            waitSFX();
            waitForSecs(0.2f);
            if(letterAudio != OCSP_LETTER_AUDIO_NONE)
            {
                int col = targetlab.colour();
                targetlab.setColour(Color.RED);
                playLetterAudio(sl.text() );
                waitForSecs(0.2f);
                waitAudio();
                targetlab.setColour(col);
                waitForSecs(0.2f);
            }

            highlightDash(++letterIdx);
            waitForSecs(0.4f);
        }

        blendWord();
        waitForSecs(0.3f);
        speakWord(currWordID);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        flyWordToBottom();
        showDashes();
        waitForSecs(0.3f);
        letterIdx = 0;
        highlightMarker();
        waitForSecs(0.4f);

        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.2f);

        if(RADemo)
        {
            buttonDemo();
        }
        nextScene();
    }

}
