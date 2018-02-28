package org.onebillion.onecourse.mainui.oc_video;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBTextLayer;
import org.onebillion.onecourse.controls.OBVideoPlayer;
import org.onebillion.onecourse.glstuff.OBRenderer;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBBrightnessManager;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBSystemsManager;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.onebillion.onecourse.utils.OBAnim.ANIM_EASE_IN_EASE_OUT;

/**
 * Created by pedroloureiro on 07/02/2017.
 */

public class OC_VideoPlayback extends OC_SectionController
{
    public int subtitleIndex;
    public List<OBXMLNode> subtitleList;
    public float subtitleTextSize;
    public OC_VideoPlaybackRunnable syncSubtitlesRunnable;
    //
    public Map<String, OBXMLNode> videoXMLDict;
    public List<String> videoPlaylist;
    public int currentVideoIndex;
    //
    public List<OBControl> videoPreviewImages;
    public OBGroup videoPreviewGroup;
    //
    private static Typeface plain, bold, italic, boldItalic;
    private float maximumX, minimumX;
    private PointF lastPoint = new PointF();
    private PointF lastLastPoint = new PointF();
    private PointF firstPoint = new PointF();
    private long lastMoveEvent, lastlastMoveEvent;
    private int videoPreviewIdx = 0;
    private int videoScrollState;
    private int intro_video_state = 0;
    private final static int ivs_act_normal = 0,
            ivs_before_play = 1,
            ivs_playing_full_screen = 2;
    private final static int VIDEO_SCROLL_NONE = 0,
            VIDEO_SCROLL_TOUCH_DOWNED = 1,
            VIDEO_SCROLL_MOVED = 2;
    private PointF videoTouchDownPoint = new PointF();
    public OBVideoPlayer videoPlayer;
    private String movieFolder;
    private boolean slowingDown;
    private boolean inited = false;
    //
    public static int fadeTime = 1000;
    public static int defaultWaitTime = 50;
    public float currentVideoPlayerVolume;

    private Map<Long,String> watchedVideos;

    private static Typeface plainFont ()
    {
        if (plain == null)
            plain = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Regular.otf");
        return plain;
    }


    private static Typeface boldFont ()
    {
        if (bold == null)
            bold = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Bold.otf");
        return bold;
    }


    private static Typeface italicFont ()
    {
        if (italic == null)
            italic = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-Italic.otf");
        return italic;
    }


    private static Typeface boldItalicFont ()
    {
        if (boldItalic == null)
            boldItalic = Typeface.createFromAsset(MainActivity.mainActivity.getAssets(), "F37Ginger-BoldItalic.otf");
        return boldItalic;
    }


