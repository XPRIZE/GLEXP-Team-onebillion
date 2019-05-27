package com.maq.xprize.onecourse.mainui.generic;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 17/01/2017.
 */

public class OC_Generic_ColourObjects extends OC_Generic_Event
{
    public int selectedColour;
    public int correctQuantity;
    public int correctColour;
    //
    public Map<String,Integer> coloursOfObjects;




    public OC_Generic_ColourObjects()
    {
        super();
    }


    public String value_paintPotPrefix()
    {
        return "paintpot";
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
        return true;
    }

    public String value_colourObjectSFX()
    {
        return "colourObject";
    }

    public String value_selectPaintPotSFX()
    {
        return "selectColour";
    }

    public Boolean value_shouldPlayWrongAudio_wrongColourForObject()
    {
        return true;
    }

    public Boolean value_shouldPlayWrongAudioSFX_wrongPaintpotColour()
    {
        return true;
    }

    public Boolean value_shoulPlayWrongAudioSFX_paintpotNotSelected()
    {
        return true;
    }

    public Boolean value_shouldPlayWrongAudioSFX_replacingObjectColour()
    {
        return true;
    }

    public Boolean value_mustColourObjectCorrectly()
    {
        return true;
    }

    public Boolean action_isColourCorrectForObject(OBControl object)
    {
        return true;
    }



    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        MainActivity.log(String.format("OC_Generic_ColourObjects %s", (redraw) ? "redrawing" : "keeping scene"));
        //
        super.action_prepareScene(scene, redraw);
        //
        Boolean resetColours = eventAttributes.get("resetColours") != null && eventAttributes.get("resetColours").equals("true");
        //
        if (coloursOfObjects == null || redraw || resetColours)
        {
            coloursOfObjects = new HashMap<String,Integer>();
        }
        //
        for (OBControl control : filterControls(String.format("%s.*", value_objectPrefix())))
        {
            Integer colourInDictionary = coloursOfObjects.get(control.attributes().get("id"));
            if (!redraw && colourInDictionary != null)
            {
                OC_Generic.colourObject(control, colourInDictionary.intValue());
                control.setProperty("colour", colourInDictionary.intValue());
            }
            else
            {
                OC_Generic.colourObject(control, Color.WHITE);
                control.setProperty("colour", -1);
            }
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
                int paintpotColour = control.fillColor();
                control.setProperty("colour", paintpotColour);
            }
        }
        action_selectPaintPoint(null);
        correctColour = getColourForString(eventAttributes.get("correctColour"));
        //
        correctQuantity = -1;
        String correctNumberString = eventAttributes.get("correctNumber");
        if (correctNumberString == null)
        {
            correctNumberString = eventAttributes.get("correct_quantity");
        }
        if (correctNumberString != null)
        {
            correctQuantity = Integer.parseInt(correctNumberString);
        }
    }


    public int getColourForString(String colourName)
    {
        if (colourName == null)
        {
            return -1;
        }
        else if (colourName.compareTo("red") == 0)
        {
            return Color.RED;
        }
        else if (colourName.compareTo("green") == 0)
        {
            return Color.GREEN;
        }
        else if (colourName.compareTo("blue") == 0)
        {
            return Color.BLUE;
        }
        else if (colourName.compareTo("yellow") == 0)
        {
            return Color.YELLOW;
        }
        else if (colourName.compareTo("pink") == 0)
        {
            return OBUtils.colorFromRGBString("255,128,255");
        }
        else
        {
            return Color.LTGRAY;
        }
    }



    public void action_selectPaintPoint(OBGroup selectedPaintPot)
    {
        if (selectedPaintPot == null)
        {
            selectedColour = -1;
        }
        for (OBGroup paintpot : (List<OBGroup>) (Object) filterControls(String.format("%s.*", value_paintPotPrefix())))
        {
            paintpot.objectDict.get("selector_frame").setHidden(!paintpot.equals(selectedPaintPot));
            if (paintpot.equals(selectedPaintPot))
            {
                selectedColour = (int) paintpot.propertyValue("colour");
            }
        }
    }



    public int action_getMatchingColouredObjectCount(int colour)
    {
        int counter = 0;
        for (OBControl control : filterControls(String.format("%s.*", value_objectPrefix())))
        {
            int controlColour = (int) control.propertyValue("colour");
            if (colour == controlColour) counter++;
        }
        return counter;
    }


    public void action_colourObjectWithSelectedColour(OBControl object)
    {
        lockScreen();
        OC_Generic.colourObject(object, selectedColour);
        object.setProperty("colour", selectedColour);
        unlockScreen();
        //
        coloursOfObjects.put((String)object.attributes().get("id"), Integer.valueOf(selectedColour));
    }


    public void action_refreshSelectedColour()
    {
        // do nothing for this case
    }

    public void action_playColourObjectSoundEffect(boolean wait)
    {
        try
        {
            playSfxAudio(value_colourObjectSFX(), wait);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_ColourObjects:action_playColourObjectSoundEffect:exception caught");
            e.printStackTrace();
        }
    }

    public void action_playSelectPaintpotSoundEffect(boolean wait)
    {
        try
        {
            playSfxAudio(value_selectPaintPotSFX(), wait);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_ColourObjects:action_playColourObjectSoundEffect:exception caught");
            e.printStackTrace();
        }
    }





    public OBControl findPaintPot (PointF pt)
    {
        targets = filterControls(String.format("%s.*", value_paintPotPrefix()));
        return super.findTarget(pt);
    }


    public OBControl findObject (PointF pt)
    {
        targets = filterControls(String.format("%s.*", value_objectPrefix()));
        return super.findTarget(pt);
    }





    public void checkPaintPot(OBControl paintpot)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            int paintpotColour = (int) paintpot.propertyValue("colour");
            if (value_mustPickCorrectColour() && paintpotColour != correctColour)
            {
                if (value_shouldPlayWrongAudioSFX_wrongPaintpotColour())
                {
                    playSfxAudio("wrong", false);
                }
                OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        playSceneAudio("INCORRECT", false);
                    }
                });
            }
            else
            {
                lockScreen();
                action_playSelectPaintpotSoundEffect(false);
                action_selectPaintPoint((OBGroup) paintpot);
                unlockScreen();
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_CountingTo3_S2:checkObject:exception caught");
            e.printStackTrace();
            setStatus(STATUS_AWAITING_CLICK);
        }
    }



    public void checkObject(OBControl object)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            action_refreshSelectedColour();
            //
            if (selectedColour == -1)
            {
                // colour hasnt been selected
                if (value_shoulPlayWrongAudioSFX_paintpotNotSelected())
                {
                    playSfxAudio("wrong", false);
                }
                OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        playSceneAudio("INCORRECT", false);
                    }
                });
                setStatus(STATUS_AWAITING_CLICK);
            }
            else
            {
                int objectColour = (int) object.propertyValue("colour");
                if (objectColour != -1 && !value_canReplaceColours())
                {
                    // cannot replace the existing colour
                    if (value_shouldPlayWrongAudioSFX_replacingObjectColour())
                    {
                        playSfxAudio("wrong", false);
                    }
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            playSceneAudio("INCORRECT", false);
                        }
                    });
                    setStatus(STATUS_AWAITING_CLICK);
                }
                else
                {
                    if (value_mustColourObjectCorrectly() && ! action_isColourCorrectForObject(object))
                    {
                        // wrong colour for the object
                        if (value_shouldPlayWrongAudio_wrongColourForObject())
                        {
                            playSfxAudio("wrong", false);
                        }
                        OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                        {
                            @Override
                            public void run () throws Exception
                            {
                                playSceneAudio("INCORRECT", false);
                            }
                        });
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                    else
                    {
                        action_playColourObjectSoundEffect(false);
                        action_colourObjectWithSelectedColour(object);
                        //
                        if (action_getMatchingColouredObjectCount(selectedColour) == correctQuantity)
                        {
                            waitForSecs(0.3);
                            //
                            gotItRightBigTick(true);
                            waitForSecs(0.3);
                            //
                            playSceneAudio("CORRECT", true);
                            if (audioSceneExists("FINAL"))
                            {
                                waitForSecs(0.3);
                                playSceneAudio("FINAL", true);
                            }
                            waitForSecs(0.3);
                            //
                            nextScene();
                        }
                        else
                        {
                            setStatus(STATUS_AWAITING_CLICK);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_ColourObjects:checkObject:exception caught");
            e.printStackTrace();
            setStatus(STATUS_AWAITING_CLICK);
        }
    }





    @Override
    public void touchDownAtPoint (PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl paintpot = findPaintPot(pt);
            if (paintpot != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        checkPaintPot(paintpot);
                    }
                });
            }
            else
            {
                final OBControl object = findObject(pt);
                if (object != null)
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            checkObject(object);
                        }
                    });
                }
            }
        }
    }
}
