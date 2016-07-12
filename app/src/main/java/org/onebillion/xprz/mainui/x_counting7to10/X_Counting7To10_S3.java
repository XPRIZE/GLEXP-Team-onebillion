package org.onebillion.xprz.mainui.x_counting7to10;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.mainui.generic.XPRZ_Generic_SelectCorrectObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 11/07/16.
 */
public class X_Counting7To10_S3 extends XPRZ_Generic_SelectCorrectObject
{
    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        List<OBGroup> controls = (List<OBGroup>) (Object) filterControls("obj.*");
        for (OBGroup control : controls)
        {
            List<OBControl> petal_layers = control.filterMembers("petals.*");
            for (OBControl layer : petal_layers)
            {
                layer.hide();
            }
            OBControl layer = control.objectDict.get(String.format("petals_%s", control.attributes().get("petals")));
            if (layer != null) layer.show();
        }
    }


    @Override
    public void action_answerIsCorrect (OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        waitForSecs(0.3);
        //
        action_lowlight(target);
        waitForSecs(0.3);
        //
        playSceneAudio("CORRECT", true);
        if (audioSceneExists("FINAL"))
        {
            waitForSecs(0.3);
            playSceneAudio("FINAL", true);
        }
        else
        {
            waitForSecs(0.3);
        }
        //
        nextScene();
    }


    @Override
    public void action_highlight (OBControl control) throws Exception
    {
        lockScreen();
        OBGroup group = (OBGroup) control;
        List<OBControl> controls = new ArrayList(group.objectDict.values());
        if (controls.size() > 0)
        {
            Boolean layerChanged = false;
            for (OBControl c : controls)
            {
                if (c.attributes().get("id") != null)
                {
                    layerChanged = true;
                    control.highlight();
                }
            }
            if (!layerChanged)
            {
                control.highlight();
            }
        }
        else
        {
            control.highlight();
        }
        unlockScreen();
    }


    @Override
    public void action_lowlight (OBControl control) throws Exception
    {
        lockScreen();
        OBGroup group = (OBGroup) control;
        List<OBControl> controls = new ArrayList(group.objectDict.values());
        if (controls.size() > 0)
        {
            Boolean layerChanged = false;
            for (OBControl c : controls)
            {
                if (c.attributes().get("id") != null)
                {
                    layerChanged = true;
                    control.lowlight();
                }
            }
            if (!layerChanged)
            {
                control.lowlight();
            }
        }
        else
        {
            control.lowlight();
        }
        unlockScreen();
    }


    @Override
    public String action_getObjectPrefix ()
    {
        return "obj";
    }
}