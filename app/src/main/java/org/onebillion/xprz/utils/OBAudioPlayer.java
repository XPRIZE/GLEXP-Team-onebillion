package org.onebillion.xprz.utils;

/**
 * Created by alan on 11/10/15.
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;

public class OBAudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener
{
    public final int OBAP_IDLE = 0,
            OBAP_PREPARING = 1,
            OBAP_PLAYING = 2,
            OBAP_SEEKING = 3;
    public MediaPlayer player;
    public Lock playerLock;
    Condition condition;
    int state;
    long fromTime;

    public OBAudioPlayer ()
    {
        player = new MediaPlayer();
        playerLock = new ReentrantLock();
        condition = playerLock.newCondition();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
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
        if (player != null)
        {
            MediaPlayer cpplayer = player;
            player = null;
            setState(OBAP_IDLE);
            playerLock.lock();
            condition.signalAll();
            playerLock.unlock();
            try
            {
                cpplayer.reset();
                cpplayer.release();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void startPlaying (AssetFileDescriptor afd)
    {
        if (isPlaying())
            stopPlaying();
        fromTime = -1;
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        try
        {
            state = OBAP_PREPARING;
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepareAsync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            setState(OBAP_IDLE);
            return;
        }

    }

    public void startPlayingAtTime (AssetFileDescriptor afd, long fr)
    {
        if (isPlaying())
            stopPlaying();
        fromTime = fr;
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnCompletionListener(this);
        try
        {
            state = OBAP_PREPARING;
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepareAsync();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            setState(OBAP_IDLE);
            return;
        }

    }

    public void prepare (AssetFileDescriptor afd)
    {
        if (isPlaying())
            stopPlaying();
        fromTime = -1;
        player = new MediaPlayer();
        //player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        try
        {
            state = OBAP_PREPARING;
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepare();
            setState(OBAP_PLAYING);
            playerLock.lock();
            condition.signalAll();
            playerLock.unlock();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            setState(OBAP_IDLE);
            return;
        }

    }

    public void play() //call only after prepare!!!
    {
        if(player != null)
        {
            setState(OBAP_PLAYING);
            player.start();
        }
    }

    public void waitAudio ()
    {
        if (getState() == OBAP_IDLE)
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
        if (getState() == OBAP_IDLE)
            return;
        playerLock.lock();
        while (player == null || getState() == OBAP_PREPARING)
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
        if (getState() == OBAP_IDLE)
            return;
        playerLock.lock();
        while (player == null || getState() != OBAP_PLAYING)
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

    public boolean isPlaying ()
    {
        if (player == null)
            return false;
        try
        {
            return player.isPlaying();
        }
        catch (Exception e)
        {
        }
        return false;
    }

    public boolean isPreparing ()
    {
        return getState() == OBAP_PREPARING;
    }

    public boolean isIdle ()
    {
        return getState() == OBAP_IDLE;
    }

    @Override
    public void onPrepared (MediaPlayer mp)
    {
        if (fromTime > 0)
        {
            setState(OBAP_SEEKING);
            player.seekTo((int) fromTime);
        }
        else
        {
            setState(OBAP_PLAYING);

            playerLock.lock();
            condition.signalAll();
            playerLock.unlock();


            player.start();
        }
    }

    @Override
    public void onCompletion (MediaPlayer mp)
    {
        setState(OBAP_IDLE);
        MediaPlayer cpplayer = player;
        player = null;
        playerLock.lock();
        condition.signalAll();
        playerLock.unlock();
        try
        {
            cpplayer.reset();
            cpplayer.release();
        }
        catch (Exception e)
        {
        }
    }

    double duration ()
    {
        if (player != null)
        {
            waitPrepared();
            return player.getDuration() / 1000.0;
        }
        return 0.0;
    }

    @Override
    public void onSeekComplete (MediaPlayer mp)
    {
        setState(OBAP_PLAYING);
        playerLock.lock();
        condition.signalAll();
        playerLock.unlock();
        player.start();
    }
}