package com.maq.xprize.onecourse.mainui.oc_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBImage;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.controls.OBTextLayer;
import com.maq.xprize.onecourse.controls.OBPresenter;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic;
import com.maq.xprize.onecourse.mainui.generic.OC_Generic_WordsEvent;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBImageManager;
import com.maq.xprize.onecourse.utils.OBPhoneme;
import com.maq.xprize.onecourse.utils.OBSyllable;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OBWord;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by pedroloureiro on 28/07/16.
 */
public class OC_Mwyh1 extends OC_Generic_WordsEvent
{
    OBPresenter presenter;
    OBLabel mainLabel;
    OBImage image;
    float mainLabelPositionFactor;
    List<OBLabel> touchables, destinations;
    List<Float> leftOffsets;
    List<OBPath> lines;
    List<OBSyllable> syllables;
    List<OBWord> words;
    int mode;
    Boolean showPicture;
    OBAnimationGroup moveBackAnimationGroup;
    double lastUserInteractionTimeStamp;
    Boolean reminderActive;

    public static final float MAX_GAP_FACTOR = 1.8f;
    public static float FIRST_REMINDER_DELAY = 6.0f;
    public static float SECOND_REMINDER_DELAY = 6.0f;

    public OC_Mwyh1()
    {
        super();
    }

    public void miscSetup()
    {
        super.miscSetup();
        //
        OBGroup presenterControl = (OBGroup) objectDict.get("presenter");
        if (presenterControl != null)
        {
            presenter = OBPresenter.characterWithGroup(presenterControl);
            presenter.control.setZPosition(200);
            presenter.control.setProperty("restPos", presenter.control.getWorldPosition());
            presenter.control.setRight(0);
            presenter.control.show();
        }
        //
        textSize = Float.parseFloat(eventAttributes.get("textsize"));
        mode = Integer.parseInt(parameters.get("mode"));
        needDemo = parameters.get("demo").compareTo("true") == 0;
        showPicture = (parameters.get("picture") != null && parameters.get("picture").compareTo("true") == 0);
        //
        mainLabelPositionFactor = (showPicture) ? 0.63f : 0.5f;
        //
        main_loadParameters();
        currNo = 0;
    }


    @Override
    public void prepare()
    {
        super.prepare();
        //
        loadFingers();
        loadEvent("mastera");
        miscSetup();
        //
        int totalEvents = main_getTotalEvents();
        //
        events = new ArrayList(Arrays.asList("c", "d", "e"));
        while (events.size() < totalEvents)
        {
            events.add(events.get(events.size() - 1));
        }
        while (events.size() > totalEvents)
        {
            events.remove(events.get(events.size() - 1));
        }
        //
        events.add("finale");
        if (needDemo)
        {
            events.add(0, "b");
        }
//        events.add(0, "a");
        //
        doVisual(currentEvent());
    }

    @Override
    public void doMainXX() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        doIntro(false);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    @Override
    public void doAudio(String scene) throws Exception
    {
        List audio = getAudioForScene(currentEvent(), "PROMPT");
        List replayAudio = Arrays.asList(main_getMainLabelAudioFile());
        setReplayAudio(replayAudio);
        playAudioQueued(audio);
        //
        doReminder(scene);
    }


    @Override
    public void doReminder() throws Exception
    {
        //MainActivity.log("overridden doReminder was called");
        // do nothing
    }

    public void doReminder (final String scene) throws Exception
    {
        lastUserInteractionTimeStamp = OC_Generic.currentTime();
        reminderActive = true;
        //
        doReminderWithStatusTime(scene, true);
    }



