package org.onebillion.onecourse.mainui.oc_comprehension;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_prepr3.OC_PrepR3;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.controls.OBLabel.OBLABEL_ALIGN_LEFT;
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
    Map<String,OC_PrepR3.Est3_Passage> storyDict;
    String audioPrefix;
    List<OBGroup> answerControls,screenOrderedAnswerControls;
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

    public OBLabel createQuestionLabelFromText(String tx)
    {
        OBControl questionRect = objectDict.get("questionrect");
        OBLabel qlab = new OBLabel(tx,font);
        qlab.setLineSpaceAdd(lineSpacing);

        qlab.setZPosition(10);
        qlab.setColour(Color.BLACK);
        //qlab.setWrapped(true);
        qlab.setMaxWidth(questionRect.width());
        qlab.sizeToBoundingBox();
        qlab.setLeft(questionRect.left());
        qlab.setTop(questionRect.top());
        RectF f = qlab.frame();
        //f.height() += 2; //Hack due to last line being lost
        qlab.setFrame(f);

        return qlab;
    }

    public void setUpQuestion(int questionNo)
    {
        OC_PrepR3.Est3_Question qu = currPassage.questions.get(questionNo);

        OBLabel tempLab = createQuestionLabelFromText("Ay");
        questionLabel = createQuestionLabelFromText(qu.questionText);
        questionLabel.setBottom(tempLab.bottom());
        questionLabel.hide();
        attachControl(questionLabel);

        int idx = 0;
        List<OBGroup> anss = new ArrayList<>();
        OBControl rect = objectDict.get("textrect1");
        OBControl but1 = objectDict.get("button1");
        for(String ansstr : qu.answers)
        {
            OBGroup but = (OBGroup) but1.copy();
            but.show();
            but.objectDict.get("normal").setShadow(0,0.1f,0,applyGraphicScale(3),Color.BLACK);
            OBLabel lab = new OBLabel(ansstr,font);
            lab.setZPosition(10);
            lab.setColour(Color.BLACK);
            //lab.setWrapped(true);
            lab.setAlignment(OBLABEL_ALIGN_LEFT);
            lab.sizeToBoundingBoxMaxWidth(rect.width());
            lab.setLeft(rect.left());
            lab.setTop(rect.top());

            OBGroup gp = new OBGroup(Arrays.asList(but,lab));
            RectF f = new RectF(gp.frame());
            f.right = f.left + (rect.width());
            gp.setFrame(f);
            gp.setBackgroundColor(Color.WHITE);
            anss.add(gp);
            gp.setProperty("answerno",(idx));
            //gp.hide();
            idx++;
        }

        answerControls = anss;

        PointF buttonPos0 = but1.position();
        float yinc = objectDict.get("button2").position() .y - buttonPos0.y;

        float gptop = anss.get(0).top() - but1.height();
        if(questionLabel.bottom() > gptop)
        {
            float diff = gptop - questionLabel.bottom();
            PointF pos = OB_Maths.AddPoints(questionLabel.position() , new PointF(0, diff));
            questionLabel.setPosition(pos);
        }
        idx = 0;
        screenOrderedAnswerControls = OBUtils.randomlySortedArray(anss);
        for(OBControl c : screenOrderedAnswerControls)
        {
            PointF pos = c.position();
            pos.y +=(yinc * idx);
            c.setPosition(pos);
            attachControl(c);
            c.hide();
            idx++;
        }
        List<OBControl> allcontrols = new ArrayList<>(Arrays.asList((OBControl)questionLabel));
        allcontrols.addAll(answerControls);
        float minx = bounds() .width();
        float maxx = 0;
        for(OBControl c : allcontrols)
        {
            if(c.left() < minx)
                minx = c.left();
            if(c.right() > maxx)
                maxx = c.right();
        }
        float margin = (bounds() .width() -(maxx - minx)) / 2;
        float xdiff = margin - questionLabel.left();
        for(OBControl c : allcontrols)
        {
            PointF pos = c.position();
            pos.x += xdiff;
            c.setPosition(pos);
        }
    }

    public void replayAudio()
    {
        if(busyStatuses.contains(status()))
        return;
        if(currentEvent().equals("e"))
        {
            setStatus(status());
            final long stt = statusTime();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    try
                    {
                        showThingsOneByOne();
                        waitForSecs(2f);
                        if(statusTime() == stt)
                            playAudioQueuedScene("PROMPT.REMINDER",false);
                    }
                    catch(Exception  e)
                    {
                    }
                }
            });
        }
        else
            super.replayAudio();
    }

    public void showThingsOneByOne()
    {
        try
        {
            long token = takeSequenceLockInterrupt(true);
            waitForSecs(0.3f);
            checkSequenceToken(token);
            String fn = String.format("%s_%s_q%d_q",audioPrefix,currPassage.passageID,questionNo + 1);
            waitForSecs(0.3f);
            questionLabel.setColour(Color.RED);
            playAudioQueued(Arrays.asList((Object)fn),true);
            questionLabel.setColour(Color.BLACK);
            checkSequenceToken(token);
            waitForSecs(0.2f);
            checkSequenceToken(token);
            OBConditionLock playLock = null;

            for(OBGroup c : screenOrderedAnswerControls)
            {
                OBLabel lab = (OBLabel) c.members.get(1);
                waitForSecs(0.2f);
                checkSequenceToken(token);
                int answerno = (Integer)c.propertyValue("answerno");
                lab.setColour(Color.RED);
                playLock = playAnswerNo(answerno);
                waitAudioQueue(playLock);
                lab.setColour(Color.BLACK);
            }
        }
        catch(Exception e)
        {
            questionLabel.setColour(Color.BLACK);
            for(OBGroup c : screenOrderedAnswerControls)
            {
                OBLabel lab = (OBLabel) c.members.get(1);
                lab.setColour(Color.BLACK);
            }
        }
        sequenceLock.unlock();
    }

     public OBConditionLock playAnswerNo(int answerno) throws Exception
    {
        String fn = String.format("%s_%s_q%d_a_%d",audioPrefix,currPassage.passageID,questionNo + 1,answerno+1);
        return playAudioQueued(Arrays.asList((Object)fn),false);
    }

    public void showMember(String mem,OBGroup qgroup)
    {
        OBGroup button = (OBGroup) qgroup.members.get(0);
        lockScreen();
        for(String s : button.objectDict.keySet())
        {
            if(s.equals(mem))
                button.showMembers(s);
            else
                button.hideMembers(s);
        }
        unlockScreen();
    }

    public void highlightButtonForQuestion(OBGroup qgroup)
    {
        showMember("high",qgroup);
    }

    public void lowlightButtonForQuestion(OBGroup qgroup)
    {
        showMember("normal",qgroup);
    }

    public void dimButtonForQuestion(OBGroup qgroup)
    {
        showMember("dim",qgroup);
        qgroup.setShadowOpacity(0);
    }

    public void feedbackButtonForQuestion(OBGroup qgroup)
    {
        showMember("feedback",qgroup);
        qgroup.setShadowOpacity(0);
    }

    public void bringOnThings()
    {
        try
        {
            waitForSecs(0.3f);
            if(playQuestionAudio)
            {
                String fn = String.format("%s_%s_q%d_q",audioPrefix,currPassage.passageID,questionNo + 1);
                playSfxAudio("newquestion",false);
                questionLabel.show();
                waitForSecs(0.3f);
                playAudioQueued(Arrays.asList((Object)fn),true);

                waitForSecs(0.2f);

                for(OBGroup c : screenOrderedAnswerControls)
                {
                    waitAudioQueue(audioLock);
                    waitForSecs(0.2f);
                    OBControl but = c.members.get(0);
                    OBControl lab = c.members.get(1);
                    lockScreen();
                    but.hide();
                    lab.hide();
                    c.show();
                    unlockScreen();
                    playSfxAudio("mcbuttonon",false);
                    but.show();
                    waitForSecs(0.2f);
                    lab.show();
                    int answerno = (Integer)c.propertyValue("answerno");
                    audioLock = playAnswerNo(answerno);
                }
            }
            else
            {
                playSfxAudio("fullquestion",false);
                lockScreen();
                questionLabel.show();
                for(OBGroup c : screenOrderedAnswerControls)
                {
                    c.show();
                }
                unlockScreen();
            }
        }
        catch(Exception e)
        {

        }
    }

    public void checkArrow()
    {
        try
        {
            objectDict.get("arrow").highlight();
            playSfxAudio("arrowtouch",true);
            objectDict.get("arrow").lowlight();
            waitForSecs(0.1f);
            nextScene();
        }
        catch(Exception e)
        {

        }
    }

    public void popOffQuestion() throws Exception
    {
        playSfxAudio("alloff",false);
        lockScreen();
        for(OBControl c : answerControls)
            c.hide();
        questionLabel.hide();
        unlockScreen();
    }

    public void nextQuestion() throws Exception
    {
        if(eventIndex < events.size() - 1)
            popOffQuestion();
        waitForSecs(0.3f);
        questionNo++;
        nextScene();
    }

    public Object findTarget(PointF pt)
    {
        return(finger(-1,3,targets,pt));
    }

}
