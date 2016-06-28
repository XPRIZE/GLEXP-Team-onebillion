package org.onebillion.xprz.mainui.generic;

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


    public XPRZ_Generic_WordsEvent()
    {
        super();
    }


    @Override
    public void setSceneXX (String scene)
    {
        super.setSceneXX(scene);
    }
}