    public void doReminderWithStatusTime(final String eventName, final Boolean playAudio) throws Exception
    {
        if (this.aborting()) return;
        //
        if (!reminderActive)
        {
            //MainActivity.log("Reminder killed");
            return;
        }
        //
        if (!eventName.equalsIgnoreCase(currentEvent()))
        {
            //MainActivity.log("Reminder killed, wrong event: " + eventName + " " + currentEvent());
            return;
        }
        //
        double currentTime = OC_Generic.currentTime();
        Boolean thresholdTimeReached = currentTime - lastUserInteractionTimeStamp >= FIRST_REMINDER_DELAY;
        Boolean statusNotBusy = status() != STATUS_CHECKING && status() != STATUS_DOING_DEMO;
        //
        if (thresholdTimeReached && statusNotBusy)
        {
            if (playAudio)
            {
                //MainActivity.log("Reminder audio playing " + eventName + " " + currentEvent());
                action_playFinalWord();
            }
            action_flashLine(lastUserInteractionTimeStamp);
            //
            lastUserInteractionTimeStamp = OC_Generic.currentTime();
            //
            if (this.aborting()) return;
            OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    doReminderWithStatusTime(eventName, false);
                }
            });
        }
        else
        {
            if (this.aborting()) return;
            OBUtils.runOnOtherThreadDelayed(1.0f, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    doReminderWithStatusTime(eventName, playAudio);
                }
            });
        }
    }


    public void doIntro(Boolean demo) throws Exception
    {
        if (showPicture)
        {
            playSfxAudio("picon", false);
            image.show();
            waitForSecs(0.3);
        }
        action_introTouchables();
        waitForSecs(0.3);
        //
        action_introLines();
        waitForSecs(0.3);
        //
        action_playFinalWord();
        //
        if (demo)
        {
            waitAudio();
            waitForSecs(0.3);
            action_markLine();
            //
            lastUserInteractionTimeStamp = OC_Generic.currentTime();
        }
        else
        {
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    waitAudio();
                    waitForSecs(0.3);
                    action_markLine();
                    //
                    lastUserInteractionTimeStamp = OC_Generic.currentTime();
                }
            });
        }
    }


    public String main_getMainLabelAudioFile()
    {
        if (mode == 1)
        {
            OBSyllable syl = syllables.get(currNo);
            return syl.audio();
        }
        else if (mode == 2)
        {
            String ws[] = parameters.get("words").split(",");
            String currentWord = ws[currNo];
            return currentWord;
        }
        else if (mode == 3)
        {
            String ws[] = parameters.get("words").split(",");
            String currentWord = ws[currNo];
            return currentWord;
        }
        return null;
    }


    public void action_playComponentsAudioForLabel(OBLabel label) throws Exception
    {
        if (mode == 1)
        {
            List<OBPhoneme> phonemes = main_getMainLabelComponents();
            for (OBPhoneme phoneme : phonemes)
            {
                if (phoneme.text.compareTo(label.text()) == 0)
                {
                    phoneme.playAudio(this, true);
//                    playAudio(phoneme.audio());
//                    waitAudio();
                    return;
                }
            }
            //
            String audio = String.format("is_%s", label.text());
            playAudio(audio);
            waitAudio();
            return;
        }
        else if (mode == 2)
        {
            OBWord word = words.get(currNo);
            List<OBSyllable> wordSyllables = word.syllables();
            String text = ((OBTextLayer) label.layer).text();
            for (OBSyllable syllable : wordSyllables)
            {
                if (text.compareTo(syllable.text) == 0)
                {
                    try
                    {
                        syllable.playAudio(this, true);
//                        String audioFile = main_getMainLabelAudioFile().replace("fc_", "fc_syl_");
//                        playAudioFromTo(audioFile, (Double) syllable.timings.get(0), (Double) syllable.timings.get(1));
//                        waitAudio();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
        else if (mode == 3)
        {
            OBWord word = words.get(currNo);
            List<OBPhoneme> wordPhonemes = word.phonemes();
            String text = ((OBTextLayer) label.layer).text();
            for (OBPhoneme phoneme : wordPhonemes)
            {
                if (text.compareTo(phoneme.text) == 0)
                {
                    try
                    {
                        String audioFile = main_getMainLabelAudioFile().replace("fc_", "fc_let_");
                        playAudioFromTo(audioFile, (Double) phoneme.timings.get(0), (Double) phoneme.timings.get(1));
                        waitAudio();
                    }
                    catch (Exception e)
                    {
                        try
                        {
                            String audio = String.format("is_%s", label.text());
                            playAudio(audio);
                            waitAudio();
                        }
                        catch (Exception e2)
                        {
                            e.printStackTrace();
                            e2.printStackTrace();
                        }
                    }
                    return;
                }
            }
        }
    }


    public void main_loadParameters()
    {
        String ws[] = parameters.get("words").split(",");
        //
        if (mode == 1)
        {
            syllables = new ArrayList();
            for (String word : ws)
            {
                syllables.add((OBSyllable) wordComponents.get(word));
            }
        }
        else if (mode == 2)
        {
            words = new ArrayList();
            for (String word : ws)
            {
                words.add((OBWord) wordComponents.get(word));
            }
        }
        else if (mode == 3)
        {
            words = new ArrayList();
            for (String word : ws)
            {
                words.add((OBWord) wordComponents.get(word));
            }
        }
    }


    public int main_getTotalEvents()
    {
        if (mode == 1)
        {
            return (needDemo) ? syllables.size() - 1 : syllables.size();
        }
        else if (mode == 2)
        {
            return (needDemo) ? words.size() - 1 : words.size();
        }
        else if (mode == 3)
        {
            return (needDemo) ? words.size() - 1 : words.size();
        }
        return 0;
    }


    public String main_getMainLabelText()
    {
        if (mode == 1)
        {
            OBSyllable syl = syllables.get(currNo);
            return syl.text;
        }
        else if (mode == 2)
        {
            OBWord word = words.get(currNo);
            return word.text;
        }
        else if (mode == 3)
        {
            OBWord word = words.get(currNo);
            return word.text;
        }
        return null;
    }


    public List<OBPhoneme> main_getMainLabelComponents()
    {
        if (mode == 1)
        {
            OBSyllable syl = syllables.get(currNo);
            return syl.phonemes;
        }
        else if (mode == 2)
        {
            OBWord word = words.get(currNo);
            return (List<OBPhoneme>) (Object) word.syllables();
        }
        else if (mode == 3)
        {
            OBWord word = words.get(currNo);
            return word.phonemes();
        }
        return null;
    }

    public Boolean main_isLastWord()
    {
        if (mode == 1)
        {
            OBSyllable syl = syllables.get(currNo);
            return syl.equals(syllables.get(syllables.size() - 1));
        }
        else if (mode == 2)
        {
            OBWord word = words.get(currNo);
            return word.equals(words.get(words.size() - 1));
        }
        else if (mode == 3)
        {
            OBWord word = words.get(currNo);
            return word.equals(words.get(words.size() - 1));
        }
        return null;
    }


    public Boolean main_isLastPlacement()
    {
        int placedControls = action_getPlacedObjects();
        //
        if (mode == 1)
        {
            OBSyllable syl = syllables.get(currNo);
            return syl.phonemes.size() <= placedControls;
        }
        else if (mode == 2)
        {
            OBWord word = words.get(currNo);
            return word.syllables().size() <= placedControls;
        }
        else if (mode == 3)
        {
            OBWord word = words.get(currNo);
            return word.phonemes().size() <= placedControls;
        }
        return null;
    }


    @Override
    public void setSceneXX(String scene)
    {
        if (scene.compareTo("finale") == 0)
        {
            return;
        }
        //
        super.setSceneXX(scene);
        //
        if (image != null)
        {
            detachControl(image);
        }
        if (mainLabel != null)
        {
            detachControl(mainLabel);
        }
        if (touchables != null)
        {
            for (OBLabel label : touchables)
            {
                detachControl(label);
            }
        }
        if (destinations != null)
        {
            for (OBLabel label : destinations)
            {
                detachControl(label);
            }
        }
        //
        if (showPicture)
        {
            OBWord currentWord = words.get(currNo);
            image = OBImageManager.sharedImageManager().imageForName(currentWord.ImageFileName());
            image.setZPosition(4);
            image.setScale(applyGraphicScale(0.8f));
            image.setPosition(bounds().width() * 0.5f, bounds().height() * 0.29f);
            attachControl(image);
            image.hide();
        }
        //
        mainLabel = action_setupLabel(main_getMainLabelText());
        mainLabel.setPosition(bounds().width() * 0.5f, bounds().height() * mainLabelPositionFactor);
        mainLabel.setZPosition(4);
//        mainLabel.hide();
        attachControl(mainLabel);
        //
        action_generateComponents(main_getMainLabelComponents());
        float smallestWidth = Float.MAX_VALUE;
        float largestWidth = 0;
        float sumWidth = 0;
        for (OBLabel label : destinations)
        {
            smallestWidth = Math.min(smallestWidth, label.width());
            largestWidth = Math.max(largestWidth, label.width());
            sumWidth += label.width();
        }
        //
        float lineWidth = (float) Math.min(0.9f * largestWidth, 1.1 * (sumWidth / (float) destinations.size()));
        //
        float normalWidth = mainLabel.width();
        float maxGap = MAX_GAP_FACTOR * textSize;
        float width = normalWidth + (destinations.size() - 1) * maxGap;
        float left = mainLabel.position().x - width / 2.0f;
        for (int i = 0; i < destinations.size(); i++)
        {
            OBLabel label = destinations.get(i);
            label.setLeft(leftOffsets.get(i) + maxGap * i);
        }
        OBGroup labelGroup = new OBGroup((List<OBControl>) (Object) destinations);
        labelGroup.setPosition(bounds().width() * 0.5f, bounds().height() * mainLabelPositionFactor);
        for (OBLabel label : destinations)
        {
            labelGroup.removeMember(label);
            label.setProperty("finalPosition", OC_Generic.copyPoint(label.getWorldPosition()));
            attachControl(label);
        }
        //
        OBControl bottomBar = objectDict.get("bottombar");
        int totalDivisions = main_getMainLabelComponents().size() + 1;
        float divisionLength = bounds().width() / (float) totalDivisions;
        //
        List<PointF> positions = new ArrayList();
        for (int i = 0; i < totalDivisions - 1; i++)
        {
            positions.add(new PointF(divisionLength * (i + 1), bottomBar.position().y));
        }
        List labels = new ArrayList();
        for (int i = 0; i < destinations.size(); i++)
        {
            OBLabel label = destinations.get(i);
            OBLabel copy = (OBLabel) label.copy();
            labels.add(copy);
        }
        //
        int i = 0;
        List sortedTouchables = new ArrayList();
        for (OBLabel label : (List<OBLabel>) OBUtils.randomlySortedArray(labels))
        {
            label.setPosition(positions.get(i));
            label.setProperty("originalPosition", OC_Generic.copyPoint(label.position()));
            label.show();
            attachControl(label);
            PointF position = OC_Generic.copyPoint(label.position());
            position.x -= bounds().width();
            label.setPosition(position);
            sortedTouchables.add(0, label);
            i++;
        }
        touchables = sortedTouchables;
        //
        List linesToBeAdded = new ArrayList();
        OBPath originalLine = (OBPath) objectDict.get("line");
        for (OBLabel label : destinations)
        {
            OBPath line = (OBPath) originalLine.copy();
            line.setWidth(lineWidth);
            line.setPosition(OC_Generic.copyPoint(label.position()));
            line.setBottom(label.bottom() - 0.1f * label.height());
            line.setZPosition(3);
            line.setProperty("originalColour", line.strokeColor());
            attachControl(line);
            linesToBeAdded.add(line);
            line.hide();
        }
        lines = linesToBeAdded;
        originalLine.hide();
    }


    public void demoa() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        List aud = currentAudio("DEMO");
        PointF position = OC_Generic.copyPoint((PointF) presenter.control.propertyValue("restPos"));
        presenter.walk(position);
        presenter.faceFront();
        waitForSecs(0.3);
        //
        presenter.speak(Arrays.asList(aud.get(0)), this); // You’ll listen!
        //
        presenter.moveHandToEarController(this);
        waitForSecs(0.3);
        //
        presenter.moveHandFromEarController(this);
        waitForSecs(0.3);
        //
        presenter.speak(Arrays.asList(aud.get(1)), this); // And then you’ll drag letters, to make what you hear.
        waitForSecs(0.3);
        //
        PointF currPos = OC_Generic.copyPoint(presenter.control.getWorldPosition());
        PointF destPos = new PointF(currPos.x - bounds().width() * 0.2f, currPos.y);
        presenter.walk(destPos);
        presenter.faceFront();
        waitForSecs(0.3);
        //
        presenter.speak(Arrays.asList(aud.get(2)), this); // Are you ready?
        waitForSecs(0.3);
        //
        currPos = OC_Generic.copyPoint(presenter.control.getWorldPosition());
        OBControl side = presenter.control.objectDict.get("faceright");
        destPos = new PointF(-side.width() * 1.3f, currPos.y);
        presenter.walk(destPos);
        //
        nextScene();
    }


    public void demob() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 1.3f, 0, 0.1f, true, this);
        //
        action_playNextDemoSentence(false); // Listen ... and watch me
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 0.9f, 0, 0.3f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        doIntro(true);
        //
        action_playNextDemoSentence(false); // I will drag the letters into place, in the right order.
        OC_Generic.pointer_moveToRelativePointOnScreen(0.8f, 0.8f, -10, 0.6f, true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        int placed = 0;
        //
        lockScreen();
        action_markLine();
        unlockScreen();
        //
        for (OBPhoneme word : main_getMainLabelComponents())
        {
            OBLabel touchable = null;
            for (OBLabel label : touchables)
            {
                if (label.text().compareTo(word.text) == 0)
                {
                    touchable = label;
                    break;
                }
            }
            //
            if (touchable != null)
            {
                touchables.remove(touchable);
                OC_Generic.pointer_moveToObject(touchable, -5, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
                touchable.setColour(Color.RED);
                waitForSecs(0.1);
                //
                OBLabel destination = destinations.get(placed);
                PointF position = OC_Generic.copyPoint(destination.position());
                touchable.setZPosition(10);
                OC_Generic.pointer_moveToPointWithObject(touchable, position, -5, 0.6f, true, this);
                //
                OBPath line = lines.get(placed);
                playSfxAudio("click", false);
                //
                placed++;
                //
                lockScreen();
                line.hide();
                action_markLine();
                unlockScreen();
                //
                position = OC_Generic.copyPoint(touchable.position());
                position.y = touchable.bottom() + touchable.height() * 0.1f;
                movePointerToPoint(position, -5, 0.3f, true);
                //
                action_playComponentsAudioForLabel(touchable);
                waitForSecs(0.3);
                //
                lockScreen();
                destination.show();
                touchable.hide();
                touchable.setColour(Color.BLACK);
                unlockScreen();
            }
        }
        waitForSecs(0.3);
        //
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 0.9f, 0, 0.9f, false, this);
        action_endOfEvent();
        waitForSecs(0.1);
        //
        action_playNextDemoSentence(true); // Now your turn.
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        action_exitAll();
        waitForSecs(0.3);
        //
        currNo++;
        nextScene();
    }


    public void democ() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        loadPointer(POINTER_MIDDLE);
        OC_Generic.pointer_moveToRelativePointOnScreen(0.9f, 1.3f, 0, 0.1f, true, this);
        //
        doIntro(true);
        //
        OBControl trb = MainActivity.mainViewController.topRightButton;
        final PointF butPt = OB_Maths.locationForRect(0.5f, 1.1f, trb.frame());
        final PointF offpt = new PointF(butPt.x, butPt.y);
        offpt.y = glView().getHeight();
        theMoveSpeed = glView().getWidth();
        moveObjects(Collections.singletonList(thePointer), butPt, -1, OBAnim.ANIM_EASE_IN_EASE_OUT);
        waitForSecs(0.01f);
        action_playNextDemoSentence(false); // And remember … this lets you listen again.
        waitAudio();
        waitForSecs(0.5f);
        //
        thePointer.hide();
        waitForSecs(0.3);
        //
        setStatus(STATUS_AWAITING_CLICK);
        doAudio(currentEvent());
    }


    public void demofinale() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        //
        playSceneAudio("FINAL", true);
        waitForSecs(0.3);
        //
        nextScene();
    }


    public void action_generateComponents(List<OBPhoneme> components)
    {
        String text = mainLabel.text();
        //
        int startingIndex = 0;
        destinations = new ArrayList();
        leftOffsets = new ArrayList();
        //
        for (OBPhoneme word : components)
        {
            int index = text.indexOf(word.text, startingIndex);
            int length = word.text.length();
            RectF bb = OBUtils.getBoundsForSelectionInLabel(index, index + length, mainLabel);
            float left = bb.left;
            //
            OBLabel l = action_setupLabel(word.text);
            l.setPosition(OC_Generic.copyPoint(mainLabel.position()));
            l.setLeft(left);
            l.setProperty("originalPosition", OC_Generic.copyPoint(l.position()));
            destinations.add(l);
            attachControl(l);
            startingIndex += word.text.length();
            l.hide();
            l.enable();
            leftOffsets.add(left);
        }
    }


