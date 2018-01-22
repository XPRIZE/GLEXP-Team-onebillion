package org.onebillion.onecourse.mainui.oc_prepr3;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;

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

    Map <String,Est3_Passage>passageDict;
    List<String>passages;
    Est3_Passage currPassage;
    OBFont font;
    OBLabel passageLabel;
    int questionNo;
    List answerButtons;
    int score,totalQuestions;
    int highlightedIdx;
    int hiColour;
    OBLabel layOutBox;
    String audioPrefix;

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
                    passage.passageID = (cqNode.attributeStringValue("Object"));

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
                        question.questionID = (questionNode.attributeStringValue("Object"));
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

    public boolean showBackButton()
    {
        return false;
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

    public void fin()
    {
        showScoreHammerScore((int)(score * 10.0/totalQuestions),false);
    }


    public void clearAll()
    {
        deleteControls(".*");
        if(layOutBox != null)
        {
            detachControl(layOutBox);
            layOutBox = null;
        }
        if(passageLabel != null)
        {
            detachControl(passageLabel);
            passageLabel = null;
        }
        detachControls(answerButtons);
        answerButtons = null;
    }

    public void layOutLine(List<OBReadingWord>wordarr, float leftEdge, float rightEdge, float y, int justification, Typeface typeFace, float typeSize, String paraText)
    {
        List<OBReadingWord>words = new ArrayList<OBReadingWord>(wordarr);
        while (!words.isEmpty() && words.get(words.size()-1).text.startsWith(" "))
            words.remove(words.size()-1);
        if (words.isEmpty())
            return;
        numberOfTextLines++;
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(typeSize);
        textPaint.setTypeface(typeFace);
        float lineStart;
        OBReadingWord w0 = words.get(0);
        lineStart = leftEdge;
         for (OBReadingWord w : words)
        {
            OBLabel lab;
            lab = new OBLabel(w.text,typeFace,typeSize);
            lab.setColour(Color.BLACK);
            float widthUpToWord = textPaint.measureText(paraText.substring(w0.index,w.index));
            RectF f = new RectF(lab.frame());
            f.offset(-f.left,-f.top);
            f.offset(lineStart + widthUpToWord,y);
            lab.setFrame(f);
            if (!w.text.endsWith(" "))
            {
                float wdth = textPaint.measureText(paraText.substring(w0.index,w.index + w.text.length()));
                lab.setRight(wdth + lineStart);
            }
            textBox.insertMember(lab,0,"");
            w.frame = new RectF(lab.frame());
            w.label = lab;
            w.homePosition = new PointF(lab.position().x,lab.position().y);
            if ((w.flags & WORD_SPEAKABLE) != 0)
                lab.setZPosition(LABEL_ZPOS);
            else
                lab.setZPosition(LABEL_ZPOS - 3);
        }
    }
    void layOutManually(String text)
    {
        OBReadingPara para = new OBReadingPara(text,1);
        float lineHeight = fontSize;
        float topY = 0;
        float rightEdge = textBox.width();

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(font.typeFace);

        List<OBReadingWord> currLine = new ArrayList<>();
        PointF pos = new PointF(0, topY);
        int lineStartIndex = 0;
        float lineStart = 0;
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
                layOutLine(currLine,0,rightEdge,topY,textJustification,font.typeFace,fontSize,para.text);
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
    }

    public void setUpPassage()
    {
        clearAll();
        highlightedIdx = -1;
        loadEvent("passage");
        String passageID = passages.get(currNo);
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
        RectF f = new RectF(textBox.frame());
        layOutBox = new OBLabel(passageText.toString(),font);
        layOutBox.setJustification(OBTextLayer.JUST_LEFT);
        layOutBox.setFrame(f);
        layOutBox.setMaxWidth(f.width());
        attachControl(layOutBox);

        objectDict.get("arrow").hide();
    }

    public void setSceneintro()
    {
        setUpPassage();
    }

}
