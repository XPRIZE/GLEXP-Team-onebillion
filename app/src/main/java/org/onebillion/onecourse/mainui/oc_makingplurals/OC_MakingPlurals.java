package org.onebillion.onecourse.mainui.oc_makingplurals;

import android.graphics.Color;

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
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    List<OBLabel> labels_mode2_demo, labels_mode2;
    List<OBLabel> draggableLabels;
    List<OBPath> underlines;
    OBGroup button;
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
    long lastActionTakenTimestamp;

    public void doAudio(String scene) throws Exception
    {
        setReplayAudio((List<Object>) (Object) getAudioForScene(this.currentEvent(), "REPEAT"));
        long timeStamp = System.currentTimeMillis();
        lastActionTakenTimestamp = timeStamp;
        waitForSecs(0.9f);
        if (lastActionTakenTimestamp == timeStamp)
        {
            promptAudioLock = playAudioQueuedScene("PROMPT", false);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    waitAudioQueue(promptAudioLock);
                    lastActionTakenTimestamp = System.currentTimeMillis();
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
        needDemo = parameters.get("demo").equals("true");
        this.currNo = 0;
        button = (OBGroup) objectDict.get("button");
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
            int totalEventsPart1 = (int) words_mode1.size() - 1;
            events = Arrays.asList("c,d,e,f".split(","));
            //
            while (events.size() < totalEventsPart1)
            {
                events.add(events.get(events.size() - 1));
            }
            while (events.size() > totalEventsPart1)
            {
                events.remove(events.get(events.size() - 1));
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
        long timestamp = lastActionTakenTimestamp;
        double elapsed = System.currentTimeMillis() - lastActionTakenTimestamp;
        if ((status() == STATUS_WAITING_FOR_DRAG || status() == STATUS_AWAITING_CLICK) && elapsed > reminderDelay)
        {
            if (block != null)
            {
                block.run(timestamp);
                lastActionTakenTimestamp = System.currentTimeMillis();
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
                playAudioQueuedScene("REMINDER", 300, false); // Slide the words to their correct places.;
                alreadyPlayedReminder = true;
            }
            List<OBLabel> labels = new ArrayList<>();
            for (OBLabel label : draggableLabels)
            {
                if (label.isEnabled())
                {
                    labels.add(label);
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


    public List<List<OBWord>>  extractWordsFromParameter(String  parameterName)
    {
        List<List<OBWord>> result = new ArrayList<>();
        String ws = parameters.get(parameterName);
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
                    MainActivity.log("OC_MakingPlurals:extractWordsFromParameter: ERROR --> %s.() has no entry : the local dictionary. skipping", item);
                    continue;
                }
                words.add(entry);
            }
            result.add(words);
        }
        return result;
    }


    /*



    public void introWords()
    {
        wordsShowing = true;
        List<> set = words_mode1.objectAtIndex(currNo);
        OBWord singularWord = set.firstObject;
        OBWord pluralWord = set.lastObject;
        lockScreen();
        imageSingular = OBImageManager.sharedImageManager.imageForName(singularWord.imageName);
        imageSingular.setScale ( 0.8 * (boxSingular.width / imageSingular.width));
        attachControl(imageSingular);
        imageSingular.setPosition(boxSingular.position);
        imageSingular.top -= imageSingular.height * 0.1;
        imageSingular.setZPosition ( 30.0);
        toggleSingularSymbol(true);

        unlockScreen();
        playSfxAudio("imageon",false);
        waitForSecs(0.6f);
        lockScreen();
        labelSingular = createLabel_simple([objectDict objectForKey:"singular_label")  text:singularWord.text colour:colourTextNormal];
        labelSingular.setZPosition ( 30.0);
        attachControl(labelSingular);

        unlockScreen();
        playSfxAudio("wordon",false);
        waitForSecs(0.3f);
        lockScreen();
        labelSingular.setColour ( colourTextHilited);

        unlockScreen();
    [singularWord playAudio:wait:true];
        lockScreen();
        labelSingular.setColour ( colourTextNormal);

        unlockScreen();
        waitForSecs(0.8f);
        lockScreen();
        imagePlural = OBImageManager.sharedImageManager.imageForName(pluralWord.imageName);
        imagePlural.setScale ( 0.8 * (boxPlural.width / imagePlural.width));
        attachControl(imagePlural);
        imagePlural.setPosition(boxPlural.position);
        imagePlural.top -= imagePlural.height * 0.1;
        imagePlural.setZPosition ( 30.0);
        toggleSingularSymbol(false);
        togglePluralSymbol(true);

        unlockScreen();
        playSfxAudio("imageon",false);
        waitForSecs(0.6f);
        lockScreen();
        labelPlural = createLabel_simple([objectDict objectForKey:"plural_label")  text:pluralWord.text colour:colourTextNormal];
        labelPlural.setZPosition ( 30.0);
        attachControl(labelPlural);

        unlockScreen();
        playSfxAudio("wordon",false);
        waitForSecs(0.3f);
        lockScreen();
        labelPlural.setColour ( colourTextHilited);

        unlockScreen();
    [pluralWord playAudio:wait:true];
        lockScreen();
        labelPlural.setColour ( colourTextNormal);
        togglePluralSymbol(false);

        unlockScreen();
        waitForSecs(0.4f);

    }
    public void exitWords(boolean fullClear)
    {
        wordsShowing = false;
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
        if (!fullClear)
        {
            playSfxAudio("wordpicoff",false);

        }
        waitForSecs(0.3f);

    }
    public void colourLabel(OBLabel  label inRange:(NSRange) range, Color  colour, Color  remainingColour)
    {
        if (remainingColour == null)
        {
            remainingColour = Color.blackColor;

        }
        lockScreen();
        NSMutableAttributedString mastr;
        Object s = label.string();
        if (![s isKindOfClass:NSAttributedString.class()])
        {
            s = [NSAttributedString.alloc() initWithString:s attributes:@
            {
                NSFontAttributeName:label.font
            }
];

        }
        mastr = [NSMutableAttributedString.alloc() initWithAttributedString:s];
        Map<> *attributes = @
        {
            NSForegroundColorAttributeName:(id)remainingColour.CGColor
        }
        ;
        [mastr addAttributes:attributes range:NSMakeRange(0, mastr.string.length)];
        if (colour)
        {
            Map<> *attributes = @
            {
                NSForegroundColorAttributeName:(id)colour.CGColor
            }
            ;
            [mastr addAttributes:attributes range:range];

        }
        label.setString ( (String*)mastr);

        unlockScreen();

    }
    public void toggleSingularSymbol(boolean state)
    {
        lockScreen();
        symbolSingular.setFillColor ( state ? colourSymbolHilited : colourSymbolNormal);

        unlockScreen();

    }
    public void togglePluralSymbol(boolean state)
    {
        lockScreen();
        for (OBPath symbol : symbolPlural.members)
        {
            symbol.setFillColor ( state ? colourSymbolHilited : colourSymbolNormal);

        }

        unlockScreen();

    }
    public void hiliteButton()
    {
        lockScreen();
        button.setFillColor ( colourButtonHilited);
        button.setOpacity(1.0);

        unlockScreen();

    }
    public void disableButton()
    {
        lockScreen();
        button.setFillColor ( colourButtonDisabled);
        button.disable();

        unlockScreen();

    }
    public void enableButton()
    {
        lockScreen();
        button.setFillColor ( colourButtonNormal);
        button.enable();

        unlockScreen();

    }

    public Object findButton(PointF pt)
    {
        OBControl c = finger(0,2,Arrays.asList(button),pt, true);
        return c;

    }
    public Object findLabel(PointF pt)
    {
        OBLabel c = finger(0,2,draggableLabels,pt, true);
        return c;

    }
    public void checkButton()
    {
        setStatus(STATUS_CHECKING);
        hidePointer();
        playSfxAudio("buttontouch",false);
        hiliteButton();
        waitForSecs(0.3f);
        disableButton();
        if (wordsShowing)
        {
            exitWords(false);
            waitForSecs(0.3f);

        }
        introWords();
        waitForSecs(1.2f);
        if (currentEvent.equals("g"))
        {
            waitForSecs(1.5f);
            exitWords(true);
            waitForSecs(0.3f);
            nextScene();

        }
        else
        {
            enableButton();
            playSfxAudio("buttonactive",false);
            setLastActionTakenTimestamp ( [NSDate.date() timeIntervalSince1970]);
            setStatus(STATUS_AWAITING_CLICK);
            currNo++;
            nextScene();

        }

    }
    public void checkDragAtPoint(PointF pt)
    {
        if (target = = null) return;
        setStatus(STATUS_CHECKING);
        OBLabel label = (OBLabel ) target;
        OBLabel otherLabel = findLabelUnderLabel(label);
        if (otherLabel == null)
        {
            PointF originalPosition = OC_Generic.copyPoint(label.propertyValue("original.position())") ;
        [label moveToPoint:originalPosition time:0.3 wait:true];

        }
        else
        {
            swapLabel(label withLabel:otherLabel);
            lockScreen();
            RectF labelFrame = label.propertyValue("original_frame").RectFValue();
            if (RectOverlapRatio(label.frame, labelFrame) >= 0.9)
            {
                label.setColour ( colourTextNormal);
                label.disable();

            }
            RectF otherLabelFrame = otherLabel.propertyValue("original_frame").RectFValue();
            if (RectOverlapRatio(otherLabel.frame, otherLabelFrame) >= 0.9)
            {
                otherLabel.setColour ( colourTextNormal);
                otherLabel.disable();

            }

            unlockScreen();
            waitForSecs(0.3f);
            boolean allPlaced = true;
            for (OBLabel label : draggableLabels)
            {
                if (label.setIsEnabled) allPlaced ( false);

            }
            if (allPlaced)
            {
                gotItRightBigTick(true);
                waitForSecs(0.3f);
                nextScene();
                return;

            }

        }
        setLastActionTakenTimestamp ( [NSDate.date() timeIntervalSince1970]);
        setStatus(STATUS_AWAITING_CLICK);

    }
    public void touchDownAtPoint(PointF pt, View  v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            setLastActionTakenTimestamp ( [NSDate.date() timeIntervalSince1970]);
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
                    checkDragTarget(obj point:pt);

                }

            }

        }

    }
    public void demob()
    {
        setStatus(STATUS_BUSY);
        List<> audio = currentAudio("DEMO");
        presenter walk:presenter.control.settings.get("restpos").();
        presenter.faceFront();
        waitForSecs(0.2f);
    [presenter speak:Arrays.asList(audio.get(0)) controller:;
        // Now let’s see how a word changes when there’s more than one of a thing.;
        waitForSecs(0.2f);
        PointF currPos = OC_Generic.copyPoint(presenter.control.position());
        PointF destPos = new PointF(currPos.x + bounds().size.width * 0.3f,  currPos.yf);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3f);
    [presenter speak:Arrays.asList(audio.get(1)) controller:;
        // Are you ready?;
        waitForSecs(0.2f);
        currPos = presenter.control.position;
        destPos = new PointF(currPos.x + bounds().size.width * 0.3f,  currPos.yf);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3f);
        nextScene();

    }
    public void democ()
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        PointF destination = OC_Generic.copyPoint(symbolSingular.position());
        destination.y += symbolSingular.height;
        toggleSingularSymbol(true);
        playAudioScene("DEMO",0,false);
        // Look. One dot.;
        movePointerToPoint(destination,0.6f,true);
        waitForAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",1,false);
        // Singular.;
        waitForAudio();
        toggleSingularSymbol(false);
        waitForSecs(0.3f);
        destination = symbolPlural.position;
        destination.y += symbolPlural.height;
        movePointerToPoint(destination,0.6f,true);
        togglePluralSymbol(true);
        playAudioScene("DEMO",2,false);
        // More than one dot.;
        waitForAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",3,false);
        // Plural.;
        waitForAudio();
        togglePluralSymbol(false);
        hidePointer();
        waitForSecs(1.5f);
        doAudio(currentEvent);
        // no prompt, just replay audio;
        setStatus(STATUS_AWAITING_CLICK);
        enableButton();
        playSfxAudio("buttonactive",false);
        long timeStamp = statusTime;
        OBUtils.runOnOtherThreadDelayed(1.5,new OBUtils.RunLambda()
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
                            destination.setX ( button.right + button.width * 0.2);
                            playAudioScene("DEMO",4,false);
                            // Now touch the button.;
                            movePointerToPoint(destination,0.0,0.6f,false);
                            waitAudio();
                            waitForSecs(0.3f);
                            hidePointer();

                        }

                    }
                }
            });

        } ) ;

    }
    public void setSceneh()
    {
        float labelHeight = objectDict.objectForKey("singular_label").height * 1.3;
        float totalHeight = labelHeight * words_mode1.size();
        float top = boxSingular.top;
        float bottom = bounds().size.height - labelHeight * 0.5;
        float availableHeight = (bottom - top) - totalHeight;
        int longestWord = 0;
        for (List<> pair : words_mode2)
        {
            OBWord singularWord = pair.firstObject;
            longestWord = Math.max(longestWord, (int) singularWord.text.length);
            OBWord pluralWord = pair.lastObject;
            longestWord = Math.max(longestWord, (int) pluralWord.text.length);

        }
        top += availableHeight * 0.5;
        OBControl singularLabelControl = objectDict.objectForKey("singular_label");
        OBControl pluralLabelControl = objectDict.objectForKey("plural_label");
        labels_mode2_demo = new ArrayList<>();
        float longestLabelSingular = 0;
        float longestLabelPlural = 0;
        for (List<> pair : words_mode1)
        {
            List<> labels = new ArrayList<>();
            OBWord singularWord = pair.firstObject;
            OBWord pluralWord = pair.lastObject;
            OBLabel singular = createLabel_simple(singularLabelControl  text:singularWord.text colour:colourTextNormal);
            singular.setZPosition ( 30.0);
            singular.setTop ( top);
            singular.setProperty("word",singularWord);
            longestLabelSingular = Math.max(longestLabelSingular, singular.width);
            attachControl(singular);
            singular.hide();
            labels.add(singular);
            OBLabel plural = createLabel_simple(pluralLabelControl  text:pluralWord.text colour:colourTextNormal);
            plural.setZPosition ( 30.0);
            plural.setTop ( top);
            plural.setProperty("word",pluralWord);
            longestLabelPlural = Math.max(longestLabelPlural, plural.width);
            attachControl(plural);
            plural.hide();
            labels.add(plural);
            top += labelHeight;
            labels_mode2_demo.add(labels);

        }
        for (List<> pair : labels_mode2_demo)
        {
            OBLabel singular = pair.firstObject;
            singular.setLeft ( symbolSingular.position.x - longestLabelSingular * 0.5);
            OBLabel plural = pair.lastObject;
            plural.setLeft ( symbolPlural.position.x - longestLabelPlural * 0.5);

        }

    }
    public void demoh()
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO",0,true);
        // Here are those words again.;
        for (List<> pair : labels_mode2_demo)
        {
            OBLabel singular = pair.firstObject;
            OBLabel plural = pair.lastObject;
            lockScreen();
            singular.show();
            plural.show();

            unlockScreen();
            playSfxAudio("wordon",false);
            waitForSecs(0.3f);
            lockScreen();
            singular.setColour ( colourTextHilited);
            toggleSingularSymbol(true);

            unlockScreen();
            OBWord singularWord = singular.propertyValue("word");
        [singularWord playAudio:wait:true];
            lockScreen();
            singular.setColour ( colourTextNormal);
            toggleSingularSymbol(false);

            unlockScreen();
            waitForSecs(0.3f);
            lockScreen();
            plural.setColour ( colourTextHilited);
            togglePluralSymbol(true);

            unlockScreen();
            OBWord pluralWord = plural.propertyValue("word");
        [pluralWord playAudio:wait:true];
            lockScreen();
            plural.setColour ( colourTextNormal);
            togglePluralSymbol(false);

            unlockScreen();
            waitForSecs(0.3f);

        }
        float right = 0;
        for (List<> pair : labels_mode2_demo)
        {
            OBLabel label = pair.firstObject;
            right = Math.max(right, label.right + label.width * 0.2);

        }
        OBLabel firstLabel = ((List<> ) labels_mode2_demo.firstObject).firstObject;
        PointF firstPoint = OC_Generic.copyPoint(firstLabel.position());
        firstPoint.setX ( right);
        OBLabel lastLabel = ((List<> ) labels_mode2_demo.lastObject).firstObject;
        PointF lastPoint = OC_Generic.copyPoint(lastLabel.position());
        lastPoint.setX ( right);
        toggleSingularSymbol(true);
        movePointerToPoint(firstPoint,0.0,0.6f,true);
        playAudioScene("DEMO",1,false);
        // Remember. One of each thing.;
        movePointerToPoint(lastPoint,0.0,1.8f,true);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",2,false);
        // Singular.;
        waitAudio();
        toggleSingularSymbol(false);
        waitForSecs(0.3f);
        right = 0;
        for (List<> pair : labels_mode2_demo)
        {
            OBLabel label = pair.lastObject;
            right = Math.max(right, label.right + label.width * 0.2);

        }
        firstLabel = ((List<> ) labels_mode2_demo.firstObject).lastObject;
        firstPoint = firstLabel.position;
        firstPoint.setX ( right);
        lastLabel = ((List<> ) labels_mode2_demo.lastObject).lastObject;
        lastPoint = lastLabel.position;
        lastPoint.setX ( right);
        togglePluralSymbol(true);
        movePointerToPoint(firstPoint,0.0,0.6f,true);
        playAudioScene("DEMO",3,false);
        // More than one of each thing.;
        movePointerToPoint(lastPoint,0.0,1.8f,true);
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",4,false);
        // Plural.;
        waitAudio();
        togglePluralSymbol(false);
        waitForSecs(0.3f);
        if (groupMode.equals("1"))
        {
            firstLabel = ((List<> ) labels_mode2_demo.firstObject).firstObject;
            firstPoint = firstLabel.topLeft;
            lastLabel = ((List<> ) labels_mode2_demo.lastObject).lastObject;
            lastPoint = lastLabel.bottomRight;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            playAudioScene("DEMO2",0,false);
            // For these words, the singular and plural are the same!;
            movePointerToPoint(lastPoint,0.0,2.4f,true);
            waitAudio();

        }
        else if (groupMode.equals("2"))
        {
            firstLabel = ((List<> ) labels_mode2_demo.firstObject).firstObject;
            firstPoint = firstLabel.topLeft;
            lastLabel = ((List<> ) labels_mode2_demo.lastObject).lastObject;
            lastPoint = lastLabel.bottomRight;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            playAudioScene("DEMO3",0,false);
            // For these words, the singular and plural are very different!;
            movePointerToPoint(lastPoint,0.0,2.4f,true);
            waitAudio();
            waitForSecs(0.3f);
            playAudioScene("DEMO3",1,false);
            // You’ll get to know them.;
            waitAudio();

        }
        else if (groupMode.equals("3"))
        {
            firstPoint = objectDict.objectForKey("plural_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2_demo);
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            playAudioScene("DEMO4",(addedMultipleLetters ? 1: 0),false);
            // To make the plurals, a letter is added.  |  To make the plurals, letters are added.;
            waitAudio();
            waitForSecs(0.3f);
            demoPluralWords(labels_mode2_demo);
            waitForSecs(0.6f);
            playAudioScene("DEMO4",2,false);
            // Look at the pattern.;
            waitAudio();

        }
        else if (groupMode.equals("4"))
        {
            firstPoint = objectDict.objectForKey("singular_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO5",0,false);
            // Look at the pattern here.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoSingularWords(labels_mode2_demo);
            waitForSecs(0.3f);
            firstPoint = objectDict.objectForKey("plural_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO5",1,false);
            // And here.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoPluralWords(labels_mode2_demo);
            waitForSecs(0.3f);
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2_demo);
            playAudioScene("DEMO5",(addedMultipleLetters ? 3: 2),false);
            // To make the plurals, this letter is added.  |   To make the plurals, these letters are added.;
            waitAudio();
            waitForSecs(0.6f);
            demoPluralWords_underlineAddedLetters(labels_mode2_demo);

        }
        else if (groupMode.equals("5"))
        {
            firstPoint = objectDict.objectForKey("singular_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO6",0,false);
            // Look at the pattern here.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoSingularWords(labels_mode2_demo);
            waitForSecs(0.3f);
            firstPoint = objectDict.objectForKey("plural_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO6",1,false);
            // And here.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoPluralWords(labels_mode2_demo);
            waitForSecs(0.3f);
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2_demo);
            playAudioScene("DEMO6",(addedMultipleLetters ? 3: 2),false);
            // See how a letter changed, to make the plural.  |  See how letters changed, to make the plural.;
            waitAudio();

        }
        hidePointer();
        waitForSecs(2.5f);
        lockScreen();
        for (List<> pair : labels_mode2_demo)
        {
            for (OBLabel label : pair)
            {
                label.hide();

            }

        }
        for (OBLabel line : underlines)
        {
            line.hide();

        }

        unlockScreen();
        playSfxAudio("alloff",false);
        waitForSecs(0.3f);
        nextScene();

    }
    public void setScenei()
    {
        hideControls("button");
        hideControls(".*_label");
        hideControls(".*_box");
        float labelHeight = objectDict.objectForKey("singular_label").height * 1.3;
        float totalHeight = labelHeight * words_mode2.size();
        float top = boxSingular.top;
        float bottom = bounds().size.height - labelHeight * 0.5;
        float availableHeight = (bottom - top) - totalHeight;
        int longestWord = 0;
        for (List<> pair : words_mode2)
        {
            OBWord singularWord = pair.firstObject;
            longestWord = Math.max(longestWord, (int) singularWord.text.length);
            OBWord pluralWord = pair.lastObject;
            longestWord = Math.max(longestWord, (int) pluralWord.text.length);

        }
        top += availableHeight * 0.5;
        OBControl singularLabelControl = objectDict.objectForKey("singular_label");
        OBControl pluralLabelControl = objectDict.objectForKey("plural_label");
        labels_mode2 = new ArrayList<>();
        draggableLabels = new ArrayList<>();
        List<> label_tops = new ArrayList<>();
        int rowColourIndex = 0;
        float longestLabelSingular = 0;
        float longestLabelPlural = 0;
        for (List<> pair : words_mode2)
        {
            List<> labels = new ArrayList<>();
            OBWord singularWord = pair.firstObject;
            OBWord pluralWord = pair.lastObject;
            OBLabel singular = createLabel_simple(singularLabelControl  text:singularWord.text colour:colourTextNormal);
            singular.setZPosition ( 30.0);
            singular.setTop ( top);
            singular.setProperty("word",singularWord);
            longestLabelSingular = Math.max(longestLabelSingular, singular.width);
            attachControl(singular);
            singular.hide();
            labels.add(singular);
            OBLabel plural = createLabel_simple(pluralLabelControl  text:pluralWord.text colour:colourTextNormal);
            plural.setZPosition ( 30.0);
            plural.setTop ( top);
            plural.setProperty("word",pluralWord);
            longestLabelPlural = Math.max(longestLabelPlural, plural.width);
            attachControl(plural);
            plural.hide();
            labels.add(plural);
            draggableLabels.add(plural);
            label_tops.add(NSNumber.numberWithFloat(plural.top));
            top += labelHeight;
            labels_mode2.add(labels);
            OBControl bar = [OBControl.alloc() init];
            bar.setFillColor ( colourRowArray.objectAtIndex(rowColourIndex));
            bar.setFrame(new RectF(0, singular.position.y - labelHeight * 0.5, bounds().size.width, labelHeight));
            attachControl(bar);
            bar.setZPosition ( 29.0);
            rowColourIndex++;

        }
        for (List<> pair : labels_mode2)
        {
            OBLabel singular = pair.firstObject;
            singular.setLeft ( symbolSingular.position.x - longestLabelSingular * 0.5);
            OBLabel plural = pair.lastObject;
            plural.setLeft ( symbolPlural.position.x - longestLabelPlural * 0.5);
            plural.setProperty("original_frame",NSValue valueWithRectF:plural..frame());

        }
        while (true)
        {
            draggableLabels = List<>.arrayWithArray(OBUtils.randomlySortedArray(draggableLabels));
            boolean needsReshuffle = false;
            for (int i = 0;
                 i < draggableLabels.size();
                 i++)
            {
                OBLabel label = draggableLabels.objectAtIndex(i);
                for (int j = 0;
                     j < labels_mode2.size();
                     j++)
                {
                    List<> pair = labels_mode2.objectAtIndex(j);
                    OBLabel plural = pair.lastObject;
                    if (label.equals(plural) && i == j)
                    {
                        needsReshuffle = true;
                        break;

                    }

                }

            }
            if (needsReshuffle) continue;
            break;

        }
        for (int i = 0;
             i < draggableLabels.size();
             i++)
        {
            OBLabel label = draggableLabels.objectAtIndex(i);
            label.setTop ( (float)label_tops.objectAtIndex(i) );
            label.setColour ( colourTextMovable);
            label.setProperty("original_position",OC_Generic.copyPoint(label.position()));
            label.setProperty("original_left",NSNumber.numberWithFloat(label.left));

        }

    }
    public void demoi()
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        playAudioScene("DEMO",0,false);
        // Now let’s try this.;
        movePointerToRestingPosition(0.3 wait:true);
        waitAudio();
        waitForSecs(0.3f);
        toggleSingularSymbol(true);
        for (List<> pair : labels_mode2)
        {
            OBLabel singular = pair.firstObject;
            lockScreen();
            singular.show();

            unlockScreen();
            playSfxAudio("singular",false);
            waitForSecs(0.2f);

        }
        float right = 0;
        for (List<> pair : labels_mode2)
        {
            OBLabel label = pair.firstObject;
            right = Math.max(right, label.right + label.width * 0.2);

        }
        OBLabel firstLabel = ((List<> ) labels_mode2.firstObject).firstObject;
        PointF firstPoint = OC_Generic.copyPoint(firstLabel.position());
        firstPoint.setX ( right);
        OBLabel lastLabel = ((List<> ) labels_mode2.lastObject).firstObject;
        PointF lastPoint = OC_Generic.copyPoint(lastLabel.position());
        lastPoint.setX ( right);
        movePointerToPoint(firstPoint,0.0,0.6f,true);
        playAudioScene("DEMO",1,false);
        // Singular.;
        movePointerToPoint(lastPoint,0.0,1.8f,true);
        waitAudio();
        toggleSingularSymbol(false);
        waitForSecs(0.3f);
        togglePluralSymbol(true);
        for (OBLabel label : draggableLabels)
        {
            lockScreen();
            label.show();

            unlockScreen();
            playSfxAudio("plural",false);
            waitForSecs(0.2f);

        }
        right = 0;
        for (List<> pair : labels_mode2)
        {
            OBLabel label = pair.lastObject;
            right = Math.max(right, label.right + label.width * 0.2);

        }
        firstPoint = ((OBLabel ) draggableLabels.firstObject).position;
        firstPoint.setX ( right);
        lastPoint = ((OBLabel ) draggableLabels.lastObject).position;
        lastPoint.setX ( right);
        movePointerToPoint(firstPoint,0.0,0.6f,true);
        playAudioScene("DEMO",2,false);
        // Plural.;
        movePointerToPoint(lastPoint,0.0,1.8f,true);
        waitAudio();
        togglePluralSymbol(false);
        waitForSecs(0.3f);
        playAudioScene("DEMO",3,false);
        // But these words are : the wrong order!;
        for (int i = 0;
             i < 3;
             i++)
        {
            lockScreen();
            for (List<> pair : labels_mode2)
            {
                OBLabel plural = pair.lastObject;
                plural.setOpacity(0.25);

            }

            unlockScreen();
            waitForSecs(0.2f);
            lockScreen();
            for (List<> pair : labels_mode2)
            {
                OBLabel plural = pair.lastObject;
                plural.setOpacity(1.0);

            }

            unlockScreen();
            waitForSecs(0.2f);

        }
        waitAudio();
        waitForSecs(0.3f);
        playAudioScene("DEMO",4,false);
        // Watch me.;
        waitAudio();
        waitForSecs(0.3f);
        int randomWordIndex = randomInt(0, (int) draggableLabels.size() - 1);
        OBLabel label = draggableLabels.objectAtIndex(randomWordIndex);
        RectF frame = label.propertyValue("original_frame").RectFValue();
        OBLabel otherLabel;
        float bestDistance = 10000;
        for (OBLabel possibleMatch : draggableLabels)
        {
            float delta = fabsf((float) possibleMatch.position.y - (float) (frame.origin.y + frame.size.height * 0.5));
            if (delta < bestDistance)
            {
                bestDistance = delta;
                otherLabel = possibleMatch;

            }

        }
        movePointerToPoint(label.position,0.0,0.6f,true);
        OC_Generic.pointer_moveToPointWithObject(label, otherLabel.position, 0.0, 0.6f, true, this);
        swapLabel(label withLabel:otherLabel);
        lockScreen();
        RectF labelFrame = label.propertyValue("original_frame").RectFValue();
        if (RectOverlapRatio(label.frame, labelFrame) >= 0.9)
        {
            label.setColour ( colourTextNormal);
            label.disable();

        }
        RectF otherLabelFrame = otherLabel.propertyValue("original_frame").RectFValue();
        if (RectOverlapRatio(otherLabel.frame, otherLabelFrame) >= 0.9)
        {
            otherLabel.setColour ( colourTextNormal);
            otherLabel.disable();

        }

        unlockScreen();
        hidePointer();
        waitForSecs(1.2f);
        playAudioScene("DEMO",5,false);
        // Your turn;
        lockScreen();
        label.setColour ( colourTextMovable);
        label.enable();
        otherLabel.setColour ( colourTextMovable);
        otherLabel.enable();

        unlockScreen();
        swapLabel(label withLabel:otherLabel);
        waitAudio();
        waitForSecs(0.3f);
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent);

    }
    public void demoj()
    {
        setStatus(STATUS_BUSY);
        loadPointer(POINTER_MIDDLE);
        movePointerToRestingPosition(0.3 wait:true);
        playAudioScene("DEMO",0,false);
        // Good! They’re : order.;
        waitAudio();
        waitForSecs(0.9f);
        for (List<> pair : labels_mode2)
        {
            OBLabel singular = pair.firstObject;
            OBLabel plural = pair.lastObject;
            lockScreen();
            singular.setColour ( colourTextHilited);
            toggleSingularSymbol(true);

            unlockScreen();
            OBWord singularWord = singular.propertyValue("word");
        [singularWord playAudio:wait:true];
            lockScreen();
            singular.setColour ( colourTextNormal);
            toggleSingularSymbol(false);

            unlockScreen();
            waitForSecs(0.3f);
            lockScreen();
            plural.setColour ( colourTextHilited);
            togglePluralSymbol(true);

            unlockScreen();
            OBWord pluralWord = plural.propertyValue("word");
        [pluralWord playAudio:wait:true];
            lockScreen();
            plural.setColour ( colourTextNormal);
            togglePluralSymbol(false);

            unlockScreen();
            waitForSecs(0.3f);

        }
        if (!groupMode.equals("0"))
        {
            playAudioScene("DEMO",1,false);
            // Now let's check!;
            waitAudio();
            waitForSecs(0.3f);

        }
        if (groupMode.equals("1"))
        {
            OBLabel firstLabel = ((List<> ) labels_mode2.firstObject).firstObject;
            PointF firstPoint = firstLabel.topLeft;
            OBLabel lastLabel = ((List<> ) labels_mode2.lastObject).lastObject;
            PointF lastPoint = lastLabel.bottomRight;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            playAudioScene("DEMO2",0,false);
            // The singular and plural are the same.;
            movePointerToPoint(lastPoint,0.0,2.4f,true);
            waitAudio();

        }
        else if (groupMode.equals("2"))
        {
            OBLabel firstLabel = ((List<> ) labels_mode2.firstObject).firstObject;
            PointF firstPoint = firstLabel.topLeft;
            OBLabel lastLabel = ((List<> ) labels_mode2.lastObject).lastObject;
            PointF lastPoint = lastLabel.bottomRight;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            playAudioScene("DEMO3",0,false);
            // The singular and plural are very different.;
            movePointerToPoint(lastPoint,0.0,2.4f,true);
            waitAudio();

        }
        else if (groupMode.equals("3"))
        {
            PointF firstPoint = OC_Generic.copyPoint(objectDict.objectForKey("plural_label").position());
            firstPoint.setY ( bounds().size.height * 0.9);
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2);
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            playAudioScene("DEMO4",(addedMultipleLetters ? 1: 0),false);
            // The same letter is added here too. |  The same letters are added here too.;
            waitAudio();
            waitForSecs(0.3f);
            demoPluralWords(labels_mode2);
            waitAudio();

        }
        else if (groupMode.equals("4"))
        {
            PointF firstPoint = OC_Generic.copyPoint(objectDict.objectForKey("singular_label").position());
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO5",0,false);
            // These show the same pattern as before.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoSingularWords(labels_mode2);
            waitForSecs(0.3f);
            firstPoint = objectDict.objectForKey("plural_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO5",1,false);
            // So do these.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoPluralWords(labels_mode2);
            waitForSecs(0.3f);
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2);
            playAudioScene("DEMO5",(addedMultipleLetters ? 3: 2),false);
            // This letter is added, to make the plural.  |  These letters are added, to make the plural.;
            waitAudio();
            waitForSecs(0.6f);
            demoPluralWords_underlineAddedLetters(labels_mode2);
            waitForSecs(0.3f);

        }
        else if (groupMode.equals("5"))
        {
            PointF firstPoint = OC_Generic.copyPoint(objectDict.objectForKey("singular_label").position());
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO6",0,false);
            // These show the same pattern as before.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoSingularWords(labels_mode2);
            waitForSecs(0.3f);
            firstPoint = objectDict.objectForKey("plural_label").position;
            firstPoint.setY ( bounds().size.height * 0.9);
            playAudioScene("DEMO6",1,false);
            // So do these.;
            movePointerToPoint(firstPoint,0.0,0.6f,true);
            waitAudio();
            demoPluralWords(labels_mode2);
            waitForSecs(0.3f);
            boolean addedMultipleLetters = wereRequiredMultipleLettersForPlural(labels_mode2);
            playAudioScene("DEMO6",(addedMultipleLetters ? 3: 2),false);
            // A letter changed, to make the plural.  |  Letters changed, to make the plural.;
            waitAudio();
            demoPluralWords_underlineChangedLetters(labels_mode2);
            waitForSecs(0.3f);

        }
        hidePointer();
        waitForSecs(2.5f);
        playAudioQueuedScene("FINAL",300,true);
        waitForSecs(0.7f);
        nextScene();

    }
    public void setScenek()
    {
        setScenei();

    }
    public void demok()
    {
        demoi();

    }
    public void demol()
    {
        demoj();

    }
    public void swapLabel(OBLabel  label1, OBLabel  label2)
    {
        PointF original1 = OC_Generic.copyPoint(label1.propertyValue("original.position())") ;
        PointF original2 = OC_Generic.copyPoint(label2.propertyValue("original.position())") ;
        PointF newOriginal1 = original2;
        newOriginal1.setX ( original1.x);
        PointF newOriginal2 = original1;
        newOriginal2.setX ( original2.x);
        label1.setProperty("original_position",NSValue.valueWithPointF(newOriginal1));
        label2.setProperty("original_position",NSValue.valueWithPointF(newOriginal2));
    [label1 moveToPoint:newOriginal1 time:0.3 wait:false];
        playSfxAudio("match",false);
    [label2 moveToPoint:newOriginal2 time:0.3 wait:true];

    }
    public OBLabel  findLabelUnderLabel(OBLabel  label)
    {
        OBLabel closestMatch;
        float bestOverlap = 0.0;
        for (OBLabel possibleMatch : draggableLabels)
        {
            if (!possibleMatch.isEnabled) continue;
            if (possibleMatch.equals(label)) continue;
            float overlap = RectOverlapRatio(label.frame, possibleMatch.frame);
            if (overlap > bestOverlap)
            {
                closestMatch = possibleMatch;
                bestOverlap = overlap;

            }

        }
        return closestMatch;

    }
    public boolean wereRequiredMultipleLettersForPlural(List<>  words)
    {
        for (List<> pair : words)
        {
            OBLabel plural = pair.lastObject;
            OBWord pluralWord = plural.propertyValue("word");
            int deltaFromRootPlural = (int) pluralWord.text.length - (int) pluralWord.Root.length;
            if (deltaFromRootPlural > 1) return true;

        }
        return false;

    }
    public void demoPluralWords(List<>  labels)
    {
        for (List<> pair : labels)
        {
            OBLabel plural = pair.lastObject;
            OBWord pluralWord = plural.propertyValue("word");
            String wordRoot = pluralWord.Root;
            if (wordRoot != null && wordRoot.length > 0)
            {
                NSRange range = pluralWord.text.rangeOfString(wordRoot);
                colourLabel(plural inRange:range withColour:colourTextNormal andRemainingColour:colourTextPlural);

            }
            else
            {
                plural.setColour ( colourTextNormal);

            }
            playSfxAudio("pluralpattern",false);
            waitForSecs(0.2f);

        }

    }
    public void demoPluralWords_underlineAddedLetters(List<>  labels)
    {
        underlines = new ArrayList<>();
        for (List<> pair : labels)
        {
            OBLabel singular = pair.firstObject;
            OBWord singularWord = singular.propertyValue("word");
            OBLabel plural = pair.lastObject;
            String wordRoot = singularWord.text;
            lockScreen();
            List<> pluralBreakdown = breakdownLabel(plural);
            NSRange pluralRange = plural.text.rangeOfString(wordRoot);
            PointF point1, point2;
            int firstIndex, lastIndex;
            for (firstIndex = 0;
                 firstIndex < pluralBreakdown.size();
                 firstIndex++)
            {
                if (NSLocationInRange(firstIndex, pluralRange)) continue;
                OBLabel label = pluralBreakdown.objectAtIndex(firstIndex);
                point1 = label.bottomLeft;
                break;

            }
            for (lastIndex = firstIndex;
                 lastIndex < pluralBreakdown.size();
                 lastIndex++)
            {
                if (NSLocationInRange(lastIndex, pluralRange)) break;
                OBLabel label = pluralBreakdown.objectAtIndex(lastIndex);
                point2 = label.bottomRight;

            }
            float nudge = plural.height * 0.1;
            point1.y -= nudge;
            point2.y -= nudge;
            OBPath underline = [OBPath.alloc() initLinePt1:point1 pt2:point2];
            underline.setLineWidth ( applyGraphicScale(3.0));
            underline.setStrokeColor ( colourLine);
            underline.setZPosition ( 31.00);
            attachControl(underline);
            underlines.add(underline);

            unlockScreen();
            playSfxAudio("underline",false);
            waitForSecs(0.2f);
            waitForSecs(0.3f);

        }

    }
    public void demoPluralWords_underlineChangedLetters(List<>  labels)
    {
        underlines = new ArrayList<>();
        for (List<> pair : labels)
        {
            OBLabel singular = pair.firstObject;
            OBWord singularWord = singular.propertyValue("word");
            OBLabel plural = pair.lastObject;
            OBWord pluralWord = plural.propertyValue("word");
            String wordRoot = singularWord.Root;
            lockScreen();
            List<> singularBreakdown = breakdownLabel(singular);
            NSRange singularRange = singular.text.rangeOfString(wordRoot);
            PointF point1, point2;
            int firstIndex, lastIndex;
            for (firstIndex = 0;
                 firstIndex < singularBreakdown.size();
                 firstIndex++)
            {
                if (NSLocationInRange(firstIndex, singularRange)) continue;
                OBLabel label = singularBreakdown.objectAtIndex(firstIndex);
                point1 = label.bottomLeft;
                break;

            }
            for (lastIndex = firstIndex;
                 lastIndex < singularBreakdown.size();
                 lastIndex++)
            {
                if (NSLocationInRange(lastIndex, singularRange)) break;
                OBLabel label = singularBreakdown.objectAtIndex(lastIndex);
                point2 = label.bottomRight;

            }
            float nudge = singular.height * 0.1;
            point1.y -= nudge;
            point2.y -= nudge;
            OBPath underline = [OBPath.alloc() initLinePt1:point1 pt2:point2];
            underline.setLineWidth ( applyGraphicScale(3.0));
            underline.setStrokeColor ( colourLine);
            underline.setZPosition ( 31.00);
            attachControl(underline);
            underlines.add(underline);

            unlockScreen();
            playSfxAudio("underline",false);
            waitForSecs(0.2f);
            wordRoot = pluralWord.Root;
            lockScreen();
            List<> pluralBreakdown = breakdownLabel(plural);
            NSRange pluralRange = plural.text.rangeOfString(wordRoot);
            PointF point1, point2;
            int firstIndex, lastIndex;
            for (firstIndex = 0;
                 firstIndex < pluralBreakdown.size();
                 firstIndex++)
            {
                if (NSLocationInRange(firstIndex, pluralRange)) continue;
                OBLabel label = pluralBreakdown.objectAtIndex(firstIndex);
                point1 = label.bottomLeft;
                break;

            }
            for (lastIndex = firstIndex;
                 lastIndex < pluralBreakdown.size();
                 lastIndex++)
            {
                if (NSLocationInRange(lastIndex, pluralRange)) break;
                OBLabel label = pluralBreakdown.objectAtIndex(lastIndex);
                point2 = label.bottomRight;

            }
            float nudge = plural.height * 0.1;
            point1.y -= nudge;
            point2.y -= nudge;
            OBPath underline = [OBPath.alloc() initLinePt1:point1 pt2:point2];
            underline.setLineWidth ( applyGraphicScale(3.0));
            underline.setStrokeColor ( colourLine);
            underline.setZPosition ( 31.00);
            attachControl(underline);
            underlines.add(underline);

            unlockScreen();
            playSfxAudio("underline",false);
            waitForSecs(0.2f);
            waitForSecs(0.3f);

        }

    }
    public void demoSingularWords(List<>  labels)
    {
        for (List<> pair : labels)
        {
            OBLabel singular = pair.firstObject;
            OBWord singularWord = singular.propertyValue("word");
            String wordRoot = singularWord.Root;
            if (wordRoot != null && wordRoot.length > 0)
            {
                NSRange range = singularWord.text.rangeOfString(wordRoot);
                colourLabel(singular inRange:range withColour:colourTextNormal andRemainingColour:colourTextSingular);

            }
            else
            {
                singular.setColour ( colourTextNormal);

            }
            playSfxAudio("singpattern",false);
            waitForSecs(0.2f);

        }

    }
    public void movePointerToRestingPosition(float time, boolean wait)
    {
        movePointerToPoint(new PointF(0.9 * bounds().size.widthf,  0.9 * bounds().size.heightf),timef,wait);

    }
    public void hidePointer()
    {
        movePointerToPoint(new PointF(1.1 * bounds().size.widthf,  1.1 * bounds().size.heightf),0,0.6f,false);

    }
    public List<>  breakdownLabel(OBLabel  mainLabel)
    {
        Map<> *attributes = @
        {
            NSFontAttributeName:mainLabel.font,                                 NSForegroundColorAttributeName:(id).get(Color.BLACKCGColor)
        }
        ;
        NSAttributedString astr = [NSAttributedString.alloc()initWithString:mainLabel.text attributes:attributes];
        CTLineRef line = CTLineCreateWithAttributedString((CFAttributedStringRef)astr);
        List<> lefts = new ArrayList<>();
        for (int i = 0;
             i < mainLabel.text.length;
             i++)
        {
            float f = CTLineGetOffsetForStringIndex(line, i, NULL);
            lefts.add(f);

        }
        List<> labs = new ArrayList<>();
        for (int i = 0;
             i < mainLabel.text.length;
             i++)
        {
            String text = mainLabel.text.substringWithRange(NSMakeRange(i, 1));
            OBLabel l = new OBLabel(text,font,fontSize);
            l.setColour ( Color.BLACK);
            l.setPosition ( mainLabel.position);
            l.setLeft ( mainLabel.left + (float)lefts.get(i));
            l.setProperty("origpos",OC_Generic.copyPoint(l.position()));
            labs.add(l);

        }
        return labs;

    }

     */
}
