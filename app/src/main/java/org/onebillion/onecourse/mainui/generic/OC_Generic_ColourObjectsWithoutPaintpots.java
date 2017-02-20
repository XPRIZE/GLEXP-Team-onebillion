package org.onebillion.onecourse.mainui.generic;

import android.graphics.Color;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.mainui.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pedroloureiro on 17/01/2017.
 */

public class OC_Generic_ColourObjectsWithoutPaintpots extends OC_Generic_ColourObjects
{
    List<Integer> randomColours;

    public OC_Generic_ColourObjectsWithoutPaintpots()
    {
        super();
    }

    public Boolean value_mustPickCorrectColour()
    {
        return false;
    }

    public String value_colourObjectSFX ()
    {
        return "colour_object";
    }

    public Boolean value_shouldPlayWrongAudio_wrongColourForObject()
    {
        return false;
    }

    public Boolean value_shouldPlayWrongAudioSFX_wrongPaintpotColour()
    {
        return false;
    }

    public Boolean value_shoulPlayWrongAudioSFX_paintpotNotSelected()
    {
        return false;
    }

    public Boolean value_shouldPlayWrongAudioSFX_replacingObjectColour()
    {
        return false;
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        super.action_prepareScene(scene, redraw);
        //
        List controls = filterControls(String.format("%s.*", value_objectPrefix()));
        randomColours = new ArrayList<>();
        //
        for (int i = 0; i < 360; i += 360 / controls.size())
        {
            int index = (int) Math.round(Math.random() * randomColours.size());
            randomColours.add(index, Color.HSVToColor(new float[] {i, (float) (90 + Math.random() * 10), (float) (50 + Math.random() * 10)}));
        }
    }


    public int action_getTotalColouredObjectCount()
    {
        int counter = 0;
        for (OBControl control : filterControls(String.format("%s.*", value_objectPrefix())))
        {
            int controlColour = (int) control.propertyValue("colour");
            if (controlColour != -1) counter++;
        }
        return counter;
    }


    public int action_getMatchingColouredObjectCount(int colour)
    {
        return action_getTotalColouredObjectCount();
    }


    public void action_refreshSelectedColour()
    {
        int counter = action_getTotalColouredObjectCount();
        selectedColour = randomColours.get(counter);
    }


}
