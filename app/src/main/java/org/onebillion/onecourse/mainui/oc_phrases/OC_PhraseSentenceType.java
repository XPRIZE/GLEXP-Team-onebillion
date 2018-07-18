package org.onebillion.onecourse.mainui.oc_phrases;

import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OBMainViewController;
import org.onebillion.onecourse.mainui.oc_typing.OC_TypewriterManager;
import org.onebillion.onecourse.mainui.oc_typing.OC_TypewriterReceiver;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import org.onebillion.onecourse.utils.OB_MutInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 13/06/2018.
 */

public class OC_PhraseSentenceType extends OC_PhraseSentence implements OC_TypewriterReceiver
{

    boolean sentenceMode;
    List<Map<String,Object>> currentWordData;
    List<OBReadingWord> currentReadingWords;
    int currentWordIndex, currentLetterIndex;
    OBLabel currentLabel;
    List<Map<String,Object>> eventsData;
    OC_TypewriterManager typewriterManager;
    OBPath screenLine;
    List<String> currentAudios;
    int wrongCount, currentMode;
    int lowlightColour, lineColour;
    boolean hiddenMode, showExample, showPresenter;

    private Handler reminderHandler;
    private Runnable reminderRunnable;

    @Override
    public float getFontSize()
    {
        return super.getFontSize() *(sentenceMode ? 0.8f : 0.9f);
    }

    @Override
    public void miscSetUp()
    {
        setStatus(STATUS_BUSY);
        loadEvent("master");
        screenLine =(OBPath)objectDict.get("line");
        screenLine.sizeToBoundingBoxIncludingStroke();
        lineColour = screenLine.strokeColor();
        showExample = OBUtils.getBooleanValue(parameters.get("example"));
        showPresenter = OBUtils.getBooleanValue(parameters.get("presenter"));
        currentMode = OBUtils.getIntValue(parameters.get("type"));
        lowlightColour = OBUtils.colorFromRGBString(eventAttributes.get("colour_lowlight"));
        reminderHandler = new Handler();
        screenLine.hide();
        Map<String,Map<String,Object>> components = new ArrayMap<>();
        if(parameters.get("mode").equals("phrase"))
        {
            sentenceMode = false;
            components.putAll(loadComponent("phrase",getLocalPath("phrases.xml")));
        }
        else
        {
            sentenceMode = true;
            components.putAll(loadComponent("sentence",getLocalPath("sentences.xml")));
        }
        loadAudioXML(getConfigPath(String.format(sentenceMode ? "tst%daudio.xml" : "tph%daudio.xml",currentMode)));
        componentDict = components;


        OBControl tb = objectDict.get("textbox");
        textBoxOriginalPos = new PointF();
        textBoxOriginalPos.set(tb.position());
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        textBox.setShouldTexturise(false);
        attachControl(textBox);

        String[] idList = parameters.get("targets").split(",");
        loadEventsForList(Arrays.asList(idList));
        highlightColour = Color.RED;
        typewriterManager = new OC_TypewriterManager(parameters.get("keyboard"), objectDict.get("keyboard_rect"),
                (OBPath)objectDict.get("button_normal"),(OBPath)objectDict.get("button_disabled"),
                getAudioForScene("sfx","click").get(0), this);
        typewriterManager.lockKeysForString(parameters.get("locked"));
        typewriterManager.setSpecialAudio(getAudioForScene("sfx","fullstop").get(0),OC_TypewriterManager.KEY_DOT);
        typewriterManager.setSpecialAudio(getAudioForScene("sfx","spacebar").get(0),OC_TypewriterManager.KEY_SPACE);
        if(sentenceMode)
            typewriterManager.setCapitalMode(true);
        OBControl bg = objectDict.get("keyboard_bg");
        typewriterManager.fitBackground(bg);
        setUpWordStuff();
    }

