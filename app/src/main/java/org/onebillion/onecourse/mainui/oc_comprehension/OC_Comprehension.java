package org.onebillion.onecourse.mainui.oc_comprehension;

import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_prepr3.OC_PrepR3;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.oc_prepr3.OC_PrepR3.LoadPassagesXML;
import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;

public class OC_Comprehension extends OC_SectionController
{
    OC_PrepR3.Est3_Passage currPassage;
    float textSize;
    OBFont font;
    boolean playQuestionAudio;
    OBPresenter presenter;
    String storyID;
    Map storyDict;
    String audioPrefix;
    List answerControls,screenOrderedAnswerControls;
    OBLabel questionLabel;
    int questionNo;
    OBConditionLock audioLock;
    float lineSpacing;

    public void miscSetUp()
    {
        audioPrefix = "lc";
        storyDict = LoadPassagesXML(getLocalPath("passages.xml"));
        loadEvent("mastera");
        playQuestionAudio = OBUtils.coalesce(parameters.get("quaudio") ,"false").equals("true");
        textSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize") ,"56"));
        lineSpacing = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("linespacing") ,"27"));
        storyID = OBUtils.coalesce(parameters.get("storyid") ,"1");
        font = StandardReadingFontOfSize(textSize);
    }
    public void prepare()
    {
        super.prepare();
        loadFingers();
        miscSetUp();
        doVisual(currentEvent());
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
                catch(Exception exception)
                {
                }
            }
        });
    }

}
