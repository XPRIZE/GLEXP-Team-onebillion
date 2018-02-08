package org.onebillion.onecourse.mainui.oc_patternwordsyl;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by michal on 16/01/2018.
 */

public class OC_Pws2 extends OC_Pws
{
    List<List<String>> syllablesOrder;

    public String eventName()
    {
        return "pws2";
    }

    public String sectionAudioName()
    {
        return "pws2";
    }

    public boolean demomatchPlayAudio()
    {
        return false;
    }

    public void prepare()
    {
        super.prepare();
        String[] syllableGroups = parameters.get("syllables").split(";");
        syllablesOrder = new ArrayList<>();
        List<String> syllables = new ArrayList<>();
        for(String syllablesString : syllableGroups)
        {
            String[] sylArr = syllablesString.split(",");
            List<String> layout = new ArrayList<>();
            for(String phonemeid : sylArr)
            {
                if (componentDict.containsKey(phonemeid))
                {
                    syllables.add(phonemeid);
                    layout.add(phonemeid);
                }
                else
                {
                    layout.add("");
                }
            }

            syllablesOrder.add(layout);

        }
        prepareLabelsForPhonemeIds(OBUtils.randomlySortedArray(syllables),false);
        loadEventList(OBUtils.getBooleanValue(parameters.get("pattern")),true);
    }

    public void demopattern() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        OBControl syllablesBox = objectDict.get("syllables_box");
        List<OBAnim> anims = new ArrayList<>();
        boolean smallSet = syllablesOrder.size() < 3;
        float leftStart = smallSet ? 0.18f : 0.0f;
        long maxRow = syllablesOrder.get(0).size();
        for(int i=0; i<syllablesOrder.size(); i++)
            if(maxRow < syllablesOrder.get(i).size())
                maxRow = syllablesOrder.get(i).size();

        for(int i=0; i<syllablesOrder.size(); i++)
        {
            List<String> arr = syllablesOrder.get(i);
            float left = leftStart + i*0.33f;
            for(int j=0; j<arr.size(); j++)
            {
                String objId = arr.get(j);
                if(objId.equals(""))
                    continue;
                OBControl con = targetLabelsDict.get(objId);
                float top = j*0.9f/maxRow;
                PointF topLeft = OB_Maths.locationForRect(left,top,syllablesBox.frame());
                anims.add(OBAnim.propertyAnim("left",topLeft.x,con));
                anims.add(OBAnim.propertyAnim("top",topLeft.y,con));
            }
        }
        playSfxAudio("lineup",false);
        OBAnimationGroup.runAnims(anims,0.5,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.8f);
        lockScreen();

        for(OBLabel label : targetLabels)
            label.setColour(eventColours.get("pattern"));
        playSfxAudio("colour",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        playAudioScene("DEMO2",0,true);
        waitForSecs(0.3f);
        PointF loc = OB_Maths.locationForRect(1f,1f,syllablesBox.frame());
        for(int i=0; i<maxRow; i++)
        {
            OBLabel prevLabel = null;
            boolean newRow = true;
            for(int j=0; j<syllablesOrder.size(); j++)
            {
                if(syllablesOrder.get(j).size() <= i)
                    continue;
                String objId = syllablesOrder.get(j).get(i);
                if(objId == null || objId.equals(""))
                    continue;

                if(!targetLabelsDict.containsKey(objId))
                    continue;

                OBLabel label = targetLabelsDict.get(objId);
                if(newRow)
                {
                    newRow = false;
                    loc.y = label.position().y;
                    movePointerToPoint(loc,-40,i==0?0.5f:0.3f,true);
                }
                lockScreen();
                if(prevLabel != null)
                    prevLabel.setColour(eventColours.get("normal"));
                label.setColour(eventColours.get("highlight"));

                unlockScreen();
                playAudio((String)label.propertyValue("audio"));
                waitAudio();
                waitForSecs(0.3f);
                prevLabel = label;
            }
            prevLabel.setColour(eventColours.get("normal"));
            waitForSecs(0.3f);
        }
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        nextScene();
    }


}
