package org.onebillion.onecourse.mainui.oc_makingplurals;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBImage;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.generic.OC_Generic;
import org.onebillion.onecourse.mainui.generic.OC_Generic_Event;
import org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBImageManager;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.mainui.generic.OC_Generic.randomInt;
import static org.onebillion.onecourse.utils.OBUtils.RectOverlapRatio;

/**
 * Created by pedroloureiro on 24/04/2018.
 */

/*
 * SOUND EFFECTS
 *
 * buttonactive
 * buttontouch
 * imageon
 * wordon
 * wordpicoff
 * singular
 * plural
 * match
 * alloff
 * singpattern
 * pluralpattern
 * underline
 */

public class OC_MakingPlurals extends OC_Wordcontroller
{
    public static float INITIAL_DELAY = 10.0f;
    public static float FOLLOWING_DELAY = 6.0f;

    List<OBLabel> labels;
    Map<String, OBPhoneme> localDictionary;
    List<List<OBWord>> words_mode1;
    List<List<OBWord>> words_mode2;
    int colourTextNormal, colourTextMovable, colourTextSingular, colourTextPlural, colourTextHilited;
    int colourButtonNormal, colourButtonDisabled, colourButtonHilited;
    int colourSymbolNormal, colourSymbolHilited;
    int colourLine;
    List<Integer> colourRowArray;
    List<List<OBLabel>> labels_mode2_demo, labels_mode2;
    List<OBLabel> draggableLabels;
    List<OBPath> underlines;
    OBPath button;
    OBLabel labelSingular, labelPlural;
    OBPath boxSingular, boxPlural;
    OBImage imageSingular, imagePlural;
    OBControl symbolSingular;
    OBGroup symbolPlural;
    OBPresenter presenter;
    boolean wordsShowing;
    boolean goToPart2;
    boolean hasMode1;
    boolean alreadyPlayedReminder;
    String groupMode;
    OBConditionLock promptAudioLock;
    double lastActionTakenTimestamp;

