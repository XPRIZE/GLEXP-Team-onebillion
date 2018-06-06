package org.onebillion.onecourse.mainui.oc_listeninggame;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/01/2018.
 */

public class OC_Lg4 extends OC_Lg
{
    Map<String,OBPhoneme> componentDict;

    public String sectionAudioName()
    {
        return "lg4";
    }

    public void prepare()
    {
        super.prepare();
        componentDict = OBUtils.LoadWordComponentsXML(true);

        if(parameters.containsKey("trials"))
            setMaxTrials(OBUtils.getIntValue(parameters.get("trials")));

        String[] wordParams1a = null;
        if(parameters.containsKey("mode1a"))
            wordParams1a = parameters.get("mode1a").split(",");

        String[] wordParams1b = null;
        if(parameters.containsKey("mode1b"))
            wordParams1b = parameters.get("mode1b").split(",");

        if(parameters.containsKey("distractors"))
            addToDistrators(wordsWithPicturesForIds(parameters.get("distractors").split(",")));

        int size = OBUtils.getIntValue(parameters.get("size"));
        if(wordParams1a != null)
        {
            setupEventForLearning(wordsWithPicturesForIds(wordParams1a),size);
        }

        if(wordParams1b != null)
        {
            setupEventForPractice(wordsWithPicturesForIds(wordParams1b),size);
        }

        finalisePrepare();
    }

    public List<OBPhoneme> wordsWithPicturesForIds(String[] phonemeIds)
    {
        List<OBPhoneme> arr = new ArrayList<>();

        for(String phonemeId : phonemeIds)
        {
            OBPhoneme pho = componentDict.get(phonemeId);
            if(pho != null && pho.getClass() == OBWord.class)
            {
                if(OBImageManager.sharedImageManager().getImgPath(((OBWord) pho).imageName) != null)
                arr.add(pho);
            }
        }
        return arr;
    }

    public OBControl loadTargetForPhoneme(OBPhoneme phon, OBControl bg, int type, Map<String,Object> data)
    {
        OBImage image = loadImageWithName(((OBWord) phon).imageName,new PointF(0, 0),new RectF(bounds()),false);
        image.setRasterScale(0.5f);
        image.setPosition(bg.position());
        return image;
    }

    public void highlightTarget(OBControl control)
    {
        lockScreen();
        OBControl bg = (OBControl)control.propertyValue("bg");
        bg.setBorderColor(Color.RED);
        bg.setBorderWidth(applyGraphicScale(8));
        unlockScreen();
    }


    public void lowlightTarget(OBControl control)
    {
        lockScreen();
        OBControl bg = (OBControl)control.propertyValue("bg");
        bg.setBorderWidth(0);
        unlockScreen();
    }

    public boolean keepTargetsSameSize()
    {
        return false;
    }

}