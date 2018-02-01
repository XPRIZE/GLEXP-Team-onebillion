package org.onebillion.onecourse.mainui.oc_prepm;

import android.graphics.Color;
import android.graphics.PointF;
import android.view.View;

import org.onebillion.onecourse.controls.*;
import org.onebillion.onecourse.mainui.OC_SectionController;
import org.onebillion.onecourse.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by alan on 29/01/2018.
 */

public class OC_PrepM1 extends OC_SectionController
{
    final static int EST1_UNITS = 0,
            EST1_TENS = 1,
            EST1_HUNDREDS = 2;
    PointF textPosLeft,textPosRight;
    float textSize,labScale;
    OBLabel leftLabel,rightLabel;
    OBFont font;
    int nums[] = new int[2];
    int score;
    List leftLabs,rightLabs;
    String wrongMode;
    int testMode;
    int q3;
    float bottomY;

    interface callblock
    {
        public boolean process(int ia,int ib);
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

    static List<List<Integer>> combinationOf(List<Integer> ixa,List<Integer> ixb,callblock cb)
    {
        List<List<Integer>> results = new ArrayList<>();
        for (Integer ia : ixa)
            for (Integer ib : ixb)
            {
                if (cb.process(ia,ib))
                    results.add(Arrays.asList(ia,ib));
            }
        return results;
    }

    static List<Integer> intSetFromTo(int st,int en)
    {
        List<Integer> lst = new ArrayList<>();
        for (int i = st;i < en;i++)
            lst.add(i);
        return lst;
    }

    public String layoutName()
    {
        return "mastera";
    }

    public void miscSetUp()
    {
        wrongMode = "u";
        textSize = (Float.parseFloat(eventAttributes.get("textsize")));
        String tm = OBUtils.coalesce(parameters.get("testmode") ,"units");
        if(tm.equals("tens"))
        {
            testMode = EST1_TENS;
            wrongMode = "tu";
        }
        else if(tm.equals("hundreds"))
        {
            testMode = EST1_HUNDREDS;
            wrongMode = "htu";
        }
        else
            testMode = EST1_UNITS;
        loadEvent(wrongMode);
        OBControl textboxl = objectDict.get("textboxl");
        textPosLeft = textboxl.position();
        OBControl textboxr = objectDict.get("textboxr");
        textPosRight = textboxr.position();
        OBControl smallrect = objectDict.get("rectnuml_0");
        labScale = smallrect.height() / textboxl.height();
        font = OBUtils.StandardReadingFontOfSize(textSize);
        for(OBControl c : filterControls("count.*"))
            c.setProperty("origpos",new PointF(c.position().x,c.position().y));
        hideControls("(line|count|cont).*");
        score = 0;
        bottomY = objectDict.get("countl1_1").bottom();
        objectDict.get("presenter").hide();
        q3 = 1;
    }

