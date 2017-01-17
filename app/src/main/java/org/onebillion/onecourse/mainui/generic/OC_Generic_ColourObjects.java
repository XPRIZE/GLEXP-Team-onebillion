package org.onebillion.onecourse.mainui.generic;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.utils.OBUtils;

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
    int selectedColour;
    int correctColour, correctQuantity;
    Map<String,Integer> coloursOfObjects;

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

    @Override
    public void action_prepareScene (String scene, Boolean redraw)
    {
        MainActivity.log(String.format("OC_Generic_ColourObjects %s", (redraw) ? "redrawing" : "keeping scene"));
        //
        super.action_prepareScene(scene, redraw);
        //
        if (coloursOfObjects == null || redraw)
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
        int i = 0;
        String colours[] = eventAttributes.get("colours").split(",");
        for (OBControl control : sortedFilteredControls(String.format("%s.*", value_paintPotPrefix())))
        {
            int paintpotColour = getColourForString(colours[i]);
            control.setProperty("colour", paintpotColour);
            OC_Generic.colourObject(control, paintpotColour);
            i++;
        }
        action_selectPaintPoint(null);
        correctColour = getColourForString(eventAttributes.get("correctColour"));
        //
        correctQuantity = -1;
        String correctNumberString = eventAttributes.get("correctNumber");
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
                playSfxAudio("wrong", false);
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
                playSfxAudio("selectColour", false);
                action_selectPaintPoint((OBGroup) paintpot);
                unlockScreen();
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception e)
        {
            MainActivity.log("OC_CountingTo3_S2:checkObject:exception caught");
            setStatus(STATUS_AWAITING_CLICK);
        }
    }



    public void checkObject(OBControl object)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            if (selectedColour == -1)
            {
                // colour hasnt been selected
                playSfxAudio("wrong", false);
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
                    playSfxAudio("wrong", false);
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
                    playSfxAudio("colourObject", false);
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
        catch (Exception e)
        {
            MainActivity.log("OC_Generic_ColourObjects:checkObject:exception caught");
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
