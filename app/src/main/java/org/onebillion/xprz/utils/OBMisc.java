package org.onebillion.xprz.utils;

import android.util.ArrayMap;

import org.onebillion.xprz.mainui.XPRZ_SectionController;

import java.util.List;
import java.util.Map;

/**
 * Created by michal on 28/07/16.
 */
public class OBMisc
{
    public static Map<String,Integer> loadEventColours(XPRZ_SectionController controller)
    {

        Map<String,Integer> map = new ArrayMap<>();
        for(String key : controller.eventAttributes.keySet())
        {
            if(key.startsWith("colour_"))
            {
                map.put(key.substring(7),OBUtils.colorFromRGBString(controller.eventAttributes.get(key)));
            }
        }

        return map;
    }

    public static void doSceneAudio(float remindDelay, String event, long statusTime, String postfix, XPRZ_SectionController controller) throws Exception
    {
        String repeat = String.format("PROMPT%s.REPEAT",postfix);
        String prompt = String.format("PROMPT%s",postfix);
        String remind = String.format("REMIND%s",postfix);

        List<String> repeatAudio = controller.getAudioForScene(event,repeat);
        if(repeatAudio != null && repeatAudio.size() > 0)
            controller.setReplayAudio(OBUtils.insertAudioInterval(repeatAudio,300));
        else
            controller.setReplayAudio(OBUtils.insertAudioInterval(controller.getAudioForScene(event,String.format("REPEAT%s",postfix)),300));

        controller.playAudioQueuedScene(event,prompt,300,false);

        List<String> remindAudio = controller.getAudioForScene(event,repeat);
        if(remindDelay > 0 && remindAudio != null && remindAudio.size() > 0)
            controller.reprompt(statusTime,OBUtils.insertAudioInterval(remindAudio,300),remindDelay);

    }


}
