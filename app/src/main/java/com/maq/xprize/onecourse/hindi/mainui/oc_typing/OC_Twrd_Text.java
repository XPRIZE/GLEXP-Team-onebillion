package com.maq.xprize.onecourse.hindi.mainui.oc_typing;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.hindi.controls.OBControl;
import com.maq.xprize.onecourse.hindi.controls.OBGroup;
import com.maq.xprize.onecourse.hindi.controls.OBLabel;
import com.maq.xprize.onecourse.hindi.controls.OBPath;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;
import com.maq.xprize.onecourse.hindi.mainui.oc_reading.OC_Reading;
import com.maq.xprize.onecourse.hindi.utils.OBAnim;
import com.maq.xprize.onecourse.hindi.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.hindi.utils.OBConditionLock;
import com.maq.xprize.onecourse.hindi.utils.OBFont;
import com.maq.xprize.onecourse.hindi.utils.OBMisc;
import com.maq.xprize.onecourse.hindi.utils.OBUtils;
import com.maq.xprize.onecourse.hindi.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 12/06/2018.
 */

public class OC_Twrd_Text extends OC_Twrd
{
    public static int FEEDBACK_TICK=0, FEEDBACK_SFX=1, FEEDBACK_NONE=2;

    public List<Map<String,Object>> eventsData;
    public int currentEventIndex, currentLetterIndex;
    public boolean keyAudio, replayAudioOnWrong, replayAudioOnCorrect;
    // public List<OBLabel> letterLabels;
    public List<Map<String,Object>> currentWordData;
    public OBLabel screenLabel;
    public OBPath screenLine;
    public boolean imageMode;

    private int wrongCount, feedback;
    private int lowlightColour, lineColour;
    private boolean hiddenMode;
    private OBConditionLock animationLock;
    private String currentText, currentAudio;
    private OBPath picturebg;
    private OBControl picture;

    @Override
    public void prepare()
    {
        super.prepare();
        loadEvent("twrdword");
        picturebg = (OBPath)objectDict.get("image_box");
        picturebg.sizeToBoundingBoxIncludingStroke();
        screenLine = (OBPath)objectDict.get("line");
        screenLine.sizeToBoundingBoxIncludingStroke();
        lineColour = screenLine.strokeColor();
        screenLine.setZPosition(1);
        screenLine.setProperty("start_width",screenLine.width());
        lowlightColour = OBUtils.colorFromRGBString(eventAttributes.get("lowlight_colour"));
        imageMode = OBUtils.getBooleanValue(parameters.get("image"));
        keyAudio = false;
        replayAudioOnCorrect = true;
        eventsData = new ArrayList<>();
        currentEventIndex = 0;
    }

    public void loadCurrentImage(String imageName)
    {
        if(picture != null)
            detachControl(picture);
        picture = loadImageWithName(imageName,picturebg.position(),picturebg.bounds);
        if(picture != null)
        {
            picture.setScale(picturebg.bounds.width()/(picture.width()+applyGraphicScale(20)));
            picture.setRasterScale(picture.scale());
            picture.setRotation(picturebg.rotation);
            picture.setPosition(picturebg.position());
            picture.setZPosition(picturebg.zPosition()+1);
            picture.hide();
        }
    }

    public float getFontSize()
    {
        return applyGraphicScale(130);
    }

