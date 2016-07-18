package org.onebillion.xprz.utils;


import android.hardware.camera2.CaptureRequest;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import org.onebillion.xprz.mainui.OBSectionController;

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

    public void prepareForVideoRecording(double audioLength, Size size)
    {
        videoSize = size;
        super.prepareForRecording(audioLength);
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




}
