package org.onebillion.onecourse.mainui.oc_addsubcounters;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;

public class OC_AddSubCountersPt1 extends OC_AddSubCounters
{
    OBFont equfont,panelfont;
    float equTextSize,panelTextSize;
    float counterXinc,counterYinc;
    OBLabel equLabel;
    List<OBControl>leftCounters,rightCounters;
    List<OBLabel>bottomLabels;
    List<ocasc_equation>equations;
    OBPresenter presenter;
    boolean standalone;

    public void buildRandomSign(boolean isPlus,int num,int minnum,int maxnum)
    {
        List<List<Integer>> tuples = new ArrayList<>();
        for(int n = minnum;n <= maxnum;n++)
        {
            for(int o = 1;o < n;o++)
            {
                int r = isPlus?n - o:o;
                int l = isPlus?o:n;
                tuples.add(Arrays.asList(l ,r));
            }
        }
        List<ocasc_equation>mEquations = new ArrayList<>();
        for(List tuple : OBUtils.randomlySortedArray(tuples).subList(0, num))
        {
            ocasc_equation eq = new ocasc_equation();
            eq.isPlus = (isPlus);
            eq.lh = (Integer)(tuple.get(0));
            eq.rh = (Integer)(tuple.get(1));
            mEquations.add(eq);
        }
        equations = mEquations;
    }

    public void buildRandomEquationList(String randomParam)
    {
        List<String>parms = Arrays.asList(randomParam.split(","));
        boolean isPlus = parms.get(0).equals("+");
        int minn,maxn;
        if(parms.get(0).equals("1"))
        {
            minn = 1;
            maxn = 10;
        }
        else
        {
            minn = 6;
            maxn = 20;
        }
        buildRandomSign(isPlus,8,minn,maxn);
    }

    public void buildEquationList(String inParam)
    {
        List<ocasc_equation>mEquations = new ArrayList<>();
        for(String p : inParam.split(";"))
        {
            List<String> components = Arrays.asList(p.split(","));
            if(TextUtils.isDigitsOnly(components.get(0)))
            {
                int ct = Integer.parseInt(components.get(0));
                String sign = components.get(1);
                int lnum = Integer.parseInt(components.get(2));
                int rnum = Integer.parseInt(components.get(3));
                int linc = Integer.parseInt(components.get(4));
                int rinc = Integer.parseInt(components.get(5));
                for(int i = 0;i < ct;i++)
                {
                    ocasc_equation eq =  new ocasc_equation();
                    eq.isPlus=(sign.equals("+"));
                    eq.lh=(lnum + i * linc);
                    eq.rh=(rnum + i * rinc);
                    mEquations.add(eq);
                    if(isovernum(eq, 20))
                        equAudio = false;
                }
            }
        }
        equations = mEquations;
    }

