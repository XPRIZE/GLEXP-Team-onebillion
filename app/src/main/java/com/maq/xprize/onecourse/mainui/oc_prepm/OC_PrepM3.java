package com.maq.xprize.onecourse.mainui.oc_prepm;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.oc_numberlines.OC_Numberlines_Additions;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBMisc;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 18/01/2018.
 */

public class OC_PrepM3 extends OC_PrepMWithVideo
{
    List<List<Integer> > equationList;
    boolean additionMode;
    Map<String,Integer> eventColours;
    int score;
    int maxNum;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
        eventColours = OBMisc.loadEventColours(this);
        additionMode = parameters.get("mode").equals("add");
        maxNum = OBUtils.getIntValue(parameters.get("max"));
        loadEquationList();
        events = new ArrayList<>();
        for(int i=1; i<=equationList.size(); i++)
        {
            String eventName = String.format("%d",i);
            if(audioScenes.get(eventName) != null)
            {
                events.add(eventName);
            }
            else
            {
                events.add("default");
            }
        }
        score = 0;

        setSceneXX(currentEvent());
        OBPath line =(OBPath) objectDict.get("line");
        line.sizeToBoundingBoxIncludingStroke();

    }

    public void start()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                waitForSecs(0.5f);
                startScene();
            }
        });
    }

    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        List<Integer> eq = currentEquationValues();
        int num1 = eq.get(0) , num2 = eq.get(1);
        loadEquationAndNumbers(num1,num2,maxNum,additionMode);
    }

    public void doMainXX() throws Exception
    {
        startScene();
    }

    public void fin()
    {
        showScoreHammerScore(score,false);
    }

    public void touchDownAtPoint(PointF pt, View v)
    {
        if (status() == STATUS_AWAITING_CLICK)
        {
            final OBControl targ = finger(0, 1, (List<OBControl>)(Object)eventTargets, pt);
            if (targ != null)
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
        }
    }


    public void startScene() throws Exception
    {
        showCurrentEquation();
        waitForSecs(0.3f);
        lockScreen();
        for(OBControl con : eventTargets)
            con.show();
        objectDict.get("bottom_bar").show();
        playSfxAudio("numbers",false);
        unlockScreen();
        waitSFX();
        waitForSecs(0.3f);
        OBMisc.doSceneAudio(4,setStatus(STATUS_AWAITING_CLICK),this);
    }

    public void checkTarget(OBLabel targ) throws Exception
    {
        targ.setColour(eventColours.get("highlight"));
        if((boolean)targ.propertyValue("correct"))
        {
            gotItRightBigTick(false);
            score++;
            waitForSecs(0.3f);
            lockScreen();
            OC_Numberlines_Additions.showEquation(currentEquation,5,5,null,this);
            objectDict.get("line").hide();
            objectDict.get("bottom_bar").hide();
            for(OBControl con : eventTargets)
                con.hide();
            playSfxAudio("equation",false);
            unlockScreen();
            waitSFX();
            waitForSecs(1f);
            if(eventIndex !=(events.size()-1))
            {
                PointF loc = OBMisc.copyPoint(currentEquation.position());
                loc.x  = 1.5f * bounds().width();
                playSfxAudio("slide",false);
                OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(loc,currentEquation)),0.5,true,OBAnim.ANIM_EASE_IN,this);
                waitSFX();
                waitForSecs(0.3f);
            }
            nextScene();
        }
        else
        {
            if(shouldCollectMiscData())
            {
                List<Integer> eq = currentEquationValues();
                int num1 = eq.get(0) , num2 = eq.get(1);
                String userAnswer = String.format("%d %s %d = %s",num1,additionMode ? "+" : "–",num2,targ.text());
                collectMiscData("wrong",userAnswer);
            }
            objectDict.get("wrong_cross").show();
            gotItWrongWithSfx();
            waitForSecs(0.3f);
            demoCount();
        }
    }

    public void showCurrentEquation() throws Exception
    {
        lockScreen();
        OC_Numberlines_Additions.showEquation(currentEquation,1,4,null,this);
        OBPath line =(OBPath) objectDict.get("line");
        RectF rect1 = currentEquation.objectDict.get("part4").getWorldFrame();
        RectF rect2 = currentEquation.objectDict.get("part5").getWorldFrame();
        float left = rect1.left + rect1.width() + applyGraphicScale(20);
        line.setPosition(OB_Maths.locationForRect(new PointF(0.5f, 1.05f) ,rect2));
        line.setLeft(left);
        //line.setWidth(2*(line.position.x-left));
        line.show();
        OBControl wrongCross = objectDict.get("wrong_cross");
        wrongCross.setPosition(line.position());
        wrongCross.setBottom(line.top() - applyGraphicScale(4));
        playSfxAudio("equation",false);
        unlockScreen();
        waitSFX();
    }

    public List<Integer> currentEquationValues()
    {
        return equationList.get(eventIndex);
    }

    public void loadEquationList()
    {
        equationList = new ArrayList<>();
        if(maxNum == 10)
        {
            if(additionMode)
            {
                addEquationToList(OB_Maths.randomInt(2,5),1);
                addEquationToList(randomNumFromArray(Arrays.asList(2,4)),2);
                addEquationToList(randomNumFromArray(Arrays.asList(3,5 ,6,7)),2);
                addEquationToList(OB_Maths.randomInt(3,5),OB_Maths.randomInt(3,5));
                int num1, num2;
                do
                {
                    num1 = OB_Maths.randomInt(3,5);

                }
                while(!addEquationToList(num1,num1));
                addEquationToList(OB_Maths.randomInt(6,8),1);
                do
                {
                    num1 = randomNumFromArray(Arrays.asList(4,6));

                }
                while(!addEquationToList(num1,4));
                do
                {
                    num1 = randomNumFromArray(Arrays.asList(2,4));
                    num2 = randomNumFromArray(Arrays.asList(3,5));

                }
                while(!addEquationToList(num1,num2));
                do
                {
                    num1 = OB_Maths.randomInt(2,9);

                }
                while(!addEquationToList(num1,10-num1));
                do
                {
                    num1 = OB_Maths.randomInt(2,9);

                }
                while(!addEquationToList(num1,OB_Maths.randomInt(1,10-num1)));

            }
            else
            {
                addEquationToList(OB_Maths.randomInt(2,5),1);
                addEquationToList(OB_Maths.randomInt(3,5),2);
                addEquationToList(OB_Maths.randomInt(6,9),3);
                addEquationToList(OB_Maths.randomInt(6,9),5);
                addEquationToList(OB_Maths.randomInt(6,9),4);
                addEquationToList(OB_Maths.randomInt(6,9),1);
                addEquationToList(randomNumFromArray(Arrays.asList(8,10)),2);
                int num;
                do
                {
                    num = randomNumFromArray(Arrays.asList(3,5,7));

                }
                while(!addEquationToList(9,num));
                do
                {
                    num = OB_Maths.randomInt(1, 9);

                }
                while(!addEquationToList(10,num));
                do
                {
                    num = OB_Maths.randomInt(2, 9);

                }
                while(!addEquationToList(num,OB_Maths.randomInt(1, num-1)) );

            }

        }
        else if(maxNum == 20)
        {
            if(additionMode)
            {
                addEquationToList(randomNumFromArray(Arrays.asList(11 ,12)),randomNumFromArray(Arrays.asList(2,4)));
                addEquationToList(randomNumFromArray(Arrays.asList(13 ,14)),randomNumFromArray(Arrays.asList(2,4)));
                addEquationToList(OB_Maths.randomInt(15,17),2);
                addEquationToList(OB_Maths.randomInt(6,9),OB_Maths.randomInt(3,5));
                int num = OB_Maths.randomInt(7,9);
                addEquationToList(num,OB_Maths.randomInt(6, num-1));
                do
                {
                    num = OB_Maths.randomInt(2, 9);
                }
                while(!addEquationToList(num,OB_Maths.randomInt(10-num, num-1)));

                do
                {
                    num = OB_Maths.randomInt(5, 10);
                }
                while(!addEquationToList(num,num));

                do
                {
                    num = OB_Maths.randomInt(5, 8);
                }
                while(!addEquationToList(num,num+1));

                do
                {
                    num = OB_Maths.randomInt(2,9);
                }
                while(!addEquationToList(randomNumFromArray(Arrays.asList(5 ,8 ,10)),5));

                do
                {
                    num = OB_Maths.randomInt(2,9);
                }
                while(!addEquationToList(2*OB_Maths.randomInt(1,4),10));

            }
            else
            {
                addEquationToList(OB_Maths.randomInt(11,20),1);
                addEquationToList(OB_Maths.randomInt(12,20),2);
                addEquationToList(OB_Maths.randomInt(14,20),4);
                addEquationToList(OB_Maths.randomInt(13,20),3);
                addEquationToList((OB_Maths.randomInt(1,5) *2) +10,10);
                int num1,num2;
                do
                {
                    num1 = OB_Maths.randomInt(0,5);
                }
                while(!addEquationToList((num1*2) +10,OB_Maths.randomInt(num1+1, num1+3>6?6:num1+3)*2));

                do
                {
                    num1 = OB_Maths.randomInt(0,5);
                    num2 =(OB_Maths.randomInt(num1+1, num1+3>6?6:num1+3) *2) +1;
                }
                while(!addEquationToList((num1*2) +10,num2));

                do
                {
                    num1 = OB_Maths.randomInt(0,5);
                    num2 = OB_Maths.randomInt(num1+1, num1+3>6?6:num1+3)*2;
                }
                while(!addEquationToList((num1*2) +10,num2));

                for(int i=0; i<2; i++)
                {
                    do
                    {
                        num1 = OB_Maths.randomInt(11, 20);
                        num2 = OB_Maths.randomInt(1, 9);
                    }
                    while(!addEquationToList(num1,num2));
                }
            }
        }
    }

    public int randomNumFromArray(List<Integer> arr)
    {
        return arr.get(OB_Maths.randomInt(0,arr.size()-1));
    }

    public boolean addEquationToList(int num1,int num2)
    {
        boolean addRow = true;
        for(int i=0; i<equationList.size(); i++)
        {
            if(equationList.get(i).get(0) == num1 &&
                equationList.get(i).get(1) == num2)
            {
                addRow = false;
                break;
            }
        }
        if(addRow)
            equationList.add(Arrays.asList(num1, num2));

        return addRow;
    }

    public void demoCount() throws Exception
    {
        prepareCountingVideo(String.format("counting-%d",maxNum));
        waitForSecs(1f);
        lockScreen();
        for(OBControl con : eventTargets)
            con.hide();
        objectDict.get("bottom_bar").hide();
        objectDict.get("wrong_cross").hide();
        objectDict.get("line").hide();
        unlockScreen();
        waitForSecs(0.3f);
        playSfxAudio("slide",false);
        OBAnimationGroup.runAnims(Arrays.asList(OBAnim.moveAnim(objectDict.get("demo_loc").position() ,currentEquation)),0.5,true,OBAnim.ANIM_EASE_IN,this);
        waitSFX();
        waitForSecs(0.5f);
        lockScreen();
        objectDict.get("video_frame").show();
        videoPlayer.show();
        unlockScreen();
        playSfxAudio("video",true);
        waitForSecs(0.5f);
        List<String> audio = getAudioForScene(String.format("counting_%d",maxNum),"DEMO");
        if(audio != null)
        {
            playAudio(audio.get(0));
            waitAudio();
        }
        waitForSecs(0.5f);
        List<Integer> arr = currentEquationValues();
        int num1 = arr.get(0);
        int num2 = arr.get(1);
        int result = additionMode ? num1+num2 : num1-num2;
        OC_Numberlines_Additions.colourEquation(currentEquation,1,1,eventColours.get("highlight"),this);
        waitForSecs(0.3f);
        playTickVideoFomNum(0,num1,2,true);
        waitForSecs(1f);
        OC_Numberlines_Additions.colourEquation(currentEquation,1,1, Color.BLACK,this);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(currentEquation,2,3,eventColours.get("highlight"),this);
        waitForSecs(0.3f);
        playTickVideoFomNum(num1,result,2,false);
        waitForSecs(1f);
        OC_Numberlines_Additions.colourEquation(currentEquation,2,3,Color.BLACK,this);
        waitForSecs(0.3f);
        OC_Numberlines_Additions.colourEquation(currentEquation,5,5,eventColours.get("highlight"),this);
        OC_Numberlines_Additions.showEquation(currentEquation,5,5,null,this);
        playAudio(String.format("n_%d",result));
        waitForSecs(5f);
        fin();
    }
}
