package org.onebillion.onecourse.mainui.oc_reading;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 25/06/16.
 */
public class OC_ReadingIReadForce extends OC_ReadingIRead
{
    int wordIdx;

    public void prepare()
    {
        super.prepare();
        loadFingers();
    }

    public void setUpScene()
    {
        super.setUpScene();
        wordIdx = 0;
        setUpDecorationForWord(words.get(wordIdx));
    }

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
                    }
                    else
                    {
                        doMainXX();
                        setStatus(STATUS_AWAITING_CLICK);
                        endBody();
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.execute();
    }

    public void stage2() throws Exception
    {
        readPage();
        bringUpNextButton();
        if (pageNo == 0)
            demoNext();
    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.8f);
        lockScreen();
        wordback.show();
        wordback2.show();
        unlockScreen();
    }

    public void endBody()
    {
        final long stt = statusTime;
        OBUtils.runOnOtherThreadDelayed(3, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                remindBox(stt,3);
            }
        });
    }

    public void demoa() throws Exception
    {
        lockScreen();
        loadEvent("anna");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF pt = presenter.control.position();
        presenter.control.setProperty("restpos",new PointF(pt.x,pt.y));
        presenter.control.setRight(0);
        presenter.control.show();
        unlockScreen();
        presenter.walk((PointF) presenter.control.propertyValue("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        Map<String,List> eventd = (Map<String, List>) audioScenes.get("a");
        List<Object> aud = eventd.get("DEMO");
        presenter.speak(aud.subList(0,1),this);

        presenter.moveHandfromIndex(1,6,0.4);



        presenter.speak(aud.subList(1,2),this);
        waitForSecs(0.2f);

        lockScreen();
        wordback.show();
        wordback2.show();
        unlockScreen();
        waitForSecs(0.4f);

        presenter.moveHandfromIndex(6,0,0.3);
        waitForSecs(0.4f);

        PointF currPos = presenter.control.position();
        PointF destpos = new PointF(-presenter.control.width()/2, currPos.y);
        setStatus(STATUS_AWAITING_CLICK);
        presenter.walk(destpos);
        endBody();
    }

    public void demoNext() throws Exception
    {
        if (doArrowDemo)
        {
            waitForSecs(0.3f);
            PointF destPoint = OB_Maths.locationForRect(-0.1f, 0.3f, MainViewController().bottomRightButton.frame());
            loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.1f, new RectF(bounds())),destPoint);
            movePointerToPoint(destPoint,-1,true);
            waitForSecs(0.05f);
            Map<String,List> eventd = (Map<String, List>) audioScenes.get("c");
            playAudioQueued(eventd.get("DEMO"),true);
            waitForSecs(0.5f);
            thePointer.hide();
        }
     }

    public void nextWord() throws Exception
    {
        wordIdx++;
        if (wordIdx >= words.size())
        {
            waitForSecs(0.4f);
            lockScreen();
            wordback.hide();
            wordback2.hide();
            unlockScreen();

            stage2();
        }
        else
        {
            lockScreen();
            setUpDecorationForWord(words.get(wordIdx));
            wordback.show();
            wordback2.show();
            unlockScreen();
            long st =setStatus(STATUS_AWAITING_CLICK);
            endBody();
        }
    }

    public void flashBox(long sttime)
    {
        try
        {
            for (int i = 0;i < 2;i++)
            {
                lockScreen();
                wordback.hide();
                unlockScreen();
                waitAndCheck(sttime,0.1,4);
                lockScreen();
                wordback.show();
                unlockScreen();
                waitAndCheck(sttime,0.1,4);
            }
        }
        catch (Exception exception)
        {
            lockScreen();
            wordback.setHidden(wordIdx >= words.size());
            unlockScreen();
        }
    }

    public void remindBox(final long sttime,float secs)
    {
        if (statusChanged(sttime))
            return;
        flashBox(sttime);
        if (statusChanged(sttime))
            return;
        OBUtils.runOnOtherThreadDelayed(3, new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                remindBox(sttime,3);
            }
        });

    }

    public void checkTarget(OBReadingWord rw,PointF pt)
    {
        try
        {
            highlightAndSpeakWord(rw);
            nextWord();
        }
        catch (Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        if (wordIdx >= 0 && wordIdx < words.size())
        {
            OBControl lab = words.get(wordIdx).label;
            if (finger(0,2,Collections.singletonList(lab),pt) != null)
                return words.get(wordIdx);
        }
        return null;
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final Object obj = findTarget(pt);
            if (obj instanceof OBReadingWord)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkTarget((OBReadingWord)obj,pt);
                    }
                });
            }
        }
    }

    public void highlightAndSpeakWord(OBReadingWord w)
    {
        try
        {
            long token = takeSequenceLockInterrupt(true);
            if (token == sequenceToken)
            {
                boolean withBackground = jumpOffset > 0;
                highlightWordWithBackground(w,true,withBackground);
                speakWordAsPartial(w);
                lowlightWordWithBackground(w,true,withBackground);
            }
        }
        catch (Exception exception)
        {
        }
        sequenceLock.unlock();
    }


}
