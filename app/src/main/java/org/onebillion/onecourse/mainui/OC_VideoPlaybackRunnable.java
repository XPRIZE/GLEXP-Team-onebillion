package org.onebillion.onecourse.mainui;

import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLNode;

/**
 * Created by pedroloureiro on 14/02/2017.
 */

public class OC_VideoPlaybackRunnable implements Runnable
{
    private int videoIndex;
    private OC_VideoPlayback controller;
    //
    private int maxExcessBeforePause;

    public OC_VideoPlaybackRunnable(int videoIndex, OC_VideoPlayback controller)
    {
        this.videoIndex = videoIndex;
        this.controller = controller;
    }

    public void run()
    {
        if (controller.subtitleIndex >= controller.subtitleList.size())
        {
            MainActivity.log("OC_VideoPlaybackRunnable:run:reached the end of the subtitles.");
            return;
        }
        //
        OBXMLNode subtitleNode = controller.subtitleList.get(controller.subtitleIndex);
        int start = subtitleNode.attributeIntValue("start");
        int startFade = start - controller.fadeTime;
        if (startFade < 0) startFade = 0;
        //
        int currentPosition = controller.videoPlayer == null ? -1 : controller.videoPlayer.currentPosition();
        //
        int excessBeforeFade = (currentPosition == -1) ? controller.defaultWaitTime : startFade - currentPosition;
        //
        if (excessBeforeFade > 0)
        {
            OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(this);
            OBSystemsManager.sharedManager.getMainHandler().postDelayed(this, excessBeforeFade);
        }
        else
        {
            int currentExcessBeforePause = (currentPosition == -1) ? controller.defaultWaitTime : start - currentPosition;
            this.maxExcessBeforePause = Math.max(currentExcessBeforePause, excessBeforeFade);
            //
            if (currentExcessBeforePause > 0)
            {
                if (controller.nextSubtitleHasPause())
                {
                    float volumeReduction = (float) controller.defaultWaitTime / (float) maxExcessBeforePause;
                    //
//                    MainActivity.log("OC_VideoPlaybackRunnable:run:volume reduction: " + volumeReduction);
//                    MainActivity.log("OC_VideoPlaybackRunnable:run:excess before pause: " + currentExcessBeforePause);
                    //
                    controller.currentVideoPlayerVolume -= volumeReduction;
                    if (controller.currentVideoPlayerVolume < 0) controller.currentVideoPlayerVolume = 0;
                    controller.videoPlayer.player.setVolume(controller.currentVideoPlayerVolume, controller.currentVideoPlayerVolume);
                    //
//                    MainActivity.log("OC_VideoPlaybackRunnable:run:setting video player volume to " + controller.currentVideoPlayerVolume);
                    //
                    OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(this);
                    OBSystemsManager.sharedManager.getMainHandler().postDelayed(this, controller.defaultWaitTime); // this is on purpose to have a regular fade
                }
                else
                {
                    OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(this);
                    OBSystemsManager.sharedManager.getMainHandler().postDelayed(this, currentExcessBeforePause);
                }
            }
            else
            {
                //MainActivity.log("OC_VideoPlaybackRunnable:run:showing subtitle:" + controller.subtitleIndex + " for video: " + controller.currentVideoIndex);
                //
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        controller.showSubtitle(controller.currentVideoIndex);
                    }
                });
            }
        }
    }

}
