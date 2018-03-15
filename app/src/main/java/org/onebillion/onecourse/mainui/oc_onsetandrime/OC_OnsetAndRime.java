package org.onebillion.onecourse.mainui.oc_onsetandrime;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.oc_reading.OC_Reading;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUserPressedBackException;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller.boundingBoxForText;
import static org.onebillion.onecourse.utils.OBAudioManager.AM_MAIN_CHANNEL;
import static org.onebillion.onecourse.utils.OBUtils.LoadWordComponentsXML;
import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;

/**
 * Created by alan on 27/12/17.
 */

public class OC_OnsetAndRime extends OC_Onset
{








    public void demoa()throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        PointF destpt = OB_Maths.locationForRect(0.9f, -0.1f,objectDict.get("bottomrect").frame());
        PointF startpt = pointForDestPoint(destpt,10);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.1f);
        playAudioQueuedScene("DEMO",true);
        slideInLabels();
        waitForSecs(0.4f);
        nextScene();
    }

    public void demob() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        PointF ppos = new PointF();
        ppos.set(thePointer.position());
        movePointerForwards(applyGraphicScale(250), -1);
        waitForSecs(0.1f);
        playAudioQueuedScene("DEMO", true);
        waitForSecs(0.4f);
        playAudioQueued(Arrays.asList((Object) wordIDs.get(currNo)), true);
        waitForSecs(0.4f);

        playAudioQueuedScene("DEMO2", true);
        PointF destpt = OB_Maths.locationForRect(0.5f, 0.6f, bottomLabels.get(0).frame());
        movePointerToPoint(destpt, -35, -1, true);
        waitForSecs(0.4f);
        OBLabel dragLabel = bottomLabels.get(0);
        destpt.x = (completeLabel.left() + (dragLabel.width() / 2));
        destpt.y = (completeLabel.position().y);
        //moveObjects(Arrays.asList(dragLabel,thePointer),destpt,-1,OBAnim.ANIM_EASE_IN_EASE_OUT,null);

        List anims = AnimsForMoveToPoint(Arrays.asList(dragLabel, thePointer), destpt);
        OBAnim turnAnim = OBAnim.rotationAnim((float) Math.toRadians(-10), thePointer);
        anims.add(turnAnim);
        OBAnimationGroup.runAnims(anims, 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, null);


        playSfxAudio("onset", true);
        lockScreen();
        dash.hide();
        unlockScreen();

        movePointerForwards(applyGraphicScale(-300), -1);
        waitForSecs(0.4f);

        lockScreen();
        completeLabel.setColour(Color.RED);
        completeLabel.show();
        dragLabel.hide();
        rimeLabel.hide();
        dash.hide();
        unlockScreen();

        playAudioQueued(Arrays.asList((Object) wordIDs.get(currNo)), true);
        waitForSecs(0.8f);


        lockScreen();
        completeLabel.hide();
        dragLabel.show();
        rimeLabel.show();
        unlockScreen();

        movePointerToPoint(ppos, -1, false);
        destpt = (PointF) dragLabel.propertyValue("botpos");
        moveObjects(Arrays.asList((OBControl) dragLabel), destpt, -1.5f, OBAnim.ANIM_EASE_IN_EASE_OUT);
        lockScreen();
        dash.show();
        unlockScreen();
        waitForSecs(0.3f);

        playAudioQueuedScene("DEMO3", true);

        nextScene();
    }


    public void democ() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        playAudioScene("DEMO",0,true);
        waitForSecs(0.3f);
        //playAudioQueued(Arrays.asList(wordIDs.get(currNo)),true);
        //waitForSecs(0.3f);

        PointF destpt = OB_Maths.locationForRect(0.5f, 2f, dash.frame());
        movePointerToPoint(destpt,-1,true);
        playAudioScene("DEMO",1,true);

        RectF f = AllLabelsFrame(bottomLabels);
        movePointerToPoint(OB_Maths.locationForRect(0f, 0.6f, f),-35,-1,true);
        playAudioScene("DEMO",2,false);
        waitForSecs(0.5f);

        movePointerToPoint(OB_Maths.locationForRect(1f, 0.6f, f),-5,-1,true);
        waitForSecs(0.8f);

        PointF butPt;
        lockScreen();
        butPt = OB_Maths.locationForRect(0.5f,1.1f, MainViewController().topRightButton.frame);
        unlockScreen();
        movePointerToPoint(butPt,0,-1,true);

        playAudioScene("DEMO",3,true);

        waitForSecs(0.4f);
        thePointer.hide();
        waitForSecs(0.65f);
        nextScene();

    }


    public void demof() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        playSfxAudio("paneloff",false);
        objectDict.get("bottomrect").hide();
        waitForSecs(0.2f);
        shuffleLabels();
        nextScene();
    }

    public void  demog()throws Exception
    {
        int rimeColour = objectDict.get("rimeswatch").fillColor();
        PointF destpt = OB_Maths.locationForRect(0.9f, -0.1f ,objectDict.get("bottomrect").frame());
        PointF startpt = pointForDestPoint(destpt,10);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.2f);
        int sufflen =(int) rime.length();
        for(OBLabel lab : labelList)
        {
            playSfxAudio("pattern",false);
            lockScreen();
            lab.setColour(rimeColour);
            lab.setHighRange(0,lab.text().length()  - sufflen,Color.BLACK);
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
            waitForSecs(0.2f);
        }
        nextScene();
    }

    public void demoh() throws Exception
    {
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",true);
        int i = 0;
        for(OBLabel lab : labelList)
        {
            lockScreen();
            lab.setColour(Color.RED);
            unlockScreen();
            playAudioQueued(Arrays.asList((Object)wordIDs.get(i)),true);
            lockScreen();
            lab.setColour(Color.BLACK);
            unlockScreen();

            waitForSecs(0.2f);
            i++;
        }
        waitForSecs(0.8f);
        scatterLabels();
        nextScene();
    }

    public void demoi() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        thePointer.hide();
        nextScene();
    }

}
