package com.maq.xprize.onecourse.utils;

/**
 * Created by alan on 11/10/15.
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;

import com.maq.xprize.onecourse.mainui.MainActivity;

public class OBAudioPlayer extends OBGeneralAudioPlayer implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener
{
    public MediaPlayer player;
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

    public void stopPlaying()
    {
        stopPlaying(false);
    }

    public void stopPlaying (boolean stopNow)
    {
        synchronized (this)
        {
            setState(OBAP_IDLE);
            if (player != null)
            {
                final MediaPlayer cpplayer = player;
                player = null;
                try
                {
                    cpplayer.setOnPreparedListener(null);
                    cpplayer.setOnCompletionListener(null);
                    cpplayer.setOnSeekCompleteListener(null);
                }
                catch (Exception e)
                {
                    // do nothing
                }
                try
                {
                    if (cpplayer.isPlaying())
                        cpplayer.stop();
                }
                catch (Exception e)
                {
                    // do nothing
                }
                playerLock.lock();
                condition.signalAll();
                playerLock.unlock();
                //
                if (stopNow)
                {
                    cpplayer.reset();
                    cpplayer.release();
                }
                else
                {
                    try
                    {
                        Runnable runnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                cpplayer.reset();
                                cpplayer.release();
                            }
                        };
                        Handler handler = new Handler();
                        handler.postDelayed(runnable, 250);
                    }
                    catch (java.lang.RuntimeException runTimeException)
                    {
                        // do nothing --> prevent spam of "Can't create handler inside thread that has not called Looper.prepare()"
                    }
                    catch (Exception e)
                    {
                        // if it's something else, then show it on logs
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void startPlaying (AssetFileDescriptor afd)
    {
        synchronized (this)
        {
            if (isPlaying())
                stopPlaying(true);
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
    }

    public void startPlayingAtTime (AssetFileDescriptor afd, long fr)
    {
        startPlayingAtTimeVolume(afd,fr,1.0f);
    }

    public void startPlayingAtTimeVolume(AssetFileDescriptor afd, long fr,float vol)
    {
        if (isPlaying())
            stopPlaying();
        volume = vol;
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
        if (getState() == OBAP_IDLE || player == null)
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
        if (getState() == OBAP_IDLE || player == null)
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
        if (player == null)
        {
            MainActivity.log("OBAudioPlayer.onPrepared.player is null");
            //
            setState(OBAP_IDLE);
            //
            playerLock.lock();
            condition.signalAll();
            playerLock.unlock();
        }
        else
        {
            player.setVolume(volume, volume);
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
    }

    @Override
    public void onCompletion (MediaPlayer mp)
    {
        setState(OBAP_IDLE);
        final MediaPlayer cpplayer = player;
        player = null;
        playerLock.lock();
        condition.signalAll();
        playerLock.unlock();
        try
        {
            Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    if(cpplayer != null)
                        cpplayer.reset();

                    if(cpplayer != null)
                        cpplayer.release();
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable,250);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public double duration ()
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

    public int currentPositionms()
    {
        if (player != null)
            return player.getCurrentPosition();
        return -1;
    }
}