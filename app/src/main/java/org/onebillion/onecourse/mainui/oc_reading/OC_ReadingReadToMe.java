package org.onebillion.onecourse.mainui.oc_reading;

import android.graphics.PointF;
import android.os.AsyncTask;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 07/06/16.
 */
public class OC_ReadingReadToMe extends OC_Reading
{
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
                        demob();
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
        List aud = (List) ((Map<String,Object>)audioScenes.get("a")).get("DEMO");
        Object s = aud.get(introNo);
        playAudioQueued(Arrays.asList(s),true);
        waitForSecs(0.5f);
        readPage();
    }

    public void demob() throws Exception
    {
        waitForSecs(0.3f);
        showNextArrow(true);
        PointF destPoint = OB_Maths.locationForRect(-0.1f, 0.3f, MainViewController().bottomRightButton .frame());
        loadPointerStartPoint(OB_Maths.locationForRect(0.5f, 1.1f, bounds()),destPoint);
        movePointerToPoint(destPoint,-1,true);
        List aud = (List) ((Map<String,Object>)audioScenes.get("b")).get("DEMO");
        playAudioQueued(aud ,true);
        waitForSecs(0.5f);
        thePointer.hide();
    }

    public void democ() throws Exception
    {
        waitForSecs(0.3f);
        showNextArrow(true);
        List aud = (List) ((Map<String,Object>)audioScenes.get("c")).get("DEMO");
        playAudioQueued(aud ,true);
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

    public void touchDownAtPoint(final PointF pt,View v)
    {

    }
}
