package org.onebillion.onecourse.mainui.oc_egragrid;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 17/01/2018.
 */

public class OC_Egrid extends OC_SectionController
{
    public List<OBLabel> wordLabels;
    public List<List<OBLabel>> labelsLayout;
    public int lowlightColour;
    public OBPhoneme targetPhoneme;
    public int wrongCount;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        targetPhoneme = null;
        wrongCount = 0;
        lowlightColour = OBUtils.colorFromRGBString(eventAttributes.get("lowlight_colour"));
        wordLabels = new ArrayList<>();
        Map<String,OBPhoneme> wordComponents = OBUtils.LoadWordComponentsXML(true);
        String[] phonemeids = parameters.get("targets").split(",");
        int rowCount = OBUtils.getIntValue(parameters.get("row"));
        int colCount = OBUtils.getIntValue(parameters.get("column"));
        OBControl workRect = objectDict.get("work_rect");
        Typeface typeface  = OBUtils.TypefaceForFile("LiberationSans-Regular.ttf");
        float distCol = 0.19f;
        float startLeft  = 0.5f - colCount*distCol/2.0f + 0.5f * distCol;
        float distRow = 0.09f;
        float startTop  = 0.5f - rowCount*distRow/2.0f + 0.5f * distRow;
        float maxSize = workRect.width() * distCol*0.9f;
        OBPath line =(OBPath) objectDict.get("line");
        line.sizeToBoundingBoxIncludingStroke();
        line.setWidth(workRect.width() * distCol*colCount);
        List<OBPhoneme> phonemesArr = new ArrayList<>();
        for(String phoid : phonemeids)
        {
            if(wordComponents.containsKey(phoid))
            {
                phonemesArr.add(wordComponents.get(phoid));
            }
        }
        List<OBPhoneme> phonemes = phonemesArr;
        List<List<OBLabel>> fullLayout = new ArrayList<>();
        List<OBLabel> row = new ArrayList<>();
        int count = 0;
        int size = rowCount*colCount;
        while(count < size)
        {
            if(count%phonemes.size() == 0)
                phonemes = OBUtils.randomlySortedArray(phonemes);
            OBPhoneme pho = phonemes.get(count%phonemes.size());
            OBLabel label = new OBLabel(pho.text,typeface,applyGraphicScale(23));
            RectF frame = new RectF(label.frame);
            //frame.inset(-applyGraphicScale(2) , -applyGraphicScale(2));
            //label.setFrame(frame);
            label.setColour(Color.BLACK);
            label.setPosition(OB_Maths.locationForRect(startLeft + distCol*(count%colCount),(float)(startTop + distRow*Math.floor(count/colCount)), workRect.frame()));
            attachControl(label);
            label.setProperty("phoneme",pho);
            if(label.width() > maxSize)
            {
                label.setScale(1.0f -((label.width() -maxSize) *1.0f/label.width() ));
            }
            wordLabels.add(label);
            label.enable();
            label.hide();
            row.add(label);
            if((count%colCount) ==colCount-1 && count!=0)
            {
                fullLayout.add(row);
                row = new ArrayList<>();
            }
            if(count > 0 && count < size-1 && count%colCount == 0)
            {
                OBPath path = (OBPath)line.copy();
                path.setPosition(OB_Maths.locationForRect(0.5f,(float)(startTop + distRow*Math.floor(count/colCount) - 0.5f*distRow),workRect.frame()));
                path.show();
                attachControl(path);
            }
            count++;
        }
        labelsLayout = fullLayout;

    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl label = finger(0, 1, (List<OBControl>) (Object) wordLabels, pt);
            if (label != null && label.isEnabled())
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        wordTouched((OBLabel) label);
                    }
                });
            }
        }
    }

    public void wordTouched(OBLabel label) throws Exception
    {

    }

    public void checkWordAudio(OBLabel label) throws Exception
    {
        highlightAndPlayAudio(label);
        if(wordLabels.size() == 0)
        {
            waitForSecs(0.3f);
            nextScene();
        }
        else
        {
            unlockWithReminder();
        }
    }

    public void checkWordWithTarget(OBLabel label) throws Exception
    {
        if(targetPhoneme == null)
            return;
        playAudio(null);
        label.setColour(Color.RED);
        OBPhoneme pho  = (OBPhoneme)label.propertyValue("phoneme");
        if(pho == targetPhoneme)
        {
            gotItRightBigTick(false);
            waitSFX();
            disableLabel(label);
            nextTarget();
        }
        else
        {
            wrongCount++;
            gotItWrongWithSfx();
            waitSFX();
            lockScreen();
            label.setColour(Color.BLACK);
            if(wrongCount == 1)
                hideWrongRows();
            else if(wrongCount > 1)
                hideWrongLabels();

            unlockScreen();
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitForSecs(0.3f);
            if(time == statusTime())
            {
                playTargetAudio();
            }
        }

    }

    public void nextTarget() throws Exception
    {
        wrongCount = 0;
    }

    public void playTargetAudio() throws Exception
    {
        if(targetPhoneme != null)
            targetPhoneme.playAudio(this,true);

    }

    public void hideWrongRows() throws Exception
    {
        for(List<OBLabel> row : labelsLayout)
        {
            if(!labels(row,targetPhoneme))
            {
                for(OBLabel label : row)
                    label.hide();
            }
        }
    }

    public void hideWrongLabels() throws Exception
    {
        for(List<OBLabel> row : labelsLayout)
        {
            for(OBLabel label : row)
            {
                if((!label.hidden() && label.propertyValue("phoneme") != targetPhoneme) || !label.isEnabled())
                {
                    label.hide();
                }
            }
        }
    }

    public boolean labels(List<OBLabel> row,OBPhoneme phoneme) throws Exception
    {
        for(OBLabel label : row)
        {
            if(label.propertyValue("phoneme") == phoneme && label.isEnabled())
                return true;
        }
        return false;
    }

    public void resetLabels(boolean enable) throws Exception
    {
        lockScreen();
        for(List<OBLabel> row : labelsLayout)
        {
            for(OBLabel label : row)
            {
                label.show();
                if(enable)
                    enableLabel(label);
            }
        }
        unlockScreen();
    }

    public void unlockWithReminder() throws Exception
    {
        List<String> audio = getAudioForScene(currentEvent(),"REMIND");
        if(audio != null)
            reprompt(setStatus(STATUS_AWAITING_CLICK),OBUtils.insertAudioInterval(audio,300),4);
    }

    public List<Object> audioWithPhoneme(String scene) throws Exception
    {
        List<String> audio = getAudioForScene(currentEvent(),"REMIND");

        List<String> fullAudio = new ArrayList<>();
        if(audio != null)
            fullAudio.addAll(audio);

        if(targetPhoneme != null)
            fullAudio.add(targetPhoneme.audio());

        return OBUtils.insertAudioInterval(fullAudio,300);

    }

    public void animateGridShow() throws Exception
    {
        playSfxAudio("gridshow",false);
        float wait = 0.75f/wordLabels.size();
        for(OBLabel con : wordLabels)
        {
            con.show();
            waitForSecs(wait);
        }
        waitSFX();
    }


    public void playAudioForLabel(OBLabel label) throws Exception
    {
        OBPhoneme pho =(OBPhoneme)label.propertyValue("phoneme");
        pho.playAudio(this,true);
    }

    public void highlightAndPlayAudio(OBLabel label) throws Exception
    {
        label.setColour(Color.RED);
        playAudioForLabel(label);
        disableLabel(label);
        wordLabels.remove(label);
    }

    public void disableLabel(OBLabel label)
    {
        label.setColour(lowlightColour);
        label.disable();
    }

    public void enableLabel(OBLabel label)
    {
        label.setColour(Color.BLACK);
        label.enable();
    }

}
