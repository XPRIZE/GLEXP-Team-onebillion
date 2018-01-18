package org.onebillion.onecourse.mainui.oc_pzmath;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.ArrayMap;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by michal on 18/01/2018.
 */

public class OC_EgmaSubTest5 extends OC_EgmaWithVideo
{
    boolean additionMode;
    Map<String,Integer> eventColours;
    List<String> currentQuestionAudio;
    List<Integer> currentEquationValues;
    int maxNum, score;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        eventColours = OBMisc.loadEventColours(this);
        objectDict.get("wrong_cross").setZPosition(10);
        score = 0;
        setSceneXX(currentEvent());
    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                startScene();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        String mode = eventAttributes.get("mode");
        additionMode = mode.equals("add");
        List<String> eq1Arr = Arrays.asList(eventAttributes.get("str").split(","));
        List<String> eq2Arr = Arrays.asList(eventAttributes.get(mode).split(","));
        eq1Arr = OBUtils.randomlySortedArray(eq1Arr);
        eq2Arr = OBUtils.randomlySortedArray(eq2Arr);
        int num1 = OBUtils.getIntValue(eq1Arr.get(0)) , num2 = OBUtils.getIntValue(eq2Arr.get(1));
        currentEquationValues = Arrays.asList(num1,num2);
        maxNum = OBUtils.getIntValue(eventAttributes.get("max"));
        loadEquationAndNumbers(num1,num2,maxNum,additionMode);
        currentQuestionAudio= new ArrayList<>();
        currentQuestionAudio.add(audioForQuestion(String.format("%s_str_%d", currentEvent() , num1)));
        currentQuestionAudio.add(audioForQuestion(String.format(additionMode ? "%s_add_%d" : "%s_sub_%d", currentEvent() , num2)));
        currentQuestionAudio.add(audioForQuestion(String.format("%s_end", currentEvent())));
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        showScoreHammerScore(score*2,false);
    }

    public void touchDownAtPoint(final PointF pt, View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0,1,(List<OBControl>)(Object)eventTargets,pt);
            if(targ != null)
            {
                setStatus(STATUS_BUSY);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget((OBLabel)targ);
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
                            playAudioQueued(OBUtils.insertAudioInterval(currentQuestionAudio,300));
                    }
                } );
            }
        }
    }

    public String audioForQuestion(String question)
    {
        List<String> audio = getAudioForScene(question,"DEMO");
        if(audio != null)
        {
            return audio.get(0);
        }
        return "";
    }

    public void startScene() throws Exception
    {
        waitForSecs(0.5f);
        objectDict.get("q_image").show();
        playSfxAudio("image",true);
        waitForSecs(0.3f);
        List<Object> audio = OBUtils.insertAudioInterval(currentQuestionAudio,300);
        setReplayAudio(audio);
        playAudioQueued(audio,true);
        waitForSecs(0.3f);
        lockScreen();
        for(OBControl con : eventTargets)
            con.show();
        objectDict.get("bottom_bar").show();
        playSfxAudio("numbers",false);
        unlockScreen();
        setStatus(STATUS_AWAITING_CLICK);
    }

    public void checkTarget(OBLabel targ) throws Exception
    {
        targ.setColour(eventColours.get("highlight"));
        if((boolean)targ.propertyValue("correct"))
        {
            gotItRightBigTick(false);
            score++;
            waitForSecs(1f);
            lockScreen();
            OC_Numberlines_Additions.showEquation(currentEquation,1,5,null,this);
            objectDict.get("bottom_bar").hide();
            for(OBControl con : eventTargets)
                con.hide();
            playSfxAudio("equation",false);
            unlockScreen();
            waitSFX();
            waitForSecs(1f);
            if(eventIndex !=(events.size()-1))
            {
                OBControl image = objectDict.get("q_image");
                PointF loc = OBMisc.copyPoint(image.position());
                loc.x = 1.5f * bounds().width();
                playSfxAudio("slide",false);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc,image)),0.5,true, OBAnim.ANIM_EASE_IN,this);
                waitSFX();
                waitForSecs(0.3f);
            }
            nextScene();
        }
        else
        {
            objectDict.get("wrong_cross").show();
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            demoCount();
        }
    }

    public void demoCount() throws Exception
    {
        prepareEgmaVideo(String.format("counting-%d",maxNum));
        waitForSecs(1f);
        lockScreen();
        for(OBControl con : eventTargets)
            con.hide();
        objectDict.get("q_image").hide();
        objectDict.get("bottom_bar").hide();
        objectDict.get("wrong_cross").hide();
        unlockScreen();
        waitForSecs(0.5f);
        OC_Numberlines_Additions.showEquation(currentEquation,1,4,"equation",this);
        waitForSecs(0.3f);
        lockScreen();
        objectDict.get("video_frame").show();
        videoPlayer.show();
        unlockScreen();
        playSfxAudio("video",true);
        waitForSecs(0.5f);
        List<String> audio = getAudioForScene(String.format("counting_%d",maxNum),"DEMO");
        if(audio != null)
            playAudio(audio.get(0));
        waitAudio();
        waitForSecs(0.5f);
        int num1 = currentEquationValues.get(0).intValue();
        int num2 = currentEquationValues.get(1).intValue();
        OC_Numberlines_Additions.colourEquation(currentEquation,1,1,eventColours.get("highlight"),this);
        waitForSecs(0.3f);
        playAudio(currentQuestionAudio.get(0));
        waitAudio();
        waitForSecs(0.3f);
        playTickVideoFomNum(0,num1,2,true);
        waitForSecs(1f);
        OC_Numberlines_Additions.colourEquation(currentEquation,1,1, Color.BLACK,this);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(currentEquation,2,3,eventColours.get("highlight"),this);
        waitForSecs(0.3f);
        playAudio(currentQuestionAudio.get(1));
        waitAudio();
        waitForSecs(0.3f);
        int answear = additionMode ? num1+num2 : num1-num2;
        playTickVideoFomNum(num1,answear,1,false);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(currentEquation,2,3,Color.BLACK,this);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(currentEquation,5,5,eventColours.get("highlight"),this);
        OC_Numberlines_Additions.showEquation(currentEquation,5,5,"equation",this);
        waitForSecs(0.3f);
        playAudio(audioForQuestion(String.format("%s_ans_%d",currentEvent() , answear)));
        waitAudio();
        waitForSecs(1f);
        waitForSecs(5f);
        fin();
    }
}
