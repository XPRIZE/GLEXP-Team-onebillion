package com.maq.xprize.onecourse.mainui.oc_countwhatyousee;

import android.graphics.Color;
import android.util.ArrayMap;

import com.maq.xprize.onecourse.controls.OBControl;
import com.maq.xprize.onecourse.controls.OBGroup;
import com.maq.xprize.onecourse.controls.OBLabel;
import com.maq.xprize.onecourse.controls.OBPath;
import com.maq.xprize.onecourse.mainui.OC_SectionController;
import com.maq.xprize.onecourse.utils.OBUtils;
import com.maq.xprize.onecourse.utils.OB_Maths;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by michal on 15/01/2018.
 */

public class OC_Cwys extends OC_SectionController
{
    public OBGroup currentBlockGroup;

    public void prepare()
    {
        setStatus(STATUS_BUSY);
        super.prepare();
        loadFingers();
        loadEvent("master");
    }

    public void loadUnitsForNumber(int number)
    {
        if(currentBlockGroup != null)
        {
            detachControl(currentBlockGroup);
        }
        OBControl unit = objectDict.get("unit");
        float blockDist = unit.width()*0.5f;
        float centre = OB_Maths.locationForRect(0.5f,0.5f,objectDict.get("work_rect") .frame()) .x;
        float bottom = objectDict.get("work_rect").bottom();

        currentBlockGroup = OC_Cwys_Additions.loadUnitsBlockForNumber(number,unit,blockDist,centre,bottom, this);

    }

    public String eventNameForPrefix(String prefix,int num,boolean last)
    {
        String eventName = null;
        if(last)
        {
            eventName = String.format("%slast",prefix);
        }
        if(!last || audioScenes.get(eventName) == null)
        {
            eventName = String.format("%s%d",prefix,num);
        }
        if(audioScenes.get(eventName) != null)
        {
            return eventName;
        }
        return String.format("%sdefault",prefix);
    }

    public void demoCountUnitsInGroups(OBGroup blockGroup,OBLabel label) throws Exception
    {
        List<OBGroup> hundredBlocks = OC_Cwys_Additions.getHundredBlocksFromBlockGroup(blockGroup);
        List<OBGroup> tenColumns = OC_Cwys_Additions.getTenColumnsFromBlockGroup(blockGroup);
        List<OBControl> units = OC_Cwys_Additions.getUnitsFromBlockGroup(blockGroup);
        if(hundredBlocks.size() != 0)
        {
            lockScreen();
            label.setHighRange(0,1,Color.RED);
            for(OBGroup block : hundredBlocks)
                OC_Cwys_Additions.highlightBlock(block,true);

            unlockScreen();
            playAudio(String.format("plc100_%d",hundredBlocks.size()));
            waitAudio();
            waitForSecs(0.3f);
            lockScreen();
            label.setColour(Color.BLACK);
            for(OBGroup block : hundredBlocks)
                OC_Cwys_Additions.highlightBlock(block,false);

            unlockScreen();
            waitForSecs(0.3f);
        }
        int hiIndex = label.text().length() > 2 ? 1 : 0;
        if(tenColumns.size() != 0)
        {
            lockScreen();
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            for(OBGroup column : tenColumns)
                OC_Cwys_Additions.highlightColumn(column,true);

            unlockScreen();
            playAudio(String.format("plc10_%d",tenColumns.size()));
            waitAudio();
            waitForSecs(0.3f);
            lockScreen();
            label.setColour(Color.BLACK);
            for(OBGroup column : tenColumns)
                OC_Cwys_Additions.highlightColumn(column,false);

            unlockScreen();
            waitForSecs(0.3f);
        }
        else if(hundredBlocks.size() != 0)
        {
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            playAudio("plc10_0");
            waitAudio();
            waitForSecs(0.3f);
            label.setColour(Color.BLACK);
            waitForSecs(0.3f);
        }
        hiIndex = label.text().length()-1;
        if(units.size() != 0)
        {
            lockScreen();
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            for(OBControl unit : units)
                OC_Cwys_Additions.highlightUnit(unit,true);

            unlockScreen();
            playAudio(String.format("plc1_%d",units.size()));
            waitAudio();
            waitForSecs(0.3f);
            lockScreen();
            label.setColour(Color.BLACK);
            for(OBControl unit : units)
                OC_Cwys_Additions.highlightUnit(unit,false);

            unlockScreen();
            waitForSecs(0.3f);
        }
        else
        {
            label.setHighRange(hiIndex,hiIndex+1,Color.RED);
            playAudio("plc1_0");
            waitAudio();
            waitForSecs(0.3f);
            label.setColour(Color.BLACK);
            waitForSecs(0.3f);
        }
    }



}
