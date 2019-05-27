package com.maq.xprize.onecourse.mainui.oc_morphology;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPresenter;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OC_Morphology_A extends OC_Morphology
{
    public class  ocm_screen extends Object {
        String imageID;
        List<String> sentenceIDs;
    }
    int screenNo,buttonNo;
    List<ocm_screen>screens;
    List<OBLabel>labels;
    int hiColour,doneColour;

    public void miscSetUp()
    {
        super.miscSetUp();
        List scrs = new ArrayList<>();
        for(String screentxt : parameters.get("screens").split(";"))
        {
            String[] comps = screentxt.split(",");
            ocm_screen scr = new ocm_screen();
            scr.imageID = (comps[0]);
            scr.sentenceIDs = Arrays.asList(Arrays.copyOfRange(comps,1, comps.length));
            scrs.add(scr);
        }
        screens = scrs;
        mtype = OBUtils.coalesce(parameters.get("mtype") ,"");
        screenNo = 0;
        boolean annaIntro = OBUtils.coalesce(parameters.get("annaintro") ,"false").equals("true");
        boolean partTwo = OBUtils.coalesce(parameters.get("parttwo") ,"false").equals("true");
        events = new ArrayList<>();
        if(annaIntro)
            events.add("a");
        events.add("b");
        if(!annaIntro)
            events.add("c");
        events.add("d");
        events.add("d2");
        events.add("e");
        if(screens.get(0).sentenceIDs.size() > 2)
            events.add("e");
        events.add("f");
        events.add("g");
        events.add("g2");
        int i = 1;
        while(screens.size() > i)
        {
            events.add("h");
            events.add("i");
            if(screens.get(i).sentenceIDs.size() > 2)
                events.add("i");
            events.add("j");
            events.add("k");
            i++;
        }
        if(partTwo)
        {
            events.add("l");
            events.add("l2");
            events.add("l3");
            events.add("m");
        }
    }

    public long switchStatus(String scene)
    {
        if(Arrays.asList("g2","k","m").contains(scene))
            return setStatus(STATUS_AWAITING_ARROW_CLICK);
        if(Arrays.asList("l2").contains(scene))
            return setStatus(STATUS_AWAITING_CLICK2);
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void scalePicToBox()
    {
        OBControl picbox = objectDict.get("picbox");
        float wratio = mainPic.width() / picbox.width();
        float hratio = mainPic.height() / picbox.height();
        float ratio = wratio > hratio?wratio:hratio;
        mainPic.setScale(1 / ratio);
        PointF rp = new PointF();
        rp.set(picbox.position());

        mainPic.setPosition(rp);
    }

    public void setUpPic()
    {
        if(screenNo == 0 || !screens.get(screenNo).imageID.equals(screens.get(screenNo-1).imageID))
        {
            if(mainPic != null)
                detachControl(mainPic);
            mainPic = loadImageWithName(screens.get(screenNo).imageID,new PointF(0.5f, 0.5f),boundsf());
            //objectDict.setGet("mainpic")(mainPic);
            mainPic.setZPosition(objectDict.get("textbox_0").zPosition() - 0.01f);
            scalePicToBox();
            mainPic.hide();
        }
    }

    public OBControl bullet(int i)
    {
        String bname = String.format("bullet_%d",i);
        return objectDict.get(bname);
    }

    public void setBulletActive(int i)
    {
        OBControl cont = bullet(i);
        lockScreen();
        cont.lowlight();
        cont.setOpacity(1.0f);
        unlockScreen();
    }

    public void setBulletInactive(int i)
    {
        OBControl cont = bullet(i);
        lockScreen();
        cont.setFillColor(hiColour);
        cont.lowlight();
        cont.setOpacity(0.2f);
        unlockScreen();
    }

    public void setBulletDone(int i)
    {
        OBControl cont = bullet(i);
        lockScreen();
        cont.lowlight();
        cont.setOpacity(1);
        cont.setFillColor(doneColour);
        unlockScreen();
    }

    public void setBulletSelected(int i)
    {
        OBControl cont = bullet(i);
        lockScreen();
        cont.setFillColor(hiColour);
        cont.highlight();
        cont.setOpacity(1.0f);
        unlockScreen();
    }

    public void setUpScene()
    {
        deleteControls(".*");
        if (labels != null)
            for(OBControl c : labels)
                detachControl(c);
        loadEvent("a");
        OBControl bullet = bullet(0);
        hiColour = bullet.fillColor();
        doneColour = objectDict.get("swatch").fillColor();
        setUpPic();
        ocm_screen screen = screens.get(screenNo);
        List<String> skeys = screen.sentenceIDs;
        labels = new ArrayList<>();
        for(int i = 0;i < skeys.size();i++)
        {
            String[] cmps = skeys.get(i).split("\\.");
            String sid = cmps[1];
            ocm_sentence se = sentenceDict.get(sid);
            OBControl rect = objectDict.get(String.format("textbox_%d",i));
            RectF f = rect.frame();
            float w = rect.left() - bullet.left();
            f.right = f.left + (bounds().width() - w);
            rect.setFrame(f);
            OBLabel lab = createLabelFromText(se.text,rect);
            se.words = (WordsFromSentence(se.text));
            LoadTimingsForWords(se.words, getLocalPath(String.format("%s.etpa",sid)));
            attachControl(lab);
            labels.add(lab);
            lab.hide();
            setBulletInactive(i);
        }

        RectF fu = new RectF(bullet.frame());
        for(OBControl l : labels)
            fu.union(l.frame());
        float diff =(bounds().width() / 2) - fu.centerX();
        List<OBControl> lc = new ArrayList<>();
        lc.addAll(labels);
        lc.addAll(filterControls("bullet.*"));
        for(OBControl c : lc)
        {
            PointF pos = new PointF();
            pos.set(c.position());
            pos.x += diff;
            c.setPosition(pos);
        }
        buttonNo = 0;
        hideControls("bullet.*");
    }

    public void setScenea()
    {
        loadEvent("anna");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("anna"));
        presenter.control.setZPosition(200);
        PointF rp = new PointF();
        rp.set(presenter.control.position());
        presenter.control.setProperty("restpos",rp);
        presenter.control.setRight(0);
        presenter.control.show();
    }

    public void setSceneb()
    {
        setUpScene();
    }

    public void setSceneg2()
    {
        targets = Arrays.asList(objectDict.get("arrow"));
    }

    public void setSceneh()
    {
        setUpScene();
    }

    public void setScenek()
    {
        targets = Arrays.asList(objectDict.get("arrow"));
    }

    public void setScenel()
    {
        deleteControls(".*");
        loadEvent("l");
        OBControl textbox1 = objectDict.get("textbox1");
        OBControl textbox2 = objectDict.get("textbox2");
        float top = textbox1.top();
        float yinc = textbox2.top() - top;
        labels = new ArrayList<>();
        float maxx = 0;
        List arr = new ArrayList<>();
        for(ocm_screen scr : screens)
        {
            arr.addAll(scr.sentenceIDs);
        }
        sentenceIds = arr;
        for(String k : sentenceIds)
        {
            String[] karr = k.split("\\.");
            String sid = karr[1];
            ocm_sentence se = sentenceDict.get(sid);
            OBLabel lab = createLabelFromText(se.text,textbox1);
            lab.setTop(top);
            attachControl(lab);
            labels.add(lab);
            lab.hide();
            if(lab.right() > maxx)
                maxx = lab.right();
            top += yinc;
        }
        float diff = (bounds().width() / 2) -((labels.get(0).left() + maxx) / 2);
        if(diff != 0.0)
        {
            for(OBLabel lab : labels)
            {
                PointF pos = lab.position();
                pos.x += diff;
                lab.setPosition(pos);
            }
        }
    }

    public void setScenem()
    {
        targets = Arrays.asList(objectDict.get("arrow"));
    }

    public void doAudio() throws Exception
    {
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",true);
    }

    public void activateButton() throws Exception
    {
        setBulletActive(buttonNo);
        playSfxAudio("buttonactive",false);
    }

    public void doButton() throws Exception
    {
        doAudio();
        activateButton();
        targets = Arrays.asList(bullet(buttonNo));
    }
    public void doMaind2() throws Exception
    {
        doButton();
    }
    public void doMaine() throws Exception
    {
        doButton();
    }

    public void doMainXX() throws Exception
    {
        doAudio();
    }

    public void doMainh() throws Exception
    {
        bringOnThings();
        waitForSecs(0.3f);
        doButton();
    }
    public void doMaini() throws Exception
    {
        doButton();
    }
    public void doMaink() throws Exception
    {
        waitForSecs(2f);
        doAudio();
        OBControl arrow = objectDict.get("arrow");
        playSfxAudio("arrowon",false);
        arrow.show();
    }

    public void doMainm() throws Exception
    {
        waitForSecs(2f);
        doAudio();
        OBControl arrow = objectDict.get("arrow");
        playSfxAudio("arrowon",false);
        arrow.show();
    }

    public void doBulletReminder(final long stt)
    {
        reprompt(stt, null, 4.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                try
                {
                    for(int i = 0;i < 2 && statusTime() == stt;i++)
                    {
                        bullet(buttonNo).setOpacity(0.2f);
                        waitForSecs(0.4f);
                        bullet(buttonNo).setOpacity(1);
                        waitForSecs(0.4f);
                    }
                }
                catch(Exception e)
                {

                }
                if(!aborting())
                    doBulletReminder(stt);

            }
        });
    }

    public void doScreenReprompt(final long stt,final List aud,long secs)
    {
        reprompt(stt, aud, secs, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                try {
                    if (!aborting())
                        doScreenReprompt(stt, aud, 5);
                } catch (Exception e) {
                }
            }
        });
    }

    public void flashArrow(final long stt)
    {
        reprompt(stt, null, 6.0f, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                OBControl c = objectDict.get("arrow");
                try {
                    {
                        for (int i = 0; i < 2 && statusTime() == stt; i++) {
                            c.highlight();
                            waitForSecs(0.2f);
                            c.lowlight();
                            waitForSecs(0.2f);
                        }
                    }
                } catch (Exception e) {
                    c.lowlight();
                }
                if (!aborting())
                    flashArrow(stt);
            }
        });
    }


    public void endBody()
    {
        if(status() == STATUS_AWAITING_CLICK)
            doBulletReminder(statusTime());
        else if(status() == STATUS_AWAITING_ARROW_CLICK)
            flashArrow(statusTime());
        else if(status() == STATUS_AWAITING_CLICK2)
            doScreenReprompt(statusTime(),currentAudio("PROMPT.REPEAT"),4);
    }

    public void demoa() throws Exception
    {
        presenter.walk((PointF)presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(1.3f);

        OBControl head = presenter.control.objectDict.get("head");
        PointF right = convertPointFromControl(new PointF(head.width(), 0),head);
        PointF currPos = presenter.control.position();
        float margin = right.x - currPos.x + applyGraphicScale(20);
        PointF edgepos = new PointF(bounds() .width() - margin, currPos.y);
        presenter.walk(edgepos);
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak(Arrays.asList(aud.get(1)),this);
        waitForSecs(0.5f);
        OBControl faceright = presenter.control.objectDict.get("faceright");
        PointF lp = presenter.control.convertPointFromControl(new PointF(0, 0),faceright);
        PointF destpos = new PointF(bounds() .width() + lp.x + 1, currPos.y);
        presenter.walk(destpos);
        detachControl(presenter.control);
        presenter = null;

        nextScene();
    }

    public void bringOnThings() throws Exception
    {
        if(mainPic.hidden())
        {
            playSfxAudio("picon",false);
            mainPic.show();
        }
        waitForSecs(0.3f);
        playSfxAudio("buttonson",false);
        lockScreen();
        for(int i = 0;i < screens.get(screenNo).sentenceIDs.size();i++)
            showControls(String.format("bullet_%d",i));
        unlockScreen();
    }

    public void demob() throws Exception
    {
        bringOnThings();
        waitForSecs(0.3f);
        nextScene();
    }

    public void democ() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);

        nextScene();
    }

    public void demod() throws Exception
    {
        if(thePointer == null || thePointer.hidden())
        {
            PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
            PointF startpt = pointForDestPoint(destpoint,2);
            loadPointerStartPoint(startpt,destpoint);
        }
        List<OBControl> arr = new ArrayList<>();
        for(int i = 0;i < screens.get(screenNo).sentenceIDs.size();i++)
            arr.add(objectDict.get(String.format("bullet_%d",i)));

        PointF dest = OB_Maths.locationForRect(new PointF(1.3f, 0.5f) , arr.get(0).frame());

        movePointerToPoint(dest,-20,-1,true);
        playAudioQueuedScene("DEMO",false);
        dest = OB_Maths.locationForRect(new PointF(1.3f, 0.5f) , arr.get(arr.size()-1).frame());
        movePointerToPoint(dest,-0.5f,true);
        waitForSecs(0.4f);
        waitAudio();
        thePointer.hide();
        waitForSecs(0.4f);
        nextScene();
    }

    public void demof() throws Exception
    {
        RectF rct = new RectF();
        for(OBControl c : labels)
            rct.union(c.frame());
        PointF destpoint = OB_Maths.locationForRect(new PointF(1.05f,1.01f) ,rct);
        PointF startpt = pointForDestPoint(destpoint,10);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.5f);

        int idx = 0;
        for(String cid : screens.get(screenNo) .sentenceIDs)
        {
            String[] sarr = cid.split("\\.");
            String sid = sarr[1];
            ocm_sentence se = sentenceDict.get(sid);
            String mkey = String.format("%s+%s",mtype,sarr[0]);
            List<List<Integer>> av = se.markups.get(mkey);
            for(List<Integer> v : av)
            {
                int st = v.get(0);
                int en = v.get(1);
                OBLabel lab = labels.get(idx);
                playSfxAudio("colour",false);
                lab.setHighRange(st,en,hiColour);
                waitForSecs(0.2f);
                waitSFX();
            }
            idx++;
        }
        waitForSecs(1f);
        thePointer.hide();
        nextScene();
    }

    public void demog() throws Exception
    {
        waitForSecs(2f);
        OBControl arrow = objectDict.get("arrow");
        playSfxAudio("arrowon",false);
        arrow.show();
        waitForSecs(0.2f);
        waitSFX();
        PointF destpoint = OB_Maths.locationForRect(new PointF(-0.3f,0.5f) ,arrow.frame());
        PointF startpt = pointForDestPoint(destpoint,-2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);
        //waitForSecs(1f);
        thePointer.hide();
        nextScene();
    }

    public void demoj() throws Exception
    {
        demof();
    }

    public void demol() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.66f,0.66f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioScene("DEMO",0,true);
        destpoint = OB_Maths.locationForRect(new PointF(0.56f,0.56f) ,bounds());
        movePointerToPoint(destpoint,-1,true);
        playAudioScene("DEMO",1,true);
        thePointer.hide();
        nextScene();
    }

    public void demol3() throws Exception
    {
        waitForSecs(0.4f);
        int idx = 0;
        for(OBLabel lab : labels)
        {
            playSfxAudio("sentence",false);
            lab.show();
            waitForSecs(0.2f);
            waitSFX();
            String sek = sentenceIds.get(idx).split("\\.")[(1)];
            lab.setColour(Color.RED);
            playAudioQueued((List)Arrays.asList(sek),true);
            lab.setColour(Color.BLACK);

            idx++;
        }
        waitForSecs(0.4f);

        PointF destpoint = OB_Maths.locationForRect(new PointF(0.9f,0.9f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",true);

        idx = 0;
        for(OBLabel lab : labels)
        {
            String k = sentenceIds.get(idx);
            String[] karr = k.split("\\.");
            String sid = karr[1];
            ocm_sentence se = sentenceDict.get(sid);
            String mkey = String.format("%s+%s",mtype,karr[0]);
            List<List<Integer>> av = se.markups.get(mkey);
            for(List<Integer>v : av)
            {
                int st = v.get(0);
                int en = v.get(1);
                playSfxAudio("colour",false);
                lab.setHighRange(st,en,hiColour);
                waitForSecs(0.2f);
                waitSFX();
            }
            idx++;
        }
        waitForSecs(1f);
        thePointer.hide();
        nextScene();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.3f);
            String params = String.format("morphology3;morphologya/sentencegroups=%s",parameters.get("sentencegroups"));
            goToCard(OC_Morphology_3.class,params,false);
        }
        catch(Exception e)
        {
        }
     }


    public void nextScreen() throws Exception
    {
        screenNo++;
        boolean popoffImage =(screenNo >= screens.size() || !screens.get(screenNo).imageID.equals(screens.get(screenNo-1) .imageID));

        playSfxAudio("alloff",false);
        lockScreen();
        if(popoffImage)
        {
            detachControl(mainPic);
            mainPic = null;
        }
        deleteControls("bullet.*");
        for(OBControl l : labels)
            detachControl(l);
        labels = null;
        unlockScreen();
        waitForSecs(0.4f);
        nextScene();
    }
    public void nextButton() throws Exception
    {
        if(++buttonNo > screens.get(screenNo).sentenceIDs.size())
            nextScreen();
    else
        {
            nextScene();
        }
    }
    public void checkButton(OBControl targ)
    {
        try
        {
            //playSfxAudio("buttontouch",false);
            setBulletSelected(buttonNo);
            waitForSecs(0.3f);
            setBulletActive(buttonNo);
            playSfxAudio("sentence",false);
            labels.get(buttonNo).show();
            String sek = screens.get(screenNo) .sentenceIDs.get(buttonNo);
            String sid = sek.split("\\.")[1];
            sequenceToken = 0;
            readWords(sentenceDict.get(sid).words,labels.get(buttonNo),sid,0,false);
            setBulletDone(buttonNo);
            waitForSecs(1f);
            waitSFX();
            nextButton();
        }
        catch(Exception exception)
        {
        }
    }

    public void checkArrow()
    {
        try
        {
            objectDict.get("arrow").highlight();
            //playSfxAudio("arrowtouch",true);
            //waitForSecs(0.3f);
            //objectDict.get("arrow").lowlight();
            //waitForSecs(0.1f);
            nextScreen();
        }
        catch(Exception e)
        {
        }
    }

    public void checkScreenClick()
    {
        try
        {
            nextScene();
        }
        catch(Exception e)
        {
        }
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_ARROW_CLICK)
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
            final OBControl targ = (OBControl)findTarget(pt);
            if(targ != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton(targ);
                    }
                });
            }
        }
        else if(status() == STATUS_AWAITING_CLICK2)
        {
            setStatus(STATUS_CHECKING);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    checkScreenClick();
                }
            });
        }
    }

}