    public void loadEventTextAndLine(String eventText)
    {
        if(screenLabel != null)
            detachControl(screenLabel);
        OBControl box = objectDict.get(imageMode ? "word_box_image" : "word_box");
        List<OBLabel> allLetters = new ArrayList<>();
        float fontSize = getFontSize();
        OBFont font = new OBFont(OBUtils.standardTypeFace(),fontSize);
        //see if font fits : the box
        Map attributes = new HashMap();
        attributes.put("font",font);
        float textWidth = OC_Reading.WidthOfText(eventText,attributes,0);
        if(textWidth > box.width())
        {
            fontSize *= box.width()/textWidth;
        }
        font = new OBFont(OBUtils.standardTypeFace(),fontSize);
        screenLabel = new OBLabel(eventText, font);
        attachControl(screenLabel);
        screenLabel.setPosition(box.position());
        screenLabel.setZPosition(2);
        //split text into typing parts to avoid special characters that are not on the keyboard
        currentWordData = new ArrayList<>();
        for(int i=0; i< eventText.length(); i++)
        {
            String letter = eventText.substring(i, i+1);
            if(typewriterManager.keyboardKeys.containsKey(letter.toLowerCase()))
            {
                Map<String,Object> dict = new ArrayMap();
                dict.put("letter",letter);
                dict.put("display",letter);
                dict.put("index",i);
                dict.put("key",typewriterManager.keyboardKeys.get(letter.toLowerCase()));
                currentWordData.add(dict);
            }
            else if(currentWordData.size() > 0)
            {
                String lastDisplay = (String)currentWordData.get(currentWordData.size()-1).get("display");
                currentWordData.get(currentWordData.size()-1).put("display",String.format("%s%s",lastDisplay,letter));
            }
        }

        PointF loc = OBMisc.copyPoint(screenLabel.getWorldPosition());
        loc.y += OBUtils.getFontCapHeight(font.typeFace, font.size)/2.0f + screenLine.lineWidth();
        for(Map<String,Object> letterDict : currentWordData)
        {
            String letter = (String)letterDict.get("display");
            int index = (int)letterDict.get("index");
            RectF selectionRect = OBUtils.getBoundsForSelectionInLabel(index, index + letter.length(), screenLabel);
            letterDict.put("line_width", selectionRect.width());
            letterDict.put("line_loc", new PointF(selectionRect.centerX(),loc.y));
        }

        screenLine.setPosition((PointF) currentWordData.get(0).get("line_loc"));
        screenLine.setProperty("start_loc",OBMisc.copyPoint(screenLine.position()));

        colourAllLetters(Color.TRANSPARENT);
    }

    public void setCurrentTextEvent()
    {
        setTextEvent(currentEventIndex);
    }

    public void setTextEvent(int index)
    {
        currentLetterIndex =0;
        wrongCount =0;
        String nextAudio = (String)eventsData.get(index).get("audio");
        String nextText = (String)eventsData.get(index).get("text");
        hiddenMode = (boolean)eventsData.get(index).get("hidden");
        replayAudioOnWrong = (boolean)eventsData.get(index).get("replayWrong");
        feedback = (int)eventsData.get(index).get("feedback");
        if(!currentAudioSameAsIndex(index))
        {
            loadEventTextAndLine(nextText);
            if(imageMode && eventsData.get(index) .get("image") != null)
                loadCurrentImage((String)eventsData.get(index).get("image"));
        }
        if(hiddenMode)
        {
            colourAllLetters(Color.BLACK);
            hideWordLetters();
        }
        currentText = nextText;
        currentAudio = nextAudio;
    }

    public boolean currentAudioSameAsIndex(int index)
    {
        if(eventsData.size() <= index)
            return false;

        String nextAudio = (String)eventsData.get(index).get("audio");
        return currentAudio != null && currentAudio.equals(nextAudio);
    }

    public void demoKeyTouch(OBGroup key)
    {
        super.touchDownKey(key, !keyAudio);
        if(keyAudio)
            playLetterAudioForKey(key);
    }

    public void playLetterAudioForKey(OBGroup key)
    {
        playAudio(String.format("alph_%s",key.propertyValue("value")));
    }

    public void colourCurrentLetter(int colour)
    {
        int index = (int)currentWordData.get(currentLetterIndex).get("index");
        String display = (String)currentWordData.get(currentLetterIndex).get("display");
        screenLabel.setHighRange(0, index + display.length(), colour);
    }

