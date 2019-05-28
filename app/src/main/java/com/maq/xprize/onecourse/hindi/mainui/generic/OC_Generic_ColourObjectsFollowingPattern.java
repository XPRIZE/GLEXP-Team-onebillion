package com.maq.xprize.onecourse.hindi.mainui.generic;

import android.graphics.Color;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBPath;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pedroloureiro on 18/01/2017.
 */

public class OC_Generic_ColourObjectsFollowingPattern extends OC_Generic_ColourObjects
{

    Map<String,Integer> colouredTypes;


    public String value_paintPotPrefix()
    {
        return "paint";
    }

    public String value_objectPrefix()
    {
        return "obj";
    }

    public Boolean value_canReplaceColours()
    {
        return false;
    }

    public Boolean value_mustPickCorrectColour()
    {
        return false;
    }

    public String value_colourObjectSFX()
    {
        return "paint_object";
    }

    public String value_selectPaintPotSFX()
    {
        return "paint_select";
    }

    public Boolean value_shouldPlayWrongAudio_wrongColourForObject()
    {
        return true;
    }

    public Boolean value_shouldPlayWrongAudioSFX_wrongPaintpotColour()
    {
        return false;
    }

    public Boolean value_shoulPlayWrongAudioSFX_paintpotNotSelected()
    {
        return true;
    }

    public Boolean value_shouldPlayWrongAudioSFX_replacingObjectColour()
    {
        return false;
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
        int result = action_getTotalColouredObjectCount();
        return result;
    }



    public Boolean action_isColourCorrectForObject(OBControl object)
    {
        action_updateColourTypes();
        //
        Integer correctColourForType = colouredTypes.get(object.attributes().get("type"));
        if (correctColourForType != null)
        {
            return selectedColour == correctColourForType.intValue();
        }
        //
        for (Integer storedValue : colouredTypes.values())
        {
            if (selectedColour == storedValue.intValue()) return false;
        }
        return true;
    }


    public int action_getColorForPaintpot(OBGroup paintpot)
    {
        OBPath frame = (OBPath) paintpot.objectDict.get("colour");
        if (frame != null)
        {
            return frame.fillColor();
        }
        return paintpot.fillColor();
    }


    public void action_updateColourTypes()
    {
        colouredTypes = new HashMap<>();
        //
        for (OBControl control : filterControls(String.format("%s.*", value_objectPrefix())))
        {
            int originalColour = control.fillColor();
            if (originalColour != Color.WHITE)
            {
                String type = (String) control.attributes().get("type");
                if (type != null)
                {
                    colouredTypes.put(type, new Integer(originalColour));
                }
                control.setProperty("colour", originalColour);
            }
            else
            {
                control.setProperty("colour", -1);
            }
        }
    }


    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        if (coloursOfObjects == null)
        {
            coloursOfObjects = new HashMap<>();
        }
        String coloursString = eventAttributes.get("colours");
        if (coloursString != null)
        {
            int i = 0;
            String colours[] = eventAttributes.get("colours").split(",");
            for (OBControl control : sortedFilteredControls(String.format("%s.*", value_paintPotPrefix())))
            {
                int paintpotColour = getColourForString(colours[i]);
                control.setProperty("colour", paintpotColour);
                OC_Generic.colourObject(control, paintpotColour);
                i++;
            }
        }
        else
        {
            for (OBControl control : sortedFilteredControls(String.format("%s.*", value_paintPotPrefix())))
            {
                int paintpotColour = action_getColorForPaintpot((OBGroup)control);
                control.setProperty("colour", paintpotColour);
            }
        }
        action_selectPaintPoint(null);
        action_updateColourTypes();
        //
        correctQuantity = filterControls(String.format("%s.*", value_objectPrefix())).size();
    }
}