/*
                int index = fullWord.text.indexOf(partWordPhoneme.text);
                int len = partWordPhoneme.text.length();
                RectF bb = OBUtils.getBoundsForSelectionInLabel(index,index+len,fullWordLabel);
                float left = bb.left;


                fullWordLabel.setHighRange(index,index+len,Color.BLUE);
                partWordLabel.setProperty("dest_left",left);

-(void) action_generateComponents:(NSArray*)components font:(UIFont*)font
{
    NSDictionary *attributes = @{NSFontAttributeName:font,
                                 NSForegroundColorAttributeName:(id)[[UIColor blackColor]CGColor]
                                 };
    NSString *text = mainLabel.string;
    NSAttributedString *astr = [[NSAttributedString alloc]initWithString:text attributes:attributes];
    CTLineRef line = CTLineCreateWithAttributedString((CFAttributedStringRef)astr);
    NSMutableArray *lefts = [NSMutableArray array];
    for (int i = 0;i < [text length];i++)
    {
        CGFloat f = CTLineGetOffsetForStringIndex(line, i, NULL);
        [lefts addObject:@(f)];
    }
    NSMutableArray *labs = [NSMutableArray array];
    int idx = 0,i = 0;
    NSMutableArray *syllableLefts = [NSMutableArray array];
    for (OBPhoneme *word in components)
    {
//        OBPhoneme *word = localDictionary[component];
        OBLabel *l = [OBLabel textWithString:word.text font:font];
        l.colour = [UIColor blackColor];
        l.position = mainLabel.position;
        syllableLefts[i] = lefts[idx];
        l.left = mainLabel.left + [lefts[idx]floatValue];
        [l setProperty:@"originalPosition" value:[NSValue valueWithCGPoint:l.position]];
        [labs addObject:l];
        [self attachControl:l];
        idx+= [word.text length];
        [l hide];
        i++;
    }
    destinations = labs;
    leftOffsets = syllableLefts;
}

 */


    public void action_segmentComponents()
    {
        final float normalWidth = mainLabel.width();
        final float maxGap = MAX_GAP_FACTOR * textSize;
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for (OBLabel label : destinations)
                {
                    PointF start = (PointF) label.propertyValue("originalPosition");
                    PointF end = (PointF) label.propertyValue("finalPosition");
                    PointF diff = OB_Maths.DiffPoints(end, start);
                    PointF position = new PointF(start.x + diff.x * frac, start.y + diff.y * frac);
                    label.setPosition(position);
                }
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public void action_joinComponents()
    {
        final float normalWidth = mainLabel.width();
        final float maxGap = MAX_GAP_FACTOR * textSize;
        OBAnim anim = new OBAnimBlock()
        {
            @Override
            public void runAnimBlock(float frac)
            {
                for (OBLabel label : destinations)
                {
                    PointF start = (PointF) label.propertyValue("finalPosition");
                    PointF end = (PointF) label.propertyValue("originalPosition");
                    PointF diff = OB_Maths.DiffPoints(end, start);
                    PointF position = new PointF(start.x + diff.x * frac, start.y + diff.y * frac);
                    label.setPosition(position);
                }
            }
        };
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.6, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
    }


    public void action_playFinalWord()
    {
        playAudio(main_getMainLabelAudioFile());
    }


    public void action_endOfEvent() throws Exception
    {
        waitForSecs(0.3);
        //
        for (OBLabel label : destinations)
        {
            label.setColour(Color.RED);
            action_playComponentsAudioForLabel(label);
            label.setColour(Color.BLACK);
            waitForSecs(0.2);
        }
        //
        playSfxAudio("blend", false);
        action_joinComponents();
        //
        lockScreen();
        for (OBLabel label : destinations)
        {
            label.hide();
        }
        mainLabel.show();
        unlockScreen();
        waitForSecs(0.3);
        //
        action_playFinalWord();
        mainLabel.setColour(Color.RED);
        waitAudio();
        waitForSecs(0.3);
        //
        mainLabel.setColour(Color.BLACK);
        waitForSecs(0.9);
    }


    public void action_markLine()
    {
        if (main_isLastPlacement())
        {
            return;
        }
        //
        OBPath line = lines.get(action_getPlacedObjects());
        line.setStrokeColor(Color.RED);
    }


    public int action_getPlacedObjects()
    {
        int placedControls = 0;
        for (OBControl touchable : touchables)
        {
            if (!touchable.isEnabled())
            {
                placedControls++;
            }
        }
        return placedControls;
    }


    public void action_flashLine(double timeStamp) throws Exception
    {
        if (main_isLastPlacement())
        {
            return;
        }
        //
        OBPath line = lines.get(action_getPlacedObjects());
        for (int i = 0; i < 3; i++)
        {
            lockScreen();
            line.setStrokeColor((int) line.propertyValue("originalColour"));
            unlockScreen();
            waitForSecs(0.3);
            if (timeStamp != lastUserInteractionTimeStamp) break;
            //
            lockScreen();
            line.setStrokeColor(Color.RED);
            unlockScreen();
            waitForSecs(0.3);
            if (timeStamp != lastUserInteractionTimeStamp) break;
        }
    }


    public void action_introTouchables() throws Exception
    {
        float duration = 0.6f;
        playSfxAudio("letterson", false);
        //
        for (OBControl control : touchables)
        {
            OBAnim anim = OBAnim.moveAnim((PointF) control.propertyValue("originalPosition"), control);
            OBAnimationGroup.runAnims(Arrays.asList(anim), duration, false, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
            waitForSecs(0.05);
        }
        waitSFX();
        waitForSecs(duration);
    }


    public void action_introLines() throws Exception
    {
        for (OBPath line : lines)
        {
            playSfxAudio("lineson", false);
            line.show();
            waitForSecs(0.3);
        }
    }


    public void action_exitAll() throws Exception
    {
        playSfxAudio("picoff", false);
        //
        lockScreen();
        for (OBControl control : lines)
        {
            control.hide();
        }
        for (OBControl control : touchables)
        {
            control.hide();
        }
        for (OBControl control : destinations)
        {
            control.hide();
        }
        mainLabel.hide();
        //
        if (image != null)
        {
            image.hide();
        }
        //
        unlockScreen();
        //
        waitSFX();
    }


    public Boolean action_verifyDropPosition(PointF position) throws Exception
    {
        synchronized (this)
        {
            if (target == null) return false;
            if (!target.isEnabled()) return false;
            //
            int placedObjectsCount = action_getPlacedObjects();
            if (placedObjectsCount >= destinations.size()) return false;
            //
            OBLabel label = (OBLabel) target;
            OBLabel correctLabel = destinations.get(placedObjectsCount);
            //
            if (correctLabel.text().compareTo(label.text()) == 0)
            {
                if (OBUtils.RectOverlapRatio(correctLabel.frame, label.frame) > 0.2)
                {
                    setStatus(STATUS_CHECKING);
                    //
                    playSfxAudio("click", false);
                    if (action_getPlacedObjects() < lines.size())
                    {
                        OBPath line = lines.get(action_getPlacedObjects());
                        lockScreen();
                        line.hide();
                        label.disable();
                        unlockScreen();
                    }
                    //
                    if (moveBackAnimationGroup != null)
                    {
                        moveBackAnimationGroup.flags = OBAnimationGroup.ANIM_CANCEL;
                    }
                    OBAnim anim = OBAnim.moveAnim(OC_Generic.copyPoint(correctLabel.position()), label);
                    OBAnimationGroup.runAnims(Arrays.asList(anim), 0.1f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    //
                    lockScreen();
                    correctLabel.show();
                    label.hide();
                    label.disable();
                    action_markLine();
                    unlockScreen();
                    //
                    return true;
                }
            }
            return false;
        }
    }


    public void checkTouchableAtPosition(PointF position, long timeStamp)
    {
        if (statusChanged(timeStamp))
        {
            return;
        }
        //
        try
        {
            if (!target.isEnabled()) return;
            //
            if (action_verifyDropPosition(position))
            {
                if (target != null)
                {
                    OBLabel label = (OBLabel) target;
                    target = null;
                    checkForLastPlacement(label);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public void checkForLastPlacement(OBLabel label) throws Exception
    {
        if (main_isLastPlacement())
        {
            reminderActive = false;
            //
            action_playComponentsAudioForLabel(label);
            action_endOfEvent();
            //
            gotItRightBigTick(true);
            waitForSecs(0.3);
            //
            playSceneAudio("FINAL", true);
            //
            if (!main_isLastWord())
            {
                action_exitAll();
                waitForSecs(0.3);
            }
            //
            currNo++;
            nextScene();
        }
        else
        {
            lastUserInteractionTimeStamp = OC_Generic.currentTime();
            setStatus(STATUS_AWAITING_CLICK);
            //
            action_playComponentsAudioForLabel(label);
            label.setColour(Color.BLACK);
        }
    }


    public void checkTouchableDropAtPosition(PointF position, long timeStamp)
    {
        synchronized (this)
        {
            if (statusChanged(timeStamp)) return;
            //
            if (target == null) return;
            if (!target.isEnabled()) return;
            //
            setStatus(STATUS_CHECKING);
            //
            try
            {
                OBLabel label = (OBLabel) target;
                label.setColour(Color.BLACK);
                //
                if (action_verifyDropPosition(position))
                {
                    target = null;
                    checkForLastPlacement(label);
                }
                else
                {
                    gotItWrongWithSfx();
                    //
                    if (moveBackAnimationGroup != null)
                    {
                        moveBackAnimationGroup.flags = OBAnimationGroup.ANIM_CANCEL;
                    }
                    OBAnim anim = OBAnim.moveAnim((PointF) label.propertyValue("originalPosition"), label);
                    moveBackAnimationGroup = OBAnimationGroup.runAnims(Arrays.asList(anim), 0.1f, true, OBAnim.ANIM_EASE_IN_EASE_OUT, this);
                    //
                    setStatus(STATUS_AWAITING_CLICK);
                    //
                    final double lastTimeStamp = lastUserInteractionTimeStamp;
                    final long lastStatusTime = statusTime;
                    //
                    OBUtils.runOnOtherThreadDelayed(0.3f, new OBUtils.RunLambda()
                    {
                        @Override
                        public void run() throws Exception
                        {
                            if (lastUserInteractionTimeStamp != lastTimeStamp) return;
                            if (statusChanged(lastStatusTime)) return;
                            //
                            action_playFinalWord();
                        }
                    });
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    public void checkDragTarget(OBControl targ, PointF pt)
    {
        super.checkDragTarget(targ, pt);
        //
        if (target.equals(targ) && status() == STATUS_DRAGGING)
        {
            OBLabel label = (OBLabel) target;
            label.setColour(Color.RED);
        }
    }

    public OBControl findTouchable(PointF pt)
    {
        return finger(-1, 2, (List<OBControl>) (Object) touchables, pt, true);
    }

    @Override
    public void touchDownAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            lastUserInteractionTimeStamp = OC_Generic.currentTime();
            //
            final OBControl obj = findTouchable(pt);
            if (obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    @Override
                    public void run() throws Exception
                    {
                        checkDragTarget(obj, pt);
                    }
                });
            }
        }
    }

    @Override
    public void touchUpAtPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            lastUserInteractionTimeStamp = OC_Generic.currentTime();
            //
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkTouchableDropAtPosition(pt, statusTime);
                }
            });
        }
    }

    @Override
    public void touchMovedToPoint(final PointF pt, View v)
    {
        if (status() == STATUS_DRAGGING)
        {
            if (target != null)
            {
                lockScreen();
                target.setPosition(OB_Maths.AddPoints(pt, dragOffset));
                unlockScreen();
            }
            //
            lastUserInteractionTimeStamp = OC_Generic.currentTime();
            //
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    checkTouchableAtPosition(pt, statusTime);
                }
            });
        }
    }

}