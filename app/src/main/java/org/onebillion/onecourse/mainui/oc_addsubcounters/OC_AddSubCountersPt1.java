package org.onebillion.onecourse.mainui.oc_addsubcounters;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.oc_lettersandsounds.OC_Wordcontroller;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUtils;

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
        for(OBLabel l : labs)
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

}
