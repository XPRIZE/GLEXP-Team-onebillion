package org.onebillion.xprz.mainui.x_reading;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;

import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.utils.OBReadingPara;
import org.onebillion.xprz.utils.OBReadingWord;
import org.onebillion.xprz.utils.OB_Maths;

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
    OBGroup anna;
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

}
