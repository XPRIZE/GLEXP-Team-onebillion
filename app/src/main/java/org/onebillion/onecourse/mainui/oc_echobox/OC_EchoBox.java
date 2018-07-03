package org.onebillion.onecourse.mainui.oc_echobox;

import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.OC_SectionController;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioPlayer;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBGeneralAudioRecorder;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.onebillion.onecourse.utils.OB_Maths.rndom;

public class OC_EchoBox extends OC_SectionController
{
    static int AUDIBLE_THRESHOLD = -30,
        STATUS_RECORDING = 1024,
        STATUS_PLAYING_RECORDING = 1025;

    OBGeneralAudioRecorder recorder;
    //NSTimer timer;
    OBAudioPlayer effectPlayer;
    long timeLastSound,timeRecordingStart,timeFirstSound,sectionStartTime,recordingDuration;
    boolean shouldFlickEars,finalStage;
    List<Integer>wfColours;
    boolean part2;
    Map <String,OBPhoneme>  wordDict;
    Map wordAndSentenceDict;
    List<String>words,sentences;
    float textSize;
    Map<String,Map > sentenceDict;
    OBLabel mainLabel;
    String currKey;

    public Map loadComponent(String comp,String xmlPath)
    {
        Map<String,Map > dict = new HashMap();
        try
        {
            OBXMLNode xmlNode = null;
            if(xmlPath != null)
            {
                OBXMLManager xmlman = new OBXMLManager();
                List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                xmlNode = xl.get(0);
                for(OBXMLNode phrasenode : xmlNode.childrenOfType(comp))
                {
                    Map md = new HashMap();
                    md.putAll(phrasenode.attributes);
                    String k = phrasenode.attributeStringValue("id");
                    String contents = phrasenode.contents;
                    if(contents != null)
                        md.put("contents",contents);
                    dict.put(k,md);
                }
            }
        }
        catch(Exception e)
        {

        }
        return dict;
     }

    public void miscSetup()
    {
        textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetup();
        events = new ArrayList<>(Arrays.asList("a","b","c","d","e","f","g"));
        doVisual(currentEvent());
        processAnchors();
        sectionStartTime = SystemClock.uptimeMillis();

    }

    void processAnchors()
    {

    }
    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void oDict(Map<String,OBControl> od,List<String> s,List<String> h)
    {
        lockScreen();
        for(String str : s)
            od.get(str).show();
        for(String str : h)
            od.get(str).hide();
        unlockScreen();
    }
    public void toucanBlink() throws Exception
    {
        OBGroup toucan =(OBGroup) objectDict.get("toucan");
        oDict(toucan.objectDict,Arrays.asList("eyehalf"),Arrays.asList("eyeopen","eyeclosed"));
        waitForSecs(0.1f);
        oDict(toucan.objectDict,Arrays.asList("eyeclosed"),Arrays.asList("eyeopen","eyehalf"));
        waitForSecs(0.1f);
        oDict(toucan.objectDict,Arrays.asList("eyehalf"),Arrays.asList("eyeopen","eyeclosed"));
        waitForSecs(0.1f);
        oDict(toucan.objectDict,Arrays.asList("eyeopen"),Arrays.asList("eyehalf","eyeclosed"));
    }

    public void toucanBlinker()
    {
        if(!aborting())
        {
            try
            {
                toucanBlink();
                OBUtils.runOnOtherThreadDelayed((float)(10 + rndom() * 5),new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        toucanBlinker();
                    }
                });
            }
            catch(Exception e)
            {
            }
        }
    }

}
