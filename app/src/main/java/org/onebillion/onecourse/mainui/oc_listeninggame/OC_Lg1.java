package org.onebillion.onecourse.mainui.oc_listeninggame;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBSyllable;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by michal on 19/07/16.
 */
public class OC_Lg1 extends OC_Lg
{
    Map<String,OBPhoneme> componentDict;


    public String sectionAudioName()
    {
        return "lg1";
    }

    public void prepare()
    {
        super.prepare();
        componentDict = OBUtils.LoadWordComponentsXML(true);

        if(parameters.containsKey("trials"))
            setMaxTrials(OBUtils.getIntValue(parameters.get("trials")));

        String[] wordParams1a = null;
        if(parameters.containsKey("mode1a"))
            wordParams1a = parameters.get("mode1a").split(";");

        String[] wordParams1b = null;
        if(parameters.containsKey("mode1b"))
            wordParams1b = parameters.get("mode1b").split(";");

        String[] wordParams2 = null;
        if(parameters.containsKey("mode2"))
            wordParams2 = parameters.get("mode2").split(";");

        if(parameters.containsKey("distractors"))
            addToDistrators(phonemesForIds(parameters.get("distractors").split(";")));

        int size = OBUtils.getIntValue(parameters.get("size"));
        if(wordParams1a != null)
        {
            setupEventForLearning(phonemesForIds(wordParams1a),size);
        }

        if(wordParams1b != null)
        {
            setupEventForPractice(phonemesForIds(wordParams1b),size);
        }

        if(wordParams2 != null)
        {
            setupEventForBuilding(phonemesForIds(wordParams2),size);
        }

        finalisePrepare();
    }

    public void setupEventForBuilding(List<OBPhoneme> phonemeList, int size)
    {
        List<OBPhoneme> extraDistrators = new ArrayList<>();
        for(int i=0; i<maxTrials; i++)
        {
            int index = i%phonemeList.size();
            if(index == 0)
                phonemeList = OBUtils.randomlySortedArray(phonemeList);


            extraDistrators.add(phonemeList.get(index));
            List<Map<String,Object>> events1 = new ArrayList<>();
            List<OBPhoneme> parts = null;
            if(phonemeList.get(index).getClass() == OBSyllable.class)
            {
                OBSyllable syl = (OBSyllable)phonemeList.get(index);
                parts = syl.phonemes;

            }
            else if(phonemeList.get(index).getClass() == OBWord.class)
            {
                OBWord word = (OBWord)phonemeList.get(index);
                parts = (List<OBPhoneme>)(Object)word.syllables();
            }
            extraDistrators.addAll(parts);

            for(OBPhoneme pho : parts)
            {
                events1.add(eventDataSize(size,pho));
            }
            events1.add(eventDataSize(size,phonemeList.get(index)));

            addToEventData(events1);
        }
        addToDistrators(extraDistrators);
    }

    public List<OBPhoneme> phonemesForIds(String[] phonemeIds)
    {
        List<OBPhoneme> arr = new ArrayList<>();


        for(String phonemeInfo : phonemeIds)
        {
            String[] phonemeData = phonemeInfo.split(",");
            String phonemeId = phonemeData[0];

            if(componentDict.containsKey(phonemeId))
            {
                OBPhoneme pho = null;
                if (phonemeData.length > 1 && phonemeData[1].equals("c"))
                {
                    pho = componentDict.get(phonemeId).copy();
                    if(pho.text.length() > 1)
                        pho.text = pho.text.substring(0, 1).toUpperCase() + pho.text.substring(1);
                    else
                        pho.text = pho.text.toUpperCase();
                }
                else
                {
                    pho = componentDict.get(phonemeId);
                }
                arr.add(pho);
            }
        }
        return arr;
    }


}
