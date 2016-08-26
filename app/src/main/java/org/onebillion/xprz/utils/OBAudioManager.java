package org.onebillion.xprz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.onebillion.xprz.mainui.MainActivity;

public class OBAudioManager {
    public static final String AM_MAIN_CHANNEL = "0",
            AM_BACKGROUND_CHANNEL = "1",
            AM_SFX_CHANNEL = "2";
    final static int MAX_AUDIOPATH_CACHE_COUNT = 40;
    public static OBAudioManager audioManager;
    public Map<String,OBAudioPlayer> players;
    Map<String,AssetFileDescriptor> pathCacheDict = new HashMap<>();
    List<String>pathCacheList = new ArrayList<>();
    public OBAudioManager()
    {
        players = new HashMap<String, OBAudioPlayer>();
        audioManager = this;
    }

    public static Map<String,Object> loadAudioXML(InputStream xmlStream) throws Exception
    {
        Map<String,Object> audioDict = new HashMap<String, Object>();;
        if (xmlStream != null)
        {
            OBXMLManager xmlManager = new OBXMLManager();
            Object xmlobj = xmlManager.parseFile(xmlStream);
            if (xmlobj != null)
            {
                Map<String,Float> sfxvols = new HashMap<>();
                audioDict.put("__sfxvols",sfxvols);

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
                        if (volk != null)
                            sfxvols.put(pgkey,Float.parseFloat(volk));
                    }
                    phrasegroups.put("__keys", (List<Object>)(Object) groupList);
                    audioDict.put(ekey, phrasegroups);
                }
            }
        }
        return audioDict;
    }

    public void stopPlayingOnChannel(String ch)
    {
        OBAudioPlayer player = players.get(ch);
        player.stopPlaying();
    }

    public void stopPlaying()
    {
        stopPlayingOnChannel(AM_MAIN_CHANNEL);
    }

    public void stopPlayingSFX()
    {
        stopPlayingOnChannel(AM_SFX_CHANNEL);
    }

    public void stopPlayingBackground()
    {
        stopPlayingOnChannel(AM_BACKGROUND_CHANNEL);
    }

    public void stopAllAudio()
    {
        for (String k : players.keySet())
        stopPlayingOnChannel(k);
    }

    public AssetFileDescriptor getAudioPathFD(String fileName)
    {
        AssetFileDescriptor fd = pathCacheDict.get(fileName);
        if (fd == null)
        {
            Map<String, Object> config = MainActivity.mainActivity.config;
            String suffix = (String) config.get(MainActivity.CONFIG_AUDIO_SUFFIX);
            List<String> searchPaths = (List<String>) config.get(MainActivity.CONFIG_AUDIO_SEARCH_PATH);
            for (String path : searchPaths)
            {
                String fullPath = path + "/" + fileName + "." + suffix;
                fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                if (fd != null)
                    break;
                else
                    fd = null;
            }
            if (fd == null)
                return null;
        }
        pathCacheList.remove(fileName);
        pathCacheList.add(fileName);
        pathCacheDict.put(fileName,fd);
        if(pathCacheList.size() >= MAX_AUDIOPATH_CACHE_COUNT)
        {
            String firstobj = pathCacheList.get(0);
            pathCacheList.remove(0);
            pathCacheDict.remove(firstobj);
        }
        return fd;

//        AssetManager am = MainActivity.mainActivity.getAssets();
//        for (String path : searchPaths)
//        {
//            String fullpath = path+"/"+fileName+"."+suffix;
//            try
//            {
//                AssetFileDescriptor fd = am.openFd(fullpath);
//                return fd;
//            }
//            catch (IOException e)
//            {
//            }
//        }
//        return null;
    }

    public void startPlaying(String fileName,String channel)
    {
        OBAudioPlayer player = playerForChannel(channel);
        if (fileName == null)
            player.stopPlaying();
        else
        {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            player.startPlaying(fd);
        }
    }

    public void startPlaying(String fileName,String channel,double atTime)
    {
        startPlaying(fileName,channel,atTime,1.0f);
    }

    public void startPlaying(String fileName,String channel,double atTime,float atVolume)
    {
        OBAudioPlayer player = playerForChannel(channel);
        if (fileName == null)
            player.stopPlaying();
        else
        {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            player.startPlayingAtTimeVolume(fd,(int)(atTime*1000),atVolume);
        }
    }
    public void startPlaying(String fileName,double atTime)
    {
        startPlaying(fileName,AM_MAIN_CHANNEL,atTime);
    }

    public void startPlaying(String fileName)
    {
        startPlaying(fileName,AM_MAIN_CHANNEL);
    }

    public void startPlayingSFX(String fileName,float vol)
    {
        startPlaying(fileName,AM_SFX_CHANNEL,0,vol);
    }

    public void waitAudioChannel(String ch)
    {
        OBAudioPlayer player = players.get(ch);
        if (player != null)
            player.waitAudio();
    }

    public void waitAudio()
    {
        waitAudioChannel(AM_MAIN_CHANNEL);
    }

    public void waitSFX()
    {
        waitAudioChannel(AM_SFX_CHANNEL);
    }

    Boolean isPlayingChannel(String ch)
    {
        OBAudioPlayer player = players.get(ch);
        if (player != null)
            return player.isPlaying();
        return false;
    }

    public Boolean isPlaying()
    {
        return isPlayingChannel(AM_MAIN_CHANNEL);
    }

    Boolean isPreparingChannel(String ch)
    {
        OBAudioPlayer player = players.get(ch);
        if (player != null)
            return player.isPreparing();
        return false;
    }

    public Boolean isPreparing()
    {
        return isPreparingChannel(AM_MAIN_CHANNEL);
    }

    public double durationForChannel(String ch)
    {
        OBAudioPlayer player = players.get(ch);
        if (player != null)
            return player.duration();
        return 0.0;
    }

    public double duration()
    {
        return durationForChannel(AM_MAIN_CHANNEL);
    }


    public double durationSFX()
    {
        return durationForChannel(AM_SFX_CHANNEL);
    }

    public OBAudioPlayer playerForChannel(String channel)
    {
        OBAudioPlayer player = players.get(channel);
        if (player == null)
        {
            player = new OBAudioPlayer();
            synchronized(this)
            {
                players.put(channel,player);
            }
        }
        return player;
    }

    public void prepareForChannel(final String fileName, final String channel)
    {
        OBAudioPlayer player = playerForChannel(channel);
        AssetFileDescriptor fd = getAudioPathFD(fileName);
        player.prepare(fd);
    }

    public void clearCaches()
    {
        pathCacheDict.clear();
        pathCacheList.clear();
        synchronized(players)
        {
            Set<String> tempPlayers = new HashSet(players.keySet());
            for(String s : tempPlayers)
            {
                if (!s.equals(AM_MAIN_CHANNEL))
                {
                    players.remove(s);
                }
            }
        }
    }

}
