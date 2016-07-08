package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.xprz.controls.*;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBAnim;
import org.onebillion.xprz.utils.OBAnimationGroup;
import org.onebillion.xprz.utils.OBAudioManager;
import org.onebillion.xprz.utils.OBAudioRecorder;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OBWord;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 07/07/16.
 */
public class X_WordAudioRec extends XPRZ_SectionController
{

    static int MODE_LETTER=0,
            MODE_LETTERS=1,
            MODE_WORD=2;

    OBAudioRecorder audioRecorder;
    OBLabel fullWordLabel, partWordLabel;
    List<Map<String,Object>> eventsData;
    double recordDuration;
    int currentWord, currentMode;
    List<String> modeDict;
    OBControl screenImage;
    OBPhoneme partWordPhoneme;
    OBWord fullWord;
    boolean wordFeedback;
    OBGroup nextButton;



    public void prepare()
    {
        boolean permission1 = MainActivity.mainActivity.isStoragePermissionGranted();
        boolean permission2 = MainActivity.mainActivity.isMicrophonePermissionGranted();
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        modeDict = Arrays.asList("l","s","w");

        Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);

        objectDict.get("frame").setZPosition(12);
        OBControl shutter = objectDict.get("shutter");
        shutter.setZPosition(10);
        shutter.setAnchorPoint(new PointF(0f, 0.5f));
        shutter.setProperty("start_width",shutter.width());

        audioRecorder = new OBAudioRecorder(OBUtils.getFilePathForTempFile(this),this);
        objectDict.get("bg").show();

        eventsData = new ArrayList<>();

        wordFeedback = parameters.get("wordfeedback").equals("true");

        String[] arr = parameters.get("words").split(";");
        for(String param : arr)
        {
            String[] par = param.split(",");
            Map<String,Object> dict = new ArrayMap<>();
            OBWord word = (OBWord)componentDict.get(par[0]);
            if(word == null)
                continue;

            dict.put("word",word);
            int mode;
            if(par.length == 1)
            {
                mode = MODE_WORD;
            }
            else
            {
                OBPhoneme pho = componentDict.get(par[1]);
                if(pho == null)
                    continue;

                dict.put("display",pho);

                if(par[1].length() == 1)
                mode = MODE_LETTER;
                else
                mode = MODE_LETTERS;

            }

            dict.put("mode",mode);
            eventsData.add(dict);
        }

        currentWord = 1;


