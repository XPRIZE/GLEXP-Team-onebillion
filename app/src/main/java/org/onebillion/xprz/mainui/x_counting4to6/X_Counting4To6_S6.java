package org.onebillion.xprz.mainui.x_counting4to6;

import android.graphics.PointF;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Tracing;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 23/06/16.
 */
public class X_Counting4To6_S6 extends XPRZ_Generic_Tracing
{
    OBGroup imageGroup;


    public X_Counting4To6_S6()
    {
        super(false);
    }

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        if (redraw)
        {
            if (imageGroup != null)
            {
                detachControl(imageGroup);
            }
        }
        List<OBControl> images = filterControls("obj.*");
        imageGroup = new OBGroup(images);
        attachControl(imageGroup);
    }



    public void demo6a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Four cats.
        XPRZ_Generic.pointer_moveToObject(imageGroup, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // We write four like this.
        pointer_demoTrace(false);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Four
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo6c() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Five birds. Six tomatoes.
        XPRZ_Generic.pointer_moveToObject(imageGroup, -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        pointer_demoTrace(true);
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo6e() throws Exception
    {
        demo6c();
    }

}
