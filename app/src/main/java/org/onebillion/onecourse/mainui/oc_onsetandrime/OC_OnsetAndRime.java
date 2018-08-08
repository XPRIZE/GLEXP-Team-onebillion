package org.onebillion.onecourse.mainui.oc_onsetandrime;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 27/12/17.
 */

public class OC_OnsetAndRime extends OC_Onset
{

    public void demoa()throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoa();

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

        actionsForDemoc();

        nextScene();
    }


    public void demof() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemof();

        nextScene();
    }

    public void  demog()throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemog(rime,false);

        nextScene();
    }

    public void demoh() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoh();

        nextScene();
    }

    public void demoi() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);

        actionsForDemoi();

        nextScene();
    }

}
