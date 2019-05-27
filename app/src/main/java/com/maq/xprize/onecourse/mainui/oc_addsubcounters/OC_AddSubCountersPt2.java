package com.maq.xprize.onecourse.mainui.oc_addsubcounters;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.controls.OBPresenter;
import com.maq.xprize.onecourse.mainui.MainActivity;
import com.maq.xprize.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import com.maq.xprize.onecourse.utils.OBAnim;
import com.maq.xprize.onecourse.utils.OBAnimationGroup;
import com.maq.xprize.onecourse.utils.OBAudioManager;
import com.maq.xprize.onecourse.utils.OBFont;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.utils.OBAudioManager.AM_MAIN_CHANNEL;
import static com.maq.xprize.onecourse.utils.OBUtils.StandardReadingFontOfSize;

public class OC_AddSubCountersPt2 extends OC_AddSubCounters
{
    float textSize;
    OBFont font;
    OBPresenter presenter;
    List<ocasc_equation> equations;
    List<OBLabel> labels;
    boolean isPlus,standalone,equaudio;

    public void buildEquationList(String inParam)
    {
        List<ocasc_equation>mEquations = new ArrayList<>();
        String[] components = inParam.split(",");
        if(TextUtils.isDigitsOnly(components[0]))
        {
            int ct = Integer.parseInt(components[0]);
            ct = 10;
            String sign = components[1];
            isPlus = sign.equals("+");
            int lnum = Integer.parseInt(components[2]);
            int rnum = Integer.parseInt(components[3]);
            int linc = Integer.parseInt(components[4]);
            int rinc = Integer.parseInt(components[5]);
            for(int i = 0;i < ct;i++)
            {
                ocasc_equation eq = new ocasc_equation();
                eq.isPlus = (isPlus);
                eq.lh = (lnum + i * linc);
                eq.rh = (rnum + i * rinc);
                mEquations.add(eq);
                if(isovernum(eq, 20))
                    equAudio = false;

            }
        }
        equations = mEquations;
    }

    public void miscSetUp()
    {
        super.miscSetUp();
        buildEquationList(parameters.get("equations"));
        loadEvent("mastera");
        textSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize") ,"42"));
        font = StandardReadingFontOfSize(textSize);
        events = new ArrayList<>(Arrays.asList("m","n","o"));
        while(events.size() < equations.size())
            events.add(events.get(events.size()-1));
        while(events.size() > equations.size())
            events.remove(events.size()-1);
        events.add("p");
        events.add(0,"mpref");
        boolean annaIntro = OBUtils.coalesce(parameters.get("annaintro") , "false").equals("true");
        standalone = OBUtils.coalesce(parameters.get("standalone") , "false").equals("true");
        if(!standalone)
            annaIntro = false;
        events.add(0,annaIntro?"k":"l");
        equaudio = OBUtils.coalesce(parameters.get("equaudio"), "true").equals("true");
        final OBPath button = (OBPath) objectDict.get("button");
        float outset = applyGraphicScale(20);
        button.outdent(outset);

    }

