package com.maq.xprize.onecourse.hindi.utils;

import android.media.MediaRecorder;

import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by michal on 07/07/16.
 */
public class OBAudioRecorder
{

    public boolean activityPaused;
    protected Lock recorderLock;
    protected MediaRecorder mediaRecorder;
    protected String recordedPath;
    protected Timer recordingTimer;
    protected boolean recording;
    protected int recordCount;
    protected long expectedAudioLength;
    protected long timeLastSound, timeRecordingStart, timeFirstSound;
    protected WeakReference<OBSectionController> sectionControllerWeakReference;
    protected int passThreshold = 2500;
    public float silenceTimming = 3.0f;
    protected Condition condition;


    public OBAudioRecorder(String recordFilePath, OBSectionController controller)
    {
        activityPaused = false;
        recordedPath = recordFilePath;
        sectionControllerWeakReference = new WeakReference<>(controller);

        recording = false;

        recorderLock = new ReentrantLock();
    }


    protected void initRecorder()
    {
        recordingTimer = null;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(recordedPath);
    }


    public void startRecording(double audioLength)
    {
        if(activityPaused)
            return;

        prepareForRecording();
        startMediaRecorderAndTimer(audioLength);
    }

    public void prepareForRecording()
    {
        condition = recorderLock.newCondition();
        recordCount = 0;

        initRecorder();
        try
        {
            mediaRecorder.prepare();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void startMediaRecorderAndTimer(double audioLength)
    {
        if(mediaRecorder != null && !activityPaused)
        {
            recording = true;

            timeRecordingStart = timeLastSound = timeFirstSound = System.currentTimeMillis();
            mediaRecorder.start();
            if(audioLength > 0)
            {
                expectedAudioLength = Math.round(audioLength * 1000);
                recordingTimer = new Timer();
                recordingTimer.scheduleAtFixedRate(new TimerTask()
                {

                    @Override
                    public void run()
                    {
                        recordingTimerFire();
                    }
                }, 50, 50);
            }
        }
    }



    public void stopRecording()
    {
        try
        {
            if (recording)
            {
                recording = false;
                if(recordingTimer != null)
                {
                    recordingTimer.cancel();
                    recordingTimer.purge();
                }
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        if(condition == null)
            return;

        recorderLock.lock();
        try
        {
            condition.signalAll();
        }
        finally
        {
            recorderLock.unlock();

        }
        condition = null;
    }
/*
    public void playRecording()
    {
        OBGeneralAudioPlayer player = OBAudioManager.audioManager.playerForChannel(OBAudioManager.AM_MAIN_CHANNEL);
        if(audioRecorded())
        {
            long audioStart = timeFirstSound-timeRecordingStart;
            player.startPlayingAtTime(OBUtils.getAssetFileDescriptorForPath(recordedPath), audioStart-400>0?audioStart-400:0);
        }
        else
        {
            long audioStart = 5000 - expectedAudioLength;
            player.startPlayingAtTime(OBUtils.getAssetFileDescriptorForPath(recordedPath),audioStart>0?audioStart:0);
        }
    }
*/
    public void playRecording() throws Exception
    {
        OBGeneralAudioPlayer player = OBAudioManager.audioManager.playerForChannel(OBAudioManager.AM_MAIN_CHANNEL);
        long startTime = 0;
        long endTime =  expectedAudioLength;
        long waitTime = 2000;
        if(audioRecorded())
        {
            long audioStart = timeFirstSound-timeRecordingStart;
            startTime = audioStart-400>0?audioStart-400:0;
            endTime = timeLastSound-timeRecordingStart+400;
            waitTime = endTime-startTime;
        }
        player.startPlayingAtTimeVolume(OBUtils.getAssetFileDescriptorForPath(recordedPath),startTime, 1.0f);
        sectionControllerWeakReference.get().waitForSecs(waitTime/1000f);
        player.stopPlaying(true);
    }


    public int getAverangePower()
    {
        return mediaRecorder.getMaxAmplitude();
    }

    public void recordingTimerFire()
    {
        if(recording && !sectionControllerWeakReference.get()._aborting)
        {
            long currentTime = System.currentTimeMillis();
            int val = getAverangePower();
           // Log.d(this.getClass().getName(), String.format("Power: %d",val));
            if(val > passThreshold)
            {
                recordCount++;
                timeLastSound = currentTime;
                if(recordCount == 3)
                    timeFirstSound = timeLastSound-150;
            }

            if(timeRecordingStart + 5000 < currentTime && !audioRecorded())
            {
                stopRecording();
            }
            else if(audioRecorded())
            {
                if(((timeLastSound + silenceTimming) < currentTime) ||
                    ((timeFirstSound + expectedAudioLength) < currentTime))
                {
                    stopRecording();
                }
            }
        }
        else
        {
            recordingTimer.cancel();
        }
    }

    public boolean audioRecorded()
    {
        return recordCount >= 6;
    }

    public void waitForRecord()
    {
        if(condition == null)
            return;

        recorderLock.lock();
        try
        {
            condition.await();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            recorderLock.unlock();

        }
    }


    public String getRecordingPath()
    {
        return recordedPath;
    }

    public void onResume()
    {
        activityPaused = false;
    }


    public void onPause()
    {
        activityPaused = true;
        stopRecording();
    }


}
