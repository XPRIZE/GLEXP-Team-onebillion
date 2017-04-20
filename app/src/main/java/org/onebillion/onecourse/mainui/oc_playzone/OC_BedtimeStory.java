package org.onebillion.onecourse.mainui.oc_playzone;

import org.onebillion.onecourse.mainui.OC_SectionController;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alan on 20/04/2017.
 */

public class OC_BedtimeStory extends OC_SectionController
{
    public void miscSetUp()
    {

    }
    
    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList<>();
        events.addAll(Arrays.asList("a"));
        doVisual(currentEvent());
    }

}
