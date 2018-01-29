package org.onebillion.onecourse.mainui.oc_prepm;

import android.graphics.PointF;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.*;

import java.util.List;

/**
 * Created by alan on 29/01/2018.
 */

public class OC_PrepM1 extends OC_SectionController
{
    static int EST1_UNITS = 0,
            EST1_TENS = 1,
            EST1_HUNDREDS = 2;
    PointF textPosLeft,textPosRight;
    float textSize,labScale;
    OBLabel leftLabel,rightLabel;
    OBFont font;
    int nums[] = new int[2];
    int score;
    List leftLabs,rightLabs;
    String wrongMode;
    int testMode;
    int q3;
    float bottomY;

}
