package org.onebillion.onecourse.mainui.oc_prepm;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by michal on 17/01/2018.
 */

public class OC_PrepMWithVideo extends OC_SectionController
{
    public OBVideoPlayer videoPlayer;
    public List<OBLabel> eventTargets;
    public OBGroup currentEquation;

    Map<Integer,Map<String,Integer>> videoTimmings;
    String videoName;
    Timer videoTimer;
    int nextNumAudio;

    public void prepareCountingVideo(String name)
    {
        lockScreen();
        videoName = name;
        List<String> audio = getAudioForScene("sfx", "count");
        OBAudioManager.audioManager.prepareForChannel(audio.get(0),"special");
        loadVideoTimmings();
        loadVideoPlayer();
        unlockScreen();
    }

    public void cleanUp()
    {
        super.cleanUp();
        if(videoPlayer != null)
        {
            videoPlayer.stop();
            videoPlayer.cleanUp(MainActivity.mainActivity.renderer);
        }

        if(videoTimer != null)
        {
            videoTimer.cancel();
            videoTimer.purge();
        }
    }

    public void loadVideoPlayer()
    {
        OBControl videoBox = objectDict.get("video_box");
        String videoFilePath = getLocalPath(String.format("%s.mp4",videoName));
        videoPlayer = new OBVideoPlayer(videoBox.frame(), this, false, false);
        AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(videoFilePath);
        videoPlayer.prepareForPlaying(afd,0,null);
        videoPlayer.playAfterPrepare = false;
        videoPlayer.stopOnCompletion = false;
        videoBox.hide();
        videoPlayer.hide();
        attachControl(videoPlayer);
        OBPath path = (OBPath)objectDict.get("video_frame");
        path.sizeToBoundingBoxIncludingStroke();
        path.setZPosition(9);
        videoPlayer.setZPosition(10);
    }

    public void loadVideoTimmings()
    {
        videoTimmings = new ArrayMap<>();
        OBXMLManager xmlman =  new OBXMLManager();
        try
        {
            List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(getLocalPath(String.format("%s.xml", videoName))));

            OBXMLNode rootNode = xl.get(0);
            for (OBXMLNode partNode : rootNode.childrenOfType("part"))
            {
                int number = OBUtils.getIntValue(partNode.contents);
                int startTime = partNode.attributeIntValue("start");
                int endTime = partNode.attributeIntValue("end");
                int midTime = partNode.attributeIntValue("mid");
                Map<String, Integer> dict = new ArrayMap<>();
                dict.put("start", startTime);
                dict.put("end", endTime);
                dict.put("mid", midTime);
                videoTimmings.put(number, dict);
            }
        }
        catch (Exception e)
        {
            MainActivity.log(e.getMessage());
        }
    }

    public void playTickVideoFomNum(final int num1, final int num2,final float rate,boolean refresh)
    {
        Map<String,Integer> timeDict1 = videoTimmings.get(num1);
        Map<String,Integer> timeDict2 = videoTimmings.get(num1<num2 ? num2 : num2+1);
        final int start = num1 < num2 ? timeDict1.get("start") : timeDict1.get("end");
        final int end =  num1 < num2 ? timeDict2.get("end") : timeDict2.get("start");
        videoPlayer.resetPlayerLock();

        if(refresh)
        {
            videoPlayer.setPlayRate(rate);
            videoPlayer.playAfterPrepare = false;
            videoPlayer.seekTo(start, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    videoPlayer.start();
                    setTickTimmingsForVideoFromNum(end, num1, num1 > num2);
                }
            });
        }
        else if(num1 > num2)
        {
            final int audioDuration = videoPlayer.mediaPlayer().getDuration();
            String videoFilePath = getLocalPath(String.format("%s-reversed.mp4",videoName));
            AssetFileDescriptor afd = OBUtils.getAssetFileDescriptorForPath(videoFilePath);

            videoPlayer.stop();
            videoPlayer.prepareForPlaying(afd,audioDuration-start,new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    videoPlayer.setPlayRate(rate);

                    videoPlayer.start();
                    setTickTimmingsForVideoFromNum(end, num1, num1 > num2);
                }
            });
        }
        else
        {
            videoPlayer.start();
            setTickTimmingsForVideoFromNum(end, num1, num1 > num2);
        }
        videoPlayer.waitForVideo();
        videoTimer.cancel();
        videoTimer.purge();
    }

    public void setTickTimmingsForVideoFromNum(final int endTime, int num, final boolean backward)
    {
        nextNumAudio = backward ? num : num+1;
        final int videoDuration = videoPlayer.mediaPlayer().getDuration();
        videoTimer = new Timer();
        videoTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                int sec = videoPlayer.currentPosition();
                if((!backward && sec >= videoTimmings.get(nextNumAudio).get("mid")) ||
                        (backward && nextNumAudio > 0 && sec >= (videoDuration-videoTimmings.get(nextNumAudio).get("mid"))))
                {
                    if(!backward)
                        nextNumAudio++;
                    else
                        nextNumAudio--;
                    try
                    {
                        playSfxAudio("count", false);
                    }
                    catch (Exception e)
                    {
                    }
                }

                if((!backward && sec >=endTime) ||
                        (backward && nextNumAudio > 0 && sec >= (videoDuration-endTime)))
                {
                    videoPlayer.pause();
                    videoPlayer.finishVideoWait();
                    this.cancel();
                }
            }
        },10,10);
    }


    public void loadEquationAndNumbers(int num1,int num2,int maxNum,boolean addition)
    {
        if(eventTargets != null)
        {
            for(OBControl con : eventTargets)
                detachControl(con);

        }
        eventTargets = new ArrayList<>();
        if(currentEquation!=null)
            detachControl(currentEquation);
        int result = addition ? num1 + num2 : num1 - num2;
        String sign = addition ? "+" : "–";
        OC_Numberlines_Additions.loadEquation(String.format("%d %s %d = %d",num1,sign,num2,result),"equation",objectDict.get("eq_box"), Color.BLACK,false,this);
        currentEquation =(OBGroup)objectDict.get("equation");
        OC_Numberlines_Additions.hideEquation(currentEquation,this);
        List<Integer> choices = new ArrayList<>();
        for(int i=1; i<=maxNum; i++)
        {
            if(i!=result)
                choices.add(i);
        }
        List<Integer> randChoices = OBUtils.randomlySortedArray(choices);
        List<Integer> randAnswers = OBUtils.randomlySortedArray(Arrays.asList(randChoices.get(0) ,randChoices.get(1) ,result));
        OBFont font = OC_Numberlines_Additions.getLabelForEquation(1,currentEquation).font();
        int index = 1;
        for(int answer : randAnswers)
        {
            OBLabel label = new OBLabel(String.format("%d",answer),font);
            label.setColour(Color.BLACK);
            label.setPosition(OB_Maths.locationForRect(0.1f+ index*(0.8f/(randAnswers.size()+1)),0.5f,objectDict.get("bottom_bar").frame()));
            attachControl(label);
            label.setProperty("correct",answer==result);
            label.setZPosition(3);
            eventTargets.add(label);
            label.hide();
            index++;
        }
    }

}
