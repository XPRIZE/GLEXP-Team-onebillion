package com.maq.xprize.onecourse.mainui.oc_caps;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import com.maq.xprize.onecourse.controls.*;
import com.maq.xprize.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import com.maq.xprize.onecourse.utils.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 02/01/2018.
 */

public class OC_CapsFirst extends OC_Wordcontroller
{
    public static int STATUS_AWAITING_PIC_CLICK = 1008,
    STATUS_AWAITING_WORD_CLICK = 1009;
    List<String> persons;
    float fontSize;
    OBLabel mainLabel;
    OBLabel capLabel,lcLabel,bodyLabel;
    OBControl picBox,textBox;
    OBImage mainPic;
    Map<String,OBPhoneme> wordDict;
    float labelRight;
    String currentCode;
    OBPresenter presenter;
    OBConditionLock audioLock;

    static String CapitaliseString(String s,boolean cap)
    {
        String s1 = s.substring(0,1);
        String s2 = s.substring(1);
        if(cap)
            s1 = s1.toUpperCase();
        else
            s1 = s1.toLowerCase();
        return s1 + s2;
    }

    public void miscSetUp()
    {
        loadEvent("mastera");
        String s;
        if((s = parameters.get("persons")) != null)
            persons = Arrays.asList(s.split(";"));
        if((s = eventAttributes.get("textsize")) != null)
            fontSize = Float.parseFloat(s);
        wordDict = OBUtils.LoadWordComponentsXML(true);

        (picBox = objectDict.get("picbox")).hide();
        (textBox = objectDict.get("textbox")).hide();
        currNo = 0;
        events = Arrays.asList("a,b,c,d,e,f".split(","));
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_PIC_CLICK);
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        miscSetUp();
        doVisual(currentEvent());
    }

    public void setUpLabel(String tx)
    {
        OBFont font = OBUtils.StandardReadingFontOfSize(fontSize);
        mainLabel = new OBLabel(tx,font);
        mainLabel.setColour(Color.BLACK);
        mainLabel.setPosition(textBox.position());
        labelRight = mainLabel.right();
        PointF pos = new PointF();
        pos.set(mainLabel.position());

        String capS1 = tx.substring(0,1);
        String lcS1 = capS1.toLowerCase();
        String body = tx.substring(1);

        bodyLabel = new OBLabel(body,font);
        bodyLabel.setPosition(mainLabel.position());
        bodyLabel.setRight(mainLabel.right());
        bodyLabel.setColour(Color.BLACK);
        attachControl(bodyLabel);

        capLabel = new OBLabel(capS1,font);
        capLabel.setPosition(mainLabel.position());
        capLabel.setLeft(mainLabel.left());
        capLabel.setColour(Color.BLACK);
        attachControl(capLabel);

        mainLabel.setString(CapitaliseString(tx, false));
        mainLabel.sizeToBoundingBox();
        mainLabel.setPosition(pos);
        mainLabel.setRight(labelRight);

        lcLabel = new OBLabel(lcS1,font);
        lcLabel.setPosition(mainLabel.position());
        lcLabel.setLeft(mainLabel.left());
        lcLabel.setColour(Color.BLACK);
        attachControl(lcLabel);

        capLabel.hide();
        lcLabel.hide();
        bodyLabel.hide();
    }

    public void setUpScene()
    {
        if(mainPic != null)
            detachControl(mainPic);
        if(lcLabel != null)
            detachControl(lcLabel);
        if(capLabel != null)
            detachControl(capLabel);
        if(bodyLabel != null)
            detachControl(bodyLabel);
        String s = persons.get(currNo);
        List<String> arr = Arrays.asList(s.split(","));
        currentCode = arr.get(0).trim();
        OBWord orw = (OBWord) wordDict.get(currentCode);
        setUpLabel(orw.text);
        String picname = arr.get(1).trim();
        mainPic = loadImageWithName(picname,new PointF(0.5f, 0.5f),boundsf());

        scalePic(mainPic,picBox);
        mainPic.hide();
    }

    public void setScenea()
    {
        super.setSceneXX(currentEvent());
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF restpos = new PointF();
        restpos.set(presenter.control.position());
        presenter.control.setProperty("restpos",restpos);
        presenter.control.setRight(0);
        presenter.control.show();
    }

    public void setSceneXX(String scene)
    {
        setUpScene();
        targets = Arrays.asList((OBControl)mainPic);
    }

    public void doAudio(String prompt) throws Exception
    {
        setReplayAudio((List<Object>)(Object)currentAudio(String.format("%s.REPEAT",prompt)));
        audioLock = playAudioQueuedScene(prompt,false);
    }

    public void doMainXX() throws Exception
    {
        showPic();
        doAudio("PROMPT");
        waitForSecs(0.3f);
    }

    public void doMain2() throws Exception
    {
        waitForSecs(0.3f);
        doAudio("PROMPT2");
        waitForSecs(0.3f);
    }

    public void doBody2() throws Exception
    {
        doMain2();
        targets = Arrays.asList((OBControl)mainLabel);
        setStatus(STATUS_AWAITING_WORD_CLICK);
        endBody2();
    }

    public void flashPic(long sttime) throws Exception
    {
        float radius = applyGraphicScale(13);
        try
        {
            for(int i = 0;i < 2 && !statusChanged(sttime);i++)
            {
                lockScreen();
                mainPic.setShadow(radius,1,0,0,Color.RED);
                unlockScreen();
                waitForSecs(0.4f);
                lockScreen();
                mainPic.setShadow(0,0,0,0,Color.TRANSPARENT);
                unlockScreen();
                waitForSecs(0.4f);
            }
        }
        catch(Exception exception)
        {
        }

    }

    public void remindPics(final long sttime,final float secs)throws Exception
    {
        if(statusChanged(sttime))
            return;
        flashPic(sttime);
        OBUtils.runOnOtherThreadDelayed(secs,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                remindPics(sttime,secs);
            }
        });
    }

    public void setColourForLabels(int col) throws Exception
    {
        lockScreen();
        for(OBLabel lab : Arrays.asList(lcLabel,capLabel,bodyLabel))
            lab.setColour(col);
        unlockScreen();
    }
    public void flashLabels(long sttime) throws Exception
    {
        try
        {
            for(int i = 0;i < 3 && !statusChanged(sttime);i++)
            {
                setColourForLabels(Color.RED);
                waitForSecs(0.2f);
                setColourForLabels(Color.BLACK);
                waitForSecs(0.2f);
            }
        }
        catch(Exception exception)
        {
        }

    }

    public void remindLabels(final long sttime,final float secs) throws Exception
    {
        if(statusChanged(sttime))
            return;
        flashLabels(sttime);
        OBUtils.runOnOtherThreadDelayed(secs,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                remindLabels(sttime,secs);
            }
        });
    }

    public void endBody()
    {
        try
        {
            if(currentAudio("PROMPT.REMINDER") != null)
            {
                final long stt = statusTime();
                waitForSecs(0.2f);
                waitAudioQueue(audioLock);
                reprompt(stt,(List<Object>)(Object)currentAudio("PROMPT.REMINDER"),5,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        remindPics(stt,5);
                    }
                });
            }
            else
            {
                final long tin = statusTime();
                waitAudioQueue(audioLock);
                OBUtils.runOnOtherThreadDelayed(5,new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        remindPics(tin,5);
                    }
                });
            }
        }
        catch(Exception e)
        {

        }
    }

    public void endBody2() throws Exception
    {
        if(currentAudio("PROMPT2.REMINDER") != null)
        {
            final long stt = statusTime();
            waitForSecs(0.2f);
            waitAudio();
            reprompt(stt,(List<Object>)(Object)currentAudio("PROMPT2.REMINDER"),5,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    remindLabels(stt,5);
                }
            });
        }
        else
        {
            final long tin = statusTime();
            OBUtils.runOnOtherThreadDelayed(5,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    remindLabels(tin,5);
                }
            });
        }
    }

    public void showPic() throws Exception
    {
        if(mainPic.hidden())
        {
            mainPic.show();
            playSfxAudio("picon",true);
        }
    }

    public void demoa() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(1f);
        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = presenter.control.position();
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds().width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.2f);
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
        waitForSecs(0.3f);
        showPic();
        waitForSecs(0.3f);
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.5f, 1f) ,mainPic.frame());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.3f);

        playAudioScene("DEMO",0,true);
        waitForSecs(0.6f);
        playAudioScene("DEMO",1,true);
        waitForSecs(0.2f);
        movePointerToPoint(OB_Maths.locationForRect(new PointF(0.8f, 0.8f) , bounds()),-1,true);
        playAudioScene("DEMO",2,true);
        waitForSecs(0.6f);

        thePointer.hide();
        doBody(currentEvent());
    }

    public void demob2() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.5f, 1f) ,mainLabel.frame());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.5f);
        thePointer.hide();
        doBody2();

    }

    public void demoendb() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.5f, 1f) ,capLabel.frame());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.5f);
        lockScreen();
        capLabel.setColour(Color.RED);
        unlockScreen();

        playAudioScene("DEMOFIN",0,true);
        lockScreen();
        capLabel.setColour(Color.BLACK);
        unlockScreen();
        waitForSecs(0.3f);

        destpoint = OB_Maths.locationForRect(new PointF(1.2f, 1f) ,bodyLabel.frame());
        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.2f);
        playAudioScene("DEMOFIN",1,true);

        waitForSecs(0.6f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demoendc() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.5f, 1f) ,capLabel.frame());
        PointF startpt = pointForDestPoint(destpoint,30);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        lockScreen();
        capLabel.setColour(Color.RED);
        unlockScreen();
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMOFIN",true);
        lockScreen();
        capLabel.setColour(Color.BLACK);
        unlockScreen();
        waitForSecs(0.3f);
        thePointer.hide();
        waitForSecs(1f);
    }

    public void demoendd() throws Exception
    {
        demoendc();
    }

    public void demoende() throws Exception
    {
        demoendc();
    }

    public void demoendf() throws Exception
    {
        demoendc();
    }

    public void animLetterOff() throws Exception
    {
        lcLabel.hide();
    }

    public void animLetterOn() throws Exception
    {
        PointF pt = capLabel.position();
        lockScreen();
        capLabel.setScale(2.2f);
        capLabel.show();
        unlockScreen();
        OBAnim scaleAnim = OBAnim.scaleAnim(1.0f,capLabel);
        OBAnimationGroup.runAnims(Arrays.asList(scaleAnim),0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        lockScreen();
        capLabel.setScale(1.0f);
        capLabel.setPosition(pt);
        unlockScreen();
    }


    public void fin()
    {
        String l = parameters.get("letters");
        try
        {
            Class cnm = Class.forName("com.maq.xprize.onecourse.mainui.oc_caps.OC_CapsMore");
            goToCard(cnm,String.format("capsmore;capsfirst/letters=%s",l),false);
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public void finishEvent() throws Exception
    {
        waitForSecs(1.5f);
        performSel("demoend",currentEvent());
        currNo++;
        waitForSecs(1.5f);
        nextScene();
    }

    public void checkLabelTarget(Object targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            waitForSecs(0.2f);
            playSfxAudio("letteroff",false);
            animLetterOff();
            waitForSecs(0.3f);
            playSfxAudio("capon",false);
            animLetterOn();

            waitForSecs(0.3f);
            lockScreen();
            bodyLabel.setColour(Color.RED);
            capLabel.setColour(Color.RED);
            unlockScreen();
            playAudioQueued(Arrays.asList((Object)currentCode),true);
            lockScreen();
            capLabel.setColour(Color.BLACK);
            bodyLabel.setColour(Color.BLACK);
            unlockScreen();
            finishEvent();
        }
        catch(Exception exception)
        {
        }
    }

    public void nextSubEvent()throws Exception
    {
        if(currentEvent().equals("b"))
            demob2();
        else
            doBody2();
    }

    public void checkPicTarget(Object targ)
    {
        setStatus(STATUS_CHECKING);
        try
        {
            playSfxAudio("nameshow",false);
            lockScreen();
            bodyLabel.show();
            lcLabel.show();
            unlockScreen();
            waitForSecs(0.3f);
            lockScreen();
            bodyLabel.setColour(Color.RED);
            lcLabel.setColour(Color.RED);
            unlockScreen();
            playAudioQueued(Arrays.asList((Object)currentCode),true);
            lockScreen();
            lcLabel.setColour(Color.BLACK);
            bodyLabel.setColour(Color.BLACK);
            unlockScreen();
            nextSubEvent();
        }
        catch(Exception exception)
        {
        }
    }

    public Object findTarget(PointF pt)
    {
        OBControl c =finger(0,2,targets,pt);
        return c;
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_PIC_CLICK)
        {
            final Object obj = findTarget(pt);
            if(obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkPicTarget(obj);
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_WORD_CLICK)
        {
            final Object obj = findTarget(pt);
            if(obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkLabelTarget(obj);
                    }
                });
            }
        }

    }

}
