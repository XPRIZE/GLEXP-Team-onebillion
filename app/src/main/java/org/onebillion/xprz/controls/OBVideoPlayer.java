package org.onebillion.xprz.controls;

import android.content.res.AssetFileDescriptor;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

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
        implements SurfaceTexture.OnFrameAvailableListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener
{
    public Surface surface;
    public SurfaceTexture surfaceTexture;
    public Lock playerLock;
    public boolean activityPaused;
    Condition condition;
    long fromTime;
    private int textureId;
    private Size previewSize;
    private MediaPlayer player;

    public OBVideoPlayer(RectF frame, XPRZ_SectionController sectionController)
    {
        setFrame(frame.left, frame.top, frame.right, frame.bottom);
        setScaleX(-1);
        textureId = MainActivity.mainActivity.renderer.textureObjectIds[2];
        rebuildTexture();
        playerLock = new ReentrantLock();

        activityPaused = false;

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

        if (ratio1 > ratio2)
        {
            widthRatio = ((cameraWidth - boundsWidth*hratio) / 2.0f) / cameraWidth;
        } else if (ratio1 < ratio2)
        {
            heightRatio = ((cameraHeight - boundsHeight*wratio) / 2.0f) / cameraHeight;
        }


        tr.setUVs(widthRatio, heightRatio, 1 - widthRatio, 1 - heightRatio);
        tr.drawSurface(renderer, 0, 0, boundsWidth, boundsHeight, surfaceTexture);
        tr.setUVs(0, 0, 1, 1);

    }


    synchronized public void onFrameAvailable(SurfaceTexture surface)
    {
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



    public void startPlayingAtTime(String filePath, long fr)
    {
        if(filePath == null || activityPaused )
            return;
        condition = playerLock.newCondition();
        fromTime = fr;
        AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(filePath);
        player = new MediaPlayer();
        player.setSurface(surface);
        //surface.release();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);


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
        stop();
    }

    @Override
    public void onPrepared(MediaPlayer mp)
    {
        if (fromTime > 0)
        {
            player.seekTo((int) fromTime);
        }
        else
        {

            player.start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp)
    {
        player.start();
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

}

