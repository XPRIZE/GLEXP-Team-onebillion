package org.onebillion.onecourse.mainui.oc_echobox;

import org.onebillion.onecourse.mainui.OC_SectionController;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OC_EchoBox extends OC_SectionController
{
    static int AUDIBLE_THRESHOLD = -30,
        STATUS_RECORDING = 1024,
        STATUS_PLAYING_RECORDING = 1025;

}
