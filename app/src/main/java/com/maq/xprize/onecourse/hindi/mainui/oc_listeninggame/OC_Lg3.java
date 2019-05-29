package com.maq.xprize.onecourse.hindi.mainui.oc_listeninggame;

import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.utils.OBAudioManager;
import com.maq.xprize.onecourse.hindi.utils.OBPhoneme;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/01/2018.
 */

public class OC_Lg3 extends OC_Lg
{
    Map<String, OBPhoneme> tempNumPhonemes;
    int maxNumberLength;

    public String sectionAudioName()
    {
        return "lg3";
    }

    public void prepare()
    {
        super.prepare();
        maxNumberLength = 0;
        tempNumPhonemes = new ArrayMap<>();
        if(parameters.containsKey("trials"))
            setMaxTrials(OBUtils.getIntValue(parameters.get("trials")));

        String[] numberParams1a = null;
        if(parameters.containsKey("mode1a"))
            numberParams1a = parameters.get("mode1a").split(",");

        String[] numberParams1b = null;
        if(parameters.containsKey("mode1b"))
            numberParams1b = parameters.get("mode1b").split(",");

        if(parameters.containsKey("distractors"))
            addToDistrators(phonemesForNums(parameters.get("distractors").split(",")));

        int size = OBUtils.getIntValue(parameters.get("size"));
        if(numberParams1a != null)
        {
            setupEventForLearning(phonemesForNums(numberParams1a),size);
        }

        if(numberParams1b != null)
        {
            setupEventForPractice(phonemesForNums(numberParams1b),size);
        }


        finalisePrepare();
    }

    public List<OBPhoneme> phonemesForNums(String[] numbers)
    {
        List<OBPhoneme> arr = new ArrayList<>();

        for(String number : numbers)
        {
            if(number.length() > maxNumberLength)
                maxNumberLength = number.length();
            String audioId = String.format("n_%s",number);

            if(tempNumPhonemes.get(audioId) != null)
            {
                OBPhoneme pho = tempNumPhonemes.get(audioId);
                arr.add(pho);
            }
            else if(OBAudioManager.audioManager.getAudioPath(audioId) != null)
            {
                OBPhoneme pho = new OBPhoneme(number,audioId);
                tempNumPhonemes.put(audioId, pho);
                arr.add(pho);
            }
        }
        return arr;
    }

    public float fontSizeForType(int type)
    {
        return applyGraphicScale(60+Math.abs(80-(maxNumberLength*20)));
    }



}
