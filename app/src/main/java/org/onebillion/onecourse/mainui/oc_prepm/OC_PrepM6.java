package org.onebillion.onecourse.mainui.oc_prepm;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 11/06/2018.
 */

public class OC_PrepM6 extends OC_SectionController
{
    boolean multiplyMode;
    Map<String,Integer> eventColours;
    List<String> currentQuestionAudio;
    List<OBControl> currentButtons, feedbackObjects, feedbackCounters;
    String audioAnswer;
    int maxNum, score, correctAnswer;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        OBPath button = (OBPath)objectDict.get("button");
        button.sizeToBoundingBoxInset(-button.lineWidth() - button.getShadowOffsetY());
        ((OBPath)objectDict.get("line")).sizeToBoundingBoxIncludingStroke();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        eventColours = OBMisc.loadEventColours(this);
        objectDict.get("wrong_cross").setZPosition(10);
        score = 0;
        setSceneXX(currentEvent());
    }

    @Override
    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception {
                startScene();
            }
        });
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        String mode = eventAttributes.get("mode");
        multiplyMode = mode.equals("mul");
        List<String> eq1Arr = Arrays.asList(eventAttributes.get("str").split(","));
        List<String> eq2Arr = Arrays.asList(eventAttributes.get(mode).split(","));
        eq1Arr = OBUtils.randomlySortedArray(eq1Arr);
        eq2Arr = OBUtils.randomlySortedArray(eq2Arr);
        int num1 = OBUtils.getIntValue(eq1Arr.get(0));
        int num2 = OBUtils.getIntValue(eq2Arr.get(0));
        maxNum = OBUtils.getIntValue(eventAttributes.get("max"));
        correctAnswer = multiplyMode ?(num1 * num2) : num2/num1;
        List<Integer> choices = new ArrayList<>();
        for(int i=correctAnswer-4; i<=correctAnswer+4; i++)
        {
            if(i!=correctAnswer && i>0)
                choices.add(i);
        }
        currentButtons = new ArrayList<>();
        List<Integer> randChoices = OBUtils.randomlySortedArray(choices);
        List<Integer> randAnswers = OBUtils.randomlySortedArray(Arrays.asList(randChoices.get(0) ,randChoices.get(1) ,correctAnswer));
        OBControl button = objectDict.get("button");
        for(int i=0; i<randAnswers.size(); i++)
        {
            int num = randAnswers.get(i);
            OBControl buttonCopy = button.copy();
            OBLabel numLabel = new OBLabel(String.format("%d", num) , OBUtils.StandardReadingFontOfSize(60));
            numLabel.setColour(Color.BLACK);
            numLabel.setPosition(buttonCopy.position());
            numLabel.show();
            numLabel.setZPosition(2);
            buttonCopy.show();
            buttonCopy.setZPosition(1);
            OBGroup group = new OBGroup(Arrays.asList(numLabel, buttonCopy));
            group.objectDict.put("label",numLabel);
            group.hide();
            attachControl(group);
            currentButtons.add(group);
            group.setPosition (OB_Maths.locationForRect(0.2f +(0.6f/(randAnswers.size()+1)) *(i+1),0.9f,this.bounds()));
            group.setProperty("correct", num == correctAnswer);
        }
        feedbackObjects = new ArrayList<>();
        feedbackCounters = new ArrayList<>();
        OBControl counter = objectDict.get("counter");
        if(multiplyMode)
        {
            OBPath line =(OBPath)objectDict.get("line");
            for(int i=0; i<num1; i++)
            {
                OBPath lineCopy = (OBPath)line.copy();
                lineCopy.setPosition (OB_Maths.locationForRect(0.5f,0.15f +(0.7f/(num1+1.0f)) *(i+1),objectDict.get("work_rect") .frame()));
                lineCopy.hide();
                lineCopy.setZPosition(1);
                attachControl(lineCopy);
                feedbackObjects.add(lineCopy);
                for(int j=0; j<num2; j++)
                {
                    OBControl counterCopy = counter.copy();
                    counterCopy.setPosition (OB_Maths.locationForRect((1/(num2+1.0f))*(j+1),0.5f,lineCopy.frame()));
                    counterCopy.hide();
                    counterCopy.setZPosition(2);
                    attachControl(counterCopy);
                    feedbackCounters.add(counterCopy);
                }
            }
        }
        else
        {
            List<List<OBControl>> countersDist = new ArrayList<>();
            for(int i=0; i<num1; i++)
            {
                OBControl child = objectDict.get(String.format("child_%d",i+1));
                feedbackObjects.add(child);
                PointF loc = OB_Maths.locationForRect(0.1f,(1.0f/(num1+1))*(i+1),objectDict.get("work_rect").frame());
                child.setProperty("dest_loc",loc);
                PointF startLoc = OBMisc.copyPoint(loc);
                startLoc.x = OB_Maths.locationForRect(0.2f,0.5f,objectDict.get("work_rect").frame()).x;
                countersDist.add(new ArrayList<OBControl>());
                for(int j=0; j<correctAnswer; j++)
                {
                    OBControl counterCopy = counter.copy();
                    startLoc.x += counterCopy.width() *1.1f;
                    counterCopy.setProperty("dest_loc",OBMisc.copyPoint(startLoc));
                    counterCopy.hide();
                    attachControl(counterCopy);
                    countersDist.get(countersDist.size()-1).add(counterCopy);
                }
            }
            for(int i=0; i<correctAnswer; i++)
            {
                for(int j=0; j<countersDist.size(); j++)
                {
                    OBControl counterObj = countersDist.get(j) .get(i);
                    feedbackCounters.add(counterObj);
                    counterObj.setPosition(objectDict.get(String.format("loc_%d",(int)(i*countersDist.size()) +j+1)).position());
                }
            }
        }
        currentQuestionAudio = new ArrayList<>();
        currentQuestionAudio.add(audioForQuestion(String.format("%s_str_%d", currentEvent() , num1)));
        currentQuestionAudio.add(audioForQuestion(String.format("%s_%s_%d", currentEvent() ,mode, num2)));
        currentQuestionAudio.add(audioForQuestion(String.format("%s_end", currentEvent())));
        audioAnswer = null;
        List<String> ansAud = getAudioForScene(String.format("%s_ans_%d", currentEvent() , correctAnswer) ,"DEMO");
        if(ansAud != null && ansAud.size()>0)
            audioAnswer = getAudioForScene(String.format("%s_ans_%d", currentEvent() , correctAnswer) ,"DEMO") .get(0);
    }

    @Override
    public void doMainXX() throws Exception
    {
        startScene();
    }

    @Override
    public void fin()
    {
        showScoreHammerScore(score*2,false);
    }

    @Override
    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,currentButtons,pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception {
                        checkTarget((OBGroup)targ);
                    }
                });
            }
            else
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        playAudio(null);
                        if(objectDict.get("q_image").frame().contains(pt.x, pt.y))
                            playAudioQueued(OBUtils.insertAudioInterval(currentQuestionAudio, 600));
                    }
                });
            }
        }
    }


    public String audioForQuestion(String question)
    {
        if(audioScenes.containsKey(question))
        {
            return getAudioForScene(question,"DEMO") .get(0);
        }
        return "";
    }

    public void startScene() throws Exception
    {
        waitForSecs(0.5f);
        lockScreen();
        for(OBControl con : feedbackObjects)
            con.show();
        playSfxAudio("obj_show",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        List<Object> audios = OBUtils.insertAudioInterval(currentQuestionAudio, 600);
        setReplayAudio(audios);
        playAudioQueued(audios,true);
        lockScreen();
        for(OBControl con : currentButtons)
            con.show();
        playSfxAudio("num_show",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkTarget(OBGroup targ) throws Exception
    {
        OBLabel label =(OBLabel)targ.objectDict.get("label");
        label.setColour(eventColours.get("highlight"));
        if((boolean)targ.propertyValue("correct"))
        {
            gotItRightBigTick(false);
            score++;
            waitForSecs(0.5f);
            if(multiplyMode)
            {
                lockScreen();
                for(OBControl con : feedbackCounters)                    con.show();
                playSfxAudio("counter_show",false);
                unlockScreen();
                waitSFX();
                waitForSecs(0.5f);
            }
            playAudio(audioAnswer);
            waitForAudio();
            waitForSecs(1f);
            if(eventIndex !=(events.size()-1))
            {
                lockScreen();
                for(OBControl con : feedbackCounters)
                    detachControl(con);
                for(OBControl con : feedbackObjects)
                    detachControl(con);
                for(OBControl con : currentButtons)
                    detachControl(con);
                unlockScreen();
            }
            nextScene();
        }
        else
        {
            objectDict.get("wrong_cross").show();
            gotItWrongWithSfx();
            waitForSecs(1.5f);
            lockScreen();
            for(OBControl con : currentButtons)
                con.hide();
            objectDict.get("wrong_cross").hide();
            unlockScreen();
            waitForSecs(0.5f);
            if(multiplyMode)
                demoFeedbackMultiply();
            else
                demoFeedbackDivide();
        }
    }

    public void demoFeedbackMultiply() throws Exception
    {
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("work_rect") .frame()),-30,0.5f,true);
        playAudio(currentQuestionAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        movePointerToPoint(OB_Maths.locationForRect(1.1f,0.5f,feedbackObjects.get(0).frame()),-15,0.5f,true);
        playAudio(currentQuestionAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : feedbackCounters)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,con.frame()),-20,0.3f,true);
            waitForSecs(0.1f);
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,con.frame()),-20,0.2f,true);
            playSfxAudio("counter_show",false);
            con.show();
            waitSFX();
            movePointerToPoint(OB_Maths.locationForRect(0.5f,1.1f,con.frame()),-20,0.2f,true);
            waitForSecs(0.2f);
        }
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("work_rect") .frame()),-30,0.5f,true);
        playAudio(audioAnswer);
        waitForAudio();
        waitForSecs(6f);
        fin();
    }

    public void demoFeedbackDivide() throws Exception
    {
        List<OBAnim> anims = new ArrayList<>();
        for(OBControl con : feedbackObjects)
        {
            anims.add(OBAnim.scaleAnim(0.9f/feedbackObjects.size() * con.scale(),con));
            anims.add(OBAnim.moveAnim((PointF)con.propertyValue("dest_loc") ,con));
        }
        OBAnimationGroup.runAnims(anims,0.7,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        waitForSecs(0.5f);
        loadPointer(POINTER_LEFT);
        movePointerToPoint(OB_Maths.locationForRect(0.1f,0.9f,objectDict.get("work_rect") .frame()),-30,0.5f,true);
        playAudio(currentQuestionAudio.get(0));
        waitForAudio();
        waitForSecs(0.3f);
        lockScreen();
        for(OBControl con : feedbackCounters)            con.show();
        playSfxAudio("counter_show",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.5f);
        movePointerToPoint(OB_Maths.locationForRect(1f,0.85f,objectDict.get("work_rect") .frame()),-30,0.5f,true);
        playAudio(currentQuestionAudio.get(1));
        waitForAudio();
        waitForSecs(0.3f);
        for(OBControl con : feedbackCounters)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.5f,0.5f,con.frame()),-20,0.3f,true);
            waitForSecs(0.1f);
            PointF loc = (PointF)con.propertyValue("dest_loc");
            OBMisc.moveControlWithAttached(con,Arrays.asList(thePointer),loc,0.3f,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
            playSfxAudio("counter_show",true);
        }
        movePointerToPoint(OB_Maths.locationForRect(0.9f,0.9f,objectDict.get("work_rect") .frame()),-30,0.5f,true);
        playAudio(audioAnswer);
        waitForAudio();
        waitForSecs(6f);
        fin();
    }
}
