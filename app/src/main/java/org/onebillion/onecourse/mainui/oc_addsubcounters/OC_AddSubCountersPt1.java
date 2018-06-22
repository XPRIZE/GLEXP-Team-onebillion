package org.onebillion.onecourse.mainui.oc_addsubcounters;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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


}
