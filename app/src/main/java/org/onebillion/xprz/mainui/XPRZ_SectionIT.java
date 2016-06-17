package org.onebillion.xprz.mainui;

import android.graphics.Color;
import android.graphics.Typeface;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBUtils;

import java.util.Arrays;

/**
 * Created by alan on 17/06/16.
 */
public class XPRZ_SectionIT extends XPRZ_SectionController
{
    public void prepare()
    {
        super.prepare();
        lockScreen();
        loadFingers();
        loadEvent("mastera");
        String[] eva = ((String)eventAttributes.get("scenes")).split(",");
        events = Arrays.asList(eva);
        doVisual(currentEvent());
        unlockScreen();
    }

    public long switchStatus(String scene)
    {
        if (Arrays.asList("it6","it7").contains(scene))
            return setStatus(STATUS_WAITING_FOR_DRAG);
        if (Arrays.asList("it9").contains(scene))
            return setStatus(STATUS_AWAITING_ARROW_CLICK);
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
            }});
    }

    public void setSceneXX(String scene)
    {
        if (Arrays.asList("it1","it6","it9").contains(scene))
            deleteControls(".*");
        setSceneXX(scene);
        targets = filterControls("obj.*");
    }

}
