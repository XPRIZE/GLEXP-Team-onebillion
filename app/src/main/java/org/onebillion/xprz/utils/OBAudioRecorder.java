package org.onebillion.xprz.utils;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import org.onebillion.xprz.mainui.OBSectionController;

import java.io.IOException;
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

    public Lock recorderLock;
    MediaRecorder mediaRecorder;
    String recordedPath;
    Timer recordingTimer;
    boolean recording;
    int recordCount;
    long expectedAudioLength;
    long timeLastSound, timeRecordingStart, timeFirstSound;
    OBSectionController sectionController;
    int voiceThreshold = 1500;
    Condition condition;


    public OBAudioRecorder(String recordFilePath, OBSectionController controller)
    {
        recordedPath =recordFilePath;
        sectionController = controller;

        recording = false;

        recorderLock = new ReentrantLock();
        condition = recorderLock.newCondition();

    }


    private void resetAudioRecorder()
    {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(recordedPath);
    }


    public void startRecording(double audioLength)
    {
        expectedAudioLength = Math.round(audioLength * 1000);
        recordCount = 0;
        recording = true;
        resetAudioRecorder();
        recordingTimer = new Timer();
        timeRecordingStart = timeLastSound = timeFirstSound = System.currentTimeMillis();
        try
        {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        recordingTimer.scheduleAtFixedRate(new TimerTask()
        {

            @Override
            public void run()
            {
                recordingTimerFire();
            }
        }, 50,50);

    }

    public void stopRecording()
    {
        recording = false;
        recordingTimer.cancel();
        recordingTimer.purge();
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        //mediaRecorder.reset();
    }

    public void playRecording()
    {
        OBAudioPlayer player = OBAudioManager.audioManager.playerForChannel(OBAudioManager.AM_MAIN_CHANNEL);
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
        if(recording && !sectionController._aborting)
        {
            long currentTime = System.currentTimeMillis();
            int val = getAverangePower();
           // Log.d(this.getClass().getName(), String.format("Power: %d",val));
            if(val > voiceThreshold)
            {
                recordCount++;
                timeLastSound = currentTime;
                if(recordCount == 3)
                    timeFirstSound = timeLastSound;
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

    public void finishWait()
    {
        recorderLock.lock();
        try
        {
            condition.signalAll();
        }
        finally
        {
            recorderLock.unlock();

        }
        stopRecording();
    }



}
