package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBCameraManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBVideoRecorder;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OC_FatController;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 13/04/2017.
 */

public class OC_PlayZoneVideo extends OC_SectionController
{
    OBVideoPlayer videoPlayer;
    OBCameraManager cameraManager;
    OBVideoRecorder videoRecorder;

    boolean videoOrientationFront;
    OBControl screenOverlay;
    boolean recording;
    String currentFileName, videoFilePath, thumbnailFilePath, videoFileName, thumbnalFileName;


    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        screenOverlay = objectDict.get("video_overlay");
        screenOverlay.setFrame(new RectF(bounds()));
        OBControl screenView = objectDict.get("video_frame");
        screenView.setFrame(new RectF(bounds()));
        videoOrientationFront = false;
        //objectDict.get("recording_button").setZPosition(10);
        //objectDict.get("camera_icon").setZPosition(10);
        String fileName = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
        currentFileName  = String.format("video_%s",fileName);
        List<String> files = OC_PlayZoneAsset.assetsNamesForNewFile(OC_PlayZoneAsset.ASSET_VIDEO);
        thumbnalFileName = files.get(0);
        videoFileName = files.get(1);
        thumbnailFilePath = OC_PlayZoneAsset.pathToAsset(thumbnalFileName);
        videoFilePath = OC_PlayZoneAsset.pathToAsset(videoFileName);

        videoPlayer = new OBVideoPlayer(screenView.frame(),this,true, false);
        videoRecorder = new OBVideoRecorder(videoFilePath,this);
        cameraManager = new OBCameraManager(this);
        videoPlayer.setZPosition(screenView.zPosition());
        attachControl(videoPlayer);
        videoOrientationFront = true;
    }

    public int buttonFlags()
    {
        return  MainViewController().SHOW_TOP_LEFT_BUTTON;

    }

    public void setSceneXX(String  scene)
    {
        super.setSceneXX(scene);

    }
    public void doMainXX()
    {

    }


    public void exitEvent()
    {
        try
        {
            cameraManager.onPause();
            videoRecorder.onPause();
            videoPlayer.cleanUp(MainActivity.mainActivity.renderer);
            OBUtils.deleteEmptyFilesInFolder("pzvideo", this);
        } catch(Exception e)
        {
            e.printStackTrace();
        }

        super.exitEvent();
    }


    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {

                cameraManager.startPreviewForRecording(videoPlayer,videoRecorder, videoOrientationFront ? OBCameraManager.CAMERA_FRONT : OBCameraManager.CAMERA_BACK);
                setStatus(STATUS_AWAITING_CLICK);
            }
        });

    }


    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            if (finger(0, 2, Arrays.asList(objectDict.get("camera_icon")), pt) != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        toggleVideoOrientation();
                        setStatus(STATUS_AWAITING_CLICK);
                    }
                });

            }
            else if (finger(0, 2, Arrays.asList(objectDict.get("recording_button")), pt) != null)
            {
                setStatus(STATUS_BUSY);
                final OBGroup button = (OBGroup)objectDict.get("recording_button");
                button.objectDict.get("highlight").show();
                recording = true;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        playSFX("click");
                        waitForSecs(0.2f);
                        button.hide();
                        animateRecordingPulse();
                        videoRecorder.startMediaRecorderAndTimer(-1);
                        OBUtils.runOnOtherThreadDelayed(2, new OBUtils.RunLambda()
                        {
                            public void run() throws Exception
                            {
                                finishRecording();
                            }

                        });
                    }
                });
            }
        }
    }


    public void toggleVideoOrientation() throws Exception
    {
        videoOrientationFront = !videoOrientationFront;
        cameraManager.stopWithWait();
        videoPlayer.clearDisplay();
        videoPlayer.setDisplayMirrored(videoOrientationFront);
        cameraManager.startPreviewForRecording(videoPlayer, videoRecorder, videoOrientationFront ? OBCameraManager.CAMERA_FRONT : OBCameraManager.CAMERA_BACK);

    }

    public void finishRecording() throws Exception
    {
        if (recording)
        {
            recording = false;
            videoRecorder.stopRecording();
            cameraManager.stopPreview();
            playSFX("click");

            try
            {
                Bitmap bitmap = getFirstFrameForVideo(videoFilePath);
                Matrix m = new Matrix();
                float thumbScale = 0.3f;
                m.preScale((videoOrientationFront ? -1 : 1) * thumbScale, thumbScale);
                Bitmap dst = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);

                dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
                FileOutputStream out = new FileOutputStream(thumbnailFilePath);
                dst.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.close();

                OC_FatController fatController = (OC_FatController)MainActivity.mainActivity.fatController;
                Map<String,String> map = new ArrayMap<>();
                map.put("video", videoFileName);
                map.put("mirrored",String.valueOf(videoOrientationFront));
                fatController.savePlayZoneAssetForCurrentUserType(OC_PlayZoneAsset.ASSET_VIDEO,thumbnalFileName,map);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        animateRecordFinish();
                    }
                });
            }catch (Exception e)
            {
                exitEvent();
            }
        }
    }


    public void animateRecordFinish() throws Exception
    {
        OBControl videoFrame = videoPlayer;
        float shrinkScale = 0.4f;
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.propertyAnim("scaleX", (videoFrame.scaleX() > 0 ? 1 : -1) * shrinkScale, videoFrame),
                OBAnim.propertyAnim("scaleY", shrinkScale, videoFrame))
                , 0.3, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        waitForSecs(0.2f);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float) Math.toRadians(-360), videoFrame),
                OBAnim.moveAnim(OB_Maths.locationForRect(-0.5f, 0.5f, bounds()), videoFrame)), 0.5, true, OBAnim.ANIM_EASE_IN, this);
        if (!this._aborting)
            exitEvent();
    }


    public void animateRecordingPulse()
    {
        final OBSectionController controller = this;
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                OBGroup recordingControl = (OBGroup) objectDict.get("recording_pulse");
                lockScreen();
                screenOverlay.hide();
                objectDict.get("camera_icon").hide();
                recordingControl.show();
                MainViewController().topLeftButton.hide();

                unlockScreen();
                OBControl pulse = recordingControl.objectDict.get("pulse");
                float scale = pulse.scale();
                while (recording && !_aborting)
                {
                    lockScreen();
                    pulse.setScale(scale);
                    pulse.setOpacity(1);

                    unlockScreen();
                    OBAnimationGroup.runAnims(Arrays.asList(OBAnim.scaleAnim(2f, pulse), OBAnim.opacityAnim(0, pulse)), 2, true, OBAnim.ANIM_EASE_OUT, controller);

                }

            }

        });
    }

    public Bitmap getFirstFrameForVideo(String vidPath)
    {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(vidPath);
        Bitmap bitmap = mmr.getFrameAtTime(0);
        mmr.release();
        return bitmap;

    }

   /* @Override
    public void onResume()
    {
        videoRecorder.onResume();
        videoPlayer.onResume();
        cameraManager.onResume();
        super.onResume();
    }*/

    @Override
    public void onPause()
    {
        if (recording)
        {
            recording = false;
            videoRecorder.stopRecording();
        }
        this.exitEvent();

    }


}
