package org.onebillion.onecourse.mainui.oc_playzone;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBGroup;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBConditionLock;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBUserPressedBackException;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OB_Maths.rndom;

/**
 * Created by alan on 12/09/2017.
 */

public class OC_EgmaSubTest1 extends OC_SectionController
{
    PointF textPosLeft,textPosRight;
    float textSize,labScale;
    OBLabel leftLabel,rightLabel;
    OBFont font;
    int[] nums = new int[2];
    int score;
    List<OBLabel> leftLabs,rightLabs;
    String wrongMode;
    OBPresenter presenter;

    public interface est_iface
    {
        boolean checkVals(int idxa,int idxb);
    }

    class est_test_0 implements est_iface
    {
        public boolean checkVals(int idxa, int idxb)
        {
            return idxb > idxa;
        }
    }
    static int digitof(int num,int digitno)
    {
        while(digitno > 0)
        {
            num = num / 10;
            digitno--;
        }
        return num % 10;
    }

    static boolean hasRepeatedDigit(int n)
    {
        int bits = 0;
        while(n > 0)
        {
            int d = n % 10;
            if(((1 << d) & bits) != 0)
                return true;
            bits = bits |(1 << d);
            n = n / 10;
        }
        return false;
    }

    static List<List<Integer>> combinationOf(List<Integer> ixa,List<Integer> ixb,est_iface tester)
    {
        List<List<Integer>> results = new ArrayList<>();
        for (int idxa : ixa)
            for (int idxb : ixb)
            {
                if (tester.checkVals(idxa,idxb))
                    results.add(Arrays.asList(idxa,idxb));
            }
        return results;
    }

    static List<Integer> intSetFromTo(int st,int en)
    {
        List<Integer> mixs = new ArrayList<>();
        for (int i = st;i <= en;i++)
            mixs.add(i);
        return mixs;
    }

    public String layoutName()
    {
        return "mastera";
    }

    public void miscSetUp()
    {
        wrongMode = "u";
        textSize = (Float.parseFloat(eventAttributes.get("textsize")));
        loadEvent(wrongMode);
        OBControl textboxl = objectDict.get("textboxl");
        textPosLeft = textboxl.position();
        OBControl textboxr = objectDict.get("textboxr");
        textPosRight = new PointF();
        textPosRight.set(textboxr.position());
        OBControl smallrect = objectDict.get("rectnuml_0");
        labScale = smallrect.height()  / textboxl.height();
        font = OBUtils.StandardReadingFontOfSize(textSize);
        for(OBControl c : filterControls("count.*"))
        {
            PointF pt = c.position();
            c.setProperty("origpos",new PointF(pt.x,pt.y));
        }
        hideControls("(line|count|cont).*");
        score = 0;

        presenter = OBPresenter.characterWithGroup((OBGroup) objectDict.get("presenter"));
        presenter.control.setZPosition(200);
        PointF pt = presenter.control.position();
        presenter.control.setProperty("restpos",new PointF(pt.x,pt.y));
        presenter.control.setRight(0);
        presenter.control.show();
    }

    public void prepare()
    {
        super.prepare();
        loadEvent(layoutName());
        loadFingers();
        events = Arrays.asList(eventAttributes.get("scenes").split(","));
        miscSetUp();
        doVisual(currentEvent());
    }

