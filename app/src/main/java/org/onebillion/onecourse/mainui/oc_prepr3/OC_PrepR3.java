package org.onebillion.onecourse.mainui.oc_prepr3;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.onebillion.onecourse.mainui.oc_reading.OC_Reading;
import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;
import static org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller.boundingBoxForText;
import static org.onebillion.onecourse.utils.OBReadingWord.WORD_SPEAKABLE;

/**
 * Created by alan on 22/01/2018.
 */

public class OC_PrepR3 extends OC_Reading
{
    class Est3_Question
    {
        String questionText,questionID;
        int sentenceIndex;
        List<String> answers;
    }
    class Est3_Passage
    {
        String passageID;
        List<String> sentences;
        List<Est3_Question> questions;
    }

    static int MARGIN = 24;
    Map <String,Est3_Passage>passageDict;
    List<String>passages;
    Est3_Passage currPassage;
    OBFont font;
    OBLabel passageLabel;
    int questionNo;
    List answerButtons;
    int score,totalQuestions;
    int highlightedIdx;
    int hiColour,loColour;
    OBLabel layOutBox;
    OBGroup textGroup;
    String audioPrefix;
    boolean manualLayoutMode = true;
    List<List<OBReadingWord>> laidOutLines;
    List<OBControl>backingRects;
    List<OBControl>randomQs;
    String collectionPassageID;
    int collectionQuestionNo,collectionAnswerNo;

