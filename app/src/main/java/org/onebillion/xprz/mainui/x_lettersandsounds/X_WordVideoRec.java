package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Environment;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Size;
import android.view.View;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBVideoPlayer;
import org.onebillion.xprz.controls.XPRZ_Presenter;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBCameraManager;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBVideoRecorder;
import org.onebillion.xprz.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 11/07/16.
 */
public class X_WordVideoRec extends XPRZ_SectionController
{
    static final int MODE_WORD = 1;
    String text;

    OBLabel wordLabel;
    float expectedAudioLenght;
    int  currentWord, currentMode;
    OBControl lastFrame;
    XPRZ_Presenter presenter;
    List<Map<String,Object>> wordEvents;
    OBVideoRecorder videoRecorder;
    OBVideoPlayer videoPlayer;
    OBCameraManager cameraManager;
    OBGroup nextButton;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");

        OBGroup presenterbox = (OBGroup)objectDict.get("presenterbox");
        presenter = XPRZ_Presenter.characterWithGroup((OBGroup)presenterbox.objectDict.get("presenter"));
        presenter.faceFront();

        Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);

        wordEvents = new ArrayList<>();

        for(String wordid : parameters.get("words").split(","))
        {
            Map<String,Object> dict = new ArrayMap<>();


            int mode = -1;

            mode = MODE_WORD;
            OBWord readingWord = (OBWord)componentDict.get(wordid);
            dict.put("word",readingWord);

            dict.put("mode",mode);
            wordEvents.add(dict);
        }

        currentWord = 1;

        if(parameters.get("demo").equalsIgnoreCase("true") &&
        parameters.get("presenter").equalsIgnoreCase("true"))
        objectDict.get("presenterbox").show();
        videoRecorder = new OBVideoRecorder(OBUtils.getFilePathForTempFile(this),this);
        videoPlayer = new OBVideoPlayer(objectDict.get("videobox").frame(),this);
        videoPlayer.setZPosition(100);

        cameraManager = new OBCameraManager(this);
        attachControl(videoPlayer);


        nextButton = loadVectorWithName("arrow_next", new PointF(0, 0), new RectF(bounds()));
        nextButton.setScale((bounds().height() * 0.1f) / nextButton.height());
        nextButton.setBottom(bounds().height() - applyGraphicScale(10));
        nextButton.setRight(bounds().width() - applyGraphicScale(10));
        attachControl(nextButton);
        nextButton.hide();


        //setWordScene(currentWord);
    }

    @Override
    public void exitEvent()
    {
        onPause();
        OBUtils.cleanUpTempFiles(this);
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

            }
        });

    }


    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {

    }

    public void showVideoPreview()
    {
        cameraManager.startPreview(videoPlayer);
    }


    public void startVideoRecorder(double audioLength)
    {
        cameraManager.startRecording(videoPlayer, videoRecorder, audioLength);
    }

    public void waitForVideoRecorder() throws Exception
    {
        videoRecorder.waitForRecord();
        videoRecorder.stopRecording();
        cameraManager.stopPreview();
    }

    public void replayRecordedVideo()
    {
        videoPlayer.startPlayingAtTime(videoRecorder.getRecordingPath(), 0);
        videoPlayer.waitForVideo();
    }

    @Override
    public void onResume()
    {
        videoRecorder.onResume();
        videoPlayer.onResume();
        cameraManager.onResume();
    }

    @Override
    public void onPause()
    {
        try
        {
            videoRecorder.onPause();
            videoPlayer.onPause();
            cameraManager.onPause();

        } catch(Exception e)
        {

        }

    }
}