    public void start()
    {
        setStatus(0);
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            public void run() throws Exception
            {
                try
                {
                    if(!performSel("demo",currentEvent()) )
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    public void numbers(int[] ns,int i)
    {
        int q3 = 1;
        int n1=765,n2=999;
        List<Integer>ixa,ixb;
        List<List<Integer>> combos = null;
        List<Integer> idxs;
        switch(i)
        {
            case 0:                             // units only
                ixa = OC_EgmaSubTest1.intSetFromTo(1,9);
                ixb = ixa;
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return idxb > idxa;
                    }
                });
                break;
            case 1:
                ixa = OC_EgmaSubTest1.intSetFromTo(2,9);
                ixb = ixa;
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return idxb - idxa == 2 || idxb - idxa == 3;
                    }
                });
                break;
            case 2:
                ixa = OC_EgmaSubTest1.intSetFromTo(12,19);
                ixb = OC_EgmaSubTest1.intSetFromTo(21,29);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) &&(idxb - idxa != 10);
                    }
                });
                break;
            case 3:
                ixa = Arrays.asList(q3);
                ixb = OC_EgmaSubTest1.intSetFromTo(31,39);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) &&(idxb - idxa != 10);
                    }
                });
                break;
            case 4:
                ixa = OC_EgmaSubTest1.intSetFromTo(41,49);
                ixb = OC_EgmaSubTest1.intSetFromTo(51,59);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) &&(idxb - idxa != 10);
                    }
                });
                break;
            case 5:
                ixa = OC_EgmaSubTest1.intSetFromTo(61,69);
                ixb = ixa;
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        int diff = Math.abs(idxb - idxa);
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) && diff > 1 && diff < 5;
                    }
                });
                break;
            case 6:
                ixa = OC_EgmaSubTest1.intSetFromTo(61,69);
                ixb = OC_EgmaSubTest1.intSetFromTo(71,79);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) && idxb - idxa == 10;
                    }
                });
                break;
            case 7:
            case 8:
            case 9:
                ixa = OC_EgmaSubTest1.intSetFromTo(40,99);
                ixb = OC_EgmaSubTest1.intSetFromTo(40,99);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) && idxb - idxa >= 10;
                    }
                });
                break;
            case 10:
                ixa = OC_EgmaSubTest1.intSetFromTo(80,99);
                ixb = OC_EgmaSubTest1.intSetFromTo(90,99);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) && idxb - idxa > 1;
                    }
                });
                break;
            case 70:
                ixa = OC_EgmaSubTest1.intSetFromTo(123,149);
                ixb = OC_EgmaSubTest1.intSetFromTo(133,159);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        int diga = digitof(idxa, 0);
                        int digb = digitof(idxb, 0);
                        if(diga < 2 || digb < 2 || diga == digb)
                            return false;
                        diga = digitof(idxa, 1);
                        digb = digitof(idxb, 1);
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb) && digb - diga == 1;
                    }
                });
                break;
            case 80:
                ixa = OC_EgmaSubTest1.intSetFromTo(391,398);
                ixb = OC_EgmaSubTest1.intSetFromTo(412,439);
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        int diga = digitof(idxa, 0);
                        int digb = digitof(idxb, 0);
                        if(diga < 1 || digb < 1 || diga == digb)
                            return false;
                        return !hasRepeatedDigit(idxa) && !hasRepeatedDigit(idxb);
                    }
                });
                break;
            case 90:
                ixa = OC_EgmaSubTest1.intSetFromTo(705,795);
                ixb = ixa;
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        int diga = digitof(idxa, 0);
                        int digb = digitof(idxb, 0);
                        return diga == 5 && digb == 0;
                    }
                });
                break;
            case 100:
                ixa = OC_EgmaSubTest1.intSetFromTo(900,999);
                ixb = ixa;
                combos = combinationOf(ixa, ixb, new est_iface()
                {
                    public boolean checkVals(int idxa, int idxb)
                    {
                        int diga = digitof(idxa, 0);
                        int digb = digitof(idxb, 0);
                        if(diga == digb || !(diga == 3 || diga == 5 || diga == 7) || !(digb == 3 || digb == 5 || digb == 7))
                            return false;
                        diga = digitof(idxa, 1);
                        digb = digitof(idxb, 1);
                        return diga == digb && diga != 0;
                    }
                });
                break;
            default:
                break;
        }
        if(combos.size()  > 0)
        {
            idxs = OBUtils.RandomObjectFromArray(combos);
            n1 = idxs.get(0);
            n2 = idxs.get(1);
        }
        if(i == 2)
            q3 = n2;
        if(rndom() < 0.5)
        {
            int temp = n1;
            n1 = n2;
            n2 = temp;
        }
        ns[0] = n1;
        ns[1] = n2;
    }

    public OBLabel mainLabelForNumber(int n,PointF pt)
    {
        String s = String.format("%d",n);
        OBLabel l = new OBLabel(s,font);
        l.setZPosition(10);
        l.setPosition(pt);
        l.setColour(Color.BLACK);
        l.setProperty("origpos",new PointF(pt.x,pt.y));
        l.setProperty("offpos",new PointF(pt.x,bounds().height() + l.height()));
        l.setProperty("number",n);
        l.setBottom(-1);
        attachControl(l);
        return l;
    }
    public void setSceneXX(String  scene)
    {
        if(leftLabel != null)
        {
            detachControl(leftLabel);
            detachControl(rightLabel);
        }
        int nidx = Integer.parseInt(scene);
        if(wrongMode.equals("u"))
            nidx = 0;
        numbers(nums,nidx);
        leftLabel = mainLabelForNumber(nums[0],textPosLeft);
        rightLabel = mainLabelForNumber(nums[1],textPosRight);
        hideControls("(line|count).*");

        targets = Arrays.asList((OBControl)leftLabel,rightLabel);
        if(nums[0] > nums[1])
            target = leftLabel;
        else
            target = rightLabel;
    }

    public void setSceneintro()
    {

    }

    public void doMainXX() throws Exception
    {
        bringInNumbers();
        setReplayAudio((List<Object>)(Object)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",false);
        waitForSecs(0.3f);
    }

    public void moveToPosition(String property,List<OBControl> objs,float dursecs,float scale)
    {
        List<OBAnim>arr = new ArrayList<>();
        for(OBControl c : objs)
        {
            PointF pt = (PointF)c.propertyValue(property);
            OBAnim anim = OBAnim.moveAnim(pt,c);
            arr.add(anim);
            if(scale != 1)
            {
                anim = OBAnim.scaleAnim(scale,c);
                arr.add(anim);
            }
        }
        OBAnimationGroup.runAnims(arr,dursecs,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void moveToPositionOneByOne(String property,List<OBControl>objs,float dursecs,float scale) throws Exception
    {
        float duration = dursecs / objs.size();
        for(OBControl c : objs)
        {
            OBAnim manim = OBAnim.moveAnim((PointF) c.propertyValue(property),(c));
            OBAnimationGroup.runAnims(Arrays.asList(manim), duration, false, OBAnim.ANIM_EASE_IN, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    playSfxAudio("drop",false);
                }
            },this);
            waitForSecs(duration * 0.8);
        }
    }

    public void bringInNumbers() throws Exception
    {
        playSfxAudio("slide",false);
        moveToPosition("origpos",Arrays.asList((OBControl)leftLabel,rightLabel),0.3f,1);
    }

    public void demointro() throws Exception
    {
        presenter.walk((PointF) presenter.control.propertyValue("restpos"));
        presenter.faceFront();
        waitForSecs(0.2f);
        presenter.speak((List<Object>)(Object)currentAudio("DEMO"),this);
        waitForSecs(1f);
        presenter.speak((List<Object>)(Object)currentAudio("DEMO2"),this);
        waitForSecs(0.2f);
        OBControl faceright = presenter.control.objectDict.get("faceright");
        PointF currPos = presenter.control.position();
        PointF lp = presenter.control.convertPointFromControl(new PointF(0, 0),faceright);
        PointF destpos = new PointF(bounds().width() + (presenter.control.width()/2f) - lp.x, currPos.y);
        presenter.walk(destpos);
        detachControl(presenter.control);
        presenter = null;
        nextScene();
    }

    public void highlightLabels(List<OBLabel> labs,boolean high)
    {
        lockScreen();
        int col = high?Color.RED :Color.BLACK;
        for(OBLabel lab : labs)
            lab.setColour(col);
        unlockScreen();
    }

    public List layOutString(String text,OBLabel mLabel,OBFont fnt,List<OBControl> destRects)
    {
        OBLabel label = new OBLabel(text,fnt);
        List<Float> lefts = new ArrayList<>();
        for(int i = 0;i < text.length();i++)
        {
            float f = label.textOffset(i);
            lefts.add(f);
        }
        List labs = new ArrayList<>();
        for(int i = 0;i < text.length();i++)
        {
            String chars = text.substring(i,i + 1);
            OBLabel l = new OBLabel(chars,font);
            l.setColour(Color.BLACK);
            l.setPosition(mLabel.position());
            l.setZPosition(mLabel.zPosition() +1);
            l.setLeft(mLabel.left() + lefts.get(i));
            l.setProperty("origpos",new PointF(l.position().x,l.position().y));
            OBControl destrect = destRects.get(text.length() -i-1);
            l.setProperty("destpos",new PointF(destrect.position().x,destrect.position().y));
            labs.add(l);
            attachControl(l);
        }
        return labs;
    }

    public void showLines(List<OBControl> lines,int n)
    {
        if(lines.size()  == 0)
            return;
        if(n > 9)
        {
            lines.get(1).show();
            if(n > 99)
                lines.get(0).show();
        }
    }

    public void showContainers(String side,int n)
    {
        objectDict.get(String.format("container%s1",side)).show();
        if(n > 9)
        {
            objectDict.get(String.format("container%s10",side)).show();
        }
    }

    public void reposition(List<OBControl>objs,int digit)throws Exception
    {
        float screenh = bounds().height();
        List<OBControl> arr = new ArrayList<>();
        lockScreen();
        for(int i = 0;i < digit;i++)
        {
            OBControl c = objs.get(i);
            c.show();
            PointF pos = c.position();
            c.setProperty("posa",new PointF(pos.x,pos.y));
            c.setPosition(pos.x,pos.y - screenh);
            arr.add(c);
        }
        unlockScreen();
        moveToPositionOneByOne("posa",arr,arr.size()  * 0.2f,objs.get(0).scale() );
    }

    public void showCountObjects(String infix,int n) throws Exception
    {
        List spquantities = (List<Object>) ((Map<String,Object>)audioScenes.get("1")).get("POWER");;
        List spnumbers = (List<Object>) ((Map<String,Object>)audioScenes.get("1")).get("COUNT");;
        List<OBControl>nrects = sortedFilteredControls(String.format("rectnum%s_.",infix));
        boolean mustdo = false;
        int digit = digitof(n, 2);
        if(digit > 0)
        {
            mustdo = true;
            List<OBControl>hundreds = sortedFilteredControls(String.format("count%s100_.",infix));
            PointF av = averageBottomPoint(hundreds.get(0),hundreds.get(1));
            reposition(hundreds,digit);
            movePointerToPoint(av,-1,true);
            //playAudioQueued(@spquantities.get(0).() ,true);
            waitForSecs(0.3f);
            moveToPosition("origpos",hundreds,0.3f,1);
            movePointerToPoint(OB_Maths.locationForRect(0.6f, 0.9f, nrects.get(2).frame()),-1,true);
            playAudioQueued(Arrays.asList(spnumbers.get(digit)),true);
            waitForSecs(0.4f);
        }

        digit = digitof(n, 1);
        if(digit > 0 || mustdo)
        {
            mustdo = true;
            List<OBControl>tens = sortedFilteredControls(String.format("count%s10_.",infix));
            PointF av = averageBottomPoint(tens.get(0),tens.get(1));
            movePointerToPoint(av,-1,true);
            //playAudioQueued(@spquantities.get(1).() ,true);
            reposition(tens,digit);
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.6f, 0.9f, nrects.get(1).frame()),-1,true);
            playAudioQueued(Arrays.asList(spnumbers.get(digit)),true);
            waitForSecs(0.4f);
        }
        digit = digitof(n, 0);
        if(digit > 0 || mustdo)
        {
            mustdo = true;
            List<OBControl>units = sortedFilteredControls(String.format("count%s1_.",infix));
            PointF av = averageBottomPoint(units.get(0),units.get(1));
            movePointerToPoint(av,-1,true);
            //playAudioQueued(@spquantities.get(2).() ,true);
            reposition(units,digit);
            waitForSecs(0.3f);
            movePointerToPoint(OB_Maths.locationForRect(0.6f, 0.9f, nrects.get(0) .frame()),-1,true);
            if(!wrongMode.equals("u"))
                playAudioQueued(Arrays.asList(spnumbers.get(digit)),true);
            waitForSecs(0.4f);
        }
    }

    public PointF averageBottomPoint(OBControl o1,OBControl o2)
    {
        PointF pt = new PointF();
        pt.y = (o1.bottom());
        pt.x = ((o1.left()  + o2.right() ) / 2);
        return pt;
    }

    public void playNumber(int n) throws OBUserPressedBackException
    {
        String fn = String.format("nums/n_%d",n);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public void doTheWrongThing()throws Exception
    {
        Map<String,List> aud1 = (Map<String, List>) audioScenes.get("1");
        playAudioQueued(aud1.get("INCORRECT"),true);
        lockScreen();
        leftLabs = layOutString(leftLabel.text(),leftLabel,font,sortedFilteredControls("rectnuml.*"));
        rightLabs = layOutString(rightLabel.text(),rightLabel,font,sortedFilteredControls("rectnumr.*"));
        leftLabel.hide();
        rightLabel.hide();
        unlockScreen();
        List<OBControl> temparr = new ArrayList<>();
        temparr.addAll(leftLabs);
        temparr.addAll(rightLabs);
        moveToPosition("destpos",temparr,0.4f,labScale);
        waitForSecs(0.2f);
        lockScreen();
        showContainers("l",nums[0]);
        showContainers("r",nums[1]);
        unlockScreen();
        waitForSecs(0.2f);
        playAudioQueued(aud1.get("INCORRECT2") ,true);
        PointF firstPt = OB_Maths.locationForRect(0.5f,0.8f,bounds());
        PointF startpt = pointForDestPoint(firstPt,25);
        loadPointerStartPoint(startpt,firstPt);

        showCountObjects("l",nums[0]);
        waitForSecs(0.4f);
        highlightLabels(leftLabs,true);
        playNumber(nums[0]);
        highlightLabels(leftLabs,false);
        showCountObjects("r",nums[1]);
        waitForSecs(0.4f);
        highlightLabels(rightLabs,true);
        playNumber(nums[1]);
        highlightLabels(rightLabs,false);
        waitForSecs(0.4f);

        List<OBLabel> desta =(nums[0] > nums[1])?leftLabs:rightLabs;
        PointF destpt = OB_Maths.locationForRect(1, 0.6f,desta.get(desta.size()-1).frame());
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.2f);
        playAudioQueued(aud1.get("INCORRECT3"),true);

        waitForSecs(0.4f);

        thePointer.hide();
        fin();
    }

    public OBLabel smallLabelForNumber(int n,OBControl bg,OBFont fnt)
    {
        String s = String.format("%d",n);
        OBLabel l = new OBLabel(s,fnt);
        l.setZPosition(bg.zPosition() +1);
        l.setPosition(bg.position() );
        l.setColour(Color.WHITE);
        (bg.parent).insertMember(l,0,s);
        return l;
    }

    public void setUpFinale()
    {
        lockScreen();
        if(leftLabel != null)
        {
            detachControl(leftLabel);
            detachControl(rightLabel);
            if (leftLabs != null)
            {
                List<OBLabel> temparr = new ArrayList<>(leftLabs);
                temparr.addAll(rightLabs);
                for(OBControl c : temparr)
                    detachControl(c);
            }
        }
        deleteControls("(rect|line|count|cont).*");
        loadEvent("finale");
        float txsize = (Float.parseFloat(eventAttributes.get("textsize")));

        OBFont fnt = OBUtils.UnscaledReadingFontOfSize(txsize);
        OBGroup machine = (OBGroup)objectDict.get("machine");
        OBControl swatch = objectDict.get("swatch");
        int greycol = swatch.fillColor();
        for(int i = 1;i <= 10;i++)
        {
            OBControl sq = machine.objectDict.get(String.format("squ_%d",i));
            smallLabelForNumber(i,sq,fnt);
            sq.setProperty("origcol",sq.fillColor());
            sq.setFillColor(greycol);
            OBControl star = machine.objectDict.get(String.format("star_%d",i));
            star.setProperty("origcol",star.fillColor());
            star.setFillColor(greycol);
        }
        unlockScreen();
    }

    public void fin()
    {
        try
        {
            waitForSecs(0.5f);
            setUpFinale();
            waitForSecs(0.5f);
            playAudioQueued(((Map<String,List<Object>>)(audioScenes.get("finale"))).get("DEMO") ,true);
            waitForSecs(0.5f);
            animateFinale();
            waitForSecs(0.5f);
        } catch (Exception e)
        {
        }
        exitEvent();
     }

    public void animateHammerStrike() throws Exception
    {
        OBControl hammer = objectDict.get("hammer");
        OBControl hammer2 = objectDict.get("hammer2");
        float angle1 = hammer.rotation();
        float angle2 = hammer2.rotation();
        PointF pos1 = new PointF();
        pos1.set(hammer.position());
        PointF pos2 = new PointF();
        pos2.set(hammer2.position());
        moveObjects(Arrays.asList(hammer),pos2,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
        OBAnim rotAnim = OBAnim.rotationAnim(angle2,hammer);
        OBAnimationGroup.runAnims(Arrays.asList(rotAnim),0.1,true,OBAnim.ANIM_EASE_IN,null);
        playSfxAudio("hit",false);
        waitForSecs(0.02f);
        rotAnim = OBAnim.rotationAnim(angle1,hammer);
        OBAnimationGroup.runAnims(Arrays.asList(rotAnim),0.1,true,OBAnim.ANIM_EASE_IN,null);
        moveObjects(Arrays.asList(hammer),pos1,-1,OBAnim.ANIM_EASE_IN_EASE_OUT);
    }

    static String CatNameForIdx(String c,int idx)
    {
        if(idx > 1)
            return String.format("%s%d",c,idx);
        return c;
    }

    public void animateFinale() throws Exception
    {
        animateHammerStrike();
        waitForSecs(0.2f);
        OBGroup machine = (OBGroup)objectDict.get("machine");
        for(int i = 1;i <= score;i++)
        {
            OBControl sq = machine.objectDict.get(String.format("squ_%d",i));
            OBControl star = machine.objectDict.get(String.format("star_%d",i));
            playSfxAudio("ding",false);
            lockScreen();
            sq.setFillColor((Integer)sq.propertyValue("origcol"));
            star.setFillColor((Integer)star.propertyValue("origcol"));
            unlockScreen();
            waitForSecs(0.5f);
        }
        waitForSecs(0.5f);
        String cat = CatNameForIdx("SUMMARY",score + 1);
        OBConditionLock audioLock = playAudioQueued(((Map<String,List<Object>>)(audioScenes.get("finale"))).get(cat) ,false);
        if(score > 0)
        {
            OBControl swatch = objectDict.get("swatch");
            int greycol = swatch.fillColor();
            OBControl sq = machine.objectDict.get(String.format("squ_%d",score));
            int col = (Integer)sq.propertyValue("origcol");
            for(int i = 0;i < 5;i++)
            {
                sq.setFillColor(greycol);
                waitForSecs(0.3f);
                sq.setFillColor(col);
                waitForSecs(0.3f);
            }
        }
        waitAudioQueue(audioLock);
        waitForSecs(0.3f);
    }

    public void checkTarget(OBControl targ)
    {
        try
        {
            highlightLabels(Arrays.asList((OBLabel)targ),true);
            playSfxAudio("tap",true);
            if(targ == target)
            {
                score++;
                gotItRightBigTick(true);
                waitForSecs(0.2f);
                playNumber((Integer)targ.propertyValue("number"));
                highlightLabels(Arrays.asList((OBLabel)targ),false);
                waitForSecs(0.2f);
                playSfxAudio("slide",false);
                moveToPosition("offpos",Arrays.asList((OBControl)leftLabel,rightLabel),0.3f,1);
                nextScene();
            }
            else
            {
                displayCross();
                highlightLabels(Arrays.asList((OBLabel)targ),false);
                doTheWrongThing();
            }
        }
        catch(Exception exception)
        {
        }
    }

    OBControl findTarget(PointF pt)
    {
        return finger(-1,2,targets,pt);
    }

    public void touchDownAtPoint(final PointF pt,View v)
    {
        if(status()  == STATUS_AWAITING_CLICK)
        {
            final OBControl obj = findTarget(pt);
            setStatus(STATUS_CHECKING);
            if(obj != null)
            {
                OBUtils.runOnOtherThread(new OBUtils.RunLambda()
                {
                    public void run() throws Exception
                    {
                        checkTarget(obj);
                    }
                });
            }
            else
                switchStatus(currentEvent());
        }
    }

}
