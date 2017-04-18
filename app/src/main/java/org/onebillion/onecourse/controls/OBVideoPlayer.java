package org.onebillion.onecourse.controls;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;

import android.opengl.GLES20;
import android.util.Log;
import android.util.Size;
import android.view.Surface;


import org.onebillion.onecourse.glstuff.ColorShaderProgram;
import org.onebillion.onecourse.glstuff.GradientRect;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.glstuff.SurfaceShaderProgram;

import org.onebillion.onecourse.glstuff.TextureRect;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


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


    int fillType = VP_FILL_TYPE_ASPECT_FILL;
    Condition condition;
    long fromTime;
    private int textureId;
    private Size previewSize;
    //
    public MediaPlayer player;
    //
    private int backgroundFillColour;
    boolean mirrored;
    public boolean frameIsAvailable = false;

    public OBVideoPlayer(RectF frame, OC_SectionController sectionController, boolean mirrored, boolean _playAfterPrepare)
    {
        activityPaused = true;
        setFrame(frame.left, frame.top, frame.right, frame.bottom);
        this.mirrored = false;
        setDisplayMirrored(mirrored);
        textureId = MainActivity.mainActivity.renderer.textureObjectIds[2];
        rebuildTexture();
        playerLock = new ReentrantLock();
        playAfterPrepare = _playAfterPrepare;
        activityPaused = false;
        setBackgroundFillColour(Color.BLACK);

    }
    public OBVideoPlayer(RectF frame, OC_SectionController sectionController)
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

    public void setDisplayMirrored(boolean mirrored)
    {
        if(mirrored != this.mirrored)
        {
            this.mirrored = mirrored;
            setScaleX((this.mirrored ? -1 : 1)* Math.abs(scaleX()));
        }
    }


    public void setBackgroundFillColour(int colour)
    {
        backgroundFillColour = colour;
    }

    private void rebuildTexture()
    {
        surfaceTexture = new SurfaceTexture(textureId);

        surface = new Surface(surfaceTexture);
        surfaceTexture.setOnFrameAvailableListener(this);
        clearSurface(surfaceTexture);
    }

    public void clearDisplay()
    {
        clearSurface(surfaceTexture);
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

            //draw background
            ColorShaderProgram colourShader = (ColorShaderProgram) renderer.colourProgram;
            float col[] = new float[4];
            OBUtils.setFloatColour( Color.red(backgroundFillColour) / 255.0f, Color.green(backgroundFillColour) / 255.0f, Color.blue(backgroundFillColour) / 255.0f, 1, col);
            colourShader.useProgram();
            colourShader.setUniforms(tempMatrix);
            GradientRect gr = renderer.gradientRect;
            gr.draw(renderer, 0, 0, bounds.right - bounds.left, bounds.bottom - bounds.top, col, col);

            //draw video
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
        if (!frameIsAvailable)
            return;
        TextureRect tr = renderer.textureRect;

        float boundsWidth = bounds.right - bounds.left;
        float boundsHeight = bounds.bottom - bounds.top;

        int cameraWidth = previewSize.getWidth();
        int cameraHeight = previewSize.getHeight();
        //MainActivity.log(String.format("video size %d %d",cameraWidth,cameraHeight));
        float wratio = cameraWidth/boundsWidth;
        float hratio = cameraHeight/boundsHeight;

        float ratio1 = cameraWidth * 1.0f / cameraHeight;
        float ratio2 = boundsWidth / boundsHeight;

        float widthRatio = 0;
        float heightRatio = 0;

        float left=0,right=boundsWidth,top=0,bottom=boundsHeight;
        if (fillType == VP_FILL_TYPE_ASPECT_FIT)
        {
            float oldratio1 = ratio1;
            ratio1 = ratio2;
            ratio2 = oldratio1;

            float ratiow = boundsWidth / cameraWidth;
            float ratioh = boundsHeight / cameraHeight;
            if (ratioh > ratiow)
            {
                top = (boundsHeight - cameraHeight * ratiow) / 2;
                bottom = boundsHeight - top;
            }
            else if (ratioh < ratiow)
            {
                left = (boundsWidth - cameraWidth * ratioh) / 2;
                right = boundsHeight - left;
            }
        }

        if (ratio1 > ratio2)
        {
            widthRatio = -1*((cameraWidth - boundsWidth*hratio) / 2.0f) / cameraWidth;
        } else if (ratio1 < ratio2)
        {
            heightRatio = -1*((cameraHeight - boundsHeight*wratio) / 2.0f) / cameraHeight;
        }

        float targetUVW = 0, targetUVH = 0;

        if(widthRatio < 0)
        {
            targetUVW = -1*widthRatio;
            widthRatio = 0;
        }

        if(heightRatio < 0)
        {
            targetUVH = -1*heightRatio;
            heightRatio = 0;
        }

        tr.setUVs(targetUVW, targetUVH, 1 - targetUVW, 1 - targetUVH);
        //tr.drawSurface(renderer, widthRatio*boundsWidth, heightRatio*boundsHeight, (1 - widthRatio)*boundsWidth, (1 - heightRatio)*boundsHeight, surfaceTexture, mirrored);
        tr.drawSurface(renderer, left,top,right,bottom, surfaceTexture, mirrored);
        tr.setUVs(0, 0, 1, 1);
    }


    synchronized public void onFrameAvailable(SurfaceTexture surface)
    {
        frameIsAvailable = true;
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
        startPlayingAtTime(afd, fr, this, this);
    }


    public void startPlayingAtTime(AssetFileDescriptor afd, long fr, MediaPlayer.OnPreparedListener preparedListener, MediaPlayer.OnCompletionListener completionListener)
    {
        if(afd == null || activityPaused )
            return;

        condition = playerLock.newCondition();
        fromTime = fr;

        player = new MediaPlayer();
        player.setSurface(surface);
        //surface.release();
        player.setOnPreparedListener(preparedListener);
        player.setOnCompletionListener(completionListener);
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
        invalidate();
    }

    public void cleanUp(OBRenderer renderer)
    {
        activityPaused = true;
        stop();

        surfaceTexture.setOnFrameAvailableListener(null);
        //surfaceTexture.release();*
        TextureRect tr = renderer.textureRect;
        tr.unbindSurface(renderer);

    }

    // stolen from https://gist.github.com/HugoGresse/5ca05821444353a823bb
    private void clearSurface(SurfaceTexture texture)
    {
        if(texture == null){
            return;
        }

        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl.eglInitialize(display, null);

        int[] attribList = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL10.EGL_WINDOW_BIT,
                EGL10.EGL_NONE, 0,
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        egl.eglChooseConfig(display, attribList, configs, configs.length, numConfigs);
        EGLConfig config = configs[0];
        EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{
                12440, 2,
                EGL10.EGL_NONE
        });
        EGLSurface eglSurface = egl.eglCreateWindowSurface(display, config, texture,
                new int[]{
                        EGL10.EGL_NONE
                });

        egl.eglMakeCurrent(display, eglSurface, eglSurface, context);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        egl.eglSwapBuffers(display, eglSurface);
        egl.eglDestroySurface(display, eglSurface);
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
        egl.eglDestroyContext(display, context);
        egl.eglTerminate(display);
    }


    public int currentPosition()
    {
        try
        {
            return player.getCurrentPosition();
        }
        catch (Exception e)
        {
        }
        return -1;
    }

    public int duration()
    {
        try
        {
            return player.getDuration();
        }
        catch (Exception e)
        {
            return -1;
        }
    }
}

