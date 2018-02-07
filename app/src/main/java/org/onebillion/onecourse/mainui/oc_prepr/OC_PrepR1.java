package org.onebillion.onecourse.mainui.oc_prepr;

import android.graphics.Color;
import android.util.ArrayMap;

import org.json.JSONObject;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.oc_egrid.OC_Egrid;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 22/01/2018.
 */

public class OC_PrepR1 extends OC_Egrid
{
    int currentPhonemeIndex, currentScore, maxScore;
    List<OBPhoneme> targetPhonemes;
    Map<String,List<String>> wrongMap;

    public void prepare()
    {
        super.prepare();
        events = Arrays.asList("intro");
        wrongMap = new ArrayMap<>();
        String mode = parameters.get("mode");
        loadAudioXML(getConfigPath(String.format("prepr1%saudio.xml",mode)));
        currentPhonemeIndex = 0;
        targetPhonemes = new ArrayList<>();
        int maxAttempts = OBUtils.getIntValue(parameters.get("max"));
        currentScore = 0;
        List<OBLabel> randomLabels = OBUtils.randomlySortedArray(wordLabels);
        for(OBLabel label : randomLabels.subList(0, maxAttempts))
        {
            targetPhonemes.add((OBPhoneme)label.propertyValue("phoneme"));
        }
        maxScore = 2*maxAttempts;
        currentPhonemeIndex = 0;
        targetPhoneme = targetPhonemes.get(0);
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                demo();
            }
        });
    }

    public void wordTouched(OBLabel label) throws Exception
    {
        if(targetPhoneme == null)
            return;
        playAudio(null);
        label.setColour(Color.RED);
        OBPhoneme pho  = (OBPhoneme)label.propertyValue("phoneme");
        if(pho == targetPhoneme)
        {
            if(wrongCount==0)
                currentScore+=2;
            else
                currentScore+=1;
            gotItRightBigTick(false);
            waitSFX();
            label.setColour(Color.BLACK);
            nextTarget();

        }
        else
        {
            if(shoulCollectMiscData())
            {
                if(!wrongMap.containsKey(targetPhoneme.soundid))
                    wrongMap.put(targetPhoneme.soundid, new ArrayList<String>());
                wrongMap.get(targetPhoneme.soundid).add(pho.soundid);
            }
            wrongCount++;
            gotItWrongWithSfx();
            waitSFX();
            if(wrongCount == 1)
            {
                lockScreen();
                label.setColour(Color.BLACK);
                hideWrongRows();
                unlockScreen();
                long time = setStatus(STATUS_AWAITING_CLICK);
                waitForSecs(0.3f);
                if(time == statusTime())
                {
                    playTargetAudio();
                }

            }
            else if(wrongCount > 1)
            {
                label.setColour(Color.BLACK);
                demoWrong();
            }
        }
    }

    public void fin()
    {
        if (shoulCollectMiscData())
        {
            if(wrongMap.size() > 0)
                collectMiscData("wrong", wrongMap);
        }
        showScoreHammerScore((int)((currentScore*1.0f/maxScore)*10),false);
    }

    public void nextTarget() throws Exception
    {
        wrongCount = 0;
        currentPhonemeIndex++;
        if(currentPhonemeIndex >= targetPhonemes.size())
        {
            waitForSecs(0.3f);
            fin();

        }
        else
        {
            resetLabels(false);
            waitForSecs(0.3f);
            targetPhoneme = targetPhonemes.get(currentPhonemeIndex);
            String nextPhonemeAudio = targetPhoneme.audio();
            setReplayAudio(audioWithPhoneme("PROMPT.REPEAT"));
            playAudio(nextPhonemeAudio);
            long time = setStatus(STATUS_AWAITING_CLICK);
            waitAudio();
            reprompt(time,audioWithPhoneme("REMIND"),5);

        }

    }
    public void startScene() throws Exception
    {
        setReplayAudio(audioWithPhoneme("PROMPT.REPEAT"));
        playAudioQueued(audioWithPhoneme("PROMPT"),true);
        long time = setStatus(STATUS_AWAITING_CLICK);
        waitAudio();
        reprompt(time,audioWithPhoneme("REMIND"),5);

    }

    public OBLabel getCorrectLabel() throws Exception
    {
        for(List<OBLabel> row : labelsLayout)
        {
            for(OBLabel label : row)
            {
                if((!label.hidden() && label.propertyValue("phoneme") == targetPhoneme) || !label.isEnabled())
                {
                    return label;
                }
            }
        }
        return null;
    }

    public List<OBLabel> getCorrectLabelRow() throws Exception
    {
        for(List<OBLabel> row : labelsLayout)
        {
            for(OBLabel label : row)
            {
                if((!label.hidden() && label.propertyValue("phoneme") == targetPhoneme) || !label.isEnabled())
                {
                    return row;
                }
            }
        }
        return null;
    }

    public void demo() throws Exception
    {
        animateGridShow();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO",300,true);
        waitForSecs(0.3f);
        startScene();
    }

    public void demoWrong() throws Exception
    {
        OBLabel correctLabel = getCorrectLabel();
        waitForSecs(0.5f);
        for(int i=0; i<3; i++)
        {
            correctLabel.setColour(Color.BLUE);
            waitForSecs(0.5f);
            correctLabel.setColour(Color.BLACK);
            waitForSecs(0.5f);
        }
        List<OBLabel> labelRow = getCorrectLabelRow();
        loadPointer(POINTER_LEFT);
        for(int i=0; i<labelRow.size(); i++)
        {
            OBLabel label = labelRow.get(i);
            movePointerToPoint(OB_Maths.locationForRect(0.6f,1.2f,label.frame()),i==0?0.5f:0.3f,true);
            label.setColour(Color.BLUE);
            OBPhoneme pho = (OBPhoneme)label.propertyValue("phoneme");
            pho.playAudio(this,true);
            waitForSecs(0.3f);
            label.setColour(Color.BLACK);
            waitForSecs(0.3f);
        }
        thePointer.hide();
        waitForSecs(0.5f);
        fin();
    }

}