    public void prepare()
    {
        super.prepare();
        loadEvent(layoutName());
        loadFingers();
        events = new ArrayList<>(Arrays.asList(eventAttributes.get("scenes").split(",")));
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
                    if(!performSel("demo",currentEvent()))
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

    public List<List<Integer>> unitsNumbersForScene(int i)
    {
        List<Integer> ixa,ixb;
        List combos = null;
        ixa = intSetFromTo(1,9);
        ixb = ixa;
        combos = combinationOf(ixa, ixb, new callblock()
        {
            @Override
            public boolean process(int ia, int ib)
            {
                return ib > ia;
            }
        });
        return combos;
    }

    public List<List<Integer>> tensNumbersForScene(int i)
    {
        List<Integer> ixa,ixb;
        List<List<Integer>> combos = null;
        switch(i)
        {
            case 1:
                ixa = OC_PrepM1.intSetFromTo(2,9);
                ixb = ixa;
                combos = combinationOf(ixa, ixb, new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return ib - ia == 2 || ib - ia == 3;
                    }
                });
            break;
            case 2:
                ixa = OC_PrepM1.intSetFromTo(12,19);
                ixb = OC_PrepM1.intSetFromTo(21,29);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && (ib - ia != 10);
                    }
                });
            break;
            case 3:
                ixa = Arrays.asList(q3);
                ixb = OC_PrepM1.intSetFromTo(31,39);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && (ib - ia != 10);
                    }
                });
            break;
            case 4:
                ixa = OC_PrepM1.intSetFromTo(41,49);
                ixb = OC_PrepM1.intSetFromTo(51,59);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && (ib - ia != 10);
                    }
                });
            break;
            case 5:
                ixa = OC_PrepM1.intSetFromTo(61,69);
                ixb = ixa;
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        int diff = Math.abs(ib - ia);
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && diff > 1 && diff < 5;
                    }
                });
            break;
            case 6:
                ixa = OC_PrepM1.intSetFromTo(61,69);
                ixb = OC_PrepM1.intSetFromTo(71,79);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && ib - ia == 10;
                    }
                });
            break;
            case 7:
            case 8:
            case 9:
                ixa = OC_PrepM1.intSetFromTo(40,99);
                ixb = OC_PrepM1.intSetFromTo(40,99);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && ib - ia >= 10;
                    }
                });
            break;
            case 10:
                ixa = OC_PrepM1.intSetFromTo(80,99);
                ixb = OC_PrepM1.intSetFromTo(90,99);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && ib - ia > 1;
                    }
                });
            break;
            default:
                break;
        }
        return combos;
    }

    public List<List<Integer>> hundredsNumbersForScene(int i)
    {
        List<Integer> ixa,ixb;
        List combos = null;
        switch(i)
        {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                combos = tensNumbersForScene(i);
                break;
            case 7:
                ixa = OC_PrepM1.intSetFromTo(123,149);
                ixb = OC_PrepM1.intSetFromTo(133,159);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        int diga = digitof(ia, 0);
                        int digb = digitof(ib, 0);
                        if (diga < 2 || digb < 2 || diga == digb)
                            return false;
                        diga = digitof(ia, 1);
                        digb = digitof(ib, 1);
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib) && digb - diga == 1;
                    }
            });
            break;
            case 8:
                ixa = OC_PrepM1.intSetFromTo(391,398);
                ixb = OC_PrepM1.intSetFromTo(412,439);
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        int diga = digitof(ia, 0);
                        int digb = digitof(ib, 0);
                        if (diga < 1 || digb < 1 || diga == digb)
                            return false;
                        return !hasRepeatedDigit(ia) && !hasRepeatedDigit(ib);
                    }
            });
            break;
            case 9:
                ixa = OC_PrepM1.intSetFromTo(305,395);
                ixb = ixa;
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        int diga = digitof(ia, 0);
                        int digb = digitof(ib, 0);
                        return diga == 5 && digb == 0;
                    }
            });
            break;
            case 10:
                ixa = OC_PrepM1.intSetFromTo(300,399);
                ixb = ixa;
                combos = combinationOf(ixa, ixb,new callblock()
                {
                    @Override
                    public boolean process(int ia, int ib)
                    {
                        int diga = digitof(ia, 0);
                        int digb = digitof(ib, 0);
                        if (diga == digb || !(diga == 3 || diga == 5 || diga == 7) || !(digb == 3 || digb == 5 || digb == 7))
                            return false;
                        diga = digitof(ia, 1);
                        digb = digitof(ib, 1);
                        return diga == digb && diga != 0;
                    }
            });
            break;
            default:
                break;
        }
        return combos;
    }

    public void numbers(int[] ns,int i)
    {
        int n1=765,n2=999;
        List<List<Integer>> combos = null;
        switch(testMode)
        {
            case EST1_HUNDREDS:
                combos = hundredsNumbersForScene(i);
                break;
            case EST1_TENS:
                combos = tensNumbersForScene(i);
                break;
            case EST1_UNITS:
            default:
                combos = unitsNumbersForScene(i);
                break;
        }
        if(combos.size() > 0)
        {
            List<Integer> idxs = OBUtils.RandomObjectFromArray(combos);
            n1 = idxs.get(0);
            n2 = idxs.get(1);
        }
        if(i == 2)
            q3 = n2;
        if(OB_Maths.rndom() < 0.5)
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
        l.setProperty("origpos",pt);
        l.setProperty("offpos",new PointF(pt.x, bounds() .height() + l.height()));
        l.setProperty("number",n);
        l.setBottom(-1);
        attachControl(l);
        return l;
    }
    public void setSceneXX(String scene)
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
        hideControls("(line|count) .*");

        targets = (List)Arrays.asList(leftLabel,rightLabel);
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
        setReplayAudio((List)currentAudio("PROMPT.REPEAT"));
        playAudioQueuedScene("PROMPT",0.3f,false);
        waitForSecs(0.3f);
    }


    public void moveToPosition(String property,List<OBControl> objs,float dursecs,float scale) throws Exception
    {
        List arr = new ArrayList<>();
        for(OBControl c : objs)
        {
            PointF pt = (PointF) c.propertyValue(property);
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

    public void moveToPositionOneByOne(String property,List<OBControl> objs,float dursecs,float scale) throws Exception
    {
        float duration = dursecs / objs.size();
        for(OBControl c : objs)
        {
            OBAnim manim = OBAnim.moveAnim((PointF) c.propertyValue(property),c);
            OBAnimationGroup.runAnims(Arrays.asList(manim), duration, false, OBAnim.ANIM_EASE_IN, new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    playSfxAudio("drop",false);
                }
            },this);
        }
        waitForSecs(duration * 0.8);
    }

    public void bringInNumbers() throws Exception
    {
        playSfxAudio("slide",false);
        moveToPosition("origpos",(List)Arrays.asList(leftLabel,rightLabel),0.3f,1);
    }

    public void demointro() throws Exception
    {
        /*playAudioQueuedScene("DEMO",true);
        waitForSecs(0.4f);
        playAudioQueuedScene("DEMO2",true);
        waitForSecs(0.4f);*/
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
        if(lines.size() == 0)
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

    public void reposition(List<OBControl> objs,int digit) throws Exception
    {
        float screenh = bounds() .height();
        List arr = new ArrayList<>();
        lockScreen();
        for(int i = 0;i < digit;i++)
        {
            OBControl c = objs.get(i);
            c.show();
            PointF pos = new PointF();
            pos.set(c.position());
            c.setProperty("posa",pos);
            c.setPosition(pos.x,pos.y - screenh);
            arr.add(c);
        }
        unlockScreen();
        moveToPositionOneByOne("posa",arr,arr.size() * 0.2f,objs.get(0).scale());
    }

    public void showHundreds(String infix,int n) throws Exception
    {
        //List spquantities = audioScenes.get("1") .get("POWER");
        List spnumbers = ((Map<String,List>)audioScenes.get("1")).get("COUNT");
        List<OBControl>nrects = sortedFilteredControls(String.format("rectnum%s_.",infix));
        boolean mustdo = false;
        int digit = digitof(n, 2);
        if(digit > 0)
        {
            mustdo = true;
            List objs = new ArrayList<>();
            OBControl c = objectDict.get("countl100_1");
            float boty = bottomY;
            OBControl box = nrects.get(2);
            float x = box.position() .x;
            for(int i = 0;i < digit;i++)
            {
                OBControl newc = c.copy();
                PointF pos = new PointF(x, 0);
                lockScreen();
                newc.setPosition(pos);
                newc.setBottom(boty);
                pos = new PointF();
                pos.set(newc.position());
                newc.setProperty("destpos",(pos));
                newc.setBottom(-10);
                newc.show();
                objs.add(newc);
                attachControl(newc);
                unlockScreen();
                moveObjects(Arrays.asList(newc),pos,-1.6f,OBAnim.ANIM_EASE_IN);
                playSfxAudio("drop",false);
                boty = newc.top();
            }
        }
        waitForSecs(0.1);
        waitSFX();
        playAudioQueued(Arrays.asList(spnumbers.get(digit)),true);
        waitForSecs(0.4f);
    }

    public void showTens(String infix,int n) throws Exception
    {
        //List spquantities = audioScenes.get("1") .get("POWER");
        List spnumbers = ((Map<String,List>)audioScenes.get("1")).get("COUNT");
        List<OBControl>nrects = sortedFilteredControls(String.format("rectnum%s_.",infix));
        boolean mustdo = false;
        int digit = digitof(n, 1);
        if(digit > 0)
        {
            mustdo = true;
            List objs = new ArrayList<>();
            OBControl c = objectDict.get("countl10_1");
            float y = bottomY - c.height() / 2.0f;
            OBControl box = nrects.get(1);
            float x = box.position() .x -((digit / 2.0f) -0.5f) * c.width();
            for(int i = 0;i < digit;i++)
            {
                OBControl newc = c.copy();
                PointF pos = new PointF(x, y);
                lockScreen();
                newc.setPosition(pos);
                pos = new PointF();
                pos.set(newc.position());
                newc.setProperty("destpos",pos);
                newc.setBottom(-10);
                newc.show();
                objs.add(newc);
                attachControl(newc);
                unlockScreen();
                moveObjects(Arrays.asList(newc),pos,-1.6f,OBAnim.ANIM_EASE_IN);
                playSfxAudio("drop",false);
                x += newc.width();
            }
        }
        waitForSecs(0.1);
        waitSFX();
        playAudioQueued(Arrays.asList(spnumbers.get(digit)),true);
        waitForSecs(0.4f);
    }

    public void showUnits(String infix,int n) throws Exception
    {
        //List spquantities = audioScenes.get("1") .get("POWER");
        List spnumbers = ((Map<String,List>)audioScenes.get("1")).get("COUNT");
        List<OBControl>nrects = sortedFilteredControls(String.format("rectnum%s_.",infix));
        boolean mustdo = false;
        int digit = digitof(n, 0);
        if(digit > 0)
        {
            mustdo = true;
            List objs = new ArrayList<>();
            OBControl c = objectDict.get("countl1_1");
            float boty = bottomY;
            OBControl box = nrects.get(0);
            float x = box.position() .x;
            for(int i = 0;i < digit;i++)
            {
                OBControl newc = c.copy();
                PointF pos = new PointF(x, 0);
                lockScreen();
                newc.setPosition(pos);
                newc.setBottom(boty);
                pos = new PointF();
                pos.set(newc.position());
                newc.setProperty("destpos",(pos));
                newc.setBottom(-10);
                newc.show();
                objs.add(newc);
                attachControl(newc);
                unlockScreen();
                moveObjects(Arrays.asList(newc),pos,-1.6f,OBAnim.ANIM_EASE_IN);
                playSfxAudio("drop",false);
                boty = newc.top();
            }
        }
        waitForSecs(0.1);
        waitSFX();
        if (nums[0] > 9)
        {
            playAudioQueued(Arrays.asList(spnumbers.get(digit)),true);
            waitForSecs(0.4f);
        }
    }

    public void playNumber(int n) throws Exception
    {
        //String fn = String.format("nums/n_%d",n);
        String fn = String.format("n_%d",n);
        playAudioQueued(Arrays.asList((Object)fn),true);
    }

    public List animsToMoveLabelsTogether(List<OBControl> labs)
    {
        if(labs.size() < 2)
            return new ArrayList();
        float y = labs.get(0).position() .y;
        List anims = new ArrayList<>();
        if(labs.size() == 2)
        {
            float x =(labs.get(0).position() .x + labs.get(1).position() .x) / 2;
            anims.add(OBAnim.moveAnim(new PointF(x - labs.get(0).width() / 2, y),labs.get(0)));
            anims.add(OBAnim.moveAnim(new PointF(x + labs.get(1).width() / 2, y),labs.get(1)));
        }
        else
        {
            float x = labs.get(1).position() .x;
            anims.add(OBAnim.moveAnim(new PointF(x - labs.get(1).width() / 2 - labs.get(0).width() / 2, y),labs.get(0)));
            anims.add(OBAnim.moveAnim(new PointF(x + labs.get(2).width() / 2 + labs.get(1).width() / 2, y),labs.get(2)));
        }
        return anims;
    }

    public void moveLabelsTogether(List<OBControl> labs) throws Exception
    {
        if(labs.size() < 2)
            return;
        List anims = animsToMoveLabelsTogether(labs);
        OBAnimationGroup.runAnims(anims,0.4,true,OBAnim.ANIM_EASE_IN_EASE_OUT,null);
    }

    public void doTheWrongThing() throws Exception
    {
        playAudioQueuedScene("1","INCORRECT",true);
        lockScreen();
        leftLabs = layOutString(leftLabel.text(),leftLabel,font,sortedFilteredControls("rectnuml.*"));
        rightLabs = layOutString(rightLabel.text(),rightLabel,font,sortedFilteredControls("rectnumr.*"));
        leftLabel.hide();
        rightLabel.hide();
        unlockScreen();
        List<OBControl> alllabs = new ArrayList<>(leftLabs);
        alllabs.addAll(rightLabs);
        moveToPosition("destpos",alllabs,0.4f,labScale);
        waitForSecs(0.2f);
        playAudioQueuedScene("1","INCORRECT2",true);
        PointF firstPt = OB_Maths.locationForRect(new PointF(0.5f,0.8f) ,bounds());
        PointF startpt = pointForDestPoint(firstPt,25);
        loadPointerStartPoint(startpt,firstPt);

        if(nums[0] > 9)
        {
            if(nums[0] > 99)
                showHundreds("l",nums[0]);
            showTens("l",nums[0]);
        }
        showUnits("l",nums[0]);
        waitForSecs(0.2f);
        moveLabelsTogether(leftLabs);
        waitForSecs(0.4f);

        highlightLabels(leftLabs, true);
        playNumber(nums[0]);
        highlightLabels(leftLabs, false);

        if(nums[1] > 9)
        {
            if(nums[1] > 99)
                showHundreds("r",nums[1]);
            showTens("r",nums[1]);
        }
        showUnits("r",nums[1]);
        waitForSecs(0.2f);
        moveLabelsTogether(rightLabs);
        waitForSecs(0.4f);

        highlightLabels(rightLabs,true);
        playNumber(nums[1]);
        highlightLabels(rightLabs,false);
        waitForSecs(0.4f);

        List<OBLabel> desta =(nums[0] > nums[1]) ?leftLabs:rightLabs;
        PointF destpt = OB_Maths.locationForRect(new PointF(0.5f, 0.75f) ,desta.get(0).frame());
        destpt.x = ((desta.get(0).position() .x + desta.get(desta.size()-1).position() .x) / 2);
        movePointerToPoint(destpt,-1,true);
        waitForSecs(0.5f);
        playAudioQueuedScene("1","INCORRECT3",true);

        waitForSecs(1f);

        thePointer.hide();
        fin();
    }

    public OBLabel smallLabelForNumber(int n,OBControl bg,OBFont fnt)
    {
        String s = String.format("%d",n);
        OBLabel l = new OBLabel(s,fnt);
        l.setZPosition(bg.zPosition() +1);
        l.setPosition(bg.position());
        l.setColour(Color.WHITE);
        bg.parent.insertMember(l,0,s);
        return l;
    }

    public void setUpFinale()
    {
        lockScreen();
        if(leftLabel != null)
        {
            detachControl(leftLabel);
            detachControl(rightLabel);
            detachControls(leftLabs);
            detachControls(rightLabs);
        }
        deleteControls("(rect|line|count|cont) .*");
        loadEvent("finale");
        float txsize = Float.parseFloat(eventAttributes.get("textsize"));
        OBFont fnt = OBUtils.UnscaledReadingFontOfSize(txsize);
        OBGroup machine =(OBGroup) objectDict.get("machine");
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
            waitForSecs(0.8f);
            if (!_aborting)
                showScoreHammerScore((int)(score * 10.0/10),false);
        }
        catch(Exception e)
        {

        }
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
                moveToPosition("offpos",(List)Arrays.asList(leftLabel,rightLabel),0.3f,1);
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

    public OBControl findTarget(PointF pt)
    {
        OBControl c =finger(-1,2,targets,pt);
        return c;
    }

    public void touchDownAtPoint(PointF pt,View v)
    {
        if(status() == STATUS_AWAITING_CLICK)
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