    public void miscSetUp()
    {
        super.miscSetUp();
        if(parameters.get("equations") == null)
            buildRandomEquationList(parameters.get("random"));
        else
            buildEquationList(parameters.get("equations"));
        loadEvent("mastera");
        equTextSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("equtextsize") ,"42"));
        equfont = StandardReadingFontOfSize(equTextSize);
        panelTextSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("paneltextsize") ,"42"));
        panelfont = StandardReadingFontOfSize(panelTextSize);
        events = new ArrayList<>(Arrays.asList("e","f","g","h"));
        while(events.size() < equations.size() - 1)
            events.add(events.get(events.size()-1));
        while(events.size() > equations.size() - 1)
            events.remove(events.size()-1);
        events.add("i");
        events.add(0,"epref");
        boolean optDemo = OBUtils.coalesce(parameters.get("optdemo") , "false").equals("true");
        if(optDemo)
            events.add(0,"d");
        events.add(0,"c");
        boolean annaIntro = OBUtils.coalesce(parameters.get("annaintro") , "false").equals("true");
        standalone = OBUtils.coalesce(parameters.get("standalone") , "false").equals("true");
        if(standalone)
            annaIntro = false;
        events.add(0,annaIntro?"a":"b");
    }


    public long switchStatus(String scene)
    {
        return setStatus(STATUS_WAITING_FOR_BUTTON_CLICK);
    }


    public void layOutEquation(ocasc_equation equ,RectF rect)
    {
        StringBuilder str = new StringBuilder();
        str.append(String.format("%d ",equ.lh));
        str.append(equ.isPlus?"+":"–");
        str.append(String.format(" %d",equ.rh));
        str.append(" = ");
        int val = equ.lh;
        val +=(equ.isPlus?1:-1) * equ.rh;
        str.append(String.format("%d",val));
        equLabel = new OBLabel(str.toString(),equfont);
        equLabel.setPosition(new PointF(rect.centerX() ,rect.centerY()));
        equLabel.setColour(Color.BLACK);
        attachControl(equLabel);
        equLabel.hide();
    }

    public OBControl createCounter(int i,float x0,float y0,OBControl template)
    {
        OBControl pi = template.copy();
        int xi = i % 10;
        int yi = i / 10;
        pi.setPosition(new PointF(x0 + xi * counterXinc, y0 + yi * counterYinc));
        pi.show();
        attachControl(pi);
        pi.hide();
        return pi;
    }
    public void layOutCountersLeft(int noLeft,int noRight)
    {
        OBControl pp = objectDict.get("counterp1");
        OBControl pb = objectDict.get("counterp10");
        PointF pos = pp.position();
        float x0 = pos.x;
        float y0 = pos.y;
        pos = pb.position();
        counterXinc =(pos.x - x0) / 9;
        pb = objectDict.get("counterb1");
        pos = pb.position();
        counterYinc = pos.y - y0;
        leftCounters = new ArrayList<>();
        for(int i = 0;i < noLeft;i++)
        {
            leftCounters.add(createCounter(i,x0,y0,pp));
        }
        rightCounters = new ArrayList<>();
        for(int i = 0;i < noRight;i++)
        {
            rightCounters.add(createCounter(i+noLeft,x0,y0,pb));
        }
    }

    public List<OBLabel> layOutBottomLabels(List<Integer> numbers)
    {
        OBControl swatch = objectDict.get("bottomnumberswatch");
        int col = swatch.fillColor();
        List<OBLabel>labs = new ArrayList<>();
        for(Integer n : numbers)
        {
            OBLabel l = new OBLabel(String.format("%d",n.intValue()),panelfont);
            l.setColour(col);
            attachControl(l);
            labs.add(l);
            l.hide();
        }
        List<OBLabel> blabs = OBUtils.randomlySortedArray(labs);
        List<Map> comps = new ArrayList<>();
        Map ds = new HashMap();
        ds.put("fixed",false);
        ds.put("width",2.0f);
        comps.add(ds);
        Map spacer = null;
        for(OBLabel l : blabs)
        {
            if(spacer != null)
                comps.add(spacer);
            Map d = new HashMap();
            d.put("fixed",true);
            d.put("width",l.width());
            d.put("obj",l);
            comps.add(d);
            spacer = new HashMap();
            spacer.put("fixed",false);
            spacer.put("width",1.0f);
        }
        comps.add(ds);
        RectF frme = objectDict.get("bottomrect").frame();
        float totwidth = 0;
        float totvariable = 0;
        for(Map d : comps)
        {
            boolean fixed = (Boolean)d.get("fixed");
            if(fixed)
                totwidth += (Float)d.get("width");
            else
                totvariable += (Float)d.get("width");
        }
        float multiplier =(frme.width() - totwidth) / totvariable;
        float y = frme.top + frme.height() / 2;
        float x = 0;
        for(Map d : comps)
        {
            OBLabel l = (OBLabel)d.get("obj");
            if(l != null)
            {
                float tempx = x + l.width() / 2;
                PointF pos = new PointF(tempx, y);
                l.setPosition(pos);
                l.setProperty("botpos",(pos));
                x += l.width();
            }
            else
            {
                x += (Float)d.get("width") * multiplier;
            }
        }

        return labs;
    }

    public void positionRedLine(String op)
    {
        float l = equLabel.left();
        String str = equLabel.text();
        int rst = str.indexOf(op);
        String str2 = str.substring(0,rst + 2);
        float w2 = OC_Wordcontroller.boundingBoxForText(str2, equfont) .width();
        float x = l + w2;
        OBControl c = objectDict.get("redline");
        c.setLeft(x);
        c.hide();
    }

    public List bottomNumbers(int correctone)
    {
        List<Integer> nos = new ArrayList<>();
        for(int i = 1;i <= 4;i++)
        {
            nos.add((correctone + i));
            int n = correctone - i;
            if(n >= 0)
                nos.add((n));
        }
        List rana = OBUtils.randomlySortedArray(nos);
        return Arrays.asList((correctone) ,rana.get(0) ,rana.get(1));
    }

    public void showButtonSign(boolean show)
    {
        OBGroup sign =(OBGroup) objectDict.get("sign");
        if(show)
            sign.showMembers("sign");
        else
            sign.hideMembers("sign");
    }

    public void setUpScene()
    {
        ocasc_equation eq = equations.get(currNo);
        String ev = eq.isPlus?"plus":"minus";
        loadEvent(ev);
        layOutEquation(eq,objectDict.get("equationbox").frame());
        layOutCountersLeft(eq.lh,eq.isPlus?eq.rh:0);
        int val = eq.lh;
        val +=(eq.isPlus?1:-1) * eq.rh;
        bottomLabels = layOutBottomLabels(bottomNumbers(val));
        positionRedLine("=");
        showButtonSign(false);

        hideControls("sign");
        showResult(false);
    }

    public void setScenea()
    {
        loadEvent("anna");
        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("anna"));
        presenter.control.setZPosition(200);
        PointF pos = new PointF();
        pos.set(presenter.control.position());
        presenter.control.setProperty("restpos",pos);
        presenter.control.setRight(0);
        presenter.control.show();
        setUpScene();

    }

    public void setScenec()
    {
    }

    public void setScened()
    {
    }

    public void setSceneepref()
    {
    }

    public void setScenee()
    {
        targets = Arrays.asList(objectDict.get("sign"));
    }

    public void setSceneXX(String scene)
    {
        setUpScene();
        targets = Arrays.asList(objectDict.get("sign"));
    }

    public void setButtonActive(boolean act)
    {
        float op = act?1:0.5f;
        OBControl but = objectDict.get("sign");
        lockScreen();
        but.setOpacity(op);
        but.lowlight();
        unlockScreen();
    }

    public void showResult(boolean show)
    {
        int col = show?Color.BLACK :Color.TRANSPARENT;
        String txt = equLabel.text();
        int rst = txt.indexOf("=");
        int idx =(int) rst + 2;
        lockScreen();
        equLabel.setHighRange(idx,txt.length(),col);
        unlockScreen();
    }

    public void highlightLh(boolean high)
    {
        int col = high?Color.RED :Color.BLACK;
        int rst = equLabel.text().indexOf(" ");
        equLabel.addColourRange(0, rst,col);
    }

    public void highlightrh(boolean high)
    {
        int col = high?Color.RED :Color.BLACK;
        String tx = equLabel.text();
        int rst = tx.indexOf("+");
        if (rst < 0)
            rst = tx.indexOf("–");
        int st = rst;
        rst = tx.indexOf(" ",st + 2);
        equLabel.addColourRange(st,rst,col);
    }

    public void setAnswerColour(int col)
    {
        String tx = equLabel.text();
        int rst = equLabel.text().indexOf("=");
        int st = (int)(rst + 2);
        equLabel.addColourRange(st, tx.length(),col);
    }

    public void slideOnBottomLabels() throws Exception
    {
        playSfxAudio("slide",false);
        List anims = new ArrayList<>();
        lockScreen();
        for(OBLabel lab : bottomLabels)
        {
            PointF pos = new PointF();
            pos.set(lab.position());
            lab.setPosition(new PointF(pos.x + bounds() .width(), pos.y));
            anims.add(OBAnim.moveAnim(pos,lab));
            lab.show();
        }
        unlockScreen();
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void hideBottomLabels() throws Exception
    {
        List anims = new ArrayList<>();
        lockScreen();
        for(OBLabel lab : bottomLabels)
        {
            anims.add(OBAnim.opacityAnim(0,lab));
        }
        unlockScreen();
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
        lockScreen();
        for(OBLabel lab : bottomLabels)
        {
            lab.hide();
            lab.setOpacity(1);
        }
        unlockScreen();
    }

    public void clearOff()
    {
        List<OBControl> arr = new ArrayList<>(leftCounters);
        arr.addAll(bottomLabels);
        arr.addAll(rightCounters);
        lockScreen();
        for(OBControl c : arr)
            detachControl(c);
        detachControl(equLabel);
        deleteControls("redline");
        unlockScreen();
    }

    public void readEquation()throws Exception
    {
        readEquation(equLabel,equations.get(currNo),Color.RED);
    }

    public void showThings() throws Exception
    {
        showEqu();
        waitForSecs(0.1f);
        waitSFX();
        waitForSecs(0.9f);
        showSign();
    }

    public void doMainXX() throws Exception
    {
        if(equLabel.hidden())
        {
            showThings();
            waitForSecs(0.1f);
            waitSFX();
            waitForSecs(0.1f);
            playAudioQueuedScene("PROMPT",false);
        }
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
    }

    public void endBody()
    {
        flashButton(statusTime());
    }

    public void flashButton(final long stt)
    {
        reprompt(stt, null, 5, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                OBControl c = objectDict.get("sign");
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

    public void demoa() throws Exception
    {
        presenter.walk((PointF) presenter.control.settings.get("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        List aud = currentAudio("DEMO");
        presenter.speak(Arrays.asList(aud.get(0)),this);
        waitForSecs(0.5f);
        presenter.speak(Arrays.asList(aud.get(1)),this);

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

        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,2);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        nextScene();
    }

    public void demob() throws Exception
    {
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        PointF startpt = pointForDestPoint(destpoint,15);
        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioQueuedScene("DEMO",1,true);
        nextScene();
    }

    public void showEqu() throws Exception
    {
        playSfxAudio("eqnon",false);
        lockScreen();
        showControls("redline");
        equLabel.show();
        unlockScreen();
    }

    public void showSign() throws Exception
    {
        playSfxAudio("button_on",false);
        lockScreen();
        showControls("sign");
        unlockScreen();
    }

    public void democ() throws Exception
    {
        showEqu();
        waitForSecs(0.1f);
        waitSFX();
        waitForSecs(0.2f);
        showSign();
        PointF destpoint = OB_Maths.locationForRect(new PointF(0.5f,3) ,objectDict.get("redline").frame());
        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",1,true);
        destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.85f) ,bounds());
        waitForSecs(0.2f);
        movePointerToPoint(destpoint,-1,true);
        nextScene();
    }

    public void showLeftCounters() throws Exception
    {
        playSfxAudio("beep_1",false);
        highlightLh(true);
        waitForSecs(0.7f);

        playSfxAudio("counter_1",false);
        lockScreen();
        for(OBControl c : leftCounters)
        {
            c.show();
        }
        unlockScreen();
        waitForSecs(0.4f);
        highlightLh(false);
    }

    public void showRightCounters() throws Exception
    {
        playSfxAudio("beep_1",false);
        highlightrh(true);
        waitForSecs(0.7f);
        ocasc_equation eq = equations.get(currNo);
        if(eq.isPlus)
        {
            for(OBControl c : rightCounters)
            {
                playSfxAudio("counter_2",false);
                c.show();
                waitForSecs(0.3f);
            }
        }
        else
        {
            OBControl pb = objectDict.get("counterb1");
            int col = pb.fillColor();
            int ct = eq.rh;
            for(int i = 0;i < ct;i++)
            {
                playSfxAudio("counter_2",false);
                OBControl c = leftCounters.get(leftCounters.size() - 1 - i);
                c.setProperty("origcol",c.fillColor());
                c.setFillColor(col);
                waitForSecs(0.3f);
            }
        }
        highlightrh(false);
    }

    public void demod() throws Exception
    {
        playAudioScene("DEMO",0,true);
        waitForSecs(0.2f);
        waitForSecs(0.1f);
        waitSFX();
        waitForSecs(0.2f);

        final OBControl button = objectDict.get("sign");
        final PointF restpoint = OB_Maths.locationForRect(new PointF(1.5f, 0.8f) , button.frame());
        movePointerToPoint(restpoint,-1,true);
        waitForSecs(0.4f);
        playAudioScene("DEMO",1,true);
        movePointerToPoint(button.position(),-1,true);
        button.highlight();
        OBUtils.runOnOtherThreadDelayed(0.3f,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                button.lowlight();
                setButtonActive(false);
                movePointerToPoint(restpoint,-0.5f,false);
            }
        });
        showLeftCounters();

        waitForSecs(1.3f);

        playSfxAudio("buttonactive_1",false);
        lockScreen();
        showButtonSign(true);
        setButtonActive(true);
        unlockScreen();
        playAudioQueuedScene("DEMO2",true);
        movePointerToPoint(button.position(),-1,true);
        button.highlight();
        OBUtils.runOnOtherThreadDelayed(0.3f,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                button.lowlight();
                setButtonActive(false);
                movePointerToPoint(restpoint,-0.5f,false);
            }
        });


        showRightCounters();

        hideControls("sign");

        OBControl o =(rightCounters.size() > 0?rightCounters:leftCounters).get((rightCounters.size() > 0?rightCounters:leftCounters).size()-1);

        PointF destpoint = OB_Maths.locationForRect(new PointF(0f, 1.5f) , o.frame());
        destpoint.x = (bounds() .width() / 2);

        movePointerToPoint(destpoint,-1,true);
        waitForSecs(0.3f);
        playAudioQueuedScene("DEMO3",true);

        //destpoint.setX(bounds() .width() - applyGraphicScale(40));
        destpoint = OB_Maths.locationForRect(new PointF(0.85f, 0.66f) , bounds());
        movePointerToPoint(destpoint,-1,true);

        slideOnBottomLabels();

        destpoint.y = (objectDict.get("bottomrect").frame().centerY());

        movePointerToPoint(destpoint,-1,true);

        playAudioQueuedScene("DEMO4",true);

        movePointerToPoint(bottomLabels.get(0).position(),-1,true);
        waitForSecs(0.1f);

        playSfxAudio("choose",false);
        bottomLabels.get(0).highlight();
        final PointF fdp = new PointF(destpoint.x,destpoint.y);
        OBUtils.runOnOtherThreadDelayed(0.3f,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                bottomLabels.get(0).lowlight();
                setButtonActive(false);
                movePointerToPoint(fdp,-1,false);
            }
        });
        waitForSecs(0.3f);
        playSfxAudio("final",false);
        setAnswerColour(Color.RED);
        objectDict.get("redline").hide();
        waitForSecs(0.3f);
        setAnswerColour(Color.BLACK);
        hideBottomLabels();

        waitForSecs(0.3f);

        readEquation();

        waitForSecs(0.8f);
        playSfxAudio("alloff",false);
        lockScreen();
        List<OBControl> arr = new ArrayList(leftCounters);
        arr.addAll(rightCounters);
        for(OBControl c : arr)
        {
            c.hide();
            Object col = c.propertyValue("origcol");
            if(col != null)
                c.setFillColor((Integer)col);
        }
        showResult(false);
        objectDict.get("redline").show();
        unlockScreen();
        setButtonActive(true);
        showButtonSign(false);
        showSign();
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO5",true);
        nextScene();
    }

    public void demoepref() throws Exception
    {
        playSfxAudio("buttonactive_1",false);
        setButtonActive(true);
        waitForSecs(0.4f);
        OBControl button = objectDict.get("sign");
        PointF restpoint = OB_Maths.locationForRect(new PointF(1.1f, 0.8f) , button.frame());
        movePointerToPoint(restpoint,-1,true);
        waitForSecs(0.2f);
        playAudioQueuedScene("DEMO",true);
        waitForSecs(0.1f);
        thePointer.hide();
        nextScene();

    }

    public void e2Demo() throws Exception
    {
        slideOnBottomLabels();

        OBControl o =(rightCounters.size() > 0?rightCounters:leftCounters).get((rightCounters.size() > 0?rightCounters:leftCounters).size()-1);
        PointF destpoint = OB_Maths.locationForRect(new PointF(0f, 1.5f) , o.frame());
        destpoint.x = (bounds() .width() / 2);
        PointF startpt = pointForDestPoint(destpoint,10);

        loadPointerStartPoint(startpt,destpoint);
        movePointerToPoint(destpoint,-1,true);
        playAudioScene("DEMO",0,true);

        destpoint = OB_Maths.locationForRect(new PointF(0.8f,0.6f) ,objectDict.get("bottomrect").frame());
        movePointerToPoint(destpoint,-1,true);
        playAudioScene("DEMO",1,true);

        thePointer.hide();
    }

    public void fin()
    {
        if(standalone)
        {
            super.fin();
        }
        else
        {
            try
            {
                waitForSecs(0.3f);
            }
            catch(Exception e)
            {

            }
            List ps = new ArrayList(Arrays.asList(((String)params).split("/")));
            ps.remove(0);
            ps.add(0,"addsubcounterspt2");
            goToCard(OC_AddSubCountersPt2.class,TextUtils.join("/",ps),false);
        }
    }

    public void stage2() throws Exception
    {
        waitForSecs(0.3f);
        if(currentEvent().equals("e"))
            e2Demo();
        else
        {
            List aud = currentAudio("STAGE2");
            if(aud.size() > 0)
                playAudioQueued(Arrays.asList(aud.get(0)),true);
            slideOnBottomLabels();
            if(aud.size() > 1)
                playAudioQueued(Arrays.asList(aud.get(1)),false);
        }
        setReplayAudio((List)currentAudio("STAGE2.REPEAT"));
        targets = new ArrayList<OBControl>(bottomLabels);
        setStatus(STATUS_WAITING_FOR_ANSWER);
    }

    public void checkAnswer(final OBControl targ)
    {
        try
        {
            playSfxAudio("choose",false);
            targ.highlight();
            OBUtils.runOnOtherThreadDelayed(0.3f,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    targ.lowlight();
                }
            });
            if(targ == bottomLabels.get(0))
            {
                waitForSecs(0.3f);
                playSfxAudio("final",false);
                setAnswerColour(Color.BLACK);
                objectDict.get("redline").hide();
                waitForSecs(0.3f);
                //setAnswerColour(Color.black());
                hideBottomLabels();
                gotItRightBigTick(false);
                waitForSecs(1.3f);
                readEquation();
                waitForSecs(2f);
                nextEquation();
            }
            else
            {
                gotItWrongWithSfx();
                waitForSecs(0.2f);
                playAudioQueuedScene("INCORRECT",false);
                setStatus(STATUS_WAITING_FOR_ANSWER);
            }
        }
        catch(Exception e)
        {
        }

    }

    public void nextEquation() throws Exception
    {
        if(eventIndex + 1 < events.size())
            clearOff();
        else
        {
            waitForSecs(0.4f);
            gotItRightBigTick(true);
            waitForSecs(0.2f);
        }
        currNo++;
        nextScene();
    }

    public void checkButton2() throws Exception
    {
        final OBControl button = objectDict.get("sign");
        button.highlight();
        OBUtils.runOnOtherThreadDelayed(0.5f,new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                button.lowlight();
                setButtonActive(false);
            }
        });

        showRightCounters();

        hideControls("sign");

        stage2();
    }

    public void button2() throws Exception
    {
        playSfxAudio("buttonactive_1",false);
        lockScreen();
        showButtonSign(true);
        setButtonActive(true);
        unlockScreen();
        waitForSecs(0.1f);
        waitSFX();
        playAudioQueuedScene("BUTTON2",false);
        setStatus(STATUS_AWAITING_CLICK2);
        flashButton(statusTime());
    }

    public void checkButton()
    {
        try
        {
            final OBControl button = objectDict.get("sign");
            button.highlight();
            OBUtils.runOnOtherThreadDelayed(0.5f,new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    button.lowlight();
                    setButtonActive(false);
                }
            });
             showLeftCounters();
            button2();
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
        else if(status() == STATUS_AWAITING_CLICK2)
        {
            Object obj = findTarget(pt);
            if(obj != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkButton2();
                    }
                });
            }
        }
        else if(status() == STATUS_WAITING_FOR_ANSWER)
        {
            final OBControl obj = (OBControl) findTarget(pt);
            if(obj != null)
            {
                setStatus(STATUS_CHECKING);
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkAnswer(obj);
                    }
                });
            }
        }

    }

}
