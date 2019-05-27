package com.maq.xprize.onecourse.utils;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.res.AssetFileDescriptor;

import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.OBSectionController;


/**
 * OBAudioManager
 * Controls the functionality of playing audio files and waiting for those files to finish.
 * Can also preload audio into channels and retrive the duration of the audio file
 */
public class OBAudioManager
{
    public static final String AM_MAIN_CHANNEL = "0",
            AM_BACKGROUND_CHANNEL = "1",
            AM_SFX_CHANNEL = "2";
    final static int MAX_AUDIOPATH_CACHE_COUNT = 40;
    public static OBAudioManager audioManager;
    public Map<String, OBGeneralAudioPlayer> players;
    Map<String, AssetFileDescriptor> pathCacheDict = new HashMap<>();
    List<String> pathCacheList = new ArrayList<>();

    public OBAudioManager ()
    {
        players = new HashMap<String, OBGeneralAudioPlayer>();
        audioManager = this;
    }

    public static Map<String, Object> loadAudioXML (InputStream xmlStream) throws Exception
    {
        Map<String, Object> audioDict = new HashMap<String, Object>();
        ;
        if (xmlStream != null)
        {
            OBXMLManager xmlManager = new OBXMLManager();
            Object xmlobj = xmlManager.parseFile(xmlStream);
            if (xmlobj != null)
            {
                Map<String, Float> sfxvols = new HashMap<>();
                List<String> evts = new ArrayList<>();
                audioDict.put("__sfxvols", sfxvols);

                List<OBXMLNode> children;
                if (xmlobj instanceof OBXMLNode)
                    children = ((OBXMLNode) xmlobj).childrenOfType("event");
                else
                {
                    children = ((List<OBXMLNode>) xmlobj).get(0).childrenOfType(("event"));
                }
                for (OBXMLNode xmlevent : children)
                {
                    String ekey = xmlevent.attributeStringValue("id");
                    evts.add(ekey);
                    Map<String, List<Object>> phrasegroups = new HashMap<String, List<Object>>();
                    List<String> groupList = new ArrayList<>();
                    for (OBXMLNode xmlphrasegroup : xmlevent.childrenOfType("phrasegroup"))
                    {
                        String pgkey = xmlphrasegroup.attributeStringValue("id");
                        groupList.add(pgkey);
                        List<Object> phrases = new ArrayList<Object>();
                        for (OBXMLNode xmlphrase : xmlphrasegroup.childrenOfType("phrase"))
                        {
                            String phrase = xmlphrase.contents.trim();
                            try
                            {
                                int n = Integer.parseInt(phrase);
                                phrases.add(n);
                            }
                            catch (Exception e)
                            {
                                phrases.add(phrase);
                            }
                        }
                        phrasegroups.put(pgkey, phrases);
                        String volk = xmlphrasegroup.attributeStringValue("vol");
                        if (volk != null && phrases.size() > 0)
                            sfxvols.put((String)phrases.get(phrases.size()-1), Float.parseFloat(volk));
                    }
                    phrasegroups.put("__keys", (List<Object>) (Object) groupList);
                    audioDict.put(ekey, phrasegroups);
                }
                audioDict.put("__events",evts);
            }
        }
        return audioDict;
    }

    public void stopPlayingOnChannel (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        player.stopPlaying(true);
    }

    public void stopPlaying ()
    {
        stopPlayingOnChannel(AM_MAIN_CHANNEL);
    }

    public void stopPlayingSFX ()
    {
        stopPlayingOnChannel(AM_SFX_CHANNEL);
    }

    public void stopPlayingBackground ()
    {
        stopPlayingOnChannel(AM_BACKGROUND_CHANNEL);
    }

    public void stopAllAudio ()
    {
        for (String k : players.keySet())
            stopPlayingOnChannel(k);
    }

    public String getAudioPath(String fileName)
    {
        for (String suffix : OBConfigManager.sharedManager.getAudioExtensions())
        {
            for (String path : OBConfigManager.sharedManager.getAudioSearchPaths())
            {
                String fullPath = path + "/" + fileName + "." + suffix;
                AssetFileDescriptor fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                if (fd != null)
                {
                    return fullPath;
                }
            }
        }
        return null;
    }


    public AssetFileDescriptor getAudioPathFD (String fileName)
    {
        AssetFileDescriptor fd = pathCacheDict.get(fileName);
        if (fd == null)
        {
            for (String suffix : OBConfigManager.sharedManager.getAudioExtensions())
            {
                for (String path : OBConfigManager.sharedManager.getAudioSearchPaths())
                {
                    String fullPath = path + "/" + fileName + "." + suffix;
                    fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                    if (fd != null)
                    {
                        break;
                    }
                    else
                    {
                        fd = null;
                    }
                }
            }
            if (fd == null)
            {
                return null;
            }
        }
        pathCacheList.remove(fileName);
        pathCacheList.add(fileName);
        pathCacheDict.put(fileName, fd);
        if (pathCacheList.size() >= MAX_AUDIOPATH_CACHE_COUNT)
        {
            String firstobj = pathCacheList.get(0);
            pathCacheList.remove(0);
            pathCacheDict.remove(firstobj);
        }
        return fd;
    }

