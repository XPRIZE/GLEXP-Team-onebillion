package com.maq.xprize.onecourse.mainui.oc_comprehension;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPresenter;
import com.maq.xprize.onecourse.mainui.oc_prepr3.OC_PrepR3;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimBlock;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBAudioManager;
import com.maq.xprize.onecourse.utils.OBAudioPlayer;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.maq.xprize.onecourse.utils.OBAudioManager.AM_MAIN_CHANNEL;
import static com.maq.xprize.onecourse.utils.OB_Maths.clamp01;

public class OC_ListeningComprehension extends OC_Comprehension {
    boolean listenTwice;
    int storyPlays, maxStoryPlays;

    public void miscSetUp() {
        super.miscSetUp();
        boolean showAnnaintro = OBUtils.coalesce(parameters.get("annaintro"), "false").equals("true");
        listenTwice = OBUtils.coalesce(parameters.get("listentwice"), "false").equals("true");
        maxStoryPlays = listenTwice ? 2 : 1;
        events = new ArrayList<>();
        events.add(showAnnaintro ? "a" : "b");
        events.addAll(Arrays.asList("c", "d"));
        currPassage = storyDict.get(storyID);
        for (int i = 0; i < currPassage.questions.size(); i++)
            events.addAll(Arrays.asList("e"));
        loadEvent("passage");
    }


    public long switchStatus(String scene) {
        if (scene.equals("e"))
            return setStatus(STATUS_AWAITING_CLICK);
        return setStatus(STATUS_WAITING_FOR_BUTTON_CLICK);
    }


    public void setScenea() {
        loadEvent("a");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF pos = new PointF();
        pos.set(presenter.control.position());
        presenter.control.setProperty("restpos", pos);
        presenter.control.setRight(0);
        presenter.control.show();
        setButtonInactiveSfx(false);
    }

    public void setSceneb() {
        setButtonInactiveSfx(false);
    }

    public void setScenee() {
        deleteControls("speech.*");
        deleteControls("arrow");
        deleteControls("button.*");
        deleteControls("textrect.*");
        deleteControls("questionrect.*");
        deleteControls("answersr.*");
        loadEvent("question");
        if (questionLabel != null)
            detachControl(questionLabel);
        if (answerControls != null)
            for (OBControl c : answerControls)
                detachControl(c);

        setUpQuestion(questionNo);
    }

