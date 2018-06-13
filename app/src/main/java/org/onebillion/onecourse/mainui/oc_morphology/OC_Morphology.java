package org.onebillion.onecourse.mainui.oc_morphology;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.SystemClock;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPresenter;
import org.onebillion.onecourse.mainui.MainActivity;
import org.onebillion.onecourse.mainui.oc_reading.OC_Reading;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBReadingWord;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OBXMLManager;
import org.onebillion.onecourse.utils.OBXMLNode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.onebillion.onecourse.utils.OBUtils.StandardReadingFontOfSize;

public class OC_Morphology extends OC_Reading
{
    Map sentenceDict;
    OBFont font;
    float textSize;
    List sentenceIds;
    List sentenceGroups;
    String group,mtype;
    OBPresenter presenter;

    public class ocm_word extends Object
    {
        int st,en;
        float startTime,endTime;
    }
    public class ocm_sentence extends Object
    {
        String sid,text;
        Map markups;
        List<ocm_word>words;
        public ocm_sentence(String sid,String tx)
        {
            super();
            this.sid = sid;
            this.text = tx;
        }
     }
    public static boolean WordCharacter(char ch)
    {
        return OBReadingWord.isWordCharacter(ch);
    }

    static int CharNoForWordi(String s,int wno)
    {
        boolean boundary = true;
        int chno = 0;
        int wordno = -1;
        while(chno < s.length())
        {
            if(WordCharacter(s.charAt(chno)))
            {
                if(boundary)
                {
                    wordno++;
                    if(wordno == wno)
                        return chno;
                    boundary = false;
                }
            }
            else
                boundary = true;
            chno++;
        }
        return -1;
    }

    Map  LoadMorphologySentencesXML(String xmlPath)
    {
        Map<String,ocm_sentence> dict = new HashMap();
        if(xmlPath != null)
        {
            try
            {
                OBXMLManager xmlman = new OBXMLManager();
                List<OBXMLNode> xml = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                OBXMLNode root = xml.get(0);

                for(OBXMLNode pNode : root.childrenOfType("phrase"))
                {
                    String sid = pNode.attributeStringValue("Object");
                    ocm_sentence se = new ocm_sentence(sid,pNode.contents);
                    dict.put(sid,se);
                }
                for(OBXMLNode mNode : root.childrenOfType("markup"))
                {
                    String sid = mNode.attributeStringValue("sid");
                    String mtype = mNode.attributeStringValue("mtype");
                    String group = mNode.attributeStringValue("group");
                    ocm_sentence se = dict.get(sid);
                    if(se == null)
                        MainActivity.log("LoadMorphologySentencesXML - Missing sentence %",sid);
                    else
                    {
                        String text = se.text;
                        String wr = mNode.attributeStringValue("mrange");
                        List<String> comps = Arrays.asList(wr.split(","));
                        List<List> rs = new ArrayList<>();
                        for(String comp : comps)
                        {
                            int st,en;
                            List<String> divs = Arrays.asList(comp.split(":"));
                            if(divs.size() > 1)
                            {
                                int wno = Integer.parseInt(divs.get(0));
                                int wordstart = CharNoForWordi(text, wno);
                                int wordend = wordstart;
                                while(wordend < text.length() && WordCharacter(text.charAt(wordend)))
                                    wordend++;
                                List<String> vals = Arrays.asList(divs.get(1).split("-"));
                                int v0 = Integer.parseInt(vals.get(0));
                                int v1 = Integer.parseInt(vals.get(1));
                                st = wordstart + v0;
                                en = wordstart + v1;
                            }
                            else
                            {
                                List<String> vals = Arrays.asList(divs.get(0).split("-"));
                                int v0 = Integer.parseInt(vals.get(0));
                                int v1 = Integer.parseInt(vals.get(1));
                                st = v0;
                                en = v1;
                            }
                            rs.add(Arrays.asList(st,en));
                        }
                        String k = String.format("%s+%",mtype,group);
                        se.markups.put(k,rs);
                    }
                }            }
            catch(Exception e)
            {

            }
        }
        return dict;
    }

