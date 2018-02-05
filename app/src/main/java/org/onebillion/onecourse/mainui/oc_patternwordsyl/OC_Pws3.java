package org.onebillion.onecourse.mainui.oc_patternwordsyl;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.ArrayMap;

import org.onebillion.onecourse.controls.OBControl;
import org.onebillion.onecourse.controls.OBLabel;
import org.onebillion.onecourse.utils.OBAnim;
import org.onebillion.onecourse.utils.OBAnimationGroup;
import org.onebillion.onecourse.utils.OBFont;
import org.onebillion.onecourse.utils.OBMisc;
import org.onebillion.onecourse.utils.OBPhoneme;
import org.onebillion.onecourse.utils.OBUtils;
import org.onebillion.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 16/01/2018.
 */

public class OC_Pws3 extends OC_Pws
{
    Map<String,List<OBLabel>> rimeColumns;
    Map<String,OBControl> screenColumns;
    Map<String,OBControl> screenColumnsHeaders;
    float fontSize = 55;

    public String eventName()
    {
        return "pws3";
    }

    public String sectionAudioName()
    {
        return "pws3";
    }

    public boolean demomatchShowDropTarget()
    {
        return false;
    }

    public boolean demomatchPlayAudio()
    {
        return true;
    }

    public PointF demomatchDropTargetLoc(OBLabel target)
    {
        String rime = (String)target.propertyValue("rime");
        OBControl cont = screenColumns.get(rime);
        return OBMisc.copyPoint(cont.position());
    }

    public void prepare()
    {
        super.prepare();
        String[] rimeGroups = parameters.get("words").split(";");
        Map<String,List<String>> rimeDict = new ArrayMap();
        for(String rimeString : rimeGroups)
        {
            String[] arr = rimeString.split(":");
            if(arr.length > 1)
            {
                String[] wordIds = arr[1].split(",");
                rimeDict.put(arr[0], Arrays.asList(wordIds));
            }
        }
        prepareLabelsForRimeDict(rimeDict);
        loadTable();
        wordBag.prepareWithObjects((List<OBControl>)(Object)targetLabels);
        loadEventList(OBUtils.getBooleanValue(parameters.get("pattern")),false);
    }

    public void prepareLabelsForRimeDict(Map<String,List<String>> rimeDict)
    {
        targetLabels = new ArrayList<>();
        targetLabelsDict = new ArrayMap<>();
        Map<String,List<OBLabel>> tempDict =new ArrayMap<>();
        rimeColumns = new LinkedHashMap<>();
        float minScale = 1;
        OBControl fitBox = objectDict.get("word_size");
        OBFont font = OBUtils.StandardReadingFontOfSize(fontSize);
        for(String key : rimeDict.keySet())
        {
            rimeColumns.put(key,new ArrayList<OBLabel>());
            tempDict.put(key,new ArrayList<OBLabel>());
            for(String phonemeid : rimeDict.get(key))
            {
                if(componentDict.containsKey(phonemeid))
                {
                    OBPhoneme pho = componentDict.get(phonemeid);
                    OBLabel targetLabel = new OBLabel(pho.text,font);
                    targetLabel.setColour(eventColours.get("normal"));
                    targetLabel.enable();
                    targetLabel.setProperty("rime",key);
                    targetLabel.setProperty("audio",pho.audio());
                    targetLabel.setPosition(OB_Maths.locationForRect(0.5f,0.5f,this.bounds()));
                    targetLabel.setProperty("drop_loc",OBMisc.copyPoint(objectDict.get("drop_area").position()));
                    if(fitBox.width() < targetLabel.width())
                        targetLabel.setScale(targetLabel.width()/fitBox.width());
                    targetLabel.setProperty("anim_scale",1.0f);
                    targetLabel.setZPosition(10);
                    attachControl(targetLabel);
                    targetLabelsDict.put(phonemeid,targetLabel);
                    rimeColumns.get(key).add(targetLabel);
                    tempDict.get(key).add(targetLabel);
                    if(minScale > targetLabel.scale())
                        minScale = targetLabel.scale();
                }
            }
        }
        scaleLabels(new ArrayList<OBLabel>(targetLabelsDict.values()),minScale);
        boolean scan = true;
        while(scan)
        {
            List<OBLabel> arr = new ArrayList<>();
            boolean dataFound = false;
            for(String key : tempDict.keySet())
            {
                if(tempDict.get(key).size() > 0)
                {
                    arr.add(tempDict.get(key).get(0));
                    tempDict.get(key).remove(0);
                    dataFound = true;
                }
            }
            if(arr.size()>0)
                targetLabels.addAll(OBUtils.randomlySortedArray(arr));
            if(!dataFound)
                scan = false;
        }
    }

