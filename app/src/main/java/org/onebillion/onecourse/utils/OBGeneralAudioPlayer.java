package org.onebillion.onecourse.utils;

import android.content.res.AssetFileDescriptor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Created by alan on 05/03/2018.
 */

public abstract class OBGeneralAudioPlayer
{
    public static final int OBAP_IDLE = 0,
            OBAP_PREPARING = 1,
            OBAP_PLAYING = 2,
            OBAP_SEEKING = 3,
            OBAP_FINISHED = 4,
            OBAP_PAUSED = 5;
    public Lock playerLock;
    Condition condition;
    int state;
    float volume = 1.0f;

    public synchronized int getState ()
    {
        return state;
    }
    synchronized void setState (int st)
    {
        state = st;
    }

    public abstract void waitAudio ();
    public abstract void waitPrepared ();
    public abstract void waitUntilPlaying ();
    public abstract void stopPlaying ();
    public abstract void play();
    public abstract void prepare(AssetFileDescriptor afd);
    public abstract void startPlaying (AssetFileDescriptor afd);
    public void startPlayingAtTime (AssetFileDescriptor afd, long fr)
    {
        startPlayingAtTimeVolume(afd,fr,1.0f);
    }

    public abstract void startPlayingAtTimeVolume(AssetFileDescriptor afd, long fr,float vol);
    public abstract double duration();
    public abstract int currentPositionms();

    public boolean isPlaying ()
    {
        return state == OBAP_PLAYING;
    }

    public boolean isPreparing ()
    {
        return getState() == OBAP_PREPARING;
    }

    public boolean isIdle ()
    {
        return getState() == OBAP_IDLE;
    }

}
