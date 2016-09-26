package org.onebillion.xprz.mainui.x_reading;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.onebillion.xprz.controls.OBControl;
import org.onebillion.xprz.controls.OBGroup;
import org.onebillion.xprz.controls.OBImage;
import org.onebillion.xprz.controls.OBLabel;
import org.onebillion.xprz.controls.OBPath;
import org.onebillion.xprz.mainui.MainActivity;
import org.onebillion.xprz.mainui.OBMainViewController;
import org.onebillion.xprz.mainui.XPRZ_SectionController;
import org.onebillion.xprz.utils.OBReadingPara;
import org.onebillion.xprz.utils.OBReadingWord;
import org.onebillion.xprz.utils.OBXMLManager;
import org.onebillion.xprz.utils.OBXMLNode;
import org.onebillion.xprz.utils.OBUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alan on 02/06/16.
 */
public class X_Reading extends XPRZ_SectionController
{
    public static int OB_RD_DEMO_NONE=0,
            OB_RD_DEMO_PARTIAL=1,
            OB_RD_DEMO_FULL=2,
            TEXT_JUSTIFY_LEFT=0,
            TEXT_JUSTIFY_CENTRE=1,
            TEXT_JUSTIFY_RIGHT=2,
            PIC_JUSTIFY_TOP=0,
            PIC_JUSTIFY_LEFT=1,
            PIC_JUSTIFY_RIGHT=2,
            STATUS_FINISHING=1002,
            LABEL_ZPOS=10,
            LABEL_HI_ZPOS=15;

    public Lock sharedLock;
    public Condition sharedCondition;
    int currPara,level;
    float jumpOffset,lineHeightMultiplier,paraMultiplier,letterSpacing,fontSize,spaceExtra,playRate;
    boolean reading,questionsAvailable,slowWordsAvailable,paragraphlessMode;
    int picJustify,textJustification;
    String indentString;
    int demoType,introNo,numberOfTextLines;
    int pageNo,maxPageNo,readingMode;
    List<OBReadingPara> paragraphs;
    OBGroup textBox;
    OBImage mainPic;
    OBReadingWord highlightedWord;
    int highlightColour,backgroundColour;

    static String CrunchedString(String s)
    {
        String t = s;
        String[] l = t.toLowerCase().split("[^[a-z][A-Z]]*");
        t = TextUtils.join("",l);
        return t;
    }

    static boolean IsLeftHanger(String ch)
    {
        for (String ap : Arrays.asList("“","‘"))
            if (ap.equals(ch))
                return true;
        return false;
    }

