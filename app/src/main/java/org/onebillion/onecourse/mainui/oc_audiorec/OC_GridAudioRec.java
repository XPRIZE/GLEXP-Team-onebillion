package org.onebillion.onecourse.mainui.oc_audiorec;

import android.graphics.Color;
import android.graphics.PointF;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.controls.OBPath;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 08/06/2018.
 */

public class OC_GridAudioRec extends OC_AudioRecSection
{
    static int MODE_WORD=1, MODE_SYLLABLE=2, MODE_NONWORD=3, MODE_NUMBER=4;
    List<List<OBControl>> screenBoxes;
    List<OBLabel> screenLabels;
    List<OBLabel> eventLabels;
    Map<String,Integer> eventColours;
    int gridSizeX, gridSizeY;
    int currentMode;
    OBPhoneme targetPhoneme;

    @Override
    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        String[] sizes = parameters.get("size").split(",");
        gridSizeX = OBUtils.getIntValue(sizes[0]);
        gridSizeY = OBUtils.getIntValue(sizes[1]);
        loadEvent("master");
        currentMode = MODE_WORD;
        String mode = parameters.get("mode");
        if(mode.equals("syllable"))
        {
            currentMode = MODE_SYLLABLE;
            mergeAudioScenesForPrefix("SYL");

        }
        else if(mode.equals("nonword"))
        {
            currentMode = MODE_NONWORD;
            mergeAudioScenesForPrefix("NW");

        }
        else if(mode.equals("number"))
        {
            currentMode = MODE_NUMBER;
            mergeAudioScenesForPrefix("NUM");

        }
        eventColours = OBMisc.loadEventColours(this);
        loadBoxes();
        loadEventData();
        setSceneXX(currentEvent());
    }

    @Override
    public void setSceneXX(String scene)
    {
        super.setSceneXX(scene);
        targetLabel = eventLabels.get(eventIndex);
        targetPhoneme = (OBPhoneme)targetLabel.propertyValue("phoneme");
        prepareForRecordingEvent(targetPhoneme.audio());
    }

    @Override
    public void doMainXX() throws Exception
    {
        waitForSecs(0.3f);
        if(eventIndex != 0)
        {
            highlightNextLabel();
            waitForSecs(0.3f);
        }
        demoSceneStart(targetLabel);
        playStartAudio("PROMPT",true);
        waitForSecs(0.15f);
        startRecordingEvent(targetLabel);
    }

    public void highlightNextLabel() throws Exception
    {
        OBLabel currentLabel = eventLabels.get(eventIndex);
        OBLabel lastLabel = eventIndex > 0 ? eventLabels.get(eventIndex-1) : null;
        lockScreen();
        OBControl currentBox = (OBControl)currentLabel.propertyValue("box");
        currentBox.show();
        if(lastLabel != null)
        {
            lastLabel.setColour(eventColours.get("text_lowlight"));
            OBControl lastBox = (OBControl)lastLabel.propertyValue("box");
            lastBox.hide();
        }
        playSfxAudio("hilite",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void prepareForRecordingEvent(String audio)
    {
        if(currentMode == MODE_NUMBER)
        {
            int num = OBUtils.getIntValue(targetPhoneme.text);
            if(num <= 10)
                recordDuration = 3;
            else if(num <100)
                recordDuration = 4;
            else
                recordDuration = 8;
        }
        else
        {
            super.prepareForRecordingEvent(audio);
        }
    }

    public void loadBoxes()
    {
        screenBoxes = new ArrayList<>();
        OBControl box = objectDict.get("box");
        OBControl workRect = objectDict.get("work_rect");
        float lineWidth = box.borderWidth;
        PointF centrePoint = OB_Maths.locationForRect(0.5f,0.5f,workRect.frame());
        float top =  (float)Math.floor(centrePoint.y - (gridSizeY/2.0) *box.height() +(gridSizeY/2.0) *lineWidth);
        float startLeft = (float)Math.floor(centrePoint.x - (gridSizeX/2.0) *box.width() +(gridSizeX/2.0) *lineWidth);
        for(int i=1; i<=gridSizeY; i++)
        {
            float left = startLeft;
            List<OBControl> boxes = new ArrayList<>();
            for(int j=1; j<=gridSizeX; j++)
            {
                OBPath boxCopy = (OBPath)box.copy();
                attachControl(boxCopy);
                boxCopy.setTop(top);
                boxCopy.setLeft(left);
                left = boxCopy.right() - lineWidth;
                boxCopy.hide();
                boxes.add(boxCopy);
                boxCopy.setZPosition(5);
                if(j==gridSizeX)
                    top = boxCopy.bottom()-lineWidth;

                boxCopy.sizeToBoundingBoxIncludingStroke();
            }
            screenBoxes.add(boxes);
        }
    }


    public void loadEventData()
    {
        List<String> eventsList = new ArrayList<>();
        List<OBPhoneme> phonemes = new ArrayList<>();
        eventLabels = new ArrayList<>();
        int index = 1, max = gridSizeX*gridSizeY;
        String[] targets = parameters.get("targets").split(",");
        if(currentMode == MODE_NUMBER)
        {
            for(int i=0; i<targets.length; i++)
            {
                int rangeCount = OBUtils.getIntValue(targets[i]);
                int from = (int)Math.pow(10,i);
                int to = (int)Math.pow(10,i+1)-1;
                if(to > 700)
                    to=700;
                List<Integer> numList = OBMisc.integerList(from,to);
                if(i == targets.length-1)
                {
                    int currentTotal = phonemes.size() + rangeCount;
                    if(currentTotal < max)
                        rangeCount = max - phonemes.size();

                }
                List<Integer> randNumList = null;
                for(int j=0; j<rangeCount; j++)
                {
                    if(j%numList.size() == 0)
                        randNumList = OBUtils.randomlySortedArray(numList);
                    int number =randNumList.get(j%randNumList.size()).intValue();
                    String audioId = String.format("n_%d",number);
                    OBPhoneme pho = new OBPhoneme(String.format("%d",number),audioId);
                    phonemes.add(pho);
                }
            }
        }
        else
        {
            Map<String,OBPhoneme> componentDict = OBUtils.LoadWordComponentsXML(true);
            for(String wordId : OBUtils.randomlySortedArray(Arrays.asList(targets)))
            {
                if(componentDict.get(wordId) != null)
                {
                    phonemes.add(componentDict.get(wordId));
                    index++;
                    if(index > max)
                        break;
                }
            }
        }
        index = 0;
        float scale = 1;
        for(List<OBControl> row : screenBoxes)
        {
            for(OBControl box : row)
            {
                OBPhoneme phoneme = phonemes.get(index%phonemes.size());
                index++;
                OBLabel label = new OBLabel(phoneme.text,OBUtils.StandardReadingFontOfSize(currentMode != MODE_NUMBER ? 40 : 60));
                label.setColour(Color.BLACK);
                label.setPosition(box.position());
                attachControl(label);
                if(label.width() > 0.8*box.width())
                    label.setScale(0.8f*box.width()/label.width());
                label.setProperty("box",box);
                label.setProperty("phoneme",phoneme);
                label.setZPosition(10);
                label.hide();
                eventLabels.add(label);
                String eventName = String.format("%d",index);
                if(audioScenes.get(eventName) == null)
                    eventName = "default";
                eventsList.add(eventName);
                if(scale > label.scale())
                    scale = label.scale();
            }
        }
        for(OBLabel label : eventLabels)
            label.setScale(scale);
        events = eventsList;
    }

    public void showGrid() throws Exception
    {
        lockScreen();
        for(OBControl con : eventLabels)
            con.show();
        playSfxAudio("fillgrid",false);
        unlockScreen();
        waitSFX();
    }

    @Override
    public void recordingEventFinished() throws Exception
    {
        targetLabel.setColour(Color.BLUE);
        waitForSecs(0.3f);
        playTargetAudio();
        waitForSecs(0.3f);
        targetLabel.setColour(Color.BLACK);
        waitForSecs(0.5f);
        List<String> audio = getAudioForScene(currentEvent() ,"FINAL2");
        if(audio != null)
        {
            playAudio(audio.get(0));
            waitForAudio();
            waitForSecs(1f);
        }
        waitForSecs(0.5f);
        if(isLastEvent())
        {
            lockScreen();
            OBControl box = (OBControl)targetLabel.propertyValue("box");
            box.hide();
            targetLabel.setColour(eventColours.get("text_lowlight"));
            unlockScreen();
            waitForSecs(0.5f);
        }
        nextScene();
    }

    public void playTargetAudio() throws Exception
    {
        targetPhoneme.playAudio(this,true);
    }

    public void clearScene() throws Exception
    {
        lockScreen();
        for(OBLabel label : screenLabels)
            detachControl(label);
        screenLabels = null;
        targetLabel = null;
        playSfxAudio("allumbersoff",false);
        unlockScreen();
        waitSFX();
    }

    public void nextButtonPressed() throws Exception
    {

    }

    public void demoIntro1() throws Exception
    {
        super.demoIntro1();
        showGrid();
    }

    public void demoIntro2() throws Exception
    {
        highlightNextLabel();
        super.demoIntro2();
        if(!showPresenter && currentMode == MODE_NONWORD)
        {
            movePointerToPoint(OB_Maths.locationForRect(0.9f,0.8f,this.bounds()),-30,0.5f,true);
            playAudio(getAudioForScene("intro2","DEMO") .get(2));
            waitAudio();
            waitForSecs(0.5f);

        }
        waitForSecs(0.3f);
    }

    public void demoEvent1() throws Exception
    {
        OBPath box = (OBPath)targetLabel.propertyValue("box");
        waitForSecs(0.3f);
        if(thePointer == null || thePointer.hidden)
            loadPointer(POINTER_LEFT);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-20,0.5f,"DEMO",0,0.3f);
        moveScenePointer(OB_Maths.locationForRect(0.6f,1.1f,box.frame()),-30,0.5f,"DEMO",1,0.3f);
        moveScenePointer(OB_Maths.locationForRect(1.1f,0.9f,recordingButton.control.frame()),-25,0.5f,"DEMO",2,0.3f);
        thePointer.hide();
    }

    public void demoEvent2() throws Exception
    {
        playAudioQueuedScene("DEMO",true);
        thePointer.hide();
    }



}