    Map LoadPassagesXML(String xmlPath)
    {
        Map dict = new HashMap();
        try
        {
            if(xmlPath != null)
            {
                OBXMLManager xmlman = new OBXMLManager();
                OBXMLNode root = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath)).get(0);

                for(OBXMLNode cqNode : root.children)
                {
                    Est3_Passage passage = new Est3_Passage();
                    passage.passageID = (cqNode.attributeStringValue("id"));

                    List<OBXMLNode>passagesNodes = cqNode.childrenOfType("passages");
                    if(passagesNodes.size() == 0)
                        throw new Exception(String.format("Passages missing for %",passage.passageID));

                    List<OBXMLNode>passageNodes = passagesNodes.get(0).childrenOfType("passage");
                    if(passageNodes.size() == 0)
                        throw new Exception(String.format("Passage missing for %s",passage.passageID));
                    List<String>strings = new ArrayList<>();
                    for(OBXMLNode passageNode : passageNodes)
                        strings.add(passageNode.contents);
                    passage.sentences = strings;
                    List questions = new ArrayList<>();
                    passage.questions = questions;
                    for(OBXMLNode quNode : cqNode.childrenOfType("qu"))
                    {
                        List<OBXMLNode>questionNodes = quNode.childrenOfType("question");
                        if(questionNodes.size() == 0)
                            throw new Exception(String.format("Question missing for %s.%s",passage.passageID,quNode.attributeStringValue("id")));
                        OBXMLNode questionNode = questionNodes.get(0);
                        Est3_Question question = new Est3_Question();
                        questions.add(question);
                        question.questionID = (questionNode.attributeStringValue("id"));
                        question.questionText = (questionNode.contents);
                        List answers = new ArrayList<>();
                        question.answers = (answers);
                        for(OBXMLNode answerNode : quNode.childrenOfType("answer"))
                        {
                            answers.add(answerNode.contents);
                        }
                        List<OBXMLNode>correctNodes = quNode.childrenOfType("correct");
                        if(correctNodes.size() == 0)
                            throw new Exception(String.format("Correct reference missing for %s.%s",passage.passageID,quNode.attributeStringValue("id")));
                        question.sentenceIndex = Integer.parseInt(correctNodes.get(0).contents);
                    }
                    dict.put(passage.passageID,passage);
                }
            }
        }
        catch(Exception e)
        {
        }
        return dict;
    }

    public boolean showNextButton()
    {
        return false;
    }

    public boolean showRAButton()
    {
        return true;
    }

    public void miscSetUp()
    {
        passageDict = LoadPassagesXML(getLocalPath("passages.xml"));
        loadEvent("mastera");
        hiColour = objectDict.get("hiswatch").fillColor();
        String qcard = OBUtils.coalesce(parameters.get("qcard") ,"2,3,4,5");
        List qcs = new ArrayList<>();
        for(String s : qcard.split(","))
            qcs.add(Integer.parseInt(s));
        List<String> passingKeys = new ArrayList<>();
        for(String k : passageDict.keySet())
        {
            Est3_Passage p = passageDict.get(k);
            if(qcs.contains(p.questions.size()))
                passingKeys.add(k);
        }
        int questionCount = 5;
        String noqs = parameters.get("noqs");
        if(noqs != null)
            questionCount = Integer.parseInt(noqs);
        passages = OBUtils.randomlySortedArray(passingKeys).subList(0,Math.min(questionCount,passingKeys.size()));
        highlightedIdx = -1;
    }

    public void setUpEvents()
    {
        events = new ArrayList<>(Arrays.asList("intro,b".split(",")));
     }

    public void prepare()
    {
        theMoveSpeed = bounds() .width();
        textJustification = TEXT_JUSTIFY_CENTRE;
        initialised = true;
        processParams();
        eventsDict = loadXML(getConfigPath(String.format("%s.xml",sectionName())));
        loadAudioXML(getConfigPath(String.format("%saudio.xml",sectionAudioName())));
        loadFingers();
        initialised = true;
        miscSetUp();
        setUpEvents();
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
                catch(Exception exception) {
                }
            }
        });
    }

    public void runThroughAnswers()
    {
        try
        {
            playAudioForQuestion(questionNo);
            waitForSecs(0.3f);
            showAnswers(randomQs);
            setStatus(STATUS_WAITING_FOR_ANSWER);
        }
        catch(Exception e)
        {
        }
    }

    public void replayAudio()
    {
        if(busyStatuses.contains(status()))
            return;
        if(status() == STATUS_WAITING_FOR_ANSWER)
        {
            setStatus(STATUS_CHECKING);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    runThroughAnswers();
                }
            });
        }
        else
        {
            if(_replayAudio != null)
            {
                setStatus(status());
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        _replayAudio();
                    }
                });
            }
        }
    }

    public void fin()
    {
        showScoreHammerScore((int)(score * 10.0/totalQuestions),false);
    }


    public void clearAll()
    {
        deleteControls(".*");
        if(textGroup != null)
        {
            detachControl(textGroup);
            textGroup = null;
        }
        detachControls(answerButtons);
        answerButtons = null;
    }

    public List<OBReadingWord> layOutLine(List<OBReadingWord>wordarr, float leftEdge, float rightEdge, float y, int justification, OBFont fnt, String paraText)
    {
        List<OBReadingWord>words = new ArrayList<>(wordarr);
        List<OBReadingWord>outWordList = new ArrayList<>();
        boolean lastLine = false;
        if (!words.isEmpty())
        {
            OBReadingWord w = words.get(words.size()-1);
            lastLine = (w.index + w.text.length() >= paraText.length());
        }
        while (!words.isEmpty() && words.get(words.size()-1).text.startsWith(" "))
            words.remove(words.size()-1);
        if (words.isEmpty())
            return outWordList;
        numberOfTextLines++;
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(fnt.size);
        textPaint.setTypeface(fnt.typeFace);
        float lineStart;
        OBReadingWord w0 = words.get(0);
        lineStart = leftEdge;
        int noSpaces = 0;
        for (OBReadingWord w : words)
        {
            OBLabel lab;
            lab = new OBLabel(w.text,fnt);
            lab.setColour(Color.BLACK);
            float widthUpToWord = textPaint.measureText(paraText.substring(w0.index,w.index));
            RectF f = new RectF(lab.frame());
            f.offset(-f.left,-f.top);
            f.offset(lineStart + widthUpToWord,y);
            lab.setFrame(f);
            if (w.text.endsWith(" "))
                noSpaces++;
            else
            {
                float wdth = textPaint.measureText(paraText.substring(w0.index,w.index + w.text.length()));
                lab.setRight(wdth + lineStart);
            }
            textGroup.insertMember(lab,0,"");
            if (lab.bottom() > textGroup.bounds().bottom)
            {
                float diff = lab.bottom() - textGroup.bounds().bottom;
                RectF tgf = new RectF(textGroup.frame());
                tgf.bottom += diff;
                textGroup.setFrame(tgf);
            }
            w.frame = new RectF(lab.frame());
            w.label = lab;
            w.homePosition = new PointF(lab.position().x,lab.position().y);
            if ((w.flags & WORD_SPEAKABLE) != 0)
                lab.setZPosition(LABEL_ZPOS);
            else
                lab.setZPosition(LABEL_ZPOS - 3);
            outWordList.add(w);
        }
        if (!lastLine)
        {
            OBReadingWord w = outWordList.get(words.size()-1);
            float spaceToFill = textGroup.width() - w.label.right();
            if (spaceToFill > 0.0f)
            {
                float spinc = spaceToFill / noSpaces;
                float shift = 0.0f;
                for (OBReadingWord rw : outWordList)
                {
                    OBLabel lab = rw.label;
                    PointF pos = new PointF(lab.position().x,lab.position().y);
                    pos.x += shift;
                    if (rw.text.endsWith(" "))
                    {
                        shift += spinc;
                        pos.x += spinc;
                    }
                    lab.setPosition(pos);
                }
            }
        }
        return outWordList;
    }

    List<List<OBReadingWord>> layOutManually(String text)
    {
        OBReadingPara para = new OBReadingPara(text,1);
        float lineHeight = font.size;
        lineHeightMultiplier = 1.2f;
        float topY = 0;
        float rightEdge = textGroup.width();

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(font.size);
        textPaint.setTypeface(font.typeFace);

        List<OBReadingWord> currLine = new ArrayList<>();
        PointF pos = new PointF(0, topY);
        int lineStartIndex = 0;
        float lineStart = 0;
        List<List<OBReadingWord>> outlines = new ArrayList<>();
        for(OBReadingWord w : para.words)
        {
            float sizew = textPaint.measureText(para.text.substring(lineStartIndex,w.index + w.text.length()));
            if ((w.flags & WORD_SPEAKABLE)!=0 && sizew + lineStart > rightEdge)
            {
                OBReadingWord lastw = currLine.get(currLine.size()-1);
                if (IsLeftHanger(lastw.text))
                    currLine.remove(currLine.size()-1);
                else
                    lastw = null;
                outlines.add(layOutLine(currLine,0,rightEdge,topY,textJustification,font,para.text));
                currLine.clear();
                if (lastw != null)
                    currLine.add(lastw);
                topY += (lineHeight * lineHeightMultiplier);
                pos.y = topY;
                lineStart = 0;
                lineStartIndex = (int)w.index;
            }
            currLine.add(w);
        }
        if (currLine.size() > 0)
            outlines.add(layOutLine(currLine,0,rightEdge,topY,textJustification,font,para.text));

        return outlines;
    }

    static boolean rangesIntersect(int r1s,int r1e,int r2s,int r2e)
    {
        return r2s >= r1s && r2s < r1e;
    }

    List<OBControl> createBackingRects(List<List<OBReadingWord>> lineList)
    {
        List<OBControl> rects = new ArrayList();
        if (highlightedIdx < 0)
            return rects;
        int lenSoFar = 0,highlightstart=-1,highlightend=-1;
        int i = 0;
        for(String s : currPassage.sentences)
        {
            if(lenSoFar > 0)
                lenSoFar++;
            if(i == highlightedIdx)
            {
                highlightstart = lenSoFar;
            }
            lenSoFar += s.length();
            if(i == highlightedIdx)
            {
                highlightend = lenSoFar;
            }
            i++;
        }
        for (List<OBReadingWord>line : lineList)
        {
            RectF unionRect = new RectF();
            for (OBReadingWord rw : line)
            {
                if (rangesIntersect(highlightstart,highlightend,rw.index,rw.index +rw.text.length()) )
                {
                    unionRect.union(rw.label.frame());
                }
            }
            OBControl rect = new OBControl();
            rect.setFrame(unionRect);
            rect.setFillColor(hiColour);
            textGroup.insertMember(rect,0,"rect");
            rects.add(rect);
        }
        return rects;
    }

    public void setUpPassage()
    {
        clearAll();
        loadEvent("passage");
        String passageID = passages.get(currNo);
        collectionPassageID = passageID;
        currPassage = passageDict.get(passageID);
        fontSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize") ,"40"));
        font = OBUtils.StandardReadingFontOfSize(fontSize);

        StringBuilder passageText = new StringBuilder();
        for(String s : currPassage.sentences)
        {
            if(passageText.length() > 0)
                passageText.append(" ");
            passageText.append(s);
        }
        OBControl textBox = objectDict.get("textbox");
        if (manualLayoutMode)
        {
            textGroup = new OBGroup(new ArrayList<OBControl>(),objectDict.get("textbox").frame());
            laidOutLines = layOutManually(passageText.toString());
            backingRects = createBackingRects(laidOutLines);
            attachControl(textGroup);
        }
        else
        {
            RectF f = new RectF(textBox.frame());
            layOutBox = new OBLabel(passageText.toString(),font);
            layOutBox.setJustification(OBTextLayer.JUST_LEFT);
            layOutBox.setFrame(f);
            layOutBox.setMaxWidth(f.width());
            attachControl(layOutBox);
        }


        objectDict.get("arrow").hide();
    }

    public void setSceneintro()
    {
        setUpPassage();
    }

    public void setScenefeedback()
    {
        highlightedIdx = currPassage.questions.get(questionNo) .sentenceIndex - 1;
        setUpPassage();
        for (OBControl c : backingRects)
            c.hide();
    }

    public void setSceneb() throws Exception
    {
     }

    public void doMainb()
    {
        OBUtils.runOnOtherThreadDelayed(10,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                if(status() == STATUS_IDLE)
                {
                    setStatus(STATUS_AWAITING_ARROW_CLICK);
                    objectDict.get("arrow").show();
                    setReplayAudio((List)getAudioForScene("arrow","PROMPT"));
                    reprompt(statusTime(),(List)((Map)audioScenes.get("arrow")).get("PROMPT"),50);
                }
            }
        });
    }
    String firstWordOfText(String tx)
    {
        int loc = tx.indexOf(" ");
        if(loc < 0)
            return tx;
        return tx.substring(loc);
    }

    public PointF positionForFirstWord()
    {
        OBReadingWord rw = laidOutLines.get(0).get(0);
        PointF pos = rw.label.position();
        return convertPointFromControl(pos,textGroup);
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_IDLE);
    }

    public void demointro() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        audioPrefix = currentAudio("DEMO").get(0);
        int r = audioPrefix.indexOf("_");
        audioPrefix = audioPrefix.substring(0,r);
        playAudioQueuedScene("DEMO",0.4f,true);
        waitForSecs(0.4f);
        PointF destpt = positionForFirstWord();
        PointF startpt = pointForDestPoint(destpt,25);
        loadPointerStartPoint(startpt,destpt);
        movePointerToPoint(destpt,-1,true);
        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.4f);
        thePointer.hide();
        setReplayAudio((List)currentAudio("DEMO2"));
        nextScene();
    }

    public void demofeedback() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO",true);
        lockScreen();
        for (OBControl c : backingRects)
            c.show();
        unlockScreen();
        playSfxAudio("tap",false);
        waitForSecs(0.7f);
        Est3_Question qu = currPassage.questions.get(questionNo);
        int sno = qu.sentenceIndex;
        playAudioForSentence(sno);
        waitForSecs(1f);
        nextScene();
    }

    public void clearPassage()
    {
        lockScreen();
        detachControl(textGroup);
        textGroup = null;
        deleteControls("arrow");
        unlockScreen();
    }

    public void preQuestion() throws Exception
    {
        playAudioQueued((List)((Map)audioScenes.get("prequestion")).get("DEMO"),true);
    }

    public void playAudioForQuestion(int qno) throws Exception
    {
        Est3_Question qu = currPassage.questions.get(questionNo);
        int r = qu.questionID.indexOf(".");
        String qid = qu.questionID.substring(0,r);
        String fn = String.format("%s_%s_%s_q",audioPrefix,currPassage.passageID,qid);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public void playAudioForQuestion(int qno,int ano) throws Exception
    {
        Est3_Question qu = currPassage.questions.get(questionNo);
        int r = qu.questionID.indexOf(".");
        String qid = qu.questionID.substring(0,r);
        String fn = String.format("%s_%s_%s_a_%d",audioPrefix,currPassage.passageID,qid,ano);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public void playAudioForSentence(int sno) throws Exception
    {
        String fn = String.format("%s_%s_p_p_%d",audioPrefix,currPassage.passageID,sno);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public void checkLabelBoxWidthForQuestion(Est3_Question qu)
    {
        OBControl labelBox = objectDict.get("labelbox");
        float sp = applyGraphicScale(MARGIN);
        float w = labelBox.width() - sp * 2;
        for(String ansstr : qu.answers)
        {
            RectF bb = boundingBoxForText(ansstr, font);
            if(bb.width() > w)
            {
                w = bb.width();
            }
        }
        if(w > labelBox.width() - sp * 2)
        {
            RectF f = labelBox.bounds();
            PointF pos = labelBox.position();
            Map<Object,String> attrs = (Map)labelBox.settings.get("attrs");
            float cornerRadius = Float.parseFloat(attrs.get("cornerradius"));
            cornerRadius *= f.height();
            f.right = f.left + (w + sp * 2);
            Path p = new Path();
            p.addRoundRect(f, cornerRadius, cornerRadius, Path.Direction.CCW);

            OBPath obp = new OBPath();
            obp.setPath(p);
            obp.setFillColor(labelBox.fillColor());
            obp.setStrokeColor(labelBox.strokeColor());
            obp.setZPosition(labelBox.zPosition());
            obp.sizeToBoundingBox();
            obp.setPosition(pos);
            detachControl(labelBox);
            objectDict.put("labelbox",obp);
        }
    }

    public List setUpQuestion(int i)
    {
        clearAll();
        collectionQuestionNo = i;
        loadEvent("questions");
        Est3_Question qu = currPassage.questions.get(questionNo);
        checkLabelBoxWidthForQuestion(qu);
        OBControl labelBox = objectDict.get("labelbox");
        loColour = labelBox.fillColor();
        RectF fr = objectDict.get("textbox").frame();
        float y = fr.top;
        float bottom = (fr.bottom);
        float interspace = ((bottom - y) - qu.answers.size() * labelBox.height()) /(qu.answers.size() + 1);
        y +=(labelBox.height() / 2);
        y += interspace;
        List anss = new ArrayList<>();
        int idx = 0;
        for(String ansstr : qu.answers)
        {
            OBControl backing = labelBox.copy();
            OBGroup gp = new OBGroup(Arrays.asList(backing));
            anss.add(gp);

            OBLabel lab = new OBLabel(ansstr,font);
            lab.setZPosition(10);
            lab.setColour(Color.BLACK);
            lab.setPosition(backing.position());
            lab.setLeft(applyGraphicScale(MARGIN));
            gp.insertMember(lab,0,"lab");
            gp.setProperty("answerno",(idx));
            gp.hide();
            idx++;
        }
        answerButtons = anss;
        List<OBGroup> randarray = OBUtils.randomlySortedArray(answerButtons);
        for(OBGroup gp : randarray)
        {
            PointF pos = gp.position();
            pos.y = (y);
            gp.setPosition(pos);
            attachControl(gp);
            y +=(interspace + gp.height());
        }
        labelBox.hide();
        return randarray;
    }

    public void showAnswers(List<OBControl> randomQuestions) throws Exception
    {
        for(int i = 0;i < randomQuestions.size();i++)
        {
            if (randomQuestions.get(i).hidden())
                randomQuestions.get(i).show();
            else
                highlightControl((OBGroup)randomQuestions.get(i));
            int answerno = (Integer)(randomQuestions.get(i).propertyValue("answerno"));
            playAudioForQuestion(questionNo,answerno+1);
            lowlightControl((OBGroup)randomQuestions.get(i));
            waitForSecs(0.2f);
        }
    }

    public void nextQuestion()
    {
        try
        {
            if(++questionNo >= currPassage.questions.size())
            {
                nextScene();
            }
            else
            {
                doQuestion();
            }
        }
        catch(Exception e)
        {

        }
     }

    public void highlightControl(OBGroup g)
    {
        OBControl c = g.members.get(1);
        c.setFillColor(hiColour);
    }

    public void lowlightControl(OBGroup g)
    {
        OBControl c = g.members.get(1);
        c.setFillColor(loColour);
    }

    public void collectData()
    {
        if (shouldCollectMiscData())
        {
            collectMiscData("incorrect",Arrays.asList(collectionPassageID,collectionQuestionNo,collectionAnswerNo));
        }
    }

    public void checkButton(OBControl targ)
    {
        highlightControl((OBGroup) targ);
        try
        {
            int idx = answerButtons.indexOf(targ);
            collectionAnswerNo = idx;
            if(idx == 0)
            {
                playSfxAudio("tap",true);
                waitForSecs(0.4f);
                gotItRightBigTick(true);
                waitForSecs(0.4f);
                score++;
            }
            else
            {
                playSfxAudio("tap",false);
                waitForSecs(0.2f);
                waitSFX();
                gotItWrong();
                displayCross();
                waitForSecs(0.4f);
                waitSFX();
                events.add("feedback");
                collectData();
                nextScene();
                return;
            }
            targ.lowlight();
            nextQuestion();
        }
        catch(Exception exception)
        {
        }
    }

    public void doQuestion() throws Exception
    {
        totalQuestions++;
        lockScreen();
        randomQs = setUpQuestion(questionNo);
        unlockScreen();
        playAudioForQuestion(questionNo);
        waitForSecs(0.3f);
        showAnswers(randomQs);
        setStatus(STATUS_WAITING_FOR_ANSWER);
    }

    public void checkArrow()
    {
        try
        {
            OBControl arrow = objectDict.get("arrow");
            arrow.highlight();
            waitForSecs(0.4f);
            clearPassage();
            waitForSecs(0.4f);
            preQuestion();
            waitForSecs(0.4f);
            questionNo = 0;
            doQuestion();
        }
        catch(Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        return(finger(-1,2,answerButtons,pt));
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_ARROW_CLICK)
        {
            if(objectDict.get("arrow").frame() .contains(pt.x,pt.y))
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkArrow();
                    }
                });
            }
        }
        else if(status() == STATUS_WAITING_FOR_ANSWER)
        {
            final Object obj = findTarget(pt);
            if(obj != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton((OBControl)obj);
                    }
                });
            }
        }
    }



}
