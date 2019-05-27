package com.maq.xprize.onecourse.mainui.oc_diagnostics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pedroloureiro on 02/05/2018.
 */

public class OC_DiagnosticsQuestion
{
    public String eventUUID;
    public List correctAnswers;
    public List<String> distractors;
    public List unitsUsed;
    public Map<String, Object> additionalInformation;

    public OC_DiagnosticsQuestion(String eventUUID, List correctAnswers, List distractors, List unitsUsed)
    {
        this.eventUUID = eventUUID;
        this.correctAnswers = correctAnswers;
        this.distractors = distractors;
        this.unitsUsed = unitsUsed;
        this.additionalInformation = new HashMap<String, Object>();
    }
}