    public void touchDownKey(final OBGroup key, boolean sound)
    {
        OBGroup targetKey = (OBGroup)currentWordData.get(currentLetterIndex).get("key");
        boolean correct = targetKey == key;
        lockScreen();
        super.touchDownKey(key, correct && !keyAudio);
        if(correct)
        {
            if(keyAudio)
                playLetterAudioForKey(key);
            colourCurrentLetter(Color.BLACK);
            if(currentLetterIndex == currentWordData.size()-1)
                screenLine.hide();
        }

        unlockScreen();
        if(correct && currentLetterIndex != currentWordData.size()-1)
            moveLineToNextLetter();
        if(correct)
        {
            nextLetter(key);
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
                    lockScreen();
                    typewriterManager.lowlightKey(key);
                    if (wrongCount == 1)
                        disableRowsOfKeyboard();
                    else
                        disableAllWrongKeys();

                    unlockScreen();
                    long time = typewriterManager.unlock();
                    waitSFX();
                    waitForSecs(0.3f);
                    if (replayAudioOnWrong && time == statusTime())
                        playCurrentAudio(false);
                }
            } );

        }

    }

    public void touchUpKey(OBGroup key)
    {
        super.touchUpKey(key);
        doCurrentReminder(typewriterManager.unlock());
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

    public String currentEvent()
    {
        return (String)eventsData.get(currentEventIndex).get("event");
    }


    public boolean nextLetter(final OBGroup key)
    {
        gotItRight();
        resetKeyboard(key);
        currentLetterIndex++;
        if(currentLetterIndex >= currentWordData.size())
        {
            typewriterManager.lock();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception {
                    waitForSecs(0.2f);
                    lockScreen();
                    typewriterManager.lowlightKey(key);
                    unlockScreen();
                    if (keyAudio)
                        waitAudio();
                    waitForSecs(0.3f);
                    if (replayAudioOnCorrect) {
                        highlightTextWithAudio();
                        lockScreen();
                        colourAllLetters(Color.BLACK);
                        unlockScreen();
                        waitForSecs(0.3f);
                    }
                    if (feedback == FEEDBACK_TICK) {
                        displayTick();

                    } else if (feedback == FEEDBACK_SFX) {
                        playSFX("correct");
                        waitSFX();

                    }
                    if (!performSel("demoFin", currentEvent()))
                        playAudioQueued(OBUtils.insertAudioInterval(getAudioForScene(currentEvent(), "FINAL"), 300),
                                true);
                    if (currentEventIndex != eventsData.size() - 1) {
                        waitForSecs(0.5f);
                        hideText();
                        waitForSecs(0.3f);
                    }
                    nextText();
                }
            });
            return false;
        }
        return true;
    }

    public void nextText() throws Exception
    {
        currentEventIndex++;
        if(currentEventIndex < eventsData.size())
        {
            lockScreen();
            setCurrentTextEvent();
            unlockScreen();
            startCurrentScene();
        }
        else
        {
            fin();
        }
    }

    public void startCurrentScene() throws Exception
    {
        if(!performSel("demo",currentEvent()))
        {
            standardStartDemo();
        }
        waitForSecs(0.3f);
        if(!hiddenMode)
        {
            highlightTextWithAudio();
            lowLightText();
        }
        else
        {
            playCurrentAudio(true);
        }
        waitForSecs(0.3f);
        showLine();
        waitForSecs(0.2f);
        startEvent();
    }

    public void standardStartDemo() throws Exception
    {
        if(!hiddenMode)
        {
            showText();
            waitForSecs(0.3f);
        }
        else if(imageMode)
        {
            showImage();
            waitForSecs(0.3f);
        }
        playAudioQueuedScene("DEMO",0.3f,true);
    }

    public void startEvent() throws Exception
    {
        String promptScene =  "PROMPT";
        String remindScene =  "REMIND";
        String repeatScene =  "PROMPT.REPEAT";
        List<Object> repeatAudio = audioWithMainWord(repeatScene);
        setReplayAudio(repeatAudio);
        long time = typewriterManager.unlock();
        playAudioQueuedScene(promptScene,0.3f,true);
        List<Object> remindAudio = audioWithMainWord(remindScene);
        doReminder(remindAudio,time);

    }
    public void doReminder(List<Object> audio, long time)
    {
        doReminder(audio,time,7);
    }

    public void doReminder(final List<Object> audio,final long time,final float delay)
    {
        if(audio != null)
        {
            OBUtils.runOnOtherThreadDelayed(delay,new OBUtils.RunLambda()
            {
                public void run() throws Exception {
                    try
                    {
                        if (!statusChanged(time))
                        {
                            playAudioQueued(audio);
                            for (int i = 0; i < 4 && !statusChanged(time); i++) {
                                screenLine.setStrokeColor(Color.RED);
                                if (statusChanged(time))
                                    break;
                                waitForSecs(0.2f);
                                screenLine.setStrokeColor(lineColour);
                                if (statusChanged(time))
                                    break;
                                waitForSecs(0.2f);
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
    }

    public void doCurrentReminder(long time)
    {
        List<Object> remindAudio = audioWithMainWord("REMIND");
        doReminder(remindAudio,time);
    }

    public List<Object> audioWithMainWord(String scene)
    {
        List<String> audio = getAudioForScene(currentEvent() ,scene);
        if(audio != null)
        {
            audio = new ArrayList<>(audio);
            audio.add(currentAudio);
            return OBUtils.insertAudioInterval(audio ,300);
        }
        else
        {
            return Arrays.asList((Object)currentAudio);
        }
    }

    public void resetWord()
    {
        lockScreen();
        currentLetterIndex = 0;
        colourAllLetters(Color.TRANSPARENT);
        unlockScreen();
    }

    public void colourAllLetters(int colour)
    {
        screenLabel.setHighRange(-1,-1, Color.BLACK);
        screenLabel.setColour(colour);
    }

    public void showText() throws Exception
    {
        lockScreen();
        colourAllLetters(Color.BLACK);
        if(imageMode)
        {
            picture.show();
            picturebg.show();
        }
        playSfxAudio("wordon",false);
        unlockScreen();
        waitSFX();
    }

    public void showImage() throws Exception
    {
        if(imageMode && picture != null && picture.hidden)
        {
            lockScreen();
            picture.show();
            picturebg.show();
            playSfxAudio("wordon",false);
            unlockScreen();
            waitSFX();
        }
    }

    public void hideWordLetters()
    {
        lockScreen();
        colourAllLetters(Color.TRANSPARENT);
        unlockScreen();
    }

    public void hideText() throws Exception
    {
        lockScreen();
        colourAllLetters(Color.TRANSPARENT);
        if(imageMode && picture != null && !currentAudioSameAsIndex(currentEventIndex+1))
        {
            picture.hide();
            picturebg.hide();
        }
        playSfxAudio("wordoff",false);
        unlockScreen();
        waitSFX();
    }

    public void lowLightText() throws Exception
    {
        lockScreen();
        colourAllLetters(lowlightColour);
        playSfxAudio("wordon",false);
        unlockScreen();
        waitSFX();
    }

    public void showLine() throws Exception
    {
        if(hiddenMode)
        {
            screenLine.setWidth((float)screenLine.propertyValue("start_width"));
        }
        else
        {
            screenLine.setWidth((float)currentWordData.get(currentLetterIndex).get("line_width"));
        }
        screenLine.setPosition((PointF)screenLine.propertyValue("start_loc"));
        screenLine.show();
        playSfxAudio("lineshow",true);
    }

    public void moveLineToNextLetter()
    {
        if(currentLetterIndex +1 >= currentWordData.size())
            return;
        deregisterAnimationGroupWithName("line_move");
        PointF loc = (PointF)currentWordData.get(currentLetterIndex+1).get("line_loc");
        float width = (float)currentWordData.get(currentLetterIndex+1).get("line_width");

        List<OBAnim> anims = new ArrayList<>();
        anims.add(OBAnim.moveAnim(loc, screenLine));
        if (!hiddenMode)
        {
            anims.add(OBAnim.propertyAnim("width", width, screenLine));
        }
        OBAnimationGroup animGroup = OBAnimationGroup.runAnims(anims, 0.2, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
        registerAnimationGroup(animGroup, "line_move");
    }

    public void disableRowsOfKeyboard() throws Exception
    {
        OBGroup key = (OBGroup)currentWordData.get(currentLetterIndex).get("key");
        typewriterManager.disableRowsSkip(key);
    }

    public void disableAllWrongKeys() throws Exception
    {
        OBGroup key = (OBGroup)currentWordData.get(currentLetterIndex).get("key");
        typewriterManager.disableAllSkip(key);
    }

    public void highlightTextWithAudio() throws Exception
    {
        lockScreen();
        colourAllLetters(Color.RED);
        unlockScreen();
        playCurrentAudio(true);
        waitForSecs(0.6f);
    }

    public void playCurrentAudio(boolean wait) throws Exception
    {
        if(currentAudio != null)
        {
            playAudio(currentAudio);
            if(wait)
                waitAudio();
        }
    }

    public void demoTypeLabels() throws Exception
    {
        float moveTime = 0.5f;
        for(Map<String,Object> letterDict : currentWordData)
        {
            OBGroup key = (OBGroup)letterDict.get("key");
            movePointerToPoint(OB_Maths.locationForRect(0.7f,1.05f,key.frame()),-35,moveTime,true);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.65f,key.frame()),-35,0.2f,true);

            lockScreen();
            demoKeyTouch(key);
            colourCurrentLetter(Color.BLACK);
            if(currentLetterIndex >= currentWordData.size()-1)
                screenLine.hide();
            unlockScreen();

            moveLineToNextLetter();
            waitForAudio();
            currentLetterIndex++;
            typewriterManager.lowlightKey(key);
            movePointerToPoint(OB_Maths.locationForRect(0.7f,1.05f,key.frame()),-35,0.2f,true);
            moveTime = 0.3f;
        }
    }

}
