package org.onebillion.xprz.mainui.x_counting7to10;

import android.graphics.Color;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_Tracing;
import org.onebillion.xprz.utils.OBRunnableSyncUI;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class X_Counting7To10_S6 extends XPRZ_Generic_Tracing
{

    public X_Counting7To10_S6()
    {
        super(true);
    }

    public void action_flashDots() throws Exception
    {
        OBGroup dots = (OBGroup) objectDict.get("dots");
        //
        for (int i = 0; i < 5; i++)
        {
            lockScreen();
            dots.hide();
            unlockScreen();
            //
            waitForSecs(0.15);
            //
            lockScreen();
            dots.show();
            unlockScreen();
            //
            waitForSecs(0.15);
        }
    }


    public void tracing_setup()
    {
        new OBRunnableSyncUI()
        {
            public void ex()
            {
                pathColour = Color.BLUE;
                //
                path1 = (OBGroup) objectDict.get("trace_p1");
                //
                uPaths = tracing_processDigit(path1);
                subPaths = new ArrayList();
                subPaths.addAll(tracing_subpathControlsFromPath("trace_p1"));
                //
                path1.hide();
                //
                path2 = (OBGroup) objectDict.get("trace_p2");
                if (path2 != null)
                {
                    uPaths.addAll(tracing_processDigit(path2));
                    subPaths.addAll(tracing_subpathControlsFromPath("trace_p2"));
                    //
                    path2.hide();
                }
                //
                for (OBControl c : subPaths) c.hide();
                //
                dash1 = (OBImage) objectDict.get("dash");
            }
        }.run();
    }



    public void demo6a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Look.
        XPRZ_Generic.pointer_moveToObjectByName("obj", -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Seven Spots on the frog
        waitForSecs(0.3);
        //
        pointer_demoTrace(true); // Seven
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        lockScreen();
        tracing_reset();
        unlockScreen();
        //
        setStatus(STATUS_WAITING_FOR_TRACE);
        doAudio(currentEvent());
    }


    public void demo6b() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        XPRZ_Generic.pointer_moveToObjectByName("obj", -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Eight spots on the butterfly.  Nine spots on the cat.   Ten spots on the fish.
        waitForSecs(0.3);
        //
        pointer_demoTrace(true); // Eight.   Nine.   Ten.
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        lockScreen();
        tracing_reset();
        unlockScreen();
        //
        setStatus(STATUS_WAITING_FOR_TRACE);
        doAudio(currentEvent());
    }


    public void demo6c() throws Exception
    {
        demo6b();
    }


    public void demo6d() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        XPRZ_Generic.pointer_moveToObjectByName("obj", -15, 0.6f, EnumSet.of(XPRZ_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(true); // Eight spots on the butterfly.  Nine spots on the cat.   Ten spots on the fish.
        waitForSecs(0.3);
        //
        pointer_demoTrace(true); // Eight.   Nine.   Ten.
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        lockScreen();
        tracing_reset();
        unlockScreen();
        //
        setStatus(STATUS_WAITING_FOR_TRACE);
        doAudio(currentEvent());
    }
}