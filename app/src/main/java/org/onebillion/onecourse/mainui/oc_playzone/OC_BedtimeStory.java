package org.onebillion.onecourse.mainui.oc_playzone;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;

import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAudioManager;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.CHANNEL_OUT_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

/**
 * Created by alan on 20/04/2017.
 */

public class OC_BedtimeStory extends OC_SectionController
{
    public static final String fileName = "1_sample";
    MediaExtractor mediaExtractor;
    MediaCodec codec;
    AudioTrack audioTrack;
    long fLength = 0,fAmtRead,amtWritten=0;
    long presentationTimeus = 0;
    public void miscSetUp()
    {

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
            mediaExtractor = new MediaExtractor();
            AssetFileDescriptor afd = OBAudioManager.audioManager.getAudioPathFD(fileName);
            FileDescriptor fd = afd.getFileDescriptor();
            long fOffset = afd.getStartOffset();
            fLength = afd.getLength();
            fAmtRead = 0;
            mediaExtractor.setDataSource(fd,fOffset,fLength);
            int numTracks = mediaExtractor.getTrackCount();
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
        catch (Exception e)
        {
            e.printStackTrace();
            MainActivity.log("error opening media " + e.getMessage());
        }
    }

    ByteBuffer fillBuffer()
    {
        //long amtToAlloc = Math.min(fLength - fAmtRead,4096);
        long amtToAlloc = 4096;
        int byteAlloc = (int)amtToAlloc * 2;
        if (amtToAlloc > 0)
        {
            {
                ByteBuffer b = ByteBuffer.allocate(byteAlloc);
                int bytesWritten = 0;
                int bytesRead = mediaExtractor.readSampleData(b,bytesWritten);
                while (bytesRead > 0 && (bytesWritten + bytesRead) < byteAlloc)
                {
                    bytesWritten += bytesRead;
                    fAmtRead += bytesRead;
                    mediaExtractor.advance();
                    bytesRead = mediaExtractor.readSampleData(b,bytesWritten);
                }
                if (bytesWritten > 0)
                    return b;
            }
        }
        return null;
    }

    void fillBuffero(ByteBuffer b)
    {
        int amt = b.limit() - b.position();
        int bytesWritten = 0;
        presentationTimeus = mediaExtractor.getSampleTime();
        int bytesRead = mediaExtractor.readSampleData(b,bytesWritten);
        while (bytesRead > 0 && (bytesWritten + bytesRead) < amt)
        {
            bytesWritten += bytesRead;
            fAmtRead += bytesRead;
            mediaExtractor.advance();
            bytesRead = mediaExtractor.readSampleData(b,bytesWritten);
        }
    }
    Boolean fillBuffer(ByteBuffer b)
    {
        presentationTimeus = mediaExtractor.getSampleTime();
        int bytesRead = mediaExtractor.readSampleData(b, 0);
        if (bytesRead > 0)
        {
            fAmtRead += bytesRead;
            mediaExtractor.advance();
            return false;
        }
        return true;
    }
}
