package org.onebillion.onecourse.mainui.oc_onsetandrime;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.oc_reading.OC_Reading;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUserPressedBackException;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller.boundingBoxForText;
import static org.onebillion.onecourse.utils.OBAudioManager.AM_MAIN_CHANNEL;
import static org.onebillion.onecourse.utils.OBUtils.LoadWordComponentsXML;
import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;

/**
 * Created by alan on 15/03/2018.
 */

public class OC_Onset extends OC_Reading
{
}