    public void doAudio(String scene) throws Exception
    {
        setReplayAudio((List<Object>) (Object) getAudioForScene(this.currentEvent(), "REPEAT"));
        double timeStamp = OC_Generic.currentTime();
        lastActionTakenTimestamp = timeStamp;
        waitForSecs(0.9f);
        //
        if (lastActionTakenTimestamp == timeStamp)
        {
            promptAudioLock = playAudioQueuedScene("PROMPT", false);
            waitForSecs(0.5);
            //
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    waitAudioQueue(promptAudioLock);
                    lastActionTakenTimestamp = OC_Generic.currentTime();
                    alreadyPlayedReminder = false;
                    doReminderForEvent(currentEvent(), INITIAL_DELAY, new OC_Generic_Event.RunLambdaWithTimestamp()
                    {
                        @Override
                        public void run(double timestamp) throws Exception
                        {
                            doReminder(timestamp);
                        }
                    });
                }
            });
        }
    }


    public void miscSetUp()
    {
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restpos", OC_Generic.copyPoint(presenter.control.position()));
            presenter.control.setRight(0);
            presenter.control.show();
        }
        localDictionary = OBUtils.LoadWordComponentsXML(true);
        words_mode1 = extractWordsFromParameter("mode1");
        words_mode2 = extractWordsFromParameter("mode2");
        groupMode = parameters.get("group");
        colourTextNormal = objectDict.get("text_colour_normal").fillColor();
        colourTextMovable = objectDict.get("text_colour_movable").fillColor();
        colourTextSingular = objectDict.get("text_colour_singular").fillColor();
        colourTextPlural = objectDict.get("text_colour_plural").fillColor();
        colourTextHilited = objectDict.get("text_colour_hilited").fillColor();
        colourButtonNormal = objectDict.get("button_colour_normal").fillColor();
        colourButtonHilited = objectDict.get("button_colour_hilited").fillColor();
        colourButtonDisabled = objectDict.get("button_colour_disabled").fillColor();
        colourSymbolNormal = objectDict.get("symbol_colour_normal").fillColor();
        colourSymbolHilited = objectDict.get("symbol_colour_hilited").fillColor();
        colourLine = objectDict.get("line_colour").fillColor();
        colourRowArray = new ArrayList<>();
        for (OBPath rowColour : (List<OBPath>) (Object) filterControls("row_colour.*"))
        {
            colourRowArray.add(rowColour.fillColor());
        }
        hideControls(".*_colour.*");
        hideControls(".*_label");
        boxSingular = (OBPath) objectDict.get("singular_box");
        boxPlural = (OBPath) objectDict.get("plural_box");
        symbolSingular = objectDict.get("singular_symbol");
        symbolPlural = (OBGroup) objectDict.get("plural_symbol");
        hasMode1 = words_mode1.size() > 0;
        needDemo = parameters.get("demo") != null && parameters.get("demo").equals("true");
        this.currNo = 0;
        button = (OBPath) objectDict.get("button");
        button.disable();
    }


    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("master");
        miscSetUp();
        if (hasMode1)
        {
            loadAudioXML(getConfigPath("mp1audio.xml"));
            //
            int totalEventsPart1 = words_mode1.size() - 1;
            events = new ArrayList();
            events.addAll(Arrays.asList("c,d,e,f".split(",")));
            //
            while (events.size() < totalEventsPart1)
            {
                events.add(events.get(events.size() - 1));
            }
            while (events.size() > totalEventsPart1)
            {
                int lastIndex = events.size() - 1;
                events.remove(lastIndex);
            }
            events.add("g");
            events.add("h");
            events.add("i");
            events.add("j");
            if (needDemo)
            {
                events.add(0, "b");
            }
        }
        else
        {
            loadAudioXML(getConfigPath("mp2audio.xml"));
            events = Arrays.asList("k,l".split(","));
        }
        doVisual(currentEvent());
    }


    public void doReminderForEvent(final String event, double reminderDelay, final OC_Generic_Event.RunLambdaWithTimestamp block) throws Exception
    {
        if (!this.currentEvent().equals(event))
        {
            return;
        }
        if (lastActionTakenTimestamp == -1)
        {
            return;
        }
        double timestamp = lastActionTakenTimestamp;
        double elapsed = OC_Generic.currentTime() - lastActionTakenTimestamp;
        if ((status() == STATUS_WAITING_FOR_DRAG || status() == STATUS_AWAITING_CLICK) && elapsed > reminderDelay)
        {
            if (block != null)
            {
                block.run(timestamp);
                lastActionTakenTimestamp = OC_Generic.currentTime();
            }
            OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            doReminderForEvent(event, FOLLOWING_DELAY, block);
                        }
                    });
                }
            });
        }
        else
        {
            OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                    {
                        public void run() throws Exception
                        {
                            doReminderForEvent(event, FOLLOWING_DELAY, block);
                        }
                    });
                }
            });
        }
    }

    public void doReminder(double timeStamp) throws Exception
    {
        if (Arrays.asList("c", "d", "e", "f", "g").contains(this.currentEvent()))
        {
            for (int i = 0; i < 3; i++)
            {
                lockScreen();
                button.setFillColor(colourButtonHilited);
                unlockScreen();
                waitForSecs(0.2f);
                if (_aborting)
                {
                    return;
                }
                if (lastActionTakenTimestamp != timeStamp)
                {
                    break;
                }
                lockScreen();
                button.setFillColor(colourButtonNormal);

                unlockScreen();
                waitForSecs(0.2f);
                if (_aborting)
                {
                    return;
                }
                if (lastActionTakenTimestamp != timeStamp)
                {
                    break;
                }
            }
            lockScreen();
            button.setFillColor(colourButtonNormal);
            unlockScreen();
        }
        else
        {
            if (!alreadyPlayedReminder)
            {
                playAudioQueuedScene("REMINDER", 0.3f, false); // Slide the words to their correct places.;
                alreadyPlayedReminder = true;
            }
            List<OBLabel> labels = new ArrayList<>();
            if (draggableLabels != null)
            {
                for (OBLabel label : draggableLabels)
                {
                    if (label.isEnabled())
                    {
                        labels.add(label);
                    }
                }
            }
            for (int i = 0; i < 3; i++)
            {
                lockScreen();
                for (OBLabel label : labels)
                {
                    label.setOpacity(0.25f);
                }
                unlockScreen();
                waitForSecs(0.2f);
                //
                if (_aborting)
                {
                    return;
                }
                if (lastActionTakenTimestamp != timeStamp)
                {
                    break;
                }
                lockScreen();
                for (OBLabel label : labels)
                {
                    label.setOpacity(1.0f);
                }
                unlockScreen();
                waitForSecs(0.2f);
                //
                if (_aborting)
                {
                    return;
                }
                if (lastActionTakenTimestamp != timeStamp)
                {
                    break;
                }
            }
            //
            lockScreen();
            for (OBLabel label : labels)
            {
                label.setOpacity(1.0f);
            }
            unlockScreen();
            //
            while (!_aborting && OBAudioManager.audioManager.isPlaying())
            {
                waitForSecs(0.1f);
            }
        }
    }

    public void doMainXX() throws Exception
    {
        if (!performSel("demo", currentEvent()))
        {
            setStatus(STATUS_AWAITING_CLICK);
            waitForSecs(0.6f);
            doAudio(currentEvent());
        }
    }


    public List<List<OBWord>> extractWordsFromParameter(String parameterName)
    {
        List<List<OBWord>> result = new ArrayList<>();
        String ws = parameters.get(parameterName);
        if (ws == null) return result;
        //
        List<String> sets = Arrays.asList(ws.split(";"));
        for (String set : sets)
        {
            List<String> items = Arrays.asList(set.split(","));
            List<OBWord> words = new ArrayList<>();
            for (String item : items)
            {
                OBWord entry = (OBWord) localDictionary.get(item);
                if (entry == null)
                {
                    MainActivity.log("OC_MakingPlurals:extractWordsFromParameter: ERROR --> [%s] has no entry : the local dictionary. skipping", item);
                    continue;
                }
                words.add(entry);
            }
            result.add(words);
        }
        return result;
    }

    public void introWords() throws Exception
    {
        wordsShowing = true;
        List<OBWord> set = words_mode1.get(currNo);
        OBWord singularWord = set.get(0);
        OBWord pluralWord = set.get(set.size() - 1);
        //
        lockScreen();
        imageSingular = OBImageManager.sharedImageManager().imageForName(singularWord.imageName);
        imageSingular.setScale(0.8f * (boxSingular.width() / imageSingular.width()));
        attachControl(imageSingular);
        imageSingular.setPosition(boxSingular.position());
        imageSingular.setTop(imageSingular.top() - imageSingular.height() * 0.1f);
        imageSingular.setZPosition(30.0f);
        toggleSingularSymbol(true);
        unlockScreen();
        //
        playSfxAudio("imageon", false);
        waitForSecs(0.6f);
        //
        lockScreen();
        labelSingular = OC_Generic.action_createLabelForControl(objectDict.get("singular_label"), singularWord.text, 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        labelSingular.setZPosition(30.0f);
        attachControl(labelSingular);
        unlockScreen();
        //
        playSfxAudio("wordon", false);
        waitForSecs(0.3f);
        //
        lockScreen();
        labelSingular.setColour(colourTextHilited);
        unlockScreen();
        singularWord.playAudio(this, true);
        //
        lockScreen();
        labelSingular.setColour(colourTextNormal);
        unlockScreen();
        //
        waitForSecs(0.8f);
        //
        lockScreen();
        imagePlural = OBImageManager.sharedImageManager().imageForName(pluralWord.imageName);
        imagePlural.setScale(0.8f * (boxPlural.width() / imagePlural.width()));
        attachControl(imagePlural);
        imagePlural.setPosition(boxPlural.position());
        imagePlural.setTop(imagePlural.top() - imagePlural.height() * 0.1f);
        imagePlural.setZPosition(30.0f);
        toggleSingularSymbol(false);
        togglePluralSymbol(true);
        unlockScreen();
        //
        playSfxAudio("imageon", false);
        waitForSecs(0.6f);
        //
        lockScreen();
        labelPlural = OC_Generic.action_createLabelForControl(objectDict.get("plural_label"), pluralWord.text, 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
        labelPlural.setZPosition(30.0f);
        attachControl(labelPlural);
        unlockScreen();
        //
        playSfxAudio("wordon", false);
        waitForSecs(0.3f);
        //
        lockScreen();
        labelPlural.setColour(colourTextHilited);
        unlockScreen();
        //
        pluralWord.playAudio(this, true);
        //
        lockScreen();
        labelPlural.setColour(colourTextNormal);
        togglePluralSymbol(false);
        unlockScreen();
        //
        waitForSecs(0.4f);
    }


    public void exitWords(boolean fullClear) throws Exception
    {
        wordsShowing = false;
        //
        lockScreen();
        detachControl(labelPlural);
        detachControl(labelSingular);
        detachControl(imageSingular);
        detachControl(imagePlural);
        if (fullClear)
        {
            button.hide();
            boxSingular.hide();
            boxPlural.hide();
            if (underlines != null)
            {
                for (OBPath line : underlines)
                {
                    detachControl(line);
                }
            }
        }
        unlockScreen();
        //
        if (!fullClear)
        {
            playSfxAudio("wordpicoff", false);
        }
        waitForSecs(0.3f);
    }


    public void toggleSingularSymbol(boolean state)
    {
        lockScreen();
        symbolSingular.setFillColor(state ? colourSymbolHilited : colourSymbolNormal);
        unlockScreen();
    }


    public void togglePluralSymbol(boolean state)
    {
        lockScreen();
        for (OBPath symbol : (List<OBPath>) (Object) symbolPlural.members)
        {
            symbol.setFillColor(state ? colourSymbolHilited : colourSymbolNormal);
        }
        unlockScreen();
    }


    public void hiliteButton()
    {
        lockScreen();
        button.setFillColor(colourButtonHilited);
        button.setOpacity(1.0f);
        unlockScreen();
    }

    public void disableButton()
    {
        lockScreen();
        button.setFillColor(colourButtonDisabled);
        button.disable();
        unlockScreen();
    }

    public void enableButton()
    {
        lockScreen();
        button.setFillColor(colourButtonNormal);
        button.enable();
        unlockScreen();
    }


    public Object findButton(PointF pt)
    {
        OBControl c = finger(0, 2, (List<OBControl>) (Object) Arrays.asList(button), pt, true);
        return c;
    }

    public Object findLabel(PointF pt)
    {
        if (draggableLabels != null)
        {
            OBControl c = finger(0, 2, (List<OBControl>) (Object) draggableLabels, pt, true);
            return c;
        }
        return null;
    }

    public void checkButton() throws Exception
    {
        setStatus(STATUS_CHECKING);
        hidePointer();
        playSfxAudio("buttontouch", false);
        hiliteButton();
        waitForSecs(0.3f);
        //
        disableButton();
        if (wordsShowing)
        {
            exitWords(false);
            waitForSecs(0.3f);
        }
        introWords();
        waitForSecs(1.2f);
        //
        if (currentEvent().equals("g"))
        {
            waitForSecs(1.5f);
            //
            exitWords(true);
            waitForSecs(0.3f);
            //
            nextScene();
        }
        else
        {
            enableButton();
            playSfxAudio("buttonactive", false);
            lastActionTakenTimestamp = OC_Generic.currentTime();
            setStatus(STATUS_AWAITING_CLICK);
            currNo++;
            nextScene();
        }
    }


    public void checkDragAtPoint(PointF pt)
    {
        try
        {
            if (target == null)
            {
                return;
            }
            setStatus(STATUS_CHECKING);
            OBLabel label = (OBLabel) target;
            OBLabel otherLabel = findLabelUnderLabel(label);
            if (otherLabel == null)
            {
                PointF originalPosition = OC_Generic.copyPoint((PointF) label.propertyValue("original_position"));
                label.moveToPoint(originalPosition, 0.3f, true);
            }
            else
            {
                swapLabel(label, otherLabel);
                //
                lockScreen();
                RectF labelFrame = (RectF) label.propertyValue("original_frame");
                if (RectOverlapRatio(label.frame, labelFrame) >= 0.9)
                {
                    label.setColour(colourTextNormal);
                    label.disable();
                }
                RectF otherLabelFrame = (RectF) otherLabel.propertyValue("original_frame");
                if (RectOverlapRatio(otherLabel.frame, otherLabelFrame) >= 0.9)
                {
                    otherLabel.setColour(colourTextNormal);
                    otherLabel.disable();

                }
                unlockScreen();
                waitForSecs(0.3f);
                //
                boolean allPlaced = true;
                for (OBLabel draggableLabel : (List<OBLabel>) (Object) draggableLabels)
                {
                    if (draggableLabel.isEnabled())
                    {
                        allPlaced = false;
                    }
                }
                //
                if (allPlaced)
                {
                    gotItRightBigTick(true);
                    waitForSecs(0.3f);
                    //
                    nextScene();
                    return;
                }
            }
            lastActionTakenTimestamp = OC_Generic.currentTime();
            setStatus(STATUS_AWAITING_CLICK);
        } catch (Exception e)
        {
            MainActivity.log("OC_MakingPlurals:checkDragAtPoint exception caught: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            lastActionTakenTimestamp = OC_Generic.currentTime();
            Object obj = findButton(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton();
                    }
                });

            }
            else
            {
                obj = findLabel(pt);
                if (obj != null)
                {
                    checkDragTarget((OBControl) obj, pt);
                }
            }
        }
    }


    public void movePointerToRestingPosition(float time, boolean wait)
    {
        movePointerToPoint(new PointF(0.9f * bounds().width(), 0.9f * bounds().height()), time, wait);
    }

    public void hidePointer()
    {
        movePointerToPoint(new PointF(1.1f * bounds().width(), 1.1f * bounds().height()), 0, 0.6f, false);

    }

    public void swapLabel(OBLabel label1, OBLabel label2) throws Exception
    {
        PointF original1 = OC_Generic.copyPoint((PointF) label1.propertyValue("original_position"));
        PointF original2 = OC_Generic.copyPoint((PointF) label2.propertyValue("original_position"));
        //
        PointF newOriginal1 = OC_Generic.copyPoint(original2);
        newOriginal1.x = original1.x;
        //
        PointF newOriginal2 = OC_Generic.copyPoint(original1);
        newOriginal2.x = original2.x;
        //
        label1.setProperty("original_position", newOriginal1);
        label2.setProperty("original_position", newOriginal2);
        //
        label1.moveToPoint(newOriginal1, 0.3f, false);
        playSfxAudio("match", false);
        label2.moveToPoint(newOriginal2, 0.3f, true);
    }

    public OBLabel findLabelUnderLabel(OBLabel label)
    {
        OBLabel closestMatch = null;
        float bestOverlap = 0.0f;
        for (OBLabel possibleMatch : draggableLabels)
        {
            if (!possibleMatch.isEnabled())
            {
                continue;
            }
            if (possibleMatch.equals(label))
            {
                continue;
            }
            float overlap = RectOverlapRatio(label.frame, possibleMatch.frame);
            if (overlap > bestOverlap)
            {
                closestMatch = possibleMatch;
                bestOverlap = overlap;
            }
        }
        return closestMatch;
    }

    public boolean wereRequiredMultipleLettersForPlural(List<List<OBLabel>> words)
    {
        for (List<OBLabel> pair : words)
        {
            OBLabel plural = pair.get(pair.size() - 1);
            OBWord pluralWord = (OBWord) plural.propertyValue("word");
            int deltaFromRootPlural = (int) pluralWord.text.length() - (int) pluralWord.Root.length();
            if (deltaFromRootPlural > 1)
            {
                return true;
            }
        }
        return false;
    }


    public List<OBLabel> breakdownLabel(OBLabel mainLabel)
    {
        List<Float> lefts = new ArrayList<>();
        for (int i = 0; i < mainLabel.text().length(); i++)
        {
            float f = mainLabel.textOffset(i);
            lefts.add(f);
        }
        //
        List<OBLabel> labs = new ArrayList<>();
        for (int i = 0; i < mainLabel.text().length(); i++)
        {
            String text = mainLabel.text().substring(i, i+1);
            OBLabel l = new OBLabel(text, mainLabel.font().typeFace, mainLabel.fontSize());
            l.setColour(Color.BLACK);
            l.setPosition(mainLabel.position());
            l.setLeft(mainLabel.left() + lefts.get(i));
            l.setProperty("original_position", OC_Generic.copyPoint(l.position()));
            labs.add(l);
        }
        return labs;
    }


    public void colourLabel(OBLabel label, int startIdx, int length, int colour, int remainingColour)
    {
        if (remainingColour == -1)
        {
            remainingColour = Color.BLACK;
        }
        //
        lockScreen();
        label.setColour(remainingColour);
        label.setHighRange(startIdx, length, colour);
        unlockScreen();
    }

    public void demoPluralWords(List<List<OBLabel>> labels) throws Exception
    {
        for (List<OBLabel> pair : labels)
        {
            OBLabel plural = pair.get(pair.size() - 1);
            OBWord pluralWord = (OBWord) plural.propertyValue("word");
            String wordRoot = pluralWord.Root;
            if (wordRoot != null && wordRoot.length() > 0)
            {
                int index = pluralWord.text.indexOf(wordRoot);
                colourLabel(plural, index, wordRoot.length(), colourTextNormal, colourTextPlural);
            }
            else
            {
                plural.setColour(colourTextNormal);
            }
            playSfxAudio("pluralpattern", false);
            waitForSecs(0.2f);
        }
    }

    public void demoPluralWords_underlineAddedLetters(List<List<OBLabel>> labels) throws Exception
    {
        try
        {
            underlines = new ArrayList<>();
            for (List<OBLabel> pair : labels)
            {
                OBLabel singular = pair.get(0);
                OBWord singularWord = (OBWord) singular.propertyValue("word");
                OBLabel plural = pair.get(pair.size() - 1);
                String wordRoot = singularWord.text;
                //
                lockScreen();
                List<OBLabel> pluralBreakdown = breakdownLabel(plural);
                int index = plural.text().indexOf(wordRoot);
                PointF point1 = null;
                PointF point2 = null;
                int firstIndex, lastIndex;
                //
                for (firstIndex = 0; firstIndex < pluralBreakdown.size(); firstIndex++)
                {
                    if (firstIndex >= index && firstIndex < index + wordRoot.length())
                    {
                        continue;
                    }
                    OBLabel label = pluralBreakdown.get(firstIndex);
                    point1 = label.bottomLeft();
                    break;
                }
                //
                for (lastIndex = firstIndex; lastIndex < pluralBreakdown.size(); lastIndex++)
                {
                    if (lastIndex >= index && lastIndex < index + wordRoot.length())
                    {
                        break;
                    }
                    OBLabel label = pluralBreakdown.get(lastIndex);
                    point2 = label.bottomRight();
                }
                float nudge = plural.height() * 0.1f;
                point1.y -= nudge;
                point2.y -= nudge;
                OBPath underline = new OBPath(point1, point2);
                underline.setLineWidth(applyGraphicScale(3.0f));
                underline.setStrokeColor(colourLine);
                underline.setZPosition(31.0f);
                underline.sizeToBoundingBoxIncludingStroke();
                attachControl(underline);
                underlines.add(underline);
                unlockScreen();
                //
                playSfxAudio("underline", false);
                waitForSecs(0.5f);
            }
        }
        catch (Exception e)
        {
            MainActivity.log("Exception caught: " + e.toString());
            e.printStackTrace();
        }
    }

    public void demoPluralWords_underlineChangedLetters(List<List<OBLabel>> labels) throws Exception
    {
        underlines = new ArrayList<>();
        for (List<OBLabel> pair : labels)
        {
            OBLabel singular = pair.get(0);
            OBWord singularWord = (OBWord) singular.propertyValue("word");
            OBLabel plural = pair.get(pair.size() - 1);
            OBWord pluralWord = (OBWord) plural.propertyValue("word");
            String wordRoot = singularWord.Root;
            //
            lockScreen();
            List<OBLabel> singularBreakdown = breakdownLabel(singular);
            int index = singular.text().indexOf(wordRoot);
            PointF point1 = null;
            PointF point2 = null;
            int firstIndex, lastIndex;
            //
            for (firstIndex = 0; firstIndex < singularBreakdown.size(); firstIndex++)
            {
                if (firstIndex >= index && firstIndex < index + wordRoot.length())
                {
                    continue;
                }
                //
                OBLabel label = singularBreakdown.get(firstIndex);
                point1 = label.bottomLeft();
                break;
            }
            for (lastIndex = firstIndex; lastIndex < singularBreakdown.size(); lastIndex++)
            {
                if (lastIndex >= index && lastIndex < index + wordRoot.length())
                {
                    break;
                }
                OBLabel label = singularBreakdown.get(lastIndex);
                point2 = label.bottomRight();
            }
            //
            float nudge = singular.height() * 0.1f;
            point1.y -= nudge;
            point2.y -= nudge;
            //
            OBPath underline = new OBPath(point1, point2);
            underline.setLineWidth(applyGraphicScale(3.0f));
            underline.setStrokeColor(colourLine);
            underline.setZPosition(31.0f);
            attachControl(underline);
            underlines.add(underline);
            unlockScreen();
            //
            playSfxAudio("underline", false);
            waitForSecs(0.2f);
            //
            wordRoot = pluralWord.Root;
            lockScreen();
            List<OBLabel> pluralBreakdown = breakdownLabel(plural);
            //
            index = plural.text().indexOf(wordRoot);
            for (firstIndex = 0; firstIndex < pluralBreakdown.size(); firstIndex++)
            {
                if (firstIndex >= index && firstIndex < index + wordRoot.length())
                {
                    continue;
                }
                OBLabel label = pluralBreakdown.get(firstIndex);
                point1 = label.bottomLeft();
                break;
            }
            for (lastIndex = firstIndex; lastIndex < pluralBreakdown.size(); lastIndex++)
            {
                if (lastIndex >= index && lastIndex < index + wordRoot.length())
                {
                    break;
                }
                OBLabel label = pluralBreakdown.get(lastIndex);
                point2 = label.bottomRight();

            }
            nudge = plural.height() * 0.1f;
            point1.y -= nudge;
            point2.y -= nudge;
            underline = new OBPath(point1, point2);
            underline.setLineWidth(applyGraphicScale(3.0f));
            underline.setStrokeColor(colourLine);
            underline.setZPosition(31.0f);
            attachControl(underline);
            underlines.add(underline);
            unlockScreen();
            //
            playSfxAudio("underline", false);
            waitForSecs(0.5f);
        }
    }

    public void demoSingularWords(List<List<OBLabel>> labels) throws Exception
    {
        for (List<OBLabel> pair : labels)
        {
            OBLabel singular = pair.get(0);
            OBWord singularWord = (OBWord) singular.propertyValue("word");
            String wordRoot = singularWord.Root;
            if (wordRoot != null && wordRoot.length() > 0)
            {
                int index = singularWord.text.indexOf(wordRoot);
                colourLabel(singular, index, wordRoot.length(), colourTextNormal, colourTextSingular);
            }
            else
            {
                singular.setColour(colourTextNormal);
            }
            playSfxAudio("singpattern", false);
            waitForSecs(0.2f);
        }
    }


    public void demob() throws Exception
    {
        setStatus(STATUS_BUSY);
        List<String> audio = currentAudio("DEMO");
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        //
        presenter.speak((List<Object>) (Object) Arrays.asList(audio.get(0)), this);     // Now let’s see how a word changes when there’s more than one of a thing.;
        waitForSecs(0.2f);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        PointF destPos = new PointF(currPos.x + bounds().width() * 0.3f, currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3f);
        //
        presenter.speak((List<Object>) (Object) Arrays.asList(audio.get(1)), this);     // Are you ready?;
        waitForSecs(0.2f);
        //
        currPos = presenter.control.position();
        destPos = new PointF(currPos.x + bounds().width() * 0.3f, currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3f);
        //
        nextScene();
    }

    public void democ() throws Exception
    {
        setStatus(STATUS_BUSY);
        //
        loadPointer(POINTER_MIDDLE);
        PointF destination = OC_Generic.copyPoint(symbolSingular.position());
        destination.y += symbolSingular.height();
        //
        toggleSingularSymbol(true);
        playAudioScene("DEMO", 0, false);       // Look. One dot.
        movePointerToPoint(destination, 0.6f, true);
        waitForAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 1, false);       // Singular.
        waitForAudio();
        toggleSingularSymbol(false);
        waitForSecs(0.3f);
        //
        destination = OC_Generic.copyPoint(symbolPlural.position());
        destination.y += symbolPlural.height();
        movePointerToPoint(destination, 0.6f, true);
        togglePluralSymbol(true);
        playAudioScene("DEMO", 2, false);       // More than one dot.
        waitForAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false);       // Plural.
        waitForAudio();
        togglePluralSymbol(false);
        hidePointer();
        waitForSecs(1.5f);
        //
        doAudio(currentEvent());                // no prompt, just replay audio;
        setStatus(STATUS_AWAITING_CLICK);
        enableButton();
        playSfxAudio("buttonactive", false);
        final long timeStamp = statusTime;
        //
        OBUtils.runOnOtherThreadDelayed(1.5f, new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        if (statusTime == timeStamp)
                        {
                            PointF destination = OC_Generic.copyPoint(button.position());
                            destination.x = button.right() + button.width() * 0.2f;
                            playAudioScene("DEMO", 4, false);             // Now touch the button.;
                            movePointerToPoint(destination, 0.0f, 0.6f, false);
                            waitAudio();
                            waitForSecs(0.3f);
                            //
                            hidePointer();
                        }
                    }
                });
            }
        });
    }


    public void setSceneh()
    {
        float labelHeight = objectDict.get("singular_label").height() * 1.3f;
        float totalHeight = labelHeight * words_mode1.size();
        float top = boxSingular.top();
        float bottom = bounds().height() - labelHeight * 0.5f;
        float availableHeight = (bottom - top) - totalHeight;
        int longestWord = 0;
        //
        for (List<OBWord> pair : words_mode2)
        {
            OBWord singularWord = pair.get(0);
            longestWord = Math.max(longestWord, (int) singularWord.text.length());
            OBWord pluralWord = pair.get(pair.size() - 1);
            longestWord = Math.max(longestWord, (int) pluralWord.text.length());
        }
        top += availableHeight * 0.5;
        OBControl singularLabelControl = objectDict.get("singular_label");
        OBControl pluralLabelControl = objectDict.get("plural_label");
        //
        labels_mode2_demo = new ArrayList<>();
        //
        float longestLabelSingular = 0;
        float longestLabelPlural = 0;
        for (List<OBWord> pair : words_mode1)
        {
            List<OBLabel> labels = new ArrayList<>();
            OBWord singularWord = pair.get(0);
            OBWord pluralWord = pair.get(pair.size() - 1);
            OBLabel singular = OC_Generic.action_createLabelForControl(singularLabelControl, singularWord.text, 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            singular.setZPosition(30.0f);
            singular.setTop(top);
            singular.setProperty("word", singularWord);
            longestLabelSingular = Math.max(longestLabelSingular, singular.width());
            attachControl(singular);
            singular.hide();
            labels.add(singular);
            OBLabel plural = OC_Generic.action_createLabelForControl(pluralLabelControl, pluralWord.text, 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            plural.setZPosition(30.0f);
            plural.setTop(top);
            plural.setProperty("word", pluralWord);
            longestLabelPlural = Math.max(longestLabelPlural, plural.width());
            attachControl(plural);
            plural.hide();
            labels.add(plural);
            top += labelHeight;
            labels_mode2_demo.add(labels);
        }
        for (List<OBLabel> pair : labels_mode2_demo)
        {
            OBLabel singular = pair.get(0);
            singular.setLeft(symbolSingular.position().x - longestLabelSingular * 0.5f);
            OBLabel plural = pair.get(pair.size() - 1);
            plural.setLeft(symbolPlural.position().x - longestLabelPlural * 0.5f);
        }
    }


    public void demoh() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, true);        // Here are those words again.;
        for (List<OBLabel> pair : labels_mode2_demo)
        {
            OBLabel singular = pair.get(0);
            OBLabel plural = pair.get(pair.size() - 1);
            //
            lockScreen();
            singular.show();
            plural.show();
            unlockScreen();
            playSfxAudio("wordon", false);
            waitForSecs(0.3f);
            //
            lockScreen();
            singular.setColour(colourTextHilited);
            toggleSingularSymbol(true);
            unlockScreen();
            //
            OBWord singularWord = (OBWord) singular.propertyValue("word");
            singularWord.playAudio(this, true);
            //
            lockScreen();
            singular.setColour(colourTextNormal);
            toggleSingularSymbol(false);
            unlockScreen();
            waitForSecs(0.3f);
            //
            lockScreen();
            plural.setColour(colourTextHilited);
            togglePluralSymbol(true);
            unlockScreen();
            //
            OBWord pluralWord = (OBWord) plural.propertyValue("word");
            pluralWord.playAudio(this, true);
            //
            lockScreen();
            plural.setColour(colourTextNormal);
            togglePluralSymbol(false);
            unlockScreen();
            waitForSecs(0.3f);
        }
        float right = 0;
        for (List<OBLabel> pair : labels_mode2_demo)
        {
            OBLabel label = pair.get(0);
            right = Math.max(right, label.right() + label.width() * 0.2f);
        }
        OBLabel firstLabel = labels_mode2_demo.get(0).get(0);
        PointF firstPoint = OC_Generic.copyPoint(firstLabel.position());
        firstPoint.x = right;
        //
        OBLabel lastLabel = labels_mode2_demo.get(labels_mode2_demo.size() - 1).get(0);
        PointF lastPoint = OC_Generic.copyPoint(lastLabel.position());
        lastPoint.x = right;
        //
        toggleSingularSymbol(true);
        movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
        playAudioScene("DEMO", 1, false);           // Remember. One of each thing.;
        movePointerToPoint(lastPoint, 0.0f, 1.8f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 2, false);           // Singular.
        waitAudio();
        toggleSingularSymbol(false);
        waitForSecs(0.3f);
        //
        right = 0;
        for (List<OBLabel> pair : labels_mode2_demo)
        {
            OBLabel label = pair.get(pair.size() - 1);
            right = Math.max(right, label.right() + label.width() * 0.2f);
        }
        List<OBLabel> firstPair = labels_mode2_demo.get(0);
        firstLabel = firstPair.get(firstPair.size() - 1);
        firstPoint = OC_Generic.copyPoint(firstLabel.position());
        firstPoint.x = right;
        //
        List<OBLabel> lastPair = labels_mode2_demo.get(labels_mode2_demo.size() - 1);
        lastLabel = lastPair.get(lastPair.size() - 1);
        lastPoint = OC_Generic.copyPoint(lastLabel.position());
        lastPoint.x = right;
        togglePluralSymbol(true);
        movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
        playAudioScene("DEMO", 3, false);           // More than one of each thing.
        movePointerToPoint(lastPoint, 0.0f, 1.8f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 4, false);           // Plural.
        waitAudio();
        togglePluralSymbol(false);
        waitForSecs(0.3f);
        //
        if (groupMode.equals("1"))
        {
            firstLabel = firstPair.get(0);
            firstPoint = firstLabel.topLeft();
            //
            lastLabel = lastPair.get(lastPair.size() - 1);
            lastPoint = lastLabel.bottomRight();
            //
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            playAudioScene("DEMO2", 0, false);      // For these words, the singular and plural are the same!
            movePointerToPoint(lastPoint, 0.0f, 2.4f, true);
            waitAudio();
        }
        else if (groupMode.equals("2"))
        {
            firstLabel = firstPair.get(0);
            firstPoint = firstLabel.topLeft();
            //
            lastLabel = lastPair.get(lastPair.size() - 1);
            lastPoint = lastLabel.bottomRight();
            //
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            playAudioScene("DEMO3", 0, false);      // For these words, the singular and plural are very different!
            movePointerToPoint(lastPoint, 0.0f, 2.4f, true);
            waitAudio();
            waitForSecs(0.3f);
            //
            playAudioScene("DEMO3", 1, false);      // You’ll get to know them.;
            waitAudio();
        }
        else if (groupMode.equals("3"))
        {
            firstPoint = OC_Generic.copyPoint(objectDict.get("plural_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2_demo);
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            playAudioScene("DEMO4", (addedMultipleLetters ? 1 : 0), false);     // To make the plurals, a letter is added.  |  To make the plurals, letters are added.;
            waitAudio();
            waitForSecs(0.3f);
            //
            demoPluralWords(labels_mode2_demo);
            waitForSecs(0.6f);
            //
            playAudioScene("DEMO4", 2, false);      // Look at the pattern.;
            waitAudio();
        }
        else if (groupMode.equals("4"))
        {
            firstPoint = OC_Generic.copyPoint(objectDict.get("singular_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO5", 0, false);      // Look at the pattern here.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoSingularWords(labels_mode2_demo);
            waitForSecs(0.3f);
            //
            firstPoint = OC_Generic.copyPoint(objectDict.get("plural_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO5", 1, false);      // And here.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoPluralWords(labels_mode2_demo);
            waitForSecs(0.3f);
            //
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2_demo);
            playAudioScene("DEMO5", (addedMultipleLetters ? 3 : 2), false);     // To make the plurals, this letter is added.  |   To make the plurals, these letters are added.;
            waitAudio();
            waitForSecs(0.6f);
            //
            demoPluralWords_underlineAddedLetters(labels_mode2_demo);
        }
        else if (groupMode.equals("5"))
        {
            firstPoint = OC_Generic.copyPoint(objectDict.get("singular_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO6", 0, false);      // Look at the pattern here.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoSingularWords(labels_mode2_demo);
            waitForSecs(0.3f);
            //
            firstPoint = OC_Generic.copyPoint(objectDict.get("plural_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO6", 1, false);      // And here.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoPluralWords(labels_mode2_demo);
            waitForSecs(0.3f);
            //
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2_demo);
            playAudioScene("DEMO6", (addedMultipleLetters ? 3 : 2), false);     // See how a letter changed, to make the plural.  |  See how letters changed, to make the plural.;
            waitAudio();
        }
        hidePointer();
        waitForSecs(2.5f);
        //
        lockScreen();
        for (List<OBLabel> pair : labels_mode2_demo)
        {
            for (OBLabel label : pair)
            {
                label.hide();
            }
        }
        if (underlines != null)
        {
            for (OBPath line : underlines)
            {
                line.hide();
            }
        }
        unlockScreen();
        //
        playSfxAudio("alloff", false);
        waitForSecs(0.3f);
        //
        nextScene();
    }


    public void setScenei()
    {
        hideControls("button");
        hideControls(".*_label");
        hideControls(".*_box");
        //
        float labelHeight = objectDict.get("singular_label").height() * 1.3f;
        float totalHeight = labelHeight * words_mode2.size();
        float top = boxSingular.top();
        float bottom = bounds().height() - labelHeight * 0.5f;
        float availableHeight = (bottom - top) - totalHeight;
        int longestWord = 0;
        //
        for (List<OBWord> pair : words_mode2)
        {
            OBWord singularWord = pair.get(0);
            longestWord = Math.max(longestWord, singularWord.text.length());
            OBWord pluralWord = pair.get(pair.size() - 1);
            longestWord = Math.max(longestWord, pluralWord.text.length());
        }
        top += availableHeight * 0.5f;
        OBControl singularLabelControl = objectDict.get("singular_label");
        OBControl pluralLabelControl = objectDict.get("plural_label");
        //
        labels_mode2 = new ArrayList<>();
        draggableLabels = new ArrayList<>();
        //
        List<Float> label_tops = new ArrayList<>();
        int rowColourIndex = 0;
        float longestLabelSingular = 0;
        float longestLabelPlural = 0;
        for (List<OBWord> pair : words_mode2)
        {
            List<OBLabel> labels = new ArrayList<>();
            OBWord singularWord = pair.get(0);
            OBWord pluralWord = pair.get(pair.size() - 1);
            OBLabel singular = OC_Generic.action_createLabelForControl(singularLabelControl, singularWord.text, 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            singular.setZPosition(30.0f);
            singular.setTop(top);
            singular.setProperty("word", singularWord);
            longestLabelSingular = Math.max(longestLabelSingular, singular.width());
            attachControl(singular);
            singular.hide();
            labels.add(singular);
            //
            OBLabel plural = OC_Generic.action_createLabelForControl(pluralLabelControl, pluralWord.text, 1.0f, false, OBUtils.standardTypeFace(), colourTextNormal, this);
            plural.setZPosition(30.0f);
            plural.setTop(top);
            plural.setProperty("word", pluralWord);
            longestLabelPlural = Math.max(longestLabelPlural, plural.width());
            attachControl(plural);
            plural.hide();
            labels.add(plural);
            //
            draggableLabels.add(plural);
            //
            label_tops.add(plural.top());
            top += labelHeight;
            labels_mode2.add(labels);
            OBControl bar = new OBControl();
            bar.setFillColor(colourRowArray.get(rowColourIndex));
            bar.setFrame(new RectF(0, singular.position().y - labelHeight * 0.5f, bounds().width(), labelHeight));
            attachControl(bar);
            bar.setZPosition(29.0f);
            rowColourIndex++;
        }
        //
        for (List<OBLabel> pair : labels_mode2)
        {
            OBLabel singular = pair.get(0);
            singular.setLeft(symbolSingular.position().x - longestLabelSingular * 0.5f);
            OBLabel plural = pair.get(pair.size() - 1);
            plural.setLeft(symbolPlural.position().x - longestLabelPlural * 0.5f);
            plural.setProperty("original_frame", OC_Generic.copyRectF(plural.frame()));
        }
        //
        while (true)
        {
            draggableLabels = OBUtils.randomlySortedArray(draggableLabels);
            boolean needsReshuffle = false;
            for (int i = 0; i < draggableLabels.size(); i++)
            {
                OBLabel label = draggableLabels.get(i);
                //
                for (int j = 0; j < labels_mode2.size(); j++)
                {
                    List<OBLabel> pair = labels_mode2.get(j);
                    OBLabel plural = pair.get(pair.size() - 1);
                    if (label.equals(plural) && i == j)
                    {
                        needsReshuffle = true;
                        break;
                    }
                }
            }
            if (needsReshuffle)
            {
                continue;
            }
            break;
        }
        //
        for (int i = 0; i < draggableLabels.size(); i++)
        {
            OBLabel label = draggableLabels.get(i);
            label.setTop(label_tops.get(i));
            label.setColour(colourTextMovable);
            label.setProperty("original_position", OC_Generic.copyPoint(label.position()));
            label.setProperty("original_left", label.left());
        }
    }

    public void demoi() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        playAudioScene("DEMO", 0, false);       // Now let’s try this.
        movePointerToRestingPosition(0.3f, true);
        waitAudio();
        waitForSecs(0.3f);
        //
        toggleSingularSymbol(true);
        for (List<OBLabel> pair : labels_mode2)
        {
            OBLabel singular = pair.get(0);
            lockScreen();
            singular.show();
            unlockScreen();
            //
            playSfxAudio("singular", false);
            waitForSecs(0.2f);
        }
        float right = 0;
        for (List<OBLabel> pair : labels_mode2)
        {
            OBLabel label = pair.get(0);
            right = Math.max(right, label.right() + label.width() * 0.2f);
        }
        //
        List<OBLabel> firstPair = labels_mode2.get(0);
        OBLabel firstLabel = firstPair.get(0);
        PointF firstPoint = OC_Generic.copyPoint(firstLabel.position());
        firstPoint.x = right;
        //
        List<OBLabel> lastPair = labels_mode2.get(labels_mode2.size() - 1);
        OBLabel lastLabel = lastPair.get(0);
        PointF lastPoint = OC_Generic.copyPoint(lastLabel.position());
        lastPoint.x = right;
        //
        movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
        playAudioScene("DEMO", 1, false);   // Singular.;
        movePointerToPoint(lastPoint, 0.0f, 1.8f, true);
        waitAudio();
        toggleSingularSymbol(false);
        waitForSecs(0.3f);
        //
        togglePluralSymbol(true);
        for (OBLabel label : draggableLabels)
        {
            lockScreen();
            label.show();
            unlockScreen();
            //
            playSfxAudio("plural", false);
            waitForSecs(0.2f);
        }
        right = 0;
        for (List<OBLabel> pair : labels_mode2)
        {
            OBLabel label = pair.get(pair.size() - 1);
            right = Math.max(right, label.right() + label.width() * 0.2f);
        }
        //
        firstPoint = OC_Generic.copyPoint(draggableLabels.get(0).position());
        firstPoint.x = right;
        lastPoint = OC_Generic.copyPoint(draggableLabels.get(draggableLabels.size() - 1).position());
        lastPoint.x = right;
        //
        movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
        playAudioScene("DEMO", 2, false);   // Plural.;
        movePointerToPoint(lastPoint, 0.0f, 1.8f, true);
        waitAudio();
        togglePluralSymbol(false);
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 3, false);   // But these words are : the wrong order!;
        for (int i = 0; i < 3; i++)
        {
            lockScreen();
            for (List<OBLabel> pair : labels_mode2)
            {
                OBLabel plural = pair.get(pair.size() - 1);
                plural.setOpacity(0.25f);
            }
            unlockScreen();
            waitForSecs(0.2f);
            //
            lockScreen();
            for (List<OBLabel> pair : labels_mode2)
            {
                OBLabel plural = pair.get(pair.size() - 1);
                plural.setOpacity(1.0f);
            }
            unlockScreen();
            waitForSecs(0.2f);
        }
        waitAudio();
        waitForSecs(0.3f);
        //
        playAudioScene("DEMO", 4, false);   // Watch me.;
        waitAudio();
        waitForSecs(0.3f);
        //
        int randomWordIndex = randomInt(0, draggableLabels.size() - 1);
        OBLabel label = draggableLabels.get(randomWordIndex);
        RectF frame = (RectF) label.propertyValue("original_frame");
        OBLabel otherLabel = null;
        float bestDistance = 10000;
        for (OBLabel possibleMatch : draggableLabels)
        {
            float delta = Math.abs(possibleMatch.position().y - frame.centerY());   // frame.origin.y --> might be a bug using centerY
            if (delta < bestDistance && possibleMatch != label)
            {
                bestDistance = delta;
                otherLabel = possibleMatch;
            }
        }
        movePointerToPoint(label.position(), 0.0f, 0.6f, true);
        OC_Generic.pointer_moveToPointWithObject(label, otherLabel.position(), 0.0f, 0.6f, true, this);
        swapLabel(label, otherLabel);
        //
        lockScreen();
        RectF labelFrame = (RectF) label.propertyValue("original_frame");
        if (RectOverlapRatio(label.frame, labelFrame) >= 0.9)
        {
            label.setColour(colourTextNormal);
            label.disable();
        }
        RectF otherLabelFrame = (RectF) otherLabel.propertyValue("original_frame");
        if (RectOverlapRatio(otherLabel.frame, otherLabelFrame) >= 0.9)
        {
            otherLabel.setColour(colourTextNormal);
            otherLabel.disable();
        }
        unlockScreen();
        //
        hidePointer();
        waitForSecs(1.2f);
        //
        playAudioScene("DEMO", 5, false);   // Your turn;
        //
        lockScreen();
        label.setColour(colourTextMovable);
        label.enable();
        otherLabel.setColour(colourTextMovable);
        otherLabel.enable();
        unlockScreen();
        //
        swapLabel(label, otherLabel);
        waitAudio();
        waitForSecs(0.3f);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void demoj() throws Exception
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        //
        movePointerToRestingPosition(0.3f, true);
        playAudioScene("DEMO", 0, false); // Good! They’re : order.;
        waitAudio();
        waitForSecs(0.9f);
        //
        for (List<OBLabel> pair : labels_mode2)
        {
            OBLabel singular = pair.get(0);
            OBLabel plural = pair.get(pair.size() - 1);
            //
            lockScreen();
            singular.setColour(colourTextHilited);
            toggleSingularSymbol(true);
            unlockScreen();
            //
            OBWord singularWord = (OBWord) singular.propertyValue("word");
            singularWord.playAudio(this, true);
            //
            lockScreen();
            singular.setColour(colourTextNormal);
            toggleSingularSymbol(false);
            unlockScreen();
            waitForSecs(0.3f);
            //
            lockScreen();
            plural.setColour(colourTextHilited);
            togglePluralSymbol(true);
            unlockScreen();
            //
            OBWord pluralWord = (OBWord) plural.propertyValue("word");
            pluralWord.playAudio(this, true);
            //
            lockScreen();
            plural.setColour(colourTextNormal);
            togglePluralSymbol(false);
            unlockScreen();
            waitForSecs(0.3f);
        }
        if (!groupMode.equals("0"))
        {
            playAudioScene("DEMO", 1, false);   // Now let's check!;
            waitAudio();
            waitForSecs(0.3f);
        }
        //
        List<OBLabel> firstPair = labels_mode2.get(0);
        List<OBLabel> lastPair = labels_mode2.get(labels_mode2.size() - 1);
        //
        if (groupMode.equals("1"))
        {
            OBLabel firstLabel = firstPair.get(0);
            PointF firstPoint = firstLabel.topLeft();
            OBLabel lastLabel = lastPair.get(lastPair.size() - 1);
            PointF lastPoint = lastLabel.bottomRight();
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            playAudioScene("DEMO2", 0, false);      // The singular and plural are the same.;
            movePointerToPoint(lastPoint, 0.0f, 2.4f, true);
            waitAudio();
        }
        else if (groupMode.equals("2"))
        {
            OBLabel firstLabel = firstPair.get(0);
            PointF firstPoint = firstLabel.topLeft();
            OBLabel lastLabel = lastPair.get(lastPair.size() - 1);
            PointF lastPoint = lastLabel.bottomRight();
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            playAudioScene("DEMO3", 0, false);      // The singular and plural are very different.;
            movePointerToPoint(lastPoint, 0.0f, 2.4f, true);
            waitAudio();
        }
        else if (groupMode.equals("3"))
        {
            PointF firstPoint = OC_Generic.copyPoint(objectDict.get("plural_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2);
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            playAudioScene("DEMO4", (addedMultipleLetters ? 1 : 0), false);     // The same letter is added here too. |  The same letters are added here too.;
            waitAudio();
            waitForSecs(0.3f);
            //
            demoPluralWords(labels_mode2);
            waitAudio();
        }
        else if (groupMode.equals("4"))
        {
            PointF firstPoint = OC_Generic.copyPoint(objectDict.get("singular_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO5", 0, false);          // These show the same pattern as before.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoSingularWords(labels_mode2);
            waitForSecs(0.3f);
            //
            firstPoint = OC_Generic.copyPoint(objectDict.get("plural_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO5", 1, false);          // So do these.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoPluralWords(labels_mode2);
            waitForSecs(0.3f);
            //
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2);
            playAudioScene("DEMO5", (addedMultipleLetters ? 3 : 2), false);         // This letter is added, to make the plural.  |  These letters are added, to make the plural.;
            waitAudio();
            waitForSecs(0.6f);
            //
            demoPluralWords_underlineAddedLetters(labels_mode2);
            waitForSecs(0.3f);
        }
        else if (groupMode.equals("5"))
        {
            PointF firstPoint = OC_Generic.copyPoint(objectDict.get("singular_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO6", 0, false);      // These show the same pattern as before.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoSingularWords(labels_mode2);
            waitForSecs(0.3f);
            //
            firstPoint = OC_Generic.copyPoint(objectDict.get("plural_label").position());
            firstPoint.y = bounds().height() * 0.9f;
            playAudioScene("DEMO6", 1, false);      // So do these.;
            movePointerToPoint(firstPoint, 0.0f, 0.6f, true);
            waitAudio();
            demoPluralWords(labels_mode2);
            waitForSecs(0.3f);
            //
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2);
            playAudioScene("DEMO6", (addedMultipleLetters ? 3 : 2), false);     // A letter changed, to make the plural.  |  Letters changed, to make the plural.;
            waitAudio();
            demoPluralWords_underlineChangedLetters(labels_mode2);
            waitForSecs(0.3f);
        }
        hidePointer();
        waitForSecs(2.5f);
        //
        playAudioQueuedScene("FINAL", 0.3f, true);
        waitForSecs(0.7f);
        //
        nextScene();
    }


    public void setScenek()
    {
        setScenei();
    }


    public void demok() throws Exception
    {
        demoi();
    }


    public void demol() throws Exception
    {
        demoj();
    }
}
