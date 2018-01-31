package org.onebillion.onecourse.mainui.oc_reading;

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
import static org.onebillion.onecourse.utils.OBAudioBufferPlayer.OBAP_FINISHED;

/**
 * Created by alan on 20/04/2017.
 */

public class OC_BedtimeStory extends OC_SectionController
{
    public static final int STATUS_STARTING_FILE = 1001,
        STATUS_READING_FILE = 1002,
        STATUS_FINISHED_FILE = 1003;

    OBAudioBufferPlayer player;
    boolean allFilesDone = false;
    private Runnable timerRunnable;
    private Handler timerHandler = new Handler();
    float dftArea[] = new float[1024];
    float displayScaleFactor = 1.0f;
    float maxValue = 1.5f;
    int fileIndex;

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
        zeroBuckets();
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
        setStatus(STATUS_NIL);
        try
        {
            startReading();
            scheduleTimerEvent();
        }
        catch (Exception e)
        {
        }
    }

    String fileNameForIndex(int idx)
    {
        return String.format("para%d_1",idx);
    }

    void startReading()
    {
        fileIndex = 0;
        processAndPlayFile(fileNameForIndex(fileIndex));
    }

    public Boolean processAndPlayFile(String fileName)
    {
        try
        {
            setStatus(STATUS_STARTING_FILE);
            player = new OBAudioBufferPlayer();
            AssetFileDescriptor afd = OBAudioManager.audioManager.getAudioPathFD(fileName);
            if (afd == null)
            {
                allFilesDone = true;
                return false;
            }
            player.startPlaying(afd);
        }
        catch (Exception e)
        {
            allFilesDone = true;
            return false;
        }
        return true;
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
    static float totInFloatArray(float[] fa,int st, int en)
    {
        if (fa.length == 0)
            return 0;
        float tot = 0;
        for (int i = st;i < en;i++)
        {
            tot += fa[i];
        }
        return tot;
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

    void putTotsIntoBuckets(float data[],int inBucket[],float outBucket[])
    {
        int st = 0;
        for (int i = 0;i < inBucket.length;i++)
        {
            float tot = totInFloatArray(data,st,inBucket[i]);
            outBucket[i] = tot;
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

    void zeroBuckets()
    {
        lockScreen();
        for (int i = 0;i < 4;i++)
        {
            OBControl c = objectDict.get(String.format("bar%d",4-i));
            setObjectVal(c,0f);
        }
        unlockScreen();

    }
    void processDFT()
    {
        player.getCurrentBufferFloats(dftArea);
        OB_Maths.dlDFT(dftArea);
        //MainActivity.log(String.format("max after dft %g",maxInFloatArray(dftArea,0,dftArea.length)));
        //int[] thresholdindexes = {512-256,512-128,512-64,512};
        //int[] thresholdindexes = {512-128,512-64,512-32,512-2};
        int[] thresholdindexes = {512-160,512-32,512-6,512-2};
        float[] outBuckets = new float[4];
        putTotsIntoBuckets(dftArea,thresholdindexes,outBuckets);
        lockScreen();
        int noBuckets = outBuckets.length;
        for (int i = 0;i < noBuckets;i++)
        {
            OBControl c = objectDict.get(String.format("bar%d",noBuckets-i));
            float val = (float)Math.log10(outBuckets[i] + 1);
            //float val = (outBuckets[i]);
            //MainActivity.log(String.format("%g",val));
            //sc = OB_Maths.clamp(0.1f,2,sc);
            setObjectVal(c,val * 0.4f);
        }
        unlockScreen();
    }

    public void exitEvent()
    {
        player.stopPlaying();
        super.exitEvent();
    }

    void nextFile()
    {
        fileIndex++;
        Runnable r = new Runnable()
            {
                @Override
                public void run()
                {
                    processAndPlayFile(fileNameForIndex(fileIndex));
                    if (allFilesDone)
                        exitEvent();
                }
            };
        Handler h = new Handler();
        h.postDelayed(r,300);
    }

    void timerEvent()
    {
        if (player.isPlaying())
        {
            if (status() == STATUS_STARTING_FILE)
                setStatus(STATUS_READING_FILE);
            processDFT();
        }
        else
        {
            if (status() == STATUS_READING_FILE)
            {
                if (player.getState() == OBAP_FINISHED)
                {
                    setStatus(STATUS_FINISHED_FILE);
                    zeroBuckets();
                    nextFile();
                }
            }
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