    List<ocm_word>WordsFromSentence(String sentence)
    {
        List<ocm_word>words = new ArrayList<>();
        int st = 0;
        while(st < sentence.length())
        {
            while(st < sentence.length() && !WordCharacter(sentence.charAt(st)))
                st++;
            int en = st;
            while(en < sentence.length() && WordCharacter(sentence.charAt(en)))
                en++;
            if(en > st)
            {
                ocm_word wd = new ocm_word();
                wd.st = st;
                wd.en = en;
                words.add(wd);
                st = en;
            }
        }
        return words;
    }

    void LoadTimingsForWords(List<ocm_word> words,String xmlPath)
    {
        if(xmlPath != null && OBUtils.fileExistsAtPath(xmlPath))
        {
            try
            {
                OBXMLNode xmlNode = null;
                OBXMLManager xmlman = new OBXMLManager();
                List<OBXMLNode> l = xmlman.parseFile(OBUtils.getInputStreamForPath(xmlPath));
                xmlNode = l.get(0);
                List<OBXMLNode> arr = xmlNode.childrenOfType("timings");
                if(arr.size() > 0)
                {
                    OBXMLNode elem = arr.get(0);
                    arr = elem.childrenOfType("timing");
                    int idx = 0;
                    for(ocm_word w : words)
                    {
                        if(idx < arr.size())
                        {
                            OBXMLNode n = arr.get(idx);
                            w.startTime = (float)(n.attributeFloatValue("start"));
                            w.endTime = (float)(n.attributeFloatValue("end"));
                            idx++;
                        }
                    }
                }
            }
            catch(Exception e)
            {

            }
        }
    }

    public boolean showRAButton()
    {
        return true;
    }

    public boolean showNextButton()
    {
        return false;
    }

    public void miscSetUp()
    {
        sentenceDict = LoadMorphologySentencesXML(getLocalPath("morph_sentences.xml"));
        loadEvent("mastera");
        textSize = Float.parseFloat(OBUtils.coalesce(eventAttributes.get("textsize") ,"42"));
        font = StandardReadingFontOfSize(textSize);
    }

    public void prepare()
    {
        theMoveSpeed = bounds().width();
        initialised = true;
        processParams();
        eventsDict = loadXML(getConfigPath(String.format("%s.xml",sectionName())));
        loadAudioXML(getConfigPath(String.format("%saudio.xml",sectionAudioName())));
        loadFingers();
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
                catch(Exception exception)
                {
                }
            }
        });
    }

    public OBLabel createLabelFromText(String tx, OBControl rect)
    {
        OBLabel lab = new OBLabel(tx,font);

        lab.setZPosition(10);
        //lab.setWrapped(true);
        lab.sizeToBoundingBoxMaxWidth(rect.width());
        lab.setLeft(rect.left());
        lab.setTop(rect.top());
        RectF f = lab.frame();
        //f.height() += 2; //Hack due to last line being lost
        lab.setFrame(f);

        return lab;
    }

    public Object findTarget(PointF pt)
    {
        return(finger(-1,3,targets,pt));
    }

    public void readWords(List<ocm_word> words,OBLabel label,String fileName,long token,boolean canInterrupt) throws Exception
    {
        playAudio(fileName);
        long startTime = SystemClock.uptimeMillis();
        for(ocm_word w : words)
        {
            double currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
            double waitTime = w.startTime - currTime;
            if(waitTime > 0.0)
                waitForSecs(waitTime);
            checkSequenceToken(token);
            label.setHighRange(w.st,w.en,Color.RED);
            currTime = (SystemClock.uptimeMillis() - startTime) / 1000.0;
            waitTime = w.endTime - currTime;
            if(waitTime > 0.0 && token == sequenceToken)
                waitForSecs(waitTime);
            label.setColour(Color.BLACK);
            if(canInterrupt)
                checkSequenceToken(token);
        }
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


}
