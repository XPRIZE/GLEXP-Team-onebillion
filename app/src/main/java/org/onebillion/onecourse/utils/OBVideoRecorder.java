package org.onebillion.onecourse.utils;


import android.content.res.AssetFileDescriptor;
import android.media.MediaRecorder;
import android.util.Size;
import android.view.Surface;

import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.mainui.OBSectionController;

/**
 * Created by michal on 08/07/16.
 */
public class OBVideoRecorder extends OBAudioRecorder
{


    private Size videoSize;

    public OBVideoRecorder(String recordFilePath,OBSectionController controller)
    {
        super(recordFilePath,controller);

    }

    public void prepareForVideoRecording(Size size)
    {
        videoSize = size;
        super.prepareForRecording();
    }


    @Override
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