    public void loadTable()
    {
        screenLabels = new ArrayList<>();
        float extraHeight = 1.1f;
        float extraWidth = 1.2f;
        screenColumns = new ArrayMap<>();
        screenColumnsHeaders = new ArrayMap<>();
        OBControl firstControl = targetLabels.get(0);
        float maxHeight = firstControl.height();
        float maxWidth = firstControl.width();
        for(OBControl con : targetLabels)
        {
            if(con.height() > maxHeight)
                maxHeight = con.height();
            if(con.width() > maxWidth)
                maxWidth = con.width();
        }
        long maxRows = -1;
        List<String> keys = new ArrayList<>(rimeColumns.keySet());
        for(String key : keys)
        {
            if(maxRows < 0 || maxRows < rimeColumns.get(key).size())
                maxRows = rimeColumns.get(key).size();
        }
        OBControl workRect = objectDict.get("word_rect");
        float columnWidth = workRect.width()*0.33f;
        if(columnWidth < maxWidth*extraWidth)
            columnWidth = maxWidth*extraWidth;
        float headerHeight = maxHeight*extraHeight;
        float distFromHeader = 0.25f * headerHeight;
        float columnHeight = maxRows*maxHeight*extraHeight + headerHeight + distFromHeader*2;
        long columnCount = rimeColumns.keySet().size();
        float columnDist = applyGraphicScale(4);
        float fullWidth = columnWidth*columnCount + columnDist*(columnCount-1);
        float left = workRect.position().x -(fullWidth/2);

        for(int i=0; i<columnCount; i++)
        {
            String rime = keys.get(i);
            int colour = eventColours.get(String.format("column_%d",i+1));
            OBControl columnBox = new OBControl();
            columnBox.setFrame(new RectF(0, 0, columnWidth, columnHeight));
            columnBox.setBackgroundColor(colour);
            columnBox.setPosition(workRect.position());
            columnBox.setLeft(left);
            columnBox.setZPosition(1);
            attachControl(columnBox);
            screenColumns.put(rime,columnBox);
            left += columnWidth + columnDist;
            OBControl headerBox = columnBox.copy();
            headerBox.setFrame(new RectF(0, 0, columnWidth, headerHeight));
            headerBox.setPosition(columnBox.position());
            headerBox.setTop(columnBox.top());
            headerBox.setZPosition(1.2f);
            attachControl(headerBox);
            screenColumnsHeaders.put(rime,headerBox);
            columnBox.setOpacity(0.2f);
            OBLabel label = new OBLabel(String.format("_%s",rime), OBUtils.StandardReadingFontOfSize(fontSize));
            label.setColour(eventColours.get("normal"));
            label.setPosition(headerBox.position());
            label.setRight (headerBox.right() -(0.1f * headerBox.width()));
            label.setZPosition(1.5f);
            attachControl(label);
            colourLabel(label,rime);
            float top = headerBox.bottom() + distFromHeader;
            float right = label.right();
            for(int j=0; j<rimeColumns.get(rime).size(); j++)
            {
                OBLabel con = rimeColumns.get(rime).get(j);
                OBLabel copy = (OBLabel)con.copy();
                copy.setTop(top);
                copy.setRight(right);
                copy.hide();
                con.setProperty("target",copy);
                attachControl(copy);
                screenLabels.add(copy);
                top += extraHeight*maxHeight;
            }
        }
    }

    public void colourLabel(OBLabel label,String rime)
    {
        int index = label.text().indexOf(rime);
        label.setHighRange(label.text().length()-rime.length(),index+rime.length(),eventColours.get("pattern"));
    }

    public boolean checkTargetDrop(OBLabel target)
    {
        String rime = (String)target.propertyValue("rime");
        OBControl columnBox = screenColumns.get(rime);
        return columnBox.frame.contains(target.position().x, target.position().y);
    }

    public void animateTargetSnap(OBControl target) throws Exception
    {
        animateWobble = false;
        float startScale = (float)target.propertyValue("start_scale");
        PointF loc = (PointF)target.propertyValue("column_loc");
        List<OBAnim> anims = new ArrayList<>();
        anims.add(OBAnim.scaleAnim(startScale,target));
        anims.add(OBAnim.moveAnim(loc,target));
        anims.add(OBAnim.rotationAnim(0.1f,target));
        OBAnimationGroup.runAnims(anims,0.1f,true,OBAnim.ANIM_EASE_IN_EASE_OUT,this);
        playSfxAudio("match",true);
    }

    public boolean shouldPlayIncorrectAudio(OBControl target)
    {
        boolean playAudio = false;
        for(OBControl con : screenColumns.values())
        {
            if(!con.hidden && con.frame.contains(target.position().x, target.position().y))
            {
                playAudio = true;
                break;
            }
        }
        return playAudio;
    }

    public void highlightAndAudio(OBLabel label) throws Exception
    {
        label.setColour(eventColours.get("highlight"));
        String rime = (String)label.propertyValue("rime");
        playAudio((String)label.propertyValue("audio"));
        waitAudio();
        waitForSecs(0.3f);
        lockScreen();
        label.setColour(eventColours.get("normal"));
        colourLabel(label,rime);
        unlockScreen();
        waitForSecs(0.3f);
    }

    public void demopattern() throws Exception
    {
        loadPointer(POINTER_LEFT);
        List<String> keys = new ArrayList<>(rimeColumns.keySet());
        for(int i=0; i<keys.size(); i++)
        {
            String rime = keys.get(i);
            OBControl column = screenColumns.get(rime);
            List<OBLabel> labels = rimeColumns.get(rime);
            movePointerToPoint(OB_Maths.locationForRect(0.7f,1.15f,column.frame()),-30,0.5f,true);
            if(i==0)
                playAudioQueuedScene("DEMO",300,true);
            waitForSecs(0.3f);
            for(OBLabel label : labels)
                super.highlightAndAudio(label);
        }
        waitForSecs(0.5f);
        thePointer.hide();
        waitForSecs(0.3f);
        displayTick();
        waitForSecs(0.3f);
        nextScene();
    }
}
