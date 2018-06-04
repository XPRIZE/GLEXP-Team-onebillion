package org.onebillion.onecourse.mainui.oc_comprehension;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.oc_prepr3.OC_PrepR3;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBAudioPlayer;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OC_ReadingComprehension extends OC_Comprehension
{
    static int STATUS_AWAITING_TICK_CLICK = 1024;
    OBLabel passageLabel;

    public void miscSetUp()
    {
        super.miscSetUp();
        boolean showAnnaintro = OBUtils.coalesce(parameters.get("annaintro") ,"false").equals("true");
        events = new ArrayList<>();
        if(showAnnaintro)
            events.add("a");
        events.addAll(Arrays.asList("b","b2","c","c2"));
        currPassage = storyDict.get(storyID);
        for(int i = 0;i < currPassage.questions.size();i++)
            events.addAll(Arrays.asList("e"));
        setUpPassage();
    }

    public long switchStatus(String scene)
    {
        if(scene.equals("b2"))
            return setStatus(STATUS_AWAITING_TICK_CLICK);
        if(scene.equals("c2"))
            return setStatus(STATUS_AWAITING_ARROW_CLICK);
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void clearAll()
    {
        deleteControls(".*");
    }

    public List<Integer> rangeForSentenceIndex(int idx)
    {
        int len = 0,i = 0;
        for(String s : currPassage.sentences)
        {
            if(len > 0)
                len++;
            if(i == idx)
                return Arrays.asList(len,len + s.length());
            len += s.length();
            i++;
        }
        return Arrays.asList(0,len);
    }

    public void setUpPassage()
    {
        clearAll();
        loadEvent("passage");
        StringBuilder passageText = new StringBuilder();
        for(String s : currPassage.sentences)
        {
            if(passageText.length() > 0)
                passageText.append(" ");
            passageText.append(s);
        }
        OBControl textBox = objectDict.get("textbox");
        passageLabel = new OBLabel(passageText.toString(),font);
        passageLabel.setLineSpaceAdd(lineSpacing);
        passageLabel.setZPosition(50);
        passageLabel.setColour(Color.BLACK);
        passageLabel.setAlignment(OBLabel.OBLABEL_ALIGN_LEFT);
        //passageLabel.setWrapped(true);
        passageLabel.sizeToBoundingBoxMaxWidth(textBox.width());
        passageLabel.setLeft(textBox.left());
        passageLabel.setTop(textBox.top());
        RectF f = new RectF(passageLabel.frame());
        //f.bottom += textSize / 2; //Hack due to last line being lost
        passageLabel.setFrame(f);
        passageLabel.setBackgroundColor(Color.WHITE);
        attachControl(passageLabel);
        passageLabel.setPosition(objectDict.get("centrepoint").position());
        PointF pf = new PointF();
        pf.set(passageLabel.position());
        passageLabel.setProperty("origpos",pf);
    }

    public void setScenea()
    {
        loadEvent("a");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF pf = new PointF();
        pf.set(presenter.control.position());
        presenter.control.setProperty("restpos",pf);
        presenter.control.setRight(0);
        presenter.control.show();
    }

    public void setSceneb()
    {
        targets = Arrays.asList(objectDict.get("smalltick"));
    }

    public void setScenec()
    {
        targets = Arrays.asList(objectDict.get("arrow"));
    }

    public void setScenee()
    {
        passageLabel.setBottom(-1);
        deleteControls("smalltick");
        deleteControls("arrow");
        deleteControls("questionrect.*");
        deleteControls("answersr.*");
        loadEvent("question");
        if(questionLabel != null)
            detachControl(questionLabel);
        if (answerControls != null)
            for(OBControl c : answerControls)
                detachControl(c);

        setUpQuestion(questionNo);
    }

    public void tickDemo1(long stt,List aud,int ct)
    {
        try
        {
            long token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.6f) ,objectDict.get("smalltick").frame());
                PointF startpt = pointForDestPoint(destpoint,30);
                loadPointerStartPoint(startpt,destpoint);
                movePointerToPoint(destpoint,-1,true);
                checkSequenceToken(token);
                playAudioQueued(aud,true);
                checkSequenceToken(token);
                waitForSecs(0.4f);
                checkSequenceToken(token);
                waitForSecs(0.4f);
                flashTick(stt,60);
            }
        }
        catch(Exception e)
        {
        }
        thePointer.hide();
        sequenceLock.unlock();
    }

    public void doMainXX() throws Exception
    {
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",0.3f,false);
    }


    public void endBody() {
        try {
            if (currentEvent().equals("b2")) {
                final long stt = statusTime();
                reprompt(stt, null, 5.0f, new OBUtils.RunLambda() {
                    @Override
                    public void run() throws Exception {
                        tickDemo1(stt, currentAudio("PROMPT.REMINDER"), 1);
                    }
                });

            } else if (currentEvent().equals("c2")) {
                long stt = statusTime();
                flashArrow(stt);
            } else if (currentEvent().equals("e")) {
                long stt = statusTime();
                waitAudioQueue(audioLock);
                waitForSecs(2f);
                if (stt == statusTime()) {
                    playAudioQueuedScene("PROMPT", 0.3f, true);
                    reprompt(stt, (List) currentAudio("PROMPT.REMINDER"), 10, null);
                }
            }
        } catch (Exception e) {

        }
    }

    public void playStorySentence(int sno) throws Exception
    {
        String fn = String.format("%s_%s_p_p_%d",audioPrefix,currPassage.passageID,sno + 1);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public void animatePassage(boolean on) throws Exception
    {
        PointF pos = new PointF();
        pos.set((PointF)passageLabel.propertyValue("origpos"));
        if (!on)
            pos.y = -1 - passageLabel.height() / 2;
        OBAnim anim = OBAnim.moveAnim(pos,passageLabel);
        float destopacity = on?0:1;
        List anims = new ArrayList();
        anims.add(anim);
        anims.add(OBAnim.opacityAnim(destopacity,questionLabel));
        for(OBControl c : answerControls)
            anims.add(OBAnim.opacityAnim(destopacity,c));
        OBAnimationGroup.runAnims(anims,0.3,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
    }

    public void flashArrow(final long stt)
    {
        reprompt(stt, null, 5.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                OBControl c = objectDict.get("arrow");
                try
                {
                    for (int i = 0; i < 2; i++)
                    {
                        c.highlight();
                        waitForSecs(0.2f);
                        c.lowlight();
                        waitForSecs(0.2f);
                    }
                }
                catch (Exception e) {
                    c.lowlight();
                }
                if (!aborting())
                    flashArrow(stt);
            }
        });
    }

    public void flashTick(final long stt,float delay)
    {
        reprompt(stt, null, delay, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                OBControl c = objectDict.get("smalltick");
                try
                {
                    {
                        for(int i = 0;i < 2;i++)
                        {
                            c.highlight();
                            waitForSecs(0.3f);
                            c.lowlight();
                            waitForSecs(0.3f);
                        }
                    }
                }
                catch(Exception e)
                {
                    c.lowlight();
                }
                if(!aborting())
                    flashTick(stt,60);
            }
        });
    }

    public void replayQuestion(final long stt) throws Exception
    {
        reprompt(stt, null, 12f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                showThingsOneByOne();
                waitForSecs(1.5f);
                if(statusTime() == stt)
                    playAudioQueuedScene("PROMPT.REMINDER",false);
            }
        });
    }

    public void doMaine()
    {
        bringOnThings();
        targets = (List)answerControls;
    }


    public void demoa() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(1.3f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.5f);

        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = presenter.control.position();
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds() .width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(2)),this);
        waitForSecs(0.5f);
        OBControl faceright = presenter.control.objectDict.get("faceright");
        PointF lp = presenter.control.convertPointFromControl(new PointF(0, 0),faceright);
        PointF destpos = new PointF(bounds() .width() + lp.x + 1, currPos.y);
        presenter.walk(destpos);
        detachControl(presenter.control);
        presenter = null;

        nextScene();
    }

    public void demob() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,12);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.2f);

        destpoint = OB_Maths.locationForRect(new PointF(0.8f, 0.7f) , objectDict.get("smalltick").frame());
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO2",true);

        waitForSecs(0.8f);
        thePointer.hide();
        nextScene();
    }

    public void democ() throws Exception
    {
        waitForSecs(0.4f);
        PointF destpoint = OB_Maths.locationForRect(new PointF(0f,0.5f) ,objectDict.get("arrow").frame());
        PointF startpt = pointForDestPoint(destpoint,-2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.8f);
        thePointer.hide();
        nextScene();
    }

    public void checkTick()
    {
        try
        {
            takeSequenceLockInterrupt(true);
            sequenceLock.unlock();
            OBControl tick = objectDict.get("smalltick");
            tick.highlight();
            waitForSecs(0.3f);
            playSfxAudio("tickoff",false);
            tick.hide();
            waitForSecs(0.5f);
            objectDict.get("arrow").show();
            playSfxAudio("arrowon",false);
            waitForSecs(0.2f);
            waitSFX();
            nextScene();

        }
        catch(Exception exception)
        {
        }
    }

    public void checkAnswer(OBControl targ)
    {
        try
        {
            takeSequenceLockInterrupt(true);
            unlockSequenceLock();
            playSfxAudio("mctouch",true);
            if(targ == answerControls.get(0))
            {
                highlightButtonForQuestion((OBGroup) targ);
                waitForSecs(0.3f);
                gotItRightBigTick(true);
                waitForSecs(1.25f);
                nextQuestion();
            }
            else
            {
                dimButtonForQuestion((OBGroup) targ);
                gotItWrongWithSfx();
                waitForSecs(0.2f);
                waitSFX();
                waitForSecs(0.2f);
                playAudioQueuedScene("INCORRECT",true);
                waitForSecs(0.3f);
                int bgcol = objectDict.get("swatch").fillColor();
                OC_PrepR3.Est3_Question qu = currPassage.questions.get(questionNo);
                List ranges = new ArrayList<>();
                for(Integer n : qu.sentenceIndices)
                {
                    int nn = n.intValue();
                    List<Integer> r = rangeForSentenceIndex(nn - 1);
                    ranges.add(r);
                }
                lockScreen();
                passageLabel.setBackgroundColourRanges(ranges,bgcol);
                unlockScreen();
                animatePassage(true);
                for(Integer n : qu.sentenceIndices)
                {
                    int nn = n.intValue();
                    List<Integer> r = rangeForSentenceIndex(nn - 1);
                    passageLabel.setHighRange(r.get(0),r.get(1),Color.RED);
                    playStorySentence(nn - 1);
                    waitForSecs(0.3f);
                    passageLabel.setHighRange(-1,-1,Color.BLACK);
                }
                waitForSecs(2.5f);
                animatePassage(false);

                playSfxAudio("ping",false);
                feedbackButtonForQuestion(answerControls.get(0));
                waitForSecs(0.1f);
                waitSFX();

                OBGroup c = answerControls.get(0);
                OBLabel lab = (OBLabel) c.members.get(1);
                lab.setColour(Color.rgb(255,192,1));

                waitAudioQueue(playAnswerNo(0));
                waitForSecs(0.5f);
                lab.setColour(Color.BLACK);
                waitForSecs(0.5f);
                nextQuestion();
            }
        }
        catch(Exception e)
        {

        }
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_TICK_CLICK)
        {
            if(findTarget(pt) != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTick();
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_ARROW_CLICK)
        {
            if(findTarget(pt) != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkArrow();
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_CLICK)
        {
            final Object targ = findTarget(pt);
            if(targ != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkAnswer((OBControl) targ);
                    }
                });
            }
        }
    }

}