    public void loadTimingsPara(OBReadingPara para,String xmlPath)
    {
        try
        {
            if (xmlPath != null)
            {
                OBXMLNode xmlNode = null;
                OBXMLManager xmlManager = new OBXMLManager();
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
//                List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(xmlPath));
                String filename = OBUtils.lastPathComponent(xmlPath);
                boolean isSlow = (filename.startsWith("ps"));
                if (isSlow)
                    slowWordsAvailable = true;
                xmlNode = xl.get(0);
                List<OBXMLNode> arr = xmlNode.childrenOfType("timings");
                OBXMLNode elem = arr.get(0);
                arr = elem.childrenOfType("timing");
                int idx = 0;
                for (OBReadingWord w : para.words)
                {
                    if ((w.flags & OBReadingWord.WORD_SPEAKABLE) != 0 && (idx < arr.size()))
                    {
                        OBXMLNode n = arr.get(idx);
                        if (isSlow)
                        {
                            w.slowTimeStart = n.attributeFloatValue("start");
                            w.slowTimeEnd = n.attributeFloatValue("end");
                        }
                        else
                        {
                            w.timeStart = n.attributeFloatValue("start");
                            w.timeEnd = n.attributeFloatValue("end");
                        }
                        idx++;
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
    }


    public List<List<Double>> loadSyllableTimingsForWord(OBReadingWord word,String xmlPath)
    {
        List<List<Double>> timings = new ArrayList<>();
        try
        {
            if (xmlPath != null)
            {
                OBXMLNode xmlNode = null;
                OBXMLManager xmlManager = new OBXMLManager();
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
//                List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(xmlPath));
                xmlNode = xl.get(0);
                List<OBXMLNode> arr = xmlNode.childrenOfType("timings");
                OBXMLNode elem = arr.get(0);
                for (OBXMLNode xtiming : elem.childrenOfType("timing"))
                {
                    double start = xtiming.attributeFloatValue("start");
                    double end = xtiming.attributeFloatValue("end");
                    timings.add(Arrays.asList(start,end));
                }
            }
        }
        catch (Exception e)
        {
        }
        return timings;
    }

    public List<OBReadingPara>loadPageXML(String xmlPath)
    {
        indentString = "NN";
        lineHeightMultiplier = 1;
        paraMultiplier = 1;
        fontSize = applyGraphicScale(40);
        playRate = 1.0f;
        letterSpacing = 0;
        OBXMLNode xmlNode = null;
        if (xmlPath != null)
        {
            try
            {
                OBXMLManager xmlManager = new OBXMLManager();
                List<OBXMLNode> xl = xmlManager.parseFile(OBUtils.getInputStreamForPath(xmlPath));
//                List<OBXMLNode> xl = xmlManager.parseFile(MainActivity.mainActivity.getAssets().open(xmlPath));
                xmlNode = xl.get(0);
                String str;
                if ((str = xmlNode.attributeStringValue("fontsize"))!=null)
                    fontSize = applyGraphicScale(Float.parseFloat(str));
                if ((str = xmlNode.attributeStringValue("wordplayrate"))!=null)
                    playRate = applyGraphicScale(Float.parseFloat(str));
                if ((str = xmlNode.attributeStringValue("textjustify"))!=null && str.equals("centre"))
                    textJustification = TEXT_JUSTIFY_CENTRE;
                else
                    textJustification = TEXT_JUSTIFY_LEFT;
                paragraphlessMode = ((str = xmlNode.attributeStringValue("noparas"))!= null && str.equals("true"));
                String indentstr = xmlNode.attributeStringValue("indent");
                if (indentstr != null)
                    indentString = indentstr;
                String lhstr = xmlNode.attributeStringValue("lineheight");
                if (lhstr != null)
                    lineHeightMultiplier = Float.parseFloat(lhstr);
                String phstr = xmlNode.attributeStringValue("paraheight");
                if (phstr != null)
                    paraMultiplier = Float.parseFloat(phstr);
                String lsstr = xmlNode.attributeStringValue("letterspacing");
                if (lsstr != null)
                    letterSpacing = applyGraphicScale(Float.parseFloat(lsstr));
                String lstr = xmlNode.attributeStringValue("level");
                if (lstr != null)
                    level = Integer.parseInt(lstr);
                List<OBXMLNode> pageNodes = xmlNode.childrenOfType("page");
                List<OBXMLNode> questionNodes = xmlNode.childrenOfType("question");
                questionsAvailable = questionNodes.size() > 0;
                if (!pageNodes.isEmpty())
                {
                    OBXMLNode lastpagenode = pageNodes.get(pageNodes.size()-1);
                    maxPageNo = Integer.parseInt(lastpagenode.attributeStringValue("pageno"));
                }
                for (OBXMLNode pageNode : pageNodes)
                {
                    String pno = pageNode.attributeStringValue("pageno");
                    if (Integer.parseInt(pno) == pageNo)
                    {
                        String picj = pageNode.attributeStringValue("picjustify");
                        if (picj != null)
                        {
                            if (picj.equals("left"))
                                picJustify = PIC_JUSTIFY_LEFT;
                            else if (picj.equals("right"))
                                picJustify = PIC_JUSTIFY_RIGHT;
                        }
                        String pindentstr = pageNode.attributeStringValue("indent");
                        if (pindentstr != null)
                            indentString = pindentstr;
                        String plhstr = pageNode.attributeStringValue("lineheight");
                        if (plhstr != null)
                            lineHeightMultiplier = Float.parseFloat(plhstr);
                        List<OBReadingPara> paras = new ArrayList<>();
                        int i = 1;
                        for (OBXMLNode paraNode : pageNode.childrenOfType("para"))
                        {
                            paras.add(new OBReadingPara(paraNode.contents,i));
                            i++;
                        }
                        return paras;
                    }
                }
            }
            catch (Exception e)
            {
            }

        }
        return Collections.emptyList();
    }

    public void loadTemplate()
    {
        eventsDict = loadXML(getConfigPath("booktemplate.xml"));
        if (pageNo == 0)
        {
            loadEvent("title");
            String s;
            if ((s = eventAttributes.get("largefontsize"))!=null)
                fontSize = applyGraphicScale(Float.parseFloat(s));
            if ((s = eventAttributes.get("largelineheight"))!=null)
                lineHeightMultiplier = Float.parseFloat(s);
            if ((s = eventAttributes.get("largespacing"))!=null)
                letterSpacing = Float.parseFloat(s);
            textJustification = TEXT_JUSTIFY_CENTRE;
        }
        else
        {
            if (picJustify == PIC_JUSTIFY_RIGHT)
                loadEvent("picright");
            else if (picJustify == PIC_JUSTIFY_LEFT)
                loadEvent("picleft");
            else
                loadEvent("normal");
        }
        loadEvent("lines");
    }

    public boolean showBackButton()
    {
        return true;
    }

    public boolean showRAButton()
    {
        return false;
    }

    public boolean showNextButton()
    {
        return true;
    }

    public boolean showPrevButton()
    {
        return pageNo > 1;
    }

    public int buttonFlags()
    {
        int tlflag = showBackButton()? OBMainViewController.SHOW_TOP_LEFT_BUTTON:0;
        int blflag = showPrevButton()?OBMainViewController.SHOW_BOTTOM_LEFT_BUTTON:0;
        int brflag = showNextButton()?OBMainViewController.SHOW_BOTTOM_RIGHT_BUTTON:0;
        int trflag = showRAButton()?OBMainViewController.SHOW_TOP_RIGHT_BUTTON:0;
        return tlflag|trflag|blflag|brflag;
    }

    public void showNextArrow(boolean show)
    {
        if (_aborting)
            return;
        lockScreen();
        if (show)
        {
            if (pageNo == maxPageNo)
                MainActivity.mainViewController.setBottomRightButton("star");
            else
                MainActivity.mainViewController.setBottomRightButton("std");
            MainViewController().bottomRightButton.setOpacity(1.0f);
            MainViewController().bottomRightButton.setHidden(false);
        }
        else
        {
            MainViewController().bottomRightButton.setOpacity(0.0f);
            MainViewController().bottomRightButton.setHidden(true);
        }
        MainActivity.mainViewController.bottomRightButton.setNeedsRetexture();
        invalidateView(0,0,bounds().width(),bounds().height());
        unlockScreen();
    }

    public void showNextArrowAndRA(boolean show)
    {
        if (_aborting)
            return;
        lockScreen();
        showNextArrow(show);
        if (show)
        {
            MainViewController().topRightButton.setOpacity(1.0f);
            MainViewController().topRightButton.setHidden(false);
        }
        else
        {
            MainViewController().topRightButton.setOpacity(0.0f);
            MainViewController().topRightButton.setHidden(true);
        }
        invalidateView(0,0,bounds().width(),bounds().height());
        unlockScreen();
    }

    public void prepare()
    {
        super.prepare();
        loadFingers();
        highlightColour = Color.RED;
        backgroundColour = Color.argb(255,0,243,243);
        sharedLock = new ReentrantLock();
        sharedCondition = sharedLock.newCondition();
        pageNo = 0;
        if (parameters.get("page") != null)
            pageNo = Integer.parseInt(parameters.get("page"));
        if (parameters.get("intro") != null)
            introNo = Integer.parseInt(parameters.get("intro"));
        demoType = OB_RD_DEMO_NONE;
        String s = parameters.get("demo");
        if (s!=null)
            if (s.equals("full"))
                demoType = OB_RD_DEMO_FULL;
            else if (s.equals("partial"))
                demoType = OB_RD_DEMO_PARTIAL;
        initialised = true;
        paragraphs = loadPageXML(getLocalPath("book.xml"));
        loadTemplate();
        int i = 1;
        for (OBReadingPara para : paragraphs)
        {
            loadTimingsPara(para,getLocalPath(String.format("p%d_%d.etpa",pageNo,i)));
            loadTimingsPara(para,getLocalPath(String.format("ps%d_%d.etpa",pageNo,i)));
            i++;
        }
        //setButtons();
        setUpScene();
    }

    public void scalePicToBox()
    {
        OBControl picbox = objectDict.get("imagebox");
        float wratio = mainPic.width() / picbox.width();
        float hratio = mainPic.height() / picbox.height();
        float ratio = wratio > hratio?wratio:hratio;
        mainPic.setScale(1 / ratio);
        mainPic.setPosition(picbox.position());
    }

    public void adjustTextPosition()
    {
        if (numberOfTextLines < 3)
        {
            PointF pos = textBox.position();
            pos.y += (fontSize / numberOfTextLines);
            textBox.setPosition(pos);
        }
    }

    public boolean adjustTitleTextPosition()
    {
        return true;
    }

    public void flashContinuouslyAfter(final float secs)
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception
            {
                while (!_aborting && status() != STATUS_FINISHING)
                {
                    try
                    {
                        flashNextButton();
                        waitForSecs(secs + 1.5);
                    }
                    catch (Exception exception)
                    {
                    }

                }
            }
        });

    }

    public void flashNextButton()
    {
        OBUtils.runOnOtherThread(new OBUtils.RunLambda()
        {
            @Override
            public void run() throws Exception
            {
                if (_aborting)
                    return;
                try
                {
                    waitForSecs(0.3f);
                    lockScreen();
                    MainViewController().bottomRightButton.setOpacity(1.0f);
                    MainViewController().bottomRightButton.setHidden(false);
                    invalidateControl(MainViewController().bottomRightButton);
                    unlockScreen();
                    waitForSecs(0.5f);
                    for (int i = 0;i < 2;i++)
                    {
                        takeSequenceLockInterrupt(false);
                        sequenceLock.unlock();
                        lockScreen();
                        MainViewController().bottomRightButton.setOpacity(0.2f);
                        invalidateControl(MainViewController().bottomRightButton);
                        unlockScreen();
                        waitForSecs(0.3f);
                        lockScreen();
                        MainViewController().bottomRightButton.setOpacity(1.0f);
                        invalidateControl(MainViewController().bottomRightButton);
                        unlockScreen();
                        waitForSecs(0.3f);
                    }
                }
                catch (Exception exception)
                {
                    showNextArrow(!_aborting);
                }
            }
        });
    }

    public void bringUpNextButton()
    {
        showNextArrowAndRA(true);
        OBUtils.runOnOtherThreadDelayed(5, new OBUtils.RunLambda() {
            @Override
            public void run() throws Exception {
                if (status() != STATUS_FINISHING && !_aborting)
                    flashContinuouslyAfter(4);

            }
        });
    }
    public void setUpScene()
    {
        OBControl tb = objectDict.get("textbox");
        detachControl(tb);
        textBox = new OBGroup(Collections.singletonList(tb));
        tb.hide();
        attachControl(textBox);
        mainPic = loadImageWithName(String.format("p%d",pageNo),new PointF(0.5f, 0.5f),new RectF(bounds()));
        objectDict.put("mainpic",mainPic);
        mainPic.setZPosition(60);
        scalePicToBox();

        OBPath picbox = (OBPath) objectDict.get("imagebox");
        picbox.setFillColor(0);
        textBox.setZPosition(70);

        if (pageNo == 0)
        {
            layOutText();
            if (!adjustTitleTextPosition())
            {
                fontSize = applyGraphicScale(Float.parseFloat(eventAttributes.get("smallfontsize")));
                lineHeightMultiplier = Float.parseFloat(eventAttributes.get("smalllineheight"));
                letterSpacing = Float.parseFloat(eventAttributes.get("smallspacing"));
                for (OBControl c : new ArrayList<>(textBox.members))
                {
                    detachControl(c);
                    textBox.removeMember(c);
                }
                layOutText();
                adjustTextPosition();
            }
        }
        else
        {
            layOutText();
            adjustTextPosition();
        }
        for (OBReadingPara para : paragraphs)
            for (OBReadingWord w : para.words)
                if (w.frame != null)
                    w.frame = convertRectFromControl(w.frame,textBox);
    }


    public void layOutLine(List<OBReadingWord>wordarr, float leftEdge, float rightEdge, float y, int justification, Typeface typeFace,float typeSize, String paraText)
    {
        List<OBReadingWord>words = new ArrayList<OBReadingWord>(wordarr);
        while (!words.isEmpty() && words.get(words.size()-1).text.startsWith(" "))
            words.remove(words.size()-1);
        if (words.isEmpty())
            return;
        numberOfTextLines++;
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(typeSize);
        textPaint.setTypeface(typeFace);
        if (letterSpacing > 0)
            textPaint.setLetterSpacing(letterSpacing / typeSize);
        float lineStart;
        OBReadingWord w0 = words.get(0),w1 = words.get(words.size()-1);
        String thisText = paraText.substring(w0.index,w1.index + w1.text.length());
        float textWidth = textPaint.measureText(thisText);
        if (justification == TEXT_JUSTIFY_CENTRE)
        {
            lineStart = (rightEdge - textWidth)/2;
        }
        else if (justification == TEXT_JUSTIFY_RIGHT)
        {
            lineStart = rightEdge - textWidth;
        }
        else
        {
            lineStart = leftEdge;
            if (w0.index == 0)
                lineStart += textPaint.measureText(indentString);
        }
        for (OBReadingWord w : words)
        {
            OBLabel lab;
            lab = new OBLabel(w.text,typeFace,typeSize);
            lab.setColour(Color.BLACK);
            lab.setLetterSpacing(letterSpacing);
            float widthUpToWord = textPaint.measureText(paraText.substring(w0.index,w.index));
            RectF f = new RectF(lab.frame());
            f.offset(-f.left,-f.top);
            f.offset(lineStart + widthUpToWord,y);
            lab.setFrame(f);
            if (!w.text.endsWith(" "))
            {
                float wdth = textPaint.measureText(paraText.substring(w0.index,w.index + w.text.length()));
                lab.setRight(wdth + lineStart);
            }
            //attachControl(lab);
            textBox.insertMember(lab,0,"");
            //detachControl(lab);
            w.frame = new RectF(lab.frame());
            w.label = lab;
            w.homePosition = new PointF(lab.position().x,lab.position().y);
            if ((w.flags & OBReadingWord.WORD_SPEAKABLE) != 0)
                lab.setZPosition(LABEL_ZPOS);
            else
                lab.setZPosition(LABEL_ZPOS - 3);
        }
    }

    public float layOutText()
    {
        Typeface tf = OBUtils.standardTypeFace();
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(fontSize);
        textPaint.setTypeface(tf);
        if (letterSpacing > 0)
            textPaint.setLetterSpacing(letterSpacing / fontSize);
        float szw = textPaint.measureText("NN");
        spaceExtra = 0;
        jumpOffset = fontSize * 0.6f;
        float lineHeight = fontSize;
        float topY = 0;//textBox.top();
        float indent = szw;
        if (textJustification == TEXT_JUSTIFY_CENTRE)
            indent = 0;
        float rightEdge = textBox.width();
        List<OBReadingPara> paras = paragraphs;
        if (paragraphlessMode)
        {
            OBReadingPara para = new OBReadingPara("",0);
            para.words = new ArrayList<>();
            StringBuilder sofar = new StringBuilder();
            for (OBReadingPara p : paras)
            {
                if (sofar.length() > 0)
                {
                    sofar.append(" ");
                    OBReadingWord rw = OBReadingWord.wordFromString(" ",0,1,OBReadingWord.WORD_CAN_BREAK,0);
                    rw.index = sofar.length();
                    para.words.add(rw);
                }
                int offset = sofar.length();
                sofar.append(p.text);
                for (OBReadingWord wd : p.words)
                {
                    para.words.add(wd);
                    wd.index += offset;
                }
            }
            para.text = sofar.toString();
            paras = Collections.singletonList(para);
        }
        for (OBReadingPara para : paras)
        {
            List<OBReadingWord> currLine = new ArrayList<>();
            PointF pos = new PointF(indent, topY);
            int lineStartIndex = 0;
            float lineStart = indent;
            for (OBReadingWord w : para.words)
            {
                float sizew = textPaint.measureText(para.text.substring(lineStartIndex,w.index + w.text.length()));
                if ((w.flags & OBReadingWord.WORD_SPEAKABLE)!=0 && sizew + lineStart > rightEdge)
                {
                    OBReadingWord lastw = currLine.get(currLine.size()-1);
                    if (IsLeftHanger(lastw.text))
                        currLine.remove(currLine.size()-1);
                    else
                        lastw = null;
                    layOutLine(currLine,0,rightEdge,topY,textJustification,tf,fontSize,para.text);
                    currLine.clear();
                    if (lastw != null)
                        currLine.add(lastw);
                    topY += (lineHeight * lineHeightMultiplier);
                    pos.y = topY;
                    lineStart = 0;
                    lineStartIndex = (int)w.index;
                }
                currLine.add(w);
            }
            if (currLine.size() > 0)
                layOutLine(currLine,0,rightEdge,topY,textJustification,tf,fontSize,para.text);
            topY += (lineHeight * lineHeightMultiplier * paraMultiplier);
        }
        return topY;
    }

    public void highlightWord(OBReadingWord w,boolean high,boolean withBackground)
    {
        lockScreen();
        if (high)
        {
            w.label.setColour(highlightColour);
            if (withBackground)
                w.label.backgroundColor = backgroundColour;
        }
        else
        {
            w.label.setColour(Color.BLACK);
            if (withBackground)
                w.label.backgroundColor = 0;
        }
        unlockScreen();
    }

    public void highlightWord(OBReadingWord w,int stidx,int enidx,boolean h,boolean withBackground)
    {
        lockScreen();
        w.label.setColour(Color.BLACK);
        if (h)
        {
            w.label.setHighRange(stidx,enidx,Color.RED);
        }
        else
        {
            w.label.setHighRange(-1,-1,0);
        }
        unlockScreen();
    }

    public void speakWordAsPartial(OBReadingWord w) throws Exception
    {
        String pref = "p";
        if (slowWordsAvailable)
            pref = "ps";
        playAudioFromTo(String.format("%s%d_%d",pref,pageNo,w.paraNo),w.slowTimeStart,w.slowTimeEnd);
        waitAudio();
    }

    public void readParagraph(int pidx,double token,boolean canInterrupt) throws Exception
    {

    }

    public boolean readPage()
    {
        return _readPage();
    }
    public boolean _readPage()
    {
        long token = -1;
        try
        {
            reading = true;
            Log.i("readpage",String.format("%d",((ReentrantLock)sequenceLock).getQueueLength()));
            token = takeSequenceLockInterrupt(true);
            if (token == sequenceToken)
            {
                while (currPara < paragraphs.size())
                {
                    checkSequenceToken(token);
                    readParagraph(currPara,token,true);
                    currPara++;
                    for (int i = 0;i < 6;i++)
                    {
                        checkSequenceToken(token);
                        waitForSecs(0.1);
                    }
                }
            }
            setStatus(STATUS_AWAITING_CLICK);
        }
        catch (Exception exception)
        {
        }
        sequenceLock.unlock();
        reading = false;
        return (sequenceToken == token);
    }

    public void replayAudio()
    {
        if (status() != STATUS_DOING_DEMO)
        {
            new AsyncTask<Void, Void, Void>()
            {
                @Override
                protected Void doInBackground(Void... params)
                {
                    currPara = 0;
                    _readPage();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
        }
    }

    public void finishBook()
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                fin();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public void doNextStuff(final Class cls)
    {
        try
        {
            takeSequenceLockInterrupt(true);
        }
        catch (Exception e)
        {

        }
        sequenceLock.unlock();
        if (pageNo == maxPageNo)
        {
            finishBook();
        }
        else
        {
            int p = pageNo + 1;
            StringBuilder parmString = new StringBuilder();
            parmString.append(parameters.get("0"));
            parmString.append(String.format("/page=%d",p));
            if (parameters.get("cq") != null)
                parmString.append(String.format("/cq=%s",parameters.get("cq")));
            _aborting = true;
            playAudio(null);
            final String fParmString = parmString.toString();
            OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    MainViewController().pushViewController(cls,true,true,fParmString,true);
                }
            });
        }
    }
    public void nextPage()
    {
        if (!_aborting && !MainViewController().navigating && status()!= STATUS_FINISHING)
        {
            final Class c = this.getClass();
            setStatus(STATUS_FINISHING);
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    doNextStuff(c);
                }
            });
        }

    }

    public void doPrevStuff(final Class cls)
    {
        try
        {
            takeSequenceLockInterrupt(true);
        }
        catch (Exception e)
        {

        }
        sequenceLock.unlock();
        int p = pageNo - 1;
        StringBuilder parmString = new StringBuilder();
        parmString.append(parameters.get("0"));
        parmString.append(String.format("/page=%d",p));
        if (parameters.get("cq") != null)
            parmString.append(String.format("/cq=%s",parameters.get("cq")));
        _aborting = true;
        playAudio(null);
        final String fParmString = parmString.toString();
        OBUtils.runOnMainThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    MainViewController().pushViewController(cls,true,false,fParmString,true);
                }
            });
    }

    public void prevPage()
    {
        if (!_aborting && !MainViewController().navigating && status()!= STATUS_FINISHING)
        {
            setStatus(STATUS_FINISHING);
            final Class c = this.getClass();
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                @Override
                public void run() throws Exception
                {
                    doPrevStuff(c);
                }
            });
        }

    }


}