    public void doMaind() throws Exception {
        setReplayAudio((List) currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT", 0.3f, false);
    }

    public void doMaine() {
        bringOnThings();
        targets = (List) answerControls;
    }

    public void endBody() {
        try {
            if (currentEvent().equals("d")) {
                flashButton(statusTime());
            } else if (currentEvent().equals("e")) {
                long stt = statusTime();
                waitAudioQueue(audioLock);
                waitForSecs(2f);
                if (stt == statusTime()) {
                    playAudioQueuedScene("PROMPT", 0.3f, true);
                    replayQuestion(stt);
                }
            }
        } catch (Exception e) {

        }
    }

    public void setButtonSpeakingSfx(boolean playSfx) {
        OBGroup sb = (OBGroup) objectDict.get("speechbutton");
        lockScreen();
        sb.objectDict.get("inactive").hide();
        sb.objectDict.get("normal").hide();
        sb.objectDict.get("speaking").show();
        unlockScreen();
    }

    public void setButtonInactiveSfx(boolean playSfx) {
        OBGroup sb = (OBGroup) objectDict.get("speechbutton");
        lockScreen();
        sb.objectDict.get("inactive").show();
        sb.objectDict.get("normal").hide();
        sb.objectDict.get("speaking").hide();
        unlockScreen();
    }

    public void setButtonActiveSfx(boolean playSfx) throws Exception {
        if (playSfx)
            playSfxAudio("buttonactive", false);
        OBGroup sb = (OBGroup) objectDict.get("speechbutton");
        lockScreen();
        sb.objectDict.get("inactive").hide();
        sb.objectDict.get("normal").show();
        sb.objectDict.get("speaking").hide();
        unlockScreen();
    }

    public OBControl innerCircle() {
        OBGroup sb = (OBGroup) objectDict.get("speechbutton");
        OBGroup l = (OBGroup) sb.objectDict.get("speaking");
        return l.objectDict.get("Circle1");
    }

    public void deployThrobAnim() throws Exception {
        final OBControl circ = innerCircle();
        OBAnimBlock blockAnim = new OBAnimBlock() {
            @Override
            public void runAnimBlock(float frac) {
                float ang = (float) (frac * Math.PI);
                float t = (float) Math.sin(ang);
                circ.setOpacity(1.0f - 0.3f * t);
            }
        };

        OBAnimationGroup ag = new OBAnimationGroup();
        registerAnimationGroup(ag, "throb");
        ag.applyAnimations(Arrays.asList((OBAnim) blockAnim), 2.8f, false, OBAnim.ANIM_LINEAR, -1, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                circ.setOpacity(1.0f);
            }
        }, this);
    }

    public void growCircle() throws Exception {
        final OBControl circle = innerCircle();
        final float origsc = circle.scale();
        circle.setProperty("origscale", (circle.scale()));
        OBAnim anim = new OBAnimBlock() {
            @Override
            public void runAnimBlock(float frac) {
                float t = clamp01(frac);
                float amt = OB_Maths.interpolateVal(origsc, origsc * 1.25f, t);
                lockScreen();
                circle.setScale(amt);
                unlockScreen();
            }
        };

        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, null);
    }

    public void shrinkCircle() throws Exception {
        OBControl circle = innerCircle();
        float sc = (Float) circle.propertyValue("origscale");
        OBAnim anim = OBAnim.scaleAnim(sc, circle);
        OBAnimationGroup.runAnims(Arrays.asList(anim), 0.3, false, OBAnim.ANIM_EASE_IN_EASE_OUT, null);
    }

    public void flashButton(final long stt) {
        reprompt(stt, null, 5.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                OBControl c = objectDict.get("speechbutton");
                try {
                    long token = takeSequenceLockInterrupt(true);
                    if (token == sequenceToken) {
                        for (int i = 0; i < 3; i++) {
                            c.setOpacity(0.5f);
                            waitForSecs(0.2f);
                            c.setOpacity(1.0f);
                            waitForSecs(0.2f);
                            checkSequenceToken(token);
                        }
                    }
                } catch (Exception e) {
                    c.setOpacity(1.0f);
                }
                sequenceLock.unlock();
                if (!_aborting)
                    flashButton(stt);

            }
        });
    }

    public void flashArrow(final long stt) {
        reprompt(stt, null, 5.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {

                OBControl c = objectDict.get("arrow");
                try {
                    {
                        for (int i = 0; i < 2; i++) {
                            c.highlight();
                            waitForSecs(0.2f);
                            c.lowlight();
                            waitForSecs(0.2f);
                        }
                    }
                } catch (Exception e) {
                    c.lowlight();
                }
                if (!_aborting)
                    flashArrow(stt);
            }
        });
    }

    public void replayQuestion(final long stt) {
        reprompt(stt, null, 5.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                try {
                    showThingsOneByOne();
                    waitForSecs(2f);
                    if (statusTime() == stt)
                        playAudioQueuedScene("PROMPT.REMINDER", false);
                } catch (Exception e) {

                }
            }
        });
    }

    public void demoa() throws Exception
    {
        presenter.walk((PointF)presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        presenter.moveHandToEarController(this);
        waitForSecs(1.3f);
        presenter.moveHandFromEarController(this);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.5f);

        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = new PointF();
        currPos.set(presenter.control.position());
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

        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);

        nextScene();

    }

    public void demob() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);

        nextScene();
    }

    public void democ() throws Exception
    {
        waitForSecs(0.3f);

        setButtonActiveSfx(true);

        waitForSecs(0.3f);

        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.7f, 1.05f) , objectDict.get("speechbutton").frame()),-15,-1,true);
        playAudioQueuedScene("DEMO",true);

        waitForSecs(0.4f);
        thePointer.hide();

        nextScene();
    }

    public void playStorySentence(int sno) throws Exception
    {
        String fn = String.format("%s_%s_p_p_%d",audioPrefix,currPassage.passageID,sno + 1);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public void speakStory() throws Exception
    {
        OC_PrepR3.Est3_Passage story = storyDict.get(storyID);
        takeSequenceLockInterrupt(true);
        sequenceLock.unlock();
        storyPlays++;
        setButtonSpeakingSfx(false);
        waitForSecs(0.4f);

        //startPulse();
        deployThrobAnim();
        for(int i = 0;i < story.sentences.size();i++)
        {
            playStorySentence(i);
            waitForSecs(0.5f);
        }
        killAnimations();
        //stopPulse();
    }

    public void arrowDemo() throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        waitForSecs(0.3f);
        if(storyPlays < maxStoryPlays)
        {
            PointF destpoint = OB_Maths.locationForRect(new PointF(0.6f,1.05f) ,objectDict.get("speechbutton").frame());
            PointF startpt = pointForDestPoint(destpoint,2);
            loadPointerStartPoint(startpt,destpoint);
            movePointerToPoint(destpoint,-1,true);
            playAudioScene("DEMO",0,true);
            destpoint = OB_Maths.locationForRect(new PointF(0f,0.5f) ,objectDict.get("arrow").frame());
            movePointerToPoint(destpoint,-2,-1,true);
            playAudioScene("DEMO",1,true);
            setReplayAudio((List)currentAudio("PROMPT2.REPEAT"));
        }
        else
        {
            PointF destpoint = OB_Maths.locationForRect(new PointF(0f,0.5f) ,objectDict.get("arrow").frame());
            PointF startpt = pointForDestPoint(destpoint,-2);
            loadPointerStartPoint(startpt,destpoint);
            movePointerToPoint(destpoint,-1,true);
            playAudioQueuedScene("DEMO2",true);
            setReplayAudio((List)currentAudio("PROMPT3.REPEAT"));
        }
        waitForSecs(0.4f);
        thePointer.hide();
    }

    public void arrowStage() throws Exception
    {
        if(storyPlays < maxStoryPlays)
            setButtonActiveSfx(true);
        else
            setButtonInactiveSfx(true);
        waitForSecs(0.9f);
        OBControl arrow = objectDict.get("arrow");
        if(arrow.hidden())
        {
            playSfxAudio("arrowon",false);
            showControls("arrow");
            waitForSecs(0.2f);
            waitSFX();
        }
        arrowDemo();
        targets = Arrays.asList(arrow);
        if(storyPlays < maxStoryPlays)
            setStatus(STATUS_AWAITING_CLICK2);
        else
            setStatus(STATUS_AWAITING_ARROW_CLICK);
        flashArrow(statusTime());
    }


    public void checkButton()
    {
        try
        {
            setButtonSpeakingSfx(false);
            growCircle();
            playSfxAudio("buttonpress",false);
            objectDict.get("arrow").hide();
            waitForSecs(1f);
            waitSFX();
            speakStory();
            shrinkCircle();
            waitForSecs(0.4f);
            arrowStage();

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
                OC_PrepR3.Est3_Question qu = currPassage.questions.get(questionNo);
                for(Integer n : qu.sentenceIndices)
                {
                    int nn = n.intValue();
                    playStorySentence(nn - 1);
                    waitForSecs(0.3f);
                }
                waitForSecs(1f);
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
        if(status() == STATUS_WAITING_FOR_BUTTON_CLICK)
        {
            if(objectDict.get("speechbutton").frame() .contains(pt.x,pt.y))
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton();
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_CLICK2)
        {
            if(objectDict.get("speechbutton").frame() .contains(pt.x,pt.y))
            {
                setStatus(STATUS_CHECKING);
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
                        checkAnswer((OBControl)targ);
                    }
                });
            }
        }
    }

}
