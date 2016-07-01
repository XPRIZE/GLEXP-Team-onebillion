package org.onebillion.xprz.mainui.generic;

import android.graphics.Color;
import android.graphics.Typeface;

import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;

import java.util.Map;

/**
 * Created by pedroloureiro on 28/06/16.
 */
public class XPRZ_Generic_WordsEvent extends XPRZ_Generic_Event
{
    public Map<String,OBPhoneme> wordComponents;
    public Boolean needDemo;
    public float textSize;


    public XPRZ_Generic_WordsEvent()
    {
        super();
    }


    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
    }


    public void action_setColourForLabel (OBLabel label, int colour)
    {
        label.setColour(colour);
    }


    public void action_highlightLabel (OBLabel label, Boolean high)
    {
        lockScreen();
        if (high) action_setColourForLabel(label, Color.RED);
        else action_setColourForLabel(label, Color.BLACK);
        unlockScreen();
    }


    public OBLabel action_setupLabel (String text)
    {
        Typeface tf = OBUtils.standardTypeFace();
        OBLabel label = new OBLabel(text, tf, applyGraphicScale(textSize));
        label.setColour(Color.BLACK);
        label.setZPosition(XPRZ_Generic.getNextZPosition(this));
        label.texturise(false, this);
        //
        label.hide();
        label.disable();
        attachControl(label);
        return label;
    }
}
