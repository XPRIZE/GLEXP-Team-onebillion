package com.maq.xprize.onecourse.mainui.generic;

import android.graphics.Color;
import android.graphics.Typeface;

import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBUtils;

import java.util.Map;

/**
 * OC_Generic_WordsEvent
 * Generic Event for Literacy Activities. Contains a collection of functions to highlight text
 *
 * Created by pedroloureiro on 28/06/16.
 */
public class OC_Generic_WordsEvent extends OC_Generic_Event
{
    public Map<String,OBPhoneme> wordComponents;
    public Boolean needDemo;
    public float textSize;


    public OC_Generic_WordsEvent()
    {
        super();
    }


    public void miscSetup()
    {
        wordComponents = OBUtils.LoadWordComponentsXML(true);
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
        label.setZPosition(OC_Generic.getNextZPosition(this));
        label.texturise(false, this);
        //
        label.hide();
        label.disable();
        attachControl(label);
        return label;
    }
}