    @Override
    public void setUpScene()
    {
        cleanUpScene();
        Map<String,Object> eventDict = eventsData.get(eventIndex);
        currComponentKey = (String)eventDict.get("target");

        hiddenMode = (boolean)eventDict.get("hidden");
        Map<String,Object> currPhraseDict = componentDict.get(currComponentKey);
        String ptext = (String)currPhraseDict.get("contents");
        OBReadingPara para = new OBReadingPara(ptext,1);
        paragraphs = Arrays.asList(para);
        layOutText();
        calcWordFrames();
        //adjustTextPosition();
        List<String> curAudios = new ArrayList<>();
        int i=1;
        for(OBReadingPara parag : paragraphs)
        {
            loadTimingsPara(parag, getLocalPath(String.format("%s_%d.etpa",currComponentKey,i)),false);
            String audioName = String.format("%s_%d",currComponentKey,i);
            curAudios.add(audioName);
            i++;
        }
        currentAudios = curAudios;
        highlightCurrentParagraph(hiddenMode ? Color.TRANSPARENT : Color.BLACK);
        if(hiddenMode)
            textBox.show();
        else
            textBox.hide();
        List<OBReadingWord> readingWords = new ArrayList<>();
        for(OBReadingPara readingPara : paragraphs)
        {
            for(OBReadingWord readingWord : readingPara.words)
            {
                if(readingWord.label != null && !readingWord.label.text().equals(" "))
                {
                    if(readingWord.label.text().equals(","))
                    {
                        readingWord.label.hide();
                    }
                    else
                    {
                        readingWords.add(readingWord);
                    }
                }
            }
        }
        currentReadingWords = readingWords;
        if(sentenceMode)
        {
            float leftAlign = -1, startY = -1, difAlign = 0;
            for(OBReadingPara readingPara : paragraphs)
            {
                for(OBReadingWord readingWord : readingPara.words)
                {
                    if(readingWord.label != null)
                    {
                        OBLabel label = readingWord.label;
                        if(leftAlign < 0)
                        {
                            leftAlign = label.left();
                            startY = label.position().y;
                        }
                        if(Math.abs(startY - label.position().y) > applyGraphicScale(5))
                        {
                            startY = label.position().y;
                            difAlign = label.left() - leftAlign;
                        }
                        if(difAlign != 0)
                            label.setLeft(label.left() - difAlign);
                    }
                }}

        }
        currentWordIndex = 0;
        currentLetterIndex = 0;
        wordIdx = 0;
        wrongCount = 0;
        setupCurrentLabel();
    }

    public int buttonFlags()
    {
        return OBMainViewController.SHOW_TOP_LEFT_BUTTON|OBMainViewController.SHOW_TOP_RIGHT_BUTTON|0|0;
    }

    @Override
    public void setSceneXX(String scene)
    {
        currPara = 0;
        setUpScene();
    }

