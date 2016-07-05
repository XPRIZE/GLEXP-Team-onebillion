package org.onebillion.xprz.mainui.x_lettersandsounds;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBPhoneme;
import org.onebillion.xprz.utils.OBUtils;
import org.onebillion.xprz.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alan on 05/07/16.
 */
public class X_Puzzle extends X_Wordcontroller
{
    static final int SHOW_TEXT_NONE = 0,
    SHOW_TEXT_INITIAL = 1,
    SHOW_TEXT_WORD =2;

    List<String> words;
    String currWord;
    List<OBPath> pieces,positions,puzzlePieces,swatches;
    List<RectF> homeRects;
    OBGroup puzzle;
    boolean gotoStage2;
    float textSize;
    OBControl textBox;
    OBLabel label;
    Map<String,String> wordDict;
    Map<String,OBPhoneme> componentDict;

    ReentrantLock finishLock;
    boolean animDone,preAssembled,firstTime;
    int showText;
    ReentrantLock audioLock;
    boolean firstTimeIn;

    public void miscSetUp()
    {
        firstTimeIn = true;
        //wordDict = LoadFlashcardXML(getLocalPath("flashcards.xml"));
        componentDict = OBUtils.LoadWordComponentsXML(true);

        String s = eventAttributes.get("textsize");
        if (s != null)
            textSize = Float.parseFloat(s);
        String ws = parameters.get("words");
        words = Arrays.asList(ws.split(","));
        currNo = 0;
        finishLock = new ReentrantLock();
        swatches = (List<OBPath>)(Object)OBUtils.randomlySortedArray(filterControls("swatch.*"));
        s = parameters.get("showtext");
        if (s != null)
        {
            if (s.equals("initial"))
                showText = SHOW_TEXT_INITIAL;
            else if (s.equals("word"))
                showText = SHOW_TEXT_WORD;
        }
        if (showText > 0)
            loadEvent("text");
        else
            loadEvent("notext");
        needDemo = false;
        if ((s = parameters.get("demo"))!= null)
            needDemo = s.equals("true");
        if ((s = parameters.get("preassembled"))!= null)
            preAssembled = s.equals("true");
        textBox = objectDict.get("textbox");
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        loadEvent("mastera");
        miscSetUp();
        //events = "c,d,e"componentsSeparatedByString.(",")mutableCopy.();
        events = new ArrayList<>();
        events.add("c");
        events.add(preAssembled?"d1":"d2");
        events.add("e");
        while (events.size() < words.size())
            events.add(events.get(events.size()-1));
        while (events.size() > words.size())
            events.remove(events.size()-1);
        if (needDemo)
        {
            String format = parameters.get("format");
            boolean p9 = format.equals("puzzle9");
            events.add(0,p9?"b2":"b1");
        }
        events.add(0,preAssembled?"a1":"a2");
        doVisual(currentEvent());
        firstTime = true;
    }

    public void maskImage(OBGroup g,int idx)
    {
        List<OBPath> pcs = (List<OBPath>)(Object)g.filterMembers("piece.*",true);
        for (int i = 0;i < pcs.size();i++)
            if (i != idx)
                pcs.get(i).hide();
        OBPath np = (OBPath)pcs.get(idx).copy();
        np.setFillColor(Color.WHITE);
        np.setStrokeColor(0);
        OBPath background = (OBPath) g.objectDict.get("background");
        //background.fillColor = currentSwatch().fillColor;
        background.parent.setMaskControl(np);
        PointF pt = convertPointFromControl(pcs.get(idx).position(),pcs.get(idx).parent);
        PointF offset = OB_Maths.DiffPoints(g.position(),pt);
        PointF destpos = positions.get(idx).position();
        PointF pos = OB_Maths.AddPoints(offset, destpos);
        g.setProperty("origpos",pos);
    }

}