    public void setButtonActive(boolean active)
    {
        float op = active?1.0f:0.5f;
        lockScreen();
        objectDict.get("button").setOpacity(op);
        unlockScreen();
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_BUTTON_CLICK);
    }

    public void setScenek()
    {
        loadEvent("anna");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("anna"));
        presenter.control.setZPosition(200);
        PointF pt = new PointF();
        pt.set(presenter.control.position());
        presenter.control.setProperty("restpos",pt);
        presenter.control.setRight(0);
        presenter.control.show();
        setButtonActive(false);
    }

    public void setScenel()
    {
        setButtonActive(false);
    }

    public OBLabel layOutEquation(ocasc_equation equ,float left,float top)
    {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%d ",equ.lh));
        str.append(equ.isPlus?"+":"–");
        str.append(String.format(" %d",equ.rh));
        str.append(" = ");
        int val = equ.lh;
        val +=(equ.isPlus?1:-1) * equ.rh;
        str.append(String.format("%d",val));
        OBLabel equLabel = new OBLabel(str.toString(),font);
        equLabel.setLeft(left);
        equLabel.setTop(top);
        equLabel.setColour(Color.RED);
        attachControl(equLabel);
        equLabel.hide();
        return equLabel;
    }

    public void setScenem()
    {
        List labs = new ArrayList<>();
        OBControl boxl1 = objectDict.get("boxl1");
        OBControl boxl2 = objectDict.get("boxl2");
        OBControl boxr1 = objectDict.get("boxr1");
        float left = boxl1.left();
        float top = (boxl1.top());
        float yinc = boxl2.top() - boxl1.top();
        float xinc = boxr1.left() - boxl1.left();
        int noRows =((int) equations.size() + 1) / 2;
        for(int i = 0;i < 2;i++)
            for(int j = 0;j < noRows;j++)
            {
                int idx = i * noRows + j;
                if(idx < equations.size())
                {
                    ocasc_equation eq = equations.get(idx);
                    labs.add(layOutEquation(eq,left + xinc * i,top + yinc * j));
                }
            }
        labels = labs;
        targets = Arrays.asList(objectDict.get("button"));
    }


    public void doMainXX() throws Exception
    {
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        OBControl b = objectDict.get("button");
        if(b.opacity() < 1.0)
        {
            playSfxAudio("buttonactive",false);
            setButtonActive(true);
        }
        if(currentAudio("PROMPT") != null)
        {
            waitForSecs(0.2f);
            waitSFX();
            playAudioQueuedScene("PROMPT",false);
        }
    }

    public void endBody()
    {
        flashButton(statusTime());
    }

    public void flashButton(final long stt)
    {
    reprompt(stt, null, 6, new OBUtils.RunLambda() {
        @Override
        public void run() throws Exception
        {
            OBControl c = objectDict.get("button");
            try
            {
                {
                    for(int i = 0;i < 3 && statusTime() == stt;i++)
                    {
                        c.highlight();
                        waitForSecs(0.2f);
                        c.lowlight();
                        waitForSecs(0.2f);
                    }
                }
            }
            catch(Exception e)
            {
                c.lowlight();
            }
            if(!aborting())
                flashButton(stt);
        }
    });
    }

    public void demok() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        if(isPlus)
            presenter.speak(Arrays.asList(aud.get(0)),this);
        else
            presenter.speak(Arrays.asList(aud.get(1)),this);

        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = new PointF();
        currPos.set(presenter.control.position());
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds() .width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak((List)currentAudio("DEMO2"),this);
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

    public void demol() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,15);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",1,true);
        nextScene();
    }

    public void demompref() throws Exception
    {
        playSfxAudio("buttonactive",false);
        lockScreen();
        setButtonActive(true);
        unlockScreen();
        waitForSecs(0.2f);
        waitSFX();
        movePointerToPoint(OB_Maths.locationForRect(new PointF(1.5f, 0.5f) , objectDict.get("button").frame()),-1,true);
        playAudioQueuedScene("DEMO",true);
        thePointer.hide();
        nextScene();
    }

    public void demop() throws Exception
    {
        waitForSecs(1f);
        gotItRightBigTick(true);
        waitForSecs(1f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.3f);
        int col = objectDict.get("swatch").fillColor();
        OBAudioManager aman = OBAudioManager.audioManager;
        Map<String,Map<String,List>> ass = (Map)audioScenes;
        List ll = ass.get("sfx") .get("ping");
        aman.prepare((String) ass.get("sfx") .get("ping") .get(0));
        float duration = (float) aman.duration();
        float waittime =(duration - 1) / labels.size();
        aman.playOnChannel(AM_MAIN_CHANNEL);
        for(OBLabel l : labels)
        {
            l.setColour(col);

            waitForSecs(waittime);
        }
        waitSFX();
        waitForSecs(0.3f);

        lockScreen();
        for(OBLabel l : labels)
            l.setColour(Color.BLACK);
        unlockScreen();
        waitForSecs(3f);
        nextScene();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            Map<String, List> eventd = (Map<String, List>) audioScenes.get(String.format("finale%d",standalone?1:2));
            playAudioQueued(eventd.get(String.format("DEMO%s",isPlus?"":"2")),true);
            MainActivity.mainActivity.fatController.completeEvent(this);
        }
        catch(Exception exception)
        {
        }
    }


    public void nextEquation()
    {
        currNo++;
        nextScene();
    }

    public void checkButton()
    {
        try
        {
            final OBControl button = objectDict.get("button");
            //playSfxAudio("touch",false);
            button.highlight();
            OBUtils.runOnOtherThreadDelayed(0.5f,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    button.lowlight();
                    setButtonActive(false);
                }
            });
            waitForSecs(0.1f);
            waitSFX();
            waitForSecs(0.2f);
            playSfxAudio("eqnon",false);
            labels.get(currNo).show();
            waitForSecs(0.4f);
            readEquation(labels.get(currNo),equations.get(currNo),Color.RED);
            //waitForSecs(0.4f);
            nextEquation();
        }
        catch(Exception e)
        {
        }
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_WAITING_FOR_BUTTON_CLICK)
        {
            Object obj = findTarget(pt);
            if(obj != null)
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
    }

}
