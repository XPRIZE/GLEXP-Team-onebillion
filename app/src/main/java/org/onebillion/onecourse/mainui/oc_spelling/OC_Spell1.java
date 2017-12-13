package org.onebillion.onecourse.mainui.oc_spelling;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 13/12/17.
 */

public class OC_Spell1 extends OC_Spell
{
    public void setSceneb()
    {
    }

    public void setScenec()
    {
        reminderAudio = currentAudio("PROMPT.REMINDER");
    }

    public void demoa() throws Exception
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
        showWordAndSpeak(currWordID);
        waitForSecs(0.4f);
        flyWordToBottom();
        highlightLetter(letterIdx);
        waitForSecs(0.4f);
        nextScene();
    }

    public void demob() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        RectF botrect = objectDict.get("bottomrect") .frame();
        PointF destpt = new PointF(rightMostLabelX()  + botrect.width() / 2f,botrect.centerY());
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
            playSfxAudio("letterin",false);
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

            highlightLetter(++letterIdx);
            waitForSecs(0.4f);
        }

        speakWord(currWordID);
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(0.3f);
        flyWordToBottom();
        letterIdx = 0;
        highlightLetter(letterIdx);
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO2",true);
        nextScene();
    }

    public void blendWord()
    {

    }
}
