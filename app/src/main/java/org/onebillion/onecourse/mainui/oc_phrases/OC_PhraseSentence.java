package org.onebillion.onecourse.mainui.oc_phrases;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.oc_reading.OC_Reading;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimBlock;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBAudioManager;
import org.onebillion.onecourse.utils.OBReadingPara;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.onebillion.onecourse.utils.OBReadingWord.WORD_SPEAKABLE;

/**
 * Created by alan on 06/07/17.
 */

public class OC_PhraseSentence extends OC_Reading
{
    OBControl wordback,wordback2;
    List<OBReadingWord> words;
    RectF saveWordBackFrame;
    int lineColour,boxColour,borderColour;
    int wordIdx;
    String currComponentKey;
    Map<String,Map<String,Object>>componentDict;
    List<String>componentList;
    OBPresenter presenter;
    PointF textBoxOriginalPos;

    public Map loadComponent(String comp,String xmlPath)
    {
        fontSize = applyGraphicScale(76);
        Map<String,Map> dict = new HashMap<>();
        OBXMLNode xmlNode = null;
        try
        {
            if(xmlPath != null)
            {
                OBXMLManager xmlman = new OBXMLManager();
                List<OBXMLNode> xl = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                xmlNode = xl.get(0);
                String str;
                if(xmlNode.attributeStringValue("fontsize") != null)
                    fontSize = applyGraphicScale((float)xmlNode.attributeFloatValue("fontsize") );
                for(OBXMLNode phrasenode : xmlNode.childrenOfType(comp))
                {
                    Map md = new HashMap();
                    md.putAll(phrasenode.attributes);
                    String k = phrasenode.attributeStringValue("id");
                    String contents = phrasenode.contents;
                    if(contents != null)
                        md.put("contents",contents);
                    dict.put(k,md);
                }
            }
        }
        catch (Exception e)
        {
        }
        return dict;
    }

    public void miscSetUp()
    {

    }

    public void setUpWordStuff()
    {
        OBControl wb = objectDict.get("wordback");
        wordback = new OBControl();
        wordback.setFrame(wb.frame);
        wordback.setBackgroundColor(boxColour = wb.fillColor());
        wordback.setBorderColor(wb.strokeColor());
        wordback.setBorderWidth(wb.lineWidth());
        wordback.setCornerRadius(applyGraphicScale(3));

        wordback2 = wordback.copy();
        wordback2.setBackgroundColor(borderColour = Color.WHITE);
        wordback2.setBorderColor(0);
        wordback2.setBorderWidth(0);
        textBox.insertMember(wordback,0,"wordback");
        textBox.insertMember(wordback2,0,"wordback2");
        wb.hide();
        wordback.hide();
        wordback2.hide();
    }

    public void prepare()
    {
        theMoveSpeed = bounds().width();
        lineHeightMultiplier = 1.33f;
        textJustification = TEXT_JUSTIFY_CENTRE;
        initialised = true;
        processParams();
        eventsDict = loadXML(getConfigPath(sectionName() + ".xml"));
        loadAudioXML(getConfigPath(sectionAudioName() + "audio.xml"));
        loadFingers();
        highlightColour =  Color.argb(255,255,0,0);
        backgroundColour =  Color.argb(255,255,243,243);
        sharedLock = new ReentrantLock();
        sharedCondition = sharedLock.newCondition();
        initialised = true;
        miscSetUp();
        doVisual(currentEvent());
    }

    public void calcWordFrames()
    {
        for(OBReadingPara para : paragraphs)
            for(OBReadingWord w : para.words)
                if (w.frame != null)
                    w.frame = convertRectFromControl(w.label.frame,textBox);
    }

    public boolean showRAButton()
    {
        return true;
    }