        nextButton = loadVectorWithName("arrow_next", new PointF(0, 0), new RectF(bounds()));
        nextButton.setScale((bounds().height() * 0.1f) / nextButton.height());
        nextButton.setBottom(bounds().height() - applyGraphicScale(10));
        nextButton.setRight(bounds().width() - applyGraphicScale(10));
        attachControl(nextButton);
        nextButton.hide();
        setWordScene(currentWord);

    }

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON|0|0|0;
    }


    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                doMainXX();
            }
        });
    }

    public void setWordScene(int wordNum)
    {
        Map<String,Object> curWord = eventsData.get(wordNum-1);
        currentMode = (int)curWord.get("mode");

        Typeface font = OBUtils.standardTypeFace();
        float textSize = currentMode == MODE_WORD ? applyGraphicScale(120) : applyGraphicScale(140);

        if(screenImage != null)
            detachControl(screenImage);

        hideAndResetLeftButtons();

        fullWord = (OBWord)curWord.get("word");
        OBControl textBox = objectDict.get("textbox");
        if(currentMode == MODE_WORD || wordFeedback)
        {
            fullWordLabel = new OBLabel(fullWord.text,font,textSize);
            fullWordLabel.setColour(Color.BLACK);

            fullWordLabel.setPosition(textBox.position());
            fullWordLabel.hide();
            attachControl(fullWordLabel);
            if(fullWordLabel.width()>textBox.width())
                fullWordLabel.setScale(1.0f - ((fullWordLabel.width()-textBox.width())*1.0f/fullWordLabel.width()));
        }

        if(currentMode != MODE_WORD && (!displayWordSameAsPrevious(wordNum) || wordFeedback))
        {
            partWordPhoneme = (OBPhoneme)curWord.get("display");
            partWordLabel = new OBLabel(partWordPhoneme.text,font,textSize);
            partWordLabel.setColour(Color.BLACK);

            OBAudioManager.audioManager.prepareForChannel(partWordPhoneme.audio(), "special");


            partWordLabel.setPosition(textBox.position());
            partWordLabel.hide();
            attachControl(partWordLabel);

            if(wordFeedback)
            {
                int index = fullWord.text.indexOf(partWordPhoneme.text);
                int len = partWordPhoneme.text.length();
                RectF bb = OBUtils.getBoundsForSelectionInLabel(index,index+len,fullWordLabel);
                float left = bb.left;


                fullWordLabel.setHighRange(index,index+len,Color.BLUE);
                partWordLabel.setProperty("dest_left",left);


            }
        }

        if(currentMode == MODE_WORD)
        {
            OBAudioManager.audioManager.prepareForChannel(fullWord.audio(), "special");
        }



        recordDuration = OBAudioManager.audioManager.durationForChannel("special");
        screenImage = loadImageWithName(fullWord.ImageFileName(), new PointF(0,0), new RectF(bounds()));
        OBControl bg = objectDict.get("bg");
        screenImage.setScale(applyGraphicScale(1));
        if(screenImage.width() > bg.width())
        {
            screenImage.setScale(screenImage.scale()*(bg.width()-10.0f)/screenImage.width());
        }
        screenImage.setZPosition(5);
        screenImage.setPosition(bg.position());

    }

    public void doMainXX() throws Exception
    {
        waitForSecs(0.3f);
        OBLabel curLabel = currentMode == MODE_WORD ? fullWordLabel: partWordLabel;
        wordShow(curLabel);
        String currentScene = String.format("%d%s",currentWord, modeDict.get(currentMode));

        if(currentWord == 1 )
        {
            List<String> demoAudio = getSceneAudio("DEMO");
            if(((String)parameters.get("demo")).equalsIgnoreCase("true"))
            {
                playAudio(demoAudio.get(0));
                waitAudio();
                waitForSecs(0.3f);
                loadPointer(POINTER_LEFT);
                movePointerToPoint(OB_Maths.locationForRect(0.8f,1.1f,curLabel.frame()),-40,0.5f,true);
                playAudio(demoAudio.get(1));
                waitAudio();
                waitForSecs(0.5f);
                thePointer.hide();
            }
            else
            {
                playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("DEMO2"), 300),true);
            }
        }

        waitForSecs(0.3f);


        if(getAudioForScene(currentScene,"PROMPT2") != null && displayWordSameAsPrevious(currentWord))
        {
            playSceneAudio("PROMPT2",true);
        }
        else
        {
            playSceneAudio("PROMPT",true);
        }

        setStatus(STATUS_AWAITING_CLICK);
        waitForSecs(currentMode == MODE_WORD ? 1.5 : 1);
        performRecordingEvent(curLabel,0);
    }

    public void nextWord() throws Exception
    {
        currentWord++;
        if(currentWord > eventsData.size())
        {
            fin();
        }
        else
        {
            lockScreen();
            setWordScene(currentWord);
            unlockScreen();
            doMainXX();
        }
    }

    public void fin()
    {
        try
        {

            waitForSecs(0.3f);
            playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene(String.format("finale%s", modeDict.get(currentMode)),"DEMO"),300),true);
           // (XPRZ_FatController*)FatController().completeEvent(;
            displayAward();
            exitEvent();
        }
        catch (Exception exception)
        {
        }

    }

    public void exitEvent()
    {
        OBUtils.cleanUpTempFiles(this);
        super.exitEvent();
    }

    public void replayAudio()
    {
        if(status() != STATUS_BUSY)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {

                @Override
                public void run() throws Exception
                {
                    replayWordAudio();
                }
            });


        }

    }

    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK &&
                finger(0,2,(List<OBControl>)(Object)Collections.singletonList(nextButton),pt) != null)
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    nextButtonClicked();
                }
            });
        }
    }


    public void animateShutter(boolean open) throws Exception
    {
        OBControl shutter = objectDict.get("shutter");
        playSfxAudio(open?"shutteropen":"shutterclosed",false);
        if(!open)
            shutter.show();
        OBAnimationGroup.runAnims(Collections.singletonList(OBAnim.propertyAnim("width",open?1:(float)shutter.settings.get("start_width"),shutter)),0.3f,true,open?OBAnim.ANIM_EASE_IN:OBAnim.ANIM_EASE_OUT,this);
        if(open)
            shutter.hide();
        waitSFX();
    }

    public boolean displayWordSameAsPrevious(int index)
    {
        index--;
        if(index == 0 || eventsData.size() <= index)
            return false;

        Map<String,Object> cur = eventsData.get(index);
        Map<String,Object> prev = eventsData.get(index-1);

        if((int)cur.get("mode") == MODE_WORD
                || (int)prev.get("mode")  == MODE_WORD
                || cur.get("display") == null || prev.get("display") == null)
        return false;

        return cur.get("display") == prev.get("display");
    }

    public void performRecordingEvent(OBLabel curLabel,int count) throws Exception
    {
        wordRecordStart(curLabel,true);
        setStatus(STATUS_AWAITING_CLICK);
        startRecording();

        audioRecorder.waitForRecord();
        if(count < 2 && !audioRecorder.audioRecorded())
        {
            setStatus(STATUS_BUSY);
            curLabel.setColour(Color.BLACK);

            if(count == 0 || getSceneAudio("REMINDER2") == null)
            {
                playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("REMINDER"), 300),true);
            }
            else
            {
                playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio("REMINDER2"), 300),true);
            }

            count++;
            waitForSecs(0.5f);
            performRecordingEvent(curLabel,count);
        }
        else
        {
            setStatus(STATUS_BUSY);
            wordRecordStop(curLabel);
            if(getSceneAudio("FINAL") != null)
            {
                playAudio(getSceneAudio("FINAL").get(0));
                waitAudio();
            }
            waitForSecs(0.5f);
            audioRecorder.playRecording();
            waitAudio();
            waitForSecs(0.3f);

            if(currentMode != MODE_WORD)
            {
                partWordLabel.setColour(Color.BLUE);
                waitForSecs(0.3f);
                partWordPhoneme.playAudio(this,true);
                waitForSecs(0.5f);
            }


            animateShutter(true);
            waitForSecs(0.3f);

            if(currentMode == MODE_WORD)
            {
                fullWordLabel.setColour(Color.BLUE);
            }
            else
            {
                if(wordFeedback)
                    animatePartWordSlide();
            }

            waitForSecs(0.3f);
            fullWord.playAudio(this,true);
            waitForSecs(0.3f);
            if(currentMode == MODE_WORD)
            {
                fullWordLabel.setColour(Color.BLACK);
            }
            else
            {
                if(wordFeedback)
                {
                    //[colourEntireLabel:fullWordLabel colour:.get(Color.BLACK]);
                }
                else
                {
                    partWordLabel.setColour(Color.BLACK);
                }
            }
            waitForSecs(1f);


            if(!isLastWord())
            {
                showLeftButtons();
                playSfxAudio("arrowon",true);
                playSceneAudio("ARROW",false);
                flashNextButton(setStatus(STATUS_AWAITING_CLICK));
            }
            else
            {
                nextWord();
            }
        }
    }

    public void replayWordAudio()
    {

        long  token = takeSequenceLockInterrupt(true);
        try
        {
            if (token == sequenceToken)
            {
                if(currentMode != MODE_WORD)
                {
                    partWordLabel.setColour(Color.BLUE);
                    partWordPhoneme.playAudio(this,true);
                    checkSequenceToken(token);
                    waitForSecs(0.5f);
                    checkSequenceToken(token);
                }
                else
                {
                    fullWordLabel.setColour(Color.BLUE);
                }

                fullWord.playAudio(this,true);
                checkSequenceToken(token);
                waitForSecs(0.3f);
                checkSequenceToken(token);

            }
        }
        catch (Exception exception)
        {
        }
        if(currentMode == MODE_WORD)
        {
            fullWordLabel.setColour(Color.BLACK);
        }
        else
        {
            if(!wordFeedback)
            {
                partWordLabel.setColour(Color.BLACK);
            }
        }

        sequenceLock.unlock();

    }

    public void wordShow(OBLabel curLabel) throws Exception
    {
        if(curLabel.hidden)
        {
            playSfxAudio("texton",false);
            curLabel.show();
            waitSFX();
        }
    }


    public void wordRecordStart(OBLabel curLabel,boolean pulse) throws Exception
    {
        playSfxAudio("ping",false);
        curLabel.setColour(Color.RED);
        if(pulse)
            animateWordPulse(curLabel);

        waitSFX();
    }

    public void wordRecordStop(OBLabel curLabel) throws Exception
    {
        playSfxAudio("click",false);
        curLabel.setColour(Color.BLACK);
        waitSFX();
    }

    public void startRecording()
    {
        audioRecorder.startRecording(recordDuration * 1.5 + 1);
    }


    public void stopRecoding()
    {
        audioRecorder.stopRecording();
    }

    public void nextButtonClicked() throws Exception
    {

        setStatus(STATUS_BUSY);
        nextButton.highlight();
        takeSequenceLockInterrupt(true);
        sequenceLock.unlock();
        if(!isLastWord())
        {
            lockScreen();
            if(currentMode != MODE_WORD && (!displayWordSameAsPrevious(currentWord+1)  || wordFeedback))
            {
                detachControl(partWordLabel);
                partWordLabel = null;
            }
            if(currentMode == MODE_WORD || wordFeedback)
            {
                detachControl(fullWordLabel);
                fullWordLabel = null;
            }
            hideAndResetLeftButtons();
            unlockScreen();

            waitForSecs(0.5f);
            animateShutter(false);
            waitForSecs(0.5f);
        }

        nextWord();


    }

    public boolean isLastWord()
    {
        return currentWord == eventsData.size();
    }


    public void animateWordPulse(OBLabel wordLabel)
    {
        float startScale = wordLabel.scale();
        OBAnimationGroup.chainAnimations(Arrays.asList(Collections.singletonList(OBAnim.scaleAnim(startScale*1.2f,wordLabel)),
                Collections.singletonList(OBAnim.scaleAnim(startScale,wordLabel))),
                Arrays.asList(0.2f,0.2f), false,
                Arrays.asList(OBAnim.ANIM_EASE_IN_EASE_OUT,OBAnim.ANIM_EASE_IN_EASE_OUT),1,this);
    }

    public void animatePartWordSlide() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        if(fullWordLabel.scale() != partWordLabel.scale())
        {
            anims.add(OBAnim.scaleAnim(fullWordLabel.scale(),partWordLabel));
        }
        anims.add(OBAnim.propertyAnim("left",(float)partWordLabel.settings.get("dest_left"),partWordLabel));

        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        lockScreen();
        fullWordLabel.show();
        partWordLabel.hide();
        playSfxAudio("texton",false);
        unlockScreen();
        waitSFX();
    }

    public List<String> getSceneAudio(String audio)
    {
        String currentScene = String.format("%d%s",currentWord, modeDict.get(currentMode));
        if(audioScenes.get(currentScene) != null)
        {
            return getAudioForScene(currentScene,audio);
        }
        else
        {
            return getAudioForScene(String.format("default%s", modeDict.get(currentMode)),audio);
        }
    }

    public void playSceneAudio(String name, boolean wait) throws Exception
    {
        setReplayAudio(OBUtils.insertAudioInterval(getSceneAudio(String.format("%s.REPEAT",name)),300));
        playAudioQueued(OBUtils.insertAudioInterval(getSceneAudio(name),300), wait);

    }

    public void showLeftButtons()
    {
        lockScreen();
        nextButton.show();
        nextButton.setOpacity(1);
        MainViewController().topRightButton.show();
        MainViewController().topRightButton.setOpacity(1);


        unlockScreen();
    }

    public void hideAndResetLeftButtons()
    {
        lockScreen();
        nextButton.hide();
        nextButton.lowlight();
        nextButton.setOpacity(1);
        MainViewController().topRightButton.hide();
        MainViewController().topRightButton.setOpacity(0);
        unlockScreen();
    }

    public void flashNextButton(final long time)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                try
                {
                    waitForSecs(3f);
                    if(time == statusTime && !_aborting)
                    {
                        nextButton.setOpacity(1);
                    }
                    waitForSecs(0.5f);
                    for (int i = 0;i < 2;i++)
                    {
                        if(time == statusTime && !_aborting)
                        {
                            nextButton.setOpacity(0.2f);
                        }
                        waitForSecs(0.3f);
                        if(time == statusTime && !_aborting)
                        {
                            nextButton.setOpacity(1);
                        }
                        waitForSecs(0.3f);
                    }
                    if(time == statusTime && !_aborting)
                    {
                        flashNextButton(statusTime);
                    }

                }
                catch (Exception exception)
                {
                    nextButton.setOpacity(1);
                }
            }
        });
    }




}
