package com.maq.xprize.onecourse.hindi.utils;


import android.content.res.AssetFileDescriptor;
import android.media.MediaRecorder;
import android.util.Size;
import android.view.Surface;

import com.maq.xprize.onecourse.hindi.controls.OBVideoPlayer;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by michal on 08/07/16.
 */
public class OBVideoRecorder
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
    protected WeakReference<OBSectionController> sectionController;
    protected int voiceThreshold = 1500;
    protected Condition condition;
    private Size videoSize;


    public OBVideoRecorder(String recordFilePath, OBSectionController controller)
    {
        activityPaused= false;
        recordedPath =recordFilePath;
        sectionController = new WeakReference<>(controller);

        recording = false;

        recorderLock = new ReentrantLock();
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
    }

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

    public int getAverangePower()
    {
        return mediaRecorder.getMaxAmplitude();
    }

    public void recordingTimerFire()
    {
        if(recording && !sectionController.get()._aborting)
        {
            long currentTime = System.currentTimeMillis();
            int val = getAverangePower();
            // Log.d(this.getClass().getName(), String.format("Power: %d",val));
            if(val > voiceThreshold)
            {
                recordCount++;
                timeLastSound = currentTime;
                if(recordCount == 3)
                    timeFirstSound = timeLastSound-150;
            }

            if(timeRecordingStart + 5000 < currentTime && !audioRecorded())
            {
                finishWait();
            }
            else if(timeRecordingStart + expectedAudioLength < currentTime && audioRecorded())
            {
                if(timeLastSound + 1500 < currentTime || timeFirstSound + expectedAudioLength*2 < currentTime)
                {
                    finishWait();
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

    private void finishWait()
    {
        stopRecording();

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
        finishWait();
    }


    public void prepareForVideoRecording(Size size)
    {
        videoSize = size;
        prepareForRecording();
    }


    protected void initRecorder()
    {

        mediaRecorder = new MediaRecorder();

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mediaRecorder.setVideoEncodingBitRate(100000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoSize.getWidth(),videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(recordedPath);


    }

    public Surface getSurface()
    {
        return mediaRecorder.getSurface();
    }

    public void playVideoRecording(final OBVideoPlayer videoPlayer)
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(recordedPath);
                if(audioRecorded())
                {
                    long audioStart = timeFirstSound-timeRecordingStart;
                    videoPlayer.startPlayingAtTime(afd, audioStart-400>0?audioStart-400:0);
                }
                else
                {
                    long audioStart = 5000 - expectedAudioLength;
                    videoPlayer.startPlayingAtTime(afd,audioStart>0?audioStart:0);
                }
            }
        });

    }


}