    public int buttonFlags ()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON;
    }


    public void prepare ()
    {
        watchedVideos = new ArrayMap<>();
        super.prepare();
        loadEvent("mastera");
        subtitleTextSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("subtitletextsize")));
        String playlist = parameters.get("playlist");
        loadPlaylist(playlist);
        loadVideoThumbnails();
        //
        hideControls("video_preview.*");
        hideControls("video_textbox");
        //
        scrollPreviewToVisible(videoPreviewIdx, false);
        //
        OBBrightnessManager.sharedManager.onSuspend();
    }


    @Override
    public void exitEvent()
    {
        if(shouldCollectMiscData())
            collectMiscData("watched", watchedVideos);
        //
        OBBrightnessManager.sharedManager.onContinue();
        //
        super.exitEvent();
    }

    public void cleanUp ()
    {
        MainActivity.log("OC_VideoPlayback:cleanUp");
        //
        if (videoPlayer != null)
        {
            videoPlayer.stop();
        }
        //
        super.cleanUp();
    }


    private void playNextVideo ()
    {
        currentVideoIndex++;
        if (currentVideoIndex >= videoPlaylist.size())
        {
            MainActivity.log("OC_VideoPlayback:playNextVideo:reached the end of the playlist");
            return;
        }
        //
        scrollPreviewToVisible(currentVideoIndex, true);
        selectPreview(currentVideoIndex);
        setUpVideoPlayerForIndex(currentVideoIndex, true);
    }


    private void clearSubtitle ()
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
        {
            @Override
            public void run () throws Exception
            {
                OBLabel oldLabel = (OBLabel) objectDict.get("subtitle");
                objectDict.remove("subtitle");
                //
                if (oldLabel != null)
                {
                    lockScreen();
                    detachControl(oldLabel);
                    unlockScreen();
                }
            }
        });
    }


    public boolean nextSubtitleHasPause ()
    {
        if (subtitleIndex >= subtitleList.size())
        {
            MainActivity.log("OC_VideoPlayback:nextSubtitleHasPause:reached the end of the subtitle for this video");
            return false;
        }
        //
        OBXMLNode subtitleNode = subtitleList.get(subtitleIndex);
        String text = subtitleNode.attributeStringValue("text");
        return text.startsWith("#");
    }


    public void showSubtitle (int videoIndex)
    {
        if (currentVideoIndex != videoIndex)
        {
            MainActivity.log("OC_VideoPLayback:showSubtitle:mismatch of video index and current video index in scene. aborting");
            return;
        }
        //
        OBControl textbox = objectDict.get("video_textbox");
        //
        if (textbox != null)
        {
            clearSubtitle();
            //
            if (subtitleIndex >= subtitleList.size())
            {
                MainActivity.log("OC_VideoPlayback:showSubtitle:reached the end of the subtitle for this video");
                return;
            }
            //
            OBXMLNode subtitleNode = subtitleList.get(subtitleIndex);
            //
            String text = subtitleNode.attributeStringValue("text");
            int waitPeriod = 0;
            if (text.startsWith("#"))
            {
                String components[] = text.split("#");
                waitPeriod = Integer.parseInt(components[1]);
                text = components[2];
            }
            float fontSize = subtitleTextSize;
            if (text.length() > 50) fontSize *= 0.9;
            if (text.length() > 60) fontSize *= 0.9;
            //
            final OBLabel label = new OBLabel(text, plainFont(), fontSize);
            label.setMaxWidth(textbox.width());
            label.setJustification(OBTextLayer.JUST_LEFT);
            label.setLineSpaceMultiplier(1.2f);
            label.sizeToBoundingBox();
            label.setPosition(new PointF(textbox.position().x, textbox.position().y));
            label.setZPosition(videoPreviewGroup.zPosition());
            label.setColour(Color.BLACK);
            //
            objectDict.put("subtitle", label);
            //
            //MainActivity.log("OC_VideoPlayback:showSubtitle:attaching new subtitle");
            //
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    lockScreen();
                    attachControl(label);
                    unlockScreen();
                }
            });
            //
            if (waitPeriod > 0)
            {
                setStatus(STATUS_WAITING_FOR_RESUME);
                //
                if (videoPlayer.isPlaying())
                {
                    videoPlayer.pause();
                }
                try
                {
                    Thread.sleep(waitPeriod * 1000);
                    //
                    if (currentVideoIndex != videoIndex)
                    {
                        MainActivity.log("OC_VideoPLayback:showSubtitle:mismatch of video index and current video index in scene. aborting");
                        return;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    // nothing to do here
                }
                setStatus(STATUS_IDLE);
                //
                currentVideoPlayerVolume = 1;
                videoPlayer.player.setVolume(currentVideoPlayerVolume, currentVideoPlayerVolume);
                //
                videoPlayer.start();
            }
            else if (!videoPlayer.isPlaying())
            {
                videoPlayer.start();
            }
            //
            int end = subtitleNode.attributeIntValue("end");
            int currentPosition = videoPlayer == null ? -1 : videoPlayer.currentPosition();
            //
            while (currentPosition < end && !_aborting)
            {
                int excess = end - currentPosition;
                if (currentPosition == -1) excess = 10;
                //
                MainActivity.log("OC_VideoPlayback:showSubtitle:waiting for subtitle:" + excess);
                //
                try
                {
                    int totalSleepTime = 0;
                    while (totalSleepTime < excess && !_aborting)
                    {
                        Thread.sleep(10);
                        //
                        if (currentVideoIndex != videoIndex)
                        {
                            MainActivity.log("OC_VideoPLayback:showSubtitle:mismatch of video index and current video index in scene. aborting");
                            return;
                        }
                        //
                        totalSleepTime += 10;
                    }
                }
                catch (Exception e)
                {
                    MainActivity.log("OC_VideoPlayback:showSubtitle:exception caught");
                    e.printStackTrace();
                }
                //
                currentPosition = videoPlayer == null ? -1 : videoPlayer.currentPosition();
            }
            //
            clearSubtitle();
            subtitleIndex++;
            //
            OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(syncSubtitlesRunnable);
            OBSystemsManager.sharedManager.getMainHandler().post(syncSubtitlesRunnable);
        }
    }


    private void loadSubtitles (int idx)
    {
        subtitleIndex = 0;
        subtitleList = new ArrayList();
        //
        String videoID = videoPlaylist.get(idx);
        String subtitles = videoXMLDict.get(videoID).attributeStringValue("subtitles");
        if (subtitles == null || subtitles.equalsIgnoreCase(""))
        {
            MainActivity.log("OC_VideoPlayback:loadSubtitles: subtitles not set for video " + videoID);
            return;
        }
        //
        String srtPath = getLocalPath(subtitles);
        //
        if (srtPath == null)
        {
            MainActivity.log("OC_VideoPlayback:loadSubtitles: couldn't find subtitles for video " + videoID);
            return;
        }
        //
        try
        {
            Scanner scanner = new Scanner(OBUtils.getInputStreamForPath(srtPath));
            String line;
            //
            while (scanner.hasNextLine() && !_aborting)
            {
                line = scanner.nextLine();
                //
                if (line.isEmpty()) continue;
                //
                // index
                String index = line;
                //
                // time stamp
                line = scanner.nextLine();
                String[] timestamps = line.split(" --> ");
                if (timestamps.length != 2)
                {
                    MainActivity.log("OC_VideoPlayback:loadSubtitles --> unable to find 2 timestamps in line: " + line);
                    MainActivity.log("OC_VideoPlayback:loadSubtitles.Unable to continue loading subtitles");
                    return;
                }
                //
                if (!timestamps[0].contains(","))
                {
                    int breakIndex = timestamps[0].lastIndexOf(".");
                    char[] charArray = timestamps[0].toCharArray();
                    charArray[breakIndex] = ',';
                    timestamps[0] = new String(charArray);
                }
                if (timestamps[0].startsWith("0:"))
                {
                    timestamps[0] = timestamps[0].substring(2);
                }
                //
                String[] topParts = timestamps[0].split(",");
                if (topParts.length != 2)
                {
                    MainActivity.log("OC_VideoPlayback:loadSubtitles --> unable to find 2 components separated by , in timestamp: " + timestamps[0]);
                    MainActivity.log("OC_VideoPlayback:loadSubtitles.Unable to continue loading subtitles");
                    return;
                }
                String[] parts = topParts[0].split(":");
                if (parts.length != 3)
                {
                    MainActivity.log("OC_VideoPlayback:loadSubtitles --> unable to find 3 components separated by : in timestamp: " + timestamps[0]);
                    MainActivity.log("OC_VideoPlayback:loadSubtitles.Unable to continue loading subtitles");
                    return;
                }
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                int milliseconds = Integer.parseInt(topParts[1]);
                //
                long subStart = milliseconds + seconds * 1000 + minutes * 60 * 1000 + hours * 60 * 60 * 1000;
                //
                if (!timestamps[1].contains(","))
                {
                    int breakIndex = timestamps[1].lastIndexOf(".");
                    char[] charArray = timestamps[1].toCharArray();
                    charArray[breakIndex] = ',';
                    timestamps[1] = new String(charArray);
                }
                if (timestamps[1].startsWith("0:"))
                {
                    timestamps[1] = timestamps[1].substring(2);
                }
                //
                topParts = timestamps[1].split(",");
                if (topParts.length != 2)
                {
                    MainActivity.log("OC_VideoPlayback:loadSubtitles --> unable to find 2 components separated by , in timestamp: " + timestamps[1]);
                    MainActivity.log("OC_VideoPlayback:loadSubtitles.Unable to continue loading subtitles");
                    return;
                }
                parts = topParts[0].split(":");
                if (parts.length != 3)
                {
                    MainActivity.log("OC_VideoPlayback:loadSubtitles --> unable to find 3 components separated by : in timestamp: " + timestamps[1]);
                    MainActivity.log("OC_VideoPlayback:loadSubtitles.Unable to continue loading subtitles");
                    return;
                }
                hours = Integer.parseInt(parts[0]);
                minutes = Integer.parseInt(parts[1]);
                seconds = Integer.parseInt(parts[2]);
                milliseconds = Integer.parseInt(topParts[1]);
                //
                long subEnd = milliseconds + seconds * 1000 + minutes * 60 * 1000 + hours * 60 * 60 * 1000;
                //
                // text
                String text = "";
                line = scanner.nextLine();
                while (!line.equals(""))
                {
                    text = text + ((text.length() > 0) ? System.lineSeparator() : "") + line;
                    //
                    if (!scanner.hasNextLine()) break;
                    //
                    line = scanner.nextLine();
                }
                //
                OBXMLNode subtitleNode = new OBXMLNode();
                subtitleNode.attributes.put("index", index);
                subtitleNode.attributes.put("text", text);
                subtitleNode.attributes.put("start", String.valueOf(subStart));
                subtitleNode.attributes.put("end", String.valueOf(subEnd));
                //
                subtitleList.add(subtitleNode);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_VideoPlayback:loadSubtitles:exception caught while reading srt " + srtPath);
            e.printStackTrace();
        }
    }


    private void loadPlaylist (String playlistName)
    {
        String playlistxml = String.format("%s.xml", playlistName);
        videoPlaylist = new ArrayList<>();
        videoXMLDict = new HashMap<>();
        String xmlPath = getConfigPath(playlistxml);
        try
        {
            OBXMLManager xmlManager = new OBXMLManager();
            List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
            OBXMLNode xmlNode = xl.get(0);
            List<OBXMLNode> videoXMLs = xmlNode.childrenOfType("video");
            for (OBXMLNode n : videoXMLs)
            {
                String key = n.attributeStringValue("id");
                videoXMLDict.put(key, n);
                videoPlaylist.add(key);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("OC_VideoPlayback:loadPlaylist:exception caught while loading " + playlistxml);
            e.printStackTrace();
        }
    }


    private void loadVideoThumbnails ()
    {
        deleteControls("video_selector");
        //
        OBControl p1 = objectDict.get("video_preview1");
        OBControl p2 = objectDict.get("video_preview2");
        float videoPreviewX = p1.position().x;
        float videoPreviewTopY = p1.position().y;
        float videoPreviewXOffset = p2.position().x - p1.position().x;
        float videoPreviewHeight = p1.height();
        int idx = 0;
        float zpos = OC_Generic.getNextZPosition(this);
        //
        videoPreviewImages = new ArrayList<>();
        List<OBControl> lstgp = new ArrayList<>();
        for (String videoID : videoPlaylist)
        {
            OBXMLNode videoNode = videoXMLDict.get(videoID);
            String frame = videoNode.attributeStringValue("frame");
            //
            OBImage im = loadImageWithName(frame, new PointF(), new RectF(), false);
            if (im == null)
            {
                MainActivity.log("NULL IMAGE " + frame);
            }
            if (movieFolder == null)
            {
                String f = OBImageManager.sharedImageManager().getImgPath(frame);
                f = OBUtils.stringByDeletingLastPathComponent(f);
                f = OBUtils.stringByDeletingLastPathComponent(f);
                movieFolder = OBUtils.stringByAppendingPathComponent(f, "movies");
            }
            videoPreviewImages.add(im);
            im.setPosition(videoPreviewX + idx * videoPreviewXOffset, videoPreviewTopY);
            float originalScale = videoPreviewHeight / im.height();
            im.setProperty("originalScale", originalScale);
            originalScale *= 0.75;
            im.setScale(originalScale);
            im.setZPosition(5);
            //
            idx++;
        }
        lstgp.addAll(videoPreviewImages);
        //
        OBControl mask = objectDict.get("playlist_mask");
        OBControl maskCopy = mask.copy();
        lstgp.add(maskCopy);
        float gradientNudge = videoPreviewImages.get(0).width() * 0.25f;
        RectF frame = OBGroup.frameUnion(lstgp);
        frame.left = mask.left();
        frame.right = frame.left + frame.width() + gradientNudge;
        //
        videoPreviewGroup = new OBGroup(lstgp, frame);
        videoPreviewGroup.removeMember(maskCopy);
        attachControl(videoPreviewGroup);
        objectDict.put("videoPreviewGroup", videoPreviewGroup);
        maximumX = videoPreviewGroup.position().x + gradientNudge;
        minimumX = maximumX - (videoPreviewGroup.right() - mask.right()) - gradientNudge;
        videoPreviewGroup.setPosition(new PointF(maximumX, videoPreviewGroup.position().y));
        videoPreviewGroup.setShouldTexturise(false);
        selectPreview(videoPreviewIdx);
        mask.setHidden(true);
        //
        OC_Generic.sendObjectToTop(videoPreviewGroup, this);
        OC_Generic.sendObjectToTop(objectDict.get("top_bar_left"), this);
        OC_Generic.sendObjectToTop(objectDict.get("top_bar_right"), this);
        OC_Generic.sendObjectToTop(objectDict.get("gradient_left"), this);
        OC_Generic.sendObjectToTop(objectDict.get("gradient_right"), this);
    }


    public void start ()
    {
        super.start();
        setStatus(STATUS_IDLE);
        //
        if (videoPlayer != null)
        {
            videoPlayer.frameIsAvailable = false;
        }
        if (!inited)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run () throws Exception
                {
                    OBRenderer rn = MainActivity.mainActivity.renderer;
                    while (rn.colourProgram == null)
                    {
                        try
                        {
                            waitForSecs(0.1);
                        }
                        catch (Exception e)
                        {
                            MainActivity.log("OC_VideoPlayback:videoinit:exception caught");
                            e.printStackTrace();
                        }
                    }
                    OBUtils.runOnMainThread(new OBUtils.RunLambda()
                    {
                        @Override
                        public void run () throws Exception
                        {
                            lockScreen();
                            //
                            if (videoPlayer != null)
                            {
                                videoPlayer.stop();
                                detachControl(videoPlayer);
                            }
                            //
                            setUpVideoPlayerForIndex(videoPreviewIdx, false);
                            scrollPreviewToVisible(videoPreviewIdx, false);
                            //
                            unlockScreen();
                        }
                    });
                }
            });
            inited = true;
        }
    }


    private void selectPreview (int i)
    {
        videoPreviewIdx = i;
        OBControl pim = videoPreviewImages.get(videoPreviewIdx);
        //
        lockScreen();
        for (OBControl preview : videoPreviewImages)
        {
            float originalScale = (float) preview.propertyValue("originalScale");
            float newScale = pim.equals(preview) ? originalScale : originalScale * 0.75f;
            preview.setScale(newScale);
        }
        videoPreviewGroup.setNeedsRetexture();
        unlockScreen();

    }

    private void setUpVideoPlayerForIndex (final int idx, boolean play)
    {
        if (idx < 0)
        {
            return;
        }
        // Cleanup, if there are videos playing, stop them
        if (videoPlayer != null)
        {
            videoPlayer.stop();
        }
        //
        // Cleanup, if there are runnables, kill them
        if (syncSubtitlesRunnable != null)
        {
            OBSystemsManager.sharedManager.getMainHandler().removeCallbacks(syncSubtitlesRunnable);
        }
        //
        // Cleanup, if there are subtitles, clear them
        clearSubtitle();
        //
        if (idx == 0 && !play)
            intro_video_state = ivs_before_play;
        else
            intro_video_state = ivs_act_normal;
        //
        currentVideoIndex = idx;
        //
        String videoID = videoPlaylist.get(idx);
        OBXMLNode videoNode = videoXMLDict.get(videoID);
        String movie = videoNode.attributeStringValue("file");
        //
        //OBAnalyticsManager.sharedManager.playZoneVideoWatched(videoID);
        if(shouldCollectMiscData())
            watchedVideos.put(System.currentTimeMillis()/1000,videoID);
        //
        String movieName = OBUtils.stringByAppendingPathComponent(movieFolder, movie);
        OBControl placeHolder = objectDict.get("video_video");
        //
        loadSubtitles(idx);
        subtitleIndex = 0;
        //
        if (subtitleList.size() > subtitleIndex)
        {
            OBXMLNode subtitleNode = subtitleList.get(subtitleIndex);
            int start = subtitleNode.attributeIntValue("start");
            //
            if (start == 0) play = false;
        }
        //
        if (videoPlayer == null)
        {
            lockScreen();
            //
            RectF r = new RectF();
            r.set(placeHolder.frame());
            r.left = (int) r.left;
            r.top = (int) r.top;
            r.right = (float) Math.ceil(r.right);
            r.bottom = (float) Math.ceil(r.bottom);
            placeHolder.setFrame(r);
            videoPlayer = new OBVideoPlayer(r, this, false, false);
            videoPlayer.stopOnCompletion = true;
            videoPlayer.setZPosition(190);
            videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FILL);
            //
            attachControl(videoPlayer);
            //
            unlockScreen();
        }
        //
        final OC_VideoPlayback finalSelf = this;
        //
        videoPlayer.playAfterPrepare = play;
        videoPlayer.startPlayingAtTime(OBUtils.getAssetFileDescriptorForPath(movieName), 0, new MediaPlayer.OnPreparedListener()
        {
            @Override
            public void onPrepared (MediaPlayer mp)
            {
                MainActivity.log("OC_VideoPlayback:setUpVideoPlayerForIndex:onPrepared");
                //
                videoPlayer.onPrepared(mp);
                //
                currentVideoPlayerVolume = 1;
                videoPlayer.player.setVolume(currentVideoPlayerVolume, currentVideoPlayerVolume);
                //
                setStatus(STATUS_IDLE);
                //
                videoPlayer.start();
                //
                syncSubtitlesRunnable = new OC_VideoPlaybackRunnable(idx, finalSelf);
                OBSystemsManager.sharedManager.getMainHandler().post(syncSubtitlesRunnable);
            }
        }, new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion (MediaPlayer mp)
            {
                // Only when it finishes playing the video
                currentVideoIndex = -1;
                //
                videoPlayer.onCompletion(mp);
            }
        });
    }


    private void scrollPreviewToVisible (int idx, boolean animate)
    {
        OBControl preview = videoPreviewImages.get(idx);
        float gradientNudge = preview.width() * 0.25f;
        float leftEdge = ((OBControl) objectDict.get("top_bar_left")).right() + gradientNudge;
        float rightEdge = ((OBControl) objectDict.get("top_bar_right")).left() - gradientNudge;
        float diff = 0;
        float leftThreshold = leftEdge + preview.width() * 0.75f;
        float rightThreshold = rightEdge - preview.width() * 0.75f;
        RectF previewFrame = convertRectFromControl(preview.frame, videoPreviewGroup);
        float previewLeft = previewFrame.left;
        float previewRight = previewFrame.right;
        //
        if (previewLeft < leftThreshold)
        {
            diff = leftThreshold - previewLeft;
        }
        else if (previewRight > rightThreshold)
        {
            diff = rightThreshold - previewRight;
        }
        if (diff == 0)
        {
            return;

        }
        float newX = videoPreviewGroup.position().x + diff;
        if (newX > maximumX) newX = maximumX;
        if (newX < minimumX) newX = minimumX;
        if (newX != videoPreviewGroup.position().x)
        {
            PointF pt = OC_Generic.copyPoint(new PointF(newX, videoPreviewGroup.position().y));
            if (animate)
            {
                OBAnim anim = OBAnim.moveAnim(pt, videoPreviewGroup);
                OBAnimationGroup animGroup = new OBAnimationGroup();
                registerAnimationGroup(animGroup, "videoscrollanim");
                animGroup.applyAnimations(Arrays.asList(anim), 0.4f, true, ANIM_EASE_IN_EASE_OUT, null);
            }
            else
            {
                videoPreviewGroup.setPosition(pt);
            }
        }
    }


    public void touchUpAtPoint (PointF pto, View v)
    {
        if (status() != STATUS_IDLE)
        {
            return;
        }
        //
        setStatus(STATUS_BUSY);
        //
        if (videoScrollState > 0)
        {
            boolean mustSelect = videoScrollState != VIDEO_SCROLL_MOVED;
            videoScrollState = VIDEO_SCROLL_NONE;
            if (mustSelect)
            {
                for (int i = 0; i < videoPreviewImages.size(); i++)
                {
                    OBControl im = videoPreviewImages.get(i);
                    RectF f = convertRectFromControl(im.bounds(), im);
                    if (f.contains(pto.x, pto.y))
                    {
                        selectPreview(i);
                        scrollPreviewToVisible(i, true);
                        //
                        setUpVideoPlayerForIndex(i, true);
                        return;
                    }
                }
            }
            else
            {
                float dist = lastPoint.x - lastLastPoint.x;
                float time = (lastMoveEvent - lastlastMoveEvent) / 1000.0f;
                final float speed = dist / time;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run () throws Exception
                    {
                        slowDown(speed, videoPreviewGroup);
                    }
                });
            }
        }
        setStatus(STATUS_IDLE);
    }

    private void slowDown (float xSpeed, OBControl group)
    {
        slowingDown = true;
        try
        {
            while (slowingDown)
            {
                if (Math.abs(xSpeed) < 1)
                {
                    slowingDown = false;
                    return;
                }
                xSpeed *= 0.925;
                float dist = xSpeed * 0.02f;
                float x = group.position().x;
                x += dist;
                boolean fin = false;
                if (x > maximumX)
                {
                    x = maximumX;
                    fin = true;
                }
                else if (x < minimumX)
                {
                    x = minimumX;
                    fin = true;
                }
                group.setPosition(x, group.position().y);
                if (fin)
                    slowingDown = false;
                waitForSecs(0.02);
            }
        }
        catch (Exception e)
        {
            slowingDown = false;
        }
    }

    public void touchMovedToPoint (PointF pt, View v)
    {
        if (status() == 0)
        {
            MainActivity.log("OC_VideoPlayback:touchMovedToPoint:status 0");
            return;
        }
        //
        if (videoScrollState == VIDEO_SCROLL_TOUCH_DOWNED && OB_Maths.PointDistance(videoTouchDownPoint, pt) > applyGraphicScale(8))
        {
            videoScrollState = VIDEO_SCROLL_MOVED;
        }
        if (videoScrollState > 0)
        {
            float dx = pt.x - lastPoint.x;
            float newX = videoPreviewGroup.position().x + dx;
            if (newX <= maximumX && newX >= minimumX)
            {
                videoPreviewGroup.setPosition(newX, videoPreviewGroup.position().y);
            }
            lastLastPoint.x = lastPoint.x;
            lastlastMoveEvent = lastMoveEvent;
            lastPoint.x = pt.x;
            lastMoveEvent = System.currentTimeMillis();
        }
    }

    private void handleVideoPress (PointF pt)
    {
        if (intro_video_state == ivs_before_play)
        {
            lockScreen();
            OBControl placeHolder = objectDict.get("video_video");
            videoPlayer.setFillType(OBVideoPlayer.VP_FILL_TYPE_ASPECT_FILL);
            videoPlayer.setFrame(placeHolder.frame());
            unlockScreen();
            //
            currentVideoPlayerVolume = 1;
            videoPlayer.player.setVolume(currentVideoPlayerVolume, currentVideoPlayerVolume);
            //
            videoPlayer.start();
            //
            intro_video_state = ivs_playing_full_screen;
            return;
        }
        if (videoPlayer.isPlaying())
        {
            videoPlayer.pause();
        }
        else
        {
            currentVideoPlayerVolume = 1;
            if (videoPlayer != null && videoPlayer.player != null)
            {
                videoPlayer.player.setVolume(currentVideoPlayerVolume, currentVideoPlayerVolume);
                //
                videoPlayer.start();
            }
            else
            {
                setUpVideoPlayerForIndex(currentVideoIndex, true);
            }
        }
    }


    public void touchDownAtPoint (PointF pt, View v)
    {
        lastPoint.set(pt);
        lastLastPoint.set(pt);
        firstPoint.set(pt);
        slowingDown = false;
        //
        if (status() != STATUS_IDLE)
        {
            return;
        }
        //
        lastPoint.set(pt);
        //
        videoScrollState = VIDEO_SCROLL_NONE;
        RectF f = videoPreviewGroup.frame();
        if (f.contains(pt.x, pt.y))
        {
            videoScrollState = VIDEO_SCROLL_TOUCH_DOWNED;
            videoTouchDownPoint.set(pt);
        }
        else
        {
            if (videoPlayer != null && videoPlayer.frame().contains(pt.x, pt.y))
            {
                handleVideoPress(pt);
            }
        }
    }


    public void onResume ()
    {
        videoPlayer.onResume();
        if (videoPreviewIdx >= 0)
        {
            setUpVideoPlayerForIndex(videoPreviewIdx, false);
        }
        super.onResume();
    }


    @Override
    public void onPause ()
    {
        super.onPause();
        try
        {
            videoPlayer.onPause();
        }
        catch (Exception e)
        {
            MainActivity.log("OC_VideoPlayback:onPause:exception caught");
            e.printStackTrace();
        }

    }
}