    public void playOnChannel(String ch)
    {
        playerForChannel(ch).play();
    }

    public void startPlaying (String fileName, String channel)
    {
        OBGeneralAudioPlayer player = playerForChannel(channel);
        if (fileName == null)
            player.stopPlaying();
        else
        {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            if (fd != null)
            {
                player.startPlaying(fd);
            }
            else
            {
                MainActivity.log("Error caught in OBAudioManager.startPlaying [" + fileName + "] returned a null file descriptor");
            }
        }
    }

    public void startPlaying (String fileName, String channel, double atTime)
    {
        startPlaying(fileName, channel, atTime, 1.0f);
    }

    public void startPlaying (String fileName, String channel, double atTime, float atVolume)
    {
        OBGeneralAudioPlayer player = playerForChannel(channel);
        if (fileName == null)
            player.stopPlaying();
        else
        {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            player.startPlayingAtTimeVolume(fd, (int) (atTime * 1000), atVolume);
        }
    }

    public void startPlayingFromTo(String fileName, String channel, double fromSecs, double toSecs)
    {
        OBAudioBufferPlayer player = (OBAudioBufferPlayer)playerForChannel(channel,OBAudioBufferPlayer.class);
        if (fileName == null)
            player.stopPlaying();
        else
        {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            player.startPlaying(fd, fromSecs,toSecs);
        }
    }

    public void startPlayingFromTo(String fileName,double fromSecs, double toSecs)
    {
        startPlayingFromTo(fileName,AM_MAIN_CHANNEL,fromSecs,toSecs);
    }

    public void startPlaying (String fileName, double atTime)
    {
        startPlaying(fileName, AM_MAIN_CHANNEL, atTime);
    }

    public void startPlaying (String fileName)
    {
        startPlaying(fileName, AM_MAIN_CHANNEL);
    }

    public void startPlayingSFX (String fileName, float vol)
    {
        startPlaying(fileName, AM_SFX_CHANNEL, 0, vol);
    }

    public void waitPrepared ()
    {
        waitPrepared(AM_MAIN_CHANNEL);
    }

    public void waitUntilPlaying ()
    {
        waitUntilPlaying(AM_MAIN_CHANNEL);
    }

    public void waitPrepared (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            player.waitPrepared();
    }

    public void waitUntilPlaying (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            player.waitUntilPlaying();
    }

    public void waitAudioChannel (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            player.waitAudio();
    }

    public void waitAudio ()
    {
        waitAudioChannel(AM_MAIN_CHANNEL);
    }

    public void waitSFX ()
    {
        waitAudioChannel(AM_SFX_CHANNEL);
    }

    public Boolean isPlayingChannel (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            return player.isPlaying();
        return false;
    }

    public Boolean isPlaying ()
    {
        return isPlayingChannel(AM_MAIN_CHANNEL);
    }

    Boolean isPreparingChannel (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            return player.isPreparing();
        return false;
    }

    public Boolean isPreparing ()
    {
        return isPreparingChannel(AM_MAIN_CHANNEL);
    }

    public double durationForChannel (String ch)
    {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            return player.duration();
        return 0.0;
    }

    public double duration ()
    {
        return durationForChannel(AM_MAIN_CHANNEL);
    }


    public double durationSFX ()
    {
        return durationForChannel(AM_SFX_CHANNEL);
    }

    public OBGeneralAudioPlayer playerForChannel (String channel)
    {
        OBGeneralAudioPlayer player = players.get(channel);
        if (player == null)
        {
            player = new OBAudioPlayer();
            synchronized (this)
            {
                players.put(channel, player);
            }
        }
        return player;
    }

    public OBGeneralAudioPlayer playerForChannel (String channel,Class cls)
    {
        OBGeneralAudioPlayer player = players.get(channel);
        if (player == null || (player.getClass() != cls))
        {
            Constructor<?> cons;
            try
            {
                cons = cls.getConstructor();
                player = (OBGeneralAudioPlayer) cons.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }

            synchronized (this)
            {
                players.put(channel, player);
            }
        }
        return player;
    }

    public void prepareForChannel (final String fileName, final String channel)
    {
        OBGeneralAudioPlayer player = playerForChannel(channel);
        AssetFileDescriptor fd = getAudioPathFD(fileName);
        player.prepare(fd);
    }

    public void prepare(final String fileName)
    {
        prepareForChannel(fileName,AM_MAIN_CHANNEL);
    }

    public void clearCaches ()
    {
        pathCacheDict.clear();
        pathCacheList.clear();
        synchronized (players)
        {
            Set<String> tempPlayers = new HashSet(players.keySet());
            for (String s : tempPlayers)
            {
                if (!s.equals(AM_MAIN_CHANNEL))
                {
                    players.remove(s);
                }
            }
        }
    }

}
