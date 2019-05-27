package com.maq.xprize.onecourse.mainui.oc_patternwordsyl;

import android.graphics.PointF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by michal on 15/01/2018.
 */

public class OC_Pws1 extends OC_Pws
{
    OBLabel patternLabel;
    boolean playPattern;

    public void prepare()
    {
        super.prepare();
        String[] words = parameters.get("words").split(",");
        prepareLabelsForPhonemeIds(OBUtils.randomlySortedArray(Arrays.asList(words)),OBUtils.getBooleanValue(parameters.get("images")));
        if(parameters.get("pattern") != null)
        {
            String[]arr = parameters.get("pattern").split(",");
            if(componentDict.get(arr[0]) != null)
            {
                playPattern = arr[1].equals("true");
                OBPhoneme pho= componentDict.get(arr[0]);
                OBControl imageBox = objectDict.get("image_box");
                patternLabel = new OBLabel(pho.text,OBUtils.StandardReadingFontOfSize(140));
                if(patternLabel.width() > imageBox.width())                patternLabel.setScale(imageBox.width()/patternLabel.width());
                patternLabel.setColour(eventColours.get("pattern"));
                patternLabel.setProperty("audio",pho.audio());
                OBGroup group = new OBGroup(Arrays.asList((OBControl)patternLabel));
                group.sizeToTightBoundingBox();
                group.setPosition(imageBox.position());
                group.setZPosition(10);
                attachControl(group);
                patternLabel.hide();
            }
        }
        loadEventList(patternLabel != null,true);
    }

    public String eventName()
    {
        return "pws1";
    }

    public String sectionAudioName()
    {
        return "pws1";
    }

    public int audioRepeatCount()
    {
        return 2;
    }

    public void demopattern() throws Exception
    {
        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.5f,0.9f,this.bounds()),-30,0.5f,"DEMO",0,0.3f);
        final float heightDist = 0.8f * targetLabels.get(0).height();

        List<OBLabel> highlightList = new ArrayList<>(targetLabels);
        Collections.sort(highlightList, new Comparator<OBLabel>()
                {
                    @Override
                    public int compare(OBLabel lhs, OBLabel rhs)
                    {
                        PointF loc1 = lhs.position();
                        PointF loc2 = rhs.position();
                        float diff = loc2.y-loc1.y;
                        if(Math.abs(diff) < heightDist)
                        {
                            if(loc1.x < loc2.x)
                                return -1;
                            else
                                return 1;
                        }
                        else
                        {
                            if(diff>0)
                                return -1;
                            else
                                return 1;
                        }
                    }
                }
        );

        for(OBLabel con : highlightList)
        {
            int index = con.text().indexOf(patternLabel.text());
            con.setHighRange(index, index+patternLabel.text().length(),eventColours.get("pattern"));
            playSfxAudio("pattern",true);
        }
        waitForSecs(0.3f);
        OBControl imageBox = objectDict.get("image_box");
        lockScreen();
        patternLabel.show();
        imageBox.show();
        playSfxAudio("bigletters",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.8f,0.9f,imageBox.frame()),-30,0.5f,"DEMO",1,0.3f);
        if(playPattern)
        {
            patternLabel.setColour(eventColours.get("highlight"));
            for(int i=0; i<3; i++)
            {
                playAudio((String)patternLabel.propertyValue("audio"));
                waitAudio();
                waitForSecs(0.7f);
            }
            patternLabel.setColour(eventColours.get("pattern"));
        }
        else
        {
            moveScenePointer(OB_Maths.locationForRect(0.3f,0.9f,imageBox.frame()),-30,0.5f,"DEMO3",0,0.3f);
        }
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1.5f);
        lockScreen();
        patternLabel.hide();
        imageBox.hide();
        for(OBLabel con : targetLabels)
        {
            con.setHighRange(-1, -1, eventColours.get("normal"));
            con.setColour(eventColours.get("normal"));
        }
        unlockScreen();
        waitForSecs(0.3f);
        nextScene();
    }

    public void demofind_1() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        List<OBLabel> newOrder = OBUtils.VeryRandomlySortedArray(targetLabels);
        for(int i=0; i<targetLabels.size(); i++)
        {
            anims.add(OBAnim.moveAnim(newOrder.get(i).position(),targetLabels.get(i)));
        }
        playSfxAudio("wordswap",false);
        OBAnimationGroup.runAnims(anims,0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitSFX();
        waitForSecs(0.3f);
        startScene();
    }


}
