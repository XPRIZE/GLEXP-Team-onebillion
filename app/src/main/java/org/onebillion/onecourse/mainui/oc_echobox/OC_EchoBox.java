package org.onebillion.onecourse.mainui.oc_echobox;

import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioBufferPlayer;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBAudioPlayer;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBGeneralAudioRecorder;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBTimer;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.onebillion.onecourse.utils.OBGeneralAudioPlayer.OBAP_FINISHED;
import static org.onebillion.onecourse.utils.OBGeneralAudioPlayer.OBAP_PLAYING;
import static org.onebillion.onecourse.utils.OBGeneralAudioPlayer.OBAP_PREPARING;
import static org.onebillion.onecourse.utils.OBUtils.LoadWordComponentsXML;
import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;
import static org.onebillion.onecourse.utils.OBUtils.runOnMainThread;
import static org.onebillion.onecourse.utils.OBUtils.runOnOtherThread;
import static org.onebillion.onecourse.utils.OB_Maths.randomInt;
import static org.onebillion.onecourse.utils.OB_Maths.rndom;

public class OC_EchoBox extends OC_SectionController
{
    static int AUDIBLE_THRESHOLD = 1200,
        STATUS_RECORDING = 1024,
        STATUS_PLAYING_RECORDING = 1025;

    OBGeneralAudioRecorder recorder;
    OBTimer timer;
    OBAudioBufferPlayer effectPlayer;
    long timeLastSound,timeRecordingStart,timeFirstSound,sectionStartTime,recordingDuration;
    boolean shouldFlickEars,finalStage;
    List<Integer>wfColours;
    boolean part2;
    Map <String,OBPhoneme>  wordDict;
    Map<String,String> wordAndSentenceDict;
    List<String>words,sentences;
    float textSize;
    Map<String,Map > sentenceDict;
    OBLabel mainLabel;
    String currKey;

    static Map LoadWordAndSentenceXML(String xmlPath)
    {
        Map dict = new HashMap();
        try {
            if (xmlPath != null) {
                OBXMLManager xmlman = new OBXMLManager();
                List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                OBXMLNode root = xl.get(0);

                List<OBXMLNode> nodes = root.childrenOfType("prompt");
                for (OBXMLNode promptNode : nodes) {
                    String audioid = promptNode.attributeStringValue("id");
                    String content = promptNode.contents;

                    dict.put(audioid,content);
                }
            }
        }
        catch(Exception e)
        {

        }
        return dict;
    }

    public Map loadComponent(String comp,String xmlPath)
    {
        Map<String,Map > dict = new HashMap();
        try
        {
            OBXMLNode xmlNode = null;
            if(xmlPath != null)
            {
                OBXMLManager xmlman = new OBXMLManager();
                List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                xmlNode = xl.get(0);
                for(OBXMLNode phrasenode : xmlNode.childrenOfType(comp))
                {
                    Map md = new HashMap();
                    md.putAll(phrasenode.attributes);
                    String k = phrasenode.attributeStringValue("id");
                    String contents = phrasenode.contents;
                    if(contents != null)
                        md.put("contents",contents);
                    dict.put(k,md);
                }
            }
        }
        catch(Exception e)
        {

        }
        return dict;
     }