    public boolean showNextButton()
    {
        return false;
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
                    if (!performSel("demo",currentEvent()))
                    {
                        doBody(currentEvent());
                    }
                }
                catch(Exception exception) {
                }
            }
        });
    }

    public void setUpDecorationForWord(OBReadingWord rw)
    {
        if (rw == null)
        {
            wordback.hide();
            return;
        }
        float px1 = applyGraphicScale(1);
        RectF f = new RectF(rw.label.frame());
        f.inset(-px1,-px1);
        wordback.setFrame(f);
        saveWordBackFrame = new RectF(wordback.frame());
        float amt = 1 * px1;
        f = new RectF(wordback.frame());
        f.inset(-amt,-amt);
        wordback2.setFrame(f);
        wordback.setZPosition(LABEL_ZPOS - 1);
        wordback2.setZPosition(LABEL_ZPOS - 2);
    }

    public void highlightWordWithBackground(OBReadingWord w,boolean withBackground,boolean jump)
    {
        lockScreen();
        w.label.setColour(highlightColour);
        w.label.setZPosition(LABEL_HI_ZPOS);
        if (withBackground)
        {
            wordback.show();
            wordback.setZPosition(LABEL_HI_ZPOS - 1);
            wordback2.show();
            wordback2.setZPosition(LABEL_HI_ZPOS - 2);
        }
        unlockScreen();
        if (jump)
        {
            OBAnimationGroup agp = new OBAnimationGroup();
            PointF pos = OB_Maths.OffsetPoint(w.label.position(), 0, -jumpOffset);
            OBAnim anim1 = OBAnim.moveAnim(pos,w.label);
            final float top = saveWordBackFrame.top;
            final float top2 = top - jumpOffset;
            float bot = saveWordBackFrame.bottom;
            final float amt = applyGraphicScale(1);
            OBAnim anim2 = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    RectF f = new RectF(saveWordBackFrame);
                    float y = OB_Maths.interpolateVal(top,top2,frac);
                    f.top = y;
                    wordback.setFrame(f);
                    f.inset(-amt,-amt);
                    wordback2.setFrame(f);
                }
            };
            agp.applyAnimations(Arrays.asList(anim1,anim2),0.1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
    }

    public void lowlightWordWithBackground(OBReadingWord w,boolean withBackground,boolean jump)
    {
        if (jump)
        {
            OBAnimationGroup agp = new OBAnimationGroup();
            //PointF pos = OB_Maths.OffsetPoint(w.label.position(), 0, jumpOffset);
            PointF pos = w.homePosition;
            OBAnim anim1 = OBAnim.moveAnim(pos,w.label);
            final float top = saveWordBackFrame.top;
            final float top2 = top - jumpOffset;
            final float amt = applyGraphicScale(1);
            OBAnim anim2 = new OBAnimBlock()
            {
                @Override
                public void runAnimBlock(float frac)
                {
                    RectF f = new RectF(saveWordBackFrame);
                    float y = OB_Maths.interpolateVal(top2,top,frac);
                    f.top = y;
                    wordback.setFrame(f);
                    f.inset(-amt,-amt);
                    wordback2.setFrame(f);
                }
            };
            agp.applyAnimations(Arrays.asList(anim1,anim2),0.1,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        }
        w.label.setColour(Color.BLACK);
        w.label.setZPosition(LABEL_ZPOS);
        if (withBackground)
        {
            wordback.setZPosition(LABEL_ZPOS - 1);
            wordback2.setZPosition(LABEL_ZPOS - 2);
        }
    }

    public long switchStatus(String scene)
    {
        return setStatus(STATUS_AWAITING_CLICK);
    }

    List<String> SplitToPrefixSuffix(String s)
    {
        Pattern p = Pattern.compile("[0123456789]");
        Matcher matcher = p.matcher(s);
        matcher.find();
        if (matcher.matches())
        {
            int idx = matcher.start();
            String s1 = s.substring(0,idx);
            String s2 = s.substring(idx);
            return Arrays.asList(s1,s2);
        }
         return Arrays.asList(s,"");
    }

    String SlowVersion(String s,boolean slow)
    {
        if(!slow)
            return s;
        List<String> arr = SplitToPrefixSuffix(s);
        return String.format("%ss%s",arr.get(0),"s",arr.get(1));
    }

    public void speakWordAsPartial(OBReadingWord w,String key,boolean wait) throws Exception
    {
        String fn = String.format("%s_%d",key,w.paraNo);
        double ts = w.timeStart,te = w.timeEnd;
        if(slowWordsAvailable)
        {
            ts = w.slowTimeStart;
            te = w.slowTimeEnd;
        }
        playAudioFromTo(SlowVersion(fn,slowWordsAvailable),ts,te);
        if (wait)
            waitAudio();
    }

    public void speakWordAsPartial(OBReadingWord w,String key) throws Exception
    {
        speakWordAsPartial(w,key,true);
    }

    public void replayAudio()
    {
        if(busyStatuses.contains(status()))
            return;
        if(_replayAudio != null)
        {
            setStatus(status());
            OBUtils.runOnOtherThread(new OBUtils.RunLambda()
            {
                public void run() throws Exception
                {
                    _replayAudio();
                }
            });
        }
    }

    public List wordExtents()
    {
        List<List>arr = new ArrayList<>();
        if (words == null)
            return arr;
        List linearr = new ArrayList<>();
        float lastx = -1;
        for(OBReadingWord w : words)
        {
            RectF f = w.frame;
            float x = f.left;
            if(x < lastx)
            {
                List two = Arrays.asList(linearr.get(0),linearr.get(linearr.size()-1));
                arr.add(two);
                linearr = new ArrayList<>();
            }
            linearr.add(w);
            lastx = f.right;
        }
        if(linearr.size()  > 0)
        {
            List two = Arrays.asList(linearr.get(0),linearr.get(linearr.size()-1));
            arr.add(two);
        }
        return arr;
    }

    public void flashBox(long sttime)
    {
        try
        {
            for (int i = 0;i < 2;i++)
            {
                lockScreen();
                wordback.hide();
                unlockScreen();
                waitAndCheck(sttime,0.1,4);
                lockScreen();
                wordback.show();
                unlockScreen();
                waitAndCheck(sttime,0.1,4);
            }
        }
        catch (Exception exception)
        {
            lockScreen();
            wordback.show();
            unlockScreen();
        }
    }
    public void remindBox(long sttime,float secs)
    {
        if (statusChanged(sttime))
            return;
        flashBox(sttime);
        endBody();
    }

    public void showPic() throws Exception
    {
        if(mainPic.hidden() )
        {
            waitForSecs(0.3f);
            playSfxAudio("picon",false);
            lockScreen();
            mainPic.show();
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
        }
    }

    public boolean showWords() throws Exception
    {
        if(textBox.hidden() )
        {
            waitForSecs(0.3f);
            playSfxAudio("wordson",false);
            lockScreen();
            textBox.show();
            unlockScreen();
            waitForSecs(0.2f);
            waitSFX();
            return true;
        }
        return false;
    }

    public void readParagraph(int pidx,long token,boolean canInterrupt) throws Exception
    {
        String fn = String.format("%s_%d",currComponentKey,pidx+1);
        readParagraph(pidx,fn,token,canInterrupt);
    }

    public void highlightAndSpeakWord(OBReadingWord w,boolean syllablesIfPoss)
    {
        try
        {
            long token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                boolean withBackground = jumpOffset > 0;
                highlightWordWithBackground(w,true,withBackground);
                if(syllablesIfPoss && syllableAudioExistsForWord(w,currComponentKey) )
                {
                    List arr = SplitToPrefixSuffix(currComponentKey);
                    String fileName = String.format("%@syl%@_%",arr.get(0),arr.get(1),CrunchedString(w.text));
                    speakSyllablesForWord(w,fileName);
                    highlightWord(w,true,false);
                    waitForSecs(0.2f);
                }
                //else
                speakWordAsPartial(w,currComponentKey);
                lowlightWordWithBackground(w,true,withBackground);
            }
        }
        catch(Exception exception)
        {
        }
        sequenceLock.unlock();
    }

    public void readParagraph(int pidx,long timestamp) throws Exception
    {
        String fn = String.format("%s_%d",currComponentKey,pidx+1);
        readParagraph(pidx,fn,timestamp);
    }

    public void readParagraph(int pidx,String fileName,long timestamp) throws Exception
    {
        OBReadingPara para = paragraphs.get(pidx);
        playAudio(fileName);
        long startTime = SystemClock.uptimeMillis();
        for(OBReadingWord w : para.words)
        {
            if (w.label != null &&(w.flags & WORD_SPEAKABLE) != 0)
            {
                double currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
                double waitTime = startTime - currTime;
                if(waitTime > 0.0)
                    waitForSecs(waitTime);
                if(timestamp != statusTime)
                    break;
                highlightWord(w,true,false);
                currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
                waitTime = w.timeEnd - currTime;
                if(waitTime > 0.0 && timestamp == statusTime)
                    waitForSecs(waitTime);
                highlightWord(w,false,false);
                if(timestamp != statusTime )
                    break;
            }
        }
    }

    public boolean syllableAudioExistsForWord(OBReadingWord rw,String key)
    {
        String localPath = OBAudioManager.audioManager.getAudioPath(String.format("%s_%d",key,rw.paraNo));
        if(localPath != null)
        {
            String dirPath = OBUtils.stringByDeletingLastPathComponent(localPath);
            String txt = CrunchedString(rw.text);
            List arr = SplitToPrefixSuffix(key);
            String syllPath = OBUtils.stringByAppendingPathComponent(dirPath,String.format("%ssyl%s_%s.etpa",arr.get(0),arr.get(1),txt));
            if(syllPath != null && OBUtils.fileExistsAtPath(syllPath))
                return true;
        }
        return false;

    }
    public boolean readPage()
    {
        long token = -1;
        try
        {
            reading = true;
            token = takeSequenceLockInterrupt(true);
            if(token == sequenceToken)
            {
                currPara = 0;
                while(currPara < paragraphs.size() )
                {
                    readParagraph(currPara,token,true);
                    currPara++;
                    waitForSecs(0.6f);
                }
            }
        }
        catch(Exception exception)
        {
        }
        sequenceLock.unlock();
        reading = false;
        return(sequenceToken == token);
    }

    public boolean readPageStatusTime(long timestamp)
    {
        try
        {
            reading = true;
            if(timestamp == statusTime )
            {
                while(currPara < paragraphs.size() )
                {
                    readParagraph(currPara,timestamp);
                    currPara++;
                    waitForSecs(0.6f);
                }
            }
        }
        catch(Exception exception)
        {
        }
        reading = false;
        return(timestamp == statusTime);
    }

}
