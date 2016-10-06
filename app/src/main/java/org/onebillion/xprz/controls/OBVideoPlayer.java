package org.onebillion.xprz.controls;

import android.content.res.AssetFileDescriptor;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import android.util.Log;
import android.util.Size;
import android.view.Surface;


import org.onebillion.xprz.glstuff.OBRenderer;
import org.onebillion.xprz.glstuff.SurfaceShaderProgram;

import org.onebillion.xprz.glstuff.TextureRect;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBViewController;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBUtils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by michal on 11/07/16.
 */
public class OBVideoPlayer extends OBControl
        implements SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnErrorListener,MediaPlayer.OnVideoSizeChangedListener
{
    public Surface surface;
    public SurfaceTexture surfaceTexture;
    public Lock playerLock;
    public boolean activityPaused,playAfterPrepare=true,stopOnCompletion=true;
    public static int VP_FILL_TYPE_STRETCH = 0,
    VP_FILL_TYPE_ASPECT_FIT = 1,
    VP_FILL_TYPE_ASPECT_FILL = 2;


    int fillType = VP_FILL_TYPE_ASPECT_FIT;
    Condition condition;
    long fromTime;
    private int textureId;
    private Size previewSize;
    private MediaPlayer player;
    boolean mirrored;

    public OBVideoPlayer(RectF frame, XPRZ_SectionController sectionController,boolean mirrored,boolean _playAfterPrepare)
    {
        setFrame(frame.left, frame.top, frame.right, frame.bottom);
        this.mirrored = mirrored;
        if (mirrored)
            setScaleX(-1);
        textureId = MainActivity.mainActivity.renderer.textureObjectIds[2];
        rebuildTexture();
        playerLock = new ReentrantLock();
        playAfterPrepare = _playAfterPrepare;
        activityPaused = false;

    }
    public OBVideoPlayer(RectF frame, XPRZ_SectionController sectionController)
    {
        this(frame,sectionController,true,true);
    }

    public boolean isPlaying()
    {
        if (player == null)
            return false;
        try
        {
            return player.isPlaying();
        }
        catch(Exception e)
        {

        }
        return false;
    }
    private void rebuildTexture()
    {
        surfaceTexture = new SurfaceTexture(textureId);

        surface = new Surface(surfaceTexture);
        surfaceTexture.setOnFrameAvailableListener(this);
    }


    public void setPreviewSize(Size size)
    {
        previewSize = size;
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
    }

    public void render(OBRenderer renderer, OBViewController vc, float[] modelViewMatrix)
    {
        if (!hidden && bounds().width() > 0 && bounds().height() > 0 && previewSize != null && surfaceTexture != null)
        {
            matrix3dForDraw();
            android.opengl.Matrix.multiplyMM(tempMatrix, 0, modelViewMatrix, 0, modelMatrix, 0);
            float op = opacity();
            float[] finalCol = new float[4];
            for (int i = 0; i < 3; i++)
            {
                finalCol[i] = blendColour[i];
            }
            finalCol[3] = blendColour[3] * op;
            SurfaceShaderProgram surfaceShader = (SurfaceShaderProgram) renderer.surfaceProgram;
            surfaceShader.useProgram();
            surfaceShader.setUniforms(tempMatrix, textureId, finalCol);
            renderLayer(renderer, vc);
        }
    }

    public void renderLayer(OBRenderer renderer, OBViewController vc)
    {
        TextureRect tr = renderer.textureRect;

        float boundsWidth = bounds.right - bounds.left;
        float boundsHeight = bounds.bottom - bounds.top;

        int cameraWidth = previewSize.getWidth();
        int cameraHeight = previewSize.getHeight();

        float wratio = cameraWidth/boundsWidth;
        float hratio = cameraHeight/boundsHeight;

        float ratio1 = cameraWidth * 1.0f / cameraHeight;
        float ratio2 = boundsWidth / boundsHeight;

        float widthRatio = 0;
        float heightRatio = 0;

        if (fillType == VP_FILL_TYPE_ASPECT_FIT)
        {
            if (ratio1 > ratio2)
            {
                widthRatio = ((cameraWidth - boundsWidth*hratio) / 2.0f) / cameraWidth;
            } else if (ratio1 < ratio2)
            {
                heightRatio = ((cameraHeight - boundsHeight*wratio) / 2.0f) / cameraHeight;
            }
        }


        tr.setUVs(widthRatio, heightRatio, 1 - widthRatio, 1 - heightRatio);
        tr.drawSurface(renderer, 0, 0, boundsWidth, boundsHeight, surfaceTexture,mirrored);
        tr.setUVs(0, 0, 1, 1);

    }


    synchronized public void onFrameAvailable(SurfaceTexture surface)
    {
        if(!activityPaused)
            invalidate();
    }

    public void stop()
    {
        if (player != null)
        {
            MediaPlayer cpplayer = player;
            player = null;
            finishVideoWait();
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


    public void startPlayingAtTime(AssetFileDescriptor afd, long fr)
    {
        if(afd == null || activityPaused )
            return;
        condition = playerLock.newCondition();
        fromTime = fr;

        player = new MediaPlayer();
        player.setSurface(surface);
        //surface.release();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnErrorListener(this);
        player.setOnVideoSizeChangedListener(this);

        try
        {
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            player.prepareAsync();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp)
    {
        if (stopOnCompletion)
            stop();
    }

    public void pause()
    {
        try
        {
            player.pause();
        }
        catch (Exception e)
        {
        }
    }

    public void start()
    {
        try
        {
            player.start();
        }
        catch (Exception e)
        {
        }
    }

    public void seekTo(int fromTime)
    {
        try
        {
            player.seekTo(fromTime);
        }
        catch(Exception e)
        {

        }
    }
    @Override
    public void onPrepared(MediaPlayer mp)
    {
        if (!playAfterPrepare)
        {
            player.seekTo(0);
            return;
        }
        if (fromTime > 0)
        {
            seekTo((int) fromTime);
        }
        else
        {
            start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp)
    {
        if (playAfterPrepare)
            start();
        else
        {
            //start();
            //pause();
        }

    }

    public void waitForVideo()
    {
        if(condition == null)
            return;

        playerLock.lock();
        try
        {
            condition.await();
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            playerLock.unlock();
        }

    }

    private void finishVideoWait()
    {
        if(condition == null)
            return;

        playerLock.lock();
        try
        {
            condition.signalAll();
        }
        finally
        {
            playerLock.unlock();

        }

        condition = null;
    }

    public void onPause()
    {
        activityPaused = true;
        stop();
    }

    public void onResume()
    {
        rebuildTexture();
        activityPaused = false;
    }

    public MediaPlayer mediaPlayer()
    {
        return player;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.i("mediaplayer",String.format("%d %d",what,extra));
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height)
    {
        setPreviewSize(new Size(width, height));
        invalidate();
    }

    public int fillType()
    {
        return fillType;
    }

    public void setFillType(int fillType)
    {
        this.fillType = fillType;
    }

    public void cleanUp(OBRenderer renderer)
    {
        activityPaused = true;
        stop();

        surfaceTexture.setOnFrameAvailableListener(null);
        surfaceTexture.release();
        TextureRect tr = renderer.textureRect;
        tr.unbindSurface(renderer);

    }

}

