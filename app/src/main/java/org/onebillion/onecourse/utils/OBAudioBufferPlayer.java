package org.onebillion.onecourse.utils;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import org.onebillion.onecourse.mainui.MainActivity;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.media.AudioFormat.CHANNEL_OUT_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

/**
 * Created by alan on 13/07/17.
 */

public class OBAudioBufferPlayer extends Object
{
    public final int OBAP_IDLE = 0,
            OBAP_PREPARING = 1,
            OBAP_PLAYING = 2,
            OBAP_SEEKING = 3;
    public MediaExtractor mediaExtractor;
    MediaCodec codec;
    AudioTrack audioTrack;
    AssetFileDescriptor afd;
    public Lock playerLock;
    Condition condition;
    int state;
    float volume = 1.0f;
    long fromTime,fileLength,fileAmtRead,amtWritten;
    long presentationTimeus = 0;

    public OBAudioBufferPlayer ()
    {
        mediaExtractor = new MediaExtractor();
        playerLock = new ReentrantLock();
        condition = playerLock.newCondition();
        setState(OBAP_IDLE);
    }

    synchronized int getState ()
    {
        return state;
    }

    synchronized void setState (int st)
    {
        state = st;
    }

    public void stopPlaying ()
    {

    }

    public void prepare()
    {
        try
        {
            FileDescriptor fd = afd.getFileDescriptor();
            long fOffset = afd.getStartOffset();
            fileLength = afd.getLength();
            fileAmtRead = 0;
            mediaExtractor.setDataSource(fd,fOffset,fileLength);
            mediaExtractor.selectTrack(0);
            AudioFormat.Builder afb = new AudioFormat.Builder();
            afb.setChannelMask(CHANNEL_OUT_MONO);
            afb.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
            afb.setSampleRate(44100);
            AudioAttributes.Builder aab = new AudioAttributes.Builder();
            aab.setUsage(AudioAttributes.USAGE_MEDIA);
            aab.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
            int bufsz = AudioTrack.getMinBufferSize(44100,CHANNEL_OUT_MONO,ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(aab.build(),
                    afb.build(),
                    bufsz,AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
            MediaFormat format = mediaExtractor.getTrackFormat(0);
            String mime = format.getString(MediaFormat.KEY_MIME);
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);

            codec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec mc, int inputBufferId)
                {
                    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
                    // fill inputBuffer with valid data
                    Boolean fin = fillBuffer(inputBuffer);
                    MainActivity.log(String.format("%d bytes read",inputBuffer.limit()));
                    inputBuffer.rewind();
                    codec.queueInputBuffer(inputBufferId,0,inputBuffer.limit(),presentationTimeus,fin?BUFFER_FLAG_END_OF_STREAM:0);
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo info) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                    MediaFormat bufferFormat = codec.getOutputFormat(outputBufferId); // option A
                    // bufferFormat is equivalent to mOutputFormat
                    // outputBuffer is ready to be processed or rendered.
                    int res = audioTrack.write(outputBuffer,outputBuffer.limit(),AudioTrack.WRITE_BLOCKING);
                    amtWritten += res;
                    if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING && amtWritten > 300)
                        audioTrack.play();
                    MainActivity.log(String.format("%d bytes written",res));
                    codec.releaseOutputBuffer(outputBufferId,true);
                }

                @Override
                public void onOutputFormatChanged(MediaCodec mc, MediaFormat format) {
                    // Subsequent data will conform to new format.
                    // Can ignore if using getOutputFormat(outputBufferId)
                    //mOutputFormat = format; // option B
                }

                @Override
                public void onError(MediaCodec codec,MediaCodec.CodecException e)
                {
                    e.printStackTrace();
                }
            });
            codec.start();
        }
        catch(Exception e)
        {

        }
    }

    Boolean fillBuffer(ByteBuffer b)
    {
        presentationTimeus = mediaExtractor.getSampleTime();
        int bytesRead = mediaExtractor.readSampleData(b, 0);
        if (bytesRead > 0)
        {
            fileAmtRead += bytesRead;
            mediaExtractor.advance();
            return false;
        }
        return true;
    }

    public void startPlaying (AssetFileDescriptor afd)
    {
        if (isPlaying())
            stopPlaying();
        this.afd = afd;
        prepare();
    }

    public boolean isPlaying ()
    {
        return state == OBAP_PLAYING;
    }


}
