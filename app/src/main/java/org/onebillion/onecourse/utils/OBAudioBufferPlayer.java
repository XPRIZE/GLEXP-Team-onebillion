package org.onebillion.onecourse.utils;

import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTimestamp;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;

import org.onebillion.onecourse.mainui.MainActivity;

import java.io.FileDescriptor;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.media.AudioFormat.CHANNEL_OUT_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;

/**
 * Created by alan on 13/07/17.
 */

public class OBAudioBufferPlayer extends OBGeneralAudioPlayer
{
    final int NO_BUFFERS = 16;
    public MediaExtractor mediaExtractor;
    MediaCodec codec;
    AudioTrack audioTrack;
    AssetFileDescriptor afd;
    double fromTime,toTime;
    long fileLength,fileAmtRead,amtWritten;
    long startFrame = 0,endFrame = Long.MAX_VALUE;
    long presentationTimeus = 0, durationus = -1;
    Boolean playWhenReady;
    int bufferSequenceNo = 0,nextBufIdx=0;
    boolean wantsFFTData;
    SimpleBuffer buffers[] = new SimpleBuffer[NO_BUFFERS];
    AudioTimestamp timeStamp = new AudioTimestamp();
    int sampleRate = 44100;

    public class SimpleBuffer extends Object
    {
        public static final int SB_EMPTY = 0,
        SB_WRITING = 1,
        SB_READING = 2,
        SB_WRITTEN = 3;
        int state = 0;
        int bufferSize = 1024;
        long sequence = 0;
        long presentationTimeus = 0;
        long frameNo;
        short data[] = new short[bufferSize];

        public SimpleBuffer()
        {
            super();
        }
        public Boolean startReading()
        {
            if (state == SB_WRITING)
                return false;
            state = SB_READING;
            return true;
        }
        public void stopReading()
        {
            state = SB_WRITTEN;
        }

        public Boolean startWriting()
        {
            state = SB_WRITING;
            return true;
        }
        public void stopWriting()
        {
            state = SB_WRITTEN;
        }
        public int writeData(ShortBuffer ib, int len)
        {
            if (len > bufferSize)
                len = bufferSize;
            ib.get(data,0,len);
            for (int i = len;i < bufferSize;i++)
                data[i] = 0;
            return len;
        }
    }
    public OBAudioBufferPlayer ()
    {
        this(false);
    }

    public OBAudioBufferPlayer (boolean withFFT)
    {
        mediaExtractor = new MediaExtractor();
        playerLock = new ReentrantLock();
        condition = playerLock.newCondition();
        setState(OBAP_IDLE);
        wantsFFTData = withFFT;
        if (wantsFFTData)
            for (int i = 0;i < NO_BUFFERS;i++)
                buffers[i] = new SimpleBuffer();
        fromTime = 0.0;
        toTime = -1.0;
    }


