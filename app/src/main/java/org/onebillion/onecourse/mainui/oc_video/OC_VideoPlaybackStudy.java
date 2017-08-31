package org.onebillion.onecourse.mainui.oc_video;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.mainui.OBSectionController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by alan on 30/08/2017.
 */

public class OC_VideoPlaybackStudy extends OC_SectionController
{
    OBPresenter presenter;
    OBGroup presenter_group;
    OBControl video;
    OBVideoPlayer videoPlayer;
    boolean presenterIsTalking;

    public void prepare()
    {
        //
        setStatus(0);
        //
        presenterIsTalking = false;
        //
        theMoveSpeed = bounds().width();
        initialised = true;
        processParams();
        String eventsFileName = String.format("%s.xml",sectionName());
        String eventsFilePath = getConfigPath(eventsFileName);
        eventsDict = loadXML(eventsFilePath);
        //
        String audioFileName = String.format("%saudio.xml",sectionName());
        String audioFilePath = getConfigPath(audioFileName);
        loadAudioXML(audioFilePath);
        //
        loadFingers();
        loadEvent("master");
        //masterObjects = objectDict.copy();
        doVisual(currentEvent());
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                if (!performSel("demo",currentEvent()))
                {
                    doBody(currentEvent());
                }
                action_startScene();
            }});
    }

    public void  action_startScene() throws Exception
    {
        String videoFile = parameters.get("video");
        String videoFilePath = OBVideoPlayer.getVideoPath(videoFile);
        videoPlayer = new OBVideoPlayer(video.frame(),this,false,true);
        videoPlayer.setZPosition(190);
        videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FILL);
        attachControl(videoPlayer);
        videoPlayer.prepareForPlaying(OBUtils.getAssetFileDescriptorForPath(videoFilePath),0,null);
        videoPlayer.playAfterPrepare = true;
        //
        String presenterIntro = parameters.get("presenterIntro");
        boolean usesPresenter = presenterIntro != null;
        //
        Map<String, List> eventd = (Map<String, List>) audioScenes.get(presenterIntro);
        if(eventd == null)
        {
            usesPresenter = false;
        }
        //
        if(usesPresenter)
        {

            List introAudio = eventd.get("DEMO");
            List endAudio = eventd.get("DEMO2");
            //
            if(introAudio == null || introAudio.size()  == 0)
            {
                usesPresenter = false;
            }
            if(endAudio == null || endAudio.size()  == 0)
            {
                usesPresenter = false;
            }
        }
        if(usesPresenter)
        {
            presenterIsTalking = true;
            //
            List introAudio = eventd.get("DEMO");
            final List endAudio = eventd.get("DEMO2");
            //
            waitForSecs(0.3f);
            //
            presenter_show();
            waitForSecs(0.3f);
            //
            presenter.speak(introAudio,this);
            waitForSecs(0.3f);
            //
            presenter_hide();
            waitForSecs(0.3f);
            //
            presenterIsTalking = false;
            final OBSectionController fthis = this;
            //
            videoPlayer.start(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    presenterIsTalking = true;
                    //
                    presenter_show();
                    waitForSecs(0.3f);
                    //
                    presenter.speak(endAudio,fthis);
                    //
                    waitForSecs(0.3f);
                    //
                    presenter_hide();
                    waitForSecs(0.3f);
                    //
                    presenterIsTalking = false;
                    //
                    nextScene();
                }
            });
        }
        else
        {
            videoPlayer.start(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    nextScene();
                }
            });
        }
    }

    public void cleanUp()
    {
        super.cleanUp();
        //
        videoPlayer.stop();
        //videoPlayer.tearDown();
    }

    public void setSceneXX(String scene)
    {
        //
        action_prepareScene(false);
        //
    }


    public void  action_prepareScene(boolean redrawScene)
    {
        presenter_group =(OBGroup ) objectDict.get("presenter");
        //
        presenter_group.hide();
        presenter_group.hideMembers("faceside");
        //
        presenter = OBPresenter.characterWithGroup((OBGroup) presenter_group.objectDict.get("faceforward"));
        presenter_group.objectDict.get("faceforward").show();
        //
        presenter.showOnly("mouth_0");
        //
        video = objectDict.get("video");
    }

    public void  presenter_show()
    {
        presenter_group.show();
        playSFX("snap");
    }

    public void  presenter_hide()
    {
        presenter_group.hide();
        playSFX("suction_plop");
    }

    public void  onPause()
    {
        if(videoPlayer != null)
        {
            videoPlayer.pause();
        }
    }


    public void  onResume()
    {
        if(videoPlayer != null && !presenterIsTalking)
        {
            videoPlayer.start();
        }
    }


}