    @Override
    public void doMainXX() throws Exception
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                setStatus(STATUS_BUSY);
                doIntro();
                List<String> repeat = getAudioForScene(currentEvent(), "PROMPT.REPEAT");
                if (repeat != null)
                    setReplayAudio(OBUtils.insertAudioInterval(repeat,300));
                showScreenLine(false);
                flashLineTime(typewriterManager.unlock(), 5);
            }
        });
    }

    @Override
    public void replayAudio()
    {
        try
        {
            if (status() == STATUS_AWAITING_CLICK)
            {
                if (hiddenMode)
                {
                    playAudioQueued(OBUtils.insertAudioInterval(currentAudios, 300));
                }
                else
                {
                    super.replayAudio();
                }
            }
        }
        catch (Exception e)
        {

        }
    }


    @Override
    public void touchDownAtPoint(PointF pt, View v)
    {
        typewriterManager.touchDownAtPoint(pt);
    }

    @Override
    public void touchMovedToPoint(PointF pt,View v) {
    }

    @Override
    public void touchUpAtPoint(PointF pt,View v)
    {
        typewriterManager.touchUpAtPoint(pt);
    }

    public void touchUpKey(OBGroup key)
    {
        typewriterManager.touchUpKey(key);
        flashLineTime(typewriterManager.unlock(),5);
    }

    public void touchDownKey(final OBGroup key, boolean sound)
    {
        Map<String,Object> letterDict = currentWordData.get(currentLetterIndex);
        final OBGroup correctKey = (OBGroup)letterDict.get("key");
        boolean correct = correctKey == key;
        boolean lastLetter = currentWordIndex == currentReadingWords.size()-1 && currentLetterIndex == currentWordData.size()-1;
        lockScreen();
        typewriterManager.touchDownKey(key,correct);
        if(correct)
        {
            highlightCurrentLetter(Color.BLACK);
        }
        if(lastLetter)
            screenLine.hide();

        unlockScreen();
        if(correct)
        {
            boolean recapitalise = currentWordIndex == 0 && currentLetterIndex == 0 && sentenceMode;
            if(recapitalise)
            {
                typewriterManager.lock();
                typewriterManager.skipTouchUp();
            }
            nextLetter(key);
            if(recapitalise)
            {
                typewriterManager.lowlightKey(key);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        demoTypeCapLetter();
                        typewriterManager.unlock();
                    }
                });
            }
        }
        else if(!correct)
        {
            typewriterManager.lock();
            typewriterManager.skipTouchUp();
            wrongCount++;
            playAudio(null);
            gotItWrongWithSfx();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception {
                    waitForSecs(0.2f);
                    boolean spaceKey = correctKey == typewriterManager.keyboardKeys.get(OC_TypewriterManager.KEY_SPACE);
                    boolean dotKey = correctKey == typewriterManager.keyboardKeys.get(OC_TypewriterManager.KEY_DOT);
                    lockScreen();
                    typewriterManager.lowlightKey(key);
                    if (spaceKey || dotKey) {
                        typewriterManager.disableAllSkip(correctKey);
                    } else {
                        if (wrongCount == 1)
                            typewriterManager.disableRowsSkip(correctKey);
                        else
                            typewriterManager.disableAllSkip(correctKey);
                    }

                    unlockScreen();
                    long time = typewriterManager.unlock();
                    if (spaceKey) {
                        playAudioQueuedScene("INCORRECT", 0.3f, true);
                    } else if (dotKey) {
                        playAudioQueuedScene("INCORRECT2", 0.3f, true);

                        flashLineTime(time, 5);
                    }
                }
            });
        }
    }

    public void doIntro() throws Exception
    {
        if(eventIndex == 0)
        {
            demoIntro1();
            demoIntro2();
        }
        if(eventIndex == 0 && showExample)
        {
            demoExample();
        }
        demoCapitalise();
        performSel("demoEvent",currentEvent());
        if(thePointer != null)
            thePointer.hide();
        waitForSecs(0.f);
        int curAud = eventIndex%3;
        boolean isDefault = currentEvent().equals("default");
        if(isDefault && curAud == 1 && audioScenes.get("PROMPT2") != null)
        {
            playAudioQueuedScene("PROMPT2",0.3f,true);
        }
        else if(isDefault && curAud == 2 && audioScenes.get("PROMPT3") != null)
        {
            playAudioQueuedScene("PROMPT3",0.3f,true);
        }
        else
        {
            playAudioQueuedScene("PROMPT",0.3f,true);
        }
        waitForSecs(0.6f);
        phraseIntro();
        waitForSecs(0.3f);
    }

    public void demoCapitalise() throws Exception
    {
        if(sentenceMode && !typewriterManager.capitalMode())
        {
            waitForSecs(0.3f);
            capitaliseKeyboard(true);
            waitForSecs(0.3f);
        }
    }

    public void phraseIntro() throws Exception
    {
        if(!hiddenMode)
        {
            lockScreen();
            textBox.show();
            playSfxAudio("phraseon",false);
            unlockScreen();
            waitSFX();
            waitForSecs(0.3f);
        }

        if(hiddenMode)
        {
            playAudioQueued(OBUtils.insertAudioInterval(currentAudios, 300),true);
        }
        else
        {
            readPage();
        }

        if(!hiddenMode)
        {
            waitForSecs(0.3f);
            lockScreen();
            highlightCurrentParagraph(lowlightColour);
            playSfxAudio("grey",false);
            unlockScreen();
            waitSFX();
        }
        waitForSecs(0.3f);
    }

    public void showScreenLine(boolean wait) throws Exception
    {
        screenLine.show();
        playSfxAudio("lineshow",wait);
    }

    public void cleanUpScene()
    {
        if(paragraphs != null) {
            for (OBReadingPara para : paragraphs)
                for (OBReadingWord w : para.words)
                    if (w.label != null)
                        w.label.parent.removeMember(w.label);
            textBox.setPosition(textBoxOriginalPos);
        }
    }

    public void clearOff() throws Exception
    {
        playSfxAudio("phraseoff",false);
        lockScreen();
        textBox.hide();
        unlockScreen();
        waitForSecs(0.2f);
        waitSFX();
    }

    public void loadEventsForList(List<String> idList)
    {
        events = new ArrayList<>();
        eventsData = new ArrayList<>();
        for(int i=0; i<idList.size(); i++)
        {
            String phid = idList.get(i);
            if(componentDict.get(phid) != null)
            {
                String eventName = String.format("%d", i+1);
                if(i == idList.size()-1)
                    eventName = "last";
                addEvent(eventName,phid);
            }
        }
    }

    public void addEvent(String eventName,String target)
    {
        if(currentMode == 2)
        {
            for(int i=1; i<3; i++)
            {
                String subEventName = String.format("%s_%d",eventName,i);
                addEventDataForTarget(target,i==2);
                if(audioScenes.containsKey(subEventName))
                {
                    events.add(subEventName);
                }
                else
                {
                    events.add(String.format("default_%d",i));
                }
            }
        }
        else
        {
            addEventDataForTarget(target,currentMode==3);
            if(audioScenes.containsKey(eventName))
            {
                events.add(eventName);
            }
            else
            {
                events.add("default");
            }
        }
    }

    public void addEventDataForTarget(String target,boolean hidden)
    {
        Map<String,Object> eventDict =  new ArrayMap<>();
        eventDict.put("target",target);
        eventDict.put("hidden",hidden);
        eventsData.add(eventDict);
    }

    public void resetKeyboard(OBGroup skipKey)
    {
        wrongCount = 0;
        lockScreen();
        for(OBGroup key : typewriterManager.keyboardKeys.values())
        {
            if(skipKey != key)
            {
                typewriterManager.enableKey(key);
            }
        }
        unlockScreen();
    }

    public void moveLineToCurrentLetter()
    {
        deregisterAnimationGroupWithName("line_move");
        Map<String,Object> letterData = currentWordData.get(currentLetterIndex);
        PointF loc = (PointF)letterData.get("line_loc");
        float width = (float)letterData.get("line_width");
        List<OBAnim> anims = new ArrayList<>();
        anims.add(OBAnim.moveAnim(loc, screenLine));
        if (!hiddenMode)
            anims.add(OBAnim.propertyAnim("width", width, screenLine));
        OBAnimationGroup animGroup = OBAnimationGroup.runAnims(anims, 0.2, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        registerAnimationGroup(animGroup, "line_move");
    }

    public void startPhase() throws Exception
    {
        wrongCount = 0;
        moveLineToCurrentLetter();
        //typewriterManager.unlock();
    }

    public void readCurrentParagraph() throws Exception
    {
        int index = 0;
        while(index < paragraphs.size())
        {
            readParagraph(index,-1,false);
            index++;
            waitForSecs(0.6f);
        }
    }

    public void setupCurrentLabel()
    {
        OBReadingWord readingWord = currentReadingWords.get(currentWordIndex);
        currentLabel = readingWord.label;
        OBFont font = currentLabel.font();
        currentWordData = new ArrayList<>();
        for(int i=0; i< currentLabel.text().length(); i++)
        {
            String letter = currentLabel.text().substring(i, i+1);
            if(typewriterManager.keyboardKeys.containsKey(letter.toLowerCase()))
            {
                Map<String,Object> dict = new ArrayMap();
                dict.put("letter",letter);
                dict.put("display",letter);
                dict.put("range",i);
                dict.put("key",typewriterManager.keyboardKeys.get(letter.toLowerCase()));
                currentWordData.add(dict);
            }
            else if(currentWordData.size() > 0)
            {
                String lastDisplay = (String)currentWordData.get(currentWordData.size()-1).get("display");
                currentWordData.get(currentWordData.size()-1).put("display",String.format("%s%s",lastDisplay,letter));
            }
        }
        PointF lineLoc = OBMisc.copyPoint(currentLabel.getWorldPosition());
        lineLoc.y += OBUtils.getFontCapHeight(font.typeFace, font.size)/2.0f + screenLine.lineWidth();
        for(Map<String,Object> letterDict : currentWordData)
        {
            String letter = (String)letterDict.get("display");
            int index = (int)letterDict.get("range");
            RectF selectionRect = OBUtils.getBoundsForSelectionInLabel(index, index + letter.length(), readingWord.label);
            letterDict.put("line_width", selectionRect.width());
            letterDict.put("line_loc", new PointF(selectionRect.centerX(),lineLoc.y));
        }

        RectF rect = currentLabel.getWorldFrame();
        Map<String,Object> attr = new ArrayMap<>();
        attr.put("font",font);
        float spaceWidth = WidthOfText(" ", attr, 0);
        int wordPoolSize = sentenceMode ? currentReadingWords.size()-3 : currentReadingWords.size()-2;
        if(currentWordIndex <= wordPoolSize)
        {
            Map<String,Object> letterDict = new ArrayMap();
            letterDict.put("line_loc",new PointF(rect.right + spaceWidth*0.5f,lineLoc.y));
            letterDict.put("line_width",spaceWidth);
            letterDict.put("letter",OC_TypewriterManager.KEY_SPACE);
            letterDict.put("key",typewriterManager.keyboardKeys.get(OC_TypewriterManager.KEY_SPACE));
            letterDict.put("range",-1);
            currentWordData.add(letterDict);
        }
        else if(sentenceMode && currentWordIndex == currentReadingWords.size()-1)
        {
            Map<String,Object> letterDict = new ArrayMap();
            letterDict.put("line_loc",new PointF(rect.centerX(), lineLoc.y));
            letterDict.put("line_width",spaceWidth);
            letterDict.put("letter",OC_TypewriterManager.KEY_SPACE);
            letterDict.put("key",typewriterManager.keyboardKeys.get(OC_TypewriterManager.KEY_DOT));
            letterDict.put("range",0);
            currentWordData.add(letterDict);
        }
        currentLetterIndex = 0;

        if(currentWordIndex == 0)
        {
            screenLine.setPosition((PointF) currentWordData.get(0).get("line_loc"));
            screenLine.setWidth(hiddenMode ? spaceWidth*1.5f : (float)currentWordData.get(0).get("line_width"));
            screenLine.setProperty("start_loc",OBMisc.copyPoint(screenLine.position()));
        }
    }

    public void nextLetter(final OBGroup key)
    {
        wrongCount = 0;
        resetKeyboard(key);
        currentLetterIndex++;
        if(currentLetterIndex >= currentWordData.size())
        {
            currentWordIndex++;
            currentLetterIndex = 0;
            if(currentWordIndex >= currentReadingWords.size())
            {
                typewriterManager.lock();
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        waitForSecs(0.2f);
                        typewriterManager.lowlightKey(key);
                        if (sentenceMode && eventIndex == 0)
                            demoTypeDot();
                        phraseCompleted();
                    }
                });
            }
            else
            {
                setupCurrentLabel();
                moveLineToCurrentLetter();
            }
        }
        else
        {
            moveLineToCurrentLetter();
        }
    }

    public void highlightCurrentParagraph(int colour)
    {
        for(OBReadingPara para : paragraphs)
        {
            for(OBReadingWord w : para.words)
            {
                if(w.label != null)
                    w.label.setColour(colour);
            }
        }
    }

    public void capitaliseKeyboard(boolean caps) throws Exception
    {
        lockScreen();
        for(OBGroup key : typewriterManager.keyboardKeys.values())
            key.highlight();
        playSfxAudio("keyswitch1",false);

        unlockScreen();
        waitSFX();
        waitForSecs(0.5f);
        lockScreen();
        for(OBGroup key : typewriterManager.keyboardKeys.values())
            key.lowlight();
        typewriterManager.setCapitalMode(caps);
        playSfxAudio("keyswitch2",false);
        unlockScreen();
        waitSFX();
    }

    public void highlightCurrentLetter(int colour)
    {
        Map<String,Object> letterData = currentWordData.get(currentLetterIndex);
        int range = (int)letterData.get("range");
        if(range > -1)
        {
            currentLabel.setHighRange(0,range+1,colour);
        }
    }

    public void phraseCompleted() throws Exception
    {
        waitForSecs(0.5f);
        readPage();
        waitForSecs(0.3f);
        gotItRightBigTick(true);
        waitForSecs(0.3f);
        playAudioQueuedScene("FINAL",0.3f,true);
        waitForSecs(1f);
        if(currentEvent() != events.get(events.size()-1))
        {
            clearOff();
            waitForSecs(0.4f);
        }
        nextScene();
    }

    public RectF getTextRect()
    {
        RectF rect = new RectF();
        for(int i=0; i<paragraphs.size(); i++)
        {
            OBReadingPara readingPara = paragraphs.get(i);
            for(int j=0; j<readingPara.words.size(); j++)
            {
                OBReadingWord readingWord = readingPara.words.get(j);
                if(readingWord.frame != null)
                {
                    RectF wordFrame = readingWord.frame;
                    if (i == 0 && j == 0)
                        rect = new RectF(wordFrame);
                    else
                        rect.union(wordFrame);
                }
            }
        }
        return rect;
    }

    public void flashButton(OBGroup group,int count) throws Exception
    {
        for(int i=0; i<count; i++)
        {
            group.setOpacity(0.5f);
            waitForSecs(0.3f);
            group.setOpacity(1);
            waitForSecs(0.3f);
        }
    }

    public void flashLineTime(final long time,final float delay)
    {
        if(reminderRunnable != null)
            reminderHandler.removeCallbacks(reminderRunnable);

        reminderRunnable = new Runnable() {
            @Override
            public void run() {
                if(statusChanged(time))
                    return;
                OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
                    @Override
                    public void run() throws Exception {
                        try {
                            while (!statusChanged(time)) {
                                for (int i = 0;
                                     i < 4 && !statusChanged(time);
                                     i++) {
                                    screenLine.setStrokeColor(Color.RED);
                                    if (statusChanged(time))
                                        break;
                                    waitForSecs(0.6f);
                                    screenLine.setStrokeColor(lineColour);
                                    if (statusChanged(time))
                                        break;
                                    waitForSecs(0.6f);
                                }
                                screenLine.setStrokeColor(lineColour);
                            }

                        } catch (Exception exception) {

                        } finally {
                            screenLine.setStrokeColor(lineColour);
                        }
                    }
                });


            }
        };
        reminderHandler.postDelayed(reminderRunnable, (long)(delay * 1000));
    }

    public void demoIntro1() throws Exception
    {
        if(showPresenter)
        {
            OBMisc.standardDemoIntro1(this);
        }
    }

    public void demoIntro2() throws Exception
    {
        if(!showPresenter)
        {
            OBMisc.standardDemoIntro2(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),
                    OB_Maths.locationForRect(0.8f,0.9f,this.bounds()), this);
        }
    }

    public void demoExample() throws Exception
    {
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()),-20,0.5f,true);
        playExampleAudio("DEMO",0);
        demoCapitalise();
        phraseIntro();
        showScreenLine(true);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.7f,this.bounds()),-20,0.5f,true);
        playExampleAudio("DEMO",1);
        theMoveSpeed = bounds().width();
        if(sentenceMode)
        {
            PointF pt = OB_Maths.locationForRect(0.5f,0.5f,screenLine.frame());
            pt.y += applyGraphicScale(20);
            movePointerToPoint(pt,-30,0.5f,true);
            playExampleAudio("DEMO2",0);
            demoTouchCurrentKey(true);
            movePointerToPoint(OB_Maths.locationForRect(0.9f,0.7f,this.bounds()),-30,0.5f,true);
            playExampleAudio("DEMO2",1);
            waitForSecs(0.3f);
            capitaliseKeyboard(false);
            waitForSecs(1f);
            while(currentWordIndex < currentReadingWords.size()-1)
            {
                pointerTypeCurrentWord();
            }
            pt = OB_Maths.locationForRect(0.5f,0.5f,screenLine.frame());
            pt.y += applyGraphicScale(20);
            movePointerToPoint(pt,-30,0.5f,true);
            playExampleAudio("DEMO2",2);
            movePointerToPoint(OB_Maths.locationForRect(1.2f,0.5f,currentDemoKey().frame()),-35,-1,true);
            flashButton(currentDemoKey(),1);
            List<String> exAudio = getAudioForScene("example","DEMO2");
            if(exAudio.size() > 3)
                playAudio(exAudio.get(3));
            flashButton(currentDemoKey(),2);
            waitAudio();
            waitForSecs(0.3f);
            demoTouchCurrentKey(false);
        }
        else
        {
            if(!hiddenMode)
            {
                while(currentLetterIndex < currentWordData.size()-1)
                {
                    demoTouchCurrentKey(true);
                }
                PointF pt = OB_Maths.locationForRect(0.5f,0.5f,screenLine.frame());
                pt.y += applyGraphicScale(20);
                movePointerToPoint(pt,-30,0.5f,true);
                playExampleAudio("DEMO2",0);
                movePointerToPoint(OB_Maths.locationForRect(1.02f,0.5f,currentDemoKey().frame()),-35,-1,true);
                flashButton(currentDemoKey(),1);
                playAudio(getAudioForScene("example","DEMO2") .get(1));
                flashButton(currentDemoKey(),2);
                waitAudio();
                waitForSecs(0.3f);
                demoTouchCurrentKey(true);
            }
            while(currentWordIndex < currentReadingWords.size())
            {
                pointerTypeCurrentWord();
            }
        }
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(1.5f);
        clearOff();
        waitForSecs(0.4f);
        lockScreen();
        setUpScene();
        unlockScreen();
        playExampleAudio("DEMO3",0);
    }

    public void playExampleAudio(String category,int index) throws Exception
    {
        playAudio(getAudioForScene("example",category) .get(index));
        waitAudio();
        waitForSecs(0.3f);
    }

    public void pointerTypeCurrentWord() throws Exception
    {
        int currentIndex = currentWordIndex;
        while(currentLetterIndex < currentWordData.size() && currentIndex == currentWordIndex)
        {
            demoTouchCurrentKey(true);
        }
    }

    public OBGroup currentDemoKey() throws Exception
    {
        Map<String,Object> letterDict = currentWordData.get(currentLetterIndex);
        return (OBGroup)letterDict.get("key");
    }

    public void demoTouchCurrentKey(boolean moveTo) throws Exception
    {
        boolean lastItem = currentWordIndex == currentReadingWords.size()-1 && currentLetterIndex == currentWordData.size() -1;
        OBGroup key = currentDemoKey();
        if(moveTo)        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.05f,key.frame()),-35,-1,true);
        movePointerToPoint(OB_Maths.locationForRect(0.5f,0.65f,key.frame()),-35,0.1f,true);
        lockScreen();
        lockScreen();
        typewriterManager.touchDownKey(key,true);
        highlightCurrentLetter(Color.BLACK);
        currentLetterIndex++;
        if(lastItem)
            screenLine.hide();

        unlockScreen();
        if(currentLetterIndex >= currentWordData.size())
        {
            currentWordIndex++;
            currentLetterIndex = 0;
            if(!lastItem)
                setupCurrentLabel();

        }
        if(!lastItem)
        {
            moveLineToCurrentLetter();
        }
        waitForAudio();
        waitForSecs(0.1f);
        typewriterManager.lowlightKey(key);
        movePointerToPoint(OB_Maths.locationForRect(0.6f,1.05f,key.frame()),-35,0.1f,true);
    }

    public void demoEvent1() throws Exception
    {
        if(currentMode == 3)
        {
            if(thePointer == null || thePointer.hidden)
                loadPointer(POINTER_LEFT);
            moveScenePointer(OB_Maths.locationForRect(0.9f,0.9f,this.bounds()) ,-20,0.5f,"DEMO",0,0.3f);
            moveScenePointer(OB_Maths.locationForRect(0.85f,0.8f,this.bounds()) ,-20,0.5f,"DEMO",1,0.3f);
            moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f,1.1f) , MainViewController() .topRightButton.frame),0,0.5f,"DEMO",2,0.5f);
            thePointer.hide();
        }
    }

    public void demoEvent1_2() throws Exception
    {
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()) ,-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(new PointF(0.5f,1.1f) , MainViewController() .topRightButton.frame),0,0.5f,"DEMO",1,0.5f);
        thePointer.hide();
    }

    public void demoTypeCapLetter() throws Exception
    {
        List<String> arrAudio = getAudioForScene(currentEvent() ,"CORRECT");
        if(currentMode == 1)
        {
            if(arrAudio != null)
            {
                waitForSecs(0.3f);
                loadPointer(POINTER_LEFT);
                PointF pt = OB_Maths.locationForRect(0f,0.5f,screenLine.frame());
                pt.y += applyGraphicScale(10);
                pt.x -= applyGraphicScale(10);
                moveScenePointer(pt,-30,0.5f,"CORRECT",0,0.3f);
                thePointer.hide();
            }
        }
        else
        {
            playAudioScene("CORRECT",0,true);
        }
        waitForSecs(0.3f);
        capitaliseKeyboard(false);
        if(arrAudio != null && arrAudio.size() > 1)
        {
            waitForSecs(0.3f);
            playAudioScene("CORRECT",1,true);
        }
    }

    public void demoTypeDot() throws Exception
    {
        if(currentMode == 1)
        {
            loadPointer(POINTER_LEFT);
            RectF rect = currentLabel.getWorldFrame();
            moveScenePointer(OB_Maths.locationForRect(new PointF(1.1f, 1.1f) , rect),-30,0.5f,"CORRECT2",0,0.3f);
            thePointer.hide();
        }
        else
        {
            playAudioQueuedScene("CORRECT2",0.3f,true);
        }
    }

}
