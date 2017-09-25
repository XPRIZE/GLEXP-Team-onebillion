package org.onebillion.onecourse.mainui.oc_playzone;

import android.content.res.AssetFileDescriptor;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Handler;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAudioBufferPlayer;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OB_Maths;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.CHANNEL_OUT_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

/**
 * Created by alan on 20/04/2017.
 */

public class OC_BedtimeStory extends OC_SectionController
{
    public String fileName = "1_sample";
    OBAudioBufferPlayer player;
    private Runnable timerRunnable;
    private Handler timerHandler = new Handler();
    float dftArea[] = new float[1024];
    float displayScaleFactor = 1.0f;
    float maxValue = 1.5f;

    public int buttonFlags ()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }

    public void miscSetUp()
    {
        for (OBControl c : filterControls("obj.*"))
            ((OBPath)c).outdent(applyGraphicScale(5));
        List<OBControl> rects = sortedFilteredControls("rect.*");
        RectF b = rects.get(0).bounds();
        PointF p1 = OB_Maths.locationForRect(0.5f,0.25f,b);
        PointF p2 = OB_Maths.locationForRect(0.5f,0.75f,b);
        for (int i = 0;i < rects.size();i++)
        {
            int j = i + 1;
            OBPath bar = (OBPath) objectDict.get(String.format("bar%d",j));
            OBPath rect = (OBPath) objectDict.get(String.format("rect%d",j));
            OBPath circle = (OBPath) objectDict.get(String.format("obj%d",j));
            bar.setStrokeColor(circle.fillColor());
            bar.setFrame(rect.frame());
            bar.setLineWidth(bar.width()/2f);
            Path path = bar.path();
            path.reset();
            path.moveTo(p1.x,p1.y);
            path.lineTo(p2.x,p2.y);
            rect.hide();
            circle.hide();
        }
        fileName = parameters.get("story");
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        events = new ArrayList<>();
        events.addAll(Arrays.asList("a"));
        doVisual(currentEvent());
    }

    public void start()
    {
        setStatus(0);
        try
        {
            player = new OBAudioBufferPlayer();
            AssetFileDescriptor afd = OBAudioManager.audioManager.getAudioPathFD(fileName);
            player.startPlaying(afd);
            scheduleTimerEvent();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            MainActivity.log("error opening media " + e.getMessage());
        }
    }

    static float maxInFloatArray(float[] fa,int st, int en)
    {
        if (fa.length == 0)
            return 0;
        float maxx = fa[st];
        for (int i = st + 1;i < en;i++)
        {
            float x = fa[i];
            if (x > maxx)
                maxx = x;
        }
        return maxx;
    }
    void putMaxesIntoBuckets(float data[],int inBucket[],float outBucket[])
    {
        int st = 0;
        for (int i = 0;i < inBucket.length;i++)
        {
            float maxx = maxInFloatArray(data,st,inBucket[i]);
            outBucket[i] = maxx;
            st = inBucket[i];
        }
    }

    float mapValue(float minr,float maxr,float inVal)
    {
        if (inVal > maxValue)
            maxValue = inVal;
        else if (inVal < 0)
            inVal = 0;
        return minr + (inVal / maxValue) * (maxr - minr);
    }

    void setObjectValo(OBControl obj,float val)
    {
        float sc = mapValue(0.5f,1.2f,val);
        obj.setScale(sc);
    }
    void setObjectVal(OBControl obj,float val)
    {
        float sc = mapValue(0.01f,0.9f,val);
        float h = obj.height() / 2f;
        float len = sc * h;
        float x = obj.width() / 2f;
        OBPath p = (OBPath)obj;
        Path path = p.path();
        path.reset();
        path.moveTo(x,h - len);
        path.lineTo(x,h + len);
        p.setNeedsRetexture();
        p.invalidate();
    }
    void processDFT()
    {
        player.getCurrentBufferFloats(dftArea);
        OB_Maths.dlDFT(dftArea);
        //MainActivity.log(String.format("max after dft %g",maxInFloatArray(dftArea,0,dftArea.length)));
        //int[] thresholdindexes = {512-256,512-128,512-64,512};
        int[] thresholdindexes = {512-128,512-64,512-32,512-2};
        float[] outBuckets = new float[4];
        putMaxesIntoBuckets(dftArea,thresholdindexes,outBuckets);
        lockScreen();
        int noBuckets = outBuckets.length;
        for (int i = 0;i < noBuckets;i++)
        {
            OBControl c = objectDict.get(String.format("bar%d",noBuckets-i));
            float val = (float)Math.log10(outBuckets[i] + 1);
            //float val = (outBuckets[i]);
            //MainActivity.log(String.format("%g",val));
            //sc = OB_Maths.clamp(0.1f,2,sc);
            setObjectVal(c,val);
        }
        unlockScreen();
    }

    void timerEvent()
    {
        if (player.isPlaying())
        {
            //MainActivity.log(String.format("currentframe %d",player.currentFrame()));
            processDFT();
        }
        scheduleTimerEvent();
    }
    void scheduleTimerEvent()
    {
        if (_aborting || theStatus == STATUS_EXITING)
            return;
        if (timerRunnable == null)
        {
            timerRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    timerEvent();
                }
            };
        }
        timerHandler.removeCallbacks(timerRunnable);
        timerHandler.postDelayed(timerRunnable,20);
    }

    public void stopAllAudio ()
    {
        super.stopAllAudio();
        player.stopPlaying();
    }
}
