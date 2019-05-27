package com.maq.xprize.onecourse.mainui.oc_reading;

import android.graphics.PointF;
import android.os.AsyncTask;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBPresenter;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 07/06/16.
 */
public class OC_ReadingReadToMe extends OC_Reading
{
    OBPresenter presenter;

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
                        if (OBUtils.coalesce(parameters.get("demo"),"").equals("true"))
                            demoa();
                        waitForSecs(0.7);
                        readPage();
                        waitForSecs(0.3f);
                        showNextArrow(true);
                        if (doArrowDemo)
                            democ();
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                    else
                    {
                        waitForSecs(0.5);
                        readingReplayAudio();
                    }
                }
                catch (Exception exception)
                {
                }
                return null;
            }}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void setUpScene()
    {
        super.setUpScene();
        OBControl c = objectDict.get("wordback");
        if (c != null)
            c.hide();
    }

    public boolean showPrevButton()
    {
        return false;
    }

    public boolean showNextButton()
    {
        return false;
    }

    public void demoa() throws Exception
    {
        lockScreen();
        loadEvent("anna");
        presenter = OBPresenter.characterWithGroup((OBGroup)objectDict.get("presenter"));
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
        aud = eventd.get("DEMO2");
        presenter.speak(aud,this);
        waitForSecs(0.4f);
        PointF currPos = presenter.control.position();
        PointF destpos = new PointF(-presenter.control.width()/2, currPos.y);
        presenter.walk(destpos);
    }

    public void demob() throws Exception
    {
    }

    public void democ() throws Exception
    {
        PointF destPoint = OB_Maths.locationForRect(-0.1f, 0.3f, MainViewController().bottomRightButton .frame());
        loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.1f, bounds()),destPoint);
        movePointerToPoint(destPoint,-1,true);
        List aud = (List) ((Map<String,Object>)audioScenes.get("c")).get("DEMO");
        playAudioQueued(aud ,true);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void readingFinished()
    {
        try
        {
            waitForSecs(0.8f);
            if(status()  != STATUS_FINISHING && !_aborting)
            {
                bringUpNextButton();
            }
        }
        catch(Exception exception)
        {
        }
    }

    public boolean readPage()
    {
        if(super.readPage() )
        {
            readingFinished();
            return true;
        }
        return false;
    }

    public void readingReplayAudio()
    {
        if (!_aborting && !MainViewController().navigating && status()!= STATUS_FINISHING && status() != STATUS_DOING_DEMO)
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    currPara = 0;
                    readPage();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {

    }
}
