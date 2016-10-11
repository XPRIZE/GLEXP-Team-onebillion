package org.onebillion.xprz.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Size;
import android.view.Surface;


import org.onebillion.xprz.controls.OBVideoPlayer;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Collections.min;

/**
 * Created by michal on 14/07/16.
 */
public class OBCameraManager
{
    public CaptureRequest.Builder previewBuilder;
    public Size previewSize;
    public Size recordingSize;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private CameraManager cameraManager;
    private boolean activityPaused;
    private Lock cameraLock;
    private Condition condition;
    private WeakReference<XPRZ_SectionController> controller;

    private Semaphore cameraSemaphore;

    public OBCameraManager(XPRZ_SectionController sectionController)
    {
        cameraSemaphore = new Semaphore(1);
        activityPaused = false;
        cameraLock = new ReentrantLock();

       // startCameraLock();

        controller = new WeakReference<>(sectionController);

       // startCamera();
    }


    private void startCamera()
    {
        startCameraLock();
        OBUtils.runOnMainThread(
                new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {

                        try
                        {
                            cameraManager = (CameraManager) controller.get().activity.getSystemService(Context.CAMERA_SERVICE);
                            if (!cameraSemaphore.tryAcquire(3, TimeUnit.SECONDS))
                            {
                                throw new RuntimeException("Camera not retrieved");
                            }
                            startBackgroundThread();
                            for (String cameraId : cameraManager.getCameraIdList())
                            {
                                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);


                                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                                {
                                    try
                                    {
                                        cameraManager.openCamera(cameraId, new CameraDevice.StateCallback()
                                                {

                                                    @Override
                                                    public void onOpened(CameraDevice camDevice)
                                                    {
                                                        cameraSemaphore.release();
                                                        cameraDevice = camDevice;
                                                        finishCameraWait();

                                                    }

                                                    @Override
                                                    public void onDisconnected(CameraDevice camDevice)
                                                    {
                                                        cameraSemaphore.release();
                                                        cameraDevice.close();
                                                        cameraDevice = null;
                                                        finishCameraWait();
                                                    }

                                                    @Override
                                                    public void onError(CameraDevice camDevice, int error)
                                                    {
                                                        cameraSemaphore.release();
                                                        cameraDevice.close();
                                                        cameraDevice = null;
                                                        finishCameraWait();
                                                    }

                                                    @Override
                                                    public void onClosed(CameraDevice camDevice)
                                                    {
                                                        cameraSemaphore.release();
                                                        cameraDevice = null;
                                                        finishCameraWait();
                                                    }
                                                },
                                                null);
                                        break;
                                    } catch (SecurityException e)
                                    {
                                        Log.d("Camera error", e.getLocalizedMessage());
                                    }

                                }
                            }

                        } catch (Exception e)
                        {
                            Log.d("Camera error", e.getLocalizedMessage());
                        }
                    }
                }
        );



    }

    private void startBackgroundThread()
    {
        backgroundThread = new HandlerThread("CameraBackgroundThreadHandler");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }



    public void stop() throws Exception
    {
        /*
        try
        {
            cameraSemaphore.tryAcquire(3, TimeUnit.SECONDS);
        } catch (Exception e)
        {

        }
        */
        stopPreview(false);

        try
        {
            if (cameraDevice != null)
                cameraDevice.close();
        }catch (Exception e)
        {

        }

        cameraDevice = null;

        stopBackgroundThread();

        cameraSemaphore.release();
    }


    private void stopBackgroundThread()
    {
        if (backgroundThread != null)
        {
            backgroundThread.quitSafely();
            try
            {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }


    private void startPreviewForControls(final OBVideoPlayer videoPlayer, final OBVideoRecorder videoRecorder)
    {
        if (videoPlayer == null || activityPaused || controller.get()._aborting || videoPlayer.activityPaused)
            return;

        try
        {
            if(cameraDevice == null)
            {
                startCamera();
                waitForCameraReady();
                if(cameraDevice == null)
                    throw new Exception("Error connecting to the camera!");
            }
            controller.get().lockScreen();
            stopPreview(true);
            startCameraLock();
            calculatePreviewAndRecordingSizes(videoPlayer.width(), videoPlayer.height());
            videoPlayer.setPreviewSize(previewSize);

            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            List<Surface> surfaces = new ArrayList<>();

            surfaces.add(videoPlayer.surface);
            if (videoRecorder != null)
            {
                videoRecorder.prepareForVideoRecording(recordingSize);
                surfaces.add(videoRecorder.getSurface());
            }

            for (Surface surface : surfaces)
                previewBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback()
            {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession)
                {
                    captureSession = cameraCaptureSession;

                    updatePreview(videoPlayer);


                    finishCameraWait();

                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession)
                {
                    captureSession = null;
                    finishCameraWait();
                }

                @Override
                public void onClosed(CameraCaptureSession session)
                {
                    captureSession = null;
                    finishCameraWait();
                }
            }, backgroundHandler);
            waitForCameraReady();
            controller.get().unlockScreen();
        } catch (CameraAccessException e)
        {
            e.printStackTrace();
            finishCameraWait();
        } catch (Exception e)
        {
            e.printStackTrace();
            finishCameraWait();
        }
    }

    public void startPreview(OBVideoPlayer videoPlayer)
    {
        startPreviewForControls(videoPlayer, null);
    }


    public void startPreviewForRecording(final OBVideoPlayer videoPlayer, final OBVideoRecorder videoRecorder)
    {
        startPreviewForControls(videoPlayer, videoRecorder);
    }


    private void calculatePreviewAndRecordingSizes(float width, float height)
    {
        if (cameraDevice == null)
            return;
        try
        {
            recordingSize = null;
            previewSize = null;
            CameraCharacteristics cameraChar = cameraManager.getCameraCharacteristics(cameraDevice.getId());

            StreamConfigurationMap configMap = cameraChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            Size[] mediaRecorderSizes = configMap.getOutputSizes(MediaRecorder.class);
            for (Size size : mediaRecorderSizes)
            {
                if (size.getHeight() <= 720 && size.getHeight() * 16 == size.getWidth() * 9)
                {
                    recordingSize = size;
                    break;
                }
            }
            if (recordingSize == null)
                recordingSize = mediaRecorderSizes[mediaRecorderSizes.length - 1];


            Size[] sizes = configMap.getOutputSizes(SurfaceTexture.class);
            List<Size> availableSizes = new ArrayList<>();

            for (Size option : sizes)
            {
                if (recordingSize.getWidth() * option.getHeight() == recordingSize.getHeight() * option.getWidth() &&
                        option.getWidth() >= width && option.getHeight() >= height)
                {
                    availableSizes.add(option);
                }
            }

            if (availableSizes.size() > 0)
            {
                previewSize = min(availableSizes, new Comparator<Size>()
                {
                    @Override
                    public int compare(Size lhs, Size rhs)
                    {
                        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                                (long) rhs.getWidth() * rhs.getHeight());
                    }
                });
            } else
            {
                previewSize = sizes[0];
            }

        } catch (Exception e)
        {
            previewSize = new Size(1920, 1080);
            recordingSize = new Size(1920, 1080);
            e.printStackTrace();

        }
    }


    private void updatePreview(final OBVideoPlayer videoPlayer)
    {
        if (null == cameraDevice)
        {
            return;
        }
        try
        {
            setUpCaptureRequestBuilder(previewBuilder);

            captureSession.setRepeatingRequest(previewBuilder.build(),
                    new CameraCaptureSession.CaptureCallback()
                    {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result)
                        {
                            videoPlayer.invalidate();
                            super.onCaptureCompleted(session, request, result);
                        }
                    }
                    , backgroundHandler);
        } catch (CameraAccessException e)
        {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder)
    {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }


    public void stopPreview() throws Exception
    {
        stopPreview(true);

    }

    private void stopPreview(Boolean wait) throws Exception
    {
        if (captureSession != null)
        {
            if (wait)
                startCameraLock();
            captureSession.close();
            if (wait)
                waitForCameraReady();

            captureSession = null;

        }
    }

    private void startCameraLock()
    {
        condition = cameraLock.newCondition();
    }

    public void waitForCameraReady() throws Exception
    {
        if (condition == null)
            return;

        controller.get().checkAbort();

        cameraLock.lock();
        try
        {
            condition.await();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cameraLock.unlock();
        }
        controller.get().checkAbort();
    }

    private void finishCameraWait()
    {
        if (condition == null)
            return;
        cameraLock.lock();
        try
        {
            condition.signalAll();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            cameraLock.unlock();

        }
        condition = null;
    }

    public void onResume()
    {
        activityPaused = false;
    }

    public void onPause() throws Exception
    {
        activityPaused = true;
        stop();

    }

}
