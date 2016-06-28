package org.onebillion.xprz.mainui.x_reading;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.utils.OBReadingPara;
import org.onebillion.xprz.utils.OBReadingWord;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 07/06/16.
 */
public class X_ReadingReadToMeNTx extends X_ReadingReadToMe
{
    XPRZ_Presenter presenter;
    int cqType;
    boolean questionsAsked;
    String pageName;

    public void start()
    {
        setStatus(0);
        new AsyncTask<Void, Void,Void>()
        {
            protected Void doInBackground(Void... params) {
                try
                {
                    if (pageNo == 0)
                    {
                        setStatus(STATUS_DOING_DEMO);
                        demoa();
                        waitForSecs(0.7);
                        readTitle();
                        waitForSecs(0.5);
                        democ();
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                    else
                    {
                        waitForSecs(0.5);
                        replayAudio();
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.execute();
    }

    public void loadTemplate()
    {
        eventsDict = loadXML(getConfigPath("booktemplate.xml"));
        if (pageNo == 0)
        {
            loadEvent("title");
            String s;
            if ((s = eventAttributes.get("largefontsize"))!=null)
                fontSize = applyGraphicScale(Float.parseFloat(s));
            if ((s = eventAttributes.get("largelineheight"))!=null)
                lineHeightMultiplier = Float.parseFloat(s);
            if ((s = eventAttributes.get("largespacing"))!=null)
                letterSpacing = Float.parseFloat(s);
            textJustification = TEXT_JUSTIFY_CENTRE;
        }
        else
        {
            if (picJustify == PIC_JUSTIFY_RIGHT|| picJustify == PIC_JUSTIFY_LEFT)
                loadEvent("piconlyport");
            else
                loadEvent("piconly");
        }
    }

    public float layOutText()
    {
        if (pageNo == 0)
            return super.layOutText();
        return 0;
    }

    public void readParagraph(int pidx,double token,boolean canInterrupt) throws Exception
    {
        List<Object> l = (List<Object>)(Object)Collections.singletonList(String.format("p%d_%d",pageNo,pidx+1,true));
        playAudioQueued(l,true);
    }

    public void readTitle() throws Exception
    {
        OBReadingPara para = paragraphs.get(0);
        lockScreen();
        for (OBReadingWord w : para.words)
        {
            if (w.label != null && ((w.flags & OBReadingWord.WORD_SPEAKABLE) != 0))
            {
                highlightWord(w,true,false);
            }
        }
        textBox.setNeedsRetexture();
        unlockScreen();
        readParagraph(0,0,false);
        lockScreen();
        for (OBReadingWord w : para.words)
        {
            if (w.label != null && ((w.flags & OBReadingWord.WORD_SPEAKABLE) != 0))
            {
                highlightWord(w,false,false);
            }
        }
        textBox.setNeedsRetexture();
        unlockScreen();
    }

    public void readingFinished()
    {
        try
    {
        waitForSecs(0.8);
        if (status() != STATUS_FINISHING && !_aborting)
        {
            //if (!considerComprehensionQuestions())
            bringUpNextButton();
        }
    }
        catch (Exception exception)
        {
        }

    }
    public boolean readPage()
    {
        if (super.readPage())
        {
            readingFinished();
            return true;
        }
        return false;
    }
    public boolean showNextButton()
    {
        return false;
    }

    public void replayAudio()
    {
        if (pageNo == 0)
        {
            setStatus(status());
            new AsyncTask<Void, Void, Void>()
            {
                protected Void doInBackground(Void... params)
                {
                    try
                    {
                        readTitle();
                    }
                    catch(Exception e)
                    {

                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
        else
            super.replayAudio();
    }

    public void demoa() throws Exception
    {
        lockScreen();
        loadEvent("anna");
        presenter = XPRZ_Presenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        presenter.control.setProperty("restpos",new PointF(presenter.control.position().x,presenter.control.position().y));
        presenter.control.setRight(0);
        presenter.control.show();
        unlockScreen();

        presenter.walk((PointF) presenter.control.propertyValue("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        Map<String,List> eventd = (Map<String, List>) audioScenes.get("a");

        List<Object> aud = eventd.get("DEMO");
        presenter.speak(aud,this);
        waitForSecs(0.4f);
        PointF currPos = presenter.control.position();
        PointF destpos = new PointF(-presenter.control.width()/2, currPos.y);
        presenter.walk(destpos);
    }

    public void democ() throws Exception
    {
        waitForSecs(0.3f);
        showNextArrowAndRA(true);
        PointF destPoint = OB_Maths.locationForRect(-0.1f, 0.3f, MainActivity.mainViewController.bottomRightButton.frame());
        loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.1f,new RectF(bounds())),destPoint);
        movePointerToPoint(destPoint,-1,true);
        playAudioQueuedScene("c","DEMO",true);
        waitForSecs(0.5f);
        thePointer.hide();
    }



    public void showQuestionElements() throws Exception
    {
        if (!objectDict.get("cameo").hidden)
            return;
        playSfxAudio("anna_on",false);
        lockScreen();
        showControls("cameo");
        unlockScreen();
    }

    public void hideQuestionElements() throws Exception
    {
        playSfxAudio("anna_off",false);
        lockScreen();
        hideControls("cameo");
        if (cqType == 2)
        {
            hideControls("answer.*");
        }
        unlockScreen();
    }

    public void workOutQuestionType(Map<String,Object> eventAudio)
    {
        if (eventAudio.get("ANSWER") != null)
            cqType = 2;
        else if (eventAudio.get("CORRECT") != null)
            cqType = 1;
        else
            cqType = 3;
    }

    public boolean correctFirst()
    {
        Map<String,List<String>> asp = (Map<String, List<String>>) audioScenes.get(pageName);
        List<String> keys = asp.get("__keys");
        for (String key : keys)
        {
            if (key.equals("CORRECT"))
                return true;
            else if (key.equals("INCORRECT"))
                return false;
        }
        return true;
    }

    public void loadCQAudioXMLs()
    {
        String path = getConfigPath("cqaudio.xml");
        loadAudioXML(path);
        Map<String,Object> d = audioScenes;
        if (pageNo == maxPageNo)
        {
            if (d.get("final") != null)
                d.put(pageName,d.get("final"));
        }
        path = getConfigPath("cqsfx.xml");
        loadAudioXML(path);
        audioScenes.putAll(d);
    }

    public String pageName()
    {
        return String.format("p%d",pageNo);
    }

    public boolean considerComprehensionQuestions() throws Exception
    {
        String usecq = parameters.get("cq");
        if (usecq == null || !usecq.equals("true"))
            return false;
        if (questionsAsked)
            return false;
        loadCQAudioXMLs();
        pageName = pageName();
        if (audioScenes.get(pageName) != null)
        {
            workOutQuestionType((Map<String, Object>) audioScenes.get(pageName));
            loadCQPage();
            questionsAsked = true;
            if (cqType == 1)
            {
                targets = Arrays.asList(objectDict.get("shape"));
                demoCqType1a();
            }
            else if (cqType == 2)
            {
                if (correctFirst())
                    targets = Arrays.asList(objectDict.get("answer1"),objectDict.get("answer2"));
                else
                    targets = Arrays.asList(objectDict.get("answer2"),objectDict.get("answer1"));
                demoCqType2a();
            }
            else if (cqType == 3)
            {
                targets = Collections.emptyList();
                //demoCqType3a();
            }
            return true;
        }
        return false;
    }

    public void demoCqType2a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.4f);
        showQuestionElements();
        waitForSecs(0.4f);
        demoCqType2b(true);
    }

    public void demoCqType2b(boolean firstTime)
    {
        long token = -1;
        try
        {
            token = takeSequenceLockInterrupt(true);
            if (token == sequenceToken)
            {
/*                [control:anna speak:audioScenespageName.()"PROMPT".()];
                waitForSecs(0.4f);
                OBPath answer1 = (OBPath) objectDict.get("answer1");
                answer1.show();
                deployFlashAnim(answer1);
                [control:anna speak:audioScenespageName.()"ANSWER".()];
                checkSequenceToken(token);
                waitForSecs(0.2f);
                killAnimations();
                setStatus(STATUS_WAITING_FOR_ANSWER);
                checkSequenceToken(token);
                waitForSecs(0.4f);
                OBPath answer2 = (OBPath) objectDict.get("answer2");
                answer2.show();
                deployFlashAnim(answer2);
                control(anna speak:audioScenespageName.()"ANSWER2".()];
                checkSequenceToken(token);
                waitForSecs(0.2f);
                checkSequenceToken(token);
                killAnimations();
                waitForSecs(0.3f);
                checkSequenceToken(token);
                [control:anna speak:audioScenespageName.()"PROMPT2".()];
                checkSequenceToken(token);
                waitForSecs(0.3f);
                checkSequenceToken(token);
                if (firstTime)
                [reprompt:statusTime audio:nil after:5 action:^{
                demoCqType2b(true);
            }];*/
            }
        }
        catch (Exception exception)
        {
        }
        killAnimations();
        sequenceLock.unlock();
    }

    void setAnswerButtonActive(OBPath c)
    {
        lockScreen();
        c.setFillColor((Integer)c.propertyValue("fillcolour"));
        c.setStrokeColor((Integer)c.propertyValue("strokecolour"));
        unlockScreen();
    }

    void setAnswerButtonInActive(OBPath c)
    {
        lockScreen();
        c.setFillColor((Integer)c.propertyValue("desatfillcolour"));
        c.setStrokeColor((Integer)c.propertyValue("desatstrokecolour"));
        unlockScreen();
    }

    void setAnswerButtonSelected(OBPath c)
    {
        lockScreen();
        c.setFillColor((Integer)c.propertyValue("fillcolour"));
        c.setStrokeColor((Integer)c.propertyValue("strokecolour"));
        unlockScreen();
    }

    public void loadCQPage()
    {
        Map<String, Object> evd = loadXML(getConfigPath("cq.xml"));
        eventsDict.putAll(evd);
        evd = loadXML(getConfigPath("eventcq.xml"));
        eventsDict.putAll(evd);
        lockScreen();
        doVisual("cqmain");
        presenter = XPRZ_Presenter.characterWithGroup((OBGroup)objectDict.get("annahead"));

        //anna = (OBGroup) objectDict.get("annahead");
        if (cqType == 1 || cqType == 3)
        {
            loadEvent(pageName());
        }
        else if (cqType == 2)
        {
            for (OBControl p : filterControls("answer.*"))
            {
                OBPath c = (OBPath) p;
                int col = c.fillColor();
                c.setProperty("fillcolour", col);
                c.setProperty("desatfillcolour", OBUtils.DesaturatedColour(col, 0.2f));
                col = c.strokeColor();
                c.setProperty("strokecolour", col);
                c.setProperty("desatstrokecolour", OBUtils.DesaturatedColour(col, 0.2f));
                setAnswerButtonInActive(c);
            }
        }
        else
        {

        }
        hideControls("answer.*");
        hideControls("cameo");
        unlockScreen();
    }

    public void demoCqType1a() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.4f);
        showQuestionElements();
        waitForSecs(0.4f);
        demoCqType1b(true);
    }

    public void demoCqType1b(boolean firstTime)
    {
        long token = -1;
        try
        {
            token = takeSequenceLockInterrupt(true);
            if (token == sequenceToken)
            {/*
                waitForSecs(0.4f);
                showQuestionElements();
                waitForSecs(0.4f);
                checkSequenceToken(token);

                List<Object> aud = OBUtils.insertAudioInterval(audioScenes[pageName].get("PROMPT")),500);
                [control:anna speak:aud];
                checkSequenceToken(token);
                setStatus(STATUS_WAITING_FOR_ANSWER);
                waitForSecs(0.4f);
                checkSequenceToken(token);
                if (firstTime)
                [reprompt:statusTime audio:nil after:5 action:^{
                demoCqType1b(true);
            }];*/
            }
        }
        catch (Exception exception)
        {
        }
        sequenceLock.unlock();
    }

}