    public void miscSetup()
    {
        //textSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("textsize")));
        textSize = (Float.parseFloat(eventAttributes.get("textsize")));
        OBGroup tiger = (OBGroup) objectDict.get("tiger");
        tiger.outdent(tiger.width()/3);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetup();
        events = new ArrayList<>(Arrays.asList("a","b","c","d","e","f","g"));
        doVisual(currentEvent());
        processAnchors();
        sectionStartTime = SystemClock.uptimeMillis();

    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void oDict(Map<String,OBControl> od,List<String> s,List<String> h)
    {
        lockScreen();
        for(String str : s)
            od.get(str).show();
        for(String str : h)
            od.get(str).hide();
        unlockScreen();
    }
    public void toucanBlink() throws Exception
    {
        OBGroup toucan =(OBGroup) objectDict.get("toucan");
        oDict(toucan.objectDict,Arrays.asList("eyehalf"),Arrays.asList("eyeopen","eyeclosed"));
        waitForSecs(0.1f);
        oDict(toucan.objectDict,Arrays.asList("eyeclosed"),Arrays.asList("eyeopen","eyehalf"));
        waitForSecs(0.1f);
        oDict(toucan.objectDict,Arrays.asList("eyehalf"),Arrays.asList("eyeopen","eyeclosed"));
        waitForSecs(0.1f);
        oDict(toucan.objectDict,Arrays.asList("eyeopen"),Arrays.asList("eyehalf","eyeclosed"));
    }

    public void toucanBlinker()
    {
        if(!aborting())
        {
            try
            {
                toucanBlink();
                OBUtils.runOnOtherThreadDelayed((float)(10 + rndom() * 5),new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        toucanBlinker();
                    }
                });
            }
            catch(Exception e)
            {
            }
        }
    }

    public void tigerBlink() throws Exception
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        oDict(head.objectDict,Arrays.asList("eyeshalf"),Arrays.asList("eyesopen","eyesclosed"));
        waitForSecs(0.05f);
        oDict(head.objectDict,Arrays.asList("eyesclosed"),Arrays.asList("eyesopen","eyeshalf"));
        waitForSecs(0.05f);
        oDict(head.objectDict,Arrays.asList("eyeshalf"),Arrays.asList("eyesopen","eyesclosed"));
        waitForSecs(0.05f);
        oDict(head.objectDict,Arrays.asList("eyesopen"),Arrays.asList("eyeshalf","eyesclosed"));
    }

    public void tigerBlinker()
    {
        if(!aborting())
        {
            try
            {
                tigerBlink();
                OBUtils.runOnOtherThreadDelayed((float)(7 + rndom() * 7),new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        tigerBlinker();
                    }
                });
            }
            catch(Exception e)
            {

            }
        }
    }

    public void speakEffectFile(String audioFile,float fromSecs,float toSecs,Map od,List<String> keys)
    {
        List<Integer> indices = new ArrayList<>();
        for(int i = 0;i < keys.size();i++)
            indices.add((i));
        try
        {
            long token = takeSequenceLockInterrupt(true);
            AssetFileDescriptor fd = OBUtils.getAssetFileDescriptorForPath(audioFile);
            playFile(fd,fromSecs,toSecs,2.0f);
            int idx = 0;
            while(effectPlayer.getState() < OBAP_PLAYING)
            {
                checkSequenceToken(token);
                waitForSecs(0.02f);
            }
            long playToken = effectPlayer.playToken;
            while(effectPlayer.getState() == OBAP_PLAYING && playToken == effectPlayer.playToken)
            {
                float av = effectPlayer.averagePower();
                MainActivity.log("av %g",av);
                if(av < 0.01)
                    idx = 0;
                int fno = indices.get(idx);
                List<List>split = splitSequence(keys,fno);
                oDict(od,split.get(0),split.get(1));

                waitForSecs(0.07 + rndom() /10);
                checkSequenceToken(token);
                int newIdx = randomInt(0,(int) keys.size() - 1);
                if(newIdx == idx)
                    newIdx =(idx + 1) % indices.size();
                idx = newIdx;
            }
        }
        catch(Exception exception)
        {
        }
        sequenceLock.unlock();
        int fno = indices.get(0);
        List<List>split = splitSequence(keys,fno);
        oDict(od,split.get(0),split.get(1));
    }

    public void speak(List audioFiles,Map od,List<String> keys)
    {
        List<Integer> indices = new ArrayList<>();
        for(int i = 0;i < keys.size();i++)
            indices.add((i));
        for (int i =(int) keys.size() - 1;i >= 0;i--)
            indices.add((i));
        try
        {
            long token = takeSequenceLockInterrupt(true);
            for(String audioFile : (List<String>)audioFiles)
            {
                AssetFileDescriptor fd = OBAudioManager.audioManager.getAudioPathFD(audioFile);
                playFile(fd,0,-1,1.0f);
                int idx = 0;
                while(effectPlayer.getState() < OBAP_PLAYING)
                {
                    checkSequenceToken(token);
                    waitForSecs(0.02f);
                }
                while(effectPlayer.getState() == OBAP_PLAYING)
                {
                    if(effectPlayer.averagePower() < 0.001)
                        idx = 0;
                    int fno = indices.get(idx).intValue();
                    List<List>split = splitSequence(keys,fno);
                    oDict(od,split.get(0),split.get(1));

                    waitForSecs(0.02 + rndom() /20);
                    checkSequenceToken(token);
                    idx =(idx + 1) % indices.size();
                }
                int fno = indices.get(0).intValue();
                List<List>split = splitSequence(keys,fno);
                oDict(od,split.get(0),split.get(1));

            }
        }
        catch(Exception exception)
        {
        }
        sequenceLock.unlock();
        int fno = indices.get(0);
        List<List>split = splitSequence(keys,fno);
        oDict(od,split.get(0),split.get(1));
    }

    public void toucanSpeak(List audioFiles)throws Exception
    {
        OBGroup toucan =(OBGroup) objectDict.get("toucan");
        speak(audioFiles,toucan.objectDict,Arrays.asList("beakclosed","beakhalf","beakopen"));
        toucanTwitchTail();
    }

    public void tigerSpeak(String audioFile,float fromSecs,float toSecs)
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        OBGroup mouth =(OBGroup) head.objectDict.get("mouth");
        speakEffectFile(audioFile,fromSecs,toSecs,mouth.objectDict,Arrays.asList("mouth0","mouth1","mouth2","mouth3","mouth4","mouth5","mouth6"));
    }
    public List<List> splitSequence(List<String> array,int idx)
    {
        List a1 = Arrays.asList(array.get(idx));
        List a2 = new ArrayList(array);
        a2.remove(idx);
        return Arrays.asList(a1,a2);
    }

    public void toucanSpreadWings()
    {
        OBGroup toucan =(OBGroup) objectDict.get("toucan");
        List lwings = Arrays.asList("lwing0","lwing1","lwing2","lwing3");
        List rwings = Arrays.asList("rwing0","rwing1","rwing2","rwing3");
        List<Integer> indices = Arrays.asList(0,1,2,3,3,2,1,0);
        try
        {
            for(Integer n : indices)
            {
                int idx = n.intValue();
                List<List>lsplit = splitSequence(lwings,idx);
                List<List>rsplit = splitSequence(rwings,idx);
                List sh = new ArrayList(lsplit.get(0));
                sh.addAll(rsplit.get(0));
                List hi = new ArrayList(lsplit.get(1));
                hi.addAll(rsplit.get(1));
                oDict(toucan.objectDict,sh,hi);
                waitForSecs(0.1f);
            }
        }
        catch(Exception e)
        {
        }
    }

    public void processAnchors()
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        head.setAnchorPointFromAnchor();
        OBGroup earleft =(OBGroup) head.objectDict.get("earleft");
        earleft.setAnchorPointFromAnchor();
        OBGroup earright =(OBGroup) head.objectDict.get("earright");
        earright.setAnchorPointFromAnchor();
    }

    public void tigerLeanHeadAnim(boolean anim) throws Exception
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        if(anim)
        {
            OBAnim rotAnim = OBAnim.rotationAnim((float)Math.toRadians(-20),head);
            OBAnimationGroup.runAnims(Arrays.asList(rotAnim),0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        }
        else
            head.setRotation((float)Math.toRadians(-20));
    }

    public void tigerUnleanHeadAnim(boolean anim) throws Exception
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        if(anim)
        {
            OBAnim rotAnim = OBAnim.rotationAnim((float)Math.toRadians(0),head);
            OBAnimationGroup.runAnims(Arrays.asList(rotAnim),0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        }
        else
            head.setRotation((float)Math.toRadians(0));
    }

    public void tigerFlickEars(int times) throws Exception
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        OBGroup earleft =(OBGroup) head.objectDict.get("earleft");
        OBGroup earright =(OBGroup) head.objectDict.get("earright");
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(10),earleft) ,OBAnim.rotationAnim((float)Math.toRadians(-10),earright)),0.05,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        for(int i = 0;i < times;i++)
            OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(-10),earleft) ,OBAnim.rotationAnim((float)Math.toRadians(10),earright)),0.1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.rotationAnim((float)Math.toRadians(0),earleft) ,OBAnim.rotationAnim((float)Math.toRadians(0),earright)),0.05,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void tigerFlicker()
    {
        if(!aborting())
        {
            try
            {
                if(shouldFlickEars)
                {
                    tigerFlickEars(1);
                    OBUtils.runOnOtherThreadDelayed((float)(1 + rndom() * 2),new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            tigerFlicker();
                        }
                    });
                }
            }
            catch(Exception e)
            {

            }
        }
    }

    public void tigerFlickWhiskers(int times) throws Exception
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        OBGroup head =(OBGroup) tiger.objectDict.get("head");
        OBGroup whiskers0 =(OBGroup) head.objectDict.get("whiskers0");
        OBGroup whiskers1 =(OBGroup) head.objectDict.get("whiskers1");
        List<OBGroup> arr = Arrays.asList(whiskers0,whiskers1);
        for(int i = 0;i < times;i++)
        {
            for(int j = 0;j < 2;j++)
            {
                lockScreen();
                arr.get(j%2).hide();
                arr.get((j + 1) %2).show();
                unlockScreen();
                waitForSecsNoThrow(0.2);
            }
        }
    }

    public void tigerFlickWhiskers()
    {
        if(!aborting())
        {
            try
            {
                if(true)
                {
                    tigerFlickWhiskers(1);
                    OBUtils.runOnOtherThreadDelayed((float)(13 + rndom() * 5),new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            tigerFlickWhiskers();
                        }
                    });
                }
            }
            catch(Exception e)
            {

            }
        }
    }

    public void tigerTwitchTail() throws Exception
    {
        OBGroup tiger =(OBGroup) objectDict.get("tiger");
        oDict(tiger.objectDict,Arrays.asList("tail1"),Arrays.asList("tail0"));
        waitForSecs(0.3f);
        oDict(tiger.objectDict,Arrays.asList("tail0"),Arrays.asList("tail1"));
    }

    public void tigerTailTwitcher()
    {
        if(!aborting())
        {
            try
            {
                if (effectPlayer != null &&(effectPlayer.getState() == OBAP_PLAYING || effectPlayer.getState() == OBAP_PREPARING))
                {
                    tigerTwitchTail();
                    OBUtils.runOnOtherThreadDelayed((float)(5 + rndom() * 7),new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            tigerTailTwitcher();
                        }
                    });
                }
            }
            catch(Exception e)
            {

            }
        }
    }

    public void toucanTwitchTail() throws Exception
    {
        OBGroup toucantail =(OBGroup) objectDict.get("toucantail");
        oDict(toucantail.objectDict,Arrays.asList("tail1"),Arrays.asList("tail0"));
        waitForSecsNoThrow(0.1);
        oDict(toucantail.objectDict,Arrays.asList("tail0"),Arrays.asList("tail1"));
    }

    public void setScenea()
    {
        targets = Arrays.asList(objectDict.get("tiger"));
    }

    public void setUpLabel(String txt)
    {
        OBFont font = StandardReadingFontOfSize(textSize);
        if(mainLabel != null)
        {
            detachControl(mainLabel);
            mainLabel = null;
        }
        mainLabel = new OBLabel(txt,font);
        mainLabel.setColour(Color.BLACK);
        mainLabel.setPosition(objectDict.get("paper").position());
        mainLabel.setZPosition(objectDict.get("paper").zPosition() + 1);
        attachControl(mainLabel);
    }

    public void doWord()
    {
        currKey = words.get(currNo);
        setUpLabel(wordAndSentenceDict.get(currKey));
    }

    public void setScene2a()
    {
        doWord();
    }

    public void doSentence()
    {
        currKey = sentences.get(currNo);
         setUpLabel((String)wordAndSentenceDict.get(currKey));
    }

    public void setScene2b()
    {
        doSentence();
        currNo++;
    }

    public void setScene2c()
    {
        doWord();
    }

    public void setScene2d()
    {
        doSentence();
        currNo++;
    }

    public void doMaina() throws Exception
    {
        //waitForSecs(0.2f);
        toucanBlinker();
        //waitForSecs(0.2f);
        toucanSpreadWings();
        tigerFlickWhiskers();
        waitForSecs(0.1f);
        toucanSpeak(currentAudio("PROMPT"));
        waitForSecs(0.3f);
        tigerFlicker();
    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.2f);
        toucanSpreadWings();
        waitForSecs(0.1f);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                toucanSpeak(currentAudio("PROMPT"));
            }
        });
        waitForSecs(0.1f);
    }

    public void flashTiger(long stt)
    {
        if(stt == statusTime())
        {
            OBControl swatch = objectDict.get("shadowswatch");
            Map swattrs = swatch.shadowAttributes();
            OBControl tigger = objectDict.get("tiger");
            Map tattrs = tigger.shadowAttributes();
            try
            {
                for(int i = 0;i < 3 && stt == statusTime();i++)
                {
                    tigger.setShadowAttributes(swattrs);
                    waitForSecs(0.2f);
                    tigger.setShadowAttributes(tattrs);
                    waitForSecs(0.2f);
                }
            }
            catch(Exception  e)
            {
                tigger.setShadowAttributes(tattrs);
            }
        }
    }

    public void tigerRemind(final long stt)
    {
        if(statusTime() == stt)
        {
            flashTiger(stt);
            OBUtils.runOnOtherThreadDelayed(10,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    tigerRemind(stt);
                }
            });
        }
    }
    public void endBody()
    {
        final List aud = currentAudio("PROMPT.REMINDER");
        if(aud != null)
        {
            try
            {
                final long stt = statusTime();
                effectPlayer.waitAudio();
                reprompt(stt, null, 5, new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
                            @Override
                            public void run() throws Exception {
                                tigerRemind(stt);
                            }
                        });
                        toucanSpeak(aud);
                    }
                });
            }
            catch(Exception e)
            {

            }
        }
    }

    public void recordingProcess()
    {
        try
        {
            playSfxAudio("beep",false);
            tigerLeanHeadAnim(true);
            tigerBlinker();
            setStatus(STATUS_BUSY);
            effectPlayer.stopPlaying();
            startRecording();
        }
        catch(Exception e)
        {

        }
    }

    public void startRecording()
    {
        setStatus(STATUS_RECORDING);
        final OC_SectionController weakthis = this;
        runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                if(recorder == null)
                    recorder = new OBGeneralAudioRecorder();
                if(recorder.recording())
                    recorder.stopRecording();
                timeRecordingStart = SystemClock.uptimeMillis();
                timeFirstSound = timeLastSound = 0;
                recorder.startRecordingToPath(OBUtils.getFilePathForTempFile(weakthis),10);
                startTimer();
                shouldFlickEars = true;
                runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        tigerFlicker();

                    }
                });
            }
        });
    }

    public void playbackFinished()
    {
        //NSLog("finished play");
    }

    public void playFile(AssetFileDescriptor fd)
    {
        playFile(fd,0,-1,1);
    }

    public void playFile(final AssetFileDescriptor fd,final float fromSecs,final float toSecs,final float vol)
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
           // @Override
            public void run() throws Exception
            {
                //if(effectPlayer == null)
                    effectPlayer = new OBAudioBufferPlayer(false,true);
                effectPlayer.stopPlaying();
                //effectPlayer.changePitchAmt(350);
                effectPlayer.setVolume(vol);
        //effectPlayer setCompletionBlock:^(OBAudioEffectPlayer player) {
          //      weakplaybackFinished();

                effectPlayer.startPlaying(fd, fromSecs,toSecs);

            }
        });
    }

    public boolean doneEnough()
    {
        long currTime = SystemClock.uptimeMillis();
        return(currTime - sectionStartTime > 300000.0);
    }
    public void nextRecording()
    {
        if(finalStage)
        {
            exitEvent();
            return;
        }
        try
        {
            finalStage = doneEnough();
            if(finalStage)
            {
                waitForSecs(0.3f);
                toucanSpreadWings();
                waitForSecs(0.3f);
                toucanSpeak(audioForEvent("final","DEMO"));
                waitForSecs(0.3f);
            }
            recordingProcess();
        }
        catch(Exception e)
        {
        }
    }

    public void playbackStageFrom(float fromSecs,float toSecs)
    {
         try
        {
            waitForSecs(0.3f);
            OBUtils.runOnOtherThreadDelayed(0.3f,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    tigerTailTwitcher();
                }
            });
            tigerSpeak(recorder.recordingPath,fromSecs,toSecs);
            setStatus(STATUS_CHECKING);
        }
        catch(Exception e)
        {

        }
    }

    public void cleanUp()
    {
        if(timer != null)
        {
            stopTimer();
        }
        if(recorder != null)
            recorder.stopRecording();
        recorder = null;
        if(effectPlayer != null && effectPlayer.getState() != OBAP_FINISHED)
        {
            effectPlayer.stopPlaying();
        }
        effectPlayer = null;
        super.cleanUp();
    }

    public long maximumDuration()
    {
        return 300000;
    }

    public void setUpPart2()
    {
        wordDict = LoadWordComponentsXML(true);
        String s = parameters.get("words");
        words = Arrays.asList(s.split(","));
        sentenceDict = loadComponent("sentence",getLocalPath("sentences.xml"));
        s = parameters.get("sentences");
        sentences = Arrays.asList(s.split(","));

        wordAndSentenceDict = LoadWordAndSentenceXML(getLocalPath("words.xml"));
    }

    public void goToPart2()
    {
        part2 = true;
        setUpPart2();
        events = new ArrayList(Arrays.asList("2a","2b"));
        int ct =(int) Math.min(words.size() , sentences.size());
        for(int i = 0;i < ct - 1;i++)
            events.addAll(Arrays.asList("2c","2d"));
        eventIndex = 0;
        objectDict.get("paper").show();
    }

    public void nextScene()
    {
        ++eventIndex;
        long timeElapsed = SystemClock.uptimeMillis() - sectionStartTime;
        if(!part2)
        {
            if(timeElapsed > 60000 || eventIndex >= events.size())
            {
                goToPart2();
            }
        }
        else
        {
            if(timeElapsed > maximumDuration() || eventIndex >= events.size())
            {
                fin();
                return;
            }
        }
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                setScene(events.get(eventIndex));
            }
        });
    }

    public void recordingFinished()
    {
        stopTimer();
        setStatus(STATUS_CHECKING);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                shouldFlickEars = false;
                tigerFlickEars(3);
                tigerUnleanHeadAnim(true);
                if(timeLastSound > 0)
                {
                    setStatus(STATUS_PLAYING_RECORDING);
                    try
                    {
                        float startsecs = (timeFirstSound - timeRecordingStart)/1000f;
                        float endsecs = (timeLastSound - timeRecordingStart) / 1000f;
                        startsecs -= 0.25;
                        if(startsecs < 0)
                            startsecs = 0;
                        endsecs += 1;
                        //if(timeFirstSound != 0)
                          //  NSLog("firstsound %g,lastsound %g",startsecs,endsecs);
                        playbackStageFrom(startsecs,endsecs);
                        waitForSecs(1f);
                    }
                    catch(Exception e)
                    {

                    }
                }
                if(part2)
                {
                    toucanSpeak(Arrays.asList(String.format("ebws_%s",currKey)));
                    waitForSecs(1f);
                }
                nextScene();
            }
        });
    }

    public void timerFire(OBTimer t)
    {
        if(t == timer)
        {
            if(!recorder.recording())
            {
                recordingFinished();
                return;
            }
            long currentTime = SystemClock.uptimeMillis();
            recordingDuration = currentTime - timeRecordingStart;
            float val = recorder.getAveragePower();
            MainActivity.log("%g",val);
            if(val > AUDIBLE_THRESHOLD)
            {
                MainActivity.log("currenttime %d",recordingDuration);
                timeLastSound = currentTime;
                if(timeFirstSound == 0)
                    timeFirstSound = timeLastSound;
            }
            else
            {
                if(currentTime - timeRecordingStart > 5000 && timeLastSound == 0)
                {
                    stopTimer();
                    recorder.stopRecording();
                    recordingFinished();
                    return;
                }
                if(timeLastSound > 0 && currentTime - timeRecordingStart > 3000 && currentTime - timeLastSound > 3000)
                {
                    recorder.stopRecording();
                    recordingFinished();
                }
            }
        }
        else
            t.invalidate();
    }

    public void startTimer()
    {
        OBUtils.runOnMainThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                if(timer != null)
                    timer.invalidate();
                timer = new OBTimer(0.03f)
                {
                    @Override
                    public int timerEvent(OBTimer timer)
                    {
                        timerFire(timer);
                        return 1;
                    }
                };
                timer.scheduleTimerEvent();
            }
        });
    }

    public void stopTimer()
    {
        if(timer != null)
            timer.invalidate();
        timer = null;
    }

    public void replayAudio()
    {
        if(busyStatuses.contains((status())))
        return;
        setStatus(status());
        final List aud = currentAudio("PROMPT.REPEAT");
        if(aud != null)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    toucanSpeak(aud);
                }
            });
        }
    }

    public void checkAnswer(OBControl targ)
    {
        try
        {
            takeSequenceLockInterrupt(true);
            unlockSequenceLock();
            recordingProcess();
        }
        catch(Exception e)
        {

        }
    }
    public Object findTarget(PointF pt)
    {
        return(finger(-1,1,targets,pt));
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        //MainActivity.log(">>>>>>>>>>>>>");
        if(status() == STATUS_AWAITING_CLICK)
        {
            setStatus(STATUS_CHECKING);
            runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    final Object targ = findTarget(pt);
                    if(targ != null)
                    {
                        checkAnswer((OBControl) targ);
                    }
                    else
                        setStatus(STATUS_AWAITING_CLICK);
                }
            });
        }
        else if(status() == STATUS_RECORDING)
        {
            Object targ = findTarget(pt);
            if(targ != null)
            {
                setStatus(STATUS_CHECKING);
                stopTimer();
                recorder.stopRecording();
                recordingFinished();
            }
        }
    }

}
