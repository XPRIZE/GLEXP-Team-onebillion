package com.maq.xprize.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.mainui.oc_prepr3.OC_PrepR3;
import com.maq.xprize.onecourse.utils.OBFont;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.maq.xprize.onecourse.mainui.oc_prepr3.OC_PrepR3.LoadPassagesXML;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_ParagraphAudioRec extends OC_AudioRecSection
{
    float lineSpacing, textSize;
    OBFont font;
    OBLabel passageLabel;
    OC_PrepR3.Est3_Passage currPassage;
    String currentAudio;
    List<Integer> currentRange;
    int textBackgroundColour;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadEvent("master");
        Map<String,OC_PrepR3.Est3_Passage> storyDict = LoadPassagesXML(getLocalPath("passages.xml"));
        textBackgroundColour = OBUtils.colorFromRGBString("235,235,235");
        textSize = 32;
        lineSpacing = 27;
        String storyId = parameters.get("target");
        currPassage = storyDict.get(storyId);
        font = OBUtils.StandardReadingFontOfSize(textSize);
        setUpPassage();
        List<String> eventsList = new ArrayList<>();
        for(int i=1; i<=currPassage.sentences.size(); i++)
        {
            String eventName = String.format("%d",i);
            if(audioScenes.get(eventName) == null)
                eventName = "default";
            eventsList.add(eventName);
        }
        events = eventsList;
        setSceneXX(currentEvent());
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        currentAudio = audioForSentence(eventIndex);
        currentRange = rangeForSentenceIndex(eventIndex);
        prepareForRecordingEvent(currentAudio);
    }

    @Override
    public void doMainXX() throws Exception
    {
        if(eventIndex != 0)
        {
            waitForSecs(0.3f);
            highlightCurrentSentence();
        }
        demoSceneStart(targetLabel);
        waitForSecs(0.3f);
        playStartAudio("PROMPT",true);
        waitForSecs(0.3f);
        startRecordingEvent(targetLabel);
    }

    @Override
    public void prepareForRecordingEvent(String audio)
    {
        super.prepareForRecordingEvent(audio);
        recordDuration *= 1.5;
    }

    public List<Integer> rangeForSentenceIndex(int idx)
    {
        int len = 0,i = 0;
        for(String s : currPassage.sentences)
        {
            if(len > 0)
                len++;
            if(i == idx)
                return Arrays.asList(len,len + s.length());
            len += s.length();
            i++;
        }
        return Arrays.asList(0,len);
    }

    public String audioForSentence(int sno)
    {
        return String.format("comp_%s_p_p_%d",currPassage.passageID,sno + 1);
    }

    public void setUpPassage()
    {
        StringBuilder passageText = new StringBuilder();
        for(String s : currPassage.sentences)
        {
            if(passageText.length() > 0)
                passageText.append(" ");
            passageText.append(s);
        }
        OBControl textBox = objectDict.get("textbox");
        passageLabel = new OBLabel(passageText.toString(),font);
        passageLabel.setLineSpaceAdd(lineSpacing);
        passageLabel.setZPosition(50);
        passageLabel.setColour(Color.BLACK);
        passageLabel.setAlignment(OBLabel.OBLABEL_ALIGN_LEFT);
        //passageLabel.setWrapped(true);
        passageLabel.sizeToBoundingBoxMaxWidth(textBox.width());
        passageLabel.setLeft(textBox.left());
        passageLabel.setTop(textBox.top());
        RectF f = new RectF(passageLabel.frame());
        //f.bottom += textSize / 2; //Hack due to last line being lost
        passageLabel.setFrame(f);
        passageLabel.setBackgroundColor(Color.WHITE);
        attachControl(passageLabel);
        passageLabel.setPosition(textBox.position());
        PointF pf = new PointF();
        pf.set(passageLabel.position());
        passageLabel.setProperty("origpos",pf);
        passageLabel.hide();
    }

    @Override
    public void recordingEventFinished() throws Exception
    {
        highlightPassage(Color.BLUE,currentRange);
        waitForSecs(0.3f);
        playTargetAudio();
        waitForSecs(1f);
        highlightPassage(Color.BLACK,currentRange);
        waitForSecs(0.5f);
        List<String> audio = getAudioForScene(currentEvent() ,"FINAL2");
        if(audio != null)
        {
            playAudio(audio.get(0));
            waitForAudio();
        }
        waitForSecs(1f);
        if(isLastEvent())
        {
            demoReadParagraph();
        }
        nextScene();
    }

    public void highlightPassage(int colour,List<Integer> range) throws Exception
    {
        passageLabel.setHighRange(range.get(0),range.get(1),colour);
    }

    @Override
    public void animateWordRecordStart(OBLabel curLabel,boolean pulse) throws Exception
    {
        lockScreen();
        highlightPassage(Color.RED,currentRange);
        playSfxAudio("ping",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void animateWordRecordStop(OBLabel curLabel) throws Exception
    {
        recordingButton.stopRecordingAnimation();
        lockScreen();
        highlightPassage(Color.BLACK,currentRange);
        recordingButton.setState(OC_AudioRecordingButton.BUTTON_STATE_INACTIVE);
        playSfxAudio("cancel",false);
        unlockScreen();
        waitSFX();
    }


    public void highlightCurrentSentence() throws Exception
    {
        lockScreen();
        passageLabel.setBackgroundColourRanges(Arrays.asList(currentRange),textBackgroundColour);
        playSfxAudio("line",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void playTargetAudio() throws Exception
    {
        playAudio(currentAudio);
        waitForAudio();
    }

    @Override
    public void demoInitScreen() throws Exception
    {
        waitForSecs(0.3f);
        playSfxAudio("paraon",false);
        passageLabel.show();
        waitSFX();
        waitForSecs(0.7f);
        super.demoInitScreen();
    }

    @Override
    public void demoPreExample() throws Exception
    {
        waitForSecs(0.3f);
        highlightCurrentSentence();
    }

    public void demoReadParagraph() throws Exception
    {
        passageLabel.setBackgroundColourRanges(null,Color.WHITE);
        waitForSecs(0.5f);
        playAudioQueuedScene("end","DEMO", 0.3f,true);
        waitForSecs(0.6f);
        for(int i=0; i<currPassage.sentences.size(); i++)
        {
            List<Integer> range = rangeForSentenceIndex(i);
            String audio = audioForSentence(i);
            highlightPassage(Color.BLUE,range);
            playAudio(audio);
            waitForAudio();
            waitForSecs(0.3f);
            highlightPassage(Color.BLACK,range);
            waitForSecs(0.3f);
        }
    }

    @Override
    public void demoEvent1() throws Exception
    {
        if(!OBUtils.getBooleanValue(parameters.get("example")))        highlightCurrentSentence();
        if(thePointer == null || thePointer.hidden)        loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.2f,0.2f,passageLabel.frame()),-25,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-30,0.5f,"DEMO",2,0.5f);
        thePointer.hide();
    }
}