    public void waitAudio ()
    {
        if (getState() == OBAP_FINISHED)
            return;
        playerLock.lock();
        while (getState() == OBAP_PLAYING ||
                getState() == OBAP_PREPARING ||
                getState() == OBAP_SEEKING)
        {
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
            }
        }
        playerLock.unlock();
    }

    public void waitPrepared ()
    {
        if (getState() == OBAP_IDLE || getState() == OBAP_FINISHED)
            return;
        playerLock.lock();
        while (getState() == OBAP_PREPARING)
        {
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
            }
        }
        playerLock.unlock();
    }

    public void waitUntilPlaying ()
    {
        if (getState() == OBAP_IDLE || getState() == OBAP_FINISHED)
            return;
        playerLock.lock();
        while (getState() != OBAP_PLAYING)
        {
            try
            {
                condition.await();
            }
            catch (InterruptedException e)
            {
            }
        }
        playerLock.unlock();
    }

    public void stopPlaying ()
    {
        if (isPlaying())
        {
            audioTrack.stop();
            setState(OBAP_FINISHED);
            playerLock.lock();
            condition.signalAll();
            playerLock.unlock();
            cleanUp();
        }
    }

    void trackFinished()
    {
        //stopPlaying();
        setState(OBAP_FINISHED);
        playerLock.lock();
        condition.signalAll();
        playerLock.unlock();
        cleanUp();
    }

    void cleanUp()
    {

        if(audioTrack != null)
        {
            audioTrack.flush();
            audioTrack.release();
            audioTrack = null;
        }

        if(codec != null)
        {
            codec.stop();
            codec.release();
            codec = null;
        }

    }

    public void play() //call only after prepare!!!
    {
        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener()
        {
            @Override
            public void onMarkerReached(AudioTrack track)
            {
                trackFinished();
            }

            @Override
            public void onPeriodicNotification(AudioTrack track)
            {

            }
        });
        audioTrack.play();
        state = OBAP_PLAYING;
    }

    public void finishedPrepare()
    {
        if (playWhenReady)
        {
            play();
        }
    }

    int nextAvailableWriteBuffer()
    {
        SimpleBuffer b = buffers[nextBufIdx];
        while (!(b.state == SimpleBuffer.SB_WRITTEN || b.state == SimpleBuffer.SB_EMPTY))
        {
            nextBufIdx = (nextBufIdx + 1) % NO_BUFFERS;
        }
        return nextBufIdx;
    }

    void writeOutputBufferToBuffers(ByteBuffer outputBuffer,long prestimeus,long frameNo)
    {
        ShortBuffer ib = outputBuffer.asShortBuffer();
        int noInts = ib.limit();
        if (noInts == 0)
            return;
        //MainActivity.log(String.format("Writing buffers for f %d",frameNo));
        ib.rewind();
        long bufferDurationus = 1024 * 1000000 / sampleRate;
        long framesInBuffer = 1024;
        int i = 0;
        while (noInts > 0)
        {
            int wamt = Math.min(noInts,1024);
            int idx = nextAvailableWriteBuffer();
            SimpleBuffer sb = buffers[idx];
            sb.startWriting();
            sb.writeData(ib,wamt);
            sb.sequence = bufferSequenceNo++;
            sb.presentationTimeus = prestimeus + (long)(i * bufferDurationus);
            sb.frameNo = frameNo + i * framesInBuffer;
            //MainActivity.log(String.format("  writing to buffer %d, us - %d",idx,sb.frameNo));
            sb.stopWriting();
            noInts -= wamt;
            nextBufIdx = (nextBufIdx + 1) % NO_BUFFERS;
            i++;
        }
    }

    int closestBufferToTimeus(long prestimeus)
    {
        int closest = -1;
        long mindiff = prestimeus;
        int offset = nextBufIdx;
        for (int i = 0;i < NO_BUFFERS;i++)
        {
            int idx = (offset + i) % NO_BUFFERS;
            SimpleBuffer sb = buffers[idx];
            if (sb.state == SimpleBuffer.SB_EMPTY || sb.state == SimpleBuffer.SB_WRITTEN)
            {
                if (Math.abs(prestimeus - sb.presentationTimeus) < mindiff)
                {
                    mindiff = Math.abs(prestimeus - sb.presentationTimeus);
                    closest = idx;
                }
            }
        }
        if (closest >= 0)
            return closest;
        return 0;
    }

    int closestBufferToFrameNo(long frameNo)
    {
        frameNo = frameNo - 512;
        int closest = -1;
        long mindiff = frameNo;
        int offset = nextBufIdx;
        for (int i = 0;i < NO_BUFFERS;i++)
        {
            int idx = (offset + i) % NO_BUFFERS;
            SimpleBuffer sb = buffers[idx];
            if (sb.state == SimpleBuffer.SB_EMPTY || sb.state == SimpleBuffer.SB_WRITTEN)
            {
                if (Math.abs(frameNo - sb.frameNo) < mindiff)
                {
                    mindiff = Math.abs(frameNo - sb.frameNo);
                    closest = idx;
                }
            }
        }
        if (closest >= 0)
            return closest;
        return 0;
    }

    Boolean getFloatsFromBufferClosestToFrameNo(float[] of,long frameNo)
    {
        int attempts = 0;
        while (attempts < 3)
        {
            //MainActivity.log(String.format("Looking for buffer %d",frameNo));
            int idx = closestBufferToFrameNo(frameNo);
            SimpleBuffer sb = buffers[idx];
            if (sb.startReading())
            {
                //MainActivity.log(String.format("  reading from buffer %d, f - %d",idx,sb.frameNo));

                short d[] = sb.data;
                int sz = Math.min(of.length,sb.data.length);
                for (int i = 0;i < sz;i++)
                    of[i] = (d[i] / 32767f);
                sb.stopReading();
                return true;
            }
            attempts++;
        }
        return false;
    }

    public long currentPlayTimeus()
    {
        audioTrack.getTimestamp(timeStamp);
        return timeStamp.nanoTime / 1000;
    }

    public long currentFrame()
    {
        if (audioTrack != null)
        {
            audioTrack.getTimestamp(timeStamp);
            return timeStamp.framePosition;
        }
        return 0;
    }

    public Boolean getCurrentBufferFloats(float[] of)
    {
        return getFloatsFromBufferClosestToFrameNo(of,currentFrame());
    }

    public void prepare(AssetFileDescriptor afd)
    {
        this.afd = afd;
        try
        {
            state = OBAP_PREPARING;
            FileDescriptor fd = afd.getFileDescriptor();
            long fOffset = afd.getStartOffset();
            fileLength = afd.getLength();
            fileAmtRead = 0;
            mediaExtractor.setDataSource(fd,fOffset,fileLength);
            mediaExtractor.selectTrack(0);
            AudioFormat.Builder afb = new AudioFormat.Builder();
            afb.setChannelMask(CHANNEL_OUT_MONO);
            afb.setEncoding(AudioFormat.ENCODING_PCM_16BIT);
            afb.setSampleRate(sampleRate);
            AudioAttributes.Builder aab = new AudioAttributes.Builder();
            aab.setUsage(AudioAttributes.USAGE_MEDIA);
            aab.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
            int bufsz = AudioTrack.getMinBufferSize(sampleRate,CHANNEL_OUT_MONO,ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(aab.build(),
                    afb.build(),
                    bufsz,AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
            audioTrack.setVolume(volume);
            MediaFormat format = mediaExtractor.getTrackFormat(0);
            String mime = format.getString(MediaFormat.KEY_MIME);
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);

            durationus = format.containsKey(android.media.MediaFormat.KEY_DURATION) ?
                    format.getLong(android.media.MediaFormat.KEY_DURATION) : -1;
            if (toTime > 0.0)
            {
                double dur = toTime - fromTime;
                int noFrames = (int)(dur * sampleRate);
                audioTrack.setNotificationMarkerPosition(noFrames);
                endFrame = noFrames;
            }
            codec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(MediaCodec mc, int inputBufferId)
                {
                    ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferId);
                    boolean fin = fillBuffer(inputBuffer);
                    //MainActivity.log(String.format("%d bytes read",inputBuffer.limit()));
                    inputBuffer.rewind();
                    codec.queueInputBuffer(inputBufferId,0,inputBuffer.limit(),presentationTimeus,fin?BUFFER_FLAG_END_OF_STREAM:0);
                }

                @Override
                public void onOutputBufferAvailable(MediaCodec mc, int outputBufferId, MediaCodec.BufferInfo info) {
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferId);
                    MediaFormat bufferFormat = codec.getOutputFormat(outputBufferId);
                    int bytesInBuffer = outputBuffer.limit();
                    if (((amtWritten + bytesInBuffer) / 2) > endFrame)
                    {
                        long framesToWrite = endFrame - (amtWritten / 2);
                        bytesInBuffer = ((int)framesToWrite*2);
                    }
                    boolean endOfStream = ((info.flags & BUFFER_FLAG_END_OF_STREAM) != 0) || state == OBAP_FINISHED;
                    if (endOfStream)
                    {
                        int framesEnd = (int)(amtWritten + bytesInBuffer) / 2;
                        audioTrack.setNotificationMarkerPosition(framesEnd);
                    }
                    if (wantsFFTData)
                        writeOutputBufferToBuffers(outputBuffer,info.presentationTimeUs,amtWritten / 2);
                    int res = audioTrack.write(outputBuffer,bytesInBuffer,AudioTrack.WRITE_BLOCKING);
                    amtWritten += res;
                    if (state == OBAP_PREPARING && amtWritten > 300)
                    {
                        finishedPrepare();
                    }
                    //MainActivity.log(String.format("%d bytes written",res));
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
            if (fromTime > 0.0)
                mediaExtractor.seekTo((long)(fromTime * 1000000),MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            codec.start();
        }
        catch(Exception e)
        {

        }
    }

    public double duration ()
    {
        waitPrepared();
        if (durationus >= 0)
            return durationus / 1000000.0;
        return 0.0;
    }

    public int currentPositionms()
    {
        return (int)(currentPlayTimeus() / 1000);
    }


    boolean fillBuffer(ByteBuffer b)
    {
        if (state == OBAP_FINISHED)
            return true;
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
        playWhenReady = true;
        prepare(afd);
    }

    public void startPlaying (AssetFileDescriptor afd,double fromSecs,double toSecs)
    {
        if (isPlaying())
            stopPlaying();
        fromTime = fromSecs;
        toTime = toSecs;
        startPlaying(afd);
    }

    public void startPlayingAtTimeVolume(AssetFileDescriptor afd, long fr,float vol)
    {
        if (isPlaying())
            stopPlaying();
        volume = vol;
        fromTime = fr;
        startPlaying(afd);
    }



}
