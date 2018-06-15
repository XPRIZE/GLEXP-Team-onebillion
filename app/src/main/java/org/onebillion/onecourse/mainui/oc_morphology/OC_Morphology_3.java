package org.onebillion.onecourse.mainui.oc_morphology;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OC_Morphology_3 extends OC_Morphology
{
    List originalLabels,substitutedLabels,labelsAndDash,bottomLabels;
    List words;
    OBLabel substitutedLabel;
    int indexOfSubstitutedLabel;
    float dashWidth;
    OBControl dash;
    boolean playFirstAudio;
    Boolean annaIntro;
    public class ocm_sinstance extends Object
    {
         String sid,imageName;
         ocm_sentence sentence;
         int wordNo;
         int rst,ren;
    }
    List<ocm_sinstance> instances;

    public void miscSetUp()
    {
        super.miscSetUp();
        highlightColour = Color.RED;
        //sharedLock = new OBConditionLock(PROCESS_DONE);

        annaIntro = OBUtils.coalesce(parameters.get("annaintro"),"false").equals("true");
        playFirstAudio = OBUtils.coalesce(parameters.get("playhint") , "true").equals("true");
        List<String> gps = Arrays.asList(parameters.get("sentencegroups").split(";"));
        instances = new ArrayList<>();
        for(String gp : gps)
        {
            String[] comps = gp.split(",");
            ocm_sinstance sin = new ocm_sinstance();
            sin.sid = (comps[0]);
            sin.imageName = (comps[1]);
            sin.sentence = sentenceDict.get(sin.sid);
            String groupname = comps[2];
            String mtype = comps[3];
            String mkey = String.format("%s+%s",mtype,groupname);
            List<List<Integer>> av = sin.sentence.markups.get(mkey);
            List<Integer>v = av.get(0);
            int st = v.get(0);
            int en = v.get(1);
            sin.rst = st;
            sin.ren = en;
            instances.add(sin);
        }
        events = new ArrayList<>();
        events.add(annaIntro?"a":"b");
        events.add("n");
        events.add("o");
        List ev2 = Arrays.asList("o2","p","q","r");
        while(ev2.size() < instances.size())
            ev2.add(ev2.get(ev2.size() - 1));
        while(ev2.size() > instances.size())
            ev2.remove(ev2.size()-1);
        events.addAll(ev2);
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_DRAG);
    }

    public void scalePicToBox()
    {
        OBControl picbox = objectDict.get("imagebox");
        float wratio = mainPic.width() / picbox.width();
        float hratio = mainPic.height() / picbox.height();
        float ratio = wratio > hratio?wratio:hratio;
        mainPic.setScale(1 / ratio);
        mainPic.setPosition(picbox.position());
    }

    public void setUpPic(String imageName)
    {
        if(mainPic != null)
            detachControl(mainPic);
        mainPic = loadImageWithName(imageName,new PointF(0.5f, 0.5f),boundsf());
        mainPic.setZPosition(objectDict.get("textbox").zPosition() -0.01f);
        scalePicToBox();
        mainPic.hide();
    }

}
